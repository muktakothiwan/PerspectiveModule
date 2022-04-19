package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.vaspsolutions.analytics.common.Constants;

public class IA_PanelLabel extends JLabel {

	public IA_PanelLabel(String arg0) {
		super(arg0);
		this.setFont(new Font("SansSerif", Font.BOLD, 12));
		this.setForeground(Constants.COLOR_GREY_LABEL);
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		
		this.setHorizontalAlignment(SwingConstants.LEFT);
	}

}
