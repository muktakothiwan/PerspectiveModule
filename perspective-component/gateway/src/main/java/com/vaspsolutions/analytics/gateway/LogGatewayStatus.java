package com.vaspsolutions.analytics.gateway;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
/**
 * THis class represents a task that is executed from gateway to log start / stop time.
 * @author YM : Created on 06/26/2015
 *
 */
public class LogGatewayStatus implements Runnable {

	private GatewayContext context;
	private String status;
	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 
	
	public LogGatewayStatus(GatewayContext context, String gatewayStatus) {
		super();
		this.context = context;
		this.status = gatewayStatus;
	}
	@Override
	public void run() {
		
		String newDS = "", oldDS = "", oldAudit = "", oldAlarm = "";
		List<MODIAPersistentRecord> results;
		MODIAPersistentRecord record;
		SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
		results = context.getPersistenceInterface().query(query);
		String eventTime = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
					String actor_host = "";
					try {
						actor_host = InetAddress.getLocalHost().getHostName();
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Date date = new Date();
					eventTime = sdf.format(date);
					String insertQuery = "INSERT INTO mod_ia_audit_events("
							+ "`ACTION`,`ACTION_TARGET`, `ACTION_VALUE`, `ACTOR`, `ACTOR_HOST`,"
							+ "`EVENT_TIMESTAMP`,`ORIGINATING_CONTEXT`,`ORIGINATING_SYSTEM`,"
							+ "`STATUS_CODE`) VALUES ('"
							+ this.status + "',null,null, 'SYSTEM', '" + actor_host
							+ "','" + eventTime + "', 2, null, 0);";
					if(dsNew != null )
					{
						try {
							conNew = dsNew.getConnection(); //db where mod_ia tables are located
							conNew.runUpdateQuery(insertQuery);
							
						} catch (SQLException e) {
							
							e.printStackTrace();
						}finally{
								try {
										if(conNew != null)
										{
											conNew.close();
										}
										
									} catch (SQLException e) {
										log.error("LogGatewayStatus : error in con close " + e);
									
									}
							}
					}
				//}
			}
		}
		

	}

}
