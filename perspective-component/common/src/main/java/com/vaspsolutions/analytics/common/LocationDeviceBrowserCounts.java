package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class LocationDeviceBrowserCounts implements Serializable{
	HashMap<String ,Integer> locations;
	HashMap<String ,Integer> browsers;
	DevicesInformation devices;
	HashMap<String ,Integer> operatingSystems;
	public HashMap<String, Integer> getOperatingSystems() {
		return operatingSystems;
	}
	public void setOperatingSystems(HashMap<String, Integer> operatingSystems) {
		this.operatingSystems = operatingSystems;
	}
	public HashMap<String ,Integer> getLocations() {
		return locations;
	}
	public void setLocations(HashMap<String ,Integer> locations) {
		this.locations = locations;
	}
	public HashMap<String ,Integer> getBrowsers() {
		return browsers;
	}
	public void setBrowsers(HashMap<String ,Integer> browsers) {
		this.browsers = browsers;
	}
	public DevicesInformation getDevices() {
		return devices;
	}
	public void setDevices(DevicesInformation devices) {
		this.devices = devices;
	}

}
