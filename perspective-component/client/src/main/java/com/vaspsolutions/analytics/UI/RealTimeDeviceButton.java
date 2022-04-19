package com.vaspsolutions.analytics.UI;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vaspsolutions.analytics.common.Constants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.SwingConstants;

public class RealTimeDeviceButton extends JPanel {
	public JPanel imgPanel;
	public JLabel lblBoldText;
	public JLabel lblNormalText;
	public RealTimeDeviceButton() {
		//this.setPreferredSize(new Dimension(50,40));
		this.setForeground(Color.WHITE);
		this.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{ 10};
		gridBagLayout.rowHeights = new int[]{30,22, 10};
		gridBagLayout.columnWeights = new double[]{Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0,0.0,0.0};
		setLayout(gridBagLayout);
		
		imgPanel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		imgPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		add(imgPanel, gbc_panel);
		imgPanel.setLayout(new BorderLayout(0, 0));
		
		lblBoldText = new JLabel("0");
		lblBoldText.setForeground(Color.WHITE);
		lblBoldText.setVerticalAlignment(SwingConstants.BOTTOM);
		lblBoldText.setVerticalTextPosition(JLabel.BOTTOM);
		lblBoldText.setFont(new Font("Tahoma", Font.BOLD, 16));
		GridBagConstraints gbc_lblBoldText = new GridBagConstraints();
		gbc_lblBoldText.insets = new Insets(0, 0, 0, 0);
		gbc_lblBoldText.anchor = GridBagConstraints.SOUTH;
		gbc_lblBoldText.gridx = 0;
		gbc_lblBoldText.gridy = 1;
		add(lblBoldText, gbc_lblBoldText);
		
		 lblNormalText = new JLabel("New label");
		 lblNormalText.setVerticalAlignment(SwingConstants.TOP);
		 lblNormalText.setVerticalTextPosition(JLabel.TOP);
		 lblNormalText.setForeground(Constants.COLOR_BUTTON_GREY_LABEL);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTH;
		gbc_lblNewLabel.insets = new Insets(0,0,5,0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		add(lblNormalText, gbc_lblNewLabel);
		
		
	}

}
