package com.vaspsolutions.analytics.UI;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;

import com.vaspsolutions.analytics.common.Constants;

public class UserVisitsTableCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		UserVisitsPanel uservisit = (UserVisitsPanel)arg1; 
	//	uservisit.setBorder(BorderFactory.createEmptyBorder());
		uservisit.setBorder(new MatteBorder(0, 0, 1, 0, Constants.COLOR_MAIN_BACKGROUND));
		//_ret.setViewportView(sessionData);
		return uservisit;
	}

}
