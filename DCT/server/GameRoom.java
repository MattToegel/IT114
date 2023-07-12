package DCT.server;

import DCT.common.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

import DCT.common.Character.ActionType;
import DCT.common.Character.CharacterType;
import DCT.common.exceptions.CharacterAlreadyAssignedException;
import DCT.common.exceptions.InvalidMoveException;
import DCT.server.CharacterFactory.ControllerType;
import DCT.common.Constants;
import DCT.common.Grid;
import DCT.common.Phase;
import DCT.common.TimedEvent;
import DCT.common.CellData;
import DCT.common.Character;

public class GameRoom extends Room {
    Phase currentPhase = Phase.READY;
    private static Logger logger = Logger.getLogger(GameRoom.class.getName());
    private TimedEvent readyTimer = null;
    private ConcurrentHashMap<Long, ServerPlayer> players = new ConcurrentHashMap<Long, ServerPlayer>();
    private Grid grid = new Grid();
    private Character currentTurnCharacter = null;
    Random rand = new Random();
    private List<Character> turnOrder = new ArrayList<Character>();

    public GameRoom(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /**
     * Attempts to lookup and load a character
     * 
     * @param client
     * @param character expected to contain search/lookup criteria, not an actual
     *                  full character reference
     */
    protected void loadCharacter(ServerThread client, Character charData) {
        // for now using character code to fetch
        String characterCode = charData.getCode();
        String[] parts = characterCode.split("-");
        if (parts.length >= 2) {
            String position = parts[0];
            String code = parts[1];
            Consumer<Character> callback = character -> {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Character created: ").append(character.getName()).append("\n");
                    sb.append("Character level: ").append(character.getLevel()).append("\n");
                    sb.append("Character type: ").append(character.getType()).append("\n");
                    sb.append("Character action type: ").append(character.getActionType()).append("\n");
                    sb.append("Character stats: ").append("\n");
                    sb.append("Attack: ").append(character.getAttack()).append("\n");
                    sb.append("Vitality: ").append(character.getVitality()).append("\n");
                    sb.append("Defense: ").append(character.getDefense()).append("\n");
                    sb.append("Will: ").append(character.getWill()).append("\n");
                    sb.append("Luck: ").append(character.getLuck()).append("\n");
                    sb.append("Progression Rate: ").append(character.getProgressionRate()).append("\n");
                    sb.append("Range: ").append(character.getRange()).append("\n");

                    System.out.println(sb.toString());
                    assignCharacter(client, character);
                    // client.sendCharacter(client.getClientId(), character);
                    syncCharacter(client.getClientId(), character);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            CharacterFactory.loadCharacter(position, code, callback);
        }
    }

    /**
     * Attempts to create a random character of the given type (TANK, DAMAGE,
     * SUPPORT)
     * 
     * @param client
     * @param ct
     */
    protected void createCharacter(ServerThread client, CharacterType ct) {
        Consumer<Character> callback = character -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("Character created: ").append(character.getName()).append("\n");
                sb.append("Character level: ").append(character.getLevel()).append("\n");
                sb.append("Character type: ").append(character.getType()).append("\n");
                sb.append("Character action type: ").append(character.getActionType()).append("\n");
                sb.append("Character stats: ").append("\n");
                sb.append("Attack: ").append(character.getAttack()).append("\n");
                sb.append("Vitality: ").append(character.getVitality()).append("\n");
                sb.append("Defense: ").append(character.getDefense()).append("\n");
                sb.append("Will: ").append(character.getWill()).append("\n");
                sb.append("Luck: ").append(character.getLuck()).append("\n");
                sb.append("Progression Rate: ").append(character.getProgressionRate()).append("\n");
                sb.append("Range: ").append(character.getRange()).append("\n");

                System.out.println(sb.toString());
                assignCharacter(client, character);
                // client.sendCharacter(client.getClientId(), character);
                syncCharacter(client.getClientId(), character);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        CharacterFactory.createCharacter(ControllerType.PLAYER, ct, 1, Utils.randomEnum(ActionType.class), callback);
    }

    @Override
    protected void addClient(ServerThread client) {
        logger.info("Adding client as player");
        players.computeIfAbsent(client.getClientId(), id -> {
            ServerPlayer player = new ServerPlayer(client);
            super.addClient(client);
            logger.info(String.format("Total clients %s", clients.size()));// change visibility to protected
            return player;
        });
    }

    protected void setReady(ServerThread client) {
        logger.info("Ready check triggered");
        if (currentPhase != Phase.READY) {
            logger.warning(String.format("readyCheck() incorrect phase: %s", Phase.READY.name()));
            return;
        }
        if (readyTimer == null) {
            sendMessage(null, "Ready Check Initiated, 30 seconds to join");
            readyTimer = new TimedEvent(30, () -> {
                readyTimer = null;
                readyCheck(true);
            });
        }
        // Hashmaps allow fast lookup by keys
        if (players.containsKey(client.getClientId())) {
            ServerPlayer sp = players.get(client.getClientId());
            sp.setReady(true);
            logger.info(String.format("Marked player %s[%s] as ready", sp.getClient().getClientName(), sp
                    .getClient().getClientId()));
            syncReadyStatus(sp.getClient().getClientId());
        }
        /*
         * Example demonstrating stream api and filters (not ideal in this scenario
         * since a hashmap has a more officient approach)
         * This concept may be beneficial in the future for other lookup data
         * players.values().stream().filter(p -> p.getClient().getClientId() ==
         * client.getClientId()).findFirst()
         * .ifPresent(p -> {
         * p.setReady(true);
         * logger.info(String.format("Marked player %s[%s] as ready",
         * p.getClient().getClientName(), p
         * .getClient().getClientId()));
         * syncReadyStatus(p.getClient().getClientId());
         * });
         */
        readyCheck(false);
    }

    private void readyCheck(boolean timerExpired) {
        if (currentPhase != Phase.READY) {
            return;
        }
        // two examples for the same result
        // int numReady = players.values().stream().mapToInt((p) -> p.isReady() ? 1 :
        // 0).sum();
        long numReady = players.values().stream().filter(ServerPlayer::isReady).count();
        if (numReady >= Constants.MINIMUM_PLAYERS) {

            if (timerExpired) {
                sendMessage(null, "Ready Timer expired, starting session");
                start();
            } else if (numReady >= players.size()) {
                sendMessage(null, "Everyone in the room marked themselves ready, starting session");
                if (readyTimer != null) {
                    readyTimer.cancel();
                    readyTimer = null;
                }
                start();
            }

        } else {
            if (timerExpired) {
                resetSession();
                sendMessage(null, "Ready Timer expired, not enough players. Resetting ready check");
            }
        }
    }

    private void start() {
        updatePhase(Phase.SELECTION);
        // TODO example
        sendMessage(null,
                "Session started: Create or Load your characters via /createcharacter or /loadcharacter <code>");
        new TimedEvent(30, () -> generateDungeon());
    }

    private void generateDungeon() {
        int width = 5, height = 5;
        if (grid.hasCells()) {
            grid.reset();
        }
        grid.build(width, height);
        // TODO sync grid subset
        syncGridDimensions(width, height);
        grid.print();
        List<CellData> startCells = grid.getCellsARoundPoint(grid.getStartDoor().getX(), grid.getStartDoor().getY());
        syncCells(startCells);
        // setup characters
        turnOrder = players.values().stream().filter(p -> p.isReady() && p.hasCharacter()).map(p -> p.getCharacter())
                .toList();
        // TODO sorting

        nextTurn();
    }

    // start handle next turn
    private void nextTurn() {
        updatePhase(Phase.TURN);
        if (currentTurnCharacter == null) {
            currentTurnCharacter = turnOrder.get(0);
        } else {
            int currentIndex = turnOrder.indexOf(currentTurnCharacter);
            currentIndex++;
            if (currentIndex >= turnOrder.size()) {
                currentIndex = 0;
            }
            currentTurnCharacter = turnOrder.get(currentIndex);
        }
        if (currentTurnCharacter != null) {
            ServerPlayer sp = ((ServerPlayer) currentTurnCharacter.getController());
            syncCurrentTurn(sp.getClient().getClientId());
            sendMessage(null, String.format("It's %s's turn", sp.getClient().getClientName()));
            cancelReadyTimer();
            // TODO set back to lower number after debugging
            readyTimer = new TimedEvent(3000, () -> {
                sendMessage(null,
                        String.format("%s took to long and has been skipped", sp.getClient().getClientName()));
                nextTurn();
            });
        }
    }

    private synchronized void syncCurrentTurn(long clientId) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendCurrentTurn(clientId);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    private void syncGridDimensions(int x, int y) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendGridDimensions(x, y);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    private void syncCells(List<CellData> cells) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendCells(cells);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    // end handle next turn
    private void cancelReadyTimer() {
        if (readyTimer != null) {
            readyTimer.cancel();
            readyTimer = null;
        }
    }

    public void handleMove(int x, int y, ServerThread client) {
        ServerPlayer currentPlayer = (ServerPlayer) currentTurnCharacter.getController();
        if (currentPlayer.getClient().getClientId() != client.getClientId()) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            return;
        }
        if (currentTurnCharacter.isInCell()) {
            logger.info(currentTurnCharacter.getName() + " is in a cell before move");
        }
        boolean success = false;
        try {
            success = grid.addCharacterToCellValidate(x, y, currentTurnCharacter);
        } catch (InvalidMoveException ime) {
            sendMessage(null, String.join("\n", ime.getMessages()));
        }
        if (success) {
            cancelReadyTimer();
            sendMessage(null, String.format("%s moved to cell %s,%s", currentTurnCharacter.getName(), x, y));
            // Sync cells around target cell
            List<CellData> startCells = grid.getCellsARoundPoint(x, y);
            syncCells(startCells);
            /*
             * // Sync a single cell: TODO sync actual cell data
             * CellData cd = grid.getCellData(x, y);
             * if (cd != null) {
             * List<CellData> cell = new ArrayList<CellData>();
             * cell.add(cd);
             * syncCells(cell);
             * 
             * } else {
             * logger.severe(String.format("Cell[%s][%s] is null", x, y));
             * }
             */
            grid.print();
            if (grid.reachedEnd(currentTurnCharacter.getCurrentCell())) {
                endDungeon();
            } else {
                nextTurn();
            }

        } else {
            String error = String.format("%s failed to move to cell %s,%s", currentTurnCharacter.getName(), x, y);
            logger.info(error);
            sendMessage(null, error);
        }
    }

    private void endDungeon(){
        //TODO give experience / rewards

        Iterator<Character> iter = turnOrder.iterator();
        while(iter.hasNext()){
            Character c = iter.next();
            if(c.isInCell()){
                grid.removeCharacterFromCell(c.getCurrentCell().getX(), c.getCurrentCell().getY(), c);
            }
        }
        grid.reset();
        syncGridReset();
        resetSession();//TODO allow the session to continue a new dungeon or quit rather than just resetting
    }
    private synchronized void syncGridReset(){
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendGridReset();
            if (!success) {
                handleDisconnect(client);
            }
        }
    }
    private synchronized void resetSession() {
        players.values().stream().forEach(p -> p.setReady(false));
        updatePhase(Phase.READY);
        sendMessage(null, "Session ended, please intiate ready check to begin a new one");
    }

    private void updatePhase(Phase phase) {
        if (currentPhase == phase) {
            return;
        }
        currentPhase = phase;
        // NOTE: since the collection can yield a removal during iteration, an iterator
        // is better than relying on forEach
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendPhaseSync(currentPhase);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    protected void handleDisconnect(ServerPlayer player) {
        if (players.containsKey(player.getClient().getClientId())) {
            players.remove(player.getClient().getClientId());
            super.handleDisconnect(null, player.getClient()); // change visibility to protected
            logger.info(String.format("Total clients %s", clients.size()));
            sendMessage(null, player.getClient().getClientName() + " disconnected");
            if (players.isEmpty()) {
                close();
            }
        }
    }

    private void syncReadyStatus(long clientId) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendReadyStatus(clientId);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }

    // handle character
    private void assignCharacter(ServerPlayer player, Character character) throws Exception {
        if (player.hasCharacter()) {
            throw new CharacterAlreadyAssignedException("Character already assigned");
        }
        player.assignCharacter(character);
    }

    private void assignCharacter(ServerThread client, Character character) {
        try {
            ServerPlayer sp = players.get(client.getClientId());
            assignCharacter(sp, character);
        } catch (CharacterAlreadyAssignedException ce) {
            if (currentPhase != Phase.SELECTION) {
                client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You already have a character assigned");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncCharacter(long clientId, Character character) {
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            boolean success = client.getClient().sendCharacter(clientId, character);
            if (!success) {
                handleDisconnect(client);
            }
        }
    }
    @Override
    public void close(){
        super.close();
        players.clear();
        players = null;
        currentTurnCharacter = null;
        //turnOrder.clear(); // this is actually an immutable array so can't clear it
        turnOrder = null;
    }
}
