import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    // A shared file between all the nodes. Could be somewhere on the internet...
    private final String logFile = "comms.log";
    private PrintWriter writer;

    public Logger(String id) {
        try {
            FileWriter fw = new FileWriter(logFile, true);
            writer = new PrintWriter(fw, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Object obj) {
        // Timestamp could vary from one machine to another
        writer.println(System.currentTimeMillis() + ": " + obj.toString());
    }
}