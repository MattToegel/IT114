package Project.Common;

public class PositionPayload extends Payload {
    private int x, y;

    public PositionPayload(int x, int y) {
        this.x = x;
        this.y = y;
        setPayloadType(PayloadType.POSITION);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
