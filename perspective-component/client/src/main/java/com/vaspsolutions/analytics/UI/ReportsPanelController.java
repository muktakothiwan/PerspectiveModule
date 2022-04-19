package com.vaspsolutions.analytics.UI;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.batik.ext.awt.image.TableTransfer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.MarkerAxisBand;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryStepRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import com.inductiveautomation.factorypmi.application.components.PMITable;
import com.inductiveautomation.ignition.common.Dataset;
import com.vaspsolutions.analytics.common.ActiveUsersInfo;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.GroupReportRecord;
import com.vaspsolutions.analytics.common.MODIAServiceUnavailableException;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.OverviewInformation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Shape;

import javax.swing.JLabel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.LineBorder;
import javax.swing.JComboBox;

/**
 * Class to represent the Reports View.
 * 
 * @author YM : Created on 06/19/2015.
 * 
 */

public class ReportsPanelController extends JPanel implements
		TreeSelectionListener, ActionListener, MouseListener {

	private ModuleRPC rpc;
	private String project;
	private String gateway;
	private String dataSource;

	AlarmSummaryReportPanelController alarmSummaryPanel;
	
	
	JPanel categoryPanel; // to hold menu

	JTree menuTree; // left side categories
	private JPanel reports;
	private JPanel reportGraph;
	private JScrollPane reportDetails;
	private boolean reportForAllGateways;
	private boolean reportForAllProjects;

	public String selectedReportMenu = "";
	private JPanel headerPanel;
	private JLabel lblreportTitle;
	private JPanel panel;
	private JComboBox comboBox;

	int constantDuration;
	int noOfPeople;
	int columnSelected = -1;
	ReportsTable currentTable;
	boolean sortOrder = true;
	ChartPanel _reportChartPanel;
	JScrollPane upperScroll;

	/* changes for sorting */
	DefaultTableModel summaryRow = new DefaultTableModel(0, 3);

	public ReportsPanelController(ModuleRPC _rpc, String _gateway,
			String _project, String _datasource) {

		//following line hides the lines from the report menu tree
				UIManager.put("Synthetica.tree.line.type", "NONE");
		// initialize local members
		this.setSize(1720, 1080);
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		rpc = _rpc;
		project = _project;
		gateway = _gateway;
		dataSource = _datasource;
		if ((gateway.compareToIgnoreCase("All Gateways") == 0)) {
			reportForAllGateways = true;
		} else {
			reportForAllGateways = false;
		}
		if ((project.compareToIgnoreCase("All Projects") == 0)) {
			reportForAllProjects = true;
		} else {
			reportForAllProjects = false;
		}

		this.noOfPeople = rpc.getNumberOfActiveUsersOnController(
				constantDuration, gateway, project, reportForAllGateways,
				reportForAllProjects);
		System.out.println("people: "+ this.noOfPeople);
		// set the layout

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 200, 1500, 0 };
		gridBagLayout.rowHeights = new int[] { 1070 };
		gridBagLayout.columnWeights = new double[] { Double.MIN_VALUE, 1.0,
				Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { Double.MIN_VALUE };
		setLayout(gridBagLayout);

		constantDuration = Constants.LAST_THIRTY_DAYS;

		categoryPanel = new JPanel();
		categoryPanel.setBorder(new MatteBorder(0, 0, 0, 1,
				(Color) Color.LIGHT_GRAY));
		categoryPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_categoryPanel = new GridBagConstraints();
		gbc_categoryPanel.insets = new Insets(0, 0, 0, 0);
		gbc_categoryPanel.fill = GridBagConstraints.BOTH;
		gbc_categoryPanel.gridx = 0;
		gbc_categoryPanel.gridy = 0;
		add(categoryPanel, gbc_categoryPanel);

		reports = new JPanel();
		reports.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_reports = new GridBagConstraints();
		gbc_reports.fill = GridBagConstraints.BOTH;
		gbc_reports.gridx = 1;
		gbc_reports.gridy = 0;
		add(reports, gbc_reports);
		GridBagLayout gbl_reports = new GridBagLayout();
		gbl_reports.columnWidths = new int[] { 0, 0 };
		gbl_reports.rowHeights = new int[] { 20, 200, 860 };
		gbl_reports.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_reports.rowWeights = new double[] { 0.0, 0.0, 1.0 };
		reports.setLayout(gbl_reports);

		headerPanel = new JPanel();
		headerPanel.setBorder(new MatteBorder(1, 0, 1, 0,
				(Color) Constants.COLOR_GRADIENT_LINE));
		headerPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_headerPanel = new GridBagConstraints();
		gbc_headerPanel.insets = new Insets(0, 0, 0, 0);
		gbc_headerPanel.fill = GridBagConstraints.BOTH;
		gbc_headerPanel.gridx = 0;
		gbc_headerPanel.gridy = 0;
		reports.add(headerPanel, gbc_headerPanel);
		headerPanel.setLayout(new BorderLayout(0, 0));

		lblreportTitle = new JLabel("");
		lblreportTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblreportTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
		headerPanel.add(lblreportTitle, BorderLayout.CENTER);

		reportGraph = new JPanel();
		reportGraph.setPreferredSize(new Dimension(1500, 200));
		// reportGraph.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		reportGraph.setBackground(Color.WHITE);
		GridBagConstraints gbc_reportGraph = new GridBagConstraints();
		gbc_reportGraph.insets = new Insets(0, 0, 0, 0);
		gbc_reportGraph.fill = GridBagConstraints.BOTH;
		gbc_reportGraph.gridx = 0;
		gbc_reportGraph.gridy = 1;
		reports.add(reportGraph, gbc_reportGraph);
		reportGraph.setLayout(new BorderLayout(0, 0));

		// JFREE Chart Panel
		_reportChartPanel = new ChartPanel(null);
		_reportChartPanel.setBackground(Color.DARK_GRAY);
		_reportChartPanel.setOpaque(false);
		_reportChartPanel.setPreferredSize(new Dimension(1400, 200));
		reportGraph.add(_reportChartPanel, BorderLayout.CENTER);

		reportDetails = new JScrollPane();
		reportDetails.setBorder(BorderFactory.createEmptyBorder());

		reportDetails.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		reportDetails.setPreferredSize(new Dimension(1500, 860));
		GridBagConstraints gbc_reportDetails = new GridBagConstraints();
		gbc_reportDetails.fill = GridBagConstraints.BOTH;
		gbc_reportDetails.gridx = 0;
		gbc_reportDetails.gridy = 2;
		reports.add(reportDetails, gbc_reportDetails);

		reportDetails.getViewport().setBackground(
				Constants.COLOR_WHITE_BACKGROUND);

		// for alarms report scroll to be added in place of chart
		upperScroll = new JScrollPane();
		upperScroll.setPreferredSize(new Dimension(1520, 180));
		upperScroll.setBorder(BorderFactory.createEmptyBorder());

		// alarmSummaryPanel = new
		// AlarmSummaryReportPanel(alarmSummaryTable,rpc,constantDuration,reportForAllProjects,project);
		// //AlarmSummaryReportPanel(ReportsTable reportTable, ModuleRPC rpc,int
		// duration,boolean allProjects, String projecName)
		// //rpc.getAlarmsSummaryReport(project, reportForAllProjects,
		// constantDuration);
		// this.reportDetails.setViewportView(alarmSummaryPanel);

		initializeMenu();

	}

	// create the left hand side menu
	private void initializeMenu() {
		String[] durations = new String[] { "Today", "Yesterday",
				"Last 7 Days", "Last 30 Days", "Last 90 Days", "Last 365 Days",
				"This week", "This month", "This year", "Last month",
				"Last week", "Last year" };

		// menu is created as a JTree.
		DefaultMutableTreeNode treeTop = new DefaultMutableTreeNode("Reports");
		
		// create the Top node of overview menu
		DefaultMutableTreeNode topNodeOverview = new DefaultMutableTreeNode(
				"<html><b>&nbsp;Overview</b><br></html>");

		topNodeOverview.add(new DefaultMutableTreeNode("By Date"));
		topNodeOverview.add(new DefaultMutableTreeNode("Cities"));
		topNodeOverview.add(new DefaultMutableTreeNode("Groups"));
		topNodeOverview.add(new DefaultMutableTreeNode("Top Projects"));
		topNodeOverview.add(new DefaultMutableTreeNode("Top Screens"));
		topNodeOverview.add(new DefaultMutableTreeNode("Bounce Rate"));

		// create the Top node of Systems menu
		DefaultMutableTreeNode topNodeSystems = new DefaultMutableTreeNode(
				"<html><br><b>&nbsp;Systems</b><br></html>");

		topNodeSystems.add(new DefaultMutableTreeNode("Device Types"));
		topNodeSystems.add(new DefaultMutableTreeNode("Platforms"));
		topNodeSystems.add(new DefaultMutableTreeNode("Browsers"));
		topNodeSystems.add(new DefaultMutableTreeNode("Screen Resolutions"));

		DefaultMutableTreeNode topNodebehaviour = new DefaultMutableTreeNode(
				"<html><br><b>&nbsp;Behavior</b><br></html>");

		topNodebehaviour.add(new DefaultMutableTreeNode("Actions per visit"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Visit Duration"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Engagement"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Frequency"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Recency"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Active Users"));

		DefaultMutableTreeNode topNodealarms = new DefaultMutableTreeNode(
				"<html><br><b>&nbsp;Alarms</b><br></html>");
		topNodealarms.add(new DefaultMutableTreeNode("Alarm Summary"));
		
		treeTop.add(topNodeOverview);
		treeTop.add(topNodeSystems);
		treeTop.add(topNodebehaviour);
		treeTop.add(topNodealarms);
		ImageIcon treeNodeIcon = new ImageIcon(getClass().getResource(
				"treenode.png"));
		Image img = treeNodeIcon.getImage().getScaledInstance(25, 25,
				Image.SCALE_SMOOTH);
		treeNodeIcon = new ImageIcon(img);
		UIManager.put("Tree.leafIcon",  treeNodeIcon);
		UIManager.put("Tree.paintLines",  false);
		
		menuTree = new JTree(treeTop);
		menuTree.addTreeSelectionListener(this);
		menuTree.setRootVisible(false);
		menuTree.setShowsRootHandles(false);
		menuTree.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		menuTree.setCellRenderer(new ReportsMenuTreeRenderer()); //defined this custom render for 8.0
//		//expand all nodes of the menu tree
//		
		int noOfNodes = menuTree.getRowCount();
		
		int index = 0;
		while (index < noOfNodes) {
			menuTree.expandRow(index);
			index++;
			noOfNodes = menuTree.getRowCount();

		}
		GridBagLayout gbl_categoryPanel = new GridBagLayout();
		gbl_categoryPanel.columnWidths = new int[] { 199, 0 };
		gbl_categoryPanel.rowHeights = new int[] { 60, 681, 0 };
		gbl_categoryPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_categoryPanel.rowWeights = new double[] { 0.0, 0.0,
				Double.MIN_VALUE };
		categoryPanel.setLayout(gbl_categoryPanel);

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 60));
		panel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		categoryPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0 };
		gbl_panel.rowHeights = new int[] { 20 };
		gbl_panel.columnWeights = new double[] { 1.0 };
		gbl_panel.rowWeights = new double[] { 1.0 };
		panel.setLayout(gbl_panel);

		comboBox = new JComboBox<String>();
		// int width = comboBox.getWidth();
		// int height = comboBox.getHeight();
		comboBox.setPreferredSize(new Dimension(180, 15));
		comboBox.setUI(new ComboArrowUI());
		comboBox.setForeground(Color.WHITE);
		comboBox.setBackground(Constants.COLOR_COMBO_BACKGROUND);
		comboBox.setOpaque(true);
		comboBox.setFocusable(false);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.insets = new Insets(15, 5, 5, 5);
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;

		comboBox.setModel(new DefaultComboBoxModel<String>(durations));
		comboBox.setSelectedItem(durations[3]);
		comboBox.setActionCommand(Constants.CMD_DURATION_SELECT);
		comboBox.addActionListener((ActionListener) this);

		panel.add(comboBox, gbc_comboBox);
		GridBagConstraints gbc_menuTree = new GridBagConstraints();
		gbc_menuTree.fill = GridBagConstraints.BOTH;
		gbc_menuTree.insets = new Insets(2, 0, 0, 0);
		gbc_menuTree.gridx = 0;
		gbc_menuTree.gridy = 1;
		this.categoryPanel.add(menuTree, gbc_menuTree);

	}

	public void refreshReport(String GatewayName, String projectName) {
		this.gateway = GatewayName;
		if (gateway != null
				&& (gateway.compareToIgnoreCase("All Gateways") != 0)) {
			reportForAllGateways = false;
		} else {
			reportForAllGateways = true;
		}

		this.project = projectName;
		if (project != null
				&& (project.compareToIgnoreCase("All Projects") != 0)) {
			reportForAllProjects = false;
		} else {
			reportForAllProjects = true;
		}

		this.noOfPeople = rpc.getNumberOfActiveUsersOnController(
				constantDuration, GatewayName, projectName,
				reportForAllGateways, reportForAllProjects);
		addReport();
	}

	/**
	 * capture the menu selection event and show corresponding report.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) menuTree
				.getLastSelectedPathComponent();

		if (selectedNode.isLeaf()) {
			selectedReportMenu = selectedNode.getUserObject().toString();

		}
		this.reportGraph.removeAll();
		if (selectedReportMenu.compareToIgnoreCase("Alarm Summary") != 0) {
			this._reportChartPanel = new ChartPanel(null);
			this.reportGraph.add(_reportChartPanel, BorderLayout.CENTER);
		} else {

			this.reportGraph.add(upperScroll, BorderLayout.CENTER);
		}
		addReport();
	}

	private void addReport() {
		Dataset reportData;
		int noOfRecords = 0;
		// this.reportDetails.setViewportView(null);
		// this.reportGraph.removeAll();

		// Overview reports
		if (selectedReportMenu.compareToIgnoreCase("By Date") == 0) {
			this.lblreportTitle.setText("By Date");
			System.out.println("constantDuration: " + constantDuration	+ " Gateway: " + this.gateway + " Project: " + this.project
					+ " AllGateways: " + this.reportForAllGateways	+ " AllProjects: " + this.reportForAllProjects);

			Dataset _overview = rpc.reportsGetOverviewByDateController(constantDuration, this.gateway, this.project, this.reportForAllGateways, this.reportForAllProjects);

			DefaultTableModel overviewData = new DefaultTableModel(0, 3);
			overviewData.setColumnIdentifiers(new Object[] { "By Date",	"People", "Visits", "Actions" });
			// create a line chart
			DefaultCategoryDataset _lineData = new DefaultCategoryDataset();
			JFreeChart _lineChart;

			// create a line chart to show total actions and total sessions for
			// various durations
			// retrieve the series data from gateway using RPC
			if (_overview != null) {
				SimpleDateFormat df = new SimpleDateFormat(
						"EE MMM dd HH:mm:ss z yyyy");
				SimpleDateFormat sdf = new SimpleDateFormat("EE, MMM dd, yyyy");

				int noOfRows = _overview.getRowCount();
				int i = 0;
				int val1, val2, val3;
				int t_val1 = 0, t_val2 = 0, t_val3 = 0;
				String usersValue = "";
				String series1 = "People";
				String series2 = "Visits";
				String series3 = "Actions";
				Date xDate = null;
				String dateValTable = "";

				int startVal = 0;

				// handling special logic for last 365 display
				if (constantDuration == Constants.LAST_365_DAYS) {
					Integer curMonth = Calendar.getInstance().get(Calendar.MONTH);

					
					if (curMonth == 11) {
						curMonth = 1;
					} else {
						curMonth = curMonth + 2;
					}
					System.out.println("curMonth : " + curMonth+ "overview"+ _overview);
					startVal = Constants.binarySearchOnDataset(4, curMonth, _overview);

					if (startVal < 0) {
						startVal = 0;
					}

					for (i = startVal - 1; i >= 0; i--) {

						String dateVal = _overview.getValueAt(i, 0).toString();

						dateValTable = dateVal;

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (_overview.getValueAt(i, 1) != null) {
							val1 = (int) Float.parseFloat(_overview.getValueAt(i, 1).toString());
							t_val1 = t_val1 + val1;
						}

						// _lineData.addValue(val1, series1, dateVal);
						if (_overview.getValueAt(i, 2) != null) {
							val2 = (int) Float.parseFloat(_overview.getValueAt(i, 2).toString());
							t_val2 = t_val2 + val2;
						}
						// _lineData.addValue(val2, series2, dateVal);

						if (_overview.getValueAt(i, 3) != null) {
							val3 = (int) Float.parseFloat(_overview.getValueAt(i, 3).toString());
							t_val3 = t_val3 + val3;
						}
						// _lineData.addValue(val3, series3, dateVal);

						overviewData.addRow(new Object[] { dateValTable, val1,val2, val3 });

					}

					for (i = noOfRows - 1; i >= startVal; i--) {

						String dateVal = _overview.getValueAt(i, 0).toString();

						dateValTable = dateVal;

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (_overview.getValueAt(i, 1) != null) {
							val1 = (int) Float.parseFloat(_overview.getValueAt(i, 1).toString());
							t_val1 = t_val1 + val1;
						}

						// _lineData.addValue(val1, series1, dateVal);
						if (_overview.getValueAt(i, 2) != null) {
							val2 = (int) Float.parseFloat(_overview.getValueAt(i, 2).toString());
							t_val2 = t_val2 + val2;
						}
						// _lineData.addValue(val2, series2, dateVal);

						if (_overview.getValueAt(i, 3) != null) {
							val3 = (int) Float.parseFloat(_overview.getValueAt(i, 3).toString());
							t_val3 = t_val3 + val3;
						}
						// _lineData.addValue(val3, series3, dateVal);

						overviewData.addRow(new Object[] { dateValTable, val1,val2, val3 });
					}

				}

				else {

					for (i = startVal; i < noOfRows; i++) {

						String dateVal = _overview.getValueAt(i, 0).toString();

						if (constantDuration != Constants.TODAY
								&& constantDuration != Constants.YESTERDAY
								&& constantDuration != Constants.LAST_365_DAYS
								&& constantDuration != Constants.THIS_YEAR
								&& constantDuration != Constants.LAST_YEAR) {
							try {
								xDate = df.parse(dateVal);
								dateValTable = sdf.format(xDate);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							dateValTable = dateVal;
						}
						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (_overview.getValueAt(i, 1) != null) {
							val1 = (int) Float.parseFloat(_overview.getValueAt(i, 1).toString());
							t_val1 = t_val1 + val1;
						}

						// _lineData.addValue(val1, series1, dateVal);
						if (_overview.getValueAt(i, 2) != null) {
							val2 = (int) Float.parseFloat(_overview.getValueAt(i, 2).toString());
							t_val2 = t_val2 + val2;
						}
						// _lineData.addValue(val2, series2, dateVal);

						if (_overview.getValueAt(i, 3) != null) {
							val3 = (int) Float.parseFloat(_overview.getValueAt(i, 3).toString());
							t_val3 = t_val3 + val3;
						}
						// _lineData.addValue(val3, series3, dateVal);

						overviewData.addRow(new Object[] { dateValTable, val1, val2, val3 });
					}
				}

				// if(noOfRows > 0)
				// {
				if (constantDuration == Constants.TODAY) {
				//	t_val3 = t_val3 + 1;
				}
				/* changes for soting */
				overviewData.addRow(new Object[] { noOfRows + " Result(s)",	this.noOfPeople + " People", t_val2 + " Visits", t_val3 + " Actions" });
				// overviewData.addRow(new Object[]{ noOfRows + " Result(s)",
				// this.noOfPeople , t_val2 , t_val3 });
				// summaryRow.addRow(new Object[]{ noOfRows + " Result(s)",
				// this.noOfPeople + " People", t_val2 + " Visits", t_val3 +
				// " Actions"});
				/* end change for sorting */
				// }

				// Data for Graph
				if (constantDuration == Constants.LAST_365_DAYS) {
					for (i = startVal; i < noOfRows; i++) {
						String dateVal = "";

						dateVal = _overview.getValueAt(i, 0).toString();

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (_overview.getValueAt(i, 1) != null) {
							val1 = (int) Float.parseFloat(_overview.getValueAt(i, 1).toString());
						}

						_lineData.addValue(val1, series1, dateVal);
						if (_overview.getValueAt(i, 2) != null) {
							val2 = (int) Float.parseFloat(_overview.getValueAt(i, 2).toString());
						}
						_lineData.addValue(val2, series2, dateVal);

						if (_overview.getValueAt(i, 3) != null) {
							val3 = (int) Float.parseFloat(_overview.getValueAt(i, 3).toString());
						}
						_lineData.addValue(val3, series3, dateVal);

					}

					for (i = 0; i < startVal; i++) {
						String dateVal = "";

						dateVal = _overview.getValueAt(i, 0).toString();

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (_overview.getValueAt(i, 1) != null) {
							val1 = (int) Float.parseFloat(_overview.getValueAt(i, 1).toString());
						}

						_lineData.addValue(val1, series1, dateVal);
						if (_overview.getValueAt(i, 2) != null) {
							val2 = (int) Float.parseFloat(_overview.getValueAt(i, 2).toString());
						}
						_lineData.addValue(val2, series2, dateVal);

						if (_overview.getValueAt(i, 3) != null) {
							val3 = (int) Float.parseFloat(_overview.getValueAt(i, 3).toString());
						}
						_lineData.addValue(val3, series3, dateVal);

					}
				} else {

					for (i = noOfRows - 1; i >= 0; i--) {
						String dateVal = "";
						if (constantDuration != Constants.TODAY
								&& constantDuration != Constants.YESTERDAY
								&& constantDuration != Constants.LAST_365_DAYS
								&& constantDuration != Constants.THIS_YEAR
								&& constantDuration != Constants.LAST_YEAR) {
							dateVal = _overview.getValueAt(i, 0).toString()
									.substring(0, 10);
						} else {
							dateVal = _overview.getValueAt(i, 0).toString();
						}
						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (_overview.getValueAt(i, 1) != null) {
							val1 = (int) Float.parseFloat(_overview.getValueAt(i, 1).toString());
						}

						_lineData.addValue(val1, series1, dateVal);
						if (_overview.getValueAt(i, 2) != null) {
							val2 = (int) Float.parseFloat(_overview.getValueAt(i, 2).toString());
						}
						_lineData.addValue(val2, series2, dateVal);

						if (_overview.getValueAt(i, 3) != null) {
							val3 = (int) Float.parseFloat(_overview.getValueAt(i, 3).toString());
						}
						_lineData.addValue(val3, series3, dateVal);

					}

				}

			}
			addTableView(overviewData, 3, "People", "Visits", "Actions");
			createLineChart(_lineData);

		} else if (selectedReportMenu.compareToIgnoreCase("Cities") == 0) {

			this.lblreportTitle.setText("Cities");
			System.out.println("constantDuration: " + constantDuration
					+ " Gateway: " + this.gateway + " Project: " + this.project
					+ " AllGateways: " + this.reportForAllGateways
					+ " AllProjects: " + this.reportForAllProjects);
			reportData = rpc.getCitiesReportDataController(this.gateway,
					this.project, this.reportForAllGateways,
					this.reportForAllProjects, constantDuration);

			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			int size = 0;

			size = reportData.getRowCount();
			String tempVal;

			DefaultTableModel screenDepthTableModel = new DefaultTableModel();
			screenDepthTableModel.setColumnIdentifiers(new Object[] { "Cities"," People", "Visits", "Actions" });
			int totalNoOfPeople = 0;
			int totalNoOfVisits = 0;
			int totalNoOfActions = 0;
			int peopleVal = 0;
			int actionVal = 0;
			int visitsVal = 0;
			String screen = "";
			for (int i = 0; i < size; i++) {
				screen = reportData.getValueAt(i, "Cities").toString();

				// code to remove repeated comma when state is null or city is
				// null

				screen = screen.trim();
				while (screen.startsWith(",")) {
					screen = screen.substring(1);
				}
				if (screen.endsWith(",")) {
					screen = screen.replaceAll(",", "");
				}
				screen = screen.replace(", null, ", ", ");
				screen = screen.replace("false, ", " ");
				screen = screen.replace("null", "None");
				if (reportData.getValueAt(i, "People") != null) {
					peopleVal = (int) Float.parseFloat(reportData.getValueAt(i,"People").toString());
				}
				if (reportData.getValueAt(i, "Actions") != null) {
					actionVal = (int) Float.parseFloat(reportData.getValueAt(i,"Actions").toString());
				}
				if (reportData.getValueAt(i, "noOfScreens") != null) {
					actionVal = actionVal+ (int) Float.parseFloat(reportData.getValueAt(i,"noOfScreens").toString());
				}
				if (reportData.getValueAt(i, "Visits") != null) {
					visitsVal = (int) Float.parseFloat(reportData.getValueAt(i,"Visits").toString());
				}

				totalNoOfPeople = totalNoOfPeople + peopleVal;
				totalNoOfVisits = totalNoOfVisits + visitsVal;
				totalNoOfActions = totalNoOfActions + actionVal;
		
				if (i < 10) // top 10 cities only in the graph.
				{
					dataset.addValue(peopleVal, series1, screen);
					dataset.addValue(visitsVal, series2, screen);
					dataset.addValue(actionVal, series3, screen);
				}
				
				screenDepthTableModel.addRow(new Object[] { screen, peopleVal,visitsVal, actionVal });
			}

			// if(size > 0)
			// {
			screenDepthTableModel.addRow(new Object[] { size + " Result(s)",this.noOfPeople + " People", totalNoOfVisits + " Visits",
					totalNoOfActions + " Actions" });

			// }
		
			// to create dummy records in case dataset is less.
			if (size < 10) {
				for (int i = size - 1; i < 10; i++) {
					dataset.addValue(0, series1, "    ");
					dataset.addValue(0, series2, "    ");
					dataset.addValue(0, series3, "    ");
				}
			}
			addTableView(screenDepthTableModel, 3, "People", "Visits","Actions");
			// Draw Graph

			JFreeChart chart = ChartFactory.createBarChart("", // chart title
					"", // domain axis label
					"", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips?
					false // URLs?
					);

			applyBarChartFormat(chart);

			this._reportChartPanel.setChart(chart);

		} else if (selectedReportMenu.compareToIgnoreCase("Groups") == 0) {

			this.lblreportTitle.setText("Groups");

			// System.out.println("constantDuration: " + constantDuration
			// + " Gateway: " + this.gateway + " Project: " + this.project
			// + " AllGateways: " + this.reportForAllGateways
			// + " AllProjects: " + this.reportForAllProjects);
			// List<GroupReportRecord> grpReportData = rpc
			// .getGroupsReportDataController(gateway, project,
			// reportForAllGateways, reportForAllProjects,
			// constantDuration);

			Map<String, List<GroupReportRecord>> allGroupRecords = new HashMap<String, List<GroupReportRecord>>();

			String[] allGateways = rpc.getGateways();

			int gatwaysLength = allGateways.length;
			System.out.println("no of gateways : " + gatwaysLength);
			if(reportForAllGateways){
				for (int i = 1; i < gatwaysLength; i++) {
//					System.out.println("Caling groups report service for : " +allGateways[i] );
					List<GroupReportRecord> groupRecords = new ArrayList<GroupReportRecord>();
					try {
						groupRecords = rpc.getGroupsReportDataController(allGateways[i], project, reportForAllGateways,	reportForAllProjects, constantDuration);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}// (project, reportForAllProjects, Constants.YESTERDAY);
					allGroupRecords.put(allGateways[i], groupRecords);

				}
				}else{
					List<GroupReportRecord> groupRecords = new ArrayList<GroupReportRecord>();
					try {
						groupRecords = rpc.getGroupsReportDataController(this.gateway, project, reportForAllGateways,	reportForAllProjects, constantDuration);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					allGroupRecords.put(this.gateway, groupRecords);

				}
			System.out.println("after for loop : " );
			System.out.println("all records: "+allGroupRecords);

			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			String tempVal;

			DefaultTableModel groupsTableModel = new DefaultTableModel();
			groupsTableModel.setColumnIdentifiers(new Object[] { "Gateway", "Group", " People", "Visits", "Actions" });
			int totalNoOfPeople = 0;
			int totalNoOfVisits = 0;
			int totalNoOfActions = 0;
			int peopleVal = 0;
			int actionVal = 0;
			int visitsVal = 0;
			String groupName = "";
			String gatewayName = "";
			int totalResults = 0;
			 HashMap<String,Integer> people = new HashMap<String, Integer>();
			   HashMap<String,Integer> visits = new HashMap<String, Integer>();
			   HashMap<String,Integer> actions = new HashMap<String, Integer>();
			   
			for (Map.Entry<String, List<GroupReportRecord>> record :allGroupRecords.entrySet() ) {
				List<GroupReportRecord> itrGroupDAta = record.getValue();
				gatewayName = record.getKey();
				if(itrGroupDAta != null)
				{
					int itrsize = itrGroupDAta.size();
					System.out.println("itrsize : " + itrsize );
					for (int j = 0; j < itrsize; j++) {
						GroupReportRecord grpReportData = itrGroupDAta.get(j);
						System.out.println("j : " + j);
						
						groupName = grpReportData.getGroupName();
						peopleVal = grpReportData.getNoOfPeople();
						actionVal = grpReportData.getNoOfActions();
						visitsVal = grpReportData.getNoOfVisits();
						groupsTableModel.addRow(new Object[] { gatewayName,groupName,
								peopleVal, visitsVal, actionVal });
						totalNoOfPeople = totalNoOfPeople + peopleVal;
						totalNoOfVisits = totalNoOfVisits + visitsVal;
						totalNoOfActions = totalNoOfActions + actionVal;
	
						 if (!people.containsKey(groupName)) {
						       people.put(groupName, peopleVal);
						      }else{
						       peopleVal = peopleVal + people.get(groupName);
						       people.put(groupName, peopleVal);
						      }
						      if (!visits.containsKey(groupName)) {
						       visits.put(groupName, visitsVal);
						      }else{
						       visitsVal = visitsVal + visits.get(groupName);
						       visits.put(groupName, visitsVal);
						      }
						      if (!actions.containsKey(groupName)) {
						       actions.put(groupName, actionVal);
						      }else{
						       actionVal = actionVal + actions.get(groupName);
						       actions.put(groupName, actionVal);
						      }
						totalResults++;
						
					}
				}
				else
				{
					System.out.println("itrsize null");
				}
			}
			Set<Entry<String, Integer>> set1=people.entrySet();  
		      Iterator<Entry<String, Integer>> itr1=set1.iterator();  
		      while (itr1.hasNext()) {
		       Entry<String,Integer> entry=itr1.next();  
		       peopleVal = entry.getValue();
		       groupName = entry.getKey();
		       dataset.addValue(peopleVal, series1, groupName);
		   }
		      Set<Entry<String, Integer>> set2=visits.entrySet();  
		      Iterator<Entry<String, Integer>> itr2=set2.iterator();  
		      while (itr2.hasNext()) {
		       Entry<String,Integer> entry=itr2.next();  
		       visitsVal = entry.getValue();
		       groupName = entry.getKey();
		       dataset.addValue(visitsVal, series2, groupName);
		   }
		      Set<Entry<String, Integer>> set3=actions.entrySet();  
		      Iterator<Entry<String, Integer>> itr3=set3.iterator();  
		      while (itr3.hasNext()) {
		       Entry<String,Integer> entry=itr3.next();  
		       actionVal = entry.getValue();
		       groupName = entry.getKey();
		       dataset.addValue(actionVal, series3, groupName);
		   }
			if (allGroupRecords.size() > 0) {
				// query the overview and get visits , actions
				OverviewInformation _overview = rpc.getOverviewOnController(constantDuration, gateway, project,	reportForAllGateways, reportForAllProjects);

				groupsTableModel.addRow(new Object[] {totalResults + " Result(s)", "",
						this.noOfPeople + " People",_overview.getNoOfSessions() + " Visits",_overview.getNoOfActions() + " Actions" });

			}
			// if(size < 10)
			// {
			// for(int i=size-1; i<10; i++)
			// {
			// dataset.addValue(0 , series1, " " );
			// dataset.addValue(0 , series2, " ");
			// dataset.addValue(0 , series3, " ");
			// }
			// }
//			addTableViewController(groupsTableModel, 3, "People", "Visits", "Actions");
			ImageIcon blueIcon;
			ImageIcon yellowIcon;
			ImageIcon greenIcon;
			IA_txtIconTableHeaderRenderer headerRenderer; 
			
			if(groupsTableModel != null && groupsTableModel.getRowCount() > 0)
			{
				//initialize the image icons to be displayed as column headers on the reports.
				blueIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Blue.png"));
				blueIcon = new ImageIcon(blueIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
				yellowIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Yellow.png"));
				yellowIcon = new ImageIcon(yellowIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
				greenIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Green.png"));
				greenIcon = new ImageIcon(greenIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
					
				//define the reports table header cell renderer that displays text + icon
				headerRenderer = new IA_txtIconTableHeaderRenderer();
				ReportsTable _table; //for reports other than Alarm reports.
				_table = new ReportsTable(groupsTableModel);
						
				_table.setPreferredScrollableViewportSize(this.reportDetails.getPreferredSize());
				//_table.setModel(tableData);
				_table.setVisible(true);
			     
			    _table.getTableHeader().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));
				 
					RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
					rightRenderer.paddingSize = 10;
					
					LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
					leftRenderer.paddingSize = 10;
			
					_table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
					_table.getColumnModel().getColumn(1).setCellRenderer( leftRenderer );
					_table.getColumnModel().getColumn(2).setCellRenderer( rightRenderer );
					_table.getColumnModel().getColumn(3).setCellRenderer( rightRenderer );
					_table.getColumnModel().getColumn(4).setCellRenderer( rightRenderer );
				
					//adjust the total widths of the columns
					_table.getColumnModel().getColumn(0).setPreferredWidth(350);
					_table.getColumnModel().getColumn(1).setPreferredWidth(350);
					_table.getColumnModel().getColumn(2).setPreferredWidth(100);
					_table.getColumnModel().getColumn(3).setPreferredWidth(100);
					_table.getColumnModel().getColumn(4).setPreferredWidth(100);
		
					//create sorting ability
					_table.setAutoCreateRowSorter(false);
					
					//apply renderer for teh header
					_table.getColumnModel().getColumn(0).setHeaderRenderer(new LeftHeaderCellRenderer());
					_table.getColumnModel().getColumn(1).setHeaderRenderer(new LeftHeaderCellRenderer());
					_table.getColumnModel().getColumn(2).setHeaderRenderer(headerRenderer);
					_table.getColumnModel().getColumn(3).setHeaderRenderer( headerRenderer );
					_table.getColumnModel().getColumn(4).setHeaderRenderer( headerRenderer );
				
					//set teh header value text + icon
					_table.getColumnModel().getColumn(2).setHeaderValue(new IA_TextIcon("People", blueIcon));
					_table.getColumnModel().getColumn(3).setHeaderValue(new IA_TextIcon("Visits",yellowIcon));
					_table.getColumnModel().getColumn(4).setHeaderValue(new IA_TextIcon("Actions",greenIcon));
					_table.setEnabled(false);
					
					/*change for sorting */
					TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>()
							{

							};
							sorter.setModel(_table.getModel());
							sorter.setSortable(0, false);
							sorter.setSortable(1, false);
							sorter.setComparator(2, new Comparator<Integer>() {

								@Override
								public int compare(Integer o1, Integer o2) {
									
									return o1.compareTo(o2);
								}
							});
							sorter.setComparator(3, new Comparator<Integer>() {

								@Override
								public int compare(Integer o1, Integer o2) {
									
									return o1.compareTo(o2);
								}
							});
							sorter.setComparator(4, new Comparator<Integer>() {

								@Override
								public int compare(Integer o1, Integer o2) {
									
									return o1.compareTo(o2);
								}
							});
							_table.setRowSorter(sorter);
							
						
							/* end add change for sorting */
					//display table on the UI
					this.reportDetails.setViewportView(_table);

			}
			// Draw Graph

			JFreeChart chart = ChartFactory.createBarChart("", // chart title
					"", // domain axis label
					"", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips?
					false // URLs?
					);

			applyBarChartFormat(chart);

			this._reportChartPanel.setChart(chart);

		} else if (selectedReportMenu.compareToIgnoreCase("Top Screens") == 0) {
			this.lblreportTitle.setText("Top Screens");

			System.out.println("constantDuration: " + constantDuration+ " Gateway: " + this.gateway + " Project: " + this.project
					+ " AllGateways: " + this.reportForAllGateways+ " AllProjects: " + this.reportForAllProjects);
			// get the Top screens data using RPC
			reportData = rpc.getTopScreensController(constantDuration,this.gateway, this.project, this.reportForAllGateways,this.reportForAllProjects);
			// set the tabular view
			DefaultTableModel screensData = new DefaultTableModel(0, 5);

			noOfRecords = 0;
			// create a bar chart
			if (reportData != null) {
				noOfRecords = reportData.getRowCount();
				/*
				 * if(noOfRecords > 10) { noOfRecords = 10; }
				 */
				// create a bar chart and add
				String series1 = "People";
				String series2 = "Actions";

				DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				String categoryName = "";
				String _gatewayName = "";
				String _projectName = "";
				
				int t_val1 = 0, t_val2 = 0;
				// build the dataset for bar chart
				for (int i = 0; i < noOfRecords; i++) {
					_gatewayName = reportData.getValueAt(i, 0).toString();
					_projectName = reportData.getValueAt(i, 1).toString();
					categoryName = reportData.getValueAt(i, 2).toString();
					int val1 = (int) Float.parseFloat(reportData.getValueAt(i,3).toString());
					int val2 = (int) Float.parseFloat(reportData.getValueAt(i,4).toString());
					if (i < 10) {
						dataset.addValue(val1, series1, categoryName);
						dataset.addValue(val2, series2, categoryName);
					}
					t_val1 = t_val1 + val1;
					t_val2 = t_val2 + val2;
					screensData.addRow(new Object[] {_gatewayName,_projectName, categoryName, val1, val2 });
				}

				// if(noOfRecords > 0)
				// {
				screensData.addRow(new Object[] {noOfRecords + " Results(s)","", "",this.noOfPeople + " People", t_val2 + " Actions" });
				// }
				screensData.setColumnIdentifiers(new Object[] { "Gateway Name","Project Name","Page Views","People", "Actions" });

//				addTableViewControllerTopScreens(screensData, 2, "People", "Actions");
				ImageIcon blueIcon;
				ImageIcon yellowIcon;
				ImageIcon greenIcon;
				IA_txtIconTableHeaderRenderer headerRenderer; 
				
				if(screensData != null && screensData.getRowCount() > 0)
				{
					//initialize the image icons to be displayed as column headers on the reports.
					blueIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Blue.png"));
					blueIcon = new ImageIcon(blueIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
					yellowIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Yellow.png"));
					yellowIcon = new ImageIcon(yellowIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
					greenIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Green.png"));
					greenIcon = new ImageIcon(greenIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
						
					//define the reports table header cell renderer that displays text + icon
					headerRenderer = new IA_txtIconTableHeaderRenderer();
					ReportsTable _table; //for reports other than Alarm reports.
					_table = new ReportsTable(screensData);
							
					_table.setPreferredScrollableViewportSize(this.reportDetails.getPreferredSize());
					//_table.setModel(tableData);
					_table.setVisible(true);
				     
				    _table.getTableHeader().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));
					 
						RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
						rightRenderer.paddingSize = 10;
						
						LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
						leftRenderer.paddingSize = 10;
				
						_table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
						_table.getColumnModel().getColumn(1).setCellRenderer( leftRenderer );
						_table.getColumnModel().getColumn(2).setCellRenderer( leftRenderer );
						_table.getColumnModel().getColumn(3).setCellRenderer( rightRenderer );
						_table.getColumnModel().getColumn(4).setCellRenderer( rightRenderer );
					
						//adjust the total widths of the columns
						_table.getColumnModel().getColumn(0).setPreferredWidth(300);
						_table.getColumnModel().getColumn(1).setPreferredWidth(300);
						_table.getColumnModel().getColumn(2).setPreferredWidth(200);
						_table.getColumnModel().getColumn(3).setPreferredWidth(100);
						_table.getColumnModel().getColumn(4).setPreferredWidth(100);
			
						//create sorting ability
						_table.setAutoCreateRowSorter(false);
						
						//apply renderer for teh header
						_table.getColumnModel().getColumn(0).setHeaderRenderer(new LeftHeaderCellRenderer());
						_table.getColumnModel().getColumn(1).setHeaderRenderer(new LeftHeaderCellRenderer());
						_table.getColumnModel().getColumn(2).setHeaderRenderer(new LeftHeaderCellRenderer());
						_table.getColumnModel().getColumn(3).setHeaderRenderer( headerRenderer );
						_table.getColumnModel().getColumn(4).setHeaderRenderer( headerRenderer );
					
						//set teh header value text + icon
						_table.getColumnModel().getColumn(3).setHeaderValue(new IA_TextIcon("People", blueIcon));
						_table.getColumnModel().getColumn(4).setHeaderValue(new IA_TextIcon("Actions",yellowIcon));
				
						_table.setEnabled(false);
						
						/*change for sorting */
						TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>()
								{

								};
								sorter.setModel(_table.getModel());
								sorter.setSortable(0, false);
								sorter.setSortable(1, false);
								sorter.setComparator(2, new Comparator<Integer>() {

									@Override
									public int compare(Integer o1, Integer o2) {
										
										return o1.compareTo(o2);
									}
								});
								sorter.setComparator(3, new Comparator<Integer>() {

									@Override
									public int compare(Integer o1, Integer o2) {
										
										return o1.compareTo(o2);
									}
								});
								sorter.setComparator(4, new Comparator<Integer>() {

									@Override
									public int compare(Integer o1, Integer o2) {
										
										return o1.compareTo(o2);
									}
								});
								_table.setRowSorter(sorter);
								
							
								/* end add change for sorting */
						//display table on the UI
						this.reportDetails.setViewportView(_table);

				}
				if (noOfRecords < 10) {
					for (int i = noOfRecords - 1; i < 10; i++) {
						dataset.addValue(0, series1, " ");
						dataset.addValue(0, series2, " ");

					}
				}
				JFreeChart chart = ChartFactory.createBarChart("", // chart
																	// title
						"", // domain axis label
						"", // range axis label
						dataset, // data
						PlotOrientation.VERTICAL, // orientation
						false, // include legend
						true, // tooltips?
						false // URLs?
						);

				//
				applyBarChartFormat(chart);
				this._reportChartPanel.setChart(chart);

			}

		}else if (selectedReportMenu.compareToIgnoreCase("Top Projects") == 0) {
				this.lblreportTitle.setText("Top Projects");

				System.out.println("constantDuration: " + constantDuration+ " Gateway: " + this.gateway + " Project: " + this.project
						+ " AllGateways: " + this.reportForAllGateways+ " AllProjects: " + this.reportForAllProjects);
				// get the Top screens data using RPC
				reportData = rpc.getTopProjectsController(constantDuration,this.gateway, this.project, this.reportForAllGateways, this.reportForAllProjects);
				// set the tabular view
				DefaultTableModel screensData = new DefaultTableModel(0, 5);
				screensData.setColumnIdentifiers(new Object[] { "Gateway Name","Project Name", "People","Visits", "Actions" });
				noOfRecords = 0;
				// create a bar chart
				if (reportData != null) {
					noOfRecords = reportData.getRowCount();
					/*
					 * if(noOfRecords > 10) { noOfRecords = 10; }
					 */
					// create a bar chart and add
					String series1 = "People";
					String series2 = "Visits";
					String series3 = "Actions";

					DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				
					String _gatewayName = "";
					String _projectName = "";
					
					int totalNoOfPeople = 0, totalNoOfVisits = 0, totalNoOfActions = 0;
					// build the dataset for bar chart
					for (int i = 0; i < noOfRecords; i++) {
						_gatewayName = reportData.getValueAt(i, 0).toString();
						_projectName = reportData.getValueAt(i, 1).toString();
						
						int peopleVal = (int) Float.parseFloat(reportData.getValueAt(i,2).toString());
						int visitsVal = (int) Float.parseFloat(reportData.getValueAt(i,3).toString());
						int actionVal = (int) Float.parseFloat(reportData.getValueAt(i,4).toString());
						
				
					// }
					

					if (i < 10) // top 10 cities only in the graph.
					{
						dataset.addValue(peopleVal, series1, _projectName);
						dataset.addValue(visitsVal, series2, _projectName);
						dataset.addValue(actionVal, series3, _projectName);
					}
					totalNoOfPeople = totalNoOfPeople + peopleVal;
					totalNoOfVisits = totalNoOfVisits + visitsVal;
					totalNoOfActions = totalNoOfActions + actionVal;
			
					screensData.addRow(new Object[] {_gatewayName, _projectName, peopleVal,visitsVal, actionVal });
				}

				screensData.addRow(new Object[] { noOfRecords + " Result(s)"," ",
						this.noOfPeople + " People", totalNoOfVisits + " Visits",totalNoOfActions + " Actions" });

//				addTableViewController(screensData, 3, "People", "Visits", "Actions");
				ImageIcon blueIcon;
				ImageIcon yellowIcon;
				ImageIcon greenIcon;
				IA_txtIconTableHeaderRenderer headerRenderer; 
				
				if(screensData != null && screensData.getRowCount() > 0)
				{
					//initialize the image icons to be displayed as column headers on the reports.
					blueIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Blue.png"));
					blueIcon = new ImageIcon(blueIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
					yellowIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Yellow.png"));
					yellowIcon = new ImageIcon(yellowIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
					greenIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Green.png"));
					greenIcon = new ImageIcon(greenIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
						
					//define the reports table header cell renderer that displays text + icon
					headerRenderer = new IA_txtIconTableHeaderRenderer();
					ReportsTable _table; //for reports other than Alarm reports.
					_table = new ReportsTable(screensData);
							
					_table.setPreferredScrollableViewportSize(this.reportDetails.getPreferredSize());
					//_table.setModel(tableData);
					_table.setVisible(true);
				     
				    _table.getTableHeader().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));
					 
						RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
						rightRenderer.paddingSize = 10;
						
						LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
						leftRenderer.paddingSize = 10;
				
						_table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
						_table.getColumnModel().getColumn(1).setCellRenderer( leftRenderer );
						_table.getColumnModel().getColumn(2).setCellRenderer( rightRenderer );
						_table.getColumnModel().getColumn(3).setCellRenderer( rightRenderer );
						_table.getColumnModel().getColumn(4).setCellRenderer( rightRenderer );
					
						//adjust the total widths of the columns
						_table.getColumnModel().getColumn(0).setPreferredWidth(350);
						_table.getColumnModel().getColumn(1).setPreferredWidth(350);
						_table.getColumnModel().getColumn(2).setPreferredWidth(100);
						_table.getColumnModel().getColumn(3).setPreferredWidth(100);
						_table.getColumnModel().getColumn(4).setPreferredWidth(100);
			
						//create sorting ability
						_table.setAutoCreateRowSorter(false);
						
						//apply renderer for teh header
						_table.getColumnModel().getColumn(0).setHeaderRenderer(new LeftHeaderCellRenderer());
						_table.getColumnModel().getColumn(1).setHeaderRenderer(new LeftHeaderCellRenderer());
						_table.getColumnModel().getColumn(2).setHeaderRenderer(headerRenderer);
						_table.getColumnModel().getColumn(3).setHeaderRenderer( headerRenderer );
						_table.getColumnModel().getColumn(4).setHeaderRenderer( headerRenderer );
					
						//set teh header value text + icon
						_table.getColumnModel().getColumn(2).setHeaderValue(new IA_TextIcon("People", blueIcon));
						_table.getColumnModel().getColumn(3).setHeaderValue(new IA_TextIcon("Visits",yellowIcon));
						_table.getColumnModel().getColumn(4).setHeaderValue(new IA_TextIcon("Actions",greenIcon));
						_table.setEnabled(false);
						
						/*change for sorting */
						TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>()
								{

								};
								sorter.setModel(_table.getModel());
								sorter.setSortable(0, false);
								sorter.setSortable(1, false);
								sorter.setComparator(2, new Comparator<Integer>() {

									@Override
									public int compare(Integer o1, Integer o2) {
										
										return o1.compareTo(o2);
									}
								});
								sorter.setComparator(3, new Comparator<Integer>() {

									@Override
									public int compare(Integer o1, Integer o2) {
										
										return o1.compareTo(o2);
									}
								});
								sorter.setComparator(4, new Comparator<Integer>() {

									@Override
									public int compare(Integer o1, Integer o2) {
										
										return o1.compareTo(o2);
									}
								});
								_table.setRowSorter(sorter);
								
							
								/* end add change for sorting */
						//display table on the UI
						this.reportDetails.setViewportView(_table);

				}
				// Draw Graph

				JFreeChart chart = ChartFactory.createBarChart("", // chart title
						"", // domain axis label
						"", // range axis label
						dataset, // data
						PlotOrientation.VERTICAL, // orientation
						false, // include legend
						true, // tooltips?
						false // URLs?
						);

				applyBarChartFormat(chart);

				this._reportChartPanel.setChart(chart);
				}

		} else if (selectedReportMenu.compareToIgnoreCase("Bounce Rate") == 0) {
			this.lblreportTitle.setText("Bounce Rate");
			
			System.out.println("constantDuration: " + constantDuration+ " Gateway: " + this.gateway + " Project: " + this.project
					+ " AllGateways: " + this.reportForAllGateways+ " AllProjects: " + this.reportForAllProjects);
			
			Dataset bRateData = rpc.getBounceRateReportByDateController(constantDuration, this.gateway, this.project,
					this.reportForAllGateways, this.reportForAllProjects);
			DefaultTableModel overviewData = new DefaultTableModel(0, 3);
			
			overviewData.setColumnIdentifiers(new Object[] { "By Date","People", "Visits", "Bounce Rate" });
			// create a line chart
			DefaultCategoryDataset _lineData = new DefaultCategoryDataset();

			SimpleDateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
			SimpleDateFormat sdf = new SimpleDateFormat("EE, MMM dd, yyyy");
			SimpleDateFormat chartDF = new SimpleDateFormat("EE MMM dd");
			// create a line chart to show total actions and total sessions for
			// various durations
			float bounceRate = rpc.getBounceRateController(constantDuration, this.gateway, this.project,
					this.reportForAllGateways, this.reportForAllProjects);
			// retrieve the series data from gateway using RPC
			if (bRateData != null) {
				int noOfRows = bRateData.getRowCount();
				int i = 0;
				int val1, val2, val3;
				int t_val1 = 0, t_val2 = 0, t_val3 = 0;
				String usersValue = "";
				String series1 = "People";
				String series2 = "Visits";
				String series3 = "Actions";
				Date xDate = null;

				int startVal = 0;
				// handling special logic for last 365 display
				if (constantDuration == Constants.LAST_365_DAYS) {
					Integer curMonth = Calendar.getInstance().get(
							Calendar.MONTH);

					System.out.println("curMonth : " + curMonth+ "bRateData"+ bRateData);
					if (curMonth == 11) {
						curMonth = 1;
					} else {
						curMonth = curMonth + 2;
					}

					startVal = Constants.binarySearchOnDataset(4, curMonth, bRateData);
					if (startVal < 0) {
						startVal = 0;
					}

					for (i = startVal - 1; i >= 0; i--) {
						String dateVal = bRateData.getValueAt(i, 0).toString();

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (bRateData.getValueAt(i, 2) != null) {
							val1 = (int) Float.parseFloat(bRateData.getValueAt(i, 2).toString());
							t_val1 = t_val1 + val1;
						}

						if (bRateData.getValueAt(i, 3) != null) {
							val2 = (int) Float.parseFloat(bRateData.getValueAt(i, 3).toString());
							t_val2 = t_val2 + val2;
						}

						if (bRateData.getValueAt(i, 1) != null) {
							val3 = (int) Float.parseFloat(bRateData.getValueAt(i, 1).toString());
							t_val3 = t_val3 + val3;
						}

						overviewData.addRow(new Object[] { dateVal, val1, val2,val3 });
					}
					for (i = noOfRows - 1; i >= startVal; i--) {
						String dateVal = bRateData.getValueAt(i, 0).toString();

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (bRateData.getValueAt(i, 2) != null) {
							val1 = (int) Float.parseFloat(bRateData.getValueAt(i, 2).toString());
							t_val1 = t_val1 + val1;
						}

						if (bRateData.getValueAt(i, 3) != null) {
							val2 = (int) Float.parseFloat(bRateData.getValueAt(i, 3).toString());
							t_val2 = t_val2 + val2;
						}

						if (bRateData.getValueAt(i, 1) != null) {
							val3 = (int) Float.parseFloat(bRateData.getValueAt(i, 1).toString());
							t_val3 = t_val3 + val3;
						}

						overviewData.addRow(new Object[] { dateVal, val1, val2, val3 });
					}
				}

				else {
					for (i = noOfRows - 1; i >= 0; i--) {

						String dateVal = bRateData.getValueAt(i, 0).toString();
						if (constantDuration != Constants.TODAY
								&& constantDuration != Constants.YESTERDAY
								&& constantDuration != Constants.LAST_365_DAYS
								&& constantDuration != Constants.THIS_YEAR
								&& constantDuration != Constants.LAST_YEAR) {
							try {
								xDate = df.parse(dateVal);
								dateVal = sdf.format(xDate);
							} catch (ParseException e) {

								e.printStackTrace();
							}
						}

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (bRateData.getValueAt(i, 2) != null) {
							val1 = (int) Float.parseFloat(bRateData.getValueAt(i, 2).toString());
							t_val1 = t_val1 + val1;
						}

						if (bRateData.getValueAt(i, 3) != null) {
							val2 = (int) Float.parseFloat(bRateData.getValueAt(i, 3).toString());
							t_val2 = t_val2 + val2;
						}

						if (bRateData.getValueAt(i, 1) != null) {
							val3 = (int) Float.parseFloat(bRateData.getValueAt(i, 1).toString());
							t_val3 = t_val3 + val3;
						}

						overviewData.addRow(new Object[] { dateVal, val1, val2,val3 });
					}
				}
				if (noOfRows > 0) {
					bounceRate = (bounceRate) * 100;
					t_val3 =(int)bounceRate ; // to get the average
				}

				overviewData.addRow(new Object[] { noOfRows + " Results(s)",this.noOfPeople + " People", t_val2 + " Visits", t_val3 + " % Bounce Rate" });

				// create the line data in ascending order
				if (constantDuration == Constants.LAST_365_DAYS) {
					for (i = startVal; i < noOfRows; i++) {
						String dateVal = bRateData.getValueAt(i, 0).toString();

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (bRateData.getValueAt(i, 2) != null) {
							val1 = (int) Float.parseFloat(bRateData.getValueAt(i, 2).toString());
						}

						_lineData.addValue(val1, series1, dateVal);
						if (bRateData.getValueAt(i, 3) != null) {
							val2 = (int) Float.parseFloat(bRateData.getValueAt(i, 3).toString());
						}
						_lineData.addValue(val2, series2, dateVal);

						if (bRateData.getValueAt(i, 1) != null) {
							val3 = (int) Float.parseFloat(bRateData.getValueAt(i, 1).toString());
						}
						_lineData.addValue(val3, series3, dateVal);
					}
					for (i = 0; i < startVal; i++) {
						String dateVal = bRateData.getValueAt(i, 0).toString();

						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (bRateData.getValueAt(i, 2) != null) {
							val1 = (int) Float.parseFloat(bRateData.getValueAt(i, 2).toString());
						}

						_lineData.addValue(val1, series1, dateVal);
						if (bRateData.getValueAt(i, 3) != null) {
							val2 = (int) Float.parseFloat(bRateData.getValueAt(i, 3).toString());
						}
						_lineData.addValue(val2, series2, dateVal);

						if (bRateData.getValueAt(i, 1) != null) {
							val3 = (int) Float.parseFloat(bRateData.getValueAt(i, 1).toString());
						}
						_lineData.addValue(val3, series3, dateVal);
					}
				} else {
					for (i = 0; i < noOfRows; i++) {

						String dateVal = bRateData.getValueAt(i, 0).toString();
						if (constantDuration != Constants.TODAY
								&& constantDuration != Constants.YESTERDAY
								&& constantDuration != Constants.LAST_365_DAYS
								&& constantDuration != Constants.THIS_YEAR
								&& constantDuration != Constants.LAST_YEAR) {
							try {
								xDate = df.parse(dateVal);
								dateVal = chartDF.format(xDate);
							} catch (ParseException e) {

								e.printStackTrace();
							}
						}
						val1 = 0;
						val2 = 0;
						val3 = 0;
						if (bRateData.getValueAt(i, 2) != null) {
							val1 = (int) Float.parseFloat(bRateData.getValueAt(i, 2).toString());
						}

						_lineData.addValue(val1, series1, dateVal);
						if (bRateData.getValueAt(i, 3) != null) {
							val2 = (int) Float.parseFloat(bRateData.getValueAt(i, 3).toString());
						}
						_lineData.addValue(val2, series2, dateVal);

						if (bRateData.getValueAt(i, 1) != null) {
							val3 = (int) Float.parseFloat(bRateData.getValueAt(i, 1).toString());
						}
						_lineData.addValue(val3, series3, dateVal);

					}
				}
				// _lineChart = ChartFactory.createLineChart("", "", "",
				// _lineData, PlotOrientation.VERTICAL, false, true, true );

			}

			addTableView(overviewData, 3, "People", "Visits", "Bounce Rate");
			createLineChart(_lineData);
		}

		// Systems reports
		else if (selectedReportMenu.compareToIgnoreCase("Device Types") == 0) {

			this.lblreportTitle.setText("Device Types");
			System.out.println("constantDuration: " + constantDuration+ " Gateway: " + this.gateway + " Project: " + this.project
					+ "      AllGateways: " + this.reportForAllGateways+ " AllProjects: " + this.reportForAllProjects);
			
			reportData = rpc.getDeviceTypeReportController(constantDuration,this.gateway, this.project, this.reportForAllGateways, this.reportForAllProjects);
			DefaultTableModel devicesData = new DefaultTableModel(0, 3);
			devicesData.setColumnIdentifiers(new Object[] { "Device Types","People", "Visits", "Actions" });

			if (reportData != null) {
				noOfRecords = reportData.getRowCount();

				String series1 = "People";
				String series2 = "Visits";
				String series3 = "Actions";

				DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				String category = "";
				int noOfPeople = 0;
				int noOfActions = 0;
				int noOfvisits = 0;

				int totalNoOfPeople = 0;
				int totalNoOfActions = 0, totalNoOfVisits = 0;
				for (int i = 0; i < noOfRecords; i++) {
					category = "";
					noOfPeople = 0;
					noOfActions = 0;
					noOfvisits = 0;

					if (reportData.getValueAt(i, 0) != null) {
						category = reportData.getValueAt(i, 0).toString();
					}
					if (reportData.getValueAt(i, 1) != null) {
						noOfPeople = (int) Float.parseFloat(reportData.getValueAt(i, 1).toString());
					}
					if (reportData.getValueAt(i, 2) != null) {
						noOfvisits = (int) Float.parseFloat(reportData.getValueAt(i, 2).toString());
					}
					if (reportData.getValueAt(i, 3) != null) {
						noOfActions = (int) Float.parseFloat(reportData.getValueAt(i, 3).toString());
					}
					if (reportData.getValueAt(i, 4) != null) {
						noOfActions = noOfActions + (int) Float.parseFloat(reportData.getValueAt(i, 4).toString());
					}

					dataset.addValue(noOfPeople, series1, category);
					dataset.addValue(noOfvisits, series2, category);
					dataset.addValue(noOfActions, series3, category);

					devicesData.addRow(new Object[] { category, noOfPeople,
							noOfvisits, noOfActions });
					totalNoOfPeople = totalNoOfPeople + noOfPeople;
					totalNoOfVisits = totalNoOfVisits + noOfvisits;
					totalNoOfActions = totalNoOfActions + noOfActions;
				}
				// if(noOfRecords > 0)
				// {
				devicesData.addRow(new Object[] { noOfRecords + " Result(s)",
						this.noOfPeople + " People",
						totalNoOfVisits + " Visits",
						totalNoOfActions + " Actions" });
				// }

				// to create dummy records in case dataset is less.
				if (noOfRecords < 10) {
					for (int i = noOfRecords - 1; i < 10; i++) {
						dataset.addValue(0, series1, "      ");
						dataset.addValue(0, series2, "      ");
						dataset.addValue(0, series3, "      ");
					}
				}
				JFreeChart chart = ChartFactory.createBarChart("", // chart
																	// title
						"", // domain axis label
						"", // range axis label
						dataset, // data
						PlotOrientation.VERTICAL, // orientation
						false, // include legend
						true, // tooltips?
						false // URLs?
						);

				applyBarChartFormat(chart);

				this._reportChartPanel.setChart(chart);

				addTableView(devicesData, 3, "People", "Visits", "Actions");

			}

		} else if (selectedReportMenu.compareToIgnoreCase("Platforms") == 0) {

			this.lblreportTitle.setText("Platforms");
			System.out.println("constantDuration: " + constantDuration
					+ " Gateway: " + this.gateway + " Project: " + this.project
					+ "      AllGateways: " + this.reportForAllGateways
					+ " AllProjects: " + this.reportForAllProjects);
			reportData = rpc.getPlatformReportController(constantDuration,
					this.gateway, this.project, this.reportForAllGateways,
					this.reportForAllProjects);
			DefaultTableModel platformsData = new DefaultTableModel(0, 3);
			platformsData.setColumnIdentifiers(new Object[] { "Platforms",
					"People", "Visits", "Actions" });

			if (reportData != null) {
				noOfRecords = reportData.getRowCount();

				String series1 = "People";
				String series2 = "Visits";
				String series3 = "Actions";

				DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				String category = "";
				int noOfPeople = 0;
				int noOfActions = 0;
				int noOfvisits = 0;
				int totalNoOfPeople = 0;
				int totalNoOfVisits = 0;
				int totalNoOfActions = 0;
				for (int i = 0; i < noOfRecords; i++) {
					category = "";
					noOfPeople = 0;
					noOfActions = 0;
					noOfvisits = 0;

					if (reportData.getValueAt(i, 0) != null) {
						category = reportData.getValueAt(i, 0).toString();
					}
					if (reportData.getValueAt(i, 1) != null) {
						noOfPeople = (int) Float.parseFloat(reportData
								.getValueAt(i, 1).toString());
					}
					if (reportData.getValueAt(i, 2) != null) {
						noOfvisits = (int) Float.parseFloat(reportData
								.getValueAt(i, 2).toString());
					}
					if (reportData.getValueAt(i, 3) != null) {
						noOfActions = (int) Float.parseFloat(reportData
								.getValueAt(i, 3).toString());
					}
					if (reportData.getValueAt(i, 4) != null) {
						noOfActions = noOfActions
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, 4).toString());
					}
					if (i < 10) {

						dataset.addValue(noOfPeople, series1, category);
						dataset.addValue(noOfvisits, series2, category);
						dataset.addValue(noOfActions, series3, category);
					}
					platformsData.addRow(new Object[] { category, noOfPeople,
							noOfvisits, noOfActions });

					totalNoOfPeople = totalNoOfPeople + noOfPeople;
					totalNoOfVisits = totalNoOfVisits + noOfvisits;
					totalNoOfActions = totalNoOfActions + noOfActions;
				}

				// if(noOfRecords > 0){
				platformsData.addRow(new Object[] { noOfRecords + " Result(s)",
						this.noOfPeople + " People",
						totalNoOfVisits + " Visits",
						totalNoOfActions + " Actions" });
				// }
				// to create dummy records in case dataset is less.
				if (noOfRecords < 10) {
					for (int i = noOfRecords - 1; i < 10; i++) {
						dataset.addValue(0, series1, " ");
						dataset.addValue(0, series2, " ");
						dataset.addValue(0, series3, " ");
					}
				}
				JFreeChart chart = ChartFactory.createBarChart("", // chart
																	// title
						"", // domain axis label
						"", // range axis label
						dataset, // data
						PlotOrientation.VERTICAL, // orientation
						false, // include legend
						true, // tooltips?
						false // URLs?
						);

				applyBarChartFormat(chart);

				this._reportChartPanel.setChart(chart);

				addTableView(platformsData, 3, "People", "Visits", "Actions");

			}

		} else if (selectedReportMenu.compareToIgnoreCase("Browsers") == 0) {
			this.lblreportTitle.setText("Browsers");
			
			System.out.println("constantDuration: " + constantDuration
					+ " Gateway: " + this.gateway + " Project: " + this.project
					+ "      AllGateways: " + this.reportForAllGateways
					+ " AllProjects: " + this.reportForAllProjects);
			
			// query total no of distinct users across all browsers. This is
			// because mobile clients would not have browser information.
			int noOfUsers = rpc.getDistinctUsersFromBrowsersController(
					constantDuration, this.gateway, this.project,
					this.reportForAllGateways, this.reportForAllProjects);

			reportData = rpc.getBrowserReportController(constantDuration,
					this.gateway, this.project, this.reportForAllGateways,
					this.reportForAllProjects);
			DefaultTableModel browsersData = new DefaultTableModel(0, 3);
			browsersData.setColumnIdentifiers(new Object[] { "Browsers",
					"People", "Visits", "Actions" });

			if (reportData != null) {
				noOfRecords = reportData.getRowCount();

				String series1 = "People";
				String series2 = "Visits";
				String series3 = "Actions";

				DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				String category = "";
				int noOfPeople = 0;
				int noOfActions = 0;
				int noOfvisits = 0;
				int totalNoOfPeople = 0;
				int totalNoOfVisits = 0;
				int totalNoOfActions = 0;
				for (int i = 0; i < noOfRecords; i++) {
					category = "";
					noOfPeople = 0;
					noOfActions = 0;
					noOfvisits = 0;

					if (reportData.getValueAt(i, 0) != null) {
						category = reportData.getValueAt(i, 0).toString();
					}
					if (reportData.getValueAt(i, 3) != null) {
						noOfPeople = (int) Float.parseFloat(reportData
								.getValueAt(i, 3).toString());
					}
					if (reportData.getValueAt(i, 2) != null) {
						noOfvisits = (int) Float.parseFloat(reportData
								.getValueAt(i, 2).toString());
					}
					if (reportData.getValueAt(i, 1) != null) {
						noOfActions = (int) Float.parseFloat(reportData
								.getValueAt(i, 1).toString());
					}
					if (reportData.getValueAt(i, 4) != null) {
						noOfActions = noOfActions
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, 4).toString());
					}
					if (i < 10) {
						dataset.addValue(noOfPeople, series1, category);
						dataset.addValue(noOfvisits, series2, category);
						dataset.addValue(noOfActions, series3, category);
					}
					browsersData.addRow(new Object[] { category, noOfPeople,
							noOfvisits, noOfActions });
					totalNoOfPeople = totalNoOfPeople + noOfPeople;
					totalNoOfVisits = totalNoOfVisits + noOfvisits;
					totalNoOfActions = totalNoOfActions + noOfActions;
				}

				// if(noOfRecords > 0){
				browsersData.addRow(new Object[] { noOfRecords + " Result(s)",
						this.noOfPeople + " People", totalNoOfVisits + " Visits",
						totalNoOfActions + " Actions" });
				// }
				// to create dummy records in case dataset is less.
				if (noOfRecords < 10) {
					for (int i = noOfRecords - 1; i < 10; i++) {
						dataset.addValue(0, series1, " ");
						dataset.addValue(0, series2, " ");
						dataset.addValue(0, series3, " ");
					}
				}
				JFreeChart chart = ChartFactory.createBarChart("", // chart
																	// title
						"", // domain axis label
						"", // range axis label
						dataset, // data
						PlotOrientation.VERTICAL, // orientation
						false, // include legend
						true, // tooltips?
						false // URLs?
						);

				applyBarChartFormat(chart);

				this._reportChartPanel.setChart(chart);

				addTableView(browsersData, 3, "People", "Visits", "Actions");

			}

		} else if (selectedReportMenu.compareToIgnoreCase("Screen Resolutions") == 0) {
			this.lblreportTitle.setText("Screen Resolutions");
			reportData = rpc.getScreenResolutionDataController(this.gateway,
					this.project, this.reportForAllGateways,
					this.reportForAllProjects, constantDuration);
			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			int size = 0;
			int totalNoOfPeople = 0;
			int totalNoOfVisits = 0;
			int totalNoOfActions = 0;
			size = reportData.getRowCount();
			String tempVal;

			DefaultTableModel screenDepthTableModel = new DefaultTableModel();
			screenDepthTableModel.setColumnIdentifiers(new Object[] {
					"Screen Resolution"," People", "Visits", "Actions", });

			for (int i = 0; i < size; i++) {
				String screen = reportData.getValueAt(i, "SCREEN_RESOLUTION").toString();
				int peopleVal = (int) Float.parseFloat(reportData.getValueAt(i,
						"People").toString());
				int actionVal = (int) Float.parseFloat(reportData.getValueAt(i,
						"Actions").toString());
				actionVal = actionVal
						+ (int) Float.parseFloat(reportData.getValueAt(i,
								"noOfScreens").toString());
				int visitsVal = (int) Float.parseFloat(reportData.getValueAt(i,
						"Visits").toString());

				dataset.addValue(peopleVal, series1, screen);
				dataset.addValue(visitsVal, series2, screen);
				dataset.addValue(actionVal, series3, screen);

				screenDepthTableModel.addRow(new Object[] { screen, peopleVal,
						visitsVal, actionVal });
				totalNoOfPeople = totalNoOfPeople + peopleVal;
				totalNoOfVisits = totalNoOfVisits + visitsVal;
				totalNoOfActions = totalNoOfActions + actionVal;
			}
			// if(size > 0){
			screenDepthTableModel.addRow(new Object[] { size + " Result(s)",
					this.noOfPeople + " People", totalNoOfVisits + " Visits",
					totalNoOfActions + " Actions" });
			// }

			// to create dummy records in case dataset is less.
			if (size < 10) {
				for (int i = size - 1; i < 10; i++) {
					dataset.addValue(0, series1, "        .");
					dataset.addValue(0, series2, "        .");
					dataset.addValue(0, series3, "        .");
				}
			}
			// Draw Graph

			JFreeChart chart = ChartFactory.createBarChart("", // chart title
					"", // domain axis label
					"", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips?
					false // URLs?
					);

			applyBarChartFormat(chart);

			this._reportChartPanel.setChart(chart);
			// data for table

			addTableView(screenDepthTableModel, 3, "People", "Visits",
					"Actions");
		}

		// behavior reports
		else if (selectedReportMenu.compareToIgnoreCase("Actions Per Visit") == 0) {
			this.lblreportTitle.setText("Actions Per Visit");

			reportData = rpc.getActionsPerVisitReportInformationController(
					this.gateway, this.project, this.reportForAllGateways,
					this.reportForAllProjects, constantDuration);

			int actionPerVisit_1_5People = 0, actionPerVisit_1_5Visits = 0, actionPerVisit_1_5Actions = 0;
			int actionPerVisit_6to10_People = 0, actionPerVisit_6to10_Visits = 0, actionPerVisit_6to10_Actions = 0;
			int actionPerVisit_10to20_People = 0, actionPerVisit_10to20_Visits = 0, actionPerVisit_10to20_Actions = 0;
			int actionPerVisit_21to30_People = 0, actionPerVisit_21to30_Visits = 0, actionPerVisit_21to30_Actions = 0;
			int actionPerVisit_31to50_People = 0, actionPerVisit_31to50_Visits = 0, actionPerVisit_31to50_Actions = 0;
			int actionPerVisit_51plus_People = 0, actionPerVisit_51plus_Visits = 0, actionPerVisit_51plus_Actions = 0;

			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			int size = 0;
			int totalPeople = 0, totalVisits = 0, totalActions = 0;
			if (reportData != null) {
				size = reportData.getRowCount();
				String tempVal;

				for (int i = 0; i < size; i++) {
					if (reportData.getValueAt(i, "count_of_actions") != null) {
						tempVal = reportData.getValueAt(i, "count_of_actions")
								.toString();
						if (tempVal.compareToIgnoreCase("NULL") == 0) {
							// i++;
						} else if (tempVal.compareToIgnoreCase("1-5") == 0) {
							actionPerVisit_1_5People = actionPerVisit_1_5People
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "People").toString());
							actionPerVisit_1_5Visits = actionPerVisit_1_5Visits
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "Visits").toString());
							actionPerVisit_1_5Actions = actionPerVisit_1_5Actions
									+ (int) Float.parseFloat(reportData
											.getValueAt(i, "Actions")
											.toString());
						} else if (tempVal.compareToIgnoreCase("6-10") == 0) {
							actionPerVisit_6to10_People = actionPerVisit_6to10_People
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "People").toString());
							;
							actionPerVisit_6to10_Visits = actionPerVisit_6to10_Visits
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "Visits").toString());
							actionPerVisit_6to10_Actions = actionPerVisit_6to10_Actions
									+ (int) Float.parseFloat(reportData
											.getValueAt(i, "Actions")
											.toString());
						} else if (tempVal.compareToIgnoreCase("11-20") == 0) {
							actionPerVisit_10to20_People = actionPerVisit_10to20_People
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "People").toString());
							;
							actionPerVisit_10to20_Visits = actionPerVisit_10to20_Visits
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "Visits").toString());
							actionPerVisit_10to20_Actions = actionPerVisit_10to20_Actions
									+ (int) Float.parseFloat(reportData
											.getValueAt(i, "Actions")
											.toString());
						} else if (tempVal.compareToIgnoreCase("21-30") == 0) {
							actionPerVisit_21to30_People = actionPerVisit_21to30_People
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "People").toString());
							;
							actionPerVisit_21to30_Visits = actionPerVisit_21to30_Visits
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "Visits").toString());
							actionPerVisit_21to30_Actions = actionPerVisit_21to30_Actions
									+ (int) Float.parseFloat(reportData
											.getValueAt(i, "Actions")
											.toString());
						} else if (tempVal.compareToIgnoreCase("31-50") == 0) {
							actionPerVisit_31to50_People = actionPerVisit_31to50_People
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "People").toString());
							;
							actionPerVisit_31to50_Visits = actionPerVisit_31to50_Visits
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "Visits").toString());
							actionPerVisit_31to50_Actions = actionPerVisit_31to50_Actions
									+ (int) Float.parseFloat(reportData
											.getValueAt(i, "Actions")
											.toString());
						} else if (tempVal.compareToIgnoreCase("51 or more") == 0) {
							actionPerVisit_51plus_People = actionPerVisit_51plus_People
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "People").toString());
							;
							actionPerVisit_51plus_Visits = actionPerVisit_51plus_Visits
									+ (int) Float
											.parseFloat(reportData.getValueAt(
													i, "Visits").toString());
							actionPerVisit_51plus_Actions = actionPerVisit_51plus_Actions
									+ (int) Float.parseFloat(reportData
											.getValueAt(i, "Actions")
											.toString());
						}
					}

				}
				// data for graph

				dataset.addValue(actionPerVisit_1_5People, series1, "1 to 5");
				dataset.addValue(actionPerVisit_1_5Visits, series2, "1 to 5");
				dataset.addValue(actionPerVisit_1_5Actions, series3, "1 to 5");

				dataset.addValue(actionPerVisit_6to10_People, series1,
						"6 to 10");
				dataset.addValue(actionPerVisit_6to10_Visits, series2,
						"6 to 10");
				dataset.addValue(actionPerVisit_6to10_Actions, series3,
						"6 to 10");

				dataset.addValue(actionPerVisit_10to20_People, series1,
						"11 to 20");
				dataset.addValue(actionPerVisit_10to20_Visits, series2,
						"11 to 20");
				dataset.addValue(actionPerVisit_10to20_Actions, series3,
						"11 to 20");

				dataset.addValue(actionPerVisit_21to30_People, series1,
						"21 to 30");
				dataset.addValue(actionPerVisit_21to30_Visits, series2,
						"21 to 30");
				dataset.addValue(actionPerVisit_21to30_Actions, series3,
						"21 to 30");

				dataset.addValue(actionPerVisit_31to50_People, series1,
						"31 to 50");
				dataset.addValue(actionPerVisit_31to50_Visits, series2,
						"31 to 50");
				dataset.addValue(actionPerVisit_31to50_Actions, series3,
						"31 to 50");

				dataset.addValue(actionPerVisit_51plus_People, series1,
						"51 or More");
				dataset.addValue(actionPerVisit_51plus_Visits, series2,
						"51 or More");
				dataset.addValue(actionPerVisit_51plus_Actions, series3,
						"51 or More");

				// to create dummy records in case dataset is less.
				for (int dummy = 0; dummy < 4; dummy++) {
					dataset.addValue(0, series1, " ");
					dataset.addValue(0, series2, " ");
					dataset.addValue(0, series3, " ");
				}
				JFreeChart chart = ChartFactory.createBarChart("", // chart
																	// title
						"", // domain axis label
						"", // range axis label
						dataset, // data
						PlotOrientation.VERTICAL, // orientation
						false, // include legend
						true, // tooltips?
						false // URLs?
						);

				applyBarChartFormat(chart);

				this._reportChartPanel.setChart(chart);

				// Data for table
				DefaultTableModel screenDepthTableModel = new DefaultTableModel();
				screenDepthTableModel.setColumnIdentifiers(new Object[] {
						"Actions Per Visit", " People", "Visits", "Actions" });

				// if(size > 0)
				// {
				screenDepthTableModel.addRow(new Object[] { "1 to 5",
						actionPerVisit_1_5People, actionPerVisit_1_5Visits,
						actionPerVisit_1_5Actions });
				screenDepthTableModel.addRow(new Object[] { "6 to 10",
						actionPerVisit_6to10_People,
						actionPerVisit_6to10_Visits,
						actionPerVisit_6to10_Actions });
				screenDepthTableModel.addRow(new Object[] { "11 to 20",
						actionPerVisit_10to20_People,
						actionPerVisit_10to20_Visits,
						actionPerVisit_10to20_Actions });
				screenDepthTableModel.addRow(new Object[] { "21 to 30",
						actionPerVisit_21to30_People,
						actionPerVisit_21to30_Visits,
						actionPerVisit_21to30_Actions });
				screenDepthTableModel.addRow(new Object[] { "31 to 50",
						actionPerVisit_31to50_People,
						actionPerVisit_31to50_Visits,
						actionPerVisit_31to50_Actions });
				screenDepthTableModel.addRow(new Object[] { "51 or More",
						actionPerVisit_51plus_People,
						actionPerVisit_51plus_Visits,
						actionPerVisit_51plus_Actions });

				totalPeople = actionPerVisit_1_5People
						+ actionPerVisit_6to10_People
						+ actionPerVisit_10to20_People
						+ actionPerVisit_21to30_People
						+ actionPerVisit_31to50_People
						+ actionPerVisit_51plus_People;

				totalVisits = actionPerVisit_1_5Visits
						+ actionPerVisit_6to10_Visits
						+ actionPerVisit_10to20_Visits
						+ actionPerVisit_21to30_Visits
						+ actionPerVisit_31to50_Visits
						+ actionPerVisit_51plus_Visits;

				totalActions = actionPerVisit_1_5Actions
						+ actionPerVisit_6to10_Actions
						+ actionPerVisit_10to20_Actions
						+ actionPerVisit_21to30_Actions
						+ actionPerVisit_31to50_Actions
						+ actionPerVisit_51plus_Actions;
				screenDepthTableModel.addRow(new Object[] { "",
						this.noOfPeople + " People", totalVisits + " Visits",
						totalActions + " Actions" });
				// }

				addTableView(screenDepthTableModel, 3, "People", "Visits",
						"Actions");

			}

		} else if (selectedReportMenu.compareToIgnoreCase("Visit Duration") == 0) {
			this.lblreportTitle.setText("Visit Duration");

			reportData = rpc.getVisitDurationReportInformationController(
					this.gateway, this.project, this.reportForAllGateways,
					this.reportForAllProjects, constantDuration);

			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			int people_0_5 = 0, visits_0_5 = 0, actions_0_5 = 0;
			int people_6_10 = 0, visits_6_10 = 0, actions_6_10 = 0;
			int people_11_20 = 0, visits_11_20 = 0, actions_11_20 = 0;
			int people_21_30 = 0, visits_21_30 = 0, actions_21_30 = 0;
			int people_31_40 = 0, visits_31_40 = 0, actions_31_40 = 0;
			int people_41_50 = 0, visits_41_50 = 0, actions_41_50 = 0;
			int people_51_60 = 0, visits_51_60 = 0, actions_51_60 = 0;
			int people_61_120 = 0, visits_61_120 = 0, actions_61_120 = 0;
			int people_120 = 0, visits_120 = 0, actions_120 = 0;
			
			
			int size = 0;
			
			if(reportData != null)
			{
				size = reportData.getRowCount();
			}

			String tempVal = "";

			for (int i = 0; i < size; i++) {

				if (reportData.getValueAt(i, "session_duration_mins") != null) {
					tempVal = reportData.getValueAt(i, "session_duration_mins")
							.toString();
					if (tempVal.compareToIgnoreCase("0-5") == 0) {
						people_0_5 = people_0_5
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_0_5 = visits_0_5
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_0_5 = actions_0_5
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("11-20") == 0) {
						people_11_20 = people_11_20
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_11_20 = visits_11_20
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_11_20 = actions_11_20
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("120") == 0) {
						people_120 = people_120
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_120 = visits_120
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_120 = actions_120
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("21-30") == 0) {
						people_21_30 = people_21_30
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_21_30 = visits_21_30
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_21_30 = actions_21_30
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("31-40") == 0) {
						people_31_40 = people_31_40
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_31_40 = visits_31_40
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_31_40 = actions_31_40
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("41-50") == 0) {
						people_41_50 = people_41_50
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_41_50 = visits_41_50
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_41_50 = actions_41_50
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("51-60") == 0) {
						people_51_60 = people_51_60
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_51_60 = visits_51_60
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_51_60 = actions_51_60
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("6-10") == 0) {
						people_6_10 = people_6_10
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_6_10 = visits_6_10
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_6_10 = actions_6_10
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					} else if (tempVal.compareToIgnoreCase("61-120") == 0) {
						people_61_120 = people_61_120
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "People").toString());
						visits_61_120 = visits_61_120
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Visits").toString());
						actions_61_120 = actions_61_120
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "Actions").toString())
								+ (int) Float.parseFloat(reportData.getValueAt(
										i, "noOfScreens").toString());
					}
				}
			}

			dataset.addValue(people_0_5, series1, "0-5 min.");
			dataset.addValue(visits_0_5, series2, "0-5 min.");
			dataset.addValue(actions_0_5, series3, "0-5 min.");

			dataset.addValue(people_6_10, series1, "6-10 min.");
			dataset.addValue(visits_6_10, series2, "6-10 min.");
			dataset.addValue(actions_6_10, series3, "6-10 min.");

			dataset.addValue(people_11_20, series1, "11-20 min.");
			dataset.addValue(visits_11_20, series2, "11-20 min.");
			dataset.addValue(actions_11_20, series3, "11-20 min.");

			dataset.addValue(people_21_30, series1, "21-30 min.");
			dataset.addValue(visits_21_30, series2, "21-30 min.");
			dataset.addValue(actions_21_30, series3, "21-30 min.");

			dataset.addValue(people_31_40, series1, "31-40 min.");
			dataset.addValue(visits_31_40, series2, "31-40 min.");
			dataset.addValue(actions_31_40, series3, "31-40 min.");

			dataset.addValue(people_41_50, series1, "41-50 min.");
			dataset.addValue(visits_41_50, series2, "41-50 min.");
			dataset.addValue(actions_41_50, series3, "41-50 min.");

			dataset.addValue(people_51_60, series1, "51 min.-1 hr.");
			dataset.addValue(visits_51_60, series2, "51 min.-1 hr.");
			dataset.addValue(actions_51_60, series3, "51 min.-1 hr.");

			dataset.addValue(people_61_120, series1, "1 hr.-2 hr.");
			dataset.addValue(visits_61_120, series2, "1 hr.-2 hr.");
			dataset.addValue(actions_61_120, series3, "1 hr.-2 hr.");

			dataset.addValue(people_120, series1, "2+ hr.");
			dataset.addValue(visits_120, series2, "2+ hr.");
			dataset.addValue(actions_120, series3, "2+ hr.");

			JFreeChart chart = ChartFactory.createBarChart("", // chart title
					"", // domain axis label
					"", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips?
					false // URLs?
					);

			applyBarChartFormat(chart);

			this._reportChartPanel.setChart(chart);

			// Data for table
			DefaultTableModel visitDutrationTableModel = new DefaultTableModel();
			visitDutrationTableModel.setColumnIdentifiers(new Object[] {
					"Visit Duration", " People", "Visits", "Actions" });

			visitDutrationTableModel.addRow(new Object[] { "0-5 minutes",
					people_0_5, visits_0_5, actions_0_5 });
			visitDutrationTableModel.addRow(new Object[] { "6-10 minutes",
					people_6_10, visits_6_10, actions_6_10 });
			visitDutrationTableModel.addRow(new Object[] { "11-20 minutes",
					people_11_20, visits_11_20, actions_11_20 });
			visitDutrationTableModel.addRow(new Object[] { "21-30 minutes",
					people_21_30, visits_21_30, actions_21_30 });
			visitDutrationTableModel.addRow(new Object[] { "31-40 minutes",
					people_31_40, visits_31_40, actions_31_40 });
			visitDutrationTableModel.addRow(new Object[] { "41-50 minutes",
					people_41_50, visits_41_50, actions_41_50 });
			visitDutrationTableModel.addRow(new Object[] { "51 minutes-1 hour",
					people_51_60, visits_51_60, actions_51_60 });
			visitDutrationTableModel.addRow(new Object[] { "1 hour-2 hours",
					people_61_120, visits_61_120, actions_61_120 });
			visitDutrationTableModel.addRow(new Object[] { "2+ hours",
					people_120, visits_120, actions_120 });

			// compute the summary row
			int totalPeople = people_0_5 + people_6_10 + people_11_20
					+ people_21_30 + people_31_40 + people_41_50 + people_51_60
					+ people_61_120 + people_120;

			int totalVisits = visits_0_5 + visits_6_10 + visits_11_20
					+ visits_21_30 + visits_31_40 + visits_41_50 + visits_51_60
					+ visits_61_120 + visits_120;
			int totalActions = actions_0_5 + actions_6_10 + actions_11_20
					+ actions_21_30 + actions_31_40 + actions_41_50
					+ actions_51_60 + actions_61_120 + actions_120;
			
			
			visitDutrationTableModel.addRow(new Object[] { "",
					this.noOfPeople + " People", totalVisits + " Visits",
					totalActions + " Actions" });

			addTableView(visitDutrationTableModel, 3, "People", "Visits",
					"Actions");

		} else if (selectedReportMenu.compareToIgnoreCase("Engagement") == 0) {
			this.lblreportTitle.setText("Engagement");

			reportData = rpc
					.getEngagementReportInformationScreenDepthController(
							this.gateway, this.project,
							this.reportForAllGateways,
							this.reportForAllProjects, constantDuration);
			int Screen_1_People = 0, Screen_1_Visits = 0, Screen_1_Actions = 0;
			int Screen_2_People = 0, Screen_2_Visits = 0, Screen_2_Actions = 0;
			int Screen_3_People = 0, Screen_3_Visits = 0, Screen_3_Actions = 0;
			int Screen_4_People = 0, Screen_4_Visits = 0, Screen_4_Actions = 0;
			int Screen_5_People = 0, Screen_5_Visits = 0, Screen_5_Actions = 0;
			int Screen_6to10_People = 0, Screen_6to10_Visits = 0, Screen_6to10_Actions = 0;
			int Screen_11to15_People = 0, Screen_11to15_Visits = 0, Screen_11to15_Actions = 0;
			int Screen_16to20_People = 0, Screen_16to20_Visits = 0, Screen_16to20_Actions = 0;
			int Screen_20Plus_People = 0, Screen_20Plus_Visits = 0, Screen_20Plus_Actions = 0;

			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			int size = 0;

			size = reportData.getRowCount();
			int tempVal = 0;
			for (int i = 0; i < size; i++) {

				if (reportData.getValueAt(i, "no_of_screens") != null) {
					tempVal = (int) Float.parseFloat(reportData.getValueAt(i,
							"no_of_screens").toString());
				} else {
					tempVal = 0;
				}
				if (tempVal == 1) {
					Screen_1_People = Screen_1_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_1_Actions = Screen_1_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_1_Visits = Screen_1_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal == 2) {
					Screen_2_People = Screen_2_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_2_Actions = Screen_2_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_2_Visits = Screen_2_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal == 3) {
					Screen_3_People = Screen_3_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_3_Actions = Screen_3_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_3_Visits = Screen_3_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal == 4) {
					Screen_4_People = Screen_4_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_4_Actions = Screen_4_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_4_Visits = Screen_4_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal == 5) {
					Screen_5_People = Screen_5_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_5_Actions = Screen_5_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_5_Visits = Screen_5_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal >= 6 && tempVal <= 10) {
					Screen_6to10_People = Screen_6to10_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_6to10_Actions = Screen_6to10_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_6to10_Visits = Screen_6to10_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal >= 11 && tempVal <= 15) {
					Screen_11to15_People = Screen_11to15_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_11to15_Actions = Screen_11to15_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_11to15_Visits = Screen_11to15_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal >= 16 && tempVal <= 20) {
					Screen_16to20_People = Screen_16to20_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_16to20_Actions = Screen_16to20_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_16to20_Visits = Screen_16to20_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				} else if (tempVal > 20) {
					Screen_20Plus_People = Screen_20Plus_People
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"People").toString());
					Screen_20Plus_Actions = Screen_20Plus_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString());
					Screen_20Plus_Visits = Screen_20Plus_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Sessions").toString());
				}

			}

			// data for graph

			dataset.addValue(Screen_1_People, series1, "1 screen");
			dataset.addValue(Screen_1_Visits, series2, "1 screen");
			dataset.addValue(Screen_1_Actions, series3, "1 screen");

			dataset.addValue(Screen_2_People, series1, "2 screens");
			dataset.addValue(Screen_2_Visits, series2, "2 screens");
			dataset.addValue(Screen_2_Actions, series3, "2 screens");

			dataset.addValue(Screen_3_People, series1, "3 screens");
			dataset.addValue(Screen_3_Visits, series2, "3 screens");
			dataset.addValue(Screen_3_Actions, series3, "3 screens");

			dataset.addValue(Screen_4_People, series1, "4 screens");
			dataset.addValue(Screen_4_Visits, series2, "4 screens");
			dataset.addValue(Screen_4_Actions, series3, "4 screens");

			dataset.addValue(Screen_5_People, series1, "5 screens");
			dataset.addValue(Screen_5_Visits, series2, "5 screens");
			dataset.addValue(Screen_5_Actions, series3, "5 screens");

			dataset.addValue(Screen_6to10_People, series1, "6-10 screens");
			dataset.addValue(Screen_6to10_Visits, series2, "6-10 screens");
			dataset.addValue(Screen_6to10_Actions, series3, "6-10 screens");

			dataset.addValue(Screen_11to15_People, series1, "11-15 screens");
			dataset.addValue(Screen_11to15_Visits, series2, "11-15 screens");
			dataset.addValue(Screen_11to15_Actions, series3, "11-15 screens");

			dataset.addValue(Screen_16to20_People, series1, "16-20 screens");
			dataset.addValue(Screen_16to20_Visits, series2, "16-20 screens");
			dataset.addValue(Screen_16to20_Actions, series3, "16-20 screens");

			dataset.addValue(Screen_20Plus_People, series1, "20+ screens");
			dataset.addValue(Screen_20Plus_Visits, series2, "20+ screens");
			dataset.addValue(Screen_20Plus_Actions, series3, "20+ screens");

			JFreeChart chart = ChartFactory.createBarChart("", // chart title
					"", // domain axis label
					"", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips?
					false // URLs?
					);

			applyBarChartFormat(chart);

			this._reportChartPanel.setChart(chart);

			// Data for table
			DefaultTableModel screenDepthTableModel = new DefaultTableModel();
			screenDepthTableModel.setColumnIdentifiers(new Object[] {
					"Depth of Screen Viewed", " People", "Visits", "Actions" });

			// if(size > 0)
			// {
			screenDepthTableModel.addRow(new Object[] { "1 screen",
					Screen_1_People, Screen_1_Visits, Screen_1_Actions });
			screenDepthTableModel.addRow(new Object[] { "2 screens",
					Screen_2_People, Screen_2_Visits, Screen_2_Actions });
			screenDepthTableModel.addRow(new Object[] { "3 screens",
					Screen_3_People, Screen_3_Visits, Screen_3_Actions });
			screenDepthTableModel.addRow(new Object[] { "4 screens",
					Screen_4_People, Screen_4_Visits, Screen_4_Actions });
			screenDepthTableModel.addRow(new Object[] { "5 screens",
					Screen_5_People, Screen_5_Visits, Screen_5_Actions });
			screenDepthTableModel.addRow(new Object[] { "6-10 screens",
					Screen_6to10_People, Screen_6to10_Visits,
					Screen_6to10_Actions });
			screenDepthTableModel.addRow(new Object[] { "11-15 screens",
					Screen_11to15_People, Screen_11to15_Visits,
					Screen_11to15_Actions });
			screenDepthTableModel.addRow(new Object[] { "16-20 screens",
					Screen_16to20_People, Screen_16to20_Visits,
					Screen_16to20_Actions });
			screenDepthTableModel.addRow(new Object[] { "20+ screens",
					Screen_20Plus_People, Screen_20Plus_Visits,
					Screen_20Plus_Actions });
			// }

			int totalPeople = Screen_1_People + Screen_2_People
					+ Screen_3_People + Screen_4_People + Screen_5_People
					+ Screen_6to10_People + Screen_11to15_People
					+ Screen_16to20_People + Screen_20Plus_People;

			int totalVisits = Screen_1_Visits + Screen_2_Visits
					+ Screen_3_Visits + Screen_4_Visits + Screen_5_Visits
					+ Screen_6to10_Visits + Screen_11to15_Visits
					+ Screen_16to20_Visits + Screen_20Plus_Visits;
			int totalActions = Screen_1_Actions + Screen_2_Actions
					+ Screen_3_Actions + Screen_4_Actions + Screen_5_Actions
					+ Screen_6to10_Actions + Screen_11to15_Actions
					+ Screen_16to20_Actions + Screen_20Plus_Actions;
			
			
			screenDepthTableModel.addRow(new Object[] { "",
					this.noOfPeople + " People", totalVisits + " Visits",
					totalActions + " Actions" });

			addTableView(screenDepthTableModel, 3, "People", "Visits",
					"Actions");

		} else if (selectedReportMenu.compareToIgnoreCase("Frequency") == 0) {
			this.lblreportTitle.setText("Frequency");

			reportData = rpc.getFrequencyReportInformationController(gateway,
					project, reportForAllGateways, reportForAllProjects,
					constantDuration);

			int Session_1_People = 0, Session_1_Visits = 0, Session_1_Actions = 0;
			int Session_2_People = 0, Session_2_Visits = 0, Session_2_Actions = 0;
			int Session_3_People = 0, Session_3_Visits = 0, Session_3_Actions = 0;
			int Session_4_People = 0, Session_4_Visits = 0, Session_4_Actions = 0;
			int Session_5_People = 0, Session_5_Visits = 0, Session_5_Actions = 0;
			int Session_6to10_People = 0, Session_6to10_Visits = 0, Session_6to10_Actions = 0;
			int Session_11to15_People = 0, Session_11to15_Visits = 0, Session_11to15_Actions = 0;
			int Session_16to20_People = 0, Session_16to20_Visits = 0, Session_16to20_Actions = 0;
			int Session_20Plus_People = 0, Session_20Plus_Visits = 0, Session_20Plus_Actions = 0;

			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			int size = 0;

			if (reportData != null) {
				size = reportData.getRowCount();
			}
			int tempVal;

			for (int i = 0; i < size; i++) {
				tempVal = (int) Float.parseFloat(reportData.getValueAt(i,
						"Visits").toString());

				if (tempVal == 1 || tempVal == 0) {
					Session_1_People = Session_1_People + 1;
					Session_1_Actions = Session_1_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_1_Visits = Session_1_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 2) {
					Session_2_People = Session_2_People + 1;
					Session_2_Actions = Session_2_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_2_Visits = Session_2_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 3) {
					Session_3_People = Session_3_People + 1;
					Session_3_Actions = Session_3_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_3_Visits = Session_3_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 4) {
					Session_4_People = Session_4_People + 1;
					Session_4_Actions = Session_4_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_4_Visits = Session_4_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 5) {
					Session_5_People = Session_5_People + 1;
					Session_5_Actions = Session_5_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_5_Visits = Session_5_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal > 5 && tempVal <= 10) {
					Session_6to10_People = Session_6to10_People + 1;
					Session_6to10_Actions = Session_6to10_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_6to10_Visits = Session_6to10_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal > 10 && tempVal <= 15) {
					Session_11to15_People = Session_11to15_People + 1;
					Session_11to15_Actions = Session_11to15_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_11to15_Visits = Session_11to15_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal > 15 && tempVal <= 20) {
					Session_16to20_People = Session_16to20_People + 1;
					Session_16to20_Actions = Session_16to20_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_16to20_Visits = Session_16to20_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal > 20) {
					Session_20Plus_People = Session_20Plus_People + 1;
					Session_20Plus_Actions = Session_20Plus_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					Session_20Plus_Visits = Session_20Plus_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}

			}

			// Data for bar chart

			dataset.addValue(Session_1_People, series1, "1 session");
			dataset.addValue(Session_1_Visits, series2, "1 session");
			dataset.addValue(Session_1_Actions, series3, "1 session");

			dataset.addValue(Session_2_People, series1, "2 sessions");
			dataset.addValue(Session_2_Visits, series2, "2 sessions");
			dataset.addValue(Session_2_Actions, series3, "2 sessions");

			dataset.addValue(Session_3_People, series1, "3 sessions");
			dataset.addValue(Session_3_Visits, series2, "3 sessions");
			dataset.addValue(Session_3_Actions, series3, "3 sessions");

			dataset.addValue(Session_4_People, series1, "4 sessions");
			dataset.addValue(Session_4_Visits, series2, "4 sessions");
			dataset.addValue(Session_4_Actions, series3, "4 sessions");

			dataset.addValue(Session_5_People, series1, "5 sessions");
			dataset.addValue(Session_5_Visits, series2, "5 sessions");
			dataset.addValue(Session_5_Actions, series3, "5 sessions");

			dataset.addValue(Session_6to10_People, series1, "6-10 sessions");
			dataset.addValue(Session_6to10_Visits, series2, "6-10 sessions");
			dataset.addValue(Session_6to10_Actions, series3, "6-10 sessions");

			dataset.addValue(Session_11to15_People, series1, "11-15 sessions");
			dataset.addValue(Session_11to15_Visits, series2, "11-15 sessions");
			dataset.addValue(Session_11to15_Actions, series3, "11-15 sessions");

			dataset.addValue(Session_16to20_People, series1, "16-20 sessions");
			dataset.addValue(Session_16to20_Visits, series2, "16-20 sessions");
			dataset.addValue(Session_16to20_Actions, series3, "16-20 sessions");

			dataset.addValue(Session_20Plus_People, series1, "20+ sessions");
			dataset.addValue(Session_20Plus_Visits, series2, "20+ sessions");
			dataset.addValue(Session_20Plus_Actions, series3, "20+ sessions");

			JFreeChart chart = ChartFactory.createBarChart("", // chart title
					"", // domain axis label
					"", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips?
					false // URLs?
					);

			applyBarChartFormat(chart);

			this._reportChartPanel.setChart(chart);

			// Data for table

			DefaultTableModel screenDepthTableModel = new DefaultTableModel();
			screenDepthTableModel.setColumnIdentifiers(new Object[] {
					"Count of sessions", " People", "Visits", "Actions" });

			// if(size > 0)
			// {
			screenDepthTableModel.addRow(new Object[] { "1 session",
					Session_1_People, Session_1_Visits, Session_1_Actions });
			screenDepthTableModel.addRow(new Object[] { "2 sessions",
					Session_2_People, Session_2_Visits, Session_2_Actions });
			screenDepthTableModel.addRow(new Object[] { "3 sessions",
					Session_3_People, Session_3_Visits, Session_3_Actions });
			screenDepthTableModel.addRow(new Object[] { "4 sessions",
					Session_4_People, Session_4_Visits, Session_4_Actions });
			screenDepthTableModel.addRow(new Object[] { "5 sessions",
					Session_5_People, Session_5_Visits, Session_5_Actions });
			screenDepthTableModel.addRow(new Object[] { "6-10 sessions",
					Session_6to10_People, Session_6to10_Visits,
					Session_6to10_Actions });
			screenDepthTableModel.addRow(new Object[] { "11-15 sessions",
					Session_11to15_People, Session_11to15_Visits,
					Session_11to15_Actions });
			screenDepthTableModel.addRow(new Object[] { "16-20 sessions",
					Session_16to20_People, Session_16to20_Visits,
					Session_16to20_Actions });
			screenDepthTableModel.addRow(new Object[] { "20+ sessions",
					Session_20Plus_People, Session_20Plus_Visits,
					Session_20Plus_Actions });
			// }
			int totalPeople = Session_1_People + Session_2_People
					+ Session_3_People + Session_4_People + Session_5_People
					+ Session_6to10_People + Session_11to15_People
					+ Session_16to20_People + Session_20Plus_People;

			int totalVisits = Session_1_Visits + Session_2_Visits
					+ Session_3_Visits + Session_4_Visits + Session_5_Visits
					+ Session_6to10_Visits + Session_11to15_Visits
					+ Session_16to20_Visits + Session_20Plus_Visits;
			int totalActions = Session_1_Actions + Session_2_Actions
					+ Session_3_Actions + Session_4_Actions + Session_5_Actions
					+ Session_6to10_Actions + Session_11to15_Actions
					+ Session_16to20_Actions + Session_20Plus_Actions;
			
			screenDepthTableModel.addRow(new Object[] { "",
					this.noOfPeople + " People", totalVisits + " Visits",
					totalActions + " Actions" });

			addTableView(screenDepthTableModel, 3, "People", "Visits",
					"Actions");

		} else if (selectedReportMenu.compareToIgnoreCase("Recency") == 0) {
			this.lblreportTitle.setText("Recency");

			reportData = rpc.getRecencytReportInformationController(gateway,
					project, reportForAllGateways, reportForAllProjects,
					constantDuration);

			int Recency_1_People = 0, Recency_1_Visits = 0, Recency_1_Actions = 0;
			int Recency_2_People = 0, Recency_2_Visits = 0, Recency_2_Actions = 0;
			int Recency_3_People = 0, Recency_3_Visits = 0, Recency_3_Actions = 0;
			int Recency_4_People = 0, Recency_4_Visits = 0, Recency_4_Actions = 0;
			int Recency_5_People = 0, Recency_5_Visits = 0, Recency_5_Actions = 0;
			int Recency_6to10_People = 0, Recency_6to10_Visits = 0, Recency_6to10_Actions = 0;
			int Recency_11to15_People = 0, Recency_11to15_Visits = 0, Recency_11to15_Actions = 0;
			int Recency_16to20_People = 0, Recency_16to20_Visits = 0, Recency_16to20_Actions = 0;
			int Recency_20Plus_People = 0, Recency_20Plus_Visits = 0, Recency_20Plus_Actions = 0;

			String series1 = "People";
			String series2 = "Visits";
			String series3 = "Actions";

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			int size = 0;

			size = reportData.getRowCount();

			int tempVal;

			for (int i = 0; i < size; i++) {
				tempVal = (int) Float.parseFloat(reportData.getValueAt(i,
						"Days").toString());

				// if(tempVal == 1 || tempVal == 0){
				if (tempVal <= 1) {
					Recency_1_People = Recency_1_People + 1;
					Recency_1_Actions = Recency_1_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_1_Visits = Recency_1_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 2) {
					Recency_2_People = Recency_2_People + 1;
					Recency_2_Actions = Recency_2_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_2_Visits = Recency_2_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 3) {
					Recency_3_People = Recency_3_People + 1;
					Recency_3_Actions = Recency_3_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_3_Visits = Recency_3_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 4) {
					Recency_4_People = Recency_4_People + 1;
					Recency_4_Actions = Recency_4_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_4_Visits = Recency_4_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal == 5) {
					Recency_5_People = Recency_5_People + 1;
					Recency_5_Actions = Recency_5_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_5_Visits = Recency_5_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal >= 6 && tempVal <= 10) {
					Recency_6to10_People = Recency_6to10_People + 1;
					Recency_6to10_Actions = Recency_6to10_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_6to10_Visits = Recency_6to10_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal >= 11 && tempVal <= 15) {
					Recency_11to15_People = Recency_11to15_People + 1;
					Recency_11to15_Actions = Recency_11to15_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_11to15_Visits = Recency_11to15_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal >= 16 && tempVal <= 20) {
					Recency_16to20_People = Recency_16to20_People + 1;
					Recency_16to20_Actions = Recency_16to20_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_16to20_Visits = Recency_16to20_Visits
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Visits").toString());
				}
				if (tempVal > 20) {
					Recency_20Plus_People = Recency_20Plus_People + 1;
					Recency_20Plus_Actions = Recency_20Plus_Actions
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"Actions").toString())
							+ (int) Float.parseFloat(reportData.getValueAt(i,
									"noOfScreens").toString());
					String testing = reportData.getValueAt(i, "Visits")
							.toString();
					Recency_20Plus_Visits = Recency_20Plus_Visits
							+ (int) Float.parseFloat(testing);
				}

			}

			// Data for bar chart

			dataset.addValue(Recency_1_People, series1, "1 day");
			dataset.addValue(Recency_1_Visits, series2, "1 day");
			dataset.addValue(Recency_1_Actions, series3, "1 day");

			dataset.addValue(Recency_2_People, series1, "2 days");
			dataset.addValue(Recency_2_Visits, series2, "2 days");
			dataset.addValue(Recency_2_Actions, series3, "2 days");

			dataset.addValue(Recency_3_People, series1, "3 days");
			dataset.addValue(Recency_3_Visits, series2, "3 days");
			dataset.addValue(Recency_3_Actions, series3, "3 days");

			dataset.addValue(Recency_4_People, series1, "4 days");
			dataset.addValue(Recency_4_Visits, series2, "4 days");
			dataset.addValue(Recency_4_Actions, series3, "4 days");

			dataset.addValue(Recency_5_People, series1, "5 days");
			dataset.addValue(Recency_5_Visits, series2, "5 days");
			dataset.addValue(Recency_5_Actions, series3, "5 days");

			dataset.addValue(Recency_6to10_People, series1, "6-10 days");
			dataset.addValue(Recency_6to10_Visits, series2, "6-10 days");
			dataset.addValue(Recency_6to10_Actions, series3, "6-10 days");

			dataset.addValue(Recency_11to15_People, series1, "11-15 days");
			dataset.addValue(Recency_11to15_Visits, series2, "11-15 days");
			dataset.addValue(Recency_11to15_Actions, series3, "11-15 days");

			dataset.addValue(Recency_16to20_People, series1, "16-20 days");
			dataset.addValue(Recency_16to20_Visits, series2, "16-20 days");
			dataset.addValue(Recency_16to20_Actions, series3, "16-20 days");

			dataset.addValue(Recency_20Plus_People, series1, "20+ days");
			dataset.addValue(Recency_20Plus_Visits, series2, "20+ days");
			dataset.addValue(Recency_20Plus_Actions, series3, "20+ days");

			JFreeChart chart = ChartFactory.createBarChart("", // chart title
					"", // domain axis label
					"", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips?
					false // URLs?
					);

			applyBarChartFormat(chart);

			this._reportChartPanel.setChart(chart);

			// Data for table

			DefaultTableModel screenDepthTableModel = new DefaultTableModel();
			screenDepthTableModel
					.setColumnIdentifiers(new Object[] {
							"Days Since Last Session", " People", "Visits",
							"Actions" });

			if (size > 0) {
				screenDepthTableModel
						.addRow(new Object[] { "1 day", Recency_1_People,
								Recency_1_Visits, Recency_1_Actions });
				screenDepthTableModel
						.addRow(new Object[] { "2 days", Recency_2_People,
								Recency_2_Visits, Recency_2_Actions });
				screenDepthTableModel
						.addRow(new Object[] { "3 days", Recency_3_People,
								Recency_3_Visits, Recency_3_Actions });
				screenDepthTableModel
						.addRow(new Object[] { "4 days", Recency_4_People,
								Recency_4_Visits, Recency_4_Actions });
				screenDepthTableModel
						.addRow(new Object[] { "5 days", Recency_5_People,
								Recency_5_Visits, Recency_5_Actions });
				screenDepthTableModel.addRow(new Object[] { "6-10 days",
						Recency_6to10_People, Recency_6to10_Visits,
						Recency_6to10_Actions });
				screenDepthTableModel.addRow(new Object[] { "11-15 days",
						Recency_11to15_People, Recency_11to15_Visits,
						Recency_11to15_Actions });
				screenDepthTableModel.addRow(new Object[] { "16-20 days",
						Recency_16to20_People, Recency_16to20_Visits,
						Recency_16to20_Actions });
				screenDepthTableModel.addRow(new Object[] { "20+ days",
						Recency_20Plus_People, Recency_20Plus_Visits,
						Recency_20Plus_Actions });
			}
			int totalPeople = Recency_1_People + Recency_2_People
					+ Recency_3_People + Recency_4_People + Recency_5_People
					+ Recency_6to10_People + Recency_11to15_People
					+ Recency_16to20_People + Recency_20Plus_People;

			int totalVisits = Recency_1_Visits + Recency_2_Visits
					+ Recency_3_Visits + Recency_4_Visits + Recency_5_Visits
					+ Recency_6to10_Visits + Recency_11to15_Visits
					+ Recency_16to20_Visits + Recency_20Plus_Visits;
			int totalActions = Recency_1_Actions + Recency_2_Actions
					+ Recency_3_Actions + Recency_4_Actions + Recency_5_Actions
					+ Recency_6to10_Actions + Recency_11to15_Actions
					+ Recency_16to20_Actions + Recency_20Plus_Actions;
			screenDepthTableModel.addRow(new Object[] { "",
					totalPeople + " People", totalVisits + " Visits",
					totalActions + " Actions" });

			addTableView(screenDepthTableModel, 3, "People", "Visits",
					"Actions");

		} else if (selectedReportMenu.compareToIgnoreCase("Active Users") == 0) {
			// variables for total count of active users at bottom of table.

			int oneDayActiveUsers = 0;
			int sevenDayActiveUsers = 0;
			int fourteenDayActiveUsers = 0;
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			ActiveUsersInfo aInfo = rpc.getActiveUsersCountsOnController(project, reportForAllProjects, gateway, reportForAllGateways, constantDuration);

			if (aInfo != null) {
				oneDayActiveUsers = aInfo.getOneDayActiveUsers();
				sevenDayActiveUsers = aInfo.getSevenDayActiveUsers();
				fourteenDayActiveUsers = aInfo.getFourteenDayActiveUsers();
			}

			this.lblreportTitle.setText("Active Users");
			reportData = rpc.getActiveUserDataReportGraphController(this.dataSource,
					constantDuration, this.gateway, this.project, this.reportForAllGateways, this.reportForAllProjects);// (
																				// Constants.LAST_THIRTY_DAYS,
																				// this.project,
																				// this.reportForAllProjects);
			DefaultTableModel platformsData = new DefaultTableModel(0, 3);
			platformsData.setColumnIdentifiers(new Object[] { "By Date",
					"1 Day active Users", "7 Day active Users",
					"14 Day active Users" });
			SimpleDateFormat sdf = new SimpleDateFormat("EE, MMM dd, yyyy");
			SimpleDateFormat df = new SimpleDateFormat(
					"EE MMM dd HH:mm:ss z yyyy");
			SimpleDateFormat dfGraph = new SimpleDateFormat("EE MMM dd");

			if (reportData != null) {
				noOfRecords = reportData.getRowCount();

				String series1 = "1 Day";
				String series2 = "7 Day";
				String series3 = "14 Day";

				String category = "";
				String categoryGraph = "";
				JFreeChart _lineChart;
				Date xDate = null;
				float totaloneDayActive = 0;
				float totalfourteenDayActive = 0f;
				float totalsevenDayActive = 0;
				int startVal = 0;
				if (constantDuration == Constants.LAST_365_DAYS) {
					Integer curMonth = Calendar.getInstance().get(
							Calendar.MONTH);

					if (curMonth == 11) {
						curMonth = 1;
					} else {
						curMonth = curMonth + 2;
					}

					startVal = Constants.binarySearchOnDataset(4, curMonth, reportData);
					
					if (startVal < 0) {
						startVal = 0;
					}

					for (int i = startVal - 1; i >= 0; i--) {
						category = "";
						float oneDayActive = 0;
						float fourteenDayActive = 0f;
						float sevenDayActive = 0;

						if (reportData.getValueAt(i, 0) != null)
							category = reportData.getValueAt(i, 0).toString();

						if (reportData.getValueAt(i, 1) != null) {
							oneDayActive = Float.parseFloat(reportData
									.getValueAt(i, 1).toString());
						}
						if (reportData.getValueAt(i, 2) != null) {
							sevenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 2).toString());
						}
						if (reportData.getValueAt(i, 3) != null) {
							fourteenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 3).toString());
						}

						totaloneDayActive = totaloneDayActive + oneDayActive;
						totalfourteenDayActive = totalfourteenDayActive
								+ fourteenDayActive;
						totalsevenDayActive = totalsevenDayActive
								+ sevenDayActive;
						platformsData.addRow(new Object[] { category,
								(int) oneDayActive, (int) sevenDayActive,
								(int) fourteenDayActive });

					}

					for (int i = noOfRecords - 1; i >= startVal; i--) {

						category = "";
						float oneDayActive = 0;
						float fourteenDayActive = 0f;
						float sevenDayActive = 0;

						if (reportData.getValueAt(i, 0) != null)
							category = reportData.getValueAt(i, 0).toString();

						if (reportData.getValueAt(i, 1) != null) {
							oneDayActive = Float.parseFloat(reportData
									.getValueAt(i, 1).toString());
						}
						if (reportData.getValueAt(i, 2) != null) {
							sevenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 2).toString());
						}
						if (reportData.getValueAt(i, 3) != null) {
							fourteenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 3).toString());
						}

						totaloneDayActive = totaloneDayActive + oneDayActive;
						totalfourteenDayActive = totalfourteenDayActive
								+ fourteenDayActive;
						totalsevenDayActive = totalsevenDayActive
								+ sevenDayActive;
						platformsData.addRow(new Object[] { category,
								(int) oneDayActive, (int) sevenDayActive,
								(int) fourteenDayActive });
					}

				}

				else {

					for (int i = noOfRecords - 1; i >= 0; i--) {
						category = "";
						float oneDayActive = 0;
						float fourteenDayActive = 0f;
						float sevenDayActive = 0;

						if (constantDuration != Constants.LAST_365_DAYS
								&& constantDuration != Constants.TODAY
								&& constantDuration != Constants.YESTERDAY
								&& constantDuration != Constants.LAST_YEAR
								&& constantDuration != Constants.THIS_YEAR) {
							if (reportData.getValueAt(i, 0) != null) {
								String dateVal = reportData.getValueAt(i, 0)
										.toString();
								try {
									xDate = df.parse(dateVal);
									category = sdf.format(xDate);
								} catch (ParseException e) {
									e.printStackTrace();
								}

							}
						} else {
							if (reportData.getValueAt(i, 0) != null)
								category = reportData.getValueAt(i, 0)
										.toString();
						}
						if (reportData.getValueAt(i, 1) != null) {
							oneDayActive = Float.parseFloat(reportData
									.getValueAt(i, 1).toString());
						}
						if (reportData.getValueAt(i, 2) != null) {
							sevenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 2).toString());
						}
						if (reportData.getValueAt(i, 3) != null) {
							fourteenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 3).toString());
						}

						totaloneDayActive = totaloneDayActive + oneDayActive;
						totalfourteenDayActive = totalfourteenDayActive
								+ fourteenDayActive;
						totalsevenDayActive = totalsevenDayActive
								+ sevenDayActive;
						platformsData.addRow(new Object[] { category,
								(int) oneDayActive, (int) sevenDayActive,
								(int) fourteenDayActive });
					}
				}
				platformsData.addRow(new Object[] { noOfRecords + " Result(s)",
						oneDayActiveUsers + " People",
						sevenDayActiveUsers + " People",
						fourteenDayActiveUsers + " People" });

				// create line chart data in ascending order
				if (constantDuration == Constants.LAST_365_DAYS) {
					for (int i = startVal; i < noOfRecords; i++) {
						categoryGraph = "";
						float oneDayActive = 0;
						float fourteenDayActive = 0f;
						float sevenDayActive = 0;

						if (reportData.getValueAt(i, 0) != null)
							categoryGraph = reportData.getValueAt(i, 0)
									.toString();

						if (reportData.getValueAt(i, 1) != null) {
							oneDayActive = Float.parseFloat(reportData
									.getValueAt(i, 1).toString());
						}
						if (reportData.getValueAt(i, 2) != null) {
							sevenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 2).toString());
						}
						if (reportData.getValueAt(i, 3) != null) {
							fourteenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 3).toString());
						}

						dataset.addValue((int) oneDayActive, series1,
								categoryGraph);
						dataset.addValue((int) sevenDayActive, series2,
								categoryGraph);
						dataset.addValue((int) fourteenDayActive, series3,
								categoryGraph);

					}

					for (int i = 0; i < startVal; i++) {

						categoryGraph = "";
						float oneDayActive = 0;
						float fourteenDayActive = 0f;
						float sevenDayActive = 0;

						if (reportData.getValueAt(i, 0) != null)
							categoryGraph = reportData.getValueAt(i, 0)
									.toString();

						if (reportData.getValueAt(i, 1) != null) {
							oneDayActive = Float.parseFloat(reportData
									.getValueAt(i, 1).toString());
						}
						if (reportData.getValueAt(i, 2) != null) {
							sevenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 2).toString());
						}
						if (reportData.getValueAt(i, 3) != null) {
							fourteenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 3).toString());
						}

						dataset.addValue((int) oneDayActive, series1,
								categoryGraph);
						dataset.addValue((int) sevenDayActive, series2,
								categoryGraph);
						dataset.addValue((int) fourteenDayActive, series3,
								categoryGraph);
					}
				} else {
					for (int i = 0; i < noOfRecords; i++) {
						categoryGraph = "";
						float oneDayActive = 0;
						float fourteenDayActive = 0f;
						float sevenDayActive = 0;

						if (constantDuration != Constants.LAST_365_DAYS
								&& constantDuration != Constants.TODAY
								&& constantDuration != Constants.YESTERDAY
								&& constantDuration != Constants.LAST_YEAR
								&& constantDuration != Constants.THIS_YEAR) {
							if (reportData.getValueAt(i, 0) != null) {
								String dateVal = reportData.getValueAt(i, 0)
										.toString();
								try {
									xDate = df.parse(dateVal);
									categoryGraph = dfGraph.format(xDate);
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}

						} else {
							if (reportData.getValueAt(i, 0) != null)
								categoryGraph = reportData.getValueAt(i, 0)
										.toString();
						}
						if (reportData.getValueAt(i, 1) != null) {
							oneDayActive = Float.parseFloat(reportData
									.getValueAt(i, 1).toString());
						}
						if (reportData.getValueAt(i, 2) != null) {
							sevenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 2).toString());
						}
						if (reportData.getValueAt(i, 3) != null) {
							fourteenDayActive = Float.parseFloat(reportData
									.getValueAt(i, 3).toString());
						}

						dataset.addValue((int) oneDayActive, series1,
								categoryGraph);
						dataset.addValue((int) sevenDayActive, series2,
								categoryGraph);
						dataset.addValue((int) fourteenDayActive, series3,
								categoryGraph);
					}
					// _lineChart = ChartFactory.createLineChart("", "", "",
					// dataset, PlotOrientation.VERTICAL, false, true, true );
				}

			}
			addTableView(platformsData, 3, "1-Day Active Users",
					"7-Day Active Users", "14-Day Active Users");
			createLineChart(dataset);
		}

		// alarms reports
		else if (selectedReportMenu.compareToIgnoreCase("Alarm Summary") == 0) {
			this.lblreportTitle.setText("Alarm Summary");

			// get clear time to populate in upper table

			Dataset _alarmsClearTime = rpc.getAlarmsClearTimeController(constantDuration,this.gateway,
					this.project, this.reportForAllGateways, this.reportForAllProjects);
			// set value in the UI
			// set the value in labels on the UI
			int alarmsClearDSSize;
			String medAlarmsClr = "00:00:00", highAlarmsclr = "00:00:00", criticalAlarmsClr = "00:00:00", lowAlarmsClr = "00:00:00";
			String alarmPriority;
			// SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			int r = 0;
			if (_alarmsClearTime != null) {
				alarmsClearDSSize = _alarmsClearTime.getRowCount();
				for (r = 0; r < alarmsClearDSSize; r++) {
					if (_alarmsClearTime.getValueAt(r, 0) != null) {
						alarmPriority = _alarmsClearTime.getValueAt(r, 0)
								.toString();
						if (alarmPriority.compareToIgnoreCase("Medium") == 0) // medium
						{
							if (_alarmsClearTime.getValueAt(r, 1) != null) {
								medAlarmsClr = _alarmsClearTime
										.getValueAt(r, 1).toString();
							}
						} else if (alarmPriority.compareToIgnoreCase("High") == 0) // high
						{
							if (_alarmsClearTime.getValueAt(r, 1) != null) {
								highAlarmsclr = _alarmsClearTime.getValueAt(r,
										1).toString();
							}
						} else if (alarmPriority
								.compareToIgnoreCase("Critical") == 0) // critical
						{
							if (_alarmsClearTime.getValueAt(r, 1) != null) {
								criticalAlarmsClr = _alarmsClearTime
										.getValueAt(r, 1).toString();
							}
						} else if (alarmPriority.compareToIgnoreCase("Low") == 0) // critical
						{
							if (_alarmsClearTime.getValueAt(r, 1) != null) {
								lowAlarmsClr = _alarmsClearTime
										.getValueAt(r, 1).toString();
							}
						}

					}
				}
			}

			// get ack times
			Dataset _alarmsAckTime = rpc.getAlarmsAckTimeController(constantDuration,this.gateway,
					this.project, this.reportForAllGateways, this.reportForAllProjects);
			// set value in the UI
			// set the value in labels on the UI
			int alarmsAckDSSize;
			String medAlarmsAck = "00:00:00", highAlarmsAck = "00:00:00", criticalAlarmsAck = "00:00:00", lowAlarmsAck = "00:00:00";
			if (_alarmsAckTime != null) {
				alarmsAckDSSize = _alarmsAckTime.getRowCount();
				for (r = 0; r < alarmsAckDSSize; r++) {
					if (_alarmsAckTime.getValueAt(r, 0) != null) {
						alarmPriority = _alarmsAckTime.getValueAt(r, 0)
								.toString();
						if (alarmPriority.compareToIgnoreCase("Medium") == 0) // medium
						{
							if (_alarmsAckTime.getValueAt(r, 1) != null) {
								System.out.println("_alarmsAckTime : med "
										+ _alarmsAckTime.getValueAt(r, 1));
								medAlarmsAck = _alarmsAckTime.getValueAt(r, 1)
										.toString();
							}

						} else if (alarmPriority.compareToIgnoreCase("High") == 0) // high
						{
							if (_alarmsAckTime.getValueAt(r, 1) != null) {
								System.out.println("_alarmsAckTime : high "
										+ _alarmsAckTime.getValueAt(r, 1));
								highAlarmsAck = _alarmsAckTime.getValueAt(r, 1)
										.toString();
							}
						} else if (alarmPriority
								.compareToIgnoreCase("Critical") == 0) // critical
						{
							if (_alarmsAckTime.getValueAt(r, 1) != null) {
								System.out.println("_alarmsAckTime : critical "
										+ _alarmsAckTime.getValueAt(r, 1));
								criticalAlarmsAck = _alarmsAckTime.getValueAt(
										r, 1).toString();
							}
						} else if (alarmPriority.compareToIgnoreCase("Low") == 0) // critical
						{
							if (_alarmsAckTime.getValueAt(r, 1) != null) {
								System.out.println("_alarmsAckTime : low "
										+ _alarmsAckTime.getValueAt(r, 1));
								lowAlarmsAck = _alarmsAckTime.getValueAt(r, 1)
										.toString();
							}
						}

					}
				}
			}
			reportData = rpc.getAlarmsSummaryReportController(gateway, project, reportForAllGateways,
					reportForAllProjects, constantDuration);

			ReportsTable alarmSummaryTable = new ReportsTable();

			DefaultTableModel platformsData = new DefaultTableModel(0, 3);

			platformsData.setColumnIdentifiers(new Object[] { "Gateway Name", "Alarms",
					"Alarm Priority", "Quantity",
					"Average Time to Acknowledge", "Average Time To Clear" });

			int size = 0;

			if (reportData != null) {
				size = reportData.getRowCount();
			}
			String avgTimeAck = "00:00:00";
			String avgTimeClr = "00:00:00";
			float avgTimeAckSeconds = 0;
			float avgTimeClrSeconds = 0;

			int lowSum = 0;
			int mediumSum = 0;
			int highSum = 0;
			int criticalSum = 0;
			int lowRechords = 0;
			int mediumRechords = 0;
			int highRechords = 0;
			int criticalRechords = 0;
			float avgClrSumLow = 0, avgAckSumLow = 0;
			float avgClrSumMedium = 0, avgAckSumMedium = 0;
			float avgClrSumHigh = 0, avgAckSumHigh = 0;
			float avgClrSumCritical = 0, avgAckSumCitical = 0;

			long avgClrSumLowMS = 0, avgAckSumLowMS = 0;
			long avgClrSumMediumMS = 0, avgAckSumMediumMS = 0;
			long avgClrSumHighMS = 0, avgAckSumHighMS = 0;
			long avgClrSumCriticalMS = 0, avgAckSumCiticalMS = 0;
			CenterCellRenderer centerRenderer = new CenterCellRenderer();
			centerRenderer.paddingSize = 10;

			int sumAlarmCount = 0;
			for (int i = 0; i < size; i++) {
				String gatewayName = reportData.getValueAt(i, "gateway_id").toString();
				String alarmName = reportData.getValueAt(i, "alarm_name").toString();
				String priority = reportData.getValueAt(i, "alarm_priority").toString();
				sumAlarmCount = 0;
				sumAlarmCount = (int) Float.parseFloat(reportData.getValueAt(i,	"Quantity").toString());
				avgTimeAck = "00:00:00";
				avgTimeClr = "00:00:00";
				if (reportData.getValueAt(i, "TimeToAck") != null) {
					avgTimeAck = reportData.getValueAt(i, "TimeToAck").toString();
					// avgTimeAck = avgTimeAck.substring(11, 16);
					// avgTimeAck = avgTimeAck.substring(11,19);
				}
				if (reportData.getValueAt(i, "TimetToClr") != null) {
					avgTimeClr = reportData.getValueAt(i, "TimetToClr").toString();
					// avgTimeClr = avgTimeClr.substring(11, 16);
					// avgTimeClr = avgTimeClr.substring(11,19);
				}
				if (reportData.getValueAt(i, "timeToAckSeconds") != null) {
					avgTimeAckSeconds = Float.parseFloat(reportData.getValueAt(	i, "timeToAckSeconds").toString());
				}
				if (reportData.getValueAt(i, "timeToClrSeconds") != null) {
					avgTimeClrSeconds = Float.parseFloat(reportData.getValueAt(	i, "timeToClrSeconds").toString());
				}
				if (priority.compareToIgnoreCase("Low") == 0) {
					lowSum = lowSum + sumAlarmCount;
					lowRechords++;
					avgClrSumLow = avgClrSumLow + avgTimeClrSeconds;
					avgAckSumLow = avgAckSumLow + avgTimeAckSeconds;
				}
				if (priority.compareToIgnoreCase("Medium") == 0) {
					mediumSum = mediumSum + sumAlarmCount;
					mediumRechords++;
					avgClrSumMedium = avgClrSumMedium + avgTimeClrSeconds;
					avgAckSumMedium = avgAckSumMedium + avgTimeAckSeconds;
				}
				if (priority.compareToIgnoreCase("High") == 0) {
					highSum = highSum + sumAlarmCount;
					highRechords++;
					avgClrSumHigh = avgClrSumHigh + avgTimeClrSeconds;
					avgAckSumHigh = avgAckSumHigh + avgTimeAckSeconds;
				}
				if (priority.compareToIgnoreCase("Critical") == 0) {
					criticalSum = criticalSum + sumAlarmCount;
					criticalRechords++;
					avgClrSumCritical = avgClrSumCritical + avgTimeClrSeconds;
					avgAckSumCitical = avgAckSumCitical + avgTimeAckSeconds;
				}

				platformsData.addRow(new Object[] { gatewayName,alarmName, priority,
						sumAlarmCount, avgTimeAck, avgTimeClr });
			}

			// Upper Table
			JTable upperTable = new JTable();

			AlarmSummaryUpperTableHeaderRenderer _upperHRenderer = new AlarmSummaryUpperTableHeaderRenderer();
			DefaultTableModel totalAvgData = new DefaultTableModel(0, 5);

			// get avg times values in milliseconds

			totalAvgData.setColumnIdentifiers(new Object[] { "", "", "","Average Time to Acknowledge", "Average Time To Clear" });

			if (lowRechords > 0) {
				totalAvgData.addRow(new Object[] { "Total Low Priority Alarms",	"Low", lowSum, lowAlarmsAck, lowAlarmsClr });
			} else {
				totalAvgData.addRow(new Object[] { "Total Low Priority Alarms",	"Low", lowSum, "00:00:00", "00:00:00" });
			}

			if (mediumRechords > 0) {
				totalAvgData.addRow(new Object[] {"Total Medium Priority Alarms", "Medium", mediumSum, medAlarmsAck, medAlarmsClr });
			} else {
				totalAvgData.addRow(new Object[] {"Total Medium Priority Alarms", "Medium", mediumSum, "00:00:00", "00:00:00" });
			}
			if (highRechords > 0) {
				totalAvgData.addRow(new Object[] {"Total High Priority Alarms", "High", highSum, highAlarmsAck, highAlarmsclr });
			} else {
				totalAvgData.addRow(new Object[] {"Total High Priority Alarms", "High", highSum, "00:00:00", "00:00:00" });
			}
			if (criticalRechords > 0) {
				totalAvgData.addRow(new Object[] {"Total Critical Priority Alarms", "Critical", criticalSum, criticalAlarmsAck, criticalAlarmsClr });
			} else {
				totalAvgData.addRow(new Object[] {"Total Critical Priority Alarms", "Critical",	criticalSum, "00:00:00", "00:00:00" });
			}

			upperTable.setPreferredScrollableViewportSize(new Dimension(1720, 140));
			upperTable.setModel(totalAvgData);

			upperTable.setVisible(true);
			upperTable.setShowGrid(false);
			upperTable.setRowHeight(32);
			upperTable.setBackground(Color.WHITE);

			upperTable.getColumnModel().getColumn(0).setPreferredWidth(600);
			upperTable.getColumnModel().getColumn(1).setPreferredWidth(100);
			upperTable.getColumnModel().getColumn(2).setPreferredWidth(100);
			upperTable.getColumnModel().getColumn(3).setPreferredWidth(100);
			upperTable.getColumnModel().getColumn(4).setPreferredWidth(100);
			
			System.out.println("preffered width");
			upperTable.getColumnModel().getColumn(0).setHeaderRenderer(_upperHRenderer);
			upperTable.getColumnModel().getColumn(1).setHeaderRenderer(_upperHRenderer);
			upperTable.getColumnModel().getColumn(2).setHeaderRenderer(_upperHRenderer);
			upperTable.getColumnModel().getColumn(3).setHeaderRenderer(_upperHRenderer);
			upperTable.getColumnModel().getColumn(4).setHeaderRenderer(_upperHRenderer);
			upperTable.setEnabled(false);

			alarmSummaryTable.setPreferredScrollableViewportSize(this.reportDetails.getPreferredSize());
			alarmSummaryTable.setModel(platformsData);
			alarmSummaryTable.setVisible(true);
			alarmSummaryTable.setEnabled(false);

			alarmSummaryTable.getTableHeader().setBorder(
					BorderFactory.createMatteBorder(1, 0, 1, 0,
							Color.LIGHT_GRAY));

			LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
			leftRenderer.paddingSize = 10;
			alarmSummaryTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
			alarmSummaryTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
			alarmSummaryTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
			alarmSummaryTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
			alarmSummaryTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
			alarmSummaryTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

			upperTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
			upperTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
			upperTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
			upperTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
			upperTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
		
			
			alarmSummaryTable.getColumnModel().getColumn(0).setPreferredWidth(250);
			alarmSummaryTable.getColumnModel().getColumn(1).setPreferredWidth(200);
			alarmSummaryTable.getColumnModel().getColumn(2).setPreferredWidth(150);
			alarmSummaryTable.getColumnModel().getColumn(3).setPreferredWidth(150);
			alarmSummaryTable.getColumnModel().getColumn(4).setPreferredWidth(150);
			alarmSummaryTable.getColumnModel().getColumn(5).setPreferredWidth(150);
		
			CenterHeaderCellRenderer centerHRenderer = new CenterHeaderCellRenderer();
			alarmSummaryTable.getColumnModel().getColumn(0).setHeaderRenderer(new LeftHeaderCellRenderer());
			alarmSummaryTable.getColumnModel().getColumn(1).setHeaderRenderer(centerHRenderer);
			alarmSummaryTable.getColumnModel().getColumn(2).setHeaderRenderer(centerHRenderer);
			alarmSummaryTable.getColumnModel().getColumn(3).setHeaderRenderer(centerHRenderer);
			alarmSummaryTable.getColumnModel().getColumn(4).setHeaderRenderer(centerHRenderer);
			alarmSummaryTable.getColumnModel().getColumn(5).setHeaderRenderer(centerHRenderer);
		
			
			this.reportGraph.setBackground(Color.WHITE);
			this.upperScroll.setViewportView(upperTable);
			// this.reportDetails.setViewportView(alarmSummaryTable);
			alarmSummaryPanel = new AlarmSummaryReportPanelController(alarmSummaryTable,rpc, constantDuration, reportForAllProjects, project,reportForAllGateways, gateway);
			// AlarmSummaryReportPanel(ReportsTable reportTable, ModuleRPC
			// rpc,int duration,boolean allProjects, String projecName)
			// rpc.getAlarmsSummaryReport(project, reportForAllProjects,
			// constantDuration);
			this.reportDetails.setViewportView(alarmSummaryPanel);

			alarmSummaryTable.setAutoCreateRowSorter(false);
			currentTable = alarmSummaryTable;

			if (columnSelected != -1) {

				// TableRowSorter<TableModel> sorter = new
				// TableRowSorter<TableModel>(alarmSummaryTable.getModel());
				// alarmSummaryTable.setRowSorter(sorter);
				// List<RowSorter.SortKey> sortKeys = new ArrayList<SortKey>();
				//
				// sortKeys.add(new RowSorter.SortKey(columnSelected,
				// SortOrder.ASCENDING));
				//
				// sorter.setSortKeys(sortKeys);
				// sorter.sort();

				SortAlarmTable(true);

			}

			// add the header click listener

			JTableHeader header = alarmSummaryTable.getTableHeader();

			header.addMouseListener(this);
		}

		revalidate();
		repaint();
	}

	

	void applyBarChartFormat(JFreeChart chart) {

		// chart.setBackgroundPaint(Color.white);
		// chart.setBorderVisible(false);
		//
		// // get a reference to the plot for further customization...
		// CategoryPlot plot = chart.getCategoryPlot();
		// plot.setBackgroundPaint(Color.white);
		// plot.setDomainGridlinePaint(Color.white);
		// plot.setRangeGridlinePaint(Constants.COLOR_BLUE_LABEL);
		// plot.setOutlineVisible(false);
		// // set the range axis to display integers only...
		//
		// CategoryAxis axis = plot.getDomainAxis();
		// ValueAxis axis1 = plot.getRangeAxis();
		//
		// Font font3 = new Font("Tahoma", Font.PLAIN, 10);
		// // plot.getDomainAxis().setLabelFont(font3);
		// // plot.getRangeAxis().setLabelFont(font3);
		// //
		// axis.setTickLabelFont(font3);
		// axis1.setTickLabelFont(font3);
		//
		// NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setAutoRangeIncludesZero(true);
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// rangeAxis.setAxisLineVisible(false);
		// // disable bar outlines...
		// BarRenderer renderer = (BarRenderer) plot.getRenderer();
		// renderer.setMaximumBarWidth(.04);
		// renderer.setShadowVisible(false);
		// renderer.setDrawBarOutline(false);
		// ((BarRenderer)plot.getRenderer()).setBarPainter(new
		// StandardBarPainter());
		// renderer.setItemMargin(0.0);
		// renderer.setSeriesPaint(0, Constants.COLOR_BLUE_LABEL);
		// renderer.setSeriesPaint(1, new Color(253,184,40));
		// renderer.setSeriesPaint(2, Constants.COLOR_SLIDE_PANE_GREEN);
		//
		//
		// CategoryAxis domainAxis = plot.getDomainAxis();
		// domainAxis.setCategoryMargin(0.2);
		//
		// domainAxis.setCategoryLabelPositions(
		// CategoryLabelPositions.STANDARD);

		Font chartFont = new Font("Arial", Font.PLAIN, 10);
		chart.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		chart.setBorderVisible(false);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		// plot.setDomainGridlinePaint(Color.white);
		((BarRenderer) plot.getRenderer())
				.setBarPainter(new StandardBarPainter());
		plot.setRangeGridlinePaint(Constants.COLOR_GRADIENT_LINE);
		plot.setOutlineVisible(false);
		 plot.getRenderer().setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
	        	    "{0}, {1}, {2}", NumberFormat.getInstance()));
		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAxisLineVisible(false);
		rangeAxis.setAutoRangeIncludesZero(false);
		// disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);
		renderer.setMaximumBarWidth(0.02);
		renderer.setItemMargin(0.0);

		renderer.setSeriesPaint(0, Constants.COLOR_BLUE_LABEL);
		renderer.setSeriesPaint(1, new Color(249, 166, 43));
		renderer.setSeriesPaint(2, Constants.COLOR_SLIDE_PANE_GREEN);
		CategoryAxis domainAxis = plot.getDomainAxis();
		// domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

		CategoryLabelPositions p = domainAxis.getCategoryLabelPositions();

		CategoryLabelPosition left = new CategoryLabelPosition(
				RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT,
				TextAnchor.CENTER_LEFT, 0.0, CategoryLabelWidthType.RANGE,
				0.20f // Assign 70% of space for category labels
		);

		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.replaceLeftPosition(p, left));
		domainAxis.setTickLabelFont(chartFont);
		// rangeAxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN,
		// 11));

		rangeAxis.setTickLabelFont(chartFont);
		rangeAxis.setTickLabelPaint(Color.BLACK);

		// domainAxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN,
		// 11));
		domainAxis.setTickLabelPaint(Color.BLACK);
		// domainAxis.setCategoryMargin(0.0);

	}

	void createLineChart(DefaultCategoryDataset _data) {

		/*
		 * 
		 * CategoryPlot _plot = (CategoryPlot) _lineChart.getPlot();
		 * _plot.setBackgroundImageAlpha(0.0f); _plot.setBackgroundPaint(new
		 * Color(0xFF, 0xFF, 0xFF, 0));
		 * _plot.setRangeGridlinePaint(Constants.COLOR_COMBO_BACKGROUND);
		 * _plot.setRangeGridlinesVisible(true);
		 * _plot.setDomainGridlinesVisible(false);
		 * _plot.setOutlineVisible(false); //to set the line colours
		 * LineAndShapeRenderer renderer = (LineAndShapeRenderer)
		 * _plot.getRenderer();
		 * renderer.setSeriesPaint(0,Constants.COLOR_BLUE_LABEL);
		 * renderer.setSeriesPaint(1,Constants.COLOR_SLIDE_PANE_GREEN);
		 * renderer.setSeriesPaint(2,Constants.COLOR_ORANGE_TEXT);
		 * //renderer.setSeriesPaint(1,new Color(249,166,43));
		 * renderer.setDrawOutlines(false);
		 * 
		 * Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 5.0f, 5.0f);
		 * 
		 * //to set the series marker shape to circle , default is square
		 * renderer.setSeriesShape(0, circle); renderer.setSeriesShape(1,
		 * circle); renderer.setSeriesShape(2, circle);
		 * renderer.setBaseShapesVisible(true);
		 * 
		 * 
		 * NumberAxis rangeAxis = (NumberAxis) _plot.getRangeAxis();
		 * rangeAxis.setVisible(true);
		 * rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		 * rangeAxis.setAutoRangeIncludesZero(true);
		 * rangeAxis.setAxisLineVisible(false);
		 * //rangeAxis.setAxisLinePaint(Constants.COLOR_GREY_LABEL);
		 * rangeAxis.setAxisLineVisible(false);
		 * 
		 * final CategoryAxis domainAxis = _plot.getDomainAxis();
		 * domainAxis.setVisible(true);
		 * domainAxis.setTickLabelPaint(Constants.COLOR_COMBO_BACKGROUND);
		 * domainAxis.setAxisLineVisible(false);
		 * 
		 * domainAxis.setMaximumCategoryLabelWidthRatio(1);
		 * 
		 * ChartPanel _lineChartPanel = new ChartPanel(_lineChart);
		 * 
		 * _lineChartPanel.setBackground(Color.DARK_GRAY);
		 * _lineChartPanel.setOpaque(false);
		 * _lineChartPanel.setPreferredSize(new Dimension(700,150));
		 * this.reportGraph.add(_lineChartPanel, BorderLayout.CENTER);
		 */

		JFreeChart _lineChart;
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		CategoryAxis domainAxis1 = null;

		Font chartFont = new Font("Arial", Font.PLAIN, 10);
		// new CategoryAxis("Category");
		int noOfRecords = _data.getRowCount();
		if (noOfRecords <= 30) {
			domainAxis1 = new CategoryAxisSkipLabels(3);
		} else if (noOfRecords > 30 && noOfRecords <= 50) {
			domainAxis1 = new CategoryAxisSkipLabels(5);
		} else if (noOfRecords > 50) {
			domainAxis1 = new CategoryAxisSkipLabels(7);
		}

		domainAxis1.setTickMarksVisible(true);

		// domainAxis1.setCategoryLabelPositions(new CategoryLabelPositions(new
		// CategoryLabelPosition(RectangleAnchor.BOTTOM,
		// TextBlockAnchor.CENTER_LEFT
		// ), new CategoryLabelPosition(
		// RectangleAnchor.TOP, TextBlockAnchor.TOP_LEFT
		// ),new CategoryLabelPosition(
		// RectangleAnchor.RIGHT, TextBlockAnchor.CENTER_RIGHT,
		// CategoryLabelWidthType.RANGE, 0.30f
		// ), new CategoryLabelPosition(
		// RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT,
		// CategoryLabelWidthType.RANGE, 0.30f
		// ) ));

		// domainAxis1.setCategoryLabelPositionOffset(-10);
		CategoryLabelPositions p = domainAxis1.getCategoryLabelPositions();

		CategoryLabelPosition left = new CategoryLabelPosition(
				RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT,
				TextAnchor.CENTER_LEFT, 0.0, CategoryLabelWidthType.RANGE,
				0.20f // Assign 70% of space for category labels
		);

		domainAxis1.setCategoryLabelPositions(CategoryLabelPositions
				.replaceLeftPosition(p, left));

		domainAxis1.setCategoryMargin(0.0);
		domainAxis1.setLowerMargin(0.0);
		domainAxis1.setTickMarksVisible(true);
		domainAxis1.setTickLabelInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		// ((CategoryAxisSkipLabels)domainAxis1).setDisplaySkippedTickMarks(false);
		domainAxis1.setVisible(true);

		domainAxis1.setTickLabelFont(chartFont);
		// domainAxis1.setTickLabelFont(new Font(Font.DIALOG, Font.PLAIN, 11));
		// domainAxis.setTickLabelPaint(Constants.COLOR_PLOT_LABEL);

		domainAxis1.setAxisLineVisible(false);
		NumberAxis rangeAxis1 = new NumberAxis("");

		CategoryPlot plot = new CategoryPlot(_data, domainAxis1, rangeAxis1,
				renderer);
		plot.setAxisOffset(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		_lineChart = new JFreeChart(plot);
		_lineChart.setBackgroundImageAlpha(0.0f);
		_lineChart.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		_lineChart.setBorderVisible(false);
		_lineChart.removeLegend();
		CategoryPlot _plot = (CategoryPlot) _lineChart.getPlot();
		// _plot.setAxisOffset(new RectangleInsets(0,2,0,0));
		_plot.setBackgroundImageAlpha(0.0f);
		_plot.setBackgroundPaint(new Color(0xFF, 0xFF, 0xFF, 0));
		_plot.setRangeGridlinePaint(Constants.COLOR_GRADIENT_LINE);
		// _plot.setDomainGridlinePaint(Constants.COLOR_COMBO_BACKGROUND);
		_plot.setRangeGridlinesVisible(true);
		_plot.setDomainGridlinesVisible(false);
		_plot.setOutlineVisible(false);
		// to set the line colours
		renderer = (LineAndShapeRenderer) _plot.getRenderer();
		renderer.setSeriesPaint(0, Constants.COLOR_BLUE_LABEL);
		renderer.setSeriesPaint(2, Constants.COLOR_SLIDE_PANE_GREEN);
		renderer.setSeriesPaint(1, new Color(249, 166, 43));
		renderer.setDrawOutlines(false);
		renderer.setSeriesToolTipGenerator(0,
				new StandardCategoryToolTipGenerator());
		Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 5.0f, 5.0f);

		// to set the series marker shape to circle , default is square
		renderer.setSeriesShape(0, circle);
		renderer.setSeriesShape(1, circle);
		renderer.setSeriesShape(2, circle);
		renderer.setBaseShapesVisible(true);

		NumberAxis rangeAxis = (NumberAxis) _plot.getRangeAxis();
		rangeAxis.setVisible(true);
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRangeIncludesZero(true);
		rangeAxis.setAxisLineVisible(false);

		// rangeAxis.setAxisLinePaint(Constants.COLOR_GREY_LABEL);
		rangeAxis.setAxisLineVisible(false);
		// rangeAxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN,
		// 11));
		rangeAxis.setTickLabelFont(chartFont);
		// rangeAxis.setTickLabelPaint(Constants.COLOR_PLOT_LABEL);

		// HorizontalCategoryAxis _hcat = _plot.getDomainAxis();

		this._reportChartPanel.setChart(_lineChart);

	}

	void addTableView(DefaultTableModel tableData, int noOfColumns, String colHeader1, String colHeader2, String colHeader3)
	{
		 //to hold the reports data.
		ImageIcon blueIcon;
		ImageIcon yellowIcon;
		ImageIcon greenIcon;
		IA_txtIconTableHeaderRenderer headerRenderer; 
		
		if(tableData != null && tableData.getRowCount() > 0)
		{
			//initialize the image icons to be displayed as column headers on the reports.
			blueIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Blue.png"));
			blueIcon = new ImageIcon(blueIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
			yellowIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Yellow.png"));
			yellowIcon = new ImageIcon(yellowIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
			greenIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Green.png"));
			greenIcon = new ImageIcon(greenIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
				
			//define the reports table header cell renderer that displays text + icon
			headerRenderer = new IA_txtIconTableHeaderRenderer();
			ReportsTable _table; //for reports other than Alarm reports.
			_table = new ReportsTable(tableData);
	
			
			_table.setPreferredScrollableViewportSize(this.reportDetails.getPreferredSize());
			//_table.setModel(tableData);
			_table.setVisible(true);
		     
		    _table.getTableHeader().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));
			 
				RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
				rightRenderer.paddingSize = 10;
				
				LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
				leftRenderer.paddingSize = 10;
				
				
				_table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
				_table.getColumnModel().getColumn(1).setCellRenderer( rightRenderer );
				_table.getColumnModel().getColumn(2).setCellRenderer( rightRenderer );
				
				//adjust the total widths of the columns
				if(noOfColumns == 2)
				{
					_table.getColumnModel().getColumn(0).setPreferredWidth(800);
				}
				else if(noOfColumns == 3)
				{
					_table.getColumnModel().getColumn(0).setPreferredWidth(700);
				}
				
				_table.getColumnModel().getColumn(1).setPreferredWidth(100);
				_table.getColumnModel().getColumn(2).setPreferredWidth(100);
				
				//create sorting ability
				_table.setAutoCreateRowSorter(false);
				
				//apply renderer for teh header
				_table.getColumnModel().getColumn(0).setHeaderRenderer(new LeftHeaderCellRenderer());
				_table.getColumnModel().getColumn(1).setHeaderRenderer(headerRenderer );
				_table.getColumnModel().getColumn(2).setHeaderRenderer( headerRenderer );
				
				//set teh header value text + icon
				_table.getColumnModel().getColumn(1).setHeaderValue(new IA_TextIcon(colHeader1, blueIcon));
				_table.getColumnModel().getColumn(2).setHeaderValue(new IA_TextIcon(colHeader2,yellowIcon));
				
				//there are minimum 2 data values , if three then apply format to additional column
				if(noOfColumns == 3)
				{
					_table.getColumnModel().getColumn(3).setCellRenderer( rightRenderer );
					_table.getColumnModel().getColumn(3).setPreferredWidth(100);
					_table.getColumnModel().getColumn(3).setHeaderRenderer( headerRenderer );
					_table.getColumnModel().getColumn(3).setHeaderValue(new IA_TextIcon(colHeader3, greenIcon));
				}
				_table.setEnabled(false);
				
				/*change for sorting */
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>()
						{

						};
				
						sorter.setModel(_table.getModel());
						
//						sorter.setComparator(0, new Comparator<String>() {
//
//							@Override
//							public int compare(String o1, String o2) {
//								
//								return o1.compareTo(o2);
//							}
//						});
						sorter.setSortable(0, false);
						sorter.setComparator(1, new Comparator<Integer>() {

							@Override
							public int compare(Integer o1, Integer o2) {
								
								return o1.compareTo(o2);
							}
						});
						sorter.setComparator(2, new Comparator<Integer>() {

							@Override
							public int compare(Integer o1, Integer o2) {
								
								return o1.compareTo(o2);
							}
						});
						if(noOfColumns == 3)
						{
							sorter.setComparator(3, new Comparator<Integer>() {

							@Override
							public int compare(Integer o1, Integer o2) {
								
								return o1.compareTo(o2);
							}
							});
						}
						_table.setRowSorter(sorter);
						
					
						/* end add change for sorting */
				//display table on the UI
				this.reportDetails.setViewportView(_table);
		}
		
	}

	
	void addTableViewControllerTopScreens(DefaultTableModel tableData, int noOfColumns,
			String colHeader1, String colHeader2) {
		 //to hold the reports data.
			ImageIcon blueIcon;
			ImageIcon yellowIcon;
			ImageIcon greenIcon;
			IA_txtIconTableHeaderRenderer headerRenderer; 
			
			if(tableData != null && tableData.getRowCount() > 0)
			{
				//initialize the image icons to be displayed as column headers on the reports.
				blueIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Blue.png"));
				blueIcon = new ImageIcon(blueIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
				yellowIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Yellow.png"));
				yellowIcon = new ImageIcon(yellowIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
				greenIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Green.png"));
				greenIcon = new ImageIcon(greenIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
					
				//define the reports table header cell renderer that displays text + icon
				headerRenderer = new IA_txtIconTableHeaderRenderer();
				ReportsTable _table; //for reports other than Alarm reports.
				_table = new ReportsTable(tableData);
		
				
				_table.setPreferredScrollableViewportSize(this.reportDetails.getPreferredSize());
				//_table.setModel(tableData);
				_table.setVisible(true);
			     
			    _table.getTableHeader().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));
				 
					RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
					rightRenderer.paddingSize = 10;
					
					LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
					leftRenderer.paddingSize = 10;
					
					CenterCellRenderer centerRenderer = new CenterCellRenderer();
					centerRenderer.paddingSize = 10;
					_table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
					_table.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
					_table.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
					_table.getColumnModel().getColumn(3).setCellRenderer( rightRenderer );
					_table.getColumnModel().getColumn(4).setCellRenderer( rightRenderer );
					
					//adjust the total widths of the columns
					_table.getColumnModel().getColumn(0).setPreferredWidth(300);
					_table.getColumnModel().getColumn(1).setPreferredWidth(300);
					_table.getColumnModel().getColumn(2).setPreferredWidth(200);
					_table.getColumnModel().getColumn(3).setPreferredWidth(100);
					_table.getColumnModel().getColumn(4).setPreferredWidth(100);
					
					//create sorting ability
					_table.setAutoCreateRowSorter(false);
					
					//apply renderer for teh header
					_table.getColumnModel().getColumn(0).setHeaderRenderer(new LeftHeaderCellRenderer());
					_table.getColumnModel().getColumn(1).setHeaderRenderer( headerRenderer );
					_table.getColumnModel().getColumn(2).setHeaderRenderer( headerRenderer );
					_table.getColumnModel().getColumn(3).setHeaderRenderer( headerRenderer );
					_table.getColumnModel().getColumn(4).setHeaderRenderer( headerRenderer );
					
					//set teh header value text + icon
					_table.getColumnModel().getColumn(3).setHeaderValue(new IA_TextIcon(colHeader1, blueIcon));
					_table.getColumnModel().getColumn(4).setHeaderValue(new IA_TextIcon(colHeader2, yellowIcon));
					
					//there are minimum 2 data values , if three then apply format to additional column
			
					_table.setEnabled(false);
					
					/*change for sorting */
					TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>()
							{

							};
					
							sorter.setModel(_table.getModel());
							_table.setRowSorter(sorter);
							/* end add change for sorting */
					//display table on the UI
					this.reportDetails.setViewportView(_table);
			}
			
		}
	
	void addTableViewController(DefaultTableModel tableData, int noOfColumns,
			String colHeader1, String colHeader2, String colHeader3) {
		 //to hold the reports data.
			ImageIcon blueIcon;
			ImageIcon yellowIcon;
			ImageIcon greenIcon;
			IA_txtIconTableHeaderRenderer headerRenderer; 
			System.out.println("In addTableViewController - ");
			if(tableData != null && tableData.getRowCount() > 0)
			{
				//initialize the image icons to be displayed as column headers on the reports.
				blueIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Blue.png"));
				blueIcon = new ImageIcon(blueIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
				yellowIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Yellow.png"));
				yellowIcon = new ImageIcon(yellowIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
				greenIcon = new ImageIcon(getClass().getResource("Report-Chart-Colors-Green.png"));
				greenIcon = new ImageIcon(greenIcon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
					
				//define the reports table header cell renderer that displays text + icon
				headerRenderer = new IA_txtIconTableHeaderRenderer();
				ReportsTable _table; //for reports other than Alarm reports.
				_table = new ReportsTable(tableData);
		
				
				_table.setPreferredScrollableViewportSize(this.reportDetails.getPreferredSize());
				//_table.setModel(tableData);
				_table.setVisible(true);
			     
			    _table.getTableHeader().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));
				 
					RightPaddedTableCellRenderer rightRenderer = new RightPaddedTableCellRenderer();
					rightRenderer.paddingSize = 10;
					
					LeftPaddedTableCellRenderer leftRenderer = new LeftPaddedTableCellRenderer();
					leftRenderer.paddingSize = 10;
					
					
					_table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
					_table.getColumnModel().getColumn(1).setCellRenderer( rightRenderer );
					_table.getColumnModel().getColumn(2).setCellRenderer( rightRenderer );
					_table.getColumnModel().getColumn(3).setCellRenderer( rightRenderer );
					
					//adjust the total widths of the columns
					_table.getColumnModel().getColumn(0).setPreferredWidth(500);
					_table.getColumnModel().getColumn(1).setPreferredWidth(100);
					_table.getColumnModel().getColumn(2).setPreferredWidth(100);
					_table.getColumnModel().getColumn(3).setPreferredWidth(100);
					
					//create sorting ability
					_table.setAutoCreateRowSorter(false);
					
					//apply renderer for teh header
					_table.getColumnModel().getColumn(0).setHeaderRenderer(new LeftHeaderCellRenderer());
					_table.getColumnModel().getColumn(1).setHeaderRenderer(headerRenderer );
					_table.getColumnModel().getColumn(3).setHeaderRenderer( headerRenderer );
					_table.getColumnModel().getColumn(2).setHeaderRenderer(headerRenderer );
					_table.getColumnModel().getColumn(3).setHeaderRenderer( headerRenderer );
					
					//set teh header value text + icon
					_table.getColumnModel().getColumn(2).setHeaderValue(new IA_TextIcon(colHeader1, blueIcon));
					_table.getColumnModel().getColumn(3).setHeaderValue(new IA_TextIcon(colHeader2,yellowIcon));
					_table.getColumnModel().getColumn(4).setHeaderValue(new IA_TextIcon(colHeader3, greenIcon));
								
					_table.setEnabled(false);
					
					/*change for sorting */
					TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>()
							{


							};
					
							sorter.setModel(_table.getModel());
							
							sorter.setComparator(0, new Comparator<String>() {

								@Override
								public int compare(String o1, String o2) {
									
									return o1.compareTo(o2);
								}
							});
							
							sorter.setComparator(1, new Comparator<Integer>() {

								@Override
								public int compare(Integer o1, Integer o2) {
									
									return o1.compareTo(o2);
								}
							});
							sorter.setComparator(2, new Comparator<Integer>() {

								@Override
								public int compare(Integer o1, Integer o2) {
									
									return o1.compareTo(o2);
								}
							});
							if(noOfColumns == 3)
							{
								sorter.setComparator(3, new Comparator<Integer>() {

								@Override
								public int compare(Integer o1, Integer o2) {
									
									return o1.compareTo(o2);
								}
								});
							}
							_table.setRowSorter(sorter);
							
						
							/* end add change for sorting */
					//display table on the UI
					this.reportDetails.setViewportView(_table);
			}
			
		}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String actionCommad = e.getActionCommand();
		int indexToShow = 0;
		// this.top_left.middlePane.setPreferredSize(new Dimension(170,100));
		if (actionCommad.compareToIgnoreCase(Constants.CMD_DURATION_SELECT) == 0) {
			this.constantDuration = Constants.TODAY;
			String selectedDuration = comboBox.getSelectedItem().toString();
			if (selectedDuration != null) {
				selectedDuration = selectedDuration.trim();
				if (selectedDuration.compareToIgnoreCase("Today") == 0) {
					this.constantDuration = Constants.TODAY;
				} else if (selectedDuration.compareToIgnoreCase("Yesterday") == 0) {
					this.constantDuration = Constants.YESTERDAY;
				}

				else if (selectedDuration.compareToIgnoreCase("Last 7 Days") == 0) {
					this.constantDuration = Constants.LAST_SEVEN_DAYS;
				} else if (selectedDuration.compareToIgnoreCase("Last 30 Days") == 0) {
					this.constantDuration = Constants.LAST_THIRTY_DAYS;
				} else if (selectedDuration.compareToIgnoreCase("Last 90 Days") == 0) {
					this.constantDuration = Constants.LAST_NINTY_DAYS;
				} else if (selectedDuration
						.compareToIgnoreCase("Last 365 Days") == 0) {
					this.constantDuration = Constants.LAST_365_DAYS;
				} else if (selectedDuration.compareToIgnoreCase("This week") == 0) {
					this.constantDuration = Constants.THIS_WEEK;
				} else if (selectedDuration.compareToIgnoreCase("This month") == 0) {
					this.constantDuration = Constants.THIS_MONTH;
				} else if (selectedDuration.compareToIgnoreCase("This year") == 0) {
					this.constantDuration = Constants.THIS_YEAR;
				} else if (selectedDuration.compareToIgnoreCase("Last month") == 0) {
					this.constantDuration = Constants.LAST_MONTH;
				} else if (selectedDuration.compareToIgnoreCase("Last week") == 0) {
					this.constantDuration = Constants.LAST_WEEK;
				} else if (selectedDuration.compareToIgnoreCase("Last year") == 0) {
					this.constantDuration = Constants.LAST_YEAR;
				} else {
					this.constantDuration = Constants.TODAY;
				}
			}
		}

		this.noOfPeople = rpc.getNumberOfActiveUsersOnController(constantDuration, gateway,project,reportForAllGateways,
				reportForAllProjects);

		this.reportGraph.removeAll();
		if (selectedReportMenu.compareToIgnoreCase("Alarm Summary") != 0) {
			this._reportChartPanel = new ChartPanel(null);
			this.reportGraph.add(_reportChartPanel, BorderLayout.CENTER);
		} else {

			this.reportGraph.add(this.upperScroll, BorderLayout.CENTER);
		}

		addReport();
		revalidate();
		repaint();

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point point = e.getPoint();
		int column = currentTable.columnAtPoint(point);
		// alarmSummaryTable.setC;

		System.out.println("Column no clicked is : " + column);
		columnSelected = column;

		SortAlarmTable(false);

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

	public void SortAlarmTable(boolean isRefresh) {
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				currentTable.getModel());
		currentTable.setRowSorter(sorter);
		List<RowSorter.SortKey> sortKeys = new ArrayList<SortKey>();

		System.out.println("SortAlarmTable with isRefresh - " + isRefresh);
		System.out.println("SortAlarmTable with sortOrder - " + sortOrder);

		// if(columnSelected == 1)
		// {
		// SortOrder order =
		// SortOrder.values()["Low","Medium","High","Critical"];
		// sortKeys.add(new RowSorter.SortKey(columnSelected,order));
		// }
		// else
		// {

		if (isRefresh == true) {
			if (sortOrder == true) {
				sortKeys.add(new RowSorter.SortKey(columnSelected,
						SortOrder.ASCENDING));
			} else {
				sortKeys.add(new RowSorter.SortKey(columnSelected,
						SortOrder.DESCENDING));
			}
		} else {
			if (sortOrder == true) {
				sortOrder = false;
				sortKeys.add(new RowSorter.SortKey(columnSelected,
						SortOrder.DESCENDING));
			} else {
				sortOrder = true;
				sortKeys.add(new RowSorter.SortKey(columnSelected,
						SortOrder.ASCENDING));
			}
		}

		// }
		sorter.setSortKeys(sortKeys);
		if (columnSelected == 3) {
			sorter.setComparator(3, new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {

					return o1.compareTo(o2);
				}
			});
		}
		sorter.sort();
	}

}
