import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.LinkedList;
import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;

public class Messenger {

    private static final String LOG_FILE = "history.log";

    private String username;
    private static final Logger logger = Logger.getInstance();

    public Messenger() {
        this.username = "SYSTEM";
    }

    public void monocast(String message, String room, String recepient, boolean history) {
        ConnectionFactory factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            String text = history ? message
                    : new StringBuilder("[").append(username).append("]: ").append(message).toString();
            channel.exchangeDeclare(room, BuiltinExchangeType.DIRECT);
            channel.basicPublish(room, recepient, null, text.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message, String room, boolean includeUsername) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            String text = includeUsername
                    ? new StringBuilder("[").append(username).append("]: ").append(message).toString()
                    : message;
            channel.exchangeDeclare(room, BuiltinExchangeType.FANOUT);
            channel.basicPublish(room, "", null, text.getBytes("UTF-8"));
            if (includeUsername) {
                logger.log(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayHistory(String room) {
        List<String> history = fetchHistory();
        for (String message : history) {
            monocast(message, room, username, true);
        }
    }

    private List<String> fetchHistory() {
        List<String> history = new LinkedList<>();
        try {
            File logFile = new File(LOG_FILE);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(logFile));
            String msg = null;
            while ((msg = in.readLine()) != null) {
                history.add(msg);
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        return history;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
