package com.vaspsolutions.analytics.common;

import java.io.Serializable;
import java.util.HashMap;

import com.inductiveautomation.ignition.common.Dataset;
/**
 * Class to store alarms information to be shown on dashboard and real time
 * @author YM
 *
 */
public class AlarmsInformation implements Serializable{

	private static long serialVersionUID = 1L;

	private int noOfActiveAlarms;
	private int noOfAckAlarms;
	private HashMap<String, Integer> activeAlarmsCount;
	private HashMap<String, Integer> ackAlarmsCount;
	private HashMap<String, String> timeToClearAlarmsPerPriority;
	private HashMap<String, String> timeToAckAlarmsPerPriority;
	
	private String avgClearTime;
	private String avgAckTime;
	
	
	/**
	 * @return the noOfActiveAlarms
	 */
	public int getNoOfActiveAlarms() {
		return noOfActiveAlarms;
	}
	/**
	 * @param noOfActiveAlarms the noOfActiveAlarms to set
	 */
	public void setNoOfActiveAlarms(int noOfActiveAlarms) {
		this.noOfActiveAlarms = noOfActiveAlarms;
	}
	/**
	 * @return the noOfAckAlarms
	 */
	public int getNoOfAckAlarms() {
		return noOfAckAlarms;
	}
	/**
	 * @param noOfAckAlarms the noOfAckAlarms to set
	 */
	public void setNoOfAckAlarms(int noOfAckAlarms) {
		this.noOfAckAlarms = noOfAckAlarms;
	}
	
	/**
	 * @return the avgClearTime
	 */
	public String getAvgClearTime() {
		return avgClearTime;
	}
	/**
	 * @param avgClearTime the avgClearTime to set
	 */
	public void setAvgClearTime(String avgClearTime) {
		this.avgClearTime = avgClearTime;
	}
	/**
	 * @return the avgAckTime
	 */
	public String getAvgAckTime() {
		return avgAckTime;
	}
	/**
	 * @param avgAckTime the avgAckTime to set
	 */
	public void setAvgAckTime(String avgAckTime) {
		this.avgAckTime = avgAckTime;
	}
	/**
	 * @return the activeAlarmsCount
	 */
	public HashMap<String, Integer> getActiveAlarmsCount() {
		return activeAlarmsCount;
	}
	/**
	 * @param activeAlarmsCount the activeAlarmsCount to set
	 */
	public void setActiveAlarmsCount(HashMap<String, Integer> activeAlarmsCount) {
		this.activeAlarmsCount = activeAlarmsCount;
	}
	/**
	 * @return the ackAlarmsCount
	 */
	public HashMap<String, Integer> getAckAlarmsCount() {
		return ackAlarmsCount;
	}
	/**
	 * @param ackAlarmsCount the ackAlarmsCount to set
	 */
	public void setAckAlarmsCount(HashMap<String, Integer> ackAlarmsCount) {
		this.ackAlarmsCount = ackAlarmsCount;
	}
	/**
	 * @return the timeToClearAlarmsPerPriority
	 */
	public HashMap<String, String> getTimeToClearAlarmsPerPriority() {
		return timeToClearAlarmsPerPriority;
	}
	/**
	 * @param timeToClearAlarmsPerPriority the timeToClearAlarmsPerPriority to set
	 */
	public void setTimeToClearAlarmsPerPriority(
			HashMap<String, String> timeToClearAlarmsPerPriority) {
		this.timeToClearAlarmsPerPriority = timeToClearAlarmsPerPriority;
	}
	/**
	 * @return the timeToAckAlarmsPerPriority
	 */
	public HashMap<String, String> getTimeToAckAlarmsPerPriority() {
		return timeToAckAlarmsPerPriority;
	}
	/**
	 * @param timeToAckAlarmsPerPriority the timeToAckAlarmsPerPriority to set
	 */
	public void setTimeToAckAlarmsPerPriority(
			HashMap<String, String> timeToAckAlarmsPerPriority) {
		this.timeToAckAlarmsPerPriority = timeToAckAlarmsPerPriority;
	}
	
	
}
