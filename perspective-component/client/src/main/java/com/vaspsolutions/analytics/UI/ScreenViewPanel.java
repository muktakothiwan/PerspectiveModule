package com.vaspsolutions.analytics.UI;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.Image;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;

public class ScreenViewPanel extends JPanel {
	public JLabel userNameLbl;
	public JLabel lblViewed;
	public JLabel screenNameLbl;
	public JLabel lblHoursAgo;
	public JLabel actionLbl;
	public JLabel valueLbl;
	public ScreenViewPanel() {
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.WHITE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{155, 175,  170,170,75, 5, 100};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		ImageIcon docIcon = new ImageIcon(getClass().getResource("docIcon.png"));
		Image docImg = docIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		ImageIcon newDocIcon = new ImageIcon(docImg);
		
		userNameLbl = new JLabel("UserName");
		userNameLbl.setBackground(Color.WHITE);
		userNameLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_userNameLbl = new GridBagConstraints();
		gbc_userNameLbl.anchor = GridBagConstraints.WEST;
		gbc_userNameLbl.fill = GridBagConstraints.VERTICAL;
		gbc_userNameLbl.insets = new Insets(0, 0, 0, 5);
		gbc_userNameLbl.gridx = 0;
		gbc_userNameLbl.gridy = 0;
		add(userNameLbl, gbc_userNameLbl);
		
		lblViewed = new JLabel("viewed");
		lblViewed.setBackground(Color.WHITE);
		GridBagConstraints gbc_lblViewed = new GridBagConstraints();
		gbc_lblViewed.anchor = GridBagConstraints.WEST;
		gbc_lblViewed.fill = GridBagConstraints.VERTICAL;
		gbc_lblViewed.insets = new Insets(0, 0, 0, 5);
		gbc_lblViewed.gridx = 1;
		gbc_lblViewed.gridy = 0;
		add(lblViewed, gbc_lblViewed);
		
		screenNameLbl = new JLabel("ScreenName");
		screenNameLbl.setBackground(Color.WHITE);
		screenNameLbl.setForeground(new Color(30, 144, 255));
		screenNameLbl.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_screenNameLbl = new GridBagConstraints();
		gbc_screenNameLbl.anchor = GridBagConstraints.WEST;
		gbc_screenNameLbl.fill = GridBagConstraints.VERTICAL;
		gbc_screenNameLbl.insets = new Insets(0, 0, 0, 5);
		gbc_screenNameLbl.gridx = 2;
		gbc_screenNameLbl.gridy = 0;

		add(screenNameLbl, gbc_screenNameLbl);
		
		actionLbl = new JLabel("Action");
		actionLbl.setBackground(Color.WHITE);
		actionLbl.setForeground(new Color(30, 144, 255));
		actionLbl.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_actionLbl = new GridBagConstraints();
		gbc_actionLbl.anchor = GridBagConstraints.WEST;
		gbc_actionLbl.fill = GridBagConstraints.VERTICAL;
		gbc_actionLbl.insets = new Insets(0, 0, 0, 5);
		gbc_actionLbl.gridx = 3;
		gbc_actionLbl.gridy = 0;
		add(actionLbl, gbc_actionLbl);
		
		valueLbl = new JLabel("Value");
		valueLbl.setBackground(Color.WHITE);
		valueLbl.setForeground(new Color(30, 144, 255));
		valueLbl.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_valueLbl = new GridBagConstraints();
		gbc_valueLbl.anchor = GridBagConstraints.WEST;
		gbc_valueLbl.fill = GridBagConstraints.VERTICAL;
		gbc_valueLbl.insets = new Insets(0, 0, 0, 5);
		gbc_valueLbl.gridx = 4;
		gbc_valueLbl.gridy = 0;
		add(valueLbl, gbc_valueLbl);
		
		lblHoursAgo = new JLabel("16 Hours ago");
		lblHoursAgo.setBackground(Color.WHITE);
		GridBagConstraints gbc_lblHoursAgo = new GridBagConstraints();
		gbc_lblHoursAgo.insets = new Insets(0, 0, 0, 5);
		gbc_lblHoursAgo.anchor = GridBagConstraints.EAST;
		gbc_lblHoursAgo.fill = GridBagConstraints.VERTICAL;
		gbc_lblHoursAgo.gridx = 5;
		gbc_lblHoursAgo.gridy = 0;
		add(lblHoursAgo, gbc_lblHoursAgo);
	}

}
