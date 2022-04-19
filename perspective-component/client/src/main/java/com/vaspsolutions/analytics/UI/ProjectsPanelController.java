package com.vaspsolutions.analytics.UI;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.vaspsolutions.analytics.client.AnalysisPanel;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ModuleRPC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.CardLayout;

public class ProjectsPanelController extends JPanel implements ChangeListener{
	ModuleRPC rpc;
	JTabbedPane tabbedPane;
	public GatewaysDetailsPane _gatewayDetails ;
	public ProjectsDetailsPane _projectDetails ;
	public int currentTab = 1;
	String projectName;
	String gatewayName;
	public ProjectsPanelController(ModuleRPC _rpc, String projectName, AnalysisPanel _aPanel, String gatewayName, int tabNo) {
		super();
		this.rpc = _rpc;

	
		
		this.projectName  = projectName;
		this.gatewayName = gatewayName;
		System.out.println("ProjectsPanelController init gatewayname : " + gatewayName + ", projectname : " + projectName);
		this.setPreferredSize(new Dimension(1720,1080));
		this.setBackground(Constants.COLOR_MAIN_BACKGROUND);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{1720};
		gridBagLayout.rowHeights = new int[]{83,500,220};
		gridBagLayout.columnWeights = new double[]{0.0};
		gridBagLayout.rowWeights = new double[]{0.5,0.0,0.5};
		setLayout(gridBagLayout);
		
		 this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		//tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
	//	tabbedPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BLACK_TEXT));
		tabbedPane.setPreferredSize(new Dimension(1720,500));
		
		_gatewayDetails = new GatewaysDetailsPane(_rpc, _aPanel, gatewayName);
		
		ImageIcon _projects = new ImageIcon(getClass().getResource("ProjectsPaneTabIcon.png"));
		Image newImg = _projects.getImage().getScaledInstance(300, 25, Image.SCALE_SMOOTH);
		_projects = new ImageIcon(newImg);
		System.out.println("ProjectsDetailsPane called from ProjectsPanelController");
//		log.error("ProjectsDetailsPane called from ProjectsPanelController");
		_projectDetails = new ProjectsDetailsPane(_rpc, _aPanel, this.projectName, this.gatewayName, false);
		//pass isAGent as false as we are calling from controller.
		
		tabbedPane.setFont( new Font( "SansSerif", Font.BOLD,14 ) );
		tabbedPane.addTab("",  _projects, new JPanel());
		tabbedPane.addTab("Gateways", _gatewayDetails);
		tabbedPane.addTab("Projects", _projectDetails);
		//tabbedPane.addTab("<html><p style=\"color:LightGray\"><b>Goals</b></p></html>", new JPanel());
		tabbedPane.addTab("Goals", new JPanel());
		tabbedPane.addTab("Notifications", new JPanel());
		tabbedPane.addTab("Scheduled Emails", new JPanel());
		tabbedPane.setForegroundAt(2, Color.GRAY);
		tabbedPane.setForegroundAt(3, Color.GRAY);
		tabbedPane.setForegroundAt(4, Color.GRAY);
		tabbedPane.setForegroundAt(5, Color.GRAY);
		tabbedPane.addChangeListener(this);
		tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		tabbedPane.setSelectedIndex(tabNo);
		this.currentTab = tabNo;
		//tabbedPane.setEnabledAt(2, false);
		
	//	tabbedPane.setAlignmentX(CENTER_ALIGNMENT);
		//tabbedPane.setForegroundAt(0, Constants.COLOR_WHITE_BACKGROUND);
		   UIManager.getDefaults().put("TabbedPane.lightHighlight",  Constants.COLOR_GREY_LABEL);
		   UIManager.getDefaults().put("TabbedPane.highlight",  Constants.COLOR_WHITE_BACKGROUND);
	       UIManager.getDefaults().put("TabbedPane.selectHighlight", Constants.COLOR_BLUE_LABEL);
	       UIManager.getDefaults().put("TabbedPane.background", Constants.COLOR_MAIN_BACKGROUND);
	       UIManager.getDefaults().put("TabbedPane.tabAreaBackground", Constants.COLOR_MAIN_BACKGROUND);
	       UIManager.getDefaults().put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
	       UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
	       UIManager.getDefaults().put("TabbedPane.selected", Constants.COLOR_WHITE_BACKGROUND);
	       UIManager.getDefaults().put("TabbedPane.shadow", Constants.COLOR_BLUE_LABEL);
	       UIManager.getDefaults().put("TabbedPane.focus", Constants.COLOR_BLUE_LABEL);
		tabbedPane.setUI(new BasicTabbedPaneUI(){
//			@Override
//			   protected void installDefaults() {
//			       super.installDefaults();
//			       
//			    
//			       
//			   }
			@Override
	        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
	        }
		});
	//	tabbedPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.COLOR_BLUE_LABEL));
		tabbedPane.setBorder(BorderFactory.createEmptyBorder());
		
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		add(tabbedPane, gbc_tabbedPane);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		int selIndx = this.tabbedPane.getSelectedIndex();
		if(selIndx == 1)
		{
			currentTab = 1;
			tabbedPane.setForegroundAt(1, Color.BLACK);
			tabbedPane.setForegroundAt(2, Color.GRAY);
			tabbedPane.setForegroundAt(3, Color.GRAY);
			tabbedPane.setForegroundAt(4, Color.GRAY);
			tabbedPane.setForegroundAt(5, Color.GRAY);
		}
		else if (selIndx == 2 )
		{
			currentTab = 2;
			tabbedPane.setForegroundAt(2, Color.BLACK);
			tabbedPane.setForegroundAt(1, Color.GRAY);
			tabbedPane.setForegroundAt(3, Color.GRAY);
			tabbedPane.setForegroundAt(4, Color.GRAY);
			tabbedPane.setForegroundAt(5, Color.GRAY);
		}
		else if(selIndx > 2)
		{
			tabbedPane.setSelectedIndex(currentTab);
			JOptionPane.showMessageDialog(this, "Available with premium license.");
			
		}
		
	}

}
