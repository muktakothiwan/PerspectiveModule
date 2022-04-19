package com.vaspsolutions.analytics.common;

import java.io.Serializable;

public class TestMessage implements Serializable {

	public TestMessage()
	{
		System.out.println("os name : " + System.getProperty("os.name"));
	}
}
