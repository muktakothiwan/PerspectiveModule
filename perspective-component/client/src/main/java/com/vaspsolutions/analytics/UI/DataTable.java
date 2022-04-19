package com.vaspsolutions.analytics.UI;

import javax.swing.JTable;

import com.inductiveautomation.ignition.client.util.gui.table.DatasetTableModel;

@SuppressWarnings("serial")
public class DataTable extends JTable {

	public DataTable() {
		super();
		this.setModel(new DatasetTableModel());
	}
	
}
