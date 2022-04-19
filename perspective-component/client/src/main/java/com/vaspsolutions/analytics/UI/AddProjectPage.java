package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JLabel;

import com.vaspsolutions.analytics.common.Constants;

import java.awt.GridBagConstraints;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;

import java.awt.Color;

import javax.swing.border.MatteBorder;

import java.awt.BorderLayout;

import javax.swing.JButton;

public class AddProjectPage extends JPanel {
	public JScrollPane scrollPane;
	public JButton btnAdd;
	public JButton btnClose;
	public JButton btnCancel ;
	public JLabel labelTitle;
	public AddProjectPage() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, new Color(192, 192, 192), new Color(192, 192, 192), new Color(192, 192, 192), new Color(192, 192, 192)));
	//	this.setOpaque(false);
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{100, 15};
		gridBagLayout.rowHeights = new int[]{30, 200,20};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0};
		gridBagLayout.rowWeights = new double[]{Double.MIN_VALUE, 1.0, 1.0};
		setLayout(gridBagLayout);
		
		labelTitle = new JLabel("Add Project");
		labelTitle.setOpaque(true);
		labelTitle.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		labelTitle.setBorder(BorderFactory.createEmptyBorder());
		labelTitle.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		gbc_lblNewLabel.insets = new Insets(0, 10, 5, 5);
		add(labelTitle, gbc_lblNewLabel);
		
		ImageIcon closeIcon = new ImageIcon(getClass().getResource("close-window.png"));
		closeIcon = new ImageIcon(closeIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		btnClose = new JButton("");
		btnClose.setPreferredSize(new Dimension(10,20));
		btnClose.setIcon(closeIcon);
		btnClose.setOpaque(false);
		btnClose.setBorderPainted(false);
		btnClose.setFocusable(false);
		btnClose.setFocusPainted(false);
		btnClose.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		btnClose.setActionCommand(Constants.CMD_CLOSE_ADD_PROJECT_POPUP);
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.anchor = GridBagConstraints.EAST;
		gbc_btnClose.insets = new Insets(0, 0, 0, 0);
		gbc_btnClose.fill = GridBagConstraints.VERTICAL;
		gbc_btnClose.gridx = 1;
		gbc_btnClose.gridy = 0;
		add(btnClose, gbc_btnClose);
		
		JPanel middlePanel = new JPanel();
		middlePanel.setBorder(new MatteBorder(1, 0, 1, 0, (Color) Color.LIGHT_GRAY));
		middlePanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_middlePanel = new GridBagConstraints();
		gbc_middlePanel.gridwidth = 2;
		gbc_middlePanel.insets = new Insets(0, 0, 0, 0);
		gbc_middlePanel.fill = GridBagConstraints.BOTH;
		gbc_middlePanel.gridx = 0;
		gbc_middlePanel.gridy = 1;
		add(middlePanel, gbc_middlePanel);
		
		GridBagLayout gbl_middlePanel = new GridBagLayout();
		gbl_middlePanel.columnWidths = new int[]{100};
		gbl_middlePanel.rowHeights = new int[]{25,  175};
		gbl_middlePanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_middlePanel.rowWeights = new double[]{Double.MIN_VALUE, 1.0};
		middlePanel.setLayout(gbl_middlePanel);
		
		JLabel lblDescription = new JLabel("Select the project(s) to add to Ignition Analytics");
		lblDescription.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.insets = new Insets(5, 5, 5, 0);
		gbc_lblDescription.fill = GridBagConstraints.BOTH;
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 0;
		middlePanel.add(lblDescription, gbc_lblDescription);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0,0,0,0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		middlePanel.add(scrollPane, gbc_scrollPane);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(Constants.COLOR_WHITE_BACKGROUND);
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		
		JPanel btnsPanel = new JPanel();
		btnsPanel.setBorder(BorderFactory.createEmptyBorder());
		btnsPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_btnsPanel = new GridBagConstraints();
		gbc_btnsPanel.gridwidth = 2;
		gbc_btnsPanel.insets = new Insets(0,0,0,0);
		gbc_btnsPanel.fill = GridBagConstraints.BOTH;
		gbc_btnsPanel.gridx = 0;
		gbc_btnsPanel.gridy = 2;
		add(btnsPanel, gbc_btnsPanel);
		GridBagLayout gbl_btnsPanel = new GridBagLayout();
		gbl_btnsPanel.columnWidths = new int[]{305,41,10};
		gbl_btnsPanel.rowHeights = new int[]{10};
		gbl_btnsPanel.columnWeights = new double[]{Double.MIN_VALUE,0.1,0.1};
		gbl_btnsPanel.rowWeights = new double[]{ Double.MIN_VALUE};
		btnsPanel.setLayout(gbl_btnsPanel);
		
		 btnAdd = new JButton("Add");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.NORTH;
		gbc_btnAdd.gridx = 2;
		gbc_btnAdd.gridy = 0;
		
		Border b = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(69, 138, 201)));
		
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		btnsPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
//		b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
//		b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 1, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
//		b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 0, 0, 1, Constants.COLOR_WHITE_BACKGROUND));
//		
		
		btnAdd.setBorder(b);
		btnAdd.setActionCommand(Constants.CMD_ADD_PROJECT);
		btnAdd.setFocusable(false);
		btnAdd.setFocusPainted(false);
		btnAdd.setOpaque(false);
		btnsPanel.add(btnAdd, gbc_btnAdd);
		
		btnCancel = new JButton("Cancel");
		
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.NORTH;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		btnCancel.setBorder(b);
		btnCancel.setActionCommand(Constants.CMD_CANCEL_ADD_PROJECT);
		btnCancel.setFocusable(false);
		btnCancel.setFocusPainted(false);
		btnCancel.setOpaque(false);
		btnsPanel.add(btnCancel, gbc_btnCancel);
	}

}
