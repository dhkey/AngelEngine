package angel.engine;

import angel.engine.core.Engine;
import angel.engine.core.GameState;
import angel.engine.core.LevelLoader;
import angel.engine.ui.InputHandler;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    static final int TILE = 32;

    private Engine engine;
    private GameState state;

    private Canvas canvas;
    private GraphicsContext graphics;

    @Override
    public void start(Stage stage) throws Exception {
        
        int[][] map = LevelLoader.loadMap("level_1.json");
        
        state = new GameState(map);

        engine = new Engine();
        
        canvas = new Canvas(state.map_width * TILE, state.map_height * TILE);
        graphics = canvas.getGraphicsContext2D();

        BorderPane root = new BorderPane(canvas);
        Scene scene = new Scene(root);

        InputHandler inputHandler = new InputHandler(engine, state, this::draw);
        scene.setOnKeyPressed(inputHandler::handle);

        draw();

        stage.setTitle("Angel Engine");
        stage.setScene(scene);
        stage.show();
        root.requestFocus();
    }

    private void draw() {

        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int y = 0; y < state.map_height; y++) {
            for (int x = 0; x < state.map_width; x++) {
                
                if (state.map[y][x] == 1) {
                    graphics.fillRect(x * TILE, y * TILE, TILE, TILE);
                }
                graphics.strokeRect(x * TILE, y * TILE, TILE, TILE);
            }
        }
        
        graphics.fillRect(
                state.px * TILE + 6,
                state.py * TILE + 6,
                TILE - 12,
                TILE - 12
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}