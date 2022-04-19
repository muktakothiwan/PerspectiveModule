package com.vaspsolutions.analytics.UI;


import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.vaspsolutions.analytics.common.Constants;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class CombinedTopSlides extends JPanel {
	public IA_SlidePanelElement _slide1;
	public IA_SlidePanelElement _slide2;
	public IA_SlidePanelElement _slide3;
	public IA_SlidePanelElement _slide4;
	public IA_SlidePanelElement _slide5;
	public IA_SlidePanelElement _slide6;
	
	public CombinedTopSlides() {
		
		this.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{90,90,100};
		gridBagLayout.rowHeights = new int[]{80, 80};
		gridBagLayout.columnWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		//this.setPreferredSize(new Dimension(170,100));
	
		
		_slide1 = new IA_SlidePanelElement();
		
		_slide1.lblTitle.setText("Total Visits");
		
		_slide1.lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
		
		
		_slide1.lblPercent.setFont(new Font("SansSerif", Font.BOLD, 15));
		
		GridBagConstraints gbc__slide1 = new GridBagConstraints();
		gbc__slide1.fill = GridBagConstraints.BOTH;
		gbc__slide1.insets = new Insets(0, 0, 5, 5);
		gbc__slide1.gridx = 0;
		gbc__slide1.gridy = 0;
		this.add(_slide1, gbc__slide1);
		_slide2 = new IA_SlidePanelElement();
		_slide2.lblTitle.setText("Total Users");
		_slide2.lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
		_slide2.lblPercent.setFont(new Font("SansSerif", Font.BOLD, 15));
		GridBagConstraints gbc__slide2 = new GridBagConstraints();
		gbc__slide2.fill = GridBagConstraints.BOTH;
		gbc__slide2.insets = new Insets(0, 0, 5, 5);
		gbc__slide2.gridx = 1;
		gbc__slide2.gridy = 0;
		this.add(_slide2, gbc__slide2);
		_slide3 = new IA_SlidePanelElement();
		_slide3.lblTitle.setText("Total Screenviews");
		_slide3.lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
		_slide3.lblPercent.setFont(new Font("SansSerif", Font.BOLD, 15));
		GridBagConstraints gbc__slide3 = new GridBagConstraints();
		gbc__slide3.fill = GridBagConstraints.BOTH;
		gbc__slide3.insets = new Insets(0, 0, 5, 0);
		gbc__slide3.gridx = 2;
		gbc__slide3.gridy = 0;
		this.add(_slide3, gbc__slide3);
		_slide4 = new IA_SlidePanelElement();
		_slide4.lblTitle.setText("Bounce Rate");
		_slide4.lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
		_slide4.lblPercent.setFont(new Font("SansSerif", Font.BOLD, 15));
		GridBagConstraints gbc__slide4 = new GridBagConstraints();
		gbc__slide4.fill = GridBagConstraints.BOTH;
		gbc__slide4.insets = new Insets(0, 0, 0, 5);
		gbc__slide4.gridx = 0;
		gbc__slide4.gridy = 1;
		this.add(_slide4, gbc__slide4);
		_slide5 = new IA_SlidePanelElement();
		_slide5.lblTitle.setText("Avg Session");
		_slide5.lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
		_slide5.lblPercent.setFont(new Font("SansSerif", Font.BOLD, 15));
		GridBagConstraints gbc__slide5 = new GridBagConstraints();
		gbc__slide5.fill = GridBagConstraints.BOTH;
		gbc__slide5.insets = new Insets(0, 0, 0, 5);
		gbc__slide5.gridx = 1;
		gbc__slide5.gridy = 1;
		this.add(_slide5, gbc__slide5);
		_slide6 = new IA_SlidePanelElement();
		_slide6.lblTitle.setText("Avg Screens/Visit");
		_slide6.lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
		_slide6.lblPercent.setFont(new Font("SansSerif", Font.BOLD, 15));
		GridBagConstraints gbc__slide6 = new GridBagConstraints();
		gbc__slide6.fill = GridBagConstraints.BOTH;
		gbc__slide6.gridx = 2;
		gbc__slide6.gridy = 1;
		this.add(_slide6, gbc__slide6);
		
	}

}
