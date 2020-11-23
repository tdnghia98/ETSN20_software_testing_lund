package reqsimile.gui;

import reqsimile.*;
import java.awt.HeadlessException;
import java.awt.event.*;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 * GUI_MainMenu defines the main window menu.
 * 
 * @author Johan Natt och Dag
 */
public class GUI_MainMenu extends JMenuBar {

    /** Backward serializable compatibility */
    private static final long serialVersionUID = 1L;

    /**
     * The menu content formatted accordingly:
     * 
     * Each menu is formatted as:
     * { MENUNAME, 
     *   MENUITEM, MENUDESCRIPTION, MENUID, ....
     * }
     * Menus are then comma separated.
     * MENUITEM may be "-" for menu separator, and if it is specified,
     * no MENUDESCRIPTION or MENUID should be specified.
     */
    private String[][] mainMenu = new String[][] {
            { "File", "Open Project", "Opens a project from disk",
                    String.valueOf(APPCONSTANTS.FILE_OPEN), "-", "Exit",
                    "Close down ReqSimile",
                    String.valueOf(APPCONSTANTS.FILE_EXIT) },

            { "Data", "Set sources...",
                    "Setup requirements data sources for the current project",
                    String.valueOf(APPCONSTANTS.DATA_SOURCES),
                    "Fetch requirements", "Fetch requirements from database",
                    String.valueOf(APPCONSTANTS.DATA_FETCH),
                    "Preprocess requirements",
                    "Fetch requirements from database",
                    String.valueOf(APPCONSTANTS.DATA_PREPROCESS), },

            {
                    "View",
                    Application.getProjectPref(
                            APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.A],
                            APPCONSTANTS.PREF_DB_NAME,
                            APPCONSTANTS.PREF_DEF_DB_NAME[APPCONSTANTS.A]),
                    "Show requirements set A",
                    String.valueOf(APPCONSTANTS.VIEW_A),
                    Application.getProjectPref(
                            APPCONSTANTS.PREF_SECTION_DB[APPCONSTANTS.B],
                            APPCONSTANTS.PREF_DB_NAME,
                            APPCONSTANTS.PREF_DEF_DB_NAME[APPCONSTANTS.B]),
                    "Show requirements set B",
                    String.valueOf(APPCONSTANTS.VIEW_B), "-", "Log",
                    "Show log window", String.valueOf(APPCONSTANTS.VIEW_LOG),
                    "Candidate Requirements", "Show results table",
                    String.valueOf(APPCONSTANTS.VIEW_RESULT) },

            { "Report",
            		 "Token count", "Number of tokens in the requirements sets", 
            		 String.valueOf(APPCONSTANTS.REPORT_TOKENCOUNT),
            		 "Summarize in log", "A summary is printed in log for easy extraction",
            		 String.valueOf(APPCONSTANTS.REPORT_SUMMARY)
            },

            { "Help", "About", "Show about box",
                    String.valueOf(APPCONSTANTS.HELP_ABOUT) } };

    //	 "Tokenization",
    //	 "Spell correction",
    //	 "Stemming",
    //	 "Stop word removal",
    //	 "Synonyms",
    //	 "Logic/AI",
    //	 "Term weighting",
    //	 "Similarity calculation"

    /**
     * Constructor.
     */
    public GUI_MainMenu() throws HeadlessException {
        initMenu();
    }

    /**
     * Initalizes the menu
     */
    private void initMenu() {

        for (int i = 0; i < mainMenu.length; i++) {
            // Create menu
            JMenu menu = new JMenu(mainMenu[i][0]);
            JMenuItem item;
            JSeparator sep;
            ActionListener menuListener = new GUI_MainMenuListener();
            for (int j = 1; j < mainMenu[i].length; j++) {

                if (mainMenu[i][j] == "-") {
                    // Add a separator
                    sep = new JSeparator();
                    menu.add(sep);
                } else {
                    // Add an item
                    item = new JMenuItem(mainMenu[i][j++]);
                    item.setToolTipText(mainMenu[i][j++]);
                    item.setName(mainMenu[i][j]);
                    item.addActionListener(menuListener);
                    if (j == 1)
                        item.setMnemonic(KeyEvent.VK_X);
                    menu.add(item);
                }
            }
            // Add the menu
            add(menu);
        }
    }

    /**
     * Returns the index of the menu that holds the items with the name of
     * the specfied requirements set.
     * 
     * @param reqSet A requirements set id
     * @return A menu index
     */
    public static int getRsLabelMenu(int reqSet) {
        return 2;
    }
    
    /**
     * Returns the index of the menu item that holds the name
     * of the specified requirements set.
     * 
     * @param reqSet A requirements set id
     * @return A menu item index
     */
    public static int getRsLabelMenuItem(int reqSet) {
        return reqSet;
    }
    
}