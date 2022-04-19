package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.GridBagLayout;

import javax.swing.JTable;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Color;

public class SessionDislayPanel extends JScrollPane {
	public JTable table;
	public SessionDislayPanel(JTable screenViewTable) {
		setBackground(Color.WHITE);
//		GridBagLayout gridBagLayout = new GridBagLayout();
//		gridBagLayout.columnWidths = new int[]{0, 0};
//		gridBagLayout.rowHeights = new int[]{0, 0};
//		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
//		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
//		setLayout(gridBagLayout);
		
		table = screenViewTable;
//		GridBagConstraints gbc_table = new GridBagConstraints();
//		gbc_table.fill = GridBagConstraints.BOTH;
//		gbc_table.gridx = 0;
//		gbc_table.gridy = 0;
//		add(table, gbc_table);
		
		table.setPreferredScrollableViewportSize(new Dimension(1000,120));
		this.setViewportView(table);
		this.setAutoscrolls(true);
		
	}

}
