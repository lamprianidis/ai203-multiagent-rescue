package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import environment.EnvironmentHolder;
import environment.GridEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEvacueeAgent extends Agent {
    private String agentId;
    private GridEnvironment env;
    private int x, y;
    private final Random random = new Random();

    @Override
    protected void setup() {
        env = EnvironmentHolder.getEnvironment();
        agentId = getLocalName();

        Object[] args = getArguments();
        x = (int) args[0];
        y = (int) args[1];
        env.addAgent(agentId, x, y);

        addBehaviour(new TickerBehaviour(this, 400) {
            @Override
            protected void onTick() {
                List<int[]> validMoves = new ArrayList<>();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (Math.abs(dx) + Math.abs(dy) != 1) {
                            continue;
                        }
                        int newX = x + dx;
                        int newY = y + dy;
                        if (newX < 0 || newY < 0 || newX >= env.getWidth() || newY >= env.getHeight()) {
                            continue;
                        }
                        if (env.getCell(newX, newY).isBlocked()) {
                            continue;
                        }
                        validMoves.add(new int[]{newX, newY});
                    }
                }

                if (!validMoves.isEmpty()) {
                    int[] nextPos = validMoves.get(random.nextInt(validMoves.size()));
                    int targetX = nextPos[0];
                    int targetY = nextPos[1];
                    if (env.tryMoveAgent(agentId, targetX, targetY)) {
                        x = targetX;
                        y = targetY;
                    }
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        env.removeAgent(agentId);
    }
}
