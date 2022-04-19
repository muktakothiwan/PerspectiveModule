package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inductiveautomation.ignition.common.Dataset;

public class RealTimeData implements Serializable {
	
	private int sevenDaysMax;
	private int sevenDaysMin;
	private CurrentOverview cOverview;
	private int noOfNewUsers;
	private int noOfActiveUsers;
	private HashMap<String,Integer> contentData;
	private AlarmsInformation alarmInfo;
	private List<UsersOverviewInformation> uOverview;
	private Dataset engagementData;
	private ActiveUsersInfo activeUsersData;
	public int getSevenDaysMax() {
		return sevenDaysMax;
	}
	public void setSevenDaysMax(int sevenDaysMax) {
		this.sevenDaysMax = sevenDaysMax;
	}
	public int getSevenDaysMin() {
		return sevenDaysMin;
	}
	public void setSevenDaysMin(int sevenDaysMin) {
		this.sevenDaysMin = sevenDaysMin;
	}
	public CurrentOverview getcOverview() {
		return cOverview;
	}
	public void setcOverview(CurrentOverview cOverview) {
		this.cOverview = cOverview;
	}
	public int getNoOfNewUsers() {
		return noOfNewUsers;
	}
	public void setNoOfNewUsers(int noOfNewUsers) {
		this.noOfNewUsers = noOfNewUsers;
	}
	public int getNoOfActiveUsers() {
		return noOfActiveUsers;
	}
	public void setNoOfActiveUsers(int noOfActiveUsers) {
		this.noOfActiveUsers = noOfActiveUsers;
	}
	public HashMap<String, Integer> getContentData() {
		return contentData;
	}
	public void setContentData(HashMap<String, Integer> contentData) {
		this.contentData = contentData;
	}
	public AlarmsInformation getAlarmInfo() {
		return alarmInfo;
	}
	public void setAlarmInfo(AlarmsInformation alarmInfo) {
		this.alarmInfo = alarmInfo;
	}
	
	public Dataset getEngagementData() {
		return engagementData;
	}
	public void setEngagementData(Dataset engagementData) {
		this.engagementData = engagementData;
	}
	public ActiveUsersInfo getActiveUsersData() {
		return activeUsersData;
	}
	public void setActiveUsersData(ActiveUsersInfo activeUsersData) {
		this.activeUsersData = activeUsersData;
	}
	public List<UsersOverviewInformation> getuOverview() {
		return uOverview;
	}
	public void setuOverview(List<UsersOverviewInformation> uOverview) {
		this.uOverview = uOverview;
	}
	
	
}
