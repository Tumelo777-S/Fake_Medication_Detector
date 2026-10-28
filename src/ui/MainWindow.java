package ui;

import algorithms.BFS;
import algorithms.Classification;
import algorithms.Classification.ClassificationResult;
import algorithms.Dijkstra;
import algorithms.KNNGraph;
import algorithms.MST;
import algorithms.SimilarityDetection;
import graph.Edge;
import graph.Graph;
import graph.Node;
import imageprocessing.ColorHistogram;
import imageprocessing.FeatureExtractor;
import imageprocessing.ImageToGraph;
import model.Database;
import model.MedicineRecord;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// the main application window shown after the landing page
// contains three columns: image panel, graph visualiser, and results panel
public class MainWindow {

    private static final Color BG_MAIN    = Color.rgb(248,248,248);
    private static final Color BG_PANEL   = Color.WHITE;
    private static final Color BORDER_CLR = Color.rgb(220,220,220);
    private static final Color TEXT_MAIN  = Color.rgb(30,30,30);
    private static final Color TEXT_MUTED = Color.rgb(120,120,120);

    private final Stage stage;
    private ImagePanel      imagePanel;
    private GraphVisualizer graphVisualizer;
    private ResultPanel     resultPanel;
    private Label           statusLabel, dbStatusLabel;
    private ProgressBar     progressBar;
    private Slider          kSlider;
    private Label           kValueLabel;
    private CheckBox        mstToggle;
    private javafx.scene.shape.Circle dbDot;

    private Button btnLoad, btnUpload, btnAnalyse, btnPath, btnClear;
    private Button btnAddNode, btnRemoveNode, btnUpdateNode, btnRemoveEdge, btnUpdateEdge;

    private Database             database = new Database();
    private BufferedImage        uploadedImage;
    private Graph                uploadedGraph;
    private double[]             uploadedFeatureVector;
    private ClassificationResult lastResult;
    private int                  currentK = 5;

    /** creates the main window, builds all the UI components and sets up the stage */
    public MainWindow() {
        stage = new Stage();
        stage.setTitle("Fake Medicine Detection");
        stage.setMinWidth(1150);
        stage.setMinHeight(720);
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BG_MAIN, null, null)));
        root.setPadding(new Insets(20));
        root.setTop(buildHeader());
        root.setCenter(buildCentre());
        root.setBottom(buildControls());
        BorderPane.setMargin(root.getTop(), new Insets(0, 0, 20, 0));
        BorderPane.setMargin(root.getBottom(), new Insets(20, 0, 0, 0));
        stage.setScene(new Scene(root, 1400, 820));
        stage.setMaximized(true);
        stage.setOnCloseRequest(e -> Platform.exit());
    }


    /** shows the main window on screen */
    public void show() { stage.show(); }

    private HBox buildHeader() {
        HBox h = new HBox();
        h.setAlignment(Pos.CENTER_LEFT);
        h.setBackground(new Background(new BackgroundFill(BG_MAIN, null, null)));
        Label title = new Label("Fake medicine detector");
        title.setFont(Font.font("SansSerif", 24));
        title.setTextFill(TEXT_MAIN);
        HBox right = new HBox(15);
        right.setAlignment(Pos.CENTER_RIGHT);
        statusLabel = new Label("Load a dataset to begin");
        statusLabel.setFont(Font.font("SansSerif", 15));
        statusLabel.setTextFill(TEXT_MAIN);
        // DB status pill
        HBox pill = new HBox(6);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(4, 12, 4, 10));
        pill.setStyle("-fx-background-color: rgb(245,245,245); -fx-border-color: rgb(220,220,220); -fx-border-radius: 20; -fx-background-radius: 20;");
        dbDot = new javafx.scene.shape.Circle(4, Color.rgb(180,180,180));
        dbStatusLabel = new Label(" No database");
        dbStatusLabel.setFont(Font.font("SansSerif", 14));
        dbStatusLabel.setTextFill(TEXT_MAIN);
        pill.getChildren().addAll(dbDot, dbStatusLabel);
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(100);
        progressBar.setVisible(false);
        right.getChildren().addAll(statusLabel, pill, progressBar);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        h.getChildren().addAll(title, spacer, right);
        return h;
    }
    private GridPane buildCentre() {
        GridPane centre = new GridPane();
        centre.setHgap(20);
        centre.setVgap(0);
        centre.setBackground(new Background(new BackgroundFill(BG_MAIN, null, null)));
        // 3 equal columns — each 33.33%
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setFillWidth(true);
            cc.setHgrow(Priority.ALWAYS);
            centre.getColumnConstraints().add(cc);
        }
        RowConstraints rc = new RowConstraints();
        rc.setFillHeight(true);
        rc.setVgrow(Priority.ALWAYS);
        centre.getRowConstraints().add(rc);
        // column 1 - image panel

        VBox col1 = roundedCol();
        col1.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox c1Top = new HBox(12);
        c1Top.setAlignment(Pos.CENTER_LEFT);
        c1Top.getChildren().addAll(createBadge("1"), lbl("Uploaded image", 16));
        imagePanel = new ImagePanel();
        imagePanel.setMinHeight(300);
        VBox.setVgrow(imagePanel, Priority.ALWAYS);
        btnLoad   = createRoundedBtn("Load DB");   btnLoad.setOnAction(e -> onLoadDataset());
        btnUpload = createRoundedBtn("Upload");     btnUpload.setOnAction(e -> onUploadImage()); btnUpload.setDisable(true);
        btnLoad.setMaxWidth(Double.MAX_VALUE);   HBox.setHgrow(btnLoad,   Priority.ALWAYS);
        btnUpload.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnUpload, Priority.ALWAYS);
        HBox c1Btns = new HBox(12, btnLoad, btnUpload);
        c1Btns.setPadding(new Insets(12, 0, 0, 0));
        col1.getChildren().addAll(c1Top, imagePanel, c1Btns);
        // column 2 - graph visualiser

        VBox col2 = roundedCol();
        col2.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox c2Top = new HBox(12);
        c2Top.setAlignment(Pos.CENTER_LEFT);
        c2Top.getChildren().addAll(createBadge("2"), lbl("Image graph", 16));
        graphVisualizer = new GraphVisualizer(FeatureExtractor.GRID_ROWS, FeatureExtractor.GRID_COLS);
        graphVisualizer.setNodeClickListener((nodeId, isLeft) ->
            setStatus("Node " + nodeId + " selected as " + (isLeft ? "SOURCE" : "TARGET")));
        ScrollPane graphScroll = new ScrollPane(graphVisualizer);
        graphScroll.setFitToWidth(false); graphScroll.setFitToHeight(false);
        graphScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        graphScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        graphScroll.setStyle("-fx-background-color: white;");
        graphScroll.setMinHeight(200);
        VBox.setVgrow(graphScroll, Priority.ALWAYS);
        btnAnalyse = createRoundedBtn("Analyse");   btnAnalyse.setOnAction(e -> onAnalyse());  btnAnalyse.setDisable(true);
        btnPath    = createRoundedBtn("Find path");  btnPath.setOnAction(e -> onFindPath());    btnPath.setDisable(true);
        HBox c2Row1 = new HBox(12, btnAnalyse, btnPath);
        btnAnalyse.setMaxWidth(Double.MAX_VALUE); btnPath.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnAnalyse, Priority.ALWAYS); HBox.setHgrow(btnPath, Priority.ALWAYS);
        mstToggle = new CheckBox("MST");
        mstToggle.setFont(Font.font("SansSerif", 13)); mstToggle.setTextFill(TEXT_MAIN);
        mstToggle.setOnAction(e -> graphVisualizer.setShowMST(mstToggle.isSelected()));
        Label kLbl = new Label("k ="); kLbl.setFont(Font.font("SansSerif", 13)); kLbl.setTextFill(TEXT_MAIN);
        kSlider = new Slider(1, 15, 5); kSlider.setPrefWidth(100);
        kValueLabel = new Label("5"); kValueLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 13)); kValueLabel.setTextFill(TEXT_MAIN);
        kSlider.valueProperty().addListener((obs, ov, nv) -> {
            currentK = nv.intValue(); kValueLabel.setText(String.valueOf(currentK));
            if (!kSlider.isValueChanging() && uploadedImage != null) rebuildGraph();
        });
        HBox c2Row2 = new HBox(10, mstToggle, kLbl, kSlider, kValueLabel);
        c2Row2.setAlignment(Pos.CENTER_LEFT);
        VBox c2Bot = new VBox(15, c2Row1, c2Row2);
        c2Bot.setPadding(new Insets(16, 0, 0, 0));
        col2.getChildren().addAll(c2Top, graphScroll, c2Bot);
        // column 3 - results panel

        VBox col3 = roundedCol();
        col3.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox c3Top = new HBox(12);
        c3Top.setAlignment(Pos.CENTER_LEFT);
        c3Top.getChildren().addAll(createBadge("3"), lbl("Results", 16));
        resultPanel = new ResultPanel();
        resultPanel.setMinHeight(200);
        VBox.setVgrow(resultPanel, Priority.ALWAYS);
        col3.getChildren().addAll(c3Top, resultPanel);
        centre.add(col1, 0, 0);
        centre.add(col2, 1, 0);
        centre.add(col3, 2, 0);
        return centre;
    }
    private HBox buildControls() {
        VBox outer = roundedCol();
        outer.setPadding(new Insets(2, 15, 2, 15));
        Label gLbl = new Label("GRAPH");
        gLbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        gLbl.setTextFill(TEXT_MUTED);
        btnAddNode    = createRoundedBtn("+ Node");   btnAddNode.setOnAction(e -> onAddNode());       btnAddNode.setDisable(true);
        btnRemoveNode = createRoundedBtn("- Node");   btnRemoveNode.setOnAction(e -> onRemoveNode()); btnRemoveNode.setDisable(true);
        btnUpdateNode = createRoundedBtn("Upd node"); btnUpdateNode.setOnAction(e -> onUpdateNode()); btnUpdateNode.setDisable(true);
        btnRemoveEdge = createRoundedBtn("- Edge");   btnRemoveEdge.setOnAction(e -> onRemoveEdge()); btnRemoveEdge.setDisable(true);
        btnUpdateEdge = createRoundedBtn("Upd edge"); btnUpdateEdge.setOnAction(e -> onUpdateEdge()); btnUpdateEdge.setDisable(true);
        btnClear      = createRoundedBtn("Clear");    btnClear.setOnAction(e -> onClear());            btnClear.setDisable(true);
        HBox row = new HBox(10, gLbl, btnAddNode, btnRemoveNode, btnUpdateNode, btnRemoveEdge, btnUpdateEdge, btnClear);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 0, 12, 0));
        outer.getChildren().add(row);
        HBox wrap = new HBox(outer);
        wrap.setBackground(new Background(new BackgroundFill(BG_MAIN, null, null)));
        HBox.setHgrow(outer, Priority.ALWAYS);
        return wrap;
    }
    private void onAddNode() {
        if (uploadedGraph == null) return;
        try {
            int id = uploadedGraph.nodeCount();
            TextInputDialog dlg = new TextInputDialog("region_" + id);
            dlg.setTitle("Add Node"); dlg.setHeaderText(null);
            dlg.setContentText("Enter label for new node (ID will be " + id + "):");
            java.util.Optional<String> res = dlg.showAndWait();
            if (res.isEmpty() || res.get().trim().isEmpty()) return;
            String label = res.get().trim();
            double[] v = new double[ColorHistogram.HISTOGRAM_LENGTH];
            for (int i = 0; i < v.length; i++) v[i] = Math.random() * 0.1;
            uploadedGraph.addNode(new Node(id, label, v));
            if (uploadedGraph.nodeCount() > 1) KNNGraph.connectKNN(uploadedGraph, Math.min(currentK, uploadedGraph.nodeCount()-1));
            graphVisualizer.refreshGraph(uploadedGraph);
            setStatus("CREATE: Added node " + id + " (" + label + ")");
            afterCrudChange();
        } catch (Exception ex) { setStatus("Error adding node: " + ex.getMessage()); }
    }
    private void onRemoveNode() {
        if (uploadedGraph == null) return;
        try {
            int s = graphVisualizer.getSelectedSourceId();
            if (s < 0) {
                TextInputDialog dlg = new TextInputDialog(); dlg.setTitle("Remove Node");
                dlg.setHeaderText(null); dlg.setContentText("Enter node ID to remove:");
                java.util.Optional<String> r = dlg.showAndWait();
                if (r.isEmpty() || r.get().trim().isEmpty()) return;
                s = Integer.parseInt(r.get().trim());
            }
            if (!uploadedGraph.containsNode(s)) { setStatus("Node " + s + " not found"); return; }
            uploadedGraph.removeNode(s); graphVisualizer.refreshGraph(uploadedGraph);
            setStatus("DELETE: Removed node " + s); afterCrudChange();
        } catch (NumberFormatException ex) { setStatus("Invalid input. Enter a number."); }
        catch (Exception ex) { setStatus("Error: " + ex.getMessage()); }
    }
    private void onUpdateNode() {
        if (uploadedGraph == null) return;
        try {
            int s = graphVisualizer.getSelectedSourceId();
            if (s < 0) {
                TextInputDialog dlg = new TextInputDialog(); dlg.setTitle("Update Node");
                dlg.setHeaderText(null); dlg.setContentText("Enter node ID to update:");
                java.util.Optional<String> r = dlg.showAndWait();
                if (r.isEmpty() || r.get().trim().isEmpty()) return;
                s = Integer.parseInt(r.get().trim());
            }
            Node n = uploadedGraph.getNode(s);
            if (n == null) { setStatus("Node " + s + " not found"); return; }
            TextInputDialog dlg = new TextInputDialog(n.getLabel());
            dlg.setTitle("Update Node"); dlg.setHeaderText(null);
            dlg.setContentText("New label for node " + s + ":");
            java.util.Optional<String> lbl = dlg.showAndWait();
            if (lbl.isPresent() && !lbl.get().isEmpty()) n.setLabel(lbl.get());
            double[] fv = n.getFeatureVector();
            for (int i = 0; i < fv.length; i++) fv[i] = fv[i]*0.9 + Math.random()*0.1;
            n.setFeatureVector(fv); graphVisualizer.refreshGraph(uploadedGraph);
            setStatus("UPDATE: Node " + s + " label -> '" + n.getLabel() + "'"); afterCrudChange();
        } catch (NumberFormatException ex) { setStatus("Invalid input. Enter a number."); }
        catch (Exception ex) { setStatus("Error: " + ex.getMessage()); }
    }
    private int[] parseTwoIds() {
        int s = graphVisualizer.getSelectedSourceId(), t = graphVisualizer.getSelectedTargetId();
        if (s >= 0 && t >= 0) return new int[]{s, t};
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Edge"); dlg.setHeaderText(null);
        dlg.setContentText("Enter source,destination node IDs (e.g. 3,5):");
        java.util.Optional<String> res = dlg.showAndWait();
        if (res.isEmpty() || res.get().trim().isEmpty()) return null;
        String[] parts = res.get().split(",");
        if (parts.length != 2) { setStatus("Invalid format. Use: source,destination (e.g. 3,5)"); return null; }
        return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
    }
    private void onRemoveEdge() {
        if (uploadedGraph == null) return;
        try {
            int[] ids = parseTwoIds(); if (ids == null) return;
            int s = ids[0], t = ids[1];
            boolean exists = uploadedGraph.containsEdge(s, t) || uploadedGraph.containsEdge(t, s);
            if (!exists) {
                StringBuilder nb = new StringBuilder();
                for (Edge e : uploadedGraph.getEdgesOfNode(s)) nb.append(e.getDestination().getId()).append(" ");
                setStatus("No edge " + s + "-" + t + ". Node " + s + " connects to: [" + nb.toString().trim() + "]"); return;
            }
            int before = uploadedGraph.edgeCount();
            uploadedGraph.removeEdge(s, t); uploadedGraph.removeEdge(t, s);
            graphVisualizer.setGraph(uploadedGraph);
            setStatus("DELETE: Edge " + s + "-" + t + " removed (edges: " + before + " → " + uploadedGraph.edgeCount() + ")");
            afterCrudChange();
        } catch (NumberFormatException ex) { setStatus("Invalid input. Enter numbers like: 3,5"); }
        catch (Exception ex) { setStatus("Error: " + ex.getMessage()); }
    }
    private void onUpdateEdge() {
        if (uploadedGraph == null) return;
        try {
            int[] ids = parseTwoIds(); if (ids == null) return;
            int s = ids[0], t = ids[1];
            boolean exists = uploadedGraph.containsEdge(s, t) || uploadedGraph.containsEdge(t, s);
            if (!exists) {
                StringBuilder nb = new StringBuilder();
                for (Edge e : uploadedGraph.getEdgesOfNode(s)) nb.append(e.getDestination().getId()).append(" ");
                setStatus("No edge " + s + "-" + t + ". Node " + s + " connects to: [" + nb.toString().trim() + "]"); return;
            }
            double cur = 0;
            for (Edge e : uploadedGraph.getEdgesOfNode(s)) if (e.getDestination().getId() == t) { cur = e.getWeight(); break; }
            if (cur == 0) for (Edge e : uploadedGraph.getEdgesOfNode(t)) if (e.getDestination().getId() == s) { cur = e.getWeight(); break; }
            TextInputDialog dlg = new TextInputDialog(String.format("%.4f", cur));
            dlg.setTitle("Update Edge"); dlg.setHeaderText("Edge " + s + "-" + t + " current weight: " + String.format("%.4f", cur));
            dlg.setContentText("Enter new weight:");
            java.util.Optional<String> res = dlg.showAndWait();
            if (res.isEmpty() || res.get().trim().isEmpty()) return;
            double nw = Double.parseDouble(res.get().trim());
            boolean ok = uploadedGraph.updateEdge(s, t, nw) | uploadedGraph.updateEdge(t, s, nw);
            graphVisualizer.setGraph(uploadedGraph);
            setStatus(ok ? "UPDATE OK: Edge " + s + "-" + t + " weight: " + String.format("%.4f", nw)
                         : "UPDATE ISSUE: Edge " + s + "-" + t);
            afterCrudChange();
        } catch (NumberFormatException ex) { setStatus("Invalid input. Enter a valid number."); }
        catch (Exception ex) { setStatus("Error: " + ex.getMessage()); }
    }
    private void onLoadDataset() {
        String path = null;
        File auto = new File("dataset");
        if (!auto.exists()) auto = new File(System.getProperty("user.dir"), "dataset");
        if (auto.exists() && auto.isDirectory()) { path = auto.getAbsolutePath(); }
        else {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Dataset Folder (contains Real/ and Fake/)");
            File chosen = dc.showDialog(stage);
            if (chosen == null) return;
            path = chosen.getAbsolutePath();
        }
        final String dp = path;
        btnLoad.setDisable(true); progressBar.setProgress(-1); progressBar.setVisible(true);
        setStatus("Loading dataset from: " + dp);
        Task<Void> task = new Task<>() {
            protected Void call() throws Exception {
                database.load(dp, new Database.LoadProgressListener() {
                    public void onImageLoaded(String n, boolean r, int t) { updateMessage("Loaded " + t + " images..."); }
                    public void onComplete(int r, int f) { updateMessage("Done: " + r + " real + " + f + " fake"); }
                });
                return null;
            }
        };
        statusLabel.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            progressBar.setVisible(false); btnLoad.setDisable(false);
            dbStatusLabel.setText("DB: " + database.totalCount() + " images (" + database.getRealCount() + "R/" + database.getFakeCount() + "F)");
            dbStatusLabel.setTextFill(Color.rgb(40, 160, 60));
            dbDot.setFill(Color.rgb(100, 200, 100));
            btnUpload.setDisable(false);
            setStatus("Dataset loaded. Upload an image to analyse.");
        });
        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            progressBar.setVisible(false); btnLoad.setDisable(false);
            Throwable ex = task.getException();
            setStatus("Load failed: " + (ex != null ? ex.getMessage() : "unknown"));
        });
        new Thread(task).start();
    }
    private void onUploadImage() {
        File datasetDir = new File("dataset");
        if (!datasetDir.exists()) datasetDir = new File(System.getProperty("user.dir"), "dataset");
        File file;
        if (datasetDir.exists() && datasetDir.isDirectory()) {
            file = ImagePickerDialog.showDialog(stage, datasetDir.getAbsolutePath());
        } else {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Medicine Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp"));
            file = fc.showOpenDialog(stage);
        }
        if (file == null) return;
        final File sel = file;
        progressBar.setProgress(-1); progressBar.setVisible(true);
        setStatus("Processing " + sel.getName() + "...");
        resultPanel.clearResults(); graphVisualizer.setGraph(null); imagePanel.clearImage();
        Task<Void> task = new Task<>() {
            BufferedImage img; Graph g; double[] fv;
            protected Void call() throws Exception {
                img = ImageIO.read(sel); if (img == null) throw new Exception("Cannot read image");
                g = ImageToGraph.convert(img, currentK); fv = FeatureExtractor.extract(img); return null;
            }
            protected void succeeded() {
                progressBar.setVisible(false);
                uploadedImage = img; uploadedGraph = g; uploadedFeatureVector = fv;
                imagePanel.setImage(img, sel.getName()); graphVisualizer.setGraph(g);
                setCrudEnabled(true); btnAnalyse.setDisable(false); btnPath.setDisable(false); btnClear.setDisable(false);
                setStatus("Loaded: " + g.nodeCount() + " nodes, " + g.edgeCount() + " edges, k=" + currentK);
            }
            protected void failed() {
                progressBar.setVisible(false);
                Throwable ex = getException();
                setStatus("Failed: " + (ex != null ? ex.getMessage() : "unknown"));
            }
        };
        new Thread(task).start();
    }
    private void onAnalyse() {
        if (uploadedGraph == null || !database.isLoaded()) return;
        btnAnalyse.setDisable(true); progressBar.setProgress(-1); progressBar.setVisible(true);
        setStatus("Analysing...");
        Task<Void> task = new Task<>() {
            ClassificationResult cr; double simScore;
            List<MedicineRecord> topMatches = new ArrayList<>();
            protected Void call() {
                List<double[]> scores = new ArrayList<>();
                List<MedicineRecord> all = database.getAllRecords();
                for (int i = 0; i < all.size(); i++) {
                    double sim = SimilarityDetection.computeSimilarity(uploadedGraph, all.get(i).getGraph());
                    scores.add(new double[]{sim, i});
                }
                scores.sort((a, b) -> Double.compare(b[0], a[0]));
                simScore = scores.isEmpty() ? 0 : scores.get(0)[0];
                for (int i = 0; i < Math.min(5, scores.size()); i++) topMatches.add(all.get((int) scores.get(i)[1]));
                cr = Classification.classify(uploadedGraph, database);
                return null;
            }
            protected void succeeded() {
                progressBar.setVisible(false); btnAnalyse.setDisable(false);
                lastResult = cr;
                resultPanel.showResultWithImages(cr, simScore, topMatches, uploadedGraph);
                setStatus("Analysis complete.");
            }
            protected void failed() {
                progressBar.setVisible(false); btnAnalyse.setDisable(false);
                setStatus("Analysis failed.");
            }
        };
        new Thread(task).start();
    }
    private void onFindPath() {
        if (uploadedGraph == null) return;
        int s = graphVisualizer.getSelectedSourceId(), t = graphVisualizer.getSelectedTargetId();
        if (s < 0 || t < 0) {
            List<Node> all = uploadedGraph.getAllNodes();
            if (all.size() < 2) { setStatus("Need at least 2 nodes for pathfinding"); return; }
            if (s < 0) s = all.get(0).getId();
            if (t < 0) t = all.get(all.size() - 1).getId();
        }
        if (!uploadedGraph.containsNode(s) || !uploadedGraph.containsNode(t)) {
            setStatus("Invalid nodes. Left-click source, right-click target first."); return;
        }
        Dijkstra.ShortestPathResult r = Dijkstra.findShortestPath(uploadedGraph, s, t);
        if (r.pathFound()) { graphVisualizer.highlightPath(r); setStatus("Path " + s + "->" + t + ": " + r.toReadableString()); }
        else setStatus("No path found between " + s + " and " + t);
    }
    private void onClear() {
        uploadedImage = null; uploadedGraph = null; uploadedFeatureVector = null; lastResult = null;
        imagePanel.clearImage(); graphVisualizer.setGraph(null); resultPanel.clearResults();
        setCrudEnabled(false); btnAnalyse.setDisable(true); btnPath.setDisable(true); btnClear.setDisable(true);
        setStatus("Cleared.");
    }
    private void afterCrudChange() {
        if (database != null && database.isLoaded() && uploadedGraph != null && uploadedFeatureVector != null) onAnalyse();
    }
    private void rebuildGraph() {
        if (uploadedImage == null) return;
        setStatus("Rebuilding k=" + currentK + "...");
        Task<Void> task = new Task<>() {
            Graph ng;
            protected Void call() { ng = ImageToGraph.convert(uploadedImage, currentK); return null; }
            protected void succeeded() {
                uploadedGraph = ng; graphVisualizer.setGraph(ng);
                setStatus("Rebuilt: " + ng.nodeCount() + " nodes, " + ng.edgeCount() + " edges, k=" + currentK);
            }
            protected void failed() { setStatus("Rebuild failed."); }
        };
        new Thread(task).start();
    }
    private void setCrudEnabled(boolean e) {
        btnAddNode.setDisable(!e); btnRemoveNode.setDisable(!e); btnUpdateNode.setDisable(!e);
        btnRemoveEdge.setDisable(!e); btnUpdateEdge.setDisable(!e);
    }
    private void setStatus(String msg) { Platform.runLater(() -> statusLabel.setText(msg)); }
    private Label lbl(String text, int size) {
        Label l = new Label(text); l.setFont(Font.font("SansSerif", size)); l.setTextFill(TEXT_MAIN); return l;
    }
    private Button createRoundedBtn(String text) {
        Button b = new Button(text);
        String norm = "-fx-background-color: white; -fx-border-color: rgb(220,220,220); -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: rgb(30,30,30); -fx-font-size: 15px; -fx-cursor: hand; -fx-pref-height: 44px;";
        String hvr  = "-fx-background-color: rgb(245,245,245); -fx-border-color: rgb(220,220,220); -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: rgb(30,30,30); -fx-font-size: 15px; -fx-cursor: hand; -fx-pref-height: 44px;";
        String pr   = "-fx-background-color: rgb(235,235,235); -fx-border-color: rgb(220,220,220); -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: rgb(30,30,30); -fx-font-size: 15px; -fx-cursor: hand; -fx-pref-height: 44px;";
        b.setStyle(norm);
        b.setOnMouseEntered(e -> b.setStyle(hvr));
        b.setOnMouseExited(e -> b.setStyle(norm));
        b.setOnMousePressed(e -> b.setStyle(pr));
        b.setOnMouseReleased(e -> b.setStyle(hvr));
        b.setPrefWidth(100);
        return b;
    }
    private Label createBadge(String num) {
        Label l = new Label(num);
        l.setPrefSize(24, 24); l.setAlignment(Pos.CENTER);
        l.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        l.setTextFill(TEXT_MUTED);
        l.setStyle("-fx-background-color: rgb(240,240,240); -fx-background-radius: 4;");
        return l;
    }
    private VBox roundedCol() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color: white; -fx-border-color: rgb(220,220,220); -fx-border-radius: 8; -fx-background-radius: 8;");
        return v;
    }
}