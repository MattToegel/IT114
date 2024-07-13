package Project.Common;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Represents a Tower that can be placed on a Cell.
 * Towers have attributes like attack power, defense, range, and health.
 * Also includes energy allocation for attack and defense.
 */
public class Tower implements Serializable {
    private static long nextTowerId = 1;

    private static synchronized long getNextTowerId() {
        return nextTowerId++;
    }

    private long id;
    private long clientId; // Reference to the owner
    private int attack;
    private int defense;
    private int range;
    private int health;
    private int allocatedEnergy;
    private static final Random random = new Random();
    private boolean attacked = false;
    private boolean allocated = false;

    public void setDidAllocate(boolean alloc) {
        allocated = alloc;
    }

    public boolean didAllocate() {
        return allocated;
    }

    public void setDidAttack(boolean attacked) {
        this.attacked = attacked;
    }

    public boolean didAttack() {
        return attacked;
    }

    /**
     * Constructs a Tower with default attributes, generating a new ID.
     *
     * @param clientId the ID of the player who owns this tower.
     */
    public Tower(long clientId) {
        this.id = getNextTowerId();
        this.clientId = clientId;
        this.attack = 1;
        this.defense = 1;
        this.range = 1;
        this.health = 5;
        this.allocatedEnergy = 0;
    }

    /**
     * Constructs a Tower with default attributes, using the specified tower ID.
     *
     * @param clientId the ID of the player who owns this tower.
     * @param towerId  the ID of the tower being set.
     */
    public Tower(long clientId, long towerId) {
        this.id = towerId;
        this.clientId = clientId;
        this.attack = 1;
        this.defense = 1;
        this.range = 1;
        this.health = 5;
        this.allocatedEnergy = 0;
    }

    /**
     * Constructs a Tower with specified attributes.
     *
     * @param clientId the ID of the player who owns this tower.
     * @param towerId  the ID of the tower being set.
     * @param attack   the attack power of the tower.
     * @param defense  the defense power of the tower.
     * @param range    the range of the tower.
     * @param health   the health of the tower.
     */
    public Tower(long clientId, long towerId, int attack, int defense, int range, int health, int allocatedEnergy) {
        this.id = towerId;
        this.clientId = clientId;
        this.attack = attack;
        this.defense = defense;
        this.range = range;
        this.health = health;
        this.allocatedEnergy = allocatedEnergy;
    }

    public void refresh() {
        attacked = false;
        allocated = false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getAllocatedEnergy() {
        return allocatedEnergy;
    }

    public void allocateEnergy(int energy) {
        this.allocatedEnergy += energy;
    }

    public void clearAllocatedEnergy() {
        this.allocatedEnergy = 0;
    }

    /**
     * Applies damage to the tower.
     *
     * @param damage the amount of damage to apply.
     */
    public void takeDamage(int damage) {
        this.health = Math.max(this.health - damage, 0); // Ensure health doesn't go below 0
    }

    /**
     * Calculates the damage to each defender based on the attacker's attack
     * and energy modifier against the defenders' defense and energy modifiers,
     * and applies the damage. It uses a callback to inform which tower took
     * what damage. Dice rolls are included in the calculation.
     * 
     * @param attacker       the attacking tower.
     * @param defenders      the list of defending towers.
     * @param damageCallback the callback to inform which tower took what damage.
     */
    public static void calculateDamage(Tower attacker, List<Tower> defenders,
            BiConsumer<Tower, Integer> damageCallback) {
        int numDefenders = defenders.size();
        // int splitAttack = attacker.attack / numDefenders;
        int modAttack = attacker.attack + attacker.allocatedEnergy;
        for (Tower defender : defenders) {
            int attackDieRoll = random.nextInt(6) + 1;
            int defenderDieRoll = random.nextInt(6) + 1;
            int effectiveAttack = Math.max(0, (modAttack + attackDieRoll) / numDefenders);
            // splitAttack + attacker.allocatedEnergy + random.nextInt(6) + 1; // Dice roll
            // 1-6
            int effectiveDefense = Math.max(0, defender.defense + defender.allocatedEnergy + defenderDieRoll);
            int damage = effectiveAttack - effectiveDefense;

            if (damage > 0) {
                defender.takeDamage(damage);
                damageCallback.accept(defender, damage);
            } else {
                damageCallback.accept(defender, 0); // Indicate a miss
            }
        }
    }

    /**
     * Returns a string representation of the tower.
     *
     * @return a string representation of the tower.
     */
    @Override
    public String toString() {
        return "Tower{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", attack=" + attack +
                ", defense=" + defense +
                ", range=" + range +
                ", health=" + health +
                ", allocatedEnergy=" + allocatedEnergy +
                '}';
    }
}
