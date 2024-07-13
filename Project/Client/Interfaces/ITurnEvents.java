package Project.Client.Interfaces;

public interface ITurnEvents extends IClientEvents {
    /**
     * Notifies whose turn it currently is
     * @param clientId
     */
    void onCurrentTurn(long clientId);
}
