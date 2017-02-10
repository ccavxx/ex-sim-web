package com.topsec.tsm.sim.report.chart.highchart;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.jfree.data.category.DefaultCategoryDataset;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.CallableCallback;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicArea;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicColumn;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicLine;
import com.topsec.tsm.sim.report.chart.highchart.model.Pie;
import com.topsec.tsm.sim.report.chart.highchart.model.Spline;
import com.topsec.tsm.sim.report.chart.highchart.model.SubItem;
import com.topsec.tsm.sim.report.component.ChartImageCreator;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartCategoryFormatter;
import com.topsec.tsm.sim.util.FastJsonUtil;

public class HighChartImageCreator extends ChartImageCreator {

	private List<ChartData> chartData ;
	private CreateChartFactory factory = CreateChartFactory.getInstance();
	
	public HighChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight,List<ChartData> chartData) {
		super(chartType,imageType,chartWidth,chartHeight,null) ;
		this.chartData = chartData ;
	}


	public HighChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight,List<ChartData> chartData,CallableCallback<String> callback) {
		super(chartType,imageType,chartWidth,chartHeight,callback) ;
		this.chartData = chartData ;
	}


	@Override
	public String generateChartImage() {
		if(chartType == null){
			return null ;
		}
		String chartJSON = null ;
		switch(chartType){
			case 1: 
				chartJSON = createBarChart3D(); break;
			case 2: break ;
			case 3: break ;
			case 4: 
				chartJSON = createAreaChart();break ;
			case 5: 
				chartJSON = createPieChart3D(); break;
			case 6:
				chartJSON = createLineChart3D();break;
			default: ;
		}
		if (chartJSON != null) {
			String imagePath = null;
			try {
				imagePath = HighChartExportTool.exportUsePhantomjs(chartJSON);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return imagePath ;
		}
		return null;
	}
	/**
	 * 创建折线图
	 * @return
	 */
	private String createLineChart3D(){
		List<String> categories = new ArrayList<String>() ;
		List<SubItem> items = new ArrayList<SubItem>() ;
		List<Object> datas = new ArrayList<Object>() ;
		for(ChartData data:chartData){
			categories.add(data.getSerise()) ;
			datas.add(data.getValue()) ;
	    }
		SubItem item0 = new SubItem("","",datas.toArray()) ;
		items.add(item0) ;
	    Spline line = new Spline("","",items);
	    line.setXcategories(categories) ;
		String json = factory.createJsonSpline(line, super.getChartWidth(), super.getChartHeight()) ;
		return json ;
	}
	/**
	 * 创建3D饼图
	 * @return
	 */
	private String createPieChart3D(){
		List<Object> data = new ArrayList<Object>();
		Map<String, Object> series = new HashMap<String, Object>();
		series.put("type", Pie.CHART_TYPE) ;
		series.put("data", data) ;
		for(ChartData cd:chartData){
			data.add(FastJsonUtil.wrapper(cd.getSerise(),cd.getValue())) ;
		}
		Pie pie = new Pie(Pie.CHART_TYPE, "", "", series) ;
		String jsonData = factory.createJsonPie(pie, super.getChartWidth(), super.getChartHeight()) ;
		return jsonData ;
	}
	/**
	 * 创建3D柱状图
	 * @return
	 */
	private String createBarChart3D(){
		List<Map<String,Object>> series = new ArrayList<Map<String,Object>>() ;
		for(ChartData data:chartData){
			Map<String, Object> srs = new HashMap<String, Object>();
			srs.put("name", data.getSerise()) ;
			srs.put("data", FastJsonUtil.wrapper(data.getValue())) ;
			series.add(srs) ;
		}
		BasicColumn bc = new BasicColumn(String.valueOf(chartType), "", null, null, "", series) ;
		String jsonData = factory.createJsonBasicColumn(bc,super.getChartWidth() , super.getChartHeight()) ;
		return jsonData ;
	}
	/**
	 * 创建区域图
	 * @return
	 */
	private String createAreaChart(){
		Map<String,Map> series = new HashMap<String,Map>() ;
		TreeSet<String> categories = new TreeSet<String>() ;
		for(ChartData data:chartData){
			categories.add(data.getCategory()) ;
			Map<String,Object> srs = series.get(data.getSerise());
			if(srs == null){
				srs = new HashMap<String,Object>() ;
				srs.put("name", data.getSerise()) ;
				srs.put("data", new ArrayList()) ;
				series.put(data.getSerise(), srs) ;
			}
			((List)srs.get("data")).add(data.getValue()) ;
		}
		Map<String,Object> xAxis = new HashMap<String,Object>() ;
		xAxis.put("categories", categories) ;
		BasicArea bc = new BasicArea(String.valueOf(chartType),"",xAxis,"","", new ArrayList(series.values())) ;
		String jsonData = factory.createJsonBasicArea(bc,super.getChartWidth() , super.getChartHeight()) ;
		return jsonData ;
	}

}
