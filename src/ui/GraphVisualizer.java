package ui;

import algorithms.Dijkstra;
import algorithms.MST;
import graph.Edge;
import graph.Graph;
import graph.Node;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// draws the image graph as a 2D diagram inside a scrollable canvas
// nodes are colour coded by their feature vector and you can click them to select source/target
public class GraphVisualizer extends Pane {

    // drawing constants
    private static final int    NODE_RADIUS    = 14;
    private static final int    PADDING        = 30;
    private static final Color  EDGE_COLOR     = Color.rgb(180, 180, 180, 160 / 255.0);
    private static final Color  NODE_DEFAULT   = Color.rgb(80,  80,  80);
    private static final Color  NODE_PATH      = Color.rgb(40,  40,  40);
    private static final Color  NODE_BORDER    = Color.rgb(50,  50,  50);
    private static final Color  BG_COLOR       = Color.WHITE;
    private static final Color  MST_COLOR      = Color.rgb(100, 100, 100, 160 / 255.0);
    private static final Color  SELECT_COLOR   = Color.BLACK;
    private static final Color  TARGET_COLOR   = Color.rgb(255, 80,  80);

    // Grid layout
    private final int gridCols;
    private final int gridRows;

    // Data
    private Graph                        graph;
    private Dijkstra.ShortestPathResult  highlightedPath;
    private final Map<Integer, double[]> nodePositions = new HashMap<>();

    // Feature flags
    private boolean    colourByFeature = true;
    private boolean    showMST         = false;
    private List<Edge> mstEdges        = null;

    // Selection
    private int selectedSourceId = -1;
    private int selectedTargetId = -1;

    // Listener
    private NodeClickListener nodeClickListener;

    // The single canvas that covers the whole Pane
    private final Canvas canvas;

    // callback interface for when the user clicks a node
    public interface NodeClickListener {
        void onNodeClicked(int nodeId, boolean isLeftClick);
    }

    // ------------------------------------------------------------------
    //  Constructor
    // ------------------------------------------------------------------
    public GraphVisualizer(int gridRows, int gridCols) {
        this.gridRows = gridRows;
        this.gridCols = gridCols;

        // Match Swing preferred / minimum sizes
        setPrefSize(420, 380);
        setMinSize(200, 200);

        canvas = new Canvas(420, 380);
        getChildren().add(canvas);

        // Resize canvas when the Pane is resized
        widthProperty().addListener((obs, o, n)  -> { canvas.setWidth(n.doubleValue());  redraw(); });
        heightProperty().addListener((obs, o, n) -> { canvas.setHeight(n.doubleValue()); redraw(); });

        // Mouse click for node selection
        canvas.setOnMouseClicked(this::handleClick);

        // Tooltip (re-evaluated on each MOVE, same logic as Swing getToolTipText)
        Tooltip tip = new Tooltip();
        Tooltip.install(canvas, tip);
        canvas.setOnMouseMoved(e -> {
            if (graph == null) { tip.hide(); return; }
            for (Map.Entry<Integer, double[]> entry : nodePositions.entrySet()) {
                double[] pos = entry.getValue();
                double dx = e.getX() - pos[0];
                double dy = e.getY() - pos[1];
                if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS + 2) {
                    Node n = graph.getNode(entry.getKey());
                    if (n != null) {
                        tip.setText(n.getLabel()
                                + "\nid=" + n.getId()
                                + "\nneighbours=" + graph.getNeighbours(n.getId()).size());
                        return;
                    }
                }
            }
            tip.setText(null);
        });

        redraw();
    }

    // public methods

    /**
     * loads a new graph into the visualiser and resets all selections and highlights
     *
     * @param graph the graph to display
     */
    public void setGraph(Graph graph) {
        this.graph           = graph;
        this.highlightedPath = null;
        this.mstEdges        = null;
        this.selectedSourceId = -1;
        this.selectedTargetId = -1;
        updatePreferredSize();
        redraw();
    }

    /**
     * refreshes the graph display without resetting the selected source and target nodes
     * useful after a CRUD operation so the user keeps their current selection
     *
     * @param graph the updated graph to display
     */
    public void refreshGraph(Graph graph) {
        this.graph           = graph;
        this.highlightedPath = null;
        this.mstEdges        = null;
        // keep selectedSourceId and selectedTargetId so the user doesn't lose their picks
        updatePreferredSize();
        redraw();
    }

    /**
     * highlights the given shortest path on the graph canvas
     *
     * @param result the dijkstra result containing the path to highlight
     */
    public void highlightPath(Dijkstra.ShortestPathResult result) {
        this.highlightedPath = result;
        redraw();
    }

    /**
     * turns colour-by-feature on or off
     * when on, each node is coloured based on its RGB histogram values
     *
     * @param enabled true to colour by feature, false for default grey
     */
    public void setColourByFeature(boolean enabled) {
        this.colourByFeature = enabled;
        redraw();
    }

    /**
     * shows or hides the minimum spanning tree overlay on the graph
     *
     * @param show true to show the MST, false to hide it
     */
    public void setShowMST(boolean show) {
        this.showMST = show;
        if (show && graph != null) {
            mstEdges = MST.computeMST(graph);
        } else {
            mstEdges = null;
        }
        redraw();
    }

    /**
     * sets the source node for a shortest path search
     *
     * @param nodeId the id of the node to use as source
     */
    public void setSelectedSource(int nodeId) {
        this.selectedSourceId = nodeId;
        redraw();
    }

    /**
     * sets the target node for a shortest path search
     *
     * @param nodeId the id of the node to use as target
     */
    public void setSelectedTarget(int nodeId) {
        this.selectedTargetId = nodeId;
        redraw();
    }

    public int getSelectedSourceId() { return selectedSourceId; }
    public int getSelectedTargetId() { return selectedTargetId; }

    /**
     * registers a listener that gets called whenever the user clicks a node
     *
     * @param listener the listener to call
     */
    public void setNodeClickListener(NodeClickListener listener) {
        this.nodeClickListener = listener;
    }

    // size helpers

    private void updatePreferredSize() {
        if (graph == null || graph.nodeCount() == 0) {
            setPrefSize(420, 380);
            canvas.setWidth(420);
            canvas.setHeight(380);
            return;
        }
        int cols       = Math.max(1, gridCols);
        int totalRows  = (graph.nodeCount() + cols - 1) / cols;
        int neededH    = totalRows * 70 + 2 * PADDING;
        int neededW    = cols     * 70 + 2 * PADDING;
        double w = Math.max(420, neededW);
        double h = Math.max(380, neededH);
        setPrefSize(w, h);
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    // click handling

    private void handleClick(MouseEvent e) {
        if (graph == null) return;
        for (Map.Entry<Integer, double[]> entry : nodePositions.entrySet()) {
            double[] pos = entry.getValue();
            double dx = e.getX() - pos[0];
            double dy = e.getY() - pos[1];
            if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS + 2) {
                int     nodeId = entry.getKey();
                boolean isLeft = e.getButton() == MouseButton.PRIMARY;

                if (isLeft) {
                    selectedSourceId = nodeId;
                } else {
                    selectedTargetId = nodeId;
                }
                redraw();

                if (nodeClickListener != null) {
                    nodeClickListener.onNodeClicked(nodeId, isLeft);
                }
                return;
            }
        }
    }

    // rendering - draws everything on the canvas

    /**
     * redraws the whole graph canvas, call this whenever something changes
     */
    public void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Clear background
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        if (graph == null || graph.nodeCount() == 0) {
            drawEmptyMessage(gc, w, h);
            return;
        }

        computeNodePositions();
        drawEdges(gc);

        if (showMST && mstEdges != null) {
            drawMSTEdges(gc);
        }

        if (highlightedPath != null && highlightedPath.pathFound()) {
            drawHighlightedPath(gc);
        }

        drawNodes(gc);
    }

    // ------------------------------------------------------------------

    private void computeNodePositions() {
        nodePositions.clear();
        int cols       = Math.max(1, gridCols);
        int totalNodes = graph.getAllNodes().size();
        int actualRows = Math.max(1, (totalNodes + cols - 1) / cols);

        int xStep = 70;
        int yStep = 70;

        // Expand canvas if needed (matches Swing revalidate logic)
        double neededW = cols      * xStep + 2 * PADDING;
        double neededH = actualRows * yStep + 2 * PADDING;
        double curW = Math.max(420, neededW);
        double curH = Math.max(380, neededH);
        if (canvas.getWidth() < curW || canvas.getHeight() < curH) {
            canvas.setWidth(curW);
            canvas.setHeight(curH);
            setPrefSize(curW, curH);
        }

        for (Node node : graph.getAllNodes()) {
            int id  = node.getId();
            int row = id / gridCols;
            int col = id % gridCols;
            double x = PADDING + col * xStep;
            double y = PADDING + row * yStep;
            nodePositions.put(id, new double[]{x, y});
        }
    }

    // ------------------------------------------------------------------

    private void drawEdges(GraphicsContext gc) {
        gc.setStroke(EDGE_COLOR);
        for (Edge edge : graph.getAllEdges()) {
            double[] pSrc  = nodePositions.get(edge.getSource().getId());
            double[] pDest = nodePositions.get(edge.getDestination().getId());
            if (pSrc == null || pDest == null) continue;
            double thickness = Math.max(0.5, 2.0 / (1.0 + edge.getWeight() * 5));
            gc.setLineWidth(thickness);
            gc.strokeLine(pSrc[0], pSrc[1], pDest[0], pDest[1]);
        }
    }

    private void drawMSTEdges(GraphicsContext gc) {
        gc.setStroke(MST_COLOR);
        gc.setLineWidth(2.5);
        for (Edge edge : mstEdges) {
            double[] p1 = nodePositions.get(edge.getSource().getId());
            double[] p2 = nodePositions.get(edge.getDestination().getId());
            if (p1 != null && p2 != null) {
                gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
            }
        }
    }

    private void drawHighlightedPath(GraphicsContext gc) {
        List<Integer> path = highlightedPath.path;
        gc.setStroke(NODE_PATH);
        gc.setLineWidth(3.0);
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = nodePositions.get(path.get(i));
            double[] p2 = nodePositions.get(path.get(i + 1));
            if (p1 != null && p2 != null) {
                gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
            }
        }
    }

    // ------------------------------------------------------------------

    private void drawNodes(GraphicsContext gc) {
        Set<Integer> pathNodeIds = new HashSet<>();
        if (highlightedPath != null) {
            pathNodeIds.addAll(highlightedPath.path);
        }

        for (Node node : graph.getAllNodes()) {
            double[] pos = nodePositions.get(node.getId());
            if (pos == null) continue;

            double cx = pos[0];
            double cy = pos[1];
            boolean onPath = pathNodeIds.contains(node.getId());

            double x = cx - NODE_RADIUS;
            double y = cy - NODE_RADIUS;
            double d = NODE_RADIUS * 2;

            // Fill colour
            Color fillColor;
            if (onPath) {
                fillColor = NODE_PATH;
            } else if (colourByFeature) {
                fillColor = getNodeColour(node);
            } else {
                fillColor = NODE_DEFAULT;
            }
            gc.setFill(fillColor);
            gc.fillOval(x, y, d, d);

            // Border colour
            if (node.getId() == selectedSourceId) {
                gc.setStroke(SELECT_COLOR);
                gc.setLineWidth(3.0);
            } else if (node.getId() == selectedTargetId) {
                gc.setStroke(TARGET_COLOR);
                gc.setLineWidth(3.0);
            } else {
                gc.setStroke(NODE_BORDER);
                gc.setLineWidth(1.5);
            }
            gc.strokeOval(x, y, d, d);

            // Label — auto-contrast
            double brightness = (fillColor.getRed()   * 0.299
                               + fillColor.getGreen() * 0.587
                               + fillColor.getBlue()  * 0.114);
            gc.setFill(brightness > 0.5 ? Color.BLACK : Color.WHITE);
            gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(String.valueOf(node.getId()), cx, cy);
        }
    }

    // ------------------------------------------------------------------

    // figures out the display colour for a node based on its feature vector
    // uses the first 24 values (8 bins each for R, G, B) to estimate the dominant colour
    private Color getNodeColour(Node node) {
        double[] fv = node.getFeatureVector();
        if (fv.length < 24) return NODE_DEFAULT;

        int    binsPerChannel = 8;
        double r = 0, g = 0, b = 0;
        for (int i = 0; i < binsPerChannel; i++) {
            double midpoint = (i + 0.5) / binsPerChannel;
            r += fv[i]                         * midpoint;
            g += fv[i + binsPerChannel]        * midpoint;
            b += fv[i + 2 * binsPerChannel]    * midpoint;
        }

        // Scale 0-255, clamp (matches Swing: min 30, max 255)
        int ri = clamp((int)(r * 255 * 3), 30, 255);
        int gi = clamp((int)(g * 255 * 3), 30, 255);
        int bi = clamp((int)(b * 255 * 3), 30, 255);
        return Color.rgb(ri, gi, bi);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.min(hi, Math.max(lo, v));
    }

    // ------------------------------------------------------------------

    private void drawEmptyMessage(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setFont(Font.font("SansSerif", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Graph appears after analysis", w / 2, h / 2);
    }
}