package angel.engine.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LevelLoader {
    public static int[][] loadMap(String resourceName) throws Exception {
        return loadLevel(resourceName).map();
    }

    public static int[][] loadMap(Path path) throws Exception {
        return loadLevel(path).map();
    }

    public static LevelData loadLevel(String resourceName) throws Exception {
        ObjectMapper manager = new ObjectMapper();
        InputStream file = LevelLoader.class.getResourceAsStream("/" + resourceName);
        if (file == null) throw new IllegalStateException("Resource is not found! ["+resourceName+"]");

        JsonNode root = manager.readTree(file);
        return parseLevel(root);
    }

    public static LevelData loadLevel(Path path) throws Exception {
        ObjectMapper manager = new ObjectMapper();
        JsonNode root = manager.readTree(path.toFile());
        return parseLevel(root);
    }

    public static void saveMap(Path path, int[][] map) throws Exception {
        saveLevel(path, map, List.of(), null, null);
    }

    public static void saveLevel(Path path, int[][] map, List<GameState.Portal> portals,
                                 Integer spawnX, Integer spawnY) throws Exception {
        ObjectMapper manager = new ObjectMapper();
        ObjectNode root = manager.createObjectNode();
        int height = map.length;
        int width = map[0].length;

        root.put("width", width);
        root.put("height", height);

        ArrayNode walls = manager.createArrayNode();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (map[y][x] == 1) {
                    ArrayNode wall = manager.createArrayNode();
                    wall.add(x);
                    wall.add(y);
                    walls.add(wall);
                }
            }
        }
        root.set("walls", walls);

        ArrayNode portalNodes = manager.createArrayNode();
        for (GameState.Portal portal : portals) {
            ObjectNode portalNode = manager.createObjectNode();
            portalNode.put("x", portal.x());
            portalNode.put("y", portal.y());
            portalNode.put("target", portal.target());
            portalNodes.add(portalNode);
        }
        root.set("portals", portalNodes);

        if (spawnX != null && spawnY != null) {
            ObjectNode spawn = manager.createObjectNode();
            spawn.put("x", spawnX);
            spawn.put("y", spawnY);
            root.set("spawn", spawn);
        }
        manager.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
    }

    private static LevelData parseLevel(JsonNode root) {
        int[][] map = parseMap(root);
        List<GameState.Portal> portals = parsePortals(root);
        Integer[] spawn = parseSpawn(root);
        return new LevelData(map, portals, spawn[0], spawn[1]);
    }

    private static int[][] parseMap(JsonNode root) {
        int width = root.get("width").asInt();
        int height = root.get("height").asInt();
        int[][] map = new int[height][width];
        JsonNode walls = root.get("walls");
        if (walls != null) {
            for (JsonNode wall : walls){
                int x = wall.get(0).asInt();
                int y = wall.get(1).asInt();
                map[y][x] = 1;
            }
        }

        return map;
    }

    private static List<GameState.Portal> parsePortals(JsonNode root) {
        List<GameState.Portal> portals = new ArrayList<>();
        JsonNode portalNodes = root.get("portals");
        if (portalNodes == null) {
            return portals;
        }
        for (JsonNode portalNode : portalNodes) {
            int x = portalNode.get("x").asInt();
            int y = portalNode.get("y").asInt();
            String target = portalNode.get("target").asText();
            portals.add(new GameState.Portal(x, y, target));
        }
        return portals;
    }

    private static Integer[] parseSpawn(JsonNode root) {
        JsonNode spawnNode = root.get("spawn");
        if (spawnNode == null || spawnNode.isNull()) {
            return new Integer[]{null, null};
        }
        JsonNode xNode = spawnNode.get("x");
        JsonNode yNode = spawnNode.get("y");
        if (xNode == null || yNode == null) {
            return new Integer[]{null, null};
        }
        return new Integer[]{xNode.asInt(), yNode.asInt()};
    }

    public record LevelData(int[][] map, List<GameState.Portal> portals, Integer spawnX, Integer spawnY) {
    }
}
