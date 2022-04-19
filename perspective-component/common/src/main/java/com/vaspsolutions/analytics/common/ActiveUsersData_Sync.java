package com.vaspsolutions.analytics.common;

import java.io.Serializable;

import com.inductiveautomation.ignition.common.Dataset;

public class ActiveUsersData_Sync implements Serializable{
	private static final long serialVersionUID = 1L;

	public String agentName;
	
	public Dataset hourlyActiveUsers; //from mod_ia_hourly_active_users
	public Dataset dailyActiveUsers; //from mod_ia_daily_active_users
	public Dataset monthlyActiveUsers; //from mod_ia_monthly_active_users
}
