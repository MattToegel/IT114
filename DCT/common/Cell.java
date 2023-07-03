package DCT.common;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import DCT.server.ServerPlayer;



public class Cell {
    ConcurrentHashMap<Long, Character> charactersInCell = new ConcurrentHashMap<Long, Character>();
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

    public void reset() {
        charactersInCell.clear();
        blocked = false;
    }

    public void add(long clientId, Character character) {
        charactersInCell.computeIfAbsent(clientId, id -> {
            return character;
        });
    }

    public void remove(long clientId) {
        if (charactersInCell.containsKey(clientId)) {
            charactersInCell.remove(clientId);
        }
    }

    public List<Character> getCharactersInCell() {
        return charactersInCell.values().stream().collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("In cell[%s, %s,] (", x, y));
        Iterator<Character> iter = charactersInCell.values().iterator();
        while (iter.hasNext()) {
            Character character = iter.next();
            Player p = character.getController();
            if (p instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer) p;
                sb.append(String.format("%s - %s", sp.getClient().getClientId(), sp.getClient().getClientName()));
            } else {
                sb.append(String.format("%s", "A player is here"));
            }
            sb.append(System.lineSeparator()); // Start a new line after each row
        }

        sb.append(")");
        return sb.toString();
    }
}
