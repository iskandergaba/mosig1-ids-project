import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.Stack;

public class Main {

    private static int nodeCount;

    public static void main(String[] args) throws IOException, TimeoutException {

        if (args.length < 1) {
            System.out.println("Missing argument: topology description file");
            System.exit(-1);
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            throw new IllegalArgumentException("Invalid file argument: File not found.");
        }

        Scanner scanner = new Scanner(file);
        nodeCount = scanner.nextInt();
        boolean[][] graph = new boolean[nodeCount][nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            scanner.nextLine();
            for (int j = 0; j < nodeCount; j++) {
                graph[i][j] = scanner.nextInt() == 0 ? false : true;
            }
        }
        scanner.close();

        // Make sure the user provided a connected graph input
        if (!isConnected(graph, new boolean[nodeCount], 0)) {
            throw new IllegalArgumentException("Invalid adjacency matrix: The graph is not connected.");
        }

        // Get routing tables for nodes from any node to any other node
        List<Map<String, String>> routingTables = getRoutingTables(graph);
        // Get ring topology
        List<Integer> ring = tour(graph, nodeCount, 0);
        // Print overlay topology - not sure about this one
        System.out.println("You are working with network:");
        printRing(ring);

        // Create underlay and overlay nodes
        Node[] nodes = new Node[nodeCount];
        VirtualNode[] vnodes = new VirtualNode[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            Node node = new Node(Integer.toString(i), routingTables.get(i));
            nodes[i] = node;
            String left = Integer.toString(ring.get((i + nodeCount - 1) % nodeCount));
            String right = Integer.toString(ring.get((i + 1) % nodeCount));
            VirtualNode vnode = new VirtualNode(node, left, right);
            vnodes[ring.get(i)] = vnode;
        }

        // Interact with the user
        scanner = new Scanner(System.in);
        String action;
        boolean exiting = true;
        int virtualID, physicalID, destID;
        String message;
        int msgCounter = 0;
        Message msg;
        while (exiting) {
            System.out.println(
                    "\nChoose action: \n 1. Send message on physical level \n 2. SendRight on virtual level \n 3. SendLeft on virtual level \n 4. Exit");
            action = scanner.nextLine();
            switch (action) {
                case "1":
                    System.out.println("<sender> <destination @> <message>");
                    physicalID = scanner.nextInt();
                    destID = scanner.nextInt();
                    message = scanner.nextLine();
                    if (destID == physicalID) {
                        System.out.println("You can't send the message to the sender");
                    } else if ((physicalID >= nodeCount) || (physicalID < 0) || (destID >= nodeCount) || (destID < 0)) {
                        System.out.println("The node does not exist in the topology");
                    } else {
                        msgCounter++;
                        msg = new Message(msgCounter, message);
                        msg.setSource(Integer.toString(physicalID));
                        msg.setDestination(Integer.toString(destID));
                        msg.setDirection(Message.Direction.Direct);
                        nodes[physicalID].send(msg);
                    }
                    break;
                case "2":
                    System.out.println("<sender> <message>");
                    virtualID = scanner.nextInt();
                    message = scanner.nextLine();
                    if ((virtualID >= nodeCount) || (virtualID < 0)) {
                        System.out.println("The node does not exist in the topology");
                    } else {
                        msgCounter++;
                        msg = new Message(msgCounter, message);
                        vnodes[virtualID].SendRight(msg);
                    }
                    break;
                case "3":
                    System.out.println("<sender> <message>");
                    virtualID = scanner.nextInt();
                    message = scanner.nextLine();
                    if ((virtualID >= nodeCount) || (virtualID < 0)) {
                        System.out.println("The node does not exist in the topology");
                    } else {
                        msgCounter++;
                        msg = new Message(msgCounter, message);
                        vnodes[virtualID].SendLeft(msg);
                    }
                    break;
                case "4":
                    exiting = false;
                    scanner.close();
                    System.exit(0);
                    break;
            }
        }
    }

    // Check if the topology is a connected graph (Depth-First Traversal)
    private static boolean isConnected(boolean[][] graph, boolean[] marked, int node) {
        marked[node] = true;
        for (int i = 0; i < nodeCount; i++) {
            if (node != i && graph[node][i] && !marked[i]) {
                isConnected(graph, marked, i);
            }
        }
        for (boolean n : marked) {
            if (!n) {
                return false;
            }
        }
        return true;
    }

    // Get a list of nodes' routing tables
    private static List<Map<String, String>> getRoutingTables(boolean[][] graph) {
        List<Map<String, String>> hopList = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            Map<String, String> hops = new HashMap<>();
            hopList.add(hops);
        }

        for (int i = 0; i < nodeCount; i++) {
            List<List<Integer>> paths = getShortestPaths(graph, i);
            for (int j = 0; j < nodeCount; j++) {
                List<Integer> path = paths.get(j);
                Map<String, String> hops = hopList.get(i);
                if (i != j) {
                    hops.put(Integer.toString(j), Integer.toString(path.get(1)));
                }
            }
        }
        return hopList;
    }

    // Get the shortest paths between a node and all other nodes (Breadth-First
    // Traversal)
    private static List<List<Integer>> getShortestPaths(boolean[][] graph, int node) {
        int[] distTo = new int[nodeCount];
        int[] edgeTo = new int[nodeCount];
        boolean[] marked = new boolean[nodeCount];
        Queue<Integer> q = new LinkedList<>();

        for (int i = 0; i < nodeCount; i++) {
            distTo[i] = Integer.MAX_VALUE;
        }
        distTo[node] = 0;
        marked[node] = true;
        q.add(node);

        while (!q.isEmpty()) {
            int v = q.poll();
            for (int i = 0; i < nodeCount; i++) {
                if (v != i && graph[v][i] && !marked[i]) {
                    edgeTo[i] = v;
                    distTo[i] = distTo[v] + 1;
                    marked[i] = true;
                    q.add(i);
                }
            }
        }

        List<List<Integer>> paths = new ArrayList<>();
        for (int t = 0; t < nodeCount; t++) {
            List<Integer> path = new ArrayList<>();
            for (int x = t; distTo[x] != 0; x = edgeTo[x]) {
                path.add(x);
            }
            path.add(node);
            Collections.reverse(path);
            paths.add(path);
        }
        return paths;
    }

    // Construct the overlay ring topology (Nearest-Neighbor Algorithm)
    private static List<Integer> tour(boolean graph[][], int nnodes, int start) {
        Stack<Integer> stack = new Stack<Integer>();
        List<Integer> ring = new ArrayList<Integer>();
        int[] visited = new int[nnodes + 1];
        visited[start] = 1;
        stack.push(start);
        int element, dst = 0, i;
        boolean minFlag = false;
        ring.add(start);
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
        return ring;
    }

    // Print the ring topology
    public static void printRing(List<Integer> ring) {
        for (int i = 0; i < ring.size(); i++)
            System.out.print(ring.get(i) + "\t");

    }
}
