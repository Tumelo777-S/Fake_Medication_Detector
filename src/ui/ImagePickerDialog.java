package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

// shows a dialog where the user can pick an image from the dataset
// has two tabs for Real and Fake images with thumbnail previews
public class ImagePickerDialog extends Stage {

    private static final int   THUMB_SIZE = 90;
    private static final Color BG       = Color.WHITE;
    private static final Color SELECTED  = Color.rgb(40, 160, 60);
    private static final Color HOVER_BG  = Color.rgb(220, 220, 220);
    private static final Color BORDER    = Color.rgb(200, 200, 200);

    private File selectedFile = null;

    public ImagePickerDialog(Window owner, String datasetPath) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Select Medicine Image");
        setWidth(720);
        setHeight(520);
        centerOnScreen();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BG, null, null)));

        // Title
        Label title = new Label("  Select an image to analyse");
        title.setFont(Font.font("SansSerif", javafx.scene.text.FontWeight.BOLD, 14));
        title.setTextFill(Color.BLACK);
        title.setPadding(new Insets(10, 10, 6, 10));
        title.setBackground(new Background(new BackgroundFill(BG, null, null)));
        root.setTop(title);

        // --- Tabs ---
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: white;");
        tabs.setTabMinWidth(80);

        File realDir = new File(datasetPath, "Real");
        File fakeDir = new File(datasetPath, "Fake");

        if (!realDir.exists()) realDir = new File(datasetPath, "real");
        if (!fakeDir.exists()) fakeDir = new File(datasetPath, "fake");

        if (realDir.exists() && realDir.isDirectory()) {
            Tab t = new Tab("Real", buildImageGrid(realDir));
            tabs.getTabs().add(t);
        }
        if (fakeDir.exists() && fakeDir.isDirectory()) {
            Tab t = new Tab("Fake", buildImageGrid(fakeDir));
            tabs.getTabs().add(t);
        }
        if (tabs.getTabs().isEmpty()) {
            tabs.getTabs().add(new Tab("All Images", buildImageGrid(new File(datasetPath))));
        }
        root.setCenter(tabs);

        // --- Bottom buttons ---
        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(8, 10, 8, 10));
        bottom.setBackground(new Background(new BackgroundFill(Color.rgb(245, 245, 245), null, null)));
        bottom.setBorder(new Border(new BorderStroke(
                BORDER, BorderStrokeStyle.SOLID, null,
                new BorderWidths(1, 0, 0, 0))));

        Button btnBrowse = new Button("Browse Other...");
        btnBrowse.setOnAction(e -> browseOther());
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e -> { selectedFile = null; close(); });

        bottom.getChildren().addAll(btnBrowse, btnCancel);
        root.setBottom(bottom);

        setScene(new Scene(root));
    }

    // builds a scrollable grid of image thumbnails from the given folder
    private ScrollPane buildImageGrid(File folder) {
        FlowPane grid = new FlowPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        grid.setBackground(new Background(new BackgroundFill(BG, null, null)));

        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".bmp");
        });

        if (files == null || files.length == 0) {
            grid.getChildren().add(new Label("No images found in " + folder.getName()));
            ScrollPane sp = new ScrollPane(grid);
            sp.setBorder(Border.EMPTY);
            return sp;
        }

        for (File imgFile : files) {
            grid.getChildren().add(createThumbnail(imgFile));
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setBorder(Border.EMPTY);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    // creates a single thumbnail card for one image file
    private VBox createThumbnail(File imgFile) {
        VBox panel = new VBox(2);
        panel.setPrefSize(THUMB_SIZE + 10, THUMB_SIZE + 25);
        panel.setMaxSize(THUMB_SIZE + 10, THUMB_SIZE + 25);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(4));
        panel.setBackground(new Background(new BackgroundFill(BG, new CornerRadii(4), null)));
        panel.setBorder(new Border(new BorderStroke(
                BORDER, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        panel.setCursor(javafx.scene.Cursor.HAND);

        // Thumbnail image
        ImageView imgView = new ImageView();
        imgView.setFitWidth(THUMB_SIZE);
        imgView.setFitHeight(THUMB_SIZE);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);
        try (FileInputStream fis = new FileInputStream(imgFile)) {
            imgView.setImage(new Image(fis, THUMB_SIZE, THUMB_SIZE, true, false));
        } catch (Exception ex) {
            // leave blank
        }

        // File name label
        String name = imgFile.getName();
        if (name.length() > 12) name = name.substring(0, 10) + "..";
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("SansSerif", 9));
        nameLabel.setTextFill(Color.rgb(80, 80, 80));
        nameLabel.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(imgView, nameLabel);

        // Hover + click
        panel.setOnMouseEntered(e -> {
            panel.setBackground(new Background(new BackgroundFill(HOVER_BG, new CornerRadii(4), null)));
            panel.setBorder(new Border(new BorderStroke(
                    SELECTED, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2))));
        });
        panel.setOnMouseExited(e -> {
            panel.setBackground(new Background(new BackgroundFill(BG, new CornerRadii(4), null)));
            panel.setBorder(new Border(new BorderStroke(
                    BORDER, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        });
        panel.setOnMouseClicked(e -> {
            selectedFile = imgFile;
            close();
        });

        return panel;
    }

    // opens a file chooser so the user can pick from somewhere else on the computer
    private void browseOther() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Medicine Image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.bmp"));
        File chosen = fc.showOpenDialog(this);
        if (chosen != null) {
            selectedFile = chosen;
            close();
        }
    }

    // returns whichever file the user clicked, or null if they cancelled
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * shows the dialog and waits for the user to pick or cancel
     *
     * @param parent the parent window
     * @param datasetPath path to the dataset folder
     * @return the selected file, or null if the user cancelled
     */
    public static File showDialog(Window parent, String datasetPath) {
        ImagePickerDialog dlg = new ImagePickerDialog(parent, datasetPath);
        dlg.showAndWait();
        return dlg.getSelectedFile();
    }
}
