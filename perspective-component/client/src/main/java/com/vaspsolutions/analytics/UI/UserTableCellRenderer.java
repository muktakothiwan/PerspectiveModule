package com.vaspsolutions.analytics.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.vaspsolutions.analytics.common.Constants;

public class UserTableCellRenderer extends DefaultTableCellRenderer  implements TableCellRenderer {
	 
	
	public static final DefaultTableCellRenderer    DEFAULT_RENDERER    = new DefaultTableCellRenderer();
	
	IA_UserListPanel userList;
	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		// TODO Auto-generated method stub
		
		 Component c = DEFAULT_RENDERER.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);

		
		
		
		
		try {
			userList = (IA_UserListPanel)arg1;
			userList.setBorder(new MatteBorder(0, 0, 1, 0, Constants.COLOR_GREY_LABEL));
			if (arg2){
				userList.userNameLbl.setForeground(Color.WHITE);
				userList.timeSinceLastSeenLbl.setForeground(Color.WHITE);
				userList.lblProfileName.setForeground(Color.WHITE);
				userList.lblGatewayName.setForeground(Color.WHITE);
				userList.setBackground(Constants.COLOR_BLUE_LABEL);
				userList.panel.setBackground(Constants.COLOR_BLUE_LABEL);
				//userList.panelForGraph.setBackground(Constants.COLOR_BLUE_LABEL);
			}else
			{
				userList.userNameLbl.setForeground(Color.BLACK);
				userList.timeSinceLastSeenLbl.setForeground(Color.BLACK);
				userList.lblProfileName.setForeground(Color.BLACK);
				userList.lblGatewayName.setForeground(Color.BLACK);
				userList.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				userList.panel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				//userList.panelForGraph.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			}
			//userList.userNameLbl.setText("Omkar");
			//log.error("Cell Renderer value is"+userList.username);
			
			//userList.setPanelData(name, arg2, arg0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.error(e);
		}
//		JPanel cellPanel = userList;
		
		//cellPanel.add(userList);
//		return cellPanel;
		return userList;
	}

}
