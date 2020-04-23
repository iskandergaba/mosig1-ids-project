import java.io.IOException;

public class VirtualNode {
    private Node Node;
    private String leftNeighbour;
    private String rightNeighbour;

    VirtualNode(Node Node) throws IOException {
        this.Node = Node;
        this.leftNeighbour = leftNeighbour;
        this.rightNeighbour = rightNeighbour;
    }

    public void setLeftNeighbour(String leftNeighbour) {
		this.leftNeighbour = leftNeighbour;
	}

	public String getLeftNeighbour() {
		return leftNeighbour;
	}

	public void setRightNeighbour(String rightNeighbour) {
		this.rightNeighbour = rightNeighbour;
	}

    public String getRightNeighbour() {
		return rightNeighbour;
	}

    public void SendLeft(Message msg) throws IOException {
        msg.setDirection(Message.Direction.Left);
        msg.setDestination(this.leftNeighbour);
        this.Node.send(msg);

    }

    public void SendRight(Message msg) throws IOException {
        //msg.setSource(this.Node.returnID());
        msg.setDirection(Message.Direction.Right);
        msg.setDestination(this.rightNeighbour);
        this.Node.send(msg);
    }
}