package Project.Client.Interfaces;

public interface IGridEvents extends IGameEvents {
    /**
     * Receives grid dimensions (-1,-1 is used to reset)
     * @param rows
     * @param columns
     */
    void onReceiveGrid(int rows, int columns);
}
