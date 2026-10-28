package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;

import java.awt.image.BufferedImage;

// shows the uploaded image on a canvas that resizes with the panel
// when no image is loaded it shows a placeholder message
public class ImagePanel extends Pane {

    private static final Color BG_EMPTY  = Color.rgb(245, 245, 248);
    private static final Color BG_LOADED = Color.WHITE;

    private final Canvas canvas = new Canvas();
    private Image   fxImage;
    private String  imageLabel;

    public ImagePanel() {
        getChildren().add(canvas);

        // Bind canvas dimensions to the pane so it fills its allocated space
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        // Redraw whenever size changes
        canvas.widthProperty().addListener(o  -> redraw());
        canvas.heightProperty().addListener(o -> redraw());

        setStyle("-fx-background-color: rgb(245,245,248);");
    }

    // public methods

    public void setImage(BufferedImage img, String label) {
        this.fxImage     = (img != null) ? toFXImage(img) : null;
        this.imageLabel  = label;
        setStyle(fxImage != null
                ? "-fx-background-color: white;"
                : "-fx-background-color: rgb(245,245,248);");
        redraw();
    }

    public void clearImage() {
        setImage(null, null);
    }

    // drawing logic

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double pw = canvas.getWidth();
        double ph = canvas.getHeight();

        gc.clearRect(0, 0, pw, ph);

        if (fxImage == null) {
            drawPlaceholder(gc, pw, ph);
            return;
        }

        // Centre-fit scaling (same logic as original Swing paintComponent)
        double imgW  = fxImage.getWidth();
        double imgH  = fxImage.getHeight();
        double labelH = (imageLabel != null) ? 22 : 0;
        double availH = ph - labelH - 8;
        double scale  = Math.min(pw / imgW, availH / imgH);
        double drawW  = imgW * scale;
        double drawH  = imgH * scale;
        double drawX  = (pw - drawW) / 2;
        double drawY  = (ph - labelH - drawH) / 2;

        gc.setImageSmoothing(true);
        gc.drawImage(fxImage, drawX, drawY, drawW, drawH);

        if (imageLabel != null) {
            // Semi-transparent footer bar
            gc.setFill(Color.rgb(255, 255, 255, 0.78));
            gc.fillRect(0, ph - labelH, pw, labelH);

            gc.setFill(Color.rgb(40, 40, 40));
            gc.setFont(Font.font("SansSerif", 11));
            gc.setFontSmoothingType(FontSmoothingType.LCD);

            String txt = imageLabel.length() > 45
                    ? "..." + imageLabel.substring(imageLabel.length() - 42)
                    : imageLabel;

            // approximate center - JavaFX doesn't give us exact text width easily
            double textX = Math.max(4, (pw - txt.length() * 6.2) / 2);
            gc.fillText(txt, textX, ph - 5);
        }
    }

    private void drawPlaceholder(GraphicsContext gc, double pw, double ph) {
        gc.setFill(BG_EMPTY);
        gc.fillRect(0, 0, pw, ph);
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setFont(Font.font("SansSerif", 15));
        String msg = "No image loaded";
        double textX = Math.max(4, (pw - msg.length() * 7.5) / 2);
        gc.fillText(msg, textX, ph / 2);
    }

    // converts a BufferedImage to a JavaFX Image without needing the javafx.swing module
    static Image toFXImage(BufferedImage bi) {
        if (bi == null) return null;
        int w = bi.getWidth();
        int h = bi.getHeight();
        WritableImage wi = new WritableImage(w, h);
        PixelWriter pw   = wi.getPixelWriter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pw.setArgb(x, y, bi.getRGB(x, y));
            }
        }
        return wi;
    }
}