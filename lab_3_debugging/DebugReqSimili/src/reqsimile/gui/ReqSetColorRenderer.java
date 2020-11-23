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
public class ReqSetColorRenderer implements TableCellRenderer, APPCONSTANTS {

	TableCellRenderer defaultRenderer;
	Color baseColor = null;
	
	/**
	 * 
	 */
	public ReqSetColorRenderer(TableCellRenderer defaultRenderer) {
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
	    
	    if (tableModel.isMarked(row))
	    	baseColor = CLR_ACTIVE_LINK;
	    else
	    	baseColor = table.getBackground();
	    
	    if (tableModel.isLinked(row))
			baseColor = Common.multiplyBlend(baseColor, CLR_PASSIVE_LINK);
	    
		if (isSelected)
			baseColor = Common.multiplyBlend(baseColor, table.getSelectionBackground());
	
	    Component c =
            defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

	    // Set the component color
		c.setBackground(baseColor);
		
		return c;
	}
}
