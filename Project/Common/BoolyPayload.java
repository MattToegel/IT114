package Project.Common;

public class BoolyPayload extends Payload {
    private boolean isAway;

    public BoolyPayload() {
        setPayloadType(PayloadType.AWAY);
    }

    public boolean isAway() {
        return isAway;
    }

    public void setAway(boolean isAway) {
        this.isAway = isAway;
    }
}
