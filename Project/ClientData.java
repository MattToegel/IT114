package Project;

public class ClientData {
    public static long DEFAULT_CLIENT_ID = -1L;
    private long clientId = ClientData.DEFAULT_CLIENT_ID;
    private String clientName;
    public long getClientId() {
        return clientId;
    }
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }
    public String getClientName() {
        return clientName;
    }
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void reset(){
        this.clientId = ClientData.DEFAULT_CLIENT_ID;
        this.clientName = "";
    }
}
