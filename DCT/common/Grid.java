package DCT.common;

import java.util.List;
import java.util.logging.Logger;
import DCT.server.ServerPlayer;

public class Grid {
    private Cell[][] cells = null;
    private static Logger logger = Logger.getLogger(Grid.class.getName());
    private DoorCell start = null;
    private DoorCell end = null;

    public boolean hasCells() {
        return cells != null;
    }

    public DoorCell getStartDoor() {
        return start;
    }

    public DoorCell getEndDoor() {
        return end;
    }

    public void build(int rows, int columns) {
        if (cells != null) {
            reset();
        }
        logger.info("Starting grid generation");
        cells = new Cell[rows][columns];

        // Create walls for the entire grid initially
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                cells[row][column] = new WallCell(row, column);
            }
        }

        // Rule 1: Generate Start and End doors
        int startEdge = (int) (Math.random() * 4);
        int endEdge = (startEdge + 2) % 4; // Rule 2: Ensure end door is on a different edge
        // Generate Start Door
        DoorCell startDoor = GridHelpers.generateDoorOnEdge(rows, columns, startEdge);
        this.start = startDoor;
        cells[startDoor.getX()][startDoor.getY()] = this.start;

        // Generate End Door
        DoorCell endDoor = GridHelpers.generateDoorOnEdge(rows, columns, endEdge);
        this.end = endDoor;
        this.end.setEnd(true);
        cells[endDoor.getX()][endDoor.getY()] = this.end;

        // Rule 3: Generate a valid path from start door to end door
        GridHelpers.generatePath(startDoor, endDoor, cells);

        // Rule 4: Generate random branches from the main path
        GridHelpers.addBranches(cells);
        logger.info(String.format("Finished generating %s x %s grid", rows, columns));
    }

    public void buildBasic(int rows, int columns) {
        if (cells != null) {
            reset();
        }
        logger.info("Starting grid generation");
        cells = new Cell[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                cells[row][column] = null;// new WallCell(row, column);
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
            if (previous != null) {
                removeCharacterFromCell(x, y, character);
            }
            cells[x][y].add(y, character);
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
     * @param x         row
     * @param y         column
     * @param character character to remove
     * @return success
     */
    public boolean removeCharacterFromCell(int x, int y, Character character) {
        ServerPlayer sp = (ServerPlayer) character.getController();
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

    public void print() {
        GridHelpers.printGrid(cells);
    }

    public List<CellData> getCellsARoundPoint(int x, int y) {
        List<Cell> subset = GridHelpers.getCellsWithinRangeList(x, y, cells);
        return subset.stream().filter(c->c!=null).map(c -> {
            CellData cd = new CellData();
            cd.map(c);
            return cd;
        }).toList();
    }

    public void update(List<CellData> data) {
        data.stream().forEach(cd -> {
            int x = cd.getX();
            int y = cd.getY();
            boolean blocked = cd.isBlocked();
            boolean locked = cd.isLocked();
            List<Long> pcs = cd.getPlayerCharactersInCell();
            if (cd.getCellType() == CellType.START_DOOR || cd.getCellType() == CellType.END_DOOR) {
                if (!(cells[x][y] instanceof DoorCell)) {
                    if (cells[x][y] != null) {
                        cells[x][y].reset();
                    }
                    cells[x][y] = new DoorCell(x, y);
                }
                
                ((DoorCell) cells[x][y]).setLocked(locked);

            }
            else if(cd.getCellType() == CellType.WALL){
                if(!(cells[x][y] instanceof WallCell)){
                    if (cells[x][y] != null) {
                        cells[x][y].reset();
                    }
                    cells[x][y] = new WallCell(x, y);
                }
            }
            else if(cd.getCellType() == CellType.TILE){
                if(!(cells[x][y] instanceof Cell)){
                    if (cells[x][y] != null) {
                        cells[x][y].reset();
                    }
                    cells[x][y] = new Cell(x, y);
                }
            }

            cells[x][y].setBlocked(blocked);
             // TODO add characters to cell
        });

    }

    public static void main(String[] args) {
        Grid g = new Grid();
        g.build(25, 25);
        GridHelpers.printGrid(g.cells);

        Cell[][] n = GridHelpers.getCellsWithinRange2D(g.getStartDoor().getX(), g.getStartDoor().getY(), g.cells);
        GridHelpers.printGrid(n);
        System.out.println("");
        n = GridHelpers.getCellsWithinRange2D(g.getEndDoor().getX(), g.getEndDoor().getY(), g.cells);
        GridHelpers.printGrid(n);
    }
}
