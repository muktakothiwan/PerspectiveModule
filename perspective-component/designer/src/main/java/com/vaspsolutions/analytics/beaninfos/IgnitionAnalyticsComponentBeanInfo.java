package com.vaspsolutions.analytics.beaninfos;

import java.awt.Image;
import java.beans.IntrospectionException;

import com.inductiveautomation.factorypmi.designer.property.customizers.DynamicPropertyProviderCustomizer;
import com.inductiveautomation.factorypmi.designer.property.customizers.StyleCustomizer;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.vision.api.designer.beans.CommonBeanInfo;
import com.inductiveautomation.vision.api.designer.beans.CustomizerDescriptor;
import com.inductiveautomation.vision.api.designer.beans.VisionBeanDescriptor;
import com.vaspsolutions.analytics.client.IgnitionAnalyticsComponent;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ModuleRPC;


public class IgnitionAnalyticsComponentBeanInfo extends CommonBeanInfo {

	public IgnitionAnalyticsComponentBeanInfo()
	{
		super(IgnitionAnalyticsComponent.class, new CustomizerDescriptor[] {
			DynamicPropertyProviderCustomizer.VALUE_DESCRIPTOR, StyleCustomizer.VALUE_DESCRIPTOR });
	}
	
	@Override
	protected void initProperties() throws IntrospectionException {
		// Adds common properties
		super.initProperties();
		
		ModuleRPC rpc = ModuleRPCFactory.create(Constants.MODULE_ID, ModuleRPC.class);
		rpc.getPersistenceRecord();
		
		//commenting this as we are accepting configuration from webPage.
	//	addProp("dsName", "Datasource Name", "Data source to be used for storing audit trail", CAT_DATA, PREFERRED_MASK | BOUND_MASK);
		
		
		addDataQuality();
		
	}

	@Override
	public Image getIcon(int kind) {
		/* switch (kind) {
			case BeanInfo.ICON_COLOR_16x16:
			case BeanInfo.ICON_MONO_16x16:
				return new ImageIcon(getClass().getResource("/images/hello_world_16.png")).getImage();
			case SimpleBeanInfo.ICON_COLOR_32x32:
			case SimpleBeanInfo.ICON_MONO_32x32:
				return new ImageIcon(getClass().getResource("/images/hello_world_32.png")).getImage();
		} */
		return null;
	}
	
	@Override
	protected void initDesc() {
		VisionBeanDescriptor bean = getBeanDescriptor();
		bean.setName("Analytics Module");
		bean.setDisplayName("Analytics Module");
		bean.setShortDescription("A component that displays realtime and historical information");
		
	}
}
