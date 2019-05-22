import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultCaret;

public class BingoClient implements Runnable {
	private static Socket clientSocket = null;
	private static PrintStream outputStream = null;
	private static BufferedReader inputStream = null;
	
	private static BufferedReader inputLine = null;
	private static boolean closed = false;
	
	private BingoCard bingoCard;
	private String name;
	
	private JFrame frame = new JFrame();
	private JTextArea messageArea;
	private JLabel callerNumber;
	
	private String[][] ticketOneString;
	private String[][] ticketTwoString;
	private String[][] ticketThreeString; 
	private String[][] ticketFourString;
	private String[][] ticketFiveString; 
	private String[][] ticketSixString;
	
	private ArrayList<String[][]> tickets = new ArrayList<String[][]>();
	
	private JTable ticketOne;
	private JTable ticketTwo;
	private JTable ticketThree;
	private JTable ticketFour;
	private JTable ticketFive;
	private JTable ticketSix;
	
	private ArrayList<String> calledNumbers = new ArrayList<String>();
	private ArrayList<String> checkedNumbers = new ArrayList<String>();
	
	public static void main (String [] args) {
		int portNumber = 2222;
		String host = "localhost";
		
		if(args.length > 0) {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}
		
		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			outputStream = new PrintStream(clientSocket.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch(IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host " + host);
		}
		
		if(clientSocket != null && outputStream != null && inputStream != null) {
			try {
				new Thread(new BingoClient()).start();
				while(!closed) {
					outputStream.println(inputLine.readLine().trim());
				}
				outputStream.close();
				inputStream.close();
				clientSocket.close();
			} catch(IOException e) {
				System.err.println("IOException: " + e);
			}
		}
	}
	
	public void run() {
		String responseLine;
		try {
			while((responseLine = inputStream.readLine()) != null) {
				String[] tokens = responseLine.split(" ");
				if(tokens[0].equals("CREATE_ERROR_UI")) {
					String msg = "";
					for(int i = 0; i < tokens.length; i++) {
						msg += tokens[i];
						if(i != (tokens.length - 1)) {
							msg += " ";
						}
					}
					createErrorUI(msg);
				} else if(tokens[0].equals("CREATE_LOGIN_UI")) {
					createLoginUI();
				} else if(tokens[0].equals("CREATE_GAME_UI")) {
					bingoCard = new BingoCard();
					createGameUI(bingoCard.toString());
				} else if(tokens[0].equals("CALLER")) {
					updateCaller(tokens[1]);
					calledNumbers.add(tokens[1]);
					for(int i = 0; i < tickets.size(); i++) {
						if(checkFullHouseWinner(tickets.get(i))) {
							outputStream.println("FULL_HOUSE_WON ");
						}
						if(checkTwoLinesWinner(tickets.get(i))) {
							outputStream.println("TWO_LINES_WON ");
						}
						if(checkOneLineWinner(tickets.get(i))) {
							outputStream.println("ONE_LINE_WON ");
						}
					}
				} else if(tokens[0].equals("MESSAGE")) {
					String messageContent = "";
					for(int i = 1; i < tokens.length; i++) {
						messageContent += tokens[i];
						if(i != tokens.length - 1) {
							messageContent += " ";
						}
					}
					updateMessageArea(messageContent);
				} else if(tokens[0].equals("NEW_USER")) {
					updateMessageArea("*** A new user " + tokens[1] + " has enetered the room! ***");
				} else if(tokens[0].equals("MORE_PLAYERS")) {
					updateMessageArea("Waiting for more players...");
				} else if(tokens[0].equals("USER_QUIT")) {
					String messageContent = "";
					for(int i = 1; i < tokens.length; i++) {
						messageContent += tokens[i];
						if(i != tokens.length - 1) {
							messageContent += " ";
						}
					}
					updateMessageArea(messageContent);
				} else if(tokens[0].equals("GAME_OVER")) {
					outputStream.println("QUIT ");
				} else if(tokens[0].equals("BYE")) {
					frame.dispose();
					System.exit(0);
				}
			}
			closed = true;
		} catch(IOException e) {
			System.err.println("IOException: " + e);
		}
	}

	private void createErrorUI(String error) {
		JOptionPane.showMessageDialog(null, error);
	}

	private void createLoginUI() {
		name = JOptionPane.showInputDialog("Please eneter your name:");
		outputStream.println(name);
	}
	
	public void checkNumber(String number) {
		ArrayList<String[][]> card = new ArrayList<String[][]>();
		card.add(ticketOneString);
		card.add(ticketTwoString);
		card.add(ticketThreeString);
		card.add(ticketFourString);
		card.add(ticketFiveString);
		card.add(ticketSixString);
		for(int i = 0; i < card.size(); i++) {
			for(int j = 0; j < card.get(i).length; j++) {
				for(int k = 0; k < card.get(i)[j].length; k++) {
					if(card.get(i)[j][k].equals(number)) {
						card.get(i)[j][k] = "X";
					}
				}
			}
		}
		
		ArrayList<JTable> tables = new ArrayList<JTable>();
		tables.add(ticketOne);
		tables.add(ticketTwo);
		tables.add(ticketThree);
		tables.add(ticketFour);
		tables.add(ticketFive);
		tables.add(ticketSix);
		
		for(int i = 0; i < tables.size(); i++) {
			((AbstractTableModel)tables.get(i).getModel()).fireTableDataChanged();
		}
	}
	
	public boolean checkOneLineWinner(String[][] ticketString) {
		boolean oneLineWinner = true;
		
		ArrayList<String> lineOne = new ArrayList<String>();
		ArrayList<String> lineTwo = new ArrayList<String>();
		ArrayList<String> lineThree = new ArrayList<String>();
		
		for(int i = 0; i < ticketString.length; i++) {
			for(int j = 0; j < ticketString[i].length; j++) {
				if(ticketString[i][j] != "") {
					if(i == 0) {
						lineOne.add(ticketString[i][j]);
					} else if(i == 1) {
						lineTwo.add(ticketString[i][j]);
					} else if(i == 2) {
						lineThree.add(ticketString[i][j]);
					}
				}
			}
		}
		
		for(int i = 0; i < lineOne.size(); i++) {
			if(!checkedNumbers.contains(lineOne.get(i))) {
				oneLineWinner = false;
			}
		}
		
		if(oneLineWinner == false) {
			oneLineWinner = true;
			for(int i = 0; i < lineTwo.size(); i++) {
				if(!checkedNumbers.contains(lineTwo.get(i))) {
					oneLineWinner = false;
				}
			}
		}
		
		if(oneLineWinner == false) {
			oneLineWinner = true;
			for(int i = 0; i < lineThree.size(); i++) {
				if(!checkedNumbers.contains(lineThree.get(i))) {
					oneLineWinner = false;
				}
			}
		}
		
		
		
		return oneLineWinner;
	}
	
	public boolean checkTwoLinesWinner(String[][] ticketString) {
		boolean twoLinesWinner = false;
		
		boolean firstLineFull = true;
		boolean secondLineFull = true;
		boolean thirdLineFull = true;
		
		ArrayList<String> lineOne = new ArrayList<String>();
		ArrayList<String> lineTwo = new ArrayList<String>();
		ArrayList<String> lineThree = new ArrayList<String>();
		
		for(int i = 0; i < ticketString.length; i++) {
			for(int j = 0; j < ticketString[i].length; j++) {
				if(ticketString[i][j] != "") {
					if(i == 0) {
						lineOne.add(ticketString[i][j]);
					} else if(i == 1) {
						lineTwo.add(ticketString[i][j]);
					} else if(i == 2) {
						lineThree.add(ticketString[i][j]);
					}
				}
			}
		}
		
		for(int i = 0; i < lineOne.size(); i++) {
			if(!checkedNumbers.contains(lineOne.get(i))) {
				firstLineFull = false;
			}
		}
		
		for(int i = 0; i < lineTwo.size(); i++) {
			if(!checkedNumbers.contains(lineTwo.get(i))) {
				secondLineFull = false;
			}
		}
		
		for(int i = 0; i < lineThree.size(); i++) {
			if(!checkedNumbers.contains(lineThree.get(i))) {
				thirdLineFull = false;
			}
		}
		
		if(firstLineFull && secondLineFull) {
			twoLinesWinner = true;
		} else if(firstLineFull && thirdLineFull) {
			twoLinesWinner = true;
		} else if(secondLineFull && thirdLineFull) {
			twoLinesWinner = true;
		}
		
		return twoLinesWinner;
	}
	
	public boolean checkFullHouseWinner(String[][] ticketString) {
		boolean fullHouseWinner = false;
		
		boolean firstLineFull = true;
		boolean secondLineFull = true;
		boolean thirdLineFull = true;
		
		ArrayList<String> lineOne = new ArrayList<String>();
		ArrayList<String> lineTwo = new ArrayList<String>();
		ArrayList<String> lineThree = new ArrayList<String>();
		
		for(int i = 0; i < ticketString.length; i++) {
			for(int j = 0; j < ticketString[i].length; j++) {
				if(ticketString[i][j] != "") {
					if(i == 0) {
						lineOne.add(ticketString[i][j]);
					} else if(i == 1) {
						lineTwo.add(ticketString[i][j]);
					} else if(i == 2) {
						lineThree.add(ticketString[i][j]);
					}
				}
			}
		}
		
		for(int i = 0; i < lineOne.size(); i++) {
			if(!checkedNumbers.contains(lineOne.get(i))) {
				firstLineFull = false;
			}
		}
		
		for(int i = 0; i < lineTwo.size(); i++) {
			if(!checkedNumbers.contains(lineTwo.get(i))) {
				secondLineFull = false;
			}
		}
		
		for(int i = 0; i < lineThree.size(); i++) {
			if(!checkedNumbers.contains(lineThree.get(i))) {
				thirdLineFull = false;
			}
		}
		
		if(firstLineFull && secondLineFull && thirdLineFull) {
			fullHouseWinner = true;
		}
		
		return fullHouseWinner;
	}
	
	private void createGameUI(String bingoCard) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				outputStream.println("QUIT");
			}
		});
		frame.setTitle("Bingo90");
		frame.setResizable(false);
		
		JPanel panel = new JPanel(new GridBagLayout());
		frame.getContentPane().add(panel);
		
		JPanel caller = new JPanel();
		caller.setPreferredSize(new Dimension(300, 50));
		caller.setBorder(BorderFactory.createBevelBorder(EtchedBorder.RAISED, Color.GRAY, Color.GRAY));
		caller.setBackground(Color.white);
		
		JPanel messages = new JPanel();
		messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
		messages.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 2), "Chat Room", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, (new Font("Serif", Font.BOLD, 21))));
		
		JPanel cardArea = new JPanel();
		cardArea.setLayout(new BoxLayout(cardArea, BoxLayout.Y_AXIS));
		cardArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 2), "Bingo Card", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, (new Font("Serif", Font.BOLD, 21))));
		
		JLabel callerArea = new JLabel("Caller: ");
		callerArea.setFont(new Font("Serif", Font.BOLD, 21));
		callerArea.setHorizontalAlignment(SwingConstants.LEFT);
		
		callerNumber = new JLabel();
		callerNumber.setFont(new Font("Serif", Font.BOLD, 21));	
		callerNumber.setHorizontalAlignment(SwingConstants.LEFT);
		
		ticketOneString = getTicketString(1);
		ticketTwoString = getTicketString(2);
		ticketThreeString = getTicketString(3);
		ticketFourString = getTicketString(4);
		ticketFiveString = getTicketString(5);
		ticketSixString = getTicketString(6);
		
		tickets.add(ticketOneString);
		tickets.add(ticketTwoString);
		tickets.add(ticketThreeString);
		tickets.add(ticketFourString);
		tickets.add(ticketFiveString);
		tickets.add(ticketSixString);

		String[] columnNames = { "col1", "col2", "col3", "col4", "col5", "col6", "col7", "col8", "col9"};
		
		BingoTableModel ticketOneModel = new BingoTableModel(ticketOneString, columnNames);
		ticketOne = new BingoTicket(ticketOneString, columnNames, ticketOneModel, Color.red) {
			public Component prepareRenderer(
			        TableCellRenderer renderer, int row, int column)
			    {
			        Component c = super.prepareRenderer(renderer, row, column);
			        String number = (String)ticketOne.getValueAt(row, column);
			        if(checkedNumbers.contains(number)) {
			        	c.setBackground(Color.BLACK);
			        	c.setForeground(Color.WHITE);
			        } else {
			        	c.setBackground(Color.WHITE);
			        	c.setForeground(Color.BLACK);
			        }

			        return c;
			    }
		};
		ticketOne.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JTable table = (JTable) event.getSource();
				Point point = event.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				String value = (String) table.getValueAt(row, column);
				if(calledNumbers.contains(value)) {
					if(!checkedNumbers.contains(value)) {
						checkedNumbers.add(value);
					}
				}
			}
		});
		
		BingoTableModel ticketTwoModel = new BingoTableModel(ticketTwoString, columnNames);
		ticketTwo = new BingoTicket(ticketTwoString, columnNames, ticketTwoModel, Color.blue) {
			public Component prepareRenderer(
			        TableCellRenderer renderer, int row, int column)
			    {
			        Component c = super.prepareRenderer(renderer, row, column);
			        String number = (String)ticketTwo.getValueAt(row, column);
			        if(checkedNumbers.contains(number)) {
			        	c.setBackground(Color.BLACK);
			        	c.setForeground(Color.WHITE);
			        } else {
			        	c.setBackground(Color.WHITE);
			        	c.setForeground(Color.BLACK);
			        }

			        return c;
			    }
		};
		ticketTwo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JTable table = (JTable) event.getSource();
				Point point = event.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				String value = (String) table.getValueAt(row, column);
				if(calledNumbers.contains(value)) {
					if(!checkedNumbers.contains(value)) {
						checkedNumbers.add(value);
					}
				}
			}
		});
		
		BingoTableModel ticketThreeModel = new BingoTableModel(ticketThreeString, columnNames);
		ticketThree = new BingoTicket(ticketThreeString, columnNames, ticketThreeModel, Color.yellow) {
			public Component prepareRenderer(
			        TableCellRenderer renderer, int row, int column)
			    {
			        Component c = super.prepareRenderer(renderer, row, column);
			        String number = (String)ticketThree.getValueAt(row, column);
			        if(checkedNumbers.contains(number)) {
			        	c.setBackground(Color.BLACK);
			        	c.setForeground(Color.WHITE);
			        } else {
			        	c.setBackground(Color.WHITE);
			        	c.setForeground(Color.BLACK);
			        }

			        return c;
			    }
		};
		ticketThree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JTable table = (JTable) event.getSource();
				Point point = event.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				String value = (String) table.getValueAt(row, column);
				if(calledNumbers.contains(value)) {
					if(!checkedNumbers.contains(value)) {
						checkedNumbers.add(value);
					}
				}
			}
		});
		
		BingoTableModel ticketFourModel = new BingoTableModel(ticketFourString, columnNames);
		ticketFour = new BingoTicket(ticketFourString, columnNames, ticketFourModel, Color.red) {
			public Component prepareRenderer(
			        TableCellRenderer renderer, int row, int column)
			    {
			        Component c = super.prepareRenderer(renderer, row, column);
			        String number = (String)ticketFour.getValueAt(row, column);
			        if(checkedNumbers.contains(number)) {
			        	c.setBackground(Color.BLACK);
			        	c.setForeground(Color.WHITE);
			        } else {
			        	c.setBackground(Color.WHITE);
			        	c.setForeground(Color.BLACK);
			        }

			        return c;
			    }
		};
		ticketFour.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JTable table = (JTable) event.getSource();
				Point point = event.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				String value = (String) table.getValueAt(row, column);
				if(calledNumbers.contains(value)) {
					if(!checkedNumbers.contains(value)) {
						checkedNumbers.add(value);
					}
				}
			}
		});
		
		BingoTableModel ticketFiveModel = new BingoTableModel(ticketFiveString, columnNames);
		ticketFive = new BingoTicket(ticketFiveString, columnNames, ticketFiveModel, Color.blue) {
			public Component prepareRenderer(
			        TableCellRenderer renderer, int row, int column)
			    {
			        Component c = super.prepareRenderer(renderer, row, column);
			        String number = (String)ticketFive.getValueAt(row, column);
			        if(checkedNumbers.contains(number)) {
			        	c.setBackground(Color.BLACK);
			        	c.setForeground(Color.WHITE);
			        } else {
			        	c.setBackground(Color.WHITE);
			        	c.setForeground(Color.BLACK);
			        }

			        return c;
			    }
		};
		ticketFive.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JTable table = (JTable) event.getSource();
				Point point = event.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				String value = (String) table.getValueAt(row, column);
				if(calledNumbers.contains(value)) {
					if(!checkedNumbers.contains(value)) {
						checkedNumbers.add(value);
					}
				}
			}
		});
		
		BingoTableModel ticketSixModel = new BingoTableModel(ticketSixString, columnNames);
		ticketSix = new BingoTicket(ticketSixString, columnNames, ticketSixModel, Color.yellow) {
			public Component prepareRenderer(
			        TableCellRenderer renderer, int row, int column)
			    {
			        Component c = super.prepareRenderer(renderer, row, column);
			        String number = (String)ticketSix.getValueAt(row, column);
			        if(checkedNumbers.contains(number)) {
			        	c.setBackground(Color.BLACK);
			        	c.setForeground(Color.WHITE);
			        } else {
			        	c.setBackground(Color.WHITE);
			        	c.setForeground(Color.BLACK);
			        }

			        return c;
			    }
		};
		ticketSix.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JTable table = (JTable) event.getSource();
				Point point = event.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);
				String value = (String) table.getValueAt(row, column);
				if(calledNumbers.contains(value)) {
					if(!checkedNumbers.contains(value)) {
						checkedNumbers.add(value);
					}
				}
			}
		});
		
		messageArea = new JTextArea(40, 1);
		DefaultCaret caret = (DefaultCaret)messageArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		messageArea.setText("Hello " + name + ", welcome to our bingo room!");
		messageArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(messageArea);
		
		JTextField userMessageField = new JTextField(1);
		userMessageField.addActionListener(event -> {
			outputStream.println("MESSAGE " + userMessageField.getText());
			userMessageField.setText("");;
		});
		
		caller.add(callerArea);
		caller.add(callerNumber);
		
		cardArea.add(ticketOne);
		cardArea.add(Box.createVerticalStrut(5));
		cardArea.add(ticketTwo);
		cardArea.add(Box.createVerticalStrut(5));
		cardArea.add(ticketThree);
		cardArea.add(Box.createVerticalStrut(5));
		cardArea.add(ticketFour);
		cardArea.add(Box.createVerticalStrut(5));
		cardArea.add(ticketFive);
		cardArea.add(Box.createVerticalStrut(5));
		cardArea.add(ticketSix);
		cardArea.setPreferredSize(new Dimension(600, 700));
		
		messages.add(scroll);
		messages.add(userMessageField);
		messages.setPreferredSize(new Dimension(500, 700));
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		panel.add(caller);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridheight = 2;
		
		panel.add(cardArea, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		
		panel.add(messages, gbc);
		
		frame.pack();
		
		frame.setVisible(true);
	}
	
	public String[][] getTicketString(int ticketNumber) {
		int[][] grid = bingoCard.getGrid();
		int numberOfElementsInGrid = grid[0].length;
		int ticketStart = ((ticketNumber * 3) - 3);
		
		int ticketLength = 3;
		int[][] ticket = new int[ticketLength][numberOfElementsInGrid];
		
		int ticketIterator = 0;
		for(int i = ticketStart; i < (ticketStart + 3); i++) {
			ticket[ticketIterator] = grid[i];
			ticketIterator++;
		}
		
		String[][] ticketString = new String[ticketLength][numberOfElementsInGrid];
		
		for(int i = 0; i < ticket.length; i++) {
			for(int j = 0; j < ticket[i].length; j++) {
				if(ticket[i][j] == 0) {
					ticketString[i][j] = "";
				}else {
					ticketString[i][j] = Integer.toString(ticket[i][j]);
				}
			}
		}
		return ticketString;
	}
	
	public void	updateMessageArea (String message) {
		messageArea.append("\n" + message);
	}
	
	public void updateCaller(String message) {
		callerNumber.setText(message);
	}
}
