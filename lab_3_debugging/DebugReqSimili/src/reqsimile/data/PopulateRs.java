package reqsimile.data;

import java.sql.*;
import java.util.*;

import javax.swing.table.DefaultTableModel;

import reqsimile.*;
import reqsimile.gui.*;

/**
 * PopulateRs is a thread that reads all the data from the database and
 * populates the data sets from which the tables get their data to display.
 * <p>
 * Any field as specified to be included in the summary view, detail view or the
 * calculation are fecthed from the database. The field for calculation are used
 * in the preprocessing step ({@link PreProcessRs})
 * <p>
 * This class should not be used if data is to beread on a needs basis. The
 * current approach is not memory efficient as it reads all data into memory. It
 * is done for speed purposes and because the program currently can not answer
 * incrementally to any database changes. The preprocessing is made upon request
 * and on the snapshot of the database, read by the run method in this class.
 * 
 * @author Johan Natt och Dag
 * @see ReqData
 * @since 1.4.2
 */
public class PopulateRs extends Thread {

	/** Main gui window */
	GUI_Main gui;

	/**
	 * Constructor
	 *  
	 */
	public PopulateRs() {
		this.gui = Application.getGUI();
	}

	/**
	 * Starts the thread that reads all required data from the database
	 */
	public void run() {
		ReqData reqData;
		ReqDataModel reqSummaryDataModel;
		Connection dbConn; // Database connection
		Statement sqlQuery; // The SQL statement
		ResultSet rs; // The result of my SQL statement
		ResultSetMetaData rsMetaData; // 
		int nRows = 0;
		String sqlTable;
		String rsName;

		// Measure the approximate time it takes
		long startTime = System.currentTimeMillis();

		// Clear results table
		Application.getRsSummaryDataModel(APPCONSTANTS.S).setRowMap(new int[0]);
		Application.getRsDetailDataModel(APPCONSTANTS.S).setRowMap(-2);
		Application.getRsSummaryDataModel(APPCONSTANTS.S)
				.fireTableStructureChanged();
		Application.getRsDetailDataModel(APPCONSTANTS.S)
				.fireTableStructureChanged();

		for (int reqSet = APPCONSTANTS.A; reqSet <= APPCONSTANTS.B; reqSet++) {
			reqData = Application.getRsDataSet(reqSet);
			reqSummaryDataModel = Application.getRsSummaryDataModel(reqSet);

			rsName = Application.getProjectPref(
					APPCONSTANTS.PREF_SECTION_DB[reqSet],
					APPCONSTANTS.PREF_DB_NAME,
					APPCONSTANTS.PREF_DEF_DB_NAME[reqSet]);

			// Get db connection
			dbConn = reqData.getDbConnection();
			if (dbConn == null) {
				gui.logPrintln("No connection established for " + rsName);
				return; // Quit thread if there is no connection available
			}

			gui.disableInteraction(reqSet);

			// Update listeners for column width settings
			gui.updateRsListeners(reqSet);

			// Get id field
			String idField = Application.getProjectPref(
					APPCONSTANTS.PREF_SECTION_DB[reqSet],
					APPCONSTANTS.PREF_DB_FIELD_KEY,
					APPCONSTANTS.PREF_DEF_DB_FIELD_KEY);
			// Get selected summary fields
			Collection summaryFields = Common.toCollection(Application
					.getProjectPref(APPCONSTANTS.PREF_SECTION_DB[reqSet],
							APPCONSTANTS.PREF_DB_FIELDS_SUMMARY,
							APPCONSTANTS.PREF_DEF_DB_FIELDS_SUMMARY));
			// Get selected detail fields
			Collection detailFields = Common.toCollection(Application
					.getProjectPref(APPCONSTANTS.PREF_SECTION_DB[reqSet],
							APPCONSTANTS.PREF_DB_FIELDS_DETAIL,
							APPCONSTANTS.PREF_DEF_DB_FIELDS_DETAIL));
			// Get calculation fields
			Collection calcFields = Common.toCollection(Application
					.getProjectPref(APPCONSTANTS.PREF_SECTION_DB[reqSet],
							APPCONSTANTS.PREF_DB_FIELDS_CALC,
							APPCONSTANTS.PREF_DEF_DB_FIELDS_CALC));

			gui.logPrintln("Counting total row set in " + rsName);
			gui.setStatus("Fetching information from database table...");

			// Create statement
			try {
				sqlQuery = dbConn.createStatement();
			} catch (Exception e) {
				gui.showError("Unable to create SQL statement for fetching "
						+ rsName, e);
				gui.enableInteraction(reqSet);
				return;
			}

			try {
				sqlTable = "["
						+ Application.getProjectPref(
								APPCONSTANTS.PREF_SECTION_DB[reqSet],
								APPCONSTANTS.PREF_DB_TABLE,
								APPCONSTANTS.PREF_DEF_DB_TABLE) + "]";
				rs = sqlQuery.executeQuery("SELECT COUNT(*) from " + sqlTable);
				rs.next(); // There should be one row
				nRows = rs.getInt(1); // Get value in first column (# records)
				rs.close();
			} catch (Exception e) {
				gui.showError("Unable to count rows in " + rsName, e);
				// Enable table selection listener again
				gui.enableInteraction(reqSet);
				return;
			}

			gui.logPrintln("Found " + nRows + " rows in " + rsName);

			// Create SQL statement

			// Get the union of all selected fields (through the aid of a
			// HashSet)
			Collection sqlFieldsSet = new LinkedHashSet(); // Predictable field
			// order!
			// Include id field
			sqlFieldsSet.add(idField);
			// Include summary fields
			sqlFieldsSet.addAll(summaryFields);
			// Include detail fields
			sqlFieldsSet.addAll(detailFields);
			// Include calc fields
			sqlFieldsSet.addAll(calcFields);

			// Convert to string
			String sqlFields = Common.getSqlFields(sqlFieldsSet);

			String sqlStmt = "SELECT " + sqlFields + " FROM " + sqlTable
					+ " ORDER BY " + Common.getSqlFields(summaryFields);
			gui.setStatus("Sending query to database...");

			try {
				sqlQuery = dbConn.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				rs = sqlQuery.executeQuery(sqlStmt);
			} catch (Exception e) {
				gui.showError("Unable to fetch data for " + rsName, e);
				// Enable table selection listner again
				gui.enableInteraction(reqSet);
				return;
			}

			gui.logPrintln("Populating table for " + rsName
					+ " (watch the progress bar)");
			gui.setStatus("Populating table...");

			// Fetch all data from table and put it in the req data set
			gui.resetProgressBar();

			//gui.logPrintln(sqlFieldsSet.size() + " cols");

			// Now include result fields
			sqlFieldsSet.addAll(APPCONSTANTS.RESULT_FIELDS);

			// Initialize data model
			reqData.resetData(nRows, sqlFieldsSet.size());
			reqData.setColumnNames(sqlFieldsSet.toArray());
			reqSummaryDataModel.setHeaderMap((ArrayList) summaryFields);
			reqData.setReqKeyColumn(0); // Req Key is first column in data set
			// Update the column types (FIELD_XXX)
			reqData.setColumnTypes(summaryFields, APPCONSTANTS.FIELD_SUMMARY);
			reqData.setColumnTypes(detailFields, APPCONSTANTS.FIELD_DETAIL);
			reqData.setColumnTypes(calcFields, APPCONSTANTS.FIELD_CALC);
			reqData.setColumnTypes(APPCONSTANTS.RESULT_FIELDS,
					APPCONSTANTS.FIELD_RESULT);

			reqSummaryDataModel.fireTableStructureChanged();

			// And fetch all data from recordset
			try {
				for (int row = 0; rs.next(); row++) {
					for (int col = 0; col < sqlFieldsSet.size()
							- APPCONSTANTS.RESULT_FIELDS.size(); col++) {
						reqData.setValueAt(rs.getObject(col + 1), row, col);

						// Update progressbar on each 0.2 percent step
						if (((int) (200 * (row + 1) / nRows)) != ((int) (200 * row / nRows))) {
							gui.setProgressBar((int) (100 * (row + 1) / nRows));
						}
					} // for col
				} // for row
				rs.close();
			} catch (Exception e) {
				gui.showError("Unable to update table for " + rsName, e);
				gui.enableInteraction(reqSet);
				return;
			}

			//
			// Fetch links
			//
			String linksTable = Application.getProjectPref(
					APPCONSTANTS.PREF_SECTION_LINKS,
					APPCONSTANTS.PREF_DB_TABLE, APPCONSTANTS.PREF_DEF_DB_TABLE);
			//			String linksToFetch = Application.getPref(PREF_SECTION_LINKS,
			// PREF_DB_FIELD_LINK_KEY[Common.otherReqSet(reqSet)],
			// PREF_DEF_DB_FIELD_LINKS_KEY[Common.otherReqSet(reqSet)] );
			String linkKeyField = Application.getProjectPref(
					APPCONSTANTS.PREF_SECTION_LINKS,
					APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[reqSet],
					APPCONSTANTS.PREF_DEF_DB_FIELD_LINKS_KEY[reqSet]);

			sqlStmt = "SELECT [" + linkKeyField + "], COUNT([" + linkKeyField
					+ "]) AS Count" + " FROM [" + linksTable + "]"
					+ " GROUP BY [" + linkKeyField + "]";

			// gui.logPrintln("For reqSet " + reqSet + ":" + sqlStmt);

			try {
				rs = sqlQuery.executeQuery(sqlStmt);
			} catch (SQLException e) {
				gui
						.showError(
								"<Fatal error> Unable to execute database query for fetching links:",
								e);
				gui.enableInteraction(reqSet);
				return; // Fatal error. Cannot proceed.
			}
			HashMap linked = new HashMap();
			try {
				while (rs.next()) {
					linked.put(new Integer(rs.getInt(1)), new Integer(rs
							.getInt(2)));
				}
			} catch (Exception e) {
				gui
						.showError(
								"<Fatal error> Unable to fetch similar requirements",
								e);
			}
			// Close the recordset
			try {
				rs.close();
			} catch (Exception e) {
				gui.showError("<Fatal error> Unable to close recordset:", e);
			}
			// Set the links
			reqSummaryDataModel.setReqLinks(-1, Common.otherReqSet(reqSet),
					linked);

			reqSummaryDataModel.fireTableDataChanged();

			// Set column headers for detail view
			DefaultTableModel reqDetailRowHeaderData = Application
					.getRsDetailRowHeaderDataModel(reqSet);
			// Set number of rows
			reqDetailRowHeaderData.setRowCount(detailFields.size());
			// Set the row names
			String field;
			int row = 0;
			for (Iterator it = detailFields.iterator(); it.hasNext();) {
				field = (String) it.next();
				reqDetailRowHeaderData.setValueAt(field, row++, 0);
			}
			// Set the row header default width
			gui.adjustTableRowHeaderWidth(reqSet);

			// Set the data model
			ReqDataModel reqDetailDataModel = Application
					.getRsDetailDataModel(reqSet);
			reqDetailDataModel.setColumnNames(new String[] { "Details" });
			reqDetailDataModel.setRowMap(0);
			reqDetailDataModel.setHeaderMap((ArrayList) detailFields);

			reqDetailDataModel.fireTableStructureChanged();
			reqDetailDataModel.fireTableDataChanged();

			gui.adjustTableRowHeight(reqSet, "Description"); // TODO: Generic

			// Use textarea as cell renderer so that words wrap
			gui.setCellRenderer(reqSet);

			// Synchronize row heights with the row headers heights
			gui.synchRsRowHeights(reqSet);

			gui.enableInteraction(reqSet);

			gui.logPrintln(rsName + " fetched in " + Common.timeDiff(startTime)
					+ ".");
		}

	}

}

