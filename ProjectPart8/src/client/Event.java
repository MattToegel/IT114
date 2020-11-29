package client;

import java.awt.Point;

public interface Event {
    void onClientConnect(String clientName, String message);

    void onClientDisconnect(String clientName, String message);

    void onMessageReceive(String clientName, String message);

    void onChangeRoom();

    void onSyncDirection(String clientName, Point direction);

    void onSyncPosition(String clientName, Point position);

    void onGetRoom(String roomName);

    void onResize(Point p);

    void onGetChair(String chairName, Point position, Point dimension, String sitter);

    void onResetChairs();

    void onGetTicket(String ticketName, Point position, Point dimension, String holder);// boolean isAvailable);

    void onResetTickets();

    void onSetCountdown(String message, int duration);

    void onToggleLock(boolean isLocked);

    void onUpdateTicketCollector(int chairIndex);

    void onPlayerKicked(String clientName);
}
