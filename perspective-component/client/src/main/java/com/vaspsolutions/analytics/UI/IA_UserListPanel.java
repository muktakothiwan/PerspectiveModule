package com.vaspsolutions.analytics.UI;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Font;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;

public class IA_UserListPanel extends JPanel {
	
	public JLabel userNameLbl;
	public JLabel timeSinceLastSeenLbl;
	public JPanel panel;
	String username;
	public JLabel staticImgLbl;
	public JLabel lblProfileName;
	public JLabel lblGatewayName;
	public IA_UserListPanel() {
		setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.WHITE);
		username = "Omkar";
		
//		setOpaque(false);
//		this.panel.setOpaque(false);
//		this.panelForGraph.setOpaque(false);
		
		setPreferredSize(new Dimension(230, 54));
		
		ImageIcon alarmsIcon = new ImageIcon(getClass().getResource("userPanelImg.png"));
		Image newAlarmImg = alarmsIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		ImageIcon newImgIcon = new ImageIcon(newAlarmImg);
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{56, 186, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 2;
		gbc_panel.insets = new Insets(8, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{1, 0};
		gbl_panel.rowHeights = new int[]{1, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		 staticImgLbl = new JLabel("");
		GridBagConstraints gbc_staticImgLbl = new GridBagConstraints();
		gbc_staticImgLbl.fill = GridBagConstraints.BOTH;
		gbc_staticImgLbl.gridx = 0;
		gbc_staticImgLbl.gridy = 0;
		panel.add(staticImgLbl, gbc_staticImgLbl);
		staticImgLbl.setIcon(newImgIcon);
		
		userNameLbl = new JLabel("");
		userNameLbl.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_userNameLbl = new GridBagConstraints();
		gbc_userNameLbl.fill = GridBagConstraints.BOTH;
		gbc_userNameLbl.insets = new Insets(0, 0, 5, 5);
		gbc_userNameLbl.gridx = 1;
		gbc_userNameLbl.gridy = 0;
		userNameLbl.setText(username);
		add(userNameLbl, gbc_userNameLbl);
		
		timeSinceLastSeenLbl = new JLabel("");
		timeSinceLastSeenLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_timeSinceLastSeenLbl = new GridBagConstraints();
		gbc_timeSinceLastSeenLbl.fill = GridBagConstraints.BOTH;
		gbc_timeSinceLastSeenLbl.insets = new Insets(0, 0, 5, 0);
		gbc_timeSinceLastSeenLbl.gridx = 2;
		gbc_timeSinceLastSeenLbl.gridy = 0;
		add(timeSinceLastSeenLbl, gbc_timeSinceLastSeenLbl);
		
		lblProfileName = new JLabel("");
		lblProfileName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblProfileName.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblProfileName = new GridBagConstraints();
		gbc_lblProfileName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblProfileName.insets = new Insets(0, 0, 0, 5);
		gbc_lblProfileName.gridx = 1;
		gbc_lblProfileName.gridy = 1;
		add(lblProfileName, gbc_lblProfileName);
		
		
		lblGatewayName = new JLabel("");
		lblGatewayName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblGatewayName.setHorizontalAlignment(SwingConstants.LEFT);
		lblGatewayName.setVisible(false);
		GridBagConstraints gbc_lblGatewayName = new GridBagConstraints();
		gbc_lblGatewayName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblGatewayName.insets = new Insets(0, 0, 0, 5);
		gbc_lblGatewayName.gridx = 2;
		gbc_lblGatewayName.gridy = 1;
		add(lblGatewayName, gbc_lblGatewayName);
		
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	
	public void setPanelData (String userName, boolean isSelected, JTable table){
		this.username = username;
		
		this.userNameLbl.setText(userName);
		
		if(isSelected){
			setBackground(Color.GRAY);
		}
		else 
			{setBackground(Color.white);}
	}
}
