package Project.Common;

public class TurnStatusPayload extends Payload {
    private boolean didTakeTurn;
    private int roll;

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public TurnStatusPayload() {
        setPayloadType(PayloadType.TURN);
    }

    public boolean didTakeTurn() {
        return didTakeTurn;
    }

    public void setDidTakeTurn(boolean didTakeTurn) {
        this.didTakeTurn = didTakeTurn;
    }

}
