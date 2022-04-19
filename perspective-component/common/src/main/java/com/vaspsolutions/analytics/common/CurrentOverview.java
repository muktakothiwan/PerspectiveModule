package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.inductiveautomation.ignition.common.Dataset;

public class CurrentOverview implements Serializable{

	/**
	 * A class to represnt the information to be sent to Real Time Panel
	 */
	private static final long serialVersionUID = 1L;
	
	private long activeSessionLength;
	private int noOfActiveSessions;
	private int noOfActionsByCurrentUsers;
	private List<ScreensCount> screenViewscountPerUser;
	private List<UserLocations> userLocations;
	private HashMap<String, String> distinctUsers;
	private LocationDeviceBrowserCounts locationDeviceBrowsers;
	
	public LocationDeviceBrowserCounts getLocationDeviceBrowsers() {
		return locationDeviceBrowsers;
	}
	
	public void setLocationDeviceBrowsers(
			LocationDeviceBrowserCounts locationDeviceBrowsers) {
		this.locationDeviceBrowsers = locationDeviceBrowsers;
	}
	public HashMap<String, String> getDistinctUsers() {
		return distinctUsers;
	}
	public void setDistinctUsers(HashMap<String, String> distinctUsers) {
		this.distinctUsers = distinctUsers;
	}
	private int noOfActiveUsers;
	/**
	 * @return the activeSessionLength
	 */
	public long getActiveSessionLength() {
		return activeSessionLength;
	}
	/**
	 * @param activeSessionLength the activeSessionLength to set
	 */
	public void setActiveSessionLength(long activeSessionLength) {
		this.activeSessionLength = activeSessionLength;
	}
	/**
	 * @return the noOfActionsByCurrentUsers
	 */
	public int getNoOfActionsByCurrentUsers() {
		return noOfActionsByCurrentUsers;
	}
	/**
	 * @param noOfActionsByCurrentUsers the noOfActionsByCurrentUsers to set
	 */
	public void setNoOfActionsByCurrentUsers(int noOfActionsByCurrentUsers) {
		this.noOfActionsByCurrentUsers = noOfActionsByCurrentUsers;
	}
	
	
	/**
	 * @return the noOfActiveUsers
	 */
	public int getNoOfActiveUsers() {
		return noOfActiveUsers;
	}
	/**
	 * @param noOfActiveUsers the noOfActiveUsers to set
	 */
	public void setNoOfActiveUsers(int noOfActiveUsers) {
		this.noOfActiveUsers = noOfActiveUsers;
	}
	/**
	 * @return the noOfActiveSessions
	 */
	public int getNoOfActiveSessions() {
		return noOfActiveSessions;
	}
	/**
	 * @param noOfActiveSessions the noOfActiveSessions to set
	 */
	public void setNoOfActiveSessions(int noOfActiveSessions) {
		this.noOfActiveSessions = noOfActiveSessions;
	}
	/**
	 * @return the screenViewscountPerUser
	 */
	public List<ScreensCount> getScreenViewscountPerUser() {
		return screenViewscountPerUser;
	}
	/**
	 * @param screenViewscountPerUser the screenViewscountPerUser to set
	 */
	public void setScreenViewscountPerUser(
			List<ScreensCount> screenViewscountPerUser) {
		this.screenViewscountPerUser = screenViewscountPerUser;
	}
	/**
	 * @return the userLocations
	 */
	public List<UserLocations> getUserLocations() {
		return userLocations;
	}
	/**
	 * @param userLocations the userLocations to set
	 */
	public void setUserLocations(List<UserLocations> userLocations) {
		this.userLocations = userLocations;
	}

}
