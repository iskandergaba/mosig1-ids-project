import java.io.IOException;

public class VirtualNode {
    private Node node;
    private String leftNeighbor, rightNeighbor;

    VirtualNode(Node Node, String leftNeighbor, String rightNeighbor) throws IOException {
        this.node = Node;
        this.leftNeighbor = leftNeighbor;
        this.rightNeighbor = rightNeighbor;
    }

    public void sendLeft(int id, String text) throws IOException {
        send(id, text, leftNeighbor);
    }

    public void sendRight(int id, String text) throws IOException {
        send(id, text, rightNeighbor);
    }

    private void send(int id, String text, String dest) throws IOException {
        Message msg = new Message(id, text);
        msg.setSource(node.getId());
        msg.setDestination(dest);
        node.send(msg);
    }
}