import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Node {
    public static String EXCHANGE_NAME = "DIRECT";

    private String id;
    private List<String> neighbors;

    Node(String id, List<String> neighbors) throws IOException, TimeoutException {
        this.id = id;
        this.neighbors = neighbors;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        String queue = channel.queueDeclare().getQueue();
        channel.queueBind(queue, EXCHANGE_NAME, id);
        channel.basicConsume(queue, true, receiveCallback, consumerTag -> {
        });
    }

    void send(String message, String target) {
        if (message == null) {
            throw new IllegalArgumentException("Invalid 'message' argument.");
        } else if (!neighbors.contains(target)) {
            throw new IllegalArgumentException("Invalid 'target' argument.");
        }
        ConnectionFactory factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(EXCHANGE_NAME, target, null, message.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DeliverCallback receiveCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");

        // TODO: determine whether this node is the final target.
        /** Idea 1: Message contains metadata header of a form like this:
         * "$id_0, id_1, ..., id_f$ - message"
         * This metadata represents the physical nodes left to reach
         * the destination.
         */
        /** Idea 2: Message contains metadata header of a form like this:
         * "$sender, reciver$ - message"
         * This assumes that the physical node knows the shortest path
         * to every other node and can determine the next hop based on
         * the sender's id. While this requires more pre-computation, it 
         * reduces the payload being sent (i.e. smaller metadata)
         */
        /** Idea 3: Message contains metadata header of a form like this:
         * "$sender, reciver$ - message"
         * Similar to Idea 2, but instead of storing the entirety of paths,
         * each node only stores the next hop to be taken in the case of
         * each final destination. For example:
         * next_hops = {x0_f -> 2, x1_f -> 8, x2_f -> 3, ...}
         * Maybe this would help us precompute the paths a bit more efficiently
         */
        
        boolean isFinalDestination = false; // temp variable value
        String target = "temp";             // temp variable value
        if (isFinalDestination) {
            // This node is the final target. Print the message.
            System.out.println(message);
        } else {
            // This node is NOT the final target.
            // Process the message meta-data
            message = processMetadata(message);
            // Send the message to the next target
            send(message, target);
        }
    };

    private String processMetadata(String message) {
        // TODO: implement processing of the metadata
        return message;
    }

}