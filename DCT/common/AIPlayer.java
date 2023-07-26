package DCT.common;

public class AIPlayer extends Player {
    private long clientId = -1;

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getClientId() {
        return clientId;
    }
}
