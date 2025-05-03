package environment;

public class EnvironmentFactory {

    public static GridEnvironment buildOfficeEnvironment() {
        GridEnvironment env = new GridEnvironment(40, 30);

        // perimeter
        for (int x = 0; x < env.getWidth(); x++) {
            env.defineWall(x, 0);
            env.defineWall(x, env.getHeight() - 1);
        }
        for (int y = 0; y < env.getHeight(); y++) {
            env.defineWall(0, y);
            env.defineWall(env.getWidth() - 1, y);
        }

        // corridors
        for (int x = 1; x < env.getWidth() - 1; x++) {
            env.defineWall(x, 10);
            env.defineWall(x, 19);
        }
        for (int y = 1; y < env.getHeight() - 1; y++) {
            env.defineWall(10, y);
            env.defineWall(20, y);
        }

        // door openings
        int[] blockXs = {1, 11, 21};
        for (int bx : blockXs) {
            env.getCell(bx + 4, 10).setType(Cell.CellType.FREE);
            env.getCell(bx + 4, 19).setType(Cell.CellType.FREE);
        }
        env.getCell(10, 5).setType(Cell.CellType.FREE);
        env.getCell(20, 25).setType(Cell.CellType.FREE);

        // desks
        int[] blockYs = {1, 12};
        for (int bx : blockXs) {
            for (int by : blockYs) {
                for (int y = by + 1; y < by + 7; y += 2) {
                    for (int x = bx + 1; x < bx + 9; x += 3) {
                        env.defineObstacle(x, y);
                    }
                }
            }
        }

        // exits
        env.defineExit(env.getWidth() - 1, 5, 1);
        env.defineExit(env.getWidth() - 1, 24, 2);
        env.defineExit(0, 14, 3);

        return env;
    }
}
