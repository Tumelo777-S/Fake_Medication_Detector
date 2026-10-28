package imageprocessing;

import graph.AdjacencyList;
import graph.Graph;
import graph.Node;

import java.awt.image.BufferedImage;


// takes an image and turns it into a kNN graph
// splits the image into a 6x6 grid, creates a node for each cell,
// then connects similar nodes based on their colour histograms
public class ImageToGraph {


    /**
     * converts a BufferedImage into a kNN graph
     * each grid cell becomes a node with a colour histogram as its feature vector
     * nodes are then connected using the KNNGraph algorithm
     *
     * @param image the image to convert
     * @param k how many nearest neighbours each node should connect to
     * @return the completed graph
     */
    public static Graph convert(BufferedImage image, int k) {
        Graph graph = new AdjacencyList();

        // figure out the size of each grid cell
        int imgW = image.getWidth();
        int imgH = image.getHeight();
        int gridRows = FeatureExtractor.GRID_ROWS;
        int gridCols = FeatureExtractor.GRID_COLS;
        int cellW = Math.max(1, imgW / gridCols);
        int cellH = Math.max(1, imgH / gridRows);

        // create one node per grid cell
        int nodeId = 0;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int x = col * cellW;
                int y = row * cellH;

                // last row/col gets whatever pixels are left over
                int w = (col == gridCols - 1) ? imgW - x : cellW;
                int h = (row == gridRows - 1) ? imgH - y : cellH;

                // compute the colour histogram for this cell and use it as the feature vector
                double[] hist = ColorHistogram.computeRegion(image, x, y, w, h);
                graph.addNode(new Node(nodeId, "r" + row + "c" + col, hist));
                nodeId++;
            }
        }

        // connect each node to its k nearest neighbours
        algorithms.KNNGraph.connectKNN(graph, k);
        return graph;
    }
}