package reqsimile.data;

import javax.swing.table.AbstractTableModel;

/**
 * InputFieldsTableModel is the table model for the tables in the settings
 * dialog used for selecting which fields that are to be displayed in the
 * summary and detail views, and be used for calculation.
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class InputFieldsTableModel extends AbstractTableModel {

	/** Backward serialization compatibility */
	private static final long serialVersionUID = 1L;

	/** Column's names in the table */
	private String[] columnNames = { "Field", "Summary", "Detail", "Calc" };

	/** The table data */
	Object[][] dataSet;

	/**
	 * Resets the table data to hold a specified number of rows.
	 * 
	 * @param rows
	 *            Number of rows in the empty data set
	 */
	public void resetTable(int rows) {
		this.dataSet = new Object[rows][columnNames.length];
		// Set boolean false to the three right-most columns as default
		for (int r = 0; r < rows; r++)
			for (int c = 1; c < columnNames.length; c++)
				setValueAt(new Boolean(false), r, c);
	}

	/**
	 * Returns the number of columns available in the table model
	 * 
	 * @return Number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Returns the number of rows available in the table model
	 * 
	 * @return Number of rows
	 */
	public int getRowCount() {
		return this.dataSet.length;
	}

	/**
	 * Returns true if a cell is editable. All columns except the first is
	 * editable.
	 * 
	 * @param row
	 *            A row in the table
	 * @param col
	 *            A column in the table
	 * @return true if the cell is editable, false otherwise
	 */
	public boolean isCellEditable(int row, int col) {
		return (col > 0);
	}

	/**
	 * Return the name for a column
	 * 
	 * @param col
	 *            A column in the table
	 * @return The name of the column
	 */
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/**
	 * Returns the data type for a column
	 * 
	 * @param col
	 *            A column in the table
	 * @return The java type class for the data in the column
	 */
	public Class getColumnClass(int col) {
		if (getValueAt(0, col) != null)
			return getValueAt(0, col).getClass();
		else
			return String.class;
	}

	/**
	 * Returns the value at row, col
	 * 
	 * @param row
	 *            A row in the table
	 * @param col
	 *            A column in the table
	 * @return A Java object representing the value
	 */
	public Object getValueAt(int row, int col) {
		return this.dataSet[row][col];
	}

	/**
	 * Sets the value at row, col
	 * 
	 * @param value
	 *            The value to set
	 * @param row
	 *            A row in the table
	 * @param col
	 *            A column in the table
	 */
	public void setValueAt(Object value, int row, int col) {
		if (row >= getRowCount()) {
			// Add a row to the data set
			Object[][] newDataSet = new Object[row + 1][getColumnCount()];
			System.arraycopy(dataSet, 0, newDataSet, 0, dataSet.length);
			int oldRowCount = getRowCount();
			dataSet = newDataSet;
			// Set boolean false to the three right-most columns as default
			for (int r = oldRowCount; r <= row; r++)
				for (int c = 1; c < getColumnCount(); c++)
					setValueAt(new Boolean(false), r, c);
			// Notify changes
			fireTableRowsInserted(getRowCount() - 1, row);
			//fireTableStructureChanged();
		}
		dataSet[row][col] = value;
		fireTableCellUpdated(row, col);
	}

}