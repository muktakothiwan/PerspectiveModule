package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;

import javax.swing.border.MatteBorder;

import com.vaspsolutions.analytics.common.Constants;
/**
 * 
 * @author YM : Created on 06/26/2015.
 * A class to display user visits information
 *
 */
public class UserVisitsPanel extends JPanel {
	
	public JLabel lblUserName;
	public JPanel noOfVisits;
	public JLabel lblLastLoginTime;
	public JLabel lblLastScreenName;
	public UserVisitsPanel() {
		setBorder(new MatteBorder(0, 0, 1, 0, Constants.COLOR_MAIN_BACKGROUND));
		
		//this.setBackground(new Color(229, 233, 236));
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{108, 297};
		gridBagLayout.rowHeights = new int[]{12,12,16};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{ 0.0, 0.0, 0.0};
		setLayout(gridBagLayout);
		
		lblUserName = new JLabel("cccccc");
		lblUserName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblUserName = new GridBagConstraints();
		gbc_lblUserName.anchor = GridBagConstraints.WEST;
		gbc_lblUserName.insets = new Insets(2, 5, 2, 2);
		gbc_lblUserName.fill = GridBagConstraints.VERTICAL;
		gbc_lblUserName.gridx = 0;
		gbc_lblUserName.gridy = 0;
		add(lblUserName, gbc_lblUserName);
		
		lblLastLoginTime = new JLabel("");
		lblLastLoginTime.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblLastLoginTime = new GridBagConstraints();
		gbc_lblLastLoginTime.anchor = GridBagConstraints.EAST;
		gbc_lblLastLoginTime.insets = new Insets(2, 2, 2, 2);
		gbc_lblLastLoginTime.gridx = 1;
		gbc_lblLastLoginTime.gridy = 0;
		add(lblLastLoginTime, gbc_lblLastLoginTime);
		
		lblLastScreenName = new JLabel("");
		lblLastScreenName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblLastScreenName = new GridBagConstraints();
		gbc_lblLastScreenName.fill = GridBagConstraints.VERTICAL;
		gbc_lblLastScreenName.anchor = GridBagConstraints.WEST;
		gbc_lblLastScreenName.gridwidth = 2;
		gbc_lblLastScreenName.insets = new Insets(2, 5, 2, 2);
		gbc_lblLastScreenName.gridx = 0;
		gbc_lblLastScreenName.gridy = 1;
		add(lblLastScreenName, gbc_lblLastScreenName);
		
		JLabel lblNewLabel_1 = new JLabel("");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(2, 2, 2, 2);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		noOfVisits = new JPanel();
		GridBagConstraints gbc_noOfVisits = new GridBagConstraints();
		gbc_noOfVisits.fill = GridBagConstraints.BOTH;
		gbc_noOfVisits.gridx = 1;
		gbc_noOfVisits.gridy = 2;
		add(noOfVisits, gbc_noOfVisits);
		noOfVisits.setOpaque(false);
		noOfVisits.setLayout(new BorderLayout(0, 0));
	}

}
