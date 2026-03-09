package angel.engine.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class StartMenuView {

    private final Runnable onCreate;
    private final Runnable onEdit;

    public StartMenuView(Runnable onCreate, Runnable onEdit) {
        this.onCreate = onCreate;
        this.onEdit = onEdit;
    }

    public Scene createScene() {
        Label title = new Label("Angel Engine");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: #f0f6fc;");

        Button createButton = new Button("Create game");
        createButton.setPrefWidth(220);
    createButton.setOnAction(e -> onCreate.run());

        Button editButton = new Button("Edit game");
        editButton.setPrefWidth(220);
    editButton.setOnAction(e -> onEdit.run());

        VBox menu = new VBox(16, title, createButton, editButton);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: #0d1117;");

        return new Scene(new StackPane(menu), 900, 600);
    }
}
