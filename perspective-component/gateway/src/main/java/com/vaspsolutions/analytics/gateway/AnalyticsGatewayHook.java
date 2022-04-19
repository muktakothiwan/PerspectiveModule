package com.vaspsolutions.analytics.gateway;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseMode;
import com.inductiveautomation.ignition.common.licensing.LicenseRestriction;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.model.ApplicationScope;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.model.event.InvalidListenerException;
import com.inductiveautomation.ignition.common.tags.model.event.TagChangeListener;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.gan.GatewayNetworkManager;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IRecordListener;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.model.GatewayModule;
import com.inductiveautomation.ignition.gateway.model.ModuleObserver;
import com.inductiveautomation.ignition.gateway.tags.model.GatewayTagManager;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.DefaultConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.api.ServerListener;
import com.inductiveautomation.metro.api.ServerState;
import com.inductiveautomation.metro.api.ServiceManager;
import com.inductiveautomation.perspective.common.api.ComponentRegistry;
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateRegistry;
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext;
import com.vaspsolutions.analytics.common.Analytics;
import com.vaspsolutions.analytics.common.Constants;

import simpleorm.dataset.SQuery;

import com.vaspsolutions.analytics.common.component.display.Messenger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaspsolutions.analytics.common.component.display.Image;
import com.vaspsolutions.analytics.common.component.display.TagCounter;
import com.vaspsolutions.analytics.gateway.delegate.MessageComponentModelDelegate;
import com.vaspsolutions.analytics.gateway.endpoint.DataEndpoints;
import com.vaspsolutions.analytics.gateway.services.AgentService;
import com.vaspsolutions.analytics.gateway.services.AgentServiceImpl;
import com.vaspsolutions.analytics.gateway.services.GetAnalyticsInformationService;
import com.vaspsolutions.analytics.gateway.services.GetAnalyticsInformationServiceImpl;

public class AnalyticsGatewayHook extends AbstractGatewayModuleHook implements TagChangeListener, IRecordListener<MODIAPersistentRecord>, ModuleObserver {

	private static final String[] IA_CONFIG_MENU_PATH = {"Analytics Module"};
	public static final ConfigCategory IA_CONFIG_CATEGORY = new ConfigCategory("Analytics Module", "IAConfigPage.nav.header", 700);
	Logger logger = LoggerFactory.getLogger(Constants.MODULE_LOG_NAME); 

	private GatewayContext gatewayContext;
	private PerspectiveContext perspectiveContext;
	private ComponentRegistry componentRegistry;
	private ComponentModelDelegateRegistry modelDelegateRegistry;
	private DataEndpoints routes;
	private ModuleRPCImpl _gcRPC;
	private PerspectiveSessionListener _sessionListener;
	@Override
	public void setup(GatewayContext context) {
		this.gatewayContext = context;
		logger.info("Setting up Analytics module.");

		BundleUtil.get().addBundle("ignitionanalytics", this.getClass(), "MOD_IA_HomePage");
		BundleUtil.get().addBundle("IAConfigPage", this.getClass(), "IAConfigPage");


		try {
			//register the persistent record
			gatewayContext.getSchemaUpdater().updatePersistentRecords(MODIAPersistentRecord.META);

			// create records if needed
			maybeCreateIASettings(context);

			// get the settings record and do something with it...
			MODIAPersistentRecord theOneRecord = context.getLocalPersistenceInterface().find(MODIAPersistentRecord.META, 0L);


			// listen for updates to the settings record...

			MODIAPersistentRecord.META.addRecordListener(this);

			//initialize our Gateway nav menu
			//initMenu();

			logger.debug("Setup Complete.");
			this.gatewayContext.getModuleManager().addModuleObserver(this);


		} catch (SQLException e) {

			e.printStackTrace();
		} 
	}

	@Override
	public void startup(LicenseState activationState) {
		
		this._gcRPC = new ModuleRPCImpl(gatewayContext);
		logger.info("Starting up Analytics Module Gateway Hook!");

		this.perspectiveContext = PerspectiveContext.get(this.gatewayContext);
		this.componentRegistry = this.perspectiveContext.getComponentRegistry();
		this.modelDelegateRegistry = this.perspectiveContext.getComponentModelDelegateRegistry();
		
		this._sessionListener = new PerspectiveSessionListener();
		this.perspectiveContext.getEventBus().register(this._sessionListener);

		if (this.componentRegistry != null) {
			logger.info("Registering Analytics component.");
			this.componentRegistry.registerComponent(Image.DESCRIPTOR);
//			this.componentRegistry.registerComponent(TagCounter.DESCRIPTOR);
//			this.componentRegistry.registerComponent(Messenger.DESCRIPTOR);
		} else {
			logger.error("Reference to component registry not found, Rad Components will fail to function!");
		}

//		if (this.modelDelegateRegistry != null) {
//			log.info("Registering model delegates.");
//			this.modelDelegateRegistry.register(Messenger.COMPONENT_ID, MessageComponentModelDelegate::new);
//		} else {
//			log.error("ModelDelegateRegistry was not found!");
//		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if(Constants.licenseCheckEnabled)
		{
			if(activationState.getLicenseMode() == LicenseMode.Trial)
			{
				//			if(licenseState.isTrialExpired() == false )
				//			{
				//				startUPTasks();
				//			}
				//			else
				//			{
				logger.error("IA module : Please check the license.");
				//			}
			}
			else 
			{
				List<LicenseRestriction> licenseDetails = activationState.getModuleLicense().getLicenseDetails();
				Iterator<LicenseRestriction> itr;
				String dateStr = null;
				if ( !licenseDetails.isEmpty())
				{
					itr = licenseDetails.iterator();
					while(itr.hasNext())
					{
						LicenseRestriction res = itr.next();
						String kName = res.getRestrictionName();
						if(kName.compareToIgnoreCase("EXPIRATION_DATE") == 0)
						{
							dateStr = res.getRestrictionValue();
							break;
						}
					}

					if (dateStr != null)
					{
						Date currentdate = new Date();

						try {
							Date expirationDate = sdf.parse(dateStr);


							if (currentdate.compareTo(expirationDate) <= 0)
							{
								logger.error("IA module : Trial valid.");
								startUPTasks();
								this.gatewayContext.getExecutionManager().registerWithInitialDelay(Constants.MODULE_ID, "LicenseChecker", new LicenseCheckerTask(expirationDate, this.gatewayContext), 30000, TimeUnit.MILLISECONDS, 0);

							}
							else
							{
								logger.error("IA module : The trial expired.");
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					else
					{
						//dateStr not found means there is activated license without expiration date set , i.e. valid license so proceed .
						startUPTasks();

						logger.error("IA module : Valid License. No expiration date set.");


					}

				}
				else
				{
					logger.error("IA module : Please check the trial license. Details not found.");
				}



			}
		}
		else
		{
			startUPTasks();			
		}



	}

	@Override
	public void shutdown() {
		logger.info("Shutting down RadComponent module and removing registered components.");
		if (this.componentRegistry != null) {
			this.componentRegistry.removeComponent(Image.COMPONENT_ID);
//			this.componentRegistry.removeComponent(TagCounter.COMPONENT_ID);
//			this.componentRegistry.removeComponent(Messenger.COMPONENT_ID);
		} else {
			logger.warn("Component registry was null, could not unregister Rad Components.");
		}
//		if (this.modelDelegateRegistry != null ) {
//			this.modelDelegateRegistry.remove(Messenger.COMPONENT_ID);
//		}

		BundleUtil.get().removeBundle("IAConfigPage");
		//
		//        	    /* remove our nodes from the menu */
		//this.gatewayContext.getConfigMenuModel().removeConfigMenuNode(IA_CONFIG_MENU_PATH);
		shutdownTasks();

	}



	@Override
	public void notifyLicenseStateChanged(LicenseState licenseState) {

		super.notifyLicenseStateChanged(licenseState);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if(Constants.licenseCheckEnabled)
		{
			if(licenseState.getLicenseMode() == LicenseMode.Trial)
			{
				//			if(licenseState.isTrialExpired())
				//			{
				logger.error("Analytics module : Please check the license.");
				//				shutdownTasks();
				//			}
				//			else
				//			{
				//				//call startup methods
				//				startUPTasks();
				//			}
			}
			else 
			{
				List<LicenseRestriction> licenseDetails = licenseState.getModuleLicense().getLicenseDetails();
				Iterator<LicenseRestriction> itr;
				String dateStr = null;
				if ( !licenseDetails.isEmpty())
				{
					itr = licenseDetails.iterator();
					while(itr.hasNext())
					{
						LicenseRestriction res = itr.next();
						String kName = res.getRestrictionName();
						if(kName.compareToIgnoreCase("EXPIRATION_DATE") == 0)
						{
							dateStr = res.getRestrictionValue();
							break;
						}
					}
					if (dateStr != null)
					{
						Date currentdate = new Date();
						try {
							Date expirationDate = sdf.parse(dateStr);

							if (currentdate.compareTo(expirationDate) <= 0)
							{
								logger.error("Analytics module : Trial valid.");
								startUPTasks();
								this.gatewayContext.getExecutionManager().registerWithInitialDelay(Constants.MODULE_ID, "LicenseChecker", new LicenseCheckerTask(expirationDate, this.gatewayContext), 30000, TimeUnit.MILLISECONDS, 0);

							}
							else
							{
								shutdownTasks();
								logger.error("Analytics module : The trial expired.");
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					else
					{
						//dateStr not found means there is activated license without expiration date set , i.e. valid license so proceed .
						startUPTasks();

						//logger.error("Analytics module : Valid License. No expiration date set.");
					}

				}
				else
				{
					shutdownTasks();
					logger.error("Analytics module : Please check the Analytics Module  license. Details not found.");
				}

			}
		}
		else
		{
			startUPTasks();
		}


	}

	private void startUPTasks()
	{

		
		String dbName =_gcRPC.getPersistenceRecord(); 
		boolean isAgent = _gcRPC.getIfAgent();
		boolean isEnterprise = _gcRPC.getIfEnterprise();
		if ( dbName != null && dbName.length() > 0 )
		{
			//call a function to log gateway start time on startup

			this.gatewayContext.getExecutionManager().executeOnce(new LogGatewayStatus(this.gatewayContext, "GATEWAY_START"));
			logger.error("Gateway hook : registeting the task Alarms Summary");;
			this.gatewayContext.getExecutionManager().register("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_HOURLY_ALARMS_SUMMARY", new HourlyAlarmsSummaryTask(this.gatewayContext),15000);
			logger.error("Gateway hook : registeting the task Audit Summary");;
			this.gatewayContext.getExecutionManager().register("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_HOURLY_AUDIT_SUMMARY", new HourlyAuditSummaryTask(this.gatewayContext),60000);
			logger.error("Gateway hook : registeting the task Active users");;
			this.gatewayContext.getExecutionManager().register("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_ACTIVE_USERS_SUMMARY", new ActiveUserSummaryTask(this.gatewayContext),60000);


			//register a sync task on agent
			//			if(isAgent == true)
			//			{
			if(isEnterprise)
			{
				logger.error("Gateway hook : registeting the task Audit Sync");;
				this.gatewayContext.getExecutionManager().register("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_GANAUDITSYNC", new GANAuditSyncTask(this.gatewayContext),60000);
			}
			//			}

		}

		//store known servers 
		if(isEnterprise)
		{
			GatewayNetworkManager gm = this.gatewayContext.getGatewayAreaNetworkManager();
			//			logger.error("getNoOfUserPerScreenFromRemoteServer : 1");
			// Service setup
			ServiceManager sm = this.gatewayContext.getGatewayAreaNetworkManager().getServiceManager();


			if(isAgent == false)
			{    
				gm.addServerListener(new ServerListener() {


					@Override
					public void serverStateChanged(ServerId arg0, ServerState arg1) {
						logger.error("server state changed for  : "  + arg0.getServerName() + ", state : " + arg1.name());

						if(arg0.getServerName().trim().compareToIgnoreCase(gm.getServerAddress().getServerName().trim()) != 0)
						{
							_gcRPC.updateGANServer(arg0.toString(), arg0.getServerName(), arg1.name());
						}


					}
				});


				//agents
				List<ServerId> servers = gm.getKnownServers();

				//controller
				servers.add(gm.getServerAddress());
				for (ServerId srvid:servers)
				{

					logger.error("srv name: " + srvid.getServerName());
					logger.error("srv role : " + srvid.getRole());
					logger.error("srv root server id: " + srvid.getRootServerId());
					logger.error("srv descriptive string : " + srvid.toDescriptiveString());
					logger.error("srv id hashcode: " + srvid.hashCode());
					logger.error("srv id toString : " + srvid.toString());
					if(gm.getConnectionForServer(srvid) != null)
					{
						logger.error("local UUID : " + gm.getConnectionForServer(srvid).getLocalUUID());
						logger.error("Remote ID : " + gm.getConnectionForServer(srvid).getRemoteId());
						logger.error("conn ID : " + gm.getConnectionForServer(srvid).getId());
					}


				}
				//logger.error("gateway gan setup key : " + this.gatewayContext.getGatewayAreaNetworkManager().GAN_SETUP_PROP_KEY);
				//logger.error("gateway server address : " + this.gatewayContext.getGatewayAreaNetworkManager().getServerAddress().getRole());

				_gcRPC.storeGANServersList(servers);
				//Register a service on controller



				//register service on controller
				GetAnalyticsInformationService srv = new GetAnalyticsInformationServiceImpl(this.gatewayContext);
				sm.registerService(GetAnalyticsInformationService.class, srv);

			}


			//regsiter agent service on agent 
			AgentService agentSrv = new AgentServiceImpl(this.gatewayContext);
			sm.registerService(AgentService.class, agentSrv);



		}
		//create and subscribe to the tag to monitor DB update status

		TagPath myPath;
		TagPath parentPath;
		TagPath screenTagPath;
		//SQLTagsManager _tagManager;
		GatewayTagManager _tagManager;
		try {

			parentPath = TagPathParser.parse("[default]");
			myPath = TagPathParser.parse("[default]IA_DB_STATUS");

			screenTagPath = TagPathParser.parse("[default]IA_SCREEN_VIEW_UPDATE_TIME");
			_tagManager = this.gatewayContext.getTagManager();


			this.gatewayContext.getTagManager().subscribeAsync(myPath, this);
			this.gatewayContext.getTagManager().subscribeAsync(screenTagPath, this);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("IA module gatewayHook startup : exception for tag subscribe" + e);
			e.printStackTrace();
		}


	}

	public void shutdownTasks()
	{
		try
		{
			logger.error("gateway shutdown task called");

			//call function to log gateway stop time
			this.gatewayContext.getExecutionManager().executeOnce(new LogGatewayStatus(this.gatewayContext, "GATEWAY_SHUTDOWN"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("shutdown hook exception : " + e.getMessage());
		}

		this.gatewayContext.getExecutionManager().unRegister("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_HOURLY_ALARMS_SUMMARY");
		this.gatewayContext.getExecutionManager().unRegister("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_HOURLY_AUDIT_SUMMARY");
		this.gatewayContext.getExecutionManager().unRegister("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_ACTIVE_USERS_SUMMARY");
		this.gatewayContext.getExecutionManager().unRegister("MODULE_IGNITION_ANALYTICS", "MODULE_IGNITION_ANALYTICS_GANAUDITSYNC");
		//unregister the license checker task
		this.gatewayContext.getExecutionManager().unRegister(Constants.MODULE_ID, "LicenseChecker");

		//Unregister services

		ServiceManager sm = this.gatewayContext.getGatewayAreaNetworkManager().getServiceManager();
		sm.unregisterService(GetAnalyticsInformationService.class);
		sm.unregisterService(AgentService.class);

		//unregister perspective session listener
		this.perspectiveContext.getEventBus().unregister(this._sessionListener);
		
		//unsubscribe the tag
		TagPath myPath, screenTagPath ;
		try {

			myPath = TagPathParser.parse("[default]IA_DB_STATUS");
			screenTagPath = TagPathParser.parse("[default]IA_SCREEN_VIEW_UPDATE_TIME");
			this.gatewayContext.getTagManager().unsubscribeAsync(myPath, this);
			this.gatewayContext.getTagManager().unsubscribeAsync(screenTagPath, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IA module gatewayHook shutdown : exception for tag unsubscribe" + e);
			e.printStackTrace();
		}

		//this.gatewayContext.getModuleManager().removeModuleObserver(this);
	}
	@Override
	public Optional<String> getMountedResourceFolder() {
		return Optional.of("mounted");
	}

	@Override
	public void mountRouteHandlers(RouteGroup routeGroup) {
		// where you may choose to implement web server endpoints accessible via `host:port/system/data/
		routes = new DataEndpoints(this.perspectiveContext, routeGroup);
	}

	// Lets us use the route http://<gateway>/res/radcomponents/*
	@Override
	public Optional<String> getMountPathAlias() {
		return Optional.of(Analytics.URL_ALIAS);
	}

	

	public void maybeCreateIASettings(GatewayContext context) {
		try {

			List<MODIAPersistentRecord> results;
			SQuery<MODIAPersistentRecord> query = new SQuery<MODIAPersistentRecord>(MODIAPersistentRecord.META);
			results = context.getPersistenceInterface().query(query);
			MODIAPersistentRecord record;
			if(results != null && results.size() > 0)
			{

				record = results.get(0);
				if(record != null && record.getId() != 0L)
				{

					record.setId(0L);
					context.getPersistenceInterface().save(record);
				}
			}
			else
			{
				MODIAPersistentRecord settingsRecord = context.getLocalPersistenceInterface().createNew(MODIAPersistentRecord.META);
				settingsRecord.setId(0L);
				//settingsRecord.setDatasource("");

				context.getSchemaUpdater().ensureRecordExists(settingsRecord);
			}

			/*
			 * This doesn't override existing settings, only replaces it with these if we didn't
			 * exist already.
			 */

		} catch (Exception e) {
			logger.error("Failed to establish Analytics module settings Record exists", e);
		}

		logger.trace("Analytics module Settings Record Established");
	}

	@Override
	public void moduleAdded(GatewayModule arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moduleRemoved(GatewayModule arg0) {
		// TODO Auto-generated method stub
		String moduleID = arg0.getInfo().getId();
		//		logger.error("module removed called for : " + moduleID);
		if(moduleID.trim().compareToIgnoreCase("com.vaspsolutions.analytics") == 0)
		{
			//			logger.error("b4 cleanupMOdule DB : " + new Date());
			this._gcRPC.cleanupModuleDB();
			//			logger.error("after cleanupMOdule DB : " + new Date());

			//remove the module observer on uninstall
			this.gatewayContext.getModuleManager().removeModuleObserver(this);

			//remove the persistent record listener
			MODIAPersistentRecord.META.removeRecordListener(this);
		}
		//		else
		//		{
		//			logger.error("comparison false");
		//		}
	}

	@Override
	public void moduleStarted(GatewayModule arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moduleStopped(GatewayModule arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recordAdded(MODIAPersistentRecord arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recordDeleted(KeyValue arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recordUpdated(MODIAPersistentRecord arg0) {
		logger.error("recordUpdated()");
		_gcRPC = new ModuleRPCImpl(gatewayContext);
		_gcRPC.createAndPopulateAuditDB(Long.parseLong(arg0.getDatasource()), arg0.getIsAgent());


	}

	@Override
	public List<ConfigCategory> getConfigCategories() {
		return Collections.singletonList(IA_CONFIG_CATEGORY);
	}

	public static final IConfigTab IA_CONFIG_ENTRY = DefaultConfigTab.builder()
			.category(IA_CONFIG_CATEGORY)
			.name("Analytics Module")
			.i18n("IAConfigPage.nav.settings.title")
			.page(IAConfigPage.class)
			.terms("Analytics Module Settings")
			.build();

	@Override
	public List<? extends IConfigTab> getConfigPanels() {
		return Arrays.asList(
				IA_CONFIG_ENTRY
				);
	}

	@Override
	public void tagChanged(com.inductiveautomation.ignition.common.tags.model.event.TagChangeEvent arg0)
			throws InvalidListenerException {
		// TODO Auto-generated method stub
		try {

			String tagName = arg0.getTagPath().getLastPathComponent();
			//If Tag is IA_SCREEN_VIEW_UPDATE_TIME

			if(tagName.compareToIgnoreCase("IA_DB_STATUS") == 0)
			{
				// IF Tag IA_DB_STATUS is changed then send notification to the client.
				this.gatewayContext.getGatewaySessionManager().sendNotification(ApplicationScope.CLIENT, Constants.MODULE_ID, "DATA_CHANGE", "DATA_CHANGE");
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object getRPCHandler(ClientReqSession session, String projectName) {
		// TODO Auto-generated method stub
		return new ModuleRPCImpl(this.gatewayContext);
	}
	
	
}
