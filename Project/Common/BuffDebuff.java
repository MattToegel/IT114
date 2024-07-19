package Project.Common;

import java.io.Serializable;

/**
 * Represents a Buff or Debuff that can be applied to a Tower.
 */
public class BuffDebuff implements Serializable {
    private static long nextBuffDebuffId = 1;

    private static synchronized long getNextBuffDebuffId() {
        return nextBuffDebuffId++;
    }

    public enum EffectType {
        ENHANCED_RANGE,       // Increase range percentage
        DEFENSE_BOOST,        // Increase defense percentage
        POWER_STRIKE,         // Increase attack percentage
        SHIELD_GENERATOR,     // Prevent attack
        OVERCHARGE,           // Double attack power
        FORTIFY,              // Increase defense of all towers
        RESOURCE_DENIAL,      // Prevent energy allocation
        ENERGY_SHIELD,        // Absorb the next 3 damage
        EMP_BLAST,            // Reduce attack of all enemy towers
        FORCEFIELD            // Prevent all damage
    }

    private long id;
    private String name;
    private int duration;
    private EffectType effectType;
    private double modifier;

    public BuffDebuff(EffectType effectType, double modifier) {
        // temporary "fix" to prevent losing benefits early
        // is super imbalanced because some effects get a 2 turn duration, this is a TODO fix item
        this(effectType, modifier, 2);
    }

    public BuffDebuff(EffectType effectType, double modifier, int duration) {
        this.id = getNextBuffDebuffId();
        this.name = effectType.name().replace("_", " ");
        this.duration = duration;
        this.effectType = effectType;
        this.modifier = modifier;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public void reduceDuration() {
        this.duration--;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public double getModifier() {
        return modifier;
    }

    @Override
    public String toString() {
        return "BuffDebuff{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", duration=" + duration +
                ", effectType=" + effectType +
                ", modifier=" + modifier +
                '}';
    }
}
