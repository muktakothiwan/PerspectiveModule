package com.vaspsolutions.analytics.UI;

import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import com.vaspsolutions.analytics.common.Constants;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.RenderingHints;

import javax.swing.SwingConstants;

public class IA_SlidePanelElement extends JPanel {
	public JLabel lblPercent;
	public JLabel lblTitle ;
	public JLabel lblBottom;
	public IA_SlidePanelElement() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{56};
		gridBagLayout.rowHeights = new int[]{12, 12, 12, 12};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.25, 0.25, 0.25, 0.25};
		setLayout(gridBagLayout);
		//this.setOpaque(false);
		this.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		lblPercent = new JLabel("25%");
		lblPercent.setFont(new Font("SansSerif", Font.BOLD, 40));
		lblPercent.setForeground(Color.WHITE);
		
		GridBagConstraints gbc_lblPercent = new GridBagConstraints();
		gbc_lblPercent.anchor = GridBagConstraints.SOUTH;
		gbc_lblPercent.insets = new Insets(0, 0, 5, 0);
		gbc_lblPercent.gridx = 0;
		gbc_lblPercent.gridy = 1;
		add(lblPercent, gbc_lblPercent);
		
		lblTitle = new JLabel("Bounce Rate");
		lblTitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblBounceRAte = new GridBagConstraints();
		gbc_lblBounceRAte.insets = new Insets(0, 0, 5, 0);
		gbc_lblBounceRAte.gridx = 0;
		gbc_lblBounceRAte.gridy = 2;
		add(lblTitle, gbc_lblBounceRAte);
		
		lblBottom = new JLabel("");
		lblBottom.setHorizontalAlignment(SwingConstants.CENTER);
		lblBottom.setBackground(Constants.COLOR_SLIDE_PANE_GREEN);
		lblBottom.setOpaque(true);
		lblBottom.setForeground(Color.WHITE);
		GridBagConstraints gbc_lblBottom = new GridBagConstraints();
		gbc_lblBottom.fill = GridBagConstraints.BOTH;
		gbc_lblBottom.gridx = 0;
		gbc_lblBottom.gridy = 3;
		add(lblBottom, gbc_lblBottom);
	}
	@Override
	public void paint(Graphics g) {
		
	
		Graphics2D g2d = (Graphics2D)g;
		 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
			super.paint(g2d);
	}

}
