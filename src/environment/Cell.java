package environment;

public class Cell {
    public enum CellType {
        FREE, // Free space
        WALL, // Permanent wall
        OBSTACLE, // Temporary obstacle
        EXIT // Emergency exit
    }

    private CellType type;

    public Cell(CellType type) {
        this.type = type;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public boolean isBlocked() {
        return type == CellType.WALL || type == CellType.OBSTACLE;
    }
}
