package reqsimile.simcalc;

import reqsimile.APPCONSTANTS;
import reqsimile.Application;
import reqsimile.Common;
import reqsimile.gui.*;
import reqsimile.data.*;

import java.sql.*;
import java.util.*;

import javax.swing.table.DefaultTableModel;

/**
 * FetchSimilar is a thread that fetches all similar requirements 
 * to a specified requirements (e.g. a requirements selected in a table)
 * <p>
 * This thread is run every time a new list of requirements shall be fetched. Thus,
 * for performance reasons this should be kept simple and fast.
 * <p>
 * The current solution depends on a certain way of preprocessing requirements.
 * See the PreProcessRs class on how the preprocessing is made. 
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 * @see PreProcessRs
 */
public class FetchSimilar extends Thread {
	
	/** The main gui. */ 
	private GUI_Main gui;
	
	/** The ids for the two requirements sets to compare */
	private int reqSet, compareToReqSet;
	
	/** The data sources for the two requirements sets to compare */
	private ReqData reqData, compareToReqData;
	
	/** The data models for the requirements sets to compare and for the result set */
	private ReqDataModel reqDataModel, compareToReqDataModel, compareToReqDetailDataModel, reqResultDataModel, reqResultDetailDataModel;
	
	/** Names of the tables holding the preprocessed information which are used in queries */
	private String preProcessTable, tokenWeightsTable, tokensTable, otherPreProcessTable, otherTokenWeightsTable;
	
	/** Name of the table holding the links */
	private String linksTable;
	
	/** The names of the fields in the link table. 
	 * <p> 
	 * If R is the selected requirements to which all similar requirements are
	 * to be fetched, then:
	 * <ul>
	 * <li>LinksToFetch is the name of the field that holds the requirements keys
	 * of the requirements that may be linked to R
	 * <li>LinksToMatch is the name of the field that holds the requirements key
	 * of R.
	 * </ul>
	 * The two fields depends on in which requirements set R resides.
	 */
	private String linksToFetch, linksToMatch;
	
	/** Database connetion for the data set from which similar requirements shall be
	 * fetched.
	 */
	private Connection dbConn;
	
	/** Sql query. Reused. */
	private Statement sqlQuery;
	
	/** The unique key for the selected requirements (for which similar requirements
	 * shall be fetched)
	 */
	private int selectedReqKey;
	
	/**
	 * Constructor.
	 * 
	 * @param reqSet	The id of the requirement set containing the requirement
	 * 					for which similar requirements shall be fetched.  
	 */
	public FetchSimilar(int reqSet) {
		this.gui = Application.getGUI();
		this.initialize(reqSet);
	}
	
	/**
	 * Initializes variables to reflect current choices and
	 * project preferences.
	 */
	public void initialize(int reqSet) {
		this.reqSet = reqSet;
		this.compareToReqSet = Common.otherReqSet(reqSet);

		this.reqData = Application.getRsDataSet(reqSet);
		this.reqDataModel = Application.getRsSummaryDataModel(reqSet);
		
		this.reqResultDataModel = Application.getRsSummaryDataModel(APPCONSTANTS.S);
		this.reqResultDetailDataModel = Application.getRsDetailDataModel(APPCONSTANTS.S);
		
		this.compareToReqData = Application.getRsDataSet(compareToReqSet);
		this.compareToReqDataModel = Application.getRsSummaryDataModel(compareToReqSet);
		this.compareToReqDetailDataModel = Application.getRsDetailDataModel(compareToReqSet);
		
       	this.dbConn = this.compareToReqData.getDbConnection();
		this.preProcessTable = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB_GENERAL, APPCONSTANTS.PREF_DB_PREPROCESS_TABLE, APPCONSTANTS.PREF_DEF_DB_PREPROCESS_TABLE[reqSet] );
		this.tokenWeightsTable = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB[reqSet], APPCONSTANTS.PREF_DB_TOKENWEIGHTS_TABLE, APPCONSTANTS.PREF_DEF_DB_TOKENWEIGHTS_TABLE[reqSet] );
		this.otherPreProcessTable = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB_GENERAL, APPCONSTANTS.PREF_DB_PREPROCESS_TABLE, APPCONSTANTS.PREF_DEF_DB_PREPROCESS_TABLE[Common.otherReqSet(reqSet)] );
		this.otherTokenWeightsTable = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB[Common.otherReqSet(reqSet)], APPCONSTANTS.PREF_DB_TOKENWEIGHTS_TABLE, APPCONSTANTS.PREF_DEF_DB_TOKENWEIGHTS_TABLE[Common.otherReqSet(reqSet)] );

		this.linksTable = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_LINKS, APPCONSTANTS.PREF_DB_TABLE, APPCONSTANTS.PREF_DEF_DB_TABLE );
		this.linksToFetch = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_LINKS, APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[Common.otherReqSet(reqSet)], APPCONSTANTS.PREF_DEF_DB_FIELD_LINKS_KEY[Common.otherReqSet(reqSet)] );
		this.linksToMatch = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_LINKS, APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[reqSet], APPCONSTANTS.PREF_DEF_DB_FIELD_LINKS_KEY[reqSet] );
		
		this.tokensTable = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB_GENERAL, APPCONSTANTS.PREF_DB_TOKENS_TABLE, APPCONSTANTS.PREF_DEF_DB_TOKENS_TABLE );
		// Create SQL statement
		try {
			sqlQuery = dbConn.createStatement();
		} catch (SQLException e) {
			gui.showError("Unable to create SQL statemet for fetching similar requirements:", e);
		}
		this.selectedReqKey = gui.getReqKeyForSelectedRow(reqSet);
	}
    
	public void run() {
		ResultSet rs;
		String selectedReq = "";
		double sqSum = 0;
		int[] similarReqs = new int[this.compareToReqData.getRowCount()];	// Holds req id's
		double similarReqSqSum, similarReqProdSum;
		int row, index, colSimValue, colButton;
		boolean changeStructure = false, changeDetailStructure = false;
		Integer reqKey;
		
		if (dbConn == null) {
			gui.logPrintln("No connection established!");
			return;	// Quit thread if there is no connection available
		}

		gui.disableInteraction(reqSet);

		long startTime = System.currentTimeMillis();
		gui.logPrintln("Fetching similar requirements");

		String sqlSimilar;

		Collection searchTerms = gui.getSearchTerms();

		
		if (searchTerms.size() > 0) {
			// There are additional search terms to be considered in the ranking

		    String preProcessedReq = "";
			String sqlMakeReq;
			String altTokenWeightsTable = "rsTokenWeightsSearchTerms";

			// DROP old preprocess TABLE
			try {
				sqlQuery.executeUpdate("DROP TABLE " + altTokenWeightsTable);
			} catch (Exception e) {
				gui.showError("<Non-fatal error> Could not delete preprocessing table "
						+ tokenWeightsTable, e);
			}
		    
		    // Fetch information about the selected requirement
			// As a precaution, check if a requirement has been selected.
		    // If terms are submitted directly after application launch, there is no
		    // requirement selected
		    if ((selectedReqKey >= 0) && (!gui.useSearchTermsOnly())) {	
				// Create select statement for fetching the preprocessed requirement
			    // that is marked in the table.
				String sqlPreProcessReq = 
						"SELECT Tokens, SqSum" +
						" FROM " + preProcessTable +
						" WHERE ReqKey=" + selectedReqKey;

				// Now fetch the preprocessed requirement
				try {
					rs = sqlQuery.executeQuery(sqlPreProcessReq);
					rs.next(); // Go to first row
					preProcessedReq = selectedReq = rs.getString(1);	// Get preproceed req
					sqSum = rs.getDouble(2);	// Get preprocessed req's sqSum
					rs.close();
				} catch (SQLException e) {
					gui.showError("Unable to fetch preprocessed requirement", e);
					gui.enableInteraction(reqSet);
					return; // Fatal error. Cannot proceed.
				}
				
				// Create a table with the information about the selected requirement
				sqlMakeReq = "SELECT *"+
				" INTO " + altTokenWeightsTable +
			    " FROM [" + tokenWeightsTable + "]" +
				" WHERE ReqKey = " + selectedReqKey;
		    } else {
		        // Make an empty table
				sqlMakeReq = "CREATE TABLE "
					+ altTokenWeightsTable + 
					" (ReqKey INTEGER, TokenKey INTEGER, Weight DOUBLE);";
		    }

		    // Create token weights table for the "new" tokens (selected requirement + search terms)
			try {
				sqlQuery.executeUpdate(sqlMakeReq);
			} catch (SQLException e) {
				gui.showError("Unable to create new token weights table", e);
				gui.enableInteraction(reqSet);
				return; // Fatal error. Cannot proceed.
			}

			// Insert search terms into the token weights table
			String termList = "";
			for(Iterator it = searchTerms.iterator(); it.hasNext(); ) {
				termList += "'" + (String)it.next() + "',"; 
			}

			if (termList != "") {
				termList = termList.substring(0, termList.length()-1);
				
				sqlMakeReq = "INSERT INTO " +
					altTokenWeightsTable + " (ReqKey, TokenKey, Weight)" +
					" SELECT " + selectedReqKey + " As ReqKey, TokenKey, 1 AS Weight" +
				    " FROM [" + tokensTable + "]" +
					" WHERE Token In (" + termList + ")";
				try {
					sqlQuery.executeUpdate(sqlMakeReq);
				} catch (SQLException e) {
					gui.showError("Unable to insert search terms into new token weights table", e);
					gui.enableInteraction(reqSet);
					return; // Fatal error. Cannot proceed.
				}
			}
			
		    // Add the sqSum for the search terms
			sqSum += Cosine.sqSumLog(searchTerms);
			
			// From now on, use the new produced token weights table
			tokenWeightsTable = altTokenWeightsTable;

		} else {		
			// Create select statement for fetching the square sum for the preprocessed requirement
			String sqlPreProcessReq = 
					"SELECT Tokens, SqSum" +
					" FROM " + preProcessTable +
					" WHERE ReqKey=" + selectedReqKey;
			
			// Now fetch the square sum for the preprocessed requirement
			try {
				rs = sqlQuery.executeQuery(sqlPreProcessReq);
				rs.next(); // Go to first row
				//selectedReq = rs.getString(1);	// Get preproceed req
				sqSum = rs.getDouble(2);
				rs.close();
			} catch (SQLException e) {
				gui.showError("Unable to fetch preprocessed requirement", e);
				gui.enableInteraction(reqSet);
				return; // Fatal error. Cannot proceed.
			}
		}

		// Create select statement for fetching similar requirements and the squared sum of 
		//		SELECT rsTokenWeightsB.ReqKey, Min(rsPreProcessedB.SqSum) AS MinOfSqSum, Sum([rsTokenWeightsA].[Weight]*[rsTokenWeightsB].[Weight]) AS ProdSum
		//		FROM (rsTokenWeightsB INNER JOIN rsTokenWeightsA ON rsTokenWeightsB.TokenKey = rsTokenWeightsA.TokenKey)
		//		LEFT JOIN rsPreProcessedB ON rsTokenWeightsB.ReqKey = rsPreProcessedB.Key
		//		WHERE (((rsTokenWeightsA.ReqKey)=3))
		//		GROUP BY rsTokenWeightsB.ReqKey;
		sqlSimilar = 
				"SELECT " + 
					"[" + otherTokenWeightsTable + "].ReqKey," +
					"MIN([" + otherPreProcessTable + "].SqSum) AS SqSum," +
					"SUM([" + tokenWeightsTable + "].Weight * [" + otherTokenWeightsTable + "].Weight) AS ProdSum" +
				" FROM " +
					"([" + otherTokenWeightsTable + "]"+
					" INNER JOIN [" + tokenWeightsTable + "]"+
					" ON [" + otherTokenWeightsTable + "].TokenKey=[" + tokenWeightsTable + "].TokenKey)" +  
				" LEFT JOIN [" + otherPreProcessTable + "]" +
				" ON [" + otherTokenWeightsTable + "].ReqKey=[" + otherPreProcessTable + "].ReqKey" +
				" WHERE [" + tokenWeightsTable + "].ReqKey=" + selectedReqKey +
				" GROUP BY [" + otherTokenWeightsTable + "].ReqKey"+
				" ORDER BY SUM([" + tokenWeightsTable + "].Weight * [" + otherTokenWeightsTable + "].Weight)/MIN([" + otherPreProcessTable + "].SqSum) DESC;";

		// Now let the database do the job and execute that complex statement
		try {
			//gui.logPrintln(sqlSimilar);
			rs = sqlQuery.executeQuery(sqlSimilar);
		} catch (SQLException e) {
			gui.showError("<Fatal error> Unable to execute database query for fetching similar requirements ("+sqlSimilar+"):", e);
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}
		
		// Fetch the requirements and calculate similarity
		colSimValue = compareToReqData.getColumnPos(APPCONSTANTS.RESULT_FIELD_SIMVALUE);
		colButton = compareToReqData.getColumnPos(APPCONSTANTS.RESULT_FIELD_LINK_BUTTON);

		index = 0;
		try {
			while (rs.next()) {
				// TODO: Accept any type for requirements key
				//       Now assumes integer, which may cause errors
				//       Alternatively: Do not simply accept any other
				//       data type for the req key (which is actually a better
				//       idea as it makes the preprocessing stage and similarity
				//       calculation far quicker).
				row = compareToReqData.getRowForReqKey(rs.getInt(1));
				compareToReqData.setValueAt(
						new Double(
						        rs.getDouble(3) /
						        (sqSum * rs.getDouble(2))
						),
						row,
						colSimValue
						);
				compareToReqData.setValueAt(
						"Link",
						row,
						colButton);
				// Save the row
				similarReqs[index++] = row;
			}
		} catch (Exception e) {
			gui.showError("<Fatal error> Unable to fetch similar requirements", e);
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}
		// Close the recordset
		try {
			rs.close();
		} catch (Exception e) {
			gui.showError("<Fatal error> Unable to close recordset:", e);			
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}

		// Reduce the id array size 
		int[] trimmedMap = new int[index];
		
		
		// Make sure the result table uses data from the correct data set
		if ( (this.compareToReqData != this.reqResultDataModel.getDataSet()) ) {
			this.reqResultDataModel.setDataSet(this.compareToReqData);
			this.reqResultDetailDataModel.setDataSet(this.compareToReqData);
			changeStructure = true;
		}

		if (reqResultDetailDataModel.getHeaderMap().length == 0) {
			changeDetailStructure = true;
		}

		System.arraycopy(similarReqs, 0, trimmedMap, 0, index);
		// And set the filter
		reqResultDataModel.setRowMap( trimmedMap );
		
		reqResultDataModel.showZeroSimilarity();

		// Get summary fields to show
		Collection showFields = Common.toCollection(Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB[Common.otherReqSet(reqSet)], APPCONSTANTS.PREF_DB_FIELDS_SUMMARY, APPCONSTANTS.PREF_DEF_DB_FIELDS_SUMMARY));
		// Add results fields
		showFields.addAll(APPCONSTANTS.RESULT_FIELDS);
		// Update result set table model
		reqResultDataModel.setHeaderMap((ArrayList)showFields);
		
		//
		// Fetch all requirements that are linked to the currently selected/marked requirement 
		//
		String sqlLinks = 
			"SELECT " + 
				"[" + linksToFetch + "]" +
			" FROM " +
				"[" + linksTable + "]" +
			" WHERE [" + linksToMatch + "] = " + selectedReqKey;
		//gui.logPrintln("Linked to marked: " + sqlLinks);
		try {
			rs = sqlQuery.executeQuery(sqlLinks);
		} catch (SQLException e) {
			gui.showError("<Fatal error> Unable to execute database query for fetching links (" + sqlLinks + "):", e);
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}
		HashMap linked = new HashMap();
		try {
			while (rs.next()) {
				linked.put(new Integer(rs.getInt(1)), new Integer(1));
			}
		} catch (Exception e) {
			gui.showError("<Fatal error> Unable to fetch similar requirements", e);
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}
		// Close the recordset
		try {
			rs.close();
		} catch (Exception e) {
			gui.showError("<Fatal error> Unable to close recordset:", e);			
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}
		
		//
		// Fetch all requirements that are linked to any BUT the currently selected/marked requirement 
		//
		sqlLinks = 
			"SELECT DISTINCT " + 
				"[" + linksToFetch + "]" +
			" FROM " +
				"[" + linksTable + "]" +
			" WHERE NOT [" + linksToMatch + "] = " + selectedReqKey;
		//gui.logPrintln("Linked to other: " + sqlLinks);
		try {
			rs = sqlQuery.executeQuery(sqlLinks);
		} catch (SQLException e) {
			gui.showError("<Fatal error> Unable to execute database query for fetching links:", e);
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}
		try {
			while (rs.next()) {
				reqKey = new Integer(rs.getInt(1));				
				if (linked.containsKey(reqKey)) {
					linked.put(reqKey, new Integer(3));	// Binary 11					
				} else
					linked.put(reqKey, new Integer(2));	// Binary 10
			}
		} catch (Exception e) {
			gui.showError("<Fatal error> Unable to fetch similar requirements", e);
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}
		// Close the recordset
		try {
			rs.close();
		} catch (Exception e) {
			gui.showError("<Fatal error> Unable to close recordset:", e);			
			gui.enableInteraction(reqSet);
			return; // Fatal error. Cannot proceed.
		}		
		// Set the links
		reqResultDataModel.setReqLinks(selectedReqKey , Common.otherReqSet(reqSet), linked);
				
//		if (trimmedMap.length == 0) {
//			reqResultDetailDataModel.setColumnNames(new String[]{"Details"});
//			reqResultDetailDataModel.setHeaderMap(new int[0]);
//			reqResultDetailDataModel.setRowMap(-2);
//		} else {
			reqResultDetailDataModel.setColumnNames(new String[]{"Details"});
			reqResultDetailDataModel.setRowMap(similarReqs[0]);
			reqResultDetailDataModel.setHeaderMap(compareToReqDetailDataModel.getHeaderMap());
//		}
		
		// Set the detail model to reflect the same view as
		// the detail model for the source requirements set
	
		// Set row headers for detail view
		DefaultTableModel reqResultDetailRowHeaderDataModel = Application.getRsDetailRowHeaderDataModel(APPCONSTANTS.S);
		DefaultTableModel reqDetailRowHeaderDataModel = Application.getRsDetailRowHeaderDataModel(compareToReqSet);
		// Set number of rows
		reqResultDetailRowHeaderDataModel.setRowCount(reqDetailRowHeaderDataModel.getRowCount());
		// Set the row names
		for(row = 0; row < reqDetailRowHeaderDataModel.getRowCount(); row++) {
			reqResultDetailRowHeaderDataModel.setValueAt(
					reqDetailRowHeaderDataModel.getValueAt(row, 0)
					, row, 0);
		}
		// Set the row header default widths
		gui.adjustTableRowHeaderWidth(APPCONSTANTS.S);		

		if (changeStructure) {

			reqResultDataModel.fireTableStructureChanged();
			
			// Set renderer for link buttons column
			gui.setTableButtonRendererAndEditor(APPCONSTANTS.S, showFields.size()-1);		
		}
		if (changeDetailStructure) {		
			reqResultDetailDataModel.fireTableStructureChanged();
			gui.adjustTableRowHeight(APPCONSTANTS.S, "Description");
		}
		
		reqDataModel.setMarkedLink(gui.getRsSelectedRow(reqSet));
		compareToReqDataModel.setMarkedLink(-1);
		
		reqResultDataModel.fireTableDataChanged();		
		reqResultDetailDataModel.fireTableDataChanged();		

//		if (trimmedMap.length > 0 ) {
			// Use textarea as cell renderer so that words wrap

			gui.setCellRenderer(APPCONSTANTS.S);
		
			// Synchronize row heights with the row headers heights
			gui.synchRsRowHeights(APPCONSTANTS.S);

			// Update listeners for the result table
			gui.updateRsListeners(APPCONSTANTS.S);
//		}

		gui.enableInteraction(reqSet);

		gui.logPrintln("Done in approximately " + Common.timeDiff(startTime) + "." );

	}

}



