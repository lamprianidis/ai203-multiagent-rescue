package agents;

import environment.EnvironmentHolder;
import environment.GridEnvironment;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.ArrayList;
import java.util.List;

public abstract class HelperAgent extends Agent {
    protected String agentId;
    protected GridEnvironment env;
    protected int x, y;

    protected abstract long getInterval();
    protected abstract void move();

    // Parameters for agent's movement
    protected int stuckCounter = 0;
    protected static final int MAX_STUCK_STEPS = 3;
    public boolean isStuck() {
        return stuckCounter < MAX_STUCK_STEPS;
    }

    @Override
    protected void setup() {
        env = EnvironmentHolder.getEnvironment();
        agentId = getLocalName();

        List<int[]> exits = new ArrayList<>(env.getAllExitPositions());
        int[] exitPos = exits.get(new java.util.Random().nextInt(exits.size()));
        x = exitPos[0];
        y = exitPos[1];
        env.addAgent(agentId, null, x, y);

        addBehaviour(new TickerBehaviour(this, getInterval()) {
            @Override
            protected void onTick() {
                move();
            }
        });
    }

    protected List<int[]> getValidMoves() {
        List<int[]> moves = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (Math.abs(dx) + Math.abs(dy) != 1) {
                    continue;
                }
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= env.getWidth() || ny >= env.getHeight()) {
                    continue;
                }
                if (env.getCell(nx, ny).isBlocked()) {
                    continue;
                }
                moves.add(new int[]{nx, ny});
            }
        }
        return moves;
    }
}
