package DCT.client;

public interface Event {
    void onClientConnect(String clientName, String message);

    void onClientDisconnect(String clientName, String message);

    void onMessageReceive(String clientName, String message);

    void onChangeRoom();

    void onGetRoom(String roomName);

	void onIsMuted(String clientName, boolean isMuted);

}