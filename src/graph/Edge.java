package graph;


// represents a connection between two nodes in the graph
// the weight is the euclidean distance between their feature vectors (lower = more similar)
public class Edge {

    private Node source;
    private Node destination;
    private double weight;


    /**
     * creates an edge connecting two nodes with a given weight
     *
     * @param source the node the edge starts from
     * @param destination the node the edge points to
     * @param weight the distance between the two nodes' feature vectors
     */
    public Edge(Node source, Node destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    /**
     * returns the source node of this edge
     *
     * @return the source node
     */
    public Node getSource() { return source; }

    /**
     * returns the destination node of this edge
     *
     * @return the destination node
     */
    public Node getDestination() { return destination; }

    /**
     * returns the weight of this edge
     *
     * @return the edge weight
     */
    public double getWeight() { return weight; }

    /**
     * updates the weight of this edge
     *
     * @param newWeight the new weight to set
     */
    public void setWeight(double newWeight) { this.weight = newWeight; }


    /**
     * given one endpoint of the edge, returns the other one
     *
     * @param node one of the two nodes in this edge
     * @return the other node, or null if the given node isn't part of this edge
     */
    public Node getOtherNode(Node node) {
        if (node.equals(source)) {
            return destination;
        }
        if (node.equals(destination)) {
            return source;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Edge[" + source.getLabel()
             + " --(" + String.format("%.4f", weight) + ")--> "
             + destination.getLabel() + "]";
    }
}