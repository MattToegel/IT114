package Project.Common;

/**
 * Represents a single cell in a grid.
 * The cell is defined by its row (x) and column (y) coordinates.
 */
public class Cell {
    private int x;
    private int y;
    private Tower tower;

    /**
     * Constructs a Cell with specified coordinates.
     *
     * @param x the row index of the cell.
     * @param y the column index of the cell.
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.tower = null; // Initially, the cell has no tower.
    }

    /**
     * Gets the row index of the cell.
     *
     * @return the row index.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the column index of the cell.
     *
     * @return the column index.
     */
    public int getY() {
        return y;
    }

    /**
     * Checks if the cell is occupied.
     *
     * @return true if the cell has a tower, false otherwise.
     */
    public boolean isOccupied() {
        return tower != null;
    }

    /**
     * Gets the tower placed on the cell.
     *
     * @return the tower, or null if no tower is placed.
     */
    public Tower getTower() {
        return tower;
    }

    /**
     * Places a tower on the cell.
     *
     * @param tower the tower to be placed.
     */
    public void placeTower(Tower tower) {
        if (this.tower != null) {
            throw new IllegalStateException("Cell is already occupied by a tower.");
        }
        this.tower = tower;
    }

    public void updateTower(Tower tower) {
        if (this.tower == null) {
            throw new IllegalStateException("No tower is assigned to this Cell.");
        }
        this.tower = tower;
    }

    /**
     * Removes the tower from the cell.
     */
    public void removeTower() {
        this.tower = null;
    }

    /**
     * Resets the cell to its initial state.
     */
    public void reset() {
        this.tower = null; // Remove the tower reference if any
    }

    /**
     * Returns a string representation of the cell.
     *
     * @return a string representation of the cell.
     */
    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                ", tower=" + tower +
                '}';
    }
}
