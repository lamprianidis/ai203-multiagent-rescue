package environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MapIO {
    private static final String DIR = "maps";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<String> listSavedMapNames() {
        File dir = new File(DIR);
        List<String> names = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles((d, n) -> n.endsWith(".json"))) {
                names.add(f.getName().replaceFirst("\\.json$", ""));
            }
        }
        return names;
    }

    public static void saveMap(String name, Cell.CellType[][] grid) throws Exception {
        new File(DIR).mkdirs();
        try (FileWriter fw = new FileWriter(DIR + "/" + name + ".json")) {
            GSON.toJson(new MapData(grid), fw);
        }
    }

    public static GridEnvironment loadMap(String name) {
        try (FileReader fr = new FileReader(DIR + "/" + name + ".json")) {
            MapData md = GSON.fromJson(fr, MapData.class);
            int W = md.grid.length, H = md.grid[0].length;
            GridEnvironment env = new GridEnvironment(W, H);
            int exitId = 0;
            for (int x = 0; x < W; x++) {
                for (int y = 0; y < H; y++) {
                    switch (md.grid[x][y]) {
                        case WALL -> env.defineWall(x, y);
                        case OBSTACLE -> env.defineObstacle(x, y);
                        case EXIT -> {
                            env.defineExit(x, y, exitId);
                            exitId++;
                        }
                        default -> {}
                    }
                }
            }
            return env;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteMap(String name) throws Exception {
        File f = new File(DIR + "/" + name + ".json");
        if (f.exists() && !f.delete()) {
            throw new RuntimeException("Could not delete map file: " + f.getPath());
        }
    }

    private static class MapData {
        Cell.CellType[][] grid;
        MapData(Cell.CellType[][] grid) { this.grid = grid; }
    }
}
