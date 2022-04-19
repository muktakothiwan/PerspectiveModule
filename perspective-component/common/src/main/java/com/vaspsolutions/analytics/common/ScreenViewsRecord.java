package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.Date;

public class ScreenViewsRecord implements Serializable{

	private int auditEventID;
	private Date viewTimestamp;
	private String username;
	private String screenName;
	private String screenPath;
	private String screenTitle;
	private String projectName;
	private String action;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public String getScreenPath() {
		return screenPath;
	}
	public void setScreenPath(String screenPath) {
		this.screenPath = screenPath;
	}
	public int getAuditEventID() {
		return auditEventID;
	}
	public void setAuditEventID(int auditEventID) {
		this.auditEventID = auditEventID;
	}
	public Date getViewTimestamp() {
		return viewTimestamp;
	}
	public void setViewTimestamp(Date viewTimestamp) {
		this.viewTimestamp = viewTimestamp;
	}
	public String getScreenTitle() {
		return screenTitle;
	}
	public void setScreenTitle(String screenTitle) {
		this.screenTitle = screenTitle;
	}
	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}
	/**
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
}
