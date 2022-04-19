package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

import com.vaspsolutions.analytics.common.Constants;
/**
 * Class to have custom style for Combo box arrow button 
 * @author YM : Created 05/18/2015 
 * 
 *
 */
public class ComboArrowUI extends BasicComboBoxUI {

	JButton btn;
	protected void installDefaults() {
        super.installDefaults();
        comboBox.setPreferredSize(new Dimension(150,25));
        comboBox.setBackground(Constants.COLOR_COMBO_BACKGROUND);
        comboBox.setForeground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3)); // Make room for the extra rounded border
        comboBox.setFocusable(false); // Make sure it never get's the selection higlight
        // in this case the combobox should be opaque to make sure the background is painted. 
    }
 
	 public static ComboBoxUI createUI(JComponent c) {
         return new ComboArrowUI();
     }
	 @Override
	    public void update(Graphics g, JComponent c) {
	      
		 if (c.isOpaque()) {
			 
			 g.setColor(Constants.COLOR_COMBO_BACKGROUND);
	         g.fillRoundRect(0, 0, c.getWidth() ,c.getHeight() , 14, 14);
	         g.drawRoundRect(0, 0, c.getWidth() ,c.getHeight() , 14, 14);
		 }
	        paint(g, c);
	    }
	 
	 
	@Override
	public void paintCurrentValueBackground(Graphics g, Rectangle bounds,
			boolean hasFocus) {
		// TODO Auto-generated method stub
		//super.paintCurrentValueBackground(g, bounds, hasFocus);
		 final Color t = g.getColor();
         g.setColor(Constants.COLOR_COMBO_BACKGROUND);
         g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
         g.setColor(t);
	}

	@Override protected JButton createArrowButton() {
         btn = new BasicArrowButton(
                BasicArrowButton.SOUTH,
                Constants.COLOR_COMBO_BACKGROUND, Constants.COLOR_COMBO_BACKGROUND,
                Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_COMBO_BACKGROUND);
       //
       //btn.setBorder(BorderFactory.createLineBorder(Constants.COLOR_COMBO_BACKGROUND,1,true));
         btn.setBorder(new MatteBorder(0, 1, 0, 0,  Constants.COLOR_WHITE_BACKGROUND));
         
       return btn;
    }
	@Override
	public void paint(Graphics arg0, JComponent arg1) {
		// TODO Auto-generated method stub
		super.paint(arg0, arg1);
		//btn.setBackground(Color.ORANGE);
		//btn.setBorder(BorderFactory.createLineBorder(Constants.COLOR_COMBO_BACKGROUND));
	}
}
