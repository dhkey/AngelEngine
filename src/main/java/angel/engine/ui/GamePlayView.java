package angel.engine.ui;

import angel.engine.core.Engine;
import angel.engine.core.GameState;
import angel.engine.core.LevelLoader;
import angel.engine.render.MapRenderer;
import java.nio.file.Path;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class GamePlayView {

    private final Engine engine = new Engine();
    private final MapRenderer renderer = new MapRenderer();

    private GameState state;
    private Path levelPath;
    private Canvas canvas;
    private GraphicsContext graphics;
    private int tileSize = 32;

    public Scene createScene(Path levelPath) throws Exception {
        this.levelPath = levelPath;
        loadLevel(levelPath, null);

        canvas = new Canvas();
        graphics = canvas.getGraphicsContext2D();

    BorderPane root = new BorderPane();
    root.setPadding(Insets.EMPTY);
    root.setMinSize(0, 0);
    root.setStyle("-fx-background-color: #1f2330;");

        StackPane canvasPane = buildCanvasPane();
    root.setCenter(canvasPane);
    BorderPane.setMargin(canvasPane, Insets.EMPTY);

    Scene scene = new Scene(root, 900, 600);
    scene.setFill(javafx.scene.paint.Color.web("#1f2330"));
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));

        canvas.widthProperty().addListener((obs, oldValue, newValue) -> render());
        canvas.heightProperty().addListener((obs, oldValue, newValue) -> render());
        render();

        return scene;
    }

    private StackPane buildCanvasPane() {
    StackPane container = new StackPane(canvas);
    container.setStyle("-fx-background-color: #1f2330; -fx-background-radius: 0;");
        StackPane.setAlignment(canvas, Pos.CENTER);
        canvas.widthProperty().bind(container.widthProperty());
        canvas.heightProperty().bind(container.heightProperty());
        container.setMinSize(0, 0);
        return container;
    }

    private void handleKey(KeyCode code) {
        boolean moved = false;
        if (code == KeyCode.W || code == KeyCode.UP) moved = engine.move(state, 0, -1);
        if (code == KeyCode.S || code == KeyCode.DOWN) moved = engine.move(state, 0, 1);
        if (code == KeyCode.A || code == KeyCode.LEFT) moved = engine.move(state, -1, 0);
        if (code == KeyCode.D || code == KeyCode.RIGHT) moved = engine.move(state, 1, 0);
        if (code == KeyCode.R) state.resetPlayer();
        if (moved) {
            handlePortalStep();
        }
        render();
    }

    private void handlePortalStep() {
        int index = findPortalIndex(state.playerX, state.playerY);
        if (index < 0 || levelPath == null) {
            return;
        }
        GameState.Portal portal = state.portals.get(index);
        Path targetPath = levelPath.getParent().resolve(portal.target());
        try {
            loadLevel(targetPath, levelPath.getFileName().toString());
            levelPath = targetPath;
        } catch (Exception ex) {
            // ignore load errors for now
        }
    }

    private void loadLevel(Path targetPath, String previousLevelName) throws Exception {
        LevelLoader.LevelData levelData = LevelLoader.loadLevel(targetPath);
        Integer spawnX = levelData.spawnX();
        Integer spawnY = levelData.spawnY();
        if (previousLevelName != null) {
            int[] linked = findLinkedPortal(levelData, previousLevelName);
            if (linked != null) {
                spawnX = linked[0];
                spawnY = linked[1];
            }
        }
        state = new GameState(levelData.map(), levelData.portals(), spawnX, spawnY);
    }

    private int[] findLinkedPortal(LevelLoader.LevelData levelData, String previousLevelName) {
        for (GameState.Portal portal : levelData.portals()) {
            if (portal.target().equals(previousLevelName)) {
                return new int[]{portal.x(), portal.y()};
            }
        }
        return null;
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

    private void render() {
        double[] transform = computeTransform();
        double scale = transform[0];
        double offsetX = transform[1];
        double offsetY = transform[2];
        if (scale <= 0) {
            return;
        }
        renderer.render(graphics, state, tileSize, true, scale, offsetX, offsetY);
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
}
