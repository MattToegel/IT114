package DCT.common;

public class PositionPayload extends Payload {
    private int x, y;

    public PositionPayload(PayloadType pt) {
        setPayloadType(pt);
    }

    public PositionPayload() {
        // default to hide
        this(PayloadType.MOVE);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setCoord(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
