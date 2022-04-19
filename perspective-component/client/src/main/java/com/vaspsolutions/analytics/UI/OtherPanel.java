package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.JFreeChart;

import com.inductiveautomation.ignition.client.util.gui.table.DatasetTableModel;
import com.inductiveautomation.ignition.common.Dataset;

public class OtherPanel extends JPanel {

	private int num_1dayActiveUsers;
	private IA_Label lbl_1dayActiveUsers;
	private OrangeText val_1dayActiveUsers;
	
	private int num_7dayActiveUsers;
	private IA_Label lbl_7dayActiveUsers;
	private OrangeText val_7dayActiveUsers;
	
	private int num_14dayActiveUsers;
	private IA_Label lbl_14dayActiveUsers;
	private OrangeText val_14dayActiveUsers;	
	
	
//	private List<String> userLastSessions; //Number of days since last session for each user
	private Dataset userLastSessions;
	private IA_Label lbluserLastSessions; //Number of days since last session for each user
	//private HistoryTable actuserLastSessions; //Number of days since last session for each user
	private DataTable actuserLastSessions;
	
	
	public OtherPanel() {
		super();
		this.setSize(400,100);
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
		
		GridLayout _gl = new GridLayout(0,2);
		_gl.setVgap(5);
		
		setLayout(_gl);
		
		//this.userLastSessions = new ArrayList<String>();
		
		lbluserLastSessions = new IA_Label(); 
		lbluserLastSessions.setText("Number of days since last session for each user");
		//actuserLastSessions = new HistoryTable(Constants.TABLE_USERS_DURATION); //Number of days since last session for each user
		actuserLastSessions = new DataTable(); //Number of days since last session for each user
		
		this.num_1dayActiveUsers = 0;
		lbl_1dayActiveUsers = new IA_Label();
		lbl_1dayActiveUsers.setText("Number of Active Users since 1 day : ");
		val_1dayActiveUsers = new OrangeText();
		
		this.num_7dayActiveUsers = 0;
		lbl_7dayActiveUsers = new IA_Label();
		lbl_7dayActiveUsers.setText("Number of Active Users since 7 days : ");
		val_7dayActiveUsers = new OrangeText();
		
		this.num_14dayActiveUsers = 0;
		lbl_14dayActiveUsers = new IA_Label();
		lbl_14dayActiveUsers.setText("Number of Active Users since 14 days : ");
		val_14dayActiveUsers = new OrangeText();
		

		add(lbl_1dayActiveUsers);
		add(val_1dayActiveUsers);
		add(lbl_7dayActiveUsers);
		add(val_7dayActiveUsers);
		add(lbl_14dayActiveUsers);
		add(val_14dayActiveUsers);
		add(lbluserLastSessions);
		//add(actuserLastSessions);
		actuserLastSessions.setAutoCreateRowSorter(true);
		actuserLastSessions.setSize(10, 30);
		add(new JScrollPane(actuserLastSessions));
		
	}
	
	public Dataset getUserLastSessions() {
		return userLastSessions;
	}




	public void setUserLastSessions(Dataset userLastSessions) {
		this.userLastSessions = userLastSessions;
		//add data in the table
			/*	DefaultTableModel newData = (DefaultTableModel)actuserLastSessions.getModel();
				String uName = "";
				String sessionCnt = "";
				int noOfRecords = userLastSessions.size();
				int i= 0;
				for(i=0; i<noOfRecords; i++)
				{
					uName = userLastSessions.get(i).split(",")[0];
					sessionCnt = userLastSessions.get(i).split(",")[1];
					
					newData.addRow(new Object[] {uName, sessionCnt});
				}
			
				actuserLastSessions.setModel(newData); */
		DatasetTableModel model = (DatasetTableModel)actuserLastSessions.getModel();
		model.setDataset(userLastSessions);
		actuserLastSessions.setModel(model);
	}

	public int getNum_1dayActiveUsers() {
		return num_1dayActiveUsers;
	}

	public void setNum_1dayActiveUsers(int num_1dayActiveUsers) {
		this.num_1dayActiveUsers = num_1dayActiveUsers;
		this.val_1dayActiveUsers.setText("" + num_1dayActiveUsers);
	}

	public int getNum_14dayActiveUsers() {
		return num_14dayActiveUsers;
		
	}

	public void setNum_14dayActiveUsers(int num_14dayActiveUsers) {
		this.num_14dayActiveUsers = num_14dayActiveUsers;
		this.val_14dayActiveUsers.setText("" + num_14dayActiveUsers);
	}

	public int getNum_7dayActiveUsers() {
		return num_7dayActiveUsers;
	}

	public void setNum_7dayActiveUsers(int num_7dayActiveUsers) {
		this.num_7dayActiveUsers = num_7dayActiveUsers;
		this.val_7dayActiveUsers.setText("" + num_7dayActiveUsers);
	}

}
