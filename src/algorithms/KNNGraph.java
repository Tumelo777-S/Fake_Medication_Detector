package algorithms;

import graph.Graph;
import graph.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// builds a kNN graph by connecting each node to its k closest neighbours
// we measure closeness using euclidean distance on the feature vectors
public class KNNGraph {


    /**
     * goes through every node and connects it to its k nearest neighbours
     * uses euclidean distance between feature vectors to measure how close two nodes are
     *
     * @param graph the graph whose nodes we need to connect
     * @param k how many neighbours each node should be connected to
     */
    public static void connectKNN(Graph graph, int k) {
        List<Node> allNodes = graph.getAllNodes();
        int n = allNodes.size();

        for (int i = 0; i < n; i++) {
            Node u = allNodes.get(i);
            double[] vecU = u.getFeatureVector();

            // calculate the distance from this node to every other node
            List<double[]> distances = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                Node v = allNodes.get(j);
                double dist = euclideanDistance(vecU, v.getFeatureVector());
                distances.add(new double[]{dist, v.getId()});
            }

            // sort so the closest nodes come first
            distances.sort(DistanceComp);

            // connect to only the top k closest ones
            int connections = Math.min(k, distances.size());
            for (int c = 0; c < connections; c++) {

                int neighbourId = (int) distances.get(c)[1];
                double weight = distances.get(c)[0];
                graph.addEdge(u.getId(), neighbourId, weight);
                
            }
        }
    }


    /**
     * calculates the euclidean distance between two feature vectors
     *
     * @param a first feature vector
     * @param b second feature vector
     * @return the distance between them as a double
     */
    private static double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }


    // comparator to sort distance arrays by the distance value (index 0)
    private static final Comparator<double[]> DistanceComp = new Comparator<double[]>() {
        @Override
        public int compare(double[] a, double[] b) {
            return Double.compare(a[0], b[0]);
        }
    };

}