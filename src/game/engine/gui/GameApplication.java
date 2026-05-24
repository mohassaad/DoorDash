package game.engine.gui;

import game.engine.Game;
import game.engine.Role;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApplication extends Application {
    
    private static Game game;
    private static Role selectedRole;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));	// lazem loader fo 
        Parent root = loader.load();
        
        GameBoardController controller = loader.getController();
        controller.initializeGame(game);
        
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("DooR Dash - Scare vs Laugh Touchdown");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    public static void setSelectedRole(Role role) {
        selectedRole = role;
        try {
            game = new Game(selectedRole);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}