package com.vaspsolutions.analytics.client;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.vaspsolutions.analytics.UI.IA_Label;
import com.vaspsolutions.analytics.UI.IA_TextBox;
import com.vaspsolutions.analytics.common.Constants;
import com.vaspsolutions.analytics.common.ModuleRPC;

import java.awt.Insets;

/**
 * A class that extends from JPanel to show screen configuration screen to the user and 
 * accept and store configuration information
 * @author YM
 *
 */
public class ConfigurationPanel extends JPanel implements ActionListener {
	
	ModuleRPC rpc; 
	
	private IA_Label lblNewDSName;
	private IA_Label lblExistingAuditDS;
	private IA_Label lblExistingAuditTableName;
	private IA_Label lblExistingAlarmTableName;

	private IA_TextBox txtnewDSName;
	private IA_TextBox txtexistingAuditDS;
	private IA_TextBox txtexistingAuditTableName;
	private IA_TextBox txtexistingAlarmTableName;
	
	public ConfigurationPanel(ModuleRPC rpc) {
		
		this.rpc = rpc;
		
		//set layout and add UI components.
		IA_Label lblTitle = new IA_Label();
		lblTitle.setText("Please provide following configuration information");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{185, 40, 10, 10, 10, 10, 10};
		gridBagLayout.rowHeights = new int[]{20, 20, 20, 20, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
				
		lblNewDSName = new IA_Label();
		lblNewDSName.setText("Please enter the name of newly configured data source : ");
				
				
		GridBagConstraints gbc_lblNewDSName = new GridBagConstraints();
		gbc_lblNewDSName.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewDSName.anchor = GridBagConstraints.EAST;
		gbc_lblNewDSName.gridwidth = 3;
		gbc_lblNewDSName.gridx = 0;
		gbc_lblNewDSName.gridy = 0;
		add(lblNewDSName, gbc_lblNewDSName);
		txtnewDSName = new IA_TextBox();
		txtnewDSName.setSize(50, 20);		
		GridBagConstraints gbc_txtnewDSName = new GridBagConstraints();
		gbc_txtnewDSName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtnewDSName.gridwidth = 4;
		gbc_txtnewDSName.insets = new Insets(0, 0, 5, 5);
		gbc_txtnewDSName.gridx = 3;
		gbc_txtnewDSName.gridy = 0;
		
		add(txtnewDSName, gbc_txtnewDSName);
				
		lblExistingAuditDS = new IA_Label();
		lblExistingAuditDS.setText("Please enter the name of existing Audit data source : ");
				
				
		GridBagConstraints gbc_lblExistingAuditDS = new GridBagConstraints();
		gbc_lblExistingAuditDS.anchor = GridBagConstraints.EAST;
		gbc_lblExistingAuditDS.insets = new Insets(0, 0, 5, 5);
		gbc_lblExistingAuditDS.gridwidth = 3;
		gbc_lblExistingAuditDS.gridx = 0;
		gbc_lblExistingAuditDS.gridy = 1;
		add(lblExistingAuditDS, gbc_lblExistingAuditDS);
		
		txtexistingAuditDS = new IA_TextBox();
		GridBagConstraints gbc_txtexistingAuditDS = new GridBagConstraints();
		gbc_txtexistingAuditDS.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtexistingAuditDS.gridwidth = 4;
		gbc_txtexistingAuditDS.insets = new Insets(0, 0, 5, 5);
		gbc_txtexistingAuditDS.gridx = 3;
		gbc_txtexistingAuditDS.gridy = 1;
		add(txtexistingAuditDS, gbc_txtexistingAuditDS);
				
		lblExistingAuditTableName = new IA_Label();
		lblExistingAuditTableName.setText("Please enter the name of existing Audit table : ");
		GridBagConstraints gbc_lblExistingAuditTableName = new GridBagConstraints();
		gbc_lblExistingAuditTableName.anchor = GridBagConstraints.EAST;
		gbc_lblExistingAuditTableName.insets = new Insets(0, 0, 5, 5);
		gbc_lblExistingAuditTableName.gridwidth = 3;
		gbc_lblExistingAuditTableName.gridx = 0;
		gbc_lblExistingAuditTableName.gridy = 2;
		add(lblExistingAuditTableName, gbc_lblExistingAuditTableName);
		
				
		txtexistingAuditTableName = new IA_TextBox("AUDIT_EVENTS");
		GridBagConstraints gbc_txtexistingAuditTableName = new GridBagConstraints();
		gbc_txtexistingAuditTableName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtexistingAuditTableName.gridwidth = 5;
		gbc_txtexistingAuditTableName.insets = new Insets(0, 0, 5, 0);
		gbc_txtexistingAuditTableName.gridx = 3;
		gbc_txtexistingAuditTableName.gridy = 2;
		add(txtexistingAuditTableName, gbc_txtexistingAuditTableName);
				
		lblExistingAlarmTableName = new IA_Label();
		lblExistingAlarmTableName.setText("Please enter the name of existing Alarm events table : ");
				
		GridBagConstraints gbc_lblExistingAlarmTableName = new GridBagConstraints();
		gbc_lblExistingAlarmTableName.anchor = GridBagConstraints.EAST;
		gbc_lblExistingAlarmTableName.insets = new Insets(0, 0, 5, 5);
		gbc_lblExistingAlarmTableName.gridwidth = 3;
		gbc_lblExistingAlarmTableName.gridx = 0;
		gbc_lblExistingAlarmTableName.gridy = 3;
		add(lblExistingAlarmTableName, gbc_lblExistingAlarmTableName);
		txtexistingAlarmTableName = new IA_TextBox("ALARM_EVENTS");
				
		GridBagConstraints gbc_txtexistingAlarmTableName = new GridBagConstraints();
		gbc_txtexistingAlarmTableName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtexistingAlarmTableName.gridwidth = 5;
		gbc_txtexistingAlarmTableName.insets = new Insets(0, 0, 5, 0);
		gbc_txtexistingAlarmTableName.gridx =3;
		gbc_txtexistingAlarmTableName.gridy = 3;
		add(txtexistingAlarmTableName, gbc_txtexistingAlarmTableName);

		JButton okButton = new JButton("Ok");
		okButton.setActionCommand(Constants.CMD_CONFIGURE_OK);
		okButton.addActionListener(this);
		okButton.setSize(20, 10);
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_okButton.insets = new Insets(0, 0, 0, 5);
		gbc_okButton.gridx = 0;
		gbc_okButton.gridy = 4;
		add(okButton, gbc_okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setSize(20, 10);
		cancelButton.setActionCommand(Constants.CMD_CONFIGURE_CANCEL);
		cancelButton.addActionListener(this);
				
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 4;
		add(cancelButton, gbc_cancelButton);
		
		
	}
	
	/**
	 * Method to handle user UI actions
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
//		String command = arg0.getActionCommand();
//		if(command.compareToIgnoreCase(Constants.CMD_CONFIGURE_OK) == 0)
//		{
//			String newDSName = "";
//			String oldDSName = "";
//			String auditTableName = "";
//			String alarmsTableName = "";
//			
//			newDSName = this.txtnewDSName.getText();
//			oldDSName = this.txtexistingAuditDS.getText();
//			auditTableName = this.txtexistingAuditTableName.getText();
//			alarmsTableName = this.txtexistingAlarmTableName.getText();
//			
//			//check that data source names are not null.
//			if(newDSName == null || newDSName.length() == 0)
//			{
//				JOptionPane.showMessageDialog(this, "Please enter new datasource name");
//			}
//			else if(oldDSName == null || oldDSName.length()==0 )
//			{
//				JOptionPane.showMessageDialog(this, "Please enter old datasource name");
//			}
//			else
//			{
//				
//					this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//				//make a RPC call to store configuration information.
//				if(rpc.createAndPopulateAuditDB(newDSName, oldDSName, auditTableName, alarmsTableName) == true)
//				{
//					rpc.createPersistenceRecord(newDSName, oldDSName, auditTableName, alarmsTableName);
//					rpc.executeTasksOnce();
//					JOptionPane.showMessageDialog(this, "Configuration saved successfully");
//					this.setCursor(Cursor.getDefaultCursor());
//				}
//				else
//				{
//					JOptionPane.showMessageDialog(this, "There was error in configuration, please try again");
//					this.setCursor(Cursor.getDefaultCursor());
//				}
//				
//			}
//			
//		}
//		else if(command.compareToIgnoreCase(Constants.CMD_CONFIGURE_CANCEL) == 0)
//		{
//			this.txtnewDSName.setText("");
//			this.txtexistingAuditDS.setText("");
//			revalidate();
//		}
		
	}

}
