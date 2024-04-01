package Project.Client;

import Project.Common.Cell;
import Project.Common.Player;

public class ClientPlayer extends Player {
    private long clientId;
    private String clientName;
    private Cell currentCell;

    public void setCell(Cell c) {
        currentCell = c;
    }

    public Cell getCell() {
        return currentCell;
    }
    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
