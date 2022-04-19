package com.vaspsolutions.analytics.common;

import java.io.Serializable;

import com.inductiveautomation.ignition.common.Dataset;

public class AlarmsSummary_Sync implements Serializable {
	private static final long serialVersionUID = 1L;

	public String agentName;
	
	public Dataset hourlyAlarmCounts; // from table mod_ia_hourly_alarms_counts
	public Dataset dailyAlarmsSummary; // from table mod_ia_daily_alarms_summary
	
}
