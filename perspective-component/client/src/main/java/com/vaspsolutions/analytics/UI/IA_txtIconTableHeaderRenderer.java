package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.vaspsolutions.analytics.common.Constants;

public class IA_txtIconTableHeaderRenderer extends DefaultTableCellRenderer {
	 public Component getTableCellRendererComponent(JTable table, 
			 Object obj,boolean isSelected, boolean hasFocus, int row, 
			 int column) {
		 javax.swing.border.Border rightPadding = BorderFactory.createEmptyBorder(0, 0, 0, 10);
		 IA_TextIcon i = (IA_TextIcon)obj;
			   if (obj == i) {
			  setIcon(i.headerIcon);
			  setText(i.headerText);
			   }
			  // setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			   setBackground(Color.WHITE);
			   setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
			   setBorder(BorderFactory.createCompoundBorder(getBorder(), rightPadding));
			   setHorizontalAlignment(JLabel.RIGHT);
			   setHorizontalTextPosition(JLabel.LEFT);
			   setPreferredSize(new Dimension(100,25));
			   setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			   return this;
			   }
}
