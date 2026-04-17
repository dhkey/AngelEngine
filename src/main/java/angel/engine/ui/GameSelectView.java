package angel.engine.ui;

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameSelectView {

    private final List<String> games;
    private final Consumer<String> onSelect;
    private final Consumer<String> onPlay;
    private final Runnable onBack;

    public GameSelectView(List<String> games, Consumer<String> onSelect, Consumer<String> onPlay, Runnable onBack) {
        this.games = games;
        this.onSelect = onSelect;
        this.onPlay = onPlay;
        this.onBack = onBack;
    }

    public Scene createScene() {
        Label title = new Label("Select game");
        title.getStyleClass().add("engine-section-title");

        Label subtitle = new Label("Choose a project to edit or launch.");
        subtitle.getStyleClass().add("engine-subtitle");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(games);
        listView.getStyleClass().add("engine-list");
        listView.setCellFactory(view -> new GameCell(onPlay));
        Label placeholder = new Label("No games found");
        placeholder.getStyleClass().add("engine-detail-label");
        listView.setPlaceholder(placeholder);

        Button openButton = new Button("Open");
        openButton.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onSelect.accept(selected);
            }
        });

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("engine-button-secondary");
        backButton.setOnAction(e -> onBack.run());

        HBox actions = new HBox(12, openButton, backButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(14, title, subtitle, listView, actions);
        layout.setPadding(new Insets(28));
        layout.setMaxWidth(700);
        layout.getStyleClass().add("engine-shell");
        VBox.setVgrow(listView, Priority.ALWAYS);

        StackPane root = new StackPane(layout);
        root.setPadding(new Insets(36));
        root.getStyleClass().add("engine-select-root");

        Scene scene = new Scene(root, 900, 600);
        return EngineTheme.apply(scene, "engine-select-root");
    }

    private static class GameCell extends ListCell<String> {
        private final Consumer<String> onPlay;
        private final Label nameLabel;
        private final Button runButton;
        private final HBox layout;

        private GameCell(Consumer<String> onPlay) {
            this.onPlay = onPlay;
            nameLabel = new Label();
            nameLabel.getStyleClass().add("engine-detail-label");
            runButton = new Button("Run game");
            runButton.getStyleClass().add("engine-button-secondary");
            runButton.setOnAction(e -> {
                String item = getItem();
                if (item != null) {
                    onPlay.accept(item);
                }
            });
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            layout = new HBox(12, nameLabel, spacer, runButton);
            layout.setAlignment(Pos.CENTER_LEFT);
            layout.setPadding(new Insets(4, 6, 4, 6));
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                nameLabel.setText(item);
                setGraphic(layout);
            }
        }
    }
}
