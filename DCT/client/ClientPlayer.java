package DCT.client;

import DCT.common.Player;

public class ClientPlayer extends Player{
    private String clientName;
    private long clientId;
    
    public String getClientName() {
        return clientName;
    }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }
}
