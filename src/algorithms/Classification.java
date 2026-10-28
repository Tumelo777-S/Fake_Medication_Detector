package algorithms;

import graph.Graph;
import model.Database;
import model.MedicineRecord;

import java.util.ArrayList;
import java.util.List;


// uses kNN to decide if an unknown medicine image is real or fake
// compares it against everything in the database and takes a majority vote
public class Classification {

    private static final int K = 5;


    // holds the result after classifying an image
    public static class ClassificationResult {
        public final String label;
        public final double confidence;
        public final double bestSimilarity;
        public final String bestMatchName;
        public final List<String> topKDetails;

        /**
         * creates a classification result with all the verdict details
         *
         * @param label either "AUTHENTIC" or "FAKE"
         * @param confidence fraction of the k neighbours that agreed (0.0 to 1.0)
         * @param bestSimilarity similarity score of the closest match
         * @param bestMatchName filename of the closest matching record
         * @param topKDetails list of readable summaries for the top k matches
         */
        public ClassificationResult(String label, double confidence,
                                    double bestSimilarity, String bestMatchName,
                                    List<String> topKDetails) {
            this.label = label;
            this.confidence = confidence;
            this.bestSimilarity = bestSimilarity;
            this.bestMatchName = bestMatchName;
            this.topKDetails = topKDetails;
        }
    }


    /**
     * takes an unknown image graph and classifies it as AUTHENTIC or FAKE
     * it does this by comparing the image to every record in the database
     * then the top k most similar ones vote on what it is
     *
     * @param unknownGraph the graph built from the uploaded image
     * @param database the database with all the known medicine records
     * @return a ClassificationResult with the label, confidence and top matches
     */
    public static ClassificationResult classify(Graph unknownGraph, Database database) {
        List<MedicineRecord> allRecords = database.getAllRecords();

        if (allRecords.isEmpty()) {
            return new ClassificationResult("UNKNOWN", 0.0, 0.0, "No database loaded", new ArrayList<>());
        }

        // compute similarity to every record in the database
        List<double[]> scores = new ArrayList<>();
        for (int i = 0; i < allRecords.size(); i++) {
            double sim = SimilarityDetection.computeSimilarity(unknownGraph, allRecords.get(i).getGraph());
            // store the similarity score and the index together so we can sort later
            scores.add(new double[]{sim, i});
        }

        // sort from highest similarity to lowest
        scores.sort((a, b) -> Double.compare(b[0], a[0]));

        int effectiveK = Math.min(K, scores.size());
        int realVotes = 0, fakeVotes = 0;

        // collect the top k results and count the votes
        List<String> topKDetails = new ArrayList<>();

        double bestSim = scores.get(0)[0];
        String bestName = allRecords.get((int) scores.get(0)[1]).getName();

        for (int i = 0; i < effectiveK; i++) {
            double sim = scores.get(i)[0];
            int idx = (int) scores.get(i)[1];
            MedicineRecord rec = allRecords.get(idx);

            // vote based on whether the similar record is real or fake
            if (rec.isReal()) {
                realVotes++;
            } else {
                fakeVotes++;
            }

            // save a readable summary of this match
            topKDetails.add(String.format("#%d  %-25s  [%s]  sim=%.4f",
                i + 1, rec.getName(), rec.isReal() ? "REAL" : "FAKE", sim));
        }

        // whoever got more votes wins
        String winnerLabel = (realVotes >= fakeVotes) ? "AUTHENTIC" : "FAKE";
        int winnerVotes = Math.max(realVotes, fakeVotes);
        double confidence = (double) winnerVotes / effectiveK;

        return new ClassificationResult(winnerLabel, confidence, bestSim, bestName, topKDetails);
    }
}