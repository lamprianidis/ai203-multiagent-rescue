package agents.manager;

import environment.Cell;
import environment.EnvironmentHolder;
import environment.GridEnvironment;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AgentManager {
    private static AgentContainer container;
    private static final List<AgentController> controllers = new ArrayList<>();

    public static void init(AgentContainer ac) {
        container = ac;
    }

    public static void register(AgentController ac){
        controllers.add(ac);
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
            register(ac);
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
            register(ac);
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
            register(ac);
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
                    new Object[]{x, y, settings.fireSeverity}
            );
            fire.start();
            register(fire);
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
                    "agents.FirefighterHelperAgent",
                    new Object[]{x, y}
            );
            ac.start();
            register(ac);
        }

        // FireSensor agents
        List<int[]> wallCells = new ArrayList<>();
        for (int x = 0; x < env.getWidth(); x++) {
            for (int y = 0; y < env.getHeight(); y++) {
                if (env.getCell(x, y).getType() == Cell.CellType.WALL) {
                    wallCells.add(new int[]{x, y});
                }
            }
        }
        wallCells.sort(Comparator.comparingInt(coords -> coords[0] + coords[1]));
        int totalWalls = wallCells.size();
        for (int i = 0; i < settings.fireSensorCount; i++) {
            int wallIndex = (int)((long)i * totalWalls / settings.fireSensorCount);
            int[] wallPosition = wallCells.get(wallIndex);
            int spawnX = wallPosition[0], spawnY = wallPosition[1];
            AgentController ac = container.createNewAgent(
                    "FireSensor" + i,
                    "agents.FireSensorAgent",
                    new Object[]{spawnX, spawnY}
            );
            ac.start();
            register(ac);
        }

        // Rescuer agents
        for (int i = 0; i < settings.rescuerCount; i++) {
            int x, y;
            do {
                x = random.nextInt(width);
                y = random.nextInt(height);
            } while (env.getCell(x, y).isBlocked());

            AgentController ac = container.createNewAgent(
                    "Rescuer" + i,
                    "agents.RescuerHelperAgent",
                    new Object[]{x, y}
            );
            ac.start();
            register(ac);
        }

        // Announcer agent
        AgentController controller = container.createNewAgent(
                "Announcer",
                "agents.AnnouncerAgent",
                new Object[]{}
        );
        controller.start();
        register(controller);
    }

    public static void killAll() {
        for (AgentController ac : controllers) {
            try {
                ac.kill();
            } catch (Exception ignored) { }
        }
        controllers.clear();
    }

    public static void suspendAll() {
        for (AgentController ac : controllers) {
            try { ac.suspend(); } catch (Exception ignored) {}
        }
    }

    public static void resumeAll() {
        for (AgentController ac : controllers) {
            try { ac.activate(); } catch (Exception ignored) {}
        }
    }
}