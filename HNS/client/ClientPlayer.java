package HNS.client;

import HNS.common.Player;

public class ClientPlayer extends Player {
    private long clientId;

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    private String clientName;

    public ClientPlayer(long clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
    }
}
