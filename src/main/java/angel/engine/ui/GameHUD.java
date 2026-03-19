package angel.engine.ui;

import angel.engine.core.GameState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameHUD extends VBox {

    private final Label healthLabel;
    private final Rectangle healthBar;
    private final double maxBarWidth = 200;

    public GameHUD() {
        setPadding(new Insets(10));
        setSpacing(5);
        setAlignment(Pos.TOP_LEFT);
        
        
        

        healthLabel = new Label("Health: 100/100");
        healthLabel.setTextFill(Color.WHITE);
        healthLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        
        HBox barContainer = new HBox();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        
        
        Rectangle backgroundBar = new Rectangle(maxBarWidth, 10);
        backgroundBar.setFill(Color.web("#555555"));
        backgroundBar.setArcWidth(5);
        backgroundBar.setArcHeight(5);

        
        healthBar = new Rectangle(maxBarWidth, 10);
        healthBar.setFill(Color.web("#ff4444"));
        healthBar.setArcWidth(5);
        healthBar.setArcHeight(5);

        
        
        javafx.scene.layout.StackPane barStack = new javafx.scene.layout.StackPane();
        barStack.setAlignment(Pos.CENTER_LEFT);
        barStack.getChildren().addAll(backgroundBar, healthBar);

        getChildren().addAll(healthLabel, barStack);
    }

    public void update(GameState state) {
        if (state == null) return;
        
        int current = Math.max(0, state.health);
        int max = state.maxHealth;
        
        healthLabel.setText(String.format("Health: %d/%d", current, max));
        
        double percent = (double) current / max;
        healthBar.setWidth(maxBarWidth * percent);
        
        
        if (percent > 0.5) {
            healthBar.setFill(Color.web("#44ff44"));
        } else if (percent > 0.25) {
            healthBar.setFill(Color.web("#ffff44"));
        } else {
            healthBar.setFill(Color.web("#ff4444"));
        }
    }
}
