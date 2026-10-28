package algorithms;

import datastructures.Stack;
import datastructures.Heap;
import graph.Edge;
import graph.Graph;
import graph.Node;

import java.util.*;


// dijkstra's shortest path - finds cheapest route between two nodes using a min heap
public class Dijkstra {

    public static final double INFINITY = Double.MAX_VALUE; // used as the starting distance for all nodes


    /**
     * finds the shortest path from source to target using dijkstra's algorithm
     * uses a min heap to always process the closest unvisited node first
     *
     * @param graph the graph to search through
     * @param sourceId the node we start from
     * @param targetId the node we want to reach
     * @return a ShortestPathResult with the path and total cost
     */
    public static ShortestPathResult findShortestPath(Graph graph, int sourceId, int targetId) {
        Map<Integer, Double> dist = new HashMap<>();     // shortest known distance to each node
        Map<Integer, Integer> previous = new HashMap<>(); // which node we came from to get here

        for (Node n : graph.getAllNodes()) {
            dist.put(n.getId(), INFINITY); // start everything at infinity
            previous.put(n.getId(), -1);   // no previous node yet
        }
        dist.put(sourceId, 0.0); // distance from source to itself is 0

        Heap pq = new Heap(graph.nodeCount()); // min heap to pick next closest node
        pq.insert(0.0, sourceId);

        Set<Integer> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            double[] current = pq.extractMin();
            double currDist = current[0]; // pull out the distance and id
            int currId = (int) current[1];

            if (visited.contains(currId)) continue;
            visited.add(currId);

            if (currId == targetId) break; // found the target, stop early

            List<Edge> edges = graph.getEdgesOfNode(currId);
            for (Edge edge : edges) {
                int neighbourId = edge.getDestination().getId();
                if (!dist.containsKey(neighbourId)) continue;

                double newDist = currDist + edge.getWeight();
                if (newDist < dist.get(neighbourId)) {
                    // found a shorter way to reach this neighbour
                    dist.put(neighbourId, newDist);
                    previous.put(neighbourId, currId);
                    pq.insert(newDist, neighbourId);
                }
            }
        }

        // reconstruct the path by tracing back through previous nodes
        double totalCost = dist.getOrDefault(targetId, INFINITY);
        List<Integer> path = new ArrayList<>();

        if (totalCost < INFINITY) {
            Stack<Integer> pathStack = new Stack<>();
            int step = targetId;
            while (step != -1) {
                pathStack.push(step);
                step = previous.getOrDefault(step, -1);
            }
            while (!pathStack.isEmpty()) {
                path.add(pathStack.pop());
            }
        }

        return new ShortestPathResult(path, totalCost, graph);
    }


    // holds the result of a shortest path search
    public static class ShortestPathResult {
        public final List<Integer> path;
        public final double totalCost;
        private final Graph graph;

        /**
         * creates the result object from a completed dijkstra run
         *
         * @param path the list of node ids forming the shortest path
         * @param totalCost the total distance of the path
         * @param graph the graph used for the search (needed to look up node labels)
         */
        public ShortestPathResult(List<Integer> path, double totalCost, Graph graph) {
            this.path = path;
            this.totalCost = totalCost;
            this.graph = graph;
        }

        /**
         * returns true if a valid path was found between source and target
         *
         * @return true if path is non-empty and cost is not infinity
         */
        public boolean pathFound() { return !path.isEmpty() && totalCost < INFINITY; }

        /**
         * gives back the path as a readable string with node labels and total cost
         *
         * @return a string like "nodeA -> nodeB -> nodeC  (cost: 0.1234)"
         */
        @Override
        public String toString() {
            if (!pathFound()) return "No path found.";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                Node n = graph.getNode(path.get(i));
                sb.append(n != null ? n.getLabel() : "?" + path.get(i));
                if (i < path.size() - 1) sb.append(" -> ");
            }
            sb.append(String.format("  (cost: %.4f)", totalCost));
            return sb.toString();
        }

        /**
         * same as toString but also includes node ids which is useful for debugging
         *
         * @return readable string with ids included
         */
        public String toReadableString() {
            return toString();
        }
    }
}