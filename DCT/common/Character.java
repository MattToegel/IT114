package DCT.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

import DCT.client.ClientPlayer;
import DCT.server.ServerPlayer;

public class Character implements Serializable, Cloneable {
    public enum CharacterType {
        TANK, DAMAGE, SUPPORT
    }

    /**
     * Their primary action form (determines range)
     */
    public enum ActionType {
        MELEE, MISSILE, GLOBAL
    }

    private String name;
    /**
     * Special title the character can gain
     */
    private String title;
    /**
     * Attribute that determines physical damage
     */
    private int attack;
    /**
     * Attribute that determines life value
     */
    private int vitality;
    /**
     * Attribute that reduces incoming damage
     */
    private int defense;
    /**
     * Attribute that determines non-physical damage/output
     */
    private int will;

    private int currentLife;
    private int maxLife;

    /**
     * Hidden internal value that determines how quickly the character improves
     */
    private int progressionRate;
    /**
     * Hidden internal value to give a chance for a positive scenario in certain
     * cases
     */
    private int luck;

    public int range = 1;
    private int level = 1;

    private String code = "";

    private long experience = 0;
    private CharacterType type;
    private ActionType actionType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CharacterType getType() {
        return type;
    }

    public void setType(CharacterType type) {
        this.type = type;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExperience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getVitality() {
        return vitality;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getCurrentLife() {
        return currentLife;
    }

    public void setCurrentLife(int currentLife) {
        this.currentLife = currentLife;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    public int getProgressionRate() {
        return progressionRate;
    }

    public void setProgressionRate(int progressionRate) {
        this.progressionRate = progressionRate;
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getWill() {
        return will;
    }

    public void setWill(int will) {
        this.will = will;
    }

    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        }
    }

    public static Character deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis)) {
            return (Character) in.readObject();
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    // game stuff

    // transient ignores field during serialization
    private transient Player controller = null;
    private transient Cell currentCell = null;
    private Aggro aggroManager = new Aggro();

    private boolean didAttack;
    private boolean didMove;
    private boolean didHeal;

    public float getLifePercent() {
        return ((float) currentLife) / ((float) maxLife) * 100;
    }

    public boolean isAlive() {
        return currentLife > 0;
    }

    public void fullHeal() {
        if (maxLife <= 0) {
            maxLife = vitality; // TODO add formula
        }
        currentLife = maxLife;
    }

    public void startTurn() {
        didAttack = false;
        didMove = false;
        if (type == CharacterType.SUPPORT) {
            didHeal = false;
        } else {
            didHeal = true; // prevent non-support types from healing
        }
    }

    public void usedAttack() {
        didAttack = true;
    }

    public void usedMove() {
        didMove = true;
    }

    public void usedHeal() {
        didHeal = true;
    }

    public boolean didAttack() {
        return didAttack;
    }

    public boolean didMove() {
        return didMove;
    }

    public boolean didHeal() {
        return didHeal;
    }

    public boolean actionsExhausted() {
        return didAttack && didHeal && didMove;
    }

    public boolean hasTarget() {
        return aggroManager.getTargetCharacter() != null && aggroManager.getTargetCharacter().isAlive();
    }

    public Character getTarget() {
        return aggroManager.getTargetCharacter();
    }

    public boolean currentTargetWithinRange() {
        if (getTarget() == null || getTarget().getCurrentCell() == null) {
            return false;
        }
        return withinRange(this, getTarget());
    }

    public static boolean withinRange(Character source, Character otherTarget) {
        return getDistanceBetween(source, otherTarget) <= source.getRange();
    }

    public static int getDistanceBetween(Character source, Character otherTarget) {
        if (otherTarget == null || otherTarget.getCurrentCell() == null) {
            return Integer.MAX_VALUE;
        }
        if (source == null || source.getCurrentCell() == null) {
            return Integer.MAX_VALUE;
        }
        Point tp = new Point(otherTarget.getCurrentCell().getX(), otherTarget.getCurrentCell().getY());
        Point mp = new Point(source.getCurrentCell().getX(), source.getCurrentCell().getY());
        int dist = GridHelpers.getManhattanDistance(tp, mp);
        return dist;
    }

    public long attackCurrentTarget() {
        if (getTarget() == null) {
            return -1;
        }
        return getTarget().takeDamage(this);
    }

    public long takeDamage(Character source) {
        Random rand = new Random();
        // TODO add better formula, this is an unbalanced proof of concept
        long attack = rand.nextInt(source.getAttack()) + 1;
        long defense = rand.nextInt(this.getDefense()) + 1;

        long result = defense - attack;
        if (result <= 0) {
            result = 1;// always take at least 1 damage
        }
        currentLife -= result;
        if (getClientId() < 1) {
            aggroManager.updateAggro(source, result);
        }
        return result;
    }

    public long receiveHeal(Character source) {
        long will = source.getWill();
        // TODO add better formula
        Random rand = new Random();
        currentLife += rand.nextLong(will) + 1;
        if (currentLife > maxLife) {
            currentLife = maxLife;
        }
        return will;
    }

    public void setTarget(Character target) {
        aggroManager.updateAggro(target, 1);
    }

    public void setController(Player controller) {
        this.controller = controller;
    }

    public long getClientId() {
        if (controller instanceof AIPlayer) {
            return ((AIPlayer) controller).getClientId();
        } else if (controller instanceof ServerPlayer) {
            return ((ServerPlayer) controller).getClient().getClientId();
        } else if (controller instanceof ClientPlayer) {
            return ((ClientPlayer) controller).getClientId();
        }
        return Constants.DEFAULT_CLIENT_ID;
    }

    public String getClientName() {
        if (controller instanceof AIPlayer) {
            return "Server";
        } else if (controller instanceof ServerPlayer) {
            return ((ServerPlayer) controller).getClient().getClientName();
        } else if (controller instanceof ClientPlayer) {
            return ((ClientPlayer) controller).getClientName();
        }
        return "";
    }

    public void setCurrentCell(Cell c) {
        currentCell = c;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    public boolean isInCell() {
        return currentCell != null;
    }

    @Override
    public Character clone() {
        // Important: This is a slow and memory intensive way to clone an object since
        // it's a deep clone
        // it's more efficient to manually map fields to a copy.
        try {
            // The serialize method is called to convert the current object into a byte
            // array.
            // This is a process of converting the state of an object into a byte stream.
            byte[] serialized = this.serialize();

            // The byte array is then passed to the deserialize method to create a new
            // object.
            // Deserialization is the reverse process of serialization where we convert the
            // byte stream back to a copy of the object.
            Character copy = Character.deserialize(serialized);

            // The new object, which is a copy of the current object, is returned.
            return copy;
        } catch (IOException | ClassNotFoundException e) {
            // If something goes wrong with the serialization or deserialization process,
            // a RuntimeException is thrown with a custom message and the original
            // exception.
            throw new RuntimeException("Failed to clone Character object", e);
        }
    }
}
