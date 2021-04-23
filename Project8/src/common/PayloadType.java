package common;

public enum PayloadType {
	CONNECT, DISCONNECT, MESSAGE, CLEAR_PLAYERS, SYNC_DIRECTION, SYNC_POSITION, CREATE_ROOM, JOIN_ROOM, GET_ROOMS,
	SYNC_GAME_SIZE, PLACE_SHIP, ATTACK, SHIP_STATUS, ATTACK_RADIUS, CAN_ATTACK, SHIP_COUNT, PHASE_CHANGE
}