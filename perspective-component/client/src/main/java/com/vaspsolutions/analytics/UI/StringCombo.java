package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

@SuppressWarnings("serial")
public class StringCombo extends JComboBox<String>   {

	
	@SuppressWarnings("unchecked")
	public StringCombo() {
		super();
	this.setBackground(Color.GRAY);	
	this.setUI(new ComboArrowUI());
	
	}

	public StringCombo(String[] arg0) {
		super(arg0);
		
	}

	
}
