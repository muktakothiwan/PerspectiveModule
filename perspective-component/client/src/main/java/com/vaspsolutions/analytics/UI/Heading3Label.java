package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Font;




import javax.swing.JLabel;

@SuppressWarnings("serial")
public class Heading3Label extends JLabel {

	
	public Heading3Label() {
		super();
		setProperties();
	}

	public Heading3Label(String text) {
		super(text);
		
		setProperties();
	}
	
	private void setProperties() {
		this.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
		this.setBackground(Color.WHITE);
	}

}
