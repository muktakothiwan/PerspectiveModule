package com.vaspsolutions.analytics.common;
import java.io.Serializable;

public class ContentsData implements Serializable{
	
	private String screenName;
	private int userCount;
	
	public ContentsData() {
		super();
		this.screenName  = "";
		this.userCount = 0;
	}
	
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public int getUserCount() {
		return userCount;
	}
	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
}
