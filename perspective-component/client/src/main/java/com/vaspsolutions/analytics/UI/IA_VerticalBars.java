package com.vaspsolutions.analytics.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.inductiveautomation.factorypmi.application.components.PMIBarChart;


public class IA_VerticalBars extends JPanel{

	private int noOfBars;
	public IA_VerticalBars(int noOfBars)
	{
		super();
		
	
		this.noOfBars = noOfBars;
	}
	
	public void setNoOfBars(int noOfars)
	{
		this.noOfBars = noOfBars;
		repaint();
	}
	@Override
    public void paintComponent(Graphics g) {
       
		this.setSize(100, 10);
		this.setBackground(new Color(215,218,220));
		this.setBorder(new EmptyBorder(1,1,1,1));
		super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        float startX = 0.0f;
        float startY = 2.0f;
        float endX = startX;
        float endY = 9.0f;
        
        g2.setStroke(new BasicStroke(5));
        g2.setPaint(Color.LIGHT_GRAY);
        
        int drawnsegments = 0;
        boolean firstTwo = true;
      /*  if(this.noOfBars > 10)
        {
        	this.noOfBars = 10;
        }*/
        for(int i=0; i< this.noOfBars; i++)
        {
        	if(drawnsegments == 2)
        	{
        		drawnsegments = 0;
        		if(firstTwo == false)
        		{
        			firstTwo = true;
        		}
        		else
        		{
        			firstTwo = false;
        		}
        	}
        	if(firstTwo)
        	{
        		  g2.setPaint(new Color(116, 180, 218));
        	}
        	else
        	{
        		g2.setPaint(Color.gray);
        	}
             Line2D verticalLine = new Line2D.Float(startX, startY, endX, endY ); 
             g2.draw(verticalLine);
             
             startX = startX + 7.0f;
             endX = startX;
             
             drawnsegments = drawnsegments + 1;
        }
       
    }
}
