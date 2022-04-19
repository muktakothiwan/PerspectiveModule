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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;

public class RenameGatewayPage extends JPanel {
	public JButton btnAdd;
	public JButton btnClose;
	public JButton btnCancel ;
	public JLabel labelTitle;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	public JComboBox<String> comboBoxOldName;
	public JComboBox<String> comboBoxNewName;
	public RenameGatewayPage() {
		setBorder(new BevelBorder(BevelBorder.LOWERED, new Color(192, 192, 192), new Color(192, 192, 192), new Color(192, 192, 192), new Color(192, 192, 192)));
	//	this.setOpaque(false);
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{420, 20};
		gridBagLayout.rowHeights = new int[]{20, 90,20};
		gridBagLayout.columnWeights = new double[]{0.9, 0.1};
		gridBagLayout.rowWeights = new double[]{0.4, 0.2, 0.4};
		setLayout(gridBagLayout);
		
		labelTitle = new JLabel("Rename Gateway");
		labelTitle.setOpaque(true);
		labelTitle.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		labelTitle.setBorder(BorderFactory.createEmptyBorder());
		labelTitle.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblRenameLabel = new GridBagConstraints();
		gbc_lblRenameLabel.anchor = GridBagConstraints.WEST;
		gbc_lblRenameLabel.gridx = 0;
		gbc_lblRenameLabel.gridy = 0;
		gbc_lblRenameLabel.insets = new Insets(5, 10, 5, 5);
		add(labelTitle, gbc_lblRenameLabel);
		
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
		btnClose.setActionCommand(Constants.CMD_CLOSE_RENAME_GATEWAY_POPUP);
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.anchor = GridBagConstraints.EAST;
		gbc_btnClose.insets = new Insets(5, 0, 5, 5);
		gbc_btnClose.fill = GridBagConstraints.BOTH;
		gbc_btnClose.gridx = 1;
		gbc_btnClose.gridy = 0;
		add(btnClose, gbc_btnClose);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		gbc_panel.gridwidth = 2;
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		add(panel, gbc_panel);
		
		GridBagLayout gbl_Panel = new GridBagLayout();
		gbl_Panel.columnWidths = new int[]{100,100};
		gbl_Panel.rowHeights = new int[]{10,10};
		gbl_Panel.columnWeights = new double[]{0.5,0.5};
		gbl_Panel.rowWeights = new double[]{ 0.3,0.7};
		panel.setLayout(gbl_Panel);
		
		lblNewLabel = new JLabel("Select Old Gateway Name");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		lblNewLabel_1 = new JLabel("Select New Gateway Name");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblNewLabel_1.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		comboBoxOldName = new JComboBox<String>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(5, 5, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		panel.add(comboBoxOldName, gbc_comboBox);
		
		comboBoxNewName = new JComboBox<String>();
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(5, 5, 5, 5);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		panel.add(comboBoxNewName, gbc_comboBox_1);
		
		
		
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
		gbl_btnsPanel.columnWidths = new int[]{100,100};
		gbl_btnsPanel.rowHeights = new int[]{10};
		gbl_btnsPanel.columnWeights = new double[]{0.5,0.5};
		gbl_btnsPanel.rowWeights = new double[]{ Double.MIN_VALUE};
		btnsPanel.setLayout(gbl_btnsPanel);
		
		 btnAdd = new JButton("Rename");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		
		Border b = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(69, 138, 201)));
		btnAdd.setBorder(b);
		btnAdd.setActionCommand(Constants.CMD_SAVE_RENAME_GATEWAY);
		btnAdd.setFocusable(false);
		btnAdd.setFocusPainted(false);
		btnAdd.setOpaque(false);
		btnsPanel.add(btnAdd, gbc_btnAdd);
		
		btnCancel = new JButton("Cancel");
		
		
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		btnCancel.setBorder(b);
		btnCancel.setActionCommand(Constants.CMD_CANCEL_RENAME_GATEWAY);
		btnCancel.setFocusable(false);
		btnCancel.setFocusPainted(false);
		btnCancel.setOpaque(false);
		btnsPanel.add(btnCancel, gbc_btnCancel);
		
		
		
	}

}
