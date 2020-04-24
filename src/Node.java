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
    private String id;
    private Map<String, String> exchangeMap, routingTable;
    private Logger logger;

    Node(String id, Map<String, String> exchangeMap, Map<String, String> routingTable)
            throws IOException, TimeoutException {
        this.id = id;
        this.routingTable = routingTable;
        this.exchangeMap = exchangeMap;

        // Initialize the logging
        logger = new Logger(id);
        logger.log("Node " + id + ": Initializing...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        logger.log("Node " + id + ": Setting exchange channels...");
        for (String exKey : exchangeMap.keySet()) {
            Channel channel = connection.createChannel();
            String exId = exchangeMap.get(exKey);
            channel.exchangeDeclare(exId, BuiltinExchangeType.DIRECT);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, exId, id);
            channel.basicConsume(queue, true, receiveCallback, consumerTag -> {
            });
            logger.log("Node " + id + ": Exhange channel " + exId + " initialized.");
        }
        // Log the creation of the node
        logger.log("Node " + id + ": Exchange channels set.");
        logger.log("Node " + id + ": Ready.");
    }

    public String getId() {
        return id;
    }

    public void send(Message message) throws IOException {
        send(message, false);
    }

    public void forward(Message message) throws IOException {
        send(message, true);
    }

    private void send(Message message, boolean forward) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(message);
        String nextHop = routingTable.get(message.getDestination());
        if (message.getMessage() == null) {
            throw new IllegalArgumentException("Invalid 'message' argument.");
        } else if (message.getDestination() == null) {
            throw new IllegalArgumentException("Invalid 'target' argument.");
        }
        String exId = exchangeMap.get(nextHop);
        ConnectionFactory factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exId, BuiltinExchangeType.DIRECT);
            channel.basicPublish(exId, nextHop, null, os.toByteArray());
            objectOutputStream.close();
            // Log the passage by this node
            if (forward) {
                logger.log("Node " + id + ": Forwarding Message " + message.getId() + "...");
            } else {
                logger.log("Node " + id + ": Sending Message " + message.getId() + "...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DeliverCallback receiveCallback = (consumerTag, delivery) -> {
        ByteArrayInputStream is = new ByteArrayInputStream(delivery.getBody());
        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            Message msg = (Message) ois.readObject();
            if ((msg.getMessage() == null) || (msg.getDestination() == null)) {
                throw new IllegalArgumentException("Invalid 'message' format.");
            }
            if (id.equals(msg.getDestination())) {
                // This node is the final hop. Log the message.
                logger.log("Node " + id + ": Message " + msg.getId() + " received.");
                logger.log("Node " + id + ": " + msg);
            } else {
                // Forward the message to the next hop
                forward(msg);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error when trying to deserialize package.");
        }
        return;
    };
}
