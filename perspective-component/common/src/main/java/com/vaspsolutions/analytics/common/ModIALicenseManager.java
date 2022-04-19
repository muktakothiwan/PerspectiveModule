package com.vaspsolutions.analytics.common;

import java.util.Date;

import com.inductiveautomation.ignition.common.licensing.LicenseMode;
import com.inductiveautomation.ignition.common.licensing.LicenseState;

public class ModIALicenseManager {
	private static ModIALicenseManager instance = null;
	   private LicenseState lState;
	   private Date expirationDate;
	   protected ModIALicenseManager(){
		      
	   }
	   public static ModIALicenseManager getInstance(){
	      if(instance == null) {
	    	  System.out.println("license manager creating new instance");
	         instance = new ModIALicenseManager();
	      }
	      return instance;
	   }
	   public void setLicenseState(LicenseState activationState){
	      this.lState = activationState;
	   }
	   
	   public void setExpirationDate(Date eDate)
	   {
		   this.expirationDate = eDate;
	   }
	   
	   public boolean getLicenseExpired()
	   {
		   if(Constants.licenseCheckEnabled)
		   {

		      if( lState.getLicenseMode() == LicenseMode.Trial){
		         return true;
		      } else{
		    	  Date currentDate = new Date();
		    	  if(this.expirationDate == null)
		    	  {
		    		  return false;
		    	  }
		    	  else 
		    	  {
		    		  if(currentDate.compareTo(this.expirationDate) <= 0)
		    			  
		    		  {
		    			  return false; //license is valid. Hence isLicenseExpired will be false. All other cases, return true
		    		  }
		    		  else
		    		  {
		    			  return true;
		    		  }
		    	  }
		      }
		   }
		   else
		   {
			   return false;
		   }
	   }
}
