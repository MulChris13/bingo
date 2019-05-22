import java.util.ArrayList;

public class Caller {
	private ArrayList<Integer> calledNumbers;
	
	public Caller() {
		calledNumbers = new ArrayList<Integer>();
	}
	
	public int chooseNumber() {
		boolean calledPreviously = true;
		int number = 0;
		while(calledPreviously) {
			number = (int)(Math.random() * 90) + 1;
			if(!calledNumbers.contains(number)) {
				calledNumbers.add(number);
				calledPreviously = false;
			}
		}
		return number;
	}
}
