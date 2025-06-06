package environment;

public class EnvironmentFactory {

    public static GridEnvironment buildOfficeEnvironment() {
        int W = 60, H = 45;
        GridEnvironment env = new GridEnvironment(W, H);

        // perimeter walls
        for (int x = 0; x < W; x++) {
            env.defineWall(x, 0);
            env.defineWall(x, H - 1);
        }
        for (int y = 0; y < H; y++) {
            env.defineWall(0, y);
            env.defineWall(W - 1, y);
        }

        // horizontal corridors at y=12 and y=32
        for (int x = 2; x < W - 2; x++) {
            env.defineWall(x, 12);
            env.defineWall(x, 32);
        }
        // door openings every 8 cells
        for (int x = 6; x < W - 6; x += 8) {
            env.getCell(x, 12).setType(Cell.CellType.FREE);
            env.getCell(x, 32).setType(Cell.CellType.FREE);
        }

        // zig‑zag connectors between corridors
        for (int i = 0; i < 6; i++) {
            int bx = 6 + i * 8;
            for (int y = 13; y < 32; y++) {
                env.defineWall(bx, y);
            }
            int oy = (i % 2 == 0) ? 13 : 31;
            env.getCell(bx, oy).setType(Cell.CellType.FREE);
        }

        // vertical branches above and below corridors
        int[] branchXs = {15, 30, 45};
        for (int bx : branchXs) {
            // upper branch
            for (int y = 1; y < 12; y++) {
                env.defineWall(bx, y);
            }
            env.getCell(bx, 6).setType(Cell.CellType.FREE);
            // lower branch
            for (int y = 33; y < H - 1; y++) {
                env.defineWall(bx, y);
            }
            env.getCell(bx, H - 6).setType(Cell.CellType.FREE);
        }

        // open passages at the central horizontal line
        int midY = H / 2;
        for (int i = 0; i < 6; i++) {
            int bx = 6 + i * 8;
            env.getCell(bx, midY).setType(Cell.CellType.FREE);
            env.getCell(bx, midY - 1).setType(Cell.CellType.FREE);
        }
        for (int bx : branchXs) {
            env.getCell(bx, midY).setType(Cell.CellType.FREE);
            env.getCell(bx, midY - 1).setType(Cell.CellType.FREE);
        }

        // corner rooms in each quadrant
        int roomW = 10, roomH = 6;
        int[][] rooms = {
                {2, 2},
                {W - roomW - 2, 2},
                {2, H - roomH - 2},
                {W - roomW - 2, H - roomH - 2}
        };
        for (var o : rooms) {
            int x0 = o[0], y0 = o[1];
            // top and bottom walls
            for (int dx = 0; dx < roomW; dx++) {
                env.defineWall(x0 + dx, y0);
                env.defineWall(x0 + dx, y0 + roomH - 1);
            }
            // left and right walls
            for (int dy = 0; dy < roomH; dy++) {
                env.defineWall(x0, y0 + dy);
                env.defineWall(x0 + roomW - 1, y0 + dy);
            }
            // doorway at bottom center
            env.getCell(x0 + roomW / 2, y0 + roomH - 1)
                    .setType(Cell.CellType.FREE);
        }

        // exits on outer walls
        env.defineExit(0,         17, 1);
        env.defineExit(0,         35, 2);
        env.defineExit(W - 1,     20, 3);
        env.defineExit(W - 1,     37, 4);
        env.defineExit(W / 2 + 7, 0,  5);
        env.defineExit(W / 2 - 6, H - 1, 6);

        return env;
    }
}
