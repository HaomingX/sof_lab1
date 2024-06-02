import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CustomGraph {

    private Map<String, Map<String, Integer>> adjacencyMap;

    public CustomGraph() {
        this.adjacencyMap = new HashMap<>();
    }

    public void addVertex(String vertex) {
        adjacencyMap.putIfAbsent(vertex, new HashMap<>());
    }

    public void addEdge(String from, String to) {
        Map<String, Integer> neighbors = adjacencyMap.get(from);
        neighbors.put(to, neighbors.getOrDefault(to, 0) + 1);
    }

    public void printGraph() {
        for (String vertex : adjacencyMap.keySet()) {
            System.out.print(vertex + " -> ");
            for (Map.Entry<String, Integer> entry : adjacencyMap.get(vertex).entrySet()) {
                System.out.print(entry.getKey() + "(" + entry.getValue() + ") ");
            }
            System.out.println();
        }
    }

    String queryBridgeWords(String word1, String word2) {
        if (!adjacencyMap.containsKey(word1) || !adjacencyMap.containsKey(word2)) {
            return "No bridge words from word1 to word2!";
        }

        // word1->string, string->word2 其中string为bridge word
        StringBuilder bridgeWords = new StringBuilder();
        for (String bridgeWord : adjacencyMap.get(word1).keySet()) {
            if (adjacencyMap.get(bridgeWord).containsKey(word2)) {
                bridgeWords.append(bridgeWord).append(" ");
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from word1 to word2!";
        } else {
            return bridgeWords.toString();
        }
    }

    public String generateNewText(String inputText) {
        String[] words = inputText.split("\\s+");
        StringBuilder newText = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            newText.append(word1).append(" ");

            String bridgeWords = queryBridgeWords(word1, word2);
            if (!bridgeWords.startsWith("No bridge words")) {
                String[] bridgeWordsArray = bridgeWords.split(" ");
                // import random
                Random random = new Random();
                int randomIndex = random.nextInt(bridgeWordsArray.length);
                newText.append(bridgeWordsArray[randomIndex]).append(" ");
            }
        }

        // 添加最后一个单词
        newText.append(words[words.length - 1]);

        return newText.toString();
    }

    public String calcShortestPath(String word1, String word2) {
        if (!adjacencyMap.containsKey(word1) || !adjacencyMap.containsKey(word2)) {
            return "No path from " + word1 + " to " + word2;
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousVertices = new HashMap<>();

        // 初始化距离和前驱节点
        for (String vertex : adjacencyMap.keySet()) {
            distances.put(vertex, Integer.MAX_VALUE); // 初始化为无穷大
            previousVertices.put(vertex, null); // 前驱节点初始化为null
        }
        distances.put(word1, 0); // 起始节点距离为0

        PriorityQueue<String> queue = new PriorityQueue<>((a, b) -> distances.get(a) - distances.get(b)); // 优先队列，按距离降序
        queue.offer(word1); // 将起始节点加入队列

        while (!queue.isEmpty()) {
            String currentVertex = queue.poll(); // 弹出距离最小的节点

            if (currentVertex.equals(word2)) {
                // 已找到最短路径,构建路径字符串
                List<String> path = new ArrayList<>();
                String vertex = word2;
                while (vertex != null) {
                    path.add(0, vertex);
                    vertex = previousVertices.get(vertex);
                }
                return String.join(" -> ", path);
            }

            Map<String, Integer> neighbors = adjacencyMap.get(currentVertex);
            for (String neighbor : neighbors.keySet()) {
                int altDistance = distances.get(currentVertex) + 1;
                if (altDistance < distances.get(neighbor)) {
                    distances.put(neighbor, altDistance);
                    previousVertices.put(neighbor, currentVertex);
                    queue.offer(neighbor);
                }
            }
        }

        return "No path from " + word1 + " to " + word2;
    }

    public void randomWalk() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 'stop' to stop the random walk.");

        Random random = new Random();
        List<String> path = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>(); // 记录已访问的边

        // 选择一个随机起点
        String start = adjacencyMap.keySet().stream().skip(random.nextInt(adjacencyMap.size())).findFirst().orElse(null);
        if (start == null) {
            System.out.println("The graph is empty.");
            return;
        }
        path.add(start);

        String current = start;
        while (true) {
            Map<String, Integer> neighbors = adjacencyMap.get(current);
            if (neighbors == null || neighbors.isEmpty()) {
                System.out.println("No neighbors found for: " + current);
                break;
            }

            List<String> neighborList = new ArrayList<>(neighbors.keySet());
            String next = neighborList.get(random.nextInt(neighborList.size()));
            String edge = current + "->" + next;

            if (visitedEdges.contains(edge)) {
                System.out.println("Repeated edge found: " + edge);
                break;
            }
            visitedEdges.add(edge);
            path.add(next);

            System.out.println("Current path: " + String.join(" -> ", path));
            System.out.print("Enter 'stop' to stop the random walk, or press Enter to continue: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("stop")) {
                break;
            }

            current = next;
        }

        System.out.println("Random walk path: " + String.join(" -> ", path));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk.txt"))) {
            writer.write("Random walk path: " + String.join(" -> ", path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //功能需求2：展现有向图
    public void showDirectedGraph() {
        System.setProperty("org.graphstream.ui", "swing");
        Graph streamGraph = new SingleGraph("Text Graph");

        // Add nodes and edges
        for (String vertex : adjacencyMap.keySet()) {
            if (streamGraph.getNode(vertex) == null) {
                streamGraph.addNode(vertex).setAttribute("ui.label", vertex);
            }
            for (Map.Entry<String, Integer> entry : adjacencyMap.get(vertex).entrySet()) {
                String neighbor = entry.getKey();
                int weight = entry.getValue();
                if (streamGraph.getNode(neighbor) == null) {
                    streamGraph.addNode(neighbor).setAttribute("ui.label", neighbor);
                }
                String edgeId = vertex + "->" + neighbor;
                if (streamGraph.getEdge(edgeId) == null) {
                    Edge edge = streamGraph.addEdge(edgeId, vertex, neighbor, true);
                    edge.setAttribute("weight", weight);
                    edge.setAttribute("ui.label", weight);
                }
            }
        }

        // Enhanced styling
        String stylesheet =
                "node {" +
                        "   shape: circle;" +
                        "   size: 67px;" +
                        "   fill-color: #1f78b4;" +
                        "   text-size: 15px;" +
                        "   text-color: white;" +
                        "   text-style: bold;" +
                        "}" +
                        "edge {" +
                        "   shape: line;" +
                        "   size: 2px;" +
                        "   fill-color: #33a02c;" +
                        "   arrow-size: 10px, 5px;" +
                        "   text-size: 15px;" +
                        "   text-background-mode: rounded-box;" +
                        "   text-background-color: white;" +
                        "   text-padding: 3px;" +
                        "   text-offset: 5px, 0px;" +
                        "}";

        streamGraph.setAttribute("ui.stylesheet", stylesheet);
        streamGraph.setAttribute("ui.quality");
        streamGraph.setAttribute("ui.antialias");

        // Display the graph
        streamGraph.display();
    }

    // 重载方法，展现有向图并高亮最短路径
    public void showDirectedGraph(String word1, String word2) {
        System.setProperty("org.graphstream.ui", "swing");
        Graph streamGraph = new SingleGraph("Text Graph");

        // Add nodes and edges
        for (String vertex : adjacencyMap.keySet()) {
            if (streamGraph.getNode(vertex) == null) {
                streamGraph.addNode(vertex).setAttribute("ui.label", vertex);
            }
            for (Map.Entry<String, Integer> entry : adjacencyMap.get(vertex).entrySet()) {
                String neighbor = entry.getKey();
                int weight = entry.getValue();
                if (streamGraph.getNode(neighbor) == null) {
                    streamGraph.addNode(neighbor).setAttribute("ui.label", neighbor);
                }
                String edgeId = vertex + "->" + neighbor;
                if (streamGraph.getEdge(edgeId) == null) {
                    Edge edge = streamGraph.addEdge(edgeId, vertex, neighbor, true);
                    edge.setAttribute("weight", weight);
                    edge.setAttribute("ui.label", weight);
                }
            }
        }

        // Enhanced styling
        String stylesheet =
                "node {" +
                        "   shape: circle;" +
                        "   size: 67px;" +
                        "   fill-color: #1f78b4;" +
                        "   text-size: 15px;" +
                        "   text-color: white;" +
                        "   text-style: bold;" +
                        "}" +
                        "edge {" +
                        "   shape: line;" +
                        "   size: 2px;" +
                        "   fill-color: #33a02c;" +
                        "   arrow-size: 10px, 5px;" +
                        "   text-size: 15px;" +
                        "   text-background-mode: rounded-box;" +
                        "   text-background-color: white;" +
                        "   text-padding: 3px;" +
                        "   text-offset: 5px, 0px;" +
                        "}" +
                        "edge.highlighted {" +
                        "   fill-color: red;" +
                        "   size: 3px;" +
                        "}";

        streamGraph.setAttribute("ui.stylesheet", stylesheet);
        streamGraph.setAttribute("ui.quality");
        streamGraph.setAttribute("ui.antialias");

        // Highlight the shortest path
        String shortestPath = calcShortestPath(word1, word2);
        if (!shortestPath.startsWith("No path")) {
            String[] pathNodes = shortestPath.split(" -> ");
            for (int i = 0; i < pathNodes.length - 1; i++) {
                String edgeId = pathNodes[i] + "->" + pathNodes[i + 1];
                Edge edge = streamGraph.getEdge(edgeId);
                edge.setAttribute("ui.class", "highlighted");
            }

            int pathLength = 0;
            for (int i = 0; i < pathNodes.length - 1; i++) {
                String from = pathNodes[i];
                String to = pathNodes[i + 1];
                pathLength += adjacencyMap.get(from).get(to);
            }
            System.out.println("Shortest path length: " + pathLength);
        }

        // Display the graph
        streamGraph.display();
    }

    private static String[] readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Clean the content by removing non-alphabetic characters and converting to lowercase
        String cleanedContent = content.toString().replaceAll("[^a-zA-Z ]", " ").toLowerCase();

        // Split the cleaned content into words and return as an array
        return cleanedContent.split("\\s+");
    }

    public static void main(String[] args) {
        CustomGraph customGraph = new CustomGraph();
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        String file = "Text/1.txt"; // Update this path according to your file location
        String filePath = currentDir.resolve(file).toString();
        String[] words = readFile(filePath);

        if (words == null) {
            System.out.println("Error reading the file.");
            return;
        }

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            if (!word1.isEmpty() && !word2.isEmpty()) {
                customGraph.addVertex(word1);
                customGraph.addVertex(word2);
                customGraph.addEdge(word1, word2);
            }
        }

        customGraph.printGraph();
        // customGraph.showDirectedGraph();
        System.out.println(customGraph.queryBridgeWords("to", "out"));
        System.out.println(customGraph.generateNewText("Seek to explore new and exciting synergies"));
        System.out.println(customGraph.calcShortestPath("to", "and"));
        customGraph.showDirectedGraph("to", "and");
        // customGraph.randomWalk();
    }
}
