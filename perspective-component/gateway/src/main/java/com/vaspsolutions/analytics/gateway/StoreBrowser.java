package com.vaspsolutions.analytics.gateway;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import simpleorm.dataset.SQuery;

import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public class StoreBrowser implements Serializable{

	

	public static void storeBrowserInformation(String browser, String ipAddress, int bVersion, GatewayContext gc)
	{
		List<MODIAPersistentRecord> results;
		MODIAPersistentRecord record;
		Datasource ds;
		String insertQuery = "";
		SRConnection con = null;
	
		//first retrieve the Data store name from Persistence record
		
		SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
		results = gc.getPersistenceInterface().query(query);
		
		String dsName = "";
		
		if(results != null && results.size() > 0)
		{
			
			record = results.get(0);
			if(record != null)
			{
					dsName = record.getDatasource();
				
			}
		
			//connect to the DB and store the record
			ds = gc.getDatasourceManager().getDatasource(Long.parseLong(dsName));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
			try {
				//con = ds.getConnection();
				insertQuery = "INSERT INTO mod_ia_browser_info (BROWSER_NAME,IP_ADDRESS, BROWSER_VERSION, TIMESTAMP ) VALUES('" 
						+ browser + "', '" + ipAddress + "', " + bVersion 
						+ ", '"+ sdf.format(new Date()) + "');";
				
				//con.runUpdateQuery(insertQuery);
				
				gc.getHistoryManager().storeHistory(ds.getName(), new StoreForwardDS(insertQuery, ds.getName()));
			
			} catch (SQLException e) {
				e.printStackTrace();
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					if(con!= null)
					{
						con.close();
					}
				} catch (SQLException e) {
					
					e.printStackTrace();
				}
			}
			
		}
		
	}
}
