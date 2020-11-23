/*
 * Created on 2004-aug-24
 *
 */
package reqsimile.gui;

import reqsimile.*;
import reqsimile.APPCONSTANTS;
import reqsimile.data.*;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author johannod
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SimValueColorRenderer implements TableCellRenderer, APPCONSTANTS {

	TableCellRenderer defaultRenderer;
	Color baseColor = null;
	Double simValue;
	
	/**
	 * 
	 */
	public SimValueColorRenderer(TableCellRenderer defaultRenderer) {
		this.defaultRenderer = defaultRenderer;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int col) {

	    ReqDataModel tableModel = (ReqDataModel) table.getModel();

	    simValue = tableModel.getSimValue(row);
	    
    	if (tableModel.getColumnName(col).equals(RESULT_FIELD_SIMVALUE) && (simValue.doubleValue() > 0)) {
   			baseColor = new Color(255, 0, 0, Math.round(255 * simValue.floatValue()) );
    	} else {
    		switch (tableModel.getLinkStatus(row)) {
    			case 0:	// No link
    				baseColor = table.getBackground();
    				break;
    			case 1:	// Link to marked only
    				baseColor = CLR_ACTIVE_LINK;
    				break;
    			case 2: // Link to another only
    				baseColor = CLR_PASSIVE_LINK;
    				break;
    			case 3:
    				baseColor = Common.multiplyBlend(CLR_PASSIVE_LINK, CLR_ACTIVE_LINK);
    				break;
    		}
    	}
    	
		if (isSelected)
			baseColor = Common.multiplyBlend(baseColor, table.getSelectionBackground());
	
	    Component c =
            defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

	    // Set the component color
		c.setBackground(baseColor);
		
		return c;
	}
}
