package angel.engine.ui;

import angel.engine.core.GameState;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class StatusPanel extends VBox {

    private final Label modeLabel;
    private final Label positionLabel;
    private final Label stepsLabel;
    private final Label mapLabel;
    private final Label tileLabel;
    private final VBox statsBox;
    private final VBox toolsBox;
    private final ToggleGroup toolGroup;
    private final Map<BuildTool, ToggleButton> toolButtons;
    private Consumer<BuildTool> onToolSelected;

    public StatusPanel() {
        modeLabel = new Label();
        positionLabel = new Label();
        stepsLabel = new Label();
        mapLabel = new Label();
        tileLabel = new Label();

        statsBox = new VBox(8,
                new Label("Engine Stats"),
                new Separator(),
                modeLabel,
                positionLabel,
                stepsLabel,
                mapLabel,
                tileLabel
        );

        toolGroup = new ToggleGroup();
        toolButtons = new EnumMap<>(BuildTool.class);
        toolsBox = buildToolsBox();

        getChildren().addAll(statsBox, toolsBox);
        setPadding(new Insets(10));
        setPrefWidth(200);
        setMinWidth(0);
        setStyle("-fx-background-color: #2b3042; -fx-background-radius: 8; -fx-text-fill: white;");
        styleLabels(statsBox);
        styleLabels(toolsBox);
        setBuildMode(false);
    }

    public void setMode(String mode) {
        modeLabel.setText("Mode: " + mode);
    }

    public void setBuildMode(boolean enabled) {
        toolsBox.setManaged(enabled);
        toolsBox.setVisible(enabled);
    }

    public void setBuildTool(BuildTool tool) {
        ToggleButton button = toolButtons.get(tool);
        if (button != null) {
            toolGroup.selectToggle(button);
        }
    }

    public void setOnToolSelected(Consumer<BuildTool> onToolSelected) {
        this.onToolSelected = onToolSelected;
    }

    public void update(GameState state, int tileSize) {
        positionLabel.setText("Player: (" + state.playerX + ", " + state.playerY + ")");
        stepsLabel.setText("Steps: " + state.steps);
        mapLabel.setText("Map: " + state.mapWidth + "x" + state.mapHeight);
        tileLabel.setText("Tile: " + tileSize + "px");
    }

    private VBox buildToolsBox() {
        Label title = new Label("Build tools");
        title.setTextFill(Color.WHITE);

        ToggleButton wallButton = createToolButton("Wall", BuildTool.WALL);
        ToggleButton emptyButton = createToolButton("Erase", BuildTool.EMPTY);
        ToggleButton portalButton = createToolButton("Portal", BuildTool.PORTAL);
        ToggleButton enemyButton = createToolButton("Enemy", BuildTool.ENEMY);

        VBox box = new VBox(8, title, wallButton, emptyButton, portalButton, enemyButton);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(0, 0, 0, 8));
        return box;
    }

    private void styleLabels(VBox box) {
        box.getChildren().forEach(node -> {
            if (node instanceof Label label) {
                label.setTextFill(Color.WHITE);
            }
        });
    }

    private ToggleButton createToolButton(String label, BuildTool tool) {
        ToggleButton button = new ToggleButton(label);
        button.setToggleGroup(toolGroup);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            if (onToolSelected != null) {
                onToolSelected.accept(tool);
            }
        });
        toolButtons.put(tool, button);
        return button;
    }
}
