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
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: #f0f6fc;");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(games);
        listView.setStyle("-fx-background-color: #0d1117; -fx-control-inner-background: #0d1117; -fx-text-fill: #f0f6fc;");
        listView.setCellFactory(view -> new GameCell(onPlay));
        listView.setPlaceholder(new Label("No games found"));

        Button openButton = new Button("Open");
        openButton.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onSelect.accept(selected);
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBack.run());

        HBox actions = new HBox(12, openButton, backButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(16, title, listView, actions);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #0d1117;");
        VBox.setVgrow(listView, Priority.ALWAYS);

        return new Scene(layout, 900, 600);
    }

    private static class GameCell extends javafx.scene.control.ListCell<String> {
        private final Consumer<String> onPlay;
        private final Label nameLabel;
        private final Button runButton;
        private final HBox layout;

        private GameCell(Consumer<String> onPlay) {
            this.onPlay = onPlay;
            nameLabel = new Label();
            nameLabel.setStyle("-fx-text-fill: #f0f6fc;");
            runButton = new Button("Run game");
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
