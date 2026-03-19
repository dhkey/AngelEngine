package angel.engine.core;

import java.util.Iterator;
import java.util.List;

public class Engine {
    public boolean move(GameState state, int dx, int dy) {
        if (dx != 0 || dy != 0) {
            state.lastDirX = dx;
            state.lastDirY = dy;
        }

        int nx = state.playerX + dx;
        int ny = state.playerY + dy;

        if (nx < 0 || ny < 0 || nx >= state.mapWidth || ny >= state.mapHeight) {
            return false;
        }
        
        if (state.map[ny][nx] == 0) {
            
            for (GameState.Enemy e : state.enemies) {
                if (e.x() == nx && e.y() == ny) return false;
            }
            
            state.playerX = nx;
            state.playerY = ny;
            state.steps += 1;
            return true;
        }
        return false;
    }

    public void shoot(GameState state) {
        if (state.lastDirX == 0 && state.lastDirY == 0) return;
        
        double speed = 0.5;
        GameState.Projectile p = new GameState.Projectile(
                state.playerX + 0.5,
                state.playerY + 0.5,
                state.lastDirX * speed,
                state.lastDirY * speed
        );
        state.projectiles.add(p);
    }

    public void updateProjectiles(GameState state) {
        Iterator<GameState.Projectile> it = state.projectiles.iterator();
        while (it.hasNext()) {
            GameState.Projectile p = it.next();
            p.x += p.dx;
            p.y += p.dy;

            
            if (p.x < 0 || p.y < 0 || p.x >= state.mapWidth || p.y >= state.mapHeight) {
                it.remove();
                continue;
            }

            int tx = (int) p.x;
            int ty = (int) p.y;
            
            
            if (state.map[ty][tx] == 1) {
                it.remove();
                continue;
            }

            
            boolean hit = false;
            Iterator<GameState.Enemy> enemyIt = state.enemies.iterator();
            while (enemyIt.hasNext()) {
                GameState.Enemy e = enemyIt.next();
                
                if (e.x() == tx && e.y() == ty) {
                    enemyIt.remove();
                    hit = true;
                    break;
                }
            }
            
            if (hit) {
                it.remove();
            }
        }
    }
}
