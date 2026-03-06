package angel.engine.core;

public class Engine {

    public void move(GameState state, int dx, int dy) {
        int nx = state.px + dx;
        int ny = state.py + dy;

        if (nx < 0 || ny < 0 || nx >= state.map_width || ny >= state.map_height){
            return;
        }
        if (state.map[ny][nx] == 0){
            state.px = nx;
            state.py = ny;
        }
    }

}
