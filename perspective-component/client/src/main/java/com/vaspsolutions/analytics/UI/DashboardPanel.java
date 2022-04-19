package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;
import javax.swing.BoxLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Window;
import java.text.AttributedString;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.JScrollPane;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ColorUIResource;
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
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.text.TextBlock;
import org.jfree.text.TextBlockAnchor;
import org.jfree.text.TextUtilities;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import com.inductiveautomation.factorypmi.application.components.PMIBarChart;
import com.inductiveautomation.factorypmi.application.components.PMIButton;
import com.inductiveautomation.factorypmi.application.components.PMIChart;
import com.inductiveautomation.factorypmi.application.components.PMIEasyChart.EasyChart;
import com.inductiveautomation.factorypmi.application.components.PMIImage;
import com.inductiveautomation.factorypmi.application.components.PMITable;
import com.inductiveautomation.factorypmi.application.components.chart.PMIChartPanel;
import com.inductiveautomation.ignition.client.images.ImageLoader;
import com.inductiveautomation.ignition.client.images.PathIcon;
import com.inductiveautomation.ignition.client.util.gui.LinkButton;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.SystemUtilities;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;
import com.inductiveautomation.vision.api.client.VisionClientInterface;
import com.vaspsolutions.analytics.common.ActiveUsersInfo;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.DevicesInformation;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.OverviewInformation;
import com.vaspsolutions.analytics.common.ScreensCount;
import com.vaspsolutions.analytics.common.UsersCount;

import javax.swing.DropMode;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;

import javax.swing.border.MatteBorder;
/**
 * A class to represent the GUI layout , extends JPanel 
 * @author YM : Created on 05/20/2015
 *
 */
public class DashboardPanel extends JPanel implements ActionListener, MouseListener  {
	
	private static final long serialVersionUID = 1L;
	public IA_SlidingPanel top_left;
	public JPanel timeLine ;
	public RoundedPanel overview;
	public JPanel activeUsers ;
	public JPanel freqAndRecency;
	public JPanel engageMent;
	public JPanel alarms;
	public JPanel y_overview;
	public JPanel reportsAndTrends;
	public JPanel gateWay; 

	//components in Overview panel
	private IA_Label lblUsers;
	public OrangeText txtUsers;
	private IA_Label lblSessions;
	public OrangeText txtSessions;
	private IA_Label lblScreenViews;
	public OrangeText txtScreenViews;
	private IA_Label lblScreensbyCurrent;
	public OrangeText txtScreensbyCurrent;
	private IA_Label lblActions;
	public OrangeText txtActions;
	private IA_Label lblBounceRate;
	public OrangeText txtBounceRate;
	private IA_Label lblAvgSessionDuration;
	public OrangeText txtAvgSessionDuration;
	private JPanel duration;
	private MenuButton btnDay;
	private MenuButton btnWeek;
	private MenuButton btnMonth;
	private ModuleRPC rpc;
	
	//used for data retrieval
	public String currentProject;
	public String dataSource;
	public int currentDuration;
	public boolean allProjects;
	//end used for data retrieval
	
	private JLabel lblyUser;
	private OrangeText valyUsers;
	private JLabel lblySessions;
	private OrangeText valySessions;
	private JLabel lblYActions;
	private OrangeText valYActions;
	private JPanel contentPanel;
	private JScrollPane content;
	private IA_SlidePanelElement _slide1, _slide2,_slide3, _slide4, _slide5, _slide6;
	private OrangeText txtUppercent;
	private OrangeText txtUpTimeString;
	private OrangeText txtDowntimepercent;
	private OrangeText txtDowntimestring;
	private JComboBox<String> comboDuration;
	private RoundedButton btn1DayActiveUsers;
	private RoundedButton btn7DayActiveUsers;
	private RoundedButton btn14DayActiveUsers;
	private JPanel activeUsers_Chart;
	private JLabel label_3;
	private JLabel label_4;
	private JButton lblIconMedium;
	private JButton lblIconHigh;
	private JButton lblIconCritical;
	private JLabel lblValueMedium;
	private JLabel lblValueHigh;
	private JLabel lblValueCritical;
	private JLabel lblAverageTimeTo;
	private JLabel lblTimeToClrMedium;
	private JLabel lblTimeToClrHigh;
	private JLabel lblTimeToClrCritical;
	private JLabel lblIconUsers;
	private JLabel lblIconSessions;
	private JLabel lblIconActions;
	private JLabel valNewUsers;
	private JLabel lblNewUsers;
	private JLabel valAvgSession;
	private JLabel valActionsPerSession;
	private RoundedButton_WideLabel btnCountOfSessions;
	private RoundedButton_WideLabel btnDaysSinceLogin;
	private JPanel freqAndRecencyChart;
	private RoundedButton_WideLabel btnEngageDuration;
	private RoundedButton_WideLabel btnEngageDepth;
	private JPanel engGraphPanel;
	private Dataset activeUsersData;
	
	private JPanel upTimeLblPanel;
	private JLabel lblNewLabel;
	private JPanel downTimeLblPanel;
	private JLabel lblDownTime;
	private JPanel entireTopPanel;
	CombinedTopSlides topSlide;
	
	ImageIcon highlightWhite;
	ImageIcon nonHighlightGreyWhite;
	ImageIcon newAll;
	private JPanel panel;
	private JButton lblIconDividingLine;
	private JLabel lblYDayDivide1;
	private JLabel lblyDayDivide2;
	
	private JPanel panel_1;
	private RealTimeDeviceButton realTimeDeviceButton;
	private RealTimeDeviceButton realTimeDeviceButton_1;
	private JPanel panel_2;
	private JPanel chartPanel_1;
	private JPanel chartPanel_2;
	private ChartPanel chartPanel_3;
	private ChartPanel chartPanel_4;
	private JPanel panel_3;
	private JLabel darkBlueLbl;
	private JLabel lightBlueLbl;
	private JLabel darkerBlueLbl;
	private JLabel lighterBlueLbl;
	
	private JLabel darkOrnageLbl;
	private JLabel lightOrangeLbl;
	private JLabel darkerOrnageLbl;
	private JLabel lighterOrangeLbl;
	
	private JLabel device1Lbl;
	private JLabel device2Lbl;
	private JLabel device3Lbl;
	private JLabel device4Lbl;
	
	private JLabel browser1Lbl;
	private JLabel browser2Lbl;
	private JLabel browser3Lbl;
	private JLabel browser4Lbl;
	
	public int slideNo = 0;
	public int comboDurationSelectedIndex = 0;
	//
	// browser and device charts 
	ChartPanel deviceChartPanel;
	ChartPanel browserChartPanel;
	
	
	//active users chart panel
	ChartPanel _AUartPanel;
	
	//timeline chart panel
	 ChartPanel _timelineChartPanel;
	 
	 //engagaement chart panel
	 ChartPanel engChartPanel;
	 
	 //freq abd rec chart panel
	 ChartPanel _freqRecChartPanel;
	//Maintain satate of the current selected buttons on refresh
	private boolean oneDayActive = true;
	private boolean sevenDayActive = false;
	private boolean fourteenDayActive = false;
	private boolean screenDepth = false;
	private boolean screenViews = true;
	
	private boolean showFrequency = true;
	private boolean showRecency = false;
	
	//incons
	
	ImageIcon symbolForMobile;
	ImageIcon symbolForPC;
	ImageIcon symbolForMac;
	ImageIcon symbolForLinux;
	ImageIcon newBrowser;
	ImageIcon newBrowser2;
	ImageIcon newOpera;
	ImageIcon newSafari;
	ImageIcon newMozilla;
		ImageIcon newDarkBlue;
		ImageIcon newlightBlue;
		ImageIcon newDarkerBlue;
		ImageIcon newlighterBlue;
		ImageIcon newDarkOrange;
		ImageIcon newlightOrange;
		ImageIcon newDarkerOrange;
		ImageIcon newlighterOrange;
		
	
	public DashboardPanel(ModuleRPC _rpc, String projectName, String dsName, int slideNo, int comboIndex){
		
		
		System.setProperty("awt.useSystemAAFontSettings","gasp");
		System.setProperty("swing.aatext", "true");
//		UIManager.put("ComboBox.background", new ColorUIResource(Constants.COLOR_COMBO_BACKGROUND));
		
		ImageIcon imgHollow = new ImageIcon(getClass().getResource("White-Circle.png"));
		Image img = imgHollow.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		highlightWhite = new ImageIcon(img);
		
		ImageIcon imgFilled = new ImageIcon(getClass().getResource("Gray-and-White-Circle.png"));
		Image imgFilled1 = imgFilled.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		nonHighlightGreyWhite = new ImageIcon(imgFilled1);
		
		ImageIcon imgAll = new ImageIcon(getClass().getResource("White-Squares.png"));
		Image imageAll = imgAll.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		newAll = new ImageIcon(imageAll);
		
		//create icons
				ImageIcon symbolMobile = new ImageIcon(getClass().getResource("Mobile-White.png"));
		 	 	Image symbolNewMobile = symbolMobile.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		 	 	symbolForMobile = new ImageIcon(symbolNewMobile);
		 	        
		 	 	ImageIcon symbolPC = new ImageIcon(getClass().getResource("Windows_Icon.png"));
		 		Image symbolNewPC = symbolPC.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		 		symbolForPC = new ImageIcon(symbolNewPC);
		 		
		 		ImageIcon symbolMac = new ImageIcon(getClass().getResource("Mac_Icon.png"));
		 	 	Image symbolNewMac = symbolMac.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		 	 	symbolForMac = new ImageIcon(symbolNewMac);
		 	        
		 	 	ImageIcon symbolLinux = new ImageIcon(getClass().getResource("Linux_Icon.png"));
		 		Image symbolNewLinux = symbolLinux.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		 		symbolForLinux = new ImageIcon(symbolNewLinux);
		 		
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
		 		

		 		
		 		ImageIcon darkBlueIcon = new ImageIcon(getClass().getResource("Dark_Blue.png"));
		 		Image darkBlue = darkBlueIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newDarkBlue = new ImageIcon(darkBlue);
		 		
		 		ImageIcon lightBlueIcon = new ImageIcon(getClass().getResource("Light_blue.png"));
		 		Image lightBlue = lightBlueIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		 		newlightBlue = new ImageIcon(lightBlue);
		 		
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
		//create icons for dividing lines.
		ImageIcon icondividingLine = new ImageIcon(getClass().getResource("Dividing-Line.png"));
		Image imgDIcon = icondividingLine.getImage().getScaledInstance(400, 20, Image.SCALE_SMOOTH);
		icondividingLine = new ImageIcon(imgDIcon);
		
		ImageIcon ydayDividingLine = new ImageIcon(icondividingLine.getImage().getScaledInstance(320, 20, Image.SCALE_SMOOTH));
		String[] durations = new String[]{"Today", "Yesterday","Last 7 Days","Last 30 Days","Last 90 Days","Last 365 Days","This week","This month","This year","Last month","Last week","Last year"};
		this.rpc = _rpc;
		this.slideNo = slideNo;
		this.comboDurationSelectedIndex = comboIndex;
		//this.setPreferredSize(new Dimension(1720, 1080));
		this.setPreferredSize(new Dimension(1720, 1080));
		this.setOpaque(false);
		if(projectName.compareToIgnoreCase("All Projects") == 0)
		{
			this.allProjects = true;
		}
		else
		{
			this.allProjects = false;
		}
		this.currentProject = projectName;
		this.dataSource = dsName;
		//setBackground(new Color(176, 196, 222));
		Font lblFont =  new Font(Font.SANS_SERIF, Font.BOLD, 11);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		
		gridBagLayout.columnWidths = new int[]{430,430,430,430};
		gridBagLayout.rowHeights = new int[]{210,280 , 280, 280,30};
		gridBagLayout.columnWeights = new double[]{0.25, 0.25, 0.25, 0.25};
		gridBagLayout.rowWeights = new double[]{0.00, 0.30, 0.30, 0.30, 0.10};
		setLayout(gridBagLayout);
		this.setBackground(Color.DARK_GRAY);
		
		entireTopPanel = new JPanel();
		entireTopPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		entireTopPanel.setOpaque(true);
		//entireTopPanel.setBorder(new MatteBorder(0, 2, 0, 0, Constants.COLOR_TOP_PANEL_BACKGROUND));
		entireTopPanel.setPreferredSize(new Dimension(1720,210));
		entireTopPanel.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagConstraints gbc_entireTopPanel = new GridBagConstraints();
		gbc_entireTopPanel.anchor = GridBagConstraints.WEST;
		gbc_entireTopPanel.gridwidth = 4;
		gbc_entireTopPanel.insets = new Insets(0, 0, 0, 0);
		gbc_entireTopPanel.fill = GridBagConstraints.BOTH;
		gbc_entireTopPanel.gridx = 0;
		gbc_entireTopPanel.gridy = 0;
		add(entireTopPanel, gbc_entireTopPanel);
		GridBagLayout gbl_entireTopPanel = new GridBagLayout();
		gbl_entireTopPanel.columnWidths = new int[]{329, 1000, 320};
		gbl_entireTopPanel.rowHeights = new int[]{200, 0};
		gbl_entireTopPanel.columnWeights = new double[]{0.20, 0.60, 0.20};
		//gbl_entireTopPanel.columnWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
		gbl_entireTopPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		entireTopPanel.setLayout(gbl_entireTopPanel);
		
		top_left = new IA_SlidingPanel(7);
		//top_left.setPreferredSize(new Dimension(329,200));
		GridBagConstraints gbc_top_left = new GridBagConstraints();
		gbc_top_left.insets = new Insets(0, 0, 0, 0);
		gbc_top_left.fill = GridBagConstraints.BOTH;
		gbc_top_left.gridx = 0;
		gbc_top_left.gridy = 0;
		entireTopPanel.add(top_left, gbc_top_left);
		top_left.btnNext.addActionListener(this);
		top_left.btnPrev.addActionListener(this);
		top_left.firstSlideBtn.addActionListener(this);
		top_left.secondSlideBtn.addActionListener(this);
		top_left.thirdSlideBtn.addActionListener(this);
		top_left.forthSlideBtn.addActionListener(this);
		top_left.fifthSlideBtn.addActionListener(this);
		top_left.sixthSlideBtn.addActionListener(this);
		top_left.allSlidesBtn.addActionListener(this);
		top_left.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		
		//add top left child panels
		_slide1 = new IA_SlidePanelElement();
		_slide2 = new IA_SlidePanelElement();
		_slide3 = new IA_SlidePanelElement();
		_slide4 = new IA_SlidePanelElement();
		_slide5 = new IA_SlidePanelElement();
		_slide6 = new IA_SlidePanelElement();
		
		_slide1.lblTitle.setText("Total Visits");
		_slide2.lblTitle.setText("Total Users");
		_slide3.lblTitle.setText("Total Screenviews");
		_slide4.lblTitle.setText("Bounce Rate");
		_slide5.lblTitle.setText("Avg Session");
		_slide6.lblTitle.setText("Avg Screens/Visit");
		
		
		//combined slide
		 topSlide = new CombinedTopSlides();
		
		
		timeLine = new JPanel();
		timeLine.setBorder(null);
		timeLine.setPreferredSize(new Dimension(1000,200));
		GridBagConstraints gbc_timeLine = new GridBagConstraints();
		gbc_timeLine.fill = GridBagConstraints.BOTH;
		gbc_timeLine.insets = new Insets(0, 0, 0, 0);
		gbc_timeLine.gridx = 1;
		gbc_timeLine.gridy = 0;
		entireTopPanel.add(timeLine, gbc_timeLine);
		timeLine.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		timeLine.setLayout(new BorderLayout(0, 0));
		
		_timelineChartPanel = new ChartPanel(null);
		_timelineChartPanel.setPreferredSize(new Dimension(1000,200));
		timeLine.add(_timelineChartPanel, BorderLayout.CENTER);
		
		duration = new JPanel();
	
		GridBagConstraints gbc_duration = new GridBagConstraints();
		gbc_duration.fill = GridBagConstraints.HORIZONTAL;
		gbc_duration.gridx = 2;
		gbc_duration.gridy = 0;
		entireTopPanel.add(duration, gbc_duration);
		duration.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagLayout gbl_duration = new GridBagLayout();
		gbl_duration.columnWidths = new int[]{48, 246, 0, 0};
		gbl_duration.rowHeights = new int[]{26, 0};
		gbl_duration.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_duration.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		duration.setLayout(gbl_duration);
		
		comboDuration = new JComboBox<String>();
		comboDuration.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		comboDuration.setBackground(Constants.COLOR_COMBO_BACKGROUND);
		
		comboDuration.setOpaque(true);
		comboDuration.setFocusable(false);
		// comboProjects.setModel(new DefaultComboBoxModel(projects));
		
		comboDuration.setUI(new ComboArrowUI());
		//comboDuration.setBorder(BorderFactory.createLineBorder(Constants.COLOR_COMBO_BACKGROUND, 1, true));
		comboDuration.setActionCommand(Constants.CMD_DURATION_SELECT);
		comboDuration.addActionListener(this);
		comboDuration.setModel(new DefaultComboBoxModel<String>(durations));
		GridBagConstraints gbc_comboDuration = new GridBagConstraints();
		gbc_comboDuration.fill = GridBagConstraints.BOTH;
		gbc_comboDuration.insets = new Insets(0, 0, 0, 5);
		gbc_comboDuration.gridx = 1;
		gbc_comboDuration.gridy = 0;
		duration.add(comboDuration, gbc_comboDuration);
		
		//Overview panel and its contents
		//Overview panel and its contents
				overview = new RoundedPanel();
				overview.setForeground(Constants.COLOR_WHITE_BACKGROUND);
				overview.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				overview.setBorder(new EmptyBorder(0, 10, 10, 10));
				overview.setBackground(new Color(224, 255, 255));
				GridBagConstraints gbc_overview = new GridBagConstraints();
				gbc_overview.gridheight = 2;
				gbc_overview.insets = new Insets(5, 0, 5, 5);
				gbc_overview.fill = GridBagConstraints.BOTH;
				gbc_overview.gridx = 0;
				gbc_overview.gridy = 1;
				add(overview, gbc_overview);
				GridBagLayout gbl_overview = new GridBagLayout();
				gbl_overview.columnWidths = new int[]{199, 0};
				gbl_overview.rowHeights = new int[]{18, 0, 0};
				gbl_overview.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_overview.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
				overview.setLayout(gbl_overview);
				IA_PanelLabel lblOverviewPanel = new IA_PanelLabel("Overview");
				lblOverviewPanel.setVerticalAlignment(SwingConstants.TOP);
				GridBagConstraints gbc_lblOverviewPanel = new GridBagConstraints();
				gbc_lblOverviewPanel.fill = GridBagConstraints.BOTH;
				gbc_lblOverviewPanel.insets = new Insets(0, 0, 0, 0);
				gbc_lblOverviewPanel.gridx = 0;
				gbc_lblOverviewPanel.gridy = 0;
				overview.add(lblOverviewPanel, gbc_lblOverviewPanel);
				
				panel = new JPanel();
				panel.setBackground(Color.WHITE);
				panel.setLayout(new GridLayout(0, 2));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.insets = new Insets(0,0,0,0);
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 1;
				overview.add(panel, gbc_panel);
				lblUsers = new IA_Label("Users");
				panel.add(lblUsers);
				
				txtUsers = new OrangeText();
				panel.add(txtUsers);
				
				
				lblSessions = new IA_Label("Sessions");
				panel.add(lblSessions);
				
				txtSessions = new OrangeText();
				panel.add(txtSessions);
				
				
				lblScreenViews = new IA_Label("Screen Views");
				panel.add(lblScreenViews);
				
				txtScreenViews = new OrangeText();
				panel.add(txtScreenViews);
				
				
				lblScreensbyCurrent = new IA_Label("Screens Per Sessions");
				panel.add(lblScreensbyCurrent);
				lblScreensbyCurrent.setText("Screens / Session");
				
				txtScreensbyCurrent = new OrangeText();
				panel.add(txtScreensbyCurrent);
				
				
				lblActions = new IA_Label("Actions");
				panel.add(lblActions);
				
				txtActions = new OrangeText();
				panel.add(txtActions);
				
				lblBounceRate = new IA_Label("Bounce Rate");
				panel.add(lblBounceRate);
				
				txtBounceRate = new OrangeText();
				panel.add(txtBounceRate);
				
				lblAvgSessionDuration = new IA_Label("Average Session Duration");
				panel.add(lblAvgSessionDuration);
				lblAvgSessionDuration.setText("Avg. Session Duration");
				
				txtAvgSessionDuration = new OrangeText();
				panel.add(txtAvgSessionDuration);
		
		contentPanel = new RoundedPanel();
		contentPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
		contentPanel.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.fill = GridBagConstraints.BOTH;
		gbc_contentPanel.gridheight = 2;
		gbc_contentPanel.insets = new Insets(5, 0, 5, 5);
		gbc_contentPanel.gridx = 1;
		gbc_contentPanel.gridy = 1;
		add(contentPanel, gbc_contentPanel);
		contentPanel.setLayout(new BorderLayout(0, 0));
		IA_PanelLabel lblContentPanel = new IA_PanelLabel("Content");
		lblContentPanel.setVerticalAlignment(SwingConstants.TOP);
		
		contentPanel.add(lblContentPanel, BorderLayout.NORTH);
		content = new JScrollPane();
		content.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(content, BorderLayout.CENTER);
		
		
		activeUsers = new RoundedPanel();
		activeUsers.setBorder(new EmptyBorder(0, 10, 10, 10));
		activeUsers.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		
		GridBagConstraints gbc_activeUsers = new GridBagConstraints();
		gbc_activeUsers.insets = new Insets(5, 0, 5, 5);
		gbc_activeUsers.fill = GridBagConstraints.BOTH;
		gbc_activeUsers.gridx = 2;
		gbc_activeUsers.gridy = 1;
		add(activeUsers, gbc_activeUsers);
		GridBagLayout gbl_activeUsers = new GridBagLayout();
		gbl_activeUsers.columnWidths = new int[]{85, 85, 85};
		gbl_activeUsers.rowHeights = new int[]{20, 30, 100};
		gbl_activeUsers.columnWeights = new double[]{0.33,0.33,0.33};
		gbl_activeUsers.rowWeights = new double[]{0.0, 0.0, 1.0};
		activeUsers.setLayout(gbl_activeUsers);
		
		
		btn1DayActiveUsers = new RoundedButton();
		btn1DayActiveUsers.setName("btn1DayActiveUsers");
		btn1DayActiveUsers.setPreferredSize(new Dimension(85,30));
		
		btn1DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
		btn1DayActiveUsers.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		btn1DayActiveUsers.addMouseListener(this);
		
		JButton btnActiveUsersPanel = new JButton("Active Users");
		btnActiveUsersPanel.setFont(new Font("SansSerif", Font.BOLD, 12));
		btnActiveUsersPanel.setHorizontalTextPosition(JLabel.LEFT);
		btnActiveUsersPanel.setBorder(BorderFactory.createEmptyBorder());
		btnActiveUsersPanel.setFocusPainted(false);
		btnActiveUsersPanel.setActionCommand(Constants.ACTIVE_USER_REPORT);
		btnActiveUsersPanel.addActionListener(this);
		btnActiveUsersPanel.setBackground(Color.WHITE);
		btnActiveUsersPanel.setForeground(Constants.COLOR_GREY_LABEL);
		btnActiveUsersPanel.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblActiveUsersPanel = new GridBagConstraints();
		gbc_lblActiveUsersPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblActiveUsersPanel.gridwidth = 3;
		gbc_lblActiveUsersPanel.insets = new Insets(0, 0, 5, 5);
		gbc_lblActiveUsersPanel.gridx = 0;
		gbc_lblActiveUsersPanel.gridy = 0;
		activeUsers.add(btnActiveUsersPanel, gbc_lblActiveUsersPanel);
		
		
		GridBagConstraints gbc_btn1DayActiveUsers = new GridBagConstraints();
		gbc_btn1DayActiveUsers.fill = GridBagConstraints.BOTH;
		gbc_btn1DayActiveUsers.insets = new Insets(0, 0, 0, 5);
		gbc_btn1DayActiveUsers.gridx = 0;
		gbc_btn1DayActiveUsers.gridy = 1;
		activeUsers.add(btn1DayActiveUsers, gbc_btn1DayActiveUsers);
		
		
		btn7DayActiveUsers = new RoundedButton();
		btn7DayActiveUsers.setPreferredSize(new Dimension(85,30));
		btn7DayActiveUsers.setName("btn7DayActiveUsers");
		btn7DayActiveUsers.addMouseListener(this);
		GridBagConstraints gbc_btn7DayActiveUsers = new GridBagConstraints();
		gbc_btn7DayActiveUsers.fill = GridBagConstraints.BOTH;
		gbc_btn7DayActiveUsers.insets = new Insets(0, 0, 0, 5);
		gbc_btn7DayActiveUsers.gridx = 1;
		gbc_btn7DayActiveUsers.gridy = 1;
		activeUsers.add(btn7DayActiveUsers, gbc_btn7DayActiveUsers);
		
		btn14DayActiveUsers = new RoundedButton();
		btn14DayActiveUsers.setName("btn14DayActiveUsers");
		btn14DayActiveUsers.setPreferredSize(new Dimension(85,30));
		btn14DayActiveUsers.addMouseListener(this);
		
		GridBagConstraints gbc_btn14DayActiveUsers = new GridBagConstraints();
		gbc_btn14DayActiveUsers.fill = GridBagConstraints.BOTH;
		gbc_btn14DayActiveUsers.gridx = 2;
		gbc_btn14DayActiveUsers.gridy = 1;
		activeUsers.add(btn14DayActiveUsers, gbc_btn14DayActiveUsers);
		
		activeUsers_Chart = new JPanel();
		GridBagConstraints gbc_activeUsers_Chart = new GridBagConstraints();
		gbc_activeUsers_Chart.fill = GridBagConstraints.BOTH;
		gbc_activeUsers_Chart.gridx = 0;
		gbc_activeUsers_Chart.gridy = 2;
		gbc_activeUsers_Chart.gridwidth = 3;
		activeUsers.add(activeUsers_Chart, gbc_activeUsers_Chart);
		activeUsers_Chart.setLayout(new BorderLayout());
		
		_AUartPanel = new ChartPanel(null);
		_AUartPanel.setPreferredSize(new Dimension(350, 250));
		activeUsers_Chart.add(_AUartPanel, BorderLayout.CENTER);
		
		freqAndRecency = new RoundedPanel();
		freqAndRecency.setPreferredSize(new Dimension(430,280));
		freqAndRecency.setBorder(new EmptyBorder(0, 10, 10, 10));
		freqAndRecency.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_freqAndRecency = new GridBagConstraints();
		gbc_freqAndRecency.insets = new Insets(5, 0, 5, 0);
		gbc_freqAndRecency.fill = GridBagConstraints.BOTH;
		gbc_freqAndRecency.gridx = 3;
		gbc_freqAndRecency.gridy = 1;
		add(freqAndRecency, gbc_freqAndRecency);
		
		GridBagLayout gbl_freqAndRecency = new GridBagLayout();
		gbl_freqAndRecency.columnWidths = new int[]{60, 60,60,60};
		gbl_freqAndRecency.rowHeights = new int[]{20, 50, 10, 10, 10, 10, 10,10};
		gbl_freqAndRecency.columnWeights = new double[]{0.25, 0.25, 0.25, 0.25};
		gbl_freqAndRecency.rowWeights = new double[]{0.0, 0.0, 1.0, 0.1, 0.1, 0.1, 0.1, 0.1};
		freqAndRecency.setLayout(gbl_freqAndRecency);
		
		IA_PanelLabel lblFreqRecencyPanel = new IA_PanelLabel("Frequency and Recency");
		GridBagConstraints gbc_lblFreqRecencyPanel = new GridBagConstraints();
		gbc_lblFreqRecencyPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblFreqRecencyPanel.insets = new Insets(0, 0, 5, 0);
		gbc_lblFreqRecencyPanel.gridwidth = 4;
		gbc_lblFreqRecencyPanel.anchor = GridBagConstraints.NORTH;
		gbc_lblFreqRecencyPanel.gridx = 0;
		gbc_lblFreqRecencyPanel.gridy = 0;
		freqAndRecency.add(lblFreqRecencyPanel, gbc_lblFreqRecencyPanel);
		
		btnCountOfSessions = new RoundedButton_WideLabel();
		btnCountOfSessions.setName("btnCountOfSessions");
		btnCountOfSessions.setPreferredSize(new Dimension(120,30));
		
		
		btnCountOfSessions.addMouseListener(this);
		GridBagConstraints gbc_btnCountOfSessions = new GridBagConstraints();
		gbc_btnCountOfSessions.fill = GridBagConstraints.BOTH;
		gbc_btnCountOfSessions.gridwidth = 2;
		gbc_btnCountOfSessions.insets = new Insets(0, 0, 5, 5);
		gbc_btnCountOfSessions.gridx = 0;
		gbc_btnCountOfSessions.gridy = 1;
		freqAndRecency.add(btnCountOfSessions, gbc_btnCountOfSessions);
		
		btnDaysSinceLogin = new RoundedButton_WideLabel();
		btnDaysSinceLogin.setPreferredSize(new Dimension(120,50));
		btnDaysSinceLogin.lblUp.setText("Days Since Last Session");
		
		
		btnDaysSinceLogin.lblMiddleLeft.setText(" ");
		btnDaysSinceLogin.lblMiddleRight.setText(" ");
		btnDaysSinceLogin.lblLowerLeft.setText(" ");
		btnDaysSinceLogin.lblLowerRight.setText(" ");
		
		btnDaysSinceLogin.setName("btnDaysSinceLogin");
		btnDaysSinceLogin.addMouseListener(this);
		
		GridBagConstraints gbc_btnDaysSinceLogin = new GridBagConstraints();
		gbc_btnDaysSinceLogin.fill = GridBagConstraints.BOTH;
		gbc_btnDaysSinceLogin.gridwidth = 2;
		gbc_btnDaysSinceLogin.insets = new Insets(0, 0, 5, 0);
		gbc_btnDaysSinceLogin.gridx = 2;
		gbc_btnDaysSinceLogin.gridy = 1;
		freqAndRecency.add(btnDaysSinceLogin, gbc_btnDaysSinceLogin);
		
		freqAndRecencyChart = new JPanel();
		
		freqAndRecencyChart.setForeground(Color.WHITE);
		freqAndRecencyChart.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_freqAndRecencyChart = new GridBagConstraints();
		gbc_freqAndRecencyChart.gridheight = 6;
		gbc_freqAndRecencyChart.gridwidth = 4;
		gbc_freqAndRecencyChart.insets = new Insets(0, 0, 5, 5);
		gbc_freqAndRecencyChart.fill = GridBagConstraints.BOTH;
		gbc_freqAndRecencyChart.gridx = 0;
		gbc_freqAndRecencyChart.gridy = 2;
		freqAndRecency.add(freqAndRecencyChart, gbc_freqAndRecencyChart);
		freqAndRecencyChart.setLayout(new BorderLayout());
		
		
		_freqRecChartPanel = new ChartPanel(null);
		_freqRecChartPanel.setPreferredSize(new Dimension(300, 200));
		freqAndRecencyChart.add(_freqRecChartPanel, BorderLayout.CENTER);
		engageMent = new RoundedPanel();
		engageMent.setPreferredSize(new Dimension(430,280));
		engageMent.setBorder(new EmptyBorder(0, 10, 10, 10));
		engageMent.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		
		GridBagConstraints gbc_engageMent = new GridBagConstraints();
		gbc_engageMent.insets = new Insets(0, 0, 5, 5);
		gbc_engageMent.fill = GridBagConstraints.BOTH;
		gbc_engageMent.gridx = 2;
		gbc_engageMent.gridy = 2;
		add(engageMent, gbc_engageMent);
		
		GridBagLayout gbl_engageMent = new GridBagLayout();
		gbl_engageMent.columnWidths = new int[]{175, 175};
		gbl_engageMent.rowHeights = new int[]{20, 50, 50};
		gbl_engageMent.columnWeights = new double[]{0.5, 0.5};
		gbl_engageMent.rowWeights = new double[]{0.0, 0.0, 1.0};
		engageMent.setLayout(gbl_engageMent);
		IA_PanelLabel lblEngagementPanel = new IA_PanelLabel("Engagement");
		GridBagConstraints gbc_lblEngagementPanel = new GridBagConstraints();
		gbc_lblEngagementPanel.gridwidth = 2;
		gbc_lblEngagementPanel.anchor = GridBagConstraints.NORTH;
		gbc_lblEngagementPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblEngagementPanel.insets = new Insets(0, 0, 5, 0);
		gbc_lblEngagementPanel.gridx = 0;
		gbc_lblEngagementPanel.gridy = 0;
		engageMent.add(lblEngagementPanel, gbc_lblEngagementPanel);
		
		btnEngageDuration = new RoundedButton_WideLabel();
		btnEngageDuration.setName("btnEngageDuration");
		btnEngageDuration.addMouseListener(this);
		GridBagConstraints gbc_btnEngageDuration = new GridBagConstraints();
		gbc_btnEngageDuration.fill = GridBagConstraints.BOTH;
		gbc_btnEngageDuration.insets = new Insets(0, 0, 5, 5);
		gbc_btnEngageDuration.gridx = 0;
		gbc_btnEngageDuration.gridy = 1;
		String valSessions = "0";
		String valScreens = "0";
		String btnEngageDurationString = "<html>"
				+ "<body>"
				+ "<table><tr><td span = \"2\" align = \"center\">"
				+ "Duration </td></tr><tr><td align = \"left\"> <font size = \"3\"><b>"+ valSessions +"</b></font><br>"
				+ "<font size = \"2\">Sessions</font>"
				+ "</td><td align = \"left\"> <font size = \"3\"><b>"+ valScreens +"</b></font><br>"
				+ "<font size = \"2\">Screen Views</font>"
				+ "</td></tr></table></body></html>";
		//btnEngageDuration.setText(btnEngageDurationString);
	
		btnEngageDuration.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
		btnEngageDuration.setPreferredSize(new Dimension(120,30));
		engageMent.add(btnEngageDuration, gbc_btnEngageDuration);
		
		btnEngageDepth = new RoundedButton_WideLabel();
		btnEngageDepth.setName("btnEngageDepth");
		btnEngageDepth.setPreferredSize(new Dimension(120,30));
		btnEngageDepth.lblUp.setText("Screen Depth");
		btnEngageDepth.lblMiddleLeft.setText(" ");
		btnEngageDepth.lblMiddleRight.setText(" ");
		btnEngageDepth.lblLowerLeft.setText(" ");
		btnEngageDepth.lblLowerRight.setText(" ");
		GridBagConstraints gbc_btnEngageDepth = new GridBagConstraints();
		gbc_btnEngageDepth.fill = GridBagConstraints.BOTH;
		gbc_btnEngageDepth.insets = new Insets(0, 0, 5, 0);
		gbc_btnEngageDepth.gridx = 1;
		gbc_btnEngageDepth.gridy = 1;
		btnEngageDepth.addMouseListener(this);
		
		engageMent.add(btnEngageDepth, gbc_btnEngageDepth);
		
		engGraphPanel = new JPanel();
		GridBagConstraints gbc_engGraphPanel = new GridBagConstraints();
		gbc_engGraphPanel.gridwidth = 2;
		gbc_engGraphPanel.insets = new Insets(0, 0, 0, 5);
		gbc_engGraphPanel.fill = GridBagConstraints.BOTH;
		gbc_engGraphPanel.gridx = 0;
		gbc_engGraphPanel.gridy = 2;
		engageMent.add(engGraphPanel, gbc_engGraphPanel);
		engGraphPanel.setLayout(new BorderLayout(0, 0));
		engGraphPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		
		 engChartPanel = new ChartPanel(null);
		 engChartPanel.setPreferredSize(new Dimension(300,200));
		 engChartPanel.setBorder(BorderFactory.createEmptyBorder());
		 engGraphPanel.add(engChartPanel, BorderLayout.CENTER);
		alarms = new RoundedPanel();
		alarms.setBorder(new EmptyBorder(0, 10, 10, 10));
		alarms.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		alarms.setPreferredSize(new Dimension(425,260));
		GridBagConstraints gbc_alarms = new GridBagConstraints();
		gbc_alarms.insets = new Insets(0, 0, 5, 0);
		gbc_alarms.fill = GridBagConstraints.BOTH;
		gbc_alarms.gridx = 3;
		gbc_alarms.gridy = 2;
		add(alarms, gbc_alarms);
		GridBagLayout gbl_alarms = new GridBagLayout();
		gbl_alarms.columnWidths = new int[]{68, 68, 68,68,68,68};
		gbl_alarms.rowHeights = new int[]{18,20, 0,20,20};
		gbl_alarms.columnWeights = new double[]{Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE};
		gbl_alarms.rowWeights = new double[]{0.0, 0.4, 0.0, 0.2, 0.2};
		alarms.setLayout(gbl_alarms);
		
		IA_PanelLabel lblAlarmsPanel = new IA_PanelLabel("Alarms");
		GridBagConstraints gbc_lblAlarmsPanel = new GridBagConstraints();
		gbc_lblAlarmsPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAlarmsPanel.gridwidth = 6;
		gbc_lblAlarmsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_lblAlarmsPanel.anchor = GridBagConstraints.NORTH;
		gbc_lblAlarmsPanel.gridx = 0;
		gbc_lblAlarmsPanel.gridy = 0;
		alarms.add(lblAlarmsPanel, gbc_lblAlarmsPanel );
		
		//get the alarms Image Icon
		ImageIcon alarmsIcon = new ImageIcon(getClass().getResource("Alarm-with-Blue-Ringer.png"));
		Image newAlarmImg = alarmsIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		ImageIcon _newAlarmIcon = new ImageIcon(newAlarmImg);
		
		
		lblIconMedium = new JButton("Medium");
		lblIconMedium.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIconMedium.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		lblIconMedium.setBorder(new LineBorder(Color.WHITE));
		lblIconMedium.setFocusPainted(false);
		lblIconMedium.setVerticalTextPosition(SwingConstants.BOTTOM);
		lblIconMedium.setHorizontalTextPosition(SwingConstants.CENTER);
		GridBagConstraints gbc_lblIconMedium = new GridBagConstraints();
		gbc_lblIconMedium.fill = GridBagConstraints.BOTH;
		gbc_lblIconMedium.insets = new Insets(0, 0, 5, 0);
		gbc_lblIconMedium.gridx = 0;
		gbc_lblIconMedium.gridy = 1;
		lblIconMedium.setIcon(_newAlarmIcon);
		alarms.add(lblIconMedium, gbc_lblIconMedium);
		
		lblValueMedium = new JLabel("");
		lblValueMedium.setVerticalTextPosition(SwingConstants.TOP);
		lblValueMedium.setVerticalAlignment(SwingConstants.TOP);
		lblValueMedium.setHorizontalAlignment(SwingConstants.LEFT);
		lblValueMedium.setFont(new Font("SansSerif", Font.BOLD, 20));
		GridBagConstraints gbc_lblValueMedium = new GridBagConstraints();
		gbc_lblValueMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblValueMedium.insets = new Insets(0, 0, 17, 0);
		gbc_lblValueMedium.gridx = 1;
		gbc_lblValueMedium.gridy = 1;
		alarms.add(lblValueMedium, gbc_lblValueMedium);
		
		lblIconHigh = new JButton("High");
		lblIconHigh.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIconHigh.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		
		lblIconHigh.setBorder(new LineBorder(Color.WHITE));
		lblIconHigh.setFocusPainted(false);
		lblIconHigh.setVerticalTextPosition(SwingConstants.BOTTOM);
		lblIconHigh.setHorizontalTextPosition(SwingConstants.CENTER);
		GridBagConstraints gbc_lblIconHigh = new GridBagConstraints();
		gbc_lblIconHigh.fill = GridBagConstraints.BOTH;
		gbc_lblIconHigh.insets = new Insets(0, 0, 5, 0);
		gbc_lblIconHigh.gridx = 2;
		gbc_lblIconHigh.gridy = 1;
		lblIconHigh.setIcon(_newAlarmIcon);
		alarms.add(lblIconHigh, gbc_lblIconHigh);
		
		lblValueHigh = new JLabel("");
		lblValueHigh.setVerticalAlignment(SwingConstants.TOP);
		lblValueHigh.setHorizontalAlignment(SwingConstants.LEFT);
		lblValueHigh.setVerticalTextPosition(SwingConstants.TOP);
		lblValueHigh.setFont(new Font("SansSerif", Font.BOLD, 20));
		GridBagConstraints gbc_lblValueHigh = new GridBagConstraints();
		gbc_lblValueHigh.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblValueHigh.insets = new Insets(0, 0, 17, 0);
		gbc_lblValueHigh.gridx = 3;
		gbc_lblValueHigh.gridy = 1;
		alarms.add(lblValueHigh, gbc_lblValueHigh);
		
		lblIconCritical = new JButton("Critical");
	
		lblIconCritical.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIconCritical.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		lblIconCritical.setBorder(new LineBorder(Color.WHITE));
		lblIconCritical.setFocusPainted(false);
		lblIconCritical.setVerticalTextPosition(SwingConstants.BOTTOM);
		lblIconCritical.setHorizontalTextPosition(SwingConstants.CENTER);
		GridBagConstraints gbc_lblIconCritical = new GridBagConstraints();
		gbc_lblIconCritical.fill = GridBagConstraints.BOTH;
		gbc_lblIconCritical.insets = new Insets(0, 0, 5, 0);
		gbc_lblIconCritical.gridx = 4;
		gbc_lblIconCritical.gridy = 1;
		lblIconCritical.setIcon(_newAlarmIcon);
		alarms.add(lblIconCritical, gbc_lblIconCritical);
		
		lblValueCritical = new JLabel("");
		lblValueCritical.setVerticalAlignment(SwingConstants.TOP);
		lblValueCritical.setHorizontalAlignment(SwingConstants.LEFT);
		lblValueCritical.setVerticalTextPosition(SwingConstants.TOP);
		lblValueCritical.setFont(new Font("SansSerif", Font.BOLD, 20));
		GridBagConstraints gbc_lblValueCritical = new GridBagConstraints();
		gbc_lblValueCritical.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblValueCritical.insets = new Insets(0, 0, 17, 0);
		gbc_lblValueCritical.gridx = 5;
		gbc_lblValueCritical.gridy = 1;
		alarms.add(lblValueCritical, gbc_lblValueCritical);
		
		
		
		lblIconDividingLine = new JButton("");
		
		lblIconDividingLine.setHorizontalAlignment(AbstractButton.CENTER);
		lblIconDividingLine.setHorizontalTextPosition(AbstractButton.LEFT);
		lblIconDividingLine.setVerticalTextPosition(AbstractButton.BOTTOM);
		//lblIconDividingLine.setIconTextGap(0);
		lblIconDividingLine.setOpaque(false);
		lblIconDividingLine.setVerticalAlignment(SwingConstants.TOP);
	
		lblIconDividingLine.setBorderPainted(false);
		lblIconDividingLine.setFocusPainted(false);
		
		lblIconDividingLine.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblIconDividingLine.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		lblIconDividingLine.setBorder(BorderFactory.createEmptyBorder());
		
		GridBagConstraints gbc_lblIconDividingLine = new GridBagConstraints();
		gbc_lblIconDividingLine.fill = GridBagConstraints.BOTH;
		gbc_lblIconDividingLine.gridwidth = 6;
		gbc_lblIconDividingLine.insets = new Insets(0, 0, 0, 0);
		gbc_lblIconDividingLine.gridx = 0;
		gbc_lblIconDividingLine.gridy = 2;
		lblIconDividingLine.setIcon(icondividingLine);
		alarms.add(lblIconDividingLine, gbc_lblIconDividingLine);
		
		lblAverageTimeTo = new JLabel("Average Time To Clear Alarms");
		GridBagConstraints gbc_lblAverageTimeTo = new GridBagConstraints();
		gbc_lblAverageTimeTo.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblAverageTimeTo.gridwidth = 3;
		gbc_lblAverageTimeTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblAverageTimeTo.gridx = 0;
		gbc_lblAverageTimeTo.gridy = 3;
		alarms.add(lblAverageTimeTo, gbc_lblAverageTimeTo);
		
		lblTimeToClrMedium = new JLabel("");
		lblTimeToClrMedium.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblTimeToClrMedium = new GridBagConstraints();
		gbc_lblTimeToClrMedium.gridwidth = 2;
		gbc_lblTimeToClrMedium.insets = new Insets(0, 0, 0, 5);
		gbc_lblTimeToClrMedium.gridx = 0;
		gbc_lblTimeToClrMedium.gridy = 4;
		alarms.add(lblTimeToClrMedium, gbc_lblTimeToClrMedium);
		
		lblTimeToClrHigh = new JLabel("");
		lblTimeToClrHigh.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblTimeToClrHigh = new GridBagConstraints();
		gbc_lblTimeToClrHigh.gridwidth = 2;
		gbc_lblTimeToClrHigh.insets = new Insets(0, 0, 0, 5);
		gbc_lblTimeToClrHigh.gridx = 2;
		gbc_lblTimeToClrHigh.gridy = 4;
		alarms.add(lblTimeToClrHigh, gbc_lblTimeToClrHigh);
		
		lblTimeToClrCritical = new JLabel("");
		lblTimeToClrCritical.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblTimeToClrCritical = new GridBagConstraints();
		gbc_lblTimeToClrCritical.gridwidth = 2;
		gbc_lblTimeToClrCritical.gridx = 4;
		gbc_lblTimeToClrCritical.gridy = 4;
		alarms.add(lblTimeToClrCritical, gbc_lblTimeToClrCritical);
		
		y_overview = new RoundedPanel();
		y_overview.setBorder(new EmptyBorder(0, 10, 0, 10));
		y_overview.setForeground(Constants.COLOR_WHITE_BACKGROUND);
	//	y_overview.setPreferredSize(new Dimension(419,280));
		GridBagConstraints gbc_y_overview = new GridBagConstraints();
		gbc_y_overview.insets = new Insets(0, 0, 0, 5); 
		gbc_y_overview.fill = GridBagConstraints.BOTH;
		gbc_y_overview.gridx = 0;
		gbc_y_overview.gridy = 3;
		add(y_overview, gbc_y_overview);
	
		GridBagLayout gbl_y_overview = new GridBagLayout();
		gbl_y_overview.columnWidths = new int[]{10, 20, 20, 20, 10};
		gbl_y_overview.rowHeights = new int[]{10, 0, 10, 10, 10, 10, 10, 10,10, 10};
		gbl_y_overview.columnWeights = new double[]{0.2, 0.2, 0.2, 0.2, 0.2};
		gbl_y_overview.rowWeights = new double[]{0.0, 0.2, 0.1, 0.1, 0.1, 0.1, 0.1,0.1, 0.1};
		y_overview.setLayout(gbl_y_overview);
		
		IA_PanelLabel lblYdayOverviewPanel = new IA_PanelLabel("Yesterday's Overview");
		GridBagConstraints gbc_lblYdayOverviewPanel = new GridBagConstraints();
		gbc_lblYdayOverviewPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblYdayOverviewPanel.gridwidth = 5;
		gbc_lblYdayOverviewPanel.insets = new Insets(0, 0, 5, 5);
		gbc_lblYdayOverviewPanel.gridx = 0;
		gbc_lblYdayOverviewPanel.gridy = 0;
		gbc_lblYdayOverviewPanel.gridwidth=3;
		
		y_overview.add(lblYdayOverviewPanel, gbc_lblYdayOverviewPanel);
		
		valNewUsers = new JLabel("");
		valNewUsers.setFont(new Font("SansSerif", Font.BOLD, 18));
		GridBagConstraints gbc_valNewUsers = new GridBagConstraints();
		gbc_valNewUsers.fill = GridBagConstraints.VERTICAL;
		gbc_valNewUsers.insets = new Insets(0, 0, 5, 5);
		gbc_valNewUsers.gridx = 3;
		gbc_valNewUsers.gridy = 1;
		y_overview.add(valNewUsers, gbc_valNewUsers);
		
		
		valyUsers = new OrangeText();
		valyUsers.setFont(new Font("SansSerif", Font.BOLD, 18));
		valyUsers.setForeground(Color.BLACK);
		GridBagConstraints gbc_valyUsers = new GridBagConstraints();
		gbc_valyUsers.fill = GridBagConstraints.VERTICAL;
		gbc_valyUsers.insets = new Insets(0, 0, 5, 5);
		gbc_valyUsers.anchor = GridBagConstraints.WEST;
		gbc_valyUsers.gridx = 1;
		gbc_valyUsers.gridy = 1;
		y_overview.add(valyUsers, gbc_valyUsers);
		
		
		
		lblyUser = new JLabel("Users");
		//lblyUser.setFont(new Font("SansSerif", Font.BOLD, 11));
		GridBagConstraints gbc_lblyUser = new GridBagConstraints();
		gbc_lblyUser.fill = GridBagConstraints.VERTICAL;
		gbc_lblyUser.anchor = GridBagConstraints.WEST;
		gbc_lblyUser.insets = new Insets(0, 0, 5, 5);
		gbc_lblyUser.gridx = 1;
		gbc_lblyUser.gridy = 2;
		y_overview.add(lblyUser, gbc_lblyUser);
		
		lblIconUsers = new JLabel("");
		//set the icon
		
		ImageIcon iconUsers = new ImageIcon(getClass().getResource("ydayOverviewUsers.png"));
		Image newImgUsers = iconUsers.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		lblIconUsers.setIcon(new ImageIcon(newImgUsers));
		
		GridBagConstraints gbc_lblIconUsers = new GridBagConstraints();
		gbc_lblIconUsers.gridheight = 2;
		gbc_lblIconUsers.insets = new Insets(0, 0, 5, 5);
		gbc_lblIconUsers.gridx = 2;
		gbc_lblIconUsers.gridy = 1;
		y_overview.add(lblIconUsers, gbc_lblIconUsers);
		
		lblIconSessions = new JLabel("");
		ImageIcon iconSessions = new ImageIcon(getClass().getResource("ydayOverviewSessions.png"));
		Image newImgSessions = iconSessions.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		
		lblYDayDivide1 = new JLabel("");
		GridBagConstraints gbc_lblYDayDivide1 = new GridBagConstraints();
		gbc_lblYDayDivide1.fill = GridBagConstraints.BOTH;
		gbc_lblYDayDivide1.gridwidth = 3;
		gbc_lblYDayDivide1.insets = new Insets(0, 0, 7, 0);
		gbc_lblYDayDivide1.gridx = 1;
		gbc_lblYDayDivide1.gridy = 3;
		lblYDayDivide1.setIcon(ydayDividingLine);
		y_overview.add(lblYDayDivide1, gbc_lblYDayDivide1);
		
		
		lblIconSessions.setIcon(new ImageIcon(newImgSessions));
		
		GridBagConstraints gbc_lblIconSessions = new GridBagConstraints();
		gbc_lblIconSessions.insets = new Insets(0, 0, 5, 5);
		gbc_lblIconSessions.gridheight = 2;
		gbc_lblIconSessions.gridx = 2;
		gbc_lblIconSessions.gridy = 4;
		y_overview.add(lblIconSessions, gbc_lblIconSessions);
		
		lblNewUsers = new JLabel("New");
		GridBagConstraints gbc_lblNewUsers = new GridBagConstraints();
		gbc_lblNewUsers.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewUsers.gridx = 3;
		gbc_lblNewUsers.gridy = 2;
		y_overview.add(lblNewUsers, gbc_lblNewUsers);
		
		valySessions = new OrangeText();
		valySessions.setFont(new Font("SansSerif", Font.BOLD, 18));
		valySessions.setForeground(Color.BLACK);
		GridBagConstraints gbc_valySessions = new GridBagConstraints();
		gbc_valySessions.fill = GridBagConstraints.VERTICAL;
		gbc_valySessions.insets = new Insets(0, 0, 5, 5);
		gbc_valySessions.anchor = GridBagConstraints.WEST;
		gbc_valySessions.gridx = 1;
		gbc_valySessions.gridy = 4;
		y_overview.add(valySessions, gbc_valySessions);
		
		lblySessions = new JLabel("Sessions");
		//lblySessions.setFont(new Font("SansSerif", Font.BOLD, 11));
		GridBagConstraints gbc_lblySessions = new GridBagConstraints();
		gbc_lblySessions.fill = GridBagConstraints.VERTICAL;
		gbc_lblySessions.anchor = GridBagConstraints.WEST;
		gbc_lblySessions.insets = new Insets(0, 0, 5, 5);
		gbc_lblySessions.gridx = 1;
		gbc_lblySessions.gridy = 5;
		y_overview.add(lblySessions, gbc_lblySessions);
		
		lblIconActions = new JLabel("");
		ImageIcon iconActions = new ImageIcon(getClass().getResource("ydayOverviewActions.png"));
		Image newImgActions = iconActions.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		
		lblyDayDivide2 = new JLabel("");
		GridBagConstraints gbc_lblyDayDivide2 = new GridBagConstraints();
		gbc_lblyDayDivide2.fill = GridBagConstraints.BOTH;
		gbc_lblyDayDivide2.gridwidth = 3;
		gbc_lblyDayDivide2.insets = new Insets(0, 0, 5, 5);
		gbc_lblyDayDivide2.gridx = 1;
		gbc_lblyDayDivide2.gridy = 6;
		lblyDayDivide2.setIcon(ydayDividingLine);
		y_overview.add(lblyDayDivide2, gbc_lblyDayDivide2);
		
		lblIconActions.setIcon(new ImageIcon(newImgActions));
		GridBagConstraints gbc_lblIconActions = new GridBagConstraints();
		gbc_lblIconActions.insets = new Insets(0, 0, 5, 5);
		gbc_lblIconActions.gridheight = 2;
		gbc_lblIconActions.gridx = 2;
		gbc_lblIconActions.gridy = 7;
		y_overview.add(lblIconActions, gbc_lblIconActions);
		
		valYActions = new OrangeText();
		valYActions.setFont(new Font("SansSerif", Font.BOLD, 18));
		valYActions.setForeground(Color.BLACK);
		GridBagConstraints gbc_valYActions = new GridBagConstraints();
		gbc_valYActions.fill = GridBagConstraints.VERTICAL;
		gbc_valYActions.insets = new Insets(0, 0, 5, 5);
		gbc_valYActions.anchor = GridBagConstraints.WEST;
		gbc_valYActions.gridx = 1;
		gbc_valYActions.gridy = 7;
		y_overview.add(valYActions, gbc_valYActions);
		
				
				lblYActions = new JLabel("Actions");
				//lblYActions.setFont(new Font("SansSerif", Font.BOLD, 11));
				GridBagConstraints gbc_lblYActions = new GridBagConstraints();
				gbc_lblYActions.anchor = GridBagConstraints.WEST;
				gbc_lblYActions.insets = new Insets(0, 0, 5, 5);
				gbc_lblYActions.gridx = 1;
				gbc_lblYActions.gridy = 8;
				y_overview.add(lblYActions, gbc_lblYActions);
				
				valAvgSession = new JLabel("");
				valAvgSession.setFont(new Font("SansSerif", Font.BOLD, 18));
				GridBagConstraints gbc_valAvgSession = new GridBagConstraints();
				gbc_valAvgSession.fill = GridBagConstraints.VERTICAL;
				gbc_valAvgSession.insets = new Insets(0, 0, 5, 5);
				gbc_valAvgSession.gridx = 3;
				gbc_valAvgSession.gridy = 4;
				y_overview.add(valAvgSession, gbc_valAvgSession);
				
				JLabel lblavgSesion = new JLabel("Avg. Session");
				GridBagConstraints gbc_lblavgSesion = new GridBagConstraints();
				gbc_lblavgSesion.insets = new Insets(0, 0, 5, 5);
				gbc_lblavgSesion.gridx = 3;
				gbc_lblavgSesion.gridy = 5;
				y_overview.add(lblavgSesion, gbc_lblavgSesion);		
				
		
				valActionsPerSession = new JLabel("");
				valActionsPerSession.setFont(new Font("SansSerif", Font.BOLD, 18));
				GridBagConstraints gbc_ActionsPerSession = new GridBagConstraints();
				gbc_ActionsPerSession.fill = GridBagConstraints.VERTICAL;
				gbc_ActionsPerSession.insets = new Insets(0, 0, 5, 5);
				gbc_ActionsPerSession.gridx = 3;
				gbc_ActionsPerSession.gridy = 7;
				y_overview.add(valActionsPerSession, gbc_ActionsPerSession);
				
				JLabel lblActionPerSesion = new JLabel("Actions/Session");
				GridBagConstraints gbc_lblActionPerSesion = new GridBagConstraints();
				gbc_lblActionPerSesion.insets = new Insets(0, 0, 5, 5);
				gbc_lblActionPerSesion.gridx = 3;
				gbc_lblActionPerSesion.gridy = 8;
				y_overview.add(lblActionPerSesion, gbc_lblActionPerSesion);
		
		reportsAndTrends = new RoundedPanel();
		reportsAndTrends.setBorder(new EmptyBorder(0, 10, 0, 10));
		reportsAndTrends.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		reportsAndTrends.setPreferredSize(new Dimension(419, 280));
		GridBagConstraints gbc_reportsAndTrends = new GridBagConstraints();
		gbc_reportsAndTrends.insets = new Insets(0, 0, 0, 5);
		gbc_reportsAndTrends.fill = GridBagConstraints.BOTH;
		gbc_reportsAndTrends.gridx = 1;
		gbc_reportsAndTrends.gridy = 3;
		add(reportsAndTrends, gbc_reportsAndTrends);
		GridBagLayout gbl_reportsAndTrends = new GridBagLayout();
		gbl_reportsAndTrends.columnWidths = new int[]{213, 0};
		gbl_reportsAndTrends.rowHeights = new int[] {10, 90, 120, 50};
		gbl_reportsAndTrends.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_reportsAndTrends.rowWeights = new double[]{0.0, 0.0,0.0, 1.0};
		reportsAndTrends.setLayout(gbl_reportsAndTrends);
		
		IA_PanelLabel lblRptsAndTrndsPanel = new IA_PanelLabel("Devices");
		GridBagConstraints gbc_lblRptsAndTrndsPanel = new GridBagConstraints();
		gbc_lblRptsAndTrndsPanel.insets = new Insets(0, 0, 0, 0);
		gbc_lblRptsAndTrndsPanel.anchor = GridBagConstraints.NORTH;
		gbc_lblRptsAndTrndsPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblRptsAndTrndsPanel.gridx = 0;
		gbc_lblRptsAndTrndsPanel.gridy = 0;
		reportsAndTrends.add(lblRptsAndTrndsPanel, gbc_lblRptsAndTrndsPanel);
		
		panel_1 = new JPanel();
		panel_1.setBorder(null);
		panel_1.setPreferredSize(new Dimension(419, 90));
		panel_1.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 0, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		reportsAndTrends.add(panel_1, gbc_panel_1);
		panel_1.setLayout(new GridLayout(1, 2));
		
		ImageIcon devicePC = new ImageIcon(getClass().getResource("Desktop-Monitor.png"));
		Image devicePCImg = devicePC.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
		ImageIcon pcIcon = new ImageIcon(devicePCImg);
		
		ImageIcon devicePhone = new ImageIcon(getClass().getResource("Mobile.png"));
		Image devicePhoneImg = devicePhone.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		ImageIcon phoneIcon = new ImageIcon(devicePhoneImg);
		
		JLabel pcPicLabel = new JLabel(pcIcon);
		JLabel phonePicLabel = new JLabel(phoneIcon);
		
		realTimeDeviceButton = new RealTimeDeviceButton();
		realTimeDeviceButton.lblBoldText.setForeground(Color.BLACK);
		realTimeDeviceButton.setBackground(Color.WHITE);
		realTimeDeviceButton.imgPanel.setBackground(Color.WHITE);
//		GridBagLayout gridBagLayout_1 = (GridBagLayout) realTimeDeviceButton.getLayout();
//		gridBagLayout_1.rowWeights = new double[]{1.0, 0.0, 0.0};
//		gridBagLayout_1.rowHeights = new int[]{30, 22, 10};
//		gridBagLayout_1.columnWeights = new double[]{4.9E-324};
//		gridBagLayout_1.columnWidths = new int[]{10};
		realTimeDeviceButton.lblNormalText.setText("Desktop");
		realTimeDeviceButton.lblNormalText.setForeground(Color.BLACK);
		realTimeDeviceButton.lblBoldText.setText("0");
		realTimeDeviceButton.lblBoldText.setFont(new Font("Tahoma", Font.BOLD, 22));
		realTimeDeviceButton.setName("DESKTOP_BTN");
		realTimeDeviceButton.setForeground(Color.BLACK);
		realTimeDeviceButton.imgPanel.add(pcPicLabel);
		//realTimeDeviceButton.imgPanel.add(comp);
		panel_1.add(realTimeDeviceButton);
		
		realTimeDeviceButton_1 = new RealTimeDeviceButton();
		realTimeDeviceButton_1.setBackground(Color.WHITE);
		realTimeDeviceButton_1.lblBoldText.setForeground(Color.BLACK);
		realTimeDeviceButton_1.setForeground(Color.BLACK);
		realTimeDeviceButton_1.imgPanel.setBackground(Color.WHITE);
//		GridBagLayout gridBagLayout_2 = (GridBagLayout) realTimeDeviceButton_1.getLayout();
//		gridBagLayout_2.rowWeights = new double[]{1.0, 0.0, 0.0};
//		gridBagLayout_2.rowHeights = new int[]{30, 22, 10};
//		gridBagLayout_2.columnWeights = new double[]{4.9E-324};
//		gridBagLayout_2.columnWidths = new int[]{10};
		realTimeDeviceButton_1.lblNormalText.setText("Mobile");
		realTimeDeviceButton_1.lblNormalText.setForeground(Color.BLACK);
		realTimeDeviceButton_1.lblBoldText.setText("0");
		realTimeDeviceButton_1.lblBoldText.setFont(new Font("Tahoma", Font.BOLD, 22));
		//realTimeDeviceButton_1.setPreferredSize(new Dimension(40, 40));
		realTimeDeviceButton_1.setName("MOBILE_BTN");
		//realTimeDeviceButton_1.lblBoldText.setIcon(phoneIcon);;
		realTimeDeviceButton_1.imgPanel.add(phonePicLabel);
		panel_1.add(realTimeDeviceButton_1);
		
		panel_2 = new JPanel();
		panel_2.setBackground(new Color(250, 253, 255));
		panel_2.setPreferredSize(new Dimension(419,120));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 0, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		reportsAndTrends.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{28, 150, 60, 150, 28};
		gbl_panel_2.rowHeights = new int[]{120};
		gbl_panel_2.columnWeights = new double[]{0.33, 0.0, 0.33, 0.0, 0.33};
		gbl_panel_2.rowWeights = new double[]{1.0};
		panel_2.setLayout(gbl_panel_2);
		
		chartPanel_1 = new JPanel();
		chartPanel_1.setPreferredSize(new Dimension(120, 120));
		chartPanel_1.setBorder(BorderFactory.createEmptyBorder());
		chartPanel_1.setBackground(new Color(250, 253, 255));
		chartPanel_1.setLayout(new BorderLayout());
		GridBagConstraints gbc_chartPanel_1 = new GridBagConstraints();
		gbc_chartPanel_1.fill = GridBagConstraints.BOTH;
		gbc_chartPanel_1.insets = new Insets(0, 0, 0, 0);
		gbc_chartPanel_1.gridx = 1;
		gbc_chartPanel_1.gridy = 0;
		panel_2.add(chartPanel_1, gbc_chartPanel_1);
		
		deviceChartPanel = new ChartPanel(null);
		chartPanel_1.add(deviceChartPanel);
		
		chartPanel_2 = new JPanel();
		chartPanel_2.setPreferredSize(new Dimension(120, 120));
		chartPanel_2.setBorder(BorderFactory.createEmptyBorder());
		chartPanel_2.setBackground(new Color(250, 253, 255));
		chartPanel_2.setLayout(new BorderLayout());
		GridBagConstraints gbc_chartPanel_2 = new GridBagConstraints();
		gbc_chartPanel_2.fill = GridBagConstraints.BOTH;
		gbc_chartPanel_2.insets = new Insets(0, 0, 0, 0);
		gbc_chartPanel_2.gridx = 3;
		gbc_chartPanel_2.gridy = 0;
		panel_2.add(chartPanel_2, gbc_chartPanel_2);
		
		browserChartPanel = new ChartPanel(null);
		chartPanel_2.add(browserChartPanel, BorderLayout.CENTER);
		
		panel_3 = new JPanel();
		panel_3.setBackground(new Color(250, 253, 255));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 3;
		reportsAndTrends.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{50, 25, 25, 25, 25, 50, 50, 25, 25, 25, 25, 50};
		gbl_panel_3.rowHeights = new int[]{15, 15};
		gbl_panel_3.columnWeights = new double[]{0.25, 0.0, 0.0, 0.0, 0.0, 0.25, 0.25, 0.0, 0.0, 0.0, 0.0, 0.25};
		gbl_panel_3.rowWeights = new double[]{0.5, 0.5};
		panel_3.setLayout(gbl_panel_3);
		
		
	         
		 		
		
		darkBlueLbl = new JLabel();
		darkBlueLbl.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_darkBlueLbl = new GridBagConstraints();
		gbc_darkBlueLbl.insets = new Insets(1, 1, 1, 1);
		gbc_darkBlueLbl.gridx = 2;
		gbc_darkBlueLbl.gridy = 0;
		panel_3.add(darkBlueLbl, gbc_darkBlueLbl);
		
		lightBlueLbl = new JLabel("");
		lightBlueLbl.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lightBlueLbl = new GridBagConstraints();
		gbc_lightBlueLbl.insets = new Insets(1, 1, 1, 1);
		gbc_lightBlueLbl.gridx = 3;
		gbc_lightBlueLbl.gridy = 0;
		panel_3.add(lightBlueLbl, gbc_lightBlueLbl);
		
		darkerBlueLbl = new JLabel();
		GridBagConstraints gbc_darkerBlueLbl = new GridBagConstraints();
		gbc_darkerBlueLbl.insets = new Insets(1, 1, 1, 1);;
		gbc_darkerBlueLbl.gridx = 1;
		gbc_darkerBlueLbl.gridy = 0;
		panel_3.add(darkerBlueLbl, gbc_darkerBlueLbl);
		
		lighterBlueLbl = new JLabel("");
		GridBagConstraints gbc_lighterBlueLbl = new GridBagConstraints();
		gbc_lighterBlueLbl.insets = new Insets(1, 1, 1, 1);;
		gbc_lighterBlueLbl.gridx = 4;
		gbc_lighterBlueLbl.gridy = 0;
		panel_3.add(lighterBlueLbl, gbc_lighterBlueLbl);
		
		
		darkOrnageLbl = new JLabel("");
		GridBagConstraints gbc_darkOrnageLbl = new GridBagConstraints();
		gbc_darkOrnageLbl.insets = new Insets(1, 1, 1, 1);
		gbc_darkOrnageLbl.gridx = 8;
		gbc_darkOrnageLbl.gridy = 0;
		panel_3.add(darkOrnageLbl, gbc_darkOrnageLbl);
		
		lightOrangeLbl = new JLabel("");
		GridBagConstraints gbc_lightOrangeLbl = new GridBagConstraints();
		gbc_lightOrangeLbl.insets = new Insets(1, 1, 1, 1);
		gbc_lightOrangeLbl.gridx = 9;
		gbc_lightOrangeLbl.gridy = 0;
		panel_3.add(lightOrangeLbl, gbc_lightOrangeLbl);
		
		darkerOrnageLbl = new JLabel("");
		GridBagConstraints gbc_darkerOrnageLbl = new GridBagConstraints();
		gbc_darkerOrnageLbl.insets = new Insets(1, 1,1, 1);
		gbc_darkerOrnageLbl.gridx = 7;
		gbc_darkerOrnageLbl.gridy = 0;
		panel_3.add(darkerOrnageLbl, gbc_darkerOrnageLbl);
		
		lighterOrangeLbl = new JLabel("");
		GridBagConstraints gbc_lighterOrangeLbl = new GridBagConstraints();
		gbc_lighterOrangeLbl.insets = new Insets(1, 1, 1, 1);
		gbc_lighterOrangeLbl.gridx = 10;
		gbc_lighterOrangeLbl.gridy = 0;
		panel_3.add(lighterOrangeLbl, gbc_lighterOrangeLbl);
		
		device1Lbl = new JLabel("");
		GridBagConstraints gbc_device1Lbl = new GridBagConstraints();
		gbc_device1Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_device1Lbl.gridx = 2;
		gbc_device1Lbl.gridy = 1;
		
		panel_3.add(device1Lbl, gbc_device1Lbl);
		
		device2Lbl = new JLabel("");
		GridBagConstraints gbc_device2Lbl = new GridBagConstraints();
		gbc_device2Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_device2Lbl.gridx = 3;
		gbc_device2Lbl.gridy = 1;
		
		panel_3.add(device2Lbl, gbc_device2Lbl);
		
		device3Lbl = new JLabel("");
		GridBagConstraints gbc_device3Lbl = new GridBagConstraints();
		gbc_device3Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_device3Lbl.gridx = 1;
		gbc_device3Lbl.gridy = 1;
		
		panel_3.add(device3Lbl, gbc_device3Lbl);
		
		device4Lbl = new JLabel("");
		GridBagConstraints gbc_device4Lbl = new GridBagConstraints();
		gbc_device4Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_device4Lbl.gridx = 4;
		gbc_device4Lbl.gridy = 1;
		
		panel_3.add(device4Lbl, gbc_device4Lbl);
		
		
		browser1Lbl = new JLabel("");
		GridBagConstraints gbc_browser1Lbl = new GridBagConstraints();
		gbc_browser1Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_browser1Lbl.gridx = 8;
		gbc_browser1Lbl.gridy = 1;
		panel_3.add(browser1Lbl, gbc_browser1Lbl);
		
		browser2Lbl = new JLabel("");
		GridBagConstraints gbc_browser2Lbl = new GridBagConstraints();
		gbc_browser2Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_browser2Lbl.gridx = 9;
		gbc_browser2Lbl.gridy = 1;
		panel_3.add(browser2Lbl, gbc_browser2Lbl);
		
		browser3Lbl = new JLabel("");
		GridBagConstraints gbc_browser3Lbl = new GridBagConstraints();
		gbc_browser3Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_browser3Lbl.gridx = 7;
		gbc_browser3Lbl.gridy = 1;
		panel_3.add(browser3Lbl, gbc_browser3Lbl);
		
		browser4Lbl = new JLabel("");
		GridBagConstraints gbc_browser4Lbl = new GridBagConstraints();
		gbc_browser4Lbl.insets = new Insets(1, 1, 1, 1);
		gbc_browser4Lbl.gridx = 10;
		gbc_browser4Lbl.gridy = 1;
		panel_3.add(browser4Lbl, gbc_browser4Lbl);
		
		gateWay = new RoundedPanel();
		gateWay.setBorder(new EmptyBorder(0, 10, 10, 10));
		gateWay.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		
		GridBagConstraints gbc_gateWay = new GridBagConstraints();
		gbc_gateWay.insets = new Insets(0, 0, 0, 5);
		gbc_gateWay.fill = GridBagConstraints.BOTH;
		gbc_gateWay.gridx = 2;
		gbc_gateWay.gridy = 3;
		add(gateWay, gbc_gateWay);
		GridBagLayout gbl_gateWay = new GridBagLayout();
		gbl_gateWay.columnWidths = new int[]{100, 229};
		gbl_gateWay.rowHeights = new int[]{22, 27, 27, 27, 27, 27, 27};
		gbl_gateWay.columnWeights = new double[]{1.0, 0.8};
		gbl_gateWay.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, 0.0};
		gateWay.setLayout(gbl_gateWay);
		
		IA_PanelLabel lblGatewayPanel = new IA_PanelLabel("Gateway");
		GridBagConstraints gbc_lblGatewayPanel = new GridBagConstraints();
		gbc_lblGatewayPanel.gridwidth = 2;
		gbc_lblGatewayPanel.fill = GridBagConstraints.BOTH;
		gbc_lblGatewayPanel.insets = new Insets(0, 0, 5, 0);
		gbc_lblGatewayPanel.gridx = 0;
		gbc_lblGatewayPanel.gridy = 0;
		gateWay.add(lblGatewayPanel, gbc_lblGatewayPanel);
		
		downTimeLblPanel = new JPanel();
		downTimeLblPanel.setBorder(null);
		downTimeLblPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_downTimeLblPanel = new GridBagConstraints();
		gbc_downTimeLblPanel.insets = new Insets(0, 0, 5, 0);
		gbc_downTimeLblPanel.fill = GridBagConstraints.BOTH;
		gbc_downTimeLblPanel.gridx = 1;
		gbc_downTimeLblPanel.gridy = 2;
		gateWay.add(downTimeLblPanel, gbc_downTimeLblPanel);
		GridBagLayout gbl_downTimeLblPanel = new GridBagLayout();
		gbl_downTimeLblPanel.columnWidths = new int[]{0, 0};
		gbl_downTimeLblPanel.rowHeights = new int[]{0, 0};
		gbl_downTimeLblPanel.columnWeights = new double[]{0.40,0.60};
		gbl_downTimeLblPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		downTimeLblPanel.setLayout(gbl_downTimeLblPanel);
		
		txtDowntimepercent = new OrangeText();
		txtDowntimepercent.setFont(new Font("SansSerif", Font.BOLD, 20));
		txtDowntimepercent.setBackground(Color.WHITE);
		GridBagConstraints gbc_txtDowntimepercent = new GridBagConstraints();
		gbc_txtDowntimepercent.anchor = GridBagConstraints.EAST;
		gbc_txtDowntimepercent.insets = new Insets(0, 0, 0, 5);
		gbc_txtDowntimepercent.fill = GridBagConstraints.VERTICAL;
		gbc_txtDowntimepercent.gridx = 0;
		gbc_txtDowntimepercent.gridy = 0;
		downTimeLblPanel.add(txtDowntimepercent, gbc_txtDowntimepercent);
		txtDowntimepercent.setForeground(Color.BLACK);
		
		lblDownTime = new JLabel("Uptime");
		lblDownTime.setFont(new Font("SansSerif", Font.PLAIN, 12));
		GridBagConstraints gbc_lblDownTime = new GridBagConstraints();
		gbc_lblDownTime.fill = GridBagConstraints.BOTH;
		gbc_lblDownTime.gridx = 1;
		gbc_lblDownTime.gridy = 0;
		downTimeLblPanel.add(lblDownTime, gbc_lblDownTime);
		
		txtDowntimestring = new OrangeText();
		txtDowntimestring.setFont(new Font("SansSerif", Font.PLAIN, 12));
		txtDowntimestring.setForeground(Color.BLACK);
		GridBagConstraints gbc_txtDowntimestring = new GridBagConstraints();
		gbc_txtDowntimestring.anchor = GridBagConstraints.EAST;
		gbc_txtDowntimestring.fill = GridBagConstraints.VERTICAL;
		gbc_txtDowntimestring.insets = new Insets(0, 0, 5, 0);
		gbc_txtDowntimestring.gridx = 1;
		gbc_txtDowntimestring.gridy = 3;
		gateWay.add(txtDowntimestring, gbc_txtDowntimestring);
		
		upTimeLblPanel = new JPanel();
		upTimeLblPanel.setBorder(null);
		upTimeLblPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_upTimeLblPanel = new GridBagConstraints();
		gbc_upTimeLblPanel.insets = new Insets(0, 0, 5, 0);
		gbc_upTimeLblPanel.fill = GridBagConstraints.BOTH;
		gbc_upTimeLblPanel.gridx = 1;
		gbc_upTimeLblPanel.gridy = 4;
		gateWay.add(upTimeLblPanel, gbc_upTimeLblPanel);
		GridBagLayout gbl_upTimeLblPanel = new GridBagLayout();
		gbl_upTimeLblPanel.columnWidths = new int[]{0, 0};
		gbl_upTimeLblPanel.rowHeights = new int[]{0, 0};
		gbl_upTimeLblPanel.columnWeights = new double[]{0.40,0.60};
		gbl_upTimeLblPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		upTimeLblPanel.setLayout(gbl_upTimeLblPanel);
		
			txtUppercent = new OrangeText();
			txtUppercent.setFont(new Font("SansSerif", Font.BOLD, 20));
			GridBagConstraints gbc_txtUppercent = new GridBagConstraints();
			gbc_txtUppercent.anchor = GridBagConstraints.EAST;
			gbc_txtUppercent.insets = new Insets(0, 0, 0, 5);
			gbc_txtUppercent.fill = GridBagConstraints.VERTICAL;
			gbc_txtUppercent.gridx = 0;
			gbc_txtUppercent.gridy = 0;
			upTimeLblPanel.add(txtUppercent, gbc_txtUppercent);
			txtUppercent.setForeground(Color.BLACK);
			
			lblNewLabel = new JLabel("Downtime");
			lblNewLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
			gbc_lblNewLabel.gridx = 1;
			gbc_lblNewLabel.gridy = 0;
			upTimeLblPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		txtUpTimeString = new OrangeText();
		txtUpTimeString.setFont(new Font("SansSerif", Font.PLAIN, 12));
		txtUpTimeString.setForeground(Color.BLACK);
		GridBagConstraints gbc_txtUpTimeString = new GridBagConstraints();
		gbc_txtUpTimeString.insets = new Insets(0, 0, 5, 0);
		gbc_txtUpTimeString.anchor = GridBagConstraints.NORTHEAST;
		gbc_txtUpTimeString.gridx = 1;
		gbc_txtUpTimeString.gridy = 5;
		gateWay.add(txtUpTimeString, gbc_txtUpTimeString); 
		
		GridBagConstraints gbc_lblImageLabel = new GridBagConstraints();
		gbc_lblImageLabel.fill = GridBagConstraints.BOTH;
		gbc_lblImageLabel.gridheight = 5;
		gbc_lblImageLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblImageLabel.gridx = 0;
		gbc_lblImageLabel.gridy = 1;
		
		JLabel imagePanel = new JLabel();
		ImageIcon gIcon = new ImageIcon(getClass().getResource("Gateway.png"));
		Image newImg = gIcon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
		imagePanel.setIcon(new ImageIcon(newImg));
		gateWay.add(imagePanel, gbc_lblImageLabel);
		
		darkBlueLbl.setIcon(newDarkBlue);
		lightBlueLbl.setIcon(newlightBlue);
		darkerBlueLbl.setIcon(newDarkerBlue);
		lighterBlueLbl.setIcon(newlighterBlue);
		
		darkOrnageLbl.setIcon(newDarkOrange);
		darkerOrnageLbl.setIcon(newDarkerOrange);
		lightOrangeLbl.setIcon(newlighterOrange);
		lighterOrangeLbl.setIcon(newlighterOrange);
		
		this.comboDuration.setSelectedIndex(comboDurationSelectedIndex);
//		try
//		{
//			if(this.currentProject.compareToIgnoreCase("All") == 0)
//			{
//				populateData(Constants.TODAY, null, true);
//			}
//			else
//			{
//				populateData(Constants.TODAY, this.currentProject, false);
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
		
		//set the default background colors.
		btnCountOfSessions.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
		btnCountOfSessions.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
		btnDaysSinceLogin.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		btnDaysSinceLogin.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
		btnEngageDepth.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		btnEngageDepth.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
		
		btnEngageDuration.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
		btnEngageDuration.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);

//		btn1DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
//		btn1DayActiveUsers.setForeground(Constants.COLOR_WHITE_BACKGROUND);
//		btn7DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
//		btn7DayActiveUsers.setForeground(Constants.COLOR_BUTTON_GREY_LABEL);
//		btn14DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
//		btn14DayActiveUsers.setForeground(Constants.COLOR_BUTTON_GREY_LABEL);
		
//		revalidate();
//		repaint();
	}

	/**
	 * Function to populate the screen with required analytics information
	 * This is shown when analysis menu is clicked.
	 * @author YM : Created on 06/11/2015
	 */
	public void populateData(int duration, String projectName, boolean allProjects){
		
		resetView();
//		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.z");
//		SimpleDateFormat hhMMssDate = new SimpleDateFormat("mm:ss");
		Date  tempDate;
		//get the overview information using rpc call
		float bounceRate = 0;
		System.out.println("dashboard before getovwrview : " + new Date().getTime());
		OverviewInformation oInfo = rpc.getOverview( duration, projectName, allProjects);
		
		System.out.println("dashboard before get yday ovwrview : " + new Date().getTime());
		OverviewInformation sliderInfo = rpc.getYesterdayOverviewForSlider(duration, projectName, allProjects);
		if(oInfo != null)
		{
			
			//add gateway up/down time percent
			
			txtDowntimepercent.setText( "" + String.format("%.3f", oInfo.getGatewayUpTimePercent())  + "%" );
			txtDowntimestring.setText("" + oInfo.getGatewayUpTimeString());
			txtUppercent.setText("" + String.format("%.3f", oInfo.getGatewayDownTimePercent()) + " % "  );
			txtUpTimeString.setText( "" + oInfo.getGatewayDownTimeString());
	
			//add content
			JTable _screensTable = new JTable();
			DefaultTableModel _screensModel = new DefaultTableModel(0,2);
			List<ScreensCount> allScreenViews = oInfo.getScreenViews();
			int noOfRecords =0, k=0;
			int totalScreens = 0;
			System.out.println("dashboard before adding screns : " + new Date().getTime());
			if(allScreenViews != null)
			{
				noOfRecords = allScreenViews.size();
				for(k=0; k<noOfRecords; k++)
				{
					_screensModel.addRow(new Object[]{allScreenViews.get(k).getScreenName(), allScreenViews.get(k).getNoOfViews()});
					totalScreens = totalScreens + allScreenViews.get(k).getNoOfViews();
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
				
				RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
				rightRenderer.paddingSize = 45;
				rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
				_screensTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
				_screensTable.setEnabled(false);
				this.content.setViewportView(_screensTable);
				txtScreenViews.setText("" + totalScreens);

			}
			//get users information
			System.out.println("dashboard before getNo of active users : " + new Date().getTime());
			int totalUsers = oInfo.getNoOfActiveUsers();
			txtUsers.setText("" + totalUsers);
			txtActions.setText("" + oInfo.getNoOfActions());
			

			if(oInfo.getAvgSessionDuration() != null && oInfo.getAvgSessionDuration().length() > 0)
			{
				if(oInfo.getAvgSessionDuration().contains("."))
				{
					txtAvgSessionDuration.setText(oInfo.getAvgSessionDuration());
				}
				else
				{
					txtAvgSessionDuration.setText(oInfo.getAvgSessionDuration());
				}
				
			}
			
			
			
			bounceRate = (oInfo.getBounceRate()) * 100;
					txtBounceRate.setText("" +  (int)bounceRate + " %");
			
			if(oInfo.getNoOfSessions() > 0)
			{
				txtSessions.setText("" + oInfo.getNoOfSessions());
				txtScreensbyCurrent.setText((int)oInfo.getAverageScreensPerVisit() + "");
				
			}
	      
			//add the sliding Panel elements
			
		}
		
		System.out.println("dashboard before get active users information : " + new Date().getTime());
		this.activeUsersData = rpc.getActiveUsersInformation(this.currentProject, this.allProjects, this.currentDuration);
		int noOfActiveUsers = 0, i;
		int total1DayActiveUsers = 0;
		int total7DayActiveUsers = 0;
		int total14DayActiveUsers = 0;
		
		System.out.println("dashboard before get active users counts : " + new Date().getTime());
		ActiveUsersInfo aInfo = rpc.getActiveUsersCounts(this.currentProject, this.allProjects, this.currentDuration);
		if(aInfo != null)
		{
				total1DayActiveUsers = aInfo.getOneDayActiveUsers() ;
				total7DayActiveUsers = aInfo.getSevenDayActiveUsers() ;
				total14DayActiveUsers = aInfo.getFourteenDayActiveUsers();
		}	
		//initially create chart for 1 day active users
		if(oneDayActive){
			btn1DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
			btn1DayActiveUsers.setTextColor(Constants.COLOR_WHITE_BACKGROUND);
			btn14DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btn14DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
			btn7DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btn7DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
			createActiveUsersChart(1);
		}
		else if(sevenDayActive){
			btn7DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
			btn7DayActiveUsers.setTextColor(Constants.COLOR_WHITE_BACKGROUND);
			btn1DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btn1DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
			btn14DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btn14DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
			createActiveUsersChart(7);
		}
		else if(fourteenDayActive){
			btn14DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
			btn14DayActiveUsers.setTextColor(Constants.COLOR_WHITE_BACKGROUND);
			btn1DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btn1DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
			btn7DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btn7DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
			createActiveUsersChart(14);
		}
		
		btn1DayActiveUsers.lblboldtext.setText(""+total1DayActiveUsers);
		//btn1DayActiveUsers.lblboldtext.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		btn1DayActiveUsers.lblnormaltext.setText("1 Day Active Users");
		//btn1DayActiveUsers.lblnormaltext.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		
		
		btn7DayActiveUsers.lblboldtext.setText(""+total7DayActiveUsers);
		btn7DayActiveUsers.lblnormaltext.setText("7 Day Active Users");
		
		btn14DayActiveUsers.lblboldtext.setText(""+total14DayActiveUsers);
		btn14DayActiveUsers.lblnormaltext.setText("14 Day Active Users");
		//add alarms values for duration
		
		
		System.out.println("dashboard before get alarms : " + new Date().getTime());
		
		Dataset _alarms = rpc.getAlarms(duration, this.currentProject, this.allProjects);
		
		//set the value in labels on the UI
		int alarmsDSSize, r;
		int medAlarms = 0, highAlarms = 0, criticalAlarms = 0;
		String alarmPriority;
		if(_alarms != null)
		{
			alarmsDSSize = _alarms.getRowCount();
			
			for(r=0; r<alarmsDSSize; r++)
			{
				alarmPriority = _alarms.getValueAt(r, 0).toString();
				
				if(alarmPriority.compareToIgnoreCase("Medium") == 0)
				{
					medAlarms = (int)Double.parseDouble(_alarms.getValueAt(r, 1).toString());
				}
				else if(alarmPriority.compareToIgnoreCase("High") == 0)
				{
					highAlarms = (int)Double.parseDouble(_alarms.getValueAt(r, 1).toString());
				}
				else if(alarmPriority.compareToIgnoreCase("Critical") == 0)
				{
					criticalAlarms = (int)Double.parseDouble(_alarms.getValueAt(r, 1).toString());
				}
			}
		}
		lblValueMedium.setText("" + medAlarms);
		lblValueHigh.setText("" + highAlarms);
		lblValueCritical.setText("" + criticalAlarms);
		
		
		System.out.println("dashboard before get alarms clear time : " + new Date().getTime());
		//retrieve time to clear alarms 
		Dataset _alarmsClearTime = rpc.getAlarmsClearTime(duration, this.currentProject, this.allProjects);
		//set value in the UI
		//set the value in labels on the UI
				int alarmsClearDSSize;
				String medAlarmsClr = "", highAlarmsclr = "", criticalAlarmsClr = "";
				String alarmPriorityClr;
				//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				if(_alarmsClearTime != null)
				{
					alarmsClearDSSize = _alarmsClearTime.getRowCount();
					for(r=0; r<alarmsClearDSSize; r++)
					{
						if(_alarmsClearTime.getValueAt(r, 0) != null)
						{
							alarmPriority = _alarmsClearTime.getValueAt(r, 0).toString();
							if(alarmPriority.compareToIgnoreCase("Medium") == 0) //medium
							{
								medAlarmsClr = _alarmsClearTime.getValueAt(r, 1).toString();
							//	lblTimeToClrMedium.setText(medAlarmsClr.substring(3, 8));
								lblTimeToClrMedium.setText(medAlarmsClr.substring(0, 8));
							}
							else if(alarmPriority.compareToIgnoreCase("High") == 0) //high
							{
								highAlarmsclr = _alarmsClearTime.getValueAt(r, 1).toString();
								lblTimeToClrHigh.setText(highAlarmsclr.substring(0, 8));
							}
							else if(alarmPriority.compareToIgnoreCase("Critical") == 0) //critical
							{
								
								criticalAlarmsClr = _alarmsClearTime.getValueAt(r, 1).toString();
								lblTimeToClrCritical.setText(criticalAlarmsClr.substring(0, 8));
							}
						}
					}
				}
				
			
		
		//add Freq and recency information
		
		        btnCountOfSessions.lblUp.setText("Count Of Sessions");
				btnCountOfSessions.lblMiddleLeft.setText("" +oInfo.getNoOfSessions() );
				btnCountOfSessions.lblMiddleRight.setText("" +oInfo.getNoOfScreenViews() );
				btnCountOfSessions.lblLowerLeft.setText("Sessions");
				btnCountOfSessions.lblLowerRight.setText("Screen Views");
				
				
				btnDaysSinceLogin.lblMiddleLeft.setText("" +oInfo.getNoOfSessions() );
				btnDaysSinceLogin.lblMiddleRight.setText("" +oInfo.getNoOfScreenViews() );
				btnDaysSinceLogin.lblLowerLeft.setText("Sessions");
				btnDaysSinceLogin.lblLowerRight.setText("Screen Views");
				
		        if(showFrequency)
		        {
		        	btnCountOfSessions.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
					btnCountOfSessions.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
					
					btnDaysSinceLogin.setBackground(Constants.COLOR_WHITE_BACKGROUND);
					btnDaysSinceLogin.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
		        	Dataset freqInfo = rpc.getFrequencyInformation( this.currentProject, this.allProjects, duration);
		        	createFrequencyChart(freqInfo);
		        }
		        else if(showRecency)
		        {
		        	btnCountOfSessions.setBackground(Constants.COLOR_WHITE_BACKGROUND);
					btnCountOfSessions.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblLowerLeft.setForeground(Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblLowerRight.setForeground(Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblMiddleLeft.setForeground(Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblMiddleRight.setForeground(Constants.COLOR_WHITE_BACKGROUND);
					btnDaysSinceLogin.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
					btnDaysSinceLogin.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
					Dataset recencyData = rpc.getDaysSinceLastLoginPerUser( this.currentProject, this.allProjects, this.currentDuration);
					createRecencyChart(recencyData);
		        }
		
		//add engagement information screens viewed by each user on each visit
		
		
		
		btnEngageDuration.lblUp.setText("Duration");
		btnEngageDuration.lblMiddleLeft.setText("" +oInfo.getNoOfSessions() );
		btnEngageDuration.lblMiddleRight.setText("" +oInfo.getNoOfScreenViews() );
		btnEngageDuration.lblLowerLeft.setText("Sessions");
		btnEngageDuration.lblLowerRight.setText("Screen Views");
		
		btnEngageDepth.lblMiddleLeft.setText("" +oInfo.getNoOfSessions() );
		btnEngageDepth.lblMiddleRight.setText("" +oInfo.getNoOfScreenViews() );
		btnEngageDepth.lblLowerLeft.setText("Sessions");
		btnEngageDepth.lblLowerRight.setText("Screen Views");
		
		if(screenViews){
			btnEngageDepth.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btnEngageDepth.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
			btnEngageDuration.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
			btnEngageDuration.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
			createEngagementChart(rpc.getEngagementInformation( this.currentProject, this.allProjects, this.currentDuration));
		}
		else if(screenDepth){
			btnEngageDuration.setBackground(Constants.COLOR_WHITE_BACKGROUND);
			btnEngageDuration.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
			btnEngageDepth.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
			btnEngageDepth.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
			createEngagementChartScreenDepth(rpc.getEngagementInformationScreenDepth( this.currentProject, this.allProjects, this.currentDuration));
			
		}
		
		
		System.out.println("dashboard before get yesterday overview : " + new Date().getTime());
		
		//add yesterday's overview.
		
		//y'day overview would always be yday and not relative. 
		// as per Chris's comment on 11th Jan , QA doc query 67
		OverviewInformation yInfo = rpc.getYesterdayOverview( Constants.YESTERDAY, projectName, allProjects);
		
		if(yInfo != null)
		{
			valYActions.setText("" + yInfo.getNoOfActions()) ;
			valySessions.setText("" + yInfo.getNoOfSessions());
			valyUsers.setText("" +yInfo.getNoOfActiveUsers());
		//	valNewUsers.setText("" + yInfo.getNoOfNewUsers());
			valNewUsers.setText("" +rpc.getNumberOfNewUsers(Constants.YESTERDAY, projectName, allProjects));
			if(yInfo.getAvgSessionDuration() != null && yInfo.getAvgSessionDuration().length() > 0)
			{
				if(yInfo.getAvgSessionDuration().contains("."))
				{
					if(yInfo.getAvgSessionDuration().contains("-"))
					{
						valAvgSession.setText(yInfo.getAvgSessionDuration().substring(14, 19));
					}
					else
					{
						valAvgSession.setText(yInfo.getAvgSessionDuration().substring(3, 8));
					}
					
				}
				else
				{
					
					if(yInfo.getAvgSessionDuration().contains("-"))
					{
						valAvgSession.setText(yInfo.getAvgSessionDuration().substring(14));
					}
					else
					{
						valAvgSession.setText(yInfo.getAvgSessionDuration().substring(3));
					}
				}
			}
			else
			{
				valAvgSession.setText("00:00");
			}
		
		
			if(yInfo.getNoOfSessions() == 0)
			{
				valActionsPerSession.setText("0.0");
			}
			else
			{
				valActionsPerSession.setText(String.format("%.2f", (float)(yInfo.getNoOfActions()/yInfo.getNoOfSessions())));
			}
		}
		if(oInfo != null && sliderInfo != null)
		{
			System.out.println("oInfo & sliderInfo not null");
			
			//int bRate = 0;
			try {
				if(oInfo.getNoOfActiveUsers() == 0)
				{
					_slide4.lblPercent.setText("0 %");
					_slide4.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
					_slide4.lblBottom.setText("+ 0%");
				}
				else
				{
					
					_slide4.lblPercent.setText( (int) bounceRate + " %");
					if(sliderInfo.getBounceRate() == 0)
					{
					
						//bRate =  Math.round(((float) oInfo.getBounceRate() / oInfo.getNoOfActiveUsers()) * 100 );
						//_slide4.lblBottom.setText(String.format("%.2f",  ((float) oInfo.getBounceRate() / oInfo.getNoOfActiveUsers()) * 100) + "%");
						if(bounceRate >= 0)
						{
							_slide4.lblBottom.setText( "+" + (int)(bounceRate) * 100 + "%");
						}
						else
						{
							_slide4.lblBottom.setText( (int)bounceRate + "%");
						}
							_slide4.lblBottom.setBackground(Constants.COLOR_RED_LABEL_BACKGROUND);
					}
					else
					{
						if(oInfo.getBounceRate() <= sliderInfo.getBounceRate())
						{
							
							_slide4.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
							_slide4.lblBottom.setText(Math.round(  (((float)oInfo.getBounceRate() - sliderInfo.getBounceRate())/sliderInfo.getBounceRate()) * 100) + "%");
						}
						else
						{
							int bRate = Math.round(  (((float)oInfo.getBounceRate() - sliderInfo.getBounceRate())/sliderInfo.getBounceRate()) * 100);
							_slide4.lblBottom.setBackground(Constants.COLOR_RED_LABEL_BACKGROUND);
							if(bRate >= 0)
							{
								_slide4.lblBottom.setText( "+" + bRate + "%");
							}
							else
							{
								_slide4.lblBottom.setText( bRate + "%");
							}
						}
					}
				}
				
				
				_slide2.lblPercent.setText(oInfo.getNoOfActiveUsers() + "");
				if(sliderInfo.getNoOfActiveUsers() == 0)
				{
					_slide2.lblBottom.setText("+" + Math.round(oInfo.getNoOfActiveUsers() * 100) + "%");
					_slide2.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
				}
				else
				{
					
					if(oInfo.getNoOfActiveUsers()  >= sliderInfo.getNoOfActiveUsers())
					{
						_slide2.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
						_slide2.lblBottom.setText("+" + Math.round( (((float)oInfo.getNoOfActiveUsers() - sliderInfo.getNoOfActiveUsers())/sliderInfo.getNoOfActiveUsers()) * 100) + "%");
					}
					else
					{
						_slide2.lblBottom.setBackground(Constants.COLOR_RED_LABEL_BACKGROUND);
						int actUsrs = Math.round( (((float)oInfo.getNoOfActiveUsers() - sliderInfo.getNoOfActiveUsers())/sliderInfo.getNoOfActiveUsers()) * 100);
						if(actUsrs >=0 )
						{
							_slide2.lblBottom.setText( "-" + actUsrs + "%");
						}
						else
						{
							_slide2.lblBottom.setText( actUsrs + "%");
						}
					}
				}
				_slide3.lblPercent.setText(oInfo.getNoOfScreenViews() + "");
				if(sliderInfo.getNoOfScreenViews() == 0)
				{
					_slide3.lblBottom.setText("+"+Math.round(oInfo.getNoOfScreenViews() * 100) + "%");
					_slide3.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
				}
				else
				{
					
					if(oInfo.getNoOfScreenViews() >= sliderInfo.getNoOfScreenViews())
					{
						_slide3.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
						_slide3.lblBottom.setText("+" +Math.round( ((((float)oInfo.getNoOfScreenViews() - sliderInfo.getNoOfScreenViews()) / sliderInfo.getNoOfScreenViews())) * 100 )+ "%");
					}
					else
					{
						int noSViews = Math.round( ((((float)oInfo.getNoOfScreenViews() - sliderInfo.getNoOfScreenViews()) / sliderInfo.getNoOfScreenViews())) * 100 );
						if(noSViews >= 0)
						{
							_slide3.lblBottom.setText( "-" + noSViews+ "%");
						}
						else
						{
							_slide3.lblBottom.setText( noSViews+ "%");
						}
						_slide3.lblBottom.setBackground(Constants.COLOR_RED_LABEL_BACKGROUND);
					}
				}
				_slide1.lblPercent.setText(oInfo.getNoOfSessions() + "");
				if(sliderInfo.getNoOfSessions() == 0)
				{
					_slide1.lblBottom.setText("+" + Math.round(( oInfo.getNoOfSessions() * 100 ))+ "%");
					_slide1.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
				}
				
				else
				{
					float sessionsPercent =(( (float)oInfo.getNoOfSessions() - sliderInfo.getNoOfSessions())/ sliderInfo.getNoOfSessions());
					sessionsPercent = sessionsPercent * 100;
					
					
					if(oInfo.getNoOfSessions()  >= sliderInfo.getNoOfSessions())
					{
						
						_slide1.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
						_slide1.lblBottom.setText( "+" + Math.round( sessionsPercent) + "%");
					}
					else
					{
						
						_slide1.lblBottom.setBackground(Constants.COLOR_RED_LABEL_BACKGROUND);	
						if(Math.round(sessionsPercent) >= 0)
						{
							_slide1.lblBottom.setText(  "-" + Math.round( sessionsPercent) + "%");
						}
						else
						{
							_slide1.lblBottom.setText(  Math.round( sessionsPercent) + "%");
						}
					}
				}
				
			
			float todaySession = 0;
			float yDaySession = 0;
			String strDuration = "";
			if(oInfo.getAvgSessionDuration() != null && oInfo.getAvgSessionDuration().length() > 0)
			{
				if(oInfo.getAvgSessionDuration() != null){
				_slide5.lblPercent.setText(oInfo.getAvgSessionDuration() );
				}
				strDuration = oInfo.getAvgSessionDuration();
				
				 todaySession = ((Float.parseFloat(strDuration.split(":")[0])) * 3600) +
						 ((Float.parseFloat(strDuration.split(":")[1])) * 60)	+
						 (Float.parseFloat(strDuration.split(":")[0]));
				
			}
			else
			{
				_slide5.lblPercent.setText("00:00:00" );
			}
			if(sliderInfo.getAvgSessionDuration() != null)
			{
				strDuration = sliderInfo.getAvgSessionDuration();
				if(strDuration != null && strDuration.length() > 0)
				{
					String splitted[] = strDuration.split(":");
					if(splitted.length >= 1 && splitted[0] != null)
					{
						String hourVal = "00";
						if(splitted[0].contains("-"))
						{
							hourVal = splitted[0].substring(splitted[0].length() - 2);
						}
						else
						{
							hourVal = splitted[0];
						}
						yDaySession = ((Float.parseFloat(hourVal)) * 3600);
					}
					if(splitted.length >= 2 && splitted[1] != null)
					{
						yDaySession = yDaySession + ((Float.parseFloat(splitted[1])) * 60);
					}
					if(splitted.length >= 3 && splitted[2] != null)
					{
						yDaySession = yDaySession + (Float.parseFloat(splitted[2]));
					}
					
						 
				}
			}
			if(yDaySession != 0)
			{
				
				if(todaySession >= yDaySession)
				{
					
					_slide5.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
					_slide5.lblBottom.setText("+" + Math.round( ((float)(todaySession - yDaySession)/yDaySession) * 100 )+ "%");
				}
				else
				{
					int val =  Math.round( ((float)(todaySession - yDaySession)/yDaySession) * 100 );
					_slide5.lblBottom.setBackground(Constants.COLOR_RED_LABEL_BACKGROUND);
					if(val >= 0)
					{
						_slide5.lblBottom.setText( "-" + val + "%");
					}
					else
					{
						_slide5.lblBottom.setText( val + "%");
					}
				}
			}
			else
			{
				_slide5.lblBottom.setText( "+" + Math.round(todaySession * 100) + "%");
				_slide5.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
			}
			
			_slide6.lblPercent.setText(txtScreensbyCurrent.getText() + "");
			if(sliderInfo.getAverageScreensPerVisit() == 0)
			{
				_slide6.lblBottom.setText("+ 100%");
				_slide6.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
			}
			else
			{
				if(oInfo.getAverageScreensPerVisit() >= sliderInfo.getAverageScreensPerVisit())
				{
					_slide6.lblBottom.setText("+"+(Math.round(Math.round(oInfo.getAverageScreensPerVisit()) / Math.round(sliderInfo.getAverageScreensPerVisit())) * 100) + "%");
					_slide6.lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
				}
				else
				{
					int valAvg = Math.round((Math.round(oInfo.getAverageScreensPerVisit()) / Math.round(sliderInfo.getAverageScreensPerVisit())) * 100 );
					if(valAvg >= 0)
					{
						_slide6.lblBottom.setText( "-" + valAvg + "%");
					}
					else
					{
						_slide6.lblBottom.setText(valAvg + "%");	
					}
					
					_slide6.lblBottom.setBackground(Constants.COLOR_RED_LABEL_BACKGROUND);
				}
			}
			
			topSlide._slide1.lblBottom.setText(this._slide1.lblBottom.getText());
			topSlide._slide1.lblBottom.setBackground(this._slide1.lblBottom.getBackground());
			topSlide._slide1.lblPercent.setText(this._slide1.lblPercent.getText());
			
			topSlide._slide2.lblBottom.setText(this._slide2.lblBottom.getText());
			topSlide._slide2.lblBottom.setBackground(this._slide2.lblBottom.getBackground());
			topSlide._slide2.lblPercent.setText(this._slide2.lblPercent.getText());
			
			topSlide._slide3.lblBottom.setText(this._slide3.lblBottom.getText());
			topSlide._slide3.lblBottom.setBackground(this._slide3.lblBottom.getBackground());
			topSlide._slide3.lblPercent.setText(this._slide3.lblPercent.getText());
			
			topSlide._slide4.lblBottom.setText(this._slide4.lblBottom.getText());
			topSlide._slide4.lblBottom.setBackground(this._slide4.lblBottom.getBackground());
			topSlide._slide4.lblPercent.setText(this._slide4.lblPercent.getText());
			
			topSlide._slide5.lblBottom.setText(this._slide5.lblBottom.getText());
			topSlide._slide5.lblBottom.setBackground(this._slide5.lblBottom.getBackground());
			topSlide._slide5.lblPercent.setText(this._slide5.lblPercent.getText());
			
			topSlide._slide6.lblBottom.setText(this._slide6.lblBottom.getText());
			topSlide._slide6.lblBottom.setBackground(this._slide6.lblBottom.getBackground());
			topSlide._slide6.lblPercent.setText(this._slide6.lblPercent.getText());
			
			
			
				top_left.addPanelToList(_slide1);
				top_left.addPanelToList(_slide2);
				top_left.addPanelToList(_slide3);
				top_left.addPanelToList(_slide4);
				top_left.addPanelToList(_slide5);
				top_left.addPanelToList(_slide6);
				top_left.addPanelToList(topSlide);
				System.out.println("slideNo : " + this.slideNo);
				top_left.setSelectedPanel(this.slideNo);
				
				
				
			} catch (Exception e) {
				
				//e.printStackTrace();
			}
		}
		else
		{
			System.out.println("oInfo or sliderInfo is null");
		}
		
		System.out.println("dashboard before slider effect : " + new Date().getTime());
		sliderEffect(this.slideNo);
		
		/** add devices and browser charts **/
		
//		darkBlueLbl.setIcon(null);
//		darkerBlueLbl.setIcon(null);
//		lightBlueLbl.setIcon(null);
//		lighterBlueLbl.setIcon(null);
//		
//		darkOrnageLbl.setIcon(null);
//		darkerOrnageLbl.setIcon(null);
//		lightOrangeLbl.setIcon(null);
//		lighterOrangeLbl.setIcon(null);
//		
//		browser2Lbl.setIcon(null);
//        browser1Lbl.setIcon(null);
//        browser3Lbl.setIcon(null);
//        browser4Lbl.setIcon(null);
//        
//        device1Lbl.setIcon(null);
//        device2Lbl.setIcon(null);
//        device3Lbl.setIcon(null);
//        device4Lbl.setIcon(null);
		
		System.out.println("dashboard before get device information : " + new Date().getTime());
		
		DevicesInformation dInfo = rpc.getDeviceInformation( duration, currentProject, allProjects);
		
	    
	    realTimeDeviceButton.lblBoldText.setText("" + dInfo.getNoOfClientsOnDesktop());
	    realTimeDeviceButton_1.lblBoldText.setText("" + dInfo.getNoOfClientsOnMobile());
	    
	  //Add a pie chart showing no of devices
		DefaultPieDataset dataset = new DefaultPieDataset();
		System.out.println("dashboard before get top os : " + new Date().getTime());
	    Dataset operatingSystems = rpc.getTopOperatingSystems( duration, currentProject, allProjects);
	    
	    if(operatingSystems != null)
	    {
	    	if(operatingSystems.getRowCount() > 0)
        	{
        		dataset.setValue(operatingSystems.getValueAt(0, "OS_NAME").toString(), Integer.parseInt(operatingSystems.getValueAt(0, "Users").toString()));
        	}
        	if(operatingSystems.getRowCount() >= 2 )
        	{
        		dataset.setValue(operatingSystems.getValueAt(1, "OS_NAME").toString(), Integer.parseInt(operatingSystems.getValueAt(1, "Users").toString()));
        	}
        	if(operatingSystems.getRowCount() >= 3 )
        		
        	{
        		dataset.setValue(operatingSystems.getValueAt(2, "OS_NAME").toString(), Integer.parseInt(operatingSystems.getValueAt(2, "Users").toString()));
        	}
        	if(operatingSystems.getRowCount() >= 4 )
        	{
        		dataset.setValue(operatingSystems.getValueAt(3, "OS_NAME").toString(), Integer.parseInt(operatingSystems.getValueAt(3, "Users").toString()));
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
//            _piePlot.setSectionPaint("Mobile", new Color(0,96,191));
//            _piePlot.setSectionPaint("Desktop", new Color(138,197,255));
            _piePlot.setShadowXOffset(0.0);
            _piePlot.setShadowYOffset(0.0);
            _piePlot.setBaseSectionOutlinePaint(Color.WHITE);	
            
            //create the OS iscons
            
           
    		
            //set the color scheme of the operating systems chart
            
            if( operatingSystems.getRowCount() > 0)
	        {
            	_piePlot.setSectionPaint(operatingSystems.getValueAt(0, "OS_NAME").toString(), new Color(0,96,191));
            	darkBlueLbl.setVisible(true);
            	if(operatingSystems.getValueAt(0, "OS_NAME").toString().toLowerCase().contains("windows")   )
            		device1Lbl.setIcon(symbolForPC);
            	else if(operatingSystems.getValueAt(0, "OS_NAME").toString().toLowerCase().contains("mobile"))
            		device1Lbl.setIcon(symbolForMobile);
            	else if(operatingSystems.getValueAt(0, "OS_NAME").toString().toLowerCase().contains("mac"))
            		device1Lbl.setIcon(symbolForMac);
            	else if(operatingSystems.getValueAt(0, "OS_NAME").toString().toLowerCase().contains("linux") )
            		device1Lbl.setIcon(symbolForLinux);
            	
            	device1Lbl.setVisible(true);
	        }
            if( operatingSystems.getRowCount() >= 2)
	        {
            	_piePlot.setSectionPaint(operatingSystems.getValueAt(1, "OS_NAME").toString(), new Color(138,197,255));
            	lightBlueLbl.setVisible(true);
            	if(operatingSystems.getValueAt(1, "OS_NAME").toString().toLowerCase().contains("windows")   )
            		device2Lbl.setIcon(symbolForPC);
            	else if(operatingSystems.getValueAt(1, "OS_NAME").toString().toLowerCase().contains("mobile"))
            		device2Lbl.setIcon(symbolForMobile);
            	else if(operatingSystems.getValueAt(1, "OS_NAME").toString().toLowerCase().contains("mac"))
            		device2Lbl.setIcon(symbolForMac);
            	else if(operatingSystems.getValueAt(1, "OS_NAME").toString().toLowerCase().contains("linux") )
            		device2Lbl.setIcon(symbolForLinux);
            	
            	device2Lbl.setVisible(true);
	        }
            if( operatingSystems.getRowCount() >= 3)
	        {
            	_piePlot.setSectionPaint(operatingSystems.getValueAt(2, "OS_NAME").toString(), Color.BLUE);
            	darkerBlueLbl.setVisible(true);
            	if(operatingSystems.getValueAt(2, "OS_NAME").toString().toLowerCase().contains("windows")   )
            		device3Lbl.setIcon(symbolForPC);
            	else if(operatingSystems.getValueAt(2, "OS_NAME").toString().toLowerCase().contains("mobile"))
            		device3Lbl.setIcon(symbolForMobile);
            	else if(operatingSystems.getValueAt(2, "OS_NAME").toString().toLowerCase().contains("mac"))
            		device3Lbl.setIcon(symbolForMac);
            	else if(operatingSystems.getValueAt(2, "OS_NAME").toString().toLowerCase().contains("linux") )
            		device3Lbl.setIcon(symbolForLinux);
            	
            	device3Lbl.setVisible(true);
	        }
            if( operatingSystems.getRowCount() >= 4)
	        {
            	_piePlot.setSectionPaint(operatingSystems.getValueAt(3, "OS_NAME").toString(), new Color(215, 215, 255));
            	lighterBlueLbl.setVisible(true);
            	if(operatingSystems.getValueAt(3, "OS_NAME").toString().toLowerCase().contains("windows")   )
            		device4Lbl.setIcon(symbolForPC);
            	else if(operatingSystems.getValueAt(3, "OS_NAME").toString().toLowerCase().contains("mobile"))
            		device4Lbl.setIcon(symbolForMobile);
            	else if(operatingSystems.getValueAt(3, "OS_NAME").toString().toLowerCase().contains("mac"))
            		device4Lbl.setIcon(symbolForMac);
            	else if(operatingSystems.getValueAt(3, "OS_NAME").toString().toLowerCase().contains("linux") )
            		device4Lbl.setIcon(symbolForLinux);
            	device4Lbl.setVisible(true);
	        }
            
            deviceChartPanel.setChart(_pieChart);
            deviceChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
            //deviceChartPanel.setPreferredSize(new Dimension(120, 120));
            deviceChartPanel.setBorder(BorderFactory.createEmptyBorder());
            deviceChartPanel.repaint();

            //browser chart
	    }
	    
	    System.out.println("dashboard before get browser information : " + new Date().getTime());
	 		Dataset browserData = rpc.getBrowserInformation(duration, currentProject, allProjects);
	 		DefaultPieDataset browserPlot = new DefaultPieDataset();
	 		int noOfBrowserRows = 0;
        if(browserData != null )
        {
       
        	noOfBrowserRows = browserData.getRowCount();
        	if(noOfBrowserRows > 0)
        	{
        		browserPlot.setValue(browserData.getValueAt(0, "browser_name").toString(), Integer.parseInt(browserData.getValueAt(0, "bCount").toString()));
        	}
        	if(noOfBrowserRows >= 2 )
        	{
        		browserPlot.setValue(browserData.getValueAt(1, "browser_name").toString(), Integer.parseInt(browserData.getValueAt(1, "bCount").toString()));
        	}
        	if(noOfBrowserRows >= 3 )
        		
        	{
        		browserPlot.setValue(browserData.getValueAt(2, "browser_name").toString(), Integer.parseInt(browserData.getValueAt(2, "bCount").toString()));
        	}
        	if(noOfBrowserRows >= 4 )
        	{
        		browserPlot.setValue(browserData.getValueAt(3, "browser_name").toString(), Integer.parseInt(browserData.getValueAt(3, "bCount").toString()));
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
	        
        
        String bName = "";
        if(noOfBrowserRows > 0)
        {
    	  bName = browserData.getValueAt(0, "browser_name").toString();
        _pieBrowswerPlot.setSectionPaint(bName, new Color(251,175,25));
        darkOrnageLbl.setVisible(true);
        if(bName.compareToIgnoreCase("IE") == 0 || bName.toLowerCase().contains("explorer") )
        	browser1Lbl.setIcon(newBrowser);
        else if(bName.toLowerCase().contains("chrome"))
        	browser1Lbl.setIcon(newBrowser2);
        else if(bName.toLowerCase().contains("safari"))
        	browser1Lbl.setIcon(newSafari);
        else if(bName.toLowerCase().contains("opera") )
        	browser1Lbl.setIcon(newOpera);
        else if(bName.toLowerCase().contains("mozilla") )
        	browser1Lbl.setIcon(newMozilla);
        
        browser1Lbl.setVisible(true);
        }
        if(noOfBrowserRows >= 2 )
        {
        	bName = browserData.getValueAt(1, "browser_name").toString();
        	_pieBrowswerPlot.setSectionPaint(bName, new Color(254,193,71));
        	lightOrangeLbl.setVisible(true);
        	if(bName.compareToIgnoreCase("IE") == 0 || bName.toLowerCase().contains("explorer") )
	        	browser2Lbl.setIcon(newBrowser);
	        else if(bName.toLowerCase().contains("chrome") )
	        	browser2Lbl.setIcon(newBrowser2);
	        else if(bName.toString().toLowerCase().contains("safari") )
	        	browser2Lbl.setIcon(newSafari);
	        else if(bName.toLowerCase().contains("opera") )
	        	browser2Lbl.setIcon(newOpera);
	        else if(bName.toLowerCase().contains("mozilla") )
	        	browser2Lbl.setIcon(newMozilla);
        	
        	browser2Lbl.setVisible(true);
        }
        
        if(noOfBrowserRows >= 3 )
        {
        	bName = browserData.getValueAt(2, "browser_name").toString();
        	_pieBrowswerPlot.setSectionPaint(bName, new Color(255,128,0));
        	darkerOrnageLbl.setVisible(true);;
        	if(bName.compareToIgnoreCase("IE") == 0 || bName.toLowerCase().contains("explorer") )
	        	browser3Lbl.setIcon(newBrowser);
	        else if(bName.toLowerCase().contains("chrome") )
	        	browser3Lbl.setIcon(newBrowser2);
	        else if(bName.toLowerCase().contains("safari") )
	        	browser3Lbl.setIcon(newSafari);
	        else if(bName.toLowerCase().contains("opera") )
	        	browser3Lbl.setIcon(newOpera);
	        else if(bName.toLowerCase().contains("mozilla") )
	        	browser3Lbl.setIcon(newMozilla);
        	browser3Lbl.setVisible(true);
        }
        
        if(noOfBrowserRows >= 4 )
        {
        	bName = browserData.getValueAt(3, "browser_name").toString();
        	_pieBrowswerPlot.setSectionPaint(bName, new Color(239,228,176));
        	lighterOrangeLbl.setVisible(true);
        	if(bName.compareToIgnoreCase("IE") == 0 || bName.toLowerCase().contains("explorer") )
	        	browser4Lbl.setIcon(newBrowser);
	        else if(bName.toLowerCase().contains("chrome") )
	        	browser4Lbl.setIcon(newBrowser2);
	        else if(bName.toLowerCase().contains("safari") )
	        	browser4Lbl.setIcon(newSafari);
	        else if(bName.toLowerCase().contains("opera") )
	        	browser4Lbl.setIcon(newOpera);
	        else if(bName.toLowerCase().contains("mozilla") )
	        	browser4Lbl.setIcon(newMozilla);
        	browser4Lbl.setVisible(true);
        }
	 	
        browserChartPanel.setChart(_browserGraph);
        browserChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
        //browserChartPanel.setPreferredSize(new Dimension(120, 120));
        browserChartPanel.setBorder(BorderFactory.createEmptyBorder());
       
        browserChartPanel.repaint();
        
        }
	
//		revalidate();
//		repaint();
	}

	/**
	 * function to reset screen display to populate new data.
	 */
	private void resetView()
	{
		
		//make all the labels invisible in device chart panel
		
		
//		this.content.setViewportView(null);
//		//this.overview.removeAll();
//		this.timeLine.removeAll();
//		//this.activeUsers.removeAll();
//		this.activeUsers_Chart.removeAll();
//		this.chartPanel_1.removeAll();
//		this.chartPanel_2.removeAll();
		//this.reportsAndTrends.removeAll();
		//this.gateWay.removeAll();
		//this.freqTable.setViewportView(null);
		//this.alarmsScroll.setViewportView(null);
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String actionCommad = arg0.getActionCommand();
		int indexToShow = 0;
		//this.top_left.middlePane.setPreferredSize(new Dimension(170,100));
		if(actionCommad.compareToIgnoreCase(Constants.CMD_DURATION_SELECT) ==0)
		{
			this.currentDuration = Constants.TODAY;
			this.comboDurationSelectedIndex = comboDuration.getSelectedIndex();
			String selectedDuration = comboDuration.getSelectedItem().toString();
			if(selectedDuration != null)
			{
				selectedDuration = selectedDuration.trim();
				if(selectedDuration.compareToIgnoreCase("Today") == 0)
				{
					this.currentDuration  = Constants.TODAY;
				}
				else if(selectedDuration.compareToIgnoreCase("Yesterday") == 0)
				{
					this.currentDuration  = Constants.YESTERDAY;
				}
				
				else if(selectedDuration.compareToIgnoreCase("Last 7 Days") == 0)
				{
					this.currentDuration  = Constants.LAST_SEVEN_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("Last 30 Days") == 0)
				{
					this.currentDuration  = Constants.LAST_THIRTY_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("Last 90 Days") == 0)
				{
					this.currentDuration  = Constants.LAST_NINTY_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("Last 365 Days") == 0)
				{
					this.currentDuration  = Constants.LAST_365_DAYS;
				}
				else if(selectedDuration.compareToIgnoreCase("This week") == 0)
				{
					this.currentDuration  = Constants.THIS_WEEK;
				}
				else if(selectedDuration.compareToIgnoreCase("This month") == 0)
				{
					this.currentDuration  = Constants.THIS_MONTH;
				}
				else if(selectedDuration.compareToIgnoreCase("This year") == 0)
				{
					this.currentDuration  = Constants.THIS_YEAR;
				}
				else if(selectedDuration.compareToIgnoreCase("Last month") == 0)
				{
					this.currentDuration  = Constants.LAST_MONTH;
				}
				else if(selectedDuration.compareToIgnoreCase("Last week") == 0)
				{
					this.currentDuration  = Constants.LAST_WEEK;
				}
				else if(selectedDuration.compareToIgnoreCase("Last year") == 0)
				{
					this.currentDuration  = Constants.LAST_YEAR;
				}
				else
				{
					this.currentDuration  = Constants.TODAY;
				}
				
			}
		
			//clear the alarms clear time text that was displayed earlier.
			
			lblTimeToClrMedium.setText("");
			lblTimeToClrCritical.setText("");
			lblTimeToClrHigh.setText("");
			
			//clear the overview test
			txtUsers.setText("0");
			txtSessions.setText("0");
			txtScreenViews.setText("0");
			txtScreensbyCurrent.setText("0");
			txtActions.setText("0");
			txtBounceRate.setText("0%");
			txtAvgSessionDuration.setText("00:00");
			
			darkBlueLbl.setVisible(false);
			darkerBlueLbl.setVisible(false);
			lightBlueLbl.setVisible(false);
			lighterBlueLbl.setVisible(false);
			
			darkOrnageLbl.setVisible(false);
			darkerOrnageLbl.setVisible(false);
			lightOrangeLbl.setVisible(false);
			lighterOrangeLbl.setVisible(false);
			
			browser2Lbl.setVisible(false);
	        browser1Lbl.setVisible(false);
	        browser3Lbl.setVisible(false);
	        browser4Lbl.setVisible(false);
	        
	        device1Lbl.setVisible(false);
	        device2Lbl.setVisible(false);
	        device3Lbl.setVisible(false);
	        device4Lbl.setVisible(false);
	        
			if(this.currentProject.compareToIgnoreCase("All Projects") == 0)
			{
				populateData(this.currentDuration , null, true);
			}
			else
			{
				populateData(this.currentDuration , this.currentProject, false);
			}
			
		}
		
		
		
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_SLIDING_PREV) == 0)
		{
			
			if(top_left.selectedIndex == 0)
			{
				indexToShow = top_left._panels.size() - 1;
			}
			else
			{
				indexToShow = top_left.selectedIndex - 1;
			}
			this.slideNo = indexToShow;
			this.sliderEffect(indexToShow);
			
		}
		else if(actionCommad.compareToIgnoreCase(Constants.CMD_SLIDING_NEXT) == 0)
		{
			if(top_left.selectedIndex == (top_left.actNoOfPanels - 1))
			{
				indexToShow = 0;
			}
			else
			{
				indexToShow = top_left.selectedIndex + 1;
			}
			this.slideNo = indexToShow;
			this.sliderEffect(indexToShow);
		}
		//Actions on top left sliding buttons BY Omkar
		
				else
					if(arg0.getActionCommand().compareToIgnoreCase(Constants.COMMAND_FIRST_SLIDE_BTN) == 0){
					
			
					
					top_left.firstSlideBtn.setIcon(highlightWhite);
					top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
					this.sliderEffect(0);
					this.slideNo = 0;
//					validate();
//					repaint();
				}
					else
						if(arg0.getActionCommand().compareToIgnoreCase(Constants.COMMAND_SECOND_SLIDE_BTN) == 0){
					
					
					top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.secondSlideBtn.setIcon(highlightWhite);
					top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
					
					this.sliderEffect(1);
					this.slideNo = 1;
//					validate();
//					repaint();
				}
				else
				if(arg0.getActionCommand().compareToIgnoreCase(Constants.COMMAND_THIRD_SLIDE_BTN) == 0){
					
					
					top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.thirdSlideBtn.setIcon(highlightWhite);
					top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
					this.sliderEffect(2);
					this.slideNo = 2;
//					validate();
//					repaint();
				}
				else
					if(arg0.getActionCommand().compareToIgnoreCase(Constants.COMMAND_FOURTH_SLIDE_BTN) == 0){
					
					
					top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.forthSlideBtn.setIcon(highlightWhite);
					top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
					sliderEffect(3);
					this.slideNo = 3;
//					validate();
//					repaint();
				}
				else
				if(arg0.getActionCommand().compareToIgnoreCase(Constants.COMMAND_FIFTH_SLIDE_BTN) == 0){
					
					
					top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.fifthSlideBtn.setIcon(highlightWhite);
					top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
					this.sliderEffect(4);
					this.slideNo = 4;
//					validate();
//					repaint();
				}
				else
					if(arg0.getActionCommand().compareToIgnoreCase(Constants.COMMAND_SIXTH_SLIDE_BTN) == 0){
					
					
					top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
					top_left.sixthSlideBtn.setIcon(highlightWhite);
					this.sliderEffect(5);
					this.slideNo = 5;
//					validate();
//					repaint();
				}
					else
						if(arg0.getActionCommand().compareToIgnoreCase(Constants.COMMAND_ALL_SLIDE_BTN) == 0){
							
							

							top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
							top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
							top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
							top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
							top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
							top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
							this.slideNo = 6;
							this.sliderEffect(6);
							
					
				}
		//Method to show active users report from Report Panel.
						else if(arg0.getActionCommand().compareToIgnoreCase(Constants.ACTIVE_USER_REPORT) == 0){
						}
		top_left.firstSlideBtn.repaint();
		top_left.secondSlideBtn.repaint();
		top_left.thirdSlideBtn.repaint();
		top_left.forthSlideBtn.repaint();
		top_left.fifthSlideBtn.repaint();
		top_left.sixthSlideBtn.repaint();
//		validate();
//		repaint();
		
	}
	
	private void createEngagementChart(Dataset engagementData)
	{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat origFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
		JFreeChart chart = null;
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
		    	 String tempDateStr = "";
		    	 if(engagementData.getValueAt(i, 0) != null)
		    	 {
		    		 try {
						tempDate = origFormat.parse(engagementData.getValueAt(i, 0).toString());
						tempDateStr =  sdf.format(tempDate);
						if(tempDateStr.length() > 13 && (tempDateStr.contains(":") == true))
						{
							tempDateStr = tempDateStr.substring(11);
							String timeVal[] = tempDateStr.split(":");
				
			    		 tempsessionVal =  60 * (Double.parseDouble(timeVal[0])) 
			    				 + Double.parseDouble(timeVal[1]) 
			    				 + (1/60.0) * Double.parseDouble(timeVal[2]);
						}
						} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		
		    	 }
		    	 if(engagementData.getValueAt(i, 1) != null)
		    	 {
		    		 tempUserVal = Integer.parseInt(engagementData.getValueAt(i, 1).toString());
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
		     
			 if(noOfRecords > 0)
			 {
			     dataset.addValue(users1Session, series1, "0-5 minutes");
		    	 dataset.addValue(screens1Session, series2, "0-5 minutes");
		    	 dataset.addValue(users2_5Session, series1, "6-10 minutes");
		    	 dataset.addValue(screens2_5Session, series2, "6-10 minutes");
		    	 dataset.addValue(users6_10Session, series1, "11-30 minutes");
		    	 dataset.addValue(screens6_10Session, series2, "11-30 minutes");
		    	 dataset.addValue(users11_25Session, series1, "31 minutes - 1 hour");
		    	 dataset.addValue(screens11_25Session, series2, "31 minutes - 1 hour");
		    	 dataset.addValue(users26_50Session, series1, "1-2 hours");
		    	 dataset.addValue(screens26_50Session, series2, "1-2 hours");
		    	 dataset.addValue(users50PlusSession, series1, "2+ hours");
		    	 dataset.addValue(screens50PlusSession, series2, "2+ hours");
		    	 
		    	 chart = ChartFactory.createBarChart(
			                "",         // chart title
			                "",               // domain axis label
			                "",                  // range axis label
			                dataset,                  // data
			                PlotOrientation.HORIZONTAL, // orientation
			                true,                     // include legend
			                false,                     // tooltips?
			                false                     // URLs?
			            );
			        chart.setBackgroundPaint(Color.white);
			        
			        // get a reference to the plot for further customisation...
			        CategoryPlot plot = chart.getCategoryPlot();
			        plot.setBackgroundPaint(Color.white);
			        plot.setDomainGridlinePaint(Color.white);
			        plot.setRangeGridlinePaint(Color.white);
			        plot.setOutlineVisible(false);
			        
			        // set the range axis to display integers only...
			        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			        rangeAxis.setVisible(false);
			        rangeAxis.setTickLabelsVisible(false);
			        rangeAxis.setAxisLineVisible(false);
			        rangeAxis.setAutoRangeIncludesZero(false);
			      //  rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			      //  rangeAxis.setVerticalTickLabels(true);
			        // disable bar outlines...
			         BarRenderer renderer = (BarRenderer) plot.getRenderer();
			        renderer.setDrawBarOutline(false);
			        renderer.setShadowVisible(false);
			       // renderer.setItemMargin(-2);
			        renderer.setItemMargin(0);
			        renderer.setSeriesPaint(0, new Color(12, 107, 181));
			        renderer.setSeriesPaint(1, new Color(198,229,248));
			     
			        final CategoryAxis domainAxis = plot.getDomainAxis();
			        //left-align the category labels 
			        CategoryLabelPositions categorylabelpositions = domainAxis.getCategoryLabelPositions(); 
	
			        CategoryLabelPosition categorylabelposition = new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, 0.0D, CategoryLabelWidthType.RANGE, 1.0F); 
			        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.replaceLeftPosition(categorylabelpositions, categorylabelposition));
					
			        domainAxis.setTickLabelPaint(Constants.COLOR_BLACK_TEXT);
			        domainAxis.setAxisLineVisible(false);
			        LegendTitle legend = chart.getLegend();
			        legend.setPosition(RectangleEdge.TOP);
			        legend.setFrame(new BlockBorder(Color.WHITE));
			        
			        chart.setBorderVisible(false);
			       
			       
			 }
		}
		 engChartPanel.setChart(chart);
		 engChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		 engChartPanel.setOpaque(false);
	     engGraphPanel.repaint();
	}
	private void createFrequencyChart(Dataset freqData) {
		JFreeChart chart = null;
		
		if(freqData != null)
		{
			 String series1 = "Users";
		     String series2 = "Screens";
		     int users1Session = 0, screens1Session = 0;
		     int users2_5Session = 0, screens2_5Session = 0;
		     int users6_10Session = 0, screens6_10Session = 0;
		     int users11_25Session = 0, screens11_25Session = 0;
		     int users26_50Session = 0, screens26_50Session = 0;
		     int users50PlusSession = 0, screens50PlusSession = 0;
		     int noOfRecords = 0;
		   
		     int tempUserVal = 0, tempScreenVal = 0;
		     noOfRecords = freqData.getRowCount();
		     
		     
		     DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		     
		     //build the dataset for bar chart
		     for(int i=0; i<noOfRecords; i++)
		     {
		    	 tempUserVal = 0;
		    	 tempScreenVal = 0;
		    	 if(freqData.getValueAt(i, 1) != null)
		    	 {
		    		 tempUserVal = (int)Float.parseFloat(freqData.getValueAt(i, 1).toString());
		    	 }
		    	 if(freqData.getValueAt(i, 3) != null)
		    	 {
		    		 tempScreenVal = (int)Float.parseFloat(freqData.getValueAt(i, 3).toString());
		    	 }
		    	
		    	 if(tempUserVal == 1)
		    	 {
		    		 users1Session++;
		    		 screens1Session = screens1Session + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 2 && tempUserVal <=5)
		    	 {
		    		 users2_5Session++;
		    		 screens2_5Session = screens2_5Session + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 6 && tempUserVal <=10)
		    	 {
		    		 users6_10Session++;
		    		 screens6_10Session = screens6_10Session + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 11 && tempUserVal <=25)
		    	 {
		    		 users11_25Session++;
		    		 screens11_25Session = screens11_25Session + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 26 && tempUserVal <=50)
		    	 {
		    		 users26_50Session++;
		    		 screens26_50Session = screens26_50Session + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 51)
		    	 {
		    		 users50PlusSession++;
		    		 screens50PlusSession = screens50PlusSession + tempScreenVal;
		    	 }
		     }
		     
			 if(noOfRecords > 0)
			 {
			    
			     dataset.addValue(users1Session, series1, "1 Session");
		    	 dataset.addValue(screens1Session, series2, "1 Session");
		    	 dataset.addValue(users2_5Session, series1, "2-5 Sessions");
		    	 dataset.addValue(screens2_5Session, series2, "2-5 Sessions");
		    	 dataset.addValue(users6_10Session, series1, "6-10 Sessions");
		    	 dataset.addValue(screens6_10Session, series2, "6-10 Sessions");
		    	 dataset.addValue(users11_25Session, series1, "11-25 Sessions");
		    	 dataset.addValue(screens11_25Session, series2, "11-25 Sessions");
		    	 dataset.addValue(users26_50Session, series1, "26-50 Sessions");
		    	 dataset.addValue(screens26_50Session, series2, "26-50 Sessions");
		    	 dataset.addValue(users50PlusSession, series1, "50+ Sessions");
		    	 dataset.addValue(screens50PlusSession, series2, "50+ Sessions");
		    	 
		    	  chart = ChartFactory.createBarChart(
			                "",         // chart title
			                "",               // domain axis label
			                "",                  // range axis label
			                dataset,                  // data
			                PlotOrientation.HORIZONTAL, // orientation
			                true,                     // include legend
			                false,                     // tooltips?
			                false                     // URLs?
			            );
			        chart.setBackgroundPaint(Color.white);
			        
			        // get a reference to the plot for further customisation...
			        CategoryPlot plot = chart.getCategoryPlot();
			        plot.setBackgroundPaint(Color.white);
			        plot.setDomainGridlinePaint(Color.white);
			        plot.setRangeGridlinePaint(Color.white);
			     
			        plot.setOutlineVisible(false);
			        // set the range axis to display integers only...
			        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			        rangeAxis.setVisible(false);
			        rangeAxis.setTickLabelsVisible(false);
			        rangeAxis.setAxisLineVisible(false);
			        rangeAxis.setAutoRangeIncludesZero(false);
			      //  rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			      //  rangeAxis.setVerticalTickLabels(true);
			        // disable bar outlines...
			         BarRenderer renderer = (BarRenderer) plot.getRenderer();
			        renderer.setDrawBarOutline(false);
			        renderer.setShadowVisible(false);
			       // renderer.setItemMargin(-2);
			        renderer.setItemMargin(0);
			        renderer.setSeriesPaint(0, new Color(12, 107, 181));
			        renderer.setSeriesPaint(1, new Color(198,229,248));
			     
			        final CategoryAxis domainAxis = plot.getDomainAxis();
			        //left-align the category labels 
			        CategoryLabelPositions categorylabelpositions = domainAxis.getCategoryLabelPositions(); 
	
			        CategoryLabelPosition categorylabelposition = new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, 0.0D, CategoryLabelWidthType.RANGE, 1.0F); 
			        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.replaceLeftPosition(categorylabelpositions, categorylabelposition));
					   
			      //  domainAxis.setCategoryMargin(2);
			        domainAxis.setTickLabelPaint(Constants.COLOR_BLACK_TEXT);
			        domainAxis.setAxisLineVisible(false);
			        LegendTitle legend = chart.getLegend();
			        legend.setPosition(RectangleEdge.TOP);
			        legend.setFrame(new BlockBorder(Color.WHITE));
			        chart.setBorderVisible(false);
			      
			       
			 }
		}
		_freqRecChartPanel.setChart(chart);
        _freqRecChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
        _freqRecChartPanel.setOpaque(false);
        _freqRecChartPanel.repaint();
		
	}


	private void createRecencyChart(Dataset recencyData) {
		
		JFreeChart chart = null;
		if(recencyData != null)
		{
			 String series1 = "Users";
		     String series2 = "Screens";
		     int users1Day = 0, screens1Days = 0;
		     int users2_5Days = 0, screens2_5days = 0;
		     int users6_10Days = 0, screens6_10Days = 0;
		     int users11_25Days = 0, screens11_25Days = 0;
		     int users26_50Days = 0, screens26_50days = 0;
		     int users50PlusDays = 0, screens50PlusDays = 0;
		     int noOfRecords = 0;
		   
		     int tempUserVal = 0, tempScreenVal = 0;
		     noOfRecords = recencyData.getRowCount();
		     
		     
		     DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		     
		     //build the dataset for bar chart
		     for(int i=0; i<noOfRecords; i++)
		     {
		    	 tempUserVal = 0;
		    	 tempScreenVal = 0;
		    	 if(recencyData.getValueAt(i, 1) != null)
		    	 {
		    		 tempUserVal = (int)Float.parseFloat(recencyData.getValueAt(i, 1).toString());
		    	 }
		    	 
		    	 if(recencyData.getValueAt(i, 2) != null)
		    	 {
		    		 tempScreenVal = (int)Float.parseFloat(recencyData.getValueAt(i, 2).toString());
		    	 }
		    	
		    	 if(tempUserVal <= 1)
		    	 {
		    		 users1Day++;
		    		 screens1Days = screens1Days + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 2 && tempUserVal <=5)
		    	 {
		    		 users2_5Days++;
		    		 screens2_5days = screens2_5days + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 6 && tempUserVal <=10)
		    	 {
		    		 users6_10Days++;
		    		 screens6_10Days = screens6_10Days + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 11 && tempUserVal <=25)
		    	 {
		    		 users11_25Days++;
		    		 screens11_25Days = screens11_25Days + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 26 && tempUserVal <=50)
		    	 {
		    		 users26_50Days++;
		    		 screens26_50days = screens26_50days + tempScreenVal;
		    	 }
		    	 else if(tempUserVal >= 51)
		    	 {
		    		 users50PlusDays++;
		    		 screens50PlusDays = screens50PlusDays + tempScreenVal;
		    	 }
		     }
		     
		     if(noOfRecords > 0)
		     {
				    
			     dataset.addValue(users1Day, series1, "1 Day");
		    	 dataset.addValue(screens1Days, series2, "1 Day");
		    	 dataset.addValue(users2_5Days, series1, "2-5 Days");
		    	 dataset.addValue(screens2_5days, series2, "2-5 Days");
		    	 dataset.addValue(users6_10Days, series1, "6-10 Days");
		    	 dataset.addValue(screens6_10Days, series2, "6-10 Days");
		    	 dataset.addValue(users11_25Days, series1, "11-25 Days");
		    	 dataset.addValue(screens11_25Days, series2, "11-25 Days");
		    	 dataset.addValue(users26_50Days, series1, "26-50 Days");
		    	 dataset.addValue(screens26_50days, series2, "26-50 Days");
		    	 dataset.addValue(users50PlusDays, series1, "50+ Days");
		    	 dataset.addValue(screens50PlusDays, series2, "50+ Days");
		    	 
		    	 chart = ChartFactory.createBarChart(
			                "",         // chart title
			                "",               // domain axis label
			                "",                  // range axis label
			                dataset,                  // data
			                PlotOrientation.HORIZONTAL, // orientation
			                true,                     // include legend
			                false,                     // tooltips?
			                false                     // URLs?
			            );
			        chart.setBackgroundPaint(Color.white);
			        chart.setBorderVisible(false);
			        
			        // get a reference to the plot for further customisation...
			        CategoryPlot plot = chart.getCategoryPlot();
			        plot.setBackgroundPaint(Color.white);
			        plot.setDomainGridlinePaint(Color.white);
			        plot.setRangeGridlinePaint(Color.white);
			        plot.setOutlineVisible(false);
			        
			        // set the range axis to display integers only...
			        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			        rangeAxis.setVisible(false);
			        rangeAxis.setTickLabelsVisible(false);
			        rangeAxis.setAxisLineVisible(false);
			       rangeAxis.setAutoRangeIncludesZero(false);
			      //  rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			      //  rangeAxis.setVerticalTickLabels(true);
			        // disable bar outlines...
			         BarRenderer renderer = (BarRenderer) plot.getRenderer();
			        renderer.setDrawBarOutline(false);
			        renderer.setItemMargin(0);
			        renderer.setShadowVisible(false);
			        renderer.setSeriesPaint(0, new Color(12, 107, 181));
			        renderer.setSeriesPaint(1, new Color(198,229,248));
			    
			        
			       
			        final CategoryAxis domainAxis = plot.getDomainAxis();
			      
			      //left-align the category labels 
			        CategoryLabelPositions categorylabelpositions = domainAxis.getCategoryLabelPositions(); 
	
			        CategoryLabelPosition categorylabelposition = new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, 0.0D, CategoryLabelWidthType.RANGE, 1.0F); 
	
			        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.replaceLeftPosition(categorylabelpositions, categorylabelposition));
			       // domainAxis.setCategoryMargin(2);
			        domainAxis.setAxisLineVisible(false);
			        domainAxis.setTickLabelPaint(Constants.COLOR_BLACK_TEXT);
			      
			        LegendTitle legend = chart.getLegend();
			        legend.setPosition(RectangleEdge.TOP);
			        legend.setFrame(new BlockBorder(Color.WHITE));
			        
			       
		     }
		}
		 _freqRecChartPanel.setChart(chart);
	     _freqRecChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
	     _freqRecChartPanel.setOpaque(false);
	     _freqRecChartPanel.repaint();
		
	}
	
	private void createActiveUsersChart(int chartType)
	{
		
		//Add a line chart with no of active users over a duration
				int noOfActiveUsers = 0, i, dataSetCol = 0;
				
						DefaultCategoryDataset dsActiveUsers = new DefaultCategoryDataset();
						JFreeChart _lineChartActiveUsers;
						switch(chartType)
						{
						case 1:
							dataSetCol = 1;
							break;
						case 7:
							dataSetCol = 2;
							break;
						case 14:
							dataSetCol = 3;
							break;
						}	
				if(activeUsersData != null)
				{
					//create a line chart to show total actions and total sessions for various durations
				
				
					noOfActiveUsers = activeUsersData.getRowCount();
					
					int startVal = 0;
					 if(currentDuration == Constants.LAST_365_DAYS){
							Integer curMonth = Calendar.getInstance().get(Calendar.MONTH);
							
							if (curMonth == 11) {
								curMonth = 1;
							} else {
								curMonth = curMonth + 2;
							}	
							System.out.println("curMonth : " + curMonth);
							startVal = Constants.binarySearchOnDataset(4, curMonth, activeUsersData);
							
							if (startVal < 0) {
								startVal = 0;
							}
							
							System.out.println("startVal : " + startVal + ", dataset length: " + noOfActiveUsers );
							for (i = startVal; i < noOfActiveUsers; i++) {
								if(activeUsersData.getValueAt(i, dataSetCol) != null)
								{
									if(activeUsersData.getValueAt(i, 0) != null)
									{
										dsActiveUsers.addValue((int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString()), "users", activeUsersData.getValueAt(i, 0).toString());
									}
									else
									{
										dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
									}
								}
								else
								{
									dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
								}
							}
							
							for (i = 0; i < startVal; i++) {
								if(activeUsersData.getValueAt(i, dataSetCol) != null)
								{
									if(activeUsersData.getValueAt(i, 0) != null)
									{
										dsActiveUsers.addValue((int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString()), "users", activeUsersData.getValueAt(i, 0).toString());
									}
									else
									{
										dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
									}
								}
								else
								{
									dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
								}
							}
						}
				
				else
				{
					for(i=0; i<noOfActiveUsers; i++)
					{
						if(activeUsersData.getValueAt(i, dataSetCol) != null)
						{
							if(activeUsersData.getValueAt(i, 0) != null)
							{
								dsActiveUsers.addValue((int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString()), "users", activeUsersData.getValueAt(i, 0).toString());
							}
							else
							{
								dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
							}
						}
						else
						{
							dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
						}
					}
				}
					 			
					

					
					
					int skipCount = 1;
					if(noOfActiveUsers > 7)
					{
						skipCount = 2;
					}
					else if(noOfActiveUsers > 15)
					{
						skipCount = 3;
					}
					else if(noOfActiveUsers > 30)
					{
						skipCount = 4;
					}
					
					 CategoryAxis domainAxis1 = null;
					 domainAxis1 = new CategoryAxisSkipLabels(skipCount);
			        domainAxis1.setTickMarksVisible(true);
			      

					CategoryLabelPositions p = domainAxis1.getCategoryLabelPositions();

					CategoryLabelPosition left = new CategoryLabelPosition(
					    RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, 
					    TextAnchor.CENTER_LEFT, 0.0,
					    CategoryLabelWidthType.RANGE, 0.70f //Assign 70% of space for category labels
					);

					domainAxis1.setCategoryLabelPositions(CategoryLabelPositions
					        .replaceLeftPosition(p, left));

			        domainAxis1.setCategoryMargin(0.0);
			        domainAxis1.setLowerMargin(0.0);
			        domainAxis1.setTickMarksVisible(true);
			        domainAxis1.setTickLabelInsets(new RectangleInsets(0.0,0.0,0.0,0.0));
			       // ((CategoryAxisSkipLabels)domainAxis1).setDisplaySkippedTickMarks(false);
			        domainAxis1.setVisible(true);
			        
			        domainAxis1.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			     //   domainAxis.setTickLabelPaint(Constants.COLOR_PLOT_LABEL);
			        
			        
					domainAxis1.setAxisLineVisible(false);
					LineAndShapeRenderer renderer = new LineAndShapeRenderer();
			        renderer.setSeriesPaint(0,new Color(12, 107, 181));
			      
			        Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 5.0f, 5.0f);
			       
			        //to set the series marker shape to circle , default is square
			        renderer.setSeriesShape(0, circle);
			        renderer.setBaseShapesVisible(true);
			        renderer.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
			        NumberAxis rangeAxis1 = new NumberAxis("");
			        
			        CategoryPlot plot = new CategoryPlot(
			        		dsActiveUsers, domainAxis1, rangeAxis1, renderer
			        );
			        plot.setAxisOffset(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
					
			        //_lineChartActiveUsers = ChartFactory.createLineChart("", "", " ", dsActiveUsers, PlotOrientation.VERTICAL, false, true, true );
					
			        
			        _lineChartActiveUsers = new JFreeChart(plot);
			        _lineChartActiveUsers.removeLegend();
			        _lineChartActiveUsers.setBackgroundImageAlpha(0.0f);
					_lineChartActiveUsers.setBackgroundPaint(Color.WHITE);
					
					
					CategoryPlot _auplot = (CategoryPlot) _lineChartActiveUsers.getPlot();
					
					_auplot.setBackgroundImageAlpha(0.0f);
				//	_auplot.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
					_auplot.setBackgroundPaint(Color.WHITE);
					_auplot.setRangeGridlinePaint(Constants.COLOR_WHITE_BACKGROUND); 
					_auplot.setOutlineVisible(false);

			       
			        NumberAxis aurangeAxis = (NumberAxis) _auplot.getRangeAxis();
			        aurangeAxis.setVisible(true); 
			        aurangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			        aurangeAxis.setAutoRangeIncludesZero(true);
			        aurangeAxis.setAxisLineVisible(false);
			        
			        _AUartPanel.setChart(_lineChartActiveUsers);
			
					_AUartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
					_AUartPanel.setOpaque(false);
					//_AUartPanel.setPreferredSize(new Dimension(350, 250));
					
					_AUartPanel.repaint();

				}

		}
	
	private void createLineChart(Dataset graphData, String seriesName)
	{
		
		
		//create the line and bar charts 
		
				DefaultCategoryDataset _lineData = new DefaultCategoryDataset();
				JFreeChart _lineChart;
				
				//create a line chart to show total actions and total sessions for various durations
				//retrieve the series data from gateway using RPC 
				if(graphData != null)
				{
					int noOfRows = graphData.getRowCount();
					int i = 0;
					String usersValue = "";
					int startVal = 0;
					if(this.currentDuration == Constants.LAST_365_DAYS)
					{
						Integer curMonth = Calendar.getInstance().get(Calendar.MONTH);

						if (curMonth == 11) {
							curMonth = 1;
						} else {
							curMonth = curMonth + 2;
						}
						startVal = Constants.binarySearchOnDataset(2, curMonth, graphData);

						if (startVal < 0) {
							startVal = 0;
						}
						
						for (i = startVal; i < noOfRows; i++) {

							if(graphData.getValueAt(i, 1) != null)
							{
								_lineData.addValue((int)Float.parseFloat(graphData.getValueAt(i, 1).toString()), seriesName, graphData.getValueAt(i, 0).toString());
							}
							else
							{
								_lineData.addValue(0,seriesName, graphData.getValueAt(i, 0).toString());
							}
							

						}

						for (i = 0; i < startVal; i++) {

							if(graphData.getValueAt(i, 1) != null)
							{
								_lineData.addValue((int)Float.parseFloat(graphData.getValueAt(i, 1).toString()), seriesName, graphData.getValueAt(i, 0).toString());
							}
							else
							{
								_lineData.addValue(0,seriesName, graphData.getValueAt(i, 0).toString());
							}
							

						}
					}
					else
					{
						for(i=0; i<noOfRows; i++)
						{
							if(graphData.getValueAt(i, 1) != null)
							{
								_lineData.addValue((int)Float.parseFloat(graphData.getValueAt(i, 1).toString()), seriesName, graphData.getValueAt(i, 0).toString());
							}
							else
							{
								_lineData.addValue(0,seriesName, graphData.getValueAt(i, 0).toString());
							}
						}
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
			        renderer.setDrawOutlines(false);
			        
			        Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 5.0f, 5.0f);
			       
			        //to set the series marker shape to circle , default is square
			        renderer.setSeriesShape(0, circle);
			        renderer.setBaseShapesVisible(true);
			        
			        NumberAxis rangeAxis = (NumberAxis) _plot.getRangeAxis();
					rangeAxis.setVisible(true); 
					rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
					rangeAxis.setTickLabelPaint(Constants.COLOR_COMBO_BACKGROUND);
			        rangeAxis.setAutoRangeIncludesZero(true);
			        rangeAxis.setAxisLineVisible(true);
			        //rangeAxis.setAxisLinePaint(Constants.COLOR_GREY_LABEL);
			        rangeAxis.setAxisLineVisible(false);
			      
			        final CategoryAxis domainAxis = _plot.getDomainAxis();
			        domainAxis.setVisible(true);
			        domainAxis.setTickLabelPaint(Constants.COLOR_COMBO_BACKGROUND);
					domainAxis.setAxisLineVisible(false);
	
					_timelineChartPanel.setChart(_lineChart);
			        _timelineChartPanel.setBackground(Color.DARK_GRAY);
					_timelineChartPanel.setOpaque(false);
					//_timelineChartPanel.setPreferredSize(new Dimension(1000,150));
					_timelineChartPanel.repaint();
					
				}
	}
	//Omkar Slider Effect Function 
		public void sliderEffect (int indexToShow1){
			top_left.selectedIndex = indexToShow1;
			
			
			top_left.middlePane.setPreferredSize(new Dimension(170,100));
			
		
			Dataset graphData = null;
			
			switch(indexToShow1)
			{
			
			
			case 0:
				
				graphData = rpc.getTotalVisitsData( this.currentProject, this.allProjects, this.currentDuration);
				createLineChart(graphData, "Total Visits");
				
				top_left.firstSlideBtn.setIcon(highlightWhite);
				top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
				break;
			case 1:
				graphData = rpc.getTotalUsersData( this.currentProject, this.allProjects, this.currentDuration);
				createLineChart(graphData, "Total Users");
				top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.secondSlideBtn.setIcon(highlightWhite);
				top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
				break;
			case 2:
				graphData = rpc.getTotalScreenViewsData(this.currentProject, this.allProjects, this.currentDuration);
				createLineChart(graphData, "Screen Views");
				top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.thirdSlideBtn.setIcon(highlightWhite);
				top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
				break;
			case 3:
				graphData = rpc.getBounceRateData( this.currentProject, this.allProjects, this.currentDuration);
				createLineChart(graphData, "Bounce Rate");
				top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.forthSlideBtn.setIcon(highlightWhite);
				top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
				break;
			case 4:
				graphData = rpc.getAvgSessionData(this.currentProject, this.allProjects, this.currentDuration);
				createLineChart(graphData, "Avg. Session");
				top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.fifthSlideBtn.setIcon(highlightWhite);
				top_left.sixthSlideBtn.setIcon(nonHighlightGreyWhite);
				break;
			case 5:
				graphData = rpc.getAvgScreenViewsData( this.currentProject, this.allProjects, this.currentDuration);
				createLineChart(graphData, "Avg. Screens/Visit");
				top_left.firstSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.secondSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.thirdSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.forthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.fifthSlideBtn.setIcon(nonHighlightGreyWhite);
				top_left.sixthSlideBtn.setIcon(highlightWhite);
				break;
			case 6:
				this.top_left.middlePane.setPreferredSize(new Dimension(250,100));
				_timelineChartPanel.setChart(null);
				_timelineChartPanel.repaint();
				break;
			
			}
			try
			{
				top_left.middlePane.removeAll();
				System.out.println("indexToShow1 : " +indexToShow1);
			
				top_left.middlePane.add(top_left._panels.get(indexToShow1));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
//			revalidate();
//			repaint();
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(arg0.getSource() instanceof  RoundedButton)
			{
				RoundedButton currentBtn = (RoundedButton) arg0.getSource();
			
			
			if(currentBtn.getName().compareToIgnoreCase("btn1DayActiveUsers") == 0)
			{
				oneDayActive= true;
				sevenDayActive = false;
				fourteenDayActive = false;
				btn1DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
				btn1DayActiveUsers.setTextColor(Constants.COLOR_WHITE_BACKGROUND);
				btn14DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				btn14DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
				btn7DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				btn7DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
				createActiveUsersChart(1);
			}
			else if(currentBtn.getName().compareToIgnoreCase("btn7DayActiveUsers") == 0)
			{
				oneDayActive= false;
				sevenDayActive = true;
				fourteenDayActive = false;
				btn7DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
				btn7DayActiveUsers.setTextColor(Constants.COLOR_WHITE_BACKGROUND);
				btn1DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				btn1DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
				btn14DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				btn14DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
				createActiveUsersChart(7);
			}
			else if(currentBtn.getName().compareToIgnoreCase("btn14DayActiveUsers") == 0)
			{
				oneDayActive= false;
				sevenDayActive = false;
				fourteenDayActive = true;
				
				btn14DayActiveUsers.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
				btn14DayActiveUsers.setTextColor(Constants.COLOR_WHITE_BACKGROUND);
				btn1DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				btn1DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
				btn7DayActiveUsers.setBackground(Constants.COLOR_WHITE_BACKGROUND);
				btn7DayActiveUsers.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL);
				createActiveUsersChart(14);
			}
			}
			else if(arg0.getSource() instanceof RoundedButton_WideLabel)
			{
				RoundedButton_WideLabel currentBtn = (RoundedButton_WideLabel) arg0.getSource();
				
				 if(currentBtn.getName().compareToIgnoreCase("btnCountOfSessions") == 0)
				{
					showFrequency = true;
					showRecency = false;
					btnCountOfSessions.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
					btnCountOfSessions.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
					
					btnDaysSinceLogin.setBackground(Constants.COLOR_WHITE_BACKGROUND);
					btnDaysSinceLogin.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
					Dataset freqData = rpc.getFrequencyInformation( this.currentProject, this.allProjects, this.currentDuration);
					createFrequencyChart(freqData);
				}
				else if(currentBtn.getName().compareToIgnoreCase("btnDaysSinceLogin") == 0)
				{
					showFrequency = false;
					showRecency = true;
					btnCountOfSessions.setBackground(Constants.COLOR_WHITE_BACKGROUND);
					btnCountOfSessions.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblLowerLeft.setForeground(Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblLowerRight.setForeground(Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblMiddleLeft.setForeground(Constants.COLOR_WHITE_BACKGROUND);
//					btnCountOfSessions.lblMiddleRight.setForeground(Constants.COLOR_WHITE_BACKGROUND);
					btnDaysSinceLogin.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
					btnDaysSinceLogin.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
					Dataset recencyData = rpc.getDaysSinceLastLoginPerUser( this.currentProject, this.allProjects, this.currentDuration);
					createRecencyChart(recencyData);
				}
				else if(currentBtn.getName().compareToIgnoreCase("btnEngageDuration") == 0)
				{
					screenDepth = false;
					screenViews = true;
					btnEngageDepth.setBackground(Constants.COLOR_WHITE_BACKGROUND);
					btnEngageDepth.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
					btnEngageDuration.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
					btnEngageDuration.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
					
					Dataset engage  = rpc.getEngagementInformation( this.currentProject, this.allProjects, this.currentDuration);
					createEngagementChart(engage);
				}
				else if(currentBtn.getName().compareToIgnoreCase("btnEngageDepth") == 0)
				{
					screenDepth = true;
					screenViews = false;
					btnEngageDuration.setBackground(Constants.COLOR_WHITE_BACKGROUND);
					btnEngageDuration.setTextColor(Constants.COLOR_BUTTON_GREY_LABEL, Constants.COLOR_WHITE_BACKGROUND);
					btnEngageDepth.setBackground(Constants.COLOR_BUTTON_GREY_BACKGROUND);
					btnEngageDepth.setTextColor(Constants.COLOR_WHITE_BACKGROUND, Constants.COLOR_WHITE_BACKGROUND);
					
					Dataset engageDepth  = rpc.getEngagementInformationScreenDepth( this.currentProject, this.allProjects, this.currentDuration);
					createEngagementChartScreenDepth(engageDepth);
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
	
		//By Omkar
		
			private void createEngagementChartScreenDepth(Dataset engagementData)
			{
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(engagementData != null)
				{
					String series1 = "Sessions";
				    String series2 = "Screen Views";
				    
				    int sessions0_2Screens = 0, screens0_2 = 0;
				     int sessions3_4Screens = 0, screens3_4= 0;
				     int sessions5_7Screens = 0, screens5_7 = 0;
				     int sessions8_10Screens = 0, screens8_10 = 0;
				     int sessions11_15Screens = 0, screens11_15 = 0;
				     int sessions16PlusScreens = 0, screens16Plus = 0;
				     int noOfRecords = 0;
				   
				     int tempScreensVal = 0;
				     int tempUserVal = 0;
				     noOfRecords = engagementData.getRowCount();
				     Date tempDate = null;
				     
				     DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				     
				     //build the dataset for bar chart
				     for(int i=0; i<noOfRecords; i++)
				     {
				    	 tempScreensVal = 0;
				    	 tempUserVal = 0;
				    	 tempDate = null;
				    	 
				    	 if(engagementData.getValueAt(i, 0) != null)
				    	 {
				    		 tempUserVal = Integer.parseInt(engagementData.getValueAt(i, 0).toString());
				    	 }
				    	 if(engagementData.getValueAt(i, 1) != null)
				    	 {
				    		 tempScreensVal = (int)Double.parseDouble(engagementData.getValueAt(i, 1).toString());
				    	 }
				    	 if(tempScreensVal <= 2.0)
				    	 {
				    		 sessions0_2Screens = sessions0_2Screens + tempUserVal;
				    		 screens0_2 = screens0_2 + tempScreensVal;
				    	 }
				    	 else if(tempScreensVal > 2.0 && tempScreensVal <=4.0)
				    	 {
				    		 sessions3_4Screens += tempUserVal;
				    		 screens3_4 = screens3_4 + tempScreensVal;
				    	 }
				    	 else if(tempScreensVal > 4.0 && tempScreensVal <=7.0)
				    	 {
				    		 sessions5_7Screens += tempUserVal;
				    		 screens5_7 = screens5_7 + tempScreensVal;
				    	 }
				    	 else if(tempScreensVal > 7.0 && tempScreensVal <=10.0)
				    	 {
				    		 sessions8_10Screens += tempUserVal;
				    		 screens8_10 = screens8_10 + tempScreensVal;
				    	 }
				    	 else if(tempScreensVal > 10.0 && tempScreensVal <=15.0)
				    	 {
				    		 sessions11_15Screens += tempUserVal;
				    		 screens11_15 = screens11_15 + tempScreensVal;
				    	 }
				    	 else if(tempScreensVal > 15.0)
				    	 {
				    		 sessions16PlusScreens += tempUserVal;
				    		 screens16Plus = screens16Plus + tempScreensVal;
				    	 }
				     }
				     
					 if(noOfRecords > 0)
					 {
					     dataset.addValue(sessions0_2Screens, series1, "0-2 screens");
					     dataset.addValue(screens0_2, series2, "0-2 screens");
				    	 dataset.addValue(sessions3_4Screens, series1, "3-4 screens");
				    	 dataset.addValue(screens3_4, series2, "3-4 screens");
				    	 dataset.addValue(sessions5_7Screens, series1, "5-7 screens");
				    	 dataset.addValue(screens5_7, series2, "5-7 screens");
				    	 dataset.addValue(sessions8_10Screens, series1, "8-10 screens");
				    	 dataset.addValue(screens8_10, series2, "8-10 screens");
				    	 dataset.addValue(sessions11_15Screens, series1, "11-15 screens");
				    	 dataset.addValue(screens11_15, series2, "11-15 screens");
				    	 dataset.addValue(sessions16PlusScreens, series1, "16+ screens");
				    	 dataset.addValue(screens16Plus, series2, "16+ screens");
				    	 
				    	 JFreeChart chartScreenDepth = ChartFactory.createBarChart(
					                "",         // chart title
					                "",               // domain axis label
					                "",                  // range axis label
					                dataset,                  // data
					                PlotOrientation.HORIZONTAL, // orientation
					                true,                     // include legend
					                false,                     // tooltips?
					                false                     // URLs?
					            );
				    	 chartScreenDepth.setBackgroundPaint(Color.white);
					        
					        // get a reference to the plot for further customisation...
					        CategoryPlot plot = chartScreenDepth.getCategoryPlot();
					        plot.setBackgroundPaint(Color.white);
					        plot.setDomainGridlinePaint(Color.white);
					        plot.setRangeGridlinePaint(Color.white);
					        plot.setOutlineVisible(false);
					        
					        // set the range axis to display integers only...
					        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
					        rangeAxis.setVisible(false);
					        rangeAxis.setTickLabelsVisible(false);
					        rangeAxis.setAxisLineVisible(false);
					        rangeAxis.setAutoRangeIncludesZero(false);
					      //  rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
					      //  rangeAxis.setVerticalTickLabels(true);
					        // disable bar outlines...
					         BarRenderer renderer = (BarRenderer) plot.getRenderer();
					        renderer.setDrawBarOutline(false);
					        renderer.setShadowVisible(false);
					       // renderer.setItemMargin(-2);
					        renderer.setItemMargin(0);
					        renderer.setSeriesPaint(0, new Color(12, 107, 181));
					        renderer.setSeriesPaint(1, new Color(198,229,248));
					     
					        final CategoryAxis domainAxis = plot.getDomainAxis();
					        //left-align the category labels 
					        CategoryLabelPositions categorylabelpositions = domainAxis.getCategoryLabelPositions(); 
			
					        CategoryLabelPosition categorylabelposition = new CategoryLabelPosition(RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, TextAnchor.CENTER_LEFT, 0.0D, CategoryLabelWidthType.RANGE, 1.0F); 
					        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.replaceLeftPosition(categorylabelpositions, categorylabelposition));
							
					        domainAxis.setTickLabelPaint(Constants.COLOR_BLACK_TEXT);
					        domainAxis.setAxisLineVisible(false);
					        LegendTitle legend = chartScreenDepth.getLegend();
					        legend.setPosition(RectangleEdge.TOP);
					        legend.setFrame(new BlockBorder(Color.WHITE));
					        
					        chartScreenDepth.setBorderVisible(false);
					       
					       // PMIChartPanel chartPanel = new PMIChartPanel(chart);
					        engChartPanel.setChart(chartScreenDepth);
					        engGraphPanel.repaint();
					     
//					        revalidate();
//					        repaint();
					 }
				}
			}
	

}
