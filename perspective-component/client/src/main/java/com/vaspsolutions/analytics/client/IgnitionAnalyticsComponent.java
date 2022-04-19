package com.vaspsolutions.analytics.client;

import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ModIALicenseManager;
import com.vaspsolutions.analytics.common.ModuleRPC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;


import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.gateway_interface.PushNotificationListener;
import com.inductiveautomation.ignition.common.gateway.messages.PushNotification;
import com.inductiveautomation.vision.api.client.components.model.AbstractVisionComponent;

/**
 * Main Ignition Analytics Component 
 * @author YM
 *
 */
 
public class IgnitionAnalyticsComponent extends AbstractVisionComponent 
	implements PropertyChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel mainPanel; //UI area below menu bar
	private String dsName;
	ModuleRPC rpc;

	public IgnitionAnalyticsComponent() {
//		System.setProperty("awt.useSystemAAFontSettings","on");
//		System.setProperty("swing.aatext", "true");

		setOpaque(false);
		Toolkit toolKit = Toolkit.getDefaultToolkit();
		
		int w = (int)toolKit.getScreenSize().getWidth();
		int h = (int)toolKit.getScreenSize().getHeight();
		
		this.setPreferredSize(new Dimension(1920,1080));
		//setSize(w,h);
		setFont(new Font("Dialog", Font.PLAIN, 24));
		this.setBackground(Color.WHITE);

		setBorder(new LineBorder(new Color(0, 0, 0)));
		setLayout(new GridLayout(0, 1));
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(1920,1080);
		//panel.setSize(w,h);
	
		mainPanel = new JPanel();
		panel.add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new GridLayout(0, 1));
		
		this.add(panel);
		if (ModIALicenseManager.getInstance().getLicenseExpired()) 
		{
	         //trial expired
			mainPanel.add(new JLabel("Trial expired"));
//			System.out.println("Trial expired");
			
		}
		else
		{
			rpc = ModuleRPCFactory.create(Constants.MODULE_ID, ModuleRPC.class);
			
			String sqlVersion = rpc.getSQLVersion();
			String errorMessage = "";
			boolean validSQLVersion = true;
			if(sqlVersion != null && sqlVersion.length() > 0)
			{
				if(sqlVersion.startsWith("5.5" ) || sqlVersion.startsWith("5.7" ) || sqlVersion.startsWith("6" ) || sqlVersion.startsWith("8" ) )
				{
					errorMessage = "";
					validSQLVersion = true;
				}
				else
				{
					errorMessage = "Module is supported on MySQL 5.5.x, 5.7.x, 6.x, 8.x and will not work correctly for : " 
							+ sqlVersion;
					validSQLVersion = false;
				}
			}
			else
			{
				errorMessage = "Can not connect to database or determine version. Please check database connection and restart.";
				validSQLVersion = false;
			}

			if(validSQLVersion)
			{
				String configuredDS = rpc.getPersistenceRecord();
				if(configuredDS == null)
				{
					this.dsName = "Please set";
				}
				else
				{
				
					this.dsName = configuredDS;
					this.mainPanel.add(new AnalysisPanel(rpc));
				}
		
				revalidate();
				repaint();
			}
			else
			{
				mainPanel.add(new JLabel(errorMessage));
			}
			
		}
	}

	/**
	 * @return the dsName
	 */
	public String getDsName() {
		return dsName;
	}



	/**
	 * @param dsName the dsName to set
	 */
	public void setDsName(String dsName) {
		String old = this.dsName;
		try
		{
			if(dsName != null )
			{
				this.dsName = dsName;
				firePropertyChange("dsName", old, this.dsName);
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//this.rpc.setModuleDS(dsName);
				this.rpc.createPersistenceRecord(this.dsName, this.dsName, "AUDIT_EVENTS", "ALARM_EVENTS");
//				this.rpc.createAndPopulateAuditDB(this.dsName, this.dsName, "AUDIT_EVENTS", "ALARM_EVENTS");
			//	this.rpc.executeTasksOnce();
				this.mainPanel.removeAll();
				this.mainPanel.add(new AnalysisPanel(this.rpc));
				revalidate();
				repaint();
				this.setCursor(Cursor.getDefaultCursor());
			}
		}catch(Exception e)
		{
			System.out.println("Error setting ds name. Please try again.");
		}
		
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
			System.out.println("in prop change : " + this.dsName) ;
			
	}



	
	
	@Override
	public void paint(Graphics g) {
		
	
		Graphics2D g2d = (Graphics2D)g;
		 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
			super.paint(g2d);
	}






	
}
