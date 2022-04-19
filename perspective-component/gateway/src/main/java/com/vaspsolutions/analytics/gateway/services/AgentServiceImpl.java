package com.vaspsolutions.analytics.gateway.services;

import java.util.HashMap;
import java.util.List;

import org.jfree.util.Log;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.GroupReportRecord;
import com.vaspsolutions.analytics.gateway.GANAuditSyncTask;
import com.vaspsolutions.analytics.gateway.ModuleRPCImpl;

public class AgentServiceImpl implements AgentService {
	GatewayContext context;
	ModuleRPCImpl rpc;
	
	
	public AgentServiceImpl(GatewayContext context) {
		super();
		this.context = context;
		rpc = new ModuleRPCImpl(this.context);
	}

	@Override
	public String[] getProjectNotAddedOnAgent() {
		Log.error("getProjectNotAddedOnAgent : Agent Service called.");
		return rpc.getProjectNotAddedRoIgnitionAnalytics();
	}

	@Override
	public void addProjectsToAgent(List<String> projectsToAdd) {
		Log.error("addProjectsToAgent : Agent Service called.");
		rpc.addProjectsToModule(projectsToAdd);
	}

	@Override
	public String[] deleteProjectsFromAgent(String projectToDelete) {
		Log.error("deleteProjectsFromAgent : Agent Service called.");
		return rpc.deleteAndGetUpdatedProjectsList(projectToDelete);
	}

	// By Sayali
	public List<GroupReportRecord> getGroupsReportInformation( String projectName, boolean allProjects, int duration) {
		Log.error("getGroupsReportInformation : Agent Service called.");
		return rpc.getGroupsReportData( projectName, allProjects, duration);
	}
	
	//Created by Omkar on 4 Jan 2017
		@Override
		public CurrentOverview getCurrentOverViewOnAgent(String projectName,boolean allProjects){
			Log.error("getCurrentOverViewOnAgent : Agent Service called.");
			return rpc.getCurrentOverview(projectName, allProjects);
		}

		@Override
		public HashMap<String, Integer> getNumberOfUsersPerScreenRealTimeFromAgent(String projectName,
				boolean allProjects) {
			Log.error("getNumberOfUsersPerScreenRealTimeFromAgent : Agent Service called.");
			return rpc.getNumberOfUsersPerScreenRealTime(projectName, allProjects);
		}

		@Override
		public boolean stopDataSync() {
			boolean retVal = true;
			try
			{
				this.context.getExecutionManager().unRegister("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_GANAUDITSYNC");
			}
			catch(Exception e)
			{
				retVal = false;
				Log.error("stopDataSync : " + e);
			}
			return retVal;
		}
		
		@Override
		public boolean startDataSync() {
			boolean retVal = true;
			try
			{
				this.context.getExecutionManager().register("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_GANAUDITSYNC", new GANAuditSyncTask(this.context),60000);
			}
			catch(Exception e)
			{
				retVal = false;
				Log.error("startDataSync : " + e);
			}
			return retVal;
		}
}
