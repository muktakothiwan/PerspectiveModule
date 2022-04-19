package com.vaspsolutions.analytics.gateway;



/**
 * A class to create module specific persistent record.
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import simpleorm.dataset.SFieldFlags;






import simpleorm.dataset.SFieldScalar;

import com.inductiveautomation.ignition.gateway.datasource.records.DatasourceRecord;
import com.inductiveautomation.ignition.gateway.gan.IncomingConnection;
import com.inductiveautomation.ignition.gateway.localdb.persistence.BooleanField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.LongField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.ReferenceField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;


public class MODIAPersistentRecord extends PersistentRecord {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	
	public static final RecordMeta<MODIAPersistentRecord> META = new RecordMeta<MODIAPersistentRecord>(MODIAPersistentRecord.class,"MODIA_SETTINGS")
			.setNounKey("MODIAPersistentRecord.Noun").setNounPluralKey(
		            "MODIAPersistentRecord.Noun.Plural");
	public static final IdentityField Id = new IdentityField(META);
//	public static final StringField dataSource =
//			new StringField(META, "dataSource", SFieldFlags.SMANDATORY);


public static final LongField ConnectionId = new LongField(META, "ConnectionId"); 
public static final ReferenceField<DatasourceRecord> dataSource = new ReferenceField<DatasourceRecord>(META, DatasourceRecord.META, "dataSource", ConnectionId).setDescriptive(true);	
	//(META, DatasourceRecord.META, DatasourceRecord.Name, dsId  ); 
			
			//ReferenceField<DatasourceRecord>(META, DatasourceRecord.META, "dataSource", dsId);
    public static BooleanField isEnterprise = new BooleanField(META, "isEnterprise", 
			 SFieldFlags.SMANDATORY).setDefault(false); 
//	
	public static BooleanField isAgent = 
			new BooleanField(META, "isAgent", 
					 SFieldFlags.SMANDATORY).setDefault(false);
	public static final LongField ControllerConnectionId = new LongField(META, "ControllerConnectionId"); 
	public static final ReferenceField<IncomingConnection> ControllerName = new ReferenceField<IncomingConnection>(META, IncomingConnection.META, "ControllerName", ControllerConnectionId).setDescriptive(true);	

	static final Category HubConfiguration = new Category("MODIAPersistentRecord.Category.Configuration", 1000).include(dataSource);
	static final Category EnterpriseConfiguration = new Category("MODIAPersistentRecord.Category.EnterpriseConfiguration", 1001, true).include(isEnterprise, isAgent, ControllerName);
	
	
	
	
	@Override
	public RecordMeta<?> getMeta() {
		return this.META;
	}
	public Long getId() {
		return getLong(Id);
	}
	public  String getDatasource() {
		//return getString(dataSource);
		return "" + getLong(ConnectionId);
	}

	public void setId(Long id) {
		setLong(Id, id);
	}
	public void setDatasource(String datasource) {
		setString(dataSource, datasource);
	}
	public boolean getIsAgent() {
		return getBoolean(isAgent);
	}
	public  void setIsAgent(boolean isAgent) {
		setBoolean(MODIAPersistentRecord.isAgent, isAgent);
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public static Category getHubconfiguration() {
		return HubConfiguration;
	}
	public Long getConnectionid() {
		return getLong(ConnectionId);
	}
	public Long getControllerconnectionid() {
		return getLong(ControllerConnectionId);
	}
	
	public  boolean getIsEnterprise() {
		return getBoolean(isEnterprise);
	}
	public  void setIsEnterprise(boolean isEnterprise) {
		setBoolean(MODIAPersistentRecord.isEnterprise ,isEnterprise);
	}

}
