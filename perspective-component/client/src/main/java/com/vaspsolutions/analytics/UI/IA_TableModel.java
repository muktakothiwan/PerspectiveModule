package com.vaspsolutions.analytics.UI;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/*
 * Class to use for Table shown on Projects Page
 * Created by YM : 06/05/2015
 * 
 */
public class IA_TableModel extends AbstractTableModel {

	private Object[] _columnNames = {"Project", "Users", "Sessions", "Avg. Session Duration", "Actions", "Remove"};
	private Vector data = new Vector();
	
	@Override
	public int getColumnCount() {
		
		return _columnNames.length;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return ((Vector) data.get(rowIndex)).get(columnIndex);
	}
	
	@Override
	public Class getColumnClass(int c) {
         return getValueAt(0, c).getClass();
     }
	
	@Override
	public String getColumnName(int column){
		return (String) _columnNames[column];
		}
	@Override
	public boolean isCellEditable(int row, int col){
		 
		boolean retVal  = false;
		if (col < 6){
			retVal = false; 
		 }
		 else 
		 {
			
			 	 retVal = true;
			
		 }
		return retVal;
	}
	
	
	public void addRowData(Object[] row){
		data.add(new Vector());
		 for(int i =0; i<row.length; i++){
		 ((Vector) data.get(data.size()-1)).add(row[i]);
		 }
		 fireTableDataChanged();
	}
	
	Vector getTableData(){
		return this.data;
	}

	public Object[] get_columnNames() {
		return _columnNames;
	}

	public void set_columnNames(Object[] _columnNames) {
		this._columnNames = _columnNames;
	}

}
