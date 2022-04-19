package com.vaspsolutions.analytics.gateway;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.vaspsolutions.analytics.common.Constants;


public class GANAuditSyncTask implements Runnable{

	public GANAuditSyncTask(GatewayContext context) {
		super();
		this.context = context;
	}
	private GatewayContext context;
	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 
	@Override
	public void run() {

		ModuleRPCImpl _gcRPC = new ModuleRPCImpl(this.context);
		
		boolean status = _gcRPC.sendSummaryOnScreenViewChange();
		if(status == false)
			log.error("GANAuditSyncTask sendSummaryOnScreenViewChange returned " + status);
		
		status  = _gcRPC.sendActiveUsers();
		if(status == false)
			log.error("GANAuditSyncTask sendActiveUsers returned " + status);
		
		status = _gcRPC.sendBrowsers();
		if(status == false)	
			log.error("GANAuditSyncTask sendBrowsers returned " + status);

		status = _gcRPC.sendLocationInfo();
		if(status == false)
			log.error("GANAuditSyncTask sendLocationInfo returned " + status);
		
		status = _gcRPC.sendUsersStatus();
		if(status == false)
			log.error("GANAuditSyncTask sendUsersStatus returned " + status);
		
		status = _gcRPC.sendAuditActions();
		if(status == false)
			log.error("GANAuditSyncTask sendAuditActions returned " + status);
		
		status = _gcRPC.sendModIAAuditEvents();
		if(status == false)
			log.error("GANAuditSyncTask sendModIAAuditEvents returned " + status);
		//update last sync times 
		_gcRPC.persisteLastSynchTimes();
	}

}
