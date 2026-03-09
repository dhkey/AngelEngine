package angel.engine.render;

import angel.engine.core.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapRenderer {

    public void render(GraphicsContext graphics, GameState state, int tileSize, boolean showGrid,
                       double scale, double offsetX, double offsetY) {
        graphics.setFill(Color.web("#1f2330"));
        graphics.fillRect(0, 0, graphics.getCanvas().getWidth(), graphics.getCanvas().getHeight());

        graphics.save();
        graphics.translate(offsetX, offsetY);
        graphics.scale(scale, scale);

        for (int y = 0; y < state.mapHeight; y++) {
            for (int x = 0; x < state.mapWidth; x++) {
                if (state.map[y][x] == 1) {
                    graphics.setFill(Color.web("#3e445e"));
                    graphics.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
                if (showGrid) {
                    graphics.setStroke(Color.web("#2f344d"));
                    graphics.strokeRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }

        graphics.setFill(Color.web("#8ecae6"));
        graphics.fillOval(state.playerX * tileSize + 6, state.playerY * tileSize + 6,
                tileSize - 12, tileSize - 12);

    graphics.setFill(Color.web("#b084f5"));
    for (GameState.Portal portal : state.portals) {
        graphics.fillOval(portal.x() * tileSize + 8, portal.y() * tileSize + 8,
            tileSize - 16, tileSize - 16);
    }

        graphics.restore();
    }
}
