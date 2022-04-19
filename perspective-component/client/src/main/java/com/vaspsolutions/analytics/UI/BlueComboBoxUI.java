package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class BlueComboBoxUI extends BasicComboBoxUI {
	 @Override protected JButton createArrowButton() {
	       BasicArrowButton basic =  new BasicArrowButton(
	            BasicArrowButton.SOUTH,
	            Color.white, Color.white,
	            new Color(0,191,255), Color.white);
	       ImageIcon imgDownArrow = new ImageIcon(getClass().getResource("hollowCircle.png"));
			Image img = imgDownArrow.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
			ImageIcon newDown = new ImageIcon(img);
			basic.setBackground(Color.WHITE);
			basic.setBorder(BorderFactory.createEmptyBorder());
			basic.setFocusPainted(false);
			basic.setIcon(newDown);
	        return basic;
	    }
}
