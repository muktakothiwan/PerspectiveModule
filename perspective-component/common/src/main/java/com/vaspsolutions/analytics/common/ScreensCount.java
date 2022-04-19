package com.vaspsolutions.analytics.common;

import java.io.Serializable;

public class ScreensCount implements Serializable{

	private String screenName;
	private int noOfViews;
	
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public int getNoOfViews() {
		return noOfViews;
	}
	public void setNoOfViews(int noOfViews) {
		this.noOfViews = noOfViews;
	}
}
