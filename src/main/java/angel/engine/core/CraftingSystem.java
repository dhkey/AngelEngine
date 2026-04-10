package angel.engine.core;

import java.util.List;
import java.util.Optional;

public class CraftingSystem {

    public CraftingSystem() {
    }

    public CraftingSystem(List<CraftingRecipe> recipes) {
        // TODO: store elements
    }

    public void registerRecipe(CraftingRecipe recipe) {
        // TODO: implement recipe registration.
    }

    public List<CraftingRecipe> recipes() {
        // TODO: return registered recipes.
        return List.of();
    }

    public Optional<CraftingRecipe> findRecipeById(String recipeId) {
        // TODO: implement recipe lookup by id.
        return Optional.empty();
    }

    public boolean canCraft(String recipeId, CraftingInventory inventory) {
        // TODO: implement ingredient checks.
        return false;
    }

    public CraftingResult craft(String recipeId, CraftingInventory inventory) {
        // TODO: implement craft flow (consume ingredients + return result).
        return CraftingResult.notImplemented("Crafting system skeleton: not implemented yet");
    }

    public interface CraftingService {

        List<CraftingRecipe> recipes();

        Optional<CraftingRecipe> findRecipeById(String recipeId);

        boolean canCraft(String recipeId, CraftingInventory inventory);

        CraftingResult craft(String recipeId, CraftingInventory inventory);
    }

    public record CraftingStack(String itemId, int amount) { }

    public record CraftingRecipe(String recipeId, List<CraftingStack> ingredients,
                                 CraftingStack result, String description) { }

    public static final class CraftingInventory {

        public int amountOf(String itemId) {
            // TODO: read item amount from inventory storage.
            return 0;
        }

        public void setAmount(String itemId, int amount) {
            // TODO: persist item amount in inventory storage.
        }
    }

    public record CraftingResult(boolean success, String message, CraftingStack craftedItem) {

        public static CraftingResult notImplemented(String message) {
            return new CraftingResult(false, message, null);
        }
    }
}
