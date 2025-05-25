package agents.util;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.logging.*;

public final class MessageLogger {
    private static final Logger LOG = Logger.getLogger(MessageLogger.class.getName());

    static {
        try {
            Handler fh = new FileHandler("agent-messages.log", true);
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.INFO);
            LOG.addHandler(fh);
            LOG.setUseParentHandlers(false);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to initialize log file handler", e);
        }
    }

    private MessageLogger() {}

    public static void logMessage(ACLMessage msg) {
        String sender = msg.getSender().getLocalName();

        List<String> receivers = new ArrayList<>();
        var iterator = msg.getAllReceiver();
        while (iterator.hasNext()) {
            AID aid = (AID) iterator.next();
            receivers.add(aid.getLocalName());
        }
        String receiverList = String.join(",", receivers);

        String log = String.format(
                "[%s -> %s] perf=%s, content=\"%s\"",
                sender,
                receiverList,
                ACLMessage.getPerformative(msg.getPerformative()),
                msg.getContent()
        );

        System.out.println(log);
        LOG.info(log);
    }
}
