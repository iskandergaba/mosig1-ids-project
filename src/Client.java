import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Client {
    private static final String CHAT_ROOM = "CHAT_ROOM";
    private static final String CHAT_DIRECT = "DM";
    private static final String SYS_REQ = "SYS_REQ";
    private static final String SYS_RCV = "SYS_RCV";
    private static Set<String> users = new HashSet<>();

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel idReqChannel = connection.createChannel();
        Channel idRcvChannel = connection.createChannel();
        Channel monocastChannel = connection.createChannel();
        Channel broadcastChannel = connection.createChannel();

        Messenger messenger = new Messenger();

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
        System.out.println("[System] Client chat is running.");
        System.out.print("Enter a username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        while (users.contains(username) || username.isEmpty()) {
            messenger.broadcast("", SYS_REQ, false);
            System.out.print(username + " is invalid. Enter another username: ");
            scanner = new Scanner(System.in);
            username = scanner.nextLine();
        }
        // Legal username. Set the messenger
        messenger.setUsername(username);

        // Broadcast new client username
        messenger.broadcast(username, SYS_RCV, false);

        broadcastChannel.exchangeDeclare(CHAT_ROOM, BuiltinExchangeType.FANOUT);
        String broadcastQueue = broadcastChannel.queueDeclare().getQueue();
        broadcastChannel.queueBind(broadcastQueue, CHAT_ROOM, "");
        DeliverCallback messageCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(message);
        };
        broadcastChannel.basicConsume(broadcastQueue, true, messageCallback, consumerTag -> {
        });

        monocastChannel.exchangeDeclare(CHAT_DIRECT, BuiltinExchangeType.DIRECT);
        String monocastQueue = monocastChannel.queueDeclare().getQueue();
        monocastChannel.queueBind(monocastQueue, CHAT_DIRECT, username);
        monocastChannel.basicConsume(monocastQueue, true, messageCallback, consumerTag -> {
        });

        System.out.println("Welcome, " + username + "!");
        System.out.println("Type: 'q!' to leave the chat");
        System.out.println("Type: '-h' to get the message history");
        System.out.println("Type: '-c' to connect with particular user");
        System.out.println("Type anything else to send as a message");
        System.out.println();

        messenger.broadcast("Just joined the server!", CHAT_ROOM, true);
        while (true) {
            String message = scanner.nextLine();
            if ("q!".equals(message)) {
                messenger.broadcast("Left the server.", CHAT_ROOM, true);
                // Let everybody know you are leaving so that
                // they remove you from their user list
                messenger.broadcast(username, SYS_REQ, false);
                // Free resources
                broadcastChannel.close();
                monocastChannel.close();
                idReqChannel.close();
                idRcvChannel.close();
                scanner.close();
                // Exit
                System.exit(0);
            } else if ("-h".equals(message)) {
                messenger.displayHistory(CHAT_DIRECT);
            } else if ("-c".equals(message)) {
                System.out.println("Direct chat mode ednabled.");
                System.out.print("Enter a username: ");
                String recepient = scanner.nextLine();
                if (!users.contains(recepient) || recepient.isEmpty()) {
                    System.out.println("Invalid username.");
                } else {
                    System.out.print("Write message: ");
                    message = scanner.nextLine();
                    messenger.monocast(message, CHAT_DIRECT, recepient, false);
                    System.out.println("Sent.");
                }
                System.out.println("Direct chat mode disabled.\n");
            } else {
                messenger.broadcast(message, CHAT_ROOM, true);
            }
        }
    }
}
