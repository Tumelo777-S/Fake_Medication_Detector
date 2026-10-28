package algorithms;
import datastructures.Queue;
import graph.Graph;
import graph.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// BFS algorithm - goes through the graph level by level using a queue
public class BFS {

    /**
     * does a BFS from the start node and returns all the nodes it visited in order
     *
     * @param graph the graph we are searching through
     * @param startId the id of the node we start from
     * @return a list of node ids in the order they were visited
     */
    public static List<Integer> traverse(Graph graph, int startId) {
        List<Integer> order = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        // put the starting node in the queue and mark it visited so we don't go back
        Queue<Integer> queue = new Queue<>();
        queue.enqueue(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            int current = queue.dequeue(); // take the next node out
            order.add(current);

            // check all neighbours, if not visited yet add them to the queue
            for (Node neighbour : graph.getNeighbours(current)) {
                if (!visited.contains(neighbour.getId())) {
                    visited.add(neighbour.getId());
                    queue.enqueue(neighbour.getId());
                }
            }
        }
        return order;
    }

    /**
     * counts how many separate connected components are in the graph
     * a component is just a group of nodes that can reach each other
     *
     * @param graph the graph to check
     * @return the number of separate components found
     */
    public static int countComponents(Graph graph) {
        Set<Integer> visited = new HashSet<>();
        int components = 0;

        for (Node node : graph.getAllNodes()) {
            // if we haven't visited this node it means its a new component
            if (!visited.contains(node.getId())) {
                components++;

                // do a BFS from this node to mark everything in this component
                Queue<Integer> queue = new Queue<>();
                queue.enqueue(node.getId());
                visited.add(node.getId());

                while (!queue.isEmpty()) {
                    int curr = queue.dequeue();
                    for (Node nb : graph.getNeighbours(curr)) {
                        if (!visited.contains(nb.getId())) {
                            visited.add(nb.getId());
                            queue.enqueue(nb.getId());
                        }
                    }
                }
            }
        }
        return components;
    }
}
