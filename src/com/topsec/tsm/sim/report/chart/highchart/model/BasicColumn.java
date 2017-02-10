package com.topsec.tsm.sim.report.chart.highchart.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.chart.highchart.CreateChartFactory;
import com.topsec.tsm.sim.report.chart.highchart.HChart;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;

public class BasicColumn implements HChart{
	public static final String CHART_TYPE ="column";
	private String type="column";
	private String title;
	private Map<String,Object> xAxis;
	private String ytitle;
	private String unit;
	private List<Map<String,Object>> series;
	
	public BasicColumn() {
		super();
	}

	public BasicColumn(String type, String title, Map xAxis, String ytitle,
			String unit, List<Map<String,Object>> series) {
		super();
		this.type = type;
		this.title = title;
		this.xAxis = xAxis;
		this.ytitle = ytitle;
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
	public Map getxAxis() {
		return xAxis;
	}
	public Map getXAxis() {
		return xAxis;
	}

	public void setXAxis(Map xAxis) {
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

	public List<Map<String,Object>> getSeries() {
		return series;
	}

	public void setSeries(List<Map<String,Object>> series) {
		this.series = series;
	}

	@Override
	public Object createChart(List<Map> data, Map<Object, Object> subMap,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		List<Map<String,Object>> series = new ArrayList<Map<String,Object>>() ;
		List<String> categories = new ArrayList<String>();
		for(Map map:data){
			Map<String,Object> ser = new HashMap<String,Object>() ;
			categories.add(StringUtil.toString(map.get(category)));
			if ("TYPE".equalsIgnoreCase(category)
					||"ALLLOGTYPE".equalsIgnoreCase(category)) {
				ser.put("name", DeviceTypeNameUtil.getDeviceTypeName((String)map.get(category), Locale.getDefault())) ;
			}else{
				ser.put("name", map.get(category)) ;
			}
			Object []objects=new Object[chartItems.length];
			for(int j=0,jlen=chartItems.length;j<jlen;j++){
				objects[j]=map.get(chartItems[j]) ;
			}
			ser.put("data",FastJsonUtil.wrapper(objects));
			series.add(ser) ;
		}
		HashMap<String, Object> xAxis = new HashMap<String, Object>();
		xAxis.put("categories", categories);
		xAxis.put("gridLineDashStyle", "dash") ;
		Map<String,String> keys = CreateChartFactory.getFieldValue(subMap.get("tableFiled").toString(), subMap.get("tableLable").toString());
		
		/*List<Map> sList = new ArrayList<Map>();
	    for(int i=0,len=objs.length;i<len;i++){
	    	Map tmp= new HashMap();
	    	categories.add(StringUtil.toString(map.get(category)));
	    	tmp.put("name", keys.get(chartItems[i]));
	    	Object[] datas = new Object[objs[i].length] ;
	    	for(int index=0;index<datas.length;index++){
	    		Map<String,Object> dataMap = new HashMap<String, Object>(2) ;
	    		dataMap.put("y", objs[i][index]) ;
	    		dataMap.put("color", HChart.colors[index%HChart.colors.length]) ;
	    		datas[index] = dataMap ;
	    	}
	    	tmp.put("data", datas);
	    	sList.add(tmp);
	    }*/
	    String title = subMap.get("subName").toString();
	    int countSign = ReportModel.getCountSign(chartItems, data);
	    String unit = ReportUiConfig.Capability.get(countSign);
	    BasicColumn column = new BasicColumn(BasicColumn.CHART_TYPE, title, xAxis, null, unit, series);
		return column;
	}
}
