package angel.engine.core;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public final int mapWidth;
    public final int mapHeight; // height of map
    public final int[][] map;
    public final List<Portal> portals;

    public int playerX; // player x
    public int playerY; // player y
    public int startX;
    public int startY;
    public int steps;

    public GameState(int[][] map) {
        this(map, new ArrayList<>(), null, null);
    }

    public GameState(int[][] map, List<Portal> portals) {
        this(map, portals, null, null);
    }

    public GameState(int[][] map, List<Portal> portals, Integer spawnX, Integer spawnY) {
        this.map = map;
        this.mapWidth = map[0].length;
        this.mapHeight = map.length;
        this.portals = new ArrayList<>(portals);
        int[] spawn = resolveSpawn(spawnX, spawnY);
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

    public void setSpawn(int x, int y) {
        this.startX = x;
        this.startY = y;
        resetPlayer();
    }

    private int[] resolveSpawn(Integer spawnX, Integer spawnY) {
        if (spawnX != null && spawnY != null) {
            if (spawnX >= 0 && spawnY >= 0 && spawnX < mapWidth && spawnY < mapHeight) {
                if (map[spawnY][spawnX] == 0) {
                    return new int[]{spawnX, spawnY};
                }
            }
        }
        return findSpawn();
    }

    public record Portal(int x, int y, String target) {
    }
}
