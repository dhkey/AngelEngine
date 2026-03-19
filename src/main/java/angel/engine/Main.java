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
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
        game -> handleBuildGame(game),
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

    private void handleBuildGame(String gameName) {
        String mvnPath = findMavenExecutable();
        if (mvnPath == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Maven executable (mvn) not found. Please locate it manually.",
                    ButtonType.OK, ButtonType.CANCEL);
            alert.setHeaderText("Build Requirement Missing");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Locate Maven Executable (mvn)");
            File mvnFile = fileChooser.showOpenDialog(null);
            if (mvnFile == null) {
                return;
            }
            mvnPath = mvnFile.getAbsolutePath();
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Export Directory");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory == null) {
            return;
        }

        final String finalMvnPath = mvnPath;
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Building game package for " + gameName + ". This may take a few minutes...",
                ButtonType.OK);
        alert.setHeaderText(null);
        alert.show();

        new Thread(() -> {
            boolean success = false;
            try {
                ProcessBuilder builder;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    builder = new ProcessBuilder("cmd", "/c", finalMvnPath, "clean", "package", "dependency:copy-dependencies");
                } else {
                    builder = new ProcessBuilder("sh", "-c", "\"" + finalMvnPath + "\" clean package dependency:copy-dependencies");
                }
                
                builder.directory(Paths.get(System.getProperty("user.dir")).toFile());
                builder.inheritIO();
                Process process = builder.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    Path destDir = selectedDirectory.toPath().resolve(gameName);
                    Files.createDirectories(destDir);
                    
                    Path libDir = destDir.resolve("lib");
                    Path gamesDir = destDir.resolve("games");
                    Path gameDir = gamesDir.resolve(gameName);

                    Files.createDirectories(libDir);
                    Files.createDirectories(gamesDir);

                    Path sourceJar = Paths.get("target", "AngelEngine-1.0-SNAPSHOT.jar");
                    if (Files.exists(sourceJar)) {
                        Files.copy(sourceJar, destDir.resolve("AngelEngine.jar"), StandardCopyOption.REPLACE_EXISTING);
                    }

                    Path sourceLibs = Paths.get("target", "dependency");
                    if (Files.exists(sourceLibs)) {
                        try (Stream<Path> stream = Files.list(sourceLibs)) {
                            stream.forEach(path -> {
                                try {
                                    Files.copy(path, libDir.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }

                    Path sourceGame = gameRepository.getGamesRoot().resolve(gameName);
                    copyDirectory(sourceGame, gameDir);
                    createRunScripts(destDir, gameName);

                    success = true;
                    
                    final Path finalDestDir = destDir;
                    Platform.runLater(() -> {
                         alert.close();
                         showAlert("Build complete",
                                 "Game exported to " + finalDestDir.toAbsolutePath());
                    });
                    return; 
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                success = false;
            }

            boolean finalSuccess = success;
            Platform.runLater(() -> {
                if (alert.isShowing()) alert.close();
                if (!finalSuccess) {
                    showAlert("Build failed",
                            "Unable to export game. Check terminal output.");
                }
            });
        }).start();
    }

    private String findMavenExecutable() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            
            String m2Home = System.getenv("M2_HOME");
            if (m2Home != null) {
                Path p = Paths.get(m2Home, "bin", "mvn.cmd");
                if (Files.exists(p)) return p.toAbsolutePath().toString();
            }
            String mavenHome = System.getenv("MAVEN_HOME");
            if (mavenHome != null) {
                Path p = Paths.get(mavenHome, "bin", "mvn.cmd");
                if (Files.exists(p)) return p.toAbsolutePath().toString();
            }
            
            
            String pathEnv = System.getenv("PATH");
            if (pathEnv != null) {
                for (String part : pathEnv.split(File.pathSeparator)) {
                    Path p = Paths.get(part, "mvn.cmd");
                    if (Files.exists(p)) return p.toAbsolutePath().toString();
                }
            }
            return "mvn.cmd";
        }

        
        String[] specificPaths = {
            "/usr/bin/mvn",
            "/usr/local/bin/mvn",
            "/opt/homebrew/bin/mvn",
            "/opt/local/bin/mvn",
            System.getProperty("user.home") + "/bin/mvn",
            System.getProperty("user.home") + "/.sdkman/candidates/maven/current/bin/mvn"
        };
        
        for (String path : specificPaths) {
            if (Files.isExecutable(Paths.get(path))) {
                return path;
            }
        }
        
        
        String m2Home = System.getenv("M2_HOME");
        if (m2Home != null) {
            Path p = Paths.get(m2Home, "bin", "mvn");
            if (Files.isExecutable(p)) return p.toAbsolutePath().toString();
        }
        
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null) {
            Path p = Paths.get(mavenHome, "bin", "mvn");
            if (Files.isExecutable(p)) return p.toAbsolutePath().toString();
        }

        
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            for (String part : pathEnv.split(File.pathSeparator)) {
                Path p = Paths.get(part, "mvn");
                if (Files.isExecutable(p)) {
                    return p.toAbsolutePath().toString();
                }
            }
        }
        
        
        return null;
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            Path targetPath = target.resolve(source.relativize(sourcePath));
            try {
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createRunScripts(Path destDir, String gameName) throws IOException {
        Path batchFile = destDir.resolve("run.bat");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(batchFile))) {
            writer.println("@echo off");
            writer.println("setlocal");
            writer.println("set \"GAME_NAME=" + gameName + "\"");
            writer.println("set \"CP=AngelEngine.jar;lib/*\"");
            writer.println("java -cp \"%CP%\" angel.engine.Launcher \"%GAME_NAME%\"");
            writer.println("endlocal");
        }

        Path shellFile = destDir.resolve("run.sh");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(shellFile))) {
            writer.println("#!/bin/sh");
            writer.println("GAME_NAME=\"" + gameName + "\"");
            writer.println("CP=\"AngelEngine.jar:lib/*\"");
            writer.println("java -cp \"$CP\" angel.engine.Launcher \"$GAME_NAME\"");
        }
        shellFile.toFile().setExecutable(true);
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
