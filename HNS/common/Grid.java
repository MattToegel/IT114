package HNS.common;

import java.util.logging.Logger;

public class Grid {
    private Cell[][] cells = null;
    private static Logger logger = Logger.getLogger(Grid.class.getName());

    public void build(int rows, int columns) {
        logger.info("Starting grid generation");
        cells = new Cell[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                cells[row][column] = new Cell(row, column);
            }
        }
        logger.info(String.format("Finished generating %s x %s grid", rows, columns));
    }

    public void reset() {
        if (cells == null || cells.length == 0) {
            logger.severe("Trying to reset empty grid");
            return;
        }
        for (int row = 0, rows = cells.length; row < rows; row++) {
            for (int column = 0, columns = cells[0].length; column < columns; column++) {
                cells[row][column].reset();
            }
        }
    }

    public Boolean addPlayerToCell(int x, int y, Player p) {
        try {
            cells[x][y].add(y, p);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}