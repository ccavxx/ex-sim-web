package com.topsec.tsm.sim.report.chart.highchart.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.chart.highchart.HChart;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.report.util.ReportUiConfig;

public class Pie implements HChart{
	public static final String CHART_TYPE ="pie";
	private String type="pie";
	private String title;
	private String unit;
	private Map series;
	
	public Pie() {
		super();
	}

	public Pie(String type, String title, String unit, Map series) {
		super();
		this.type = type;
		this.title = title;
		this.unit = unit;
		this.series = series;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Map getSeries() {
		return series;
	}

	public void setSeries(Map series) {
		this.series = series;
	}

	@Override
	public Object createChart(List<Map> data, Map<Object, Object> subMap,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
	
		String chartItem =StringUtil.toString(subMap.get("chartItem"), "");
		String category = subMap.get("category").toString();
		Object [][] objs = new Object[data.size()][2];
		
		String [] categories = new String[data.size()];
		for(int i=0,len=data.size();i<len;i++){
			Map map = data.get(i);
			objs[i][0] = map.get(category);
			objs[i][1] = map.get(chartItem);
		}
	    Map series = new HashMap();
	    series.put("type", Pie.CHART_TYPE);
	    series.put("data", objs);
		
	    String title = subMap.get("subName").toString();
	    String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
	    int countSign = ReportModel.getCountSign(chartItems, data);
	    String unit = ReportUiConfig.Capability.get(countSign);
	    Pie pie = new Pie(Pie.CHART_TYPE, title, unit, series);
		return pie;
	}
	
}
