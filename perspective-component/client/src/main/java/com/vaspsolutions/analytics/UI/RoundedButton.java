package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.vaspsolutions.analytics.common.Constants;

import java.awt.Font;

public class RoundedButton extends JPanel {
	  protected Dimension arcs = new Dimension(20, 20);
	  public JLabel lblboldtext;
	  public JLabel lblnormaltext;
	public RoundedButton() {
		setLayout(new GridLayout(0,1));
		setOpaque(false);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblboldtext = new JLabel("");
		lblboldtext.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblboldtext.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblboldtext);
		
		 lblnormaltext = new JLabel("");
		lblnormaltext.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblnormaltext.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblnormaltext);
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        int shadowGap = 1;
        Graphics2D graphics = (Graphics2D) g;

       
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
    			RenderingHints.VALUE_ANTIALIAS_ON);
        //Draws the rounded opaque panel with borders.
        graphics.setColor(getBackground());
        graphics.fillRoundRect(0, 0, width - shadowGap, 
		height - shadowGap, arcs.width, arcs.height);
        graphics.setColor(getBackground());
        graphics.setStroke(new BasicStroke(1));
        graphics.drawRoundRect(0, 0, width - shadowGap, 
		height - shadowGap, arcs.width, arcs.height);

        //Sets strokes to default, is better.
        graphics.setStroke(new BasicStroke());
    }
	public void setTextColor(Color _color)
	{
		this.lblboldtext.setForeground(_color);
		this.lblnormaltext.setForeground(_color);
	
	}


}
