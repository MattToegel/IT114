package HNS.common;

public class GameOptionsPayload extends Payload {
    private GameOptions options;

    public GameOptionsPayload() {
        setPayloadType(PayloadType.GAME_OPTIONS);
    }

    public GameOptions getOptions() {
        return options;
    }

    public void setOptions(GameOptions options) {
        this.options = options;
    }
}
