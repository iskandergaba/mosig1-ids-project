import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class ClientGUI {
  private static final String CHAT_ROOM = "CHAT_ROOM";
  private static final String CHAT_DIRECT = "DM";
  private static final String SYS_REQ = "SYS_REQ";
  private static final String SYS_RCV = "SYS_RCV";
  private static Set<String> users = new HashSet<>();
  Messenger messenger;
  Connection connection;
  Channel idReqChannel;
  Channel idRcvChannel;
  Channel broadcastChannel;
  Channel monocastChannel;

  public void init() throws Exception {
    messenger = new Messenger();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    connection = factory.newConnection();
    idReqChannel = connection.createChannel();
    idRcvChannel = connection.createChannel();

    // Illegal usernames to not interfere with channel comms.
    users.add(SYS_REQ);
    users.add(SYS_RCV);

    idReqChannel.exchangeDeclare(SYS_REQ, BuiltinExchangeType.FANOUT);
    String idReqQueue = idReqChannel.queueDeclare().getQueue();
    idReqChannel.queueBind(idReqQueue, SYS_REQ, "");
    DeliverCallback idReqCallback = (consumerTag, delivery) -> {
      String user = new String(delivery.getBody(), "UTF-8");
      // If requesting username is sent, remove it.
      // Else, send back the reciever username
      if (!user.equals(SYS_REQ) && !user.equals(SYS_RCV) && users.contains(user)) {
        users.remove(user);
      } else {
        messenger.broadcast(messenger.getUsername(), SYS_RCV, false);
      }
    };
    idReqChannel.basicConsume(idReqQueue, true, idReqCallback, consumerTag -> {
    });

    idRcvChannel.exchangeDeclare(SYS_RCV, BuiltinExchangeType.FANOUT);
    String idRcvQueue = idRcvChannel.queueDeclare().getQueue();
    idRcvChannel.queueBind(idRcvQueue, SYS_RCV, "");
    DeliverCallback idRcvCallback = (consumerTag, delivery) -> {
      String user = new String(delivery.getBody(), "UTF-8");
      // A set will only add an element if it does not exist already
      users.add(user);
    };
    idRcvChannel.basicConsume(idRcvQueue, true, idRcvCallback, consumerTag -> {
    });
    // Request usernames from all othre clients
    messenger.broadcast("", SYS_REQ, false);
  }

  public void doConnect() {
    // Request usernames from all othre clients
    messenger.broadcast("", SYS_REQ, false);

    if (connect.getText().equals("Connect")) {
      if (name.getText().length() < 2) {
        JOptionPane.showMessageDialog(frame, "You need to type a name.");
        return;
      } else if (users.contains(name.getText())) {
        JOptionPane.showMessageDialog(frame, "Username already taken, try another username.");
        return;
      }
      try {
        String username = name.getText();
        // Legal username. Set the messenger
        messenger.setUsername(username);

        // Broadcast new client username
        messenger.broadcast(username, SYS_RCV, false);

        JOptionPane.showMessageDialog(frame, "[System] Client chat is running");
        connect.setText("Disconnect");
        broadcastChannel = connection.createChannel();
        monocastChannel = connection.createChannel();

        monocastChannel.exchangeDeclare(CHAT_DIRECT, BuiltinExchangeType.DIRECT);
        String monocastQueue = monocastChannel.queueDeclare().getQueue();
        monocastChannel.queueBind(monocastQueue, CHAT_DIRECT, username);

        broadcastChannel.exchangeDeclare(CHAT_ROOM, BuiltinExchangeType.FANOUT);
        String broadcastQueue = broadcastChannel.queueDeclare().getQueue();
        broadcastChannel.queueBind(broadcastQueue, CHAT_ROOM, "");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          tx.setText(tx.getText() + "\n" + message);
        };
        monocastChannel.basicConsume(monocastQueue, true, deliverCallback, consumerTag -> {
        });
        broadcastChannel.basicConsume(broadcastQueue, true, deliverCallback, consumerTag -> {
        });
        messenger.broadcast("Just joined the server!", CHAT_ROOM, true);
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(frame, "ERROR, we couldn't connect....");
      }
    } else {
      disconnect();
    }
  }

  public void disconnect() {
    if (connect.getText().equals("Disconnect")) {
      try {
        messenger.broadcast("Left the server.", CHAT_ROOM, true);
        messenger.broadcast(messenger.getUsername(), SYS_REQ, false);
        idReqChannel.close();
        idRcvChannel.close();
        broadcastChannel.close();
        monocastChannel.close();
        System.exit(0);
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(frame, "ERROR, can't disconnect...");
      }
    }
  }

  public void sendText() {
    if (connect.getText().equals("Connect")) {
      JOptionPane.showMessageDialog(frame, "You need to connect first.");
      return;
    }
    String st;
    st = tf.getText();
    tf.setText("");
    try {
      messenger.broadcast(st, CHAT_ROOM, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void historyText() {
    if (connect.getText().equals("Connect")) {
      JOptionPane.showMessageDialog(frame, "You need to connect first.");
      return;
    }
    try {
      tx.setText("");
      messenger.displayHistory(CHAT_DIRECT);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void writeMsg(String st) {
    tx.setText(tx.getText() + "\n" + st);
  }

  public static void main(String[] args) throws Exception {
    new ClientGUI();
  }

  // User Interface code.
  public ClientGUI() throws Exception {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    init();
    frame = new JFrame("Group Chat");
    JPanel main = new JPanel();
    JPanel top = new JPanel();
    JPanel cn = new JPanel();
    JPanel bottom = new JPanel();
    tf = new JTextField();
    name = new JTextField();
    tx = new JTextArea();
    tx.setEditable(false);
    connect = new JButton("Connect");
    JButton bt = new JButton("Send");
    JButton ht = new JButton("History");
    main.setLayout(new BorderLayout(5, 5));
    top.setLayout(new GridLayout(1, 0, 5, 5));
    cn.setLayout(new BorderLayout(5, 5));
    bottom.setLayout(new BorderLayout(5, 5));
    JLabel q1 = new JLabel("Username: ");
    q1.setHorizontalAlignment(JLabel.CENTER);
    top.add(q1);
    top.add(name);
    top.add(connect);
    cn.add(new JScrollPane(tx), BorderLayout.CENTER);
    bottom.add(tf, BorderLayout.CENTER);
    bottom.add(bt, BorderLayout.EAST);
    bottom.add(ht, BorderLayout.WEST);
    main.add(top, BorderLayout.NORTH);
    main.add(cn, BorderLayout.CENTER);
    main.add(bottom, BorderLayout.SOUTH);
    main.setBorder(new EmptyBorder(10, 10, 10, 10));
    // Events
    connect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doConnect();
      }
    });
    bt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendText();
      }
    });
    ht.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        historyText();
      }
    });
    tf.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendText();
      }
    });
    frame.setContentPane(main);
    frame.setSize(600, 600);
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        disconnect();
        System.exit(0);
      }
    });
  }

  JTextArea tx;
  JTextField tf, name;
  JButton connect;
  JFrame frame;
}