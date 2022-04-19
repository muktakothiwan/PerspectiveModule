package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.SwingConstants;

import java.awt.Font;

public class RoundedButton_WideLabel extends JPanel {
	
	  protected Dimension arcs = new Dimension(20, 20);
	  JLabel lblUp;
	  JLabel lblMiddleRight;
	  JLabel lblMiddleLeft;
	  JLabel lblLowerLeft;
	  JLabel lblLowerRight;
	public RoundedButton_WideLabel() {
		
		this.setOpaque(false);
		int w = this.getWidth();
		int h = this.getHeight();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{w/2, w/2};
		gridBagLayout.rowHeights = new int[]{h/4,h/2, h/4};
		gridBagLayout.columnWeights = new double[]{0.5,0.5};
		gridBagLayout.rowWeights = new double[]{0.33,0.33, 0.0};
		setLayout(gridBagLayout);
		
		lblUp = new JLabel("");
		lblUp.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblUp.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblUp = new GridBagConstraints();
		gbc_lblUp.fill = GridBagConstraints.VERTICAL;
		gbc_lblUp.gridwidth = 2;
		gbc_lblUp.insets = new Insets(0, 0, 5, 0);
		gbc_lblUp.gridx = 0;
		gbc_lblUp.gridy = 0;
		add(lblUp, gbc_lblUp);
		
		lblMiddleRight = new JLabel("");
		lblMiddleRight.setHorizontalAlignment(SwingConstants.CENTER);
		lblMiddleRight.setFont(new Font("Tahoma", Font.BOLD, 18));
		GridBagConstraints gbc_lblMiddleRight = new GridBagConstraints();
		gbc_lblMiddleRight.insets = new Insets(0, 0, 5, 0);
		gbc_lblMiddleRight.gridx = 1;
		gbc_lblMiddleRight.gridy = 1;
		add(lblMiddleRight, gbc_lblMiddleRight);
		
		lblMiddleLeft = new JLabel("");
		lblMiddleLeft.setHorizontalAlignment(SwingConstants.CENTER);
		lblMiddleLeft.setFont(new Font("Tahoma", Font.BOLD, 18));
		GridBagConstraints gbc_lblMiddleLeft = new GridBagConstraints();
		gbc_lblMiddleLeft.insets = new Insets(0, 0, 5, 5);
		gbc_lblMiddleLeft.gridx = 0;
		gbc_lblMiddleLeft.gridy = 1;
		add(lblMiddleLeft, gbc_lblMiddleLeft);
		
		lblLowerLeft = new JLabel("");
		lblLowerLeft.setVerticalAlignment(SwingConstants.TOP);
		lblLowerLeft.setHorizontalAlignment(SwingConstants.CENTER);
		lblLowerLeft.setFont(new Font("SansSerif", Font.PLAIN, 11));
		GridBagConstraints gbc_lblLowerLeft = new GridBagConstraints();
		gbc_lblLowerLeft.insets = new Insets(0, 0, 0, 5);
		gbc_lblLowerLeft.gridx = 0;
		gbc_lblLowerLeft.gridy = 2;
		add(lblLowerLeft, gbc_lblLowerLeft);
		
		lblLowerRight = new JLabel("");
		lblLowerRight.setVerticalAlignment(SwingConstants.TOP);
		lblLowerRight.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblLowerRight.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblLowerRight = new GridBagConstraints();
		gbc_lblLowerRight.gridx = 1;
		gbc_lblLowerRight.gridy = 2;
		add(lblLowerRight, gbc_lblLowerRight);
		
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
	
	public void setTextColor(Color _upcolor, Color _otherColor)
	{
		this.lblLowerLeft.setForeground(_otherColor);
		this.lblLowerRight.setForeground(_otherColor);
		this.lblUp.setForeground(_upcolor);
		this.lblMiddleLeft.setForeground(_otherColor);
		this.lblMiddleRight.setForeground(_otherColor);
	}

}
