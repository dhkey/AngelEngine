package angel.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CraftingSystemTest {

    @TempDir
    Path tempDir;

    @Test
    void craftConsumesIngredientsAndAddsResult() {
        CraftingSystem system = new CraftingSystem();
        system.registerElement(new CraftingSystem.CraftingElement("wood", "Wood"));
        system.registerElement(new CraftingSystem.CraftingElement("plank", "Plank"));
        system.registerRecipe(new CraftingSystem.CraftingRecipe(
                "plank_from_wood",
                List.of(new CraftingSystem.CraftingStack("wood", 2)),
                new CraftingSystem.CraftingStack("plank", 4),
                "2x wood -> 4x plank"
        ));

        CraftingSystem.CraftingInventory inventory = new CraftingSystem.CraftingInventory();
        inventory.setAmount("wood", 5);

        CraftingSystem.CraftingResult result = system.craft("plank_from_wood", inventory);

        assertTrue(result.success());
        assertEquals(4, inventory.amountOf("plank"));
        assertEquals(3, inventory.amountOf("wood"));
    }

    @Test
    void cannotCraftWhenIngredientsMissing() {
        CraftingSystem system = new CraftingSystem();
        system.registerElement(new CraftingSystem.CraftingElement("stone", "Stone"));
        system.registerElement(new CraftingSystem.CraftingElement("pickaxe", "Pickaxe"));
        system.registerRecipe(new CraftingSystem.CraftingRecipe(
                "pickaxe",
                List.of(new CraftingSystem.CraftingStack("stone", 3)),
                new CraftingSystem.CraftingStack("pickaxe", 1),
                ""
        ));

        CraftingSystem.CraftingInventory inventory = new CraftingSystem.CraftingInventory();
        inventory.setAmount("stone", 2);

        assertFalse(system.canCraft("pickaxe", inventory));
        CraftingSystem.CraftingResult result = system.craft("pickaxe", inventory);
        assertFalse(result.success());
        assertEquals(2, inventory.amountOf("stone"));
        assertTrue(result.message().toLowerCase().contains("missing ingredients"));
    }

    @Test
    void loadAndSaveCraftingConfigRoundTrip() throws Exception {
        Path gameConfig = tempDir.resolve("game.json");
        Files.writeString(gameConfig, """
                {
                  "name": "Test Game",
                  "levels": ["level_1.json"]
                }
                """);

        CraftingSystem system = new CraftingSystem();
        system.registerElement(new CraftingSystem.CraftingElement("iron", "Iron Ingot"));
        system.registerElement(new CraftingSystem.CraftingElement("stick", "Stick"));
        system.registerElement(new CraftingSystem.CraftingElement("sword", "Sword"));
        system.registerRecipe(new CraftingSystem.CraftingRecipe(
                "iron_sword",
                List.of(
                        new CraftingSystem.CraftingStack("iron", 2),
                        new CraftingSystem.CraftingStack("stick", 1)
                ),
                new CraftingSystem.CraftingStack("sword", 1),
                "2x iron + 1x stick -> sword"
        ));

        system.saveToGameConfig(gameConfig);

        CraftingSystem loaded = new CraftingSystem();
        loaded.loadFromGameConfig(gameConfig);

        assertTrue(loaded.findElementById("iron").isPresent());
        assertTrue(loaded.findRecipeById("iron_sword").isPresent());
        assertEquals("Sword", loaded.findElementById("sword").orElseThrow().displayName());
    }
}
