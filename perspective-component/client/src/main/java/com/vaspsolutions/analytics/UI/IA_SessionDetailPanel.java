package com.vaspsolutions.analytics.UI;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.util.List;

import javax.swing.JTable;

import com.vaspsolutions.analytics.common.Constants;

import java.awt.GridLayout;

import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.SwingConstants;

public class IA_SessionDetailPanel extends JPanel {
	public JTable table;
	public JLabel sessionDateLbl;
	public JLabel sessionLocationLbl;
	public JLabel sessionFlagLbl;
	public JLabel sessionBrowserLbl;
	public JLabel sessionOSLbl;
	public JPanel panel;
	
	public List<ScreenViewPanel> screenViewData;
	public JScrollPane displaySessionTablePanel;
	public JLabel useNameLbl;
	public JLabel screenNameLbl;
	public IA_SessionDetailPanel() {
		setBorder(new MatteBorder(0, 0, 1, 0, (Color) Color.LIGHT_GRAY));
		setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setBackground(Constants.COLOR_MAIN_BACKGROUND);
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		sessionDateLbl = new JLabel("");
		sessionDateLbl.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		sessionDateLbl.setFont(new Font("Tahoma", Font.PLAIN, 14));
		sessionDateLbl.setForeground(new Color(139, 0, 0));
		GridBagConstraints gbc_sessionDateLbl = new GridBagConstraints();
		gbc_sessionDateLbl.insets = new Insets(0, 0, 0, 5);
		gbc_sessionDateLbl.fill = GridBagConstraints.BOTH;
		gbc_sessionDateLbl.gridx = 0;
		gbc_sessionDateLbl.gridy = 0;
		panel.add(sessionDateLbl, gbc_sessionDateLbl);
		
		sessionLocationLbl = new JLabel("");
		sessionLocationLbl.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_sessionLocationLbl = new GridBagConstraints();
		gbc_sessionLocationLbl.insets = new Insets(0, 0, 0, 5);
		gbc_sessionLocationLbl.fill = GridBagConstraints.HORIZONTAL;
		gbc_sessionLocationLbl.gridx = 10;
		gbc_sessionLocationLbl.gridy = 0;
		panel.add(sessionLocationLbl, gbc_sessionLocationLbl);
		
		sessionFlagLbl = new JLabel("");
		sessionFlagLbl.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_sessionFlagLbl = new GridBagConstraints();
		gbc_sessionFlagLbl.insets = new Insets(0, 0, 0, 5);
		gbc_sessionFlagLbl.fill = GridBagConstraints.HORIZONTAL;
		gbc_sessionFlagLbl.gridx = 11;
		gbc_sessionFlagLbl.gridy = 0;
		panel.add(sessionFlagLbl, gbc_sessionFlagLbl);
		
		sessionBrowserLbl = new JLabel("");
		sessionBrowserLbl.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_sessionBrowserLbl = new GridBagConstraints();
		gbc_sessionBrowserLbl.insets = new Insets(0, 0, 0, 5);
		gbc_sessionBrowserLbl.gridx = 12;
		gbc_sessionBrowserLbl.gridy = 0;
		panel.add(sessionBrowserLbl, gbc_sessionBrowserLbl);
		
		sessionOSLbl = new JLabel("");
		sessionOSLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		sessionOSLbl.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_sessionOSLbl = new GridBagConstraints();
		gbc_sessionOSLbl.gridx = 13;
		gbc_sessionOSLbl.gridy = 0;
		panel.add(sessionOSLbl, gbc_sessionOSLbl);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new MatteBorder(0, 0, 1, 0, (Color) Color.LIGHT_GRAY));
		panel_1.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		add(panel_1, BorderLayout.SOUTH);
		
		displaySessionTablePanel = new JScrollPane();
		displaySessionTablePanel.setBorder(BorderFactory.createEmptyBorder());
		displaySessionTablePanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		displaySessionTablePanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		displaySessionTablePanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		displaySessionTablePanel.setViewportView(null);
		add(displaySessionTablePanel, BorderLayout.CENTER);
		//displaySessionTablePanel.setLayout(new BorderLayout(0, 0));
		
//		useNameLbl = new JLabel("New label");
//		GridBagConstraints gbc_useNameLbl = new GridBagConstraints();
//		gbc_useNameLbl.insets = new Insets(0, 0, 0, 5);
//		gbc_useNameLbl.fill = GridBagConstraints.HORIZONTAL;
//		gbc_useNameLbl.gridx = 0;
//		gbc_useNameLbl.gridy = 1;
//		displaySessionTablePanel.add(useNameLbl, gbc_useNameLbl);
//		
//		screenNameLbl = new JLabel("New label");
//		GridBagConstraints gbc_screenNameLbl = new GridBagConstraints();
//		gbc_screenNameLbl.fill = GridBagConstraints.HORIZONTAL;
//		gbc_screenNameLbl.gridx = 1;
//		gbc_screenNameLbl.gridy = 1;
//		displaySessionTablePanel.add(screenNameLbl, gbc_screenNameLbl);
		
		
		
//		table = new JTable(new ScreenViewTableModel(screenViewData));
//		displaySessionTablePanel.add(table);
//		table.setRowHeight(40);
//		table.setDefaultRenderer(ScreenViewPanel.class, new ScreenViewCellRenderer());
	}
	
//	public void paintTableScreenView (List<ScreenViewPanel> screenViewData1){
//		System.out.println("In Paint table method");
//		screenViewData = screenViewData1;
//		table = new JTable(new ScreenViewTableModel(screenViewData));
//		table.setRowHeight(40);
//		table.setTableHeader(null);
//		table.setDefaultRenderer(ScreenViewPanel.class, new ScreenViewCellRenderer());
//		displaySessionTablePanel.add(table);
//		
//		JPanel panel_1 = new JPanel();
//		add(panel_1, BorderLayout.SOUTH);
//	}

}
