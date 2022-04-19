package com.vaspsolutions.analytics.UI;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;

public class IA_CategoryAxis extends CategoryAxis {

	/* (non-Javadoc)
	 * @see org.jfree.chart.axis.CategoryAxis#drawCategoryLabels(java.awt.Graphics2D, java.awt.geom.Rectangle2D, java.awt.geom.Rectangle2D, org.jfree.ui.RectangleEdge, org.jfree.chart.axis.AxisState, org.jfree.chart.plot.PlotRenderingInfo)
	 */
	@Override
	protected AxisState drawCategoryLabels(Graphics2D arg0, Rectangle2D arg1,
			Rectangle2D arg2, RectangleEdge arg3, AxisState arg4,
			PlotRenderingInfo arg5) {
		// TODO Auto-generated method stub
	
		return super.drawCategoryLabels(arg0, arg1, arg2, arg3, arg4, arg5);
	}

}
