package angel.engine.ui;

import angel.engine.core.CraftingSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class CraftingEditorView {

    private final Path gameConfigPath;
    private final Runnable onBack;
    private final CraftingSystem craftingSystem = new CraftingSystem();

    private ListView<String> elementListView;
    private ListView<String> recipeListView;
    private Label statusLabel;

    public CraftingEditorView(Path gameConfigPath, Runnable onBack) {
        this.gameConfigPath = gameConfigPath;
        this.onBack = onBack;
    }

    public Scene createScene() {
        loadCraftingData();

        Label title = new Label("Craft editor");
        title.getStyleClass().add("engine-section-title");

        Label subtitle = new Label("Manage craft elements and recipes for this game.");
        subtitle.getStyleClass().add("engine-subtitle");

        elementListView = new ListView<>();
        elementListView.getStyleClass().add("engine-list");
        refreshElements();

        Label elementTitle = new Label("Elements");
        elementTitle.getStyleClass().add("engine-side-title");

        TextField elementIdField = new TextField();
        elementIdField.setPromptText("item id (e.g. iron_ingot)");
        Button addElementButton = new Button("Add element");
        addElementButton.getStyleClass().add("engine-button-secondary");
        addElementButton.setOnAction(e -> addElement(elementIdField));
        Button removeElementButton = new Button("Remove selected");
        removeElementButton.getStyleClass().add("engine-button-secondary");
        removeElementButton.setOnAction(e -> removeSelectedElement());

        HBox elementActions = new HBox(10, addElementButton, removeElementButton);
        HBox elementEditor = new HBox(10, elementIdField, elementActions);
        HBox.setHgrow(elementIdField, Priority.ALWAYS);
        elementActions.setAlignment(Pos.CENTER_RIGHT);

        VBox elementPane = new VBox(10, elementTitle, elementListView, elementEditor);
        elementPane.getStyleClass().add("engine-side-panel");
        elementPane.setPadding(new Insets(12));
        VBox.setVgrow(elementListView, Priority.ALWAYS);

        recipeListView = new ListView<>();
        recipeListView.getStyleClass().add("engine-list");
        refreshRecipes();

        Label recipeTitle = new Label("Recipes");
        recipeTitle.getStyleClass().add("engine-side-title");

        TextField recipeIdField = new TextField();
        recipeIdField.setPromptText("recipe id");
        TextField ingredientsField = new TextField();
        ingredientsField.setPromptText("ingredients: wood:2,stone:1");
        TextField resultField = new TextField();
        resultField.setPromptText("result: pickaxe:1");

        Button addRecipeButton = new Button("Add recipe");
        addRecipeButton.getStyleClass().add("engine-button-secondary");
        addRecipeButton.setOnAction(e -> addRecipe(recipeIdField, ingredientsField, resultField));

        Button removeRecipeButton = new Button("Remove selected");
        removeRecipeButton.getStyleClass().add("engine-button-secondary");
        removeRecipeButton.setOnAction(e -> removeSelectedRecipe());

        VBox recipeEditor = new VBox(8, recipeIdField, ingredientsField, resultField);
        HBox recipeActions = new HBox(10, addRecipeButton, removeRecipeButton);
        recipeActions.setAlignment(Pos.CENTER_RIGHT);

        VBox recipePane = new VBox(10, recipeTitle, recipeListView, recipeEditor, recipeActions);
        recipePane.getStyleClass().add("engine-side-panel");
        recipePane.setPadding(new Insets(12));
        VBox.setVgrow(recipeListView, Priority.ALWAYS);

        HBox content = new HBox(12, elementPane, recipePane);
        HBox.setHgrow(elementPane, Priority.ALWAYS);
        HBox.setHgrow(recipePane, Priority.ALWAYS);
        content.setPrefHeight(420);

        statusLabel = new Label("Craft editor ready");
        statusLabel.getStyleClass().add("engine-detail-label");

        Button saveButton = new Button("Save crafts");
        saveButton.setOnAction(e -> saveCraftingData());

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("engine-button-secondary");
        backButton.setOnAction(e -> onBack.run());

        HBox footer = new HBox(10, saveButton, backButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox shell = new VBox(14, title, subtitle, content, statusLabel, footer);
        shell.setPadding(new Insets(24));
        shell.setMaxWidth(940);
        shell.getStyleClass().add("engine-shell");
        VBox.setVgrow(content, Priority.ALWAYS);

        StackPane root = new StackPane(shell);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("engine-select-root");

        Scene scene = new Scene(root, 1020, 680);
        return EngineTheme.apply(scene, "engine-select-root");
    }

    private void loadCraftingData() {
        craftingSystem.clear();
        try {
            craftingSystem.loadFromGameConfig(gameConfigPath);
            if (craftingSystem.elements().isEmpty()) {
                seedDefaults();
            }
        } catch (IOException ex) {
            seedDefaults();
            setStatus("Using default crafting set (load failed: " + ex.getMessage() + ")", false);
        }
    }

    private void seedDefaults() {
        if (craftingSystem.findElementById("wood").isEmpty()) {
            craftingSystem.registerElement(new CraftingSystem.CraftingElement("wood", "Wood"));
        }
        if (craftingSystem.findElementById("stone").isEmpty()) {
            craftingSystem.registerElement(new CraftingSystem.CraftingElement("stone", "Stone"));
        }
    }

    private void addElement(TextField idField) {
        String rawId = idField.getText() == null ? "" : idField.getText().trim();
        if (rawId.isBlank()) {
            setStatus("Element id cannot be empty", false);
            return;
        }
        try {
            craftingSystem.registerElement(new CraftingSystem.CraftingElement(rawId, formatDisplayName(rawId)));
            refreshElements();
            idField.clear();
            setStatus("Element added: " + rawId, true);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), false);
        }
    }

    private void removeSelectedElement() {
        String selected = elementListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an element first", false);
            return;
        }
        String elementId = selected.split(" :: ")[0];
        try {
            boolean removed = craftingSystem.removeElement(elementId);
            if (!removed) {
                setStatus("Element not found: " + elementId, false);
                return;
            }
            refreshElements();
            setStatus("Element removed: " + elementId, true);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            setStatus(ex.getMessage(), false);
        }
    }

    private void addRecipe(TextField recipeIdField, TextField ingredientsField, TextField resultField) {
        String recipeId = recipeIdField.getText() == null ? "" : recipeIdField.getText().trim();
        String ingredientsText = ingredientsField.getText() == null ? "" : ingredientsField.getText().trim();
        String resultText = resultField.getText() == null ? "" : resultField.getText().trim();
        if (recipeId.isBlank() || ingredientsText.isBlank() || resultText.isBlank()) {
            setStatus("Recipe id, ingredients and result are required", false);
            return;
        }
        try {
            List<CraftingSystem.CraftingStack> ingredients = parseStacks(ingredientsText);
            CraftingSystem.CraftingStack result = parseSingleStack(resultText, "result");
            String description = ingredients.stream()
                    .map(stack -> stack.amount() + "x " + stack.itemId())
                    .collect(Collectors.joining(" + "))
                    + " -> " + result.amount() + "x " + result.itemId();
            craftingSystem.registerRecipe(new CraftingSystem.CraftingRecipe(recipeId, ingredients, result, description));
            refreshRecipes();
            recipeIdField.clear();
            ingredientsField.clear();
            resultField.clear();
            setStatus("Recipe saved: " + recipeId, true);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), false);
        }
    }

    private void removeSelectedRecipe() {
        String selected = recipeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a recipe first", false);
            return;
        }
        int idx = selected.indexOf(" :: ");
        String recipeId = idx > 0 ? selected.substring(0, idx) : selected;
        try {
            boolean removed = craftingSystem.removeRecipe(recipeId);
            if (!removed) {
                setStatus("Recipe not found: " + recipeId, false);
                return;
            }
            refreshRecipes();
            setStatus("Recipe removed: " + recipeId, true);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), false);
        }
    }

    private void saveCraftingData() {
        try {
            craftingSystem.saveToGameConfig(gameConfigPath);
            setStatus("Crafting saved to " + gameConfigPath.getFileName(), true);
        } catch (IOException ex) {
            setStatus("Save failed: " + ex.getMessage(), false);
        }
    }

    private List<CraftingSystem.CraftingStack> parseStacks(String raw) {
        List<CraftingSystem.CraftingStack> stacks = new ArrayList<>();
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            stacks.add(parseSingleStack(trimmed, "ingredient"));
        }
        if (stacks.isEmpty()) {
            throw new IllegalArgumentException("At least one ingredient is required");
        }
        return stacks;
    }

    private CraftingSystem.CraftingStack parseSingleStack(String raw, String label) {
        String[] parts = raw.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid " + label + " format: " + raw + " (expected id:amount)");
        }
        String itemId = parts[0].trim();
        String amountRaw = parts[1].trim();
        if (itemId.isBlank()) {
            throw new IllegalArgumentException(label + " item id cannot be blank");
        }
        int amount;
        try {
            amount = Integer.parseInt(amountRaw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " amount must be a number: " + raw);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException(label + " amount must be positive: " + raw);
        }
        return new CraftingSystem.CraftingStack(itemId, amount);
    }

    private void refreshElements() {
        if (elementListView == null) {
            return;
        }
        List<String> labels = craftingSystem.elements().stream()
                .map(element -> element.itemId() + " :: " + element.displayName())
                .toList();
        elementListView.getItems().setAll(labels);
    }

    private void refreshRecipes() {
        if (recipeListView == null) {
            return;
        }
        List<String> labels = craftingSystem.recipes().stream()
                .map(recipe -> recipe.recipeId() + " :: " + recipe.description())
                .toList();
        recipeListView.getItems().setAll(labels);
    }

    private void setStatus(String message, boolean success) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(message);
        statusLabel.setTextFill(success ? Color.web("#8ee69e") : Color.web("#ff9292"));
    }

    private static String formatDisplayName(String itemId) {
        String[] parts = itemId.split("[_\\-\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase());
            }
        }
        return builder.isEmpty() ? itemId : builder.toString();
    }
}
