package com.vaspsolutions.analytics.UI;


import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import com.vaspsolutions.analytics.common.Constants;

@SuppressWarnings("serial")
public class HistoryTable extends JTable {

	
	public HistoryTable(int tableType) {
		super();
	
		DefaultTableModel model = new DefaultTableModel(0, 2);
		
		
		switch(tableType)
		{
		case Constants.TABLE_ALARMS_NUMBERS:
			model.setColumnIdentifiers(new Object[] {"Priority", "Total Number"});
			break;
		case Constants.TABLE_ALARMS_TIME:
			model.setColumnIdentifiers(new Object[] {"Priority", "Time"});
			break;
		case Constants.TABLE_USERS_ACTIONS:
			model.setColumnIdentifiers(new Object[] {"Users", "Total Number"});
			break;
		case Constants.TABLE_USERS_DURATION:
			model.setColumnIdentifiers(new Object[] {"Users", "Duration"});
			break;
		}
		
		
		this.setModel(model);
		this.setAutoCreateRowSorter(true);
		
	}


	
}
