import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1234L;

	public enum Direction {
		Left, Right, Direct
	}

	private int id;
	private String src, dest, message;
	private Direction direction = Direction.Direct;

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

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSource() {
		return src;
	}

	public void setSource(String setSrc) {
		this.src = setSrc;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("M").append(id).append(" - ").append(src).append(" => ").append(dest).append(": ").append(message);
		return sb.toString();
	}
}
