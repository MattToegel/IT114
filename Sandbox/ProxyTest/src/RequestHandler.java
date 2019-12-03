/*

-----------------------------------------------------------------------------------------------
STOLEN FROM THIS MAN ON GITHUB
https://github.com/stefano-lupo/Java-Proxy-Server/blob/master/src/RequestHandler.java

-----------------------------------------------------------------------------------------------
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RequestHandler implements Runnable {

	/**
	 * Socket connected to client passed by Proxy server
	 */
	Socket clientSocket;

	/**
	 * Read data client sends to proxy
	 */
	BufferedReader proxyToClientBr;

	/**
	 * Send data from proxy to client
	 */
	BufferedWriter proxyToClientBw;

	/**
	 * Creates a ReuqestHandler object capable of servicing HTTP(S) GET requests
	 * @param clientSocket socket connected to the client
	 */
	public RequestHandler(Socket clientSocket){
		this.clientSocket = clientSocket;
		try{
			//this could close the conneciton early
			this.clientSocket.setSoTimeout(2000);
			proxyToClientBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			proxyToClientBw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Reads and examines the requestString and calls the appropriate method based
	 * on the request type.
	 */
	@Override
	public void run() {

		// Get Request from client
		String requestString;
		try{
			requestString = proxyToClientBr.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error reading request from client");
			return;
		}


		System.out.println("Request Received " + requestString);
		sendPageToClient(requestString);
	}


	/*
	 * Handles HTTPS requests between client and remote server
	 * @param urlString desired file to be transmitted over https
	 */
	private void sendPageToClient(String urlString){
		try{
			// Open a socket to the remote server
			Socket proxyToServerSocket = new Socket("192.168.0.175", 8080);
			//this could close the connection early
			proxyToServerSocket.setSoTimeout(5000);

			//Create a Buffered Writer betwen proxy and remote
			BufferedWriter proxyToServerBW = new BufferedWriter(new OutputStreamWriter(proxyToServerSocket.getOutputStream()));

			// Create Buffered Reader from proxy and remote
			BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerSocket.getInputStream()));

			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

			Thread inputThread = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						//this is to clientSocket, probably not to a server
						System.out.println("sending to server: " + urlString );
						out.println(urlString);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			inputThread.start();//start the thread




			// Listen to remote server and relay to client
			try {
				byte[] buffer = new byte[4096];
				int read;
				do {
					read = proxyToServerSocket.getInputStream().read(buffer);
					if (read > 0) {
						//already have out on line 92
						
						clientSocket.getOutputStream().write(buffer, 0, read);
						if (proxyToServerSocket.getInputStream().available() < 1) {
							clientSocket.getOutputStream().flush();
						}
					}
				} while (read >= 0);
			}
			catch (SocketTimeoutException e) {

			}
			catch (IOException e) {
				e.printStackTrace();
			}


			// Close Down Resources
			if(proxyToServerSocket != null){
				proxyToServerSocket.close();
			}

			if(proxyToServerBR != null){
				proxyToServerBR.close();
			}

			if(proxyToServerBW != null){
				proxyToServerBW.close();
			}

			if(proxyToClientBw != null){
				proxyToClientBw.close();
			}


		} catch (SocketTimeoutException e) {
			String line = "HTTP/1.0 504 Timeout Occured after 10s\n" +
					"User-Agent: ProxyServer/1.0\n" +
					"\r\n";
			try{
				proxyToClientBw.write(line);
				proxyToClientBw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		catch (Exception e){
			System.out.println("Error on HTTPS : " + urlString );
			e.printStackTrace();
		}
	}
}