package agents;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RescuerHelperAgent extends HelperAgent{
    private int[][] distMap;
    private long stopUntil = 0;
    private String helpInjured = null;

    private boolean isNeighbor(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) == 1;
    }

    @Override
    protected long getInterval() {
        return 500;
    }

    @Override
    protected void setup() {
        super.setup();
        distMap = env.computeDistanceToInjuredOrFire("injured");
    }

    @Override
    protected void move() {
        if (System.currentTimeMillis() < stopUntil) {
            return; // Freeze
        }
        if (stuckCounter >= 2) {
            distMap = env.computeDistanceToInjuredOrFire("injured");
        }

        List<int[]> validMoves = getValidMoves();
        validMoves.stream()
                .min(Comparator.comparingInt(p -> distMap[p[0]][p[1]]))
                .ifPresent(nextPos -> {
                    int newX = nextPos[0];
                    int newY = nextPos[1];

                    boolean moved = env.tryMoveAgent(agentId, newX, newY);
                    if (moved) {
                        x = newX;
                        y = newY;
                        stuckCounter = 0;
                    } else {
                        stuckCounter++;
                    }
                });

        // Fallback: agent moves to a neighbour valid cell if stacks
        if (stuckCounter >= 2 && !validMoves.isEmpty()) {
            java.util.Collections.shuffle(validMoves);
            int[] fallbackMove = validMoves.get(0);

            boolean moved = env.tryMoveAgent(agentId, fallbackMove[0], fallbackMove[1]);
            if (moved) {
                x = fallbackMove[0];
                y = fallbackMove[1];
                stuckCounter = 0;
            } else {
                stuckCounter++;
            }
        }

        // Freeze when Injured is reached
        for (Map.Entry<String, int[]> entry : env.getAllAgentPositions().entrySet()) {
            String otherId = entry.getKey();
            int[] pos = entry.getValue();

            if (!otherId.equals(agentId) &&
                    isNeighbor(x, y, pos[0], pos[1]) &&
                    env.getAgentType(otherId) == EvacueeAgent.Type.INJURED) {

                stopUntil = System.currentTimeMillis() + 2000;
                helpInjured = otherId;
                // TODO: This message should be printed in console
                System.out.println(getLocalName() + " is helping injured evacuee" + otherId + "for 2 seconds");
            }
        }

        // Injured turns to Calm
        if (helpInjured != null && System.currentTimeMillis() >= stopUntil) {
            int[] pos2 = env.getAllAgentPositions().get(helpInjured);
            if (pos2 != null) {
                env.removeAgent(helpInjured);
                // Replace Injured with Calm evacuee agent
                String newId = "rescued_" + helpInjured;
                try {
                    jade.wrapper.AgentController calm = getContainerController().createNewAgent(
                            newId,
                            "agents.CalmEvacueeAgent",
                            new Object[]{pos2[0], pos2[1]}
                    );
                    calm.start();
                    distMap = env.computeDistanceToInjuredOrFire("injured"); // Recompute distances for next injured
                } catch (Exception e) {
                    e.printStackTrace();
                }
                helpInjured = null;
            }
        }
    }
}