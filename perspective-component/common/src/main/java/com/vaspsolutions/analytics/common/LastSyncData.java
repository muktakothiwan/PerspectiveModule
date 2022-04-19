package com.vaspsolutions.analytics.common;
import java.sql.SQLException;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.gateway.datasource.Datasource;
/*
 * Singleton class that is used to store last sync time stamp for each of the tables from Agent
 * 
 */
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.util.DBUtilities;

public class LastSyncData {
	
	private static LastSyncData instance = null;
	private String last_sync_date = null;
	private String last_sync_hour = null;
	private String last_sync_sessions_timestamp = null;
	

	private String last_sync_screen_timestamp = null;
	private String last_synch_client_start_timestamp = null;
	
	//for alarms
	private String last_sync_daily_alarms_date = null;
	private String last_sync_hourly_alarms_date = null;
	private String last_sync_hourly_alarms_hour = null;
	
	//for browsers
	private String last_sync_browsers_timestamp = null;
	
	//for active users
	private String last_sync_hourly_aUsers_date = null;
	private String last_sync_hourly_aUsers_hour = null;
	private String last_sync_daily_aUsers_date = null;
	private String last_sync_monthly_aUsers_month = null;
	private String last_sync_monthly_aUsers_year = null;
	
	//for location info
	private int last_sync_locationID = -1;
	
	//for audit actions
	
	private String last_sync_audit_date = null;
	private String last_sync_mod_ia_audit = null;
	public static LastSyncData getInstance(Datasource ds){
	      if(instance == null) {
	    	 
	         instance = new LastSyncData(ds);
	      }
	      return instance;
	   }

	public LastSyncData(Datasource ds) {
		super();
		SRConnection con = null;
		Dataset result = null;
		//initialize the variables from Database
		try {
			 con = ds.getConnection();
			 
			 result = con.runQuery("SELECT last_sync_browsers_timestamp, last_sync_daily_alarms_date, last_sync_daily_aUsers_date, last_sync_date, last_sync_hour,"
			 		+ " last_sync_hourly_alarms_date, last_sync_hourly_alarms_hour, last_sync_hourly_aUsers_date, last_sync_hourly_aUsers_hour,"
			 		+ " last_sync_monthly_aUsers_month, last_sync_monthly_aUsers_year, last_sync_screen_timestamp, last_synch_client_start_timestamp "
			 		+ " , last_sync_audit, last_sync_mod_ia_audit, last_sync_sessions_timestamp "
			 		+ " FROM mod_ia_last_sync;");
			 
			 if(result != null && result.getRowCount() > 0)
			 {
				 if (result.getValueAt(0, "last_sync_browsers_timestamp") == null)
				 {
					 this.last_sync_browsers_timestamp = null;
				 }
				 else
				 {
					 this.last_sync_browsers_timestamp = result.getValueAt(0, "last_sync_browsers_timestamp").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_daily_alarms_date") == null)
				 {
					 this.last_sync_daily_alarms_date = null;
				 }
				 else
				 {
					 this.last_sync_daily_alarms_date = result.getValueAt(0,"last_sync_daily_alarms_date").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_daily_aUsers_date") == null)
				 {
					 this.last_sync_daily_aUsers_date = null;
				 }
				 else
				 {
					 this.last_sync_daily_aUsers_date = result.getValueAt(0,"last_sync_daily_aUsers_date").toString();
				 }
				 
				 if( result.getValueAt(0,"last_sync_date") == null)
				 {
					 this.last_sync_date = null;
				 }
				 else
				 {
					 this.last_sync_date = result.getValueAt(0,"last_sync_date").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_hour") == null)
				 {
					 this.last_sync_hour = null;
				 }
				 else
				 {
					 this.last_sync_hour = result.getValueAt(0,"last_sync_hour").toString();
				 }
				 
				 
				 if( result.getValueAt(0,"last_sync_hourly_alarms_date") == null)
				 {
					 this.last_sync_hourly_alarms_date = null;
				 }
				 else
				 {
					 this.last_sync_hourly_alarms_date = result.getValueAt(0,"last_sync_hourly_alarms_date").toString();	 
				 }
				 
				 if(result.getValueAt(0,"last_sync_hourly_alarms_hour") == null)
				 {
					 last_sync_hourly_alarms_hour = null;
				 }
				 else
				 {
					 this.last_sync_hourly_alarms_hour = result.getValueAt(0,"last_sync_hourly_alarms_hour").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_hourly_aUsers_date") == null)
				 {
					 this.last_sync_hourly_aUsers_date = null;
				 }
				 else
				 {
					 this.last_sync_hourly_aUsers_date = result.getValueAt(0,"last_sync_hourly_aUsers_date").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_hourly_aUsers_hour") == null)
				 {
					 this.last_sync_hourly_aUsers_hour = null;
				 }
				 else
				 {
					 this.last_sync_hourly_aUsers_hour = result.getValueAt(0,"last_sync_hourly_aUsers_hour").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_monthly_aUsers_month") == null)
				 {
					 this.last_sync_monthly_aUsers_month = null; 
				 }
				 else
				 {
					 this.last_sync_monthly_aUsers_month = result.getValueAt(0,"last_sync_monthly_aUsers_month").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_monthly_aUsers_year") == null)
				 {
					 this.last_sync_monthly_aUsers_year = null;
				 }
				 else
				 {
					 this.last_sync_monthly_aUsers_year = result.getValueAt(0,"last_sync_monthly_aUsers_year").toString();
				 }
				 
				 
				 if(result.getValueAt(0,"last_sync_screen_timestamp") == null)
				 {
					 this.last_sync_screen_timestamp = null;
				 }
				 else
				 {
					 this.last_sync_screen_timestamp = result.getValueAt(0,"last_sync_screen_timestamp").toString();
				 }
				 
				 if(result.getValueAt(0,"last_synch_client_start_timestamp") == null)
				 {
					 this.last_synch_client_start_timestamp  = null;
				 }
				 else
				 {
					 this.last_synch_client_start_timestamp  = result.getValueAt(0,"last_synch_client_start_timestamp").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_audit") == null)
				 {
					 this.last_sync_audit_date  = null;
				 }
				 else
				 {
					 this.last_sync_audit_date  = result.getValueAt(0,"last_sync_audit").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_mod_ia_audit") == null)
				 {
					 this.last_sync_mod_ia_audit  = null;
				 }
				 else
				 {
					 this.last_sync_mod_ia_audit  = result.getValueAt(0,"last_sync_mod_ia_audit").toString();
				 }
				 
				 if(result.getValueAt(0,"last_sync_sessions_timestamp") == null)
				 {
					 this.last_sync_sessions_timestamp  = null;
				 }
				 else
				 {
					 this.last_sync_sessions_timestamp  = result.getValueAt(0,"last_sync_sessions_timestamp").toString();
				 }
				 
				
			 }
			 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			DBUtilities.close(con);
		}
		
		
		
	}

	public String getLast_sync_date() {
		return last_sync_date;
	}

	public void setLast_sync_date(String last_sync_date) {
		this.last_sync_date = last_sync_date;
	}

	public String getLast_sync_hour() {
		return last_sync_hour;
	}

	public void setLast_sync_hour(String last_sync_hour) {
		this.last_sync_hour = last_sync_hour;
	}

	public String getLast_sync_screen_timestamp() {
		return last_sync_screen_timestamp;
	}

	public void setLast_sync_screen_timestamp(String last_sync_screen_timestamp) {
		this.last_sync_screen_timestamp = last_sync_screen_timestamp;
	}

	public String getLast_synch_client_start_timestamp() {
		return last_synch_client_start_timestamp;
	}

	public void setLast_synch_client_start_timestamp(
			String last_synch_client_start_timestamp) {
		this.last_synch_client_start_timestamp = last_synch_client_start_timestamp;
	}

	public String getLast_sync_daily_alarms_date() {
		return last_sync_daily_alarms_date;
	}

	public void setLast_sync_daily_alarms_date(String last_sync_daily_alarms_date) {
		this.last_sync_daily_alarms_date = last_sync_daily_alarms_date;
	}

	public String getLast_sync_hourly_alarms_date() {
		return last_sync_hourly_alarms_date;
	}

	public void setLast_sync_hourly_alarms_date(String last_sync_hourly_alarms_date) {
		this.last_sync_hourly_alarms_date = last_sync_hourly_alarms_date;
	}

	public String getLast_sync_hourly_alarms_hour() {
		return last_sync_hourly_alarms_hour;
	}

	public void setLast_sync_hourly_alarms_hour(String last_sync_hourly_alarms_hour) {
		this.last_sync_hourly_alarms_hour = last_sync_hourly_alarms_hour;
	}

	public String getLast_sync_browsers_timestamp() {
		return last_sync_browsers_timestamp;
	}

	public void setLast_sync_browsers_timestamp(String last_sync_browsers_timestamp) {
		this.last_sync_browsers_timestamp = last_sync_browsers_timestamp;
	}

	public String getLast_sync_hourly_aUsers_date() {
		return last_sync_hourly_aUsers_date;
	}

	public void setLast_sync_hourly_aUsers_date(String last_sync_hourly_aUsers_date) {
		this.last_sync_hourly_aUsers_date = last_sync_hourly_aUsers_date;
	}

	public String getLast_sync_hourly_aUsers_hour() {
		return last_sync_hourly_aUsers_hour;
	}

	public void setLast_sync_hourly_aUsers_hour(String last_sync_hourly_aUsers_hour) {
		this.last_sync_hourly_aUsers_hour = last_sync_hourly_aUsers_hour;
	}

	public String getLast_sync_daily_aUsers_date() {
		return last_sync_daily_aUsers_date;
	}

	public void setLast_sync_daily_aUsers_date(String last_sync_daily_aUsers_date) {
		this.last_sync_daily_aUsers_date = last_sync_daily_aUsers_date;
	}

	public String getLast_sync_monthly_aUsers_month() {
		return last_sync_monthly_aUsers_month;
	}

	public void setLast_sync_monthly_aUsers_month(
			String last_sync_monthly_aUsers_month) {
		this.last_sync_monthly_aUsers_month = last_sync_monthly_aUsers_month;
	}

	public String getLast_sync_monthly_aUsers_year() {
		return last_sync_monthly_aUsers_year;
	}

	public void setLast_sync_monthly_aUsers_year(
			String last_sync_monthly_aUsers_year) {
		this.last_sync_monthly_aUsers_year = last_sync_monthly_aUsers_year;
	}

	public int getLast_sync_locationID() {
		return last_sync_locationID;
	}

	public void setLast_sync_locationID(int last_sync_locationID) {
		this.last_sync_locationID = last_sync_locationID;
	}

	public String getLast_sync_audit_date() {
		return last_sync_audit_date;
	}

	public void setLast_sync_audit_date(String last_sync_audit_date) {
		this.last_sync_audit_date = last_sync_audit_date;
	}

	public String getLast_sync_mod_ia_audit() {
		return last_sync_mod_ia_audit;
	}

	public void setLast_sync_mod_ia_audit(String last_sync_mod_ia_audit) {
		this.last_sync_mod_ia_audit = last_sync_mod_ia_audit;
	}
	public String getLast_sync_sessions_timestamp() {
		return last_sync_sessions_timestamp;
	}

	public void setLast_sync_sessions_timestamp(String last_sync_sessions_timestamp) {
		this.last_sync_sessions_timestamp = last_sync_sessions_timestamp;
	}
}

	   