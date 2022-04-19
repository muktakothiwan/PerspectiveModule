package com.vaspsolutions.analytics.UI;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class AddNewProjectTableModel extends AbstractTableModel {

	private Object[] _columnNames = {"Project Name", "Add"};
	private Vector data = new Vector();
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
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
		 
		if(col == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	public void addRowData(Object[] row){
		data.add(new Vector());
		 for(int i =0; i<row.length; i++){
		 ((Vector) data.get(data.size()-1)).add(row[i]);
		 }
		 fireTableDataChanged();
	}
	
	@Override
	public void setValueAt(Object value, int row, int col){
		 ((Vector) data.get(row)).setElementAt(value, col);
		
		 fireTableCellUpdated(row,col);
		}
	Vector getTableData(){
		return this.data;
	}

}
