import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BingoServer {
	
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	
	private static final int maxClientsCount = 10;
	private static final ClientThread[] threads = new ClientThread[maxClientsCount];
	private static Room[] rooms = new Room[maxClientsCount / 2];
	
	public static void main (String [] args) {
		int portNumber = 2222;
		if(args.length < 1) {
			System.out.println("Usage: java MultithreadServer <portNumber>\n" + "Now using port number = " + portNumber);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
		}
		
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for(i = 0; i < maxClientsCount; i++) {
					if(threads[i] == null) {
						(threads[i] = new ClientThread(clientSocket, threads)).start();
						System.out.println("New connection: " + clientSocket);
						break;
					}
				}
				
				if(i % 2 == 0) {
					Room room = new Room(threads[i], i / 2);
					rooms[i] = room;
					room.start();
				}
				
				if(i % 2 == 1) {
					rooms[i - 1].addPlayer(threads[i]);
				}
				
				if(i == maxClientsCount) {
					PrintStream outputStream = new PrintStream(clientSocket.getOutputStream());
					outputStream.println("TOO_BUSY");
					outputStream.close();
					clientSocket.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
