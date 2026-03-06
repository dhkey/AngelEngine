package angel.engine.core;

public class Engine {

    public boolean move(GameState state, int dx, int dy) {
        int nx = state.px + dx;
        int ny = state.py + dy;

        if (nx < 0 || ny < 0 || nx >= state.map_width || ny >= state.map_height) {
            return false;
        }
        if (state.map[ny][nx] == 0) {
            state.px = nx;
            state.py = ny;
            return true;
        }
        return false;
    }
}
