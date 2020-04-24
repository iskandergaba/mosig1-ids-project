import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1234L;

	private int id;
	private String src, dest, message;

	public Message(int id, String message) {
		this.id = id;
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

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Message #").append(id).append(" - Node ").append(src).append(" => Node ").append(dest).append(": \"")
				.append(message).append("\"");
		return sb.toString();
	}
}
