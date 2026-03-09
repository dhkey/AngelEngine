package angel.engine.ui;

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LevelSelectView {

    private final String gameName;
    private final List<String> levels;
    private final Consumer<String> onOpenLevel;
    private final Consumer<String> onPlayLevel;
    private final Consumer<List<String>> onReorderLevels;
    private final Runnable onCreateLevel;
    private final Runnable onBack;

    public LevelSelectView(String gameName, List<String> levels, Consumer<String> onOpenLevel,
                           Consumer<String> onPlayLevel, Consumer<List<String>> onReorderLevels,
                           Runnable onCreateLevel, Runnable onBack) {
        this.gameName = gameName;
        this.levels = levels;
        this.onOpenLevel = onOpenLevel;
        this.onPlayLevel = onPlayLevel;
        this.onReorderLevels = onReorderLevels;
        this.onCreateLevel = onCreateLevel;
        this.onBack = onBack;
    }

    public Scene createScene() {
        Label title = new Label("Levels for " + gameName);
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: #f0f6fc;");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(levels);
        listView.setStyle("-fx-background-color: #0d1117; -fx-control-inner-background: #0d1117; -fx-text-fill: #f0f6fc;");
        listView.setPlaceholder(new Label("No levels yet"));

        Button openButton = new Button("Open");
        openButton.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onOpenLevel.accept(selected);
            }
        });

        Button createButton = new Button("Create level");
        createButton.setOnAction(e -> onCreateLevel.run());

    Button upButton = new Button("Up");
    upButton.setOnAction(e -> moveSelection(listView, -1));

    Button downButton = new Button("Down");
    downButton.setOnAction(e -> moveSelection(listView, 1));

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBack.run());

    HBox actions = new HBox(12, openButton, createButton, upButton, downButton, backButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(16, title, listView, actions);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #0d1117;");
        VBox.setVgrow(listView, Priority.ALWAYS);

        return new Scene(layout, 900, 600);
    }

    private void moveSelection(ListView<String> listView, int direction) {
        int index = listView.getSelectionModel().getSelectedIndex();
        int target = index + direction;
        if (index < 0 || target < 0 || target >= listView.getItems().size()) {
            return;
        }
        String current = listView.getItems().get(index);
        listView.getItems().set(index, listView.getItems().get(target));
        listView.getItems().set(target, current);
        listView.getSelectionModel().select(target);
        onReorderLevels.accept(List.copyOf(listView.getItems()));
    }
}
