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
    private static int msgId;

    private final static String SEND_LEFT = "1";
    private final static String SEND_RIGHT = "2";
    private final static String SEND = "3";
    private final static String DISPLAY_TOPOLOGY = "4";
    private final static String EXIT = "5";

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

        // Get physical exchange channel names (i.e. edges) between any node and its
        // physical neighbors
        List<Map<String, String>> exchangeMaps = getExchangeMaps(graph);
        // Get routing tables for nodes from any node to any other node
        List<Map<String, String>> routingTables = getRoutingTables(graph);
        // Get ring topology
        List<Integer> ring = tour(graph, nodeCount, 0);
        // Print overlay topology - not sure about this one
        printRing(ring);

        // Create underlay and overlay nodes
        Node[] nodes = new Node[nodeCount];
        VirtualNode[] vnodes = new VirtualNode[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            Node node = new Node(Integer.toString(ring.get(i)), exchangeMaps.get(ring.get(i)),
                    routingTables.get(ring.get(i)));
            nodes[ring.get(i)] = node;
            String left = Integer.toString(ring.get((i + nodeCount - 1) % nodeCount));
            String right = Integer.toString(ring.get((i + 1) % nodeCount));
            VirtualNode vnode = new VirtualNode(node, left, right);
            vnodes[ring.get(i)] = vnode;
        }

        // Interact with the user
        scanner = new Scanner(System.in);
        String action;
        boolean exiting = true;
        int srcId, destId;
        String message;
        Message msg;
        while (exiting) {
            System.out.println("\nOptions:\n" + SEND_LEFT + ": Send a message to left virtual neighbor\n" + SEND_RIGHT
                    + ": Send a message to right virtual neighbor\n" + SEND + ": Send a message on the physical level\n"
                    + DISPLAY_TOPOLOGY + ": Show the overlay ring\n" + EXIT + ": Exit");
            System.out.print("Choose an option: ");
            action = scanner.nextLine();
            switch (action) {
                case SEND_LEFT:
                    System.out.println("<source> <message>");
                    srcId = scanner.nextInt();
                    message = scanner.nextLine();
                    // Message cleaning
                    if (message.charAt(0) == ' ') {
                        message = message.substring(1);
                    }
                    if ((srcId >= nodeCount) || (srcId < 0)) {
                        System.out.println("The node does not exist in the topology");
                    } else {
                        vnodes[srcId].sendLeft(msgId++, message);
                    }
                    break;
                case SEND_RIGHT:
                    System.out.println("<source> <message>");
                    srcId = scanner.nextInt();
                    message = scanner.nextLine();
                    // Message cleaning
                    if (message.charAt(0) == ' ') {
                        message = message.substring(1);
                    }
                    if ((srcId >= nodeCount) || (srcId < 0)) {
                        System.out.println("The node does not exist in the topology");
                    } else {
                        vnodes[srcId].sendRight(msgId++, message);
                    }
                    break;
                case SEND:
                    System.out.println("<source> <destination> <message>");
                    srcId = scanner.nextInt();
                    destId = scanner.nextInt();
                    message = scanner.nextLine();
                    // Message cleaning
                    if (message.charAt(0) == ' ') {
                        message = message.substring(1);
                    }
                    if (destId == srcId) {
                        System.out.println("You can't send the message to the sender");
                    } else if ((srcId >= nodeCount) || (srcId < 0) || (destId >= nodeCount) || (destId < 0)) {
                        System.out.println("The node does not exist in the topology");
                    } else {
                        msg = new Message(msgId++, message);
                        msg.setSource(Integer.toString(srcId));
                        msg.setDestination(Integer.toString(destId));
                        nodes[srcId].send(msg);
                    }
                    break;
                case DISPLAY_TOPOLOGY:
                    printRing(ring);
                    break;
                case EXIT:
                    exiting = false;
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option");
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

    // Get a list of nodes' exchange channel maps
    private static List<Map<String, String>> getExchangeMaps(boolean[][] graph) {
        List<Map<String, String>> exchangeMaps = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            Map<String, String> exMap = new HashMap<>();
            exchangeMaps.add(exMap);
        }

        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                Map<String, String> exMap = exchangeMaps.get(i);
                if (i != j && graph[i][j]) {
                    String exchangeId = Integer.toString(Math.min(i, j)) + "-" + Integer.toString(Math.max(i, j));
                    exMap.put(Integer.toString(j), exchangeId);
                }
            }
        }
        return exchangeMaps;
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
        System.out.println("\nNetwork ring topology:");
        System.out.print("↳ ");
        for (int i = 0; i < ring.size(); i++) {
            System.out.print(ring.get(i));
            System.out.print(i == (ring.size() - 1) ? " ↰" : " - ");
        }
        System.out.println();
    }
}
