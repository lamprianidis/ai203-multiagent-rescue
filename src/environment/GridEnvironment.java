package environment;

import java.util.HashMap;
import java.util.Map;

public class GridEnvironment {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    private final Map<String, int[]> agentPositions = new HashMap<>();
    private final Map<Integer, int[]> exitPositions = new HashMap<>();

    public GridEnvironment(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Cell(Cell.CellType.FREE);
            }
        }
    }

    public synchronized void defineWall(int x, int y) {
        grid[x][y].setType(Cell.CellType.WALL);
    }

    public synchronized void defineObstacle(int x, int y) {
        grid[x][y].setType(Cell.CellType.OBSTACLE);
    }

    public synchronized void defineExit(int x, int y, int exitId) {
        grid[x][y].setType(Cell.CellType.EXIT);
        exitPositions.put(exitId, new int[]{x, y});
    }

    public synchronized void addAgent(String agentId, int x, int y) {
        agentPositions.put(agentId, new int[]{x, y});
    }

    public synchronized void removeAgent(String agentId) {
        agentPositions.remove(agentId);
    }

    public synchronized boolean tryMoveAgent(String agentId,
                                             int toX, int toY) {
        Cell target = grid[toX][toY];
        if (target.isBlocked()) {
            return false;
        }
        agentPositions.put(agentId, new int[]{toX, toY});
        if (target.getType() == Cell.CellType.EXIT) {
            agentPositions.remove(agentId);
        }
        return true;
    }

    public synchronized Map<String, int[]> getAllAgentPositions() {
        return new HashMap<>(agentPositions);
    }

    public synchronized Cell getCell(int x, int y) {
        return grid[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
