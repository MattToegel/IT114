package Project.Common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Card implements Serializable {
    private int id;
    private String name;
    private String description;
    private int energy;
    private CardType type;
    private int cardNumber;

    public enum CardType {
        BUFF, DEBUFF, INSTANT
    }

    public enum CardName {
        RESOURCE_BOOST,
        ENHANCED_RANGE,
        DEFENSE_BOOST,
        POWER_STRIKE,
        REPAIR,
        SABOTAGE,
        ENERGY_SURGE,
        SHIELD_GENERATOR,
        TELEPORT,
        OVERCHARGE,
        ENERGY_DRAIN,
        FORTIFY,
        RESOURCE_DENIAL,
        RAPID_CONSTRUCTION,
        ENERGY_SHIELD,
        RECON_DRONE,
        SUPPLY_DROP,
        ARTILLERY_STRIKE,
        EMP_BLAST,
        COMMAND_CENTER,
        TERRAFORMING,
        COUNTERMEASURES,
        RESOURCE_THEFT,
        BACKUP_SYSTEMS,
        FORCEFIELD
    }

    public Card(int id, String name, String description, int energy, CardType type, int cardNumber) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.energy = energy;
        this.type = type;
        this.cardNumber = cardNumber;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getEnergy() {
        return energy;
    }

    public CardType getType() {
        return type;
    }

    public int getCardNumber() {
        return cardNumber;
    }

    @Override
    public String toString() {
        return cardNumber + ") " + name + " (" + description + ") - Energy: " + energy + " - Type: " + type;
    }

    public static Card copy(Card card) {
        try {
            // Serialize to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(card);
            out.flush();
            byte[] bytes = bos.toByteArray();

            // Deserialize from byte array
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(bis);
            return (Card) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public CardName getCardNameEnum(){
        CardName cardName = CardName.values()[getCardNumber() - 1]; // Assuming cardNumber starts from 1
        return cardName;
    }
    // Factory method using card number
    public static BuffDebuff createBuffDebuff(int cardNumber) {
        CardName cardName = CardName.values()[cardNumber - 1]; // Assuming cardNumber starts from 1
        return createBuffDebuff(cardName);
    }

    // Factory method using CardName
    public static BuffDebuff createBuffDebuff(CardName cardName) {
        switch (cardName) {
            case ENHANCED_RANGE:
                return new BuffDebuff(BuffDebuff.EffectType.ENHANCED_RANGE, 1.0);
            case DEFENSE_BOOST:
                return new BuffDebuff(BuffDebuff.EffectType.DEFENSE_BOOST, 0.5);
            case POWER_STRIKE:
                return new BuffDebuff(BuffDebuff.EffectType.POWER_STRIKE, 0.5);
            case SHIELD_GENERATOR:
                return new BuffDebuff(BuffDebuff.EffectType.SHIELD_GENERATOR, 1.0);
            case OVERCHARGE:
                return new BuffDebuff(BuffDebuff.EffectType.OVERCHARGE, 2.0);
            case FORTIFY:
                return new BuffDebuff(BuffDebuff.EffectType.FORTIFY, 0.5);
            case RESOURCE_DENIAL:
                return new BuffDebuff(BuffDebuff.EffectType.RESOURCE_DENIAL, 1.0);
            case ENERGY_SHIELD:
                return new BuffDebuff(BuffDebuff.EffectType.ENERGY_SHIELD, 3.0);
            case EMP_BLAST:
                return new BuffDebuff(BuffDebuff.EffectType.EMP_BLAST, 0.5);
            case FORCEFIELD:
                return new BuffDebuff(BuffDebuff.EffectType.FORCEFIELD, 1.0);
            default:
                throw new IllegalArgumentException("Unknown card name: " + cardName);
        }
    }

    public boolean requiresTarget() {
        switch (this.getCardNameEnum()) {
            case ENHANCED_RANGE:
            case DEFENSE_BOOST:
            case POWER_STRIKE:
            case REPAIR:
            case SABOTAGE:
            case SHIELD_GENERATOR:
            case OVERCHARGE:
            case RAPID_CONSTRUCTION:
            case ENERGY_SHIELD:
            case FORCEFIELD:
            case ARTILLERY_STRIKE:
            
                return true;
            default:
                return false;
        }
    }
}
