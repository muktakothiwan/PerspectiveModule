package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class AlarmSummaryUpperTableHeaderRenderer extends
		DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, 
			 Object obj,boolean isSelected, boolean hasFocus, int row, 
			 int column) {
		 javax.swing.border.Border leftPadding = BorderFactory.createEmptyBorder(0, 10, 0, 0);
		 super.getTableCellRendererComponent(table, obj, isSelected, hasFocus,
	                row, column);
			  // setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			   setBackground(Color.WHITE);
			   setHorizontalAlignment(JLabel.CENTER);
			   setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			   setForeground(new Color(49,156,214));
			   setPreferredSize(new Dimension(100,25));
			   return this;
			   }
}
