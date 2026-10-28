package graph;


// a single vertex in the image graph
// each node represents one region (cell) in the image grid
// it stores an id, a label like "r0c1", and a colour histogram feature vector
public class Node {

    private int id;
    private String label;
    private double[] featureVector;


    /**
     * creates a node with an id, a label and a colour histogram feature vector
     *
     * @param id unique integer id for this node
     * @param label human readable label like "r0c1" for row 0 column 1
     * @param featureVector the colour histogram extracted from this image region
     */
    public Node(int id, String label, double[] featureVector) {
        this.id = id;
        this.label = label;
        this.featureVector = featureVector.clone();
    }

    /**
     * returns the unique id of this node
     *
     * @return the node id
     */
    public int getId() { return id; }

    /**
     * returns the label of this node
     *
     * @return the node label
     */
    public String getLabel() { return label; }

    /**
     * updates the label of this node
     *
     * @param label the new label to set
     */
    public void setLabel(String label) { this.label = label; }

    /**
     * returns the feature vector for this node
     * we return a copy so nobody can accidentally change the original
     *
     * @return a copy of the feature vector array
     */
    public double[] getFeatureVector() { return featureVector.clone(); }

    /**
     * updates the feature vector for this node
     *
     * @param featureVector the new feature vector to store
     */
    public void setFeatureVector(double[] featureVector) {
        this.featureVector = featureVector.clone();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node)) return false;
        return this.id == ((Node) obj).id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() { return "Node[id=" + id + ", label=" + label + "]"; }
}