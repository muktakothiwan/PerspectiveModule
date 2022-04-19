package com.vaspsolutions.analytics.UI;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class UsersButton extends JButton {

	public UsersButton() {
		super();
		InitButtonStyle();
	}

	public UsersButton(Action arg0) {
		super(arg0);
		InitButtonStyle();
	}

	public UsersButton(Icon arg0) {
		super(arg0);
		InitButtonStyle();
	}

	public UsersButton(String arg0, Icon arg1) {
		super(arg0, arg1);
		InitButtonStyle();
	}

	public UsersButton(String arg0) {
		super(arg0);
		InitButtonStyle();
	}

	private void InitButtonStyle()
	{
		 this.setForeground(Color.BLACK);
		 this.setBackground(Color.WHITE);
		 this.setOpaque(true);
		 this.setVerticalAlignment(SwingConstants.CENTER);
		 this.setHorizontalAlignment(SwingConstants.LEFT);
		 this.setVerticalTextPosition(SwingConstants.CENTER);
		 this.setHorizontalTextPosition(SwingConstants.CENTER);
		// this.setBorder(new EmptyBorder(1, 1, 1, 1));
		 this.setBorderPainted(false);
		  
	}
}
