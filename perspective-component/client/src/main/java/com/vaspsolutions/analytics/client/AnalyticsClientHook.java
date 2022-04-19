package com.vaspsolutions.analytics.client;





import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inductiveautomation.factorypmi.application.FPMISystem;
import com.inductiveautomation.factorypmi.application.FPMIWindow;
import com.inductiveautomation.factorypmi.application.script.builtin.SecurityUtilities;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.licensing.LicenseMode;
import com.inductiveautomation.ignition.common.licensing.LicenseRestriction;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook;
import com.inductiveautomation.vision.api.client.VisionClientInterface;
import com.inductiveautomation.vision.api.client.VisionWindowListener;
import com.vaspsolutions.analytics.common.ClientRecord;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.HTTPRequestor;

import com.vaspsolutions.analytics.common.ModIALicenseManager;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.ScreenViewsRecord;
import org.jfree.util.Log;

/**
 * A hook provided by Ignition SDK 
 * 1. Override Startup method to
 * 		- add a window listener
 * 		- get OS, IP , Mobile flag information using Java and FPMIApp properties
 * 		- call free geoip web service to get location information for the user.
 * 		- makes a rpc call to gateway to log all this information in IA module specific tables.
 * 2. Implemets Vision Window Listener , to take appropriate actions on window events
 * 
 * @author YM
 *
 */

public class AnalyticsClientHook extends AbstractClientModuleHook implements VisionWindowListener, InternalFrameListener {

	private volatile ClientContext _context; //store the client context to be used in Screen handlers
	private VisionClientInterface vision ;
	private ModuleRPC rpc; //store an instance of Module RPC class 
	Logger logger = LoggerFactory.getLogger(Constants.MODULE_LOG_NAME); 
	@Override
	public void startup(ClientContext context, LicenseState activationState)
			throws Exception {
		_context = context;
	
		logger.info("In Startup");
		vision = (VisionClientInterface)context.getModule(VisionClientInterface.VISION_MODULE_ID);
		logger.info("after vision");
		rpc = ModuleRPCFactory.create(Constants.MODULE_ID, ModuleRPC.class);
		logger.info("after rpc creation");
		
		
		ModIALicenseManager manager = ModIALicenseManager.getInstance();
		manager.setLicenseState(activationState);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		if(Constants.licenseCheckEnabled)
		{
			if(activationState.getLicenseMode() == LicenseMode.Trial)
			{
	//			if(activationState.isTrialExpired() == false )
	//			{
	//				startTasks();
	//			}
	//			else
	//			{
					System.out.println("IA module : Please check the license and/or reset trial.");
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
							manager.setExpirationDate(expirationDate);
							if (currentdate.compareTo(expirationDate) <= 0)
							{
								startTasks();
							}
							else
							{
								System.out.println("IA module : The trial expired.");
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					else
					{
						//dateStr not found means there is activated license without expiration date set , i.e. valid license so proceed .
						startTasks();
						
					}
					
				}
				else
				{
					System.out.println("IA module : Please check the trial license. Details not found.");
				}
			}
		}
		else
		{
			startTasks();
		}
		
	}

	private void startTasks() {
		//Add a listener for window open and close events.
		logger.info("b4 add win listener");
				vision.addWindowListener(this);
				logger.info("after add win listener");
				
				
//				System.out.println("Client hook : startup tasks.");
				//get the OS, IP address and mobile flag information in the system.
				ClientRecord cRec = new ClientRecord();
				
				
				
				try {
					cRec.setHostInternalIP(InetAddress.getLocalHost().getHostAddress());
					cRec.setHostName(InetAddress.getLocalHost().getHostName());
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					logger.info(e1.toString());
					e1.printStackTrace();
				}
				
				
				
				cRec.setUserName(SecurityUtilities.getUsername());
				
				if(FPMISystem.isMobile() == true)
				{
					cRec.setoSName("Mobile OS");
					cRec.setOsVersion("-");
				}
				else
				{
					cRec.setoSName(System.getProperty("os.name"));
					cRec.setOsVersion(System.getProperty("os.version"));
				}

				cRec.setMobile(FPMISystem.isMobile());
				cRec.setProject(this._context.getProject().getName());
				cRec.setClientContext(4);
				
				//get the screen resolution
				Toolkit toolKit = Toolkit.getDefaultToolkit();
				
				String sResolution = "" + (int)toolKit.getScreenSize().getWidth() + " x " + (int)toolKit.getScreenSize().getHeight();
				cRec.setScreenResolution(sResolution.trim() );
				
				//call the service to get External IP 
				HTTPRequestor req = new HTTPRequestor();
				try
				{
				String extIP = req.callService("http://checkip.amazonaws.com/" + cRec.getHostInternalIP());
//				System.out.println("calling service");
				if(extIP != null  && extIP.length() <= 15)
				{
					
					cRec.setHostExternalIP(extIP);
					//call a web service to get location , latitude and longitude information if location info is not available
					if(rpc.IfLocationExists(cRec.getHostInternalIP(), cRec.getHostExternalIP()) != true)
					{
						try {
						//
//							System.out.println("location if ");
						String latlongString = null;
						latlongString = req.callService(Constants.geoip_service_1_url + extIP + Constants.geoip_api_key);
						JSONObject locationJSON;
						if(latlongString != null)
						{
							
							locationJSON = new JSONObject(latlongString );
							cRec.setCity(locationJSON.get("city").toString());
							cRec.setCountry(locationJSON.get("country_name").toString());
							cRec.setState(locationJSON.get("region_name").toString());
							cRec.setLatitude(Double.parseDouble(locationJSON.get("latitude").toString()));
							cRec.setLongitude(Double.parseDouble(locationJSON.get("longitude").toString()));
						}
						else
						{
							latlongString = req.callService(Constants.geoip_service_2_url + extIP + Constants.geoip_api_key);
							if(latlongString != null)
							{
								
								locationJSON = new JSONObject(latlongString );
								cRec.setCity(locationJSON.get("city").toString());
								cRec.setCountry(locationJSON.get("country_name").toString());
								cRec.setState(locationJSON.get("region_name").toString());
								cRec.setLatitude(Double.parseDouble(locationJSON.get("latitude").toString()));
								cRec.setLongitude(Double.parseDouble(locationJSON.get("longitude").toString()));
							}
							else
							{
								locationJSON = new JSONObject("" + extIP);
							}
						}
						
						
					} catch (JSONException e) {
						cRec.setHostExternalIP("00.00.00.00");
						cRec.setCity("Unknown");
						logger.info(e.toString());
					}
					
					}
					else
					{
						cRec.setCity(null);
						cRec.setCountry(null);
						cRec.setState(null);
					}
				}
				else
				{
					extIP = "00.00.00.00";
					cRec.setHostExternalIP(extIP);
					cRec.setCity("Unknown");
//					System.out.println("External IP: "+extIP);
//					extIP = "";
				}
//				System.out.println("after getting location");
			}catch(Exception e)
			{
				cRec.setHostExternalIP("00.00.00.00");
				cRec.setCity("Unknown");
				logger.info(e.toString());
			}
				
				
				try {
//					System.out.println("before call to store client information");
					rpc.storeClientInformation(cRec);
//					System.out.println("after call to store client information");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.info(e.toString());
				}
			}
		
	

	@Override
	public void windowClosed(FPMIWindow arg0) {
	
	
		//arg0.removeInternalFrameListener(this);
		ScreenViewsRecord record = new ScreenViewsRecord();
		
		record.setUsername(SecurityUtilities.getUsername());
		record.setScreenName(arg0.getName());
		//record.setScreenPath(arg0.getPath());
		record.setScreenTitle(arg0.getTitle());
		record.setProjectName(this._context.getProject().getName());
		record.setAction(Constants.SCREEN_CLOSE);
		try {
			//log to the database
			rpc.storeScreenViewedInformation(record);
		} catch (Exception e) {
			
			logger.info(e.toString());
		}
	}

	@Override
	public void windowOpened(FPMIWindow arg0) {
		
		
		//arg0.addInternalFrameListener(this);
		ScreenViewsRecord record = new ScreenViewsRecord();
		
		record.setUsername(SecurityUtilities.getUsername());
		record.setScreenName(arg0.getName());
		record.setScreenPath(arg0.getPath());
		record.setScreenTitle(arg0.getTitle());
		record.setProjectName(this._context.getProject().getName());
		record.setAction(Constants.SCREEN_OPEN);
		try {
			//log to the database
			rpc.storeScreenViewedInformation(record);
		} catch (Exception e) {
			
			logger.info(e.toString());
		}
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent arg0) {
		
		
		FPMIWindow win = (FPMIWindow)arg0.getSource();
		
		ScreenViewsRecord record = new ScreenViewsRecord();
		
		record.setUsername(SecurityUtilities.getUsername());
		record.setScreenName(win.getName());
		record.setScreenPath(win.getPath());
		record.setScreenTitle(win.getTitle());
		record.setProjectName(this._context.getProject().getName());
		record.setAction(Constants.SCREEN_OPEN);
		try {
			//log to the database
			rpc.storeScreenViewedInformation(record);
		} catch (Exception e) {
			
			logger.info(e.toString());
		}
		
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent arg0) {
		
		
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent arg0) {
		
		
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent arg0) {
		
		
		
		FPMIWindow win = (FPMIWindow)arg0.getSource();
		ScreenViewsRecord record = new ScreenViewsRecord();
		record.setUsername(SecurityUtilities.getUsername());
		record.setScreenName(win.getName());
		//record.setScreenPath(arg0.getPath());
		record.setScreenTitle(win.getTitle());
		record.setProjectName(this._context.getProject().getName());
		record.setAction(Constants.SCREEN_CLOSE);
		try {
			//log to the database
			rpc.storeScreenViewedInformation(record);
		} catch (Exception e) {
			
			logger.info(e.toString());
		}
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent arg0) {
		
		
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent arg0) {
		
		
	}

	@Override
	public void shutdown() {
		super.shutdown();
		vision.removeWindowListener(this); 
	}

	/*added by YM  : 13-June-2016 
	 * (non-Javadoc)
	 * @see com.inductiveautomation.vision.api.client.AbstractClientModuleHook#notifyActivationStateChanged(com.inductiveautomation.ignition.common.licensing.LicenseState)
	 *
	 *Function to handle when LIcense state changes.
	 */
	@Override
	public void notifyActivationStateChanged(LicenseState licenseState) {
		
		super.notifyActivationStateChanged(licenseState);
		
		ModIALicenseManager manager = ModIALicenseManager.getInstance();
		manager.setLicenseState(licenseState);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if(Constants.licenseCheckEnabled)
		{
			if(licenseState.getLicenseMode() == LicenseMode.Trial)
			{
	//			if(licenseState.isTrialExpired())
	//			{
					System.out.println("IA module : Please check the license.");
	//			}
	//			else
	//			{
	//				//call startup methods
	//				startTasks();
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
								manager.setExpirationDate(expirationDate);
								
								if (currentdate.compareTo(expirationDate) <= 0)
								{
									startTasks();
								}
								else
								{
									System.out.println("IA module : The trial expired.");
								}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					else
					{
						//dateStr not found means there is activated license without expiration date set , i.e. valid license so proceed .
						startTasks();
					}
					
				}
				else
				{
					System.out.println("IA module : Please check the trial license. Details not found.");
				}
				
			}
		}
		else
		{
			startTasks();
		}
	}
	
}
