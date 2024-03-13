package Project.Common;

public class ConnectionPayload extends Payload {

    public ConnectionPayload() {
        setPayloadType(PayloadType.CLIENT_ID);
    }

    public ConnectionPayload(boolean isConnected) {
        setPayloadType(isConnected ? PayloadType.CONNECT : PayloadType.DISCONNECT);
    }

    /**
     * Who the payload is from
     */
    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return super.toString() + ", Client name " + getClientName();
    }
}
