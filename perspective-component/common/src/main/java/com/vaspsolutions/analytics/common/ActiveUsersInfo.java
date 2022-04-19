package com.vaspsolutions.analytics.common;

import java.io.Serializable;

public class ActiveUsersInfo implements Serializable {

	private int OneDayActiveUsers;
	private int SevenDayActiveUsers;
	
	private int FourteenDayActiveUsers;
	
	public ActiveUsersInfo() {
		super();
		this.OneDayActiveUsers = 0;
		this.SevenDayActiveUsers = 0;
		this.FourteenDayActiveUsers = 0;
	}
	/**
	 * @return the oneDayActiveUsers
	 */
	public int getOneDayActiveUsers() {
		return OneDayActiveUsers;
	}
	/**
	 * @param oneDayActiveUsers the oneDayActiveUsers to set
	 */
	public void setOneDayActiveUsers(int oneDayActiveUsers) {
		OneDayActiveUsers = oneDayActiveUsers;
	}
	/**
	 * @return the sevenDayActiveUsers
	 */
	public int getSevenDayActiveUsers() {
		return SevenDayActiveUsers;
	}
	/**
	 * @param sevenDayActiveUsers the sevenDayActiveUsers to set
	 */
	public void setSevenDayActiveUsers(int sevenDayActiveUsers) {
		SevenDayActiveUsers = sevenDayActiveUsers;
	}
	/**
	 * @return the fourteenDayActiveUsers
	 */
	public int getFourteenDayActiveUsers() {
		return FourteenDayActiveUsers;
	}
	/**
	 * @param fourteenDayActiveUsers the fourteenDayActiveUsers to set
	 */
	public void setFourteenDayActiveUsers(int fourteenDayActiveUsers) {
		FourteenDayActiveUsers = fourteenDayActiveUsers;
	}
}
