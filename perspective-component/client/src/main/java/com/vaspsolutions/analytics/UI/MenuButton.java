package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.vaspsolutions.analytics.common.Constants;

public class MenuButton extends JButton {

	public MenuButton() {
		super();
		InitButtonStyle();
	}

	public MenuButton(String text, Icon icon) {
		super(text, icon);
		InitButtonStyle();
	}
	public MenuButton(String text) {
		super(text);
		InitButtonStyle();
	}
	
	private void InitButtonStyle()
	{
		 this.setForeground(Color.WHITE);
		 this.setBackground(new Color(59, 70, 78));
		 this.setOpaque(false);
		 this.setVerticalAlignment(SwingConstants.CENTER);
		// this.setHorizontalAlignment(SwingConstants.LEFT);
	//	 this.setVerticalTextPosition(SwingConstants.CENTER);
	//	 this.setHorizontalTextPosition(JButton.RIGHT);
		// this.setBorder(new EmptyBorder(1, 1, 1, 1));
		 this.setBorderPainted(false);
		 this.setFocusPainted(false);
		// this.setPreferredSize(new Dimension(200,35));
		 
		 
		 
		this.setHorizontalAlignment(AbstractButton.LEADING);
			this.setHorizontalTextPosition(AbstractButton.CENTER);
			this.setVerticalTextPosition(AbstractButton.CENTER);
			this.setIconTextGap(0);
			
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		  
	}

}
