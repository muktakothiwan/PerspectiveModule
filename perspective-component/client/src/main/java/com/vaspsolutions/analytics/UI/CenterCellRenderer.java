package com.vaspsolutions.analytics.UI;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CenterCellRenderer extends DefaultTableCellRenderer {
	int paddingSize;
	 @Override
	    public Component getTableCellRendererComponent(JTable table,
	            Object value, boolean isSelected, boolean hasFocus,
	            int row, int column){
		 javax.swing.border.Border rightPadding = BorderFactory.createEmptyBorder(0, 0, 0, paddingSize);
	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
	                row, column);
	        setBorder(BorderFactory.createCompoundBorder(getBorder(), rightPadding));
	        setHorizontalAlignment(JLabel.CENTER);
	        return this;
	}
}
