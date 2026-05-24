package game.engine.gui;

import game.engine.Role;
import game.engine.monsters.Monster;
import game.engine.monsters.Dasher;
import game.engine.monsters.Dynamo;
import game.engine.monsters.MultiTasker;
import game.engine.monsters.Schemer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PlayerInfoController {

    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label typeLabel;
    @FXML private Label currentRoleLabel;
    @FXML private Label energyLabel;
    @FXML private Label positionLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar energyBar;
    @FXML private ImageView monsterPortrait;

    private Monster monster;
    private boolean isPlayer;

    public void setMonster(Monster monster, boolean isPlayer) {
        this.monster = monster;
        this.isPlayer = isPlayer;

        // Load and set the portrait
        if (monsterPortrait != null) {
            String name = monster.getName();
            Image img = CellUI.getMonsterImageByName(name);

            if (img != null) {
                monsterPortrait.setImage(img);
                System.out.println("✓ Portrait loaded for: " + name);
            } else {
                System.err.println("✗ No portrait found for: " + name);
            }
        }

        updateStats(monster);
    }

    public void updateStats(Monster monster) {
        this.monster = monster;
        
        nameLabel.setText(monster.getName());
        roleLabel.setText(monster.getOriginalRole().toString());
        typeLabel.setText(monster.getClass().getSimpleName());
        
        if (monster.isConfused()) {
            currentRoleLabel.setText("CURRENT: " + monster.getRole().toString());
            currentRoleLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            currentRoleLabel.setText("CURRENT: " + monster.getRole().toString());
            currentRoleLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
        }
        
        energyLabel.setText(monster.getEnergy() + " / 1000");
        positionLabel.setText("Cell " + monster.getPosition());
        
        StringBuilder status = new StringBuilder();
        
        // Common status effects
        if (monster.isShielded()) status.append("🛡️ Shielded ");
        if (monster.isFrozen()) status.append("❄️ Frozen ");
        if (monster.isConfused()) status.append("🔄 Confused (").append(monster.getConfusionTurns()).append(" left) ");
        
        // DASHER - Lightning Movement (2x speed) / Momentum Rush (3x speed)
        if (monster instanceof Dasher) {
            int momentum = ((Dasher) monster).getMomentumTurns();
            if (momentum > 0) {
                status.append("⚡ Momentum Rush (3x speed, ").append(momentum).append(" left) ");
            } else {
                status.append("⚡ Lightning Movement (2x speed) ");
            }
        }
        
        // DYNAMO - Energy Amplification (2x gains & losses)
        if (monster instanceof Dynamo) {
            status.append("💪 Energy Amplification (2x gains/losses) ");
        }
        
        // MULTITASKER - Half speed + 200 energy bonus / Focus Mode (normal speed)
        if (monster instanceof MultiTasker) {
            int focus = ((MultiTasker) monster).getNormalSpeedTurns();
            if (focus > 0) {
                status.append("🎯 Focus Mode (normal speed, ").append(focus).append(" left) ");
            } else {
                status.append("🐢 Slow & Steady (½ speed + 200 energy) ");
            }
        }
        
        // SCHEMER - Energy Manipulation (+10 to all energy changes)
        if (monster instanceof Schemer) {
            status.append("🃏 Energy Manipulation (+10 to all changes) ");
        }
        
        if (status.length() == 0) {
            statusLabel.setText("No active effects");
        } else {
            statusLabel.setText(status.toString());
        }
        
        double progress = Math.min(1.0, monster.getEnergy() / 1000.0);
        energyBar.setProgress(progress);
        
        if (monster.getOriginalRole() == Role.SCARER) {
            energyBar.setStyle("-fx-accent: #3fc9d6;");
        } else {
            energyBar.setStyle("-fx-accent: #ff7833;");
        }
    }
}