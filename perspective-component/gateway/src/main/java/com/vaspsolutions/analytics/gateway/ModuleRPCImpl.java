package com.vaspsolutions.analytics.gateway;



import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.project.Project;
import com.inductiveautomation.ignition.common.project.ProjectNotFoundException;
import com.inductiveautomation.ignition.common.user.ContactInfo;
import com.inductiveautomation.ignition.common.user.User;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.gan.GatewayNetworkManager;
import com.inductiveautomation.ignition.gateway.gan.IncomingConnection;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.user.UserSourceManager;
import com.inductiveautomation.ignition.gateway.user.UserSourceProfileRecord;
import com.inductiveautomation.ignition.gateway.util.DBUtilities;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.api.ServiceManager;
import com.inductiveautomation.metro.api.services.ServiceState;
import com.vaspsolutions.analytics.common.ActiveUsersData_Sync;
import com.vaspsolutions.analytics.common.ActiveUsersInfo;
import com.vaspsolutions.analytics.common.AlarmsInformation;
import com.vaspsolutions.analytics.common.AlarmsSummary_Sync;
import com.vaspsolutions.analytics.common.ClientRecord;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ContentsData;
import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.DevicesInformation;
import com.vaspsolutions.analytics.common.GroupReportRecord;
import com.vaspsolutions.analytics.common.LastSyncData;
import com.vaspsolutions.analytics.common.LocationDeviceBrowserCounts;
import com.vaspsolutions.analytics.common.MODIAServiceUnavailableException;
import com.vaspsolutions.analytics.common.ModIAConfigurationException;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.OverviewInformation;
import com.vaspsolutions.analytics.common.Projects_Sync;
import com.vaspsolutions.analytics.common.RealTimeData;
import com.vaspsolutions.analytics.common.ScreenViewsRecord;
import com.vaspsolutions.analytics.common.ScreensCount;
import com.vaspsolutions.analytics.common.SummaryOnScreenViewChange_Sync;
import com.vaspsolutions.analytics.common.UserLocations;
import com.vaspsolutions.analytics.common.UserSessionsInfo;
import com.vaspsolutions.analytics.common.UserSyncRecord;
import com.vaspsolutions.analytics.common.UsersCount;
import com.vaspsolutions.analytics.common.UsersOverviewInformation;
import com.vaspsolutions.analytics.gateway.services.AgentService;
import com.vaspsolutions.analytics.gateway.services.GetAnalyticsInformationService;

import simpleorm.dataset.SQuery;


/**
 * This is the class that has all the methods to retrieve the data using APIs from gatewayContext, database
 * @author YM : Created on 04/06/2015
 * **/
public class ModuleRPCImpl implements ModuleRPC, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GatewayContext mycontext;
	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 
	private Long moduleDS = -1L;
	private String controllerName = "";
	private boolean isAgent; //this is used to check if gateway is agent or controller
	private boolean isEnterprise; //this is used to check if module is in Enterprise mode
	private String installDate = "";
	public ModuleRPCImpl(GatewayContext con){
		this.mycontext = con;
		String strModuleDS = this.getPersistenceRecord();
		if(strModuleDS != null && strModuleDS.length() > 0)
		{
			try
			{
				this.moduleDS = Long.parseLong(strModuleDS);
			}
			catch(Exception e)
			{
				log.error("Module DS is not correctly set");
			}
			
			SRConnection conn = null;
			try {
					Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			
					conn = ds.getConnection(); 
					Dataset resDS = null;
					resDS = conn.runQuery("SELECT Installation_time from mod_ia_db_status");
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							this.installDate = resDS.getValueAt(0, 0).toString();
						}
						else
						{
							this.installDate = "";
						}
					}
					else
					{
						this.installDate = "";
					}
				} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
				finally{
				if(conn!=null){
					try {
						conn.close();
	//					log.error("getAlarms : after con close");
					} catch (SQLException e) {
	//					log.error(" getAlarms : in con close exception.");
					
						e.printStackTrace();
					}
					
					}
				}
		}
		this.isAgent = this.getIfAgent();
		this.isEnterprise = this.getIfEnterprise();
		Long controllerID = this.getControllerId();
		SQuery<IncomingConnection> connectionQ = new SQuery<IncomingConnection>(IncomingConnection.META);
		List<IncomingConnection> results;
		results = mycontext.getPersistenceInterface().query(connectionQ);
		for (IncomingConnection record : results) {
		    if (record.getId() == controllerID)
		    {
		    	
		    	this.controllerName = record.getConnectionId().getSystemName();
//		    	log.error("incoming conn name : " + record.getConnectionId());
		    	break;
		    }		    
		}
	}

	/** Method to get current active sessions length.
	 *  First calls getwaySessionManager API to get active sessions
	 * 	Then for each session, gets the creation time
	 * 	calculates duration as current time - creation time
	 * @author YM
	 * @return List of users with session length
	 * 
	 * YM : Modified the method on 9-March-2017 to count sessions only for monitored projects.
	 */
	@Override
	public List<UserSessionsInfo> getActiveSessions(String projectName, boolean allProjects) {
		
	
		List<UserSessionsInfo> returnList = new ArrayList<UserSessionsInfo>();
		UserSessionsInfo _usInfo;
		
		List<ClientReqSession> sessions = mycontext.getGatewaySessionManager().findSessions();
		if(sessions != null)
		{
			int noOfSessions = sessions.size();
			
			String user;
			long creationTime, timeDifference;
			Date current = new Date();
			Date created;
			String sessionProject = "";
			User currentUser;
			if(noOfSessions > 0)
			{
				if(allProjects == false)
				{
//					log.error("getActiveSessions : allProjects false");
					for(int j=0; j<noOfSessions; j++)
					{
					
						_usInfo = new UserSessionsInfo();
						//sessionProject = sessions.get(j).getAttribute(ClientReqSession.SESSION_PROJECT).toString();
						sessionProject = sessions.get(j).getAttribute(ClientReqSession.SESSION_PROJECT_NAME).toString();
						//sessionProject = this.mycontext.getProjectManager().getProjectName(Long.parseLong(sessionProject), ProjectVersion.Published);
						//sessionProject = this.mycontext.getProjectManager().get
//						log.error("getActiveSessions : sessionProject : " + sessionProject 
//								 + "projectName : " + projectName);
						if(sessionProject.compareToIgnoreCase(projectName) == 0 && this.isProjectMonitored(projectName))
						{
//							log.error("getActiveSessions : project is session project and us monitored");
							creationTime = sessions.get(j).getCreationTime();
							created = new Date(creationTime);
							
							timeDifference = current.getTime() - created.getTime();
							// in milliseconds
							
							currentUser = (User)sessions.get(j).getAttribute(ClientReqSession.SESSION_USER);
//							user = currentUser.get(User.Username) + " , " 
//							+ timeDifference + " , " + creationTime + " , " 
//							+ currentUser.getProfileName(); //also concatenate user profile
							
							_usInfo.setUserName(currentUser.get(User.Username));
							_usInfo.setProfileName(currentUser.getProfileName());
							_usInfo.setCreationTime(creationTime);
							_usInfo.setTimeDifference(timeDifference);
							returnList.add(_usInfo);
							
						}
						
					}
				}
				else
				{
//					log.error("getActiveSessions : allProjects true");
					for(int j=0; j<noOfSessions; j++)
					{
					
						_usInfo = new UserSessionsInfo();
						//sessionProject = sessions.get(j).getAttribute(ClientReqSession.SESSION_PROJECT).toString();
						sessionProject = sessions.get(j).getAttribute(ClientReqSession.SESSION_PROJECT_NAME).toString();
						//sessionProject = this.mycontext.getProjectManager().getProjectName(Long.parseLong(sessionProject), ProjectVersion.Published);
						
//						log.error("user session project : " + sessionProject);
						if(this.isProjectMonitored(sessionProject))
						{
//							log.error("this.isProjectMonitored true for : " + sessionProject);
							creationTime = sessions.get(j).getCreationTime();
							created = new Date(creationTime);
								
							timeDifference = current.getTime() - created.getTime();
							 // in milliseconds
							currentUser = (User)sessions.get(j).getAttribute(ClientReqSession.SESSION_USER);
	//						user = currentUser.get(User.Username) + " , " 
	//						+ timeDifference + " , " + creationTime + " , " 
	//						+ currentUser.getProfileName(); //also concatenate user profile
							
							_usInfo.setUserName(currentUser.get(User.Username));
							_usInfo.setProfileName(currentUser.getProfileName());
							_usInfo.setCreationTime(creationTime);
							_usInfo.setTimeDifference(timeDifference);
							returnList.add(_usInfo);
						}
						else
						{
//							log.error("this.isProjectMonitored false for : " + sessionProject);
						}
				
					}
					
				}
			}
			
		}
		
		
		return returnList;
	}

	

	/**
	 * Method to retrieve users in a project
	 * @return
	 */
	@Override
	public List<String> getAllUsers() {
		
		List<String> users = new ArrayList<String>();
		Iterator<User> retrieveUsers;
		User user;
		String userRec = "";
		Collection<User> profileUsers = null;
		long sysAuthProfile;
		List<Project> _projects = null;
		try
 		{
			
	
			//1st get the configured user profile id from gateway settings page
			sysAuthProfile = mycontext.getSystemProperties().getSystemAuthProfileId();
			
			//then retrive the UserSource Profile and get no of configured users.
			
			//this should work because at a time gateway can have only one default user  profile.
			SQuery<UserSourceProfileRecord> userProfilesQuery = new SQuery<UserSourceProfileRecord>(UserSourceProfileRecord.META);
			List<UserSourceProfileRecord> results;
			results = mycontext.getPersistenceInterface().query(userProfilesQuery);
			for (UserSourceProfileRecord record : results) {
			    String profileName = record.getName();
			    profileUsers = mycontext.getUserSourceManager().getProfile(profileName).getUsers();
			    
			    retrieveUsers = profileUsers.iterator();
	 			while(retrieveUsers.hasNext())
	 			{
	 				user = retrieveUsers.next();
	 				userRec = user.get(User.Username);
	 				users.add(userRec);
	 			}
			}

			
			
			
 			
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		
 		}
		return users;
	}

	
	
	

	/**
	 * method to create the persistent record for module configuration.
	 */
	@Override
	public void createPersistenceRecord(String dsName, String oldDS, String audit, String alarm) {
		
		List<MODIAPersistentRecord> results;
		MODIAPersistentRecord record;
		SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
		results = mycontext.getPersistenceInterface().query(query);
		this.moduleDS = Long.parseLong(dsName);
		try
		{
			//check if record already exists
			if(results != null && results.size() > 0)
			{
				
				record = results.get(0);
				if(record != null)
				{
				//	record.setDbcreated(true);
					record.setDatasource(dsName);
//					record.setExistingAlarmTableName(alarm);
//					record.setExistingAuditTableName(audit);
//					record.setOldDataSource(oldDS);
					mycontext.getPersistenceInterface().save(record);
				}
				else
				{
					//if record exists but is null then create new record
					MODIAPersistentRecord r = mycontext.getPersistenceInterface().createNew(MODIAPersistentRecord.META);
					r.setDatasource(dsName);
//					r.setDbcreated(true);
//					
//					r.setExistingAlarmTableName(alarm);
//					r.setExistingAuditTableName(audit);
//					r.setOldDataSource(oldDS);
					mycontext.getPersistenceInterface().save(r);
				}
				
			}
			else
			{
				//if record does not exist then create new record
				MODIAPersistentRecord r = mycontext.getPersistenceInterface().createNew(MODIAPersistentRecord.META);
	
				//r.setDbcreated(true);
				r.setDatasource(dsName);
//				r.setExistingAlarmTableName(alarm);
//				r.setExistingAuditTableName(audit);
//				r.setOldDataSource(oldDS);
				mycontext.getPersistenceInterface().save(r);
			}
		}catch(Exception e)
		{
			log.error("Create persistent record : " + e);
		}
	}

	

	/**
	 * A function to get count of alarms priority wise
	 * @param duration
	 * @param dataSource
	 * @return
	 */
	@Override
	public Dataset getAlarms(int duration,  String projectName, boolean allProjects) {
		
		Datasource ds;
		Dataset resDS = null;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		String sqlQuery = "";
		SRConnection con = null;
	
		String dateFilter = getDateFilter(duration, "ALARM_DATE");
		int month = getLastMonth();
		int year = getThisYear();
		int thisYear = getThisYear();
		if(month == 0)
		{
			month = 12;
			year = getLastYear();
		}
		
		int lastYear = getLastYear();
		
		sqlQuery = "SELECT ALARM_PRIORITY, SUM(ALARMS_COUNT) "
				+ " FROM MOD_IA_DAILY_ALARMS_SUMMARY WHERE "
				+ dateFilter + " group by ALARM_PRIORITY;";
			
//		log.error("getAlarms sql query : " + sqlQuery);
		
		int r=0;
			try {
					con = ds.getConnection();
					
					resDS = con.runQuery(sqlQuery);
				}
					catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
//					log.error("getAlarms : after con close");
				} catch (SQLException e) {
//					log.error(" getAlarms : in con close exception.");
				
					e.printStackTrace();
				}
				
				}
		}
		return resDS;
	}

	
	
	
	

	

	/**
	 * A function to get information per user about how long the user has not logged in.
	 * @param dataSource data source where audit data is available
	 * @return data set containing records with username and number of days since last login
	 * @see Dataset
	 */
	@Override
	public Dataset getDaysSinceLastLoginPerUser( String projectName, boolean allProjects, int duration) {
		List<String> retList  = new ArrayList<String>();
		Datasource ds ;
		Dataset resDS = null;
	
		
		String startDate = getDayAndTime(duration);
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		String sqlQuery = "";
		SRConnection con = null;
		if(allProjects)
		{
			
//			sqlQuery = "SELECT username, DATEDIFF(DATE('"
//					+ startDate + "'), MAX(DATE(session_start))), sum(no_of_screens) FROM mod_ia_daily_sessions group by username;";
			
			sqlQuery = "SELECT distinct(concat(a.username,b.auth_profile)) as username, DATEDIFF(DATE('"
					+ startDate + "'), MAX(DATE(a.session_start))), sum(a.no_of_screens) FROM mod_ia_daily_sessions a, mod_ia_projects b "
					+ " where a.session_start >= '" + this.installDate + "' and a.project_name = b.project_name group by username;";	
			
		}
		else
		{
//			sqlQuery = "SELECT username, DATEDIFF(DATE(NOW()), MAX(DATE(session_start))) , count(no_of_screens) FROM mod_ia_daily_sessions where "
//					+ dateFilter + " and PROJECT_NAME = '" + projectName +"' group by username;";
			sqlQuery = "SELECT distinct(concat(a.username,b.auth_profile)) as username, DATEDIFF(DATE('"
					+ startDate + "'), MAX(DATE(a.session_start))) , sum(a.no_of_screens) FROM mod_ia_daily_sessions a, mod_ia_projects b where "
					+ " a.PROJECT_NAME = '" + projectName +"' and "
					+ "a.session_start >= '" + this.installDate + "' and a.project_name = b.project_name group by username;";
		}
		
		int r=0;
			try {
//				log.error(" getDaysSinceLastLoginPerUser sql query : " + sqlQuery);
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					
					}
					catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
	
		
			return resDS;
	}

	/**
	 * A function to duplicate the audit and alarm tables for our own module.
	 * It first creates the module specific audit and alarm tables and then copies all data from these tables to our module tables.
	 * @param newDS - new data source where module specific audit tables are to be created
	 * @param oldDS - data source where audit tables exist currently
	 * @param auditTable - name of the audit table
	 * @param alarmTable - name of the alarm table
	 * @return success or failure.
	 */
	@Override
	public boolean createAndPopulateAuditDB(Long newDS, boolean isAgent) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		boolean retVal = false;
		
		SRConnection con1 = null;
		SRConnection con2 = null;
		String newLineChar = System.getProperty("line.separator");
		Datasource ds1, ds2;
		String createTempTable = "";
		
//		log.error("ModuleRPCImpl : isEnterprise " +  this.isEnterprise + ", isAgent : " + this.isAgent 
//				+ ", controller name : " + this.controllerName);
		try {
			
			
			ds1 = mycontext.getDatasourceManager().getDatasource(newDS);
			
			ds2 = mycontext.getDatasourceManager().getDatasource(newDS);
			
			if(ds1 != null && ds2 != null)
			{
				//con1 and Con2 , in case there are different databases,
//				log.error("ds1 = " + ds1.getDescription() + ", ds2 : " + ds2.getDescription());
				con1 = ds1.getConnection();
//				log.error("con1 ... ");
				con2 = ds2.getConnection();
//				log.error("before con2 and con1 null check");
				if(con2 != null && con1 != null)
				{
//					log.error("1");
					createTempTable= "";
					createTempTable = "CREATE TABLE mod_ia_audit_events ("
							+ " `AUDIT_EVENTS_ID` int(11) NOT NULL DEFAULT '0',"
							+ " `EVENT_TIMESTAMP` datetime DEFAULT NULL,"
							+ " `ACTOR` varchar(255) CHARACTER SET latin1 DEFAULT NULL,"
							+ " `ACTOR_HOST` varchar(255) CHARACTER SET latin1 DEFAULT NULL,"
							+ " `ACTION` varchar(255) CHARACTER SET latin1 DEFAULT NULL,"
							+ " `ACTION_TARGET` varchar(255) CHARACTER SET latin1 DEFAULT NULL,"
							+ " `ACTION_VALUE` varchar(255) CHARACTER SET latin1 DEFAULT NULL,"
							+ " `STATUS_CODE` int(11) DEFAULT NULL,"
							+ " `ORIGINATING_SYSTEM` varchar(255) CHARACTER SET latin1 DEFAULT NULL,"
							+ " `ORIGINATING_CONTEXT` int(11) DEFAULT NULL"
							+ " ) ;";
					
					try
					{
//						log.error("mod_ia_audit_events create before.");
						con1.runUpdateQuery(createTempTable);
//						log.error("mod_ia_audit_events create called.");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_audit_events" + s);
					}
					//create the table to store screen views information
					
					
					
					
					String createLocationTable = "CREATE TABLE IF NOT EXISTS MOD_IA_LOCATION_INFO ("
							+ "`LOCATION_ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ "`INTERNAL_IP` varchar(45) DEFAULT NULL,"
							+ "`EXTERNAL_IP` varchar(45) DEFAULT NULL,"
							+ "`CITY` varchar(45) DEFAULT NULL,"
							+ "`STATE` varchar(45) DEFAULT NULL,"
							+ "`COUNTRY` varchar(45) DEFAULT NULL,"
							+ "`LATITUDE` double DEFAULT NULL,"
							+ "`LONGITUDE` double DEFAULT NULL,"
							+ "PRIMARY KEY (`LOCATION_ID`));";
					
					try
					{
						con1.runUpdateQuery(createLocationTable);	
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_location_info" + s);
					}
				//	con1.runUpdateQuery("DROP TABLE IF EXISTS `mod_ia_hours`;");
					//create the dummy hourly table
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_hours` ("
							+ "`Hour` int(11) NOT NULL, PRIMARY KEY (`Hour`));");
						
						con1.runUpdateQuery("INSERT INTO MOD_IA_HOURS VALUES (0),(1),(2),(3),(4),(5),(6),(7),"
								+ "(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23);");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_hours " + s);
					}
					
					
					
					//create table months
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_month` ("
							+ " `monthNumber` int(11) NOT NULL,"
							+ " `monthName` varchar(45) DEFAULT NULL,"
							+ " PRIMARY KEY (`monthNumber`)); ");
						
						con1.runUpdateQuery("INSERT INTO mod_ia_month VALUES (1, 'JAN'),(2, 'FEB'),(3, 'MAR'),(4, 'APR'),(5, 'MAY')"
								+ ",(6, 'JUN'),(7, 'JUL'),(8, 'AUG'),(9, 'SEP'),(10, 'OCT'),(11, 'NOV'),(12, 'DEC');");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_month" + s);
					}
					
					
					
					
					String createBrowserTable = "CREATE TABLE IF NOT EXISTS mod_ia_browser_info ("
							+ " `CLIENT_ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `BROWSER_NAME` varchar(40) DEFAULT NULL,"
							+ " `TIMESTAMP` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
							+ " `IP_ADDRESS` varchar(45) DEFAULT NULL,"
							+ " `BROWSER_VERSION` int(11) DEFAULT NULL,"
							+ " PRIMARY KEY (`CLIENT_ID`));";
					
					try
					{
						con1.runUpdateQuery(createBrowserTable);
				
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_browser_info" + s);
					}
					
					//create the summary tables now.
					
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_hourly_alarms_counts` ("
							+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `ALARM_DATE` date NULL DEFAULT NULL,"
							+ " `ALARM_HOUR` int(11) DEFAULT NULL,"
							+ " `PRIORITY` varchar(45) DEFAULT NULL,"
							+ " `ALARMS_COUNT` int(11) DEFAULT NULL,"
							+ " PRIMARY KEY (`ID`));");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_hourly_alarms_counts" + s);
					}
					
					//table for alarms summary report
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_daily_alarms_summary` ("
							+ " `ALARM_DATE` date DEFAULT NULL,"
							+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `ALARM_NAME` varchar(200) DEFAULT NULL,"
							+ " `ALARM_PRIORITY` varchar(45) DEFAULT NULL,"
							+ " `ALARMS_COUNT` int(11) DEFAULT NULL,"
							+ " `AVG_TIME_TO_CLEAR` time DEFAULT NULL,"
							+ " `AVG_TIME_TO_ACK` time DEFAULT NULL,"
							+ " `TOTAL_ACTIVE_TIME` time DEFAULT NULL,"
							+ " PRIMARY KEY (`ID`));");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_daily_alarms_summary" + s);
					}
					
					//create audit summary tables.
					
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_monthly_active_users` ("
						+ "  `ID` int(11) NOT NULL AUTO_INCREMENT,"
						+ " `MONTH_NO` int(11) DEFAULT NULL,"
						+ " `YEAR` int(11) DEFAULT NULL, "
						+ " `ONE_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
						+ " `SEVEN_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
						+ " `FOURTEEN_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
						+ " `GREATER_ACTIVE_USERS` int(11) DEFAULT NULL,"
						+ " `PROJECT_NAME` varchar(200) DEFAULT NULL,"
						+ " PRIMARY KEY (`ID`));");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_monthly_active_users" + s);
					}
							
					
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_daily_active_users` ("
							+ "  `ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `SUMMARY_DATE` date DEFAULT NULL,"
							+ " `ONE_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `SEVEN_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `FOURTEEN_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `GREATER_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `PROJECT_NAME` varchar(200) DEFAULT NULL,"
							+ " PRIMARY KEY (`ID`));");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_daily_active_users" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_daily_overview` ("
							+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `OVERVIEW_DATE` date DEFAULT NULL,"
							+ " `ACTIONS` int(11) DEFAULT NULL,"
							+ " `AVG_SESSION_DURATION` double DEFAULT NULL,"
							+ " `BOUNCE_RATE` double DEFAULT NULL,"
							+ " `SCREENS_PER_SESSION` double DEFAULT NULL,"
							+ " `TOTAL_SCREENVIEWS` int(11) DEFAULT NULL,"
							+ " `TOTAL_SESSIONS` int(11) DEFAULT NULL,"
							+ " `TOTAL_USERS` int(11) DEFAULT NULL,"
							+ " `NEW_USERS` int(11) DEFAULT NULL,"
							+ " `ACTIONS_PER_SESSION` double DEFAULT NULL,"
							+ " `PROJECT_NAME` varchar(200) DEFAULT NULL,"
							+ "PRIMARY KEY (`ID`));");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_daily_overview" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_daily_sessions` ("
							+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `SESSION_DATE` date DEFAULT NULL,"
							+ " `USERNAME` varchar(200) DEFAULT NULL,"
							+ " `PROJECT_NAME` varchar(200) DEFAULT NULL,"
							+ " `SESSION_START` timestamp NULL DEFAULT NULL,"
							+ " `SESSION_END` timestamp NULL DEFAULT NULL,"
							+ " `SESSION_DURATION` time DEFAULT NULL,"
							+ " `NO_OF_SCREENS` int(11) DEFAULT NULL,"
							+ " `NO_OF_ACTIONS` int(11) DEFAULT NULL,"
							+ "	SESSION_STATUS char(1) DEFAULT 'C',"
							+ "	SESSION_CONTEXT int DEFAULT NULL,"
							+ " HOSTNAME varchar(100) DEFAULT NULL,"
							+ " PRIMARY KEY (`ID`),"
							+ " KEY `DateIndex` (`SESSION_DATE`),"
							+ " KEY `DurationIndex` (`SESSION_DURATION`)"
							+ " );");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_daily_sessions" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_hourly_active_users` ("
							+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `SUMMARY_DATE` date DEFAULT NULL,"
							+ " `SUMMARY_HOUR` int(11) DEFAULT NULL,"
							+ " `ONE_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `SEVEN_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `FOURTEEN_DAY_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `GREATER_ACTIVE_USERS` int(11) DEFAULT NULL,"
							+ " `PROJECT_NAME` varchar(200) DEFAULT NULL,"
							+ " PRIMARY KEY (`ID`));");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_hourly_active_users" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_hourly_overview` ("
							+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `OVERVIEW_DATE` date DEFAULT NULL,"
							+ " `OVERVIEW_HOUR` int(11) DEFAULT NULL,"
							+ " `TOTAL_USERS` int(11) DEFAULT NULL,"
							+ " `TOTAL_SESSIONS` int(11) DEFAULT NULL,"
							+ " `TOTAL_SCREENVIEWS` int(11) DEFAULT NULL,"
							+ " `SCREENS_PER_SESSION` double DEFAULT NULL,"
							+ " `ACTIONS` int(11) DEFAULT NULL,"
							+ " `BOUNCE_RATE` double DEFAULT NULL,"
							+ " `AVG_SESSION_DURATION` double DEFAULT NULL,"
							+ " `PROJECT_NAME` varchar(200) DEFAULT NULL,"
							+ " PRIMARY KEY (`ID`));");
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_hourly_overview" + s);
					}
					
					//create hourly proc
					String createProcStatement = "";
					createProcStatement = " CREATE PROCEDURE `hourlyAlarmsSummary`()" + newLineChar
							+ " BEGIN" + newLineChar
							+ " DECLARE summaryHour INT;" + newLineChar
							+ " DECLARE hourDate DATETIME;" + newLineChar
							+ " DECLARE existingHour INT;" + newLineChar
							+ " select  hour(now()) into summaryHour; " + newLineChar
							+ " select current_date into hourDate;" + newLineChar
							+ " set sql_mode = '';"
							+ " if(summaryHour = -1) then" + newLineChar
							+ " 	set summaryHour = 23;" + newLineChar
							+ "		select date_sub(curDATE(),interval 1 day) into hourDate;" + newLineChar
							+ "	end if;" + newLineChar
							+ " set existingHour = -1;" + newLineChar
							+ " select max(ALARM_HOUR) into existingHour FROM mod_ia_hourly_alarms_counts WHERE DATE(ALARM_DATE) = hourDate;" + newLineChar
							+ " if(existingHour is NULL) then" + newLineChar
							+ "		SET existingHour = summaryHour - 1;" + newLineChar
							+ " end if;" + newLineChar
							+ " if(existingHour != -1 && existingHour <= summaryHour) then" + newLineChar

							+ "		while existingHour <= summaryHour do" + newLineChar
							+ "			delete from mod_ia_hourly_alarms_counts where ALARM_DATE = hourDate and ALARM_HOUR = existingHour;"
							+ "			insert into mod_ia_hourly_alarms_counts(PRIORITY, ALARMS_COUNT, ALARM_DATE, ALARM_HOUR)" + newLineChar
							+ "			select case when priority = 0 then 'Diagnostic'" + newLineChar
							+ "			when priority = 1 then 'Low'" + newLineChar
							+ "			when priority = 2 then 'Medium'" + newLineChar
							+ "			when priority = 3 then 'High'" + newLineChar
							+ "			when priority = 4 then 'Critical'" + newLineChar
							+ "			end as alarm_priority, count(distinct eventid), hourDate, existingHour" + newLineChar
							+ "			from ALARM_EVENTS" + newLineChar
							+ "			where HOUR(eventtime) = existingHour and DATE(eventtime) = hourDate and eventtype = 0" + newLineChar
							+ "			group by priority;" + newLineChar
							+ "			SET existingHour = existingHour + 1;" + newLineChar
							+ "		end while;" + newLineChar
							+ "	end if;" + newLineChar
							
							+ "END"  + newLineChar;  
							
					
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure hourlyAlarmsSummary" + s);
					}
					
					//create daily proc
					createProcStatement = "";
					createProcStatement = " CREATE PROCEDURE `dailyAlarmsSummary`() "
							+ " BEGIN"
							+ " DECLARE dailyDate DATE;"
							+ " DECLARE existingRecordDate DATE;"
							+ " set sql_mode = '';"
							+ " SELECT DATE_SUB(CURDATE(), INTERVAL 0 DAY) INTO dailyDate;"
							
							/* summarize the data in table mod_ia_daily_alarms summary for alarms summary report*/
			
							+ " SELECT MAX(ALARM_DATE) FROM mod_ia_daily_alarms_summary into existingRecordDate; "
							+ " if( existingRecordDate IS NULL) then "
							+ " 	SET existingRecordDate = date_sub(dailyDate, interval 1 day); "
							+ "	end if; "
							+ "	if(existingRecordDate <= dailyDate) then "
							+ "		while existingRecordDate <= dailyDate do "
							+ "			DELETE FROM mod_ia_daily_alarms_summary where ALARM_DATE = existingRecordDate;"
							+ "			"
							+ "			INSERT INTO mod_ia_daily_alarms_summary(ALARM_DATE, ALARM_NAME, ALARM_PRIORITY, ALARMS_COUNT, AVG_TIME_TO_CLEAR, AVG_TIME_TO_ACK, TOTAL_ACTIVE_TIME ) "
							
							+ " select existingRecordDate, tab2.displaypath, case when tab2.priority = 0 then 'Diagnostic' "
							+ "	when tab2.priority = 1 then 'Low' "
							+ " when tab2.priority = 2 then 'Medium' "
							+ " when tab2.priority = 3 then 'High' "
							+ " when tab2.priority = 4 then 'Critical' "
							+ " end as priority	,count(tab2.sourceName), SEC_TO_TIME(AVG(TIME_TO_SEC(tab2.timeToClear))), SEC_TO_TIME(AVG(TIME_TO_SEC(tab3.timeToAck))), SEC_TO_TIME(SUM(TIME_TO_SEC(tab2.timeToClear))) "
							+ " from ( "
							+ " select a.eventid eventidClr, a.priority  priority, a.source  sourceName, a.displaypath displaypath, TIMEDIFF(c.eventtime,a.eventtime ) timeToClear "
							+ " from ALARM_EVENTS a "
							+ " LEFT JOIN ALARM_EVENTS c ON c.eventid = a.eventid AND c.eventtype = 1 "
							+ " where a.eventtype = 0 and DATE(a.eventtime) = existingRecordDate ) as tab2 ,"
							+ " ("
							+ " select a.eventid as eventidAck, a.priority priority,  a.source sourceName, a.displaypath displaypath, TIMEDIFF(c.eventtime,a.eventtime ) timeToAck "
							+ " from ALARM_EVENTS a "
							+ " LEFT JOIN ALARM_EVENTS c on  c.eventid = a.eventid AND c.eventtype = 2 "
							+ " where a.eventtype = 0 and  DATE(a.eventtime) = existingRecordDate ) as tab3 "
							+ " where tab2.eventidClr = tab3.eventidAck group by priority, tab2.sourceName;"

							+ "			SET existingRecordDate = date_add(existingRecordDate, interval 1 day);	"
							+ "		end while;"
							+ "	end if;"
							+ ""
							+ "END";
							
							
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure dailyAlarmsSummary" + s);
					}
					
					//create monthly proc
					createProcStatement = "";
				
					
					
					//create daily overview proc
					
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `daily_overview`(IN projectName VARCHAR(200))"
							+ " BEGIN"
							+ " DECLARE noOfUsers DOUBLE DEFAULT NULL;"
							+ " DECLARE noOfScreens INT DEFAULT NULL;"
							+ " DECLARE noOfActions INT DEFAULT NULL;"
							+ " DECLARE actionsPerSession DOUBLE DEFAULT NULL;"
							+ " DECLARE summaryHour INT DEFAULT NULL;"
							+ " DECLARE dailyDate DATE;"
							+ " DECLARE noOfVisits INT DEFAULT NULL;"
							+ " DECLARE filterString VARCHAR(250);"
							+ " DECLARE screensPerSession DOUBLE DEFAULT NULL;"
							+ " DECLARE avgSessionDuration DOUBLE DEFAULT NULL;"
							+ " DECLARE bounceRate DOUBLE DEFAULT NULL;"
							+ " DECLARE usersOneScreenView DOUBLE DEFAULT NULL;"
							+ " DECLARE noOfNewUsers int DEFAULT NULL;"
							+ "	DECLARE installationDate TIMESTAMP;"
							+ " set sql_mode = '';"
							+ " SET filterString = CONCAT(\"project=\",projectName);"
							+ " SELECT CURDATE() INTO dailyDate;"
							+ " SELECT INSTALLATION_TIME into installationDate from MOD_IA_DB_STATUS;"
							+ " SELECT count(distinct(actor)) into noOfUsers"
							+ " FROM AUDIT_EVENTS"
							+ " where ORIGINATING_SYSTEM = filterString "
							+ " and date(event_timestamp) = dailyDate and event_timestamp >=  installationDate and STATUS_CODE = 0;"
//							+ " SELECT count(action) into noOfActions"
//							+ " FROM AUDIT_EVENTS"
//							+ " where ORIGINATING_SYSTEM = filterString"
//							+ " and date(event_timestamp) = dailyDate and STATUS_CODE = 0;"
//							+ " select count(action) into noOfVisits"
//							+ " from AUDIT_EVENTS"
//							+ " where ORIGINATING_SYSTEM = filterString"
//							+ " and date(event_timestamp) = dailyDate and"
//							+ " action = 'login' and status_code = 0;"
//							+ " select count(screen_name) into noOfScreens"
//							+ " from mod_ia_screen_views where project = projectName"
//							+ " and date(VIEW_timestamp) = dailyDate and ACTION='SCREEN_OPEN';"
//							+ " SET screensPerSession = noOfScreens / noOfVisits;"
							+ " SELECT count(username) into usersOneScreenView FROM"
							+ " (SELECT count(screen_name) noOfScreenViews, username FROM"
							+ " mod_ia_screen_views where  date(view_timestamp) = dailyDate and ACTION='SCREEN_OPEN'"
							+ " and PROJECT = projectName group by username having noOfScreenViews = 1) as dt;"
							+ " SET bounceRate = usersOneScreenView / noOfUsers;"
//							+ " select AVG(TIME_TO_SEC(TIMEDIFF(logoutTime, loginTime))) into avgSessionDuration"
//							+ " from ( select a.actor as actor, max(a.event_timestamp) as loginTime,"
//							+ " b.event_timestamp as logoutTime"
//							+ " from AUDIT_EVENTS a, AUDIT_EVENTS b"
//							+ " where a.actor = b.actor  and a.action = 'login' and b.action = 'logout' and "
//							+ " date(a.event_timestamp) = dailyDate and a.STATUS_CODE = 0 "
//							+ " and a.ORIGINATING_SYSTEM = filterString "
//							+ " and a.event_timestamp < b.event_timestamp  group by logoutTime order by logoutTime desc) as st;"
							
							+ " SELECT count(x.actor) into noOfNewUsers"
							+ " FROM (SELECT distinct(actor) from AUDIT_EVENTS"
							+ " where action = 'login' and status_code = 0 and DATE(event_timestamp) = dailyDate"
							+ " and event_timestamp >= installationDate ) as x,"
							+ " (SELECT actor, min(event_timestamp) as firstLogin"
							+ " from AUDIT_EVENTS where action='login' and event_timestamp >= installationDate  and status_code = 0 group by actor) as y"
							+ " where x.actor = y.actor and DATE(y.firstLogin) = dailyDate;"
							+ " "
//							+ " SET noOfActions = noOfActions + noOfScreens;"
//							+ " SET actionsPerSession = noOfActions / noOfVisits;"
							+ " IF EXISTS(SELECT PROJECT_NAME FROM mod_ia_daily_overview "
							+ " where OVERVIEW_DATE = dailyDate and PROJECT_NAME = projectName) THEN"
							+ " 	UPDATE mod_ia_daily_overview "
							+ "		SET ACTIONS = noOfActions,"
							+ "		ACTIONS_PER_SESSION = actionsPerSession,"
							+ "		AVG_SESSION_DURATION = avgSessionDuration,"
							
							+ "		BOUNCE_RATE = bounceRate,"
							+ "		NEW_USERS = noOfNewUsers,"
							+ "		SCREENS_PER_SESSION = screensPerSession,"
							+ "		TOTAL_SCREENVIEWS = noOfScreens,"
							+ "		TOTAL_SESSIONS = noOfVisits,"
							+ "		TOTAL_USERS = noOfUsers"
							+ "		where OVERVIEW_DATE = dailyDate and PROJECT_NAME = projectName;"
							+ " ELSE"
							+ "		INSERT INTO mod_ia_daily_overview(OVERVIEW_DATE, PROJECT_NAME, "
							+ "		ACTIONS, ACTIONS_PER_SESSION, BOUNCE_RATE, NEW_USERS, SCREENS_PER_SESSION, TOTAL_SCREENVIEWS,"
							+ "		TOTAL_SESSIONS, TOTAL_USERS, AVG_SESSION_DURATION)"
							+ "		VALUES (dailyDate, projectName, noOfActions, actionsPerSession,"
							+ "		bounceRate, noOfNewUsers, screensPerSession,"
							+ "		noOfScreens, noOfVisits, noOfUsers, avgSessionDuration);"
							+ "	END IF;"
							+ " END "; 
							
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure daily_overview" + s);
					}
					
					//create daily active users summary procedure
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `dailyActiveUsersSummary`(IN projectName VARCHAR(200))"
							+ " BEGIN"
							+ " DECLARE dailyDate DATE;"
							+ " DECLARE existingRecordDate DATE;"
							+ " DECLARE userName VARCHAR(200);"
							+ " DECLARE daysSince INT;"
							+ " DECLARE OneDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE SevenDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE FourteenDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE otherDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE filterString VARCHAR(250);"
							+ " DECLARE finished INT DEFAULT 0;"
							+ " DECLARE summaryHour INT;"
							+ " DECLARE hourDate DATETIME;"
							+ " DECLARE installationTime DATETIME;"
							+ " declare activeUsersCursor CURSOR for "
							+ " 	select actor ,"
							+ "		DATEDIFF(DATE(NOW()), MAX(DATE(event_timestamp))) as DaysSinceLogin"
							+ "		FROM AUDIT_EVENTS where action='login' "
							+ "		and ORIGINATING_SYSTEM = filterString and event_timestamp >= installationTime and status_code = 0"
							+ "		group by actor;"
							+ " DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;"
							+ " set sql_mode = '';"
							+ " SET filterString = CONCAT(\"project=\",projectName);"
							+ "	SELECT CURDATE() INTO dailyDate;"
							+ "	SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ "	OPEN activeUsersCursor;"
							+ "	users: loop"
							+ "	FETCH activeUsersCursor into userName, daysSince;"
							+ "	IF finished = 1 then"
							+ "		LEAVE users;"
							+ "	end if;"
							+ "	IF daysSince <= 0 then"
							+ "		SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince >= 1 and daysSince <= 7 then"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ " if daysSince > 7 and daysSince <= 14 then"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince > 14 then "
							+ "		SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "	end if;"
							+ " END LOOP users;"
							+ " IF EXISTS(SELECT  PROJECT_NAME FROM mod_ia_daily_active_users "
							+ " where SUMMARY_DATE = dailyDate and PROJECT_NAME = projectName) THEN"
							+ "		UPDATE mod_ia_daily_active_users"
							+ "		SET ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
							+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
							+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
							+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
							+ "		where SUMMARY_DATE = dailyDate and PROJECT_NAME = projectName;"
							+ "	ELSE"
							+ "		INSERT INTO mod_ia_daily_active_users(SUMMARY_DATE, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (dailyDate, projectName, OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "	END IF;"
							+ "	CLOSE activeUsersCursor;"
							+ " END";
					
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure dailyActiveUsersSummary" + s);
					}
					
					//create daily active users summary for ALL projects.
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `dailyActiveUsersSummaryAllProjects`()"
							+ " BEGIN"
							+ " DECLARE dailyDate DATE;"
							+ " DECLARE existingRecordDate DATE;"
							+ " DECLARE userName VARCHAR(200);"
							+ " DECLARE daysSince INT;"
							+ " DECLARE OneDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE SevenDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE FourteenDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE otherDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE finished INT DEFAULT 0;"
							+ " DECLARE summaryHour INT;"
							+ " DECLARE hourDate DATETIME;"
							+ " DECLARE installationTime DATETIME;"
							+ " declare activeUsersCursor CURSOR for "
							+ " 	select concat(a.actor,b.auth_profile) as actor ,"
							+ "		DATEDIFF(DATE(NOW()), MAX(DATE(event_timestamp))) as DaysSinceLogin"
							+ "		FROM AUDIT_EVENTS a, mod_ia_projects b where a.action='login'"
							+ "		and a.event_timestamp >= installationTime "
							+ "		and a.status_code = 0"
							+ "		and a.ORIGINATING_SYSTEM = concat('project=',b.project_name)"
							+ "		group by concat(a.actor,b.auth_profile);"
							+ " DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;"
							+ " set sql_mode = '';"
							+ "	SELECT CURDATE() INTO dailyDate;"
							+ "	SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ "	OPEN activeUsersCursor;"
							+ "	users: loop"
							+ "	FETCH activeUsersCursor into userName, daysSince;"
							+ "	IF finished = 1 then"
							+ "		LEAVE users;"
							+ "	end if;"
							+ "	IF daysSince <= 0 then"
							+ "		SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince >= 1 and daysSince <= 7 then"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ " if daysSince > 7 and daysSince <= 14 then"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince > 14 then "
							+ "		SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "	end if;"
							+ " END LOOP users;"
							+ " IF EXISTS(SELECT PROJECT_NAME FROM mod_ia_daily_active_users "
							+ " where SUMMARY_DATE = dailyDate and PROJECT_NAME = 'All') THEN"
							+ "		UPDATE mod_ia_daily_active_users"
							+ "		SET ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
							+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
							+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
							+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
							+ "		where SUMMARY_DATE = dailyDate and PROJECT_NAME = 'All';"
							+ "	ELSE"
							+ "		INSERT INTO mod_ia_daily_active_users(SUMMARY_DATE, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (dailyDate, 'All', OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "	END IF;"
							+ "	CLOSE activeUsersCursor;"
							+ " END";
					
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure dailyActiveAll" + s);
					}
					
					
					//create hourly overview proc
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `hourly_overview`(IN projectName varchar(200))"
							+ "	BEGIN"
							+ "	DECLARE noOfUsers INT;"
							+ "	DECLARE noOfScreens INT;"
							+ "	DECLARE noOfActions INT;"
							+ "	DECLARE summaryHour INT;"
							+ "	DECLARE hourDate DATETIME;"
							+ "	DECLARE existingHour INT;"
							+ "	DECLARE noOfVisits INT;"
							+ "	DECLARE filterString VARCHAR(250);"
							+ "	DECLARE screensPerSession DOUBLE;"
							+ "	DECLARE avgSessionDuration DOUBLE;"
							+ "	DECLARE bounceRate DOUBLE;"
							+ "	DECLARE usersOneScreenView INT;"
							+ " DECLARE installationTime DATETIME;"
							+ " set sql_mode = '';"
							+ " SET filterString = CONCAT(\"project=\",projectName);"
							+ " select  hour(now()) - 1 into summaryHour; "
							+ "	select current_date into hourDate;"
							+ "	SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ "	if(summaryHour = -1) then"
							+ "		set summaryHour = 23;"
							+ "		select date_sub(curDATE(),interval 1 day) into hourDate;"
							+ "	end if;"
							+ "	set existingHour = -1;"
							+ "	select max(OVERVIEW_HOUR) into existingHour FROM mod_ia_hourly_overview WHERE DATE(OVERVIEW_DATE) = hourDate;"
							+ " if(existingHour is NULL) then"
							+ "		SET existingHour = summaryHour - 1;"
							+ " end if;"
							+ "	if(existingHour != -1 && existingHour <= summaryHour) then"
				//			+ "		SET existingHour = existingHour + 1;"
							+ "		while existingHour <= summaryHour do"
							+ "			SET noOfUsers = NULL;"
							+ "			SET noOfScreens  = NULL;"
							+ "			SET noOfActions  = NULL;"
							+ "			SET noOfVisits  = NULL;"
							+ "			SET screensPerSession  = NULL;"
							+ "			SET avgSessionDuration  = NULL;"
							+ "			SET bounceRate  = NULL;"
							+ "			SET usersOneScreenView  = NULL;"
							+ ""
							+ "			SELECT count(distinct(actor)) into noOfUsers"
							+ "			FROM AUDIT_EVENTS "
							+ "			where ORIGINATING_SYSTEM = filterString and  HOUR(EVENT_TIMESTAMP) = existingHour "
							+ "			and date(event_timestamp) = hourDate and event_timestamp >= installationTime and status_code = 0;"
							+ ""
//							+ "			SELECT count(action) into noOfActions"
//							+ "			FROM AUDIT_EVENTS "
//							+ "			where ORIGINATING_SYSTEM = filterString and HOUR(EVENT_TIMESTAMP) = existingHour"
//							+ "			and date(event_timestamp) = hourDate and status_code = 0;"
//							+ ""
//							+ "			SELECT count(action) into noOfVisits"
//							+ "			from AUDIT_EVENTS "
//							+ "			where ORIGINATING_SYSTEM = filterString and HOUR(EVENT_TIMESTAMP) = existingHour"
//							+ "			and date(event_timestamp) = hourDate and action = 'login' and status_code = 0;"
//							
//							+ "			"
//							+ "			select count(screen_name) into noOfScreens"
//							+ "			from mod_ia_screen_views"
//							+ "			where project = projectName and HOUR(VIEW_TIMESTAMP) = existingHour "
//							+ "			and date(VIEW_timestamp) = hourDate and ACTION='SCREEN_OPEN';"
//							+ "			"
//							+ "			SET screensPerSession = noOfScreens / noOfVisits;"
							+ "			SELECT count(username) into usersOneScreenView FROM"
							+ "			(SELECT count(screen_name) noOfScreenViews, username FROM"
							+ "			mod_ia_screen_views"
							+ "			where  date(view_timestamp) = hourDate and ACTION='SCREEN_OPEN'"
							+ "			and HOUR(VIEW_TIMESTAMP) = existingHour"
							+ "			and PROJECT = projectName group by username having noOfScreenViews = 1) as dt;"
							+ ""
							+ "			SET bounceRate = usersOneScreenView / noOfUsers;"
//							+ "			select AVG(TIME_TO_SEC(TIMEDIFF(logoutTime, loginTime))) into avgSessionDuration"
//							+ "			from ( select a.actor as actor, max(a.event_timestamp) as loginTime,"
//							+ "			b.event_timestamp as logoutTime "
//							+ "			from AUDIT_EVENTS a, AUDIT_EVENTS b"
//							+ "			where a.actor = b.actor  and a.action = 'login' and b.action = 'logout' and"
//							+ "			date(a.event_timestamp) = hourDate and hour(a.event_timestamp) = existingHour"
//							+ "			and a.ORIGINATING_SYSTEM = filterString and a.status_code = 0 "
//							+ "			and a.event_timestamp < b.event_timestamp  group by logoutTime order by logoutTime desc) as st;"
//							+ "			"
//							
//							+ ""
//							+ "			SET noOfActions = noOfActions + noOfScreens;"
						//	+ "		DELETE FROM MOD_IA_HOURLY_OVERVIEW WHERE OVERVIEW_DATE = hourDate and OVERVIEW_HOUR = existingHour and PROJECT_NAME = projectName;"
							+ " 		IF EXISTS(SELECT PROJECT_NAME FROM MOD_IA_HOURLY_OVERVIEW "
							+ " 		where OVERVIEW_DATE = hourDate and OVERVIEW_HOUR = existingHour and PROJECT_NAME = projectName) THEN "
							+ "			UPDATE MOD_IA_HOURLY_OVERVIEW SET"
							+ "				ACTIONS = noOfActions, "
							+ "				BOUNCE_RATE = bounceRate, "
							+ "				SCREENS_PER_SESSION = screensPerSession, "
							+ "				TOTAL_SCREENVIEWS = noOfScreens,"
							+ "				TOTAL_SESSIONS = noOfVisits, "
							+ "				TOTAL_USERS = noOfUsers,"
							+ "				AVG_SESSION_DURATION = avgSessionDuration"
							+ "			where OVERVIEW_DATE = hourDate and OVERVIEW_HOUR = existingHour and PROJECT_NAME = projectName;"
							+ " 		ELSE "
							+ "			INSERT INTO MOD_IA_HOURLY_OVERVIEW(ACTIONS, BOUNCE_RATE,"
							+ "			OVERVIEW_DATE, OVERVIEW_HOUR, PROJECT_NAME, SCREENS_PER_SESSION, TOTAL_SCREENVIEWS, TOTAL_SESSIONS, TOTAL_USERS, AVG_SESSION_DURATION) values"
							+ "			(noOfActions, bounceRate, hourDate, existingHour, projectName, screensPerSession, noOfScreens, noOfVisits, noOfUsers, avgSessionDuration);"
							+ "		END IF;"	
							+ "		SET existingHour = existingHour + 1;"
							+ "		end while;"
							+ "	end if;"
							+ " select  hour(now()) into summaryHour; "
							+ "	select current_date into hourDate;"
							+ "			SET noOfUsers = NULL;"
							+ "			SET noOfScreens  = NULL;"
							+ "			SET noOfActions  = NULL;"
							+ "			SET noOfVisits  = NULL;"
							+ "			SET screensPerSession  = NULL;"
							+ "			SET avgSessionDuration  = NULL;"
							+ "			SET bounceRate  = NULL;"
							+ "			SET usersOneScreenView  = NULL;"
							+ ""
							+ "			SELECT count(distinct(actor)) into noOfUsers"
							+ "			FROM AUDIT_EVENTS "
							+ "			where ORIGINATING_SYSTEM = filterString and  HOUR(EVENT_TIMESTAMP) = summaryHour "
							+ "			and date(event_timestamp) = hourDate and STATUS_CODE = 0;"
							+ ""
//							+ "			SELECT count(action) into noOfActions"
//							+ "			FROM AUDIT_EVENTS "
//							+ "			where ORIGINATING_SYSTEM = filterString and HOUR(EVENT_TIMESTAMP) = summaryHour"
//							+ "			and date(event_timestamp) = hourDate and STATUS_CODE = 0;"
//							+ ""
//							+ "			SELECT count(action) into noOfVisits"
//							+ "			from AUDIT_EVENTS "
//							+ "			where ORIGINATING_SYSTEM = filterString and HOUR(EVENT_TIMESTAMP) = summaryHour"
//							+ "			and date(event_timestamp) = hourDate and action = 'login' and status_code = 0;"
//							
							+ "			"
//							+ "			select count(screen_name) into noOfScreens"
//							+ "			from mod_ia_screen_views"
//							+ "			where project = projectName and HOUR(VIEW_TIMESTAMP) = summaryHour "
//							+ "			and date(VIEW_timestamp) = hourDate and ACTION='SCREEN_OPEN';"
//							+ "			"
//							+ "			SET screensPerSession = noOfScreens / noOfVisits;"
							+ "			SELECT count(username) into usersOneScreenView FROM"
							+ "			(SELECT count(screen_name) noOfScreenViews, username FROM"
							+ "			mod_ia_screen_views"
							+ "			where  date(view_timestamp) = hourDate and ACTION='SCREEN_OPEN'"
							+ "			and HOUR(VIEW_TIMESTAMP) = summaryHour"
							+ "			and PROJECT = projectName group by username having noOfScreenViews = 1) as dt;"
							+ ""
							+ "			SET bounceRate = usersOneScreenView / noOfUsers;"
//							+ "			select AVG(TIME_TO_SEC(TIMEDIFF(logoutTime, loginTime))) into avgSessionDuration"
//							+ "			from ( select a.actor as actor, max(a.event_timestamp) as loginTime,"
//							+ "			b.event_timestamp as logoutTime "
//							+ "			from AUDIT_EVENTS a, AUDIT_EVENTS b"
//							+ "			where a.actor = b.actor  and a.action = 'login' and b.action = 'logout' and"
//							+ "			date(a.event_timestamp) = hourDate and hour(a.event_timestamp) = summaryHour"
//							+ "			and a.ORIGINATING_SYSTEM = filterString and a.STATUS_CODE = 0"
//							+ "			and a.event_timestamp < b.event_timestamp  group by logoutTime order by logoutTime desc) as st;"
//							+ "			"
//							
//							+ ""
//							+ "			SET noOfActions = noOfActions + noOfScreens;"
							+ " 		IF EXISTS(SELECT PROJECT_NAME FROM MOD_IA_HOURLY_OVERVIEW "
							+ " 		where OVERVIEW_DATE = hourDate and OVERVIEW_HOUR = summaryHour and PROJECT_NAME = projectName) THEN "
							+ "			UPDATE MOD_IA_HOURLY_OVERVIEW SET"
							+ "				ACTIONS = noOfActions, "
							+ "				BOUNCE_RATE = bounceRate, "
							+ "				SCREENS_PER_SESSION = screensPerSession, "
							+ "				TOTAL_SCREENVIEWS = noOfScreens,"
							+ "				TOTAL_SESSIONS = noOfVisits, "
							+ "				TOTAL_USERS = noOfUsers,"
							+ "				AVG_SESSION_DURATION = avgSessionDuration"
							+ "			where OVERVIEW_DATE = hourDate and OVERVIEW_HOUR = summaryHour and PROJECT_NAME = projectName;"
							+ " 		ELSE "
							+ "			INSERT INTO MOD_IA_HOURLY_OVERVIEW(ACTIONS, BOUNCE_RATE,"
							+ "			OVERVIEW_DATE, OVERVIEW_HOUR, PROJECT_NAME, SCREENS_PER_SESSION, TOTAL_SCREENVIEWS, TOTAL_SESSIONS, TOTAL_USERS, AVG_SESSION_DURATION) values"
							+ "			(noOfActions, bounceRate, hourDate, summaryHour, projectName, screensPerSession, noOfScreens, noOfVisits, noOfUsers, avgSessionDuration);"
							+ "			END IF;"
							+ " END"
							+ "	"; 
							
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure hourlyOverview" + s);
					}
					
					//create hourly active users summary proc
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `hourlyActiveUsersSummary`(IN projectName VARCHAR(200))"
							+ " BEGIN"
							+ "	DECLARE userName VARCHAR(200);"
							+ "	DECLARE daysSince INT;"
							+ "	DECLARE OneDayActiveUsers INT ;"
							+ "	DECLARE SevenDayActiveUsers INT;"
							+ "	DECLARE FourteenDaysActiveUsers INT;"
							+ "	DECLARE otherDaysActiveUsers INT;"
							+ "	DECLARE filterString VARCHAR(250);"
							+ "	DECLARE finished INT DEFAULT 0;"
							+ "	DECLARE summaryHour INT;"
							+ "	DECLARE hourDate DATETIME;"
							+ "	DECLARE existingHour INT;"
							+ " DECLARE installationTime DATETIME;"
							+ ""
							+ " declare activeUsersCursor CURSOR for"
							+ "		select actor ,"
							+ "		DATEDIFF(DATE(NOW()), MAX(DATE(event_timestamp))) as DaysSinceLogin "
							+ "		FROM AUDIT_EVENTS where action='login'"
							+ "		and ORIGINATING_SYSTEM = filterString and event_timestamp >= installationTime and status_code = 0"
							+ "		group by actor;"
							+ ""
							+ "		DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;"
							+ ""
							+ " set sql_mode = '';"
							+ "	SET filterString = CONCAT(\"project=\",projectName);"
							+ "	select  hour(now()) into summaryHour; "
							+ " select current_date into hourDate;"
							+ "	SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ "	if(summaryHour = -1) then"
							+ "		set summaryHour = 23;"
							+ "		select date_sub(curDATE(),interval 1 day) into hourDate;"
							+ "	end if;"
							+ " set existingHour = -1;"
							+ "	select max(SUMMARY_HOUR) into existingHour FROM mod_ia_hourly_active_users WHERE DATE(SUMMARY_DATE) = hourDate"
							+ " and PROJECT_NAME = projectName;"
							+ "	if(existingHour is NULL) then"
							+ "		SET existingHour = summaryHour - 1;"
							+ "	end if;"
							+ "	if(existingHour != -1 && existingHour < summaryHour) then"
							+ "		SET existingHour = existingHour + 1;"
							+ "		while existingHour <= summaryHour do"
							+ "			SET otherDaysActiveUsers = 0;"
							+ "			SET OneDayActiveUsers = 0;"
							+ "			SET SevenDayActiveUsers = 0;"
							+ "			SET FourteenDaysActiveUsers = 0;"
							+ "		OPEN activeUsersCursor;"
							+ "		users: loop"
							+ "			FETCH activeUsersCursor into userName, daysSince;"
							+ "			IF finished = 1 then"
							+ "				LEAVE users;"
							+ "			end if;"
							+ "			IF daysSince <= 0 then"
							+ "			SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "			SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "			SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince >= 1 and daysSince <= 7 then"
							+ "				SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 7 and daysSince <= 14 then"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 14 then"
							+ "				SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "			end if;"
							+ "		END LOOP users;"
							//+ "		DELETE FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and PROJECT_NAME = projectName;"
							+ "		IF EXISTS( SELECT PROJECT_NAME FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = projectName) "
							+ "		THEN"
							+ "			UPDATE MOD_IA_HOURLY_ACTIVE_USERS SET "
							+ "			ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
							+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
							+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
							+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
							+ "			WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = projectName;"
							+ "		ELSE "
							+ "		INSERT INTO MOD_IA_HOURLY_ACTIVE_USERS(SUMMARY_DATE, SUMMARY_HOUR, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (hourDate, existingHour, projectName, OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "		END IF;"	
							+ "		CLOSE activeUsersCursor;"
							+ "		"
							+ " 	SET existingHour = existingHour + 1;"
							+ "		end while;"
							+ "	else if (existingHour = summaryHour) then "
							+ "			SET otherDaysActiveUsers = 0;"
							+ "			SET OneDayActiveUsers = 0;"
							+ "			SET SevenDayActiveUsers = 0;"
							+ "			SET FourteenDaysActiveUsers = 0;"
							+ "		OPEN activeUsersCursor;"
							+ "		users: loop"
							+ "			FETCH activeUsersCursor into userName, daysSince;"
							+ "			IF finished = 1 then"
							+ "				LEAVE users;"
							+ "			end if;"
							+ "			IF daysSince <= 0 then"
							+ "			SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "			SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "			SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince >= 1 and daysSince <= 7 then"
							+ "				SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 7 and daysSince <= 14 then"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 14 then"
							+ "				SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "			end if;"
							+ "		END LOOP users;"
							//+ "		DELETE FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = projectName;"
							+ "		IF EXISTS( SELECT PROJECT_NAME FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = projectName) "
							+ "		THEN"
							+ "			UPDATE MOD_IA_HOURLY_ACTIVE_USERS SET "
							+ "			ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
							+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
							+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
							+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
							+ "			WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = projectName;"
							+ "		ELSE"
							+ "		INSERT INTO MOD_IA_HOURLY_ACTIVE_USERS(SUMMARY_DATE, SUMMARY_HOUR, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (hourDate, existingHour, projectName, OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "		END IF;"
							+ "		CLOSE activeUsersCursor;"
							+ " end if;"
							+ "	end if;"
							+ " END"; 
							
				
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure hourlyActiveUsersSummary" + s);
					}
					
					
					//create hourly active users summary proc for All projects
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `hourlyActiveUsersSummaryAll`()"
							+ " BEGIN"
							+ "	DECLARE userName VARCHAR(200);"
							+ "	DECLARE daysSince INT;"
							+ "	DECLARE OneDayActiveUsers INT ;"
							+ "	DECLARE SevenDayActiveUsers INT;"
							+ "	DECLARE FourteenDaysActiveUsers INT;"
							+ "	DECLARE otherDaysActiveUsers INT;"
							+ "	DECLARE finished INT DEFAULT 0;"
							+ "	DECLARE summaryHour INT;"
							+ "	DECLARE hourDate DATETIME;"
							+ "	DECLARE existingHour INT;"
							+ " DECLARE installationTime DATETIME;"
							+ " declare activeUsersCursor CURSOR for"
							+ "		select concat(a.actor,b.auth_profile) as actor ,"
							+ "		DATEDIFF(DATE(NOW()), MAX(DATE(event_timestamp))) as DaysSinceLogin "
							+ "		FROM AUDIT_EVENTS a, mod_ia_projects b where a.action='login'"
							+ "		and a.event_timestamp >= installationTime and a.status_code = 0"
							+ "     and a.ORIGINATING_SYSTEM = concat('project=',b.project_name)"
							+ "		group by concat(a.actor,b.auth_profile);"
							+ ""
							+ "		DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;"
							+ ""
							+ " set sql_mode = '';"
							+ "	select  hour(now()) into summaryHour; "
							+ " select current_date into hourDate;"
							+ "	SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ "	if(summaryHour = -1) then"
							+ "		set summaryHour = 23;"
							+ "		select date_sub(curDATE(),interval 1 day) into hourDate;"
							+ "	end if;"
							+ " set existingHour = -1;"
							+ "	select max(SUMMARY_HOUR) into existingHour FROM mod_ia_hourly_active_users WHERE DATE(SUMMARY_DATE) = hourDate and PROJECT_NAME = 'All';"
							+ "	if(existingHour is NULL) then"
							+ "		SET existingHour = summaryHour - 1;"
							+ "	end if;"
							+ "	if(existingHour != -1 && existingHour < summaryHour) then"
							+ "		SET existingHour = existingHour + 1;"
							+ "		while existingHour <= summaryHour do"
							+ "			SET otherDaysActiveUsers = 0;"
							+ "			SET OneDayActiveUsers = 0;"
							+ "			SET SevenDayActiveUsers = 0;"
							+ "			SET FourteenDaysActiveUsers = 0;"
							+ "		OPEN activeUsersCursor;"
							+ "		users: loop"
							+ "			FETCH activeUsersCursor into userName, daysSince;"
							+ "			IF finished = 1 then"
							+ "				LEAVE users;"
							+ "			end if;"
							+ "			IF daysSince <= 0 then"
							+ "			SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "			SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "			SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince >= 1 and daysSince <= 7 then"
							+ "				SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 7 and daysSince <= 14 then"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 14 then"
							+ "				SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "			end if;"
							+ "		END LOOP users;"
							//+ "		DELETE FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and PROJECT_NAME = 'All';"
							+ "		IF EXISTS( SELECT PROJECT_NAME FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = 'All') "
							+ "		THEN"
							+ "			UPDATE MOD_IA_HOURLY_ACTIVE_USERS SET "
							+ "			ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
							+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
							+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
							+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
							+ "			WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = 'All';"
							+ "		ELSE"
							+ "		INSERT INTO MOD_IA_HOURLY_ACTIVE_USERS(SUMMARY_DATE, SUMMARY_HOUR, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (hourDate, existingHour, 'All', OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "		END IF;"	
							+ "		CLOSE activeUsersCursor;"
							+ "		"
							+ " 	SET existingHour = existingHour + 1;"
							+ "		end while;"
							+ "	else if (existingHour = summaryHour) then "
							+ "			SET otherDaysActiveUsers = 0;"
							+ "			SET OneDayActiveUsers = 0;"
							+ "			SET SevenDayActiveUsers = 0;"
							+ "			SET FourteenDaysActiveUsers = 0;"
							+ "		OPEN activeUsersCursor;"
							+ "		users: loop"
							+ "			FETCH activeUsersCursor into userName, daysSince;"
							+ "			IF finished = 1 then"
							+ "				LEAVE users;"
							+ "			end if;"
							+ "			IF daysSince <= 0 then"
							+ "			SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "			SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "			SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince >= 1 and daysSince <= 7 then"
							+ "				SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 7 and daysSince <= 14 then"
							+ "				SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "			end if;"
							+ "			if daysSince > 14 then"
							+ "				SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "			end if;"
							+ "		END LOOP users;"
						//	+ "		DELETE FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and PROJECT_NAME = 'All';"
						+ "		IF EXISTS( SELECT PROJECT_NAME FROM MOD_IA_HOURLY_ACTIVE_USERS WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = 'All') "
						+ "		THEN"
						+ "			UPDATE MOD_IA_HOURLY_ACTIVE_USERS SET "
						+ "			ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
						+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
						+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
						+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
						+ "			WHERE SUMMARY_DATE = hourDate and SUMMARY_HOUR = existingHour and  PROJECT_NAME = 'All';"
						+ "		ELSE"
							+ "		INSERT INTO MOD_IA_HOURLY_ACTIVE_USERS(SUMMARY_DATE, SUMMARY_HOUR, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (hourDate, existingHour, 'All', OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "	END IF;"
							+ "		CLOSE activeUsersCursor;"
							+ " end if;"
							+ " end if;"
							+ " END"; 
							
				
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure hourlyActiveUsersSummaryAll" + s);
					}
					//drop projects table
				
					
					
				
					
					
					//create daily sessions summary proc
					createProcStatement = ""; 
					createProcStatement = "CREATE PROCEDURE `dailySessionsSummary`(IN projectName VARCHAR(200))"
							+ " BEGIN"
							+ " DECLARE dailyDate DATE;	"
							+ "	DECLARE filterString VARCHAR(250);"
							+ "	declare _NO_OF_SCREENS INT; "
							+ "	declare _SESSION_DATE date; "
							+ "	declare _USERNAME varchar(200); "
							+ " declare _PROJECT_NAME varchar(200);"
							+ "	declare _SESSION_START datetime; "
							+ "	declare _SESSION_END datetime;"
							+ "	declare _SESSION_DURATION time;"
							+ "	declare _NO_OF_ACTIONS int;"
							+ "	declare _SESSION_STATUS char(1);"
							+ "	declare _SESSION_CONTEXT int; "
							+ " DECLARE _HOSTNAME VARCHAR(100);"
							+ "	DECLARE finished INT DEFAULT 0;"
							+ "	DECLARE installationTime DATETIME; "
							+ "	declare dailySessions CURSOR for select NO_OF_SCREENS, USERNAME, NO_OF_ACTIONS, SESSION_START, SESSION_END,"
							+ " SESSION_DURATION, SESSION_DATE, PROJECT_NAME, SESSION_STATUS, SESSION_CONTEXT, HOSTNAME from tempSessions;"
							+ " DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;"
							+ " set sql_mode = '';"
							+ "	SET filterString = CONCAT('project=',projectName);"
							+ "	create temporary table tempSessions (NO_OF_SCREENS INT, SESSION_DATE date, USERNAME varchar(200), PROJECT_NAME varchar(200), SESSION_START datetime, SESSION_END datetime, SESSION_DURATION time, NO_OF_ACTIONS int, SESSION_STATUS char(1), SESSION_CONTEXT int, HOSTNAME VARCHAR(100)) ;"
							+ "	SELECT CURDATE() INTO dailyDate;"
							+ " SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ " INSERT INTO tempSessions(NO_OF_SCREENS, USERNAME, NO_OF_ACTIONS, SESSION_START, SESSION_END, SESSION_DURATION, SESSION_DATE, PROJECT_NAME, SESSION_STATUS, SESSION_CONTEXT, HOSTNAME)"
							+ "	SELECT count(c.screen_name) 'noOfScreens', actor,  actions, loginTime 'sessionStart', logoutTime as 'sessionEnd',"
							+ " timediff(logoutTime, loginTime) as 'sessionDuration', dailyDate, projectName, dt.session_status, dt.originating_context, dt.ACTOR_HOST"
							+ "	FROM mod_ia_screen_views c"
							+ "	RIGHT JOIN "
							+ " ("
							+ " select  actor, actions, loginTime, logoutTime, originating_system , session_status, originating_context, ACTOR_HOST"
							+ " from  (	select d.actor as actor, count(c.action)  as actions, d.loginTime as loginTime, d.logoutTime as logoutTime,"
							+ " d.originating_system as originating_system, 'C' as session_status, d.ORIGINATING_CONTEXT as originating_context, d.ACTOR_HOST as ACTOR_HOST"
							+ " from audit_events c,"
							+ " (	select a.actor as actor, max(a.event_timestamp) as loginTime,"
							+ " b.event_timestamp as logoutTime, a.ORIGINATING_SYSTEM as originating_system,"
							+ " a.ORIGINATING_CONTEXT as originating_context, a.ACTOR_HOST as ACTOR_HOST"
							+ " from AUDIT_EVENTS a ,AUDIT_EVENTS b"
							+ " where a.event_timestamp >=  installationTime and  b.event_timestamp >= installationTime"
							+ " and a.event_timestamp not in (SELECT session_start from MOD_IA_DAILY_SESSIONS where session_status = 'C' and session_date = dailyDate and PROJECT_NAME = projectName)"
							+ " and b.event_timestamp not in (SELECT session_end from MOD_IA_DAILY_SESSIONS where session_status = 'C' and session_date = dailyDate and PROJECT_NAME = projectName)"
							+ " and a.actor = b.actor  and a.action = 'login' and a.STATUS_CODE = 0"
							+ "	and b.action = 'logout' and date(a.event_timestamp) = dailyDate"
							+ " and a.event_timestamp < b.event_timestamp"
							+ " and a.ORIGINATING_SYSTEM = filterString"
							+ " and a.ORIGINATING_SYSTEM = b.ORIGINATING_SYSTEM	 and a.ORIGINATING_CONTEXT = b.ORIGINATING_CONTEXT"
							+ " group by logoutTime , a.actor, a.ORIGINATING_SYSTEM, a.ORIGINATING_CONTEXT, a.ACTOR_HOST order by logoutTime desc) as d"
							+ " where c.event_timestamp >= d.loginTime and c.event_timestamp  <= d.logoutTime"
							+ " and c.originating_system = d.originating_system and c.actor = d.actor 	and c.ACTOR_HOST = d.ACTOR_HOST"
							+ " group by d.logoutTime	, d.actor, d.originating_system, d.loginTime, d.originating_context, d.ACTOR_HOST"
							+ " union"
							+ " ("
							+ " select actor, count(action) as actions, event_timestamp as loginTime, current_timestamp as logoutTime,"
							+ " originating_system	, 'O' as session_status, ORIGINATING_CONTEXT as originating_context, ACTOR_HOST"
							+ " from audit_events"
							+ " where  date(event_timestamp) = dailyDate 	and  event_timestamp >=  installationTime"
							+ " and event_timestamp > (select coalesce(max(event_timestamp),'0000-00-00 00:00:00')"
							+ " from audit_events where date(event_timestamp) = dailyDate and  event_timestamp >=  installationTime and action='logout'"
							+ " and ORIGINATING_SYSTEM = filterString)		and action = 'login' and status_code = 0"
							+ " and ORIGINATING_SYSTEM = filterString"
							+ " and event_timestamp not in (SELECT session_start from MOD_IA_DAILY_SESSIONS where session_status = 'C'"
							+ " and date(event_timestamp) = dailyDate and PROJECT_NAME = projectName)"
							+ " group by actor, event_timestamp, originating_system, ORIGINATING_CONTEXT, ACTOR_HOST)) as tab1"
							+ " where loginTime is not null"
							+ " union"
							+ " select a.actor as actor, count(a.action) as actions, b.SESSION_START as loginTime,"
							+ " a.event_timestamp as logoutTime, a.originating_system as originating_system, 'C' as session_status,"
							+ " a.ORIGINATING_CONTEXT as originating_context, a.ACTOR_HOST as ACTOR_HOST"
							+ " from audit_events a, mod_ia_daily_sessions b"
							+ " where  date(b.session_start) = dailyDate and b.session_start >= installationTime"
							+ " and b.PROJECT_NAME = projectName and a.event_timestamp > b.SESSION_START"
							+ " and a.EVENT_TIMESTAMP = (SELECT EVENT_TIMESTAMP"
							+ " from AUDIT_EVENTS where EVENT_TIMESTAMP > b.SESSION_START and ACTOR = b.USERNAME and ACTOR_HOST = b.HOSTNAME"
							+ " and ACTION = 'login'"
							+ " and ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and ORIGINATING_CONTEXT = b.SESSION_CONTEXT"
							+ " and ACTOR_HOST = b.HOSTNAME order by EVENT_TIMESTAMP"
							+ " asc limit 1)"
							+ " and b.session_status = 'O' and a.ACTION = 'login' and a.ACTOR = b.USERNAME and a.ACTOR_HOST = b.HOSTNAME and a.ORIGINATING_SYSTEM = concat('project=' , b.PROJECT_NAME)"
							+ " and a.ORIGINATING_CONTEXT = b.SESSION_CONTEXT"
							+ " group by a.EVENT_TIMESTAMP, b.SESSION_START,a.ACTOR, a.ORIGINATING_SYSTEM, a.ORIGINATING_CONTEXT, a.ACTOR_HOST"
							+ " ) as dt"
							+ " ON date(c.view_timestamp) = dailyDate"
							+ " and c.view_timestamp <= logoutTime and c.view_timestamp >= loginTime"
							+ " and c.username = actor and c.ACTION = 'SCREEN_OPEN'"
							+ " and dt.ORIGINATING_SYSTEM = concat('project=',c.PROJECT)"
							+ " group by username,loginTime ,logoutTime,dt.actions, dt.session_status, dt.originating_context, dt.actor_host;"
							+ " OPEN dailySessions;"
							+ " sessions: loop"
							+ " FETCH dailySessions into _NO_OF_SCREENS, _USERNAME, _NO_OF_ACTIONS, _SESSION_START, _SESSION_END, _SESSION_DURATION, _SESSION_DATE, _PROJECT_NAME, _SESSION_STATUS, _SESSION_CONTEXT, _HOSTNAME;"
							+ " IF finished = 1 then"
							+ " LEAVE sessions;"
							+ " End If;"
							+ " IF EXISTS (SELECT session_status FROM mod_ia_daily_sessions where session_date = dailyDate and session_start = _SESSION_START and"
							+ " username = _USERNAME and PROJECT_NAME = _PROJECT_NAME and SESSION_CONTEXT = _SESSION_CONTEXT and HOSTNAME = _HOSTNAME)"
							+ " THEN"
							+ " UPDATE mod_ia_daily_sessions"
							+ " SET NO_OF_SCREENS = _NO_OF_SCREENS,"
							+ " NO_OF_ACTIONS = _NO_OF_ACTIONS,"
							+ " SESSION_END = _SESSION_END,"
							+ " SESSION_DURATION = _SESSION_DURATION,"
							+ " SESSION_STATUS = _SESSION_STATUS"
							+ " where session_date = dailyDate and session_start = _SESSION_START and username = _USERNAME"
							+ " and PROJECT_NAME = _PROJECT_NAME and SESSION_CONTEXT = _SESSION_CONTEXT and HOSTNAME = _HOSTNAME;"
							+ " ELSE"
							+ " INSERT INTO mod_ia_daily_sessions(NO_OF_SCREENS, USERNAME, NO_OF_ACTIONS, SESSION_START, SESSION_END, SESSION_DURATION, SESSION_DATE, PROJECT_NAME, SESSION_STATUS, SESSION_CONTEXT, HOSTNAME)"
							+ " VALUES (_NO_OF_SCREENS, _USERNAME, _NO_OF_ACTIONS, _SESSION_START, _SESSION_END, _SESSION_DURATION, _SESSION_DATE, _PROJECT_NAME, _SESSION_STATUS, _SESSION_CONTEXT,_HOSTNAME);"
							+ " END IF;"
							+ " END LOOP sessions;"
							+ " CLOSE dailySessions;"
							+ " drop TEMPORARY table tempSessions;"
							+ " END";
					
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure dailySessionsSummary" + s);
					}
					
					
					//create monthly active users summary procedures
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `monthlyActiveUsersSummary`(IN projectName VARCHAR(200))"
							+ " BEGIN"
							+ " DECLARE dailyDate DATE;"
							+ " DECLARE existingRecordDate DATE;"
							+ " DECLARE userName VARCHAR(200);"
							+ " DECLARE daysSince INT;"
							+ " DECLARE OneDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE SevenDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE FourteenDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE otherDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE filterString VARCHAR(250);"
							+ " DECLARE finished INT DEFAULT 0;"
							+ " DECLARE summaryHour INT;"
							+ " DECLARE hourDate DATETIME;"
							+ " DECLARE installationTime DATETIME;"
							+ " declare activeUsersCursor CURSOR for "
							+ " 	SELECT actor ,"
							+ "		DATEDIFF(DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE)-1 DAY), MAX(DATE(event_timestamp))) as DaysSinceLogin"
							+ "		FROM AUDIT_EVENTS where action='login' "
							+ "		and ORIGINATING_SYSTEM = filterString and event_timestamp >= installationTime and status_code = 0"
							+ "		group by actor;"
							+ " DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;"
							+ " set sql_mode = '';"
							+ " SET filterString = CONCAT(\"project=\",projectName);"
							+ "	SELECT CURDATE() INTO dailyDate;"
							+ "	SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ "	OPEN activeUsersCursor;"
							+ "	users: loop"
							+ "	FETCH activeUsersCursor into userName, daysSince;"
							+ "	IF finished = 1 then"
							+ "		LEAVE users;"
							+ "	end if;"
							+ "	IF daysSince <= 0 then"
							+ "		SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince >= 1 and daysSince <= 7 then"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ " if daysSince > 7 and daysSince <= 14 then"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince > 14 then "
							+ "		SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "	end if;"
							+ " END LOOP users;"
							+ " IF EXISTS(SELECT PROJECT_NAME FROM mod_ia_monthly_active_users "
							+ " where month_no = month(dailyDate) and year = year(dailyDate) and PROJECT_NAME = projectName) THEN"
							+ "		UPDATE mod_ia_monthly_active_users"
							+ "		SET ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
							+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
							+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
							+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
							+ "		where month_no = month(dailyDate) and year = year(dailyDate) and PROJECT_NAME = projectName;"
							+ "	ELSE"
							+ "		INSERT INTO mod_ia_monthly_active_users(MONTH_NO, YEAR, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (month(dailyDate), year(dailyDate), projectName, OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "	END IF;"
							+ "	CLOSE activeUsersCursor;"
							+ " END";
					
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure monthlyActiveUsersSummary" + s);
					}
					
					//create monthly active users summary for ALL projects.
					createProcStatement = "";
					createProcStatement = "CREATE PROCEDURE `monthlyActiveUsersSummaryAllProjects`()"
							+ " BEGIN"
							+ " DECLARE dailyDate DATE;"
							+ " DECLARE existingRecordDate DATE;"
							+ " DECLARE userName VARCHAR(200);"
							+ " DECLARE daysSince INT;"
							+ " DECLARE OneDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE SevenDayActiveUsers INT DEFAULT 0;"
							+ " DECLARE FourteenDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE otherDaysActiveUsers INT DEFAULT 0;"
							+ " DECLARE finished INT DEFAULT 0;"
							+ " DECLARE summaryHour INT;"
							+ " DECLARE hourDate DATETIME;"
							+ " DECLARE installationTime DATETIME;"
							+ " declare activeUsersCursor CURSOR for "
							+ " 	select concat(a.actor,b.auth_profile) as actor ,"
							+ "		DATEDIFF(DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE)-1 DAY), MAX(DATE(event_timestamp))) as DaysSinceLogin"
							+ "		FROM AUDIT_EVENTS a, mod_ia_projects b where a.action='login' "
							+ "		and a.event_timestamp >= installationTime and a.status_code = 0"
							+ "		and a.ORIGINATING_SYSTEM = concat('project=',b.project_name)"
							+ "		group by concat(a.actor,b.auth_profile);"
							+ " DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;"
							+ " set sql_mode = '';"
							+ "	SELECT CURDATE() INTO dailyDate;"
							+ "	SELECT INSTALLATION_TIME INTO installationTime from MOD_IA_DB_STATUS;"
							+ "	OPEN activeUsersCursor;"
							+ "	users: loop"
							+ "	FETCH activeUsersCursor into userName, daysSince;"
							+ "	IF finished = 1 then"
							+ "		LEAVE users;"
							+ "	end if;"
							+ "	IF daysSince <= 0 then"
							+ "		SET OneDayActiveUsers = OneDayActiveUsers + 1;"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince >= 1 and daysSince <= 7 then"
							+ "		SET SevenDayActiveUsers = SevenDayActiveUsers + 1;"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ " if daysSince > 7 and daysSince <= 14 then"
							+ "		SET FourteenDaysActiveUsers = FourteenDaysActiveUsers + 1;"
							+ "	end if;"
							+ "	if daysSince > 14 then "
							+ "		SET otherDaysActiveUsers = otherDaysActiveUsers + 1;"
							+ "	end if;"
							+ " END LOOP users;"
							+ " IF EXISTS(SELECT PROJECT_NAME FROM mod_ia_monthly_active_users "
							+ " where month_no = month(dailyDate) and year = year(dailyDate) and PROJECT_NAME = 'All') THEN"
							+ "		UPDATE mod_ia_monthly_active_users"
							+ "		SET ONE_DAY_ACTIVE_USERS = OneDayActiveUsers,"
							+ "			SEVEN_DAY_ACTIVE_USERS = SevenDayActiveUsers,"
							+ "			FOURTEEN_DAY_ACTIVE_USERS = FourteenDaysActiveUsers,"
							+ "			GREATER_ACTIVE_USERS = otherDaysActiveUsers"
							+ "		where month_no = month(dailyDate) and year = year(dailyDate) and PROJECT_NAME = 'All';"
							+ "	ELSE"
							+ "		INSERT INTO mod_ia_monthly_active_users(MONTH_NO, YEAR, PROJECT_NAME, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS, GREATER_ACTIVE_USERS)"
							+ "		VALUES (month(dailyDate), year(dailyDate), 'All', OneDayActiveUsers, SevenDayActiveUsers, FourteenDaysActiveUsers, otherDaysActiveUsers);"
							+ "	END IF;"
							+ "	CLOSE activeUsersCursor;"
							+ " END";
					try
					{
						con1.runUpdateQuery(createProcStatement);
					}
					catch(SQLException s)
					{
						log.error("Error creating procedure monthlyActiveUsersSummaryAll" + s);
					}
					//changes end
					
					
					
					createProcStatement = "CREATE TABLE mod_ia_db_status ("
							+ " `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,"
							+ " Installation_time timestamp NULL );";
							//+ " PRIMARY KEY (`update_time`));";
					
					try
					{
						con1.runUpdateQuery(createProcStatement);
						
						String sDate = sdf.format(new Date());
						con1.runUpdateQuery("INSERT into mod_ia_db_status (update_time,Installation_time) VALUES (CURRENT_TIMESTAMP, '" + sDate + "');");
						
					}
					catch(SQLException s)
					{
						log.error("Error creating table mod_ia_db_status" + s);
					}
					
					
					if(isEnterprise)
					{
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_last_sync ("
								+ "  last_sync_date date DEFAULT NULL,"
								+ "  last_sync_hour int(11) DEFAULT NULL,"
								+ "  last_sync_sessions_timestamp timestamp NULL DEFAULT NULL,"
								+ "  last_sync_screen_timestamp timestamp NULL DEFAULT NULL,"
								+ "  last_synch_client_start_timestamp timestamp NULL DEFAULT NULL,"
								+ "  last_sync_daily_alarms_date date DEFAULT NULL,"
								+ "  last_sync_hourly_alarms_date date DEFAULT NULL,"
								+ "  last_sync_hourly_alarms_hour int(11) DEFAULT NULL,"
								+ "  last_sync_browsers_timestamp timestamp NULL DEFAULT NULL,"
								+ "  last_sync_hourly_aUsers_date date DEFAULT NULL,"
								+ "  last_sync_hourly_aUsers_hour int(11) DEFAULT NULL,"
								+ "  last_sync_daily_aUsers_date date DEFAULT NULL,"
								+ "  last_sync_monthly_aUsers_month int(11) DEFAULT NULL,"
								+ "  last_sync_monthly_aUsers_year int(11) DEFAULT NULL,"
								+ "  last_sync_audit timestamp NULL DEFAULT NULL,"
								+ "  last_sync_mod_ia_audit timestamp NULL DEFAULT NULL "
								+ " ) ;";
						try
						{
							con1.runUpdateQuery(createTempTable);
							
							con1.runUpdateQuery("INSERT INTO mod_ia_last_sync (last_sync_browsers_timestamp, last_sync_daily_alarms_date, last_sync_daily_aUsers_date,"
									+ " last_sync_date, last_sync_hour, last_sync_hourly_alarms_date, last_sync_hourly_alarms_hour, last_sync_hourly_aUsers_date,"
									+ " last_sync_hourly_aUsers_hour, last_sync_monthly_aUsers_month, last_sync_monthly_aUsers_year, last_sync_screen_timestamp,"
									+ " last_synch_client_start_timestamp, last_sync_audit,last_sync_mod_ia_audit, last_sync_sessions_timestamp ) VALUES ( null, null, null, null,null,null,null,null,null,null,null,"
									+ "null, null,null,null,null );");
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_last_sync" + e);
						}
					
					}
					//create required indices
					
					try
					{
						con1.runUpdateQuery("CREATE INDEX OriginatingSystemIndex ON AUDIT_EVENTS (ORIGINATING_SYSTEM) USING BTREE;");
					}
					catch(SQLException s)
					{
						log.error("Error creating index OriginatingSystemIndex" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE INDEX eventidIndex ON ALARM_EVENTS (eventid) USING BTREE;");
					}
					catch(SQLException s)
					{
						log.error("Error creating index eventidindex" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE INDEX eventtimeIndex ON ALARM_EVENTS (eventtime) USING BTREE;");
					}
					catch(SQLException s)
					{
						log.error("Error creating index eventtimeindex" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE INDEX eventtypeIndex ON ALARM_EVENTS (eventtype) USING BTREE;");
					}
					catch(SQLException s)
					{
						log.error("Error creating index eventtypeindex" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE INDEX IndxTime ON mod_ia_browser_info (TIMESTAMP) USING BTREE;");
					}
					catch(SQLException s)
					{
						log.error("Error creating index IndxTime" + s);
					}
					
					
					
					
					// following initialization is required on the Controller , to create aggregate tables.
					
					if(isEnterprise && this.isAgent == false) //means we are on controller so create following schema
					{

						//Yogini 14-Sept : added for Rename Gateway feature 
						createProcStatement = "";
						createProcStatement = " CREATE PROCEDURE updateGatewayName(IN oldName VARCHAR(200), IN newName VARCHAR(200))" + newLineChar
								+ " BEGIN"
								+ " delete from mod_ia_monitored_gateways where GAN_ServerName = oldName;"
								+ " update mod_ia_gateways set Is_Renamed = 1 where GAN_ServerName = oldName;"
								+ " update mod_ia_aggregates_projects set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_actions set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_audit_events set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_browser_info set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_clients set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_daily_active_users set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_daily_alarm_ack_counts set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_daily_alarm_active_counts set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_daily_alarms_summary set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_daily_overview set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_daily_screen_views set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_daily_sessions set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_hourly_active_users set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_hourly_alarms_counts set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_hourly_overview set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_location_info set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_monthly_active_users set GATEWAY_ID = newName where GATEWAY_ID = oldName;"
								+ " update mod_ia_aggregates_users set GATEWAY_ID = newName where GATEWAY_ID = oldName;" 
								+ " END"  ;  
								
						
						try
						{
							con1.runUpdateQuery(createProcStatement);
						}
						catch(SQLException s)
						{
							log.error("Error creating procedure updateGatewayName" + s);
						}
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_browser_info ( "
								+ "  ID int(11) NOT NULL AUTO_INCREMENT, "
								+ "  BROWSER_NAME varchar(40) DEFAULT NULL, "
								+ "  TIMESTAMP timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
								+ "  IP_ADDRESS varchar(45) DEFAULT NULL,"
								+ "  BROWSER_VERSION int(11) DEFAULT NULL,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID),"
								+ "  KEY IndxAggTime (TIMESTAMP) USING BTREE "
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_browser_info" + e);
						}
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_clients ( "
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  HOSTNAME varchar(100) DEFAULT NULL,"
								+ "  HOST_INTERNAL_IP varchar(45) DEFAULT NULL,"
								+ "  HOST_EXTERNAL_IP varchar(45) DEFAULT NULL,"
								+ "  OS_NAME varchar(45) DEFAULT NULL,"
								+ "  OS_VERSION varchar(45) DEFAULT NULL,"
								+ "  IS_MOBILE bit(1) DEFAULT NULL,"
								+ "  BROWSER varchar(45) DEFAULT NULL,"
								+ "  USERNAME varchar(100) DEFAULT NULL,"
								+ "  START_TIMESTAMP timestamp NULL DEFAULT CURRENT_TIMESTAMP,"
								+ "  PROJECT varchar(100) DEFAULT NULL,"
								+ "  SCREEN_RESOLUTION varchar(45) DEFAULT NULL,"
								+ "  CLIENT_CONTEXT int DEFAULT NULL,"
								+ "  GATEWAY_ID varchar(100) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID),"
								+ "  KEY IndexAggTimestamp (START_TIMESTAMP) USING BTREE"
								+ "  ); ";
						
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_clients " + e);
						}
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_daily_active_users ( "
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  SUMMARY_DATE date DEFAULT NULL,"
								+ "  ONE_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  SEVEN_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  FOURTEEN_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  GREATER_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  PROJECT_NAME varchar(200) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ " );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_daily_active_users" + e);
						}
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_daily_alarms_summary ( "
								+ "  ALARM_DATE date DEFAULT NULL,"
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  ALARM_NAME varchar(200) DEFAULT NULL,"
								+ "  ALARM_PRIORITY varchar(45) DEFAULT NULL,"
								+ "  ALARMS_COUNT int(11) DEFAULT NULL,"
								+ "  AVG_TIME_TO_CLEAR time DEFAULT NULL,"
								+ "  AVG_TIME_TO_ACK time DEFAULT NULL,"
								+ "  TOTAL_ACTIVE_TIME time DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_daily_alarms_summary" + e);
						}
						
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_daily_overview ("
								+ "  ID int(11) NOT NULL AUTO_INCREMENT, "
								+ "  OVERVIEW_DATE date DEFAULT NULL,"
								+ "  ACTIONS int(11) DEFAULT NULL,"
								+ "  AVG_SESSION_DURATION double DEFAULT NULL,"
								+ "  BOUNCE_RATE double DEFAULT NULL,"
								+ "  SCREENS_PER_SESSION double DEFAULT NULL,"
								+ "  TOTAL_SCREENVIEWS int(11) DEFAULT NULL,"
								+ "  TOTAL_SESSIONS int(11) DEFAULT NULL,"
								+ "  TOTAL_USERS int(11) DEFAULT NULL,"
								+ "  NEW_USERS int(11) DEFAULT NULL,"
								+ "  ACTIONS_PER_SESSION double DEFAULT NULL,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  GATEWAY_DOWN_TIME double DEFAULT NULL,"
								+ "  PROJECT_NAME varchar(100) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_daily_overview" + e);
						}
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_daily_screen_views ("
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  USERNAME varchar(100) DEFAULT NULL,"
								+ "  SCREEN_NAME varchar(100) DEFAULT NULL,"
								+ "  SCREEN_PATH varchar(200) DEFAULT NULL,"
								+ "  VIEW_TIMESTAMP timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
								+ "  SCREEN_TITLE varchar(100) DEFAULT NULL,"
								+ "  ACTION varchar(45) DEFAULT NULL,"
								+ "  PROJECT varchar(45) DEFAULT NULL,"
								+ "  GATEWAY_ID varchar(45) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_daily_screen_views" + e);
						}
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_daily_sessions("
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  SESSION_DATE date DEFAULT NULL,"
								+ "  USERNAME varchar(200) DEFAULT NULL,"
								+ "  PROJECT_NAME varchar(200) DEFAULT NULL,"
								+ "  SESSION_START timestamp NULL DEFAULT NULL,"
								+ "  SESSION_END timestamp NULL DEFAULT NULL,"
								+ "  SESSION_DURATION time DEFAULT NULL,"
								+ "  NO_OF_SCREENS int(11) DEFAULT NULL,"
								+ "  NO_OF_ACTIONS int(11) DEFAULT NULL,"
								+ "	 SESSION_STATUS char(1) DEFAULT 'C',"
								+ "  SESSION_CONTEXT int DEFAULT NULL,"
								+ "  HOSTNAME varchar(100) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID),"
								+ "  KEY AggDateIndex (SESSION_DATE),"
								+ "  KEY AggDurationIndex (SESSION_DURATION)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_daily_sessions" + e);
						}
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_hourly_active_users ("
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  SUMMARY_DATE date DEFAULT NULL,"
								+ "  SUMMARY_HOUR int(11) DEFAULT NULL,"
								+ "  ONE_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  SEVEN_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  FOURTEEN_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  GREATER_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  PROJECT_NAME varchar(200) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ " );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_hourly_active_users" + e);
						}
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_hourly_alarms_counts("
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  ALARM_DATE timestamp NULL DEFAULT NULL,"
								+ "  ALARM_HOUR int(11) DEFAULT NULL,"
								+ "  PRIORITY varchar(45) DEFAULT NULL,"
								+ "  ALARMS_COUNT int(11) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_hourly_alarms_counts" + e);
						}
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_hourly_overview ("
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  OVERVIEW_DATE date DEFAULT NULL,"
								+ "  OVERVIEW_HOUR int(11) DEFAULT NULL,"
								+ "  TOTAL_USERS int(11) DEFAULT NULL,"
								+ "  TOTAL_SESSIONS int(11) DEFAULT NULL,"
								+ "  TOTAL_SCREENVIEWS int(11) DEFAULT NULL,"
								+ "  SCREENS_PER_SESSION double DEFAULT NULL,"
								+ "  ACTIONS int(11) DEFAULT NULL,"
								+ "  BOUNCE_RATE double DEFAULT NULL,"
								+ "  AVG_SESSION_DURATION double DEFAULT NULL,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  PROJECT_NAME varchar(200) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ ");";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_hourly_overview " + e);
						}
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_monthly_active_users ("
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  MONTH_NO int(11) DEFAULT NULL,"
								+ "  YEAR int(11) DEFAULT NULL,"
								+ "  ONE_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  SEVEN_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  FOURTEEN_DAY_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  GREATER_ACTIVE_USERS int(11) DEFAULT NULL,"
								+ "  PROJECT_NAME varchar(200) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_monthly_active_users" + e);
						}
						
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_aggregates_projects ( "
								+ "  ID int(11) NOT NULL AUTO_INCREMENT,"
								+ "  PROJECT_NAME varchar(200) NOT NULL,"
								+ "  AUTH_PROFILE varchar(200) DEFAULT NULL,"
								+ "  GATEWAY_ID varchar(200) DEFAULT NULL,"
								+ "  PRIMARY KEY (ID)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_projects " + e);
						}
						
						createTempTable = "";
						createTempTable = "CREATE TABLE mod_ia_gateways ("
								+ "  GAN_ServerId varchar(200) NOT NULL,"
								+ "  GAN_ServerName varchar(200) DEFAULT NULL,"
								+ "  GAN_ServerState varchar(45) DEFAULT NULL,"
								+ "  GAN_ServiceState varchar(45) DEFAULT NULL,"
								+ "  Is_Renamed int(1) DEFAULT 0,"
								+ "  PRIMARY KEY (GAN_ServerId)"
								+ "  );";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_gateways" + e);
						}
						
						/* table to store last synch times */
//						createTempTable = "";
//						createTempTable = "CREATE TABLE `mod_ia_last_sync` ("
//								+ "  `last_sync_date` date DEFAULT NULL,"
//								+ "  `last_sync_hour` int(11) DEFAULT NULL,"
//								+ "  `last_sync_screen_timestamp` timestamp NULL DEFAULT NULL,"
//								+ "  `last_synch_client_start_timestamp` timestamp NULL DEFAULT NULL,"
//								+ "  `last_sync_daily_alarms_date` date DEFAULT NULL,"
//								+ "  `last_sync_hourly_alarms_date` date DEFAULT NULL,"
//								+ "  `last_sync_hourly_alarms_hour` int(11) DEFAULT NULL,"
//								+ "  `last_sync_browsers_timestamp` timestamp NULL DEFAULT NULL,"
//								+ "  `last_sync_hourly_aUsers_date` date DEFAULT NULL,"
//								+ "  `last_sync_hourly_aUsers_hour` int(11) DEFAULT NULL,"
//								+ "  `last_sync_daily_aUsers_date` date DEFAULT NULL,"
//								+ "  `last_sync_monthly_aUsers_month` int(11) DEFAULT NULL,"
//								+ "  `last_sync_monthly_aUsers_year` int(11) DEFAULT NULL );";
//						try
//						{
//							con1.runUpdateQuery(createTempTable);
//						}
//						catch(SQLException e)
//						{
//							log.error("Error creating Table mod_ia_last_sync" + e);
//						}
						
						/* table to store monitored gateways  */
						createTempTable = "";
						createTempTable = "CREATE TABLE `mod_ia_monitored_gateways` ("
								+ "  `GAN_ServerId` int(11) NOT NULL AUTO_INCREMENT,"
								+ "  `GAN_ServerName` varchar(200) DEFAULT NULL,"
								+ "  PRIMARY KEY (`GAN_ServerId`));";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_monitored_gateways" + e);
						}
						
						
						/* table to store location synced data */
						createTempTable = "";
						createTempTable = "CREATE TABLE `mod_ia_aggregates_location_info` ( "
								+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
								+ " `GATEWAY_ID` varchar(45) DEFAULT NULL,"
								+ " `INTERNAL_IP` varchar(45) DEFAULT NULL,"
								+ " `EXTERNAL_IP` varchar(45) DEFAULT NULL,"
								+ " `CITY` varchar(45) DEFAULT NULL, "
								+ " `STATE` varchar(45) DEFAULT NULL,"
								+ " `COUNTRY` varchar(45) DEFAULT NULL,"
								+ " `LATITUDE` double DEFAULT NULL,"
								+ " `LONGITUDE` double DEFAULT NULL,"
								+ " PRIMARY KEY (`ID`)) ;";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_location_info" + e);
						}
						
						
						/* table to store alarm ack counts */
						createTempTable = "";
						createTempTable = " CREATE TABLE `mod_ia_aggregates_daily_alarm_ack_counts` ("
								+ " `ID` int(11) NOT NULL AUTO_INCREMENT, "
								+ " `GATEWAY_ID` varchar(45) DEFAULT NULL, "
								+ " `ALARM_DATE` date DEFAULT NULL, "
								+ " `PRIORITY` varchar(45) DEFAULT NULL, "
								+ " `ACK_COUNT` int(11) DEFAULT NULL, "
								+ "  PRIMARY KEY (`ID`));";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_daily_alarm_ack_counts" + e);
						}
						
						
						
						/* table to store alarm active counts */
						createTempTable = "";
						createTempTable = "CREATE TABLE `mod_ia_aggregates_daily_alarm_active_counts` ("
								+ " `ID` int(11) NOT NULL AUTO_INCREMENT,"
								+ " `GATEWAY_ID` varchar(45) DEFAULT NULL, "
								+ " `ALARM_DATE` date DEFAULT NULL, "
								+ " `PRIORITY` varchar(45) DEFAULT NULL, "
								+ " `ACTIVE_COUNT` int(11) DEFAULT NULL, "
								+ "  PRIMARY KEY (`ID`)) ;";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_daily_alarm_active_counts" + e);
						}
						
						
						/* table to store users data*/
						createTempTable = "";
						createTempTable = "CREATE TABLE `mod_ia_aggregates_users` ("
								+ " `id` int(11) NOT NULL AUTO_INCREMENT, "
								+ " `gateway_id` varchar(100) DEFAULT NULL, "
								+ " `email` varchar(100) DEFAULT NULL, "
								+ " `username` varchar(100) DEFAULT NULL, "
								+ " `roles` varchar(100) DEFAULT NULL, "
								+ " `phone` varchar(45) DEFAULT NULL, "
								+ " `gateway_userprofile` varchar(100) DEFAULT NULL, "
								+ " `current_location` varchar(100) DEFAULT NULL, "
								+ " `first_seen` varchar(100) NULL DEFAULT NULL, "
								+ " `last_seen` varchar(100) NULL DEFAULT NULL, "
								+ " `last7days_duration` varchar(100) DEFAULT NULL, "
								+ " `last7days_visits` int(11) DEFAULT NULL, "
								+ " `last7days_actions` int(11) DEFAULT NULL, "
								+ " `all_actions` int(11) DEFAULT NULL, "
								+ " `all_screen_views` int(11) DEFAULT NULL, "
								+ " `current_screen` varchar(200) DEFAULT NULL, "
								+ " `online_status` varchar(45) DEFAULT NULL, "
								+ " `projectname` varchar(200) DEFAULT NULL, "
								+ " PRIMARY KEY (`id`) ) ;";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_users" + e);
						}
						
						
						/* table to store actions*/
						createTempTable = "";
						createTempTable = "CREATE TABLE `mod_ia_aggregates_actions` ("
								+ "  `ID` int(11) NOT NULL AUTO_INCREMENT,"
								+ "  `EVENT_TIMESTAMP` datetime DEFAULT NULL,"
								+ "  `ACTOR` varchar(100) DEFAULT NULL,"
								+ "  `ACTION` varchar(100) DEFAULT NULL,"
								+ "  `ACTION_VALUE` varchar(1000) DEFAULT NULL,"
								+ "  `PROJECT` varchar(100) DEFAULT NULL,"
								+ "  `GATEWAY_ID` varchar(100) DEFAULT NULL,"
								+ "  `ACTION_TARGET` varchar(1000) DEFAULT NULL,"
								+ "  PRIMARY KEY (`ID`));";
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_actions" + e);
						}
						
						/* Table to store gateway start stop time */
						createTempTable = "";
						createTempTable = "CREATE TABLE `mod_ia_aggregates_audit_events` ("
								+ " `EVENT_TIMESTAMP` datetime DEFAULT NULL, "
								+ " `ACTOR` varchar(255) DEFAULT NULL, "
								+ " `ACTOR_HOST` varchar(255) DEFAULT NULL, "
								+ " `ACTION` varchar(255) DEFAULT NULL, "
								+ " `ACTION_TARGET` varchar(255) DEFAULT NULL, "
								+ " `ACTION_VALUE` varchar(255) DEFAULT NULL, "
								+ "  `STATUS_CODE` int(11) DEFAULT NULL, "
								+ "  `ORIGINATING_SYSTEM` varchar(255) DEFAULT NULL, "
								+ "  `ORIGINATING_CONTEXT` int(11) DEFAULT NULL, "
								+ "   GATEWAY_ID varchar(255)  DEFAULT NULL );";
						
						try
						{
							con1.runUpdateQuery(createTempTable);
						}
						catch(SQLException e)
						{
							log.error("Error creating Table mod_ia_aggregates_audit_events" + e);
						}
						
						
//						createProcStatement = " create TRIGGER agent_sync "
//								+ "	AFTER INSERT on mod_ia_aggregates_daily_screen_views "
//								+ "	FOR EACH ROW "
//								+ " BEGIN"
//								+ " 	update mod_ia_db_status set update_time = current_timestamp;"
//								+ "		END;";
//						
//						try
//						{
//							con1.runUpdateQuery(createProcStatement);
//						}
//						catch(SQLException s)
//						{
//							log.error("Error creating trigger agent_sync" + s);
//						}
						
						// Add required indexes 
						createProcStatement = "CREATE INDEX alarmNameIndex ON mod_ia_aggregates_daily_alarms_summary (ALARM_NAME) USING BTREE;";
						
						try
						{
							con1.runUpdateQuery(createProcStatement);
						}
						catch(SQLException s)
						{
							log.error("Error creating index alarmNameIndex" + s);
						}
						
						createProcStatement = "CREATE INDEX alarmDateIndex ON mod_ia_aggregates_daily_alarms_summary (ALARM_DATE) USING BTREE;";
						
						try
						{
							con1.runUpdateQuery(createProcStatement);
						}
						catch(SQLException s)
						{
							log.error("Error creating index alarmDateIndex" + s);
						}
						
						createProcStatement = "CREATE INDEX gatewayIDIndex ON mod_ia_aggregates_daily_alarms_summary (GATEWAY_ID) USING BTREE;";
						
						try
						{
							con1.runUpdateQuery(createProcStatement);
						}
						catch(SQLException s)
						{
							log.error("Error creating index gatewayIDIndex" + s);
						}
					}
					
//					create projects table that will hold list of projects monitored by IgnitionAnalytics.
					try
					{
						con1.runUpdateQuery("CREATE TABLE `mod_ia_projects` ("
							//+ "  `id` int(11) NOT NULL AUTO_INCREMENT,"
							+ " `PROJECT_NAME` varchar(200) NOT NULL,"
							+ " `AUTH_PROFILE` varchar(200) DEFAULT NULL, "
							+ " PRIMARY KEY (`PROJECT_NAME`));");
					
						//get the list of projects from Gateway and insert into the table.
						//List<Project> projects = mycontext.getProjectManager().getProjectsLite(ProjectVersion.Published);
						List<String> projects = mycontext.getProjectManager().getProjectNames();
						UserSourceManager _u = mycontext.getUserSourceManager();
						int noOfProjects = 0;
						String insertProjectStmt = "";
						String deleteProjectStmt = ""; 
						if(projects != null)
						{
							noOfProjects = projects.size();
							deleteProjectStmt = "DELETE FROM mod_ia_projects;";
							List<Projects_Sync> _projectsList = new ArrayList<Projects_Sync>();
							Projects_Sync _p ;
							
							insertProjectStmt = "INSERT INTO mod_ia_projects (PROJECT_NAME, AUTH_PROFILE) VALUES ('";
							for(int n=0; n<noOfProjects; n++)
							{
								if(n == noOfProjects - 1)
								{
									insertProjectStmt = insertProjectStmt + projects.get(n) 
										+ "','" + mycontext.getProjectManager().getProjectProps(projects.get(n)).getAuthProfileName();
								
								}
								else
								{
									insertProjectStmt = insertProjectStmt + projects.get(n) 
										+ "','" + mycontext.getProjectManager().getProjectProps(projects.get(n)).getAuthProfileName()
										+ "') , ('";
								}
								_p = new Projects_Sync();
								_p.authProfile = mycontext.getProjectManager().getProjectProps(projects.get(n)).getAuthProfileName();
								_p.projectName = projects.get(n);
								_projectsList.add(_p);
							}
							insertProjectStmt = insertProjectStmt + "');";
						
							con1.runUpdateQuery(deleteProjectStmt);
							con1.runUpdateQuery(insertProjectStmt);
							
							//call service on controller to sync projects data
							GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
							if(this.isAgent)
							{
								ServiceManager sm = gm.getServiceManager();
								ServerId sid = new ServerId(this.controllerName);
								ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
//								log.error("after projects insert : on agent sid = " + sid.getServerName());
								//if service is available
								if(s == ServiceState.Available)
								{
									//call the service 
									sm.getService(sid, GetAnalyticsInformationService.class).get().receiveProjects(Constants.PROJECTS_INSERT, gm.getServerAddress().getServerName(), _projectsList);
								}
							}
							else
							{
//								log.error("after projects insert : on controller " );
								//try to insert directly.
								//this.receiveProjects(Constants.PROJECTS_INSERT, gm.getServerAddress().getServerName(), _projectsList);
								int noOfP = _projectsList.size();
								String gatewayID = gm.getServerAddress().getServerName();
								insertProjectStmt = "INSERT INTO MOD_IA_AGGREGATES_PROJECTS (`AUTH_PROFILE`, `GATEWAY_ID`, `PROJECT_NAME`) VALUES (";;
								for(int i = 0 ; i < noOfP; i++)
								{
									if(i==0)
									{
										insertProjectStmt = insertProjectStmt + "'" + _projectsList.get(i).authProfile + "' ,"
												+ "'" + gatewayID + "',"
												+ "'" + _projectsList.get(i).projectName + "')";
									}
									else
									{
										insertProjectStmt = insertProjectStmt + ", ('" + _projectsList.get(i).authProfile + "' ,"
												+ "'" + gatewayID + "',"
												+ "'" + _projectsList.get(i).projectName + "')";
									}
								}
								insertProjectStmt = insertProjectStmt + ";";
								con1.runUpdateQuery(insertProjectStmt);
							}
							
						}
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_projects" + s);
					}
					String createScreensTable = "CREATE TABLE IF NOT EXISTS MOD_IA_SCREEN_VIEWS ("
							+ "`AUDIT_EVENTS_ID` int(11) NOT NULL AUTO_INCREMENT, "
							+ "`USERNAME` varchar(100) DEFAULT NULL, "
							+ "`SCREEN_NAME` varchar(100) DEFAULT NULL, "
							+ "`SCREEN_PATH` varchar(200) DEFAULT NULL, "
							+ "`VIEW_TIMESTAMP` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
							+ "`SCREEN_TITLE` varchar(100) DEFAULT NULL, "
							+ "`ACTION` varchar(45) DEFAULT NULL, "
							+ "`PROJECT` varchar(45) DEFAULT NULL, "
							+ " PRIMARY KEY (`AUDIT_EVENTS_ID`),"
							+ " FOREIGN KEY(PROJECT) references mod_ia_projects(PROJECT_NAME));";;
					try
					{	
						con1.runUpdateQuery(createScreensTable);
					}
					catch(SQLException s)
					{
						log.error("Error creating MOD_IA_SCREEN_VIEWS" + s);
					}
					//create the table to store otehr client information
					
					String createClientTable = "CREATE TABLE IF NOT EXISTS MOD_IA_CLIENTS ("
							+ "`CLIENT_ID` int(11) NOT NULL AUTO_INCREMENT,"
							+ "`HOSTNAME` varchar(100) DEFAULT NULL,"
							+ "`HOST_INTERNAL_IP` varchar(45) DEFAULT NULL,"
							+ "`HOST_EXTERNAL_IP` varchar(45) DEFAULT NULL,"
							+ "`OS_NAME` varchar(45) DEFAULT NULL,"
							+ "`OS_VERSION` varchar(45) DEFAULT NULL,"
							+ "`IS_MOBILE` bit(1) DEFAULT NULL,"
							+ "`BROWSER` varchar(45) DEFAULT NULL,"
							+ "`USERNAME` varchar(100) DEFAULT NULL,"
							+ "`START_TIMESTAMP` timestamp NULL DEFAULT CURRENT_TIMESTAMP,"
							+ "`PROJECT` varchar(100) DEFAULT NULL,"
							+ "`SCREEN_RESOLUTION` varchar(45) DEFAULT NULL,"
							+ "CLIENT_CONTEXT int DEFAULT NULL,"
							+ "PRIMARY KEY (`CLIENT_ID`),"
							+ " FOREIGN KEY(PROJECT) references mod_ia_projects(PROJECT_NAME));";
					
					try
					{
						con1.runUpdateQuery(createClientTable);	
					}
					catch(SQLException s)
					{
						log.error("Error creating mod_ia_clients" + s);
					}
					
					try
					{
						con1.runUpdateQuery("CREATE INDEX IndexTimestamp ON mod_ia_clients (START_TIMESTAMP) USING BTREE;");
					}
					catch(SQLException s)
					{
						log.error("Error creating index IndexTimestamp" + s);
					}
					retVal = true;
					
					
				}
				else
				{
					log.error("Could not get connection to either old or new Data Source");
				}
			}
			} catch (SQLException e) {
				log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		finally{
		try {
			if(con1 != null)
			{
				con1.close();
			}
			if(con2 != null)
			{
				con2.close();
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
			log.error(e.getMessage());
		}
		}
		
		return retVal;
	}

	

	/**
	 * Method to retrieve module configuration from Persistent Record System
	 * @author YM
	 * @return Persistence record containing module configuration
	 */
	@Override
	public String getPersistenceRecord() {
			
		String retStr = null;
		List<MODIAPersistentRecord> results;
		MODIAPersistentRecord record;
		SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
		results = mycontext.getPersistenceInterface().query(query);
		
		
		
		if(results != null && results.size() > 0)
		{
			
			record = results.get(0);
			if(record != null)
			{
//				if(record.getDbcreated() == true)
//				{
					// retStr = record.getDatasource();
				retStr = record.getConnectionid() + "";
					//retStr = retStr + ", " + record.getOldDataSource();
					//retStr = retStr + ", " + record.getExistingAuditTableName();
					//retStr = retStr + ", " + record.getExistingAlarmTableName();
//					log.error("in getPersistentREcord : ds name " + retStr );
//					log.error("in getPersistentREcord : contriller name " + record.getControllerconnectionid());
//				}
//				else
//				{
//					retStr = null;
//				}
			}
			else
			{
				retStr = null;
			}
		}
		else
		{
			retStr = null;
		}
	return retStr;
	}

	
	
	/**
	 * Method to find number of logged in users in a specified duration.
	 * @param duration - time frame
	 * @param dataSource - name of the data source where data is stored
	 * @return number of logged in users in given time frame
	 */
	@Override
	public int getNumberOfActiveUsers(int duration, String projectName, boolean allProjects) {
		int numActUsers = 0;
		Datasource ds ;
		Dataset resDS = null;
	
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		//noOfDS = dsList.size();
		String sqlQuery = "";
		SRConnection con = null;
		String dateFilter = getDateFilter(duration, "event_timestamp");	
		if(allProjects)
		{
//			sqlQuery = "SELECT distinct(actor) FROM AUDIT_EVENTS where action=\"login\" and "
//					+ dateFilter + ";";
//			
			sqlQuery = "SELECT distinct(CONCAT(a.actor,b.AUTH_PROFILE))"
					+ " from AUDIT_EVENTS a,  mod_ia_projects b"
					+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
					+ " and a.actor != 'SYSTEM' and action = 'login' and status_code = 0 and "
					+ dateFilter + " ;"; 
		}
		else
		{
			sqlQuery = "SELECT distinct(actor) FROM AUDIT_EVENTS where action=\"login\" and status_code = 0 and  "
					+ dateFilter + " and ORIGINATING_SYSTEM like '%"
					+ projectName + "';";
		}
		
		
		
			try {
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					
					if(resDS != null)
					{
						numActUsers = resDS.getRowCount();
					}
			}
				catch (SQLException e) {
				
				log.error(e);
			
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		return numActUsers;
	}

	/**
	 * * Method to retrieve average time to clear alarms per priority
	 * @author YM
	 * @param duration
	 * @param dataSource
	 * @return List containing Priority wise average time.
	 */
	@Override
	public Dataset getAlarmsClearTime(int duration, String projectName, boolean allProjects) {
		
		
		Datasource ds;
		Dataset resDS = null;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		String sqlQuery = "";
		SRConnection con = null;
		String dateFilter = getDateFilter(duration,"ALARM_DATE");	
		int month = getLastMonth();
		int year = getThisYear();
		int thisYear = getThisYear();
		if(month == 0)
		{
			month = 12;
			year = getLastYear();
		}
		
		int lastYear = getLastYear();
		
			sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_CLEAR,0.0)))), '%H:%i:%s')"
					+ " FROM MOD_IA_DAILY_ALARMS_SUMMARY WHERE "
					+ dateFilter + " group by ALARM_PRIORITY";
		int r=0;
			try {
				
					con = ds.getConnection();
					
					resDS = con.runQuery(sqlQuery);
					
					
					}
					catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		
		return resDS;
	}

	/**
	 * Method to retrieve average time to acknowledge alarms per priority
	 * @author YM
	 * @param duration
	 * @param dataSource
	 * @return Dataset with Priority wise average time.
	 */
	@Override
	public Dataset getAlarmsAckTime(int duration, String projectName, boolean allProjects) {
		Datasource ds;
		Dataset resDS = null;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		String sqlQuery = "";
		SRConnection con = null;
		String dateFilter = getDateFilter(duration, "ALARM_DATE");	
		
		int month = getLastMonth();
		int year = getThisYear();
		int thisYear = getThisYear();
		if(month == 0)
		{
			month = 12;
			year = getLastYear();
		}
		
		int lastYear = getLastYear();
		
		
		//if(allProjects)
		//{
//		switch(duration)
//		{
//			case Constants.TODAY:
//			case Constants.YESTERDAY:
//				sqlQuery = " select case when priority = 0 then 'Diagnostic'"
//						+ " when priority = 1 then 'Low' "
//						+ " when priority = 2 then 'Medium' "
//						+ " when priority = 3 then 'High'"
//						+ " when priority = 4 then 'Critical'"
//						+ " end as alarm_priority, SEC_TO_TIME(AVG(TIME_TO_SEC(timetoAck)))"
//						+ " from (select a.priority as priority, TIMEDIFF(b.eventtime , a.eventtime) as timetoAck"
//						+ " from ALARM_EVENTS a, ALARM_EVENTS b"
//						+ "	where a.eventid = b.eventid and a.eventtype = 0 and b.eventtype = 2"
//						+ "	and a.eventtime < b.eventtime and"
//						+ " DATE(b.eventtime) = date(now()) ) as dt group by priority;";
//				break;
//			case Constants.LAST_SEVEN_DAYS:
//			case Constants.LAST_WEEK:
//			case Constants.LAST_THIRTY_DAYS:
//			case Constants.LAST_NINTY_DAYS:
//			case Constants.LAST_365_DAYS:
//			case Constants.THIS_WEEK:
//			case Constants.THIS_MONTH:
//				sqlQuery = "SELECT PRIORITY, SEC_TO_TIME(AVG(AVG_TIME))"
//						+ " FROM mod_ia_daily_alarms_ack_time"
//						+ " where "
//						+ dateFilter + " group by priority;";
//				break;
//			case Constants.LAST_MONTH:
//				
//				sqlQuery = "SELECT PRIORITY, SEC_TO_TIME(AVG(AVG_TIME))"
//						+ " FROM mod_ia_monthly_alarms_ack_time"
//						+ " where ALARM_MONTH = " + month + " and ALARM_YEAR = "
//						+ year + " group by priority;";
//				break;
//			case Constants.LAST_YEAR:
//				sqlQuery = "SELECT PRIORITY, SEC_TO_TIME(AVG(AVG_TIME))"
//						+ " FROM mod_ia_monthly_alarms_ack_time"
//						+ " where ALARM_YEAR = "
//						+ lastYear + " group by priority;";
//				break;
//			case Constants.THIS_YEAR:
//				sqlQuery = "SELECT PRIORITY, SEC_TO_TIME(AVG(AVG_TIME))"
//						+ " FROM mod_ia_monthly_alarms_ack_time"
//						+ " where ALARM_YEAR = "
//						+ thisYear + " group by priority;";
//				break;
//		
//		}
		sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_ACK,0.0)))), '%H:%i:%s')"
				+ " FROM MOD_IA_DAILY_ALARMS_SUMMARY WHERE "
				+ dateFilter + " group by ALARM_PRIORITY";	
		//}
		/*else
		{
			sqlQuery = "select priority, SEC_TO_TIME(AVG(TIME_TO_SEC(timetoAck))) "
					+ "from (select a.priority as priority, TIMEDIFF(b.eventtime , a.eventtime) as timetoAck "
					+ "from ALARM_EVENTS a, ALARM_EVENTS b "
					+ "where a.eventid = b.eventid and a.eventtype = 0 and b.eventtype = 2 and "
					+ dateFilter +  " and ORIGINATING_SYSTEM like '%" + projectName + "') as dt "
					+ "group by priority;";
		}
		
		*/
		
		int r=0;
			try {
				
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					
					}
					catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		
		
		return resDS;
	}

	

	/*** method to retrieve entire user profiles ***/
	@Override
	public Collection<User> getUserProfiles() {
	
		Collection<User> profileUsers = new ArrayList<User>();
		
		long sysAuthProfile;
		try
 		{
			//1st get the configured user profile id from gateway settings page
			//sysAuthProfile = mycontext.getSystemProperties().getSystemAuthProfileId();
			//then retrive the UserSource Profile and get no of configured users.
			
			//profileUsers = mycontext.getUserSourceManager().getProfile(sysAuthProfile).getUsers();
			SQuery<UserSourceProfileRecord> userProfilesQuery = new SQuery<UserSourceProfileRecord>(UserSourceProfileRecord.META);
			List<UserSourceProfileRecord> results;
			results = mycontext.getPersistenceInterface().query(userProfilesQuery);
			for (UserSourceProfileRecord record : results) {
			    String profileName = record.getName();
			  /*  if(profileUsers == null)
			    {
			    	profileUsers = mycontext.getUserSourceManager().getProfile(profileName).getUsers();
			    	log.error("in get user profiles : null userProiles " );
			    }
			    else
			    {*/
			    	
			    	profileUsers.addAll(mycontext.getUserSourceManager().getProfile(profileName).getUsers());
			    //}
			}

			 
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		
 		}
		return profileUsers;
	}

	/*** method to retrieve first login for user 
	 * @author YM : 04/17/2015 
	 * @param ds - data source name
	 * @param userName - user name
	 * 
	 * @return  date-time of user's first login
	 * ***/

	private String getFirstSeen(String userName, String projectName, boolean allProjects, String userProfile) {
		String firstSeen = "";
		Datasource _ds ; //local variable
		Dataset resDS = null;
	
		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		//noOfDS = dsList.size();
		
		String sqlQuery = "";
		if (allProjects)
		{
		
			sqlQuery = "SELECT min(event_timestamp) "
				+ " from AUDIT_EVENTS where action='login' and status_code = 0 "
				+ "	and event_timestamp >= '"+this.installDate+ "'"
				+ " and actor='" + userName.trim() + "'"
				+ " and SUBSTRING(ORIGINATING_SYSTEM,9) in"
				+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '"
				+ userProfile + "');";
		}
		else
		{
			sqlQuery = "SELECT min(event_timestamp) "
					+ " from AUDIT_EVENTS where action='login' and status_code = 0 "
					+ " and event_timestamp >= '"+this.installDate+"'"
					+ " and actor='" + userName.trim() + "'"
					+ " and SUBSTRING(ORIGINATING_SYSTEM,9) = '" + projectName + "' and SUBSTRING(ORIGINATING_SYSTEM,9) in"
					+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '"
					+ userProfile + "');";
		}
//		log.error("get first seen q : " + sqlQuery);	
		SRConnection con = null;
			
		try {
			
				con = _ds.getConnection();
				resDS = con.runQuery(sqlQuery);
				
				if(resDS != null && resDS.getRowCount() > 0)
				{
					if(resDS.getValueAt(0, 0) != null)
					{
						firstSeen = resDS.getValueAt(0, 0).toString().trim();
					}
				}
		}
		catch (SQLException e) {
			
			log.error(e);
			
		}
		finally{
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
				
					e.printStackTrace();
				}
				
				}
		}
		
		return firstSeen;
	}

	
	/*** method to retrieve last login for user 
	 * @author YM : 04/17/2015 
	 * @param ds - data source name
	 * @param userName - user name
	 * 
	 * @return date-time of user's last login 
	 * ***/
	@Override
	 public String getLastSeen(String userName, String projectName, boolean allProjects, String userProfile) {
		String lastSeen = "";
		Datasource _ds ; //local variable
		Dataset resDS = null;
		log.debug("in get last seen");
		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		//noOfDS = dsList.size();
		String sqlQuery = "";
		if(allProjects)
		{
			sqlQuery= "SELECT max(event_timestamp) "
				+ " from AUDIT_EVENTS where actor='" + userName.trim() + "'"
				+ " and event_timestamp >= '"+this.installDate+"'"
				+ " and SUBSTRING(ORIGINATING_SYSTEM,9) in"
				+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '"
				+ userProfile.trim() + "' and STATUS_CODE = 0);";
		}
		else
		{
			sqlQuery= "SELECT max(event_timestamp) "
					+ " from AUDIT_EVENTS where actor='" + userName.trim() + "'"
					+ " and event_timestamp >= '"+this.installDate+"'"
					+ " and SUBSTRING(ORIGINATING_SYSTEM,9) = '" + projectName + "' and SUBSTRING(ORIGINATING_SYSTEM,9) in"
					+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '"
					+ userProfile.trim() + "' and STATUS_CODE = 0);";
		}
			SRConnection con = null;
		try {
				con = _ds.getConnection();
				
				resDS = con.runQuery(sqlQuery);
				if(resDS != null && resDS.getRowCount() > 0)
				{
					
					if(resDS.getValueAt(0, 0) != null)
					{
						lastSeen = resDS.getValueAt(0, 0).toString().trim();
				
					}
				}
		}
		catch (SQLException e) {
			
			log.error("getLastSeen : " + e);
			
		}
		finally{
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		
		return lastSeen;
	}

	/*** method to retrieve IP Addresses from where user has logged in
	 * @author YM : 04/17/2015 
	 * @param ds - data source name
	 * @param userName - user name
	 * 
	 * @return String containing IP Addresses or host names. 
	 * ***/
	@Override
	public String getIPAddress( String userName) {
		
		String ipAddresses = "";
		Datasource _ds ; //local variable
		Dataset resDS = null;
		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		//noOfDS = dsList.size();
		String sqlQuery = "SELECT distinct actor_host from AUDIT_EVENTS where actor=\"" + userName.trim() + "\";";
		SRConnection con = null;
		int noOfIPs = 0;
		
	
		try {
			
				con = _ds.getConnection();
				resDS = con.runQuery(sqlQuery);
			
				if(resDS != null)
				{
					noOfIPs = resDS.getRowCount();
						
					for(int k=0; k<noOfIPs; k++)
					{
						ipAddresses = ipAddresses + resDS.getValueAt(k, 0).toString().trim() + ",";
						
					}
				}
		}
		catch (SQLException e) {
			
			log.error(e);
			
		}
		finally{
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		
		return ipAddresses;
	}

	
	/*** method to retrieve total number of logins for user 
	 * @author YM : 04/17/2015 
	 * @param ds - data source name
	 * @param userName - user name
	 * @param duration - time frame for which data is to be queried.
	 * @return total no of logins
	 * ***/
	@Override
	public int getTotalVisits( String userName, int duration, String projectName, boolean allProjects, String userAuthProfile) {
		int noOfVisits = 0;
		Datasource _ds ;
		Dataset resDS = null;
	

		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		//noOfDS = dsList.size();
		SRConnection con = null;
		String sqlQuery = "";
		String dateFilter = getDateFilter(duration, "EVENT_TIMESTAMP");
		if(allProjects)
		{
			sqlQuery = "SELECT count(action) from AUDIT_EVENTS where action = \"login\" and status_code = 0 and actor=\"" + userName.trim() + "\" and "
					+ dateFilter + ""
					+ " and ORIGINATING_SYSTEM in"
					+ " (select concat('project=',project_name) from mod_ia_projects where auth_profile='" +
					userAuthProfile + "');";
			
		}
		else
		{
			sqlQuery = "SELECT count(action) from AUDIT_EVENTS where action = \"login\" and status_code = 0 and actor=\"" + userName.trim() + "\" and "
					+ dateFilter + " and ORIGINATING_SYSTEM = 'project=" + projectName + "';";
			
		}
			try {
				
					con = _ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					
					if(resDS != null)
					{
						noOfVisits = Integer.parseInt(resDS.getValueAt(0, 0).toString().trim());
					}
			}
				catch (SQLException e) {
			
				log.error(e);
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		return noOfVisits;
	}

	
	/*** method to retrieve total number of actions performed by the user 
	 * @author YM : 04/17/2015 
	 * @param ds - data source name
	 * @param userName - user name
	 * @param duration - time frame for which data is to be queried.
	 * @return total no of actions
	 * ***/
	
	private int getTotalActions(String userName, int duration, String projectName, boolean allProjects, String userProfile) {
		int noOfActions = 0;
		Datasource _ds ;
		Dataset resDS = null;
	
		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		//noOfDS = dsList.size();
		String sqlQuery = "";
		String dateFilter = "";
		
		dateFilter = getDateFilter(duration, "event_timestamp");
		if(allProjects)
		{
			sqlQuery = "SELECT count(action) from AUDIT_EVENTS where actor=\"" + userName.trim() + 
					"\" and status_code = 0 and " + dateFilter + 
					" and ORIGINATING_SYSTEM in"
					+ " (select concat('project=',project_name) from mod_ia_projects where auth_profile='"
					+ userProfile + "');";
			
		}
		else
		{
			sqlQuery = "SELECT count(action) from AUDIT_EVENTS where actor=\"" + userName.trim() + 
					"\" and status_code = 0 and " + dateFilter + " and ORIGINATING_SYSTEM = 'project="	+ projectName + "'"
					+ " and ORIGINATING_SYSTEM in"
					+ " (select concat('project=',project_name) from mod_ia_projects where auth_profile='"
					+ userProfile + "');";
		}
		
		 SRConnection con = null;
		
			try {
				
					con = _ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					
					if(resDS != null)
					{
						noOfActions = Integer.parseInt(resDS.getValueAt(0, 0).toString().trim());
					}
			}
				catch (SQLException e) {
				
				log.error(e);
				
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		return noOfActions;
	}

	
	/** 
	 * method to retrieve total sessions length for a given user 
	 * @author YM : 04/17/2015 
	 * @param ds - data source name
	 * @param userName - user name
	 * @param duration - time frame for which data is to be queried.
	 * @return Total Sessions length 
	 * */
	
	private String getTotalSessionsLength( String userName, int duration, String projectName, boolean allProjects, String userProfile) {
		Datasource _ds ;
		Dataset resDS1 = null, resDS2 = null;
		String sqlQuery = "";
		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS); 
		String dateFilter = getDateFilter(duration, "a.event_timestamp");
	
		if(allProjects)
		{
			sqlQuery = "select time_format(SEC_TO_TIME(SUM(TIME_TO_SEC(TIMEDIFF(logoutTime, loginTime)))),'%H:%i:%s')"
					+ " from ("
					+ " select a.actor as actor, max(a.event_timestamp) as loginTime,"
					+ " b.event_timestamp as logoutTime "
					+ " from AUDIT_EVENTS a, AUDIT_EVENTS b "
					+ " where a.actor = b.actor  and a.actor = '" + userName.trim() + "' and"
					+ " a.action = 'login' and a.status_code = 0 and b.action = 'logout' and "
					+ dateFilter  
					+ " and a.event_timestamp < b.event_timestamp and"
					+ " a.ORIGINATING_SYSTEM = b.ORIGINATING_SYSTEM and a.ORIGINATING_SYSTEM in"
					+ " (select concat('project=',project_name) from mod_ia_projects where auth_profile='" + userProfile + "')"
					+ " group by logoutTime order by logoutTime desc) as st;";
					
		}
		else
		{
			sqlQuery = "select time_format(SEC_TO_TIME(SUM(TIME_TO_SEC(TIMEDIFF(logoutTime, loginTime)))),'%H:%i:%s')"
					+ " from ("
					+ " select a.actor as actor, max(a.event_timestamp) as loginTime,"
					+ " b.event_timestamp as logoutTime "
					+ " from AUDIT_EVENTS a, AUDIT_EVENTS b "
					+ " where a.actor = b.actor  and a.actor = '" + userName.trim() + "' and"
					+ " a.action = 'login' and a.status_code = 0 and b.action = 'logout' and "
					+ dateFilter  + " and a.ORIGINATING_SYSTEM = 'project=" + projectName 
					+ "' and a.event_timestamp < b.event_timestamp and "
					+ " a.ORIGINATING_SYSTEM = b.ORIGINATING_SYSTEM group by logoutTime order by logoutTime desc) as st;";
		}
		

		SRConnection con = null;
	
		String totalSessionsLength = "";
			try {
				
					con = _ds.getConnection();
					
					resDS1 = con.runQuery(sqlQuery);
					
					if(resDS1 != null && resDS1.getRowCount() > 0)
					{
						if(resDS1.getValueAt(0,0) != null)
						{
							totalSessionsLength = resDS1.getValueAt(0,0).toString();
						}
					}
			}
				catch (SQLException e) {
				
				log.error(e);
				
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		return totalSessionsLength;
	}

	/** 
	 * Function to retrieve number of active alarms for each priority level
	 * for today. To be shown as real time information.
	 * @author YM : 04/17/2015 
	 * @param dataSource Data source name from where to retrieve the information
	 * @return List of strings where each string contains priority , number of alarms
	 * */
	@Override
	public Dataset getActiveAlarmsCount(String projectName, boolean allProjects) {

		
		
		Datasource ds;
		Dataset resDS = null;
		
		int noOfSearchResults = 0;
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
					
		String sqlQuery = "select case when priority = 0 then 'Diagnostic'"
				+ "	when priority = 1 then 'Low'"
				+ "	when priority = 2 then 'Medium'"
				+ "	when priority = 3 then 'High'"
				+ "	when priority = 4 then 'Critical'"
				+ "	end as alarm_priority, count(distinct eventid)"
				+ "	from ALARM_EVENTS "
				+ "	where "
				//+ " DATE(eventtime) = date(now()) "
				+ " eventtime > '" + getDayAndTime(Constants.YESTERDAY) + "' and eventtime < '"
				+ getDayAndTime(Constants.TODAY)
				//+ "' and eventtime >= '"+this.installDate+"'"
				+ "' and eventid not in"
				+ " (select eventid from ALARM_EVENTS where eventtype = 1)"
				+ "	group by priority;";
		
		SRConnection con = null;
//		log.error("getActiveAlarmsCount : query is - " + sqlQuery);
		
		int r=0;
			try {
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
			}
					catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {

					e.printStackTrace();
				}
				
				}
		}
		
	
		
		return resDS;
	}

	
	/** 
	 * Function to retrieve number of acknowledged alarms for each priority level
	 * for today. To be shown as real time information.
	 * @author YM 04/17/2015 
	 * @param dataSource Data source name from where to retrieve the information
	 * @return  dataset with priority , number of alarms
	 * @see Dataset
	 * */
	@Override
	public Dataset getAckAlarmsCount( String projectName, boolean allProjects) {

		
		
		Datasource ds;
		Dataset resDS = null;
		
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		
		String sqlQuery = " select case when priority = 0 then 'Diagnostic'"
				+ "			when priority = 1 then 'Low'"
				+ "			when priority = 2 then 'Medium'"
				+ "			when priority = 3 then 'High'"
				+ "			when priority = 4 then 'Critical'"
				+ "			end as alarm_priority, count(distinct eventid)"
				+ "			from ALARM_EVENTS"
				+ "			where "
				//+ "			DATE(eventtime) = date(now()) "
				+ " eventtime > '" + getDayAndTime(Constants.YESTERDAY) + "' and eventtime < '"
				+ getDayAndTime(Constants.TODAY)
				//+ "' and eventtime >= '"+this.installDate+"'"
				+ "'			and eventid in"
				+ "			(select eventid from ALARM_EVENTS where eventtype = 2)"
				+ "			group by alarm_priority";
		
		SRConnection con = null;
//		log.error("getAckAlarmsCount : query is - " + sqlQuery);
		
		int r=0;
			try {
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					
					
					}
					catch (SQLException e) {

				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getAckAlarmsCount : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
		}
		
	
		
		return resDS;
	}

	/** YM Created on 04/20/2015 
	 * Method to calculate number of actions done by currently logged on users.
	 * Processing : 
	 * 	get no of actions and screen views for each user depending on the creation time
	 * @return noOfActions TOtal number of actions by current users.
	 * */
	@Override
	public int getNumActionsByCurrentUsers(String projectName, boolean allProjects, HashMap<String, Date> distinctUserLogins) {
		
		
		int noOfActions = 0;
		
		Datasource _ds ; //local variable
		Dataset resDS = null;
	
		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		//noOfDS = dsList.size();
		String sqlQueryAuditEvents = "";
		String sqlQueryScreenViews = "";
		SRConnection con = null;
		
		String userWithProfile = "";
		String userName = "";
		String profileName = "";
		Date creationDate = new Date();
		HashMap.Entry<String,Date> userRec;
		int i = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			
				con = _ds.getConnection();
			
				Iterator<HashMap.Entry<String,Date>> itr = distinctUserLogins.entrySet().iterator();
				while(itr.hasNext())
				{
					userRec = itr.next();	
					userWithProfile = userRec.getKey();
					creationDate = userRec.getValue();
					userName = userWithProfile.split(":")[0].trim();
					profileName = userWithProfile.split(":")[1].trim();
					
					sqlQueryAuditEvents = "";
					sqlQueryScreenViews = "";
					if(allProjects)
					{
						sqlQueryAuditEvents = "select count(action) from AUDIT_EVENTS "
							+ " WHERE actor='" + userName + "' and status_code = 0 "
							+ " and event_timestamp >= '"+this.installDate+"'"
							+ " and SUBSTRING(ORIGINATING_SYSTEM,9) in "
							+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName + "') "
							+ " and event_timestamp >= '" + sdf.format(creationDate) + "';";
					
					
						sqlQueryScreenViews = "select count(screen_name) from mod_ia_screen_views "
							+ " WHERE action = 'SCREEN_OPEN' and USERNAME='" + userName + "' and PROJECT in "
							+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName + "') "
							+ " and view_timestamp >= '" + sdf.format(creationDate) + "';";
					}
					else
					{
						sqlQueryAuditEvents = "select count(action) from AUDIT_EVENTS "
								+ " WHERE actor='" + userName + "' and status_code = 0 "
								+ " and event_timestamp >= '"+this.installDate+"'"
								+ " and SUBSTRING(ORIGINATING_SYSTEM,9) in "
								+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName + "' and project_name = '"
								+ projectName + "') "
								+ " and event_timestamp >= '" + sdf.format(creationDate) + "';";
						
						
							sqlQueryScreenViews = "select count(screen_name) from mod_ia_screen_views "
								+ " WHERE action = 'SCREEN_OPEN' and USERNAME='" + userName + "' and PROJECT in "
								+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName + "' and project_name = '"
								+ projectName + "') "
								+ " and view_timestamp >= '" + sdf.format(creationDate) + "';";
					}
					
					resDS = con.runQuery(sqlQueryAuditEvents);
					 
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							noOfActions = noOfActions + (int)Float.parseFloat(resDS.getValueAt(0, 0).toString());
						}
					}
				
					resDS = null;
					resDS = con.runQuery(sqlQueryScreenViews);
				
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							noOfActions = noOfActions + (int)Float.parseFloat(resDS.getValueAt(0, 0).toString());
						}
					}
				}
		}
		catch (SQLException e) {
			
			log.error(e);
			
		}
		finally{
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
				
					e.printStackTrace();
				}
				
				}
		}
		
		return noOfActions;
	}

	@Override
	public UsersCount getNumberofNewAndReturningUsers( String projectName, boolean allProjects) {
		
		UsersCount users = new UsersCount();
		int numNewUsers = 0;
		int numReturningUsers = 0;
		
		//first get the current users
		List<ClientReqSession> sessions = mycontext.getGatewaySessionManager().findSessions();
		if(sessions != null)
		{
			int noOfSessions = sessions.size();
			User _currentUser;
			String user, userProfile;
			long creationTime;
			Date created, firstSeen;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			if(noOfSessions > 0)
			{
				try {
				for(int j=0; j<noOfSessions; j++)
				{
					//get first sessions time for each user
					creationTime = sessions.get(j).getCreationTime();
					created = new Date(creationTime);
					_currentUser = (User)sessions.get(j).getAttribute(ClientReqSession.SESSION_USER);
					user = _currentUser.get(User.Username);
					userProfile = _currentUser.getProfileName();
					//see if first login is less than session start time
					String fSeen = getFirstSeen( user, projectName, allProjects, userProfile);
					if(fSeen != null)
					{
						firstSeen = sdf.parse(fSeen);
					
						if(firstSeen.before(created))
						{
							numReturningUsers++;  //returning user
						}
						else
						{
							numNewUsers++; //new user
						}
					}
					else
					{
						numNewUsers++;
					}
				} 
				}catch (ParseException e) {
					
					e.printStackTrace();
				}
			}
			
			users.setNum_newUsers(numNewUsers);
			users.setNum_retUsers(numReturningUsers);
		}
			return users;
	}

	/**
	 * Function to find out how many users have logged in for the first time in the given duration.
	 * HOw it works
	 * 1. Get the number of distinct users in the given time frame
	 * 2. FOr each user, get first login time
	 * 3. If first login falls in the said duration, user is new
	 * else user is returning.
	 * @author YM
	 * @param duration - time frame for which data is to be queried
	 * @param dataSource - from where data is to be retrieved
	 * @return : Number of first time logged in i.e. new users
	 * 
	 * **/
	@Override
	public int getNumberOfNewUsers(int duration,  String projectName, boolean allProjects) {
		int noOfNewUsers = 0;
		Datasource _ds;
		Dataset resDS = null;
		Date startDate, endDate, loginDate;
		_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		String sqlQuery = "";
		SRConnection con = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Calendar calStart = Calendar.getInstance();
	    calStart.set(Calendar.HOUR_OF_DAY, 0);
	    calStart.set(Calendar.MINUTE, 0);
	    calStart.set(Calendar.SECOND, 0);
	    calStart.set(Calendar.MILLISECOND, 0);
		startDate = calStart.getTime();
		
		Calendar calEnd = Calendar.getInstance();
		calEnd.set(Calendar.HOUR_OF_DAY, 0);
		calEnd.set(Calendar.MINUTE, 0);
		calEnd.set(Calendar.SECOND, 0);
		calEnd.set(Calendar.MILLISECOND, 0);
		endDate = calEnd.getTime();
		
		String filter = "";
		if(allProjects){
			filter = "";
		}else{
			filter = " and a.ORIGINATING_SYSTEM = concat('project=','"+ projectName+"') ";
		}
		try
		{
			switch(duration){
			case Constants.TODAY:
				sqlQuery = "select  a.actor, b.AUTH_PROFILE  FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) "
						+ " and a.event_timestamp >= '"+this.installDate+"'"
						+ " and a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 "+filter
						+ " group by a.actor,b.AUTH_PROFILE having DATE(min(a.event_timestamp)) = DATE(NOW());";
				endDate = startDate;
				break;
			case Constants.YESTERDAY:
				sqlQuery = "select  a.actor, b.AUTH_PROFILE  FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) "
						+ " and a.event_timestamp >= '"+this.installDate+"'"
						+ " and a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 "+filter
						+ " group by a.actor,b.AUTH_PROFILE having"
						+ " DATE(min(a.event_timestamp)) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) ;";
				calEnd.add(Calendar.DATE, -1);
				endDate = calEnd.getTime();
				break;
//			case Constants.LAST_SEVEN_DAYS:
//			
//				sqlQuery = "select  a.actor, b.AUTH_PROFILE  FROM AUDIT_EVENTS a,  mod_ia_projects b "
//						+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)  "
//						+ " and a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 and "
//						+ " DATE(a.event_timestamp) > DATE_SUB(CURDATE(), INTERVAL 7 DAY)"
//						+ " group by a.actor,b.AUTH_PROFILE;";
//				calEnd.add(Calendar.DATE, -7);
//				endDate = calEnd.getTime();
//				break;
//			case Constants.LAST_THIRTY_DAYS:
//				sqlQuery = "select  a.actor, b.AUTH_PROFILE  FROM AUDIT_EVENTS a,  mod_ia_projects b "
//						+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and "
//						+ " a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 and "
//						+ " DATE(a.event_timestamp) > DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
//						+ " group by a.actor,b.AUTH_PROFILE;";
//				calEnd.add(Calendar.DATE, -30);
//				endDate = calEnd.getTime();
//				break;
//			case Constants.LAST_NINTY_DAYS:
//				sqlQuery = "select  a.actor, b.AUTH_PROFILE  FROM AUDIT_EVENTS a,  mod_ia_projects b "
//						+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and "
//						+ " a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 and "
//						+ " DATE(a.event_timestamp) > DATE_SUB(CURDATE(), INTERVAL 90 DAY) "
//						+ " group by a.actor,b.AUTH_PROFILE;";
//				calEnd.add(Calendar.DATE, -90);
//				endDate = calEnd.getTime();
//				break;
			}
			
			
			con = _ds.getConnection();
			resDS = con.runQuery(sqlQuery);
			
			if(resDS != null)
			{
				noOfNewUsers = resDS.getRowCount();
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getNumberOfNewUsers : in con close exception.");
					
					e.printStackTrace();
				}
			}
		}
		return noOfNewUsers;
	}

	/**
	 * method to get summary of all alarms to show name(display path), count, avg clear time , avg ack time in given duration
	
	 */
	@Override
	public Dataset getAlarmsSummary(int duration, String dataSource) {
		
		Datasource ds;
		Dataset resDS = null;
		

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		String sqlQuery = "";
		SRConnection con = null;
		String dateFilter = getDateFilter(duration, "a.eventtime");
		
		sqlQuery = "select displaypath, case when priority = 0 then \"Diagnostic\""
				+ " when priority = 1 then \"Low\""
				+ " when priority = 2 then \"Medium\""
				+ " when priority = 3 then \"High\""
				+ " when priority = 4 then \"Critical\""
				+ " end as priority, count(source) as Quantity, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(timetoClear))),'%H:%i:%s') as AvgTimeToClear"
				+ " from (select a.priority as priority,a.source as source, a.displaypath as displaypath, TIMEDIFF(b.eventtime , a.eventtime) as timetoClear "
				+ " from ALARM_EVENTS a, ALARM_EVENTS b "
				+ " where a.eventid = b.eventid and a.eventtype = 0 and b.eventtype = 2 and "
				+ dateFilter + ") as dt "
				+ " group by source;";
		
		

			try {
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
				}
					catch (SQLException e) {
			
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getAlarmsSummary : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
		}
		
		return resDS;
	}

	/**
	 * Method to store screens viewed information captured at client
	 * @author YM - created on 06/09/2015
	 * @param screen 
	 * @see ScreenViewsRecord
	 */
	@Override
	public void storeScreenViewedInformation(ScreenViewsRecord screen) throws ModIAConfigurationException {
		
		List<MODIAPersistentRecord> results;
		MODIAPersistentRecord record;
		Datasource ds;
		String insertQuery = "";
		SRConnection con = null;
	
		//first retrieve the Data store name from Persistence record
		
		SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
		results = mycontext.getPersistenceInterface().query(query);
		
		String dsName = "";
		
		if(results != null && results.size() > 0)
		{
			
			record = results.get(0);
			if(record != null)
			{
					dsName = record.getDatasource();
			}
			else
			{
				throw new ModIAConfigurationException("Analytics module not configured properly.");
			}
			//connect to the DB and store the record
			ds = mycontext.getDatasourceManager().getDatasource(Long.parseLong(dsName));
			try {
				con = ds.getConnection();
				if(screen != null)
				{
					String clientProject = screen.getProjectName();
					String projectCheckQuery = "SELECT PROJECT_NAME from MOD_IA_PROJECTS where PROJECT_NAME = '"+clientProject+"'";
					log.error("storeScreenViewedInformation : proj chk q" + projectCheckQuery);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
					Dataset projectsData = con.runQuery(projectCheckQuery);
					int datasetSize = 0;
					
					if (projectsData != null){
						datasetSize = projectsData.getRowCount();
					}
					if(datasetSize == 1)
					{
						insertQuery = "INSERT INTO MOD_IA_SCREEN_VIEWS (USERNAME, SCREEN_NAME, SCREEN_PATH, SCREEN_TITLE, ACTION, PROJECT, VIEW_TIMESTAMP) "
								+ " VALUES ('"
								+ screen.getUsername() +"','" + screen.getScreenName() + "','"
								+ screen.getScreenPath() +  "','" + screen.getScreenTitle() + "','"
								+ screen.getAction() + "','" + screen.getProjectName() + "', '"
								+ sdf.format(new Date()) + "');";
						log.error("storeScreenViewedInformation : insertQuery" + insertQuery);
						this.mycontext.getHistoryManager().storeHistory(ds.getName(), new StoreForwardDS(insertQuery, ds.getName()));
					}
				}
				else
				{
					log.error("Function to store screen view was called without data.");
				}
			
			} catch ( Exception e) {
				log.error(e);
				
			}finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("storeScreenViewedInformation : in con close exception.");
					log.error(e);
					
				}
				}
			}
			
		}
		else
		{
			throw new ModIAConfigurationException("Analytics module not configured.");
		}
		
	}

	/**
	 * Method to get List of screens viewed and count of each screen in a given duration
	 * @author YM: Created on 10/06/2015
	 * @param duration - time frame for which data is to be queried
	 * @param dataSource - from where data is to be retrieved
	 * @return : list of screen names along with count of views per screen
	 * @see ScreensCount
	 */
	@Override
	public List<ScreensCount> getScreensViewedCounts(int duration, String projectName, boolean allProjects) {
		List<ScreensCount> retList = new ArrayList<ScreensCount>();
		ScreensCount objScreen;
		Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		Dataset results = null;
		String sqlQuery = "";
		SRConnection con = null;
		int noOfResults;	
		
		String dateFilter = getDateFilter(duration, "a.VIEW_TIMESTAMP");
		String sessionDateFilter = getDateFilter(duration, "s.SESSION_START");
		if(allProjects)
		{
			sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME) as screenCount"
				 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
				 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + dateFilter + " and " + sessionDateFilter
				 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
				 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME "
				 + " GROUP BY a.SCREEN_NAME order by screenCount desc;";
		}
		else
		{
			sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME) as screenCount"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + dateFilter + " and " + sessionDateFilter
					 + " and a.PROJECT = '"	+ projectName + "' "
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME"
					 + " GROUP BY a.SCREEN_NAME order by screenCount desc;";
			
		}
//		log.error("getScreensViewedCounts : sql query is : " + sqlQuery);
		try {
					con = ds.getConnection();
					results = con.runQuery(sqlQuery);
					if(results != null)
					{
						noOfResults = results.getRowCount();
						for(int i=0; i<noOfResults; i++)
						{
							objScreen = new ScreensCount();
							objScreen.setScreenName(results.getValueAt(i, 0).toString());
							objScreen.setNoOfViews(Integer.parseInt(results.getValueAt(i, 1).toString()));
							retList.add(objScreen);
						}
					}
				}
					catch (SQLException e) {
			
				log.error(e);
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getScreensViewedCounts : in con close exception.");
					
					log.error(e);
				}
				
				}
			}	
		
		
		
		return retList;
	}
	
	/**
	 * Method to get List of screens viewed and count of each screen in a given duration for a user
	 * @author YM: Created on 11/06/2015
	 * @param duration - time frame for which data is to be queried
	 * @param dataSource - from where data is to be retrieved
	 * @return : list of screen names along with count of views per screen
	 * @see ScreensCount
	 */

	private List<ScreensCount> getScreensViewedCountsPerUser( int duration, String username, String projectName, boolean allProjects, String profileName) {
		List<ScreensCount> retList = new ArrayList<ScreensCount>();
		ScreensCount objScreen;
		Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		Dataset results = null;
		String sqlQuery = "";
		SRConnection con = null;
		int noOfResults;	
		
		String dateFilter = getDateFilter(duration, "a.VIEW_TIMESTAMP");
		String sessiondateFilter = getDateFilter(duration, "s.session_start");
		if(allProjects)
		{
			sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME)"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE " + dateFilter + " and a.USERNAME = '"
					 + username.trim() + "' and " + sessiondateFilter
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME "
					 + " and ACTION='SCREEN_OPEN'  and PROJECT IN ("
					 + " SELECT PROJECT_NAME FROM MOD_IA_PROJECTS where AUTH_PROFILE = '" + profileName +"')"
					 + " GROUP BY a.SCREEN_NAME;";
		}
		else
		{
			
			sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME)"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE " + dateFilter + " and a.USERNAME = '"
					 + username.trim() + "' and " + sessiondateFilter
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME "
					 + " and PROJECT = '"	+ projectName + "'"
					 + " and ACTION='SCREEN_OPEN'  and PROJECT IN ("
					 + " SELECT PROJECT_NAME FROM MOD_IA_PROJECTS where AUTH_PROFILE = '" + profileName +"')"
					 + " GROUP BY a.SCREEN_NAME;";
		}
		
		
		try {
					con = ds.getConnection();
					results = con.runQuery(sqlQuery);
					if(results != null)
					{
						noOfResults = results.getRowCount();
						for(int i=0; i<noOfResults; i++)
						{
							objScreen = new ScreensCount();
							objScreen.setScreenName(results.getValueAt(i, 0).toString());
							objScreen.setNoOfViews(Integer.parseInt(results.getValueAt(i, 1).toString()));
							retList.add(objScreen);
						}
					}
				}
					catch (SQLException e) {
			
				log.error(e);
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getScreensViewedCountsPerUser : in con close exception.");
					
					log.error(e);
				}
				
				}
			}	
		
		
		
		return retList;
	}

	/**
	 * Function to find out bounce rate i.e. no of users viewing only one screen
	 * @author YM - Created on 06/12/2015
	 */
	@Override
	public float getBounceRate(int duration, String projectName, boolean allProjects) {
		float bounceRate = 0;
		Datasource datasource = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		Dataset results = null;
		String sqlScreensQuery = "";
		String usersQuery = "";
		SRConnection con = null;
		
		String screenViewDateFilter = getDateFilter(duration, "a.VIEW_TIMESTAMP");
		String auditDateFilter = getDateFilter(duration, "EVENT_TIMESTAMP");
		String sessionDateFilter = getDateFilter(duration,"s.session_start");
		if(allProjects)
		{
			sqlScreensQuery = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
			 + " (SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
			 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
			 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
			 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
			 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME) as x"
			 + " where x.PROJECT = b.PROJECT_NAME"
			 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
			usersQuery = "SELECT count(distinct(concat(a.actor,s.auth_profile))) as noOfUsers "
					+ " FROM AUDIT_EVENTS a, mod_ia_projects s WHERE STATUS_CODE = 0"
					+ " and " + auditDateFilter + " ;";
		}
		else{
			sqlScreensQuery =" SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
					 + " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
					 + " and a.project = '" + projectName + "' "
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
					 + " where x.PROJECT = b.PROJECT_NAME"
					 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
			
			usersQuery = "SELECT count(distinct(concat(a.actor,s.auth_profile))) as noOfUsers "
					+ " FROM AUDIT_EVENTS a, mod_ia_projects s WHERE STATUS_CODE = 0"
					+ " and " + auditDateFilter + " and PROJECT like '%" + projectName + "';";
		}
//		log.error("sqlScreensQuery "+sqlScreensQuery);
		try {
					con = datasource.getConnection();

					results = con.runQuery(sqlScreensQuery);
					
					float usersWithOneScreen = 0;
					float totalUsers = 0;
					if(results != null && results.getRowCount() > 0)
					{
//						if(results.getValueAt(0, 0) != null)
//						{
//							usersWithOneScreen = Float.parseFloat(results.getValueAt(0, 0).toString());
//						}
						
						usersWithOneScreen = results.getRowCount();
					}
				
					
					
					results = con.runQuery(usersQuery);
					
					if(results != null && results.getRowCount() > 0)
					{
						if(results.getValueAt(0, 0) != null)
						{
							totalUsers = Float.parseFloat(results.getValueAt(0, 0).toString());
						}
					}
					if(usersWithOneScreen == 0 || totalUsers == 0)
					{
						bounceRate = 0;
					}
					else
					{
						bounceRate = (usersWithOneScreen/totalUsers);
					}
					
				}
					catch (SQLException e) {
			
				log.error(e);
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getBounceRate : in con close exception.");
					
					log.error(e);
				}
				
				}
			}	
		return bounceRate;
	}

	
	
	/**
	 * Method to retrieve real time data for number of users viewing each screen
	 * @author YM : Created on - 06/13/2015
	 * @param ds - from where data is to be retrieved
	 * @return : Dataset containing result of the sql query
	 * @see Dataset
	 */
	@Override
	public HashMap<String,Integer> getNumberOfUsersPerScreenRealTime(String projectName, boolean allProjects) {
		
		Datasource datasource = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		Dataset results = null;
		HashMap<String, Integer> screenUserCount = new HashMap<String,Integer>();
		String sqlQuery = "";
		SRConnection con = null;
		List<ContentsData> returnList = new ArrayList<ContentsData>();
		
		ContentsData record = null;
		
		List<UserSessionsInfo> sessions = this.getActiveSessions( projectName, allProjects);
		HashMap<String,Date> usersMap = new HashMap<String, Date>();
		ClientReqSession session;
		String sessionProjects = "";
		if(sessions != null)
		{
			int noOfSessions = sessions.size();
			long creationTime = 0;
			Date created = new Date();
			String user = "";
			Date tempDate = new Date();
			if(noOfSessions > 0)
			{
				
					
						for(int j=0; j<noOfSessions; j++)
						{
							
								//for each user in the session get session start time
								creationTime = sessions.get(j).getCreationTime();
								created = new Date(creationTime);
								
								user = sessions.get(j).getUserName() + ":" + sessions.get(j).getProfileName();
								//put the value in hash map so that we don't count duplicate actions if user 
								//has multiple active sessions
								if(usersMap.containsKey(user))
								{
									tempDate = usersMap.get(user); //get the old value in hash map
									if(created.before(tempDate))
									{
										usersMap.put(user,created); //put the user name and session start time
									}
								}
								else
								{
									usersMap.put(user,created);
								}
						} 
				
					
						
						//connect to the datasource
					
					
					//Iterate over the usersMap to get all users and get respective  records.
					Iterator<HashMap.Entry<String,Date>> itr = usersMap.entrySet().iterator();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String strDate= "";
					String userName = "";
					String profileName = "";
					String userProfileName = "";
					String screenName = "";
					int userCount = 0;
					int noOfQueryResults= 0;
					int k = 0;
					try
					{
						datasource = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
						con = datasource.getConnection();
					while(itr.hasNext())
					{
						HashMap.Entry<String,Date> userRec = itr.next();
						userProfileName = userRec.getKey();
						userName = userProfileName.split(":")[0].trim();
						profileName = userProfileName.split(":")[1].trim();
						tempDate = userRec.getValue();
						strDate = sdf.format(tempDate);
						
						if(allProjects)
						{
							sqlQuery = "select distinct screen_name from mod_ia_screen_views WHERE action = 'SCREEN_OPEN' and USERNAME='"
									+ userName +"' and PROJECT in (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName + 
									"') and view_timestamp >= '" + strDate + "';";
						}
						else
						{
							sqlQuery = "select distinct screen_name from mod_ia_screen_views "
									+ " WHERE action = 'SCREEN_OPEN' and USERNAME='" + userName + "' and PROJECT in "
									+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName + "' and project_name = '"
									+ projectName + "') "
									+ " and view_timestamp >= '" + strDate + "';";
						}
						
//						log.error("getNumberOfUsersPerScreenRealTime , sql q : " + sqlQuery);
						results = con.runQuery(sqlQuery);
						
						if(results != null && results.getRowCount() > 0)
						{
							noOfQueryResults = results.getRowCount();
							for(k=0; k<noOfQueryResults; k++)
							{
								if(results.getValueAt(k, 0) != null)
								{
									screenName = results.getValueAt(k, 0).toString().trim();
									userCount = 0;
									if(screenUserCount.containsKey(screenName))
									{
										userCount = screenUserCount.get(screenName);
										userCount = userCount + 1;
										screenUserCount.put(screenName, userCount);
									}
									else
									{
										screenUserCount.put(screenName, 1);
									}
								}
							}
						
						}
					}
				} catch (Exception e) {
					
					log.error(e);
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getNumberOfScreensViewedCurrentUsers : in con close exception.");
						
						log.error(e);
					}
					
					}
				}
		
			}
		}
		
		
		
		
		
		
		
		
		//return results;
		return screenUserCount;
	}

	
	
	/**
	 * Method to find out the screen which user is/was viewing currently
	 * @author YM : Created on 06/13/2015
	 * @param ds - from where data is to be retrieved
	 * @param username - name of the user for whom data is to be retrieved
	 * @return Screen name and seen at value
	 * 
	 */

	private String getCurrentScreenForUser( String username, String projectName, boolean allProjects, String userProfile) {
		
		String screenName= "";
		Datasource datasource = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		Dataset results = null;
		String sqlQuery = "";
		SRConnection con = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if(allProjects)
		{
			sqlQuery = "select screen_name , view_timestamp from mod_ia_screen_views "
					+ " where username = '" + username + "' and view_timestamp = "
					+ " (select max(view_timestamp) from mod_ia_screen_views"
					+ " where username='" + username + "'"
					+ " and ACTION='SCREEN_OPEN' and PROJECT IN "
					+ " (SELECT PROJECT_NAME from mod_ia_projects where AUTH_PROFILE = '"+ userProfile +"')) order by AUDIT_EVENTS_ID desc;";
		}
		else
		{
			sqlQuery = "select screen_name , view_timestamp from mod_ia_screen_views "
					+ " where username = '" + username + "' "
					+ " and PROJECT = '"	+ projectName + "'"
					+ " and view_timestamp = "
					+ " (select max(view_timestamp) from mod_ia_screen_views"
					+ " where username='" + username + "'"
							+ " and ACTION='SCREEN_OPEN') order by AUDIT_EVENTS_ID desc;;";
		}
		try {
			
					con = datasource.getConnection();
					results = con.runQuery(sqlQuery);
					if(results != null && results.getRowCount() > 0)
					{
						try {
							screenName = results.getValueAt(0, 0) + ", viewed at : " + sdf1.format(sdf.parse(results.getValueAt(0, 1).toString()));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
					catch (SQLException e) {
			
				log.error(e);
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getCurrentScreenForUser : in con close exception.");
					
					log.error(e);
				}
				
				}
			}	
		return screenName;
	}

	

	@Override
	public List<ScreensCount> getNumberOfScreensViewedCurrentUsers( String projectName, boolean allProjects) {
		
		
		int noOfScreens = 0;
		String user = "";
		long creationTime;
		Date created = new Date();
		Date tempDate;
		String selectQuery = "";
		
		Datasource datasource; 
		Dataset results = null;
		SRConnection con = null;
		
		List<ScreensCount> retList = new ArrayList<ScreensCount>();
		ScreensCount record;
		List<ClientReqSession> sessions = mycontext.getGatewaySessionManager().findSessions();
		HashMap<String,Date> usersMap = new HashMap<String, Date>();
		ClientReqSession session;
		String sessionProjects = "";
		if(sessions != null)
		{
			int noOfSessions = sessions.size();
			if(noOfSessions > 0)
			{
				try {
					if(allProjects)
					{
						for(int j=0; j<noOfSessions; j++)
						{
							session = sessions.get(j);
								//for each user in the session get session start time
								creationTime = session.getCreationTime();
								created = new Date(creationTime);
								
								user = session.getAttribute(ClientReqSession.SESSION_USERNAME).toString();
								//put the value in hash map so that we don't count duplicate actions if user 
								//has multiple active sessions
								if(usersMap.containsKey(user))
								{
									tempDate = usersMap.get(user); //get the old value in hash map
									if(created.before(tempDate))
									{
										usersMap.put(user,created); //put the user name and session start time
									}
								}
								else
								{
									usersMap.put(user,created);
								}
						} 
					}
					else
					{
						for(int j=0; j<noOfSessions; j++)
						{
								session = sessions.get(j);
								sessionProjects = session.getAttribute(ClientReqSession.SESSION_PROJECT_NAME).toString().trim();
								//sessionProjects = this.mycontext.getProjectManager().getProjectName(Long.parseLong(sessionProjects), ProjectVersion.Published);
//								log.error("sessionProjects : " + sessionProjects);
								if(sessionProjects.compareToIgnoreCase(projectName) == 0)
								{
									//for each user in the session get session start time
									creationTime = session.getCreationTime();
									created = new Date(creationTime);
								
									user = session.getAttribute(ClientReqSession.SESSION_USERNAME).toString();
									//put the value in hash map so that we don't count duplicate actions if user 
									//has multiple active sessions
									if(usersMap.containsKey(user))
									{
										tempDate = usersMap.get(user); //get the old value in hash map
										if(created.before(tempDate))
										{
											usersMap.put(user,created); //put the user name and session start time
										}
									}
									else
									{
										usersMap.put(user,created);
									}
								}
						} 
					}
						
						//connect to the datasource
					datasource = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					con = datasource.getConnection();
					
					//Iterate over the usersMap to get all users and get respective  records.
					Iterator<HashMap.Entry<String,Date>> itr = usersMap.entrySet().iterator();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String strDate= "";
					while(itr.hasNext())
					{
						HashMap.Entry<String,Date> userRec = itr.next();
						tempDate = userRec.getValue();
						strDate = sdf.format(tempDate);
						
						selectQuery = "SELECT USERNAME, COUNT(DISTINCT SCREEN_NAME) FROM MOD_IA_SCREEN_VIEWS "
								+ " WHERE USERNAME = '" + userRec.getKey().trim()
								+ "' and VIEW_TIMESTAMP >= '" + strDate	+ "' and ACTION = 'SCREEN_OPEN';";
						
//						log.error("getCurrentOverview screen retrieval query : " + selectQuery);
						results = con.runQuery(selectQuery);
						if(results != null && results.getRowCount() > 0)
						{
							record = new ScreensCount();
							if(results.getValueAt(0, 0) != null)
							{
								record.setScreenName(results.getValueAt(0, 0).toString());
								record.setNoOfViews(Integer.parseInt(results.getValueAt(0, 1).toString()));
								retList.add(record);
							}
						}
						
					}
				} catch (Exception e) {
					
					log.error(e);
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getNumberOfScreensViewedCurrentUsers : in con close exception.");
						
						log.error(e);
					}
					
					}
				}
		
			}
		}
		return retList;
	}

	/**
	 * A method to store information sent from Client Startup script to the database.
	 * @author YM : Created on 6/17/2015.
	 * @param record - information record from the client
	 * @see ClientRecord
	 */
	@Override

	public void storeClientInformation(ClientRecord cRecord)
			throws ModIAConfigurationException {
		
		List<MODIAPersistentRecord> results;
		MODIAPersistentRecord record;
		Datasource ds;
		String insertQuery = "", insertLocationQuery = "";
		SRConnection con = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//first retrieve the Data store name from Persistence record
		SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
		results = mycontext.getPersistenceInterface().query(query);
		String dsName = "";
		
		if(results != null && results.size() > 0)
		{
			record = results.get(0);
			if(record != null)
			{
				
					dsName = record.getDatasource();
				
			}
			else
			{
				throw new ModIAConfigurationException("Analytics module not configured properly.");
			}
		
			//connect to the DB and store the record
			ds = mycontext.getDatasourceManager().getDatasource(Long.parseLong(dsName));
			try {
				con = ds.getConnection();
				if(cRecord != null)
				{
						//check if project is being monitored
						
						//Changed by Omkar on 26-May-2016
						//only insert if project is being monitored.
						
						String clientProject = cRecord.getProject();
						String projectCheckQuery = "SELECT PROJECT_NAME from MOD_IA_PROJECTS where PROJECT_NAME = '"+clientProject+"'";
						Dataset projectsData = con.runQuery(projectCheckQuery);
						int datasetSize = 0;
						
						if (projectsData != null)
						{
							datasetSize = projectsData.getRowCount();
						}
						if(datasetSize == 1)
						{
						insertQuery = "INSERT INTO MOD_IA_CLIENTS (BROWSER,  HOST_INTERNAL_IP, HOST_EXTERNAL_IP, "
						+ "HOSTNAME, IS_MOBILE, OS_NAME, OS_VERSION, USERNAME, PROJECT, SCREEN_RESOLUTION, START_TIMESTAMP, CLIENT_CONTEXT)"
						+ " VALUES ('" + cRecord.getBrowser() +"','"
						
						+ cRecord.getHostInternalIP() +  "','" 
						+ cRecord.getHostExternalIP() +  "','"
						+ cRecord.getHostName() + "',"
						+ cRecord.isMobile() + ",'" + cRecord.getoSName() + "','"
						+ cRecord.getOsVersion() + "','" 
						+ cRecord.getUserName() + "','" + cRecord.getProject()
						+ "', '" + cRecord.getScreenResolution()+ "','"
						+ sdf.format(new Date()) + "','" 
						+ cRecord.getClientContext() +"');";
						
						this.mycontext.getHistoryManager().storeHistory(ds.getName(), new StoreForwardDS(insertQuery, ds.getName()));
						
						String city = "";
						String state = "";
						String country = "";
						
//						log.error("storeClientInformation : city : " + cRecord.getCity() + 
//							", state : " + cRecord.getState() + ", country : "
//									+ cRecord.getCountry());
						
						if(IfLocationExists(cRecord.getHostInternalIP(), cRecord.getHostExternalIP()) == false)
						{
							if(cRecord.getCity() != null )
							{
								city = cRecord.getCity();
								
							}
							if(cRecord.getCountry() != null )
							{
								country = cRecord.getCountry();
							}
							if(cRecord.getState() != null )
							{
								state = cRecord.getState();
							}
//							log.error("storeClientInformation : location exists false");
							insertLocationQuery = "INSERT INTO mod_ia_location_info (`CITY`,`COUNTRY`,"
								+ "`EXTERNAL_IP`,`INTERNAL_IP`,`LATITUDE`,`LONGITUDE`,`STATE`)"
								+ "VALUES ('"
								+ city + "','" + country + "','"
								+ cRecord.getHostExternalIP() + "','" + cRecord.getHostInternalIP() + "',"
								+ cRecord.getLatitude() + "," + cRecord.getLongitude() + ",'"
								+ state + "');";
							this.mycontext.getHistoryManager().storeHistory(ds.getName(), new StoreForwardDS(insertLocationQuery, ds.getName()));
						}
						else
						{
							if(cRecord.getCity() != null && cRecord.getCountry() != null && cRecord.getState() != null)
							{
//								log.error("storeClientInformation : location exists");
								String updateQuery = "update mod_ia_location_info "
										+ "	set CITY = '" + city + "',"
										+ " STATE = '" + state + "', "
										+ " COUNTRY = '" + country + "', "
										+ " EXTERNAL_IP = '" + cRecord.getHostExternalIP() + "', "
										+ " LATITUDE = " + cRecord.getLatitude() + ", "
										+ " LONGITUDE = " + cRecord.getLongitude() 
										+ " WHERE INTERNAL_IP = '" + cRecord.getHostInternalIP() + "';";
								
								this.mycontext.getHistoryManager().storeHistory(ds.getName(), new StoreForwardDS(insertLocationQuery, ds.getName()));
							}
							
						}
					}
				}
				else
				{
					log.error("Function to store client information was called without data.");
				}
				
			} catch (Exception e) {
				log.error("storeClientInformation : " + e);
				
			}finally{
				if(con != null)
				{
				try {
					
					con.close();
				} catch (SQLException e) {
					log.error("storeClientInformation : con close exception. " + e);
					
				}
				}
			}
			
		}
		else
		{
			throw new ModIAConfigurationException("Analytics module not configured.");
		}
		
	}

	/***
	 * Method to get list of projects in the gateway that are being monitored by Ignition Analytics module.
	 * @author YM : Created on 08/24/2015
	 */
	@Override
	public String[] getProjects(String projectName) {
		
		String[] returnProjects = null;
		int noOfProjects = 0, i=0;
		Datasource ds;
		Dataset resDS = null;
		returnProjects = new String[1];
		returnProjects[0] = "All Projects"; 
		
		SRConnection con = null;
				
		String sqlQuery = "";
		if(projectName.compareToIgnoreCase("All Projects") == 0){
		sqlQuery = "SELECT PROJECT_NAME FROM MOD_IA_PROJECTS;";
		}
		else{
			sqlQuery = "SELECT PROJECT_NAME FROM MOD_IA_PROJECTS where PROJECT_NAME = '" + projectName + "' ;";
		}
	//	log.error("getProjects: "+sqlQuery);
		try {
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			resDS = con.runQuery(sqlQuery);
			if(resDS != null && resDS.getRowCount() > 0)
			{
				noOfProjects = resDS.getRowCount();
				returnProjects = new String[noOfProjects + 1];
				returnProjects[0] = "All Projects"; //add the option for all projects
				for(i=0; i<noOfProjects; i++)
				{
					if(resDS.getValueAt(i,0) != null)
					{
						returnProjects[i+1] = resDS.getValueAt(i,0).toString();
					}
				}
				
			}
			
			
		}
		catch (Exception e) {
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getProjects : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		return returnProjects;
	}

	/***
	 * Method to remove a project from monitoring by Ignition Analytics module.
	 * @author YM : Created on 08/24/2015
	 */
	@Override
	public String[] deleteAndGetUpdatedProjectsList(String projectName) {
		
		Datasource ds;
		
		String[] returnProjects = null;
		int noOfProjects = 0, i=0;
		Dataset resDS = null;
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String sqlSelectQuery = "SELECT PROJECT_NAME FROM MOD_IA_PROJECTS;";
		String sqlDeleteQuery = "DELETE FROM MOD_IA_PROJECTS WHERE PROJECT_NAME = '" + projectName.trim() + "';";
		
		List<Projects_Sync> projects = new ArrayList<Projects_Sync>();
		Projects_Sync _p ;
		try {
			con = ds.getConnection();
			//delete teh requested project
			con.runUpdateQuery(sqlDeleteQuery);
			
			//retrieve modified list 
			
			resDS = con.runQuery(sqlSelectQuery);
			if(resDS != null && resDS.getRowCount() > 0)
			{
				noOfProjects = resDS.getRowCount();
				returnProjects = new String[noOfProjects + 1];
				returnProjects[0] = "All Projects"; //add the option for all projects
				for(i=0; i<noOfProjects; i++)
				{
					if(resDS.getValueAt(i,0) != null)
					{
						returnProjects[i+1] = resDS.getValueAt(i,0).toString();
						_p = new Projects_Sync();
						_p.projectName =  resDS.getValueAt(i,0).toString();
						projects.add(_p);
					}
				}
				
			}
			
			
				//call service on controller to delete projects
				GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
				
				if(this.isAgent)
				{
					ServiceManager sm = gm.getServiceManager();
			
					ServerId sid = new ServerId(this.controllerName);
					ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
					//if service is available
					if(s == ServiceState.Available)
					{
						//call the service 
						boolean status = sm.getService(sid, GetAnalyticsInformationService.class).get().receiveProjects(Constants.PROJECTS_DELETE, gm.getServerAddress().getServerName(), projects);
					}
				}
				else
				{
					this.receiveProjects(Constants.PROJECTS_DELETE, gm.getServerAddress().getServerName(), projects);
				}
			
			
		}
		catch (SQLException e) {
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("deleteAndGetUpdatedProjectsList : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		return returnProjects;	
	}
	/**
	 * Method to retrieve the overview information for given duration and given project/all projects
	 */
	@Override
	public OverviewInformation getOverview( int duration,
			String projectName, boolean allProjects) {
			OverviewInformation info = new OverviewInformation();
	
	Datasource ds;
	Dataset resDS = null;
	ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
	SRConnection con = null;
			
	String dateFilter = getDateFilter(duration, "session_start");
	String sqlQuery = "";
	String filter = "";
	String filterScreens = "";
	
	int noOfUsers = this.getNumberOfActiveUsers( duration, projectName, allProjects);
	
	if(allProjects)
	{
		filter = "";
		filterScreens = filter;
	}
	else
	{
		filter = " and PROJECT_NAME = '" + projectName + "' ";
		filterScreens = " and a.ORIGINATING_SYSTEM = 'Project=' + '" + projectName + "' ";		
	}
	
	sqlQuery = "select sum(coalesce((no_of_screens+no_of_actions), 0)) as ACTIONS,"
			+ " coalesce((time_format(SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))),'%H:%i:%s') ) , \"00:00:00\") as AVG_SESSION_DURATION,"
			+ " coalesce((SUM(no_of_screens)/count(session_start)), 0) as SCREENS_PER_SESSION,"
			+ " count(session_start) as sessions"
			+ " from mod_ia_daily_sessions  WHERE "
			+ dateFilter + filter + ";";
	log.error("getOverview : " + sqlQuery);
	
	
	try {
		con = ds.getConnection();
		resDS = con.runQuery(sqlQuery);
		if(resDS != null && resDS.getRowCount() > 0)
		{
			if(resDS.getValueAt(0, 0) != null)
			{
				info.setNoOfActions((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
			}
			if(resDS.getValueAt(0, 1) != null)
			{
				info.setAvgSessionDuration(resDS.getValueAt(0, 1).toString());
			}
			if(resDS.getValueAt(0, 2) != null)
			{
				info.setAverageScreensPerVisit((int)Float.parseFloat(resDS.getValueAt(0, 2).toString()));
			}
			if(resDS.getValueAt(0, 3) != null)
			{
				info.setNoOfSessions((int)Float.parseFloat(resDS.getValueAt(0, 3).toString()));
			}
			
		}
		
		info.setNoOfActiveUsers(noOfUsers);
		//get the bounce rate values for given duration if the duration is not today or yesterday

			String screenViewDateFilter = getDateFilter(duration, "a.VIEW_TIMESTAMP");
			String auditDateFilter = getDateFilter(duration, "EVENT_TIMESTAMP");
			String sessionDateFilter = getDateFilter(duration,"s.session_start");
			if(allProjects){
//				sqlQuery = "SELECT count(screen_name) noOfScreenViews"
//						+ " FROM mod_ia_screen_views WHERE "
//						+ screenViewDateFilter + " and ACTION='SCREEN_OPEN'"
//						+ " group by username having count(screen_name) = 1;";
		
			
				sqlQuery = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
						 + " (SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
						 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
						 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
						 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME) as x"
						 + " where x.PROJECT = b.PROJECT_NAME"
						 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
			
			}
			else{
				
				
				sqlQuery = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
						 + " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
						 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
						 + " and a.project = '" + projectName + "' "
						 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
						 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
						 + " where x.PROJECT = b.PROJECT_NAME"
						 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
				
//				sqlQuery = "SELECT count(screen_name) noOfScreenViews,username"
//						+ " FROM mod_ia_screen_views WHERE "
//						+ screenViewDateFilter + " and project = '" + projectName + "' and ACTION='SCREEN_OPEN'"
//						+ " group by username having count(screen_name) = 1;";
			}
			resDS = con.runQuery(sqlQuery);
			
			float usersWithOneScreen = 0;
			float totalUsers = 0;
			if(resDS != null && resDS.getRowCount() > 0)
			{
				if(resDS.getValueAt(0, 0) != null)
				{
					usersWithOneScreen = resDS.getRowCount();
				}

			}
			
			sqlQuery = " SELECT count(distinct(concat(a.actor , b.auth_profile))) as noOfUsers"
					+ " FROM audit_events a, mod_ia_projects b WHERE action = 'login' and status_code = 0"
					+ " and a.ORIGINATING_SYSTEM = concat('Project=' , b.PROJECT_NAME ) and "
					+ auditDateFilter + filterScreens + ";";
//			log.error("getOverview : query noOfysers " + sqlQuery);
			
			resDS = con.runQuery(sqlQuery);
			
			if(resDS != null && resDS.getRowCount() > 0)
			{
				if(resDS.getValueAt(0, 0) != null)
				{
					totalUsers = Float.parseFloat(resDS.getValueAt(0, 0).toString());
				}
			}
			if(usersWithOneScreen == 0 || totalUsers == 0)
			{
				info.setBounceRate(0);
			}
			else
			{
				info.setBounceRate(usersWithOneScreen/totalUsers);
			}
	}
	catch (SQLException e) {
	
	log.error(e);
}
finally{
	if(con!=null){
	try {
		con.close();
	} catch (SQLException e) {
		log.error("getOverview : in con close exception.");
		
		e.printStackTrace();
	}
	
	}
}

int totalScreens = 0;
List<ScreensCount> allScreenViews = this.getScreensViewedCounts( duration, projectName, allProjects);
if(allScreenViews != null)
{
	int noOfRecords = allScreenViews.size();
	for(int k=0; k<noOfRecords; k++)
	{
		totalScreens = totalScreens + allScreenViews.get(k).getNoOfViews();
	}
	info.setNoOfScreenViews(totalScreens);
}
info.setScreenViews(allScreenViews);
	//calculate gateway up and downtime in given duration
 	Calendar firstHour = Calendar.getInstance();
    firstHour.set(Calendar.HOUR_OF_DAY, 00);
    firstHour.set(Calendar.MINUTE, 00);
    firstHour.set(Calendar.SECOND, 00);
    
    int month = 0;
    int year;
    Date sDate;
    Date todayDate = new Date();
    long startTime;
	long endTime;
	long diffTime;
	long diffDays;

    
    
	Double gDownTime = this.getGatewayDowntime(duration);
	
	Double totalTime = 86400.000;
	Double gUpTime = 0.000;
	switch(duration)
	{
	case Constants.TODAY:
		totalTime = 86400.000;
		break;
	case Constants.YESTERDAY:
		totalTime = 86400.000;
		break;
	case Constants.LAST_SEVEN_DAYS:	
		case Constants.LAST_WEEK:
		totalTime =604800.000;
		break;
	case Constants.LAST_THIRTY_DAYS:
		totalTime =2592000.000;
		break;
	case Constants.LAST_NINTY_DAYS:
		totalTime =7776000.000;
		break;
	case Constants.LAST_365_DAYS:
		totalTime =31536000.000;
		break;
	case Constants.LAST_MONTH:
		month = firstHour.get(Calendar.MONTH);
		if(month == 0)
		{
			year = firstHour.get(Calendar.YEAR);
			firstHour.set(year - 1, 11, 1);
		}
		else
		{
			firstHour.set(Calendar.MONTH, month - 1);
		}
		totalTime = (double)(firstHour.getActualMaximum(Calendar.DAY_OF_MONTH) * 24 * 60 * 60) ;
		break;

	case Constants.LAST_YEAR:
		year = firstHour.get(Calendar.YEAR);
		firstHour.set(year - 1,0,1);
		totalTime = (double)firstHour.getActualMaximum(Calendar.DAY_OF_YEAR) * 24 * 60 * 60 ;
		break;
	case Constants.THIS_MONTH:
		month = firstHour.get(Calendar.MONTH);
		firstHour.set(Calendar.DAY_OF_MONTH, 1);
		sDate = firstHour.getTime();
		startTime = sDate.getTime();
		endTime = todayDate.getTime();
		totalTime = (double)(endTime - startTime);
		totalTime = totalTime / 1000;
		break;
	case Constants.THIS_WEEK:
		firstHour.set(Calendar.DAY_OF_WEEK, firstHour.getFirstDayOfWeek());
		sDate = firstHour.getTime();
		startTime = sDate.getTime();
		endTime = todayDate.getTime();
		
		totalTime = (double)(endTime - startTime);
		totalTime = totalTime / 1000; //seconds
		break;
	case Constants.THIS_YEAR:
		year = firstHour.get(Calendar.YEAR);
		firstHour.set(year,0,1);
		sDate = firstHour.getTime();
		startTime = sDate.getTime();
		endTime = todayDate.getTime();
		totalTime = (double)(endTime - startTime);
		totalTime = totalTime / 1000;
		break;
	default:
	}
	//calculate up time , down time and percentages
	int day;
	long hours, minutes, seconds;
	
	
		gUpTime = totalTime - gDownTime;
	
	
	if(gDownTime == 0)
	{
		info.setGatewayDownTimePercent(0);
		info.setGatewayUpTimePercent(100);
		info.setGatewayDownTimeString("0 days, 0 hours, 0 minutes, 0 seconds");
	}
	else
	{
		
		info.setGatewayDownTimePercent((float)(gDownTime  /totalTime)* 100);
		info.setGatewayUpTimePercent((float)(gUpTime  /totalTime)* 100);
	
		//calculate days for down time
		
		
		gDownTime = gDownTime * 1000; //get val in ms
		day = (int)(gDownTime / (1000 * 60 * 60 * 24));        
		hours =  (long)(gDownTime / (1000 * 60 * 60)) % 24;
		minutes = (long)(gDownTime / (1000 * 60)) % 60;;
		seconds = (long)(gDownTime / 1000) % 60;
		info.setGatewayDownTimeString(day + " days, " + hours +  " hours, "
				 + minutes + " minutes, " + seconds + " seconds");
	}
	
	//calculate days for uptime
	
	gUpTime = gUpTime * 1000; //get val in ms
	day = (int)(gUpTime / (1000 * 60 * 60 * 24));        
	hours =  (long)(gUpTime / (1000 * 60 * 60)) % 24;
	minutes = (long)(gUpTime / (1000 * 60)) % 60;;
	seconds = (long)(gUpTime / 1000) % 60;
	
	info.setGatewayUpTimeString(day + " days, " + hours +  " hours, "
			 + minutes + " minutes, " + seconds + " seconds");
	
return info;
	}

	/**
	 * Method to retrieve the overview information for given duration and given project/all projects
	 */
	@Override
	public OverviewInformation getYesterdayOverview( int duration,
			String projectName, boolean allProjects) {
			
		OverviewInformation info = new OverviewInformation();
		
		
		Datasource ds;
		Dataset resDS = null;
		Dataset resDS1 = null;
		String startDate = "", endDate = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
//		String dateFilter = getDateFilter(duration, "overview_date");
		String sqlQuery = "";
		String sqlQueryTotalUsers = "";
		String sqlScreensQuery = "";
		String sqlScreensQueryTotal = "";

		//y'day overview would always be yday and not relative. 
		// as per Chris's comment on 11th Jan , QA doc query 67
		
		String dateFilter = getDateFilter(duration, "session_start");
		String dateFilterAuditEvents = getDateFilter(duration, "EVENT_TIMESTAMP");
		String screenViewDateFilter = getDateFilter(duration, "a.VIEW_TIMESTAMP");
		String sessionDateFilter = getDateFilter(duration,"s.session_start");
		if(allProjects)
		{
//			sqlQuery = "select sum(ACTIONS), dbo.SEC_TO_TIME(AVG(AVG_SESSION_DURATION)), SUM(BOUNCE_RATE), AVG(SCREENS_PER_SESSION), SUM(TOTAL_SCREENVIEWS), SUM(TOTAL_SESSIONS),"
//				+ " SUM(TOTAL_USERS), SUM(NEW_USERS), AVG(ACTIONS_PER_SESSION) from mod_ia_daily_overview"
//				+ " WHERE " + dateFilter ;
			sqlQuery = "select sum(no_of_screens+no_of_actions) as ACTIONS,"
					+ " SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))) as AVG_SESSION_DURATION,"
					+ " (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION,"
					+ " count(session_start) as sessions"
					+ " from mod_ia_daily_sessions  WHERE "
					+ dateFilter + ";";
			
//			sqlScreensQuery = "SELECT count(screen_name) noOfScreenViews,username"
//					+ " FROM mod_ia_screen_views WHERE "
//					+ screenViewDateFilter + " and ACTION='SCREEN_OPEN'"
//					+ " group by username having count(screen_name) = 1;";
			
		
			sqlScreensQuery = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
					 + " (SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME) as x"
					 + " where x.PROJECT = b.PROJECT_NAME"
					 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
			
			sqlScreensQueryTotal = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
					 + " (SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME) as x"
					 + " where x.PROJECT = b.PROJECT_NAME"
					 + " ;";
			
			
			
			sqlQueryTotalUsers =  "SELECT count(distinct(concat(a.actor , b.AUTH_PROFILE)))"
					+ " from AUDIT_EVENTS a,  mod_ia_projects b"
					+ " where a.ORIGINATING_SYSTEM = concat('project=' , b.PROJECT_NAME)"
					+ " and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
					+ dateFilterAuditEvents + " ;"; 
		}
		else
		{
			sqlQuery = "select sum(no_of_screens+no_of_actions) as ACTIONS,"
					+ " SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))) as AVG_SESSION_DURATION,"
					+ " (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION,"
					+ " count(session_start) as sessions"
					+ " from mod_ia_daily_sessions  WHERE "
					+ dateFilter + " and PROJECT_NAME = '" + projectName + "' ;";
			
			sqlScreensQuery = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
					 + " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
					 + " and a.project = '" + projectName + "' "
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
					 + " where x.PROJECT = b.PROJECT_NAME"
					 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
			
			sqlScreensQueryTotal = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
					 + " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
					 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
					 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
					 + " and a.project = '" + projectName + "' "
					 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
					 + " where x.PROJECT = b.PROJECT_NAME"
					 + " ;";
			
			sqlQueryTotalUsers =  "SELECT count(distinct(concat(a.actor , b.AUTH_PROFILE)))"
					+ " from AUDIT_EVENTS a,  mod_ia_projects b"
					+ " where a.ORIGINATING_SYSTEM = concat('project=' ,b.PROJECT_NAME)"
					+ " and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
					+ dateFilterAuditEvents + " and ORIGINATING_SYSTEM like '%"
					+ projectName + "';";
		}
		try {
			con = ds.getConnection();
			resDS = con.runQuery(sqlQuery);
			if(resDS != null && resDS.getRowCount() > 0)
			{
				if(resDS.getValueAt(0, 0) != null)
				{
					info.setNoOfActions((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
				}
				if(resDS.getValueAt(0, 1) != null)
				{
					info.setAvgSessionDuration(resDS.getValueAt(0, 1).toString());
				}
				if(resDS.getValueAt(0, 2) != null)
				{
					info.setAverageScreensPerVisit(Float.parseFloat(resDS.getValueAt(0, 2).toString()));
				}
				if(resDS.getValueAt(0, 3) != null)
				{
					info.setNoOfSessions((int)(Float.parseFloat(resDS.getValueAt(0, 3).toString())));
				}
			
			}
			
			resDS = con.runQuery(sqlScreensQuery);
			float usersWithOneScreen = 0;
			float totalUsers = 0;
			if(resDS != null && resDS.getRowCount() > 0)
			{
//				if(resDS.getValueAt(0, 0) != null)
//				{
					usersWithOneScreen = resDS.getRowCount();
				//}

			}
			
			resDS = con.runQuery(sqlQueryTotalUsers);
//			if(resDS1 != null)
//			{
//				info.setNoOfActiveUsers ((int) resDS1.getRowCount());
//			}
			
			
			if(resDS != null && resDS.getRowCount() > 0)
			{
				if(resDS.getValueAt(0, 0) != null)
				{
					totalUsers = Float.parseFloat(resDS.getValueAt(0, 0).toString());
					info.setNoOfActiveUsers ((int)totalUsers);
				}
			}
			if(usersWithOneScreen == 0 || totalUsers == 0)
			{
				info.setBounceRate(0);
			}
			else
			{
				info.setBounceRate(usersWithOneScreen/totalUsers);
			}
			
			info.setNoOfActiveUsers ((int)totalUsers);
			
			
		}
		catch (SQLException e) {
		
		log.error("getYesterdayOverview: "+e);
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getYesterdayOverview : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		
	return info;
	}
	/**
	 * function to get average clear time for alarms
	 */
	@Override
	public String getAverageClearTime(int duration,  String projectName, boolean allProjects) {
		Datasource ds;
		Dataset resDS = null;
		String timeToClear = "";
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		String sqlQuery = "";
		SRConnection con = null;
			
		//eventtype = 0 means alarms generated , eventtype = 1 means alarm cleared
		
		String dateFilter = getDateFilter(duration, "ALARM_DATE" );
		
		
		
		int month = getLastMonth();
		int year = getThisYear();
		int thisYear = getThisYear();
		if(month == 0)
		{
			month = 12;
			year = getLastYear();
		}
		
		int lastYear = getLastYear();
		
		sqlQuery = "SELECT time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_CLEAR,0.0)))),'%H:%i:%s')"
				+ " FROM MOD_IA_DAILY_ALARMS_SUMMARY WHERE "
				+ dateFilter ;
		int r=0;
			try {
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							timeToClear = resDS.getValueAt(0, 0).toString();
					
						}
					}
					
				}
				catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getAverageClearTime : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
		}
		
		return timeToClear;
	}

	/**
	 * function to get average acknowledgment time for alarms
	 */
	@Override
	public String getAverageAckTime(int duration,  String projectName, boolean allProjects) {
		Datasource ds;
		Dataset resDS = null;
		String timeToAck = "";
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		String sqlQuery = "";
		SRConnection con = null;
		
		
		String dateFilter = getDateFilter(duration, "ALARM_DATE");
		//eventtype = 0 means alarms generated , eventtype = 2 means alarm acknowledged
		
	
		
		int month = getLastMonth();
		int year = getThisYear();
		int thisYear = getThisYear();
		if(month == 0)
		{
			month = 12;
			year = getLastYear();
		}
		
		int lastYear = getLastYear();
		
		
		
		sqlQuery = "SELECT time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_ACK,0.0)))),'%H:%i:%s')"
				+ " FROM MOD_IA_DAILY_ALARMS_SUMMARY WHERE "
				+ dateFilter ;
		int r=0;
			try {
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							//try and add following code 
							//String s = new SimpleDateFormat("HH.mm.ss.SSS").format(resDS.getValueAt(0, 0).toString());
							//timeToAck = Long.parseLong(s);
							timeToAck = resDS.getValueAt(0, 0).toString();
					
						}
					}
					
				}
				catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getAverageAckTime : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
		}
	return timeToAck;
	}

	/** 
	 * method to retrieve alarms information to be sent to the client 
	 * this method calls various functions and sends the combined information as alarmsInformation
	 *@author YM : Created on 06/23/2015
	 * @see AlarmsInformation
	 */
	@Override
	public AlarmsInformation getAlarmsOverview( int duration,
			String projectName, boolean allProjects) {
		
		AlarmsInformation _alarms = new AlarmsInformation();
		HashMap<String, Integer> mapActAlarms = new HashMap<String, Integer>();
		HashMap<String, Integer> mapAckAlarms = new HashMap<String, Integer>();
		HashMap<String, String> mapTimeToClearAlarms = new HashMap<String, String>();
		HashMap<String, String> mapTimeToAckAlarms = new HashMap<String, String>();
		
		Dataset temp;
		int i, noOfRecs, noOfAlarms = 0;
		//get list of active alarms priority wise and calculate total count
		temp  = this.getActiveAlarmsCount( projectName, allProjects);
		if(temp != null)
		{
			noOfRecs = temp.getRowCount();
			for(i=0; i<noOfRecs; i++)
			{
				noOfAlarms = noOfAlarms + (int)Double.parseDouble(temp.getValueAt(i, 1).toString());
				mapActAlarms.put(temp.getValueAt(i, 0).toString(), (int)Double.parseDouble(temp.getValueAt(i, 1).toString()));
			}
			
			_alarms.setActiveAlarmsCount(mapActAlarms);
			_alarms.setNoOfActiveAlarms(noOfAlarms);
		}
		
		//get list of acknowledged alarms priority wise and calculate total count
		noOfAlarms = 0;
		temp  = this.getAckAlarmsCount( projectName, allProjects);
		if(temp != null)
		{
			noOfRecs = temp.getRowCount();
			for(i=0; i<noOfRecs; i++)
			{
				
				noOfAlarms = noOfAlarms + (int)Double.parseDouble(temp.getValueAt(i, 1).toString());
				mapAckAlarms.put(temp.getValueAt(i, 0).toString(), (int)Double.parseDouble(temp.getValueAt(i, 1).toString()));
			}
			
			_alarms.setAckAlarmsCount(mapAckAlarms);
			_alarms.setNoOfAckAlarms(noOfAlarms);
		}
		
		//get list of average time to clear alarms per priority
				
				temp  = this.getAlarmsClearTime(duration,  projectName, allProjects);
				if(temp != null)
				{
					noOfRecs = temp.getRowCount();
					for(i=0; i<noOfRecs; i++)
					{
						
						mapTimeToClearAlarms.put(temp.getValueAt(i, 0).toString(),temp.getValueAt(i, 1).toString());
					}
					
					_alarms.setTimeToClearAlarmsPerPriority(mapTimeToClearAlarms);
					
				}
				
				//get list of average time to acknowledge alarms per priority
				
				temp  = this.getAlarmsAckTime(duration, projectName, allProjects);
				if(temp != null)
				{
					noOfRecs = temp.getRowCount();
					for(i=0; i<noOfRecs; i++)
					{
						String s = "00:00:00.0"; 
						if(temp.getValueAt(i, 1) != null)
						{
							//s = new SimpleDateFormat("HH.mm.ss.S").format(temp.getValueAt(i, 1));
							s = temp.getValueAt(i, 1).toString();
						}
						//timeToAck = Long.parseLong(s);
						mapTimeToAckAlarms.put(temp.getValueAt(i, 0).toString(), s);
					}
					
					_alarms.setTimeToAckAlarmsPerPriority(mapTimeToAckAlarms);
					
				}
				_alarms.setAvgClearTime(this.getAverageClearTime(duration, projectName, allProjects));
				_alarms.setAvgAckTime(this.getAverageAckTime(duration,  projectName, allProjects));
		
				return _alarms;
	}

	@Override
	public DevicesInformation getDeviceInformation( int duration,
			String projectName, boolean allProjects) {
	
		Datasource ds;
		Dataset resDS = null;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		DevicesInformation retInfo = new DevicesInformation();
		String sqlQuery = "";
		SRConnection con = null;

		String dateFilter = getDateFilter(duration, "b.START_TIMESTAMP");

		String filter = "";
		if(allProjects )
		{
			filter = "";				
		}
		
		else 
		{
			filter = " and a.PROJECT_NAME = '" + projectName + "' ";				
		}
		
		
		
		sqlQuery = "select case"
				+ " when b.IS_MOBILE = 0 then 'Desktop' "
				+ " when b.IS_MOBILE = 1 then 'Mobile' end as 'deviceType', count(distinct SESSION_START) as Sessions "
				+ " FROM mod_ia_daily_sessions a, mod_ia_clients b where "
				+ dateFilter + filter
				+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
				+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end"
				+ " and a.SESSION_CONTEXT = b.CLIENT_CONTEXT and a.HOSTNAME = b.HOSTNAME"
				+ " and a.username = b.username group by IS_MOBILE;";
//		log.error("getDeviceInformation :" + sqlQuery);
		
		int r=0, noOfRows;
			try {
				
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						noOfRows = resDS.getRowCount();
						for(r=0; r<noOfRows; r++)
						{
							if(resDS.getValueAt(r, 0).toString().compareToIgnoreCase("Desktop") == 0)
							{
								retInfo.setNoOfClientsOnDesktop(Integer.parseInt(resDS.getValueAt(r, 1).toString()));
							}
							else
							{
								retInfo.setNoOfClientsOnMobile(Integer.parseInt(resDS.getValueAt(r, 1).toString()));
							}
						}
						
					}
					
				}
				catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getDeviceInformation : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
		}
	return retInfo;
	}

	/**
	 * Method to retrieve Top Screens information 
	 * Returns a Dataset with Screen name, No Of People and No of Actions(Screen views)
	 */
	@Override
	public Dataset getTopScreens( int duration, String projectName,
			boolean allProjects) {
		
		Dataset repScreens = null;
		Datasource ds;
		String sqlQuery = "";
		
		SRConnection con = null;
		String timefilter = getDateFilter(duration, "VIEW_TIMESTAMP");
		
		if(allProjects)
		{
			sqlQuery = "SELECT SCREEN_NAME as 'PageViews', COUNT(DISTINCT USERNAME) as 'People',"
					+ " COUNT(SCREEN_NAME) as 'Actions'"
					+ " FROM mod_ia_screen_views where action = 'SCREEN_OPEN' and "
					+ timefilter 
					+ " group by SCREEN_NAME order by People desc, Actions desc;";
		}
		else
		{
			sqlQuery = "SELECT SCREEN_NAME as 'PageViews', COUNT(DISTINCT USERNAME) as 'People',"
					+ " COUNT(SCREEN_NAME) as 'Actions'"
					+ " FROM mod_ia_screen_views where action = 'SCREEN_OPEN' and "
					+ timefilter + " and Project = '" + projectName + "'"
					+ " group by SCREEN_NAME order by People desc, Actions desc;";
		}
		
		//connect to the database and get records
		try{
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			repScreens = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("GetTopScreens : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getTopScreens : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return repScreens;
	}
	/* Report for Platform Report. By Omkar*/ 
	@Override
	public Dataset getPlatformReport( int duration, String projectName, boolean allProjects) {
		Dataset platformDataSet = null;
		Datasource dsname;
		String sqlQuery = "";
		
		SRConnection con = null;
		String timefilter = getDateFilter(duration, "START_TIMESTAMP");
		if(allProjects){
			sqlQuery = "select dt.OS_NAME as OS_NAME, count(distinct (concat(dt.username, c.AUTH_PROFILE))) as people,"
					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
					+ " from ("
					+ " select distinct b.OS_NAME as OS_NAME, "
					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b"
					+ " where " + timefilter + " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
					+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end and a.username = b.username"
					+ " and a.SESSION_CONTEXT = b.CLIENT_CONTEXT and a.HOSTNAME = b.HOSTNAME"
					+ " ) as dt,"
					+ " mod_ia_projects c"
					+ " where dt.PROJECT = c.PROJECT_NAME group by OS_NAME ;";
		}
		else {

			sqlQuery = "select dt.OS_NAME as OS_NAME, count(distinct (concat(dt.username, c.AUTH_PROFILE))) as people,"
					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
					+ " from ("
					+ " select distinct b.OS_NAME as OS_NAME, "
					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b"
					+ " where " + timefilter + "and b.project = '" + projectName + "' and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
					+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end and a.username = b.username"
					+ " and a.SESSION_CONTEXT = b.CLIENT_CONTEXT and a.HOSTNAME = b.HOSTNAME"
					+ " ) as dt, "
					+ "mod_ia_projects c"
					+ " where dt.PROJECT = c.PROJECT_NAME group by OS_NAME ;";
			
			
		}
		
		
		try {
			
			dsname = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = dsname.getConnection();
//			log.error("platform report sql q : " + sqlQuery);
			platformDataSet = con.runQuery(sqlQuery);
			
		} catch (Exception e) {
			// TODO: handle exception
			log.error("Get Platforms :"+e);
		}
		finally{
			if(con != null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getPlatformReport : in con close exception.");
					// TODO: handle exception
					log.error("Get Platforms :"+e);
					e.printStackTrace();
				}
			}
		}
		
		
		
	return platformDataSet;
	
	}
	@Override
	public CurrentOverview getCurrentOverview( String projectName,
			boolean allProjects) {
		
		CurrentOverview info = new CurrentOverview();
		List<UserSessionsInfo> sessions = this.getActiveSessions( projectName, allProjects);
		HashMap<String, String> distinctUsers = new HashMap<String,String>();
		HashMap<String, Date> distinctUserLogins = new HashMap<String,Date>();
		int noOfSessions = 0;
		long totalSessionsLength = 0;
		
		Date created = new Date();
		Date tempDate;
		
		if(sessions != null)
		{
			noOfSessions = sessions.size();
		}
//		log.error("getCurrentOverview : no of sessions " + noOfSessions);
		info.setNoOfActiveSessions(noOfSessions);
		String user = "";
		String project = "";
		boolean addSessionFlag = false;
		for(int k=0; k<noOfSessions; k++)
		{
				user = sessions.get(k).getUserName() + ":" + sessions.get(k).getProfileName();
				created = new Date(sessions.get(k).getCreationTime());
				
				totalSessionsLength = totalSessionsLength + sessions.get(k).getTimeDifference();
				distinctUsers.put(user, user);
				if(distinctUserLogins.containsKey(user))
				{
					tempDate = distinctUserLogins.get(user); //get the old value in hash map
					if(created.before(tempDate))
					{
						distinctUserLogins.put(user,created); //put the user name and session start time
					}
				}
				else
				{
					distinctUserLogins.put(user,created);
				}
			
			
		}
		info.setDistinctUsers(distinctUsers);
		int noOfDistinctUsers = distinctUsers.size();
		String[] users = new String[noOfDistinctUsers];
		int index = 0;
		Iterator<HashMap.Entry<String,String>> itr = distinctUsers.entrySet().iterator();
		while(itr.hasNext())
		{
			HashMap.Entry<String,String> userRec = itr.next();
			users[index++] = userRec.getValue();
		
		}
		info.setUserLocations(this.getGeoInformationForUsers(this.moduleDS, users));
//		log.error("getCurrentOverview : no of active users  " + noOfDistinctUsers);
		info.setNoOfActiveUsers(noOfDistinctUsers);
		info.setActiveSessionLength(totalSessionsLength/1000); //in seconds
		//info.setUsersPerScreenRealTime(this.getNumberOfUsersPerScreenRealTime(ds, projectName, allProjects));
		info.setNoOfActionsByCurrentUsers(this.getNumActionsByCurrentUsers(projectName, allProjects, distinctUserLogins));
		info.setScreenViewscountPerUser(this.getNumberOfScreensViewedCurrentUsers( projectName, allProjects));
		info.setLocationDeviceBrowsers(this.getRealTimeLocationsDevicesAndBrowsers(projectName, allProjects, distinctUserLogins));
		
		return info;
	}
	
	
	
	/**
	 * Method to check if we already have geo information for given location with IP
	 * @param locationIP
	 * @param dsName
	 * @return true/false
	 */
	public boolean IfLocationExists(String locationIntIP, String locationExtIP)
	{
		boolean locationExists = false;
		Datasource ds;
		String sqlQuery = "";
		Dataset result;
		SRConnection con = null;
		
			sqlQuery = "SELECT EXTERNAL_IP  FROM mod_ia_location_info where INTERNAL_IP = '"
					+ locationIntIP  + "' AND EXTERNAL_IP = '" + locationExtIP + "';";
		
//		log.error("IfLocationExists was called from : " + locationIntIP);
		//connect to the database and get records
		try{
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			result = con.runQuery(sqlQuery);
			if(result == null || result.getRowCount() == 0)
			{
				locationExists = false;
			}
			else
			{
				if(result.getValueAt(0, 0)!= null && result.getValueAt(0, 0).toString().compareToIgnoreCase(locationExtIP) == 0)
				{
					locationExists = true;
				}
				else
				{
					locationExists = false;
				}
			}
		}
		catch(Exception e){
			log.error("IfLocationExists : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("IfLocationExists : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return locationExists;
	}
	
	/**
	 * Method to retrieve user's geo location information
	 * Created by : YM on 06/26/2015
	 * @param userName
	 * @return UserLocation record
	 * @see UserLocations
	 */
	private List<UserLocations> getGeoInformationForUsers(Long dsName, String[] users)
	{
		List<UserLocations> userLocations = new ArrayList<UserLocations>();
		
		Datasource ds;
		String sqlQuery = "";
		Dataset result;
		SRConnection con = null;
		String userName;
		String profileName;
		//connect to the database and get records
		try{
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			
			for(int i=0; i<users.length; i++)
			{
				userName = users[i].split(":")[0];
				profileName = users[i].split(":")[1];
				sqlQuery = "SELECT a.USERNAME, b.city, b.state, b.country, b.LATITUDE, b.LONGITUDE"
						+ " FROM mod_ia_clients a, mod_ia_location_info b"  
						+ " WHERE a.HOST_INTERNAL_IP = b.INTERNAL_IP and a.HOST_EXTERNAL_IP = b.EXTERNAL_IP and a.START_TIMESTAMP ="
						+ " (select max(START_TIMESTAMP) from mod_ia_clients where username = '"
						+ userName + "' and PROJECT in  "
						+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '"
						+ profileName + "'));";
				
				
				result = con.runQuery(sqlQuery);
				if(result != null && result.getRowCount() > 0)
				{
					UserLocations userLoc = new UserLocations();
					userLoc.setUserName(result.getValueAt(0, 0).toString());
					userLoc.setUserAuthProfile(profileName);
					if(result.getValueAt(0, 1) != null)
					{
						userLoc.setCity(result.getValueAt(0, 1).toString());
					}
					if(result.getValueAt(0, 2) != null)
					{
						userLoc.setState(result.getValueAt(0, 2).toString());
					}
					if(result.getValueAt(0, 3) != null)
					{
						userLoc.setCountry(result.getValueAt(0, 3).toString());
					}
					if(result.getValueAt(0, 4) != null)
					{
						userLoc.setLatitude(Double.parseDouble(result.getValueAt(0, 4).toString()));
					}
					if(result.getValueAt(0, 5) != null)
					{
						userLoc.setLongitude(Double.parseDouble(result.getValueAt(0, 5).toString()));
					}
					userLocations.add(userLoc);
				}
			}
			
			
		}
		catch(Exception e){
			log.error("getGeoInformationForUsers : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getGeoInformationForUsers : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		
		return userLocations;
	}
	
	/**
	 * A method to retrieve time in seconds for which gateway was down in a given duration
	 * @param dsName
	 * @param duration
	 * @return downtime in seconds
	 */
	private Double getGatewayDowntime( int duration)
	{
		Double downTime = 0.0;
		String dateFilter = getDateFilter(duration, "b.event_timestamp");
		Datasource ds;
		
		Dataset result;
		SRConnection con = null;
		
		String sqlQuery = "select sum(timestampdiff(second,shutdownTime, startTime))"
				+ "from"
				+ " (select min(a.event_timestamp) as startTime, b.event_timestamp as shutdownTime"
				+ " from MOD_IA_AUDIT_EVENTS a,MOD_IA_AUDIT_EVENTS b"
				+ " where a.actor = b.actor  and a.action = 'GATEWAY_START' and b.action = 'GATEWAY_SHUTDOWN'"
				+ " and " + dateFilter + " and b.event_timestamp <= a.event_timestamp"
				+ " group by shutdownTime order by shutdownTime desc) as dt;";
		try{
			
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				
				result = con.runQuery(sqlQuery);
				if(result != null && result.getRowCount() > 0)
				{
					if(result.getValueAt(0, 0) != null)
					{
						//downTime = new Double(Double.parseDouble(result.getValueAt(0, 0).toString())).longValue();
						downTime = Double.parseDouble(result.getValueAt(0, 0).toString());
					}
					
				}
		}
		catch(Exception e){
			log.error("getGatewayDowntime : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getGatewayDowntime : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return downTime;
	}

	@Override
	public UsersOverviewInformation getUserInformation( int duration, String uName,
			String projectName, boolean allProjects, String userAuthProfile) {
		
		UsersOverviewInformation uinfo = new UsersOverviewInformation();
		uinfo.setFirstSeen(this.getFirstSeen( uName, projectName, allProjects,userAuthProfile));
		uinfo.setLastSeen(this.getLastSeen( uName, projectName, allProjects,userAuthProfile));
		uinfo.setTotalActionsLast7Days(this.getTotalActions( uName, duration, projectName, allProjects,userAuthProfile));
		uinfo.setTotalSessionsLength(this.getTotalSessionsLength( uName, duration, projectName, allProjects, userAuthProfile));
		uinfo.setTotalVisitsLast7Days(this.getTotalVisits( uName, duration, projectName, allProjects, userAuthProfile));
		uinfo.setScreensViewedLast7Days(this.getScreensViewedCountsPerUser( duration, uName, projectName, allProjects, userAuthProfile));
		uinfo.setCurrentScreen(this.getCurrentScreenForUser( uName, projectName, allProjects, userAuthProfile));
		
		String location = this.getUserLocation(uName, userAuthProfile);
		location = location.replace(", null, ", ", ");
		location = location.replace("false, ", " ");
		location = location.replace("null", "None");
		uinfo.setLocation(location);
		
		//overall till now.
		uinfo.setTotalActions(this.getTotalActions( uName, Constants.LAST_365_DAYS, projectName, allProjects,userAuthProfile));
		uinfo.setTotalVisits(this.getTotalVisits( uName, Constants.LAST_365_DAYS, projectName, allProjects, userAuthProfile));
		uinfo.setScreensViewed(this.getScreensViewedCountsPerUser( Constants.LAST_365_DAYS, uName, projectName, allProjects, userAuthProfile));
		return uinfo;
	}

	/**
	 * A function to get reports data - Overview by Date
	 */
	@Override
	public Dataset reoprtsGetOverviewByDate( int duration,
			String projectName, boolean allProjects) {
		
		Dataset overview = null;
		Datasource ds;
		String sqlQuery = "";
		
		SRConnection con = null;
		String timefilter = getDateFilter(duration, "event_timestamp");
//		String viewTimefilter = getDateFilter(duration, "overview_date");
		String modIAFilter = getDateFilter(duration, "session_start");
		String filter = "";
		String filter1 = "";
		if(allProjects)
		{
			filter = filter1 = "";
		}
		else{
			
			filter = " and PROJECT_NAME = '" + projectName + "'";
			filter1 = " and a.ORIGINATING_SYSTEM = 'project=' + '" + projectName + "'";
		}
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
//				sqlQuery = "select a.Hour as Hour, b.TOTAL_USERS, b.TOTAL_SESSIONS, b.ACTIONS"
//						+ " from mod_ia_hours as a "
//						+ " left join (select overview_hour,SUM(TOTAL_USERS) as TOTAL_USERS,SUM(TOTAL_SESSIONS) as TOTAL_SESSIONS, SUM(ACTIONS ) as ACTIONS"
//						+ " from mod_ia_hourly_overview where "
//						+ modIAFilter + " group by overview_hour) as b"
//						+ " on b.overview_hour = a.Hour"
//						+ " order by a.Hour desc;";
				sqlQuery ="select a.hour as Hour, b.people, b.visits, b.ACTIONS from mod_ia_hours as a"
						+ " left join (select tbl1.eventHour as overviewHour ,tbl1.people as people, tbl2.visits as visits,"
						+ " tbl2.actions as actions from (SELECT count( distinct(concat(a.actor , b.AUTH_PROFILE))) as People,"
						+ " hour(a.EVENT_TIMESTAMP) as eventHour FROM AUDIT_EVENTS a,  mod_ia_projects b"
						+ " where actor != 'SYSTEM' and STATUS_CODE = 0 and a.ORIGINATING_SYSTEM = concat('project=' , b.project_name) and "
						+ timefilter + filter1
						+ " group by eventHour ) as tbl1,"
						+ " (select sum(no_of_screens+NO_OF_ACTIONS) as actions, count(SESSION_START) as visits,"
						+ " hour(SESSION_START) as overviewHour from mod_ia_daily_sessions where "
						+ modIAFilter + filter
						+ " group by overviewHour) as tbl2 where tbl2.overviewHour = tbl1.eventHour) as b on"
						+ " b.overviewHour = a.hour order by a.hour desc; ";
			}
			else if(duration == Constants.THIS_YEAR || duration == Constants.LAST_YEAR )
			{
//				sqlQuery =  "select a.monthName, b.TOTAL_USERS, b.TOTAL_SESSIONS, b.ACTIONS"
//						+ " from mod_ia_month as a "
//						+ " left join (select  month(overview_date) as overviewmonth ,SUM(TOTAL_USERS) as TOTAL_USERS , "
//						+ " SUM(TOTAL_SESSIONS) as TOTAL_SESSIONS, SUM(ACTIONS) as ACTIONS "
//						+ " from mod_ia_daily_overview where "
//						+ modIAFilter + " group by overviewmonth ) as b"
//						+ " on b.overviewmonth = a.monthNumber"
//						+ " order by a.monthNumber desc ;";
				sqlQuery =  "select a.monthName, b.People, b.visits, b.actions, a.monthNumber"
						+ " from mod_ia_month as a left join"
						+ " (select tbl2.overviewmonth as overviewmonth, tbl2.visits as  visits,"
						+ " tbl1.people as people, tbl2.actions as actions from "
						+ " (SELECT count( distinct(concat(a.actor , b.AUTH_PROFILE))) as People, month(a.event_timestamp) as eventMonth "
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ " where a.ORIGINATING_SYSTEM = concat('project=' , b.PROJECT_NAME ) and " + timefilter + filter1
						+ " and actor != 'SYSTEM' and status_code = 0 group by month(a.event_timestamp)) as tbl1, "
						+ " (select  month(session_date) as overviewmonth , count(SESSION_START) as visits, sum(no_of_screens+NO_OF_ACTIONS) as actions"
						+ " from mod_ia_daily_sessions where " + modIAFilter + filter
						+ " group by month(session_date)) as tbl2 where tbl1.eventMonth = tbl2.overviewmonth) as b"
						+ " on a.monthNumber = b.overviewmonth order by a.monthNumber desc;";
			}
			else if( duration == Constants.LAST_365_DAYS)
			{
				sqlQuery =  "select a.monthName, b.People, b.visits, b.actions, a.monthNumber"
						+ " from mod_ia_month as a left join"
						+ " (select tbl2.overviewmonth as overviewmonth, tbl2.visits as  visits,"
						+ " tbl1.people as people, tbl2.actions as actions from "
						+ " (SELECT count( distinct(concat(a.actor , b.AUTH_PROFILE))) as People, month(a.event_timestamp) as eventMonth "
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ " where a.ORIGINATING_SYSTEM = concat('project=' , b.PROJECT_NAME ) and " + timefilter + filter1
						+ " and actor != 'SYSTEM' and status_code = 0 group by month(a.event_timestamp)) as tbl1, "
						+ " (select  month(session_date) as overviewmonth , count(SESSION_START) as visits, sum(no_of_screens+NO_OF_ACTIONS) as actions"
						+ " from mod_ia_daily_sessions where " + modIAFilter + filter
						+ " group by month(session_date)) as tbl2 where tbl1.eventMonth = tbl2.overviewmonth) as b"
						+ " on a.monthNumber = b.overviewmonth order by a.monthNumber;";
			}
			else
			{
//				sqlQuery = "select tbl1.eventDate, tbl1.people, tbl2.visits, tbl3.actions"
//					+ " from "
//					+ " ("
//					+ " SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, date(a.event_timestamp) as eventDate "
//					+ " FROM AUDIT_EVENTS a,  mod_ia_projects b "
//					+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and " + timefilter 
//					+ " and actor != 'SYSTEM' group by eventDate"
//					+ " ) as tbl1,"
//					+ " ("
//					+ " select count(action) as visits,  date(event_timestamp) as eventDate"
//					+ " from AUDIT_EVENTS "
//					+ " where action='login' and " + timefilter
//					+ " group by eventDate"
//					+ " ) as tbl2,"
//					+ " ("
//					+ " select (a.acts + coalesce(b.screenViews,0) ) as actions, a.actionDate"
//					+ " from"
//					+ " (select count(action) as acts, date(event_timestamp) as actionDate"
//					+ " from AUDIT_EVENTS"
//					+ " where " + timefilter 
//					+ " and action not in ('GATEWAY_START', 'GATEWAY_SHUTDOWN') group by actionDate) as a "
//					+ " left join "
//					+ " (select count(action) as screenViews, date(view_timestamp) as viewDate"
//					+ " from mod_ia_screen_views"
//					+ " where " + viewTimefilter + " group by viewDate) as b"
//					+ " on a.actionDate = b.viewDate group by a.actionDate"
//					+ " ) as tbl3"
//					+ " where tbl1.eventDate = tbl2.eventDate and tbl1.eventDate = tbl3.actionDate"
//					+ " group by tbl1.eventDate order by tbl1.eventDate desc;";
				
				sqlQuery = "select tbl1.eventDate, tbl1.people, tbl2.visits, tbl2.actions"
						+ " from "
						+ " ("
						+ " SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, date(a.event_timestamp) as eventDate "
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ " where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and " + timefilter + filter1
						+ " and actor != 'SYSTEM' and status_code = 0 group by eventDate"
						+ " ) as tbl1,"
						+ " ( select sum(no_of_screens+NO_OF_ACTIONS) as actions, count(SESSION_START) as visits, SESSION_DATE "
						+ " from mod_ia_daily_sessions where " + modIAFilter + filter
						+ " group by SESSION_DATE) as tbl2"
						+ " where tbl2.SESSION_DATE = tbl1.eventDate order by tbl1.eventDate desc;";
				
				
				
				
			}
		
		
		//connect to the database and get records
		try{
			
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
		//	log.error("By Date Report query is " + sqlQuery);
			overview = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("reoprtsGetOverviewByDate : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("reoprtsGetOverviewByDate : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return overview;
	}

	/**
	 * 
	 */
	
	private String getUserLocation(String uName, String userProfile)
	{
		String uLocation = "";
		Dataset resDS = null;
		Datasource ds;
		String sqlQuery = "";
		
		SRConnection con = null;
	
			sqlQuery = "SELECT city ,state, country "
					+ " from mod_ia_location_info a , mod_ia_clients b"
					+ " where a.internal_ip = b.HOST_INTERNAL_IP and a.external_ip = b.HOST_EXTERNAL_IP and b.USERNAME = '"
					+ uName + "' and b.PROJECT in "
					+ " ( SELECT PROJECT_NAME from mod_ia_projects where AUTH_PROFILE = '" + userProfile + "')"
					+ " and b.start_timestamp = (Select max(start_timestamp) from mod_ia_clients where username='" + uName + "');";
		
		
		//connect to the database and get records
		try{
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			resDS = con.runQuery(sqlQuery);
			
			if(resDS != null && resDS.getRowCount() > 0)
			{
				uLocation = "";
				String _city = "", _state = "", _country = "";
				if(resDS.getValueAt(0, 0) != null)
				{
					_city = resDS.getValueAt(0, 0).toString();
					uLocation = uLocation + _city;
				}
				if(resDS.getValueAt(0, 1) != null)
				{
					_state = resDS.getValueAt(0, 1).toString();
					uLocation = uLocation + ", " + _state;
				}
				if(resDS.getValueAt(0, 2) != null)
				{
					_country = resDS.getValueAt(0, 2).toString();
					uLocation = uLocation + ", " + _country;
				}
				
			}
		}
		catch(Exception e){
			log.error("getUserLocation : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getUserLocation : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		
		return uLocation;
	}
	/**
	 * 
	 */
	@Override
	public Dataset getTotalVisitsData(String projectName,
			boolean allProjects, int duration) {
	
		Dataset totalVisits = null;
		Datasource ds;
		String sqlQuery = "";
		String timeFilter = getDateFilter(duration, "session_start");
		SRConnection con = null;
		String filter ="";
		
		
		if(allProjects)
		{
			filter = "";
		}
		else{
			filter = " and project_name = '" + projectName + "' ";
		}
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				
				sqlQuery = "select a.Hour as Hour, sum(total_sessions)"
						+ " from mod_ia_hours as a "
						+ " left join (select count(session_start) as total_sessions, hour(session_start) as overview_hour from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by overview_hour ) as b"
						+ " on b.overview_hour = a.Hour "
						+ " group by a.Hour ;";
			}
			else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
			{
				sqlQuery = "select a.monthName, total_sessions, a.monthNumber"
						+ " from mod_ia_month as a "
						+ " left join (select count(session_start) as total_sessions, month(session_date) as overviewmonth"
						+ " from mod_ia_daily_sessions where "
						+ timeFilter + filter +  " group by overviewmonth) as b"
						+ " on b.overviewmonth = a.monthNumber"
						+ " order by a.monthNumber;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
//				sqlQuery = "select dayname(overview_date), sum(total_sessions) from mod_ia_daily_overview where "
//						+ timeFilter + " group by overview_date;";
				sqlQuery = "select dayname(session_date), count(session_start) from mod_ia_daily_sessions where "
						+ timeFilter + filter +  " group by session_date order by session_date;";
			}

			else if(duration == Constants.THIS_MONTH ) 
			{
//				sqlQuery = "select day(overview_date), sum(total_users) from mod_ia_daily_overview where "
//						+ timeFilter + " group by overview_date;";
				
				sqlQuery = "select b.dayAll, coalesce(a.total_sessions, 0) from ("
						+ " select day(session_date) as dayActual, count(session_start) as total_sessions "
						+ " from mod_ia_daily_sessions where " + timeFilter + filter
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-1) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_MONTH )
			{
				sqlQuery = "select b.dayAll, coalesce(a.total_sessions, 0) from ("
						+ " select day(session_date) as dayActual, count(session_start) as total_sessions "
						+ " from mod_ia_daily_sessions where " + timeFilter + filter
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-2) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_THIRTY_DAYS )
			{
				sqlQuery = "select day(session_date), count(session_start) from mod_ia_daily_sessions where "
						+ timeFilter + filter +  " group by session_date"
								+ " order by session_date;";
			}
			else
			{
				sqlQuery = "select day(session_date), count(session_start) from mod_ia_daily_sessions where "
						+ timeFilter + filter +  " group by session_date"
								+ " order by session_date;";						
			}
		
		
		//connect to the database and get records
		try{
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			totalVisits = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("getTotalVisitsData : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getTotalVisitsData : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return totalVisits;
	}


	// Modified Function getTotalUsersData on 18 Jan 2016
		@Override
		public Dataset getTotalUsersData( String projectName,
				boolean allProjects, int duration) {
			Dataset totalUsers = null;
			Datasource ds;
			String sqlQuery = "";
			String timeFilter = "";

			timeFilter = getDateFilter(duration, "event_timestamp");
			SRConnection con = null;
			
			if(allProjects)
			{
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{

					sqlQuery = "select a.Hour as Hour, total_users" 
							+ " from mod_ia_hours as a  left join "
							+ " ("
							+ "		select count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users,hour(event_timestamp) as event_timestamp"
							+ " 	from AUDIT_EVENTS a,  mod_ia_projects b where " 
							+ " 	a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 "
							+ " 	and "+ timeFilter +""
							+ " 	group by hour(event_timestamp) )as b"
							+ " on b.event_timestamp = a.Hour" 
							+ " group by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, total_users, a.monthNumber"
							+ " from mod_ia_month as a "
							+ " left join (select count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users, month(event_timestamp) as overviewmonth"
							+ " from audit_events a,  mod_ia_projects b where "
							+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
							+ " and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
							+ timeFilter + " group by month(event_timestamp) ) as b"
							+ " on b.overviewmonth = a.monthNumber order by a.monthNumber"
							+ " ;";
					
					

				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
					
				sqlQuery = "select dayname(event_timestamp) as overviewmonth, count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users, event_timestamp "
						+ " from audit_events a,  mod_ia_projects b where "
						+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
						+ " and a.actor != 'SYSTEM' and action = 'login' and a.status_code = 0 and "
						+ timeFilter 
						+ " group by dayname(event_timestamp)"
						+ " order by event_timestamp;"; 
					
				}
				else if(duration == Constants.THIS_MONTH ) 
				{
					
					sqlQuery = "select b.dayAll, coalesce(a.total_users, 0) from ("
							+ " select day(event_timestamp) as dayActual, count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users "
							+ " from audit_events a,  mod_ia_projects b  where " 
							+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
							+ " and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
							+ timeFilter 
							+ " group by day(event_timestamp) ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.total_users, 0) from ("
							+ " select day(event_timestamp) as dayActual, count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users "
							+ " from audit_events a,  mod_ia_projects b  where " 
							+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
							+ " and a.actor != 'SYSTEM' and action = 'login' and a.status_code = 0 and "
							+ timeFilter 
							+ " group by day(event_timestamp )) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_THIRTY_DAYS )
				{
					sqlQuery = "select day(event_timestamp) as dayActual, count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users "
							+ " from audit_events a,  mod_ia_projects b  where " 
							+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
							+ " and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
							+ timeFilter 
							+ " group by day(event_timestamp)"
							+ " order by day(event_timestamp);";
				}
				else
				{
					
					sqlQuery = "select day(event_timestamp) as dayActual, count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users "
							+ " from audit_events a,  mod_ia_projects b  where " 
							+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
							+ " and a.actor != 'SYSTEM' and a.action = 'login' and and a.status_code = 0 and "
							+ timeFilter 
							+ " group by day(event_timestamp)"
							+ " order by day(event_timestamp);";
							
				}
				
			}
			else
			{
				
				
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, total_users"
						+ " from mod_ia_hours as a "
						+ " left join (select count(distinct(actor)) as total_users,hour(event_timestamp) as overview_hour"
						+ " from AUDIT_EVENTS where "
						+ timeFilter + " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=" + projectName + "')"
								+ " group by hour(event_timestamp)) as b"
						+ " on b.overview_hour = a.Hour"
						+ " group by a.Hour;";
					
					
					
				}
				else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, total_users, a.monthNumber"
							+ " from mod_ia_month as a "
							+ " left join (select count(distinct(actor)) as total_users, month(event_timestamp) as overviewmonth"
							+ " from audit_events  where "
							+ timeFilter + " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=" + projectName + "')"
									+ " group by month(event_timestamp)) as b"
							+ " on b.overviewmonth = a.monthNumber"
							+ " order by a.monthNumber;";
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
					sqlQuery = "select dayname(event_timestamp) as dayActual, count(distinct(actor)) as total_users "
							   + " from audit_events where "
							   + timeFilter + " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=" + projectName + "') group by dayname(event_timestamp)"
							   		+ " order by day(event_timestamp);";
				}
				else if(duration == Constants.THIS_MONTH) 
				{
					sqlQuery = "select b.dayAll, coalesce(a.total_users, 0) from ("
							+ " select day(event_timestamp) as dayActual, count(distinct(actor)) as total_users "
							+ " from audit_events where " + timeFilter 
							+ " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=" + projectName + "')" 
							+ " group by day(event_timestamp) ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if( duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.total_users, 0) from ("
							+ "  select day(event_timestamp) as dayActual,  count(distinct(actor)) as total_users "
							+ " from audit_events where " + timeFilter 
							+ " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=" + projectName + "')" 
							+ " group by day(event_timestamp) ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_THIRTY_DAYS )
				{
					
					sqlQuery = "select day(event_timestamp) as dayActual, count(distinct(actor)) as total_users "
							+ " from audit_events where "
							+ timeFilter 
							+ " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=" + projectName + "')" 
							+ "  group by day(event_timestamp)"
							+ " order by day(event_timestamp);";
				}
				else
				{
					sqlQuery = "select day(event_timestamp) as dayActual, count(distinct(actor)) as total_users "
							+ " from audit_events where "
							+ timeFilter 
							+ " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=" + projectName + "')" 
							+ "  group by day(event_timestamp)"
							+ " order by day(event_timestamp);";
				}
			}
			
			
			//connect to the database and get records
			try{
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				totalUsers = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("getTotalUsersData : " + e);
			}finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error(" getTotalUsersData : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return totalUsers;
			
		}
	
	@Override
	public Dataset getTotalScreenViewsData( String projectName,
			boolean allProjects, int duration) {
		Dataset totalScreenviews = null;
		Datasource ds;
		String sqlQuery = "";
		String timeFilter = getDateFilter(duration, "session_start");
		SRConnection con = null;
		String filter = "";
		
		if(allProjects)
		{
			filter = "";
		}
		else{
			filter = " and project_name = '" + projectName + "' ";
		}
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				
				
				sqlQuery = "select a.Hour as Hour, sum(TOTAL_SCREENVIEWS) as TOTAL_SCREENVIEWS"
						+ " from mod_ia_hours as a "
						+ " left join (select sum(no_of_screens) as TOTAL_SCREENVIEWS,"
						+ " hour(session_start) as overview_hour from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by overview_hour) as b"
						+ " on b.overview_hour = a.Hour"
						+ " group by a.Hour;";
			}
			else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
			{
			
				sqlQuery = "select a.monthName, b.TOTAL_SCREENVIEWS, a.monthNumber"
						+ " from mod_ia_month as a "
						+ " left join (select sum(no_of_screens) as TOTAL_SCREENVIEWS, month(session_date) as overviewmonth"
						+ " from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by overviewmonth) as b"
						+ " on b.overviewmonth = a.monthNumber"
						+ " order by a.monthNumber;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
				sqlQuery = "select dayname(session_date), sum(no_of_screens) from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by session_date order by session_date;";
			}
			else if(duration == Constants.THIS_MONTH ) 
			{
				
				sqlQuery = "select b.dayAll, coalesce(a.TOTAL_SCREENVIEWS, 0) from ("
						+ " select day(session_date) as dayActual, sum(no_of_screens) as TOTAL_SCREENVIEWS "
						+ " from mod_ia_daily_sessions where " + timeFilter + filter
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-1) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_MONTH )
			{
				sqlQuery = "select b.dayAll, coalesce(a.TOTAL_SCREENVIEWS, 0) from ("
						+ " select day(session_date) as dayActual, sum(no_of_screens) as TOTAL_SCREENVIEWS "
						+ " from mod_ia_daily_sessions where " + timeFilter + filter
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-2) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_THIRTY_DAYS )
			{
				sqlQuery = "select day(session_date), sum(no_of_screens) from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by session_date order by session_date;";
			}
			else
			{
				sqlQuery = "select day(session_date), sum(no_of_screens) from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by session_date order by session_date;";
						
			}
			
		log.error("getTotalScreenviewsData sql q : " + sqlQuery);
				
		//connect to the database and get records
		try{
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			totalScreenviews = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("getTotalScreenViewsData : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getTotalScreenViewsData : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return totalScreenviews;
	}

	@Override
	public Dataset getBounceRateData( String projectName,
			boolean allProjects, int duration) {
		Dataset bounceRates = null;
		Datasource ds;
		String sqlQuery = "";
		String timeFilter = getDateFilter(duration, "overview_date");
		SRConnection con = null;
		String filter = "";
		
		if(allProjects)
		{
			filter = "";
		}
		else{
			filter = " and project_name = '" + projectName + "' ";
		}
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				sqlQuery = "select a.Hour as Hour, avg(bounceRate) as bounceRate"
					+ " from mod_ia_hours as a "
					+ " left join (select (bounce_rate  * 100 ) as bounceRate, overview_hour from mod_ia_hourly_overview where "
					+ timeFilter + filter + " ) as b"
					+ " on b.overview_hour = a.Hour"
					+ " group by a.Hour;";
			}
			else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
			{
				sqlQuery = "select a.monthName, bounceRate, a.monthNumber"
						+ " from mod_ia_month as a "
						+ " left join (select (avg(bounce_rate)  * 100) as bounceRate, month(overview_date) as overviewmonth"
						+ " from mod_ia_daily_overview where "
						+ timeFilter + filter + " group by overviewmonth ) as b"
						+ " on b.overviewmonth = a.monthNumber"
						+ " order by a.monthNumber;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
				sqlQuery = "select dayname(overview_date), (avg(bounce_rate)  * 100) from mod_ia_daily_overview where "
						+ timeFilter + filter + " group by overview_date order by overview_date;";
			}

			
			else if(duration == Constants.THIS_MONTH ) 
			{
				
				sqlQuery = "select b.dayAll, coalesce(a.bounce_rate, 0) from ("
						+ " select day(overview_date) as dayActual,(avg(bounce_rate)  * 100 ) as bounce_rate "
						+ " from mod_ia_daily_overview where " + timeFilter + filter
						+ " group by overview_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-1) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_MONTH )
			{
				sqlQuery = "select b.dayAll, coalesce(a.bounce_rate, 0) from ("
						+ " select day(overview_date) as dayActual, (avg(bounce_rate)  * 100  ) as bounce_rate "
						+ " from mod_ia_daily_overview where " + timeFilter + filter
						+ " group by overview_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-2) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_THIRTY_DAYS )
			{
				sqlQuery = "select day(overview_date), (avg(bounce_rate)  * 100  ) from mod_ia_daily_overview where "
						+ timeFilter + filter + " group by overview_date"
								+ " order by overview_date;";
			}
			else
			{
				sqlQuery = "select day(overview_date), (avg(bounce_rate) * 100 ) from mod_ia_daily_overview where "
						+ timeFilter + filter +" group by overview_date"
								+ " order by overview_date;";
						
			}
			
		
		
		
		//connect to the database and get records
		try{
			
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			bounceRates = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("getBounceRateData : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getBounceRateData : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return bounceRates;
	}

	@Override
	public Dataset getAvgSessionData( String projectName,
			boolean allProjects, int duration) {
		
		Dataset avgSessions = null;
		Datasource ds;
		String sqlQuery = "";
		String timeFilter = getDateFilter(duration, "session_start");
		SRConnection con = null;
		String filter = "";
		
		if(allProjects)
		{
			filter = "";
		}
		else{
			filter = " and project_name = '" + projectName + "' ";
		}
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{

				sqlQuery = "select a.Hour as Hour, avg(AVG_SESSION_DURATION) as AVG_SESSION_DURATION "
						+ " from mod_ia_hours as a "
						+ " left join (select AVG(TIME_TO_SEC(session_end) - TIME_TO_SEC(session_start))/60 as AVG_SESSION_DURATION, hour(session_start) as overview_hour from "
						+ " mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by overview_hour) as b"
						+ " on b.overview_hour = a.Hour"
						+ "  group by a.Hour;";
			}
			else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
			{
				sqlQuery = "select a.monthName, b.AVG_SESSION_DURATION ,a.monthNumber"
						+ " from mod_ia_month as a "
						+ " left join (select AVG(time_to_sec(session_end)- time_to_sec(session_start))/60 as AVG_SESSION_DURATION,"
						+ " month(session_date) as overviewmonth"
						+ " from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by overviewmonth) as b"
						+ " on b.overviewmonth = a.monthNumber"
						+ " order by a.monthNumber"
						+ " ;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
				sqlQuery = "select dayname(session_date),"
						+ " AVG(time_to_sec(session_end)- time_to_sec(session_start))/60 as AVG_SESSION_DURATION"
						+ " from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by session_date"
								+ " order by session_date;";
			}

			
			else if(duration == Constants.THIS_MONTH ) 
			{
				
				sqlQuery = "select b.dayAll, coalesce(a.AVG_SESSION_DURATION, 0) from ("
						+ " select day(session_date) as dayActual,"
						+ " AVG(time_to_sec(session_end)- time_to_sec(session_start))/60 as AVG_SESSION_DURATION "
						+ " from mod_ia_daily_sessions where " + timeFilter + filter
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-1) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_MONTH )
			{
				sqlQuery = "select b.dayAll, coalesce(a.AVG_SESSION_DURATION, 0) from ("
						+ " select day(session_date) as dayActual,"
						+ " AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION "
						+ " from mod_ia_daily_sessions where " + timeFilter + filter
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-2) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_THIRTY_DAYS )
			{
				sqlQuery = "select day(session_date),"
						+ " AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by session_date"
								+ " order by session_date;";	
			}
			else
			{
				sqlQuery = "select day(session_date),"
						+ " AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by session_date"
								+ " order by session_date;";		
			}
			
		
		//connect to the database and get records
		try{
			
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			avgSessions = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("getAvgSessionData : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error(" getAvgSessionData: in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return avgSessions;
	}

	@Override
	public Dataset getAvgScreenViewsData( String projectName,
			boolean allProjects, int duration) {
		Dataset avgScreenViews = null;
		Datasource ds;
		String sqlQuery = "";
		String timeFilter = getDateFilter(duration, "session_start");
		SRConnection con = null;
		String filter = "";
		
		if(allProjects)
		{
			filter = "";
		}
		else{
			filter = " and project_name = '" + projectName + "' ";
		}
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				
				
				sqlQuery = "select a.Hour as Hour, avg(SCREENS_PER_SESSION) as SCREENS_PER_SESSION"
						+ " from mod_ia_hours as a "
						+ " left join (select (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION, hour(session_start) as overview_hour from mod_ia_daily_sessions where "
						+ timeFilter + filter + " group by overview_hour ) as b"
						+ " on b.overview_hour = a.Hour"
						+ "  group by a.Hour;";
			}
			else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
			{
				
				sqlQuery = "select a.monthName, b.SCREENS_PER_SESSION, a.monthNumber"
						+ " from mod_ia_month as a "
						+ " left join (select (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION, month(session_date) as overviewmonth"
						+ " from mod_ia_daily_sessions where "
						+ timeFilter + filter  + " group by overviewmonth) as b"
						+ " on b.overviewmonth = a.monthNumber"
						+ " order by a.monthNumber;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
				
				sqlQuery = "select dayname(session_date), (SUM(no_of_screens)/count(session_start)) from mod_ia_daily_sessions where "
						+ timeFilter + filter  + " group by session_date order by session_date;";
			}

			else if(duration == Constants.THIS_MONTH ) 
			{
				
				sqlQuery = "select b.dayAll, coalesce(a.SCREENS_PER_SESSION, 0) from ("
						+ " select day(session_date) as dayActual, (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION "
						+ " from mod_ia_daily_sessions where " + timeFilter + filter  
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-1) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_MONTH )
			{
				sqlQuery = "select b.dayAll, coalesce(a.SCREENS_PER_SESSION, 0) from ("
						+ " select day(session_date) as dayActual, (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION "
						+ " from mod_ia_daily_sessions where " + timeFilter  + filter 
						+ " group by session_date ) as a "
						+ " right outer join "
						+ " (SELECT day(date_field) as dayAll from ( "
						+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
						+ " INTERVAL (MONTH(NOW())-2) MONTH + "
						+ " INTERVAL daynum DAY date_field FROM ("
						+ " SELECT t*10+u daynum FROM "
						+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
						+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
						+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
						+ " UNION SELECT 8 UNION SELECT 9) B"
						+ " ORDER BY daynum"
						+ " ) AA"
						+ " ) AAA"
						+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
						+ " order by b.dayAll;"
						;
			}
			else if(duration == Constants.LAST_THIRTY_DAYS )
			{
				sqlQuery = "select day(session_date), (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION from mod_ia_daily_sessions where "
						+ timeFilter + filter  + " group by session_date order by session_date;";
			}
			else
			{
			
				sqlQuery = "select day(session_date), (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION from mod_ia_daily_sessions where "
						+ timeFilter + filter  + " group by session_date order by session_date;";		
			}
			
		
			log.error("getAvgScreenViewsData : " + sqlQuery);
		//connect to the database and get records
		try{
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
			avgScreenViews = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("getAvgScreenViewsData : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getAvgScreenViewsData : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return avgScreenViews;
	}
	
	//functions to be used for formattng teh time date time filters to query appropriate data
	private String getDateFilter(int duration, String columnName)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("");
		String dateFilter = "";
		switch(duration){
		case Constants.TODAY:
			dateFilter = columnName + " > '" + getDayAndTime(Constants.YESTERDAY) + "' and " + columnName + "< '" + getDayAndTime(Constants.TODAY) + "'";
			break;
		case Constants.YESTERDAY:
			dateFilter = columnName + " > '" + getDayAndTime(Constants.DAY_BEFORE_YESTERDAY) + "' and " + columnName + "< '" + getDayAndTime(Constants.YESTERDAY) + "'";
			break;
		case Constants.LAST_SEVEN_DAYS:
			dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_SEVEN_DAYS) + "'";
			break;
		case Constants.LAST_THIRTY_DAYS:
			dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_THIRTY_DAYS) + "'";
			break;
		case Constants.LAST_NINTY_DAYS:
			dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_NINTY_DAYS) + "'";
			break;
		case Constants.LAST_365_DAYS:
			dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_365_DAYS) + "'";
			break;
		case Constants.LAST_MONTH:
			dateFilter = columnName + " >= '" + getDayAndTime(Constants.LAST_MONTH) +
						"' and " + columnName + " < '" + getDayAndTime(Constants.THIS_MONTH)
						+ "'";
			break;
		case Constants.LAST_WEEK:
			dateFilter = columnName + " >= '" + getDayAndTime(Constants.LAST_WEEK) +
			"' and " + columnName + " < '" + getDayAndTime(Constants.THIS_WEEK)
			+ "'";
			break;
		case Constants.LAST_YEAR:
			dateFilter = columnName + " >= '" + getDayAndTime(Constants.LAST_YEAR) +
			"' and " + columnName + " < '" + getDayAndTime(Constants.THIS_YEAR)
			+ "'";
			break;
		case Constants.THIS_MONTH:
			dateFilter = columnName + " >= '" + getDayAndTime(Constants.THIS_MONTH) + "'";
			break;
		case Constants.THIS_WEEK:
			dateFilter = columnName + " >= '" + getDayAndTime(Constants.THIS_WEEK) + "'";
			break;
		case Constants.THIS_YEAR:
			dateFilter = columnName + " >= '" + getDayAndTime(Constants.THIS_YEAR) + "'";
			break;
		default:
		}
		
		if(this.installDate != null && this.installDate.length() > 0 && this.installDate != ("") && columnName.compareToIgnoreCase("overview_date") != 0 && columnName.compareToIgnoreCase("summary_date") != 0 && columnName.compareToIgnoreCase("alarm_date") != 0)
		{
			dateFilter = dateFilter + " and " + columnName + " > '" + this.installDate + "' ";
		}
		return dateFilter;
	}
	
	
	//functions to be used for formattng teh time date time filters to query appropriate data
			private String getDateFilterOnController(int duration, String columnName)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("");
				String dateFilter = "";
				switch(duration){
				case Constants.TODAY:
					dateFilter = columnName + " > '" + getDayAndTime(Constants.YESTERDAY) + "' and " + columnName + "< '" + getDayAndTime(Constants.TODAY) + "'";
					break;
				case Constants.YESTERDAY:
					dateFilter = columnName + " > '" + getDayAndTime(Constants.DAY_BEFORE_YESTERDAY) + "' and " + columnName + "< '" + getDayAndTime(Constants.YESTERDAY) + "'";
					break;
				case Constants.LAST_SEVEN_DAYS:
					dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_SEVEN_DAYS) + "'";
					break;
				case Constants.LAST_THIRTY_DAYS:
					dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_THIRTY_DAYS) + "'";
					break;
				case Constants.LAST_NINTY_DAYS:
					dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_NINTY_DAYS) + "'";
					break;
				case Constants.LAST_365_DAYS:
					dateFilter = columnName + " > '" + getDayAndTime(Constants.LAST_365_DAYS) + "'";
					break;
				case Constants.LAST_MONTH:
					dateFilter = columnName + " >= '" + getDayAndTime(Constants.LAST_MONTH) +
								"' and " + columnName + " < '" + getDayAndTime(Constants.THIS_MONTH)
								+ "'";
					break;
				case Constants.LAST_WEEK:
					dateFilter = columnName + " >= '" + getDayAndTime(Constants.LAST_WEEK) +
					"' and " + columnName + " < '" + getDayAndTime(Constants.THIS_WEEK)
					+ "'";
					break;
				case Constants.LAST_YEAR:
					dateFilter = columnName + " >= '" + getDayAndTime(Constants.LAST_YEAR) +
					"' and " + columnName + " < '" + getDayAndTime(Constants.THIS_YEAR)
					+ "'";
					break;
				case Constants.THIS_MONTH:
					dateFilter = columnName + " >= '" + getDayAndTime(Constants.THIS_MONTH) + "'";
					break;
				case Constants.THIS_WEEK:
					dateFilter = columnName + " >= '" + getDayAndTime(Constants.THIS_WEEK) + "'";
					break;
				case Constants.THIS_YEAR:
					dateFilter = columnName + " >= '" + getDayAndTime(Constants.THIS_YEAR) + "'";
					break;
				default:
				}
				return dateFilter;
			}
	/**
	 * 
	 *@param duration time frame for which information is to be retrieved
	 * @return start Date for the time frame
	 */
	private String getDayAndTime(int duration)
	{
		Date retStartDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
		String sDate = "";
		Calendar lastHour = Calendar.getInstance();
	    lastHour.set(Calendar.HOUR_OF_DAY, 23);
	    lastHour.set(Calendar.MINUTE, 59);
	    lastHour.set(Calendar.SECOND, 59);
	   
	    
	    Calendar firstHour = Calendar.getInstance();
	    firstHour.set(Calendar.HOUR_OF_DAY, 00);
	    firstHour.set(Calendar.MINUTE, 00);
	    firstHour.set(Calendar.SECOND, 00);
	    int year, month;
	    try {
	   // startDate = cal.getTime();
		switch(duration){
		case Constants.TODAY:
			lastHour.setTime(new Date());
			// the data is queried 10 minutes prior because other stored procedures run at 10 mins interval so we should have consistent data.
			//lastHour.set(Calendar.MINUTE, ( lastHour.get(Calendar.MINUTE) - 10));
			sDate = sdf.format(lastHour.getTime());
			break;
		case Constants.YESTERDAY:
			lastHour.add(Calendar.DATE, -1);
			sDate = sdf.format(lastHour.getTime());
			break;
		case Constants.DAY_BEFORE_YESTERDAY:
			lastHour.add(Calendar.DATE, -2);
			sDate = sdf.format(lastHour.getTime());
			break;
		case Constants.LAST_SEVEN_DAYS:
			lastHour.add(Calendar.DATE, -7);
			sDate = sdf.format(lastHour.getTime());
			break;
		case Constants.LAST_THIRTY_DAYS:
			lastHour.add(Calendar.DATE, -30);
			sDate = sdf.format(lastHour.getTime());
			
			break;
		case Constants.LAST_NINTY_DAYS:
			lastHour.add(Calendar.DATE, -90);
			sDate = sdf.format(lastHour.getTime());
			
			break;
		case Constants.LAST_365_DAYS:
			lastHour.add(Calendar.DATE, -365);
			sDate = sdf.format(lastHour.getTime());
			break;
		case Constants.LAST_MONTH:
			month = firstHour.get(Calendar.MONTH);
			if(month == 0)
			{
				year = firstHour.get(Calendar.YEAR);
				firstHour.set(year - 1, 11, 1);
				firstHour.set(Calendar.DAY_OF_MONTH, 1);
			}
			else
			{
				firstHour.set(Calendar.MONTH, month - 1);
				firstHour.set(Calendar.DAY_OF_MONTH, 1);
			}
			sDate = sdf.format(firstHour.getTime());
			break;
		case Constants.LAST_WEEK:
			
			firstHour.set(Calendar.WEEK_OF_YEAR, firstHour.get(Calendar.WEEK_OF_YEAR) - 1);
			firstHour.set(Calendar.DAY_OF_WEEK, firstHour.getFirstDayOfWeek());
			sDate = sdf.format(firstHour.getTime());
			break;
		case Constants.LAST_YEAR:
			year = firstHour.get(Calendar.YEAR);
			firstHour.set(year - 1,0,1);
			sDate = sdf.format(firstHour.getTime());
			
			break;
		case Constants.YEAR_BEFORE_LAST:
			year = firstHour.get(Calendar.YEAR);
			firstHour.set(year - 2,0,1);
			sDate = sdf.format(firstHour.getTime());
			break;
		case Constants.THIS_MONTH:
			month = firstHour.get(Calendar.MONTH);
			firstHour.set(Calendar.DAY_OF_MONTH, 1);
			sDate = sdf.format(firstHour.getTime());
			break;
		case Constants.THIS_WEEK:
			firstHour.set(Calendar.DAY_OF_WEEK, firstHour.getFirstDayOfWeek());
			sDate = sdf.format(firstHour.getTime());
			break;
		case Constants.THIS_YEAR:
			 year = firstHour.get(Calendar.YEAR);
			firstHour.set(year,0,1);
			sDate = sdf.format(firstHour.getTime());
			break;
			
		default:
			
		}
	    } catch (Exception e1) {
			
			e1.printStackTrace();
		}
		return sDate;
	}
	
	/*** by Omkar
	 * 
	 */
	
	private String getWeekFilter(int duration){
		String weekDate = "";
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar weekCal = Calendar.getInstance();
		switch (duration) {
		case Constants.TODAY:
			weekDate = df.format(weekCal.getTime());
			break;
			
		case Constants.FIRST_DAY_OFWEEK : 
			weekCal.set(Calendar.DAY_OF_WEEK , weekCal.getFirstDayOfWeek());
			weekDate = df.format(weekCal.getTime());
			break;
		case Constants.FIRST_DAY_OF_LASTWEEK :
			weekCal.set(Calendar.DAY_OF_WEEK,weekCal.getFirstDayOfWeek());
			int week = weekCal.get(Calendar.WEEK_OF_YEAR);
			weekCal.set(Calendar.WEEK_OF_YEAR,week-1);
			weekDate = df.format(weekCal.getTime());
			break;
		case Constants.LAST_DAY_OF_LASTWEEK :
//			weekCal.set(Calendar.WEEK_OF_YEAR, -1);
//			weekCal.set(Calendar.DAY_OF_WEEK,  weekCal.getFirstDayOfWeek() + 6);
			weekCal.set(Calendar.DATE, weekCal.get(Calendar.DATE) - 7);
			weekDate = df.format(weekCal.getTime());
			break;
		case Constants.FIRST_DAY_OF_MONTH : 
			weekCal.set(Calendar.DAY_OF_MONTH, 1);
			weekDate = df.format(weekCal.getTime());
			break;
		case Constants.LAST_DAY_OF_LAST_MONTH :
			int month = weekCal.get(Calendar.MONTH);
			weekCal.set(Calendar.MONTH, month - 1);
			weekDate = df.format(weekCal.getTime());
			
//			weekCal.set(Calendar.DAY_OF_MONTH, 1);
//			weekCal.add(Calendar.DATE, -1);
//			weekDate = df.format(weekCal.getTime());
			break;
		case Constants.FIRST_DAY_OF_LAST_MONTH :
			int curMonth = weekCal.get(Calendar.MONTH);
			weekCal.set(Calendar.MONTH, curMonth-1);
			weekCal.set(Calendar.DAY_OF_MONTH, 1);
			weekDate = df.format(weekCal.getTime());
			break;
			

		default: 
			break;
		}
		
		
	
		return weekDate;
	}
	/**
	 * 
	 * @param duration
	 * @return
	 */
	private String getDurationEndDate(int duration)
	{
		Date retEndDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String eDate = "";
	    Calendar calEnd = Calendar.getInstance();
	    calEnd.set(Calendar.HOUR_OF_DAY, 23);
	    calEnd.set(Calendar.MINUTE, 59);
	    calEnd.set(Calendar.SECOND, 59);
	    calEnd.set(Calendar.MILLISECOND, 0);
	    try {
	   // startDate = cal.getTime();
		switch(duration){
		case Constants.TODAY:
			eDate = sdf.format(new Date());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.YESTERDAY:
			
			calEnd.add(Calendar.DATE, -1);
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.LAST_SEVEN_DAYS:
			
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.LAST_THIRTY_DAYS:
			
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.LAST_NINTY_DAYS:
			
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.LAST_365_DAYS:
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.LAST_MONTH:
			calEnd.set(Calendar.DAY_OF_MONTH, 1);
			   calEnd.set(Calendar.HOUR_OF_DAY, 00);
			    calEnd.set(Calendar.MINUTE, 00);
			    calEnd.set(Calendar.SECOND, 00);
			    calEnd.set(Calendar.MILLISECOND, 0);
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.LAST_WEEK:
			
			calEnd.set(Calendar.DAY_OF_WEEK, calEnd.getFirstDayOfWeek());
			   calEnd.set(Calendar.HOUR_OF_DAY, 00);
			    calEnd.set(Calendar.MINUTE, 00);
			    calEnd.set(Calendar.SECOND, 00);
			    calEnd.set(Calendar.MILLISECOND, 0);
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.LAST_YEAR:
			calEnd.set(Calendar.DAY_OF_YEAR, 1);
			calEnd.set(Calendar.HOUR_OF_DAY, 00);
			calEnd.set(Calendar.MINUTE, 00);
			calEnd.set(Calendar.SECOND, 00);
			calEnd.set(Calendar.MILLISECOND, 0);
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.THIS_MONTH:
			
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.THIS_WEEK:
			
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		case Constants.THIS_YEAR:
			eDate = sdf.format(calEnd.getTime());
			retEndDate = sdf.parse(eDate);
			break;
		default:
			
		}
	    } catch (ParseException e1) {
			
			e1.printStackTrace();
		}
		return eDate;
	}
	
	private int getThisMonth()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MONTH);
	}
	
	private int getLastMonth()
	{
		Calendar cal = Calendar.getInstance();
		return ( cal.get(Calendar.MONTH) - 1);
		
	}
	
	private int getThisYear()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}
	
	private int getLastYear()
	{
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		year = year - 1;
	
		return (  year);
		
	}

	@Override
	public void executeTasksOnce() {
		//this.mycontext.getExecutionManager().executeOnce(new SyncDB(mycontext));
		try
		{
			this.mycontext.getExecutionManager().executeOnce(new HourlyAuditSummaryTask(mycontext));
		}
		catch(Exception e)
		{
			log.error("errror in executeTasksOnce - HourlyAuditSummaryTask" + e);
		}

	}

	/**
	 * A function to retrieve frequency information from user sessions summary table.
	 * @author : YM created on 8/3/2015
	 */
	@Override
	public Dataset getFrequencyInformation( String projectName,
			boolean allProjects, int duration) {
		Dataset freqData = null;
		Datasource ds;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String dateFilter = getDateFilter(duration, "session_start");
		String sqlQuery = "";
		
		if(allProjects)
		{
			
			sqlQuery = "SELECT (concat(a.username , b.AUTH_PROFILE)) as People, count(session_start) Visits, sum(no_of_actions) as Actions,"
					+ " sum(no_of_screens) as noOfScreens from  mod_ia_daily_sessions a,   mod_ia_projects b"
					+ " where a.project_name = b.project_name and "
					+ dateFilter 
					+ " group by concat(a.username , b.AUTH_PROFILE) order by visits;";
		}
		else
		{
			
			
			sqlQuery = "SELECT concat(a.username , b.AUTH_PROFILE) as People, count(session_start) Visits, sum(no_of_actions) as Actions,"
					+ " sum(no_of_screens) as noOfScreens from  mod_ia_daily_sessions a,   mod_ia_projects b"
					+ " where a.project_name = b.project_name and "
					+ dateFilter 
					+ " and a.PROJECT_NAME = '" + projectName + "'"
					+ " group by a.username + b.AUTH_PROFILE order by visits;";
		}
		
		try {
			
			con = ds.getConnection();
			freqData = con.runQuery(sqlQuery);
			
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getFrequencyInformation : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		
		
		
	
		
		return freqData;
	}

	/**
	 * A function to retrieve frequency information from user sessions summary table.
	 * @author : YM created on 8/3/2015
	 */
	@Override
	public Dataset getEngagementInformation( String projectName,
			boolean allProjects, int duration) {
		Dataset freqData = null;
		Datasource ds;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String dateFilter = getDateFilter(duration, "session_start");
		String sqlQuery = "";
		
		if(allProjects)
		{
			sqlQuery = "SELECT  session_duration, count(username), SUM(no_of_screens) "
				+ " FROM mod_ia_daily_sessions"
				+ " WHERE " + dateFilter + " group by session_duration;";
		}
		else
		{
			sqlQuery = "SELECT  session_duration, count(username), SUM(no_of_screens) "
					+ " FROM mod_ia_daily_sessions"
					+ " WHERE " + dateFilter + " and PROJECT_NAME = '" + projectName + "' group by session_duration;";
		}
		
		try {
			con = ds.getConnection();
		
			freqData = con.runQuery(sqlQuery);
			
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			System.out.println("getEngagementInformation : error in con close");
			e.printStackTrace();
		}
		
		}
	}
		
		
		
	
		
		return freqData;
	}
	
	
	/**
	 * A function to retrieve active users information from user sessions summary table.
	 * @author : YM created on 8/3/2015
	 */
	@Override
	public Dataset getActiveUsersInformation( String projectName,
			boolean allProjects, int duration) {
		Dataset activeUsersdata = null;
		Datasource ds;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String dateFilter = getDateFilter(duration, "summary_date");
		String sqlQuery = "";
		
		if(allProjects)
		{
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				sqlQuery = "select a.Hour as Hour, one_day_active_users, seven_day_active_users, fourteen_day_active_users "
					+ " from mod_ia_hours as a "
					+ " left join (select one_day_active_users, seven_day_active_users, fourteen_day_active_users, summary_hour from mod_ia_hourly_active_users where "
					+ dateFilter + " and PROJECT_NAME = 'All' ) as b"
					+ " on b.summary_hour = a.Hour"
					+ " ;";
			}
			else if(duration == Constants.LAST_365_DAYS  )
			{
				String sDate = "";
				Calendar lastHour = Calendar.getInstance();
			    lastHour.set(Calendar.HOUR_OF_DAY, 23);
			    lastHour.set(Calendar.MINUTE, 59);
			    lastHour.set(Calendar.SECOND, 59);
			    
			    int currentYear = lastHour.get(Calendar.YEAR);
				lastHour.add(Calendar.DATE, -365);
				int yearVal = lastHour.get(Calendar.YEAR);
				int monthVal = lastHour.get(Calendar.MONTH);
				
				sqlQuery = "select a.monthName, one_day_active_users, seven_day_active_users, fourteen_day_active_users, a.monthNumber "
						+ " from mod_ia_month as a "
						+ " left join (select one_day_active_users, "
						+ " seven_day_active_users, "
						+ " fourteen_day_active_users, "
						+ " month_no as summary_month  from mod_ia_monthly_active_users where "
						+ " ( year = " + yearVal + " and month_no >= " + monthVal + ") or ( year = " + currentYear + ") "
						+ "  and PROJECT_NAME = 'All' ) as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by a.monthNumber;";
				
		
			}
			else if(duration == Constants.LAST_YEAR )
			{
				sqlQuery = "select a.monthName, one_day_active_users, seven_day_active_users, fourteen_day_active_users "
						+ " from mod_ia_month as a "
						+ " left join (select one_day_active_users, "
						+ " seven_day_active_users, "
						+ " fourteen_day_active_users, "
						+ " month_no as summary_month  from mod_ia_monthly_active_users where "
						+ "  year = year(date(now())) - 1 and PROJECT_NAME = 'All' ) as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by a.monthNumber;";
			}
			else if(duration == Constants.THIS_YEAR )
			{
				sqlQuery = "select a.monthName, one_day_active_users, seven_day_active_users, fourteen_day_active_users "
						+ " from mod_ia_month as a "
						+ " left join (select one_day_active_users, "
						+ " seven_day_active_users, "
						+ " fourteen_day_active_users, "
						+ " month_no as summary_month  from mod_ia_monthly_active_users where "
						+ " year = year(date(now())) and PROJECT_NAME = 'All' ) as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by a.monthNumber;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
				sqlQuery = "select dayname(summary_date), one_day_active_users, seven_day_active_users, fourteen_day_active_users from mod_ia_daily_active_users where "
						+ dateFilter + "  and PROJECT_NAME = 'All' group by summary_date order by summary_date;;";
			}
			else if(duration == Constants.THIS_MONTH || duration == Constants.LAST_MONTH || duration == Constants.LAST_THIRTY_DAYS || duration == Constants.LAST_NINTY_DAYS)
			{
				sqlQuery = "select day(summary_date), one_day_active_users, seven_day_active_users, fourteen_day_active_users from mod_ia_daily_active_users where "
						+ dateFilter + " and PROJECT_NAME = 'All' group by summary_date order by summary_date;;";
			}
			else
			{
				sqlQuery = "select summary_date, one_day_active_users, seven_day_active_users, fourteen_day_active_users from mod_ia_daily_active_users where "
						+ dateFilter + " and PROJECT_NAME = 'All' group by summary_date order by summary_date;;";
						
			}
		}
		else
		{
			
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				sqlQuery = "select a.Hour as Hour, one_day_active_users, seven_day_active_users, fourteen_day_active_users "
					+ " from mod_ia_hours as a "
					+ " left join (select one_day_active_users, seven_day_active_users, fourteen_day_active_users, summary_hour from mod_ia_hourly_active_users where "
					+ dateFilter + " and PROJECT_NAME = '" + projectName + "' ) as b"
					+ " on b.summary_hour = a.Hour"
					+ " ;";
			}
			else if(duration == Constants.LAST_365_DAYS )
			{
				String sDate = "";
				Calendar lastHour = Calendar.getInstance();
			    lastHour.set(Calendar.HOUR_OF_DAY, 23);
			    lastHour.set(Calendar.MINUTE, 59);
			    lastHour.set(Calendar.SECOND, 59);
			    
			    int currentYear = lastHour.get(Calendar.YEAR);
				lastHour.add(Calendar.DATE, -365);
				int yearVal = lastHour.get(Calendar.YEAR);
				int monthVal = lastHour.get(Calendar.MONTH);
				

				sqlQuery = "select a.monthName, sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users), a.monthNumber"
						+ " from mod_ia_month as a "
						+ " left join (select one_day_active_users, "
						+ " seven_day_active_users, "
						+ " fourteen_day_active_users, "
						+ " month_no as summary_month , year from mod_ia_monthly_active_users where "
						+ " ( year = " + yearVal + " and month_no >= " + monthVal + ") or ( year = " + currentYear + ") "
						+ " and PROJECT_NAME = '" + projectName + "') as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by a.monthNumber;";
			}
			else if( duration == Constants.THIS_YEAR )
			{
				
				
				sqlQuery = "select a.monthName, one_day_active_users, seven_day_active_users, fourteen_day_active_users "
						+ " from mod_ia_month as a "
						+ " left join (select one_day_active_users, "
						+ " seven_day_active_users, "
						+ " fourteen_day_active_users, "
						+ " month_no as summary_month  from mod_ia_monthly_active_users where "
						+ " year = year(date(now())) and PROJECT_NAME = '" + projectName + "' ) as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by a.monthNumber;";
			}
			else if(duration == Constants.LAST_YEAR )
			{
								
				sqlQuery = "select a.monthName, one_day_active_users, seven_day_active_users, fourteen_day_active_users "
						+ " from mod_ia_month as a "
						+ " left join (select  one_day_active_users, "
						+ "  seven_day_active_users, "
						+ "  fourteen_day_active_users, "
						+ " month_no as summary_month  from mod_ia_monthly_active_users where year = year(date(now())) - 1  "
						+ " and PROJECT_NAME = '" + projectName + "' ) as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by a.monthNumber;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
				sqlQuery = "select dayname(summary_date), one_day_active_users, seven_day_active_users, fourteen_day_active_users from mod_ia_daily_active_users where "
						+ dateFilter + " and PROJECT_NAME = '" + projectName + "' group by summary_date order by summary_date;;";
			}
			else if(duration == Constants.THIS_MONTH || duration == Constants.LAST_MONTH || duration == Constants.LAST_THIRTY_DAYS || duration == Constants.LAST_NINTY_DAYS)
			{
				sqlQuery = "select day(summary_date), one_day_active_users, seven_day_active_users, fourteen_day_active_users from mod_ia_daily_active_users where "
						+ dateFilter + " and PROJECT_NAME = '" + projectName + "' group by summary_date order by summary_date;;";
			}
			else
			{
				sqlQuery = "select summary_date, one_day_active_users, seven_day_active_users, fourteen_day_active_users from mod_ia_daily_active_users where "
						+ dateFilter + " and PROJECT_NAME = '" + projectName + "' group by summary_date order by summary_date;;";
						
			}
			
		}
		
//		log.error("dashboard getActiveUsersInformation"+sqlQuery);
		try {
			
			con = ds.getConnection();
			activeUsersdata = con.runQuery(sqlQuery);
			
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error(" getActiveUsersInformation : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		
		
		
	
		
		return activeUsersdata;
	}

	/**
	 * Method to retrieve atcive users counts to be set on button labels
	 * @author : YM Created on 8/6/2015.
	 */
	@Override
	public ActiveUsersInfo getActiveUsersCounts( String projectName,
			boolean allProjects, int duration) {
		
		ActiveUsersInfo aInfo = new ActiveUsersInfo();
		int noOfOneDayActiveUsers = 0;
		int noOfSevenDayActiveUsers = 0;
		int noOfFourteenDayActiveUsers = 0;
		int tempVal = 0;
		
		Datasource ds;
		Dataset activeUsersData = null;
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
		
		
		
		
		String dateFilter = getDateFilter(duration, "event_timestamp");
		String startDate = getDayAndTime(duration);
		String durationEndDate = "";
		String sqlQuery = "";
		
		if(allProjects)
		{
			
			
			if(duration == Constants.YESTERDAY || duration == Constants.LAST_WEEK || duration == Constants.LAST_MONTH || duration == Constants.LAST_YEAR)
			{
				durationEndDate = getDurationEndDate(duration);
			
				sqlQuery = "select distinct(concat(a.actor , b.AUTH_PROFILE)) as actor,"
						+ "		DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(a.event_timestamp))) as DaysSinceLogin"
					      + " from AUDIT_EVENTS a, mod_ia_projects b where a.action='login' and a.status_code = 0"
					      + " and event_timestamp < '" + durationEndDate  + "' and event_timestamp >= '" + this.installDate + "'"
					      + " and a.ORIGINATING_SYSTEM = concat('project=', b.project_name)"
					      + " group by concat(a.actor ,b.AUTH_PROFILE); ";
				
				
			}
			else
			{
				
				sqlQuery = "select distinct(concat(a.actor , b.AUTH_PROFILE)) as actor,"
						+ "		DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(a.event_timestamp))) as DaysSinceLogin"
					      + " from AUDIT_EVENTS a, mod_ia_projects b where a.action='login' and a.status_code = 0"
					      + " and a.ORIGINATING_SYSTEM =  concat('project=', b.project_name) and event_timestamp >= '" + this.installDate + "'"
					      + " group by concat(a.actor ,b.AUTH_PROFILE); ";
			}

		}
		else
		{
			if(duration == Constants.YESTERDAY || duration == Constants.LAST_WEEK || duration == Constants.LAST_MONTH || duration == Constants.LAST_YEAR)
			{
				durationEndDate = getDurationEndDate(duration);
			
				sqlQuery = "select distinct(concat(a.actor , b.AUTH_PROFILE)) as actor,"
						+ "		DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(a.event_timestamp))) as DaysSinceLogin"
					      + " from AUDIT_EVENTS a, mod_ia_projects b where a.action='login' and a.status_code = 0"
					 	  + " and ORIGINATING_SYSTEM = 'project=" + projectName + "'"
					      + " and event_timestamp < '" + durationEndDate  + "' and event_timestamp >= '" + this.installDate + "'"
					      + " and a.ORIGINATING_SYSTEM = concat('project=', b.project_name)"
					      + " group by concat(a.actor ,b.AUTH_PROFILE); ";
				
				
			}
			else
			{
				
				sqlQuery = "select distinct(concat(a.actor , b.AUTH_PROFILE)) as actor,"
						+ "		DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(a.event_timestamp))) as DaysSinceLogin"
					      + " from AUDIT_EVENTS a, mod_ia_projects b where a.action='login' and a.status_code = 0"
					 	  + " and ORIGINATING_SYSTEM = 'project=" + projectName + "'"
					      + " and a.ORIGINATING_SYSTEM =  concat('project=', b.project_name) and event_timestamp >= '" + this.installDate + "'"
					      + " group by concat(a.actor ,b.AUTH_PROFILE); ";
			}
		}
		try {
			
			
			con = ds.getConnection();
			
			
			activeUsersData = con.runQuery(sqlQuery);
			
			int noOfrecords = 0;
			if(activeUsersData != null && activeUsersData.getRowCount() > 0)
			{
				noOfrecords = activeUsersData.getRowCount();

				
				for(int i=0; i<noOfrecords; i++)
				{
					tempVal = (int)Float.parseFloat(activeUsersData.getValueAt(i, 1).toString());
					if(tempVal <= 0)
					{
						noOfOneDayActiveUsers = noOfOneDayActiveUsers + 1;
						noOfSevenDayActiveUsers = noOfSevenDayActiveUsers + 1;
						noOfFourteenDayActiveUsers = noOfFourteenDayActiveUsers + 1;
					}
					else if(tempVal >= 1 && tempVal <= 7 )
					{
						noOfSevenDayActiveUsers = noOfSevenDayActiveUsers + 1;
						noOfFourteenDayActiveUsers = noOfFourteenDayActiveUsers + 1;
					}
					else if (tempVal > 7 && tempVal <= 14)
					{
						noOfFourteenDayActiveUsers = noOfFourteenDayActiveUsers + 1;
					}
				}
				
				aInfo.setOneDayActiveUsers(noOfOneDayActiveUsers);
				aInfo.setSevenDayActiveUsers(noOfSevenDayActiveUsers);
				aInfo.setFourteenDayActiveUsers(noOfFourteenDayActiveUsers);
			}
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getActiveUsersCounts : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		return aInfo;
	}

	/**
	 * @return the moduleDS
	 */
	public Long getModuleDS() {
		return moduleDS;
	}

	/**
	 * @param moduleDS the moduleDS to set
	 */
	public void setModuleDS(String moduleDS) {
		this.moduleDS = Long.parseLong(moduleDS);
	}

	

	

	/**
	 * Get the list of projects with overview information to be shown on Projects Panel
	 * @author YM , Created on 8/12/2015
	 */
	@Override
	public Dataset getProjectDetails(int duration, String projectName) {
		Dataset returnData = null;
		
		Datasource ds;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String dateFilter = getDateFilter(duration, "a.SESSION_START");
		String dateFilter1 = getDateFilter(duration, "a.EVENT_TIMESTAMP");
		String sqlQuery = "";
		
		

		if(projectName.compareToIgnoreCase("All Projects") == 0){
//		sqlQuery = "select x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
//				+ " (SELECT b.PROJECT_NAME as Project, sum(a.TOTAL_SESSIONS) as Sessions,"
//				+ " time_format(SEC_TO_TIME(avg(a.AVG_SESSION_DURATION)),'%H:%i:%s')  as avgTime, sum(a.ACTIONS) as Actions"
//				+ " from mod_ia_daily_overview a right outer join mod_ia_projects b"
//				+ " on a.project_name = b.project_name and "
//				+ dateFilter + " group by b.PROJECT_NAME ) as x left outer join "
//				+ " (SELECT count(distinct(actor)) as Users, ORIGINATING_SYSTEM"
//				+ " FROM AUDIT_EVENTS where action='login' and status_code = 0 and "
//				+ dateFilter1 + " group by ORIGINATING_SYSTEM) as y"
//				+ " on y.ORIGINATING_SYSTEM = concat('project=',x.Project);";
		sqlQuery = "select x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
				+ " (SELECT b.PROJECT_NAME as Project, count(a.SESSION_START) as Sessions,"
				+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))),'%H:%i:%s')  as avgTime"
				+ ", sum(a.no_of_screens + a.no_of_actions) as Actions"
				+ " from mod_ia_daily_sessions a right outer join mod_ia_projects b"
				+ " on a.project_name = b.project_name and "
				+ dateFilter + " group by b.PROJECT_NAME ) as x left outer join "
				+ " (SELECT count(distinct(concat(a.actor,b.auth_profile))) as Users, a.ORIGINATING_SYSTEM"
				+ " FROM AUDIT_EVENTS a, mod_ia_projects b where a.action='login' and a.status_code = 0 and"
				+ " a.ORIGINATING_SYSTEM = concat('project=',b.project_name ) and "
				+ dateFilter1 + " group by a.ORIGINATING_SYSTEM) as y"
				+ " on y.ORIGINATING_SYSTEM = concat('project=', x.Project);";
		}
		else
		{
//			sqlQuery = "select x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
//					+ " (SELECT b.PROJECT_NAME as Project, sum(a.TOTAL_SESSIONS) as Sessions,"
//					+ " time_format(SEC_TO_TIME(avg(a.AVG_SESSION_DURATION)),'%H:%i:%s')  as avgTime, sum(a.ACTIONS) as Actions"
//					+ " from mod_ia_daily_overview a right outer join mod_ia_projects b"
//					+ " on a.project_name = b.project_name and "
//					+ dateFilter + " and b.project_name = '" + projectName + "' group by b.PROJECT_NAME ) as x left outer join "
//					+ " (SELECT count(distinct(actor)) as Users, ORIGINATING_SYSTEM"
//					+ " FROM AUDIT_EVENTS where action='login' and status_code = 0 and "
//					+ dateFilter1 + "and ORIGINATING_SYSTEM = 'project=" + projectName + "' group by ORIGINATING_SYSTEM) as y"
//					+ " on y.ORIGINATING_SYSTEM = concat('project=',x.Project);";
		
			sqlQuery = "select x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
					+ " (SELECT b.PROJECT_NAME as Project, count(a.SESSION_START) as Sessions,"
					+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))),'%H:%i:%s')  as avgTime"
					+ ", sum(a.no_of_screens + a.no_of_actions) as Actions"
					+ " from mod_ia_daily_sessions a right outer join mod_ia_projects b"
					+ " on a.project_name = b.project_name and "
					+ dateFilter + " and b.project_name = '" + projectName + "' group by b.PROJECT_NAME ) as x left outer join "
					+ " (SELECT count(distinct(concat(a.actor,b.auth_profile))) as Users, a.ORIGINATING_SYSTEM"
					+ " FROM AUDIT_EVENTS a, mod_ia_projects b where a.action='login' and a.status_code = 0 and"
					+ " a.ORIGINATING_SYSTEM = concat('project=',b.project_name) and "
					+ dateFilter1 + " and a.ORIGINATING_SYSTEM = 'project=" + projectName + "' group by a.ORIGINATING_SYSTEM) as y"
					+ " on y.ORIGINATING_SYSTEM = concat('project=' , x.Project);";
		
		}
		
		log.error("getProjectDetails: "+sqlQuery);
		try {
			
			con = ds.getConnection();
			returnData = con.runQuery(sqlQuery);
			
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getProjectDetails : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		return returnData;
	}

	/**
	 * 
	 * Function to delete a project from gateway
	 * @author : YM , Created on 08/13/2015.
	 */
	 
	@Override
	public void deleteProjectFromGateway(String projectName) {
	
		
		
	}
	
	/**
	 * 
	 */
	@Override
	
	public Dataset getScreenViewsPerUserPerVisitNew(int duration, String projectName, boolean allProjects,String selectedUser, String selectedUserProfile) {
			
			
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			Dataset results = null;
			String sqlQuery = "";
			SRConnection con = null;
			
			String dateFilter = getDateFilter(duration, "c.view_timestamp");
			if(allProjects)
			{
				sqlQuery = "select x.ScreenName as ScreenName, x.User as User, x.SessionStart as SessionStart, x.SessionEnd as SessionEnd,  x.VIEW_TIMESTAMP as VIEW_TIMESTAMP, y.location as location"
						+ " from "
						+ " (select distinct a.screen_name as 'ScreenName', a.username as User, dt.session_start as 'SessionStart',"
						+ " dt.session_end as 'SessionEnd', a.VIEW_TIMESTAMP as VIEW_TIMESTAMP, a.PROJECT as PROJECT, dt.SESSION_CONTEXT" 
						+ " from mod_ia_screen_views a , (SELECT *"
						+ " FROM mod_ia_daily_sessions"
						+ " where username = '"+selectedUser+"'"
						+ " and PROJECT_NAME in (select PROJECT_NAME "
						+ " from mod_ia_projects where AUTH_PROFILE='" + selectedUserProfile + "')"
						+ " order by session_start desc limit 10) as dt"
						+ " where a.username = '"+ selectedUser + "' and a.action = 'SCREEN_OPEN' and a.PROJECT = dt.PROJECT_NAME" // + dateFilter 
						+ " and "
						+ " ("
						+ " (dt.session_start != dt.session_end and a.view_timestamp <= dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start )"
						+ " ||"
						+ "	(dt.session_start = dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start)"
						+ " )"
						+ " order by SessionStart desc, SessionEnd , a.VIEW_TIMESTAMP desc) x, "
						+ " (select a.username as username, a.start_timestamp, a.host_internal_ip, a.host_external_ip, a.project, a.CLIENT_CONTEXT, concat (b.city , ','  , b.state, ',' , b.country) as location"
						+ " from mod_ia_clients a, mod_ia_location_info b "
						+ " where a.host_internal_ip = b.internal_ip and a.host_external_ip = b.external_ip ) y "
						+ " where x.User = y.username and y.start_timestamp > x.SessionStart and y.start_timestamp < x.SessionEnd "
						+ " and x.PROJECT = y.PROJECT and x.SESSION_CONTEXT = y.CLIENT_CONTEXT "
						+ " order by x.sessionStart desc, x.sessionEnd, x.VIEW_TIMESTAMP desc;";
			}
			else
			{
				sqlQuery = "select x.ScreenName as ScreenName, x.User as User, x.SessionStart as SessionStart, x.SessionEnd as SessionEnd,  x.VIEW_TIMESTAMP as VIEW_TIMESTAMP, y.location as location "
						+ " from "
						+ " ( select distinct a.screen_name as 'ScreenName', a.username as User, dt.session_start as 'SessionStart',"
						+ " dt.session_end as 'SessionEnd',a.VIEW_TIMESTAMP, a.PROJECT as PROJECT, dt.SESSION_CONTEXT" 
						+ " from mod_ia_screen_views a ,(SELECT *"
						+ " FROM mod_ia_daily_sessions"
						+ " where username = '"+selectedUser+"'"
								+ " and PROJECT_NAME = '" + projectName + "' "
						+ " order by session_start desc limit 10) as dt"
						+ " where  a.username = '"+ selectedUser + "' and a.action = 'SCREEN_OPEN' and a.PROJECT = dt.PROJECT_NAME" // + dateFilter 
						+ " and "
						+ " ("
						+ " (dt.session_start != dt.session_end and a.view_timestamp <= dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start )"
						+ " || "
						+ " (dt.session_start = dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start)"
						+ " ) "
						+ " order by SessionStart desc, SessionEnd , a.VIEW_TIMESTAMP desc ) x, "
						+ " (select a.username as username, a.start_timestamp, a.host_internal_ip, a.host_external_ip, a.project, a.CLIENT_CONTEXT, concat (b.city , ','  , b.state, ',' , b.country) as location"
						+ " from mod_ia_clients a, mod_ia_location_info b "
						+ " where a.host_internal_ip = b.internal_ip and a.host_external_ip = b.external_ip ) y "
						+ " where x.User = y.username and y.start_timestamp > x.SessionStart and y.start_timestamp < x.SessionEnd"
						+ " and x.PROJECT = y.PROJECT and x.SESSION_CONTEXT = y.CLIENT_CONTEXT "
						+ " order by x.sessionStart desc, x.sessionEnd, x.VIEW_TIMESTAMP desc;";
			}
			log.error("get user sessions q " + sqlQuery);
			
			try {
						con = ds.getConnection();
						results = con.runQuery(sqlQuery);
						
					}
						catch (SQLException e) {
				
					log.error(e);
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getScreenViewsPerUserPerVisitNew : in con close exception.");
						log.error(e);
					}
					
					}
				}	
			return results;
		}
	
	
	
	public Dataset getEngagementInformationScreenDepth( String projectName,
			boolean allProjects, int duration){
		
		Dataset freqData = null;
		Datasource ds;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String dateFilter = getDateFilter(duration, "session_start");
		String sqlQuery = "";
		
		if(allProjects)
		{
			sqlQuery = "select count(username), no_of_screens from mod_ia_daily_sessions where  " 
					+ dateFilter
					+ " group by NO_OF_SCREENS";
			

			
		}
		else
		{
			
			sqlQuery = "select count(username), no_of_screens from mod_ia_daily_sessions where  " 
					+ dateFilter 
					+ " and PROJECT_NAME = '" + projectName.trim() + "' "
					+ " group by NO_OF_SCREENS";
		}
		try {
			con = ds.getConnection();
			
			freqData = con.runQuery(sqlQuery);
			
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getEngagementInformationScreenDepth : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		
		return freqData;

	}
	
	
	/**
	 * A function to get reports data - Bounce rate by Date
	 */
	@Override
	public Dataset getBounceRateReportByDate( int duration,
			String projectName, boolean allProjects) {
		
		Dataset overview = null;
		Datasource ds;
		String sqlQuery = "";
		
		SRConnection con = null;
		String timefilter = getDateFilter(duration, "OVERVIEW_DATE");
		String auditTimeFilter = getDateFilter(duration, "a.EVENT_TIMESTAMP");
		String sessionTimefilter = getDateFilter(duration, "SESSION_START");
		if(allProjects)
		{
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
//				sqlQuery = "select a.Hour as Hour, bounceRate,TOTAL_USERS,TOTAL_SESSIONS"
//						+ " from mod_ia_hours as a "
//						+ " left join (select avg(bounce_rate) * 100 as bounceRate, overview_hour,sum(TOTAL_USERS) as TOTAL_USERS, sum(TOTAL_SESSIONS) as TOTAL_SESSIONS from mod_ia_hourly_overview where "
//						+ timefilter + " group by overview_hour ) as b"
//						+ " on b.overview_hour = a.Hour"
//						+ " ;";
				
				sqlQuery="select a.Hour, b.bounceRate, b.people, b.TOTAL_SESSIONS"
						+ "	from mod_ia_hours as a  left join ("
						+ "	select tbl2.OVERVIEW_HOUR as overviewHour, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ "	from ("
						+ "	SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, hour(a.event_timestamp) as eventHour"
						+ "	FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and " + auditTimeFilter +
						" and actor != 'SYSTEM' group by eventHour ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, OVERVIEW_HOUR , sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ "	from mod_ia_hourly_overview where " + timefilter 
						+ " group by OVERVIEW_HOUR ) as tbl2,"
						+ " (select count(session_start) as TOTAL_SESSIONS, hour(session_start) as sessionHour"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter 
						+ " group by hour(session_start)) as tbl3"
						+ " where tbl1.eventHour = tbl2.OVERVIEW_HOUR and tbl2.OVERVIEW_HOUR = tbl3.sessionHour) as b"
						+ " on a.Hour=b.overviewHour order by a.Hour;";
				
			}
			else if( duration == Constants.THIS_YEAR || duration == Constants.LAST_YEAR)
			{
//				sqlQuery =  "select a.monthName, bounceRate, TOTAL_USERS,TOTAL_SESSIONS"
//						+ " from mod_ia_month as a "
//						+ " left join (select (sum(bounce_rate) / sum(total_users)) * 100 as bounceRate, month(overview_date) as overviewmonth ,TOTAL_USERS,TOTAL_SESSIONS "
//						+ " from mod_ia_daily_overview where "
//						+ timefilter + " group by overviewmonth ) as b"
//						+ " on b.overviewmonth = a.monthNumber"
//						+ " order by a.monthNumber ;";
				
				sqlQuery = "select a.monthName as monthName, b.bounceRate, b.people, b.TOTAL_SESSIONS"
						+ "	from mod_ia_month as a  left join ("
						+ "	select tbl2.overviewmonth as overviewmonth, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ " from ( "
						+ " SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, month(a.event_timestamp) as eventmonth"
						+ "	FROM AUDIT_EVENTS a,  mod_ia_projects b"
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and  " + auditTimeFilter
						+ " and actor != 'SYSTEM' group by eventmonth ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, month(overview_date) as overviewmonth ,"
						+ "	sum(TOTAL_USERS) as TOTAL_USERS, sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ "	from mod_ia_daily_overview where " + timefilter + " group by overviewmonth ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, month(session_date) as sessionMonth"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter
						+ " group by month(session_date)) as tbl3"
						+ " where tbl1.eventmonth = tbl2.overviewmonth and tbl2.overviewmonth = tbl3.sessionMonth) as b"
						+ " on a.monthNumber=b.overviewmonth order by a.monthNumber;";
				
			}
			else if(duration == Constants.LAST_365_DAYS )
			{
				
				sqlQuery = "select a.monthName as monthName, b.bounceRate, b.people, b.TOTAL_SESSIONS, a.monthNumber"
						+ "	from mod_ia_month as a  left join ("
						+ "	select tbl2.overviewmonth as overviewmonth, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ " from ( "
						+ " SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, month(a.event_timestamp) as eventmonth"
						+ "	FROM AUDIT_EVENTS a,  mod_ia_projects b"
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and  " + auditTimeFilter
						+ " and actor != 'SYSTEM' group by eventmonth ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, month(overview_date) as overviewmonth ,"
						+ "	sum(TOTAL_USERS) as TOTAL_USERS, sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ "	from mod_ia_daily_overview where " + timefilter + " group by overviewmonth ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, month(session_date) as sessionMonth"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter
						+ " group by month(session_date)) as tbl3"
						+ " where tbl1.eventmonth = tbl2.overviewmonth and tbl2.overviewmonth = tbl3.sessionMonth) as b"
						+ " on a.monthNumber=b.overviewmonth order by a.monthNumber;";
				
			}
			else
			{
//			sqlQuery = "SELECT OVERVIEW_DATE, SUM(TOTAL_USERS), SUM(TOTAL_SESSIONS), AVG(BOUNCE_RATE) * 100"
//					+ " FROM MOD_IA_DAILY_OVERVIEW WHERE "
//					+ timefilter + " GROUP BY OVERVIEW_DATE;";
				
				sqlQuery = "select tbl2.OVERVIEW_DATE as overviewDate, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ "	from (SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, date(a.event_timestamp) as overviewDate"
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and  " + auditTimeFilter
						+ " and actor != 'SYSTEM' group by overviewDate ) as tbl1, "
						+ " ( select avg(bounce_rate)  * 100 as bounceRate, OVERVIEW_DATE ,"
						+ " sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ " from mod_ia_daily_overview where " + timefilter + " group by OVERVIEW_DATE ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, session_date as sessionDate"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter 
						+ " group by SESSION_DATE) as tbl3"
						+ " where tbl1.overviewDate = tbl2.OVERVIEW_DATE and tbl2.OVERVIEW_DATE = tbl3.sessionDate"
						+ " order by overviewDate;";
			}
		}
		else
		{
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
//				sqlQuery = "select a.Hour as Hour, bounceRate,TOTAL_USERS,TOTAL_SESSIONS"
//						+ " from mod_ia_hours as a "
//						+ " left join (select (bounce_rate / total_users) * 100 as bounceRate, overview_hour,TOTAL_USERS,TOTAL_SESSIONS from mod_ia_hourly_overview where "
//						+ timefilter + " AND PROJECT_NAME = '" + projectName + "' group by overview_hour ) as b"
//						+ " on b.overview_hour = a.Hour"
//						+ " ;";
				sqlQuery="select a.Hour, b.bounceRate, b.people, b.TOTAL_SESSIONS"
						+ "	from mod_ia_hours as a  left join ("
						+ "	select tbl2.OVERVIEW_HOUR as overviewHour, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ "	from ("
						+ "	SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, hour(a.event_timestamp) as eventHour"
						+ "	FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and  " + auditTimeFilter +
						" and actor != 'SYSTEM' and b.PROJECT_NAME = '" + projectName + "' group by eventHour ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, OVERVIEW_HOUR , sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ "	from mod_ia_hourly_overview where " + timefilter 
						+ " and PROJECT_NAME = '" + projectName + "' group by OVERVIEW_HOUR ) as tbl2,"
						+ " (select count(session_start) as TOTAL_SESSIONS, hour(session_start) as sessionHour"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter 
						+ " and PROJECT_NAME = '" + projectName + "' group by hour(session_start)) as tbl3"
						+ " where tbl1.eventHour = tbl2.OVERVIEW_HOUR and tbl2.OVERVIEW_HOUR = tbl3.sessionHour) as b"
						+ " on a.Hour=b.overviewHour"
						+ " order by a.Hour;";
			}
			else if(duration == Constants.THIS_YEAR || duration == Constants.LAST_YEAR)
			{
				sqlQuery = "select a.monthName as monthName, b.bounceRate, b.people, b.TOTAL_SESSIONS"
						+ "	from mod_ia_month as a  left join ("
						+ "	select tbl2.overviewmonth as overviewmonth, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ " from ( "
						+ " SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, month(a.event_timestamp) as eventmonth"
						+ "	FROM AUDIT_EVENTS a,  mod_ia_projects b"
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and  " + auditTimeFilter
						+ " and actor != 'SYSTEM' and b.PROJECT_NAME = '" + projectName + "' group by eventmonth ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, month(overview_date) as overviewmonth ,"
						+ "	sum(TOTAL_USERS) as TOTAL_USERS, sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ "	from mod_ia_daily_overview where " + timefilter + " and PROJECT_NAME = '" + projectName + "'group by overviewmonth ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, month(session_date) as sessionMonth"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter 
						+ " and PROJECT_NAME = '" + projectName + "' group by month(session_date)) as tbl3"
						+ " where tbl1.eventmonth = tbl2.overviewmonth and tbl2.overviewmonth = tbl3.sessionMonth ) as b"
						+ " on a.monthNumber=b.overviewmonth"
						+ " order by a.monthNumber;";
			}
			else if(duration == Constants.LAST_365_DAYS )
			{
				sqlQuery = "select a.monthName as monthName, b.bounceRate, b.people, b.TOTAL_SESSIONS, a.monthNumber"
						+ "	from mod_ia_month as a  left join ("
						+ "	select tbl2.overviewmonth as overviewmonth, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ " from ( "
						+ " SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, month(a.event_timestamp) as eventmonth"
						+ "	FROM AUDIT_EVENTS a,  mod_ia_projects b"
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and  " + auditTimeFilter
						+ " and actor != 'SYSTEM' and b.PROJECT_NAME = '" + projectName + "' group by eventmonth ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, month(overview_date) as overviewmonth ,"
						+ "	sum(TOTAL_USERS) as TOTAL_USERS, sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ "	from mod_ia_daily_overview where " + timefilter + " and PROJECT_NAME = '" + projectName + "'group by overviewmonth ) as tbl2, "
						+ "(select count(session_start) as TOTAL_SESSIONS, month(session_date) as sessionMonth"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter 
						+ " and PROJECT_NAME = '" + projectName + "' group by month(session_date)) as tbl3"
						+ " where tbl1.eventmonth = tbl2.overviewmonth and tbl2.overviewmonth = tbl3.sessionMonth) as b"
						+ " on a.monthNumber=b.overviewmonth"
						+ " order by a.monthNumber;";
			}
			else
			{
//			sqlQuery = "SELECT OVERVIEW_DATE, TOTAL_USERS, TOTAL_SESSIONS, BOUNCE_RATE"
//					+ " FROM MOD_IA_DAILY_OVERVIEW WHERE "
//					+ timefilter + " AND PROJECT_NAME = '" + projectName + "';";
				
				sqlQuery = "select tbl2.OVERVIEW_DATE as overviewDate, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ "	from (SELECT count( distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as People, date(a.event_timestamp) as overviewDate"
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b "
						+ "	where a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.status_code = 0 and  " + auditTimeFilter
						+ " and actor != 'SYSTEM' AND b.PROJECT_NAME = '" + projectName + "' group by overviewDate ) as tbl1, "
						+ " ( select avg(bounce_rate)  * 100 as bounceRate, OVERVIEW_DATE ,"
						+ " sum(TOTAL_SESSIONS ) as TOTAL_SESSIONS"
						+ " from mod_ia_daily_overview where " + timefilter + " AND PROJECT_NAME = '" + projectName + "' group by OVERVIEW_DATE ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, session_date as sessionDate"
						+ " from mod_ia_daily_sessions where "
						+ sessionTimefilter 
						+ " AND PROJECT_NAME = '" + projectName + "' group by SESSION_DATE) as tbl3"
						+ " where tbl1.overviewDate = tbl2.OVERVIEW_DATE and tbl2.OVERVIEW_DATE = tbl3.sessionDate"
						+ " order by overviewDate;";
			}
		}
		
		//connect to the database and get records
		try{
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
//			log.error("getBounceRateReportByDate : " + sqlQuery);
			overview = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("getBounceRateReportByDate : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getBounceRateReportByDate : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return overview;
	}
	/**
	 * Method to retrieve Device type information 
	 * Returns a Dataset with No Of People and No of Actions per device type
	 */
	@Override
	public Dataset getDeviceTypeReport( int duration, String projectName,
			boolean allProjects) {
		
		Dataset deviceData = null;
		Datasource ds;
		String sqlQuery = "";
		
		SRConnection con = null;
		String timefilter = getDateFilter(duration, "b.START_TIMESTAMP");
		
		if(allProjects)
		{
//			sqlQuery = "select case when b.IS_MOBILE = 0 then 'Desktop' "
//					+ " when b.IS_MOBILE = 1 then 'Mobile'"
//					+ " end as 'device Type', count(distinct (concat(b.username, c.AUTH_PROFILE))) as people, "
//					+ "	count(session_start) as visits, sum(a.no_of_actions)  as actions, sum(a.no_of_screens) as noOfScreens"
//					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b , mod_ia_projects c where b.PROJECT = c.PROJECT_NAME and "
//					+ timefilter 
//					+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end"
//					+ " and a.PROJECT_NAME = b.PROJECT"
//					+ " group by b.IS_MOBILE";
			
			sqlQuery = "select dt.deviceType as deviceType, count(distinct (concat(dt.username, c.AUTH_PROFILE))) as people,"
					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
					+ " from ("
					+ " select distinct case when b.IS_MOBILE = 0 then 'Desktop'  when b.IS_MOBILE = 1 then 'Mobile' end as 'deviceType', "
					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b"
					+ " where " + timefilter + " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
					+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end and a.username = b.username "
//					+ " union"
//					+ "	select case when b.IS_MOBILE = 0 then 'Desktop'  when b.IS_MOBILE = 1 then 'Mobile' end as 'device Type',"
//					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
//					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b "
//					+ " where " + timefilter + " and b.START_TIMESTAMP >= a.session_start "
//					+ " and a.PROJECT_NAME = b.PROJECT and a.session_start = a.session_end  ) as dt, "
					+ " ) as dt,"
					+ " mod_ia_projects c"
					+ " where dt.PROJECT = c.PROJECT_NAME group by deviceType ;";
		}
		else
		{
//			sqlQuery = "select case when b.IS_MOBILE = 0 then 'Desktop' "
//					+ " when b.IS_MOBILE = 1 then 'Mobile'"
//					+ " end as 'device Type', count(distinct (concat(b.username, c.AUTH_PROFILE))) as people, "
//					+ "	count(session_start) as visits, sum(a.no_of_actions)  as actions, sum(a.no_of_screens) as noOfScreens"
//					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b, mod_ia_projects c where b.PROJECT = c.PROJECT_NAME and "
//					+ timefilter + " and b.Project = '" + projectName + "'"
//					+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end"
//					+ " and a.PROJECT_NAME = b.PROJECT"
//					+ " group by b.IS_MOBILE";
		
			sqlQuery = "select dt.deviceType as deviceType, count(distinct (concat(dt.username, c.AUTH_PROFILE))) as people,"
					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
					+ " from ("
					+ " select distinct case when b.IS_MOBILE = 0 then 'Desktop'  when b.IS_MOBILE = 1 then 'Mobile' end as 'deviceType', "
					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b"
					+ " where " + timefilter +  " and b.Project = '" + projectName + "' and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
					+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end and a.username = b.username"
//					+ " union"
//					+ "	select case when b.IS_MOBILE = 0 then 'Desktop'  when b.IS_MOBILE = 1 then 'Mobile' end as 'device Type',"
//					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
//					+ " FROM mod_ia_daily_sessions a, mod_ia_clients b "
//					+ " where " + timefilter + " and b.Project = '" + projectName + "'" + " and b.START_TIMESTAMP >= a.session_start "
//					+ " and a.PROJECT_NAME = b.PROJECT and a.session_start = a.session_end  ) as dt, "
					+ " ) as dt, "
					+ " mod_ia_projects c"
					+ " where dt.PROJECT = c.PROJECT_NAME group by deviceType ;";
		}
		
		//connect to the database and get records
		try{
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();
//			log.error("Device type report q : " + sqlQuery);
			
			deviceData = con.runQuery(sqlQuery);
		}
		catch(Exception e){
			log.error("getDeviceTypeReport : " + e);
		}finally{
			//close the database connection 
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getDeviceTypeReport : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		return deviceData;
	}
	
	//By Omkar
	
	public Dataset getActiveUserDataReportGraph(String datasource, int duration, String projectName, boolean allProjects){
		Dataset activeUsersData = null ;
		
		Datasource dsname;
		String sqlQuery = "";
		
		SRConnection con = null;
		String timefilter = getDateFilter(duration, "SUMMARY_DATE");
		
		if(allProjects)
		{
			
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				sqlQuery = "select a.Hour as Hour, COALESCE(ONE_DAY_ACTIVE_USERS, 0), "
						+ " COALESCE(SEVEN_DAY_ACTIVE_USERS, 0),"
						+ " COALESCE(FOURTEEN_DAY_ACTIVE_USERS, 0) " 
						+ " from mod_ia_hours as a "
						+ " left join (SELECT SUMMARY_HOUR,   ONE_DAY_ACTIVE_USERS, "
						+ " SEVEN_DAY_ACTIVE_USERS, "
						+ " FOURTEEN_DAY_ACTIVE_USERS "
						+ " FROM mod_ia_hourly_active_users where PROJECT_NAME = 'All' and "+ timefilter +" group by SUMMARY_HOUR  ) as b "
						+ " on b.summary_hour = a.Hour "
						+ " order by a.Hour;";
			}
			else if(duration == Constants.LAST_365_DAYS ) 
			{
				//query from daily active users and group by month
				
				String sDate = "";
				Calendar lastHour = Calendar.getInstance();
			    lastHour.set(Calendar.HOUR_OF_DAY, 23);
			    lastHour.set(Calendar.MINUTE, 59);
			    lastHour.set(Calendar.SECOND, 59);
			    
			    int currentYear = lastHour.get(Calendar.YEAR);
				lastHour.add(Calendar.DATE, -365);
				int yearVal = lastHour.get(Calendar.YEAR);
				int monthVal = lastHour.get(Calendar.MONTH);
				sqlQuery = "select a.monthName, sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users), a.monthNumber "
						+ " from mod_ia_month as a "
						+ " left join (select one_day_active_users, "
						+ " seven_day_active_users, "
						+ " fourteen_day_active_users, "
						+ " month_no as summary_month , year from mod_ia_monthly_active_users where "
						+ " ( year = " + yearVal + " and month_no >= " + monthVal + ") or ( year = " + currentYear + ") and "
						+ " PROJECT_NAME = 'All' ) as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by  a.monthNumber, b.year desc;";
				
//				
//				sqlQuery = "select a.monthName, b.ONE_DAY_ACTIVE_USERS, b.SEVEN_DAY_ACTIVE_USERS,"
//						+ " b.FOURTEEN_DAY_ACTIVE_USERS, a.monthNumber "
//						+ " from mod_ia_month as a"
//						+ " left join (select  month(SUMMARY_DATE) as overviewmonth ,ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS "
//						+ " FROM mod_ia_daily_active_users where PROJECT_NAME = 'All' and " + timefilter + " group by overviewmonth ) as b"
//						+ "	on b.overviewmonth = a.monthNumber order by a.monthNumber;";
			}
			else if(duration == Constants.LAST_YEAR )
			{
				//query from monthly active users for last year
				sqlQuery = "select a.monthName, b.ONE_DAY_ACTIVE_USERS, b.SEVEN_DAY_ACTIVE_USERS, b.FOURTEEN_DAY_ACTIVE_USERS"
						+ "	from mod_ia_month as a"
						+ "	left join (select  month_no ,ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS"
						+ "	FROM mod_ia_monthly_active_users where PROJECT_NAME = 'All' and year = year(now()) - 1 ) as b"
						+ " on b.month_no = a.monthNumber order by a.monthNumber ;";
			}
			else if(duration == Constants.THIS_YEAR)
			{
				//query from monthly active users for current year	
				sqlQuery = "select a.monthName, b.ONE_DAY_ACTIVE_USERS, b.SEVEN_DAY_ACTIVE_USERS, b.FOURTEEN_DAY_ACTIVE_USERS"
						+ "	from mod_ia_month as a"
						+ "	left join (select  month_no ,ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS"
						+ "	FROM mod_ia_monthly_active_users where PROJECT_NAME = 'All' and year = year(now()) ) as b"
						+ " on b.month_no = a.monthNumber order by a.monthNumber ;";
			}
			else
			{
				sqlQuery = "SELECT SUMMARY_DATE, ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS "
							+ " FROM mod_ia_daily_active_users where PROJECT_NAME = 'All' and "+ timefilter +" order by SUMMARY_DATE ;";	
			}
		}
		else 
		{
			
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
					sqlQuery = "select a.Hour as Hour, COALESCE(ONE_DAY_ACTIVE_USERS, 0), "
								+ " COALESCE(SEVEN_DAY_ACTIVE_USERS, 0),"
								+ " COALESCE(FOURTEEN_DAY_ACTIVE_USERS, 0) " 
								+ " from mod_ia_hours as a "
								+ " left join (SELECT SUMMARY_HOUR, ONE_DAY_ACTIVE_USERS, "
								+ " SEVEN_DAY_ACTIVE_USERS, "
								+ " FOURTEEN_DAY_ACTIVE_USERS "
								+ " FROM mod_ia_hourly_active_users where "+ timefilter
								+ " and PROJECT_NAME = '"+ projectName +"'"
								+" order by SUMMARY_HOUR  ) as b "
								+ " on b.summary_hour = a.Hour "
								+ " order by a.Hour ;";
			}
			else if(duration == Constants.LAST_365_DAYS ) 
			{
				
				String sDate = "";
				Calendar lastHour = Calendar.getInstance();
			    lastHour.set(Calendar.HOUR_OF_DAY, 23);
			    lastHour.set(Calendar.MINUTE, 59);
			    lastHour.set(Calendar.SECOND, 59);
			    
			    int currentYear = lastHour.get(Calendar.YEAR);
				lastHour.add(Calendar.DATE, -365);
				int yearVal = lastHour.get(Calendar.YEAR);
				int monthVal = lastHour.get(Calendar.MONTH);
				sqlQuery = "select a.monthName, sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users), a.monthNumber "
						+ " from mod_ia_month as a "
						+ " left join (select one_day_active_users, "
						+ " seven_day_active_users, "
						+ " fourteen_day_active_users, "
						+ " month_no as summary_month , year from mod_ia_monthly_active_users where "
						+ " ( year = " + yearVal + " and month_no >= " + monthVal + ") or ( year = " + currentYear + ") and "
						+ " PROJECT_NAME = '" + projectName + "' ) as b "
						+ " on b.summary_month = a.monthNumber"
						+ " group by a.monthName order by  a.monthNumber, b.year desc;";
				
//				sqlQuery = "select a.monthName, b.ONE_DAY_ACTIVE_USERS, b.SEVEN_DAY_ACTIVE_USERS,"
//						+ " b.FOURTEEN_DAY_ACTIVE_USERS"
//						+ " from mod_ia_month as a"
//						+ " left join (select  month(SUMMARY_DATE) as overviewmonth ,ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS "
//						+ " FROM mod_ia_daily_active_users where PROJECT_NAME = '" + projectName + "' and " + timefilter + " group by overviewmonth ) as b"
//						+ "	on b.overviewmonth = a.monthNumber order by a.monthNumber ;";
				
			}
			else if(duration == Constants.LAST_YEAR )
			{
				sqlQuery = "select a.monthName, b.ONE_DAY_ACTIVE_USERS, b.SEVEN_DAY_ACTIVE_USERS, b.FOURTEEN_DAY_ACTIVE_USERS"
						+ "	from mod_ia_month as a"
						+ "	left join (select  month_no ,ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS"
						+ "	FROM mod_ia_monthly_active_users where PROJECT_NAME = '" + projectName + "' and year = year(now()) - 1 ) as b"
						+ " on b.month_no = a.monthNumber order by a.monthNumber ;";
			}
			else if(duration == Constants.THIS_YEAR)
			{
				sqlQuery = "select a.monthName, b.ONE_DAY_ACTIVE_USERS, b.SEVEN_DAY_ACTIVE_USERS, b.FOURTEEN_DAY_ACTIVE_USERS"
						+ "	from mod_ia_month as a"
						+ "	left join (select  month_no ,ONE_DAY_ACTIVE_USERS, SEVEN_DAY_ACTIVE_USERS, FOURTEEN_DAY_ACTIVE_USERS"
						+ "	FROM mod_ia_monthly_active_users where PROJECT_NAME = '" + projectName + "' and year = year(now()) ) as b"
						+ " on b.month_no = a.monthNumber order by a.monthNumber ;";
			}
			else 
			{
				sqlQuery = "SELECT SUMMARY_DATE, ONE_DAY_ACTIVE_USERS,SEVEN_DAY_ACTIVE_USERS,FOURTEEN_DAY_ACTIVE_USERS"
							+ " FROM mod_ia_daily_active_users where "+ timefilter +" and PROJECT_NAME = '"+ projectName +"' order by SUMMARY_DATE ;";
			}
			
		}
		
//		log.error("reports getActiveUserDataReportGraph: "+sqlQuery);
		try {
			
			dsname = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = dsname.getConnection();
			activeUsersData = con.runQuery(sqlQuery);
			
		} catch (Exception e) {
			// TODO: handle exception
			log.error("getActiveUserDataReportGraph :"+e);
		}
		finally{
			if(con != null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error(" getActiveUserDataReportGraph: in con close exception.");
					// TODO: handle exception
					log.error("getActiveUserDataReportGraph :"+e);
					e.printStackTrace();
				}
			}
		}
		
			return activeUsersData;
		
		}
		
		public Dataset getSevenDaysMaxMin(String datasource, int duration, String projectName, boolean allProjects){
			Dataset maxminData = null;
			String sqlQuery = "null";
			String dateFilter = getDateFilter(Constants.LAST_SEVEN_DAYS, "a.EVENT_TIMESTAMP");
			
			SRConnection con = null;
			Datasource ds;
			//modified the sql query to return results for all dates, to handle missing data case.
			
			if(allProjects){
				
				
				
//				sqlQuery = "Select max(total_users) as 7_day_max, min(total_users) as 7_day_min from "
//						+ " (select count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users"
//						+ " from AUDIT_EVENTS a,  mod_ia_projects b"
//						+ " where  a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 "
//						+ " and "+ dateFilter +""
//						+ " group by date(event_timestamp))as a;";
				
				sqlQuery = "select date(event_timestamp), count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users"
						+ " from AUDIT_EVENTS a,  mod_ia_projects b"
						+ " where  a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and a.actor != 'SYSTEM' and"
						+ " a.action='login' and a.status_code = 0 "
						+ " and "+ dateFilter +""
						+ " group by date(event_timestamp);";

			}else
			{
//				sqlQuery =  "Select max(total_users) as 7_day_max,min(total_users) as 7_day_min from "
//						+ " (select count(distinct(actor)) as total_users"
//						+ " from AUDIT_EVENTS "
//						+ " where "+ dateFilter 
//						+ " and status_code = 0 and ORIGINATING_SYSTEM = concat('project=',"+ projectName +") "
//						+ " group by date(event_timestamp) )as a;";
				
				sqlQuery = "select date(event_timestamp), count(distinct(CONCAT(a.actor,b.AUTH_PROFILE))) as total_users"
						+ " from AUDIT_EVENTS a,  mod_ia_projects b"
						+ " where  a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and"
						+ " a.ORIGINATING_SYSTEM = concat('project=','"+ projectName +"') and"
						+ " a.actor != 'SYSTEM' and a.action='login' and a.status_code = 0 "
						+ " and "+ dateFilter +" group by date(event_timestamp);";
			}
			
			
			try {
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				maxminData = con.runQuery(sqlQuery);
				
			} catch (Exception e) {
				// TODO: handle exception
				log.error("getSevenDaysMaxMin :"+e);
			}
			finally{
				if(con != null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getSevenDaysMaxMin : in con close exception.");
						// TODO: handle exception
						log.error("getSevenDaysMaxMin :"+e);
						e.printStackTrace();
					}
				}
			}
			
			
			return maxminData;
		}

		/**
		 * Function to return a list of projects present on Gateway but not added to Ignition Analytics module
		 * @author : YM Created on Sept-12-2015.
		 * @return list of Project names
		 */
		@Override
		public String[] getProjectNotAddedRoIgnitionAnalytics() {
			
			Datasource ds;
			String[] returnarray = null;
			List<String> returnProjects=  new ArrayList<String>();
			//String[] returnProjects = null;
			int noOfProjects = 0, noOfGatewayProjects = 0,i=0, noOfNewProjectsToAdd = 0;
			Dataset resDS = null;
			List<String> _gatewayProjects;
			List<String> _iaProjects = new ArrayList<String>();
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String sqlSelectQuery = "SELECT PROJECT_NAME FROM MOD_IA_PROJECTS;";
			
			
			try {
				
				//get teh list of projects from Gateway
				_gatewayProjects = this.mycontext.getProjectManager().getProjectNames();
				
				//get the list of projects added to Ignition ANalytics module.
				con = ds.getConnection();
				resDS = con.runQuery(sqlSelectQuery);
				
				if(resDS != null && resDS.getRowCount() > 0)
				{
					noOfProjects = resDS.getRowCount();
					
					for(i=0; i<noOfProjects; i++)
					{
						if(resDS.getValueAt(i,0) != null)
						{
							_iaProjects.add( resDS.getValueAt(i,0).toString());
						}
					}
					
					
				}
				noOfNewProjectsToAdd = _gatewayProjects.size() - _iaProjects.size();
				if(noOfNewProjectsToAdd > 0)
				{
					returnProjects = new ArrayList<String>();
					noOfGatewayProjects = _gatewayProjects.size();
					for(i=0; i<noOfGatewayProjects; i++)
					{
						if(!(_iaProjects.contains(_gatewayProjects.get(i))))
						{
							//log.info(_gatewayProjects.get(i));
							returnProjects.add( _gatewayProjects.get(i));
							
						}
					}
				}
				
				int cnt = returnProjects.size();
				returnarray = new String[cnt];
				for(int r=0; r<cnt; r++)
				{
					returnarray[r] = returnProjects.get(r);	
				}
	
			}
			catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getProjectNotAddedRoIgnitionAnalytics : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			return returnarray;	
		
		}

		/**
		 * A function insert one or more rows in mod_ia_projects.
		 * this is called from Add New Project UI where user selects one or more projects to be added.
		 * @author YM : Created on 09/14/2015.
		 * @param projectNames - list of project names to be added.
		 */
		@Override
		public void addProjectsToModule(List<String> projectNames) {

			
			
			
			
			Datasource ds;
			int noOfProjects = 0;
			SRConnection con = null;
			String insertProjectStmt = "INSERT INTO mod_ia_projects (PROJECT_NAME, AUTH_PROFILE) VALUES ('";
			UserSourceManager _um;
			try {
					ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					_um = mycontext.getUserSourceManager();
					long projId;
					con = ds.getConnection();
					List<Projects_Sync> _projectsList = new ArrayList<Projects_Sync>();
					Projects_Sync _p ;
					if(projectNames != null)
					{
						noOfProjects = projectNames.size();
						try {
						for(int n=0; n<noOfProjects; n++)
						{
							//projId = mycontext.getProjectManager().getProjectId(projectNames.get(n));
							//projId = mycontext.getProjectManager().get
							if(n == noOfProjects - 1)
							{
								//get the projec auth profile using project manager getProps API
								
									insertProjectStmt = insertProjectStmt + projectNames.get(n) 
											+ "' , '" + mycontext.getProjectManager().getProjectProps(projectNames.get(n)).getAuthProfileName();
								} 
							
							else
							{
								insertProjectStmt = insertProjectStmt + projectNames.get(n) + 
										 "' , '" + mycontext.getProjectManager().getProjectProps(projectNames.get(n)).getAuthProfileName()+ "') , ('";
							}
							_p = new Projects_Sync();
							_p.authProfile = mycontext.getProjectManager().getProjectProps(projectNames.get(n)).getAuthProfileName();
							_p.projectName = projectNames.get(n) ;
							_projectsList.add(_p);
						}}
						catch (ProjectNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						insertProjectStmt = insertProjectStmt + "');";
						con.runUpdateQuery(insertProjectStmt);
						
						//if add project is initiated form controller then we dont need to call service to send data again
						
						//call service on controller to sync projects data
						GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
					
						if(this.isAgent)
						{
							ServiceManager sm = gm.getServiceManager();
							ServerId sid = new ServerId(this.controllerName);
							ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
				
							//if service is available
							if(s == ServiceState.Available)
							{
								//call the service 
								sm.getService(sid, GetAnalyticsInformationService.class).get().receiveProjects(Constants.PROJECTS_INSERT, gm.getServerAddress().getServerName(), _projectsList);
							}
						}
						else
						{
							this.receiveProjects(Constants.PROJECTS_INSERT, gm.getServerAddress().getServerName(), _projectsList);
						}
						
					}
		
				}
				catch (SQLException e) {
						e.printStackTrace();
					}
				finally{
					if(con!=null){
						try {
								con.close();
						} catch (SQLException e) {
							log.error("addProjectsToModule : in con close exception.");
			
								e.printStackTrace();
							}
		
						}
				}
		} //end of function

		/**
		 * Following functions are used to retrieve reports specific dataset for given project names and gven duration
		 */
		public Dataset getEngagementReportInformationScreenDepth( String projectName,
				boolean allProjects, int duration){
			
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "session_start");
			String sqlQuery = "";
			
			if(allProjects)
			{
				sqlQuery =  "SELECT  a.no_of_screens as no_of_screens, count(a.session_date) as Sessions, count(distinct (concat(a.username, b.AUTH_PROFILE))) as People, "
						+ " sum(a.no_of_actions) + sum(a.no_of_screens) as Actions"
						+ " from mod_ia_daily_sessions a, mod_ia_projects b where "
						+ dateFilter
						+ " and a.PROJECT_NAME = b.PROJECT_NAME group by no_of_screens;";
				
			}
			else
			{
				sqlQuery =  "SELECT a.no_of_screens as no_of_screens, count(a.session_date) as Sessions, count(distinct (concat(a.username, b.AUTH_PROFILE))) as People,"
						+ " sum(a.no_of_actions) + sum(a.no_of_screens) as Actions"
						+ " from mod_ia_daily_sessions a, mod_ia_projects b where "
						+ dateFilter 
						+ " and a.PROJECT_NAME = b.PROJECT_NAME and a.PROJECT_NAME = '"+projectName+"'"
						+ " group by no_of_screens;";
				
			}
			try {
				con = ds.getConnection();
				
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error(" getEngagementReportInformationScreenDepth : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			return freqData;

		}
		
		
		
		public Dataset getFrequencytReportInformation( String projectName,
				boolean allProjects, int duration){
			
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "session_start");
			String sqlQuery = "";
			
			if(allProjects)
			{
				
				sqlQuery =  "select concat(a.USERNAME , b.AUTH_PROFILE ) as username , count(a.session_start) as Visits,"
						 +	" sum(a.no_of_actions) as Actions, sum(a.no_of_screens) as noOfScreens "
						 + "	from  mod_ia_daily_sessions a, mod_ia_projects b where a.PROJECT_NAME = b.PROJECT_NAME"
						 + "	and "
						 +    dateFilter
						 +	" group by username;";
				
			}
			else
			{
				sqlQuery =  "select concat(a.USERNAME , b.AUTH_PROFILE ) as username , count(a.session_start) as Visits,"
						 +	" sum(a.no_of_actions) as Actions, sum(a.no_of_screens) as noOfScreens "
						 + " from  mod_ia_daily_sessions a, mod_ia_projects b where "
						 + " a.PROJECT_NAME = '"+projectName+ "' and a.PROJECT_NAME = b.PROJECT_NAME"
						 + "	and "
						 +    dateFilter
						 
						 +	" group by username;";
				
				
				
			}
			try {
				
				con = ds.getConnection();
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getFrequencytReportInformation : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			
			
		
			
			return freqData;

		}
		
		
		public Dataset getRecencytReportInformation( String projectName,
				boolean allProjects, int duration){
			
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "session_start");
			String sqlQuery = "";
			String startDate = getDayAndTime(duration);
			String durationEndDate = "";
			
			if(allProjects)
			{
				
				
				if(duration == Constants.YESTERDAY || duration == Constants.LAST_WEEK || duration == Constants.LAST_MONTH || duration == Constants.LAST_YEAR)
				{
					
					
					sqlQuery =  "select concat(a.USERNAME , b.AUTH_PROFILE ) as username, DATEDIFF(date('" + startDate + "') ,max(session_date)) as Days, count(session_start) as Visits, "
							+		"sum(no_of_actions) as Actions, sum(no_of_screens) as noOfScreens from mod_ia_daily_sessions a, mod_ia_projects b "
							+ " where session_start < '"
							+ durationEndDate + "' and session_start >= '"+this.installDate+"' and a.PROJECT_NAME = b.PROJECT_NAME group by username;";
				}
				else
				{
					sqlQuery =  "select concat(a.USERNAME , b.AUTH_PROFILE ) as username, DATEDIFF(date('" + startDate + "') ,max(session_date)) as Days , count(session_start) Visits, "
							+		"sum(no_of_actions) as Actions, sum(no_of_screens) as noOfScreens from mod_ia_daily_sessions a, mod_ia_projects b "
							+ " where session_start >= '"+this.installDate+"' and a.PROJECT_NAME = b.PROJECT_NAME group by username;";
							 
				}

			}
			else
			{
				if(duration == Constants.YESTERDAY || duration == Constants.LAST_WEEK || duration == Constants.LAST_MONTH || duration == Constants.LAST_YEAR)
				{
					durationEndDate = getDurationEndDate(duration);
					sqlQuery =  "select username, DATEDIFF(date('" + startDate + "') ,max(session_date)) as Days , count(session_start) Visits, "
							+		"sum(no_of_actions) as  Actions, sum(no_of_screens) as noOfScreens from mod_ia_daily_sessions where session_start < '"
							 +    durationEndDate + "' and PROJECT_NAME = '"+projectName+"'"
							 +	" group by username;";
				}
				else
				{
					
					sqlQuery =  "select username, DATEDIFF(date('" + startDate + "') ,max(session_date)) as Days , count(session_start) Visits, "
							+		"sum(no_of_actions) as  Actions, sum(no_of_screens) as noOfScreens from mod_ia_daily_sessions where"
							 + " PROJECT_NAME = '"+projectName+"'"
							 +	" group by username;";
				}
			}
			
			
			
			try {
				
				con = ds.getConnection();
				log.error("Recency report sql q : " + sqlQuery);
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getRecencytReportInformation : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			
			
		
			
			return freqData;

		}

	
		public Dataset getVisitDurationReportInformation( String projectName,
				boolean allProjects, int duration){
			
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "session_start");
			String sqlQuery = "";
			
			if(allProjects)
			{
			
			
				sqlQuery =  "select count(distinct username) as People , count(session_start) as Visits,"
						+ "	sum(no_of_actions) as Actions, session_duration_mins, sum(no_of_screens) as noOfScreens from "
						+ " ( select concat(x.USERNAME , y.AUTH_PROFILE ) as username , session_start,"
						+ "	no_of_actions, "
			+ " case when (TIME_TO_SEC(session_duration))/60 <= 5 then '0-5'"
			+ "	 when (TIME_TO_SEC(session_duration))/60 > 5 and (TIME_TO_SEC(session_duration))/60 <= 10 then '6-10'"
			+ " when (TIME_TO_SEC(session_duration))/60 > 10 and (TIME_TO_SEC(session_duration))/60 <= 20 then '11-20'"
			+ " when (TIME_TO_SEC(session_duration))/60 > 20 and (TIME_TO_SEC(session_duration))/60 <= 30 then '21-30' "
			+ " when (TIME_TO_SEC(session_duration))/60 > 30 and (TIME_TO_SEC(session_duration))/60 <= 40 then '31-40'"
			+ " when (TIME_TO_SEC(session_duration))/60 > 40 and (TIME_TO_SEC(session_duration))/60 <= 50 then '41-50'"
			+ " when (TIME_TO_SEC(session_duration))/60 > 50 and (TIME_TO_SEC(session_duration))/60 <= 60 then '51-60'"
			+ " when (TIME_TO_SEC(session_duration))/60 > 60 and (TIME_TO_SEC(session_duration))/60 <= 120 then '61-120'"
			+ " when (TIME_TO_SEC(session_duration))/60 > 120  then '120'"
			+ " end as session_duration_mins, no_of_screens"
			+ " from mod_ia_daily_sessions x, mod_ia_projects y  where " + dateFilter
			+ " and x.project_name = y.PROJECT_NAME) as a group by session_duration_mins; ";	
			}
			else
			{
				sqlQuery =  "select count(distinct username) as People , count(session_start) as Visits,"
						+"	sum(no_of_actions) as Actions, "
						+ " case when (time_to_sec(session_duration))/60 <= 5 then '0-5'"
						+ "	 when (time_to_sec(session_duration))/60 > 5 and (time_to_sec(session_duration))/60 <= 10 then '6-10'"
						+" when (time_to_sec(session_duration))/60 > 10 and (time_to_sec(session_duration))/60 <= 20 then '11-20'"
						+ " when (time_to_sec(session_duration))/60 > 20 and (time_to_sec(session_duration))/60 <= 30 then '21-30' "
						+ " when (time_to_sec(session_duration))/60 > 30 and (time_to_sec(session_duration))/60 <= 40 then '31-40'"
						+ " when (time_to_sec(session_duration))/60 > 40 and (time_to_sec(session_duration))/60 <= 50 then '41-50'"
						+ " when (time_to_sec(session_duration))/60 > 50 and (time_to_sec(session_duration))/60 <= 60 then '51-60'"
						+ " when (time_to_sec(session_duration))/60 > 60 and (time_to_sec(session_duration))/60 <= 120 then '61-120'"
						+ " when (time_to_sec(session_duration))/60 > 120  then '120'"
						+ " end as session_duration_mins, sum(no_of_screens) as noOfScreens"
						+ " from mod_ia_daily_sessions a, mod_ia_projects b where "
						+ " a.PROJECT_NAME = b.PROJECT_NAME and a.PROJECT_NAME ='"+projectName+"' and "  + dateFilter
						+ " group by session_duration_mins; ";
						
			}
			try {
				
				con = ds.getConnection();
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getVisitDurationReportInformation : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			
			
		
			
			return freqData;

		}
		
		
		public Dataset getActionsPerVisitReportInformation( String projectName,
				boolean allProjects, int duration){
			
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "session_start");
			String sqlQuery = "";
			
			if(allProjects)
			{
				sqlQuery = "select count(distinct concat(a.username, b.auth_profile)) as People, count(session_start) as Visits,"
						 + " sum(no_of_actions + NO_OF_SCREENS) as Actions, "
						 + "	case when (NO_OF_ACTIONS + NO_OF_SCREENS) >= 1 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 5 then '1-5' "
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 5 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 10 then '6-10' "
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 10 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 20 then '11-20' "
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 20 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 30 then '21-30'"
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 30 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 50 then '31-50'"
						
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 50  then '51 or more'"
						 + " end as  count_of_actions" 
						 + " from mod_ia_daily_sessions a, mod_ia_projects b"
						 + " where a.PROJECT_NAME = b.PROJECT_NAME and " + dateFilter
						 + " group by count_of_actions; ";
				
			}
			else
			{
				sqlQuery =  "select count(distinct username) as People , count(session_start) as Visits,"
						 + " sum(no_of_actions + NO_OF_SCREENS) as Actions, "
						 + "	case when (NO_OF_ACTIONS + NO_OF_SCREENS) >= 1 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 5 then '1-5' "
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 5 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 10 then '6-10' "
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 10 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 20 then '11-20' "
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 20 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 30 then '21-30'"
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 30 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 50 then '31-50'"
						
						 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 50  then '51 or more'"
						 + " end as  count_of_actions" 
						 + " from mod_ia_daily_sessions"
						 + " where PROJECT_NAME = '"+projectName+"' and " + dateFilter
						 + " group by count_of_actions; ";
						
			}
			try {
				
				con = ds.getConnection();
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getActionsPerVisitReportInformation : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			
			
			return freqData;

		}

		/**
		 * Function to retrieve Browser reports data 
		 * @author YM : Created on 09/28/2015
		 * 
		 */
		@Override
		public Dataset getBrowserReport(int duration, String projectName,
				boolean allProjects) {
			Dataset browserData = null;
			Datasource ds;
			String sqlQuery = "";
			
			SRConnection con = null;
			String timefilter = getDateFilter(duration, "c.START_TIMESTAMP");
			
			if(allProjects)
			{

				sqlQuery = "select  dt.browser_name as browser_name, sum(dt.no_of_actions) as Actions, count(dt.session_start) as Visits, "
						+ " count(distinct (concat(dt.username, c.AUTH_PROFILE))) as People, sum(dt.no_of_screens) as noOfScreens from "
						+ "( select distinct b.browser_name as browser_name, a.no_of_actions as no_of_actions, a.session_start as session_start,"
						+ " b.username as username, a.no_of_screens as no_of_screens, a.PROJECT_NAME as PROJECT_NAME "
						+ " from mod_ia_daily_sessions a "
						+ " right join "
						+ " ( select start_timestamp, IP_ADDRESS, username , concat(trim(browser_name) , ' ' ,browser_version) as browser_name, "
						+ " c.Project as Project,  c.CLIENT_CONTEXT as CLIENT_CONTEXT, c.HOSTNAME as HOSTNAME "
						+ " from mod_ia_clients c, mod_ia_browser_info  d "
						+ " where (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ) and  d.IP_ADDRESS is not null and "
						+ timefilter + " and d.timestamp <= c.start_timestamp and d.timestamp = "
						+ " (select max(timestamp) from mod_ia_browser_info where timestamp <= c.start_timestamp"
						+ " and (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ))) as b"
						+ " on ( a.SESSION_CONTEXT = b.CLIENT_CONTEXT and a.HOSTNAME = b.HOSTNAME and "
						+ " a.session_start != a.session_end and b.start_timestamp >= a.session_start "
						+ "and b.start_timestamp <= a.session_end and a.username = b.username"
						+ " and a.PROJECT_NAME = b.PROJECT)"
//						+ " union "
//						+ " select b.browser_name as browser_name, a.no_of_actions as no_of_actions, a.session_start as session_start,"
//						+ " b.username as username, a.no_of_screens as no_of_screens, a.PROJECT_NAME as PROJECT_NAME "
//						+ " from mod_ia_daily_sessions a right join"
//						+ "( select start_timestamp, IP_ADDRESS, username , concat(trim(browser_name) , ' ' ,browser_version)"
//						+ " as browser_name from mod_ia_clients c, mod_ia_browser_info  d"
//						+ " where (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ) and  d.IP_ADDRESS is not null and "
//						+ timefilter + " and d.timestamp <= c.start_timestamp and d.timestamp = "
//						+ "(select max(timestamp) from mod_ia_browser_info where timestamp <= c.start_timestamp)) as b "
//						+ "on (b.start_timestamp >= a.session_start and a.session_start = a.session_end) "
						+ " )as dt , "
						+ " mod_ia_projects c where dt.PROJECT_NAME = c.PROJECT_NAME group by dt.browser_name;";
			}
			else
			{
				

				sqlQuery = "select  dt.browser_name as browser_name, sum(dt.no_of_actions) as Actions, count(dt.session_start) as Visits, "
						+ " count(distinct (concat(dt.username, c.AUTH_PROFILE))) as People, sum(dt.no_of_screens) as noOfScreens from "
						+ "( select distinct b.browser_name as browser_name, a.no_of_actions as no_of_actions, a.session_start as session_start,"
						+ " b.username as username, a.no_of_screens as no_of_screens, a.PROJECT_NAME as PROJECT_NAME "
						+ " from mod_ia_daily_sessions a "
						+ " right join "
						+ " ( select start_timestamp, IP_ADDRESS, username , concat(trim(browser_name) , ' ' ,browser_version) as browser_name, "
						+ " c.Project as Project,  c.CLIENT_CONTEXT as CLIENT_CONTEXT, c.HOSTNAME as HOSTNAME "
						+ " from mod_ia_clients c, mod_ia_browser_info  d "
						+ " where (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ) and  d.IP_ADDRESS is not null and "
						+ timefilter + " and c.Project = '" + projectName + "' and d.timestamp <= c.start_timestamp and d.timestamp = "
						+ " (select max(timestamp) from mod_ia_browser_info where timestamp <= c.start_timestamp"
						+ " and (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ))) as b"
						+ " on ( a.SESSION_CONTEXT = b.CLIENT_CONTEXT and a.HOSTNAME = b.HOSTNAME and "
						+ " a.session_start != a.session_end and b.start_timestamp >= a.session_start "
						+ "and b.start_timestamp <= a.session_end and a.username = b.username"
						+ " and a.PROJECT_NAME = b.PROJECT and a.PROJECT_NAME = '" + projectName + "')"
//						+ " union "
//						+ " select b.browser_name as browser_name, a.no_of_actions as no_of_actions, a.session_start as session_start,"
//						+ " b.username as username, a.no_of_screens as no_of_screens, a.PROJECT_NAME as PROJECT_NAME "
//						+ " from mod_ia_daily_sessions a right join"
//						+ "( select start_timestamp, IP_ADDRESS, username , concat(trim(browser_name) , ' ' ,browser_version)"
//						+ " as browser_name from mod_ia_clients c, mod_ia_browser_info  d"
//						+ " where (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ) and  d.IP_ADDRESS is not null and "
//						+ timefilter + "and c.Project = '" + projectName + "' and d.timestamp <= c.start_timestamp and d.timestamp = "
//						+ "(select max(timestamp) from mod_ia_browser_info where timestamp <= c.start_timestamp)) as b "
//						+ "on (b.start_timestamp >= a.session_start and a.session_start = a.session_end) "
						+ " )as dt , "
						+ " mod_ia_projects c where dt.PROJECT_NAME = c.PROJECT_NAME group by dt.browser_name;";
				
			}
			
			//connect to the database and get records
			try{
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
//				log.error("GetBrowserReport sql q : " + sqlQuery);
				browserData = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("GetBrowserReport : " + e);
			}finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getBrowserReport : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return browserData;
		}
		
		/**
		 * Function to retrieve Screen resolution information to be shown on reports 
		 * @author Omkar  created on 09-23-2015
		 */
		@Override
		public Dataset getScreenResolutionData( String projectName,boolean allProjects, int duration){
			
			Dataset screensData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "a.session_start");
			String sqlQuery = "";
			
			if(allProjects)
			{
//				sqlQuery = "select distinct a.screen_resolution as Screen , count(distinct a.username) as People ,"
//						+ " sum(b.no_of_actions) as Actions,"
//						+ " count(b.session_start)as Visits, sum(b.no_of_screens) as noOfScreens"  
//						+ "	from mod_ia_clients a, mod_ia_daily_sessions b"
//						+ " where a.start_timestamp >= b.session_start and a.start_timestamp <= b.session_end and "
//						+   dateFilter
//						+ " group by a.screen_resolution;";
				sqlQuery = "select dt.SCREEN_RESOLUTION as SCREEN_RESOLUTION, count(distinct (concat(dt.username, c.AUTH_PROFILE))) as People,"
						+ " count(dt.session_start) as Visits, sum(dt.no_of_actions)  as Actions, sum(dt.no_of_screens) as noOfScreens"
						+ " from ("
						+ " select distinct b.SCREEN_RESOLUTION as SCREEN_RESOLUTION, "
						+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
						+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
						+ " FROM mod_ia_daily_sessions a, mod_ia_clients b"
						+ " where " + dateFilter + " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
						+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end and a.username = b.username "
						+ " and a.SESSION_CONTEXT = b.CLIENT_CONTEXT and a.HOSTNAME= b.HOSTNAME"
//						+ " union"
//						+ "	select b.SCREEN_RESOLUTION as SCREEN_RESOLUTION,"
//						+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//						+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
//						+ " FROM mod_ia_daily_sessions a, mod_ia_clients b "
//						+ " where " + dateFilter + " and b.START_TIMESTAMP >= a.session_start "
//						+ " and a.PROJECT_NAME = b.PROJECT and a.session_start = a.session_end  ) as dt, "
						+ " ) as dt, "
						+ " mod_ia_projects c"
						+ " where dt.PROJECT = c.PROJECT_NAME group by SCREEN_RESOLUTION ;";
			}
			else
			{

				sqlQuery = "select dt.SCREEN_RESOLUTION as SCREEN_RESOLUTION, count(distinct (concat(dt.username, c.AUTH_PROFILE))) as people,"
						+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
						+ " from ("
						+ " select distinct b.SCREEN_RESOLUTION as SCREEN_RESOLUTION, "
						+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
						+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
						+ " FROM mod_ia_daily_sessions a, mod_ia_clients b"
						+ " where " + dateFilter + "and b.project = '" + projectName + "' and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
						+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end and a.username = b.username"
						+ " and a.SESSION_CONTEXT = b.CLIENT_CONTEXT and a.HOSTNAME= b.HOSTNAME"
//						+ " union"
//						+ "	select b.SCREEN_RESOLUTION as SCREEN_RESOLUTION,"
//						+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//						+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT"
//						+ " FROM mod_ia_daily_sessions a, mod_ia_clients b "
//						+ " where " + dateFilter + "and b.project = '" + projectName + "' and b.START_TIMESTAMP >= a.session_start "
//						+ " and a.PROJECT_NAME = b.PROJECT and a.session_start = a.session_end  ) as dt, "
						+ " ) as dt,"
						+ " mod_ia_projects c"
						+ " where dt.PROJECT = c.PROJECT_NAME group by SCREEN_RESOLUTION ;";
						
			}
			try {
				
				con = ds.getConnection();
				screensData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getScreenResolutionData : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			return screensData;

		}
		
		/**
		 * Function to retrieve Cities information to be shown on reports 
		 * @author Omkar  created on 09-24-2015
		 */
		@Override
		public Dataset getCitiesReportData( String projectName,boolean allProjects, int duration){
			
			Dataset citiesData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "session_start");
			String dateFilterClients = getDateFilter(duration, "start_timestamp");
			String sqlQuery = "";
			
			if(allProjects)
			{
				sqlQuery = "select dt.location as Cities, count(distinct (concat(dt.username , b.AUTH_PROFILE))) as People, count(dt.session_start) as Visits,"
						+ " sum(dt.no_of_actions) as Actions, sum(dt.no_of_screens) as noOfScreens from"
						+ " ( select a.location as location , a.username as username, b.session_start as session_start,"
						+ " b.no_of_actions as no_of_actions,  b.no_of_screens as no_of_screens , b.PROJECT_NAME as PROJECT_NAME, a.HOSTNAME as HOSTNAME"
						+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, concat(CITY, ', ' ,STATE ,', ' ,COUNTRY ) as location, CLIENT_CONTEXT, HOSTNAME"
						+ "	from mod_ia_clients  join mod_ia_location_info"
						+ " on mod_ia_clients.HOST_INTERNAL_IP = mod_ia_location_info.INTERNAL_IP and mod_ia_clients.HOST_EXTERNAL_IP = mod_ia_location_info.EXTERNAL_IP where " + dateFilterClients
						+ "	) as a, mod_ia_daily_sessions b	where session_start != session_end and " + dateFilter 
						+ " and a.username = b.username and a.start_timestamp >= b.session_start"
						+ "	and a.start_timestamp <= b.session_end and a.CLIENT_CONTEXT = b.SESSION_CONTEXT and a.HOSTNAME = b.HOSTNAME"
						+ "	union "
						+ "	select a.location as location , a.username as username, b.session_start as session_start,"
						+ "	b.no_of_actions as no_of_actions, b.no_of_screens as no_of_screens, b.PROJECT_NAME as PROJECT_NAME, a.HOSTNAME as HOSTNAME"
						+ "	from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, concat(CITY , ', ', STATE , ', ' , COUNTRY) as location, CLIENT_CONTEXT, HOSTNAME"
						+ "	from mod_ia_clients  join mod_ia_location_info"
						+ "	on mod_ia_clients.HOST_INTERNAL_IP = mod_ia_location_info.INTERNAL_IP and mod_ia_clients.HOST_EXTERNAL_IP = mod_ia_location_info.EXTERNAL_IP"
						+ "	where " + dateFilterClients + " ) as a,"
						+ "	mod_ia_daily_sessions b	where session_start = session_end and a.CLIENT_CONTEXT = b.SESSION_CONTEXT and a.HOSTNAME = b.HOSTNAME and " 
						+ dateFilter + " and a.username = b.username and a.start_timestamp >= b.session_start  "
						+ " ) as dt, mod_ia_projects b where dt.PROJECT_NAME = b.PROJECT_NAME group by  dt.location;";
			}
			else
			{
//				sqlQuery =  "select a.location as Cities , count(distinct a.username) as People, count(b.session_start) as Visits, sum(b.no_of_actions) as Actions, sum(b.no_of_screens) as noOfScreens "
//						+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, STATE, COUNTRY) as location "
//						+ " from mod_ia_clients "
//						+ " join mod_ia_location_info "
//						+ " on mod_ia_clients.HOST_INTERNAL_IP = mod_ia_location_info.INTERNAL_IP where " + dateFilterClients
//						+ " ) as a, mod_ia_daily_sessions b where " + dateFilter + " and a.username = b.username and a.start_timestamp >= b.session_start "
//						+ " and a.start_timestamp <= b.session_end "
//						+ " and PROJECT_NAME = '" + projectName + "'"
//						+ " group by a.location order by People desc;";
				

				sqlQuery = "select dt.location as Cities, count(distinct (concat(dt.username , b.AUTH_PROFILE))) as People, count(dt.session_start) as Visits,"
						+ " sum(dt.no_of_actions) as Actions, sum(dt.no_of_screens) as noOfScreens from"
						+ " ( select a.location as location , a.username as username, b.session_start as session_start,"
						+ " b.no_of_actions as no_of_actions,  b.no_of_screens as no_of_screens , b.PROJECT_NAME as PROJECT_NAME, a.HOSTNAME as HOSTNAME"
						+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, concat(CITY , ', ' , STATE , ', ' , COUNTRY ) as location, CLIENT_CONTEXT, HOSTNAME"
						+ "	from mod_ia_clients  join mod_ia_location_info"
						+ " on mod_ia_clients.HOST_INTERNAL_IP = mod_ia_location_info.INTERNAL_IP and mod_ia_clients.HOST_EXTERNAL_IP = mod_ia_location_info.EXTERNAL_IP where " + dateFilterClients
						+ "	) as a, mod_ia_daily_sessions b	where session_start != session_end and " + dateFilter 
						+ " and a.username = b.username and a.start_timestamp >= b.session_start "
						+ " and a.CLIENT_CONTEXT = b.SESSION_CONTEXT and a.HOSTNAME = b.HOSTNAME"
						+ "	and a.start_timestamp <= b.session_end and b.PROJECT_NAME = '" + projectName + "' "
						+ "	union "
						+ "	select a.location as location , a.username as username, b.session_start as session_start,"
						+ "	b.no_of_actions as no_of_actions, b.no_of_screens as no_of_screens, b.PROJECT_NAME as PROJECT_NAME, a.HOSTNAME as HOSTNAME"
						+ "	from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, concat(CITY , ', ' , STATE , ', ', COUNTRY) as location, CLIENT_CONTEXT, HOSTNAME"
						+ "	from mod_ia_clients  join mod_ia_location_info"
						+ "	on mod_ia_clients.HOST_INTERNAL_IP = mod_ia_location_info.INTERNAL_IP and mod_ia_clients.HOST_EXTERNAL_IP = mod_ia_location_info.EXTERNAL_IP "
						+ "	where " + dateFilterClients + " ) as a,"
						+ "	mod_ia_daily_sessions b	where session_start = session_end"
						+ " and a.CLIENT_CONTEXT = b.SESSION_CONTEXT and a.HOSTNAME = b.HOSTNAME and " 
						+ dateFilter + " and a.username = b.username and a.start_timestamp >= b.session_start "
						+ " and b.PROJECT_NAME = '" + projectName + "' "
						+ " ) as dt, mod_ia_projects b where dt.PROJECT_NAME = b.PROJECT_NAME group by  dt.location;";
						
			}
			try {
				con = ds.getConnection();
				
				citiesData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getCitiesReportData : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			return citiesData;

		}
		
		public Dataset getAlarmsSummaryReport( String projectName,boolean allProjects, int duration){
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "ALARM_DATE");
			String sqlQuery = "";
			
			/**
			 * commenting the if else condition as alarms are not specific to a project.
			 */

				sqlQuery = "select alarm_name,alarm_priority,sum(alarms_count) as Quantity,"
						 + " time_format(SEC_TO_TIME(avg(TIME_TO_SEC(coalesce(avg_time_to_ack,0.0)))),'%H:%i:%s') as TimeToAck,"
						 + " time_format(SEC_TO_TIME(avg(TIME_TO_SEC(coalesce(avg_time_to_clear,0.0)) )), '%H:%i:%s')as TimetToClr,  avg(TIME_TO_SEC(avg_time_to_ack)) timeToAckSeconds,"
						 + " avg(TIME_TO_SEC(avg_time_to_clear) ) as timeToClrSeconds from mod_ia_daily_alarms_summary where "
						 + 	dateFilter
						 + " group by alarm_priority,alarm_name;";
				
			try {
				con = ds.getConnection();
				
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getAlarmsSummaryReport : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			return freqData;

		}
		public boolean checkUserOnlineOrOffline( String projectName, boolean allProjects,String userName, String profileName){
			
			boolean userOnline = false;
			List<ClientReqSession> sessions = mycontext.getGatewaySessionManager().findSessions();
			int noOfSessions = 0;
			String currentUserProject = "";
			User _currentUser;
			if(sessions != null && sessions.size() > 0)
			{
				noOfSessions = sessions.size();
				for(int i=0; i<noOfSessions; i++)
				{
					_currentUser =(User) sessions.get(i).getAttribute(ClientReqSession.SESSION_USER);
					if(allProjects)
					{
						if(_currentUser.get(User.Username).compareToIgnoreCase(userName) == 0 &&
							_currentUser.getProfileName().compareToIgnoreCase(profileName) == 0 )
						{
							userOnline = true;
							break;
						}
					}
					else
					{
						currentUserProject = sessions.get(i).getAttribute(ClientReqSession.SESSION_PROJECT_NAME).toString();
						//currentUserProject  = this.mycontext.getProjectManager().getProjectName(Long.parseLong(currentUserProject), ProjectVersion.Published);
						
						if(_currentUser.get(User.Username).compareToIgnoreCase(userName) == 0 &&
								_currentUser.getProfileName().compareToIgnoreCase(profileName) == 0
							&& currentUserProject.compareToIgnoreCase(projectName) == 0)
							{
								userOnline = true;
								break;
							}
					}
				}
			}
			return userOnline;

			
			
		}
		/*
		 * Method to return browsers information to be shown on Real time panel
		 * @author : Omkar Awate created on : 16-Oct-2015
		 */
		public Dataset getBrowserInformation(int duration, String projectName, boolean allProjects)
		{
			Dataset browserData = null;
			
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilter(duration, "b.start_timestamp");
			String sqlQuery = "";
			
			if(allProjects)
			{
				sqlQuery = "select a.browser_name as browser_name, count(a.browser_name) as bCount "
						  + " from "
						  + " mod_ia_browser_info a, mod_ia_clients b"
						  + " where (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP)  "
						  + " and a.timestamp <= b.start_timestamp and a.timestamp = (select max(timestamp) from mod_ia_browser_info where timestamp <= b.start_timestamp) and "
						  + dateFilter
						  + " group by a.browser_name order by bCount desc;";
				
			}
			else
			{
				sqlQuery = "select a.browser_name as browser_name, count(a.browser_name) as bCount "
						  + " from "
						  + " mod_ia_browser_info a, mod_ia_clients b"
						  + " where (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP)  "
						  + " and a.timestamp <= b.start_timestamp and a.timestamp = (select max(timestamp) from mod_ia_browser_info where timestamp <= b.start_timestamp) and "
						  + dateFilter 
						  + " and b.project = '" + projectName + "' "
						  + " group by a.browser_name order by bCount desc;";
						
			}
			try {
				
				con = ds.getConnection();
				browserData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getBrowserInformation : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			return browserData;
		}

		/**
		 * Method to return Groups information for reports.
		 * 
		 */

		@Override
		public List<GroupReportRecord> getGroupsReportData(String projectName,
				boolean allProjects, int duration) {
			
			List<GroupReportRecord> returnData = new ArrayList<GroupReportRecord>();
			Iterator<String> retrieveRoles;
			Iterator<User> retrieveUsers;
			String roleName;
			GroupReportRecord _record;
			HashMap<String,Integer> rolesMapPeople = new HashMap<String,Integer>();
			HashMap<String,Integer> rolesMapVisits = new HashMap<String,Integer>();
			HashMap<String,Integer> rolesMapActions = new HashMap<String,Integer>();
			Collection<String> profileRoles= null;
			Collection<User> profileUsers= null;
			HashMap<String,String> allProfileRoles=  new HashMap<String,String>();
			HashMap<String,Collection<User>> profileUsersMap = new HashMap<String,Collection<User>>();
			Dataset groupsData = null;
			int noOfRecs = 0, i =0;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			//first get list of all user profiles and in each profile, get role name
		
			SQuery<UserSourceProfileRecord> userProfilesQuery = new SQuery<UserSourceProfileRecord>(UserSourceProfileRecord.META);
			List<UserSourceProfileRecord> results;
			results = mycontext.getPersistenceInterface().query(userProfilesQuery);
			try {
			for (UserSourceProfileRecord record : results) {
			    String profileName = record.getName();
			    
				
			
				//store the user values in each profile in a hash map for later use
				profileUsers = mycontext.getUserSourceManager().getProfile(profileName).getUsers();
				profileUsersMap.put(profileName, profileUsers);
				
				//get all the roles from the profile and store in hash maps for people, visits and actions.
				profileRoles =	mycontext.getUserSourceManager().getProfile(profileName).getRoles();
				retrieveRoles = profileRoles.iterator();
	 			while(retrieveRoles.hasNext())
	 			{
	 				//even if there is same role name in multiple profiles, it will overwrite the values that is what we want.
	 				roleName = retrieveRoles.next();
	 				rolesMapPeople.put(roleName, 0);
	 				rolesMapVisits.put(roleName, 0);
	 				rolesMapActions.put(roleName, 0);
	 				allProfileRoles.put(roleName,roleName);
	 			}
			}
			} catch (Exception e) {
				
				log.error("getGroupsReportData : error retrieving roles" + e);
			}
			//
			String dateFilter = getDateFilter(duration, "EVENT_TIMESTAMP");
			String dateFilter1 = getDateFilter(duration, "VIEW_TIMESTAMP");
			String sqlQuery = "";
			//get the visits and actions data group by people.
			
			if(allProjects)
			{
				sqlQuery = "select tab1.actor , tab1.profile, tab1.actions, tab2.visits from "
						+ " (select actor, profile, sum(actions) as actions from "
						+ " (SELECT a.actor as actor,b.AUTH_PROFILE as profile,  count(a.action) as actions"
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b where"
						+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and "
						+ " a.actor != 'SYSTEM' and a.status_code = 0 and "
						+ dateFilter + "group by a.actor,b.AUTH_PROFILE "
						+ " union all"
						+ " SELECT c.USERNAME as actor,d.AUTH_PROFILE as profile,  count(c.screen_name) as actions"
						+ " FROM mod_ia_screen_views c,  mod_ia_projects d where c.PROJECT = d.PROJECT_NAME and "
						+ " c.action = 'SCREEN_OPEN' and "
						+ dateFilter1 + " group by c.USERNAME,d.AUTH_PROFILE ) as dt "
						+ " group by actor,profile) as tab1, "
						+ " (SELECT a.actor as actor,b.AUTH_PROFILE as profile,  count(a.action) as visits"
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b where"
						+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and "
						+ " a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
						+ dateFilter + " group by a.actor,b.AUTH_PROFILE) "
						+ " as tab2 where tab1.actor = tab2.actor and tab1.profile = tab2.profile;";
				
			}
			else
			{
				sqlQuery = "select tab1.actor, tab1.profile, tab1.actions, tab2.visits from "
						+ " (select actor, profile, sum(actions) as actions from "
						+ " (SELECT a.actor as actor,b.AUTH_PROFILE as profile,  count(a.action) as actions"
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b where"
						+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and "
						+ " a.ORIGINATING_SYSTEM = concat('project=','" + projectName 
						+ "') and a.actor != 'SYSTEM' and a.status_code = 0 and "
						+ dateFilter + "group by a.actor,b.AUTH_PROFILE "
						+ " union all"
						+ " SELECT c.USERNAME as actor,d.AUTH_PROFILE as profile,  count(c.screen_name) as actions"
						+ " FROM mod_ia_screen_views c,  mod_ia_projects d where c.PROJECT = d.PROJECT_NAME and "
						+ " c.action = 'SCREEN_OPEN' and "
						+ dateFilter1 + " group by c.USERNAME,d.AUTH_PROFILE ) as dt "
						+ " group by actor,profile) as tab1, "
						+ " (SELECT a.actor as actor,b.AUTH_PROFILE as profile,  count(a.action) as visits"
						+ " FROM AUDIT_EVENTS a,  mod_ia_projects b where"
						+ " a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) and "
						+ " a.ORIGINATING_SYSTEM = concat('project=','" + projectName 
						+ "') and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
						+ dateFilter + " group by a.actor,b.AUTH_PROFILE) "
						+ " as tab2 where tab1.actor = tab2.actor and tab1.profile = tab2.profile;";
						
			}
			try {
				
				con = ds.getConnection();
				
//				log.error("getGroupsReportData : sql q : " + sqlQuery);
				groupsData = con.runQuery(sqlQuery);
				String userName = "", profileName = "";
				User userRec;
				Collection<String> userRoles;
				int noOfPeople;
				int noOfActions;
				int noOfVisits;
				int rNoActions, rNoVisits;
				if(groupsData != null)
				{
//					log.error("getGroupsReportData :groupsData not null ");
					noOfRecs = groupsData.getRowCount();
					//for each person 
					for(i=0; i<noOfRecs; i++)
					{
						
						rNoActions  = 0;
						rNoVisits = 0;
						//get username and profilename
						if(groupsData.getValueAt(i, 0) != null && groupsData.getValueAt(i, 1) != null)
						{
							userName = groupsData.getValueAt(i, 0).toString();
							profileName = groupsData.getValueAt(i, 1).toString();
							if(groupsData.getValueAt(i, 2) != null)
							{
								rNoActions = (int) Float.parseFloat(groupsData.getValueAt(i, 2).toString());
							}
							if(groupsData.getValueAt(i, 3) != null)
							{
								rNoVisits = (int) Float.parseFloat(groupsData.getValueAt(i, 3).toString());
							}
							//based on profilenmae, get users list
							profileUsers = profileUsersMap.get(profileName);
							retrieveUsers = profileUsers.iterator();

							while(retrieveUsers.hasNext())
							{
								
								userRec = retrieveUsers.next();
								if(userRec.get(User.Username).compareToIgnoreCase(userName) == 0)
								{
									//if given user exists, get roles.
									userRoles = userRec.getRoles();
									
									//for each role , update people, actions and visits values.
									retrieveRoles = userRoles.iterator();
									while(retrieveRoles.hasNext())
									{
										roleName = retrieveRoles.next();
										//store no of people
										//get orig value, incerement by 1 and put back
										noOfPeople = rolesMapPeople.get(roleName);
										rolesMapPeople.put(roleName, (noOfPeople + 1));
										
										//calculate and store noOfActions for this role.
										noOfActions = rolesMapActions.get(roleName);
										rolesMapActions.put(roleName,( noOfActions + rNoActions));
										
										//calculate and store noOfVisits for this role.
										noOfVisits = rolesMapVisits.get(roleName);
										rolesMapVisits.put(roleName,( noOfVisits + rNoVisits));
									}
									
								}
							}
						
						
						}
					}
					
					//now create the dataset to be returned.
					profileRoles = allProfileRoles.values();
					retrieveRoles = profileRoles.iterator();
					
		 			while(retrieveRoles.hasNext())
		 			{
		 				//even if there is same role name in multiple profiles, it will overwrite the values that is what we want.
		 				roleName = retrieveRoles.next();
		 				_record = new GroupReportRecord();
		 				_record.setGroupName(roleName);
		 				_record.setNoOfActions(rolesMapActions.get(roleName));
		 				_record.setNoOfPeople( rolesMapPeople.get(roleName));
		 				_record.setNoOfVisits(rolesMapVisits.get(roleName));
		 				returnData.add(_record);
		 			}
					
		 			
				}
				else
				{
//					log.error("getGroupsReportData :groupsData  null ");
				}
			}
			catch (Exception e) {
			log.error("getGroupsReport : exception is " + e);
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getGroupsReportData : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			return returnData;
		}

		@Override
		public Dataset getErrorsInformation(String projectName,
				boolean allProjects, int duration) {
			Dataset auditRecs = null;
			String sqlQuery = "select ACTOR, ACTION, EVENT_TIMESTAMP from AUDIT_EVENTS where STATUS_CODE != 0;";
			try {
				Datasource ds;
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				SRConnection con = ds.getConnection();
				auditRecs = con.runQuery(sqlQuery);;
			
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			//check teh console log
			
	
			return auditRecs;
		}
		
		/**
		 * New method created by Omkar on 29-Jan-2016
		 * This is required as yesterday information needed in Header Panel and yesterday overview section of dashboard are different.
		 * @param duration
		 * @param projectName
		 * @param allProjects
		 * @return
		 */
		@Override
		public OverviewInformation getYesterdayOverviewForSlider( int duration,
				String projectName, boolean allProjects) {
				
				OverviewInformation info = new OverviewInformation();
			
				Datasource ds;
				Dataset resDS = null;
				Dataset resDS1 = null;
				String startDate = "", endDate = "";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				SRConnection con = null;
						
				String dateFilter = getDateFilter(duration, "overview_date");
				String sqlQuery = "";
				String sqlQueryTotalUsers = "";
				String sqlScreensQuery = "";
				String sqlScreensQueryTotal = "";
				//calculate yesterday time period for given duration
				Calendar durationStart = Calendar.getInstance();
				durationStart.set(Calendar.HOUR_OF_DAY, 00);
				durationStart.set(Calendar.MINUTE, 00);
				durationStart.set(Calendar.SECOND, 00);
			   
			    
			    Calendar durationEnd = Calendar.getInstance();
			    durationEnd.set(Calendar.HOUR_OF_DAY, 23);
			    durationEnd.set(Calendar.MINUTE, 59);
			    durationEnd.set(Calendar.SECOND, 59);
			   
			    switch(duration)
			    {
			    case Constants.TODAY:
			    	durationStart.add(Calendar.DATE, -1);
			    	durationEnd.add(Calendar.DATE, -1);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					
			    	break;
			    case Constants.YESTERDAY:
			    	durationStart.add(Calendar.DATE, -2);
			    	durationEnd.add(Calendar.DATE, -2);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.LAST_SEVEN_DAYS:
					durationStart.add(Calendar.DATE, -14);
			    	durationEnd.add(Calendar.DATE, -7);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.LAST_THIRTY_DAYS:
					durationStart.add(Calendar.DATE, -60);
			    	durationEnd.add(Calendar.DATE, -30);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.LAST_NINTY_DAYS:
					durationStart.add(Calendar.DATE, -180);
			    	durationEnd.add(Calendar.DATE, -90);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.LAST_365_DAYS:
					durationStart.add(Calendar.DATE, -730);
			    	durationEnd.add(Calendar.DATE, -365);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.LAST_MONTH:
					durationStart.set(Calendar.MONTH, durationStart.get(Calendar.MONTH) - 2);
					durationStart.set(Calendar.DAY_OF_MONTH, 1);
				   	durationEnd.set(Calendar.MONTH, durationEnd.get(Calendar.MONTH) - 2);
			    	durationEnd.set(Calendar.DAY_OF_MONTH, durationEnd.getActualMaximum(durationEnd.DAY_OF_MONTH));
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.LAST_WEEK:
					durationStart.set(Calendar.WEEK_OF_YEAR, durationStart.get(Calendar.WEEK_OF_YEAR) - 2);
					durationStart.set(Calendar.DAY_OF_WEEK, durationStart.getFirstDayOfWeek());
					durationEnd.set(Calendar.WEEK_OF_YEAR, durationEnd.get(Calendar.WEEK_OF_YEAR) - 2);
					durationEnd.set(Calendar.DAY_OF_WEEK, durationEnd.getFirstDayOfWeek() + 6);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.LAST_YEAR:
					durationStart.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 2);
					durationStart.set(Calendar.DAY_OF_YEAR, 1);
					durationEnd.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 2);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.THIS_MONTH:
					durationStart.set(Calendar.MONTH, durationStart.get(Calendar.MONTH) - 1);
					durationStart.set(Calendar.DAY_OF_MONTH, 1);
				   	durationEnd.set(Calendar.MONTH, durationEnd.get(Calendar.MONTH) - 1);
			    	durationEnd.set(Calendar.DAY_OF_MONTH, durationEnd.getActualMaximum(durationEnd.DAY_OF_MONTH));
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				case Constants.THIS_WEEK:
					durationStart.set(Calendar.WEEK_OF_YEAR, durationStart.get(Calendar.WEEK_OF_YEAR) - 1);
					durationStart.set(Calendar.DAY_OF_WEEK, durationStart.getFirstDayOfWeek());
					durationEnd.set(Calendar.WEEK_OF_YEAR, durationEnd.get(Calendar.WEEK_OF_YEAR) - 1);
					durationEnd.set(Calendar.DAY_OF_WEEK, durationEnd.getFirstDayOfWeek() + 6);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					
					break;
				case Constants.THIS_YEAR:
					durationStart.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 1);
					durationStart.set(Calendar.DAY_OF_YEAR, 1);
					durationEnd.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 1);
					startDate = sdf.format(durationStart.getTime());
					endDate = sdf.format(durationEnd.getTime());
					break;
				default:
			    }
			    
			    
//
			    
			    dateFilter = "session_date >= '" + startDate + "' and session_date <= '" + endDate + "' and session_start >= '"+this.installDate+"'";
			    String dateFilterAuditEvents = "EVENT_TIMESTAMP >= '" + startDate + "' and EVENT_TIMESTAMP <= '" + endDate + "' and EVENT_TIMESTAMP >= '"+this.installDate+"'";;
				String screenViewDateFilter = "VIEW_TIMESTAMP >= '" + startDate + "' and VIEW_TIMESTAMP <= '" + endDate + "'";
				String sessionDateFilter = "s.session_date >= '" + startDate + "' and s.session_date <= '" + endDate + "' and s.session_start > '"+this.installDate+"'";
				
				
				if(allProjects)
				{
					sqlQuery = "select sum(no_of_screens+no_of_actions) as ACTIONS,"
							+ " SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))) as AVG_SESSION_DURATION,"
							+ " (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION,"
							+ " count(session_start) as sessions"
							+ " from mod_ia_daily_sessions  WHERE "
							+ dateFilter + ";";
					
					sqlScreensQuery = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
							 + " (SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
							 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
							 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
							 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
							 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME) as x"
							 + " where x.PROJECT = b.PROJECT_NAME"
							 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
					
					sqlScreensQueryTotal = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
							 + " (SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
							 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
							 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
							 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
							 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME) as x"
							 + " where x.PROJECT = b.PROJECT_NAME"
							 + " ;";
					
					sqlQueryTotalUsers =  "SELECT count(distinct( concat(a.actor , b.AUTH_PROFILE)))"
							+ " from AUDIT_EVENTS a,  mod_ia_projects b"
							+ " where a.ORIGINATING_SYSTEM = concat('project=' , b.PROJECT_NAME)"
							+ " and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
							+ dateFilterAuditEvents + " ;"; 
					
				}
				else
				{
					sqlQuery = "select sum(no_of_screens+no_of_actions) as ACTIONS,"
							+ " SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))) as AVG_SESSION_DURATION,"
							+ " (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION,"
							+ " count(session_start) as sessions"
							+ " from mod_ia_daily_sessions  WHERE "
							+ dateFilter + " and PROJECT_NAME = '" + projectName + "' ;";
					
					sqlScreensQuery = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
							 + " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
							 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
							 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
							 + " and a.project = '" + projectName + "' "
							 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
							 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
							 + " where x.PROJECT = b.PROJECT_NAME"
							 + " GROUP BY x.username, b.AUTH_PROFILE having count(screen_name) = 1;";
					
					sqlScreensQueryTotal = " SELECT count(x.SCREEN_NAME) from mod_ia_projects b, "
							 + " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project"
							 + " FROM MOD_IA_SCREEN_VIEWS a, MOD_IA_DAILY_SESSIONS s"
							 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter
							 + " and a.project = '" + projectName + "' "
							 + " and a.PROJECT = s.PROJECT_NAME and a.VIEW_TIMESTAMP >= s.SESSION_START "
							 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
							 + " where x.PROJECT = b.PROJECT_NAME"
							 + " ;";
					
					sqlQueryTotalUsers =  "SELECT count(distinct( concat(a.actor , b.AUTH_PROFILE)))"
							+ " from AUDIT_EVENTS a,  mod_ia_projects b"
							+ " where a.ORIGINATING_SYSTEM = 'project=' + b.PROJECT_NAME "
							+ " and a.actor != 'SYSTEM' and a.action = 'login' and a.status_code = 0 and "
							+ dateFilterAuditEvents + " and ORIGINATING_SYSTEM like '%"
							+ projectName + "';";;
				}
			    try {
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
//					log.error("getYesterdayOverviewForSlider : sqlQ " + sqlQuery);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							info.setNoOfActions((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
						}
						if(resDS.getValueAt(0, 1) != null)
						{
							info.setAvgSessionDuration(resDS.getValueAt(0, 1).toString());
						}
						if(resDS.getValueAt(0, 2) != null)
						{
							info.setAverageScreensPerVisit(Float.parseFloat(resDS.getValueAt(0, 2).toString()));
						}
						if(resDS.getValueAt(0, 3) != null)
						{
							info.setNoOfSessions((int)(Float.parseFloat(resDS.getValueAt(0, 3).toString())));
						}
					}
					
//					log.error("getYesterdayOverviewForSlider : screens q " + sqlScreensQuery);
					resDS = con.runQuery(sqlScreensQuery);
					
					float usersWithOneScreen = 0;
					float totalUsers = 0;
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							usersWithOneScreen = Float.parseFloat(resDS.getValueAt(0, 0).toString());
						}

					}
					
//					log.error("getYesterdayOverviewForSlider : sqlQueryTotalUsers q " + sqlQueryTotalUsers);
					resDS = con.runQuery(sqlQueryTotalUsers);
									
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							totalUsers = Float.parseFloat(resDS.getValueAt(0, 0).toString());
							info.setNoOfActiveUsers ((int)totalUsers);
						}
					}
					if(usersWithOneScreen == 0 || totalUsers == 0)
					{
						info.setBounceRate(0);
					}
					else
					{
						info.setBounceRate(usersWithOneScreen/totalUsers);
					}
					
					resDS = null;
					resDS = con.runQuery(sqlScreensQueryTotal);
					
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							info.setNoOfScreenViews((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
						}
						else
						{
							info.setNoOfScreenViews(0);
						}
					}
					else
					{
						info.setNoOfScreenViews(0);
					}
				}
				catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
			}
				
			return info;
		}

		/****
		 * Done by : YM : 05-Feb-2016.
		 * Added new method to retrieve location, devices and browsers information for logged in users.
		 * 
		 */
		@Override
		public LocationDeviceBrowserCounts getRealTimeLocationsDevicesAndBrowsers(
				String projectName, boolean allProjects,
				HashMap<String, Date> users) {
			
			LocationDeviceBrowserCounts returnData = new LocationDeviceBrowserCounts();
			
			
			Datasource _ds ; //local variable
			Dataset resDS = null;
		
			_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			//noOfDS = dsList.size();
			String sqlQueryLoc_devices = "";
			String sqlQueryBrowsers = "";
			SRConnection con = null;
			
			String userWithProfile = "";
			String userName = "";
			String profileName = "";
			Date creationDate = new Date();
			HashMap.Entry<String,Date> userRec;
			
			HashMap<String, Integer> locations = new HashMap<String, Integer>();
			
			DevicesInformation devices = new DevicesInformation();
			devices.setNoOfClientsOnDesktop(0);
			devices.setNoOfClientsOnMobile(0);
			
			HashMap<String, Integer> browsers = new HashMap<String, Integer>();
			HashMap<String, Integer> opSystems = new HashMap<String, Integer>();
			
			String location = "";
			String browserName = "";
			boolean deviceType = false;
			int noOfUsersPerLocation = 0;
			int noOfUsersPerBrowser = 0;
			String osName = "";
			int i = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			try {
				
					con = _ds.getConnection();
				
					Iterator<HashMap.Entry<String,Date>> itr = users.entrySet().iterator();
					while(itr.hasNext())
					{
						userRec = itr.next();	
						userWithProfile = userRec.getKey();
						creationDate = userRec.getValue();
						userName = userWithProfile.split(":")[0].trim();
						profileName = userWithProfile.split(":")[1].trim();
						
						if(allProjects)
						{
							sqlQueryLoc_devices = "select CONCAT(CITY, ',', STATE, ',',COUNTRY) as location, IS_MOBILE, OS_NAME "
									+ "	from mod_ia_clients join mod_ia_location_info on "
									+ "	mod_ia_clients.HOST_INTERNAL_IP = mod_ia_location_info.INTERNAL_IP and mod_ia_clients.HOST_EXTERNAL_IP = mod_ia_location_info.EXTERNAL_IP where "
									+ " username = '" + userName + "' and start_timestamp >= '"
									+ sdf.format(creationDate) + "' and project in "
							+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName + "') ;";
							
							
							sqlQueryBrowsers = "select a.browser_name as browser_name  "
									+ "from mod_ia_browser_info a, mod_ia_clients b "
									+ " where (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP) "
									+ " and a.timestamp <= b.start_timestamp and a.timestamp = "
									+ " (select max(timestamp) from mod_ia_browser_info where timestamp <= b.start_timestamp) and "
									+ " b.start_timestamp >= '" + sdf.format(creationDate) + "' and b.username = '" 
									+ userName + "' and project in "
									+ " (select project_name from mod_ia_projects where AUTH_PROFILE = '" + profileName+ "') ;";
						}
						else
						{
							sqlQueryLoc_devices = "select CONCAT(CITY, ',', STATE, ',',COUNTRY) as location, IS_MOBILE, OS_NAME "
									+ "	from mod_ia_clients join mod_ia_location_info on "
									+ "	mod_ia_clients.HOST_INTERNAL_IP = mod_ia_location_info.INTERNAL_IP and mod_ia_clients.HOST_EXTERNAL_IP = mod_ia_location_info.EXTERNAL_IP where "
									+ " username = '" + userName + "' and start_timestamp >= '"
									+ sdf.format(creationDate) + "' and project = '" + projectName  + "';";
							
							
							sqlQueryBrowsers = "select a.browser_name as browser_name  "
									+ "from mod_ia_browser_info a, mod_ia_clients b "
									+ " where (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP) "
									+ " and a.timestamp <= b.start_timestamp and a.timestamp = "
									+ " (select max(timestamp) from mod_ia_browser_info where timestamp <= b.start_timestamp) and "
									+ " b.start_timestamp >= '" + sdf.format(creationDate) + "' and b.username = '" + userName 
									+ "' and project = '" + projectName  + "';";
						}
						
						
//						log.error("getRealTimeLocationsDevicesAndBrowsers sql q loc device : "  + sqlQueryLoc_devices);
//						log.error("getRealTimeLocationsDevicesAndBrowsers sql q browsers: "  + sqlQueryBrowsers);
						
						//get the location information
						resDS = con.runQuery(sqlQueryLoc_devices);
						
						if(resDS != null && resDS.getRowCount() > 0)
						{
							//extract location name
							if(resDS.getValueAt(0, 0) != null)
							{
								location = resDS.getValueAt(0, 0).toString();
								if(locations.containsKey(location))
								{
									noOfUsersPerLocation = locations.get(location);
									noOfUsersPerLocation = noOfUsersPerLocation + 1;
									locations.put(location, noOfUsersPerLocation);
								}
								else
								{
									locations.put(location, 1);
								}
							}
							
							//extract device type
							if(resDS.getValueAt(0, 1) != null)
							{
								deviceType = Boolean.parseBoolean(resDS.getValueAt(0, 1).toString());
								if(deviceType == true)
								{
									devices.setNoOfClientsOnMobile(devices.getNoOfClientsOnMobile() + 1);
								}
								else
								{
									devices.setNoOfClientsOnDesktop(devices.getNoOfClientsOnDesktop() + 1);
								}
							}
							
							//extract os name
							int noOfUserPerOS = 0;
							if(resDS.getValueAt(0, 2) != null)
							{
								osName = resDS.getValueAt(0, 2).toString().trim();
								if(opSystems.containsKey(osName))
								{
									noOfUserPerOS = opSystems.get(osName);
									noOfUserPerOS = noOfUserPerOS + 1;
									opSystems.put(osName, noOfUserPerOS);
								}
								else
								{
									opSystems.put(osName, 1);
								}
							}
						}
						
						//get browsers information
						resDS = null;
						
						resDS = con.runQuery(sqlQueryBrowsers);
						if(resDS != null && resDS.getRowCount() > 0)
						{
							if(resDS.getValueAt(0, 0) != null)
							{
								browserName = 	resDS.getValueAt(0, 0).toString().trim();
								if(browsers.containsKey(browserName))
								{
									noOfUsersPerBrowser = browsers.get(browserName);
									noOfUsersPerBrowser = noOfUsersPerBrowser + 1;
									browsers.put(browserName, noOfUsersPerBrowser);
									
								}
								else
								{
									browsers.put(browserName, 1);
								}
							}
						}
						
					}
					
					
			}
			catch (SQLException e) {
				
				log.error(e);
				
			}
			finally{
				if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
					
						e.printStackTrace();
					}
					
					}
			}
			
			//populate the data structure to be returned.
			
			returnData.setBrowsers(browsers);
			returnData.setLocations(locations);
			returnData.setDevices(devices);
			returnData.setOperatingSystems(opSystems);
			return returnData;
		}
		
		/**
		 * A function to retrieve Top OS information in a given duration
		 * Called from Dashboard Panel to display device information in graph
		 * Created by Yogini : 17-Feb-2016 
		 */
		
		@Override
		public Dataset getTopOperatingSystems( int duration,
				String projectName, boolean allProjects) {
		
			Datasource ds;
			Dataset resDS = null;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilter(duration, "START_TIMESTAMP");
			
			if(allProjects == false)
			{
				sqlQuery = "select OS_NAME , count( username) as 'Users'"
						+ " from mod_ia_clients  "
						+ " where  " + dateFilter + " and PROJECT = '"	+ projectName + "'"
						+ " group by OS_NAME order by Users desc;";
			}
			else
			{
				sqlQuery = "select OS_NAME, count( username) as 'Users'"
						+ " from mod_ia_clients  "
						+ " where  " + dateFilter
						+ " group by OS_NAME order by Users desc;";
			}
			
			
				try {
					
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getTopOperatingSystems : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
			}
		return resDS;
		}

		/**
		 * Added by Yogini on 07-March-2016
		 * This function would replace rpc.getUserProfiles call from UserPanel so as to show only users that are logged in only once.
		 * 
		 */
		@Override
		public Dataset getAllLoggedInUsers(boolean allProjects,
				String projectName) {
			Dataset users = null;
			Datasource ds;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
//			log.error("get all logged in users  : allProjects = " + allProjects);
			if(allProjects)
			{
				sqlQuery = "SELECT distinct(CONCAT(a.actor, ':', b.AUTH_PROFILE)) as uName, max(a.event_timestamp) as lastSeen"
						+ " from AUDIT_EVENTS a,  mod_ia_projects b "
						+ " where a.status_code = 0 and a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME) "
						+ " and a.EVENT_TIMESTAMP > '" + this.installDate + "' "
						+ " group By uName order by lastSeen desc;";
			}
			else
			{
				sqlQuery = "SELECT distinct(CONCAT(a.actor, ':', b.AUTH_PROFILE)) as uName, max(a.event_timestamp) as lastSeen"
						+ " from AUDIT_EVENTS a,  mod_ia_projects b "
						+ " where a.status_code = 0 and a.ORIGINATING_SYSTEM = concat('project=',b.PROJECT_NAME)"
						+ " and ORIGINATING_SYSTEM = concat('project=','" + projectName + "') "
						+ " and a.EVENT_TIMESTAMP > '" + this.installDate + "' "
						+ " group By uName order by lastSeen desc;";
			}
			
			
				try {
						con = ds.getConnection();
//						log.error("get all logged in users  q  : " + sqlQuery);
						users = con.runQuery(sqlQuery);
						
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getAllLoggedInUsers : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
			}
			return users;
		}
		
		/*
		 * By Omkar
		 */
		@Override
		public Dataset alarmSummaryReportRingChart(String projectName,boolean allProjects, int duration){
			Datasource ds;
			Dataset resDS = null;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilter(duration, "ALARM_DATE");
			
			
				sqlQuery = "select ALARM_NAME, SUM(ALARMS_COUNT) totalAlarms from mod_ia_daily_alarms_summary "
						+ " where "
						+ dateFilter
						+ " group by ALARM_NAME"
						+ " order by totalAlarms DESC"
						+ " limit 10;";
			
				
			
				try {
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getTopOperatingSystems : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
			}
		return resDS;
		}
		
		/*
		 * By Yogini 05-April-2016
		 */
		@Override
		public Dataset getTop10AlarmsByDuration(String projectName,boolean allProjects, int duration){
			Datasource ds;
			Dataset resDS = null;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilter(duration, "ALARM_DATE");
			
			
				sqlQuery = "select ALARM_NAME, (SUM(TIME_TO_SEC(TOTAL_ACTIVE_TIME)))/60 totalTime from mod_ia_daily_alarms_summary "
						+ " where "
						+ dateFilter
						+ " group by ALARM_NAME"
						+ " order by totalTime DESC"
						+ " limit 10;";
			
				
			
				try {
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getTop10AlarmsByDuration : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
			}
		return resDS;
		}
		
		/*
		 * Yogini : For alarm summary chart
		 */
		@Override
		public Dataset getAlarmCountsPerDuration(String projectName,boolean allProjects, int duration){
			Datasource ds;
			Dataset resDS = null;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilter(duration, "ALARM_DATE");
			
			
			if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
			{
				sqlQuery = "select a.Hour as Hour, coalesce(b.ALARM_COUNT, 0) from mod_ia_hours  a "
						+ " left join "
						+ " (select ALARM_HOUR, sum(ALARMS_COUNT) ALARM_COUNT "
						+ " FROM mod_ia_hourly_alarms_counts WHERE "
						+ dateFilter + " group by ALARM_HOUR) as b"
						+ " on a.Hour = b.ALARM_HOUR"
						+ " ;";
			}
			else if(duration == Constants.THIS_YEAR || duration == Constants.LAST_YEAR || duration == Constants.LAST_365_DAYS)
			{
				sqlQuery =  "select a.monthName, coalesce(b.ALARMS_COUNT,0) "
						+ " from mod_ia_month as a "
						+ " left join (select  month(ALARM_DATE) as overviewmonth ,SUM(ALARMS_COUNT) ALARMS_COUNT "
						+ " from mod_ia_daily_alarms_summary where "
						+ dateFilter + " group by overviewmonth ) as b"
						+ " on b.overviewmonth = a.monthNumber"
						+ "  ;";
			}
			else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
			{
				sqlQuery = "select dayname(ALARM_DATE), sum(ALARMS_COUNT) from MOD_IA_DAILY_ALARMS_SUMMARY where "
						+ dateFilter + " group by ALARM_DATE;";
			}
			else
			{
			
				sqlQuery = "SELECT ALARM_DATE, SUM(ALARMS_COUNT) "
						+ " FROM MOD_IA_DAILY_ALARMS_SUMMARY "
						+ " WHERE " + dateFilter + " group by ALARM_DATE "
						+ " ;";
						
			}
			
				
			
				try {
						con = ds.getConnection();
						log.error("alarms per day report q : " + sqlQuery);
						resDS = con.runQuery(sqlQuery);
						
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getAlarmCountsPerDuration : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
			}
		return resDS;
		}

		
		
		/*query total no of distinct users across all browsers. This is because mobile clients would not have browser information.
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getDistinctUsersFromBrowsers(int, java.lang.String, boolean)
		 */

		@Override
		public int getDistinctUsersFromBrowsers(int duration,
				String projectName, boolean allProjects) {
			int userCount = 0;
			Dataset browserData = null;
			Datasource ds;
			String sqlQuery = "";
			
			SRConnection con = null;
			String timefilter = getDateFilter(duration, "c.START_TIMESTAMP");
			String filter = "";
			if(allProjects )
			{
				filter = "";
			}
			else 
			{
				filter = " and c.PROJECT = '" + projectName + "' ";
			}
			

			sqlQuery = " select count(distinct(concat(c.username , a.AUTH_PROFILE)))"
					+ " from mod_ia_clients c, mod_ia_browser_info  d, mod_ia_projects a "
					+ " where (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ) "
					+ " and  d.IP_ADDRESS is not null and " + timefilter
					+ " and d.timestamp <= c.start_timestamp and a.PROJECT_NAME = c.PROJECT "
					+  filter + ";";
//			log.error("getDistinctUsersFromBrowsers : " + sqlQuery);
			//connect to the database and get records
			try{
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				
				browserData = con.runQuery(sqlQuery);
				
				if(browserData != null && browserData.getRowCount() > 0)
				{
					if(browserData.getValueAt(0, 0) != null)
					{
						userCount = (int)Float.parseFloat(browserData.getValueAt(0, 0).toString());	
					}
					 
				}
			}
			catch(Exception e){
				log.error("GetBrowserReport : " + e);
			}finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getBrowserReport : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return userCount;
		}

		
		

		
		

		/*
		 * Method to retrieve list of projects from a gateway
		 * if GatewayName input is null then send all project names from all gateways.
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getProjectsOnGateway(java.lang.String)
		 */
		@Override
		public String[]  getProjectsOnGateway(String gatewayName,String projectName) {
			
			String[]  projectsList = null ;
			
			int noOfProjects = 0, i=0;
			Datasource ds;
			Dataset resDS = null;
			projectsList = new String[1];
			projectsList[0] = "All Projects"; 
			
			SRConnection con = null;
					
			String sqlQuery = "";
			if(gatewayName != null && projectName != null){
			if(gatewayName.compareToIgnoreCase("All Gateways") == 0 && projectName.compareToIgnoreCase("All Projects") == 0)
			{
				sqlQuery = "SELECT distinct PROJECT_NAME from MOD_IA_AGGREGATES_PROJECTS"
						 + " where GATEWAY_ID in (SELECT GAN_SERVERNAME FROM mod_ia_monitored_gateways)";
			}
			else if(gatewayName.compareToIgnoreCase("All Gateways") != 0 && projectName.compareToIgnoreCase("All Projects") == 0)
			{
				sqlQuery = "SELECT distinct PROJECT_NAME from MOD_IA_AGGREGATES_PROJECTS where GATEWAY_ID = '"
						+ gatewayName + "';";
			}
			else if(gatewayName.compareToIgnoreCase("All Gateways") == 0 && projectName.compareToIgnoreCase("All Projects") != 0)
			{
				sqlQuery = "SELECT distinct PROJECT_NAME from MOD_IA_AGGREGATES_PROJECTS where PROJECT_NAME = '"
						+ projectName + "';";
			}
			else
			{
				sqlQuery = "SELECT distinct PROJECT_NAME from MOD_IA_AGGREGATES_PROJECTS where GATEWAY_ID = '"
						+ gatewayName + "' and PROJECT_NAME = '" + projectName + "';";
			}
//			log.error("getProjectsOnGateway: "+sqlQuery);
			
			
			try {
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				resDS = con.runQuery(sqlQuery);
				if(resDS != null && resDS.getRowCount() > 0)
				{
					noOfProjects = resDS.getRowCount();
					projectsList = new String[noOfProjects + 1];
					projectsList[0] = "All Projects"; //add the option for all projects
					for(i=0; i<noOfProjects; i++)
					{
						if(resDS.getValueAt(i,0) != null)
						{
							projectsList[i+1] = resDS.getValueAt(i,0).toString();
						}
					}
					
				}
				
				
			}
			catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getProjectsOnGateway : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
		}
			return projectsList;
		}
		
//		@Override
//		public boolean storeGANServersList(List<ServerId> serverIds) {
//			boolean status = true;
//			
//			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
//			SRConnection con = null;
//			
//			
//			String insertQ = "INSERT INTO MOD_IA_GATEWAYS(GAN_ServerId, GAN_ServerName) values ( ";
//			int i = 0;
//			
//			if(serverIds != null)
//			{
//				for (ServerId s : serverIds)
//				{
//					
//					if( i == 0)
//					{
//						insertQ = insertQ + "'" + s.getRootServerId() + "', '" + s.getServerName() + "') ";
//					}
//					else
//					{
//						insertQ =  insertQ + ", ( '" + s.getRootServerId() + "', '" + s.getServerName() + "') ";
//					}
//					i++;
//				}
//			
//				insertQ = insertQ + ";";
//				log.error("storeGANServersList  : insert q is - " + insertQ  );
//				try {
//					con = ds.getConnection();
//					
//					//first remove all entries , then add new ones
//					con.runUpdateQuery("DELETE FROM MOD_IA_GATEWAYS;");
//					//perform insert
//					con.runUpdateQuery(insertQ);
//				}
//				catch(Exception e)
//				{
//					e.printStackTrace();
//					status = false;
//				}
//			}
//			
//			return status;
//		}

		
		@Override
		public boolean storeGANServersList(List<ServerId> serverIds) {
			boolean status = true;
			
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			Dataset result = null;
			String selectQ = "";
			String insertQ = "INSERT INTO MOD_IA_GATEWAYS(GAN_ServerId, GAN_ServerName) values ( ";
			int i = 0;
			if(serverIds != null)
			{
				for (ServerId s : serverIds)
				{
					try {
						con = ds.getConnection();
						
						//first check if server exists 
						selectQ = "SELECT GAN_ServerId from mod_ia_gateways where GAN_ServerId = '" + s.getRootServerId() + "';";
						result = con.runQuery(selectQ);
						if(result == null || result.getRowCount() == 0)
						{
							//server does not exist so insert
							insertQ = "INSERT INTO MOD_IA_GATEWAYS(GAN_ServerId, GAN_ServerName, GAN_ServerState) values ('"
									+ s.getRootServerId() + "', '" + s.getServerName() + "', 'Connected');";
							
							con.runUpdateQuery(insertQ);
							log.error("insert MOD_IA_MONITORED_GATEWAYS server query : " + insertQ);
							insertQ = "INSERT INTO MOD_IA_MONITORED_GATEWAYS(GAN_ServerName) values ('"
									 + s.getServerName() + "');";
							log.error("insert MOD_IA_MONITORED_GATEWAYS server query : " + insertQ);
							con.runUpdateQuery(insertQ);
						}
						else
						{
							log.error("gateway record exists for : " + s.getServerName());
						}
						
					}
					catch(Exception e)
					{
						e.printStackTrace();
						status = false;
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("storeGANServersList : in con close exception.");
							
							e.printStackTrace();
						}
						
						}
					}
				}
			}
			else
			{
				log.error("storeGANServersList : no server ids");
			}
			return status;
		}
		

		@Override
		public Dataset retrieveHourlyAlarmSummary(String alarmDate, int hourNo) {
			Dataset retData = null;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			String selectQ = "SELECT * from mod_ia_hourly_alarms_counts where ALARM_HOUR >= " + hourNo + " and ALARM_DATE >= " + alarmDate;
			try {
				con = ds.getConnection();
				//perform insert
				retData = con.runQuery(selectQ);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("retrieveHourlyAlarmSummary : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return retData;
		}

		@Override
		public Dataset retrieveDailyAlarmSummary(String alarmDate) {
			Dataset retData = null;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			String selectQ = "SELECT * from mod_ia_daily_alarms_summary where ALARM_DATE >= " + alarmDate;
			try {
				con = ds.getConnection();
				
				retData = con.runQuery(selectQ);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("retrieveDailyAlarmSummary : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return retData;
		}

		@Override
		public Dataset retrieveDailySessions(String sessionDate) {
			Dataset retData = null;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			String selectQ = "SELECT * from mod_ia_daily_sessions where session_start >= " + sessionDate;
			try {
				con = ds.getConnection();
				
				retData = con.runQuery(selectQ);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("retrieveDailySessions : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return retData;
		}

		@Override
		public Dataset retrieveModIAClient(String sessionDate) {
			Dataset retData = null;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			String selectQ = "SELECT * from mod_ia_clients where start_timestamp >= " + sessionDate;
			try {
				con = ds.getConnection();
				retData = con.runQuery(selectQ);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			return retData;
		}

		@Override
		public Dataset retrieveModIABrowsers(String sessionDate) {
			Dataset retData = null;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			String selectQ = "SELECT * from mod_ia_browser_info where start_timestamp >= " + sessionDate;
			try {
				con = ds.getConnection();
				retData = con.runQuery(selectQ);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			return retData;
		}

		
		@Override
		public Dataset retrieveModIALocations() {
			Dataset retData = null;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			String selectQ = "SELECT * from mod_ia_location_info;";
			try {
				con = ds.getConnection();
				retData = con.runQuery(selectQ);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			return retData;
		}

		@Override
		public Dataset retrieveModIAScreenViews(String sessionDate) {
			Dataset retData = null;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			String selectQ = "SELECT * from mod_ia_screen_views where view_timestamp >= " + sessionDate;
			try {
				con = ds.getConnection();
				retData = con.runQuery(selectQ);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			return retData;
		}

		@Override
		public RealTimeData retrieveRealTimeData(String projectName,
				boolean allProjects) {
		RealTimeData retData = new RealTimeData();
		
		Dataset maxMin = this.getSevenDaysMaxMin(this.moduleDS + "", Constants.LAST_SEVEN_DAYS, projectName, allProjects);
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
		
		CurrentOverview cView = this.getCurrentOverview(projectName, allProjects);
		UsersOverviewInformation uInfo;
		List<UsersOverviewInformation> uInfoList = new ArrayList<UsersOverviewInformation>();
		//collect user info
		List<UserLocations> uList =cView.getUserLocations(); 
		int noOfUsers = uList.size();
		
		i = 0;
		for(i=0; i<noOfUsers; i++)
		{
			uInfo = new UsersOverviewInformation();
			uInfo = this.getUserInformation(Constants.TODAY, uList.get(i).getUserName(), projectName, allProjects, uList.get(i).getUserAuthProfile());
			if(uInfo != null)
			{
				uInfoList.add(uInfo);
			}
			
		}
		retData.setSevenDaysMax(maxVal);
		retData.setSevenDaysMin(minVal);
		retData.setActiveUsersData(this.getActiveUsersCounts(projectName, allProjects, Constants.TODAY));
		retData.setAlarmInfo(this.getAlarmsOverview(Constants.TODAY, projectName, allProjects));
		retData.setContentData(this.getNumberOfUsersPerScreenRealTime(projectName, allProjects));
		retData.setcOverview(cView);
		retData.setEngagementData(this.getEngagementInformation(projectName, allProjects, Constants.TODAY));
		retData.setNoOfActiveUsers(this.getNumberOfActiveUsers(Constants.TODAY,projectName, allProjects ));
		retData.setNoOfNewUsers(this.getNumberOfNewUsers(Constants.TODAY, projectName, allProjects));
		retData.setuOverview(uInfoList);
		
		return retData;
		}

		@Override
		public boolean storeClientsInformation(Dataset clientData) {
			
//			log.error("received data from agent.");
			return true;
		}

		/*
		 * This service is called from each Agent to send Audit summary data 
		 * COntroller stores the data in aggregate tables.
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#receiveSummaryOnScreenViewChange(com.vaspsolutions.analytics.common.SummaryOnScreenViewChange_Sync)
		 */
		@Override
		public boolean receiveSummaryOnScreenViewChange(SummaryOnScreenViewChange_Sync summary) {
			
			boolean retVAl = true;
			//log.error("receive summary service on controller is called");
			int totalRows = 0, i =0;
			String selQ= "";
			String insQ = "";
			String updateQ = "";
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			Dataset selectResult = null;
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			SimpleDateFormat sdDuration = new SimpleDateFormat("HH:mm:ss");
			//get data sets one by one and insert into the aggregates table.
			String agentGatewayID = summary.agentName;
			String dataFrom = "";
			String dataFromDate = "";
			if(this.isGatewayMonitored(agentGatewayID))
			{
				try {
							con = ds.getConnection();
							Dataset dailyOverview = summary.dailyOverview;
							if(dailyOverview != null)
							{
								totalRows = dailyOverview.getRowCount();
							//	log.error("daily overview rows : " + totalRows);
								if(totalRows > 0) //data actualy exists
								{
									//update if record exists else insert
									
									for(i=0; i<totalRows; i++)
									{
										selQ = "SELECT GATEWAY_ID from mod_ia_aggregates_daily_overview where"
												+ " OVERVIEW_DATE = '" + dailyOverview.getValueAt(i, "OVERVIEW_DATE").toString() + "'"
												+ " and GATEWAY_ID = '" + agentGatewayID + "' and PROJECT_NAME = '" + dailyOverview.getValueAt(i, "PROJECT_NAME") +"';";
										
										selectResult = con.runQuery(selQ);
										if(selectResult != null && selectResult.getRowCount() > 0)
										{
											//update
											updateQ = "UPDATE mod_ia_aggregates_daily_overview SET "
													+ "ACTIONS = " + dailyOverview.getValueAt(i, "ACTIONS") + ", "
													+ "ACTIONS_PER_SESSION = " + dailyOverview.getValueAt(i, "ACTIONS_PER_SESSION") + ", "
													+ "AVG_SESSION_DURATION = " + dailyOverview.getValueAt(i, "AVG_SESSION_DURATION") + ", "
													+ "BOUNCE_RATE = " + dailyOverview.getValueAt(i, "BOUNCE_RATE") + ", "
													+ "NEW_USERS = " + dailyOverview.getValueAt(i, "NEW_USERS") + ", "
													+ "SCREENS_PER_SESSION = " + dailyOverview.getValueAt(i, "SCREENS_PER_SESSION") + ", "
													+ "TOTAL_SCREENVIEWS = " + dailyOverview.getValueAt(i, "TOTAL_SCREENVIEWS") + ", "
													+ "TOTAL_SESSIONS = " + dailyOverview.getValueAt(i, "TOTAL_SESSIONS") + ", "
													+ "TOTAL_USERS = " + dailyOverview.getValueAt(i, "TOTAL_USERS")
													+ " WHERE OVERVIEW_DATE = '" + dailyOverview.getValueAt(i, "OVERVIEW_DATE").toString() + "'"
													+ " and GATEWAY_ID = '" + agentGatewayID + "' and PROJECT_NAME = '" 
													+ dailyOverview.getValueAt(i, "PROJECT_NAME") +"';";
											con.runUpdateQuery(updateQ);
										}
										else
										{
											insQ = "INSERT INTO mod_ia_aggregates_daily_overview (ACTIONS, ACTIONS_PER_SESSION, AVG_SESSION_DURATION,"
													+ "BOUNCE_RATE,GATEWAY_ID,NEW_USERS,OVERVIEW_DATE,SCREENS_PER_SESSION,"
													+ "TOTAL_SCREENVIEWS,TOTAL_SESSIONS,TOTAL_USERS, PROJECT_NAME) VALUES ("
													+ dailyOverview.getValueAt(i, "ACTIONS") + ", "
												+ dailyOverview.getValueAt(i, "ACTIONS_PER_SESSION") + "," 
												+ dailyOverview.getValueAt(i, "AVG_SESSION_DURATION") + ","
												+ dailyOverview.getValueAt(i, "BOUNCE_RATE") + ","
												+ "'" + agentGatewayID + "', "
												+ dailyOverview.getValueAt(i, "NEW_USERS") + ","
												+ "'" + dailyOverview.getValueAt(i, "OVERVIEW_DATE") + "',"
												+ dailyOverview.getValueAt(i, "SCREENS_PER_SESSION") + ","
												+ dailyOverview.getValueAt(i, "TOTAL_SCREENVIEWS") + ","
												+ dailyOverview.getValueAt(i, "TOTAL_SESSIONS") + ","
												+ dailyOverview.getValueAt(i, "TOTAL_USERS") + ","
												+ "'" + dailyOverview.getValueAt(i, "PROJECT_NAME") + "');"
												;
											
											con.runUpdateQuery(insQ);
										}
										
									}
									
									
									
									
								}
							}
							
							/*
							 * Insert data in hourly overview
							 */
							SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
							Dataset hourlyOverview = summary.hourlyOverview;
							if(hourlyOverview != null)
							{
								totalRows = hourlyOverview.getRowCount();
								if(totalRows > 0)
								{
									dataFrom = hourlyOverview.getValueAt(0, "OVERVIEW_DATE").toString();
									String hourFrom = hourlyOverview.getValueAt(0, "OVERVIEW_HOUR").toString();
									
								
									Date lastSyncDate = sdfDate.parse(dataFrom);
									Date todaysDate = new Date();
									
									
									if(todaysDate.compareTo(lastSyncDate) >= 0)
									{
										selQ = "DELETE FROM mod_ia_aggregates_hourly_overview where OVERVIEW_DATE >= '" + dataFrom + "' "
											+ " and OVERVIEW_HOUR >= " + hourFrom + " and GATEWAY_ID = '" + agentGatewayID + "';";
									}
									else
									{
										selQ = "DELETE FROM mod_ia_aggregates_hourly_overview where OVERVIEW_DATE >= '" + dataFrom + "' "
												+ " and GATEWAY_ID = '" + agentGatewayID + "';";
									}
									
											
									insQ = "INSERT INTO mod_ia_aggregates_hourly_overview (ACTIONS,AVG_SESSION_DURATION, BOUNCE_RATE, GATEWAY_ID,"
											+ " OVERVIEW_DATE, OVERVIEW_HOUR, SCREENS_PER_SESSION, TOTAL_SCREENVIEWS, TOTAL_SESSIONS,TOTAL_USERS,"
											+ " PROJECT_NAME)"
											+ " VALUES (";
									for(i=0; i<totalRows; i++)
									{
										if(i == 0)
										{
											insQ = insQ + hourlyOverview.getValueAt(i, "ACTIONS") + ", "
													+ hourlyOverview.getValueAt(i, "AVG_SESSION_DURATION") + ", "
													+ hourlyOverview.getValueAt(i, "BOUNCE_RATE") + ", "
													+ "'" + agentGatewayID + "', "
													+ "'" + hourlyOverview.getValueAt(i, "OVERVIEW_DATE") + "', "
													+ hourlyOverview.getValueAt(i, "OVERVIEW_HOUR") + ", "
													+ hourlyOverview.getValueAt(i, "SCREENS_PER_SESSION") + ", "
													+ hourlyOverview.getValueAt(i, "TOTAL_SCREENVIEWS") + ", "
													+ hourlyOverview.getValueAt(i, "TOTAL_SESSIONS") + ", "
													+ hourlyOverview.getValueAt(i, "TOTAL_USERS") + ", "
													+ "'" + hourlyOverview.getValueAt(i, "PROJECT_NAME") 
													+ "')";
										}
										else
										{
											insQ = insQ +  ",(" + hourlyOverview.getValueAt(i, "ACTIONS") + ", "
													+ hourlyOverview.getValueAt(i, "AVG_SESSION_DURATION") + ", "
													+ hourlyOverview.getValueAt(i, "BOUNCE_RATE") + ", "
													+ "'" + agentGatewayID + "', "
													+ "'" + hourlyOverview.getValueAt(i, "OVERVIEW_DATE") + "', "
													+ hourlyOverview.getValueAt(i, "OVERVIEW_HOUR") + ", "
													+ hourlyOverview.getValueAt(i, "SCREENS_PER_SESSION") + ", "
													+ hourlyOverview.getValueAt(i, "TOTAL_SCREENVIEWS") + ", "
													+ hourlyOverview.getValueAt(i, "TOTAL_SESSIONS") + ", "
													+ hourlyOverview.getValueAt(i, "TOTAL_USERS") + ", "
													+ "'" + hourlyOverview.getValueAt(i, "PROJECT_NAME") 
													+ "')";
										}
									}
									insQ = insQ + ";";
									//log.error("receiveSummaryOnScreenViewChange hourlyOverview insert= " + insQ);
									//log.error("receiveSummaryOnScreenViewChange hourlyOverview delete = " + selQ);
									con.runUpdateQuery(selQ);
									con.runUpdateQuery(insQ);
								}
							}
							
							/*
							 * Insert data in daily sessions
							 */
							Dataset dailySessions = summary.dailySessions;
							selectResult = null;
							if(dailySessions != null)
							{
								totalRows = dailySessions.getRowCount();
								if(totalRows > 0)
								{
									
								
									for(i=0; i<totalRows; i++)
									{
										selQ = "select GATEWAY_ID from mod_ia_aggregates_daily_sessions where SESSION_START = '" 
												+ dailySessions.getValueAt(i,"SESSION_START") + "'"
												+ " and GATEWAY_ID = '" + agentGatewayID + "'"
												+ " and PROJECT_NAME = '" + dailySessions.getValueAt(i,"PROJECT_NAME") + "'"
												+ " and USERNAME = '" + dailySessions.getValueAt(i,"USERNAME") + "'"
												+ " and SESSION_CONTEXT = " + dailySessions.getValueAt(i,"SESSION_CONTEXT")
												+ " and HOSTNAME = '" +  dailySessions.getValueAt(i,"HOSTNAME")+ "';";
										
										selectResult = con.runQuery(selQ);
										if(selectResult != null && selectResult.getRowCount() > 0)
										{
											updateQ = "UPDATE mod_ia_aggregates_daily_sessions SET"
													+ " NO_OF_ACTIONS = " + dailySessions.getValueAt(i,"NO_OF_ACTIONS") + ", "
													+ " NO_OF_SCREENS = " + dailySessions.getValueAt(i,"NO_OF_SCREENS") + ", "
													+ " SESSION_DURATION = '" + dailySessions.getValueAt(i,"SESSION_DURATION") + "', "
													+ " SESSION_END = '" + dailySessions.getValueAt(i,"SESSION_END") + "', "
													+ " SESSION_STATUS = '" + dailySessions.getValueAt(i,"SESSION_STATUS") + "' "
													+ " where SESSION_START = '" + dailySessions.getValueAt(i,"SESSION_START") + "'"
													+ " and GATEWAY_ID = '" + agentGatewayID + "'"
													+ " and PROJECT_NAME = '" + dailySessions.getValueAt(i,"PROJECT_NAME") + "'"
													+ " and USERNAME = '" + dailySessions.getValueAt(i,"USERNAME") + "'"
													+ " and SESSION_CONTEXT = " + dailySessions.getValueAt(i,"SESSION_CONTEXT")
													+ " and HOSTNAME = '" +  dailySessions.getValueAt(i,"HOSTNAME")+ "';";
											con.runUpdateQuery(updateQ);
										}
										else
										{
											insQ = "INSERT INTO mod_ia_aggregates_daily_sessions (GATEWAY_ID, NO_OF_ACTIONS, NO_OF_SCREENS, PROJECT_NAME, SESSION_DATE,"
													+ "SESSION_DURATION,SESSION_END,SESSION_START,USERNAME, SESSION_CONTEXT, HOSTNAME, SESSION_STATUS) VALUES ('" +
													 agentGatewayID + "', "
													+ dailySessions.getValueAt(i,"NO_OF_ACTIONS") + ", "
													+ dailySessions.getValueAt(i,"NO_OF_SCREENS") + ", "
													+ "'" + dailySessions.getValueAt(i,"PROJECT_NAME") + "', "
													+ "'" + dailySessions.getValueAt(i,"SESSION_DATE") + "', "
													+ "'" + dailySessions.getValueAt(i,"SESSION_DURATION") + "', "
													+ "'" + dailySessions.getValueAt(i,"SESSION_END") + "', "
													+ "'" + dailySessions.getValueAt(i,"SESSION_START") + "', "
													+ "'" + dailySessions.getValueAt(i,"USERNAME") + "', "									
													+ dailySessions.getValueAt(i,"SESSION_CONTEXT")
													+ ", '" + dailySessions.getValueAt(i,"HOSTNAME")
													+ "', '" + dailySessions.getValueAt(i,"SESSION_STATUS")
													+ "');";

											con.runUpdateQuery(insQ);
													
										}
									}
								}
							}
							/*
							 * Insert data in screen views
							 */
							Dataset screens = summary.screenViews;
							selectResult = null;
//							log.error("Screens: "+screens);
							if(screens != null)
							{
//								log.error("screens not null ");
								totalRows = screens.getRowCount();
								if(totalRows > 0)
								{
									
//									if(dataFrom.contains("."))
//									{
//										dataFrom = dataFrom.split(".")[0];
//									}
									dataFromDate = dataFrom.substring(0,10);
									selQ = "DELETE FROM mod_ia_aggregates_daily_screen_views where VIEW_TIMESTAMP >= '" + dataFrom + "'"
											+ " and GATEWAY_ID = '" + agentGatewayID + "';";
//									delQ = "DELETE FROM mod_ia_aggregates_daily_screen_views where CONVERT(DATE, VIEW_TIMESTAMP) >= '" + dataFromDate + "'"
//											+ " and GATEWAY_ID = '" + agentGatewayID + "';";
									
									for(i=0; i<totalRows; i++)
									{
										dataFrom = screens.getValueAt(i, "VIEW_TIMESTAMP").toString();
										selQ = "SELECT GATEWAY_ID FROM mod_ia_aggregates_daily_screen_views where "
												+ " VIEW_TIMESTAMP = '" + dataFrom + "'"
												+ " and GATEWAY_ID = '" + agentGatewayID 
												+ "' AND USERNAME = '"  + screens.getValueAt(i, "USERNAME") 
												+ "' AND ACTION = '" + screens.getValueAt(i, "ACTION")  
												+ "' AND SCREEN_NAME = '" + screens.getValueAt(i, "SCREEN_NAME")
												+ "' AND SCREEN_PATH = '" + screens.getValueAt(i, "SCREEN_PATH")
												+ "' AND SCREEN_TITLE = '" + screens.getValueAt(i, "SCREEN_TITLE")
												+ "' AND PROJECT = '" + screens.getValueAt(i, "PROJECT")
												+ "';";
										selectResult = con.runQuery(selQ);
										if(selectResult == null || selectResult.getRowCount() == 0)
										{
											insQ = "INSERT INTO mod_ia_aggregates_daily_screen_views (ACTION, GATEWAY_ID, PROJECT, SCREEN_NAME, SCREEN_PATH,"
													+ "SCREEN_TITLE, USERNAME, VIEW_TIMESTAMP) VALUES ("
													+ "'"+ screens.getValueAt(i, "ACTION") + "',"
													+ "'"+ agentGatewayID + "',"
													+ "'"+ screens.getValueAt(i, "PROJECT") + "',"
													+ "'"+ screens.getValueAt(i, "SCREEN_NAME") + "',"
													+ "'"+ screens.getValueAt(i, "SCREEN_PATH") + "',"
													+ "'"+ screens.getValueAt(i, "SCREEN_TITLE") + "',"
													+ "'"+ screens.getValueAt(i, "USERNAME") + "',"
													+ "'" + screens.getValueAt(i, "VIEW_TIMESTAMP") + "'"
													+ ");";
											con.runUpdateQuery(insQ);	
											
										}
									}
								}
							}
							/*
							 * Insert data in clients
							 */
							int isMobileIntVal = 0;
							Dataset clients = summary.clients;
							if(clients != null)
							{
								totalRows = clients.getRowCount();
								if(totalRows > 0)
								{
									
								
									for(i=0; i<totalRows; i++)
									{
										if( Boolean.parseBoolean(clients.getValueAt(i, "IS_MOBILE").toString()) == true)
										{
											isMobileIntVal = 1;
										}
										else
										{
											isMobileIntVal = 0;
										}
										dataFrom = clients.getValueAt(i, "START_TIMESTAMP").toString();
										selQ = "SELECT GATEWAY_ID FROM mod_ia_aggregates_clients where "
												+ " START_TIMESTAMP = '" + dataFrom + "'"
												+ " and GATEWAY_ID = '" + agentGatewayID + "'"
												+ " and BROWSER = '" +  clients.getValueAt(i, "BROWSER") + "'"
												+ " and HOST_EXTERNAL_IP = '" +  clients.getValueAt(i, "HOST_EXTERNAL_IP") + "'"
												+ " and HOST_INTERNAL_IP = '" +  clients.getValueAt(i, "HOST_INTERNAL_IP") + "'"
												+ " and HOSTNAME = '" +  clients.getValueAt(i, "HOSTNAME") + "'"
												+ " and IS_MOBILE = " +  isMobileIntVal
												+ " and OS_NAME = '" +  clients.getValueAt(i, "OS_NAME") + "'"
												+ " and OS_VERSION = '" +  clients.getValueAt(i, "OS_VERSION") + "'"
												+ " and PROJECT = '" +  clients.getValueAt(i, "PROJECT") + "'"
												+ " and SCREEN_RESOLUTION = '" +  clients.getValueAt(i, "SCREEN_RESOLUTION") + "'"
												+ " and START_TIMESTAMP = '" +  clients.getValueAt(i, "START_TIMESTAMP") + "'"
												+ " and USERNAME = '" +  clients.getValueAt(i, "USERNAME") + "'"
												+ " and CLIENT_CONTEXT = " +  clients.getValueAt(i, "CLIENT_CONTEXT") 
												+ ";";	
										selectResult = con.runQuery(selQ);
										if(selectResult == null || selectResult.getRowCount() == 0)
										{
											insQ = "INSERT INTO mod_ia_aggregates_clients (BROWSER, GATEWAY_ID, HOST_EXTERNAL_IP, HOST_INTERNAL_IP, HOSTNAME,"
													+ "IS_MOBILE, OS_NAME, OS_VERSION, PROJECT, SCREEN_RESOLUTION, START_TIMESTAMP, USERNAME, CLIENT_CONTEXT) VALUES ("
													+ "'" + clients.getValueAt(i, "BROWSER") + "',"
													+ "'" + agentGatewayID + "',"
													+ "'" + clients.getValueAt(i, "HOST_EXTERNAL_IP") + "',"
													+ "'" + clients.getValueAt(i, "HOST_INTERNAL_IP") + "',"
													+ "'" + clients.getValueAt(i, "HOSTNAME") + "',"
													+ isMobileIntVal + ","
													+ "'" + clients.getValueAt(i, "OS_NAME") + "',"
													+ "'" + clients.getValueAt(i, "OS_VERSION") + "',"
													+ "'" + clients.getValueAt(i, "PROJECT") + "',"
													+ "'" + clients.getValueAt(i, "SCREEN_RESOLUTION") + "',"
													+ "'" + clients.getValueAt(i, "START_TIMESTAMP") + "',"
													+ "'" + clients.getValueAt(i, "USERNAME") + "', "
													+ clients.getValueAt(i, "CLIENT_CONTEXT") 
													+ ");";
											con.runUpdateQuery(insQ);		
										}
									}
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						//	log.error("you are in catch");
							retVAl = false;
						}
						finally{
							//close the database connection 
							if(con!=null){
							try {
								con.close();
							} catch (SQLException e) {
								log.error("receiveSummaryOnScreenViewChange : in con close exception.");
								
								e.printStackTrace();
							}
							
							}
						}
				
			}
			//log.error("receiveSummaryOnScreenViewChange retVal: "+retVAl);
			return retVAl;
		}

		/*
		 * Retrieve data from following tables on screen view change 
		 * mod_ia_hourly_overview, mod_ia_daily_overview, mod_ia_daily_sessions, mod_ia_clients, mod_ia_screen_views
		 * then call service from Conroller to send this data
		 */
		@Override
		public boolean sendSummaryOnScreenViewChange() {
			
			boolean returnStatus = true;
			SummaryOnScreenViewChange_Sync _dataToSync = new SummaryOnScreenViewChange_Sync();
			
			String selQ = "" ; 
			
//			log.error("we are in sendSummaryOnScreenViewChange method");
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			LastSyncData _lstSync = LastSyncData.getInstance(ds);
			SRConnection con = null;
			Dataset retData;
			
			String newSyncDate = null;
			String newSyncHour = null;
			String newSyncClientTS = null;
			String newSyncScreenTS = null;
			String newSyncSessionsTS = null;
			String dataFrom = "";
			String dataFromDate = "";
			int no = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				con = ds.getConnection();
				
				//retrieve data from mod_ia_hourly_overview
				if(_lstSync.getLast_sync_date() != null && _lstSync.getLast_sync_hour() != null)
				{
					Date lastSyncDate = sdf.parse(_lstSync.getLast_sync_date());
					
					Date todaysDate = new Date();
					
					
					if(todaysDate.compareTo(lastSyncDate) >= 0)
					{
						selQ = "SELECT * from mod_ia_hourly_overview where OVERVIEW_DATE >= '" + _lstSync.getLast_sync_date ()
								+ "' and OVERVIEW_HOUR >= " + _lstSync.getLast_sync_hour() + ";";
					}
					else
					{
						selQ = "SELECT * from mod_ia_hourly_overview where OVERVIEW_DATE >= '" + _lstSync.getLast_sync_date ()
						//+ "' and OVERVIEW_HOUR = " + _lstSync.getLast_sync_hour() +
								+ "';";
					}
				}
				else
				{
					selQ = "SELECT * from mod_ia_hourly_overview";
				}
	
//				log.error("hourly overview query : " + selQ);
				retData = con.runQuery(selQ);
				_dataToSync.hourlyOverview = retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
						newSyncDate = retData.getValueAt(no -1 , 1 ).toString();
						newSyncHour = retData.getValueAt(no -1 , 2 ).toString();
					}
				}
				//retrieve data from mod_ia_daily_overview
				if(_lstSync.getLast_sync_date()  != null)
				{
					selQ = "SELECT * from mod_ia_daily_overview where OVERVIEW_DATE >= '" + _lstSync.getLast_sync_date()  
						+ "';";
				}
				else
				{
					selQ = "SELECT * from mod_ia_daily_overview";
				}
//				log.error("daily overview query : " + selQ);
				retData = con.runQuery(selQ);
				_dataToSync.dailyOverview = retData;
				
				//retrieve data from mod_ia_daily_sessions
				if(_lstSync.getLast_sync_sessions_timestamp()  != null)
				{
					selQ = "SELECT * from mod_ia_daily_sessions where SESSION_START >= '" + _lstSync.getLast_sync_sessions_timestamp()  
						+ "' order by session_status, session_start;";
				}
				else
				{
					selQ = "SELECT * from mod_ia_daily_sessions order by session_status, session_start";
				}
				retData = con.runQuery(selQ);
				_dataToSync.dailySessions = retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					boolean foundOpenSession = false;
					if(no > 0)
					{
						for(int i=0; i<no; i++)
						{
							if(retData.getValueAt(i, "SESSION_STATUS") != null && 
									retData.getValueAt(i, "SESSION_STATUS").toString().compareTo("O") == 0)
							{
								newSyncSessionsTS = retData.getValueAt(i , "SESSION_START").toString();
								foundOpenSession = true;
								break;
							}
						}
						if(!foundOpenSession)
							newSyncSessionsTS = retData.getValueAt(no - 1 , "SESSION_START").toString();
					}
					
				}
				//retrieve data from mod_ia_clients
				if(_lstSync.getLast_synch_client_start_timestamp() != null)
				{
					selQ = "SELECT * from mod_ia_clients where START_TIMESTAMP >= '" + _lstSync.getLast_synch_client_start_timestamp() 
						+ "';";
				}
				else
				{
					selQ = "SELECT * from mod_ia_clients";
				}
				//log.error("sendSummaryOnScreenViewChange clients: "+selQ);
				retData = con.runQuery(selQ);
				_dataToSync.clients= retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
					 newSyncClientTS = retData.getValueAt(no -1 , 9).toString();
					}
					
				}
				
			
				//retrieve data from mod_ia_screen_views
				if(_lstSync.getLast_sync_screen_timestamp() != null)
				{
					dataFrom =_lstSync.getLast_sync_screen_timestamp();
					
//					log.error("dataFrom : "+dataFrom+"dataFromDate : "+dataFromDate);
					dataFromDate = dataFrom.substring(0,10);
					selQ = "SELECT * from mod_ia_screen_views where  VIEW_TIMESTAMP >= '" + dataFrom
						+ "';";
				}
				else
				{
					selQ = "SELECT * from mod_ia_screen_views";
				}
				//log.error("sendSummaryOnScreenViewChange: "+selQ);
				retData = con.runQuery(selQ);
				
				_dataToSync.screenViews = retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
					 newSyncScreenTS = retData.getValueAt(no -1 , 4 ).toString();
					}
					
				}
				
			}
			catch(Exception e)
			{
				returnStatus = false;
				e.printStackTrace();
				
			}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("sendSummaryOnScreenViewChange : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			//When on Agent, call service on the controller. to send repsective data else just call local method 
			boolean status = false;
			_dataToSync.agentName = this.mycontext.getGatewayAreaNetworkManager().getServerAddress().getServerName();
			if(this.isAgent)
			{
				ServiceManager sm = this.mycontext.getGatewayAreaNetworkManager().getServiceManager();
				ServerId sid = new ServerId(this.controllerName);
				ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
		
				
			//if service is available
				if(s == ServiceState.Available)
				{
					//call the service 
//					log.error("Before service call in sendSummaryOnScreenViewChange");
					 status = sm.getService(sid, GetAnalyticsInformationService.class).get().receiveSummaryOnScreenViewChange(_dataToSync);
//					 log.error("after service call in sendSummaryOnScreenViewChange");
					//update all last_sync variables
					
					
				}
				else
				{
					returnStatus = false;
				}
			}
			else
			{
				status = this.receiveSummaryOnScreenViewChange(_dataToSync);
				//log.error("status after receive: "+status);
			}
			
			//check the status
			if(status == true)
			{
				_lstSync.setLast_sync_date(newSyncDate);
				_lstSync.setLast_sync_hour(newSyncHour);
				_lstSync.setLast_sync_screen_timestamp(newSyncScreenTS);
				_lstSync.setLast_synch_client_start_timestamp(newSyncClientTS);
				_lstSync.setLast_sync_sessions_timestamp(newSyncSessionsTS);
			}
			else
			{
				returnStatus = false;
			}
			return returnStatus;
		}

		/*
		 * THis service gets called from the Agent to send Alarms Summary data 
		 * On controller data is stored in the aggregate summary tables.
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#receiveAlarmsSummary(com.vaspsolutions.analytics.common.AlarmsSummary_Sync)
		 */
		@Override
		public boolean receiveAlarmsSummary(AlarmsSummary_Sync aSummary) {
			
			boolean retVAl = true;
			int totalRows = 0, i =0;
			String selQ= "";
			String insQ = "";
			String updateQ = "";
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			Dataset results = null;
			
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			//get data sets one by one and insert into the aggregates table.
			String agentGatewayID = aSummary.agentName;
			
			String dataFrom = "";
			if(this.isGatewayMonitored(agentGatewayID))
			{
				try {
					con = ds.getConnection();
					//store in daily aalrms summary
					Dataset dailyAlarmsSummary = aSummary.dailyAlarmsSummary;
					if(dailyAlarmsSummary != null)
					{
						totalRows = dailyAlarmsSummary.getRowCount();
						if(totalRows > 0) //data actualy exists
						{
							//first delete , then insert
							
							
							
							
							for(i=0; i<totalRows; i++)
							{
								dataFrom = dailyAlarmsSummary.getValueAt(i, "ALARM_DATE").toString();
								selQ = "SELECT ALARM_NAME FROM mod_ia_aggregates_daily_alarms_summary WHERE "
										+ "ALARM_DATE = '" + dataFrom + "' "
										+ "and ALARM_NAME = '" + dailyAlarmsSummary.getValueAt(i, "ALARM_NAME") + "'"
										+ "and ALARM_PRIORITY = '" + dailyAlarmsSummary.getValueAt(i, "ALARM_PRIORITY") + "'"
										+ ";";								
								results = con.runQuery(selQ);
								if(results != null && results.getRowCount() > 0)
								{
									updateQ = "UPDATE mod_ia_aggregates_daily_alarms_summary SET "
											+ " ALARM_NAME = '" + dailyAlarmsSummary.getValueAt(i, "ALARM_NAME") + "' , "
											+ " ALARMS_COUNT = " + dailyAlarmsSummary.getValueAt(i, "ALARMS_COUNT") + ", ";
										updateQ = updateQ + " AVG_TIME_TO_ACK = ";		
										if(dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_ACK") == null)
										{
											updateQ = updateQ + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_ACK") + " , "; 
										}
										else
										{
											updateQ = updateQ +"'" + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_ACK") + "', ";
										}
										updateQ = updateQ + " AVG_TIME_TO_CLEAR = ";
										if(dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_CLEAR") == null)
										{
											updateQ = updateQ + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_CLEAR") + " , "; 
										}
										else
										{
											updateQ = updateQ + "'" + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_CLEAR") + "', ";
										}
										
										updateQ = updateQ + " TOTAL_ACTIVE_TIME = ";
										if(dailyAlarmsSummary.getValueAt(i, "TOTAL_ACTIVE_TIME") == null)
										{
											updateQ = updateQ + dailyAlarmsSummary.getValueAt(i, "TOTAL_ACTIVE_TIME") ;
										}
										else
										{
											updateQ = updateQ + "'" + dailyAlarmsSummary.getValueAt(i, "TOTAL_ACTIVE_TIME") +  "'"
											;
										}
										
										updateQ = updateQ	+ " WHERE "
										+ "ALARM_DATE = '" + dataFrom + "' "
										+ "and ALARM_NAME = '" + dailyAlarmsSummary.getValueAt(i, "ALARM_NAME") + "'"
										+ "and ALARM_PRIORITY = '" + dailyAlarmsSummary.getValueAt(i, "ALARM_PRIORITY") + "'"
										+ ";";
										con.runUpdateQuery(updateQ);
								}
								else
								{
									insQ = "INSERT INTO mod_ia_aggregates_daily_alarms_summary (ALARM_DATE, ALARM_NAME, ALARM_PRIORITY, ALARMS_COUNT,"
											+ "AVG_TIME_TO_ACK, AVG_TIME_TO_CLEAR, GATEWAY_ID, TOTAL_ACTIVE_TIME) VALUES ( ";
									if(dailyAlarmsSummary.getValueAt(i, "ALARM_DATE") == null)
									{
										insQ = insQ 
												+  "'" +dailyAlarmsSummary.getValueAt(i, "ALARM_DATE") + "', ";
									}
									else
									{
										insQ = insQ  
												+  "'" + dailyAlarmsSummary.getValueAt(i, "ALARM_DATE").toString() + "', ";
												//sd.format(dailyAlarmsSummary.getValueAt(i, "ALARM_DATE").toString()) + "', ";
									}
									
											insQ = insQ + "'" + dailyAlarmsSummary.getValueAt(i, "ALARM_NAME") + "' ," 
											+ "'" + dailyAlarmsSummary.getValueAt(i, "ALARM_PRIORITY") + "' ,"
											+ dailyAlarmsSummary.getValueAt(i, "ALARMS_COUNT") + ",";
											if(dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_ACK") == null)
											{
												insQ = insQ + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_ACK") + " ,"; 
											}
											else
											{
												insQ = insQ +"'" + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_ACK") + "',";
											}
											
											if(dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_CLEAR") == null)
											{
												insQ = insQ + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_CLEAR") + " ,"; 
											}
											else
											{
												insQ = insQ + "'" + dailyAlarmsSummary.getValueAt(i, "AVG_TIME_TO_CLEAR") + "',";
											}
											
											insQ = insQ +  "'" + agentGatewayID + "', ";
											
											if(dailyAlarmsSummary.getValueAt(i, "TOTAL_ACTIVE_TIME") == null)
											{
												insQ = insQ + dailyAlarmsSummary.getValueAt(i, "TOTAL_ACTIVE_TIME") +  ");";
											}
											else
											{
												insQ = insQ + "'" + dailyAlarmsSummary.getValueAt(i, "TOTAL_ACTIVE_TIME") +  "');"
												;
											}
											con.runUpdateQuery(insQ);
								}
							}
							
							
							
							
						}
					}
					SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
					//store in hourly alarms counts
					Dataset hourlyAlarmsCounts = aSummary.hourlyAlarmCounts;
					if(hourlyAlarmsCounts != null)
					{
						totalRows = hourlyAlarmsCounts.getRowCount();
						if(totalRows > 0) //data actualy exists
						{
							
							for(i=0; i<totalRows; i++)
							{
								dataFrom = hourlyAlarmsCounts.getValueAt(i, "ALARM_DATE").toString();
								String hour = hourlyAlarmsCounts.getValueAt(i, "ALARM_HOUR").toString();
								selQ = "SELECT GATEWAY_ID FROM mod_ia_aggregates_hourly_alarms_counts "
										+ " WHERE ALARM_DATE = '" + dataFrom + "' "
										+ " and ALARM_HOUR = " + hour 
										+ " and GATEWAY_ID = '" + agentGatewayID + "'"
										+ " and PRIORITY = '" + hourlyAlarmsCounts.getValueAt(i, "PRIORITY") +  "';";
								results = con.runQuery(selQ);
								if(results != null && results.getRowCount() >0)
								{
									updateQ = " UPDATE mod_ia_aggregates_hourly_alarms_counts SET"
											+ " ALARMS_COUNT = " + hourlyAlarmsCounts.getValueAt(i, "ALARMS_COUNT") 
											+ " WHERE ALARM_DATE = '" + dataFrom + "' "
											+ " and ALARM_HOUR = " + hour 
											+ " and GATEWAY_ID = '" + agentGatewayID + "'"
											+ " and PRIORITY = '" + hourlyAlarmsCounts.getValueAt(i, "PRIORITY") +  "';";
									con.runUpdateQuery(updateQ);
								}
								else
								{
									insQ = "INSERT INTO mod_ia_aggregates_hourly_alarms_counts ( ALARM_DATE, ALARM_HOUR, ALARMS_COUNT, GATEWAY_ID, "
											+ " PRIORITY) VALUES ( "
											+  "'" + hourlyAlarmsCounts.getValueAt(i, "ALARM_DATE") + "', "
											+  hourlyAlarmsCounts.getValueAt(i, "ALARM_HOUR") + " ," 
											+  hourlyAlarmsCounts.getValueAt(i, "ALARMS_COUNT") + " ,"
											+ "'" + agentGatewayID + "', "
											+ "'" + hourlyAlarmsCounts.getValueAt(i, "PRIORITY") +  "');"	
											;
									con.runUpdateQuery(insQ);
								}
							}
							
							
						}
					}
				}
				catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retVAl = false;
				}
				finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("receiveAlarmsSummary : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
			}
			return retVAl;
		}

		/*
		 * This function retrieves the data from alarm summary tables depending of last sync time 
		 * calls service on controller to send Alarms data
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#sendAlarmsSummary()
		 */
		@Override
		public boolean sendAlarmsSummary() {
			
			boolean returnStatus = true;
			AlarmsSummary_Sync aSummary = new AlarmsSummary_Sync();
			String selQ = "" ; 
			
			
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			LastSyncData _lstSync = LastSyncData.getInstance(ds);
			SRConnection con = null;
			Dataset retData;
			
			String newSyncHourlyDate = null;
			String newSyncHour = null;
			String newSyncDailyDate = null;
			int no = 0;
			try {
				con = ds.getConnection();
				
				//retrieve data from mod_ia_hourly_alarm_counts
				if(_lstSync.getLast_sync_hourly_alarms_date() != null && _lstSync.getLast_sync_hourly_alarms_hour() != null)
				{
					selQ = "SELECT * from mod_ia_hourly_alarms_counts where ALARM_DATE >= '"
							+ _lstSync.getLast_sync_hourly_alarms_date() + "' "
							+ " and ALARM_DATE >= '" + this.installDate.substring(0, 10) + "'"
//							+ "and ALARM_HOUR >= " + _lstSync.getLast_sync_hourly_alarms_hour()
							+ ";";
				}
				else
				{
					selQ = "SELECT * from mod_ia_hourly_alarms_counts"
							+ " where ALARM_DATE >= '"+ this.installDate.substring(0, 10) + "'"
							+ " ;";
				}
				
				retData = con.runQuery(selQ);
				aSummary.hourlyAlarmCounts = retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
						newSyncHourlyDate = retData.getValueAt(no -1 , 1 ).toString();
						newSyncHour = retData.getValueAt(no -1 , 2 ).toString();
					}
				}
				//retrieve data from mod_ia_daily_alarms_summary
				
				if(_lstSync.getLast_sync_daily_alarms_date() != null)
				{
					selQ = "SELECT * from mod_ia_daily_alarms_summary where ALARM_DATE >= '"
//							+ _lstSync.getLast_sync_daily_alarms_date() 
							+ newSyncHourlyDate + "'"
							+ " and ALARM_DATE >= '" + this.installDate.substring(0, 10)
							+ "';";
				}
				else
				{
					 selQ = "SELECT * from mod_ia_daily_alarms_summary"
							 + " where ALARM_DATE >= '" + this.installDate.substring(0, 10)
					 		+ "';";
					//selQ = "SELECT ALARM_DATE, ID, ALARM_NAME, ALARM_PRIORITY, ALARMS_COUNT, AVG_TIME_TO_CLEAR, time_to_sec(AVG_TIME_TO_ACK) as AVG_TIME_TO_ACK, TOTAL_ACTIVE_TIME FROM mod_ia_daily_alarms_summary;";
					
				}
				//log.error("sendAlarmsSummary: "+selQ);
				retData = con.runQuery(selQ);
				aSummary.dailyAlarmsSummary = retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
						newSyncDailyDate = retData.getValueAt(no -1 , "ALARM_DATE" ).toString();
						
					}
				}
			}
			catch(Exception e)
			{
				returnStatus = false;
				e.printStackTrace();
				
			}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("sendAlarmsSummary : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			boolean status = false;
			
			//call service only when on Agent , else make a local call
			aSummary.agentName = this.mycontext.getGatewayAreaNetworkManager().getServerAddress().getServerName();
			if(this.isAgent)
			{
				ServiceManager sm = this.mycontext.getGatewayAreaNetworkManager().getServiceManager();
				ServerId sid = new ServerId(this.controllerName);
				ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
			
				
				//if service is available
				if(s == ServiceState.Available)
				{
					//call the service 
					status = sm.getService(sid, GetAnalyticsInformationService.class).get().receiveAlarmsSummary(aSummary);
					
				}
				else
				{
					status = false;
				}
			}
			else
			{
				status = this.receiveAlarmsSummary(aSummary);
			}
			
			//update all last_sync variables
			if(status == true)
			{
				_lstSync.setLast_sync_daily_alarms_date(newSyncDailyDate);
				_lstSync.setLast_sync_hourly_alarms_date(newSyncHourlyDate);
				_lstSync.setLast_sync_hourly_alarms_hour(newSyncHour);
					
			}
			else
			{
				returnStatus = false;
			}
				
			return returnStatus;
		}

		/*
		 * runs on controller to update data in local database
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#receiveBrowsers(java.lang.String, com.inductiveautomation.ignition.common.Dataset)
		 */
		@Override
		public boolean receiveBrowsers(String agentGatewayID, Dataset browserData) {
			boolean retVAl = true;
			int totalRows = 0, i =0;
			String selQ= "";
			String insQ = "";
			
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			Dataset dataSet = null;
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//get data sets one by one and insert into the aggregates table.
			
			if(this.isGatewayMonitored(agentGatewayID))
			{
				String dataFrom = "";
				String dataFromDate = "";
				try {
					
					//store in daily aalrms summary
					
					if(browserData != null)
					{
						totalRows = browserData.getRowCount();
						if(totalRows > 0) //data actualy exists
						{
							//first delete , then insert
							
							
							dataFrom = browserData.getValueAt(0, "TIMESTAMP").toString();
							dataFromDate = dataFrom.substring(0, 10);
					//		log.error("dataFrom : " + dataFrom + " dataFromDate : " + dataFromDate);
//							delQ = "DELETE FROM mod_ia_aggregates_browser_info WHERE TIMESTAMP >= '" + dataFrom + "' and GATEWAY_ID = '"
//									+ agentGatewayID + "';";
//							
////							delQ = "DELETE FROM mod_ia_aggregates_browser_info WHERE CONVERT(date, TIMESTAMP) >= '" + dataFromDate + "' and GATEWAY_ID = '"
////									+ agentGatewayID + "';";
							
							
							
							con = ds.getConnection();
							for(i=0; i<totalRows; i++)
							{
								selQ = "SELECT BROWSER_NAME FROM MOD_IA_AGGREGATES_BROWSER_INFO WHERE "
										+ " GATEWAY_ID = '" + agentGatewayID + "' "
										+ " AND TIMESTAMP = '" + sd.format(browserData.getValueAt(i, "TIMESTAMP"))  + "' "
										+ " AND BROWSER_NAME = '" + browserData.getValueAt(i, "BROWSER_NAME") + "' "
										+ " AND BROWSER_VERSION = " + browserData.getValueAt(i, "BROWSER_VERSION") 
										+ " AND IP_ADDRESS = '" + browserData.getValueAt(i, "IP_ADDRESS") + "' "
										+ "" ;
								
								//log.error("receiveBrowsers : sql q " + selQ);
								dataSet = con.runQuery(selQ);
								if(dataSet == null || dataSet.getRowCount() == 0)
								{
										insQ = "INSERT INTO mod_ia_aggregates_browser_info (BROWSER_NAME, BROWSER_VERSION, GATEWAY_ID, IP_ADDRESS,"
											+ "TIMESTAMP) VALUES ( " 
											+ "'" + browserData.getValueAt(i, "BROWSER_NAME") + "' ," 
											+ browserData.getValueAt(i, "BROWSER_VERSION") + " ,"
											+ "'" + agentGatewayID + "', "
											+ "'" + browserData.getValueAt(i, "IP_ADDRESS") + "',"
											+ "'" + sd.format(browserData.getValueAt(i, "TIMESTAMP")) + "' )"
											;
										con.runUpdateQuery(insQ);
								}
							}
							
							
							
						}
					}
					
					
				}
				catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retVAl = false;
				}
				finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("receiveBrowsers : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
			}
			return retVAl;
		}

		@Override
		public boolean receiveActiveUsersSummary(ActiveUsersData_Sync auSummary) {
			boolean retVAl = true;
			int totalRows = 0, i =0;
			String delQ= "";
			String selQ = "";
			String insQ = "";
			
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
			//get data sets one by one and insert into the aggregates table.
			String agentGatewayID = auSummary.agentName;
//			log.error("receiveActiveUsersSummary agentGatewayID "+agentGatewayID);
			String dataFrom = "";
			Dataset dataSet = null;
			String updateQ = "";
			if(this.isGatewayMonitored(agentGatewayID))
			{
//				log.error("isGatewayMonitored status: "+ this.isGatewayMonitored(agentGatewayID));
				try {
					con = ds.getConnection();
//					log.error("Connection in receiveActiveUsersSummary done ");
					//store in daily active users
					Dataset dailyActiveUsers = auSummary.dailyActiveUsers;
				//	log.error("dailyActiveUsers is null and dataset: " +dailyActiveUsers);
					if(dailyActiveUsers != null)
					{
//						log.error("dailyActiveUsers is not null");
						totalRows = dailyActiveUsers.getRowCount();
						if(totalRows > 0) //data actualy exists
						{
							//first delete , then insert
//							log.error("Total rows of dailyActiveUsers : "+totalRows);
							
							dataFrom = dailyActiveUsers.getValueAt(0, "SUMMARY_DATE").toString();
							delQ = "DELETE FROM mod_ia_aggregates_daily_active_users WHERE SUMMARY_DATE >= '" + dataFrom + "' and GATEWAY_ID = '"
									+ agentGatewayID + "';";
							
							for(i=0; i<totalRows; i++)
							{
							
								insQ = "";
								insQ = "INSERT INTO mod_ia_aggregates_daily_active_users (FOURTEEN_DAY_ACTIVE_USERS, GATEWAY_ID, "
										+ " GREATER_ACTIVE_USERS, ONE_DAY_ACTIVE_USERS, PROJECT_NAME, SEVEN_DAY_ACTIVE_USERS, SUMMARY_DATE) VALUES ( ";
//								log.error("No of row of dailyActiveUsers : "+i);
								/**** ORIGINAL CODE
								if(i == 0)
								{
									insQ = insQ 
										+  dailyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
										+ "'" + agentGatewayID + "', "
										+  dailyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
										+  dailyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
										+  "'" + dailyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
										+  dailyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
										+  "'" + dailyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "' " 
										+ ") "
										;
//									log.error("when i = 0");
//									log.error("receiveActiveUsersSummary = " + insQ);
								}
								else
								{
									insQ = insQ +  ",(" 
											+  dailyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
											+ "'" + agentGatewayID + "', "
											+  dailyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
											+  dailyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
											+  "'" + dailyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
											+  dailyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
											+  "'" + dailyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "' " 
											+  ")"
											;
//									log.error("when i : "+i);
//									log.error("receiveActiveUsersSummary = " + insQ);
								}
							}
							insQ = insQ + 
									";";
//							log.error("receiveActiveUsersSummary dailyActiveUsers = " + insQ);
							
							con.runUpdateQuery(delQ);
							con.runUpdateQuery(insQ); 
							
							
							ORIGINAL CODE
							*/
							
//								 log.error("receiveActiveUsersSummary : select q is " + "SELECT PROJECT_NAME FROM MOD_IA_AGGREGATES_DAILY_ACTIVE_USERS WHERE GATEWAY_ID = '" + agentGatewayID + "' AND "
//											+ " SUMMARY_DATE = '" + dailyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "' AND PROJECT_NAME = '" + dailyActiveUsers.getValueAt(i, "PROJECT_NAME") + "';"  );
								dataSet = con.runQuery("SELECT PROJECT_NAME FROM MOD_IA_AGGREGATES_DAILY_ACTIVE_USERS WHERE GATEWAY_ID = '" + agentGatewayID + "' AND "
									+ " SUMMARY_DATE = '" + dailyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "' AND PROJECT_NAME = '" + dailyActiveUsers.getValueAt(i, "PROJECT_NAME") + "';"  );
							 
						
							 if(dataSet != null && dataSet.getRowCount() > 0)
							 {
								 //update
								 
								 //log.error("in update ");
								 updateQ = "UPDATE MOD_IA_AGGREGATES_DAILY_ACTIVE_USERS "
								 		+ " SET FOURTEEN_DAY_ACTIVE_USERS = " + dailyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS")  + " ,"
								 		+ " GREATER_ACTIVE_USERS = " + dailyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS")  + " ,"
								 		+ " ONE_DAY_ACTIVE_USERS = " + dailyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS")  + " ,"
								 		+ " SEVEN_DAY_ACTIVE_USERS = " + dailyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS")  
								 		+ " WHERE GATEWAY_ID = '" + agentGatewayID + "' AND "
								 		+ " SUMMARY_DATE = '" +  dailyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "' AND PROJECT_NAME = '" + dailyActiveUsers.getValueAt(i, "PROJECT_NAME") + "';"; 
								 //log.error("in update daily active ysers q : " + updateQ);
								 	con.runUpdateQuery(updateQ);	
							 }
							 else
							 {
								 //insert 
								
								 insQ = insQ 
											+  dailyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
											+ "'" + agentGatewayID + "', "
											+  dailyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
											+  dailyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
											+  "'" + dailyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
											+  dailyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
											+  "'" + dailyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "' " 
											+ ") "
											;
								// log.error("in insert daily active : " + insQ);
								 con.runUpdateQuery(insQ);
							 }
							
						}
					}
					}
					
					SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
					//store in hourly active users
					Dataset hourlyActiveUsers = auSummary.hourlyActiveUsers;
					if(hourlyActiveUsers != null)
					{
						totalRows = hourlyActiveUsers.getRowCount();
						if(totalRows > 0) //data actualy exists
						{
							//first delete , then insert
//							log.error("Total rows of hourlyActiveUsers : "+totalRows);

							
							dataFrom = hourlyActiveUsers.getValueAt(0, "SUMMARY_DATE").toString();
							String dataHour = hourlyActiveUsers.getValueAt(0, "SUMMARY_HOUR").toString();
							
							Date lastSyncDate = sdfDate.parse(dataFrom);
							Date todaysDate = new Date();
							
							
							
							for(i=0; i<totalRows; i++)
							{
							/*	if(i == 0)
								{
									insQ = insQ 
										+  hourlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
										+ "'" + agentGatewayID + "', "
										+  hourlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
										+  hourlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
										+  "'" + hourlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
										+  hourlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
										+  "'" +hourlyActiveUsers.getValueAt(i, "SUMMARY_DATE")  + "', "
										+  hourlyActiveUsers.getValueAt(i, "SUMMARY_HOUR")
										+ ") "
										;
//									log.error("when i : "+i);
//									log.error("receiveActiveUsersSummary = " + insQ);
								}
								else
								{
									insQ = insQ +  ",(" 
											+  hourlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
											+ "'" + agentGatewayID + "', "
											+  hourlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
											+  hourlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
											+  "'" + hourlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
											+  hourlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
											+  "'" +hourlyActiveUsers.getValueAt(i, "SUMMARY_DATE")  + "', "
											+  hourlyActiveUsers.getValueAt(i, "SUMMARY_HOUR")
											+  ")"
											;
//									log.error("when i : "+i);
//									log.error("receiveActiveUsersSummary = " + insQ);
								} */
								
								
								selQ = "SELECT PROJECT_NAME FROM mod_ia_aggregates_hourly_active_users WHERE SUMMARY_DATE = '" + hourlyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "' and GATEWAY_ID = '"
										+ agentGatewayID + "' and SUMMARY_HOUR = " + hourlyActiveUsers.getValueAt(i, "SUMMARY_HOUR") 
										+ " and PROJECT_NAME = '" + hourlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "';";
								
								dataSet = con.runQuery(selQ);
								
								if(dataSet != null && dataSet.getRowCount() > 0)
								{
									
									 updateQ = "UPDATE mod_ia_aggregates_hourly_active_users "
									 		+ " SET FOURTEEN_DAY_ACTIVE_USERS = " + hourlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS")  + " ,"
									 		+ " GREATER_ACTIVE_USERS = " + hourlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS")  + " ,"
									 		+ " ONE_DAY_ACTIVE_USERS = " + hourlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS")  + " ,"
									 		+ " SEVEN_DAY_ACTIVE_USERS = " + hourlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS")  
									 		+ " WHERE GATEWAY_ID = '" + agentGatewayID + "' AND "
									 		+ " SUMMARY_DATE = '" + hourlyActiveUsers.getValueAt(i, "SUMMARY_DATE") 
									 		+ "' and SUMMARY_HOUR = " + hourlyActiveUsers.getValueAt(i, "SUMMARY_HOUR") 
									 		+ " AND PROJECT_NAME = '" + hourlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "';"; 
									 //log.error("in update hourly active ysers q : " + updateQ);
									 	con.runUpdateQuery(updateQ);	
								}
								else
								{
									
									 insQ = "INSERT INTO mod_ia_aggregates_hourly_active_users (FOURTEEN_DAY_ACTIVE_USERS, GATEWAY_ID,"
												+ " GREATER_ACTIVE_USERS, ONE_DAY_ACTIVE_USERS, PROJECT_NAME, SEVEN_DAY_ACTIVE_USERS,"
												+ " SUMMARY_DATE, SUMMARY_HOUR) VALUES ( "
												+  hourlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
												+ "'" + agentGatewayID + "', "
												+  hourlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
												+  hourlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
												+  "'" + hourlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
												+  hourlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
												+  "'" + hourlyActiveUsers.getValueAt(i, "SUMMARY_DATE") + "', " 
												+  hourlyActiveUsers.getValueAt(i, "SUMMARY_HOUR")
												+ "); "
												;
									// log.error("in insert hourly active ysers q : " + insQ);
									 con.runUpdateQuery(insQ);
								}
							}
//							insQ = insQ + 
//									";";
////							log.error("receiveActiveUsersSummary hourlyActiveUsers = " + insQ);;
//							con.runUpdateQuery(delQ);
//							con.runUpdateQuery(insQ);
							
							
						}
					}
					
					//store in monthly active users
					Dataset monthlyActiveUsers = auSummary.monthlyActiveUsers;
					if(monthlyActiveUsers != null)
					{
						totalRows = monthlyActiveUsers.getRowCount();
						if(totalRows > 0) //data actualy exists
						{
							
							
							dataFrom = monthlyActiveUsers.getValueAt(0, "MONTH_NO").toString();
							String dataHour = monthlyActiveUsers.getValueAt(0, "YEAR").toString();
//							delQ = "DELETE FROM mod_ia_aggregates_monthly_active_users WHERE MONTH_NO >= '" + dataFrom + "' and GATEWAY_ID = '"
//									+ agentGatewayID + "' and YEAR >= " + dataHour + ";";
							
							for(i=0; i<totalRows; i++)
							{
//								if(i == 0)
//								{
//									insQ = insQ 
//										+  monthlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
//										+ "'" + agentGatewayID + "', "
//										+  monthlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
//										+  monthlyActiveUsers.getValueAt(i, "MONTH_NO") + ", "
//										+  monthlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
//										+  "'" + monthlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
//										+  monthlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
//										+  monthlyActiveUsers.getValueAt(i, "YEAR")
//										+ ") "
//										;
//								}
//								else
//								{
//									insQ = insQ +  ",(" 
//											+  monthlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
//											+ "'" + agentGatewayID + "', "
//											+  monthlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
//											+  monthlyActiveUsers.getValueAt(i, "MONTH_NO") + ", "
//											+  monthlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
//											+  "'" + monthlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
//											+  monthlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
//											+  monthlyActiveUsers.getValueAt(i, "YEAR")
//											+  ")"
//											;
//								}
								
								selQ = "SELECT PROJECT_NAME FROM mod_ia_aggregates_monthly_active_users WHERE MONTH_NO = " + monthlyActiveUsers.getValueAt(i, "MONTH_NO") + " and GATEWAY_ID = '"
										+ agentGatewayID + "' and YEAR = " + monthlyActiveUsers.getValueAt(i, "YEAR") 
										+ " and PROJECT_NAME = '" + monthlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "';";
								
								dataSet = con.runQuery(selQ);
								
								if(dataSet != null && dataSet.getRowCount() > 0)
								{
									//log.error("in update ");
									 updateQ = "UPDATE mod_ia_aggregates_monthly_active_users "
									 		+ " SET FOURTEEN_DAY_ACTIVE_USERS = " + monthlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS")  + " ,"
									 		+ " GREATER_ACTIVE_USERS = " + monthlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS")  + " ,"
									 		+ " ONE_DAY_ACTIVE_USERS = " + monthlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS")  + " ,"
									 		+ " SEVEN_DAY_ACTIVE_USERS = " + monthlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS")  
									 		+ " WHERE GATEWAY_ID = '" + agentGatewayID + "' AND "
									 		+ " MONTH_NO = " + monthlyActiveUsers.getValueAt(i, "MONTH_NO") 
									 		+ " and YEAR = " + monthlyActiveUsers.getValueAt(i, "YEAR") 
									 		+ " AND PROJECT_NAME = '" + monthlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "';"; 
								//	 log.error("in update monthly active ysers q : " + updateQ);	
									 	con.runUpdateQuery(updateQ);	
								}
								else
								{
									insQ = "INSERT INTO mod_ia_aggregates_monthly_active_users (FOURTEEN_DAY_ACTIVE_USERS, GATEWAY_ID, "
										+ " GREATER_ACTIVE_USERS, MONTH_NO, ONE_DAY_ACTIVE_USERS, PROJECT_NAME,SEVEN_DAY_ACTIVE_USERS,"
										+ " YEAR) VALUES ( "
										+  monthlyActiveUsers.getValueAt(i, "FOURTEEN_DAY_ACTIVE_USERS") + ", "
										+ "'" + agentGatewayID + "', "
										+  monthlyActiveUsers.getValueAt(i, "GREATER_ACTIVE_USERS") + ", "
										+  monthlyActiveUsers.getValueAt(i, "MONTH_NO") + ", "
										+  monthlyActiveUsers.getValueAt(i, "ONE_DAY_ACTIVE_USERS") + ", "
										+  "'" + monthlyActiveUsers.getValueAt(i, "PROJECT_NAME") + "', "
										+  monthlyActiveUsers.getValueAt(i, "SEVEN_DAY_ACTIVE_USERS") + ", "
										+  monthlyActiveUsers.getValueAt(i, "YEAR")
										+  ")"
										;
									// log.error("in insert monthly active ysers q : " + insQ);
							 con.runUpdateQuery(insQ);
								}
							}
//							insQ = insQ + 
//									";";
////							log.error("receiveAtiveUsersSummary monthlyActiveUsers insq = " + insQ);
//							con.runUpdateQuery(delQ);
//							con.runUpdateQuery(insQ);
							
							
						}
					}
					
					
				}catch(Exception e){
					log.error("receiveAtiveUsersSummary exception is : " + e);
				}
				finally{
					if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("receiveAtiveUsersSummary : in con close exception.");
							
							e.printStackTrace();
						}
					}
				}
			}
			return retVAl;
		}

		/*
		 * runs on agent to send browser information
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#sendBrowsers()
		 */
		@Override
		public boolean sendBrowsers() {
			boolean returnStatus = true;
			
			String selQ = "" ; 
			
			
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			LastSyncData _lstSync = LastSyncData.getInstance(ds);
			SRConnection con = null;
			Dataset retData = null;
			
			String newSyncBrowserTS = null;
			int no = 0;
			try {
				con = ds.getConnection();
				
				//retrieve data from mod_ia_hourly_alarm_counts
				if(_lstSync.getLast_sync_browsers_timestamp() != null )
				{
					selQ = "SELECT * from mod_ia_browser_info where TIMESTAMP >= '"
							+ _lstSync.getLast_sync_browsers_timestamp() 
							+ "';";
				}
				else
				{
					selQ = "SELECT * from mod_ia_browser_info;";
				}
				
				retData = con.runQuery(selQ);
				
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
						newSyncBrowserTS = retData.getValueAt(no -1 , 2 ).toString();
					}
				}
				
			}
			catch(Exception e)
			{
				returnStatus = false;
				e.printStackTrace();
				
			}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("sendBrowsers : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			
			boolean syncStatus = false;
			String agentName = this.mycontext.getGatewayAreaNetworkManager().getServerAddress().getServerName();
			if(this.isAgent)
			{
				ServiceManager sm = this.mycontext.getGatewayAreaNetworkManager().getServiceManager();
				
				ServerId sid = new ServerId(this.controllerName);
				ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
			
				//if service is available
				if(s == ServiceState.Available)
				{
					//call the service 
					syncStatus = sm.getService(sid, GetAnalyticsInformationService.class).get().receiveBrowsers(agentName, retData);
					
				}
				else
				{
					returnStatus = false;
				}
			}
			else
			{
				syncStatus = this.receiveBrowsers(agentName, retData);
			}
			//update all last_sync variables
			if(syncStatus == true)
			{
				_lstSync.setLast_sync_browsers_timestamp(newSyncBrowserTS);
			}
			else
			{
				returnStatus = false;
			}
			return returnStatus;
		}

		@Override
		public boolean sendActiveUsers() {
			boolean returnStatus = true;
			String selQ = "" ; 
			ActiveUsersData_Sync auSummary = new ActiveUsersData_Sync();
			
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			LastSyncData _lstSync = LastSyncData.getInstance(ds);
			SRConnection con = null;
			Dataset retData;
			
			String new_sync_hourly_aUsers_date = null;
			String new_sync_hourly_aUsers_hour = null;
			String new_sync_daily_aUsers_date = null;
			String new_sync_monthly_aUsers_month = null;
			String new_sync_monthly_aUsers_year = null;
			
			int no = 0;
			try {
				con = ds.getConnection();
				
				//retrieve data from mod_ia_hourly_actieve_users
				if(_lstSync.getLast_sync_hourly_aUsers_date() != null && _lstSync.getLast_sync_hourly_aUsers_hour() != null)
				{
					selQ = "SELECT * from mod_ia_hourly_active_users where SUMMARY_DATE >= '"
							+ _lstSync.getLast_sync_hourly_aUsers_date() 
							//+ "' and SUMMARY_HOUR >= " + _lstSync.getLast_sync_hourly_aUsers_hour()
							+ "';";
				}
				else
				{
					selQ = "SELECT * from mod_ia_hourly_active_users;";
				}
				
				retData = con.runQuery(selQ);
				auSummary.hourlyActiveUsers= retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
						new_sync_hourly_aUsers_date = retData.getValueAt(no -1 , 1 ).toString();
						new_sync_hourly_aUsers_hour = retData.getValueAt(no -1 , 2 ).toString();
					}
				}
				
				//retrieve data from mod_ia_daily_active_users
				
				if(_lstSync.getLast_sync_daily_aUsers_date() != null)
				{
					selQ = "SELECT * from mod_ia_daily_active_users where SUMMARY_DATE >= '"
							+ _lstSync.getLast_sync_daily_aUsers_date() 
							+ "';";
				}
				else
				{
					selQ = "SELECT * from mod_ia_daily_active_users;";
				}
				
				retData = con.runQuery(selQ);
				auSummary.dailyActiveUsers = retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
						new_sync_daily_aUsers_date = retData.getValueAt(no -1 , 1 ).toString();
						
					}
				}
				
				//retrieve data from mod_ia_monthly_actieve_users
				if(_lstSync.getLast_sync_monthly_aUsers_month() != null && _lstSync.getLast_sync_monthly_aUsers_year() != null)
				{
					selQ = "SELECT * from mod_ia_monthly_active_users where MONTH_NO >= '"
							+ _lstSync.getLast_sync_monthly_aUsers_month() + "' and YEAR >= " + _lstSync.getLast_sync_monthly_aUsers_year()
							+ ";";
				}
				else
				{
					selQ = "SELECT * from mod_ia_monthly_active_users;";
				}
				
				retData = con.runQuery(selQ);
				auSummary.monthlyActiveUsers= retData;
				if(retData != null )
				{
					no = retData.getRowCount();
					if(no > 0)
					{
						new_sync_monthly_aUsers_month = retData.getValueAt(no -1 , 1 ).toString();
						new_sync_monthly_aUsers_year = retData.getValueAt(no -1 , 2 ).toString();
					}
				}
			}
			catch(Exception e)
			{
				returnStatus = false;
				e.printStackTrace();
				
			}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("sendAlarmsSummary : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			
			boolean status = false;
			auSummary.agentName = this.mycontext.getGatewayAreaNetworkManager().getServerAddress().getServerName();
			if(this.isAgent)
			{
				ServiceManager sm = this.mycontext.getGatewayAreaNetworkManager().getServiceManager();
				
				ServerId sid = new ServerId(this.controllerName);
				
				ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
				//if service is available
				if(s == ServiceState.Available)
				{
					//call the service 
					status = sm.getService(sid, GetAnalyticsInformationService.class).get().receiveActiveUsersSummary(auSummary);
				
					
				}
				else
				{
					returnStatus = false;
				}
			}
			else
			{
				status = this.receiveActiveUsersSummary(auSummary);
			}
			//update all last_sync variables
			if(status == true)
			{
				_lstSync.setLast_sync_daily_aUsers_date(new_sync_daily_aUsers_date);
				_lstSync.setLast_sync_hourly_aUsers_date(new_sync_hourly_aUsers_date);
				_lstSync.setLast_sync_hourly_aUsers_hour(new_sync_hourly_aUsers_hour);
				_lstSync.setLast_sync_monthly_aUsers_month(new_sync_monthly_aUsers_month);
				_lstSync.setLast_sync_monthly_aUsers_year(new_sync_monthly_aUsers_year);
						
			}
			else
			{
				returnStatus = false;
			}
			return returnStatus;
		}

		@Override
		public boolean getIfAgent() {
			boolean retVal = false;
			List<MODIAPersistentRecord> results;
			MODIAPersistentRecord record;
			SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
			results = mycontext.getPersistenceInterface().query(query);
			
			if(results != null && results.size() > 0)
			{
				
				record = results.get(0);
				if(record != null)
				{
					retVal = record.getIsAgent();
				}
				
			}
			
		return retVal;
		}

		@Override
		public Long getControllerId() {
			Long retVal = -1L;
			List<MODIAPersistentRecord> results;
			MODIAPersistentRecord record;
			SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
			results = mycontext.getPersistenceInterface().query(query);
			
			
			
			if(results != null && results.size() > 0)
			{
				
				record = results.get(0);
				if(record != null)
				{
					retVal = record.getControllerconnectionid();
				}
				
			}
			
		return retVal;
		}

		/**
		 * Method to retrieve the overview information for given duration and given project/all projects
		 */
		@Override
		public OverviewInformation getOverviewOnController( int duration,
				String gatewayName, String projectName, boolean allGateways, boolean allProjects) {
				OverviewInformation info = new OverviewInformation();
			
				
				Datasource ds;
				Dataset resDS = null;
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				SRConnection con = null;
						
				String dateFilter = getDateFilterOnController(duration, "session_start");
				String screenViewDateFilter = getDateFilterOnController(duration, "a.VIEW_TIMESTAMP");
				String sessionDateFilter = getDateFilterOnController(duration, "s.session_start");
				String auditDateFilter = getDateFilterOnController(duration, "EVENT_TIMESTAMP");

				String sqlQuery = "";
				String sqlScreensQuery = "";
				String usersQuery = "";
				String filter = "";
				String filterScreens = "";
				
				if(allProjects && allGateways)
				{
					filter = "";
					filterScreens = "";
				}
				else if(allProjects && !allGateways)
				{
					filter = " and GATEWAY_ID = '" + gatewayName + "' ";	
					filterScreens = " and a.GATEWAY_ID = '" + gatewayName + "' ";	
				}
				else if(!allProjects && allGateways)
				{
					filter = " and PROJECT_NAME = '" + projectName + "' ";			
					filterScreens = " and a.PROJECT = '" + projectName + "' ";		
				}
				else if(!allProjects && !allGateways)
				{
					filter = " and GATEWAY_ID = '" + gatewayName + "' and PROJECT_NAME = '" + projectName + "' ";
					filterScreens = " and a.GATEWAY_ID = '" + gatewayName + "' and a.PROJECT = '" + projectName + "' ";
				}
				int noOfUsers = this.getNumberOfActiveUsersOnController(duration, gatewayName, projectName, allGateways, allProjects);
				
				sqlQuery = "select sum(no_of_screens+no_of_actions) as ACTIONS,"
						+ " time_format(SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))), '%H:%i:%s') as AVG_SESSION_DURATION,"
						+ " (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION,"
						+ " count(session_start) as sessions"
						+ " from mod_ia_aggregates_daily_sessions  WHERE "
						+ dateFilter + filter + ";";				

				sqlScreensQuery = "SELECT count(x.SCREEN_NAME) as noOfScreenViews FROM "
						+ "	mod_ia_aggregates_projects b,"
						+ " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project, a.GATEWAY_ID as GATEWAY_ID"
						 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter + filterScreens
						 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID and a.VIEW_TIMESTAMP >= s.SESSION_START "
						 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
						 + " where x.GATEWAY_ID = b.GATEWAY_ID and x.PROJECT = b.PROJECT_NAME GROUP BY x.username, b.AUTH_PROFILE, x.GATEWAY_ID having count(screen_name) = 1;";
				
				
				usersQuery = "SELECT count(distinct(concat(a.actor , b.auth_profile , a.gateway_id))) as noOfUsers "
						+ " FROM mod_ia_aggregates_actions a, mod_ia_aggregates_projects b WHERE action = 'login' and "
						+ auditDateFilter + filterScreens + " and a.gateway_id = b.gateway_id and a.project = b.project_name;";

//				log.error("getOverviewOnController : sql q is " + sqlQuery);
//				log.error("getOverviewOnController : sql screens q is " + sqlScreensQuery);
//				log.error("getOverviewOnController : sql users q is " + usersQuery);
				try {
					con = ds.getConnection();
				
					resDS = con.runQuery(sqlQuery);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							info.setNoOfActions((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
						}
						if(resDS.getValueAt(0, 1) != null)
						{
							info.setAvgSessionDuration(resDS.getValueAt(0, 1).toString());
						}
						if(resDS.getValueAt(0, 2) != null)
						{
							info.setAverageScreensPerVisit(Float.parseFloat(resDS.getValueAt(0, 2).toString()));
						}
						if(resDS.getValueAt(0, 3) != null)
						{
							info.setNoOfSessions((int)Float.parseFloat(resDS.getValueAt(0, 3).toString()));
						}						
							info.setNoOfActiveUsers(noOfUsers);
						
					}
					
						resDS = con.runQuery(sqlScreensQuery);
						
						float usersWithOneScreen = 0;
						float totalUsers = 0;
						if(resDS != null && resDS.getRowCount() > 0)
						{
//							if(resDS.getValueAt(0, 0) != null)
//							{
//								usersWithOneScreen = Float.parseFloat(resDS.getValueAt(0, 0).toString());
//							}
							usersWithOneScreen = resDS.getRowCount();
						}
						
						resDS = con.runQuery(usersQuery);
						
						if(resDS != null && resDS.getRowCount() > 0)
						{
							if(resDS.getValueAt(0, 0) != null)
							{
								totalUsers = Float.parseFloat(resDS.getValueAt(0, 0).toString());
							}
						}
						if(usersWithOneScreen == 0 || totalUsers == 0)
						{
							info.setBounceRate(0);
						}
						else
						{
							info.setBounceRate(usersWithOneScreen/totalUsers);
						}
				}
				catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				
				
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getOverviewOnController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			
			int totalScreens = 0;
			List<ScreensCount> allScreenViews = this.getScreensViewedCountsOnController(duration, gatewayName, projectName, allGateways, allProjects);
			if(allScreenViews != null)
			{
				int noOfRecords = allScreenViews.size();
				for(int k=0; k<noOfRecords; k++)
				{
					totalScreens = totalScreens + allScreenViews.get(k).getNoOfViews();
				}
				info.setNoOfScreenViews(totalScreens);
			}
			info.setScreenViews(allScreenViews);
				//calculate gateway up and downtime in given duration
			 	Calendar firstHour = Calendar.getInstance();
			    firstHour.set(Calendar.HOUR_OF_DAY, 00);
			    firstHour.set(Calendar.MINUTE, 00);
			    firstHour.set(Calendar.SECOND, 00);
			    
			    int month = 0;
			    int year;
			    Date sDate;
			    Date todayDate = new Date();
			    long startTime;
				long endTime;
				long diffTime;
				long diffDays;
			    
				Double gDownTime = this.getGatewayDowntimeOnController(duration, gatewayName, allGateways);
				
				Double totalTime = 86400.000;
				Double gUpTime = 0.000;
				switch(duration)
				{
				case Constants.TODAY:
					totalTime = 86400.000;
					break;
				case Constants.YESTERDAY:
					totalTime = 86400.000;
					break;
				case Constants.LAST_SEVEN_DAYS:	
					case Constants.LAST_WEEK:
					totalTime =604800.000;
					break;
				case Constants.LAST_THIRTY_DAYS:
					totalTime =2592000.000;
					break;
				case Constants.LAST_NINTY_DAYS:
					totalTime =7776000.000;
					break;
				case Constants.LAST_365_DAYS:
					totalTime =31536000.000;
					break;
				case Constants.LAST_MONTH:
					month = firstHour.get(Calendar.MONTH);
					if(month == 0)
					{
						year = firstHour.get(Calendar.YEAR);
						firstHour.set(year - 1, 11, 1);
					}
					else
					{
						firstHour.set(Calendar.MONTH, month - 1);
					}
					totalTime = (double)(firstHour.getActualMaximum(Calendar.DAY_OF_MONTH) * 24 * 60 * 60) ;
					break;
			
				case Constants.LAST_YEAR:
					year = firstHour.get(Calendar.YEAR);
					firstHour.set(year - 1,0,1);
					totalTime = (double)firstHour.getActualMaximum(Calendar.DAY_OF_YEAR) * 24 * 60 * 60 ;
					break;
				case Constants.THIS_MONTH:
					month = firstHour.get(Calendar.MONTH);
					firstHour.set(Calendar.DAY_OF_MONTH, 1);
					sDate = firstHour.getTime();
					startTime = sDate.getTime();
					endTime = todayDate.getTime();
					totalTime = (double)(endTime - startTime);
					totalTime = totalTime / 1000;
					break;
				case Constants.THIS_WEEK:
					firstHour.set(Calendar.DAY_OF_WEEK, firstHour.getFirstDayOfWeek());
					sDate = firstHour.getTime();
					startTime = sDate.getTime();
					endTime = todayDate.getTime();
					
					totalTime = (double)(endTime - startTime);
					totalTime = totalTime / 1000; //seconds
					break;
				case Constants.THIS_YEAR:
					year = firstHour.get(Calendar.YEAR);
					firstHour.set(year,0,1);
					sDate = firstHour.getTime();
					startTime = sDate.getTime();
					endTime = todayDate.getTime();
					totalTime = (double)(endTime - startTime);
					totalTime = totalTime / 1000;
					break;
				default:
				}
				//calculate up time , down time and percentages
				int day;
				long hours, minutes, seconds;
				
				
				gUpTime = totalTime - gDownTime;
				
				
				if(gDownTime == 0)
				{
					info.setGatewayDownTimePercent(0);
					info.setGatewayUpTimePercent(100);
					info.setGatewayDownTimeString("0 days, 0 hours, 0 minutes, 0 seconds");
				}
				else
				{
					
					info.setGatewayDownTimePercent((float)(gDownTime  /totalTime)* 100);
					info.setGatewayUpTimePercent((float)(gUpTime  /totalTime)* 100);
				
					//calculate days for down time
								
					gDownTime = gDownTime * 1000; //get val in ms
					day = (int)(gDownTime / (1000 * 60 * 60 * 24));        
					hours =  (long)(gDownTime / (1000 * 60 * 60)) % 24;
					minutes = (long)(gDownTime / (1000 * 60)) % 60;;
					seconds = (long)(gDownTime / 1000) % 60;
					info.setGatewayDownTimeString(day + " days, " + hours +  " hours, "
							 + minutes + " minutes, " + seconds + " seconds");
				}
				
				//calculate days for uptime
				
				gUpTime = gUpTime * 1000; //get val in ms
				day = (int)(gUpTime / (1000 * 60 * 60 * 24));        
				hours =  (long)(gUpTime / (1000 * 60 * 60)) % 24;
				minutes = (long)(gUpTime / (1000 * 60)) % 60;;
				seconds = (long)(gUpTime / 1000) % 60;
				
				info.setGatewayUpTimeString(day + " days, " + hours +  " hours, "
						 + minutes + " minutes, " + seconds + " seconds");
				
			return info;
		}
		
		/**
		 * A method to retrieve time in seconds for which gateway was down in a given duration
		 * @param dsName
		 * @param duration
		 * @return downtime in seconds
		 */
		private Double getGatewayDowntimeOnController( int duration, String gatewayName, boolean allGateways)
		{
			Double downTime = 0.0;
			String dateFilter = getDateFilterOnController(duration, "b.event_timestamp");
			Datasource ds;
			
			Dataset result;
			SRConnection con = null;
			
			String sqlQuery = "";
			
			if(allGateways)
			{
				sqlQuery = "select sum(timestampdiff(second,shutdownTime, startTime))"
					+ "from"
					+ " (select min(a.event_timestamp) as startTime, b.event_timestamp as shutdownTime"
					+ " from MOD_IA_AGGREGATES_AUDIT_EVENTS a,MOD_IA_AGGREGATES_AUDIT_EVENTS b"
					+ " where a.actor = b.actor  and a.action = 'GATEWAY_START' and b.action = 'GATEWAY_SHUTDOWN'"
					+ " and a.GATEWAY_ID = b.GATEWAY_ID "
					+ " and " + dateFilter + " and b.event_timestamp <= a.event_timestamp"
					+ " group by shutdownTime order by shutdownTime desc) as dt;";
			}
			else
			{
				sqlQuery = "select sum(timestampdiff(second,shutdownTime, startTime))"
						+ "from"
						+ " (select min(a.event_timestamp) as startTime, b.event_timestamp as shutdownTime"
						+ " from MOD_IA_AGGREGATES_AUDIT_EVENTS a,MOD_IA_AGGREGATES_AUDIT_EVENTS b"
						+ " where a.actor = b.actor  and a.action = 'GATEWAY_START' and b.action = 'GATEWAY_SHUTDOWN'"
						+ " and a.GATEWAY_ID = b.GATEWAY_ID and a.GATEWAY_ID = '" + gatewayName + "'"
						+ " and " + dateFilter + " and b.event_timestamp <= a.event_timestamp"
						+ " group by shutdownTime order by shutdownTime desc) as dt;";
			}
			try{
				
					ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					con = ds.getConnection();
					
					result = con.runQuery(sqlQuery);
					if(result != null && result.getRowCount() > 0)
					{
						if(result.getValueAt(0, 0) != null)
						{
							//downTime = new Double(Double.parseDouble(result.getValueAt(0, 0).toString())).longValue();
							downTime = Double.parseDouble(result.getValueAt(0, 0).toString());
						}
						
					}
			}
			catch(Exception e){
				log.error("getGatewayDowntime : " + e);
			}finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getGatewayDowntime : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return downTime;
		}

		/*
		 * Method to retrieve active users information on the Controller
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getNumberOfActiveUsersOnController(int, java.lang.String, java.lang.String, boolean, boolean)
		 */
		@Override
		public int getNumberOfActiveUsersOnController(int duration,
				String GatewayName, String projectName, boolean allGateways,
				boolean allProjects) {
			int numActUsers = 0;
			Datasource ds ;
			Dataset resDS = null;
		
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			
			//noOfDS = dsList.size();
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilterOnController(duration, "event_timestamp");	
		
			
			if(allProjects && allGateways)
			{
				sqlQuery = "SELECT distinct(CONCAT(a.ACTOR,b.AUTH_PROFILE, a.GATEWAY_ID))"
						+ " from mod_ia_aggregates_actions a,  mod_ia_aggregates_projects b"
						+ " where a.action = 'login' and a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID "
						+ " and "
						+ dateFilter + " ;"; 
			}
			else if(allProjects && !allGateways)
			{
				sqlQuery = "SELECT distinct(CONCAT(a.ACTOR,b.AUTH_PROFILE, a.GATEWAY_ID))"
						+ " from mod_ia_aggregates_actions a,  mod_ia_aggregates_projects b"
						+ " where a.action='login' and a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID and a.GATEWAY_ID = '" + GatewayName + "'"
						+ " and "
						+ dateFilter + " ;"; 
				
			}
			else if(!allProjects && !allGateways)
			{
				sqlQuery = "SELECT distinct(CONCAT(a.ACTOR,b.AUTH_PROFILE, a.GATEWAY_ID))"
						+ " from mod_ia_aggregates_actions a,  mod_ia_aggregates_projects b"
						+ " where a.action = 'login' and a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID and a.GATEWAY_ID = '" + GatewayName + "'"
								+ " and a.PROJECT = '" + projectName + "' "
						+ "  and "
						+ dateFilter + " ;"; 
			}
			else if(!allProjects && allGateways)
			{
			
				sqlQuery = "SELECT distinct(CONCAT(a.ACTOR,b.AUTH_PROFILE, a.GATEWAY_ID))"
						+ " from mod_ia_aggregates_actions a,  mod_ia_aggregates_projects b"
						+ " where a.action='login' and a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID and a.PROJECT = '" + projectName + "' "
						+ " and "
						+ dateFilter + " ;"; 
			}
		
				try {
//					log.error("getNumberOfActiveUsersOnController sql q is : " + sqlQuery);
					
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
						if(resDS != null)
						{
							numActUsers = resDS.getRowCount();
						}
				}
					catch (SQLException e) {
					
					log.error(e);
				
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
					
					}
			}
			return numActUsers;
		}

		/*
		 * Method to retrieve screen viewed counts on Controller from the aggregates tables.
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getScreensViewedCountsOnController(int, java.lang.String, java.lang.String, boolean, boolean)
		 */
		@Override
		public List<ScreensCount> getScreensViewedCountsOnController(
				int duration, String GatewayName, String projectName,
				boolean allGateways, boolean allProjects) {
			List<ScreensCount> retList = new ArrayList<ScreensCount>();
			ScreensCount objScreen;
			Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			Dataset results = null;
			String sqlQuery = "";
			SRConnection con = null;
			int noOfResults;	
			String dateFilter = getDateFilterOnController(duration, "a.VIEW_TIMESTAMP");
			String sessionDateFilter = getDateFilterOnController(duration, "s.SESSION_START");		
			if(allProjects && allGateways)
			{

				sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME) as screenCount"
						 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + dateFilter + " and " + sessionDateFilter
						 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = s.GATEWAY_ID and a.USERNAME = s.USERNAME"
						 + " and a.VIEW_TIMESTAMP >= s.SESSION_START and a.VIEW_TIMESTAMP <= s.SESSION_END "
						 + " GROUP BY a.SCREEN_NAME order by screenCount desc;";
			}
			else if(allProjects && !allGateways)
			{
				sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME) as screenCount"
						 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + dateFilter + " and " + sessionDateFilter
						 + " AND a.GATEWAY_ID = '" + GatewayName + "'"
						 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = s.GATEWAY_ID and a.USERNAME = s.USERNAME"
						 + " and a.VIEW_TIMESTAMP >= s.SESSION_START and a.VIEW_TIMESTAMP <= s.SESSION_END "
						 + " GROUP BY a.SCREEN_NAME order by screenCount desc;";
			
			}
			else if(!allProjects && !allGateways)
			{
				sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME) as screenCount"
						 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + dateFilter + " and " + sessionDateFilter
						 + " AND a.GATEWAY_ID = '" + GatewayName + "' AND a.PROJECT = '" + projectName + "'"
						 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = s.GATEWAY_ID and a.USERNAME = s.USERNAME"
						 + " and a.VIEW_TIMESTAMP >= s.SESSION_START and a.VIEW_TIMESTAMP <= s.SESSION_END "
						 + " GROUP BY a.SCREEN_NAME order by screenCount desc;";
				
			}
			else if(!allProjects && allGateways)
			{
				sqlQuery = "SELECT a.SCREEN_NAME , count(a.SCREEN_NAME) as screenCount"
						 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + dateFilter + " and " + sessionDateFilter
						 + " AND a.PROJECT = '" + projectName + "'"
						 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = s.GATEWAY_ID and a.USERNAME = s.USERNAME"
						 + " and a.VIEW_TIMESTAMP >= s.SESSION_START and a.VIEW_TIMESTAMP <= s.SESSION_END "
						 + " GROUP BY a.SCREEN_NAME order by screenCount desc;";
			}
			
//			log.error("getScreensViewedCountsOnController: "+sqlQuery);
			
				try {
						con = ds.getConnection();
						results = con.runQuery(sqlQuery);
						if(results != null)
						{
							noOfResults = results.getRowCount();
							for(int i=0; i<noOfResults; i++)
							{
								objScreen = new ScreensCount();
								objScreen.setScreenName(results.getValueAt(i, 0).toString());
								objScreen.setNoOfViews(Integer.parseInt(results.getValueAt(i, 1).toString()));
								retList.add(objScreen);
							}
						}
					}
					catch (SQLException e) {
				
					log.error(e);
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getScreensViewedCounts : in con close exception.");
						
						log.error(e);
					}
					
					}
				}	
			
			return retList;
		}

		@Override
		public OverviewInformation getYesterdayOverviewForSliderOnController(
				int duration, String gatewayName, String projectName,
				boolean allGateways, boolean allProjects) {
			OverviewInformation info = new OverviewInformation();
			
			Datasource ds;
			Dataset resDS = null;
			Dataset resDS1 = null;
			String startDate = "", endDate = "";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
//			String dateFilter = getDateFilter(duration, "overview_date");
			String sqlQuery = "";
			String sqlQueryTotalUsers = "";
			
			//calculate yesterday time period for given duration
			Calendar durationStart = Calendar.getInstance();
			durationStart.set(Calendar.HOUR_OF_DAY, 00);
			durationStart.set(Calendar.MINUTE, 00);
			durationStart.set(Calendar.SECOND, 00);
		   
		    
		    Calendar durationEnd = Calendar.getInstance();
		    durationEnd.set(Calendar.HOUR_OF_DAY, 23);
		    durationEnd.set(Calendar.MINUTE, 59);
		    durationEnd.set(Calendar.SECOND, 59);
		   
		    switch(duration)
		    {
		    case Constants.TODAY:
		    	durationStart.add(Calendar.DATE, -1);
		    	durationEnd.add(Calendar.DATE, -1);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				
		    	break;
		    case Constants.YESTERDAY:
		    	durationStart.add(Calendar.DATE, -2);
		    	durationEnd.add(Calendar.DATE, -2);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.LAST_SEVEN_DAYS:
				durationStart.add(Calendar.DATE, -14);
		    	durationEnd.add(Calendar.DATE, -7);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.LAST_THIRTY_DAYS:
				durationStart.add(Calendar.DATE, -60);
		    	durationEnd.add(Calendar.DATE, -30);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.LAST_NINTY_DAYS:
				durationStart.add(Calendar.DATE, -180);
		    	durationEnd.add(Calendar.DATE, -90);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.LAST_365_DAYS:
				durationStart.add(Calendar.DATE, -730);
		    	durationEnd.add(Calendar.DATE, -365);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.LAST_MONTH:
				durationStart.set(Calendar.MONTH, durationStart.get(Calendar.MONTH) - 2);
				durationStart.set(Calendar.DAY_OF_MONTH, 1);
			   	durationEnd.set(Calendar.MONTH, durationEnd.get(Calendar.MONTH) - 2);
		    	durationEnd.set(Calendar.DAY_OF_MONTH, durationEnd.getActualMaximum(durationEnd.DAY_OF_MONTH));
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.LAST_WEEK:
				durationStart.set(Calendar.WEEK_OF_YEAR, durationStart.get(Calendar.WEEK_OF_YEAR) - 2);
				durationStart.set(Calendar.DAY_OF_WEEK, durationStart.getFirstDayOfWeek());
				durationEnd.set(Calendar.WEEK_OF_YEAR, durationEnd.get(Calendar.WEEK_OF_YEAR) - 2);
				durationEnd.set(Calendar.DAY_OF_WEEK, durationEnd.getFirstDayOfWeek() + 6);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.LAST_YEAR:
				durationStart.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 2);
				durationStart.set(Calendar.DAY_OF_YEAR, 1);
				durationEnd.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 2);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.THIS_MONTH:
				durationStart.set(Calendar.MONTH, durationStart.get(Calendar.MONTH) - 1);
				durationStart.set(Calendar.DAY_OF_MONTH, 1);
			   	durationEnd.set(Calendar.MONTH, durationEnd.get(Calendar.MONTH) - 1);
		    	durationEnd.set(Calendar.DAY_OF_MONTH, durationEnd.getActualMaximum(durationEnd.DAY_OF_MONTH));
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			case Constants.THIS_WEEK:
				durationStart.set(Calendar.WEEK_OF_YEAR, durationStart.get(Calendar.WEEK_OF_YEAR) - 1);
				durationStart.set(Calendar.DAY_OF_WEEK, durationStart.getFirstDayOfWeek());
				durationEnd.set(Calendar.WEEK_OF_YEAR, durationEnd.get(Calendar.WEEK_OF_YEAR) - 1);
				durationEnd.set(Calendar.DAY_OF_WEEK, durationEnd.getFirstDayOfWeek() + 6);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				
				break;
			case Constants.THIS_YEAR:
				durationStart.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 1);
				durationStart.set(Calendar.DAY_OF_YEAR, 1);
				durationEnd.set(Calendar.YEAR, durationStart.get(Calendar.YEAR) - 1);
				startDate = sdf.format(durationStart.getTime());
				endDate = sdf.format(durationEnd.getTime());
				break;
			default:
		    }
		    
		    
		    String dateFilter = "session_date >= '" + startDate + "' and session_date <= '" + endDate + "'";
		    String dateFilterAuditEvents = "EVENT_TIMESTAMP >= '" + startDate + "' and EVENT_TIMESTAMP <= '" + endDate + "'";
		    String screenViewDateFilter = "a.VIEW_TIMESTAMP >= '" + startDate + "' and a.VIEW_TIMESTAMP <= '" + endDate + "'";
		    String sessionDateFilter = "s.session_start >= '" + startDate + "' and s.session_start <= '" + endDate + "'";
			String filter = "";
			String filterScreens = "";
			String sqlScreensQueryBrate = "";
			String sqlScreensQueryTotal = "";
			if(allProjects && allGateways)
			{
				filter = "";
				filterScreens = "";
			}
			else if(allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + gatewayName + "' ";	
				filterScreens =" and a.GATEWAY_ID = '" + gatewayName + "' ";	
			}
			else if(!allProjects && allGateways)
			{
				filter = " and PROJECT_NAME = '" + projectName + "' ";			
				filterScreens = " and a.PROJECT = '" + projectName + "' ";		
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + gatewayName + "' and PROJECT_NAME = '" + projectName + "' ";
				filterScreens = " and a.GATEWAY_ID = '" + gatewayName + "' and a.PROJECT = '" + projectName + "' ";
			}
			sqlQuery = "select sum(no_of_screens+no_of_actions) as ACTIONS,"
					+ " time_format(SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))), '%H:%i:%s') as AVG_SESSION_DURATION,"
					+ " (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION,"
					+ " count(session_start) as sessions"
					+ " from mod_ia_aggregates_daily_sessions  WHERE "
					+ dateFilter + filter + ";";

			
			sqlScreensQueryBrate = "SELECT count(x.SCREEN_NAME) as noOfScreenViews FROM "
					+ "	mod_ia_aggregates_projects b,"
					+ " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project, a.GATEWAY_ID as GATEWAY_ID"
					 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
					 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter + filterScreens
					 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID and a.VIEW_TIMESTAMP >= s.SESSION_START "
					 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
					 + " where x.GATEWAY_ID = b.GATEWAY_ID and x.PROJECT = b.PROJECT_NAME GROUP BY x.username, b.AUTH_PROFILE, x.GATEWAY_ID having count(screen_name) = 1;";
			
			
			
			sqlQueryTotalUsers = "SELECT count(distinct(concat(a.actor,b.auth_profile,a.gateway_id))) as noOfUsers "
					+ " FROM mod_ia_aggregates_actions a, mod_ia_aggregates_projects b WHERE action = 'login' and "
					+ dateFilterAuditEvents + filterScreens + " and a.gateway_id = b.gateway_id and a.project = b.project_name;";
				
//			log.error("getYesterdayOverviewForSliderOnController: "+sqlQuery);
//			log.error("getYesterday total users: "+sqlQueryTotalUsers);
//			log.error("getYesterdayOverviewForSliderOnController screens: "+sqlScreensQueryBrate);
			
			try {
				con = ds.getConnection();
				
				resDS = con.runQuery(sqlQuery);
				if(resDS != null && resDS.getRowCount() > 0)
				{
					if(resDS.getValueAt(0, 0) != null)
					{
						info.setNoOfActions((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
					}
					if(resDS.getValueAt(0, 1) != null)
					{
						info.setAvgSessionDuration(resDS.getValueAt(0, 1).toString());
					}
					if(resDS.getValueAt(0, 2) != null)
					{
						info.setAverageScreensPerVisit(Float.parseFloat(resDS.getValueAt(0, 2).toString()));
					}
					if(resDS.getValueAt(0, 3) != null)
					{
						info.setNoOfSessions((int)(Float.parseFloat(resDS.getValueAt(0, 3).toString())));
					}					
				}
				resDS = con.runQuery(sqlScreensQueryBrate);
				float usersWithOneScreen = 0;
				float totalUsers = 0;
				if(resDS != null && resDS.getRowCount() > 0)
				{
//					if(resDS.getValueAt(0, 0) != null)
//					{
						usersWithOneScreen = resDS.getRowCount();
					//}

				}
				
				resDS1 = con.runQuery(sqlQueryTotalUsers);
							
				if(resDS != null && resDS.getRowCount() > 0)
				{
					if(resDS.getValueAt(0, 0) != null)
					{
						totalUsers = Float.parseFloat(resDS1.getValueAt(0, 0).toString());
						info.setNoOfActiveUsers((int) totalUsers);
					}
				}
				if(usersWithOneScreen == 0 || totalUsers == 0)
				{
					info.setBounceRate(0);
				}
				else
				{
					info.setBounceRate(usersWithOneScreen/totalUsers);
				}
				
				
				sqlScreensQueryTotal = "SELECT count(x.SCREEN_NAME) as noOfScreenViews FROM "
						+ "	mod_ia_aggregates_projects b,"
						+ " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project, a.GATEWAY_ID as GATEWAY_ID"
						 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter + filterScreens
						 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID and a.VIEW_TIMESTAMP >= s.SESSION_START "
						 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
						 + " where x.GATEWAY_ID = b.GATEWAY_ID and x.PROJECT = b.PROJECT_NAME;";
				resDS = null;
				resDS = con.runQuery(sqlScreensQueryTotal);
				if(resDS != null && resDS.getRowCount() > 0)
				{
					if(resDS.getValueAt(0, 0) != null)
					{
						info.setNoOfScreenViews((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
					}
					else
					{
						info.setNoOfScreenViews(0);
					}

				}
				else
				{
					info.setNoOfScreenViews(0);
				}
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
			
			}
		}
			
		return info;
		}

		@Override
		public Dataset getActiveUsersInformationOnController(
				String projectName, boolean allProjects, String gatewayName,
				boolean allGateways, int duration) {
			Dataset activeUsersdata = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilterOnController(duration, "summary_date");
			String sqlQuery = "";
			String filter = "";
			
			if(allGateways && allProjects)
			{
				filter = " and PROJECT_NAME = 'All'"; 
			}
			else if(allGateways && !allProjects)
			{
				filter = " and PROJECT_NAME = '"  + projectName + "'";
			}
			else if(!allGateways && allProjects)
			{
				filter = " and GATEWAY_ID = '"  + gatewayName + "' AND PROJECT_NAME = 'All' ";
			}
			else if(!allGateways && !allProjects)
			{
				filter = " and GATEWAY_ID = '" + gatewayName + "' AND PROJECT_NAME = '"  + projectName + "'";
			}
			
			
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users) "
						+ " from mod_ia_hours as a "
						+ " left join (select one_day_active_users, seven_day_active_users, fourteen_day_active_users, summary_hour from mod_ia_aggregates_hourly_active_users where "
						+ dateFilter + filter + ") as b"
						+ " on b.summary_hour = a.Hour"
						+ " group by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS  )
				{
					String sDate = "";
					Calendar lastHour = Calendar.getInstance();
				    lastHour.set(Calendar.HOUR_OF_DAY, 23);
				    lastHour.set(Calendar.MINUTE, 59);
				    lastHour.set(Calendar.SECOND, 59);
				    
				    int currentYear = lastHour.get(Calendar.YEAR);
					lastHour.add(Calendar.DATE, -365);
					int yearVal = lastHour.get(Calendar.YEAR);
					int monthVal = lastHour.get(Calendar.MONTH);
					sqlQuery = "select a.monthName, sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users), a.monthNumber"
							+ " from mod_ia_month as a "
							+ " left join (select one_day_active_users, "
							+ " seven_day_active_users, "
							+ " fourteen_day_active_users, "
							+ " month_no as summary_month , year from mod_ia_aggregates_monthly_active_users where "
							+ " ( year = " + yearVal + " and month_no >= " + monthVal + ") or ( year = " + currentYear + ") "
							+ filter + "  ) as b "
							+ " on b.summary_month = a.monthNumber"
							+ " group by a.monthName order by a.monthNumber;";

//					log.error("LAST_365_DAYS : "+ sqlQuery);
				}
				else if(duration == Constants.LAST_YEAR )
				{
					sqlQuery = "select a.monthName, sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users)"
							+ " from mod_ia_month as a "
							+ " left join (select one_day_active_users, "
							+ " seven_day_active_users, "
							+ " fourteen_day_active_users, "
							+ " month_no as summary_month  from mod_ia_aggregates_monthly_active_users where "
							+ "  year = year(date(now())) - 1 " + filter + ") as b "
							+ " on b.summary_month = a.monthNumber"
							+ " group by a.monthName order by a.monthNumber;";
				}
				else if(duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName,sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users)"
							+ " from mod_ia_month as a "
							+ " left join (select one_day_active_users, "
							+ " seven_day_active_users, "
							+ " fourteen_day_active_users, "
							+ " month_no as summary_month  from mod_ia_aggregates_monthly_active_users where "
							+ " year = year(date(now())) " + filter + ") as b "
							+ " on b.summary_month = a.monthNumber"
							+ " group by a.monthName order by a.monthNumber;";
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
					sqlQuery = "select dayname(summary_date), sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users)"
							+ " from mod_ia_aggregates_daily_active_users where "
							+ dateFilter + filter + " group by dayname(summary_date) order by summary_date;" ;
				}
				else if(duration == Constants.THIS_MONTH || duration == Constants.LAST_MONTH || duration == Constants.LAST_THIRTY_DAYS || duration == Constants.LAST_NINTY_DAYS)
				{
					sqlQuery = "select day(summary_date), sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users) from mod_ia_aggregates_daily_active_users where "
							+ dateFilter + filter + " group by day(summary_date) order by summary_date;";
				}
				else
				{
					sqlQuery = "select summary_date, sum(one_day_active_users), sum(seven_day_active_users), sum(fourteen_day_active_users) from mod_ia_aggregates_daily_active_users where "
							+ dateFilter + filter + " group by summary_date order by summary_date;";
							
				}
			
//			log.error("oneday : "+ sqlQuery);
			try {
				
				con = ds.getConnection();
				activeUsersdata = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error(" getActiveUsersInformationOnController : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			
			return activeUsersdata;
		}

		
		@Override
		public ActiveUsersInfo getActiveUsersCountsOnController(
				String projectName, boolean allProjects, String gatewayName,
				boolean allGateways, int duration) {
			ActiveUsersInfo aInfo = new ActiveUsersInfo();
			int noOfOneDayActiveUsers = 0;
			int noOfSevenDayActiveUsers = 0;
			int noOfFourteenDayActiveUsers = 0;
			int tempVal = 0;
			
			Datasource ds;
			Dataset activeUsersData = null;
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			
				
				String dateFilter = getDateFilterOnController(duration, "event_timestamp");
				String startDate = getDayAndTime(duration);
				String durationEndDate = "";
				String sqlQuery = "";
				
				String filter = "";
				if(allGateways && allProjects)
				{
					filter = " where "; 
				}
				else if(allGateways && !allProjects)
				{
					filter = " where a.PROJECT = '"  + projectName + "' and";
				}
				else if(!allGateways && allProjects)
				{
					filter = " where a.GATEWAY_ID = '"  + gatewayName + "' and";
				}
				else if(!allGateways && !allProjects)
				{
					filter = " where a.GATEWAY_ID = '" + gatewayName + "' AND a.PROJECT = '"  + projectName + "' and";
				}
					
					if(duration == Constants.YESTERDAY || duration == Constants.LAST_WEEK || duration == Constants.LAST_MONTH || duration == Constants.LAST_YEAR)
					{
						durationEndDate = getDurationEndDate(duration);
						
							sqlQuery = "select distinct(concat(a.ACTOR,b.AUTH_PROFILE,a.GATEWAY_ID)) as ACTOR,"
									+ "DATEDIFF(DATE('" + startDate + "'), MAX(DATE(event_timestamp))) as DaysSinceLogin "
									+ "	FROM MOD_IA_AGGREGATES_ACTIONS a, MOD_IA_AGGREGATES_PROJECTS b " + filter 
									+ " action = 'login' and a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID"
									+ " and event_timestamp < '" + durationEndDate  + "'"
									+ "	group by concat(a.ACTOR,b.AUTH_PROFILE,a.GATEWAY_ID);";
						
					}
					else
					{
						sqlQuery = "select distinct(concat(a.ACTOR,b.AUTH_PROFILE,a.GATEWAY_ID)) as ACTOR,"
								+ "DATEDIFF(DATE('" + startDate + "'), MAX(DATE(event_timestamp))) as DaysSinceLogin "
								+ "	FROM MOD_IA_AGGREGATES_ACTIONS a, MOD_IA_AGGREGATES_PROJECTS b " + filter 
								+ " action = 'login' and a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID"
								+ "	group by concat(a.ACTOR,b.AUTH_PROFILE,a.GATEWAY_ID);";
//						
					}

//					log.error("getActiveUsersCountsOnController : sql qq is : " + sqlQuery);
			try {
				
				
				con = ds.getConnection();
				
//				log.error("getActiveUsersCountsOnController : sql qq is : " + sqlQuery);
				activeUsersData = con.runQuery(sqlQuery);
				
				int noOfrecords = 0;
				if(activeUsersData != null && activeUsersData.getRowCount() > 0)
				{
					noOfrecords = activeUsersData.getRowCount();

					
					for(int i=0; i<noOfrecords; i++)
					{
						tempVal = (int)Float.parseFloat(activeUsersData.getValueAt(i, 1).toString());
						if(tempVal <= 0)
						{
							noOfOneDayActiveUsers = noOfOneDayActiveUsers + 1;
							noOfSevenDayActiveUsers = noOfSevenDayActiveUsers + 1;
							noOfFourteenDayActiveUsers = noOfFourteenDayActiveUsers + 1;
						}
						else if(tempVal >= 1 && tempVal <= 7 )
						{
							noOfSevenDayActiveUsers = noOfSevenDayActiveUsers + 1;
							noOfFourteenDayActiveUsers = noOfFourteenDayActiveUsers + 1;
						}
						else if (tempVal > 7 && tempVal <= 14)
						{
							noOfFourteenDayActiveUsers = noOfFourteenDayActiveUsers + 1;
						}
					}
					
					aInfo.setOneDayActiveUsers(noOfOneDayActiveUsers);
					aInfo.setSevenDayActiveUsers(noOfSevenDayActiveUsers);
					aInfo.setFourteenDayActiveUsers(noOfFourteenDayActiveUsers);
				}
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getActiveUsersOnController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return aInfo;
		}

		@Override
		public Dataset getAlarmsOnController(int duration, String gatewayName, boolean allGateways) {
			
			Datasource ds;
			Dataset resDS = null;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			
			String sqlQuery = "";
			SRConnection con = null;
		
			String dateFilter = getDateFilterOnController(duration, "ALARM_DATE");
			
			if(!allGateways)
			{
			
				sqlQuery = "SELECT ALARM_PRIORITY, SUM(ALARMS_COUNT) "
					+ " FROM mod_ia_aggregates_daily_alarms_summary WHERE "
					+ dateFilter + " and GATEWAY_ID = '" + gatewayName + "' group by ALARM_PRIORITY;";
			}
			else
			{
				sqlQuery = "SELECT ALARM_PRIORITY, SUM(ALARMS_COUNT) "
						+ " FROM mod_ia_aggregates_daily_alarms_summary WHERE "
						+ dateFilter + " group by ALARM_PRIORITY;";
			}
			
		
				try {
						con = ds.getConnection();
						
						resDS = con.runQuery(sqlQuery);
					}
						catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getAlarmsOnController : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
			
			return resDS;
		}

		
		
		/**
		 * A function to retrieve frequency information from user sessions summary table.
		 * @author : YM created on 8/3/2015
		 */
		@Override
		public Dataset getFrequencyInformationOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration) {
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilterOnController(duration, "session_start");
			String sqlQuery = "SELECT  username, count(username) , count(no_of_screens) "
					+ " FROM mod_ia_aggregates_daily_sessions WHERE " + dateFilter ;
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(!allProjects && allGateways)
			{
				filter = " and PROJECT_NAME = '" + projectName + "' ";
			}
			else if(allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + gatewayName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + gatewayName + "' and PROJECT_NAME = '" + projectName + "' ";
			}
			sqlQuery = sqlQuery + filter + " group by username;";
			try {
			
//				log.error("getFrequencyInformationOnController : query - " + sqlQuery);
				con = ds.getConnection();
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getFreqInformationOnController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			
			
			
		
			
			return freqData;
		}		
		
		/**
		 * A function to get information per user about how long the user has not logged in.
		 * @param dataSource data source where audit data is available
		 * @return data set containing records with username and number of days since last login
		 * @see Dataset
		 */
		@Override
		public Dataset getDaysSinceLastLoginPerUserOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration) {
			List<String> retList  = new ArrayList<String>();
			Datasource ds ;
			Dataset resDS = null;
		
			
			String startDate = getDayAndTime(duration);
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			if(allProjects && allGateways)
			{
				sqlQuery = "SELECT username, DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(session_start))), sum(no_of_screens) FROM mod_ia_aggregates_daily_sessions group by username, GATEWAY_ID;";
			}
			else if(!allProjects && allGateways)
			{
				sqlQuery = "SELECT username, DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(session_start))) , sum(no_of_screens) FROM mod_ia_aggregates_daily_sessions where "
						+ " PROJECT_NAME = '" + projectName +"' group by username;";
			}
			else if(allProjects && !allGateways)
			{
				sqlQuery = "SELECT username, DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(session_start))), sum(no_of_screens) FROM mod_ia_aggregates_daily_sessions WHERE GATEWAY_ID = '"
								+ gatewayName + "'"
								+ " group by username;";
			}
			else if(!allProjects && !allGateways)
			{
				sqlQuery = "SELECT username, DATEDIFF(DATE('"
						+ startDate + "'), MAX(DATE(session_start))), sum(no_of_screens) FROM mod_ia_aggregates_daily_sessions WHERE GATEWAY_ID = '"
								+ gatewayName + "' AND PROJECT_NAME = '" + projectName + "' "
								+ " group by username;";
			}
			int r=0;
				try {
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
						}
						catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getDaysSinceLastLognController : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
		
			
				return resDS;
		}
		
		/**
		 * A function to retrieve frequency information from user sessions summary table.
		 * @author : YM created on 14/11/2016
		 */
		@Override
		public Dataset getEngagementInformationOnController( String projectName,
				boolean allProjects, String gatewayName, boolean allGateways, int duration) {
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilterOnController(duration, "session_start");
			String sqlQuery = "";
			
			if(allProjects && allGateways)
			{
				sqlQuery = "SELECT  session_duration, count(username), SUM(no_of_screens) "
					+ " FROM mod_ia_aggregates_daily_sessions"
					+ " WHERE " + dateFilter + " group by session_duration;";
			}
			else if(!allProjects && allGateways)
			{
				sqlQuery = "SELECT  session_duration, count(username), SUM(no_of_screens) "
						+ " FROM mod_ia_aggregates_daily_sessions"
						+ " WHERE " + dateFilter + " and PROJECT_NAME = '" + projectName + "' group by session_duration;";
			}
			else if(allProjects && !allGateways)
			{
				sqlQuery = "SELECT  session_duration, count(username), SUM(no_of_screens) "
						+ " FROM mod_ia_aggregates_daily_sessions"
						+ " WHERE " + dateFilter + " and GATEWAY_ID = '" + gatewayName + "' group by session_duration;";
			}
			else if(!allProjects && !allGateways)
			{
				sqlQuery = "SELECT  session_duration, count(username), SUM(no_of_screens) "
						+ " FROM mod_ia_aggregates_daily_sessions"
						+ " WHERE " + dateFilter + " and GATEWAY_ID = '" + gatewayName + "' and PROJECT_NAME = '" + projectName + "' group by session_duration;";
			}
			
			try {
//				log.error("getEngagementInformationOnController sql q : " + sqlQuery);
				con = ds.getConnection();
			
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getengagementInformationController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			
			
			return freqData;
		}
		
		/*
		 * Functon to retrieve engagement infirmation on Controller
		 * @author - YM - created on 14-Nov-2016.
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getEngagementInformationScreenDepthOnController(java.lang.String, boolean, java.lang.String, boolean, int)
		 */
		public Dataset getEngagementInformationScreenDepthOnController( String projectName,
				boolean allProjects, String gatewayName, boolean allGateways, int duration){
			
			Dataset freqData = null;
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String screensdateFilter = getDateFilterOnController(duration, "c.view_timestamp");
			String auditdateFilter = getDateFilterOnController(duration, "session_start");
			String sqlQuery = "";
			
			if(allProjects && allGateways)
			{
				
				
				sqlQuery = "SELECT count(username), no_of_screens from mod_ia_aggregates_daily_sessions where "
						+ auditdateFilter + " group by NO_OF_SCREENS;";
				
			}
			else if(allProjects && !allGateways)
			{
				
				sqlQuery = "SELECT count(username), no_of_screens from mod_ia_aggregates_daily_sessions where "
						+ auditdateFilter 
						+ " and GATEWAY_ID = '" + gatewayName + "' "
						+ " group by NO_OF_SCREENS;";
				
			}
			else if(!allProjects && allGateways)
			{
//				sqlQuery = "SELECT count(actor), noOfScreens from ( "
//						+ " SELECT count(c.screen_name) 'noOfScreens', b.username as actor,  b.NO_OF_ACTIONS as actions, "
//						+ " b.SESSION_START as SESSION_START, b.SESSION_END as SESSION_END, b.SESSION_DURATION "
//						+ " FROM mod_ia_aggregates_daily_screen_views c RIGHT JOIN (SELECT * from mod_ia_aggregates_daily_sessions where " + auditdateFilter + ") as b"
//						+ " ON  " + screensdateFilter + " and c.PROJECT = '" + projectName + "' and c.view_timestamp < b.SESSION_END and c.view_timestamp >= b.SESSION_START "
//						+ " and c.username = b.username and c.ACTION = 'SCREEN_OPEN' and b.PROJECT_NAME = c.PROJECT and c.GATEWAY_ID = b.GATEWAY_ID"
//						+ " group by b.username,SESSION_START ,SESSION_END ) as anotherTable"
//						+ " group by noOfScreens; ";
				sqlQuery = "SELECT count(username), no_of_screens from mod_ia_aggregates_daily_sessions where "
						+ auditdateFilter 
						+ " and PROJECT_NAME = '" + projectName + "'"
						+ " group by NO_OF_SCREENS;";
			}
			else if(!allProjects && !allGateways)
			{
//				sqlQuery = "SELECT count(actor), noOfScreens from ( "
//						+ " SELECT count(c.screen_name) 'noOfScreens', b.username as actor,  b.NO_OF_ACTIONS as actions, "
//						+ " b.SESSION_START as SESSION_START, b.SESSION_END as SESSION_END, b.SESSION_DURATION "
//						+ " FROM mod_ia_aggregates_daily_screen_views c RIGHT JOIN (SELECT * from mod_ia_aggregates_daily_sessions where " + auditdateFilter + ") as b"
//						+ " ON  " + screensdateFilter + " and c.PROJECT = '" + projectName + "' and C.GATEWAY_ID = '" + gatewayName + "' "
//						+ " and c.view_timestamp < b.SESSION_END and c.view_timestamp >= b.SESSION_START "
//						+ " and c.username = b.username and c.ACTION = 'SCREEN_OPEN' and b.PROJECT_NAME = c.PROJECT and c.GATEWAY_ID = b.GATEWAY_ID"
//						+ " group by b.username,SESSION_START ,SESSION_END ) as anotherTable"
//						+ " group by noOfScreens; ";
				sqlQuery = "SELECT count(username), no_of_screens from mod_ia_aggregates_daily_sessions where "
						+ auditdateFilter 
						+ " and PROJECT_NAME = '" + projectName + "' and GATEWAY_ID = '" + gatewayName + "' "
						+ " group by NO_OF_SCREENS;";
			}
			try {
				con = ds.getConnection();
//				log.error("getEngagementInformationScreenDepthOnController : " + sqlQuery);
				freqData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getEngagementInfrmationScreenDepthController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			
			return freqData;

		}
		
		/**
		 * Method to retrieve the overview information for given duration and given project/all projects
		 * added by YM : 14-Nov-2016
		 */
		@Override
		public OverviewInformation getYesterdayOverviewOnController( int duration,
				String projectName, boolean allProjects, String gatewayName, boolean allGateways) {
				
				OverviewInformation info = new OverviewInformation();
			
				Datasource ds;
				Dataset resDS = null;
				Dataset resDS1 = null;
				String startDate = "", endDate = "";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				SRConnection con = null;
						
				String sqlQuery = "";
				String sqlQueryTotalUsers = "";
				String dateFilterAuditEvents = getDateFilterOnController(duration, "START_TIMESTAMP");
				String dateFilter = getDateFilterOnController(duration, "session_start");
				String screenViewDateFilter = getDateFilterOnController(duration, "a.VIEW_TIMESTAMP");
				String sessionDateFilter = getDateFilterOnController(duration, "s.session_start");
				String sqlScreensQuery = "";
				String usersQuery = "";
				String filter = "";
				String filter1 = "";
				
				if(allProjects && allGateways)
				{
					filter = filter1 = "";
				}
				else if(allProjects && !allGateways)
				{
					filter = " and GATEWAY_ID = '" + gatewayName + "' ";	
					filter1 = " and a.GATEWAY_ID = '" + gatewayName + "' ";	
				}
				else if(!allProjects && allGateways)
				{
					filter = " and PROJECT_NAME = '" + projectName + "' ";	
					filter1 = " and a.PROJECT = '" + projectName + "' ";	
				}
				else if(!allProjects && !allGateways)
				{
					filter = " and GATEWAY_ID = '" + gatewayName + "' and PROJECT_NAME = '" + projectName + "' ";
					filter1 = " and a.GATEWAY_ID = '" + gatewayName + "' and a.PROJECT = '" + projectName + "' ";
				}
				int noOfUsers = this.getNumberOfActiveUsersOnController(duration, gatewayName, projectName, allGateways, allProjects);
				
				sqlQuery = "select sum(no_of_screens+no_of_actions) as ACTIONS,"
						+ " time_format(SEC_TO_TIME(AVG(time_to_sec(session_end)-time_to_sec(session_start))), '%H:%i:%s') as AVG_SESSION_DURATION,"
						+ " (SUM(no_of_screens+no_of_actions)/count(session_start)) as Actions_PER_SESSION,"
						+ " count(session_start) as sessions"
						+ " from mod_ia_aggregates_daily_sessions  WHERE "
						+ dateFilter + filter + ";";
				log.error(sqlQuery);
				
				sqlScreensQuery = "SELECT count(a.SCREEN_NAME) as noOfScreenViews"
						 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
						 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter + filter1
						 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID and a.VIEW_TIMESTAMP >= s.SESSION_START "
						 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME "
						 + " GROUP BY a.username having count(screen_name) = 1;";
				
				sqlQueryTotalUsers = "SELECT count(distinct(concat(a.USERNAME,b.AUTH_PROFILE,a.GATEWAY_ID)))"
						+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
						+ " where a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID "
						+ " and "
						+ dateFilterAuditEvents + filter1 + " ;"; 
				
//				log.error("getYesterdayOverviewOnController: "+sqlQuery);
//				log.error(" getYesterdayOverview Total Users: "+sqlQueryTotalUsers);
//				log.error("getYesterdayOverviewOnController screen: "+sqlScreensQuery);
				
				try {
					
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							info.setNoOfActions((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
						}
						if(resDS.getValueAt(0, 1) != null)
						{
							info.setAvgSessionDuration(resDS.getValueAt(0, 1).toString());
						}
						if(resDS.getValueAt(0, 2) != null)
						{
							info.setActionsPerSession(Float.parseFloat(resDS.getValueAt(0, 2).toString()));
						}
						if(resDS.getValueAt(0, 3) != null)
						{
							info.setNoOfSessions((int)(Float.parseFloat(resDS.getValueAt(0, 3).toString())));
						}
						
					}
					
					resDS = con.runQuery(sqlScreensQuery);
					float usersWithOneScreen = 0;
					float totalUsers = 0;
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							usersWithOneScreen = Float.parseFloat(resDS.getValueAt(0, 0).toString());
						}

					}
					
					resDS = con.runQuery(sqlQueryTotalUsers);
				
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							totalUsers = Float.parseFloat(resDS.getValueAt(0, 0).toString());
							info.setNoOfActiveUsers((int) totalUsers);
						}
					}
					if(usersWithOneScreen == 0 || totalUsers == 0)
					{
						info.setBounceRate(0);
					}
					else
					{
						info.setBounceRate(usersWithOneScreen/totalUsers);
					}
					
					String sqlScreensQueryTotal = "SELECT count(x.SCREEN_NAME) as noOfScreenViews FROM "
							+ "	mod_ia_aggregates_projects b,"
							+ " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project, a.GATEWAY_ID as GATEWAY_ID"
							 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
							 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter + filter1
							 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID and a.VIEW_TIMESTAMP >= s.SESSION_START "
							 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
							 + " where x.GATEWAY_ID = b.GATEWAY_ID and x.PROJECT = b.PROJECT_NAME;";
					resDS = null;
					resDS = con.runQuery(sqlScreensQueryTotal);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							info.setNoOfScreenViews((int)Float.parseFloat(resDS.getValueAt(0, 0).toString()));
						}
						else
						{
							info.setNoOfScreenViews(0);
						}

					}
					else
					{
						info.setNoOfScreenViews(0);
					}
					
				}
				catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getYesterdayOverviewOnController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
				
			return info;
		}
		
		/*
		 * Added by Yogini on 14-nov-2016
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getDeviceInformationOnController(int, java.lang.String, boolean, java.lang.String, boolean)
		 */
		@Override
		public DevicesInformation getDeviceInformationOnController( int duration,
				String projectName, boolean allProjects, String gatewayName, boolean allGateways) {
		
			Datasource ds;
			Dataset resDS = null;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			DevicesInformation retInfo = new DevicesInformation();
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilterOnController(duration, "START_TIMESTAMP");
			String filter = "";
			
			if(allProjects && allGateways)
			{
				filter = "";				
			}
			else if(allProjects && !allGateways)
			{
				filter = " and a.GATEWAY_ID = '" + gatewayName + "' ";				
			}
			else if(!allProjects && allGateways)
			{
				filter = " and a.PROJECT_NAME = '" + projectName + "' ";				
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and a.GATEWAY_ID = '" + gatewayName + "' and a.PROJECT_NAME = '" + projectName + "' ";
			}
			
			
			sqlQuery = "select case"
					+ " when b.IS_MOBILE = 0 then 'Desktop' "
					+ " when b.IS_MOBILE = 1 then 'Mobile' end as 'deviceType', count(distinct session_start) as Sessions "
					+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b where "
					+ dateFilter + filter
					+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
					+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID and a.session_start != a.session_end"
					+ " and b.CLIENT_CONTEXT = a.SESSION_CONTEXT and a.username = b.username and a.HOSTNAME = b.HOSTNAME group by IS_MOBILE;";
			
//			log.error("getDeviceInformationOnController: "+sqlQuery);
			
			int r=0, noOfRows;
				try {
					
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						if(resDS != null && resDS.getRowCount() > 0)
						{
							noOfRows = resDS.getRowCount();
							for(r=0; r<noOfRows; r++)
							{
								if(resDS.getValueAt(r, 0).toString().compareToIgnoreCase("Desktop") == 0)
								{
									retInfo.setNoOfClientsOnDesktop(Integer.parseInt(resDS.getValueAt(r, 1).toString()));
								}
								else
								{
									retInfo.setNoOfClientsOnMobile(Integer.parseInt(resDS.getValueAt(r, 1).toString()));
								}
							}
							
						}
						
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getDeviceINformationOnCOntroller : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
		return retInfo;
		}
		
		/**
		 * A function to retrieve Top OS information in a given duration
		 * Called from Dashboard Panel on COntroller to display device information in graph
		 * Created by Yogini : 14-Nov-2016 
		 */
		
		@Override
		public Dataset getTopOperatingSystemsOnController( int duration,
				String projectName, boolean allProjects, String gatewayName, boolean allGateways) {
		
			Datasource ds;
			Dataset resDS = null;

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilterOnController(duration, "START_TIMESTAMP");
			
			if(allProjects && allGateways)
			{
				sqlQuery = "select OS_NAME, count( username) as 'Users'"
						+ " from mod_ia_aggregates_clients  "
						+ " where  " + dateFilter
						+ " group by OS_NAME order by Users desc;";
			}
			else if(allProjects && !allGateways)
			{
				sqlQuery = "select OS_NAME, count( username) as 'Users'"
						+ " from mod_ia_aggregates_clients  "
						+ " where  " + dateFilter
						+ " and GATEWAY_ID = '" + gatewayName + "' group by OS_NAME order by Users desc;";
			}
			else if(!allProjects && allGateways)
			{
				sqlQuery = "select OS_NAME, count( username) as 'Users'"
						+ " from mod_ia_aggregates_clients  "
						+ " where  " + dateFilter
						+ " and PROJECT = '" + projectName + "' group by OS_NAME order by Users desc;";
			}
			else if(!allProjects && !allGateways)
			{
				sqlQuery = "select OS_NAME, count( username) as 'Users'"
						+ " from mod_ia_aggregates_clients  "
						+ " where  " + dateFilter
						+ " and GATEWAY_ID = '" + gatewayName + "' and PROJECT = '" + projectName + "' group by OS_NAME order by Users desc;";
			}
			
			
				try {
					
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getTopOSController : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
		return resDS;
		}
		
		/*
		 * Method to return browsers information to be shown on Real time panel - for controller
		 * @author : YM created on : 14-Nov-2016
		 */
		public Dataset getBrowserInformationOnController(int duration, String projectName, boolean allProjects, String gatewayName, boolean allGateways)
		{
			Dataset browserData = null;
			
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilterOnController(duration, "b.start_timestamp");
			String sqlQuery = "";
			
			if(allProjects && allGateways)
			{
				sqlQuery = "select a.browser_name as browser_name, count(a.browser_name) as bCount "
						  + " from "
						  + " mod_ia_aggregates_browser_info a, mod_ia_aggregates_clients b"
						  + " where a.GATEWAY_ID = b.GATEWAY_ID and (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP)  "
						  + " and a.timestamp <= b.start_timestamp and a.timestamp = "
						  + " (select max(timestamp) from mod_ia_aggregates_browser_info "
						  + " where gateway_id = b.GATEWAY_ID and timestamp <= b.start_timestamp) and "
						  + dateFilter
						  + " group by a.browser_name order by bCount desc;";
				
			}
			else if(allProjects && !allGateways)
			{
				sqlQuery = "select a.browser_name as browser_name, count(a.browser_name) as bCount "
						  + " from "
						  + " mod_ia_aggregates_browser_info a, mod_ia_aggregates_clients b"
						  + " where b.GATEWAY_ID = '" + gatewayName + "' and a.GATEWAY_ID = b.GATEWAY_ID and (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP)  "
						  + " and a.timestamp <= b.start_timestamp and a.timestamp = "
						  + " (select max(timestamp) from mod_ia_aggregates_browser_info "
						  + " where gateway_id = b.GATEWAY_ID and timestamp <= b.start_timestamp) and "
						  + dateFilter
						  + " group by a.browser_name order by bCount desc;";
				
			}
			else if(!allProjects && allGateways)
			{
				sqlQuery = "select a.browser_name as browser_name, count(a.browser_name) as bCount "
						  + " from "
						  + " mod_ia_aggregates_browser_info a, mod_ia_aggregates_clients b"
						  + " where b.PROJECT = '" + projectName + "' and a.GATEWAY_ID = b.GATEWAY_ID and (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP)  "
						  + " and a.timestamp <= b.start_timestamp and a.timestamp = "
						  + " (select max(timestamp) from mod_ia_aggregates_browser_info "
						  + " where gateway_id = b.GATEWAY_ID and timestamp <= b.start_timestamp) and "
						  + dateFilter
						  + " group by a.browser_name order by bCount desc;";
			}
			else if(!allProjects && !allGateways)
			{
				sqlQuery = "select a.browser_name as browser_name, count(a.browser_name) as bCount "
						  + " from "
						  + " mod_ia_aggregates_browser_info a, mod_ia_aggregates_clients b"
						  + " where b.GATEWAY_ID = '" + gatewayName + "' and b.PROJECT = '" + projectName + "' and a.GATEWAY_ID = b.GATEWAY_ID and (a.IP_ADDRESS = b.HOST_INTERNAL_IP OR a.IP_ADDRESS = b.HOST_EXTERNAL_IP)  "
						  + " and a.timestamp <= b.start_timestamp and a.timestamp = "
						  + " (select max(timestamp) from mod_ia_aggregates_browser_info "
						  + " where gateway_id = b.GATEWAY_ID and timestamp <= b.start_timestamp) and "
						  + dateFilter
						  + " group by a.browser_name order by bCount desc;";
			}
			
			try {
				
				con = ds.getConnection();
				browserData = con.runQuery(sqlQuery); 
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
			finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getBrowserInformationController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			
			return browserData;
		}

		@Override
		public Dataset getTotalVisitsDataOnController(String projectName,
				boolean allProjects, String gatewayName, boolean allGateways,
				int duration) {
			Dataset totalVisits = null;
			Datasource ds;
			String sqlQuery = "";
			String timeFilter = getDateFilterOnController(duration, "session_start");
			SRConnection con = null;
			
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(allProjects && !allGateways)
			{
				filter = " AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			else if(!allProjects && allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, sum(total_sessions)"
						+ " from mod_ia_hours as a "
						+ " left join (select count(session_start) as total_sessions, hour(SESSION_START) as overview_hour from mod_ia_aggregates_daily_sessions where "
						+ timeFilter + filter + " group by overview_hour) as b"
						+ " on b.overview_hour = a.Hour "
						+ " group by a.Hour order by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, b.total_sessions, a.monthNumber"
							+ " from mod_ia_month as a "
//							+ " left join (select sum(total_sessions) as total_sessions, month(overview_date) as overviewmonth"
//							+ " from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter +  " ) as b"
//							+ " on b.overviewmonth = a.monthNumber"
//							+ " order by a.monthNumber;";
							+ " left join (select count(session_start) as total_sessions, month(session_date) as overviewmonth"
							+ " from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter +  " group by overviewmonth) as b"
							+ " on b.overviewmonth = a.monthNumber"
							+ " order by a.monthNumber"
							+ " ;";
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
//					sqlQuery = "select dayname(overview_date), sum(total_sessions) from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " group by overview_date;";
					sqlQuery = "select dayname(session_date), count(session_start) from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by session_date;";
				}
				else if(duration == Constants.THIS_MONTH ) 
				{
					
					sqlQuery = "select b.dayAll, coalesce(a.total_sessions, 0) from ("
//							+ " select day(overview_date) as dayActual, sum(total_sessions) as total_sessions "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter +  filter
//							+ " group by overview_date ) as a "
							+ " select day(session_date) as dayActual, count(session_start) as total_sessions "
							+ " from mod_ia_aggregates_daily_sessions where " + timeFilter + filter
							+ " group by session_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.total_sessions, 0) from ("
//							+ " select day(overview_date) as dayActual, sum(total_sessions) as total_sessions "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
//							+ filter + " group by overview_date ) as a "
							+ " select day(session_date) as dayActual, count(session_start) as total_sessions "
							+ " from mod_ia_aggregates_daily_sessions where " + timeFilter +filter
							+ " group by session_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else
				{
//					sqlQuery = "select day(overview_date), sum(total_sessions) from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " group by overview_date;";
					sqlQuery = "select day(session_date), count(session_start) from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by session_date order by session_date;";
							
				}
			
			//connect to the database and get records
			try{
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
//				log.error("getTotalVisitsDataController : " + sqlQuery);
				totalVisits = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("getTotalVisitsDataController : " + e);
			}finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getTotalVisitsDataController : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
			return totalVisits;
		}

		@Override
		public Dataset getTotalUsersDataOnController(String projectName,
				boolean allProjects, String gatewayName, boolean allGateways,
				int duration) {
			Dataset totalUsers = null;
			Datasource ds;
			String sqlQuery = "";
			String timeFilter = "";

			timeFilter = getDateFilterOnController(duration, "start_timestamp");
			SRConnection con = null;
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(allProjects && !allGateways)
			{
				filter = " AND a.GATEWAY_ID = '" + gatewayName + "' ";
			}
			else if(!allProjects && allGateways)
			{
				filter = " AND a.PROJECT = '" + projectName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " AND a.PROJECT = '" + projectName + "' AND a.GATEWAY_ID = '" + gatewayName + "' ";
			}
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{

					sqlQuery = "select a.Hour as Hour, total_users" 
							+ " from mod_ia_hours as a  left join "
							+ " ("
							+ "		select count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users,hour(start_timestamp) as event_timestamp"
							+ " 	from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b where " 
							+ " 	a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID "
							+ " 	and "+ timeFilter + filter + ""
							+ " 	group by hour(start_timestamp) )as b"
							+ " on b.event_timestamp = a.Hour" 
							+ " group by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS)
				{
					
//					String sDate = "";
//					Calendar lastHour = Calendar.getInstance();
//				    lastHour.set(Calendar.HOUR_OF_DAY, 23);
//				    lastHour.set(Calendar.MINUTE, 59);
//				    lastHour.set(Calendar.SECOND, 59);
//				    
//				    int currentYear = lastHour.get(Calendar.YEAR);
//					lastHour.add(Calendar.DATE, -365);
//					int yearVal = lastHour.get(Calendar.YEAR);
//					int monthVal = lastHour.get(Calendar.MONTH);
//					sqlQuery = "select a.monthName,total_users"
//							+ " from mod_ia_month as a "
//							+ " left join (select count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users, "
//							+ " month(start_timestamp) as overviewmonth, year(start_timestamp) as overviewyear"
//							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b where "
//							+ " a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID and "
//							+ " ( year(start_timestamp) = " + yearVal + " and month(start_timestamp) >= " + monthVal + ") or ( year(start_timestamp) = " + currentYear + ") "
//							+ filter + " group by month(start_timestamp) ) as b"
//							+ " on b.overviewmonth = a.monthNumber"
//							+ " group by a.monthName order by a.monthNumber, b.overviewyear;";
//					
					sqlQuery = "select a.monthName, total_users, a.monthNumber"
							+ " from mod_ia_month as a "
							+ " left join (select count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users, month(start_timestamp) as overviewmonth"
							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b where "
							+ " a.PROJECT = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID and "
							+ timeFilter + filter + " group by month(start_timestamp) ) as b"
							+ " on b.overviewmonth = a.monthNumber"
							+ " order by a.monthNumber;";
				}
				else if(duration == Constants.LAST_YEAR )
				{
					sqlQuery = "select a.monthName, total_users"
							+ " from mod_ia_month as a "
							+ " left join (select count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users, "
							+ " month(start_timestamp) as overviewmonth, year(start_timestamp) as overviewyear"
							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b where "
							+ " year(start_timestamp) = year(date(now())) - 1 " + filter + " group by month(start_timestamp) ) as b "
							+ " on b.overviewmonth = a.monthNumber"
							+ " group by a.monthName order by a.monthNumber;";
				}
				else if(duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, total_users"
							+ " from mod_ia_month as a "
							+ " left join (select count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users, "
							+ " month(start_timestamp) as overviewmonth, year(start_timestamp) as overviewyear"
							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b where "
							+ " year(start_timestamp) = year(date(now())) " + filter + " group by month(start_timestamp) ) as b "
							+ " on b.overviewmonth = a.monthNumber"
							+ " group by a.monthName order by a.monthNumber;";

				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
					
				sqlQuery = "select dayname(start_timestamp) as overviewmonth, count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users, start_timestamp "
						+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b where "
						+ " a.PROJECT = b.PROJECT_NAME "
						+ " and a.GATEWAY_ID = b.GATEWAY_ID and "
						+ timeFilter + filter
						+ " group by dayname(start_timestamp)"
						+ " order by start_timestamp;"; 
					
				}
				else if(duration == Constants.THIS_MONTH ) 
				{
					
					sqlQuery = "select b.dayAll, coalesce(a.total_users, 0) from ("
							+ " select day(start_timestamp) as dayActual, count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users "
							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b  where " 
							+ " a.PROJECT = b.PROJECT_NAME "
							+ " and a.GATEWAY_ID = b.GATEWAY_ID and "
							+ timeFilter + filter
							+ " group by day(start_timestamp) ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.total_users, 0) from ("
							+ " select day(start_timestamp) as dayActual, count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users "
							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b  where " 
							+ " a.PROJECT = b.PROJECT_NAME "
							+ " and a.GATEWAY_ID = b.GATEWAY_ID and "
							+ timeFilter + filter
							+ " group by day(start_timestamp )) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_THIRTY_DAYS )
				{
					sqlQuery = "select day(start_timestamp) as dayActual, count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users "
							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b  where " 
							+ " a.PROJECT = b.PROJECT_NAME "
							+ " and a.GATEWAY_ID = b.GATEWAY_ID and "
							+ timeFilter  + filter
							+ " group by day(start_timestamp) order by start_timestamp;";
				}
				else
				{
					
					sqlQuery = "select day(start_timestamp) as dayActual, count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users "
							+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b  where " 
							+ " a.PROJECT = b.PROJECT_NAME "
							+ " and a.GATEWAY_ID = b.GATEWAY_ID and "
							+ timeFilter + filter
							+ " group by day(start_timestamp) order by start_timestamp;";
							
				}
//				log.error("\n\n getTotalUsersDataOnController: "+sqlQuery);
			//connect to the database and get records
			try{
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				totalUsers = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("getTotalUsersDataOnController : " + e);
			}finally{
				//close the database connection 
				DBUtilities.close(con);
			}
			return totalUsers;
		}

		@Override
		public Dataset getTotalScreenViewsDataOnController(String projectName,
				boolean allProjects, String gatewayName, boolean allGateways,
				int duration) {
			Dataset totalScreenviews = null;
			Datasource ds;
			String sqlQuery = "";
			String timeFilter = getDateFilterOnController(duration, "session_start");
			SRConnection con = null;
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(allProjects && !allGateways)
			{
				filter = " AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			else if(!allProjects && allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' AND GATEWAY_ID = '" + gatewayName + "' ";
			}
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, sum(TOTAL_SCREENVIEWS) as TOTAL_SCREENVIEWS"
						+ " from mod_ia_hours as a "
//						+ " left join (select TOTAL_SCREENVIEWS, overview_hour from mod_ia_aggregates_hourly_overview where "
//						+ timeFilter + filter + " ) as b"
//						+ " on b.overview_hour = a.Hour"
//						+ " group by a.Hour;";
						+ " left join (select sum(NO_OF_SCREENS) as TOTAL_SCREENVIEWS, hour(session_start) as overview_hour  from mod_ia_aggregates_daily_sessions where "
						+ timeFilter + filter + " group by overview_hour ) as b"
						+ " on b.overview_hour = a.Hour"
						+ " group by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, TOTAL_SCREENVIEWS, a.monthNumber"
							+ " from mod_ia_month as a "
//							+ " left join (select sum(TOTAL_SCREENVIEWS) as TOTAL_SCREENVIEWS, month(overview_date) as overviewmonth"
//							+ " from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " ) as b"
//							+ " on b.overviewmonth = a.monthNumber"
//							+ " order by a.monthNumber;";
							+ " left join (select sum(NO_OF_SCREENS) as TOTAL_SCREENVIEWS, month(session_date) as overviewmonth"
							+ " from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by month(session_date) ) as b"
							+ " on b.overviewmonth = a.monthNumber"
							+ " order by a.monthNumber"
							+ " ;";
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
					sqlQuery = "select dayname(session_date), sum(NO_OF_SCREENS) from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by session_date;";
				}
				else if(duration == Constants.THIS_MONTH ) 
				{
					
					sqlQuery = "select b.dayAll, coalesce(a.TOTAL_SCREENVIEWS, 0) from ("
//							+ " select day(overview_date) as dayActual, sum(TOTAL_SCREENVIEWS) as TOTAL_SCREENVIEWS "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
//							+ filter + " group by overview_date ) as a "
							+ " select day(session_date) as dayActual, sum(NO_OF_SCREENS) as TOTAL_SCREENVIEWS "
							+ " from mod_ia_aggregates_daily_sessions where " + timeFilter + filter
							+ " group by session_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.TOTAL_SCREENVIEWS, 0) from ("
//							+ " select day(overview_date) as dayActual, sum(TOTAL_SCREENVIEWS) as TOTAL_SCREENVIEWS "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter + filter  
//							+ " group by overview_date ) as a "
							+ " select day(session_date) as dayActual, sum(NO_OF_SCREENS) as TOTAL_SCREENVIEWS "
							+ " from mod_ia_aggregates_daily_sessions where " + timeFilter + filter
							+ " group by session_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
//				else if(duration == Constants.LAST_THIRTY_DAYS )
//				{
//					sqlQuery = "select day(overview_date), sum(TOTAL_SCREENVIEWS) from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " group by overview_date;";
//				}
				else
				{
					sqlQuery = "select day(session_date), sum(NO_OF_SCREENS) from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by session_date"
									+ " order by session_date;";
							
				}
				

			
			//connect to the database and get records
			try{
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
//				log.error("getScreenviewsDataOnController : " + sqlQuery);
				totalScreenviews = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("getScreenviewsDataOnController : " + e);
			}finally{
				//close the database connection 
				DBUtilities.close(con);
			}
			return totalScreenviews;
		}

		@Override
		public Dataset getBounceRateDataOnController(String projectName,
				boolean allProjects, String gatewayName, boolean allGateways,
				int duration) {
			Dataset bounceRates = null;
			Datasource ds;
			String sqlQuery = "";
			String timeFilter = getDateFilterOnController(duration, "overview_date");
			SRConnection con = null;
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(allProjects && !allGateways)
			{
				filter = " AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			else if(!allProjects && allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, avg(bounceRate) as bounceRate"
						+ " from mod_ia_hours as a "
						+ " left join (select (bounce_rate  * 100 ) as bounceRate, overview_hour from mod_ia_aggregates_hourly_overview where "
						+ timeFilter + filter + " ) as b"
						+ " on b.overview_hour = a.Hour"
						+ " group by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, bounceRate, a.monthNumber"
							+ " from mod_ia_month as a "
							+ " left join (select (avg(bounce_rate)  * 100) as bounceRate, month(overview_date) as overviewmonth"
							+ " from mod_ia_aggregates_daily_overview where "
							+ timeFilter + filter + " group by overviewmonth ) as b"
							+ " on b.overviewmonth = a.monthNumber"
							+ " order by a.monthNumber;";
//					log.error("getBounceRateDataOnController last365 sql q : " + sqlQuery);
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
					sqlQuery = "select dayname(overview_date), (avg(bounce_rate)  * 100) from mod_ia_aggregates_daily_overview where "
							+ timeFilter + filter + " group by overview_date order by overview_date;";
				}

				
				else if(duration == Constants.THIS_MONTH ) 
				{
					
					sqlQuery = "select b.dayAll, coalesce(a.bounce_rate, 0) from ("
							+ " select day(overview_date) as dayActual,(avg(bounce_rate)  * 100 ) as bounce_rate "
							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
							+ filter + " group by overview_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.bounce_rate, 0) from ("
							+ " select day(overview_date) as dayActual, (avg(bounce_rate)  * 100  ) as bounce_rate "
							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
							+ filter + " group by overview_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_THIRTY_DAYS )
				{
					sqlQuery = "select day(overview_date), (avg(bounce_rate)  * 100  ) from mod_ia_aggregates_daily_overview where "
							+ timeFilter + filter + " group by overview_date"
									+ " order by overview_date;";
				}
				else
				{
					sqlQuery = "select day(overview_date), (avg(bounce_rate) * 100 ) from mod_ia_aggregates_daily_overview where "
							+ timeFilter + filter + " group by overview_date"
									+ "order by overview_date;";
							
				}
				
			
			
			//connect to the database and get records
			try{
				
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
//				log.error("getBounceRateDataOnController : " + sqlQuery);
				bounceRates = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("getBounceRateDataOnController : " + e);
			}finally{
				//close the database connection 
				DBUtilities.close(con);
			}
			return bounceRates;
		}

		@Override
		public Dataset getAvgSessionDataOnController(String projectName,
				boolean allProjects, String gatewayName, boolean allGateways,
				int duration) {
			Dataset avgSessions = null;
			Datasource ds;
			String sqlQuery = "";
			String timeFilter = getDateFilterOnController(duration, "session_start");
			SRConnection con = null;
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(allProjects && !allGateways)
			{
				filter = " AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			else if(!allProjects && allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{

					sqlQuery = "select a.Hour as Hour, avg(AVG_SESSION_DURATION) as AVG_SESSION_DURATION "
							+ " from mod_ia_hours as a "
//							+ " left join (select AVG_SESSION_DURATION/60 as AVG_SESSION_DURATION, overview_hour from mod_ia_aggregates_hourly_overview where "
//							+ timeFilter + filter + " ) as b"
//							+ " on b.overview_hour = a.Hour"
//							+ "  group by a.Hour;";
							+ " left join (select "
							+ "AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION,"
							+ " hour(session_start) as overview_hour from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by overview_hour ) as b"
							+ " on b.overview_hour = a.Hour"
							+ "  group by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, AVG_SESSION_DURATION, a.monthNumber"
							+ " from mod_ia_month as a "
//							+ " left join (select (AVG(AVG_SESSION_DURATION))/60 as AVG_SESSION_DURATION, month(overview_date) as overviewmonth"
//							+ " from mod_ia_aggregates_daily_overview where "
//							+ timeFilter +  filter + " ) as b"
//							+ " on b.overviewmonth = a.monthNumber"
//							+ " order by a.monthNumber;";
							+ " left join (select "
							+ "AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION,"
							+ " month(session_date) as overviewmonth"
							+ " from mod_ia_aggregates_daily_sessions where "
							+ timeFilter +  filter + " group by overviewmonth ) as b"
							+ " on b.overviewmonth = a.monthNumber"
							+ "  order by a.monthNumber;";
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
//					sqlQuery = "select dayname(overview_date), (AVG(AVG_SESSION_DURATION))/60 as AVG_SESSION_DURATION from mod_ia_aggregates_daily_overview where "
//							+ timeFilter +  filter + " group by overview_date;";
					sqlQuery = "select dayname(session_date), "
							+ " AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION"
							+ " from mod_ia_aggregates_daily_sessions where "
							+ timeFilter +  filter + " group by session_date order by session_date;";
				}

				
				else if(duration == Constants.THIS_MONTH ) 
				{
					
					sqlQuery = "select b.dayAll, coalesce(a.AVG_SESSION_DURATION, 0) from ("
//							+ " select day(overview_date) as dayActual, (AVG(AVG_SESSION_DURATION))/60 as AVG_SESSION_DURATION "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
//							+  filter + " group by overview_date ) as a "
							+ " select day(session_date) as dayActual, "
							+ "AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION "
							+ " from mod_ia_aggregates_daily_sessions where " + timeFilter + filter
							+ " group by session_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.AVG_SESSION_DURATION, 0) from ("
//							+ " select day(overview_date) as dayActual, (AVG(AVG_SESSION_DURATION))/60 as AVG_SESSION_DURATION "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
//							+ filter +  " group by overview_date ) as a "
							+ " select day(session_date) as dayActual, "
							+ "AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION "
							+ " from mod_ia_aggregates_daily_sessions where " + timeFilter + filter
							+ " group by session_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
//				else if(duration == Constants.LAST_THIRTY_DAYS )
//				{
//					sqlQuery = "select day(overview_date), (AVG(AVG_SESSION_DURATION))/60 as AVG_SESSION_DURATION from mod_ia_aggregates_daily_overview where "
//							+ timeFilter +  filter + " group by overview_date;";
//				}
				else
				{
//					sqlQuery = "select day(overview_date), (AVG(AVG_SESSION_DURATION))/60 as AVG_SESSION_DURATION from mod_ia_aggregates_daily_overview where "
//							+ timeFilter +  filter + " group by overview_date;";
					sqlQuery = "select day(session_date), "
							+ "AVG(time_to_sec(session_end)-time_to_sec(session_start))/60 as AVG_SESSION_DURATION"
							+ " from mod_ia_aggregates_daily_sessions where "
							+ timeFilter +  filter + " group by session_date"
									+ " order by session_date;";
							
				}
				
			
			//connect to the database and get records
			try{
				
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
//				log.error("getAvgSessionDataOnController : " + sqlQuery);
				avgSessions = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("getAvgSessionDataOnController : " + e);
			}finally{
				//close the database connection 
				DBUtilities.close(con);
			}
			return avgSessions;
		}

		@Override
		public Dataset getAvgScreenViewsDataOnController(String projectName,
				boolean allProjects, String gatewayName, boolean allGateways,
				int duration) {
			Dataset avgScreenViews = null;
			Datasource ds;
			String sqlQuery = "";
			String timeFilter = getDateFilterOnController(duration, "session_start");
			SRConnection con = null;
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(allProjects && !allGateways)
			{
				filter = " AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			else if(!allProjects && allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " AND PROJECT_NAME = '" + projectName + "' AND GATEWAY_ID = '" + gatewayName + "' ";
			}
			
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, avg(SCREENS_PER_SESSION) as SCREENS_PER_SESSION"
						+ " from mod_ia_hours as a "
//						+ " left join (select SCREENS_PER_SESSION, overview_hour from mod_ia_aggregates_hourly_overview where "
//						+ timeFilter + filter + " ) as b"
//						+ " on b.overview_hour = a.Hour"
//						+ "  group by a.Hour;";
					+ " left join (select (SUM(no_of_screens)/count(session_start)) as SCREENS_PER_SESSION,"
					+ " hour( session_start) as overview_hour from mod_ia_aggregates_daily_sessions where "
					+ timeFilter + filter + " group by overview_hour ) as b"
					+ " on b.overview_hour = a.Hour"
					+ "  group by a.Hour;";
				}
				else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR )
				{
					sqlQuery = "select a.monthName, SCREENS_PER_SESSION, a.monthNumber"
							+ " from mod_ia_month as a "
//							+ " left join (select AVG(SCREENS_PER_SESSION) as SCREENS_PER_SESSION, month(overview_date) as overviewmonth"
//							+ " from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " ) as b"
//							+ " on b.overviewmonth = a.monthNumber"
//							+ " order by a.monthNumber;";
						+ " left join (select AVG((SUM(no_of_screens)/count(session_start))) as SCREENS_PER_SESSION,"
						+ " month(session_date) as overviewmonth"
						+ " from mod_ia_aggregates_daily_sessions where "
						+ timeFilter + filter + " group by month(session_date) ) as b"
						+ " on b.overviewmonth = a.monthNumber"
						+ " order by a.monthNumber"
						+ " ;";
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
//					sqlQuery = "select dayname(overview_date), AVG(SCREENS_PER_SESSION) from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " group by overview_date;";
					sqlQuery = "select dayname(session_date),"
							+ " AVG((SUM(no_of_screens)/count(session_start))) from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by session_date"
									+ " order by session_date;";
				}

				else if(duration == Constants.THIS_MONTH ) 
				{
					
					sqlQuery = "select b.dayAll, coalesce(a.SCREENS_PER_SESSION, 0) from ("
//							+ " select day(overview_date) as dayActual, AVG(SCREENS_PER_SESSION) as SCREENS_PER_SESSION "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
//							+ filter + " group by overview_date ) as a "
							+ "( select day(session_date) as dayActual, AVG(SCREENS_PER_SESSION) as SCREENS_PER_SESSION"
							+ " from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter
							+ " group by session_date ) as a"
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-1) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = MONTH(NOW())) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
				else if(duration == Constants.LAST_MONTH )
				{
					sqlQuery = "select b.dayAll, coalesce(a.SCREENS_PER_SESSION, 0) from ("
//							+ " select day(overview_date) as dayActual, AVG(SCREENS_PER_SESSION) as SCREENS_PER_SESSION "
//							+ " from mod_ia_aggregates_daily_overview where " + timeFilter 
//							+ filter + " group by overview_date ) as a "
							+ "( select day(session_date) as dayActual,"
							+ " AVG((SUM(no_of_screens)/count(session_start))) as SCREENS_PER_SESSION"
							+ " from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter
							+ " group by session_date ) as a "
							+ " right outer join "
							+ " (SELECT day(date_field) as dayAll from ( "
							+ " SELECT MAKEDATE(YEAR(NOW()),1) + "
							+ " INTERVAL (MONTH(NOW())-2) MONTH + "
							+ " INTERVAL daynum DAY date_field FROM ("
							+ " SELECT t*10+u daynum FROM "
							+ "  (SELECT 0 t UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) A,"
							+ " (SELECT 0 u UNION SELECT 1 UNION SELECT 2 UNION SELECT 3"
							+ " UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7"
							+ " UNION SELECT 8 UNION SELECT 9) B"
							+ " ORDER BY daynum"
							+ " ) AA"
							+ " ) AAA"
							+ " WHERE MONTH(date_field) = (MONTH(NOW()) - 1)) as b on a.dayActual = b.dayAll"
							+ " order by b.dayAll;"
							;
				}
//				else if(duration == Constants.LAST_THIRTY_DAYS )
//				{
//					sqlQuery = "select day(overview_date), AVG(SCREENS_PER_SESSION) from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " group by overview_date;";
//				}
				else
				{
//					sqlQuery = "select day(overview_date), AVG(SCREENS_PER_SESSION) from mod_ia_aggregates_daily_overview where "
//							+ timeFilter + filter + " group by overview_date;";
					sqlQuery = "select day(session_date),"
							+ " AVG((SUM(no_of_screens)/count(session_start))) from mod_ia_aggregates_daily_sessions where "
							+ timeFilter + filter + " group by session_date"
									+ " order by session_date;";
							
				}
				
			//connect to the database and get records
			try{
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
//				log.error("getAvgScreenViewsDataOnController : " + sqlQuery);
				avgScreenViews = con.runQuery(sqlQuery);
			}
			catch(Exception e){
				log.error("getAvgScreenViewsDataOnController : " + e);
			}finally{
				//close the database connection 
				DBUtilities.close(con);
			}
			return avgScreenViews;
		}

		
		@Override
		public boolean receiveProjects(int operation, String gatewayID,
				List<Projects_Sync> projects) {
			boolean retVal = true;
			Datasource ds;
			String query = "", selectQuery = "";
			SRConnection con = null;
			Dataset resDS = null;
			
			int size = 0, i=0;
			
			if(this.isGatewayMonitored(gatewayID))
			{
				if(projects != null )
				{
					size = projects.size();
				}
				
				if(size > 0)
				{
					try{
						ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
						con = ds.getConnection();
						if(operation == Constants.PROJECTS_DELETE)
						{
							query = "DELETE FROM MOD_IA_AGGREGATES_PROJECTS WHERE GATEWAY_ID = '" + gatewayID + "' and PROJECT_NAME NOT IN (";
							for(i=0; i<size; i++)
							{
								if(i==0)
								{
									query = query + "'" + projects.get(i).projectName + "'";
								}
								else
								{
									query = query + ", '" + projects.get(i).projectName + "'";
								}
							}
							query = query + ");";
							con.runUpdateQuery(query);	
						}
						else if(operation == Constants.PROJECTS_INSERT)
						{
							
							for(i=0; i<size; i++)
							{
								//first check if record exists
								selectQuery = "SELECT PROJECT_NAME FROM MOD_IA_AGGREGATES_PROJECTS where "
										+ " GATEWAY_ID = '" + gatewayID + "' AND"
										+ " PROJECT_NAME = '" +  projects.get(i).projectName  + "' AND"
										+ " AUTH_PROFILE = '" + projects.get(i).authProfile + "';";
								resDS = con.runQuery(selectQuery);
								//insert only when record does not exist
								if(resDS == null || resDS.getRowCount() == 0)
								{
									query = "INSERT INTO MOD_IA_AGGREGATES_PROJECTS (`AUTH_PROFILE`, `GATEWAY_ID`, `PROJECT_NAME`) VALUES ("
											+ "'" + projects.get(i).authProfile + "', '" + gatewayID 
											+ "' , '" + projects.get(i).projectName  + "');";
									
//									log.error("insert aggregates project q : " + query);
									con.runUpdateQuery(query);
								}
							}
							
						}
					
					}
					catch(Exception e){
						log.error("receiveProjects : " + e);
						retVal = false;
					}finally{
					//close the database connection 
					DBUtilities.close(con);
					}
				}
			}
			return retVal;
		}

		@Override
		public boolean updateGANServer(String serverId, String serverName,
				String ServerState) {
			boolean retVal = true;
			
			Datasource ds;
			SRConnection con = null;
			Dataset data = null;
			String query , insertQ = "";
			try
			{
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				
				data = con.runQuery("SELECT GAN_ServerId from MOD_IA_GATEWAYS WHERE GAN_ServerId = '" + serverId + "';");
				
				if(data != null && data.getRowCount() > 0)
				{
					query = "UPDATE MOD_IA_GATEWAYS set GAN_ServerName = '" + serverName + "' , GAN_ServerState = '" + ServerState + "'"
							+ " WHERE GAN_ServerId = '" + serverId + "';";
					log.error("updateGANServer : sql q is : " + query);
					con.runUpdateQuery(query);
				}
				else
				{
					query = "INSERT INTO MOD_IA_GATEWAYS(GAN_ServerId, GAN_ServerName, GAN_ServerState) VALUES ('" + serverId 
							+ "', '" + serverName  + "' , '" + ServerState + "');";
					log.error("updateGANServer : sql q is : " + query);
					con.runUpdateQuery(query);
					if(ServerState.compareToIgnoreCase("Connected") == 0)
					{
						insertQ = "INSERT INTO MOD_IA_MONITORED_GATEWAYS(GAN_ServerName) values ('"
							 + serverName + "');";
						log.error("insert MOD_IA_MONITORED_GATEWAYS server query : " + insertQ);
						con.runUpdateQuery(insertQ);
					}
					
				}
					
				
			}
			catch(Exception e){
			log.error("updateGANServer : " + e);
			retVal = false;
			}finally{
				//close the database connection 
				DBUtilities.close(con);
			}
			
			
			return retVal;
		}

		@Override
		public void persisteLastSynchTimes() {
			
			Datasource ds;
			SRConnection con = null;
			Dataset data = null;
			String query = "";
			
			try
			{
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				LastSyncData _l = LastSyncData.getInstance(ds);	
			
				query = "UPDATE mod_ia_last_sync ";
				if(_l.getLast_sync_browsers_timestamp() != null)
				{
					query = query + " set last_sync_browsers_timestamp = '" + _l.getLast_sync_browsers_timestamp() + "' , "; 
				}
				else
				{
					 query = query + " set last_sync_browsers_timestamp = null , " ;
				}
				
				if(_l.getLast_sync_daily_alarms_date() != null)
				{
					query = query + " last_sync_daily_alarms_date = '" + _l.getLast_sync_daily_alarms_date() + "' , ";
				}
				else
				{
					query = query + " last_sync_daily_alarms_date = null , ";
				}
						
				if(_l.getLast_sync_daily_aUsers_date() != null)
				{
					query = query + " last_sync_daily_aUsers_date = '" + _l.getLast_sync_daily_aUsers_date() + "' , ";
				}
				else
				{
					query = query + " last_sync_daily_aUsers_date = null ,";
				}
						
				if( _l.getLast_sync_date() != null)
				{
					query = query + " last_sync_date = '" + _l.getLast_sync_date() + "', ";
				}
				else
				{
					query = query + " last_sync_date = null, ";
				}
						
				if(_l.getLast_sync_hour() != null)
				{
					query = query + " last_sync_hour = " + _l.getLast_sync_hour() + ", ";
				}
				else
				{
					query = query	+ " last_sync_hour = null , ";
				}
						
				if( _l.getLast_sync_hourly_alarms_date() != null)
				{
					query = query + " last_sync_hourly_alarms_date = '" + _l.getLast_sync_hourly_alarms_date() + "' , ";
				}
				else
				{
					query = query + " last_sync_hourly_alarms_date = null , ";
				}
						
				if( _l.getLast_sync_hourly_alarms_hour() != null)
				{
					query = query + " last_sync_hourly_alarms_hour = " + _l.getLast_sync_hourly_alarms_hour() + " , ";
				}
				else
				{
					query = query + " last_sync_hourly_alarms_hour = null  , ";
				}
						 
				if( _l.getLast_sync_hourly_aUsers_date() != null)
				{
					query = query + " last_sync_hourly_aUsers_date = '" + _l.getLast_sync_hourly_aUsers_date() + "' , ";
				}
				else
				{
					query = query + " last_sync_hourly_aUsers_date = null , ";
				}
						
				if(_l.getLast_sync_hourly_aUsers_hour() != null)
				{
					query = query + " last_sync_hourly_aUsers_hour = " + _l.getLast_sync_hourly_aUsers_hour() + " , ";
				}
				else
				{
					query = query + " last_sync_hourly_aUsers_hour = null , ";	
				}
						
				if( _l.getLast_sync_monthly_aUsers_month() != null)
				{
					query = query + " last_sync_monthly_aUsers_month = " + _l.getLast_sync_monthly_aUsers_month() + " , ";
				}
				else
				{
					query = query + " last_sync_monthly_aUsers_month = null , ";
				}
						
				if( _l.getLast_sync_monthly_aUsers_year() != null)
				{
					query = query + " last_sync_monthly_aUsers_year = " + _l.getLast_sync_monthly_aUsers_year() + " , ";
				}
				else
				{
					query = query + " last_sync_monthly_aUsers_year = null, ";
				}
						
				if( _l.getLast_sync_screen_timestamp() != null)
				{
					query = query + " last_sync_screen_timestamp = '" + _l.getLast_sync_screen_timestamp() + "' , ";
				}
				else
				{
					query = query + " last_sync_screen_timestamp = null , ";
				}
						
				if(_l.getLast_synch_client_start_timestamp() != null)
				{
					query = query+ " last_synch_client_start_timestamp = '" + _l.getLast_synch_client_start_timestamp() + "' ,";
				}
				else
				{
					query = query+ " last_synch_client_start_timestamp = null , ";
				}
				
				if(_l.getLast_sync_audit_date() != null)
				{
					if(_l.getLast_sync_audit_date().contains("."))
					{
						query = query+ " last_sync_audit = '" + _l.getLast_sync_audit_date().substring(0, 19) + "' ,";
					}
					else
					{
						query = query+ " last_sync_audit = '" + _l.getLast_sync_audit_date() + "' ,";
					}
					
				}
				else
				{
					query = query+ " last_sync_audit = null , ";
				}
				if(_l.getLast_sync_sessions_timestamp() != null)
				{
					if(_l.getLast_sync_sessions_timestamp().contains("."))
					{
						query = query+ " last_sync_sessions_timestamp = '" + _l.getLast_sync_sessions_timestamp().substring(0, 19) + "' ;";
					}
					else
					{
						query = query+ " last_sync_sessions_timestamp = '" + _l.getLast_sync_sessions_timestamp() + "' ;";
					}
					
				}
				else
				{
					query = query+ " last_sync_sessions_timestamp = null ; ";
				}
//				log.error("persistLastSynchTimes : insert q is " + query);
				con.runUpdateQuery(query);
			}
			catch(Exception e){
			log.error("persistLastSynchTimes : " + e);
			
			}finally{
				//close the database connection 
				DBUtilities.close(con);
			}
		}

		@Override
		public String[] getGateways() {
			String[] returnGateways = null;
			int noOfGateways = 0, i=0;
			Datasource ds;
			Dataset resDS = null;
			returnGateways = new String[1];
			returnGateways[0] = "All Gateways"; 
			
			
			SRConnection con = null;
					
			String sqlQuery = "";
			sqlQuery = "SELECT GAN_ServerName FROM mod_ia_gateways where GAN_ServerState = 'Connected'"
					+ " and GAN_ServerName in (SELECT GAN_ServerName from mod_ia_monitored_gateways);";
			
			try {
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				resDS = con.runQuery(sqlQuery);
				if(resDS != null && resDS.getRowCount() > 0)
				{
					noOfGateways = resDS.getRowCount();
					returnGateways = new String[noOfGateways + 1];
					returnGateways[0] = "All Gateways"; //add the option for all projects
					for(i=0; i<noOfGateways; i++)
					{
						if(resDS.getValueAt(i,0) != null)
						{
							returnGateways[i+1] = resDS.getValueAt(i,0).toString();
						}
					}
					
				}
				
				
			}
			catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getGateways : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			return returnGateways;
		}

		@Override
		public String[] deleteAndGetUpdatedGatewaysList(String gatewayName) {
			Datasource ds;
			
			String[] returnGateways = null;
			int noOfProjects = 0, i=0;
			Dataset resDS = null;
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String sqlSelectQuery = "SELECT GAN_ServerName FROM mod_ia_monitored_gateways;";
			String sqlDeleteQuery = "DELETE FROM mod_ia_monitored_gateways WHERE GAN_ServerName = '" + gatewayName.trim() + "';";
			
			try {
				con = ds.getConnection();
				//delete the requested gateway
				con.runUpdateQuery(sqlDeleteQuery);
				
				//retrieve modified list 
				
				resDS = con.runQuery(sqlSelectQuery);
				if(resDS != null && resDS.getRowCount() > 0)
				{
					noOfProjects = resDS.getRowCount();
					returnGateways = new String[noOfProjects + 1];
					returnGateways[0] = "All Gateways"; //add the option for all projects
					for(i=0; i<noOfProjects; i++)
					{
						if(resDS.getValueAt(i,0) != null)
						{
							returnGateways[i+1] = resDS.getValueAt(i,0).toString();
						}
					}
					
				}
				
				//call a service on Agent to stop sending data.
				ServiceManager sm = this.mycontext.getGatewayAreaNetworkManager().getServiceManager();
				ServerId sid = new ServerId(gatewayName);
				ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
				//if service is available
				if(s == ServiceState.Available)
				{
					//call the service 
					sm.getService(sid, AgentService.class).get().stopDataSync();
				}
				
				//make the Users status offline in the mod_ia_aggregates_users
				
				con.runUpdateQuery("UPDATE mod_ia_aggregates_users SET online_status = 'Offline'"
						+ " WHERE gateway_id = '" + gatewayName + "';");
			}
			catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("deleteAndGetUpdatedGatewaysList : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			return returnGateways;	
		}

		
		@Override
		public String[] getGatewaysNotAddedToIgnitionAnalytics() {
			Datasource ds;
			
			String[] returnGateways = null;
			int noOfGateways = 0, i=0;
			Dataset resDS = null;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String sqlSelectQuery = "SELECT distinct GAN_ServerName from mod_ia_gateways where"
					+ " GAN_Serverstate = 'Connected' and GAN_ServerName not in (select GAN_ServerName from mod_ia_monitored_gateways);";
			
			try {
				
				//get the list of projects added to Ignition ANalytics module.
				con = ds.getConnection();
				resDS = con.runQuery(sqlSelectQuery);
				
				if(resDS != null && resDS.getRowCount() > 0)
				{
					noOfGateways = resDS.getRowCount();
					returnGateways = new String[noOfGateways];
					for(i=0; i<noOfGateways; i++)
					{
						if(resDS.getValueAt(i,0) != null)
						{
							returnGateways[i] =  resDS.getValueAt(i,0).toString();
						}
					}
				}
				
				
				
			}
			catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getGatewaysNotAddedToIgnitionAnalytics : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			return returnGateways;
		}

		/*
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getGatewaysNotAddedToIgnitionAnalytics()
		 * Added by Yogini on 26-Dec-2016
		 * To retrieve the gateway details to be shown on Gateway Pane under Projects Panel 
		 * for enterprise module.
		 *
		 */
		@Override
		public Dataset getGatewayDetails(int duration, String gatewayName, Boolean allGateways) {
			Dataset returnData = null;
			
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilterOnController(duration, "a.SESSION_START");
			String dateFilter1 = getDateFilterOnController(duration, "a.START_TIMESTAMP");
			String sqlQuery = "";
//			if(allGateways){
//			sqlQuery = "SELECT x.gateway_id, coalesce(y.Users, 0), coalesce(x.Sessions,0), coalesce(x.avgTime, '00:00'), coalesce(x.Actions,0) from"
//					+ " (SELECT b.GAN_ServerName as gateway_id, sum(a.TOTAL_SESSIONS) as Sessions,"
//					+ " time_format(SEC_TO_TIME(avg(a.AVG_SESSION_DURATION)),'%H:%i:%s')  as avgTime, sum(a.ACTIONS) as Actions"
//					+ " from mod_ia_aggregates_daily_overview a right outer join mod_ia_monitored_gateways b"
//				+ " on a.gateway_id = b.GAN_ServerName and "
//					+ dateFilter + " group by b.GAN_ServerName ) as x left outer join "
//					+ " (SELECT count(distinct(USERNAME)) as Users, gateway_id"
//					+ " FROM mod_ia_aggregates_clients where "
//					+ dateFilter1 + " group by gateway_id) as y"
//					+ " on y.gateway_id = x.gateway_id;";
//			}
//			else{
//				sqlQuery = "SELECT x.gateway_id, coalesce(y.Users, 0), coalesce(x.Sessions,0), coalesce(x.avgTime, '00:00'), coalesce(x.Actions,0) from"
//						+ " (SELECT b.GAN_ServerName as gateway_id, sum(a.TOTAL_SESSIONS) as Sessions,"
//						+ " time_format(SEC_TO_TIME(avg(a.AVG_SESSION_DURATION)),'%H:%i:%s')  as avgTime, sum(a.ACTIONS) as Actions"
//						+ " from mod_ia_aggregates_daily_overview a right outer join mod_ia_monitored_gateways b"
//					+ " on a.gateway_id = b.GAN_ServerName and gateway_id = '"
//						+ gatewayName + "' and "
//						+ dateFilter + " group by b.GAN_ServerName ) as x left outer join "
//						+ " (SELECT count(distinct(USERNAME)) as Users, gateway_id"
//						+ " FROM mod_ia_aggregates_clients where gateway_id = '"
//						+ gatewayName + "' and "
//						+ dateFilter1 + " group by gateway_id) as y"
//						+ " on y.gateway_id = x.gateway_id where x.gateway_id = '" + gatewayName +"';";
//			}
			if(allGateways){
				sqlQuery = "SELECT x.gateway_id, coalesce(y.Users, 0), coalesce(x.Sessions,0),"
						+ " coalesce(x.avgTime, '00:00:00'), coalesce(x.Actions,0) from"
						+ " (SELECT b.GAN_ServerName as gateway_id, count(a.session_start) as Sessions,"
						+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))) , '%H:%i:%s') as avgTime,"
						+ " sum(a.no_of_screens + a.no_of_actions) as Actions"
						+ " from mod_ia_aggregates_daily_sessions a right outer join mod_ia_monitored_gateways b"
						+ " on a.gateway_id = b.GAN_ServerName and "
						+ dateFilter + " group by b.GAN_ServerName ) as x left outer join "
						+ " (SELECT count(distinct(concat(a.USERNAME,b.auth_profile,a.gateway_id))) as Users, a.gateway_id"
						+ " FROM mod_ia_aggregates_clients a, mod_ia_aggregates_projects b where "
						+ dateFilter1 + " and a.project = b.project_name and a.gateway_id = b.gateway_id group by a.gateway_id) as y"
						+ " on y.gateway_id = x.gateway_id;";
				}
				else{
					sqlQuery = "SELECT x.gateway_id, coalesce(y.Users, 0), coalesce(x.Sessions,0), coalesce(x.avgTime, '00:00:00'), coalesce(x.Actions,0) from"
							+ " (SELECT b.GAN_ServerName as gateway_id, count(a.session_start) as Sessions,"
							+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))), '%H:%i:%s')  as avgTime"
							+ ", sum(a.no_of_screens + a.no_of_actions) as Actions"
							+ " from mod_ia_aggregates_daily_sessions a right outer join mod_ia_monitored_gateways b"
							+ " on a.gateway_id = b.GAN_ServerName and gateway_id = '"
							+ gatewayName + "' and "
							+ dateFilter + " group by b.GAN_ServerName ) as x left outer join "
							+ " (SELECT count(distinct(concat(a.USERNAME,b.auth_profile,a.gateway_id))) as Users, a.gateway_id"
							+ " FROM mod_ia_aggregates_clients a, mod_ia_aggregates_projects b where a.project = b.project_name and"
							+ " a.gateway_id = b.gateway_id and a.gateway_id = '"
							+ gatewayName + "' and "
							+ dateFilter1 + " group by a.gateway_id) as y"
							+ " on y.gateway_id = x.gateway_id where x.gateway_id = '" + gatewayName +"';";
				}
			try {
//				log.error("getGatewayDetails : sql q is - " + sqlQuery);
				con = ds.getConnection();
				returnData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getGatewayDetails : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			return returnData;
		}

		@Override
		public void addGatewaysToModule(List<String> gatewayNames) {
			Datasource ds;
			int noOfGateways = 0;
			SRConnection con = null;
			String insertGatewaysStmt = "INSERT INTO mod_ia_monitored_gateways (GAN_ServerName) VALUES ('";
			UserSourceManager _um;
			try {
					ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					con = ds.getConnection();
					if(gatewayNames != null)
					{
						noOfGateways = gatewayNames.size();
				
						for(int n=0; n<noOfGateways; n++)
						{
							
							//call a service on Agent to start sending data.
							ServiceManager sm = this.mycontext.getGatewayAreaNetworkManager().getServiceManager();
							ServerId sid = new ServerId(gatewayNames.get(n));
							ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
							//if service is available
							if(s == ServiceState.Available)
							{
								//call the service 
								sm.getService(sid, AgentService.class).get().startDataSync();
							}
							
							if(n == noOfGateways - 1)
							{
								//get the project auth profile using project manager getProps API
								insertGatewaysStmt = insertGatewaysStmt +  gatewayNames.get(n) 
										+ "' ); ";
							}
							else
							{
								insertGatewaysStmt = insertGatewaysStmt + gatewayNames.get(n)  
										 + "') , ('";
							}
						}
						con.runUpdateQuery(insertGatewaysStmt);
					
					}
		
				}
				catch (SQLException e) {
						e.printStackTrace();
					}
				finally{
					if(con!=null){
						try {
								con.close();
						} catch (SQLException e) {
							log.error("addGatewaysToModule  : in con close exception.");
			
								e.printStackTrace();
							}
		
						}
				}
			
		}
		
		/**
		 * Get the list of projects with overview information to be shown on Projects Panel
		 * @author YM , Created on 8/12/2015
		 */
		@Override
		public Dataset getProjectDetailsPerGateway(int duration, String gatewayName, String projectName) {
			Dataset returnData = null;
			
			Datasource ds;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
					
			String dateFilter = getDateFilterOnController(duration, "a.SESSION_START");
			String dateFilter1 = getDateFilterOnController(duration, "a.START_TIMESTAMP");
			String sqlQuery = "";
//			log.error("getProjectDetailsPerGateway gatewayname: "+ gatewayName+"  projectname: "+ projectName);
			if(gatewayName != null && projectName != null){

				gatewayName = gatewayName.trim();
				if(gatewayName.compareToIgnoreCase("All Gateways") == 0 && projectName.compareToIgnoreCase("All Projects") == 0)
				{
					sqlQuery = "select x.Gateway_id, x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
						+ " (SELECT b.GATEWAY_ID as GATEWAY_ID, b.PROJECT_NAME as Project, count(a.session_start) as Sessions,"
						+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))), '%H:%i:%s')  as avgTime"
						+ ", sum(a.no_of_screens + a.no_of_actions) as Actions"
						+ " from mod_ia_aggregates_daily_sessions a right outer join mod_ia_aggregates_projects b"
						+ " on a.project_name = b.project_name and   a.gateway_id = b.GATEWAY_ID and "
						+ dateFilter + " group by b.GATEWAY_ID, b.PROJECT_NAME) as x left outer join "
						+ " (SELECT count(distinct(concat(a.USERNAME,b.auth_profile,a.gateway_id))) as Users, a.PROJECT, a.GATEWAY_ID"
						+ "  FROM mod_ia_aggregates_clients a, mod_ia_aggregates_projects b"
						+ " where a.project = b.project_name and a.gateway_id = b.gateway_id and "
						+ dateFilter1 + " group by a.GATEWAY_ID, a.PROJECT) as y"
						+ " on y.PROJECT = x.Project and y.GATEWAY_ID = x.GATEWAY_ID order by x.Gateway_id;";
					
//					log.error("A");
				}
				else if(gatewayName.compareToIgnoreCase("All Gateways") != 0 && projectName.compareToIgnoreCase("All Projects") == 0)
				{
					sqlQuery = "select x.gateway_id, x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
							+ " (SELECT b.GATEWAY_ID as GATEWAY_ID, b.PROJECT_NAME as Project, count(a.session_start) as Sessions,"
							+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))), '%H:%i:%s')  as avgTime"
							+ ", sum(a.no_of_screens + a.no_of_actions) as Actions"
							+ " from mod_ia_aggregates_daily_sessions a right outer join"
							+ " ( Select * from mod_ia_aggregates_projects where gateway_id = '" + gatewayName + "') b"
							+ " on a.project_name = b.project_name and  a.GATEWAY_ID = b.GATEWAY_ID and "
							+ " a.gateway_id = '"+ gatewayName + "' and "
							+ " b.gateway_id = '"+ gatewayName + "' and "
							+ dateFilter + " group by b.GATEWAY_ID, b.PROJECT_NAME ) as x left outer join "
							+ " (SELECT count(distinct(concat(a.USERNAME,b.auth_profile,a.gateway_id))) as Users, a.PROJECT, a.GATEWAY_ID"
							+ "  FROM mod_ia_aggregates_clients a, mod_ia_aggregates_projects b"
							+ " where a.project = b.project_name and a.gateway_id = b.gateway_id and"
							+ " a.gateway_id = '" + gatewayName + "' and "
							+ dateFilter1 + " group by a.GATEWAY_ID, a.PROJECT) as y"
							+ " on x.Gateway_id = '" + gatewayName + "' and y.PROJECT = x.Project and y.GATEWAY_ID = x.GATEWAY_ID order by x.Gateway_id;";
//					log.error("B");
				}
				else if(gatewayName.compareToIgnoreCase("All Gateways") == 0 && projectName.compareToIgnoreCase("All Projects") != 0)
				{
					sqlQuery = "select x.gateway_id, x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
							+ " (SELECT b.GATEWAY_ID as GATEWAY_ID, b.PROJECT_NAME as Project, count(a.session_start) as Sessions,"
							+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))), '%H:%i:%s')  as avgTime"
							+ ", sum(a.no_of_screens + a.no_of_actions) as Actions"
							+ " from mod_ia_aggregates_daily_sessions a right outer join "
							+ " (select * from mod_ia_aggregates_projects where project_name = '"  + projectName +"' ) b"
							+ " on a.project_name = b.project_name and a.GATEWAY_ID = b.GATEWAY_ID and a.project_name = '"+ projectName + "' and "
							+ dateFilter + " group by b.GATEWAY_ID, b.PROJECT_NAME ) as x left outer join "
							+ " (SELECT count(distinct(concat(a.USERNAME,b.auth_profile,a.gateway_id))) as Users, a.PROJECT, a.GATEWAY_ID"
							+ "  FROM mod_ia_aggregates_clients a, mod_ia_aggregates_projects b"
							+ " where a.project = b.project_name and a.gateway_id = b.gateway_id and a.project = '" + projectName + "' and "
							+ dateFilter1 + " group by a.GATEWAY_ID, a.PROJECT) as y"
							+ " on y.PROJECT = x.Project and y.GATEWAY_ID = x.GATEWAY_ID order by x.Gateway_id;";
//					log.error("C");
				}
				else
				{
					sqlQuery = "select x.gateway_id, x.Project, coalesce(y.Users, 0), x.Sessions, x.avgTime, x.Actions from"
							+ " (SELECT b.GATEWAY_ID as GATEWAY_ID, b.PROJECT_NAME as Project, count(a.session_start) as Sessions,"
							+ " time_format(SEC_TO_TIME(AVG(time_to_sec(a.session_end) - time_to_sec(a.session_start))), '%H:%i:%s')  as avgTime"
							+ ", sum(a.no_of_screens + a.no_of_actions) as Actions"
							+ " from mod_ia_aggregates_daily_sessions a right outer join "
							+ " ( select * from mod_ia_aggregates_projects where gateway_id = '" + gatewayName + 
							  "' and project_name = '" + projectName +"') b"
							+ " on a.project_name = b.project_name and a.GATEWAY_ID = b.GATEWAY_ID and  "
							+ " a.gateway_id = '" + gatewayName + "' and a.project_name = '"+ projectName + "' and "
							+ " b.gateway_id = '" + gatewayName + "' and b.project_name = '"+ projectName + "' and "
							+ dateFilter + " group by b.GATEWAY_ID, b.PROJECT_NAME ) as x left outer join "
							+ " (SELECT count(distinct(concat(a.USERNAME,b.auth_profile,a.gateway_id))) as Users, a.PROJECT, a.GATEWAY_ID"
							+ "  FROM mod_ia_aggregates_clients a, mod_ia_aggregates_projects b"
							+ " where a.project = b.project_name and a.gateway_id = b.gateway_id and "
							+ " a.gateway_id = '" + gatewayName + "' and a.project = '" + projectName + "' and "
							+ dateFilter1 + " group by a.GATEWAY_ID, a.PROJECT) as y"
							+ " on x.gateway_id = '" + gatewayName + "' and y.PROJECT = x.Project and"
							+ " y.GATEWAY_ID = x.GATEWAY_ID order by x.Gateway_id;";
//					log.error("D");
				}
//				log.error("getProjectDetailsPerGateway : "+ sqlQuery);
			
			
			try {
				
				con = ds.getConnection();
				returnData = con.runQuery(sqlQuery);
				
			}
			catch (SQLException e) {
			
			e.printStackTrace();
		}
		finally{
			if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				log.error("getProjectDetailsPerGateway : in con close exception.");
				
				e.printStackTrace();
			}
			
			}
		}
			}
			return returnData;
		}

		/* Function created by Yogini on 29-Dec-2016
		 * Called from Project Details Pane on Controller to get list of projects from Agent 
		 * that are not added to Analytics module 
		 * 
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getProjectsNotAddedFromAgent(java.lang.String)
		 */
		@Override
		public String[] getProjectsNotAddedFromAgent(String gatewayName)  {
			
			String[] projectList = null;
			GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
			try
			{
				if(this.isAgent == false)
				{
					if(gatewayName.compareToIgnoreCase(gm.getServerAddress().getServerName().trim()) == 0)
					{
						projectList = this.getProjectNotAddedRoIgnitionAnalytics();
					}
					else
					{
						ServiceManager sm = gm.getServiceManager();
						ServerId sid = new ServerId(gatewayName);
						ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
						//if service is available
						if(s == ServiceState.Available)
						{
							//call the service 
							projectList = sm.getService(sid, AgentService.class).get().getProjectNotAddedOnAgent();
						}
						else
						{
							//throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gatewayName);
						}
					}
				}
			}
			catch(Exception e)
			{
				log.error("getProjectsNotAddedFromAgent : Exception in calling agent service " + e);
				//throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gatewayName);
			}
			
			return projectList;
		}

		@Override
		public void addProjectsToAgent(String gatewayName, List<String> projectsToAdd) throws MODIAServiceUnavailableException {
			
			int noOfProjects = projectsToAdd.size();
			
			if(noOfProjects > 0)
			{
				//call service from Agent
				GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
				try
				{
					if(this.isAgent == false)
					{
						if(gatewayName.compareToIgnoreCase(gm.getServerAddress().getServerName().trim()) == 0)
						{
							this.addProjectsToModule(projectsToAdd);
						}
						else
						{
							ServiceManager sm = gm.getServiceManager();
							ServerId sid = new ServerId(gatewayName);
							ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
							//if service is available
							if(s == ServiceState.Available)
							{
								//call the service 
								sm.getService(sid, AgentService.class).get().addProjectsToAgent(projectsToAdd);
							}
							else
							{
								throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gatewayName);
							}
						}
					}
				}
				catch(Exception e)
				{
					log.error("getProjectsNotAddedFromAgent : Exception in calling agent service " + e);
					throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gatewayName);
				}
				
				
			}
			
			
		}

		@Override
		public String[] deleteProjectsFromAgent(String gatewayName, String projectToDelete) throws MODIAServiceUnavailableException {
			//call service from Agent
			
			String[]  returnProjects = null;
			GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
			try
			{
				if(this.isAgent == false)
				{
					if(gatewayName.compareToIgnoreCase(gm.getServerAddress().getServerName().trim()) == 0)
					{
						returnProjects = this.deleteAndGetUpdatedProjectsList(projectToDelete);
					}
					else
					{
						ServiceManager sm = gm.getServiceManager();
						ServerId sid = new ServerId(gatewayName);
						ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
						//if service is available
						if(s == ServiceState.Available)
						{
							//call the service 
							returnProjects = sm.getService(sid, AgentService.class).get().deleteProjectsFromAgent(projectToDelete);
						}
						else
						{
							throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gatewayName);
						}
					}
				}
			}
			catch(Exception e)
			{
				log.error("getProjectsNotAddedFromAgent : Exception in calling agent service " + e);
				throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gatewayName);
			}
			return returnProjects;
		}
		
		
		/*
		 * Omkar on 26-Dec-2016
		 */
		public Dataset getSevenDaysMaxMinForController(String datasource, int duration, String projectName, boolean allProjects,String gateWayName,boolean allGateways)
		{
			Dataset maxminData = null;
			String sqlQuery = "null";
			String dateFilter = getDateFilterOnController(Constants.LAST_SEVEN_DAYS, "a.START_TIMESTAMP");
			
			SRConnection con = null;
			Datasource ds;
			//modified the sql query to return results for all dates, to handle missing data case.
			
			if(allProjects && allGateways){
				
				sqlQuery = "select date(start_timestamp), count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users"
						+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
						+ " where  a.PROJECT = b.PROJECT_NAME "
						+ " and "+ dateFilter + " group by date(start_timestamp) "
						+ ";";

			}else
			if(!allProjects && allGateways){
				sqlQuery = "select date(start_timestamp), count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users"
						+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
						+ " where  a.PROJECT = b.PROJECT_NAME and a.PROJECT = '" + projectName +"' "
						+ " and "+ dateFilter +" group by date(start_timestamp)"
						+ ";";
			}
			else if (allProjects  && !allGateways) {
				sqlQuery = "select date(start_timestamp), count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users"
						+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
						+ " where  a.PROJECT = b.PROJECT_NAME "
						+ " and a.GATEWAY_ID = '"
						+ gateWayName + "'"
						+ " and "+ dateFilter +" group by date(start_timestamp)"
						+ ";";
			}
			else if (!allProjects && !allGateways) {
				sqlQuery = "select date(start_timestamp), count(distinct(CONCAT(a.username,b.AUTH_PROFILE,a.GATEWAY_ID))) as total_users"
						+ " from mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
						+ " where  a.PROJECT = b.PROJECT_NAME and a.PROJECT = '"+ projectName +"' "
						+ " and a.GATEWAY_ID = '"
						+ gateWayName + "'"
						+ " and "+ dateFilter +" group by date(start_timestamp)"
						+ ";";
			}
				
//			log.error("\n getSevenDaysMaxMinForController: "+sqlQuery);
			try {
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				maxminData = con.runQuery(sqlQuery);
				
			} catch (Exception e) {
				// TODO: handle exception
				log.error("getSevenDaysMaxMinForController :"+e);
			}
			finally{
				if(con != null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getSevenDaysMaxMinForController : in con close exception.");
						// TODO: handle exception
						log.error("getSevenDaysMaxMinForController :"+e);
						e.printStackTrace();
					}
				}
			}
			
			
			return maxminData;
			
		}
		
		/* Omkar 26-Dec-2016
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getNumberOfNewUsersOnController(int, java.lang.String, boolean, java.lang.String, boolean)
		 */
		@Override
		public int getNumberOfNewUsersOnController(int duration,  String projectName, boolean allProjects,String gateWayName, boolean allGateWays) {
			int noOfNewUsers = 0;
			Datasource _ds;
			Dataset resDS = null;
			Date startDate, endDate, loginDate;
			_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			String user = "";
			String userProfile = "";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			Calendar calStart = Calendar.getInstance();
		    calStart.set(Calendar.HOUR_OF_DAY, 0);
		    calStart.set(Calendar.MINUTE, 0);
		    calStart.set(Calendar.SECOND, 0);
		    calStart.set(Calendar.MILLISECOND, 0);
			startDate = calStart.getTime();
			
			Calendar calEnd = Calendar.getInstance();
			calEnd.set(Calendar.HOUR_OF_DAY, 0);
			calEnd.set(Calendar.MINUTE, 0);
			calEnd.set(Calendar.SECOND, 0);
			calEnd.set(Calendar.MILLISECOND, 0);
			endDate = calEnd.getTime();
			String startDateStr = "";
			startDateStr = sdf.format(startDate);
			String filter = "";
			if(allProjects && allGateWays)
			{
				filter = "";
			}
			else if(!allProjects && allGateWays)
			{
				filter = " and a.PROJECT = '" + projectName + "' ";
			}
			else if(allProjects && !allGateWays)
			{
				filter = " and a.GATEWAY_ID = '" + gateWayName + "' ";
			}
			else if(!allProjects && !allGateWays)
			{
				filter = " and a.GATEWAY_ID = '" + gateWayName + "' and a.PROJECT = '" + projectName + "' ";
			}
			
			try
			{
				
				switch(duration){
				case Constants.TODAY:
					sqlQuery = "select  a.username, b.AUTH_PROFILE , a.GATEWAY_ID"
							+ " FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b "
							+ " where a.PROJECT = b.PROJECT_NAME and a.gateway_id = b.gateway_id " + filter
							+ " group by a.username,b.AUTH_PROFILE,a.gateway_id"
							+ " having DATE(min(a.START_TIMESTAMP)) = DATE(NOW());";
					endDate = startDate; 
					break;
				case Constants.YESTERDAY:
//					calStart.add(Calendar.DATE, -1);
//					startDate = calStart.getTime();
//					startDateStr = sdf.format(startDate);
					sqlQuery = "select  a.username, b.AUTH_PROFILE , a.GATEWAY_ID"
							+ " FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b "
							+ " where a.PROJECT = b.PROJECT_NAME and a.gateway_id = b.gateway_id " + filter
							+ " group by a.username,b.AUTH_PROFILE,a.gateway_id"
							+ " having DATE(min(a.START_TIMESTAMP)) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) ;";
					calEnd.add(Calendar.DATE, -1);
					endDate = calEnd.getTime();
					break;
				}
				con = _ds.getConnection();
				resDS = con.runQuery(sqlQuery);
				String gatewayID;
				if(resDS != null)
				{
					noOfNewUsers = resDS.getRowCount();
		
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			finally{
				if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getNumberOfNewUsersOnController : in con close exception.");
						
						e.printStackTrace();
					}
				}
			}
			return noOfNewUsers;
		}
		
		/*
		 * Omkar 26-Dec-2016 
		 * Query alarms information on controller for real time panel
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getAlarmsOverviewOnController(int, java.lang.String, boolean, java.lang.String, boolean)
		 */
		@Override
		public AlarmsInformation getAlarmsOverviewOnController( int duration, String projectName, boolean allProjects,String currentGateway, boolean allGateways) {
			
			AlarmsInformation _alarms = new AlarmsInformation();
			HashMap<String, Integer> mapActAlarms = new HashMap<String, Integer>();
			HashMap<String, Integer> mapAckAlarms = new HashMap<String, Integer>();
			HashMap<String, String> mapTimeToClearAlarms = new HashMap<String, String>();
			HashMap<String, String> mapTimeToAckAlarms = new HashMap<String, String>();
			
			Dataset temp;
			int i, noOfRecs, noOfAlarms = 0;
			//get list of active alarms priority wise and calculate total count
			temp  = this.getActiveAlarmsCountOnController( currentGateway, allGateways);
			if(temp != null)
			{
				noOfRecs = temp.getRowCount();
				for(i=0; i<noOfRecs; i++)
				{
					noOfAlarms = noOfAlarms + (int)Double.parseDouble(temp.getValueAt(i, 1).toString());
					mapActAlarms.put(temp.getValueAt(i, 0).toString(), (int)Double.parseDouble(temp.getValueAt(i, 1).toString()));
				}
				
				_alarms.setActiveAlarmsCount(mapActAlarms);
				_alarms.setNoOfActiveAlarms(noOfAlarms);
			}
			
			//get list of acknowledged alarms priority wise and calculate total count
			noOfAlarms = 0;
			temp  = this.getAckAlarmsCountOnController( currentGateway, allGateways);
			if(temp != null)
			{
				noOfRecs = temp.getRowCount();
				for(i=0; i<noOfRecs; i++)
				{
					noOfAlarms = noOfAlarms + (int)Double.parseDouble(temp.getValueAt(i, 1).toString());
					mapAckAlarms.put(temp.getValueAt(i, 0).toString(), (int)Double.parseDouble(temp.getValueAt(i, 1).toString()));
				}
				
				_alarms.setAckAlarmsCount(mapAckAlarms);
				_alarms.setNoOfAckAlarms(noOfAlarms);
			}
			
			//get list of average time to clear alarms per priority
					temp  = this.getAlarmsClearTimeController(duration, currentGateway, projectName, allGateways, allProjects);
					if(temp != null)
					{
						noOfRecs = temp.getRowCount();
						for(i=0; i<noOfRecs; i++)
						{
							mapTimeToClearAlarms.put(temp.getValueAt(i, 0).toString(),temp.getValueAt(i, 1).toString());
						}
						_alarms.setTimeToClearAlarmsPerPriority(mapTimeToClearAlarms);
					}
					
					//get list of average time to acknowledge alarms per priority
					temp  = this.getAlarmsAckTimeController(duration, currentGateway,projectName,allGateways, allProjects);
					if(temp != null)
					{
						noOfRecs = temp.getRowCount();
						for(i=0; i<noOfRecs; i++)
						{
							String s = "00:00:00.0"; 
							if(temp.getValueAt(i, 1) != null)
							{
								//s = new SimpleDateFormat("HH.mm.ss.S").format(temp.getValueAt(i, 1));
								s = temp.getValueAt(i, 1).toString();
							}
							//timeToAck = Long.parseLong(s);
							mapTimeToAckAlarms.put(temp.getValueAt(i, 0).toString(), s);
						}
						
						_alarms.setTimeToAckAlarmsPerPriority(mapTimeToAckAlarms);
						
					}
					_alarms.setAvgClearTime(this.getAverageClearTime(duration, projectName, allProjects));
					_alarms.setAvgAckTime(this.getAverageAckTime(duration,  projectName, allProjects));
			
					return _alarms;
		}
		
		/*
		 * Omkar on 26-Dec-2016 
		 * Query 
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#getUserInformationOnController(int, java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String, boolean)
		 */
		public UsersOverviewInformation getUserInformationOnController( int duration, String uName, String projectName, boolean allProjects, String userAuthProfile,String currenGateway, boolean allGateways) {
			Datasource _ds;
			Dataset resDS = null;
			_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			SRConnection con = null;
			UsersOverviewInformation uinfo = new UsersOverviewInformation();
			String sqlQ = "SELECT gateway_id, email, username, roles, phone, gateway_userprofile,"
					+ " current_location, first_seen, last_seen, last7days_duration, last7days_visits, "
					+ " last7days_actions, all_actions, all_screen_views,  current_screen, online_status,"
					+ " projectname FROM mod_ia_aggregates_users WHERE username = '" + uName + "' "
					+ " and gateway_userprofile = '" + userAuthProfile + "' ";
			if(allGateways && allProjects)
			{
				sqlQ = sqlQ + " and projectname = 'All';";
			}
			else if(!allGateways && allProjects)
			{
				sqlQ = sqlQ + " and projectname = 'All' and gateway_id = '" + currenGateway + "'";
			}
			else if(allGateways && !allProjects)
			{
				sqlQ = sqlQ + " and projectname = '" + projectName + "' ";
			}
			else if(!allGateways && !allProjects)
			{
				sqlQ = sqlQ + " and projectname = '" + projectName + "' and gateway_id = '" + currenGateway + "'";
			}
			try
			{
//				log.error("getUserInformationOnController : sql q is " + sqlQ);
				con = _ds.getConnection();
				resDS = con.runQuery(sqlQ);
				if(resDS != null)
				{
					if(resDS.getValueAt(0, "first_seen") != null)
					{
						uinfo.setFirstSeen(resDS.getValueAt(0, "first_seen").toString());
					}
					if(resDS.getValueAt(0, "last_seen") != null)
					{
						uinfo.setLastSeen(resDS.getValueAt(0, "last_seen").toString());
					}
					if(resDS.getValueAt(0, "last7days_actions") != null)
					{
						uinfo.setTotalActionsLast7Days(Integer.parseInt(resDS.getValueAt(0, "last7days_actions").toString()));
					}
					if(resDS.getValueAt(0, "last7days_duration") != null)
					{
						uinfo.setTotalSessionsLength(resDS.getValueAt(0, "last7days_duration").toString());
					}
					if(resDS.getValueAt(0, "last7days_visits") != null)
					{
						uinfo.setTotalVisitsLast7Days(Integer.parseInt(resDS.getValueAt(0, "last7days_visits").toString()));
					}
//					if(resDS.getValueAt(0, "last_seen") != null)
//					{
//						uinfo.setScreensViewedLast7Days(this.getScreensViewedCountsPerUser( duration, uName, projectName, allProjects, userAuthProfile));
//					}
					if(resDS.getValueAt(0, "current_screen") != null)
					{
						uinfo.setCurrentScreen(resDS.getValueAt(0, "current_screen").toString());
					}
					if(resDS.getValueAt(0, "current_location") != null)
					{
						String location = resDS.getValueAt(0, "current_location").toString();
						location = location.replace(", null, ", ", ");
						location = location.replace("false, ", " ");
						location = location.replace("null", "None");
						uinfo.setLocation(location);
					}
					if(resDS.getValueAt(0, "all_actions") != null)
					{
						uinfo.setTotalActions(Integer.parseInt(resDS.getValueAt(0, "all_actions").toString()));
					}
//					if(resDS.getValueAt(0, "last_seen") != null)
//					{
//						uinfo.setTotalVisits(this.getTotalVisits( uName, Constants.LAST_365_DAYS, projectName, allProjects, userAuthProfile));
//					}
//					if(resDS.getValueAt(0, "last_seen") != null)
//					{
//						uinfo.setScreensViewed(this.getScreensViewedCountsPerUser( Constants.LAST_365_DAYS, uName, projectName, allProjects, userAuthProfile));
//					}
				}
				
			}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getUserInformationOnController : in con close exception.");
					
					e.printStackTrace();
				}
			}
		}
			return uinfo;
		}

		//By Yogini on 16-Jan-2017 for users panel changes.
		@Override
		public boolean receiveUsersStatus(String agentID, List<UserSyncRecord> users) {
			boolean returnValue = true;
			
			if(this.isGatewayMonitored(agentID))
			{
				if(users == null || users.size() == 0)
				{
					log.error("receiveUsersStatus was called however there was nothing to insert from Gateway : " + agentID);
				}
				else
				{
					int usersSize = users.size();
					int i = 0;
					Datasource _ds;
					Dataset resDS = null;
					
					_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					String sqlQuery = "";
					SRConnection con = null;
					UserSyncRecord _record;
					try
					{
						con = _ds.getConnection();
						for(i=0; i<usersSize; i++)
						{
							_record  = users.get(i);
							sqlQuery = "SELECT username from mod_ia_aggregates_users where gateway_id = '"
									+ agentID + "' and username = '" + _record.getUserName() + "' and "
											+ " gateway_userprofile = '" + _record.getGatewayUserprofile()
											+ "' and projectname = '" + _record.getProjectName() + "';";
							resDS = con.runQuery(sqlQuery);
							sqlQuery = "";
							if(resDS != null && resDS.getRowCount() > 0)
							{
								//record exists so update 
								sqlQuery = "update mod_ia_aggregates_users set "
										+ " email = '"  + _record.getEmail() + "' ,"
										+ " roles = '"  + _record.getRoles() + "' ,"
										+ " phone = '" + _record.getPhone() + "' ,"	
										+ " current_location = '" + _record.getCurrentLocation() + "' ,"
										+ " first_seen = '"  + _record.getFirstSeen() + "' ,"
										+ " last_seen = '" + _record.getLastSeen() + "' ,"	
										+ " last7days_duration = '" + _record.getLast7daysDuration() + "' ,"
										+ " last7days_visits = "  + _record.getLast7daysVisits() + " ,"
										+ " last7days_actions = "  + _record.getLast7DaysActions() + " ,"	
										+ " all_actions = " + _record.getAllActions() + " ,"
										+ " all_screen_views = " + _record.getAllScreenViews() + " ,"
										+ " current_screen = '" + _record.getCurrentScreen() + "' ,"	
										+ " online_status = '"  + _record.getOnlineStatus() + "'"
										+ " where gateway_id = '" + agentID 
										+ "' and username = '" + _record.getUserName() 
										+ "' and gateway_userprofile = '" + _record.getGatewayUserprofile()
										+ "' and projectname = '" + _record.getProjectName() + "';";
							}
							else
							{
								//record does not exist so insert
								sqlQuery = "insert into mod_ia_aggregates_users "
										+ "(gateway_id, username, gateway_userprofile, email,roles , phone, current_location, "
										+ " first_seen,last_seen, last7days_duration, last7days_visits, last7days_actions, "
										+ " all_actions, all_screen_views, current_screen, online_status, projectName ) VALUES ( "
										+ " '"  + agentID + "' ,"
										+ " '"  + _record.getUserName() + "' ,"
										+ " '"  + _record.getGatewayUserprofile() + "' ,"
										+ " '"  + _record.getEmail() + "' ,"
										+ " '"  + _record.getRoles() + "' ,"
										+ " '" + _record.getPhone() + "' ,"	
										+ " '" + _record.getCurrentLocation() + "' ,"
										+ " '"  + _record.getFirstSeen() + "' ,"
										+ " '" + _record.getLastSeen() + "' ,"	
										+ " '" + _record.getLast7daysDuration() + "' ,"
										+ _record.getLast7daysVisits() + " ,"
										+ _record.getLast7DaysActions() + " ,"	
										+ _record.getAllActions() + " ,"
										+ _record.getAllScreenViews() + " ,"
										+ " '" + _record.getCurrentScreen() + "' ,"	
										+ " '"  + _record.getOnlineStatus() + "',"
										+ " '"  + _record.getProjectName() + "'"
										+ ");";
							}
//							log.error("receiveUsersStatus : insert q : " + sqlQuery);
							con.runUpdateQuery(sqlQuery);	
							
						}
					}
					catch(Exception e){
						returnValue = false;
						e.printStackTrace();
					}
					finally{
						if(con!=null){
							try {
								con.close();
							} catch (SQLException e) {
								log.error("receiveUsersStatus : in con close exception.");
								
								e.printStackTrace();
							}
						}
					}
				}
			}
			return returnValue;
		}

		/*
		 * Added by Yogini on 17-jan-2016
		 * This will run on agent and will be called from scheduled task to 
		 * send online users information at the Agent Gateway
		 * (non-Javadoc)
		 * @see com.vaspsolutions.analytics.common.ModuleRPC#sendUsersStatus()
		 */
		@Override
		public boolean sendUsersStatus() {
			
			boolean returnStatus = true;
			boolean syncStatus = false;
			List<UserSyncRecord> _users = new ArrayList<UserSyncRecord>();
			
			UserSyncRecord _record;
			//make respective function calls to collect data 
			
			//get all user profiles
			Collection<User> userProfiles = this.getUserProfiles();
			
			//get all users that are at least logged in once
			Dataset _loggedinUsers = this.getAllLoggedInUsers(true, "");
			int i=0, noOfUsers = _loggedinUsers.getRowCount(); 
			String userProfileName = "", profileName = "", userName = "";
			
			//get list of all projects
			String projects[] = this.getProjects("All Projects");
			int noOfProjects = projects.length;
			for(i=0; i<noOfUsers; i++)
			{
				userProfileName = _loggedinUsers.getValueAt(i, 0).toString();
				userName = userProfileName.split(":")[0];
				profileName = userProfileName.split(":")[1];
				//retrieve data per project.
				for(int p=1;p <noOfProjects; p++)
				{
					boolean isUserOnline = this.checkUserOnlineOrOffline( projects[p], false, userName, profileName);
					UsersOverviewInformation _uinfo = this.getUserInformation( Constants.LAST_SEVEN_DAYS, userName, projects[p], false, profileName);
					
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
							
					}
					_record = new UserSyncRecord();
					_record.setUserName(userName);
					_record.setGatewayUserprofile(profileName);
					_record.setAllActions(_uinfo.getTotalActions() + totalScreens);
					_record.setAllScreenViews(totalScreens);
					_record.setCurrentLocation(_uinfo.getLocation());
					_record.setCurrentScreen(_uinfo.getCurrentScreen());
					_record.setFirstSeen(_uinfo.getFirstSeen());
					_record.setLast7DaysActions(_uinfo.getTotalActionsLast7Days() + totalScreens7);
					_record.setLast7daysDuration(_uinfo.getTotalSessionsLength());
					_record.setLast7daysVisits(_uinfo.getTotalVisitsLast7Days());
					_record.setLastSeen(_uinfo.getLastSeen());
					
					if(isUserOnline == true)
					{
						_record.setOnlineStatus("Online");
					}
					else
					{
						_record.setOnlineStatus("Offline");
					}
					
					
					//retrieve and set email and roles
					Iterator<User> retrieveUsers;
					retrieveUsers = userProfiles.iterator();
					User currentUser ;
					List<ContactInfo> contact;
					
					int noOfcontacts;
					String[] userRoles;
					int noOfRoles;
					String _contact = "", _contactType;
					String roles = "";
					while(retrieveUsers.hasNext())
					{
						
						currentUser = retrieveUsers.next();
						if((currentUser.get(User.Username).trim().compareToIgnoreCase(userName)) == 0 
								&& (currentUser.getProfileName().compareToIgnoreCase(profileName) == 0))
						{
							contact = currentUser.getContactInfo();
							if(contact != null)
							{
								noOfcontacts = contact.size();
								for(int j=0; j<noOfcontacts; j++)
								{
								
									_contactType = contact.get(j).getContactType() ;
									if(_contactType.compareTo("email") == 0)
									{
										_contact = _contact + "  " + contact.get(j).getValue();
									}
								}
							}
							
							//get roles information
							Collection<String> tempRoles = currentUser.getRoles();
							if(tempRoles != null)
							{
								userRoles = tempRoles.toArray(new String[currentUser.getRoles().size()]);
								noOfRoles = userRoles.length;
								for(int k=0;k<noOfRoles; k++)
								{
									if(k == 0)
									{
										roles = roles + userRoles[k] ;
									}
									else
									{
										roles = roles  + "," + userRoles[k] ;
									}
								}
							}
						}
					
				}
				
					//_record.setPhone(phone);
					_record.setEmail(_contact);
					_record.setRoles(roles);
					_record.setProjectName(projects[p]);
					_users.add(_record);
				}
				
				//repeat above for All projects.
				boolean isUserOnline = this.checkUserOnlineOrOffline( "", true, userName, profileName);
				UsersOverviewInformation _uinfo = this.getUserInformation( Constants.LAST_SEVEN_DAYS, userName, "", true, profileName);
				
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
						
				}
				_record = new UserSyncRecord();
				_record.setUserName(userName);
				_record.setGatewayUserprofile(profileName);
				_record.setAllActions(_uinfo.getTotalActions() + totalScreens);
				_record.setAllScreenViews(totalScreens);
				_record.setCurrentLocation(_uinfo.getLocation());
				_record.setCurrentScreen(_uinfo.getCurrentScreen());
				_record.setFirstSeen(_uinfo.getFirstSeen());
				_record.setLast7DaysActions(_uinfo.getTotalActionsLast7Days() + totalScreens7);
				_record.setLast7daysDuration(_uinfo.getTotalSessionsLength());
				_record.setLast7daysVisits(_uinfo.getTotalVisitsLast7Days());
				_record.setLastSeen(_uinfo.getLastSeen());
				
				if(isUserOnline == true)
				{
					_record.setOnlineStatus("Online");
				}
				else
				{
					_record.setOnlineStatus("Offline");
				}
				
				
				//retrieve and set email and roles
				Iterator<User> retrieveUsers;
				retrieveUsers = userProfiles.iterator();
				User currentUser ;
				List<ContactInfo> contact;
				
				int noOfcontacts;
				String[] userRoles;
				int noOfRoles;
				String _contact = "", _contactType;
				String roles = "";
				while(retrieveUsers.hasNext())
				{
					
					currentUser = retrieveUsers.next();
					if((currentUser.get(User.Username).trim().compareToIgnoreCase(userName)) == 0 
							&& (currentUser.getProfileName().compareToIgnoreCase(profileName) == 0))
					{
						contact = currentUser.getContactInfo();
						if(contact != null)
						{
							noOfcontacts = contact.size();
							for(int j=0; j<noOfcontacts; j++)
							{
							
								_contactType = contact.get(j).getContactType() ;
								if(_contactType.compareTo("email") == 0)
								{
									_contact = _contact + "  " + contact.get(j).getValue();
								}
							}
						}
						
						//get roles information
						Collection<String> tempRoles = currentUser.getRoles();
						if(tempRoles != null)
						{
							userRoles = tempRoles.toArray(new String[currentUser.getRoles().size()]);
							noOfRoles = userRoles.length;
							for(int k=0;k<noOfRoles; k++)
							{
								if(k == 0)
								{
									roles = roles + userRoles[k] ;
								}
								else
								{
									roles = roles  + "," + userRoles[k] ;
								}
							}
						}
					}
				
			}
			
				//_record.setPhone(phone);
				_record.setEmail(_contact);
				_record.setRoles(roles);
				_record.setProjectName("All");
				_users.add(_record);
			}
			String agentName = this.mycontext.getGatewayAreaNetworkManager().getServerAddress().getServerName();
			
			if(this.isAgent)
			{
				
				ServiceManager sm = this.mycontext.getGatewayAreaNetworkManager().getServiceManager();
				
				ServerId sid = new ServerId(this.controllerName);
				ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
			
				//if service is available
				if(s == ServiceState.Available)
				{
					//call the service 
					syncStatus = sm.getService(sid, GetAnalyticsInformationService.class).get().receiveUsersStatus(agentName, _users);
				}
				else
				{
					returnStatus = false;
				}
				
				if(syncStatus == false)
				{
					returnStatus = false;
				}
			}
			else
			{
				//you are on controller itself , so call local method to populate data.
				this.receiveUsersStatus(agentName, _users);
			}
			
			return returnStatus;
		}

		/*
		 * Yogini 18-Jan-2017
		 * To receive alarm ack counts data from Agent 
		 */
		@Override
		public boolean receiveAlarmsAckCounts(String agentID, Date date, Dataset ackAlarmCounts) {
			boolean returnValue = true;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			if(this.isGatewayMonitored(agentID))
			{
				if(ackAlarmCounts == null || ackAlarmCounts.getRowCount() == 0)
				{
					log.error("receiveAlarmsAckCounts was called however there was nothing to insert from Gateway : " + agentID);
				}
				else
				{
					int alarmsSize = ackAlarmCounts.getRowCount();
					int i = 0;
					Datasource _ds;
					
					_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					String sqlQuery = "";
					SRConnection con = null;
					Dataset dataSet = null;
					try
					{
						con = _ds.getConnection();
						
							
						for(i=0; i<alarmsSize; i++)
						{
							sqlQuery = "SELECT PRIORITY FROM mod_ia_aggregates_daily_alarm_ack_counts"
									+ " WHERE GATEWAY_ID = '" + agentID + "' "
									+ " and ALARM_DATE = '" + sdf.format(date) + "'"
									+ " and PRIORITY = '" + ackAlarmCounts.getValueAt(i,0) + "';";
							dataSet = con.runQuery(sqlQuery);

							if(dataSet != null && dataSet.getRowCount() > 0)
							{
								sqlQuery = "UPDATE mod_ia_aggregates_daily_alarm_ack_counts"
										+ " SET ACK_COUNT = " + ackAlarmCounts.getValueAt(i,1)
										+ " WHERE GATEWAY_ID = '" + agentID + "' "
										+ " and ALARM_DATE = '" + sdf.format(date) + "'"
										+ " and PRIORITY = '" + ackAlarmCounts.getValueAt(i,0) + "';";
								con.runUpdateQuery(sqlQuery);
							}
							else
							{
								sqlQuery = "INSERT INTO mod_ia_aggregates_daily_alarm_ack_counts"
										+ "(GATEWAY_ID, ALARM_DATE, PRIORITY, ACK_COUNT) VALUES (" 
										+ "'" + agentID + "',"
										+ "'" + sdf.format(date) + "',"
										+ "'" + ackAlarmCounts.getValueAt(i,0) + "',"
										+ ackAlarmCounts.getValueAt(i,1)+ ");";
								
								con.runUpdateQuery(sqlQuery);
							}
							
								
						}
//						log.error("receiveAlarmsAckCounts : " + sqlQuery);
					}
					catch(Exception e){
						returnValue = false;
						e.printStackTrace();
					}
					finally{
						if(con!=null){
							try {
								con.close();
							} catch (SQLException e) {
								log.error("receiveAlarmsAckCounts : in con close exception.");
								
								e.printStackTrace();
							}
						}
					}
				}
			}
			return returnValue;
		}

		/*
		 * Yogini 18-Jan-2017
		 * To receive alarm active counts data from Agent 
		 */
		@Override
		public boolean receiveAlarmsActiveCounts(String agentID, Date date, Dataset activeAlarmCounts) {
			boolean returnValue = true;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if(this.isGatewayMonitored(agentID))
			{
				if(activeAlarmCounts == null || activeAlarmCounts.getRowCount() == 0)
				{
					log.error("receiveAlarmsActiveCounts was called however there was nothing to insert from Gateway : " + agentID);
				}
				else
				{
					int alarmsSize = activeAlarmCounts.getRowCount();
					int i = 0;
					Datasource _ds;
					
					_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					String sqlQuery = "";
					SRConnection con = null;
					try
					{
						con = _ds.getConnection();
						
						Dataset dataSet = null;
						for(i=0; i<alarmsSize; i++)
						{
							
							sqlQuery = "SELECT PRIORITY FROM mod_ia_aggregates_daily_alarm_active_counts"
									+ " WHERE GATEWAY_ID = '" + agentID + "' "
									+ " and ALARM_DATE = '" + sdf.format(date) + "'"
									+ " and PRIORITY = '" + activeAlarmCounts.getValueAt(i,0) + "';";
							dataSet = con.runQuery(sqlQuery);

							if(dataSet != null && dataSet.getRowCount() > 0)
							{
								sqlQuery = "UPDATE mod_ia_aggregates_daily_alarm_active_counts"
										+ " SET ACTIVE_COUNT = " + activeAlarmCounts.getValueAt(i,1)
										+ " WHERE GATEWAY_ID = '" + agentID + "' "
										+ " and ALARM_DATE = '" + sdf.format(date) + "'"
										+ " and PRIORITY = '" + activeAlarmCounts.getValueAt(i,0) + "';";
								con.runUpdateQuery(sqlQuery);
							}
							else
							{
								sqlQuery = "INSERT INTO mod_ia_aggregates_daily_alarm_active_counts"
										+ "(GATEWAY_ID, ALARM_DATE, PRIORITY, ACTIVE_COUNT) VALUES (" 
										+ "'" + agentID + "',"
										+ "'" + sdf.format(date) + "',"
										+ "'" + activeAlarmCounts.getValueAt(i,0) + "',"
										+ activeAlarmCounts.getValueAt(i,1)+ ");";
								
								con.runUpdateQuery(sqlQuery);
							}
							
							
							
						}
//						log.error("receiveAlarmsActiveCounts : " + sqlQuery);
					}
					catch(Exception e){
						returnValue = false;
						e.printStackTrace();
					}
					finally{
						if(con!=null){
							try {
								con.close();
							} catch (SQLException e) {
								log.error("receiveAlarmsActiveCounts : in con close exception.");
								
								e.printStackTrace();
							}
						}
					}
				}
			}
			return returnValue;
		}

		/*
		 * Yogini 18-Jan-2017
		 * To receive location data from Agent 
		 */
		@Override
		public boolean receiveLocationInfo(String agentID, Dataset locationInfo) {
			boolean returnValue = true;
			
			if(this.isGatewayMonitored(agentID))
			{
				if(locationInfo == null || locationInfo.getRowCount() == 0)
				{
					log.error("receiveLocationInfo was called however there was nothing to insert from Gateway : " + agentID);
				}
				else
				{
					int locationsSize = locationInfo.getRowCount();
					int i = 0;
					Datasource _ds;
					
					_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					String sqlQuery = "";
					SRConnection con = null;
					try
					{
						con = _ds.getConnection();
						
						Dataset dataSet = null;
						
						
						for(i=0; i<locationsSize; i++)
						{
							
							sqlQuery = "SELECT INTERNAL_IP FROM mod_ia_aggregates_location_info"
									+ " WHERE GATEWAY_ID = '" + agentID + "'"
									+ " AND INTERNAL_IP = '" + locationInfo.getValueAt(i,0) + "' "
									+ " AND EXTERNAL_IP = '" + locationInfo.getValueAt(i,1) + "' " 
									;
							
							dataSet = con.runQuery(sqlQuery);
							
							if(dataSet == null ||  dataSet.getRowCount() == 0)
							{
								
								sqlQuery = "INSERT INTO mod_ia_aggregates_location_info"
										+ "(GATEWAY_ID, INTERNAL_IP, EXTERNAL_IP, CITY, STATE, COUNTRY, LATITUDE, LONGITUDE ) VALUES (" 
										+ "'" + agentID + "',"
										+ "'" + locationInfo.getValueAt(i,0) + "',"
										+ "'" + locationInfo.getValueAt(i,1) + "',"
										+ "'" + locationInfo.getValueAt(i,2) + "',"
										+ "'" + locationInfo.getValueAt(i,3) + "',"
										+ "'" + locationInfo.getValueAt(i,4) + "',"
										+ locationInfo.getValueAt(i,5) + ","
										+ locationInfo.getValueAt(i,6)+ ");";
								con.runUpdateQuery(sqlQuery);
							}
							
						}
						//log.error("receive location info : ins q : " + sqlQuery);
					}
					catch(Exception e){
						returnValue = false;
						e.printStackTrace();
					}
					finally{
						if(con!=null){
							try {
								con.close();
							} catch (SQLException e) {
								log.error("receiveLocationInfo : in con close exception.");
								
								e.printStackTrace();
							}
						}
					}
				}
			}
			return returnValue;
		}

		/*
		 * Yogini 18-Jan-2017
		 * To send  alarm ack counts data to controller
		 */
		@Override
		public boolean sendAlarmsAckCounts() {
			boolean returnValue = true;
			
			
				Dataset resDS = null;
				
				resDS = this.getAckAlarmsCount("", true);
				
				if(resDS != null && resDS.getRowCount() > 0)
				{
					//call service on Controller to send data
					GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
					if(this.isAgent)
					{
						ServiceManager sm = gm.getServiceManager();
						ServerId sid = new ServerId(this.controllerName);
						ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
//						log.error("sendAlarmsAckCounts : on agent sid = " + sid.getServerName());
						//if service is available
						if(s == ServiceState.Available)
						{
							//call the service 
							sm.getService(sid, GetAnalyticsInformationService.class).get().receiveAlarmsAckCounts( gm.getServerAddress().getServerName(), new Date(), resDS);
						}
						else
						{
							returnValue = false;
						}
					}
					else
					{
						this.receiveAlarmsAckCounts(gm.getServerAddress().getServerName(), new Date(), resDS);
					}
				}
			
			return returnValue;
		}

		/*
		 * Yogini 18-Jan-2017
		 * To send  alarm active counts data to controller
		 */
		@Override
		public boolean sendAlarmsActiveCounts() {
			boolean returnValue = true;
			
			Dataset resDS = null;
			resDS = this.getActiveAlarmsCount("", true);
			
			if(resDS != null && resDS.getRowCount() > 0)
			{
				//call service on Controller to send data
				GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
				if(this.isAgent)
				{
					ServiceManager sm = gm.getServiceManager();
					ServerId sid = new ServerId(this.controllerName);
					ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
//					log.error("sendAlarmsActiveCounts : on agent sid = " + sid.getServerName());
					//if service is available
					if(s == ServiceState.Available)
					{
						//call the service 
						sm.getService(sid, GetAnalyticsInformationService.class).get().receiveAlarmsActiveCounts( gm.getServerAddress().getServerName(), new Date(), resDS);
					}
					else
					{
						returnValue = false;
					}
				}
				else
				{
					this.receiveAlarmsActiveCounts(gm.getServerAddress().getServerName(), new Date(), resDS);
				}
			}
		
		return returnValue;
		}

		@Override
		public boolean sendLocationInfo() {
			boolean returnValue = true;
			
			Datasource _ds;
			Dataset resDS = null;
			
			_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			UserSyncRecord _record;
			try
			{
				con = _ds.getConnection();
				sqlQuery = "SELECT INTERNAL_IP, EXTERNAL_IP,CITY, STATE, COUNTRY, LATITUDE, LONGITUDE"
						+ " FROM MOD_IA_LOCATION_INFO;" ; 
				resDS = con.runQuery(sqlQuery);
				
			}
			catch(Exception e){
				returnValue = false;
				e.printStackTrace();
			}
			finally{
				if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("sendLocationInfo : in con close exception.");
						
						e.printStackTrace();
					}
				}
			}
			
			if(resDS != null && resDS.getRowCount() > 0)
			{
				//call service on Controller to send data
				GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
				if(this.isAgent)
				{
					ServiceManager sm = gm.getServiceManager();
					ServerId sid = new ServerId(this.controllerName);
					ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
//					log.error("sendAlarmsActiveCounts : on agent sid = " + sid.getServerName());
					//if service is available
					if(s == ServiceState.Available)
					{
						//call the service 
						sm.getService(sid, GetAnalyticsInformationService.class).get().receiveLocationInfo(gm.getServerAddress().getServerName(), resDS);
					}
					else
					{
						returnValue = false;
					}
				}
				else
				{
					this.receiveLocationInfo(gm.getServerAddress().getServerName(), resDS);
				}
			}
		
		return returnValue;
		}

		@Override
		public Dataset getAllLoggedInUsersOnController(boolean allProjects, String projectName, boolean allGateways,
				String gatewayName) {
			
			Dataset users = null;
			Datasource _ds;
			
			_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			String sqlQuery = "";
			SRConnection con = null;
			
			if(allGateways && allProjects)
			{
				sqlQuery = "SELECT gateway_id, email, username, roles, gateway_userprofile, current_location, first_seen, last_seen,"
						+ " last7days_duration, last7days_visits, last7days_actions, "
						+ " all_actions, all_screen_views, current_screen, online_status, projectname from mod_ia_aggregates_users"
						+ " where projectname = 'All'";
			}
			else if(allGateways && !allProjects)
			{
				sqlQuery = "SELECT a.gateway_id as gateway_id, email, username, roles, gateway_userprofile, current_location, first_seen, last_seen,"
						+ " last7days_duration, last7days_visits, last7days_actions, "
						+ " all_actions, all_screen_views, current_screen, online_status, projectname "
						+ " from mod_ia_aggregates_users a , mod_ia_aggregates_projects b"
						+ " where a.gateway_id = b.GATEWAY_ID and "
						+ " a.projectname = '" + projectName + "' and "
						+ " b.project_name = '" + projectName + "' and "
						+ " a.gateway_userprofile = b.AUTH_PROFILE";
			}
			else if(!allGateways && allProjects)
			{
				sqlQuery = "SELECT gateway_id, email, username, roles, gateway_userprofile, current_location, first_seen, last_seen,"
						+ " last7days_duration, last7days_visits, last7days_actions, "
						+ " all_actions, all_screen_views, current_screen, online_status, projectname from mod_ia_aggregates_users where gateway_id = '"
						+ gatewayName + "' and projectname = 'All';";
			}
			else if(!allGateways && !allProjects)
			{
				sqlQuery = "SELECT a.gateway_id as gateway_id, email, username, roles, gateway_userprofile, current_location, first_seen, last_seen,"
						+ " last7days_duration, last7days_visits, last7days_actions, "
						+ " all_actions, all_screen_views, current_screen, online_status, projectname "
						+ " from mod_ia_aggregates_users a , mod_ia_aggregates_projects b"
						+ " where a.gateway_id = '" + gatewayName + "' and "
						+ " b.gateway_id = '" + gatewayName + "' and "
						+ " a.projectname = '" + projectName + "' and "
						+ " b.project_name = '" + projectName + "' and "
						+ " a.gateway_id = b.GATEWAY_ID and"
						+ " a.gateway_userprofile = b.AUTH_PROFILE";
			}
			
			try
			{
				con = _ds.getConnection();
//				log.error("getAllLoggedInUsersOnController : sql q " + sqlQuery);
				users = con.runQuery(sqlQuery);
				
			}
			catch(Exception e){
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getAllLoggedInUsersOnController : in con close exception.");
						
						e.printStackTrace();
					}
				}
			}
			return users;
		}
		
		
		/**
		 * 
		 */
		@Override
		
		public Dataset getScreenViewsPerUserPerVisitOnController(int duration, String projectName, boolean allProjects,String selectedUser, String selectedUserProfile, String gatewayName, boolean showActions) {
				Datasource ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				Dataset results = null;
				String sqlQuery = "";
				SRConnection con = null;
				
				String dateFilter = getDateFilterOnController(duration, "c.view_timestamp");
				
				if(showActions)
				{
//					log.error("getScreenViewsPerUserPerVisitOnController : showActionns = true");
					if(allProjects)
					{
						sqlQuery = "select x.ScreenName as ScreenName, x.User as User, x.SessionStart as SessionStart, x.SessionEnd as SessionEnd,  x.VIEW_TIMESTAMP as VIEW_TIMESTAMP, y.location as location,"
								+ " x.ACTIONS as 'actions' , x.Project as 'Project', x.gateway as 'Gateway', x.action as action, x.action_value as action_value "
								+ " from "
								+ " (select a.screen_name as 'ScreenName', a.username as User, dt.session_start as 'SessionStart',"
								+ " dt.session_end as 'SessionEnd', a.VIEW_TIMESTAMP as VIEW_TIMESTAMP, (dt.NO_OF_ACTIONS + dt.NO_OF_SCREENS) as 'actions' "
								+ " , a.PROJECT as 'Project' , a.GATEWAY_ID as 'gateway', a.action as action, a.action_value as action_value " 
								+ " from "
								+ " ("
								+ " SELECT screen_name, username, project, gateway_id, view_timestamp, action, action_value"
								+ " from "
								+ " ( "
								+ " SELECT action as screen_name, username as username, project as project, gateway_id as gateway_id, view_timestamp as view_timestamp, screen_name as action, '' as action_value"
								+ " from mod_ia_aggregates_daily_screen_views "
								+ " union all"
								+ " select action as screen_name, actor as username, project as project, gateway_id as gateway_id , event_timestamp as view_timestamp, action_target as action, action_value as action_value"
								+ " from mod_ia_aggregates_actions"
								+ " ) as c) as a , "
								+ " (SELECT *"
								+ " FROM mod_ia_aggregates_daily_sessions"
								+ " where username = '"+selectedUser+"'"
								+ " and PROJECT_NAME in (select PROJECT_NAME "
								+ " from mod_ia_aggregates_projects where AUTH_PROFILE='" + selectedUserProfile + "' "
										+ " and gateway_id = '"+ gatewayName + "')"
								+ " and gateway_id = '" + gatewayName +"' order by session_start desc limit 10) as dt"
								+ " where a.gateway_id = '" + gatewayName + "' and a.gateway_id = dt.gateway_id and a.username = '"+ selectedUser + "' and a.screen_name != 'SCREEN_CLOSE' and a.PROJECT = dt.PROJECT_NAME" // + dateFilter 
								+ " and "
								+ " ("
								+ " (dt.session_start != dt.session_end and a.view_timestamp <= dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start )"
								+ " ||"
								+ "	(dt.session_start = dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start)"
								+ " )"
								+ " order by SessionStart desc, SessionEnd , a.VIEW_TIMESTAMP desc) x, "
								+ " (select a.username as username, a.start_timestamp, a.host_internal_ip, a.host_external_ip, a.project as project, concat (b.city , ','  , b.state, ',' , b.country) as location"
								+ " from mod_ia_aggregates_clients a, mod_ia_aggregates_location_info b "
								+ " where a.gateway_id = '" + gatewayName + "' and a.gateway_id = b.gateway_id and a.host_internal_ip = b.internal_ip and a.host_external_ip = b.external_ip ) y "
								+ " where x.User = y.username and y.start_timestamp >= x.SessionStart and y.start_timestamp <= x.SessionEnd "
								+ " and x.project = y.project order by x.sessionStart desc, x.sessionEnd, x.VIEW_TIMESTAMP desc;";
					}
					else 
					{
						sqlQuery = "select x.ScreenName as ScreenName, x.User as User, x.SessionStart as SessionStart, x.SessionEnd as SessionEnd,  x.VIEW_TIMESTAMP as VIEW_TIMESTAMP, y.location as location "
								+ " , x.ACTIONS as 'actions' , x.Project as 'Project', x.gateway as 'Gateway' , x.action as action, x.action_value as action_value from "
								+ " ( select a.screen_name as 'ScreenName', a.username as User, dt.session_start as 'SessionStart',"
								+ " dt.session_end as 'SessionEnd',a.VIEW_TIMESTAMP , (dt.NO_OF_ACTIONS + dt.NO_OF_SCREENS) as 'actions', "
								+ " a.PROJECT as 'Project' , a.GATEWAY_ID as 'gateway' , a.action as action, a.action_value as action_value" 
								+ " from "
								+ " ("
								+ " SELECT screen_name, username, project, gateway_id, view_timestamp, action, action_value"
								+ " from "
								+ " ( "
								+ " SELECT action as screen_name, username as username, project as project, gateway_id as gateway_id, view_timestamp as view_timestamp, screen_name as action, '' as action_value"
								+ " from mod_ia_aggregates_daily_screen_views "
								+ " union all "
								+ " select action as screen_name, actor as username, project as project, gateway_id as gateway_id , event_timestamp as view_timestamp, action_target as action, action_value as action_value"
								+ " from mod_ia_aggregates_actions"
								+ " ) as c) as a , "
								+ " (SELECT *"
								+ " FROM mod_ia_aggregates_daily_sessions"
								+ " where username = '"+selectedUser+"'"
										+ " and PROJECT_NAME = '" + projectName + "' "
												+ " and gateway_id = '" + gatewayName + "'"
								+ " order by session_start desc limit 10) as dt"
								+ " where  a.gateway_id = '" + gatewayName + "' and a.gateway_id = dt.gateway_id and a.username = '"+ selectedUser + "' and a.screen_name != 'SCREEN_CLOSE' and a.PROJECT = dt.PROJECT_NAME" // + dateFilter 
								+ " and "
								+ " ("
								+ " (dt.session_start != dt.session_end and a.view_timestamp <= dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start )"
								+ " || "
								+ " (dt.session_start = dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start)"
								+ " ) "
								+ " order by SessionStart desc, SessionEnd , a.VIEW_TIMESTAMP desc ) x, "
								+ " (select a.username as username, a.start_timestamp, a.host_internal_ip, a.host_external_ip, a.project as project, concat (b.city , ','  , b.state, ',' , b.country) as location"
								+ " from mod_ia_aggregates_clients a, mod_ia_aggregates_location_info b "
								+ " where a.gateway_id = '" + gatewayName + "' and a.gateway_id = b.gateway_id and a.host_internal_ip = b.internal_ip and a.host_external_ip = b.external_ip ) y "
								+ " where x.User = y.username and y.start_timestamp >= x.SessionStart and y.start_timestamp <= x.SessionEnd"
								+ " and x.project = y.project order by x.sessionStart desc, x.sessionEnd, x.VIEW_TIMESTAMP desc;";
					}
					
					//log.error("getScreenViewsPerUserPerVisitOnController : showActions true sqlQuery : " + sqlQuery);
				}
				else
				{
					if(allProjects)
					{
						sqlQuery = "select x.ScreenName as ScreenName, x.User as User, x.SessionStart as SessionStart, x.SessionEnd as SessionEnd,  x.VIEW_TIMESTAMP as VIEW_TIMESTAMP, y.location as location,"
								+ " x.ACTIONS as 'actions' , x.Project as 'Project', x.gateway as 'Gateway', '' as action, '' as action_value"
								+ " from "
								+ " (select distinct a.screen_name as 'ScreenName', a.username as User, dt.session_start as 'SessionStart',"
								+ " dt.session_end as 'SessionEnd', a.VIEW_TIMESTAMP as VIEW_TIMESTAMP, (dt.NO_OF_ACTIONS + dt.NO_OF_SCREENS) as 'actions' "
								+ " , a.PROJECT as 'Project' , a.GATEWAY_ID as 'gateway' " 
								+ " from mod_ia_aggregates_daily_screen_views a , (SELECT *"
								+ " FROM mod_ia_aggregates_daily_sessions"
								+ " where username = '"+selectedUser+"'"
								+ " and PROJECT_NAME in (select PROJECT_NAME "
								+ " from mod_ia_aggregates_projects where AUTH_PROFILE='" + selectedUserProfile + "' "
										+ " and gateway_id = '"+ gatewayName + "')"
								+ " and gateway_id = '" + gatewayName +"' order by session_start desc limit 10) as dt"
								+ " where a.gateway_id = '" + gatewayName + "' and a.gateway_id = dt.gateway_id and a.username = '"+ selectedUser + "' and a.action = 'SCREEN_OPEN' and a.PROJECT = dt.PROJECT_NAME" // + dateFilter 
								+ " and "
								+ " ("
								+ " (dt.session_start != dt.session_end and a.view_timestamp <= dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start )"
								+ " ||"
								+ "	(dt.session_start = dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start)"
								+ " )"
								+ " order by SessionStart desc, SessionEnd , a.VIEW_TIMESTAMP desc) x, "
								+ " (select a.username as username, a.start_timestamp, a.host_internal_ip, a.host_external_ip, a.project as project, concat (b.city , ','  , b.state, ',' , b.country) as location"
								+ " from mod_ia_aggregates_clients a, mod_ia_aggregates_location_info b "
								+ " where a.gateway_id = '" + gatewayName + "' and a.gateway_id = b.gateway_id and a.host_internal_ip = b.internal_ip and a.host_external_ip = b.external_ip ) y "
								+ " where x.User = y.username and y.start_timestamp >= x.SessionStart and y.start_timestamp <= x.SessionEnd "
								+ " and x.project = y.project order by x.sessionStart desc, x.sessionEnd, x.VIEW_TIMESTAMP desc;";
					}
					else 
					{
						sqlQuery = "select x.ScreenName as ScreenName, x.User as User, x.SessionStart as SessionStart, x.SessionEnd as SessionEnd,  x.VIEW_TIMESTAMP as VIEW_TIMESTAMP, y.location as location "
								+ " , x.ACTIONS as 'actions' , x.Project as 'Project', x.gateway as 'Gateway' , '' as action, '' as action_value from "
								+ " ( select distinct a.screen_name as 'ScreenName', a.username as User, dt.session_start as 'SessionStart',"
								+ " dt.session_end as 'SessionEnd',a.VIEW_TIMESTAMP , (dt.NO_OF_ACTIONS + dt.NO_OF_SCREENS) as 'actions', "
								+ " a.PROJECT as 'Project' , a.GATEWAY_ID as 'gateway' " 
								+ " from mod_ia_aggregates_daily_screen_views a ,(SELECT *"
								+ " FROM mod_ia_aggregates_daily_sessions"
								+ " where username = '"+selectedUser+"'"
										+ " and PROJECT_NAME = '" + projectName + "' "
												+ " and gateway_id = '" + gatewayName + "'"
								+ " order by session_start desc limit 10) as dt"
								+ " where a.gateway_id = '" + gatewayName + "' and a.gateway_id = dt.gateway_id and a.username = '"+ selectedUser + "' and a.action = 'SCREEN_OPEN' and a.PROJECT = dt.PROJECT_NAME" // + dateFilter 
								+ " and "
								+ " ("
								+ " (dt.session_start != dt.session_end and a.view_timestamp <= dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start )"
								+ " || "
								+ " (dt.session_start = dt.session_end and a.VIEW_TIMESTAMP >= dt.session_start)"
								+ " ) "
								+ " order by SessionStart desc, SessionEnd , a.VIEW_TIMESTAMP desc ) x, "
								+ " (select a.username as username, a.start_timestamp, a.host_internal_ip, a.host_external_ip, a.project as project, concat (b.city , ','  , b.state, ',' , b.country) as location"
								+ " from mod_ia_aggregates_clients a, mod_ia_aggregates_location_info b "
								+ " where a.gateway_id = '" +gatewayName +"' and a.gateway_id = b.gateway_id and a.host_internal_ip = b.internal_ip and a.host_external_ip = b.external_ip ) y "
								+ " where x.User = y.username and y.start_timestamp >= x.SessionStart and y.start_timestamp <= x.SessionEnd"
								+ " and x.project = y.project order by x.sessionStart desc, x.sessionEnd, x.VIEW_TIMESTAMP desc;";
					}
				}
				
				try {
//					log.error("getScreenViewsPerUserPerVisitOnController : sql query : " + sqlQuery + "\n");
							con = ds.getConnection();
							results = con.runQuery(sqlQuery);
							
						}
							catch (SQLException e) {
					
						log.error(e);
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("getScreenViewsPerUserPerVisitOnController : in con close exception.");
							log.error(e);
						}
						
						}
					}	
				return results;
			}

		@Override
		public boolean receiveAuditActions(String agentID, Dataset auditActions) {
			boolean returnValue = true;
			
			if(this.isGatewayMonitored(agentID))
			{
				if(auditActions == null || auditActions.getRowCount() == 0)
				{
					log.error("receiveAuditActions was called however there was nothing to insert from Gateway : " + agentID);
				}
				else
				{
					int actionsSize = auditActions.getRowCount();
					int i = 0;
					Datasource _ds;
					
					_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					String sqlQuery = "";
					SRConnection con = null;
					try
					{
						con = _ds.getConnection();
						
						sqlQuery = "DELETE FROM mod_ia_aggregates_actions"
								+ " WHERE GATEWAY_ID = '" + agentID + "' and"
										+ " EVENT_TIMESTAMP >= '"
								+ auditActions.getValueAt(0 , "EVENT_TIMESTAMP").toString()
								+ "';";
						
						con.runUpdateQuery(sqlQuery);
						
						sqlQuery = "INSERT INTO mod_ia_aggregates_actions"
								+ "(GATEWAY_ID, EVENT_TIMESTAMP, ACTOR, ACTION, ACTION_VALUE, PROJECT, ACTION_TARGET) VALUES (";
						String project = "", timeStamp = "";
						for(i=0; i<actionsSize; i++)
						{
							project = "";
	//						log.error("receiveAudit actions timeStamp value : " + auditActions.getValueAt(i , "EVENT_TIMESTAMP").toString() + ", i =  " + i);
							if (auditActions.getValueAt(i , "EVENT_TIMESTAMP").toString().contains("."))
							{
								timeStamp = (auditActions.getValueAt(i , "EVENT_TIMESTAMP").toString()).substring(0, 19);
							}
							else
							{
								timeStamp = auditActions.getValueAt(i , "EVENT_TIMESTAMP").toString();
							}
							if(auditActions.getValueAt(i,"ORIGINATING_SYSTEM") != null)
							{
								project = auditActions.getValueAt(i,"ORIGINATING_SYSTEM").toString().split("=")[1];
							}
							if(i == actionsSize - 1)
							{
								sqlQuery = sqlQuery 
									+ "'" + agentID + "',"
									+ "'" + timeStamp + "',"
									+ "'" + auditActions.getValueAt(i,"ACTOR") + "',"
									+ "'" + auditActions.getValueAt(i,"ACTION") + "',"
									+ "'" + auditActions.getValueAt(i,"ACTION_VALUE") + "',"
									+ "'" + project + "',"
									+ "'" + auditActions.getValueAt(i,"ACTION_TARGET") + "'"
	 										+ ");";
							}
							else
							{
								sqlQuery = sqlQuery 
										+ "'" + agentID + "',"
										+ "'" + timeStamp + "',"
										+ "'" + auditActions.getValueAt(i,"ACTOR") + "',"
										+ "'" + auditActions.getValueAt(i,"ACTION") + "',"
										+ "'" + auditActions.getValueAt(i,"ACTION_VALUE") + "',"
										+ "'" + project + "', "
										+ "'" + auditActions.getValueAt(i,"ACTION_TARGET") + "'"
												+ "),(";
							}
							
						}
						//log.error("receive auditActions  : ins q : " + sqlQuery);
						con.runUpdateQuery(sqlQuery);	
					}
					catch(Exception e){
						returnValue = false;
						e.printStackTrace();
					}
					finally{
						if(con!=null){
							try {
								con.close();
							} catch (SQLException e) {
								log.error("receiveLocationInfo : in con close exception.");
								
								e.printStackTrace();
							}
						}
					}
				}
			}
			return returnValue;
		}

		@Override
		public boolean sendAuditActions() {
			boolean returnValue = true;
			
			Datasource _ds;
			Dataset resDS = null;
			
			_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			LastSyncData _lstSync = LastSyncData.getInstance(_ds);
			
			String sqlQuery = "";
			SRConnection con = null;
			
			try
			{
				con = _ds.getConnection();
				if(_lstSync.getLast_sync_audit_date() != null)
				{
					sqlQuery = "SELECT EVENT_TIMESTAMP, ACTOR, ACTION, ACTION_VALUE, ORIGINATING_SYSTEM, ACTION_TARGET "
							+ " FROM AUDIT_EVENTS WHERE STATUS_CODE = 0 and EVENT_TIMESTAMP >= '" 
							+ _lstSync.getLast_sync_audit_date() + "' "
							+ " and EVENT_TIMESTAMP >= '" + this.installDate + "' "
							+ " order by EVENT_TIMESTAMP ASC;";	
				}
				else
				{
					sqlQuery = "SELECT EVENT_TIMESTAMP, ACTOR, ACTION, ACTION_VALUE, ORIGINATING_SYSTEM , ACTION_TARGET"
							+ " FROM AUDIT_EVENTS WHERE STATUS_CODE = 0 "
							+ " and EVENT_TIMESTAMP >= '" + this.installDate + "' "
							+ " order by EVENT_TIMESTAMP ASC;";	
					
				}
				resDS = con.runQuery(sqlQuery);
				
			}
			catch(Exception e){
				returnValue = false;
				e.printStackTrace();
			}
			finally{
				if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("sendAuditActions : in con close exception.");
						
						e.printStackTrace();
					}
				}
			}
			
			if(resDS != null && resDS.getRowCount() > 0)
			{
				//call service on Controller to send data
				GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
				if(this.isAgent)
				{
					ServiceManager sm = gm.getServiceManager();
					ServerId sid = new ServerId(this.controllerName);
					ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
//					log.error("sendAuditActions : on agent sid = " + sid.getServerName());
					//if service is available
					if(s == ServiceState.Available)
					{
						//call the service 
						sm.getService(sid, GetAnalyticsInformationService.class).get().receiveAuditActions(gm.getServerAddress().getServerName(), resDS);
					}
					else
					{
						returnValue = false;
					}
				}
				else
				{
					this.receiveAuditActions(gm.getServerAddress().getServerName(), resDS);
				}
				if (returnValue == true)
				{
					_lstSync.setLast_sync_audit_date(resDS.getValueAt(resDS.getRowCount() - 1, "EVENT_TIMESTAMP").toString());
				}
			}
			
		
		return returnValue;
		}
		
		
		
		/**************************
		 * Following functions added by Sayali for Reports on Enterprise Gateway
		 */
		
		@Override
		public Dataset getTopProjectsController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects) {

			Dataset repProjects = null;
			Datasource ds;
			String sqlQuery = "";

			SRConnection con = null;

			
			String timefilter = getDateFilterOnController(duration, "START_TIMESTAMP");
			String timefilter1 = getDateFilterOnController(duration, "SESSION_START");
			String filter = "";
			String filter1 = "";
			if(allGateways)
			{
				filter = "";
				filter1 = "";
			}
			else
			{
				filter = " and a.GATEWAY_ID = '" + GatewayName + "' ";
				filter1 = " and GATEWAY_ID = '" + GatewayName + "' ";
			}
			

			sqlQuery = "select  tb2.GATEWAY_ID as 'Gateway_Name', tb2.Project_Name as 'Project_Name', tb1.People as 'People',"
					+ "  tb2.Visits as 'Visits', tb2.Actions as 'Actions' from "
					+ " ( SELECT a.gateway_id, a.project, count( distinct(concat(a.username,b.AUTH_PROFILE,a.gateway_id))) as People"
					+ " FROM mod_ia_aggregates_clients a, mod_ia_aggregates_projects b where " 
					+ timefilter
					+ " and  a.gateway_id = b.gateway_id and a.PROJECT = b.PROJECT_NAME" + filter
					+ " group by a.project, a.gateway_id ) as tb1,"
					+ " (select  GATEWAY_ID , Project_Name as 'Project_Name', sum(no_of_screens + no_of_actions) as 'Actions', count(session_start) as 'Visits'"
					+ " from mod_ia_aggregates_daily_sessions where "
					+ timefilter1 + filter1
					+ " group by project_Name, GATEWAY_ID) as tb2 where tb1.gateway_id = tb2.gateway_id and"
					+ " tb1.PROJECT = tb2.PROJECT_NAME order by  tb2.Visits desc;";
			
//			log.error("Top_projects query: " + sqlQuery);
			// connect to the database and get records
			try {

				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				repProjects = con.runQuery(sqlQuery);
			} catch (Exception e) {
				log.error("GetTopProjectsController : " + e);
			} finally {
				// close the database connection
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						log.error("GetTopProjectsController : in con close exception.");

						e.printStackTrace();
					}

				}
			}
			return repProjects;
		}
		
		
		/**
		 * Method to retrieve average time to acknowledge alarms per priority
		 * @author sayali on 18-01-2017
		 * @param duration
		 * @param dataSource
		 * @return Dataset with Priority wise average time.
		 */
		@Override
		public Dataset getAlarmsAckTimeController(int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects) {
			Datasource ds;
			Dataset resDS = null;
			
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			
			String sqlQuery = "";
			SRConnection con = null;
			String dateFilter = getDateFilterOnController(duration, "ALARM_DATE");	
			
			int month = getLastMonth();
			int year = getThisYear();
			int thisYear = getThisYear();
			if(month == 0)
			{
				month = 12;
				year = getLastYear();
			}
			
			int lastYear = getLastYear();
			if(allGateways){
				sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_ACK,0.0)))), '%H:%i:%s')"
						+ " FROM MOD_IA_AGGREGATES_DAILY_ALARMS_SUMMARY WHERE "
						+ dateFilter + " group by ALARM_PRIORITY";	
			}else{
				
				sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_ACK,0.0)))), '%H:%i:%s')"
						+ " FROM MOD_IA_AGGREGATES_DAILY_ALARMS_SUMMARY WHERE GATEWAY_ID = '" + GatewayName + "' and "
						+ dateFilter + " group by ALARM_PRIORITY";	
			}
			//System.out.println("getAlarmsAckTimeController"+sqlQuery);
			
			int r=0;
				try {
						con = ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
						}
						catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
					
					}
			}
			
			
			return resDS;
		}
		
		
		/**
		 * sayali 9 jan 2017 Method to retrieve Top Screens information Returns a
		 * Dataset with Screen name, No Of People and No of Actions(Screen views)
		 */
		public Dataset getTopScreensController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects) {

			Dataset repScreens = null;
			Datasource ds;
			String sqlQuery = "";

			SRConnection con = null;
			String timefilter = getDateFilterOnController(duration, "VIEW_TIMESTAMP");
			String filter = "";
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(!allProjects && allGateways)
			{
				filter = " and PROJECT = '" + projectName + "' ";
			}
			else if(allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + GatewayName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + GatewayName + "' and PROJECT = '" + projectName + "' ";
			}
			
			sqlQuery = "SELECT GATEWAY_ID as 'Gateway_Name', PROJECT as 'Project_Name', SCREEN_NAME as 'PageViews', COUNT(DISTINCT USERNAME) as 'People',"
						+ " COUNT(ACTION) as 'Actions'"
						+ " FROM mod_ia_aggregates_daily_screen_views where action = 'SCREEN_OPEN' and "
						+ timefilter + filter
						+ " group by SCREEN_NAME, PROJECT, GATEWAY_ID order by Actions desc,  People desc;";

			//System.out.println("Top_screen query: " + sqlQuery);

			// connect to the database and get records
			try {

				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				repScreens = con.runQuery(sqlQuery);
			} catch (Exception e) {
				log.error("GetTopScreensController : " + e);
			} finally {
				// close the database connection
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getTopScreensController : in con close exception.");

						e.printStackTrace();
					}

				}
			}
			return repScreens;
		}
		
		
		/* Report for Platform Report. By sayali */
		public Dataset getPlatformReportController(int duration,
				String GatewayName, String projectName, Boolean allGateways,
				boolean allProjects) {
			Dataset platformDataSet = null;
			Datasource dsname;
			String sqlQuery = "";

			SRConnection con = null;

			String timefilter = getDateFilterOnController(duration, "START_TIMESTAMP");
			String filter= "";

			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(!allProjects && allGateways)
			{
				filter = " and b.PROJECT = '" + projectName + "' ";
			}
			else if(allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' and b.PROJECT = '" + projectName + "' ";
			}
			
				sqlQuery = "select dt.OS_NAME as OS_NAME, count(distinct(concat(dt.username,c.AUTH_PROFILE,dt.gateway_id)))  as people,"
						+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
						+ " from ("
						+ " select distinct b.OS_NAME as OS_NAME, "
						+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
						+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT, b.GATEWAY_ID as GATEWAY_ID"
						+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b"
						+ " where "
						+ timefilter + filter
						+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
						+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID and a.session_start != a.session_end and a.username = b.username"
						+ " ) as dt,"
						+ " mod_ia_aggregates_projects c"
						+ " where dt.PROJECT = c.PROJECT_NAME and dt.GATEWAY_ID = c.GATEWAY_ID  group by OS_NAME ;";
			try {

				dsname = mycontext.getDatasourceManager().getDatasource(
						this.moduleDS);
				con = dsname.getConnection();
//				log.error("Platform query: "+sqlQuery);
				platformDataSet = con.runQuery(sqlQuery);

			} catch (Exception e) {
				// TODO: handle exception
				log.error("Get Platforms :" + e);
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getPlatformReportController : in con close exception.");
						// TODO: handle exception
						log.error("Get Platforms :" + e);
						e.printStackTrace();
					}
				}
			}

			return platformDataSet;

		}
		
		
		/**
		 * A function to get reports data - Overview by Date
		 * @author Sayali dec 2016
		 * update on feb 2017
		 */

		public Dataset reportsGetOverviewByDateController(int duration,	String GatewayName, String projectName, Boolean allGateways, boolean allProjects) {

			Dataset overview = null;
			Datasource ds;
			String sqlQuery = "";

			SRConnection con = null;
			String timefilter = getDateFilterOnController(duration, "start_timestamp");
			String modIAFilter = getDateFilterOnController(duration, "session_start");
			String filter = "";
			String filter1 = "";
			if(allProjects && allGateways)
			{
				filter = "";
				filter1 = "";
			}
			else if(!allProjects && allGateways)
			{
				filter = " and b.PROJECT_NAME = '" + projectName + "' ";
				filter1 = " and PROJECT_NAME = '" + projectName + "' ";
			}
			else if(allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' ";
				filter1 = " and GATEWAY_ID = '" + GatewayName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' and b.PROJECT_NAME = '" + projectName + "' ";
				filter1 = " and GATEWAY_ID = '" + GatewayName + "' and PROJECT_NAME = '" + projectName + "' ";
			}
			
				if (duration == Constants.TODAY || duration == Constants.YESTERDAY) {
					
					sqlQuery = "Select a.hour as Hour, b.people, b.visits, b.ACTIONS from mod_ia_hours as a "
							+ " left join (select tbl1.eventHour as overviewHour,  tbl1.people as people, tbl2.visits as visits,"
							+ " tbl2.actions as actions from (SELECT count( distinct(concat(a.username,b.AUTH_PROFILE,a.gateway_id))) as People,"
							+ " hour(a.start_timestamp) as eventHour FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b "
							+ " where a.project = b.PROJECT_NAME and a.gateway_id = b.gateway_id and username != 'SYSTEM' and is_mobile = 0 and "
							+ timefilter + filter
							+ " group by hour(a.start_timestamp) ) as tbl1,"
							+ " (select sum(no_of_screens+NO_OF_ACTIONS) as actions, count(SESSION_START) as visits,"
							+ " hour(SESSION_START) as overviewHour from mod_ia_aggregates_daily_sessions where "
							+ modIAFilter + filter1
							+ " group by hour(SESSION_START)) as tbl2 where tbl2.overviewHour = tbl1.eventHour) as b"
							+ " on b.overviewHour = a.hour order by a.hour;";
					
				} else if (duration == Constants.THIS_YEAR
						|| duration == Constants.LAST_YEAR) {
					
					sqlQuery = "  select a.monthName as monthName, b.people, b.visits, b.ACTIONS, a.monthNumber"
							+ " from mod_ia_month as a left join"
							+ " (select tbl2.overviewmonth as overviewmonth, tbl2.visits as  visits,"
							+ " tbl1.people as people, tbl2.actions as actions from "
							+ " (SELECT count( distinct(concat(a.username,b.AUTH_PROFILE,a.gateway_id))) as People,"
							+ " month(a.start_timestamp) as eventMonth"
							+ " FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
							+ " where a.project = b.PROJECT_NAME and a.gateway_id = b.gateway_id"
							+ " and username != 'SYSTEM' and is_mobile = 0 and "
							+ timefilter + filter
							+ " group by month(a.start_timestamp) ) as tbl1,"
							+ " (select sum(no_of_screens+NO_OF_ACTIONS) as actions, count(SESSION_START) as visits,"
							+ " month(SESSION_DATE) as overviewmonth from mod_ia_aggregates_daily_sessions where "
							+ modIAFilter + filter1
							+ " group by month(session_date)) as tbl2 where tbl2.overviewmonth = tbl1.eventMonth) as b"
							+ " on b.overviewmonth = a.monthNumber order by a.monthNumber; ";
		
					
			
				} else if (duration == Constants.LAST_365_DAYS) {

					sqlQuery = " select a.monthName as monthName, b.people, b.visits, b.ACTIONS, a.monthNumber"
							+ " from mod_ia_month as a"
							+ " left join (select tbl1.eventDate as overviewmonth ,tbl1.people as people, tbl2.visits as visits,"
							+ " tbl2.actions as actions"
							+ " from (SELECT count( distinct(concat(a.username,b.AUTH_PROFILE,a.gateway_id))) as People, month(a.start_timestamp) as eventDate"
							+ " FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
							+ " where a.project = b.PROJECT_NAME and a.gateway_id = b.gateway_id"
							+ " and username != 'SYSTEM' and is_mobile = 0 and "
							+ timefilter + filter
							+ " group by month(a.start_timestamp) ) as tbl1,"
							+ " (select sum(no_of_screens+NO_OF_ACTIONS) as actions, count(SESSION_START) as visits,"
							+ " month(SESSION_DATE) as overviewmonth from mod_ia_aggregates_daily_sessions where "
							+ modIAFilter + filter1
							+ " group by month(session_date)) as tbl2 where tbl2.overviewmonth = tbl1.eventDate) as b"
							+ " on b.overviewmonth = a.monthNumber order by a.monthNumber; ";
					
				} else {
					
					sqlQuery = "select tbl1.eventDate, tbl1.people, tbl2.visits, tbl2.actions"
							+ " from  ( "
							+ "SELECT count( distinct(concat(a.username,b.AUTH_PROFILE,a.gateway_id))) as People, DATE(a.start_timestamp) as eventDate"
							+ " FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
							+ " where a.gateway_id = b.gateway_id and a.PROJECT = b.PROJECT_NAME and "
							+ timefilter  + filter
							+ " and username != 'SYSTEM' and is_mobile = 0  group by DATE(a.start_timestamp)"
							+ " ) as tbl1,"
							+ " ( select sum(no_of_screens+NO_OF_ACTIONS) as actions, count(SESSION_START) as visits, SESSION_DATE "
							+ " from mod_ia_aggregates_daily_sessions where " + modIAFilter + filter1
							+ " group by SESSION_DATE) as tbl2"
							+ " where tbl2.SESSION_DATE = tbl1.eventDate order by tbl1.eventDate desc;";
				}
			
//			log.error("ByDate Report query : " + sqlQuery);
			// connect to the database and get records
			try {

				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				// log.error("By Date Report query is " + sqlQuery);
				overview = con.runQuery(sqlQuery);
			} catch (Exception e) {
				log.error("reportsGetOverviewByDateController : " + e);
			} finally {
				// close the database connection
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						log.error("reportsGetOverviewByDateController : in con close exception.");

						e.printStackTrace();
					}

				}
			}
			return overview;
		}
		
		
		
		/**
		 * sayali 9 jan 2017 A function to get reports data - Bounce rate by Date on report panel controller
		 */
		public Dataset getBounceRateReportByDateController(int duration,
				String GatewayName, String projectName, Boolean allGateways,
				boolean allProjects) {

			Dataset overview = null;
			Datasource ds;
			String sqlQuery = "";

			SRConnection con = null;
			String timefilter = getDateFilterOnController(duration, "OVERVIEW_DATE");
			String auditTimeFilter = getDateFilterOnController(duration, "a.START_TIMESTAMP");
			String sessionTimefilter = getDateFilterOnController(duration, "SESSION_START");
			String filter = "";
			String filter1 = "";
			if(allProjects && allGateways)
			{
				filter = "";
				filter1 = "";
			}
			else if(!allProjects && allGateways)
			{
				filter = " and b.PROJECT_NAME = '" + projectName + "' ";
				filter1 = " and PROJECT_NAME = '" + projectName + "' ";
			}
			else if(allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' ";
				filter1 = " and GATEWAY_ID = '" + GatewayName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' and b.PROJECT_NAME = '" + projectName + "' ";
				filter1 = " and GATEWAY_ID = '" + GatewayName + "' and PROJECT_NAME = '" + projectName + "' ";
			}
			
			if (duration == Constants.TODAY || duration == Constants.YESTERDAY) {

				sqlQuery = "select a.Hour, b.bounceRate, b.people, b.TOTAL_SESSIONS"
						+ "	from mod_ia_hours as a  left join ("
						+ "	select tbl2.OVERVIEW_HOUR as overviewHour, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ "	from ("
						+ "	SELECT count( distinct(concat(a.USERNAME,b.AUTH_PROFILE,a.gateway_id))) as People, hour(a.start_timestamp) as eventHour, a.client_context"
						+ "	FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b "
						+ "	where a.IS_MOBILE = 0 and a.gateway_id = b.gateway_id and a.PROJECT = b.PROJECT_NAME and "
						+ auditTimeFilter + filter
						+ " and USERNAME != 'SYSTEM' group by hour(a.start_timestamp)) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, OVERVIEW_HOUR"
						+ "	from mod_ia_aggregates_hourly_overview where "
						+ timefilter + filter1
						+ " group by OVERVIEW_HOUR ) as tbl2,"
						+ " (select count(session_start) as TOTAL_SESSIONS, hour(session_start) as sessionHour, session_context"
						+ " from mod_ia_aggregates_daily_sessions where "
						+ sessionTimefilter + filter1
						+ " group by hour(session_start)) as tbl3"
						+ " where tbl1.eventHour = tbl2.OVERVIEW_HOUR and tbl2.OVERVIEW_HOUR = tbl3.sessionHour and tbl1.client_context = tbl3.session_context) as b"
						+ " on a.Hour=b.overviewHour order by a.hour desc;";
				
			} else if (duration == Constants.THIS_YEAR
					|| duration == Constants.LAST_YEAR) {

				sqlQuery = "select a.monthName as monthName, b.bounceRate, b.people, b.TOTAL_SESSIONS"
						+ "	from mod_ia_month as a  left join ("
						+ "	select tbl2.overviewmonth as overviewmonth, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ " from ( "
						+ " SELECT count( distinct(concat(a.USERNAME,b.AUTH_PROFILE,a.gateway_id))) as People, month(a.start_timestamp) as eventmonth, client_context"
						+ "	FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
						+ "	where a.IS_MOBILE = 0 and  a.gateway_id = b.gateway_id and a.PROJECT = b.PROJECT_NAME and "
						+ auditTimeFilter + filter
						+ " and USERNAME != 'SYSTEM' group by month(a.start_timestamp) ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, month(overview_date) as overviewmonth "
						+ "	from mod_ia_aggregates_daily_overview where "
						+ timefilter + filter1
						+ " group by month(overview_date) ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, month(session_date) as sessionMonth, session_context"
						+ " from mod_ia_aggregates_daily_sessions where "
						+ sessionTimefilter + filter1
						+ " group by month(session_date)) as tbl3"
						+ " where tbl1.eventmonth = tbl2.overviewmonth and tbl2.overviewmonth = tbl3.sessionMonth and tbl1.client_context = tbl3.session_context ) as b"
						+ " on a.monthNumber=b.overviewmonth"
						+ " order by a.monthNumber;";

			} else if (duration == Constants.LAST_365_DAYS) {

				sqlQuery = "select a.monthName as monthName, b.bounceRate, b.people, b.TOTAL_SESSIONS, a.monthNumber"
						+ "	from mod_ia_month as a  left join ("
						+ "	select tbl2.overviewmonth as overviewmonth, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ " from ( "
						+ " SELECT count( distinct(concat(a.USERNAME,b.AUTH_PROFILE,a.gateway_id))) as People, month(a.start_timestamp) as eventmonth, client_context"
						+ "	FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b"
						+ "	where a.IS_MOBILE = 0 and  a.gateway_id = b.gateway_id and a.PROJECT = b.PROJECT_NAME and "
						+ auditTimeFilter + filter
						+ " and USERNAME != 'SYSTEM' group by month(a.start_timestamp) ) as tbl1,"
						+ "	( select avg(bounce_rate)  * 100 as bounceRate, month(overview_date) as overviewmonth "
						+ "	from mod_ia_aggregates_daily_overview where "
						+ timefilter + filter1
						+ " group by month(overview_date) ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, month(session_date) as sessionMonth, session_context"
						+ " from mod_ia_aggregates_daily_sessions where "
						+ sessionTimefilter + filter1
						+ " group by month(session_date)) as tbl3"
						+ " where tbl1.eventmonth = tbl2.overviewmonth and tbl2.overviewmonth = tbl3.sessionMonth and tbl1.client_context = tbl3.session_context ) as b"
						+ " on a.monthNumber=b.overviewmonth"
						+ " order by a.monthNumber;";

			} else {

				sqlQuery = "select tbl2.OVERVIEW_DATE as overviewDate, tbl2.bounceRate as  bounceRate, tbl1.people as people, tbl3.TOTAL_SESSIONS as TOTAL_SESSIONS"
						+ "	from (SELECT count( distinct(concat(a.USERNAME,b.AUTH_PROFILE,a.gateway_id))) as People, DATE(a.start_timestamp) as overviewDate, client_context"
						+ " FROM mod_ia_aggregates_clients a,  mod_ia_aggregates_projects b "
						+ "	where a.IS_MOBILE = 0 and  a.gateway_id = b.gateway_id and a.PROJECT = b.PROJECT_NAME and "
						+ auditTimeFilter + filter
						+ " and USERNAME != 'SYSTEM' group by DATE(a.start_timestamp) ) as tbl1, "
						+ " ( select avg(bounce_rate)  * 100 as bounceRate, OVERVIEW_DATE "
						+ " from mod_ia_aggregates_daily_overview where "
						+ timefilter + filter1
						+ " group by OVERVIEW_DATE ) as tbl2,"
						+ "(select count(session_start) as TOTAL_SESSIONS, session_date as sessionDate, session_context"
						+ " from mod_ia_aggregates_daily_sessions where "
						+ sessionTimefilter + filter1
						+ " group by SESSION_DATE) as tbl3"
						+ " where tbl1.overviewDate = tbl2.OVERVIEW_DATE and tbl2.OVERVIEW_DATE = tbl3.sessionDate and tbl1.client_context = tbl3.session_context"
						+ " order by tbl1.overviewDate ;";
			}

//			log.error("Bounce_Rate query: "+ sqlQuery);

			// connect to the database and get records
			try {
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();

				overview = con.runQuery(sqlQuery);
			} catch (Exception e) {
				log.error("getBounceRateReportByDateController : " + e);
			} finally {
				// close the database connection
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getBounceRateReportByDateController : in con close exception.");

						e.printStackTrace();
					}

				}
			}
			return overview;
		}
		
		/**
		 * Sayali 10 jan 2017 Method to retrieve Device type information Returns a
		 * Dataset with No Of People and No of Actions per device type
		 * @ author sayali created on 13-01-2017 on report panel controller
		 */
		@Override
		public Dataset getDeviceTypeReportController(int duration,
				String GatewayName, String projectName, Boolean allGateways,
				boolean allProjects) {

			Dataset deviceData = null;
			Datasource ds;
			String sqlQuery = "";

			SRConnection con = null;
			String timefilter = getDateFilterOnController(duration, "b.START_TIMESTAMP");
			String filter = "";
			
			if(allProjects && allGateways)
			{
				filter = "";
			}
			else if(!allProjects && allGateways)
			{
				filter = " and b.PROJECT = '" + projectName + "' ";
			}
			else if(allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' ";
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and b.GATEWAY_ID = '" + GatewayName + "' and b.PROJECT = '" + projectName + "' ";
			}
			
			sqlQuery = "select dt.deviceType as deviceType, count(distinct (concat(dt.username,c.AUTH_PROFILE,dt.gateway_id))) as people,"
					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
					+ " from ("
					+ " select distinct case when b.IS_MOBILE = 0 then 'Desktop'  when b.IS_MOBILE = 1 then 'Mobile' end as 'deviceType', "
					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT, b.GATEWAY_ID as GATEWAY_ID"
					+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b"
					+ " where "
					+ timefilter + filter
					+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
					+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID and a.session_start != a.session_end and a.username = b.username "
					+ " ) as dt,"
					+ " mod_ia_aggregates_projects c"
					+ " where dt.PROJECT = c.PROJECT_NAME and dt.GATEWAY_ID = c.GATEWAY_ID group by deviceType ;";
			
//			log.error("Device type query: "+sqlQuery);

			// connect to the database and get records
			try {
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
//				log.error("Device type report query - "+sqlQuery);
				deviceData = con.runQuery(sqlQuery);
			} catch (Exception e) {
				log.error("getDeviceTypeReport : " + e);
			} finally {
				// close the database connection
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getDeviceTypeReport : in con close exception.");

						e.printStackTrace();
					}

				}
			}
			return deviceData;
		}
		
		/**
		 * function to return report of active users on controller
		 * @author sayali  created on 18-01-2017
		 */
	
		public Dataset getActiveUserDataReportGraphController(String datasource, int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects){
			Dataset activeUsersData = null ;
			
			Datasource dsname;
			String sqlQuery = "";
			
			SRConnection con = null;
			String timefilter = getDateFilterOnController(duration, "SUMMARY_DATE");

			String filter = "";
			
			if(allProjects && allGateways)
			{
				filter = " and PROJECT_NAME = 'All' ";
//				log.error(" and PROJECT_NAME = 'All' ");
			}
			else if(!allProjects && allGateways)
			{
				filter = " and PROJECT_NAME = '" + projectName + "' ";
//				log.error(" and PROJECT_NAME = '" + projectName + "' ");
			}
			else if(allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + GatewayName + "' and PROJECT_NAME = 'All' ";
//				log.error(" and GATEWAY_ID = '" + GatewayName + "' and PROJECT_NAME = 'All' ");
			}
			else if(!allProjects && !allGateways)
			{
				filter = " and GATEWAY_ID = '" + GatewayName + "' and PROJECT_NAME = '" + projectName + "' ";
//				log.error(" and GATEWAY_ID = '" + GatewayName + "' and PROJECT_NAME = '" + projectName + "' ");
			}
			
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, COALESCE(ONE_DAY_ACTIVE_USERS, 0), "
								+ " COALESCE(SEVEN_DAY_ACTIVE_USERS, 0),"
								+ " COALESCE(FOURTEEN_DAY_ACTIVE_USERS, 0) " 
								+ " from mod_ia_hours as a "
								+ " left join (SELECT SUMMARY_HOUR, sum(ONE_DAY_ACTIVE_USERS) as ONE_DAY_ACTIVE_USERS,"
								+ " sum(SEVEN_DAY_ACTIVE_USERS) as SEVEN_DAY_ACTIVE_USERS, "
								+ " sum(FOURTEEN_DAY_ACTIVE_USERS) as FOURTEEN_DAY_ACTIVE_USERS "
								+ " FROM mod_ia_aggregates_hourly_active_users where "
								+ timefilter + filter
								+ " group by SUMMARY_HOUR ) as b "
								+ " on b.summary_hour = a.Hour"
								+ " order by a.Hour;";
//					log.error("getActiveUserDataReportGraphController : " + sqlQuery);
				}
				else if(duration == Constants.LAST_365_DAYS ) 
				{
					//query from daily active users and group by month
					String sDate = "";
					Calendar lastHour = Calendar.getInstance();
				    lastHour.set(Calendar.HOUR_OF_DAY, 23);
				    lastHour.set(Calendar.MINUTE, 59);
				    lastHour.set(Calendar.SECOND, 59);
				    
				    int currentYear = lastHour.get(Calendar.YEAR);
					lastHour.add(Calendar.DATE, -365);
					int yearVal = lastHour.get(Calendar.YEAR);
					int monthVal = lastHour.get(Calendar.MONTH);
					sqlQuery = "select a.monthName, sum(one_day_active_users) as one_day_active_users, sum(seven_day_active_users) as seven_day_active_users,"
							+ " sum(fourteen_day_active_users) as fourteen_day_active_users, a.monthNumber "
							+ " from mod_ia_month as a "
							+ " left join (select one_day_active_users, "
							+ " seven_day_active_users, "
							+ " fourteen_day_active_users, "
							+ " month_no as summary_month , year from mod_ia_aggregates_monthly_active_users where "
							+ " ( year = " + yearVal + " and month_no >= " + monthVal + ") or ( year = " + currentYear + ")"
							+ filter + " ) as b "
							+ " on b.summary_month = a.monthNumber"
							+ " group by a.monthName,a.monthNumber;"
							+ " order by a.monthNumber";
//					log.error("getActiveUserDataReportGraphController 365 days : " + sqlQuery);
//											
				}
				else if(duration == Constants.LAST_YEAR )
				{
					//query from monthly active users for last year
					sqlQuery = "select a.monthName,sum(ONE_DAY_ACTIVE_USERS) as ONE_DAY_ACTIVE_USERS, sum(SEVEN_DAY_ACTIVE_USERS) as SEVEN_DAY_ACTIVE_USERS,"
							+ " sum(FOURTEEN_DAY_ACTIVE_USERS) as FOURTEEN_DAY_ACTIVE_USERS, a.monthNumber"
							+ " from mod_ia_month as a"
							+ " left join (select  one_day_active_users,  seven_day_active_users,  fourteen_day_active_users,  month_no"
							+ "	FROM mod_ia_aggregates_monthly_active_users where year = year(now()) - 1" + filter
							+ ") as b on b.month_no = a.monthNumber group by a.monthName,a.monthNumber"
							+ " order by a.monthNumber;";
//					log.error("getActiveUserDataReportGraphController : " + sqlQuery);
				}	
				else if(duration == Constants.THIS_YEAR)
				{
					//query from monthly active users for current year	

					sqlQuery = "select a.monthName,sum(ONE_DAY_ACTIVE_USERS) as ONE_DAY_ACTIVE_USERS, sum(SEVEN_DAY_ACTIVE_USERS) as SEVEN_DAY_ACTIVE_USERS,"
							+ " sum(FOURTEEN_DAY_ACTIVE_USERS) as FOURTEEN_DAY_ACTIVE_USERS, a.monthNumber"
							+ " from mod_ia_month as a"
							+ " left join (select  one_day_active_users,  seven_day_active_users,  fourteen_day_active_users,  month_no"
							+ "	FROM mod_ia_aggregates_monthly_active_users where year = year(now())" + filter
							+ ") as b on b.month_no = a.monthNumber group by a.monthName,a.monthNumber"
							+ " order by a.monthNumber;";
//					log.error("getActiveUserDataReportGraphController : " + sqlQuery);
				}
				else
				{
					sqlQuery = "SELECT SUMMARY_DATE, SUM(ONE_DAY_ACTIVE_USERS), SUM(SEVEN_DAY_ACTIVE_USERS), SUM(FOURTEEN_DAY_ACTIVE_USERS)"
								+ " FROM mod_ia_aggregates_daily_active_users where "
								+ timefilter + filter + " group by SUMMARY_DATE order by SUMMARY_DATE;";	
					
//					log.error("getActiveUserDataReportGraphController : " + sqlQuery);
				}
//			log.error("Active users query: "+sqlQuery);
			try {
				
				dsname = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = dsname.getConnection();
				activeUsersData = con.runQuery(sqlQuery);
				
			} catch (Exception e) {
				// TODO: handle exception
				log.error("getActiveUserDataReportGraphController :"+e);
			}
			finally{
				if(con != null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error(" getActiveUserDataReportGraphController: in con close exception.");
						// TODO: handle exception
						log.error("getActiveUserDataReportGraphController :"+e);
						e.printStackTrace();
					}
				}
			}
			
				return activeUsersData;
			
			}
	
	/**
	 * Following functions are used to retrieve reports specific dataset for
	 * given project names and given duration on report panel controller
	 * sayali 
	 */
	public Dataset getEngagementReportInformationScreenDepthController(String GatewayName, String projectName, boolean allGateways,
			boolean allProjects, int duration) {

		Dataset freqData = null;
		Datasource ds;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;

		String dateFilter = getDateFilterOnController(duration, "session_start");
		String sqlQuery = "";
		String filter = "";
		
		if(allProjects && allGateways)
		{
			filter = "";
		}
		else if(!allProjects && allGateways)
		{
			filter = " and a.PROJECT_NAME = '" + projectName + "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' and a.PROJECT_NAME = '" + projectName + "' ";
		}
		
		sqlQuery = "SELECT a.no_of_screens as no_of_screens, count(a.session_date) as Sessions,"
				+ " count(distinct (concat(a.username, b.AUTH_PROFILE,a.gateway_id))) as People, "
				+ " sum(a.no_of_actions) + sum(a.no_of_screens) as Actions"
				+ " from mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_projects b where "
				+ dateFilter
				+ " and a.PROJECT_NAME = b.PROJECT_NAME and a.GATEWAY_ID = b.GATEWAY_ID"
				+ filter + " group by no_of_screens;";
		
//		log.error("Engagement Report Information: "+sqlQuery);

		
		try {
			con = ds.getConnection();

			freqData = con.runQuery(sqlQuery);

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error(" getEngagementReportInformationScreenDepthController : in con close exception.");

					e.printStackTrace();
				}

			}
		}

		return freqData;

	}
	
	/**
	 * A function to retrieve frequency information from user sessions summary
	 * table.
	 * 
	 * @author : Sayali created on 12/01/2017
	 */

	@Override
	public Dataset getFrequencyReportInformationController(String GatewayName,
			       
			String projectName, boolean allGateways, boolean allProjects,
			int duration) {
		Dataset freqData = null;
		Datasource ds;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;

		String dateFilter = getDateFilterOnController(duration, "session_start");
		String sqlQuery = "";
		String filter = "";
		
		if(allProjects && allGateways)
		{
			filter = "";
		}
		else if(!allProjects && allGateways)
		{
			filter = " and a.PROJECT_NAME = '" + projectName + "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' and a.PROJECT_NAME = '" + projectName + "' ";
		}
			sqlQuery = "SELECT (concat(a.username,b.AUTH_PROFILE,a.gateway_id)) as People, count(session_start) Visits,"
					+ " sum(no_of_actions) as Actions, sum(no_of_screens) as noOfScreens from  mod_ia_aggregates_daily_sessions a,"
					+ "   mod_ia_aggregates_projects b where a.project_name = b.project_name and a.gateway_id = b.gateway_id and "
					+ dateFilter + filter
					+ " group by (concat(a.username,b.AUTH_PROFILE,a.gateway_id))"
					+ " order by visits;";

//			log.error("Frequency Report Information: "+sqlQuery);
		try {

			con = ds.getConnection();
			freqData = con.runQuery(sqlQuery);

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getFrequencyInformationController : in con close exception.");

					e.printStackTrace();
				}

			}
		}

		return freqData;
	}
	
	/**
	 * Function to get Recency Report on report panel controller
	 * @author Sayali 09-02-2017
	 */

	@Override
	public Dataset getRecencytReportInformationController(String GatewayName,
			String projectName, boolean allGateways, boolean allProjects,
			int duration) {
		Dataset freqData = null;
		Datasource ds;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;

		String dateFilter = getDateFilterOnController(duration, "session_start");
		String sqlQuery = "";
		String startDate = getDayAndTime(duration);
		String durationEndDate = "";
		String filter = "";
		
		if(allProjects && allGateways)
		{
			filter = "";
		}
		else if(!allProjects && allGateways)
		{
			filter = " and a.PROJECT_NAME = '" + projectName + "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' and a.PROJECT_NAME = '" + projectName + "' ";
		}

		if (duration == Constants.YESTERDAY
				|| duration == Constants.LAST_WEEK
				|| duration == Constants.LAST_MONTH
				|| duration == Constants.LAST_YEAR) {
			durationEndDate = getDurationEndDate(duration);

			sqlQuery = "SELECT (concat(a.username,b.AUTH_PROFILE,a.gateway_id)) as People,"
					+ "   DATEDIFF(date('"
						+ startDate
						+ "') ,max(session_date)) as Days ,count(session_start) Visits, "
					+ "sum(no_of_actions) as Actions, sum(no_of_screens) as noOfScreens from mod_ia_aggregates_daily_sessions a,"
					+ " mod_ia_aggregates_projects b where a.project_name = b.project_name and a.gateway_id = b.gateway_id and session_start < '"
					+ durationEndDate + "'" + filter
					+ " group by (concat(a.username,b.AUTH_PROFILE,a.gateway_id)) order by Visits;";
		} else {
			sqlQuery =  "SELECT (concat(a.username,b.AUTH_PROFILE,a.gateway_id)) as People,"
					+ "   DATEDIFF(date('"
					+ startDate
					+ "') ,max(session_date)) as Days,count(session_start) Visits, "
					+ "sum(no_of_actions) as Actions, sum(no_of_screens) as noOfScreens from mod_ia_aggregates_daily_sessions a,"
					+ " mod_ia_aggregates_projects b where a.project_name = b.project_name and a.gateway_id = b.gateway_id "
					+ filter
					+ " group by (concat(a.username,b.AUTH_PROFILE,a.gateway_id)) order by Visits;";
		}

//		log.error("Recency report: "+ sqlQuery );



		try {

			con = ds.getConnection();
			freqData = con.runQuery(sqlQuery);

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getRecencytReportInformationController : in con close exception.");

					e.printStackTrace();
				}

			}
		}

		return freqData;
	}
	
	/** 
	 * function to get Visit Duration Information on report panel controller
	 * @author sayali 11-01-2017
	 */
	
	public Dataset getVisitDurationReportInformationController(String GatewayName, String projectName, boolean allGateways,
			boolean allProjects, int duration) {

		Dataset freqData = null;
		Datasource ds;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;


		String dateFilter = getDateFilterOnController(duration, "session_start");
		String sqlQuery = "";
		String filter = "";
		
		if(allProjects && allGateways)
		{
			filter = "";
		}
		else if(!allProjects && allGateways)
		{
			filter = " and a.PROJECT_NAME = '" + projectName + "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' and a.PROJECT_NAME = '" + projectName + "' ";
		}
		
		sqlQuery =  "select count(distinct username) as People , count(session_start) as Visits,"
		+ "	sum(no_of_actions) as Actions, session_duration_mins, sum(no_of_screens) as noOfScreens from "
		+ " ( select CONCAT(a.username,b.AUTH_PROFILE,a.gateway_id) as username , session_start,"
		+"	no_of_actions, "
		+ " case when (TIME_TO_SEC(session_duration))/60 <= 5 then '0-5'"
		+ "	 when (TIME_TO_SEC(session_duration))/60 > 5 and (TIME_TO_SEC(session_duration))/60 <= 10 then '6-10'"
		+" when (TIME_TO_SEC(session_duration))/60 > 10 and (TIME_TO_SEC(session_duration))/60 <= 20 then '11-20'"
		+ " when (TIME_TO_SEC(session_duration))/60 > 20 and (TIME_TO_SEC(session_duration))/60 <= 30 then '21-30' "
		+ " when (TIME_TO_SEC(session_duration))/60 > 30 and (TIME_TO_SEC(session_duration))/60 <= 40 then '31-40'"
		+ " when (TIME_TO_SEC(session_duration))/60 > 40 and (TIME_TO_SEC(session_duration))/60 <= 50 then '41-50'"
		+ " when (TIME_TO_SEC(session_duration))/60 > 50 and (TIME_TO_SEC(session_duration))/60 <= 60 then '51-60'"
		+ " when (TIME_TO_SEC(session_duration))/60 > 60 and (TIME_TO_SEC(session_duration))/60 <= 120 then '61-120'"
		+ " when (TIME_TO_SEC(session_duration))/60 > 120  then '120'"
		+ " end as session_duration_mins, no_of_screens"
		+ " from mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_projects b where "
		+ "	a.gateway_id = b.gateway_id and a.PROJECT_NAME = b.PROJECT_NAME and "
		+ dateFilter + filter
		+ " ) as c group by session_duration_mins; ";

//		log.error("Visit Duration ; "+sqlQuery);
			
		
		
		try {

			con = ds.getConnection();
			freqData = con.runQuery(sqlQuery);

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getVisitDurationReportInformationController : in con close exception.");

					e.printStackTrace();
				}

			}
		}

		return freqData;

	}
	
	/**
	 * Function to retrieve ACTIONS PER VISIT REPORT INFORMATION
	 * 
	 * @author sayali : Created on 11/01/2017
	 * 
	 */
	public Dataset getActionsPerVisitReportInformationController(String GatewayName, String projectName, boolean allGateways,
			boolean allProjects, int duration) {

		Dataset freqData = null;
		Datasource ds;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;

		String dateFilter = getDateFilterOnController(duration, "session_start");
		String sqlQuery = "";
		String filter = "";
		
		if(allProjects && allGateways)
		{
			filter = "";
		}
		else if(!allProjects && allGateways)
		{
			filter = " and a.PROJECT_NAME = '" + projectName + "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and a.GATEWAY_ID = '" + GatewayName + "' and a.PROJECT_NAME = '" + projectName + "' ";
		}
		
		sqlQuery = "select count( distinct(a.username)) as People , count(a.session_start) as Visits, sum(a.Actions) as Actions,a.count_of_actions "
				 + " from "
				 + " (select (concat(a.username,b.auth_profile,a.gateway_id)) as username, session_start, "
				 + " (no_of_actions + NO_OF_SCREENS) as Actions, "
				 + " case when (NO_OF_ACTIONS + NO_OF_SCREENS) >= 1 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 5 then '1-5' "
				 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 5 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 10 then '6-10' "
				 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 10 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 20 then '11-20' "
				 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 20 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 30 then '21-30'"
				 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 30 and (NO_OF_ACTIONS + NO_OF_SCREENS) <= 50 then '31-50'"
				 + " when (NO_OF_ACTIONS + NO_OF_SCREENS) > 50  then '51 or more'"
				 + " end as  count_of_actions" 
				 + " from mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_projects b where"
				 + " a.gateway_id = b.gateway_id and a.PROJECT_NAME = b.PROJECT_NAME and "
				 + dateFilter + filter + " ) as a "
				 + " group by count_of_actions; ";

//		log.error("Actions per visit : "+sqlQuery);
		
		
		try {

			con = ds.getConnection();
			freqData = con.runQuery(sqlQuery);

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getActionsPerVisitReportInformationController : in con close exception.");

					e.printStackTrace();
				}

			}
		}

		return freqData;

	}
	
	/**
	 * Function to retrieve Browser reports data
	 * 
	 * @author sayali : Created on 10/01/2017
	 * 
	 */
	
	@Override
	public Dataset getBrowserReportController(int duration, String GatewayName,
			String projectName, Boolean allGateways, boolean allProjects) {
		Dataset browserData = null;
		Datasource ds;
		String sqlQuery = "";

		SRConnection con = null;

		String timefilter = getDateFilterOnController(duration, "c.START_TIMESTAMP");
		String filter = "";
		String filter1 = "";
		
		if(allProjects && allGateways)
		{
			filter = "";
			filter1 = "";
			
		}
		else if(!allProjects && allGateways)
		{
			filter = " and dt.PROJECT_NAME = '" + projectName + "' ";
			filter1 = " and c.Project = '"+ projectName	+ "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and dt.GATEWAY_ID = '" + GatewayName + "' ";
			filter1 = " and c.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and dt.GATEWAY_ID = '" + GatewayName + "' and dt.PROJECT_NAME = '" + projectName + "' ";
			filter1 = " and c.GATEWAY_ID = '" + GatewayName + "' and c.Project = '"+ projectName	+ "' ";
		}
		
		sqlQuery = "select  dt.browser_name as browser_name, sum(dt.no_of_actions) as Actions, count(dt.session_start) as Visits, "
				+ " count(distinct (concat(dt.username,c.AUTH_PROFILE, c.gateway_id))) as People, sum(dt.no_of_screens) as noOfScreens from "
				+ "( select distinct b.browser_name as browser_name, a.no_of_actions as no_of_actions, a.session_start as session_start,"
				+ " b.username as username, a.no_of_screens as no_of_screens, a.GATEWAY_ID as GATEWAY_ID, a.PROJECT_NAME as PROJECT_NAME "
				+ " from mod_ia_aggregates_daily_sessions a "
				+ " right join "
				+ " ( select start_timestamp, IP_ADDRESS, username , concat(LTRIM(RTRIM(browser_name)) , ' ', browser_version) as browser_name"
				+ " , c.GATEWAY_ID as GATEWAY_ID, c.PROJECT as PROJECT"
				+ " from mod_ia_aggregates_clients c, mod_ia_aggregates_browser_info  d "
				+ " where (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ) and  d.IP_ADDRESS is not null and c.GATEWAY_ID = d.GATEWAY_ID and "
				+ timefilter + filter1
				+ " and d.timestamp <= c.start_timestamp and d.timestamp = "
				+ " (select max(timestamp) from mod_ia_aggregates_browser_info where timestamp <= c.start_timestamp and gateway_id = c.gateway_id "
				+ filter1
				+ " and ( IP_ADDRESS = c.HOST_INTERNAL_IP or IP_ADDRESS =c.HOST_EXTERNAL_IP ))) as b"
				+ " on ( a.session_start != a.session_end and b.start_timestamp >= a.session_start "
				+ "and b.start_timestamp <= a.session_end and a.username = b.username"
				+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID)"
				+ " )as dt , "
				+ " mod_ia_aggregates_projects c where dt.PROJECT_NAME = c.PROJECT_NAME " + filter
				+ " and dt.GATEWAY_ID = c.GATEWAY_ID group by dt.browser_name;";
		

//		log.error("Browser query: "+sqlQuery);
		// connect to the database and get records
		try {
//			log.error("Browser query : "+sqlQuery);
			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();

			browserData = con.runQuery(sqlQuery);
		} catch (Exception e) {
			log.error("getBrowserReportController : " + e);
		} finally {
			// close the database connection
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getBrowserReportController : in con close exception.");

					e.printStackTrace();
				}

			}
		}
		return browserData;
	}
	
	/**
	 * Function to retrieve Screen resolution information to be shown on reports
	 * 
	 * @author sayali created on 09-23-2015
	 */

	public Dataset getScreenResolutionDataController(String GatewayName, String projectName, boolean allGateways,
			boolean allProjects, int duration) {

		Dataset screensData = null;
		Datasource ds;

		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;

//		String dateFilter = getDateFilter(duration, "a.session_start");
//		String sqlQuery = "";
//
//		if (allGateways && allProjects) {
//			
//			sqlQuery = "select dt.SCREEN_RESOLUTION as SCREEN_RESOLUTION, count(distinct (CONCAT(dt.username, c.AUTH_PROFILE, c.gateway_id))) as People,"
//					+ " count(dt.session_start) as Visits, sum(dt.no_of_actions)  as Actions, sum(dt.no_of_screens) as noOfScreens"
//					+ " from ("
//					+ " select b.SCREEN_RESOLUTION as SCREEN_RESOLUTION, "
//					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT, b.GATEWAY_ID as GATEWAY_ID"
//					+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b"
//					+ " where "
//					+ dateFilter
//					+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
//					+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID and a.session_start != a.session_end and a.username = b.username "
//					+ " ) as dt, "
//					+ " mod_ia_aggregates_projects c"
//					+ " where dt.PROJECT = c.PROJECT_NAME and dt.GATEWAY_ID = c.GATEWAY_ID group by SCREEN_RESOLUTION ;";
//			
////			log.error("Screen Ressolution AllP and AllG : "+sqlQuery);
//		} else if (allGateways && !allProjects) {
//
//			sqlQuery = "select dt.SCREEN_RESOLUTION as SCREEN_RESOLUTION, count(distinct (CONCAT(dt.username, c.AUTH_PROFILE, c.gateway_id))) as people,"
//					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
//					+ " from ("
//					+ " select b.SCREEN_RESOLUTION as SCREEN_RESOLUTION, "
//					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT, b.GATEWAY_ID as GATEWAY_ID"
//					+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b"
//					+ " where "
//					+ dateFilter
//					+ " and b.project = '"
//					+ projectName
//					+ "' and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
//					+ " and a.PROJECT_NAME = b.PROJECT and a.session_start != a.session_end and a.username = b.username"
//					+ " ) as dt,"
//					+ " mod_ia_aggregates_projects c"
//					+ " where dt.PROJECT = c.PROJECT_NAME and dt.GATEWAY_ID = c.GATEWAY_ID"
//					+ " and dt.project = '"
//					+ projectName
//					+ "' group by SCREEN_RESOLUTION ;";
////			log.error("Screen Ressolution !AllP and AllG : "+sqlQuery);
//		} else if (!allGateways && allProjects) {
//
//			sqlQuery = "select dt.SCREEN_RESOLUTION as SCREEN_RESOLUTION, count(distinct (CONCAT(dt.username, c.AUTH_PROFILE, c.gateway_id))) as people,"
//					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"//, sum(dt.no_of_screens) as noOfScreens
//					+ " from ("
//					+ " select b.SCREEN_RESOLUTION as SCREEN_RESOLUTION, "
//					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT, b.GATEWAY_ID as GATEWAY_ID"
//					+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b"
//					+ " where "
//					+ dateFilter
//					+ "and b.GATEWAY_ID = '"
//					+ GatewayName
//					+ "' and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
//					+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID and a.session_start != a.session_end and a.username = b.username"
//					+ " ) as dt,"
//					+ " mod_ia_aggregates_projects c"
//					+ " where  dt.PROJECT = c.PROJECT_NAME and dt.GATEWAY_ID = c.GATEWAY_ID and"
//					+ " dt.GATEWAY_ID = '"
//					+ GatewayName
//					+ "' group by SCREEN_RESOLUTION ;";
////			log.error("Screen Ressolution AllP and !AllG : "+sqlQuery);
//
//		} else {
//
//			sqlQuery = "select dt.SCREEN_RESOLUTION as SCREEN_RESOLUTION, count(distinct (CONCAT(dt.username, c.AUTH_PROFILE, c.gateway_id))) as people,"
//					+ " count(dt.session_start) as visits, sum(dt.no_of_actions)  as actions, sum(dt.no_of_screens) as noOfScreens"
//					+ " from ("
//					+ " select b.SCREEN_RESOLUTION as SCREEN_RESOLUTION, "
//					+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
//					+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT, b.GATEWAY_ID as GATEWAY_ID"
//					+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b"
//					+ " where "
//					+ dateFilter
//					+ " and b.GATEWAY_ID = '"
//					+ GatewayName
//					+ "' and b.project = '"
//					+ projectName
//					+ "' and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
//					+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID and a.session_start != a.session_end and a.username = b.username"
//					+ " ) as dt,"
//					+ " mod_ia_aggregates_projects c"
//					+ " where dt.PROJECT = c.PROJECT_NAME and dt.GATEWAY_ID = c.GATEWAY_ID"
//					+ "and dt.GATEWAY_ID = '"
//					+ GatewayName
//					+ "' and dt.project = '"
//					+ projectName
//					+ "' group by SCREEN_RESOLUTION ;";
////			log.error("Screen Resolution !AllP and !AllG : "+sqlQuery);
//		}
		String dateFilter = getDateFilterOnController(duration, "a.session_start");
		String sqlQuery = "";
		String filter = "";
		
		if(allProjects && allGateways)
		{
			filter = "";
		}
		else if(!allProjects && allGateways)
		{
			filter = " and dt.PROJECT = '" + projectName + "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and dt.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and dt.GATEWAY_ID = '" + GatewayName + "' and dt.PROJECT = '" + projectName + "' ";
		}
	
		sqlQuery = "select dt.SCREEN_RESOLUTION as SCREEN_RESOLUTION, count(distinct (concat(dt.username,c.AUTH_PROFILE,c.gateway_id))) as People,"
				+ " count(dt.session_start) as Visits, sum(dt.no_of_actions)  as Actions, sum(dt.no_of_screens) as noOfScreens"
				+ " from ("
				+ " select distinct b.SCREEN_RESOLUTION as SCREEN_RESOLUTION, "
				+ " b.username as username, a.session_start as session_start, a.no_of_actions  as no_of_actions,"
				+ " a.no_of_screens as no_of_screens, b.PROJECT as PROJECT, b.GATEWAY_ID as GATEWAY_ID"
				+ " FROM mod_ia_aggregates_daily_sessions a, mod_ia_aggregates_clients b"
				+ " where "
				+ dateFilter
				+ " and b.START_TIMESTAMP >= a.session_start and b.start_timestamp <= a.session_end "
				+ " and a.PROJECT_NAME = b.PROJECT and a.GATEWAY_ID = b.GATEWAY_ID and a.session_start != a.session_end and a.username = b.username "
				+ " ) as dt, "
				+ " mod_ia_aggregates_projects c"
				+ " where dt.PROJECT = c.PROJECT_NAME and dt.GATEWAY_ID = c.GATEWAY_ID" + filter
				+ " group by SCREEN_RESOLUTION ;";
			
//			log.error("Screen Ressolution: "+sqlQuery);
		try {

			con = ds.getConnection();
			screensData = con.runQuery(sqlQuery);

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getScreenResolutionData : in con close exception.");

					e.printStackTrace();
				}

			}
		}

		return screensData;

	}
	
	/**
	 * Function to retrieve Cities information to be shown on reports
	 * 
	 * @author sayali created on 05-01-2017
	 */
	public Dataset getCitiesReportDataController(String GatewayName, String projectName,boolean allGateways, boolean allProjects, int duration){
		
		Dataset citiesData = null;
		Datasource ds;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String dateFilter = getDateFilterOnController(duration, "session_start");
		String dateFilterClients = getDateFilterOnController(duration, "start_timestamp");
		String sqlQuery = "";
		
		if(allProjects && allGateways)
		{
			sqlQuery = "select dt.location as Cities, count(distinct (concat(dt.username, b.AUTH_PROFILE, dt.gateway_id))) as People, count(dt.session_start) as Visits, "
					+ " sum(dt.no_of_actions) as Actions, sum(dt.no_of_screens) as noOfScreens from"
					+ " ( select a.location as location , a.username as username, b.session_start as session_start,"
					+ " b.no_of_actions as no_of_actions,  b.no_of_screens as no_of_screens , b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID"
					+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location,"
					+ " mod_ia_aggregates_clients.gateway_id as	gateway_id	from mod_ia_aggregates_clients join mod_ia_aggregates_location_info"
					+ " on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and "
					+ " mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = mod_ia_aggregates_location_info.GATEWAY_ID where " + dateFilterClients
					+ " ) as a, mod_ia_aggregates_daily_sessions b	where session_start != session_end and " + dateFilter
					+ " and a.username = b.username and a.start_timestamp >= b.session_start"
					+ " and a.start_timestamp <= b.session_end	and a.gateway_id = b.gateway_id"
					+ " union "
					+ " select	a.location as location, a.username as username, b.session_start as session_start,"
					+ " b.no_of_actions as no_of_actions, b.no_of_screens as no_of_screens, b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID"
					+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location,"
					+ " mod_ia_aggregates_clients.gateway_id as gateway_id from mod_ia_aggregates_clients  join mod_ia_aggregates_location_info"
					+ " on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP"
					+ " where " + dateFilterClients + " ) as a,"
					+ "	mod_ia_aggregates_daily_sessions b	where session_start = session_end and "
					+ dateFilter + " and a.username = b.username and a.start_timestamp >= b.session_start and a.gateway_id = b.gateway_id"
					+ " ) as dt, mod_ia_aggregates_projects b where dt.PROJECT_NAME = b.PROJECT_NAME and dt.GATEWAY_ID = b.GATEWAY_ID group by Cities; ";
			
//			log.error("allProjects && allGateways ");
		
		}
		else if(!allProjects && allGateways)
		{
			
			sqlQuery = "select dt.location as Cities, count(distinct (concat(dt.username, b.AUTH_PROFILE, dt.gateway_id))) as People, count(dt.session_start) as Visits,"
					+ " sum(dt.no_of_actions) as Actions, sum(dt.no_of_screens) as noOfScreens from"
					+ " ( select a.location as location , a.username as username, b.session_start as session_start,"
					+ " b.no_of_actions as no_of_actions,  b.no_of_screens as no_of_screens , b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID "
					+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location"
					+ "	from mod_ia_aggregates_clients  join mod_ia_aggregates_location_info"
					+ " on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP "
					+ " and mod_ia_aggregates_clients.PROJECT = '" + projectName + "'"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = mod_ia_aggregates_location_info.GATEWAY_ID where " + dateFilterClients
					+ "	) as a, mod_ia_aggregates_daily_sessions b	where session_start != session_end and " + dateFilter 
					+ " and a.username = b.username and a.start_timestamp >= b.session_start"
					+ "	and a.start_timestamp <= b.session_end and b.PROJECT_NAME = '" + projectName + "' "
					+ "	union "
					+ "	select a.location as location , a.username as username, b.session_start as session_start,"
					+ "	b.no_of_actions as no_of_actions, b.no_of_screens as no_of_screens, b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID "
					+ "	from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location"
					+ "	from mod_ia_aggregates_clients  join mod_ia_aggregates_location_info"
					+ "	on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP"
					+ " and mod_ia_aggregates_clients.PROJECT = '" + projectName + "'"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = mod_ia_aggregates_location_info.GATEWAY_ID where " + dateFilterClients + " ) as a,"
					+ "	mod_ia_aggregates_daily_sessions b	where session_start = session_end and " 
					+ dateFilter + " and a.username = b.username and a.start_timestamp >= b.session_start "
					+ " and b.PROJECT_NAME = '" + projectName + "' "
					+ " ) as dt, mod_ia_aggregates_projects b where dt.PROJECT_NAME = b.PROJECT_NAME and dt.GATEWAY_ID = b.GATEWAY_ID group by Cities;";
//			log.error("!allProjects && allGateways ");
					
		}
		else if(allProjects && !allGateways)
		{
			
			sqlQuery = "select dt.location as Cities, count(distinct (concat(dt.username, b.AUTH_PROFILE, dt.gateway_id))) as People, count(dt.session_start) as Visits,"
					+ " sum(dt.no_of_actions) as Actions , sum(dt.no_of_screens) as noOfScreens from"
					+ " ( select a.location as location , a.username as username, b.session_start as session_start,"
					+ " b.no_of_actions as no_of_actions,  b.no_of_screens as no_of_screens ,  b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID"
					+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location"
					+ "	from mod_ia_aggregates_clients  join mod_ia_aggregates_location_info"
					+ " on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = '" + GatewayName + "'"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = mod_ia_aggregates_location_info.GATEWAY_ID where " + dateFilterClients
					+ "	) as a, mod_ia_aggregates_daily_sessions b	where session_start != session_end and " + dateFilter 
					+ " and a.username = b.username and a.start_timestamp >= b.session_start"
					+ "	and a.start_timestamp <= b.session_end and b.GATEWAY_ID = '" + GatewayName + "' "
					+ "	union "
					+ "	select a.location as location , a.username as username, b.session_start as session_start,"
					+ "	b.no_of_actions as no_of_actions, b.no_of_screens as no_of_screens,  b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID"
					+ "	from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location"
					+ "	from mod_ia_aggregates_clients  join mod_ia_aggregates_location_info"
					+ "	on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = '" + GatewayName + "'"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = mod_ia_aggregates_location_info.GATEWAY_ID where " + dateFilterClients + " ) as a,"
					+ "	mod_ia_aggregates_daily_sessions b	where session_start = session_end and " 
					+ dateFilter + " and a.username = b.username and a.start_timestamp >= b.session_start "
					+ " and b.GATEWAY_ID = '" + GatewayName + "' "
					+ " ) as dt, mod_ia_aggregates_projects b where dt.PROJECT_NAME = b.PROJECT_NAME and dt.GATEWAY_ID = b.GATEWAY_ID group by Cities;";
//			log.error("allProjects && !allGateways ");
			//log.error("City Query : " + sqlQuery);
		}
		else
		{
			
			sqlQuery = "select dt.location as Cities, count(distinct (concat(dt.username, b.AUTH_PROFILE, dt.gateway_id))) as People, count(dt.session_start) as Visits,"
					+ " sum(dt.no_of_actions) as Actions , sum(dt.no_of_screens) as noOfScreens from"
					+ " ( select a.location as location , a.username as username, b.session_start as session_start,"
					+ " b.no_of_actions as no_of_actions,  b.no_of_screens as no_of_screens ,  b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID"
					+ " from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location"
					+ "	from mod_ia_aggregates_clients  join mod_ia_aggregates_location_info"
					+ " on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = '" + GatewayName + "'"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = mod_ia_aggregates_location_info.GATEWAY_ID where " + dateFilterClients
					+ "	) as a, mod_ia_aggregates_daily_sessions b	where session_start != session_end and " + dateFilter 
					+ " and a.username = b.username and a.start_timestamp >= b.session_start"
					+ "	and a.start_timestamp <= b.session_end and b.PROJECT_NAME = '" + projectName + "' and b.GATEWAY_ID = '" + GatewayName + "' "
					+ "	union "
					+ "	select a.location as location , a.username as username, b.session_start as session_start,"
					+ "	b.no_of_actions as no_of_actions, b.no_of_screens as no_of_screens,  b.PROJECT_NAME as PROJECT_NAME, b.GATEWAY_ID as GATEWAY_ID"
					+ "	from (select username, START_TIMESTAMP, INTERNAL_IP,HOST_INTERNAL_IP, CONCAT(CITY, ', ', STATE, ', ', COUNTRY) as location"
					+ "	from mod_ia_aggregates_clients  join mod_ia_aggregates_location_info"
					+ "	on mod_ia_aggregates_clients.HOST_INTERNAL_IP = mod_ia_aggregates_location_info.INTERNAL_IP and mod_ia_aggregates_clients.HOST_EXTERNAL_IP = mod_ia_aggregates_location_info.EXTERNAL_IP"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = '" + GatewayName + "'"
					+ " and mod_ia_aggregates_clients.GATEWAY_ID = mod_ia_aggregates_location_info.GATEWAY_ID "
					+ "	where " + dateFilterClients + " ) as a,"
					+ "	mod_ia_aggregates_daily_sessions b	where session_start = session_end and " 
					+ dateFilter + " and a.username = b.username and a.start_timestamp >= b.session_start "
					+ " and b.PROJECT_NAME = '" + projectName + "' and b.GATEWAY_ID = '" + GatewayName + "' "
					+ " ) as dt, mod_ia_aggregates_projects b where dt.PROJECT_NAME = b.PROJECT_NAME and dt.GATEWAY_ID = b.GATEWAY_ID group by Cities;";
//			log.error("!allProjects && !allGateways ");	
		}
		
		try {
			con = ds.getConnection();
//			log.error("City Query : " + sqlQuery);
			
			citiesData = con.runQuery(sqlQuery);
			
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getCitiesReportDataController : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		
		return citiesData;

	}
	
	/**
	 * @author sayali  created on 17-01-2017
	 * Function to get alarm summary on controller
	 */
	
	public Dataset getAlarmsSummaryReportController( String GatewayName, String projectName,boolean allGateways, boolean allProjects, int duration){
		Dataset freqData = null;
		Datasource ds;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		SRConnection con = null;
				
		String dateFilter = getDateFilterOnController(duration, "ALARM_DATE");
		String sqlQuery = "";
		
		/**
		 * commenting the if else condition as alarms are not specific to a project.
		 */
		if(allGateways){
			sqlQuery = "select GATEWAY_ID, alarm_name, alarm_priority, sum(alarms_count) as Quantity,"
					 + " time_format(SEC_TO_TIME(avg(TIME_TO_SEC(coalesce(avg_time_to_ack,0.0)))),'%H:%i:%s') as TimeToAck,"
					 + " time_format(SEC_TO_TIME(avg(TIME_TO_SEC(coalesce(avg_time_to_clear,0.0)) )), '%H:%i:%s')as TimetToClr,"
					 + " avg(TIME_TO_SEC(avg_time_to_ack)) timeToAckSeconds,"
					 + " avg(TIME_TO_SEC(avg_time_to_clear) ) as timeToClrSeconds from mod_ia_aggregates_daily_alarms_summary where "
					 + 	dateFilter
					 + " group by alarm_priority,alarm_name, gateway_id;";
		}else{

			sqlQuery = "select GATEWAY_ID, alarm_name, alarm_priority, sum(alarms_count) as Quantity,"
					+ " time_format(SEC_TO_TIME(avg(TIME_TO_SEC(coalesce(avg_time_to_ack,0.0)))),'%H:%i:%s') as TimeToAck,"
					+ " time_format(SEC_TO_TIME(avg(TIME_TO_SEC(coalesce(avg_time_to_clear,0.0)) )), '%H:%i:%s')as TimetToClr,"
					+ " avg(TIME_TO_SEC(avg_time_to_ack)) timeToAckSeconds,"
					+ " avg(TIME_TO_SEC(avg_time_to_clear) ) as timeToClrSeconds from mod_ia_aggregates_daily_alarms_summary where "
					+ 	dateFilter
					+ " and GATEWAY_ID='"
					+ GatewayName
					+ "' group by alarm_priority,alarm_name;";

		}
//		System.out.println("getAlarmsSummaryReportController"+sqlQuery);
		try {
			con = ds.getConnection();
			
			freqData = con.runQuery(sqlQuery);
			
		}
		catch (SQLException e) {
		
		e.printStackTrace();
	}
	finally{
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			log.error("getAlarmsSummaryReportController : in con close exception.");
			
			e.printStackTrace();
		}
		
		}
	}
		return freqData;

	}
	
	/**
	 * Method to return Groups information for reports. sayali
	 * @throws MODIAServiceUnavailableException 
	 * 
	 */
	@Override
	public List<GroupReportRecord> getGroupsReportDataController(
			String GatewayName, String projectName, boolean allGateways,
			boolean allProjects, int duration) {

		List<GroupReportRecord> returnData = new ArrayList<GroupReportRecord>();
//		Iterator<String> retrieveRoles;
//		Iterator<User> retrieveUsers;
//		String roleName;
//		GroupReportRecord _record;
//		HashMap<String, Integer> rolesMapPeople = new HashMap<String, Integer>();
//		HashMap<String, Integer> rolesMapVisits = new HashMap<String, Integer>();
//		HashMap<String, Integer> rolesMapActions = new HashMap<String, Integer>();
//		Collection<String> profileRoles = null;
//		Collection<User> profileUsers = null;
//		HashMap<String, String> allProfileRoles = new HashMap<String, String>();
//		HashMap<String, Collection<User>> profileUsersMap = new HashMap<String, Collection<User>>();
//		Dataset groupsData = null;
//		int noOfRecs = 0, i = 0;
		
		
		// call service on controller to sync projects data
		GatewayNetworkManager gm = this.mycontext
				.getGatewayAreaNetworkManager();


//		log.error("getGroupsReportDataController : function called. with gateway name : " + GatewayName);
		try {
			if (this.isAgent == false) {
//				log.error("getGroupsReportDataController : in isGAnet false.");
				if (GatewayName.compareToIgnoreCase(gm.getServerAddress()
						.getServerName().trim()) == 0) {
//					log.error("getGroupsReportDataController : gateway is controller.");
					returnData = this
							.getGroupsReportData(projectName, allProjects, duration);
				} 
			
				else {
					
//					log.error("getGroupsReportDataController : calling service for gateway : " + GatewayName);
					ServiceManager sm = gm.getServiceManager();
					ServerId sid = new ServerId(GatewayName);
					ServiceState s = sm.getRemoteServiceState(sid,
							AgentService.class);
					// if service is available
					if (s == ServiceState.Available) {
						// call the service
//						log.error("getGroupsReportDataController : service available for  : " + GatewayName);
						returnData = sm.getService(sid, AgentService.class)
								.get().getGroupsReportInformation(projectName, allProjects, duration);
					} else {
						throw new MODIAServiceUnavailableException(
								"Agent Service is unavailable on gateway : "
										+ GatewayName);
					}
				}
			
			}
		} catch (Exception e) {
			log.error("getGroupsReportDataController : Exception in calling agent service "
					+ e);
//			throw new MODIAServiceUnavailableException(
//					"Agent Service is unavailable on gateway : " + GatewayName);
		}
	

		return returnData;
	}
	
	/*
	 * query total no of distinct users across all browsers. This is because
	 * mobile clients would not have browser information. (non-Javadoc)
	 * sayali 10-01-2017
	 * @see
	 * com.vaspsolutions.analytics.common.ModuleRPC#getDistinctUsersFromBrowsers
	 * (int, java.lang.String, boolean)
	 */

	public int getDistinctUsersFromBrowsersController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects) {
		int userCount = 0;
		Dataset browserData = null;
		Datasource ds;
		String sqlQuery = "";

		SRConnection con = null;

		String timefilter = getDateFilterOnController(duration, "c.START_TIMESTAMP");
		String filter= "";
		
		if(allProjects && allGateways)
		{
			filter = "";
		}
		else if(!allProjects && allGateways)
		{
			filter = " and c.PROJECT = '" + projectName + "' ";
		}
		else if(allProjects && !allGateways)
		{
			filter = " and c.GATEWAY_ID = '" + GatewayName + "' ";
		}
		else if(!allProjects && !allGateways)
		{
			filter = " and c.GATEWAY_ID = '" + GatewayName + "' and c.PROJECT = '" + projectName + "' ";
		}

		sqlQuery = " select count(distinct(concat(c.username, a.AUTH_PROFILE,d.gateway_id)))"
				+ " from mod_ia_aggregates_clients c, mod_ia_aggregates_browser_info  d, mod_ia_aggregates_projects a "
				+ " where (c.HOST_INTERNAL_IP = d.IP_ADDRESS OR c.HOST_EXTERNAL_IP = d.IP_ADDRESS ) "
				+ " and  d.IP_ADDRESS is not null and " + timefilter
				+ " and d.timestamp <= c.start_timestamp and a.PROJECT_NAME = c.PROJECT "
				+ "and a.GATEWAY_ID = d.GATEWAY_ID" + filter + ";";
		
//		log.error("getDistinctUsersFromBrowsersController :  "+sqlQuery);
		// connect to the database and get records
		try {

			ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
			con = ds.getConnection();

			
			browserData = con.runQuery(sqlQuery);

			if (browserData != null && browserData.getRowCount() > 0) {
				if (browserData.getValueAt(0, 0) != null) {
					userCount = (int) Float.parseFloat(browserData.getValueAt(
							0, 0).toString());
				}

			}
		} catch (Exception e) {
			log.error("getDistinctUsersFromBrowsersController : " + e);
		} finally {
			// close the database connection
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getDistinctUsersFromBrowsersController : in con close exception.");

					e.printStackTrace();
				}

			}
		}
		return userCount;
	}
	
	/**
	 * * Method to retrieve average time to clear alarms per priority
	 * @author YM
	 * @param duration
	 * @param dataSource
	 * @return List containing Priority wise average time.
	 */
	@Override
	public Dataset getAlarmsClearTimeController(int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects) {
		
		
		Datasource ds;
		Dataset resDS = null;
		
		ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
		
		String sqlQuery = "";
		SRConnection con = null;
		String dateFilter = getDateFilterOnController(duration,"ALARM_DATE");	
		int month = getLastMonth();
		int year = getThisYear();
		int thisYear = getThisYear();
		if(month == 0)
		{
			month = 12;
			year = getLastYear();
		}
		
		int lastYear = getLastYear();
		if(allGateways){
			sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_CLEAR,0.0)))), '%H:%i:%s')"
					+ " FROM mod_ia_aggregates_daily_alarms_summary WHERE "
					+ dateFilter + " group by ALARM_PRIORITY";
		}
		else{
			sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_CLEAR,0.0)))), '%H:%i:%s')"
					+ " FROM mod_ia_aggregates_daily_alarms_summary WHERE GATEWAY_ID = '" + GatewayName + "' and "
					+ dateFilter + " group by ALARM_PRIORITY";
		}
//		System.out.println("getAlarmsClearTimeController"+sqlQuery);
		int r=0;
			try {
				
					con = ds.getConnection();
					
					resDS = con.runQuery(sqlQuery);
					
					
					}
					catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
				
				}
		}
		
		return resDS;
	}
	
	
	//Written by Omkar on 5 Jan 2017
	
			public CurrentOverview getRealTimeAllGateWayOverview(String gateWayName, String currentProject, boolean allProjects) {
				CurrentOverview overview = null;
				
				
//			log.error("getRealTimeAllGateWayOverview : for gateway" + gateWayName);
			GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
			try{
			if (this.isAgent == false){
				
				
				ServiceManager sm = gm.getServiceManager();
				ServerId sid = new ServerId(gateWayName);
				if(gateWayName.compareToIgnoreCase(gm.getServerAddress().getServerName().trim()) == 0)
				{
					//System.out.println("Controller function is called");
					overview = this.getCurrentOverview(currentProject, allProjects);
				}
				else
				{
					
					ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
					if(s == ServiceState.Available){
					
					 overview = sm.getService(sid, AgentService.class).get().getCurrentOverViewOnAgent(currentProject, allProjects);
//					 log.error("Service called on Gateway : " + gateWayName);
				}
				else {
//					log.error("Service is not called");
					//throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gateWayName);
					
				}
				}

			}
			else{
//				log.error("Is a Agent: agent function is called");
				overview = this.getCurrentOverview(currentProject, allProjects);

			}
			
			
			

			
			}
			catch (Exception e){
				//throw new MODIAServiceUnavailableException();
			}
			return overview;	
		}
			
			/*
			 * 
			 */
			private String getFirstSeenOnController(String userName, boolean allProjects, String projectName, String gatewayName, String userProfile) {
				String firstSeen = "";
				Datasource _ds ; //local variable
				Dataset resDS = null;
			
				_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				//noOfDS = dsList.size();
				String sqlQuery = "";
				if(allProjects)
				{
					sqlQuery = "SELECT first_seen from mod_ia_aggregates_users"
						+ " WHERE gateway_id = '" + gatewayName + "' and projectname = 'All'"
						+ " and gateway_userprofile = '" + userProfile + "' and "
						+ " username = '" + userName + "';";
				}
				else
				{
					sqlQuery = "SELECT first_seen from mod_ia_aggregates_users"
							+ " WHERE gateway_id = '" + gatewayName + "' and projectname = '" + projectName + "'"
							+ " and gateway_userprofile = '" + userProfile + "' and "
							+ " username = '" + userName + "';";
				}
//				log.error("get first seen on controller q : " + sqlQuery);	
				SRConnection con = null;
					
				try {
					
						con = _ds.getConnection();
						resDS = con.runQuery(sqlQuery);
						
						if(resDS != null && resDS.getRowCount() > 0)
						{
							if(resDS.getValueAt(0, 0) != null)
							{
								firstSeen = resDS.getValueAt(0, 0).toString().trim();
							}
						}
				}
				catch (SQLException e) {
					
					log.error(e);
					
				}
				finally{
					if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
						
							e.printStackTrace();
						}
						
						}
				}
				
				return firstSeen;
			}
			
			
			/****** Methods to retrieve Alarms information for Real Time Panel
			 * 
			 */
			/*
			 * Method to retrieve avg alam clear time per priority
			 */
			@Override
			public Dataset getAlarmsClearTimeOnController(int duration,
					String gatewayName, boolean allGateways) {
				Datasource ds;
				Dataset resDS = null;
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				
				String sqlQuery = "";
				SRConnection con = null;
				String dateFilter = getDateFilterOnController(duration,"ALARM_DATE");	
				if(allGateways)
				{
					sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_CLEAR,0.0)))), '%H:%i:%s')"
							+ " FROM mod_ia_aggregates_daily_alarms_summary WHERE "
							+ dateFilter + " group by ALARM_PRIORITY";
				}
				else
				{
					sqlQuery = "SELECT ALARM_PRIORITY, time_format(SEC_TO_TIME(AVG(TIME_TO_SEC(coalesce(AVG_TIME_TO_CLEAR,0.0)))), '%H:%i:%s')"
							+ " FROM mod_ia_aggregates_daily_alarms_summary WHERE "
							+ dateFilter + " AND GATEWAY_ID = '" + gatewayName + "' group by ALARM_PRIORITY";
				}
					
				int r=0;
					try {
						
							con = ds.getConnection();
							
							resDS = con.runQuery(sqlQuery);
							
							
							}
							catch (SQLException e) {
						
						e.printStackTrace();
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							
							e.printStackTrace();
						}
						
						}
				}
				
				return resDS;
			}
			
			@Override
			public Dataset getActiveAlarmsCountOnController(String gatewayName, boolean allGateways) {

				
				
				Datasource ds;
				Dataset resDS = null;
				
				int noOfSearchResults = 0;
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				String sqlQuery = "";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				if(allGateways)
				{
					sqlQuery = "SELECT PRIORITY, SUM(ACTIVE_COUNT)"
							+ " FROM MOD_IA_AGGREGATES_DAILY_ALARM_ACTIVE_COUNTS"
							+ " WHERE ALARM_DATE = '"+ sdf.format(new Date())+ "'"
							+ " GROUP BY PRIORITY;";
				}
				else
				{
					sqlQuery = "SELECT PRIORITY, ACTIVE_COUNT"
							+ " FROM MOD_IA_AGGREGATES_DAILY_ALARM_ACTIVE_COUNTS"
							+ " WHERE ALARM_DATE = '"+ sdf.format(new Date())+ "'"
							+ " and GATEWAY_ID = '" + gatewayName +"';";
				}
				
				SRConnection con = null;
					try {
						
							con = ds.getConnection();
							resDS = con.runQuery(sqlQuery);
					}
							catch (SQLException e) {
						
						e.printStackTrace();
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {

							e.printStackTrace();
						}
						
						}
				}
				
				return resDS;
			}

			
			/** 
			 * Function to retrieve number of acknowledged alarms for each priority level
			 * for today. To be shown as real time information.
			 * @author YM 04/17/2015 
			 * @param dataSource Data source name from where to retrieve the information
			 * @return  dataset with priority , number of alarms
			 * @see Dataset
			 * */
			@Override
			public Dataset getAckAlarmsCountOnController( String gatewayName, boolean allGateways) {
				Datasource ds;
				Dataset resDS = null;
				
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				String sqlQuery = "";
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				if(allGateways)
				{
					sqlQuery = "SELECT PRIORITY, SUM(ACK_COUNT)"
							+ " FROM MOD_IA_AGGREGATES_DAILY_ALARM_ACK_COUNTS"
							+ " WHERE ALARM_DATE = '"+ sdf.format(new Date())+ "'"
							+ " GROUP BY PRIORITY;";
				}
				else
				{
					sqlQuery = "SELECT PRIORITY, ACK_COUNT"
							+ " FROM MOD_IA_AGGREGATES_DAILY_ALARM_ACK_COUNTS"
							+ " WHERE ALARM_DATE = '"+ sdf.format(new Date())+ "'"
							+ " and GATEWAY_ID = '" + gatewayName +"';";
				}
				
				SRConnection con = null;
					
				
				int r=0;
					try {
						
							con = ds.getConnection();
							resDS = con.runQuery(sqlQuery);
							
							
							}
							catch (SQLException e) {

						e.printStackTrace();
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("getAckAlarmsCount : in con close exception.");
							
							e.printStackTrace();
						}
						
						}
				}
				
				return resDS;
			}
			
			
			
			/* By Yogini
			 * Real Time Panle - COntent section
			 */
			@Override
			public HashMap<String,HashMap<String,Integer>> getNumberOfUsersPerScreenRealTimeOnController(String gatewayName, boolean allGateways, String projectName, boolean allProjects)  
			{
				HashMap<String,HashMap<String,Integer>> returnData = new HashMap<String,HashMap<String,Integer>>();
				GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
				ServiceManager sm = gm.getServiceManager();
				HashMap<String,Integer> contentData = null;
				if (this.isAgent == false)
				{
					if(allGateways)
					{
						//first get list of all gateways and then for each gateway retrieve real time data
						String[] gateways = this.getGateways();
					
						int i, noOfGateways = 0;
						noOfGateways = gateways.length;
						contentData = null;
						for(i=1; i<noOfGateways;i++)
						{
							contentData = null;
							ServerId sid = new ServerId(gateways[i]);
							if(gateways[i].compareToIgnoreCase(gm.getServerAddress().getServerName().trim()) == 0)
							{
//								log.error("Controller function is called");
								contentData = this.getNumberOfUsersPerScreenRealTime(projectName, allProjects);
							}
							else
							{
								ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
								if(s == ServiceState.Available){
									
									contentData = sm.getService(sid, AgentService.class).get().getNumberOfUsersPerScreenRealTimeFromAgent(projectName, allProjects);
//									 log.error("Service called on Gateway : " + gateways[i]);
								}
								else {
//									log.error("Service is not called");
									//throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gateways[i]);
									
								}
							}
							returnData.put(gateways[i], contentData);
						}
					}
					else
					{
						//get the data for specific gateway
						ServerId sid = new ServerId(gatewayName);
						if(gatewayName.compareToIgnoreCase(gm.getServerAddress().getServerName().trim()) == 0)
						{
//							System.out.println("Controller function is called");
							contentData = this.getNumberOfUsersPerScreenRealTime(projectName, allProjects);
						}
						else
						{
							ServiceState s = sm.getRemoteServiceState(sid, AgentService.class);
							if(s == ServiceState.Available){
								
								contentData = sm.getService(sid, AgentService.class).get().getNumberOfUsersPerScreenRealTimeFromAgent(projectName, allProjects);
//								log.error("Service called on Gateway : " + gatewayName);
							}
							else {
//								log.error("Service is not called");
								//throw new MODIAServiceUnavailableException("Agent Service is unavailable on gateway : " + gatewayName);
								
							}
						}
						returnData.put(gatewayName, contentData);
					}	
				}
				
				return returnData;
			}

			@Override
			public boolean receiveModIAAuditEvents(String agentID, Dataset modIAAudit) {
				boolean returnValue = true;
				
				if(this.isGatewayMonitored(agentID))
				{
					if(modIAAudit == null || modIAAudit.getRowCount() == 0)
					{
						log.error("receiveModIAAuditEvents was called however there was nothing to insert from Gateway : " + agentID);
					}
					else
					{
						int actionsSize = modIAAudit.getRowCount();
						int i = 0;
						Datasource _ds;
						
						_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
						String sqlQuery = "";
						SRConnection con = null;
						try
						{
							con = _ds.getConnection();
							
							sqlQuery = "DELETE FROM mod_ia_aggregates_audit_events"
									+ " WHERE GATEWAY_ID = '" + agentID + "' and"
											+ " EVENT_TIMESTAMP >= '"
									+ modIAAudit.getValueAt(0 , "EVENT_TIMESTAMP").toString()
									+ "';";
							
							con.runUpdateQuery(sqlQuery);
							
							sqlQuery = "INSERT INTO mod_ia_aggregates_audit_events"
									+ "(GATEWAY_ID, EVENT_TIMESTAMP, ACTOR, ACTOR_HOST, ACTION, ACTION_TARGET, ACTION_VALUE, STATUS_CODE, ORIGINATING_SYSTEM, ORIGINATING_CONTEXT) VALUES (";
							String timeStamp = "";
							for(i=0; i<actionsSize; i++)
							{
								
	//							log.error("receiveAudit actions timeStamp value : " + auditActions.getValueAt(i , "EVENT_TIMESTAMP").toString() + ", i =  " + i);
								if (modIAAudit.getValueAt(i , "EVENT_TIMESTAMP").toString().contains("."))
								{
									timeStamp = (modIAAudit.getValueAt(i , "EVENT_TIMESTAMP").toString()).substring(0, 19);
								}
								else
								{
									timeStamp = modIAAudit.getValueAt(i , "EVENT_TIMESTAMP").toString();
								}
								
								if(i == actionsSize - 1)
								{
									sqlQuery = sqlQuery 
										+ "'" + agentID + "',"
										+ "'" + timeStamp + "',"
										+ "'" + modIAAudit.getValueAt(i,"ACTOR") + "',"
										+ "'" + modIAAudit.getValueAt(i,"ACTOR_HOST") + "',"
										+ "'" + modIAAudit.getValueAt(i,"ACTION") + "',"
										+ "'" + modIAAudit.getValueAt(i,"ACTION_TARGET") + "',"
										+ "'" + modIAAudit.getValueAt(i,"ACTION_VALUE") + "',"
										+ modIAAudit.getValueAt(i,"STATUS_CODE") + ","
										+ "'" + modIAAudit.getValueAt(i,"ORIGINATING_SYSTEM") + "',"
										+ "'" + modIAAudit.getValueAt(i,"ORIGINATING_CONTEXT") + "'"
										
		 										+ ");";
								}
								else
								{
									sqlQuery = sqlQuery 
											+ "'" + agentID + "',"
											+ "'" + timeStamp + "',"
											+ "'" + modIAAudit.getValueAt(i,"ACTOR") + "',"
											+ "'" + modIAAudit.getValueAt(i,"ACTOR_HOST") + "',"
											+ "'" + modIAAudit.getValueAt(i,"ACTION") + "',"
											+ "'" + modIAAudit.getValueAt(i,"ACTION_TARGET") + "',"
											+ "'" + modIAAudit.getValueAt(i,"ACTION_VALUE") + "',"
											+ modIAAudit.getValueAt(i,"STATUS_CODE") + ","
											+ "'" + modIAAudit.getValueAt(i,"ORIGINATING_SYSTEM") + "',"
											+ "'" + modIAAudit.getValueAt(i,"ORIGINATING_CONTEXT") + "'"	
													+ "),(";
								}
								
							}
//							log.error("receiveModIAAuditEvents  : ins q : " + sqlQuery);
							con.runUpdateQuery(sqlQuery);	
						}
						catch(Exception e){
							returnValue = false;
							e.printStackTrace();
						}
						finally{
							if(con!=null){
								try {
									con.close();
								} catch (SQLException e) {
									log.error("receiveModIAAuditEvents : in con close exception.");
									
									e.printStackTrace();
								}
							}
						}
					}
				}
				return returnValue;
			}

			@Override
			public boolean sendModIAAuditEvents() {
				boolean returnValue = true;
				
				Datasource _ds;
				Dataset resDS = null;
				
				_ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				LastSyncData _lstSync = LastSyncData.getInstance(_ds);
				
				String sqlQuery = "";
				SRConnection con = null;
				
				try
				{
					con = _ds.getConnection();
					if(_lstSync.getLast_sync_mod_ia_audit() != null)
					{
						sqlQuery = "SELECT EVENT_TIMESTAMP, ACTOR, ACTOR_HOST, ACTION, ACTION_TARGET, ACTION_VALUE, STATUS_CODE, ORIGINATING_SYSTEM, ORIGINATING_CONTEXT "
								+ " FROM MOD_IA_AUDIT_EVENTS WHERE EVENT_TIMESTAMP >= '" 
								+ _lstSync.getLast_sync_mod_ia_audit() + "' order by EVENT_TIMESTAMP ASC;";	
					}
					else
					{
						sqlQuery = "SELECT EVENT_TIMESTAMP, ACTOR, ACTOR_HOST, ACTION, ACTION_TARGET, ACTION_VALUE, STATUS_CODE, ORIGINATING_SYSTEM, ORIGINATING_CONTEXT"
								+ " FROM MOD_IA_AUDIT_EVENTS order by EVENT_TIMESTAMP ASC;";	
						
					}
					resDS = con.runQuery(sqlQuery);
					
				}
				catch(Exception e){
					returnValue = false;
					e.printStackTrace();
				}
				finally{
					if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("sendModIAAuditEvents : in con close exception.");
							
							e.printStackTrace();
						}
					}
				}
				
				if(resDS != null && resDS.getRowCount() > 0)
				{
					//call service on Controller to send data
					GatewayNetworkManager gm = this.mycontext.getGatewayAreaNetworkManager();
					if(this.isAgent)
					{
						ServiceManager sm = gm.getServiceManager();
						ServerId sid = new ServerId(this.controllerName);
						ServiceState s = sm.getRemoteServiceState(sid, GetAnalyticsInformationService.class);
//						log.error("sendAuditActions : on agent sid = " + sid.getServerName());
						//if service is available
						if(s == ServiceState.Available)
						{
							//call the service 
							sm.getService(sid, GetAnalyticsInformationService.class).get().receiveModIAAuditEvents(gm.getServerAddress().getServerName(), resDS);
						}
						else
						{
							returnValue = false;
						}
					}
					else
					{
						this.receiveModIAAuditEvents(gm.getServerAddress().getServerName(), resDS);
					}
					if (returnValue == true)
					{
						_lstSync.setLast_sync_mod_ia_audit(resDS.getValueAt(resDS.getRowCount() - 1, "EVENT_TIMESTAMP").toString());
					}
				}
				
			
			return returnValue;
			}

			@Override
			public boolean isGatewayMonitored(String gatewayName) {
				boolean gatewayMonitored = false;
				
				Datasource ds;
				Dataset resDS = null;

				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				String sqlQuery = "SELECT GAN_SERVERNAME FROM mod_ia_monitored_gateways where GAN_SERVERNAME = '"
						+ gatewayName + "';";
				SRConnection con = null;
				try {
					con = ds.getConnection();
					resDS = con.runQuery(sqlQuery);
					if(resDS != null && resDS.getRowCount() > 0)
					{
						gatewayMonitored = true;
					}
				}
				catch (SQLException e) {
				
				e.printStackTrace();
			}
			finally{
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("isGatewayMonitored : in con close exception.");
					
					e.printStackTrace();
				}
				
				}
			}
				
				return gatewayMonitored;
			}
			
			
			
			private boolean isProjectMonitored(String projectName) {
				boolean projectMonitored = false;
				String _projects[] = this.getProjects(projectName);
				int size = _projects.length;
				
				for(int i=0; i<size; i++)
				{
					if(_projects[i].compareToIgnoreCase(projectName) == 0)
					{
						projectMonitored = true;
						break;
					}
				}
				return projectMonitored;
			}
			
			/*
			 * By Sayali 21-03-2017
			 */
			@Override
			public Dataset getTop10AlarmsByDurationController(String projectName, boolean allProjects, String GatewayName, boolean allGateways, int duration){
				Datasource ds;
				Dataset resDS = null;

				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				String sqlQuery = "";
				SRConnection con = null;
				String dateFilter = getDateFilterOnController(duration, "ALARM_DATE");
				
				if(allGateways){
					sqlQuery = "select ALARM_NAME, SUM(TIME_TO_SEC(TOTAL_ACTIVE_TIME)) totalTime from mod_ia_aggregates_daily_alarms_summary "
							+ " where "
							+ dateFilter
							+ " group by ALARM_NAME"
							+ " order by totalTime DESC"
							+ " limit 10;";
				}
				else{
					sqlQuery = "select ALARM_NAME, SUM(TIME_TO_SEC(TOTAL_ACTIVE_TIME)) totalTime from mod_ia_aggregates_daily_alarms_summary "
							+ " where "
							+ dateFilter
							+ " and GATEWAY_ID='"
							+ GatewayName
							+ "' group by ALARM_NAME"
							+ " order by totalTime DESC"
							+ " limit 10;";
				}
					
//				log.error("getTop10AlarmsByDurationController: "+sqlQuery);
					try {
							con = ds.getConnection();
							resDS = con.runQuery(sqlQuery);
							
						}
						catch (SQLException e) {
						
						e.printStackTrace();
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("getTop10AlarmsByDuratioController : in con close exception.");
							
							e.printStackTrace();
						}
						
						}
				}
			return resDS;
			}
			
			/*
			 * By sayali 21-03-2017
			 */
			@Override
			public Dataset alarmSummaryReportRingChartController(String projectName,boolean allProjects, String GatewayName, boolean allGateways, int duration){
				Datasource ds;
				Dataset resDS = null;

				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				String sqlQuery = "";
				SRConnection con = null;
				String dateFilter = getDateFilterOnController(duration, "ALARM_DATE");
				
				if(allGateways){
					sqlQuery = "select ALARM_NAME, SUM(ALARMS_COUNT) totalAlarms from mod_ia_aggregates_daily_alarms_summary "
							+ " where "
							+ dateFilter
							+ " group by ALARM_NAME"
							+ " order by totalAlarms DESC"
							+ " limit 10;";
				}else{
						sqlQuery = "select ALARM_NAME, SUM(ALARMS_COUNT) totalAlarms from mod_ia_aggregates_daily_alarms_summary "
								+ " where "
								+ dateFilter
								+ " and GATEWAY_ID='"
								+ GatewayName
								+ "' group by ALARM_NAME"
								+ " order by totalAlarms DESC"
								+ " limit 10;";
					}
//				log.error("alarmSummaryReportRingChartController: "+sqlQuery);
					try {
							con = ds.getConnection();
							resDS = con.runQuery(sqlQuery);
							
						}
						catch (SQLException e) {
						
						e.printStackTrace();
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("alarmSummaryReportRingChartController : in con close exception.");
							
							e.printStackTrace();
						}
						
						}
				}
			return resDS;
			}
			
			/*
			 * Sayali : For alarm summary chart 21-03-2017
			 */
			@Override
			public Dataset getAlarmCountsPerDurationController(String projectName,boolean allProjects, String GatewayName, boolean allGateways, int duration){
				Datasource ds;
				Dataset resDS = null;

				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				String sqlQuery = "";
				SRConnection con = null;
				String dateFilter = getDateFilterOnController(duration, "ALARM_DATE");
				String filter = "";
				
				if(allGateways)
				{
					filter = "";
				}
				else
				{
					filter =  " and GATEWAY_ID='"
							+ GatewayName
							+ "'";
				}
				
				if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
				{
					sqlQuery = "select a.Hour as Hour, coalesce(b.ALARM_COUNT, 0) from mod_ia_hours  a "
							+ " left join "
							+ " (select ALARM_HOUR, sum(ALARMS_COUNT) ALARM_COUNT "
							+ " FROM mod_ia_aggregates_hourly_alarms_counts WHERE "
							+ dateFilter + filter + " group by ALARM_HOUR) as b"
							+ " on a.Hour = b.ALARM_HOUR"
							+ " order by a.Hour"
							+ " ;";
				}
				else if(duration == Constants.THIS_YEAR || duration == Constants.LAST_YEAR || duration == Constants.LAST_365_DAYS)
				{
					sqlQuery =  "select a.monthName, coalesce(b.ALARMS_COUNT,0) "
							+ " from mod_ia_month as a "
							+ " left join (select  month(ALARM_DATE) as overviewmonth ,SUM(ALARMS_COUNT) ALARMS_COUNT "
							+ " from mod_ia_aggregates_daily_alarms_summary where "
							+ dateFilter + filter + " group by overviewmonth ) as b"
							+ " on b.overviewmonth = a.monthNumber"
							+ " order by a.monthNumber"
							+ "  ;";
				}
				else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS )
				{
					sqlQuery = "select dayname(ALARM_DATE), sum(ALARMS_COUNT) from mod_ia_aggregates_daily_alarms_summary where "
							+ dateFilter + filter + " group by ALARM_DATE order by ALARM_DATE;";
				}
				else
				{
				
					sqlQuery = "SELECT ALARM_DATE, SUM(ALARMS_COUNT) "
							+ " FROM mod_ia_aggregates_daily_alarms_summary "
							+ " WHERE " + dateFilter + filter + " group by ALARM_DATE order by ALARM_DATE;"
							+ " ;";
							
				}
				
//				log.error("getAlarmCountsPerDurationController: "+sqlQuery);	
				
					try {
							con = ds.getConnection();
							resDS = con.runQuery(sqlQuery);
							
						}
						catch (SQLException e) {
						
						e.printStackTrace();
					}
					finally{
						if(con!=null){
						try {
							con.close();
						} catch (SQLException e) {
							log.error("getAlarmCountsPerDurationController : in con close exception.");
							
							e.printStackTrace();
						}
						
						}
				}
			return resDS;
			}
			
			/**
			 * Method to retrieve the overview information for given duration and given project/all projects
			 */
			@Override
			public float getBounceRateController( int duration,
					String gatewayName, String projectName, boolean allGateways, boolean allProjects) {

				float bounceRate = 0;
					
					Datasource ds;
					Dataset resDS = null;
					ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					SRConnection con = null;
							
//					String dateFilter = getDateFilter(duration, "overview_date");
//					String screenViewDateFilter = getDateFilter(duration, "VIEW_TIMESTAMP");
//					String auditDateFilter = getDateFilter(duration, "EVENT_TIMESTAMP");
//
//					String sqlQuery = "";
//					String sqlScreensQuery = "";
//					String usersQuery = "";
//
//								
//					if(allProjects && allGateways)
//					{
//						
//						sqlScreensQuery = "SELECT count(screen_name) noOfScreenViews, username"
//								+ " FROM mod_ia_aggregates_daily_screen_views WHERE "
//								+ screenViewDateFilter + " and ACTION='SCREEN_OPEN' "
//								+ " group by username having noOfScreenViews = 1;";
//						
//						usersQuery = "SELECT count(distinct(ACTOR)) as noOfUsers "
//								+ " FROM mod_ia_aggregates_actions WHERE action = 'login' and " + auditDateFilter + " ;";
//					}
//					else if(allProjects && !allGateways)
//					{
//						
//						sqlScreensQuery = "SELECT count(screen_name) noOfScreenViews, username"
//								+ " FROM mod_ia_aggregates_daily_screen_views WHERE "
//								+ screenViewDateFilter + " and ACTION='SCREEN_OPEN' and GATEWAY_ID = '" + gatewayName + "'"
//								+ " group by username having noOfScreenViews = 1;";
//						
//						usersQuery = "SELECT count(distinct(ACTOR)) as noOfUsers "
//								+ " FROM mod_ia_aggregates_actions WHERE  action = 'login' and " + auditDateFilter + " and GATEWAY_ID = '" + gatewayName + "';";
//					}
//					else if(!allProjects && !allGateways)
//					{					
//						sqlScreensQuery = "SELECT count(screen_name) noOfScreenViews, username"
//								+ " FROM mod_ia_aggregates_daily_screen_views WHERE "
//								+ screenViewDateFilter + " and ACTION='SCREEN_OPEN' and GATEWAY_ID = '" + gatewayName + "' AND PROJECT = '"
//								+ projectName + "' "
//								+ " group by username having noOfScreenViews = 1;";
//						
//						usersQuery = "SELECT count(distinct(ACTOR)) as noOfUsers "
//								+ " FROM mod_ia_aggregates_actions WHERE  action = 'login' and " + auditDateFilter + " and GATEWAY_ID = '" + gatewayName + "'"
//								+ " AND PROJECT = '" + projectName  + "';";
//					}
//					else
//					{
//						sqlScreensQuery = "SELECT count(screen_name) noOfScreenViews, username"
//								+ " FROM mod_ia_aggregates_daily_screen_views WHERE "
//								+ screenViewDateFilter + " and ACTION='SCREEN_OPEN' and PROJECT = '" + projectName + "'"
//								+ " group by username having noOfScreenViews = 1;";
//						
//						usersQuery = "SELECT count(distinct(ACTOR)) as noOfUsers "
//								+ " FROM mod_ia_aggregates_actions WHERE  action = 'login' and " + auditDateFilter + " and PROJECT = '" + projectName + "';";
//					}
					String screenViewDateFilter = getDateFilterOnController(duration, "a.VIEW_TIMESTAMP");
					String auditDateFilter = getDateFilterOnController(duration, "a.EVENT_TIMESTAMP");
					String sessionDateFilter = getDateFilterOnController(duration, "s.session_start");
					String sqlScreensQuery = "";
					String usersQuery = "";
					String filter = "";
					
					if(allProjects && allGateways)
					{
						filter = "";
					}
					else if(!allProjects && allGateways)
					{
						filter = " and PROJECT = '" + projectName + "' ";
					}
					else if(allProjects && !allGateways)
					{
						filter = " and GATEWAY_ID = '" + gatewayName + "' ";
					}
					else if(!allProjects && !allGateways)
					{
						filter = " and GATEWAY_ID = '" + gatewayName + "' and PROJECT = '" + projectName + "' ";
					}
				
//					sqlScreensQuery = "SELECT count(screen_name) noOfScreenViews"
//							+ " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
//							+ " WHERE a.ACTION = 'SCREEN_OPEN' AND "
//							+ screenViewDateFilter + " and " + sessionDateFilter + filter
//						    + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID"
//						    + " and a.VIEW_TIMESTAMP >= s.SESSION_START "
//					        + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME "
//							+ " group by a.username having count(a.screen_name) = 1;";
					sqlScreensQuery = "SELECT count(x.SCREEN_NAME) as noOfScreenViews FROM "
							+ "	mod_ia_aggregates_projects b,"
							+ " ( SELECT a.SCREEN_NAME as SCREEN_NAME , a.USERNAME as username, a.PROJECT as project, a.GATEWAY_ID as GATEWAY_ID"
							 + " FROM mod_ia_aggregates_daily_screen_views a, mod_ia_aggregates_daily_sessions s"
							 + " WHERE a.ACTION = 'SCREEN_OPEN' AND " + screenViewDateFilter + " and " + sessionDateFilter + filter
							 + " and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID and a.VIEW_TIMESTAMP >= s.SESSION_START "
							 + " and a.VIEW_TIMESTAMP <= s.SESSION_END and a.USERNAME = s.USERNAME ) as x"
							 + " where x.GATEWAY_ID = b.GATEWAY_ID and x.PROJECT = b.PROJECT_NAME GROUP BY x.username, b.AUTH_PROFILE, x.GATEWAY_ID having count(screen_name) = 1;";
					
					
					usersQuery = "SELECT count(distinct(concat(a.ACTOR,s.auth_profile, a.gateway_id))) as noOfUsers "
							+ " FROM mod_ia_aggregates_actions a, mod_ia_aggregates_projects s"
							+ " WHERE a.action = 'login' and a.PROJECT = s.PROJECT_NAME and a.GATEWAY_ID = S.GATEWAY_ID"
							+ " and " + auditDateFilter + filter + " ;";
//					log.error("getBounceRateController sqlScreensQuery: "+sqlScreensQuery);
//					log.error("getBounceRateController usersQuery: "+usersQuery);
					try {
						con = ds.getConnection();
				
							resDS = con.runQuery(sqlScreensQuery);
							
							float usersWithOneScreen = 0;
							float totalUsers = 0;
							if(resDS != null && resDS.getRowCount() > 0)
							{
//								if(resDS.getValueAt(0, 0) != null)
//								{
//									usersWithOneScreen = Float.parseFloat(resDS.getValueAt(0, 0).toString());
//								}
								usersWithOneScreen = resDS.getRowCount();

							}
							
							resDS = con.runQuery(usersQuery);
							
							if(resDS != null && resDS.getRowCount() > 0)
							{
								if(resDS.getValueAt(0, 0) != null)
								{
									totalUsers = Float.parseFloat(resDS.getValueAt(0, 0).toString());
								}
							}
							if(usersWithOneScreen == 0 || totalUsers == 0)
							{
								bounceRate = 0;
							}
							else
							{
								bounceRate = (usersWithOneScreen/totalUsers);
							}
					}
					catch (SQLException e) {
					
					e.printStackTrace();
				}
				finally{
					
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getOverviewOnController : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}

				return bounceRate;
			}
			
			
			@Override
			public boolean getIfEnterprise() {
				boolean retVal = false;
				List<MODIAPersistentRecord> results;
				MODIAPersistentRecord record;
				SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
				results = mycontext.getPersistenceInterface().query(query);
				
				if(results != null && results.size() > 0)
				{
					
					record = results.get(0);
					if(record != null)
					{
						retVal = record.getIsEnterprise();
					}
					
				}
				
			return retVal;
			}
			
			@Override
			public int cleanupModuleDB() {
				
				int retVal = 0; 
				Datasource ds;
				String sqlQuery = "";
				
				SRConnection con = null;
			
				
//				log.error("cleanupModuleDB , this.moduleDS : " + this.moduleDS );
				
				//connect to the database and get records
				try{
					ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					con = ds.getConnection();
				
//					try
//					{
//						sqlQuery = "DROP TRIGGER hourly_audit_summary";
//						con.runUpdateQuery(sqlQuery);
//						
//					}
//					catch(Exception e)
//					{
//						log.error("removeModuleFunctions : error dropping trigger hourly_audit_summary" + e);
//					}
//					
//					try
//					{
//						sqlQuery = "DROP TRIGGER hourly_sessions_summary;";
//						con.runUpdateQuery(sqlQuery);
//						
//					}
//					catch(Exception e)
//					{
//						log.error("removeModuleFunctions : error dropping trigger hourly_sessions_summary" + e);
//					}
					try
					{
						sqlQuery = "DROP INDEX OriginatingSystemIndex ON AUDIT_EVENTS;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping OriginatingSystemIndex" + e);
					}
					
					try
					{
						sqlQuery = "DROP INDEX eventidIndex ON alarm_events;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping eventidIndex" + e);
					}
					
					try
					{
						sqlQuery = "DROP INDEX eventtimeIndex ON alarm_events;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping eventtimeIndex" + e);
					}
					
					try
					{
						sqlQuery = "DROP INDEX eventtypeIndex ON alarm_events;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping " + e);
					}
					
					try
					{
						sqlQuery = "DROP PROCEDURE hourly_overview;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure hourly_overview" + e);
					}
					
					try
					{
						sqlQuery = "DROP PROCEDURE hourlyActiveUsersSummary;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure hourlyActiveUsersSummary" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE hourlyActiveUsersSummaryAll;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure hourlyActiveUsersSummaryAll" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE hourlyAlarmsSummary;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure hourlyAlarmsSummary" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE daily_overview;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure daily_overview" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE dailyActiveUsersSummary;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure dailyActiveUsersSummary" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE dailyActiveUsersSummaryAllProjects;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure dailyActiveUsersSummaryAllProjects" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE dailyAlarmsSummary;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure dailyAlarmsSummary" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE dailySessionsSummary;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure dailySessionsSummary" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE monthlyActiveUsersSummary;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure monthlyActiveUsersSummary" + e);
					}
					try
					{
						sqlQuery = "DROP PROCEDURE monthlyActiveUsersSummaryAllProjects;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping procedure monthlyActiveUsersSummaryAllProjects" + e);
					}
								
					try
					{
						sqlQuery = "DROP TABLE mod_ia_audit_events;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_audit_events" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_browser_info;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping [mod_ia_browser_info]" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE MOD_IA_CLIENTS;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping MOD_IA_CLIENTS" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_daily_active_users;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_daily_active_users" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_daily_alarms_summary;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_daily_alarms_summary " + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_daily_overview;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_daily_overview" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_daily_sessions;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_daily_sessions " + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_db_status;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_db_status" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_hourly_active_users;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_hourly_active_users" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_hourly_alarms_counts;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_hourly_alarms_counts" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_hourly_overview;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_hourly_overview " + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_hours;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_hours" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE MOD_IA_LOCATION_INFO;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping MOD_IA_LOCATION_INFO " + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_month;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_month" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_monthly_active_users;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_monthly_active_users" + e);
					}
					
					
					
					try
					{
						sqlQuery = "DROP TABLE MOD_IA_SCREEN_VIEWS;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping MOD_IA_SCREEN_VIEWS " + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_projects;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_projects" + e);
					}
					
					try
					{
						sqlQuery = "DROP TABLE mod_ia_last_sync;";
						con.runUpdateQuery(sqlQuery);
						
					}
					catch(Exception e)
					{
						log.error("removeModuleFunctions : error dropping mod_ia_last_sync" + e);
					}
					
					if(this.isEnterprise && this.isAgent == false)
					{
						
						try
						{
							sqlQuery = "DROP PROCEDURE updateGatewayName;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping procedure updateGatewayName" + e);
						}
						
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_gateways;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_gateways" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_monitored_gateways;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_monitored_gateways" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_users;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_users" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_projects;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_projects" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_monthly_active_users;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_monthly_active_users" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_location_info;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_location_info" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_hourly_overview;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_hourly_overview" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_hourly_alarms_counts;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_hourly_alarms_counts" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_hourly_active_users;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_hourly_active_users" + e);
						}
						
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_daily_sessions;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_daily_sessions" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_daily_screen_views;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_daily_screen_views" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_daily_overview;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_daily_overview" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_daily_alarms_summary;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_daily_alarms_summary" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_daily_alarm_active_counts;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_daily_alarm_active_counts" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_daily_alarm_ack_counts;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_daily_alarm_ack_counts" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_daily_active_users;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_daily_active_users" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_clients;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_clients" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_browser_info;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_browser_info " + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_audit_events;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_audit_events" + e);
						}
						
						try
						{
							sqlQuery = "DROP TABLE mod_ia_aggregates_actions;";
							con.runUpdateQuery(sqlQuery);
							
						}
						catch(Exception e)
						{
							log.error("removeModuleFunctions : error dropping mod_ia_aggregates_actions" + e);
						}
						
						
					}
				}
				catch(Exception e){
					log.error("removeModuleFunctions : " + e);
				}finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("removeModuleFunctions : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
				
				return retVal;
			}	
			
			@Override
			public String getSQLVersion() {
				String retVal = null; 
				Datasource ds;
				String sqlQuery = "";
				
				SRConnection con = null;
				Dataset resDS;
				
				
				//connect to the database and get records
				try{
					ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
					con = ds.getConnection();
				
					sqlQuery = "SELECT @@VERSION;";
					resDS = con.runQuery(sqlQuery);
					
					if(resDS != null && resDS.getRowCount() > 0)
					{
						if(resDS.getValueAt(0, 0) != null)
						{
							retVal = resDS.getValueAt(0,0).toString().trim();
						}
					}
				}
				catch(Exception e){
					log.error("getSQLVersion : " + e);
				}finally{
					//close the database connection 
					if(con!=null){
					try {
						con.close();
					} catch (SQLException e) {
						log.error("getSQLVersion : in con close exception.");
						
						e.printStackTrace();
					}
					
					}
				}
				
				return retVal;		
		}

		@Override
		public List<String> getGatewaysForRename() {
			List<String> retList = new ArrayList<String>(); 
			Datasource ds;
			String sqlQuery = "";
			
			SRConnection con = null;
			Dataset resDS;
			
			
			//connect to the database and get records
			try{
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
			
				sqlQuery = "SELECT GAN_ServerName from mod_ia_gateways where Is_Renamed = 0;";
				resDS = con.runQuery(sqlQuery);
				
				if(resDS != null && resDS.getRowCount() > 0)
				{
					int rowCount = resDS.getRowCount();
					for(int i=0; i<rowCount; i++)
					{
						if(resDS.getValueAt(i, 0) != null)
						{
							retList.add(resDS.getValueAt(i, 0).toString());
						}
					}
				}
			}
			catch(Exception e){
				log.error("getGatewaysForRename : " + e);
			}finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("getGatewaysForRename : in con close exception.");
				}
				
				}
			}
			
			return retList;	
		}

		@Override
		public int updateNewGatewayName(String oldName, String newName) {
			// TODO Auto-generated method stub
			
			log.error("called updateNewGatewayName with oldname : " + oldName + ", newname : " + newName);
			Datasource ds;
			String sqlQuery = "";
			
			SRConnection con = null;
			int result = -1;
			
			
			//connect to the database and get records
			try{
				ds = mycontext.getDatasourceManager().getDatasource(this.moduleDS);
				con = ds.getConnection();
				
				sqlQuery = "CALL updateGatewayName('" + oldName + "','" + newName + "');";
				result = con.runUpdateQuery(sqlQuery);
				log.error("updateNewGatewayName : result of proc call : " + result);
			
			}
			catch(Exception e){
				log.error("updateNewGatewayName : " + e);
			}finally{
				//close the database connection 
				if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					log.error("updateNewGatewayName : in con close exception.");
				}
				
				}
			}
			return result;
		}	
}



