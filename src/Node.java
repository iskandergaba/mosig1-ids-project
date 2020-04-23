
//Why does it print the result after the menu? 
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class Node {
    public static String EXCHANGE_NAME = "DIRECT";

    private String id;
    private Map<String, String> routingTable;

    Node(String id, Map<String, String> routingTable) throws IOException, TimeoutException {
        this.id = id;
        this.routingTable = routingTable;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queue = channel.queueDeclare().getQueue();
        channel.queueBind(queue, EXCHANGE_NAME, id);
        channel.basicConsume(queue, true, receiveCallback, consumerTag -> {
        });
    }

    public String getId() {
        return id;
    }

    public void send(Message message) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(message);
        String nextHop = routingTable.get(message.getDestination());
        if (message.getMessage() == null) {
            throw new IllegalArgumentException("Invalid 'message' argument.");
        } else if (message.getDestination() == null) {
            throw new IllegalArgumentException("Invalid 'target' argument.");
        }
        ConnectionFactory factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(EXCHANGE_NAME, nextHop, null, os.toByteArray());
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DeliverCallback receiveCallback = (consumerTag, delivery) -> {
        // String message = new String(delivery.getBody(), "UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(delivery.getBody());
        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            Message msg = (Message) ois.readObject();
            if ((msg.getMessage() == null) || (msg.getDestination() == null)) {
                throw new IllegalArgumentException("Invalid 'message' format.");
            }
            if (id.equals(msg.getDestination())) {
                // This node is the final hop. Print the message.
                System.out.println(msg.toString());
            } else {
                //System.out.println(id + ": Forwarding");
                // Send the message to the next hop
                send(msg);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error when trying to deserialize package.");
        }
        return;
    };
}
