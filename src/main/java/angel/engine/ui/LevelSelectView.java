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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LevelSelectView {

    private final String gameName;
    private final List<String> levels;
    private final Consumer<String> onOpenLevel;
    private final Consumer<String> onPlayLevel;
    private final Consumer<List<String>> onReorderLevels;
    private final Runnable onCreateLevel;
    private final Runnable onEditCrafts;
    private final Runnable onBack;

    public LevelSelectView(String gameName, List<String> levels, Consumer<String> onOpenLevel,
                           Consumer<String> onPlayLevel, Consumer<List<String>> onReorderLevels,
                           Runnable onCreateLevel, Runnable onEditCrafts, Runnable onBack) {
        this.gameName = gameName;
        this.levels = levels;
        this.onOpenLevel = onOpenLevel;
        this.onPlayLevel = onPlayLevel;
        this.onReorderLevels = onReorderLevels;
        this.onCreateLevel = onCreateLevel;
        this.onEditCrafts = onEditCrafts;
        this.onBack = onBack;
    }

    public Scene createScene() {
        Label title = new Label("Levels for " + gameName);
        title.getStyleClass().add("engine-section-title");

        Label subtitle = new Label("Open, reorder, create and play levels.");
        subtitle.getStyleClass().add("engine-subtitle");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(levels);
        listView.getStyleClass().add("engine-list");
        Label placeholder = new Label("No levels yet");
        placeholder.getStyleClass().add("engine-detail-label");
        listView.setPlaceholder(placeholder);

        Button openButton = new Button("Open");
        openButton.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onOpenLevel.accept(selected);
            }
        });

        Button createButton = new Button("Create level");
        createButton.getStyleClass().add("engine-button-secondary");
        createButton.setOnAction(e -> onCreateLevel.run());

        Button editCraftsButton = new Button("Edit crafts");
        editCraftsButton.getStyleClass().add("engine-button-secondary");
        editCraftsButton.setOnAction(e -> onEditCrafts.run());

        Button upButton = new Button("Up");
        upButton.getStyleClass().add("engine-button-secondary");
        upButton.setOnAction(e -> moveSelection(listView, -1));

        Button downButton = new Button("Down");
        downButton.getStyleClass().add("engine-button-secondary");
        downButton.setOnAction(e -> moveSelection(listView, 1));

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("engine-button-secondary");
        backButton.setOnAction(e -> onBack.run());

        HBox actions = new HBox(12, openButton, createButton, editCraftsButton, upButton, downButton, backButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(14, title, subtitle, listView, actions);
        layout.setPadding(new Insets(28));
        layout.setMaxWidth(760);
        layout.getStyleClass().add("engine-shell");
        VBox.setVgrow(listView, Priority.ALWAYS);

        StackPane root = new StackPane(layout);
        root.setPadding(new Insets(36));
        root.getStyleClass().add("engine-select-root");

        Scene scene = new Scene(root, 900, 600);
        return EngineTheme.apply(scene, "engine-select-root");
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
