package Project.Client.Interfaces;

import Project.Common.Grid;

public interface IGridEvents extends IGameEvents {
    /**
     * Receives grid dimensions (-1,-1 is used to reset)
     * @param grid 
     */
    void onReceiveGrid(Grid grid);
}
