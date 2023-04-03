package HNS.common;

import java.io.Serializable;

public class CellData implements Serializable {
    private int x, y;
    private long[] playersInCell;
    private boolean isBlocked;

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
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

    public void setCoord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public long[] getPlayersInCell() {
        return playersInCell;
    }

    public void setPlayersInCell(long[] playersInCell) {
        this.playersInCell = playersInCell;
    }
}
