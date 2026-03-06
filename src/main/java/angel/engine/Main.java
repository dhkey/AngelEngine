package angel.engine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application{

    static final int TILE = 32;
    static final int WIDTH = 20;
    static final int HEIGHT = 15;

    int px = 2, py = 2;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH * TILE, HEIGHT * TILE);
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        BorderPane root = new BorderPane(canvas);
        Scene scene = new Scene(root);

        drag(graphics);

        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.W) py --;
            if (code == KeyCode.S) py ++;
            if (code == KeyCode.A) px --;
            if (code == KeyCode.D) px ++;
            drag(graphics);
        });

        stage.setTitle("Angel Engine");
        stage.setScene(scene);
        stage.show();
    }

    void drag(GraphicsContext graphics) {
        graphics.clearRect(0, 0, WIDTH * TILE, HEIGHT * TILE);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                graphics.strokeRect(x * TILE, y * TILE, TILE, TILE);
            }
        }

        graphics.fillRect(px * TILE + 6, py * TILE + 6, TILE - 12, TILE - 12);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
