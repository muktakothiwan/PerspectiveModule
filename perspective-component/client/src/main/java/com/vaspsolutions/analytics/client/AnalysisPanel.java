package com.vaspsolutions.analytics.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.inductiveautomation.factorypmi.application.script.builtin.SecurityUtilities;
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager;
import com.inductiveautomation.ignition.client.gateway_interface.PushNotificationListener;
import com.inductiveautomation.ignition.common.gateway.messages.PushNotification;
import com.vaspsolutions.analytics.UI.DashboardPanel;
import com.vaspsolutions.analytics.UI.DashboardPanelController;
import com.vaspsolutions.analytics.UI.IA_LeftMenuPanel;
import com.vaspsolutions.analytics.UI.IA_LeftMenuPanelController;
import com.vaspsolutions.analytics.UI.IA_TableModel;
import com.vaspsolutions.analytics.UI.ProjectsPanel;
import com.vaspsolutions.analytics.UI.ProjectsPanelController;
import com.vaspsolutions.analytics.UI.RealTimePanel;
import com.vaspsolutions.analytics.UI.RealTimePanelController;
import com.vaspsolutions.analytics.UI.ReportsPanel;
import com.vaspsolutions.analytics.UI.ReportsPanelController;
import com.vaspsolutions.analytics.UI.UserPanel;
import com.vaspsolutions.analytics.UI.UserPanelController;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ModuleRPC;
//import com.incors.plaf.alloy.cu;

public class AnalysisPanel extends JPanel implements ActionListener, PushNotificationListener {
	
	/**
	 * THis is place holder to show Analysis information
	 */
	private static final long serialVersionUID = 1L;
	ModuleRPC rpc;
	private String newDSName; //datasource to create our audit tables 
	private String existingAuditDSName;
	private String existingAuditTable;
	private String existingAlarmsTable;
	private String selectedProject ="All Projects";
	private String selectedGateway = "All Gateways";
	private boolean allProjects = true;
	private boolean allGateways = false;
	DashboardPanel _mPanel = null; //shows dashboard
	public IA_LeftMenuPanel _menu; //shows left menu
	ReportsPanel _reports = null; //holds reports information
	RealTimePanel _realTime = null; //placehlder for real time information
	ProjectsPanel _projectsPanel = null;
	UserPanel _userPanel = null;
	JTable projectsList;	
	IA_TableModel _projectsListModel;

	JPanel contentPanel; //main Panel 
	int currentView; //to make a note of current view by user (Dashboard, Real Time, reports etc) Used when user changes the project.
	ImageIcon dashboardHighlightIcon;
	ImageIcon realTimeHighlightIcon;
	ImageIcon projectsHighlightIcon;
	ImageIcon usersHighlightIcon;
	ImageIcon reportsHighlightIcon;
	ImageIcon dashboardNavigationIcon;
	ImageIcon realTimeNavigationIcon;
	ImageIcon projectsNavigationIcon;
	ImageIcon usersNavigationIcon;
	ImageIcon reportsNavigationIcon;
	
	//for COntroller
	public IA_LeftMenuPanelController _menuController;
	public DashboardPanelController _mPanelController = null;
	public ProjectsPanelController _projectsPanelController = null;
	public UserPanelController _userPanelController = null;
	public ReportsPanelController _reportsController = null; //holds reports information on controller
	public RealTimePanelController _realTimeController = null;
	
	boolean isAgent = false;
	boolean isEnterprise = false;
	public AnalysisPanel(ModuleRPC _rpc){
		setBorder(BorderFactory.createEmptyBorder());

		
		
		Toolkit toolKit = Toolkit.getDefaultToolkit();
		
		int w = (int)toolKit.getScreenSize().getWidth();
		int h = (int)toolKit.getScreenSize().getHeight();
		
		this.setPreferredSize(new Dimension(1920, 1080));
	//	this.setPreferredSize(new Dimension(w, h));
		//get an instance of RPC module 
		this.rpc = _rpc;
		this.setBackground(Constants.COLOR_MAIN_BACKGROUND);
		//retrieve the module configuration parameters.
		getModuleConfiguration();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{200, 1720};
		gridBagLayout.rowHeights = new int[]{1080};
		gridBagLayout.columnWeights = new double[]{0.5,0.5};
		gridBagLayout.rowWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		
		//panel to hold menu bar
		JPanel menuPanel = new JPanel();
		menuPanel.setPreferredSize(new Dimension(200,1080));
		menuPanel.setBorder(BorderFactory.createEmptyBorder());
		//menuPanel.setOpaque(false);
		menuPanel.setBackground(Constants.COLOR_MENU_BACKGROUND);
		GridBagConstraints gbc_menuPanel = new GridBagConstraints();
		gbc_menuPanel.fill = GridBagConstraints.BOTH;
		gbc_menuPanel.insets = new Insets(0, 0, 0, 0);
		gbc_menuPanel.gridx = 0;
		gbc_menuPanel.gridy = 0;
		add(menuPanel, gbc_menuPanel);
		
		
		//init icons
		
		projectsNavigationIcon = new ImageIcon(getClass().getResource("Projects--Navigation.png"));
		projectsNavigationIcon = new ImageIcon(projectsNavigationIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH));
	
		dashboardNavigationIcon = new ImageIcon(getClass().getResource("Dashboard--Navigation.png"));
		dashboardNavigationIcon = new ImageIcon(dashboardNavigationIcon.getImage().getScaledInstance(200,30, Image.SCALE_SMOOTH));
	
		realTimeNavigationIcon = new ImageIcon(getClass().getResource("Rel-Time--Navigation.png"));
		realTimeNavigationIcon = new ImageIcon(realTimeNavigationIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH));
	
		usersNavigationIcon = new ImageIcon(getClass().getResource("Users--Navigation.png"));
		usersNavigationIcon = new ImageIcon(usersNavigationIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH));
	
		reportsNavigationIcon = new ImageIcon(getClass().getResource("Reports--Navigation.png"));
		reportsNavigationIcon = new ImageIcon(reportsNavigationIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH));
	
		projectsHighlightIcon = new ImageIcon(getClass().getResource("Projects-Highlight.png"));
		projectsHighlightIcon = new ImageIcon(projectsHighlightIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH));
	
		dashboardHighlightIcon = new ImageIcon(getClass().getResource("Dashboard-Highlight.png"));
		dashboardHighlightIcon = new ImageIcon(dashboardHighlightIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH));
	
		realTimeHighlightIcon = new ImageIcon(getClass().getResource("Real-Time-Highlight.png"));
		realTimeHighlightIcon = new ImageIcon(realTimeHighlightIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH));
	
		usersHighlightIcon = new ImageIcon(getClass().getResource("Users-Highlight.png"));
		usersHighlightIcon = new ImageIcon(usersHighlightIcon.getImage().getScaledInstance(200, 30,Image.SCALE_SMOOTH));
	
		reportsHighlightIcon = new ImageIcon(getClass().getResource("Reports-Highlight.png"));
		reportsHighlightIcon = new ImageIcon(reportsHighlightIcon.getImage().getScaledInstance(200, 30,Image.SCALE_SMOOTH));
	
		//panel to show all content
		contentPanel = new JPanel();
		contentPanel.setPreferredSize(new Dimension(1720,1080));
		contentPanel.setOpaque(false);
		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.insets = new Insets(0,0,0,0);
		gbc_contentPanel.fill = GridBagConstraints.BOTH;
		gbc_contentPanel.gridx = 1;
		gbc_contentPanel.gridy = 0;
		add(contentPanel, gbc_contentPanel);
		contentPanel.setBackground(Constants.COLOR_MENU_BACKGROUND);
		contentPanel.setLayout(new GridLayout(0,1));
		
		menuPanel.setLayout(new BorderLayout(0, 0));
		
		//construct the left side menu
		this.isAgent = this.rpc.getIfAgent();
		this.isEnterprise = this.rpc.getIfEnterprise();
		if(this.isEnterprise == true && this.isAgent == false)
		{
			_menuController = new IA_LeftMenuPanelController();
			
			Font fontDeopDown = new Font("Tahoma", Font.PLAIN, 11);
			//fontDeopDown;
			_menuController.comboProjects.setFont(fontDeopDown);
			_menuController.comboProjects.setForeground(Color.WHITE);
		
			_menuController.comboGateways.setFont(fontDeopDown);
			_menuController.comboGateways.setForeground(Color.WHITE);
			this._menuController.lblUserNameLbl.setText(" User : " + SecurityUtilities.getUsername()); 
		
			//code to update time on left menu bar every 1 min
		
			Timer timer = new Timer();
		
			timer.scheduleAtFixedRate( new TimerTask(){
				public void run(){
					Date curDate = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm aaa");
					_menuController.lblDateTimeLbl.setText(" Time : " + sdf.format(curDate));
					revalidate();
					repaint();
				}
			}, (long)0 , (long)500 );
		
		
			_menuController.btnProjects.setActionCommand(Constants.ControllerbtnProjects_Click_Action);
			_menuController.btnProjects.addActionListener(this);
		
			_menuController.btnRealTime.setActionCommand(Constants.ControllerbtnRealTime_Click_Action);
			_menuController.btnRealTime.addActionListener(this);
		
			_menuController.btnDashboard.setActionCommand(Constants.ControllerbtnDashboard_Click_Action);
			_menuController.btnDashboard.addActionListener(this);
			
			_menuController.btnReports.setActionCommand(Constants.ControllerbtnReport_Click_Action);
			_menuController.btnReports.addActionListener(this);
				
			_menuController.btnUsers.setActionCommand(Constants.ControllerbtnUsers_Click_Action);
			_menuController.btnUsers.addActionListener(this);
				
			_menuController.comboProjects.setActionCommand(Constants.Controllerprojectslist_Selection_Change);
			_menuController.comboProjects.addActionListener(this);
		
			_menuController.btnLogout.setActionCommand(Constants.btnLogout_Click_Action);
			_menuController.btnLogout.addActionListener(this);	
			
			_menuController.comboGateways.setActionCommand(Constants.Controllergatewayslist_Selection_Change);
			_menuController.comboGateways.addActionListener(this);
			
			//get list of gateways
			String[] _gateways = rpc.getGateways();
			if(_gateways != null)
			{
				_menuController.comboGateways.setModel(new DefaultComboBoxModel<String>(_gateways));
			}
			
			String[] _projects = rpc.getProjectsOnGateway("All Gateways", "All Projects");
			if(_projects != null)
			{
				_menuController.comboProjects.setModel(new DefaultComboBoxModel<String>(_projects));
			}
			
			
			menuPanel.add(_menuController, BorderLayout.CENTER);
			_menuController.btnDashboard.setIcon(dashboardHighlightIcon);
					
		
			//add main analysis content for 1st project in the list on startup
			this.allGateways = true;
			this.allProjects = true;
			this.selectedGateway = "All Gateways";
			this.selectedProject = "All Projects";
			_mPanelController = new DashboardPanelController(rpc,"All Gateways", "All Projects", this.newDSName, 0, 0);
			
			contentPanel.add(_mPanelController);
			currentView = Constants.VIEW_DASHOARD;
		}
		else
		{
			_menu = new IA_LeftMenuPanel();
		
			Font fontDeopDown = new Font("Tahoma", Font.PLAIN, 11);
			//fontDeopDown;
			_menu.comboProjects.setFont(fontDeopDown);
			_menu.comboProjects.setForeground(Color.WHITE);
		
		
			this._menu.lblUserNameLbl.setText(" User : " + SecurityUtilities.getUsername()); 
		
			//code to update time on left menu bar every 1 min
		
			Timer timer = new Timer();
		
			timer.scheduleAtFixedRate( new TimerTask(){
				public void run(){
					Date curDate = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm aaa");
					_menu.lblDateTimeLbl.setText(" Time : " + sdf.format(curDate));
					revalidate();
					repaint();
				}
			}, (long)0 , (long)500 );
		
		
			_menu.btnProjects.setActionCommand(Constants.btnProjects_Click_Action);
			_menu.btnProjects.addActionListener(this);
		
			_menu.btnRealTime.setActionCommand(Constants.btnRealTime_Click_Action);
			_menu.btnRealTime.addActionListener(this);
		
			_menu.btnDashboard.setActionCommand(Constants.btnDashboard_Click_Action);
			_menu.btnDashboard.addActionListener(this);
			
			_menu.btnReports.setActionCommand(Constants.btnReport_Click_Action);
			_menu.btnReports.addActionListener(this);
				
			_menu.btnUsers.setActionCommand(Constants.btnUsers_Click_Action);
			_menu.btnUsers.addActionListener(this);
				
			_menu.comboProjects.setActionCommand(Constants.projectslist_Selection_Change);
			_menu.comboProjects.addActionListener(this);
		
			_menu.btnLogout.setActionCommand(Constants.btnLogout_Click_Action);
			_menu.btnLogout.addActionListener(this);		
			String[] _projects = rpc.getProjects("All Projects");
			if(_projects != null)
			{
				_menu.comboProjects.setModel(new DefaultComboBoxModel<String>(_projects));
			}
			
			
			menuPanel.add(_menu, BorderLayout.CENTER);
			_menu.btnDashboard.setIcon(dashboardHighlightIcon);
					
		
			this.allProjects = true;
			//add main analysis content for 1st project in the list on startup
			_mPanel = new DashboardPanel(rpc,"All Projects", this.newDSName, 0, 0);
			
			contentPanel.add(_mPanel);
			currentView = Constants.VIEW_DASHOARD;
		

		}
				
		//register to receive notification from gateway
				
		GatewayConnectionManager.getInstance().addPushNotificationListener(this);
	}
	
	/**
	 * Method to retrieve module configuration from Persistent record and store values in class variables
	 */
	private void getModuleConfiguration(){
		
		String modParams = rpc.getPersistenceRecord();
		
		if(modParams != null){
			String[] params = modParams.split(",");
			this.newDSName = params[0].trim();
		//	this.existingAuditDSName = params[1].trim();
		//	this.existingAuditTable = params[2].trim();
		//	this.existingAlarmsTable = params[3].trim();
		}
		else
		{
			JOptionPane.showMessageDialog(this,"Analytics module not configured. Cannot proceed.");
		}
	}

	/**
	 * 
	 * Method to refresh UI at timed interval
	 */
	
	private void refreshUI()
	{
		if(!this.isEnterprise || this.isAgent)
		{
			if(currentView == Constants.VIEW_DASHOARD)
			{
				if(this._mPanel == null)
				{
					this._mPanel = new DashboardPanel(rpc,_menu.comboProjects.getSelectedItem().toString(),newDSName, 0, 0);
				}
				else
				{
					if(this._mPanel.currentProject.compareToIgnoreCase("All Projects") == 0)
					{
						this._mPanel.populateData(_mPanel.currentDuration , null, true);
					}
					else
					{
						this._mPanel.populateData(_mPanel.currentDuration , _mPanel.currentProject, false);
					}
				}
			}
			else if(currentView == Constants.VIEW_REPORTS)
			{
				/* date 03-Oct-2016 */
		    	if(this._reports == null)
		    	{
		    		this._reports = new ReportsPanel(rpc, _menu.comboProjects.getSelectedItem().toString(), newDSName);
		    	}
		    	else
		    	{
		    		//refresh only in case of alarms
		    		if(this._reports.selectedReportMenu.compareToIgnoreCase("Alarm Summary") == 0)
		    		{
		    			this._reports.refreshReport(_menu.comboProjects.getSelectedItem().toString());
		    		}
		    	}
			}
			else if(currentView == Constants.VIEW_REALTIME)
			{
				if(this._realTime == null)
				{
					this._realTime = new RealTimePanel(rpc, _menu.comboProjects.getSelectedItem().toString(), this.newDSName, 0);
	    		
				}
				else
				{
					this._realTime.populateData(Constants.TODAY);
				}
			}
			else if(currentView == Constants.VIEW_PROJECTS)
			{
				if(_projectsPanel == null)
				{
					this._projectsPanel = new ProjectsPanel(rpc,_menu.comboProjects.getSelectedItem().toString(), this );
				}
				else
				{
					this._projectsPanel._projectDetails.populateData(_projectsPanel._projectDetails.selectedDuration, null, null);
				}
			}
			else if(currentView == Constants.VIEW_USERS)
			{
			//	this._userPanel = new UserPanel(rpc, this.newDSName,_menu.comboProjects.getSelectedItem().toString() );
				//this._userPanel.userPanelPopulate();
				
	
				
				this._userPanel.populateUsersList();
				this._userPanel.userPanelPopulate();
			}
		}
		else
		{
			//when on controller
			if(currentView == Constants.VIEW_DASHOARD)
			{
				if(_mPanelController == null)
				{
					_mPanelController = new DashboardPanelController(rpc,_menuController.comboGateways.getSelectedItem().toString(), _menuController.comboProjects.getSelectedItem().toString().trim(),this.newDSName, 0, 0);
				}
				else
				{
					
					if(this._mPanelController.allProjects && this._mPanelController.allGateways) //all gateways , all projects
					{
						this._mPanelController.populateData(this._mPanelController.currentDuration , null, null, true);
					}
					else if(!(this._mPanelController.allGateways) && this._mPanelController.allProjects) //single gateway , all projects
					{
						this._mPanelController.populateData(this._mPanelController.currentDuration , this._mPanelController.currentGateway, null, true);
					}
					else if(!(this._mPanelController.allGateways) && !(this._mPanelController.allProjects)) //single gateway single project
					{
						this._mPanelController.populateData(this._mPanelController.currentDuration , this._mPanelController.currentGateway, this._mPanelController.currentProject, false);
					}
					else if(this._mPanelController.allGateways && !(this._mPanelController.allProjects)) //Ignore the gateway and only select based on project
					{
						this._mPanelController.populateData(this._mPanelController.currentDuration , null, this._mPanelController.currentProject, false);
					}
					
				}
			}
			else if(currentView == Constants.VIEW_REPORTS)
			{
				if(_reportsController == null)
				{
					_reportsController = new ReportsPanelController(rpc, _menuController.comboGateways.getSelectedItem().toString(), _menuController.comboProjects.getSelectedItem().toString(), newDSName);
				}
				else
				{
					this._reportsController.refreshReport( _menuController.comboGateways.getSelectedItem().toString(),_menuController.comboProjects.getSelectedItem().toString());
				}
			}
			else if(currentView == Constants.VIEW_REALTIME)
			{
				System.out.println("Real Time refresh is called at : " + new Date());
				if(_realTimeController == null)
				{
					_realTimeController = new RealTimePanelController(rpc, _menuController.comboProjects.getSelectedItem().toString(), this.newDSName, 0, _menuController.comboGateways.getSelectedItem().toString());
				}
				else
				{
					_realTimeController.populateData(Constants.TODAY);
				}
			}
			else if(currentView == Constants.VIEW_PROJECTS)
			{
				System.out.println("Refresh UI called. ");
				if(_projectsPanelController == null)
				{
					_projectsPanelController = new ProjectsPanelController(rpc,_menuController.comboProjects.getSelectedItem().toString(), this, _menuController.comboGateways.getSelectedItem().toString(), 1);
				}
				else
				{
					//_projectsPanelController._projectDetails.populateData(_projectsPanelController._projectDetails.selectedDuration, _menuController.comboGateways.getSelectedItem().toString(), _menuController.comboProjects.getSelectedItem().toString());
					_projectsPanelController._projectDetails.gatewayName = this.selectedGateway;
					_projectsPanelController._projectDetails.projectName = this.selectedProject;
					_projectsPanelController._gatewayDetails.allGateways = this.allGateways;
					_projectsPanelController._gatewayDetails.gatewayName = this.selectedGateway;
					_projectsPanelController._gatewayDetails.populateData(_projectsPanelController._gatewayDetails.selectedDuration, this.selectedGateway, this.allGateways);
					_projectsPanelController._projectDetails.populateData(_projectsPanelController._projectDetails.selectedDuration, _menuController.comboGateways.getSelectedItem().toString(),_menuController.comboProjects.getSelectedItem().toString());
				
				}
			}
			else if(currentView == Constants.VIEW_USERS)
			{
//				if(_userPanelController == null)
//				{
//					_userPanelController = new UserPanelController(rpc, this.newDSName,_menuController.comboProjects.getSelectedItem().toString(), _menuController.comboGateways.getSelectedItem().toString() );
//				}
//				else
//				{
					
					this._userPanelController.populateUsersList();
					this._userPanelController.userPanelPopulate();
				//}
			}
		}
		revalidate();
		repaint();
	}
	
	
	/**
	 * Method to capture various UI click actions.
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		String actionCmd = arg0.getActionCommand();
		System.out.println("Actio start time : " + new Date().getTime());
		System.out.println("Action name : " + actionCmd);
		//click of Projects button
		if(actionCmd.compareToIgnoreCase(Constants.btnProjects_Click_Action) == 0)
		{
			System.out.println("Projects clicked : time : " + new Date().getTime());
			//erase earlier display
			contentPanel.removeAll();
			currentView = Constants.VIEW_PROJECTS;
			
			//set menu highlights as per selection
			_menu.btnDashboard.setIcon(dashboardNavigationIcon);
			_menu.btnProjects.setIcon(projectsHighlightIcon);
			_menu.btnRealTime.setIcon(realTimeNavigationIcon);
			_menu.btnReports.setIcon(reportsNavigationIcon);
			_menu.btnUsers.setIcon(usersNavigationIcon);
			
			//add data for selected project
			if(_projectsPanel == null)
			{
				_projectsPanel = new ProjectsPanel(rpc,_menu.comboProjects.getSelectedItem().toString(), this);
			}
			else
			{
				_projectsPanel._projectDetails.projectName = this.selectedProject;
				
				_projectsPanel._projectDetails.populateData(_projectsPanel._projectDetails.selectedDuration, null,_menu.comboProjects.getSelectedItem().toString());
			}
			contentPanel.add(_projectsPanel);
			revalidate();
			repaint();
	
			System.out.println("Projects loaded : time : " + new Date().getTime());
		}
		//click of Dashboards button
		else if(actionCmd.compareToIgnoreCase(Constants.btnDashboard_Click_Action) == 0)
		{
			
			System.out.println("Dashboard clicked : time : " + new Date().getTime());
			//erase earlier display
			contentPanel.removeAll();
			currentView = Constants.VIEW_DASHOARD;
		
			//set menu highlights as per selection
			_menu.btnDashboard.setIcon(dashboardHighlightIcon);
			_menu.btnProjects.setIcon(projectsNavigationIcon);
			_menu.btnRealTime.setIcon(realTimeNavigationIcon);
			_menu.btnReports.setIcon(reportsNavigationIcon);
			_menu.btnUsers.setIcon(usersNavigationIcon);
			
			//if dashboard panel is null then create new else show from where user left
			if(_mPanel == null)
			{
				_mPanel = new DashboardPanel(rpc,_menu.comboProjects.getSelectedItem().toString(),this.newDSName, 0, 0);
			}
			else
			{
//				if(this._mPanel.currentProject.compareToIgnoreCase("All Projects") == 0)
//				{
//					this._mPanel.populateData(_mPanel.currentDuration , null, true);
//				}
//				else
//				{
//					this._mPanel.populateData(_mPanel.currentDuration , _mPanel.currentProject, false);
//				}
				_mPanel.allProjects = this.allProjects;
				_mPanel.currentProject = this.selectedProject;
				_mPanel.populateData(_mPanel.currentDuration, this.selectedProject, this.allProjects);
			}
			contentPanel.add(_mPanel);
			revalidate();
			repaint();
			System.out.println("dashboard loaded: time : " + new Date().getTime());
		}
		
		//click of Real Time 
		else if(actionCmd.compareToIgnoreCase(Constants.btnRealTime_Click_Action) == 0)
		{
			System.out.println("RealTime clicked : time : " + new Date().getTime());
			//erase earlier display
			contentPanel.removeAll();
			int prevComboIndex = 0;
			//add data for real time , new or existing
			if(_realTime == null)
			{
				_realTime = new RealTimePanel(rpc, _menu.comboProjects.getSelectedItem().toString(), this.newDSName, 0);
			}
			else
			{
				prevComboIndex = _realTime.rightComboSelectedIndex;
				_realTime = new RealTimePanel(rpc, _menu.comboProjects.getSelectedItem().toString(), this.newDSName, prevComboIndex);
			}
			contentPanel.add(_realTime);
			currentView = Constants.VIEW_REALTIME;
			
			//set menu highlights as per selection
			_menu.btnDashboard.setIcon(dashboardNavigationIcon);
			_menu.btnProjects.setIcon(projectsNavigationIcon);
			_menu.btnRealTime.setIcon(realTimeHighlightIcon);
			_menu.btnReports.setIcon(reportsNavigationIcon);
			_menu.btnUsers.setIcon(usersNavigationIcon);
			
			revalidate();
			repaint();
			System.out.println("RealTime loaded : time : " + new Date().getTime());
		}
		
		//click of Reports
		else if(actionCmd.compareToIgnoreCase(Constants.btnReport_Click_Action) == 0)
		{
			System.out.println("Reports clicked : time : " + new Date().getTime());
			contentPanel.removeAll();
			
			//add data for selected project
			if(_reports == null)
			{
				_reports = new ReportsPanel(rpc, _menu.comboProjects.getSelectedItem().toString(), newDSName);
			}
			else
			{
				this._reports.refreshReport(_menu.comboProjects.getSelectedItem().toString());
			}
			contentPanel.add(_reports);
			currentView = Constants.VIEW_REPORTS;
			//set menu highlights as per selection
			_menu.btnDashboard.setIcon(dashboardNavigationIcon);
			_menu.btnProjects.setIcon(projectsNavigationIcon);
			_menu.btnReports.setIcon(reportsHighlightIcon);
			_menu.btnRealTime.setIcon(realTimeNavigationIcon);
			_menu.btnUsers.setIcon(usersNavigationIcon);
			revalidate();
			repaint();
			System.out.println("Reports loaded: time : " + new Date().getTime());
		}
		//click of Users 
		else if(actionCmd.compareToIgnoreCase(Constants.btnUsers_Click_Action) == 0)
		{
			System.out.println("Users clicked : time : " + new Date().getTime());
			contentPanel.removeAll();
			
			//add data for selected project
			if(_userPanel == null)
			{
				_userPanel = new UserPanel(rpc, this.newDSName,_menu.comboProjects.getSelectedItem().toString() );
			}
			else
			{
				_userPanel.allProjects = this.allProjects;
				_userPanel.currentProject = this.selectedProject;
				_userPanel.populateUsersList();
			}
			contentPanel.add(_userPanel);
			currentView = Constants.VIEW_USERS;
			
			//set the menu button highlights
			_menu.btnDashboard.setIcon(dashboardNavigationIcon);
			_menu.btnProjects.setIcon(projectsNavigationIcon);
			_menu.btnUsers.setIcon(usersHighlightIcon);
			_menu.btnRealTime.setIcon(realTimeNavigationIcon);
			_menu.btnReports.setIcon(reportsNavigationIcon);
			revalidate();
			repaint();
			System.out.println("Users loaded: time : " + new Date().getTime());
		}
		//project selection from list
		else if(actionCmd.compareToIgnoreCase(Constants.projectslist_Selection_Change) == 0)
		{
			this.selectedProject = _menu.comboProjects.getSelectedItem().toString();
			if(this.selectedProject.compareToIgnoreCase("All Projects") == 0)
			{
				this.allProjects = true;
			}
			else
			{
				this.allProjects = false;
			}
			
			//determine content that user is viewing and refresh that content
			switch(currentView)
			{
			case Constants.VIEW_PROJECTS:
				//erase earlier display
				contentPanel.removeAll();
				int currentDuration = _projectsPanel._projectDetails.selectedDuration;
				_projectsPanel = new ProjectsPanel(rpc,_menu.comboProjects.getSelectedItem().toString(), this);
				//_projectsPanel._projectDetails.populateData(currentDuration, "", _menu.comboProjects.getSelectedItem().toString());
				contentPanel.add(_projectsPanel);
				break;
			case Constants.VIEW_DASHOARD:
				//erase earlier display
				contentPanel.removeAll();
				//add data for selected project
				_mPanel = new DashboardPanel(rpc,_menu.comboProjects.getSelectedItem().toString(),this.newDSName, _mPanel.slideNo, _mPanel.comboDurationSelectedIndex);
				contentPanel.add(_mPanel);
				break;
			case Constants.VIEW_REALTIME:
				//erase earlier display
				contentPanel.removeAll();
				_realTime = new RealTimePanel(rpc, _menu.comboProjects.getSelectedItem().toString(), this.newDSName, _realTime.rightComboSelectedIndex);
				contentPanel.add(_realTime);
				
				break;
			case Constants.VIEW_REPORTS:
				this._reports.refreshReport(_menu.comboProjects.getSelectedItem().toString());
				
				break;
			case Constants.VIEW_USERS:
				//erase earlier display
				contentPanel.removeAll();
				_userPanel = new UserPanel(rpc, this.newDSName,_menu.comboProjects.getSelectedItem().toString() );
				contentPanel.add(_userPanel);
				break;
			}
			
		}
		
		//for controller menu
		if(actionCmd.compareToIgnoreCase(Constants.ControllerbtnProjects_Click_Action) == 0)
		{
			System.out.println("Projects clicked : time : " + new Date().getTime());
			//erase earlier display
			contentPanel.removeAll();
			currentView = Constants.VIEW_PROJECTS;
			
			//set menu highlights as per selection
			_menuController.btnDashboard.setIcon(dashboardNavigationIcon);
			_menuController.btnProjects.setIcon(projectsHighlightIcon);
			_menuController.btnRealTime.setIcon(realTimeNavigationIcon);
			_menuController.btnReports.setIcon(reportsNavigationIcon);
			_menuController.btnUsers.setIcon(usersNavigationIcon);
			
			//add data for selected project
			if(_projectsPanelController == null)
			{
				_projectsPanelController = new ProjectsPanelController(rpc,_menuController.comboProjects.getSelectedItem().toString(), this, _menuController.comboGateways.getSelectedItem().toString(), 1);
			}
			else
			{
				_projectsPanelController._projectDetails.gatewayName = this.selectedGateway;
				_projectsPanelController._projectDetails.projectName = this.selectedProject;
				_projectsPanelController._gatewayDetails.allGateways = this.allGateways;
				_projectsPanelController._gatewayDetails.gatewayName = this.selectedGateway;
				_projectsPanelController._gatewayDetails.populateData(_projectsPanelController._gatewayDetails.selectedDuration, this.selectedGateway, this.allGateways);
				_projectsPanelController._projectDetails.populateData(_projectsPanelController._projectDetails.selectedDuration, _menuController.comboGateways.getSelectedItem().toString(),_menuController.comboProjects.getSelectedItem().toString());
			}
			contentPanel.add(_projectsPanelController);
			revalidate();
			repaint();
	
			System.out.println("Projects loaded : time : " + new Date().getTime());
		}
		//click of Dashboards button
		else if(actionCmd.compareToIgnoreCase(Constants.ControllerbtnDashboard_Click_Action) == 0)
		{
			
			System.out.println("Dashboard clicked : time : " + new Date().getTime());
			//erase earlier display
			contentPanel.removeAll();
			currentView = Constants.VIEW_DASHOARD;
		
			//set menu highlights as per selection
			_menuController.btnDashboard.setIcon(dashboardHighlightIcon);
			_menuController.btnProjects.setIcon(projectsNavigationIcon);
			_menuController.btnRealTime.setIcon(realTimeNavigationIcon);
			_menuController.btnReports.setIcon(reportsNavigationIcon);
			_menuController.btnUsers.setIcon(usersNavigationIcon);
			
			//if dashboard panel is null then create new else show from where user left
			if(_mPanelController == null)
			{
				_mPanelController = new DashboardPanelController(rpc,_menuController.comboGateways.getSelectedItem().toString(), _menuController.comboProjects.getSelectedItem().toString().trim(),this.newDSName, 0, 0);
			}
			else
			{
				
				
				this._mPanelController.allGateways = this.allGateways;
				this._mPanelController.allProjects = this.allProjects;
				this._mPanelController.currentGateway = this.selectedGateway;
				this._mPanelController.currentProject = this.selectedProject;
				this._mPanelController.populateData(this._mPanelController.currentDuration , selectedGateway, selectedProject, allProjects);
//				if(this._mPanelController.allProjects && this._mPanelController.allGateways) //all gateways , all projects
//				{
//					this._mPanelController.populateData(this._mPanelController.currentDuration , null, null, true);
//				}
//				else if(!(this._mPanelController.allGateways) && this._mPanelController.allProjects) //single gateway , all projects
//				{
//					this._mPanelController.populateData(this._mPanelController.currentDuration , this._mPanelController.currentGateway, null, true);
//				}
//				else if(!(this._mPanelController.allGateways) && !(this._mPanelController.allProjects)) //single gateway single project
//				{
//					this._mPanelController.populateData(this._mPanelController.currentDuration , this._mPanelController.currentGateway, this._mPanelController.currentProject, false);
//				}
//				else if(this._mPanelController.allGateways && !(this._mPanelController.allProjects)) //Ignore the gateway and only select based on project
//				{
//					this._mPanelController.populateData(this._mPanelController.currentDuration , null, this._mPanelController.currentProject, false);
//				}
				
			}
			contentPanel.add(_mPanelController);
			revalidate();
			repaint();
			System.out.println("dashboard loaded: time : " + new Date().getTime());
		}
		
		//click of Real Time 
		else if(actionCmd.compareToIgnoreCase(Constants.ControllerbtnRealTime_Click_Action) == 0)
		{
			System.out.println("RealTime clicked : time : " + new Date().getTime());
			//erase earlier display
			contentPanel.removeAll();
			int prevComboindex = 0;
			//add data for real time , new or existing
			if(_realTimeController == null)
			{
				_realTimeController = new RealTimePanelController(rpc, _menuController.comboProjects.getSelectedItem().toString(), this.newDSName, 0, _menuController.comboGateways.getSelectedItem().toString());
			}
			else
			{
				prevComboindex = _realTimeController.rightComboSelectedIndex;
				_realTimeController = new RealTimePanelController(rpc, _menuController.comboProjects.getSelectedItem().toString(), this.newDSName, prevComboindex, _menuController.comboGateways.getSelectedItem().toString());
				
			}
			contentPanel.add(_realTimeController);
			currentView = Constants.VIEW_REALTIME;
			
			//set menu highlights as per selection
			_menuController.btnDashboard.setIcon(dashboardNavigationIcon);
			_menuController.btnProjects.setIcon(projectsNavigationIcon);
			_menuController.btnRealTime.setIcon(realTimeHighlightIcon);
			_menuController.btnReports.setIcon(reportsNavigationIcon);
			_menuController.btnUsers.setIcon(usersNavigationIcon);
			
			revalidate();
			repaint();
			System.out.println("RealTime loaded : time : " + new Date().getTime());
		}
		
		//click of Reports
		else if(actionCmd.compareToIgnoreCase(Constants.ControllerbtnReport_Click_Action) == 0)
		{
			System.out.println("Reports clicked : time : " + new Date().getTime());
			contentPanel.removeAll();
			
			//add data for selected project
			if(_reportsController == null)
			{
				_reportsController = new ReportsPanelController(rpc, _menuController.comboGateways.getSelectedItem().toString(), _menuController.comboProjects.getSelectedItem().toString(), newDSName);
			}
			else
			{
				this._reportsController.refreshReport( _menuController.comboGateways.getSelectedItem().toString(),_menuController.comboProjects.getSelectedItem().toString());
			}
			contentPanel.add(_reportsController);
			currentView = Constants.VIEW_REPORTS;
			//set menu highlights as per selection
			_menuController.btnDashboard.setIcon(dashboardNavigationIcon);
			_menuController.btnProjects.setIcon(projectsNavigationIcon);
			_menuController.btnReports.setIcon(reportsHighlightIcon);
			_menuController.btnRealTime.setIcon(realTimeNavigationIcon);
			_menuController.btnUsers.setIcon(usersNavigationIcon);
			revalidate();
			repaint();
			System.out.println("Reports loaded: time : " + new Date().getTime());
		}
		//click of Users 
		else if(actionCmd.compareToIgnoreCase(Constants.ControllerbtnUsers_Click_Action) == 0)
		{
			System.out.println("Users clicked : time : " + new Date().getTime());
			contentPanel.removeAll();
			
			//add data for selected project
			if(_userPanelController == null)
			{
				_userPanelController = new UserPanelController(rpc, this.newDSName,_menuController.comboProjects.getSelectedItem().toString(), _menuController.comboGateways.getSelectedItem().toString() );
			}
			else
			{
				_userPanelController.allGateways = this.allGateways;
				_userPanelController.allProjects = this.allProjects;
				_userPanelController.currentGateway = this.selectedGateway;
				_userPanelController.currentProject = this.selectedProject;
				_userPanelController.populateUsersList();
			}
			contentPanel.add(_userPanelController);
			currentView = Constants.VIEW_USERS;
			
			//set the menu button highlights
			_menuController.btnDashboard.setIcon(dashboardNavigationIcon);
			_menuController.btnProjects.setIcon(projectsNavigationIcon);
			_menuController.btnUsers.setIcon(usersHighlightIcon);
			_menuController.btnRealTime.setIcon(realTimeNavigationIcon);
			_menuController.btnReports.setIcon(reportsNavigationIcon);
			revalidate();
			repaint();
			System.out.println("Users loaded: time : " + new Date().getTime());
		}
		//project selection from list
		else if(actionCmd.compareToIgnoreCase(Constants.Controllerprojectslist_Selection_Change) == 0)
		{
			this.selectedGateway = _menuController.comboGateways.getSelectedItem().toString();
			this.selectedProject = _menuController.comboProjects.getSelectedItem().toString();
			
			if(this.selectedGateway.compareToIgnoreCase("All Gateways") == 0)
			{
				this.allGateways = true;
			}
			else
			{
				this.allGateways = false;
			}
			
			if(this.selectedProject.compareToIgnoreCase("All Projects") == 0)
			{
				this.allProjects = true;
			}
			else
			{
				this.allProjects = false;
			}
			
			//determine content that user is viewing and refresh that content
			switch(currentView)
			{
			case Constants.VIEW_PROJECTS:
				//erase earlier display
				contentPanel.removeAll();
			
				int tabNo = 1;
				if(_projectsPanelController != null)
				{
					tabNo = _projectsPanelController.currentTab;
				}
				_projectsPanelController = new ProjectsPanelController(this.rpc, _menuController.comboProjects.getSelectedItem().toString(), this, _menuController.comboGateways.getSelectedItem().toString(), tabNo);
			
				contentPanel.add(_projectsPanelController);
				break;
			case Constants.VIEW_DASHOARD:
				//erase earlier display
				contentPanel.removeAll();
				//add data for selected project
				_mPanelController = new DashboardPanelController(rpc,_menuController.comboGateways.getSelectedItem().toString(), _menuController.comboProjects.getSelectedItem().toString(),this.newDSName, _mPanelController.slideNo, _mPanelController.comboDurationSelectedIndex);
				contentPanel.add(_mPanelController);
				break;
			case Constants.VIEW_REALTIME:
				//erase earlier display
				contentPanel.removeAll();
				_realTimeController = new RealTimePanelController(rpc, _menuController.comboProjects.getSelectedItem().toString(), this.newDSName, _realTimeController.rightComboSelectedIndex, _menuController.comboGateways.getSelectedItem().toString());
				contentPanel.add(_realTimeController);
				
				break;
			case Constants.VIEW_REPORTS:
				this._reportsController.refreshReport( _menuController.comboGateways.getSelectedItem().toString(),_menuController.comboProjects.getSelectedItem().toString());
				
				break;
			case Constants.VIEW_USERS:
				//erase earlier display
				contentPanel.removeAll();
				_userPanelController = new UserPanelController(rpc, this.newDSName,_menuController.comboProjects.getSelectedItem().toString(), _menuController.comboGateways.getSelectedItem().toString() );
				contentPanel.add(_userPanelController);
				break;
			}
			
		}
		else if(actionCmd.compareToIgnoreCase(Constants.Controllergatewayslist_Selection_Change) == 0)
		{
			String[] _projects = rpc.getProjectsOnGateway(_menuController.comboGateways.getSelectedItem().toString(),"All Projects");
			if(_projects != null)
			{
				_menuController.comboProjects.setModel(new DefaultComboBoxModel<String>(_projects));
				_menuController.comboProjects.setSelectedIndex(0);
			}
			//determine content that user is viewing and refresh that content

			
		}
		else if(actionCmd.compareToIgnoreCase(Constants.btnLogout_Click_Action) == 0)
		{
			System.exit(0);
			
			//this.getParent().getParent().setVisible(false);
			//this.clientContext.getApp().shutdown(true);
			
			
		}
		
		System.out.println("Action end time : " + new Date().getTime());
		

	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	
	@Override
	protected void paintComponent(Graphics arg0) {
		
		super.paintComponent(arg0);
		 Image background = Toolkit.getDefaultToolkit().createImage("24.svg");
		 arg0.drawImage(background, 0, 0, null);
	}

	/**
	 * Function to refresh the projectsList 
	 * @author : YM , Created on 08/13/2015.
	 * Updated function to cater for enterprise module where there could be two different menus
	 * based on module running on controller or agent 
	 * Changed by YM on 27-Dec-2016
	 */
	public void refreshProjectsList(String[] newList)
	{
		if(_menu != null)
		{
			_menu.comboProjects.setModel(new DefaultComboBoxModel<String>(newList));
		}
		else if(_menuController != null)
		{
			_menuController.comboProjects.setModel(new DefaultComboBoxModel<String>(newList));
		}
		revalidate();
		repaint();
	}

	/*
	 * Added by YM : 27-Dec-2016
	 * To refresh the gateways list based on addition/removal from Projects Page.
	 */
	public void refreshGatewaysList(String[] newList)
	{
		_menuController.comboGateways.setModel(new DefaultComboBoxModel<String>(newList));
		revalidate();
		repaint();
	}
	@Override
	public void receiveNotification(PushNotification arg0) {
		String notificationType = arg0.getMessageType();
	
		if(notificationType.compareToIgnoreCase("DATA_CHANGE") == 0)
		{
			refreshUI();
		}
		else if(notificationType.compareToIgnoreCase("TRIAL_EXPIRED") == 0)
		{
			this.contentPanel.removeAll();
			this.contentPanel.add(new JLabel("Trial Expired, Please contact module developers for further assistance"), BorderLayout.CENTER);
		}
	}
	
	
	

}
