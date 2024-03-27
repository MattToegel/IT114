package Project.Server;

import Project.Common.Cell;
import Project.Common.Constants;
import Project.Common.Phase;
import Project.Common.Player;
import Project.Common.TextFX;
import Project.Common.TextFX.Color;

public class ServerPlayer extends Player {
    private ServerThread client;
    private Cell currentCell;

    public ServerPlayer(ServerThread t) {
        client = t;
        System.out.println(TextFX.colorize("Wrapped ServerThread " + t.getClientName(), Color.CYAN));
    }

    // getters/setters
    public long getClientId() {
        if (client == null) {
            return Constants.DEFAULT_CLIENT_ID;
        }
        return client.getClientId();
    }

    public String getClientName() {
        if (client == null) {
            return "";
        }
        return client.getClientName();
    }

    /**
     * Removes player from existing cell, and adds them to new cell
     * 
     * @param cell
     */
    protected void setCell(Cell cell) {
        setCell(cell, true);
    }

    /**
     * Adds a player to a cell, conditionally removes them from a previous cell
     * 
     * @param cell
     * @param removeFromCurrent
     */
    protected void setCell(Cell cell, boolean removeFromCurrent) {
        if (cell == null) {
            this.currentCell = null;
            return;
        }
        if (this.currentCell != null && removeFromCurrent) {
            // TODO handle leaving cell
            this.currentCell.removePlayer(getClientId());
        }
        this.currentCell = cell;
        cell.addPlayer(getClientId(), getClientName());
    }

    public int getCellX() {
        if (this.currentCell == null) {
            return -1;
        }
        return this.currentCell.getX();
    }

    public int getCellY() {
        if (this.currentCell == null) {
            return -1;
        }
        return this.currentCell.getY();
    }

    // send wrappers
    public void sendGridDimensions(int x, int y) {
        if (client == null) {
            return;
        }
        client.sendGridDimensions(x, y);
    }

    public void sendPlayerPosition(long clientId, int x, int y) {
        if (client == null) {
            return;
        }
        client.sendPlayerPosition(clientId, x, y);
    }
    public void sendPhase(Phase phase) {
        if (client == null) {
            return;
        }
        client.sendPhase(phase.name());
    }


    public void sendReadyState(long clientId, boolean isReady) {
        if (client == null) {
            return;
        }
        client.sendReadyState(clientId, isReady);
    }

    public void sendPlayerTurnStatus(long clientId, boolean didTakeTurn) {
        if (client == null) {
            return;
        }
        client.sendPlayerTurnStatus(clientId, didTakeTurn);
    }

    public void sendResetLocalTurns() {
        if (client == null) {
            return;
        }
        client.sendResetLocalTurns();
    }

    public void sendResetLocalReadyState() {
        if (client == null) {
            return;
        }
        client.sendResetLocalReadyState();
    }

    public void sendCurrentPlayerTurn(long clientId) {
        if (client == null) {
            return;
        }
        client.sendCurrentPlayerTurn(clientId);
    }
}
