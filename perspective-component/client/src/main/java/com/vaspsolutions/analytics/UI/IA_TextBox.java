package com.vaspsolutions.analytics.UI;


import java.awt.Font;

import javax.swing.JTextField;

public class IA_TextBox extends JTextField {
	
	private Font txtFont;
	public IA_TextBox() {
		super();
		
		
		setTxtFont();
		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setAlignmentY(RIGHT_ALIGNMENT);
		//this.setPreferredSize(new Dimension(10, 30));
		this.setSize(30, 10);
		
		// TODO Auto-generated constructor stub
	}

	public IA_TextBox(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public IA_TextBox(String arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public IA_TextBox(String arg0) {
		super(arg0);
		setTxtFont();		
		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setAlignmentY(RIGHT_ALIGNMENT);
		
		// TODO Auto-generated constructor stub
	}

	public Font getTxtFont() {
		return txtFont;
	}

	public void setTxtFont() {
		Font newFont =  new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		this.txtFont = newFont;
	}

}
