package com.vaspsolutions.analytics.common;
import java.io.Serializable;

public class ModuleAuditRecord implements Serializable{
	
	
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ACTOR;
	  private String  ACTOR_HOST;
	  private String  ACTION;
	  private String ACTION_TARGET;
	  private String ACTION_VALUE;
	  private int STATUS_CODE;
	  private String ORIGINATING_SYSTEM;
	  private String ORIGINATING_CONTEXT;
	public String getACTOR() {
		return ACTOR;
	}
	public void setACTOR(String aCTOR) {
		ACTOR = aCTOR;
	}
	public String getACTOR_HOST() {
		return ACTOR_HOST;
	}
	public void setACTOR_HOST(String aCTOR_HOST) {
		ACTOR_HOST = aCTOR_HOST;
	}
	public String getACTION() {
		return ACTION;
	}
	public void setACTION(String aCTION) {
		ACTION = aCTION;
	}
	public String getACTION_TARGET() {
		return ACTION_TARGET;
	}
	public void setACTION_TARGET(String aCTION_TARGET) {
		ACTION_TARGET = aCTION_TARGET;
	}
	public String getACTION_VALUE() {
		return ACTION_VALUE;
	}
	public void setACTION_VALUE(String aCTION_VALUE) {
		ACTION_VALUE = aCTION_VALUE;
	}
	public int getSTATUS_CODE() {
		return STATUS_CODE;
	}
	public void setSTATUS_CODE(int sTATUS_CODE) {
		STATUS_CODE = sTATUS_CODE;
	}
	public String getORIGINATING_SYSTEM() {
		return ORIGINATING_SYSTEM;
	}
	public void setORIGINATING_SYSTEM(String oRIGINATING_SYSTEM) {
		ORIGINATING_SYSTEM = oRIGINATING_SYSTEM;
	}
	public String getORIGINATING_CONTEXT() {
		return ORIGINATING_CONTEXT;
	}
	public void setORIGINATING_CONTEXT(String oRIGINATING_CONTEXT) {
		ORIGINATING_CONTEXT = oRIGINATING_CONTEXT;
	}
}
