package agents;

import environment.Cell;
import java.util.List;
import java.util.Random;

public class PanickedEvacueeAgent extends EvacueeAgent {
    private final Random random = new Random();

    @Override
    protected Type getType() {
        return Type.PANICKED;
    }

    @Override
    protected long getInterval() {
        return 400;
    }

    @Override
    protected void move() {
        List<int[]> validMoves = getValidMoves();
        if (!validMoves.isEmpty()) {
            int[] nextPos = validMoves.get(random.nextInt(validMoves.size()));
            int newX = nextPos[0], newY = nextPos[1];
            if (env.tryMoveAgent(agentId, newX, newY)) {
                x = newX; y = newY;
                
                if (env.getCell(newX, newY).getType() == Cell.CellType.EXIT) {
                    doDelete();
                }
            }
        }
    }
}
