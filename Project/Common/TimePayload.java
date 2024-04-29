package Project.Common;

public class TimePayload extends Payload {
    private int time;

    public TimePayload() {
        setPayloadType(PayloadType.TIME);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
