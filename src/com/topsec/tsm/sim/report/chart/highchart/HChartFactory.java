package com.topsec.tsm.sim.report.chart.highchart;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicColumn;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicLine;
import com.topsec.tsm.sim.report.chart.highchart.model.Pie;
import com.topsec.tsm.sim.report.chart.highchart.model.Spline;

public class HChartFactory {
	private static HChartFactory instance = new HChartFactory();

	private static HChart column = new BasicColumn();
	private static HChart line = new BasicLine();
	private static HChart spline = new Spline();
	private static HChart pie = new Pie();
	
	public Object createChart(List<Map> data, Map<Object,Object> subMap,HttpServletRequest request, HttpServletResponse response)
			throws Exception {
        String type = StringUtil.toString(subMap.get("chartType"),"");
		
		
		if (type == null) {
			throw new Exception("The chart type is null!");
		}
		
		if (type.equals("1")) {
			return column.createChart(data, subMap, request, response);
		} else if (type.equals("2")) {
			return column.createChart(data, subMap, request, response);
		} else if (type.equals("3")) {
			return column.createChart(data, subMap, request, response);
		} else if (type.equals("4")) {//modify by wza 2014 8 18
			return spline.createChart(data, subMap, request, response);
//			return line.createChart(data, subMap, request, response);
		}else if(type.equals("5")){
			return pie.createChart(data, subMap, request, response) ;
		} else if (type.equals("6")) {
			return spline.createChart(data, subMap, request, response);
		}
		return null;
	}

	public static HChartFactory getInstance() {
		return instance;
	}

}
