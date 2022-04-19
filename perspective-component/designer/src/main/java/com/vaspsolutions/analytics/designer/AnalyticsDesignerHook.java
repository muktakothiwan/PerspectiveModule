package com.vaspsolutions.analytics.designer;

import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vaspsolutions.analytics.client.IgnitionAnalyticsComponent;
import com.vaspsolutions.analytics.common.ClientRecord;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.HTTPRequestor;
import com.vaspsolutions.analytics.common.ModIAConfigurationException;
import com.vaspsolutions.analytics.common.ModIALicenseManager;
import com.vaspsolutions.analytics.common.ModuleRPC;
import com.vaspsolutions.analytics.common.component.display.Image;
import org.json.JSONObject;

import com.inductiveautomation.factorypmi.application.FPMISystem;
import com.inductiveautomation.factorypmi.application.script.builtin.SecurityUtilities;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseMode;
import com.inductiveautomation.ignition.common.licensing.LicenseRestriction;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry;
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface;
import com.inductiveautomation.vision.api.client.VisionClientInterface;
import com.inductiveautomation.vision.api.designer.VisionDesignerInterface;
import com.inductiveautomation.vision.api.designer.palette.JavaBeanPaletteItem;
import com.inductiveautomation.vision.api.designer.palette.Palette;
import com.inductiveautomation.vision.api.designer.palette.PaletteItemGroup;


/**
 * The 'hook' class for the designer scope of the module.  Registered in the ignitionModule configuration of the
 * root build.gradle file.
 */
public class AnalyticsDesignerHook extends AbstractDesignerModuleHook {

	private static final LoggerEx logger = LoggerEx.newBuilder().build(Constants.MODULE_LOG_NAME);

	static {
		BundleUtil.get()
		.addBundle("analyticscomponents", AnalyticsDesignerHook.class.getClassLoader(), "analyticscomponents");
	}

	public AnalyticsDesignerHook() {
		logger.info("Registering Analytics component in Designer!");
	}
	private DesignerContext context;
	private DesignerComponentRegistry registry;
	private VisionClientInterface vision ;
	private ModuleRPC rpc; //store an instance of Module RPC class 
	@Override
	public void startup(DesignerContext context, LicenseState activationState) throws Exception {
		this.context = context;
		vision = (VisionClientInterface)context.getModule(VisionClientInterface.VISION_MODULE_ID);
		rpc = ModuleRPCFactory.create(Constants.MODULE_ID, ModuleRPC.class);
		ModIALicenseManager manager = ModIALicenseManager.getInstance();
		manager.setLicenseState(activationState);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if(Constants.licenseCheckEnabled)
		{
			if(activationState.getLicenseMode() == LicenseMode.Trial)
			{

				System.out.println("IA module : Please check the license .");

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
								init();
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
						init();

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
			init();			
		}
	}

	private void init() {
		logger.info("Initializing registry entrants...");
		
		PerspectiveDesignerInterface pdi = PerspectiveDesignerInterface.get(context);
		registry = pdi.getDesignerComponentRegistry();
		// register components to get them on the palette
		registry.registerComponent(Image.DESCRIPTOR);

		
		//registering vision component
		context.addBeanInfoSearchPath("com.vaspsolutions.analytics.beaninfos");
		VisionDesignerInterface sdk = (VisionDesignerInterface) context.getModule(VisionDesignerInterface.VISION_MODULE_ID);
		
		
		if (sdk != null) {
			Palette palette = sdk.getPalette();
			PaletteItemGroup group = palette.addGroup("Analytics Module");
			group.addPaletteItem(new JavaBeanPaletteItem(IgnitionAnalyticsComponent.class));
		}	
		
		//end registering vision ocmponent
		ClientRecord cRec = new ClientRecord();



		try {
			cRec.setHostInternalIP(InetAddress.getLocalHost().getHostAddress());
			cRec.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
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
		cRec.setProject(this.context.getProject().getName());
		cRec.setClientContext(2);

		//get the screen resolution
		Toolkit toolKit = Toolkit.getDefaultToolkit();

		String sResolution = "" + (int)toolKit.getScreenSize().getWidth() + " x " + (int)toolKit.getScreenSize().getHeight();
		cRec.setScreenResolution(sResolution.trim() );

		//call the service to get External IP 
		HTTPRequestor req = new HTTPRequestor();
		try
		{
			String extIP = req.callService("http://checkip.amazonaws.com/" + cRec.getHostInternalIP());
			//		System.out.println("calling service");
			if(extIP != null  && extIP.length() <= 15)
			{

				cRec.setHostExternalIP(extIP);
				//call a web service to get location , latitude and longitude information if location info is not available
				if(rpc.IfLocationExists(cRec.getHostInternalIP(), cRec.getHostExternalIP()) != true)
				{
					try {
						//
						//					System.out.println("location if ");
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


					} catch (Exception e) {
						cRec.setHostExternalIP("00.00.00.00");
						cRec.setCity("Unknown");
						e.printStackTrace();
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
				//			System.out.println("External IP: "+extIP);
				//			extIP = "";
			}
			//		System.out.println("after getting location");
		}catch(Exception e)
		{
			cRec.setHostExternalIP("00.00.00.00");
			cRec.setCity("Unknown");
			e.printStackTrace();
		}


		try {
			//			System.out.println("before call to store client information");
			rpc.storeClientInformation(cRec);
			//			System.out.println("after call to store client information");
		} catch (ModIAConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	@Override
	public void shutdown() {
		removeComponents();
	}

	private void removeComponents() {
		registry.removeComponent(Image.COMPONENT_ID);

	}


}
