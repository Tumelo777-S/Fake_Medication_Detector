package main;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.LandingPage;
import ui.MainWindow;


// entry point for the application
// shows the landing page first and then opens the main window when the user clicks start
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // use the primary stage for the landing page
        LandingPage landing = new LandingPage(primaryStage);

        landing.setOnStartListener(() -> {
            landing.close();
            MainWindow mainWindow = new MainWindow();
            mainWindow.show();
        });

        landing.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}