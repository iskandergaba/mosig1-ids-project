import java.util.*;
import java.io.IOException;

/** Here we get the ring topology from the input graph that is connected.
 * We use repetitive nearest neighbour algorithm (same as nearest neigbhour,
 * but checks the tours from every node and chooses the one with min length) <- TODO
*/
public class RingNode {

    private Stack<Integer> stack;
    public LinkedList<Integer> ring;

    public RingNode() {
        stack = new Stack<Integer>();
        ring = new LinkedList<Integer>();
    }

    public void tour(boolean graph[][], int nnodes, int start)

    {
        int[] visited = new int[nnodes + 1];
        visited[start] = 1;
        stack.push(start);
        int element, dst = 0, i;
        boolean minFlag = false;
        ring.add(start);
        int min = Integer.MAX_VALUE;
        while (!stack.isEmpty()) {
            element = stack.peek();

            i = 0;
            while (i < nnodes) {
                if ((graph[element][i] == true) && visited[i] == 0) {
                    dst = i;
                    minFlag = true;
                    break;
                }
                i++;
            }
            if (minFlag) {
                visited[dst] = 1;
                stack.push(dst);
                minFlag = false;
                ring.add(dst);
                continue;
            }
            stack.pop();
        }

    }

    public void printRing() {
        Iterator<Integer> iterator = ring.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + "\t");
        }
    }

    public void sendLeft(Message msg, List<Node> nodes) throws IOException { // Source is virtualid, index in the ring
        int index = msg.getSource();
        if (index >= ring.size()) {
            System.out.println("unknown node");
            return;
            // throw IOexception??
        }
        int src = ring.get(index);
        msg.setSource(src);
        msg.setDirection(Message.Direction.Left);
        msg.setDestination(Integer.toString(ring.get((index + ring.size() - 1) % ring.size())));
        nodes.get(src).send(msg);
    }

    public void sendRight(Message msg, List<Node> nodes) throws IOException {
        int index = msg.getSource();
        if (index >= ring.size()) {
            System.out.println("unknown node");
            return;
            // throw IOexception??
        }
        int src = ring.get(index);
        msg.setSource(src);
        msg.setDirection(Message.Direction.Right);
        msg.setDestination(Integer.toString(ring.get((index + 1) % ring.size())));
        nodes.get(src).send(msg);
    }
}
