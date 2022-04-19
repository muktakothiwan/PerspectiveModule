package com.vaspsolutions.analytics.common;
import java.io.Serializable;
import java.util.List;
/**
 * Class to hold information displayed in overview section.
 * @author YM : Created on 06/23/2015
 *
 */
public class OverviewInformation implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int noOfActiveUsers;
	private int noOfSessions;
	private List<ScreensCount> screenViews;
	private int noOfActions;
	private float bounceRate;
	private String avgSessionDuration;
	private String gatewayDownTimeString;
	private String gatewayUpTimeString;
	private float gatewayUpTimePercent;
	private float gatewayDownTimePercent;
	private float averageScreensPerVisit;
	private int noOfNewUsers;
	private float actionsPerSession;
	private int noOfScreenViews;
	/**
	 * @return the noOfActiveUsers
	 */
	public int getNoOfActiveUsers() {
		return noOfActiveUsers;
	}
	public OverviewInformation() {
		super();
		noOfActiveUsers = 0;
		noOfSessions = 0;
		noOfActions = 0;
		bounceRate = 0;
		avgSessionDuration = "00:00:00";
		gatewayDownTimeString = "";
		gatewayUpTimeString = "";
		gatewayUpTimePercent = 100;
		gatewayDownTimePercent = 0;
		averageScreensPerVisit = 0;
		noOfNewUsers = 0;
		actionsPerSession = 0;
		noOfScreenViews = 0;
	}
	/**
	 * @param noOfActiveUsers the noOfActiveUsers to set
	 */
	public void setNoOfActiveUsers(int noOfActiveUsers) {
		this.noOfActiveUsers = noOfActiveUsers;
	}
	/**
	 * @return the noOfSessions
	 */
	public int getNoOfSessions() {
		return noOfSessions;
	}
	/**
	 * @param noOfSessions the noOfSessions to set
	 */
	public void setNoOfSessions(int noOfSessions) {
		this.noOfSessions = noOfSessions;
	}
	/**
	 * @return the screenViews
	 */
	public List<ScreensCount> getScreenViews() {
		return screenViews;
	}
	/**
	 * @param screenViews the screenViews to set
	 */
	public void setScreenViews(List<ScreensCount> screenViews) {
		this.screenViews = screenViews;
	}
	/**
	 * @return the noOfActions
	 */
	public int getNoOfActions() {
		return noOfActions;
	}
	/**
	 * @param noOfActions the noOfActions to set
	 */
	public void setNoOfActions(int noOfActions) {
		this.noOfActions = noOfActions;
	}
	/**
	 * @return the bounceRate
	 */
	public float getBounceRate() {
		return bounceRate;
	}
	/**
	 * @param bounceRate the bounceRate to set
	 */
	public void setBounceRate(float bounceRate) {
		this.bounceRate = bounceRate;
	}
	/**
	 * @return the avgSessionDuration
	 */
	public String getAvgSessionDuration() {
		return avgSessionDuration;
	}
	/**
	 * @param avgSessionDuration the avgSessionDuration to set
	 */
	public void setAvgSessionDuration(String avgSessionDuration) {
		this.avgSessionDuration = avgSessionDuration;
	}
	/**
	 * @return the gatewayDownTimeString
	 */
	public String getGatewayDownTimeString() {
		return gatewayDownTimeString;
	}
	/**
	 * @param gatewayDownTimeString the gatewayDownTimeString to set
	 */
	public void setGatewayDownTimeString(String gatewayDownTimeString) {
		this.gatewayDownTimeString = gatewayDownTimeString;
	}
	/**
	 * @return the gatewayUpTimeString
	 */
	public String getGatewayUpTimeString() {
		return gatewayUpTimeString;
	}
	/**
	 * @param gatewayUpTimeString the gatewayUpTimeString to set
	 */
	public void setGatewayUpTimeString(String gatewayUpTimeString) {
		this.gatewayUpTimeString = gatewayUpTimeString;
	}
	/**
	 * @return the gatewayUpTimePercent
	 */
	public float getGatewayUpTimePercent() {
		return gatewayUpTimePercent;
	}
	/**
	 * @param gatewayUpTimePercent the gatewayUpTimePercent to set
	 */
	public void setGatewayUpTimePercent(float gatewayUpTimePercent) {
		this.gatewayUpTimePercent = gatewayUpTimePercent;
	}
	/**
	 * @return the gatewayDownTimePercent
	 */
	public float getGatewayDownTimePercent() {
		return gatewayDownTimePercent;
	}
	/**
	 * @param gatewayDownTimePercent the gatewayDownTimePercent to set
	 */
	public void setGatewayDownTimePercent(float gatewayDownTimePercent) {
		this.gatewayDownTimePercent = gatewayDownTimePercent;
	}
	/**
	 * @return the averageScreensPerVisit
	 */
	public float getAverageScreensPerVisit() {
		return averageScreensPerVisit;
	}
	/**
	 * @param averageScreensPerVisit the averageScreensPerVisit to set
	 */
	public void setAverageScreensPerVisit(float averageScreensPerVisit) {
		this.averageScreensPerVisit = averageScreensPerVisit;
	}
	/**
	 * @return the noOfNewUsers
	 */
	public int getNoOfNewUsers() {
		return noOfNewUsers;
	}
	/**
	 * @param noOfNewUsers the noOfNewUsers to set
	 */
	public void setNoOfNewUsers(int noOfNewUsers) {
		this.noOfNewUsers = noOfNewUsers;
	}
	/**
	 * @return the actionsPerSession
	 */
	public float getActionsPerSession() {
		return actionsPerSession;
	}
	/**
	 * @param actionsPerSession the actionsPerSession to set
	 */
	public void setActionsPerSession(float actionsPerSession) {
		this.actionsPerSession = actionsPerSession;
	}
	/**
	 * @return the noOfScreenViews
	 */
	public int getNoOfScreenViews() {
		return noOfScreenViews;
	}
	/**
	 * @param noOfScreenViews the noOfScreenViews to set
	 */
	public void setNoOfScreenViews(int noOfScreenViews) {
		this.noOfScreenViews = noOfScreenViews;
	}
}
