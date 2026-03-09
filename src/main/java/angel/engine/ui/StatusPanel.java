package angel.engine.ui;

import angel.engine.core.GameState;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class StatusPanel extends VBox {

    private final Label modeLabel;
    private final Label positionLabel;
    private final Label stepsLabel;
    private final Label mapLabel;
    private final Label tileLabel;

    public StatusPanel() {
        modeLabel = new Label();
        positionLabel = new Label();
        stepsLabel = new Label();
        mapLabel = new Label();
        tileLabel = new Label();

        getChildren().addAll(
                new Label("Engine Stats"),
                new Separator(),
                modeLabel,
                positionLabel,
                stepsLabel,
                mapLabel,
                tileLabel
        );
        setPadding(new Insets(10));
        setPrefWidth(200);
    setMinWidth(0);
        setStyle("-fx-background-color: #2b3042; -fx-background-radius: 8; -fx-text-fill: white;");
        getChildren().forEach(node -> {
            if (node instanceof Label label) {
                label.setTextFill(Color.WHITE);
            }
        });
    }

    public void setMode(String mode) {
        modeLabel.setText("Mode: " + mode);
    }

    public void update(GameState state, int tileSize) {
        positionLabel.setText("Player: (" + state.playerX + ", " + state.playerY + ")");
        stepsLabel.setText("Steps: " + state.steps);
        mapLabel.setText("Map: " + state.mapWidth + "x" + state.mapHeight);
        tileLabel.setText("Tile: " + tileSize + "px");
    }
}
