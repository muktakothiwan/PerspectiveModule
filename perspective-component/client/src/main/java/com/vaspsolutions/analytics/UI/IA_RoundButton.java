package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JButton;

import com.vaspsolutions.analytics.common.Constants;

public class IA_RoundButton extends JButton {

	public Color _colr;
	public IA_RoundButton() {
		super();
		//Dimension size = getPreferredSize();
	    //size.width = size.height = Math.max(size.width,size.height);
	    setPreferredSize(new Dimension(12,12));
	   this.setOpaque(false);
	  //  setContentAreaFilled(false);
	}

	/* (non-Javadoc)
	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics arg0) {
		 arg0.setColor(_colr);
		 arg0.drawOval(0, 0, getSize().width-1,     getSize().height-1);
		super.paintBorder(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		 
		      g.setColor(_colr);
		
		    g.fillOval(0, 0, getSize().width-1,getSize().height-1);
		    g.drawOval(0, 0, getSize().width-1,getSize().height-1);
		super.paintComponent(g);
	}
	  Shape shape;
	  public boolean contains(int x, int y) {
	    if (shape == null || 
	      !shape.getBounds().equals(getBounds())) {
	      shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
	    }
	    return shape.contains(x, y);
	  }

}
