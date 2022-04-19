package com.vaspsolutions.analytics.gateway.services;

import java.util.HashMap;
import java.util.List;

import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.GroupReportRecord;


public interface AgentService {

	String[] getProjectNotAddedOnAgent();
	void addProjectsToAgent(List<String> projectsToAdd);
	String[] deleteProjectsFromAgent(String projectToDelete);
	//Added by Sayali
	List<GroupReportRecord> getGroupsReportInformation(String projectName, boolean allProjects, int duration);
	
	//by Omkar for Real Time Panel
	
	public CurrentOverview getCurrentOverViewOnAgent(String projectName,boolean allProjects);
	//Yogini for real time panel - screen views
	public HashMap<String,Integer> getNumberOfUsersPerScreenRealTimeFromAgent(String projectName, boolean allProjects);
	
	//When gateway is added / removed , agent to start / stop sending data.
	public boolean stopDataSync();
	public boolean startDataSync();
}
