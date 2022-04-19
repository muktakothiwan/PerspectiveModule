package com.vaspsolutions.analytics.gateway.services;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inductiveautomation.ignition.common.Dataset;
import com.vaspsolutions.analytics.common.ActiveUsersData_Sync;
import com.vaspsolutions.analytics.common.AlarmsSummary_Sync;
import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.OverviewInformation;
import com.vaspsolutions.analytics.common.Projects_Sync;
import com.vaspsolutions.analytics.common.RealTimeData;
import com.vaspsolutions.analytics.common.SummaryOnScreenViewChange_Sync;
import com.vaspsolutions.analytics.common.UserSyncRecord;

public interface GetAnalyticsInformationService {

	//functions for direct retrieval of data for a particular gateway , particular project
	public CurrentOverview getCurrentOverview();
	public HashMap<String,Integer> getNumberOfUsersPerScreenRealTime();
	

	//functions for scheduled tasks 
	public Dataset getAlarmsHourlyOverview(String alarmDate, int hourNo);
	public Dataset getAlarmsDailyOverview(String alarmDate);
	public OverviewInformation getDailyOverview(int duration, String projectName, boolean allProjects);
	
	public Dataset getDailySessions(String dailyDate);
	public Dataset getModIAClients(String dailyDate);
	public Dataset getModIABrowserInfo(String dailyDate);
	public Dataset getModIALocationInfo();
	
	public RealTimeData getRealTimeData(String projectName, boolean allProjects);
	public Dataset getRealTimeGraphDataToday(String projectName, boolean allProjects, int currentDurationA);
	public Dataset getRealTimeGraphDataYesterday(String projectName, boolean allProjects, int currentDurationB);
	
	/*
	 * Services on Controller for data sync
	 */
	public boolean receiveSummaryOnScreenViewChange(SummaryOnScreenViewChange_Sync summary);
	public boolean receiveAlarmsSummary(AlarmsSummary_Sync aSummary);
	public boolean receiveActiveUsersSummary(ActiveUsersData_Sync auSummary);
	public boolean receiveBrowsers(String agentID, Dataset browserData);
	public boolean receiveProjects(int operation, String gatewayID, List<Projects_Sync> projects);
	
	//By Yogini on 16-Jan-2017 for users panel changes.
	public boolean receiveUsersStatus(String agentID, List<UserSyncRecord> users);
	
	//By Yogini on 18-Jan-2017 for alarm counts
	public boolean receiveAlarmsAckCounts(String agentID, Date date, Dataset ackAlarmCounts);
	public boolean receiveAlarmsActiveCounts(String agentID, Date date, Dataset activeAlarmCounts);
	public boolean receiveLocationInfo(String agentID, Dataset locationInfo);
	
	//By Yogini 08-Feb-2017 for audit_events sync
	public boolean receiveAuditActions(String agentID, Dataset auditActions);
	
	//By Yogini 06-March-2017 to sync mod_ia_audit_events for gateway uptime
	public boolean receiveModIAAuditEvents(String agentID, Dataset modIAAudit);
	
	
}
