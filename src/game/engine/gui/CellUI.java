package game.engine.gui;

import game.engine.Role;
import game.engine.cells.Cell;
import game.engine.cells.CardCell;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.ContaminationSock;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import javafx.animation.FadeTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class CellUI extends StackPane {

    public static final double CELL_SIZE = 65.0;

    private static Image imgScarerDoor;
    private static Image imgScarerDoorUsed;
    private static Image imgLaugherDoor;
    private static Image imgLaugherDoorUsed;
    private static Image imgConveyor;
    private static Image imgSock;
    private static Image imgCard;
    private static Image imgScarerMarker;
    private static Image imgLaugherMarker;
    private static final Map<String, Image> monsterImages = new HashMap<>();

    private static boolean imagesLoaded = false;
    
    // Card tracking system
    private static boolean[] cardUsed = new boolean[100];
    private static int remainingCards = 0;
    private static int totalCards = 0;

    private final int index;
    private final ImageView baseImage;
    private final Label indexLabel;
    private final Label energyLabel;
    private final Label monsterNameLabel;
    private final ImageView monsterImage;
    private final Rectangle glowBorder;
    private final ImageView playerMarker;
    private Label usedLabel;

    public CellUI(int index) {
        this.index = index;

        setPrefSize(CELL_SIZE, CELL_SIZE);
        setMinSize(CELL_SIZE, CELL_SIZE);
        setMaxSize(CELL_SIZE, CELL_SIZE);

        setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #2a2a3a; -fx-border-width: 1;");

        baseImage = new ImageView();
        baseImage.setFitWidth(CELL_SIZE);
        baseImage.setFitHeight(CELL_SIZE);
        baseImage.setPreserveRatio(false);
        baseImage.setSmooth(true);

        indexLabel = new Label(String.valueOf(index));
        indexLabel.setStyle(
            "-fx-text-fill: #ffffff; -fx-font-size: 9px; -fx-font-weight: bold; " +
            "-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 1 3 1 3; -fx-background-radius: 3;");
        StackPane.setAlignment(indexLabel, Pos.TOP_LEFT);

        energyLabel = new Label("");
        energyLabel.setStyle(
            "-fx-text-fill: #ffffff; -fx-font-size: 10px; -fx-font-weight: bold; " +
            "-fx-background-color: rgba(0,0,0,0.55); -fx-padding: 1 4 1 4; -fx-background-radius: 3;");
        StackPane.setAlignment(energyLabel, Pos.BOTTOM_RIGHT);

        monsterNameLabel = new Label("");
        monsterNameLabel.setStyle(
            "-fx-text-fill: #ffffff; -fx-font-size: 8px; -fx-font-weight: bold; " +
            "-fx-background-color: rgba(0,0,0,0.65); -fx-padding: 1 3 1 3; -fx-background-radius: 3;");
        StackPane.setAlignment(monsterNameLabel, Pos.BOTTOM_CENTER);

        monsterImage = new ImageView();
        monsterImage.setFitWidth(CELL_SIZE);
        monsterImage.setFitHeight(CELL_SIZE);
        monsterImage.setPreserveRatio(false);
        monsterImage.setSmooth(true);

        glowBorder = new Rectangle(CELL_SIZE - 2, CELL_SIZE - 2);
        glowBorder.setFill(Color.TRANSPARENT);
        glowBorder.setStrokeWidth(4);
        glowBorder.setOpacity(0);
        glowBorder.setMouseTransparent(true);
        glowBorder.setArcWidth(3);
        glowBorder.setArcHeight(3);
        StackPane.setAlignment(glowBorder, Pos.CENTER);

        playerMarker = new ImageView();
        playerMarker.setFitWidth(28);
        playerMarker.setFitHeight(28);
        playerMarker.setPreserveRatio(true);
        playerMarker.setOpacity(0);
        playerMarker.setMouseTransparent(true);
        
        usedLabel = new Label("USED");
        usedLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px; -fx-font-weight: bold; " +
            "-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 2 4 2 4; -fx-background-radius: 3;");
        usedLabel.setMouseTransparent(true);
        usedLabel.setVisible(false);
        StackPane.setAlignment(usedLabel, Pos.CENTER);

        getChildren().addAll(baseImage, monsterImage, indexLabel, energyLabel, 
                             monsterNameLabel, glowBorder, playerMarker, usedLabel);
    }
    
    // Card tracking methods
    public static void initializeCardTracking(Cell[][] boardCells) {
        totalCards = 0;
        remainingCards = 0;
        for (int i = 0; i < 100; i++) {
            int row = i / 10;
            int col;
            if (row % 2 == 0) {
                col = i % 10;
            } else {
                col = 9 - (i % 10);
            }
            if (boardCells[row][col] instanceof CardCell) {
                cardUsed[i] = false;
                totalCards++;
                remainingCards++;
            }
        }
    }

    public static boolean isCardUsed(int index) {
        if (index < 0 || index >= 100) return true;
        return cardUsed[index];
    }

    public static void useCard(int index) {
        if (index >= 0 && index < 100 && !cardUsed[index]) {
            cardUsed[index] = true;
            remainingCards--;
        }
    }

    public static void renewAllCards() {
        for (int i = 0; i < 100; i++) {
            cardUsed[i] = false;
        }
        remainingCards = totalCards;
    }

    public static int getRemainingCards() {
        return remainingCards;
    }

    public static int getTotalCards() {
        return totalCards;
    }

    private static void loadImagesIfNeeded() {
    if (imagesLoaded) return;
    imagesLoaded = true;

    imgScarerDoor       = loadImage("/images/scarer_notactiv.png");
    imgScarerDoorUsed   = loadImage("/images/scarer_activated.png");
    imgLaugherDoor      = loadImage("/images/laugher_notactiv.png");
    imgLaugherDoorUsed  = loadImage("/images/laugher_activated.png.png");
    imgConveyor         = loadImage("/images/zigzag_wooden_ladder.png");
    imgSock             = loadImage("/images/contamsocks.png");
    imgCard             = loadImage("/images/IDcard.png");
    imgScarerMarker     = loadImage("/images/bluebutton_scarer.png");
    imgLaugherMarker    = loadImage("/images/player_marker_red_orange.png");

    monsterImages.put("James P. Sullivan", loadImage("/images/monster.png"));
    monsterImages.put("Mike Wazowski",     loadImage("/images/Adobe Express - file (1).png"));
    monsterImages.put("Randall Boggs",     loadImage("/images/Randall_Bogs.png"));
    monsterImages.put("Celia Mae",         loadImage("/images/Celia Mae.png"));
    monsterImages.put("Roz",               loadImage("/images/Roz.png"));
    monsterImages.put("Fungus",            loadImage("/images/fungus_monstersinc.png"));
    monsterImages.put("Henry J. Waternoose", loadImage("/images/Henry J. Waternoose.png"));
    monsterImages.put("Yeti",              loadImage("/images/Yeti_new.png"));

    System.out.println("Monster images loaded. Keys: " + monsterImages.keySet());
}

    private static Image loadImage(String path) {
        try {
            java.net.URL url = CellUI.class.getResource(path);
            if (url == null) {
                System.err.println("ERROR: Image not found on classpath: " + path);
                return null;
            }
            Image img = new Image(url.toExternalForm(), true);
            if (img.isError()) {
                System.err.println("ERROR loading: " + path);
            } else {
                System.out.println("Loaded: " + path);
            }
            return img;
        } catch (Exception e) {
            System.err.println("Exception loading: " + path + " — " + e.getMessage());
            return null;
        }
    }

    public static Image getMonsterImageByName(String name) {
        loadImagesIfNeeded();
        return monsterImages.get(name);
    }

    public void updateDisplay(Cell[][] boardCells) {
        int row = index / 10;
        int col;
        if (row % 2 == 0) {
            col = index % 10;
        } else {
            col = 9 - (index % 10);
        }

        Cell cell = boardCells[row][col];

        baseImage.setImage(null);
        energyLabel.setText("");
        monsterImage.setImage(null);
        monsterNameLabel.setText("");
        usedLabel.setVisible(false);
        
        // Remove any old used labels that might have been added directly
        getChildren().removeIf(node -> node instanceof Label && node != indexLabel && node != energyLabel && node != monsterNameLabel && node != usedLabel);

        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            if (door.getRole() == Role.SCARER) {
                baseImage.setImage(door.isActivated() ? imgScarerDoorUsed : imgScarerDoor);
            } else {
                baseImage.setImage(door.isActivated() ? imgLaugherDoorUsed : imgLaugherDoor);
            }
            energyLabel.setText("+" + door.getEnergy());
            if (door.isActivated()) {
                energyLabel.setStyle(
                    "-fx-text-fill: #888888; -fx-font-size: 10px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 1 4 1 4; -fx-background-radius: 3;");
            } else {
                energyLabel.setStyle(
                    "-fx-text-fill: #ffffff; -fx-font-size: 10px; -fx-font-weight: bold; " +
                    "-fx-background-color: rgba(0,0,0,0.55); -fx-padding: 1 4 1 4; -fx-background-radius: 3;");
            }
            setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #2a2a3a; -fx-border-width: 1;");

        } else if (cell instanceof MonsterCell) {
            MonsterCell mc = (MonsterCell) cell;
            String monsterName = mc.getCellMonster().getName();
            Image img = monsterImages.get(monsterName);
            if (img != null) {
                monsterImage.setImage(img);
            }
            monsterNameLabel.setText(monsterName);
            setStyle("-fx-background-color: #1a2540; -fx-border-color: #2a2a3a; -fx-border-width: 1;");

        } else if (cell instanceof ConveyorBelt) {
            baseImage.setImage(imgConveyor);
            setStyle("-fx-background-color: #1a3520; -fx-border-color: #2a2a3a; -fx-border-width: 1;");
            
        } else if (cell instanceof ContaminationSock) {
            baseImage.setImage(imgSock);
            setStyle("-fx-background-color: #3a2010; -fx-border-color: #2a2a3a; -fx-border-width: 1;");

        } else if (cell instanceof CardCell) {
            if (cardUsed[index]) {
                // Used card cell - dark gray-blue background with "USED" text
                setStyle("-fx-background-color: #2a2a3a; -fx-border-color: #3a3a4a; -fx-border-width: 1;");
                usedLabel.setVisible(true);
                baseImage.setImage(null);
            } else {
                // Unused card cell - show card image with dark red background
                baseImage.setImage(imgCard);
                setStyle("-fx-background-color: #3a1010; -fx-border-color: #2a2a3a; -fx-border-width: 1;");
            }

        } else {
            // Normal cells - checkerboard pattern with dark gray-blue colors
            if ((row + col) % 2 == 0) {
                setStyle("-fx-background-color: #2a2a3a; -fx-border-color: #1a1a2e; -fx-border-width: 1;");
            } else {
                setStyle("-fx-background-color: #1e1e2e; -fx-border-color: #1a1a2e; -fx-border-width: 1;");
            }
            baseImage.setImage(null);
        }
    }

    public void showPlayer(Role role) {
        playerMarker.setImage(role == Role.SCARER ? imgScarerMarker : imgLaugherMarker);
        Color borderColor = (role == Role.SCARER) ? Color.web("#3fc9d6") : Color.web("#ff7833");
        glowBorder.setStroke(borderColor);
        glowBorder.setEffect(new DropShadow(15, borderColor));
        glowBorder.setStrokeWidth(5);
        
        FadeTransition borderFade = new FadeTransition(Duration.millis(260), glowBorder);
        borderFade.setFromValue(glowBorder.getOpacity());
        borderFade.setToValue(0.9);
        borderFade.play();

        FadeTransition markerFade = new FadeTransition(Duration.millis(260), playerMarker);
        markerFade.setFromValue(playerMarker.getOpacity());
        markerFade.setToValue(1.0);
        markerFade.play();
    }

    public void hidePlayer() {
        FadeTransition borderFade = new FadeTransition(Duration.millis(260), glowBorder);
        borderFade.setFromValue(glowBorder.getOpacity());
        borderFade.setToValue(0);
        borderFade.play();

        FadeTransition markerFade = new FadeTransition(Duration.millis(260), playerMarker);
        markerFade.setFromValue(playerMarker.getOpacity());
        markerFade.setToValue(0);
        markerFade.play();
    }

    public int getIndex() { return index; }
    public void setMonsterPresent(String monsterName, Role role) { showPlayer(role); }
    public void clearMonster() { hidePlayer(); }
    public void clearMonsters() { hidePlayer(); }
}