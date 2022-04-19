package com.vaspsolutions.analytics.gateway;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import simpleorm.dataset.SQuery;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.vaspsolutions.analytics.common.Constants;


public class HourlyAlarmsSummaryTask implements Runnable {

	
	private GatewayContext context;
	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 
	public HourlyAlarmsSummaryTask(GatewayContext context) {
		super();
		this.context = context;
	}

	@Override
	public void run() {
		//First query the persistent record to find out if new DB is created and get the table names.
				String newDS = "";
				List<MODIAPersistentRecord> results;
				MODIAPersistentRecord record;
				SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
				results = context.getPersistenceInterface().query(query);
				
				int returnCode = 0;
				if(results != null && results.size() > 0)
				{
					record = results.get(0);
					if(record != null)
					{
//						if(record.getDbcreated() == true)
//						{
							newDS = record.getDatasource();
							Datasource dsNew = context.getDatasourceManager().getDatasource(Long.parseLong(newDS)); //DB where mod_ia tables are located
								SRConnection conNew = null;
							
							if(dsNew != null )
							{
								try {
									conNew = dsNew.getConnection(); //DB where mod_ia tables are located
										//execute the stored procedure to summarize alarms data
//									log.error("Call to Hourly Alarms summary task");
									returnCode = conNew.runUpdateQuery("CALL hourlyAlarmsSummary;");
//									log.error("Hourly Alarms summary task  return code : " + returnCode);
									//call daily Alarms Summary
									
									returnCode = conNew.runUpdateQuery("CALL dailyAlarmsSummary;");
									
									returnCode = conNew.runUpdateQuery("update mod_ia_db_status set update_time = current_timestamp;");
									
								} catch (SQLException e) {
									
									log.error("Hourly Alarms summary task  : " + e);
									
								}finally{
										try {
												if(conNew != null)
												{
													conNew.close();
												}
												
										} catch (SQLException e) {
											log.error("Hourly Alarms summary task  : error in con close " + e);
										}
								}
							}
							

							
						//}
							//also call the service on controller to send data							
							ModuleRPCImpl _gcRPC = new ModuleRPCImpl(this.context);
							if (_gcRPC.getIfEnterprise())
							{
								_gcRPC.sendAlarmsSummary();
							
								_gcRPC.sendAlarmsAckCounts();
							
								_gcRPC.sendAlarmsActiveCounts();
							}
					}
				}
				else
				{
					log.error("Hourly Alarms summary task  : Could not read module configuration" );
				}
				

	}

}
