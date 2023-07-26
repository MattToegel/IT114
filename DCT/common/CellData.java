package DCT.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CellData implements Serializable {
    private int x, y;
    private boolean blocked, locked;
    private List<Character> charactersInCell = new ArrayList<Character>();
    private CellType cellType;

    public int getX() {
        return x;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
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

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public List<Character> getCharactersInCell() {
        return charactersInCell;
    }

    public void map(Cell cell) {
        if (cell != null) {
            this.x = cell.getX();
            this.y = cell.getY();
            this.charactersInCell = cell.getCharactersInCell().stream().map(c -> {
                // hide info from players
                Character copy = (Character) c.clone();
                copy.setLuck(0);
                copy.setProgressionRate(0);
                return c;
            }).toList();
            this.blocked = cell.isBlocked();
            if (cell instanceof DoorCell) {
                this.cellType = ((DoorCell) cell).isEnd() ? CellType.END_DOOR : CellType.START_DOOR;
                this.locked = ((DoorCell) cell).isLocked();
            } else if (cell instanceof WallCell) {
                this.cellType = CellType.WALL;
            } else {
                this.cellType = CellType.TILE;
            }
        }
    }
}
