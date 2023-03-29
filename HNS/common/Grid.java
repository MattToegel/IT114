package HNS.common;

import java.util.logging.Logger;

import HNS.server.ServerPlayer;

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

    /**
     * Adds a player to the cell at coord x,y.
     * Assigns player's current cell if successful
     * 
     * @param x
     * @param y
     * @param p
     * @return
     */
    public Boolean addPlayerToCell(int x, int y, Player p) {
        try {
            cells[x][y].add(y, p);
            p.setCurrentCell(cells[x][y]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Removes a player from a cell at coord x,y by ServerPlayer.
     * Calls {@link Grid#removePlayerFromCell(int, int, long)}
     * 
     * @param x  row
     * @param y  column
     * @param sp player to remove
     * @return success
     */
    public boolean removePlayerFromCell(int x, int y, ServerPlayer sp) {
        boolean status = removePlayerFromCell(x, y, sp.getClient().getClientId());
        if (status) {
            sp.setCurrentCell(null);
        }
        return status;
    }

    /**
     * Removes a player from cell at coord x,y by clientId
     * 
     * @param x        row
     * @param y        column
     * @param clientId reference to remove
     * @return success
     */
    public boolean removePlayerFromCell(int x, int y, long clientId) {
        try {
            cells[x][y].remove(clientId);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Cell getCell(int x, int y) {
        try {
            return cells[x][y];
        } catch (Exception e) {
            return null;
        }
    }

    public int remaining() {
        int total = 0;
        for (int row = 0, rows = cells.length; row < rows; row++) {
            for (int column = 0, columns = cells[0].length; column < columns; column++) {
                total += cells[row][column].playersInCell.values().stream().count();
            }
        }
        return total;
    }
}