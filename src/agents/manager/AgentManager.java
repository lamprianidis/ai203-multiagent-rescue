package agents.manager;

import environment.Cell;
import environment.EnvironmentHolder;
import environment.GridEnvironment;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AgentManager {
    private static AgentContainer container;
    private static final List<AgentController> controllers = new ArrayList<>();

    public static void init(AgentContainer ac) {
        container = ac;
    }

    public static void spawnAll(AgentSettings settings) throws StaleProxyException {
        controllers.clear();
        GridEnvironment env = EnvironmentHolder.getEnvironment();
        Random random = new Random();
        int width = env.getWidth(), height = env.getHeight();

        // Calm evacuees
        for (int i = 0; i < settings.calmCount; i++) {
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
            controllers.add(ac);
        }

        // Panicked evacuees
        for (int i = 0; i < settings.panickedCount; i++) {
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
            controllers.add(ac);
        }

        // Injured evacuees
        for (int i = 0; i < settings.injuredCount; i++) {
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
            controllers.add(ac);
        }

        // Fireplace agents
        for (int i = 0; i < settings.fireplaceCount; i++) {
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
            controllers.add(fire);
        }

        // Firefighter agents
        for (int i = 0; i < settings.firefighterCount; i++) {
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
            controllers.add(ac);
        }

        // FireSensor agents
        for (int i = 0; i < settings.fireSensorCount; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked() ||
                    env.getCell(x, y).getType() == Cell.CellType.EXIT);

            AgentController ac = container.createNewAgent(
                    "FireSensor" + i,
                    "agents.FireSensorAgent",
                    new Object[]{x,y}
            );
            ac.start();
            controllers.add(ac);
        }

        // Announcer agent
        AgentController controller = container.createNewAgent(
                "Announcer",
                "agents.AnnouncerAgent",
                new Object[]{}
        );
        controller.start();
    }

    public static void killAll() {
        for (AgentController ac : controllers) {
            try {
                ac.kill();
            } catch (Exception ignored) { }
        }
        controllers.clear();
    }
}