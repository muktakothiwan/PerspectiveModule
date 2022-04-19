package com.vaspsolutions.analytics.UI;

import java.awt.Dimension;

import javax.swing.JPanel;

import com.inductiveautomation.factorypmi.application.script.builtin.SecurityUtilities;
import com.inductiveautomation.ignition.common.Dataset;

import com.vaspsolutions.analytics.client.AnalysisPanel;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.MODIAServiceUnavailableException;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.OverviewInformation;

import java.awt.GridBagLayout;

import javax.swing.JComboBox;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JLabel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;




public class ProjectsDetailsPane extends JPanel implements ActionListener, MouseListener {

	private ModuleRPC rpc;
	private IA_TableModel _projectsListModel;
	private IA_TableModel_Controller _projectsListModelController;
	JTable projectsList;
	JScrollPane scrollPane;
	JComboBox<String> comboBox;
	public int selectedDuration;
	AnalysisPanel aPanel;
	String[] projectNamesList;
	Popup popup;
	JTable addProjectsTable ;
	RoundWhiteButtonBlueBorder btnNewProject;
	Boolean isAdminUser = false;
	Boolean isAgent = false;
	boolean isEnterprise = false;
	public String gatewayName ;
	public String projectName ;
	boolean allProjects;
	boolean allGateways;
	public ProjectsDetailsPane(ModuleRPC _rpc, AnalysisPanel _aPanel, String _projectName, String _gatewayName, boolean _isAgent) {
		super();
		this.rpc = _rpc;
		this.aPanel = _aPanel;
		this.isAgent = _isAgent;
		this.isEnterprise = _rpc.getIfEnterprise();
		this.projectName = _projectName;
		this.gatewayName = _gatewayName;
		this.setPreferredSize(new Dimension(1720, 432));
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		this.setBorder(BorderFactory.createEmptyBorder());
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{170, 180,516, 360,146,344};
		gridBagLayout.rowHeights = new int[]{10, 15,400};
		gridBagLayout.columnWeights = new double[]{0.2, 0.2,1.0,0.2,0.2,0.2};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0,1.0};
		setLayout(gridBagLayout);
		String[] durations = new String[]{"  Today", "  Yesterday","  Last 7 Days","  Last 30 Days","  Last 90 Days","  Last 365 Days","  This week","  This month","  This year","  Last month","  Last week","  Last year"};
		String loggedInUser = SecurityUtilities.getUsername();
		
	
		
		int noOfRoles = 0;
		if(SecurityUtilities.getRoles() != null )
		{
			noOfRoles = SecurityUtilities.getRoles().size();
		}
		for(int r=0; r<noOfRoles; r++)
		{
			if(SecurityUtilities.getRoles().get(r).toString().compareToIgnoreCase("Administrator") == 0)
			{
				isAdminUser = true;
			}
		}
		
		System.out.println("ProjectsDetailsPane projectname : " + projectName);
		comboBox = new JComboBox<String>();
		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		comboBox.setPreferredSize(new Dimension(150,25));
		comboBox.setModel(new DefaultComboBoxModel<String>(durations));
		comboBox.setUI(new ComboArrowUI());
		comboBox.setActionCommand(Constants.CMD_DURATION_SELECT);
		comboBox.addActionListener(this);
	//	comboBox.setBorder(new MatteBorder(0, 1, 0, 0,  Constants.COLOR_WHITE_BACKGROUND));
		
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.BOTH;
		gbc_comboBox.insets = new Insets(5, 0, 5, 0);
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		add(comboBox, gbc_comboBox);
		
		JLabel lblProjects = new JLabel("Projects");
		lblProjects.setPreferredSize(new Dimension(516,32));
		lblProjects.setHorizontalTextPosition(JLabel.LEFT);
		lblProjects.setFont(new Font("SansSerif", Font.BOLD, 20));
		lblProjects.setForeground(Constants.COLOR_BLACK_TEXT);
		
		GridBagConstraints gbc_lblProjects = new GridBagConstraints();
		gbc_lblProjects.fill = GridBagConstraints.BOTH;
		gbc_lblProjects.insets = new Insets(0, 0, 5, 5);
		gbc_lblProjects.gridx = 2;
		gbc_lblProjects.gridy = 1;
		add(lblProjects, gbc_lblProjects);
		
		/*JButton btnNewProject = new JButton("Add Project");
		btnNewProject.setPreferredSize(new Dimension(200,25));
		btnNewProject.setFont(new Font("SansSerif", Font.BOLD, 16));
		btnNewProject.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		btnNewProject.setForeground(Constants.COLOR_BLUE_LABEL);
		btnNewProject.setOpaque(false);
	//	btnNewProject.setMargin(new Insets(15, 15, 15, 15));
		btnNewProject.setHorizontalTextPosition(JButton.CENTER); */
		
		btnNewProject = new RoundWhiteButtonBlueBorder();
		btnNewProject.setName(Constants.CMD_NEW_PROJECT);
		
		btnNewProject.setPreferredSize(new Dimension(146,35));
		btnNewProject.lblboldtext.setText("Add Project");
		btnNewProject.addMouseListener(this);
		GridBagConstraints gbc_btnNewProject = new GridBagConstraints();
		gbc_btnNewProject.fill = GridBagConstraints.BOTH;
		gbc_btnNewProject.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewProject.gridx = 4;
		gbc_btnNewProject.gridy = 1;
		add(btnNewProject, gbc_btnNewProject);
		//only provide access when user is admin (as per email from Chris dated 30-Aug-2016)
		btnNewProject.setVisible(isAdminUser);
		
		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(1032, 400));
		scrollPane.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = 2;
		gbc_scrollPane.gridwidth = 3;
		add(scrollPane, gbc_scrollPane);
		this.selectedDuration = Constants.TODAY;
		if(!this.isEnterprise || this.isAgent)
		{
			System.out.println("getProjects projectNamesList"+this.projectName);
			projectNamesList = this.rpc.getProjects(this.projectName);
			populateData(Constants.TODAY, null,this.projectName);
		}
		else
		{
			System.out.println("getProjectsOnGateway projectNamesList");
			projectNamesList = this.rpc.getProjectsOnGateway(this.gatewayName,this.projectName);
			populateData(Constants.TODAY, this.gatewayName,this.projectName);
		}
	}

	public void populateData(int duration, String gatewayName,String projectName)
	{
		Dataset projects ;
		if(!this.isEnterprise || this.isAgent)
		{
			System.out.println("getProjectDetails projectname: "+ projectName);
			projects = rpc.getProjectDetails(duration, projectName);
			System.out.println("getProjectDetails projectname after execution: "+ projectName);
		}
		else
		{
			System.out.println("getProjectDetailsPerGateway gatewayname: "+ gatewayName+"  projectname: "+ projectName);
			projects = rpc.getProjectDetailsPerGateway(duration, gatewayName, projectName);
		}
		UIManager.getDefaults().put("Table.scrollPaneBorder", null);
		
		projectsList = new JTable();
		projectsList.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		_projectsListModel   = new IA_TableModel();
		_projectsListModelController = new IA_TableModel_Controller();
		ImageIcon removeIcon = new ImageIcon(getClass().getResource("Blue-Circle-X.png"));
		removeIcon = new ImageIcon(removeIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		int noOfProjects = 0;
		float avgTime, noUsers, noSessions, noActions;
		double tempTime;
		String avgSessionTIme = "00:00";
		//SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		SimpleDateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
		if(!this.isEnterprise || this.isAgent)
		{
		if(projects != null)
		{
			noOfProjects = projects.getRowCount();
			for(int i=0; i<noOfProjects; i++)
			{
				if(isProjectMonitored(projects.getValueAt(i, 0).toString()) == true)
				{
					System.out.println("isProjectMonitored for  "+ projects.getValueAt(i, 0).toString() + " returned true");
					if(projects.getValueAt(i, 3) != null)
						{
							
								try {
									avgSessionTIme = projects.getValueAt(i, 3).toString();
									
									//avgSessionTIme = projects.getValueAt(i, 3).toString();//.substring(3, 8);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
					else
					{
						avgSessionTIme = "00:00";
					}
					if(projects.getValueAt(i, 1) != null)
					{
						noUsers = Float.parseFloat(projects.getValueAt(i, 1).toString());
						
					}
					else
					{
						noUsers = 0;
					}
					if(projects.getValueAt(i, 2) != null)
					{
						noSessions = Float.parseFloat(projects.getValueAt(i, 2).toString());
						
					}
					else
					{
						noSessions = 0;
					}
					if(projects.getValueAt(i, 4) != null)
					{
						noActions = Float.parseFloat(projects.getValueAt(i, 4).toString());
						
					}
					else
					{
						noActions = 0;
					}
					
					_projectsListModel.addRowData(new Object[]{projects.getValueAt(i, 0), (int)noUsers,(int) noSessions, avgSessionTIme, (int)noActions, removeIcon});
				}
				else
				{
					System.out.println("isProjectMonitored for  "+ projects.getValueAt(i, 0).toString() + " returned false");
				}
			}
		}
		}else{
			if(projects != null)
			{
				noOfProjects = projects.getRowCount();
				for(int i=0; i<noOfProjects; i++)
				{
					if(isProjectMonitored(projects.getValueAt(i, 1).toString()) == true)
					{
						if(projects.getValueAt(i, 4) != null)
							{
								
									try {
										avgSessionTIme = projects.getValueAt(i, 4).toString();
										
										//avgSessionTIme = projects.getValueAt(i, 3).toString();//.substring(3, 8);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							}
						else
						{
							avgSessionTIme = "00:00";
						}
						if(projects.getValueAt(i, 2) != null)
						{
							noUsers = Float.parseFloat(projects.getValueAt(i, 2).toString());
							
						}
						else
						{
							noUsers = 0;
						}
						if(projects.getValueAt(i,3) != null)
						{
							noSessions = Float.parseFloat(projects.getValueAt(i, 3).toString());
							
						}
						else
						{
							noSessions = 0;
						}
						if(projects.getValueAt(i, 5) != null)
						{
							noActions = Float.parseFloat(projects.getValueAt(i, 5).toString());
							
						}
						else
						{
							noActions = 0;
						}
						
						_projectsListModelController.addRowData(new Object[]{projects.getValueAt(i, 0), projects.getValueAt(i, 1), (int)noUsers,(int) noSessions, avgSessionTIme, (int)noActions, removeIcon});
					}
				}
		}
		}
			Font headerFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
			if(!this.isEnterprise || this.isAgent)
			{
			projectsList.setModel(_projectsListModel);
			projectsList.getColumnModel().getColumn(0).setPreferredWidth(80);
			
			projectsList.setRowHeight(40);
			projectsList.getTableHeader().setBackground(Constants.COLOR_WHITE_BACKGROUND);
			projectsList.getTableHeader().setFont(headerFont );
			projectsList.getTableHeader().setForeground(Constants.COLOR_BLACK_TEXT);
			projectsList.setPreferredScrollableViewportSize(new Dimension(1032, 400));
			Border b = BorderFactory.createCompoundBorder();
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.COLOR_BLACK_TEXT));
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 1, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 0, 0, 1, Constants.COLOR_WHITE_BACKGROUND));
			projectsList.getTableHeader().setBorder(b);
			
			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment( JLabel.CENTER );
			centerRenderer.setBorder(BorderFactory.createEmptyBorder());
			
			
			projectsList.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
			projectsList.getColumnModel().getColumn(0).setHeaderRenderer(centerRenderer );
			projectsList.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			projectsList.getColumnModel().getColumn(1).setHeaderRenderer(centerRenderer );
			projectsList.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
			projectsList.getColumnModel().getColumn(2).setHeaderRenderer(centerRenderer );
			projectsList.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
			projectsList.getColumnModel().getColumn(3).setHeaderRenderer(centerRenderer );
			projectsList.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
			projectsList.getColumnModel().getColumn(4).setHeaderRenderer(centerRenderer );
			projectsList.getColumnModel().getColumn(5).setHeaderRenderer(centerRenderer );
		//	projectsList.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
		
			//following 2 lines hide the grid lines of the table.
			projectsList.setShowGrid(false);
			projectsList.setIntercellSpacing(new Dimension(0, 0));
			projectsList.setShowVerticalLines(false);
			//add a listener for the table.
			projectsList.setPreferredScrollableViewportSize(new Dimension(300,200));
			projectsList.addMouseListener(this);
			projectsList.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			projectsList.setOpaque(false);
			this.scrollPane.setViewportView(null);
			scrollPane.setViewportView(projectsList);
			scrollPane.getViewport().setBackground(Constants.COLOR_WHITE_BACKGROUND);
			}
			else{

				projectsList.setModel(_projectsListModelController);
				projectsList.getColumnModel().getColumn(0).setPreferredWidth(80);
				
				projectsList.setRowHeight(40);
				projectsList.getTableHeader().setBackground(Constants.COLOR_WHITE_BACKGROUND);
				projectsList.getTableHeader().setFont(headerFont );
				projectsList.getTableHeader().setForeground(Constants.COLOR_BLACK_TEXT);
				projectsList.setPreferredScrollableViewportSize(new Dimension(1032, 400));
				Border b = BorderFactory.createCompoundBorder();
				b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.COLOR_BLACK_TEXT));
				b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
				b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 1, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
				b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 0, 0, 1, Constants.COLOR_WHITE_BACKGROUND));
				projectsList.getTableHeader().setBorder(b);
				
				DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
				centerRenderer.setHorizontalAlignment( JLabel.CENTER );
				centerRenderer.setBorder(BorderFactory.createEmptyBorder());
				
				
				projectsList.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
				projectsList.getColumnModel().getColumn(0).setHeaderRenderer(centerRenderer );
				projectsList.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
				projectsList.getColumnModel().getColumn(1).setHeaderRenderer(centerRenderer );
				projectsList.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
				projectsList.getColumnModel().getColumn(2).setHeaderRenderer(centerRenderer );
				projectsList.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
				projectsList.getColumnModel().getColumn(3).setHeaderRenderer(centerRenderer );
				projectsList.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
				projectsList.getColumnModel().getColumn(4).setHeaderRenderer(centerRenderer );
				projectsList.getColumnModel().getColumn(5).setHeaderRenderer(centerRenderer );
				projectsList.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
				projectsList.getColumnModel().getColumn(6).setHeaderRenderer(centerRenderer );
//				projectsList.getColumnModel().getColumn(6).setCellRenderer( centerRenderer );
				
				//following 2 lines hide the grid lines of the table.
				projectsList.setShowGrid(false);
				projectsList.setIntercellSpacing(new Dimension(0, 0));
				projectsList.setShowVerticalLines(false);
				//add a listener for the table.
				projectsList.setPreferredScrollableViewportSize(new Dimension(300,200));
				projectsList.addMouseListener(this);
				projectsList.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				projectsList.setOpaque(false);
				this.scrollPane.setViewportView(null);
				scrollPane.setViewportView(projectsList);
				scrollPane.getViewport().setBackground(Constants.COLOR_WHITE_BACKGROUND);
			}
		
		revalidate();
		repaint();
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCommad = arg0.getActionCommand();
		int indexToShow = 0;
		if(actionCommad.compareToIgnoreCase(Constants.CMD_DURATION_SELECT) ==0)
		{
			this.selectedDuration = Constants.TODAY;
			String selectedDuration = comboBox.getSelectedItem().toString();
			if(selectedDuration != null)
			{
				selectedDuration = selectedDuration.trim();
				if(selectedDuration.compareToIgnoreCase("Today") == 0)
				{
					this.selectedDuration  = Constants.TODAY;
				}
				else if(selectedDuration.compareToIgnoreCase("Yesterday") == 0)
				{
					this.selectedDuration  = Constants.YESTERDAY;
				}
			
				else if(selectedDuration.compareToIgnoreCase("Last 7 Days") == 0)
				{
					this.selectedDuration  = Constants.LAST_SEVEN_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("Last 30 Days") == 0)
				{
					this.selectedDuration  = Constants.LAST_THIRTY_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("Last 90 Days") == 0)
				{
					this.selectedDuration  = Constants.LAST_NINTY_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("Last 365 Days") == 0)
				{
					this.selectedDuration  = Constants.LAST_365_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("This week") == 0)
				{
					this.selectedDuration  = Constants.THIS_WEEK;
				}
				else if(selectedDuration.compareToIgnoreCase("This month") == 0)
				{
					this.selectedDuration  = Constants.THIS_MONTH;
				}
				else if(selectedDuration.compareToIgnoreCase("This year") == 0)
				{
					this.selectedDuration  = Constants.THIS_YEAR;
				}
				else if(selectedDuration.compareToIgnoreCase("Last month") == 0)
				{
					this.selectedDuration  = Constants.LAST_MONTH;
				}
				else if(selectedDuration.compareToIgnoreCase("Last week") == 0)
				{
					this.selectedDuration  = Constants.LAST_WEEK;
				}
				else if(selectedDuration.compareToIgnoreCase("Last year") == 0)
				{
					this.selectedDuration  = Constants.LAST_YEAR;
				}
				else
				{
					this.selectedDuration  = Constants.TODAY;
				}
				
			}
			populateData(this.selectedDuration, this.gatewayName,this.projectName);
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_NEW_PROJECT) ==0)
		{
			//moved to mouse listener
			
			
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_CLOSE_ADD_PROJECT_POPUP) ==0)
		{
			popup.hide();
			
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_CANCEL_ADD_PROJECT) ==0)
		{
			//clear the check boxes on UI
			
			AddNewProjectTableModel _model =  (AddNewProjectTableModel)addProjectsTable.getModel();
			int noOfRows = _model.getRowCount();
			int i = 0;
			
			for(i=0; i<noOfRows; i++)
			{
				_model.setValueAt(false, i, 1);
			}
			
			revalidate();
			repaint();
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_ADD_PROJECT) ==0)
		{
			
			//get all names for which user has selected Add 
			AddNewProjectTableModel _model =  (AddNewProjectTableModel)addProjectsTable.getModel();
			List<String> projectsToAdd = new ArrayList<String>();
			int noOfRows = _model.getRowCount();
			int i = 0;
			
			for(i=0; i<noOfRows; i++)
			{
				if((Boolean)_model.getValueAt(i, 1) == true)
				{
					projectsToAdd.add((String)_model.getValueAt(i, 0));
				}
				
			}
			
			//call rpc method to insert these values in mod_ia_projects.
			if(!this.isEnterprise || this.isAgent)
			{
				this.rpc.addProjectsToModule(projectsToAdd);
				projectNamesList = this.rpc.getProjects(this.projectName);
			}
			else
			{
				
				//call service from Agent to add projects
				
				if(this.gatewayName.trim().compareToIgnoreCase("All Gateways") == 0)
				{
					JOptionPane.showMessageDialog(this, "Please select a gateway to add Project.");
				}
				else
				{

					System.out.println("Gateway: "+gatewayName);
					System.out.println("addProjectsToAgent before called: ");
					try
					{
						this.rpc.addProjectsToAgent(this.gatewayName, projectsToAdd);
						System.out.println("addProjectsToAgent after called: ");
					}
					catch (MODIAServiceUnavailableException e) {
						JOptionPane.showMessageDialog(this, "Can not connect to requested gateway at this time , please try adding project later");
					}
				}
				System.out.println("getProjectsOnGateway this.gatewayName,this.projectName "+this.gatewayName+ ", " + this.projectName);
				projectNamesList = this.rpc.getProjectsOnGateway(this.gatewayName,this.projectName);

			}
			popup.hide();
			populateData(this.selectedDuration, this.gatewayName,this.projectName);
			this.aPanel.refreshProjectsList(this.projectNamesList);
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		Point p = arg0.getPoint();
		if( arg0.getSource() == this.projectsList)
		{
			
			if(isAdminUser)
			{
				int row = projectsList.rowAtPoint(p);
				int col = projectsList.columnAtPoint(p);
				
				int warningDialogOptions = JOptionPane.YES_NO_OPTION;
				int i = 0, j=0;
				
				if(!this.isEnterprise || this.isAgent)
				{
					if(col == 5)
					{
						int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove project ?",
								"Warning", warningDialogOptions);
						
						if(result == JOptionPane.YES_OPTION)
						{
							String projectToDelete = projectsList.getValueAt(row, 0).toString();
							
							this.projectNamesList = this.rpc.deleteAndGetUpdatedProjectsList(projectToDelete);
							populateData(this.selectedDuration, this.gatewayName,this.projectName);
							
							this.aPanel.refreshProjectsList(this.projectNamesList);
						}
					}
				}
				else
				{
					if(col == 6)
					{
						int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove project ?",
								"Warning", warningDialogOptions);
						
						if(result == JOptionPane.YES_OPTION)
						{
							String projectToDelete = projectsList.getValueAt(row, 1).toString();
						
							String gatewayNameForDelete = projectsList.getValueAt(row, 0).toString();
									try
									{
										this.rpc.deleteProjectsFromAgent(gatewayNameForDelete, projectToDelete);
										this.projectNamesList = this.rpc.getProjectsOnGateway(this.gatewayName,this.projectName);
									}
									catch (MODIAServiceUnavailableException e)
									{
										JOptionPane.showMessageDialog(this, "Can not connect to requested gateway at this time , please try adding project later");
									}
							
							populateData(this.selectedDuration, this.gatewayName,this.projectName);
							
							this.aPanel.refreshProjectsList(this.projectNamesList);
						}
					
					}
				}
			}	
			
			else
			{
				JOptionPane.showMessageDialog(this, "You need to have Admin rights to remove project.");
			}
			
		}
		else if(arg0.getSource() == this.btnNewProject)
		{
			//call rpc function to get the list of projects that are not added in Ignition
			String[] projectsToAdd = null;
			boolean showAllProjectsMessage = true;
			System.out.println("Clicked on Add Project Gateway Name : " + this.gatewayName);
			if(this.isEnterprise && !this.isAgent)
			{
				if(this.gatewayName.trim().compareToIgnoreCase("All Gateways") == 0)
				{
					JOptionPane.showMessageDialog(this, "Please select a gateway to add Project.");
					showAllProjectsMessage = false;
				}
				else
				{
					
					try {
						
						projectsToAdd = rpc.getProjectsNotAddedFromAgent(this.gatewayName);
						
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this, "Can not connect to requested gateway at this time , please try adding project later");
						//e.printStackTrace();
					}
					System.out.println("getProjectsNotAddedFromAgent called");
				}
			}
			else
			{
					System.out.println("Agent true");
					projectsToAdd = rpc.getProjectNotAddedRoIgnitionAnalytics();
					System.out.println("getProjectNotAddedRoIgnitionAnalytics called");
			}
				
				AddNewProjectTableModel tableModel = new AddNewProjectTableModel();
				int noOfProjects = 0;
				
				if(projectsToAdd != null )
				{
					noOfProjects = projectsToAdd.length;
				}
				
				int i = 0;
				addProjectsTable = new JTable();
				if(noOfProjects == 0 && showAllProjectsMessage == true)
				{
					JOptionPane.showMessageDialog(this, "All the projects have already been added.");
				}
				else if(noOfProjects > 0)
				{
					for(i=0; i<noOfProjects; i++)
					{
						tableModel.addRowData(new Object[]{projectsToAdd[i],false});
					}
					addProjectsTable.setModel(tableModel);
					addProjectsTable.getTableHeader().setBackground(Constants.COLOR_WHITE_BACKGROUND);
					addProjectsTable.setShowGrid(false);
					addProjectsTable.setRowHeight(15);
					AddProjectPage _page = new AddProjectPage();
					_page.scrollPane.setViewportView(addProjectsTable);
					_page.btnAdd.addActionListener(this);
					_page.btnCancel.addActionListener(this);
					_page.btnClose.addActionListener(this);
					
					PopupFactory factory = PopupFactory.getSharedInstance();
					popup = factory.getPopup(this, _page, 500, 150);
					popup.show();
					
					//JOptionPane.showMessageDialog(this, _page);
				}
			}
		
		
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean isProjectMonitored(String projectName)
	{
		boolean projectMonitored = false;
		int i =0, noOfProjects;
		noOfProjects = this.projectNamesList.length;
		for( i=0; i<noOfProjects; i++)
		{
			if(this.projectNamesList[i].compareToIgnoreCase(projectName) == 0)
			{
				projectMonitored = true;
				break;
			}
		}
		return projectMonitored;
	}

}
