package angel.engine.ui;

import angel.engine.core.GameState;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameHUD extends VBox {

    private final Label titleLabel;
    private final Label healthLabel;
    private final Label statusLabel;
    private final Rectangle healthBar;
    private static final double MAX_BAR_WIDTH = 128;

    public GameHUD() {
        getStyleClass().add("engine-hud");
        setPadding(new Insets(8, 10, 8, 10));
        setSpacing(6);
        setAlignment(Pos.TOP_LEFT);
        setMouseTransparent(true);
        setPickOnBounds(false);

        titleLabel = new Label("Combat HUD");
        titleLabel.getStyleClass().add("engine-hud-title");
        healthLabel = new Label("Health: 100/100");
        healthLabel.getStyleClass().add("engine-hud-label");
        statusLabel = new Label("Status: Stable");
        statusLabel.getStyleClass().add("engine-hud-status");

        Rectangle backgroundBar = new Rectangle(MAX_BAR_WIDTH, 8);
        backgroundBar.setFill(Color.web("#435165"));
        backgroundBar.setArcWidth(5);
        backgroundBar.setArcHeight(5);

        healthBar = new Rectangle(MAX_BAR_WIDTH, 8);
        healthBar.setFill(Color.web("#ff4444"));
        healthBar.setArcWidth(5);
        healthBar.setArcHeight(5);

        HBox metricsRow = new HBox(8, healthLabel, buildDivider(), statusLabel);
        metricsRow.setAlignment(Pos.CENTER_LEFT);

        StackPane barStack = new StackPane();
        barStack.setAlignment(Pos.CENTER_LEFT);
        barStack.getChildren().addAll(backgroundBar, healthBar);

        getChildren().addAll(titleLabel, metricsRow, barStack);
    }

    public void update(GameState state) {
        if (state == null) {
            return;
        }
        int current = Math.max(0, state.health);
        int max = state.maxHealth;

        healthLabel.setText(String.format("Health: %d/%d", current, max));
        statusLabel.setText(current > max * 0.35 ? "Status: Stable" : "Status: Critical");
        double safeMax = Math.max(1, max);
        double percent = (double) current / safeMax;
        healthBar.setWidth(MAX_BAR_WIDTH * percent);

        if (percent > 0.5) {
            healthBar.setFill(Color.web("#44ff44"));
        } else if (percent > 0.25) {
            healthBar.setFill(Color.web("#ffff44"));
        } else {
            healthBar.setFill(Color.web("#ff4444"));
        }
    }

    private Separator buildDivider() {
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPrefHeight(14);
        return separator;
    }
}
