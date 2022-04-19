package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.vaspsolutions.analytics.common.Constants;


public class OrangeText extends JLabel {

	public OrangeText() {
		super();
		this.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		this.setForeground(Constants.COLOR_ORANGE_TEXT);
		this.setHorizontalAlignment(SwingConstants.RIGHT);
		this.setOpaque(false);
		this.setBorder(new EmptyBorder(1,1,1,1));
		
	}

	

}
