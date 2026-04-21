package angel.engine.ui;

import angel.engine.core.CraftingSystem;
import angel.engine.core.Engine;
import angel.engine.core.GameState;
import angel.engine.core.LevelLoader;
import angel.engine.render.MapRenderer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class GamePlayView {

    private final Engine engine = new Engine();
    private final MapRenderer renderer = new MapRenderer();
    private final CraftingSystem craftingSystem = new CraftingSystem();
    private final CraftingSystem.CraftingInventory craftingInventory = new CraftingSystem.CraftingInventory();

    private GameState state;
    private Path levelPath;
    private Path gameConfigPath;
    private Canvas canvas;
    private GraphicsContext graphics;
    private int tileSize = 32;
    private GameHUD gameHUD;
    private AnimationTimer gameLoop;
    private BorderPane root;

    private StackPane gameplayStack;
    private HBox craftingOverlay;
    private ListView<String> recipeListView;
    private ListView<String> inventoryListView;
    private Label inventoryLabel;
    private Label craftingStatusLabel;
    private final Map<String, String> recipeLabelToId = new LinkedHashMap<>();

    public Scene createScene(Path levelPath) throws Exception {
        Path configPath = levelPath == null ? null : levelPath.getParent().getParent().resolve("game.json");
        return createScene(levelPath, configPath);
    }

    public Scene createScene(Path levelPath, Path gameConfigPath) throws Exception {
        this.levelPath = levelPath;
        this.gameConfigPath = gameConfigPath;
        loadLevel(levelPath, null);
        initializeCrafting(gameConfigPath);

        canvas = new Canvas();
        graphics = canvas.getGraphicsContext2D();

        root = new BorderPane();
        root.setPadding(Insets.EMPTY);
        root.setMinSize(0, 0);
        root.getStyleClass().add("engine-stage");

        StackPane canvasPane = buildCanvasPane();
        gameplayStack = new StackPane(canvasPane, buildCraftingPanel());
        StackPane.setAlignment(craftingOverlay, Pos.CENTER);
        craftingOverlay.setVisible(false);
        craftingOverlay.setManaged(false);
        root.setCenter(gameplayStack);
        BorderPane.setMargin(gameplayStack, Insets.EMPTY);

        Scene scene = new Scene(root, 900, 600);
        scene.setFill(Color.web("#1e2c44"));
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));
        EngineTheme.apply(scene, "engine-stage");

        canvas.widthProperty().addListener((obs, oldValue, newValue) -> render());
        canvas.heightProperty().addListener((obs, oldValue, newValue) -> render());

        startGameLoop();
        return scene;
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
    }

    private void update() {
        if (state != null) {
            engine.updateProjectiles(state);
        }
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

    private Node buildCraftingPanel() {
        Label title = new Label("Crafting");
        title.getStyleClass().add("crafting-overlay-title");

        Label recipeTitle = new Label("Recipes");
        recipeTitle.getStyleClass().add("engine-side-value");

        recipeListView = new ListView<>();
        recipeListView.getStyleClass().add("engine-list");
        recipeListView.setPrefHeight(96);

        Button craftButton = new Button("Craft selected");
        craftButton.setOnAction(e -> craftSelectedRecipe());
        Button saveCraftingButton = new Button("Save crafting");
        saveCraftingButton.getStyleClass().add("engine-button-secondary");
        saveCraftingButton.setOnAction(e -> saveCraftingConfig());

        Label inventoryTitle = new Label("Inventory");
        inventoryTitle.getStyleClass().add("crafting-overlay-title");
        inventoryListView = new ListView<>();
        inventoryListView.getStyleClass().add("engine-list");
        inventoryListView.setPrefHeight(236);

        inventoryLabel = new Label();
        inventoryLabel.getStyleClass().add("engine-detail-label");
        craftingStatusLabel = new Label("Crafting ready");
        craftingStatusLabel.getStyleClass().add("engine-detail-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(10, craftButton, saveCraftingButton, spacer, inventoryLabel);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox leftPanel = new VBox(
                8,
                title,
                recipeTitle,
                recipeListView,
                footer,
                craftingStatusLabel
        );
        leftPanel.setPadding(new Insets(10, 12, 10, 12));
        leftPanel.getStyleClass().add("crafting-overlay-left");
        leftPanel.setPrefWidth(520);

        VBox rightPanel = new VBox(8, inventoryTitle, inventoryListView);
        rightPanel.setPadding(new Insets(10, 12, 10, 12));
        rightPanel.getStyleClass().add("crafting-overlay-right");
        rightPanel.setPrefWidth(260);

        craftingOverlay = new HBox(14, leftPanel, rightPanel);
        craftingOverlay.setPadding(new Insets(18));
        craftingOverlay.setMaxWidth(900);
        craftingOverlay.setMouseTransparent(false);

        refreshRecipeList();
        refreshInventoryLabel();
        return craftingOverlay;
    }

    private void craftSelectedRecipe() {
        String selected = recipeListView == null ? null : recipeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setCraftingStatus("Select a recipe first", false);
            return;
        }
        String recipeId = recipeLabelToId.get(selected);
        if (recipeId == null) {
            setCraftingStatus("Selected recipe is invalid", false);
            return;
        }
        CraftingSystem.CraftingResult craftResult = craftingSystem.craft(recipeId, craftingInventory);
        if (craftResult.success()) {
            String saveError = saveInventorySnapshot();
            if (saveError == null) {
                setCraftingStatus(craftResult.message(), true);
            } else {
                setCraftingStatus(craftResult.message() + " (inventory save failed: " + saveError + ")", false);
            }
        } else {
            setCraftingStatus("Not enough resources", false);
        }
        refreshInventoryLabel();
    }

    private void saveCraftingConfig() {
        if (gameConfigPath == null) {
            setCraftingStatus("No game config to save crafting", false);
            return;
        }
        try {
            craftingSystem.saveToGameConfig(gameConfigPath, craftingInventory);
            setCraftingStatus("Crafting saved to " + gameConfigPath.getFileName(), true);
        } catch (IOException ex) {
            setCraftingStatus("Failed to save crafting: " + ex.getMessage(), false);
        }
    }

    private void refreshRecipeList() {
        if (recipeListView == null) {
            return;
        }
        recipeLabelToId.clear();
        List<String> labels = new ArrayList<>();
        for (CraftingSystem.CraftingRecipe recipe : craftingSystem.recipes()) {
            String ingredients = recipe.ingredients().stream()
                    .map(stack -> stack.amount() + "x " + stack.itemId())
                    .reduce((left, right) -> left + " + " + right)
                    .orElse("");
            String label = recipe.recipeId() + " :: " + ingredients + " -> "
                    + recipe.result().amount() + "x " + recipe.result().itemId();
            labels.add(label);
            recipeLabelToId.put(label, recipe.recipeId());
        }
        recipeListView.getItems().setAll(labels);
        if (!labels.isEmpty()) {
            recipeListView.getSelectionModel().select(0);
        }
    }

    private void refreshInventoryLabel() {
        if (inventoryLabel == null) {
            return;
        }
        List<String> items = craftingInventory.items().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
        String value = items.isEmpty() ? "empty" : String.join(" | ", items);
        inventoryLabel.setText("Inventory: " + value);
        if (inventoryListView != null) {
            inventoryListView.getItems().setAll(items.isEmpty() ? List.of("empty") : items);
        }
    }

    private void setCraftingStatus(String message, boolean success) {
        if (craftingStatusLabel == null) {
            return;
        }
        craftingStatusLabel.setText(message);
        craftingStatusLabel.setTextFill(success ? Color.web("#8ee69e") : Color.web("#ff9292"));
    }

    private void toggleCraftingPanel() {
        if (craftingOverlay == null) {
            return;
        }
        boolean newVisible = !craftingOverlay.isVisible();
        craftingOverlay.setVisible(newVisible);
        craftingOverlay.setManaged(newVisible);
    }

    private void initializeCrafting(Path configPath) {
        craftingSystem.clear();
        craftingInventory.clear();

        craftingSystem.registerElement(new CraftingSystem.CraftingElement("wood", "Wood"));
        craftingSystem.registerElement(new CraftingSystem.CraftingElement("stone", "Stone"));
        craftingSystem.registerElement(new CraftingSystem.CraftingElement("plank", "Plank"));
        craftingSystem.registerElement(new CraftingSystem.CraftingElement("pickaxe", "Pickaxe"));

        craftingSystem.registerRecipe(new CraftingSystem.CraftingRecipe(
                "plank_from_wood",
                List.of(new CraftingSystem.CraftingStack("wood", 2)),
                new CraftingSystem.CraftingStack("plank", 4),
                "2x wood -> 4x plank"
        ));
        craftingSystem.registerRecipe(new CraftingSystem.CraftingRecipe(
                "pickaxe_from_plank_stone",
                List.of(
                        new CraftingSystem.CraftingStack("plank", 2),
                        new CraftingSystem.CraftingStack("stone", 3)
                ),
                new CraftingSystem.CraftingStack("pickaxe", 1),
                "2x plank + 3x stone -> 1x pickaxe"
        ));

        boolean inventoryLoaded = false;
        if (configPath != null) {
            try {
                craftingSystem.loadFromGameConfig(configPath);
                inventoryLoaded = craftingSystem.loadInventoryFromGameConfig(configPath, craftingInventory);
            } catch (IOException ex) {
                setCraftingStatus("Failed to load crafting config: " + ex.getMessage(), false);
            }
        }
        if (!inventoryLoaded) {
            setDefaultStartingInventory();
        }
    }

    private String saveInventorySnapshot() {
        if (gameConfigPath == null) {
            return null;
        }
        try {
            craftingSystem.saveInventoryToGameConfig(gameConfigPath, craftingInventory);
            return null;
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    private void setDefaultStartingInventory() {
        craftingInventory.clear();
        craftingInventory.setAmount("wood", 6);
        craftingInventory.setAmount("stone", 6);
    }

    private void handleKey(KeyCode code) {
        boolean moved = false;
        if (code == KeyCode.W || code == KeyCode.UP) moved = engine.move(state, 0, -1);
        if (code == KeyCode.S || code == KeyCode.DOWN) moved = engine.move(state, 0, 1);
        if (code == KeyCode.A || code == KeyCode.LEFT) moved = engine.move(state, -1, 0);
        if (code == KeyCode.D || code == KeyCode.RIGHT) moved = engine.move(state, 1, 0);
        if (code == KeyCode.R) state.resetPlayer();
        if (code == KeyCode.SPACE) engine.shoot(state);
        if (code == KeyCode.E) toggleCraftingPanel();

        if (moved) {
            handlePortalStep();
        }
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
            // Keep current level if linked level cannot be loaded.
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
        renderer.render(graphics, state, tileSize, false, scale, offsetX, offsetY);
        if (gameHUD != null) {
            gameHUD.update(state);
        }
        refreshInventoryLabel();
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
