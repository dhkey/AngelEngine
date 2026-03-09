package angel.engine.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameRepository {

    private final Path gamesRoot;
    private final ObjectMapper mapper;

    public GameRepository(Path gamesRoot) {
        this.gamesRoot = gamesRoot;
        this.mapper = new ObjectMapper();
    }

    public Path getGamesRoot() {
        return gamesRoot;
    }

    public List<String> listGames() throws IOException {
        if (!Files.exists(gamesRoot)) {
            return List.of();
        }
        List<String> games = new ArrayList<>();
        try (var stream = Files.list(gamesRoot)) {
            stream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .forEach(games::add);
        }
        return games;
    }

    public void createGame(String name, String folderName) throws IOException {
        Path gameDir = gamesRoot.resolve(folderName);
        Files.createDirectories(gameDir);
        Files.createDirectories(gameDir.resolve("levels"));
        Path metadata = gameDir.resolve("game.json");
        ObjectNode payload = mapper.createObjectNode();
        payload.put("name", name);
        payload.put("createdAt", java.time.LocalDateTime.now().toString());
        payload.set("levels", mapper.createArrayNode());
        mapper.writerWithDefaultPrettyPrinter().writeValue(metadata.toFile(), payload);
    }

    public List<String> listLevels(String gameName) throws IOException {
        Path levelsDir = gamesRoot.resolve(gameName).resolve("levels");
        if (!Files.exists(levelsDir)) {
            return List.of();
        }
        List<String> ordered = loadLevelOrder(gameName);
        List<String> levels = new ArrayList<>();
        try (var stream = Files.list(levelsDir)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString())
                    .forEach(levels::add);
        }
        if (!ordered.isEmpty()) {
            List<String> result = new ArrayList<>();
            for (String item : ordered) {
                if (levels.remove(item)) {
                    result.add(item);
                }
            }
            levels.sort(Comparator.naturalOrder());
            result.addAll(levels);
            return result;
        }
        levels.sort(Comparator.naturalOrder());
        return levels;
    }

    public Path getLevelPath(String gameName, String levelFile) {
        return gamesRoot.resolve(gameName).resolve("levels").resolve(levelFile);
    }

    public void ensureLevelDir(String gameName) throws IOException {
        Files.createDirectories(gamesRoot.resolve(gameName).resolve("levels"));
    }

    public void saveLevelOrder(String gameName, List<String> order) throws IOException {
        Path metadata = gamesRoot.resolve(gameName).resolve("game.json");
        if (!Files.exists(metadata)) {
            return;
        }
        ObjectNode root = (ObjectNode) mapper.readTree(metadata.toFile());
        ArrayNode array = mapper.createArrayNode();
        order.forEach(array::add);
        root.set("levels", array);
        mapper.writerWithDefaultPrettyPrinter().writeValue(metadata.toFile(), root);
    }

    public List<String> loadLevelOrder(String gameName) throws IOException {
        Path metadata = gamesRoot.resolve(gameName).resolve("game.json");
        if (!Files.exists(metadata)) {
            return List.of();
        }
        JsonNode root = mapper.readTree(metadata.toFile());
        JsonNode levelsNode = root.get("levels");
        if (levelsNode == null || !levelsNode.isArray()) {
            return List.of();
        }
        List<String> order = new ArrayList<>();
        for (JsonNode node : levelsNode) {
            order.add(node.asText());
        }
        return order;
    }

    public void appendLevelOrder(String gameName, String levelFile) throws IOException {
        List<String> order = new ArrayList<>(loadLevelOrder(gameName));
        if (!order.contains(levelFile)) {
            order.add(levelFile);
            saveLevelOrder(gameName, order);
        }
    }
}
