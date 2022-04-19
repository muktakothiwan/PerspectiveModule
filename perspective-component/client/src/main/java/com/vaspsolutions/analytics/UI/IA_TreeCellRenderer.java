package com.vaspsolutions.analytics.UI;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class IA_TreeCellRenderer extends DefaultTreeCellRenderer {
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		try{
			
		
		if (leaf)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			
				//setOpenIcon(new ImageIcon(getClass().getResource("treenode.png")));
				setClosedIcon(new ImageIcon(getClass().getResource("treenode.png")));
				//setLeafIcon(new ImageIcon(getClass().getResource("treenode.png")));
			
				       
		}
		 super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		 return this;
		
	}

	public IA_TreeCellRenderer() {
		super();
		
	}
}
