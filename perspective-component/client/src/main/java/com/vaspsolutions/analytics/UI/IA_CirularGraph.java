package com.vaspsolutions.analytics.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.vaspsolutions.analytics.common.Constants;

public class IA_CirularGraph extends BasicProgressBarUI {
	 
	 @Override public Dimension getPreferredSize(JComponent c) {
	        Dimension d = super.getPreferredSize(c);
	        int v = Math.max(d.width, d.height);
	        d.setSize(v, v);
	        return d;
	    }
	
	@Override public void paint(Graphics g, JComponent c) {
	    
			
	        Insets b = progressBar.getInsets(); // area for border
	        int barRectWidth  = progressBar.getWidth()  - b.right - b.left;
	        int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
	        if (barRectWidth <= 0 || barRectHeight <= 0) {
	            return;
	        }

	        Graphics2D g2 = (Graphics2D) g.create();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	        double degree = 360 * progressBar.getPercentComplete();
	      //  double sz = Math.min(barRectWidth, barRectHeight);
	        double sz = 160;
	        double cx = b.left + barRectWidth  * .5;
	        double cy = b.top  + barRectHeight * .5;
	      
	        double or = sz * .5;
	      //  double or = sz;
	        double ir = or * .9; //.8;
	        
	        
	        Shape inner  = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2, ir * 2);
	        Shape outer  = new Ellipse2D.Double(cx - or, cy - or, sz, sz);
	        Shape sector = new Arc2D.Double(cx - or, cy - or, sz, sz, 90 - degree, degree, Arc2D.PIE);

	        Area foreground = new Area(sector);
	        Area background = new Area(outer);
	        Area hole = new Area(inner);

	        foreground.subtract(hole);
	        background.subtract(hole);
	        
	        // draw the track
	        
	        g2.setPaint(new Color(77,83,88));
	        g2.fill(background);

	        g2.setPaint(progressBar.getForeground());
	        g2.fill(foreground);
	        

	        
//	        if (progressBar.isStringPainted()) {
//	            paintString(g, b.left, b.bottom, barRectWidth, barRectHeight, 0, b);
//	        }
	        
	        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
		    g2.setStroke(new BasicStroke(1.5f));
	 	    g2.setPaint(Constants.COLOR_WHITE_BACKGROUND);
	 	   g2.drawString("" + progressBar.getValue(), (int)(cx - 10), (int)(cy));
	    	 g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		 	    g2.setPaint(Constants.COLOR_GREY_LABEL);
		    	
		    	g2.drawString("Users Online" , (int)(cx - 35), (int)(cy +  13));
		    	g2.dispose();
	    }
}
