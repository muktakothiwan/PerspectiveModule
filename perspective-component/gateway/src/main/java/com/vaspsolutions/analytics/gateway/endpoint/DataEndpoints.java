package com.vaspsolutions.analytics.gateway.endpoint;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;


import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.json.JSONObject;

import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.gson.Gson;
import com.inductiveautomation.ignition.common.gson.GsonBuilder;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.common.tags.model.TagProvider;
import com.inductiveautomation.ignition.common.tags.model.TagProviderInformation;
import com.inductiveautomation.ignition.common.user.User;
import com.inductiveautomation.ignition.common.util.Futures;
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.tags.model.GatewayTagManager;
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext;
import com.vaspsolutions.analytics.common.ActiveUsersInfo;
import com.vaspsolutions.analytics.common.AlarmsInformation;
import com.vaspsolutions.analytics.common.BarChartInfo;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ContentsData;
import com.vaspsolutions.analytics.common.CurrentOverview;
import com.vaspsolutions.analytics.common.DevicesInformation;
import com.vaspsolutions.analytics.common.GroupReportRecord;
import com.vaspsolutions.analytics.common.LineChartInfo;
import com.vaspsolutions.analytics.common.OverviewInformation;
import com.vaspsolutions.analytics.common.ProjectInfo;
import com.vaspsolutions.analytics.common.ReportActiveUsersInfo;
import com.vaspsolutions.analytics.common.ReportAlarmSummary;
import com.vaspsolutions.analytics.common.ReportAlarmsRingChart;
import com.vaspsolutions.analytics.common.ReportAlarmsTable;
import com.vaspsolutions.analytics.common.ReportBarChartInfo;
import com.vaspsolutions.analytics.common.ReportDataPoint;
import com.vaspsolutions.analytics.common.ScreenDepthTableRealtime;
import com.vaspsolutions.analytics.common.ScreensCount;
import com.vaspsolutions.analytics.common.SevenDaysMinMax;
import com.vaspsolutions.analytics.common.SlidesData;
import com.vaspsolutions.analytics.common.TopRecord;
import com.vaspsolutions.analytics.common.UserListData;
import com.vaspsolutions.analytics.common.UserLocationTable;
import com.vaspsolutions.analytics.common.UserLocations;
import com.vaspsolutions.analytics.common.UserScreenViewData;
import com.vaspsolutions.analytics.common.UserSessionDetailData;
import com.vaspsolutions.analytics.common.UserVisitsPanelData;
import com.vaspsolutions.analytics.common.UsersOverviewInformation;
import com.vaspsolutions.analytics.gateway.ModuleRPCImpl;


/**
 * Class containing dynamic data 'routes' or 'endpoints'.
 */
public class DataEndpoints {
	private final RouteGroup routes;
	private final PerspectiveContext context;
	private ModuleRPCImpl _gcRPC;
	// how long to hold cached value before allowing new count calculation
	private static final long CACHE_DURATION_MS = 50000L;
	private static AtomicReference<Long> tagCount = new AtomicReference<>(0L);

	private static AtomicReference<Long> timestampOfLastCount = new AtomicReference<>(0L);

	Logger log = LogManager.getLogger(Constants.MODULE_LOG_NAME); 

	public DataEndpoints(PerspectiveContext context, RouteGroup routes) {
		this.routes = routes;
		this.context = context;
		this._gcRPC = new ModuleRPCImpl(context.getGatewayContext());
		// creates a new data route reachable at host:port/main/data/radcomppnents/component/tagcount
		routes.newRoute("/component/tagcount")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchTagCount)
		.mount();


		routes.newRoute("/component/dashboarddata")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchDashboardData)
		.mount();

		routes.newRoute("/component/getprojectslist")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchProjectsList)
		.mount();

		routes.newRoute("/component/getprojectdetails")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchProjectDetails)
		.mount();

		routes.newRoute("/component/getprojectsnotadded")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchProjectsNotAdded)
		.mount();

		routes.newRoute("/component/deleteproject")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::deleteProject)
		.mount();

		routes.newRoute("/component/addproject")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::addProject)
		.mount();

		routes.newRoute("/component/realtimedata")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchRealTimeData)
		.mount();

		routes.newRoute("/component/realtimelinegraphdata")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchRealTimeLineGraphData)
		.mount();

		routes.newRoute("/component/usersdata")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchUsersData)
		.mount();

		routes.newRoute("/component/reportoverviewbydate")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_OverviewByDate)
		.mount();

		routes.newRoute("/component/reportcities")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_Cities)
		.mount(); 

		routes.newRoute("/component/reportgroups")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_Groups)
		.mount(); 

		routes.newRoute("/component/reporttopscreens")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_TopScreens)
		.mount(); 

		routes.newRoute("/component/reportbouncerate")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_BounceRate)
		.mount(); 

		routes.newRoute("/component/reportdevicetypes")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_DeviceTypes)
		.mount(); 

		routes.newRoute("/component/reportplatforms")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_Platforms)
		.mount(); 

		routes.newRoute("/component/reportbrowsers")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_Browsers)
		.mount(); 

		routes.newRoute("/component/reportscreenresolutions")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_ScreenResolutions)
		.mount(); 

		routes.newRoute("/component/reportactionspervisit")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_ActionsPerVisit)
		.mount(); 

		routes.newRoute("/component/reportvisitduration")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_VisitDuration)
		.mount(); 

		routes.newRoute("/component/reportengagement")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_Engagement)
		.mount(); 

		routes.newRoute("/component/reportfrequency")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_Frequency)
		.mount(); 

		routes.newRoute("/component/reportrecency")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_Recency)
		.mount(); 

		routes.newRoute("/component/reportactiveusers")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_ActiveUsers)
		.mount(); 

		routes.newRoute("/component/reportalarmsummary")
		.type(RouteGroup.TYPE_JSON)
		.handler(this::fetchReport_AlarmSummary)
		.mount();
	}

	/**
	 * Returns a simple json object in form of
	 * <pre>
	 *     {
	 *         "tagCount": &lt;number&gt;
	 *     }
	 * </pre>
	 *
	 * Tag counts are cached and re-caclcated no more often than {@link DataEndpoints#CACHE_DURATION_MS}.
	 *
	 * Note: an 'ideal' implementation of this sort would require some additional concurrency handling for optimal
	 * performance and safety, but was skipped to keep example focused on big picture functionality.
	 */
	private JsonObject fetchTagCount(RequestContext req, HttpServletResponse res) {
		// if we've exceeded our throttle duration, update count
		if (System.currentTimeMillis() - timestampOfLastCount.get() > CACHE_DURATION_MS) {
			timestampOfLastCount.set(System.currentTimeMillis());

			GatewayContext context = req.getGatewayContext();
			GatewayTagManager tagManager = context.getTagManager();
			List<TagProvider> providers = tagManager.getTagProviders();

			long count = providers.stream()
					.map(TagProvider::getStatusInformation)
					.map(Futures::getSafe)
					.filter(Objects::nonNull)
					.map(TagProviderInformation::getTagCount)
					.reduce(Integer::sum)
					.map(Long::valueOf)
					.orElse(0L); 



			tagCount.set(count);
		}

		JsonObject json = new JsonObject();
		json.addProperty("tagCount", tagCount.get());
		return json;
	}



	private JsonObject fetchDashboardData(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));
		log.error("DEP : fetchDashboardData: allProjects : " + allProjects);

		//retrieve overview section information and add to return JSON
		OverviewInformation oInfo = _gcRPC.getOverview(duration, projectName, allProjects);
		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();
		String _overviwJSON = gson.toJson(oInfo);
		returnJSON.addProperty("overview", _overviwJSON);

		//retrieve slider section information and add to return JSON
		OverviewInformation sliderInfo = _gcRPC.getYesterdayOverviewForSlider(duration, projectName, allProjects);

		SlidesData _slides = new SlidesData();
		if(oInfo != null && sliderInfo != null)
		{
			float bounceRate = (oInfo.getBounceRate()) * 100;

			//set the slide 1 values
			_slides.slide1Percent = oInfo.getNoOfSessions() + "";
			if(sliderInfo.getNoOfSessions() == 0)
			{
				_slides.slide1BottomText = "+" + Math.round(( oInfo.getNoOfSessions() * 100 ))+ "%";
				_slides.slide1Color = "green";
			}

			else
			{
				float sessionsPercent =(( (float)oInfo.getNoOfSessions() - sliderInfo.getNoOfSessions())/ sliderInfo.getNoOfSessions());
				sessionsPercent = sessionsPercent * 100;


				if(oInfo.getNoOfSessions()  >= sliderInfo.getNoOfSessions())
				{

					_slides.slide1Color = "green";
					_slides.slide1BottomText = "+" + Math.round( sessionsPercent) + "%";
				}
				else
				{

					_slides.slide1Color = "red";
					if(Math.round(sessionsPercent) >= 0)
					{
						_slides.slide1BottomText = "-" + Math.round( sessionsPercent) + "%";
					}
					else
					{
						_slides.slide1BottomText = Math.round( sessionsPercent) + "%";
					}
				}
			}

			//set the slide 2 values
			_slides.slide2Percent = oInfo.getNoOfActiveUsers() + "";
			if(sliderInfo.getNoOfActiveUsers() == 0)
			{
				_slides.slide2BottomText = "+" + Math.round(oInfo.getNoOfActiveUsers() * 100) + "%";
				_slides.slide2Color = "green";
			}
			else
			{

				if(oInfo.getNoOfActiveUsers()  >= sliderInfo.getNoOfActiveUsers())
				{
					_slides.slide2Color = "green";
					_slides.slide2BottomText = "+" + Math.round( (((float)oInfo.getNoOfActiveUsers() - sliderInfo.getNoOfActiveUsers())/sliderInfo.getNoOfActiveUsers()) * 100) + "%";
				}
				else
				{
					_slides.slide2Color = "red";
					int actUsrs = Math.round( (((float)oInfo.getNoOfActiveUsers() - sliderInfo.getNoOfActiveUsers())/sliderInfo.getNoOfActiveUsers()) * 100);
					if(actUsrs >=0 )
					{
						_slides.slide2BottomText = "-" + actUsrs + "%";
					}
					else
					{
						_slides.slide2BottomText = actUsrs + "%";
					}
				}
			}

			//set the slide 3 values
			_slides.slide3Percent = oInfo.getNoOfScreenViews() + "";
			if(sliderInfo.getNoOfScreenViews() == 0)
			{
				_slides.slide3BottomText = "+"+Math.round(oInfo.getNoOfScreenViews() * 100) + "%";
				_slides.slide3Color = "green"; 
			}
			else
			{

				if(oInfo.getNoOfScreenViews() >= sliderInfo.getNoOfScreenViews())
				{
					_slides.slide3Color = "green"; 
					_slides.slide3BottomText = "+" +Math.round( ((((float)oInfo.getNoOfScreenViews() - sliderInfo.getNoOfScreenViews()) / sliderInfo.getNoOfScreenViews())) * 100 )+ "%";
				}
				else
				{
					int noSViews = Math.round( ((((float)oInfo.getNoOfScreenViews() - sliderInfo.getNoOfScreenViews()) / sliderInfo.getNoOfScreenViews())) * 100 );
					if(noSViews >= 0)
					{
						_slides.slide3BottomText =  "-" + noSViews+ "%";
					}
					else
					{
						_slides.slide3BottomText =  noSViews+ "%";
					}
					_slides.slide3Color = "red"; 
				}
			}

			//Set the slide 4 values 
			if(oInfo.getNoOfActiveUsers() == 0)
			{
				_slides.slide4Percent = "0 %";
				_slides.slide4Color = "green";
				_slides.slide4BottomText = "+ 0%";
			}
			else
			{

				_slides.slide4Percent =  (int) bounceRate + " %";
				if(sliderInfo.getBounceRate() == 0)
				{

					//bRate =  Math.round(((float) oInfo.getBounceRate() / oInfo.getNoOfActiveUsers()) * 100 );
					//_slide4.lblBottom.setText(String.format("%.2f",  ((float) oInfo.getBounceRate() / oInfo.getNoOfActiveUsers()) * 100) + "%");
					if(bounceRate >= 0)
					{
						_slides.slide4BottomText = "+" + (int)(bounceRate) * 100 + "%";
					}
					else
					{
						_slides.slide4BottomText = (int)bounceRate + "%";
					}
					_slides.slide4Color = "red";
				}
				else
				{
					if(oInfo.getBounceRate() <= sliderInfo.getBounceRate())
					{

						_slides.slide4Color = "green";
						_slides.slide4BottomText = Math.round(  (((float)oInfo.getBounceRate() - sliderInfo.getBounceRate())/sliderInfo.getBounceRate()) * 100) + "%";
					}
					else
					{
						int bRate = Math.round(  (((float)oInfo.getBounceRate() - sliderInfo.getBounceRate())/sliderInfo.getBounceRate()) * 100);
						_slides.slide4Color = "red";
						if(bRate >= 0)
						{
							_slides.slide4BottomText = "+" + bRate + "%";
						}
						else
						{
							_slides.slide4BottomText = bRate + "%";
						}
					}
				}
			}

			//set the slide 5 values
			float todaySession = 0;
			float yDaySession = 0;
			String strDuration = "";
			if(oInfo.getAvgSessionDuration() != null && oInfo.getAvgSessionDuration().length() > 0)
			{
				if(oInfo.getAvgSessionDuration() != null){
					_slides.slide5Percent = oInfo.getAvgSessionDuration() ;
				}
				strDuration = oInfo.getAvgSessionDuration();

				todaySession = ((Float.parseFloat(strDuration.split(":")[0])) * 3600) +
						((Float.parseFloat(strDuration.split(":")[1])) * 60)	+
						(Float.parseFloat(strDuration.split(":")[0]));

			}
			else
			{
				_slides.slide5Percent =  "00:00:00";
			}
			if(sliderInfo.getAvgSessionDuration() != null)
			{
				strDuration = sliderInfo.getAvgSessionDuration();
				if(strDuration != null && strDuration.length() > 0)
				{
					String splitted[] = strDuration.split(":");
					if(splitted.length >= 1 && splitted[0] != null)
					{
						String hourVal = "00";
						if(splitted[0].contains("-"))
						{
							hourVal = splitted[0].substring(splitted[0].length() - 2);
						}
						else
						{
							hourVal = splitted[0];
						}
						yDaySession = ((Float.parseFloat(hourVal)) * 3600);
					}
					if(splitted.length >= 2 && splitted[1] != null)
					{
						yDaySession = yDaySession + ((Float.parseFloat(splitted[1])) * 60);
					}
					if(splitted.length >= 3 && splitted[2] != null)
					{
						yDaySession = yDaySession + (Float.parseFloat(splitted[2]));
					}


				}
			}
			if(yDaySession != 0)
			{

				if(todaySession >= yDaySession)
				{

					_slides.slide5Color = "green";
					_slides.slide5BottomText = "+" + Math.round( ((float)(todaySession - yDaySession)/yDaySession) * 100 )+ "%";
				}
				else
				{
					int val =  Math.round( ((float)(todaySession - yDaySession)/yDaySession) * 100 );
					_slides.slide5Color = "red";
					if(val >= 0)
					{
						_slides.slide5BottomText =  "-" + val + "%";
					}
					else
					{
						_slides.slide5BottomText =  val + "%";
					}
				}
			}
			else
			{
				_slides.slide5BottomText =  "+" + Math.round(todaySession * 100) + "%";
				_slides.slide5Color = "green";
			}


			//set the slide 6 values
			_slides.slide6Percent =  (int)oInfo.getAverageScreensPerVisit() + "";
			if(sliderInfo.getAverageScreensPerVisit() == 0)
			{
				_slides.slide6BottomText = "+ 100%";
				_slides.slide6Color = "green";
			}
			else
			{
				if(Math.round(sliderInfo.getAverageScreensPerVisit()) != 0)
				{
					if(oInfo.getAverageScreensPerVisit() >= sliderInfo.getAverageScreensPerVisit() )
					{
						_slides.slide6BottomText = "+"+(Math.round(Math.round(oInfo.getAverageScreensPerVisit()) / Math.round(sliderInfo.getAverageScreensPerVisit())) * 100) + "%";
						_slides.slide6Color = "green";
					}
					else
					{
						int valAvg = Math.round((Math.round(oInfo.getAverageScreensPerVisit()) / Math.round(sliderInfo.getAverageScreensPerVisit())) * 100 );
						if(valAvg >= 0)
						{
							_slides.slide6BottomText =  "-" + valAvg + "%";
						}
						else
						{
							_slides.slide6BottomText = valAvg + "%";	
						}

						_slides.slide6Color = "red";
					}
				}
				else
				{
					_slides.slide6BottomText = "+100%";
					_slides.slide6Color = "green";
				}
			}

		}
		String _slidesJSON = gson.toJson(_slides);
		returnJSON.addProperty("slider", _slidesJSON);

		//****************************************************************************************************************
		//retrieve active users information , iterate and convert to JSON
		Dataset activeUsersData  = _gcRPC.getActiveUsersInformation(projectName, allProjects, duration);


		List<LineChartInfo> oneDayActiveUsersInfo = getActiveUsersInformation(duration, activeUsersData, 1);

		String _oneDayActiveUsersJSON = gson.toJson(oneDayActiveUsersInfo);
		returnJSON.addProperty("onedayactiveusers", _oneDayActiveUsersJSON);

		List<LineChartInfo> sevenDaysActiveUsersInfo = getActiveUsersInformation(duration, activeUsersData, 2);

		String _sevenDaysActiveUsersJSON = gson.toJson(sevenDaysActiveUsersInfo);
		returnJSON.addProperty("sevendaysactiveusers", _sevenDaysActiveUsersJSON);

		List<LineChartInfo> fourteenDaysActiveUsersInfo = getActiveUsersInformation(duration, activeUsersData, 3);

		String _fourteenDaysActiveUsersJSON = gson.toJson(fourteenDaysActiveUsersInfo);
		returnJSON.addProperty("fourteendaysactiveusers", _fourteenDaysActiveUsersJSON);

		//****************************************************************************************************************
		//get active users counts
		ActiveUsersInfo aInfo = _gcRPC.getActiveUsersCounts(projectName, allProjects, duration);
		String _activeUsersInfoJSON = gson.toJson(aInfo);
		returnJSON.addProperty("activeuserscounts", _activeUsersInfoJSON);

		//****************************************************************************************************************
		//retrive alarm counts
		Dataset _alarms = _gcRPC.getAlarms(duration, projectName, allProjects);
		int alarmsDSSize, r;
		int medAlarms = 0, highAlarms = 0, criticalAlarms = 0;
		String alarmPriority;
		if(_alarms != null)
		{
			alarmsDSSize = _alarms.getRowCount();

			for(r=0; r<alarmsDSSize; r++)
			{
				alarmPriority = _alarms.getValueAt(r, 0).toString();

				if(alarmPriority.compareToIgnoreCase("Medium") == 0)
				{
					medAlarms = (int)Double.parseDouble(_alarms.getValueAt(r, 1).toString());
				}
				else if(alarmPriority.compareToIgnoreCase("High") == 0)
				{
					highAlarms = (int)Double.parseDouble(_alarms.getValueAt(r, 1).toString());
				}
				else if(alarmPriority.compareToIgnoreCase("Critical") == 0)
				{
					criticalAlarms = (int)Double.parseDouble(_alarms.getValueAt(r, 1).toString());
				}
			}
		}

		returnJSON.addProperty("mediumAlarms", medAlarms);
		returnJSON.addProperty("highAlarms", highAlarms);
		returnJSON.addProperty("criticalAlarms", criticalAlarms);

		//****************************************************************************************************************

		//retrieve alarm clear time information
		Dataset _alarmsClearTime = _gcRPC.getAlarmsClearTime(duration, projectName, allProjects);
		int alarmsClearDSSize;
		String medAlarmsClr = "", highAlarmsclr = "", criticalAlarmsClr = "";
		if(_alarmsClearTime != null)
		{
			alarmsClearDSSize = _alarmsClearTime.getRowCount();
			for(r=0; r<alarmsClearDSSize; r++)
			{
				if(_alarmsClearTime.getValueAt(r, 0) != null)
				{
					alarmPriority = _alarmsClearTime.getValueAt(r, 0).toString();
					if(alarmPriority.compareToIgnoreCase("Medium") == 0) //medium
					{
						medAlarmsClr = _alarmsClearTime.getValueAt(r, 1).toString();

						medAlarmsClr = medAlarmsClr.substring(0, 8);
					}
					else if(alarmPriority.compareToIgnoreCase("High") == 0) //high
					{
						highAlarmsclr = _alarmsClearTime.getValueAt(r, 1).toString();
						highAlarmsclr = highAlarmsclr.substring(0, 8);
					}
					else if(alarmPriority.compareToIgnoreCase("Critical") == 0) //critical
					{

						criticalAlarmsClr = _alarmsClearTime.getValueAt(r, 1).toString();
						criticalAlarmsClr = criticalAlarmsClr.substring(0, 8);
					}
				}
			}
		}
		returnJSON.addProperty("mediumAlarmsClearTime", medAlarmsClr);
		returnJSON.addProperty("highAlarmsClearTime", highAlarmsclr);
		returnJSON.addProperty("criticalAlarmsClearTime", criticalAlarmsClr);

		//****************************************************************************************************************
		//get yesterday overview information
		OverviewInformation yInfo = _gcRPC.getYesterdayOverview( Constants.YESTERDAY, projectName, allProjects);
		String _yOverviwJSON = gson.toJson(yInfo);
		returnJSON.addProperty("yesterdayoverview", _yOverviwJSON);
		//do something to create SLIDER DATA
		//get no of new users 
		int noOfNewUsers = _gcRPC.getNumberOfNewUsers(Constants.YESTERDAY, projectName, allProjects);
		returnJSON.addProperty("noofnewusers", noOfNewUsers);

		//****************************************************************************************************************

		//retrieve devices section information and add to return JSON
		DevicesInformation _devices = _gcRPC.getDeviceInformation(duration, projectName, allProjects);
		String _devicesJSON = gson.toJson(_devices);
		returnJSON.addProperty("devices", _devicesJSON);

		//****************************************************************************************************************
		//get top oS
		Dataset operatingSystems = _gcRPC.getTopOperatingSystems( duration, projectName, allProjects);
		List<TopRecord> topOs = new ArrayList<TopRecord>();
		if(operatingSystems != null && operatingSystems.getRowCount() > 0)
		{
			for (int i=0; i<operatingSystems.getRowCount() && i<4; i++)
			{
				TopRecord oneRec = new TopRecord();
				oneRec.name = operatingSystems.getValueAt(i, "OS_NAME").toString();
				oneRec.usersCount = Integer.parseInt(operatingSystems.getValueAt(i, "Users").toString());
				topOs.add(oneRec);
			}
		}

		String _topOSJSON = gson.toJson(topOs);
		returnJSON.addProperty("topOS", _topOSJSON);

		//****************************************************************************************************************
		//get browser information
		Dataset browserData = _gcRPC.getBrowserInformation(duration, projectName, allProjects);
		List<TopRecord> topBrowser = new ArrayList<TopRecord>();
		if(browserData != null && browserData.getRowCount() > 0)
		{
			for (int i=0; i<browserData.getRowCount() && i<4; i++)
			{
				TopRecord oneRec = new TopRecord();
				oneRec.name = browserData.getValueAt(i, "browser_name").toString();
				oneRec.usersCount = Integer.parseInt(browserData.getValueAt(i, "bCount").toString());
				topBrowser.add(oneRec);
			}
		}

		String _topBrowserJSON = gson.toJson(topBrowser);
		returnJSON.addProperty("topBrowser", _topBrowserJSON);

		//****************************************************************************************************************
		//retrieve frequency information
		Dataset datafreqInfo = _gcRPC.getFrequencyInformation( projectName, allProjects, duration);
		List<BarChartInfo> _listfreqInfo = new ArrayList<BarChartInfo>(); 
		BarChartInfo freqInfo = new BarChartInfo();

		if(datafreqInfo != null)
		{
			int users1Session = 0, screens1Session = 0;
			int users2_5Session = 0, screens2_5Session = 0;
			int users6_10Session = 0, screens6_10Session = 0;
			int users11_25Session = 0, screens11_25Session = 0;
			int users26_50Session = 0, screens26_50Session = 0;
			int users50PlusSession = 0, screens50PlusSession = 0;
			int noOfRecords = 0;

			int tempUserVal = 0, tempScreenVal = 0;
			noOfRecords = datafreqInfo.getRowCount();

			//build the json data for bar chart
			for(int i=0; i<noOfRecords; i++)
			{
				tempUserVal = 0;
				tempScreenVal = 0;
				if(datafreqInfo.getValueAt(i, 1) != null)
				{
					tempUserVal = (int)Float.parseFloat(datafreqInfo.getValueAt(i, 1).toString());
				}
				if(datafreqInfo.getValueAt(i, 3) != null)
				{
					tempScreenVal = (int)Float.parseFloat(datafreqInfo.getValueAt(i, 3).toString());
				}

				if(tempUserVal == 1)
				{
					users1Session++;
					screens1Session = screens1Session + tempScreenVal;
				}
				else if(tempUserVal >= 2 && tempUserVal <=5)
				{
					users2_5Session++;
					screens2_5Session = screens2_5Session + tempScreenVal;
				}
				else if(tempUserVal >= 6 && tempUserVal <=10)
				{
					users6_10Session++;
					screens6_10Session = screens6_10Session + tempScreenVal;
				}
				else if(tempUserVal >= 11 && tempUserVal <=25)
				{
					users11_25Session++;
					screens11_25Session = screens11_25Session + tempScreenVal;
				}
				else if(tempUserVal >= 26 && tempUserVal <=50)
				{
					users26_50Session++;
					screens26_50Session = screens26_50Session + tempScreenVal;
				}
				else if(tempUserVal >= 51)
				{
					users50PlusSession++;
					screens50PlusSession = screens50PlusSession + tempScreenVal;
				}
			}

			if(noOfRecords > 0)
			{//
				freqInfo = new BarChartInfo();
				freqInfo.axislabel = "1 Session";
				freqInfo.series1 = users1Session;
				freqInfo.series2 = screens1Session;
				_listfreqInfo.add(freqInfo);

				freqInfo = new BarChartInfo();
				freqInfo.axislabel = "2-5 Sessions";
				freqInfo.series1 = users2_5Session;
				freqInfo.series2 = screens2_5Session;
				_listfreqInfo.add(freqInfo);

				freqInfo = new BarChartInfo();
				freqInfo.axislabel = "6-10 Sessions";
				freqInfo.series1 = users6_10Session;
				freqInfo.series2 = screens6_10Session;
				_listfreqInfo.add(freqInfo);

				freqInfo = new BarChartInfo();
				freqInfo.axislabel = "11-25 Sessions";
				freqInfo.series1 = users11_25Session;
				freqInfo.series2 = screens11_25Session;
				_listfreqInfo.add(freqInfo);

				freqInfo = new BarChartInfo();
				freqInfo.axislabel = "26-50 Sessions";
				freqInfo.series1 = users26_50Session;
				freqInfo.series2 = screens26_50Session;
				_listfreqInfo.add(freqInfo);

				freqInfo = new BarChartInfo();
				freqInfo.axislabel = "50+ Sessions";
				freqInfo.series1 = users50PlusSession;
				freqInfo.series2 = screens50PlusSession;
				_listfreqInfo.add(freqInfo);


			}
		}   
		String _freqInfoJSON = gson.toJson(_listfreqInfo);
		returnJSON.addProperty("frequency", _freqInfoJSON); 

		//****************************************************************************************************************
		//retrieve recency information
		Dataset recencyData = _gcRPC.getDaysSinceLastLoginPerUser( projectName, allProjects, duration);
		List<BarChartInfo> _listRecencyInfo = new ArrayList<BarChartInfo>(); 
		BarChartInfo recInfo = new BarChartInfo();
		if(recencyData != null)
		{
			int users1Day = 0, screens1Day = 0;
			int users2_5Days = 0, screens2_5days = 0;
			int users6_10Days = 0, screens6_10Days = 0;
			int users11_25Days = 0, screens11_25Days = 0;
			int users26_50Days = 0, screens26_50days = 0;
			int users50PlusDays = 0, screens50PlusDays = 0;
			int noOfRecords = 0;

			int tempUserVal = 0, tempScreenVal = 0;
			noOfRecords = recencyData.getRowCount();

			//build the json data for bar chart
			for(int i=0; i<noOfRecords; i++)
			{
				tempUserVal = 0;
				tempScreenVal = 0;
				if(recencyData.getValueAt(i, 1) != null)
				{
					tempUserVal = (int)Float.parseFloat(recencyData.getValueAt(i, 1).toString());
				}

				if(recencyData.getValueAt(i, 2) != null)
				{
					tempScreenVal = (int)Float.parseFloat(recencyData.getValueAt(i, 2).toString());
				}

				if(tempUserVal <= 1)
				{
					users1Day++;
					screens1Day = screens1Day + tempScreenVal;
				}
				else if(tempUserVal >= 2 && tempUserVal <=5)
				{
					users2_5Days++;
					screens2_5days = screens2_5days + tempScreenVal;
				}
				else if(tempUserVal >= 6 && tempUserVal <=10)
				{
					users6_10Days++;
					screens6_10Days = screens6_10Days + tempScreenVal;
				}
				else if(tempUserVal >= 11 && tempUserVal <=25)
				{
					users11_25Days++;
					screens11_25Days = screens11_25Days + tempScreenVal;
				}
				else if(tempUserVal >= 26 && tempUserVal <=50)
				{
					users26_50Days++;
					screens26_50days = screens26_50days + tempScreenVal;
				}
				else if(tempUserVal >= 51)
				{
					users50PlusDays++;
					screens50PlusDays = screens50PlusDays + tempScreenVal;
				}
			}

			if(noOfRecords > 0)
			{

				recInfo = new BarChartInfo();
				recInfo.axislabel = "1 Day";
				recInfo.series1 = users1Day;
				recInfo.series2 = screens1Day;
				_listRecencyInfo.add(recInfo);

				recInfo = new BarChartInfo();
				recInfo.axislabel = "2-5 Days";
				recInfo.series1 = users2_5Days;
				recInfo.series2 = screens2_5days;
				_listRecencyInfo.add(recInfo);

				recInfo = new BarChartInfo();
				recInfo.axislabel = "6-10 Days";
				recInfo.series1 = users6_10Days;
				recInfo.series2 = screens6_10Days;
				_listRecencyInfo.add(recInfo);

				recInfo = new BarChartInfo();
				recInfo.axislabel = "11-25 Days";
				recInfo.series1 = users11_25Days;
				recInfo.series2 = screens11_25Days;
				_listRecencyInfo.add(recInfo);

				recInfo = new BarChartInfo();
				recInfo.axislabel = "26-50 Days";
				recInfo.series1 = users26_50Days;
				recInfo.series2 = screens26_50days;
				_listRecencyInfo.add(recInfo);

				recInfo = new BarChartInfo();
				recInfo.axislabel = "50+ Days";
				recInfo.series1 = users50PlusDays;
				recInfo.series2 = screens50PlusDays;
				_listRecencyInfo.add(recInfo);

			}
		} 

		String _recencyInfoJSON = gson.toJson(_listRecencyInfo);
		returnJSON.addProperty("recency", _recencyInfoJSON); 

		//****************************************************************************************************************
		//retrieve engagement information
		Dataset engagementData = _gcRPC.getEngagementInformation( projectName, allProjects, duration);
		List<BarChartInfo> _listEngagementInfo = new ArrayList<BarChartInfo>(); 
		BarChartInfo engagementInfo = new BarChartInfo();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat origFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");

		if(engagementData != null)
		{

			int users1Session = 0, screens1Session = 0;
			int users2_5Session = 0, screens2_5Session = 0;
			int users6_10Session = 0, screens6_10Session = 0;
			int users11_25Session = 0, screens11_25Session = 0;
			int users26_50Session = 0, screens26_50Session = 0;
			int users50PlusSession = 0, screens50PlusSession = 0;
			int noOfRecords = 0;

			Double tempsessionVal = 0.0;
			int tempUserVal = 0, tempScreenVal = 0;
			noOfRecords = engagementData.getRowCount();
			Date tempDate = null;


			//build the json data for bar chart
			for(int i=0; i<noOfRecords; i++)
			{
				tempsessionVal = 0.0;
				tempScreenVal = 0;
				tempUserVal = 0;
				tempDate = null;
				String tempDateStr = "";
				if(engagementData.getValueAt(i, 0) != null)
				{
				try {
//						tempDate = origFormat.parse(engagementData.getValueAt(i, 0).toString());
//						tempDateStr =  sdf.format(tempDate);
						tempDateStr = engagementData.getValueAt(i, 0).toString();
							String timeVal[] = tempDateStr.split(":");

							tempsessionVal =  60 * (Double.parseDouble(timeVal[0])) 
									+ Double.parseDouble(timeVal[1]) 
									+ (1/60.0) * Double.parseDouble(timeVal[2]);
						
					} catch (Exception e) {
						log.error("parse error : " + e);
						
					}

				}
				if(engagementData.getValueAt(i, 1) != null)
				{
					tempUserVal = Integer.parseInt(engagementData.getValueAt(i, 1).toString());
				}
				if(engagementData.getValueAt(i, 2) != null)
				{
					tempScreenVal = (int)Float.parseFloat(engagementData.getValueAt(i, 2).toString());
				}

				if(tempsessionVal <= 5.0)
				{
					users1Session = users1Session + tempUserVal;
					screens1Session = screens1Session + tempScreenVal;
				}
				else if(tempsessionVal > 5.0 && tempsessionVal <=10.0)
				{
					users2_5Session += tempUserVal;
					screens2_5Session = screens2_5Session + tempScreenVal;
				}
				else if(tempsessionVal > 10.0 && tempsessionVal <=30.0)
				{
					users6_10Session += tempUserVal;
					screens6_10Session = screens6_10Session + tempScreenVal;
				}
				else if(tempsessionVal > 30.0 && tempsessionVal <=60.0)
				{
					users11_25Session += tempUserVal;
					screens11_25Session = screens11_25Session + tempScreenVal;
				}
				else if(tempsessionVal > 60.0 && tempsessionVal <=120.0)
				{
					users26_50Session += tempUserVal;
					screens26_50Session = screens26_50Session + tempScreenVal;
				}
				else if(tempsessionVal > 120.0)
				{
					users50PlusSession += tempUserVal;
					screens50PlusSession = screens50PlusSession + tempScreenVal;
				}
			}

			if(noOfRecords > 0)
			{
				engagementInfo = new BarChartInfo();
				engagementInfo.axislabel = "0-5 Mins";
				engagementInfo.series1 = users1Session;
				engagementInfo.series2 = screens1Session;
				_listEngagementInfo.add(engagementInfo);

				engagementInfo = new BarChartInfo();
				engagementInfo.axislabel = "6-10 Mins";
				engagementInfo.series1 = users2_5Session;
				engagementInfo.series2 = screens2_5Session;
				_listEngagementInfo.add(engagementInfo);

				engagementInfo = new BarChartInfo();
				engagementInfo.axislabel = "11-30 Mins";
				engagementInfo.series1 = users6_10Session;
				engagementInfo.series2 = screens6_10Session;
				_listEngagementInfo.add(engagementInfo);

				engagementInfo = new BarChartInfo();
				engagementInfo.axislabel = "31 Mins-1 Hour";
				engagementInfo.series1 = users11_25Session;
				engagementInfo.series2 = screens11_25Session;
				_listEngagementInfo.add(engagementInfo);

				engagementInfo = new BarChartInfo();
				engagementInfo.axislabel = "1-2 Hours";
				engagementInfo.series1 = users26_50Session;
				engagementInfo.series2 = screens26_50Session;
				_listEngagementInfo.add(engagementInfo);

				engagementInfo = new BarChartInfo();
				engagementInfo.axislabel = "2+ Hours";
				engagementInfo.series1 = users50PlusSession;
				engagementInfo.series2 = screens50PlusSession;
				_listEngagementInfo.add(engagementInfo);

			}

		}

		String _engagementInfoJSON = gson.toJson(_listEngagementInfo);
		returnJSON.addProperty("screenViews", _engagementInfoJSON); 
		//****************************************************************************************************************
		//retrieve screen depth information
		Dataset screenDepthData = _gcRPC.getEngagementInformationScreenDepth( projectName, allProjects, duration);
		List<BarChartInfo> _listScreenDepthInfo = new ArrayList<BarChartInfo>(); 
		BarChartInfo screenDepthInfo = new BarChartInfo();

		if(screenDepthData != null)
		{

			int sessions0_2Screens = 0, screens0_2 = 0;
			int sessions3_4Screens = 0, screens3_4= 0;
			int sessions5_7Screens = 0, screens5_7 = 0;
			int sessions8_10Screens = 0, screens8_10 = 0;
			int sessions11_15Screens = 0, screens11_15 = 0;
			int sessions16PlusScreens = 0, screens16Plus = 0;
			int noOfRecords = 0;

			int tempScreensVal = 0;
			int tempUserVal = 0;
			noOfRecords = screenDepthData.getRowCount();


			//build the json data for bar chart
			for(int i=0; i<noOfRecords; i++)
			{
				tempScreensVal = 0;
				tempUserVal = 0;

				if(screenDepthData.getValueAt(i, 0) != null)
				{
					tempUserVal = Integer.parseInt(screenDepthData.getValueAt(i, 0).toString());
				}
				if(screenDepthData.getValueAt(i, 1) != null)
				{
					tempScreensVal = (int)Double.parseDouble(screenDepthData.getValueAt(i, 1).toString());
				}
				if(tempScreensVal <= 2.0)
				{
					sessions0_2Screens = sessions0_2Screens + tempUserVal;
					screens0_2 = screens0_2 + tempScreensVal;
				}
				else if(tempScreensVal > 2.0 && tempScreensVal <=4.0)
				{
					sessions3_4Screens += tempUserVal;
					screens3_4 = screens3_4 + tempScreensVal;
				}
				else if(tempScreensVal > 4.0 && tempScreensVal <=7.0)
				{
					sessions5_7Screens += tempUserVal;
					screens5_7 = screens5_7 + tempScreensVal;
				}
				else if(tempScreensVal > 7.0 && tempScreensVal <=10.0)
				{
					sessions8_10Screens += tempUserVal;
					screens8_10 = screens8_10 + tempScreensVal;
				}
				else if(tempScreensVal > 10.0 && tempScreensVal <=15.0)
				{
					sessions11_15Screens += tempUserVal;
					screens11_15 = screens11_15 + tempScreensVal;
				}
				else if(tempScreensVal > 15.0)
				{
					sessions16PlusScreens += tempUserVal;
					screens16Plus = screens16Plus + tempScreensVal;
				}
			}

			if(noOfRecords > 0)
			{
				screenDepthInfo = new BarChartInfo();
				screenDepthInfo.axislabel = "0-2 screens";
				screenDepthInfo.series1 = sessions0_2Screens;
				screenDepthInfo.series2 = screens0_2;
				_listScreenDepthInfo.add(screenDepthInfo);

				screenDepthInfo = new BarChartInfo();
				screenDepthInfo.axislabel = "3-4 screens";
				screenDepthInfo.series1 = sessions3_4Screens;
				screenDepthInfo.series2 = screens3_4;
				_listScreenDepthInfo.add(screenDepthInfo);

				screenDepthInfo = new BarChartInfo();
				screenDepthInfo.axislabel = "5-7 screens";
				screenDepthInfo.series1 = sessions5_7Screens;
				screenDepthInfo.series2 = screens5_7;
				_listScreenDepthInfo.add(screenDepthInfo);

				screenDepthInfo = new BarChartInfo();
				screenDepthInfo.axislabel = "8-10 screens";
				screenDepthInfo.series1 = sessions8_10Screens;
				screenDepthInfo.series2 = screens8_10;
				_listScreenDepthInfo.add(screenDepthInfo);

				screenDepthInfo = new BarChartInfo();
				screenDepthInfo.axislabel = "11-15 screens";
				screenDepthInfo.series1 = sessions11_15Screens;
				screenDepthInfo.series2 = screens11_15;
				_listScreenDepthInfo.add(screenDepthInfo);

				screenDepthInfo = new BarChartInfo();
				screenDepthInfo.axislabel = "16+ screens";
				screenDepthInfo.series1 = sessions16PlusScreens;
				screenDepthInfo.series2 = screens16Plus;
				_listScreenDepthInfo.add(screenDepthInfo);

			} 
		} 
		String _screenDepthJSON = gson.toJson(_listScreenDepthInfo);
		returnJSON.addProperty("screenDepths", _screenDepthJSON);

		//****************************************************************************************************************
		//get data for all graphs to be shown in upper panel
		Dataset totalVisitsGraphData = _gcRPC.getTotalVisitsData( projectName, allProjects, duration);
		String _totalVisitsJSON = gson.toJson(getLineChartInfo(duration, totalVisitsGraphData, "Total Visits"));
		returnJSON.addProperty("totalVisits", _totalVisitsJSON);

		Dataset totalUsersGraphData = _gcRPC.getTotalUsersData( projectName, allProjects, duration);
		String _totalUsersJSON = gson.toJson(getLineChartInfo(duration, totalUsersGraphData, "Total Users"));
		returnJSON.addProperty("totalUsers", _totalUsersJSON);

		Dataset totalScreenViewsGraphData = _gcRPC.getTotalScreenViewsData(projectName, allProjects, duration);
		String _totalScreenViewsJSON = gson.toJson(getLineChartInfo(duration, totalScreenViewsGraphData, "Screen Views"));
		log.error("Dataendpoints : totalScreenViewsGraphData : " + _totalScreenViewsJSON);
		returnJSON.addProperty("totalScreenViews", _totalScreenViewsJSON);

		Dataset bounceRateGraphData = _gcRPC.getBounceRateData( projectName, allProjects, duration);
		String _bounceRateJSON = gson.toJson(getLineChartInfo(duration, bounceRateGraphData, "Bounce Rate"));
		returnJSON.addProperty("bounceRate", _bounceRateJSON);

		Dataset avgSessionGraphData = _gcRPC.getAvgSessionData(projectName, allProjects, duration);
		String _avgSessionJSON = gson.toJson(getLineChartInfo(duration, avgSessionGraphData, "Avg. Session"));
		returnJSON.addProperty("avgSession", _avgSessionJSON);

		Dataset avgScreenViewsGraphData = _gcRPC.getAvgScreenViewsData( projectName, allProjects, duration);
		String _avgScreenViewsJSON = gson.toJson(getLineChartInfo(duration, avgScreenViewsGraphData, "Avg. Screens/Visit"));
		returnJSON.addProperty("avgScreenViews", _avgScreenViewsJSON);
		//****************************************************************************************************************
		return returnJSON;
	}// End of fetchDashboardData
	//****************************************************************************************************************
	List<LineChartInfo> getActiveUsersInformation(int duration, Dataset activeUsersData, int dataSetCol)
	{
		List<LineChartInfo> listActiveUsersData = new ArrayList<LineChartInfo>();				
		LineChartInfo activeUsers  = new LineChartInfo();

		if(activeUsersData != null)
		{
			//create a line chart to show total actions and total sessions for various durations
			int noOfActiveUsers = activeUsersData.getRowCount();
			int startVal = 0;
			if(duration == Constants.LAST_365_DAYS)
			{
				Integer curMonth = Calendar.getInstance().get(Calendar.MONTH);

				if (curMonth == 11) {
					curMonth = 1;
				} else {
					curMonth = curMonth + 2;
				}	
				System.out.println("curMonth : " + curMonth);
				startVal = Constants.binarySearchOnDataset(4, curMonth, activeUsersData);

				if (startVal < 0) {
					startVal = 0;
				}

				System.out.println("startVal : " + startVal + ", dataset length: " + noOfActiveUsers );
				for (int i = startVal; i < noOfActiveUsers; i++) 
				{

					activeUsers  = new LineChartInfo();
					if(activeUsersData.getValueAt(i, dataSetCol) != null)
					{
						if(activeUsersData.getValueAt(i, 0) != null)
						{

							activeUsers.count = (int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString());
							activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
							activeUsers.seriesName = "users";
							//dsActiveUsers.addValue((int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString()), "users", activeUsersData.getValueAt(i, 0).toString());
						}
						else
						{

							activeUsers.count = 0;
							activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
							activeUsers.seriesName = "users";
							//dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
						}
					}
					else
					{
						activeUsers.count = 0;
						activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
						activeUsers.seriesName = "users";
						//dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
					}
					listActiveUsersData.add(activeUsers);
				}

				for (int i = 0; i < startVal; i++) 
				{
					activeUsers  = new LineChartInfo();
					if(activeUsersData.getValueAt(i, dataSetCol) != null)
					{
						if(activeUsersData.getValueAt(i, 0) != null)
						{
							activeUsers.count = (int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString());
							activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
							activeUsers.seriesName = "users";
						}
						//dsActiveUsers.addValue((int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString()), "users", activeUsersData.getValueAt(i, 0).toString());

						else
						{
							activeUsers.count = 0;
							activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
							activeUsers.seriesName = "users";
						}
						//dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());

					}
					else
					{
						activeUsers.count = 0;
						activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
						activeUsers.seriesName = "users";
					}
					//dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());

					listActiveUsersData.add(activeUsers);
				}
			}

			else
			{
				for(int i=0; i<noOfActiveUsers; i++)
				{activeUsers  = new LineChartInfo();
				if(activeUsersData.getValueAt(i, dataSetCol) != null)
				{
					if(activeUsersData.getValueAt(i, 0) != null)
					{
						activeUsers.count = (int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString());
						activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
						activeUsers.seriesName = "users";
					}
					//dsActiveUsers.addValue((int)Float.parseFloat(activeUsersData.getValueAt(i, dataSetCol).toString()), "users", activeUsersData.getValueAt(i, 0).toString());

					else
					{
						activeUsers.count = 0;
						activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
						activeUsers.seriesName = "users";
					}
					//dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());

				}
				else
				{
					activeUsers.count = 0;
					activeUsers.day = activeUsersData.getValueAt(i, 0).toString();
					activeUsers.seriesName = "users";
				}
				//						dsActiveUsers.addValue(0, "users", activeUsersData.getValueAt(i, 0).toString());
				listActiveUsersData.add(activeUsers);
				}
			}

		}
		return listActiveUsersData;
	}
	//*****************************************************************************************************************
	private List<LineChartInfo> getLineChartInfo(int duration, Dataset graphData, String seriesName)
	{
		List<LineChartInfo> _listTotalVisitsInfo = new ArrayList<LineChartInfo>(); 
		LineChartInfo totalVisitsInfo = new LineChartInfo();

		//retrieve the series data from gateway using RPC 
		if(graphData != null)
		{
			int noOfRows = graphData.getRowCount();
			int i = 0;
			int startVal = 0;

			if(duration == Constants.LAST_365_DAYS)
			{
				Integer curMonth = Calendar.getInstance().get(Calendar.MONTH);

				if (curMonth == 11) {
					curMonth = 1;
				} else {
					curMonth = curMonth + 2;
				}
				startVal = Constants.binarySearchOnDataset(2, curMonth, graphData);

				if (startVal < 0) {
					startVal = 0;
				}

				for (i = startVal; i < noOfRows; i++) 
				{
					totalVisitsInfo = new LineChartInfo();
					if(graphData.getValueAt(i, 1) != null)
					{
						totalVisitsInfo.count = (int)Float.parseFloat(graphData.getValueAt(i, 1).toString());
						totalVisitsInfo.day = graphData.getValueAt(i, 0).toString();
						totalVisitsInfo.seriesName = seriesName;

						//_lineData.addValue((int)Float.parseFloat(totalVisitsGraphData.getValueAt(i, 1).toString()), seriesName, graphData.getValueAt(i, 0).toString());
					}
					else
					{
						totalVisitsInfo.count = 0;
						totalVisitsInfo.day = graphData.getValueAt(i, 0).toString();
						totalVisitsInfo.seriesName = seriesName;

						//_lineData.addValue(0,seriesName, graphData.getValueAt(i, 0).toString());
					}

					_listTotalVisitsInfo.add(totalVisitsInfo);
				}

				for (i = 0; i < startVal; i++) 
				{
					totalVisitsInfo = new LineChartInfo();
					if(graphData.getValueAt(i, 1) != null)
					{
						totalVisitsInfo.count = (int)Float.parseFloat(graphData.getValueAt(i, 1).toString());
						totalVisitsInfo.day = graphData.getValueAt(i, 0).toString();
						totalVisitsInfo.seriesName = seriesName;

						//_lineData.addValue((int)Float.parseFloat(graphData.getValueAt(i, 1).toString()), seriesName, graphData.getValueAt(i, 0).toString());
					}
					else
					{
						totalVisitsInfo.count = 0;
						totalVisitsInfo.day = graphData.getValueAt(i, 0).toString();
						totalVisitsInfo.seriesName = seriesName;

						//_lineData.addValue(0,seriesName, graphData.getValueAt(i, 0).toString());
					}

					_listTotalVisitsInfo.add(totalVisitsInfo);
				}
			}
			else
			{
				for(i=0; i<noOfRows; i++)
				{
					totalVisitsInfo = new LineChartInfo();
					if(graphData.getValueAt(i, 1) != null)
					{
						totalVisitsInfo.count = (int)Float.parseFloat(graphData.getValueAt(i, 1).toString());
						totalVisitsInfo.day = graphData.getValueAt(i, 0).toString();
						totalVisitsInfo.seriesName = seriesName;

						//_lineData.addValue((int)Float.parseFloat(graphData.getValueAt(i, 1).toString()), seriesName, totalVisitsGraphData.getValueAt(i, 0).toString());
					}
					else
					{
						totalVisitsInfo.count = 0;
						totalVisitsInfo.day = graphData.getValueAt(i, 0).toString();
						totalVisitsInfo.seriesName = seriesName;

						//_lineData.addValue(0,seriesName, graphData.getValueAt(i, 0).toString());
					}
					_listTotalVisitsInfo.add(totalVisitsInfo);
				}
			}
		}
		return _listTotalVisitsInfo;
	}
	//*****************************************************************************************************************
	//*****************************************************************************************************************	
	private JsonObject fetchRealTimeData(RequestContext req, HttpServletResponse res) 
	{
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));
		String datasource = "";
		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();
		//*****************************************************************************************************************	
		//get 7 Day max min values in traffic panel to consider 0 logged in users case on a day
		Dataset _SevenDaysMaxMinData = _gcRPC.getSevenDaysMaxMin(datasource, Constants.LAST_SEVEN_DAYS, projectName, allProjects);
		int noOfDays = 0;
		int minVal = 0; int maxVal = 0, curVal = 0;

		if(_SevenDaysMaxMinData != null)
		{
			noOfDays = _SevenDaysMaxMinData.getRowCount();

			//get min and max values
			for(int i=0; i<noOfDays; i++)
			{
				curVal = (int)Float.parseFloat(_SevenDaysMaxMinData.getValueAt(i, 1).toString());
				if(curVal > maxVal)
				{
					maxVal = curVal;
				}

				if(curVal < minVal)
				{
					minVal = curVal;
				}
			}
		}

		if(noOfDays < 7)
		{
			minVal = 0;
		}
		SevenDaysMinMax _7daysMinMax = new SevenDaysMinMax();
		_7daysMinMax.maxVal = maxVal;
		_7daysMinMax.minVal = minVal;

		String _7daysMinMaxJSON = gson.toJson(_7daysMinMax);
		returnJSON.addProperty("traffic_sevendaysminmax", _7daysMinMaxJSON);

		//*****************************************************************************************************************	
		//get current overview information
		CurrentOverview currentoverview = _gcRPC.getCurrentOverview(projectName, allProjects);

		int totalScreens = 0;
		List<ScreensCount> screensPerUser = currentoverview.getScreenViewscountPerUser();
		if(screensPerUser != null)
		{
			int noOfRecords = screensPerUser.size();
			for(int i=0; i<noOfRecords; i++)
			{
				totalScreens = totalScreens + screensPerUser.get(i).getNoOfViews();	
			}
		}

		String _totalScreensJSON = gson.toJson(totalScreens);
		returnJSON.addProperty("overview_screenviews", _totalScreensJSON);

		int screenDepthVal = 0;
		if(totalScreens > 0 )
		{
			screenDepthVal = totalScreens/ currentoverview.getNoOfActiveSessions();
		}

		//get average depth of screen
		String _avgDepthOfScreenJSON = gson.toJson(screenDepthVal);
		returnJSON.addProperty("engagement_avgdepthofscreen", _avgDepthOfScreenJSON);


		String _noofactiveusersJSON = gson.toJson(currentoverview.getNoOfActiveUsers());
		returnJSON.addProperty("traffic_users", _noofactiveusersJSON);

		String _noofactivesessionsJSON = gson.toJson(currentoverview.getNoOfActiveSessions());
		returnJSON.addProperty("overview_activesessions", _noofactivesessionsJSON);

		String _noofactionsJSON = gson.toJson(currentoverview.getNoOfActionsByCurrentUsers());
		returnJSON.addProperty("overview_actions", _noofactionsJSON);


		float actionsPerSessionVal = 0;

		if(currentoverview.getNoOfActiveUsers() > 0)
		{
			actionsPerSessionVal = ((currentoverview.getNoOfActionsByCurrentUsers() ) / currentoverview.getNoOfActiveUsers());
		}	

		String _actionsPerSessionValJSON = gson.toJson(actionsPerSessionVal);
		returnJSON.addProperty("overview_actionspersessions", _actionsPerSessionValJSON);

		int avgSessionTime = 0;

		if( currentoverview.getNoOfActiveSessions() > 0)
		{
			avgSessionTime = (int) currentoverview.getActiveSessionLength() / currentoverview.getNoOfActiveSessions();
		}
		int minutesVal = 0;
		String avgSession = "";
		DecimalFormat dFormat = new DecimalFormat("00");

		if(avgSessionTime == 0)
		{
			avgSession = "00:00";
		}
		else if(avgSessionTime < 60)
		{
			avgSession = "00:" + dFormat.format(avgSessionTime);
		}
		else
		{
			minutesVal = avgSessionTime / 60;
			avgSessionTime = avgSessionTime % 60;

			avgSession =  dFormat.format(minutesVal) + ":" +  dFormat.format(avgSessionTime);
		}

		returnJSON.addProperty("overview_avgsession", avgSession);

		//calculate avg time per screen , no of screens / total session time
		int avgTimePerScreen = 0;


		if(currentoverview.getActiveSessionLength() != 0)
		{
			if(totalScreens != 0)
			{
				avgTimePerScreen= (int) currentoverview.getActiveSessionLength() / totalScreens;
			}
			else
			{
				avgTimePerScreen = 0;
			}
		}

		String avgTimeScreen = "";

		if(avgTimePerScreen == 0)
		{
			avgTimeScreen = "00:00";
		}
		else if(avgTimePerScreen < 60)
		{
			avgTimeScreen = "00:" + dFormat.format(avgTimePerScreen);
		}
		else
		{
			minutesVal = avgTimePerScreen / 60;
			avgTimePerScreen = avgTimePerScreen % 60;

			avgTimeScreen = dFormat.format(minutesVal) + ":" +  dFormat.format(avgTimePerScreen);
		}

		returnJSON.addProperty("overview_avgtimescreen", avgTimeScreen);


		//populate engagement section
		//*****************************************************************************************************************	
		//get number of active users
		int noOfActiveUsers = _gcRPC.getNumberOfActiveUsers(Constants.TODAY, projectName, allProjects);
		String _noOfActiveUsersJSON = gson.toJson(noOfActiveUsers);
		returnJSON.addProperty("engagement_activeusers", _noOfActiveUsersJSON);

		//Populating Users Panel 
		int userOnlineVal = currentoverview.getNoOfActiveUsers(); 
		String _userOnlineValJSON = gson.toJson(userOnlineVal);
		returnJSON.addProperty("users_onlineusers", _userOnlineValJSON);

		//*****************************************************************************************************************
		List<UserLocations> userLocations = currentoverview.getUserLocations();
		log.error("userLocations::" + userLocations);
		int noOfUsers = 0, noOfLocations = 0, noOfCurrentSessionScreens = 0;
		UsersOverviewInformation uOverview;
		List<UserVisitsPanelData> _userpanelsList = new ArrayList<UserVisitsPanelData>();
		if(userLocations != null)
		{
			noOfLocations = userLocations.size();
			List<String> items = new ArrayList<String>();
			for(int i=0; i<noOfLocations; i++)
			{

				noOfCurrentSessionScreens = 0;

				// add information about each user visits
				//check and add a bar line 
				String userName = userLocations.get(i).getUserName();
				log.error("UserName::" + userName);
				String userCity = userLocations.get(i).getCity();
				log.error("userCity::" + userCity);
				String userState = userLocations.get(i).getState();
				log.error("userState::" + userState);
				String userCountry = userLocations.get(i).getCountry();
				log.error("userCountry::" + userCountry);
				double userLatitude = userLocations.get(i).getLatitude();
				log.error("userLatitude::" + userLatitude);
				double userLongitude = userLocations.get(i).getLongitude();
				log.error("userLongitude::" + userLongitude);
				String userProfile = userLocations.get(i).getUserAuthProfile();
				UserVisitsPanelData _userVisitPanel = new UserVisitsPanelData();

				_userVisitPanel.userName= userName;
				_userVisitPanel.userCity= userCity;
				_userVisitPanel.userState= userState;
				_userVisitPanel.userCountry= userCountry;
				_userVisitPanel.userLatitude= userLatitude;
				_userVisitPanel.userLongitude= userLongitude;


				uOverview = _gcRPC.getUserInformation(Constants.TODAY, userName, projectName, allProjects, userProfile);
				if(uOverview != null)
				{

					String firstseen = uOverview.getFirstSeen();
					Date date = new Date();
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					String d = df.format(date);
					if(firstseen != null)
					{
						if(d.equals(firstseen.substring(0, 10))){
							noOfUsers++;
						}
					}
					for(ScreensCount sc : screensPerUser)
					{
						if(sc.getScreenName().compareToIgnoreCase(userName) == 0)
						{
							noOfCurrentSessionScreens = sc.getNoOfViews();
						}
					}

					_userVisitPanel.noOfBars=noOfCurrentSessionScreens;
					if(uOverview.getCurrentScreen() != null && uOverview.getCurrentScreen().contains(","))
					{
						_userVisitPanel.lastScreenName= uOverview.getCurrentScreen().split(",")[0].trim();
					}
					else
					{
						_userVisitPanel.lastScreenName="";
					}

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
					Date _currentDate = new Date();
					Date _lastLoginDate = new Date();

					if(uOverview.getLastSeen() != null && uOverview.getLastSeen().length() > 0)
					{
						try {
							_lastLoginDate = sdf.parse(uOverview.getLastSeen());
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							log.error(e);
						}
					}

					int secondsDiff = (int)(_currentDate.getTime() - _lastLoginDate.getTime())/1000;

					String unitString = "";
					if(secondsDiff < 0)
					{
						secondsDiff = 0;
						unitString = " seconds ago";
					}
					if(secondsDiff >= 60) //convert to mins
					{
						secondsDiff = secondsDiff / 60;
						if(secondsDiff >= 60) //convert to hours
						{
							secondsDiff = secondsDiff / 60;
							if(secondsDiff > 24) //convert to days
							{
								secondsDiff = secondsDiff / 24;
								unitString = " days ago";
							}
							else
							{
								unitString = " hours ago";
							}
						}
						else
						{
							unitString = " minutes ago";
						}
					}
					else
					{
						unitString = " seconds ago";
					}


					_userVisitPanel.lastLoginTime=secondsDiff + unitString;
					// Arrays.asList("apple", "apple", "banana","apple", "orange", "banana", "papaya");
					items.add(userCity +", "+ userState + ", " + userCountry);

					_userpanelsList.add(_userVisitPanel);
				}

			}
			Map<String, Long> result = items.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			log.error("result Loc count::" + result);
			String locRes = "";
			for (Map.Entry<String,Long> entry : result.entrySet())  
				locRes= locRes + "_" + entry.getKey() + "-" + entry.getValue(); 
			log.error("result Loc locRes::" + locRes);
			String _usersLocCntJSON = gson.toJson(locRes);
			returnJSON.addProperty("users_Loc_Count", _usersLocCntJSON);

			log.error("_userpanelsList size::" + _userpanelsList.size());
			String _usersPanelJSON = gson.toJson(_userpanelsList);


			returnJSON.addProperty("users_table", _usersPanelJSON);

			String _userReturningJSON = gson.toJson(userOnlineVal - noOfUsers);
			returnJSON.addProperty("users_returning", _userReturningJSON);
		}
		//*****************************************************************************************************************
		//Populate location Table

		//retrieve and populate location data from hash map
		HashMap.Entry<String,Integer> locRec;
		String locationName = "";
		int locationVal = 0;
		List<UserLocationTable> locationTable = new ArrayList<UserLocationTable>();
		UserLocationTable userLocation = new UserLocationTable();
		Iterator<HashMap.Entry<String,Integer>> itrLocations = currentoverview.getLocationDeviceBrowsers().getLocations().entrySet().iterator();
		log.error("itrLocations::" +  itrLocations);
		while(itrLocations.hasNext())
		{
			locRec = itrLocations.next();
			log.error("locRec::" + locRec);
			locationName = locRec.getKey();
			locationName = locationName.trim();

			while(locationName.startsWith(","))
			{
				locationName = locationName.substring(1);
			}
			if (locationName.endsWith(",")) {
				locationName = locationName.replaceAll(",", "");
			}

			if(locationName.contains("Unknown"))
			{
				locationName = "Unknown";
			}
			locationName = locationName.replace(",null,", ",");

			locationVal = locRec.getValue();

			userLocation = new UserLocationTable();
			userLocation.locationName = locationName;
			log.error("locationName::" + locationName);
			userLocation.locationVal = locationVal;
			locationTable.add(userLocation);

		}
		log.error("Before _locationtableJSON::" + locationTable);
		String _locationtableJSON = gson.toJson(locationTable);
		log.error("After _locationtableJSON::" + locationTable);

		returnJSON.addProperty("locations_table", _locationtableJSON);

		//*****************************************************************************************************************

		//get no. of users per screen
		HashMap<String,Integer> contentData = _gcRPC.getNumberOfUsersPerScreenRealTime(projectName, allProjects);
		List<ContentsData> _noOfUsersPerScreen = new ArrayList<ContentsData>();
		ContentsData _contentData = new ContentsData();
		if(contentData != null )
		{
			Iterator<HashMap.Entry<String,Integer>> itr = contentData.entrySet().iterator();
			while(itr.hasNext())
			{
				HashMap.Entry<String,Integer> screenRec = itr.next();
				_contentData = new ContentsData();
				_contentData.setScreenName(screenRec.getKey());
				_contentData.setUserCount(screenRec.getValue());
				_noOfUsersPerScreen.add(_contentData);
			}
		}
		String _noOfUsersPerScreenJSON = gson.toJson(_noOfUsersPerScreen);
		returnJSON.addProperty("content_table", _noOfUsersPerScreenJSON);
		//*****************************************************************************************************************
		AlarmsInformation alarmInfo = _gcRPC.getAlarmsOverview(Constants.TODAY, projectName, allProjects);
		if(alarmInfo != null)
		{

			//based on the no of alarms set no of segments to draw
			int noOfActiveAlarms = alarmInfo.getNoOfActiveAlarms();
			int noOfAckAlarms = alarmInfo.getNoOfAckAlarms();
			//Buttons from alarms sub-panel

			//get alarms information 
			String _activeAlarmsJSON = gson.toJson(noOfActiveAlarms);
			returnJSON.addProperty("alarms_active", _activeAlarmsJSON);

			String _ackAlarmValJSON = gson.toJson(noOfAckAlarms);
			returnJSON.addProperty("alarms_acknowledged", _ackAlarmValJSON);



			HashMap<String, Integer> activeAlarms = alarmInfo.getActiveAlarmsCount();

			if(activeAlarms != null)
			{
				if(activeAlarms.get("High") != null)
				{
					String _activeHighJSON = gson.toJson(activeAlarms.get("High"));
					returnJSON.addProperty("alarms_active_high", _activeHighJSON);
				}
				else
				{
					String _activeHighJSON = gson.toJson("");
					returnJSON.addProperty("alarms_active_high", _activeHighJSON);
				}
				if(activeAlarms.get("Critical") != null)
				{
					String _activeCriticalJSON = gson.toJson(activeAlarms.get("Critical"));
					returnJSON.addProperty("alarms_active_critical", _activeCriticalJSON);
				}
				else
				{
					String _activeCriticalJSON = gson.toJson("");
					returnJSON.addProperty("alarms_active_critical", _activeCriticalJSON);
				}
				if(activeAlarms.get("Low") != null)
				{
					String _activeLowJSON = gson.toJson(activeAlarms.get("Low"));
					returnJSON.addProperty("alarms_active_low", _activeLowJSON);
				}
				else
				{
					String _activeLowJSON = gson.toJson("");
					returnJSON.addProperty("alarms_active_low", _activeLowJSON);
				}
				if(activeAlarms.get("Medium") != null)
				{
					String _activeMediumJSON = gson.toJson(activeAlarms.get("Medium"));
					returnJSON.addProperty("alarms_active_medium", _activeMediumJSON);
				}
				else
				{
					String _activeMediumJSON = gson.toJson("");
					returnJSON.addProperty("alarms_active_medium", _activeMediumJSON);
				}
			}


			int q, noOfSegments=0;

			if(noOfActiveAlarms <= 100)
			{
				noOfSegments = noOfActiveAlarms;
			}
			else if(noOfActiveAlarms > 100 && noOfActiveAlarms <= 1000)
			{
				q = noOfActiveAlarms / 10;
				if(noOfActiveAlarms % 10 > 0)
				{
					q = q + 1;
				}

				noOfSegments = q;
			}
			else if(noOfActiveAlarms > 1000 && noOfActiveAlarms <= 10000)
			{
				q = noOfActiveAlarms / 50;
				if(noOfActiveAlarms % 50 > 0)
				{
					q = q + 1;
				}

				noOfSegments = q;
			}
			else if(noOfActiveAlarms > 10000 )
			{

				q = noOfActiveAlarms / 100;
				if(noOfActiveAlarms % 100 > 0)
				{
					q = q + 1;
				}

				noOfSegments = q;
			}
			String _activeSegmentsJSON = gson.toJson(noOfSegments);
			returnJSON.addProperty("alarms_active_segments", _activeSegmentsJSON);

			if(alarmInfo.getAckAlarmsCount().get("High") != null)
			{
				String _ackHighJSON = gson.toJson(alarmInfo.getAckAlarmsCount().get("High"));
				returnJSON.addProperty("alarms_ack_high", _ackHighJSON);
			}
			else
			{
				String _ackHighJSON = gson.toJson("");
				returnJSON.addProperty("alarms_ack_high", _ackHighJSON);
			}
			if(alarmInfo.getAckAlarmsCount().get("Critical") != null)
			{
				String _ackCriticalJSON = gson.toJson(alarmInfo.getAckAlarmsCount().get("Critical"));
				returnJSON.addProperty("alarms_ack_critical", _ackCriticalJSON);
			}
			else
			{
				String _ackCriticalJSON = gson.toJson("");
				returnJSON.addProperty("alarms_ack_critical", _ackCriticalJSON);
			}
			if(alarmInfo.getAckAlarmsCount().get("Low") != null)
			{
				String _ackLowJSON = gson.toJson(alarmInfo.getAckAlarmsCount().get("Low"));
				returnJSON.addProperty("alarms_ack_low", _ackLowJSON);
			}
			else
			{
				String _ackLowJSON = gson.toJson("");
				returnJSON.addProperty("alarms_ack_low", _ackLowJSON);
			}
			if(alarmInfo.getAckAlarmsCount().get("Medium") != null)
			{
				String _ackMediumJSON = gson.toJson(alarmInfo.getAckAlarmsCount().get("Medium"));
				returnJSON.addProperty("alarms_ack_medium", _ackMediumJSON);
			}
			else
			{
				String _ackMediumJSON = gson.toJson("");
				returnJSON.addProperty("alarms_ack_medium", _ackMediumJSON);
			}

			if(noOfAckAlarms <= 100)
			{
				noOfSegments = noOfAckAlarms;
			}
			else if(noOfAckAlarms > 100 && noOfAckAlarms <= 1000)
			{
				q = noOfAckAlarms / 10;
				if(noOfAckAlarms % 10 > 0)
				{
					q = q + 1;
				}

				noOfSegments = q;
			}
			else if(noOfAckAlarms > 1000 && noOfAckAlarms <= 10000)
			{
				q = noOfAckAlarms / 50;
				if(noOfAckAlarms % 50 > 0)
				{
					q = q + 1;
				}

				noOfSegments = q;
			}
			else if(noOfAckAlarms > 10000)
			{
				q = noOfAckAlarms / 100;
				if(noOfAckAlarms % 100 > 0)
				{
					q = q + 1;
				}

				noOfSegments = q;
			}

			String _ackSegmentsJSON = gson.toJson(noOfSegments);
			returnJSON.addProperty("alarms_ack_segments", _ackSegmentsJSON);



			if(alarmInfo.getAvgAckTime() != null && alarmInfo.getAvgAckTime().length() > 0)
			{
				String _avgAckTimeJSON = gson.toJson(alarmInfo.getAvgAckTime());
				returnJSON.addProperty("alarms_avgacktime", _avgAckTimeJSON);
			}



			if(alarmInfo.getAvgClearTime() != null && alarmInfo.getAvgClearTime().length() > 0)
			{

				String _avgClrTimeJSON = gson.toJson(alarmInfo.getAvgClearTime());
				returnJSON.addProperty("alarms_avgclrtime", _avgClrTimeJSON);
			}
		}


		//*****************************************************************************************************************

		//get screen depth / average session time

		List<ScreenDepthTableRealtime> listScreenDepthTable = new ArrayList<ScreenDepthTableRealtime>();
		ScreenDepthTableRealtime screenDepth = new ScreenDepthTableRealtime();

		if(screensPerUser != null)
		{

			int sessions0_2Screens = 0;
			int sessions3_4Screens = 0;
			int sessions5_7Screens = 0;
			int sessions8_10Screens = 0;
			int sessions11_15Screens = 0;
			int sessions16PlusScreens = 0;
			int noOfRecords = 0;

			int  tempScreenVal = 0;
			noOfRecords = screensPerUser.size();

			//build the dataset for bar chart
			for(int i=0; i<noOfRecords; i++)
			{


				tempScreenVal = screensPerUser.get(i).getNoOfViews();
				if(tempScreenVal <= 2.0)
				{
					sessions0_2Screens++;
				}
				else if(tempScreenVal > 2.0 && tempScreenVal <=4.0)
				{
					sessions3_4Screens++;
				}
				else if(tempScreenVal > 4.0 && tempScreenVal <=7.0)
				{
					sessions5_7Screens++;
				}
				else if(tempScreenVal > 7.0 && tempScreenVal <=10.0)
				{
					sessions8_10Screens++;
				}
				else if(tempScreenVal > 10.0 && tempScreenVal <=15.0)
				{
					sessions11_15Screens++;
				}
				else if(tempScreenVal > 15.0)
				{
					sessions16PlusScreens++;
				}
			}

			if(noOfRecords > 0)
			{
				screenDepth = new ScreenDepthTableRealtime();
				screenDepth.sessionDuration = "0-2 screens";
				screenDepth.noOfUsersSessions = sessions0_2Screens;
				listScreenDepthTable.add(screenDepth);

				screenDepth = new ScreenDepthTableRealtime();
				screenDepth.sessionDuration = "3-4 screens";
				screenDepth.noOfUsersSessions = sessions3_4Screens;
				listScreenDepthTable.add(screenDepth);

				screenDepth = new ScreenDepthTableRealtime();
				screenDepth.sessionDuration = "5-7 screens";
				screenDepth.noOfUsersSessions = sessions5_7Screens;
				listScreenDepthTable.add(screenDepth);

				screenDepth = new ScreenDepthTableRealtime();
				screenDepth.sessionDuration = "8-10 screens";
				screenDepth.noOfUsersSessions = sessions8_10Screens;
				listScreenDepthTable.add(screenDepth);

				screenDepth = new ScreenDepthTableRealtime();
				screenDepth.sessionDuration = "11-15 screens";
				screenDepth.noOfUsersSessions = sessions11_15Screens;
				listScreenDepthTable.add(screenDepth);

				screenDepth = new ScreenDepthTableRealtime();
				screenDepth.sessionDuration = "16+ screens";
				screenDepth.noOfUsersSessions = sessions16PlusScreens;
				listScreenDepthTable.add(screenDepth);

			} 
		}
		String _listScreenDepthTableJSON = gson.toJson(listScreenDepthTable);
		returnJSON.addProperty("engagement_tableavgdepthofscreen", _listScreenDepthTableJSON);
		//************************************************************************************
		if(currentoverview.getLocationDeviceBrowsers() != null)
		{
			DevicesInformation dInfo = currentoverview.getLocationDeviceBrowsers().getDevices();
			String _clientsOnDesktopJSON = gson.toJson(dInfo.getNoOfClientsOnDesktop());
			returnJSON.addProperty("devices_clientsondesktop", _clientsOnDesktopJSON);

			String _clientsOnMobileJSON = gson.toJson(dInfo.getNoOfClientsOnMobile());
			returnJSON.addProperty("devices_clientsonmobile", _clientsOnMobileJSON);

		}
		//************************************************************************************
		ArrayList<ContentsData> opSystems = new ArrayList<ContentsData>();
		ContentsData _tempOSData;
		HashMap.Entry<String,Integer> osRec;
		Iterator<HashMap.Entry<String,Integer>> itrOS = currentoverview.getLocationDeviceBrowsers().getOperatingSystems().entrySet().iterator();

		while(itrOS.hasNext())
		{
			osRec = itrOS.next();
			_tempOSData = new ContentsData();
			_tempOSData.setScreenName(osRec.getKey());
			_tempOSData.setUserCount(osRec.getValue());

			opSystems.add(_tempOSData);
		}

		Collections.sort(opSystems, new Comparator<ContentsData>(){
			@Override
			public int compare(ContentsData arg0,
					ContentsData arg1) {
				if(arg0.getUserCount() > arg1.getUserCount())
				{
					return 1;
				}
				else
				{
					return 0;	
				}
			}
		});


		int noOfOSRecs = 0;
		noOfOSRecs = opSystems.size();

		//Add a pie chart showing operating systems distribution
		ArrayList<ContentsData> dataset = new ArrayList<ContentsData>();
		ContentsData _oneDataset = new ContentsData();

		for(int i=0; i<noOfOSRecs && i<4; i++)
		{
			_oneDataset = new ContentsData();
			_oneDataset.setScreenName(opSystems.get(i).getScreenName());
			_oneDataset.setUserCount(opSystems.get(i).getUserCount());
			dataset.add(_oneDataset);
		}

		// if(noOfOSRecs > 0)
		// {
		// 	dataset.setScreenName(opSystems.get(0).getScreenName());
		// 	dataset.setUserCount(opSystems.get(0).getUserCount());
		// }
		// if(noOfOSRecs >= 2 )
		// {
		// 	dataset.setScreenName(opSystems.get(1).getScreenName());
		// 	dataset.setUserCount(opSystems.get(1).getUserCount());
		// }
		// if(noOfOSRecs >= 3 )

		// {
		// 	dataset.setScreenName(opSystems.get(2).getScreenName());
		// 	dataset.setUserCount(opSystems.get(2).getUserCount());
		// }
		// if(noOfOSRecs >= 4 )
		// {
		// 	dataset.setScreenName(opSystems.get(3).getScreenName());
		// 	dataset.setUserCount(opSystems.get(3).getUserCount());
		// }

		String _osJSON = gson.toJson(opSystems);
		returnJSON.addProperty("devices_os", _osJSON);
		String _osPieJSON = gson.toJson(dataset);
		returnJSON.addProperty("devices_os_piechartdata", _osPieJSON);

		//************************************************************************************
		ArrayList<ContentsData> browsers = new ArrayList<ContentsData>();
		ContentsData _tempData;
		HashMap.Entry<String,Integer> browserRec;
		Iterator<HashMap.Entry<String,Integer>> itrBrowsers = currentoverview.getLocationDeviceBrowsers().getBrowsers().entrySet().iterator();

		while(itrBrowsers.hasNext())
		{
			browserRec = itrBrowsers.next();
			_tempData = new ContentsData();
			_tempData.setScreenName(browserRec.getKey());
			_tempData.setUserCount(browserRec.getValue());

			browsers.add(_tempData);
		}

		java.util.Collections.sort(browsers, new Comparator<ContentsData>(){
			@Override
			public int compare(ContentsData arg0,
					ContentsData arg1) {
				if(arg0.getUserCount() > arg1.getUserCount())
				{
					return 1;
				}
				else
				{
					return 0;	
				}
			}
		});
		noOfOSRecs = 0;
		noOfOSRecs = browsers.size();

		//Add a pie chart showing operating systems distribution
		dataset = new ArrayList<ContentsData>();
		for(int i=0; i<noOfOSRecs && i<4; i++)
		{
			_oneDataset = new ContentsData();
			_oneDataset.setScreenName(browsers.get(i).getScreenName());
			_oneDataset.setUserCount(browsers.get(i).getUserCount());
			dataset.add(_oneDataset);
		}

		// if(noOfOSRecs > 0)
		// {
		// 	dataset.setScreenName(browsers.get(0).getScreenName());
		// 	dataset.setUserCount(browsers.get(0).getUserCount());
		// }
		// if(noOfOSRecs >= 2 )
		// {
		// 	dataset.setScreenName(browsers.get(1).getScreenName());
		// 	dataset.setUserCount(browsers.get(1).getUserCount());
		// }
		// if(noOfOSRecs >= 3 )

		// {
		// 	dataset.setScreenName(browsers.get(2).getScreenName());
		// 	dataset.setUserCount(browsers.get(2).getUserCount());
		// }
		// if(noOfOSRecs >= 4 )
		// {
		// 	dataset.setScreenName(browsers.get(3).getScreenName());
		// 	dataset.setUserCount(browsers.get(3).getUserCount());
		// }

		String _pieBrowsersJSON = gson.toJson(dataset);
		returnJSON.addProperty("devices_browsers_piechartdata", _pieBrowsersJSON);

		String _listBrowsersJSON = gson.toJson(browsers);
		returnJSON.addProperty("devices_browsers", _listBrowsersJSON);
		//************************************************************************************************************
		//get active users info
		ActiveUsersInfo activeUserInfo  = _gcRPC.getActiveUsersCounts(projectName, allProjects, Constants.TODAY);

		String _1DayActiveUsersJSON = gson.toJson(activeUserInfo.getOneDayActiveUsers());
		returnJSON.addProperty("engagement_1dayactiveusers", _1DayActiveUsersJSON);

		String _7DayActiveUsersJSON = gson.toJson(activeUserInfo.getSevenDayActiveUsers());
		returnJSON.addProperty("engagement_7dayactiveusers", _7DayActiveUsersJSON);

		String _14DayActiveUsersJSON = gson.toJson(activeUserInfo.getFourteenDayActiveUsers());
		returnJSON.addProperty("engagement_14dayactiveusers", _14DayActiveUsersJSON);
		//************************************************************************************************************
		return returnJSON;


	}	// End of fetchRealTimeData
	//**************************************************************************************************************
	private int getValueForDay(Dataset data, String dayName)
	{
		int retVal = 0;

		int dataSetLen = data.getRowCount();

		int i = 0;

		for(i=0; i<dataSetLen; i++)
		{
			if(dayName.compareToIgnoreCase(data.getValueAt(i, 0).toString()) == 0)
			{
				retVal = (int) Float.parseFloat(data.getValueAt(i, 1).toString());
				break;
			}
		}


		return retVal;
	}
	//**************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchRealTimeLineGraphData(RequestContext req, HttpServletResponse res)
	{
		int currentDurationA = Integer.parseInt(req.getParameter("durationA"));
		int currentDurationB = Integer.parseInt(req.getParameter("durationB"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));
		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//get line graph data
		List<LineChartInfo> _lineDataDurationA = new ArrayList<LineChartInfo>();
		List<LineChartInfo> _lineDataDurationB = new ArrayList<LineChartInfo>();
		LineChartInfo lineData = new LineChartInfo();

		try {
			Dataset dataToday ;
			Dataset dataYesterDay;

			if (currentDurationA == Constants.TODAY || currentDurationB == Constants.YESTERDAY){

				dataToday = _gcRPC.getTotalUsersData( projectName, allProjects, currentDurationA);
				dataYesterDay = _gcRPC.getTotalUsersData( projectName, allProjects, currentDurationB);
			}
			else
				if ((currentDurationA == Constants.THIS_WEEK && currentDurationB == Constants.LAST_WEEK )|| (currentDurationA == Constants.THIS_MONTH || currentDurationB == Constants.LAST_MONTH))
				{	
					dataToday = _gcRPC.getTotalUsersData( projectName, allProjects, currentDurationA);
					dataYesterDay = _gcRPC.getTotalUsersData( projectName, allProjects, currentDurationB);
				}else
				{
					dataToday = _gcRPC.getTotalUsersData( projectName, allProjects, Constants.TODAY);
					dataYesterDay = _gcRPC.getTotalUsersData( projectName, allProjects, Constants.YESTERDAY);
				}

			//create a line chart to show total actions and total sessions for various durations
			int noOfRows = 0;
			int todayRow = 0;
			int yesterdayRow = 0;

			if(dataToday != null)
			{
				todayRow = dataToday.getRowCount();
			}

			if(dataYesterDay != null)
			{ 
				yesterdayRow = dataYesterDay.getRowCount();
			}
			if (todayRow>yesterdayRow)

				noOfRows = todayRow;
			else 
				noOfRows = yesterdayRow;

			int r = 0;
			if(currentDurationA != Constants.THIS_WEEK && currentDurationB != Constants.LAST_WEEK )
			{
				for(r=0; r<noOfRows; r++)
				{

					//check if i is less than todayRow
					if(r < todayRow){
						if(dataToday.getValueAt(r, 1) != null)
						{
							lineData = new LineChartInfo();				
							lineData.count = (int)Float.parseFloat(dataToday.getValueAt(r, 1).toString());
							lineData.day = dataToday.getValueAt(r, 0).toString();
							lineData.seriesName = "Today";
							_lineDataDurationA.add(lineData);
						}
						else
						{
							lineData = new LineChartInfo();				
							lineData.count = 0;
							lineData.day = dataToday.getValueAt(r, 0).toString();
							lineData.seriesName = "Today";
							_lineDataDurationA.add(lineData);
							//_lineData.addValue(0, "Today",dataToday.getValueAt(r, 0).toString());
						}
					}
					else 
					{
						lineData = new LineChartInfo();				
						lineData.count = 0;
						lineData.day = dataYesterDay.getValueAt(r, 0).toString();
						lineData.seriesName = "Today";
						_lineDataDurationA.add(lineData);
						//_lineData.addValue(0, "Today",dataYesterDay.getValueAt(r, 0).toString());
					}

					//check if i is less than yesterdayRow
					if(r < yesterdayRow){
						if(dataYesterDay.getValueAt(r, 1) != null)
						{
							lineData = new LineChartInfo();				
							lineData.count = (int)Float.parseFloat(dataYesterDay.getValueAt(r, 1).toString());
							lineData.day = dataYesterDay.getValueAt(r, 0).toString();
							lineData.seriesName = "Yesterday";
							_lineDataDurationB.add(lineData);
							//_lineData.addValue((int)Float.parseFloat(dataYesterDay.getValueAt(r, 1).toString()), "Yesterday",dataYesterDay.getValueAt(i, 0).toString());
						}
						else
						{
							lineData = new LineChartInfo();				
							lineData.count = 0;
							lineData.day = dataYesterDay.getValueAt(r, 0).toString();
							lineData.seriesName = "Yesterday";
							_lineDataDurationB.add(lineData);
							//_lineData.addValue(0, "Yesterday",dataYesterDay.getValueAt(r, 0).toString());
						}
					}
					else
					{
						lineData = new LineChartInfo();				
						lineData.count = 0;
						lineData.day = dataToday.getValueAt(r, 0).toString();
						lineData.seriesName = "Yesterday";
						_lineDataDurationB.add(lineData);
						//_lineData.addValue(0, "Yesterday",dataToday.getValueAt(r, 0).toString());
					}
				}
			}
			else
			{
				lineData = new LineChartInfo();		

				lineData.count = getValueForDay(dataToday, "monday");
				lineData.day = "Monday";
				lineData.seriesName = "Today";
				_lineDataDurationA.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataToday, "tuesday");
				lineData.day = "Tuesday";
				lineData.seriesName = "Today";
				_lineDataDurationA.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataToday, "wednesday");
				lineData.day = "Wednesday";
				lineData.seriesName = "Today";
				_lineDataDurationA.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataToday, "thursday");
				lineData.day = "Thursday";
				lineData.seriesName = "Today";
				_lineDataDurationA.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataToday, "friday");
				lineData.day = "Friday";
				lineData.seriesName = "Today";
				_lineDataDurationA.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataToday, "saturday");
				lineData.day = "Saturday";
				lineData.seriesName = "Today";
				_lineDataDurationA.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataToday, "sunday");
				lineData.day = "Sunday";
				lineData.seriesName = "Today";
				_lineDataDurationA.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataYesterDay, "monday");
				lineData.day = "Monday";
				lineData.seriesName = "Yesterday";
				_lineDataDurationB.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataYesterDay, "tuesday");
				lineData.day = "Tuesday";
				lineData.seriesName = "Yesterday";
				_lineDataDurationB.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataYesterDay, "wednesday");
				lineData.day = "Wednesday";
				lineData.seriesName = "Yesterday";
				_lineDataDurationB.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataYesterDay, "thursday");
				lineData.day = "Thursday";
				lineData.seriesName = "Yesterday";
				_lineDataDurationB.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataYesterDay, "friday");
				lineData.day = "Friday";
				lineData.seriesName = "Yesterday";
				_lineDataDurationB.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataYesterDay, "saturday");
				lineData.day = "Saturday";
				lineData.seriesName = "Yesterday";
				_lineDataDurationB.add(lineData);

				lineData = new LineChartInfo();				
				lineData.count = getValueForDay(dataYesterDay, "sunday");
				lineData.day = "Sunday";
				lineData.seriesName = "Yesterday";
				_lineDataDurationB.add(lineData);



			}
		} catch (NumberFormatException e) 
		{
			// TODO Auto-generated catch block
			log.error(e);
		}

		String _lineDataJSON = gson.toJson(_lineDataDurationA);
		returnJSON.addProperty("linegraphdataDurationA", _lineDataJSON);

		_lineDataJSON = gson.toJson(_lineDataDurationB);
		returnJSON.addProperty("linegraphdataDurationB", _lineDataJSON);

		//*****************************************************************************************************************
		return returnJSON;
	}// End of fetchRealTimeLineGraphData
	//*****************************************************************************************************************		
	//**************************************************************************************************************	
	private JsonObject fetchUsersData(RequestContext req, HttpServletResponse res) 
	{

		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));
		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		Collection<User> userProfiles = _gcRPC.getUserProfiles();
		String _userProfilesJSON = gson.toJson(userProfiles);
		returnJSON.addProperty("userprofiles", _userProfilesJSON);

		Dataset allUsers = _gcRPC.getAllLoggedInUsers(allProjects, projectName);

		long diffDate = 0;
		long diffHours1 = 0;
		UserListData userListData = new UserListData();
		List<UserListData> alluserListData = new ArrayList<UserListData>();
		List<UserListData> sortedData = new ArrayList<UserListData>();

		String loggedInStr = "";
		String userName = "";
		String userWithProfile = "";
		String profileName = "";
		int noOfUsers = 0, i;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
		//DateFormat lastLogindf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
		DateFormat lastLogindf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S",Locale.ENGLISH);
		if(allUsers != null)
		{
			noOfUsers = allUsers.getRowCount();
			//System.out.println("no of logged in users : " + noOfUsers);
			for(i=0; i<noOfUsers; i++)
			{
				loggedInStr = "";
				if(allUsers.getValueAt(i, 0) != null)
				{
					userWithProfile = allUsers.getValueAt(i, 0).toString().trim();
					userName = userWithProfile.split(":")[0];
					profileName = userWithProfile.split(":")[1];


				}
				//check if user is online 
				boolean isUserOnline = _gcRPC.checkUserOnlineOrOffline(projectName, allProjects, userName, profileName);
				if(isUserOnline)
				{
					loggedInStr = "Online";
				}
				else
				{
					String lastSeen = allUsers.getValueAt(i, 1).toString();

					if(lastSeen != "" )
					{
						Date todayDate = new Date(); 

						String today = df.format(todayDate);

						try 
						{
							Date firstDateToday = df.parse(today);
							Date logOutTime = lastLogindf.parse(lastSeen);

							diffDate = firstDateToday.getTime() - logOutTime.getTime();
							diffHours1 = diffDate / (60 * 60 * 1000);
							if(diffHours1 > 24)
							{
								diffHours1 = (int)(diffHours1 / 24);
								loggedInStr = diffHours1 + " day(s) ago";
							}
							else
							{
								if(diffHours1 == 0){
									Date logOutTime1 ;
									try {
										logOutTime1 = lastLogindf.parse(lastSeen);
										diffDate = todayDate.getTime() - logOutTime1.getTime() ;
										diffHours1 = diffDate /(60 * 1000);
										System.out.println("diffHour Value : " + diffHours1);
										loggedInStr = "" + diffHours1 + " minutes ago" ;
										//screenViewPanelData.lblHoursAgo.setText("" + diffHours + "Minutes ago");
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										log.error(e);
									}

								}
								else
									loggedInStr = diffHours1 + " hours ago";
							}

						} catch (ParseException e)
						{
							log.error("fetchUserData:" +e);
						}

					}
				}
				userListData = new UserListData();
				userListData.userName = userName;
				userListData.profileName = profileName;
				userListData.lastSeen = loggedInStr;

				UsersOverviewInformation _uinfo = _gcRPC.getUserInformation(Constants.LAST_SEVEN_DAYS, userName, projectName, allProjects, profileName);
				if(_uinfo != null)
				{

					userListData.userOverview = _uinfo;
					int j = 0;
					int totalScreenView = 0, totalScreenViewLast7Days= 0;
					log.error("userListData.userOverview.screensViewed.size()::" + userListData.userOverview.screensViewed.size());
					for (j = 0 ; j < userListData.userOverview.screensViewed.size() ; j++)
					{
						log.error("userListData.userOverview.screensViewed.get(j).getNoOfViews()::" + userListData.userOverview.screensViewed.get(j).getNoOfViews());
						totalScreenView = totalScreenView + userListData.userOverview.screensViewed.get(j).getNoOfViews();
						userListData.userOverview.screensViewedCount = totalScreenView;
					}
					userListData.userOverview.totalActions = userListData.userOverview.totalActions + totalScreenView;
					for (j = 0 ; j < userListData.userOverview.screensViewedLast7Days.size() ; j++)
					{
						totalScreenViewLast7Days = totalScreenViewLast7Days + userListData.userOverview.screensViewedLast7Days.get(j).getNoOfViews();
						userListData.userOverview.screensViewedLast7DaysCount = totalScreenViewLast7Days;
					}
					userListData.userOverview.totalActionsLast7Days = userListData.userOverview.totalActionsLast7Days + totalScreenViewLast7Days;
					userListData.userOverview = _uinfo;
				}

				List<UserSessionDetailData> userSesions = getUserSessionsData(projectName, allProjects,userName, profileName);
				userListData.userSessions = userSesions;

				if(userListData.lastSeen.contains("Online"))
				{
					alluserListData.add(userListData);
				}
				else 
				{	
					sortedData.add(userListData);
				}
				Collections.sort(sortedData, new Comparator<UserListData>() {

					@Override
					public int compare(UserListData o1, UserListData o2) {
						String t1 = o1.lastSeen.toLowerCase();
						String t2 = o2.lastSeen.toLowerCase();

						int t1flag = -1, t2flag = -1;
						int returnVal = 0;
						int t1Val, t2Val;

						if(t1.contains("minutes"))
						{
							t1flag = 0; //minutes
						}
						else if(t1.contains("hour"))
						{
							t1flag = 1;
						}
						else if(t1.contains("day"))
						{
							t1flag = 2;
						}
						else if(t1.contains("Offline"))
						{
							t1flag = 3;
						}

						if(t2.contains("minutes"))
						{
							t2flag = 0; //minutes
						}
						else if(t2.contains("hour"))
						{
							t2flag = 1;
						}
						else if(t2.contains("day"))
						{
							t2flag = 2;
						}
						else if(t2.contains("Offline"))
						{
							t2flag = 3;
						}


						if(t1flag == -1)
						{
							returnVal = 1;
						}
						else if(t2flag == -1)
						{
							returnVal = -1;
						}
						else if(t1flag > t2flag)
						{
							returnVal = 1;
						}
						else if (t1flag < t2flag)
						{
							returnVal = -1;
						}
						else
						{

							t1Val = Integer.parseInt(t1.split("\\s+")[0]);
							t2Val = Integer.parseInt(t2.split("\\s+")[0]);

							if(t1Val == t2Val)
							{
								returnVal = 0;
							}
							else if(t1Val > t2Val)
							{
								returnVal = 1;
							}
							else
							{
								returnVal = -1;
							}
						}

						return returnVal;
					}
				});
				

			}
			alluserListData.addAll(sortedData);
			alluserListData= alluserListData.stream().distinct().collect(Collectors.toList());
			String _alluserListDataJSON = gson.toJson(alluserListData);
			returnJSON.addProperty("allusersdata", _alluserListDataJSON);
		}

		CurrentOverview currentOverview = _gcRPC.getCurrentOverview(projectName, allProjects);
		String _onlineInfoJSON = gson.toJson(currentOverview);
		returnJSON.addProperty("currentoverview", _onlineInfoJSON);

		//**********************************************************************************************************
		return returnJSON;
	}	
	//****************************************************************************************************
	List<UserSessionDetailData> getUserSessionsData(String projectName, boolean allProjects, String userName, String profileName)
	{

		UserScreenViewData screenViewData =new UserScreenViewData();
		List<UserScreenViewData> listScreenViewData = new ArrayList<UserScreenViewData>();

		UserSessionDetailData sessionDetailData = new UserSessionDetailData();
		List<UserSessionDetailData> listSessionDetailData = new ArrayList<UserSessionDetailData>();

		Dataset screenViewsDS = _gcRPC.getScreenViewsPerUserPerVisitNew(Constants.LAST_365_DAYS, projectName, allProjects,userName, profileName);
		if(screenViewsDS != null)
		{

			int ii = 0;
			int rows = screenViewsDS.getRowCount();
			// log.error("rows******" + rows);
			while(ii < rows)
			{
				sessionDetailData = new UserSessionDetailData();
				String sessionStartDate = screenViewsDS.getValueAt(ii, "SessionStart").toString().trim();
				// log.error("sessionStartDate******" + sessionStartDate);
				String sessionEndDate = screenViewsDS.getValueAt(ii, "SessionEnd").toString().trim();
				// log.error("sessionEndDate******" + sessionEndDate);

				String month = sessionStartDate.substring(4, 7);
				// log.error("month******" + month);
				month = getFullMonth(month);
				String fulldate = month + " " + sessionStartDate.substring(8, 10) +" " +sessionStartDate.substring(0, 4);
				// log.error("fulldate******" + fulldate);
				sessionDetailData.sessionDate = sessionStartDate; // fulldate;

				String locString = screenViewsDS.getValueAt(ii, "location").toString().trim();
				while(locString.startsWith(","))
				{
					locString = locString.substring(1);
					locString = locString.trim();
				}

				if(locString.endsWith(","))
				{
					locString.replaceAll(",", "");
				}

				if(locString.contains("Unknown"))
				{
					locString = "Unknown";
				}
				sessionDetailData.sessionLocation = locString;
				listScreenViewData = new ArrayList<UserScreenViewData>();
				int j;
				String viewdUserName = "";
				String screenName = "";
				for (j = ii ; j < rows ; j++)
				{
					String sessionPanelStartDate = screenViewsDS.getValueAt(j, "SessionStart").toString();
					String sessionPanelEndDate = screenViewsDS.getValueAt(j, "SessionEnd").toString();
					//sessionPanelDate = sessionPanelDate.substring(1, 10);
					if (sessionStartDate.equalsIgnoreCase(sessionPanelStartDate) && sessionEndDate.equalsIgnoreCase(sessionPanelEndDate))
					{

						Date todayDate = new Date(); 
						viewdUserName = "";
						screenName = "";

						screenViewData = new UserScreenViewData();

						if(screenViewsDS.getValueAt(j, "User") != null)
						{
							viewdUserName = screenViewsDS.getValueAt(j, "User").toString();
						}
						else
						{
							viewdUserName = "";
						}
						// DateFormat dtf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
						DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S",Locale.ENGLISH);
						screenViewData.userName=viewdUserName;

						if(screenViewsDS.getValueAt(j, "ScreenName") != null)
						{
							screenName = screenViewsDS.getValueAt(j, "ScreenName").toString();
						}
						else
						{
							screenName = "";
						}
						if(screenName.length() < 50)
						{
							screenName = StringUtils.rightPad(screenName, 50 , " ");
						}
						screenViewData.screenName = screenName;

						String loginTime = screenViewsDS.getValueAt(j, "SessionEnd").toString();
						// log.error("loginTime***" + loginTime );
						long diff = 0;
						long diffHours = 0;
						try {

							//Date secondDateLogout = df.parse(logOutTime);
							Date logOutTime = dtf.parse(loginTime);
							diff = todayDate.getTime() - logOutTime.getTime() ;
							diffHours = diff / (60  * 60  * 1000);
							// log.error(":::screenViewData.userName:::" + screenViewData.userName);
							// log.error("logOutTime.getTime() ***" + logOutTime.getTime()  );
							// log.error("todayDate.getTime()***" + todayDate.getTime() );
							// log.error("diffHours***" + diffHours );

						} catch (ParseException e) {
							// TODO Auto-generated catch block
							// log.error("" + e);
						}
						if(diffHours > 24){

							diffHours = (int)diffHours/24;
							screenViewData.hrsAgo = "" + diffHours + " day(s) ago";

						}
						else
						{
							if(diffHours == 0){
								Date logOutTime;
								try {
									logOutTime = dtf.parse(loginTime);
									diff = todayDate.getTime() - logOutTime.getTime() ;
									diffHours = diff /(60 * 1000);
									//System.out.println("diffHour Value : " + diffHours);
									screenViewData.hrsAgo = "" + diffHours + " minutes ago";
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									log.error("userSessionData:" +e);
								}

							}
							else{
								//System.out.println("diffHour Value : " + diffHours);
								screenViewData.hrsAgo = "" + diffHours + " hours ago";
							}
						}

						if(screenViewData.hrsAgo.length() < 14)
						{
							screenViewData.hrsAgo = StringUtils.leftPad(screenViewData.hrsAgo, 14 , " ");
						}
						listScreenViewData.add(screenViewData);
					}
					else {
						ii = j;

						break;
					}


					sessionDetailData.screenViews = listScreenViewData;
				}
				listSessionDetailData.add(sessionDetailData);
				ii = j;
			}

		}
		// log.error("before return statement :: " + listSessionDetailData.size());
		return listSessionDetailData;

	}

	String getFullMonth(String month){
		String monthName = "";
		if(month.compareToIgnoreCase("Jan") == 0){
			monthName = "January";
		}else
			if(month.compareToIgnoreCase("Feb") == 0){
				monthName = "February";
			}else
				if(month.compareToIgnoreCase("Mar") == 0){
					monthName = "March";
				}else
					if(month.compareToIgnoreCase("Apr") == 0){	
						monthName = "April";
					}else
						if(month.compareToIgnoreCase("May") == 0){
							monthName = "May";
						}else
							if(month.compareToIgnoreCase("Jun") == 0){
								monthName = "June";
							}else
								if(month.compareToIgnoreCase("Jul") == 0){
									monthName = "July";
								}else
									if(month.compareToIgnoreCase("Aug") == 0){
										monthName = "August";
									}else
										if(month.compareToIgnoreCase("Sep") == 0){
											monthName = "September";
										}else
											if(month.compareToIgnoreCase("Oct") == 0){
												monthName = "October";
											}else
												if(month.compareToIgnoreCase("Nov") == 0){
													monthName = "November";
												}else
													if(month.compareToIgnoreCase("Dec") == 0){
														monthName = "December";
													}

		return monthName;
	}
	/** function for Projects Panel *****/
	private JsonObject fetchProjectsList(RequestContext req, HttpServletResponse res) {


		JsonObject returnJSON = new JsonObject();
		String[] _projects = _gcRPC.getProjects("All Projects");
		Gson gson = new GsonBuilder().create();
		String _projectsJSON = gson.toJson(_projects);
		returnJSON.addProperty("projects", _projectsJSON);
		return returnJSON;
	}

	private JsonObject fetchProjectDetails(RequestContext req, HttpServletResponse res) {

		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");

		JsonObject returnJSON = new JsonObject();
		Dataset _projects = _gcRPC.getProjectDetails(duration, projectName);
		List<ProjectInfo> projectsList = new ArrayList<ProjectInfo>();
		if(_projects != null)
		{

			int noOfProjects = _projects.getRowCount();
			for(int i=0; i<noOfProjects; i++)
			{

				ProjectInfo oneProjectInfo = new ProjectInfo();
				if(_projects.getValueAt(i, 0) != null)
				{
					oneProjectInfo.projectName = _projects.getValueAt(i, 0).toString();

				}
				else
				{
					oneProjectInfo.projectName= "";
				}
				if(_projects.getValueAt(i, 3) != null)
				{

					try {
						oneProjectInfo.avgSessionTime = _projects.getValueAt(i, 3).toString();

					} catch (Exception e) {
						log.error("fetchProjectDetails: "+e);
					}
				}
				else
				{
					oneProjectInfo.avgSessionTime = "00:00";
				}
				if(_projects.getValueAt(i, 1) != null)
				{
					oneProjectInfo.noOfUsers = (int) Float.parseFloat(_projects.getValueAt(i, 1).toString());

				}
				else
				{
					oneProjectInfo.noOfUsers= 0;
				}
				if(_projects.getValueAt(i, 2) != null)
				{
					oneProjectInfo.noOfSessions= (int) Float.parseFloat(_projects.getValueAt(i, 2).toString());

				}
				else
				{
					oneProjectInfo.noOfSessions = 0;
				}
				if(_projects.getValueAt(i, 4) != null)
				{
					oneProjectInfo.noOfActions= (int) Float.parseFloat(_projects.getValueAt(i, 4).toString());

				}
				else
				{
					oneProjectInfo.noOfActions= 0;
				}

				projectsList.add(oneProjectInfo);

			}
		}
		Gson gson = new GsonBuilder().create();
		String _projectsJSON = gson.toJson(projectsList);
		returnJSON.addProperty("projects", _projectsJSON);
		return returnJSON;
	}

	private JsonObject fetchProjectsNotAdded(RequestContext req, HttpServletResponse res) {


		JsonObject returnJSON = new JsonObject();
		String[] _projects = _gcRPC.getProjectNotAddedRoIgnitionAnalytics();
		Gson gson = new GsonBuilder().create();
		String _projectsJSON = gson.toJson(_projects);
		returnJSON.addProperty("projects", _projectsJSON);
		return returnJSON;
	}

	private JsonObject deleteProject(RequestContext req, HttpServletResponse res) {

		String projectName = req.getParameter("projectname");
		JsonObject returnJSON = new JsonObject();
		String[] _projects = _gcRPC.deleteAndGetUpdatedProjectsList(projectName);
		Gson gson = new GsonBuilder().create();
		String _projectsJSON = gson.toJson(_projects);
		returnJSON.addProperty("projects", _projectsJSON);
		return returnJSON;
	}

	private JsonObject addProject(RequestContext req, HttpServletResponse res) {

		String projectName = req.getParameter("projectname");
		JsonObject returnJSON = new JsonObject();
		List<String> projectNames = new ArrayList<>();
		projectNames.add(projectName);
		_gcRPC.addProjectsToModule(projectNames);
		String[] _projects = _gcRPC.getProjects("All Projects");
		Gson gson = new GsonBuilder().create();
		String _projectsJSON = gson.toJson(_projects);
		returnJSON.addProperty("projects", _projectsJSON);
		return returnJSON;
	}



	//*****************************************************************************************************************		
	//**************************************************************************************************************	
	private JsonObject fetchReport_OverviewByDate(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		// get data for By Date Report

		List<ReportDataPoint> listTableData = new ArrayList<ReportDataPoint>();
		List<ReportDataPoint> listChartData = new ArrayList<ReportDataPoint>();
		ReportDataPoint overviewData = new ReportDataPoint();

		int noOfPeople = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		Dataset _overview = _gcRPC.reoprtsGetOverviewByDate(duration, projectName, allProjects);
		if(_overview != null)
		{
			SimpleDateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
			SimpleDateFormat sdf = new SimpleDateFormat("EE, MMM dd, yyyy");
			int noOfRows = _overview.getRowCount();
			int i = 0;
			int val1, val2, val3;
			int t_val1 = 0, t_val2 = 0, t_val3 = 0;

			Date xDate = null;
			String dateValTable = "";

			int startVal = 0;


			//handling special logic for last 365 display
			if(duration == Constants.LAST_365_DAYS)
			{
				Integer curMonth = Calendar.getInstance().get(Calendar.MONTH) ;

				System.out.println("curMonth : " + curMonth);
				if(curMonth == 11)
				{
					curMonth = 1;
				}
				else 
				{
					curMonth = curMonth + 2;
				}

				startVal = Constants.binarySearchOnDataset(4,  curMonth, _overview);

				if(startVal < 0)
				{
					startVal = 0;
				}
				for(i= startVal -1 ; i >= 0 ; i--)
				{
					String dateVal = _overview.getValueAt(i, 0).toString();
					dateValTable = dateVal;
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(_overview.getValueAt(i, 1) != null)
					{
						val1 = (int)Float.parseFloat(_overview.getValueAt(i, 1).toString());
						t_val1 = t_val1 + val1;
					}

					//_lineData.addValue(val1, series1, dateVal);
					if(_overview.getValueAt(i, 2) != null)
					{
						val2 = (int)Float.parseFloat(_overview.getValueAt(i, 2).toString());
						t_val2 = t_val2 + val2;
					}
					//_lineData.addValue(val2, series2, dateVal);

					if(_overview.getValueAt(i, 3) != null)
					{
						val3 = (int)Float.parseFloat(_overview.getValueAt(i, 3).toString());
						t_val3 = t_val3 + val3;
					}
					//_lineData.addValue(val3, series3, dateVal);

					overviewData = new ReportDataPoint();
					overviewData.dateValTable = dateVal;
					overviewData.val1 = val1;
					overviewData.val2 = val2;
					overviewData.val3 = val3;
					listTableData.add(overviewData);
				}

				for(i= noOfRows - 1 ; i >= startVal ; i--)
				{
					String	 dateVal = _overview.getValueAt(i, 0).toString();
					dateValTable = dateVal;
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(_overview.getValueAt(i, 1) != null)
					{
						val1 = (int)Float.parseFloat(_overview.getValueAt(i, 1).toString());
						t_val1 = t_val1 + val1;
					}

					//_lineData.addValue(val1, series1, dateVal);
					if(_overview.getValueAt(i, 2) != null)
					{
						val2 = (int)Float.parseFloat(_overview.getValueAt(i, 2).toString());
						t_val2 = t_val2 + val2;
					}
					//_lineData.addValue(val2, series2, dateVal);

					if(_overview.getValueAt(i, 3) != null)
					{
						val3 = (int)Float.parseFloat(_overview.getValueAt(i, 3).toString());
						t_val3 = t_val3 + val3;
					}
					//_lineData.addValue(val3, series3, dateVal);

					overviewData = new ReportDataPoint();
					overviewData.dateValTable = dateValTable;
					overviewData.val1 = val1;
					overviewData.val2 = val2;
					overviewData.val3 = val3;
					listTableData.add(overviewData);
				}
			}

			else
			{
				for(i=startVal; i<noOfRows; i++)
				{
					String dateVal = _overview.getValueAt(i, 0).toString();
					if(duration != Constants.TODAY && duration != Constants.YESTERDAY
							&& duration != Constants.LAST_365_DAYS && duration != Constants.THIS_YEAR
							&& duration != Constants.LAST_YEAR)
					{
						try {
							xDate = df.parse(dateVal);
							dateValTable = sdf.format(xDate);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							log.error("fetchReport_OverviewByDate:" +e);
						}
					}
					else
					{
						dateValTable = dateVal;
					}
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(_overview.getValueAt(i, 1) != null)
					{
						val1 = (int)Float.parseFloat(_overview.getValueAt(i, 1).toString());
						t_val1 = t_val1 + val1;
					}

					//_lineData.addValue(val1, series1, dateVal);
					if(_overview.getValueAt(i, 2) != null)
					{
						val2 = (int)Float.parseFloat(_overview.getValueAt(i, 2).toString());
						t_val2 = t_val2 + val2;
					}
					//_lineData.addValue(val2, series2, dateVal);

					if(_overview.getValueAt(i, 3) != null)
					{
						val3 = (int)Float.parseFloat(_overview.getValueAt(i, 3).toString());
						t_val3 = t_val3 + val3;
					}
					//_lineData.addValue(val3, series3, dateVal);

					overviewData = new ReportDataPoint();
					overviewData.dateValTable = dateValTable;
					overviewData.val1 = val1;
					overviewData.val2 = val2;
					overviewData.val3 = val3;
					listTableData.add(overviewData);
				}
			}
			/**
			 * 
			 **/
			String _listOverviewDataJSON = gson.toJson(listTableData);
			returnJSON.addProperty("tabledata", _listOverviewDataJSON);
			/**
			 * 
			 **/

			//overviewData.addRow(new Object[]{ noOfRows + " Result(s)", noOfPeople + " People", t_val2 + " Visits", t_val3 + " Actions"});
			/**
			 * 
			 **/
			String _noOfRowsJSON = gson.toJson(noOfRows);
			returnJSON.addProperty("noofrows", _noOfRowsJSON);

			String _noOfPeopleJSON = gson.toJson(noOfPeople);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _noOfVisitsJSON = gson.toJson(t_val2);
			returnJSON.addProperty("noofvisits", _noOfVisitsJSON);

			String _noOfActionsJSON = gson.toJson(t_val3);
			returnJSON.addProperty("noofActions", _noOfActionsJSON);
			/**
			 * 
			 **/
			//Data for Graph
			if(duration == Constants.LAST_365_DAYS)
			{
				for(i=startVal; i<noOfRows; i++)
				{
					String dateVal = "";
					dateVal = _overview.getValueAt(i, 0).toString();
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(_overview.getValueAt(i, 1) != null)
					{
						val1 = (int)Float.parseFloat(_overview.getValueAt(i, 1).toString());
					}


					//_lineData.addValue(val1, series1, dateVal);

					if(_overview.getValueAt(i, 2) != null)
					{
						val2 = (int)Float.parseFloat(_overview.getValueAt(i, 2).toString());
					}


					//_lineData.addValue(val2, series2, dateVal);

					if(_overview.getValueAt(i, 3) != null)
					{
						val3 = (int)Float.parseFloat(_overview.getValueAt(i, 3).toString());
					}
					overviewData = new ReportDataPoint();
					overviewData.dateValTable = dateVal;
					overviewData.val1 = val1;
					overviewData.val2 = val2;
					overviewData.val3 = val3;
					listChartData.add(overviewData);
					//_lineData.addValue(val3, series3, dateVal);
				}
				for(i=0; i<startVal; i++)
				{
					String dateVal = "";

					dateVal = _overview.getValueAt(i, 0).toString();

					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(_overview.getValueAt(i, 1) != null)
					{
						val1 = (int)Float.parseFloat(_overview.getValueAt(i, 1).toString());
					}

					//_lineData.addValue(val1, series1, dateVal);
					if(_overview.getValueAt(i, 2) != null)
					{
						val2 = (int)Float.parseFloat(_overview.getValueAt(i, 2).toString());
					}

					//_lineData.addValue(val2, series2, dateVal);

					if(_overview.getValueAt(i, 3) != null)
					{
						val3 = (int)Float.parseFloat(_overview.getValueAt(i, 3).toString());
					}

					overviewData = new ReportDataPoint();
					overviewData.dateValTable = dateVal;
					overviewData.val1 = val1;
					overviewData.val2 = val2;
					overviewData.val3 = val3;
					listChartData.add(overviewData);
					//_lineData.addValue(val3, series3, dateVal);
				}
			}
			else
			{
				for(i= noOfRows - 1 ; i >= 0 ; i--)
				{
					String dateVal = "";
					if(duration != Constants.TODAY && duration != Constants.YESTERDAY
							&& duration != Constants.LAST_365_DAYS && duration != Constants.THIS_YEAR
							&& duration != Constants.LAST_YEAR)
					{
						dateVal = _overview.getValueAt(i, 0).toString().substring(0, 10);
					}
					else
					{
						dateVal = _overview.getValueAt(i, 0).toString();
					}
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(_overview.getValueAt(i, 1) != null)
					{
						val1 = (int)Float.parseFloat(_overview.getValueAt(i, 1).toString());
					}

					//_lineData.addValue(val1, series1, dateVal);
					if(_overview.getValueAt(i, 2) != null)
					{
						val2 = (int)Float.parseFloat(_overview.getValueAt(i, 2).toString());
					}

					//_lineData.addValue(val2, series2, dateVal);

					if(_overview.getValueAt(i, 3) != null)
					{
						val3 = (int)Float.parseFloat(_overview.getValueAt(i, 3).toString());
					}

					//_lineData.addValue(val3, series3, dateVal);
					overviewData = new ReportDataPoint();
					overviewData.dateValTable = dateVal;
					overviewData.val1 = val1;
					overviewData.val2 = val2;
					overviewData.val3 = val3;
					listChartData.add(overviewData);
				}
			}
			String _listLineChartInfoJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listLineChartInfoJSON);
		}
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************	
	private JsonObject fetchReport_Cities(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************
		int noOfPeople = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		Dataset reportData = _gcRPC.getCitiesReportData(projectName, allProjects, duration);

		List<ReportBarChartInfo> listTableData = new ArrayList<ReportBarChartInfo>();
		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int totalNoOfPeople = 0;
		int totalNoOfVisits = 0;
		int totalNoOfActions = 0;
		int peopleVal = 0;
		int actionVal = 0;
		int visitsVal = 0;
		String screen = "";

		if(reportData!=null)
		{	
			int size = reportData.getRowCount();

			for( int i = 0 ; i < size ; i++)
			{
				screen = reportData.getValueAt(i, "Cities").toString();

				//code to remove repeated comma when state is null or city is null 
				screen = screen.trim();
				while(screen.startsWith(","))
				{
					screen = screen.substring(1);
				}
				if (screen.endsWith(",")) {
					screen = screen.replaceAll(",", "");
				}
				screen = screen.replace(", null, ", ", ");
				screen = screen.replace("false, ", " ");
				screen = screen.replace("null", "None");

				if(reportData.getValueAt(i, "People") != null)
				{
					peopleVal = (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				}
				if(reportData.getValueAt(i , "Actions") != null)
				{
					actionVal = (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString());
				}
				if(reportData.getValueAt(i, "noOfScreens") != null)
				{
					actionVal = actionVal + (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
				}
				if(reportData.getValueAt(i, "Visits") != null)
				{
					visitsVal = (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
				}

				totalNoOfPeople = totalNoOfPeople + peopleVal;
				totalNoOfVisits= totalNoOfVisits + visitsVal;
				totalNoOfActions =  totalNoOfActions + actionVal;
				if(i < 10) //top 10 cities only in the graph.
				{
					chartData = new ReportBarChartInfo();
					chartData.people = peopleVal;
					chartData.visits = visitsVal;
					chartData.actions = actionVal;
					chartData.axislabel = screen;
					listChartData.add(chartData);
				}
				chartData = new ReportBarChartInfo();
				chartData.people = peopleVal;
				chartData.visits = visitsVal;
				chartData.actions = actionVal;
				chartData.axislabel = screen;
				listTableData.add(chartData);
				//screenDepthTableModel.addRow(new Object[]{screen, peopleVal,visitsVal,actionVal});
			}
			String _listCitiesDataJSON = gson.toJson(listTableData);
			returnJSON.addProperty("tabledata", _listCitiesDataJSON);

			//screenDepthTableModel.addRow(new Object[]{size + " Result(s)", noOfPeople + " People",totalNoOfVisits + " Visits",totalNoOfActions + " Actions"});
			String _sizeJSON = gson.toJson(size);
			returnJSON.addProperty("noofrecords", _sizeJSON);

			String _noOfPeopleJSON = gson.toJson(noOfPeople);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(totalNoOfVisits);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(totalNoOfActions);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);

			//to create dummy records in case dataset is less.
			/*if(size < 10)
			{
				for(int i=size-1; i<10; i++)
				{
					chartData = new ReportBarChartInfo();
					chartData.people = 0;
					chartData.visits = 0;
					chartData.actions = 0;
					chartData.axislabel = "		";
					listChartData.add(chartData);
					//dataset.addValue(0 , series1, "    " );
					//dataset.addValue(0 , series2, "    ");
					//dataset.addValue(0 , series3, "    ");
				}
			}*/
			String _listLineChartInfoJSON = gson.toJson(listChartData);
			returnJSON.addProperty("linechartdata", _listLineChartInfoJSON);
		}
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************	
	private JsonObject fetchReport_Groups(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	

		int noOfPeople = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		List<GroupReportRecord> grpReportData = _gcRPC.getGroupsReportData(projectName, allProjects, duration);

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int totalNoOfPeople = 0;
		int totalNoOfVisits = 0;
		int totalNoOfActions = 0;
		int peopleVal = 0;
		int actionVal = 0;
		int visitsVal = 0;
		String groupName = "";

		int size = grpReportData.size();
		if(size > 0)
		{
			for( int i = 0 ; i < size ; i++)
			{
				groupName = grpReportData.get(i).getGroupName();
				peopleVal = grpReportData.get(i).getNoOfPeople();
				actionVal = grpReportData.get(i).getNoOfActions();
				visitsVal = grpReportData.get(i).getNoOfVisits();
				totalNoOfPeople = totalNoOfPeople + peopleVal;
				totalNoOfVisits= totalNoOfVisits + visitsVal;
				totalNoOfActions =  totalNoOfActions + actionVal;

				chartData = new ReportBarChartInfo();
				chartData.people = peopleVal;
				chartData.visits = visitsVal;
				chartData.actions = actionVal;
				chartData.axislabel = groupName;
				listChartData.add(chartData);

				//dataset.addValue(peopleVal , series1, groupName);
				//dataset.addValue(visitsVal , series2, groupName);
				//dataset.addValue( actionVal, series3, groupName);

				//groupsTableModel.addRow(new Object[]{groupName, peopleVal,visitsVal,actionVal});
			}
			String _listGroupsDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("tableandchartdata", _listGroupsDataJSON);

			//query the overview and get visits , actions
			OverviewInformation _overview = _gcRPC.getOverview(duration, projectName, allProjects);

			//groupsTableModel.addRow(new Object[]{size + " Result(s)", noOfPeople + " People",_overview.getNoOfSessions() + " Visits",_overview.getNoOfActions() + " Actions"});

			String _sizeJSON = gson.toJson(size);
			returnJSON.addProperty("size", _sizeJSON);

			String _noOfPeopleJSON = gson.toJson(noOfPeople);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(_overview.getNoOfSessions());
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(_overview.getNoOfActions());
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);
		}

		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_TopScreens(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	

		int noOfPeople = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		Dataset reportData = _gcRPC.getTopScreens(duration, projectName, allProjects);

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		List<ReportBarChartInfo> listTableData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		if(reportData != null)
		{
			int noOfRecords = reportData.getRowCount();
			String categoryName = "";
			int t_val1 = 0, t_val2 = 0;

			//build the dataset for bar chart
			for(int i=0; i<noOfRecords; i++)
			{
				categoryName = reportData.getValueAt(i, 0).toString();
				int val1 = (int)Float.parseFloat(reportData.getValueAt(i, 1).toString());
				int val2 = (int)Float.parseFloat(reportData.getValueAt(i, 2).toString());
				if(i < 10)
				{
					chartData = new ReportBarChartInfo();
					chartData.people = val1;
					chartData.visits = 0;
					chartData.actions = val2;
					chartData.axislabel = categoryName;
					listChartData.add(chartData);
					//dataset.addValue(val1, series1, categoryName);
					//dataset.addValue(val2, series2, categoryName);
				}
				t_val1 = t_val1 + val1;
				t_val2 = t_val2 + val2;
				chartData = new ReportBarChartInfo();
				chartData.people = val1;
				chartData.visits = 0;
				chartData.actions = val2;
				chartData.axislabel = categoryName;
				listTableData.add(chartData);

				//screensData.addRow(new Object[]{categoryName, val1, val2});
			}

			// screensData.addRow(new Object[]{noOfRecords + " Results(s)", this.noOfPeople + " People", t_val2 + " Actions"});

			if(noOfRecords < 10)
			{
				for( int i=noOfRecords-1; i<10; i++)
				{
					chartData = new ReportBarChartInfo();
					chartData.people = 0;
					chartData.visits = 0;
					chartData.actions = 0;
					chartData.axislabel = " ";
					listChartData.add(chartData);
					//dataset.addValue(0 , series1, " ");
					//dataset.addValue(0 , series2, " ");

				}
			}

			String _listTopScreensDataJSON = gson.toJson(listTableData);
			returnJSON.addProperty("tabledata", _listTopScreensDataJSON);

			String _listLineChartInfoJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listLineChartInfoJSON);

			String _sizeJSON = gson.toJson(noOfRecords);
			returnJSON.addProperty("noofrecords", _sizeJSON);

			String _noOfPeopleJSON = gson.toJson(noOfPeople);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfActionsJSON = gson.toJson(t_val2);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);

		}

		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_BounceRate(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		List<ReportDataPoint> listTableData = new ArrayList<ReportDataPoint>();
		List<ReportDataPoint> listChartData = new ArrayList<ReportDataPoint>();
		ReportDataPoint bounceRateData = new ReportDataPoint();


		SimpleDateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
		SimpleDateFormat sdf = new SimpleDateFormat("EE, MMM dd, yyyy");
		SimpleDateFormat chartDF = new SimpleDateFormat("EE MMM dd");

		int noOfPeople = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		Dataset bRateData = _gcRPC.getBounceRateReportByDate(duration, projectName, allProjects);
		float bounceRate = _gcRPC.getBounceRate(duration, projectName, allProjects);
		if(bRateData != null)
		{
			int noOfRows = bRateData.getRowCount();
			int i = 0;
			int val1, val2, val3;
			int t_val1 = 0, t_val2 = 0, t_val3 = 0;

			Date xDate = null;

			int startVal = 0;
			//handling special logic for last 365 display
			if(duration == Constants.LAST_365_DAYS)
			{
				Integer curMonth = Calendar.getInstance().get(Calendar.MONTH) ;

				System.out.println("curMonth : " + curMonth);
				if(curMonth == 11)
				{
					curMonth = 1;
				}
				else 
				{
					curMonth = curMonth + 2;
				}

				startVal = Constants.binarySearchOnDataset(4,  curMonth, bRateData);
				if(startVal < 0)
				{
					startVal = 0;
				}

				for(i= startVal -1 ; i >= 0 ; i--)
				{
					String dateVal = bRateData.getValueAt(i, 0).toString();
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(bRateData.getValueAt(i, 2) != null)
					{
						val1 = (int)Float.parseFloat(bRateData.getValueAt(i, 2).toString());
						t_val1 = t_val1 + val1;
					}

					if(bRateData.getValueAt(i, 3) != null)
					{
						val2 = (int)Float.parseFloat(bRateData.getValueAt(i, 3).toString());
						t_val2 = t_val2 + val2;
					}

					if(bRateData.getValueAt(i, 1) != null)
					{
						val3 = (int)Float.parseFloat(bRateData.getValueAt(i, 1).toString());
						t_val3 = t_val3 + val3;
					}

					bounceRateData = new ReportDataPoint();
					bounceRateData.dateValTable = dateVal;
					bounceRateData.val1 = val1;
					bounceRateData.val2 = val2;
					bounceRateData.val3 = val3;
					listTableData.add(bounceRateData);
					//overviewData.addRow(new Object[]{dateVal, val1, val2, val3});
				}
				for(i= noOfRows - 1 ; i >= startVal ; i--)
				{
					String dateVal = bRateData.getValueAt(i, 0).toString();
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(bRateData.getValueAt(i, 2) != null)
					{
						val1 = (int)Float.parseFloat(bRateData.getValueAt(i, 2).toString());
						t_val1 = t_val1 + val1;
					}

					if(bRateData.getValueAt(i, 3) != null)
					{
						val2 = (int)Float.parseFloat(bRateData.getValueAt(i, 3).toString());
						t_val2 = t_val2 + val2;
					}

					if(bRateData.getValueAt(i, 1) != null)
					{
						val3 = (int)Float.parseFloat(bRateData.getValueAt(i, 1).toString());
						t_val3 = t_val3 + val3;
					}

					bounceRateData = new ReportDataPoint();
					bounceRateData.dateValTable = dateVal;
					bounceRateData.val1 = val1;
					bounceRateData.val2 = val2;
					bounceRateData.val3 = val3;
					listTableData.add(bounceRateData);
					//overviewData.addRow(new Object[]{dateVal, val1, val2, val3});
				}
			}

			else
			{
				for(i=noOfRows-1; i>=0; i--)
				{

					String dateVal = bRateData.getValueAt(i, 0).toString();
					if(duration != Constants.TODAY && duration != Constants.YESTERDAY && duration != Constants.LAST_365_DAYS && duration != Constants.THIS_YEAR && duration != Constants.LAST_YEAR){
						try {
							xDate = df.parse(dateVal);
							dateVal = sdf.format(xDate);
						} catch (ParseException e) {

							log.error(e);
						}
					}

					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(bRateData.getValueAt(i, 2) != null)
					{
						val1 = (int)Float.parseFloat(bRateData.getValueAt(i, 2).toString());
						t_val1 = t_val1 + val1;
					}

					if(bRateData.getValueAt(i, 3) != null)
					{
						val2 = (int)Float.parseFloat(bRateData.getValueAt(i, 3).toString());
						t_val2 = t_val2 + val2;
					}

					if(bRateData.getValueAt(i, 1) != null)
					{
						val3 = (int)Float.parseFloat(bRateData.getValueAt(i, 1).toString());
						t_val3 = t_val3 + val3;
					}

					bounceRateData = new ReportDataPoint();
					bounceRateData.dateValTable = dateVal;
					bounceRateData.val1 = val1;
					bounceRateData.val2 = val2;
					bounceRateData.val3 = val3;
					listTableData.add(bounceRateData);
					//overviewData.addRow(new Object[]{dateVal, val1, val2, val3});
				}
			}
			if(noOfRows > 0)
			{
				bounceRate = (bounceRate) * 100;
				t_val3 =(int)bounceRate ; // to get the average
			}

			//overviewData.addRow(new Object[]{noOfRows + " Results(s)", this.noOfPeople + " People", t_val2 + " Visits", t_val3 + " % Bounce Rate"});

			//create the line data in ascending order
			if(duration == Constants.LAST_365_DAYS)
			{
				for(i=startVal; i<noOfRows; i++)
				{
					String dateVal = bRateData.getValueAt(i, 0).toString();

					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(bRateData.getValueAt(i, 2) != null)
					{
						val1 = (int)Float.parseFloat(bRateData.getValueAt(i, 2).toString());
					}

					if(bRateData.getValueAt(i, 3) != null)
					{
						val2 = (int)Float.parseFloat(bRateData.getValueAt(i, 3).toString());
					}
					if(bRateData.getValueAt(i, 1) != null)
					{
						val3 = (int)Float.parseFloat(bRateData.getValueAt(i, 1).toString());
					}

					bounceRateData = new ReportDataPoint();
					bounceRateData.dateValTable = dateVal;
					bounceRateData.val1 = val1;
					bounceRateData.val2 = val2;
					bounceRateData.val3 = val3;
					listChartData.add(bounceRateData);
					//_lineData.addValue(val1, series1, dateVal);
					//_lineData.addValue(val2, series2, dateVal);
					//_lineData.addValue(val3, series3, dateVal);
				}
				for(i=0; i<startVal; i++)
				{
					String dateVal = bRateData.getValueAt(i, 0).toString();

					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(bRateData.getValueAt(i, 2) != null)
					{
						val1 = (int)Float.parseFloat(bRateData.getValueAt(i, 2).toString());
					}

					if(bRateData.getValueAt(i, 3) != null)
					{
						val2 = (int)Float.parseFloat(bRateData.getValueAt(i, 3).toString());
					}

					if(bRateData.getValueAt(i, 1) != null)
					{
						val3 = (int)Float.parseFloat(bRateData.getValueAt(i, 1).toString());
					}
					bounceRateData = new ReportDataPoint();
					bounceRateData.dateValTable = dateVal;
					bounceRateData.val1 = val1;
					bounceRateData.val2 = val2;
					bounceRateData.val3 = val3;
					listChartData.add(bounceRateData);
					//_lineData.addValue(val1, series1, dateVal);
					//_lineData.addValue(val2, series2, dateVal);
					//_lineData.addValue(val3, series3, dateVal);
				}
			}
			else
			{
				for(i=0; i<noOfRows; i++)
				{

					String dateVal = bRateData.getValueAt(i, 0).toString();
					if(duration != Constants.TODAY && duration != Constants.YESTERDAY && duration != Constants.LAST_365_DAYS && duration != Constants.THIS_YEAR && duration != Constants.LAST_YEAR){
						try {
							xDate = df.parse(dateVal);
							dateVal = chartDF.format(xDate);
						} catch (ParseException e) {

							log.error(e);
						}
					}
					val1 = 0;
					val2 = 0;
					val3 = 0;
					if(bRateData.getValueAt(i, 2) != null)
					{
						val1 = (int)Float.parseFloat(bRateData.getValueAt(i, 2).toString());
					}

					if(bRateData.getValueAt(i, 3) != null)
					{
						val2 = (int)Float.parseFloat(bRateData.getValueAt(i, 3).toString());
					}

					if(bRateData.getValueAt(i, 1) != null)
					{
						val3 = (int)Float.parseFloat(bRateData.getValueAt(i, 1).toString());
					}
					bounceRateData = new ReportDataPoint();
					bounceRateData.dateValTable = dateVal;
					bounceRateData.val1 = val1;
					bounceRateData.val2 = val2;
					bounceRateData.val3 = val3;
					listChartData.add(bounceRateData);

					//_lineData.addValue(val1, series1, dateVal);
					//_lineData.addValue(val2, series2, dateVal);
					//_lineData.addValue(val3, series3, dateVal);

				}
			}

			String _listBounceRateDataJSON = gson.toJson(listTableData);
			returnJSON.addProperty("tabledata", _listBounceRateDataJSON);

			String _listLineChartInfoJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listLineChartInfoJSON); 

			String _sizeJSON = gson.toJson(noOfRows);
			returnJSON.addProperty("noofrecords", _sizeJSON);

			String _noOfPeopleJSON = gson.toJson(noOfPeople);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(t_val2);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _BounceRateJSON = gson.toJson(t_val3);
			returnJSON.addProperty("bouncerate", _BounceRateJSON);

		}

		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_DeviceTypes(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************
		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int noOfUsers = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		Dataset reportData = _gcRPC.getDeviceTypeReport(duration, projectName, allProjects);
		if(reportData !=null)
		{
			int noOfRecords = reportData.getRowCount();

			String category = "";
			int noOfPeople = 0;
			int noOfActions = 0;
			int noOfvisits = 0;

			int totalNoOfPeople = 0; int totalNoOfActions = 0, totalNoOfVisits = 0;
			for (int i = 0 ; i< noOfRecords ; i++ )
			{
				category = "";
				noOfPeople = 0;
				noOfActions = 0;
				noOfvisits = 0;

				if(reportData.getValueAt(i, 0) != null)
				{
					category = reportData.getValueAt(i, 0).toString();
				}
				if(reportData.getValueAt(i, 1) != null)
				{
					noOfPeople = (int)Float.parseFloat(reportData.getValueAt(i, 1).toString());
				}
				if(reportData.getValueAt(i, 2) != null)
				{
					noOfvisits = (int)Float.parseFloat(reportData.getValueAt(i, 2).toString());
				}
				if(reportData.getValueAt(i, 3) != null)
				{
					noOfActions = (int)Float.parseFloat(reportData.getValueAt(i, 3).toString());
				}
				if(reportData.getValueAt(i, 4) != null)
				{
					noOfActions = noOfActions + (int) Float.parseFloat(reportData.getValueAt(i, 4).toString());
				}

				chartData = new ReportBarChartInfo();
				chartData.people = noOfPeople;
				chartData.visits = noOfvisits;
				chartData.actions = noOfActions;
				chartData.axislabel = category;
				listChartData.add(chartData);

				totalNoOfPeople = totalNoOfPeople + noOfPeople;
				totalNoOfVisits = totalNoOfVisits + noOfvisits;
				totalNoOfActions = totalNoOfActions  + noOfActions;
			}

			
			String _listDeviceDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("tabledata", _listDeviceDataJSON);

			//devicesData.addRow(new Object[]{noOfRecords + " Result(s)",this.noOfPeople + " People",totalNoOfVisits + " Visits", totalNoOfActions + " Actions"});

			//to create dummy records in case dataset is less for chart.
			/*if(noOfRecords < 10)
			{
				for(int i=noOfRecords-1; i<10; i++)
				{
					chartData = new ReportBarChartInfo();
					chartData.people = 0;
					chartData.visits = 0;
					chartData.actions = 0;
					chartData.axislabel = "		";
					listChartData.add(chartData);

				}
			}*/


			String _sizeJSON = gson.toJson(noOfRecords);
			returnJSON.addProperty("noofrecords", _sizeJSON);

			String _noOfPeopleJSON = gson.toJson(noOfUsers);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(totalNoOfVisits);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(totalNoOfActions);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);

			String _listChartDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listChartDataJSON);

		}
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_Platforms(RequestContext req, HttpServletResponse res) 
	{	
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int noOfUsers = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		Dataset reportData = _gcRPC.getPlatformReport(duration, projectName, allProjects);
		if(reportData !=null)
		{
			int noOfRecords = reportData.getRowCount();

			String category = "";
			int noOfPeople = 0;
			int noOfActions = 0;
			int noOfvisits = 0;

			int totalNoOfPeople = 0; int totalNoOfActions = 0, totalNoOfVisits = 0;
			for (int i = 0 ; i< noOfRecords ; i++ )
			{
				category = "";
				noOfPeople = 0;
				noOfActions = 0;
				noOfvisits = 0;

				if(reportData.getValueAt(i, 0) != null)
				{
					category = reportData.getValueAt(i, 0).toString();
				}
				if(reportData.getValueAt(i, 1) != null)
				{
					noOfPeople = (int)Float.parseFloat(reportData.getValueAt(i, 1).toString());
				}
				if(reportData.getValueAt(i, 2) != null)
				{
					noOfvisits = (int)Float.parseFloat(reportData.getValueAt(i, 2).toString());
				}
				if(reportData.getValueAt(i, 3) != null)
				{
					noOfActions = (int)Float.parseFloat(reportData.getValueAt(i, 3).toString());
				}
				if(reportData.getValueAt(i, 4) != null)
				{
					noOfActions = noOfActions + (int) Float.parseFloat(reportData.getValueAt(i, 4).toString());
				}

				chartData = new ReportBarChartInfo();
				chartData.people = noOfPeople;
				chartData.visits = noOfvisits;
				chartData.actions = noOfActions;
				chartData.axislabel = category;
				listChartData.add(chartData);

				totalNoOfPeople = totalNoOfPeople + noOfPeople;
				totalNoOfVisits = totalNoOfVisits + noOfvisits;
				totalNoOfActions = totalNoOfActions  + noOfActions;
			}

			
			String _listDeviceDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("tabledata", _listDeviceDataJSON);

			//devicesData.addRow(new Object[]{noOfRecords + " Result(s)",this.noOfPeople + " People",totalNoOfVisits + " Visits", totalNoOfActions + " Actions"});

			//to create dummy records in case dataset is less for chart.
			/*if(noOfRecords < 10)
			{
				for(int i=noOfRecords-1; i<10; i++)
				{
					chartData = new ReportBarChartInfo();
					chartData.people = 0;
					chartData.visits = 0;
					chartData.actions = 0;
					chartData.axislabel = "		";
					listChartData.add(chartData);

				}
			}*/

			String _sizeJSON = gson.toJson(noOfRecords);
			returnJSON.addProperty("noofrecords", _sizeJSON);

			String _noOfPeopleJSON = gson.toJson(noOfUsers);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(totalNoOfVisits);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(totalNoOfActions);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);

			String _listChartDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listChartDataJSON);

		}
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_Browsers(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		int noOfUsers = _gcRPC.getDistinctUsersFromBrowsers(duration, projectName, allProjects);
		Dataset reportData = _gcRPC.getBrowserReport(duration, projectName, allProjects);

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();
		if(reportData !=null)
		{
			int noOfRecords = reportData.getRowCount();

			String category = "";
			int noOfPeople = 0;
			int noOfActions = 0;
			int noOfvisits = 0;

			int totalNoOfPeople = 0; int totalNoOfActions = 0, totalNoOfVisits = 0;
			for (int i = 0 ; i< noOfRecords ; i++ )
			{
				category = "";
				noOfPeople = 0;
				noOfActions = 0;
				noOfvisits = 0;

				if(reportData.getValueAt(i, 0) != null)
				{
					category = reportData.getValueAt(i, 0).toString();
				}
				if(reportData.getValueAt(i, 1) != null)
				{
					noOfPeople = (int)Float.parseFloat(reportData.getValueAt(i, 1).toString());
				}
				if(reportData.getValueAt(i, 2) != null)
				{
					noOfvisits = (int)Float.parseFloat(reportData.getValueAt(i, 2).toString());
				}
				if(reportData.getValueAt(i, 3) != null)
				{
					noOfActions = (int)Float.parseFloat(reportData.getValueAt(i, 3).toString());
				}
				if(reportData.getValueAt(i, 4) != null)
				{
					noOfActions = noOfActions + (int) Float.parseFloat(reportData.getValueAt(i, 4).toString());
				}

				chartData = new ReportBarChartInfo();
				chartData.people = noOfPeople;
				chartData.visits = noOfvisits;
				chartData.actions = noOfActions;
				chartData.axislabel = category;
				listChartData.add(chartData);

				totalNoOfPeople = totalNoOfPeople + noOfPeople;
				totalNoOfVisits = totalNoOfVisits + noOfvisits;
				totalNoOfActions = totalNoOfActions  + noOfActions;
			}

			String _listDeviceDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("tabledata", _listDeviceDataJSON);
			//browsersData.addRow(new Object[]{noOfRecords + " Result(s)",noOfUsers + " People",totalNoOfVisits + " Visits", totalNoOfActions + " Actions"});


			//to create dummy records in case dataset is less.
			/*if(noOfRecords < 10)
			{
				for(int i=noOfRecords-1; i<10; i++)
				{
					chartData = new ReportBarChartInfo();
					chartData.people = 0;
					chartData.visits = 0;
					chartData.actions = 0;
					chartData.axislabel = "		";
					listChartData.add(chartData);
				}
			}*/

			String _sizeJSON = gson.toJson(noOfRecords);
			returnJSON.addProperty("noofrecords", _sizeJSON);

			String _noOfPeopleJSON = gson.toJson(noOfUsers);
			returnJSON.addProperty("noofusers", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(totalNoOfVisits);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(totalNoOfActions);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);

			String _listChartDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listChartDataJSON);

		}
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_ScreenResolutions(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		int noOfUsers = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);
		Dataset reportData = _gcRPC.getScreenResolutionData(projectName, allProjects, duration);

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int size = 0;
		int totalNoOfPeople = 0;
		int totalNoOfVisits = 0;
		int totalNoOfActions = 0;
		size = reportData.getRowCount();

		for( int i = 0 ; i < size ; i++)
		{
			String screen = reportData.getValueAt(i, "SCREEN_RESOLUTION").toString();
			int peopleVal = (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
			int actionVal = (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString());
			actionVal = actionVal + (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
			int visitsVal = (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());

			chartData = new ReportBarChartInfo();
			chartData.people = peopleVal;
			chartData.visits = visitsVal;
			chartData.actions = actionVal;
			chartData.axislabel = screen;
			listChartData.add(chartData);


			totalNoOfPeople = totalNoOfPeople + peopleVal;
			totalNoOfVisits = totalNoOfVisits + visitsVal;
			totalNoOfActions = totalNoOfActions  + actionVal;
		}

		
		String _listDeviceDataJSON = gson.toJson(listChartData);
		returnJSON.addProperty("tabledata", _listDeviceDataJSON);
		//screenDepthTableModel.addRow(new Object[]{size + " Result(s)",this.noOfPeople + " People",totalNoOfVisits + " Visits", totalNoOfActions + " Actions"});

		//to create dummy records in case dataset is less for chart.
		/*if(size < 10)
		{
			for(int i=size-1; i<10; i++)
			{
				chartData = new ReportBarChartInfo();
				chartData.people = 0;
				chartData.visits = 0;
				chartData.actions = 0;
				chartData.axislabel = "		";
				listChartData.add(chartData);

			}
		}*/


		String _sizeJSON = gson.toJson(size);
		returnJSON.addProperty("noofrecords", _sizeJSON);

		String _noOfPeopleJSON = gson.toJson(noOfUsers);
		returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

		String _totalNoOfVisitsJSON = gson.toJson(totalNoOfVisits);
		returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

		String _totalNoOfActionsJSON = gson.toJson(totalNoOfActions);
		returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);

		String _listChartDataJSON = gson.toJson(listChartData);
		returnJSON.addProperty("chartdata", _listChartDataJSON);

		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_ActionsPerVisit(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int noOfUsers = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);	

		Dataset reportData = _gcRPC.getActionsPerVisitReportInformation(projectName, allProjects, duration);

		int actionPerVisit_1_5People = 0 , actionPerVisit_1_5Visits = 0, actionPerVisit_1_5Actions = 0;
		int actionPerVisit_6to10_People = 0 , actionPerVisit_6to10_Visits = 0, actionPerVisit_6to10_Actions = 0;
		int actionPerVisit_10to20_People = 0 , actionPerVisit_10to20_Visits = 0, actionPerVisit_10to20_Actions = 0;
		int actionPerVisit_21to30_People = 0 , actionPerVisit_21to30_Visits = 0, actionPerVisit_21to30_Actions = 0;
		int actionPerVisit_31to50_People = 0 , actionPerVisit_31to50_Visits = 0, actionPerVisit_31to50_Actions = 0;
		int actionPerVisit_51plus_People = 0 , actionPerVisit_51plus_Visits = 0, actionPerVisit_51plus_Actions = 0;

		int size = 0;
		int totalVisits = 0, totalActions = 0;
		if(reportData != null)
		{
			size = reportData.getRowCount();
			String tempVal;

			for(int i = 0 ; i < size ; i ++)
			{
				if(reportData.getValueAt(i , "count_of_actions") != null )
				{
					tempVal = reportData.getValueAt(i , "count_of_actions").toString();
					if(tempVal.compareToIgnoreCase("NULL") == 0){
						//i++;
					}
					else if(tempVal.compareToIgnoreCase("1-5") == 0)
					{
						actionPerVisit_1_5People = actionPerVisit_1_5People +  (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
						actionPerVisit_1_5Visits = actionPerVisit_1_5Visits+(int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
						actionPerVisit_1_5Actions = actionPerVisit_1_5Actions+(int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
					}else if(tempVal.compareToIgnoreCase("6-10") == 0)
					{
						actionPerVisit_6to10_People = actionPerVisit_6to10_People +  (int)Float.parseFloat(reportData.getValueAt(i, "People").toString()); ;
						actionPerVisit_6to10_Visits = actionPerVisit_6to10_Visits+(int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
						actionPerVisit_6to10_Actions = actionPerVisit_6to10_Actions+(int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
					}else if(tempVal.compareToIgnoreCase("11-20") == 0)
					{
						actionPerVisit_10to20_People = actionPerVisit_10to20_People +  (int)Float.parseFloat(reportData.getValueAt(i, "People").toString()); ;
						actionPerVisit_10to20_Visits = actionPerVisit_10to20_Visits+(int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
						actionPerVisit_10to20_Actions = actionPerVisit_10to20_Actions+(int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
					}
					else if(tempVal.compareToIgnoreCase("21-30") == 0)
					{
						actionPerVisit_21to30_People = actionPerVisit_21to30_People +  (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());;
						actionPerVisit_21to30_Visits = actionPerVisit_21to30_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
						actionPerVisit_21to30_Actions = actionPerVisit_21to30_Actions+(int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
					}
					else if(tempVal.compareToIgnoreCase("31-50") == 0)
					{
						actionPerVisit_31to50_People = actionPerVisit_31to50_People +  (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());;
						actionPerVisit_31to50_Visits = actionPerVisit_31to50_Visits+(int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
						actionPerVisit_31to50_Actions = actionPerVisit_31to50_Actions+(int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
					}
					else if(tempVal.compareToIgnoreCase("51 or more") == 0)
					{
						actionPerVisit_51plus_People = actionPerVisit_51plus_People +  (int)Float.parseFloat(reportData.getValueAt(i, "People").toString()); ;
						actionPerVisit_51plus_Visits = actionPerVisit_51plus_Visits+(int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
						actionPerVisit_51plus_Actions = actionPerVisit_51plus_Actions+(int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
					}
				}

			}
			//data for graph

			chartData = new ReportBarChartInfo();
			chartData.people = actionPerVisit_1_5People;
			chartData.visits = actionPerVisit_1_5Visits;
			chartData.actions = actionPerVisit_1_5Actions;
			chartData.axislabel = "1 to 5";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = actionPerVisit_6to10_People;
			chartData.visits = actionPerVisit_6to10_Visits;
			chartData.actions = actionPerVisit_6to10_Actions;
			chartData.axislabel = "6 to 10";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = actionPerVisit_10to20_People;
			chartData.visits = actionPerVisit_10to20_Visits;
			chartData.actions = actionPerVisit_10to20_Actions;
			chartData.axislabel = "11 to 20";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = actionPerVisit_21to30_People;
			chartData.visits = actionPerVisit_21to30_Visits;
			chartData.actions = actionPerVisit_21to30_Actions;
			chartData.axislabel = "21 to 30";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = actionPerVisit_31to50_People;
			chartData.visits = actionPerVisit_31to50_Visits;
			chartData.actions = actionPerVisit_31to50_Actions;
			chartData.axislabel = "31 to 50";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = actionPerVisit_51plus_People;
			chartData.visits = actionPerVisit_51plus_Visits;
			chartData.actions = actionPerVisit_51plus_Actions;
			chartData.axislabel = "51 or More";
			listChartData.add(chartData);

			String _listScreenResolutionsDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("tabledata", _listScreenResolutionsDataJSON);

			//to create dummy records in case dataset is less.
			/*for(int dummy=0; dummy < 4; dummy++)
			{
				chartData = new ReportBarChartInfo();
				chartData.people = 0;
				chartData.visits = 0;
				chartData.actions = 0;
				chartData.axislabel = "		";
				listChartData.add(chartData);
			}*/
			String _listChartDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listChartDataJSON);


			totalVisits = actionPerVisit_1_5Visits + actionPerVisit_6to10_Visits + actionPerVisit_10to20_Visits
					+ actionPerVisit_21to30_Visits + actionPerVisit_31to50_Visits + actionPerVisit_51plus_Visits;

			totalActions = actionPerVisit_1_5Actions + actionPerVisit_6to10_Actions + actionPerVisit_10to20_Actions 
					+ actionPerVisit_21to30_Actions + actionPerVisit_31to50_Actions + actionPerVisit_51plus_Actions;
			//screenDepthTableModel.addRow(new Object[]{ "", this.noOfPeople + " People" , totalVisits + " Visits" , totalActions + " Actions"});
			//	 } 


			String _noOfPeopleJSON = gson.toJson(noOfUsers);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(totalVisits);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(totalActions);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);	

		}

		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_VisitDuration(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		log.error("fetchReport_VisitDuration : fetching repotrt for duration : " + duration + " , projectName : " + projectName + " , allProjects : " + allProjects);
		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int noOfUsers = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);	

		Dataset reportData = _gcRPC.getVisitDurationReportInformation(projectName, allProjects, duration);

		int people_0_5 = 0, visits_0_5 = 0,actions_0_5 = 0;
		int people_6_10 = 0,visits_6_10 = 0,actions_6_10 = 0;
		int people_11_20 =  0,visits_11_20 = 0,actions_11_20 = 0;
		int people_21_30 = 0,visits_21_30 = 0,actions_21_30 = 0;
		int people_31_40 = 0,visits_31_40 = 0,actions_31_40 =  0;
		int people_41_50 = 0,visits_41_50 = 0,actions_41_50 = 0;
		int people_51_60 = 0,visits_51_60 = 0,actions_51_60 = 0;
		int people_61_120 = 0,visits_61_120 = 0,actions_61_120 = 0;
		int people_120 = 0,visits_120 = 0, actions_120 = 0;

		int size =reportData.getRowCount();

		String tempVal = "" ;

		for(int i = 0 ; i < size ; i++)
		{

			if (reportData.getValueAt(i, "session_duration_mins") != null)
			{
				tempVal = reportData.getValueAt(i, "session_duration_mins").toString();
				if(tempVal.compareToIgnoreCase("0-5") == 0)
				{
					people_0_5 = people_0_5 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_0_5 = visits_0_5 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_0_5 = actions_0_5 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("11-20") == 0)
				{
					people_11_20 = people_11_20 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_11_20 = visits_11_20 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_11_20 = actions_11_20 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("120") == 0)
				{
					people_120 = people_120 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_120 = visits_120 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_120 = actions_120 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("21-30") == 0)
				{
					people_21_30 = people_21_30 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_21_30 = visits_21_30 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_21_30 = actions_21_30 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("31-40") == 0)
				{
					people_31_40 = people_31_40 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_31_40 = visits_31_40 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_31_40 = actions_31_40 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("41-50") == 0)
				{
					people_41_50 = people_41_50 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_41_50 = visits_41_50 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_41_50 = actions_41_50 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("51-60") == 0)
				{
					people_51_60 = people_51_60 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_51_60 = visits_51_60 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_51_60 = actions_51_60 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("6-10") == 0)
				{
					people_6_10 = people_6_10 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_6_10 = visits_6_10 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_6_10 = actions_6_10 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
				else if(tempVal.compareToIgnoreCase("61-120") == 0)
				{
					people_61_120 = people_61_120 + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
					visits_61_120 = visits_61_120 + (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());
					actions_61_120 = actions_61_120 + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i, "noOfScreens").toString());
				}
			}
		}

		chartData = new ReportBarChartInfo();
		chartData.people = people_0_5;
		chartData.visits = visits_0_5;
		chartData.actions = actions_0_5;
		chartData.axislabel = "0-5 min.";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = people_6_10;
		chartData.visits = visits_6_10;
		chartData.actions = actions_6_10;
		chartData.axislabel = "6-10 min.";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = people_11_20;
		chartData.visits = visits_11_20;
		chartData.actions = actions_11_20;
		chartData.axislabel = "11-20 min.";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = people_21_30;
		chartData.visits = visits_21_30;
		chartData.actions = actions_21_30;
		chartData.axislabel = "21-30 min.";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = people_31_40;
		chartData.visits = visits_31_40;
		chartData.actions = actions_31_40;
		chartData.axislabel = "31-40 min.";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = people_41_50;
		chartData.visits = visits_41_50;
		chartData.actions = actions_41_50;
		chartData.axislabel = "41-50 min.";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = people_51_60;
		chartData.visits = visits_51_60;
		chartData.actions = actions_51_60;
		chartData.axislabel = "51 min.-1 hr.";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = people_61_120;
		chartData.visits = visits_61_120;
		chartData.actions = actions_61_120;
		chartData.axislabel = "1 hr.-2 hr.";
		listChartData.add(chartData);	

		chartData = new ReportBarChartInfo();
		chartData.people = people_120;
		chartData.visits = visits_120;
		chartData.actions = actions_120;
		chartData.axislabel = "2+ hr.";
		listChartData.add(chartData);

		//compute the summary row 


		int totalVisits = visits_0_5 + visits_6_10 + visits_11_20 + visits_21_30 + visits_31_40
				+ visits_41_50 + visits_51_60 + visits_61_120 + visits_120;
		int totalActions = actions_0_5 + actions_6_10 + actions_11_20 + actions_21_30 + actions_31_40
				+ actions_41_50 + actions_51_60 + actions_61_120 + actions_120;
		//visitDutrationTableModel.addRow(new Object[]{"", this.noOfPeople + " People",totalVisits + " Visits",totalActions + " Actions"});

		String _listChartDataJSON = gson.toJson(listChartData);
		returnJSON.addProperty("tableandchartdata", _listChartDataJSON);	

		String _noOfPeopleJSON = gson.toJson(noOfUsers);
		returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

		String _totalNoOfVisitsJSON = gson.toJson(totalVisits);
		returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

		String _totalNoOfActionsJSON = gson.toJson(totalActions);
		returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);	
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************	
	//**************************************************************************************************************
	private JsonObject fetchReport_Engagement(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int noOfUsers = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);	

		Dataset reportData = _gcRPC.getEngagementReportInformationScreenDepth(projectName, allProjects, duration);

		int Screen_1_People = 0 , Screen_1_Visits = 0, Screen_1_Actions = 0;
		int Screen_2_People = 0 , Screen_2_Visits = 0, Screen_2_Actions = 0;
		int Screen_3_People = 0 , Screen_3_Visits = 0, Screen_3_Actions = 0;
		int Screen_4_People = 0 , Screen_4_Visits = 0, Screen_4_Actions = 0;
		int Screen_5_People = 0 , Screen_5_Visits = 0, Screen_5_Actions = 0;
		int Screen_6to10_People = 0 , Screen_6to10_Visits = 0, Screen_6to10_Actions = 0;
		int Screen_11to15_People = 0 , Screen_11to15_Visits = 0, Screen_11to15_Actions = 0;
		int Screen_16to20_People = 0 , Screen_16to20_Visits = 0, Screen_16to20_Actions = 0;
		int Screen_20Plus_People = 0 , Screen_20Plus_Visits = 0, Screen_20Plus_Actions = 0;

		int size = 0;

		size =reportData.getRowCount();
		int tempVal = 0;
		for (int i = 0 ; i< size ; i++ )
		{

			if(reportData.getValueAt(i, "no_of_screens") != null)
			{
				tempVal = (int)Float.parseFloat(reportData.getValueAt(i, "no_of_screens").toString());
			}
			else
			{
				tempVal = 0;
			}
			if(tempVal == 1  )
			{
				Screen_1_People = Screen_1_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_1_Actions = Screen_1_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString()) ;
				Screen_1_Visits = Screen_1_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal == 2 )
			{
				Screen_2_People = Screen_2_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_2_Actions = Screen_2_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_2_Visits = Screen_2_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal == 3 )
			{
				Screen_3_People = Screen_3_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_3_Actions = Screen_3_Actions +(int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_3_Visits = Screen_3_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal == 4 )
			{
				Screen_4_People = Screen_4_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_4_Actions = Screen_4_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_4_Visits = Screen_4_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal == 5 )
			{
				Screen_5_People = Screen_5_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_5_Actions = Screen_5_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_5_Visits = Screen_5_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal >=6  && tempVal <= 10 )
			{
				Screen_6to10_People = Screen_6to10_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_6to10_Actions = Screen_6to10_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_6to10_Visits = Screen_6to10_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal >= 11 && tempVal <= 15 )
			{
				Screen_11to15_People = Screen_11to15_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_11to15_Actions = Screen_11to15_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_11to15_Visits = Screen_11to15_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal >= 16 && tempVal <= 20 )
			{
				Screen_16to20_People = Screen_16to20_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_16to20_Actions = Screen_16to20_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_16to20_Visits = Screen_16to20_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
			else if(tempVal > 20 )
			{
				Screen_20Plus_People = Screen_20Plus_People + (int)Float.parseFloat(reportData.getValueAt(i, "People").toString());
				Screen_20Plus_Actions = Screen_20Plus_Actions + (int)Float.parseFloat(reportData.getValueAt(i, "Actions").toString());
				Screen_20Plus_Visits = Screen_20Plus_Visits + (int)Float.parseFloat(reportData.getValueAt(i, "Sessions").toString());
			}
		}

		//data for graph
		chartData = new ReportBarChartInfo();
		chartData.people = Screen_1_People;
		chartData.visits = Screen_1_Visits;
		chartData.actions = Screen_1_Actions;
		chartData.axislabel = "1 screen";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_2_People;
		chartData.visits = Screen_2_Visits;
		chartData.actions = Screen_2_Actions;
		chartData.axislabel = "2 screens";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_3_People;
		chartData.visits = Screen_3_Visits;
		chartData.actions = Screen_3_Actions;
		chartData.axislabel = "3 screens";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_4_People;
		chartData.visits = Screen_4_Visits;
		chartData.actions = Screen_4_Actions;
		chartData.axislabel = "4 screens";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_5_People;
		chartData.visits = Screen_5_Visits;
		chartData.actions = Screen_5_Actions;
		chartData.axislabel = "5 screens";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_6to10_People;
		chartData.visits = Screen_6to10_Visits;
		chartData.actions = Screen_6to10_Actions;
		chartData.axislabel = "6-10 screens";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_11to15_People;
		chartData.visits = Screen_11to15_Visits;
		chartData.actions = Screen_11to15_Actions;
		chartData.axislabel = "11-15 screens";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_16to20_People;
		chartData.visits = Screen_16to20_Visits;
		chartData.actions = Screen_16to20_Actions;
		chartData.axislabel = "16-20 screens";
		listChartData.add(chartData);

		chartData = new ReportBarChartInfo();
		chartData.people = Screen_20Plus_People;
		chartData.visits = Screen_20Plus_Visits;
		chartData.actions = Screen_20Plus_Actions;
		chartData.axislabel = "20+ screens";
		listChartData.add(chartData);

		int totalVisits = Screen_1_Visits + Screen_2_Visits + Screen_3_Visits + Screen_4_Visits + Screen_5_Visits
				+ Screen_6to10_Visits + Screen_11to15_Visits + Screen_16to20_Visits + Screen_20Plus_Visits;
		int totalActions = Screen_1_Actions + Screen_2_Actions + Screen_3_Actions + Screen_4_Actions + Screen_5_Actions
				+ Screen_6to10_Actions + Screen_11to15_Actions + Screen_16to20_Actions + Screen_20Plus_Actions;
		//screenDepthTableModel.addRow(new Object[]{"", this.noOfPeople + " People",totalVisits + " Visits",totalActions + " Actions"});

		String _listChartDataJSON = gson.toJson(listChartData);
		returnJSON.addProperty("tableandchartdata", _listChartDataJSON);	

		String _noOfPeopleJSON = gson.toJson(noOfUsers);
		returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

		String _totalNoOfVisitsJSON = gson.toJson(totalVisits);
		returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

		String _totalNoOfActionsJSON = gson.toJson(totalActions);
		returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);	
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************
	//**************************************************************************************************************
	private JsonObject fetchReport_Frequency(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		int noOfUsers = _gcRPC.getNumberOfActiveUsers(duration, projectName, allProjects);	
		Dataset reportData = _gcRPC.getFrequencytReportInformation(projectName, allProjects, duration);

		int Session_1_People = 0 , Session_1_Visits = 0, Session_1_Actions = 0;
		int Session_2_People = 0 , Session_2_Visits = 0, Session_2_Actions = 0;
		int Session_3_People = 0 , Session_3_Visits = 0, Session_3_Actions = 0;
		int Session_4_People = 0 , Session_4_Visits = 0, Session_4_Actions = 0;
		int Session_5_People = 0 , Session_5_Visits = 0, Session_5_Actions = 0;
		int Session_6to10_People = 0 , Session_6to10_Visits = 0, Session_6to10_Actions = 0;
		int Session_11to15_People = 0 , Session_11to15_Visits = 0, Session_11to15_Actions = 0;
		int Session_16to20_People = 0 , Session_16to20_Visits = 0, Session_16to20_Actions = 0;
		int Session_20Plus_People = 0 , Session_20Plus_Visits = 0, Session_20Plus_Actions = 0;


		int size = 0;

		if(reportData != null)
		{
			size = reportData.getRowCount();

			int tempVal;

			for(int i = 0 ; i < size ; i ++)
			{
				tempVal = (int)Float.parseFloat(reportData.getValueAt(i, "Visits").toString());

				if(tempVal == 1 || tempVal == 0){
					Session_1_People = Session_1_People + 1;
					Session_1_Actions = Session_1_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_1_Visits = Session_1_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 2){
					Session_2_People = Session_2_People + 1;
					Session_2_Actions = Session_2_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_2_Visits = Session_2_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 3){
					Session_3_People = Session_3_People + 1;
					Session_3_Actions = Session_3_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_3_Visits = Session_3_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 4){
					Session_4_People = Session_4_People + 1;
					Session_4_Actions = Session_4_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_4_Visits = Session_4_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 5){
					Session_5_People = Session_5_People + 1;
					Session_5_Actions = Session_5_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_5_Visits = Session_5_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >5 && tempVal <= 10){
					Session_6to10_People = Session_6to10_People + 1;
					Session_6to10_Actions = Session_6to10_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_6to10_Visits = Session_6to10_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >10 && tempVal <= 15){
					Session_11to15_People = Session_11to15_People + 1;
					Session_11to15_Actions = Session_11to15_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_11to15_Visits = Session_11to15_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >15 && tempVal <= 20){
					Session_16to20_People = Session_16to20_People + 1;
					Session_16to20_Actions = Session_16to20_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_16to20_Visits = Session_16to20_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >20 ){
					Session_20Plus_People = Session_20Plus_People + 1;
					Session_20Plus_Actions = Session_20Plus_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Session_20Plus_Visits = Session_20Plus_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}

			}

			//Data for bar chart

			chartData = new ReportBarChartInfo();
			chartData.people = Session_1_People;
			chartData.visits = Session_1_Visits;
			chartData.actions = Session_1_Actions;
			chartData.axislabel = "1 session";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_2_People;
			chartData.visits = Session_2_Visits;
			chartData.actions = Session_2_Actions;
			chartData.axislabel = "2 sessions";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_3_People;
			chartData.visits = Session_3_Visits;
			chartData.actions = Session_3_Actions;
			chartData.axislabel = "3 sessions";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_4_People;
			chartData.visits = Session_4_Visits;
			chartData.actions = Session_4_Actions;
			chartData.axislabel = "4 sessions";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_5_People;
			chartData.visits = Session_5_Visits;
			chartData.actions = Session_5_Actions;
			chartData.axislabel = "5 sessions";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_6to10_People;
			chartData.visits = Session_6to10_Visits;
			chartData.actions = Session_6to10_Actions;
			chartData.axislabel = "6-10 sessions";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_11to15_People;
			chartData.visits = Session_11to15_Visits;
			chartData.actions = Session_11to15_Actions;
			chartData.axislabel = "11-15 sessions";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_16to20_People;
			chartData.visits = Session_16to20_Visits;
			chartData.actions = Session_16to20_Actions;
			chartData.axislabel = "16-20 sessions";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Session_20Plus_People;
			chartData.visits = Session_20Plus_Visits;
			chartData.actions = Session_20Plus_Actions;
			chartData.axislabel = "20+ sessions";
			listChartData.add(chartData);

			int totalVisits = Session_1_Visits + Session_2_Visits + Session_3_Visits + Session_4_Visits + Session_5_Visits
					+ Session_6to10_Visits + Session_11to15_Visits + Session_16to20_Visits + Session_20Plus_Visits;
			int totalActions = Session_1_Actions + Session_2_Actions + Session_3_Actions + Session_4_Actions + Session_5_Actions
					+ Session_6to10_Actions + Session_11to15_Actions + Session_16to20_Actions + Session_20Plus_Actions;
			//screenDepthTableModel.addRow(new Object[]{"", this.noOfPeople + " People",totalVisits + " Visits",totalActions + " Actions"});

			String _listChartDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("tableandchartdata", _listChartDataJSON);	

			String _noOfPeopleJSON = gson.toJson(noOfUsers);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(totalVisits);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(totalActions);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);
		}	
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************
	//**************************************************************************************************************
	private JsonObject fetchReport_Recency(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	

		List<ReportBarChartInfo> listChartData = new ArrayList<ReportBarChartInfo>();
		ReportBarChartInfo chartData = new ReportBarChartInfo();

		Dataset reportData = _gcRPC.getRecencytReportInformation(projectName, allProjects, duration);

		int Recency_1_People = 0 , Recency_1_Visits = 0, Recency_1_Actions = 0;
		int Recency_2_People = 0 , Recency_2_Visits = 0, Recency_2_Actions = 0;
		int Recency_3_People = 0 , Recency_3_Visits = 0, Recency_3_Actions = 0;
		int Recency_4_People = 0 , Recency_4_Visits = 0, Recency_4_Actions = 0;
		int Recency_5_People = 0 , Recency_5_Visits = 0, Recency_5_Actions = 0;
		int Recency_6to10_People = 0 , Recency_6to10_Visits = 0, Recency_6to10_Actions = 0;
		int Recency_11to15_People = 0 , Recency_11to15_Visits = 0, Recency_11to15_Actions = 0;
		int Recency_16to20_People = 0 , Recency_16to20_Visits = 0, Recency_16to20_Actions = 0;
		int Recency_20Plus_People = 0 , Recency_20Plus_Visits = 0, Recency_20Plus_Actions = 0;


		int size = 0;

		if(reportData != null)
		{
			size = reportData.getRowCount();

			int tempVal;

			for(int i = 0 ; i < size ; i ++){
				tempVal = (int)Float.parseFloat(reportData.getValueAt(i, "Days").toString());

				if(tempVal == 1 || tempVal == 0){
					Recency_1_People = Recency_1_People + 1;
					Recency_1_Actions = Recency_1_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_1_Visits = Recency_1_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 2){
					Recency_2_People = Recency_2_People + 1;
					Recency_2_Actions = Recency_2_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_2_Visits = Recency_2_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 3){
					Recency_3_People = Recency_3_People + 1;
					Recency_3_Actions = Recency_3_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_3_Visits = Recency_3_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 4){
					Recency_4_People = Recency_4_People + 1;
					Recency_4_Actions = Recency_4_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_4_Visits = Recency_4_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal == 5){
					Recency_5_People = Recency_5_People + 1;
					Recency_5_Actions = Recency_5_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_5_Visits = Recency_5_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >5 && tempVal <= 10){
					Recency_6to10_People = Recency_6to10_People + 1;
					Recency_6to10_Actions = Recency_6to10_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_6to10_Visits = Recency_6to10_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >10 && tempVal <= 15){
					Recency_11to15_People = Recency_11to15_People + 1;
					Recency_11to15_Actions = Recency_11to15_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_11to15_Visits = Recency_11to15_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >15 && tempVal <= 20){
					Recency_16to20_People = Recency_16to20_People + 1;
					Recency_16to20_Actions = Recency_16to20_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_16to20_Visits = Recency_16to20_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}
				if(tempVal >20 ){
					Recency_20Plus_People = Recency_20Plus_People + 1;
					Recency_20Plus_Actions = Recency_20Plus_Actions + (int)Float.parseFloat(reportData.getValueAt(i , "Actions").toString())
					+ (int)Float.parseFloat(reportData.getValueAt(i , "noOfScreens").toString());
					Recency_20Plus_Visits = Recency_20Plus_Visits+ (int)Float.parseFloat(reportData.getValueAt(i , "Visits").toString());
				}

			}

			//Data for bar chart

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_1_People;
			chartData.visits = Recency_1_Visits;
			chartData.actions = Recency_1_Actions;
			chartData.axislabel = "1 day";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_2_People;
			chartData.visits = Recency_2_Visits;
			chartData.actions = Recency_2_Actions;
			chartData.axislabel = "2 days";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_3_People;
			chartData.visits = Recency_3_Visits;
			chartData.actions = Recency_3_Actions;
			chartData.axislabel = "3 days";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_4_People;
			chartData.visits = Recency_4_Visits;
			chartData.actions = Recency_4_Actions;
			chartData.axislabel = "4 days";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_5_People;
			chartData.visits = Recency_5_Visits;
			chartData.actions = Recency_5_Actions;
			chartData.axislabel = "5 days";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_6to10_People;
			chartData.visits = Recency_6to10_Visits;
			chartData.actions = Recency_6to10_Actions;
			chartData.axislabel = "6-10 days";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_11to15_People;
			chartData.visits = Recency_11to15_Visits;
			chartData.actions = Recency_11to15_Actions;
			chartData.axislabel = "11-15 days";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_16to20_People;
			chartData.visits = Recency_16to20_Visits;
			chartData.actions = Recency_16to20_Actions;
			chartData.axislabel = "16-20 days";
			listChartData.add(chartData);

			chartData = new ReportBarChartInfo();
			chartData.people = Recency_20Plus_People;
			chartData.visits = Recency_20Plus_Visits;
			chartData.actions = Recency_20Plus_Actions;
			chartData.axislabel = "20+ days";
			listChartData.add(chartData);

			int totalPeople = Recency_1_People + Recency_2_People + Recency_3_People + Recency_4_People + Recency_5_People
					+ Recency_6to10_People + Recency_11to15_People + Recency_16to20_People + Recency_20Plus_People;

			int totalVisits = Recency_1_Visits + Recency_2_Visits + Recency_3_Visits + Recency_4_Visits + Recency_5_Visits
					+ Recency_6to10_Visits + Recency_11to15_Visits + Recency_16to20_Visits + Recency_20Plus_Visits;
			int totalActions = Recency_1_Actions + Recency_2_Actions + Recency_3_Actions + Recency_4_Actions + Recency_5_Actions
					+ Recency_6to10_Actions + Recency_11to15_Actions + Recency_16to20_Actions + Recency_20Plus_Actions;
			//screenDepthTableModel.addRow(new Object[]{"", this.noOfPeople + " People",totalVisits + " Visits",totalActions + " Actions"});

			String _listChartDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("tableandchartdata", _listChartDataJSON);	

			String _noOfPeopleJSON = gson.toJson(totalPeople);
			returnJSON.addProperty("noofpeople", _noOfPeopleJSON);

			String _totalNoOfVisitsJSON = gson.toJson(totalVisits);
			returnJSON.addProperty("totalnoofvisits", _totalNoOfVisitsJSON);

			String _totalNoOfActionsJSON = gson.toJson(totalActions);
			returnJSON.addProperty("totalnoofactions", _totalNoOfActionsJSON);
		}	
		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************
	//**************************************************************************************************************
	private JsonObject fetchReport_ActiveUsers(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname"), dataSource ="";
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	
		List<ReportActiveUsersInfo> listTableData = new ArrayList<ReportActiveUsersInfo>();
		List<ReportActiveUsersInfo> listChartData = new ArrayList<ReportActiveUsersInfo>();
		ReportActiveUsersInfo chartData = new ReportActiveUsersInfo();

		int oneDayActiveUsers = 0;
		int sevenDayActiveUsers = 0;
		int fourteenDayActiveUsers = 0;

		ActiveUsersInfo aInfo = _gcRPC.getActiveUsersCounts(projectName, allProjects, duration);

		if(aInfo != null)
		{
			oneDayActiveUsers = aInfo.getOneDayActiveUsers() ;
			sevenDayActiveUsers = aInfo.getSevenDayActiveUsers() ;
			fourteenDayActiveUsers = aInfo.getFourteenDayActiveUsers();
		}	

		Dataset reportData = _gcRPC.getActiveUserDataReportGraph(dataSource, duration, projectName, allProjects);

		//SimpleDateFormat sdf = new SimpleDateFormat("EE, MMM dd, yyyy");
		//SimpleDateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
		//SimpleDateFormat dfGraph = new SimpleDateFormat("EE MMM dd");
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy",Locale.ENGLISH);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S",Locale.ENGLISH);
		SimpleDateFormat dfGraph = new SimpleDateFormat("MMM dd",Locale.ENGLISH);
		
		if(reportData !=null)
		{
			int noOfRecords = reportData.getRowCount();

			String category = "";
			String categoryGraph = "";

			Date xDate = null;
			float totaloneDayActive = 0;
			float totalfourteenDayActive = 0f;
			float totalsevenDayActive = 0;
			int startVal = 0;
			if(duration == Constants.LAST_365_DAYS)
			{
				Integer curMonth = Calendar.getInstance().get(Calendar.MONTH) ;

				System.out.println("curMonth : " + curMonth);
				if(curMonth == 11)
				{
					curMonth = 1;
				}
				else 
				{
					curMonth = curMonth + 2;
				}

				startVal = Constants.binarySearchOnDataset(4,  curMonth, reportData);
				System.out.println("startVal after binarySearch: " + startVal);
				if(startVal < 0)
				{
					startVal = 0;
				}


				for(int i=startVal -1 ; i >= 0 ; i--)
				{
					category = "";
					float oneDayActive = 0;
					float fourteenDayActive = 0f;
					float sevenDayActive = 0;



					if(reportData.getValueAt(i, 0) != null)
						category = reportData.getValueAt(i, 0).toString();

					if(reportData.getValueAt(i, 1) != null)
					{
						oneDayActive = Float.parseFloat(reportData.getValueAt(i, 1).toString());
					}
					if(reportData.getValueAt(i, 2) != null)
					{
						sevenDayActive = Float.parseFloat(reportData.getValueAt(i, 2).toString());
					}
					if(reportData.getValueAt(i, 3) != null)
					{
						fourteenDayActive = Float.parseFloat(reportData.getValueAt(i, 3).toString());
					}

					totaloneDayActive = totaloneDayActive + oneDayActive;
					totalfourteenDayActive = totalfourteenDayActive + fourteenDayActive ;
					totalsevenDayActive = totalsevenDayActive +sevenDayActive;

					chartData = new ReportActiveUsersInfo();
					chartData.axislabel = category;
					chartData.oneDayActive = (int)oneDayActive;
					chartData.sevenDayActive = (int)sevenDayActive;
					chartData.fourteenDayActive = (int)fourteenDayActive;
					listTableData.add(chartData);
					//platformsData.addRow(new Object[]{category,(int)oneDayActive,(int)sevenDayActive, (int)fourteenDayActive });

				}

				for(int i=noOfRecords - 1 ; i >= startVal ; i--)
				{

					category = "";
					float oneDayActive = 0;
					float fourteenDayActive = 0f;
					float sevenDayActive = 0;

					if(reportData.getValueAt(i, 0) != null)
						category = reportData.getValueAt(i, 0).toString();

					if(reportData.getValueAt(i, 1) != null)
					{
						oneDayActive = Float.parseFloat(reportData.getValueAt(i, 1).toString());
					}
					if(reportData.getValueAt(i, 2) != null)
					{
						sevenDayActive = Float.parseFloat(reportData.getValueAt(i, 2).toString());
					}
					if(reportData.getValueAt(i, 3) != null)
					{
						fourteenDayActive = Float.parseFloat(reportData.getValueAt(i, 3).toString());
					}

					totaloneDayActive = totaloneDayActive + oneDayActive;
					totalfourteenDayActive = totalfourteenDayActive + fourteenDayActive ;
					totalsevenDayActive = totalsevenDayActive +sevenDayActive;

					chartData = new ReportActiveUsersInfo();
					chartData.axislabel = category;
					chartData.oneDayActive = (int)oneDayActive;
					chartData.sevenDayActive = (int)sevenDayActive;
					chartData.fourteenDayActive = (int)fourteenDayActive;
					listTableData.add(chartData);
					//platformsData.addRow(new Object[]{category,(int)oneDayActive,(int)sevenDayActive, (int)fourteenDayActive });
				}

			}

			else
			{

				for (int i = noOfRecords -1  ; i>= 0 ; i-- )
				{
					category = "";
					float oneDayActive = 0;
					float fourteenDayActive = 0f;
					float sevenDayActive = 0;

					if(duration != Constants.LAST_365_DAYS && duration != Constants.TODAY && duration != Constants.YESTERDAY && duration != Constants.LAST_YEAR && duration != Constants.THIS_YEAR){
						if(reportData.getValueAt(i, 0) != null)
						{
							String dateVal = reportData.getValueAt(i, 0).toString();
							try {
								xDate = df.parse(dateVal);
								category = sdf.format(xDate);
							} catch (ParseException e) {
								log.error(e);
							}

						}	
					}
					else{
						if(reportData.getValueAt(i, 0) != null)
							category = reportData.getValueAt(i, 0).toString();
					}
					if(reportData.getValueAt(i, 1) != null)
					{
						oneDayActive = Float.parseFloat(reportData.getValueAt(i, 1).toString());
					}
					if(reportData.getValueAt(i, 2) != null)
					{
						sevenDayActive = Float.parseFloat(reportData.getValueAt(i, 2).toString());
					}
					if(reportData.getValueAt(i, 3) != null)
					{
						fourteenDayActive = Float.parseFloat(reportData.getValueAt(i, 3).toString());
					}

					totaloneDayActive = totaloneDayActive + oneDayActive;
					totalfourteenDayActive = totalfourteenDayActive + fourteenDayActive ;
					totalsevenDayActive = totalsevenDayActive +sevenDayActive;

					chartData = new ReportActiveUsersInfo();
					chartData.axislabel = category;
					chartData.oneDayActive = (int)oneDayActive;
					chartData.sevenDayActive = (int)sevenDayActive;
					chartData.fourteenDayActive = (int)fourteenDayActive;
					listTableData.add(chartData);
					//platformsData.addRow(new Object[]{category,(int)oneDayActive,(int)sevenDayActive, (int)fourteenDayActive });
				}
			}
			//platformsData.addRow(new Object[]{noOfRecords + " Result(s)",oneDayActiveUsers + " People",sevenDayActiveUsers + " People", fourteenDayActiveUsers + " People"});

			//create line chart data in ascending order
			if(duration == Constants.LAST_365_DAYS)
			{
				for(int i= startVal; i<noOfRecords; i++)
				{
					categoryGraph = "";
					float oneDayActive = 0;
					float fourteenDayActive = 0f;
					float sevenDayActive = 0;


					if(reportData.getValueAt(i, 0) != null)
						categoryGraph = reportData.getValueAt(i, 0).toString();

					if(reportData.getValueAt(i, 1) != null)
					{
						oneDayActive = Float.parseFloat(reportData.getValueAt(i, 1).toString());
					}
					if(reportData.getValueAt(i, 2) != null)
					{
						sevenDayActive = Float.parseFloat(reportData.getValueAt(i, 2).toString());
					}
					if(reportData.getValueAt(i, 3) != null)
					{
						fourteenDayActive = Float.parseFloat(reportData.getValueAt(i, 3).toString());
					}

					chartData = new ReportActiveUsersInfo();
					chartData.axislabel = categoryGraph;
					chartData.oneDayActive = (int)oneDayActive;
					chartData.sevenDayActive = (int)sevenDayActive;
					chartData.fourteenDayActive = (int)fourteenDayActive;
					listChartData.add(chartData);	

				}

				for(int i =0; i<startVal; i++)
				{

					categoryGraph = "";
					float oneDayActive = 0;
					float fourteenDayActive = 0f;
					float sevenDayActive = 0;


					if(reportData.getValueAt(i, 0) != null)
						categoryGraph = reportData.getValueAt(i, 0).toString();

					if(reportData.getValueAt(i, 1) != null)
					{
						oneDayActive = Float.parseFloat(reportData.getValueAt(i, 1).toString());
					}
					if(reportData.getValueAt(i, 2) != null)
					{
						sevenDayActive = Float.parseFloat(reportData.getValueAt(i, 2).toString());
					}
					if(reportData.getValueAt(i, 3) != null)
					{
						fourteenDayActive = Float.parseFloat(reportData.getValueAt(i, 3).toString());
					}


					chartData = new ReportActiveUsersInfo();
					chartData.axislabel = categoryGraph;
					chartData.oneDayActive = (int)oneDayActive;
					chartData.sevenDayActive = (int)sevenDayActive;
					chartData.fourteenDayActive = (int)fourteenDayActive;
					listChartData.add(chartData);
				}
			}
			else
			{
				for (int i = 0  ; i< noOfRecords ; i++ ){
					categoryGraph = "";
					float oneDayActive = 0;
					float fourteenDayActive = 0f;
					float sevenDayActive = 0;

					if(duration != Constants.LAST_365_DAYS && duration != Constants.TODAY && duration != Constants.YESTERDAY && duration != Constants.LAST_YEAR && duration != Constants.THIS_YEAR)
					{
						if(reportData.getValueAt(i, 0) != null)
						{
							String dateVal = reportData.getValueAt(i, 0).toString();
							try {
								xDate = df.parse(dateVal);
								categoryGraph = dfGraph.format(xDate);
							} catch (ParseException e) {
								log.error(e);
							}
						}

					}
					else{
						if(reportData.getValueAt(i, 0) != null)
							categoryGraph = reportData.getValueAt(i, 0).toString();
					}
					if(reportData.getValueAt(i, 1) != null)
					{
						oneDayActive = Float.parseFloat(reportData.getValueAt(i, 1).toString());
					}
					if(reportData.getValueAt(i, 2) != null)
					{
						sevenDayActive = Float.parseFloat(reportData.getValueAt(i, 2).toString());
					}
					if(reportData.getValueAt(i, 3) != null)
					{
						fourteenDayActive = Float.parseFloat(reportData.getValueAt(i, 3).toString());
					}


					chartData = new ReportActiveUsersInfo();
					chartData.axislabel = categoryGraph;
					chartData.oneDayActive = (int)oneDayActive;
					chartData.sevenDayActive = (int)sevenDayActive;
					chartData.fourteenDayActive = (int)fourteenDayActive;
					listChartData.add(chartData);
				}

			}
			String _listTableDataJSON = gson.toJson(listTableData);
			returnJSON.addProperty("tabledata", _listTableDataJSON);

			String _listChartDataJSON = gson.toJson(listChartData);
			returnJSON.addProperty("chartdata", _listChartDataJSON);	

			String _noOfRecordsJSON = gson.toJson(noOfRecords);
			returnJSON.addProperty("noofrecords", _noOfRecordsJSON);

			String _oneDayActiveUsersJSON = gson.toJson(oneDayActiveUsers);
			returnJSON.addProperty("onedayactiveusers", _oneDayActiveUsersJSON);

			String _sevenDayActiveUsersJSON = gson.toJson(sevenDayActiveUsers);
			returnJSON.addProperty("onedayactiveusers", _sevenDayActiveUsersJSON);

			String _fourteenDayActiveUsersJSON = gson.toJson(fourteenDayActiveUsers);
			returnJSON.addProperty("onedayactiveusers", _fourteenDayActiveUsersJSON);


		}

		//**********************************************************************************************************
		return returnJSON;
	}	
	//*****************************************************************************************************************
	//**************************************************************************************************************
	private JsonObject fetchReport_AlarmSummary(RequestContext req, HttpServletResponse res) 
	{
		int duration = Integer.parseInt(req.getParameter("duration"));
		String projectName = req.getParameter("projectname");
		boolean allProjects = Boolean.parseBoolean(req.getParameter("allprojects"));

		Gson gson = new GsonBuilder().create();
		JsonObject returnJSON = new JsonObject();

		//**********************************************************************************************************	

		List<ReportAlarmSummary> listSummaryData = new ArrayList<ReportAlarmSummary>();
		ReportAlarmSummary summaryData = new ReportAlarmSummary();

		ReportAlarmsTable summaryTable = new ReportAlarmsTable();

		Dataset _alarmsClearTime = _gcRPC.getAlarmsClearTime(duration, projectName, allProjects);

		int alarmsClearDSSize;
		String medAlarmsClr = "00:00:00", highAlarmsclr = "00:00:00", criticalAlarmsClr = "00:00:00", lowAlarmsClr = "00:00:00";
		String alarmPriority;
		//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		int r=0;
		if(_alarmsClearTime != null)
		{
			alarmsClearDSSize = _alarmsClearTime.getRowCount();
			for(r=0; r<alarmsClearDSSize; r++)
			{
				if(_alarmsClearTime.getValueAt(r, 0) != null)
				{
					alarmPriority = _alarmsClearTime.getValueAt(r, 0).toString();
					if(alarmPriority.compareToIgnoreCase("Medium") == 0) //medium
					{
						if(_alarmsClearTime.getValueAt(r, 1) != null)
						{
							medAlarmsClr = _alarmsClearTime.getValueAt(r, 1).toString();
						}
					}
					else if(alarmPriority.compareToIgnoreCase("High") == 0) //high
					{
						if(_alarmsClearTime.getValueAt(r, 1) != null)
						{
							highAlarmsclr = _alarmsClearTime.getValueAt(r, 1).toString();
						}
					}
					else if(alarmPriority.compareToIgnoreCase("Critical") == 0) //critical
					{
						if(_alarmsClearTime.getValueAt(r, 1) != null)
						{
							criticalAlarmsClr = _alarmsClearTime.getValueAt(r, 1).toString();
						}
					}
					else if(alarmPriority.compareToIgnoreCase("Low") == 0) //critical
					{
						if(_alarmsClearTime.getValueAt(r, 1) != null)
						{
							lowAlarmsClr = _alarmsClearTime.getValueAt(r, 1).toString();
						}
					}

				}
			}
		}
		Dataset _alarmsAckTime = _gcRPC.getAlarmsAckTime(duration, projectName, allProjects);

		int alarmsAckDSSize;
		String medAlarmsAck = "00:00:00", highAlarmsAck = "00:00:00", criticalAlarmsAck = "00:00:00", lowAlarmsAck="00:00:00";
		if(_alarmsAckTime != null)
		{
			alarmsAckDSSize = _alarmsAckTime.getRowCount();
			for(r=0; r<alarmsAckDSSize; r++)
			{
				if(_alarmsAckTime.getValueAt(r, 0) != null)
				{
					alarmPriority = _alarmsAckTime.getValueAt(r, 0).toString();
					if(alarmPriority.compareToIgnoreCase("Medium") == 0) //medium
					{
						if(_alarmsAckTime.getValueAt(r, 1) != null)
						{
							System.out.println("_alarmsAckTime : med " + _alarmsAckTime.getValueAt(r, 1));
							medAlarmsAck = _alarmsAckTime.getValueAt(r, 1).toString();
						}

					}
					else if(alarmPriority.compareToIgnoreCase("High") == 0) //high
					{
						if(_alarmsAckTime.getValueAt(r, 1) != null)
						{
							System.out.println("_alarmsAckTime : high " + _alarmsAckTime.getValueAt(r, 1));
							highAlarmsAck = _alarmsAckTime.getValueAt(r, 1).toString();
						}
					}
					else if(alarmPriority.compareToIgnoreCase("Critical") == 0) //critical
					{
						if(_alarmsAckTime.getValueAt(r, 1) != null)
						{
							System.out.println("_alarmsAckTime : critical " + _alarmsAckTime.getValueAt(r, 1));
							criticalAlarmsAck = _alarmsAckTime.getValueAt(r, 1).toString();
						}
					}
					else if(alarmPriority.compareToIgnoreCase("Low") == 0) //critical
					{
						if(_alarmsAckTime.getValueAt(r, 1) != null)
						{
							System.out.println("_alarmsAckTime : low " + _alarmsAckTime.getValueAt(r, 1));
							lowAlarmsAck = _alarmsAckTime.getValueAt(r, 1).toString();
						}
					}

				}
			}
		}
		Dataset reportData = _gcRPC.getAlarmsSummaryReport(projectName, allProjects, duration);
		int size = 0;

		if(reportData != null)
		{
			size = reportData.getRowCount();

			String avgTimeAck = "00:00:00";
			String avgTimeClr = "00:00:00";
			float avgTimeAckSeconds = 0;
			float avgTimeClrSeconds = 0;

			int lowSum = 0 ;
			int mediumSum = 0 ;
			int highSum = 0 ;
			int criticalSum = 0 ;
			int lowRechords = 0 ;
			int mediumRechords = 0 ;
			int highRechords = 0 ;
			int criticalRechords = 0 ;
			float avgClrSumLow = 0,avgAckSumLow =0;
			float avgClrSumMedium = 0,avgAckSumMedium =0;
			float avgClrSumHigh = 0,avgAckSumHigh =0;
			float avgClrSumCritical = 0,avgAckSumCitical =0;


			int sumAlarmCount = 0;
			for(int i = 0 ; i < size ; i++)
			{

				String alarmName = reportData.getValueAt(i, "alarm_name").toString(); 
				String priority = reportData.getValueAt(i, "alarm_priority").toString();
				sumAlarmCount = 0;
				sumAlarmCount = (int)Float.parseFloat(reportData.getValueAt(i , "Quantity").toString());
				avgTimeAck = "00:00:00";
				avgTimeClr = "00:00:00";
				if(reportData.getValueAt(i, "TimeToAck") != null)
				{
					avgTimeAck = reportData.getValueAt(i, "TimeToAck").toString();
					//avgTimeAck = avgTimeAck.substring(11, 16);
					//avgTimeAck = avgTimeAck.substring(11,19);
				}
				if(reportData.getValueAt(i, "TimetToClr") != null)
				{
					avgTimeClr = reportData.getValueAt(i, "TimetToClr").toString();
					//				avgTimeClr = avgTimeClr.substring(11, 16);
					//avgTimeClr = avgTimeClr.substring(11,19);
				}
				if(reportData.getValueAt(i, "timeToAckSeconds") != null)
				{
					avgTimeAckSeconds = Float.parseFloat(reportData.getValueAt(i, "timeToAckSeconds").toString());
				}
				if(reportData.getValueAt(i, "timeToClrSeconds") != null)
				{
					avgTimeClrSeconds = Float.parseFloat(reportData.getValueAt(i, "timeToClrSeconds").toString());
				}
				if(priority.compareToIgnoreCase("Low") == 0){
					lowSum = lowSum + sumAlarmCount;
					lowRechords++;
					avgClrSumLow = avgClrSumLow + avgTimeClrSeconds;
					avgAckSumLow = avgAckSumLow + avgTimeAckSeconds;
				}
				if(priority.compareToIgnoreCase("Medium") == 0){
					mediumSum = mediumSum + sumAlarmCount;
					mediumRechords++;
					avgClrSumMedium = avgClrSumMedium + avgTimeClrSeconds;
					avgAckSumMedium = avgAckSumMedium + avgTimeAckSeconds;
				}
				if(priority.compareToIgnoreCase("High") == 0){
					highSum = highSum + sumAlarmCount;
					highRechords++;
					avgClrSumHigh = avgClrSumHigh + avgTimeClrSeconds;
					avgAckSumHigh = avgAckSumHigh + avgTimeAckSeconds;
				}
				if(priority.compareToIgnoreCase("Critical") == 0){
					criticalSum = criticalSum + sumAlarmCount;
					criticalRechords++;
					avgClrSumCritical = avgClrSumCritical + avgTimeClrSeconds;
					avgAckSumCitical = avgAckSumCitical + avgTimeAckSeconds;
				}

				summaryData = new ReportAlarmSummary();
				summaryData.alarmName = alarmName;
				summaryData.priority = priority;
				summaryData.sumAlarmCount = sumAlarmCount;
				summaryData.avgTimeAck = avgTimeAck;
				summaryData.avgTimeClr = avgTimeClr;
				listSummaryData.add(summaryData);
			}

			summaryTable = new ReportAlarmsTable();
			if(lowRechords > 0)
			{
				summaryTable.lowSum = lowSum;
				summaryTable.lowAlarmsAck = lowAlarmsAck;
				summaryTable.lowAlarmsClr = lowAlarmsClr;

				//totalAvgData.addRow(new Object[]{"Total Low Priority Alarms", "Low",lowSum,lowAlarmsAck,lowAlarmsClr});
			}
			else
			{
				summaryTable.lowSum = lowSum;
				summaryTable.lowAlarmsAck = "00:00:00";
				summaryTable.lowAlarmsClr = "00:00:00";
				//totalAvgData.addRow(new Object[]{"Total Low Priority Alarms", "Low",lowSum,"00:00:00","00:00:00"});
			}

			if(mediumRechords > 0)
			{
				summaryTable.mediumSum = mediumSum;
				summaryTable.medAlarmsAck = medAlarmsAck;
				summaryTable.medAlarmsClr = medAlarmsClr;
				//totalAvgData.addRow(new Object[]{"Total Medium Priority Alarms", "Medium",mediumSum,medAlarmsAck,medAlarmsClr});
			}
			else
			{
				summaryTable.mediumSum = mediumSum;
				summaryTable.medAlarmsAck = "00:00:00";
				summaryTable.medAlarmsClr = "00:00:00";
				//totalAvgData.addRow(new Object[]{"Total Medium Priority Alarms", "Medium",mediumSum,"00:00:00","00:00:00"});
			}
			if(highRechords > 0)
			{
				summaryTable.highSum = highSum;
				summaryTable.highAlarmsAck = highAlarmsAck;
				summaryTable.highAlarmsclr = highAlarmsclr;
				//totalAvgData.addRow(new Object[]{"Total High Priority Alarms", "High",highSum,highAlarmsAck,highAlarmsclr});
			}
			else
			{
				summaryTable.highSum = highSum;
				summaryTable.highAlarmsAck = "00:00:00";
				summaryTable.highAlarmsclr = "00:00:00";
				//totalAvgData.addRow(new Object[]{"Total High Priority Alarms", "High",highSum,"00:00:00","00:00:00"});
			}
			if(criticalRechords > 0)
			{
				summaryTable.criticalSum = criticalSum;
				summaryTable.criticalAlarmsAck = criticalAlarmsAck;
				summaryTable.criticalAlarmsClr = criticalAlarmsClr;
				//totalAvgData.addRow(new Object[]{"Total Critical Priority Alarms", "Critical",criticalSum,criticalAlarmsAck,criticalAlarmsClr});
			}
			else
			{
				summaryTable.criticalSum = criticalSum;
				summaryTable.criticalAlarmsAck = "00:00:00";
				summaryTable.criticalAlarmsClr = "00:00:00";
				//totalAvgData.addRow(new Object[]{"Total Critical Priority Alarms", "Critical",criticalSum,"00:00:00","00:00:00"});
			}

			String _listSummaryDataJSON = gson.toJson(listSummaryData);
			returnJSON.addProperty("summarydata", _listSummaryDataJSON);

			String _summaryTableJSON = gson.toJson(summaryTable);
			returnJSON.addProperty("tabledata", _summaryTableJSON);

		}


		ReportAlarmsRingChart pieChartEntry = new ReportAlarmsRingChart();
		List<ReportAlarmsRingChart> pieChartData = new ArrayList<ReportAlarmsRingChart>();
		int datasetSize = 0;
		List<String> tagArray;
		double totalForPercentage;
		Dataset alarmSummaryData = _gcRPC.alarmSummaryReportRingChart(projectName, allProjects, duration);
		if(alarmSummaryData != null)
		{
			datasetSize = alarmSummaryData.getRowCount();
			tagArray = new ArrayList<String>();
			totalForPercentage = 0;
			for(int j = 0 ; j < datasetSize ; j++)
			{
				String key = alarmSummaryData.getValueAt(j, 0).toString();
				if(alarmSummaryData.getValueAt(j, 1) != null)
				{
					Double addDouble = Double.parseDouble(alarmSummaryData.getValueAt(j, 1).toString());
					totalForPercentage = addDouble + totalForPercentage;
					//key = key + " - " + 	 NumberFormat.getInstance().format(addDouble.intValue()) ;
					System.out.println("Key Value is : " + key);
					System.out.println("Array j value is  : " + j);
					tagArray.add(key);
				}

			}

			for(int i = 0 ; i < datasetSize ; i++)
			{
				if(alarmSummaryData.getValueAt(i, 1) != null)
				{
					Double value = Double.parseDouble(alarmSummaryData.getValueAt(i, 1).toString());

					int percentage = (int) ((value.intValue() * 100 ) / totalForPercentage);

					String valueTag = tagArray.get(i) + " (" + percentage + "%)";

					//pieDataset.insertValue(i,valueTag , value);
					pieChartEntry = new ReportAlarmsRingChart();
					pieChartEntry.count = i;
					pieChartEntry.valueTag = valueTag;
					pieChartEntry.value = value;
					pieChartData.add(pieChartEntry);

				}
			}

			String _freqRingDataJSON = gson.toJson(pieChartData);
			returnJSON.addProperty("frequency_ring_data", _freqRingDataJSON);
		}	


		alarmSummaryData = _gcRPC.getTop10AlarmsByDuration(projectName, allProjects, duration);
		pieChartData = new ArrayList<ReportAlarmsRingChart>();
		if(alarmSummaryData != null)
		{
			datasetSize = alarmSummaryData.getRowCount();
			tagArray = new ArrayList<String>();
			totalForPercentage = 0;


			for(int j = 0 ; j < datasetSize ; j++)
			{
				String key = alarmSummaryData.getValueAt(j, 0).toString();
				if(alarmSummaryData.getValueAt(j, 1) != null)
				{
					Double addDouble = Double.parseDouble(alarmSummaryData.getValueAt(j, 1).toString());
					totalForPercentage = addDouble + totalForPercentage;

					//					key = key + " - " + 	NumberFormat.getInstance().format(addDouble.intValue()) ;
					System.out.println("Key Value is : " + key);
					System.out.println("Array j value is  : " + j);
					tagArray.add(key);
				}

			}

			System.out.println("total no of rows " + datasetSize);
			for(int i = 0 ; i < datasetSize ; i++){
				if(alarmSummaryData.getValueAt(i, 1) != null)
				{
					Double value = Double.parseDouble(alarmSummaryData.getValueAt(i, 1).toString());

					int percentage = (int) ((value.intValue() * 100 ) / totalForPercentage);

					String valueTag = tagArray.get(i) + " (" + percentage + "%)";

					//pieDataset.insertValue(i,valueTag , value);
					pieChartEntry = new ReportAlarmsRingChart();
					pieChartEntry.count = i;
					pieChartEntry.valueTag = valueTag;
					pieChartEntry.value = value;
					pieChartData.add(pieChartEntry);

				}
			}


			String _durationRingDataJSON = gson.toJson(pieChartData);
			returnJSON.addProperty("duration_ring_data", _durationRingDataJSON);

		}


		String label = "";
		if(duration == Constants.TODAY || duration == Constants.YESTERDAY)
		{
			label= "Alarms by Hour of Day";
		}
		else if(duration == Constants.THIS_WEEK || duration == Constants.LAST_WEEK || duration == Constants.LAST_SEVEN_DAYS)
		{
			label= "Alarms by Day of Week";
		}
		else if(duration == Constants.THIS_MONTH || duration == Constants.LAST_NINTY_DAYS || duration == Constants.LAST_THIRTY_DAYS || duration == Constants.LAST_MONTH)
		{
			label= "Alarms by Day";
		}
		else if(duration == Constants.LAST_365_DAYS || duration == Constants.LAST_YEAR || duration == Constants.THIS_YEAR)
		{
			label= "Alarms by Month";
		}
		else
		{
			label= "Alarms by Day";
		}

		String _chartlabelJSON = gson.toJson(label);
		returnJSON.addProperty("barchart_title", _chartlabelJSON);

		Dataset alarmsByHourData = _gcRPC.getAlarmCountsPerDuration(projectName, allProjects, duration);
		LineChartInfo datapoint = new LineChartInfo();
		List<LineChartInfo> chartdata = new ArrayList<LineChartInfo>();
		if(alarmsByHourData != null)
		{
			int noOfRows = alarmsByHourData.getRowCount();
			int i;
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"); //2019-11-28 00:00:00.0
			SimpleDateFormat sdf = new SimpleDateFormat("M/d");
			String series1 = "Count";

			int val1 = 0;
			for(i=0; i<noOfRows; i++)
			{
				String dateVal = alarmsByHourData.getValueAt(i, 0).toString();
				if(duration != Constants.TODAY && duration != Constants.YESTERDAY
						&& duration != Constants.LAST_365_DAYS && duration != Constants.THIS_YEAR
						&& duration != Constants.LAST_YEAR && duration != Constants.THIS_WEEK && duration != Constants.LAST_WEEK
						&& duration != Constants.LAST_SEVEN_DAYS)
				{
					try {
						dateVal = sdf.format(df.parse(alarmsByHourData.getValueAt(i, 0).toString()));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						log.error("error parsing alarm date : " + e);
					}

				}
				else
				{
					dateVal = alarmsByHourData.getValueAt(i, 0).toString();
				}

				if(alarmsByHourData.getValueAt(i, 1) != null)
				{
					val1 = (int)Float.parseFloat(alarmsByHourData.getValueAt(i, 1).toString());
				}
				datapoint = new LineChartInfo();
				datapoint.count = val1;
				datapoint.seriesName = series1;
				datapoint.day = dateVal;
				chartdata.add(datapoint);
			}


			String _chartdataJSON = gson.toJson(chartdata);
			returnJSON.addProperty("barchart_data", _chartdataJSON);
		}


		//**********************************************************************************************************

		return returnJSON;
	}
}
