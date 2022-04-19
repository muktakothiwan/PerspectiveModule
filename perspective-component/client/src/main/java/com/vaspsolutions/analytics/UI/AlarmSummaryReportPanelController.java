package com.vaspsolutions.analytics.UI;

import java.awt.Dimension;












import java.awt.Font;



import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JScrollPane;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import com.inductiveautomation.ignition.common.Dataset;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ModuleRPC;

import java.awt.Color;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.awt.BorderLayout;

public class AlarmSummaryReportPanelController extends JPanel {
	private JPanel ringChartPanel;
	private ReportsTable _reportTable;
	private ModuleRPC _rpc;
	private boolean _allProjects;
	private String _projectName;
	private boolean _allGateways;
	private String _gatewayName;
	private int _duration;
	JPanel ringFrequencyPanel;
	JPanel ringDurationPanel;
	JPanel _panel3;
	JFreeChart returnPlot= null;
	JFreeChart durartionPlot= null;
	NumberFormat nf = NumberFormat.getInstance();
	List<Color> colorArray;
	
	public AlarmSummaryReportPanelController(ReportsTable reportTable, ModuleRPC rpc,int duration,boolean allProjects, String projecName,boolean allGateways,String gatewayName) {
		setForeground(Constants.COLOR_WHITE_BACKGROUND);
		this.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		this.setPreferredSize(new Dimension(1500,860));
		
		_duration = duration;
		_allProjects =allProjects;
		_projectName = projecName;
		_allGateways =allGateways;
		_gatewayName = gatewayName;
		this._rpc = rpc;
		
		_reportTable = reportTable;
		
		
		colorArray		 = new ArrayList<Color>();
		colorArray.add(new Color(0, 65, 112));
		colorArray.add(new Color(158, 210, 63));
		colorArray.add(new Color(248, 143, 26));
		colorArray.add(new Color(248, 97, 54));
		colorArray.add(new Color(255, 200, 1));
		colorArray.add(new Color(78, 96, 119));
		colorArray.add(new Color(127, 127, 127));
		colorArray.add(new Color(255, 255, 128));
		colorArray.add(new Color(228, 154, 176));
		colorArray.add(Color.CYAN);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0};
		gridBagLayout.rowHeights = new int[]{560,300};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0,1.0};
		setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		
		panel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0};
		gbl_panel.rowHeights = new int[]{280,280};
		gbl_panel.columnWeights = new double[]{1.0};
		gbl_panel.rowWeights = new double[]{1.0,1.0};
		panel.setLayout(gbl_panel);
		
		ringChartPanel = new JPanel();
		ringChartPanel.setBorder(null);
		ringChartPanel.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		
		ringChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_ringChartPanel = new GridBagConstraints();
		gbc_ringChartPanel.insets = new Insets(0, 20, 5, 20);
		gbc_ringChartPanel.fill = GridBagConstraints.BOTH;
		gbc_ringChartPanel.gridx = 0;
		gbc_ringChartPanel.gridy = 0;
		panel.add(ringChartPanel, gbc_ringChartPanel);
		GridBagLayout gbl_ringChartPanel = new GridBagLayout();
		gbl_ringChartPanel.columnWidths = new int[]{750,750};
		gbl_ringChartPanel.rowHeights = new int[]{0};
		gbl_ringChartPanel.columnWeights = new double[]{1.0,1.0};
		gbl_ringChartPanel.rowWeights = new double[]{1.0};
		ringChartPanel.setLayout(gbl_ringChartPanel);
		
		JPanel byFrequencyPanel = new RoundedAlarmsReportHeaderPanel();
		
		byFrequencyPanel.setBorder(null);
		byFrequencyPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		byFrequencyPanel.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_byFrequencyPanel = new GridBagConstraints();
		gbc_byFrequencyPanel.insets = new Insets(0, 0, 0, 75);
		gbc_byFrequencyPanel.fill = GridBagConstraints.BOTH;
		gbc_byFrequencyPanel.gridx = 0;
		gbc_byFrequencyPanel.gridy = 0;
		ringChartPanel.add(byFrequencyPanel, gbc_byFrequencyPanel);
		GridBagLayout gbl_byFrequencyPanel = new GridBagLayout();
		gbl_byFrequencyPanel.columnWidths = new int[]{0};
		gbl_byFrequencyPanel.rowHeights = new int[]{15,205};
		gbl_byFrequencyPanel.columnWeights = new double[]{1.0};
		gbl_byFrequencyPanel.rowWeights = new double[]{0.0,1.0};
		byFrequencyPanel.setLayout(gbl_byFrequencyPanel);
		
		JLabel lblNewLabel = new JLabel("   Alarm by Frequency");
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel.setForeground(Constants.COLOR_GREY_LABEL);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0,10, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		byFrequencyPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		ringFrequencyPanel = new JPanel();
		ringFrequencyPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		ringFrequencyPanel.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_ringFrequencyPanel = new GridBagConstraints();
		
		gbc_ringFrequencyPanel.fill = GridBagConstraints.BOTH;
		gbc_ringFrequencyPanel.gridx = 0;
		gbc_ringFrequencyPanel.gridy = 1;
		byFrequencyPanel.add(ringFrequencyPanel, gbc_ringFrequencyPanel);
		
		JPanel byDurationPanel = new RoundedAlarmsReportHeaderPanel();
		byDurationPanel.setBorder(null);
		byDurationPanel.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		byDurationPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_byDurationPanel = new GridBagConstraints();
		gbc_byDurationPanel.insets = new Insets(0, 75, 0, 0);
		gbc_byDurationPanel.fill = GridBagConstraints.BOTH;
		gbc_byDurationPanel.gridx = 1;
		gbc_byDurationPanel.gridy = 0;
		ringChartPanel.add(byDurationPanel, gbc_byDurationPanel);
		GridBagLayout gbl_byDurationPanel = new GridBagLayout();
		gbl_byDurationPanel.columnWidths = new int[]{0};
		gbl_byDurationPanel.rowHeights = new int[]{15,215};
		gbl_byDurationPanel.columnWeights = new double[]{1.0};
		gbl_byDurationPanel.rowWeights = new double[]{0.0,1.0};
		byDurationPanel.setLayout(gbl_byDurationPanel);
		
		JLabel lblNewLabel_1 = new JLabel("   Alarms by Duration (seconds)");
		lblNewLabel_1.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel_1.setForeground(Constants.COLOR_GREY_LABEL);
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 10, 0);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		byDurationPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		ringDurationPanel = new JPanel();
		ringDurationPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		ringDurationPanel.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_ringDurationPanel = new GridBagConstraints();
		gbc_ringDurationPanel.fill = GridBagConstraints.BOTH;
		gbc_ringDurationPanel.gridx = 0;
		gbc_ringDurationPanel.gridy = 1;
		byDurationPanel.add(ringDurationPanel, gbc_ringDurationPanel);
		
		JPanel barChartPanel = new RoundedAlarmsReportHeaderPanel();
		barChartPanel.setBorder(null);
		barChartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_barChartPanel = new GridBagConstraints();
		gbc_barChartPanel.insets = new Insets(5, 20, 0, 20);
		gbc_barChartPanel.fill = GridBagConstraints.BOTH;
		gbc_barChartPanel.gridx = 0;
		gbc_barChartPanel.gridy = 1;
		panel.add(barChartPanel, gbc_barChartPanel);
		GridBagLayout gbl_barChartPanel = new GridBagLayout();
		gbl_barChartPanel.columnWidths = new int[]{0};
		gbl_barChartPanel.rowHeights = new int[]{15,215};
		gbl_barChartPanel.columnWeights = new double[]{1.0};
		gbl_barChartPanel.rowWeights = new double[]{0.0,1.0};
		barChartPanel.setLayout(gbl_barChartPanel);
		
		JLabel lblNewLabel_2 = new JLabel();
		
		if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
		{
			lblNewLabel_2.setText("   Alarms by Hour of Day");
		}
		else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS)
		{
			lblNewLabel_2.setText("   Alarms by Day of Week");
		}
		else if(duration == Constants.THIS_MONTH || duration == Constants.LAST_NINTY_DAYS || duration == Constants.LAST_THIRTY_DAYS || duration == Constants.LAST_MONTH)
		{
			lblNewLabel_2.setText("   Alarms by Day");
		}
		else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR)
		{
			lblNewLabel_2.setText("   Alarms by Month");
		}
		else
		{
			lblNewLabel_2.setText("   Alarms by Day");
		}
		lblNewLabel_2.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel_2.setForeground(Constants.COLOR_GREY_LABEL);
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 10, 0);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		barChartPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		 _panel3 = new JPanel();
		 _panel3.setBorder(null);
		 _panel3.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		 _panel3.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 1;
		barChartPanel.add(_panel3, gbc_panel_3);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5,0,0,0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		//scrollPane.setViewportView(_reportTable);
		scrollPane.setViewportView(_reportTable);
		scrollPane.getViewport().setBackground(Constants.COLOR_WHITE_BACKGROUND);
		returnPlot = this.createFrequencyRingchartGraph();
		
	
		
		ChartPanel frequencyChart = new ChartPanel(returnPlot);
		frequencyChart.setBorder(null);
		
		frequencyChart.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		frequencyChart.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		frequencyChart.setPreferredSize(new Dimension(645,235));
		this.ringFrequencyPanel.add(frequencyChart,BorderLayout.CENTER);
		
		//add alarms by duration graph
		durartionPlot = this.createDurationRingchartGraph();
		
		ChartPanel durationChart = new ChartPanel(durartionPlot);
		durationChart.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		durationChart.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		durationChart.setPreferredSize(new Dimension(645,235));
		this.ringDurationPanel.add(durationChart,BorderLayout.CENTER);
		createAlarmsByHourChart();
		
		
		
	}
	
	/*
	 * OMkar 6-April-2016
	 */
	public JFreeChart createDurationRingchartGraph(){
		JFreeChart c = null;
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		int pieDataSize = 0;
		//Dataset alarmSummaryDaata = _rpc.alarmSummaryReportRingChart(projectName, allProjects, this._duration,)
		Dataset alarmSummaryData = this._rpc.getTop10AlarmsByDurationController(_projectName, _allProjects, _gatewayName, _allGateways, _duration);
		if(alarmSummaryData != null){
		int datasetSize = alarmSummaryData.getRowCount();
		List<String> tagArray = new ArrayList<String>();
		double totalForPercentage = 0;
	
	
		for(int j = 0 ; j < datasetSize ; j++){
			String key = alarmSummaryData.getValueAt(j, 0).toString();
			if(alarmSummaryData.getValueAt(j, 1) != null)
			{
				Double addDouble = Double.parseDouble(alarmSummaryData.getValueAt(j, 1).toString());
				totalForPercentage = addDouble + totalForPercentage;
				
				key = key + " - " + 	nf.format(addDouble.intValue()) ;
				System.out.println("Key Value is : " + key);
				System.out.println("Array j value is  : " + j);
				tagArray.add(key);
			}
			
		}

		
		
		System.out.println("total no of rows " + datasetSize);
		for(int i = 0 ; i < datasetSize ; i++){
			if(alarmSummaryData.getValueAt(i, 1) != null)
			{
				Double value = Double.parseDouble(alarmSummaryData.getValueAt(i, 1).toString());
			
				int percentage = (int) ((value.intValue() * 100 ) / totalForPercentage);
			
				String valueTag = tagArray.get(i) + " (" + percentage + "%)";
			
				pieDataset.insertValue(i,valueTag , value);
				pieDataSize++;
			}
		}
		
		
		
		c = ChartFactory.createRingChart("", pieDataset, false, false, false);
		
		c.setBorderVisible(false);
		
		
		
		
		RingPlot ringChart = (RingPlot)c.getPlot();
		ringChart.setCircular(true);
		ringChart.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		ringChart.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
		for(int i = 0 ; i < pieDataSize ; i++){
			
		ringChart.setSectionPaint(pieDataset.getKey(i), colorArray.get(i));
		}
		ringChart.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		ringChart.setOutlineVisible(false);
		}
		
		return c ;
	}
	
	public JFreeChart createFrequencyRingchartGraph(){
		JFreeChart c = null;
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		int pieDataSize = 0;
		//Dataset alarmSummaryDaata = _rpc.alarmSummaryReportRingChart(projectName, allProjects, this._duration,)
		Dataset alarmSummaryData = this._rpc.alarmSummaryReportRingChartController(_projectName, _allProjects, _gatewayName, _allGateways, _duration);
		if(alarmSummaryData != null){
			int datasetSize = alarmSummaryData.getRowCount();
			List<String> tagArray = new ArrayList<String>();
			double totalForPercentage = 0;
			for(int j = 0 ; j < datasetSize ; j++){
				String key = alarmSummaryData.getValueAt(j, 0).toString();
				if(alarmSummaryData.getValueAt(j, 1) != null)
				{
					Double addDouble = Double.parseDouble(alarmSummaryData.getValueAt(j, 1).toString());
					totalForPercentage = addDouble + totalForPercentage;
					key = key + " - " + 	nf.format(addDouble.intValue()) ;
					System.out.println("Key Value is : " + key);
					System.out.println("Array j value is  : " + j);
					tagArray.add(key);
				}
				
			}
			
			for(int i = 0 ; i < datasetSize ; i++){
				if(alarmSummaryData.getValueAt(i, 1) != null)
				{
					Double value = Double.parseDouble(alarmSummaryData.getValueAt(i, 1).toString());
				
					int percentage = (int) ((value.intValue() * 100 ) / totalForPercentage);
				
					String valueTag = tagArray.get(i) + " (" + percentage + "%)";
				
					pieDataset.insertValue(i,valueTag , value);
					pieDataSize++;
				}
			}

		
		
		c = ChartFactory.createRingChart("", pieDataset, false, false, false);
		
		c.setBorderVisible(false);
		
		
		RingPlot ringChart = (RingPlot)c.getPlot();
		ringChart.setCircular(true);
		ringChart.setOutlineVisible(false);
		ringChart.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
		for(int i = 0 ; i < datasetSize ; i++){
			
			ringChart.setSectionPaint(pieDataset.getKey(i), colorArray.get(i));
			}
		ringChart.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		//returnPlot = new JFreeChart(ringChart);
		}
		
		return c ;
	}
	
	public void createAlarmsByHourChart()
	{
		System.out.println("before calling getAlarmCountsPerDurationController");
		Dataset alarmsByHourData = _rpc.getAlarmCountsPerDurationController(_projectName, _allProjects, _gatewayName, _allGateways, _duration);
		System.out.println("after calling getAlarmCountsPerDurationController");
		DefaultCategoryDataset _barData = new DefaultCategoryDataset();
		JFreeChart _barChart;
		
		if(alarmsByHourData != null)
		{
			int noOfRows = alarmsByHourData.getRowCount();
			int i;
			SimpleDateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
			SimpleDateFormat sdf = new SimpleDateFormat("M/d");
			String series1 = "Count";
			Date xDate = null;
			String dateValTable = "";
			int val1 = 0;
			for(i=0; i<noOfRows; i++)
			{
				String dateVal = alarmsByHourData.getValueAt(i, 0).toString();
				if(_duration != Constants.TODAY && _duration != Constants.YESTERDAY
						&& _duration != Constants.LAST_365_DAYS && _duration != Constants.THIS_YEAR
						&& _duration != Constants.LAST_YEAR && _duration != Constants.THIS_WEEK && _duration != Constants.LAST_WEEK
						&& _duration != Constants.LAST_SEVEN_DAYS)
				{
					try {
						dateVal = sdf.format(df.parse(alarmsByHourData.getValueAt(i, 0).toString()));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				else
				{
					dateVal = alarmsByHourData.getValueAt(i, 0).toString();
				}
				
				if(alarmsByHourData.getValueAt(i, 1) != null)
				{
					val1 = (int)Float.parseFloat(alarmsByHourData.getValueAt(i, 1).toString());
				}
				_barData.addValue(val1, series1, dateVal);
			}
			
			System.out.println("After executing getAlarmCountsPerDurationController");
			//new code with skip labels
		
			BarRenderer renderer = new BarRenderer();
	        CategoryAxis domainAxis1 = null;
	        
	        Font chartFont = new Font("Arial",Font.PLAIN, 10);
	         // new CategoryAxis("Category");
	        int noOfRecords = _barData.getRowCount();
	        
	     if(this._duration == Constants.TODAY || this._duration == Constants.YESTERDAY || this._duration == Constants.THIS_WEEK || 
	    		 this._duration == Constants.LAST_WEEK || this._duration == Constants.LAST_SEVEN_DAYS || 
	    		 this._duration == Constants.THIS_YEAR || this._duration == Constants.LAST_YEAR || this._duration == Constants.LAST_365_DAYS )
	     {
	    	 domainAxis1 =  new CategoryAxisSkipLabels(1); 
	     }
	     else
	     {
	    	 domainAxis1 =  new CategoryAxisSkipLabels(1); 
//	    	 	if(noOfRows <=7)
//		        {
//		        	domainAxis1 =  new CategoryAxisSkipLabels(1); 
//		        }
//		        else if(noOfRows > 7 && noOfRows <= 15)
//		        {
//		        	domainAxis1 =  new CategoryAxisSkipLabels(2); 
//		        }
//		        else if(noOfRows > 15 && noOfRows <= 30)
//		        {
//		        	domainAxis1 =  new CategoryAxisSkipLabels(3); 
//		        }
//		        else if(noOfRows > 30 && noOfRows<=50)
//		        {
//		        	domainAxis1 =  new CategoryAxisSkipLabels(5); 
//		        }
//		        else if(noOfRows > 50)
//		        {
//		        	domainAxis1 =  new CategoryAxisSkipLabels(7); 
//		        }
	     }
	        
			
	        domainAxis1.setTickMarksVisible(true);
	        System.out.println("before drawing getAlarmCountsPerDurationController");
			CategoryLabelPositions p = domainAxis1.getCategoryLabelPositions();

			CategoryLabelPosition left = new CategoryLabelPosition(
			    RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, 
			    TextAnchor.CENTER_LEFT, 0.0,
			    CategoryLabelWidthType.RANGE, 0.20f //Assign 70% of space for category labels
			);

			domainAxis1.setCategoryLabelPositions(CategoryLabelPositions
			        .replaceLeftPosition(p, left));

	        domainAxis1.setCategoryMargin(0.0);
	        domainAxis1.setLowerMargin(0.0);
	        domainAxis1.setTickMarksVisible(true);
	        domainAxis1.setTickLabelInsets(new RectangleInsets(0.0,0.0,0.0,0.0));
	        domainAxis1.setVisible(true);
	        
	        domainAxis1.setTickLabelFont(chartFont);
	        
	        
			domainAxis1.setAxisLineVisible(false);
	        NumberAxis rangeAxis1 = new NumberAxis("");
	        
	        CategoryPlot plot = new CategoryPlot(
	        		_barData, domainAxis1, rangeAxis1, renderer
	        );
	        plot.setAxisOffset(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
	        
	        _barChart = new JFreeChart(plot);
			
			///end new code
			
			_barChart.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
			_barChart.setBorderVisible(false);
			_barChart.removeLegend();
			
		        // get a reference to the plot for further customisation...
		         plot = _barChart.getCategoryPlot();
		        plot.setBackgroundPaint(Constants.COLOR_WHITE_BACKGROUND);
		        ((BarRenderer)plot.getRenderer()).setBarPainter(new StandardBarPainter());
		        plot.setRangeGridlinePaint(Constants.COLOR_GRADIENT_LINE);
		        plot.setOutlineVisible(false);
		        
		        // set the range axis to display integers only...
		        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		       rangeAxis.setAxisLineVisible(false);
		       rangeAxis.setAutoRangeIncludesZero(false);
		        
		       // disable bar outlines...
		         renderer = (BarRenderer) plot.getRenderer();
		        renderer.setDrawBarOutline(false);
		        renderer.setShadowVisible(false);
		        renderer.setMaximumBarWidth(0.02);
		        renderer.setItemMargin(0.0);
		        renderer.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
		        renderer.setSeriesPaint(0, new Color(158, 210, 63));
		       
		        CategoryAxis domainAxis = plot.getDomainAxis();
		        
		       
		        domainAxis.setTickLabelFont(chartFont);
		       // rangeAxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		        
		        
		        rangeAxis.setTickLabelFont(chartFont);
		        rangeAxis.setTickLabelPaint(Color.BLACK);
		        
		     //   domainAxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		       domainAxis.setTickLabelPaint(Color.BLACK);
		       
		       
		       //create chart panel
		       ChartPanel chartPanel = new ChartPanel(_barChart);
		       
		       chartPanel.setBorder(null);
		       chartPanel.setForeground(Constants.COLOR_WHITE_BACKGROUND);
		       chartPanel.setBackground(Constants.COLOR_WHITE_BACKGROUND);
		       chartPanel.setPreferredSize(new Dimension(1430,235));
			   this._panel3.add(chartPanel,BorderLayout.CENTER);
		}
			
			
		}
	

}
