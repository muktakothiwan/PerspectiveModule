package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;
import javax.swing.BoxLayout;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Window;

import javax.swing.JScrollPane;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.inductiveautomation.ignition.client.util.gui.LinkButton;
import com.inductiveautomation.ignition.common.script.builtin.SystemUtilities;
import com.inductiveautomation.vision.api.client.VisionClientInterface;
import com.vaspsolutions.analytics.common.Constants;
/**
 * A class to represent the GUI layout , extends JPanel 
 * @author YM : Created on 05/20/2015
 *
 */
public class MasterPanel extends JPanel  {
	
	private static final long serialVersionUID = 1L;
	public IA_SlidingPanel top_left;
	public JPanel timeLine ;
	public JPanel overview;
	public JScrollPane content;
	public JPanel activeUsers ;
	public JPanel freqAndRecency;
	public JPanel engageMent;
	public JPanel alarms;
	public JPanel y_overview;
	public JPanel reportsAndTrends;
	public JPanel gateWay;
	public JPanel bottom_placeholder;
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
	private JLabel lblScreenViewed;
	public JButton valScreenViewed;
	private JLabel lblLoggedIn;
	public JLabel valLoggedIn;
	private JLabel lblNoOfVisits;
	
	
	public MasterPanel() {
		//setBackground(new Color(176, 196, 222));
		Font lblFont =  new Font(Font.SANS_SERIF, Font.BOLD, 11);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{176,200,200,200};
		gridBagLayout.rowHeights = new int[]{107, 80, 80, 80,36};
		gridBagLayout.columnWeights = new double[]{1.0,1.0, 1.0,1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0};
		setLayout(gridBagLayout);
		this.setBackground(Color.DARK_GRAY);
		
		top_left = new IA_SlidingPanel(3);
		top_left.setToolTipText("Item 3");
		GridBagConstraints gbc_top_left = new GridBagConstraints();
		gbc_top_left.insets = new Insets(0, 0, 5, 5);
		gbc_top_left.gridx = 0;
		gbc_top_left.gridy = 0;
		add(top_left, gbc_top_left);
		
		timeLine = new JPanel();
		timeLine.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_timeLine = new GridBagConstraints();
		gbc_timeLine.gridwidth = 3;
		gbc_timeLine.insets = new Insets(0, 0, 5, 0);
		gbc_timeLine.fill = GridBagConstraints.BOTH;
		gbc_timeLine.gridx = 1;
		gbc_timeLine.gridy = 0;
		add(timeLine, gbc_timeLine);
		timeLine.setLayout(new GridLayout(0,1));
		
		//Overview panel and its contents
		overview = new JPanel();
		overview.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_overview = new GridBagConstraints();
		gbc_overview.gridheight = 2;
		gbc_overview.insets = new Insets(0, 0, 5, 5);
		gbc_overview.fill = GridBagConstraints.BOTH;
		gbc_overview.gridx = 0;
		gbc_overview.gridy = 1;
		add(overview, gbc_overview);
		overview.setLayout(new GridLayout(0,2));
		overview.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		lblUsers = new IA_Label("Users");
		overview.add(lblUsers);
		
		txtUsers = new OrangeText();
		overview.add(txtUsers);
		
		
		lblSessions = new IA_Label("Sessions");
		overview.add(lblSessions);
		
		txtSessions = new OrangeText();
		overview.add(txtSessions);
		
		
		lblScreenViews = new IA_Label("Screen Views");
		lblScreenViews.setText("Total Screen Views");
		overview.add(lblScreenViews);
		
		txtScreenViews = new OrangeText();
		overview.add(txtScreenViews);
		
		
		lblScreensbyCurrent = new IA_Label("Screens Per Sessions");
		lblScreensbyCurrent.setText("Screen Views by Current Users");
		overview.add(lblScreensbyCurrent);
		
		txtScreensbyCurrent = new OrangeText();
		overview.add(txtScreensbyCurrent);
		
		
		lblActions = new IA_Label("Actions");
		overview.add(lblActions);
		
		txtActions = new OrangeText();
		overview.add(txtActions);
		
		lblBounceRate = new IA_Label("Bounce Rate");
		overview.add(lblBounceRate);
		
		txtBounceRate = new OrangeText();
		overview.add(txtBounceRate);
		
		lblAvgSessionDuration = new IA_Label("Average Session Duration");
		overview.add(lblAvgSessionDuration);
		
		txtAvgSessionDuration = new OrangeText();
		overview.add(txtAvgSessionDuration);
		
		
		//end overview panel
		content = new JScrollPane();
		content.setBackground(new Color(224, 255, 255));
		
		GridBagConstraints gbc_content = new GridBagConstraints();
		gbc_content.gridheight = 2;
		gbc_content.insets = new Insets(0, 0, 5, 5);
		gbc_content.fill = GridBagConstraints.BOTH;
		gbc_content.gridx = 1;
		gbc_content.gridy = 1;
		add(content, gbc_content);
		
		 activeUsers = new JPanel();
		activeUsers.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_activeUsers = new GridBagConstraints();
		gbc_activeUsers.gridwidth = 2;
		gbc_activeUsers.insets = new Insets(0, 0, 5, 0);
		gbc_activeUsers.fill = GridBagConstraints.BOTH;
		gbc_activeUsers.gridx = 2;
		gbc_activeUsers.gridy = 1;
		add(activeUsers, gbc_activeUsers);
		activeUsers.setLayout(new GridLayout(0,1));
		
		engageMent = new JPanel();
		engageMent.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_engageMent = new GridBagConstraints();
		gbc_engageMent.insets = new Insets(0, 0, 5, 5);
		gbc_engageMent.fill = GridBagConstraints.BOTH;
		gbc_engageMent.gridx = 2;
		gbc_engageMent.gridy = 2;
		add(engageMent, gbc_engageMent);
		engageMent.setLayout(new GridLayout(0,2));
		
		lblScreenViewed = new JLabel("Screen viewed");
		engageMent.add(lblScreenViewed);
		
		valScreenViewed = new JButton();
		
		valScreenViewed.setActionCommand(Constants.CMD_Screen_Name_Click);
		//valScreenViewed.addActionListener(this);
		valScreenViewed.setBorderPainted(false);
		//make a button look like Label
		valScreenViewed.setBackground(this.engageMent.getBackground());
		valScreenViewed.setFont(UIManager.getFont("Label.font"));
		valScreenViewed.setBorder(UIManager.getBorder("Label.border"));
		Font font = valScreenViewed.getFont();
		Map attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		valScreenViewed.setFont(font.deriveFont(attributes));
		
		engageMent.add(valScreenViewed);
		
		lblLoggedIn = new JLabel("Logged in since:");
		engageMent.add(lblLoggedIn);
		
		valLoggedIn = new JLabel("");
		engageMent.add(valLoggedIn);
		
		alarms = new JPanel();
		alarms.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_alarms = new GridBagConstraints();
		gbc_alarms.insets = new Insets(0, 0, 5, 0);
		gbc_alarms.fill = GridBagConstraints.BOTH;
		gbc_alarms.gridx = 3;
		gbc_alarms.gridy = 2;
		add(alarms, gbc_alarms);
	
		GridBagLayout gbl_alarms = new GridBagLayout();
		gbl_alarms.columnWidths = new int[]{0, 0, 0};
		gbl_alarms.rowHeights = new int[]{86, 0, 0};
		gbl_alarms.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_alarms.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		alarms.setLayout(gbl_alarms);
		
		y_overview = new JPanel();
		y_overview.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_y_overview = new GridBagConstraints();
		gbc_y_overview.insets = new Insets(0, 0, 5, 5);
		gbc_y_overview.fill = GridBagConstraints.BOTH;
		gbc_y_overview.gridx = 0;
		gbc_y_overview.gridy = 3;
		add(y_overview, gbc_y_overview);
		y_overview.setLayout(new GridLayout(0,1));
		
		reportsAndTrends = new JPanel();
		reportsAndTrends.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_reportsAndTrends = new GridBagConstraints();
		gbc_reportsAndTrends.insets = new Insets(0, 0, 5, 5);
		gbc_reportsAndTrends.fill = GridBagConstraints.BOTH;
		gbc_reportsAndTrends.gridx = 1;
		gbc_reportsAndTrends.gridy = 3;
		add(reportsAndTrends, gbc_reportsAndTrends);
		reportsAndTrends.setLayout(new GridLayout(0,1));
		
		gateWay = new JPanel();
		gateWay.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_gateWay = new GridBagConstraints();
		gbc_gateWay.insets = new Insets(0, 0, 5, 5);
		gbc_gateWay.fill = GridBagConstraints.BOTH;
		gbc_gateWay.gridx = 2;
		gbc_gateWay.gridy = 3;
		add(gateWay, gbc_gateWay);
		
		freqAndRecency = new JPanel();
		freqAndRecency.setBackground(new Color(224, 255, 255));
		GridBagConstraints gbc_freqAndRecency = new GridBagConstraints();
		gbc_freqAndRecency.insets = new Insets(0, 0, 5, 0);
		gbc_freqAndRecency.fill = GridBagConstraints.BOTH;
		gbc_freqAndRecency.gridx = 3;
		gbc_freqAndRecency.gridy = 3;
		add(freqAndRecency, gbc_freqAndRecency);
		
		GridBagLayout gbl_freqAndRecency = new GridBagLayout();
		gbl_freqAndRecency.columnWidths = new int[]{0, 0};
		gbl_freqAndRecency.rowHeights = new int[]{0, 0};
		gbl_freqAndRecency.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_freqAndRecency.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		freqAndRecency.setLayout(gbl_freqAndRecency);
		
		lblNoOfVisits = new JLabel("No of visits");
		GridBagConstraints gbc_lblNoOfVisits = new GridBagConstraints();
		gbc_lblNoOfVisits.gridx = 0;
		gbc_lblNoOfVisits.gridy = 0;
		freqAndRecency.add(lblNoOfVisits, gbc_lblNoOfVisits);
		
		bottom_placeholder = new JPanel();
		bottom_placeholder.setBackground(new Color(176, 196, 222));
		GridBagConstraints gbc_bottom_placeholder = new GridBagConstraints();
		gbc_bottom_placeholder.gridwidth = 4;
		gbc_bottom_placeholder.fill = GridBagConstraints.BOTH;
		gbc_bottom_placeholder.gridx = 0;
		gbc_bottom_placeholder.gridy = 4;
		add(bottom_placeholder, gbc_bottom_placeholder);
	}


	

}
