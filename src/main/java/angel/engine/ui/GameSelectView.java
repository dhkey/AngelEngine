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
    private final Consumer<String> onBuild;
    private final Runnable onBack;

    public GameSelectView(List<String> games, Consumer<String> onSelect, Consumer<String> onPlay,
                          Consumer<String> onBuild, Runnable onBack) {
        this.games = games;
        this.onSelect = onSelect;
        this.onPlay = onPlay;
        this.onBuild = onBuild;
        this.onBack = onBack;
    }

    public Scene createScene() {
        Label title = new Label("Select game");
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: #f0f6fc;");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(games);
        listView.setStyle("-fx-background-color: #0d1117; -fx-control-inner-background: #0d1117; -fx-text-fill: #f0f6fc;");
    listView.setCellFactory(view -> new GameCell(onPlay, onBuild));
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
        private final Consumer<String> onBuild;
        private final Label nameLabel;
        private final Button playButton;
        private final Button buildButton;
        private final HBox layout;

        private GameCell(Consumer<String> onPlay, Consumer<String> onBuild) {
            this.onPlay = onPlay;
            this.onBuild = onBuild;
            nameLabel = new Label();
            nameLabel.setStyle("-fx-text-fill: #f0f6fc;");
            playButton = new Button("Play");
            playButton.setOnAction(e -> {
                String item = getItem();
                if (item != null) {
                    onPlay.accept(item);
                }
            });
            buildButton = new Button("Build");
            buildButton.setOnAction(e -> {
                String item = getItem();
                if (item != null) {
                    onBuild.accept(item);
                }
            });
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            layout = new HBox(12, nameLabel, spacer, playButton, buildButton);
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
