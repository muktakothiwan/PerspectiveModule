package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.Date;

/**
 * A Class that represents the Clients table in the DB. This is used to pass information to and from the database to gateway/client
 * 
 * @author YM : Created on 06/17/2015
 *
 */
public class ClientRecord implements Serializable{

	private int clientID;
	private String hostName;
	private String hostInternalIP;
	private String hostExternalIP;
	private String oSName;
	private String osVersion;
	private boolean isMobile;
	private String browser;
	private String country;
	private String city;
	private String state;
	private double latitude;
	private double longitude;
	private String userName;
	private Date startTime;
	private String project;
	private String screenResolution;
	private int clientContext;
	
	public ClientRecord() {
		super();
		
	}
	/**
	 * @return the clientID
	 */
	public int getClientID() {
		return clientID;
	}
	/**
	 * @param clientID the clientID to set
	 */
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}
	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	/**
	 * @return the oSName
	 */
	public String getoSName() {
		return oSName;
	}
	/**
	 * @param oSName the oSName to set
	 */
	public void setoSName(String oSName) {
		this.oSName = oSName;
	}
	/**
	 * @return the osVersion
	 */
	public String getOsVersion() {
		return osVersion;
	}
	/**
	 * @param osVersion the osVersion to set
	 */
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	/**
	 * @return the isMobile
	 */
	public boolean isMobile() {
		return isMobile;
	}
	/**
	 * @param isMobile the isMobile to set
	 */
	public void setMobile(boolean isMobile) {
		this.isMobile = isMobile;
	}
	/**
	 * @return the browser
	 */
	public String getBrowser() {
		return browser;
	}
	/**
	 * @param browser the browser to set
	 */
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}
	/**
	 * @param project the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}
	/**
	 * @return the hostInternalIP
	 */
	public String getHostInternalIP() {
		return hostInternalIP;
	}
	/**
	 * @param hostInternalIP the hostInternalIP to set
	 */
	public void setHostInternalIP(String hostInternalIP) {
		this.hostInternalIP = hostInternalIP;
	}
	/**
	 * @return the hostExternalIP
	 */
	public String getHostExternalIP() {
		return hostExternalIP;
	}
	/**
	 * @param hostExternalIP the hostExternalIP to set
	 */
	public void setHostExternalIP(String hostExternalIP) {
		this.hostExternalIP = hostExternalIP;
	}
	/**
	 * @return the screenResolution
	 */
	public String getScreenResolution() {
		return screenResolution;
	}
	/**
	 * @param screenResolution the screenResolution to set
	 */
	public void setScreenResolution(String screenResolution) {
		this.screenResolution = screenResolution;
	}
	/**
	 * @return the Client_context
	 */
	public int getClientContext() {
		return clientContext;
	}
	/**
	 * @param clientContext the Client_context to set
	 */
	public void setClientContext(int clientContext) {
		this.clientContext = clientContext;
	}
}
