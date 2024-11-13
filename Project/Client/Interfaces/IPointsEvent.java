package Project.Client.Interfaces;

public interface IPointsEvent extends IGameEvents {
    /**
     * Receives the current phase
     * 
     * @param phase
     */
    void onPointsUpdate(long clientId, int points);
}