package environment;

import java.util.*;
import agents.EvacueeAgent;

public class GridEnvironment {
    private final int width;
    private final int height;
    private final Cell[][] grid;

    private final Map<String, int[]> agentPositions = new HashMap<>();
    private final Map<Integer, int[]> exitPositions = new HashMap<>();
    private final Map<String, agents.EvacueeAgent.Type> agentTypes = new HashMap<>();
    private final Map<agents.EvacueeAgent.Type, Integer> evacuatedCounts = new HashMap<>();
    private final Map<String, EvacueeAgent> agentInstances = new HashMap<>();

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

    public int[][] distanceToExits = computeDistanceToExits();

    public synchronized void defineWall(int x, int y) {
        grid[x][y].setType(Cell.CellType.WALL);
    }

    public synchronized void defineObstacle(int x, int y) {
        grid[x][y].setType(Cell.CellType.OBSTACLE);
    }

    public synchronized void defineExit(int x, int y, int exitId) {
        grid[x][y].setType(Cell.CellType.EXIT);
        exitPositions.put(exitId, new int[]{x, y});
        distanceToExits = computeDistanceToExits();
    }

    public synchronized void addAgent(String agentId,
                                      agents.EvacueeAgent.Type type,
                                      int x, int y,
                                      EvacueeAgent instance) {
        agentPositions.put(agentId, new int[]{x,y});
        if (type != null) {
            agentTypes.put(agentId, type);
        }
        agentInstances.put(agentId, instance);
    }

    public synchronized void addAgent(String agentId,
                                      agents.EvacueeAgent.Type type,
                                      int x, int y) {
        agentPositions.put(agentId, new int[]{x, y});
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
            if (otherId.startsWith("FireSensor")) continue;
            if (otherId.equals(agentId)) continue; // ignore self Id
            int[] pos = entry.getValue();
            if (pos[0] == toX && pos[1] == toY) {
                return false;
            }
        }
        agentPositions.put(agentId, new int[]{toX, toY});
        if (target.getType() == Cell.CellType.EXIT) {
            agents.EvacueeAgent.Type type = agentTypes.remove(agentId);
            agentPositions.remove(agentId);
            evacuatedCounts.merge(type, 1, Integer::sum);
        }
        return true;
    }

    public synchronized int getEvacuatedCount(agents.EvacueeAgent.Type type) {
        return evacuatedCounts.getOrDefault(type, 0);
    }

    public synchronized Map<String, int[]> getAllAgentPositions() {
        return new HashMap<>(agentPositions);
    }

    public synchronized EvacueeAgent.Type getAgentType(String agentId) {
        return agentTypes.get(agentId);
    }

    public synchronized Collection<int[]> getAllExitPositions() { return exitPositions.values(); }

    public synchronized Cell getCell(int x, int y) {
        return grid[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public synchronized boolean hasImmovableAgentAt(int x, int y) {
        for (Map.Entry<String, int[]> entry : agentPositions.entrySet()) {
            String id = entry.getKey();
            int[] pos = entry.getValue();
            if (pos[0] == x && pos[1] == y) {
                EvacueeAgent agent = agentInstances.get(id);
                if (agent != null && !agent.isStuck()) {
                    return true;
                }
            }
        }
        return false;
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
                        && !hasImmovableAgentAt(nx, ny)
                        && dist[nx][ny] > cd + 1) {
                    dist[nx][ny] = cd + 1;
                    queue.addLast(new int[]{nx, ny});
                }
            }
        }
        return dist;
    }

    public int[][] getDistanceToEexits(){
        return distanceToExits;
    }

    /**
     * Computes the minimum number of steps from each cell to the nearest injured or fire.
     * Returns a 2D array dist[x][y] = distance in steps to the closest injured or fire.
     */
    public synchronized int[][] computeDistanceToInjuredOrFire (String targetType) {
        int[][] dist = new int[width][height];
        // initialize distances to "infinite"
        for (int i = 0; i < width; i++) {
            Arrays.fill(dist[i], Integer.MAX_VALUE);
        }

        Deque<int[]> queue = new ArrayDeque<>();
        if ("fire".equalsIgnoreCase(targetType)) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (grid[i][j].getType() == Cell.CellType.OBSTACLE) {
                        dist[i][j] = 0;
                        queue.addLast(new int[]{i, j});
                    }
                }
            }
        } else if ("injured".equalsIgnoreCase(targetType)) {
            for (var entry : agentPositions.entrySet()) {
                String id = entry.getKey();
                EvacueeAgent.Type type = agentTypes.get(id);
                if (type == EvacueeAgent.Type.INJURED) {
                    int[] p = entry.getValue();
                    dist[p[0]][p[1]] = 0;
                    queue.addLast(new int[]{p[0], p[1]});
                }
            }
        } else {
            return dist;
        }

        // 3) explore in four directions
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] p = queue.removeFirst();
            int cx = p[0], cy = p[1], cd = dist[cx][cy];
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx >= 0 && ny >= 0 && nx < width && ny < height
                        && !grid[nx][ny].isBlocked()
                        && !hasImmovableAgentAt(nx, ny)
                        && dist[nx][ny] > cd + 1) {
                    dist[nx][ny] = cd + 1;
                    queue.addLast(new int[]{nx, ny});
                }
            }
        }
        return dist;
    }
}
