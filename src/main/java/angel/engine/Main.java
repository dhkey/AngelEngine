package angel.engine;

import angel.engine.ui.EngineView;
import angel.engine.ui.LoadErrorView;
import angel.engine.ui.StartMenuView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private final EngineView engineView = new EngineView();

    @Override
    public void start(Stage stage) throws Exception {
        showStartMenu(stage);
    }

    private void showStartMenu(Stage stage) {
        StartMenuView startMenu = new StartMenuView(mode -> initEngineScene(stage, mode));
        stage.setTitle("Angel Engine");
        stage.setScene(startMenu.createScene());
        stage.show();
    }

    private void initEngineScene(Stage stage, String mode) {
        try {
            Scene scene = engineView.createScene(mode);
            stage.setTitle("Angel Engine - " + mode);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            LoadErrorView errorView = new LoadErrorView();
            stage.setScene(errorView.createScene(ex.getMessage(), () -> showStartMenu(stage)));
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
