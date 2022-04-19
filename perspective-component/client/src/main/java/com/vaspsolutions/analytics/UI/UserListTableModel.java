package com.vaspsolutions.analytics.UI;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;



public class UserListTableModel extends AbstractTableModel {
	 
	
	List<IA_UserListPanel> userListData; 
	
	public UserListTableModel(List<IA_UserListPanel> userListData) {
		// TODO Auto-generated constructor stub
		this.userListData = userListData; 
	}
	
	public Class getColumnClass(int columnIndex){
		return IA_UserListPanel.class;
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
		return (userListData == null)? 0 : userListData.size();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return (userListData == null) ? null : userListData.get(arg0);
	}

}
