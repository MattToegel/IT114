package server;

import common.Player;

public class ClientPlayer {
	public ClientPlayer(ServerThread client, Player player) {
		this.client = client;
		this.player = player;
	}

	public ServerThread client;
	public Player player;
}