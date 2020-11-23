package reqsimile.simcalc;

import reqsimile.*;

import java.sql.*;
import java.util.*;

/**
 * Tokens temporarily holds all the tokens/words used in the requirements sets.
 * It is used in the preprocessing step to rebuild the token table,
 * which is stored in an external database table.
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class Tokens {

	/** The tokens */
	private HashMap tokens = new HashMap();
	
	/** The next unused token id */
	private int nextUnusedKey = 1;
	
	/** The number of added tokens */
	private int addedCount = 0;
	
	public Tokens() {
	}
	
	/**
	 * Fetches tokens from the database.
	 * <p>
	 * The database currently used is the
	 * same as the one specified by requirements set A. This should be 
	 * changed to specify any optional database.
	 * 
	 * @return True on success, false otherwise
	 */
	public boolean fetch() {
		// TODO: Enable connection to another database
		// (currently using the one for data set A) 
		Connection dbConn = Application.getRsDataSet(APPCONSTANTS.A).getDbConnection();
		Statement sqlStmt;
		ResultSet rs = null;
		
		// Reset token storge
		tokens.clear();
		nextUnusedKey = 1;

		// Send query to DB for fetching tokens
		try {
			sqlStmt = dbConn.createStatement();
			rs = sqlStmt.executeQuery("SELECT * FROM " + Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB_GENERAL, APPCONSTANTS.PREF_DB_TOKENS_TABLE, APPCONSTANTS.PREF_DEF_DB_TOKENS_TABLE ) );	
		} catch (SQLException e) {
			System.err.println("Error executing SELECT query for fetching tokens");
			System.err.println(e.getMessage());
			return false; // Fatal error
		}

		// Fetch tokens
		int key; // Current token id
		try {
			for (int row = 0; rs.next(); row++) {
				key = rs.getInt("TokenKey");
				nextUnusedKey = Math.max(key+1, nextUnusedKey);
				tokens.put(rs.getString("Token"), 
				        new Integer(key));
			}
			sqlStmt.close(); // Close lock
		} catch (Exception e) {
			System.err.println("Error fetching tokens");
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false; // Probably a fatal error
		}
		
		return true;
	}
	
	/**
	 * Stores all tokens in the database. Only tokens in memory are stored and 
	 * to preserve the tokens in the database they must first be fetched ({@link #fetch}).  
	 * <p>
	 * The database currently used is the
	 * same as the one specified by requirements set A. This should be 
	 * changed to specify any optional database.
	 * 
	 */
	public void store() {
		// TODO: Enable connection to another database
		// (currently using the one for data set A) 
		Connection dbConn = Application.getRsDataSet(APPCONSTANTS.A).getDbConnection();
		Statement sqlStmt = null;
		PreparedStatement sqlPStmt = null;

		// Fetch table name
		String tokensTable = Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB_GENERAL, APPCONSTANTS.PREF_DB_TOKENS_TABLE, APPCONSTANTS.PREF_DEF_DB_TOKENS_TABLE );
		
		// Create statement
		try {
			sqlStmt = dbConn.createStatement();
		} catch (SQLException e) {
			Application.getGUI().showError("Could not create sql statement", e);
			return;	// Fatal error, exit!
		}
		
		// DROP old tokens TABLE
		try {
			sqlStmt.executeUpdate("DROP TABLE " + tokensTable);
		} catch (Exception e) {
			Application.getGUI().showError("Could not delete tokens table", e);
			// This error is non-fatal; if the table does not exist -> fine!
			// More serious errors will be catched in the next block
		}
		
		// CREATE tokens TABLE
		try {
			sqlStmt.executeUpdate("CREATE TABLE " + tokensTable + " (TokenKey INTEGER PRIMARY KEY, Token VARCHAR(200));");
		} catch (Exception e) {
			Application.getGUI().showError("Could not create tokens table", e);
			return; // Fatal error, cannot proceed
		}
		
		
		// Prepare INSERT statement
		try {
			sqlPStmt = dbConn.prepareStatement("INSERT INTO " + tokensTable + " VALUES(?,?);");
		} catch (SQLException e) {
			Application.getGUI().showError("Could not prepare INSERT statement", e);
			return; // Fatal error, cannot proceed
		}
		
		// Iterate over all tokens and insert them into database
		String token;
		if (sqlPStmt != null) {
			try {
				for (Iterator it = tokens.keySet().iterator(); it.hasNext(); ) {
					token = (String) it.next();
					sqlPStmt.setString(2, token);
					sqlPStmt.setInt(1, ((Integer) tokens.get(token)).intValue() );
					sqlPStmt.addBatch();
				}
				// Commit the insertions
				int[] num = sqlPStmt.executeBatch();
				Application.getGUI().logPrintln(num.length + " tokens in tokens table.");
				sqlPStmt.close();
			} catch (Exception e) {
				Application.getGUI().showError("Could not insert tokens into tokens table '" + tokensTable + "'", e);
				return; // Fatal error, cannot proceed
			}
		}

	}
	
	/**
	 * Adds a token to the set of tokens.
	 * 
	 * @param token
	 * @return The unique key assigned to the token  
	 */
	public int add(String token) {
		// Must check if it exists, otherwise the id will be unnecessarily incremented 
		if (!tokens.containsKey(token)) {
			// Insert new token
			tokens.put(token, new Integer(nextUnusedKey));
			addedCount++;
			return nextUnusedKey++;
		} else {
			// Fetch id
			return ((Integer) tokens.get(token)).intValue();
		}
	}
	
	/**
	 * Returns the number of tokens added (number of calls to {@link #add}). 
	 * This is primarily used to count the additional number of
	 * tokens in a preprocessed requirements set, compared to those
	 * tokens already found in previous requirements set(s).
	 * 
	 * 
	 * @return int The number of tokens added
	 */
	public int numberAdded() {
		return addedCount;
	}

	/**
	 * Clears the counter for the number of added tokens.
	 */
	public void clearAddedCount() {
		addedCount = 0;
	}
	
	
}
