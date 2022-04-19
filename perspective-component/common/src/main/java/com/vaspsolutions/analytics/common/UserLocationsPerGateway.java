package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.List;

public class UserLocationsPerGateway implements Serializable{

	String gatewayID;
	List<UserLocations> userLocations;
	public UserLocationsPerGateway() {
		// TODO Auto-generated constructor stub
	}
	public String getGatewayID() {
		return gatewayID;
	}
	public void setGatewayID(String gatewayID) {
		this.gatewayID = gatewayID;
	}
	public List<UserLocations> getUserLocations() {
		return userLocations;
	}
	public void setUserLocations(List<UserLocations> userLocations) {
		this.userLocations = userLocations;
	}

}
