package environment;

import java.util.HashMap;
import java.util.Map;
import agents.EvacueeAgent;

public class GridEnvironment {
    private final int width;
    private final int height;
    private final Cell[][] grid;

    private final Map<String, int[]> agentPositions = new HashMap<>();
    private final Map<Integer, int[]> exitPositions = new HashMap<>();
    private final Map<String, agents.EvacueeAgent.Type> agentTypes = new HashMap<>();

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

    public synchronized void addAgent(String agentId,
                                      agents.EvacueeAgent.Type type,
                                      int x, int y) {
        agentPositions.put(agentId, new int[]{x,y});
        if (type != null) {
            agentTypes.put(agentId, type);
        }
    }

    public synchronized void removeAgent(String agentId) {
        agentPositions.remove(agentId);
        agentTypes.remove(agentId);
    }

    // Update type of evacuee to null
    public synchronized void updateAgentType(String agentId, EvacueeAgent.Type type) {
        if (type == null) {
            agentTypes.remove(agentId);
        } else {
            agentTypes.put(agentId, type);
        }
    }

    public synchronized boolean tryMoveAgent(String agentId, int toX, int toY) {
        Cell target = grid[toX][toY];
        // Check for blocked cells
        if (target.isBlocked()) {
            return false;
        }
        // Check for agent in the cell
        for (Map.Entry<String, int[]> entry : agentPositions.entrySet()) {
            String otherId = entry.getKey();
            if (otherId.equals(agentId)) continue; // ignore self Id
            int[] pos = entry.getValue();
            if (pos[0] == toX && pos[1] == toY) {
                return false;
            }
        }
        agentPositions.put(agentId, new int[]{toX, toY});
        if (target.getType() == Cell.CellType.EXIT) {
            agentPositions.remove(agentId);
            agentTypes.remove(agentId);
        }
        return true;
    }

    public synchronized Map<String, int[]> getAllAgentPositions() {
        return new HashMap<>(agentPositions);
    }

    public synchronized EvacueeAgent.Type getAgentType(String agentId) {
        return agentTypes.get(agentId);
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

    /**
     * Computes the minimum number of steps from each cell to the nearest exit.
     * Returns a 2D array dist[x][y] = distance in steps to the closest exit.
     */
    public synchronized int[][] computeDistanceToExits() {
        int[][] dist = new int[width][height];
        // initialize distances to "infinite"
        for (int i = 0; i < width; i++) {
            java.util.Arrays.fill(dist[i], Integer.MAX_VALUE);
        }
        // BFS queue with all exits at distance 0
        java.util.Deque<int[]> queue = new java.util.ArrayDeque<>();
        for (int[] e : exitPositions.values()) {
            int ex = e[0], ey = e[1];
            dist[ex][ey] = 0;
            queue.addLast(new int[]{ex, ey});
        }
        // explore in four directions
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!queue.isEmpty()) {
            int[] p = queue.removeFirst();
            int cx = p[0], cy = p[1], cd = dist[cx][cy];
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx >= 0 && ny >= 0 && nx < width && ny < height
                        && !grid[nx][ny].isBlocked()
                        && dist[nx][ny] > cd + 1) {
                    dist[nx][ny] = cd + 1;
                    queue.addLast(new int[]{nx, ny});
                }
            }
        }
        return dist;
    }
}
