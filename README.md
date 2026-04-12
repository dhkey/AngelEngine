# AngelEngine — 2. kontrolní etapa

V repozitáři je připraveno:

1. Kostra kódu
   - deklarace tříd, jejich vlastností a metod,
   - dědičnost a další vazby mezi třídami.

2. Základní technická dokumentace
   - zjednodušený diagram tříd a jejich vztahů,
   - popis stavů hry/aplikace,
   - použité technologie a knihovny.

1) Kostra kódu

#Hlavní třídy a jejich odpovědnosti

- angel.engine.Main — hlavní vstup aplikace (menu + navigace mezi obrazovkami).
- angel.Game — alternativní vstup pro spuštění hry.
- angel.engine.core.Engine — herní logika (pohyb, aktualizace, interakce).
- angel.engine.core.GameState — stav levelu a herních entit.
- angel.engine.core.LevelLoader — načítání/ukládání levelů z/do JSON.
- angel.engine.core.LevelFactory — vytváření základní struktury levelů.
- angel.engine.data.GameRepository — práce s katalogem her a levelů.
- angel.engine.render.MapRenderer — vykreslování mapy a objektů.
- angel.engine.ui.EngineView — editor levelů.
- angel.engine.ui.GamePlayView — herní režim.

#Dědičnost a vztahy mezi třídami

Dědičnost:
- angel.engine.Main extends javafx.application.Application
- angel.Game extends javafx.application.Application
- angel.engine.ui.ToolbarPanel extends javafx.scene.control.ToolBar
- angel.engine.ui.StatusPanel extends javafx.scene.layout.VBox
- angel.engine.ui.HintBar extends javafx.scene.layout.HBox
- angel.engine.ui.GameHUD extends javafx.scene.layout.VBox
- angel.engine.ui.GameSelectView.GameCell extends javafx.scene.control.ListCell<String>

Klíčové vazby (kompozice/závislosti):
- EngineView používá Engine, GameState, MapRenderer, StatusPanel, ToolbarPanel, HintBar, GameHUD.
- GamePlayView používá Engine, LevelLoader, MapRenderer, GameHUD.
- Main propojuje StartMenuView, GameSelectView, LevelSelectView, EngineView, GamePlayView, GameRepository.

---

2) Základní technická dokumentace

#Popis stavů hry/aplikace

1. Start Menu — výběr akce (vytvořit hru / upravit hru).
2. Game Select — výběr existující hry.
3. Level Select — výběr, vytvoření a řazení levelů.
4. Editor Mode — editace levelu (stěny, portály, nepřátelé, ukládání).
5. Play Mode — spuštění levelu a ovládání postavy.
6. Load/Error State — zobrazení chyb při načítání.

#Použité technologie a knihovny

- Java 21
- JavaFX (javafx-controls, javafx-graphics)
- Jackson Databind (JSON)
- Maven (build)
- JUnit 5 (testy)

Konfigurace je v souboru pom.xml.

