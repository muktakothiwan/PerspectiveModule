package com.vaspsolutions.analytics.common;

public class UserSessionsInfo {
	String userName;
	String profileName;
	long creationTime;
	long timeDifference;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	public long getTimeDifference() {
		return timeDifference;
	}
	public void setTimeDifference(long timeDifference) {
		this.timeDifference = timeDifference;
	}
	

}
