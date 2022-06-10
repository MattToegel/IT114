package Module4.Part1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    int port = 3001;

    private void start(int port) {
        this.port = port;
        System.out.println("Waiting for client connection");
        // server listening
        try (ServerSocket serverSocket = new ServerSocket(port);
                // client wait
                Socket client = serverSocket.accept(); // blocking;
                // send to client
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                // read from client
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));) {

            System.out.println("Client connected, waiting for message");
            String fromClient = "";
            // String toClient = "";
            while ((fromClient = in.readLine()) != null) {
                if ("kill server".equalsIgnoreCase(fromClient)) {
                    System.out.println("Client killed server");
                    break;
                } else {
                    System.out.println("From client: " + fromClient);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("closing server socket");
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Server");
        Server server = new Server();
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}