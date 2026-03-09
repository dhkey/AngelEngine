package angel.engine.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class LevelLoaderTest {

    @Test
    void loadsLevelFromResources() throws Exception {
        int[][] map = LevelLoader.loadMap("level_1.json");

        assertNotNull(map);
        assertEquals(15, map.length);
        assertEquals(20, map[0].length);
        assertEquals(1, map[0][0]);
    }
}
