//package com.mt.examples.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SampleSocketClient {
	Socket server;
	public SampleSocketClient() {
	}
	public void connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("Client connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void start() throws IOException {
		if(server == null) {
			return;
		}
		System.out.println("Client Started");
		try(Scanner si = new Scanner(System.in);
				PrintWriter out = new PrintWriter(server.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));){
			//Thread to listen for keyboard input so main thread isn't blocked
			Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							System.out.println("Waiting for input");
							String line = si.nextLine();
							if(!"quit".equalsIgnoreCase(line) && line != null) {
								out.println(line);
							}
							else {
								System.out.println("Stopping input thread");
								break;
							}
						}
					}
					catch(Exception e) {
						System.out.println("Client shutdown");
					}
					finally {
						close();
					}
				}
			};
			inputThread.start();//start the thread
			
			//Thread to listen for responses from server so it doesn't block main thread
			Thread fromServerThread = new Thread() {
				@Override
				public void run() {
					try {
						String fromServer = "";
						while(!server.isClosed() && (fromServer = in.readLine()) != null) {
							System.out.println("Replay from server: " + fromServer);
						}
						System.out.println("Stopping server listen thread");
					}
					catch (Exception e) {
						if(!server.isClosed()) {
							e.printStackTrace();
							System.out.println("Server closed connection");
						}
						else {
							System.out.println("Connection closed");
						}
					}
					finally {
						close();
					}
				}
			};
			fromServerThread.start();//start the thread
			
			//Keep main thread alive until the socket is closed
			while(!server.isClosed()) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);//force close
			//TODO implement cleaner closure when server stops before client
			//currently hangs/waits on the console/scanner input
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	private void close() {
		if(server != null && !server.isClosed()) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		SampleSocketClient client = new SampleSocketClient();
		int port = -1;
		try{
			//not safe but try-catch will get it
			port = Integer.parseInt(args[0]);
		}
		catch(Exception e){
			System.out.println("Invalid port");
		}
		if(port == -1){
			return;
		}
		client.connect("127.0.0.1", port);
		try {
			//if start is private, it's valid here since this main is part of the class
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
