package client;

import java.awt.Point;

import common.GameState;

public interface Event {
	void onClientConnect(String clientName, String message);

	void onClientDisconnect(String clientName, String message);

	void onMessageReceive(String clientName, String message);

	void onChangeRoom();

	void onSyncDirection(String clientName, Point direction);

	void onSyncPosition(String clientName, Point position);

	void onGetRoom(String roomName);

	void onResize(Point p);

	void onShipPlaced(int shipType, int shipId, int x, int y, int life);

	void onAttackStatus(int markerType, int x, int y);
	
	void onShipStatus(int shipId, int life);
	
	void onAttackRadius(int x, int y, int radius);
	
	void onCanAttack(String client, int attacks);
	
	void onShipCount(String client, int ships);
	
	void onPhaseChange(GameState phase);
}