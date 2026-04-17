package angel.engine.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CraftingSystem {

    private final Map<String, CraftingElement> elementsById;
    private final Map<String, CraftingRecipe> recipesById;

    public CraftingSystem() {
        this.elementsById = new LinkedHashMap<>();
        this.recipesById = new LinkedHashMap<>();
    }

    public CraftingSystem(List<CraftingRecipe> recipes) {
        this();
        if (recipes == null) {
            return;
        }
        for (CraftingRecipe recipe : recipes) {
            registerRecipe(recipe);
        }
    }

    public void clear() {
        elementsById.clear();
        recipesById.clear();
    }

    public void registerElement(CraftingElement element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        String itemId = normalizeId(element.itemId());
        if (itemId.isBlank()) {
            throw new IllegalArgumentException("Element id cannot be blank");
        }
        String displayName = element.displayName() == null || element.displayName().isBlank()
                ? itemId
                : element.displayName().trim();
        elementsById.put(itemId, new CraftingElement(itemId, displayName));
    }

    public List<CraftingElement> elements() {
        return List.copyOf(elementsById.values());
    }

    public Optional<CraftingElement> findElementById(String itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(elementsById.get(normalizeId(itemId)));
    }

    public boolean removeElement(String itemId) {
        String normalized = normalizeId(itemId);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Element id cannot be blank");
        }
        if (!elementsById.containsKey(normalized)) {
            return false;
        }
        for (CraftingRecipe recipe : recipesById.values()) {
            if (recipe.result().itemId().equals(normalized)) {
                throw new IllegalStateException("Element is used as result in recipe: " + recipe.recipeId());
            }
            for (CraftingStack ingredient : recipe.ingredients()) {
                if (ingredient.itemId().equals(normalized)) {
                    throw new IllegalStateException("Element is used in recipe: " + recipe.recipeId());
                }
            }
        }
        elementsById.remove(normalized);
        return true;
    }

    public void registerRecipe(CraftingRecipe recipe) {
        CraftingRecipe normalized = normalizeRecipe(recipe);
        for (CraftingStack ingredient : normalized.ingredients()) {
            ensureElementExists(ingredient.itemId());
        }
        ensureElementExists(normalized.result().itemId());
        recipesById.put(normalized.recipeId(), normalized);
    }

    public List<CraftingRecipe> recipes() {
        return List.copyOf(recipesById.values());
    }

    public Optional<CraftingRecipe> findRecipeById(String recipeId) {
        if (recipeId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(recipesById.get(normalizeId(recipeId)));
    }

    public boolean removeRecipe(String recipeId) {
        String normalized = normalizeId(recipeId);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Recipe id cannot be blank");
        }
        return recipesById.remove(normalized) != null;
    }

    public boolean canCraft(String recipeId, CraftingInventory inventory) {
        if (inventory == null) {
            return false;
        }
        Optional<CraftingRecipe> recipe = findRecipeById(recipeId);
        if (recipe.isEmpty()) {
            return false;
        }
        for (CraftingStack ingredient : recipe.get().ingredients()) {
            if (inventory.amountOf(ingredient.itemId()) < ingredient.amount()) {
                return false;
            }
        }
        return true;
    }

    public CraftingResult craft(String recipeId, CraftingInventory inventory) {
        if (inventory == null) {
            return CraftingResult.failure("Inventory is not initialized");
        }
        Optional<CraftingRecipe> recipeOpt = findRecipeById(recipeId);
        if (recipeOpt.isEmpty()) {
            return CraftingResult.failure("Recipe not found: " + recipeId);
        }
        CraftingRecipe recipe = recipeOpt.get();
        if (!canCraft(recipe.recipeId(), inventory)) {
            return CraftingResult.failure("Missing ingredients for recipe: " + recipe.recipeId());
        }
        for (CraftingStack ingredient : recipe.ingredients()) {
            int currentAmount = inventory.amountOf(ingredient.itemId());
            inventory.setAmount(ingredient.itemId(), currentAmount - ingredient.amount());
        }
        CraftingStack output = recipe.result();
        int existing = inventory.amountOf(output.itemId());
        inventory.setAmount(output.itemId(), existing + output.amount());
        return CraftingResult.success("Crafted " + output.itemId(), output);
    }

    public void saveToGameConfig(Path gameConfigPath) throws IOException {
        if (gameConfigPath == null) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root;
        if (Files.exists(gameConfigPath)) {
            JsonNode existingRoot = mapper.readTree(gameConfigPath.toFile());
            root = existingRoot instanceof ObjectNode objectNode
                    ? objectNode
                    : mapper.createObjectNode();
        } else {
            root = mapper.createObjectNode();
        }

        ObjectNode craftingNode = mapper.createObjectNode();
        ArrayNode elementNodes = mapper.createArrayNode();
        for (CraftingElement element : elementsById.values()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("itemId", element.itemId());
            node.put("displayName", element.displayName());
            elementNodes.add(node);
        }
        craftingNode.set("elements", elementNodes);

        ArrayNode recipeNodes = mapper.createArrayNode();
        for (CraftingRecipe recipe : recipesById.values()) {
            ObjectNode recipeNode = mapper.createObjectNode();
            recipeNode.put("recipeId", recipe.recipeId());
            recipeNode.put("description", recipe.description());

            ArrayNode ingredientNodes = mapper.createArrayNode();
            for (CraftingStack ingredient : recipe.ingredients()) {
                ObjectNode ingredientNode = mapper.createObjectNode();
                ingredientNode.put("itemId", ingredient.itemId());
                ingredientNode.put("amount", ingredient.amount());
                ingredientNodes.add(ingredientNode);
            }
            recipeNode.set("ingredients", ingredientNodes);

            ObjectNode resultNode = mapper.createObjectNode();
            resultNode.put("itemId", recipe.result().itemId());
            resultNode.put("amount", recipe.result().amount());
            recipeNode.set("result", resultNode);

            recipeNodes.add(recipeNode);
        }
        craftingNode.set("recipes", recipeNodes);
        root.set("crafting", craftingNode);

        mapper.writerWithDefaultPrettyPrinter().writeValue(gameConfigPath.toFile(), root);
    }

    public void loadFromGameConfig(Path gameConfigPath) throws IOException {
        if (gameConfigPath == null || !Files.exists(gameConfigPath)) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(gameConfigPath.toFile());
        JsonNode crafting = root.get("crafting");
        if (crafting == null || crafting.isNull()) {
            return;
        }

        JsonNode elements = crafting.get("elements");
        if (elements != null && elements.isArray()) {
            for (JsonNode elementNode : elements) {
                String itemId = text(elementNode, "itemId");
                if (itemId == null || itemId.isBlank()) {
                    continue;
                }
                String displayName = text(elementNode, "displayName");
                registerElement(new CraftingElement(itemId, displayName == null ? itemId : displayName));
            }
        }

        JsonNode recipes = crafting.get("recipes");
        if (recipes != null && recipes.isArray()) {
            for (JsonNode recipeNode : recipes) {
                String recipeId = text(recipeNode, "recipeId");
                if (recipeId == null || recipeId.isBlank()) {
                    continue;
                }
                JsonNode ingredientsNode = recipeNode.get("ingredients");
                JsonNode resultNode = recipeNode.get("result");
                if (ingredientsNode == null || !ingredientsNode.isArray() || resultNode == null) {
                    continue;
                }
                List<CraftingStack> ingredients = new ArrayList<>();
                for (JsonNode ingredientNode : ingredientsNode) {
                    String ingredientId = text(ingredientNode, "itemId");
                    int amount = intValue(ingredientNode, "amount", 1);
                    if (ingredientId == null || ingredientId.isBlank() || amount <= 0) {
                        continue;
                    }
                    ingredients.add(new CraftingStack(ingredientId, amount));
                }
                String resultId = text(resultNode, "itemId");
                int resultAmount = intValue(resultNode, "amount", 1);
                if (resultId == null || resultId.isBlank() || resultAmount <= 0 || ingredients.isEmpty()) {
                    continue;
                }
                String description = text(recipeNode, "description");
                registerRecipe(new CraftingRecipe(
                        recipeId,
                        ingredients,
                        new CraftingStack(resultId, resultAmount),
                        description == null ? "" : description
                ));
            }
        }
    }

    private CraftingRecipe normalizeRecipe(CraftingRecipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        String recipeId = normalizeId(recipe.recipeId());
        if (recipeId.isBlank()) {
            throw new IllegalArgumentException("Recipe id cannot be blank");
        }
        if (recipe.ingredients() == null || recipe.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must have ingredients");
        }
        List<CraftingStack> ingredients = new ArrayList<>();
        for (CraftingStack stack : recipe.ingredients()) {
            ingredients.add(normalizeStack(stack, "ingredient"));
        }
        CraftingStack result = normalizeStack(recipe.result(), "result");
        String description = recipe.description() == null ? "" : recipe.description().trim();
        return new CraftingRecipe(recipeId, List.copyOf(ingredients), result, description);
    }

    private CraftingStack normalizeStack(CraftingStack stack, String fieldName) {
        if (stack == null) {
            throw new IllegalArgumentException("Recipe " + fieldName + " cannot be null");
        }
        String itemId = normalizeId(stack.itemId());
        if (itemId.isBlank()) {
            throw new IllegalArgumentException("Recipe " + fieldName + " item id cannot be blank");
        }
        if (stack.amount() <= 0) {
            throw new IllegalArgumentException("Recipe " + fieldName + " amount must be positive");
        }
        return new CraftingStack(itemId, stack.amount());
    }

    private void ensureElementExists(String itemId) {
        if (!elementsById.containsKey(itemId)) {
            registerElement(new CraftingElement(itemId, itemId));
        }
    }

    private static String normalizeId(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String text(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }

    private static int intValue(JsonNode node, String field, int fallback) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return fallback;
        }
        return fieldNode.asInt(fallback);
    }

    public record CraftingElement(String itemId, String displayName) {
        public CraftingElement {
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(displayName, "displayName");
        }
    }

    public record CraftingStack(String itemId, int amount) {
        public CraftingStack {
            Objects.requireNonNull(itemId, "itemId");
        }
    }

    public record CraftingRecipe(String recipeId, List<CraftingStack> ingredients,
                                 CraftingStack result, String description) {
        public CraftingRecipe {
            Objects.requireNonNull(recipeId, "recipeId");
            Objects.requireNonNull(ingredients, "ingredients");
            Objects.requireNonNull(result, "result");
            description = description == null ? "" : description;
        }
    }

    public static final class CraftingInventory {
        private final Map<String, Integer> items = new LinkedHashMap<>();

        public int amountOf(String itemId) {
            String normalized = normalizeId(itemId);
            return items.getOrDefault(normalized, 0);
        }

        public void setAmount(String itemId, int amount) {
            String normalized = normalizeId(itemId);
            if (normalized.isBlank()) {
                throw new IllegalArgumentException("Inventory item id cannot be blank");
            }
            if (amount <= 0) {
                items.remove(normalized);
                return;
            }
            items.put(normalized, amount);
        }

        public Map<String, Integer> items() {
            return Map.copyOf(items);
        }

        public void clear() {
            items.clear();
        }
    }

    public record CraftingResult(boolean success, String message, CraftingStack craftedItem) {

        public static CraftingResult success(String message, CraftingStack craftedItem) {
            return new CraftingResult(true, message, craftedItem);
        }

        public static CraftingResult failure(String message) {
            return new CraftingResult(false, message, null);
        }
    }
}
