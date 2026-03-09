package angel.engine.ui;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;

public class ToolbarPanel extends ToolBar {

    public ToolbarPanel(Runnable onReset, Consumer<Boolean> onGridToggle, boolean gridEnabled,
                        Consumer<Boolean> onBuildToggle, boolean buildEnabled) {
        Button resetButton = new Button("Reset player");
        resetButton.setOnAction(e -> onReset.run());

        CheckBox gridToggle = new CheckBox("Grid");
        gridToggle.setSelected(gridEnabled);
        gridToggle.setOnAction(e -> onGridToggle.accept(gridToggle.isSelected()));

        CheckBox buildToggle = new CheckBox("Build mode");
        buildToggle.setSelected(buildEnabled);
        buildToggle.setOnAction(e -> onBuildToggle.accept(buildToggle.isSelected()));

        getItems().addAll(resetButton, new Separator(), gridToggle, buildToggle);
        setPadding(new Insets(4, 8, 4, 8));
    }
}
