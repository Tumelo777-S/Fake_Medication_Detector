package algorithms;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// kruskal's algorithm to find the minimum spanning tree of a graph
// uses union-find to avoid creating cycles
public class MST {


    /**
     * builds the minimum spanning tree using kruskal's algorithm
     * sorts all edges by weight then adds them one by one as long as they don't create a cycle
     *
     * @param graph the graph to compute the MST for
     * @return a list of edges that make up the MST
     */
    public static List<Edge> computeMST(Graph graph) {
        List<Edge> allEdges = graph.getAllEdges();
        List<Node> allNodes = graph.getAllNodes();

        // sort edges from smallest weight to largest
        allEdges.sort(Comparator.comparingDouble(Edge::getWeight));

        // find the highest node id so we know how big to make the arrays
        int maxId = 0;
        for (Node n : allNodes) {
            if (n.getId() > maxId) {
                maxId = n.getId();
            }
        }

        // union-find arrays - each node starts as its own parent
        int[] parent = new int[maxId + 1];
        int[] rank = new int[maxId + 1];
        for (Node n : allNodes) {
            parent[n.getId()] = n.getId();
            rank[n.getId()] = 0;
        }

        List<Edge> mstEdges = new ArrayList<>();

        for (Edge edge : allEdges) {
            int u = edge.getSource().getId();
            int v = edge.getDestination().getId();
            int rootU = find(parent, u);
            int rootV = find(parent, v);

            // only add the edge if it connects two different components (no cycle)
            if (rootU != rootV) {
                mstEdges.add(edge);
                union(parent, rank, rootU, rootV);

                if (mstEdges.size() == allNodes.size() - 1) {
                    break; // mst is complete once we have n-1 edges
                }
            }
        }

        return mstEdges;
    }


    /**
     * adds up all the edge weights in the MST and returns the total
     *
     * @param graph the graph to compute the MST weight for
     * @return total weight of the MST as a double
     */
    public static double computeMSTWeight(Graph graph) {
        List<Edge> mst = computeMST(graph);
        double total = 0;
        for (Edge e : mst) {
            total += e.getWeight();
        }
        return total;
    }


    // finds the root of the component that node x belongs to
    // also does path compression to speed up future lookups
    private static int find(int[] parent, int x) {
        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }
        return parent[x];
    }

    // merges two components together using rank to keep the tree balanced
    private static void union(int[] parent, int[] rank, int x, int y) {
        if (rank[x] < rank[y]) {
            parent[x] = y;
        } else if (rank[x] > rank[y]) {
            parent[y] = x;
        } else {
            parent[y] = x;
            rank[x]++;
        }
    }
}
