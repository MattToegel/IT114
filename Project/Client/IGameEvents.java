package Project.Client;

import java.util.List;

import Project.Common.CellData;
import Project.Common.Phase;

public interface IGameEvents extends IClientEvents {
    /**
     * Triggered when a player marks themselves ready
     * 
     * @param clientId Use -1 to reset the list
     */
    void onReceiveReady(long clientId, boolean isReady);

    /**
     * Triggered when client receives phase update from server
     * 
     * @param phase
     */
    void onReceivePhase(Phase phase);

    /**
     * Triggered when client receives grid info
     * 
     * @param rows    use <= 0 to reset
     * @param columns use <= 0 to reset
     */
    void onReceiveGrid(int rows, int columns);

    void onReceiveCell(List<CellData> cells);

    void onReceiveRoll(long clientId, int roll);

    void onReceivePoints(long clientId, int changedPoints, int currentPoints);
}
