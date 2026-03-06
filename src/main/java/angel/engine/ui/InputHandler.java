package angel.engine.ui;

import angel.engine.core.Engine;
import angel.engine.core.GameState;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class InputHandler {
    private final Engine engine;
    private final GameState state;
    private final Runnable redraw;

    private long lastMoveTime = 0;
    private static final long MOVE_DELAY_MS = 150;

    public InputHandler(Engine engine, GameState state, Runnable redraw) {
        this.engine = engine;
        this.state = state;
        this.redraw = redraw;
    }

    public void handle(KeyEvent event) {
        long now = System.currentTimeMillis();

        if (now - lastMoveTime < MOVE_DELAY_MS) {
            return;
        }
        KeyCode code = event.getCode();
        boolean moved = false;

        if (code == KeyCode.W) moved = engine.move(state, 0, -1);
        if (code == KeyCode.S) moved = engine.move(state, 0, 1);
        if (code == KeyCode.A) moved = engine.move(state, -1, 0);
        if (code == KeyCode.D) moved = engine.move(state, 1, 0);

        if (moved) {
            lastMoveTime = now;
            redraw.run();
        }

    }

}
