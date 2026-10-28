package ui;

import algorithms.Classification.ClassificationResult;
import model.MedicineRecord;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

// shows the classification result, similarity score, and top matching images
public class ResultPanel extends VBox {

    // -- Same colour constants as the Swing version --
    private static final Color AUTH_CLR  = Color.rgb(40,  160, 60);
    private static final Color FAKE_CLR  = Color.rgb(200, 50,  40);
    private static final Color NEUTRAL   = Color.rgb(150, 150, 150);
    private static final Color BG        = Color.WHITE;
    private static final Color PANEL_BG  = Color.rgb(248, 248, 248);
    private static final Color TEXT_CLR  = Color.BLACK;
    private static final Color DIM_CLR   = Color.rgb(110, 110, 110);

    // -- Components --
    private Label       verdictLabel;
    private Label       confidenceLabel;
    private Label       similarityLabel;
    private ProgressBar similarityBar;
    private Label       statsLabel;
    private VBox        matchesPanel;

    public ResultPanel() {
        setBackground(new Background(new BackgroundFill(BG, null, null)));
        setSpacing(6);
        setPadding(new Insets(8));
        buildUI();
    }

    // set up all the UI sections
    private void buildUI() {
        // verdict section at the top

        VBox vPanel = styledSection();
        Label vTitle = lbl("DETECTION RESULT", FontWeight.BOLD, 10, DIM_CLR);
        verdictLabel    = lbl("No Result Yet", FontWeight.BOLD, 24, NEUTRAL);
        confidenceLabel = lbl("Confidence: --", FontWeight.NORMAL, 11, DIM_CLR);
        vPanel.getChildren().addAll(vTitle, verdictLabel, confidenceLabel);

        // similarity score and stats in the middle

        VBox mPanel = styledSection();
        Label sTitle = lbl("SIMILARITY", FontWeight.BOLD, 10, DIM_CLR);
        similarityLabel = lbl("--", FontWeight.BOLD, 16, TEXT_CLR);

        similarityBar = new ProgressBar(0);
        similarityBar.setMaxWidth(Double.MAX_VALUE);
        similarityBar.setStyle("-fx-accent: rgb(40,160,60);");

        statsLabel = lbl("", FontWeight.NORMAL, 10, DIM_CLR);
        mPanel.getChildren().addAll(sTitle, similarityLabel, similarityBar, statsLabel);

        // bottom section shows the top matching records with thumbnails

        VBox bPanel = styledSection();
        VBox.setVgrow(bPanel, Priority.ALWAYS);
        Label mTitle = lbl("TOP MATCHES", FontWeight.BOLD, 10, DIM_CLR);
        mTitle.setPadding(new Insets(0, 0, 4, 0));

        matchesPanel = new VBox();
        matchesPanel.setBackground(new Background(new BackgroundFill(PANEL_BG, null, null)));
        Label placeholder = lbl("Run analysis to see matches",
                FontWeight.NORMAL, 11, DIM_CLR, FontPosture.ITALIC);
        matchesPanel.getChildren().add(placeholder);

        ScrollPane scroll = new ScrollPane(matchesPanel);
        scroll.setBorder(Border.EMPTY);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setBackground(new Background(new BackgroundFill(PANEL_BG, null, null)));
        VBox.setVgrow(scroll, Priority.ALWAYS);

        bPanel.getChildren().addAll(mTitle, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Combine top two sections
        VBox top = new VBox(6);
        top.setBackground(new Background(new BackgroundFill(BG, null, null)));
        top.getChildren().addAll(vPanel, mPanel);

        getChildren().addAll(top, bPanel);
        VBox.setVgrow(bPanel, Priority.ALWAYS);
    }

    // public methods

    /**
     * shows the result with thumbnail images of the top matches
     *
     * @param result the classification result from the algorithm
     * @param simScore the similarity score (0.0 to 1.0)
     * @param topMatches list of the closest matching records
     * @param graph the graph that was analysed (used for stats)
     */
    public void showResultWithImages(ClassificationResult result,
                                     double simScore,
                                     List<MedicineRecord> topMatches,
                                     graph.Graph graph) {
        // -- Verdict --
        boolean auth = "AUTHENTIC".equals(result.label);
        verdictLabel.setText(result.label);
        verdictLabel.setTextFill(auth ? AUTH_CLR : FAKE_CLR);

        // -- Confidence --
        confidenceLabel.setText("Confidence: " + String.format("%.0f%%", result.confidence * 100)
                + "    Best: " + result.bestMatchName);

        // -- Similarity --
        int pct = (int) Math.round(simScore * 100);
        similarityLabel.setText(String.format("%.4f  (%d%%)", simScore, pct));
        similarityBar.setProgress(pct / 100.0);

        String barStyle;
        if (pct >= 60)      barStyle = "-fx-accent: rgb(40,160,60);";
        else if (pct >= 35) barStyle = "-fx-accent: rgb(200,180,60);";
        else                barStyle = "-fx-accent: rgb(200,50,40);";
        similarityBar.setStyle(barStyle);

        // -- Stats --
        if (graph != null) statsLabel.setText("");

        // -- Top matches with THUMBNAILS --
        matchesPanel.getChildren().clear();

        for (int i = 0; i < topMatches.size(); i++) {
            MedicineRecord rec = topMatches.get(i);
            boolean even = (i % 2 == 0);
            Color rowBg = even ? PANEL_BG : Color.rgb(240, 240, 240);

            HBox row = new HBox(8);
            row.setBackground(new Background(new BackgroundFill(rowBg, null, null)));
            row.setPadding(new Insets(6));

            // Thumbnail
            ImageView imgView = new ImageView();
            imgView.setFitWidth(60);
            imgView.setFitHeight(60);
            imgView.setPreserveRatio(true);
            imgView.setSmooth(true);
            try (FileInputStream fis = new FileInputStream(new File(rec.getImagePath()))) {
                imgView.setImage(new javafx.scene.image.Image(fis, 60, 60, true, true));
            } catch (Exception ex) {
                // leave image blank on error
            }

            // Info text
            String labelTxt = rec.isReal() ? "REAL" : "FAKE";
            Color  labelClr = rec.isReal() ? AUTH_CLR : FAKE_CLR;

            String simStr = (i < result.topKDetails.size()) ? result.topKDetails.get(i) : "";

            VBox textPanel = new VBox(2);
            textPanel.setBackground(new Background(new BackgroundFill(rowBg, null, null)));

            Label rankLbl = lbl("#" + (i + 1) + "  " + rec.getName(), FontWeight.BOLD,   11, TEXT_CLR);
            Label typeLbl = lbl(labelTxt,                               FontWeight.BOLD,   11, labelClr);
            Label detLbl  = monoLbl(simStr, 9, DIM_CLR);

            textPanel.getChildren().addAll(rankLbl, typeLbl, detLbl);
            HBox.setHgrow(textPanel, Priority.ALWAYS);

            row.getChildren().addAll(imgView, textPanel);
            matchesPanel.getChildren().add(row);
        }
    }

    // text-only fallback in case no images are available
    public void showResult(ClassificationResult result, double simScore) {
        boolean auth = "AUTHENTIC".equals(result.label);
        verdictLabel.setText(result.label);
        verdictLabel.setTextFill(auth ? AUTH_CLR : FAKE_CLR);
        confidenceLabel.setText("Confidence: " + String.format("%.0f%%", result.confidence * 100));
        int pct = (int) Math.round(simScore * 100);
        similarityLabel.setText(String.format("%.4f  (%d%%)", simScore, pct));
        similarityBar.setProgress(pct / 100.0);

        matchesPanel.getChildren().clear();
        for (String detail : result.topKDetails) {
            Label l = monoLbl(detail, 10, TEXT_CLR);
            l.setPadding(new Insets(2, 6, 2, 6));
            matchesPanel.getChildren().add(l);
        }
    }

    public void clearResults() {
        verdictLabel.setText("No Result Yet");
        verdictLabel.setTextFill(NEUTRAL);
        confidenceLabel.setText("Confidence: --");
        similarityLabel.setText("--");
        similarityBar.setProgress(0);
        statsLabel.setText("");
        matchesPanel.getChildren().clear();
        matchesPanel.getChildren().add(
                lbl("Run analysis to see matches", FontWeight.NORMAL, 11, DIM_CLR, FontPosture.ITALIC));
    }

    // helper methods for building styled labels and panels
    private VBox styledSection() {
        VBox p = new VBox(2);
        p.setBackground(new Background(new BackgroundFill(PANEL_BG, null, null)));
        p.setBorder(new Border(new BorderStroke(
                Color.rgb(210, 210, 210), BorderStrokeStyle.SOLID,
                new CornerRadii(4), BorderWidths.DEFAULT)));
        p.setPadding(new Insets(8, 10, 8, 10));
        return p;
    }

    private Label lbl(String text, FontWeight weight, int size, Color clr) {
        Label l = new Label(text);
        l.setFont(Font.font("SansSerif", weight, size));
        l.setTextFill(clr);
        return l;
    }

    private Label lbl(String text, FontWeight weight, int size, Color clr, FontPosture posture) {
        Label l = new Label(text);
        l.setFont(Font.font("SansSerif", weight, posture, size));
        l.setTextFill(clr);
        return l;
    }

    private Label monoLbl(String text, int size, Color clr) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospaced", size));
        l.setTextFill(clr);
        return l;
    }
}