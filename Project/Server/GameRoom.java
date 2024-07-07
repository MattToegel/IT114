package Project.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import Project.Common.Card;
import Project.Common.Cell;
import Project.Common.Deck;
import Project.Common.Grid;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.Player;
import Project.Common.TimedEvent;
import Project.Common.Tower;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;

    private Grid grid = null;

    private Deck deck = null;

    private List<ServerPlayer> turnOrder = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int round = 0;
    private final int ENERGY_PER_ROUND = 5;
    private final int TURN_DURATION = 90;

    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerPlayer sp) {
        // sync GameRoom state to new client
        syncCurrentPhase(sp);
        syncReadyStatus(sp);
        if (currentPhase != Phase.READY) {
            syncGridDimensions(sp);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerPlayer sp) {

    }

    // timer handlers
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> System.out.println("Round Time: " + time));
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private void startTurnTimer() {
        // Depending when you saw this
        // Fixed in Deck/Card lesson, had incorrectly referenced roundTimer instead of
        // turnTimer. Applied the fix to older branches to avoid inconsistencies though
        turnTimer = new TimedEvent(TURN_DURATION, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> System.out.println("Turn Time: " + time));
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        grid = new Grid(4, 4);
        try {
            deck = new Deck("Project/cards.txt");
        } catch (IOException e) {
            e.printStackTrace();
            onSessionEnd();
            return;
        }
        deck.shuffle();
        sendGridDimensions();

        turnOrder.clear();
        playersInRoom.values().stream().filter(ServerPlayer::isReady).forEach(p -> {
            turnOrder.add(p); // add ServerPlayer reference
        });
        drawHands();
        Collections.shuffle(turnOrder); // random order
        round = 0;
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        // startRoundTimer(); // not using rounds as turns in this lesson
        // eachDrawCard(); // if everyone draws at once
        round++;
        sendMessage(null, "Round: " + round);
        sendResetTurnStatus();
        currentPlayerIndex = -1; // so nextPlayer makes it index 0
        LoggerUtil.INSTANCE.info("onRoundStart() end");
        onTurnStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        startTurnTimer();
        nextPlayer();
        ServerPlayer sp = getCurrentPlayer();
        if(isEliminated()){
            String message = String.format("Skipped %s(%s) due to being eliminated");
            sendMessage(null, message);
            LoggerUtil.INSTANCE.info(message);
            onTurnStart();
            return;
        }
        sendMessage(null, String.format("It's %s's turn", getCurrentPlayer().getClientName()));
        drawCard();
        sp.incrementEnergy(ENERGY_PER_ROUND);
        sendPlayerCurrentEnergy(sp);
        sp.refreshTowers();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring
        ServerPlayer sp = getCurrentPlayer();
        sp.setTakeTurn(true);
        sendTurnStatus(sp);
        int current = sp.getEnergy();
        // cap energy
        if (current > sp.getEnergyCap()) {
            int diff = current - sp.getEnergyCap();
            sp.decrementEnergy(diff);
            sendPlayerCurrentEnergy(sp);
            sendMessage(null, String.format("%s(%s) burned %s energy", sp.getClientName(), sp.getClientId(), diff));
        }
        LoggerUtil.INSTANCE.info("onTurnEnd() end");
        if (isWinConditionMet()) {
            onSessionEnd();
            return;
        }
        if (isRoundOver()) {
            onRoundEnd(); // next round
        } else {
            onTurnStart(); // next player
        }
    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetTurnTimer(); // just in case it's still active if we forgot to end it sooner
        resetRoundTimer(); // reset timer if round ended without the time expiring

        LoggerUtil.INSTANCE.info("onRoundEnd() end");
        // Currently don't have any end session logic for end of round
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        grid.reset();
        resetTurnTimer(); // just in case it's still active if we forgot to end it sooner
        resetRoundTimer(); // just in case it's still active if we forgot to end it sooner
        // clear towers and energy
        playersInRoom.values().forEach(p -> {
            p.clearTowers();
            p.setEnergy(0);
        });
        sendResetHands();
        sendGridDimensions();
        sendResetTurnStatus();
        resetReadyStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // misc logic
    @Deprecated // removing this as the Deck/Cards lesson introduces turns instead of just
                // rounds
    private void checkIfAllTookTurns() {
        long ready = playersInRoom.values().stream().filter(p -> p.isReady()).count();
        long tookTurn = playersInRoom.values().stream().filter(p -> p.isReady() && p.didTakeTurn()).count();
        if (ready == tookTurn) {
            // example of some end session condition 2
            if (grid.areAllCellsOccupied()) {
                sendMessage(null, "Congrats, you filled the grid");
                onSessionEnd();
            } else {
                sendResetTurnStatus();
                onRoundStart();
                sendMessage(null, "Move again");
            }

        }
    }

    /**
     * Hand initialization
     */
    private void drawHands() {
        // either works
        // playersInRoom.values().stream().filter(p -> p.isReady()).forEach(p -> {
        turnOrder.forEach(p -> {
            List<Card> cards = deck.draw(3);
            if (cards == null || cards.size() == 0) {
                LoggerUtil.INSTANCE.severe("Failed to draw cards");
            } else {
                p.addToHand(cards);
                syncPlayerHand(p);
            }

        });
    }

    private void drawCard() {
        ServerPlayer current = getCurrentPlayer();
        Card card = deck.draw();
        if (card != null) {
            current.addToHand(card);
            syncAddCard(current, card);
        } else {
            LoggerUtil.INSTANCE.severe("Failed to draw a card");
        }
    }

    // example if Players all draw at once
    private void eachDrawCard() {
        // either works
        // playersInRoom.values().stream().filter(p -> p.isReady()).forEach(p -> {
        turnOrder.forEach(p -> {
            Card card = deck.draw();
            if (card != null) {
                p.addToHand(card);
                syncAddCard(p, card);
            } else {
                LoggerUtil.INSTANCE.severe("Failed to draw a card");
            }
        });
    }

    /**
     * Early exit (via exception throwing) if the user isn't in the room
     * 
     * @param client
     * @throws Exception
     */
    protected void checkCurrentPlayer(ServerThread client) throws Exception {
        if (getCurrentPlayer().getClientId() != client.getClientId()) {
            LoggerUtil.INSTANCE.severe("Player isn't the current Player");
            client.sendMessage("It's not your turn");
            throw new Exception("Player isn't the current Player");
        }
    }

    /**
     * Early exit (via exception throwing) if the player can't afford the action
     * 
     * @param sp
     * @param cost positive cost
     * @throws Exception
     */
    protected void checkCost(ServerPlayer sp, int cost) throws Exception {
        if (sp.getEnergy() - cost <= 0) {
            sp.getServerThread().sendMessage("You can't afford to do that");
            LoggerUtil.INSTANCE.info("Player can't afford action");
            throw new Exception("Player can't afford action");
        }
    }

    protected void checkFinishedTurn(ServerPlayer sp) throws Exception {
        if (sp.didTakeTurn()) {
            sp.getServerThread().sendMessage("You already completed your turn");
            LoggerUtil.INSTANCE.info("Player already completed their turn");
            throw new Exception("Player already completed their turn");
        }
    }
    // misc end

    // turn helpers start
    private ServerPlayer getCurrentPlayer() {
        return turnOrder.get(currentPlayerIndex);
    }

    private ServerPlayer nextPlayer() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= turnOrder.size()) {
            currentPlayerIndex = 0;
        }
        return getCurrentPlayer();
    }

    private boolean isRoundOver() {
        int check = currentPlayerIndex + 1;
        return check >= turnOrder.size();
    }

    private boolean isWinConditionMet() {
        // find players with 0 towers (they didn't go yet) and players with at least 1
        // alive tower (not eliminated)
        List<ServerPlayer> remainingPlayers = turnOrder.stream()
                .filter(p -> p.getTotalTowers() == 0 || (p.getTotalTowers() > 0 && p.getTowersAlive() > 0))
                .collect(Collectors.toList());
        int size = remainingPlayers.size();
        if(size == 1){
            ServerPlayer sp = remainingPlayers.get(0);
            sendMessage(null, String.format("%s(%s) successfully eliminated all other opponents", sp.getClientName(), sp.getClientId()));
        }
        return size <= 1;
    }
    private boolean isEliminated(){
        ServerPlayer sp = getCurrentPlayer();
        return sp.getTotalTowers() > 0 && sp.getTowersAlive() == 0;
    }
    // turn helpers end

    // send/sync data to ServerPlayer(s)
    private void sendPlayerCurrentEnergy(ServerPlayer sp) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendPlayerCurrentEnergy(sp.getClientId(), sp.getEnergy());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    private void sendTowerStatus(int x, int y, Tower t) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendTowerStatus(x, y, t);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    private void sendResetHands() {
        playersInRoom.values().removeIf(spInRoom -> {
            spInRoom.setHand(null); // reset server data
            // using DEFAULT_CLIENT_ID as a trigger, prevents needing a nested loop to
            // update the status of each player to each player
            boolean failedToSend = !spInRoom.sendCardsInHand(null);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    private void syncRemoveCard(ServerPlayer sp, Card card) {
        sp.sendRemoveCardFromHand(card);
    }

    private void syncAddCard(ServerPlayer sp, Card card) {
        sp.sendAddCardToHand(card);
    }

    private void syncPlayerHand(ServerPlayer sp) {
        sp.sendCardsInHand(sp.getHand());
    }

    /**
     * Sends a movement coordinate of one Player to all Players (including
     * themselves)
     * 
     * @param sp
     * @param x
     * @param y
     */
    private void sendMove(ServerPlayer sp, int x, int y) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendMove(sp.getClientId(), x, y);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    /**
     * A shorthand way of telling all clients to reset their local list's turn
     * status
     */
    private void sendResetTurnStatus() {
        playersInRoom.values().removeIf(spInRoom -> {
            spInRoom.setTakeTurn(false); // reset server data
            // using DEFAULT_CLIENT_ID as a trigger, prevents needing a nested loop to
            // update the status of each player to each player
            boolean failedToSend = !spInRoom.sendTurnStatus(Player.DEFAULT_CLIENT_ID, false);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    /**
     * Sends the turn status of one Player to all Players (including themselves)
     * 
     * @param sp
     */
    private void sendTurnStatus(ServerPlayer sp) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendTurnStatus(sp.getClientId(), sp.didTakeTurn());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    private void syncGridDimensions(ServerPlayer sp) {
        sp.sendGridDimensions(grid.getRows(), grid.getCols());
    }

    private void sendGridDimensions() {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendGridDimensions(grid.getRows(), grid.getCols());
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    // end send data to ServerPlayer(s)

    // receive data from ServerThread (GameRoom specific)

    protected void handleEndTurn(ServerThread st) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkFinishedTurn(sp);
            onTurnEnd();
        } catch (Exception e) {

        }
    }

    /**
     * Handles a Player's attempt to allocate or deallocate energy to/from a Tower
     * based on coordinate
     * 
     * @param st
     * @param x
     * @param y
     * @param energy positive for allocation, negative for deallocation
     */
    protected void handleAllocationChange(ServerThread st, int x, int y, int energy) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkFinishedTurn(sp);
            Tower playersTower = grid.getCell(x, y).getTower();
            if (playersTower == null) {
                st.sendMessage("Invalid coordinate for allocation");
                return;
            }
            if (playersTower.getClientId() != st.getClientId()) {
                st.sendMessage("You can only control your towers");
                return;
            }
            if(playersTower.didAllocate()){
                st.sendMessage("You can only do one allocation event per Tower per turn");
                return;
            }
            int absEnergy = Math.abs(energy);
            LoggerUtil.INSTANCE.info(String.format("Player's current energy %s", sp.getEnergy()));
            if (energy > 0) {
                if (sp.decrementEnergy(absEnergy)) {
                    playersTower.allocateEnergy(absEnergy); // adds
                    LoggerUtil.INSTANCE
                            .info(String.format("Allocated %s energy to tower %s", absEnergy, playersTower.getId()));
                } else {
                    LoggerUtil.INSTANCE.info(
                            String.format("Failed to allocate %s energy to tower %s", absEnergy, playersTower.getId()));
                    st.sendMessage("You can't afford to allocate that much energy");
                    return;
                }
            } else {
                if (playersTower.getAllocatedEnergy() - absEnergy < 0) {
                    LoggerUtil.INSTANCE.info(String.format("Failed to deallocate %s energy from tower %s", absEnergy,
                            playersTower.getId()));
                    st.sendMessage("You can't deallocate more energy than the tower has");
                    return;
                }
                playersTower.allocateEnergy(energy); // removes
                LoggerUtil.INSTANCE
                        .info(String.format("Deallocated %s energy to tower %s", absEnergy, playersTower.getId()));
                sp.incrementEnergy(absEnergy);
            }
            LoggerUtil.INSTANCE.info(String.format("Player's energy after alloc logic %s", sp.getEnergy()));
            sendPlayerCurrentEnergy(sp);
            playersTower.setDidAllocate(true);
            sendTowerStatus(x, y, playersTower);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleAttack(ServerThread st, int x, int y, List<Long> targets) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkFinishedTurn(sp);
            Tower playersTower = grid.getCell(x, y).getTower();
            if (playersTower == null) {
                st.sendMessage("Invalid coordinate for source of attack");
                return;
            }
            if (playersTower.getClientId() != st.getClientId()) {
                st.sendMessage("You can only control your towers");
                return;
            }
            if (targets == null || targets.isEmpty()) {
                st.sendMessage("You need to provide at least one target");
                return;
            }
            // TODO prevent the same tower from attacking twice
            if (playersTower.didAttack()) {
                st.sendMessage("You can only attack once per tower per turn");
                return;
            }
            // validate range and targets
            final List<Cell> cellsInRange = grid.getValidCellsWithinRange(x, y, playersTower.getRange())
                    .stream().filter(c -> c.isOccupied() // only consider occupied cells
                            && targets.contains(c.getTower().getId()) // filter out targets that match the ids the
                                                                      // client sent
                                                                      // over (validation)
                            && c.getTower().getHealth() > 0 // make sure targets are alive (doesn't make sense to attack
                                                            // a
                                                            // defeated tower)
                    ).collect(Collectors.toList());
            if (cellsInRange == null || cellsInRange.isEmpty()) {
                st.sendMessage("No valid targets in range");
                return;
            }
            // get defenders from cells
            List<Tower> defenders = cellsInRange.stream().map(c -> c.getTower()).collect(Collectors.toList());

            // handle combat
            Tower.calculateDamage(playersTower, defenders, (defender, damage) -> {
                playersTower.setDidAttack(true);
                boolean defeated = defender.getHealth() <= 0;

                Cell towersCell = cellsInRange.stream().filter(c -> c.getTower().getId() == defender.getId())
                        .findFirst().orElse(null);
                if (towersCell != null) {
                    sendMessage(null,
                            String.format("Tower[%s] attacked Tower[%s] for %s damage %s",
                                    playersTower.getId(),
                                    defender.getId(), damage,
                                    defeated ? ": Tower Defeated" : ""));
                    sendTowerStatus(towersCell.getX(), towersCell.getY(), defender);
                    if (defeated) {
                        // TODO make this elegant
                        grid.getCell(towersCell.getX(), towersCell.getY()).updateTower(null);
                        ServerPlayer defenderPlayer = playersInRoom.get(defender.getClientId());
                        if (defenderPlayer.getTowersAlive() == 0) {
                            // TODO handle elimination
                            sendMessage(null, String.format("%s(%s) eliminated %s(%s)", sp.getClientName(),
                                    sp.getClientId(), defenderPlayer.getClientName(), defenderPlayer.getClientId()));
                        }
                    }
                } else {
                    LoggerUtil.INSTANCE.severe("Failed to find Tower's Cell during damage resolution");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handlePlaceTower(ServerThread st, int x, int y) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkFinishedTurn(sp);
            if (grid.getCell(x, y).isOccupied()) {
                st.sendMessage("This cell is already occupied");
                return;
            }
            int tempCost = 1; // TODO: adjust this based on game rules later
            checkCost(sp, tempCost);
            Tower newTower = null;
            try {
                // create a new tower for this player
                newTower = new Tower(sp.getClientId());

                // rule for first tower placement
                if (sp.getTotalTowers() == 0) {
                    if (x != 0 && y != 0) {
                        st.sendMessage("Your first tower must start at the edge");
                        return;
                    }
                } else { // rule for subsequent tower placement
                    List<Cell> validCells = grid.getValidCellsWithinRange(x, y, 1);
                    Cell target = validCells.stream().filter(c -> c.getTower().getClientId() == sp.getClientId())
                            .findFirst().orElse(null);
                    if (target == null) {
                        st.sendMessage("Towers must be placed adjacent to your own towers");
                        return;
                    }
                }

                // attempt to allocate tower to cell
                grid.setCell(x, y, newTower);
                // assign tower
                sp.setTower(newTower);
                // send update
                sendTowerStatus(x, y, newTower);
                // decrease energy
                sp.decrementEnergy(tempCost);
                // send energy update
                sendPlayerCurrentEnergy(sp);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleDiscardCard(ServerThread st, Card card) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            // TODO finish this
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            if (sp.removeFromHand(card) == null) {
                LoggerUtil.INSTANCE.severe("User doesn't have this card in hand: " + card);
                return;
            }
            syncRemoveCard(sp, card);
            sendMessage(null, String.format("%s discarded %s", st.getClientName(), card));
            // example turn end condition
            onTurnEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleUseCard(ServerThread st, Card card) {
        try {
            checkCurrentPhase(st, Phase.IN_PROGRESS);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            // TODO finish this
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            if (sp.removeFromHand(card) == null) {
                LoggerUtil.INSTANCE.severe("User doesn't have this card in hand: " + card);
                return;
            }
            syncRemoveCard(sp, card);
            sendMessage(null, String.format("%s used %s", st.getClientName(), card));
            // example turn end condition
            onTurnEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    protected void handleMove(ServerThread st, int x, int y) {
        try {
            checkPlayerInRoom(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            if (!sp.isReady()) {
                st.sendMessage("You weren't ready in time");
                return;
            }
            if (sp.didTakeTurn()) {
                st.sendMessage("You already took your turn");
                return;
            }
            if (grid.getCell(x, y).isOccupied()) {
                st.sendMessage("This cell is already occupied");
                return;
            }
            // grid.setCell(x, y, true);
            sendMove(sp, x, y);
            sp.setTakeTurn(true);
            sendTurnStatus(sp);
            checkIfAllTookTurns();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // end receive data from ServerThread (GameRoom specific)
}
