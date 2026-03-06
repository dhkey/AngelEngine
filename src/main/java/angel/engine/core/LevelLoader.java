package angel.engine.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class LevelLoader {
    public static int[][] loadMap(String resourceName) throws Exception {
        ObjectMapper manager = new ObjectMapper();
        InputStream file = LevelLoader.class.getResourceAsStream("/" + resourceName);
        if (file == null) throw new IllegalStateException("Resource is not found! ["+resourceName+"]");

        JsonNode root = manager.readTree(file);
        int width = root.get("width").asInt();
        int height = root.get("height").asInt();
        int[][] map = new int[height][width];
        JsonNode walls = root.get("walls");
        for (JsonNode wall : walls){
            int x = wall.get(0).asInt();
            int y = wall.get(1).asInt();
            map[y][x] = 1;
        }

        return map;
    }
}
