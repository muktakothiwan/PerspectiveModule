package com.vaspsolutions.analytics.UI;


import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;









import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.vaspsolutions.analytics.common.Constants;


/**
 * A class to create a layout to show Historical data
 * Stores all the required data values
 * Uses labels and tables to display the data on the UI.
 * @author YM
 * 
 *
 */
@SuppressWarnings("serial")
public class HistoryPanel extends JPanel {

	private int numUsers; //configured users in a project
	private int numSessions; //number of sessions
	private int numActions; //total number of actions irrespective of the users
	private int numScreenViews; //total number of screen viewed by all the users
	private int bounceRate; 
	private String sessionDuration; //Average session duration of all the users in a given time period.
	private int numNewUsers; //Total number of users that have logged in for the first time in given time frame
	private List<String> userSessions; // count of session for each user
	private List<String> numAlarms; //Number of alarms for each priority level
	private List<String> timeToClearAlarms; // Time to clear alarms of each priority level
	
	
	private IA_Label lblnumUsers;
	private IA_Label lblnumSessions;
	private IA_Label lblnumActions;
	private IA_Label lblnumScreenViews;
	private IA_Label lblbounceRate;
	private IA_Label lblsessionDuration;
	private IA_Label lblnumNewUsers;
	private IA_Label lbluserSessions; // count of session for each user
	private IA_Label lblnumAlarms; //Number of alarms for each priority level
	private IA_Label lbltimeToClearAlarms; 
	
	private OrangeText actnumUsers;
	private OrangeText actnumSessions;
	private OrangeText actnumActions;
	private OrangeText actnumScreenViews;
	private OrangeText actbounceRate;
	private OrangeText actsessionDuration;
	private OrangeText actnumNewUsers;

	private HistoryTable actuserSessions; // count of session for each user
	private HistoryTable actnumAlarms; //Number of alarms for each priority level
	private HistoryTable acttimeToClearAlarms;  //Average time to clear alarms per priority level
	
	
	public HistoryPanel() {
		super();
		
		BundleUtil.get().addBundle("modIA_EN", this.getClass(), "mod_ia_en");
		
		JLabel dummy = new JLabel();
		dummy.setText("   ");
		this.setSize(400, 800);
		this.setBackground(Color.WHITE);
		
		GridLayout gl = new GridLayout(0, 2);
		gl.setVgap(10);
		setLayout(gl);
		
		this.numUsers = 0;
		this.numSessions = 0;
		this.numActions = 0;
		this.numScreenViews = 0;
		this.bounceRate = 0;
		this.sessionDuration = "";
		this.numNewUsers = 0;
		this.userSessions = new ArrayList<String>();
		
		this.numAlarms = new ArrayList<String>();
		this.timeToClearAlarms = new ArrayList<String>();
		
		lblnumUsers = new IA_Label();
	
		lblnumUsers.setText("Number of users");
		//lblnumUsers.setText(BundleUtil.get().getString("modIA_EN.lbl.numUsers"));
		lblnumSessions = new IA_Label();
		lblnumSessions.setText("Number of sessions");
		
		lblnumActions = new IA_Label();
		lblnumActions.setText("Number of actions");
		
		lblnumScreenViews = new IA_Label();
		lblnumScreenViews.setText("Number of screen views");
		
		lblbounceRate = new IA_Label();
		lblbounceRate.setText("Bounce rate");
		
		lblsessionDuration = new IA_Label();
		lblsessionDuration.setText("Session duration");
		
		lblnumNewUsers = new IA_Label();
		lblnumNewUsers.setText("Number of new users");
		
		lbluserSessions = new IA_Label(); 
		lbluserSessions.setText("Number of sessions for each user");
		
		
		lblnumAlarms = new IA_Label(); 
		lblnumAlarms.setText("Number of alarms for each priority level");
		
		lbltimeToClearAlarms = new IA_Label();
		lbltimeToClearAlarms.setText("Time to clear alarms of each priority level");
		
		actnumUsers = new OrangeText();
		actnumSessions = new OrangeText();
		actnumActions = new OrangeText();
		actnumScreenViews = new OrangeText();
		actbounceRate = new OrangeText();
		actsessionDuration = new OrangeText();
		actnumNewUsers = new OrangeText();
		actuserSessions = new HistoryTable(Constants.TABLE_USERS_ACTIONS); // count of session for each user
		
		actnumAlarms = new HistoryTable(Constants.TABLE_ALARMS_NUMBERS); //Number of alarms for each priority level
		acttimeToClearAlarms = new HistoryTable(Constants.TABLE_ALARMS_TIME); 
		
		
		
		add(lblnumUsers);
		add(actnumUsers);
		add(lblnumSessions);
		add(actnumSessions);
		add(lblnumActions);
		add(actnumActions);
		add(lblnumScreenViews);
		add(actnumScreenViews);
		add(lblbounceRate);
		add(actbounceRate);
		add(lblsessionDuration);
		add(actsessionDuration);
		add(lblnumNewUsers);
		add(actnumNewUsers);

		add(lbluserSessions);
		JScrollPane _sp1 = new JScrollPane(actuserSessions);
		_sp1.setSize(30, 70);
		add(_sp1);
		
		add(lblnumAlarms);
		JScrollPane _sp2 = new JScrollPane(actnumAlarms);
		_sp2.setSize(30, 70);
		//add(_sp2);
		add(actnumAlarms);
		add(lbltimeToClearAlarms);
		JScrollPane _sp3 = new JScrollPane(acttimeToClearAlarms);
		_sp3.setSize(30, 70);
		//add(_sp3);
		add(acttimeToClearAlarms);
	}

	public int getNumUsers() {
		return numUsers;
	}


	public void setNumUsers(int numUsers) {
		this.numUsers = numUsers;
		this.actnumUsers.setText("" + numUsers);
		
	}

	public int getNumSessions() {
		return numSessions;
	}

	public void setNumSessions(int numSessions) {
		this.numSessions = numSessions;
		this.actnumSessions.setText("" + numSessions);
		
	}


	public int getNumActions() {
		return numActions;
	}

	public void setNumActions(int numActions) {
		this.numActions = numActions;
		this.actnumActions.setText("" + numActions);
	}

	public int getNumScreenViews() {
		return numScreenViews;
	}

	public void setNumScreenViews(int numScreenViews) {
		this.numScreenViews = numScreenViews;
		this.actnumScreenViews.setText("" + numScreenViews);
	}

	public int getBounceRate() {
		return bounceRate;
	}

	public void setBounceRate(int bounceRate) {
		this.bounceRate = bounceRate;
		this.actbounceRate.setText("" + bounceRate);
	}

	public String getSessionDuration() {
		return sessionDuration;
	}


	public void setSessionDuration(String sessionDuration) {
		this.sessionDuration = sessionDuration;
		this.actsessionDuration.setText(sessionDuration);
		
	}

	public int getNumNewUsers() {
		return numNewUsers;
	}


	public void setNumNewUsers(int numNewUsers) {
		this.numNewUsers = numNewUsers;
		this.actnumNewUsers.setText("" + numNewUsers);
	}

	public List<String> getUserSessions() {
		return userSessions;
	}

	public void setUserSessions(List<String> userSessions) {
		this.userSessions = userSessions;
		
		//add data in the table
		DefaultTableModel newData = (DefaultTableModel)actuserSessions.getModel();
		String uName = "";
		String sessionCnt = "";
		int noOfRecords = userSessions.size();
		int i = 0;
		for (i = 0; i < noOfRecords; i++) {
			uName = userSessions.get(i).split(",")[0];
			sessionCnt = userSessions.get(i).split(",")[1];
			
			newData.addRow(new Object[] {uName, sessionCnt});
		}
	
		actuserSessions.setModel(newData);
	}

	public List<String> getNumAlarms() {
		return numAlarms;
	}

	public void setNumAlarms(List<String> numAlarms) {
		this.numAlarms = numAlarms;
		//add data in the table
				DefaultTableModel newData = (DefaultTableModel)actnumAlarms.getModel();
				String pLevel = "";
				String noOfAlarms = "";
				int noOfRecords = numAlarms.size();
				int i= 0;
				int priorityVal;
				for(i=0; i<noOfRecords; i++) {
				
					noOfAlarms = numAlarms.get(i).split(",")[1];
					pLevel = numAlarms.get(i).split(",")[0];
					priorityVal = Integer.parseInt(pLevel.trim());
					switch(priorityVal){
					case 0:
						pLevel = "Diagnostic";
						break;
					case 1:
						pLevel = "Low";
						break;
					case 2:
						pLevel = "Medium";
						break;
					case 3:
						pLevel = "High";
						break;
					case 4:
						pLevel = "Critical";
						break;
					}
					newData.addRow(new Object[] {pLevel, noOfAlarms});
				}
			
				actnumAlarms.setModel(newData);
	
	}

	public List<String> getTimeToClearAlarms() {
		return timeToClearAlarms;
	}

	public void setTimeToClearAlarms(List<String> timeToClearAlarms) {
		this.timeToClearAlarms = timeToClearAlarms;
		
		DefaultTableModel newData = (DefaultTableModel)acttimeToClearAlarms.getModel();
		String pLevel = "";
		String temp = "", timeToClear="";
		int noOfRecords = timeToClearAlarms.size();
		int i= 0;
		int priorityVal;
		for(i=0; i<noOfRecords; i++) {
		
			temp = timeToClearAlarms.get(i).split(",")[1];
			timeToClear = temp.trim().substring(11, 18);
			pLevel = timeToClearAlarms.get(i).split(",")[0];
			priorityVal = Integer.parseInt(pLevel.trim());
			switch(priorityVal){
			case 0:
				pLevel = "Diagnostic";
				break;
			case 1:
				pLevel = "Low";
				break;
			case 2:
				pLevel = "Medium";
				break;
			case 3:
				pLevel = "High";
				break;
			case 4:
				pLevel = "Critical";
				break;
			}
			newData.addRow(new Object[] {pLevel, timeToClear});
		}
		acttimeToClearAlarms.setModel(newData);
	}
	
}


