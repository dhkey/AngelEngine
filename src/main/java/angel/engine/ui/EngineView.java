package angel.engine.ui;

import angel.engine.core.Engine;
import angel.engine.core.GameState;
import angel.engine.core.LevelLoader;
import angel.engine.render.MapRenderer;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class EngineView {

    private final Engine engine = new Engine();
    private final MapRenderer renderer = new MapRenderer();

    private GameState state;
    private Canvas canvas;
    private GraphicsContext graphics;
    private int tileSize = 32;
    private boolean showGrid = true;
    private StatusPanel statusPanel;
    private StackPane canvasContainer;
    private boolean buildMode = false;
    private HintBar hintBar;
    private Path levelPath;
    private Runnable onExit;
    private BuildTool buildTool = BuildTool.WALL;
    private boolean allowBuild = false;
    private GameHUD gameHUD;

    public Scene createScene(String mode) throws Exception {
        return createScene(mode, null, null, false);
    }

    public Scene createScene(String mode, Path levelPath) throws Exception {
        return createScene(mode, levelPath, null, false);
    }

    public Scene createScene(String mode, Path levelPath, Runnable onExit) throws Exception {
        return createScene(mode, levelPath, onExit, false);
    }

    public Scene createScene(String mode, Path levelPath, Runnable onExit, boolean allowBuild) throws Exception {
        LevelLoader.LevelData levelData = levelPath == null
                ? LevelLoader.loadLevel("level_1.json")
                : LevelLoader.loadLevel(levelPath);
        state = new GameState(levelData.map(), levelData.portals(), levelData.enemies(), levelData.spawnX(), levelData.spawnY());
        this.levelPath = levelPath;
        this.onExit = onExit;
        this.allowBuild = allowBuild;

        canvas = new Canvas();
        graphics = canvas.getGraphicsContext2D();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8));
        root.setMinSize(0, 0);
        root.getStyleClass().add("engine-stage");

        statusPanel = new StatusPanel();
        statusPanel.setMode(mode);
        statusPanel.setOnToolSelected(this::setBuildTool);
        BorderPane.setMargin(statusPanel, new Insets(8, 8, 8, 8));

        buildMode = false;
        Runnable saveAction = allowBuild && levelPath != null ? this::saveLevel : null;

        ToolbarPanel toolbar = new ToolbarPanel(
                this::restartLevel,
                this::toggleGrid,
                showGrid,
                this::setBuildMode,
                buildMode,
                saveAction,
                onExit,
                allowBuild
        );

        canvasContainer = buildCanvasPane();
        hintBar = new HintBar();
        hintBar.setBuildMode(buildMode);
        statusPanel.setBuildMode(buildMode && allowBuild);
        statusPanel.setBuildTool(buildTool);

        root.setTop(toolbar);
        root.setCenter(canvasContainer);
        root.setRight(statusPanel);
        root.setBottom(hintBar);

        BorderPane.setMargin(toolbar, new Insets(8, 8, 8, 8));
        BorderPane.setMargin(canvasContainer, new Insets(8, 8, 8, 8));
        BorderPane.setMargin(hintBar, new Insets(0, 8, 8, 8));

        Scene scene = new Scene(root, 900, 600);
        scene.setFill(Color.web("#1e2c44"));
        EngineTheme.apply(scene, "engine-stage");
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));

        canvas.widthProperty().addListener((obs, oldValue, newValue) -> render());
        canvas.heightProperty().addListener((obs, oldValue, newValue) -> render());
        canvas.setOnMouseClicked(this::handleBuildClick);
        render();

        return scene;
    }

    private StackPane buildCanvasPane() {
        StackPane container = new StackPane();
        container.getStyleClass().add("engine-canvas-shell");

        container.getChildren().add(canvas);
        StackPane.setAlignment(canvas, Pos.CENTER);
        canvas.widthProperty().bind(container.widthProperty());
        canvas.heightProperty().bind(container.heightProperty());

        gameHUD = new GameHUD();
        container.getChildren().add(gameHUD);
        StackPane.setAlignment(gameHUD, Pos.TOP_LEFT);

        container.setMinSize(0, 0);
        return container;
    }

    private void handleKey(KeyCode code) {
        boolean moved = false;
        if (code == KeyCode.W || code == KeyCode.UP) moved = engine.move(state, 0, -1);
        if (code == KeyCode.S || code == KeyCode.DOWN) moved = engine.move(state, 0, 1);
        if (code == KeyCode.A || code == KeyCode.LEFT) moved = engine.move(state, -1, 0);
        if (code == KeyCode.D || code == KeyCode.RIGHT) moved = engine.move(state, 1, 0);
        if (code == KeyCode.R) restartLevel();
        if (moved) {
            handlePortalStep();
            render();
        }
    }

    private void toggleGrid(boolean enabled) {
        showGrid = enabled;
        render();
    }

    private void setBuildMode(boolean enabled) {
        if (!allowBuild) {
            return;
        }
        buildMode = enabled;
        hintBar.setBuildMode(enabled);
        statusPanel.setBuildMode(enabled);
        render();
    }

    private void setBuildTool(BuildTool tool) {
        buildTool = tool;
        statusPanel.setBuildTool(tool);
    }

    private void saveLevel() {
        if (levelPath == null) {
            return;
        }
        try {
            LevelLoader.saveLevel(levelPath, state.map, state.portals, state.enemies, state.startX, state.startY);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Level saved", ButtonType.OK);
            alert.setHeaderText(null);
            EngineTheme.styleDialog(alert);
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    ex.getMessage() == null ? "Failed to save level" : ex.getMessage(),
                    ButtonType.OK);
            alert.setHeaderText(null);
            EngineTheme.styleDialog(alert);
            alert.showAndWait();
        }
    }

    private void restartLevel() {
        state.resetPlayer();
        render();
    }

    private void handleBuildClick(MouseEvent event) {
        if (!buildMode) {
            return;
        }
        double mouseX = event.getX();
        double mouseY = event.getY();
        double[] transform = computeTransform();
        double scale = transform[0];
        double offsetX = transform[1];
        double offsetY = transform[2];
        if (scale <= 0) {
            return;
        }
        double localX = (mouseX - offsetX) / scale;
        double localY = (mouseY - offsetY) / scale;
        int x = (int) (localX / tileSize);
        int y = (int) (localY / tileSize);
        if (x < 0 || y < 0 || x >= state.mapWidth || y >= state.mapHeight) {
            return;
        }
        if (x == state.playerX && y == state.playerY) {
            return;
        }
        switch (buildTool) {
            case WALL -> {
                state.map[y][x] = 1;
                removeEnemy(x, y);
            }
            case EMPTY -> {
                state.map[y][x] = 0;
                removeEnemy(x, y);
            }
            case ENEMY -> handleEnemyPlacement(x, y);
            case PORTAL -> {
                boolean created = handlePortalPlacement(x, y);
                if (created) {
                    setBuildTool(BuildTool.WALL);
                }
            }
        }
        render();
    }

    private void handleEnemyPlacement(int x, int y) {
        if (state.map[y][x] == 1) { 
            return;
        }
        removeEnemy(x, y);
        state.enemies.add(new GameState.Enemy(x, y, "basic"));
    }

    private void removeEnemy(int x, int y) {
        state.enemies.removeIf(e -> e.x() == x && e.y() == y);
    }

    private boolean handlePortalPlacement(int x, int y) {
        int index = findPortalIndex(x, y);
        if (index >= 0) {
            state.portals.remove(index);
            return true;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create portal");
        dialog.setHeaderText("Target level file (e.g. level_2.json)");
        dialog.setContentText("Target:");
        EngineTheme.styleDialog(dialog);
        return dialog.showAndWait().map(target -> {
            String trimmed = target.trim();
            if (trimmed.isEmpty()) {
                return false;
            }
            state.map[y][x] = 0;
            state.portals.add(new GameState.Portal(x, y, trimmed));
            return true;
        }).orElse(false);
    }

    private void handlePortalStep() {
        int index = findPortalIndex(state.playerX, state.playerY);
        if (index < 0) {
            return;
        }
        if (levelPath == null) {
            return;
        }
        GameState.Portal portal = state.portals.get(index);
        Path currentPath = levelPath;
        Path targetPath = currentPath.getParent().resolve(portal.target());
        if (!Files.exists(targetPath)) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Target level not found: " + portal.target(),
                    ButtonType.OK);
            alert.setHeaderText(null);
            EngineTheme.styleDialog(alert);
            alert.showAndWait();
            return;
        }
        try {
            LevelLoader.LevelData levelData = LevelLoader.loadLevel(targetPath);
            Integer spawnX = levelData.spawnX();
            Integer spawnY = levelData.spawnY();
            int[] linked = findLinkedPortal(levelData, currentPath.getFileName().toString());
            if (linked != null) {
                spawnX = linked[0];
                spawnY = linked[1];
            }
            state = new GameState(levelData.map(), levelData.portals(), levelData.enemies(), spawnX, spawnY);
            levelPath = targetPath;
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    ex.getMessage() == null ? "Failed to load target level" : ex.getMessage(),
                    ButtonType.OK);
            alert.setHeaderText(null);
            EngineTheme.styleDialog(alert);
            alert.showAndWait();
        }
    }

    private void render() {
        double[] transform = computeTransform();
        double scale = transform[0];
        double offsetX = transform[1];
        double offsetY = transform[2];
        if (scale <= 0) {
            return;
        }

        renderer.render(graphics, state, tileSize, showGrid, scale, offsetX, offsetY);
        statusPanel.update(state, tileSize);
        if (gameHUD != null) {
            gameHUD.update(state);
        }
    }

    private double[] computeTransform() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            return new double[]{0, 0, 0};
        }
        double mapPixelWidth = state.mapWidth * tileSize;
        double mapPixelHeight = state.mapHeight * tileSize;
        double scale = Math.min(canvasWidth / mapPixelWidth, canvasHeight / mapPixelHeight);
        double offsetX = (canvasWidth - mapPixelWidth * scale) / 2.0;
        double offsetY = (canvasHeight - mapPixelHeight * scale) / 2.0;
        return new double[]{scale, offsetX, offsetY};
    }

    private int findPortalIndex(int x, int y) {
        for (int i = 0; i < state.portals.size(); i++) {
            GameState.Portal portal = state.portals.get(i);
            if (portal.x() == x && portal.y() == y) {
                return i;
            }
        }
        return -1;
    }

    private int[] findLinkedPortal(LevelLoader.LevelData levelData, String previousLevelName) {
        for (GameState.Portal portal : levelData.portals()) {
            if (portal.target().equals(previousLevelName)) {
                return new int[]{portal.x(), portal.y()};
            }
        }
        return null;
    }
}
