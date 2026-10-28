package model;

import graph.Graph;
import imageprocessing.FeatureExtractor;
import imageprocessing.ImageToGraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


// holds all the medicine records loaded from the dataset folder
// reads images from Real/ and Fake/ subfolders and builds a graph for each one
public class Database {

    private static final int KNN_K = 5;
    private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png"};

    private final List<MedicineRecord> allRecords;
    private int realCount = 0;
    private int fakeCount = 0;
    private boolean loaded = false;
    private String datasetPath = "";


    /** creates an empty database with no records loaded yet */
    public Database() { allRecords = new ArrayList<>(); }


    /**
     * loads all images from the dataset folder into memory
     * expects a Real/ and Fake/ subfolder inside the given root path
     *
     * @param datasetRoot path to the dataset folder
     * @param listener callback to report progress while loading
     * @throws Exception if the folder doesn't exist
     */
    public void load(String datasetRoot, LoadProgressListener listener) throws Exception {
        allRecords.clear();
        realCount = 0;
        fakeCount = 0;
        loaded = false;
        datasetPath = datasetRoot;

        File root = new File(datasetRoot);
        if (!root.exists() || !root.isDirectory()) {
            throw new Exception("Dataset folder not found: " + datasetRoot);
        }

        // load real images first, then fake
        File realFolder = new File(root, "Real");
        if (realFolder.exists()) {
            loadFolder(realFolder, true, listener);
        }

        File fakeFolder = new File(root, "Fake");
        if (fakeFolder.exists()) {
            loadFolder(fakeFolder, false, listener);
        }

        loaded = true;
        if (listener != null) {
            listener.onComplete(realCount, fakeCount);
        }
    }

    // reads all images from a folder and converts each one into a MedicineRecord
    private void loadFolder(File folder, boolean isReal, LoadProgressListener listener) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!isSupportedImage(file)) continue;
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null) continue;

                // convert the image into a graph and extract its feature vector
                Graph graph = ImageToGraph.convert(img, KNN_K);
                double[] featureVec = FeatureExtractor.extract(img);

                MedicineRecord record = new MedicineRecord(
                    file.getName(), file.getAbsolutePath(), isReal, graph, featureVec);
                allRecords.add(record);

                if (isReal) {
                    realCount++;
                } else {
                    fakeCount++;
                }

                if (listener != null) {
                    listener.onImageLoaded(file.getName(), isReal, realCount + fakeCount);
                }

            } catch (Exception e) {
                System.err.println("Could not load: " + file.getName() + " - " + e.getMessage());
            }
        }
    }

    // checks if a file has one of the supported image extensions
    private boolean isSupportedImage(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }


    /**
     * returns all loaded records as a new list
     *
     * @return list of all MedicineRecords
     */
    public List<MedicineRecord> getAllRecords() { return new ArrayList<>(allRecords); }

    /**
     * returns only the records that are marked as real
     *
     * @return list of real MedicineRecords
     */
    public List<MedicineRecord> getRealRecords() {
        List<MedicineRecord> real = new ArrayList<>();
        for (MedicineRecord r : allRecords) {
            if (r.isReal()) {
                real.add(r);
            }
        }
        return real;
    }

    /**
     * returns only the records that are marked as fake
     *
     * @return list of fake MedicineRecords
     */
    public List<MedicineRecord> getFakeRecords() {
        List<MedicineRecord> fake = new ArrayList<>();
        for (MedicineRecord r : allRecords) {
            if (!r.isReal()) {
                fake.add(r);
            }
        }
        return fake;
    }

    /** returns how many records are in the database total */
    public int totalCount() { return allRecords.size(); }

    /** returns how many of the loaded records are genuine */
    public int getRealCount() { return realCount; }

    /** returns how many of the loaded records are fake */
    public int getFakeCount() { return fakeCount; }

    /** returns true if the database has been loaded at least once */
    public boolean isLoaded() { return loaded; }

    /** returns the path of the dataset folder that was last loaded */
    public String getDatasetPath() { return datasetPath; }


    // interface used to report loading progress back to the UI
    public interface LoadProgressListener {

        /**
         * called each time one image finishes loading
         *
         * @param imageName the name of the image that was just loaded
         * @param isReal true if this image came from the Real folder
         * @param totalLoaded how many images have loaded so far in total
         */
        void onImageLoaded(String imageName, boolean isReal, int totalLoaded);

        /**
         * called once after all images have finished loading
         *
         * @param realCount total number of real images loaded
         * @param fakeCount total number of fake images loaded
         */
        void onComplete(int realCount, int fakeCount);
    }
}