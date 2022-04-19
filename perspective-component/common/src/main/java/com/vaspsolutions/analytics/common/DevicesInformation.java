package com.vaspsolutions.analytics.common;

import java.io.Serializable;
/**
 * A class to send/receive data about devices information
 * @author YM
 *
 */
public class DevicesInformation implements Serializable{
	private int noOfClientsOnMobile;
	private int noOfClientsOnDesktop;
	/**
	 * @return the noOfClientsOnMobile
	 */
	public int getNoOfClientsOnMobile() {
		return noOfClientsOnMobile;
	}
	/**
	 * @param noOfClientsOnMobile the noOfClientsOnMobile to set
	 */
	public void setNoOfClientsOnMobile(int noOfClientsOnMobile) {
		this.noOfClientsOnMobile = noOfClientsOnMobile;
	}
	/**
	 * @return the noOfClientsOnDesktop
	 */
	public int getNoOfClientsOnDesktop() {
		return noOfClientsOnDesktop;
	}
	/**
	 * @param noOfClientsOnDesktop the noOfClientsOnDesktop to set
	 */
	public void setNoOfClientsOnDesktop(int noOfClientsOnDesktop) {
		this.noOfClientsOnDesktop = noOfClientsOnDesktop;
	}
	
}
