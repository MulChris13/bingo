import javax.swing.table.DefaultTableModel;

public class BingoTableModel extends DefaultTableModel {


	private static final long serialVersionUID = 1L;

	public BingoTableModel(String[][] dataValues, String[] columnNames) {
		super(dataValues, columnNames); 
	}
	
	public boolean isCellEditable(int row, int cols) {
		return false;
	}
}
