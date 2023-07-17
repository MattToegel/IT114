package DCT.common;

public class DoorCell extends Cell {
    private boolean locked;

    private boolean open;
    private boolean isEnd = false;

    public DoorCell(int x, int y) {
        super(x, y, false);

    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        cellType = isEnd ? CellType.END_DOOR : CellType.START_DOOR;
        this.isEnd = isEnd;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
