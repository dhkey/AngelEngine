package angel.engine.core;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public final int mapWidth;
    public final int mapHeight; 
    public final int[][] map;
    public final List<Portal> portals;
    public final List<Enemy> enemies;

    public int playerX; 
    public int playerY; 
    public int startX;
    public int startY;
    public int steps;
    public int health;
    public int maxHealth;
    public int lastDirX = 0;
    public int lastDirY = -1; 
    public final List<Projectile> projectiles = new ArrayList<>();

    public GameState(int[][] map) {
        this(map, new ArrayList<>(), new ArrayList<>(), null, null);
    }

    public GameState(int[][] map, List<Portal> portals, Integer spawnX, Integer spawnY) {
        this(map, portals, new ArrayList<>(), spawnX, spawnY);
    }

    public GameState(int[][] map, List<Portal> portals, List<Enemy> enemies, Integer spawnX, Integer spawnY) {
        this.map = map;
        this.mapWidth = map[0].length;
        this.mapHeight = map.length;
        this.portals = new ArrayList<>(portals);
        this.enemies = new ArrayList<>(enemies);
        int[] spawn = resolveSpawn(spawnX, spawnY);
        this.startX = spawn[0];
        this.startY = spawn[1];
        this.maxHealth = 100;
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
        this.health = maxHealth;
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

    public record Enemy(int x, int y, String type) {
    }

    public static class Projectile {
        public double x;
        public double y;
        public double dx;
        public double dy;
        public boolean active = true;

        public Projectile(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }
    }
}
