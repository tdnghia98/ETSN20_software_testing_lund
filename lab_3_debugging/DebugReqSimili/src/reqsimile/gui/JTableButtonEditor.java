/*
 * Created on 2004-aug-25
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package reqsimile.gui;

import reqsimile.Application;
import reqsimile.data.ReqDataModel;

import java.awt.Component;

import javax.swing.*;

/**
 * @author johannod
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class JTableButtonEditor extends DefaultCellEditor {

    /** Backward serializable compatibility */
	private static final long serialVersionUID = 1L;
	
	protected JButton button;
	private String label;
	
	/**
	 * Constructor
	 */
	public JTableButtonEditor(JCheckBox checkBox) { //, int mode, int metadataEditorMode) {
		super(checkBox);
		button = new JButton();
		button.setOpaque(true);
		button.addActionListener(Application.getGUI().getEventHandler().new LinkAction());
		button.addMouseListener(Application.getGUI().getEventHandler().new LinkButtonListener());
		button.setRolloverEnabled(false);
		button.setFocusable(false);
	}
	
	/**
	* Gets the tableCellEditorComponent attribute of the ButtonEditor object
	*/
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (isSelected) { 
			button.setForeground(table.getSelectionForeground()); 
			button.setBackground(table.getSelectionBackground()); 
		} else { 
			button.setForeground(table.getForeground()); 
			button.setBackground(UIManager.getColor("Button.background")); 
		} 
		label = ((ReqDataModel)table.getModel()).isLinked(row) ? "Unlink" : "Link";
		button.setText(label);
		return button;
	}
	
	public Object getCellEditorValue() { 
		return new String( label ) ; 
	}
	
}
