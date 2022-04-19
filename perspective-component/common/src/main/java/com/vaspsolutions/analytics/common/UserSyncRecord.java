package com.vaspsolutions.analytics.common;

import java.io.Serializable;


public class UserSyncRecord implements Serializable{
	String userName;
	String onlineStatus;
	String email;
	String roles;
	String phone;
	String gatewayUserprofile;
	String currentLocation;
	String firstSeen;
	String lastSeen;
	String last7daysDuration;
	int last7daysVisits;
	int last7DaysActions;
	int allActions;
	int allScreenViews;
	String currentScreen;
	String projectName;
	public UserSyncRecord() {
		
		
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getOnlineStatus() {
		return onlineStatus;
	}
	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getGatewayUserprofile() {
		return gatewayUserprofile;
	}
	public void setGatewayUserprofile(String gatewayUserprofile) {
		this.gatewayUserprofile = gatewayUserprofile;
	}
	public String getCurrentLocation() {
		return currentLocation;
	}
	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}
	public String getFirstSeen() {
		return firstSeen;
	}
	public void setFirstSeen(String firstSeen) {
		this.firstSeen = firstSeen;
	}
	public String getLastSeen() {
		return lastSeen;
	}
	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}
	public String getLast7daysDuration() {
		return last7daysDuration;
	}
	public void setLast7daysDuration(String last7daysDuration) {
		this.last7daysDuration = last7daysDuration;
	}
	public int getLast7daysVisits() {
		return last7daysVisits;
	}
	public void setLast7daysVisits(int last7daysVisits) {
		this.last7daysVisits = last7daysVisits;
	}
	public int getLast7DaysActions() {
		return last7DaysActions;
	}
	public void setLast7DaysActions(int last7DaysActions) {
		this.last7DaysActions = last7DaysActions;
	}
	public int getAllActions() {
		return allActions;
	}
	public void setAllActions(int allActions) {
		this.allActions = allActions;
	}
	public int getAllScreenViews() {
		return allScreenViews;
	}
	public void setAllScreenViews(int allScreenViews) {
		this.allScreenViews = allScreenViews;
	}
	public String getCurrentScreen() {
		return currentScreen;
	}
	public void setCurrentScreen(String currentScreen) {
		this.currentScreen = currentScreen;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
