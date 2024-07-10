package Project.Client;

import Project.Common.Player;

/**
 * Client-only data about a client(player)
 * Formerly was called ClientData, renamed to ClientPlayer in ReadyCheck lesson/branch for non-chatroom projects.
 * If chatroom projects want to follow this design update the following in this lesson:
 * Player class renamed to User
 * clientPlayer class renamed to ClientUser (or the original ClientData)
 * ServerPlayer class renamed to ServerUser
 */
public class ClientPlayer extends Player {
    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public void reset() {
        super.reset();
        this.clientName = "";
    }
}
