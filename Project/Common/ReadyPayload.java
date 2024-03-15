package Project.Common;

public class ReadyPayload extends Payload {

    private boolean isReady;

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public ReadyPayload() {
        setPayloadType(PayloadType.READY);
    }
}
