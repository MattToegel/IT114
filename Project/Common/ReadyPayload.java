package Project.Common;

public class ReadyPayload extends Payload {
    private boolean isReady;
    
    public ReadyPayload(){
        setPayloadType(PayloadType.READY);
    }
    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }
    @Override
    public String toString(){
        return super.toString() + String.format(" isReady [%s]", isReady?"ready":"not ready");
    }
}
