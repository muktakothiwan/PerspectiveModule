package com.vaspsolutions.analytics.UI;

import java.awt.Color;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;


import com.inductiveautomation.plaf.TreeCellRenderer;
import com.vaspsolutions.analytics.common.Constants;

/*
 * Added by YM : 28-May-2019
 * This is to handle customization of reports menu tree 
 */
public class ReportsMenuTreeRenderer extends TreeCellRenderer {

	@Override
	public Icon getDefaultOpenIcon() {
		// TODO Auto-generated method stub
		//return new ImageIcon(getClass().getResource("treeNodeOpen.png"));
		return null;
	}

	@Override
	public Icon getDefaultClosedIcon() {
		// TODO Auto-generated method stub
		//return new ImageIcon(getClass().getResource("treeNodeClose.png"));
		return null;
	}

	@Override
	public Icon getClosedIcon() {
		// TODO Auto-generated method stub
		return getDefaultClosedIcon();
	}

	@Override
	public Icon getDefaultLeafIcon() {
		ImageIcon treeNodeIcon = new ImageIcon(getClass().getResource("treenode.png"));
		Image img = treeNodeIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
		treeNodeIcon = new ImageIcon(img);
		return treeNodeIcon;
	}

	@Override
	public Icon getOpenIcon() {
		// TODO Auto-generated method stub
		return getDefaultOpenIcon();
	}

	@Override
	public Icon getLeafIcon() {
		// TODO Auto-generated method stub
		return getDefaultLeafIcon();
	}

	@Override
	public Color getTextSelectionColor() {
		// TODO Auto-generated method stub
		return Color.white;
	}

	@Override
	public Color getTextNonSelectionColor() {
		// TODO Auto-generated method stub
		return Color.BLACK;
	}

	@Override
	public Color getBackgroundSelectionColor() {
		// TODO Auto-generated method stub
		return Constants.COLOR_BLUE_LABEL;
	}

	@Override
	public Color getBackgroundNonSelectionColor() {
		// TODO Auto-generated method stub
		return Constants.COLOR_WHITE_BACKGROUND;
	}

//	@Override
//	public Color getBorderSelectionColor() {
//		// TODO Auto-generated method stub
//		return Constants.COLOR_BLUE_LABEL;
//	}

	
	
}
