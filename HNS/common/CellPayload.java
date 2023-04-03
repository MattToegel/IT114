package HNS.common;

public class CellPayload extends Payload {
    private CellData cellData;

    public CellPayload() {
        setPayloadType(PayloadType.CELL);
    }

    public CellData getCellData() {
        return cellData;
    }

    public void setCellData(CellData cellData) {
        this.cellData = cellData;
    }
}
