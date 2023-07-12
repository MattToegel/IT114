package DCT.common;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import DCT.server.ServerPlayer;

public class Cell {
    private ConcurrentHashMap<Long, Character> charactersInCell = new ConcurrentHashMap<Long, Character>();
    private int x, y;
    private boolean blocked = false;
    private static Logger logger = Logger.getLogger(Cell.class.getName());

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

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void reset() {
        logger.info(String.format("Cell[%s][%s] reset", x, y));
        charactersInCell.clear();
        blocked = true;
    }

    public void add(long clientId, Character character) {
        if (!charactersInCell.containsKey(clientId)) {
            charactersInCell.put(clientId, character);
            logger.info(String.format("add() Character for clientId [%s] added to cell: total %s", clientId,
                    charactersInCell.size()));
        } else {
            logger.info(String.format("add() Character for clientId [%s] already in cell: total %s", clientId,
                    charactersInCell.size()));
        }
    }

    public void remove(long clientId) {
        if (charactersInCell.containsKey(clientId)) {
            charactersInCell.remove(clientId);
            logger.info(String.format("remove() Character for clientId [%s] removed from cell: remaining %s", clientId,
                    charactersInCell.size()));
        } else {
            logger.info(String.format("remove() Character for clientId [%s] not in cell: remaining %s", clientId,
                    charactersInCell.size()));
        }
    }

    public void removeDifference(List<Long> clientIds) {
        charactersInCell.keySet().retainAll(clientIds);
    }

    public List<Character> getCharactersInCell() {
        return charactersInCell.values().stream().collect(Collectors.toList());
    }

    public List<Long> getClientIdsOfCharactersInCell() {
        return charactersInCell.keySet().stream().filter(v->v!=null).toList();
    }

    public int getNumberOfCharactersInCell() {
        return charactersInCell.size();
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

    public boolean isSameCoordinate(Cell compare){
        if(compare == null){
            return false;
        }
        return this.x == compare.getX() && this.y == compare.getY();
    }
}
