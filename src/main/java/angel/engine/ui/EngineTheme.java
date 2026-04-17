package angel.engine.ui;

import java.net.URL;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public final class EngineTheme {

    private static final String STYLESHEET_PATH = "/angel/engine/ui/engine-theme.css";
    private static final String STYLESHEET_URL = resolveStylesheetUrl();

    private EngineTheme() {
    }

    public static Scene apply(Scene scene, String... rootStyleClasses) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene cannot be null");
        }
        if (!scene.getStylesheets().contains(STYLESHEET_URL)) {
            scene.getStylesheets().add(STYLESHEET_URL);
        }
        if (scene.getRoot() == null) {
            return scene;
        }
        if (!scene.getRoot().getStyleClass().contains("engine-root")) {
            scene.getRoot().getStyleClass().add("engine-root");
        }
        if (rootStyleClasses == null) {
            return scene;
        }
        for (String styleClass : rootStyleClasses) {
            if (styleClass == null || styleClass.isBlank()) {
                continue;
            }
            if (!scene.getRoot().getStyleClass().contains(styleClass)) {
                scene.getRoot().getStyleClass().add(styleClass);
            }
        }
        return scene;
    }

    public static void styleDialog(Dialog<?> dialog) {
        if (dialog == null) {
            return;
        }
        DialogPane pane = dialog.getDialogPane();
        if (pane == null) {
            return;
        }
        if (!pane.getStylesheets().contains(STYLESHEET_URL)) {
            pane.getStylesheets().add(STYLESHEET_URL);
        }
        if (!pane.getStyleClass().contains("engine-dialog")) {
            pane.getStyleClass().add("engine-dialog");
        }
    }

    private static String resolveStylesheetUrl() {
        URL resource = EngineTheme.class.getResource(STYLESHEET_PATH);
        if (resource == null) {
            throw new IllegalStateException("Missing theme stylesheet: " + STYLESHEET_PATH);
        }
        return resource.toExternalForm();
    }
}
