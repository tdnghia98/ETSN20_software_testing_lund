package reqsimile.gui;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.sql.*;

import reqsimile.*;
import reqsimile.data.*;
import reqsimile.simcalc.*;

/**
 * GUI_Eventhandler handles all events reuslting from interaction with the gui
 * by the user. This includes all buttons, table selections but not menu
 * actions. Menu actions are handled by {@link GUI_MainMenuListener}.
 * <p>
 * This class contains all classes derived from the associated actions or
 * listeners.
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class GUI_EventHandler {

    /** The main window frame */
    GUI_Main gui;

    /**
     * Any started thread is saved here to check its state upon subsequent
     * events.
     */
    protected Thread similarityThread = null;

    /**
     * Constructor. The gui object must be supplied here since this object is
     * created from the gui and the Application object does not have the
     * reference yet (otherwise Application.getGUI() could have worked).
     * 
     * @param gui
     *            The application main gui
     */
    public GUI_EventHandler(GUI_Main gui) {
        this.gui = gui;
    }

    /**
     * ConnectAction represents the Connect button in the requirements sets tabs
     * in the settings dialog.
     * 
     * @author Johan Natt och Dag
     */
    public class ConnectAction extends AbstractAction {

        /** Backward serialization compatibility */
        private static final long serialVersionUID = 1L;

        /** The requirements set that this button is associated to */
        int reqSet;

        /** The dialog frame */
        GUI_SettingsReqSources settingsGUI;

        /**
         * Constructor.
         * 
         * @param reqSet
         *            Requirements set id to associate to this button
         * @param settingsGUI
         *            The setting dialog frame
         */
        public ConnectAction(int reqSet, GUI_SettingsReqSources settingsGUI) {
            this.reqSet = reqSet;
            this.settingsGUI = settingsGUI;
            putValue(NAME, "Connect");
            //putValue(SMALL_ICON, new ImageIcon("images/up.gif"));
            putValue(SHORT_DESCRIPTION,
                    "Connect to database and retrieve list of tables");
            //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
        }

        /**
         * Releases any previous database connection and tries to establish a
         * new connection based on the provided database paramaters in the
         * settings dialog tab.
         */
        public void actionPerformed(ActionEvent evt) {
            // Release old connection
            Application.getRsSummaryDataModel(reqSet).getDataSet()
                    .releaseDbConnection();
            // Create new connection
            Connection newConn = Application.getDbConnections().newConnection(
                    settingsGUI.getDriverURL(reqSet),
                    settingsGUI.getDriverClassName(reqSet),
                    settingsGUI.getDbURL(reqSet),
                    settingsGUI.getDbUser(reqSet),
                    settingsGUI.getDbPassword(reqSet));
            if (newConn == null) {
                gui.logPrintln("Could not get connect to database.");
                return;
            }
            // Assign new connection to requirement set's data model
            Application.getRsSummaryDataModel(reqSet).getDataSet()
                    .setDbConnection(newConn);

            settingsGUI.updateTables(reqSet);
        }
    }

    /**
     * ConnectLinksAction represents the connect button in the links tab in the
     * settings dialog.
     * 
     * @author Johan Natt och Dag
     */
    public class ConnectLinksAction extends AbstractAction {

        /** Backward serialization compatibility */
        private static final long serialVersionUID = 1L;

        /** The dialog frame */
        GUI_SettingsReqSources settingsGUI;

        /**
         * Constructor.
         * 
         * @param settingsGUI
         *            The settings dialog frame
         */
        public ConnectLinksAction(GUI_SettingsReqSources settingsGUI) {

            this.settingsGUI = settingsGUI;
            putValue(NAME, "Connect");
            //putValue(SMALL_ICON, new ImageIcon("images/up.gif"));
            putValue(SHORT_DESCRIPTION,
                    "Connect to database and retrieve list of tables");
            //putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
        }

        /**
         * Releases any previous database connection and tries to establish a
         * new connection based on the provided database paramaters in the
         * settings dialog's link tab.
         */
        public void actionPerformed(ActionEvent evt) {
            // Release old connection
            Application.getDbConnections().close(
                    Application.getLinksConnection());
            // Create new connection
            Connection newConn = Application.getDbConnections().newConnection(
                    settingsGUI.getLinksDriverURL(),
                    settingsGUI.getLinksDriverClassName(),
                    settingsGUI.getLinksDbURL(), settingsGUI.getLinksDbUser(),
                    settingsGUI.getLinksDbPassword());
            if (newConn == null) {
                gui.logPrintln("Could not get connect to database.");
                return;
            }
            // Assign new connection to requirement set's data model
            Application.setLinksConnection(newConn);

            settingsGUI.updateLinksTables();
        }
    }

    /**
     * ResetAction represents the Reset buttons in the settings dialog.
     * 
     * @author Johan Natt och Dag
     */
    public class ResetAction extends AbstractAction {

        /** Backward serialization compatibility */
        private static final long serialVersionUID = 1L;

        /** The requirements set that this button is associated to */
        int reqSet;

        /**
         * Constructor.
         * 
         * @param reqSet
         *            Requirements set id to associate to this button
         */
        public ResetAction(int reqSet) {
            this.reqSet = reqSet;
            putValue(NAME, "Reset");
            putValue(SHORT_DESCRIPTION,
                    "Reset all fields to current settings (discard all configuration changes)");
        }

        /**
         * Currently does nothing.
         */
        public void actionPerformed(ActionEvent evt) {
            gui.logPrint("Pressed Reset button.");
        }
    }

    /**
     * SaveSettingsAction takes actions when the Save button in the settings
     * dialog is pressed.
     * 
     * @author Johan Natt och Dag
     */
    public class SaveSettingsAction extends AbstractAction {

        /** Backward serialization compatibility */
        private static final long serialVersionUID = 1L;

        /** The settings dialog frame */
        GUI_SettingsReqSources settingsGUI;

        /**
         * Constructor.
         * 
         * @param settingsGUI
         *            The settings dialog frame
         */
        public SaveSettingsAction(GUI_SettingsReqSources settingsGUI) {
            this.settingsGUI = settingsGUI;
            putValue(NAME, "Save");
            putValue(SHORT_DESCRIPTION,
                    "Use and save the settings\nand close this window");
        }

        /**
         * Sets all the current project preferences as provided in the settings
         * dialog.
         */
        public void actionPerformed(ActionEvent evt) {
            for (int rs = APPCONSTANTS.A; rs <= APPCONSTANTS.B; rs++) {
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_NAME, settingsGUI.getName(rs));
                gui.setRsName(rs, settingsGUI.getName(rs));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_DRIVER_URL, settingsGUI
                                .getDriverURL(rs));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_DRIVER_CLASSNAME, settingsGUI
                                .getDriverClassName(rs));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_URL, settingsGUI.getDbURL(rs));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_USERNAME, settingsGUI
                                .getDbUser(rs));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_PASSWORD, new String(settingsGUI
                                .getDbPassword(rs)));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_TABLE, settingsGUI
                                .getTableName(rs));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_FIELD_KEY, settingsGUI
                                .getReqKeyField(rs));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_FIELDS_SUMMARY, settingsGUI
                                .getSummaryFields(rs).toString());
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_FIELDS_DETAIL, settingsGUI
                                .getDetailFields(rs).toString());
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_DB[rs],
                        APPCONSTANTS.PREF_DB_FIELDS_CALC, settingsGUI
                                .getCalcFields(rs).toString());

                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_DRIVER_URL, settingsGUI
                                .getLinksDriverURL());
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_DRIVER_CLASSNAME, settingsGUI
                                .getLinksDriverClassName());
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_URL, settingsGUI.getLinksDbURL());
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_USERNAME, settingsGUI
                                .getLinksDbUser());
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_PASSWORD, new String(settingsGUI
                                .getLinksDbPassword()));
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_TABLE, settingsGUI
                                .getLinksTableName());

                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[APPCONSTANTS.A],
                        settingsGUI.getLinkKeyFieldA());
                Application.setProjectPref(APPCONSTANTS.PREF_SECTION_LINKS,
                        APPCONSTANTS.PREF_DB_FIELD_LINK_KEY[APPCONSTANTS.B],
                        settingsGUI.getLinkKeyFieldB());

                Application.saveProjectPrefs();
            }
        }
    }

    /**
     * FetchFieldsForTableAction represents the action when a table is selected
     * in the combo box in the settings dialog.
     * 
     * @author Johan Natt och Dag
     */
    public class FetchFieldsForTableAction extends AbstractAction {

        /** Backward serialization compatibility */
        private static final long serialVersionUID = 1L;

        /** The setting dialog frame */
        GUI_SettingsReqSources gui_settings;

        /** The requirements set associated to this action */
        int reqSet;

        /** The combo box to fill with field names */
        JComboBox comboBox;

        /** The table model to fill with field names */
        InputFieldsTableModel fieldsTable;

        /**
         * Constructor.
         * 
         * @param gui
         *            The settings dialog frame
         * @param reqSet
         *            The requirements set associated to this action
         * @param comboBox
         *            The combo box to fill with field names
         * @param fieldsTable
         *            The table to fill with field names
         */
        public FetchFieldsForTableAction(GUI_SettingsReqSources gui,
                int reqSet, JComboBox comboBox,
                InputFieldsTableModel fieldsTable) {
            this.gui_settings = gui;
            this.reqSet = reqSet;
            this.comboBox = comboBox;
            this.fieldsTable = fieldsTable;
        }

        /**
         * Fetches all the fields available in the selected table and put those
         * fields in the combo box and in the table for selecting which fields
         * to show and use for calculation.
         */
        public void actionPerformed(ActionEvent evt) {

            gui.logPrintln("Fetching fields for " + reqSet);

            Connection dbConn = Application.getRsDataSet(reqSet)
                    .getDbConnection();
            if (dbConn == null)
                return;

            if (((JComboBox) evt.getSource()).getItemCount() == 0)
                return;

            // Fetch fields for table
            try {
                DatabaseMetaData dmd = dbConn.getMetaData();
                ResultSet rsFields = dmd.getColumns(dbConn.getCatalog(), null,
                        ((JComboBox) evt.getSource()).getSelectedItem()
                                .toString(), "%");

                gui_settings.suspendTableComboBox(reqSet);
                // Clear Field lists
                comboBox.removeAllItems();
                fieldsTable.resetTable(0);
                gui_settings.activateTableComboBox(reqSet);

                String fieldName;
                int row = 0;
                while (rsFields.next()) {
                    fieldName = rsFields.getString("COLUMN_NAME"); /*
                                                                    * + " (" +
                                                                    * rsFields.getString("COLUMN_SIZE") +
                                                                    * ")"
                                                                    */
                    fieldsTable.setValueAt(fieldName, row++, 0);
                    comboBox.addItem(fieldName);
                }
                rsFields.close();

            } catch (Exception e) {
                gui.logPrint("Could not fetch field list: ");
                gui.logPrintln(e.getMessage());
            }

        }
    }

    /**
     * FetchFieldsForLinksTableAction represents the action when a table is
     * selected in the combo box in the settings dialog's links tab.
     * 
     * @author Johan Natt och Dag
     */
    public class FetchFieldsForLinksTableAction extends AbstractAction {

        /** Backward serialization compatibility */
        private static final long serialVersionUID = 1L;

        /** Combo boxes which shall be filled with the fetched fields */
        JComboBox comboBoxA, comboBoxB;

        /** The setting dialog frame */
        GUI_SettingsReqSources gui_settings;

        /**
         * Constructor.
         * 
         * @param gui
         *            The settings dialog frame
         * @param comboBoxA
         *            Combobox 1 to fill with field names
         * @param comboBoxB
         *            Combobox 2 to fill with field names
         */
        public FetchFieldsForLinksTableAction(GUI_SettingsReqSources gui,
                JComboBox comboBoxA, JComboBox comboBoxB) {
            this.gui_settings = gui;
            this.comboBoxA = comboBoxA;
            this.comboBoxB = comboBoxB;
        }

        /**
         * Fetches all the fields available in the selected table and put those
         * fields in the combo boxes.
         */
        public void actionPerformed(ActionEvent evt) {

            gui.logPrintln("Fetching links fields.");

            Connection dbConn = Application.getLinksConnection();
            if (dbConn == null)
                return;

            if (((JComboBox) evt.getSource()).getItemCount() == 0)
                return;

            // Fetch fields for table
            try {
                DatabaseMetaData dmd = dbConn.getMetaData();
                ResultSet rsFields = dmd.getColumns(dbConn.getCatalog(), null,
                        ((JComboBox) evt.getSource()).getSelectedItem()
                                .toString(), "%");

                gui_settings.suspendLinksTableComboBox();
                // Clear Field lists
                comboBoxA.removeAllItems();
                comboBoxB.removeAllItems();
                gui_settings.activateLinksTableComboBox();

                String fieldName;
                int row = 0;
                while (rsFields.next()) {
                    fieldName = rsFields.getString("COLUMN_NAME"); /*
                                                                    * + " (" +
                                                                    * rsFields.getString("COLUMN_SIZE") +
                                                                    * ")"
                                                                    */
                    comboBoxA.addItem(fieldName);
                    comboBoxB.addItem(fieldName);
                }
                rsFields.close();

            } catch (Exception e) {
                gui.logPrint("Could not fetch links field list: ");
                gui.logPrintln(e.getMessage());
            }

        }
    }

    /**
     * RsSelectionListener represents the action of selecting a row in a table.
     * 
     * @author Johan Natt och Dag
     */
    public class RsSelectionListener implements ListSelectionListener {

        /** The requirements set in which a requirement has been selected */
        int reqSet;

        /** The data models for the summary and detail views */
        ReqDataModel reqDataModel, reqDetailDataModel;

        /**
         * Constructor.
         * 
         * @param reqSet
         *            The requirements set associated to the table in which a
         *            selection has been made.
         */
        RsSelectionListener(int reqSet) {
            this.reqSet = reqSet;
        }

        /**
         * Updates the detail view with the details of the newly selected
         * requirement. Starts the thread that fetches all similar requirements.
         * This last action could be moved to the
         * {@link GUI_EventHandler.RsDoubleClickListener}, depending on the
         * preferred behavior.
         */
        public void valueChanged(ListSelectionEvent e) {
            // We only care about row changes so we do not have to check
            // whether there's been a row change or a column change
            this.reqDataModel = Application.getRsSummaryDataModel(reqSet);
            this.reqDetailDataModel = Application.getRsDetailDataModel(reqSet);

            int first = e.getFirstIndex();
            int last = e.getLastIndex();

            // Update detail view
            if ((!e.getValueIsAdjusting()) && (first != last)
                    && (gui.getRsSelectedRow(reqSet) != -1)) {

                reqDetailDataModel.setRowMap(this.reqDataModel.getRowMap(gui
                        .getRsSelectedRow(reqSet)));
                reqDetailDataModel.fireTableRowsUpdated(0, reqDetailDataModel
                        .getRowCount());

                if (reqSet != APPCONSTANTS.S) {
                    // Update the marked requirement in the summary view
                    Application.getRsSummaryDataModel(reqSet).setMarkedLink(
                            gui.getRsSelectedRow(reqSet));
                    // Remove any marked requirement in the table with the
                    // requirements from the other requirements set
                    Application.getRsSummaryDataModel(
                            Common.otherReqSet(reqSet)).setMarkedLink(-1);

                    // Check if thread is already running
                    if (similarityThread != null)
                        while (similarityThread.isAlive())
                            Common.sleep(100);
                    // Start thread to fetch similar requirements
                    similarityThread = new FetchSimilar(reqSet);
                    similarityThread.start();
                }

            }
        }
    }

    /**
     * RsDoubleClickListener represents the action of double clicking a row in a
     * table.
     * 
     * @author Johan Natt och Dag
     */
    public class RsDoubleClickListener extends MouseAdapter {

        /**
         * The requirements set associated to the table in which a double click
         * was made
         */
        private int reqSet;

        /**
         * Constructor.
         * 
         * @param reqSet
         *            The requirements set associated to the table in which a
         *            double click was made
         */
        public RsDoubleClickListener(int reqSet) {
            this.reqSet = reqSet;
        }

        /**
         * Checks if double-click really was made in table and starts thread for
         * fetching similar requirements
         */
        public void mouseReleased(MouseEvent e) {
            // Check for double click
            if (e.getClickCount() == 2) {

                // Check if thread is already running,
                // otherwise wait for it to finish
                if (similarityThread != null)
                    while (similarityThread.isAlive())
                        Common.sleep(100);

                // Start thread to fetch similar requirements
                similarityThread = new FetchSimilar(reqSet);
                similarityThread.start();
            }
        }
    }

    /**
     * LinkAction represents the action for the Link button in the result table.
     * 
     * @author Johan Natt och Dag
     */
    public class LinkAction implements ActionListener {

        /** The data model for result summary view */
        private ReqDataModel reqDataModel;

        /** The underlying result data set */
        private ReqData reqData;

        /**
         * Constructor.
         */
        public LinkAction() {
            reqDataModel = Application.getRsSummaryDataModel(APPCONSTANTS.S);
            reqData = reqDataModel.getDataSet();
        }

        /**
         * Swaps the link status for the requirement associated to the pressed
         * Link/Unlink button.
         */
        public void actionPerformed(ActionEvent evt) {
            int row = gui.getRsSelectedRow(APPCONSTANTS.S);

            if (Application.getRsSummaryDataModel(
                    Common.otherReqSet(Application.getRsSummaryDataModel(
                            APPCONSTANTS.S).getLinksReqSet()))
                    .getMarkedLinkRow() < 0) {
                JOptionPane.showMessageDialog(null,
                        "You must first select a requirement to link to\n"
                                + "(in the other requirements set)\n"
                                + "by double-clicking a requirement\n"
                                + "in the upper requirements list");
                return;
            }
            reqDataModel.swapLink(row);
            // Update the button text
            if (reqDataModel.isLinked(row))
                ((JButton) evt.getSource()).setText("Unlink");
            else
                ((JButton) evt.getSource()).setText("Link");
            gui.stopCellEditing(APPCONSTANTS.S);
        }
    }

    /**
     * LinkButtonListener is used to synchronize the selected row in the result
     * table with the pressed Link/Unlink button.
     * 
     * @author Johan Natt och Dag
     */
    public class LinkButtonListener extends MouseAdapter {

        /**
         * Make sure that the correct row with pressed button is selected.
         * Dragging in the table may change the selected row, but keep focus on
         * the old button and therefore not update the selected row when that
         * same button is clicked again.
         */
        public void mousePressed(MouseEvent e) {
            Application.getGUI().synchSelectedRow(APPCONSTANTS.S, e);
        }

    }

    /**
     * SearchTermSubmitListener represent the action of pressing Enter in the
     * search term box above the result table.
     */
    public class SearchTermSubmitListener implements KeyListener {

        /*
         * (non-Javadoc)
         * 
         * @see KeyListener.keyTyped
         */
        public void keyTyped(KeyEvent e) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see KeyListener.keyReleased
         */
        public void keyReleased(KeyEvent e) {
        }

        /**
         * Fetches similar requirements if Enter key is pressed
         */
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ENTER) {
                // Check if thread is already running
                if (similarityThread != null)
                    while (similarityThread.isAlive())
                        Common.sleep(100);

                // Start thread to fetch similar requirements
                similarityThread = new FetchSimilar(gui.getRsForVisibleTab());
                similarityThread.start();

            }
        }
    }

    /**
     * searchTermsOnlyItemListener represents the action of changing the state
     * of the check box used to indicate wheter only the specified search terms
     * shall be used to find similar requirements or if both the selected
     * requirement and the search terms shall be used.
     */
    public class searchTermsOnlyItemListener implements ItemListener {

        /**
         * Fetches similar requirements if checkbox state is changed
         */
        public void itemStateChanged(ItemEvent arg0) {
            // Check if thread is already running
            if (similarityThread != null)
                while (similarityThread.isAlive())
                    Common.sleep(100);

            // Start thread to fetch similar requirements
            similarityThread = new FetchSimilar(gui.getRsForVisibleTab());
            similarityThread.start();
        }

    }
}

