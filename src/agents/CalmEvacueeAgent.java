package agents;

import environment.Cell;
import java.util.Comparator;
import java.util.List;

public class CalmEvacueeAgent extends EvacueeAgent {
    private int[][] distMap;

    @Override
    protected Type getType() {
        return Type.CALM;
    }

    @Override
    protected long getInterval() {
        return 500;
    }

    @Override
    protected void setup() {
        super.setup();
        distMap = env.computeDistanceToExits();
    }

    @Override
    protected void move() {
        // Check for dead
        if (dead) return;
        Cell cell = env.getCell(x, y);
        if (cell.getType() == Cell.CellType.OBSTACLE) {
            timeInFire += getInterval();
            if (timeInFire >= DEATH_THRESHOLD_MS) {
                dead = true;
                env.updateAgentType(agentId, null); // Convert type to null
                deathCount.incrementAndGet();
                System.out.println(agentId + " died at " + x + "," + y);
                return;
            }
        } else {
            timeInFire = 0;
        }

        List<int[]> validMoves = getValidMoves();
        validMoves.stream()
                .min(Comparator.comparingInt(p -> distMap[p[0]][p[1]]))
                .ifPresent(nextPos -> {
                    int newX = nextPos[0];
                    int newY = nextPos[1];

                    if (env.tryMoveAgent(agentId, newX, newY)) {
                        x = newX;
                        y = newY;
                    }

                    if (env.getCell(newX, newY).getType() == Cell.CellType.EXIT) {
                        doDelete();
                    }
                });
    }
}
