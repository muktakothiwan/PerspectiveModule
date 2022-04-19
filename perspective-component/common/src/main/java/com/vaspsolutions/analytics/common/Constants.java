package com.vaspsolutions.analytics.common;

import java.awt.Color;

import com.inductiveautomation.ignition.common.Dataset;
/**
 * 
 * Defines project Specific constants
 * @author YM
 *
 */
public class Constants {
	
	public static final String MODULE_ID = "com.vaspsolutions.analytics";
	
	public static final int TODAY = 0;
	public static final int YESTERDAY = 1;
	public static final int LAST_SEVEN_DAYS = 2;
	public static final int LAST_THIRTY_DAYS = 3;
	public static final int LAST_NINTY_DAYS = 4;
	public static final int LAST_365_DAYS = 5;
	public static final int THIS_WEEK = 6;
	public static final int LAST_WEEK = 7;
	public static final int THIS_MONTH = 8;
	public static final int LAST_MONTH = 9;
	public static final int THIS_YEAR = 10;
	public static final int LAST_YEAR = 11;
	public static final int DAY_BEFORE_YESTERDAY = 12;
	public static final int YEAR_BEFORE_LAST = 13;
	//by Omkar
	public static final int FIRST_DAY_OFWEEK = 14;
	public static final int FIRST_DAY_OF_LASTWEEK = 15;
	public static final int LAST_DAY_OF_LASTWEEK = 16;
	public static final int FIRST_DAY_OF_MONTH = 17;
	public static final int LAST_DAY_OF_LAST_MONTH = 18;
	public static final int FIRST_DAY_OF_LAST_MONTH = 19;
	
	//definitions in main menu
	
	public static final String CMD_CONFIGURE = "CONFIGURE";
	public static final String CMD_CONFIGURE_OK = "CONFIGURE_OK";
	public static final String CMD_CONFIGURE_CANCEL = "CONFIGURE_CANCEL";
	public static final String CMD_ANALYZE = "ANALYZE";
	
	
	
	//definitions used in User Panel
	public static final String CMD_SELECT_USER = "selectUser";
	public static final String CMD_SELECT_DURATION = "selectDuration";
	public static final String CMD_CLICK_FIND = "find";
	
	//definitions for table type to be used by Table Headers
	
	public static final int TABLE_ALARMS_NUMBERS = 1;
	public static final int TABLE_ALARMS_TIME = 2;
	public static final int TABLE_USERS_ACTIONS = 3;
	public static final int TABLE_USERS_DURATION = 4;
	
	//action commands on Projects panel
	public static final String CMD_NEW_PROJECT = "CMD_NEW_PROJECT";
	public static final String CMD_ADD_PROJECT = "CMD_ADD_PROJECT";
	public static final String CMD_CANCEL_ADD_PROJECT = "CMD_CANCEL_ADD_PROJECT";
	public static final String CMD_CLOSE_ADD_PROJECT_POPUP = "CMD_CLOSE_ADD_PROJECT_POPUP";
	
	
	//definitions for Sliding Panel
	public static final String CMD_SLIDING_PREV = "SlidingPrev";
	public static final String CMD_SLIDING_NEXT = "SlidingNext";
	
	//definitions on master panel
	
	public static final String CMD_Screen_Name_Click = "SCREEN_NAME_CLICK";
	
	//called from client hook , gateway 
	public static final String SCREEN_OPEN = "SCREEN_OPEN";
	public static final String SCREEN_CLOSE = "SCREEN_CLOSE";
	
	//Action commands for left menu
	public static final String btnProjects_Click_Action = "ProjectsClick";
	public static final String btnDashboard_Click_Action = "DashboardsClick";
	public static final String btnRealTime_Click_Action = "RealTimeClick";
	public static final String btnUsers_Click_Action = "UsersClick";
	public static final String btnReport_Click_Action = "ReportsClick";
	public static final String projectslist_Selection_Change = "ProjectSelection";
	public static final String btnLogout_Click_Action = "logout";
	
	// Actions commands for left menu when on controller
	public static final String ControllerbtnProjects_Click_Action = "ControllerProjectsClick";
	public static final String ControllerbtnDashboard_Click_Action = "ControllerDashboardsClick";
	public static final String ControllerbtnRealTime_Click_Action = "ControllerRealTimeClick";
	public static final String ControllerbtnUsers_Click_Action = "ControllerUsersClick";
	public static final String ControllerbtnReport_Click_Action = "ControllerReportsClick";
	public static final String Controllerprojectslist_Selection_Change = "ControllerProjectSelection";
	public static final String Controllergatewayslist_Selection_Change = "ControllerGatewaySelection";
	
	//constants that define current view 
	public static final int VIEW_PROJECTS = 0;
	public static final int VIEW_DASHOARD = 1;
	public static final int VIEW_REALTIME = 2;
	public static final int VIEW_USERS = 3;
	public static final int VIEW_REPORTS = 4;
	
	
	//Constants that define duration selection command from RealTime and Dashboard panels
	
	public static final String CMD_DURATION_SELECT = "CMD_DURATION_SELECT";
	public static final String BTN_DAY_CLICKED = "BTN_DAY_CLICKED";
	public static final String BTN_WEEK_CLICKED = "BTN_WEEK_CLICKED";
	public static final String BTN_MONTH_CLICKED = "BTN_MONTH_CLICKED";
	
	//constants to define action commands for dashboard buttons
	public static final String CMD_1_DAY_ACTIVE_USERS = "btn1DayActiveUsersClick";
	public static final String CMD_7_DAY_ACTIVE_USERS = "btn7DayActiveUsersClick";
	public static final String CMD_14_DAY_ACTIVE_USERS = "btn14DayActiveUsersClick";
	public static final String CMD_FREQ_CountOfSessions = "CMD_FREQ_CountOfSessions";
	public static final String CMD_RECENCY_DaysSinceLogin = "CMD_RECENCY_DaysSinceLogin";
	public static final String CMD_Engagement_SessionCounts = "CMD_Engagement_SessionCounts";
	public static final String CMD_Engagement_Screendepth = "CMD_Engagement_Screendepth";
	//colour codes 
	public static final Color COLOR_HIGHLIGHT = new Color(14 ,149 ,253);
	//public static final Color COLOR_MENU_BACKGROUND = new Color(89,113,130);
	public static final Color COLOR_MENU_BACKGROUND = new Color(59 ,70 ,78); //Color.DARK_GRAY;
	public static final Color COLOR_BLACK_TEXT = new Color(35,31,32);
	public static final Color COLOR_BLACK_TITLE_BACKGROUND = new Color(36,37,41);
	public static final Color COLOR_MAIN_BACKGROUND = new Color(228,238,245);
	public static final Color COLOR_ORANGE_TEXT = new Color(245, 147, 36);
	public static final Color COLOR_WHITE_BACKGROUND = new Color(250,253,255);
	public static final Color COLOR_GREY_LABEL = new Color(188,189,192);
	public static final Color COLOR_BUTTON_GREY_LABEL = new Color(90,114,130);
	public static final Color COLOR_BUTTON_GREY_BACKGROUND = new Color(89,113,130);
	public static final Color COLOR_COMBO_BACKGROUND = new Color(71,84,94);
	public static final Color COLOR_SLIDE_PANE_GREEN = new Color(78, 176, 10);
	public static final Color COLOR_RED_LABEL_BACKGROUND = new Color(208, 9, 9);
	public static final Color COLOR_TOP_PANEL_BACKGROUND = new Color(51,59,65);
	public static final Color COLOR_BLUE_LABEL = new Color(69,138,201);
	public static final Color COLOR_PLOT_LABEL = new Color(109,110,112);
	public static final Color COLOR_REPORT_ROW = new Color(250,253,255);
	public static final Color COLOR_GRADIENT_LINE = new Color(230,230,230);
	public static final Color COLOR_DISCRIPTOR = new Color(109,110,112);
	
	//Constants for SliderCircle Buttons Command
		public static final  String COMMAND_FIRST_SLIDE_BTN = "COMMAND_FIRST_SLIDE_BTN";
		public static final  String COMMAND_SECOND_SLIDE_BTN = "COMMAND_SECOND_SLIDE_BTN";
		public static final  String COMMAND_THIRD_SLIDE_BTN = "COMMAND_THIRD_SLIDE_BTN";
		public static final  String COMMAND_FOURTH_SLIDE_BTN = "COMMAND_FOURTH_SLIDE_BTN";
		public static final  String COMMAND_FIFTH_SLIDE_BTN = "COMMAND_FIFTH_SLIDE_BTN";
		public static final  String COMMAND_SIXTH_SLIDE_BTN = "COMMAND_SIXTH_SLIDE_BTN";
		public static final  String COMMAND_ALL_SLIDE_BTN = "COMMAND_ALL_SLIDE_BTN";
		
		//by Omkar
		public static final String ACTIVE_USER_REPORT = "ACTIVE_USER_REPORT";
		
		//Yogini 11-04-2016
		public static final Color COLOR_ALARM_ANALYTICS_PANEL_HEADER =  new Color(61,72,79);
		
		public static final int PROJECTS_DELETE = 1;
		public static final int PROJECTS_INSERT = 2;
		
		//Yogini 27-12-2016
		//action commands on Gateway details pane
		public static final String CMD_NEW_GATEWAY = "CMD_NEW_GATEWAY";
		public static final String CMD_RENAME_GATEWAY = "CMD_RENAME_GATEWAY";
		
		public static final String CMD_ADD_GATEWAY = "CMD_ADD_GATEWAY";
		public static final String CMD_CANCEL_ADD_GATEWAY = "CMD_CANCEL_ADD_GATEWAY";
		public static final String CMD_CLOSE_ADD_GATEWAY_POPUP = "CMD_CLOSE_ADD_GATEWAY_POPUP";
		
		//from RenameGatewayPage
		public static final String CMD_SAVE_RENAME_GATEWAY = "CMD_SAVE_RENAME_GATEWAY";
		public static final String CMD_CANCEL_RENAME_GATEWAY = "CMD_CANCEL_RENAME_GATEWAY";
		public static final String CMD_CLOSE_RENAME_GATEWAY_POPUP = "CMD_CLOSE_RENAME_GATEWAY_POPUP";
		//Yogini 29-08-2017 for understandable name in wrapper.log
		public static final String MODULE_LOG_NAME = "Analytics Module";
		//Yogini 31-08-2017
		public static final boolean licenseCheckEnabled = true;
		
		//Yogini  20-Nov-2018
		public static final String geoip_service_1_url = "http://api.ipstack.com/";
		public static final String geoip_service_2_url = "http://api.ipapi.com/";
		public static final String geoip_api_key = "?access_key=e38d18c07d37a1f94a4ffcfcf09a4538";
		
		/* Added by Yogini for last 365 days display logic
		 */
		
		
		public static int binarySearchOnDataset(int columnNo, int searchVal, Dataset dset) {

			int dsSize = 0;

			if (dset != null)
				dsSize = dset.getRowCount();
			int low = 0;
			int high = dsSize - 1;

			while (low <= high) {
				int middle = (low + high) / 2;
//						+ ", dset val = "
//						+ Integer.parseInt(dset.getValueAt(middle, columnNo).toString()));
				if(dset.getValueAt(middle, columnNo) != null)
				{
					if (searchVal > Integer.parseInt(dset.getValueAt(middle, columnNo).toString())) {
						low = middle + 1;
					} else if (searchVal < Integer.parseInt(dset.getValueAt(middle,	columnNo).toString())) {
						high = middle - 1;
					} else { // The element has been found
						return middle;
					}
				}
			}
			return -1;
		}
}
