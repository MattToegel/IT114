package DCT.common;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import DCT.client.ClientPlayer;
import DCT.common.exceptions.InvalidMoveException;
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
            if(cells[0] == null){
                continue;
            }
            for (int column = 0, columns = cells[0].length; column < columns; column++) {
                if(cells[row][column] == null){
                    continue;
                }
                cells[row][column].reset();
            }
        }
    }

    public List<String> validateMove(int x, int y, Character character) {
        List<String> validations = new ArrayList<String>();
        List<Cell> n = null;//GridHelpers.getNeighborCells(start, cells);
        /* TODO this is for later once character stats are utilized
        if(character.getCurrentLife() <= 0){
            validations.add(String.format("%s is unconcious and can't move.", character.getName()));
        }
        else*/ 
        if (!character.isInCell()) {
            n = GridHelpers.getNeighborCells(start, cells);
            // Rule #1 must be adjacent to start door
            //target cell must be in the adjacent list
            Cell c = n.stream().filter(_c->_c.isSameCoordinate(cells[x][y]) && !(_c instanceof DoorCell) && !(_c instanceof WallCell)).findFirst().orElse(null);
            /*DoorCell dc = n.stream().filter(c -> (c instanceof DoorCell && c.isSameCoordinate(start))).map(c -> (DoorCell) c)
                    .findFirst()
                    .orElse(null);*/
            if(c == null){
                validations.add("First move must be adjacent to the starting door");
            }
        }
        else{
            n = GridHelpers.getNeighborCells(character.getCurrentCell(), cells);
            Cell target = n.stream().filter(c->c.getX() == x && c.getY() == y).findFirst().orElse(null);
            if(target == null){
                validations.add("Can only move to an adjacent tile");
            }
            else{
                if(target instanceof WallCell){
                    validations.add("Can't move to a Wall tile");
                }
                else if(target instanceof DoorCell){
                    boolean locked = ((DoorCell)target).isLocked();
                    if(locked){
                        validations.add("The door is locked");
                    }//todo check not locked
                }
                else{
                    if(target.isBlocked()){
                        validations.add("This tile is blocked, find another way");
                    }
                }
            }
        }

        return validations;
    }

    /**
     * Adds a character to the cell at coord x,y.
     * Assigns characters's current cell if successful
     * 
     * @param x
     * @param y
     * @param character
     * @return
     * @throws InvalidMoveException
     */
    public Boolean addCharacterToCellValidate(int x, int y, Character character) throws InvalidMoveException {
        List<String> messages = validateMove(x, y, character);
        if(messages != null && !messages.isEmpty()){
            throw new InvalidMoveException(messages);
        }
        return addCharacterToCell(x, y, character);
    }
    public Boolean addCharacterToCell(int x, int y, Character character) throws InvalidMoveException {
        try {
            Cell previous = character.getCurrentCell();
            if (previous != null) {
                removeCharacterFromCell(previous.getX(), previous.getY(), character);
            }
            Player p = character.getController();
            long clientId = Constants.DEFAULT_CLIENT_ID;
            if (p instanceof ServerPlayer) {
                clientId = ((ServerPlayer) p).getClient().getClientId();
            } else if (p instanceof ClientPlayer) {
                clientId = ((ClientPlayer) p).getClientId();
            }
            cells[x][y].add(clientId, character);
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
        Player p = character.getController();
        long clientId = Constants.DEFAULT_CLIENT_ID;
        if (p instanceof ServerPlayer) {
            clientId = ((ServerPlayer) p).getClient().getClientId();
        } else if (p instanceof ClientPlayer) {
            clientId = ((ClientPlayer) p).getClientId();
        }
        boolean status = removeCharacterFromCell(x, y, clientId);
        if (status) {
            character.setCurrentCell(null);
        } else {
            logger.info("removeCharacterFromCell(x,y,character) failed removing character from cell");
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
            e.printStackTrace();
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

    public boolean reachedEnd(Cell c){
        return end.isSameCoordinate(c);
    }
    public int totalCharactersInGrid() {
        int total = 0;
        for (int row = 0, rows = cells.length; row < rows; row++) {
            for (int column = 0, columns = cells[0].length; column < columns; column++) {
                total += cells[row][column].getNumberOfCharactersInCell();
            }
        }
        return total;
    }

    public void print() {
        GridHelpers.printGrid(cells);
    }

    public List<CellData> getCellsARoundPoint(int x, int y) {
        List<Cell> subset = GridHelpers.getCellsWithinRangeList(x, y, cells);
        return subset.stream().filter(c -> c != null).map(c -> {
            CellData cd = new CellData();
            cd.map(c);
            return cd;
        }).toList();
    }

    public CellData getCellData(int x, int y) {
        Cell cell = cells[x][y];
        CellData cd = new CellData();
        cd.map(cell);
        return cd;
    }

    public void update(List<CellData> data, Hashtable<Long, ClientPlayer> players) {
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
                    //set local start/end
                    if(cd.getCellType() == CellType.START_DOOR){
                        start = (DoorCell)cells[x][y];
                    }
                    else if(cd.getCellType() == CellType.END_DOOR){
                        end = (DoorCell)cells[x][y];
                    }
                }

                ((DoorCell) cells[x][y]).setLocked(locked);

            } else if (cd.getCellType() == CellType.WALL) {
                if (!(cells[x][y] instanceof WallCell)) {
                    if (cells[x][y] != null) {
                        cells[x][y].reset();
                    }
                    cells[x][y] = new WallCell(x, y);
                }
            } else if (cd.getCellType() == CellType.TILE) {
                if (!(cells[x][y] instanceof Cell)) {
                    if (cells[x][y] != null) {
                        cells[x][y].reset();
                    }
                    cells[x][y] = new Cell(x, y);
                }
            }
            if (!pcs.isEmpty()) {
                Cell cell = cells[x][y];
                // remove characters no longer in cell
                if (cell != null && pcs != null && !pcs.isEmpty()) {
                    cell.removeDifference(pcs);
                    // add characters to cell
                    for (Long clientId : pcs) {
                        if (players.containsKey(clientId)) {
                            Character c = players.get(clientId).getCharacter();
                            if (c != null) {
                                try {
                                    addCharacterToCell(x, y, c);
                                } catch (InvalidMoveException e) {
                                    // This shouldn't happen on the client side
                                    e.printStackTrace();
                                }
                            }
                        }
                    } // TODO characters not getting removed from cells properly at least on client
                      // side, confirm server side
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
