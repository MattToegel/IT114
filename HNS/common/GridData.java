package HNS.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GridData implements Serializable {
    private List<CellData> cells = new ArrayList<CellData>();

    public List<CellData> getCells() {
        return cells;
    }

    public void setCells(List<CellData> cells) {
        this.cells = cells;
    }

    public void addCell(CellData cell) {
        this.cells.add(cell);
    }

    public void clearPlayers() {
        for (CellData cellData : cells) {
            cellData.setPlayersInCell(new long[0]);
        }
    }
}
