package DCT.common;

public class WallCell extends Cell {

    public WallCell(int x, int y) {
        super(x, y, true);
        cellType = CellType.WALL;
    }
}
