package com.vaspsolutions.analytics.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;

import com.inductiveautomation.factorypmi.application.components.PMITable;
import com.inductiveautomation.ignition.client.util.gui.table.DatasetTableModel;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.user.ContactInfo;
import com.inductiveautomation.ignition.common.user.User;
import com.kenai.jffi.Array;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.ScreensCount;
import com.vaspsolutions.analytics.common.UsersOverviewInformation;

import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.border.MatteBorder;


@SuppressWarnings("serial")
public class UserPanel extends JPanel implements ActionListener {

	 
	 
	
	ModuleRPC rpc;
	String dsName;
	JScrollPane usersListPane;
	public JPanel profileInfoPanel;
	Collection<User> userProfiles ;
	JLabel lblFirstSeenValue;
	JLabel lblLastSeenValue;
	JLabel lblSessionDurationValue;
	JLabel lblTotalVisitsValue;
	JLabel lblTotalActionsValue ;
	JLabel lblNameValue ;
	JLabel lblEmailValue;
	JTextArea lblGroupValue;
	JTextArea lblSecurityValue;
	JLabel lblAllActionsValue;
	JLabel lblScreenViewsValue;
	private JLabel lblCurrentScreen;
	private JLabel lblCurrentScreenVal;
	public JPanel ScreenViewsTimelinePanel;
	private JScrollPane screenTimelineScroll;
	public JScrollPane _usersList;
	
	List<IA_UserListPanel> userListData;
	List<IA_UserListPanel> alluserListData;
	List<IA_UserListPanel> onlineuserListData;
	int screenViewvedValues[] = null;
	DefaultCategoryDataset _barChartData;
	
	public boolean allProjects;
	public String currentProject;
	private JTable userListTable;
	private JTable table;
	
	private JPanel userDetailsPanel;
	private JPanel searchBoxPanel;
	private JPanel entirePanelHeader;
	private JPanel overViewGraphPanel;
	public JLabel userImageLbl;
	public JLabel userNamelbl;
 	private JPanel locationInnerPanel;
	public JLabel flagLbl;
	public JLabel locationLbl;
	public JLabel lblNewLabel;
	private JLabel userListImgLbl;
	private JComboBox comboBox;
	private JButton downloadBtn;
	private JButton refreshListBtn;
	private JTextField searchTextField;
	private JTable sessionTable;
	private JButton searchButton;
	private JLabel totalUsersCountLbl;
	private JLabel todayDateLbl;
	
	private ImageIcon usaUserIcon;
	private ImageIcon indiaUserIcon;
	private ImageIcon canadaUserIcon;
	private ImageIcon mexicoUserIcon;
	
	//to maintain the UI state on refresh
	public String selectedUser;
	public String profileSelectedUser;
	public int comboSelction;
	public String searchString;
	
	private JButton clearBtn;
	@SuppressWarnings("null")
	public UserPanel(ModuleRPC rpc, String ds, String projectName) {
		super();
		this.setSize(1720, 1080);
		this.rpc = rpc;
		this.dsName = ds;
		this.currentProject = projectName;
		if(projectName.compareToIgnoreCase("All Projects") == 0)
		{
			this.allProjects = true;
 		}
		else
		{
			this.allProjects = false;
		}
		//this.setSize(948, 600);
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		this.setBorder(null);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{350,500, 850};
		gridBagLayout.rowHeights = new int[]{80, 1000};
		gridBagLayout.columnWeights = new double[]{0.0,0.0,1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
		setLayout(gridBagLayout);
		
	
		GridBagConstraints gbc_usersListPane = new GridBagConstraints();
		gbc_usersListPane.insets = new Insets(0, 0, 0, 0);
		gbc_usersListPane.fill = GridBagConstraints.BOTH;
		gbc_usersListPane.gridx = 0;
		gbc_usersListPane.gridy = 1;
		
		//populate the image icons
		
		usaUserIcon = new ImageIcon(getClass().getResource("Ignition-Flag-Icons-USA.png"));
		Image tempImage = usaUserIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		usaUserIcon = new ImageIcon(tempImage);
				
		indiaUserIcon = new ImageIcon(getClass().getResource("Ignition-Flag-Icons-India.png"));
		tempImage = indiaUserIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		indiaUserIcon = new ImageIcon(tempImage);
				 
		mexicoUserIcon = new ImageIcon(getClass().getResource("Ignition-Flag-Icons-Mexico.png"));
		tempImage = mexicoUserIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		mexicoUserIcon = new ImageIcon(tempImage);
					
		canadaUserIcon = new ImageIcon(getClass().getResource("Ignition-Flag-Icons-Canada.png"));
		tempImage = canadaUserIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		canadaUserIcon = new ImageIcon(tempImage);
		
		_usersList = new JScrollPane();
		_usersList.setPreferredSize(new Dimension(350, 1000));
		_usersList.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		_usersList.setBorder(new MatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
		_usersList.setViewportBorder(BorderFactory.createEmptyBorder());
		
		this.selectedUser = null;
		this.profileSelectedUser = null;
		
		
		Border emptyBorder = BorderFactory.createEmptyBorder();
		
		searchBoxPanel = new JPanel();
		searchBoxPanel.setBorder(BorderFactory.createLineBorder(Constants.COLOR_GREY_LABEL));
		searchBoxPanel.setBackground(Color.WHITE);
		GridBagConstraints gbc_searchBoxPanel = new GridBagConstraints();
		gbc_searchBoxPanel.insets = new Insets(0, 0, 0, 0);
		gbc_searchBoxPanel.fill = GridBagConstraints.BOTH;
		gbc_searchBoxPanel.gridx = 0;
		gbc_searchBoxPanel.gridy = 0;
		add(searchBoxPanel, gbc_searchBoxPanel);
		GridBagLayout gbl_searchBoxPanel = new GridBagLayout();
		gbl_searchBoxPanel.columnWidths = new int[]{38, 137, 68, 31};
		gbl_searchBoxPanel.rowHeights = new int[]{30, 0, 0, 0};
		gbl_searchBoxPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0};
		gbl_searchBoxPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		searchBoxPanel.setLayout(gbl_searchBoxPanel);
		
		
		ImageIcon userListIcon = new ImageIcon(getClass().getResource("ydayOverviewUsers.png"));
		Image userListImg = userListIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		ImageIcon newUserListIcon = new ImageIcon(userListImg);
		
		userListImgLbl = new JLabel("");
		GridBagConstraints gbc_userListImgLbl = new GridBagConstraints();
		gbc_userListImgLbl.insets = new Insets(0, 0, 5, 5);
		gbc_userListImgLbl.fill = GridBagConstraints.VERTICAL;
		gbc_userListImgLbl.gridx = 0;
		gbc_userListImgLbl.gridy = 0;
		searchBoxPanel.add(userListImgLbl, gbc_userListImgLbl);
		add(_usersList, gbc_usersListPane);
		userListImgLbl.setIcon(newUserListIcon);
		
		comboBox = new JComboBox();
		comboBox.setBackground(Color.WHITE);
		comboBox.setUI(new BlueComboBoxUI());
		comboBox.setOpaque(false);
		comboBox.setBorder(emptyBorder);
		comboBox.setFont(new Font("Tahoma", Font.BOLD, 11));
		comboBox.setForeground(new Color(0, 191, 255));
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"All Users","Online Users"}));
		comboBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				if (arg0.getStateChange() == ItemEvent.SELECTED){
					String selectedItem = arg0.getItem().toString();
					User currentUser ;
						_usersList.setViewportView(null);
						userListData = populateUserList(selectedItem);
						table.setModel(new UserListTableModel(userListData));
						_usersList.setViewportView(table);

						ScreenViewsTimelinePanel.setVisible(false);
						profileInfoPanel.setVisible(false);
						userNamelbl.setText("");
						flagLbl.setIcon(null);
						locationLbl.setText("");
						userImageLbl.setVisible(false);
						lblNewLabel.setVisible(false);
						
						setSelectedUser();
						userPanelPopulate();
				}
			}
		});
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.anchor = GridBagConstraints.WEST;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.VERTICAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		searchBoxPanel.add(comboBox, gbc_comboBox);
		
		
		
		ImageIcon downloadIcon = new ImageIcon(getClass().getResource("newDownload.png"));
		Image downloadImg = downloadIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		ImageIcon newDownloadIcon = new ImageIcon(downloadImg);
		
		downloadBtn = new JButton("");
		downloadBtn.setBackground(Color.WHITE);
		downloadBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_downloadBtn = new GridBagConstraints();
		gbc_downloadBtn.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadBtn.insets = new Insets(0, 0, 5, 5);
		gbc_downloadBtn.gridx = 2;
		gbc_downloadBtn.gridy = 0;
		//not to add this button as per QA Doc Item 88
	//	searchBoxPanel.add(downloadBtn, gbc_downloadBtn);
		downloadBtn.setIcon(newDownloadIcon);
		
		
		ImageIcon refreshIcon = new ImageIcon(getClass().getResource("newRefresh.png"));
		Image refreshImg = refreshIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		ImageIcon newRefreshIcon = new ImageIcon(refreshImg);
		
		refreshListBtn = new JButton("");
		refreshListBtn.setBackground(Color.WHITE);
		refreshListBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_refreshListBtn = new GridBagConstraints();
		gbc_refreshListBtn.fill = GridBagConstraints.BOTH;
		gbc_refreshListBtn.insets = new Insets(0, 0, 5, 0);
		gbc_refreshListBtn.gridx = 3;
		gbc_refreshListBtn.gridy = 0;
		//not to add this button as per QA Doc Item 88
	//	searchBoxPanel.add(refreshListBtn, gbc_refreshListBtn);
		refreshListBtn.setIcon(newRefreshIcon);
		
		ImageIcon searchIcon = new ImageIcon(getClass().getResource("newsearch.png"));
		Image searchImg = searchIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		ImageIcon newSearchIcon = new ImageIcon(searchImg);
		
		searchButton = new JButton("");
		searchButton.setBackground(Color.WHITE);
		GridBagConstraints gbc_searchButton = new GridBagConstraints();
		gbc_searchButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchButton.insets = new Insets(0, 0, 5, 0);
		gbc_searchButton.gridx = 0;
		gbc_searchButton.gridy = 1;
		searchBoxPanel.add(searchButton, gbc_searchButton);
		searchButton.setIcon(newSearchIcon);
		searchButton.setBorder(new MatteBorder(1, 0, 1, 0, (Color) Color.LIGHT_GRAY));
		searchButton.setActionCommand("SEARCH_BUTTON");
		searchButton.addActionListener(this);

		
		searchTextField = new JTextField();
		GridBagConstraints gbc_searchTextField = new GridBagConstraints();
		gbc_searchTextField.insets = new Insets(0, 0, 5, 0);
		gbc_searchTextField.gridwidth = 2;
		gbc_searchTextField.fill = GridBagConstraints.BOTH;
		gbc_searchTextField.gridx = 1;
		gbc_searchTextField.gridy = 1;
		searchBoxPanel.add(searchTextField, gbc_searchTextField);
		searchTextField.setColumns(10);
		searchTextField.setBorder(new MatteBorder(1, 0, 1, 0, (Color) Color.LIGHT_GRAY));
		searchTextField.setActionCommand("SEARCH_BY_ENTER");
		searchTextField.addActionListener(this);
		
		//By Omkar as per email from Chris dated 13-March-2016
		ImageIcon clearImg = new ImageIcon(getClass().getResource("Blue-Circle-X.png"));
		Image newImg = clearImg.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		clearImg = new ImageIcon(newImg);
		
		clearBtn = new JButton("");
		clearBtn.setBorder(new MatteBorder(1, 0, 1, 0, (Color) Color.LIGHT_GRAY));
		clearBtn.setBackground(Color.WHITE);
		clearBtn.setForeground(Color.WHITE);
		GridBagConstraints gbc_clearBtn = new GridBagConstraints();
		gbc_clearBtn.fill = GridBagConstraints.BOTH;
		gbc_clearBtn.insets = new Insets(0, 0, 5, 0);
		gbc_clearBtn.gridx = 3;
		gbc_clearBtn.gridy = 1;
		clearBtn.setIcon(clearImg);
		
		clearBtn.setActionCommand("CLEAR_TEXT");
		clearBtn.setBorder(new MatteBorder(1, 0, 1, 0, (Color) Color.LIGHT_GRAY));
		clearBtn.setFocusPainted(false);
		clearBtn.addActionListener(this);
		searchBoxPanel.add(clearBtn, gbc_clearBtn);
		
		
		totalUsersCountLbl = new JLabel("");
		totalUsersCountLbl.setForeground(Color.GRAY);
		GridBagConstraints gbc_totalUsersCountLbl = new GridBagConstraints();
		gbc_totalUsersCountLbl.anchor = GridBagConstraints.WEST;
		gbc_totalUsersCountLbl.gridwidth = 2;
		gbc_totalUsersCountLbl.insets = new Insets(0, 0, 0, 5);
		gbc_totalUsersCountLbl.gridx = 0;
		gbc_totalUsersCountLbl.gridy = 2;
		searchBoxPanel.add(totalUsersCountLbl, gbc_totalUsersCountLbl);
		
		
		todayDateLbl = new JLabel("");
		todayDateLbl.setForeground(Color.GRAY);
		GridBagConstraints gbc_todayDateLbl = new GridBagConstraints();
		gbc_todayDateLbl.anchor = GridBagConstraints.EAST;
		gbc_todayDateLbl.fill = GridBagConstraints.VERTICAL;
		gbc_todayDateLbl.gridwidth = 2;
		gbc_todayDateLbl.gridx = 2;
		gbc_todayDateLbl.gridy = 2;
		searchBoxPanel.add(todayDateLbl, gbc_todayDateLbl);
		
		Date today = new Date();
		String todayDate = today.toString();
		todayDate = todayDate.substring(4, 11) + ", " + todayDate.substring(24, 28);
		
		todayDateLbl.setText(""+todayDate+" - Today");
		

		
		
		
		userDetailsPanel = new JPanel();
		userDetailsPanel.setBorder(new MatteBorder(0, 1, 1, 1, (Color) Color.LIGHT_GRAY));
		userDetailsPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		//userDetailsPanel.setBorder(new LineBorder(Constants.COLOR_GREY_LABEL));
		GridBagConstraints gbc_userDetailsPanel = new GridBagConstraints();
		gbc_userDetailsPanel.gridwidth = 2;
		gbc_userDetailsPanel.insets = new Insets(0, 0, 0, 0);
		gbc_userDetailsPanel.fill = GridBagConstraints.BOTH;
		gbc_userDetailsPanel.gridx = 1;
		gbc_userDetailsPanel.gridy = 0;
		add(userDetailsPanel, gbc_userDetailsPanel);
		GridBagLayout gbl_userDetailsPanel = new GridBagLayout();
		gbl_userDetailsPanel.columnWidths = new int[]{500, 825};
		gbl_userDetailsPanel.rowHeights = new int[]{0, 0};
		gbl_userDetailsPanel.columnWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE};
		gbl_userDetailsPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		userDetailsPanel.setLayout(gbl_userDetailsPanel);
		
		entirePanelHeader = new JPanel();
		entirePanelHeader.setBackground(Color.WHITE);
		entirePanelHeader.setBorder(null);
		GridBagConstraints gbc_entirePanelHeader = new GridBagConstraints();
		gbc_entirePanelHeader.insets = new Insets(0, 0, 0, 5);
		gbc_entirePanelHeader.fill = GridBagConstraints.BOTH;
		gbc_entirePanelHeader.gridx = 0;
		gbc_entirePanelHeader.gridy = 0;
		userDetailsPanel.add(entirePanelHeader, gbc_entirePanelHeader);
		GridBagLayout gbl_entirePanelHeader = new GridBagLayout();
		gbl_entirePanelHeader.columnWidths = new int[]{58, 0, 0};
		gbl_entirePanelHeader.rowHeights = new int[]{0, 0, 0, 0};
		gbl_entirePanelHeader.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_entirePanelHeader.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		entirePanelHeader.setLayout(gbl_entirePanelHeader);
		
		
		ImageIcon userIcon = new ImageIcon(getClass().getResource("imageUser.png"));
		Image userImg = userIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		ImageIcon newUserIcon = new ImageIcon(userImg);
		
		userImageLbl = new JLabel("");
		GridBagConstraints gbc_userImageLbl = new GridBagConstraints();
		gbc_userImageLbl.gridheight = 3;
		gbc_userImageLbl.insets = new Insets(0, 0, 0, 5);
		gbc_userImageLbl.fill = GridBagConstraints.VERTICAL;
		gbc_userImageLbl.gridx = 0;
		gbc_userImageLbl.gridy = 0;
		entirePanelHeader.add(userImageLbl, gbc_userImageLbl);
		userImageLbl.setIcon(newUserIcon);
		
		userNamelbl = new JLabel("");
		userNamelbl.setFont(new Font("Tahoma", Font.BOLD, 16));
		GridBagConstraints gbc_userNamelbl = new GridBagConstraints();
		gbc_userNamelbl.anchor = GridBagConstraints.SOUTH;
		gbc_userNamelbl.fill = GridBagConstraints.HORIZONTAL;
		gbc_userNamelbl.insets = new Insets(0, 0, 5, 0);
		gbc_userNamelbl.gridx = 1;
		gbc_userNamelbl.gridy = 0;
		entirePanelHeader.add(userNamelbl, gbc_userNamelbl);
		
		locationInnerPanel = new JPanel();
		locationInnerPanel.setBorder(null);
		locationInnerPanel.setBackground(Color.WHITE);
		GridBagConstraints gbc_locationInnerPanel = new GridBagConstraints();
		gbc_locationInnerPanel.insets = new Insets(0, 0, 5, 0);
		gbc_locationInnerPanel.fill = GridBagConstraints.BOTH;
		gbc_locationInnerPanel.gridx = 1;
		gbc_locationInnerPanel.gridy = 1;
		entirePanelHeader.add(locationInnerPanel, gbc_locationInnerPanel);
		GridBagLayout gbl_locationInnerPanel = new GridBagLayout();
		gbl_locationInnerPanel.columnWidths = new int[]{33, 0};
		gbl_locationInnerPanel.rowHeights = new int[]{31, 0};
		gbl_locationInnerPanel.columnWeights = new double[]{0.0, 0.90};
		gbl_locationInnerPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		locationInnerPanel.setLayout(gbl_locationInnerPanel);
		
		
		
		flagLbl = new JLabel("");
		GridBagConstraints gbc_flagLbl = new GridBagConstraints();
		gbc_flagLbl.insets = new Insets(0, 0, 0, 5);
		gbc_flagLbl.fill = GridBagConstraints.BOTH;
		gbc_flagLbl.gridx = 0;
		gbc_flagLbl.gridy = 0;
		locationInnerPanel.add(flagLbl, gbc_flagLbl);
		//flagLbl.setIcon(newflagIcon);
		
		locationLbl = new JLabel("");
		GridBagConstraints gbc_locationLbl = new GridBagConstraints();
		gbc_locationLbl.fill = GridBagConstraints.BOTH;
		gbc_locationLbl.gridx = 1;
		gbc_locationLbl.gridy = 0;
		locationInnerPanel.add(locationLbl, gbc_locationLbl);
		
		lblNewLabel = new JLabel("ONLINE");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setBackground(new Color(34, 139, 34));
		lblNewLabel.setOpaque(true);
		lblNewLabel.setBorder(new CompoundBorder(new LineBorder(new Color(34, 139, 34), 1), new EmptyBorder(3, 5, 3, 5)));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 2;
		entirePanelHeader.add(lblNewLabel, gbc_lblNewLabel);
		
		overViewGraphPanel = new JPanel();
		overViewGraphPanel.setBorder(null);
		overViewGraphPanel.setBackground(Color.WHITE);
		GridBagConstraints gbc_overViewGraphPanel = new GridBagConstraints();
		gbc_overViewGraphPanel.fill = GridBagConstraints.BOTH;
		gbc_overViewGraphPanel.gridx = 1;
		gbc_overViewGraphPanel.gridy = 0;
		//not to be done inthis version
		//userDetailsPanel.add(overViewGraphPanel, gbc_overViewGraphPanel);
		overViewGraphPanel.setLayout(new BorderLayout(0, 0));
		
		profileInfoPanel = new JPanel();
		profileInfoPanel.setBorder(new MatteBorder(0, 0, 0, 1, (Color) new Color(192, 192, 192)));
		profileInfoPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		profileInfoPanel.setPreferredSize(new Dimension(500, 600));
		GridBagConstraints gbc_profileInfoPanel = new GridBagConstraints();
		gbc_profileInfoPanel.insets = new Insets(0, 0, 0, 0);
		gbc_profileInfoPanel.fill = GridBagConstraints.BOTH;
		gbc_profileInfoPanel.gridx = 1;
		gbc_profileInfoPanel.gridy = 1;
		add(profileInfoPanel, gbc_profileInfoPanel);
		profileInfoPanel.setSize(300,300);
		profileInfoPanel.setVisible(false);
		profileInfoPanel.setBackground(Color.WHITE);
		//profileInfoPanel.setBorder(null);
		GridBagLayout gbl_profileInfoPanel = new GridBagLayout();
		gbl_profileInfoPanel.columnWidths = new int[]{21, 82, 0, 141, 0};
		gbl_profileInfoPanel.rowHeights = new int[]{0, 0, 27, 27, 27, 27, 0, 0, 0, 27, 0, 27, 27, 27, 27, 27, 0, 27, 27, 27, 27, 0};
		gbl_profileInfoPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_profileInfoPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		profileInfoPanel.setLayout(gbl_profileInfoPanel);
		
		JLabel userNameLabel = new JLabel("");
		GridBagConstraints gbc_userNameLabel = new GridBagConstraints();
		gbc_userNameLabel.fill = GridBagConstraints.VERTICAL;
		gbc_userNameLabel.gridwidth = 3;
		gbc_userNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_userNameLabel.gridx = 1;
		gbc_userNameLabel.gridy = 0;
		profileInfoPanel.add(userNameLabel, gbc_userNameLabel);
		
		JLabel lblProfileStats = new JLabel("Profile Stats");
		lblProfileStats.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblProfileStats = new GridBagConstraints();
		gbc_lblProfileStats.anchor = GridBagConstraints.WEST;
		gbc_lblProfileStats.gridwidth = 4;
		gbc_lblProfileStats.insets = new Insets(0, 0, 5, 0);
		gbc_lblProfileStats.gridx = 1;
		gbc_lblProfileStats.gridy = 2;
		profileInfoPanel.add(lblProfileStats, gbc_lblProfileStats);
		
		JLabel lblFirstSeen = new JLabel("First Seen");
		lblFirstSeen.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblFirstSeen = new GridBagConstraints();
		gbc_lblFirstSeen.anchor = GridBagConstraints.EAST;
		gbc_lblFirstSeen.insets = new Insets(0, 0, 5, 5);
		gbc_lblFirstSeen.gridx = 1;
		gbc_lblFirstSeen.gridy = 3;
		profileInfoPanel.add(lblFirstSeen, gbc_lblFirstSeen);
		
		lblFirstSeenValue = new JLabel("");
		GridBagConstraints gbc_lblFirstSeenValue = new GridBagConstraints();
		gbc_lblFirstSeenValue.anchor = GridBagConstraints.WEST;
		gbc_lblFirstSeenValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblFirstSeenValue.gridx = 3;
		gbc_lblFirstSeenValue.gridy = 3;
		profileInfoPanel.add(lblFirstSeenValue, gbc_lblFirstSeenValue);
		
		JLabel lblLastSeen = new JLabel("Last Seen");
		lblLastSeen.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblLastSeen = new GridBagConstraints();
		gbc_lblLastSeen.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastSeen.anchor = GridBagConstraints.EAST;
		gbc_lblLastSeen.gridx = 1;
		gbc_lblLastSeen.gridy = 4;
		profileInfoPanel.add(lblLastSeen, gbc_lblLastSeen);
		
		lblLastSeenValue = new JLabel("");
		GridBagConstraints gbc_lblLastSeenValue = new GridBagConstraints();
		gbc_lblLastSeenValue.anchor = GridBagConstraints.WEST;
		gbc_lblLastSeenValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastSeenValue.gridx = 3;
		gbc_lblLastSeenValue.gridy = 4;
		profileInfoPanel.add(lblLastSeenValue, gbc_lblLastSeenValue);
		
		JLabel lblLastDays = new JLabel("Last 7 Days");
		lblLastDays.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblLastDays = new GridBagConstraints();
		gbc_lblLastDays.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastDays.anchor = GridBagConstraints.EAST;
		gbc_lblLastDays.gridx = 1;
		gbc_lblLastDays.gridy = 5;
		profileInfoPanel.add(lblLastDays, gbc_lblLastDays);
		
		lblSessionDurationValue = new JLabel("");
		GridBagConstraints gbc_lblSessionDurationValue = new GridBagConstraints();
		gbc_lblSessionDurationValue.anchor = GridBagConstraints.WEST;
		gbc_lblSessionDurationValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblSessionDurationValue.gridx = 3;
		gbc_lblSessionDurationValue.gridy = 5;
		profileInfoPanel.add(lblSessionDurationValue, gbc_lblSessionDurationValue);
		
		lblTotalVisitsValue = new JLabel("");
		GridBagConstraints gbc_lblTotalVisitsValue = new GridBagConstraints();
		gbc_lblTotalVisitsValue.anchor = GridBagConstraints.WEST;
		gbc_lblTotalVisitsValue.gridwidth = 2;
		gbc_lblTotalVisitsValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalVisitsValue.gridx = 2;
		gbc_lblTotalVisitsValue.gridy = 6;
		profileInfoPanel.add(lblTotalVisitsValue, gbc_lblTotalVisitsValue);
		
		lblTotalActionsValue = new JLabel("");
		GridBagConstraints gbc_lblTotalActionsValue = new GridBagConstraints();
		gbc_lblTotalActionsValue.anchor = GridBagConstraints.WEST;
		gbc_lblTotalActionsValue.gridwidth = 2;
		gbc_lblTotalActionsValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalActionsValue.gridx = 2;
		gbc_lblTotalActionsValue.gridy = 7;
		profileInfoPanel.add(lblTotalActionsValue, gbc_lblTotalActionsValue);
		
		JLabel lblProfileInfo = new JLabel("Profile Info");
		lblProfileInfo.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblProfileInfo = new GridBagConstraints();
		gbc_lblProfileInfo.anchor = GridBagConstraints.WEST;
		gbc_lblProfileInfo.gridwidth = 3;
		gbc_lblProfileInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblProfileInfo.gridx = 1;
		gbc_lblProfileInfo.gridy = 10;
		profileInfoPanel.add(lblProfileInfo, gbc_lblProfileInfo);
		
		JLabel lblName = new JLabel("Name");
		lblName.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 1;
		gbc_lblName.gridy = 11;
		profileInfoPanel.add(lblName, gbc_lblName);
		
		lblNameValue = new JLabel("");
		GridBagConstraints gbc_lblNameValue = new GridBagConstraints();
		gbc_lblNameValue.anchor = GridBagConstraints.WEST;
		gbc_lblNameValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblNameValue.gridx = 3;
		gbc_lblNameValue.gridy = 11;
		profileInfoPanel.add(lblNameValue, gbc_lblNameValue);
		
		JLabel lblEmail = new JLabel("E-mail");
		lblEmail.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblEmail = new GridBagConstraints();
		gbc_lblEmail.anchor = GridBagConstraints.EAST;
		gbc_lblEmail.insets = new Insets(0, 0, 5, 5);
		gbc_lblEmail.gridx = 1;
		gbc_lblEmail.gridy = 12;
		profileInfoPanel.add(lblEmail, gbc_lblEmail);
		
		lblEmailValue = new JLabel("");
		GridBagConstraints gbc_lblEmailValue = new GridBagConstraints();
		gbc_lblEmailValue.anchor = GridBagConstraints.WEST;
		gbc_lblEmailValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblEmailValue.gridx = 3;
		gbc_lblEmailValue.gridy = 12;
		profileInfoPanel.add(lblEmailValue, gbc_lblEmailValue);
		
		JLabel lblGroup = new JLabel("Group");
		lblGroup.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblGroup = new GridBagConstraints();
		gbc_lblGroup.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblGroup.insets = new Insets(0, 0, 5, 5);
		gbc_lblGroup.gridx = 1;
		gbc_lblGroup.gridy = 13;
		profileInfoPanel.add(lblGroup, gbc_lblGroup);
		
		lblGroupValue = new JTextArea();
		lblGroupValue.setWrapStyleWord(true);
		lblGroupValue.setLineWrap(true);
		lblGroupValue.setPreferredSize(new Dimension(141,40));
		lblGroupValue.setAlignmentX(JTextArea.TOP_ALIGNMENT);
		GridBagConstraints gbc_lblGroupValue = new GridBagConstraints();
		gbc_lblGroupValue.gridwidth = 2;
		gbc_lblGroupValue.fill = GridBagConstraints.BOTH;
		gbc_lblGroupValue.insets = new Insets(0, 0, 5, 10);
		gbc_lblGroupValue.gridx = 3;
		gbc_lblGroupValue.gridy = 13;
		profileInfoPanel.add(lblGroupValue, gbc_lblGroupValue);
		
		JLabel lblSecurity = new JLabel("Security");
		lblSecurity.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblSecurity = new GridBagConstraints();
		gbc_lblSecurity.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblSecurity.insets = new Insets(0, 0, 5, 5);
		gbc_lblSecurity.gridx = 1;
		gbc_lblSecurity.gridy = 14;
		profileInfoPanel.add(lblSecurity, gbc_lblSecurity);
		
		lblSecurityValue = new JTextArea();
		lblSecurityValue.setPreferredSize(new Dimension(141,40));
		lblSecurityValue.setWrapStyleWord(true);
		lblSecurityValue.setLineWrap(true);
		GridBagConstraints gbc_lblSecurityValue = new GridBagConstraints();
		gbc_lblSecurityValue.gridwidth = 2;
		gbc_lblSecurityValue.fill = GridBagConstraints.BOTH;
		gbc_lblSecurityValue.insets = new Insets(0, 0, 5, 10);
		gbc_lblSecurityValue.gridx = 3;
		gbc_lblSecurityValue.gridy = 14;
		profileInfoPanel.add(lblSecurityValue, gbc_lblSecurityValue);
		
		JLabel lblProfileActivity = new JLabel("Profile Activity");
		lblProfileActivity.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblProfileActivity = new GridBagConstraints();
		gbc_lblProfileActivity.anchor = GridBagConstraints.WEST;
		gbc_lblProfileActivity.gridwidth = 3;
		gbc_lblProfileActivity.insets = new Insets(0, 0, 5, 5);
		gbc_lblProfileActivity.gridx = 1;
		gbc_lblProfileActivity.gridy = 17;
		profileInfoPanel.add(lblProfileActivity, gbc_lblProfileActivity);
		
		JLabel lblAllActions = new JLabel("All Actions");
		lblAllActions.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblAllActions = new GridBagConstraints();
		gbc_lblAllActions.anchor = GridBagConstraints.EAST;
		gbc_lblAllActions.insets = new Insets(0, 0, 5, 5);
		gbc_lblAllActions.gridx = 1;
		gbc_lblAllActions.gridy = 18;
		profileInfoPanel.add(lblAllActions, gbc_lblAllActions);
		
		lblAllActionsValue = new JLabel("");
		GridBagConstraints gbc_lblAllActionsValue = new GridBagConstraints();
		gbc_lblAllActionsValue.anchor = GridBagConstraints.WEST;
		gbc_lblAllActionsValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblAllActionsValue.gridx = 3;
		gbc_lblAllActionsValue.gridy = 18;
		profileInfoPanel.add(lblAllActionsValue, gbc_lblAllActionsValue);
		
		JLabel lblScreenViews = new JLabel("Screen Views");
		lblScreenViews.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblScreenViews = new GridBagConstraints();
		gbc_lblScreenViews.anchor = GridBagConstraints.EAST;
		gbc_lblScreenViews.insets = new Insets(0, 0, 5, 5);
		gbc_lblScreenViews.gridx = 1;
		gbc_lblScreenViews.gridy = 19;
		profileInfoPanel.add(lblScreenViews, gbc_lblScreenViews);
		
		lblScreenViewsValue = new JLabel("");
		GridBagConstraints gbc_lblScreenViewsValue = new GridBagConstraints();
		gbc_lblScreenViewsValue.anchor = GridBagConstraints.WEST;
		gbc_lblScreenViewsValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblScreenViewsValue.gridx = 3;
		gbc_lblScreenViewsValue.gridy = 19;
		profileInfoPanel.add(lblScreenViewsValue, gbc_lblScreenViewsValue);
		
		lblCurrentScreen = new JLabel("Current Screen");
		lblCurrentScreen.setForeground(Constants.COLOR_DISCRIPTOR);
		GridBagConstraints gbc_lblCurrentScreen = new GridBagConstraints();
		gbc_lblCurrentScreen.anchor = GridBagConstraints.EAST;
		gbc_lblCurrentScreen.insets = new Insets(0, 0, 0, 5);
		gbc_lblCurrentScreen.gridx = 1;
		gbc_lblCurrentScreen.gridy = 20;
		profileInfoPanel.add(lblCurrentScreen, gbc_lblCurrentScreen);
		
		lblCurrentScreenVal = new JLabel("");
		GridBagConstraints gbc_lblCurrentScreenVal = new GridBagConstraints();
		gbc_lblCurrentScreenVal.insets = new Insets(0, 0, 0, 5);
		gbc_lblCurrentScreenVal.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentScreenVal.gridx = 3;
		gbc_lblCurrentScreenVal.gridy = 20;
		profileInfoPanel.add(lblCurrentScreenVal, gbc_lblCurrentScreenVal);
		
		ScreenViewsTimelinePanel = new JPanel();
		ScreenViewsTimelinePanel.setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.LIGHT_GRAY));
		ScreenViewsTimelinePanel.setPreferredSize(new Dimension(1000, 1000));
		ScreenViewsTimelinePanel.setBackground(Color.WHITE);
		GridBagConstraints gbc_ScreenViewsTimelinePanel = new GridBagConstraints();
		gbc_ScreenViewsTimelinePanel.fill = GridBagConstraints.BOTH;
		gbc_ScreenViewsTimelinePanel.gridx = 2;
		gbc_ScreenViewsTimelinePanel.gridy = 1;
		add(ScreenViewsTimelinePanel, gbc_ScreenViewsTimelinePanel);
		GridBagLayout gbl_ScreenViewsTimelinePanel = new GridBagLayout();
		gbl_ScreenViewsTimelinePanel.columnWidths = new int[]{0};
		gbl_ScreenViewsTimelinePanel.rowHeights = new int[]{0, 0};
		gbl_ScreenViewsTimelinePanel.columnWeights = new double[]{ 1.0};
		gbl_ScreenViewsTimelinePanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		ScreenViewsTimelinePanel.setLayout(gbl_ScreenViewsTimelinePanel);
		
		screenTimelineScroll = new JScrollPane();
		screenTimelineScroll.setPreferredSize(new Dimension(850,1000));
		screenTimelineScroll.setBorder(BorderFactory.createEmptyBorder());
		screenTimelineScroll.setViewportBorder(BorderFactory.createEmptyBorder());
		
		GridBagConstraints gbc_screenTimelineScroll = new GridBagConstraints();
		gbc_screenTimelineScroll.fill = GridBagConstraints.BOTH;
		screenTimelineScroll.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		gbc_screenTimelineScroll.gridx = 0;
		gbc_screenTimelineScroll.gridy = 0;
		ScreenViewsTimelinePanel.add(screenTimelineScroll, gbc_screenTimelineScroll);
		
		//add table 
		
		sessionTable = new JTable();
		sessionTable.setTableHeader(null);
		sessionTable.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		sessionTable.setBorder(BorderFactory.createEmptyBorder());
		sessionTable.setShowGrid(false);
		sessionTable.setShowHorizontalLines(false);
		sessionTable.setShowVerticalLines(false);
		sessionTable.setIntercellSpacing(new Dimension(0,0));
		sessionTable.setDefaultRenderer(IA_SessionDetailPanel.class, new sessionTableCellRenderer());
		screenTimelineScroll.setViewportView(sessionTable);
		screenTimelineScroll.setBorder(BorderFactory.createEmptyBorder());
		
		this.comboSelction = 0;
		this.searchString = null;
		
		populateUsersList();
		revalidate();
		repaint();
	}

	public void populateUsersList()
	{
		this.userProfiles = rpc.getUserProfiles();
		
		//populate users list
		
		
		Dataset allUsers = rpc.getAllLoggedInUsers(allProjects, this.currentProject);

		long diffDate = 0;
		long diffHours1 = 0;
		userListData = new ArrayList<IA_UserListPanel>();
		List<IA_UserListPanel> sortedData = new ArrayList<IA_UserListPanel>();
		alluserListData = new ArrayList<IA_UserListPanel>();
		String loggedInStr = "";
		String userName = "";
		String userWithProfile = "";
		String profileName = "";
		int noOfUsers = 0, i;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
		DateFormat lastLogindf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
		if(allUsers != null)
		{
			noOfUsers = allUsers.getRowCount();
			System.out.println("no of logged in users : " + noOfUsers);
			for(i=0; i<noOfUsers; i++)
			{
				loggedInStr = "";
				if(allUsers.getValueAt(i, 0) != null)
				{
					userWithProfile = allUsers.getValueAt(i, 0).toString().trim();
					userName = userWithProfile.split(":")[0];
					profileName = userWithProfile.split(":")[1];
					
					
				}
				//check if user is online 
				boolean isUserOnline = rpc.checkUserOnlineOrOffline( currentProject, allProjects, userName, profileName);
				if(isUserOnline)
				{
					loggedInStr = "Online";
				}
				else
				{
					String lastSeen = allUsers.getValueAt(i, 1).toString();
			
					if(lastSeen != "" )
					{
						Date todayDate = new Date(); 
						
						String today = df.format(todayDate);
				
						try 
						{
							Date firstDateToday = df.parse(today);
							Date logOutTime = lastLogindf.parse(lastSeen);
							
							diffDate = firstDateToday.getTime() - logOutTime.getTime();
							diffHours1 = diffDate / (60 * 60 * 1000);
							if(diffHours1 > 24)
							{
								diffHours1 = (int)(diffHours1 / 24);
								loggedInStr = diffHours1 + " day(s) ago";
							}
							else
							{
								if(diffHours1 == 0){
									Date logOutTime1 ;
									try {
										logOutTime1 = lastLogindf.parse(lastSeen);
										diffDate = todayDate.getTime() - logOutTime1.getTime() ;
										diffHours1 = diffDate /(60 * 1000);
										System.out.println("diffHour Value : " + diffHours1);
										loggedInStr = "" + diffHours1 + " minutes ago" ;
										//screenViewPanelData.lblHoursAgo.setText("" + diffHours + "Minutes ago");
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								
								}
								else
								loggedInStr = diffHours1 + " hours ago";
							}
					
						} catch (ParseException e)
						{
									e.printStackTrace();
						}
				
					}
				}
				
				IA_UserListPanel userListPanel = new IA_UserListPanel();
				userListPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
				userListPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				userListPanel.userNameLbl.setText(userName);
				userListPanel.lblProfileName.setText(profileName);
				String userLocation = "";
				System.out.println("getUserInformation called for user : " + userName + " , profile : " + profileName);
				UsersOverviewInformation _uinfo = rpc.getUserInformation( Constants.LAST_SEVEN_DAYS, userName, this.currentProject, this.allProjects, profileName);
				if(_uinfo != null)
				{
					userLocation = _uinfo.getLocation();
					if(userLocation.toLowerCase().contains("india"))
					{
						userListPanel.staticImgLbl.setIcon(indiaUserIcon);
					}
					else if(userLocation.toLowerCase().contains("usa") || userLocation.toLowerCase().contains("united states"))
					{
						userListPanel.staticImgLbl.setIcon(usaUserIcon);
					}
					else if(userLocation.toLowerCase().contains("mexico"))
					{
						userListPanel.staticImgLbl.setIcon(mexicoUserIcon);
					}
					else if(userLocation.toLowerCase().contains("canada"))
					{
						userListPanel.staticImgLbl.setIcon(canadaUserIcon);
					}
				}
				userListPanel.timeSinceLastSeenLbl.setText(loggedInStr);
				
				
				
				//store all users data for later use for combo box selection
				
				if (userListPanel.timeSinceLastSeenLbl.getText().contains("Online")){
				 userListData.add(userListPanel);
				 alluserListData.add(userListPanel);
				}
				else	
					sortedData.add(userListPanel);
		
			}
		}
		
		//add sorting logic
		Collections.sort(sortedData, new Comparator<IA_UserListPanel>() {

			@Override
			public int compare(IA_UserListPanel o1, IA_UserListPanel o2) {
				String t1 = o1.timeSinceLastSeenLbl.getText().toLowerCase();
				String t2 = o2.timeSinceLastSeenLbl.getText().toLowerCase();
				
				int t1flag = -1, t2flag = -1;
				int returnVal = 0;
				int t1Val, t2Val;
				
				if(t1.contains("minutes"))
				{
					t1flag = 0; //minutes
				}
				else if(t1.contains("hour"))
				{
					t1flag = 1;
				}
				else if(t1.contains("day"))
				{
					t1flag = 2;
				}
				else if(t1.contains("Offline"))
				{
					t1flag = 3;
				}
				
				if(t2.contains("minutes"))
				{
					t2flag = 0; //minutes
				}
				else if(t2.contains("hour"))
				{
					t2flag = 1;
				}
				else if(t2.contains("day"))
				{
					t2flag = 2;
				}
				else if(t2.contains("Offline"))
				{
					t2flag = 3;
				}
				
				
				if(t1flag == -1)
				{
					returnVal = 1;
				}
				else if(t2flag == -1)
				{
					returnVal = -1;
				}
				else if(t1flag > t2flag)
				{
					returnVal = 1;
				}
				else if (t1flag < t2flag)
				{
					returnVal = -1;
				}
				else
				{
//					if(t1flag == -1)
//					{
//						returnVal = 1;
//					}
//					else if(t2flag == -1)
//					{
//						returnVal = -1;
//					}
//					else
//					{
						t1Val = Integer.parseInt(t1.split("\\s+")[0]);
						t2Val = Integer.parseInt(t2.split("\\s+")[0]);
						
						if(t1Val == t2Val)
						{
							returnVal = 0;
						}
						else if(t1Val > t2Val)
						{
							returnVal = 1;
						}
						else
						{
							returnVal = -1;
						}
					}
				//}
				return returnVal;
			}
		});
		alluserListData.addAll(sortedData);
		userListData.addAll(sortedData);
		//check for combo selection
		if(this.comboSelction == 1)
		{
			this.userListData = this.populateUserList("Online Users");
		}
		
		//check for search string
		if(this.searchString != null && this.searchString.length() > 0)
		{
			this.userListData = this.populateUserList("User Search");
		}
		
		
		table = new JTable(new UserListTableModel(userListData));
		table.setTableHeader(null);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.setShowGrid(false);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		table.setIntercellSpacing(new Dimension(0,0));
		
		_usersList.setViewportView(table);
		_usersList.setViewportBorder(BorderFactory.createEmptyBorder());
		table.setRowHeight(60);
		table.setRowMargin(1);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				resetUserInformation();
				userPanelPopulate();
			}
		});
		table.setDefaultRenderer(IA_UserListPanel.class, new UserTableCellRenderer());
		totalUsersCountLbl.setText("" + userListData.size() + " " + "people");
		
		setSelectedUser();
	}
	
	private void setSelectedUser()
	{
		int selectedIndex = -1;
		//search in userListData for selection
				if(this.selectedUser != null)
				{
					int totalUsers = this.userListData.size();
					String _userName = "";
					String _profileName = "";
					for(int indx=0; indx<totalUsers; indx++)
					{
						_userName = this.userListData.get(indx).userNameLbl.getText();
						_profileName = this.userListData.get(indx).lblProfileName.getText();
						
						if(_userName.compareToIgnoreCase(this.selectedUser) == 0 && _profileName.compareToIgnoreCase(this.profileSelectedUser) == 0)
						{
							selectedIndex = indx;
						}
						
					}
					
					if(selectedIndex != -1)
					{
						table.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
					}
					else
					{
						this.ScreenViewsTimelinePanel.setVisible(false);
						this.profileInfoPanel.setVisible(false);
						this.userNamelbl.setText("");
						this.flagLbl.setIcon(null);
						this.locationLbl.setText("");
						this.userImageLbl.setVisible(false);
						this.lblNewLabel.setVisible(false);
					}
				}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String aCmd = arg0.getActionCommand();
		JButton btn;
		String selectedUser = "";
		List<ContactInfo> contact;
		String _contact = "", _contactType;
		int noOfRoles;
		String roles = "";
		int noOfcontacts;
		int i;
		String[] userRoles;
		IA_UserListPanel userList;
		List<IA_UserListPanel> searchList = new ArrayList<IA_UserListPanel>();
		
		if(aCmd.compareToIgnoreCase("CLEAR_TEXT") == 0){
			this.searchTextField.setText("");
			this.searchTextField.requestFocus();
//			populateUsersList();
			
			_usersList.setViewportView(null);
			if(comboSelction == 0)
			{
				userListData = this.alluserListData;
				totalUsersCountLbl.setText("" + alluserListData.size() + " " + "people");
			}
			else
			{
				userListData = this.onlineuserListData;
				totalUsersCountLbl.setText("" + onlineuserListData.size() + " " + "people");
			}
			table.setModel(new UserListTableModel(userListData));
			_usersList.setViewportView(table);
			ScreenViewsTimelinePanel.setVisible(false);
			profileInfoPanel.setVisible(false);
			userNamelbl.setText("");
			flagLbl.setIcon(null);
			locationLbl.setText("");
			userImageLbl.setVisible(false);
			lblNewLabel.setVisible(false);
		}
		else if(aCmd.compareToIgnoreCase("SEARCH_BUTTON") == 0 || aCmd.compareToIgnoreCase("SEARCH_BY_ENTER") == 0)
		{
			_usersList.setViewportView(null);
			userListData = populateUserList("User Search");
			table.setModel(new UserListTableModel(userListData));
			_usersList.setViewportView(table);
			ScreenViewsTimelinePanel.setVisible(false);
			profileInfoPanel.setVisible(false);
			userNamelbl.setText("");
			flagLbl.setIcon(null);
			locationLbl.setText("");
			userImageLbl.setVisible(false);
			lblNewLabel.setVisible(false);
		}
		
		else if(aCmd.compareToIgnoreCase("userClick") == 0)
		{
//			
//			resetUserInformation();
//			btn = (JButton)arg0.getSource();
//			selectedUser = btn.getText().trim();
//			
//			Iterator<User> retrieveUsers;
//			retrieveUsers = this.userProfiles.iterator();
//			User currentUser ;
//			while(retrieveUsers.hasNext())
//			{
//				currentUser = retrieveUsers.next();
//				System.out.println("current user : " + currentUser.get(User.Username));
//				if(currentUser.get(User.Username).trim().compareToIgnoreCase(selectedUser) == 0)
//				{
//					contact = currentUser.getContactInfo();
//					if(contact != null)
//					{
//						noOfcontacts = contact.size();
//						for(i=0; i<noOfcontacts; i++)
//						{
//						
//							_contactType = contact.get(i).getContactType() ;
//							System.out.println("contact type is " + _contactType);
//							if(_contactType.compareTo("email") == 0)
//							{
//								_contact = _contact + "  " + contact.get(i).getValue();
//							}
//						}
//					}
//					
//					this.lblNameValue.setText(selectedUser);
//					this.lblEmailValue.setText(_contact);
//					
//					//get roles information
//					Collection<String> tempRoles = currentUser.getRoles();
//					if(tempRoles != null)
//					{
//						userRoles = tempRoles.toArray(new String[currentUser.getRoles().size()]);
//						noOfRoles = userRoles.length;
//						for(i=0;i<noOfRoles; i++)
//						{
//							if(i == 0)
//							{
//								roles = roles + userRoles[i] ;
//							}
//							else
//							{
//								roles = roles  + "," + userRoles[i] ;
//							}
//						}
//					}
//					
//					this.lblGroupValue.setText(roles);
//					this.lblSecurityValue.setText(roles);
//					//retrieve other  infrmation for user
//					UsersOverviewInformation _uinfo = rpc.getUserInformation( Constants.LAST_SEVEN_DAYS, selectedUser, this.currentProject, this.allProjects);
//					if(_uinfo != null)
//					{
//						String date = _uinfo.getFirstSeen().substring(0, 13);
////						if(_uinfo.getFirstSeen() != null)
////						{
//							this.lblFirstSeenValue.setText(date);
////						}
//						if(_uinfo.getLastSeen() != null)
//						{
//							date  = _uinfo.getLastSeen().substring(0, 13);
//							this.lblLastSeenValue.setText(date);
//						}
//						if(_uinfo.getTotalSessionsLength() != null)
//						{
//						this.lblSessionDurationValue.setText(_uinfo.getTotalSessionsLength().substring(0, 5) + " hours");
//						}
//						this.lblTotalVisitsValue.setText(_uinfo.getTotalVisits() + " visits");
//						
//						
//						List<ScreensCount> listScreens =  _uinfo.getScreensViewed();
//						int totalScreens = 0;
//						if(listScreens != null)
//						{
//							int k=0;
//							int noOfScreens = listScreens.size();
//							
//							for(k=0;k<noOfScreens; k++)
//							{
//								totalScreens = totalScreens + listScreens.get(k).getNoOfViews();
//								
//							}
//								this.lblScreenViewsValue.setText("" +totalScreens);
//						}
//						this.lblAllActionsValue.setText("" + (_uinfo.getTotalActions() + totalScreens));
//						this.lblTotalActionsValue.setText("" + (_uinfo.getTotalActions() + totalScreens) + " actions");
//						this.lblCurrentScreenVal.setText(_uinfo.getCurrentScreen());
//						this.locationLbl.setText(_uinfo.getLocation());
//							
//					}
//					
//					
//					//set the screens viewed information
//					Dataset screenViewsDS = rpc.getAllScreenViewsDatesPerUser( selectedUser,this.currentProject, this.allProjects);
//					
//					List<IA_SessionDetailPanel> sessionData = new ArrayList<IA_SessionDetailPanel>();
//					int rows = screenViewsDS.getRowCount();
//					for (int ii=0 ; ii < rows ; ii++){
//						IA_SessionDetailPanel sessionValue = new IA_SessionDetailPanel();
//						Double sessionDate = screenViewsDS.getPrimitiveValueAt(0, 2);
//						sessionValue.sessionDateLbl.setText("" + sessionDate);
//						
//						sessionData.add(sessionValue);
//						
//						
//					}
//					
//					sessionTable = new JTable(new SeesionTableModel(sessionData));
//					sessionTable.setTableHeader(null);
//				
//					sessionTable.setDefaultRenderer(IA_SessionDetailPanel.class, new sessionTableCellRenderer());
//					sessionTable.setBorder(null);
//					
//					screenTimelineScroll.setViewportView(sessionTable);
//					
//				
//					
////					JTable screenViewsTable = new JTable(new ScreenViewTableModel());
////					screenViewsTable.setData(screenViewsDS);
////					this.screenTimelineScroll.setViewportView(screenViewsTable);
//				}
//			}
			this.profileInfoPanel.setVisible(true);
		}
		
		
	}
	private void resetUserInformation(){
		this.lblFirstSeenValue.setText("");
		this.lblLastSeenValue.setText("");
		this.lblSessionDurationValue.setText("");
		this.lblAllActionsValue.setText("");
		this.lblCurrentScreenVal.setText("");
		this.lblEmailValue.setText("");
		this.lblGroupValue.setText("");
		this.lblSecurityValue.setText("");
		this.lblScreenViewsValue.setText("");
		this.lblTotalVisitsValue.setText("");
		this.lblTotalActionsValue.setText("");
		this.lblNameValue.setText("");
		this.lblScreenViewsValue.setText("");
		//this.screenTimelineScroll.setViewportView(null);
	}

	public void userPanelPopulate(){
		Dataset screenViewsDS = null;
		int btn = -1;
		
		btn = table.getSelectedRow();
		
		if(btn != -1)
		{
			IA_UserListPanel testing = userListData.get(btn);
		
			int size = userListData.size();
		
			this.selectedUser = testing.userNameLbl.getText();
			this.profileSelectedUser = testing.lblProfileName.getText();
		
		
			userImageLbl.setVisible(true);
			//check if user from selected profile is online 
			boolean isUserOnline = rpc.checkUserOnlineOrOffline( currentProject, allProjects, this.selectedUser, this.profileSelectedUser);
			if(isUserOnline){
				lblNewLabel.setBackground(new Color(34, 139, 34));
				lblNewLabel.setBorder(new CompoundBorder(new LineBorder(new Color(34, 139, 34), 1), new EmptyBorder(3, 5, 3, 5)));
				lblNewLabel.setText("ONLINE");
			}
			else
			{
				lblNewLabel.setBackground(Color.RED);
				lblNewLabel.setBorder(new CompoundBorder(new LineBorder(Color.RED, 1), new EmptyBorder(3, 5, 3, 5)));
				lblNewLabel.setText("OFFLINE");
			}
			lblNewLabel.setVisible(true);
			List<IA_SessionDetailPanel> sessionData = new ArrayList<IA_SessionDetailPanel>();
			List<ScreenViewPanel> screenViewData = null; 
	
			userNamelbl.setText(selectedUser);
		
			overViewGraphPanel.removeAll();
		
			List<ContactInfo> contact;
			String _contact = "", _contactType;
			int noOfcontacts;
			String[] userRoles;
			int noOfRoles;
			String roles = "";
			
			Iterator<User> retrieveUsers;
			retrieveUsers = this.userProfiles.iterator();
			User currentUser ;
		
			while(retrieveUsers.hasNext())
			{
				currentUser = retrieveUsers.next();
				if((currentUser.get(User.Username).trim().compareToIgnoreCase(this.selectedUser)) == 0 
						&& (currentUser.getProfileName().compareToIgnoreCase(this.profileSelectedUser) == 0))
				{
					contact = currentUser.getContactInfo();
					if(contact != null)
					{
						noOfcontacts = contact.size();
						for(int i=0; i<noOfcontacts; i++)
						{
						
							_contactType = contact.get(i).getContactType() ;
							if(_contactType.compareTo("email") == 0)
							{
								_contact = _contact + "  " + contact.get(i).getValue();
							}
						}
					}
					
					
					
					this.lblNameValue.setText(this.selectedUser);
					this.lblEmailValue.setText(_contact);
					
					//get roles information
					Collection<String> tempRoles = currentUser.getRoles();
					if(tempRoles != null)
					{
						userRoles = tempRoles.toArray(new String[currentUser.getRoles().size()]);
						noOfRoles = userRoles.length;
						for(int i=0;i<noOfRoles; i++)
						{
							if(i == 0)
							{
								roles = roles + userRoles[i] ;
							}
							else
							{
								roles = roles  + "," + userRoles[i] ;
							}
						}
					}
					
					this.lblGroupValue.setText(roles);
					this.lblSecurityValue.setText(roles);
					//retrieve other  infrmation for user
					UsersOverviewInformation _uinfo = rpc.getUserInformation( Constants.LAST_SEVEN_DAYS, this.selectedUser, this.currentProject, this.allProjects, this.profileSelectedUser);
					if(_uinfo != null)
					{
						this.lblFirstSeenValue.setText(_uinfo.getFirstSeen());
						this.lblLastSeenValue.setText(_uinfo.getLastSeen());
						this.lblSessionDurationValue.setText(_uinfo.getTotalSessionsLength() + " hours");
						
						this.lblTotalVisitsValue.setText(_uinfo.getTotalVisitsLast7Days() + " visits");
						
						String locationTest = _uinfo.getLocation();
						String locationArray[] = locationTest.split(",");
						while(locationTest.startsWith(","))
						{
							locationTest = locationTest.substring(1);
						}
						if(locationTest.endsWith(","))
						{
							locationTest.replaceAll(",", " ");
						}
						if(locationTest.contains("Unknown"))
						{
							locationTest = "Unknown";
						}
						this.locationLbl.setText(locationTest);
						String imageName = "";
						if(locationTest.toLowerCase().contains("india")){
							imageName = "Indian Flag (PNG).png";
						}
						else
						if(locationTest.toLowerCase().contains("usa") || locationTest.toLowerCase().contains("united states"))
						{
							imageName = "American Flag (PNG).png";
						}
						else
						if(locationTest.toLowerCase().contains("mexico")){
							imageName = "Mexican Flag (PNG).png";
						}
						else
						if(locationTest.toLowerCase().contains("canada")){
							imageName = "Canadian Flag (PNG).png";
						}
						else
							imageName = "";
						/* By Yogini on 07-Oct-2016
						 * Added a condition to check when image name is blank. Observed the issue on Azure setup
						 */
						if(imageName.compareToIgnoreCase("") != 0)
						{
							ImageIcon flagIcon = new ImageIcon(getClass().getResource(imageName));
							Image flagImg = flagIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
							ImageIcon newflagIcon = new ImageIcon(flagImg);
						
							flagLbl.setIcon(newflagIcon);
						}
						
						//last 7 days
						List<ScreensCount> listScreensLast7 =  _uinfo.getScreensViewedLast7Days();
						int totalScreens7 = 0;
						if(listScreensLast7 != null)
						{
							int k=0;
							int noOfScreens = listScreensLast7.size();
							
							for(k=0;k<noOfScreens; k++)
							{
								totalScreens7 = totalScreens7 + listScreensLast7.get(k).getNoOfViews();
								
							}
								//this.lblScreenViewsValue.setText("" +totalScreens7);
						}
						
						//overall
						List<ScreensCount> listScreens =  _uinfo.getScreensViewed();
						int totalScreens = 0;
						if(listScreens != null)
						{
							int k=0;
							int noOfScreens = listScreens.size();
							
							for(k=0;k<noOfScreens; k++)
							{
								totalScreens = totalScreens + listScreens.get(k).getNoOfViews();
								
							}
								this.lblScreenViewsValue.setText("" +totalScreens);
						}
						
						
						this.lblAllActionsValue.setText("" + (_uinfo.getTotalActions() + totalScreens));
						this.lblTotalActionsValue.setText("" + (_uinfo.getTotalActionsLast7Days() + totalScreens7) + " actions");
						this.lblCurrentScreenVal.setText(_uinfo.getCurrentScreen());
						
							
					}
					
					//int sel = Integer.parseInt(selectedUser);
				//	screenViewsDS = null;
					screenViewsDS = rpc.getScreenViewsPerUserPerVisitNew(Constants.LAST_365_DAYS,currentProject, allProjects,this.selectedUser, this.profileSelectedUser);
					//this.paintPanelGraphlineChart(screenViewsDS);
					int total_height = 0;
					int rows = 0 ;
				
					
					
					int k;
					
					_barChartData = new DefaultCategoryDataset();
					
					if(screenViewsDS != null){
					int ii = 0;
					rows = screenViewsDS.getRowCount();
					while(ii < rows){
						
						k = 0;
						ScreenViewPanel screenViewPanelData;
						IA_SessionDetailPanel sessionValue = new IA_SessionDetailPanel();
						sessionValue.displaySessionTablePanel.setBorder(BorderFactory.createEmptyBorder());
						String sessionStartDate = screenViewsDS.getValueAt(ii, "SessionStart").toString().trim();
						String sessionEndDate = screenViewsDS.getValueAt(ii, "SessionEnd").toString().trim();
						String month = sessionStartDate.substring(4, 7);
						month = getFullMonth(month);
						String fulldate = month + " " + sessionStartDate.substring(8, 10) +" " +sessionStartDate.substring(24, 28);
						sessionValue.sessionDateLbl.setText(fulldate);
						//changed as per QA doc dated 15-Sept -2016
						//sessionValue.sessionLocationLbl.setText(_uinfo.getLocation());
						
						String locString = screenViewsDS.getValueAt(ii, "location").toString().trim();
						while(locString.startsWith(","))
						{
							locString = locString.substring(1);
							locString = locString.trim();
						}
						
						if(locString.endsWith(","))
						{
							locString.replaceAll(",", "");
						}
						
						if(locString.contains("Unknown"))
						{
							locString = "Unknown";
						}
						sessionValue.sessionLocationLbl.setText(locString);
						screenViewData = new ArrayList<ScreenViewPanel>();
	//					String viewdUserName = screenViewsDS.getValueAt(0, "User").toString();
	//					sessionValue.useNameLbl.setText(viewdUserName);
	//					String screenName = screenViewsDS.getValueAt(0, "No. Of Screen Views").toString();
	//					sessionValue.screenNameLbl.setText(screenName);
						int j;
						String viewdUserName = "";
						String screenName = "";
						for (j = ii ; j < rows ; j++){
							String sessionPanelStartDate = screenViewsDS.getValueAt(j, "SessionStart").toString();
							String sessionPanelEndDate = screenViewsDS.getValueAt(j, "SessionEnd").toString();
							//sessionPanelDate = sessionPanelDate.substring(1, 10);
							if (sessionStartDate.equalsIgnoreCase(sessionPanelStartDate) && sessionEndDate.equalsIgnoreCase(sessionPanelEndDate)){
								
								Date todayDate = new Date(); 
								viewdUserName = "";
								screenName = "";
								screenViewPanelData = new ScreenViewPanel();
								if(screenViewsDS.getValueAt(j, "User") != null)
								{
									viewdUserName = screenViewsDS.getValueAt(j, "User").toString();
								}
								else
								{
									viewdUserName = "";
								}
								DateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
								String today = df.format(todayDate);
								screenViewPanelData.userNameLbl.setText(viewdUserName);
								
								if(screenViewsDS.getValueAt(j, "ScreenName") != null)
								{
										screenName = screenViewsDS.getValueAt(j, "ScreenName").toString();
								}
								else
								{
									screenName = "";
								}
								screenViewPanelData.screenNameLbl.setText(screenName);
								
								String loginTime = screenViewsDS.getValueAt(j, "SessionEnd").toString();
								
								
								long diff = 0;
								long diffHours = 0;
								try {
									Date firstDateToday = df.parse(today);
									//Date secondDateLogout = df.parse(logOutTime);
									Date logOutTime = df.parse(loginTime);
									diff = todayDate.getTime() - logOutTime.getTime() ;
									diffHours = diff / (60 * 60 * 1000);
									System.out.println("diffHour Value : " + diffHours);
									
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if(diffHours > 24){
									
									diffHours = (int)diffHours/24;
									screenViewPanelData.lblHoursAgo.setText("" + diffHours + " day(s) ago");
									
								}
								else
								{
									if(diffHours == 0){
										Date logOutTime;
										try {
											logOutTime = df.parse(loginTime);
											diff = todayDate.getTime() - logOutTime.getTime() ;
											diffHours = diff /(60 * 1000);
											System.out.println("diffHour Value : " + diffHours);
											screenViewPanelData.lblHoursAgo.setText("" + diffHours + " minutes ago");
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
									}
									else{
										System.out.println("diffHour Value : " + diffHours);
									screenViewPanelData.lblHoursAgo.setText("" + diffHours + " hours ago");
									}
								}
								screenViewPanelData.actionLbl.setVisible(false);
								screenViewPanelData.valueLbl.setVisible(false);
								screenViewData.add(screenViewPanelData);
							}
							else {
								ii = j;
								
								break;
							}
							
							//paintPanelGraphlineChart(screenViewvedValues);
							sessionValue.screenViewData = screenViewData;
							//sessionValue.paintTableScreenView(screenViewData);
							
							//String ss = "" +screenViewData.size();
							String graphDate = sessionPanelStartDate.substring(1, 10);
							_barChartData.addValue(screenViewData.size(), "Screen", graphDate);
							
							
						}
						JTable panelTab =  new JTable(new ScreenViewTableModel(screenViewData));
						panelTab.setTableHeader(null);
						panelTab.setBorder(BorderFactory.createLineBorder(Color.WHITE));
						panelTab.setDefaultRenderer(ScreenViewPanel.class, new ScreenViewCellRenderer());
						int tab_width = panelTab.getWidth();
						SessionDislayPanel sessionDisplayPanel = new SessionDislayPanel(panelTab);
						sessionDisplayPanel.setBorder(BorderFactory.createEmptyBorder());
						sessionValue.displaySessionTablePanel.setViewportView(panelTab);
						sessionData.add(sessionValue);
					
						//total_height = ((sessionValue.getHeight()) - tab_height + (sessionValue.panel.getHeight())) +10 ;
						total_height = sessionValue.getHeight() + 40;
						if ( j == rows ){
							break;
						}
						
					}
					//ii++;
				}
					sessionTable.setModel(new SeesionTableModel(sessionData));
					
					//set the variable row height
					int noOfsessions = sessionData.size();
					int minRowHeight = 35;
					for(int x=0; x<noOfsessions; x++)
					{
						if(sessionData.get(x).screenViewData != null )
						{
							
							sessionTable.setRowHeight(x, (minRowHeight + (20 * (sessionData.get(x).screenViewData.size()))));
						}
						else
						{
							sessionTable.setRowHeight(x,minRowHeight);
						}
					}
					
					
					
					// Code for bar chart Omkar
					
					JFreeChart chart = ChartFactory.createBarChart(
			               null,         // chart title
			                "",               // domain axis label
			                "",                  // range axis label
			                _barChartData,                  // data
			                PlotOrientation.VERTICAL, // orientation
			                false,                     // include legend
			                true,                     // tooltips?
			                false                     // URLs?
			            );
			        chart.setBackgroundPaint(Color.white);
			        
			        // get a reference to the plot for further customisation...
			        CategoryPlot plot = chart.getCategoryPlot();
			        plot.setBackgroundPaint(Color.white);
			        plot.setDomainGridlinePaint(Color.white);
			        plot.setRangeGridlinePaint(Color.white);
			      
			        // set the range axis to display integers only...
			        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			        rangeAxis.setAxisLineVisible(false);
			        // disable bar outlines...
			         BarRenderer renderer = (BarRenderer) plot.getRenderer();
			        
			         renderer.setMaximumBarWidth(.025);
			        renderer.setDrawBarOutline(false);
			        renderer.setShadowVisible(false);
			        
			        renderer.setSeriesPaint(0, new Color(80,145,184));
			        renderer.setSeriesPaint(1, new Color(253,184,40));
			        renderer.setSeriesPaint(2,  new Color(0,255,0));
			     
	
			        CategoryAxis domainAxis = plot.getDomainAxis();
			        domainAxis.setCategoryLabelPositions(
			        		CategoryLabelPositions.STANDARD);
			        domainAxis.setAxisLineVisible(false);
			        ChartPanel chartPanel = new ChartPanel(chart);
			        chartPanel.setBorder(null);
			        chartPanel.setPreferredSize(new Dimension(overViewGraphPanel.getWidth(), 80));
					overViewGraphPanel.add(chartPanel,BorderLayout.CENTER);
					
					//set the screens viewed information
					
				}
			}
			if(this.profileInfoPanel.isVisible() == false )
			{
				this.profileInfoPanel.setVisible(true);
			}
			
			if(this.ScreenViewsTimelinePanel.isVisible() == false)
			{
				this.ScreenViewsTimelinePanel.setVisible(true);
			}
		}
		
	}

	private Object getValueAt(int ii, String string) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// By omkar for graph in panel 
	private void paintPanelGraphlineChart(int noOfScreen[]){
		int totalScreensinSessions[] = noOfScreen;
		
		
		DefaultCategoryDataset _lineData = new DefaultCategoryDataset();
		//JFreeChart _lineChart;
		
		//create a line chart to show total actions and total sessions for various durations
		//retrieve the series data from gateway using RPC
		int noOfRows;
//		int todayRow = dataToday.getRowCount();
//		int yesterdayRow = dataYesterDay.getRowCount();
		
		int length = totalScreensinSessions.length;
		
		
		int i = 0;
		
		for(i=0; i<length; i++)
		{

			//_lineData.addValue(Integer.parseInt(dataToday.getValueAt(i, 1).toString()), "Today",dataToday.getValueAt(i, 0).toString());
				_lineData.addValue(totalScreensinSessions[i], "Screen", "number");
		
			
		}
		JFreeChart chart = ChartFactory.createBarChart(
                "Top Screens",         // chart title
                "Screen Name",               // domain axis label
                "Top 10",                  // range axis label
                _lineData,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
            );
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customisation...
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        // set the range axis to display integers only...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
         BarRenderer renderer = (BarRenderer) plot.getRenderer();
         renderer.setMaximumBarWidth(.25);
        renderer.setDrawBarOutline(false);
        
        
        renderer.setSeriesPaint(0, new Color(80,145,184));
        renderer.setSeriesPaint(1, new Color(253,184,40));
        renderer.setSeriesPaint(2,  new Color(0,255,0));
     

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
        		CategoryLabelPositions.STANDARD);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setSize(this.overViewGraphPanel.getWidth(), this.overViewGraphPanel.getHeight());
		overViewGraphPanel.add(chartPanel);
	}
	
	public List<IA_UserListPanel> populateUserList(String selectedItem){
		CurrentOverview onlineInfo = rpc.getCurrentOverview(currentProject, allProjects);
		Collection<User> users =  rpc.getUserProfiles();
		Iterator<User> retrieveUsers;
		String selected = selectedItem;
		
		ImageIcon usrPnlImgIcon = new ImageIcon(getClass().getResource("userPanelImg.png"));
		Image usrPnlImg = usrPnlImgIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		usrPnlImgIcon = new ImageIcon(usrPnlImg);
		
		User currentUser ;
		long diff = 0;
		long diffHours = 0;
		List<IA_UserListPanel>userListData1 = new ArrayList<IA_UserListPanel>();
		
		if(selected.compareToIgnoreCase("All Users") == 0){
			
			this.comboSelction = 0;
			totalUsersCountLbl.setText("" + alluserListData.size() + " " + "people");
			return alluserListData;
		}
		else if (selected.compareToIgnoreCase("Online Users") == 0)
		{
			
			this.comboSelction = 1;
			this.onlineuserListData = new ArrayList<IA_UserListPanel>();
			HashMap<String, String> onlineUsers = onlineInfo.getDistinctUsers();
			Set mapSet = (Set) onlineUsers.entrySet();
		    
		    int noOfAllUsers = this.alluserListData.size();
		    for(int i=0; i<noOfAllUsers; i++)
			{	
		    	Iterator mapIterator = mapSet.iterator();
				
				String name = alluserListData.get(i).userNameLbl.getText();
				String profileName = alluserListData.get(i).lblProfileName.getText();
				String currentName = name + ":" + profileName;
				
				
				//compare with allUserListData
				
					while (mapIterator.hasNext()) 
					{
						Map.Entry mapEntry = (Map.Entry) mapIterator.next();
						String keyValue = (String) mapEntry.getKey();
						String value = (String) mapEntry.getValue();
							
				
						if(keyValue.compareToIgnoreCase(currentName) == 0)
						{
						
							userListData1.add(alluserListData.get(i));
							onlineuserListData.add(alluserListData.get(i));
							break;
						}
					}
			}
			totalUsersCountLbl.setText("" + userListData1.size() + " " + "people");
			return userListData1;
		}
		else if (selected.compareToIgnoreCase("User Search") == 0)
		{
			
			String searchName = searchTextField.getText().trim().toLowerCase();
			//to do 
			//search for the user name in allUsers
			this.searchString =  searchTextField.getText().trim().toLowerCase();
			if(searchName.length() == 0)
			{
				
				if(this.comboSelction == 1)
				{
					totalUsersCountLbl.setText("" + onlineuserListData.size() + " " + "people");
					return this.onlineuserListData;
				}
				else 
				{
					totalUsersCountLbl.setText("" + this.alluserListData.size() + " " + "people");
					return this.alluserListData;
				}
			}
			else
			{
				
//				int noOfAllUsers = this.userListData.size();
//				for(int i=0; i<noOfAllUsers; i++)
//				{
//			
//				
//					String name = this.userListData.get(i).userNameLbl.getText().toLowerCase();
//					if(name.compareToIgnoreCase(searchName) == 0 || name.contains(searchName) )
//					{
//						userListData1.add(this.userListData.get(i));
//						
//					}
//				}
//				totalUsersCountLbl.setText("" + userListData1.size() + " " + "people");
				
				int noOfAllUsers = 0;
				if(this.comboSelction == 0)
				{
					noOfAllUsers = this.alluserListData.size();
					for(int i=0; i<noOfAllUsers; i++)
					{
				
					
						String name = this.alluserListData.get(i).userNameLbl.getText().toLowerCase();
						if(name.compareToIgnoreCase(searchName) == 0 || name.contains(searchName) )
						{
							userListData1.add(this.alluserListData.get(i));
							
						}
					}
					totalUsersCountLbl.setText("" + userListData1.size() + " " + "people");
				}
				else
				{
					noOfAllUsers = this.onlineuserListData.size();
					for(int i=0; i<noOfAllUsers; i++)
					{
				
						String name = this.onlineuserListData.get(i).userNameLbl.getText().toLowerCase();
						if(name.compareToIgnoreCase(searchName) == 0 || name.contains(searchName) )
						{
							userListData1.add(this.onlineuserListData.get(i));
							
						}
					}
					totalUsersCountLbl.setText("" + userListData1.size() + " " + "people");
				}
				return userListData1;
				
			}
		
		}
		else
		{
			return null;
		}
		
		
	}
	
	String getFullMonth(String month){
		String monthName = "";
		if(month.compareToIgnoreCase("Jan") == 0){
		monthName = "January";
		}else
		if(month.compareToIgnoreCase("Feb") == 0){
			monthName = "February";
		}else
		if(month.compareToIgnoreCase("Mar") == 0){
			monthName = "March";
		}else
		if(month.compareToIgnoreCase("Apr") == 0){	
			monthName = "April";
		}else
		if(month.compareToIgnoreCase("May") == 0){
			monthName = "May";
		}else
		if(month.compareToIgnoreCase("Jun") == 0){
			monthName = "June";
		}else
		if(month.compareToIgnoreCase("Jul") == 0){
			monthName = "July";
		}else
		if(month.compareToIgnoreCase("Aug") == 0){
			monthName = "August";
		}else
		if(month.compareToIgnoreCase("Sep") == 0){
			monthName = "September";
		}else
		if(month.compareToIgnoreCase("Oct") == 0){
			monthName = "October";
		}else
		if(month.compareToIgnoreCase("Nov") == 0){
		monthName = "November";
		}else
		if(month.compareToIgnoreCase("Dec") == 0){
			monthName = "December";
		}
		
		return monthName;
	}

}
