package Project.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import Project.Common.BuffDebuff;
import Project.Common.Card;
import Project.Common.Card.CardName;
import Project.Common.Card.CardType;
import Project.Common.Cell;
import Project.Common.Deck;
import Project.Common.Grid;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.Player;
import Project.Common.TimedEvent;
import Project.Common.TimerType;
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
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + playersInRoom.size());
        if (playersInRoom.isEmpty()) {
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        }
    }

    // timer handlers
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> {
            System.out.println("Round Time: " + time);
            sendCurrentTime(TimerType.ROUND, time);
        });
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
            sendCurrentTime(TimerType.ROUND, -1);
        }
    }

    private void startTurnTimer() {
        // Depending when you saw this
        // Fixed in Deck/Card lesson, had incorrectly referenced roundTimer instead of
        // turnTimer. Applied the fix to older branches to avoid inconsistencies though
        turnTimer = new TimedEvent(TURN_DURATION, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> {
            System.out.println("Turn Time: " + time);
            sendCurrentTime(TimerType.TURN, time);
        });
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
            sendCurrentTime(TimerType.TURN, -1);
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.TURN);
        // using a seed to keep client/server grids "in sync"
        long seed = new Random().nextLong();
        grid = new Grid(4, 4, seed);
        try {
            deck = new Deck("Project/cards.txt");
            deck.shuffle();
        } catch (IOException e) {
            LoggerUtil.INSTANCE.severe("Error loading deck", e);
            e.printStackTrace();
            onSessionEnd();
            return;
        }

        sendGridDimensions();
        sendCurrentTurn(null);

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
        sendGameEvent("Round: " + round);
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
        if (isEliminated()) {
            String message = String.format("Skipped %s(%s) due to being eliminated");
            sendGameEvent(message);
            LoggerUtil.INSTANCE.info(message);
            onTurnStart();
            return;
        }
        // sendGameEvent(String.format("It's %s's turn",
        // getCurrentPlayer().getClientName()));
        sendCurrentTurn(getCurrentPlayer());
        drawCard();
        sp.incrementEnergy(ENERGY_PER_ROUND);
        // check for Energy Cells
        int bonus = sp.getTotalBonusEnergy();
        if (bonus > 0) {
            sp.incrementEnergy(bonus);
            sendGameEvent(String.format("%s[%s] gain %s bonus energy from occupied Cells", sp.getClientName(),
                    sp.getClientId(), bonus));
        }
        sendPlayerCurrentEnergy(sp);
        // check for bonus cards
        int bonusCards = sp.getTotalBonusCards();
        if (bonusCards > 0) {
            sendGameEvent(String.format("%s[%s] drawing %s extra cards from occupied Cells", sp.getClientName(),
                    sp.getClientId(), bonusCards));
            for (int i = 0; i < bonusCards; i++) {
                drawCard();
            }
        }
        // sp.refreshTowers(); // moved to turn end
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
            sendGameEvent(String.format("%s(%s) burned %s energy", sp.getClientName(), sp.getClientId(), diff));
        }
        int handSize = sp.getHand().size();
        if (handSize > 7) {
            Random rand = new Random();
            int diff = handSize - 7;
            sendGameEvent(String.format("%s(%s) was forced to discard %s random cards", sp.getClientName(),
                    sp.getClientId(), diff));
            for (int i = 0; i < diff; i++) {
                int randomIndex = rand.nextInt(handSize);
                Card card = sp.getHand().get(randomIndex);
                if (sp.removeFromHand(card) == null) {
                    // This shouldn't happen here
                    LoggerUtil.INSTANCE.severe("User doesn't have this card in hand: " + card);
                    continue;
                }
                syncRemoveCard(sp, card);
                handSize = sp.getHand().size();
            }

        }
        LoggerUtil.INSTANCE.info("onTurnEnd() end");
        if (isWinConditionMet()) {
            onSessionEnd();
            return;
        }
        sp.refreshTowers();
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
        if (grid != null) {
            grid.reset();
        }
        resetTurnTimer(); // just in case it's still active if we forgot to end it sooner
        resetRoundTimer(); // just in case it's still active if we forgot to end it sooner
        // clear towers and energy
        LoggerUtil.INSTANCE.info("Resetting Towers and Energy");
        playersInRoom.values().forEach(p -> {
            p.clearTowers();
            p.setEnergy(0);
        });
        sendPlayerCurrentEnergy(null);
        LoggerUtil.INSTANCE.fine("Resetting current turn");
        sendCurrentTurn(null);
        LoggerUtil.INSTANCE.fine("Resetting hands");
        sendResetHands();
        LoggerUtil.INSTANCE.fine("Retting Grid");
        sendGridDimensions();
        LoggerUtil.INSTANCE.fine("Resetting Turn Status");
        sendResetTurnStatus();
        LoggerUtil.INSTANCE.fine("Resetting Ready Status");
        resetReadyStatus();
        LoggerUtil.INSTANCE.fine("Changing Phase to READY");
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // misc logic

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
            client.sendGameEvent("It's not your turn");
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
        // fixed cost to allow full expense (i.e., 0 remaining)
        if (sp.getEnergy() - cost < 0) {
            sp.getServerThread().sendGameEvent("You can't afford to do that");
            LoggerUtil.INSTANCE
                    .info(String.format("Player can't afford action. Cost %s Available %s", sp.getEnergy(), cost));
            throw new Exception("Player can't afford action");
        }
    }

    protected void checkFinishedTurn(ServerPlayer sp) throws Exception {
        if (sp.didTakeTurn()) {
            sp.getServerThread().sendGameEvent("You already completed your turn");
            LoggerUtil.INSTANCE.info("Player already completed their turn");
            throw new Exception("Player already completed their turn");
        }
    }

    private void placeTower(ServerPlayer sp, int x, int y, int cost) throws Exception {
        if (grid.getCell(x, y).isOccupied()) {
            sp.getServerThread().sendGameEvent("This cell is already occupied");
            return;
        }

        checkCost(sp, cost);
        Tower newTower = null;
        try {
            // create a new tower for this player
            newTower = new Tower(sp.getClientId());

            // rule for first tower placement
            if (sp.getTotalTowers() == 0) {
                // fixed logic after demo
                if ((x > 0 && x < grid.getCols() - 1) && (y > 0 && y < grid.getRows() - 1)) {
                    sp.getServerThread().sendGameEvent("Your first tower must start at the edge");
                    return;
                }
            } else { // rule for subsequent tower placement
                List<Cell> validCells = grid.getValidCellsWithinRange(x, y, 1);
                Cell target = validCells.stream()
                        .filter(c -> c.getTower() != null && c.getTower().getClientId() == sp.getClientId())
                        .findFirst().orElse(null);
                if (target == null) {
                    sp.getServerThread().sendGameEvent("Towers must be placed adjacent to your own towers");
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
            if (cost > 0) {
                sp.decrementEnergy(cost);
                // send energy update
                sendPlayerCurrentEnergy(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
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
        if (size == 1) {
            ServerPlayer sp = remainingPlayers.get(0);
            sendGameEvent(String.format("%s(%s) successfully eliminated all other opponents", sp.getClientName(),
                    sp.getClientId()));
        }
        return size <= 1;
    }

    private boolean isEliminated() {
        ServerPlayer sp = getCurrentPlayer();
        return sp.getTotalTowers() > 0 && sp.getTowersAlive() == 0;
    }
    // turn helpers end

    // card helpers start

    private List<Cell> getAllOwnedTargets(int x, int y, int range, long clientId) {
        return grid.getValidCellsWithinRange(x, y, range).stream()
                .filter(c -> c.getTower() != null && c.getTower().getClientId() == clientId)
                .collect(Collectors.toList());
    }

    private List<Cell> getAllUnownedTargets(int x, int y, int range, long clientId) {
        return grid.getValidCellsWithinRange(x, y, range).stream()
                .filter(c -> c.getTower() != null && c.getTower().getClientId() != clientId)
                .collect(Collectors.toList());
    }

    private void applyBuffDebuff(int cardNumber, List<Cell> cells, String emptyMessage) {
        cells.forEach(c -> {
            Tower t = c.getTower();
            BuffDebuff bd = Card.createBuffDebuff(cardNumber);
            t.addBuffDebuff(bd);
            sendTowerStatus(c.getX(), c.getY(), t);
        });
        if (cells.size() == 0) {
            sendGameEvent(emptyMessage);
        }
    }

    private ServerPlayer getRandomPlayerWithHand(ServerPlayer sp) {
        List<ServerPlayer> targets = playersInRoom.values().stream()
                .filter(p -> p.isReady() && p.getHand().size() > 0 && p.getClientId() != sp.getClientId())
                .collect(Collectors.toList());
        return targets.get(new Random().nextInt(targets.size()));
    }

    private void processBuffDebuff(ServerPlayer sp, Card card, int x, int y, Tower tower) {
        CardName cardName = card.getCardNameEnum();
        switch (cardName) {
            case FORTIFY:
                // Card Number: 12, Cost: 4, Copies: 2
                // Effect: Increase the defense of all towers by 50% for this turn.
                sendGameEvent(String.format("%s[%s] increasing the defense of their Towers by %s for 1 turn with %s",
                        sp.getClientName(), sp.getClientId(), "50%", card.getName()));
                applyBuffDebuff(card.getCardNumber(), getAllOwnedTargets(x, y, 1, sp.getClientId()),
                        "Fortify failed to apply to any Towers");
                break;
            case RESOURCE_DENIAL:
                // Card Number: 13, Cost: 3, Copies: 2
                // Effect: Prevent allocation/deallocation of energy to all towers for a turn.
                sendGameEvent(
                        String.format("%s[%s] is blocking energy allocation to all enemy Towers for 1 turn with %s",
                                sp.getClientName(), sp.getClientId(), card.getName()));
                // top left and max dimension
                try {
                    applyBuffDebuff(card.getCardNumber(), getAllUnownedTargets(0, 0, grid.getRows(), sp.getClientId()),
                            "Resource Denial failed to apply to any Towers");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case EMP_BLAST:
                // Card Number: 19, Cost: 4, Copies: 2
                // Effect: Reduce the attack of all enemy towers by 50% (rounded down) for this
                // turn.
                sendGameEvent(
                        String.format("%s[%s] is reducing the attack of nearby enemy Towers by %s for 1 turn with %s",
                                sp.getClientName(), sp.getClientId(), "50%", card.getName()));
                applyBuffDebuff(card.getCardNumber(), getAllUnownedTargets(x, y, 1, sp.getClientId()),
                        "EMP Blast failed to affect any Towers");
                break;
            case ENHANCED_RANGE:
                // Card Number: 2, Cost: 2, Copies: 3
                // Effect: Increase the range of one tower by 1 tile for this turn.
                sendGameEvent(String.format("%s[%s] is increasing the range of Tower[%s] for 1 turn with %s",
                        sp.getClientName(), sp.getClientId(), tower.getId(), card.getName()));
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
            case DEFENSE_BOOST:
                // Card Number: 3, Cost: 3, Copies: 3
                // Effect: Increase a tower's defense by 50% for this turn.
                sendGameEvent(String.format("%s[%s] is increasing Tower[%s]'s defense by %s for 1 turn with %s",
                        sp.getClientName(), sp.getClientId(), tower.getId(), "50%", card.getName()));
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
            case POWER_STRIKE:
                // Card Number: 4, Cost: 3, Copies: 3
                // Effect: Increase a tower's attack by 50% for this turn.
                sendGameEvent(String.format("%s[%s] is increasing Tower[%s]'s attack by %s for 1 turn with %s",
                        sp.getClientName(), sp.getClientId(), tower.getId(), "50%", card.getName()));
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
            case SHIELD_GENERATOR:
                // Card Number: 8, Cost: 3, Copies: 2
                // Effect: Prevent one tower from being attacked for this turn.
                sendGameEvent(String.format("%s[%s] is blocking attacks to Tower[%s] for 1 turn with %s",
                        sp.getClientName(), sp.getClientId(), tower.getId(), card.getName()));
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
            case OVERCHARGE:
                // Card Number: 10, Cost: 4, Copies: 2
                // Effect: Double the attack power of one tower for its next attack this turn.
                sendGameEvent(String.format("%s[%s] is doubling Tower[%s]'s attack with for 1 turn %s",
                        sp.getClientName(), sp.getClientId(), tower.getId(), card.getName()));
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
            case ENERGY_SHIELD:
                // Card Number: 15, Cost: 3, Copies: 2
                // Effect: Absorb the next 3 damage to a tower.
                sendGameEvent(String.format("%s[%s] is protecting Tower[%s] for 1 turn with %s",
                        sp.getClientName(), sp.getClientId(), tower.getId(), card.getName()));
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
            case FORCEFIELD:
                // Card Number: 25, Cost: 4, Copies: 2
                // Effect: Prevent all damage to one tower for this turn.
                sendGameEvent(String.format("%s[%s] is protecting Tower[%s] for 1 turn with %s",
                        sp.getClientName(), sp.getClientId(), tower.getId(), card.getName()));
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
            default:
                // Handle other buffs and debuffs
                if (tower != null) {
                    BuffDebuff bd = Card.createBuffDebuff(card.getCardNumber());
                    tower.addBuffDebuff(bd);
                    sendTowerStatus(x, y, tower);
                }
                break;
        }
    }

    private void processInstantCard(ServerPlayer sp, Card card, int x, int y, Tower tower) {
        CardName cardName = card.getCardNameEnum();
        switch (cardName) {
            case RAPID_CONSTRUCTION:
                try {
                    placeTower(sp, x, y, 0);
                } catch (Exception e) {
                    sp.getServerThread().sendGameEvent("Card effect fizzed: " + e.getMessage());
                }
                break;
            case RESOURCE_BOOST:
                // Card Number: 1, Cost: 2, Copies: 3
                // Effect: Instantly gain 5 additional energy points.
                sendGameEvent(String.format("%s[%s] gained 5 energy with %s", sp.getClientName(), sp.getClientId(),
                        card.getName()));
                sp.incrementEnergy(5);
                sendPlayerCurrentEnergy(sp);
                break;
            case REPAIR:
                // Card Number: 5, Cost: 2, Copies: 3
                // Effect: Restore 3 health to a damaged tower.
                if (tower != null) {
                    sendGameEvent(String.format("%s[%s] repaired Tower[%s]'s health by 3 with %s",
                            sp.getClientName(), sp.getClientId(), tower.getId(),
                            card.getName()));
                    tower.setHealth(tower.getHealth() + 3);
                    sendTowerStatus(x, y, tower);
                }
                break;
            case SABOTAGE:
                // Card Number: 6, Cost: 3, Copies: 3
                // Effect: Reduce allocated energy of an opponent's tower by 50%.
                if (tower != null) {
                    try {
                        tower.allocateEnergy((int) (tower.getAllocatedEnergy() * .5));
                        sendGameEvent(String.format("%s[%s] reduced Tower[%s]'s allocated energy by %s with %s",
                                sp.getClientName(), sp.getClientId(), tower.getId(), "50%", card.getName()));
                    } catch (Exception e) {
                        sendGameEvent(String.format(
                                "%s[%s] failed to reduced Tower[%s]'s allocated energy by %s with %s due to %s",
                                sp.getClientName(), sp.getClientId(), tower.getId(), "50%", card.getName(),
                                e.getMessage()));
                    }
                    sendTowerStatus(x, y, tower);
                }
                break;
            case ENERGY_SURGE:
                // Card Number: 7, Cost: 2, Copies: 3
                // Effect: Gain 3 additional energy points for this turn.
                sendGameEvent(String.format("%s[%s] gained 3 energy with %s", sp.getClientName(), sp.getClientId(),
                        card.getName()));
                sp.incrementEnergy(3);
                sendPlayerCurrentEnergy(sp);
                break;
            case TELEPORT:
                // Card Number: 9, Cost: 3, Copies: 2
                // Effect: Move a tower to any unoccupied tile within range.
                // Implement teleport logic here
                break;
            case RECON_DRONE:
                // Card Number: 16, Cost: 3, Copies: 2
                // Effect: Reveal a random card in your opponent's hand.
                ServerPlayer reconTarget = getRandomPlayerWithHand(sp);
                Card reveal = reconTarget.getRandomCard();
                sendGameEvent(String.format("%s[%s] peeking at a random card in a random player's hand with %s",
                        sp.getClientName(), sp.getClientId(), card.getName()));
                sp.getServerThread()
                        .sendGameEvent(String.format("Card from %s[%s]'s hand: \n%s", reconTarget.getClientName(),
                                reconTarget.getClientId(), reveal));
                break;
            case SUPPLY_DROP:
                // Card Number: 17, Cost: 4, Copies: 2
                // Effect: Instantly gain 2 cards.
                sendGameEvent(String.format("%s[%s] drew 2 cards with %s", sp.getClientName(),
                        sp.getClientId(), card.getName()));
                drawCard();
                drawCard();
                break;
            case ARTILLERY_STRIKE:
                // Card Number: 18, Cost: 5, Copies: 2
                // Effect: Deal 2 damage to all enemy towers in range of one of your towers.
                sendGameEvent(String.format("%s[%s] is damaging nearby Towers with %s", sp.getClientName(),
                        sp.getClientId(), card.getName()));
                List<Cell> artillery = getAllUnownedTargets(x, y, 1, sp.getClientId());
                artillery.forEach(c -> {
                    Tower t = c.getTower();
                    int damage = t.takeDamage(2);
                    sendGameEvent(
                            String.format("Artillery Strike hit Tower[%s] for %s damage", t.getId(), damage));
                    sendTowerStatus(c.getX(), c.getY(), t);

                });
                if (artillery.size() == 0) {
                    sendGameEvent("Artillery Strike didn't hit any targets");
                }
                break;
            case COMMAND_CENTER:
                // Card Number: 20, Cost: 5, Copies: 2
                // Effect: Increase your maximum energy cap by 5.
                sendGameEvent(String.format("%s[%s] increased their energy cap by 5 with %s",
                        sp.getClientName(), sp.getClientId(), card.getName()));
                sp.setEnergyCap(sp.getEnergyCap() + 5);
                break;
            case TERRAFORMING:
                // Card Number: 21, Cost: 3, Copies: 2
                // Effect: Change the terrain type of one tile you control.
                // Implement terraforming logic here
                break;
            case COUNTERMEASURES:
                // Card Number: 22, Cost: 4, Copies: 2
                // Effect: Remove all buffs from a target tower.
                if (tower != null) {
                    sendGameEvent(String.format("%s[%s] is removing buffs from Tower[%s] with %s",
                            sp.getClientName(), sp.getClientId(), tower.getId(),
                            card.getName()));
                    tower.removeAllBuffs();
                    sendTowerStatus(x, y, tower);
                    sendGameEvent(String.format("Remove all buffs from Tower[%s]", tower.getId()));
                }
                break;
            case RESOURCE_THEFT:
                // Card Number: 23, Cost: 3, Copies: 2
                // Effect: Steal a random card from an opponent’s hand.
                sendGameEvent(String.format("%s[%s] is stealing a random card from a random Player with %s",
                        sp.getClientName(), sp.getClientId(), card.getName()));
                ServerPlayer resourceTarget = getRandomPlayerWithHand(sp);
                Card steal = resourceTarget.getRandomCard();
                resourceTarget.removeFromHand(steal);
                syncRemoveCard(resourceTarget, steal);
                sp.addToHand(steal);
                syncAddCard(sp, steal);
                sp.getServerThread()
                        .sendGameEvent(String.format("Card from %s[%s]'s hand: \n%s", resourceTarget.getClientName(),
                                resourceTarget.getClientId(), steal));
                break;
            case BACKUP_SYSTEMS:
                // Card Number: 24, Cost: 3, Copies: 2
                // Effect: Remove all debuffs from your towers.
                sendGameEvent(String.format("%s[%s] is cleansing their Towers with %s",
                        sp.getClientName(), sp.getClientId(), card.getName()));
                List<Cell> owned = getAllOwnedTargets(x, y, grid.getCols(), sp.getClientId());
                owned.forEach(c -> {
                    Tower t = c.getTower();
                    t.removeAllDebuffs();
                    sendTowerStatus(c.getX(), c.getY(), t);

                });
                if (owned.size() == 0) {
                    sendGameEvent("Backup Systems failed to find targets to repair");
                }
                break;
            default:
                // Handle all other cases
                break;
        }
    }

    // card helpers end

    // send/sync data to ServerPlayer(s)

    private void sendCurrentTurn(ServerPlayer sp) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendCurrentTurn(sp != null ? sp.getClientId() : Player.DEFAULT_CLIENT_ID);
            if (failedToSend) {
                removedClient(spInRoom.getServerThread());
            }
            return failedToSend;
        });
    }

    private void sendGameEvent(String str) {
        sendGameEvent(str, null);
    }

    private void sendGameEvent(String str, List<Long> targets) {
        playersInRoom.values().removeIf(spInRoom -> {
            boolean canSend = false;
            if (targets != null) {
                if (targets.contains(spInRoom.getClientId())) {
                    canSend = true;
                }
            } else {
                canSend = true;
            }
            if (canSend) {
                boolean failedToSend = !spInRoom.getServerThread().sendGameEvent(str);
                if (failedToSend) {
                    removedClient(spInRoom.getServerThread());
                }
                return failedToSend;
            }
            return false;
        });
    }

    private void sendPlayerCurrentEnergy(ServerPlayer sp) {
        playersInRoom.values().removeIf(spInRoom -> {
            Long clientId = sp == null ? ServerPlayer.DEFAULT_CLIENT_ID : sp.getClientId();
            int energy = sp == null ? 0 : sp.getEnergy();
            boolean failedToSend = !spInRoom.sendPlayerCurrentEnergy(clientId, energy);
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
        sp.sendGridDimensions(grid.getRows(), grid.getCols(), grid.getSeed());
    }

    private void sendGridDimensions() {
        playersInRoom.values().removeIf(spInRoom -> {

            boolean failedToSend = !spInRoom.sendGridDimensions(grid == null ? 0 : grid.getRows(),
                    grid == null ? 0 : grid.getCols(), grid == null ? 0 : grid.getSeed());
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
            checkCurrentPhase(st, Phase.TURN);
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
            checkCurrentPhase(st, Phase.TURN);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkFinishedTurn(sp);
            Tower playersTower = grid.getCell(x, y).getTower();
            if (playersTower == null) {
                st.sendGameEvent("Invalid coordinate for allocation");
                return;
            }
            if (playersTower.getClientId() != st.getClientId()) {
                st.sendGameEvent("You can only control your towers");
                return;
            }
            if (playersTower.didAllocate()) {
                st.sendGameEvent("You can only do one allocation event per Tower per turn");
                return;
            }
            int absEnergy = Math.abs(energy);
            LoggerUtil.INSTANCE.info(String.format("Player's current energy %s", sp.getEnergy()));
            if (energy > 0) {
                if (sp.decrementEnergy(absEnergy)) {

                    try {
                        playersTower.allocateEnergy(absEnergy); // adds

                    } catch (Exception e) {
                        sendGameEvent(String.format(
                                "%s[%s] failed to allocate energy to Tower[%s] due to %s",
                                sp.getClientName(), sp.getClientId(), playersTower.getId(), e.getMessage()));
                    }
                    LoggerUtil.INSTANCE
                            .info(String.format("Allocated %s energy to tower %s", absEnergy, playersTower.getId()));
                } else {
                    LoggerUtil.INSTANCE.info(
                            String.format("Failed to allocate %s energy to tower %s", absEnergy, playersTower.getId()));
                    st.sendGameEvent("You can't afford to allocate that much energy");
                    return;
                }
            } else {
                if (playersTower.getAllocatedEnergy() - absEnergy < 0) {
                    LoggerUtil.INSTANCE.info(String.format("Failed to deallocate %s energy from tower %s", absEnergy,
                            playersTower.getId()));
                    st.sendGameEvent("You can't deallocate more energy than the tower has");
                    return;
                }
                try {
                    playersTower.allocateEnergy(absEnergy); // removes

                } catch (Exception e) {
                    sendGameEvent(String.format(
                            "%s[%s] failed to deallocate energy to Tower[%s] due to %s",
                            sp.getClientName(), sp.getClientId(), playersTower.getId(), e.getMessage()));
                }
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
            checkCurrentPhase(st, Phase.TURN);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkFinishedTurn(sp);
            Tower playersTower = grid.getCell(x, y).getTower();
            if (playersTower == null) {
                st.sendGameEvent("Invalid coordinate for source of attack");
                return;
            }
            if (playersTower.getClientId() != st.getClientId()) {
                st.sendGameEvent("You can only control your towers");
                return;
            }
            if (targets == null || targets.isEmpty()) {
                st.sendGameEvent("You need to provide at least one target");
                return;
            }
            // TODO prevent the same tower from attacking twice
            if (playersTower.didAttack()) {
                st.sendGameEvent("You can only attack once per tower per turn");
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
                st.sendGameEvent("No valid targets in range");
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
                    sendGameEvent(String.format("Tower[%s] attacked Tower[%s] for %s damage %s",
                            playersTower.getId(),
                            defender.getId(), damage,
                            defeated ? ": Tower Defeated" : ""));
                    sendTowerStatus(x, y, playersTower);
                    sendTowerStatus(towersCell.getX(), towersCell.getY(), defender);
                    if (defeated) {
                        towersCell.removeTower();
                        ServerPlayer defenderPlayer = playersInRoom.get(defender.getClientId());
                        if (defenderPlayer.getTowersAlive() == 0) {
                            // TODO handle elimination
                            sendGameEvent(String.format("%s(%s) eliminated %s(%s)", sp.getClientName(),
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
            checkCurrentPhase(st, Phase.TURN);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            checkFinishedTurn(sp);
            if (grid.getCell(x, y).isOccupied()) {
                st.sendGameEvent("This cell is already occupied");
                return;
            }
            int tempCost = grid.getCell(x, y).getCost();
            placeTower(sp, x, y, tempCost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleDiscardCard(ServerThread st, Card card) {
        try {
            checkCurrentPhase(st, Phase.TURN);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);

            ServerPlayer sp = playersInRoom.get(st.getClientId());
            if (sp.removeFromHand(card) == null) {
                LoggerUtil.INSTANCE.severe("User doesn't have this card in hand: " + card);
                return;
            }
            syncRemoveCard(sp, card);
            sendGameEvent(String.format("%s discarded %s", st.getClientName(), card));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleUseCard(ServerThread st, int x, int y, Card _card) {
        try {
            checkCurrentPhase(st, Phase.TURN);
            checkPlayerInRoom(st);
            checkCurrentPlayer(st);
            ServerPlayer sp = playersInRoom.get(st.getClientId());
            Card card = sp.getHand().stream()
                    .filter(c -> c.getId() == _card.getId())
                    .findFirst()
                    .orElse(null);
            if (card == null) {
                st.sendGameEvent("Error using card");
                LoggerUtil.INSTANCE.severe("Error using card: " + _card);
                return;
            }
            checkCost(sp, card.getEnergy());

            // eager fetch of cell/tower
            Cell cell = null;
            Tower tower = null;
            try {
                cell = grid.getCell(x, y);
                if (cell != null) {
                    tower = cell.getTower();
                }
            } catch (Exception e) {
                LoggerUtil.INSTANCE.warning("Couldn't get Cell or Tower, probably invalid coordinates");
            }

            if (card.getType() == CardType.BUFF || card.getType() == CardType.DEBUFF) {
                processBuffDebuff(sp, card, x, y, tower);
            } else {
                processInstantCard(sp, card, x, y, tower);
            }

            if (sp.removeFromHand(card) == null) {
                LoggerUtil.INSTANCE.severe("User doesn't have this card in hand: " + card);
                return;
            }
            sp.decrementEnergy(card.getEnergy());
            sendPlayerCurrentEnergy(sp);
            syncRemoveCard(sp, card);
            sendGameEvent(String.format("%s used %s", st.getClientName(), card));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // end receive data from ServerThread (GameRoom specific)
}
