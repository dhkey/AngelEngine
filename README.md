# AngelEngine

AngelEngine is a JavaFX-based 2D game editor and player. It supports creating games, managing levels, editing maps, and playing levels with shared runtime logic.

## Project description

The application is centered around two workflows:

1. **Edit mode** for game and level authoring (levels, portals, enemies, crafting definitions).
2. **Play mode** for running levels with movement, combat/projectiles, portal transitions, and crafting.

### Main capabilities

- Create and select games from a local filesystem repository.
- Create, open, reorder, and play levels.
- Edit map walls, spawn, portals, and enemies in the level editor.
- Play levels with keyboard controls and level-to-level portal travel.
- Configure crafting elements/recipes in the Craft Editor (edit flow).
- Use crafting in gameplay with persisted inventory in `game.json`.

## Tech details

### Stack

- **Language:** Java 21
- **UI:** JavaFX (`javafx-controls`, `javafx-graphics`) 21.0.2
- **Serialization:** Jackson Databind 2.17.2
- **Build tool:** Maven
- **Testing:** JUnit 5 (Surefire)

### Build plugins

- `maven-compiler-plugin` 3.12.1
- `javafx-maven-plugin` 0.0.8
- `maven-surefire-plugin` 3.2.5

## Architecture overview

- `angel.engine.Main`: application entry, scene/navigation orchestration.
- `angel.engine.data.GameRepository`: filesystem-backed game catalog (`games/<game>/...`).
- `angel.engine.core.LevelLoader`: JSON load/save boundary for level data.
- `angel.engine.core.GameState`: shared runtime state for editor and gameplay.
- `angel.engine.core.Engine`: core movement/combat/projectile logic.
- `angel.engine.render.MapRenderer`: shared rendering layer.
- `angel.engine.ui.EngineView`: level editor scene.
- `angel.engine.ui.GamePlayView`: gameplay scene.
- `angel.engine.ui.CraftingEditorView`: crafting definition editor.

## Data layout and conventions

### Game storage

- `games/<gameName>/game.json`
- `games/<gameName>/levels/*.json`

`game.json.levels` is authoritative for level order.

### Level JSON schema

- Required: `width`, `height`, `walls`
- Optional: `portals`, `enemies`, `spawn`

Map encoding uses `map[y][x]` with:
- `0 = empty`
- `1 = wall`

### Crafting persistence

- Crafting definitions are stored under `game.json.crafting`.
- Gameplay inventory is stored under `game.json.crafting.inventory`.
- Inventory updates are persisted after successful crafting actions.

## Getting started

### Prerequisites

- JDK 21+
- Maven 3.9+

### Commands

- Build: `mvn clean compile`
- Run tests: `mvn test`
- Run app (main entrypoint): `mvn javafx:run`
- Package artifacts: `mvn clean package`

## Controls (play mode)

- Move: `WASD` / Arrow keys
- Restart player: `R`
- Shoot: `Space`
- Toggle crafting overlay: `E`
