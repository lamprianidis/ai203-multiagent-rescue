package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import environment.EnvironmentHolder;
import environment.GridEnvironment;

public class AnnouncerAgent extends Agent {
    private static final String[] FIRE_ANNOUNCEMENT_TARGET_PREFIXES = {
            "Calm", "Panicked", "Injured", "Firefighter"
    };

    private GridEnvironment env;

    @Override
    protected void setup() {
        env = EnvironmentHolder.getEnvironment();

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage incoming = receive();
                if (incoming == null) {
                    block();
                    return;
                }

                if (incoming.getPerformative() == ACLMessage.INFORM) {
                    String content = incoming.getContent();
                    if (content.startsWith("FIRE")) {
                        announceFireAlert(content);
                    }
                }
            }
        });
    }

    private void announceFireAlert(String fireInfo) {
        ACLMessage announcement = new ACLMessage(ACLMessage.INFORM);
        announcement.setContent("ALERT: " + fireInfo + " EVACUATE the building immediately!");

        for (String id : env.getAllAgentPositions().keySet()) {
            for (String prefix : FIRE_ANNOUNCEMENT_TARGET_PREFIXES) {
                if (id.startsWith(prefix)) {
                    announcement.addReceiver(new AID(id, AID.ISLOCALNAME));
                    break;
                }
            }
        }
        send(announcement);
    }
}
