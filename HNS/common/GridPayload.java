package HNS.common;

public class GridPayload extends Payload {
    private GridData grid;

    public GridPayload() {
        setPayloadType(PayloadType.GRID);
    }

    public GridData getGrid() {
        return grid;
    }

    public void setGrid(GridData grid) {
        this.grid = grid;
    }
}
