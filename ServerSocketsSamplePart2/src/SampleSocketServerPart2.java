import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SampleSocketServerPart2 {
	int port = 3002;
	private void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);
				Socket client = serverSocket.accept();
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));) {
			
			System.out.println("Client connected, waiting for message");
			String fromClient = "";
			String toClient = "";
			while ((fromClient = in.readLine()) != null) {
				if ("kill server".equalsIgnoreCase(fromClient)) {
					System.out.println("Client killed server");
					break;
				}
				else {
					System.out.println("From client: " + fromClient);
					//added sending reply to client from server
					toClient = "Echo: " + fromClient;
					out.println(toClient);
					//end sending reply to client from server
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.println("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	

	public static void main(String[] args) {
		System.out.println("Starting Server");
		SampleSocketServerPart2 server = new SampleSocketServerPart2();
		server.start(3002);
		System.out.println("Server Stopped");
	}
}