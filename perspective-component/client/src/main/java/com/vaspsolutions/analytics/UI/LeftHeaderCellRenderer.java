package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.vaspsolutions.analytics.common.Constants;

public class LeftHeaderCellRenderer extends DefaultTableCellRenderer {
	 public Component getTableCellRendererComponent(JTable table, 
			 Object obj,boolean isSelected, boolean hasFocus, int row, 
			 int column) {
		 javax.swing.border.Border leftPadding = BorderFactory.createEmptyBorder(0, 10, 0, 0);
		 super.getTableCellRendererComponent(table, obj, isSelected, hasFocus,
	                row, column);
			  // setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			   setBackground(Color.WHITE);
			   setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
			   setBorder(BorderFactory.createCompoundBorder(getBorder(), leftPadding));
			   setHorizontalAlignment(JLabel.LEFT);
			   setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			   setPreferredSize(new Dimension(700,25));
			   return this;
			   }
}
