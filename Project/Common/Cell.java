package Project.Common;

/**
 * Represents a single cell in a grid.
 * The cell is defined by its row (x) and column (y) coordinates.
 */
public class Cell {
    private int x;
    private int y;
    private boolean occupied;

    /**
     * Constructs a Cell with specified coordinates.
     *
     * @param x the row index of the cell.
     * @param y the column index of the cell.
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.occupied = false;
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
     * @return true if the cell is occupied, false otherwise.
     */
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * Sets the occupied status of the cell.
     *
     * @param occupied true to mark the cell as occupied, false to mark it as unoccupied.
     */
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    /**
     * Resets the cell to its initial state.
     */
    public void reset() {
        this.occupied = false;
        // Reset other object references here if needed
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
                ", occupied=" + occupied +
                '}';
    }
}
