package Project.Client.Interfaces;

import Project.Common.Tower;

public interface ITowerEvents extends IGameEvents {
    /**
     * Current tower status at a coordinate
     * 
     * @param x
     * @param y
     * @param tower
     */
    void onReceiveTowerStatus(int x, int y, Tower tower);
}
