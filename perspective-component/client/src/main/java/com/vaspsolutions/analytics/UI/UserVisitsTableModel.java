package com.vaspsolutions.analytics.UI;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UserVisitsTableModel extends AbstractTableModel {

	 
	
	List<UserVisitsPanel> userVistsData;
	public UserVisitsTableModel(List<UserVisitsPanel> userVistsData) {
		super();
		this.userVistsData = userVistsData;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int getRowCount() {
		return (userVistsData == null)? 0 : userVistsData.size();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		return (userVistsData == null) ? null : userVistsData.get(arg0);
	}
	
	@Override
	public Class getColumnClass(int columnIndex){
		return UserVisitsPanel.class;
	}

}
