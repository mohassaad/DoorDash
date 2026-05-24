package game.engine.gui;

import game.engine.Game;
import game.engine.Role;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StartScreenController {
	
	
// we can't have same name names in fxml
    @FXML private Button startButton;
    @FXML private Button optionsButton;
    @FXML private Button exitButton;
    @FXML private Button musicToggleButton;

    @FXML private StackPane sulleyContainer;
    @FXML private StackPane mikeContainer;
    @FXML private ImageView sulleyImage;
    @FXML private ImageView mikeImage;
    @FXML private DropShadow sulleyGlow;
    @FXML private DropShadow mikeGlow;
    @FXML private Label sulleyBadge;
    @FXML private Label mikeBadge;

    private Role selectedRole = null; // null = not selected yet
    
    private Label player1NamePlate;
    private Label player2NamePlate;

    @FXML
    private void initialize() {
        SoundManager.init(); // da bel sound manager 
        SoundManager.playMusicIfNeeded();

        setupMenuButtons();
        setupMusicToggle();
        setupMonsterSelection();
        animateMonsters();
        setupNamePlates();
    }
    
    private void setupNamePlates() {
        // Create name plates
        player1NamePlate = createNamePlate("PLAYER 1 (YOU)", Role.SCARER); // for plate name use methods
        player2NamePlate = createNamePlate("PLAYER 2 (OPPONENT)", Role.LAUGHER);
        
        // Initially invisible
        player1NamePlate.setVisible(false);
        player2NamePlate.setVisible(false);
        
        // Add to the StackPane containers
        sulleyContainer.getChildren().add(player1NamePlate);
        mikeContainer.getChildren().add(player2NamePlate);
        
        // Position them at the TOP of each monster container (above the image)
        StackPane.setAlignment(player1NamePlate, Pos.TOP_CENTER);
        StackPane.setAlignment(player2NamePlate, Pos.TOP_CENTER);
        
        // Add some top margin to position above the monster image
        player1NamePlate.setTranslateY(30);
        player2NamePlate.setTranslateY(20);
    }
    
    private Label createNamePlate(String text, Role role) {
        Label namePlate = new Label(text);
        namePlate.setFont(Font.font("Impact", 14)); // for sets e3mel 7agat gahza
        namePlate.setAlignment(Pos.CENTER);
        namePlate.setMaxWidth(180);
        namePlate.setMinWidth(160);
        namePlate.setPrefHeight(30);
        
        // Set colors based on role
        if (role == Role.SCARER) {
            namePlate.setStyle(
                "-fx-background-color: #3bb3ff;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 12 5 12;"
            );
        } else {
            namePlate.setStyle(
                "-fx-background-color: #cd5c5c;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 12 5 12;"
            );
        }
        
        return namePlate;
    }
    
    private void updateNamePlates() { // handle the two conditions i
        if (selectedRole == Role.SCARER) {
            // Player chose Scarer (Sulley)
            player1NamePlate.setText("PLAYER 1 (YOU)");
            player1NamePlate.setVisible(true);
            updateNamePlateColor(player1NamePlate, Role.SCARER);
            
            // Opponent is Laugher (Mike)
            player2NamePlate.setText("PLAYER 2 (OPPONENT)");
            player2NamePlate.setVisible(true);
            updateNamePlateColor(player2NamePlate, Role.LAUGHER);
            
        } else if (selectedRole == Role.LAUGHER) {
            // Player chose Laugher (Mike)
            player2NamePlate.setText("PLAYER 1 (YOU)");
            player2NamePlate.setVisible(true);
            updateNamePlateColor(player2NamePlate, Role.LAUGHER);
            
            // Opponent is Scarer (Sulley)
            player1NamePlate.setText("PLAYER 2 (OPPONENT)");
            player1NamePlate.setVisible(true);
            updateNamePlateColor(player1NamePlate, Role.SCARER);
        }
    }
    
    private void updateNamePlateColor(Label namePlate, Role role) {
        if (role == Role.SCARER) {
            namePlate.setStyle(
                "-fx-background-color: #3bb3ff;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 12 5 12;"
            );
        } else {
            namePlate.setStyle(
                "-fx-background-color: #cd5c5c;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 12 5 12;"
            );
        }
    }

    // ----------------------------------------------------------
    // MENU BUTTONS
    // ----------------------------------------------------------
    private void setupMenuButtons() {
        // START BUTTON
        startButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(startButton, 1.06);
            }
        });
        
        startButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(startButton, 1.0);
            }
        });
        
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SoundManager.playButtonClick();
                if (selectedRole == null) {
                    showInfo("Choose a Side First!",
                        "Please click on (SCARER) or (LAUGHER) before starting the game.");
                    return;
                }
                launchGame();
            }
        });
        
        // OPTIONS BUTTON
        optionsButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(optionsButton, 1.06);
            }
        });
        
        optionsButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(optionsButton, 1.0);
            }
        });
        
        optionsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SoundManager.playButtonClick();
                showGameInstructions();
            }
        });
        
        // EXIT BUTTON
        exitButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(exitButton, 1.06);
            }
        });
        
        exitButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(exitButton, 1.0);
            }
        });	
        
        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SoundManager.playButtonClick();
                Stage stage = (Stage) exitButton.getScene().getWindow();
                stage.close();
            }
        });
    }

    // ----------------------------------------------------------
    // GAME LAUNCHING
    // ----------------------------------------------------------
    private void launchGame() {
        try {
            game.engine.Game game = new game.engine.Game(selectedRole);

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("GameBoard.fxml"));
            Parent root = loader.load();

            GameBoardController controller = loader.getController();
            controller.initializeGame(game);

            Stage stage = Main.getPrimaryStage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("DooR DasH — Game in Progress");
            stage.setFullScreenExitHint("");
            // This is the key - set fullscreen directly, not maximized
            stage.setFullScreen(true);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not start the game", "Error: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------
    // GAME INSTRUCTIONS
    // ----------------------------------------------------------
    private void showGameInstructions() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Instructions");
        alert.setHeaderText("DooR DasH — How to Play");
        alert.setContentText(
            "OBJECTIVE:\n" +
            "Reach cell 99 (Boo's Door) with at least 1000 energy.\n\n" +
            "ROLES:\n" +
            "• SCARER → gain energy from blue (SCARER) doors\n" +
            "• LAUGHER → gain energy from amber (LAUGHER) doors\n" +
            "• Landing on the wrong-colored door loses energy!\n\n" +
            "CELL TYPES:\n" +
            "• Doors: gain/lose energy based on role\n" +
            "• Monster Cells: same role = free powerup, opposite role = swap energy if you have more\n" +
            "• Card Cells: draw a random card with special effects\n" +
            "• Conveyor Belts: move forward\n" +
            "• Contamination Socks: move backward + lose 100 energy\n" +
            "• Normal Cells: nothing happens\n\n" +
            "TURN:\n" +
            "1. Optional: activate your powerup (costs 500 energy)\n" +
            "2. Roll the dice (1–6) to move\n" +
            "3. Cell effect activates\n" +
            "4. Next player's turn\n\n" +
            "WIN: Reach cell 99 with 1000+ energy!"
        );
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ----------------------------------------------------------
    // MUSIC TOGGLE
    // ----------------------------------------------------------
    private void setupMusicToggle() {
        musicToggleButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(musicToggleButton, 1.12);
            }
        });
        
        musicToggleButton.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(musicToggleButton, 1.0);
            }
        });
        
        if (SoundManager.isMusicMuted()) {
            musicToggleButton.setText("MUTE");
        } else {
            musicToggleButton.setText("♫");
        }
        
        musicToggleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SoundManager.toggleMusic();
                if (SoundManager.isMusicMuted()) {
                    musicToggleButton.setText("MUTE");
                } else {
                    musicToggleButton.setText("♫");
                }
            }
        });
    }

    private void zoom(javafx.scene.Node node, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setToX(scale);
        st.setToY(scale);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    // ----------------------------------------------------------
    // MONSTER SELECTION
    // ----------------------------------------------------------
    private void setupMonsterSelection() {
        // SULLEY (SCARER)
        sulleyContainer.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(sulleyContainer, 1.05);
                if (selectedRole != Role.SCARER) {
                    softGlow(sulleyGlow, Color.web("#7fbfff"), 18);
                }
            }
        });
        
        sulleyContainer.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(sulleyContainer, 1.0);
                if (selectedRole != Role.SCARER) {
                    softGlow(sulleyGlow, Color.web("#7fbfff"), 0);
                }
            }
        });
        
        sulleyContainer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectRole(Role.SCARER);
            }
        });
        
        // MIKE (LAUGHER)
        mikeContainer.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(mikeContainer, 1.05);
                if (selectedRole != Role.LAUGHER) {
                    softGlow(mikeGlow, Color.web("#ff6347"), 18);
                }
            }
        });
        
        mikeContainer.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                zoom(mikeContainer, 1.0);
                if (selectedRole != Role.LAUGHER) {
                    softGlow(mikeGlow, Color.web("#ff6347"), 0);
                }
            }
        });
        
        mikeContainer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectRole(Role.LAUGHER);
            }
        });
    }

    private void selectRole(Role role) {
        SoundManager.playMonsterClick();
        selectedRole = role;
        System.out.println("Selected role: " + role);
        
        // Update name plates when role is selected
        updateNamePlates();

        if (role == Role.SCARER) {
            strongGlow(sulleyGlow, Color.web("#3bb3ff"), 40);
            softGlow(mikeGlow, Color.web("#ff6347"), 0);
        } else {
            strongGlow(mikeGlow, Color.web("#cd5c5c"), 40);
            softGlow(sulleyGlow, Color.web("#7fbfff"), 0);
        }
    }

    private void softGlow(DropShadow shadow, Color color, double radius) {
        shadow.setColor(color);
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(200),
            new KeyValue(shadow.radiusProperty(), radius, Interpolator.EASE_BOTH),
            new KeyValue(shadow.spreadProperty(), radius > 0 ? 0.25 : 0.0, Interpolator.EASE_BOTH));
        Timeline t = new Timeline(keyFrame1);
        t.play();
    }

    private void strongGlow(DropShadow shadow, Color color, double radius) {
        shadow.setColor(color);
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(250),
            new KeyValue(shadow.radiusProperty(), radius, Interpolator.EASE_BOTH),
            new KeyValue(shadow.spreadProperty(), 0.55, Interpolator.EASE_BOTH));
        Timeline t = new Timeline(keyFrame1);
        t.play();
    }

    private void animateMonsters() {
        animateFloat(sulleyContainer, 1.8);
        animateFloat(mikeContainer, 2.1);
    }

    private void animateFloat(javafx.scene.Node node, double seconds) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(seconds), node);
        tt.setFromY(-6);
        tt.setToY(6);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }
}