package angel.engine.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class HintBar extends HBox {

    private final Label hint;

    public HintBar() {
    hint = new Label("Move: WASD / Arrows   •   Restart: R");
        hint.setTextFill(Color.web("#c9d1d9"));
        setPadding(new Insets(8, 12, 8, 12));
        setStyle("-fx-background-color: #161b22; -fx-background-radius: 8;");
        setMinSize(0, 0);
        HBox.setHgrow(hint, Priority.ALWAYS);
        getChildren().add(hint);
    }

    public void setBuildMode(boolean enabled) {
        if (enabled) {
            hint.setText("Move: WASD / Arrows   •   Restart: R   •   Build: Use tools then click");
        } else {
            hint.setText("Move: WASD / Arrows   •   Restart: R");
        }
    }
}
