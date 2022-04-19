package com.vaspsolutions.analytics.gateway;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.inductiveautomation.ignition.common.gson.JsonElement;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.perspective.common.api.PropertyType;
import com.inductiveautomation.perspective.gateway.event.SessionShutdownEvent;
import com.inductiveautomation.perspective.gateway.event.SessionStartupEvent;
import com.inductiveautomation.perspective.gateway.session.InternalSession;
import com.vaspsolutions.analytics.common.Constants;

public class PerspectiveSessionListener {
	Logger logger = LoggerFactory.getLogger(Constants.MODULE_LOG_NAME);
	   
	@Subscribe
	public void sessionStarted(SessionStartupEvent startupEvent)
	{
		InternalSession _session = startupEvent.getSession();
		logger.info("info : " + _session.getSessionInfo().toString());
//		JsonObject sessionProps = _session.getPropertyTreeOf(PropertyType.props).toJson();
//		logger.info("sessionProps " + sessionProps);
//		JsonObject device  = sessionProps.getAsJsonObject("device");
//		logger.info("device type " + device.get("type"));
		
		
		
	}
	
	@Subscribe
	public void sessionEnded(SessionShutdownEvent shutdownEvent)
	{
		logger.info("Session ended: " + shutdownEvent.toString());
	}
	
}
