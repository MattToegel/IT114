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
import DCT.common.GridHelpers;
import DCT.common.Phase;
import DCT.common.TimedEvent;
import DCT.common.Cell;
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
    private List<Character> enemies = new ArrayList<Character>();

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
            syncGameState(client);
            logger.info(String.format("Total clients %s", clients.size()));// change visibility to protected
            return player;
        });
    }

    private void syncGameState(ServerThread incomingClient) {
        // single data
        // sync grid
        if (grid.hasCells()) {
            incomingClient.sendGridDimensions(grid.getRows(), grid.getColumns());
        } else {
            incomingClient.sendGridReset();
        }
        if (currentTurnCharacter != null) {
            incomingClient
                    .sendCurrentTurn(currentTurnCharacter.getClientId());
        }
        incomingClient.sendPhaseSync(currentPhase);
        Iterator<ServerPlayer> iter = players.values().stream().iterator();
        while (iter.hasNext()) {
            ServerPlayer client = iter.next();
            if (client.getClient().getClientId() == incomingClient.getClientId()) {
                continue;
            }
            Character c = client.getCharacter();
            boolean success = false;
            if (c != null) {
                success = incomingClient.sendCharacter(client.getClient().getClientId(), c);
            }
            if (client.isReady()) {
                success = incomingClient.sendReadyStatus(client.getClient().getClientId());
            }

            if (!success) {
                break;
            }
        }
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
        updatePhase(Phase.PREPARING);
        int width = 5, height = 5;
        if (grid.hasCells()) {
            grid.reset();
        }
        grid.build(width, height);
        // ignore 'self is never closed' warning, we just want the reference for the
        // callback
        GameRoom self = this;
        GridHelpers.populateEnemies(grid, (enemies) -> {
            self.enemies = enemies;
            self.enemies.forEach(e -> e.fullHeal());// init enemies
            begin();
        });

    }

    private void begin() {
        // TODO sync grid subset
        syncGridDimensions(grid.getRows(), grid.getColumns());
        grid.print();
        List<CellData> startCells = grid.getCellsARoundPoint(grid.getStartDoor().getX(), grid.getStartDoor().getY());
        syncCells(startCells);
        // setup characters
        turnOrder = players.values().stream().filter(p -> p.isReady() && p.hasCharacter()).map(p -> p.getCharacter())
                .toList();
        turnOrder.forEach(c -> c.fullHeal()); // init players
        // TODO sorting

        determineTurn();
    }

    // start handle next turn
    private void determineTurn() {
        updatePhase(Phase.TURN);
        boolean isAITurn = false;
        if (currentTurnCharacter == null) {
            currentTurnCharacter = turnOrder.get(0);
        } else {
            int currentIndex = turnOrder.indexOf(currentTurnCharacter);
            currentIndex++;
            if (currentIndex >= turnOrder.size()) {
                currentIndex = 0;
                isAITurn = true;
            }
            currentTurnCharacter = turnOrder.get(currentIndex);
        }
        if (currentTurnCharacter.isAlive()) {
            if (!isAITurn) {
                startTurn();
            } else {
                doAI();
            }
        } else {
            sendMessage(null,
                    String.format("%s is no longer alive, checking for next player", currentTurnCharacter.getName()));
            long numAlive = turnOrder.stream().filter(c -> c.isAlive()).count();
            if (numAlive > 0) {
                determineTurn();
            } else {
                sendMessage(null, "All players have been defeated.");
                endDungeon();
            }
        }
    }

    private void startTurn() {
        if (currentTurnCharacter != null) {
            long clientId = currentTurnCharacter.getClientId();
            String clientName = currentTurnCharacter.getClientName();
            currentTurnCharacter.startTurn();
            syncCurrentTurn(clientId);
            sendMessage(null, String.format("It's %s's turn", clientName));
            cancelReadyTimer();
            // TODO set back to lower number after debugging
            readyTimer = new TimedEvent(3000, () -> {
                sendMessage(null,
                        String.format("%s took to long and has been skipped", clientName));
                determineTurn();
            });
        }
    }

    private boolean doAttackLogic(Character enemy) {
        if (enemy.didAttack()) {
            logger.info("enemy already attacked");
            return false;
        }
        Character target = enemy.getTarget(); // store target as a new target may be found during the attack logic
        long dmg = enemy.attackCurrentTarget();
        if (dmg > 0) {
            enemy.usedAttack();
            sendMessage(null, String.format("%s attacked %s for %s damage", enemy.getName(),
                    target.getName(), dmg));
            if (target != null && !target.isAlive()) {
                sendMessage(null, String.format("%s defeated %s", enemy.getName(),
                        target.getName()));
            }
            return true;
        } else {
            logger.info("enemy failed to attack");
        }
        return false;
    }

    public boolean doMoveLogic(Character enemy) {
        if (enemy.didMove()) {
            logger.info("enemy already moved");
            return false;
        }
        Cell closest = GridHelpers.getClosestCellToTarget(enemy, enemy.getTarget(),
                grid.getCells());
        if (closest != null) {
            try {
                boolean success = grid.addCharacterToCellValidate(closest.getX(), closest.getY(), enemy);
                if (success) {
                    enemy.usedMove();
                    sendMessage(null,
                            String.format("%s moved to %s, %s", enemy.getName(), closest.getX(), closest.getY()));
                    List<CellData> nearby = grid.getCellsARoundPoint(closest.getX(), closest.getY());
                    syncCells(nearby);
                    return true;
                }
                logger.info("Enemy failed to move");

            } catch (InvalidMoveException e1) {
                e1.printStackTrace();
            }
        } else {
            logger.info("Couldn't find closest cell");
        }
        return false;
    }

    private void findClosestTarget(Character enemy) {
        // find closest target
        int closestDist = Integer.MAX_VALUE;
        Character closest = null;
        for (Character target : turnOrder) {
            if (!target.isAlive()) {
                continue;
            }
            int dist = Character.getDistanceBetween(enemy, target);
            if (dist <= closestDist) {
                closestDist = dist;
                closest = target;
            }
        }
        if (closest != null) {
            enemy.setTarget(closest);
        }
    }

    private void doAI() {
        sendMessage(null, "It's the enemy's turn.");
        new Thread() {
            @Override
            public void run() {
                List<Character> aliveEnemies = enemies.stream().filter(e -> e.isAlive()).toList();

                for (Character enemy : aliveEnemies) {
                    enemy.startTurn();
                    try { // delay action to allow visual simulation on client side
                        Thread.sleep(1500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (!enemy.hasTarget()) {
                        findClosestTarget(enemy);
                    }

                    if (!enemy.hasTarget()) {
                        logger.info("Enemy couldn't find target");
                        continue;
                    }
                    if (enemy.currentTargetWithinRange()) {
                        doAttackLogic(enemy);

                    } else {
                        doMoveLogic(enemy);
                        if (enemy.currentTargetWithinRange()) {
                            doAttackLogic(enemy);
                        }
                    }
                    if (enemy.didMove()) {
                        logger.info("Enemy moved");
                    }
                    if (enemy.didAttack()) {
                        logger.info("Enemy attacked");
                    }
                    if (!enemy.didMove() && !enemy.didAttack()) {
                        sendMessage(null, String.format("%s did nothing", enemy.getName()));
                    }
                }
                startTurn();
            }
        }.start();
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

    private boolean isActionValid(String type, ServerThread client) {
        long clientId = currentTurnCharacter.getClientId();
        if (clientId != client.getClientId()) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            return false;
        }
        // ignore move as this is validated via the grid
        if (!currentTurnCharacter.isInCell() && !"move".equals(type)) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    String.format("Your character must be on the board to %s", type));
            return false;
        }
        if (currentTurnCharacter.getType() != CharacterType.SUPPORT && "heal".equals(type)) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "Only SUPPORT type characters can heal");
            return false;
        }
        String incomingType = type;
        if (currentTurnCharacter.didAttack() && "attack".equals(type)) {
            type = "attacked";
        } else if (currentTurnCharacter.didHeal() && "heal".equals(type)) {
            type = "healed";
        } else if (currentTurnCharacter.didMove() && "move".equals(type)) {
            type = "moved";
        }
        if (!incomingType.equals(type)) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Your character already %s this turn", type));
            return false;
        }
        return true;
    }

    private boolean isValidRange(int x, int y, ServerThread client) {
        Cell cell = currentTurnCharacter.getCurrentCell();
        Cell targetCell = grid.getCell(x, y);
        int dist = GridHelpers.getManhattanDistance(cell.getPoint(), targetCell.getPoint());
        if (dist > currentTurnCharacter.getRange()) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format(
                    "The target is outside of your characters range of %s tiles", currentTurnCharacter.getRange()));
            return false;
        }
        return true;
    }

    private List<Character> getCharactersOfTypeInCell(int x, int y, ControllerType controllerType) {
        Cell targetCell = grid.getCell(x, y);
        List<Character> characters = targetCell.getCharactersInCell().stream().filter(c -> {
            if (controllerType == ControllerType.PLAYER) {
                return c.getClientId() > 0;
            } else if (controllerType == ControllerType.NPC) {
                return c.getClientId() < 1;
            }
            return c != null;
        }).toList();
        return characters;
    }

    // user actions
    public void handleEndturn(ServerThread client) {
        if (!isActionValid("end turn", client)) {
            return;
        }
        determineTurn();
    }

    public void handleHeal(int x, int y, ServerThread client) {
        if (!isActionValid("heal", client)) {
            return;
        }
        if (!isValidRange(x, y, client)) {
            return;
        }
        List<Character> players = getCharactersOfTypeInCell(x, y, ControllerType.PLAYER);
        for (Character ally : players) {
            long heal = ally.receiveHeal(currentTurnCharacter);
            sendMessage(null,
                    String.format("%s healed %s for %s health", currentTurnCharacter.getName(), ally.getName(), heal));
            currentTurnCharacter.usedHeal();
        }
        if (currentTurnCharacter.actionsExhausted()) {
            determineTurn();
        }
    }

    public void handleAttack(final int x, final int y, ServerThread client) {
        if (!isActionValid("attack", client)) {
            return;
        }
        if (!isValidRange(x, y, client)) {
            return;
        }

        List<Character> enemiesInCell = getCharactersOfTypeInCell(x, y, ControllerType.NPC);
        for (Character enemy : enemiesInCell) {
            long dmg = enemy.takeDamage(currentTurnCharacter);
            if (dmg > 0) {
                sendMessage(null,
                        String.format("%s hit %s for %s damage", currentTurnCharacter.getName(), enemy.getName(), dmg));
                currentTurnCharacter.usedAttack();
            }

            if (!enemy.isAlive()) {
                sendMessage(null, String.format("%s has been defeated", enemy.getName()));
                grid.removeCharacterFromCell(x, y, enemy.getClientId());
                List<CellData> nearby = grid.getCellsARoundPoint(x, y);
                syncCells(nearby);
            }
        }
        if (currentTurnCharacter.actionsExhausted()) {
            determineTurn();
        }
    }

    public void handleMove(final int x, final int y, ServerThread client) {
        if (!isActionValid("move", client)) {
            return;
        }
        boolean success = false;
        try {
            success = grid.addCharacterToCellValidate(x, y, currentTurnCharacter);
            if (success) {
                currentTurnCharacter.usedMove();
            }
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
                return;
            }
        } else {
            String error = String.format("%s failed to move to cell %s,%s", currentTurnCharacter.getName(), x, y);
            logger.info(error);
            sendMessage(null, error);
        }
        if (currentTurnCharacter.actionsExhausted()) {
            determineTurn();
        }
    }

    // end user actions
    private void endDungeon() {
        // TODO give experience / rewards

        Iterator<Character> iter = turnOrder.iterator();
        while (iter.hasNext()) {
            Character c = iter.next();
            if (c.isInCell()) {
                grid.removeCharacterFromCell(c.getCurrentCell().getX(), c.getCurrentCell().getY(), c);
            }
        }
        grid.reset();
        syncGridReset();
        resetSession();// TODO allow the session to continue a new dungeon or quit rather than just
                       // resetting
    }

    private synchronized void syncGridReset() {
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
        turnOrder = null;
        players.values().stream().forEach(p -> {
            p.setReady(false);
            p.assignCharacter(null);
        });
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
    public void close() {
        super.close();
        players.clear();
        players = null;
        currentTurnCharacter = null;
        // turnOrder.clear(); // this is actually an immutable array so can't clear it
        turnOrder = null;
    }
}
