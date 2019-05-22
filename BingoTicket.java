import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class BingoTicket extends JTable {

	private static final long serialVersionUID = 1L;
	private int gap = 19;
	
	public BingoTicket(String[][] ticketString, String[] columnNames, DefaultTableModel model, Color borderColor) {
		super(ticketString, columnNames);
		setTableHeader(null);
		setModel(model);
		setCellsAlignment(SwingConstants.CENTER);
		setFont(new Font("Serif", Font.BOLD, 21));
		setRowHeight(getRowHeight() + gap);
		setBorder(BorderFactory.createLineBorder(borderColor, 2));
	}
	
	public void setCellsAlignment(int alignment) {
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(alignment);

        TableModel tableModel = getModel();

        for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++)
        {
            getColumnModel().getColumn(columnIndex).setCellRenderer(rightRenderer);
        }
	}
}
