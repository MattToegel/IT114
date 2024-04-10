package Project.Common;

import java.io.Serializable;

public class CellData implements Serializable {
    private CellType cellType;


    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
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

    private int x, y;

    private int numInCell;

    public int getNumInCell() {
        return numInCell;
    }

    public void setNumInCell(int numInCell) {
        this.numInCell = numInCell;
    }
}
