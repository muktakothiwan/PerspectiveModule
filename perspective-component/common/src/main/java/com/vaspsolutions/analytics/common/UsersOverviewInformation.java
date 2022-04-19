package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.List;

public class UsersOverviewInformation implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String firstSeen;
	String lastSeen;
	String totalSessionsLength;
	public  int totalActions;
	public  int totalVisits;
	public int totalActionsLast7Days;
	public  int totalVisitsLast7Days;
	public List<ScreensCount> screensViewed;
	public List<ScreensCount> screensViewedLast7Days;
	String currentScreen;
	String location;
	public int screensViewedCount;
	public int screensViewedLast7DaysCount;
	/**
	 * @return the firstSeen
	 */
	public String getFirstSeen() {
		return firstSeen;
	}
	/**
	 * @param firstSeen the firstSeen to set
	 */
	public void setFirstSeen(String firstSeen) {
		this.firstSeen = firstSeen;
	}
	/**
	 * @return the lastSeen
	 */
	public String getLastSeen() {
		return lastSeen;
	}
	/**
	 * @param lastSeen the lastSeen to set
	 */
	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}
	/**
	 * @return the totalSessionsLength
	 */
	public String getTotalSessionsLength() {
		return totalSessionsLength;
	}
	/**
	 * @param totalSessionsLength the totalSessionsLength to set
	 */
	public void setTotalSessionsLength(String totalSessionsLength) {
		this.totalSessionsLength = totalSessionsLength;
	}
	/**
	 * @return the totalActions
	 */
	public int getTotalActions() {
		return totalActions;
	}
	/**
	 * @param totalActions the totalActions to set
	 */
	public void setTotalActions(int totalActions) {
		this.totalActions = totalActions;
	}
	/**
	 * @return the totalVisits
	 */
	public int getTotalVisits() {
		return totalVisits;
	}
	/**
	 * @param totalVisits the totalVisits to set
	 */
	public void setTotalVisits(int totalVisits) {
		this.totalVisits = totalVisits;
	}
	/**
	 * @return the screensViewed
	 */
	public List<ScreensCount> getScreensViewed() {
		return screensViewed;
	}
	/**
	 * @param screensViewed the screensViewed to set
	 */
	public void setScreensViewed(List<ScreensCount> screensViewed) {
		this.screensViewed = screensViewed;
	}
	/**
	 * @return the currentScreen
	 */
	public String getCurrentScreen() {
		return currentScreen;
	}
	/**
	 * @param currentScreen the currentScreen to set
	 */
	public void setCurrentScreen(String currentScreen) {
		this.currentScreen = currentScreen;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	public int getTotalActionsLast7Days() {
		return totalActionsLast7Days;
	}
	public void setTotalActionsLast7Days(int totalActionsLast7Days) {
		this.totalActionsLast7Days = totalActionsLast7Days;
	}
	public int getTotalVisitsLast7Days() {
		return totalVisitsLast7Days;
	}
	public void setTotalVisitsLast7Days(int totalVisitsLast7Days) {
		this.totalVisitsLast7Days = totalVisitsLast7Days;
	}
	public List<ScreensCount> getScreensViewedLast7Days() {
		return screensViewedLast7Days;
	}
	public void setScreensViewedLast7Days(List<ScreensCount> screensViewedLast7Days) {
		this.screensViewedLast7Days = screensViewedLast7Days;
	}
}
