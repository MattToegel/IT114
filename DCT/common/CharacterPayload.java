package DCT.common;

import DCT.common.Character.CharacterType;

public class CharacterPayload extends Payload{
    private CharacterType characterType;
    private Character character;
    public CharacterPayload(){
        setPayloadType(PayloadType.CHARACTER);
    }
    public CharacterType getCharacterType() {
        return characterType;
    }
    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }
    public Character getCharacter() {
        return character;
    }
    public void setCharacter(Character character) {
        this.character = character;
    }
}
