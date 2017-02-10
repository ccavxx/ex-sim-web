package com.topsec.tsm.sim.report.chart.highchart.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.stringtemplate.v4.compiler.CodeGenerator.subtemplate_return;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.report.chart.highchart.CreateChartFactory;
import com.topsec.tsm.sim.report.chart.highchart.HChart;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

public class Spline implements HChart{
	public static final String CHART_TYPE ="spline";
	private String title;
	private String unit;
	private List<String> xcategories;
	private List<List<Double>> ydata;
	private List<SubItem> series;
	public Spline() {
		super();
	}
	
	public Spline(String title, String unit, List<SubItem> series) {
		super();
		this.title = title;
		this.unit = unit;
		this.series = series;
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
	public List<SubItem> getSeries() {
		return series;
	}
	public void setSeries(List<SubItem> series) {
		this.series = series;
	}

	private String dateType(List<Map> data,Map<Object, Object> subMap){
		if (GlobalUtil.isNullOrEmpty(data)
				||GlobalUtil.isNullOrEmpty(subMap)) {
			return null;
		}
		String category = subMap.get("category").toString();
		Map mapS=data.get(0);
		Map mapE=data.get(data.size()-1);
		if (!GlobalUtil.isNullOrEmpty(mapS.get(category))
				&&!GlobalUtil.isNullOrEmpty(mapE.get(category))) {
			try {
				String categoriesS = mapS.get(category).toString();
				String categoriesE = mapE.get(category).toString();
				Date sDate = StringUtil.toDate(categoriesS, ReportUiConfig.dFormat1);
				Date eDate = StringUtil.toDate(categoriesE, ReportUiConfig.dFormat1);
				long timeA=eDate.getTime()-sDate.getTime();
				if (timeA<2*3600*1000l) {
					return "hour";
				}else if (timeA<24*3600*1000l) {
					return "day";
				}else if (timeA<8*24*3600*1000l) {
					return "week";
				}else if (timeA<32*24*3600*1000l) {
					return "month";
				}else if (timeA<366*24*3600*1000l) {
					return "year";
				}
			} catch (Exception e) {
			}
			
		}
		boolean qushiFlag = subMap.get("chartProperty") != null&& subMap.get("chartProperty").toString().equals("1");
		String tableSql = StringUtil.nvl((String)subMap.get("tableSql")) ;
		String pageSql = StringUtil.nvl((String)subMap.get("pagesql")) ;
		String chartSql = StringUtil.nvl((String)subMap.get("chartSql")) ;
		String subName=StringUtil.nvl((String)subMap.get("subName")) ;
		if (qushiFlag) {
			if (tableSql.indexOf("Hour") > 20|| tableSql.indexOf("_hour") > 20) {
				return "hour";
			} else if (tableSql.indexOf("Day") > 20|| tableSql.indexOf("_day") > 20) {
				return "day";
			} else if (pageSql.indexOf("Hour") > 20
					|| pageSql.indexOf("_hour") > 20
					|| chartSql.indexOf("Hour") > 20) {
				return "hour";
			} else if (pageSql.indexOf("Day") > 20
					|| pageSql.indexOf("_day") > 20
					|| chartSql.indexOf("Day") > 20) {
				return "day";
			} else{
				return "month";
			}
		} else if(subName.indexOf("分布图") > 1
				&& (tableSql.indexOf("Hour") > 20|| tableSql.indexOf("_hour") > 20)){
			return "day";
		} 
		return null;
	}
	private String dateShow(String dateType,String date) {
		if (GlobalUtil.isNullOrEmpty(date)) {
			return "";
		}
		if (date.length()>=19) {
			date=date.substring(0,19);
		}
		if ("hour".equals(dateType)&&date.length()>12) {
			return date.substring(11,date.length());
			
		}else if ("day".equals(dateType)&&date.length()>13) {
			return date.substring(11,date.length()-3);
		}else if (("week".equals(dateType)
				||"month".equals(dateType))&&date.length()>11) {
			return date.substring(5,10);
		}else if ("year".equals(dateType)&&date.length()>7) {
			return date.substring(0,7);
		}
		return "";
	}
	@Override
	public Object createChart(List<Map> data, Map<Object, Object> subMap,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		data=ReportUiUtil.mapFormat(null, data, subMap);
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		Object [][] objs = new Object[chartItems.length][data.size()];
		
		String [] categories = new String[data.size()];
		List<String>xcategories=new ArrayList<String>();
		List<List<Double>>ydata=new ArrayList<List<Double>>();
		for(int j=0,jlen=chartItems.length;j<jlen;j++){
			List<Double> ytdataList=new ArrayList<Double>();
			ydata.add(ytdataList);
		}
		Map<String,Object> imap = null;
		Map<String,String> keys = CreateChartFactory.getFieldValue(subMap.get("tableFiled").toString(), subMap.get("tableLable").toString());
		List<SubItem> sList2 = new ArrayList<SubItem>();
		String dateType=dateType(data,subMap);
		if (GlobalUtil.isNullOrEmpty(dateType)) {
			dateType="day";
		}
		for(int i=0,len=data.size();i<len;i++){
			Map map = data.get(i);
			if (!GlobalUtil.isNullOrEmpty(map.get(category))) {
				categories[i] = map.get(category).toString();
				if (!xcategories.contains(categories[i])) {
					xcategories.add(dateShow(dateType,categories[i]));
				}
			}
			
			Date tmp = StringUtil.toDate(categories[i], ReportUiConfig.dFormat1);
			for(int j=0,jlen=chartItems.length;j<jlen;j++){
				if (!GlobalUtil.isNullOrEmpty(chartItems[j])) {
					
					Object ostring=map.get(chartItems[j])==null?"0":map.get(chartItems[j]);
					ydata.get(j).add(Double.valueOf(ostring.toString()));
					imap = new HashMap<String,Object>();
					imap.put("x", tmp.getTime());
					imap.put("y", ostring);
					objs[j][i]= imap;
				}
			}
		}
		
		List<SubItem> sList = new ArrayList<SubItem>();
		SubItem item = null;
	    for(int i=0,len=objs.length;i<len;i++){
	    	item = new SubItem(Spline.CHART_TYPE,keys.get(chartItems[i]),objs[i]);
	    	sList.add(item);
	    }
	    for (int i = 0; i < ydata.size(); i++) {
	    	SubItem subItem = new SubItem();
	    	subItem.setName(GlobalUtil.isNullOrEmpty(keys.get(chartItems[i]))?getSeriesName(chartItems[i]):keys.get(chartItems[i]));
	    	subItem.setType(Spline.CHART_TYPE);
	    	subItem.setData(ydata.get(i).toArray());
	    	sList2.add(subItem);
		}
	    String title = subMap.get("subName").toString();
	    int countSign = ReportModel.getCountSign(chartItems, data);
	    String unit = ReportUiConfig.Capability.get(countSign);
	    Spline spline = new Spline(title, unit, sList2);
	    spline.setXcategories(xcategories);
	    spline.setYdata(ydata);
		return spline;
	}

	private String getSeriesName(String string){
		if ("opCount".equalsIgnoreCase(string)) {
			return "无危险";
		}else if ("opCount1".equalsIgnoreCase(string)) {
			return "低危险";
		}else if ("opCount2".equalsIgnoreCase(string)) {
			return "一般危险";
		}else if ("opCount3".equalsIgnoreCase(string)) {
			return "高危险";
		}else if ("opCount4".equalsIgnoreCase(string)) {
			return "非常危险";
		}
		return "个数";
	}
	public List<String> getXcategories() {
		return xcategories;
	}

	public void setXcategories(List<String> xcategories) {
		this.xcategories = xcategories;
	}

	public List<List<Double>> getYdata() {
		return ydata;
	}

	public void setYdata(List<List<Double>> ydata) {
		this.ydata = ydata;
	}
	
}
