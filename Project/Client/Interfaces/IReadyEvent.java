package Project.Client.Interfaces;

public interface IReadyEvent extends IGameEvents {
    /**
     * Receives the ready status and id
     * 
     * @param clientId
     * @param isReady
     */
    void onReceiveReady(long clientId, boolean isReady, boolean isQuiet);
}