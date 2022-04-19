package com.vaspsolutions.analytics.gateway;

import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.history.DatasourceData;
import com.inductiveautomation.ignition.gateway.history.HistoryFlavor;

public class StoreForwardDS implements DatasourceData {

	String query;
	String dsName;

	
	public StoreForwardDS(String query, String dsName) {
		super();
		this.query = query;
		this.dsName = dsName;
	}

	@Override
	public int getDataCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public HistoryFlavor getFlavor() {
		// TODO Auto-generated method stub
		return DatasourceData.FLAVOR;
	}

	@Override
	public String getLoggerName() {
		// TODO Auto-generated method stub
		return "IA-StoreForwardDS";
	}

	@Override
	public String getSignature() {
		// TODO Auto-generated method stub
		return "IA-StoreForwardDS: " + this.dsName + " - " + query;
	}

	@Override
	public void storeToConnection(SRConnection arg0) throws Exception {

		arg0.runUpdateQuery(this.query);

	}

}
