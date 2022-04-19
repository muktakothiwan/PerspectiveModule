package com.vaspsolutions.analytics.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.vaspsolutions.analytics.common.Constants;

import java.awt.BorderLayout;

public class RoundWhiteButtonBlueBorder extends JPanel {
	protected Dimension arcs = new Dimension(15, 15);
	  public JLabel lblboldtext;
	 
	public RoundWhiteButtonBlueBorder() {
		setOpaque(false);
		setLayout(new BorderLayout(0, 0));
		lblboldtext = new JLabel("");
		lblboldtext.setFont(new Font("SansSerif", Font.BOLD, 16));
		lblboldtext.setForeground(Constants.COLOR_BLUE_LABEL);
		lblboldtext.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblboldtext, BorderLayout.CENTER);
		
		
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
      graphics.setColor(Constants.COLOR_WHITE_BACKGROUND);
      graphics.fillRoundRect(0, 0, width - shadowGap, 
		height - shadowGap, arcs.width, arcs.height);
      graphics.setColor(Constants.COLOR_BLUE_LABEL);
      graphics.setStroke(new BasicStroke(1));
      graphics.drawRoundRect(0, 0, width - shadowGap, 
		height - shadowGap, arcs.width, arcs.height);

      //Sets strokes to default, is better.
      graphics.setStroke(new BasicStroke());
  }
	
}
