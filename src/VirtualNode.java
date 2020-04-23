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

    public void sendLeft(Message msg) throws IOException {
        msg.setSource(node.getId());
        msg.setDestination(leftNeighbor);
        node.send(msg);

    }

    public void sendRight(Message msg) throws IOException {
        msg.setSource(node.getId());
        msg.setDestination(rightNeighbor);
        node.send(msg);
    }
}