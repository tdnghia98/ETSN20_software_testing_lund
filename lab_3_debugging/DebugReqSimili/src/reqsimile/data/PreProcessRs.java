package reqsimile.data;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.*;

import reqsimile.*;
import reqsimile.gui.*;
import reqsimile.simcalc.*;

/**
 * Preprocesses a requirement according to these steps:
 * <ol>
 * <li>Tokenization, stemming and stop word removal of each requirement.
 * <li>Updates the table that holds all distinct tokens (and token ids).
 * <li>Updates the frequency table that keeps the represenation of which tokens
 * occur in the requirements, and their corresponding frequency.
 * </ol>
 * The preprocessing takes less than 0.03 seconds per requirement. (based on
 * average calculated from prerprocessing 11723 requirements of various sizes on
 * a 1.7 GHz Pentium II processor)
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class PreProcessRs extends Thread {

    GUI_Main gui;

    /**
     * Constructor
     */
    public PreProcessRs() {
        this.gui = Application.getGUI();
    }

    /**
     * Runs the thread which preprocesses all requirements in the designated
     * set.
     */
    public void run() {
        ReqData reqData;
        Connection dbConn; // Database connection
        Statement sqlQuery; // The SQL statement

        String calcContent; // Content to preprocess
        String preProcessed; // Preprocessed content
        Tokens tokens = new Tokens(); // Tokens used in the requirements
        TokenFrequencies tokenFreqs = new TokenFrequencies(); // Token frequency

        StringReader stringReader = new StringReader("");
        String nextToken;
        int reqKey, key, col, nRows;

        long startTime;

        // TODO: Create preprocessing tables in optional database
		dbConn = Application.getRsDataSet(APPCONSTANTS.A).getDbConnection();
        try {
            sqlQuery = dbConn.createStatement();
        } catch (SQLException e) {
            gui.showError("Could not create sql statement for emptying tokens table", e);
            return; // Fatal error, exit!
        }

        // DROP tokens TABLE
        try {
            sqlQuery.executeUpdate("DROP TABLE " + Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB_GENERAL, APPCONSTANTS.PREF_DB_TOKENS_TABLE, APPCONSTANTS.PREF_DEF_DB_TOKENS_TABLE ));
        } catch (Exception e) {
            gui.showError("Tokens table not emptied", e);
            // This error is non-fatal; if the table cannot be emptied
            // More serious errors will be catched in the next block
        }
        
        // CREATE tokens TABLE
        try {
            // TODO: Change MEMO to a generic type
            //       (LONGVARCHAR does not seem work with MSACCESS)
            sqlQuery
                    .executeUpdate("CREATE TABLE "
                            + Application.getProjectPref(APPCONSTANTS.PREF_SECTION_DB_GENERAL, APPCONSTANTS.PREF_DB_TOKENS_TABLE, APPCONSTANTS.PREF_DEF_DB_TOKENS_TABLE )
                            + " (TokenKey INTEGER PRIMARY KEY, Token CHAR(200));");
        } catch (Exception e) {
            gui.showError("Could not create tokens table", e);
            return; // Fatal error, cannot proceed
        }
        
        for (int reqSet = APPCONSTANTS.A; reqSet <= APPCONSTANTS.B; reqSet++) {
            reqData = Application.getRsDataSet(reqSet);

            String preProcessTable = Application.getProjectPref(
                    APPCONSTANTS.PREF_SECTION_DB_GENERAL,
                    APPCONSTANTS.PREF_DB_PREPROCESS_TABLE,
                    APPCONSTANTS.PREF_DEF_DB_PREPROCESS_TABLE[reqSet]);
            String tokenFreqTable = Application.getProjectPref(
                    APPCONSTANTS.PREF_SECTION_DB[reqSet],
                    APPCONSTANTS.PREF_DB_TOKENWEIGHTS_TABLE,
                    APPCONSTANTS.PREF_DEF_DB_TOKENWEIGHTS_TABLE[reqSet]);

            String rsName = Application.getProjectPref(
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

            // Start timer
            startTime = System.currentTimeMillis();

            PreProcessor preProcessor = new PreProcessor(stringReader);

            // Try to disable auto-commit
            try {
                dbConn.setAutoCommit(false);
            } catch (SQLException e) {
                gui.logPrintln("Warning: Flat transactions not enabled for " + rsName);
                // Do not care if auto-commit cannot be turned off
                // If the driver does not support it, bad luck; performance
                // degration
            }

            try {
                sqlQuery = dbConn.createStatement();
            } catch (SQLException e) {
                gui
                        .showError("Could not create sql statement for "
                                + rsName, e);
                gui.enableInteraction(reqSet);
                return; // Fatal error, exit!
            }

            // Fetch previous tokens (if any)
            if (!tokens.fetch()) {
                gui.logPrintln("Error trying to fetch tokens for " + rsName
                        + ". Aborting.");
                gui.enableInteraction(reqSet);
                return; // Only fatal errors are returned by fetch()
            }
            tokens.clearAddedCount();

            // DROP old preprocess TABLE
            try {
                sqlQuery.executeUpdate("DROP TABLE " + preProcessTable);
            } catch (Exception e) {
                gui.showError("Warning: Preprocessing table for "
                        + rsName + " not deleted", e);
                // This error is non-fatal; if the table does not exist -> fine!
                // More serious errors will be catched in the next block
            }

            // CREATE preprocess TABLE
            try {
                // TODO: Change MEMO to a generic type
                //       (LONGVARCHAR does not seem work with MSACCESS)
                sqlQuery
                        .executeUpdate("CREATE TABLE "
                                + preProcessTable
                                + " (ReqKey INTEGER PRIMARY KEY, Tokens MEMO, SqSum DOUBLE);");
            } catch (Exception e) {
                gui.showError("Could not create preprocessing table for "
                        + rsName, e);
                gui.enableInteraction(reqSet);
                return; // Fatal error, cannot proceed
            }

            // DROP old tokenFreq TABLE
            try {
                // TODO: Enable setting of this preference from GUI
                sqlQuery.executeUpdate("DROP TABLE " + tokenFreqTable);
            } catch (Exception e) {
                gui.showError("Warning: Token frequency table for "
                        + rsName + " not deleted", e);
                // This error is non-fatal; if the table does not exist -> fine!
                // More serious errors will be catched in the next block
            }

            // CREATE preprocess TABLE
            try {
                // TODO: Enable setting of this preference from GUI
                // TODO: Allow any data type for Key
                // TODO: Change MEMO to a generic type
                //       (LONGVARCHAR does not seem work with MSACCESS)
                sqlQuery
                        .executeUpdate("CREATE TABLE "
                                + tokenFreqTable
                                + " (ReqKey INTEGER, TokenKey INTEGER, Weight DOUBLE, PRIMARY KEY ( ReqKey, TokenKey) );");
            } catch (Exception e) {
                // TODO: Check type of error and handle that
                gui.showError("Could not create token frequency table for "
                        + rsName, e);
                gui.enableInteraction(reqSet);
                return; // Fatal error, cannot proceed
            }

            // Prepare INSERT statement for preprocessed requirements
            PreparedStatement sqlInsert = null;
            try {
                sqlInsert = dbConn.prepareStatement("INSERT INTO "
                        + preProcessTable + " VALUES (?,?,?);");
            } catch (SQLException e) {
                gui.showError(
                        "Could not prepare INSERT statement for preprocessing table for "
                                + rsName, e);
                gui.enableInteraction(reqSet);
                return; // Fatal error, cannot proceed
            }

            // Prepare INSERT statement for token frequency
            PreparedStatement sqlInsertFreq = null;
            try {
                sqlInsertFreq = dbConn.prepareStatement("INSERT INTO "
                        + tokenFreqTable + " VALUES (?,?,?);");
            } catch (SQLException e) {
                gui.showError(
                        "Could not prepare INSERT statement for token frequency table for "
                                + rsName, e);
                gui.enableInteraction(reqSet);
                return; // Fatal error, cannot proceed
            }

            // Iterate over all requirements and create batch insert
            col = reqData.getColumnPos(Application.getProjectPref(
                    APPCONSTANTS.PREF_SECTION_DB[reqSet],
                    APPCONSTANTS.PREF_DB_FIELD_KEY,
                    APPCONSTANTS.PREF_DEF_DB_FIELD_KEY));
            Collection calcFields = Common.toCollection(Application
                    .getProjectPref(APPCONSTANTS.PREF_SECTION_DB[reqSet],
                            APPCONSTANTS.PREF_DB_FIELDS_CALC,
                            APPCONSTANTS.PREF_DEF_DB_FIELDS_CALC));
            nRows = reqData.getRowCount();
            try {
                gui.logPrintln("Processing " + rsName + "...");

                for (int row = 0; row < nRows; row++) {
                    // Fetch data in calculation fields
                    calcContent = reqData.getValuesAt(row, calcFields);

                    // Create input stream from string
                    //stringReader = new StringReader(calcContent);
                    // Preprocess the content
                    preProcessor.yyreset(new StringReader(calcContent));
                    // Get tokens
                    preProcessed = "";
                    try {
                        tokenFreqs.clear();
                        while ((nextToken = preProcessor.yylex()) != null) {
                            preProcessed += nextToken + " ";
                            // Add token to token table
                            key = tokens.add(nextToken);
                            tokenFreqs.increment(key);
                        }
                    } catch (IOException e) {
                        gui.showError("Could not preprocess requirement " + row
                                + " in " + rsName, e);
                        gui.enableInteraction(reqSet);
                        return; // Fatal error, cannot proceed
                    }

                    // Get req id
                    reqKey = Integer.parseInt(reqData.getValueAt(row, col)
                            .toString());

                    // Create batch for token frequency table
                    Integer tokenKey, tokenFreq;
                    for (Iterator it = tokenFreqs.iterator(); it.hasNext();) {
                        tokenKey = (Integer) it.next();
                        sqlInsertFreq.setInt(1, reqKey);
                        sqlInsertFreq.setInt(2, tokenKey.intValue());
                        sqlInsertFreq.setDouble(3, tokenFreqs
                                .getLogWeight(tokenKey));
                        sqlInsertFreq.addBatch();
                    }
                    sqlInsertFreq.executeBatch();

                    // Add SQL statement to batch
                    // TODO: Handle any data type for id. Now assumes Long
                    sqlInsert.setInt(1, reqKey);
                    sqlInsert.setString(2, preProcessed);
                    sqlInsert.setDouble(3, Math.sqrt(Cosine.sqSumLog(Arrays
                            .asList(preProcessed.split(" ")))));
                    sqlInsert.addBatch();

                    // Update progressbar on each 0.2 percent step
                    if (((int) (200 * (row + 1) / nRows)) != ((int) (200 * row / nRows)))
                        gui.setProgressBar((int) (100 * (row + 1) / nRows));

                    // Execute the sql batch each 1000 records
                    if (row % 1000 == 0) {
                        sqlInsert.executeBatch();
                    }
                }
                gui.logPrintln(tokens.numberAdded() + " new tokens found in "
                        + rsName);
                sqlInsert.executeBatch();

                // Close SQL connections
                sqlInsert.close();
                sqlInsertFreq.close();
                sqlQuery.close();

            } catch (SQLException e) {
                // TODO: handle exception
                gui.showError("Error preprocessing " + rsName, e);
                gui.enableInteraction(reqSet);
                return; // Fatal error, cannot proceed
            }

            // Try to enable auto-commit
            try {
                dbConn.setAutoCommit(true);
            } catch (SQLException e) {
                gui.showError("Unable to disable transaction for " + rsName, e);
                // Do not care if auto-commit cannot be turned on
                // If the driver does not support it, bad luck; performance
                // degration
            }

            // Store tokens
            gui.logPrintln("Storing tokens for " + rsName);
            tokens.store();

            gui.enableInteraction(reqSet);

            gui.logPrintln(rsName + " done in " + Common.timeDiff(startTime)
                    + ".");
        }

    }

}

