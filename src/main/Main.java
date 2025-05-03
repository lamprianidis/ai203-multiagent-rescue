package main;

import environment.EnvironmentFactory;
import environment.EnvironmentHolder;
import environment.GridEnvironment;
import environment.Cell;
import gui.SimulationView;
import javafx.application.Application;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        GridEnvironment env = EnvironmentFactory.buildOfficeEnvironment();
        EnvironmentHolder.setEnvironment(env);

        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(ProfileImpl.GUI, "true");
        AgentContainer container = Runtime.instance().createMainContainer(profile);

        Random random = new Random();
        int width = env.getWidth();
        int height = env.getHeight();
        for (int i = 0; i < 20; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked()
                    || env.getCell(x, y).getType() == Cell.CellType.EXIT);

            String agentName = "Agent" + i;
            AgentController agent = container.createNewAgent(
                    agentName,
                    "agents.RandomEvacueeAgent",
                    new Object[]{x, y}
            );
            agent.start();
        }

        SimulationView.setEnvironment(env);
        Application.launch(SimulationView.class, args);
    }
}
