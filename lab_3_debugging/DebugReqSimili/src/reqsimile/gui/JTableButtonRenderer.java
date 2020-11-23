package reqsimile.gui;

import reqsimile.data.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/**
 * 
 * @author Johan Natt och Dag
 */
public class JTableButtonRenderer extends JButton implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public JTableButtonRenderer() {
		setOpaque(true);
		Dimension btnSize = getPreferredSize();
		btnSize.height = 18;
		setPreferredSize(btnSize);
		setBorder(new EmptyBorder(1,1,1,1));
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			 boolean isSelected,
			 boolean hasFocus,
			 int row, int column)
	{
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getForeground());
		} else {
			//setIcon(ExpEditor);
			setForeground(table.getForeground());
			setBackground(UIManager.getColor("Button.background"));
		}
		setText( (((ReqDataModel)table.getModel()).getLinkStatus(row) & 1) == 1 ? "Unlink" : "Link" );
		return this;
	}

}
