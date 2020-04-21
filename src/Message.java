import java.io.Serializable;

public class Message implements Serializable{
	public enum Direction {
		Left, Right, Direct
	}
	private int id;
	private String dest; 
	private int src;
	private String message;
	private Direction direction = Direction.Direct;

	public Message(int id) {
		this.id = id;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("M").append(id).append(": ").append(message).append(" to: ").append(dest)
				.append(", from: ").append(src);
		return sb.toString();
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

	public int getSource() {
		return src;
	}

	public void setSource(int setSrc) {
		this.src = setSrc;
	}
}
