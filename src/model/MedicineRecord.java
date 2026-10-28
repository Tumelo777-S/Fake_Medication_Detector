package model;

import graph.Graph;


// represents a single image record in the database
// stores the file name, path, whether its real or fake, the graph, and feature vector
public class MedicineRecord {

    private String name;
    private String imagePath;
    private boolean isReal;
    private Graph graph;
    private double[] featureVector;


    /**
     * creates a medicine record from a loaded image
     *
     * @param name the filename of the image
     * @param imagePath the full path to the image file
     * @param isReal true if the medicine is genuine, false if fake
     * @param graph the kNN graph built from this image
     * @param featureVector the feature vector extracted from this image
     */
    public MedicineRecord(String name, String imagePath, boolean isReal,
                          Graph graph, double[] featureVector) {
        this.name = name;
        this.imagePath = imagePath;
        this.isReal = isReal;
        this.graph = graph;
        this.featureVector = featureVector.clone();
    }

    /** returns the filename of this record's image */
    public String getName() { return name; }

    /** returns the full file path to this record's image */
    public String getImagePath() { return imagePath; }

    /** returns true if this record is a genuine medicine */
    public boolean isReal() { return isReal; }

    /** returns "REAL" or "FAKE" as a string */
    public String getLabelString() { return isReal ? "REAL" : "FAKE"; }

    /** returns the graph built from this record's image */
    public Graph getGraph() { return graph; }

    /** returns a copy of the feature vector for this record */
    public double[] getFeatureVector() { return featureVector.clone(); }

    @Override
    public String toString() { return "MedicineRecord[" + name + " | " + getLabelString() + "]"; }
}