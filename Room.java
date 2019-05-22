import java.util.ArrayList;

public class Room extends Thread {	
	private final ArrayList<ClientThread> threads;
	private Caller caller;
	private boolean active = false;
	private boolean oneLineWon = false;
	private boolean twoLinesWon = false;
	private boolean fullHouseWon = false;
	int index;
	
	public Room(ClientThread player, int index) {
		caller = new Caller();
		this.index = index + 1;
		threads = new ArrayList<ClientThread>();
		addPlayer(player);
	}
	
	public void addPlayer(ClientThread player) {
		threads.add(player);	
	}
	
	public void run() {
		while(true) {
			if(threads.size() < 2) {
				if(threads.get(0).getClientName() != null) {
					threads.get(0).outputStream.println("MORE_PLAYERS");
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				int number = caller.chooseNumber();
				while(!active) {
					for(int i = 0; i < threads.size(); i++) {
						if(threads.get(i).getClientName() == null) {
							active = false;
						} else {
							active = true;
						}
					}
				}
				for(int i = 0; i < threads.size(); i++) {
					threads.get(i).outputStream.println("CALLER " + Integer.toString(number));
					if(!oneLineWon) {
						boolean won = threads.get(i).getOneLineWon();
						if(won) {
							for(int j = 0; j < threads.size(); j++) {
								threads.get(j).outputStream.println("MESSAGE " + "***" + threads.get(i).getUserName() + " has won one line!***");
							}
							oneLineWon = true;
						}
					} 
					if(!twoLinesWon) {
						boolean won = threads.get(i).getTwoLinesWon();
						if(won) {
							for(int j = 0; j < threads.size(); j++) {
								threads.get(j).outputStream.println("MESSAGE " + "***" + threads.get(i).getUserName() + " has won two lines!***");
							}
							twoLinesWon = true;
						}
					}
					if(!fullHouseWon) {
						boolean won = threads.get(i).getFullHouseWon();
						if(won) {
							for(int j = 0; j < threads.size(); j++) {
								threads.get(j).outputStream.println("MESSAGE " + "***" + threads.get(i).getUserName() + " has won a full house!***");
							}
							fullHouseWon = true;
						}
					}
					if(fullHouseWon) {
						for(int j = 0; j < threads.size(); j++) {
							threads.get(j).outputStream.println("MESSAGE " + "***" + " Game will now end!***");
						}
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						for(int j = 0; j < threads.size(); j++) {
							threads.get(j).outputStream.println("GAME_OVER ");
						}
					}
				}
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
