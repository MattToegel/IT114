package Project.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import Project.Common.TextFX.Color;

public class Cell {
    private Logger logger = Logger.getLogger(Cell.class.getName());
    // If a player (or player entity) can only have 1 owned item in this cell, used
    // hashmap
    // otherwise you'll need a list to allow duplicate references
    private ConcurrentHashMap<Long, String> playersInCell = new ConcurrentHashMap<Long, String>();
    private int x, y;
    // sample
    private String value;
    private List<Cell> adjacent = new ArrayList<Cell>();
    private CellType type = CellType.NONE;

    public static final String WALKABLE = "[ ]";
    public static final String UNWALABLE = "[x]";
    public static final String DRAGON = "[D]";
    public static final String START = "[S]";

    /**
     * Returns the position of other relative to this
     * 
     * @param other
     * @return
     */
    public String relativePosition(Cell other) {
        if (this.x == other.x && this.y < other.y) {
            return "right";
        } else if (this.x == other.x && this.y > other.y) {
            return "left";
        } else if (this.x < other.x && this.y == other.y) {
            return "down";
        } else if (this.x > other.x && this.y == other.y) {
            return "up";
        }
        return "invalid";
    }
    public void addAdjacent(Cell c) {
        if (c.equals(this)) {
            System.out.println(TextFX.colorize("A cell can't be adjacent to itself", Color.RED));
            return;
        }
        if (!adjacent.contains(c)) {
            adjacent.add(c);
        }
    }

    public boolean isConnected(Cell src) {
        return adjacent.contains(src);
    }

    public void printNeighbors() {
        System.out.println(String.format("Data for cell [%s,%s]", getX(), getY()));
        adjacent.forEach(c -> {
            String sa = String.format("(%s,%s)", c.getX(), c.getY());
            System.out.println(sa);
        });

    }

    /**
     * Add a player (or player entity) to this cell
     * 
     * @param clientId (who it belongs to)
     * @param name     (a simulation of entity)
     * @return true if player was added, false otherise
     * @throws Exception
     */
    public boolean addPlayer(long clientId, String name) {
        if (!playersInCell.contains(clientId)) {
            playersInCell.put(clientId, name);
            // updateCellValue();
            System.out.println(
                    TextFX.colorize(String.format("Player %s[%s] added to cell", name, clientId), Color.PURPLE));
            return true;
        } else {
            logger.warning(String.format("Player %s[%s] is already in this cell", name, clientId));
        }
        return false;

    }

    /**
     * Used to remove a player (or player entity) from this cell
     * 
     * @param clientId
     * @return true if player was removed, false otherise
     */
    public boolean removePlayer(long clientId) {
        if (playersInCell.containsKey(clientId)) {
            playersInCell.remove(clientId);
            // updateCellValue();
            return true;
        }
        return false;
    }

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.value = Cell.UNWALABLE;
        this.type = CellType.UNWALKABLE;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCellType(CellType type) {
        this.type = type;
    }

    public CellType getCellType() {
        return type;
    }
    public void reset() {
        playersInCell.clear();
    }

    public boolean isOccupied() {
        return playersInCell.size() > 0;
    }

    public int getNumberInCell() {
        return playersInCell.size();
    }
    @Override
    public String toString() {
        return isOccupied() ? String.format("[%s]", playersInCell.size()) : getValue();
    }

    public String getCoord() {
        return String.format("%s,%s", x, y);
    }
}
