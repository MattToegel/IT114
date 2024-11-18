package Popularity.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a grid of cells. The grid is defined by its number of rows and
 * columns.
 */
public class Grid {
    private int rows;
    private int cols;
    private Cell[][] cells;
    private Random rand = new Random();

    /**
     * Constructs a Grid with the specified number of rows and columns.
     *
     * @param rows the number of rows in the grid.
     * @param cols the number of columns in the grid.
     */
    public Grid(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive integers.");
        }
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        initializeCells();
    }

    /**
     * Initializes the cells in the grid.
     */
    private void initializeCells() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    /**
     * Gets the number of rows in the grid.
     *
     * @return the number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns in the grid.
     *
     * @return the number of columns.
     */
    public int getCols() {
        return cols;
    }

    /**
     * Gets the cell at the specified row and column.
     *
     * @param row the row index of the cell.
     * @param col the column index of the cell.
     * @return the cell at the specified position.
     * @throws IndexOutOfBoundsException if the position is out of bounds.
     */
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Cell position out of bounds");
        }
        return cells[row][col];
    }

    /**
     * Adds a clientId to a cell at given coordinate
     * 
     * @param x
     * @param y
     * @param clientId
     * @return true if the clientId was added to the cell's list
     * @throws IndexOutOfBoundsException if the coordinate is out of range
     */
    public boolean addPlayerToCellAtCoordinate(int x, int y, long clientId) throws IndexOutOfBoundsException {
        if (x < 0 || x >= rows || y < 0 || y >= cols) {
            throw new IndexOutOfBoundsException("Cell position out of bounds");
        }
        return cells[x][y].addPlayer(clientId);
    }

    /**
     * Adds a clientId to a cell at given coordinate, removes them from previous x,y
     * 
     * @param x
     * @param y
     * @param clientId
     * @param px       previous x
     * @param py       previous y
     * @return true if the clientId was added to the cell's list
     * @throws IndexOutOfBoundsException if the coordinate is out of range
     */
    public boolean addPlayerToCellAtCoordinate(int x, int y, long clientId, int px, int py)
            throws IndexOutOfBoundsException {
        if (x < 0 || x >= rows || y < 0 || y >= cols) {
            throw new IndexOutOfBoundsException("Cell position out of bounds");
        }
        try {
            cells[px][py].removePlayer(clientId);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.warning(String.format("Failed to remove %s from %s,%s", clientId, px, py));
        }
        return cells[x][y].addPlayer(clientId);
    }

    /**
     * Checks if all cells in the grid are occupied.
     *
     * @return true if all cells are occupied, false otherwise.
     */
    public boolean areAllCellsOccupied() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!cells[row][col].isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the most popular Cell based on number of players in the Cell. Ignores
     * unoccupied Cells.
     * 
     * @return Most popular Cell or null
     */
    public Cell getMostPopularCell() {
        Cell mostPopular = null;
        // Populate the map with occupied cells and their numbers
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell currentCell = cells[row][col];
                if (!currentCell.isOccupied()) {
                    continue;
                }

                if (
                // set popular if not set
                mostPopular == null
                        // set popular if number is greated
                        || currentCell.getNumberInCell() > mostPopular.getNumberInCell()
                        // set popular if number is equal and random is true (gives opportunity of tied
                        // cells to randomly be selected)
                        || currentCell.getNumberInCell() == mostPopular.getNumberInCell() && rand.nextBoolean()) {
                    mostPopular = currentCell;
                }
            }
        }
        return mostPopular;
    }

    /**
     * Gets all the data of all the Cells that are occupied (x,y, count) and bundles
     * it in a serialized data object
     * 
     * @return the list of OccupiedStatus data
     */
    public List<OccupiedStatus> getOccupiedStatus() {
        List<OccupiedStatus> oss = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell currentCell = cells[row][col];
                if (!currentCell.isOccupied()) {
                    continue;
                }
                OccupiedStatus os = new OccupiedStatus();
                os.setX(row);
                os.setY(col);
                os.setCount(currentCell.getNumberInCell());
                oss.add(os);
            }
        }
        return oss;
    }

    public int getCountAtCoordinate(int x, int y) {
        if (x < 0 || x >= rows || y < 0 || y >= cols) {
            throw new IndexOutOfBoundsException("Cell position out of bounds");
        }
        return cells[x][y].getNumberInCell();
    }
    /**
     * Used client-side to populate the number of people in a cell
     * @param x
     * @param y
     * @param count
     */
    public void setOccupiedStatus(int x, int y, int count){
        if (x < 0 || x >= rows || y < 0 || y >= cols) {
            throw new IndexOutOfBoundsException("Cell position out of bounds");
        }
        cells[x][y].setOccupiedStatus(count);
    }

    /**
     * Resets the grid by resetting all cells.
     */
    public void reset() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (cells[row][col] != null) {
                    cells[row][col].reset();
                }
            }
        }
    }

    /**
     * Returns a string representation of the grid.
     *
     * @return a string representation of the grid.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grid (").append(rows).append(" x ").append(cols).append("):\n");
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                sb.append(cells[row][col].isOccupied() ? "[x]" : "[ ]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Main method for demonstrating the Grid functionality.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        Grid grid = new Grid(3, 3);
        grid.addPlayerToCellAtCoordinate(1, 1, 1);
        grid.addPlayerToCellAtCoordinate(0, 2, 2);
        System.out.println(grid);
        System.out.println("All cells occupied: " + grid.areAllCellsOccupied());

        grid.reset();
        System.out.println("After reset:");
        System.out.println(grid);
    }
}
