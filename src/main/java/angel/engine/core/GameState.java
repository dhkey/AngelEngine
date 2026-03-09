package angel.engine.core;

public class GameState {
    public final int mapWidth;
    public final int mapHeight; // height of map
    public final int[][] map;

    public int playerX; // player x
    public int playerY; // player y
    public int startX;
    public int startY;
    public int steps;

    public GameState(int[][] map) {
        this.map = map;
        this.mapWidth = map[0].length;
        this.mapHeight = map.length;
        int[] spawn = findSpawn();
        this.startX = spawn[0];
        this.startY = spawn[1];
        resetPlayer();
    }

    private int[] findSpawn() {
        for (int y = 1; y < mapHeight - 1; y++) {
            for (int x = 1; x < mapWidth - 1; x++) {
                if (map[y][x] == 0) {
                    return new int[]{x, y};
                }
            }
        }
        return new int[]{1, 1};
    }

    public void resetPlayer() {
        this.playerX = startX;
        this.playerY = startY;
        this.steps = 0;
    }
}
