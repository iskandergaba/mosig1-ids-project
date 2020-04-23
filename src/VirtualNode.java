import java.io.IOException;

public class VirtualNode {
    private Node node;
    private String leftNeighbor, rightNeighbor;

    VirtualNode(Node Node, String leftNeighbor, String rightNeighbor) throws IOException {
        this.node = Node;
        this.leftNeighbor = leftNeighbor;
        this.rightNeighbor = rightNeighbor;
    }

    public void setLeftNeighbor(String leftNeighbor) {
		this.leftNeighbor = leftNeighbor;
	}

	public String getLeftNeighbor() {
		return leftNeighbor;
	}

	public void setRightNeighbor(String rightNeighbor) {
		this.rightNeighbor = rightNeighbor;
	}

    public String getRightNeighbor() {
		return rightNeighbor;
	}

    public void SendLeft(Message msg) throws IOException {
        msg.setSource(node.getId());
        msg.setDirection(Message.Direction.Left);
        msg.setDestination(leftNeighbor);
        node.send(msg);

    }

    public void SendRight(Message msg) throws IOException {
        msg.setSource(node.getId());
        msg.setDirection(Message.Direction.Right);
        msg.setDestination(rightNeighbor);
        node.send(msg);
    }
}