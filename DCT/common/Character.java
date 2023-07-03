package DCT.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Character implements Serializable{
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
     * Hidden internal value to give a chance for a positive scenario in certain cases
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

    public float getLifePercent(){
        return ((float)currentLife)/((float)maxLife) * 100;
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

    //transient ignores field during serialization
    private transient  Player controller = null;
    
    public Player getController() {
        return controller;
    }

    public void setController(Player controller) {
        this.controller = controller;
    }
    private Cell currentCell = null;

    public void setCurrentCell(Cell c) {
        currentCell = c;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    public boolean isInCell(){
        return currentCell != null;
    }
}
