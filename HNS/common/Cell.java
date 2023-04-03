package HNS.common;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import HNS.client.ClientPlayer;
import HNS.server.ServerPlayer;

public class Cell {
    ConcurrentHashMap<Long, Player> playersInCell = new ConcurrentHashMap<Long, Player>();
    private int x, y;
    private boolean blocked = false;

    public Cell(int x, int y, boolean blocked) {
        this(x, y);
        this.blocked = blocked;
    }

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.blocked = isBlocked;
    }

    public void reset() {
        playersInCell.clear();
        blocked = false;
    }

    public void add(long clientId, Player p) {
        playersInCell.computeIfAbsent(clientId, id -> {
            return p;
        });
    }

    public void remove(long clientId) {
        if (playersInCell.containsKey(clientId)) {
            playersInCell.remove(clientId);
        }
    }

    public List<Player> getPlayersInCell() {
        return playersInCell.values().stream().collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("In cell[%s, %s,] (", x, y));
        Iterator<Player> iter = playersInCell.values().iterator();
        while (iter.hasNext()) {
            Player p = iter.next();
            if (p instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer) p;
                sb.append(String.format("%s - %s", sp.getClient().getClientId(), sp.getClient().getClientName()));
            } else if (p instanceof ClientPlayer) {
                ClientPlayer cp = (ClientPlayer) p;
                sb.append(String.format("%s - %s", cp.getClientId(), cp.getClientName()));
            }
            sb.append(System.lineSeparator()); // Start a new line after each row
        }

        sb.append(")");
        return sb.toString();
    }
}
