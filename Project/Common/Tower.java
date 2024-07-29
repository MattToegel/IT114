package Project.Common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import Project.Common.Cell.Terrain;
import Project.Common.Cell.TerrainBonusType;

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
    private static final int MAX_HEALTH = 5;
    private int allocatedEnergy;
    private static final Random random = new Random();
    private boolean attacked = false;
    private boolean allocated = false;
    private Map<Long, BuffDebuff> activeBuffsDebuffs = new HashMap<>();

    private static final List<BuffDebuff.EffectType> ATTACK_EFFECTS = Arrays.asList(
            BuffDebuff.EffectType.POWER_STRIKE,
            BuffDebuff.EffectType.OVERCHARGE);

    private static final List<BuffDebuff.EffectType> DEFENSE_EFFECTS = Arrays.asList(
            BuffDebuff.EffectType.DEFENSE_BOOST,
            BuffDebuff.EffectType.FORTIFY,
            BuffDebuff.EffectType.EMP_BLAST);

    private static final List<BuffDebuff.EffectType> RANGE_EFFECTS = Collections.singletonList(
            BuffDebuff.EffectType.ENHANCED_RANGE);

    private static final List<BuffDebuff.EffectType> FORCEFIELD_EFFECTS = Collections.singletonList(
            BuffDebuff.EffectType.FORCEFIELD);

    /**
     * Transient indicates this field should not be serialized
     */
    private transient Cell cell;

    protected void setCell(Cell cell) {
        this.cell = cell;
    }

    /**
     * Returns a reference of the Cell the Tower is sitting on
     * 
     * @return
     */
    public Cell getCell() {
        return cell;
    }

    public boolean isOnEnergyCell() {
        return cell != null && cell.getTerrainType() == Terrain.ENERGY;
    }

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

    public void refresh() {
        attacked = false;
        allocated = false;
        Iterator<Map.Entry<Long, BuffDebuff>> iterator = activeBuffsDebuffs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, BuffDebuff> entry = iterator.next();
            BuffDebuff buffDebuff = entry.getValue();
            buffDebuff.reduceDuration();
            if (buffDebuff.getDuration() <= 0) {
                LoggerUtil.INSTANCE.fine("BuffDebuff Expired: " + buffDebuff);
                iterator.remove();
            }
        }
        // healing terrain (allows overheal if not at or exceeding max health)
        if (health < MAX_HEALTH) {
            // cell properties
            if (cell != null && cell.getTerrainType() == Terrain.HEALING) {
                if (cell.getTerrainBonusType() == TerrainBonusType.FLAT) {
                    health += cell.getTerrainBonus();
                } else {
                    health += health * (1 + cell.getTerrainBonus());
                }
            }
        }
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public int getAttack() {
        float calcAttack = attack*1.0f;
        // buff/debuff
        double percentModifier = activeBuffsDebuffs.values().stream()
                .filter(buff -> ATTACK_EFFECTS.contains(buff.getEffectType()))
                .mapToDouble(BuffDebuff::getModifier)
                .sum();

        // cell properties
        if (cell != null && cell.getTerrainType() == Terrain.ATTACK) {
            if (cell.getTerrainBonusType() == TerrainBonusType.FLAT) {
                calcAttack += cell.getTerrainBonus();
            } else {
                calcAttack += Math.ceil(calcAttack * (1 + cell.getTerrainBonus()));
            }
        }
        calcAttack *= Math.ceil(1 + percentModifier);// buff/debuff bonus
        return (int) Math.round(calcAttack);
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        float calcDefense = defense*1.0f;
        // buff/debuff
        double percentModifier = activeBuffsDebuffs.values().stream()
                .filter(buff -> DEFENSE_EFFECTS.contains(buff.getEffectType()))
                .mapToDouble(BuffDebuff::getModifier)
                .sum();

        // cell properties
        if (cell != null && cell.getTerrainType() == Terrain.DEFENSE) {
            if (cell.getTerrainBonusType() == TerrainBonusType.FLAT) {
                calcDefense += cell.getTerrainBonus();
            } else {
                calcDefense += Math.ceil(calcDefense * (1 + cell.getTerrainBonus()));
            }
        }
        calcDefense *= Math.ceil(1 + percentModifier);// buff/debuff bonus

        return (int) Math.round(calcDefense);
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getRange() {
        float calcRange = range*1.0f;
        // buff/debuff
        double percentModifier = activeBuffsDebuffs.values().stream()
                .filter(buff -> RANGE_EFFECTS.contains(buff.getEffectType()))
                .mapToDouble(BuffDebuff::getModifier)
                .sum();

        // cell properties
        if (cell != null && cell.getTerrainType() == Terrain.RANGE) {
            if (cell.getTerrainBonusType() == TerrainBonusType.FLAT) {
                calcRange += cell.getTerrainBonus();
            } else {
                calcRange += Math.ceil(calcRange * (1 + cell.getTerrainBonus()));
            }
        }
        calcRange *= Math.ceil(1 + percentModifier);// buff/debuff bonus

        return (int) Math.round(calcRange);
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

    public void allocateEnergy(int energy) throws Exception {
        if (!isAllocationBlocked()) {
            this.allocatedEnergy += energy;
        } else {
            throw new Exception("Allocation blocked by Resource Denial");
        }
    }

    public void clearAllocatedEnergy() {
        this.allocatedEnergy = 0;
    }

    public int takeDamage(int damage) {
        if (!isForcefieldActive()) {
            if (isEnergyShieldActive()) {
                damage = Math.max(damage - 3, 0);
            }
            this.health = Math.max(this.health - damage, 0);
            return damage;
        }
        return 0;
    }

    private boolean isAllocationBlocked() {
        return activeBuffsDebuffs.values().stream()
                .anyMatch(buff -> BuffDebuff.EffectType.RESOURCE_DENIAL == buff.getEffectType());
    }

    /**
     * Card data: 15,Energy Shield,Absorb the next 3 damage to a tower.,2,3,Buff
     * 
     * @return
     */
    private boolean isEnergyShieldActive() {
        return activeBuffsDebuffs.values().stream()
                .anyMatch(buff -> BuffDebuff.EffectType.ENERGY_SHIELD == buff.getEffectType());
    }

    /**
     * Card data: 25,Forcefield,Prevent all damage to one tower for this
     * turn.,2,4,Buff
     * 
     * @return
     */
    private boolean isForcefieldActive() {
        return activeBuffsDebuffs.values().stream()
                .anyMatch(buff -> FORCEFIELD_EFFECTS.contains(buff.getEffectType()));
    }

    public void addBuffDebuff(BuffDebuff buffDebuff) {
        LoggerUtil.INSTANCE.fine(String.format("Tower[%s] receiving buff/debuff %s", getId(), buffDebuff));
        activeBuffsDebuffs.put(buffDebuff.getId(), buffDebuff);
    }

    public void removeAllBuffs() {
        LoggerUtil.INSTANCE.fine(String.format("Tower[%s] removed all buffs", getId()));
        activeBuffsDebuffs.values().removeIf(buff -> buff.getEffectType() != BuffDebuff.EffectType.EMP_BLAST);
    }

    public void removeAllDebuffs() {
        LoggerUtil.INSTANCE.fine(String.format("Tower[%s] removed all debuffs", getId()));
        activeBuffsDebuffs.values().removeIf(buff -> buff.getEffectType() == BuffDebuff.EffectType.EMP_BLAST);
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
        int modAttack = attacker.getAttack() + attacker.getAllocatedEnergy();
        for (Tower defender : defenders) {
            int attackDieRoll = random.nextInt(6) + 1;
            int defenderDieRoll = random.nextInt(6) + 1;
            int effectiveAttack = Math.max(0, (modAttack + attackDieRoll) / numDefenders);
            int effectiveDefense = Math.max(0, defender.getDefense() + defender.getAllocatedEnergy() + defenderDieRoll);
            int damage = effectiveAttack - effectiveDefense;

            if (damage > 0) {
                // possibility of being blocked/reduced so we'll return the value
                damage = defender.takeDamage(damage);
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
                ", attack=" + getAttack() +
                ", defense=" + getDefense() +
                ", range=" + getRange() +
                ", health=" + getHealth() +
                ", allocatedEnergy=" + getAllocatedEnergy() +
                '}';
    }
}
