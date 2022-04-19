package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ScreenViewCellRenderer implements TableCellRenderer {

	ScreenViewPanel screenViewPanel;
	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		// TODO Auto-generated method stub
		
		screenViewPanel = (ScreenViewPanel)arg1;
		screenViewPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		return screenViewPanel;
	}

}
