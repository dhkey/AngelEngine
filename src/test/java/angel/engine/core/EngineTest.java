package angel.engine.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EngineTest {

    @Test
    void moveBlocksWallsAndBounds() {
        int[][] map = {
                {1, 1, 1},
                {1, 0, 1},
                {1, 1, 1}
        };
        GameState state = new GameState(map);
        Engine engine = new Engine();

        assertFalse(engine.move(state, -1, 0));
        assertFalse(engine.move(state, 1, 0));
        assertFalse(engine.move(state, 0, -1));
        assertFalse(engine.move(state, 0, 1));
    }

    @Test
    void moveAllowsFreeTiles() {
        int[][] map = {
                {1, 1, 1, 1},
                {1, 0, 0, 1},
                {1, 1, 1, 1}
        };
        GameState state = new GameState(map);
        Engine engine = new Engine();

        assertTrue(engine.move(state, 1, 0));
        assertTrue(engine.move(state, -1, 0));
    }
}
