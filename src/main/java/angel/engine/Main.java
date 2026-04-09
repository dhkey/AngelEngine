package angel.engine;

import angel.engine.core.LevelFactory;
import angel.engine.core.LevelLoader;
import angel.engine.data.GameRepository;
import angel.engine.ui.EngineView;
import angel.engine.ui.GamePlayView;
import angel.engine.ui.GameSelectView;
import angel.engine.ui.LevelSelectView;
import angel.engine.ui.LoadErrorView;
import angel.engine.ui.StartMenuView;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    private final EngineView engineView = new EngineView();
    private final GamePlayView gamePlayView = new GamePlayView();
    private final GameRepository gameRepository = new GameRepository(
        Paths.get(System.getProperty("user.dir"), "games"));

    @Override
    public void start(Stage stage) throws Exception {
        List<String> args = getParameters().getRaw();
        if (!args.isEmpty()) {
            String gameName = args.get(0);
            if (Files.exists(gameRepository.getGamesRoot().resolve(gameName))) {
                handlePlayGame(gameName);
                return;
            }
        }
        showStartMenu(stage);
    }

    private void showStartMenu(Stage stage) {
        StartMenuView startMenu = new StartMenuView(
                () -> handleCreateGame(stage),
                () -> showGameSelect(stage)
        );
        stage.setTitle("Angel Engine");
        stage.setScene(startMenu.createScene());
        stage.show();
    }

    private void handleCreateGame(Stage stage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create game");
        dialog.setHeaderText("New game name");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String name = result.get().trim();
        if (name.isBlank()) {
            showAlert("Invalid name", "Please enter a game name.");
            return;
        }

        String folderName = name.replaceAll("[^a-zA-Z0-9_-]+", "_");
        if (folderName.isBlank()) {
            showAlert("Invalid name", "Game name contains no usable characters.");
            return;
        }

        Path gameDir = gameRepository.getGamesRoot().resolve(folderName);
        if (Files.exists(gameDir)) {
            showAlert("Game exists", "A game with this name already exists.");
            return;
        }

        try {
            gameRepository.createGame(name, folderName);
        } catch (IOException ex) {
            showAlert("Create failed", ex.getMessage() == null ? "Unable to create game." : ex.getMessage());
            return;
        }

        showLevelSelect(stage, folderName, true);
    }

    private void showGameSelect(Stage stage) {
        List<String> games;
        try {
            games = gameRepository.listGames();
        } catch (IOException ex) {
            showAlert("Load failed", ex.getMessage() == null ? "Unable to read games." : ex.getMessage());
            games = List.of();
        }
        GameSelectView view = new GameSelectView(games,
                game -> showLevelSelect(stage, game, false),
                game -> handlePlayGame(game),
                () -> showStartMenu(stage));
        stage.setTitle("Angel Engine - Select game");
        stage.setScene(view.createScene());
        stage.show();
    }

    private void handlePlayGame(String gameName) {
        List<String> levels;
        try {
            levels = gameRepository.listLevels(gameName);
        } catch (IOException ex) {
            showAlert("Load failed", ex.getMessage() == null ? "Unable to read levels." : ex.getMessage());
            return;
        }
        if (levels.isEmpty()) {
            showAlert("No levels", "This game has no levels.");
            return;
        }
        String levelFile = levels.get(0);
        Path levelPath = gameRepository.getLevelPath(gameName, levelFile);
        try {
            Stage playStage = new Stage();
            Scene scene = gamePlayView.createScene(levelPath);
            playStage.setTitle("Angel Engine - Play: " + gameName);
            playStage.setScene(scene);
            playStage.show();
        } catch (Exception ex) {
            LoadErrorView errorView = new LoadErrorView();
            Stage errorStage = new Stage();
            errorStage.setTitle("Angel Engine - Play error");
            errorStage.setScene(errorView.createScene(ex.getMessage(), errorStage::close));
            errorStage.show();
        }
    }

    private void showLevelSelect(Stage stage, String gameName, boolean createFlow) {
        List<String> levels;
        try {
            levels = gameRepository.listLevels(gameName);
        } catch (IOException ex) {
            showAlert("Load failed", ex.getMessage() == null ? "Unable to read levels." : ex.getMessage());
            levels = List.of();
        }
    LevelSelectView view = new LevelSelectView(gameName,
        levels,
        level -> openLevel(stage, gameName, level, true),
        level -> openPlayWindow(gameName, level),
        order -> saveLevelOrder(gameName, order),
        () -> handleCreateLevel(stage, gameName),
                () -> {
                    if (createFlow) {
                        showStartMenu(stage);
                    } else {
                        showGameSelect(stage);
                    }
                });
        stage.setTitle("Angel Engine - Levels");
        stage.setScene(view.createScene());
        stage.show();
    }

    private void handleCreateLevel(Stage stage, String gameName) {
        Dialog<LevelSpec> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create level");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField widthField = new TextField("20");
        TextField heightField = new TextField("15");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Width:"), 0, 1);
        grid.add(widthField, 1, 1);
        grid.add(new Label("Height:"), 0, 2);
        grid.add(heightField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            String name = nameField.getText().trim();
            if (name.isBlank()) {
                return null;
            }
            try {
                int width = Integer.parseInt(widthField.getText().trim());
                int height = Integer.parseInt(heightField.getText().trim());
                if (width < 5 || height < 5) {
                    return null;
                }
                return new LevelSpec(name, width, height);
            } catch (NumberFormatException ex) {
                return null;
            }
        });

        Optional<LevelSpec> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        LevelSpec spec = result.get();
        String levelFileName = spec.name.replaceAll("[^a-zA-Z0-9_-]+", "_");
        if (levelFileName.isBlank()) {
            showAlert("Invalid name", "Please use a different level name.");
            return;
        }

        Path levelDir = gameRepository.getGamesRoot().resolve(gameName).resolve("levels");
        Path levelPath = levelDir.resolve(levelFileName + ".json");
        if (Files.exists(levelPath)) {
            showAlert("Level exists", "A level with this name already exists.");
            return;
        }

        int[][] map = LevelFactory.createEmptyMap(spec.width, spec.height);
        try {
            gameRepository.ensureLevelDir(gameName);
            LevelLoader.saveLevel(levelPath, map, List.of(), 1, 1);
            gameRepository.appendLevelOrder(gameName, levelFileName + ".json");
        } catch (Exception ex) {
            showAlert("Create failed", ex.getMessage() == null ? "Unable to create level." : ex.getMessage());
            return;
        }

        openLevel(stage, gameName, levelFileName + ".json", true);
    }

    private void openLevel(Stage stage, String gameName, String levelFile, boolean editMode) {
        Path levelPath = gameRepository.getLevelPath(gameName, levelFile);
        String mode = editMode ? "Edit game: " + gameName : "Play game: " + gameName;
        try {
            Scene scene = engineView.createScene(mode, levelPath,
                    () -> showLevelSelect(stage, gameName, false),
                    editMode);
            stage.setTitle("Angel Engine - " + mode);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            LoadErrorView errorView = new LoadErrorView();
            stage.setScene(errorView.createScene(ex.getMessage(), () -> showLevelSelect(stage, gameName, false)));
            stage.show();
        }
    }

    private void openPlayWindow(String gameName, String levelFile) {
        Path levelPath = gameRepository.getLevelPath(gameName, levelFile);
        try {
            Stage playStage = new Stage();
            Scene scene = gamePlayView.createScene(levelPath);
            playStage.setTitle("Angel Engine - Play: " + gameName);
            playStage.setScene(scene);
            playStage.show();
        } catch (Exception ex) {
            LoadErrorView errorView = new LoadErrorView();
            Stage errorStage = new Stage();
            errorStage.setTitle("Angel Engine - Play error");
            errorStage.setScene(errorView.createScene(ex.getMessage(), errorStage::close));
            errorStage.show();
        }
    }

    private void initEngineScene(Stage stage, String mode) {
        try {
            Scene scene = engineView.createScene(mode);
            stage.setTitle("Angel Engine - " + mode);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            LoadErrorView errorView = new LoadErrorView();
            stage.setScene(errorView.createScene(ex.getMessage(), () -> showStartMenu(stage)));
            stage.show();
        }
    }

    private void saveLevelOrder(String gameName, List<String> order) {
        try {
            gameRepository.saveLevelOrder(gameName, order);
        } catch (IOException ex) {
            showAlert("Save failed", ex.getMessage() == null ? "Unable to save level order." : ex.getMessage());
        }
    }

    private record LevelSpec(String name, int width, int height) {}

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
