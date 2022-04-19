package com.vaspsolutions.analytics.gateway;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import simpleorm.dataset.SQuery;

import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.vaspsolutions.analytics.common.Constants;

public class MonthlySummaryTask implements Runnable {

	private GatewayContext context;
	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 
	public MonthlySummaryTask(GatewayContext context) {
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
							Datasource dsNew = context.getDatasourceManager().getDatasource(Long.parseLong(newDS)); //db where mod_ia tables are located
								SRConnection conNew = null;
							
							if(dsNew != null )
							{
								try {
									conNew = dsNew.getConnection(); //db where mod_ia tables are located
										//execute the stored procedure.
									log.error("Monthly summary task ran at " + new Date());
									returnCode = conNew.runUpdateQuery("CALL monthlyAlarmsSummary;");
									log.error("Monthly summary task : retrunCode for CALL monthlyAlarmsSummary is - "
											+ returnCode );
									
								} catch (SQLException e) {
									
									log.error("Monthly summary task  : " + e);
									
								}finally{
										try {
												if(conNew != null)
												{
													conNew.close();
												}
												
										} catch (SQLException e) {
											log.error("Monthly summary task  : error in con close. " + e);
										}
								}
							}
						//}
					}
				}
				else
				{
					log.error("Monthly summary task  : Could not read module configuration" );
				}

	}

}
