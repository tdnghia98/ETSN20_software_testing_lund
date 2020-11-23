package reqsimile.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;

public class JTableRowHeightResizer
    extends MouseInputAdapter
{
	private Point 	last;
    private JTable 	rowTable, dataTable;
    private boolean	active, enabled;
 
    private int startY, startHeight;
    private int rowToChange;

    private static final int PIXELS = 10;
    private static final int MIN_HEIGHT = 20;
    private static final int MAX_HEIGHT = 800;
    private static final Cursor RESIZE_CURSOR = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    private Cursor oldCursor;

    public JTableRowHeightResizer(JTable rowTable, JTable dataTable)
    {
        this.rowTable = rowTable;
        this.dataTable = dataTable;
    }
    
    public void synchronizeRowHeights() {
    	for(int r=0; r<rowTable.getRowCount(); r++) {
    		if (rowTable.getRowHeight(r) < MIN_HEIGHT) {
    			rowTable.setRowHeight(r, MIN_HEIGHT);
    			dataTable.setRowHeight(r, MIN_HEIGHT);
    		}    		
    		else
    			dataTable.setRowHeight(r, rowTable.getRowHeight(r));
    	}
    }
    
    public void setEnabled(boolean what)
    {
        if (enabled == what)
            return;

        enabled = what;
        
        if (enabled)
            addListeners();
        else
            removeListeners();
    }

    protected void addListeners()
    {
        if (rowTable != null)
        {
            this.rowTable.addMouseListener(this);
            this.rowTable.addMouseMotionListener(this);
        }
    }

    protected void removeListeners()
    {
        if (rowTable != null)
        {
            rowTable.removeMouseListener(this);
            rowTable.removeMouseMotionListener(this);
        }
    }

    public void mouseExited(MouseEvent e)
    {
        if (oldCursor != null)
        {
            rowTable.setCursor(oldCursor);
            oldCursor = null;
        }
    }

    public void mouseEntered(MouseEvent e)
    {
        mouseMoved(e);
    }

    public void mouseMoved(MouseEvent e)
    {
    	JTable t = (JTable)e.getComponent();
    	int row = t.rowAtPoint(e.getPoint());
    	Rectangle r = t.getCellRect(row, -1, true);
        if (r.y + r.height - e.getY() <= PIXELS)
        {
            if (oldCursor == null)
            {
                oldCursor = rowTable.getCursor();
                rowTable.setCursor(RESIZE_CURSOR);
            }
        }
        else if (oldCursor != null)
        {
            rowTable.setCursor(oldCursor);
            oldCursor = null;
        }
    }
    
    
    public void mousePressed(MouseEvent e)
    {
    	JTable t = (JTable)e.getComponent();
    	rowToChange = t.rowAtPoint(e.getPoint());
        startHeight = t.getRowHeight(rowToChange);
    	Rectangle r = t.getCellRect(rowToChange, -1, true);
       
        startY = e.getY();
        
        if (r.y + startHeight - startY > PIXELS)
            return;
        
        active = true;
        
        if (oldCursor == null)
        {
            oldCursor = rowTable.getCursor();
            rowTable.setCursor(RESIZE_CURSOR);
        }
    }
    
    public void mouseReleased(MouseEvent e)
    {
        active = false;
    }
    
    public void mouseDragged(MouseEvent e)
    {
        if (!active)
            return;

        int newY = e.getY();
        
        int newHeight = startHeight + newY - startY;
        
        if (newHeight < MIN_HEIGHT)
            newHeight = MIN_HEIGHT;
        else if (newHeight > MAX_HEIGHT)
            newHeight = MAX_HEIGHT;
        
        rowTable.setRowHeight(rowToChange, newHeight);        
        dataTable.setRowHeight(rowToChange, newHeight);        
        rowTable.revalidate();
    }
}