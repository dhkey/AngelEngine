package angel.engine.core;

public class LevelFactory {

    private LevelFactory() {
    }

    public static int[][] createEmptyMap(int width, int height) {
        int[][] map = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    map[y][x] = 1;
                }
            }
        }
        return map;
    }
}
