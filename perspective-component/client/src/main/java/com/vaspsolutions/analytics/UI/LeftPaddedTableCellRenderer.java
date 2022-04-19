package com.vaspsolutions.analytics.UI;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class LeftPaddedTableCellRenderer extends DefaultTableCellRenderer {

public int paddingSize;
	
    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
    	javax.swing.border.Border rightPadding = BorderFactory.createEmptyBorder(0, paddingSize, 0, 0);
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);
        
        setBorder(BorderFactory.createCompoundBorder(getBorder(), rightPadding));
        setHorizontalAlignment(JLabel.LEFT);
        return this;
    }
}
