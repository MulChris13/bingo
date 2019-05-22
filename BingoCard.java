import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

public class BingoCard {
	int columnCount = 9;
	int[][] grid = new int[columnCount * 2][columnCount];
	ArrayList<ArrayList<Integer>> numbers = new ArrayList<ArrayList<Integer>>();

	int[][] combinations = {{1,1,0,1,1,0,1,0,0}, {1,1,0,1,0,1,1,0,0}, {1,0,1,1,0,1,1,0,0},
                          	{1,1,0,1,1,0,0,1,0}, {1,1,0,1,0,1,0,1,0}, {1,0,1,1,0,1,0,1,0},
                          	{1,1,0,0,1,1,0,1,0}, {1,0,1,0,1,1,0,1,0}, {0,1,1,0,1,1,0,1,0},
                          	{1,1,0,1,0,0,1,1,0}, {1,0,1,1,0,0,1,1,0}, {1,1,0,0,1,0,1,1,0},
                          	{1,0,1,0,1,0,1,1,0}, {0,1,1,0,1,0,1,1,0}, {1,0,0,1,1,0,1,1,0},
                          	{0,1,0,1,1,0,1,1,0}, {1,1,0,1,0,1,0,0,1}, {1,0,1,1,0,1,0,0,1},
                          	{1,1,0,0,1,1,0,0,1}, {1,0,1,0,1,1,0,0,1}, {0,1,1,0,1,1,0,0,1},
                          	{1,1,0,1,0,0,1,0,1}, {1,0,1,1,0,0,1,0,1}, {1,1,0,0,1,0,1,0,1},
                          	{1,0,1,0,1,0,1,0,1}, {0,1,1,0,1,0,1,0,1}, {1,0,0,1,1,0,1,0,1},
                          	{0,1,0,1,1,0,1,0,1}, {1,0,1,0,0,1,1,0,1}, {0,1,1,0,0,1,1,0,1},
                           	{1,0,0,1,0,1,1,0,1}, {0,1,0,1,0,1,1,0,1}, {0,0,1,1,0,1,1,0,1},
                           	{1,1,0,0,1,0,0,1,1}, {1,0,1,0,1,0,0,1,1}, {0,1,1,0,1,0,0,1,1},
                           	{1,0,0,1,1,0,0,1,1}, {0,1,0,1,1,0,0,1,1}, {1,0,1,0,0,1,0,1,1},
                           	{0,1,1,0,0,1,0,1,1}, {1,0,0,1,0,1,0,1,1}, {0,1,0,1,0,1,0,1,1},
                           	{0,0,1,1,0,1,0,1,1}, {0,1,0,0,1,1,0,1,1}, {0,0,1,0,1,1,0,1,1}};

	public BingoCard() {
	  generateGridLayout(combinations);
	  createAndPopulateSubLists();
	  insertSubListsIntoGrid();
	}

	public void generateGridLayout(int[][] combinations) {
		boolean validPanel, validSheet = false;
		int lBound, uBound;
		int aRandomRowCombination;

		while(!validSheet) {
			lBound = 0; uBound = 2;
			for(int i = 0; i < grid.length/3; i++) {
				if(i > 0) {
					lBound += 3;
					uBound += 3;
			 	}
				validPanel = false;
				while(!validPanel) {
					for(int r = lBound; r <= uBound; r++) {
						aRandomRowCombination = (int)(Math.random() * combinations.length);
						for(int listCount = 0; listCount < columnCount; listCount++) {
							grid[r][listCount] = combinations[aRandomRowCombination][listCount];
						}
					}
					validPanel = validateGridPanel(lBound, uBound);
				}
			}
			validSheet = validateEntireGrid();
		}
	}

	public boolean validateEntireGrid() {
		boolean validColumn = true;
		int maxColumnEntries;
		int valueCounter;

		for(int listCount = 0; listCount < columnCount && validColumn; listCount++) {
			valueCounter = 0;
			if(listCount == 0) {
				maxColumnEntries = 9;
			} else if(listCount == (columnCount - 1)) {
				maxColumnEntries = 11;
			} else {
				maxColumnEntries = 10;
			}

			for(int i = 0; i < grid.length; i++) {
				if(grid[i][listCount] == 1) {
					valueCounter++;
				}
			}

			if(valueCounter != maxColumnEntries) {
				validColumn = false;
			}
		}
		return validColumn;
	}

	public boolean validateGridPanel(int lBound, int uBound) {
		boolean validPanel = true;
		for(int listCount = 0; listCount < columnCount && validPanel; listCount++)
		{
			if((grid[lBound][listCount] + grid[lBound + 1][listCount] + grid[uBound][listCount]) < 1) {
				validPanel = false;
			}
		}
		return validPanel;
	}

	public void createAndPopulateSubLists() {
		int minValue, maxValue;

		for(int listIndex = 0; listIndex < columnCount; listIndex++) {
			numbers.add(new ArrayList<Integer>());
		}

		for(int listIndex = 0; listIndex < columnCount; listIndex++) {
			if(listIndex == 0) {
				minValue = 1;
			} else {
				minValue = listIndex * 10;
			}

			maxValue = listIndex * 10 + columnCount;

			if(listIndex == (columnCount - 1)) {
				maxValue += 1;
			}

			for(int value = minValue; value <= maxValue; value++) {
				numbers.get(listIndex).add(value);
			}

			Collections.shuffle(numbers.get(listIndex));
		}
	}

	public void insertSubListsIntoGrid() {
		int row;
		for(int c = 0; c < columnCount; c++) {
			row = 0;
			while(numbers.get(c).size() != 0) {
				if(grid[row][c] == 1) {
					grid[row][c] = numbers.get(c).get(0);
					numbers.get(c).remove(0);
				}
				row++;
			}
		}
	}

	public void displayGrid(PrintStream outputStream) {
		int i, j;
		for(i = 0; i < grid.length; i++) {
			for(j = 0; j < grid[i].length; j++) {
				if(grid[i][j] != 0) {
					outputStream.printf("%4d", grid[i][j]);
				} else {
					outputStream.print("    ");
				}
			}
			outputStream.println();
		}
	}
	
	public String toString() {
		String card = "";
		
		int i, j;
		for(i = 0; i < grid.length; i++) {
			for(j = 0; j < grid[i].length; j++) {
				if(grid[i][j] != 0) {
					card += String.format("%4d", grid[i][j]);
				} else {
					card += ("    ");
				}
			}
			card += System.lineSeparator();
		}
		return card;
	}
	
	public void displayGrid(PrintStream outputStream, int[][] gridReplacement) {
		int i, j;
		for(i = 0; i < gridReplacement.length; i++) {
			for(j = 0; j < gridReplacement[i].length; j++) {
				if(gridReplacement[i][j] != 0) {
					outputStream.printf("%4d", gridReplacement[i][j]);
				} else {
					outputStream.print("    ");
				}
			}
			outputStream.println();
		}
	}
	
	public int[][] getGrid() {
		return grid;
	}
}
