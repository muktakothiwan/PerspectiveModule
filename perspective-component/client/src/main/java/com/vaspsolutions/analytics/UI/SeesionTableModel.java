package com.vaspsolutions.analytics.UI;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SeesionTableModel extends AbstractTableModel {

	 
	
	List<IA_SessionDetailPanel> sessionTableData;
	
	public SeesionTableModel(List<IA_SessionDetailPanel> sessionData) {
		// TODO Auto-generated constructor stub
		this.sessionTableData = sessionData; 
		
	}
	
	public Class getColumnClass(int columnIndex){
		return IA_SessionDetailPanel.class;
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
		return (sessionTableData == null)? 0 : sessionTableData.size();
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
	//	log.error("Object value in Session table model : " + sessionTableData.get(arg0).getComponentCount()) ;
		return (sessionTableData == null) ? null : sessionTableData.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return true;
	}
	
	

}
