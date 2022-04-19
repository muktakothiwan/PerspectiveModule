package com.vaspsolutions.analytics.common;


import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.user.User;
import com.inductiveautomation.metro.api.ServerId;
/**
 * INterface that defines all the methods that are exposed at Gateway to be called from client
 * @author Sumedh
 *
 */
public interface ModuleRPC {
	
	
	//method to set DS name in case user changes property from designer
	public void setModuleDS(String moduleDS);

	void executeTasksOnce();
	//method called at the initial module installation to create our own tables for storing historical data
	boolean createAndPopulateAuditDB(Long newDS, boolean isAgent); //this is the function that gets called once per gateway to create our module tables and populate initial data
	
	
	//Following methods are used to store and retrieve module configuration
	void createPersistenceRecord(String dsName, String oldDS, String audit, String alarm); //this is used to store the module configuration information
	String getPersistenceRecord(); //retrieve the module configuration information

	boolean getIfAgent();
	Long getControllerId();
	
	//methods to store and access other client information such as OS, Mobile Flags etc.
	void storeClientInformation(ClientRecord record) throws ModIAConfigurationException;
	DevicesInformation getDeviceInformation(int duration, String projectName, boolean allProjects);
	Dataset getTopOperatingSystems(int duration, String projectName, boolean allProjects);
	
	//get data for browser graph to be shown on real time panel
	public Dataset getBrowserInformation(int duration, String projectName, boolean allProjects);	
		
	//method to retrieve users information - called from Users Panel, Real Time Panel
	UsersOverviewInformation getUserInformation( int duration, String uName, String projectName, boolean allProjects, String userAuthProfile);
	
	//method to retrieve Dashboard - overview section content for a given duration
	OverviewInformation getOverview ( int duration, String projectName, boolean allProjects);
	
	//method to retrieve Dashboard - yesterday overview section content for a given duration
		OverviewInformation getYesterdayOverview (int duration, String projectName, boolean allProjects);
		
	//method to retrieve real time overview information
	CurrentOverview getCurrentOverview(String projectName, boolean allProjects);
		
	//method to retrieve all alarms information to be shown on real time panel
	AlarmsInformation getAlarmsOverview( int duration, String projectName, boolean allProjects);

	
	//retrieve historical information for the duration
	
	
	
	int getNumberOfActiveUsers(int duration,  String projectName, boolean allProjects); //get the number of active users for given duration.
	
	
	//Methods to retrieve various alarms information
	Dataset getActiveAlarmsCount( String projectName, boolean allProjects);
	Dataset getAckAlarmsCount(String projectName, boolean allProjects);
	Dataset getAlarms(int duration,  String projectName, boolean allProjects); //get number of alarms per priority level for specified duration.
	//per priority
	Dataset getAlarmsClearTime(int duration,  String projectName, boolean allProjects); //get time to clear alarms per priority level for specified duration.
	Dataset getAlarmsAckTime(int duration,  String projectName, boolean allProjects); //get time to ack alarms per priority level for specified duration.
	//Average time irrespective of priority
	String getAverageClearTime(int duration,  String projectName, boolean allProjects);
	String getAverageAckTime(int duration,  String projectName, boolean allProjects);
	
	//internal calls for users summary information
	Dataset getDaysSinceLastLoginPerUser( String projectName, boolean allProjects, int duration); //get the number of days since user logged in
	List<UserSessionsInfo> getActiveSessions(String projectName, boolean allProjects); //get start time for each active session and calculate length
	int getNumActionsByCurrentUsers( String projectName, boolean allProjects, HashMap<String, Date> users);
	UsersCount getNumberofNewAndReturningUsers(String projectName, boolean allProjects); //get new and returning users
	Collection<User> getUserProfiles();					//get profile information 
	String getIPAddress( String userName);	// get IP address from audit_events
	int getTotalVisits(String userName, int duration, String projectName, boolean allProjects, String authProfile);		// get the no of total logins for the user in given duration
	
	List<String> getAllUsers(); //get no of configured users.
	int getNumberOfNewUsers(int duration,  String projectName, boolean allProjects);
	
		
	//method to store and retrieve screens viewed information in/from module database
	void storeScreenViewedInformation(ScreenViewsRecord screen) throws ModIAConfigurationException;
	List<ScreensCount> getScreensViewedCounts( int duration, String projectName, boolean allProjects);
	float getBounceRate( int duration, String projectName, boolean allProjects);
	//Dataset getScreenViewsPerUserPerVisit( int duration, String projectName, boolean allProjects);
	HashMap<String,Integer> getNumberOfUsersPerScreenRealTime( String projectName, boolean allProjects); 
	
	List<ScreensCount> getNumberOfScreensViewedCurrentUsers(String projectName, boolean allProjects);
			
	//methods to retrieve upper panel plot data for selected duration
	Dataset getTotalVisitsData( String projectName, boolean allProjects, int duration);
	Dataset getTotalUsersData( String projectName, boolean allProjects, int duration);
	Dataset getTotalScreenViewsData( String projectName, boolean allProjects, int duration);
	Dataset getBounceRateData( String projectName, boolean allProjects, int duration);
	Dataset getAvgSessionData( String projectName, boolean allProjects, int duration);
	Dataset getAvgScreenViewsData( String projectName, boolean allProjects, int duration);
	
	//method to retrieve frequency information in Dashboard 
	Dataset getFrequencyInformation( String projectName, boolean allProjects, int duration);
	
	//method to retrieve engagement information in Dashboard from summary tables.
	Dataset getEngagementInformation(String projectName, boolean allProjects, int duration);
	
	//method to retrieve active users information in Dashboard from summary tables.
	Dataset getActiveUsersInformation( String projectName, boolean allProjects, int duration);
	ActiveUsersInfo getActiveUsersCounts( String projectName, boolean allProjects, int duration);
	
	

	
	
	
	
	//called from Projects Panel
	Dataset getProjectDetails(int duration, String projectName);
	void deleteProjectFromGateway(String projectName);
	void addProjectsToModule(List<String> projectNames);
	//Function to handle projects monitored by Ignition Analytics module.
		String[] getProjects(String projectName);
		String[] deleteAndGetUpdatedProjectsList(String projectName);
		String[] getProjectNotAddedRoIgnitionAnalytics();
	
	//omkar
	public Dataset getScreenViewsPerUserPerVisitNew(int duration, String projectName, boolean allProjects,String selectedUser, String selUserProfile);
	
	//Following methods are used for REPORTS section 
	
		//1. get data by Top Screens
		Dataset getTopScreens( int duration, String projectName, boolean allProjects);
		//2. method to get summary of all alarms to show name(display path), count, avg clear time , avg ack time in given duration
		Dataset getAlarmsSummary(int duration, String dataSource);
		//3. get OverviewByDate
		Dataset reoprtsGetOverviewByDate( int duration, String projectName, boolean allProjects);
		//4. Get Platform report. OMKAR
		Dataset getPlatformReport( int duration, String projectName, boolean allProjects);
		
		//5. get Bounce rate 
		Dataset getBounceRateReportByDate( int duration, String projectName, boolean allProjects);
		
		//6. get Device types data 
		Dataset getDeviceTypeReport( int duration, String projectName, boolean allProjects);
		//7 engagement report
		public Dataset getEngagementInformationScreenDepth( String projectName,boolean allProjects, int duration);
		public Dataset getEngagementReportInformationScreenDepth( String projectName,boolean allProjects, int duration);
		//8. Recency report
		public Dataset getRecencytReportInformation( String projectName,boolean allProjects, int duration);
		//9. frequency report
		public Dataset getFrequencytReportInformation( String projectName,boolean allProjects, int duration);
		//10. visit duration report
		public Dataset getVisitDurationReportInformation( String projectName,boolean allProjects, int duration);
		//11. Actions Per visit report
		public Dataset getActionsPerVisitReportInformation( String projectName,boolean allProjects, int duration);
		
		//12. Browsers report
		Dataset getBrowserReport( int duration, String projectName, boolean allProjects);
		
		//13. Screen Resolution report
		public Dataset getScreenResolutionData( String projectName,boolean allProjects, int duration);
		
		//14. Cities report
		public Dataset getCitiesReportData( String projectName,boolean allProjects, int duration);
		
		//15. Groups report
		public List<GroupReportRecord> getGroupsReportData( String projectName,boolean allProjects, int duration);
				
		//in users panel
		public boolean checkUserOnlineOrOffline( String projectName, boolean allProjects,String userName, String profileName);
		
		//Alarm summary report
		public Dataset getAlarmsSummaryReport( String projectName,boolean allProjects, int duration);
		public String getLastSeen(String userName, String projectName, boolean allProjects, String userProfile);
		Dataset getActiveUserDataReportGraph(String datasource, int duration, String projectName, boolean allProjects);
		Dataset getSevenDaysMaxMin(String datasource, int duration, String projectName, boolean allProjects);
		
		//Function to retrieve errors.
		Dataset getErrorsInformation(String projectName,boolean allProjects, int duration);
		
		//By Omkar on 29-Jan-2016.
		public OverviewInformation getYesterdayOverviewForSlider( int duration,
				String projectName, boolean allProjects);
		
		//By Yogini on 05-Feb-2016 - to retrieve location, devices and browser information on realtime panel only for logged in users.
		
		
		public LocationDeviceBrowserCounts getRealTimeLocationsDevicesAndBrowsers(String projectName, boolean allProjects,  HashMap<String, Date> users);
		
		//By Yogini 07-March-2016 Get only logged in users to be shown on Users Panel - Users list
		
		public Dataset getAllLoggedInUsers(boolean allProjects, String projectName);
		
		//By Omkar fpr new alarm summary report ring chart
		public Dataset alarmSummaryReportRingChart(String projectName,boolean allProjects, int duration);
		public Dataset getAlarmCountsPerDuration(String projectName,boolean allProjects, int duration);
		public Dataset getTop10AlarmsByDuration(String projectName,boolean allProjects, int duration);
		
		//Yogini -- made this visible to clients so that we don't unnecessarily call web service to get location information
		public boolean IfLocationExists(String locationIntIP, String locationExtIP);
		
		//
		//Yogini 27-04-2016 . Added a function to query no of distinct users that have used browsers in a particular date range.
		int getDistinctUsersFromBrowsers( int duration, String projectName, boolean allProjects);
		
		
		
		//Functions below are for Enterprise Module Version
		//

		
		public boolean storeGANServersList(List<ServerId> serverIds);
		
		public String[]  getProjectsOnGateway(String gatewayName,String projectName);
		public boolean updateGANServer(String serverId, String serverName, String ServerState);
		
		
		
		public Dataset retrieveHourlyAlarmSummary(String alarmDate, int hourNo);
		public Dataset retrieveDailyAlarmSummary(String alarmDate);

		public Dataset retrieveDailySessions(String sessionDate);
		public Dataset retrieveModIAClient(String sessionDate);
		public Dataset retrieveModIABrowsers(String sessionDate);
		public Dataset retrieveModIALocations();
		public Dataset retrieveModIAScreenViews(String sessionDate);
		public RealTimeData retrieveRealTimeData(String projectName, boolean allProjects);
		
		public boolean storeClientsInformation(Dataset clientData);
		
		
		//following services would reside on Controller to receive data from Agents
		
		public boolean receiveSummaryOnScreenViewChange(SummaryOnScreenViewChange_Sync summary);
		public boolean receiveAlarmsSummary(AlarmsSummary_Sync aSummary);
		public boolean receiveBrowsers(String agentID, Dataset browserData);
		public boolean receiveActiveUsersSummary(ActiveUsersData_Sync auSummary);
		public boolean receiveProjects(int operation, String gatewayID , List<Projects_Sync> projects);
		public boolean receiveAuditActions(String agentID, Dataset auditActions);
		public boolean receiveUsersStatus(String agentID, List<UserSyncRecord> users);
		public boolean receiveAlarmsAckCounts(String agentID, Date date, Dataset ackAlarmCounts);
		public boolean receiveAlarmsActiveCounts(String agentID, Date date, Dataset activeAlarmCounts);
		public boolean receiveLocationInfo(String agentID, Dataset locationInfo);
		//By Yogini 06-March-2017 to sync mod_ia_audit_events for gateway uptime
		public boolean receiveModIAAuditEvents(String agentID, Dataset modIAAudit);

		//following functions would reside on Agents that would be called from scheduled tasks to call services from Controller and send data
		
		public boolean sendSummaryOnScreenViewChange();
		public boolean sendAlarmsSummary();
		public boolean sendBrowsers();
		public boolean sendActiveUsers();
		public boolean sendUsersStatus();
		public boolean sendAlarmsAckCounts();
		public boolean sendAlarmsActiveCounts();
		public boolean sendLocationInfo();
		public boolean sendAuditActions();
		public boolean sendModIAAuditEvents();
		
		//dashboard data population methods
		//method to retrieve Dashboard - overview section content for a given duration
		public OverviewInformation getOverviewOnController ( int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects);
		int getNumberOfActiveUsersOnController(int duration,  String GatewayName, String projectName,  boolean allGateways, boolean allProjects); //get the number of active users for given duration.
		public List<ScreensCount> getScreensViewedCountsOnController( int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects);
		public OverviewInformation getYesterdayOverviewForSliderOnController( int duration, String gatewayName, String projectName, boolean allGateways, boolean allProjects);
		public Dataset getActiveUsersInformationOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);
		public ActiveUsersInfo getActiveUsersCountsOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);
		public Dataset getAlarmsOnController(int duration,  String gatewayName, boolean allGateways); //get number of alarms per priority level for specified duration.
		//per priority
		
		//freq 
		public Dataset getFrequencyInformationOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);	
		public Dataset getDaysSinceLastLoginPerUserOnController( String projectName, boolean allProjects,  String gatewayName, boolean allGateways, int duration); //get the number of days since user logged in
		//method to retrieve engagement information in Dashboard from summary tables.
		public Dataset getEngagementInformationOnController(String projectName, boolean allProjects,String gatewayName, boolean allGateways, int duration);
		public Dataset getEngagementInformationScreenDepthOnController( String projectName,boolean allProjects, String gatewayName, boolean allGateways, int duration);
		
		//method to retrieve Dashboard - yesterday overview section content for a given duration
		public OverviewInformation getYesterdayOverviewOnController (int duration, String projectName, boolean allProjects, String gatewayName, boolean allGateways);
		
		public DevicesInformation getDeviceInformationOnController(int duration, String projectName, boolean allProjects, String gatewayName, boolean allGateways);
		public Dataset getTopOperatingSystemsOnController(int duration, String projectName, boolean allProjects, String gatewayName, boolean allGateways);
		//get data for browser graph to be shown on real time panel
		public Dataset getBrowserInformationOnController(int duration, String projectName, boolean allProjects, String  gatewayName, boolean allGateways);
		
		//methods to retrieve dashboard upper panel plot data for selected duration on Controller
		Dataset getTotalVisitsDataOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);
		Dataset getTotalUsersDataOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);
		Dataset getTotalScreenViewsDataOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);
		Dataset getBounceRateDataOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);
		Dataset getAvgSessionDataOnController( String projectName, boolean allProjects,String gatewayName, boolean allGateways,  int duration);
		Dataset getAvgScreenViewsDataOnController( String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);
		
		//method to persist last sync time which would be called periodically
		
		public void persisteLastSynchTimes();
		
		//Added by Yogini on 27-12-2016
		//methods from Project Panel 
		//for add remove gateways
		public String[] getGateways();
		public String[] deleteAndGetUpdatedGatewaysList(String gatewayName);
		public String[] getGatewaysNotAddedToIgnitionAnalytics();
		public void addGatewaysToModule(List<String> gatewayNames);
		public Dataset getGatewayDetails(int duration, String gatewayName, Boolean allGateways);
		public Dataset getProjectDetailsPerGateway(int duration, String gatewayName, String projectName);
		public String[] getProjectsNotAddedFromAgent(String gatewayName) ;
		void addProjectsToAgent(String gatewayName, List<String> projectsToAdd) throws MODIAServiceUnavailableException;
		String[] deleteProjectsFromAgent(String gatewayName, String projectToDelete) throws MODIAServiceUnavailableException;
		
		
		
		
	
		
		//By Omkar on 26 Dec for controller (Real time panel)
		Dataset getSevenDaysMaxMinForController(String datasource, int duration, String projectName, boolean allProjects,String gateWayName,boolean allGateways);
		int getNumberOfNewUsersOnController(int duration,  String projectName, boolean allProjects,String gateWayName, boolean allGateWays);
		AlarmsInformation getAlarmsOverviewOnController( int duration, String projectName, boolean allProjects,String currentGateway, boolean allGateways);
		UsersOverviewInformation getUserInformationOnController( int duration, String uName, String projectName, boolean allProjects, String userAuthProfile,String currenGateway, boolean allGateways);
		
		
		//By Yogini 25-January-2017 Get only logged in users to be shown on Users Panel - Users list
		public Dataset getAllLoggedInUsersOnController(boolean allProjects, String projectName, boolean allGateways, String gatewayName);
		public Dataset getScreenViewsPerUserPerVisitOnController(int duration, String projectName, boolean allProjects,String selectedUser, String selectedUserProfile, String gatewayName, boolean showActions);
		
		
		
		//By Sayali for Reports
		// 1. get OverviewByDate Sayali
		Dataset reportsGetOverviewByDateController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects);
		public Dataset getCitiesReportDataController(String GatewayName, String projectName, boolean allGateways, boolean allProjects, int duration);
		public List<GroupReportRecord> getGroupsReportDataController(String GatewayName, String projectName, boolean allGateways,
				boolean allProjects, int duration) ;
		Dataset getTopProjectsController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects);
		Dataset getTopScreensController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects);
		Dataset getBounceRateReportByDateController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects);
		Dataset getDeviceTypeReportController(int duration, String GatewayName,	String projectName, Boolean allGateways, boolean allProjects);
		Dataset getPlatformReportController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects);
		int getDistinctUsersFromBrowsersController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects);
		Dataset getBrowserReportController(int duration, String GatewayName, String projectName, Boolean allGateways, boolean allProjects);
		public Dataset getScreenResolutionDataController(String GatewayName, String projectName, boolean allGateways, boolean allProjects, int duration);
		public Dataset getActionsPerVisitReportInformationController(String GatewayName, String projectName, boolean allGateways, boolean allProjects, int duration);
		public Dataset getVisitDurationReportInformationController(String GatewayName, String projectName, boolean allGateways, boolean allProjects, int duration);
		
		public Dataset getEngagementReportInformationScreenDepthController(
				String GatewayName, String projectName, boolean allGateways, boolean allProjects, int duration);
		public Dataset getFrequencyReportInformationController(String GatewayName,	String projectName, boolean allGateways, boolean allProjects, int duration);
		public Dataset getRecencytReportInformationController(String GatewayName,
				String projectName, boolean allGateways, boolean allProjects, int duration);
		public Dataset getActiveUserDataReportGraphController(String datasource,
				int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects);
		public Dataset getAlarmsSummaryReportController(String GatewayName, String projectName, boolean allGateways, boolean allProjects, int duration);
		//get time to clear alarms per priority level for specified duration.on controller by sayali
		Dataset getAlarmsClearTimeController(int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects);
			
		//get time to ack alarms per priority level for specified duration. by sayali on 18th jan 2017
		public Dataset getAlarmsAckTimeController(int duration, String GatewayName, String projectName, boolean allGateways, boolean allProjects);
		
		
		//Added by Omkar on 5 Jan 2017
		
		CurrentOverview getRealTimeAllGateWayOverview(String gateWayName,String currentProject, boolean allProjects) ;
		
		///Added by Yogini for RealTime 17-Feb-2017
		Dataset getActiveAlarmsCountOnController( String gatewayName, boolean allgateways);
		Dataset getAckAlarmsCountOnController(String gatewayName, boolean allgateways);
		public Dataset getAlarmsClearTimeOnController(int duration,  String gatewayName, boolean allgateways); //get time to clear alarms per priority level for specified duration.
		
		
		public HashMap<String,HashMap<String,Integer>> getNumberOfUsersPerScreenRealTimeOnController(String gatewayName, boolean allGateways, String projectName, boolean allProjects) ;
		
		
		
		//By Yogini 08-March-2017 
		public boolean isGatewayMonitored(String gatewayName);
		
		//sayali
		public Dataset getTop10AlarmsByDurationController(String projectName, boolean allProjects, String gatewayName, boolean allGateways, int duration);	
		public Dataset alarmSummaryReportRingChartController(String projectName,boolean allProjects, String GatewayName, boolean allGateways, int duration);
		public Dataset getAlarmCountsPerDurationController(String projectName,boolean allProjects, String GatewayName, boolean allGateways, int duration);
		public float getBounceRateController( int duration,String gatewayName, String projectName, boolean allGateways, boolean allProjects);
		
		//Yogini added for cleanup.
		
		int cleanupModuleDB();
		
		//Added by Yogini on 29-June-2017 to call from Analysis Panel to query SQL Server version to determine module compatibility 
		public String getSQLVersion();
		
		//Yogini Added on July 17 for combning Standalone and Enterprise module
		boolean getIfEnterprise();
		
		//Yogini 13-Sept-2017 , for rename gateway functionality
		public List<String> getGatewaysForRename();
		public int updateNewGatewayName(String oldName, String newName);
}
	