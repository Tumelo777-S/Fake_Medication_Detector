package imageprocessing;

import java.awt.image.BufferedImage;


// produces a single feature vector that describes a whole image
// it combines a global colour histogram (24 values) with
// individual histograms for each cell in a 6x6 grid (6x6x24 = 864 values)
// total feature vector length is 888, then we L2 normalise it
public class FeatureExtractor {

    public static final int GRID_ROWS = 6; // 6 rows works well for the graph size
    public static final int GRID_COLS = 6;
    public static final int FEATURE_LENGTH =
            ColorHistogram.HISTOGRAM_LENGTH
          + GRID_ROWS * GRID_COLS * ColorHistogram.HISTOGRAM_LENGTH;


    /**
     * extracts the full feature vector from an image
     * starts with a global histogram then adds one histogram per grid cell
     *
     * @param image the image to extract features from
     * @return a normalised feature vector of length 888
     */
    public static double[] extract(BufferedImage image) {
        double[] combined = new double[FEATURE_LENGTH];
        int offset = 0;

        // first add the global histogram for the whole image
        double[] globalHist = ColorHistogram.compute(image);
        System.arraycopy(globalHist, 0, combined, offset, globalHist.length);
        offset += globalHist.length;

        // then add a histogram for each cell in the grid
        int imgW = image.getWidth();
        int imgH = image.getHeight();
        int cellW = Math.max(1, imgW / GRID_COLS);
        int cellH = Math.max(1, imgH / GRID_ROWS);

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = col * cellW;
                int y = row * cellH;
                int w = (col == GRID_COLS - 1) ? imgW - x : cellW;
                int h = (row == GRID_ROWS - 1) ? imgH - y : cellH;

                double[] cellHist = ColorHistogram.computeRegion(image, x, y, w, h);
                System.arraycopy(cellHist, 0, combined, offset, cellHist.length);
                offset += cellHist.length;
            }
        }

        return l2Normalise(combined);
    }


    // divides each value in the vector by the vector's total length
    // this makes sure all feature vectors have the same scale
    private static double[] l2Normalise(double[] vec) {
        double sumSq = 0.0;
        for (double v : vec) {
            sumSq += v * v;
        }
        if (sumSq == 0.0) {
            return vec;
        }
        double magnitude = Math.sqrt(sumSq);
        for (int i = 0; i < vec.length; i++) {
            vec[i] /= magnitude;
        }
        return vec;
    }
}