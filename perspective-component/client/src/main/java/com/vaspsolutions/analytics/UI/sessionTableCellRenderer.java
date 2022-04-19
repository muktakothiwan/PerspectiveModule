package com.vaspsolutions.analytics.UI;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class sessionTableCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		// TODO Auto-generated method stub
		//JScrollPane _ret = new JScrollPane();
	
		IA_SessionDetailPanel sessionData = (IA_SessionDetailPanel)arg1; 
		//sessionData.setBorder(BorderFactory.createEmptyBorder());
		//_ret.setViewportView(sessionData);
		return sessionData;
		//return _ret;
	}

}
