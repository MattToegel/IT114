package HNS.server;

import HNS.common.Player;

public class ServerPlayer extends Player implements Comparable<ServerPlayer> {
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

    /**
     * Returns a negative integer, zero, or a positive integer, depending on whether
     * the ServerPlayer clientId is less than, equal to, or greater than the
     * specified
     * ServerPlayer clientId.
     * 
     * @param other
     * @return
     */
    @Override
    public int compareTo(ServerPlayer other) {
        return (int) (this.getClient().getClientId() - other.getClient().getClientId());
    }

    public boolean isSame(ServerPlayer other) {
        return this.compareTo(other) == 0;
    }
}
