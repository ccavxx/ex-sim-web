package com.topsec.tsm.sim.newreport.chart.echart;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import com.alibaba.fastjson.JSON;
import com.topsec.tsm.sim.newreport.util.ExportDocumentUtil;
import com.topsec.tsm.sim.newreport.util.QueryUtil;
import com.topsec.tsm.sim.newreport.util.ResultOperatorUtils;
import com.topsec.tsm.sim.report.util.HtmlAndFileUtil;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

/**
 * @ClassName: EChartImageFactory
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年12月4日上午10:53:21
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class EChartImageFactory {
	private static EChartImageFactory instance= new EChartImageFactory() ;
	private EChartImageFactory() {
	}
	public static final String RHINO_EXPORT = "rhino" ;
	public static final String PLANTOMJS_EXPORT = "plantomjs" ;
	private static final AtomicLong fileCounter = new AtomicLong(0);
	private static final String PLANTOMJS_PATH = new File("phantomjs").getAbsolutePath() ;
	
	public static EChartImageFactory getInstance(){
		return instance ;
	}
	public static void clearImageFolder(){
		HtmlAndFileUtil.clearPath(PLANTOMJS_PATH+"/tmp/");
	}
	public String createChartImage(String chartOption){
		if(chartOption==null){
			String path = ReportUiUtil.getSysPath();
			path = path.substring(0,path.indexOf("WEB-INF"))+"img"+File.separatorChar+"report"+File.separatorChar+"nodata.png";
			return path;
		}
		chartOption = chartOption.replace("null", "0"); 
		String path = System.getProperty("java.io.tmpdir");
		String fileName = System.currentTimeMillis() + "_" + Math.round(Math.random()*1000) ;
		
		return path+File.separatorChar+fileName+".png";
	}
	
	public static String exportUseRhino(String jsonData){
		String imagePath = EChartImageFactory.getInstance().createChartImage(jsonData) ;
		return imagePath ;
	}
	
	public static String exportUsePhantomjs(String jsonData)throws Exception{
		String jsFileName = "/tmp/"+fileCounter.addAndGet(1)+".js" ;
		File jsFile = new File(PLANTOMJS_PATH+jsFileName) ;
		FileUtils.writeStringToFile(jsFile, jsonData,"UTF-8") ;
		try {
			String outputFileName = PLANTOMJS_PATH+"/tmp/"+fileCounter.addAndGet(1) +".png" ;
			String fileName = SystemUtils.IS_OS_WINDOWS ? "phantomjs.exe" : "phantomjs" ; 
			String command = PLANTOMJS_PATH+"/" + fileName +
							 " phantomjs/echarts-convert.js" +
					         " -infile " + jsFile.getAbsolutePath() + 
					         " -outfile " + outputFileName+ 
					         " -width " + 600+ 
					         " -height  " + 450;
			Process pro = Runtime.getRuntime().exec(command) ;
			pro.waitFor() ;
			return outputFileName ;
		}finally{
			if (jsFile != null && jsFile.exists()) {
				FileUtils.forceDelete(jsFile) ;
			}
		}
	}
	private static String getPieOption(Map mapUnit){
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []categorysCNName=(String [])dataStructureMap.get("categorysCNName");
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
    	if(colon>-1){
    		showType=showType.substring(0,colon);
    	}
    	
    	long maxVal=ExportDocumentUtil.getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		
		for(int i=0;i<categorysCNName.length;i++){
			String caName = categorysCNName[i] ;
		}
		for(int i=0;i<statisticalCNName.length;i++){
			String stName = statisticalCNName[i] ;
			if (null != unit) {
				stName=stName+"("+unit+")";
				statisticalCNName[i]=stName;
			}
		}
		
		StringBuffer option=new StringBuffer();
		option.append("{")
		.append("	title : {")
		.append("		x : 'center',")
		.append("		text : '").append(mapUnit.get("subReportName")).append("'").append(",")
		.append("		subtext : '").append(mapUnit.get("subDescribe")).append("'")
		.append("	},")
		.append("	tooltip : {")
		.append("		trigger : 'item' ,")
		.append("		formatter : '{a} <br/>{b} : {c} ({d}%)'")
		.append("	},")
		.append("	legend : { ")
		.append("		orient : 'vertical',")
		.append("		x : 'left',")
		.append("		data : ").append(JSON.toJSON(ExportDocumentUtil.getCategorys(mapUnit)))
		.append("	}, ")
		.append("	calculable : true,")
		.append("	series : ").append(seriesPieJsonString(statisticalCNName,statisticalsName,statisticalType,mapDatList,categorysName[0],unit))
		.append("}")
		;
		return option.toString();
	}
	private static StringBuffer seriesPieJsonString(String[]statisticalCNName,String []statisticalsName,String []statisticalType,List<?> dats,String catName,String unit){
		
		StringBuffer series=new StringBuffer("[");
		for (int i = 0; i < statisticalCNName.length; i++) {
			series.append(getPieSeriesUnit(statisticalCNName[i],dats,statisticalsName[i],catName,statisticalType[i],unit));
		}
		series.append("]");
		return series;
	}
	private static StringBuffer getPieSeriesUnit(String sername,List<?> dats,String staName,String catName,String type,String unit){
		
		StringBuffer series=new StringBuffer("		{");
		series.append("			name :'").append(sername).append("'").append(",")
		.append("			type : 'pie',")
		.append("			radius : '55%',")
		.append("			center : ['50%', '60%'],")
		.append("			data : ").append(smallPieSeries(dats,catName,staName,type,unit))
		.append("		}")
		;
		return series;
	}
	private static StringBuffer smallPieSeries(List<?> dats,String catName,String staName,String type,String unit){
		StringBuffer sb=new StringBuffer("			[");
		for (int i = 0; i < dats.size(); i++) {
			Map map=(Map)dats.get(i);
			Object cat=map.get(catName);
			cat=null ==cat?"UNKNOW":cat;
			if ("PRIORITY".equalsIgnoreCase(catName) 
					|| "RISK".equals(catName)) {
				if (cat.toString().indexOf("危险")==-1) {
					int val=Integer.valueOf(cat.toString());
					cat=ResultOperatorUtils.riskCnName(val);
				}
			}
			Object sta=map.get(staName);
			sta= null ==sta?"0":sta;
			if (null != unit) {
				long val=Long.valueOf(sta.toString());
				double vald=0;
				if ("FLOW_NO".equalsIgnoreCase(type) ){
					vald=ResultOperatorUtils.flowOperater(val, unit);
				}else if ("COUNT_NO".equalsIgnoreCase(type)) {
					vald=ResultOperatorUtils.showNumberOperater(val, unit);
				}
				sta=vald;
			}
			sb.append("				{")
			.append("					name :'").append(cat).append("'").append(",")
			.append("					value :").append(sta)
			.append("				}")
			;
			if (i !=dats.size()-1) {
				sb.append(",");
			}
		}
		sb.append("			]");
		return sb;
	}
	private static String getRainbowOption(Map mapUnit,String imgtype){
		Map<String,Object> statisticalMap=ExportDocumentUtil.getStatisticals(mapUnit);
		String unit=statisticalMap.get("unit").toString();
		Object dats=statisticalMap.get("datArray");
		StringBuffer option=new StringBuffer();
		option.append("{")
		.append("	title : {")
		.append("		x : 'center',")
		.append("		text : '").append(mapUnit.get("subReportName")).append("'").append(",")
		.append("		subtext : '").append(mapUnit.get("subDescribe")).append("'")
		.append("	},")
		.append("	tooltip : {")
		.append("		trigger : 'item' ")
		.append("	},")
		.append("	calculable : true,")
		.append("	grid : {")
		.append("		borderWidth : 0,")
		.append("		y : 80,")
		.append("		y2 : 60")
		.append("	},")
		.append("	xAxis : [{")
		.append("			type : 'category',")
		.append("			show : true,")
		.append("			data : ").append(JSON.toJSON(ExportDocumentUtil.getCategorys(mapUnit)))
		.append("		}")
		.append("	],")
		.append("	yAxis : [{")
		.append("			type : 'value',")
		.append("			axisLabel : {")
		.append("				formatter : '{value} ' + '").append(unit).append("'")
		.append("			},")
		.append("			show : true")
		.append("		}")
		.append("	],")
		.append("	series : [{")
		.append("			name : '").append(statisticalMap.get("statisticalCNName")).append("' ,")
		.append("			type : '").append(imgtype).append("'").append(",")
		.append("			barMaxWidth : 28,")
		.append("			itemStyle : {")
		.append("				normal : {")
		.append("					color : function (params) {")
		.append("						var colorList = ['#FF7F50', '#87CEFA', '#C1232B', '#B5C334', '#FCCE10','#E87C25', '#27727B', '#FE8463', '#9BCA63',")
		.append("							'#FAD860', '#F3A43B', '#60C0DD', '#D7504B','#C6E579', '#F4E001', '#F0805A', '#26C0C0'];")
		.append("						return colorList[params.dataIndex];")
		.append("					}")
		.append("				}")
		.append("			},")
		.append("			markPoint : {")
		.append("				data : [{")
		.append("						type : 'max',")
		.append("						name : '最大值'")
		.append("					}, {")
		.append("						type : 'min',")
		.append("						name : '最小值'")
		.append("					}")
		.append("				]")
		.append("			},")
		.append("			markLine : {")
		.append("				data : [{")
		.append("						type : 'average',")
		.append("						name : '平均值'")
		.append("					}")
		.append("				]")
		.append("			},")
		.append("			data : ").append(JSON.toJSON(dats))
		.append("		}")
		.append("	]")
		.append("}")
		;
		return option.toString();
	}
	private static String getStandardOption(Map mapUnit,String imgtype){
		Map<String,Object> seriesMap=ExportDocumentUtil.getSeries(mapUnit);
		String unit=seriesMap.get("unit").toString();
		List<Map<String, Object>> datMaps=(List<Map<String, Object>>)seriesMap.get("datList");
		StringBuffer option=new StringBuffer();
		option.append("{")
		.append("	title : {")
		.append("		x : 'center',")
		.append("		text : '").append(mapUnit.get("subReportName")).append("'").append(",")
		.append("		subtext : '").append(mapUnit.get("subDescribe")).append("'")
		.append("	},")
		.append("	tooltip : {")
		.append("		trigger : 'axis' ")
		.append("	},")
		.append("	legend : { ")
		.append("		y : 'bottom',")
		.append("		data : ").append(JSON.toJSON(ExportDocumentUtil.getLegend(mapUnit)))
		.append("	}, ")
		.append("	calculable : true,")
		.append("	xAxis : [{")
		.append("			type : 'category',")
		.append("			data : ").append(JSON.toJSON(ExportDocumentUtil.getCategorys(mapUnit)))
		.append("		}")
		.append("	],")
		.append("	yAxis : [{")
		.append("			type : 'value',")
		.append("			axisLabel : {")
		.append("				formatter : '{value} ' + '").append(unit).append("'")
		.append("			}")
		.append("		}")
		.append("	],")
		.append("	series : ").append(seriesJsonString(imgtype,datMaps))
		.append("}")
		;
		return option.toString();
	}
	public static StringBuffer seriesJsonString(String imgtype,List<Map<String, Object>> datMaps){
		
		StringBuffer series=new StringBuffer("[");
		for (int i = 0; i < datMaps.size(); i++) {
			Map<String, Object> map=datMaps.get(i);
			int pos=0;
			for (Map.Entry<String, Object> object : map.entrySet()) {
				String sername=object.getKey();
				Object []valdat=(Object [])object.getValue();
				series.append(seriesUnit(imgtype,sername,valdat));
				if (pos++ !=map.size()-1) {
					series.append(",");
				}
			}
		}
		series.append("]");
		return series;
	}
	private static StringBuffer seriesUnit(String imgtype,String sername,Object []valdat){
		boolean ismooth="line".equals(imgtype)?true:false;
		StringBuffer series=new StringBuffer("{");
		series.append("	name : '").append(sername).append("'").append(",")
		.append("	type : '").append(imgtype).append("'").append(",")
		.append("	smooth : ").append(ismooth).append(",")
		.append("	data : ").append(JSON.toJSON(valdat)).append(",")
		.append("	markPoint : {")
		.append("		data : [{")
		.append("				type : 'max',")
		.append("				name : '最大值'")
		.append("			}, {")
		.append("				type : 'min',")
		.append("				name : '最小值'")
		.append("			}")
		.append("		]")
		.append("	},")
		.append("	markLine : {")
		.append("		data : [{")
		.append("				type : 'average',")
		.append("				name : '平均值'")
		.append("			}")
		.append("		]")
		.append("	}");
		if (!ismooth) {
			series.append(",")
			.append("	barMaxWidth : 28");
		}
		series.append("}");
		return series;
	}
	public static String getOptions(Map mapUnit){
//		StringBuffer option=new StringBuffer();
		Map<String,Object> seriesMap=ExportDocumentUtil.getChartTypeInfo(mapUnit);
		String showType=seriesMap.get("showType").toString();
		Object recom=seriesMap.get("recommend");
		String recommend= null==recom?null:recom.toString();
		if("TREND".equals(showType)){
    		recommend=(recommend==null)?"line":recommend;
    		return getStandardOption(mapUnit,recommend);
    	}else if("NOT_TREND".equals(showType)){
    		recommend=(recommend==null)?"bar":recommend;
    		if("standardbar".equals(recommend)
    				|| "rainbow".equals(recommend)
    				|| "bar".equals(recommend) ){
    			return getRainbowOption(mapUnit,"bar");
    		}else if("standardline".equals(recommend)){
    			return getStandardOption(mapUnit,recommend);
    		}else if("pie".equals(recommend)){
    			return getPieOption(mapUnit);
    		}else if("eventRiver".equals(recommend) ){
    			return getStandardOption(mapUnit,"line");
    		}else {
    			recommend=recommend.replace("standard", "");
    			recommend=recommend.replace("rainbow", "");
    			throw new RuntimeException("无效的图表类型:"+recommend);
    		}
    	}
//		return option.toString();
		return "{      title : {          text: '世界人口总量',          subtext: '数据来自网络'      },      tooltip : {          trigger: 'axis'      },      legend: {          data:['2011年', '2012年']      },      calculable : true,      xAxis : [          {              type : 'value',              boundaryGap : [0, 0.01]          }      ],      yAxis : [          {              type : 'category',              data : ['巴西','印尼','美国','印度','中国','世界人口(万)']          }      ],      series : [          {              name:'2011年',              type:'bar',              data:[18203, 23489, 29034, 104970, 131744, 630230]          },          {              name:'2012年',              type:'bar',              data:[19325, 23438, 31000, 121594, 134141, 681807]          }      ]  } ";
	}
}
