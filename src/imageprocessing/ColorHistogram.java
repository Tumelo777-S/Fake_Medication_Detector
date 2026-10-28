package imageprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;


// computes a normalised RGB colour histogram from an image or a region of it
// we split each colour channel into 8 bins giving us 24 values total
// a bin is just a bucket that counts how many pixels fall in a certain colour range
public class ColorHistogram {

    public static final int BINS_PER_CHANNEL = 8;
    public static final int HISTOGRAM_LENGTH = BINS_PER_CHANNEL * 3;
    private static final int BIN_WIDTH = 256 / BINS_PER_CHANNEL; // each bin covers 32 colour values


    /**
     * computes the histogram for the whole image
     *
     * @param image the image to process
     * @return a normalised histogram array of length 24
     */
    public static double[] compute(BufferedImage image) {
        return computeRegion(image, 0, 0, image.getWidth(), image.getHeight());
    }


    /**
     * computes the histogram for a rectangular section of the image
     * used when we process individual grid cells
     *
     * @param image the full image
     * @param startX the x pixel where the region starts
     * @param startY the y pixel where the region starts
     * @param width how wide the region is in pixels
     * @param height how tall the region is in pixels
     * @return a normalised histogram array of length 24
     */
    public static double[] computeRegion(BufferedImage image,
                                         int startX, int startY,
                                         int width, int height) {
        int[] rBins = new int[BINS_PER_CHANNEL];
        int[] gBins = new int[BINS_PER_CHANNEL];
        int[] bBins = new int[BINS_PER_CHANNEL];
        int totalPixels = 0;

        int endX = Math.min(startX + width, image.getWidth());
        int endY = Math.min(startY + height, image.getHeight());

        // go through every pixel in the region
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {

                // get the individual r, g, b values for this pixel
                Color c = new Color(image.getRGB(x, y));
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                // figure out which bin each channel falls into
                int redBin = r / BIN_WIDTH;
                if (redBin >= BINS_PER_CHANNEL) {
                    redBin = BINS_PER_CHANNEL - 1;
                }

                int greenBin = g / BIN_WIDTH;
                if (greenBin >= BINS_PER_CHANNEL) {
                    greenBin = BINS_PER_CHANNEL - 1;
                }

                int blueBin = b / BIN_WIDTH;
                if (blueBin >= BINS_PER_CHANNEL) {
                    blueBin = BINS_PER_CHANNEL - 1;
                }

                rBins[redBin]++;
                gBins[greenBin]++;
                bBins[blueBin]++;
                totalPixels++;
            }
        }

        double[] histogram = new double[HISTOGRAM_LENGTH];
        if (totalPixels == 0) {
            return histogram;
        }

        // normalize each bin by dividing by total pixels so values are between 0 and 1
        for (int i = 0; i < BINS_PER_CHANNEL; i++) {

            // red bins go first
            histogram[i] = rBins[i] / (double) totalPixels;

            // then green
            histogram[i + BINS_PER_CHANNEL] = gBins[i] / (double) totalPixels;

            // then blue
            histogram[i + BINS_PER_CHANNEL * 2] = bBins[i] / (double) totalPixels;
        }

        return histogram;
    }
}