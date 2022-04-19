package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import javafx.scene.layout.Border;



import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;







import com.google.protobuf.MapEntry;
//import com.google.zxing.common.Collections;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.gateway.gan.GatewayNetworkManager;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.api.ServiceManager;
import com.inductiveautomation.metro.api.services.ServiceState;
import com.vaspsolutions.analytics.common.ActiveUsersInfo;
import com.vaspsolutions.analytics.common.AlarmsInformation;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ContentsData;
import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.DevicesInformation;
import com.vaspsolutions.analytics.common.MODIAServiceUnavailableException;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.ScreensCount;
import com.vaspsolutions.analytics.common.UserLocations;
import com.vaspsolutions.analytics.common.UserLocationsPerGateway;
import com.vaspsolutions.analytics.common.UsersOverviewInformation;










//import com.vaspsolutions.analytics.gateway.services.AgentService;



import java.awt.BorderLayout;

import javax.swing.JComboBox;


/** 
 * THis is the Class that extends from JPanel to show real time information on teh screen
 * The class has : 
 * num_ , time fields that store the values of parameters we retrieved using RPC Call to gateway
 * lbl_ fields of Type MyLabel that display label for each parameter
 * val_ fields of Type OrangeText that display the value of parameter on the UI
 *  
 *  @author YM 
 *  @since 04/13/2015
 *  */

public class RealTimePanelController extends JPanel implements MouseListener, ActionListener, MouseWheelListener {

	//variables to maintain state on UI refresh
	private boolean alrmsActive = true;
	private boolean alrmsAck = false;
	
	private boolean avgScreenDepthStatus =  true;
	private boolean activeUsersStatus =  false;
	
	private boolean userOnlineStatus = true;
	private boolean userReturningStatus = false;
	
	private static final long serialVersionUID = 1L;
	public IA_TrafficPanel trafficPanel;
	public JPanel timeLine ;
	public RoundedPanel alarms;
	public JScrollPane content;
	public JPanel mapLocation ;
	public JPanel devices;
	public JPanel users;
	public JPanel engagement;
	public JPanel currentOverview;
	private JPanel duration;
	private ModuleRPC rpc;
	private String currentProject;
	private String currentGateway;
	private int currentDurationA = Constants.TODAY;
	private int currentDurationB = Constants.YESTERDAY;
	private String dataSource; 
	private JPanel graph;
	private JTable heading;
	private JScrollPane usersList;
	private JPanel header;
	private IA_Segmented_Panel value;
	private int noOfUsers;
	private JXMapViewer mapViewer;
	private JXMapViewer mapViewerInPopup;
	private RoundedPanel panel;
	private RoundedButton btnAck;
	private RoundedButton btnActive;
	private RoundedButton btnTimeToAck;
	private RoundedButton btnTimetoClear;
	private JLabel lblSessions;
	private JLabel txtSessions;
	private JLabel lblScreenViews;
	private JLabel txtScreenViews;
	private JLabel lblActions;
	private JLabel txtActions;
	
	private boolean allProjects;
	private boolean allGateways;
	private JPanel top_left;
	private JLabel lblUsersOnline;
	private JPanel valuePanel;
	//private JLabel lblSelected;
	
	
	AlarmsInformation alarmInfo;
	
	//new added by Omkar
		private JLabel lblNewLabel;
		private JLabel lblNewLabel_1;
		private JLabel lblNewLabel_2;
		private JLabel txtAvgSession;
		private JLabel txtAvgTimeScreen;
		private JLabel txtActionSession;
		private JLabel usrIconLabel;
		private JLabel alarmIconLabel;
		private JLabel actionsIconLabel;
		private JLabel lblHedingCurrentOverView;
		private JScrollPane engagementInnerPannel;
		//private RoundedButton avgSessionTimeButton;
		private RoundedButton activeUsersBtn;
		private RoundedButton avgDepthOfScreen;
		private JPanel headerPanel;
		private JComboBox comboBoxTimeChart;
		//private JPanel timeLineGraph;
		
		private String rightComboSelected;
		private JLabel topLeftAlarmBell;
		private JLabel highPriorityLbl;
		private JLabel txtHighPriority;
		private JLabel topRightAlarmBell;
		private JLabel criticalPrioritylbl;
		private JLabel txtCriticalPriority;
		private JPanel panel_1;
		private JPanel panel_2;
		private JPanel panel_3;
		private JLabel bottomLeftBell;
		private JLabel lowPrioritylbl;
		private JLabel txtLowPriority;
		private JLabel txtMediumPriority;
		private JLabel bottomRightBell;
		private JLabel mediumPrioritylbl;
		private JLabel label;
		private JLabel lblLocations;
		private JPanel locationHeaderPanel;
		private JPanel panel_6;
		private JLabel lblNewLabel_3;
		private JPanel panelForMap;
		private JPanel deviceHeaderPanel;
		private JPanel usersHeaderPanel;
		private JLabel lblUsers;
		private JPanel usrsBtnsPanel;
		private RoundedButton usersOnlineBtn;
		private RoundedButton returningBtn;
		private GridBagConstraints gbc_locationHeaderPanel;
		private JLabel lblDayMin;
		private JLabel lblDayMax;
		private JLabel dayMinValLbl;
		private JLabel dayMaxLbl;
		private JPanel entireHeadingPanel;
		private JPanel contentLblPanel;
		private JLabel lblNewLabel_4;
		private JScrollPane scrollPane;
		private JPanel panel_4;
		private RealTimeDeviceButton button;
		private RealTimeDeviceButton button_1;
		private JPanel alarmTitle;
		private JLabel lblAlarmTitle;
		private JPanel contentHeaderLblPanel;
		private JPanel alarmsHeaderPanel;
		private JPanel panel_5;
		private JPanel panel_7;
		private JPanel panel_8;
		private JPanel panel_9;
		private JPanel panel_10;
		private JLabel dividingLineLabel1;
		private JLabel devidingLine2;
		private ChartPanel browserChartPanel;
		private JLabel topLeftarcLbl;
		private JLabel topRightArcLbl;
		private JLabel bottomLeftArcLbl;
		private JLabel bottomRightArcLbl;private JPanel symbolPanel;
		
		private JLabel device1IconLbl;
		private JLabel device2IconLbl;
		private JLabel device3IconLbl;
		private JLabel device4IconLbl;
		
		
		private JLabel browser1IconLbl;
		private JLabel browser2IconLbl;
		private JLabel browser3IconLbl;
		private JLabel browser4IconLbl;
		
		private JLabel darkBlueLbl;
		private JLabel lightBlueLbl;
		private JLabel darkerBlueLbl;
		private JLabel lighterBlueLbl;
		
		private JLabel darkOrangeLbl;
		private JLabel lightOrangeLbl;
		private JLabel darkerOrnageLbl;
		private JLabel lighterOrangeLbl;
		
		//icons 
		private ImageIcon newDarkBlue;
		private ImageIcon newlightBlue;
		private ImageIcon newDarkerBlue;
		private ImageIcon newlighterBlue;
		private ImageIcon newDarkOrange;
		private ImageIcon newlightOrange;
		private ImageIcon newDarkerOrange;
		private ImageIcon newlighterOrange;
		
        ImageIcon symbolForMobile;
        ImageIcon symbolForPC;
        ImageIcon symbolForMac;
        ImageIcon symbolForLinux;
        
        ImageIcon newBrowser;
	    ImageIcon newBrowser2;
	    ImageIcon newOpera;
	    ImageIcon newSafari;
	    ImageIcon newMozilla;
	    
	    List<CurrentOverview> overViewList;
	//Default constructor to initialize all the members.
		LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
		public int rightComboSelectedIndex = 0;
		
		 ChartPanel _lineChartPanel;
		 ChartPanel pieChartPanel;
		 
		 //making it global so that it can be accessed in functions.
		 List<ScreensCount> screensPerUser = new ArrayList<ScreensCount>();
	@SuppressWarnings("serial")
	public <Graphics> RealTimePanelController(ModuleRPC _rpc, String projectName, String dsName, int rightComboIndex,String gateWayName) {
		super();
		
		
		leftRenderer.paddingSize = 10;
		setBorder(null);
		this.setOpaque(false);
		this.setBackground(Constants.COLOR_MAIN_BACKGROUND);
		this.setPreferredSize(new Dimension(1720, 1080));
//		Container parent = this.getParent();
//		parent.
//		Dimension parentDimention = parent.getSize();
//		Double widthDouble = parentDimention.getWidth();
//		Double heightDouble = parentDimention.getHeight();
//		 int height = heightDouble.intValue();
//		 int width = widthDouble.intValue();
//		 
//		 
//		 this.setSize(width, height);
		
		
		javax.swing.border.Border emtyBordet = BorderFactory.createEmptyBorder();
		
		//combox box entries as html strings
		String comboItem1 = "<html>"
				+ "<body> <font color=\"white\">Today vs </font><font color=\"rgb(82,139,204)\">Yesterday</font>" 
				+ "</body></html>";
		String comboItem2 = "<html>"
				+ "<body> <font color=\"white\">This Week vs </font><font color=\"rgb(82,139,204)\">Last Week</font>" 
				+ "</body></html>";
		String comboItem3 = "<html>"
				+ "<body> <font color=\"white\">This Month vs </font><font color=\"rgb(82,139,204)\">Last Month</font>" 
				+ "</body></html>";
		//String rightComboBoxEntries[] = {"Today vs Yesterday","This Week vs Last Week","This Month vs Last Month"};
		String rightComboBoxEntries[] = {comboItem1,comboItem2,comboItem3};
		ImageIcon alarmsIcon = new ImageIcon(getClass().getResource("Alarm.png"));
		Image newAlarmImg = alarmsIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		ImageIcon newImgIcon = new ImageIcon(newAlarmImg);	
		this.rpc = _rpc;
		this.currentProject = projectName;
		this.currentGateway = gateWayName;
		
		if(projectName.compareToIgnoreCase("All Projects") == 0)
		{
			this.allProjects = true;
		}
		else
		{
			this.allProjects = false;
		}
		// Checking for gateways. If all then activating the allGateway flag otherwise setting to false
		
		if(gateWayName.compareToIgnoreCase("All Gateways") == 0){
			this.allGateways = true;
		}
		else
			this.allGateways = false;
		
		
		this.dataSource = dsName;
		this.rightComboSelectedIndex = rightComboIndex;
		Font lblFont =  new Font(Font.SANS_SERIF, Font.BOLD, 11);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{430,430,430,430};
		gridBagLayout.rowHeights = new int[]{210,280 , 280, 280,30};
		gridBagLayout.columnWeights = new double[]{0.25, 0.25, 0.25, 0.25};
		gridBagLayout.rowWeights = new double[]{0.0, 0.5, 0.5, 0.0, 0.0};
		setLayout(gridBagLayout);
		this.setBackground(new Color(228 ,238 ,245));
		
		entireHeadingPanel = new JPanel();
		entireHeadingPanel.setBorder(BorderFactory.createEmptyBorder());
		entireHeadingPanel.setPreferredSize(new Dimension(1720,210));
		entireHeadingPanel.setOpaque(true);
		GridBagConstraints gbc_entireHeadingPanel = new GridBagConstraints();
		entireHeadingPanel.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		gbc_entireHeadingPanel.gridwidth = 4;
		gbc_entireHeadingPanel.insets = new Insets(0, 0, 0, 0);
		gbc_entireHeadingPanel.fill = GridBagConstraints.BOTH;
		gbc_entireHeadingPanel.gridx = 0;
		gbc_entireHeadingPanel.gridy = 0;
		add(entireHeadingPanel, gbc_entireHeadingPanel);
		GridBagLayout gbl_entireHeadingPanel = new GridBagLayout();
		gbl_entireHeadingPanel.columnWidths = new int[]{329, 693, 320, 0};
		gbl_entireHeadingPanel.rowHeights = new int[]{200};
		gbl_entireHeadingPanel.columnWeights = new double[]{0.20, 0.60, 0.20, Double.MIN_VALUE};
		gbl_entireHeadingPanel.rowWeights = new double[]{1.0};
		entireHeadingPanel.setLayout(gbl_entireHeadingPanel);
		
		top_left = new JPanel();
		top_left.setPreferredSize(new Dimension(329,210));
		GridBagConstraints gbc_top_left = new GridBagConstraints();
		gbc_top_left.fill = GridBagConstraints.BOTH;
		gbc_top_left.insets = new Insets(0, 0, 0, 0);
		gbc_top_left.gridx = 0;
		gbc_top_left.gridy = 0;
		entireHeadingPanel.add(top_left, gbc_top_left);
		//top_left.setBorder(new TitledBorder(null, "Traffic", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		top_left.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagLayout gbl_top_left = new GridBagLayout();
		gbl_top_left.columnWidths = new int[]{100, 200, 100};
		gbl_top_left.rowHeights = new int[]{50,50,50,50};
		gbl_top_left.columnWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
		gbl_top_left.rowWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
		top_left.setLayout(gbl_top_left);
		
		lblUsersOnline = new JLabel("  Traffic ");
		lblUsersOnline.setForeground(Constants.COLOR_GREY_LABEL);
		lblUsersOnline.setFont(new Font("SansSerif", Font.BOLD, 12));
		GridBagConstraints gbc_lblUsersOnline = new GridBagConstraints();
		gbc_lblUsersOnline.fill = GridBagConstraints.BOTH;
		gbc_lblUsersOnline.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsersOnline.gridx = 0;
		gbc_lblUsersOnline.gridy = 0;
		top_left.add(lblUsersOnline, gbc_lblUsersOnline);
		
		trafficPanel = new IA_TrafficPanel();
		trafficPanel.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagConstraints gbc_trafficPanel = new GridBagConstraints();
		gbc_trafficPanel.gridheight = 4;
		gbc_trafficPanel.insets = new Insets(0, 0, 0, 0);
		gbc_trafficPanel.fill = GridBagConstraints.BOTH;
		gbc_trafficPanel.gridx = 1;
		gbc_trafficPanel.gridy = 0;
		top_left.add(trafficPanel, gbc_trafficPanel);
		
		dayMinValLbl = new JLabel("");
		dayMinValLbl.setFont(new Font("Tahoma", Font.BOLD, 14));
		dayMinValLbl.setForeground(Color.WHITE);
		GridBagConstraints gbc_dayMinValLbl = new GridBagConstraints();
		gbc_dayMinValLbl.anchor = GridBagConstraints.SOUTH;
		gbc_dayMinValLbl.insets = new Insets(0, 0, 5, 5);
		gbc_dayMinValLbl.gridx = 0;
		gbc_dayMinValLbl.gridy = 2;
		top_left.add(dayMinValLbl, gbc_dayMinValLbl);
		
		dayMaxLbl = new JLabel("");
		dayMaxLbl.setFont(new Font("Tahoma", Font.BOLD, 14));
		dayMaxLbl.setForeground(Color.WHITE);
		GridBagConstraints gbc_dayMaxLbl = new GridBagConstraints();
		gbc_dayMaxLbl.anchor = GridBagConstraints.SOUTH;
		gbc_dayMaxLbl.insets = new Insets(0, 0, 5, 0);
		gbc_dayMaxLbl.gridx = 2;
		gbc_dayMaxLbl.gridy = 2;
		top_left.add(dayMaxLbl, gbc_dayMaxLbl);
		
		lblDayMin = new JLabel("7 Day Min");
		lblDayMin.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblDayMin = new GridBagConstraints();
		gbc_lblDayMin.anchor = GridBagConstraints.NORTH;
		gbc_lblDayMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblDayMin.gridx = 0;
		gbc_lblDayMin.gridy = 3;
		top_left.add(lblDayMin, gbc_lblDayMin);
		
		lblDayMax = new JLabel("7 Day Max");
		lblDayMax.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblDayMax = new GridBagConstraints();
		gbc_lblDayMax.anchor = GridBagConstraints.NORTH;
		gbc_lblDayMax.gridx = 2;
		gbc_lblDayMax.gridy = 3;
		top_left.add(lblDayMax, gbc_lblDayMax);
		//trafficPanel.setSize(100,100);
		
		timeLine =  new JPanel();
		timeLine.setBorder(null);
		timeLine.setPreferredSize(new Dimension(693, 210));
		GridBagConstraints gbc_timeLine = new GridBagConstraints();
		gbc_timeLine.fill = GridBagConstraints.BOTH;
		gbc_timeLine.gridwidth = 1;
		gbc_timeLine.insets = new Insets(0, 0, 0, 0);
		gbc_timeLine.gridx = 1;
		gbc_timeLine.gridy = 0;
		entireHeadingPanel.add(timeLine, gbc_timeLine);
		
		
		timeLine.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		timeLine.setLayout(new BorderLayout(0, 0));
		
		_lineChartPanel = new ChartPanel(null);
		_lineChartPanel.setBackground(Color.DARK_GRAY);
		_lineChartPanel.setOpaque(false);
		_lineChartPanel.setPreferredSize(new Dimension(700,150));
		 timeLine.add(_lineChartPanel, BorderLayout.CENTER);
//		timeLineGraph = new JPanel();
//		
//		timeLineGraph.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
//		timeLine.add(timeLineGraph);
//		timeLineGraph.setLayout(new BorderLayout(0, 0));
		
		duration = new JPanel();
		GridBagConstraints gbc_duration = new GridBagConstraints();
		gbc_duration.fill = GridBagConstraints.HORIZONTAL;
		gbc_duration.gridx = 2;
		gbc_duration.gridy = 0;
		entireHeadingPanel.add(duration, gbc_duration);
		
		duration.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagLayout gbl_duration = new GridBagLayout();
		gbl_duration.columnWidths = new int[]{320};
		gbl_duration.rowHeights = new int[]{0,26,0};
		gbl_duration.columnWeights = new double[]{0.0};
		gbl_duration.rowWeights = new double[]{0.5,0.0,0.5};
		duration.setLayout(gbl_duration);
		
		comboBoxTimeChart = new JComboBox();
		comboBoxTimeChart.setOpaque(true);
		comboBoxTimeChart.setForeground(Color.WHITE);
		comboBoxTimeChart.setBackground(new Color(71,84,94));
		comboBoxTimeChart.setUI(new ComboArrowUI());
		
	//	comboBoxTimeChart.setPreferredSize(new Dimension(240,26));
		GridBagConstraints gbc_comboBoxTimeChart = new GridBagConstraints();
		gbc_comboBoxTimeChart.fill = GridBagConstraints.BOTH;
		gbc_comboBoxTimeChart.insets = new Insets(0,20,0,65);
		gbc_comboBoxTimeChart.gridx = 0;
		gbc_comboBoxTimeChart.gridy = 1;
		
		for (int i = 0 ; i < rightComboBoxEntries.length ; i++)
		{
			comboBoxTimeChart.addItem(rightComboBoxEntries[i]);
		}
		
		duration.add(comboBoxTimeChart, gbc_comboBoxTimeChart);
		
		comboBoxTimeChart.setActionCommand(Constants.CMD_DURATION_SELECT);
		comboBoxTimeChart.addActionListener(this);
		//Overview panel and its contents
		alarms = new RoundedPanel();
		alarms.setPreferredSize(new Dimension(420,560));
		alarms.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		alarms.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		
		alarms.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_alarms = new GridBagConstraints();
		gbc_alarms.gridheight = 2;
		gbc_alarms.fill = GridBagConstraints.BOTH;
		gbc_alarms.insets = new Insets(5, 5, 5, 5);
		gbc_alarms.gridx = 0;
		gbc_alarms.gridy = 1;
		add(alarms, gbc_alarms);
		//alarms.setBorder(new TitledBorder(null, "Alarms", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		/*alarms.setBorder(new AbstractBorder() {
			@SuppressWarnings("unused")
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.blue);
				int arc = 10;
				g2.drawRoundRect(x, y, width -1, height-1, arc, arc);
				}
		}); */
		GridBagLayout gbl_alarms = new GridBagLayout();
		gbl_alarms.columnWidths = new int[]{419};
		gbl_alarms.rowHeights = new int[] {100, 460};
		gbl_alarms.columnWeights = new double[]{1.0};
		gbl_alarms.rowWeights = new double[]{0.5, 0.5};
		alarms.setLayout(gbl_alarms);
		
		alarmsHeaderPanel = new RoundedBlackPanel();
		alarmsHeaderPanel.setPreferredSize(new Dimension(419,100));
		alarmsHeaderPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	//	alarmsHeaderPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		GridBagConstraints gbc_alarmsHeaderPanel = new GridBagConstraints();
		gbc_alarmsHeaderPanel.insets = new Insets(0, 0, 5, 0);
		gbc_alarmsHeaderPanel.fill = GridBagConstraints.BOTH;
		gbc_alarmsHeaderPanel.gridx = 0;
		gbc_alarmsHeaderPanel.gridy = 0;
		alarms.add(alarmsHeaderPanel, gbc_alarmsHeaderPanel);
		
		
		GridBagLayout gbl_alarmsHeaderPanel = new GridBagLayout();
		gbl_alarmsHeaderPanel.columnWidths = new int[]{0};
		gbl_alarmsHeaderPanel.rowHeights = new int[] {30, 70};
		gbl_alarmsHeaderPanel.columnWeights = new double[]{0.5};
		gbl_alarmsHeaderPanel.rowWeights = new double[]{0.0, 1.0};
		alarmsHeaderPanel.setLayout(gbl_alarmsHeaderPanel);
		
		alarmTitle = new RoundedBlackPanel();
		GridBagConstraints gbc_alarmTitle = new GridBagConstraints();
		gbc_alarmTitle.fill = GridBagConstraints.BOTH;
		gbc_alarmTitle.insets = new Insets(0, 0, 5, 0);
		gbc_alarmTitle.gridx = 0;
		gbc_alarmTitle.gridy = 0;
		alarmsHeaderPanel.add(alarmTitle, gbc_alarmTitle);
		alarmTitle.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		alarmTitle.setLayout(new BorderLayout(0, 0));
		
		lblAlarmTitle = new JLabel("   Alarms");
		lblAlarmTitle.setVerticalAlignment(SwingConstants.TOP);
		lblAlarmTitle.setHorizontalAlignment(SwingConstants.LEFT);
		lblAlarmTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblAlarmTitle.setForeground(Constants.COLOR_GREY_LABEL);
		alarmTitle.add(lblAlarmTitle, BorderLayout.CENTER);
		
		header = new JPanel();
		header.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		GridBagConstraints gbc_header = new GridBagConstraints();
		gbc_header.fill = GridBagConstraints.BOTH;
		gbc_header.gridx = 0;
		gbc_header.gridy = 1;
		gbc_header.insets = new Insets(0,0,0,0);
		alarmsHeaderPanel.add(header, gbc_header);
				
		GridBagLayout gbl_header = new GridBagLayout();
				gbl_header.columnWidths = new int[]{100, 100,100,100};
				gbl_header.rowHeights = new int[]{70};
				gbl_header.columnWeights = new double[]{0.33,0.33,0.33,0.33};
				gbl_header.rowWeights = new double[]{1.0};
				header.setLayout(gbl_header);
						
						btnActive = new RoundedButton();
						btnActive.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
						btnActive.lblboldtext.setForeground(Color.WHITE);
						btnActive.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
						btnActive.setForeground(Color.WHITE);
						btnActive.setName("ACTIVE_CLICK");
						btnActive.addMouseListener(this);
								
								btnAck = new RoundedButton();
								btnAck.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
								btnAck.lblboldtext.setForeground(Color.WHITE);
								btnAck.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
								btnAck.setForeground(Color.WHITE);
								btnAck.setName("ACK_CLICK");
								btnAck.addMouseListener(this);
										
										btnTimetoClear = new RoundedButton();
										btnTimetoClear.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
										btnTimetoClear.lblboldtext.setForeground(Color.WHITE);
										btnTimetoClear.setForeground(Color.WHITE);
										btnTimetoClear.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
										btnTimetoClear.setName("TIME_TO_CLEAR");
										//btnTimetoClear.setOpaque(true);
										GridBagConstraints gbc_btnTimetoClear = new GridBagConstraints();
										gbc_btnTimetoClear.fill = GridBagConstraints.BOTH;
										gbc_btnTimetoClear.insets = new Insets(0,0,5,5);
										gbc_btnTimetoClear.gridx = 3;
										gbc_btnTimetoClear.gridy = 0;
										btnTimetoClear.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
										header.add(btnTimetoClear, gbc_btnTimetoClear);
								//		btnActive.setActionCommand("ACTIVE_CLICK");
								//		btnActive.addActionListener(this);
										GridBagConstraints gbc_btnAck = new GridBagConstraints();
										gbc_btnAck.fill = GridBagConstraints.BOTH;
										gbc_btnAck.gridx = 1;
										gbc_btnAck.gridy = 0;
										gbc_btnAck.insets = new Insets(0,0,5,0);
										header.add(btnAck, gbc_btnAck);
								
								btnTimeToAck = new RoundedButton();
								btnTimeToAck.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
								btnTimeToAck.lblboldtext.setForeground(Color.WHITE);
								btnTimeToAck.setForeground(Color.WHITE);
								btnTimeToAck.setName("TIME_TO_ACK");
								//btnTimeToAck.setOpaque(true);
								btnTimeToAck.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
								GridBagConstraints gbc_btnTimeToAck = new GridBagConstraints();
								gbc_btnTimeToAck.fill = GridBagConstraints.BOTH;
								gbc_btnTimeToAck.gridx = 2;
								gbc_btnTimeToAck.gridy = 0;
								gbc_btnTimeToAck.insets = new Insets(0,0,5,0);
								btnTimeToAck.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								header.add(btnTimeToAck, gbc_btnTimeToAck);
						//		btnAck.setActionCommand("ACK_CLICK");
						//		btnAck.addActionListener(this);
								GridBagConstraints gbc_btnActive = new GridBagConstraints();
								gbc_btnActive.fill = GridBagConstraints.BOTH;
								gbc_btnActive.insets = new Insets(0,5,5,0);
								gbc_btnActive.gridx = 0;
								gbc_btnActive.gridy = 0;
								header.add(btnActive, gbc_btnActive);
		
		valuePanel = new RoundedPanel();
		valuePanel.setForeground(Color.WHITE);
		valuePanel.setPreferredSize(new Dimension(419,460));
		valuePanel.setBorder(BorderFactory.createEmptyBorder());
		//valuePanel.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_valuePanel = new GridBagConstraints();
		gbc_valuePanel.fill = GridBagConstraints.BOTH;
		gbc_valuePanel.gridx = 0;
		gbc_valuePanel.gridy = 1;
		alarms.add(valuePanel, gbc_valuePanel);
		
		GridBagLayout gbl_valuePanel = new GridBagLayout();
		gbl_valuePanel.columnWidths = new int[]{383};
		gbl_valuePanel.rowHeights = new int[]{65, 330, 65};
		gbl_valuePanel.columnWeights = new double[]{1.0};
		gbl_valuePanel.rowWeights = new double[]{0.0, 1.0, 0.0};
		valuePanel.setLayout(gbl_valuePanel);
		
		panel_1 = new JPanel();
		panel_1.setForeground(Color.WHITE);
	
		panel_1.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		panel_1.setSize(419, 50);
		panel_1.setBorder(BorderFactory.createEmptyBorder());
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		valuePanel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{10, 42, 42, 95, 95, 42, 42, 10};
		gbl_panel_1.rowHeights = new int[]{25,25};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.1,0.2, 0.2, 0.2, 0.2,0.1,0.0};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0};
		panel_1.setLayout(gbl_panel_1);
		
		
		ImageIcon topLeftArc = new ImageIcon(getClass().getResource("Top-Left.png"));
		Image topLeftArcImg = topLeftArc.getImage().getScaledInstance(10, 40, Image.SCALE_SMOOTH);
		ImageIcon newTopLeftArc = new ImageIcon(topLeftArcImg);
		
		topLeftarcLbl = new JLabel("");
		GridBagConstraints gbc_topLeftarcLbl = new GridBagConstraints();
		gbc_topLeftarcLbl.gridheight = 2;
		gbc_topLeftarcLbl.insets = new Insets(0, 5, 0, 0);
		gbc_topLeftarcLbl.gridx = 0;
		gbc_topLeftarcLbl.gridy = 0;
		topLeftarcLbl.setIcon(newTopLeftArc);
		panel_1.add(topLeftarcLbl, gbc_topLeftarcLbl);
		
		topLeftAlarmBell = new JLabel("");
		GridBagConstraints gbc_topLeftAlarmBell = new GridBagConstraints();
		gbc_topLeftAlarmBell.anchor = GridBagConstraints.SOUTHWEST;
		gbc_topLeftAlarmBell.insets = new Insets(0, 0, 0, 5);
		gbc_topLeftAlarmBell.gridx = 1;
		gbc_topLeftAlarmBell.gridy = 1;
		panel_1.add(topLeftAlarmBell, gbc_topLeftAlarmBell);
		topLeftAlarmBell.setIcon(newImgIcon);
		topLeftAlarmBell.setSize(10, 10);
		
		txtHighPriority = new JLabel("");
		txtHighPriority.setVerticalAlignment(SwingConstants.BOTTOM);
		GridBagConstraints gbc_txtHighPriority = new GridBagConstraints();
		gbc_txtHighPriority.fill = GridBagConstraints.VERTICAL;
		gbc_txtHighPriority.insets = new Insets(0, 0, 0, 0);
		gbc_txtHighPriority.gridx = 2;
		gbc_txtHighPriority.gridy = 1;
		panel_1.add(txtHighPriority, gbc_txtHighPriority);
		txtHighPriority.setFont(new Font("SansSerif", Font.BOLD, 16));
		
		txtCriticalPriority = new JLabel("");
		txtCriticalPriority.setVerticalAlignment(SwingConstants.BOTTOM);
		GridBagConstraints gbc_txtCriticalPriority = new GridBagConstraints();
		gbc_txtCriticalPriority.fill = GridBagConstraints.VERTICAL;
		gbc_txtCriticalPriority.insets = new Insets(0, 0, 0, 0);
		gbc_txtCriticalPriority.gridx = 5;
		gbc_txtCriticalPriority.gridy = 1;
		panel_1.add(txtCriticalPriority, gbc_txtCriticalPriority);
		txtCriticalPriority.setFont(new Font("SansSerif", Font.BOLD, 16));
		
		topRightAlarmBell = new JLabel("");
		GridBagConstraints gbc_topRightAlarmBell = new GridBagConstraints();
		gbc_topRightAlarmBell.anchor = GridBagConstraints.SOUTHEAST;
		gbc_topRightAlarmBell.insets = new Insets(0, 0, 0, 0);
		gbc_topRightAlarmBell.gridx = 6;
		gbc_topRightAlarmBell.gridy = 1;
		panel_1.add(topRightAlarmBell, gbc_topRightAlarmBell);
		topRightAlarmBell.setIcon(newImgIcon);
		
		ImageIcon topRightArc = new ImageIcon(getClass().getResource("Top-Right.png"));
		Image topRightArcImg = topRightArc.getImage().getScaledInstance(10, 40, Image.SCALE_SMOOTH);
		ImageIcon newTopRightArc = new ImageIcon(topRightArcImg);
		
		topRightArcLbl = new JLabel("");
		GridBagConstraints gbc_topRightArcLbl = new GridBagConstraints();
		gbc_topRightArcLbl.fill = GridBagConstraints.VERTICAL;
		gbc_topRightArcLbl.gridheight = 2;
		gbc_topRightArcLbl.gridx = 7;
		gbc_topRightArcLbl.gridy = 0;
		gbc_topRightArcLbl.insets = new Insets(0,0,0,5);
		topRightArcLbl.setIcon(newTopRightArc);
		panel_1.add(topRightArcLbl, gbc_topRightArcLbl);
		
		highPriorityLbl = new JLabel(" High Priority");
		GridBagConstraints gbc_highPriorityLbl = new GridBagConstraints();
		gbc_highPriorityLbl.anchor = GridBagConstraints.SOUTHWEST;
		gbc_highPriorityLbl.gridwidth = 3;
		gbc_highPriorityLbl.insets = new Insets(0, 0, 0, 0);
		gbc_highPriorityLbl.gridx = 1;
		gbc_highPriorityLbl.gridy = 0;
		panel_1.add(highPriorityLbl, gbc_highPriorityLbl);
		highPriorityLbl.setForeground(Color.GRAY);
		highPriorityLbl.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		criticalPrioritylbl = new JLabel("Critical Priority");
		GridBagConstraints gbc_criticalPrioritylbl = new GridBagConstraints();
		gbc_criticalPrioritylbl.anchor = GridBagConstraints.SOUTHEAST;
		gbc_criticalPrioritylbl.insets = new Insets(0, 0, 0,0);
		gbc_criticalPrioritylbl.gridwidth = 3;
		gbc_criticalPrioritylbl.gridx = 4;
		gbc_criticalPrioritylbl.gridy = 0;
		panel_1.add(criticalPrioritylbl, gbc_criticalPrioritylbl);
		criticalPrioritylbl.setFont(new Font("Tahoma", Font.BOLD, 12));
		criticalPrioritylbl.setForeground(Color.GRAY);
		
		value = new IA_Segmented_Panel();
		value.setBorder(null);
		value.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_value = new GridBagConstraints();
		gbc_value.insets = new Insets(0, 0, 5, 0);
		gbc_value.fill = GridBagConstraints.BOTH;
		gbc_value.gridx = 0;
		gbc_value.gridy = 1;
		valuePanel.add(value, gbc_value);
		GridBagLayout gbl_value = new GridBagLayout();
		gbl_value.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_value.rowHeights = new int[]{0, 0, 0, 0};
		gbl_value.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_value.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		value.setLayout(gbl_value);
		
		panel_2 = new JPanel();
		panel_2.setPreferredSize(new Dimension(419,50));
		panel_2.setBorder(BorderFactory.createEmptyBorder());
		panel_2.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		valuePanel.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		
		gbl_panel_2.columnWidths = new int[]{10, 42, 42, 95, 95, 42, 42, 10};
		gbl_panel_2.rowHeights = new int[]{25,25};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.1,0.2, 0.2, 0.2, 0.2,0.1,0.0};
		
		gbl_panel_2.rowWeights =new double[]{0.0, 0.0};
		panel_2.setLayout(gbl_panel_2);
		
		ImageIcon bottomLeftArc = new ImageIcon(getClass().getResource("Bottom-Left.png"));
		Image bottomLeftArcImg = bottomLeftArc.getImage().getScaledInstance(10, 40, Image.SCALE_SMOOTH);
		ImageIcon newbottomLeftArc = new ImageIcon(bottomLeftArcImg);
		
		bottomLeftArcLbl = new JLabel("");
		GridBagConstraints gbc_bottomLeftArcLbl = new GridBagConstraints();
		gbc_bottomLeftArcLbl.gridheight =2;
		gbc_bottomLeftArcLbl.insets = new Insets(0, 5, 0, 0);
		gbc_bottomLeftArcLbl.gridx = 0;
		gbc_bottomLeftArcLbl.gridy = 0;
		bottomLeftArcLbl.setIcon(newbottomLeftArc);
		panel_2.add(bottomLeftArcLbl, gbc_bottomLeftArcLbl);
		
		bottomLeftBell = new JLabel("");
		GridBagConstraints gbc_bottomLeftBell = new GridBagConstraints();
		gbc_bottomLeftBell.anchor = GridBagConstraints.WEST;
		gbc_bottomLeftBell.fill = GridBagConstraints.VERTICAL;
		gbc_bottomLeftBell.insets = new Insets(0, 0, 0, 0);
		gbc_bottomLeftBell.gridx = 1;
		gbc_bottomLeftBell.gridy = 0;
		bottomLeftBell.setIcon(newImgIcon);
		panel_2.add(bottomLeftBell, gbc_bottomLeftBell);
		
		txtLowPriority = new JLabel("");
		txtLowPriority.setVerticalAlignment(SwingConstants.TOP);
		txtLowPriority.setFont(new Font("SansSerif", Font.BOLD, 16));
		GridBagConstraints gbc_txtLowPriority = new GridBagConstraints();
		gbc_txtLowPriority.fill = GridBagConstraints.VERTICAL;
		gbc_txtLowPriority.insets = new Insets(0, 0, 0, 0);
		gbc_txtLowPriority.gridx = 2;
		gbc_txtLowPriority.gridy = 0;
		panel_2.add(txtLowPriority, gbc_txtLowPriority);
		
		txtMediumPriority = new JLabel("");
		txtMediumPriority.setVerticalAlignment(SwingConstants.TOP);
		txtMediumPriority.setFont(new Font("SansSerif", Font.BOLD, 16));
		GridBagConstraints gbc_txtMediumPriority = new GridBagConstraints();
		gbc_txtMediumPriority.fill = GridBagConstraints.VERTICAL;
		gbc_txtMediumPriority.insets = new Insets(0, 0, 0, 0);
		gbc_txtMediumPriority.gridx = 5;
		gbc_txtMediumPriority.gridy = 0;
		panel_2.add(txtMediumPriority, gbc_txtMediumPriority);
		
		bottomRightBell = new JLabel("");
		GridBagConstraints gbc_bottomRightBell = new GridBagConstraints();
		gbc_bottomRightBell.anchor = GridBagConstraints.EAST;
		gbc_bottomRightBell.fill = GridBagConstraints.VERTICAL;
		gbc_bottomRightBell.insets = new Insets(0, 0, 0, 0);
		gbc_bottomRightBell.gridx = 6;
		gbc_bottomRightBell.gridy = 0;
		bottomRightBell.setIcon(newImgIcon);
		panel_2.add(bottomRightBell, gbc_bottomRightBell);
		
		ImageIcon bottomRightArc = new ImageIcon(getClass().getResource("Bottom-Right.png"));
		Image bottomRightArcImg = bottomRightArc.getImage().getScaledInstance(10, 40, Image.SCALE_SMOOTH);
		ImageIcon newbottomRightArc = new ImageIcon(bottomRightArcImg);
		
		bottomRightArcLbl = new JLabel();
		GridBagConstraints gbc_bottomRightArcLbl = new GridBagConstraints();
		gbc_bottomRightArcLbl.gridheight = 2;
		gbc_bottomRightArcLbl.insets = new Insets(0, 0, 0, 5);
		gbc_bottomRightArcLbl.gridx = 7;
		gbc_bottomRightArcLbl.gridy = 0;
		bottomRightArcLbl.setIcon(newbottomRightArc);
		panel_2.add(bottomRightArcLbl, gbc_bottomRightArcLbl);
		
		lowPrioritylbl = new JLabel(" Low Priority");
		lowPrioritylbl.setForeground(Color.GRAY);
		lowPrioritylbl.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lowPrioritylbl = new GridBagConstraints();
		gbc_lowPrioritylbl.anchor = GridBagConstraints.NORTHWEST;
		gbc_lowPrioritylbl.gridwidth = 3;
		gbc_lowPrioritylbl.insets = new Insets(0, 0, 0, 0);
		gbc_lowPrioritylbl.gridx = 1;
		gbc_lowPrioritylbl.gridy = 1;
		panel_2.add(lowPrioritylbl, gbc_lowPrioritylbl);
		
		mediumPrioritylbl = new JLabel("Medium Priority");
		mediumPrioritylbl.setFont(new Font("Tahoma", Font.BOLD, 12));
		mediumPrioritylbl.setForeground(Color.GRAY);
		GridBagConstraints gbc_mediumPrioritylbl = new GridBagConstraints();
		gbc_mediumPrioritylbl.anchor = GridBagConstraints.NORTHEAST;
		gbc_mediumPrioritylbl.insets = new Insets(0, 0, 0, 0);
		gbc_mediumPrioritylbl.gridwidth = 3;
		gbc_mediumPrioritylbl.gridx = 4;
		gbc_mediumPrioritylbl.gridy = 1;
		panel_2.add(mediumPrioritylbl, gbc_mediumPrioritylbl);
		
		panel = new RoundedPanel();
		panel.setForeground(Color.WHITE);
		panel.setPreferredSize(new Dimension(419, 600));
		panel.setBorder(new EmptyBorder(0, 0, 10, 0));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridheight = 2;
		gbc_panel.insets = new Insets(5, 0, 5, 5);
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{200};
		gbl_panel.rowHeights = new int[]{15, 80, 80, 0};
		gbl_panel.columnWeights = new double[]{1.0};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		contentLblPanel = new RoundedBlackPanel();
		contentLblPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		contentLblPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		GridBagConstraints gbc_contentLblPanel = new GridBagConstraints();
		gbc_contentLblPanel.anchor = GridBagConstraints.NORTH;
		gbc_contentLblPanel.insets = new Insets(0, 0, 0, 0);
		gbc_contentLblPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_contentLblPanel.gridx = 0;
		gbc_contentLblPanel.gridy = 0;
		
		panel.add(contentLblPanel, gbc_contentLblPanel);
		
		
		//end overview panel
		content = new JScrollPane();
		GridBagConstraints gbc_content = new GridBagConstraints();
		gbc_content.fill = GridBagConstraints.BOTH;
		gbc_content.gridheight = 2;
		gbc_content.gridx = 0;
		gbc_content.gridy = 1;
		panel.add(content, gbc_content);
		content.setBorder(BorderFactory.createEmptyBorder());
		content.setBackground(new Color(224, 255, 255));
		
		
		 mapLocation = new RoundedPanel();
		 mapLocation.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		 mapLocation.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		 mapLocation.setPreferredSize(new Dimension(419, 600));
		 //mapLocation.setBorder(new TitledBorder(null, "Locations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		mapLocation.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_mapLocation = new GridBagConstraints();
		gbc_mapLocation.gridheight = 2;
		gbc_mapLocation.insets = new Insets(5, 0, 5, 5);
		gbc_mapLocation.fill = GridBagConstraints.BOTH;
		gbc_mapLocation.gridx = 2;
		gbc_mapLocation.gridy = 1;
		add(mapLocation, gbc_mapLocation);
		GridBagLayout gbl_mapLocation = new GridBagLayout();
		gbl_mapLocation.columnWidths = new int[]{285, 0};
		gbl_mapLocation.rowHeights = new int[]{15, 152, 152};
		gbl_mapLocation.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_mapLocation.rowWeights = new double[]{0.0, 0.50, 0.50};
		mapLocation.setLayout(gbl_mapLocation);
		
		locationHeaderPanel = new RoundedBlackPanel();
		
		locationHeaderPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		locationHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
//		GridBagConstraints gbc_entireHeadingPanel = new GridBagConstraints();
//		gbc_entireHeadingPanel.fill = GridBagConstraints.BOTH;
//		gbc_entireHeadingPanel.insets = new Insets(0, 0, 5, 0);
//		gbc_entireHeadingPanel.gridx = 0;
//		gbc_entireHeadingPanel.gridy = 0;
		GridBagConstraints gbc_locationHeader;
		gbc_locationHeaderPanel = new GridBagConstraints();
		gbc_locationHeaderPanel.fill = GridBagConstraints.BOTH;
		gbc_locationHeaderPanel.insets = new Insets(0, 0, 0, 0);
		gbc_locationHeaderPanel.gridx = 0;
		gbc_locationHeaderPanel.gridy = 0;
		mapLocation.add(locationHeaderPanel, gbc_locationHeaderPanel);
	
		//locationHeaderPanel.setPreferredSize(new Dimension(66,20));
		locationHeaderPanel.setLayout(new BorderLayout(0, 0));
		lblLocations = new JLabel("   Locations");
		lblLocations.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		lblLocations.setForeground(Constants.COLOR_GREY_LABEL);
		lblLocations.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		lblLocations.setBorder(new EmptyBorder(0,0,0,0));
		
		locationHeaderPanel.add(lblLocations, BorderLayout.NORTH);
		panel_8 = new JPanel();
		panel_8.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		
		locationHeaderPanel.add(panel_8, BorderLayout.CENTER);
		
		GridBagConstraints gbc_panelForMap_1 = new GridBagConstraints();
		gbc_panelForMap_1.fill = GridBagConstraints.BOTH;
		gbc_panelForMap_1.insets = new Insets(0, 0, 0, 0);
		gbc_panelForMap_1.gridx = 0;
		gbc_panelForMap_1.gridy = 1;
		GridBagConstraints gbc__locationTable_1 = new GridBagConstraints();
		gbc__locationTable_1.fill = GridBagConstraints.BOTH;
		gbc__locationTable_1.gridx = 0;
		gbc__locationTable_1.gridy = 2;
		
		panelForMap = new JPanel();
		GridBagConstraints gbc_panelForMapLocation_11 = new GridBagConstraints();
		gbc_panelForMapLocation_11.fill = GridBagConstraints.BOTH;
		gbc_panelForMapLocation_11.insets = new Insets(0, 0, 5, 0);
		gbc_panelForMapLocation_11.gridx = 0;
		gbc_panelForMapLocation_11.gridy = 1;
		mapLocation.add(panelForMap, gbc_panelForMapLocation_11);
		panelForMap.setLayout(new BorderLayout(0, 0));
		
		
		//_locationTable.setData(rpc.getTotalUsersByLocation(dataSource, currentProject, allProjects));
		
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(null);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		mapLocation.add(scrollPane, gbc_scrollPane);
		
		ImageIcon iconUsers = new ImageIcon(getClass().getResource("ydayOverviewUsers.png"));
		Image newImgUsers = iconUsers.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		
		ImageIcon iconSessions = new ImageIcon(getClass().getResource("ydayOverviewSessions.png"));
		Image newImgSessions = iconSessions.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		
		ImageIcon iconActions = new ImageIcon(getClass().getResource("ydayOverviewActions.png"));
		Image newImgActions = iconActions.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		GridBagConstraints gbc_panel_3_1 = new GridBagConstraints();
		gbc_panel_3_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3_1.fill = GridBagConstraints.BOTH;
		gbc_panel_3_1.gridx = 2;
		gbc_panel_3_1.gridy = 2;
		
		devices = new RoundedPanel();
		devices.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		devices.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		devices.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		devices.setPreferredSize(new Dimension(419, 300));
		//devices.setBorder(new TitledBorder(null, "Devices", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		//devices.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_devices = new GridBagConstraints();
		gbc_devices.insets = new Insets(5, 0, 5, 5);
		gbc_devices.fill = GridBagConstraints.BOTH;
		gbc_devices.gridx = 3;
		gbc_devices.gridy = 1;
		add(devices, gbc_devices);
		GridBagLayout gbl_devices = new GridBagLayout();
		gbl_devices.columnWidths = new int[]{213, 0};
		gbl_devices.rowHeights = new int[]{15, 40, 80, 16};
		gbl_devices.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_devices.rowWeights = new double[]{0.0, 0.0,1.0, 0.0};
		devices.setLayout(gbl_devices);
		
		deviceHeaderPanel = new JPanel();
		GridBagConstraints gbc_deviceHeaderPanel = new GridBagConstraints();
		gbc_deviceHeaderPanel.insets = new Insets(0, 0, 5, 0);
		gbc_deviceHeaderPanel.fill = GridBagConstraints.BOTH;
		gbc_deviceHeaderPanel.gridx = 0;
		gbc_deviceHeaderPanel.gridy = 0;
	//	devices.add(deviceHeaderPanel, gbc_deviceHeaderPanel);
		GridBagLayout gbl_deviceHeaderPanel = new GridBagLayout();
		gbl_deviceHeaderPanel.columnWidths = new int[]{0, 0};
		gbl_deviceHeaderPanel.rowHeights = new int[]{20, 50};
		gbl_deviceHeaderPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_deviceHeaderPanel.rowWeights = new double[]{0.0,1.0};
		deviceHeaderPanel.setLayout(gbl_deviceHeaderPanel);
		
		panel_6 = new RoundedBlackPanel();
		panel_6.setPreferredSize(new Dimension(430,14));
		panel_6.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.insets = new Insets(0, 0, 0, 0);
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 0;
		devices.add(panel_6, gbc_panel_6);
		panel_6.setForeground(Color.WHITE);
		panel_6.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		lblNewLabel_3 = new JLabel("   Devices");
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel_3.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel_3.setHorizontalTextPosition(JLabel.LEFT);
		lblNewLabel_3.setVerticalTextPosition(JLabel.CENTER);
		panel_6.add(lblNewLabel_3, BorderLayout.NORTH);
		lblNewLabel_3.setForeground(Constants.COLOR_GREY_LABEL);
		
		panel_10 = new JPanel();
		panel_10.setPreferredSize(new Dimension(430, 2));
		panel_10.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		panel_6.add(panel_10, BorderLayout.CENTER);
		
		panel_4 = new JPanel();
		panel_4.setBackground(Color.DARK_GRAY);
		panel_4.setPreferredSize(new Dimension(200,60));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		devices.add(panel_4, gbc_panel_4);
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		//ImageIcon deviceImg = new ImageIcon(getClass().getResource("devicePC.png"));
		
		ImageIcon imgFilled = new ImageIcon(getClass().getResource("Desktop-Monitor.png"));
		Image imgFilled1 = imgFilled.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
		ImageIcon deviceImg = new ImageIcon(imgFilled1);
		JLabel picLabel = new JLabel(deviceImg);

		picLabel.setPreferredSize(new Dimension(80,80));
		String path = getClass().getResource("Desktop-Monitor.png").getPath();
		panel_4.setLayout(new GridLayout(1,2));
		
		
		
		
		button = new RealTimeDeviceButton();
		button.lblBoldText.setFont(new Font("Tahoma", Font.BOLD, 22));
		button.imgPanel.add(picLabel, BorderLayout.CENTER);
		
		button.lblNormalText.setText("Desktop");
		button.lblNormalText.setForeground(Color.WHITE);
		button.setForeground(Color.white);
		button.setName("DESKTOP_BTN");
		button.addMouseListener(this);
		panel_4.add(button);
		
		//ImageIcon deviceMobile = new ImageIcon(getClass().getResource("devicePhone.png"));
		
		ImageIcon imageMobile = new ImageIcon(getClass().getResource("Mobile.png"));
		Image imgMobile = imageMobile.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		ImageIcon deviceMobile = new ImageIcon(imgMobile);
		
		
		button_1 = new RealTimeDeviceButton();
		button_1.lblBoldText.setFont(new Font("Tahoma", Font.BOLD, 22));
		
		button_1.imgPanel.add(new JLabel(deviceMobile), BorderLayout.CENTER);
		button_1.setName("MOBILE_BTN");
		
		button_1.lblNormalText.setText("Mobile");
		button_1.lblNormalText.setForeground(Color.WHITE);
		button_1.setPreferredSize(new Dimension(40,40));
		button_1.addMouseListener(this);
		panel_4.add(button_1);
		
		   graph = new JPanel();
		   graph.setBackground(Constants.COLOR_WHITE_BACKGROUND);
	         GridBagConstraints gbc_graph = new GridBagConstraints();
	         gbc_graph.insets = new Insets(0, 0, 0, 0);
	         gbc_graph.fill = GridBagConstraints.BOTH;
	         gbc_graph.gridx = 0;
	         gbc_graph.gridy = 2;
	         devices.add(graph, gbc_graph);
	         GridBagLayout gbl_graph = new GridBagLayout();
	            gbl_graph.columnWidths = new int[] {28, 150, 60, 150, 28};
	            gbl_graph.rowHeights = new int[] {150};
	            gbl_graph.columnWeights = new double[]{0.33, 0.0, 0.33, 0.0, 0.33};
	            gbl_graph.rowWeights = new double[]{1.0};
	            graph.setLayout(gbl_graph);
	            
	         symbolPanel = new JPanel();
	         symbolPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
	         GridBagConstraints gbc_symbolPanel = new GridBagConstraints();
	         gbc_symbolPanel.fill = GridBagConstraints.BOTH;
	         gbc_symbolPanel.gridx = 0;
	         gbc_symbolPanel.gridy = 3;
	         devices.add(symbolPanel, gbc_symbolPanel);
	         
	         GridBagLayout gbl_symbolPanel = new GridBagLayout();
	         gbl_symbolPanel.columnWidths = new int[]{50,25, 25,25, 25,50,50,25, 25,25, 25,50};
	         gbl_symbolPanel.rowHeights = new int[]{15, 25};
	         gbl_symbolPanel.columnWeights = new double[]{0.25, 0.0, 0.0,  0.0,  0.0, 0.25, 0.25, 0.0, 0.0, 0.0, 0.0, 0.25};
	         gbl_symbolPanel.rowWeights = new double[]{0.5,0.5};
	         symbolPanel.setLayout(gbl_symbolPanel);
	         
	     
	         
	         ImageIcon darkBlueIcon = new ImageIcon(getClass().getResource("Dark_Blue.png"));
		 		Image darkBlue = darkBlueIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		 newDarkBlue = new ImageIcon(darkBlue);
		 		
		 		ImageIcon lightBlueIcon = new ImageIcon(getClass().getResource("Light_blue.png"));
		 		Image lightBlue = lightBlueIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newlightBlue= new ImageIcon(lightBlue);
		 		
		 		ImageIcon darkerBlueIcon = new ImageIcon(getClass().getResource("Darker_Blue.png"));
		 		Image darkerBlue = darkerBlueIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newDarkerBlue = new ImageIcon(darkerBlue);
		 		
		 		ImageIcon lighterBlueIcon = new ImageIcon(getClass().getResource("Lighter_blue.png"));
		 		Image lighterBlue = lighterBlueIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newlighterBlue = new ImageIcon(lighterBlue);
		 		
		 		ImageIcon darkOrangeIcon = new ImageIcon(getClass().getResource("Dark_Orange.png"));
		 		Image darkOrange = darkOrangeIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newDarkOrange = new ImageIcon(darkOrange);
		 		
		 		ImageIcon lightOrangeIcon = new ImageIcon(getClass().getResource("Light_Orange.png"));
		 		Image lightOrange = lightOrangeIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newlightOrange = new ImageIcon(lightOrange);
		 		
		 		ImageIcon darkerOrangeIcon = new ImageIcon(getClass().getResource("Darker_Orange.png"));
		 		Image darkerOrange = darkerOrangeIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newDarkerOrange = new ImageIcon(darkerOrange);
		 		
		 		ImageIcon lighterOrangeIcon = new ImageIcon(getClass().getResource("Lighter_Orange.png"));
		 		Image lighterOrange = lighterOrangeIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newlighterOrange = new ImageIcon(lighterOrange);
	    		
		 		
		 		
	         darkBlueLbl = new JLabel();
	         GridBagConstraints gbc_darkBlueLbl = new GridBagConstraints();
	         gbc_darkBlueLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_darkBlueLbl.gridx = 2;
	         gbc_darkBlueLbl.gridy = 0;
	         darkBlueLbl.setIcon(newDarkBlue);
	         symbolPanel.add(darkBlueLbl, gbc_darkBlueLbl);
	         
	         lightBlueLbl = new JLabel("");
	         GridBagConstraints gbc_lightBlueLbl = new GridBagConstraints();
	         gbc_lightBlueLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_lightBlueLbl.gridx = 3;
	         gbc_lightBlueLbl.gridy = 0;
	         lightBlueLbl.setIcon(newlightBlue);
	         symbolPanel.add(lightBlueLbl, gbc_lightBlueLbl);
	         
	         darkerBlueLbl = new JLabel();
	         GridBagConstraints gbc_darkerBlueLbl = new GridBagConstraints();
	         gbc_darkerBlueLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_darkerBlueLbl.gridx = 1;
	         gbc_darkerBlueLbl.gridy = 0;
	         darkerBlueLbl.setIcon(newDarkerBlue);
	         symbolPanel.add(darkerBlueLbl, gbc_darkerBlueLbl);
	         
	         lighterBlueLbl = new JLabel("");
	         GridBagConstraints gbc_lighterBlueLbl = new GridBagConstraints();
	         gbc_lighterBlueLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_lighterBlueLbl.gridx = 4;
	         gbc_lighterBlueLbl.gridy = 0;
	         lighterBlueLbl.setIcon(newlighterBlue);
	         symbolPanel.add(lighterBlueLbl, gbc_lighterBlueLbl);
	         
	         darkOrangeLbl = new JLabel("");
	         GridBagConstraints gbc_darkOrangeLbl = new GridBagConstraints();
	         gbc_darkOrangeLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_darkOrangeLbl.gridx = 8;
	         gbc_darkOrangeLbl.gridy = 0;
	         darkOrangeLbl.setIcon(newDarkOrange);
	         symbolPanel.add(darkOrangeLbl, gbc_darkOrangeLbl);
	         
	         lightOrangeLbl = new JLabel("");
	         GridBagConstraints gbc_lightOrangeLbl = new GridBagConstraints();
	         gbc_lightOrangeLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_lightOrangeLbl.gridx = 9;
	         gbc_lightOrangeLbl.gridy = 0;
	         lightOrangeLbl.setIcon(newlightOrange);
	         symbolPanel.add(lightOrangeLbl, gbc_lightOrangeLbl);
	         
	         darkerOrnageLbl = new JLabel("");
	 		GridBagConstraints gbc_darkerOrnageLbl = new GridBagConstraints();
	 		gbc_darkerOrnageLbl.insets = new Insets(1, 1,1, 1);
	 		gbc_darkerOrnageLbl.gridx = 7;
	 		gbc_darkerOrnageLbl.gridy = 0;
	 		darkerOrnageLbl.setIcon(newDarkerOrange);
	 		symbolPanel.add(darkerOrnageLbl, gbc_darkerOrnageLbl);
	 		
	 		lighterOrangeLbl = new JLabel("");
	 		GridBagConstraints gbc_lighterOrangeLbl = new GridBagConstraints();
	 		gbc_lighterOrangeLbl.insets = new Insets(1, 1, 1, 1);
	 		gbc_lighterOrangeLbl.gridx = 10;
	 		gbc_lighterOrangeLbl.gridy = 0;
	 		lighterOrangeLbl.setIcon(newlighterOrange);
	 		symbolPanel.add(lighterOrangeLbl, gbc_lighterOrangeLbl);
	         
	         device1IconLbl = new JLabel("");
	         GridBagConstraints gbc_device1IconLbl = new GridBagConstraints();
	         gbc_device1IconLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_device1IconLbl.gridx = 2;
	         gbc_device1IconLbl.gridy = 1;
	         symbolPanel.add(device1IconLbl, gbc_device1IconLbl);
	         
	         device2IconLbl = new JLabel("");
	         GridBagConstraints gbc_device2IconLbl = new GridBagConstraints();
	         gbc_device2IconLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_device2IconLbl.gridx = 3;
	         gbc_device2IconLbl.gridy = 1;
	         symbolPanel.add(device2IconLbl, gbc_device2IconLbl);
	         
	         device3IconLbl = new JLabel("");
	 		GridBagConstraints gbc_device3Lbl = new GridBagConstraints();
	 		gbc_device3Lbl.insets = new Insets(1, 1, 1, 1);
	 		gbc_device3Lbl.gridx = 1;
	 		gbc_device3Lbl.gridy = 1;
	 		
	 		symbolPanel.add(device3IconLbl, gbc_device3Lbl);
	 		
	 		device4IconLbl = new JLabel("");
	 		GridBagConstraints gbc_device4Lbl = new GridBagConstraints();
	 		gbc_device4Lbl.insets = new Insets(1, 1, 1, 1);
	 		gbc_device4Lbl.gridx = 4;
	 		gbc_device4Lbl.gridy = 1;
	 		
	 		symbolPanel.add(device4IconLbl, gbc_device4Lbl);
	 		
	       
		 	
		 		
	         browser1IconLbl = new JLabel("");
	         GridBagConstraints gbc_browser1IconLbl = new GridBagConstraints();
	         gbc_browser1IconLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_browser1IconLbl.gridx = 8;
	         gbc_browser1IconLbl.gridy = 1;
	        // browser1IconLbl.setIcon(newBrowser);
	         symbolPanel.add(browser1IconLbl, gbc_browser1IconLbl);
	         
	         browser2IconLbl = new JLabel("");
	         GridBagConstraints gbc_browser2IconLbl = new GridBagConstraints();
	         gbc_browser2IconLbl.insets = new Insets(1, 1, 1, 1);
	         gbc_browser2IconLbl.gridx = 9;
	         gbc_browser2IconLbl.gridy = 1;
	       //  browser2IconLbl.setIcon(newBrowser2);
	         symbolPanel.add(browser2IconLbl, gbc_browser2IconLbl);
	         
	         browser3IconLbl = new JLabel("");
	 		GridBagConstraints gbc_browser3Lbl = new GridBagConstraints();
	 		gbc_browser3Lbl.insets = new Insets(1, 1, 1, 1);
	 		gbc_browser3Lbl.gridx = 7;
	 		gbc_browser3Lbl.gridy = 1;
	 		symbolPanel.add(browser3IconLbl, gbc_browser3Lbl);
	 		
	 		browser4IconLbl = new JLabel("");
	 		GridBagConstraints gbc_browser4Lbl = new GridBagConstraints();
	 		gbc_browser4Lbl.insets = new Insets(1, 1, 1, 1);
	 		gbc_browser4Lbl.gridx = 10;
	 		gbc_browser4Lbl.gridy = 1;
	 		symbolPanel.add(browser4IconLbl, gbc_browser4Lbl);
	 		
	 		engagement = new RoundedPanel();
	 		engagement.setForeground(Color.WHITE);
	 		engagement.setPreferredSize(new Dimension(419, 280));
	 		//engagement.setBorder(new TitledBorder(null, "Engagement", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	 		engagement.setBorder(new EmptyBorder(0, 0, 5, 0));
	 		engagement.setBackground(Color.WHITE);
	 		GridBagConstraints gbc_engagement = new GridBagConstraints();
	 		gbc_engagement.insets = new Insets(0, 0, 0, 5);
	 		gbc_engagement.fill = GridBagConstraints.BOTH;
	 		gbc_engagement.gridx = 1;
	 		gbc_engagement.gridy = 3;
	 		add(engagement, gbc_engagement);
	 		GridBagLayout gbl_engagement = new GridBagLayout();
	 		gbl_engagement.rowHeights = new int[]{80, 180};
	 		gbl_engagement.columnWidths = new int[]{70, 70, 72};
	 		gbl_engagement.rowWeights = new double[]{0.20,0.80};
	 		gbl_engagement.columnWeights = new double[]{1.0, 1.0, 1.0};
	 		engagement.setLayout(gbl_engagement);
	 		
	 		headerPanel = new RoundedBlackPanel();
	 		headerPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	 		GridBagConstraints gbc_headerPanel = new GridBagConstraints();
	 		gbc_headerPanel.gridwidth = 3;
	 		gbc_headerPanel.insets = new Insets(0, 0, 0, 0);
	 		gbc_headerPanel.fill = GridBagConstraints.BOTH;
	 		gbc_headerPanel.gridx = 0;
	 		gbc_headerPanel.gridy = 0;
	 		engagement.add(headerPanel, gbc_headerPanel);
	 		GridBagLayout gbl_headerPanel = new GridBagLayout();
	 		gbl_headerPanel.columnWidths = new int[]{2, 92, 2};
	 		gbl_headerPanel.rowHeights = new int[]{15, 30};
	 		gbl_headerPanel.columnWeights = new double[]{ 1.0, 1.0, 1.0};
	 		gbl_headerPanel.rowWeights = new double[]{0.5, 0.5};
	 		headerPanel.setLayout(gbl_headerPanel);
	 		
	 	//	IA_PanelLabel engagementHedingLabel = new IA_PanelLabel("Engagement");
	 		JLabel engagementHedingLabel = new JLabel("   Engagement");
	 		engagementHedingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
	 		GridBagConstraints gbc_engagementHedingLabel = new GridBagConstraints();
	 		gbc_engagementHedingLabel.gridwidth = 2;
	 		gbc_engagementHedingLabel.anchor = GridBagConstraints.NORTH;
	 		gbc_engagementHedingLabel.fill = GridBagConstraints.HORIZONTAL;
	 		gbc_engagementHedingLabel.insets = new Insets(0, 0, 0, 0);
	 		gbc_engagementHedingLabel.gridx = 0;
	 		gbc_engagementHedingLabel.gridy = 0;
	 		headerPanel.add(engagementHedingLabel, gbc_engagementHedingLabel);
	 		engagementHedingLabel.setVerticalAlignment(SwingConstants.TOP);
	 		engagementHedingLabel.setForeground(Constants.COLOR_GREY_LABEL);
	 		engagementHedingLabel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	 		
	 		panel_5 = new JPanel();
	 		panel_5.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	 		
	 		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
	 		gbc_panel_5.gridwidth = 4;
	 		gbc_panel_5.insets= new Insets(0,0,0,0);
	 		gbc_panel_5.fill = GridBagConstraints.BOTH;
	 		gbc_panel_5.gridx = 0;
	 		gbc_panel_5.gridy = 1;
	 		headerPanel.add(panel_5, gbc_panel_5);
	 		GridBagLayout gbl_panel_5 = new GridBagLayout();
	 		gbl_panel_5.columnWidths = new int[]{209,209};
	 		gbl_panel_5.rowHeights = new int[]{30};
	 		gbl_panel_5.columnWeights = new double[]{ 0.5,0.5};
	 		gbl_panel_5.rowWeights = new double[]{1.0};
	 		panel_5.setLayout(gbl_panel_5);
	 		
//	 		avgSessionTimeButton = new RoundedButton();
//	 		avgSessionTimeButton.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
//	 		avgSessionTimeButton.lblboldtext.setForeground(Color.WHITE);
//	 		GridBagConstraints gbc_avgSessionTimeButton = new GridBagConstraints();
//	 		gbc_avgSessionTimeButton.fill = GridBagConstraints.BOTH;
//	 		gbc_avgSessionTimeButton.insets = new Insets(5, 5, 5, 0);
//	 		gbc_avgSessionTimeButton.gridx = 0;
//	 		gbc_avgSessionTimeButton.gridy = 0;
//	 		
//	 		avgSessionTimeButton.setBorder(emtyBordet);
//	 		avgSessionTimeButton.setForeground(Color.WHITE);
//	 		avgSessionTimeButton.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
//	 		avgSessionTimeButton.setPreferredSize(new Dimension(70, 10));
//	 		avgSessionTimeButton.addMouseListener(this);
//	 		avgSessionTimeButton.setName("AVG_SESSION_TIME_BTN");
//	 		
//	 		panel_5.add(avgSessionTimeButton, gbc_avgSessionTimeButton);
	 		
	 		avgDepthOfScreen = new RoundedButton();
	 		avgDepthOfScreen.lblboldtext.setForeground(Color.WHITE);
	 		avgDepthOfScreen.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
	 		avgDepthOfScreen.addMouseListener(this);
	 		avgDepthOfScreen.setName("AVG_DEPTH_SCREEN");
	 		GridBagConstraints gbc_avgDepthOfScreen = new GridBagConstraints();
	 		gbc_avgDepthOfScreen.insets = new Insets(0, 5, 5, 5);
	 		gbc_avgDepthOfScreen.fill = GridBagConstraints.BOTH;
	 		gbc_avgDepthOfScreen.gridx = 0;
	 		gbc_avgDepthOfScreen.gridy = 0;
	 		panel_5.add(avgDepthOfScreen, gbc_avgDepthOfScreen);
	 		//btnNewButton_1.setBorderPainted(false);
	 		avgDepthOfScreen.setBorder(emtyBordet);
	 		
	 		avgDepthOfScreen.setForeground(Color.WHITE);
	 		avgDepthOfScreen.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	 		avgDepthOfScreen.setPreferredSize(new Dimension(70, 40));
	 		
	 		activeUsersBtn = new RoundedButton();
	 		activeUsersBtn.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
	 		activeUsersBtn.lblboldtext.setForeground(Color.WHITE);
	 		activeUsersBtn.addMouseListener(this);
	 		activeUsersBtn.setName("ACT_USR");
	 		GridBagConstraints gbc_activeUsersBtn = new GridBagConstraints();
	 		gbc_activeUsersBtn.fill = GridBagConstraints.BOTH;
	 		gbc_activeUsersBtn.gridx = 1;
	 		gbc_activeUsersBtn.gridy = 0;
	 		gbc_activeUsersBtn.insets = new Insets(0,5,5,5);
	 		panel_5.add(activeUsersBtn, gbc_activeUsersBtn);
	 		
	 		activeUsersBtn.setBorder(emtyBordet);
	 		
	 		activeUsersBtn.setForeground(Color.WHITE);
	 		activeUsersBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	 		
	 		activeUsersBtn.setPreferredSize(new Dimension(70, 10));

	 		
	 		engagementInnerPannel = new JScrollPane();
	 		engagementInnerPannel.setPreferredSize(new Dimension(300,180));
	 		engagementInnerPannel.setBorder(BorderFactory.createEmptyBorder());
	 		engagementInnerPannel.setViewportBorder(null);
	 		//engagementInnerPannel.
	 		GridBagConstraints gbc_engagementInnerPannel = new GridBagConstraints();
	 		gbc_engagementInnerPannel.fill = GridBagConstraints.BOTH;
	 		//gbc_engagementInnerPannel.insets = new Insets(0 , 10 , 0 ,0);
	 		gbc_engagementInnerPannel.gridwidth = 3;
	 		gbc_engagementInnerPannel.gridx = 0;
	 		gbc_engagementInnerPannel.gridy = 1;
	 		engagement.add(engagementInnerPannel, gbc_engagementInnerPannel);
//	 		
	 		
	 		//new layout by Omkar
	 		currentOverview = new RoundedPanel();
	 		currentOverview.setForeground(Color.WHITE);
	 		currentOverview.setPreferredSize(new Dimension(419, 280));
	 		currentOverview.setBorder(new EmptyBorder(0, 0, 0, 0));
	 		currentOverview.setBackground(new Color(224, 255, 255));
	 		GridBagConstraints gbc_currentOverview = new GridBagConstraints();
	 		gbc_currentOverview.insets = new Insets(0, 0, 0, 5);
	 		gbc_currentOverview.fill = GridBagConstraints.BOTH;
	 		gbc_currentOverview.gridx = 2;
	 		gbc_currentOverview.gridy = 3;
	 		add(currentOverview, gbc_currentOverview);
	 		GridBagLayout gbl_currentOverview = new GridBagLayout();
	 		gbl_currentOverview.columnWidths = new int[]{40, 110, 55, 55,110,40};
	 		gbl_currentOverview.rowHeights = new int[]{15, 30, 30, 30, 30, 30, 30, 30,30 };
	 		gbl_currentOverview.columnWeights = new double[]{0.0,0.33,0.33,0.33,0.33,0.0};
	 		gbl_currentOverview.rowWeights = new double[]{0.0,0.5,0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.5};
	 		currentOverview.setLayout(gbl_currentOverview);
	 		
	 	
	 		contentLblPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			contentLblPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
			GridBagLayout gbl_contentLblPanel = new GridBagLayout();
			gbl_contentLblPanel.columnWidths = new int[]{5,310, 5};
			gbl_contentLblPanel.rowHeights = new int[]{ 10, 0};
			gbl_contentLblPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
			gbl_contentLblPanel.rowWeights = new double[]{ 0.0, Double.MIN_VALUE};
			contentLblPanel.setLayout(gbl_contentLblPanel);
			
			lblNewLabel_4 = new JLabel("   Content");
			
			lblNewLabel_4.setVerticalAlignment(SwingConstants.TOP);
			lblNewLabel_4.setHorizontalAlignment(SwingConstants.LEFT);
			lblNewLabel_4.setHorizontalTextPosition(JLabel.LEFT);
			lblNewLabel_4.setVerticalTextPosition(JLabel.TOP);
			lblNewLabel_4.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblNewLabel_4.setForeground(Constants.COLOR_GREY_LABEL);
			GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
			gbc_lblNewLabel_4.gridwidth = 3;
			gbc_lblNewLabel_4.anchor = GridBagConstraints.NORTH;
			
			gbc_lblNewLabel_4.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 0);
			gbc_lblNewLabel_4.gridx = 0;
			gbc_lblNewLabel_4.gridy = 0;
			contentLblPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
			
			panel_9 = new JPanel();
			panel_9.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			GridBagConstraints gbc_panel_9 = new GridBagConstraints();
			gbc_panel_9.gridwidth = 3;
			gbc_panel_9.anchor = GridBagConstraints.NORTH;
			gbc_panel_9.fill = GridBagConstraints.HORIZONTAL;
			gbc_panel_9.gridx = 0;
			gbc_panel_9.gridy = 1;
			gbc_panel_9.insets = new Insets(0,0,0,0);
			contentLblPanel.add(panel_9, gbc_panel_9);
			contentHeaderLblPanel = new RoundedBlackPanel();
	 		GridBagConstraints gbc_contentHeaderLblPanel = new GridBagConstraints();
	 		gbc_contentHeaderLblPanel.gridwidth = 6;
	 		gbc_contentHeaderLblPanel.insets = new Insets(0, 0, 0, 0);
	 		gbc_contentHeaderLblPanel.fill = GridBagConstraints.BOTH;
	 		gbc_contentHeaderLblPanel.gridx = 0;
	 		gbc_contentHeaderLblPanel.gridy = 0;
	 		currentOverview.add(contentHeaderLblPanel, gbc_contentHeaderLblPanel);
	 		GridBagLayout gbl_contentHeaderLblPanel = new GridBagLayout();
	 		gbl_contentHeaderLblPanel.columnWidths = new int[]{18, 310,5};
	 		gbl_contentHeaderLblPanel.rowHeights = new int[]{15, 9};
	 		gbl_contentHeaderLblPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
	 		gbl_contentHeaderLblPanel.rowWeights = new double[]{0.0, 1.0};
	 		contentHeaderLblPanel.setLayout(gbl_contentHeaderLblPanel);
	 		
	 		//IA_PanelLabel lblHedingCurrentOverView_1 = new IA_PanelLabel("Current Overview");
	 		JLabel lblHedingCurrentOverView_1 = new JLabel("   Current Overview");
	 		GridBagConstraints gbc_lblHedingCurrentOverView_1 = new GridBagConstraints();
	 		gbc_lblHedingCurrentOverView_1.gridwidth = 2;
	 		gbc_lblHedingCurrentOverView_1.anchor = GridBagConstraints.NORTH;
	 		gbc_lblHedingCurrentOverView_1.fill = GridBagConstraints.HORIZONTAL;
	 		gbc_lblHedingCurrentOverView_1.insets = new Insets(0, 0, 0, 0);
	 		gbc_lblHedingCurrentOverView_1.gridx = 0;
	 		gbc_lblHedingCurrentOverView_1.gridy = 0;
	 		contentHeaderLblPanel.add(lblHedingCurrentOverView_1, gbc_lblHedingCurrentOverView_1);
	 		lblHedingCurrentOverView_1.setFont(new Font("SansSerif", Font.BOLD, 12));
	 		lblHedingCurrentOverView_1.setForeground(Constants.COLOR_GREY_LABEL);
	 		lblHedingCurrentOverView_1.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	 		
	 		panel_7 = new JPanel();
	 		panel_7.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
	 		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
	 		gbc_panel_7.gridwidth = 3;
	 		gbc_panel_7.fill = GridBagConstraints.BOTH;
	 		gbc_panel_7.gridx = 0;
	 		gbc_panel_7.gridy = 1;
	 		contentHeaderLblPanel.add(panel_7, gbc_panel_7);
	 		
	 		txtSessions = new JLabel("");
	 		txtSessions.setFont(txtSessions.getFont().deriveFont(txtSessions.getFont().getStyle() | Font.BOLD, 24f));
	 		txtSessions.setVerticalAlignment(SwingConstants.BOTTOM);
	 		GridBagConstraints gbc_txtSessions = new GridBagConstraints();
	 		gbc_txtSessions.anchor = GridBagConstraints.SOUTHWEST;
	 		gbc_txtSessions.insets = new Insets(0, 0, 5, 5);
	 		gbc_txtSessions.gridx = 1;
	 		gbc_txtSessions.gridy = 1;
	 		currentOverview.add(txtSessions, gbc_txtSessions);
	 		
	 		usrIconLabel = new JLabel("");
	 		usrIconLabel.setIcon(new ImageIcon(newImgUsers));
	 		
	 		GridBagConstraints gbc_usrIconLabel = new GridBagConstraints();
	 		gbc_usrIconLabel.gridwidth = 2;
	 		gbc_usrIconLabel.gridheight = 2;
	 		gbc_usrIconLabel.insets = new Insets(0, 0, 5, 5);
	 		gbc_usrIconLabel.gridx = 2;
	 		gbc_usrIconLabel.gridy = 1;
	 		currentOverview.add(usrIconLabel, gbc_usrIconLabel);
	 		
	 		txtAvgSession = new JLabel("");
	 		txtAvgSession.setFont(txtAvgSession.getFont().deriveFont(txtAvgSession.getFont().getStyle() | Font.BOLD, 27f));
	 		GridBagConstraints gbc_txtAvgSession = new GridBagConstraints();
	 		gbc_txtAvgSession.anchor = GridBagConstraints.SOUTHEAST;
	 		gbc_txtAvgSession.insets = new Insets(0, 0, 5, 5);
	 		gbc_txtAvgSession.gridx = 4;
	 		gbc_txtAvgSession.gridy = 1;
	 		currentOverview.add(txtAvgSession, gbc_txtAvgSession);
	 		
	 		lblSessions = new JLabel("Sessions");
	 		lblSessions.setVerticalAlignment(SwingConstants.TOP);
	 		lblSessions.setHorizontalAlignment(SwingConstants.LEFT);
	 		GridBagConstraints gbc_lblSessions = new GridBagConstraints();
	 		gbc_lblSessions.anchor = GridBagConstraints.NORTHWEST;
	 		gbc_lblSessions.insets = new Insets(0, 0, 5, 5);
	 		gbc_lblSessions.gridx = 1;
	 		gbc_lblSessions.gridy = 2;
	 		currentOverview.add(lblSessions, gbc_lblSessions);
	 		
	 		lblNewLabel = new JLabel("Avg. Session");
	 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
	 		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHEAST;
	 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
	 		gbc_lblNewLabel.gridx = 4;
	 		gbc_lblNewLabel.gridy = 2;
	 		currentOverview.add(lblNewLabel, gbc_lblNewLabel);
	 		
	 		ImageIcon devidingLine = new ImageIcon(getClass().getResource("Dividing-Line.png"));
			Image devidingLineNew = devidingLine.getImage().getScaledInstance(400, 20, Image.SCALE_SMOOTH);
			ImageIcon newDevidingLine = new ImageIcon(devidingLineNew);
	 		
			dividingLineLabel1 = new JLabel("");
	 		GridBagConstraints gbc_dividingLineLabel1 = new GridBagConstraints();
	 		gbc_dividingLineLabel1.fill = GridBagConstraints.HORIZONTAL;
	 		gbc_dividingLineLabel1.gridwidth = 6;
	 		gbc_dividingLineLabel1.insets = new Insets(0, 0, 0, 0);
	 		gbc_dividingLineLabel1.gridx = 0;
	 		gbc_dividingLineLabel1.gridy = 3;
	 		dividingLineLabel1.setIcon(newDevidingLine);
	 		currentOverview.add(dividingLineLabel1, gbc_dividingLineLabel1);
	 		
	 		txtScreenViews = new JLabel("");
	 		txtScreenViews.setFont(txtScreenViews.getFont().deriveFont(txtScreenViews.getFont().getStyle() | Font.BOLD, 25f));
	 		GridBagConstraints gbc_txtScreenViews = new GridBagConstraints();
	 		gbc_txtScreenViews.anchor = GridBagConstraints.SOUTHWEST;
	 		gbc_txtScreenViews.insets = new Insets(0, 0, 5, 5);
	 		gbc_txtScreenViews.gridx = 1;
	 		gbc_txtScreenViews.gridy = 4;
	 		currentOverview.add(txtScreenViews, gbc_txtScreenViews);
	 		
	 		alarmIconLabel = new JLabel("");
	 		
	 		
	 		alarmIconLabel.setIcon(new ImageIcon(newImgSessions));
	 		
	 		GridBagConstraints gbc_alarmIconLabel = new GridBagConstraints();
	 		gbc_alarmIconLabel.gridwidth = 2;
	 		gbc_alarmIconLabel.gridheight = 2;
	 		gbc_alarmIconLabel.insets = new Insets(0, 0, 5, 5);
	 		gbc_alarmIconLabel.gridx = 2;
	 		gbc_alarmIconLabel.gridy = 4;
	 		currentOverview.add(alarmIconLabel, gbc_alarmIconLabel);
	 		
	 		txtAvgTimeScreen = new JLabel("");
	 		txtAvgTimeScreen.setFont(txtAvgTimeScreen.getFont().deriveFont(txtAvgTimeScreen.getFont().getStyle() | Font.BOLD, 25f));
	 		GridBagConstraints gbc_txtAvgTimeScreen = new GridBagConstraints();
	 		gbc_txtAvgTimeScreen.anchor = GridBagConstraints.SOUTHEAST;
	 		gbc_txtAvgTimeScreen.insets = new Insets(0, 0, 5, 5);
	 		gbc_txtAvgTimeScreen.gridx = 4;
	 		gbc_txtAvgTimeScreen.gridy = 4;
	 		currentOverview.add(txtAvgTimeScreen, gbc_txtAvgTimeScreen);
	 		
	 		lblScreenViews = new JLabel("Screen Views");
	 		lblScreenViews.setHorizontalAlignment(SwingConstants.TRAILING);
	 		GridBagConstraints gbc_lblScreenViews = new GridBagConstraints();
	 		gbc_lblScreenViews.anchor = GridBagConstraints.NORTHWEST;
	 		gbc_lblScreenViews.insets = new Insets(0, 0, 5, 5);
	 		gbc_lblScreenViews.gridx = 1;
	 		gbc_lblScreenViews.gridy = 5;
	 		currentOverview.add(lblScreenViews, gbc_lblScreenViews);
	 		
	 		lblNewLabel_1 = new JLabel("Avg.Time Screen");
	 		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
	 		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
	 		gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTHEAST;
	 		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
	 		gbc_lblNewLabel_1.gridx = 4;
	 		gbc_lblNewLabel_1.gridy = 5;
	 		currentOverview.add(lblNewLabel_1, gbc_lblNewLabel_1);
	 		
	 		devidingLine2 = new JLabel("");
	 		GridBagConstraints gbc_devidingLine2 = new GridBagConstraints();
	 		gbc_devidingLine2.fill = GridBagConstraints.HORIZONTAL;
	 		gbc_devidingLine2.gridwidth = 6;
	 		gbc_devidingLine2.insets = new Insets(0, 0, 0, 0);
	 		gbc_devidingLine2.gridx = 0;
	 		gbc_devidingLine2.gridy = 6;
	 		devidingLine2.setIcon(newDevidingLine);
	 		currentOverview.add(devidingLine2, gbc_devidingLine2);
	 		
	 		txtActions = new JLabel();
	 		txtActions.setFont(txtActions.getFont().deriveFont(txtActions.getFont().getStyle() | Font.BOLD, 25f));
	 		GridBagConstraints gbc_txtActions = new GridBagConstraints();
	 		gbc_txtActions.anchor = GridBagConstraints.SOUTHWEST;
	 		gbc_txtActions.insets = new Insets(0, 0, 5, 5);
	 		gbc_txtActions.gridx = 1;
	 		gbc_txtActions.gridy = 7;
	 		currentOverview.add(txtActions, gbc_txtActions);
	 		
	 		actionsIconLabel = new JLabel("");
	 		actionsIconLabel.setIcon(new ImageIcon(newImgActions));
	 		
	 		GridBagConstraints gbc_actionsIconLabel = new GridBagConstraints();
	 		gbc_actionsIconLabel.gridwidth = 2;
	 		gbc_actionsIconLabel.gridheight = 2;
	 		gbc_actionsIconLabel.insets = new Insets(0, 0, 0, 5);
	 		gbc_actionsIconLabel.gridx = 2;
	 		gbc_actionsIconLabel.gridy = 7;
	 		currentOverview.add(actionsIconLabel, gbc_actionsIconLabel);
	 		
	 		txtActionSession = new JLabel("");
	 		txtActionSession.setFont(txtActionSession.getFont().deriveFont(txtActionSession.getFont().getStyle() | Font.BOLD, 25f));
	 		GridBagConstraints gbc_txtActionSession = new GridBagConstraints();
	 		gbc_txtActionSession.anchor = GridBagConstraints.SOUTHEAST;
	 		gbc_txtActionSession.insets = new Insets(0, 0, 5, 5);
	 		gbc_txtActionSession.gridx = 4;
	 		gbc_txtActionSession.gridy = 7;
	 		currentOverview.add(txtActionSession, gbc_txtActionSession);
	 		
	 		lblActions = new JLabel("Actions");
	 		lblActions.setHorizontalAlignment(SwingConstants.TRAILING);
	 		GridBagConstraints gbc_lblActions = new GridBagConstraints();
	 		gbc_lblActions.anchor = GridBagConstraints.NORTHWEST;
	 		gbc_lblActions.insets = new Insets(0, 0, 0, 5);
	 		gbc_lblActions.gridx = 1;
	 		gbc_lblActions.gridy = 8;
	 		currentOverview.add(lblActions, gbc_lblActions);
	 		
	 		
	 		
	 		lblNewLabel_2 = new JLabel("Actions/Sessions");
	 		lblNewLabel_2.setHorizontalAlignment(SwingConstants.LEFT);
	 		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
	 		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
	 		gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTHEAST;
	 		gbc_lblNewLabel_2.gridx = 4;
	 		gbc_lblNewLabel_2.gridy = 8;
	 		currentOverview.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
	 		users = new RoundedPanel();
	 		//users.setForeground(Color.WHITE);
	 		users.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			users.setPreferredSize(new Dimension(430, 560));
			users.setForeground(Color.WHITE);
			//users.setBorder(new TitledBorder(null, "Users", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			users.setBorder(new EmptyBorder(0, 0, 5, 0));
			//users.setBackground(new Color(224, 255, 255));
			GridBagConstraints gbc_users = new GridBagConstraints();
			gbc_users.gridheight = 2;
			gbc_users.insets = new Insets(0, 0, 0, 5);
			gbc_users.fill = GridBagConstraints.BOTH;
			gbc_users.gridx = 3;
			gbc_users.gridy = 2;
			add(users, gbc_users);
			GridBagLayout gbl_users = new GridBagLayout();
			gbl_users.columnWidths = new int[]{0, 0};
			gbl_users.rowHeights = new int[]{0, 43, 0, 0, 0, 0, 0, 0};
			gbl_users.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_users.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			users.setLayout(gbl_users);
			
			usersHeaderPanel = new RoundedBlackPanel();
			usersHeaderPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			GridBagConstraints gbc_usersHeaderPanel = new GridBagConstraints();
			gbc_usersHeaderPanel.insets = new Insets(0, 0, 0, 0);
			gbc_usersHeaderPanel.fill = GridBagConstraints.BOTH;
			gbc_usersHeaderPanel.gridx = 0;
			gbc_usersHeaderPanel.gridy = 0;
			users.add(usersHeaderPanel, gbc_usersHeaderPanel);
			GridBagLayout gbl_usersHeaderPanel = new GridBagLayout();
			gbl_usersHeaderPanel.columnWidths = new int[]{0, 0};
			gbl_usersHeaderPanel.rowHeights = new int[]{0, 0, 0};
			gbl_usersHeaderPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_usersHeaderPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			usersHeaderPanel.setLayout(gbl_usersHeaderPanel);
			
			lblUsers = new JLabel("   Users");
			lblUsers.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblUsers.setForeground(Constants.COLOR_GREY_LABEL);
			lblUsers.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			GridBagConstraints gbc_lblUsers = new GridBagConstraints();
			gbc_lblUsers.fill = GridBagConstraints.BOTH;
			gbc_lblUsers.insets = new Insets(0, 0, 5, 0);
			gbc_lblUsers.gridx = 0;
			gbc_lblUsers.gridy = 0;
			usersHeaderPanel.add(lblUsers, gbc_lblUsers);
			
			usrsBtnsPanel = new JPanel();
			usrsBtnsPanel.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			GridBagConstraints gbc_usrsBtnsPanel = new GridBagConstraints();
			gbc_usrsBtnsPanel.insets = new Insets(0,0,0,0);
			gbc_usrsBtnsPanel.fill = GridBagConstraints.BOTH;
			gbc_usrsBtnsPanel.gridx = 0;
			gbc_usrsBtnsPanel.gridy = 1;
			usersHeaderPanel.add(usrsBtnsPanel, gbc_usrsBtnsPanel);
			GridBagLayout gbl_usrsBtnsPanel = new GridBagLayout();
			gbl_usrsBtnsPanel.columnWidths = new int[]{90, 90};
			gbl_usrsBtnsPanel.rowHeights = new int[]{38, 0};
			gbl_usrsBtnsPanel.columnWeights = new double[]{0.50,0.50};
			gbl_usrsBtnsPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
			usrsBtnsPanel.setLayout(gbl_usrsBtnsPanel);
			
			usersOnlineBtn = new RoundedButton();
			usersOnlineBtn.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
			usersOnlineBtn.lblboldtext.setForeground(Color.WHITE);
			usersOnlineBtn.setName("USER_ONLINE_BTN");
			usersOnlineBtn.addMouseListener(this);
			usersOnlineBtn.setForeground(Color.WHITE);
			usersOnlineBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			usersOnlineBtn.setBorder(emtyBordet);
			usersOnlineBtn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			GridBagConstraints gbc_usersOnlineBtn = new GridBagConstraints();
			gbc_usersOnlineBtn.fill = GridBagConstraints.BOTH;
			gbc_usersOnlineBtn.insets = new Insets(0, 0, 0, 5);
			gbc_usersOnlineBtn.gridx = 0;
			gbc_usersOnlineBtn.gridy = 0;
			usrsBtnsPanel.add(usersOnlineBtn, gbc_usersOnlineBtn);
			
			returningBtn = new RoundedButton();
			returningBtn.setName("RETURNING_BTN");
			returningBtn.addMouseListener(this);
			returningBtn.lblboldtext.setForeground(Color.WHITE);
			returningBtn.lblnormaltext.setForeground(Constants.COLOR_GREY_LABEL);
			returningBtn.setForeground(Color.WHITE);
			returningBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			returningBtn.setBorder(emtyBordet);
			returningBtn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			String returningVal = ""; 
			String returningBtnString = "<html>"
					+ "<body>"
					+ "<table><tr><td align = \"center\">"
					+ "<font size = \"5\"><b>"+ returningVal +"</b></font><br>"
					+ "<font size = \"2\">Returning</font>"
					+ "</td></tr></table></body></html>";
			//returningBtn.setText(returningBtnString);
			returningBtn.lblboldtext.setText(""+ returningVal);
			returningBtn.lblnormaltext.setText("Returning");
			
			GridBagConstraints gbc_returningBtn = new GridBagConstraints();
			gbc_returningBtn.fill = GridBagConstraints.BOTH;
			gbc_returningBtn.gridx = 1;
			gbc_returningBtn.gridy = 0;
			usrsBtnsPanel.add(returningBtn, gbc_returningBtn);
			String taggedVal = ""; 
			String taggedBtnString = "<html>"
					+ "<body>"
					+ "<table><tr><td align = \"center\">"
					+ "<font size = \"5\"><b>"+ taggedVal +"</b></font><br>"
					+ "<font size = \"2\">Tagged</font>"
					+ "</td></tr></table></body></html>";
			
			heading = new JTable();
			heading.setEnabled(false);
			heading.setBorder(null);
//			GridBagConstraints gbc_heading = new GridBagConstraints();
//			gbc_heading.gridheight = 3;
//			gbc_heading.insets = new Insets(0, 0, 5, 0);
//			gbc_heading.fill = GridBagConstraints.BOTH;
//			gbc_heading.gridx = 0;
//			gbc_heading.gridy = 1;
//			heading.setPreferredSize(new Dimension(400,100));
//		//	users.add(heading, gbc_heading);
//			heading.setLayout(new BoxLayout(heading, BoxLayout.PAGE_AXIS));
			
			usersList = new JScrollPane();
			usersList.setBorder(BorderFactory.createEmptyBorder());
			usersList.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			GridBagConstraints gbc_usersList = new GridBagConstraints();
			gbc_usersList.gridheight = 7;
			gbc_usersList.fill = GridBagConstraints.BOTH;
			gbc_usersList.gridx = 0;
			gbc_usersList.insets = new Insets(0,0,0,0);
			gbc_usersList.gridy = 1;
			users.add(usersList, gbc_usersList);
		
		
			TileFactoryInfo info = new OSMTileFactoryInfo();
			DefaultTileFactory tileFactory = new DefaultTileFactory(info);
			tileFactory.setThreadPoolSize(8);
			
			// Setup JXMapViewer
			mapViewer = new JXMapViewer();
			mapViewer.setTileFactory(tileFactory);
			
			// Set the focus
			GeoPosition centerPoint = new GeoPosition(37.099,-95.71);
			mapViewer.setZoom(17);
			mapViewer.setAddressLocation(centerPoint);
			
			
			this.panelForMap.add(mapViewer);
			
			//create similar map for popup 
			
			mapViewerInPopup = new JXMapViewer();
			mapViewerInPopup.setTileFactory(tileFactory);
			
			// Set the focus
			
			mapViewerInPopup.setZoom(17);
			mapViewerInPopup.setAddressLocation(centerPoint);
			
			
			//set default backgrounds.
			 btnActive.setBackground(new Color(76 ,72 ,72));
		     btnAck.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
		     //btnAck.setBackground(Color.DARK_GRAY);
		     
		     pieChartPanel = new ChartPanel(null);
		     GridBagConstraints gbc_pieChartPanel = new GridBagConstraints();
             gbc_pieChartPanel.fill = GridBagConstraints.BOTH;
             gbc_pieChartPanel.insets = new Insets(0, 0, 0, 5);
             gbc_pieChartPanel.gridx = 1;
             gbc_pieChartPanel.gridy = 0;
             graph.add(pieChartPanel, gbc_pieChartPanel);
             
             browserChartPanel = new ChartPanel(null);
	            GridBagConstraints gbc_browserChartPanel = new GridBagConstraints();
	            gbc_browserChartPanel.fill = GridBagConstraints.BOTH;
	            gbc_browserChartPanel.gridx = 3;
	            gbc_browserChartPanel.gridy = 0;
	            graph.add(browserChartPanel, gbc_browserChartPanel);
           

	            
	            //create icons once
	            ImageIcon symbolMobile = new ImageIcon(getClass().getResource("Mobile-White.png"));
	    	 	Image symbolNewMobile = symbolMobile.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    	 	symbolForMobile = new ImageIcon(symbolNewMobile);
	    	        
	    	 	ImageIcon symbolPC = new ImageIcon(getClass().getResource("Windows_Icon.png"));
	    		Image symbolNewPC = symbolPC.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    		symbolForPC= new ImageIcon(symbolNewPC);
	    	         
	    		ImageIcon symbolMac = new ImageIcon(getClass().getResource("Mac_Icon.png"));
	    	    Image symbolNewMac = symbolMac.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    	    symbolForMac = new ImageIcon(symbolNewMac);
	    	    	        
	    	    ImageIcon symbolLinux = new ImageIcon(getClass().getResource("Linux_Icon.png"));
	    	    Image symbolNewLinux = symbolLinux.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    	    symbolForLinux = new ImageIcon(symbolNewLinux);
        
	    	    
	    	    
	    	  //create icons for displaying browser type in the pie chart
	    		ImageIcon browser1 = new ImageIcon(getClass().getResource("Internet_Explorer_Icon.png"));
	    		Image browserImage = browser1.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    		newBrowser = new ImageIcon(browserImage);
	    		
	    		ImageIcon browser2 = new ImageIcon(getClass().getResource("Chrome_Icon.png"));
	    		Image browserImage2 = browser2.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    		newBrowser2 = new ImageIcon(browserImage2);
	    				 		
	    		ImageIcon operaIcon = new ImageIcon(getClass().getResource("Opera_Icon.png"));
	    		Image operaImage = operaIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    		newOpera = new ImageIcon(operaImage);
	    			         
	    		ImageIcon safariIcon = new ImageIcon(getClass().getResource("Safari_Icon.png"));
	    		Image safariImage = safariIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    		newSafari = new ImageIcon(safariImage);
	    				 		
	    		ImageIcon mozillaIcon = new ImageIcon(getClass().getResource("Firefox_Icon.png"));
	    		Image mozillaImage = mozillaIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	    		newMozilla = new ImageIcon(mozillaImage);
	    			        
	    	    
	    	    
	    	    populateData(Constants.TODAY);
			
			
	}
	
	public void populateData(int duration)
	{
		//screensPerUser = null;

		
		//Code for Time panel (Populate Line Graph) by Omkar
				
		lineGraphPrint();

				
				
				
				//Added by YM : 25-04-2016 to Populate 7 Day max min values in traffic panel to consider 0 logged in users case on a day
				
				Dataset maxMin = rpc.getSevenDaysMaxMinForController(this.dataSource, Constants.LAST_SEVEN_DAYS, this.currentProject,this.allProjects,this.currentGateway,this.allGateways);
				int i = 0, noOfDays = 0;
				
				int minVal = 0; int maxVal = 0, curVal = 0;
				
				if(maxMin != null)
				{
					noOfDays = maxMin.getRowCount();
					
					//get min and max values
					for(i=0; i<noOfDays; i++)
					{
						curVal = (int)Float.parseFloat(maxMin.getValueAt(i, 1).toString());
						if(curVal > maxVal)
						{
							maxVal = curVal;
						}
						
						if(curVal < minVal)
						{
							minVal = curVal;
						}
					}
				}
				
				if(noOfDays < 7)
				{
					minVal = 0;
				}
				dayMaxLbl.setText("" + maxVal);
				dayMinValLbl.setText("" + minVal);
				
				
				
				
				
		JTable engagementTable = new JTable(); //hold engagement section information
		
		
		
		int noOfRecords = 0;
		int totalScreens = 0;
		
		//retrieve the current overview information
		//CurrentOverview rInfo = rpc.getCurrentOverview( currentProject, true);
		//CurrentOverview rInfo = rpc.getOverViewFromRemoteServer( );
//		if(rInfo != null)
//		{
			
//			screensPerUser = rInfo.getScreenViewscountPerUser();
//			if(screensPerUser != null)
//			{
//				noOfRecords = screensPerUser.size();
//				for( i=0; i<noOfRecords; i++)
//				{
//					totalScreens = totalScreens + screensPerUser.get(i).getNoOfViews();	
//				}
//			}
			
			
		overViewList = new ArrayList<CurrentOverview>();
//			List<CurrentOverview> overViewList = new ArrayList<CurrentOverview>();
		List<UserLocationsPerGateway> usersAllGateways = new ArrayList<UserLocationsPerGateway>();
		
		if(this.allGateways == true){
			   String[] _gateways = rpc.getGateways();
			   for (int i1 = 1 ; i1 < _gateways.length ; i1++){
			    ////if(i1 !=null){
			    try{
			    	System.out.println("b4 call to getRealTimeAllGateWayOverview : gname : " + _gateways[i1] + ", projectname : " + currentProject + " , allProjects : " + this.allProjects );
			    CurrentOverview gateWayOverView = rpc.getRealTimeAllGateWayOverview(_gateways[i1],currentProject,this.allProjects);
			    overViewList.add(gateWayOverView);
			    UserLocationsPerGateway _u = new UserLocationsPerGateway();
			    
			    if(gateWayOverView.getUserLocations() != null)
			    {
			    	_u.setGatewayID(_gateways [i1]);
			    	_u.setUserLocations(gateWayOverView.getUserLocations());
			    	usersAllGateways.add(_u);
			    }
				
			    //}
			    }
			    catch(Exception e){
			     
			     e.printStackTrace();
			    // JOptionPane.showMessageDialog(this, "Can not connect to requested gateway at this time , please try adding project later");
			     
			     
			    }
			   }
			  }
			  else{
			   try{
			    CurrentOverview gateWayOverView = rpc.getRealTimeAllGateWayOverview(this.currentGateway,this.currentProject,this.allProjects);
			    overViewList.add(gateWayOverView);
			    UserLocationsPerGateway _u = new UserLocationsPerGateway();
			    
			    if(gateWayOverView.getUserLocations() != null)
			    {
			    	_u.setGatewayID(this.currentGateway);
			    	_u.setUserLocations(gateWayOverView.getUserLocations());
			    	usersAllGateways.add(_u);
			    }
			    //}
			    }
			    catch(Exception e){
			     
			     e.printStackTrace();
			     JOptionPane.showMessageDialog(this, "Can not connect to requested gateway at this time , please try adding project later");
			     
			     
			    }
			  }
			int sessions = 0;
			int totalGatewayActiveUsers = 0;
			int noOfActionsByCurrentUsers = 0;
			long totalActiveSessionsLength = 0L;
			HashMap<String,Integer> allBrowsers = new HashMap<String,Integer>();
			HashMap<String,Integer> allLocations = new HashMap<String,Integer>();
			HashMap<String,Integer> allOpSystems = new HashMap<String,Integer>();
			DevicesInformation allDevices = new DevicesInformation();
			
			for (int i1 = 0 ; i1 < overViewList.size() ; i1++ ){
				CurrentOverview itrOverView = overViewList.get(i1);
				if(itrOverView != null)
				{
					if(itrOverView.getNoOfActiveUsers() != 0){
						totalGatewayActiveUsers = totalGatewayActiveUsers + itrOverView.getNoOfActiveUsers();
					}
					
					
					else{
						System.out.println("All Gateways");
					}
					
					if(itrOverView.getScreenViewscountPerUser() != null)
					{
						System.out.println("itrOverView.getScreenViewscountPerUser()  not null");
						noOfRecords = itrOverView.getScreenViewscountPerUser().size();
						System.out.println("noOfRecords = " + noOfRecords);
						screensPerUser.addAll(itrOverView.getScreenViewscountPerUser());
						for( i=0; i<noOfRecords; i++)
						{
							totalScreens = totalScreens + itrOverView.getScreenViewscountPerUser().get(i).getNoOfViews();	
						}
						System.out.println("totalScreens = " + totalScreens);
					}
					else
					{
						System.out.println("itrOverView.getScreenViewscountPerUser()  is null");
					}
					
					sessions = sessions +itrOverView.getNoOfActiveSessions();
					noOfActionsByCurrentUsers = noOfActionsByCurrentUsers + itrOverView.getNoOfActionsByCurrentUsers();
					totalActiveSessionsLength = totalActiveSessionsLength + itrOverView.getActiveSessionLength();
					HashMap<String, Integer> _browsers;
					HashMap<String, Integer> _locs;
					DevicesInformation _devices;
					HashMap<String, Integer> _os;
					Iterator<HashMap.Entry<String,Integer>> _itr;
					HashMap.Entry<String,Integer> _itrRec;
					int valVal;
					String keyVal;
					if(itrOverView.getLocationDeviceBrowsers() != null)
					{
						_browsers = itrOverView.getLocationDeviceBrowsers().getBrowsers();
						_locs = itrOverView.getLocationDeviceBrowsers().getLocations();
						_devices = itrOverView.getLocationDeviceBrowsers().getDevices();
						_os = itrOverView.getLocationDeviceBrowsers().getOperatingSystems();
						if(_browsers != null)
						{
							_itr = _browsers.entrySet().iterator();
							
							while(_itr.hasNext())
							{
								_itrRec = _itr.next();
								keyVal = _itrRec.getKey();
								if(allBrowsers.containsKey(keyVal))
								{
									valVal = allBrowsers.get(keyVal);
									valVal = valVal + _itrRec.getValue();
									allBrowsers.put(keyVal, valVal);
								}
								else
								{
									allBrowsers.put(keyVal, _itrRec.getValue());
								}
							}
						}
						if(_locs != null)
						{
							_itr = _locs.entrySet().iterator();
							
							while(_itr.hasNext())
							{
								_itrRec = _itr.next();
								keyVal = _itrRec.getKey();
								if(allLocations.containsKey(keyVal))
								{
									valVal = allLocations.get(keyVal);
									valVal = valVal + _itrRec.getValue();
									allLocations.put(keyVal, valVal);
								}
								else
								{
									allLocations.put(keyVal, _itrRec.getValue());
								}
							}
						}
						if(_devices != null)
						{
							allDevices.setNoOfClientsOnDesktop(allDevices.getNoOfClientsOnDesktop() + _devices.getNoOfClientsOnDesktop());
							allDevices.setNoOfClientsOnMobile(allDevices.getNoOfClientsOnMobile() + _devices.getNoOfClientsOnMobile());
						}
						if(_os != null)
						{
							_itr = _os.entrySet().iterator();
							
							while(_itr.hasNext())
							{
								_itrRec = _itr.next();
								keyVal = _itrRec.getKey();
								if(allOpSystems.containsKey(keyVal))
								{
									valVal = allOpSystems.get(keyVal);
									valVal = valVal + _itrRec.getValue();
									allOpSystems.put(keyVal, valVal);
								}
								else
								{
									allOpSystems.put(keyVal, _itrRec.getValue());
								}
							}
						}
					}
				}
			}
			
			txtScreenViews.setText("" +totalScreens );
			int screenDepthVal = 0;
			if(sessions > 0 )
			{
				screenDepthVal = totalScreens/ sessions;
			}
			
			trafficPanel.setPreferredSize(new Dimension(200,200));
			//trafficPanel.usersOnline.setValue(rInfo.getNoOfActiveUsers());
			trafficPanel.usersOnline.setValue(totalGatewayActiveUsers);
			
			// Alarm Panel Active User Btn Data
			
			txtSessions.setText(""+ sessions);
			txtActions.setText(""+ (noOfActionsByCurrentUsers ));
			
			float actionsPerSessionVal = 0;
			if(totalGatewayActiveUsers > 0)
			{
				actionsPerSessionVal = ((noOfActionsByCurrentUsers ) / totalGatewayActiveUsers);
			}
			
						
			txtActionSession.setText("" +  actionsPerSessionVal);
		
			//cacluate from total sessions length and not from database query
			int avgSessionTime = 0;
			if(sessions > 0)
			{
				avgSessionTime =	(int) totalActiveSessionsLength / sessions;
			}
			int minutesVal = 0;
			DecimalFormat dFormat = new DecimalFormat("00");
			
			if(avgSessionTime == 0)
			{
				txtAvgSession.setText("00:00");
			}
			else if(avgSessionTime < 60)
			{
				txtAvgSession.setText("00:" + dFormat.format(avgSessionTime));
			}
			else
			{
				minutesVal = avgSessionTime / 60;
				avgSessionTime = avgSessionTime % 60;
				
				txtAvgSession.setText( dFormat.format(minutesVal) + ":" +  dFormat.format(avgSessionTime));
			}

			//calculate avg time per screen , no of screens / total session time
			int avgTimePerScreen = 0;
			
			
			if(totalActiveSessionsLength != 0)
			{
				if(totalScreens != 0)
				{
					avgTimePerScreen= (int) totalActiveSessionsLength / totalScreens;
				}
				else
				{
					avgTimePerScreen = 0;
				}
			}
			
			if(avgTimePerScreen == 0)
			{
				txtAvgTimeScreen.setText("00:00");
			}
			else if(avgTimePerScreen < 60)
			{
				txtAvgTimeScreen.setText("00:" + dFormat.format(avgTimePerScreen));
			}
			else
			{
				minutesVal = avgTimePerScreen / 60;
				avgTimePerScreen = avgTimePerScreen % 60;
				
				txtAvgTimeScreen.setText( dFormat.format(minutesVal) + ":" +  dFormat.format(avgTimePerScreen));
			}
		
			 //populate engagement section
			if(avgScreenDepthStatus){
				avgDepthOfScreen.setBackground(new Color(76 ,72 ,72));
				activeUsersBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			populateScreenDepthTableController(overViewList);
			}else if(activeUsersStatus){
				avgDepthOfScreen.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				activeUsersBtn.setBackground(new Color(76 ,72 ,72));
				populateActiveUsersTableOnController();
			}
			
			//engagement Buttons
			//Avg Session Button
			
			
			//Active User Percentage
			
			//List<String> test11 = rpc.getAllUsers();
		
			//call changed by Yogini on 30-dec-2016 to match the original function
			String activeUsersEngVal = "" + rpc.getNumberOfActiveUsersOnController(Constants.TODAY, currentGateway, currentProject, allGateways, allProjects);
			//activeUsersBtn.setText(activeUserText);
			activeUsersBtn.lblboldtext.setText("" + activeUsersEngVal);
			activeUsersBtn.lblnormaltext.setText("Active Users");
			
			
			avgDepthOfScreen.lblboldtext.setText("" + screenDepthVal);
	 		avgDepthOfScreen.lblnormaltext.setText("Avg. Depth of Screen");
			
	 		//avgSessionTimeButton.setBackground(new Color(76 ,72 ,72));
//	 		avgDepthOfScreen.setBackground(new Color(76 ,72 ,72));
//			activeUsersBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
//			
			//Populating Users Panel 
			int userOnlineVal = totalGatewayActiveUsers; 
			int noOfNewUsers = rpc.getNumberOfNewUsersOnController(Constants.TODAY, currentProject, allProjects,currentGateway,allGateways);
			String userBtnString = "<html>"
					+ "<body>"
					+ "<table><tr><td align = \"center\">"
					+ "<font size = \"5\"><b>"+ totalGatewayActiveUsers +"</b></font><br>"
					+ "<font size = \"2\">User Online</font>"
					+ "</td></tr></table></body></html>";
			
			//usersOnlineBtn.setText(userBtnString);
			usersOnlineBtn.lblboldtext.setText(""+userOnlineVal);
			usersOnlineBtn.lblnormaltext.setText("User Online");
			
			
			returningBtn.lblnormaltext.setText("Returning");
			
			System.out.println("noOfUsers b4: " + noOfUsers);
			System.out.println("userOnlineVal b4: " + userOnlineVal);
			addMapAndNoOfVisitsToUI(usersAllGateways);
			
			System.out.println("noOfUsers after: " + noOfUsers);
			System.out.println("userOnlineVal after: " + userOnlineVal);
			returningBtn.lblboldtext.setText(""+ (userOnlineVal - noOfUsers));
			
			//Populate location Table
			JTable _locationTable = new JTable();
			_locationTable.setBorder(null);
			DefaultTableModel _locationTableModel = new DefaultTableModel();
			_locationTableModel.setColumnIdentifiers(new Object[] {"Location", "No of users"});

			//retrive and populate location data from hash map
			HashMap.Entry<String,Integer> locRec;
			String locationName = "";
			int locationVal = 0;
			Iterator<HashMap.Entry<String,Integer>> itrLocations = allLocations.entrySet().iterator();
			while(itrLocations.hasNext())
			{
				locRec = itrLocations.next();
				locationName = locRec.getKey();
				locationName = locationName.trim();
				
				while(locationName.startsWith(","))
				{
					locationName = locationName.substring(1);
				}
				if (locationName.endsWith(",")) {
					locationName = locationName.replaceAll(",", "");
				}
				if(locationName.contains("Unknown"))
				{
					locationName = "Unknown";
				}
				locationName = locationName.replace(",null,", ",");
				
				locationVal = locRec.getValue();
				_locationTableModel.addRow(new Object[]{locationName,locationVal});
			}
			
			
			//right align the second column
			
			RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
			rightRenderer.paddingSize = 20;
			
			
			_locationTable.setModel(_locationTableModel);
			_locationTable.setFillsViewportHeight(true);
			_locationTable.setTableHeader(null);
			_locationTable.setPreferredScrollableViewportSize(new Dimension(300,300));
			_locationTable.setShowVerticalLines(false);
			_locationTable.setRowHeight(32);
			_locationTable.setGridColor(Constants.COLOR_MAIN_BACKGROUND);
			_locationTable.setIntercellSpacing(new Dimension(0,0));
			_locationTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
			_locationTable.getColumnModel().getColumn(0).setCellRenderer(this.leftRenderer);
			_locationTable.getColumnModel().getColumn(0).setWidth(300);
			_locationTable.setEnabled(false);
			//scrollPane.setRowHeaderView(_locationTable);
			scrollPane.setViewportView(_locationTable);
			scrollPane.setPreferredSize(new Dimension(200,100));
		//}
		//populate content section
		
		JTable _screensTable = new JTable();
		RightPaddedTableCellRenderer contentCellRenderer = new RightPaddedTableCellRenderer();
		contentCellRenderer.paddingSize = 45;
		
		 
		 _screensTable.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
		 HashMap<String,HashMap<String,Integer>>  allGatewayContent = new HashMap<String,HashMap<String,Integer>>();
			 allGatewayContent = rpc.getNumberOfUsersPerScreenRealTimeOnController(currentGateway, allGateways, currentProject, allProjects);
		// HashMap<String,Integer> contentData = rpc.getNumberOfUsersPerScreenRealTime( currentProject, allProjects);
		if(allGatewayContent != null)
		{
			Iterator<HashMap.Entry<String,HashMap<String,Integer>>> itrAllContent = allGatewayContent.entrySet().iterator();
		
			HashMap<String,Integer> contentData;
			DefaultTableModel _screensModel = new DefaultTableModel(0,2);
			while(itrAllContent.hasNext())
			{
			HashMap.Entry<String,HashMap<String,Integer>> allContentRec = itrAllContent.next();
			contentData = allContentRec.getValue();
			if(contentData != null ){
			Iterator<HashMap.Entry<String,Integer>> itr = contentData.entrySet().iterator();
			while(itr.hasNext())
			{
				HashMap.Entry<String,Integer> screenRec = itr.next();
		
			_screensModel.addRow(new Object[]{screenRec.getKey(),screenRec.getValue()}); 
			
			}
		
			}
		}
		_screensModel.setColumnIdentifiers(new Object[] {"Screen Name", "No of views"});
		_screensTable.setModel(_screensModel);
		_screensTable.setFillsViewportHeight(true);
		_screensTable.setTableHeader(null);
		_screensTable.setPreferredScrollableViewportSize(new Dimension(300,300));
		_screensTable.setShowVerticalLines(false);
		_screensTable.setRowHeight(32);
		_screensTable.setGridColor(Constants.COLOR_MAIN_BACKGROUND);
		_screensTable.setIntercellSpacing(new Dimension(0,0));
		//right align the second column
		
		_screensTable.getColumnModel().getColumn(1).setCellRenderer(contentCellRenderer);
		_screensTable.getColumnModel().getColumn(0).setCellRenderer(this.leftRenderer);
		_screensTable.setEnabled(false);
		this.content.setViewportView(_screensTable);
		}
		//retrieve alarms information
		
		
		
		
		//based on the no of alarms set no of segments to draw
		
		
		//Buttons from alarms sub-panel
		alarmInfo = rpc.getAlarmsOverviewOnController(duration, currentProject, allProjects,currentGateway,allGateways);
		int ackAlarmVal = alarmInfo.getNoOfAckAlarms();
		int noOFActiveAlarms = alarmInfo.getNoOfActiveAlarms();
				
				
				btnAck.lblboldtext.setText(""+ ackAlarmVal);
				btnAck.lblnormaltext.setText("Acknowleged");
				
				
				btnActive.lblboldtext.setText( "" + noOFActiveAlarms);
				btnActive.lblnormaltext.setText("Active");
				if(alrmsActive){
			HashMap<String, Integer> activeAlarms = alarmInfo.getActiveAlarmsCount();
			txtHighPriority.setText("");
			txtCriticalPriority.setText("");
			txtLowPriority.setText("");
			txtMediumPriority.setText("");
			if(activeAlarms != null){
				if(activeAlarms.get("High") != null)
				{
					txtHighPriority.setText("" + activeAlarms.get("High"));
				}
				else
				{
					txtHighPriority.setText("");
				}
				if(activeAlarms.get("Critical") != null)
				{
					txtCriticalPriority.setText("" + activeAlarms.get("Critical"));
				}
				else
				{
					txtCriticalPriority.setText("");
				}
				if(activeAlarms.get("Low") != null)
				{
					txtLowPriority.setText("" + activeAlarms.get("Low"));
				}
				else
				{
					txtLowPriority.setText("");
				}
				if(activeAlarms.get("Medium") != null)
				{
					txtMediumPriority.setText("" + activeAlarms.get("Medium"));
				}
				else
				{
					txtMediumPriority.setText("");
				}
			}
			int q, r;
			value.setStringText("Active Alarms");
			value.setNoOfAlarms(noOFActiveAlarms);
			if(noOFActiveAlarms <= 100)
			{
				value.setNumberOfSegments(noOFActiveAlarms);
			}
			
			else if(noOFActiveAlarms > 100 && noOFActiveAlarms <= 1000)
			{
				q = noOFActiveAlarms / 10;
				if(noOFActiveAlarms % 10 > 0)
				{
					q = q + 1;
				}
			
				value.setNumberOfSegments(q);
			}
			else if(noOFActiveAlarms > 1000 && noOFActiveAlarms <= 10000)
			{
				q = noOFActiveAlarms / 50;
				if(noOFActiveAlarms % 50 > 0)
				{
					q = q + 1;
				}
			
				value.setNumberOfSegments(q);
			}
			else if(noOFActiveAlarms > 10000 )
			{
				
				q = noOFActiveAlarms / 100;
				if(noOFActiveAlarms % 100 > 0)
				{
					q = q + 1;
				}
			
				value.setNumberOfSegments(q);
			}
			btnActive.setBackground(new Color(76 ,72 ,72));
			btnAck.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				}
				else if(alrmsAck)
				{
					btnAck.setBackground(new Color(76 ,72 ,72));
					btnActive.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
					
					int noOFAckAlarms = alarmInfo.getNoOfAckAlarms();
					int q, r;
					txtHighPriority.setText("" );
					txtCriticalPriority.setText("" );
					txtLowPriority.setText("" );
					txtMediumPriority.setText("" );
					if(alarmInfo.getAckAlarmsCount().get("High") != null)
					{
						txtHighPriority.setText("" + alarmInfo.getAckAlarmsCount().get("High"));
					}
					if(alarmInfo.getAckAlarmsCount().get("Critical") != null)
					{
						txtCriticalPriority.setText("" + alarmInfo.getAckAlarmsCount().get("Critical"));
					}
					if(alarmInfo.getAckAlarmsCount().get("Low") != null)
					{
						txtLowPriority.setText("" + alarmInfo.getAckAlarmsCount().get("Low"));
					}
					if(alarmInfo.getAckAlarmsCount().get("Medium") != null)
					{
						txtMediumPriority.setText("" + alarmInfo.getAckAlarmsCount().get("Medium"));
					}
					
					value.setNoOfAlarms(noOFAckAlarms);
					value.setStringText("Ack Alarms");
					if(noOFAckAlarms <= 100)
					{
						value.setNumberOfSegments(noOFAckAlarms);
					}
					else if(noOFAckAlarms > 100 && noOFAckAlarms <= 1000)
					{
						q = noOFAckAlarms / 10;
						if(noOFAckAlarms % 10 > 0)
						{
							q = q + 1;
						}
					
						value.setNumberOfSegments(q);
					}
					else if(noOFAckAlarms > 1000 && noOFAckAlarms <= 10000)
					{
						q = noOFAckAlarms / 50;
						if(noOFAckAlarms % 50 > 0)
						{
							q = q + 1;
						}
					
						value.setNumberOfSegments(q);
					}
					else if(noOFAckAlarms > 10000)
					{
						q = noOFAckAlarms / 100;
						if(noOFAckAlarms % 100 > 0)
						{
							q = q + 1;
						}
					
						value.setNumberOfSegments(q);
					}
				}
		
		
		if(alarmInfo.getAvgAckTime() != null && alarmInfo.getAvgAckTime().length() > 0)
		{
			btnTimeToAck.lblboldtext.setText("" + alarmInfo.getAvgAckTime());
		}
		
		btnTimeToAck.lblnormaltext.setText("Avg.Ack Time");
		
		if(alarmInfo.getAvgClearTime() != null && alarmInfo.getAvgClearTime().length() > 0)
		{
		
			btnTimetoClear.lblboldtext.setText("" + alarmInfo.getAvgClearTime());
		}
		btnTimetoClear.lblnormaltext.setText("Avg Time Clear");

	
		
//hide all symbol labels in device chart.
		
		darkBlueLbl.setIcon(null);
		darkerBlueLbl.setIcon(null);
		lightBlueLbl.setIcon(null);
		lighterBlueLbl.setIcon(null);
		
		darkOrangeLbl.setIcon(null);
		darkerOrnageLbl.setIcon(null);
		lightOrangeLbl.setIcon(null);
		lighterOrangeLbl.setIcon(null);
		
		browser2IconLbl.setIcon(null);
        browser1IconLbl.setIcon(null);
        browser3IconLbl.setIcon(null);
        browser4IconLbl.setIcon(null);
        
        device1IconLbl.setIcon(null);
        device2IconLbl.setIcon(null);
        device3IconLbl.setIcon(null);
        device4IconLbl.setIcon(null);
		
	//	DevicesInformation dInfo = rpc.getDeviceInformation( duration, currentProject, allProjects);
        
        	button.lblBoldText.setText(""+ allDevices.getNoOfClientsOnDesktop());
        	button_1.lblBoldText.setText(""+ allDevices.getNoOfClientsOnMobile());
		
		
		
		//create OS icons
		
	    
	    
	    //retrieve the os infor and sort to diplay top 4 os if exists
	    ArrayList<ContentsData> opSystems = new ArrayList<ContentsData>();
		ContentsData _tempOSData;
		HashMap.Entry<String,Integer> osRec;
		Iterator<HashMap.Entry<String,Integer>> itrOS = allOpSystems.entrySet().iterator();
				 		 
		while(itrOS.hasNext())
		{
			osRec = itrOS.next();
			_tempOSData = new ContentsData();
			_tempOSData.setScreenName(osRec.getKey());
			_tempOSData.setUserCount(osRec.getValue());
			
			opSystems.add(_tempOSData);
		}
				 		
		java.util.Collections.sort(opSystems, new Comparator<ContentsData>(){
				@Override
				public int compare(ContentsData arg0,
								ContentsData arg1) {
								if(arg0.getUserCount() > arg1.getUserCount())
								{
									return 1;
								}
								else
								{
									return 0;	
								}
								}
				 		});
		
		int noOfOSRecs = 0;
		noOfOSRecs = opSystems.size();
		//Add a pie chart showing operating systems distribution
		DefaultPieDataset dataset = new DefaultPieDataset();
		
		if(noOfOSRecs > 0)
    	{
    		dataset.setValue(opSystems.get(0).getScreenName(), opSystems.get(0).getUserCount());
    	}
    	if(noOfOSRecs >= 2 )
    	{
    		dataset.setValue(opSystems.get(1).getScreenName(), opSystems.get(1).getUserCount());
    	}
    	if(noOfOSRecs >= 3 )
    		
    	{
    		dataset.setValue(opSystems.get(2).getScreenName(), opSystems.get(2).getUserCount());
    	}
    	if(noOfOSRecs >= 4 )
    	{
    		dataset.setValue(opSystems.get(3).getScreenName(), opSystems.get(3).getUserCount());
    	}
		      
		JFreeChart _pieChart = ChartFactory.createPieChart(
		                "",  // chart title
		                dataset,             // data
		                false,               // include legend
		                true,
		                false
		            );
		_pieChart.removeLegend();
		_pieChart.setBorderVisible(false);
		PiePlot _piePlot = (PiePlot) _pieChart.getPlot();
		_piePlot.setLabelGenerator(null);
		_piePlot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		_piePlot.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		_piePlot.setNoDataMessage("No data available");
		_piePlot.setCircular(true);
		_piePlot.setLabelGap(0.02);
		_piePlot.setOutlineVisible(false);
		_piePlot.setShadowXOffset(0.0);
		_piePlot.setShadowYOffset(0.0);
		_piePlot.setBaseSectionOutlinePaint(Color.WHITE);
		String osName = "";       
		if( noOfOSRecs > 0)
        {
			osName = opSystems.get(0).getScreenName();
        	_piePlot.setSectionPaint(osName, new Color(0,96,191));
        	darkBlueLbl.setIcon(newDarkBlue);
        	if(osName.toLowerCase().contains("windows")   )
        		device1IconLbl.setIcon(symbolForPC);
        	else if(osName.toLowerCase().contains("mobile"))
        		device1IconLbl.setIcon(symbolForMobile);
        	else if(osName.toLowerCase().contains("mac"))
        		device1IconLbl.setIcon(symbolForMac);
        	else if(osName.toLowerCase().contains("linux") )
        		device1IconLbl.setIcon(symbolForLinux);
        }
        if(noOfOSRecs >= 2)
        {
        	osName = opSystems.get(1).getScreenName();
        	_piePlot.setSectionPaint(osName, new Color(138,197,255));
        	lightBlueLbl.setIcon(newlightBlue);
        	if(osName.toLowerCase().contains("windows")   )
        		device2IconLbl.setIcon(symbolForPC);
        	else if(osName.toLowerCase().contains("mobile"))
        		device2IconLbl.setIcon(symbolForMobile);
        	else if(osName.toLowerCase().contains("mac"))
        		device2IconLbl.setIcon(symbolForMac);
        	else if(osName.toLowerCase().contains("linux") )
        		device2IconLbl.setIcon(symbolForLinux);
        }
        if( noOfOSRecs >= 3)
        {
        	osName = opSystems.get(2).getScreenName();
        	_piePlot.setSectionPaint(osName, Color.BLUE);
        	darkerBlueLbl.setIcon(newDarkerBlue);
        	if(osName.toLowerCase().contains("windows")   )
        		device3IconLbl.setIcon(symbolForPC);
        	else if(osName.toLowerCase().contains("mobile"))
        		device3IconLbl.setIcon(symbolForMobile);
        	else if(osName.toLowerCase().contains("mac"))
        		device3IconLbl.setIcon(symbolForMac);
        	else if(osName.toLowerCase().contains("linux") )
        		device3IconLbl.setIcon(symbolForLinux);
        }
        if( noOfOSRecs >= 4)
        {
        	osName = opSystems.get(3).getScreenName();
        	_piePlot.setSectionPaint(osName.toString(), new Color(215, 215, 255));
        	lighterBlueLbl.setIcon(newlighterBlue);
        	if(osName.toLowerCase().contains("windows")   )
        		device4IconLbl.setIcon(symbolForPC);
        	else if(osName.toLowerCase().contains("mobile"))
        		device4IconLbl.setIcon(symbolForMobile);
        	else if(osName.toLowerCase().contains("mac"))
        		device4IconLbl.setIcon(symbolForMac);
        	else if(osName.toLowerCase().contains("linux") )
        		device4IconLbl.setIcon(symbolForLinux);
        }        
		
		pieChartPanel.setChart(_pieChart);
		pieChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		pieChartPanel.setPreferredSize(new Dimension(150, 150));
		pieChartPanel.setBorder(BorderFactory.createEmptyBorder());
		            
		
		//retrieve the top 4 browser values from the Hashmap ( Hashmap is not sorted)
		ArrayList<ContentsData> browsers = new ArrayList<ContentsData>();
		ContentsData _tempData;
		HashMap.Entry<String,Integer> browserRec;
		Iterator<HashMap.Entry<String,Integer>> itrBrowsers = allBrowsers.entrySet().iterator();
				 		 
		while(itrBrowsers.hasNext())
		{
			browserRec = itrBrowsers.next();
			_tempData = new ContentsData();
			_tempData.setScreenName(browserRec.getKey());
			_tempData.setUserCount(browserRec.getValue());
			
			browsers.add(_tempData);
		}
				 		
		java.util.Collections.sort(browsers, new Comparator<ContentsData>(){
				@Override
				public int compare(ContentsData arg0,
								ContentsData arg1) {
								if(arg0.getUserCount() > arg1.getUserCount())
								{
									return 1;
								}
								else
								{
									return 0;	
								}
								}
				 		});

				 		
			      
		DefaultPieDataset browserPlot = new DefaultPieDataset();
			        
			        
		ContentsData _tempContent;
		int noOfBrowsers = browsers.size();
		if(noOfBrowsers > 0)
		{
        	_tempContent = browsers.get(0);
			browserPlot.setValue(_tempContent.getScreenName(), _tempContent.getUserCount());
			darkOrangeLbl.setIcon(newDarkOrange);
			if(_tempContent.getScreenName().compareToIgnoreCase("IE") == 0 ||_tempContent.getScreenName().toLowerCase().contains("explorer") )
				browser1IconLbl.setIcon(newBrowser);
			else if(_tempContent.getScreenName().toLowerCase().contains("chrome") )
			   	browser1IconLbl.setIcon(newBrowser2);
			else if(_tempContent.getScreenName().toLowerCase().contains("safari") )
				browser1IconLbl.setIcon(newSafari);
			else if(_tempContent.getScreenName().toLowerCase().contains("opera") )
				browser1IconLbl.setIcon(newOpera);
			else if(_tempContent.getScreenName().toLowerCase().contains("mozilla") )
			   	browser1IconLbl.setIcon(newMozilla);
			     
		}
			        if(noOfBrowsers >= 2)
			        {
			        	_tempContent = browsers.get(1);
			        	browserPlot.setValue(_tempContent.getScreenName(), _tempContent.getUserCount());
			        	lightOrangeLbl.setIcon(newlightOrange);
			        	if(_tempContent.getScreenName().compareToIgnoreCase("IE") == 0 ||_tempContent.getScreenName().toLowerCase().contains("explorer") )
				        	browser2IconLbl.setIcon(newBrowser);
				        else if(_tempContent.getScreenName().toLowerCase().contains("chrome") )
				        	browser2IconLbl.setIcon(newBrowser2);
				        else if(_tempContent.getScreenName().toLowerCase().contains("safari") )
				        	browser2IconLbl.setIcon(newSafari);
				        else if(_tempContent.getScreenName().toLowerCase().contains("opera") )
				        	browser2IconLbl.setIcon(newOpera);
				        else if(_tempContent.getScreenName().toLowerCase().contains("mozilla") )
				        	browser2IconLbl.setIcon(newMozilla);
			        }
			        if(noOfBrowsers >= 3)
			        {
			        	_tempContent = browsers.get(2);
			        	browserPlot.setValue(_tempContent.getScreenName(), _tempContent.getUserCount());
			        	darkerOrnageLbl.setIcon(newDarkerOrange);
			        	if(_tempContent.getScreenName().compareToIgnoreCase("IE") == 0 ||_tempContent.getScreenName().toLowerCase().contains("explorer") )
				        	browser3IconLbl.setIcon(newBrowser);
				        else if(_tempContent.getScreenName().toLowerCase().contains("chrome") )
				        	browser3IconLbl.setIcon(newBrowser2);
				        else if(_tempContent.getScreenName().toLowerCase().contains("safari") )
				        	browser3IconLbl.setIcon(newSafari);
				        else if(_tempContent.getScreenName().toLowerCase().contains("opera") )
				        	browser3IconLbl.setIcon(newOpera);
				        else if(_tempContent.getScreenName().toLowerCase().contains("mozilla") )
				        	browser3IconLbl.setIcon(newMozilla);
			        }
			        if(noOfBrowsers >= 4)
			        {
			        	_tempContent = browsers.get(3);
			        	browserPlot.setValue(_tempContent.getScreenName(), _tempContent.getUserCount());
			        	lighterOrangeLbl.setIcon(newlighterOrange);
			        	if(_tempContent.getScreenName().compareToIgnoreCase("IE") == 0 ||_tempContent.getScreenName().toLowerCase().contains("explorer") )
				        	browser4IconLbl.setIcon(newBrowser);
				        else if(_tempContent.getScreenName().toLowerCase().contains("chrome") )
				        	browser4IconLbl.setIcon(newBrowser2);
				        else if(_tempContent.getScreenName().toLowerCase().contains("safari") )
				        	browser4IconLbl.setIcon(newSafari);
				        else if(_tempContent.getScreenName().toLowerCase().contains("opera") )
				        	browser4IconLbl.setIcon(newOpera);
				        else if(_tempContent.getScreenName().toLowerCase().contains("mozilla") )
				        	browser4IconLbl.setIcon(newMozilla);
			        }
			        JFreeChart _browserGraph = ChartFactory.createPieChart(
			                "",  // chart title
			                browserPlot,             // data
			                false,               // include legend
			                true,
			                false
			            );
		     
			       
			        _browserGraph.removeLegend();
			        _browserGraph.setBorderVisible(false);
			        PiePlot _pieBrowswerPlot = (PiePlot) _browserGraph.getPlot();
			        _pieBrowswerPlot.setLabelGenerator(null);
			        _pieBrowswerPlot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
			        _pieBrowswerPlot.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
			        _pieBrowswerPlot.setNoDataMessage("No data available");
			        _pieBrowswerPlot.setCircular(true);
			        _pieBrowswerPlot.setLabelGap(0.02);
			        _pieBrowswerPlot.setOutlineVisible(false);
			        _pieBrowswerPlot.setShadowXOffset(0.0);
			        _pieBrowswerPlot.setShadowYOffset(0.0);
			        _pieBrowswerPlot.setBaseSectionOutlinePaint(Color.WHITE);
			        _pieBrowswerPlot.setShadowPaint(getBackground());
			        String browserName = "Internet_Explorer";
			        
			        //set section paints depending on how many browsers data is available , max 4
			        
			        if(noOfBrowsers > 0)
			        {
			        	_pieBrowswerPlot.setSectionPaint(browsers.get(0).getScreenName(), new Color(251,175,25));
			        }
			        if(noOfBrowsers >= 2)
			        {
			        	_pieBrowswerPlot.setSectionPaint(browsers.get(1).getScreenName(), new Color(254,193,71));
			        }
			        if(noOfBrowsers >= 3)
			        {
			        	_pieBrowswerPlot.setSectionPaint(browsers.get(2).getScreenName(), new Color(255,128,0));
			        }
			        if(noOfBrowsers >= 4)
			        {
			        	_pieBrowswerPlot.setSectionPaint(browsers.get(3).getScreenName(), new Color(239,228,176));
			        }

			       
			        
		           // ChartPanel pieChartPanel = new ChartPanel(_pieChart);
			        
			        browserChartPanel.setChart(_browserGraph);
			        //= new ChartPanel(_browserGraph);
		            browserChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		            browserChartPanel.setPreferredSize(new Dimension(150, 150));
		            browserChartPanel.setBorder(BorderFactory.createEmptyBorder());
		            
//		            browser2IconLbl.repaint();
//		            browser1IconLbl.repaint();

		            pieChartPanel.repaint();
		            browserChartPanel.repaint();
		            String imageName = browserName + "_Icon.png";
		 	
			        comboBoxTimeChart.setSelectedIndex(this.rightComboSelectedIndex);
			        
			        revalidate();
			        repaint();
		        
	}

	private void addMapAndNoOfVisitsToUI(List<UserLocationsPerGateway> totalUserLocations)
	{
		
		//Construct the geo locations information to be shown on teh map.
		
		List<Waypoint> geoPositions = new ArrayList<Waypoint>();
		int noOfLocations = 0, i=0, noOfCurrentSessionScreens = 0;
		UsersOverviewInformation uOverview;
		List<UserVisitsPanel> _userpanelsList = new ArrayList<UserVisitsPanel>();
		int noOfLocsPerGateway = 0;
		if(totalUserLocations != null)
		{
			noOfLocations = totalUserLocations.size();
			for(i=0; i<noOfLocations; i++)
			{
				List<UserLocations> _userLocs = totalUserLocations.get(i).getUserLocations();
				noOfLocsPerGateway = _userLocs.size();
				String gatewayName = totalUserLocations.get(i).getGatewayID();
				for(int j=0; j<noOfLocsPerGateway; j++ )
				{
					GeoPosition pos = new GeoPosition(_userLocs.get(j).getLatitude(), _userLocs.get(j).getLongitude());
					if(_userLocs.get(j).getLatitude() == 0 && _userLocs.get(j).getLongitude() == 0)
					{
						
					}
					else
					{
						geoPositions.add(new DefaultWaypoint(pos));
					}
					//geoPositions.add(new DefaultWaypoint(pos));
					noOfCurrentSessionScreens = 0;
					
					// add information about each user visits
					//check and add a bar line to - item 13
					String userName = _userLocs.get(j).getUserName();
					String userProfile = _userLocs.get(j).getUserAuthProfile();
					UserVisitsPanel _userVisitPanel = new UserVisitsPanel();
					_userVisitPanel.setBorder(BorderFactory.createEmptyBorder());
					_userVisitPanel.setPreferredSize(new Dimension(400,40));
					_userVisitPanel.lblUserName.setText(userName);
				
					if(this.allGateways)
					{
						uOverview = rpc.getUserInformationOnController(Constants.TODAY, userName,this.currentProject, this.allProjects, userProfile,gatewayName,false);
					}
					else
					{
						uOverview = rpc.getUserInformationOnController(Constants.TODAY, userName,this.currentProject, this.allProjects, userProfile,this.currentGateway,this.allGateways);
					}
					
					if(uOverview != null)
					{
						/* YM 25-04-2016 -- changed blue notches to no of screen views. as per Chris's comments*/
						//IA_VerticalBars _noOfVisits = new IA_VerticalBars(uOverview.getTotalVisits());
						String firstseen = uOverview.getFirstSeen();
					      Date date = new Date();
					      DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					      String d = df.format(date);
					      System.out.println("d: "+d);
					      System.out.println("firstseen: "+firstseen);
					      if(firstseen != null)
					      {
					    	  if(d.equals(firstseen.substring(0, 10))){
					    		  noOfUsers++;
					    		  System.out.println("noOfUsers: "+noOfUsers);
					    	  }
					      }
						for(ScreensCount sc : screensPerUser)
						{
							if(sc.getScreenName().compareToIgnoreCase(userName) == 0)
							{
								noOfCurrentSessionScreens = sc.getNoOfViews();
							}
						}
						IA_VerticalBars _noOfVisits = new IA_VerticalBars(noOfCurrentSessionScreens);
						_userVisitPanel.noOfVisits.add(_noOfVisits);
						if(uOverview.getCurrentScreen() != null && uOverview.getCurrentScreen().contains(","))
						{
							_userVisitPanel.lblLastScreenName.setText((uOverview.getCurrentScreen().split(",")[0]).trim());
						}
						else
						{
							_userVisitPanel.lblLastScreenName.setText("");
						}
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						Date _currentDate = new Date();
						Date _lastLoginDate = new Date();
						
						if(uOverview.getLastSeen() != null && uOverview.getLastSeen().length() > 0)
						{
							try {
								_lastLoginDate = sdf.parse(uOverview.getLastSeen());
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						int secondsDiff = (int)(_currentDate.getTime() - _lastLoginDate.getTime())/1000;
						
						String unitString = "";
						if(secondsDiff < 0)
						{
							secondsDiff = 0;
							unitString = " seconds ago";
						}
						if(secondsDiff >= 60) //convert to mins
						{
							secondsDiff = secondsDiff / 60;
							if(secondsDiff >= 60) //convert to hours
							{
								secondsDiff = secondsDiff / 60;
								if(secondsDiff > 24) //convert to days
								{
									secondsDiff = secondsDiff / 24;
									unitString = " days ago";
								}
								else
								{
									unitString = " hours ago";
								}
							}
							else
							{
								unitString = " minutes ago";
							}
						}
						else
						{
							unitString = " seconds ago";
						}
						
					
						_userVisitPanel.lblLastLoginTime.setText(secondsDiff + unitString);
					
						_noOfVisits.setBorder(BorderFactory.createEmptyBorder());
						_userpanelsList.add(_userVisitPanel);
						
					}
				}
			}
			
			
			UserVisitsTableModel userVisistsTablemodel = new UserVisitsTableModel(_userpanelsList);
			heading.setModel(userVisistsTablemodel);
			heading.setDefaultRenderer(UserVisitsPanel.class, new UserVisitsTableCellRenderer());
			
			heading.setRowHeight(60);
			heading.setTableHeader(null);
			heading.setBackground(new Color(229, 233, 236));
			heading.setPreferredScrollableViewportSize(new Dimension(400,300));
			
			heading.setShowVerticalLines(false);
			heading.setGridColor(Constants.COLOR_MAIN_BACKGROUND);
			heading.setIntercellSpacing(new Dimension(0,0));
			this.usersList.setViewportView(heading);
			this.usersList.getViewport().setBackground(Constants.COLOR_WHITE_BACKGROUND);
		}
		
		
		
		
		// Add interactions
		MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);
		mapViewer.addMouseListener(this);
		
		
		
		Set<Waypoint> waypoints = new HashSet<Waypoint>(geoPositions);
		WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setWaypoints(waypoints);
		
		mapViewer.setOverlayPainter(waypointPainter);
		
		
		//add interactions for mapViewer in popup
		
		// Add interactions
				MouseInputListener miaPopup = new PanMouseInputListener(mapViewerInPopup);
				mapViewerInPopup.addMouseListener(miaPopup);
				mapViewerInPopup.addMouseMotionListener(miaPopup);
				mapViewerInPopup.addMouseWheelListener(this);
				
		mapViewerInPopup.setOverlayPainter(waypointPainter);
		
		if(!geoPositions.isEmpty())
		{
			if(geoPositions.get(0) != null)
			{
				mapViewerInPopup.setCenterPosition(geoPositions.get(0).getPosition());
				mapViewer.setCenterPosition(geoPositions.get(0).getPosition());
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if(arg0.getSource() == this.mapViewer)
		{
			if(arg0.getClickCount() == 2)
			{
				JDialog dialog = new JDialog();
				dialog.getContentPane().add(this.mapViewerInPopup);
				dialog.setVisible(true);
				dialog.setSize(300, 200);
				dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
			}
		}
		
				
		if(arg0.getSource() instanceof  RoundedButton)
		{
			RoundedButton currentBtn = (RoundedButton) arg0.getSource();
		
		
			if(currentBtn.getName().compareToIgnoreCase("ACK_CLICK") == 0)
			{
	
				alrmsAck  = true;
				alrmsActive = false;
				btnAck.setBackground(new Color(76 ,72 ,72));
				btnActive.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				
				int noOFAckAlarms = alarmInfo.getNoOfAckAlarms();
				int q, r;
				txtHighPriority.setText("" );
				txtCriticalPriority.setText("" );
				txtLowPriority.setText("" );
				txtMediumPriority.setText("" );
				if(alarmInfo.getAckAlarmsCount().get("High") != null)
				{
					txtHighPriority.setText("" + alarmInfo.getAckAlarmsCount().get("High"));
				}
				if(alarmInfo.getAckAlarmsCount().get("Critical") != null)
				{
					txtCriticalPriority.setText("" + alarmInfo.getAckAlarmsCount().get("Critical"));
				}
				if(alarmInfo.getAckAlarmsCount().get("Low") != null)
				{
					txtLowPriority.setText("" + alarmInfo.getAckAlarmsCount().get("Low"));
				}
				if(alarmInfo.getAckAlarmsCount().get("Medium") != null)
				{
					txtMediumPriority.setText("" + alarmInfo.getAckAlarmsCount().get("Medium"));
				}
				
				value.setNoOfAlarms(noOFAckAlarms);
				value.setStringText("Ack Alarms");
				if(noOFAckAlarms <= 100)
				{
					value.setNumberOfSegments(noOFAckAlarms);
				}
				else if(noOFAckAlarms > 100 && noOFAckAlarms <= 1000)
				{
					q = noOFAckAlarms / 10;
					if(noOFAckAlarms % 10 > 0)
					{
						q = q + 1;
					}
				
					value.setNumberOfSegments(q);
				}
				else if(noOFAckAlarms > 1000 && noOFAckAlarms <= 10000)
				{
					q = noOFAckAlarms / 50;
					if(noOFAckAlarms % 50 > 0)
					{
						q = q + 1;
					}
				
					value.setNumberOfSegments(q);
				}
				else if(noOFAckAlarms > 10000)
				{
					q = noOFAckAlarms / 100;
					if(noOFAckAlarms % 100 > 0)
					{
						q = q + 1;
					}
				
					value.setNumberOfSegments(q);
				}
				
			}
			else if(currentBtn.getName().compareToIgnoreCase("ACTIVE_CLICK") == 0){
			
				alrmsActive = true;
				alrmsAck = false;
				
				int noOFActiveAlarms = alarmInfo.getNoOfActiveAlarms();
				int q, r;
				btnActive.setBackground(new Color(76 ,72 ,72));
				btnAck.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				txtHighPriority.setText("" );
				txtCriticalPriority.setText("" );
				txtLowPriority.setText("" );
				txtMediumPriority.setText("" );
				
				if(alarmInfo.getActiveAlarmsCount().get("High") != null)
				{
					txtHighPriority.setText("" + alarmInfo.getActiveAlarmsCount().get("High"));
				}
				if(alarmInfo.getActiveAlarmsCount().get("Critical") != null)
				{
					txtCriticalPriority.setText("" + alarmInfo.getActiveAlarmsCount().get("Critical"));
				}
				if(alarmInfo.getActiveAlarmsCount().get("Low") != null)
				{
					txtLowPriority.setText("" + alarmInfo.getActiveAlarmsCount().get("Low"));
				}
				if(alarmInfo.getActiveAlarmsCount().get("Medium") != null)
				{
					txtMediumPriority.setText("" + alarmInfo.getActiveAlarmsCount().get("Medium"));
				}
				
				value.setNoOfAlarms(noOFActiveAlarms);
				value.setStringText("Active Alarms");
				if(noOFActiveAlarms <= 100)
				{
					value.setNumberOfSegments(noOFActiveAlarms);
				}
				else if(noOFActiveAlarms > 100 && noOFActiveAlarms <= 1000)
				{
					q = noOFActiveAlarms / 10;
					if(noOFActiveAlarms % 10 > 0)
						{
						q = q +1;
						}
					
					value.setNumberOfSegments(q);
				}
				else if(noOFActiveAlarms > 1000 && noOFActiveAlarms <= 10000)
				{
					q = noOFActiveAlarms / 50;
					if( noOFActiveAlarms % 50 > 0)
					{
						q = q +1;
						}
					
					value.setNumberOfSegments(q );
				}
				else if(noOFActiveAlarms > 10000)
				{
					q = noOFActiveAlarms / 100;
					if(noOFActiveAlarms % 100 > 0)
					{
						q = q +1;
						}
					
					value.setNumberOfSegments(q );
				}
				
				value.repaint();
			}
			
			else if(currentBtn.getName().compareToIgnoreCase("AVG_DEPTH_SCREEN") == 0) {
				//avgSessionTimeButton.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				avgScreenDepthStatus = true;
				activeUsersStatus = false;
				avgDepthOfScreen.setBackground(new Color(76 ,72 ,72));
				activeUsersBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				populateScreenDepthTableController(overViewList);
				
			}
			else if(currentBtn.getName().compareToIgnoreCase("ACT_USR") == 0) {
				//avgSessionTimeButton.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				avgScreenDepthStatus = false;
				activeUsersStatus = true;
				avgDepthOfScreen.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				activeUsersBtn.setBackground(new Color(76 ,72 ,72));
				populateActiveUsersTableOnController();
			}
			else if (currentBtn.getName().compareToIgnoreCase("USER_ONLINE_BTN") == 0){
				userOnlineStatus = true;
				userReturningStatus = false;
				usersOnlineBtn.setBackground(new Color(76 ,72 ,72));
				returningBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				//taggedBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
			}
			else if (currentBtn.getName().compareToIgnoreCase("RETURNING_BTN") == 0){
				userOnlineStatus = false;
				userReturningStatus = true;
				usersOnlineBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
				returningBtn.setBackground(new Color(76 ,72 ,72));
				//taggedBtn.setBackground(Constants.COLOR_BLACK_TITLE_BACKGROUND);
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

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if (cmd.compareToIgnoreCase(Constants.CMD_DURATION_SELECT) == 0)
		{
			this.rightComboSelectedIndex = comboBoxTimeChart.getSelectedIndex();
//			this.currentDurationA = Constants.TODAY;
//			this.currentDurationB = Constants.YESTERDAY;
			String selectedDropDown = comboBoxTimeChart.getSelectedItem().toString();
		//	if (selectedDropDown.compareToIgnoreCase("Today vs YesterDay") == 0)
			if(this.rightComboSelectedIndex == 0)
			{
				this.currentDurationA = Constants.TODAY;
				this.currentDurationB = Constants.YESTERDAY;
				
			}
			//else if (selectedDropDown.compareTo("This Week vs Last Week") == 0)
			else if(this.rightComboSelectedIndex == 1)
			{
				this.currentDurationA = Constants.THIS_WEEK;
				this.currentDurationB = Constants.LAST_WEEK;
				
			}
			//else if (selectedDropDown.compareToIgnoreCase("This Month vs Last Month") == 0)
			else if(this.rightComboSelectedIndex == 2)
			{
				this.currentDurationA = Constants.THIS_MONTH;
				this.currentDurationB = Constants.LAST_MONTH;
				
			}
			
			lineGraphPrint();
			revalidate();
			repaint();
			
		}
		
	}
	
	private void lineGraphPrint() {
		
	try {
		Dataset dataToday ;
		Dataset dataYesterDay;
		if (this.currentDurationA == Constants.TODAY || this.currentDurationB == Constants.YESTERDAY){
			
		dataToday = rpc.getTotalUsersDataOnController( currentProject, this.allProjects,this.currentGateway,this.allGateways, this.currentDurationA);
		dataYesterDay = rpc.getTotalUsersDataOnController( currentProject, this.allProjects,this.currentGateway,this.allGateways, this.currentDurationB);
		}
		else
			if ((this.currentDurationA == Constants.THIS_WEEK && this.currentDurationB == Constants.LAST_WEEK )|| (this.currentDurationA == Constants.THIS_MONTH || this.currentDurationB == Constants.LAST_MONTH))
		{	
			dataToday = rpc.getTotalUsersDataOnController( currentProject, this.allProjects,this.currentGateway,this.allGateways, this.currentDurationA);
		    dataYesterDay = rpc.getTotalUsersDataOnController( currentProject, this.allProjects,this.currentGateway,this.allGateways, this.currentDurationB);
		}else
		{
			dataToday = rpc.getTotalUsersDataOnController( currentProject, this.allProjects,this.currentGateway,this.allGateways, Constants.TODAY);
			dataYesterDay = rpc.getTotalUsersDataOnController( currentProject, this.allProjects,this.currentGateway,this.allGateways, Constants.YESTERDAY);
		}
		DefaultCategoryDataset _lineData = new DefaultCategoryDataset();
		JFreeChart _lineChart;
		
		//create a line chart to show total actions and total sessions for various durations
		//retrieve the series data from gateway using RPC
		int noOfRows = 0;
		int todayRow = 0;
		int yesterdayRow = 0;
		
		if(dataToday != null)
		{
				todayRow = dataToday.getRowCount();
		}
	
		if(dataYesterDay != null)
		{ 
			yesterdayRow = dataYesterDay.getRowCount();
		}
		

		
		//new 
		
			if (todayRow>yesterdayRow)
			
			  noOfRows = todayRow;
			else 
				noOfRows = yesterdayRow;
			
			int i = 0;
			if(this.currentDurationA != Constants.THIS_WEEK && this.currentDurationB != Constants.LAST_WEEK )
			{
				for(i=0; i<noOfRows; i++)
				{
					//check if i is less than todayRow
					if(i < todayRow){
					if(dataToday.getValueAt(i, 1) != null)
					{
						int testVal = (int)Float.parseFloat(dataToday.getValueAt(i, 1).toString());
						_lineData.addValue(testVal, "Today",dataToday.getValueAt(i, 0).toString());
					}
					else
					{
						_lineData.addValue(0, "Today",dataToday.getValueAt(i, 0).toString());
					}
					}else {
						_lineData.addValue(0, "Today",dataYesterDay.getValueAt(i, 0).toString());
					}
					
					//check if i is less than yesterdayRow
					if(i < yesterdayRow){
					if(dataYesterDay.getValueAt(i, 1) != null)
					{
						_lineData.addValue((int)Float.parseFloat(dataYesterDay.getValueAt(i, 1).toString()), "Yesterday",dataYesterDay.getValueAt(i, 0).toString());
					}
					else
					{
						_lineData.addValue(0, "Yesterday",dataYesterDay.getValueAt(i, 0).toString());
					}
					}
					else
					{
						_lineData.addValue(0, "Yesterday",dataToday.getValueAt(i, 0).toString());
					}
				}
			}
			else
			{
				
				_lineData.addValue(getValueForDay(dataToday, "monday"), "Today", "Monday");
				_lineData.addValue(getValueForDay(dataToday, "tuesday"), "Today", "Tuesday");
				_lineData.addValue(getValueForDay(dataToday, "wednesday"), "Today", "Wednesday");
				_lineData.addValue(getValueForDay(dataToday, "thursday"), "Today", "Thursday");
				_lineData.addValue(getValueForDay(dataToday, "friday"), "Today", "Friday");
				_lineData.addValue(getValueForDay(dataToday, "saturday"), "Today", "Saturday");
				_lineData.addValue(getValueForDay(dataToday, "sunday"), "Today", "Sunday");
				
				_lineData.addValue(getValueForDay(dataYesterDay, "monday"), "Yesterday", "Monday");
				_lineData.addValue(getValueForDay(dataYesterDay, "tuesday"), "Yesterday", "Tuesday");
				_lineData.addValue(getValueForDay(dataYesterDay, "wednesday"), "Yesterday", "Wednesday");
				_lineData.addValue(getValueForDay(dataYesterDay, "thursday"), "Yesterday", "Thursday");
				_lineData.addValue(getValueForDay(dataYesterDay, "friday"), "Yesterday", "Friday");
				_lineData.addValue(getValueForDay(dataYesterDay, "saturday"), "Yesterday", "Saturday");
				_lineData.addValue(getValueForDay(dataYesterDay, "sunday"), "Yesterday", "Sunday");
//				for(i=0; i<todayRow; i++)
//				{
//					dayName = dataToday.getValueAt(i, 0).toString();
//					
//					
//					if(dataToday.getValueAt(i, 1) != null)
//					{
//						_lineData.addValue((int)Float.parseFloat(dataToday.getValueAt(i, 1).toString()), "Today",dayName);
//					}
//					else
//					{
//						_lineData.addValue(0, "Today",dayName);
//					}
//				}
//				
//				dayName = "";
//				for(i=0; i<yesterdayRow; i++)
//				{
//					dayName = dataYesterDay.getValueAt(i, 0).toString();
//					if(dataYesterDay.getValueAt(i, 1) != null)
//					{
//						_lineData.addValue((int)Float.parseFloat(dataYesterDay.getValueAt(i, 1).toString()), "Yesterday",dayName);
//					}
//					else
//					{
//						
//						_lineData.addValue(0, "Yesterday",dayName);
//					}
//				}
				
			}
			
			_lineChart = ChartFactory.createLineChart("", "", "", _lineData, PlotOrientation.VERTICAL, false, true, true );
			
			
			_lineChart.setBackgroundImageAlpha(0.0f);
			_lineChart.setBackgroundPaint(Constants.COLOR_TOP_PANEL_BACKGROUND);
			_lineChart.setBorderVisible(false);
			
			CategoryPlot _plot = (CategoryPlot) _lineChart.getPlot();
			_plot.setBackgroundImageAlpha(0.0f);
			_plot.setBackgroundPaint(new Color(0xFF, 0xFF, 0xFF, 0));
			//_plot.setRangeGridlinePaint(Constants.COLOR_COMBO_BACKGROUND); 
			_plot.setDomainGridlinePaint(Constants.COLOR_COMBO_BACKGROUND);
	        _plot.setRangeGridlinesVisible(false);
	        _plot.setDomainGridlinesVisible(true);
			_plot.setOutlineVisible(false);
	        //to set the line colours
	        LineAndShapeRenderer renderer = (LineAndShapeRenderer)  _plot.getRenderer();
	        renderer.setSeriesPaint(0,Color.WHITE);
	        renderer.setSeriesPaint(1, new Color(82,139,204));
	      
	        Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 5.0f, 5.0f);
	       
	        //to set the series marker shape to circle , default is square
	       // renderer.setSeriesShape(0, circle);
	        //renderer.setBaseShapesVisible(true);
	        
	        NumberAxis rangeAxis = (NumberAxis) _plot.getRangeAxis();
			rangeAxis.setVisible(true); 
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			rangeAxis.setTickLabelPaint(Constants.COLOR_COMBO_BACKGROUND);
	        rangeAxis.setAutoRangeIncludesZero(true);
	        rangeAxis.setAxisLineVisible(false);
	        //rangeAxis.setAxisLinePaint(Constants.COLOR_GREY_LABEL);
	        rangeAxis.setAxisLineVisible(false);
	        
	        final CategoryAxis domainAxis = _plot.getDomainAxis();
	        domainAxis.setVisible(true);
	        domainAxis.setTickLabelPaint(Constants.COLOR_COMBO_BACKGROUND);
			domainAxis.setAxisLineVisible(false);
			 _lineChartPanel.setChart(_lineChart);
				
		       // _lineChartPanel.setChart(_lineChart);
//		        _lineChartPanel.setBackground(Color.DARK_GRAY);
//				_lineChartPanel.setOpaque(false);
//				_lineChartPanel.setPreferredSize(new Dimension(700,150));
				_lineChartPanel.repaint();

//	        revalidate();
//	        repaint();
		
	} catch (NumberFormatException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

		
	}
public void populateScreenDepthTable(){
	
	
	if(screensPerUser != null)
	{
		
	    int sessions0_2Screens = 0;
	     int sessions3_4Screens = 0;
	     int sessions5_7Screens = 0;
	     int sessions8_10Screens = 0;
	     int sessions11_15Screens = 0;
	     int sessions16PlusScreens = 0;
	     int noOfRecords = 0;
	   
	     int  tempScreenVal = 0;
	     noOfRecords = screensPerUser.size();
	     
	     //build the dataset for bar chart
	     for(int i=0; i<noOfRecords; i++)
	     {
	    	
	    	
	    	 tempScreenVal = screensPerUser.get(i).getNoOfViews();
	    	 if(tempScreenVal <= 2.0)
	    	 {
	    		 sessions0_2Screens++;
	    	 }
	    	 else if(tempScreenVal > 2.0 && tempScreenVal <=4.0)
	    	 {
	    		 sessions3_4Screens++;
	    	 }
	    	 else if(tempScreenVal > 4.0 && tempScreenVal <=7.0)
	    	 {
	    		 sessions5_7Screens++;
	    	 }
	    	 else if(tempScreenVal > 7.0 && tempScreenVal <=10.0)
	    	 {
	    		 sessions8_10Screens++;
	    	 }
	    	 else if(tempScreenVal > 10.0 && tempScreenVal <=15.0)
	    	 {
	    		 sessions11_15Screens++;
	    	 }
	    	 else if(tempScreenVal > 15.0)
	    	 {
	    		 sessions16PlusScreens++;
	    	 }
	     }
	     
	     JTable screenDepthTable = new JTable(); //hold engagement section information
	 	DefaultTableModel screenDepthTableModel = new DefaultTableModel();
	 	screenDepthTableModel.setColumnIdentifiers(new Object[]{"Screen Depth", " Sessions"});
	     
		 if(noOfRecords > 0)
		 {
			 
			 screenDepthTableModel.addRow(new Object[]{"0-2 screens", sessions0_2Screens});
			 screenDepthTableModel.addRow(new Object[]{"3-4 screens", sessions3_4Screens });
			 screenDepthTableModel.addRow(new Object[]{"5-7 screens", sessions5_7Screens});
			 screenDepthTableModel.addRow(new Object[]{"8-10 screens", sessions8_10Screens});
			 screenDepthTableModel.addRow(new Object[]{"11-15 screens", sessions11_15Screens});
			 screenDepthTableModel.addRow(new Object[]{"16+ screens", sessions16PlusScreens});
		 } 
		 screenDepthTable.setModel(screenDepthTableModel);
		       // PMIChartPanel chartPanel = new PMIChartPanel(chart);
		 RightPaddedTableCellRenderer centerRenderer = new RightPaddedTableCellRenderer();
		 centerRenderer.paddingSize = 45;
//			centerRenderer.setHorizontalAlignment( JLabel.RIGHT);
//			centerRenderer.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
			
			screenDepthTable.setIntercellSpacing(new Dimension(0, 0));
			screenDepthTable.setShowVerticalLines(false);
			screenDepthTable.setShowHorizontalLines(false);
			screenDepthTable.setShowGrid(false);
			screenDepthTable.setRowHeight(25);
			
			screenDepthTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			screenDepthTable.getColumnModel().getColumn(0).setCellRenderer( this.leftRenderer );
			screenDepthTable.setFillsViewportHeight(true);
			screenDepthTable.setTableHeader(null);
			screenDepthTable.setEnabled(false);
			screenDepthTable.setPreferredScrollableViewportSize(new Dimension(300,180));
		 		
		        this.engagementInnerPannel.setViewportView(screenDepthTable);
		     
		        revalidate();
		        repaint();
		 }
	
}





public void populateScreenDepthTableController(List<CurrentOverview> overviewList){
	
	
	 int sessions0_2Screens = 0;
     int sessions3_4Screens = 0;
     int sessions5_7Screens = 0;
     int sessions8_10Screens = 0;
     int sessions11_15Screens = 0;
     int sessions16PlusScreens = 0;
     int noOfRecords = 0;
	int noOfAllGatewaysRecords = 0;
	if(overviewList != null){
		for(int j = 0 ; j < overviewList.size() ; j++){
			CurrentOverview currentOverView = overviewList.get(j);
			if(currentOverView != null){
			List<ScreensCount> screenCountList = currentOverView.getScreenViewscountPerUser(); 
	if(screenCountList != null)
	{
		System.out.println("populateScreenDepthTableController : screenCountList not null"  );
		
		
	     int  tempScreenVal = 0;
	     noOfRecords = screenCountList.size();
	     System.out.println("populateScreenDepthTableController : noOfRecords = " + noOfRecords );
	     noOfAllGatewaysRecords = noOfAllGatewaysRecords + noOfRecords;
	     //build the dataset for bar chart
	     for(int i=0; i<noOfRecords; i++)
	     {
	    	
	    	
	    	 tempScreenVal = screenCountList.get(i).getNoOfViews();
	    	 if(tempScreenVal <= 2.0)
	    	 {
	    		 sessions0_2Screens++;
	    	 }
	    	 else if(tempScreenVal > 2.0 && tempScreenVal <=4.0)
	    	 {
	    		 sessions3_4Screens++;
	    	 }
	    	 else if(tempScreenVal > 4.0 && tempScreenVal <=7.0)
	    	 {
	    		 sessions5_7Screens++;
	    	 }
	    	 else if(tempScreenVal > 7.0 && tempScreenVal <=10.0)
	    	 {
	    		 sessions8_10Screens++;
	    	 }
	    	 else if(tempScreenVal > 10.0 && tempScreenVal <=15.0)
	    	 {
	    		 sessions11_15Screens++;
	    	 }
	    	 else if(tempScreenVal > 15.0)
	    	 {
	    		 sessions16PlusScreens++;
	    	 }
	     }
	    
		 }
	else
	{
		System.out.println("populateScreenDepthTableController : screenCountList is null"  );
	}
		}
		}
	}
	 JTable screenDepthTable = new JTable(); //hold engagement section information
	 	DefaultTableModel screenDepthTableModel = new DefaultTableModel();
	 	screenDepthTableModel.setColumnIdentifiers(new Object[]{"Screen Depth", " Sessions"});
	     
		 if(noOfAllGatewaysRecords > 0)
		 {
			 
			 screenDepthTableModel.addRow(new Object[]{"0-2 screens", sessions0_2Screens});
			 screenDepthTableModel.addRow(new Object[]{"3-4 screens", sessions3_4Screens });
			 screenDepthTableModel.addRow(new Object[]{"5-7 screens", sessions5_7Screens});
			 screenDepthTableModel.addRow(new Object[]{"8-10 screens", sessions8_10Screens});
			 screenDepthTableModel.addRow(new Object[]{"11-15 screens", sessions11_15Screens});
			 screenDepthTableModel.addRow(new Object[]{"16+ screens", sessions16PlusScreens});
		 } 
		 screenDepthTable.setModel(screenDepthTableModel);
		       // PMIChartPanel chartPanel = new PMIChartPanel(chart);
		 RightPaddedTableCellRenderer centerRenderer = new RightPaddedTableCellRenderer();
		 centerRenderer.paddingSize = 45;
//			centerRenderer.setHorizontalAlignment( JLabel.RIGHT);
//			centerRenderer.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
			
			screenDepthTable.setIntercellSpacing(new Dimension(0, 0));
			screenDepthTable.setShowVerticalLines(false);
			screenDepthTable.setShowHorizontalLines(false);
			screenDepthTable.setShowGrid(false);
			screenDepthTable.setRowHeight(25);
			
			screenDepthTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			screenDepthTable.getColumnModel().getColumn(0).setCellRenderer( this.leftRenderer );
			screenDepthTable.setFillsViewportHeight(true);
			screenDepthTable.setTableHeader(null);
			screenDepthTable.setEnabled(false);
			screenDepthTable.setPreferredScrollableViewportSize(new Dimension(300,180));
		 		
		        this.engagementInnerPannel.setViewportView(screenDepthTable);
		     
		        revalidate();
		        repaint();
}









private void populateAverageSessionTimeTable(){
	
//	this.engagementInnerPannel.setViewportView(null);
	Dataset engagementData  = rpc.getEngagementInformationOnController(this.currentProject, this.allProjects,this.currentGateway,this.allGateways, Constants.TODAY);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	if(engagementData != null)
	{
		String series1 = "Sessions";
	    String series2 = "Screen Views";
	    
	    int users1Session = 0, screens1Session = 0;
	     int users2_5Session = 0, screens2_5Session = 0;
	     int users6_10Session = 0, screens6_10Session = 0;
	     int users11_25Session = 0, screens11_25Session = 0;
	     int users26_50Session = 0, screens26_50Session = 0;
	     int users50PlusSession = 0, screens50PlusSession = 0;
	     int noOfRecords = 0;
	   
	     Double tempsessionVal = 0.0;
	     int tempUserVal = 0, tempScreenVal = 0;
	     noOfRecords = engagementData.getRowCount();
	     Date tempDate = null;
	     
	     DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	     
	     //build the dataset for bar chart
	     for(int i=0; i<noOfRecords; i++)
	     {
	    	 tempsessionVal = 0.0;
	    	 tempScreenVal = 0;
	    	 tempUserVal = 0;
	    	 tempDate = null;
	    	 if(engagementData.getValueAt(i, 0) != null)
	    	 {
	    		 try {
					tempDate = sdf.parse(engagementData.getValueAt(i, 0).toString());
					 String timeVal[] = sdf.format(tempDate).split(":");
		    		 
		    		 tempsessionVal =  60 * (Double.parseDouble(timeVal[0])) 
		    				 + Double.parseDouble(timeVal[1]) 
		    				 + (1/60.0) * Double.parseDouble(timeVal[2]);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    	 }
	    	 if(engagementData.getValueAt(i, 1) != null)
	    	 {
	    		 tempUserVal = (int)Float.parseFloat(engagementData.getValueAt(i, 1).toString());
	    	 }
	    	 if(engagementData.getValueAt(i, 2) != null)
	    	 {
	    		 tempScreenVal = (int)Float.parseFloat(engagementData.getValueAt(i, 2).toString());
	    	 }
	    	
	    	 if(tempsessionVal <= 5.0)
	    	 {
	    		 users1Session = users1Session + tempUserVal;
	    		 screens1Session = screens1Session + tempScreenVal;
	    	 }
	    	 else if(tempsessionVal > 5.0 && tempsessionVal <=10.0)
	    	 {
	    		 users2_5Session += tempUserVal;
	    		 screens2_5Session = screens2_5Session + tempScreenVal;
	    	 }
	    	 else if(tempsessionVal > 10.0 && tempsessionVal <=30.0)
	    	 {
	    		 users6_10Session += tempUserVal;
	    		 screens6_10Session = screens6_10Session + tempScreenVal;
	    	 }
	    	 else if(tempsessionVal > 30.0 && tempsessionVal <=60.0)
	    	 {
	    		 users11_25Session += tempUserVal;
	    		 screens11_25Session = screens11_25Session + tempScreenVal;
	    	 }
	    	 else if(tempsessionVal > 60.0 && tempsessionVal <=120.0)
	    	 {
	    		 users26_50Session += tempUserVal;
	    		 screens26_50Session = screens26_50Session + tempScreenVal;
	    	 }
	    	 else if(tempsessionVal > 120.0)
	    	 {
	    		 users50PlusSession += tempUserVal;
	    		 screens50PlusSession = screens50PlusSession + tempScreenVal;
	    	 }
	     }
	     
	     JTable avgSessionTable = new JTable(); //hold engagement section information
	 	DefaultTableModel screenDepthTableModel = new DefaultTableModel();
	 	screenDepthTableModel.setColumnIdentifiers(new Object[]{"Screen Depth", " Sessions"});
	     
		 if(noOfRecords > 0)
		 {
			 screenDepthTableModel.addRow(new Object[]{"0-5 minutes", users1Session});
			 screenDepthTableModel.addRow(new Object[]{"5-10 minutes", users2_5Session });
			 screenDepthTableModel.addRow(new Object[]{"11-30 minutes", users6_10Session});
			 screenDepthTableModel.addRow(new Object[]{"31-60 minutes", users11_25Session});
			 screenDepthTableModel.addRow(new Object[]{"1-2 hours", users26_50Session});
			 screenDepthTableModel.addRow(new Object[]{"2+ hours", users50PlusSession});
		 } 
		 avgSessionTable.setModel(screenDepthTableModel);
		       // PMIChartPanel chartPanel = new PMIChartPanel(chart);
		 RightPaddedTableCellRenderer centerRenderer = new RightPaddedTableCellRenderer();
		 centerRenderer.paddingSize = 45;
			
			avgSessionTable.setIntercellSpacing(new Dimension(0, 0));
			avgSessionTable.setShowVerticalLines(false);
			avgSessionTable.setShowHorizontalLines(false);
			avgSessionTable.setShowGrid(false);
			avgSessionTable.setRowHeight(25);
			
			avgSessionTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			avgSessionTable.getColumnModel().getColumn(0).setCellRenderer( this.leftRenderer );
			avgSessionTable.setFillsViewportHeight(true);
			avgSessionTable.setTableHeader(null);
			avgSessionTable.setPreferredScrollableViewportSize(new Dimension(300,180));
			avgSessionTable.setEnabled(false);
		        this.engagementInnerPannel.setViewportView(avgSessionTable);
		     
		        revalidate();
		        repaint();
		 }

}


private void populateActiveUsersTable(){
	
	this.engagementInnerPannel.setViewportView(null);
	ActiveUsersInfo engagementData  = rpc.getActiveUsersCountsOnController(this.currentProject, this.allProjects,this.currentGateway,this.allGateways, Constants.TODAY);
	
	if(engagementData != null)
	{
	     //build the dataset for bar chart
	    
	     JTable avgSessionTable = new JTable(); //hold engagement section information
	 	DefaultTableModel screenDepthTableModel = new DefaultTableModel();
	 	screenDepthTableModel.setColumnIdentifiers(new Object[]{"User 	", " Numbers"});
	     
		 
			 screenDepthTableModel.addRow(new Object[]{"One Day Active Users", engagementData.getOneDayActiveUsers()});
			 screenDepthTableModel.addRow(new Object[]{"Seven Day Active Users", engagementData.getSevenDayActiveUsers() });
			 screenDepthTableModel.addRow(new Object[]{"Fourteen Day Active Users", engagementData.getFourteenDayActiveUsers()});
			 
		  
		 avgSessionTable.setModel(screenDepthTableModel);
		       // PMIChartPanel chartPanel = new PMIChartPanel(chart);
		 RightPaddedTableCellRenderer centerRenderer = new RightPaddedTableCellRenderer();
		 centerRenderer.paddingSize = 45;
		 centerRenderer.setHorizontalAlignment( JLabel.RIGHT);
			centerRenderer.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
			
			avgSessionTable.setIntercellSpacing(new Dimension(0, 0));
			avgSessionTable.setShowVerticalLines(false);
			avgSessionTable.setShowHorizontalLines(false);
			avgSessionTable.setShowGrid(false);
			avgSessionTable.setRowHeight(25);
			
			avgSessionTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			avgSessionTable.getColumnModel().getColumn(0).setCellRenderer( this.leftRenderer);
			avgSessionTable.setFillsViewportHeight(true);
			avgSessionTable.setTableHeader(null);
			avgSessionTable.setPreferredScrollableViewportSize(new Dimension(300,180));
			avgSessionTable.setEnabled(false);
		        this.engagementInnerPannel.setViewportView(avgSessionTable);
		     
		        revalidate();
		        repaint();
		 }

	}
private void populateActiveUsersTableOnController(){
	
	this.engagementInnerPannel.setViewportView(null);
	ActiveUsersInfo engagementData  = rpc.getActiveUsersCountsOnController(this.currentProject, this.allProjects,this.currentGateway,this.allGateways, Constants.TODAY);
	
	if(engagementData != null)
	{
	     //build the dataset for bar chart
	    
	     JTable avgSessionTable = new JTable(); //hold engagement section information
	 	DefaultTableModel screenDepthTableModel = new DefaultTableModel();
	 	screenDepthTableModel.setColumnIdentifiers(new Object[]{"User 	", " Numbers"});
	     
		 
			 screenDepthTableModel.addRow(new Object[]{"One Day Active Users", engagementData.getOneDayActiveUsers()});
			 screenDepthTableModel.addRow(new Object[]{"Seven Day Active Users", engagementData.getSevenDayActiveUsers() });
			 screenDepthTableModel.addRow(new Object[]{"Fourteen Day Active Users", engagementData.getFourteenDayActiveUsers()});
			 
		  
		 avgSessionTable.setModel(screenDepthTableModel);
		       // PMIChartPanel chartPanel = new PMIChartPanel(chart);
		 RightPaddedTableCellRenderer centerRenderer = new RightPaddedTableCellRenderer();
		 centerRenderer.paddingSize = 45;
		 centerRenderer.setHorizontalAlignment( JLabel.RIGHT);
			centerRenderer.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
			
			avgSessionTable.setIntercellSpacing(new Dimension(0, 0));
			avgSessionTable.setShowVerticalLines(false);
			avgSessionTable.setShowHorizontalLines(false);
			avgSessionTable.setShowGrid(false);
			avgSessionTable.setRowHeight(25);
			
			avgSessionTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
			avgSessionTable.getColumnModel().getColumn(0).setCellRenderer( this.leftRenderer);
			avgSessionTable.setFillsViewportHeight(true);
			avgSessionTable.setTableHeader(null);
			avgSessionTable.setPreferredScrollableViewportSize(new Dimension(300,180));
			avgSessionTable.setEnabled(false);
		        this.engagementInnerPannel.setViewportView(avgSessionTable);
		     
		        revalidate();
		        repaint();
		 }

	}
	private int getValueForDay(Dataset data, String dayName)
	{
		int retVal = 0;
		
		int dataSetLen = data.getRowCount();
		
		int i = 0;
		
		for(i=0; i<dataSetLen; i++)
		{
			if(dayName.compareToIgnoreCase(data.getValueAt(i, 0).toString()) == 0)
			{
				retVal = (int) Float.parseFloat(data.getValueAt(i, 1).toString());
				break;
			}
		}
		
		
		return retVal;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Point current = e.getPoint();
		Rectangle bound = mapViewerInPopup.getViewportBounds();
		
		double dx = current.x - bound.width / 2;
		double dy = current.y - bound.height / 2;
		
		Dimension oldMapSize = mapViewerInPopup.getTileFactory().getMapSize(mapViewerInPopup.getZoom());

		
		//calculate new zoom
		int newZoom = mapViewerInPopup.getZoom();
		int wheelRotation = e.getWheelRotation();
		if(wheelRotation < 0)
		{
			newZoom = newZoom -1;
		}
		else
		{
			newZoom = newZoom + 1;
		}
	
		mapViewerInPopup.setZoom(newZoom);
		
		Dimension mapSize = mapViewerInPopup.getTileFactory().getMapSize(mapViewerInPopup.getZoom());

		Point2D center = mapViewerInPopup.getCenter();

		double dzw = (mapSize.getWidth() / oldMapSize.getWidth());
		double dzh = (mapSize.getHeight() / oldMapSize.getHeight());

		double x = center.getX() + dx * (dzw - 1);
		double y = center.getY() + dy * (dzh - 1);

		mapViewerInPopup.setCenter(new Point2D.Double(x, y));
		
	}

}
