package DCT.common;

public class Player {

    private boolean isReady = false;

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public boolean isReady() {
        return this.isReady;
    }

    private Character character;

    /**
     * Assigns a bi-directional relationship between Character and Player so if we
     * have one we can find the other
     * 
     * @param character
     */
    public void assignCharacter(Character character) {
        if (this.character != null) {
            this.character.setController(null);
            this.character = null;
        }
        this.character = character;
        if (this.character != null) {
            this.character.setController(this);
        }
    }

    public Character getCharacter() {
        return character;
    }

    public boolean hasCharacter() {
        return character != null;
    }
}
