package reqsimile.data;

import java.awt.Color;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.swing.table.AbstractTableModel;

import reqsimile.*;
import reqsimile.gui.*;

/**
 * ReqDataModel is the middle layer between the actual data and a table in the
 * user interface. It provides a JTable with the data to present in the table
 * and fetches that data from an associated data set instaniated from
 * {@link ReqData}.
 * <p>
 * (ReqData) => (ReqDataModel) => (JTable)
 * <p>
 * The class is designed to provide the data for all table views, both the
 * summary view (list of requirements) and the detail view (requirements
 * details). This has made several methods rather complicated and it would be a
 * good idea to split it up for easier maintenance.
 * 
 * @author Johan Natt och Dag
 * @see ReqData
 * @since 1.4.2
 */
public class ReqDataModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    String name; // The name of this data set

    ReqData dataSet; // The data

    int type; // The column type this model represents

    int[] headerMap = {}; // Maps the (row/column) header fields to the data set

    String[] columnNames = null; // May override columnMap if we want to set

    // column names explicitly

    int singleRowMap = -1; // Refers to a row in the dataSet

    ArrayList rowMap; // Used for filtering the dataSet in req id

    int markedReqKey; // The double-clicked req that is linked to the links set

    int markedReqRow = -1; // Row in data set for the double-clicked req that is

    // linked to the links set

    int linksReqSet; // The req set in which the linked requirements are

    HashMap links = new HashMap(); // Linked requirements (with count)

    //   For summary view the count reflects the number of links to the
    // requirement
    //   For result view the count is interpreted binary
    //      00 - not used (link removed from list)
    //		01 - linked to marked requirement only
    //		10 - linked to another requirement only
    //		11 - linked to both another and the marked requirements
    ArrayList referenceRows; // Holds the row numbers for reference for

    // extracting elements with zero similarity

    GUI_Main gui; // Refrence to GUI

    /**
     * Constructor. Sets the data source and friendly name.
     * 
     * @param aName
     *            Friendly name of requirements set
     * @param data
     *            Underlying data set
     * @param type
     *            Column type (@link reqsimile.APPCONSTANTS}
     */
    public ReqDataModel(String aName, ReqData data, int type) {
        this.name = aName;
        this.dataSet = data;
        this.type = type;
        this.gui = Application.getGUI();
        updateRowReferenceArray();
    }

    /**
     * Empty constructor for subclassing
     */
    public ReqDataModel() {
    }

    /**
     * Sets the friendly name of the requirement's set
     * 
     * @param aName
     *            Name of requirements set
     */
    public void setName(String aName) {
        this.name = aName;
    }

    /**
     * Returns the Data name
     * 
     * @return The friendly name
     *  
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets which underlying data set to use for this model
     * 
     * @param data
     *            The data set
     */
    public void setDataSet(ReqData data) {
        this.dataSet = data;
        updateRowReferenceArray();
    }

    /**
     * Returns which underlying data set is used for this model
     * 
     * @return A data set
     */
    public ReqData getDataSet() {
        return this.dataSet;
    }

    /**
     * Sets which requirements in the data model that are linked to the other
     * requirements set. It is used to color the
     * 
     * @param markedKey
     *            The key of the requirement for which similar requirements are
     *            currently shown. This is used to show requirements that are
     *            linked to this requirement in a different color.
     * @param reqSet
     *            The id of the requirements set to which this set is linked.
     *            (i.e. the requirements set from which similar requirements are
     *            currently shown)
     * @param links
     *            The set of requirements that are linked. The set also conatins
     *            information on either (1) how many requirements that are
     *            linked to each requirement. (2) The link status (see
     *            declaration).
     */
    public void setReqLinks(int markedKey, int reqSet, HashMap links) {
        this.markedReqKey = markedKey;
        this.linksReqSet = reqSet;
        this.links = (HashMap) links;
    }

    /**
     * Returns the id of the requirements set to which this set is linked.
     * 
     * @return The requirements id
     */
    public int getLinksReqSet() {
        return linksReqSet;
    }

    /**
     * Adds a link to a requirement in the data model. If there already exists a
     * link to that requirement, the link count is increased.
     * 
     * @param key
     *            A unique requirements key
     */
    public void increaseLinkStatus(Integer key) {
        int currentCount;

        if (key == null)
            key = new Integer(markedReqKey);

        int row = this.dataSet.getRowForReqKey(key.intValue());

        if (links.containsKey(key))
            currentCount = ((Integer) this.links.get(key)).intValue();
        else
            currentCount = 0;
        currentCount++;
        this.links.put(key, new Integer(currentCount));

        fireTableRowsUpdated(row, row);
    }

    /**
     * Decreases the number of links to a requirement in the data model. If the
     * number of links becomes 0, the link is completely removed from the set of
     * link set.
     * 
     * @param key
     *            A unique requirements key
     */
    public void decreaseLinkStatus(Integer key) {
        int currentCount;

        if (key == null)
            key = new Integer(markedReqKey);

        int row = this.dataSet.getRowForReqKey(key.intValue());

        if (links.containsKey(key))
            currentCount = ((Integer) this.links.get(key)).intValue();
        else
            currentCount = 1;

        currentCount--;

        if (currentCount == 0)
            this.links.remove(key);
        else
            this.links.put(key, new Integer(currentCount));

        fireTableRowsUpdated(row, row);
    }

    /**
     * Sets which requirement that is marked for linkage.
     * 
     * @param row
     *            A row in the data model
     */
    public void setMarkedLink(int row) {
        int oldMarkedRow = markedReqRow;
        this.markedReqRow = row;
        this.markedReqKey = getKeyAt(row);
        gui.repaintTable(gui.getRsForVisibleTab());
    }

    /**
     * Returns the row that is currently marked for linkage.
     * 
     * @return A row in the data model
     */
    public int getMarkedLinkRow() {
        return markedReqRow;
    }

    /**
     * Reports whether a certain row is marked for linkage.
     * 
     * @param row
     *            A row in the data model to be queried
     * @return True if the row is marked, false otherwise
     */
    public boolean isMarked(int row) {
        return (row == this.markedReqRow);
    }

    /**
     * Changes the link status between the currently marked row and a specified
     * row. Also updates the link database to instantly reflect the changes.
     * 
     * @param row
     *            A row in the data model for which the links status
     */
    public void swapLink(int row) {
        int count;
        Integer key = new Integer(getKeyAt(row));
        Statement sqlStmt;

        Integer countI = ((Integer) links.get(key));

        if (countI == null)
            count = 0;
        else
            count = countI.intValue();
        // Swap lower bit
        count ^= 1;

        if ((count & 1) == 1) {
            links.put(key, new Integer(count)); // 11 or 10 - so keep it

            String sqlInsert = "INSERT INTO ["
                    + Application.getProjectPref(
                            APPCONSTANTS.PREF_SECTION_LINKS,
                            APPCONSTANTS.PREF_DB_TABLE,
                            APPCONSTANTS.PREF_DEF_DB_TABLE)
                    + "] (["
                    + Application.getProjectPref(
                            APPCONSTANTS.PREF_SECTION_LINKS,
                            APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[Common
                                    .otherReqSet(linksReqSet)],
                            APPCONSTANTS.PREF_DEF_DB_FIELD_LINKS_KEY[Common
                                    .otherReqSet(linksReqSet)])
                    + "],["
                    + Application
                            .getProjectPref(
                                    APPCONSTANTS.PREF_SECTION_LINKS,
                                    APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[linksReqSet],
                                    APPCONSTANTS.PREF_DEF_DB_FIELD_LINKS_KEY[linksReqSet])
                    + "]) VALUES (" + markedReqKey + "," + key.toString()
                    + ");";
            try {
                sqlStmt = dataSet.getDbConnection().createStatement();
                sqlStmt.executeUpdate(sqlInsert);
                sqlStmt.close();
            } catch (SQLException e) {
                gui.logPrintln("Unable to add link in database ("
                        + markedReqKey + "," + key.toString() + ")!");
            }

            // Also make sure the summary view's link status is updated
            Application.getRsSummaryDataModel(Common.otherReqSet(linksReqSet))
                    .increaseLinkStatus(null);
            Application.getRsSummaryDataModel(linksReqSet).increaseLinkStatus(
                    key);
        } else {
            if (count == 0)
                links.remove(key); // 00 - remove key
            else
                links.put(key, new Integer(count)); // 01 - add key

            String sqlDelete = "DELETE FROM ["
                    + Application.getProjectPref(
                            APPCONSTANTS.PREF_SECTION_LINKS,
                            APPCONSTANTS.PREF_DB_TABLE,
                            APPCONSTANTS.PREF_DEF_DB_TABLE)
                    + "] WHERE ["
                    + Application.getProjectPref(
                            APPCONSTANTS.PREF_SECTION_LINKS,
                            APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[Common
                                    .otherReqSet(linksReqSet)],
                            APPCONSTANTS.PREF_DEF_DB_FIELD_LINKS_KEY[Common
                                    .otherReqSet(linksReqSet)])
                    + "]="
                    + markedReqKey
                    + " AND ["
                    + Application
                            .getProjectPref(
                                    APPCONSTANTS.PREF_SECTION_LINKS,
                                    APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[linksReqSet],
                                    APPCONSTANTS.PREF_DEF_DB_FIELD_LINKS_KEY[linksReqSet])
                    + "]=" + key.toString();
            try {
                sqlStmt = dataSet.getDbConnection().createStatement();
                sqlStmt.executeUpdate(sqlDelete);
                sqlStmt.close();
            } catch (SQLException e) {
                gui.logPrintln("Unable to remove link in database ("
                        + markedReqKey + "," + key.toString() + ")!");
            }

            // Decrease links status for this requirement
            Application.getRsSummaryDataModel(Common.otherReqSet(linksReqSet))
                    .decreaseLinkStatus(null);
            Application.getRsSummaryDataModel(linksReqSet).decreaseLinkStatus(
                    key);
        }
        fireTableRowsUpdated(row, row);

    }

    /**
     * Returns the number of rows in the data model If column names are
     * explicitly set, then the table is rotated, and the row count equals the
     * header count.
     * 
     * @return Number of rows in the data model
     */
    public int getRowCount() {
        if (this.singleRowMap == -1)
            if (this.rowMap == null)
                return this.dataSet.getRowCount();
            else
                return this.rowMap.size();
        else if (this.singleRowMap == -2)
            return 0;
        else
            return this.headerMap.length;
    }

    /**
     * Sets a row reference to a single row in the undelying data set. If this
     * is set, the data model will provide the detail view.
     * 
     * @param row
     *            The reference to the row in the underlying data set
     * @see #setRowMap(int[])
     */
    public void setRowMap(int row) {
        this.rowMap = null;
        this.singleRowMap = row;
    }

    /**
     * Sets row references to a number of rows in the underlying data set. This
     * is used to display a subset of the rows from the data set. If this is
     * set, the data model will provide the summary view.
     * 
     * @param rows
     *            Array of references to the rows in the underlying data set
     * @see #setRowMap(int)
     */
    public void setRowMap(int[] rows) {
        this.singleRowMap = -1;
        this.rowMap = Common.toArrayList(rows);
    }

    /**
     * Returns the row in the data set that corresponds to the specified row in
     * the model.
     * 
     * @param row
     *            The row index in the table model
     * @return The row index in the data set
     */
    public int getRowMap(int row) {
        if (rowMap == null)
            return row;
        else if (rowMap.size() == 0)
            return -2;
        else
            return ((Integer) rowMap.get(Math.max(0, row))).intValue();
    }

    /**
     * Sets the columns in the data set to be provided by this data model.
     * 
     * @param coll
     *            A Collection of column names (fields) in the underlying data
     *            set
     */
    public void setHeaderMap(ArrayList coll) {
        this.headerMap = new int[coll.size()];
        for (int i = 0; i < this.headerMap.length; i++) {
            this.headerMap[i] = this.dataSet.getColumnPos((String) coll.get(i));
        }
    }

    /**
     * Sets the columns in the data set to be provided by this data model.
     * 
     * @param coll
     *            An array of refereces to columns indexes in the data set
     */
    public void setHeaderMap(int[] coll) {
        this.headerMap = coll;
    }

    /**
     * Returns the set of column index selected from the data set to be provided
     * by this data model.
     * 
     * @return An array of column index references to the underlying data set
     */
    public int[] getHeaderMap() {
        return this.headerMap;
    }

    /**
     * Sets the column names for this model explicitly Default is to get it from
     * the underlying data set.
     * 
     * @param columnNames
     *            String array with column names (may be null)
     */
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Returns the number of columns in the data model
     * 
     * @return The number of columns
     */
    public int getColumnCount() {
        if (this.singleRowMap < 0)
            return headerMap.length;
        else
            return columnNames.length;
    }

    /**
     * Returns the name of a column in the data model
     * 
     * @param col
     *            A column in the data model
     * @return The column name
     */
    public String getColumnName(int col) {
        if (this.singleRowMap < 0) {
            if (this.headerMap[col] < 0)
                return ""; //columnNames[col];
            else
                return this.dataSet.getColumnName(headerMap[col]);
        } else
            return columnNames[col];
    }

    /**
     * Returns the column data type
     * 
     * @param col
     *            A column in the data model
     * @return The column class
     */
    public Class getColumnClass(int col) {
        if (getValueAt(0, col) == null)
            return String.class;
        else
            return getValueAt(0, col).getClass();
    }

    /**
     * Returns the column index for a specified column name
     * 
     * @param colName A column name
     * @return The column index
     */
    public int getColumnPos(String colName) {
        return this.dataSet.getColumnPos(colName);
    }

    /**
     * Returns value at specified position
     * 
     * @param row
     *            A row in the data model
     * @param col
     *            A column in the data model
     * @return The cell contents
     */
    public Object getValueAt(int row, int col) {
        if (this.singleRowMap < 0)
            if (this.rowMap == null)
                return this.dataSet.getValueAt(row, headerMap[col]);
            else {
                return this.dataSet.getValueAt(((Integer) rowMap.get(row))
                        .intValue(), headerMap[col]);
            }
        else
            return this.dataSet.getValueAt(singleRowMap, headerMap[row]);
    }

    /**
     * Get the unique requirements key at the specified row
     * 
     * @param row
     *            A row in the table model
     * @return The requirements key
     */
    public int getKeyAt(int row) {
        if (this.singleRowMap < 0)
            if (this.rowMap == null)
                return this.dataSet.getKeyAt(row);
            else
                return this.dataSet.getKeyAt(((Integer) rowMap.get(row))
                        .intValue());
        else
            return this.dataSet.getKeyAt(singleRowMap);
    }

    /**
     * Returns whether specified cell is editable
     * 
     * @param row
     *            A row in the data model
     * @param col
     *            A column in the data model
     */
    public boolean isCellEditable(int row, int col) {
        return (getColumnName(col)
                .equals(APPCONSTANTS.RESULT_FIELD_LINK_BUTTON));
    }

    /**
     * Returns the foreground color (text color) for a specified row in the data
     * model based on the similarity value assigned to the requirement at that
     * row.
     * 
     * @param row
     *            A row in the data model
     * @return A Color
     */
    public Color getRowForegroundColor(int row) {
        int col = dataSet.getColumnPos(APPCONSTANTS.RESULT_FIELD_SIMVALUE);
        if (col >= 0) {
            Double simValue = (Double) this.dataSet.getValueAt(
                    ((Integer) rowMap.get(row)).intValue(), col);
            return new Color(new Float(1).floatValue(), new Float(0)
                    .floatValue(), new Float(0).floatValue(), simValue
                    .floatValue());
        } else {
            return null;
        }
    }

    /**
     * Returns the background color for a specified row in the data model based
     * on the similarity value assigned to the requirement at that row.
     * 
     * @param row
     *            A row in the data model
     * @return A Color
     */
    public Color getSimBackgroundColor(int row) {
        int col = dataSet.getColumnPos(APPCONSTANTS.RESULT_FIELD_SIMVALUE);
        if (col >= 0) {
            Double simValue = (Double) this.dataSet.getValueAt(
                    ((Integer) rowMap.get(row)).intValue(), col);
            return new Color(255, 0, 0, Math.round(255 * simValue.floatValue()));
        } else
            return null;
    }

    /**
     * Returns the similarity value for a row in the dataset (if there is such a
     * column available)
     * 
     * @param row
     *            A row in the data set
     * @return Similarity value on succes, -1 otherwise
     */
    public Double getSimValue(int row) {
        int col = dataSet.getColumnPos(APPCONSTANTS.RESULT_FIELD_SIMVALUE);
        if (col >= 0) {
            Double simValue = (Double) this.dataSet.getValueAt(
                    ((Integer) rowMap.get(row)).intValue(), col);
            if (simValue == null)
                return new Double(0);
            else
                return simValue;
        } else
            return new Double(-1);
    }

    /**
     * Returns true if the requirement at row is linked
     * 
     * @param row
     *            A row in the table model
     * @return True is the requirements is linked, false otherwise
     */
    public boolean isLinked(int row) {
        if (this.singleRowMap < 0)
            if (this.rowMap == null)
                return this.links.containsKey(new Integer(this.dataSet
                        .getKeyAt(row)));
            else
                return this.links.containsKey(new Integer(this.dataSet
                        .getKeyAt(((Integer) rowMap.get(row)).intValue())));
        else
            return this.links.containsKey(new Integer(this.dataSet
                    .getKeyAt(singleRowMap)));
    }

    /**
     * Returns the link status for a specified row. This is used for the result
     * view only and the status is interpreted binary: 00 - not used (link
     * removed from list) 01 - linked to marked requirement only 10 - linked to
     * another requirement only 11 - linked to both another and the marked
     * requirements
     * 
     * @param row
     *            A row in the table model
     * @return The link status
     */
    public int getLinkStatus(int row) {
        Integer linkStatus;

        if (this.singleRowMap < 0)
            if (this.rowMap == null)
                linkStatus = (Integer) this.links.get(new Integer(this.dataSet
                        .getKeyAt(row)));
            else
                linkStatus = (Integer) this.links.get(new Integer(this.dataSet
                        .getKeyAt(((Integer) rowMap.get(row)).intValue())));
        else
            linkStatus = (Integer) this.links.get(new Integer(this.dataSet
                    .getKeyAt(singleRowMap)));

        if (linkStatus == null)
            return 0;
        else
            return linkStatus.intValue();
    }

    /**
     * Builds a helper array with all rows in the underlying data set
     * 
     * @see #showZeroSimilarity()
     */
    public void updateRowReferenceArray() {
        referenceRows = new ArrayList();
        for (int i = 0; i < this.dataSet.getRowCount(); i++)
            referenceRows.add(new Integer(i));
    }

    /**
     * Adds all rows in the underlying data set that is currently not referred
     * to by the data model. This is used to add all requirements that were
     * assigned zero similarity to the end of the resulting list of similar
     * requirements.
     */
    public void showZeroSimilarity() {
        ArrayList zeroSimilarityRows = new ArrayList(referenceRows);
        zeroSimilarityRows.removeAll(this.rowMap);
        this.rowMap.addAll(zeroSimilarityRows);

        int col = getColumnPos(APPCONSTANTS.RESULT_FIELD_SIMVALUE);
        // Clear old similarity values
        for (int i = 0; i < zeroSimilarityRows.size(); i++) {
            dataSet.setValueAt(new Double(0), ((Integer) zeroSimilarityRows
                    .get(i)).intValue(), col);
        }
    }
}