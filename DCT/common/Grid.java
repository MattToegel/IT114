package DCT.common;

import java.util.logging.Logger;

import DCT.server.ServerPlayer;

public class Grid {
    private Cell[][] cells = null;
    private static Logger logger = Logger.getLogger(Grid.class.getName());

    public boolean hasCells(){
        return cells != null;
    }

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
     * Adds a character to the cell at coord x,y.
     * Assigns characters's current cell if successful
     * 
     * @param x
     * @param y
     * @param character
     * @return
     */
    public Boolean addCharacterToCell(int x, int y, Character character) {
        try {
            Cell previous = character.getCurrentCell();
            if(previous != null){
                removeCharacterFromCell(x, y, character);
            }
            //fixes vs code issue with y
            ServerPlayer sp = (ServerPlayer)character.getController();
            cells[x][y].add(sp.getClient().getClientId(), character);
            character.setCurrentCell(cells[x][y]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Removes a character from a cell at coord x,y by Character reference.
     * Calls {@link Grid#removeCharacterFromCell(int, int, long)}
     * 
     * @param x  row
     * @param y  column
     * @param character character to remove
     * @return success
     */
    public boolean removeCharacterFromCell(int x, int y, Character character) {
        ServerPlayer sp = (ServerPlayer)character.getController();
        boolean status = removeCharacterFromCell(x, y, sp.getClient().getClientId());
        if (status) {
            character.setCurrentCell(null);
        }
        return status;
    }

    /**
     * Removes a character from cell at coord x,y by clientId
     * 
     * @param x        row
     * @param y        column
     * @param clientId reference to remove
     * @return success
     */
    public boolean removeCharacterFromCell(int x, int y, long clientId) {
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

    public int totalCharactersInGrid() {
        int total = 0;
        for (int row = 0, rows = cells.length; row < rows; row++) {
            for (int column = 0, columns = cells[0].length; column < columns; column++) {
                total += cells[row][column].charactersInCell.values().stream().count();
            }
        }
        return total;
    }
}
