package com.vaspsolutions.analytics.common;

import java.io.Serializable;

public class UsersCount implements Serializable{

	int num_newUsers;
	int num_retUsers;
	
	
	public UsersCount() {
		super();
		// TODO Auto-generated constructor stub
	}


	public int getNum_newUsers() {
		return num_newUsers;
	}


	public void setNum_newUsers(int num_newUsers) {
		this.num_newUsers = num_newUsers;
	}


	public int getNum_retUsers() {
		return num_retUsers;
	}


	public void setNum_retUsers(int num_retUsers) {
		this.num_retUsers = num_retUsers;
	}


	
}
