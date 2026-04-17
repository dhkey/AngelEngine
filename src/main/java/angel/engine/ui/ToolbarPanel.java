package angel.engine.ui;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;

public class ToolbarPanel extends ToolBar {

    public ToolbarPanel(Runnable onRestart,
                        Consumer<Boolean> onGridToggle, boolean gridEnabled,
                        Consumer<Boolean> onBuildToggle, boolean buildEnabled,
                        Runnable onSave, Runnable onExit, boolean showBuildToggle) {
        getStyleClass().add("engine-toolbar");

        Button restartButton = new Button("Restart level");
        restartButton.setOnAction(e -> onRestart.run());
        CheckBox gridToggle = new CheckBox("Grid");
        gridToggle.setSelected(gridEnabled);
        gridToggle.setOnAction(e -> onGridToggle.accept(gridToggle.isSelected()));

        CheckBox buildToggle = new CheckBox("Build mode");
        buildToggle.setSelected(buildEnabled);
        buildToggle.setOnAction(e -> onBuildToggle.accept(buildToggle.isSelected()));

        if (onExit != null) {
            Button exitButton = new Button("Back");
            exitButton.getStyleClass().add("engine-button-secondary");
            exitButton.setOnAction(e -> onExit.run());
            getItems().add(exitButton);
        }

        getItems().add(restartButton);

        if (onSave != null) {
            Button saveButton = new Button("Save level");
            saveButton.getStyleClass().add("engine-button-secondary");
            saveButton.setOnAction(e -> onSave.run());
            getItems().add(saveButton);
        }

        getItems().add(new Separator());
        getItems().add(gridToggle);
        if (showBuildToggle) {
            getItems().add(buildToggle);
        }
        setPadding(new Insets(4, 8, 4, 8));
    }
}
