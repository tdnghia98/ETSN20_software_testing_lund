package reqsimile.gui;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.table.*;

import java.util.*;
import java.io.IOException;
import java.io.StringReader;
import java.text.*;

import reqsimile.*;
import reqsimile.data.*;
import reqsimile.gui.GUI_EventHandler.*;
import reqsimile.simcalc.PreProcessor;

//import biz.chitec.quarterback.swing.*;

//import net.sourceforge.mlf.metouia.*;  

/**
 * This class comprises the main GUI of the application
 * 
 * @author Johan Natt och Dag
 */
public class GUI_Main extends JFrame implements APPCONSTANTS /* ComponentListener */{

    /** Backward serialization compatibility */
    private static final long serialVersionUID = 1L;

    //variables for GUI objects
    GUI_MainMenu mainMenuGUI;

    GUI_EventHandler guiEventHandler;

    // Data containers
    private JTable rsTable[] = new JTable[N + NR],
            rsDetailTable[] = new JTable[N + NR],
            rsDetailRowHeaderTable[] = new JTable[N + NR];

    private JTable rsSuggestedTable, rsResultTable, rsSuggestedDetailTable,
            rsResultDetailTable;

    private RsSelectionListener rsTableSelectionlistener[] = new RsSelectionListener[N
            + NR];

    private RsDoubleClickListener rsTableDoubleClickListener[] = new RsDoubleClickListener[N
            + NR];

    // Row height resizers
    private JTableRowHeightResizer rsRowHeightResizer[] = new JTableRowHeightResizer[N
            + NR];

    // Column width resizers
    /*private TableCellSizeAdjustor rsColumnResizer[] = new TableCellSizeAdjustor[N
            + NR];*/

    // Scroll bars
    private JScrollPane rsScrollPane[] = new JScrollPane[N + NR],
            rsDetailScrollPane[] = new JScrollPane[N + NR],
            rsSuggestedScrollPane, rsResultScrollPane,
            rsSuggestedDetailScrollPane, rsResultDetailScrolPane,
            logScrollPane;

    // Requirement sets containers
    private JTabbedPane inputTabbedPane;

    // Output containers
    private JTabbedPane outputTabbedPane;

    // Requirements suggestions output container
    private JPanel suggestPanel, resultPanel;

    // Log window area
    private JTextArea logTextArea;

    // The split panes
    private JSplitPane[] rsSplitPane = new JSplitPane[N + NR];

    private JSplitPane rsSuggestedSplitPane, rsResultSplitPane, mainSplitPane;

    // The main content pane
    JPanel contentPane = new JPanel(new BorderLayout());

    // Status bar
    private JPanel statusPanel;

    private JLabel statusLabel;

    private JProgressBar statusProgressBar;

    // Configuration area
    private JPanel rsConfigPanel[] = new JPanel[N + NR];

    private JTextField rsNameTextField[] = new JTextField[N + NR];

    private JTextField rsDriverURLTextField[] = new JTextField[N + NR];

    private JTextField rsDriverClassNameTextField[] = new JTextField[N + NR];

    private JTextField rsDbURLTextField[] = new JTextField[N + NR];

    private JTextField rsDbUserTextField[] = new JTextField[N + NR];

    private JPasswordField rsDbPasswordField[] = new JPasswordField[N + NR];

    private JComboBox rsTableComboBox[] = new JComboBox[N + NR];

    private JComboBox rsReqKeyFieldComboBox[] = new JComboBox[N + NR];

    // Table holding the input fields selections
    private InputFieldsTableModel[] rsInputFieldsTableModel = new InputFieldsTableModel[N];

    private JScrollPane[] rsInputFieldsScrollPane = new JScrollPane[N];

    private JTable[] rsInputFieldsTable = new JTable[N];

    private JSplitPane configSplitPane;

    // Manual search terms
    JTextField searchTermsTextField;

    JCheckBox searchTermsOnlyCheckBox;

    private DateFormat logTimeFormat = DateFormat.getInstance();

    private boolean emitLogTime = true;

    private static final Cursor WAIT_CURSOR = Cursor
            .getPredefinedCursor(Cursor.WAIT_CURSOR);

    private static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();

    int rsFieldsListsCurrentTableRow; // Keeps track of the next row in the

    // field list table

    // to which a field is to be added

    /**
     * Constructor for the main frame.
     * 
     * @param title
     *            The application window title
     * @throws HeadlessException
     */
    public GUI_Main(String title) throws HeadlessException {
        super(title);
        initializeForm();
    }

    /**
     * Initializes the frame, adding components and sets default layout
     * 
     * @return Nothing
     */
    private void initializeForm() {
        // set the size and location
        this.setSize(new Dimension(800, 600));

        //this.addComponentListener(this);
        guiEventHandler = new GUI_EventHandler(this);

        // Add menu
        mainMenuGUI = new GUI_MainMenu();
        setJMenuBar(mainMenuGUI);

        inputTabbedPane = new JTabbedPane();

        JTableHeader corner;

        // Requirements sets (sources and similarity set)

        JPanel manualPanel = null;

        for (int reqSet = 0; reqSet < N + NR; reqSet++) {
            // Summary table
            rsTable[reqSet] = new JTable();
            rsTable[reqSet]
                    .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            rsScrollPane[reqSet] = new JScrollPane(rsTable[reqSet]);

            // Add text box for manual search terms
            if (reqSet >= N) {
                manualPanel = new JPanel(new BorderLayout());
                JPanel searchBox = new JPanel();
                searchBox.setLayout(new BoxLayout(searchBox,
                        BoxLayout.LINE_AXIS));
                searchTermsTextField = new JTextField();
                searchTermsTextField
                        .addKeyListener(guiEventHandler.new SearchTermSubmitListener());
                searchBox.add(Box.createHorizontalStrut(4));
                searchBox.add(new JLabel("Search terms:"));
                searchBox.add(Box.createHorizontalStrut(2));
                searchBox.add(searchTermsTextField);
                searchBox.add(Box.createHorizontalStrut(4));

                searchTermsOnlyCheckBox = new JCheckBox();
                searchTermsOnlyCheckBox
                        .setToolTipText("Check this to only use the search terms "
                                + "to fetch similar requirements. "
                                + "Uncheck to use both the selected "
                                + "requirement and the search terms.");
                searchTermsOnlyCheckBox
                        .addItemListener(guiEventHandler.new searchTermsOnlyItemListener());
                searchBox.add(searchTermsOnlyCheckBox);
                searchBox.add(Box.createHorizontalStrut(2));
                searchBox.add(new JLabel("Only"));
                searchBox.add(Box.createHorizontalStrut(4));

                manualPanel.add(searchBox, BorderLayout.NORTH);
                manualPanel.add(rsScrollPane[reqSet], BorderLayout.CENTER);
            }

            // Add listener for selection changes
            rsTableSelectionlistener[reqSet] = guiEventHandler.new RsSelectionListener(
                    reqSet);
            rsTable[reqSet].getSelectionModel().addListSelectionListener(
                    rsTableSelectionlistener[reqSet]);
            rsTable[reqSet].getColumnModel().getSelectionModel()
                    .addListSelectionListener(rsTableSelectionlistener[reqSet]);

            // Add listener for double clicks
            if ((reqSet < N)) {//&& !Application.manualMode()) {
                rsTableDoubleClickListener[reqSet] = guiEventHandler.new RsDoubleClickListener(
                        reqSet);
                rsTable[reqSet]
                        .addMouseListener(rsTableDoubleClickListener[reqSet]);
            }

            // Detail table
            rsDetailTable[reqSet] = new JTable();
            rsDetailRowHeaderTable[reqSet] = new JTable(); // Row
            // headers
            // Hide column headers
            //rsDetailTable[reqSet].getTableHeader().setVisible(false);
            //rsDetailRowHeaderTable[reqSet].getTableHeader().setVisible(false);
            // Turn off intercell spacing (as we use default table header
            // border)
            rsDetailRowHeaderTable[reqSet].setIntercellSpacing(new Dimension(0,
                    0));
            rsDetailRowHeaderTable[reqSet]
                    .setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            // Set header colors
            LookAndFeel.installColorsAndFont(rsDetailRowHeaderTable[reqSet],
                    "TableHeader.background", "TableHeader.foreground",
                    "TableHeader.font");
            // Assign row renderer
            rsDetailRowHeaderTable[reqSet].setDefaultRenderer(Object.class,
                    new RowHeaderRenderer());
            // Create scroll pane for table
            rsDetailScrollPane[reqSet] = new JScrollPane(rsDetailTable[reqSet]);
            // Set row header for the scroll pane
            rsDetailScrollPane[reqSet]
                    .setRowHeaderView(rsDetailRowHeaderTable[reqSet]);
            // Set the column header as corner component
            corner = rsDetailRowHeaderTable[reqSet].getTableHeader();
            corner.setReorderingAllowed(false);
            //corner.setResizingAllowed(false);
            rsDetailScrollPane[reqSet].setCorner(JScrollPane.UPPER_LEFT_CORNER,
                    corner);
            // Handle synchronization bug (does not always work)
            new JScrollPaneAdjuster(rsDetailScrollPane[reqSet]);
            // Enable row resizing (both height and width)
            new JTableRowHeaderResizer(rsDetailScrollPane[reqSet])
                    .setEnabled(true);
            rsRowHeightResizer[reqSet] = new JTableRowHeightResizer(
                    rsDetailRowHeaderTable[reqSet], rsDetailTable[reqSet]);
            rsRowHeightResizer[reqSet].setEnabled(true);

            // Automatically adjust columns
            //rsColumnResizer[reqSet] =
            // TableCellSizeAdjustor.adjustorForTable(rsTable[reqSet]);
            // Set column auto-resizing off
            rsTable[reqSet].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // Disable cell/row/column selections in row header
            //rsDetailRowHeaderTable[reqSet].setCellSelectionEnabled(false);
            //rsDetailRowHeaderTable[reqSet].setFocusable(false);

            // Requirement set split pane
            if (reqSet >= N) {
                rsSplitPane[reqSet] = new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT, manualPanel,
                        rsDetailScrollPane[reqSet]);
            } else {
                rsSplitPane[reqSet] = new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT, rsScrollPane[reqSet],
                        rsDetailScrollPane[reqSet]);
            }
            rsSplitPane[reqSet].setResizeWeight(0.4);
            rsSplitPane[reqSet].setDividerSize(3);
        }

        // Add color renderers
        registerSimValueColorRendererForClass(rsTable[S], Double.class);
        registerSimValueColorRendererForClass(rsTable[S], Integer.class);
        registerSimValueColorRendererForClass(rsTable[S], String.class);

        registerReqSetColorRendererForClass(rsTable[A], String.class);
        registerReqSetColorRendererForClass(rsTable[B], String.class);

        // Add req sets
        inputTabbedPane.add(rsSplitPane[A]);
        inputTabbedPane.add(rsSplitPane[B]);

        //
        // OUTPUT AREA (BOTTOM HALF OF WINDOW)
        //
        rsSplitPane[S].setName("Candidate requirements");

        // Log window
        logTextArea = new JTextArea(5, 20);
        logTextArea.setName("Log");
        logTextArea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.append("Welcome to ReqSimile.\n\n");
        logTextArea.append("Select your source requirements sets.\n");
        //		logTextArea.append("Use the following shortcuts to show and move
        // focus to to
        // the different tabs:\n");
        //		logTextArea.append("Ctrl-1 - Primary requirements set\n");
        //		logTextArea.append("Ctrl-2 - Secondary requirements set\n");
        //		logTextArea.append("Ctrl-S - Candidates for similarity\n");
        //		logTextArea.append("Ctrl-R - Resulting requirements set (Merge
        // result)\n");
        //		logTextArea.append("Ctrl-L - Log\n");
        //		logTextArea.append("Ctrl-Z - Alternate focus between primary and
        // candidate
        // requirements sets.\n\n");
        logTextArea.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        logScrollPane = new JScrollPane(logTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setName("Log");

        // Add suggestion table, results table and log text area to a tabbed
        // pane
        outputTabbedPane = new JTabbedPane();
        outputTabbedPane.add(logScrollPane);
        outputTabbedPane.add(rsSplitPane[S]);
        // outputTabbedPane.add(rsResultSplitPane);

        outputTabbedPane.setSelectedIndex(1);

        // Create split between input and output areas
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                inputTabbedPane, outputTabbedPane);
        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setDividerSize(3);

        //ct.add(finalPane, BorderLayout.NORTH);

        //inputPane.setResizeWeight(0.4);
        //inputPane.setDividerSize(3);
        //outputPane.setResizeWeight(0.3);
        //outputPane.setDividerSize(3);
        //mainPane.setAlignmentY(JSplitPane.TOP_ALIGNMENT);

        // Status panel
        statusLabel = new JLabel("ReqSimile initialized and ready!");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusProgressBar = new JProgressBar(0, 100);
        statusProgressBar.setValue(100);
        statusProgressBar.setStringPainted(true);
        statusProgressBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(Box.createRigidArea(new Dimension(20, 20)),
                BorderLayout.LINE_START);
        statusPanel.add(statusLabel, BorderLayout.LINE_START);
        statusPanel.add(statusProgressBar, BorderLayout.LINE_END);

        // Create a content pane holding the split pane and the status bar
        contentPane.add(mainSplitPane, BorderLayout.CENTER);
        contentPane.add(statusPanel, BorderLayout.PAGE_END);
        contentPane.setOpaque(true);

        this.setContentPane(contentPane);

        ImageIcon appIcon = new ImageIcon(APP_ICON_FILENAME, "Application icon");
        if (appIcon.getImageLoadStatus() != MediaTracker.ERRORED)
            setIconImage(appIcon.getImage());
        else
            System.err.println("Couldn't find appliction icon: reqsimile.gif");
    }

    /**
     * Centers the window on the screen.
     */
    public void centreWindow() {
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        Point p = new Point((d.width - (this.getWidth())) / 2,
                (d.height - (this.getHeight())) / 2);
        this.setLocation(p);
    }

    /**
     * Outputs a line of text in the log window area. The text will be followed
     * by a line break.
     * 
     * @param message
     *            Text to output. May contain line breaks.
     */
    public void logPrintln(String message) {
        logPrint(message + "\n");
        // Beginning of line - print log time next time
        emitLogTime = true;
    }

    /**
     * Outputs a line of text in the log window area.
     * 
     * @param message
     *            Text to output. May contain line breaks.
     */
    public void logPrint(String message) {
        // If the text area is not yet realized, and
        // we tell it to draw text, it could cause
        // a text/AWT tree deadlock. Our solution is
        // to ensure that the text area is realized
        // before attempting to draw text.
        if (logTextArea.isEnabled()) {
            if (emitLogTime) {
                // Output current time
                logTextArea.append(logTimeFormat.format(new Date(System
                        .currentTimeMillis()))
                        + " -- ");
            }
            logTextArea.append(message);
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            // Not at beginning of line - do not print log time next time
            emitLogTime = false;
        }
    }

    /**
     * Sets the name of a requirements set
     * 
     * @param reqSet
     *            The requirements set id
     * @param aName
     *            A string with the name
     */
public void setRsName(int reqSet, String aName) {
        inputTabbedPane.setTitleAt(reqSet, aName);

        if (mainMenuGUI.isVisible()) {
	        // Update menu
	        JMenuItem mi = mainMenuGUI.getMenu(GUI_MainMenu.getRsLabelMenu(reqSet)).getItem(
	                GUI_MainMenu.getRsLabelMenuItem(reqSet));
	        
	                	mi.setText(
	                        Application.getProjectPref(
	                                APPCONSTANTS.PREF_SECTION_DB[reqSet],
	                                APPCONSTANTS.PREF_DB_NAME,
	                                APPCONSTANTS.PREF_DEF_DB_NAME[reqSet])
	                );
        }
        

    }
    public void setRsSummaryDataSource(int reqSet, ReqDataModel reqData) {
        rsTable[reqSet].setModel(reqData);
    }

    public void setRsDetailDataSource(int reqSet, ReqDataModel reqData) {
        rsDetailTable[reqSet].setModel(reqData);
    }

    public void setRsDetailRowHeaderDataSource(int reqSet,
            DefaultTableModel reqData) {
        rsDetailRowHeaderTable[reqSet].setModel(reqData);
    }

    public void resetProgressBar() {
        statusProgressBar.setValue(0);
    }

    public void setProgressBar(final int aValue) {
        //SwingUtilities.invokeLater(new Runnable() {
        //public void run() {
        statusProgressBar.setValue(aValue);
        //}
        //});
    }

    public void resetStatus() {
        statusLabel.setText("Idle.");
    }

    public void setStatus(String aText) {
        statusLabel.setText(aText);
    }

    public void showError(String aText, Exception e) {
        logPrint(aText + ": ");
        if (e.getMessage() != null) {
            logPrintln(e.getMessage());
        } else {
            logPrintln("Unhandled error (reference to null pointer)");
        }
        resetStatus();
    }

    /**
     * Returns the selected row in a table
     * 
     * @param reqSet
     * @return The selected row
     */
    public int getRsSelectedRow(int reqSet) {
        return rsTable[reqSet].getSelectedRow();
    }

    /**
     * Synchronizes the row heights of the data rows to the row headers' heights
     * 
     * @param reqSet
     */
    public void synchRsRowHeights(int reqSet) {
        rsRowHeightResizer[reqSet].synchronizeRowHeights();
    }

    /**
     * Returns the unique key for the selected row in the specified requirements
     * set
     * 
     * @param reqSet
     * @return The requirements key
     */
    public int getReqKeyForSelectedRow(int reqSet) {
        int row = rsTable[reqSet].getSelectedRow();
        int key = Application.getRsDataSet(reqSet).getKeyAt(row);
        return key;
    }

    /**
     * Disables the selection change listener for a requirements set's table
     * 
     * @param reqSet
     */
    public void disableRsTableSelectionListener(int reqSet) {
        rsTable[reqSet].getSelectionModel().removeListSelectionListener(
                rsTableSelectionlistener[reqSet]);
        rsTable[reqSet].getColumnModel().getSelectionModel()
                .removeListSelectionListener(rsTableSelectionlistener[reqSet]);
    }

    /**
     * Enables the selection change listener for a requirements set's table
     * 
     * @param reqSet
     */
    public void enableRsTableSelectionListener(int reqSet) {
        rsTable[reqSet].getSelectionModel().addListSelectionListener(
                rsTableSelectionlistener[reqSet]);
        rsTable[reqSet].getColumnModel().getSelectionModel()
                .addListSelectionListener(rsTableSelectionlistener[reqSet]);
    }

    /**
     * Notify table listeners that changes to model/structure/data has been made
     * 
     * @param reqSet
     */
    public void updateRsListeners(int reqSet) {
        //rsColumnResizer[reqSet].reBindListeners();
    }

    /**
     * Sets the width of a the table column that holds the row headers for a
     * specific requirements set
     * 
     * @param reqSet
     */
    public void adjustTableRowHeaderWidth(int reqSet) {
        Dimension d = rsDetailRowHeaderTable[reqSet]
                .getPreferredScrollableViewportSize();
        d.width = rsDetailRowHeaderTable[reqSet].getPreferredSize().width;
        rsDetailRowHeaderTable[reqSet].setPreferredScrollableViewportSize(d);
    }

    public void setCellRenderer(int reqSet) {
        MultiLineCellRenderer multiLineCR = new MultiLineCellRenderer();
        rsDetailTable[reqSet].getColumnModel().getColumn(0).setCellRenderer(
                multiLineCR);
    }

    public void setTableButtonRendererAndEditor(int reqSet, int col) {
        rsTable[reqSet].getColumnModel().getColumn(col).setCellRenderer(
                new JTableButtonRenderer());
        rsTable[reqSet].getColumnModel().getColumn(col).setCellEditor(
                new JTableButtonEditor(new JCheckBox()));
    }

    public int getRowAtPoint(int reqSet, Point p) {
        return rsTable[reqSet].rowAtPoint(p);
    }

    public void setSelectedRow(int reqSet, int row) {
        rsTable[reqSet].setRowSelectionInterval(row, row);
    }

    public void synchSelectedRow(int reqSet, MouseEvent e) {
        setSelectedRow(reqSet, getRowAtPoint(reqSet, SwingUtilities
                .convertPoint(e.getComponent(), e.getPoint(), rsTable[reqSet])));
    }

    public int getSelectedRow(int reqSet, MouseEvent e) {
        return getRowAtPoint(reqSet, SwingUtilities.convertPoint(e
                .getComponent(), e.getPoint(), rsTable[reqSet]));
    }

    public void setValueAt(int reqSet, Object o, int row, int col) {
        rsTable[reqSet].setValueAt(o, row, col);
    }

    private void registerSimValueColorRendererForClass(JTable table, Class cls) {
        // Get Default Renderer from the table
        DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer) table
                .getDefaultRenderer(cls);

        // Wrap(Decorate) the color renderer around the default renderer
        TableCellRenderer colorRenderer = new SimValueColorRenderer(
                defaultRenderer);

        // Register the color Renderer with the JTable
        table.setDefaultRenderer(cls, colorRenderer);
    }

    private void registerReqSetColorRendererForClass(JTable table, Class cls) {
        // Get Default Renderer from the table
        DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer) table
                .getDefaultRenderer(cls);

        // Wrap(Decorate) the color renderer around the default renderer
        TableCellRenderer colorRenderer = new ReqSetColorRenderer(
                defaultRenderer);

        // Register the color Renderer with the JTable
        table.setDefaultRenderer(cls, colorRenderer);
    }

    /**
     * Temporarily disables the requirements selection and configuration changes
     * 
     * @param reqSet
     */
    public void disableInteraction(int reqSet) {
        // Table structure change fires a selection change
        // so we temporarily disable the listener
        disableRsTableSelectionListener(reqSet);
        // Mouse pointer
        setWaitCursor(true);

        validate();
    }

    /**
     * Enables the requirements selection and configuration changes
     */
    public void enableInteraction(int reqSet) {
        // Enable table selection listner again
        enableRsTableSelectionListener(reqSet);
        // Mouse pointer
        setWaitCursor(false);

        resetStatus();
    }

    public void setWaitCursor(boolean busy) {
        if (busy) {
            contentPane.setCursor(WAIT_CURSOR);
        } else {
            contentPane.setCursor(DEFAULT_CURSOR);
        }
    }

    public GUI_EventHandler getEventHandler() {
        return guiEventHandler;
    }

    /**
     * Activates a requirements tab
     * 
     * @param reqSet
     */
    public void displayReqSet(int reqSet) {
        inputTabbedPane.setSelectedIndex(reqSet);
    }

    /**
     * Acrtivates log window tab
     */
    public void displayLog() {
        outputTabbedPane.setSelectedIndex(0);
    }

    /**
     *  
     */
    public void displayResult() {
        outputTabbedPane.setSelectedIndex(1);
    }

    public void stopCellEditing(int reqSet) {
        rsTable[reqSet].getCellEditor().stopCellEditing();
    }

    public Collection getSearchTerms() {
        StringReader stringReader = new StringReader(searchTermsTextField
                .getText());
        String preProcessed = "", nextToken;
        HashSet tokens = new HashSet();

        PreProcessor preProcessor = new PreProcessor(stringReader);
        try {
            while ((nextToken = preProcessor.yylex()) != null) {
                preProcessed += nextToken + " ";
                // Add token to token table
                tokens.add(nextToken);
            }
        } catch (IOException e) {
            showError("Could not preprocess search terms", e);
        }

        return tokens;
    }

    public int getRsForVisibleTab() {
        return inputTabbedPane.getSelectedIndex();
    }

    public void adjustTableRowHeight(int reqSet, String field) {
        int row = 0;
        while (!rsDetailRowHeaderTable[reqSet].getValueAt(row, 0).equals(field)) {
            row++;
            if (row == rsDetailRowHeaderTable[reqSet].getRowCount())
                return;
        }
        if (rsDetailRowHeaderTable[reqSet].getValueAt(row, 0).equals(field)) {
            rsDetailRowHeaderTable[reqSet].setRowHeight(row, 300);
            synchRsRowHeights(reqSet);
        }
    }

    public void repaintTable(int reqSet) {
        rsTable[reqSet].repaint();
    }

    /**
     * Returns the state of the checkbox for using search terms only or both the
     * selected requirement and the search terms
     */
    public boolean useSearchTermsOnly() {
        return searchTermsOnlyCheckBox.isSelected();
    }

}