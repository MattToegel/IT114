package Project.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Project.Common.Cell.Terrain;
import Project.Common.Cell.TerrainBonusType;

/**
 * Represents a grid of cells.
 * The grid is defined by its number of rows and columns.
 */
public class Grid {
    private int rows;
    private int cols;
    private Cell[][] cells;
    private Random random;
    private long seed;

    /**
     * Constructs a Grid with the specified number of rows and columns.
     *
     * @param rows the number of rows in the grid.
     * @param cols the number of columns in the grid.
     * @params seed for the Random class to use to produce the same "random" results
     */
    public Grid(int rows, int cols, long randomSeed) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and columns must be positive integers.");
        }
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        // Important: using seed syncing to avoid having to sync individual Cells
        // setting a seed is like a Minecraft world seed, having the same value will
        // have the randomizer
        // produce the same "random" results
        this.seed = randomSeed;
        this.random = new Random(randomSeed);
        initializeCells();
    }

    public long getSeed() {
        return seed;
    }

    /**
     * Initializes the cells in the grid.
     */
    private void initializeCells() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell c = new Cell(row, col);
                // set random Terrain
                Terrain terrainType = Terrain.values()[random.nextInt(Terrain.values().length)];
                TerrainBonusType terrainBonusType = TerrainBonusType.FLAT;
                double bonusValue = 0;
                c.setTerrainType(terrainType);
                if (terrainType != Terrain.NONE) {
                    if (terrainType != Terrain.ENERGY && terrainType != Terrain.CARDS) {
                        // set random bonus type
                        terrainBonusType = TerrainBonusType.values()[random.nextInt(TerrainBonusType.values().length)];
                        c.setTerrainBonusType(terrainBonusType);
                    }
                    if (terrainBonusType == TerrainBonusType.FLAT) {
                        // set random bonus value based on Terrain
                        int max = terrainType == Terrain.CARDS?3:5;
                        bonusValue = random.nextInt(max) + 1; // 1-3 or 1-5
                    } else {
                        bonusValue = 0.25 + (random.nextDouble() * 0.5); // .25 - .75
                    }
                    c.setTerrainBonus(bonusValue);
                }
                cells[row][col] = c;
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
     * Sets the occupied status of the cell at the specified position.
     *
     * @param row      the row index of the cell.
     * @param col      the column index of the cell.
     * @param occupied true to mark the cell as occupied, false to mark it as
     *                 unoccupied.
     * @throws IndexOutOfBoundsException if the position is out of bounds.
     */
    public void setCell(int row, int col, Tower tower) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Cell position out of bounds");
        }
        cells[row][col].placeTower(tower);
    }

    /**
     * Attempts to mark the cell at the specified position as occupied.
     *
     * @param row the row index of the cell.
     * @param col the column index of the cell.
     * @return true if the cell was successfully marked as occupied, false if it was
     *         already occupied.
     * @throws IndexOutOfBoundsException if the position is out of bounds.
     */
    public boolean tryOccupyCell(int row, int col, Tower tower) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Cell position out of bounds");
        }
        Cell cell = cells[row][col];
        if (cell.isOccupied()) {
            return false;
        } else {
            cell.placeTower(tower);
            return true;
        }
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
        rows = 0;
        cols = 0;
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
                // sb.append(cells[row][col].isOccupied() ? "[x]" : "[ ]");
                Cell c = cells[row][col];
                // use the first letter of the Terrain as a visual
                String terrainType = c.getTerrainType().name().substring(0, 1);
                // use () or [] to show occupied status
                String text = c.isOccupied() ? String.format("(%s)", terrainType) : String.format("[%s]", terrainType);
                sb.append(text);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns a list of all valid cells within a given range from a specified
     * coordinate.
     *
     * @param centerX the x-coordinate of the center cell.
     * @param centerY the y-coordinate of the center cell.
     * @param range   the range (in units) to look for valid cells.
     * @return a list of all valid cells within the specified range.
     */
    public List<Cell> getValidCellsWithinRange(int centerX, int centerY, int range) {
        List<Cell> validCells = new ArrayList<>();
        for (int row = Math.max(0, centerX - range); row <= Math.min(rows - 1, centerX + range); row++) {
            for (int col = Math.max(0, centerY - range); col <= Math.min(cols - 1, centerY + range); col++) {
                if (Math.abs(row - centerX) + Math.abs(col - centerY) <= range) {
                    validCells.add(cells[row][col]);
                }
            }
        }
        return validCells;
    }

    /**
     * Main method for demonstrating the Grid functionality.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        Grid grid = new Grid(3, 3, new Random().nextLong());
        grid.setCell(1, 1, new Tower(-1));
        grid.setCell(0, 2, new Tower(-1));
        System.out.println(grid);

        System.out.println("Trying to occupy cell (1, 1): " + grid.tryOccupyCell(1, 1, new Tower(-1)));
        System.out.println("Trying to occupy cell (2, 2): " + grid.tryOccupyCell(2, 2, new Tower(-1)));
        System.out.println("All cells occupied: " + grid.areAllCellsOccupied());

        grid.reset();
        System.out.println("After reset:");
        System.out.println(grid);

        System.out.println("Valid cells within range 1 of (1, 1): " + grid.getValidCellsWithinRange(1, 1, 1));
    }
}
