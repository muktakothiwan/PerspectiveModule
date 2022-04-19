package com.vaspsolutions.analytics.gateway;


import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import simpleorm.dataset.SQuery;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.project.Project;
import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.vaspsolutions.analytics.common.Constants;


public class HourlyAuditSummaryTask implements Runnable {

	private GatewayContext context;
	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 
	public HourlyAuditSummaryTask(GatewayContext context) {
		super();
		this.context = context;
	}
	
	
	@Override
	public void run() {
		String newDS = "";
		List<MODIAPersistentRecord> results;
		MODIAPersistentRecord record;
		SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
		results = context.getPersistenceInterface().query(query);
		
		if(results != null && results.size() > 0)
		{
			record = results.get(0);
			if(record != null)
			{
//				if(record.getDbcreated() == true)
//				{
					newDS = record.getDatasource();
					Datasource dsNew = context.getDatasourceManager().getDatasource(Long.parseLong(newDS)); //db where mod_ia tables are located
						SRConnection conNew = null;
					
					if(dsNew != null )
					{
						try {
							conNew = dsNew.getConnection(); //db where mod_ia tables are located
								//execute the stored procedure to summerize alarms data
							int i = 0, noOfProjects = 0;
							String projectName = "";
							Dataset dsProjects = conNew.runQuery("SELECT project_name from MOD_IA_PROJECTS;");
							//get the list of projects
//							List<Project> _projects = this.context.getProjectManager().getProjectsLite(ProjectVersion.Published);
//							
//							
//							if(_projects != null)
//							{
//								noOfProjects = _projects.size();
//							}
//							
							if(dsProjects != null && dsProjects.getRowCount() > 0)
							{
								noOfProjects = dsProjects.getRowCount();
							}
							//call all stored procedures for each project.
							int returnCode = 0;
							for(i=0; i<noOfProjects; i++)
							{
								returnCode = 0;
								//projectName = _projects.get(i).getName().trim();
								if(dsProjects.getValueAt(i, 0) != null)
								{
									projectName = dsProjects.getValueAt(i, 0).toString();
								}
								else
								{
									projectName = "";
								}
								try
								{
									returnCode = conNew.runUpdateQuery("CALL hourly_overview ('" + projectName + "');");
//									log.error("Hourly Audit summary task : retrunCode for CALL hourly_overview is - "
//											+ returnCode + " , for project - " + projectName);
								}
								catch(SQLException e)
								{
									log.error("Hourly Audit summary task  : error in calling hourly_overview overview for project " + projectName +  e);
								}
								try
								{
									returnCode = conNew.runUpdateQuery("CALL hourlyActiveUsersSummary ('" + projectName + "');");
//									log.error("Hourly Audit summary task : retrunCode for CALL hourlyActiveUsersSummary is - "
//											+ returnCode + " , for project - " + projectName);
								}
								catch(SQLException e)
								{
									log.error("Hourly Audit summary task  : error in calling hourlyActiveUsersSummary overview for project " + projectName +  e);
								}
								try
								{
									returnCode = conNew.runUpdateQuery("CALL daily_overview ('" + projectName + "');");
//									log.error("Hourly Audit summary task : retrunCode for CALL daily_overview is - "
//											+ returnCode + " , for project - " + projectName);
								}
							
								catch(SQLException e)
								{
									log.error("Hourly Audit summary task  : error in calling daily_overview overview for project " + projectName +  e);
								}
								try
								{
									returnCode = conNew.runUpdateQuery("CALL dailySessionsSummary ('" + projectName + "');");
//									log.error("Hourly Audit summary task : retrunCode for CALL dailySessionsSummary is - "
//											+ returnCode + " , for project - " + projectName);
								}
								catch(SQLException e)
								{
									log.error("Hourly Audit summary task  : error in calling dailySessionsSummary overview for project " + projectName +  e);
								}
								
								
								
							}
							
							try
							{
								returnCode = conNew.runUpdateQuery("CALL hourlyActiveUsersSummaryAll();");
//								log.error("Hourly Audit summary task : retrunCode for CALL hourlyActiveUsersSummaryAll is - "
//										+ returnCode );
							}
							catch(SQLException e)
							{
								log.error("Hourly Audit summary task  : error in calling hourlyActiveUsersSummaryAll " +  e);
							}
							
							
							
						} catch (SQLException e) {
							
							log.error("Hourly Audit summary task  : " + e);
							
						}finally{
								try {
										if(conNew != null)
										{
											conNew.close();
										}
										
								} catch (SQLException e) {
									log.error("Hourly Audit summary task  : error in con close  " + e);
								}
						}
					}
				//}
			}
		}
		else
		{
			log.error("Hourly Audit summary task  : Could not read module configuration" );
		}
	}

}

