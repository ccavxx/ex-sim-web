package com.topsec.tsm.sim.report.chart.highchart;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import java_cup.runtime.double_token;
import java_cup.runtime.int_token;

import org.apache.commons.lang.ArrayUtils;
import org.one2team.highcharts.server.export.ExportType;
import org.one2team.highcharts.server.export.HighchartsExporter;
import org.stringtemplate.v4.compiler.STParser.ifstat_return;

import com.alibaba.fastjson.JSON;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.log.bean.LogSearchObject;
import com.topsec.tsm.sim.log.util.LogUtil;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicArea;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicColumn;
import com.topsec.tsm.sim.report.chart.highchart.model.BasicLine;
import com.topsec.tsm.sim.report.chart.highchart.model.ChartTable;
import com.topsec.tsm.sim.report.chart.highchart.model.ColumnData;
import com.topsec.tsm.sim.report.chart.highchart.model.Pie;
import com.topsec.tsm.sim.report.chart.highchart.model.Spline;
import com.topsec.tsm.sim.report.chart.highchart.model.SubItem;
import com.topsec.tsm.sim.report.util.ChartCategoryFormatter;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

public class CreateChartFactory {
	private Object chart;
	private ChartTable table;
	private static CreateChartFactory instance;
	public static final String Rn = "\r\n";
	public static final String s4 = "    ";
	public static final String s6 = "      ";
	//['#FFA940','#ACD25E','#FCDE3D','#CCFFFF','#94E4AE','#F4A0A0','#409FFF','#DDE6E6','#F2D8C2','#FEF5C8','#CBD8E9','#F1EAA4','#AFD8F8','#F6BD0F','#8BBA00','#FF8E46','#008E8E','#D64646','#8E468E','#588526','#B3AA00','#4169E1']
	public static final String colors="['#32c8fa','#f99049','#a4e120','#ffe666','#906bc8', '#08a4f3','#ffa03f','#99cc00','#fff558','#d040ff', '#99ccff','#ff7f3a','#labebe','#f7c43b','#ff52b0', '#6666ff','#fe8a8a','#2cb022','#ec6dff','#95cfd1']";
	private CreateChartFactory(){	
		
	};
	
	public String createJsonBasicArea(BasicArea chart,int width,int height){
		String str = 
		"{" +
		"  chart : {" + 
		"    borderColor : '#6699FF'," + 
		"    borderWidth : 1," + 
		"    type : 'area'," + 
		"    width : " +width + "," + 
		"    height : " + height + 
		"  }," + 
		"  title : {" + 
		"    text : ''" + 
		"  }," + 
		"  credits : {" + 
		"    enabled : false" + 
		"  }," + 
		"  legend : {" + 
		"    enabled : false" + 
		"  }," + 
		"  xAxis : {" + 
		"    categories : "+JSON.toJSON(chart.getxAxis().get("categories"))+"," + 
		"    labels : {" + 
		"      formatter :"+getLabelFormatter(chart.getxAxis().get("categories")) + 
		"    }" + 
		"  }," + 
		"  yAxis : {" + 
		"    title : {" + 
		"      text : ''" + 
		"    }," + 
		"    labels : {" + 
		"      formatter : function (){" + 
		"        return this.value;" + 
		"      }" + 
		"    }" + 
		"  }," + 
		"  plotOptions : {" + 
		"    area : {" + 
		"      dataLabels : {" + 
		"        enabled : true," + 
		"        formatter : function () {" + 
		"          return this.y > 0 ? this.y : '';" + 
		"        }" + 
		"      }," + 
		"      pointStart : 0," + 
		"      marker : {" + 
		"        enabled : true," + 
		"        color : '#000000'," + 
		"        connectorColor : '#000000'," + 
		"      }" + 
		"    }" + 
		"  }," + 
		"  colors : "+colors+"," + 
		"  series : " + JSON.toJSON(chart.getSeries()) + 
		"}";
		return str ;		
	}
	private String getLabelFormatter(Object categories){
		Date beginDate ;
		Date endDate ;
		Collection<Object> cgs=(Collection<Object>)categories;
		if (cgs == null || cgs.size() == 0) {
			return "function(){return this.value;}";
		}
		if(categories instanceof TreeSet){
			beginDate = StringUtil.toDate((String)((TreeSet)categories).first(),"yyyy-MM-dd HH:mm:ss") ;
			endDate = StringUtil.toDate((String)((TreeSet)categories).last(),"yyyy-MM-dd HH:mm:ss") ;
		}else if(categories instanceof List){
			List cts = (List)categories ;
			beginDate = StringUtil.toDate((String)cts.get(0),"yyyy-MM-dd HH:mm:ss") ;
			endDate = StringUtil.toDate((String)cts.get(cts.size()-1),"yyyy-MM-dd HH:mm:ss") ;
		}else if(categories instanceof String[]){
			String[] cts = (String[])categories ;
			beginDate = StringUtil.toDate(cts[0],"yyyy-MM-dd HH:mm:ss") ;
			endDate = StringUtil.toDate(cts[cts.length-1],"yyyy-MM-dd HH:mm:ss") ;
		}else{
			return "function(){return this.value;}" ;
		}
		long timeMinus = endDate.getTime() - beginDate.getTime() ;//时间差
		String matchPattern = "" ;//默认情况下只匹配整点数据
		int beginIndex = 0 ;
		int endIndex = 0 ;
		if(ChartCategoryFormatter.greaterThanOneSeason(timeMinus)){//时间差大于一季度,只显示每月1号坐标
			matchPattern = "^.{8}01" ;//只匹配每月1号数据(yyyy-MM-01)
			endIndex = 7 ;
		}else if(ChartCategoryFormatter.greaterThanOneMonth(timeMinus)){//时间差大于一个月，只显示每个月1号和15号坐标
			matchPattern = "^.{8}(01|15)" ;//只匹配每月1号和15号
			beginIndex = 5 ;
			endIndex = 10 ;
		}else if(ChartCategoryFormatter.greaterThanOneWeek(timeMinus)){//时间差大于一周，只显示奇数天坐标
			matchPattern = "00:00:00$" ;//只匹配奇数天数据
			beginIndex = 8 ;
			endIndex = 10 ;
		}else if(ChartCategoryFormatter.greaterThanOneDay(timeMinus)){//时间差大于一天，显示0点18点坐标
			matchPattern= "(00|18):00:00$" ;//匹配任意日期
			beginIndex = 8 ;
			endIndex = 14 ;
		}else if(ChartCategoryFormatter.greaterThanOneHour(timeMinus)){//时间差大于小时显示整点坐标
			matchPattern = "00:00$" ;//只匹配整点数据
			beginIndex = 11 ;
			endIndex = 13 ;
		}else{
			beginIndex = 11 ;
			endIndex = 15 ;
		}
		String formatter = "function(){" +
								"var regex = /"+matchPattern+"/g ;" +
								"if(regex.test(this.value)){" +
									"return this.value.substring("+beginIndex+","+endIndex+")" +
								"}" +
							"}" ;
		return formatter ;
	}	
	public BasicArea createBasicArea(Map<Object,Object> data){
		Map<Object, Object> subMap = (Map<Object, Object>) data.get("subMap");
		List<Map> result = (List<Map>) data.get("result");
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		int sizei=result.size()<1?1:result.size();
		Object [][] objs = new Object[chartItems.length][sizei];
		
		List<String> categories = new ArrayList<String>();
		for(int i=0,len=result.size();i<len;i++){
			Map map = result.get(i);
			categories.add(map.get(category).toString());
			for(int j=0,jlen=chartItems.length;j<jlen;j++){
				objs[j][i]=map.get(chartItems[j]);
			}
		}
		Map xAxis = new HashMap();
		xAxis.put("categories", categories);
		Map<String,String> keys = getFieldValue(subMap.get("tableFiled").toString(), subMap.get("tableLable").toString());
		List<Map> sList = new ArrayList<Map>();
	    for(int i=0,len=objs.length;i<len;i++){
	    	Map tmp= new HashMap();
	    	tmp.put("name", keys.get(chartItems[i]));
	    	tmp.put("data", objs[i]);
	    	sList.add(tmp);
	    }
	    String title = subMap.get("subName").toString();
	    String unit = data.get("unit").toString();
	    BasicArea area = new BasicArea(BasicArea.CHART_TYPE, title, xAxis, "", unit, sList);
		return area;
	}
	
	public String createJsonBasicColumn(BasicColumn chart,int width,int height) {
		String str =  
		"{" +
		"  chart : {" + 
		"    borderColor : '#6699FF'," + 
		"    borderWidth : 1," + 
		"    defaultSeriesType : 'column'," + 
		"    width : "+width+"," + 
		"    height : "+height+"," + 
		"    options3d : {" + 
		"      enabled : true," + 
		"      alpha : 4," + 
		"      beta : 12," + 
		"      depth : 50," + 
		"      viewDistance : 25" + 
		"    }" + 
		"  }," + 
		"  labels : {" + 
		"    style : {" + 
		"      color : '#000000'," + 
		"      fontFamily : '微软雅黑, \"Microsoft Yahei\",Arial,Helvetica, sans-serif'," + 
		"      fontSize : '12px'" + 
		"    }" + 
		"  }," + 
		"  title : {" + 
		"    text : ''" + 
		"  }," + 
		"  credits : {" + 
		"    enabled : false" + 
		"  }," + 
		"  legend : {" + 
		"    layout : 'vertical'," + 
		"    align : 'right'," + 
		"    verticalAlign : 'top'," + 
		"    borderWidth : 0" + 
		"  }," + 
		"  xAxis : {" + 
		"    labels : {" + 
		"      enabled : false" + 
		"    }," + 
		"    tickLength : 0" + 
		"  }," + 
		"  yAxis : {" + 
		"    min : 0," + 
		"    gridLineDashStyle : 'dash'," + 
		"    title : {" + 
		"      text : '"+chart.getUnit()+"'" + 
		"    }" + 
		"  }," + 
		"  plotOptions : {" + 
		"    column : {" + 
		"      shadow : true," + 
		"      pointWidth : 20," + 
		"      borderWidth : 0," + 
		"      dataLabels : {" + 
		"        style : {" + 
		"          color : '#000000'," + 
		"          fontFamily : '微软雅黑, \"Microsoft Yahei\",Arial,Helvetica, sans-serif'," + 
		"          fontSize : '12px'" + 
		"        }," + 
		"        enabled : true" + 
		"      }" + 
		"    }" + 
		"  }," + 
		"  colors : "+colors+"," + 
		"  series : "+JSON.toJSON(chart.getSeries())+ 
		"}";
		return str ;
	}
	
	public BasicColumn createBasicColumn(Map<Object,Object> data){
		Map<Object, Object> subMap = (Map<Object, Object>) data.get("subMap");
		List<Map> result = (List<Map>) data.get("result");
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		int sizei=result.size()<1?1:result.size();
		Object [][] objs = new Object[chartItems.length][sizei];
		
		String [] categories = new String[sizei];
		boolean flag = ReportUiUtil.isSystemLog(subMap);
		for(int i=0,len=result.size();i<len;i++){
			Map map = result.get(i);
			String tmp = StringUtil.toString(map.get(category),"N/A");
			if(flag){
				tmp = ReportUiUtil.getDeviceTypeName(StringUtil.toString(tmp,""), Locale.getDefault());
			}
			categories[i] = tmp;
			for(int j=0,jlen=chartItems.length;j<jlen;j++){
				objs[j][i]=map.get(chartItems[j]);
			}
		}
		HashMap<String, Object> xAxis = new HashMap<String, Object>();
		xAxis.put("categories", categories);
		
		Map<String,String> keys = getFieldValue(subMap.get("tableFiled").toString(), subMap.get("tableLable").toString());
		
		List<Map<String,Object>> sList = new ArrayList<Map<String,Object>>();
		
	    for(int i=0,len=objs.length;i<len;i++){
	    	Map<String, Object> tmp= new HashMap<String, Object>();
	    	String str = keys.get(chartItems[i]);
	    	str = str.replace("*","");
	    	tmp.put("name",str);
	    	Object[] obj=objs[i];
	    	
	    	for(int k=0;k<obj.length;k++){
	    		Map<String, Object> ds = new HashMap<String, Object>();
				ds.put("y", obj[k]) ;
				ds.put("color", HChart.colors[k]) ;
	    		obj[k]= ds ;
	    	}
	    	tmp.put("data", obj);
	    	sList.add(tmp);
	    }
	    String title = subMap.get("subName").toString();
	    String unit = data.get("unit").toString();
	    BasicColumn column = new BasicColumn(BasicColumn.CHART_TYPE, title,xAxis, "", unit, sList);
		return column;
	}
	
	public static Map<String,String> getFieldValue(String keys,String values){
		Map<String,String> map =new HashMap<String,String>();
		String [] _keys = keys.split(",");
		String [] _values = values.split(",");
		for(int i=0,len=_keys.length;i<len;i++){
			map.put(_keys[i], _values[i]);
		}
		return map;
	}
	
	public String createJsonBasicLine(BasicLine chart,int width,int height){
		String str =
		"{" +
		"    chart : {" + 
		"        width : "+width+"," + 
		"        height : "+height + 
		"    }," + 
		"    title : {" + 
		"        text : ''" + 
		"    }," + 
		"    credits : {" + 
		"        enabled : false" + 
		"    }," + 
		"    legend : {" + 
		"        enabled : false" + 
		"    }," + 
		"    xAxis : "+JSON.toJSON(chart.getxAxis())+"," + 
		"    yAxis : {" + 
		"        title : {" + 
		"            text : ''" + 
		"        }," + 
		"        plotLines : [{" + 
		"                value : 0," + 
		"                width : 1," + 
		"                color : '#808080'" + 
		"            }" + 
		"        ]" + 
		"    }," + 
		"    colors : "+colors+"," + 
		"    series : "+JSON.toJSON(chart.getSeries())+ 
		"}";
		return str ;
	}
	
	public BasicLine createBasicLine(Map<Object,Object> data){
		Map<Object, Object> subMap = (Map<Object, Object>) data.get("subMap");
		List<Map> result = (List<Map>) data.get("result");
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		Object [][] objs = new Object[chartItems.length][result.size()];
		
		String [] categories = new String[result.size()];
		for(int i=0,len=result.size();i<len;i++){
			Map map = result.get(i);
			categories[i] = map.get(category).toString();
			for(int j=0,jlen=chartItems.length;j<jlen;j++){
				objs[j][i]= StringUtil.toDouble(StringUtil.toString(map.get(chartItems[j]),"0"));
			}
		}
		Map xAxis = new HashMap();
		xAxis.put("categories", categories);
		
		Map<String,String> keys = getFieldValue(subMap.get("tableFiled").toString(), subMap.get("tableLable").toString());
		
		List<Map> sList = new ArrayList<Map>();
	    for(int i=0,len=objs.length;i<len;i++){
	    	Map tmp= new HashMap();
	    	tmp.put("name", keys.get(chartItems[i]));
	    	tmp.put("data", objs[i]);
	    	sList.add(tmp);
	    }
	    String title = subMap.get("subName").toString();
	    String unit = data.get("unit").toString();
	    BasicLine line = new BasicLine(title, xAxis, "", unit, sList);
		return line;
	}
	
	public Spline createSpline(Map<Object,Object> data){
		ReportUiUtil.mapFormat(data,null,null);
		Map<Object, Object> subMap = (Map<Object, Object>) data.get("subMap");
		List<Map> result = (List<Map>) data.get("result");
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		int sizei=result.size()<1?1:result.size();
		Object [][] objs = new Object[chartItems.length][sizei];
		
		String [] categories = new String[sizei];
		Map<String,Object> imap = null;
		for(int i=0,len=result.size();i<len;i++){
			Map map = result.get(i);
			categories[i] = map.get(category).toString();
			Date tmp = StringUtil.toDate(categories[i], ReportUiConfig.dFormat1);
			for(int j=0,jlen=chartItems.length;j<jlen;j++){
				imap = new HashMap<String,Object>();
				imap.put("x", tmp.getTime());
				imap.put("y", map.get(chartItems[j]));
				objs[j][i]= imap;
			}
		}
		Map<String,String> keys = getFieldValue(subMap.get("tableFiled").toString(), subMap.get("tableLable").toString());
		
		List<SubItem> sList = new ArrayList<SubItem>();
		SubItem item = null;
	    for(int i=0,len=objs.length;i<len;i++){
	    	item = new SubItem(Spline.CHART_TYPE,keys.get(chartItems[i]),objs[i]);
	    	sList.add(item);
	    }
	    String title = subMap.get("subName").toString();
	    String unit = data.get("unit").toString();
	    Spline spline = new Spline(title, unit, sList);
		return spline;
	}
	
	public String createJsonPie(Pie chart,int width,int height) {
		String str = 
		"{" +
		"    chart : {" + 
		"        borderColor : '#6699FF'," + 
		"        borderWidth : 1," + 
		"        plotShadow : false," + 
		"        width : "+width+"," + 
		"        height : "+height+"," + 
		"        type : 'pie'," + 
		"        options3d : {" + 
		"            alpha : 5," + 
		"            beta : 15" + 
		"        }" + 
		"    }," + 
		"    title : {" + 
		"        text : ''" + 
		"    }," + 
		"    credits : {" + 
		"        enabled : false" + 
		"    }," + 
		"    legend : {" + 
		"        enabled : false" + 
		"    }," + 
		"    plotOptions : {" + 
		"        pie : {" + 
		"            allowPointSelect : true," + 
		"            cursor : 'pointer'," + 
		"            dataLabels : {" + 
		"                enabled : true," + 
		"                color : '#000000'," + 
		"                connectorColor : '#000000'," + 
		"                formatter : function(){" + 
		"                  return '' + this.point.name + ',' + this.y + '(' + this.percentage.toFixed(2) + '%)'" + 
		"                }" + 
		"            }" + 
		"        }" + 
		"    }," + 
		"    colors : "+colors+"," + 
		"    series : ["+JSON.toJSON(chart.getSeries())+"]" + 
		"}";
		return str ;
	}
	
	public String createJsonSpline(Spline chart,int width,int height){
		StringBuffer sb = new StringBuffer();
		sb.append("{chart: {").append(Rn)
		.append("borderColor:'#6699FF',borderWidth:1,").append(Rn)
		.append(s4).append("defaultSeriesType: 'spline',").append(Rn)
		.append(s6).append("width:").append(width).append(",").append(Rn)
		.append(s6).append("height:").append(height).append(",").append(Rn)
		.append("spacingTop: 10,").append(Rn)
		.append(s4).append("marginRight:10").append(Rn)
		.append(s4).append("},").append(Rn)
		.append(s4).append("title: { text: ''},").append(Rn)
		.append(s6).append("credits : { enabled : false	},").append(Rn)
		.append(s6).append("xAxis: {categories: ").append(Rn)
		.append(s4).append(" ").append(JSON.toJSON(chart.getXcategories())).append(Rn)
		.append(s4).append(s4).append("},").append(Rn)
		.append(s4).append("yAxis : {min:0,title : {text : ''}},").append(Rn)
		.append(s4).append("colors:").append(colors).append(",").append(Rn)
		.append(s4).append("series:[").append(Rn);
		if (!GlobalUtil.isNullOrEmpty(chart.getSeries())) {
			for (int i = 0; i < chart.getSeries().size(); i++) {
				SubItem subItem = chart.getSeries().get(i);
				sb.append(Rn)
						.append("{")
						.append("name:'")
						.append(subItem.getName())
						.append("',")
						.append(Rn)
						.append("data: [")
						.append("{y:")
						.append(Double.valueOf(GlobalUtil.isNullOrEmpty(subItem.getData())?"0":subItem.getData()[0].toString()))
						.append("}");
				for (int j = 1; j < subItem.getData().length; j++) {
					double value = Double.valueOf(subItem.getData()[j]
							.toString());
					sb.append(",").append(value);
				}
				sb.append("]}");
				if (i < chart.getSeries().size() - 1) {
					sb.append(",").append(Rn);
				}
			}
		}
		sb.append(Rn);
		sb.append("]}");
		return sb.toString();
	}
	
	public Pie createPie(Map<Object,Object> data){
		Map<Object, Object> subMap = (Map<Object, Object>) data.get("subMap");
		List<Map> result = (List<Map>) data.get("result");
		String chartItem =StringUtil.toString(subMap.get("chartItem"), "");
		String category = subMap.get("category").toString();
		int sizei=result.size()<1?1:result.size();
		Object [][] objs = new Object[sizei][2];
		
		for(int i=0,len=result.size();i<len;i++){
			Map<?, ?> map = result.get(i);
			objs[i][0] = map.get(category);
			objs[i][1] = map.get(chartItem);
		}
	    HashMap<String, Object> series = new HashMap<String, Object>();
	    series.put("type", Pie.CHART_TYPE);
	    series.put("data", objs);
		
	    String title = subMap.get("subName").toString();
	    String unit = data.get("unit").toString();
	    Pie pie = new Pie(Pie.CHART_TYPE, title, unit, series);
		return pie;
	}
	
	public ChartTable reformingChartTable(Map<Object,Object> data){
		Map<Object, Object> subMap = (Map<Object, Object>) data.get("subMap");
		String url = data.get("url").toString();
		List<Map> result = (List<Map>) data.get("result");
		int nowSize=result.size();
		int sumPage=Integer.valueOf(data.get("sumPage")==null?"0":data.get("sumPage").toString());
		ReportUiUtil.chartTableFormat(result, subMap);
		data.put("sumPage", sumPage-(nowSize-result.size()));
		String unit = data.get("unit").toString();
		String category = subMap.get("category").toString();
		String tableLable = subMap.get("tableLable").toString().replace("*", unit);
		String [] header = tableLable.split(",");
		String[] fileds = subMap.get("tableFiled").toString().split(",");
		String logQueryUrl=null;
		String eventQueryUrl=null;
		Map frontEndParams=null;
		String logQueryCondition=null;
		if (!GlobalUtil.isNullOrEmpty(subMap.get("logQueryCondition"))) {
			if (data.containsKey("logQueryUrl") && !GlobalUtil.isNullOrEmpty(data.get("logQueryUrl"))) {
				logQueryUrl=data.get("logQueryUrl").toString();
			}
			if (data.containsKey("eventQueryUrl") && !GlobalUtil.isNullOrEmpty(data.get("eventQueryUrl"))) {
				eventQueryUrl=data.get("eventQueryUrl").toString();
			}
			
			frontEndParams=(Map)data.get("frontEndParams");
			logQueryCondition=subMap.get("logQueryCondition").toString();
		}
		Map<String, String>shareConditionMap=null;
		Map<String, String>logQueryNocommonMap=null;
		boolean isqueryLog=false;
		boolean onlyTable=false;
		boolean isBaseReport=false;
		if (!GlobalUtil.isNullOrEmpty(logQueryCondition)) {
			
			if (!GlobalUtil.isNullOrEmpty(frontEndParams)){
				isqueryLog=true;
				if(!GlobalUtil.isNullOrEmpty(frontEndParams.get("onlyTable"))){
					onlyTable="onlyTable".equalsIgnoreCase(frontEndParams.get("onlyTable").toString());
				}
				isBaseReport="baseReport".equals(frontEndParams.get("reportType"));
			}
			logQueryNocommonMap=new HashMap<String, String>();
			String logQueryTemp=null;
			String shareCondTemp=null;
			if (logQueryCondition.indexOf("#")>0) {
				String [] templogQuery=logQueryCondition.split("#");
				logQueryTemp=templogQuery[0];
				shareCondTemp=templogQuery[1];
			}else {
				logQueryTemp=logQueryCondition;
			}
			if (logQueryTemp.indexOf("@")>0) {
				String [] logQueryAll=logQueryTemp.split("@");
				for (String string : logQueryAll) {
					String [] conditionMap=string.split(":::");
					String keyString=conditionMap[0];
					String valueString=conditionMap[1];
					logQueryNocommonMap.put(keyString, valueString);
				}
			}else {
				String [] conditionMap=logQueryTemp.split(":::");
				String keyString=conditionMap[0];
				String valueString=conditionMap[1];
				logQueryNocommonMap.put(keyString, valueString);
			}
			if (!GlobalUtil.isNullOrEmpty(shareCondTemp)) {
				shareConditionMap=new HashMap<String, String>();
				if (shareCondTemp.indexOf("@")>0) {
					String [] logQueryAll=shareCondTemp.split("@");
					for (String string : logQueryAll) {
						String [] conditionMap=string.split(":::::");
						String keyString=conditionMap[0];
						String valueString=conditionMap[1];
						shareConditionMap.put(keyString, valueString);
					}
				}else {
					String [] conditionMap=shareCondTemp.split(":::::");
					String keyString=conditionMap[0];
					String valueString=conditionMap[1];
					shareConditionMap.put(keyString, valueString);
				}
			}
		}
		List<ColumnData> cList = new ArrayList<ColumnData>();
		int len = fileds.length;
		StringBuffer sb = new StringBuffer();
		boolean flag = ReportUiUtil.isSystemLog(subMap);
		for(Map map:result){
			TreeMap treeMap = new TreeMap();
			if (isqueryLog) {
				for (Map.Entry<String, String> entry : logQueryNocommonMap.entrySet()) {
					LogSearchObject logSearchObject=new LogSearchObject();
					String logQueryConditionValue=entry.getValue();
					
					StringBuffer conditionName=new StringBuffer();
					StringBuffer queryContent=new StringBuffer();
					StringBuffer operator=new StringBuffer();
					StringBuffer queryType=new StringBuffer();
					
					setProCondition(conditionName,queryContent,operator,queryType,logQueryConditionValue,logSearchObject,map);
					for (Map.Entry<String, String> entryShare : shareConditionMap.entrySet()){
						String shareConditionValue=entryShare.getValue();
						setProCondition(conditionName,queryContent,operator,queryType,shareConditionValue,logSearchObject,map);
					}
					String dvcType=frontEndParams.get("dvcType")==null?"":frontEndParams.get("dvcType").toString();
					logSearchObject.setDeviceType(dvcType.replace("Comprehensive", ""));
					logSearchObject.setNodeId(frontEndParams.get("nodeId")+"");
					String sTime=frontEndParams.get("sTime")+"";
					String eTime=frontEndParams.get("eTime")+"";
					logSearchObject.setQueryStartDate(sTime);
					logSearchObject.setQueryEndDate(eTime);
					if (conditionName.length()>3) {
						String conditionNameS=conditionName.substring(0, conditionName.length()-3);
						String queryContentS=queryContent.substring(0, queryContent.length()-3);
						String operatorS=operator.substring(0, operator.length()-3);
						String queryTypeS=queryType.substring(0, queryType.length()-3);
						
						if (map.containsKey("risk")) {
							queryContentS=resetQueryContent(map,conditionName,queryContent);
						}
						
						logSearchObject.setConditionName(conditionNameS);
						logSearchObject.setQueryContent(queryContentS);
						logSearchObject.setOperator(operatorS);
						logSearchObject.setQueryType(queryTypeS);
					}
					if (map.containsKey("risk")) {
						if ("非常危险".equals(map.get("risk"))
								|| "高危险".equals(map.get("risk"))
								|| "一般危险".equals(map.get("risk"))
								|| "低危险".equals(map.get("risk"))
								|| "无危险".equals(map.get("risk"))) {
							treeMap.put(entry.getKey()+"LogSearchObject", logSearchObject);
						}
					}else {
						treeMap.put(entry.getKey()+"LogSearchObject", logSearchObject);
					}
					
				}
				if (null !=eventQueryUrl) {
					treeMap.put("eventQueryUrl", eventQueryUrl);
				}
				if (null !=logQueryUrl) {
					treeMap.put("logQueryUrl", logQueryUrl);
				}
			}
			
			sb.replace(0, sb.length(),url);
			if (!"DVC_ADDRESS".equalsIgnoreCase(category)) {
				String valueString="";
				Object obj= null==map.get(category)?"":map.get(category);
				valueString=obj.toString();
				try {
					if (!ReportUiUtil.checkStringAll16Num(obj.toString()) && !ReportUiUtil.isEnglishOrNumber(obj.toString())) {
						valueString=URLEncoder.encode(obj.toString(), "UTF-8");
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(category)
				.append("***").append(valueString);
			}
			if ("DVC_ADDRESS".equalsIgnoreCase(category) && sb.indexOf(ReportUiConfig.dvcaddress)<0) {
				sb.append("&").append(ReportUiConfig.dvcaddress).append("=").append(map.get(category));
			}
			for(int i=0;i<len;i++){
				Object value = map.get(fileds[i]) ;
				if(flag){
					if(category.equalsIgnoreCase("ALLLOGTYPE")||category.equalsIgnoreCase("TYPE")){
						value = ReportUiUtil.getDeviceTypeName(StringUtil.toString(value,""), Locale.getDefault());
					}
				}
				if(value instanceof Date){
					treeMap.put(fileds[i],StringUtil.dateToString((Date)value, "yyyy-MM-dd HH:mm:ss"));
				}else{
					treeMap.put(fileds[i], value);
				}
			}
			
			ColumnData column = new ColumnData(treeMap,sb.toString(),0);
			cList.add(column);
		}
		boolean isDrill = false;
		int chartLink = Integer.valueOf(StringUtil.toString(subMap.get("chartLink"),"0"));
		if(chartLink>0){
			isDrill = true;
		}
		ChartTable table = new ChartTable(isDrill,header,fileds,data.get("moreUrl").toString(), cList,StringUtil.toInt(StringUtil.toString(data.get("sumPage"), "0")));
		if (isqueryLog && !isBaseReport){
			table.setSubSummarize(subMap.get("subSummarize")+"");
		}
		if (onlyTable) {
			table.putAttrsMap("onlyTable", onlyTable);
		}
		return table;
	}
	
	private void setProCondition(StringBuffer conditionName,StringBuffer queryContent
			,StringBuffer operator,StringBuffer queryType,String logQueryConditionValue
			,LogSearchObject logSearchObject,Map map){
		if (!GlobalUtil.isNullOrEmpty(logQueryConditionValue)) {
			if (logQueryConditionValue.indexOf("!:")>0) {
				String[] sonCondition=logQueryConditionValue.split("!:");
				for (String string2 : sonCondition) {
					if (string2.indexOf("=?")>0) {
						String[]sonStrings=string2.split("=");
						string2=string2.replace("?", ""+map.get(sonStrings[0]));
					}
					String[]sonStrings=string2.split("=");
					if (sonStrings.length<2) {
						continue;
					}
					if ("dvcAddress".equalsIgnoreCase(sonStrings[0])
							||"ip".equalsIgnoreCase(sonStrings[0])
							||"DVC_ADDRESS".equalsIgnoreCase(sonStrings[0])) {
						logSearchObject.setHost(sonStrings[1]);
					}else if ("group".equalsIgnoreCase(sonStrings[0])) {
						logSearchObject.setGroup(sonStrings[1]);
					}else if (!"REPORT_QUERY_TYPE".equalsIgnoreCase(sonStrings[0])){
						if ("risk".equalsIgnoreCase(sonStrings[0])
								||"priority".equalsIgnoreCase(sonStrings[0])) {
							conditionName.append("PRIORITY").append("***");
							queryContent.append(sonStrings[1]).append("***");
							operator.append("等于").append("***");
							queryType.append("String").append("***");
						}else if ("SRC_ADDRESS".equalsIgnoreCase(sonStrings[0])
								||"DEST_ADDRESS".equalsIgnoreCase(sonStrings[0])){
							conditionName.append(sonStrings[0]).append("***");
							queryContent.append(sonStrings[1]).append("***");
							operator.append("等于").append("***");
							queryType.append("ip").append("***");
						}else if ("SRC_PORT".equalsIgnoreCase(sonStrings[0])
								||"DEST_PORT".equalsIgnoreCase(sonStrings[0])){
							conditionName.append(sonStrings[0]).append("***");
							queryContent.append(sonStrings[1]).append("***");
							operator.append("等于").append("***");
							queryType.append("int").append("***");
						}else {
							conditionName.append(sonStrings[0]).append("***");
							queryContent.append(sonStrings[1]).append("***");
							operator.append("等于").append("***");
							queryType.append("String").append("***");
						}
					}
					
				}
			}else {
				if (logQueryConditionValue.indexOf("=?")>0) {
					String [] sonC=logQueryConditionValue.split("=");
					logQueryConditionValue=logQueryConditionValue.replace("?", ""+map.get(sonC[0]));
				}
				String[]sonStrings=logQueryConditionValue.split("=");
				if (sonStrings.length<2) {
					return;
				}
				if ("dvcAddress".equalsIgnoreCase(sonStrings[0])
						||"ip".equalsIgnoreCase(sonStrings[0])
						||"DVC_ADDRESS".equalsIgnoreCase(sonStrings[0])) {
					logSearchObject.setHost(sonStrings[1]);
				}else if ("group".equalsIgnoreCase(sonStrings[0])) {
					logSearchObject.setGroup(sonStrings[1]);
				}else if (!"REPORT_QUERY_TYPE".equalsIgnoreCase(sonStrings[0])){
					if ("risk".equalsIgnoreCase(sonStrings[0])
							||"priority".equalsIgnoreCase(sonStrings[0])) {
						conditionName.append("PRIORITY").append("***");
						queryContent.append(sonStrings[1]).append("***");
						operator.append("等于").append("***");
						queryType.append("String").append("***");
					}else if ("SRC_ADDRESS".equalsIgnoreCase(sonStrings[0])
							||"DEST_ADDRESS".equalsIgnoreCase(sonStrings[0])){
						conditionName.append(sonStrings[0]).append("***");
						queryContent.append(sonStrings[1]).append("***");
						operator.append("等于").append("***");
						queryType.append("ip").append("***");
					}else if ("SRC_PORT".equalsIgnoreCase(sonStrings[0])
							||"DEST_PORT".equalsIgnoreCase(sonStrings[0])){
						conditionName.append(sonStrings[0]).append("***");
						queryContent.append(sonStrings[1]).append("***");
						operator.append("等于").append("***");
						queryType.append("int").append("***");
					}else {
						conditionName.append(sonStrings[0]).append("***");
						queryContent.append(sonStrings[1]).append("***");
						operator.append("等于").append("***");
						queryType.append("String").append("***");
					}
				}
			}
		}
	}
	
	private String resetQueryContent(Map map,StringBuffer conditionName,StringBuffer queryContent){
		String resultString=null;
		if (map.containsKey("risk")) {
			if (conditionName.indexOf("PRIORITY")>-1) {
				String []conditionTempS=conditionName.toString().split("\\*\\*\\*");
				int wet=0;
				boolean isf=false;
				for (int i = 0; i < conditionTempS.length; i++) {
					if ("PRIORITY".equals(conditionTempS[i])) {
						wet=i;
						isf=true;
						break;
					}
				}
				if (isf) {
					String []contentTempS=queryContent.toString().split("\\*\\*\\*");
					queryContent=new StringBuffer();
					if ("非常危险".equals(map.get("risk"))) {
						contentTempS[wet]="4";
					}else if("高危险".equals(map.get("risk"))) {
						contentTempS[wet]="3";
					}else if("一般危险".equals(map.get("risk"))) {
						contentTempS[wet]="2";
					}else if("低危险".equals(map.get("risk"))) {
						contentTempS[wet]="1";
					}else if("无危险".equals(map.get("risk"))) {
						contentTempS[wet]="0";
					}
					for (int i = 0; i < contentTempS.length; i++) {
						queryContent.append(contentTempS[i]).append("***");
					}
				}
			}
		}
		resultString=queryContent.substring(0, queryContent.length()-3);
		return resultString;
	}
	
	public synchronized Map<String,Object> createChart(int type,Map<Object,Object> data){
		String ctype="";
	   switch(type){
	   case 1 :
		   chart = createBasicColumn(data);
		   ctype = BasicColumn.CHART_TYPE;
		   break;
	   case 4 :
		   chart = createSpline(data);
		   ctype = Spline.CHART_TYPE;
	   break;
	   case 5 :
		   chart = createPie(data);
		   ctype = Pie.CHART_TYPE;
	   }
	   
		table = reformingChartTable(data);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("type", ctype);
		map.put("chart",chart);
		map.put("table", table);
		return map;
	}
	
	public String CreateJsonChart(Object obj){
		if(obj instanceof BasicColumn) {
			return createJsonBasicColumn((BasicColumn) obj, 640,330);
		}else if(obj instanceof BasicLine) {
			return createJsonBasicLine((BasicLine) obj,640,330);
		}else if (obj instanceof Pie) {
			return createJsonPie((Pie) obj,640,330);
		}else if (obj instanceof BasicArea) {
			return createJsonBasicArea((BasicArea) obj,640,330);
		}else if(obj instanceof Spline) {
			return createJsonSpline((Spline) obj,640,330);
		}
		return null;
	}
	
	public String createChartPicuture(String chartOption){
		if(chartOption==null){
//			chartOption ="{chart: { defaultSeriesType: 'column', width: 360, height: 216},title: {  text: '源地址流量排行'},xAxis: {\"categories\":[\"192.168.75.10\",\"192.168.75.60\",\"192.168.75.20\",\"192.168.75.30\",\"192.168.75.40\"]},yAxis: {  min: 0,  title: {    text: ' ((KB))'  }},legend: {  layout: 'vertical',  backgroundColor: '#FFFFFF',  align: 'left',  verticalAlign: 'top',  x: 100,  y: 70,  floating: true,  shadow: true},tooltip: {  formatter: function () {    return '' + this.x + ': ' + this.y + '(KB)';  }},plotOptions: {  column: {    pointPadding: 0.2,    borderWidth: 0  }},series: [{\"data\":[348768,186513,116990,37877,21166],\"name\":\"流入*\"},{\"data\":[378058,165885,121586,28614,32662],\"name\":\"流出*\"}]}";
			String path = ReportUiUtil.getSysPath();
			path = path.substring(0,path.indexOf("WEB-INF"))+"img"+File.separatorChar+"report"+File.separatorChar+"nodata.png";
			return path;
		}
		chartOption = chartOption.replace("null", "0"); 
		String path = System.getProperty("java.io.tmpdir");
		String fileName = System.currentTimeMillis() + "_" + Math.round(Math.random()*1000) ;
		HighchartsExporter<String> pngFromJsonExporter = ExportType.png.createJsonExporter();
		pngFromJsonExporter.export(chartOption, null, new File (path, fileName+".png"));
		return path+File.separatorChar+fileName+".png";
	}
	
	public static CreateChartFactory getInstance(){
		if(instance==null){
			synchronized (CreateChartFactory.class) {
				if(instance==null){
					instance = new CreateChartFactory() ;
				}
			}
		}
		return instance ;
	}

}
