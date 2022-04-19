package com.vaspsolutions.analytics.UI;


import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.vaspsolutions.analytics.common.Constants;

public class IA_Label extends JLabel {

	private Font lblFont;
	private void initialize(){
		lblFont =  new Font(Font.SANS_SERIF, Font.PLAIN, 11);
		
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setForeground(Constants.COLOR_BLACK_TEXT);
		this.setFont(lblFont);
	}
	public IA_Label() {
		super();
		initialize();
		
	}

	public IA_Label(Icon arg0, int arg1) {
		super(arg0, arg1);
		initialize();
	}

	public IA_Label(Icon arg0) {
		super(arg0);
		initialize();
	}

	public IA_Label(String arg0, Icon arg1, int arg2) {
		super(arg0, arg1, arg2);
		initialize();
	}

	public IA_Label(String arg0, int arg1) {
		super(arg0, arg1);
		initialize();
	}

	public IA_Label(String arg0) {
		super(arg0);
		initialize();
	}

	
}
