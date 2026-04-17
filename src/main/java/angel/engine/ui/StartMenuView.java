package angel.engine.ui;

import javafx.geometry.Insets;
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
        title.getStyleClass().add("engine-title");

        Label subtitle = new Label("Build worlds. Tune systems. Launch games.");
        subtitle.getStyleClass().add("engine-subtitle");

        Button createButton = new Button("Create game");
        createButton.setPrefWidth(240);
        createButton.setOnAction(e -> onCreate.run());

        Button editButton = new Button("Edit game");
        editButton.getStyleClass().add("engine-button-secondary");
        editButton.setPrefWidth(240);
        editButton.setOnAction(e -> onEdit.run());

        VBox menu = new VBox(14, title, subtitle, createButton, editButton);
        menu.setAlignment(Pos.CENTER);
        menu.setMaxWidth(500);
        menu.setPadding(new Insets(34));
        menu.getStyleClass().add("engine-shell");

        StackPane root = new StackPane(menu);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("engine-menu-root");

        Scene scene = new Scene(root, 900, 600);
        return EngineTheme.apply(scene, "engine-menu-root");
    }
}
