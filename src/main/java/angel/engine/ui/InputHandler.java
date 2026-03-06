package angel.engine.ui;

import angel.engine.core.Engine;
import angel.engine.core.GameState;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class InputHandler {
    private final Engine engine;
    private final GameState state;
    private final Runnable redrawAction;

    public InputHandler(Engine engine, GameState state, Runnable redrawAction) {
        this.engine = engine;
        this.state = state;
        this.redrawAction = redrawAction;
    }

    public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.W) engine.move(state, 0, -1);
        if (code == KeyCode.S) engine.move(state, 0, 1);
        if (code == KeyCode.A) engine.move(state, -1, 0);
        if (code == KeyCode.D) engine.move(state, 1, 0);
        redrawAction.run();
    }

}
