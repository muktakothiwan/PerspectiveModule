package com.vaspsolutions.analytics.UI;

import java.awt.Color;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import java.awt.GridLayout;

import javax.swing.SwingConstants;
import javax.swing.JList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicProgressBarUI;



























import com.inductiveautomation.factorypmi.application.components.PMITreeView;
import com.inductiveautomation.factorypmi.application.components.tabstrip.PMITabStrip;
import com.vaspsolutions.analytics.client.IgnitionAnalyticsComponent;
import com.vaspsolutions.analytics.common.Constants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
/**
 * Class to hold left menu panel information.
 * @author YM : Created on 05/15/2015
 * 
 *
 */
public class IA_LeftMenuPanel extends JPanel implements ActionListener, MouseListener{

	/*JLabel logo;
	JComboBox<String> dropDownMenu;
	*/

	BufferedImage  image;
	Image img1;
	
	public MenuButton btnRealTime;
	public MenuButton btnUsers;
	public MenuButton btnReports; 
	public MenuButton btnProjects;
	public MenuButton btnDashboard;
	public MenuButton btnLogout;
	public JComboBox<String> comboProjects;
	private JLabel label;
	private JLabel label_1;
	private JLabel label_2;
	private JLabel label_3;
	private JLabel label_5;
	public String[] projects;
	private JLabel label_6;
	private JLabel label_7;
	private JPanel panel;
	private JLabel ignitionLogoLbl;
	public JLabel lblUserNameLbl;
	public JLabel lblDateTimeLbl;
	public IA_LeftMenuPanel() {
		super();
		
		try {
			image = ImageIO.read(getClass().getResource("backGround.png"));
			img1 = image.getScaledInstance(200, 1080, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// this.setOpaque(false);
		 this.setPreferredSize(new Dimension(200,1080));
		// this.setSize(new Dimension(200,1080));
		 this.setBackground(Constants.COLOR_MENU_BACKGROUND);
		// this.setBackground(Color.WHITE);
		
		 GridBagLayout gridBagLayout = new GridBagLayout();
		 gridBagLayout.columnWidths = new int[]{200};
		 gridBagLayout.rowHeights = new int[]{30,30, 30, 15, 30, 15, 30, 15, 30, 15, 30, 30, 21, 21, 21, 21, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 30,30,0};
		 gridBagLayout.columnWeights = new double[]{1.0};
		 gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		 setLayout(gridBagLayout);
		  
		 
		 ImageIcon logoIcon = new ImageIcon(getClass().getResource("Ignition-Analytics-Logo.png"));
		 
		 //Changed the logo size as per Chris , QA doc dated 10-Oct-2016
		  Image logoImage1 = logoIcon.getImage().getScaledInstance(165, 30, Image.SCALE_SMOOTH);
		    ImageIcon logoPlaced = new ImageIcon(logoImage1);
		    ignitionLogoLbl = new JLabel("");
		    GridBagConstraints gbc_ignitionLogoLbl = new GridBagConstraints();
		    gbc_ignitionLogoLbl.fill = GridBagConstraints.BOTH;
		    gbc_ignitionLogoLbl.insets = new Insets(5, 15, 5, 0);
		    gbc_ignitionLogoLbl.gridx = 0;
		    gbc_ignitionLogoLbl.gridy = 0;
		    ignitionLogoLbl.setIcon(logoPlaced);
		    add(ignitionLogoLbl, gbc_ignitionLogoLbl);
		 // projects = new String[] {"Project 1", "Project 2", "Project 3"};
		    comboProjects = new JComboBox<String>();
		    comboProjects.setPreferredSize(new Dimension(180,21));
		    comboProjects.setForeground(Color.DARK_GRAY);
		    comboProjects.setBackground(Constants.COLOR_COMBO_BACKGROUND);
		    comboProjects.setOpaque(true);
		    comboProjects.setFocusable(false);
		  
		// comboProjects.setModel(new DefaultComboBoxModel(projects));
		    
		    comboProjects.setUI(new ComboArrowUI());
		   // comboProjects.setBorder(BorderFactory.createLineBorder(Constants.COLOR_COMBO_BACKGROUND, 1, true));
		    GridBagConstraints gbc_comboProjects = new GridBagConstraints();
		    gbc_comboProjects.fill = GridBagConstraints.BOTH;
		    gbc_comboProjects.insets = new Insets(5, 5, 5, 0);
		    gbc_comboProjects.gridx = 0;
		    gbc_comboProjects.gridy = 1;
		    add(comboProjects, gbc_comboProjects);
		  
		   btnProjects = new MenuButton("");
		   ImageIcon origIcon;
		   Image newImage;
		   origIcon = new ImageIcon(getClass().getResource("Projects--Navigation.png"));
		   newImage = origIcon.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH);
		   btnProjects.setIcon(new ImageIcon(newImage));
		   btnProjects.setMargin(new Insets(0,0,0,0));
		   btnProjects.setLayout(new BorderLayout());
		   JLabel label_8 = new JLabel("                     Projects" );
		   label_8.setForeground(Color.WHITE);
		   btnProjects.add(label_8, BorderLayout.WEST);
//		   btnProjects.setHorizontalTextPosition(AbstractButton.RIGHT);
//		   btnProjects.setVerticalTextPosition(AbstractButton.CENTER);
//		   btnProjects.setIconTextGap(0);
		   GridBagConstraints gbc_btnProjects = new GridBagConstraints();
		   gbc_btnProjects.fill = GridBagConstraints.BOTH;
		   gbc_btnProjects.insets = new Insets(0, 0, 5, 0);
		   gbc_btnProjects.gridx = 0;
		   gbc_btnProjects.gridy = 2;
		   add(btnProjects, gbc_btnProjects);
		 
		  btnDashboard = new MenuButton("");
		  ImageIcon origIcon1 = new ImageIcon(getClass().getResource("Dashboard--Navigation.png"));
		  Image newImage1 = origIcon1.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH);
		  btnDashboard.setIcon(new ImageIcon(newImage1));
		  btnDashboard.setMargin(new Insets(0,0,0,0));
		  
		  btnDashboard.setLayout(new BorderLayout());
		  JLabel label_9 = new JLabel("                     Dashboard" );
		  label_9.setForeground(Color.WHITE);
		  btnDashboard.add(label_9,BorderLayout.WEST);
//		  btnDashboard.setHorizontalAlignment(AbstractButton.LEADING);
//		  btnDashboard.setHorizontalTextPosition(AbstractButton.RIGHT);
//		  btnDashboard.setVerticalTextPosition(AbstractButton.CENTER);
//		  btnDashboard.setIconTextGap(0);
		  GridBagConstraints gbc_btnDashboard = new GridBagConstraints();
		  gbc_btnDashboard.fill = GridBagConstraints.BOTH;
		  gbc_btnDashboard.insets = new Insets(0, 0, 5, 0);
		  gbc_btnDashboard.gridx = 0;
		  gbc_btnDashboard.gridy = 4;
		  add(btnDashboard, gbc_btnDashboard);
		  
		  btnRealTime = new MenuButton("");
		  ImageIcon origIcon2 = new ImageIcon(getClass().getResource("Rel-Time--Navigation.png"));
		  Image newImage2 = origIcon2.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH);
		  btnRealTime.setIcon(new ImageIcon(newImage2));
		  btnRealTime.setMargin(new Insets(0,0,0,0));
		  btnRealTime.setLayout(new BorderLayout());
		  JLabel label_10 = new JLabel("                     Real Time" );
		  label_10.setForeground(Color.WHITE);
		  btnRealTime.add(label_10, BorderLayout.WEST);
//		  btnRealTime.setHorizontalTextPosition(AbstractButton.RIGHT);
//		  btnRealTime.setVerticalTextPosition(AbstractButton.CENTER);
//		  btnRealTime.setIconTextGap(0);
		  GridBagConstraints gbc_btnRealTime = new GridBagConstraints();
		  gbc_btnRealTime.fill = GridBagConstraints.BOTH;
		  gbc_btnRealTime.insets = new Insets(0, 0, 5, 0);
		  gbc_btnRealTime.gridx = 0;
		  gbc_btnRealTime.gridy = 6;
		  add(btnRealTime, gbc_btnRealTime);
		 
		  btnUsers = new MenuButton("");
		  
		  ImageIcon origIcon3 = new ImageIcon(getClass().getResource("Users--Navigation.png"));
		  Image newImage3 = origIcon3.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH);
		  btnUsers.setIcon(new ImageIcon(newImage3));
		  btnUsers.setMargin(new Insets(0,0,0,0));
		  btnUsers.setLayout(new BorderLayout());
		  JLabel label_11 = new JLabel("                     Users" );
		  label_11.setForeground(Color.WHITE);
		  btnUsers.add(label_11, BorderLayout.WEST);
//		  btnUsers.setHorizontalTextPosition(AbstractButton.RIGHT);
//		  btnUsers.setVerticalTextPosition(AbstractButton.CENTER);
//		  btnUsers.setIconTextGap(0);
		  GridBagConstraints gbc_btnUsers = new GridBagConstraints();
		  gbc_btnUsers.fill = GridBagConstraints.BOTH;
		  gbc_btnUsers.insets = new Insets(0, 0, 5, 0);
		  gbc_btnUsers.gridx = 0;
		  gbc_btnUsers.gridy = 8;
		  add(btnUsers, gbc_btnUsers);
		 
		  btnReports = new MenuButton("");
		  ImageIcon origIcon4 = new ImageIcon(getClass().getResource("Reports--Navigation.png"));
		  Image newImage4 = origIcon4.getImage().getScaledInstance(200, 30, Image.SCALE_SMOOTH);
		  btnReports.setIcon(new ImageIcon(newImage4));
		  btnReports.setMargin(new Insets(0,0,0,0));
		  btnReports.setLayout(new BorderLayout());
		  JLabel label_12 = new JLabel("                     Reports" );
		  label_12.setForeground(Color.WHITE);
		  btnReports.add(label_12, BorderLayout.WEST);
//		  btnReports.setHorizontalTextPosition(AbstractButton.RIGHT);
//		  btnReports.setVerticalTextPosition(AbstractButton.CENTER);
//		  btnReports.setIconTextGap(0);
		  // btnReports.addActionListener(this);
		  
		    GridBagConstraints gbc_btnReports = new GridBagConstraints();
		    gbc_btnReports.fill = GridBagConstraints.BOTH;
		    gbc_btnReports.insets = new Insets(0, 0, 5, 0);
		    gbc_btnReports.gridx = 0;
		    gbc_btnReports.gridy = 10;
		    add(btnReports, gbc_btnReports);
		 
		 
		 label = new JLabel("");
		 GridBagConstraints gbc_label = new GridBagConstraints();
		 gbc_label.fill = GridBagConstraints.BOTH;
		 gbc_label.insets = new Insets(0, 0, 5, 0);
		 gbc_label.gridx = 0;
		 gbc_label.gridy = 11;
		 add(label, gbc_label);
		 
		 label_1 = new JLabel("");
		 GridBagConstraints gbc_label_1 = new GridBagConstraints();
		 gbc_label_1.fill = GridBagConstraints.BOTH;
		 gbc_label_1.insets = new Insets(0, 0, 5, 0);
		 gbc_label_1.gridx = 0;
		 gbc_label_1.gridy = 12;
		 add(label_1, gbc_label_1);
		 
		 label_2 = new JLabel("");
		 GridBagConstraints gbc_label_2 = new GridBagConstraints();
		 gbc_label_2.fill = GridBagConstraints.BOTH;
		 gbc_label_2.insets = new Insets(0, 0, 5, 0);
		 gbc_label_2.gridx = 0;
		 gbc_label_2.gridy = 13;
		 add(label_2, gbc_label_2);
		   
		   label_5 = new JLabel("");
		   GridBagConstraints gbc_label_5 = new GridBagConstraints();
		   gbc_label_5.fill = GridBagConstraints.BOTH;
		   gbc_label_5.insets = new Insets(0, 0, 5, 0);
		   gbc_label_5.gridx = 0;
		   gbc_label_5.gridy = 14;
		   add(label_5, gbc_label_5);
		              
		              label_6 = new JLabel("");
		              GridBagConstraints gbc_label_6 = new GridBagConstraints();
		              gbc_label_6.insets = new Insets(0, 0, 5, 0);
		              gbc_label_6.gridx = 0;
		              gbc_label_6.gridy = 15;
		              add(label_6, gbc_label_6);
		              
		              label_7 = new JLabel("");
		              GridBagConstraints gbc_label_7 = new GridBagConstraints();
		              gbc_label_7.insets = new Insets(0, 0, 5, 0);
		              gbc_label_7.gridx = 0;
		              gbc_label_7.gridy = 16;
		              add(label_7, gbc_label_7);
		              
		              panel = new JPanel();
		              panel.setOpaque(false);
		              GridBagConstraints gbc_panel = new GridBagConstraints();
		              gbc_panel.insets = new Insets(0, 0, 5, 0);
		              gbc_panel.fill = GridBagConstraints.BOTH;
		              gbc_panel.gridx = 0;
		              gbc_panel.gridy = 17;
		              gbc_panel.gridheight = 9;
		              add(panel, gbc_panel);
		               
		               
		                 
		                 lblUserNameLbl = new JLabel(" User : ");
		                 lblUserNameLbl.setForeground(Color.WHITE);
		                 GridBagConstraints gbc_lblUserNameLbl = new GridBagConstraints();
		                 gbc_lblUserNameLbl.fill = GridBagConstraints.VERTICAL;
		                 gbc_lblUserNameLbl.anchor = GridBagConstraints.WEST;
		                 gbc_lblUserNameLbl.insets = new Insets(5, 10, 5, 0);
		                 gbc_lblUserNameLbl.gridx = 0;
		                 gbc_lblUserNameLbl.gridy = 26;
		                 add(lblUserNameLbl, gbc_lblUserNameLbl);
		                 
		                 lblDateTimeLbl = new JLabel(" Time : ");
		                 lblDateTimeLbl.setForeground(Color.WHITE);
		                 GridBagConstraints gbc_lblDateTimeLbl = new GridBagConstraints();
		                 gbc_lblDateTimeLbl.fill = GridBagConstraints.VERTICAL;
		                 gbc_lblDateTimeLbl.anchor = GridBagConstraints.WEST;
		                 gbc_lblDateTimeLbl.insets = new Insets(5, 10, 5, 0);
		                 gbc_lblDateTimeLbl.gridx = 0;
		                 gbc_lblDateTimeLbl.gridy = 27;
		                 add(lblDateTimeLbl, gbc_lblDateTimeLbl);
		               
		                 btnLogout = new MenuButton("[Logout]");
			               btnLogout.addActionListener(this);
		                 GridBagConstraints gbc_btnLogout = new GridBagConstraints();
		                 gbc_btnLogout.fill = GridBagConstraints.BOTH;
		                 gbc_btnLogout.gridx = 0;
		                 gbc_btnLogout.gridy = 28;
		                 add(btnLogout, gbc_btnLogout);
		
		
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			if(arg0.getActionCommand().compareToIgnoreCase("ProjectsAction") == 0)
			{
				
				
			}
			else
			{
				btnProjects.setIcon(null);
			}
			if(arg0.getActionCommand().compareToIgnoreCase("RemoveProject") == 0)
			{
			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/***
	 *  
	 * Following method captures the remove button click on Projects Table
	 *
	 * @param arg0
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
		/*Point p = arg0.getPoint();
		int row = projectsList.rowAtPoint(p);
		int col = projectsList.columnAtPoint(p);
		int warningDialogOptions = JOptionPane.YES_NO_OPTION;
		int noOfProjects = projects.length;
		String[] newList = new String[]{};
		int i = 0;
		if(col == 6)
		{
			int result = JOptionPane.showConfirmDialog(this, "are you sure you want to remove project ?",
					"Warning", warningDialogOptions);
			
			if(result == JOptionPane.YES_OPTION)
			{
				//reconstruct the table and the projects list
				
				createProjectsList();
			}
			
		}*/
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		
		g.drawImage(img1, 0, 0, this);
	}
	
	

	
}
