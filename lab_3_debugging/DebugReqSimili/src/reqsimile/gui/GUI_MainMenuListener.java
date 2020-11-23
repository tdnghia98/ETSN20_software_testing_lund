package reqsimile.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import reqsimile.APPCONSTANTS;
import reqsimile.Application;
import reqsimile.Common;
import reqsimile.data.PopulateRs;
import reqsimile.data.PreProcessRs;

/**
 * GUI_MainMenuListener handles the actions to be taken upon selection of menu
 * items in the application menu.
 * 
 * @author Johan Natt och Dag
 */
public class GUI_MainMenuListener implements ActionListener {

    JMenuItem menuItem;

    int menuItemId;

    /**
     *  
     */
    public GUI_MainMenuListener() {
        super();

        // TODO Auto-generated constructor stub
    }

    public void actionPerformed(ActionEvent e) {

        menuItemId = Integer.parseInt(((JMenuItem) e.getSource()).getName());
        GUI_Main gui = Application.getGUI();

        switch (menuItemId) {
        case APPCONSTANTS.FILE_EXIT:
            Application.cleanUpForExit();
            System.exit(0);
            break;
        case APPCONSTANTS.FILE_OPEN:
            Application.openProject();
        	break;
        case APPCONSTANTS.DATA_SOURCES:
            // Show config
            GUI_SettingsReqSources s = new GUI_SettingsReqSources();
            s.setVisible(true);
            break;
        case APPCONSTANTS.DATA_FETCH:
            for (int reqSet = APPCONSTANTS.A; reqSet <= APPCONSTANTS.B; reqSet++) {
                // Check if at least one field for each view is selected
                if (Common.toCollection(
                        Application.getProjectPref(
                                APPCONSTANTS.PREF_SECTION_DB[reqSet],
                                APPCONSTANTS.PREF_DB_FIELDS_SUMMARY,
                                APPCONSTANTS.PREF_DEF_DB_FIELDS_SUMMARY))
                        .size() == 0) {
                    // No summary field(s) selected
                    gui
                            .logPrintln("There are no fields selected for the summary view.");
                    return; // Quit
                }
                if (Common.toCollection(
                        Application.getProjectPref(
                                APPCONSTANTS.PREF_SECTION_DB[reqSet],
                                APPCONSTANTS.PREF_DB_FIELDS_DETAIL,
                                APPCONSTANTS.PREF_DEF_DB_FIELDS_DETAIL)).size() == 0) {
                    // No field in list selected
                    gui
                            .logPrintln("There are no fields selected for the detail view.");
                    return; // Quit
                }
                if (Common.toCollection(
                        Application.getProjectPref(
                                APPCONSTANTS.PREF_SECTION_DB[reqSet],
                                APPCONSTANTS.PREF_DB_FIELDS_CALC,
                                APPCONSTANTS.PREF_DEF_DB_FIELDS_CALC)).size() == 0) {
                    // No calc field(s) selected
                    gui
                            .logPrintln("There are no fields selected for calculating similarity.");
                    return; // Quit
                }
            }
            // Start threads to populate summary tables
            new PopulateRs().start();
            break;
        case APPCONSTANTS.DATA_PREPROCESS:
            // Check that requirements are in memory
            // TODO: Change so that requirements are fetched directly from DB
            // and not required to be read on beforehand into memory
            if (Application.getRsDataSet(APPCONSTANTS.A).getRowCount() == 0) {
                gui
                        .logPrintln("You must first fetch requirements from database via the File menu.");
                return; // Fatal. Cannot proceed.
            }
            new PreProcessRs().start();
            break;
        case APPCONSTANTS.VIEW_A:
            gui.displayReqSet(APPCONSTANTS.A);
            break;
        case APPCONSTANTS.VIEW_B:
            gui.displayReqSet(APPCONSTANTS.B);
            break;
        case APPCONSTANTS.VIEW_LOG:
            gui.displayLog();
            break;
        case APPCONSTANTS.VIEW_RESULT:
            gui.displayResult();
            break;
        case APPCONSTANTS.REPORT_TOKENCOUNT:
        	gui.logPrintln("Not implemented yet!");
        	break;
        case APPCONSTANTS.REPORT_SUMMARY:
        	gui.logPrintln("Total number: " + Application.getRsDataSet(APPCONSTANTS.A).sumAllRequirements() +
        				   "\n\nContents: " + Application.getRsDataSet(APPCONSTANTS.A).concatenateAllReqs());
        	break;
        case APPCONSTANTS.HELP_ABOUT:
            String heading = APPCONSTANTS.APP_NAME + " v"
                    + APPCONSTANTS.APP_VERSION;
            String about = heading + "\n\n" + "by Johan Natt och Dag\n"
                    + "johan.nattochdag@telecom.lth.se\n\n"
                    + "http://reqsimile.sourceforge.net";
            JOptionPane.showMessageDialog(null, about, heading,
                    JOptionPane.INFORMATION_MESSAGE);
            break;
        }
    }

}