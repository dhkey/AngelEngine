package angel.engine.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoadErrorView {

    public Scene createScene(String message, Runnable onBack) {
        Label title = new Label("Map load error");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: #f85149;");

        Label details = new Label(message == null ? "Failed to load level" : message);
        details.setStyle("-fx-text-fill: #c9d1d9;");

        Button backButton = new Button("Back to menu");
        backButton.setOnAction(e -> onBack.run());

        VBox pane = new VBox(16, title, details, backButton);
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: #0d1117;");

        return new Scene(new StackPane(pane), 900, 600);
    }
}
