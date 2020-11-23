/*
 * Created on 2004-aug-25
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package reqsimile.gui;

import java.awt.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.*;
import javax.swing.border.*;

import reqsimile.APPCONSTANTS;
import reqsimile.Application;
import reqsimile.Common;
import reqsimile.data.*;


/**
 * @author johannod
 *
 */
public class GUI_SettingsReqSources extends JDialog implements APPCONSTANTS {

    /** Backward serializable compatibility */
	private static final long serialVersionUID = 1L;
	
    // GUI objects
	GUI_MainMenu mainMenuGUI;
	GUI_EventHandler guiEventHandler;
					
	// The main content pane
	JPanel contentPane = new JPanel(new BorderLayout());
		
	// Configuration area
	private JPanel configPanel[] = new JPanel[N+1];
	private JTextField rsNameTextField[] = new JTextField[N];
	private JTextField rsDriverURLTextField[] = new JTextField[N];
	private JTextField rsDriverClassNameTextField[] = new JTextField[N];
	private JTextField rsDbURLTextField[] = new JTextField[N];
	private JTextField rsDbUserTextField[]  = new JTextField[N];
	private JPasswordField rsDbPasswordField[] = new JPasswordField[N];

	private JTextField lnDriverURLTextField;
	private JTextField lnDriverClassNameTextField;
	private JTextField lnDbURLTextField;
	private JTextField lnDbUserTextField;
	private JPasswordField lnDbPasswordField;
	
	private JComboBox rsTableComboBox[] = new JComboBox[N];
	private JComboBox rsReqKeyFieldComboBox[] = new JComboBox[N+NR*2];

	private JComboBox lnTableComboBox;
	private JComboBox lnKeyFieldAComboBox;
	private JComboBox lnKeyFieldBComboBox;
	
	// Table holding the input fields selections
	private InputFieldsTableModel[] rsInputFieldsTableModel = new InputFieldsTableModel[N];
	private JScrollPane[] rsInputFieldsScrollPane = new JScrollPane[N];
	private JTable[] rsInputFieldsTable = new JTable[N];
	
	JButton connectButton[] = new JButton[N+NR];
	JButton resetButton[] = new JButton[N];

	JButton saveSettingsButton;
	JButton cancelSettingsButton;

	private DateFormat logTimeFormat = DateFormat.getInstance();
	private boolean emitLogTime = true;

    private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
	
    int rsFieldsListsCurrentTableRow; // Keeps track of the next row in the field list table
    								  // to which a field is to be added
    
    // Actions
    private GUI_EventHandler.FetchFieldsForTableAction fetchFieldsForTableAction[] = new GUI_EventHandler.FetchFieldsForTableAction[N];
    private GUI_EventHandler.FetchFieldsForLinksTableAction fetchFieldsForLinksTableAction;

	/**
	 * @throws java.awt.HeadlessException
	 */
	public GUI_SettingsReqSources() throws HeadlessException {
		super();
		guiEventHandler = Application.getGUI().getEventHandler();
		
		initForm();
		setData();

//		ImageIcon appIcon = new ImageIcon(APP_ICON_FILENAME, "Application icon");
//		if (appIcon.getImageLoadStatus() != MediaTracker.ERRORED )
//			setIconImage( appIcon.getImage() );
//		else
//	        System.err.println("Couldn't find appliction icon: reqsimile.gif");

		this.setModal(true);
		this.setResizable(false);
		this.setLocation(Application.getGUI().getX()+100,Application.getGUI().getY()+100);
		this.setSize(new Dimension(600, 500));
		this.setTitle("Requirements sources");
	}
	
	private void initForm() {
		
		// Input configuration tab
		JLabel rsNameLabel[] = new JLabel[N+NR];
		JLabel rsDriverURLLabel[] = new JLabel[N+NR];
		JLabel rsDriverClassNameLabel[] = new JLabel[N+NR];
		JLabel rsDbURLLabel[] = new JLabel[N+NR];
		JLabel rsDbUserLabel[] = new JLabel[N+NR];
		JLabel rsDbPasswordLabel[] = new JLabel[N+NR];
		JLabel rsTableLabel[] = new JLabel[N+NR];
		JLabel rsReqKeyLabel[] = new JLabel[N];
		
		for(int reqSet=0; reqSet<N; reqSet++) {
			rsNameLabel[reqSet] = new JLabel("Req. set name: ");
			rsDriverURLLabel[reqSet] = new JLabel("JDBC driver URL: ");
			rsDriverClassNameLabel[reqSet] = new JLabel("JDBC driver class name: ");
			rsDbURLLabel[reqSet] = new JLabel("DB URL: ");
			rsDbUserLabel[reqSet] = new JLabel("DB user name: ");
			rsDbPasswordLabel[reqSet] = new JLabel("DB password: ");
			rsTableLabel[reqSet] = new JLabel("Table/query: ");
			rsReqKeyLabel[reqSet] = new JLabel("Unique Req. key field: ");
	
			rsNameTextField[reqSet] = new JTextField();
			SwingExtensions.setPreferredWidth(rsNameTextField[reqSet],200);
			
			rsDriverURLTextField[reqSet] = new JTextField();
			SwingExtensions.setPreferredWidth(rsDriverURLTextField[reqSet], 200);

			rsDriverClassNameTextField[reqSet] = new JTextField();
			SwingExtensions.setPreferredWidth(rsDriverClassNameTextField[reqSet], 200);
			
			rsDbURLTextField[reqSet] = new JTextField();
			SwingExtensions.setPreferredWidth(rsDbURLTextField[reqSet], 200);
			
			rsDbUserTextField[reqSet] = new JTextField();
			SwingExtensions.setPreferredWidth(rsDbUserTextField[reqSet], 200);
			
			rsDbPasswordField[reqSet] = new JPasswordField();
			SwingExtensions.setPreferredWidth(rsDbPasswordField[reqSet], 200);
			
			rsReqKeyFieldComboBox[reqSet] = new JComboBox();
			SwingExtensions.setPreferredWidth(rsReqKeyFieldComboBox[reqSet], 200);

			// Table with fields
			rsInputFieldsTableModel[reqSet] = new InputFieldsTableModel();
			rsInputFieldsTableModel[reqSet].resetTable(0);
			rsInputFieldsTable[reqSet] = new JTable(rsInputFieldsTableModel[reqSet]);
			rsInputFieldsScrollPane[reqSet] = new JScrollPane(rsInputFieldsTable[reqSet]);

			rsTableComboBox[reqSet] = new JComboBox();
			fetchFieldsForTableAction[reqSet] = guiEventHandler.new FetchFieldsForTableAction(this, reqSet, rsReqKeyFieldComboBox[reqSet], rsInputFieldsTableModel[reqSet]);
			rsTableComboBox[reqSet].addActionListener( fetchFieldsForTableAction[reqSet] );
			SwingExtensions.setPreferredWidth(rsTableComboBox[reqSet], 200);
			
			
			
			// Set minimum width of first column
			rsInputFieldsTable[reqSet].getColumnModel().getColumn(0).setMinWidth(100);
			// Set maximum width of checkbox columns
			for(int c=1; c<4; c++) {
				rsInputFieldsTable[reqSet].getColumnModel().getColumn(c).setMaxWidth(60);
				rsInputFieldsTable[reqSet].getColumnModel().getColumn(c).setMinWidth(20);				
			}
			// Set table size			
			rsInputFieldsScrollPane[reqSet].setMinimumSize(new Dimension(200,60));
			rsInputFieldsScrollPane[reqSet].setMaximumSize(new Dimension(500,200));
			rsInputFieldsScrollPane[reqSet].setPreferredSize(new Dimension(300,100));
			rsInputFieldsTable[reqSet].setPreferredScrollableViewportSize(new Dimension(300,100));
			// Hide row selection bar
			rsInputFieldsTable[reqSet].setRowSelectionAllowed(false);
			
			connectButton[reqSet] = new JButton(guiEventHandler.new ConnectAction(reqSet, this));
//			resetButton[reqSet] = new JButton(guiEventHandler.new ResetAction(reqSet));
//			resetButton[reqSet].setEnabled(false);
		}
		
		GridBagConstraints c = new GridBagConstraints();

		// Configuration layout for set A
	    Border border = new LineBorder(Color.gray);
	    Border margin = new EmptyBorder(10,10,10,10);
	    int row;

		for (int reqSet=0; reqSet<N; reqSet++) {
			row = 0;
			configPanel[reqSet] = new JPanel(new GridBagLayout());
			configPanel[reqSet]= new JPanel(new GridBagLayout());
			configPanel[reqSet].setBorder(new CompoundBorder(border, margin));
			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsNameLabel[reqSet],		       row  , 0, 1, 1, .5, 2,  GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsNameTextField[reqSet],		   row++, 1, 1, 2, .5, 2,  GridBagConstraints.LINE_START);

			SwingExtensions.addToGridBag(configPanel[reqSet], rsDriverURLLabel[reqSet],		   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsDriverURLTextField[reqSet],  	   row++, 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);

			SwingExtensions.addToGridBag(configPanel[reqSet], rsDriverClassNameLabel[reqSet],	   row  , 0, 1, 1, .5, 2, GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsDriverClassNameTextField[reqSet],row++, 1, 1, 2, .5, 2, GridBagConstraints.LINE_START);

			SwingExtensions.addToGridBag(configPanel[reqSet], rsDbURLLabel[reqSet],			   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsDbURLTextField[reqSet],  		   row  , 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);
			
			SwingExtensions.getGridBagConstraints().insets = new Insets(14,5,0,0);
			SwingExtensions.addToGridBag(configPanel[reqSet], connectButton[reqSet],  		   row++, 3, 3, 1, .5, 14, GridBagConstraints.LAST_LINE_START);
	 
			SwingExtensions.addToGridBag(configPanel[reqSet], rsDbUserLabel[reqSet],			   row  , 0, 1, 1, .5, 2, GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsDbUserTextField[reqSet],  	   row++, 1, 1, 2, .5, 2, GridBagConstraints.LINE_START);
			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsDbPasswordLabel[reqSet],		   row  , 0, 1, 1, .5, 2, GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsDbPasswordField[reqSet],  	   row++, 1, 1, 2, .5, 2, GridBagConstraints.LINE_START);
			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsTableLabel[reqSet],       	   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsTableComboBox[reqSet],    	   row++, 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);
			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsReqKeyLabel[reqSet],       		   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
			SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
			SwingExtensions.addToGridBag(configPanel[reqSet], rsReqKeyFieldComboBox[reqSet],    		   row++, 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);

			SwingExtensions.addToGridBag(configPanel[reqSet], rsInputFieldsScrollPane[reqSet],   row  , 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);
		}
		
		configPanel[A].setName(Application.getProjectPref(PREF_SECTION_DB[A], PREF_DB_NAME, PREF_DEF_DB_NAME[A] ));
		configPanel[B].setName(Application.getProjectPref(PREF_SECTION_DB[B], PREF_DB_NAME, PREF_DEF_DB_NAME[B] ));

		// Links table
		configPanel[S] = new JPanel(new GridBagLayout());
		configPanel[S].setBorder(new CompoundBorder(border, margin));
		configPanel[S].setName("Links");
		JLabel lnDriverURLLabel = new JLabel("JDBC driver URL: ");
		JLabel lnDriverClassNameLabel = new JLabel("JDBC driver class name: ");
		JLabel lnDbURLLabel = new JLabel("DB URL: ");
		JLabel lnDbUserLabel = new JLabel("DB user name: ");
		JLabel lnDbPasswordLabel = new JLabel("DB password: ");
		JLabel lnTableLabel = new JLabel("Links table: ");
		JLabel lnKeyALabel = new JLabel("Links field A: ");
		JLabel lnKeyBLabel = new JLabel("Links field B: ");

		lnDriverURLTextField = new JTextField();
		SwingExtensions.setPreferredWidth(lnDriverURLTextField, 200);
		lnDriverClassNameTextField = new JTextField();
		SwingExtensions.setPreferredWidth(lnDriverClassNameTextField, 200);
		lnDbURLTextField = new JTextField();
		SwingExtensions.setPreferredWidth(lnDbURLTextField, 200);
		lnDbUserTextField = new JTextField();
		SwingExtensions.setPreferredWidth(lnDbUserTextField, 200);
		lnDbPasswordField = new JPasswordField();
		SwingExtensions.setPreferredWidth(lnDbPasswordField, 200);
		lnTableComboBox = new JComboBox();
		lnKeyFieldAComboBox = new JComboBox();
		lnKeyFieldBComboBox = new JComboBox();
		fetchFieldsForLinksTableAction = guiEventHandler.new FetchFieldsForLinksTableAction(this, lnKeyFieldAComboBox, lnKeyFieldBComboBox);
		lnTableComboBox.addActionListener( fetchFieldsForLinksTableAction );
		SwingExtensions.setPreferredWidth(lnTableComboBox, 200);

		connectButton[S] = new JButton(guiEventHandler.new ConnectLinksAction(this));

		row = 0;
		SwingExtensions.addToGridBag(configPanel[S], lnDriverURLLabel,		   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnDriverURLTextField,  	   row++, 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);

		SwingExtensions.addToGridBag(configPanel[S], lnDriverClassNameLabel,	   row  , 0, 1, 1, .5, 2, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnDriverClassNameTextField,row++, 1, 1, 2, .5, 2, GridBagConstraints.LINE_START);

		SwingExtensions.addToGridBag(configPanel[S], lnDbURLLabel,			   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnDbURLTextField,  		   row++  , 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);

		SwingExtensions.getGridBagConstraints().insets = new Insets(14,5,0,0);
		SwingExtensions.addToGridBag(configPanel[S], connectButton[S],  		   row++, 3, 3, 1, .5, 14, GridBagConstraints.LAST_LINE_START);

		SwingExtensions.addToGridBag(configPanel[S], lnDbUserLabel,			   row  , 0, 1, 1, .5, 2, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnDbUserTextField,  	   row++, 1, 1, 2, .5, 2, GridBagConstraints.LINE_START);
		
		SwingExtensions.addToGridBag(configPanel[S], lnDbPasswordLabel,		   row  , 0, 1, 1, .5, 2, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnDbPasswordField,  	   row++, 1, 1, 2, .5, 2, GridBagConstraints.LINE_START);
		
		SwingExtensions.addToGridBag(configPanel[S], lnTableLabel,       	   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnTableComboBox,    	   row++, 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);

		SwingExtensions.addToGridBag(configPanel[S], lnKeyALabel,       		   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnKeyFieldAComboBox,    		   row++, 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);

		SwingExtensions.addToGridBag(configPanel[S], lnKeyBLabel,       		   row  , 0, 1, 1, .5, 14, GridBagConstraints.LINE_END);
		SwingExtensions.getGridBagConstraints().fill = GridBagConstraints.HORIZONTAL;			
		SwingExtensions.addToGridBag(configPanel[S], lnKeyFieldBComboBox,    		   row++, 1, 1, 2, .5, 14, GridBagConstraints.LINE_START);
		
		JTabbedPane settingsTabbedPane = new JTabbedPane();
		settingsTabbedPane.add(configPanel[A]);
		settingsTabbedPane.add(configPanel[B]);
		settingsTabbedPane.add(configPanel[S]);
		settingsTabbedPane.setBorder(BorderFactory.createRaisedBevelBorder());
		
		contentPane.add(settingsTabbedPane, BorderLayout.NORTH);
		saveSettingsButton = new JButton(guiEventHandler.new SaveSettingsAction(this));
		contentPane.add(saveSettingsButton, BorderLayout.CENTER);
		
		this.setContentPane(contentPane);		
	}
	
	private void setData() {
		for(int i=0; i<N; i++) {
			rsNameTextField[i].setText(Application.getProjectPref( PREF_SECTION_DB[i], PREF_DB_NAME, PREF_DEF_DB_NAME[i] ) );
			rsDriverURLTextField[i].setText( Application.getProjectPref( PREF_SECTION_DB[i], PREF_DB_DRIVER_URL, PREF_DEF_DB_DRIVER_URL ) );
			rsDriverClassNameTextField[i].setText( Application.getProjectPref( PREF_SECTION_DB[i], PREF_DB_DRIVER_CLASSNAME, PREF_DEF_DB_DRIVER_CLASSNAME ) );
			rsDbURLTextField[i].setText( Application.getProjectPref( PREF_SECTION_DB[i], PREF_DB_URL, PREF_DEF_DB_URL ) );
			rsDbUserTextField[i].setText( Application.getProjectPref( PREF_SECTION_DB[i], PREF_DB_USERNAME, PREF_DEF_DB_USERNAME ) );
			//TODO: Protect password
			//rsDbPasswordField[i].setText( Application.getPref( PREF_SECTION_DB[i], PREF_DB_PASSWORD, PREF_DEF_DB_PASSWORD ) );
			
			Object[] tables = Application.getRsSummaryDataModel(i).getDataSet().getTables();
			if (tables != null) {
				suspendTableComboBox(i);
				rsTableComboBox[i].removeAllItems();
				for(int j = 0; j<tables.length; j++)
					rsTableComboBox[i].addItem(tables[j]);
				activateTableComboBox(i);
				rsTableComboBox[i].setSelectedItem( Application.getProjectPref(PREF_SECTION_DB[i], PREF_DB_TABLE, PREF_DEF_DB_TABLE));
				
				rsReqKeyFieldComboBox[i].setSelectedItem( Application.getProjectPref(PREF_SECTION_DB[i], PREF_DB_FIELD_KEY, PREF_DEF_DB_FIELD_KEY));

				Collection fieldList;
				fieldList = Common.toCollection(Application.getProjectPref(PREF_SECTION_DB[i], PREF_DB_FIELDS_SUMMARY, PREF_DEF_DB_FIELDS_SUMMARY));
				for(int r=0; r<rsInputFieldsTableModel[i].getRowCount(); r++)
					if(fieldList.contains(rsInputFieldsTableModel[i].getValueAt(r,0).toString()))
							rsInputFieldsTableModel[i].setValueAt(Boolean.TRUE, r, 1);
				fieldList = Common.toCollection(Application.getProjectPref(PREF_SECTION_DB[i], PREF_DB_FIELDS_DETAIL, PREF_DEF_DB_FIELDS_DETAIL));
				for(int r=0; r<rsInputFieldsTableModel[i].getRowCount(); r++)
					if(fieldList.contains(rsInputFieldsTableModel[i].getValueAt(r,0).toString()))
							rsInputFieldsTableModel[i].setValueAt(Boolean.TRUE, r, 2);
				fieldList = Common.toCollection(Application.getProjectPref(PREF_SECTION_DB[i], PREF_DB_FIELDS_CALC, PREF_DEF_DB_FIELDS_CALC));
				for(int r=0; r<rsInputFieldsTableModel[i].getRowCount(); r++)
					if(fieldList.contains(rsInputFieldsTableModel[i].getValueAt(r,0).toString()))
							rsInputFieldsTableModel[i].setValueAt(Boolean.TRUE, r, 3);
			}
			
		}

		lnDriverURLTextField.setText( Application.getProjectPref( PREF_SECTION_LINKS, PREF_DB_DRIVER_URL, PREF_DEF_DB_DRIVER_URL ) );
		lnDriverClassNameTextField.setText( Application.getProjectPref( PREF_SECTION_LINKS, PREF_DB_DRIVER_CLASSNAME, PREF_DEF_DB_DRIVER_CLASSNAME ) );
		lnDbURLTextField.setText( Application.getProjectPref( PREF_SECTION_LINKS, PREF_DB_URL, PREF_DEF_DB_URL ) );
		lnDbUserTextField.setText( Application.getProjectPref( PREF_SECTION_LINKS, PREF_DB_USERNAME, PREF_DEF_DB_USERNAME ) );
		//TODO: Protect password
		//lnDbPasswordField.setText(  Application.getPref( PREF_SECTION_LINKS, PREF_DB_PASSWORD, PREF_DEF_DB_PASSWORD ) );
		
		suspendLinksTableComboBox();
		updateLinksTables();
		activateLinksTableComboBox();
		lnTableComboBox.setSelectedItem( Application.getProjectPref(PREF_SECTION_LINKS, PREF_DB_TABLE, PREF_DEF_DB_TABLE));
		
		lnKeyFieldBComboBox.setSelectedItem( Application.getProjectPref(PREF_SECTION_LINKS, PREF_DB_FIELD_LINK_KEY[B], PREF_DEF_DB_FIELD_LINKS_KEY[B]));		
		lnKeyFieldAComboBox.setSelectedItem( Application.getProjectPref(PREF_SECTION_LINKS, PREF_DB_FIELD_LINK_KEY[A], PREF_DEF_DB_FIELD_LINKS_KEY[A]));
	}

/* --------------------------------------------------------
 * Return component contents for the requirements sources 
 * --------------------------------------------------------
 */

	
	/**
	 * Returns database's friendly name
	 * @param reqSet The requirements set id
	 * @return String
	 */	
	public String getName(int reqSet) {
		return rsNameTextField[reqSet].getText();
	}
	
	/**
	 * Returns database driver URL
	 * @param reqSet The requirements set id
	 * @return String
	 */	
	public String getDriverURL(int reqSet) {
		return rsDriverURLTextField[reqSet].getText();
	}

	/**
	 * Returns database driver class name
	 * @param reqSet The requirements set id
	 * @return String
	 */	
	public String getDriverClassName(int reqSet) {
		return rsDriverClassNameTextField[reqSet].getText();
	}

	/**
	 * Returns database URL
	 * @param reqSet The requirements set id
	 * @return String
	 */	
	public String getDbURL(int reqSet) {
		return rsDbURLTextField[reqSet].getText();
	}

	/**
	 * Returns the database user name
	 * @param reqSet The requirements set id
	 * @return String
	 */	
	public String getDbUser(int reqSet) {
		return rsDbUserTextField[reqSet].getText();
	}


	/**
	 * Returns the database connection password
	 * @param reqSet The requirements set id
	 * @return char[]
	 */	
	public String getDbPassword(int reqSet) {
		return new String(rsDbPasswordField[reqSet].getPassword());
	}

	
	/**
	 * Returns the selected table/query for a requirements set
	 * 
	 * @param reqSet The requirements set id
	 * @return String
	 */
	public String getTableName(int reqSet) {
		if (rsTableComboBox[reqSet].getItemCount() > 0) {
			return rsTableComboBox[reqSet].getSelectedItem().toString();
		} else {
			return "";
		}
	}

	/**
	 * Return the selected id field
	 * @param reqSet The requirements set id
	 * @return String
	 */
	public String getReqKeyField(int reqSet) {
		if (rsReqKeyFieldComboBox[reqSet].getItemCount() > 0) {
			return rsReqKeyFieldComboBox[reqSet].getSelectedItem().toString();
		} else {
			return "";
		}		
	}
	
/* -----------------------------------------------
 * Return component contents for the links table 
 * -----------------------------------------------
 */
	
	/**
	 * Returns database driver URL for the links table
	 * 
	 * @return String
	 */	
	public String getLinksDriverURL() {
		return lnDriverURLTextField.getText();
	}

	/**
	 * Returns database driver class name for the links table
	 * 
	 * @return String
	 */	
	public String getLinksDriverClassName() {
		return lnDriverClassNameTextField.getText();
	}

	/**
	 * Returns database URL for the links table
	 * 
	 * @return String
	 */	
	public String getLinksDbURL() {
		return lnDbURLTextField.getText();
	}

	/**
	 * Returns the database user name for the links table
	 * 
	 * @return String
	 */	
	public String getLinksDbUser() {
		return lnDbUserTextField.getText();
	}


	/**
	 * Returns the database connection password for the links table
	 * @return String
	 */	
	public String getLinksDbPassword() {
		return new String(lnDbPasswordField.getPassword());
	}

	/**
	 * Returns the selected table/query for the links
	 * 
	 * @return String
	 */
	public String getLinksTableName() {
		if (lnTableComboBox.getItemCount() > 0) {
			return lnTableComboBox.getSelectedItem().toString();
		} else {
			return "";
		}
	}
	
	/**
	 * Return the selected id field
	 * 
	 * @return String
	 */
	public String getLinkKeyFieldA() {
		if (lnKeyFieldAComboBox.getItemCount() > 0) {
			return lnKeyFieldAComboBox.getSelectedItem().toString();
		} else {
			return "";
		}		
	}

	/**
	 * Return the selected id field
	 * 
	 * @return String
	 */
	public String getLinkKeyFieldB() {
		if (lnKeyFieldBComboBox.getItemCount() > 0) {
			return lnKeyFieldBComboBox.getSelectedItem().toString();
		} else {
			return "";
		}		
	}

/* --------------------------
 * Update tables combo boxes
 * --------------------------
 */	
	
	/**
	 * Updates the tables combo box to reflect the tables available in for the data set
	 * 
	 * @param reqSet
	 */
	public void updateTables(int reqSet) {
		rsTableComboBox[reqSet].removeAllItems();
		Object[] tables = Application.getRsSummaryDataModel(reqSet).getDataSet().getTables();
		if (tables != null) {
			for(int j = 0; j<tables.length; j++)
				rsTableComboBox[reqSet].addItem(tables[j]);
		}		
	}
	
	/**
	 * Updates the tables combo box to reflect the tables available for the links
	 */
	public void updateLinksTables() {
		lnTableComboBox.removeAllItems();
		Object[] tables = Application.getDbConnections().getTables(Application.getLinksConnection());
		if (tables != null) {
			for(int j = 0; j<tables.length; j++)
				lnTableComboBox.addItem(tables[j]);
		}				
	}
	
	/**
	 * Return the list of fields to be used in the summary view
	 * 
	 * @param reqSet The requirements set for which fields to return
	 * @return A Collection of fields.
	 */
	public Collection getSummaryFields(int reqSet) {
		Collection fields = new ArrayList();
		
		for(int r=0; r<rsInputFieldsTableModel[reqSet].getRowCount(); r++)
			if ( ((Boolean)(rsInputFieldsTableModel[reqSet].getValueAt(r,1))).booleanValue())
				fields.add(rsInputFieldsTableModel[reqSet].getValueAt(r,0).toString());

		return fields;		
	}
	
	/**
	 * Return the list of fields to be used in the detail view
	 * 
	 * @param reqSet
	 * @return A Collection of fields
	 */
	public Collection getDetailFields(int reqSet) {
		Collection fields = new ArrayList();
		
		for(int r=0; r<rsInputFieldsTableModel[reqSet].getRowCount(); r++)
			if ( ((Boolean)(rsInputFieldsTableModel[reqSet].getValueAt(r,2))).booleanValue())
				fields.add(rsInputFieldsTableModel[reqSet].getValueAt(r,0).toString());

		return fields;		
	}
	
	/**
	 * Return the list of fields to be used in the similarity calculation
	 * 
	 * @param reqSet
	 * @return A Collection of fields
	 */
	public Collection getCalcFields(int reqSet) {
		Collection fields = new ArrayList();
		
		for(int r=0; r<rsInputFieldsTableModel[reqSet].getRowCount(); r++)
			if ( ((Boolean)(rsInputFieldsTableModel[reqSet].getValueAt(r,3))).booleanValue())
				fields.add(rsInputFieldsTableModel[reqSet].getValueAt(r,0).toString());

		return fields;		
	}
	
	/**
	 * Removes the action listener for the table combo box for a specified requirements set
	 * 
	 * @param reqSet The requirements set for which the tables combo box shall be suspended
	 */
	public void suspendTableComboBox(int reqSet) {
		rsTableComboBox[reqSet].removeActionListener( fetchFieldsForTableAction[reqSet] );
	}
	
	/**
	 * Removes the action listener for the links table combo box for a specified requirements set
	 */
	public void suspendLinksTableComboBox() {
		lnTableComboBox.removeActionListener( fetchFieldsForLinksTableAction );
	}
	
	/**
	 * Adds the action listener for the table combo box for a specified requirements set
	 * 
	 * @param reqSet The requirements set for which the tables combo box shall be activated
	 */
	public void activateTableComboBox(int reqSet) {
		rsTableComboBox[reqSet].addActionListener( fetchFieldsForTableAction[reqSet] );		
	}
	
	/**
	 * Adds the action listener for the links table combo box for a specified requirements set
	 */
	public void activateLinksTableComboBox() {
		lnTableComboBox.addActionListener( fetchFieldsForLinksTableAction );		
	}

}
