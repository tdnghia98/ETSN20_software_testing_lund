package reqsimile;

import java.awt.event.*;
import java.io.*;
import java.sql.*;

import javax.swing.*;
import javax.swing.table.*;

import reqsimile.gui.*;
import reqsimile.data.*;

/**
 * Application is the starting point of the application, which creates the
 * central classes and provides information about these (such as the application
 * itself, the gui, database connections, etc.).
 * <p>
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class Application {

    /** The main application. */
    private static Application m_theApp;

    /** The application main Swing frame ({@link GUI_Main}). */
    private static GUI_Main gui;

    /** The INI file for generic application preferences ({@link INIFile}). */
    private static INIFile appPrefs;

    /** The INI file for current project preferences ({@link INIFile}) */
    private static INIFile projectPrefs;

    /** Connections made to external databases ({@link DBConnections}). */
    private static DBConnections dbConnections;

    /** The data read from the database(s) ({@link ReqData}). */
    private static ReqData reqData[];

    /** The data models for the summary view table ({@link ReqDataModel}). */
    private static ReqDataModel reqSummaryDataModel[];

    /** The data models for the detail view table ({@link ReqDataModel}). */
    private static ReqDataModel reqDetailDataModel[];

    /**
     * The data models for the row headers of the detail view table (
     * {@link DefaultTableModel}).
     */
    private static DefaultTableModel reqDetailRowHeaderData[];

    /** The connection used to fetch links */
    private static Connection linkDataConn;

    /**
     * Application constructor. A number of actions are taken:
     * <ul>
     * <li>Initializes data sources and data models for the table views.
     * <li>Tries to opens latest project and read its settings
     * <li>Tries to connect to databases based on project preferences
     * <li>Initializes the gui
     * <li>Updates the GUI based on read project preferences
     * <li>Fetches requirements
     * </ul>
     */
    public Application() {
        super();

        // Open last project
        openLastProject();

        // Create window and set some preferences
        gui = new GUI_Main("ReqSimile " + APPCONSTANTS.APP_VERSION);
        gui.centreWindow();
        gui.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Initialize data sources
        initializeDataSources();

        // Update components to reflect project preferences
        updateGUIConfigSettings();

        //add a listener so we quit on close
        gui.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cleanUpForExit();
                System.exit(0);
            }
        });

        // Show the window
        gui.setVisible(true);

        // Fetch requirements
        new PopulateRs().start();

    }

    /**
     * Opens the project that was active when ReqSimile was shut down. If no
     * such last project is available, no project is loaded. If the last
     * project's preferences are not available, no project is loaded.
     * 
     * @return Nothing
     */
    private void openLastProject() {

        // Try to open the generic INI file if it exists
        appPrefs = new INIFile(APPCONSTANTS.INI_FILE);

        if (appPrefs.isLoaded()) {

            // Get the project file
            String projectFileName = appPrefs.getStringProperty(
                    APPCONSTANTS.PREF_SECTION_PROJECT,
                    APPCONSTANTS.PREF_PROJECT);

            if (projectFileName != null) {
                openProject(projectFileName);
            }
        }
        if (projectPrefs == null) {
            projectPrefs = new INIFile(appPrefs.getStringProperty(
                    APPCONSTANTS.PREF_SECTION_PROJECT,
                    APPCONSTANTS.PREF_DEF_PROJECT));
        }
    }

    /**
     * Opens a project and reads the preferences for that project. Also sets the
     * application preferences to remember the latest project loaded.
     * 
     * @param projectFileName
     *            The project file name
     * @return Nothing
     */
    private static void openProject(String projectFileName) {
        // Open the project preferences
        projectPrefs = new INIFile(projectFileName);

        // Close database connections
        if (dbConnections != null) {
            dbConnections.closeAll();
	        // Initialize data sources
	        initializeDataSources();
	        // Update components to reflect project preferences
            updateGUIConfigSettings();
	        // Fetch requirements
	        new PopulateRs().start();
        }

        // Remember currently loaded project
        appPrefs.setStringProperty(APPCONSTANTS.PREF_SECTION_PROJECT,
                APPCONSTANTS.PREF_PROJECT, projectFileName);
    }

    /**
     * Initializes data sources according to the currently loaded project, or by
     * using the defualt application preferences.
     */
    private static void initializeDataSources() {
        // Initialize variables
        dbConnections = new DBConnections();
        reqData = new ReqData[APPCONSTANTS.N];
        reqSummaryDataModel = new ReqDataModel[APPCONSTANTS.N + APPCONSTANTS.NR];
        reqDetailDataModel = new ReqDataModel[APPCONSTANTS.N + APPCONSTANTS.NR];
        reqDetailRowHeaderData = new DefaultTableModel[APPCONSTANTS.N
                + APPCONSTANTS.NR];

        Connection newConn;

        // Create data sets
        reqData[APPCONSTANTS.A] = new ReqData();
        reqData[APPCONSTANTS.B] = new ReqData();

        // Create data models for requirements sets
        reqSummaryDataModel[APPCONSTANTS.A] = new ReqDataModel(projectPrefs
                .getStringProperty(
                        APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                        APPCONSTANTS.PREF_DB_NAME,
                        APPCONSTANTS.PREF_DEF_DB_NAME[APPCONSTANTS.A]),
                reqData[APPCONSTANTS.A], APPCONSTANTS.FIELD_SUMMARY);
        reqSummaryDataModel[APPCONSTANTS.B] = new ReqDataModel(projectPrefs
                .getStringProperty(
                        APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                        APPCONSTANTS.PREF_DB_NAME,
                        APPCONSTANTS.PREF_DEF_DB_NAME[APPCONSTANTS.B]),
                reqData[APPCONSTANTS.B], APPCONSTANTS.FIELD_SUMMARY);
        reqSummaryDataModel[APPCONSTANTS.S] = new ReqDataModel(
                "Candidate requirements", new ReqData(),
                APPCONSTANTS.FIELD_SUMMARY + APPCONSTANTS.FIELD_RESULT);
        reqDetailDataModel[APPCONSTANTS.A] = new ReqDataModel(projectPrefs
                .getStringProperty(
                        APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                        APPCONSTANTS.PREF_DB_NAME,
                        APPCONSTANTS.PREF_DEF_DB_NAME[APPCONSTANTS.A]),
                reqData[APPCONSTANTS.A], APPCONSTANTS.FIELD_DETAIL);
        reqDetailDataModel[APPCONSTANTS.B] = new ReqDataModel(projectPrefs
                .getStringProperty(
                        APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                        APPCONSTANTS.PREF_DB_NAME,
                        APPCONSTANTS.PREF_DEF_DB_NAME[APPCONSTANTS.B]),
                reqData[APPCONSTANTS.B], APPCONSTANTS.FIELD_DETAIL);
        reqDetailDataModel[APPCONSTANTS.S] = new ReqDataModel(
                "Candidate requirements", new ReqData(),
                APPCONSTANTS.FIELD_DETAIL);

        // Create data models for row headers in detail view
        reqDetailRowHeaderData[APPCONSTANTS.A] = new DefaultTableModel(0, 1);
        reqDetailRowHeaderData[APPCONSTANTS.A]
                .setColumnIdentifiers(new String[] { "" });
        reqDetailRowHeaderData[APPCONSTANTS.B] = new DefaultTableModel(0, 1);
        reqDetailRowHeaderData[APPCONSTANTS.B]
                .setColumnIdentifiers(new String[] { "" });
        reqDetailRowHeaderData[APPCONSTANTS.S] = new DefaultTableModel(0, 1);
        reqDetailRowHeaderData[APPCONSTANTS.S]
                .setColumnIdentifiers(new String[] { "" });

        // Create database connections for the requirements data
        newConn = dbConnections
                .newConnection(projectPrefs.getStringProperty(
                        APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                        APPCONSTANTS.PREF_DB_DRIVER_URL,
                        APPCONSTANTS.PREF_DEF_DB_DRIVER_URL), projectPrefs
                        .getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                                APPCONSTANTS.PREF_DB_DRIVER_CLASSNAME,
                                APPCONSTANTS.PREF_DEF_DB_DRIVER_CLASSNAME),
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                                APPCONSTANTS.PREF_DB_URL,
                                APPCONSTANTS.PREF_DEF_DB_URL),
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                                APPCONSTANTS.PREF_DB_USERNAME,
                                APPCONSTANTS.PREF_DEF_DB_USERNAME),
                        // TODO: Fix a secure way to store passwords
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                                APPCONSTANTS.PREF_DB_PASSWORD,
                                APPCONSTANTS.PREF_DEF_DB_PASSWORD));
        if (newConn != null) {
            reqData[APPCONSTANTS.A].setDbConnection(newConn);
        }

        newConn = dbConnections
                .newConnection(projectPrefs.getStringProperty(
                        APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                        APPCONSTANTS.PREF_DB_DRIVER_URL,
                        APPCONSTANTS.PREF_DEF_DB_DRIVER_URL), projectPrefs
                        .getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                                APPCONSTANTS.PREF_DB_DRIVER_CLASSNAME,
                                APPCONSTANTS.PREF_DEF_DB_DRIVER_CLASSNAME),
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                                APPCONSTANTS.PREF_DB_URL,
                                APPCONSTANTS.PREF_DEF_DB_URL),
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                                APPCONSTANTS.PREF_DB_USERNAME,
                                APPCONSTANTS.PREF_DEF_DB_USERNAME),
                        // TODO: Fix a secure way to store passwords
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                                APPCONSTANTS.PREF_DB_PASSWORD,
                                APPCONSTANTS.PREF_DEF_DB_PASSWORD));
        if (newConn != null) {
            reqData[APPCONSTANTS.B].setDbConnection(newConn);
        }

        // Create database connections for the links table
        newConn = dbConnections
                .newConnection(projectPrefs.getStringProperty(
                        APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_DRIVER_URL,
                        APPCONSTANTS.PREF_DEF_DB_DRIVER_URL), projectPrefs
                        .getStringProperty(APPCONSTANTS.PREF_SECTION_LINKS,
                                APPCONSTANTS.PREF_DB_DRIVER_CLASSNAME,
                                APPCONSTANTS.PREF_DEF_DB_DRIVER_CLASSNAME),
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_LINKS,
                                APPCONSTANTS.PREF_DB_URL,
                                APPCONSTANTS.PREF_DEF_DB_URL), projectPrefs
                                .getStringProperty(
                                        APPCONSTANTS.PREF_SECTION_LINKS,
                                        APPCONSTANTS.PREF_DB_USERNAME,
                                        APPCONSTANTS.PREF_DEF_DB_USERNAME),
                        // TODO: Fix a secure way to store passwords
                        projectPrefs.getStringProperty(
                                APPCONSTANTS.PREF_SECTION_LINKS,
                                APPCONSTANTS.PREF_DB_PASSWORD,
                                APPCONSTANTS.PREF_DEF_DB_PASSWORD));
        if (newConn != null) {
            linkDataConn = newConn;
        }
    }

    /**
     * Updates the GUI configuration components to reflect settings
     */
    private static void updateGUIConfigSettings() {
        // Assign data sources to tables
        gui.setRsSummaryDataSource(APPCONSTANTS.A,
                reqSummaryDataModel[APPCONSTANTS.A]);
        gui.setRsSummaryDataSource(APPCONSTANTS.B,
                reqSummaryDataModel[APPCONSTANTS.B]);
        gui.setRsSummaryDataSource(APPCONSTANTS.S,
                reqSummaryDataModel[APPCONSTANTS.S]);
        gui.setRsDetailDataSource(APPCONSTANTS.A,
                reqDetailDataModel[APPCONSTANTS.A]);
        gui.setRsDetailDataSource(APPCONSTANTS.B,
                reqDetailDataModel[APPCONSTANTS.B]);
        gui.setRsDetailDataSource(APPCONSTANTS.S,
                reqDetailDataModel[APPCONSTANTS.S]);
        gui.setRsDetailRowHeaderDataSource(APPCONSTANTS.A,
                reqDetailRowHeaderData[APPCONSTANTS.A]);
        gui.setRsDetailRowHeaderDataSource(APPCONSTANTS.B,
                reqDetailRowHeaderData[APPCONSTANTS.B]);
        gui.setRsDetailRowHeaderDataSource(APPCONSTANTS.S,
                reqDetailRowHeaderData[APPCONSTANTS.S]);

        reqDetailDataModel[APPCONSTANTS.A].setRowMap(0);
        reqDetailDataModel[APPCONSTANTS.B].setRowMap(0);
        reqDetailDataModel[APPCONSTANTS.S].setRowMap(0);

        // Update Requirement set's names
        gui.setRsName(APPCONSTANTS.A, reqSummaryDataModel[APPCONSTANTS.A]
                .getName());
        gui.setRsName(APPCONSTANTS.B, reqSummaryDataModel[APPCONSTANTS.B]
                .getName());

    }

    /**
     * Starting point for the application
     * 
     * @param args
     *            Application arguments
     */
    public static void main(String[] args) {
        // Create the application
        m_theApp = new Application();
    }

    /**
     * Returns the Application object
     * 
     * @return The Application object
     */
    public static Application theApp() {
        return m_theApp;
    }

    /**
     * Returns the main window class object
     * 
     * @return The GUI_Main object
     */
    public static GUI_Main getGUI() {
        return gui;
    }

    /**
     * Return the object holding all database connections
     * 
     * @return The connections object
     */
    public static DBConnections getDbConnections() {
        return dbConnections;
    }

    /**
     * Return the connection for the database holding the links table.
     * 
     * @return A Connection object for the database holding the links table.
     */
    public static Connection getLinksConnection() {
        return linkDataConn;
    }

    /**
     * Sets the connection for the database holding the links table
     * 
     * @param dbConn
     *            The Connection to the database holding the links table
     */
    public static void setLinksConnection(Connection dbConn) {
        linkDataConn = dbConn;
    }

    /**
     * Returns the requirements data set for a specific requirements set (
     * {@link ReqData}).
     * 
     * @param reqSet
     *            The requirements set id
     * @return The requirements data object.
     */
    public static ReqData getRsDataSet(int reqSet) {
        return reqData[reqSet];
    }

    /**
     * Returns the data model for the summary view of a specific requirements
     * set ({@link ReqDataModel}).
     * 
     * @param reqSet
     *            The requirements set id
     * @return The requirements data model object
     */
    public static ReqDataModel getRsSummaryDataModel(int reqSet) {
        return reqSummaryDataModel[reqSet];
    }

    /**
     * Returns the data model for the detail view of a specific requirements set (
     * {@link ReqDataModel}).
     * 
     * @param reqSet
     *            The requirements set id
     * @return The requirements data model object
     */
    public static ReqDataModel getRsDetailDataModel(int reqSet) {
        return reqDetailDataModel[reqSet];
    }

    /**
     * Returns the data model for the detail row headers of a specific
     * requirements set
     * 
     * @param reqSet
     *            The requirements set id
     * @return The data model object
     */
    public static DefaultTableModel getRsDetailRowHeaderDataModel(int reqSet) {
        return reqDetailRowHeaderData[reqSet];
    }

    /**
     * Shows the file selection dialog to select a project to open. If a file is
     * chosen, the project is opened.
     */
    public static void openProject() {
        saveProjectPrefs();
        JFileChooser projFileChooser = new JFileChooser(File.separator
                + APPCONSTANTS.PROJECT_FILE_EXT);
        projFileChooser.addChoosableFileFilter(new ProjectFileFilter());
        try {
            // Create a File object containing the canonical path of the
            // desired directory
            File f = new File(new File(appPrefs.getStringProperty(
                    APPCONSTANTS.PREF_SECTION_PROJECT,
                    APPCONSTANTS.PREF_PROJECT)).getCanonicalPath());
            // Set the current directory
            projFileChooser.setCurrentDirectory(f);
        } catch (IOException e) {
            // Don't care
        }
        // Show open dialog; this method does not return until the dialog is
        // closed
        int result = projFileChooser.showOpenDialog(gui);
        if (result == JFileChooser.APPROVE_OPTION) {
            File projFile = projFileChooser.getSelectedFile();
            if (projFile.canRead()) {
                // Check if file is a ReqSimile project file (simple check)
                try {
                    FileReader projFileReader = new FileReader(projFile);
                    
                    // Read first line
                    char[] file_id = new char[APPCONSTANTS.PROJECT_FILE_ID.length()];
                    projFileReader.read(file_id, 0, APPCONSTANTS.PROJECT_FILE_ID.length());
                    projFileReader.close();
                    
                    if(APPCONSTANTS.PROJECT_FILE_ID.equals(new String(file_id))) {
                        openProject(projFile.getAbsolutePath());
                    } else {
                        JOptionPane.showMessageDialog(null, projFile.getName() + " does not seem to be a ReqSimile project file", "Project open warning", JOptionPane.WARNING_MESSAGE );
                    }
                } catch (Exception e) {
    				gui.showError("Could not read file ", e);
                }
            }
        }
    }

    /**
     * Returns a preferences property of the currently loaded project.
     * 
     * @param section
     *            The section in which the property resides in the INI file
     * @param property
     *            The name of the property
     * @param defaultValue
     *            The default value if the property cannot be fetched
     * @return A String containing the property value
     */
    public static String getProjectPref(String section, String property,
            String defaultValue) {
        return projectPrefs.getStringProperty(section, property, defaultValue);
    }

    /**
     * Sets a string preferences property of the currently loaded project.
     * 
     * @param section
     *            The section in which the property resides in the INI files
     * @param property
     *            The name of the property
     * @param value
     *            The value to set
     */
    public static void setProjectPref(String section, String property,
            String value) {
        projectPrefs.setStringProperty(section, property, value);
    }

    /**
     * Saves the current projects settings to disk.
     */
    public static void saveProjectPrefs() {
        projectPrefs.save();
    }

    /**
     * This method closes connections and saves application preferences
     */
    public static void cleanUpForExit() {
        // Close database connections
        dbConnections.closeAll();

        // Save application settings
        appPrefs.save();
    }

}