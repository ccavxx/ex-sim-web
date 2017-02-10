package com.topsec.tsm.sim.report.chart.highchart.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.chart.highchart.CreateChartFactory;
import com.topsec.tsm.sim.report.chart.highchart.HChart;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.report.util.ReportUiConfig;

public class BasicLine implements HChart{
	public static final String CHART_TYPE ="line";
	private String title;
	private Map xAxis;
	private String ytitle;
	private String unit;
	private List<Map> series;
	
	public BasicLine() {
		super();
	}

	public BasicLine(String title, Map xAxis, String ytitle, String unit,
			List<Map> series) {
		super();
		this.title = title;
		this.xAxis = xAxis;
		this.ytitle = ytitle;
		this.unit = unit;
		this.series = series;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map getxAxis() {
		return xAxis;
	}

	public void setxAxis(Map xAxis) {
		this.xAxis = xAxis;
	}

	public String getYtitle() {
		return ytitle;
	}

	public void setYtitle(String ytitle) {
		this.ytitle = ytitle;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public List<Map> getSeries() {
		return series;
	}

	public void setSeries(List<Map> series) {
		this.series = series;
	}

	@Override
	public Object createChart(List<Map> data, Map<Object, Object> subMap,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		Object [][] objs = new Object[chartItems.length][data.size()];
		
		String [] categories = new String[data.size()];
		for(int i=0,len=data.size();i<len;i++){
			Map map = data.get(i);
			categories[i] = map.get(category).toString();
			for(int j=0,jlen=chartItems.length;j<jlen;j++){
				objs[j][i]=map.get(chartItems[j]);
			}
		}
		Map xAxis = new HashMap();
		xAxis.put("categories", categories);
		
		Map<String,String> keys = CreateChartFactory.getFieldValue(subMap.get("tableFiled").toString(), subMap.get("tableLable").toString());
		
		List<Map> sList = new ArrayList<Map>();
	    for(int i=0,len=objs.length;i<len;i++){
	    	Map tmp= new HashMap();
	    	tmp.put("name", keys.get(chartItems[i]));
	    	tmp.put("data", objs[i]);
	    	sList.add(tmp);
	    }
	    String title = subMap.get("subName").toString();
	    int countSign = ReportModel.getCountSign(chartItems, data);
	    String unit = ReportUiConfig.Capability.get(countSign);
	    BasicLine line = new BasicLine(title, xAxis, "", unit, sList);
		return line;
	}

}
