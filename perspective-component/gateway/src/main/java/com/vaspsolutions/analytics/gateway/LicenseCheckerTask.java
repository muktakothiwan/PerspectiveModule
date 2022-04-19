package com.vaspsolutions.analytics.gateway;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.inductiveautomation.ignition.common.model.ApplicationScope;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.vaspsolutions.analytics.common.Constants;

public class LicenseCheckerTask implements Runnable{

	Date expirationDate;
	private GatewayContext context;
	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 
	
	
	public LicenseCheckerTask(Date expirationDate, GatewayContext context) {
		super();
		this.expirationDate = expirationDate;
		this.context = context;
	}


	@Override
	public void run() {
		
		Date currentdate = new Date();
		//log.error("Analytics module : in license checker run.");	
		if (currentdate.compareTo(expirationDate) > 0)
		{
			log.error("Analytics module : The trial expired.");
			
			//send notification to client 
			try {
				this.context.getGatewaySessionManager().sendNotification(ApplicationScope.CLIENT, Constants.MODULE_ID, "TRIAL_EXPIRED", "TRIAL_EXPIRED");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("IA module : " + e);
			}
			//do the cleanup tasks.
			
			this.context.getModuleManager().getModule(Constants.MODULE_ID).getHook().shutdown();
			
		}
		
	}

	
}
