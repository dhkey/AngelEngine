package angel.engine.render;

import angel.engine.core.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapRenderer {

    public void render(GraphicsContext graphics, GameState state, int tileSize, boolean showGrid,
                       double scale, double offsetX, double offsetY) {
        graphics.setFill(Color.web("#2c3549"));
        graphics.fillRect(0, 0, graphics.getCanvas().getWidth(), graphics.getCanvas().getHeight());

        graphics.save();
        graphics.translate(offsetX, offsetY);
        graphics.scale(scale, scale);

        for (int y = 0; y < state.mapHeight; y++) {
            for (int x = 0; x < state.mapWidth; x++) {
                if (state.map[y][x] == 1) {
                    graphics.setFill(Color.web("#5b6480"));
                    graphics.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
                if (showGrid) {
                    graphics.setStroke(Color.web("#4d5772"));
                    graphics.strokeRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }

        graphics.setFill(Color.web("#9fdbff"));
        graphics.fillOval(state.playerX * tileSize + 6, state.playerY * tileSize + 6,
                tileSize - 12, tileSize - 12);

    graphics.setFill(Color.web("#cc9aff"));
    for (GameState.Portal portal : state.portals) {
        graphics.fillOval(portal.x() * tileSize + 8, portal.y() * tileSize + 8,
            tileSize - 16, tileSize - 16);
    }

    graphics.setFill(Color.web("#ff6666"));
    for (GameState.Enemy enemy : state.enemies) {
        graphics.fillOval(enemy.x() * tileSize + 6, enemy.y() * tileSize + 6,
            tileSize - 12, tileSize - 12);
    }

    graphics.setFill(Color.web("#ffe769"));
    for (GameState.Projectile p : state.projectiles) {
        graphics.fillOval(p.x * tileSize + 10, p.y * tileSize + 10,
                tileSize - 20, tileSize - 20);
    }

        graphics.restore();
    }
}
