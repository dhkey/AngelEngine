package angel.engine.core;

public class Engine {
    public boolean move(GameState state, int dx, int dy) {
        int nx = state.playerX + dx;
        int ny = state.playerY + dy;

        if (nx < 0 || ny < 0 || nx >= state.mapWidth || ny >= state.mapHeight) {
            return false;
        }
        if (state.map[ny][nx] == 0) {
            state.playerX = nx;
            state.playerY = ny;
            state.steps += 1;
            return true;
        }
        return false;
    }

}
