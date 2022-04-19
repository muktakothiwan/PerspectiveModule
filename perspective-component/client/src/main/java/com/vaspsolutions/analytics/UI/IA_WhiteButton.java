package com.vaspsolutions.analytics.UI;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.vaspsolutions.analytics.common.Constants;

public class IA_WhiteButton extends JButton {

	public IA_WhiteButton(String arg0) {
		super(arg0);
		//this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		this.setOpaque(true);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		this.setForeground(Constants.COLOR_BUTTON_GREY_LABEL);
		this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		
	}
	
	/*@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(this.getBackground());
		g2d.drawRoundRect(0,0,getWidth()-1,
			    getHeight()-1,18,18);
	}*/
	
}
