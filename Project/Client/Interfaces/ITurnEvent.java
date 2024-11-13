package Project.Client.Interfaces;

public interface ITurnEvent extends IGameEvents{
    /**
     * Receives the current phase
     * 
     * @param phase
     */
    void onTookTurn(long clientId, boolean didtakeCurn);
}
