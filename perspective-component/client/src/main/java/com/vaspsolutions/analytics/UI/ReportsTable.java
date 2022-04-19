package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.vaspsolutions.analytics.common.Constants;

public class ReportsTable extends JTable {

	
	
	public ReportsTable() {
		super();
		initTable();
		
	}

	public ReportsTable(TableModel dm) {
		super(dm);
		initTable();
	}
	
	@Override
	public Class<?> getColumnClass(int arg0) {
	
	//	if(arg0 == 2)
		if(arg0 > 0)
		{
			return Integer.class;
		}
		else
		{
			return super.getColumnClass(arg0);
		}
	}

	void initTable()
	{
		JTableHeader hdr = this.getTableHeader();
		this.setPreferredScrollableViewportSize(new Dimension(1500,850));
		this.setRowHeight(35);
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setForeground(Constants.COLOR_BLACK_TEXT);
		this.setEnabled(false);
		this.setAutoCreateRowSorter(false);
		this.setColumnSelectionAllowed(true);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		hdr.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		hdr.setForeground(Constants.COLOR_BLACK_TEXT);
		hdr.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTable#prepareRenderer(javax.swing.table.TableCellRenderer, int, int)
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row,
			int column) {
		Component comp = super.prepareRenderer(renderer, row, column);
		  //even index, selected or not selected
		
		  if (row % 2 == 0 ) {
		  comp.setBackground(Constants.COLOR_REPORT_ROW);
		  } 
		  else {
		  comp.setBackground(Color.WHITE);
		  }
		  return comp;
		  }
	

}
