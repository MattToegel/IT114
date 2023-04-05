package HNS.common;

import java.util.logging.Logger;
import HNS.server.ServerPlayer;

public class Grid {
    private Cell[][] cells = null;
    private static Logger logger = Logger.getLogger(Grid.class.getName());
    private int rows, columns;

    public int getRowCount() {
        return rows;
    }

    public int getColumnCount() {
        return columns;
    }

    public void build(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
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
            logger.info(Constants.ANSI_RED
                    + String.format("Added to Cell[%s,%s] has %s", x, y, cells[x][y].playersInCell.size())
                    + Constants.ANSI_RESET);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
            logger.info(Constants.ANSI_RED
                    + String.format("Removed from Cell[%s,%s] has %s", x, y, cells[x][y].playersInCell.size())
                    + Constants.ANSI_RESET);
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
                long count = cells[row][column].playersInCell.size();
                total += count;
                // logger.info(Constants.ANSI_RED + String.format("Cell[%s,%s] has %s", row,
                // column, count)
                // + Constants.ANSI_RESET);
            }
        }
        return total;
    }

    /**
     * Called on the client side to import data from server
     * 
     * @param gd
     */
    public void importData(GridData gd) {
        for (CellData cd : gd.getCells()) {
            try {
                Cell cell = cells[cd.getX()][cd.getY()];
                cell.reset();
                for (long clientId : cd.getPlayersInCell()) {
                    cell.add(clientId, new Player());// don't need a real reference for now
                }
                cell.setBlocked(cd.isBlocked());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public GridData export() {
        GridData gd = new GridData();
        for (int row = 0, rows = cells.length; row < rows; row++) {
            for (int column = 0, columns = cells[0].length; column < columns; column++) {
                long[] playersInCell = cells[row][column].playersInCell.values().stream()
                        .mapToLong(p -> ((ServerPlayer) p).getClient().getClientId()).toArray();
                CellData cd = new CellData();
                cd.setBlocked(cells[row][column].isBlocked());
                cd.setCoord(row, column);
                cd.setPlayersInCell(playersInCell);
                gd.addCell(cd);
            }
        }
        return gd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Iterate through each row and column
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                // Append each element to the string builder with some formatting
                sb.append(String.format("%s", cells[i][j])); // Assuming the grid contains integers
            }
            sb.append(System.lineSeparator()); // Start a new line after each row
        }
        return sb.toString();
    }
}