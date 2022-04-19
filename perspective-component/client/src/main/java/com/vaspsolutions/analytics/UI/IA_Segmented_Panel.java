package com.vaspsolutions.analytics.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.vaspsolutions.analytics.common.Constants;
/**
 * A UI To show Alarm segments graph
 * @author YM
 *
 */
public class IA_Segmented_Panel extends JPanel{
	public IA_Segmented_Panel() {
		setBorder(null);
		this.setBackground(new Color(224, 255, 255));
		setLayout(new BorderLayout(0, 0));
	}
	private int noOfAlarms = 0;
	private int noOfSegmentsToDraw = 0;
	private String stringText = "";
	public void setNumberOfSegments(int no)
	{
		
		this.noOfSegmentsToDraw = no;
		repaint();
	}
	@Override protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);
//		this.noOfSegmentsToDraw = 40;
//		this.noOfAlarms = 400;
//		this.stringText = "Active Alarms";
		Graphics2D g2D = (Graphics2D)g;
	    g2D.setStroke(new BasicStroke(1.5f));
	    g2D.setPaint(new Color(213, 108, 15));
		int i =0;
		int initW = 160;
		int initH = 110;
		int w = initW;
	    int h = initH + 5;
	    int diametere = 90;
	    int varLength = 0;
	    if(noOfSegmentsToDraw > 0)
	    {
	    	int noOfFullCircles = noOfSegmentsToDraw/8;
	    	int noOfSegmentsInLastCircle = noOfSegmentsToDraw % 8;
		
	    	for(i=0; i<noOfFullCircles; i++)
	    	{
				Arc2D arc1 = new Arc2D.Double(w, h, diametere, diametere, 0.0, 35.0, Arc2D.OPEN);
			    Arc2D arc1_1 = new Arc2D.Double(w, h, diametere, diametere, 45.0, 35.0, Arc2D.OPEN);
			    Arc2D arc2 = new Arc2D.Double(w, h, diametere, diametere, 90.0, 35.0, Arc2D.OPEN);
			    Arc2D arc2_1 = new Arc2D.Double(w, h, diametere, diametere, 135.0, 35.0, Arc2D.OPEN);
			    Arc2D arc3 = new Arc2D.Double(w, h, diametere, diametere, 180.0, 35.0, Arc2D.OPEN);
			    Arc2D arc3_1 = new Arc2D.Double(w, h, diametere, diametere, 225.0, 35.0, Arc2D.OPEN);
			    Arc2D arc4 = new Arc2D.Double(w, h, diametere, diametere, 270.0, 35.0, Arc2D.OPEN);
			    Arc2D arc4_1 = new Arc2D.Double(w, h, diametere, diametere, 315.0, 35.0, Arc2D.OPEN);
			    
			    /*w = w-5;
			    h = h - 5;
			    diametere = diametere + 10;
			    Arc2D arc5 = new Arc2D.Double(w, h, diametere, diametere, 0.0, 35.0, Arc2D.OPEN);
			    Arc2D arc5_1 = new Arc2D.Double(w, h, diametere, diametere, 45.0, 35.0, Arc2D.OPEN);
				  
			    Arc2D arc6 = new Arc2D.Double(w , h , diametere, diametere, 90.0, 35.0, Arc2D.OPEN);
			    Arc2D arc6_1 = new Arc2D.Double(w , h , diametere, diametere, 135.0, 35.0, Arc2D.OPEN);
				   
			    Arc2D arc7 = new Arc2D.Double(w, h , diametere, diametere, 180.0, 35.0, Arc2D.OPEN);
			    Arc2D arc7_1 = new Arc2D.Double(w, h , diametere, diametere, 225.0, 35.0, Arc2D.OPEN);
				   
			    Arc2D arc8 = new Arc2D.Double(w, h  , diametere, diametere, 270.0, 35.0, Arc2D.OPEN);
			    Arc2D arc8_1 = new Arc2D.Double(w, h  , diametere, diametere, 315.0, 35.0, Arc2D.OPEN);
				
			  */  
			    g2D.draw(arc1);
			    g2D.draw(arc1_1);
			    g2D.draw(arc2);
			    g2D.draw(arc2_1);
			    g2D.draw(arc3);
			    g2D.draw(arc3_1);
			    g2D.draw(arc4);
			    g2D.draw(arc4_1);
			  
			    w = w - 5;
			    h = h - 5;
			    diametere = diametere + 10;
			    /*g2D.draw(arc5);
			    g2D.draw(arc5_1);
			    g2D.draw(arc6);
			    g2D.draw(arc6_1);
			    g2D.draw(arc7);
			    g2D.draw(arc7_1);
			    g2D.draw(arc8);
			    g2D.draw(arc8_1);*/
	    	}
		
	    	if(noOfSegmentsInLastCircle > 0)
	    	{
				int j=0;
				float startAngle = 0.0f;
				Arc2D arc ;
				for(j=0; j<noOfSegmentsInLastCircle; j++)
				{
					arc = new Arc2D.Double(w, h, diametere, diametere, startAngle, 35.0, Arc2D.OPEN);
					startAngle = startAngle + 45.0f;
					g2D.draw(arc);
				}
	    	}
	    	
	    	
	    	
	    }
	    if (this.noOfAlarms < 10)
	    	varLength = 35;
	    else
	    if(this.noOfAlarms >=10 && this.noOfAlarms <= 99)
	    	varLength = 26;
	    if(this.noOfAlarms >= 100 && noOfAlarms <= 999)
	    	varLength = 18;
	    else
	    if (this.noOfAlarms > 1000)
	    	varLength = 10 ;
	    
	   
	    		
	    g2D.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
	    g2D.setStroke(new BasicStroke(1.5f));
 	    g2D.setPaint(Constants.COLOR_BLUE_LABEL);
    	g2D.drawString(""+this.noOfAlarms, initW + varLength , initH + 50);
    	 g2D.setStroke(new BasicStroke(1.5f));
    	 g2D.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
	 	    g2D.setPaint(Constants.COLOR_BLACK_TEXT);
	 	    if(this.stringText.compareToIgnoreCase("Active Alarms") == 0)
	 	    {
	 	    	g2D.drawString(this.stringText, initW + 10  , initH + 65);
	 	    }
	 	    else
	 	    {
	 	    	g2D.drawString(this.stringText, initW + 15  , initH + 65);
	 	    }
	}
	/**
	 * @return the noOfAlarms
	 */
	public int getNoOfAlarms() {
		return noOfAlarms;
	}
	/**
	 * @param noOfAlarms the noOfAlarms to set
	 */
	public void setNoOfAlarms(int noOfAlarms) {
		this.noOfAlarms = noOfAlarms;
	}
	/**
	 * @return the stringText
	 */
	public String getStringText() {
		return stringText;
	}
	/**
	 * @param stringText the stringText to set
	 */
	public void setStringText(String stringText) {
		this.stringText = stringText;
	}
	
}
