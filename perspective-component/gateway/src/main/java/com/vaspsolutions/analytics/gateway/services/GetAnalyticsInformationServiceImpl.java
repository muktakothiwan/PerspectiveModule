package com.vaspsolutions.analytics.gateway.services;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ch.qos.logback.classic.Logger;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.vaspsolutions.analytics.common.ActiveUsersData_Sync;
import com.vaspsolutions.analytics.common.AlarmsSummary_Sync;
import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.OverviewInformation;
import com.vaspsolutions.analytics.common.Projects_Sync;
import com.vaspsolutions.analytics.common.RealTimeData;
import com.vaspsolutions.analytics.common.SummaryOnScreenViewChange_Sync;
import com.vaspsolutions.analytics.common.UserSyncRecord;
import com.vaspsolutions.analytics.gateway.ModuleRPCImpl;

public class GetAnalyticsInformationServiceImpl implements GetAnalyticsInformationService {
	
	GatewayContext context;
	ModuleRPCImpl rpc;
	
	public GetAnalyticsInformationServiceImpl(GatewayContext context) {
		super();
		this.context = context;
		rpc = new ModuleRPCImpl(this.context);
	}


	@Override
	public CurrentOverview getCurrentOverview() {
	
		CurrentOverview _view = null;
		
		_view = rpc.getCurrentOverview("", true);
		
		return _view;
		
	}


	@Override
	public HashMap<String, Integer> getNumberOfUsersPerScreenRealTime() {
	
		return rpc.getNumberOfUsersPerScreenRealTime("", true);
	}


	
	/****************************************************************************************************
	 * All the following functions are called from Scheduled task on Controller for data aggregation across agent gateways
	 * 
	 */
	
	//sync data from hourly_alarms_summary table
	@Override
	public Dataset getAlarmsHourlyOverview(String alarmDate, int hourNo) {
		return rpc.retrieveHourlyAlarmSummary(alarmDate, hourNo);
	}

	//sync data from daily_alarms_summary table

	@Override
	public Dataset getAlarmsDailyOverview(String alarmDate) {
		return rpc.retrieveDailyAlarmSummary(alarmDate);
	}


	/*
	 * Function to send daily overview data for all projects from the scheduled summarization task 
	 * (non-Javadoc)
	 * @see com.vaspsolutions.analytics.gateway.services.GetAnalyticsInformationService#getDailyOverview(java.lang.String)
	 */
	
	@Override
	public OverviewInformation getDailyOverview(int duration, String projectName, boolean allProjects) {
		
		return rpc.getOverview(duration, projectName, allProjects);
		
	}

	/*
	 * Function to sync daily_sessions table
	 */
	@Override
	public Dataset getDailySessions(String dailyDate) {
		return rpc.retrieveDailySessions(dailyDate);
	}


	/*
	 * Function to sync mod_ia_Clients table
	 * 
	 */
	@Override
	public Dataset getModIAClients(String dailyDate) {
		return rpc.retrieveModIAClient(dailyDate);
	}


	/*
	 * Function to sync mod_ia_browser_info table
	 */
	@Override
	public Dataset getModIABrowserInfo(String dailyDate) {
		return rpc.retrieveModIABrowsers(dailyDate);
	}



	/*
	 * Function to sync mod_ia_location_info table
	 */
	@Override
	public Dataset getModIALocationInfo() {
		return rpc.retrieveModIALocations();
	}


	@Override
	public RealTimeData getRealTimeData(String projectName, boolean allProjects) {
		return rpc.retrieveRealTimeData(projectName, allProjects);
	}


	@Override
	public Dataset getRealTimeGraphDataToday(String projectName,
			boolean allProjects, int currentDurationA) {
		return rpc.getTotalUsersData(projectName, allProjects, currentDurationA);
	}


	@Override
	public Dataset getRealTimeGraphDataYesterday(String projectName,
			boolean allProjects, int currentDurationB) {
		return rpc.getTotalUsersData(projectName, allProjects, currentDurationB);
	}


	@Override
	public boolean receiveSummaryOnScreenViewChange(
			SummaryOnScreenViewChange_Sync summary) {
		// TODO Auto-generated method stub
		
		
		return rpc.receiveSummaryOnScreenViewChange(summary);
	}


	@Override
	public boolean receiveAlarmsSummary(AlarmsSummary_Sync aSummary) {
		return rpc.receiveAlarmsSummary(aSummary);
	}


	@Override
	public boolean receiveActiveUsersSummary(ActiveUsersData_Sync auSummary) {
		return rpc.receiveActiveUsersSummary(auSummary);
	}


	@Override
	public boolean receiveBrowsers(String agentID, Dataset browserData) {
		return rpc.receiveBrowsers(agentID, browserData);
	}


	@Override
	public boolean receiveProjects(int operation, String gatewayID,List<Projects_Sync> projects) {
		return rpc.receiveProjects(operation,  gatewayID,projects);
	}


	//By Yogini on 16-Jan-2017 for users panel changes.
	@Override
	public boolean receiveUsersStatus(String agentID, List<UserSyncRecord> users) {
		
		return rpc.receiveUsersStatus(agentID, users);
	}


	@Override
	public boolean receiveAlarmsAckCounts(String agentID, Date date, Dataset ackAlarmCounts) {
		// TODO Auto-generated method stub
		return rpc.receiveAlarmsAckCounts(agentID, date, ackAlarmCounts);
	}


	@Override
	public boolean receiveAlarmsActiveCounts(String agentID, Date date, Dataset activeAlarmCounts) {
		// TODO Auto-generated method stub
		return rpc.receiveAlarmsActiveCounts(agentID, date, activeAlarmCounts);
	}


	@Override
	public boolean receiveLocationInfo(String agentID, Dataset locationInfo) {
		// TODO Auto-generated method stub
		return rpc.receiveLocationInfo(agentID, locationInfo);
	}


	@Override
	public boolean receiveAuditActions(String agentID, Dataset auditActions) {
		// TODO Auto-generated method stub
		return rpc.receiveAuditActions(agentID, auditActions);
	}


	@Override
	public boolean receiveModIAAuditEvents(String agentID, Dataset modIAAudit) {
		// TODO Auto-generated method stub
		return rpc.receiveModIAAuditEvents(agentID, modIAAudit);
	}


	
	
	
	
	

	
	
	
	
	
	
	
	
}
