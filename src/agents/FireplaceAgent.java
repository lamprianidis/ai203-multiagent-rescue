package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import environment.Cell;
import environment.EnvironmentHolder;
import environment.GridEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FireplaceAgent extends Agent {
    private String agentId;
    private GridEnvironment env;
    private int x, y;
    private int severity;
    private static final Random random = new Random();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        x = (int) args[0];
        y = (int) args[1];

        agentId = getLocalName();
        // TODO: May remove it after UI implementation
        severity = args.length >= 3 ? (int) args[2] : 3; // default = 3

        env = EnvironmentHolder.getEnvironment();
        env.addAgent(agentId, null, x, y);
        env.defineObstacle(x, y); // cell turns to obstacle

        long period = getPeriodForSeverity(severity);
        addBehaviour(new TickerBehaviour(this, period) {
            @Override
            protected void onTick() {
                burn();
            }
        });
    }

    // TODO: Convert the hardcoded values
    private long getPeriodForSeverity(int severity) {
        return switch (severity) {
            case 1 -> 2500;
            case 2 -> 2000;
            case 3 -> 1500;
            case 4 -> 1000;
            case 5 -> 500;
            default -> 1500;
        };
    }

    private void burn() {
        List<int[]> neighbors = getValidNeighbors();
        if (!neighbors.isEmpty()) {
            int[] next = neighbors.get(random.nextInt(neighbors.size()));
            int nx = next[0], ny = next[1];

            try {
                String newId = "Fire" + System.currentTimeMillis();
                AgentController newFire = getContainerController().createNewAgent(
                        newId,
                        "agents.FireplaceAgent",
                        new Object[]{nx, ny, severity}
                );
                newFire.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private List<int[]> getValidNeighbors() {
        List<int[]> neighbors = new ArrayList<>();
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx < 0 || ny < 0 || nx >= env.getWidth() || ny >= env.getHeight())
                continue;
            Cell cell = env.getCell(nx, ny);
            if (cell.getType() == Cell.CellType.FREE) {
                neighbors.add(new int[]{nx, ny});
            }
        }
        return neighbors;
    }

    @Override
    protected void takeDown() {
        env.removeAgent(agentId);
    }
}
