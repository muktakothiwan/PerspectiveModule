package com.vaspsolutions.analytics.common;

import java.io.Serializable;

public class GroupReportRecord implements Serializable {
	private String gatewayName;
	private String groupName;
	private int noOfPeople;
	private int noOfVisits;
	private int noOfActions;
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	/**
	 * @return the noOfPeople
	 */
	public int getNoOfPeople() {
		return noOfPeople;
	}
	/**
	 * @param noOfPeople the noOfPeople to set
	 */
	public void setNoOfPeople(int noOfPeople) {
		this.noOfPeople = noOfPeople;
	}
	/**
	 * @return the noOfVisits
	 */
	public int getNoOfVisits() {
		return noOfVisits;
	}
	/**
	 * @param noOfVisits the noOfVisits to set
	 */
	public void setNoOfVisits(int noOfVisits) {
		this.noOfVisits = noOfVisits;
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
	 * @return the gatewayName
	 */
	public String getGatewayName() {
		return gatewayName;
	}
	/**
	 * @param gatewayName the gatewayName to set
	 */
	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}
}
