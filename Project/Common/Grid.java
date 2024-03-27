package Project.Common;

import Project.Common.TextFX.Color;

public class Grid {
    private Cell[][] grid;
    // TODO note to self, see if I swapped row/col logic
    private int rows, columns;

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < columns && y >= 0 && y <= rows;
    }

    public void generate(int rows, int columns) {
        reset();
        this.rows = rows;
        this.columns = columns;
        grid = new Cell[rows][columns];
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                grid[x][y] = new Cell(x, y);
                grid[x][y].setValue(String.format("(%s,%s) ", x, y));
            }
        }
    }

    public void reset() {
        System.out.println(TextFX.colorize("Resetting grid", Color.YELLOW));
        if (grid != null) {
            for (int x = 0; x < rows; x++) {
                for (int y = 0; y < columns; y++) {
                    grid[x][y].reset();
                }
            }
            grid = null;
        }
    }

    public void print() {
        if (grid == null) {
            System.out.println(TextFX.colorize("Grid isn't setup", Color.RED));
            return;
        }
        for (int x = 0; x < rows; x++) {
            System.out.println("-".repeat(columns * 3));
            for (int y = 0; y < columns; y++) {
                String v = grid[x][y].getValue();
                System.out.print(String.format("%s", v));
            }
            System.out.println("");
        }
        System.out.println("-".repeat(columns * 3));
    }

    public boolean addPlayer(long clientId, String clientName, int x, int y) {
        if (isValidCoordinate(x, y)) {
            return grid[x][y].addPlayer(clientId, clientName);
        }
        return false;
    }

    /**
     * Returns a cell reference by coordinate (maybe we don't want to expose direct
     * access to cells)
     * 
     * @param x
     * @param y
     * @return
     */
    public Cell getCell(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return grid[x][y];
        }
        return null;
    }

    public boolean isGridFull() {
        int occupied = 0;
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                occupied += (grid[x][y].isOccupied() ? 1 : 0);
            }
        }
        return occupied == rows * columns;
    }

    public static void main(String[] args) {
        Grid grid = new Grid();
        grid.generate(8, 8);
        grid.print();
    }
}
