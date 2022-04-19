package com.vaspsolutions.analytics.UI;


import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.inductiveautomation.factorypmi.application.components.tabstrip.PMITabStrip;
/**
 * Class to create collapsible menu tree
 * @author YM : Created on 05/21/2015
 * 
 *
 */
public class CollapsibleMenuPanel extends JPanel implements TreeSelectionListener {

	/*JTree overview;
	JTree systems;
	JTree behaviour;
	JTree alarms;*/
	JTree mainTree;
	public CollapsibleMenuPanel(){
		
		this.setLayout(new GridLayout(0,1));
		
		DefaultMutableTreeNode treeTop = new DefaultMutableTreeNode("Reports");
		
		
		//create the Top node of overview menu
		DefaultMutableTreeNode topNodeOverview =
		        new DefaultMutableTreeNode("Overview");
		
		topNodeOverview.add(new DefaultMutableTreeNode("By Date"));
		topNodeOverview.add(new DefaultMutableTreeNode("Cities"));
		topNodeOverview.add(new DefaultMutableTreeNode("Groups"));
		topNodeOverview.add(new DefaultMutableTreeNode("Top Screens"));
		topNodeOverview.add(new DefaultMutableTreeNode("Top Reports"));
		topNodeOverview.add(new DefaultMutableTreeNode("Top Trends"));
		topNodeOverview.add(new DefaultMutableTreeNode("Bounce Rate"));
		/*overview = new JTree(topNodeOverview);
	
		overview.addTreeSelectionListener(this);
		this.add(overview);
		*/
		//create the Top node of Systems menu
		DefaultMutableTreeNode topNodeSystems =
		        new DefaultMutableTreeNode("Systems");
		
		topNodeSystems.add(new DefaultMutableTreeNode("Device Types"));
		topNodeSystems.add(new DefaultMutableTreeNode("Platforms"));
		topNodeSystems.add(new DefaultMutableTreeNode("Browsers"));
		topNodeSystems.add(new DefaultMutableTreeNode("Screen Resolutions"));
		
		/*systems = new JTree(topNodeSystems);
	
		systems.addTreeSelectionListener(this);
		this.add(systems);
		*/
		DefaultMutableTreeNode topNodebehaviour =
		        new DefaultMutableTreeNode("behaviour");
		
		topNodebehaviour.add(new DefaultMutableTreeNode("Actions per visit"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Visit Duration"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Engagement"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Frequency"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Recency"));
		topNodebehaviour.add(new DefaultMutableTreeNode("Active Users"));
		/*behaviour = new JTree(topNodebehaviour);
		behaviour.addTreeSelectionListener(this);
		this.add(behaviour);
		*/
		DefaultMutableTreeNode topNodealarms =
		        new DefaultMutableTreeNode("Alarms");
		topNodealarms.add(new DefaultMutableTreeNode("Alarm Summary"));
		/*alarms = new JTree(topNodealarms);
		alarms.addTreeSelectionListener(this);
		this.add(alarms);*/
		
		treeTop.add(topNodeOverview);
		treeTop.add(topNodeSystems);
		treeTop.add(topNodebehaviour);
		treeTop.add(topNodealarms);
		mainTree= new JTree(treeTop);
		mainTree.addTreeSelectionListener(this);
		//mainTree.setCellRenderer(new IA_TreeCellRenderer());
		DefaultTreeCellRenderer r = (DefaultTreeCellRenderer)mainTree.getCellRenderer();
		r.setLeafIcon(new ImageIcon(getClass().getResource("treenode.png")));
		
		this.add(mainTree);
		
	}

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		JOptionPane.showMessageDialog(this, "Selected : " + arg0.getPath());
		
	}
}
