package algorithms;

import graph.Graph;
import graph.Node;
import imageprocessing.ColorHistogram;

import java.util.List;


// compares two image graphs and gives back a score from 0.0 to 1.0
// higher score means the images are more similar
public class SimilarityDetection {

    // 40% of the score comes from the overall colour and 60% from how well nodes match
    private static final double GLOBAL_WEIGHT = 0.4;


    /**
     * compares two graphs and returns how similar they are as a value between 0 and 1
     * it combines a global colour comparison with a node-by-node matching score
     *
     * @param graphA the first graph (usually the uploaded image)
     * @param graphB the second graph (a record from the database)
     * @return similarity score between 0.0 and 1.0
     */
    public static double computeSimilarity(Graph graphA, Graph graphB) {
        double[] avgA = computeAverageFeatureVector(graphA);
        double[] avgB = computeAverageFeatureVector(graphB);

        double globalScore = cosineSimilarity(avgA, avgB);
        double nodeScore = nodeMatchScore(graphA, graphB);

        double combined = GLOBAL_WEIGHT * globalScore + (1.0 - GLOBAL_WEIGHT) * nodeScore;
        return Math.max(0.0, Math.min(1.0, combined));
    }


    /**
     * same as the other computeSimilarity but lets you pass in pre-computed feature vectors
     * useful if you already extracted them and don't want to do it again
     *
     * @param graphA first graph
     * @param graphB second graph
     * @param vecA pre-computed feature vector for graphA
     * @param vecB pre-computed feature vector for graphB
     * @return similarity score between 0.0 and 1.0
     */
    public static double computeSimilarity(Graph graphA, Graph graphB,
                                           double[] vecA, double[] vecB) {
        double globalScore = cosineSimilarity(vecA, vecB);
        double nodeScore = nodeMatchScore(graphA, graphB);
        double combined = GLOBAL_WEIGHT * globalScore + (1.0 - GLOBAL_WEIGHT) * nodeScore;
        return Math.max(0.0, Math.min(1.0, combined));
    }


    // averages all the node feature vectors in a graph into one single vector
    private static double[] computeAverageFeatureVector(Graph graph) {
        List<Node> nodes = graph.getAllNodes();
        if (nodes.isEmpty()) {
            return new double[ColorHistogram.HISTOGRAM_LENGTH];
        }

        int len = nodes.get(0).getFeatureVector().length; // should be 24 (8 bins x 3 channels)
        double[] avg = new double[len];

        for (Node node : nodes) {
            double[] vec = node.getFeatureVector();
            for (int i = 0; i < Math.min(len, vec.length); i++) {
                avg[i] += vec[i];
            }
        }

        // divide by number of nodes to get the average
        double invN = 1.0 / nodes.size();
        for (int i = 0; i < len; i++) {
            avg[i] *= invN;
        }

        return avg;
    }


    // for each node in graphA find the most similar node in graphB then average all those scores
    private static double nodeMatchScore(Graph graphA, Graph graphB) {
        List<Node> nodesA = graphA.getAllNodes();
        List<Node> nodesB = graphB.getAllNodes();

        if (nodesA.isEmpty() || nodesB.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        for (Node a : nodesA) {
            double bestSim = -1.0;
            double[] vecA = a.getFeatureVector();

            for (Node b : nodesB) {
                double sim = cosineSimilarity(vecA, b.getFeatureVector());
                if (sim > bestSim) {
                    bestSim = sim;
                }
            }

            if (bestSim >= 0) {
                totalScore += bestSim;
            }
        }

        // normalize by dividing by number of nodes in graphA
        return totalScore / nodesA.size();
    }


    /**
     * calculates cosine similarity between two vectors
     * a value of 1.0 means they point in exactly the same direction (identical)
     * a value of 0.0 means they are completely different
     *
     * @param a first vector
     * @param b second vector
     * @return cosine similarity value between -1 and 1
     */
    public static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0, magA = 0.0, magB = 0.0;
        int len = Math.min(a.length, b.length);

        for (int i = 0; i < len; i++) {
            dot  += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }

        if (magA == 0.0 || magB == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }
}