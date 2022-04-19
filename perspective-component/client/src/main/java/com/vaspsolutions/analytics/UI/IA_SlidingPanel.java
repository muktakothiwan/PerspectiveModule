package com.vaspsolutions.analytics.UI;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;




import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import com.vaspsolutions.analytics.common.Constants;

import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Cursor;
/**
 * Class to create a sliding panel
 * @author YM : 05/15/2015 
 * 
 *
 */
public class IA_SlidingPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	public int noOfPanels;       	 	//number of panels to slide
	public List<JPanel> _panels; 		//list of Panels
	public BasicArrowButton btnPrev;			//Previous button
	public BasicArrowButton btnNext;			//Next button
	public JPanel middlePane;			//Middle Area
	public int selectedIndex;			//to store current selected panel
	public int actNoOfPanels; 			//number of panels user has actually added.
	private JPanel lowerPane;
	private JButton btn0;
	private JButton btn1;
	private JButton btn2;
	private JButton btn3;
	private JButton btn4;
	private JButton btn5;
	private JButton btn6;
	public JButton firstSlideBtn;
	public JButton secondSlideBtn;
	public JButton thirdSlideBtn;
	public JButton forthSlideBtn;
	public JButton fifthSlideBtn;
	public JButton sixthSlideBtn;
	public JButton allSlidesBtn;
	
	ImageIcon newHollow;
	ImageIcon newFilled;
	ImageIcon newAll;
	
	
	public IA_SlidingPanel(int noOfPanels) {
		super();
		
		//set size and background
		//this.setSize(200,100);
		//this.setOpaque(false);
		this.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		
	 
				
		//initialize all the components
		this.noOfPanels = noOfPanels;
		this._panels = new ArrayList<JPanel>(noOfPanels);
		ImageIcon iconPrev = new ImageIcon(getClass().getResource("prev.png"));
		ImageIcon iconNext = new ImageIcon(getClass().getResource("next.png"));
		
		this.selectedIndex = 0;
		actNoOfPanels = 0;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{60, 150, 60};
		gridBagLayout.rowHeights = new int[]{50,50,30};
		gridBagLayout.columnWeights = new double[]{ 0.0, Double.MIN_VALUE,0.0};
		gridBagLayout.rowWeights = new double[]{ 0.4, 0.4,0.2};
		setLayout(gridBagLayout);
	/*	this.btnPrev = new JButton();
		btnPrev.setBackground(Color.DARK_GRAY);
		btnPrev.setIcon(iconPrev);
		btnPrev.setActionCommand(Constants.CMD_SLIDING_PREV);
		btnPrev.addActionListener(this);
		btnPrev.setBorder(new EmptyBorder(1, 1, 1, 1)); */
		this.btnPrev = new BasicArrowButton(BasicArrowButton.WEST,Constants.COLOR_TOP_PANEL_BACKGROUND,Constants.COLOR_TOP_PANEL_BACKGROUND,Constants.COLOR_COMBO_BACKGROUND,Constants.COLOR_TOP_PANEL_BACKGROUND);
//		btnPrev.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		btnPrev.setActionCommand(Constants.CMD_SLIDING_PREV);
		//btnPrev.addActionListener(this);
		btnPrev.setBorder(new EmptyBorder(1, 1, 1, 1));
		btnPrev.setForeground(Constants.COLOR_COMBO_BACKGROUND);
		
		btnPrev.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		GridBagConstraints gbc_btnPrev = new GridBagConstraints();
		gbc_btnPrev.gridheight = 2;
		gbc_btnPrev.fill = GridBagConstraints.BOTH;
		gbc_btnPrev.insets = new Insets(0, 0, 5, 0);
		gbc_btnPrev.gridx = 0;
		gbc_btnPrev.gridy = 0;
		this.add(btnPrev, gbc_btnPrev);
		
		middlePane = new JPanel();
		middlePane.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagConstraints gbc_middlePane = new GridBagConstraints();
		gbc_middlePane.fill = GridBagConstraints.BOTH;
		gbc_middlePane.gridheight = 2;
		gbc_middlePane.insets = new Insets(0, 0, 5, 0);
		gbc_middlePane.gridx = 1;
		gbc_middlePane.gridy = 0;
		this.add(middlePane, gbc_middlePane);
		middlePane.setLayout(new BorderLayout(0, 0));
		
		
		this.btnNext = new BasicArrowButton(BasicArrowButton.EAST,Constants.COLOR_TOP_PANEL_BACKGROUND,Constants.COLOR_TOP_PANEL_BACKGROUND,Constants.COLOR_COMBO_BACKGROUND,Constants.COLOR_TOP_PANEL_BACKGROUND);
		
		btnNext.setForeground(Constants.COLOR_COMBO_BACKGROUND);
		btnNext.setActionCommand(Constants.CMD_SLIDING_NEXT);
		//btnNext.addActionListener(this);
		btnNext.setBorder(new EmptyBorder(1, 1, 1, 1));
		btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnNext.setOpaque(true);
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.gridheight = 2;
		gbc_btnNext.fill = GridBagConstraints.BOTH;
		gbc_btnNext.insets = new Insets(0, 0, 5, 0);
		gbc_btnNext.gridx = 2;
		gbc_btnNext.gridy = 0;
		this.add(btnNext, gbc_btnNext);
		
		
		javax.swing.border.Border emptyBorder = BorderFactory.createEmptyBorder();
		
		lowerPane = new JPanel();
		lowerPane.setPreferredSize(new Dimension(130,10));
		lowerPane.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		GridBagConstraints gbc_lowerPane = new GridBagConstraints();
		gbc_lowerPane.insets = new Insets(0, 0, 0, 5);
		gbc_lowerPane.gridx = 1;
		gbc_lowerPane.gridy = 2;
		add(lowerPane, gbc_lowerPane);
		GridBagLayout gbl_lowerPane = new GridBagLayout();
		gbl_lowerPane.columnWidths = new int[]{5, 5, 5, 5, 5, 5, 5, 0};
		gbl_lowerPane.rowHeights = new int[]{0, 0};
		gbl_lowerPane.columnWeights = new double[]{0.14, 0.14, 0.14, 0.14, 0.14, 0.14, 0.14, Double.MIN_VALUE};
		gbl_lowerPane.rowWeights = new double[]{0.25 , 0.50};
		lowerPane.setLayout(gbl_lowerPane);
		
		firstSlideBtn = new JButton();
		firstSlideBtn.setBorder(emptyBorder);
		firstSlideBtn.setContentAreaFilled(false);
		//firstSlideBtn.setSize(new Dimension(2,2));
		//firstSlideBtn.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		firstSlideBtn.setActionCommand(Constants.COMMAND_FIRST_SLIDE_BTN);
//		firstSlideBtn.setForeground(Constants.COLOR_GREY_LABEL);
		//firstSlideBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_firstSlideBtn = new GridBagConstraints();
		gbc_firstSlideBtn.fill = GridBagConstraints.VERTICAL;
		gbc_firstSlideBtn.insets = new Insets(0, 0, 0, 5);
		gbc_firstSlideBtn.gridx = 0;
		gbc_firstSlideBtn.gridy = 0;
		//firstSlideBtn.addActionListener(this);
		firstSlideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowerPane.add(firstSlideBtn, gbc_firstSlideBtn);
		
		secondSlideBtn = new JButton();
		secondSlideBtn.setBorder(emptyBorder);
		secondSlideBtn.setContentAreaFilled(false);
//		secondSlideBtn.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
//		secondSlideBtn.setForeground(Constants.COLOR_GREY_LABEL);
		secondSlideBtn.setActionCommand(Constants.COMMAND_SECOND_SLIDE_BTN);
//		secondSlideBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_secondSlideBtn = new GridBagConstraints();
		gbc_secondSlideBtn.fill = GridBagConstraints.VERTICAL;
		gbc_secondSlideBtn.insets = new Insets(0, 0, 0, 5);
		gbc_secondSlideBtn.gridx = 1;
		gbc_secondSlideBtn.gridy = 0;
		//secondSlideBtn.addActionListener(this);
		secondSlideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowerPane.add(secondSlideBtn, gbc_secondSlideBtn);
		
		thirdSlideBtn = new JButton();
		thirdSlideBtn.setBorder(emptyBorder);
		thirdSlideBtn.setContentAreaFilled(false);
//		thirdSlideBtn.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
//		thirdSlideBtn.setForeground(Constants.COLOR_GREY_LABEL);
		thirdSlideBtn.setActionCommand(Constants.COMMAND_THIRD_SLIDE_BTN);
//		thirdSlideBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_thirdSlideBtn = new GridBagConstraints();
		gbc_thirdSlideBtn.fill = GridBagConstraints.VERTICAL;
		gbc_thirdSlideBtn.insets = new Insets(0, 0, 0, 5);
		gbc_thirdSlideBtn.gridx = 2;
		gbc_thirdSlideBtn.gridy = 0;
		//thirdSlideBtn.addActionListener(this);
		thirdSlideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowerPane.add(thirdSlideBtn, gbc_thirdSlideBtn);
		
		forthSlideBtn = new JButton();
		forthSlideBtn.setBorder(emptyBorder);
		forthSlideBtn.setContentAreaFilled(false);
	//	forthSlideBtn.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
	//	forthSlideBtn.setForeground(Constants.COLOR_GREY_LABEL);
		forthSlideBtn.setActionCommand(Constants.COMMAND_FOURTH_SLIDE_BTN);
	//	forthSlideBtn.setBorder(emptyBorder);
		
		GridBagConstraints gbc_forthSlideBtn = new GridBagConstraints();
		gbc_forthSlideBtn.fill = GridBagConstraints.VERTICAL;
		gbc_forthSlideBtn.insets = new Insets(0, 0, 0, 5);
		gbc_forthSlideBtn.gridx = 3;
		gbc_forthSlideBtn.gridy = 0;
		//forthSlideBtn.addActionListener(this);
		forthSlideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowerPane.add(forthSlideBtn, gbc_forthSlideBtn);
		
		fifthSlideBtn = new JButton();
		fifthSlideBtn.setBorder(emptyBorder);
		fifthSlideBtn.setContentAreaFilled(false);
	//	fifthSlideBtn.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
	//	fifthSlideBtn.setForeground(Constants.COLOR_GREY_LABEL);
		fifthSlideBtn.setActionCommand(Constants.COMMAND_FIFTH_SLIDE_BTN);
	//	fifthSlideBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_fifthSlideBtn = new GridBagConstraints();
		gbc_fifthSlideBtn.fill = GridBagConstraints.VERTICAL;
		gbc_fifthSlideBtn.insets = new Insets(0, 0, 0, 5);
		gbc_fifthSlideBtn.gridx = 4;
		gbc_fifthSlideBtn.gridy = 0;
		//fifthSlideBtn.addActionListener(this);
		fifthSlideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowerPane.add(fifthSlideBtn, gbc_fifthSlideBtn);
		
		sixthSlideBtn = new JButton();
		sixthSlideBtn.setBorder(emptyBorder);
		sixthSlideBtn.setContentAreaFilled(false);
		//sixthSlideBtn.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		
		sixthSlideBtn.setActionCommand(Constants.COMMAND_SIXTH_SLIDE_BTN);
		//sixthSlideBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_sixthSlideBtn = new GridBagConstraints();
		gbc_sixthSlideBtn.fill = GridBagConstraints.VERTICAL;
		gbc_sixthSlideBtn.insets = new Insets(0, 0, 0, 5);
		gbc_sixthSlideBtn.gridx = 5;
		gbc_sixthSlideBtn.gridy = 0;
		//sixthSlideBtn.addActionListener(this);
		sixthSlideBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowerPane.add(sixthSlideBtn, gbc_sixthSlideBtn);
		
		allSlidesBtn = new JButton("");
		allSlidesBtn.setBackground(Constants.COLOR_TOP_PANEL_BACKGROUND);
		allSlidesBtn.setActionCommand(Constants.COMMAND_ALL_SLIDE_BTN);
		
		allSlidesBtn.setBorder(emptyBorder);
		GridBagConstraints gbc_allSlidesBtn = new GridBagConstraints();
		gbc_allSlidesBtn.fill = GridBagConstraints.VERTICAL;
		gbc_allSlidesBtn.gridx = 6;
		gbc_allSlidesBtn.gridy = 0;
		//allSlidesBtn.addActionListener(this);
		allSlidesBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lowerPane.add(allSlidesBtn, gbc_allSlidesBtn);
		
	/*	btn1 = new JButton("");
		
		btn1.setOpaque(false);
		btn1.setBackground(Color.DARK_GRAY);
		btn1.setIcon(new ImageIcon(IA_SlidingPanel.class.getResource("/com/vasp/ignitionanalytics/UI/projectsMenuIcon.png")));
		GridBagConstraints gbc_btn1 = new GridBagConstraints();
		gbc_btn1.fill = GridBagConstraints.VERTICAL;
		gbc_btn1.insets = new Insets(0, 0, 0, 5);
		gbc_btn1.gridx = 0;
		gbc_btn1.gridy = 0;
		lowerPane.add(btn1, gbc_btn1);
		
		btn0 = new JButton("");
		btn0.setOpaque(true);
		btn0.setBackground(Color.DARK_GRAY);
		btn0.setIcon(new ImageIcon(IA_SlidingPanel.class.getResource("/com/vasp/ignitionanalytics/UI/projectsMenuIcon.png")));
		GridBagConstraints gbc_btn0 = new GridBagConstraints();
		gbc_btn0.fill = GridBagConstraints.VERTICAL;
		gbc_btn0.insets = new Insets(0, 0, 0, 5);
		gbc_btn0.gridx = 1;
		gbc_btn0.gridy = 0;
		lowerPane.add(btn0, gbc_btn0);
		
		btn2 = new JButton("");
		btn2.setBackground(Color.DARK_GRAY);
		btn2.setIcon(new ImageIcon(IA_SlidingPanel.class.getResource("/com/vasp/ignitionanalytics/UI/projectsMenuIcon.png")));
		GridBagConstraints gbc_btn2 = new GridBagConstraints();
		gbc_btn2.fill = GridBagConstraints.VERTICAL;
		gbc_btn2.insets = new Insets(0, 0, 0, 5);
		gbc_btn2.gridx = 2;
		gbc_btn2.gridy = 0;
		lowerPane.add(btn2, gbc_btn2);
		
		btn3 = new JButton("");
		btn3.setBackground(Color.DARK_GRAY);
		btn3.setIcon(new ImageIcon(IA_SlidingPanel.class.getResource("/com/vasp/ignitionanalytics/UI/projectsMenuIcon.png")));
		GridBagConstraints gbc_btn3 = new GridBagConstraints();
		gbc_btn3.fill = GridBagConstraints.VERTICAL;
		gbc_btn3.insets = new Insets(0, 0, 0, 5);
		gbc_btn3.gridx = 3;
		gbc_btn3.gridy = 0;
		lowerPane.add(btn3, gbc_btn3);
		
		btn4 = new JButton("");
		btn4.setBackground(Color.DARK_GRAY);
		btn4.setIcon(new ImageIcon(IA_SlidingPanel.class.getResource("/com/vasp/ignitionanalytics/UI/projectsMenuIcon.png")));
		GridBagConstraints gbc_btn4 = new GridBagConstraints();
		gbc_btn4.fill = GridBagConstraints.VERTICAL;
		gbc_btn4.insets = new Insets(0, 0, 0, 5);
		gbc_btn4.gridx = 4;
		gbc_btn4.gridy = 0;
		lowerPane.add(btn4, gbc_btn4);
		
		btn5 = new JButton("");
		btn5.setBackground(Color.DARK_GRAY);
		btn5.setIcon(new ImageIcon(IA_SlidingPanel.class.getResource("/com/vasp/ignitionanalytics/UI/projectsMenuIcon.png")));
		GridBagConstraints gbc_btn5 = new GridBagConstraints();
		gbc_btn5.fill = GridBagConstraints.VERTICAL;
		gbc_btn5.insets = new Insets(0, 0, 0, 5);
		gbc_btn5.gridx = 5;
		gbc_btn5.gridy = 0;
		lowerPane.add(btn5, gbc_btn5);
		
		btn6 = new JButton("");
		btn6.setBackground(Color.DARK_GRAY);
		btn6.setIcon(new ImageIcon(IA_SlidingPanel.class.getResource("/com/vasp/ignitionanalytics/UI/projectsMenuIcon.png")));
		GridBagConstraints gbc_btn6 = new GridBagConstraints();
		gbc_btn6.fill = GridBagConstraints.VERTICAL;
		gbc_btn6.gridx = 6;
		gbc_btn6.gridy = 0;
		lowerPane.add(btn6, gbc_btn6);
		*/
		
		
			ImageIcon imgHollow = new ImageIcon(getClass().getResource("White-Circle.png"));
			Image img = imgHollow.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
			newHollow = new ImageIcon(img);
			
			ImageIcon imgFilled = new ImageIcon(getClass().getResource("Gray-and-White-Circle.png"));
			Image imgFilled1 = imgFilled.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
			newFilled = new ImageIcon(imgFilled1);
			
			ImageIcon imgAll = new ImageIcon(getClass().getResource("White-Squares.png"));
			Image imageAll = imgAll.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
			newAll = new ImageIcon(imageAll);
			
			
		//firstSlideBtn.setIcon(newFilled);
		secondSlideBtn.setIcon(newHollow);
		thirdSlideBtn.setIcon(newHollow);
		forthSlideBtn.setIcon(newHollow);
		fifthSlideBtn.setIcon(newHollow);
		sixthSlideBtn.setIcon(newHollow);
		allSlidesBtn.setIcon(newAll);
	}
	
	public void removePanels()
	{
		this.noOfPanels = 0;
		actNoOfPanels = 0;
	}
	public void setSelectedPanel(int panelIndex)
	{
		this.selectedIndex = panelIndex;
		this.middlePane.removeAll();
		
		this.middlePane.add(this._panels.get(panelIndex));
		revalidate();
		repaint();
		
	}
	public void addPanelToList (JPanel _panel) throws Exception
	{
		if(actNoOfPanels == this.noOfPanels)
		{
			throw new Exception("Can not add more components");
		}
		else
		{
			//this._panels.set(actNoOfPanels, _panel);
			this._panels.add(actNoOfPanels, _panel);
			actNoOfPanels++;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		
		int indexToShow = 0;
		this.selectedIndex = indexToShow;
		
		this.middlePane.removeAll();
		this.middlePane.add(this._panels.get(indexToShow), BorderLayout.CENTER);
		validate();
		repaint();
		
		
	}
	
	

	

}
