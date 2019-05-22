import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ClientThread extends Thread {
	private String clientName = null;
	public BufferedReader inputStream = null;
	public PrintStream outputStream;
	private Socket clientSocket = null;
	private final ClientThread[] threads;
	private int maxClientsCount;
	private BingoCard bingoCard;
	
	private boolean fullHouseWon = false;
	private boolean twoLinesWon = false;
	private boolean oneLineWon = false;
	
	private String name;
	
	public ClientThread(Socket clientSocket, ClientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
		try {
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outputStream = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
	
	public String getClientName() {
		return clientName;
	}
	
	public String getUserName() {
		return name;
	}
	
	public BufferedReader getInputStream() {
		return inputStream;
	}
	
	public BingoCard getBingoCard() {
		return bingoCard;
	}
	
	public boolean getOneLineWon() {
		return oneLineWon;
	}
	
	public boolean getTwoLinesWon() {
		return twoLinesWon;
	}
	
	public boolean getFullHouseWon() {
		return fullHouseWon;
	}
	
	public void run() {
		int maxClientsCount = this.maxClientsCount;
		ClientThread[] threads = this.threads;
		
		try {
			outputStream.println("CREATE_LOGIN_UI");
			while(true) {
				name = inputStream.readLine().trim();
				if(name.indexOf('@') == -1) {
					break;
				} else {
					outputStream.println("CREATE_ERROR_UI You cant use @ symbol in names.");
				}
			}
			outputStream.println("CREATE_GAME_UI");
			
			synchronized(this) {
				for(int i = 0; i < maxClientsCount; i++) {
					if(threads[i] != null && threads[i] == this) {
						clientName = "@" + name;
						break;
					}
				}
				for(int i = 0; i < maxClientsCount; i++) {
					if(threads[i] != null && threads[i] != this) {
						threads[i].outputStream.println("NEW_USER " + name);
					}
				}
			}
			
			while(true) {
				String line = inputStream.readLine();
				String tokens[] = line.split(" "); 
				String outputMessage = "<" + name + ">";
				if(line.startsWith("QUIT")) {
					break;
				}
				
				if(line.startsWith("TOO_BUSY")) {
					System.out.println("Server too busy, try again later.");
				}
				
				if(tokens[0].equals("MESSAGE")) {
					for(int i = 1; i < tokens.length; i++) {
						outputMessage += tokens[i];
						if(i != tokens.length - 1) {
							outputMessage += " ";
						}
					}
					synchronized(this) {
						for(int j = 0; j < maxClientsCount; j++) {
							if(threads[j] != null && threads[j].clientName != null) {
								threads[j].outputStream.println("MESSAGE " + outputMessage);
							}
						}
					}
				}
				
				if(tokens[0].equals("ONE_LINE_WON")) {
					oneLineWon = true;
				}
				if(tokens[0].equals("TWO_LINES_WON")) {
					twoLinesWon = true;
				}
				if(tokens[0].equals("FULL_HOUSE_WON")) {
					fullHouseWon = true;
				}
			}
			
			synchronized(this) {
				for(int i = 0; i < maxClientsCount; i++) {
					if(threads[i] != null && threads[i] != this) {
						threads[i].outputStream.println("USER_QUIT " + "*** "+ name + " is leaving the bingo room! ***");
					}
				}
			}
			
			outputStream.println("BYE");
			synchronized(this) {
				for(int i = 0; i < maxClientsCount; i++) {
					if(threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			
			inputStream.close();
			outputStream.close();
			clientSocket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
