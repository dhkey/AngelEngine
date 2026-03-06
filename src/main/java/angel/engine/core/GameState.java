package angel.engine.core;

public class GameState {
    public int map_width;
    public int map_height; // height of map
    public int[][] map;

    public int px = 2; //player x
    public int py = 2; //player y

    public GameState(int[][] map) {
        this.map = map;
        this.map_width = map[0].length;
        this.map_height = map.length;
    }
}
