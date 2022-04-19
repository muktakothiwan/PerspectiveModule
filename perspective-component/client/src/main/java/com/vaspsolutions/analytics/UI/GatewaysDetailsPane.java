package com.vaspsolutions.analytics.UI;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.inductiveautomation.factorypmi.application.script.builtin.SecurityUtilities;
import com.inductiveautomation.ignition.common.Dataset;
import com.vaspsolutions.analytics.client.AnalysisPanel;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ModuleRPC;

import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Popup;
import javax.swing.PopupFactory;

public class GatewaysDetailsPane extends JPanel implements ActionListener, MouseListener{

	/**
	 * Create the panel.
	 */
	private ModuleRPC rpc;
	AnalysisPanel aPanel;
	public int selectedDuration;
	String[] gatewaysList;
	
	private IA_TableModel _gatewaysListModel;
	JTable gatewaysTable;
	JScrollPane scrollPane;
	RoundWhiteButtonBlueBorder btnNewGateway;
	RoundWhiteButtonBlueBorder btnRenameGateway;
	JComboBox comboBox;
	boolean isAdminUser = false;
	public String gatewayName;
	public Boolean allGateways;
	Popup popup;
	Popup popupRenameGateway;
	JTable addGatewaysTable ;
	RenameGatewayPage _pageRename;
	public GatewaysDetailsPane(ModuleRPC _rpc, AnalysisPanel _aPanel, String gatewayName) {
		this.rpc = _rpc;
		this.aPanel = _aPanel;
	
		if(gatewayName.compareToIgnoreCase("All Gateways") == 0)
		{
			this.allGateways= true;
		}
		else
		{
			this.allGateways = false;
		}
		this.gatewayName = gatewayName;
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setPreferredSize(new Dimension(1720, 432));
		this.setBorder(BorderFactory.createEmptyBorder());
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{170, 180,702, 160,160,344};
		gridBagLayout.rowHeights = new int[]{10, 15,400};
		gridBagLayout.columnWeights = new double[]{0.2, 0.2,1.0,0.2,0.2,0.2};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0,1.0};
		setLayout(gridBagLayout);
		String[] durations = new String[]{"  Today", "  Yesterday","  Last 7 Days","  Last 30 Days","  Last 90 Days","  Last 365 Days","  This week","  This month","  This year","  Last month","  Last week","  Last year"};
		String loggedInUser = SecurityUtilities.getUsername();
		
		
//		System.out.println("logged in user : " + loggedInUser);
		
//		if(loggedInUser.compareToIgnoreCase("admin") == 0)
//		{
//			isAdminUser = true;
//		}
		
		
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
		
		
		comboBox = new JComboBox();
		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		comboBox.setPreferredSize(new Dimension(150,25));
		comboBox.setModel(new DefaultComboBoxModel<String>(durations));
		comboBox.setUI(new ComboArrowUI());
		comboBox.setActionCommand(Constants.CMD_DURATION_SELECT);
		comboBox.addActionListener(this);
		comboBox.setBorder(new MatteBorder(0, 1, 0, 0,  Constants.COLOR_WHITE_BACKGROUND));
		
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.BOTH;
		gbc_comboBox.insets = new Insets(5, 0, 5, 5);
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		add(comboBox, gbc_comboBox);
		
		JLabel lblGateways = new JLabel("Gateways");
		lblGateways.setPreferredSize(new Dimension(516,32));
		lblGateways.setHorizontalTextPosition(JLabel.LEFT);
		lblGateways.setFont(new Font("SansSerif", Font.BOLD, 20));
		lblGateways.setForeground(Constants.COLOR_BLACK_TEXT);
		
		
		GridBagConstraints gbc_lblGateways = new GridBagConstraints();
		gbc_lblGateways.fill = GridBagConstraints.BOTH;
		gbc_lblGateways.insets = new Insets(0, 0, 5, 5);
		gbc_lblGateways.gridx = 2;
		gbc_lblGateways.gridy = 1;
		add(lblGateways, gbc_lblGateways);
		
		btnNewGateway = new RoundWhiteButtonBlueBorder();
		btnNewGateway.setPreferredSize(new Dimension(146,35));
		btnNewGateway.lblboldtext.setText("Add Gateway");
		btnNewGateway.setName(Constants.CMD_NEW_GATEWAY);
		btnNewGateway.addMouseListener(this);
		//only make visible if user is admin
		btnNewGateway.setVisible(isAdminUser);

		GridBagConstraints gbc_btnNewGateway = new GridBagConstraints();
		gbc_btnNewGateway.fill = GridBagConstraints.BOTH;
		gbc_btnNewGateway.anchor = GridBagConstraints.EAST;
		gbc_btnNewGateway.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewGateway.gridx = 3;
		gbc_btnNewGateway.gridy = 1;
		add(btnNewGateway, gbc_btnNewGateway);
		
		
		btnRenameGateway = new RoundWhiteButtonBlueBorder();
		btnRenameGateway.setPreferredSize(new Dimension(146,35));
		btnRenameGateway.lblboldtext.setText("Rename Gateway");
		btnRenameGateway.setName(Constants.CMD_RENAME_GATEWAY);
		btnRenameGateway.addMouseListener(this);
		//only make visible if user is admin
		btnRenameGateway.setVisible(isAdminUser);

		GridBagConstraints gbc_btnRenameGateway = new GridBagConstraints();
		gbc_btnRenameGateway.fill = GridBagConstraints.BOTH;
		//gbc_btnRenameGateway.anchor = GridBagConstraints.EAST;
		gbc_btnRenameGateway.insets = new Insets(0, 0, 5, 5);
		gbc_btnRenameGateway.gridx = 4;
		gbc_btnRenameGateway.gridy = 1;
		add(btnRenameGateway, gbc_btnRenameGateway);
		
		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(1032, 400));
		scrollPane.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = 2;
		add(scrollPane, gbc_scrollPane);

		this.selectedDuration = Constants.TODAY;
		gatewaysList = this.rpc.getGateways();
		if(allGateways){
		populateData(Constants.TODAY,gatewayName,allGateways);
		}
		else{
			populateData(Constants.TODAY,gatewayName,allGateways);
		}
	}
	
	public void populateData(int duration,String gatewayName,Boolean allGateways)
	{
		Dataset gateways = rpc.getGatewayDetails(duration,gatewayName,allGateways);
		UIManager.getDefaults().put("Table.scrollPaneBorder", null);
		
		gatewaysTable = new JTable();
		gatewaysTable.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		_gatewaysListModel  = new IA_TableModel();
		Object[] _columnNames = {"Gateway", "Users", "Sessions", "Avg. Session Duration", "Actions", "Remove"};
		_gatewaysListModel.set_columnNames(_columnNames);
		ImageIcon removeIcon = new ImageIcon(getClass().getResource("Blue-Circle-X.png"));
		removeIcon = new ImageIcon(removeIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		int noOfProjects = 0;
		float avgTime, noUsers, noSessions, noActions;
		double tempTime;
		String avgSessionTIme = "00:00";
		//SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		SimpleDateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
		if(gateways != null)
		{
			noOfProjects = gateways.getRowCount();
			for(int i=0; i<noOfProjects; i++)
			{
				if(rpc.isGatewayMonitored(gateways.getValueAt(i, 0).toString()) == true)
				{

					if(gateways.getValueAt(i, 3) != null)
						{
							
								try {
									avgSessionTIme = gateways.getValueAt(i, 3).toString();
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
					else
					{
						avgSessionTIme = "00:00";
					}
					if(gateways.getValueAt(i, 1) != null)
					{
						noUsers = Float.parseFloat(gateways.getValueAt(i, 1).toString());
						
					}
					else
					{
						noUsers = 0;
					}
					if(gateways.getValueAt(i, 2) != null)
					{
						noSessions = Float.parseFloat(gateways.getValueAt(i, 2).toString());
						
					}
					else
					{
						noSessions = 0;
					}
					if(gateways.getValueAt(i, 4) != null)
					{
						noActions = Float.parseFloat(gateways.getValueAt(i, 4).toString());
						
					}
					else
					{
						noActions = 0;
					}
					
					_gatewaysListModel.addRowData(new Object[]{gateways.getValueAt(i, 0), (int)noUsers,(int) noSessions, avgSessionTIme, (int)noActions, removeIcon});
				}
			}
		
			Font headerFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
			
			gatewaysTable.setModel(_gatewaysListModel);
			gatewaysTable.getColumnModel().getColumn(0).setPreferredWidth(80);
			
			gatewaysTable.setRowHeight(40);
			gatewaysTable.getTableHeader().setBackground(Constants.COLOR_WHITE_BACKGROUND);
			gatewaysTable.getTableHeader().setFont(headerFont );
			gatewaysTable.getTableHeader().setForeground(Constants.COLOR_BLACK_TEXT);
			gatewaysTable.setPreferredScrollableViewportSize(new Dimension(1032, 400));
			Border b = BorderFactory.createCompoundBorder();
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.COLOR_BLACK_TEXT));
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 1, 0, 0, Constants.COLOR_WHITE_BACKGROUND));
			b = BorderFactory.createCompoundBorder(b,BorderFactory.createMatteBorder(0, 0, 0, 1, Constants.COLOR_WHITE_BACKGROUND));
			gatewaysTable.getTableHeader().setBorder(b);
			
			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment( JLabel.CENTER );
			centerRenderer.setBorder(BorderFactory.createEmptyBorder());
			
			
			gatewaysTable.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
			gatewaysTable.getColumnModel().getColumn(0).setHeaderRenderer(centerRenderer );
			gatewaysTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			gatewaysTable.getColumnModel().getColumn(1).setHeaderRenderer(centerRenderer );
			gatewaysTable.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
			gatewaysTable.getColumnModel().getColumn(2).setHeaderRenderer(centerRenderer );
			gatewaysTable.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
			gatewaysTable.getColumnModel().getColumn(3).setHeaderRenderer(centerRenderer );
			gatewaysTable.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
			gatewaysTable.getColumnModel().getColumn(4).setHeaderRenderer(centerRenderer );
			gatewaysTable.getColumnModel().getColumn(5).setHeaderRenderer(centerRenderer );
		
			//following 2 lines hide the grid lines of the table.
			gatewaysTable.setShowGrid(false);
			gatewaysTable.setIntercellSpacing(new Dimension(0, 0));
			gatewaysTable.setShowVerticalLines(false);
			//add a listener for the table.
			gatewaysTable.setPreferredScrollableViewportSize(new Dimension(300,200));
			gatewaysTable.addMouseListener(this);
			gatewaysTable.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			gatewaysTable.setOpaque(false);
			this.scrollPane.setViewportView(null);
			scrollPane.setViewportView(gatewaysTable);
			scrollPane.getViewport().setBackground(Constants.COLOR_WHITE_BACKGROUND);
		}
	}

	private boolean isGatewayMonitored(String gatewayName)
	{
		boolean projectMonitored = false;
		int i =0, noOfProjects;
		noOfProjects = this.gatewaysList.length;
		for( i=0; i<noOfProjects; i++)
		{
			if(this.gatewaysList[i].compareToIgnoreCase(gatewayName) == 0)
			{
				projectMonitored = true;
				break;
			}
		}
		return projectMonitored;
	
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		Point p = arg0.getPoint();
		if( arg0.getSource() == this.gatewaysTable)
		{
			
			if(isAdminUser)
			{
				int row = gatewaysTable.rowAtPoint(p);
				int col = gatewaysTable.columnAtPoint(p);
		
				int warningDialogOptions = JOptionPane.YES_NO_OPTION;
				int i = 0, j=0;
				if(col == 5)
				{
					int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove Gateway ?",
							"Warning", warningDialogOptions);
					
					if(result == JOptionPane.YES_OPTION)
					{
						String gatewayToDelete = gatewaysTable.getValueAt(row, 0).toString();
						
						//make an rpc call to delete the gateway.
						
						this.gatewaysList = this.rpc.deleteAndGetUpdatedGatewaysList(gatewayToDelete);
						
						populateData(this.selectedDuration,gatewayName,allGateways);
						
						this.aPanel.refreshGatewaysList(this.gatewaysList);
						this.aPanel.refreshProjectsList(rpc.getProjectsOnGateway("All Gateways", "All Projects"));
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(this, "You need to have Admin rights to remove project.");
			}
			
		}
		else if(arg0.getSource() == this.btnNewGateway)
		{
			//call rpc function to get the list of projects that are not added in Ignition
			String[] gatewaysToAdd = rpc.getGatewaysNotAddedToIgnitionAnalytics();
			AddNewProjectTableModel tableModel = new AddNewProjectTableModel();
			int noOfGateways = 0;
			
			if(gatewaysToAdd != null )
			{
				noOfGateways = gatewaysToAdd.length;
			}
			int i = 0;
			addGatewaysTable= new JTable();
			if(noOfGateways == 0)
			{
				JOptionPane.showMessageDialog(this, "All the gateways have alreay been added.");
			}
			else
			{
				for(i=0; i<noOfGateways; i++)
				{
					tableModel.addRowData(new Object[]{gatewaysToAdd[i],false});
				}
				addGatewaysTable.setModel(tableModel);
				addGatewaysTable.getTableHeader().setBackground(Constants.COLOR_WHITE_BACKGROUND);
				addGatewaysTable.setShowGrid(false);
				addGatewaysTable.setRowHeight(15);
				AddProjectPage _page = new AddProjectPage();
				_page.labelTitle.setText("Add Gateway");
				_page.scrollPane.setViewportView(addGatewaysTable);
				_page.btnAdd.addActionListener(this);
				_page.btnCancel.addActionListener(this);
				_page.btnClose.addActionListener(this);
				
				PopupFactory factory = PopupFactory.getSharedInstance();
				popup = factory.getPopup(this, _page, 500, 150);
				popup.show();
				
				//JOptionPane.showMessageDialog(this, _page);
			}
		}
		else if(arg0.getSource() == this.btnRenameGateway)
		{
			List<String> gatewaysToRename = rpc.getGatewaysForRename();
			int noOfGateways = 0;
			_pageRename = new RenameGatewayPage();
			
			_pageRename.btnAdd.addActionListener(this);
			_pageRename.btnCancel.addActionListener(this);
			_pageRename.btnClose.addActionListener(this);
			if(gatewaysToRename != null )
			{
				noOfGateways = gatewaysToRename.size();
			}
			int i = 0;
			if(noOfGateways == 0)
			{
				JOptionPane.showMessageDialog(this, "All the gateways have alreay been renamed.");
			}
			else
			{
				for(i=0; i<noOfGateways; i++)
				{
					_pageRename.comboBoxNewName.addItem(gatewaysToRename.get(i));
					_pageRename.comboBoxOldName.addItem(gatewaysToRename.get(i));
				}
				PopupFactory factory = PopupFactory.getSharedInstance();
				popupRenameGateway = factory.getPopup(this, _pageRename, 500, 250);
				popupRenameGateway.show();
			}
			
		}
		
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommad = e.getActionCommand();
		System.out.println("gatewaydetails pane : action performed , actionCommnd : " + actionCommad);
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
			populateData(this.selectedDuration,gatewayName,allGateways);
		}
		
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_CLOSE_ADD_PROJECT_POPUP) ==0)
		{
			popup.hide();
			
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_CANCEL_ADD_PROJECT) ==0)
		{
			//clear the check boxes on UI
			
			AddNewProjectTableModel _model =  (AddNewProjectTableModel)addGatewaysTable.getModel();
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
			AddNewProjectTableModel _model =  (AddNewProjectTableModel)addGatewaysTable.getModel();
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
			this.rpc.addGatewaysToModule(projectsToAdd);
			popup.hide();
			gatewaysList = this.rpc.getGateways();
			populateData(this.selectedDuration,gatewayName,allGateways);
			this.aPanel.refreshGatewaysList(this.gatewaysList);
			this.aPanel.refreshProjectsList(rpc.getProjectsOnGateway("All Gateways", "All Projects"));
		}
		
		//rename gateway page events
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_CLOSE_RENAME_GATEWAY_POPUP) ==0)
		{
			popupRenameGateway.hide();
			
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_CANCEL_RENAME_GATEWAY) ==0)
		{
			popupRenameGateway.hide();
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_SAVE_RENAME_GATEWAY) ==0)
		{
		
			
			int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to rename Gateway ?", "Warning", JOptionPane.YES_NO_OPTION);
			revalidate();
			repaint();
			if(result == JOptionPane.YES_OPTION)
			{
			
				//call rpc method to update gateway name.
				
				this.rpc.updateNewGatewayName(_pageRename.comboBoxOldName.getSelectedItem().toString(), _pageRename.comboBoxNewName.getSelectedItem().toString());
				popupRenameGateway.hide();
				
				gatewaysList = this.rpc.getGateways();
				populateData(this.selectedDuration,gatewayName,allGateways);
				this.aPanel.refreshGatewaysList(this.gatewaysList);
				this.aPanel.refreshProjectsList(rpc.getProjectsOnGateway("All Gateways", "All Projects"));
			}
		}
		
	}
}
