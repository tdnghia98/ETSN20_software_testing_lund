/*
 * Created on 2004-aug-03
 *
 */
package reqsimile.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import reqsimile.Application;
import reqsimile.gui.*;

/**
 * ReqData holds all the requirements data used. It is used by ReqDataModel to
 * present all or a subset of the available information.
 * 
 * @author Johan Natt och Dag
 * @see ReqDataModel
 * @since 1.4.2
 */
public class ReqData {

    Connection dbConn = null; // Hold the JDBC Connection

    GUI_Main gui; // Refrence to main GUI

    int idCol = 0; // Requirements id column

    Object[][] dataSet = {}; // Requirements data

    ArrayList ids; // Requirement ids

    Object[] columnNames = {}; // Requirements data column names

    int[] columnTypes = {}; // Summary, detail, calc (see FIELD_ constants)

    /**
     * Constructor
     *  
     */
    public ReqData() {
        this.gui = Application.getGUI();
    }

    /**
     * Set the database connection for this data set
     * 
     * @param dbConn
     *            A Connection
     */
    public void setDbConnection(Connection dbConn) {
        this.dbConn = dbConn;
    }

    /**
     * Returns the database connection for this data model
     * 
     * @return The Connection object
     */
    public Connection getDbConnection() {
        return dbConn;
    }

    /**
     * Releases the database connection for this data model The connection is
     * closed.
     */
    public void releaseDbConnection() {
        Application.getDbConnections().close(dbConn);
        dbConn = null;
    }

    /**
     * Fetches the tables in the current database Connection must be established
     * or an error will occur.
     * 
     * @return A vector with the names of the tables
     */
    public Object[] getTables() {
        if (dbConn == null)
            return null;
        // Fetch list of tables
        gui.logPrintln("Fetching list of tables.");
        try {
            DatabaseMetaData dmd = dbConn.getMetaData();
            ResultSet rsTables = dmd.getTables(dbConn.getCatalog(), null, "%",
                    new String[] { "TABLE", "VIEW" });

            // Produce list
            Vector items = new Vector();
            while (rsTables.next())
                items.add(rsTables.getString("TABLE_NAME"));
            rsTables.close();
            return items.toArray();
        } catch (Exception e) {
            gui.showError("Unable to fetch list: ", e);
            return null;
        }
    }

    /**
     * Clears the data and sets the data size
     * 
     * @param rows
     *            Number of data rows
     * @param cols
     *            Number of data cols
     */
    public void resetData(int rows, int cols) {
        this.idCol = 0; // Req. id column reset to first column
        //this.id = new Object[rows]; // Initialize to null Objects
        this.dataSet = new Object[rows][cols]; // Initialize to null Objects
        this.columnNames = new Object[cols]; // Initialize to empty column names
        this.columnTypes = new int[cols]; // Initialize to 0, i.e. no type
        this.ids = new ArrayList(); // 
    }

    /**
     * Sets the column names
     * 
     * @param columnNames
     *            String array with column names
     */
    public void setColumnNames(Object[] columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Returns the name of a column
     * 
     * @param col
     *            A column in the data set
     * @return The column name
     */
    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    /**
     * Sets the columnType to specified type The previous type is preserved
     * 
     * @param col
     *            Column for which to set the type
     * @param type
     *            The type (FIELD_XXX constant)
     */
    public void setColumnType(int col, int type) {
        this.columnTypes[col] = (this.columnTypes[col] | type);
    }

    /**
     * Sets the column type for a set of column names to specified type
     * 
     * @param cols
     *            A collection of column names
     * @param type
     *            The type (FIELD_XXX constant in {@link reqsimile.APPCONSTANTS})
     */
    public void setColumnTypes(Collection cols, int type) {
        String field;
        for (Iterator it = cols.iterator(); it.hasNext();) {
            field = it.next().toString();
            int col = getColumnPos(field);
            this.setColumnType(col, type);
        }
    }

    /**
     * Returns an array of the indexes to columns of a specified field type
     * 
     * @param type
     *            The field type (see {@link reqsimile.APPCONSTANTS}).
     * @return Array of column indexes
     */
    public int[] getColumnMap(int type) {

        int[] colMap = {};
        int col = 0;
        for (int c = 0; c < columnTypes.length; c++) {
            if ((this.columnTypes[c] & type) != 0) {
                // Increase size of array
                int[] newColMap = new int[colMap.length + 1];
                System.arraycopy(colMap, 0, newColMap, 0, colMap.length);
                colMap = newColMap;
                // Set map
                colMap[col++] = c;
            }
        }
        return colMap;
    }

    /**
     * Returns the column index for a specified column name
     * 
     * @param colName
     *            A column name
     * @return The column index
     */
    public int getColumnPos(String colName) {
        return Arrays.asList(columnNames).indexOf(colName);
    }

    public int getRowForReqKey(int reqKey) {
        if (ids != null)
            return ids.indexOf(new Integer(reqKey));
        else
            return 0;
    }

    /**
     * Specifies which column that holds the unique requirements id
     * 
     * @param col
     */
    public void setReqKeyColumn(int col) {
        this.idCol = col;
    }

    /**
     * Returns the number of rows in the data set
     * 
     * @return Number of rows in the data set
     */
    public int getRowCount() {
        if (dataSet != null)
            return dataSet.length;
        else
            return 0;
    }

    /**
     * Returns the unique requirements key for a specified row
     * 
     * @param row
     *            A row in the data set
     * @return The requirements key
     */
    public int getKeyAt(int row) {
        int key;
        try {
            key = Integer.parseInt(this.dataSet[row][idCol].toString());
        } catch (Exception e) {
            key = -1;
        }
        return key;
    }

    /**
     * Sets a a value in the data set
     * 
     * @param value
     *            The value to assign
     * @param row
     *            A row in the data set
     * @param col
     *            A column in the data set
     */
    public void setValueAt(Object value, int row, int col) {
        this.dataSet[row][col] = value;
        if (col == idCol)
            ids.add(row, value);
    }

    /**
     * Returns data from a cell in the data set
     * 
     * @param row
     *            A row in the data set
     * @param col
     *            A column in the data set
     * @return The data at (row, col)
     */
    public Object getValueAt(int row, int col) {
        return this.dataSet[row][col];
    }

    /**
     * Returns specified cell values from a particular row in the data set. The
     * cell contents are put in a Collection object.
     * 
     * @param row
     *            A row in the data set
     * @param cols
     *            The names of the columns for which values are returned
     * @return The Collection of cell contents
     */
    public Collection getValueSetAt(int row, Collection cols) {
        Collection values = new ArrayList();
        String field;
        for (Iterator it = cols.iterator(); it.hasNext();) {
            field = it.next().toString();
            int col = getColumnPos(field);
            values.add(this.dataSet[row][col]);
        }
        return values;
    }

    /**
     * Returns specified cell values from a particular row in the data set. The
     * cell contents are concatenated with a space between each cell's content.
     * 
     * @param row
     *            The row to fetch
     * @param cols
     *            The names of the columns for which values are returned
     * @return The concatenated contents of (row, cols)
     */
    public String getValuesAt(int row, Collection cols) {
        String values = "";
        String field;
        for (Iterator it = cols.iterator(); it.hasNext();) {
            field = it.next().toString();
            int col = getColumnPos(field);
            if (this.dataSet[row][col] != null)
                values += this.dataSet[row][col].toString() + " ";
        }
        return values.trim();
    }
    
    /**
     * Return a list of concatenated requirements as string.
     * 
     * @return The requirements as a single string.
     */
    public String concatenateAllReqs() {
    	StringBuffer sb = new StringBuffer();
    	try {
	    	int counter = 0;
	    	for (int i=0; i<dataSet.length; i++) {
	    		for (int j=0; j<dataSet.length; j++) {
	    			if (dataSet[i][j] != null) {
	    				sb.append(dataSet[i][j].toString() + ", ");
	    			}
	    		}
	    	}
    	}
    	catch (Exception e) {
    		// Better safe than sorry
    	}
    	return sb.toString();
    }

    /**
     * Computes total number of requirements.
     * 
     * @return The total number.
     */
	public int sumAllRequirements() {
		return dataSet.length;
	}

}