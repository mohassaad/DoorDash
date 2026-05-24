package game.engine.gui;

import game.engine.Constants;
import game.engine.Game;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Dasher;
import game.engine.monsters.Dynamo;
import game.engine.monsters.Monster;
import game.engine.monsters.MultiTasker;
import game.engine.monsters.Schemer;
import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.cells.CardCell;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Polygon;
import javafx.scene.layout.Pane;
import javafx.animation.ScaleTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameBoardController {
	
	@FXML private javafx.scene.layout.StackPane gameEndOverlay;
	@FXML private Label gameEndTitle;
	@FXML private Label gameEndBody;
	@FXML private javafx.scene.layout.StackPane cardPopupOverlay;
	@FXML private Label cardPopupTitle;
	@FXML private Label cardPopupBody;
	@FXML private Button cardPopupCloseBtn;
    @FXML private Pane transportOverlay;
    @FXML private javafx.scene.layout.StackPane dieFace1;
    @FXML private Label dieLabel1;
    @FXML private GridPane boardGrid;
    @FXML private Label currentTurnLabel;
    @FXML private Label diceResultLabel;
    @FXML private Label messageLabel;
    @FXML private Button rollButton;
    @FXML private Button powerupButton;
    @FXML private VBox playerInfoContainer;
    @FXML private VBox opponentInfoContainer;
    @FXML private VBox cardHistoryContainer;

    private Game game;
    private Map<Integer, CellUI> cellUIMap;
    private PlayerInfoController playerInfoController;
    private PlayerInfoController opponentInfoController;

    private int lastPlayerPos = -1;
    private int lastOpponentPos = -1;
    private boolean isInitialized = false;

    @FXML
    public void initialize() {
        cellUIMap = new HashMap<>();
        setupBoard();
        setupCheatCodes();
    }

    private void setupCheatCodes() {
        boardGrid.sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> obs, Scene oldScene, Scene newScene) {
                if (newScene != null) {
                    newScene.setOnKeyPressed(event -> {
                        if (game == null) return;
                        
                        if (event.getCode() == KeyCode.W) {
                            Monster current = game.getCurrent();
                            if (current != null) {
                                int oldPosition = current.getPosition();
                                current.setPosition(99);
                                updateBoard();
                                showMessage("🔧 CHEAT: " + current.getName() + " teleported from " + oldPosition + " to cell 99!");
                                Monster winner = game.getWinner();
                                if (winner != null) {
                                    showGameEnd();
                                }
                            }
                        } else if (event.getCode() == KeyCode.E) {
                            Monster current = game.getCurrent();
                            if (current != null) {
                                int oldEnergy = current.getEnergy();
                                int newEnergy = oldEnergy + 500;
                                current.setEnergy(newEnergy);
                                updateBoard();
                                showMessage("🔧 CHEAT: " + current.getName() + " gained 500 energy! Now at " + newEnergy + "/1000");
                                Monster winner = game.getWinner();
                                if (winner != null) {
                                    showGameEnd();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void initializeGame(Game game) {
        if (isInitialized) return;
        isInitialized = true;
        
        this.game = game;
        
        // Clear containers to prevent duplicates
        playerInfoContainer.getChildren().clear();
        opponentInfoContainer.getChildren().clear();
        
        // Initialize card tracking
        CellUI.initializeCardTracking(game.getBoard().getBoardCells());
        showMessage("📊 Total Card Cells: " + CellUI.getTotalCards());
        
        loadPlayerInfo();
        updateTurnDisplay();
        updateBoard();
        
        Platform.runLater(() -> drawTransportConnections());
        
        boardGrid.widthProperty().addListener((obs, oldVal, newVal) -> 
            Platform.runLater(() -> drawTransportConnections()));
        boardGrid.heightProperty().addListener((obs, oldVal, newVal) -> 
            Platform.runLater(() -> drawTransportConnections()));
    }

    private void setupBoard() {
        boardGrid.getChildren().clear();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setStyle("-fx-background-color: #000000;");

        for (int displayRow = 0; displayRow < Constants.BOARD_ROWS; displayRow++) {
            int row = (Constants.BOARD_ROWS - 1) - displayRow;
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                int index;
                if (row % 2 == 0) {
                    index = row * Constants.BOARD_COLS + col;
                } else {
                    index = row * Constants.BOARD_COLS + (Constants.BOARD_COLS - 1 - col);
                }
                CellUI cellUI = new CellUI(index);
                cellUIMap.put(index, cellUI);
                boardGrid.add(cellUI, col, displayRow);
            }
        }

        if (transportOverlay != null) {
            transportOverlay.prefWidthProperty().bind(boardGrid.widthProperty());
            transportOverlay.prefHeightProperty().bind(boardGrid.heightProperty());
            transportOverlay.maxWidthProperty().bind(boardGrid.widthProperty());
            transportOverlay.maxHeightProperty().bind(boardGrid.heightProperty());
        }
    }

    private void loadPlayerInfo() {
        try {
            FXMLLoader playerLoader = new FXMLLoader(getClass().getResource("PlayerInfo.fxml"));
            VBox playerBox = playerLoader.load();
            playerInfoController = playerLoader.getController();
            playerInfoController.setMonster(game.getPlayer(), true);
            playerInfoContainer.getChildren().add(playerBox);

            FXMLLoader opponentLoader = new FXMLLoader(getClass().getResource("PlayerInfo.fxml"));
            VBox opponentBox = opponentLoader.load();
            opponentInfoController = opponentLoader.getController();
            opponentInfoController.setMonster(game.getOpponent(), false);
            opponentInfoContainer.getChildren().add(opponentBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateBoard() {
        for (CellUI cellUI : cellUIMap.values()) {
            cellUI.updateDisplay(game.getBoard().getBoardCells());
        }

        int playerPos = game.getPlayer().getPosition();
        int opponentPos = game.getOpponent().getPosition();

        if (lastPlayerPos != -1 && lastPlayerPos != playerPos && lastPlayerPos != opponentPos) {
            CellUI prev = cellUIMap.get(lastPlayerPos);
            if (prev != null) prev.hidePlayer();
        }
        if (lastOpponentPos != -1 && lastOpponentPos != opponentPos && lastOpponentPos != playerPos) {
            CellUI prev = cellUIMap.get(lastOpponentPos);
            if (prev != null) prev.hidePlayer();
        }

        CellUI playerCell = cellUIMap.get(playerPos);
        if (playerCell != null) playerCell.showPlayer(game.getPlayer().getRole());
        
        CellUI opponentCell = cellUIMap.get(opponentPos);
        if (opponentCell != null) opponentCell.showPlayer(game.getOpponent().getRole());

        lastPlayerPos = playerPos;
        lastOpponentPos = opponentPos;

        playerInfoController.updateStats(game.getPlayer());
        opponentInfoController.updateStats(game.getOpponent());
    }

    private void updateTurnDisplay() {
        Monster current = game.getCurrent();
        currentTurnLabel.setText(current.getName() + "'s Turn");

        if (current == game.getPlayer()) {
            playerInfoContainer.setStyle("-fx-border-color: #4ade50; -fx-border-width: 3; -fx-border-radius: 10; -fx-padding: 5;");
            opponentInfoContainer.setStyle("-fx-border-color: #555555; -fx-border-width: 1; -fx-border-radius: 10; -fx-padding: 5;");
        } else {
            opponentInfoContainer.setStyle("-fx-border-color: #ffc857; -fx-border-width: 3; -fx-border-radius: 10; -fx-padding: 5;");
            playerInfoContainer.setStyle("-fx-border-color: #555555; -fx-border-width: 1; -fx-border-radius: 10; -fx-padding: 5;");
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        Label historyLabel = new Label("• " + message);
        historyLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
        cardHistoryContainer.getChildren().add(0, historyLabel);
        if (cardHistoryContainer.getChildren().size() > 20) {
            cardHistoryContainer.getChildren().remove(20);
        }
    }

    private void showCardPopup(Card drawnCard) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎴 Card Drawn!");
        alert.setHeaderText(drawnCard.getName());
        
        String content = drawnCard.getDescription();
        alert.setContentText(content);
        
        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #1e1e2e;");
        alert.showAndWait();
    }

    @FXML
    private void onRollDice() {
        SoundManager.playButtonClick();

        if (game.getWinner() != null) {
            showGameEnd();
            return;
        }

        rollButton.setDisable(true);
        powerupButton.setDisable(true);
        diceResultLabel.setText("Rolling...");

        animateDiceRoll();
    }

    private void animateDiceRoll() {
        Random rand = new Random();

        Timeline shuffle = new Timeline(
            new KeyFrame(Duration.millis(80), e -> {
                dieLabel1.setText(String.valueOf(rand.nextInt(6) + 1));
            })
        );
        shuffle.setCycleCount(10);

        ScaleTransition s1 = new ScaleTransition(Duration.millis(180), dieFace1);
        s1.setFromX(1.0); s1.setFromY(1.0);
        s1.setToX(1.15); s1.setToY(1.15);
        s1.setAutoReverse(true);
        s1.setCycleCount(5);

        RotateTransition r1 = new RotateTransition(Duration.millis(900), dieFace1);
        r1.setByAngle(360);

        shuffle.play();
        s1.play();
        r1.play();

        shuffle.setOnFinished(e -> executeRoll());
    }

   private void executeRoll() {
    try {
        Monster before = game.getCurrent();
        int oldPosition = before.getPosition();
        int oldEnergy = before.getEnergy();

        if (before.isFrozen()) {
            showMessage("❄️ " + before.getName() + " is FROZEN — turn skipped");
            diceResultLabel.setText("SKIPPED");
            dieLabel1.setText("-");
            try {
                game.playTurn();
            } catch (Exception ex) {
                System.err.println("playTurn failed: " + ex.getMessage());
            }
            afterTurn();
            return;
        }

        // Show monster passive ability at start of turn
        showPowerupStatus(before);

        game.playTurn();

        int newPosition = before.getPosition();
        int newEnergy = before.getEnergy();
        int delta = newEnergy - oldEnergy;
        int moved = newPosition - oldPosition;
        int absMoved = Math.abs(moved);

        boolean wasTransported = (absMoved > 18) || (moved < 0);

        // Update dice display
        if (wasTransported) {
            dieLabel1.setText("T");
            diceResultLabel.setText("Transported to " + newPosition);
        } else {
            dieLabel1.setText(String.valueOf(absMoved));
            diceResultLabel.setText("Moved " + absMoved);
        }

        // Show move in action log
        String moveText = before.getName() + " moved " + oldPosition + " → " + newPosition;
        if (delta != 0) {
            moveText += "  (energy " + (delta > 0 ? "+" : "") + delta + ")";
        }
        showMessage(moveText);

        // Monster type ability messages
        if (delta != 0) {
            if (before instanceof Dynamo) {
                showMessage("💪 " + before.getName() + " (Dynamo): 2x energy effect applied!");
            }
            if (before instanceof Schemer) {
                showMessage("🃏 " + before.getName() + " (Schemer): +10 bonus applied to energy change!");
            }
            if (before instanceof MultiTasker) {
                int focus = ((MultiTasker) before).getNormalSpeedTurns();
                if (focus > 0) {
                    showMessage("🎯 " + before.getName() + " is in Focus Mode — normal speed!");
                } else {
                    showMessage("🐢 " + before.getName() + " (MultiTasker): +200 energy bonus from Energy Mastery!");
                }
            }
        }

        // Check if landed on card cell
        int newRow = newPosition / 10;
        int newCol = (newRow % 2 == 0) ? newPosition % 10 : 9 - (newPosition % 10);
        Cell landedCell = game.getBoard().getBoardCells()[newRow][newCol];
        boolean landedOnCard = (landedCell instanceof CardCell && !CellUI.isCardUsed(newPosition));

        if (landedOnCard) {
            CellUI.useCard(newPosition);
            showMessage("📇 " + before.getName() + " landed on a Card Cell! A card effect was activated.");
            showMessage("📊 Remaining Card Cells: " + CellUI.getRemainingCards() + "/" + CellUI.getTotalCards());
        }

        // Check if all cards used — renew them
        if (CellUI.getRemainingCards() == 0 && CellUI.getTotalCards() > 0) {
            CellUI.renewAllCards();
            showMessage("🔄 ALL CARD CELLS RENEWED! " + CellUI.getTotalCards() + " cards available again!");
        }

        // Transport delay then finish turn
        if (wasTransported) {
            PauseTransition transportDelay = new PauseTransition(Duration.millis(700));
            transportDelay.setOnFinished(e -> {
                String transportType = (moved > 0)
                    ? "🪜 CONVEYOR BELT! Transported forward to cell " + newPosition
                    : "🧦 CONTAMINATION SOCK! Pushed back to cell " + newPosition;
                showMessage(transportType);
                afterTurn();

                // Show card popup AFTER turn is fully done (non-blocking)
                if (landedOnCard) {
                    showCardPopupNonBlocking(before.getName());
                }
            });
            transportDelay.play();
        } else {
            afterTurn();

            // Show card popup AFTER turn is fully done (non-blocking)
            if (landedOnCard) {
                showCardPopupNonBlocking(before.getName());
            }
        }

    } catch (InvalidMoveException e) {
        // This is normal — opponent is on that cell, just tell the user to try again
        showMessage("⚠️ Cannot land on opponent! Roll again.");
        diceResultLabel.setText("Try again");
        rollButton.setDisable(false);
        powerupButton.setDisable(false);
    } catch (Exception e) {
        e.printStackTrace();
        showMessage("⚠️ Error: " + (e.getMessage() == null ? "Unknown error" : e.getMessage()));
        diceResultLabel.setText("Continue");
        rollButton.setDisable(false);
        powerupButton.setDisable(false);
    }
}

private void showCardPopupNonBlocking(String monsterName) {
    Platform.runLater(() -> {
        cardPopupTitle.setText("🎴 " + monsterName + " drew a card!");
        cardPopupBody.setText(
            "A card effect was applied.\n\n" +
            "📊 Remaining Card Cells: " + CellUI.getRemainingCards()
            + "/" + CellUI.getTotalCards() + "\n\n" +
            "Check the Action Log for details."
        );
        cardPopupOverlay.setVisible(true);
        cardPopupOverlay.setManaged(true);
    });
}
@FXML
private void onCloseCardPopup() {
    cardPopupOverlay.setVisible(false);
    cardPopupOverlay.setManaged(false);
}

    @FXML
    private void onUsePowerup() {
        SoundManager.playButtonClick();
        try {
            game.usePowerup();
            showMessage("⚡ " + game.getCurrent().getName() + " used POWERUP!");
            updateBoard();
            powerupButton.setDisable(true);
        } catch (OutOfEnergyException e) {
            showInvalidAction("Not Enough Energy", "You need at least 500 energy to use a powerup.");
        } catch (Exception e) {
            showInvalidAction("Powerup Failed", e.getMessage() == null ? "Could not use powerup" : e.getMessage());
        }
    }

    private void afterTurn() {
        try {
            updateBoard();
            updateTurnDisplay();

            Monster winner = game.getWinner();
            if (winner != null) {
                showGameEnd();
                return;
            }

            rollButton.setDisable(false);
            powerupButton.setDisable(false);
            diceResultLabel.setText("Ready");

            if (game.getCurrent().isConfused()) {
                showMessage("⚠️ " + game.getCurrent().getName() + " is CONFUSED for "
                    + game.getCurrent().getConfusionTurns() + " more turn(s)");
            }
            if (game.getCurrent().isShielded()) {
                showMessage("🛡️ " + game.getCurrent().getName() + " has SHIELD active");
            }
        } catch (Exception e) {
            e.printStackTrace();
            rollButton.setDisable(false);
            powerupButton.setDisable(false);
            diceResultLabel.setText("Ready");
        }
    }

    private void showInvalidAction(String title, String reason) {
        showMessage("⚠️ " + title + ": " + reason);
    }

    private void showGameEnd() {
    rollButton.setDisable(true);
    powerupButton.setDisable(true);

    Monster winner = game.getWinner();
    String winnerText = "🏆 " + winner.getName() + " (" + winner.getRole() + ") WINS! 🏆";
    showMessage(winnerText);

    gameEndTitle.setText(winnerText);
    gameEndBody.setText(
        "FINAL SCORES:\n\n" +
        "• " + game.getPlayer().getName() + " (" + game.getPlayer().getRole() + "): "
            + game.getPlayer().getEnergy() + " energy\n" +
        "• " + game.getOpponent().getName() + " (" + game.getOpponent().getRole() + "): "
            + game.getOpponent().getEnergy() + " energy"
    );
    gameEndOverlay.setVisible(true);
    gameEndOverlay.setManaged(true);
}

@FXML
private void onGameEndReturn() {
    returnToStartScreen();
}

@FXML
private void onGameEndExit() {
    Platform.exit();
}

    private void returnToStartScreen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("StartScreen.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle("DooR DasH — Scarer vs Laugher");
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setWidth(1280);
            stage.setHeight(720);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    @FXML
    private void onShowRules() {
        SoundManager.playButtonClick();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Rules");
        alert.setHeaderText("DooR DasH — Scare vs Laugh Touchdown");
        alert.setContentText(
            "OBJECTIVE: Reach cell 99 with 1000+ energy.\n\n" +
            "• Roll the dice to move\n" +
            "• Land on your role's doors to gain energy\n" +
            "• Land on opponent's doors to LOSE energy\n" +
            "• Card Cells give random effects\n" +
            "• Conveyor Belts move you forward\n" +
            "• Contamination Socks move you back + drain 100 energy\n" +
            "• Monster Cells give free powerups (same role) or swap energy (opposite role)\n\n" +
            "Powerups cost 500 energy.\n\n" +
            "🔧 CHEAT KEYS: Press W to teleport to cell 99, Press E to add 500 energy"
        );
        alert.showAndWait();
    }

    private void drawTransportConnections() {
        if (transportOverlay == null) return;
        transportOverlay.getChildren().clear();

        Cell[][] cells = game.getBoard().getBoardCells();

        for (int idx = 0; idx < 100; idx++) {
            int row = idx / 10;
            int col = (row % 2 == 0) ? idx % 10 : 9 - (idx % 10);
            Cell cell = cells[row][col];

            if (cell instanceof game.engine.cells.TransportCell) {
                game.engine.cells.TransportCell tc = (game.engine.cells.TransportCell) cell;
                int destination = idx + tc.getEffect();
                if (destination < 0) destination = 0;
                if (destination > 99) destination = 99;

                double[] src = getCellCenterPixel(idx);
                double[] dst = getCellCenterPixel(destination);

                String color;
                boolean isConveyor = cell instanceof game.engine.cells.ConveyorBelt;
                if (isConveyor) {
                    color = "#ffc857";
                } else {
                    color = "#ff5050";
                }

                javafx.scene.shape.QuadCurve curve = new javafx.scene.shape.QuadCurve();
                curve.setStartX(src[0]);
                curve.setStartY(src[1]);
                curve.setEndX(dst[0]);
                curve.setEndY(dst[1]);

                double midX = (src[0] + dst[0]) / 2;
                double midY = (src[1] + dst[1]) / 2;
                double dx = dst[0] - src[0];
                double dy = dst[1] - src[1];
                double len = Math.sqrt(dx * dx + dy * dy);
                double curveStrength = Math.min(50, len * 0.25);
                double offsetX = -dy / len * curveStrength;
                double offsetY = dx / len * curveStrength;

                if (!isConveyor) {
                    offsetX = -offsetX;
                    offsetY = -offsetY;
                }

                curve.setControlX(midX + offsetX);
                curve.setControlY(midY + offsetY);
                curve.setFill(null);
                curve.setStroke(javafx.scene.paint.Color.web(color));
                curve.setStrokeWidth(3);
                curve.setOpacity(0.8);

                if (!isConveyor) {
                    curve.getStrokeDashArray().addAll(8d, 6d);
                }

                Polygon arrow = makeArrowHead(src[0], src[1], dst[0], dst[1], color);
                transportOverlay.getChildren().addAll(curve, arrow);
            }
        }
    }

    private double[] getCellCenterPixel(int cellIndex) {
        CellUI cellUI = cellUIMap.get(cellIndex);
        if (cellUI == null) {
            int row = cellIndex / 10;
            int col = (row % 2 == 0) ? cellIndex % 10 : 9 - (cellIndex % 10);
            int displayRow = (Constants.BOARD_ROWS - 1) - row;
            double x = col * CellUI.CELL_SIZE + CellUI.CELL_SIZE / 2.0;
            double y = displayRow * CellUI.CELL_SIZE + CellUI.CELL_SIZE / 2.0;
            return new double[]{x, y};
        }

        javafx.geometry.Bounds cellBounds = cellUI.getBoundsInParent();
        double cellCenterX = cellBounds.getMinX() + cellBounds.getWidth() / 2.0;
        double cellCenterY = cellBounds.getMinY() + cellBounds.getHeight() / 2.0;
        return new double[]{cellCenterX, cellCenterY};
    }

    private Polygon makeArrowHead(double startX, double startY,
            double endX, double endY, String colorHex) {
    double angle = Math.atan2(endY - startY, endX - startX);
    double arrowSize = 14;
    
    // Check if this is a red (contamination sock) or yellow (conveyor belt) arrow
    boolean isRed = colorHex.equals("#ff5050");
    boolean isYellow = colorHex.equals("#ffc857");
    
    // Calculate perpendicular direction for shifting (90 degrees to the line)
    double perpX = -Math.sin(angle);  // Perpendicular X component
    double perpY = Math.cos(angle);   // Perpendicular Y component
    
    // SHIFT CONTROLS - Modify these numbers to adjust arrow position
    double shiftLeftRight = -2;    // Positive = right, Negative = left
    double shiftUpDown = 4;       // Positive = down, Negative = up
    
    double shiftX = 0;
    double shiftY = 0;
    
    if (isRed) {
        // Red arrows - adjust these values independently
        shiftX = (perpX * shiftLeftRight) + (Math.cos(angle) * shiftUpDown);
        shiftY = (perpY * shiftLeftRight) + (Math.sin(angle) * shiftUpDown);
    } else if (isYellow) {
        // Yellow arrows - adjust these values independently
        shiftX = (perpX * shiftLeftRight) + (Math.cos(angle) * shiftUpDown);
        shiftY = (perpY * shiftLeftRight) + (Math.sin(angle) * shiftUpDown);
    }
    
    // Apply shift to the end point
    double shiftedEndX = endX + shiftX;
    double shiftedEndY = endY + shiftY;
    
    // Calculate both sides of the arrow (full arrow head) using shifted end point
    double x1 = shiftedEndX - arrowSize * Math.cos(angle - Math.PI / 6);
    double y1 = shiftedEndY - arrowSize * Math.sin(angle - Math.PI / 6);
    double x2 = shiftedEndX - arrowSize * Math.cos(angle + Math.PI / 6);
    double y2 = shiftedEndY - arrowSize * Math.sin(angle + Math.PI / 6);
    
    // Add the back point of the arrow for a complete shape
    double backX = shiftedEndX - (arrowSize * 0.6) * Math.cos(angle);
    double backY = shiftedEndY - (arrowSize * 0.6) * Math.sin(angle);

    // Create a 4-point polygon for a filled arrow head
    Polygon arrow = new Polygon(shiftedEndX, shiftedEndY, x1, y1, backX, backY, x2, y2);
    arrow.setFill(javafx.scene.paint.Color.web(colorHex));
    arrow.setStroke(javafx.scene.paint.Color.web(colorHex));
    arrow.setStrokeWidth(2);
    arrow.setEffect(new javafx.scene.effect.DropShadow(8,
            javafx.scene.paint.Color.web(colorHex)));
    return arrow;
}
    
    private void showPowerupStatus(Monster monster) {
        if (monster instanceof Dasher) {
            int momentum = ((Dasher) monster).getMomentumTurns();
            if (momentum > 0) {
                showMessage("⚡ " + monster.getName() + " has Momentum Rush active for " + momentum + " more turn(s) — 3x movement speed!");
            } else {
                showMessage("⚡ " + monster.getName() + " has Lightning Movement — 2x movement speed!");
            }
        }
        
        if (monster instanceof MultiTasker) {
            int focus = ((MultiTasker) monster).getNormalSpeedTurns();
            if (focus > 0) {
                showMessage("🎯 " + monster.getName() + " has Focus Mode active for " + focus + " more turn(s) — normal movement speed!");
            } else {
                showMessage("🐢 " + monster.getName() + " has Slow & Steady — ½ movement speed but gains +200 energy!");
            }
        }
        
        if (monster instanceof Dynamo) {
            showMessage("💪 " + monster.getName() + " has Energy Amplification — all energy gains and losses are doubled!");
        }
        
        if (monster instanceof Schemer) {
            showMessage("🃏 " + monster.getName() + " has Energy Manipulation — +10 to all energy changes!");
        }
    }
}