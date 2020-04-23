import java.io.IOException;

public class VirtualNode {
    private Node node;
    private String leftNeighbor, rightNeighbor;

    VirtualNode(Node Node, String leftNeighbor, String rightNeighbor) throws IOException {
        this.node = Node;
        this.leftNeighbor = leftNeighbor;
        this.rightNeighbor = rightNeighbor;
    }

    public void sendLeft(String text) throws IOException {
        send(text, leftNeighbor);
    }

    public void sendRight(String text) throws IOException {
        send(text, rightNeighbor);
    }

    private void send(String text, String dest) throws IOException {
        Message msg = new Message(text);
        msg.setSource(node.getId());
        msg.setDestination(dest);
        node.send(msg);
    }
}