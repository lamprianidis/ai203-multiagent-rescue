package agents;

import environment.Cell;

public class InjuredEvacueeAgent extends EvacueeAgent {

    @Override
    protected Type getType() {
        return Type.INJURED;
    }

    @Override
    protected long getInterval() {
        return 1000;
    }

    @Override
    protected void move() {
        // Injured agents do not move
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
    }
}
