package reqsimile.gui;

import java.awt.Component;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;

class RowHeaderRenderer
	extends JLabel	//TODO: Use JTableHeader. But fix resizing....
	implements TableCellRenderer
//	implements 
    //implements ListCellRenderer
{
    /** Backward serializable compatibility */
	private static final long serialVersionUID = 1L;
	
    protected Border noFocusBorder, focusBorder;
    protected DefaultTableColumnModel columnModel;
    protected TableColumn column;

    public RowHeaderRenderer()
    {
    	//columnModel = new DefaultTableColumnModel();
    	//column = new TableColumn();
    	//columnModel.addColumn(column);
    	//setColumnModel(columnModel);

    	noFocusBorder = UIManager.getBorder("TableHeader.cellBorder");
        focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
        setOpaque(true);
        setBorder(noFocusBorder);
        
        setHorizontalAlignment(SwingConstants.RIGHT);
        setVerticalAlignment(TOP);
    }

    /**
     * Handle Look and Feel changes
     */
    public void updateUI()
    {
        super.updateUI();
//        Border cell = UIManager.getBorder("TableHeader.cellBorder");
//        Border focus = UIManager.getBorder("Table.focusCellHighlightBorder");
        
//        focusBorder = new BorderUIResource.CompoundBorderUIResource(cell, focus);

        //Insets i = focus.getBorderInsets(this);

//        noFocusBorder = new BorderUIResource.CompoundBorderUIResource
//             (cell, BorderFactory.createEmptyBorder(-1, i.left, 0, i.right));

        noFocusBorder = UIManager.getBorder("TableHeader.cellBorder");
        focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
    

    }
    
    public Component getTableCellRendererComponent(JTable table, Object value,
                       boolean selected, boolean focused, int row, int col)
    {   
    	JTableHeader h = table != null ? table.getTableHeader() : null;

        if (h != null)
        {
            setEnabled(h.isEnabled());         
            setComponentOrientation(h.getComponentOrientation());

            setForeground(h.getForeground());
            setBackground(h.getBackground());
            setFont(h.getFont());
        }
        else
        {
        	/* Use sensible values instead of random leftover values from the last call */
            setEnabled(true);
            setComponentOrientation(ComponentOrientation.UNKNOWN);

            setForeground(UIManager.getColor("TableHeader.foreground"));
            setBackground(UIManager.getColor("TableHeader.background"));
            setFont(UIManager.getFont("TableHeader.font"));
        }

        //column.setHeaderValue(value);
        if (value != null)
        	setText(value.toString()+" ");
 
        return this;

    }
}