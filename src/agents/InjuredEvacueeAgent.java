package agents;

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
    }
}
