package com.vaspsolutions.analytics.common;
import java.io.Serializable;

import com.inductiveautomation.ignition.common.Dataset;

public class SummaryOnScreenViewChange_Sync implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String agentName;
	
	public Dataset hourlyOverview; //from mod_ia_hourly_overview
	public Dataset dailyOverview; //from mod_ia_daily_overview
	public Dataset dailySessions; //from mod_ia_daily_sessions
	public Dataset clients;	//from mod_ia_clients
	public Dataset screenViews; //from mod_ia_screen_views
}
