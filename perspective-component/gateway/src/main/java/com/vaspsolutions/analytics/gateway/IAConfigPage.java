package com.vaspsolutions.analytics.gateway;



import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.inductiveautomation.ignition.gateway.web.components.RecordEditForm;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.model.IgnitionWebApp;
import com.inductiveautomation.ignition.gateway.web.components.RecordEditForm;
import com.inductiveautomation.ignition.gateway.web.models.LenientResourceModel;
import com.inductiveautomation.ignition.gateway.web.pages.IConfigPage;
import com.vaspsolutions.analytics.common.Constants;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Application;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleorm.dataset.SRecordInstance;

public class IAConfigPage extends RecordEditForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String[] PATH = {"Analytics Module", "settings"};
	Logger logger = LoggerFactory.getLogger(Constants.MODULE_LOG_NAME);
	
	
    public IAConfigPage(final IConfigPage configPage) {
    	
    	
        //super(configPage, null, new LenientResourceModel("IAConfigPage.nav.settings.title"),
                //((GatewayContext) Application.get()).getPersistenceInterface().find(MODIAPersistentRecord.META,0L));
        
        super(configPage, null, new LenientResourceModel("IAConfigPage.nav.settings.title"),
	            ((IgnitionWebApp) Application.get()).getContext().getPersistenceInterface().find(MODIAPersistentRecord.META, 0L)
	        );

        
    }


//    @Override
//    public String[] getMenuPath() {
//        return PATH;
//    }
    
    @Override
    public Pair<String, String> getMenuLocation() {
        return Pair.of("Analytics Module", "settings");
    }


//	@Override
//	public void setShowAdvanced(boolean showAdvanced) {
//		// TODO Auto-generated method stub
//		super.setShowAdvanced(showAdvanced);
//	}


	@Override
	protected boolean showAdvancedCheckbox(Set<Category> arg0) {
		// TODO Auto-generated method stub
		return super.showAdvancedCheckbox(arg0);
	}


	
    
    
    
}	
