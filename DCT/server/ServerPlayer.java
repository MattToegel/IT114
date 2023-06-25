package DCT.server;

import DCT.common.Player;

public class ServerPlayer extends Player {
    private ServerThread client;

    public void setClient(ServerThread client) {
        this.client = client;
    }

    public ServerThread getClient() {
        return this.client;
    }

    public ServerPlayer(ServerThread client) {
        setClient(client);
    }
}
