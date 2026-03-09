package angel.engine.ui;

import angel.engine.core.Engine;
import angel.engine.core.GameState;
import angel.engine.core.LevelLoader;
import angel.engine.render.MapRenderer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

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

    public Scene createScene(String mode) throws Exception {
        int[][] map = LevelLoader.loadMap("level_1.json");
        state = new GameState(map);

        canvas = new Canvas();
        graphics = canvas.getGraphicsContext2D();

    BorderPane root = new BorderPane();
    root.setPadding(new Insets(8));
    root.setMinSize(0, 0);

        statusPanel = new StatusPanel();
        statusPanel.setMode(mode);
        BorderPane.setMargin(statusPanel, new Insets(8, 8, 8, 8));

    buildMode = mode.toLowerCase().contains("edit");

    ToolbarPanel toolbar = new ToolbarPanel(
        this::resetLevel,
        this::toggleGrid,
        showGrid,
        this::setBuildMode,
        buildMode
    );

        canvasContainer = buildCanvasPane();
    hintBar = new HintBar();
    hintBar.setBuildMode(buildMode);

        root.setTop(toolbar);
        root.setCenter(canvasContainer);
        root.setRight(statusPanel);
        root.setBottom(hintBar);

        BorderPane.setMargin(toolbar, new Insets(8, 8, 8, 8));
        BorderPane.setMargin(canvasContainer, new Insets(8, 8, 8, 8));
        BorderPane.setMargin(hintBar, new Insets(0, 8, 8, 8));

        Scene scene = new Scene(root, 900, 600);
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));

    canvas.widthProperty().addListener((obs, oldValue, newValue) -> render());
    canvas.heightProperty().addListener((obs, oldValue, newValue) -> render());
    canvas.setOnMouseClicked(event -> handleBuildClick(event.getX(), event.getY()));
    render();

        return scene;
    }

    private StackPane buildCanvasPane() {
        StackPane container = new StackPane(canvas);
        container.setStyle("-fx-background-color: #1f2330; -fx-background-radius: 8;");
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
        if (code == KeyCode.R) resetLevel();
        if (moved) render();
    }

    private void toggleGrid(boolean enabled) {
        showGrid = enabled;
        render();
    }

    private void setBuildMode(boolean enabled) {
        buildMode = enabled;
        hintBar.setBuildMode(enabled);
        render();
    }

    private void resetLevel() {
        state.resetPlayer();
        render();
    }

    private void handleBuildClick(double mouseX, double mouseY) {
        if (!buildMode) {
            return;
        }
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
        state.map[y][x] = state.map[y][x] == 1 ? 0 : 1;
        render();
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
