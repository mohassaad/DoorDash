package game.engine.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        SoundManager.init();

        Parent root = FXMLLoader.load(getClass().getResource("StartScreen.fxml"));
        Scene scene = new Scene(root, 1280, 720);

        stage.setTitle("DooR DasH — Scarer vs Laugher");
        stage.setScene(scene);

        // StartScreen: NOT full screen
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.centerOnScreen();

        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}