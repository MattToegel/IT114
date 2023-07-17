package DCT.client;

import java.util.List;

import DCT.common.Cell;
import DCT.common.Character;
import DCT.common.Phase;

public interface IGameEvents extends IClientEvents {
    /**
     * Triggered when a player marks themselves ready
     * 
     * @param clientId Use -1 to reset the list
     */
    void onReceiveReady(long clientId);

    /**
     * Triggered when client receives phase update from server
     * 
     * @param phase
     */
    void onReceivePhase(Phase phase);

    /**
     * Triggered when client receives data from one or more cells
     * 
     * @param cells
     */
    void onReceiveCell(List<Cell> cells);

    /**
     * Triggered when client receives grid info
     * 
     * @param rows    use <= 0 to reset
     * @param columns use <= 0 to reset
     */
    void onReceiveGrid(int rows, int columns);

    /**
     * Triggered when a character is created/loaded from the server.
     * In the future will also be triggered for updates possibly
     * 
     * @param character
     */
    void onReceiveCharacter(long clientId, Character character);
}
