package com.vaspsolutions.analytics.UI;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ScreenViewTableModel extends AbstractTableModel {

	List<ScreenViewPanel> screenViewData;
	public ScreenViewTableModel(List<ScreenViewPanel> screenViewData1) {
		// TODO Auto-generated constructor stub
		this.screenViewData = screenViewData1; 
	}
	
	public Class getColumnClass(int columnIndex){
		return ScreenViewPanel.class;
	}
	
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
//		log.error("Table row count is : " + userListData.size());
//		IA_UserListPanel one = userListData.get(4);
//		String test = "Fourth value from the array list is " + one.username.toString();
//		log.error(test);
		return (screenViewData == null)? 0 : screenViewData.size();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return (screenViewData == null) ? null : screenViewData.get(arg0);
	}
}
