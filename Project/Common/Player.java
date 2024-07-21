package Project.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import Project.Common.Cell.Terrain;
import Project.Common.Cell.TerrainBonusType;

/**
 * Common Player data shared between Client and Server
 */
public class Player {
    public static long DEFAULT_CLIENT_ID = -1L;
    private long clientId = Player.DEFAULT_CLIENT_ID;
    private boolean isReady = false;
    private boolean takeTurn = false;

    private List<Card> hand = new ArrayList<>();
    private ConcurrentHashMap<Long, Tower> towers = new ConcurrentHashMap<>();
    private int energy = 0;
    private int energyCap = 20;

    public long getClientId() {
        return clientId;
    }

    public boolean didTakeTurn() {
        return takeTurn;
    }

    public void setTakeTurn(boolean tookTurn) {
        this.takeTurn = tookTurn;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void addToHand(Card card) {
        hand.add(card);
    }

    public void addToHand(List<Card> cards) {
        hand.addAll(cards);
    }

    public Card removeFromHand(Card card) {
        // Important: Since Card is being passed over the socket as Payload data
        // It likely won't be the exact object that's in the Player's hand
        // so hand.remove(card) may not always work.
        // The below logic uses Card.id which is unique so it can find the proper match
        // then that reference will be removed from the hand
        return hand.stream()
                .filter(c -> c.getId() == card.getId())
                .findFirst()
                .map(c -> {
                    hand.remove(c);
                    return c;
                })
                .orElse(null);
    }

    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    public Card getRandomCard() {
        List<Card> cards = getHand();
        return cards.get(new Random().nextInt(cards.size()));
    }

    public void setHand(List<Card> cards) {
        if (cards == null) {
            hand.clear();
        } else {
            hand = cards;
        }
    }

    /**
     * Resets all of the data (this is destructive).
     * You may want to make a softer reset for other data
     */
    public void reset() {
        this.clientId = Player.DEFAULT_CLIENT_ID;
        this.isReady = false;
        this.hand.clear();
        this.towers.clear();
        this.energy = 0;
    }

    /**
     * Sets a tower in the player's list of towers.
     * If the tower doesn't exist, it gets added.
     * If the tower exists, its data is updated.
     *
     * @param tower the tower to be added or updated.
     */
    public void setTower(Tower tower) {
        towers.compute(tower.getId(), (id, existingTower) -> {
            if (existingTower == null) {
                return tower;
            } else {
                existingTower.setAttack(tower.getAttack());
                existingTower.setDefense(tower.getDefense());
                existingTower.setRange(tower.getRange());
                existingTower.setHealth(tower.getHealth());
                existingTower.clearAllocatedEnergy();
                try {
                    existingTower.allocateEnergy(tower.getAllocatedEnergy());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return existingTower;
            }
        });
    }

    /**
     * Removes a tower from the player's list of towers.
     *
     * @param tower the tower to be removed.
     */
    public void removeTower(Tower tower) {
        towers.remove(tower.getId());
    }

    /**
     * Gets the total number of towers.
     *
     * @return the total number of towers.
     */
    public int getTotalTowers() {
        return towers.size();
    }

    /**
     * Gets the number of towers that are alive.
     * For this example, let's assume towers with health > 0 are alive.
     *
     * @return the number of towers that are alive.
     */
    public int getTowersAlive() {
        return (int) towers.values().stream().filter(t -> t.getHealth() > 0).count();
    }

    /**
     * Gets the number of towers that are destroyed.
     * For this example, let's assume towers with health <= 0 are destroyed.
     *
     * @return the number of towers that are destroyed.
     */
    public int getTowersDestroyed() {
        return (int) towers.values().stream().filter(t -> t.getHealth() <= 0).count();
    }

    /**
     * Gets the total allocated energy across all towers owned by this player.
     *
     * @return the total allocated energy.
     */
    public int getTotalAllocatedEnergy() {
        return towers.values().stream()
                .mapToInt(Tower::getAllocatedEnergy)
                .sum();
    }

    public int getTotalBonusCards(){
        return towers.values().stream()
                .filter(t->t.getCell().getTerrainType() == Terrain.CARDS)
                .map(Tower::getCell)
                .filter(c -> c.getTerrainBonusType() == TerrainBonusType.FLAT)
                .mapToInt(c -> (int) c.getTerrainBonus())
                .sum();
    }

    /**
     * Returns the total bonus energy acquired per round for occupying Cells with an
     * Energy bonus
     * 
     * @return
     */
    public int getTotalBonusEnergy() {
        // Note: For sake of ease, just assuming Energy bonuses will be FLAT instead of
        // PERCENT
        return towers.values().stream()
                .filter(Tower::isOnEnergyCell)
                .map(Tower::getCell)
                .filter(c -> c.getTerrainBonusType() == TerrainBonusType.FLAT)
                .mapToInt(c -> (int) c.getTerrainBonus())
                .sum();
    }

    public void refreshTowers() {
        towers.values().forEach(t -> t.refresh());
    }

    /**
     * Clears the player's list of towers.
     */
    public void clearTowers() {
        towers.clear();
    }

    /**
     * Increments the player's energy by the specified amount.
     * Conditionally caps the energy at the defined energy cap.
     *
     * @param amount the amount to increment.
     * @param cap    true caps it, false doesn't
     */
    public void incrementEnergy(int amount, boolean cap) {
        if (cap) {
            this.energy = Math.min(this.energy + amount, this.energyCap);
        } else {
            this.energy += amount;
        }
    }

    /**
     * Increments the player's energy by the specified amount.
     * Uncapped.
     *
     * @param amount the amount to increment.
     */
    public void incrementEnergy(int amount) {
        incrementEnergy(amount, false);
    }

    /**
     * Decrements the player's energy by the specified amount.
     * Returns false if the decrement would result in negative energy.
     *
     * @param amount the amount to decrement.
     * @return true if the energy was decremented, false if it would result in
     *         negative energy.
     */
    public boolean decrementEnergy(int amount) {
        if (this.energy - amount < 0) {
            return false;
        }
        this.energy -= amount;
        return true;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    /**
     * Gets the player's current energy.
     *
     * @return the current energy.
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Sets the player's energy cap.
     *
     * @param energyCap the new energy cap.
     */
    public void setEnergyCap(int energyCap) {
        this.energyCap = energyCap;
    }

    /**
     * Gets the player's energy cap.
     *
     * @return the energy cap.
     */
    public int getEnergyCap() {
        return energyCap;
    }
}
