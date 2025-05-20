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

        for (int i = 0; i < 110; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked() ||
                    env.getCell(x, y).getType() == Cell.CellType.EXIT);

            AgentController ac = container.createNewAgent(
                    "Calm" + i,
                    "agents.CalmEvacueeAgent",
                    new Object[]{x, y}
            );
            ac.start();
        }

        for (int i = 0; i < 20; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked() ||
                    env.getCell(x, y).getType() == Cell.CellType.EXIT);

            AgentController ac = container.createNewAgent(
                    "Panicked" + i,
                    "agents.PanickedEvacueeAgent",
                    new Object[]{x, y}
            );
            ac.start();
        }

        for (int i = 0; i < 10; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked() ||
                    env.getCell(x, y).getType() == Cell.CellType.EXIT);

            AgentController ac = container.createNewAgent(
                    "Injured" + i,
                    "agents.InjuredEvacueeAgent",
                    new Object[]{x, y}
            );
            ac.start();
        }

        // TODO: Change the static number of fires based on user's dynamic input
        for (int i = 0; i < 3; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked() ||
                    env.getCell(x, y).getType() == Cell.CellType.EXIT);

            AgentController fire = container.createNewAgent(
                    "Fireplace" + i,
                    "agents.FireplaceAgent",
                    new Object[]{x, y, 1}
            );
            fire.start();
        }

        // TODO: Change the static number of firefighters based on user's dynamic input
        for (int i = 0; i < 5; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked() ||
                    env.getCell(x, y).getType() == Cell.CellType.EXIT);

            AgentController ac = container.createNewAgent(
                    "Firefighter" + i,
                    "agents.FirefighterAgent",
                    new Object[]{x, y}
            );
            ac.start();
        }

        SimulationView.setEnvironment(env);
        Application.launch(SimulationView.class, args);
    }
}
