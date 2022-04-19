package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
/**
 * Class to implement Traffic - Users online circular graph
 * @author YM
 *
 */
public class IA_TrafficPanel extends JPanel  {

	public JProgressBar usersOnline = new JProgressBar(){
		@Override public void updateUI(){
			
			this.setPreferredSize(new Dimension(200,200));
			this.setStringPainted(false);
			super.updateUI();
			setUI(new IA_CirularGraph());
			setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		}
	};
	public IA_TrafficPanel() {
		super();
		
		this.setBackground(Color.DARK_GRAY);
				setLayout(new BorderLayout(0, 0));
				usersOnline.setOpaque(false);
				
				usersOnline.setForeground(new Color(116, 180, 218)); //light blue
				//usersOnline.setValue(100);
				usersOnline.setMinimum(0);
				usersOnline.setMaximum(40);
				usersOnline.setOrientation(JProgressBar.VERTICAL);
				add(usersOnline, BorderLayout.NORTH);
	}

	
	
	
}
