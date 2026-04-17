package angel.engine.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class HintBar extends HBox {

    private final Label hint;

    public HintBar() {
        hint = new Label("Move: WASD / Arrows   •   Restart: R");
        hint.getStyleClass().add("engine-hint-label");
        getStyleClass().add("engine-hint-bar");
        setPadding(new Insets(8, 12, 8, 12));
        setMinSize(0, 0);
        HBox.setHgrow(hint, Priority.ALWAYS);
        getChildren().add(hint);
    }

    public void setBuildMode(boolean enabled) {
        if (enabled) {
            hint.setText("Move: WASD / Arrows   •   Restart: R   •   Build: Use tools then click");
        } else {
            hint.setText("Move: WASD / Arrows   •   Restart: R   •   Crafting: E");
        }
    }
}
