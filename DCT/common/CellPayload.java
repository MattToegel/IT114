package DCT.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Added support to pass multiple cells in this payload.
 * However, typical expected usage is to send 1 (or few) rather than the entire grid.
 * This is because we typically don't share all the cell info with the players to avoid possible explotation.
 * The server should remain the only one that knows hidden information and we should only sync "public" knowledge to clients.
 */
public class CellPayload extends Payload{
    public CellPayload(){
        setPayloadType(PayloadType.CELL);
    }

    private List<CellData> cellData = new ArrayList<CellData>();

    public List<CellData> getCellData() {
        return cellData;
    }

    public void setCellData(List<CellData> cellData) {
        this.cellData = cellData;
    }
    
    public void addCellData(CellData cellData){
        this.cellData.add(cellData);
    }
}
