/*
 * Created on 2004-jun-18
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package reqsimile.gui;

import java.awt.*;
import javax.swing.*;

/**
 * Common functions for Swing components
 * @author johannod
 *
 */
public class SwingExtensions {

	private static GridBagConstraints c = new GridBagConstraints();

	/**
	 * Sets the preferred width of an arbitrary component
	 * @param aComponent The component for which the preferred width shall be set
	 * @param aWidth The width to set
	 */
	public static void setPreferredWidth(JComponent aComponent, int aWidth)
	{

		Dimension dPref = aComponent.getPreferredSize();
		dPref.width = aWidth;

		Dimension dMin = new Dimension((int)(dPref.width*0.4), dPref.height);
		Dimension dMax = new Dimension((int)(dPref.width*1.2), dPref.height);

		aComponent.setPreferredSize(dPref);
		aComponent.setMinimumSize(dMin);
		aComponent.setMaximumSize(dMax);
	}
	
	/**
	 * 
	 * @param addToComponent
	 * @param aComponent
	 * @param row
	 * @param col
	 * @param spanrows
	 * @param spancols
	 * @param weightx
	 * @param padding
	 * @param anchor
	 */
	public static void addToGridBag(JComponent addToComponent, JComponent aComponent, 
							 int row, int col, int spanrows, int spancols, 
							 double weightx, int padding, int anchor)
	{
		c.gridx = col;
		c.gridy = row;
		c.weightx = weightx;
		c.anchor = anchor;

		if (c.insets.top == 0)
			c.insets = new Insets(padding,0,0,0);  //top padding

		c.gridheight = spanrows;
		c.gridwidth = spancols;

		addToComponent.add(aComponent, c);
		
		// Reset constraints
		c = new GridBagConstraints();
	}

	/**
	 * Returns the GridBagConstraints object
	 */
	public static GridBagConstraints getGridBagConstraints()
	{
		return c;
	}


}
