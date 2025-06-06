package agents;

import environment.EnvironmentHolder;
import environment.GridEnvironment;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import agents.util.MessageLogger;

public class FireSensorAgent extends Agent {
    private String agentId;
    private GridEnvironment env;
    private int x, y;

    int range = 5;
    private long getInterval() {
        return 3000;
    }

    protected void setup() {
        env = EnvironmentHolder.getEnvironment();
        agentId = getLocalName();

        Object[] args = getArguments();
        x = (int) args[0];
        y = (int) args[1];
        env.addAgent(agentId, null, x, y);

        addBehaviour(new TickerBehaviour(this, getInterval()) {
            @Override
            protected void onTick() {
                detectFire();
            }
        });
    }

    @Override
    protected void takeDown() {
        env.removeAgent(agentId);
    }

    private void detectFire() {
        for (Map.Entry<String,int[]> agentEntry : env.getAllAgentPositions().entrySet()) {
            String otherAgentId = agentEntry.getKey();
            if (!otherAgentId.startsWith("Fire")) {
                continue;
            }

            int fireX = agentEntry.getValue()[0];
            int fireY = agentEntry.getValue()[1];

            int dist = Math.abs(fireX - x) + Math.abs(fireY - y);
            if (dist <= range) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("Announcer", AID.ISLOCALNAME));
                msg.setContent("FIRE DETECTED at " + fireX + "," + fireY + "!");
                send(msg);
                MessageLogger.logMessage(msg);
                break;
            }
        }
    }
}
