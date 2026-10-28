package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;

// the first screen the user sees when they open the app
// shows the logo, a description, a start button, and a medicine image on the right
public class LandingPage {

    // window layout sizes
    private static final int LEFT_WIDTH  = 520;
    private static final int IMG_SIZE    = 260;
    private static final int WIN_WIDTH   = LEFT_WIDTH + IMG_SIZE;
    private static final int WIN_HEIGHT  = 600;

    private final Stage stage;
    private Button startButton;
    private Runnable onStartListener;

    public LandingPage(Stage stage) {
        this.stage = stage;
        stage.setTitle("Fake Medicine Detection - Welcome");
        stage.setResizable(false);

        AnchorPane root = buildRoot();
        Scene scene = new Scene(root, WIN_WIDTH, WIN_HEIGHT);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    // builds the main layout with all the labels, buttons and image panel
    private AnchorPane buildRoot() {
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: white;");
        root.setPrefSize(WIN_WIDTH, WIN_HEIGHT);

        // left side

        // Logo (top-left, 20,25, 80×80)
        javafx.scene.Node logoNode = createLogoNode();
        AnchorPane.setLeftAnchor(logoNode, 20.0);
        AnchorPane.setTopAnchor(logoNode,  25.0);

        // Main heading (110,32, 390×45)
        Label heading = new Label("Detect Fake Medicines");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        heading.setTextFill(Color.rgb(50, 50, 50));
        heading.setPrefSize(390, 45);
        AnchorPane.setLeftAnchor(heading, 110.0);
        AnchorPane.setTopAnchor(heading,   32.0);

        // Subheading (centred in left strip, y=115)
        Label subheading = new Label("Quick and Easy");
        subheading.setFont(Font.font("Segoe UI", 13));
        subheading.setTextFill(Color.rgb(120, 120, 120));
        subheading.setAlignment(Pos.CENTER);
        subheading.setPrefWidth(LEFT_WIDTH - 40);
        AnchorPane.setLeftAnchor(subheading, 20.0);
        AnchorPane.setTopAnchor(subheading, 115.0);

        // Divider 1 (60,145, 400×2)
        Pane divider1 = divider();
        AnchorPane.setLeftAnchor(divider1, 60.0);
        AnchorPane.setTopAnchor(divider1, 145.0);

        // Description (20,158, 480×60)
        Label desc1 = new Label(
                "Upload a medicine image and instantly check if it is real or fake\n" +
                "in seconds using our advanced algorithm-powered verification technology.");
        desc1.setFont(Font.font("Segoe UI", 12));
        desc1.setTextFill(Color.rgb(100, 100, 100));
        desc1.setAlignment(Pos.CENTER);
        desc1.setWrapText(true);
        desc1.setPrefSize(LEFT_WIDTH - 40, 60);
        AnchorPane.setLeftAnchor(desc1, 20.0);
        AnchorPane.setTopAnchor(desc1, 158.0);

        // Feature cards (20,235, LEFT_WIDTH-40, 110)
        HBox featuresPanel = createFeaturesPanel();
        featuresPanel.setPrefWidth(LEFT_WIDTH - 40);
        AnchorPane.setLeftAnchor(featuresPanel, 20.0);
        AnchorPane.setTopAnchor(featuresPanel, 235.0);

        // Divider 2 (60,358)
        Pane divider2 = divider();
        AnchorPane.setLeftAnchor(divider2, 60.0);
        AnchorPane.setTopAnchor(divider2, 358.0);

        // Protect text (20,368)
        Label desc2 = new Label(
                "Protect yourself and your family by checking if your medicine is real and safe.");
        desc2.setFont(Font.font("Segoe UI", 11));
        desc2.setTextFill(Color.rgb(120, 120, 120));
        desc2.setAlignment(Pos.CENTER);
        desc2.setWrapText(true);
        desc2.setPrefSize(LEFT_WIDTH - 40, 40);
        AnchorPane.setLeftAnchor(desc2, 20.0);
        AnchorPane.setTopAnchor(desc2, 368.0);

        // START button (80,440, LEFT_WIDTH-160, 48)
        startButton = createStartButton();
        startButton.setPrefSize(LEFT_WIDTH - 160, 48);
        AnchorPane.setLeftAnchor(startButton, 80.0);
        AnchorPane.setTopAnchor(startButton, 440.0);

        // Footer hint (20,494)
        Label footer = new Label("Click START to begin");
        footer.setFont(Font.font("Segoe UI", javafx.scene.text.FontPosture.ITALIC, 11));
        footer.setTextFill(Color.rgb(160, 160, 160));
        footer.setAlignment(Pos.CENTER);
        footer.setPrefWidth(LEFT_WIDTH - 40);
        AnchorPane.setLeftAnchor(footer, 20.0);
        AnchorPane.setTopAnchor(footer, 494.0);

        // right side - the medicine pill image
        Canvas imageCanvas = createImageCanvas();
        AnchorPane.setLeftAnchor(imageCanvas, (double) LEFT_WIDTH);
        AnchorPane.setTopAnchor(imageCanvas, 0.0);

        root.getChildren().addAll(
                logoNode, heading, subheading,
                divider1, desc1, featuresPanel,
                divider2, desc2,
                startButton, footer,
                imageCanvas
        );
        return root;
    }

    // creates and draws the image canvas on the right side of the screen
    private Canvas createImageCanvas() {
        Canvas canvas = new Canvas(IMG_SIZE, WIN_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Image img = null;
        try (FileInputStream fis = new FileInputStream(new File("Logo\\Image Landing Page.jpeg"))) {
            img = new Image(fis);
        } catch (Exception ex) {
            System.err.println("Landing page image not found: " + ex.getMessage());
        }

        if (img != null) {
            // Centre-crop source to square then fill panel (same as Swing paintComponent)
            double srcW = img.getWidth();
            double srcH = img.getHeight();
            double crop = Math.min(srcW, srcH);
            double sx   = (srcW - crop) / 2;
            double sy   = (srcH - crop) / 2;

            gc.setImageSmoothing(true);
            gc.drawImage(img,
                    sx, sy, crop, crop,       // source rect (square crop)
                    0, 0, IMG_SIZE, WIN_HEIGHT // dest rect (fills canvas)
            );
        } else {
            // Fallback: teal placeholder
            gc.setFill(Color.rgb(62, 207, 178));
            gc.fillRect(0, 0, IMG_SIZE, WIN_HEIGHT);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            String msg = "Image not found";
            gc.fillText(msg, (IMG_SIZE - msg.length() * 7.0) / 2, WIN_HEIGHT / 2.0);
        }

        // Thin left border
        gc.setStroke(Color.rgb(220, 220, 220));
        gc.setLineWidth(1);
        gc.strokeLine(0, 0, 0, WIN_HEIGHT);

        return canvas;
    }

    // loads and returns the logo image, falls back to a text label if image not found
    private javafx.scene.Node createLogoNode() {
        try (FileInputStream fis = new FileInputStream(new File("Logo/Logo.jpeg"))) {
            Image icon = new Image(fis, 80, 80, true, true);
            ImageView iv = new ImageView(icon);
            iv.setFitWidth(80);
            iv.setFitHeight(80);
            // Rounded border via StackPane wrapper
            StackPane wrapper = new StackPane(iv);
            wrapper.setPrefSize(80, 80);
            wrapper.setStyle("-fx-border-color: rgb(220,220,220); " +
                             "-fx-border-radius: 8; " +
                             "-fx-background-radius: 8;");
            return wrapper;
        } catch (Exception e) {
            // Fallback label
            Label logo = new Label("LS");
            logo.setPrefSize(80, 80);
            logo.setAlignment(Pos.CENTER);
            logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            logo.setTextFill(Color.rgb(62, 207, 178));
            logo.setStyle("-fx-background-color: rgb(26,42,58); " +
                          "-fx-border-color: rgb(220,220,220); " +
                          "-fx-border-radius: 8; " +
                          "-fx-background-radius: 8;");
            System.err.println("Logo not found. Using fallback.");
            return logo;
        }
    }

    // builds the row of three feature cards (Fast, Easy, Safe)
    private HBox createFeaturesPanel() {
        HBox panel = new HBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefHeight(110);
        panel.getChildren().addAll(
                createFeatureBox("Fast", "Get results\nin seconds"),
                createFeatureBox("Easy", "Just upload\na photo"),
                createFeatureBox("Safe", "Works\noffline")
        );
        HBox.setHgrow(panel.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(panel.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(panel.getChildren().get(2), Priority.ALWAYS);
        return panel;
    }

    private VBox createFeatureBox(String title, String description) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12, 8, 12, 8));
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setStyle("-fx-background-color: rgb(250,250,250); " +
                     "-fx-border-color: rgb(220,220,220); " +
                     "-fx-border-radius: 8; " +
                     "-fx-background-radius: 8;");

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titleLbl.setTextFill(Color.rgb(70, 70, 70));

        Label descLbl = new Label(description);
        descLbl.setFont(Font.font("Segoe UI", 11));
        descLbl.setTextFill(Color.rgb(130, 130, 130));
        descLbl.setAlignment(Pos.CENTER);
        descLbl.setWrapText(true);

        box.getChildren().addAll(titleLbl, descLbl);
        return box;
    }

    // start button styles for normal, hover, and pressed states
    private static final String BTN_NORMAL  = "-fx-background-color: rgb(160,160,160); " +
                                               "-fx-background-radius: 7; " +
                                               "-fx-text-fill: white; " +
                                               "-fx-font-family: 'Segoe UI'; " +
                                               "-fx-font-weight: bold; " +
                                               "-fx-font-size: 15px; " +
                                               "-fx-cursor: hand; " +
                                               "-fx-border-width: 0;";
    private static final String BTN_HOVER   = BTN_NORMAL.replace("rgb(160,160,160)", "rgb(130,130,130)");
    private static final String BTN_PRESSED = BTN_NORMAL.replace("rgb(160,160,160)", "rgb(100,100,100)");

    private Button createStartButton() {
        Button btn = new Button("START");
        btn.setStyle(BTN_NORMAL);
        btn.setOnMouseEntered(e  -> btn.setStyle(BTN_HOVER));
        btn.setOnMouseExited(e   -> btn.setStyle(BTN_NORMAL));
        btn.setOnMousePressed(e  -> btn.setStyle(BTN_PRESSED));
        btn.setOnMouseReleased(e -> btn.setStyle(BTN_HOVER));
        return btn;
    }

    // creates a thin horizontal line used as a section divider
    private Pane divider() {
        Pane d = new Pane();
        d.setPrefSize(LEFT_WIDTH - 120, 2);
        d.setBackground(new Background(new BackgroundFill(Color.rgb(230, 230, 230), null, null)));
        return d;
    }

    // public methods

    /**
     * registers the action that runs when the user clicks START
     *
     * @param listener a Runnable that will be called on click
     */
    public void setOnStartListener(Runnable listener) {
        this.onStartListener = listener;
        startButton.setOnAction(e -> listener.run());
    }

    public void show()  { stage.show(); }
    public void close() { stage.close(); }
}