package angel.engine.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoadErrorView {

    public Scene createScene(String message, Runnable onBack) {
        Label title = new Label("Map load error");
        title.getStyleClass().add("engine-error-title");

        Label details = new Label(message == null ? "Failed to load level" : message);
        details.getStyleClass().add("engine-detail-label");

        Button backButton = new Button("Back to menu");
        backButton.getStyleClass().add("engine-button-danger");
        backButton.setOnAction(e -> onBack.run());

        VBox pane = new VBox(16, title, details, backButton);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(28));
        pane.setMaxWidth(620);
        pane.getStyleClass().add("engine-shell");

        StackPane root = new StackPane(pane);
        root.setPadding(new Insets(36));
        root.getStyleClass().add("engine-error-root");

        Scene scene = new Scene(root, 900, 600);
        return EngineTheme.apply(scene, "engine-error-root");
    }
}
