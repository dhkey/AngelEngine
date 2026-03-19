package angel;

import angel.engine.ui.GamePlayView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Game extends Application {

    @Override
    public void start(Stage stage) {
        try {
            
            Path gameConfig = Paths.get("game.json");
            File gameFile = gameConfig.toFile();
            
            
            if (!gameFile.exists()) {
                Path devPath = Paths.get("games", "TESTPG", "game.json");
                if (devPath.toFile().exists()) {
                    gameConfig = devPath;
                    gameFile = devPath.toFile();
                }
            }

            if (!gameFile.exists()) {
                 showError("Game configuration not found: " + gameConfig.toAbsolutePath());
                 return;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(gameFile);
            
            if (!root.has("levels") || root.get("levels").size() == 0) {
                showError("No levels defined in game.json");
                return;
            }
            
            String firstLevel = root.get("levels").get(0).asText();
            
            
            Path gameDir = gameConfig.getParent();
            Path levelPath;
            if (gameDir != null) {
                levelPath = gameDir.resolve("levels").resolve(firstLevel);
            } else {
                levelPath = Paths.get("levels", firstLevel);
            }
            
            GamePlayView gameView = new GamePlayView();
            Scene scene = gameView.createScene(levelPath);
            
            stage.setTitle(root.has("name") ? root.get("name").asText() : "Angel Game");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to start game: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
