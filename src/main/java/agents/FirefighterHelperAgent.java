package agents;

import environment.Cell;
import environment.EnvironmentHolder;
import environment.GridEnvironment;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.*;

public class FirefighterHelperAgent extends Agent {
    private String agentId;
    private GridEnvironment env;
    private int x, y;

    @Override
    protected void setup() {
        env = EnvironmentHolder.getEnvironment();
        agentId = getLocalName();

        Object[] args = getArguments();
        x = (int) args[0];
        y = (int) args[1];
        env.addAgent(agentId,null, x, y);

        addBehaviour(new TickerBehaviour(this, 500) {
            @Override
            protected void onTick() {
                act();
            }
        });
    }

    protected void act() {
        // Put out fire in neighbor cells
        for (int[] n : getNeighbors()) {
            int nx = n[0], ny = n[1];
            if (env.getCell(nx, ny).getType() == Cell.CellType.OBSTACLE) {
                env.getCell(nx, ny).setType(Cell.CellType.FREE);

                // Remove Fireplace agent
                for (Map.Entry<String, int[]> entry : env.getAllAgentPositions().entrySet()) {
                    String otherId = entry.getKey();
                    int[] pos = entry.getValue();
                    if (pos[0] == nx && pos[1] == ny && otherId.startsWith("Fire")) {
                        try {
                            env.removeAgent(otherId);
                            getContainerController().getAgent(otherId).kill();
                            System.out.println(agentId + " extinguished and removed " + otherId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                // Skip movement after extinguishing
                return;
            }
        }

        // Move to the closest fire
        int[][] fireDist = env.computeDistanceToInjuredOrFire("fire");
        List<int[]> moves = getValidMoves();
        moves.sort(Comparator.comparingInt(p -> fireDist[p[0]][p[1]]));

        for (int[] move : moves) {
            if (env.tryMoveAgent(agentId, move[0], move[1])) {
                x = move[0];
                y = move[1];
                break;
            }
        }
    }

    // Same logic with fireplace
    // TODO: Set a common function in a public file
    private List<int[]> getValidMoves() {
        List<int[]> list = new ArrayList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = x + d[0], ny = y + d[1];
            if (nx >= 0 && ny >= 0 && nx < env.getWidth() && ny < env.getHeight()) {
                if (!env.getCell(nx, ny).isBlocked()) {
                    list.add(new int[]{nx, ny});
                }
            }
        }
        return list;
    }

    // Same logic with fireplace
    // TODO: Set a common function in a public file
    private List<int[]> getNeighbors() {
        List<int[]> list = new ArrayList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = x + d[0], ny = y + d[1];
            if (nx >= 0 && ny >= 0 && nx < env.getWidth() && ny < env.getHeight()) {
                list.add(new int[]{nx, ny});
            }
        }
        return list;
    }

    private int[] findClosestFire() {
        int minDist = Integer.MAX_VALUE;
        int[] target = null;
        for (int i = 0; i < env.getWidth(); i++) {
            for (int j = 0; j < env.getHeight(); j++) {
                if (env.getCell(i, j).getType() == Cell.CellType.OBSTACLE) {
                    int d = manhattan(x, y, i, j);
                    if (d < minDist) {
                        minDist = d;
                        target = new int[]{i, j};
                    }
                }
            }
        }
        return target;
    }

    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    @Override
    protected void takeDown() {
        env.removeAgent(agentId);
    }
}
