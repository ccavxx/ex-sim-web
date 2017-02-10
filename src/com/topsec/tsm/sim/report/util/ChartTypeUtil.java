package com.topsec.tsm.sim.report.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

/**
 * 图表类型工具类
 * @author hp
 *
 */
public class ChartTypeUtil {

	/**
	 * FusionChart Column3D柱状图
	 */
	public static final String FC_COLUMN3D = "Column3D" ;
	/**
	 * FuscionChart ScrollArea2D 趋势图
	 */
	public static final String FC_SCROLL_AREA2D = "ScrollArea2D" ;
	/**
	 * FuscionChart 3D饼图
	 */
	public static final String FC_Pie3D = "Pie3D" ;
	
	/**
	 * 根据chartType获得相对应的FusionChart中的FCChartType
	 * @param chartType
	 * @return
	 */
	public static String getFCChartType(int chartType){
		String fcChartType = null ;
		switch(chartType){
			case 1: fcChartType =FC_COLUMN3D; break;
			case 2: break ;
			case 3: break ;
			case 4: fcChartType = FC_SCROLL_AREA2D; break ;
			case 5: fcChartType = FC_Pie3D; break;
			default: fcChartType = "" ;
		}
		return fcChartType ;
	}
	
	public static JFreeChart getJFreeChart(int chartType){
		JFreeChart chart = null ;
		switch(chartType){
			case 1: 
				chart = ChartFactory.createBarChart3D(null, null, null, null, PlotOrientation.HORIZONTAL, false, false, false); break;
			case 2: break ;
			case 3: break ;
			case 4: 
				chart = ChartFactory.createAreaChart(null,null , null, null, PlotOrientation.HORIZONTAL, false, false, false);break ;
			case 5: 
				chart = ChartFactory.createPieChart3D(null, null, false, false, false); break;
			default: ;
		}
		return chart ;
	}
	
}
