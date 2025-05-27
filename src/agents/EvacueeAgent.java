package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import environment.EnvironmentHolder;
import environment.GridEnvironment;
import java.util.ArrayList;
import java.util.List;

public abstract class EvacueeAgent extends Agent {
    public enum Type { CALM, PANICKED, INJURED }

    protected String agentId;
    protected GridEnvironment env;
    protected int x, y;

    // Parameters for dead evacuees
    protected boolean dead = false;
    protected long timeInFire = 0;
    protected static final long DEATH_THRESHOLD_MS = 2000;
    protected static final java.util.concurrent.atomic.AtomicInteger deathCount = new java.util.concurrent.atomic.AtomicInteger(0);

    protected abstract Type getType();
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

        Object[] args = getArguments();
        x = (int) args[0];
        y = (int) args[1];
        env.addAgent(agentId, getType(), x, y, this);

        addBehaviour(new TickerBehaviour(this, getInterval()) {
            @Override
            protected void onTick() {
                move();
            }
        });
    }

    @Override
    protected void takeDown() {
        env.removeAgent(agentId);
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

    // Summary of total deaths
    // TODO: Add a live counter on UI
    public static int getTotalDeaths() {
        return deathCount.get();
    }

    public static void resetTotalDeaths() {
        deathCount.set(0);
    }
}
