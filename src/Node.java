import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

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

    void send(String message, String target) {
        String nextHop = routingTable.get(target);
        if (message == null) {
            throw new IllegalArgumentException("Invalid 'message' argument.");
        } else if (target == null) {
            throw new IllegalArgumentException("Invalid 'target' argument.");
        }
        ConnectionFactory factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(EXCHANGE_NAME, nextHop, null, message.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DeliverCallback receiveCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");

        String[] tokens = message.split(",", 2);
        if (tokens.length < 2) {
            throw new IllegalArgumentException("Invalid 'message' format.");
        }

        String target = tokens[0];
        if (id.equals(target)) {
            // This node is the final hop. Print the message.
            System.out.println(tokens[1]);
        } else {
            // Send the message to the next hop
            send(message, target);
        }
    };
}
