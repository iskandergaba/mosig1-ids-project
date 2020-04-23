import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1234L;

	private static int ID = 0;
	private String src, dest, message;

	public Message(String message) {
		ID++;
		this.message = message;
	}

	public String getDestination() {
		return dest;
	}

	public void setDestination(String destination) {
		this.dest = destination;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSource() {
		return src;
	}

	public void setSource(String src) {
		this.src = src;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Message #").append(ID).append(" - ").append(src).append(" => ").append(dest).append(":").append(message);
		return sb.toString();
	}
}
