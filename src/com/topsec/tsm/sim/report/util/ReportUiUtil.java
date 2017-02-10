package com.topsec.tsm.sim.report.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.topsec.license.util.ChangePageEncode;
import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.common.common.TalSourceTypeFactory;
import com.topsec.tsm.sim.common.common.WebProperty;
import com.topsec.tsm.sim.report.bean.ReportBean;
import com.topsec.tsm.sim.report.bean.struct.BaseStruct;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

public class ReportUiUtil {
	private static Logger log = Logger.getLogger(ReportUiUtil.class);
	public static WebProperty property = new WebProperty();
	public static final String[] operationKeys={"BYTES","BYTE","TOTAL_BYTES","BYTES_IN","BYTES_OUT"};
	public static final String[] formatkeys={"TOTAL","OPCOUNT","COUNTS","opCount","opCount1","opCount2","opCount3","opCount4"};
	public static String ExpPaintDot = null;
	static {
		property.init("resource/application");
		JasperClasspath.setJasperClasspath();
		ExpPaintDot = getExpPaint();
	}
	/*
	 * 返回随机数
	 */
	public static int getRandom(int upLimit, int downLimit) {
		return (int) (Math.random() * (upLimit - downLimit)) + downLimit;
	}

	/* Bs Select END框 */
	private static final String algorithm = "MD5";

	// 年开始
	private static final int YEAR_START = 0;

	// 年结束
	private static final int YEAR_END = 4;

	// 月开始
	private static final int MONTH_START = 4;

	// 月结束
	private static final int MONTH_END = 6;

	// 天开始
	private static final int DAY_START = 6;

	// 天结束
	private static final int DAY_END = 8;

	public static String addSpan(String content) {
		return "<span class='add-on'>" + content + "</span>";
	}

	public static boolean isOs() {
		boolean reValue = false;
		String strOSname = System.getProperty("os.name");
		if (strOSname.indexOf(ReportUiConfig.OS2K) > -1
				|| strOSname.indexOf(ReportUiConfig.OSXP) > -1
				|| strOSname.toLowerCase().indexOf(ReportUiConfig.WINOS) > -1) {
			reValue = true;
		}
		return reValue;
	}
	/**
	 * 计算客户端图片宽度
	 * 
	 * @param int  screenWidth 客户端屏幕宽度
	 * @param Map  subMap  子报表参数
	 *            
	 * @return int 单个子报表宽度
	 */
	public static int calculateClientPictureWidth(int screenWidth,Map subMap,HttpServletRequest request){
		int width = ReportUiConfig.PicWidth;
		if(screenWidth>0){
			int column = 2;
			int chartType = Integer.parseInt(StringUtil.toString(subMap.get("chartType"), "0"));
			if(chartType==5){
				column=2;
			}else{
				Map<String, String> rowMap = (Map<String, String>) request.getAttribute("rowMap");
				String subRow = StringUtil.toString(subMap.get("subRow"));
				String[] columns = rowMap.get(subRow).split(",");
				column = columns.length;
			}
			width = (screenWidth-360)/column;
		}
		return width;
	}
	public static boolean isEnglishOrNumber(String charaString) {
		  return charaString.matches("^[a-zA-Z0-9%/\\-\\*. ]*");
	}
	public static boolean check16Num (String aNumber) {
	     return aNumber.matches("[a-f0-9A-F]*");
	}
	public static boolean checkStringAll16Num (String string) {
		if (null == string) {
			return false;
		}else {
			if (string.indexOf("%")>-1) {
				String[]numStrings=string.split("%");
				for (String str : numStrings) {
					if ("".equals(str)) {
						continue;
					}
					if (!check16Num(str)) {
						return false;
					}
				}
				return true;
			}
			return check16Num(string);
		}
	}
	/**
	 * 创建子主题
	 * @param subMap 查询参数
	 * @param width 宽
	 * @param url url
	 * @param talCategory 下钻参数
	 * @param top top
	 * @return
	 */
	public static String createSubTitle(Map subMap,String width,String url,String[] talCategory,int top){
		String repeatid="";
		int subRepeat=url.indexOf("subREPEAT_");
		if (subRepeat!=-1) {
			repeatid=url.split("subREPEAT_")[1];
			url=url.substring(0, subRepeat);
			url=url+"&chartTableId="+subMap.get("subId") +repeatid;
		}
		String Rn = "\r\n";
		String s4 = "    ";
		String [] tableLables = subMap.get("tableLable").toString().split(",");
		StringBuffer sb = new StringBuffer();
		String subType = subMap.get("subType").toString();
	    boolean isExistTable = StringUtil.toInt(subType, 0)==1?false:true;
		if(isExistTable){
			for(int i=0,len = tableLables.length;i<len;i++){
				sb.append("<td>").append(tableLables[i]).append("</td>");
			}
		}
		Object reTab=subMap.get("InformReportOnlyTable");
		boolean InformReportOnlyTable=reTab==null?false:Boolean.valueOf(reTab.toString());
		String tds=sb.toString();
		String title = subMap.get("subName").toString();
		title = ReportUiUtil.viewRptName(title, subType);
		if(talCategory!=null){
			boolean flag = ReportUiUtil.isSystemLog(subMap);
			if(title.indexOf("(")>0){
				String [] cats = title.substring(title.indexOf("(")+1,title.indexOf(")")).split(",");
				sb.replace(0, tds.length(), title.substring(0,title.indexOf("(")+1));
				for(int i=0,len=cats.length;i<len;i++){
					String str ="";
					if (!GlobalUtil.isNullOrEmpty(talCategory[i])
							&& talCategory[i].contains("***")) {
						str = talCategory[i].substring(talCategory[i].indexOf("***")+3);
						if (str.indexOf("%")>-1) {
							try {
								str = new String(str.getBytes("iso-8859-1"), "UTF-8");
								str = URLDecoder.decode(str, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
					}
					if(flag){
						str = DeviceTypeNameUtil.getDeviceTypeName(str, Locale.getDefault());
					}
					sb.append(cats[i]).append(" ").append(str);
					if(i<len-1){
						sb.append(" -> ");
					}
				}
				sb.append(")");
				title = sb.toString();
			}
		}
		
		sb.replace(0, sb.length(), "<div id='div_"+ subMap.get("subId") +repeatid+ "' class='easyui-panel' data-options='");
		if (!InformReportOnlyTable) {
			sb.append("headerCls:\"sim-panel-header\",");
		}else {
			sb.append("headerCls:\"dymicReport-panel-header\",height:175,");
		}
		sb.append("width:"+width+",onOpen:highcharts.loadReportData(\""+url+"\","+top+","+isExistTable+")' title='"+title+"'>").append(Rn)
		.append(s4).append("<div>").append(Rn);
		if (!InformReportOnlyTable) {
			sb.append(s4).append(s4)
					.append("<div id='chart_" + subMap.get("subId") +repeatid+ "'/>").append(Rn);
		}
		if(isExistTable){
			if (!InformReportOnlyTable) {
				sb.append(s4).append(s4).append("<a href='#' onClick=\"report.viewCmd(this,'table_"+ subMap.get("subId") +repeatid+"')\" style='margin-left:6px;'>隐藏</a>").append(Rn);
			}
			sb.append(s4).append(s4).append("<table id='table_"+subMap.get("subId") +repeatid+"' class='report-table' align='center' cellpadding='0' cellspacing='0'>").append(Rn)
			.append(s4).append(s4).append(s4).append("<thead");
			if (!InformReportOnlyTable) {
				sb.append(" class='fixedHeader'");
			}else {
				sb.append(" class='onlyfixedHeader'");
			}
			sb.append(">").append(Rn)
			.append(s4).append(s4).append(s4).append("<tr align='center'");
			if (!InformReportOnlyTable) {
				sb.append(" class='tableHead'");
			}else {
				sb.append(" class='onlytableHead'");
			}
			sb.append(">").append(Rn)
			.append(s4).append(s4).append(s4).append(s4).append(tds).append(Rn)
			.append(s4).append(s4).append(s4).append("</tr>").append(Rn)
			.append(s4).append(s4).append(s4).append("</thead>").append(Rn)
			.append(s4).append(s4).append("</table>" ).append(Rn);
		}
		sb.append(s4).append("</div>").append(Rn)
		.append("</div>");
		return sb.toString();
	}

	public static boolean isSystemLog(Map subMap){
		boolean flag = false;
		String subject=(String)subMap.get("subject");
		if("Log/Global/Detail".equals(subject)||subject.contains("Esm")){
			flag = true;
		}
		return flag;
	}
	/**
	 * 判断是否是核心节点上的报表，日志报表和自审计报表只在核心节点上
	 * @param subMap
	 * @return
	 */
	public static boolean isCoreNodeReport(Map subMap){//
		String subject = subMap.get("subject").toString();
		if ("Esm".equals(subject)||LogKeyInfo.LOG_SIM_EVENT.equals(subject)|| LogKeyInfo.LOG_SYSTEM_TYPE.equals((String) subMap.get("deviceType"))){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 构建Html所用的数据 //浏览Table用 Pagination分页用
	 * 
	 * @param Map
	 *            dataMap 数据Map
	 * @param String
	 *            tableUrl table所用的Url
	 * @param String[]
	 *            tableFiled 攫取DB数据对应的字段
	 * @param int
	 *            countSign 数据的单位信息
	 * @return String Table的Html
	 */
	private static String getItemValues(Map dataMap, String tmpValue,
			String tableFiled, String strNone) {
		// 最好区别每种情况// 如果=="&nbsp;"是否合适
		if (checkNull(tmpValue)) {// 一定不是//BYTES TOTAL opCount START_TIME
			// 避免同前台JS关键字冲突造成的问题
			if (strNone != null && strNone.trim().length() > 0) {
				if (tmpValue.indexOf("\\") >= 0)
					tmpValue = tmpValue.replace("\\", "\\\\");
				if (tmpValue.indexOf("'") >= 0)
					tmpValue = tmpValue.replace("'", "\\'");
			} else {
				if (tmpValue.indexOf("/") >= 0)
					tmpValue = tmpValue.replace("/", "／");
			}

		}
		if (!checkNull(tmpValue))// 事件源NA 其他也是NA
			tmpValue = ReportUiConfig.NA;
		return tmpValue;
	}
	
	/**
	 * 如果是数量数据转换成Double类型(通用)
	 * 
	 * @param String
	 *            item 项目
	 * @param Map
	 *            dataMap 数据包
	 * @return Double 数量的Double类型
	 */
	public static Double getUnitValue(String item, Map dataMap) {
		Double reValue = -1d;
		if (!(item.equals("BYTES")||item.equals("BYTES_IN")||item.equals("BYTES_OUT")))
			return reValue;
		String valueString=dataMap.get(item)==null?"0":dataMap.get(item).toString();
		reValue = Double.parseDouble(valueString);
		return reValue;
	}

	/**
	 * 如果是数量数据转换成Double类型(Table)
	 * 
	 * @param String
	 *            item 项目
	 * @param Map
	 *            dataMap 数据包
	 * @return Double 数量的Double类型
	 */
	public static Double getTByteValue(String[] tableFileds, Map dataMap) {
		Double reValue = -1d;
		
		String[] keys={"BYTES","BYTE","BYTES_IN","BYTES_OUT"};
		for (int i = 0; i < keys.length; i++) {
			if (dataMap.containsKey(keys[i])){
				String bytesInString=GlobalUtil.isNullOrEmpty(dataMap.get(keys[i]))?"0":(dataMap.get(keys[i]) + "");
				Double tmp =  Double.parseDouble(bytesInString);
				if(tmp>reValue){
					reValue = tmp;
				}
			}
		}
		return reValue;
		/*boolean bFlag = false;
		 * for (String tableFiled : tableFileds) {
			if (tableFiled.equals("BYTES")||tableFiled.equals("BYTES_IN")||tableFiled.equals("BYTES_OUT")) {//
				bFlag = true;
				break;
			}
		}
		if (bFlag){
			if(dataMap.containsKey("BYTES")){
				String bytesString=GlobalUtil.isNullOrEmpty(dataMap.get("BYTES"))?"0":(dataMap.get("BYTES") + "");
				reValue = Double.parseDouble(bytesString);
			}
			
			if(dataMap.containsKey("BYTES_IN")){
				String bytesInString=GlobalUtil.isNullOrEmpty(dataMap.get("BYTES_IN"))?"0":(dataMap.get("BYTES_IN") + "");
				Double tmp =  Double.parseDouble(bytesInString);
				if(tmp>reValue){
					reValue = tmp;
				}
			}
			if(dataMap.containsKey("BYTES_OUT")){
				String bytesOutString=GlobalUtil.isNullOrEmpty(dataMap.get("BYTES_OUT"))?"0":(dataMap.get("BYTES_OUT") + "");
				Double tmp =  Double.parseDouble(bytesOutString);
				if(tmp>reValue){
					reValue = tmp;
				}
			}
		}
		return reValue;*/
	}
	
	public static Double getDoubleValue(String[] tableFileds, Map dataMap) {
		Double reValue = 0d;
		String[] keys={"BYTES","BYTE","OPCOUNT","COUNTS","opCount","opCount1","opCount2","opCount3","opCount4","BYTES_IN","BYTES_OUT"};
		for (int i = 0; i < keys.length; i++) {
			if (dataMap.containsKey(keys[i])){
				String bytesInString=GlobalUtil.isNullOrEmpty(dataMap.get(keys[i]))?"0":(dataMap.get(keys[i]) + "");
				Double tmp =  Double.parseDouble(bytesInString);
				if(tmp>reValue){
					reValue = tmp;
				}
			}
		}
		return reValue;
	}

	/**
	 * 如果是数量数据转换成Double类型
	 * 
	 * @param Map
	 *            subMap 子报表信息
	 * @param Map
	 *            dataMap 数据Map
	 * @return Double 数量的Double类型
	 */

	public static Double getByteValue(Map subMap, Map dataMap) {
		Double chartItemValue = 0d;
		String chartItem = subMap.get("chartItem").toString();
		if (!chartItem.equals("BYTES"))
			return chartItemValue;
		chartItemValue = Double.parseDouble(dataMap.get(chartItem) + "");
		return chartItemValue;
	}

	/**
	 * 遍历数据集合返回 countSign
	 * 
	 * @param List
	 *            <Map> result 子报表信息
	 * @return int 数据的单位信息
	 */

	public static int getCountSign(List<Map> result) {
		int countSign = -1;
		double tmpCountSign = 0;
		for (Map map : result) {
			Iterator i = map.keySet().iterator();
			Object strKey = null;
			Object strValue = null;
			while (i.hasNext()) {
				strKey = i.next();
				if (strKey != null && (strKey.toString().equals("BYTES")
						||strKey.toString().equals("BYTES_IN")
						||strKey.toString().equals("BYTES_OUT"))) {
					strValue = map.get(strKey);
					tmpCountSign = Double.parseDouble(strValue.toString());

					if (tmpCountSign > 0) {
						countSign = ReportUiUtil.getCountCapability(tmpCountSign);
						return countSign;
					}
				}
			}
		}
		return countSign;
	}
	public static boolean isContainsObj(Object[] objs,Object obj){
		if (GlobalUtil.isNullOrEmpty(objs)||GlobalUtil.isNullOrEmpty(obj)) {
			return false;
		}
		for (Object object : objs) {
			if (!GlobalUtil.isNullOrEmpty(object)) {
				if (obj.equals(object)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 遍历数据集合返回 countSign
	 * 
	 * @param List
	 *            <Map> result 子报表信息
	 * @return int 数据的单位信息
	 */
	public static void filterExpData(List<Map> result, int countSign) {

		Object tmpStr = null;
		for (Map dataMap : result) {
			Iterator i = dataMap.keySet().iterator();
			Object key = null;
			while (i.hasNext()) {
				key = i.next();
				if (isContainsObj(operationKeys,key)) {
					Object value=dataMap.get(key);
					tmpStr = value == null ? "0" : ReportUiUtil.getChartShowValue(value.toString(), countSign);;
					if (tmpStr != null){
						dataMap.put(key, tmpStr);
					}
				}
			}
		}

	}
	
	public static void filterExpList(List<Map> result, int countSign,
			boolean tide, String tableType) {

		String tmpStr = null;
		for (Map dataMap : result) {
			Iterator i = dataMap.keySet().iterator();
			Object key = null;
			while (i.hasNext()) {
				key = i.next();
				tmpStr = ReportUiUtil.getFilterUnitValue(key + "", dataMap,countSign, tide, tableType);
				if (tmpStr != null){
					dataMap.put(key, tmpStr);
				}
			}
		}

	}

	/**
	 * 获取图片上的Title，单位
	 * 
	 * @param Map
	 *            subMap 子报表信息
	 * @param int
	 *            countSign 数据的单位信息
	 * @return void 将title信息加入subMap
	 */

	public static void getUnit(Map subMap, int countSign) {
		String unit = subMap.get("tableLable") + "";
		if (ReportUiUtil.checkNull(unit) && unit.indexOf("*") > 0) {// &&
			// countSign
			// != -1
			unit = ReportUiConfig.Capability.get(countSign);
			subMap.put("unit", ReportUiConfig.ExpUnit.replace("*", unit));
		}
		if (ReportUiUtil.checkNull(unit) && unit.indexOf("条数") > 0) {			
			subMap.put("unit", ReportUiConfig.ExpUnit.replace("*", "(条)"));
		}		
	}

	/**
	 * 构建Exp的Title
	 * 
	 * @param String
	 *            tableLable table的title在DB中对应的字段名[tableLable]
	 * @param int
	 *            countSign 数据的单位信息
	 * @return String 子报表的Exp Title内容
	 */
	public static String addExpTableTitle(String titleLable, int countSign) {
		String[] tableFileds = titleLable.split(",");
		String cb = ReportUiConfig.Capability.get(countSign);
		StringBuffer sb = new StringBuffer();
		for (String tableFiled : tableFileds) {
			if (tableFiled.indexOf("*") > 0)
				tableFiled = tableFiled.replace("*", cb);
			sb.append(tableFiled + ",");
		}
		return sb.toString();
	}

	/**
	 * 将字节单位换算成相关mb kb 单位
	 * 
	 * @param Double
	 *            d 待转换原始数据
	 * @param int
	 *            countSign 数据的单位信息
	 * @return Double 转换后的数据
	 */
	public static Double getCapability(Double d, int countSign) {
		switch (countSign) {
		// 保留两位小数
		case 1:
			d = d / (1024 * 1024 * 1024); // 如果大于10G用G标识
			break;
		case 2:
			d = d / (1024 * 1024 ); // 如果大于10m用m标识
			break;
		case 3:
			d = (d / 1024); // 如果大于10k用k标识
			break;
		}	
		Double reValue = getRound(d, ReportUiConfig.Dfraction);
		if (reValue==0 && d!=0)
			return getRound(d, 6);
		else
			return reValue;
	}
	

	/**
	 * 每三位数字用,分开
	 * 
	 * @param double
	 *            value 待格式化的Double
	 * 
	 * @return String 格式化后的字符串
	 */
	public static String customFormat(double value) {
		DecimalFormat myFormatter = new DecimalFormat("###,###");
		String output = myFormatter.format(value);
		return output;
	}

	/**
	 * 每三位数字用,分开
	 * 
	 * @param double
	 *            value 待格式化的Double
	 * 
	 * @return String 格式化后的字符串
	 */
	public static String customCpFormat(double value) {
		DecimalFormat myFormatter = new DecimalFormat("#,##0.##");// #,###.00
		String output = myFormatter.format(value);
		return output;
	}

	/**
	 * 每三位数字用,分开,保留两位小数
	 * 
	 * @param String
	 *            value 待格式化的Double *
	 * @param int
	 *            countSign 数据的单位信息
	 * @return String 格式化后的字符串
	 */
	public static String getTCapability(String value, int countSign) {
		if (value.equals("0"))
			return "0.00";
		double d = Double.parseDouble(value);
		d = getCapability(d, countSign);
		String reValue = customCpFormat(d);
		
		if (reValue.equals("0")){	//对假0作特殊处理			
			DecimalFormat myFormatter = new DecimalFormat("#,##0.######");// #,###.00
			reValue = myFormatter.format(d);
		}
		
		return reValue;
	}
	public static String customfiFormat(Object value) {
		if (null==value||value.equals("0"))
			return "0";
		double d = Double.parseDouble(value.toString());
		String reValue = customCpFormat(d);
		if (reValue.equals("0")){	//对假0作特殊处理			
			DecimalFormat myFormatter = new DecimalFormat("#,##0.######");// #,###.00
			reValue = myFormatter.format(d);
		}
		return reValue;
	}

	public static Double getChartShowValue(String value, int countSign) {
		if (value.equals("0"))
			return 0.00;
		double d = Double.parseDouble(value);
		d = getCapability(d, countSign);
		return d;
	}
	/**
	 * 根据数据判断用何单位描述
	 * 
	 * @param Double
	 *            d 待判断数值
	 * @return int 1[MB] 2[K] 0[bytes] -1[无需计算]
	 */
	public static int getCountCapability(Double d) {
		int reValue = 0;
		if (d == -1d){
			reValue = -1;
		}else if (d > 1D * 1024 * 1024 * 1024 *ReportUiConfig.UnitValue){ // 如果大于10G用G标识
			reValue = 1;
		}else if (d > 1D * 1024 * 1024 * ReportUiConfig.UnitValue){// 如果大于10M用M标识
			reValue = 2;
		}else if (d > 1D * 1024 * ReportUiConfig.UnitValue){// 如果大于10k用k标识
			reValue = 3;
		}
		return reValue;
	}

	/**
	 * 正则分析字符串
	 * 
	 * @param String
	 *            regex 待判断数值 *
	 * @param String
	 *            line 待分析字符串
	 * @return 分析后的字符串
	 */
	public static String getRegStr(String regex, String line) {
		String reValue = null;
		Pattern p = Pattern.compile(regex);
		Matcher m;
		m = p.matcher(line);
		if (m.find()) {
			reValue = m.group();
		}
		return reValue;
	}

	/**
	 * 过滤数量值
	 * 
	 * @param Map
	 *            dataMap 数据Map
	 * @param String
	 *            key 攫取DB数据对应的字段
	 * @param int
	 *            countSign 数据的单位信息
	 * @return String 过滤后的量值
	 */

	public static String getFilterUnitValue(String key, Map<String,Object> dataMap, int countSign, boolean tide, String tableType) {
		String reValue = null;
		Object value = dataMap.get(key) ;
		if (key.equals("BYTES") || key.equals("BYTES_IN") || key.equals("BYTES_OUT"))
			reValue = value == null ? "0" : ReportUiUtil.getTCapability(value.toString(), countSign);
		else if (key.equals("TOTAL") || key.equals("opCount"))
			reValue = value == null ? "0" : customFormat(new Double(new java.math.BigDecimal(value.toString()).toPlainString()));
		else if (key.equals("TOTALNO") )
			reValue = (value == null) ? "0" : customfiFormat(value);
		else if (key.equals("START_TIME"))
			reValue = filterTime(value, tide, tableType);
		else if (key.equals("DVC_TYPE") || key.equals("ALLLOGTYPE")){		
			reValue = DeviceTypeNameUtil.getDeviceTypeName((String)value);
			dataMap.put(ReportModel.UNFMT_DATA_PREFIX+key, value) ;
		}else
			reValue = value == null ? "" : value.toString();
		return reValue;
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @param Object
	 *            obj 任意对象
	 * @return Boolean true:非空 false:空
	 */
	public static boolean checkNull(Object obj) {
		boolean reValue = true;
		if (obj == null)
			return false;
		if (obj.toString().trim().length() <= 0)
			return false;
		if (obj.toString().trim().toLowerCase().equals("null"))
			return false;
		return reValue;

	}
	
	 /**
	  * 转化自定义报表的url, 把已经存在的下钻参数加入到url中
	  * @param url
	  * @param subMap
	  * @param ruleResult
	  * @return url
	  */
	public synchronized static String convertMstType2URL(String url, Map<Object, Object> subMap,List ruleResult) { 
		
		Integer mstType = Integer.parseInt(subMap.get("mstType").toString());
		if(mstType ==2){
			String deviceType = (String)subMap.get("deviceType");
			url+="&dvctype="+deviceType;
			if(ruleResult!=null&&ruleResult.size()>0){
				for (Object m : ruleResult) {
					Map map = (Map) m;
					Integer paramIndex = (Integer) map.get("htmlField"); 
					if(paramIndex==6){
						String talCategoryKey=(String)map.get("sqlParam"); 
						String talCategoryValue=(String)map.get("sqlValue");
						
						String talCategoryKeyConvert="";
					    Set<String> set=ReportUiConfig.ColumnRuleMap.keySet();
						if(set!=null){
							for (String key : set) {
								String value=ReportUiConfig.ColumnRuleMap.get(key);
								if(talCategoryKey.indexOf(value)!=-1){
									talCategoryKeyConvert=key;
									break;
								}
							}
						}
						try {
							String valueString="";
							valueString=talCategoryValue;
							if (null != talCategoryValue && talCategoryValue.indexOf("%")==-1 && !ReportUiUtil.isEnglishOrNumber(talCategoryValue)) {
								valueString=URLEncoder.encode(talCategoryValue, "UTF-8");
							}
							url += "&" + ReportUiConfig.talCategory + "="+talCategoryKeyConvert+"***"+valueString;
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return url;
	}
	

	/**
	 * 
	 * 通过Vo Bean获取下探 URL
	 * 
	 * @param bs
	 *            BaseStruct
	 * 
	 * @return 返回下探URL
	 * 
	 */
	public static String getUrl(BaseStruct bs) {
		String reValue = ReportUiConfig.goUrl + bs.getActionname()
				+ "&talrpt=talrpt";
		if (checkNull(bs.getDvctype())){
			reValue += "&" + ReportUiConfig.dvctype + "=" + bs.getDvctype();
		}
		if (checkNull(bs.getDvcaddress())){
			if(bs.getDvcaddress().equals("onlyByDvctype")){
				reValue += "&onlyByDvctype=onlyByDvctype";
			}else{
				reValue += "&" + ReportUiConfig.dvcaddress + "="
				+ bs.getDvcaddress();
			}
		}
		if (checkNull(bs.getMstrptid())){
			reValue += "&" + ReportUiConfig.mstrptid + "=" + bs.getMstrptid();
		}
		if (checkNull(bs.getSubrptid())){
			reValue += "&" + ReportUiConfig.subrptid + "=" + bs.getSubrptid();
		}
		if (checkNull(bs.getSTime())){
			reValue += "&" + ReportUiConfig.sTime + "=" + bs.getSTime();
		}
		if (checkNull(bs.getETime())){
			reValue += "&" + ReportUiConfig.eTime + "=" + bs.getETime();
		}
		if (bs.getTalCategory() != null){
			String[] talCategoryArray=bs.getTalCategory();
			if(talCategoryArray!=null&&talCategoryArray.length>0){
				for (int j = 0; j < talCategoryArray.length; j++) {
					if(talCategoryArray[j]!=null&&!talCategoryArray[j].equals("")&&!talCategoryArray[j].equals("null")){
						try {
							reValue += "&"+ ReportUiConfig.talCategory+ "="+ java.net.URLEncoder.encode(talCategoryArray[j],"UTF-8");
						} catch (UnsupportedEncodingException e) {
							log.error("Error:" + e.getMessage());
							e.printStackTrace();
						} 
					}
				}
			}
		}
		if (checkNull(bs.getViewshow())){
			try {
				reValue += "&"+ReportUiConfig.viewshow+"=" 
						+ java.net.URLEncoder.encode(bs.getViewshow(), "UTF-8");
			} catch (Exception e) {
				
				log.error("Error:" + e.getMessage());
				e.printStackTrace();
			}
		}
		if (checkNull(bs.getViewji())){
			reValue += "&viewji=" + bs.getViewji();
		}
		if (checkNull(bs.getViewtype())){
			reValue += "&viewtype=" + bs.getViewtype();
		}
		if (checkNull(bs.getMstType())){
			reValue += "&msttype=" + bs.getMstType();
		}

		String[] nodeId=bs.getNodeId();
		if(nodeId!=null){
			for (String s : nodeId) {
				reValue+="&nodeId="+s;
			}
			int len=nodeId.length;
			reValue+="&len="+len;
		}else{
			log.debug("ReportUiUtil.getUrl(), nodeId==null!!!");
		}
		
		if (checkNull(bs.getOnlyByDvctype())){
			reValue += "&onlyByDvctype=" + bs.getOnlyByDvctype();
		}
		reValue+="&hrefURL=true";	
		
		return reValue;
	}

	/**
	 * 
	 * 通过request获取Vo Bean
	 * 
	 * @param request
	 *            HttpServletRequest
	 * 
	 * @return 整理后的VO
	 * 
	 */
	public static BaseStruct getBaseS(HttpServletRequest request) {
		BaseStruct reValue = new BaseStruct();
		
		String[] nodeId = request.getParameterValues("nodeId");
		String onlyByDvctype = request.getParameter("onlyByDvctype");
		String hrefURL=request.getParameter("hrefURL");
		// 设备
		String dvcAddress = request.getParameter(ReportUiConfig.dvcaddress);
		if ((dvcAddress==null) || dvcAddress.trim().equals(""))
			dvcAddress = (String) request.getAttribute("dvcaddress");
		// 主报表ID
		String mstRptId = request.getParameter(ReportUiConfig.mstrptid);

		// 子报表
		String subRptId = request.getParameter(ReportUiConfig.subrptid);

		// 开始时间
		String sTime = request.getParameter(ReportUiConfig.sTime);
		// 结束时间
		String eTime = request.getParameter(ReportUiConfig.eTime);

		String dvctype = request.getParameter(ReportUiConfig.dvctype);

		// show input time
		String viewshow = request.getParameter(ReportUiConfig.viewshow);
		viewshow = ChangePageEncode.IsoToUtf8(viewshow);

		// 季度
		String viewji = request.getParameter(ReportUiConfig.viewji);
		if (!checkNull(viewji))
			viewji = request.getParameter("viewji");

		// 类型
		String viewtype = request.getParameter(ReportUiConfig.viewtype);
		if (!checkNull(viewtype)){
			viewtype = request.getParameter("viewtype");
		}
		
		String[] talCategoryArray=request.getParameterValues("talCategory");
		reValue.setTalCategory(talCategoryArray);
		
		// show time Input
		reValue.setDvcaddress(dvcAddress);
		reValue.setMstrptid(mstRptId);
		reValue.setSubrptid(subRptId);
		reValue.setSTime(sTime);
		reValue.setETime(eTime);
		reValue.setDvctype(dvctype);
		reValue.setViewshow(viewshow);
		reValue.setViewji(viewji);
		reValue.setViewtype(viewtype);
		reValue.setNodeId(nodeId);
		reValue.setOnlyByDvctype(onlyByDvctype);
		return reValue;
	}

	/**
	 * 
	 * 整理Vo Bean
	 * 
	 * @param bean
	 *            CreateReportBean
	 * 
	 * @param request
	 *            HttpServletRequest
	 * 
	 * @return 整理后的VO
	 * 
	 */
	public static ReportBean tidyFormBean(ReportBean bean,HttpServletRequest request) {
		if (GlobalUtil.isNullOrEmpty(request)) {
			return bean;
		}
		String defValue = "";
		// 设备地址
		if (!checkNull(bean.getDvcaddress())) {
			String dvcAddress = request.getParameter(ReportUiConfig.dvcaddress);
			if (checkNull(dvcAddress))
				bean.setDvcaddress(dvcAddress);
			else
				bean.setDvcaddress(defValue);
		}

		// 设备
		if (!checkNull(bean.getDvctype())) {
			String dvcType = request.getParameter(ReportUiConfig.dvctype);
			if (checkNull(dvcType))
				bean.setDvctype(dvcType);
			else
				bean.setDvctype(defValue);
		}
				
		// 主报表ID
		if (!checkNull(bean.getMstrptid())) {
			String mstRptId = request.getParameter(ReportUiConfig.mstrptid);
			if (checkNull(mstRptId))
				bean.setMstrptid(mstRptId);
			else
				bean.setMstrptid(defValue);
		}
		
		// 业务报表根ID
		if (!checkNull(bean.getRootId())) {
			String rootId = request.getParameter(ReportUiConfig.rootId);
			if (checkNull(rootId))
				bean.setRootId(rootId);
			else
				bean.setRootId(defValue);
		}
		
		// 设备组节点ID
		if (!checkNull(bean.getAssGroupNodeId())) {
			String assGroupNodeId = request.getParameter(ReportUiConfig.assGroupNodeId);
			if (checkNull(assGroupNodeId))
				bean.setAssGroupNodeId(assGroupNodeId);
			else
				bean.setAssGroupNodeId(defValue);
		}
		// 拓扑ID
		if (!checkNull(bean.getTopoId())) {
			String topoId = request.getParameter(ReportUiConfig.topoId);
			if (checkNull(topoId))
				bean.setTopoId(topoId);
			else
				bean.setTopoId(defValue);
		}
		
		// 节点级别
		if (!checkNull(bean.getNodeLevel())) {
			String nodeLevel = request.getParameter(ReportUiConfig.nodeLevel);
			if (checkNull(nodeLevel))
				bean.setNodeLevel(nodeLevel);
			else
				bean.setNodeLevel(defValue);
		}
		
		//日志源类型
		if (!checkNull(bean.getNodeType())) {
			String nodeType = request.getParameter(ReportUiConfig.nodeType);
			if (checkNull(nodeType))
				bean.setNodeType(nodeType);
			else
				bean.setNodeType(defValue);
		}
		
		if (!checkNull(bean.getReportType())) {
			String reportType = request.getParameter("reportType");
			if (checkNull(reportType))
				bean.setReportType(reportType);
			else
				bean.setReportType(defValue);
		}

		SimpleDateFormat sdf=new SimpleDateFormat(ReportUiConfig.dFormat1);
		// 开始时间
		if (!checkNull(bean.getTalStartTime())) {
			String sTime = request.getParameter(ReportUiConfig.sTime);
			try {
				if (checkNull(sTime)){
					bean.setTalStartTime(sTime);
				}else
					bean.setTalStartTime(getNowTime(ReportUiConfig.dFormat2)+ ReportUiConfig.sTimePostfix);
			} catch (Exception e) {
				bean.setTalStartTime(getNowTime(ReportUiConfig.dFormat2)+ ReportUiConfig.sTimePostfix);
			}
		}

		// 结束时间
		if (!checkNull(bean.getTalEndTime())) {
			String eTime = request.getParameter(ReportUiConfig.eTime);
			try {
				if (checkNull(eTime)){
					sdf.parse(eTime);
					bean.setTalEndTime(eTime);
				}else
					bean.setTalEndTime(getNowTime(ReportUiConfig.dFormat1));//
			} catch (Exception e) {
				bean.setTalEndTime(getNowTime(ReportUiConfig.dFormat1));
			}
		}
        // top
		if (!checkNull(bean.getTalTop())) {
			String top = request.getParameter(ReportUiConfig.TalTop);
			if (checkNull(top))
				bean.setTalTop(top);
			else
				bean.setTalTop("5");
		}
		// show time Input
		if (!checkNull(bean.getShowTimeInput())) {
				bean.setShowTimeInput(getNowTime(ReportUiConfig.dFormat2));
		}else{
			String showTime = ChangePageEncode.IsoToUtf8(bean.getShowTimeInput());
			bean.setShowTimeInput(showTime);
		}
		// 季度
		if (!checkNull(bean.getQselected())) {
			String qSelected = request.getParameter("qselected");
			if (!checkNull(qSelected))
				qSelected = request.getParameter("viewji");

			// 如果为空就显示默认当天的日期
			if (checkNull(qSelected))
				bean.setQselected(qSelected);
		}
		// 类型
		if (!checkNull(bean.getDtypeSelected())) {
			String dType = request.getParameter("datetypeselected");
			if (!checkNull(dType))
				dType = request.getParameter("viewtype");

			// 如果为空就显示默认当天的日期
			if (checkNull(dType))
				bean.setDtypeSelected((dType));
			else
				bean.setDtypeSelected(ReportUiConfig.dTypeNum);
		}

		if (!checkNull(bean.getTalCategory())) {
			String[] categoryValues = request.getParameterValues(ReportUiConfig.talCategory);
			if (!GlobalUtil.isNullOrEmpty(categoryValues)) {
				for (int i = 0; i < categoryValues.length; i++) {
					if (categoryValues[i].indexOf("ALLLOGTYPE")>-1&&categoryValues[i].length()>1) {
						categoryValues[i]=categoryValues[i].replaceAll("ALLLOGTYPE", "TYPE");
					}
					if (categoryValues[i].indexOf("ALLLOGIP")>-1&&categoryValues[i].length()>1) {
						categoryValues[i]=categoryValues[i].replaceAll("ALLLOGIP", "IP");
					}
				}
			}
			if (checkNull(categoryValues)){
				StringBuffer stringBuffer=null;
				String[] talCategories = null;
				for(int i=0,len=categoryValues.length;i<len;i++){
					if (categoryValues[i].length()<1) {
						continue;
					}
					if (stringBuffer==null){stringBuffer=new StringBuffer();}
					if (talCategories == null) {
						talCategories=new String[categoryValues.length] ;
					}
					talCategories[i] =  ChangePageEncode.IsoToUtf8(categoryValues[i]);
					stringBuffer.append(talCategories[i]);
					if (i<categoryValues.length-1) {
						stringBuffer.append(",");
					}
				}
				if (stringBuffer!=null&&stringBuffer.length()>0) {
					bean.setTalCategories(stringBuffer.toString());
				}
				if (talCategories!=null) {
					bean.setTalCategory(talCategories);
				}
				
			}
		}

		if (!checkNull(bean.getSubrptid())) {
			String subrptid = request.getParameter(ReportUiConfig.subrptid);
			if (checkNull(subrptid))
				bean.setSubrptid(subrptid);
			else
				bean.setSubrptid(defValue);
		}
		if (!checkNull(bean.getRickniu())) {
			String rickniu = request.getParameter("rickniu");
			if (checkNull(rickniu))
				bean.setRickniu(rickniu);
			else
				bean.setRickniu(defValue);
		}
		if (!checkNull(bean.getPagesize())) {
			String pagesize = request.getParameter("pagesize");
			if (checkNull(pagesize))
				bean.setPagesize(pagesize);
			else
				bean.setPagesize("10");
		}
		if (!checkNull(bean.getNodeId())) {
			String[] nodeId = request.getParameterValues("nodeId");
			if (checkNull(nodeId))
				bean.setNodeId(nodeId);
		}
		return bean;
	}

	/**
	 * 
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            待四舍五入的数字
	 * 
	 * @param scale
	 *            小数点后保留几位
	 * 
	 * @return 四舍五入后的结果
	 * 
	 */

	public static double getRound(double v, int scale) {
		String f = ".";
		for(int i=0; i<scale; i++){
			f = f + "0";
		}
		double reValue = 0;
		try {
			DecimalFormat myFormatter = new DecimalFormat(f);// #,###.00
			reValue = Double.parseDouble(myFormatter.format(v));
		} catch (Exception e) {
			log.error("Error:" + e.getMessage());
			e.printStackTrace();
		}
		return reValue;
	}

	/**
	 * 
	 * 计算两个日期相差时间Exp
	 * 
	 * @param String
	 *            sTime 开始时间
	 * 
	 * @param String
	 *            eTime 结束时间
	 * 
	 * @return String 日期相差时间
	 * 
	 */
	public static String getCountTime(String sTime, String eTime)
			throws ParseException {
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		Date date1 = smpFmt1.parse(sTime);
		Date date2 = smpFmt1.parse(eTime);
		final int const_second = 1000;
		final int const_minute = 60 * const_second;
		final int const_hour = 60 * const_minute;
		final int const_day = 24 * const_hour;
		long dif = date1.getTime() - date2.getTime();
		if (dif < 0)
			dif = 0 - dif;
		long dif_day = dif / const_day;
		long dif_hour = (dif - dif_day * const_day) / const_hour;
		long dif_minute = (dif - dif_day * const_day - dif_hour * const_hour)
				/ const_minute;
		long dif_second = (dif - dif_day * const_day - dif_hour * const_hour - dif_minute
				* const_minute)
				/ const_second;
		return (dif_day + "天" + dif_hour + "小时" + dif_minute + "分钟"
				+ dif_second + "秒");

	}

	/**
	 * 拼分页的检索条件
	 * 
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @param String
	 *            indexs 条件文本框下标
	 * @return List 检索条件list //NA
	 */
	public static List<String> getPaginationItem(HttpServletRequest request,
			String indexs) {
		List<String> reValue = new ArrayList<String>();
		String itemV = null;
		if (!GlobalUtil.isNullOrEmpty(indexs)) {
			for (String index : indexs.split(",")) {
				if (!index.equals("99")) {
					itemV = request.getParameter(ReportUiConfig.PageParam
							+ index);
					if (StringUtils.isBlank(itemV))
						itemV = "%" + "" + "%";
					else
						itemV = "%" + itemV.trim() + "%";
					if ("%n/a%".equalsIgnoreCase(itemV))
						itemV = "";
					reValue.add(itemV);
				}
			}
		}
		return reValue;
	}

	/**
	 * 前台显示检索条件
	 * 
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @param String
	 *            indexs 条件文本框下标
	 * @return String 显示检索条件用的Js
	 */
	public static String getViewPaginationItem(HttpServletRequest request,
			String indexs) {
		StringBuffer reValue = new StringBuffer();
		String itemV = null;
		for (String index : indexs.split(",")) {
			if (!index.equals("99")) {
				itemV = request.getParameter(ReportUiConfig.PageParam + index);
				if (StringUtils.isBlank(itemV)){
					itemV = "";
				}
				else{
					itemV = itemV.trim();
				}
				itemV = itemV.replace("\\", "\\\\");
				reValue.append("$('" + (ReportUiConfig.PageParam + index)
						+ "').value=\"" + itemV + "\";");
			}
		}
		return reValue.toString();
	}

	public static Map<Object, String> editMap(Map map) {
		Map<Object, String> reValue = new HashMap<Object, String>();
		Iterator i = map.keySet().iterator();
		Object strKey = null;
		String strValue = null;
		while (i.hasNext()) {
			strKey = i.next();
			strValue = map.get(strKey) == null ? "" : map.get(strKey).toString();
			reValue.put(strKey, strValue);
		}
		return reValue;
	}

	public static List editMap(List list) {
		List reValue = new ArrayList();
		if (list != null){
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				reValue.add(editMap(map));
			}
		}
		return reValue;
	}

	public static LinkedHashMap<String, Integer> orderMap(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(
				map.entrySet());// 排序前
		Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() { // 排序
					public int compare(Map.Entry<String, Integer> o1,
							Map.Entry<String, Integer> o2) {
						return (o2.getValue() - o1.getValue());
					}
				});
		return (LinkedHashMap<String, Integer>) map;
	}

	/**
	 * 获取当前日期
	 * 
	 * @param String
	 *            dateformat 日期格式
	 * @return String 符合规范的当天日期格式
	 */
	public static String getNowTime(String dateformat) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		String reValue = dateFormat.format(now);
		return reValue;
	}

	/**
	 * 
	 * 计算两个日期相差毫秒数,计算去那个表
	 * 
	 * @param String
	 *            sTime 开始时间
	 * 
	 * @param String
	 *            eTime 结束时间
	 * 
	 * @return String 日期相差毫秒
	 * 
	 */
	public static long countTime(String sTime, String eTime) {
		long result1 = -1;
		long result2 = -1;
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		try {
			result1 = (new Date().getTime() - smpFmt1.parse(
					eTime).getTime()) / 1000;
			result2 = (smpFmt1.parse(eTime).getTime() - smpFmt1
					.parse(sTime).getTime()) / 1000;
		} catch (ParseException e) {
			log.error("Error:" + e.getMessage());
			e.printStackTrace();
		}
		if (result2 > result1)
			return result2;
		return result1;
	}

	/**
	 * 
	 * 过滤日期去掉毫秒
	 * 
	 * @param String
	 *            time 待过滤日期
	 * @return String 过滤后日期
	 * 
	 */
	public static String filterTime(Object time, boolean tide, String tableType) {
		String reValue = null;
		if (time != null) {
			reValue = time.toString();
			reValue = reValue.endsWith(".0") ? reValue.substring(0, reValue
					.length() - 2) : reValue.toString();
			if (tide){
				String ruleDate=getTableType(tableType);
				String toTime=getAddDate(reValue, ruleDate,tableType);
				if (ruleDate.equals("hour")){
					reValue=reValue.substring(11, reValue.length());
					toTime=toTime.substring(11, toTime.length());
				}
				else if (ruleDate.equals("day")){
					reValue=reValue.substring(5, 16);
					toTime=toTime.substring(5, 16);
				}
				else if (ruleDate.equals("month")){
					reValue=reValue.substring(2, 10);
					toTime=toTime.substring(2, 10);
				}
				reValue += "-->" + toTime;
			}
		}
		return reValue;
	}

	public static String getTableType(String tableType) {
		String reValue = null;
		if (tableType.toLowerCase().indexOf("hour") >= 0)
			reValue = "hour";
		else if (tableType.toLowerCase().indexOf("day") >= 0)
			reValue = "day";
		else if (tableType.toLowerCase().indexOf("month") >= 0)
			reValue = "month";
		return reValue;
	}
	/**
	 * 
	 * @param dateType
	 * @return
	 */
	public static String toStartTime(String dateType,String eTime){
		try {
			if (GlobalUtil.isNullOrEmpty(eTime)) {
				eTime="0000-00-00 00:00:00";
				return "0000-00-00 00:00:00";
			}
			if (eTime.length()>=19) {
				eTime=eTime.substring(0,19);
			}
			if ("hour".equalsIgnoreCase(dateType)) {
				int tempHour=Integer.valueOf(eTime.substring(11, 13))-1;
				String tHour=tempHour==-1?"23":(0<tempHour&&tempHour<10)?"0"+tempHour:""+tempHour;
				if (tempHour==-1) {
					return eTime.substring(0,10)+ReportUiConfig.sTimePostfix;
				}
				return eTime.substring(0,11)+tHour+":"+eTime.substring(14, eTime.length());
			}else if("day".equalsIgnoreCase(dateType)){
				int tempDay=Integer.valueOf(eTime.substring(8, 10))-1;
				if (tempDay<=0) {
					int tempMonth=Integer.valueOf(eTime.substring(5, 7))-1;
					if (tempMonth==1||tempMonth==3||tempMonth==5||tempMonth==7
							||tempMonth==8||tempMonth==10||tempMonth==12) {
						tempDay=31+tempDay;
					}else if (tempMonth==4||tempMonth==6||tempMonth==9||tempMonth==11) {
						tempDay=30+tempDay;
					}else if (tempMonth==2) {
						tempDay=28+tempDay;
					}
					String tmonth=tempMonth==0?"12":1<=tempMonth&&tempMonth<=9?"0"+tempMonth:""+tempMonth;
					if (tempMonth==0) {
						String tyear=(Integer.valueOf(eTime.substring(0, 4))-1)+"-";
						return tyear+tmonth+"-"+tempDay+eTime.substring(10, eTime.length());
					}
					return eTime.substring(0,5)+tmonth+"-"+tempDay+eTime.substring(10, eTime.length());
				}
				String tDay=tempDay<=9?"0"+tempDay:""+tempDay;
				return eTime.substring(0,8)+tDay+eTime.substring(10, eTime.length());
			}else if("week".equalsIgnoreCase(dateType)){
				int tempDay=Integer.valueOf(eTime.substring(8, 10))-7;
				if (tempDay<=0) {
					int tempMonth=Integer.valueOf(eTime.substring(5, 7))-1;
					if (tempMonth==1||tempMonth==3||tempMonth==5||tempMonth==7
							||tempMonth==8||tempMonth==10||tempMonth==12) {
						tempDay=31+tempDay;
					}else if (tempMonth==4||tempMonth==6||tempMonth==9||tempMonth==11) {
						tempDay=30+tempDay;
					}else if (tempMonth==2) {
						tempDay=28+tempDay;
					}
					String tmonth=tempMonth==0?"12":1<=tempMonth&&tempMonth<=9?"0"+tempMonth:""+tempMonth;
					if (tempMonth==0) {
						String tyear=(Integer.valueOf(eTime.substring(0, 4))-1)+"-";
						return tyear+tmonth+"-"+tempDay+eTime.substring(10, eTime.length());
					}
					return eTime.substring(0,5)+tmonth+"-"+tempDay+eTime.substring(10, eTime.length());
				}
				String tDay=tempDay<=9?"0"+tempDay:""+tempDay;
				return eTime.substring(0,8)+tDay+eTime.substring(10, eTime.length());
			}else if("month".equalsIgnoreCase(dateType)){
				int tempMonth=Integer.valueOf(eTime.substring(5, 7))-1;
				String tmonth=tempMonth==0?"12":1<=tempMonth&&tempMonth<=9?"0"+tempMonth:""+tempMonth;
				if (tempMonth==0) {
					String tyear=(Integer.valueOf(eTime.substring(0, 4))-1)+"-";
					return tyear+tmonth+eTime.substring(7, eTime.length());
				}
				
				return eTime.substring(0,5)+tmonth+eTime.substring(7, eTime.length());
			}else if("year".equalsIgnoreCase(dateType)){
				int tyear=(Integer.valueOf(eTime.substring(0, 4))-1);
				return tyear+eTime.substring(4, eTime.length());
			}
			return eTime.substring(0,10)+ReportUiConfig.sTimePostfix;
		} catch (NumberFormatException e) {
			return eTime.substring(0,10)+ReportUiConfig.sTimePostfix;
		}
	}
	/**
	 * 
	 * 导出获取开始结束日期 Exp
	 * 
	 * @param String
	 *            type 日期类型[年月日]
	 * 
	 * @return String[] 开始结束日期
	 * 
	 */
	public static String[] getExpTime(String type) {
		String[] reValue = new String[2];
		Calendar rightNow = Calendar.getInstance();
		reValue[1] = getNowTime(ReportUiConfig.dFormat1);
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		if (type.equals("quarter") || type.equals("lable.report.quarter")
				|| type.equals("lable.report.quarterReportType")) {
			QuarterDate quarter = new QuarterDate();
			reValue[0] = quarter.getQuarterS() + ReportUiConfig.sTimePostfix;
			reValue[1] = quarter.getQuarterE() + ReportUiConfig.eTimePostfix;
		}
		if (type.equals("addMonth") || type.equalsIgnoreCase("month")
				|| type.equals("lable.report.typemonth")
				|| "lable.report.typemonthReportType".equalsIgnoreCase(type)) {
			reValue[0] = toStartTime("month", reValue[1]);

		} else if (type.equalsIgnoreCase("year") || type.equals("lable.report.typeyear")
				|| "lable.report.typeyearReportType".equalsIgnoreCase(type)) {
			
			reValue[0] = toStartTime("year", reValue[1]);
		} else if (type.equals("addDay") || type.equalsIgnoreCase("day")
				|| type.equals("lable.report.typeday")
				|| "lable.report.typedayReportType".equals(type)) {
			reValue[0] = toStartTime("day", reValue[1]);
		} else if (type.equals("addWeek") || type.equalsIgnoreCase("week")) {
			reValue[0] = toStartTime("week", reValue[1]);
		} else {
			reValue[0] = smpFmt1.format(getAddDate(type)).split(" ")[0]
					+ ReportUiConfig.sTimePostfix;
			reValue[1] = getNowTime(ReportUiConfig.dFormat1).split(" ")[0]
					+ ReportUiConfig.eTimePostfix;
		}
		return reValue;
	}

	/**
	 * 
	 * 根据日期类型[规则]计算开始日期
	 * 
	 * @param String
	 *            ruleDate 日期类型[规则]
	 * 
	 * @return Date 开始日期
	 * 
	 */
	public static Date getAddDate(String ruleDate) {
		Calendar rightNow = Calendar.getInstance();
		if (ruleDate.equals("addHour"))
			rightNow.add(Calendar.HOUR, -1);
		else if (ruleDate.equals("addDay") || ruleDate.equalsIgnoreCase("day")
				|| ruleDate.equals("lable.report.typeday")||"lable.report.typedayReportType".equals(ruleDate)){
			rightNow.add(Calendar.DATE, -1);
		}else if (ruleDate.equals("addMonth") || ruleDate.equalsIgnoreCase("month")
				|| ruleDate.equals("lable.report.typemonth")||"lable.report.typemonthReportType".equals(ruleDate))
			rightNow.add(Calendar.MONTH, -1);
		else if (ruleDate.equalsIgnoreCase("week")
				|| ruleDate.equals("lable.report.week")||"lable.report.weekReportType".equals(ruleDate)){
				Date now =  rightNow.getTime();
				rightNow.set(Calendar.DAY_OF_WEEK, 2); //rightNow为周日的时候 其实现在已经是下周一了
				Date monday =   rightNow.getTime();
				if(now.getTime()<monday.getTime()){
					rightNow.set(Calendar.MINUTE, -10080); //让日历回到这周的周一
				}
		}
		else if (ruleDate.equalsIgnoreCase("year")
				|| ruleDate.equals("lable.report.typeyear")||"lable.report.typeyearReportType".equals(ruleDate))
			rightNow.add(Calendar.YEAR, -1);
		else if (ruleDate.equals("minute"))
			rightNow.add(Calendar.MINUTE, 2);
		return rightNow.getTime();
	}

	public static Long getSEcount(String sTime, String eTime) {
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		try {
			return (smpFmt1.parse(eTime).getTime() - smpFmt1
					.parse(sTime).getTime()) / 1000;
		} catch (ParseException e) {
			
			log.error("Error:" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 根据日期类型[规则]计算开始日期
	 * 
	 * @param String
	 *            date 日期
	 * @param String
	 *            ruleDate 日期类型[规则]
	 * 
	 * @return Date 开始日期
	 * 
	 */
	public static String getAddDate(String date, String ruleDate,String table) {
		Calendar reValue = Calendar.getInstance();
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		try {
			reValue.setTime(smpFmt1.parse(date));
		} catch (ParseException e) {
			
			log.error("Error:" + e.getMessage());
			e.printStackTrace();
		}
		//2012-04-19 杨轩嘉修改时间阀值, 为了配合二次统计模板策略
		//避免趋势图出现 非正常补0的情况
		if (ruleDate.equals("hour")){
			if(table.toLowerCase().indexOf("topflow") >= 0){
				reValue.add(Calendar.MINUTE, 5);// 5分钟阀值
			}else{
				reValue.add(Calendar.MINUTE, 10);// 10分钟阀值
			}
		}
		if (ruleDate.equals("day")){
			if(table.toLowerCase().indexOf("monitor") >= 0){
				reValue.add(Calendar.HOUR_OF_DAY, 1);// 1小时阀值
			}else if(table.toLowerCase().indexOf("topflow") >= 0){
				reValue.add(Calendar.HOUR_OF_DAY, 1);// 1小时阀值
			}else{
				reValue.add(Calendar.HOUR_OF_DAY, 6);// 6小时阀值
			}
		}
		
		if (ruleDate.equals("month"))
			reValue.add(Calendar.DAY_OF_YEAR, 1);// 1天阀值
		return smpFmt1.format(reValue.getTime());
	}

	public static List<Map> chartTableFormat(List<Map> data,Map<Object, Object> subMap){
		if (GlobalUtil.isNullOrEmpty(data) || GlobalUtil.isNullOrEmpty(subMap)) {
			return data;
		}
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		if (chartItems.length<2) {
			return data;
		}
		List<Map>removeList=new ArrayList<Map>();
		for(Map map:data){
			boolean isAllZero=true;
			for (String string : chartItems) {
				if (!GlobalUtil.isNullOrEmpty(map.get(string))) {
					double value=Double.valueOf(map.get(string).toString());
					if (value>0) {
						isAllZero=false;
						break;
					}
				}
			}
			if (isAllZero) {
				removeList.add(map);
			}
		}
		if (removeList.size()>0) {
			data.removeAll(removeList);
		}
		return data;
	}
	
	public static List<Map> mapFormat(Map<Object,Object> data,List<Map> resultdata, Map<Object, Object> sub){
		List<Map> result=null;
		Map<Object, Object> subMap=null;
		if (!GlobalUtil.isNullOrEmpty(data)) {
			result = (List<Map>) data.get("result");
			subMap= (Map<Object, Object>) data.get("subMap");
		}else{
			result=resultdata;
			subMap=sub;
		}
		
		if (GlobalUtil.isNullOrEmpty(result)) {
			return result;
		}
		String[] chartItems =StringUtil.toString(subMap.get("chartItem"), "").split(",");
		String category = subMap.get("category").toString();
		String valueString=result.get(0).get(category)==null?"":result.get(0).get(category).toString();
		valueString=valueString.length()>=19?valueString.substring(0,19):valueString;
		int resultcount=0;
		boolean status=true;
		for (int i=1,len=result.size();i<len;i++) {
			Map map = result.get(i);
			String objString = map.get(category)==null?" ":map.get(category).toString();
			objString=objString.length()>=19?objString.substring(0,19):valueString;
			if (valueString.equals(objString)) {
				resultcount=i;
				status=false;
				break;
			}
		}
		if (status) {
			resultcount=result.size();
		}
		int count=result.size()/resultcount;
		if (count>1 && !GlobalUtil.isNullOrEmpty(subMap.get("serise"))
				&& result.get(0).containsKey(subMap.get("serise").toString())) {
			boolean pririsk0=false;
			boolean pririsk1=false;
			boolean pririsk2=false;
			boolean pririsk3=false;
			boolean pririsk4=false;
			String serise=subMap.get("serise").toString();
			for (int i = 0; i < resultcount; i++) {
				Map map = result.get(i);
				for (int j = 1; j < count; j++) {
					Map map2=result.get(i+resultcount*j);
					if ("无危险".equals(map2.get(serise))) {
						for (int k = 0; k < chartItems.length; k++) {
							map.put(chartItems[k], map2.get(chartItems[k]));
						}
						pririsk0=true;
					}else if ("低危险".equals(map2.get(serise))) {
						for (int k = 0; k < chartItems.length; k++) {
							map.put(chartItems[k]+1, map2.get(chartItems[k]));
						}
						pririsk1=true;
					} else if ("一般危险".equals(map2.get(serise))) {
						for (int k = 0; k < chartItems.length; k++) {
							map.put(chartItems[k]+2, map2.get(chartItems[k]));
						}
						pririsk2=true;
					}else if ("高危险".equals(map2.get(serise))) {
						for (int k = 0; k < chartItems.length; k++) {
							map.put(chartItems[k]+3, map2.get(chartItems[k]));
						}
						pririsk3=true;
					}else if ("非常危险".equals(map2.get(serise))) {
						for (int k = 0; k < chartItems.length; k++) {
							map.put(chartItems[k]+4, map2.get(chartItems[k]));
						}
						pririsk4=true;
					}
				}
			}
			if (!GlobalUtil.isNullOrEmpty(data)) {
				data.put("result", result.subList(0, resultcount));
			}else{
				result=result.subList(0, resultcount);
			}
			if (pririsk0||pririsk1||pririsk2||pririsk3||pririsk4) {
				StringBuffer stringBuffer = new StringBuffer();
				for (int i = 0; i < chartItems.length; i++) {
					stringBuffer.append(pririsk4 ? (chartItems[i] + 4 + ",") : "")
							.append(pririsk3 ? (chartItems[i] + 3 + ",") : "")
							.append(pririsk2 ? (chartItems[i] + 2 + ",") : "")
							.append(pririsk1 ? (chartItems[i] + 1 + ",") : "")
							.append(chartItems[i]).append(",");
				}
				subMap.put("chartItem", stringBuffer.toString().substring(0, stringBuffer.length()-1));
			}
		}
		return result;
	}
	/**
	 * 
	 * 计算两个日期相差毫秒数,计算去那个表
	 * 
	 * @param String
	 *            sTime 开始时间
	 * 
	 * @param String
	 *            eTime 结束时间
	 * 
	 * @return String 日期相差秒
	 * 
	 */
	public static boolean countTime(String sTime, String eTime, String ruleDate) {
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		
		boolean reValue = false;
		try {
			long result = (smpFmt1.parse(eTime).getTime() - smpFmt1
					.parse(sTime).getTime()) / 1000;
			if (ruleDate.equals("hour") && result == 0)
				return true;
			if (ruleDate.equals("day") && result ==0)
				return true;
			if (ruleDate.equals("month") && result == 0)
				return true;
			if (ruleDate.equals("qushi") && result >= 0)
				return true;
			if (ruleDate.equals("qs") && result >= 0)
				return true;
		} catch (ParseException e) {
			log.error("Error:" + e.getMessage());
			e.printStackTrace();
		}
		return reValue;
	}
	
	public synchronized static Object sumValue(Object sumValue,Object value){
		if(sumValue==null){
			sumValue=0;
		}
		if(value==null){
			return sumValue;
		}
		
		if(sumValue instanceof String){
			sumValue=Integer.valueOf((String)sumValue);
		}
		
		if(value instanceof String){
			value=Integer.valueOf((String)value);
		}
		
		if(value instanceof Integer){
			Integer valueInteger=(Integer)value;
			Integer sumValueInteger=0;
			if(sumValue instanceof Integer){
				sumValueInteger=(Integer)sumValue;
			}else if(sumValue instanceof Long){
				sumValueInteger=((Long)sumValue).intValue();
			}else if(sumValue instanceof Float){
				sumValueInteger=((Float)sumValue).intValue();
			}else if(sumValue instanceof Double){
				sumValueInteger=((Double)sumValue).intValue();
			}
			sumValueInteger+=valueInteger;
			return sumValueInteger;
		}else if(value instanceof Long){
			Long valueLong=(Long)value;
			Long sumValueLong=0L;
			if(sumValue instanceof Integer){
				sumValueLong=((Integer)sumValue).longValue();
			}else if(sumValue instanceof Long){
				sumValueLong=(Long)sumValue;
			}else if(sumValue instanceof Float){
				sumValueLong=((Float)sumValue).longValue();
			}else if(sumValue instanceof Double){
				sumValueLong=((Double)sumValue).longValue();
			}
			sumValueLong+=valueLong;
			return sumValueLong;
		}else if(value instanceof Float){
			Float valueFloat=(Float)value;
			Float sumValueFloat=0f;
			if(sumValue instanceof Integer){
				sumValueFloat=((Integer)sumValue).floatValue();
			}else if(sumValue instanceof Long){
				sumValueFloat=((Long)sumValue).floatValue();
			}else if(sumValue instanceof Float){
				sumValueFloat=(Float)sumValue;
			}else if(sumValue instanceof Double){
				sumValueFloat=((Double)sumValue).floatValue();
			}
			sumValueFloat+=valueFloat;
			return sumValueFloat;
		}else if(value instanceof Double){
			Double valueDouble=(Double)value;
			Double sumValueDouble=0D;
			if(sumValue instanceof Integer){
				sumValueDouble=((Integer)sumValue).doubleValue();
			}else if(sumValue instanceof Long){
				sumValueDouble=((Long)sumValue).doubleValue();
			}else if(sumValue instanceof Float){
				sumValueDouble=((Float)sumValue).doubleValue();
			}else if(sumValue instanceof Double){
				sumValueDouble=(Double)sumValue;
			}
			sumValueDouble+=valueDouble;
			return sumValueDouble;
		}else if(value instanceof BigDecimal){
			BigDecimal valueDouble=(BigDecimal)value;
			BigDecimal sumValueBigDecimal=new BigDecimal(0);
			if(sumValue instanceof Integer){
				sumValueBigDecimal=new BigDecimal((Integer)sumValue);
			}else if(sumValue instanceof Long){
				sumValueBigDecimal=new BigDecimal((Long)sumValue);
			}else if(sumValue instanceof Float){
				sumValueBigDecimal=new BigDecimal((Float)sumValue);
			}else if(sumValue instanceof Double){
				sumValueBigDecimal=new BigDecimal((Double)sumValue);
			}else if(sumValue instanceof BigDecimal){
				sumValueBigDecimal=(BigDecimal)sumValue;
			}
			sumValueBigDecimal=sumValueBigDecimal.add(valueDouble);
			return sumValueBigDecimal;
		}
		return 0;
	}
	
	/**
	 * 
	 * 根据导出类型显示后缀Exp
	 * 
	 * @param String
	 *            expType 导出类型
	 * 
	 * @return String 导出类型对应的后缀
	 * 
	 */
	public static String getFileSuffix(String expType) {
		if (expType.equals("pdf")){
			return ".pdf";
		}
		else if (expType.equals("rtf")){
			return ".rtf";
		}
		else if (expType.equals("excel")){
			return ".xls";
		}
		else if(expType.equals("doc")){
			return ".doc";
		}
		else if(expType.equals("docx")){
			return ".docx";
		}else if(expType.equalsIgnoreCase("html")){
			return ".zip";
		}
		return null;
	}

	/**
	 * 
	 * 根据导出类型生成文件名
	 * 
	 * @param String
	 *            expType 导出类型
	 * 
	 * @return String 导出类型对应的文件名
	 * 
	 */
	public static String getFileName(String expType) {
		String reValue = null;
		String tempnameString=getNowTime("yyyy-MM-dd HH:mm:ss");
		tempnameString=tempnameString.replaceAll(" ", "_");
		tempnameString=tempnameString.replaceAll(":", "_");
		tempnameString=tempnameString.replaceAll("-", "_");
		tempnameString=tempnameString+(int)Math.sqrt(new Date().getTime())+(int)Math.random()*128;
		if (expType.equals("pdf"))
			reValue = tempnameString + ".pdf"; //System.currentTimeMillis() + UUID.randomUUID().toString() + ".pdf";
		else if (expType.equals("rtf"))
			reValue = tempnameString + ".rtf"; //System.currentTimeMillis() + UUID.randomUUID().toString() + ".rtf";
		else if (expType.equals("excel"))
			reValue = tempnameString + ".xls"; //System.currentTimeMillis() + UUID.randomUUID().toString() + ".xls";
		else if (expType.equals("docx"))
			reValue = tempnameString + ".docx"; //System.currentTimeMillis() + UUID.randomUUID().toString() + ".docx";
		else if (expType.equals("doc"))
			reValue = tempnameString + ".doc"; //System.currentTimeMillis() + UUID.randomUUID().toString() + ".doc";
		else if (expType.equals("html"))
			reValue = tempnameString + ".htm";
		return reValue;
	}
	
	 
	public static String getFileName2(ExpStruct exp) {
		String reValue = "";		
		String ext=exp.getFileType();
		String title=exp.getRptName();
		reValue=title+getFileName(ext);
		return reValue;
	}

	/**
	 * 
	 * 根据导出类型构建JRAbstractExporter
	 * 
	 * @param String
	 *            expType 导出类型
	 * 
	 * @return JRAbstractExporter 导出类型对应的JRAbstractExporter
	 * 
	 */
	public static JRAbstractExporter getJRExporter(String expType) {
		JRAbstractExporter exporter = null;
		if (expType.equals("pdf")){
			exporter = new JRPdfExporter();
		}
		else if (expType.equals("rtf")){
			exporter = new JRRtfExporter();
		}
		else if (expType.equals("excel")){
			exporter = new JRXlsxExporter();
		}
		else if (expType.equals("html")){
			exporter = new JRHtmlExporter();
		}
		else if (expType.equals("xml")){
			exporter = new JRXmlExporter();
		}
		else if (expType.equals("csv")){
			exporter = new JRCsvExporter();
		}
		else if (expType.equals("txt")){
			exporter = new JRTextExporter();
		}else if(expType.equals("docx")){
			exporter=new JRDocxExporter();
		}else if(expType.equals("doc")){
			exporter=new JRDocxExporter();
		}
		return exporter;
	}

	/**
	 * 
	 * 构建导出目录用分隔符
	 * 
	 * @return String 导出目录用分隔符
	 * 
	 */
	public static String getExpPaint() {
		StringBuffer reValue = new StringBuffer();
		for (int i = 0; i < ReportUiConfig.Paintl; i++)
			reValue.append(ReportUiConfig.PaintDot);
		return reValue.toString();
	}

	/**
	 * 获取国际化内容
	 * 
	 * @param String
	 *            str 国际化Key
	 * 
	 * @return String 国际化内容
	 */
	public static String getProperty(String str) {
		String reValue = null;
		reValue = property.getString(str);
		return reValue;
	}

	/**
	 * 
	 * 获取系统路径
	 * 
	 * @return String 系统路径
	 * 
	 */
	public static String getSysPath() {
		String path = (new ReportUiUtil().getClass().getProtectionDomain()
				.getCodeSource().getLocation()).toString();
		path = path.substring(5);
		path = path.substring(0, path.indexOf("classes") + 8);
		int osValue = 0;
		if (ReportUiUtil.isOs()){
			osValue = 1;
		}
		path = path.substring(osValue, path.length());
		return path;
	}
	
	/**
	 * 
	 * 获取系统lib路径
	 * 
	 * @return String 系统路径
	 * 
	 */
	public static String getSystemLibPath() {
		String path = (new ReportUiUtil().getClass().getProtectionDomain()
				.getCodeSource().getLocation()).toString();
		path = path.substring(5);
		path = path.substring(0, path.indexOf("classes") + 8);
		int osValue = 0;
		if (ReportUiUtil.isOs()){
			osValue = 1;
		}
		path = path.substring(osValue, path.length());
		path=path.substring(0, path.indexOf("applications/tsm-4sim.ear"));
		path+="server"+File.separatorChar+"default"+File.separatorChar+"lib"+File.separatorChar;
		return path;
	}

	/**
	 * 
	 * 根据类型获取导出文件路径
	 * 
	 * @param String
	 *            expType 导出文件类型
	 * @return String 系统路径
	 * 
	 */
	public static String getExpFilePath(String expType) {
		String reValue = null;
		String rootPath = getSysPath();
		Calendar date = Calendar.getInstance();
		String path = "/" + date.get(date.YEAR) + "/" + date.get(date.MONTH)
				+ "/" + date.get(date.DAY_OF_MONTH) + "/";
		reValue = rootPath + expType + path;
		return reValue;
	}

	/**
	 * 
	 * 根据类型获取导出文件路径
	 * 
	 * @param String
	 *            mstChartXmlPath 链接文件路径
	 * @return String 系统路径
	 * 
	 */
	public static String getSysPath(String mstChartXmlPath) {
		String reValue = getSysPath() + "ireport/" + mstChartXmlPath;
		return reValue;

	}

	/**
	 * 
	 * 获取主报表下拉列表列表[韩敏成计划报表用]
	 * 
	 * @param String
	 *            dvcType 设备类型
	 * @return List 主报表列表
	 * 
	 */
	public static List<Map> getMstList(String dvcType) {
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		// 设置前台显示参数
		String mstListSql = ReportUiConfig.PlanListSql;
		Object[] mstListParam = { dvcType };
		Object[] mstListParam2 = { dvcType.split("/")[0]};
		List mstListResult = null;
		if(mstListParam[0].equals("Firewall/Cisco/Pix")){
			List mstResult = rptMasterTbImp.queryTmpList(mstListSql,mstListParam);
			mstListResult = rptMasterTbImp.queryTmpList(mstListSql,	mstListParam2);
			mstListResult.add(0,mstResult.get(0));
		}else{
			mstListResult = rptMasterTbImp.queryTmpList(mstListSql,
					mstListParam);
			if (mstListResult.size() == 0){
				mstListResult = rptMasterTbImp.queryTmpList(mstListSql,	mstListParam2);
			}
		}
		List<Map> list= ReportUiUtil.getMstSelectList(mstListResult);
		return list;
	}

	/**
	 * 
	 * 获取主报表下拉列表[key value]列表[韩敏成计划报表用]
	 * 
	 * @param String
	 *            dvcType 设备类型
	 * @return List 下拉框key value列表
	 * 
	 */
	public static List<Map> getMstSelectList(List<Map> mstReslut) {
		ArrayList<String[]> reValue = new ArrayList<String[]>();
		List<Map> list = new ArrayList<Map>();
		for(Map map:mstReslut){
			Map rmap = new HashMap();
			rmap.put("id", map.get("id"));
			rmap.put("mstName", map.get("mstName"));
			rmap.put("viewItem", map.get("viewItem"));
			list.add(rmap);
		}
		return list;
	}

	/**
	 * 
	 * 获取主报表下拉列表[key value]列表[韩敏成计划报表用]
	 * 
	 * @param String
	 *            dvcType 设备类型
	 * @return List 下拉框key value列表
	 * 
	 */
	public static String getExpItemValue(ExpStruct exps, String itemValue) {
		if (!exps.getFileType().equals("pdf"))
			itemValue += "<br>";
		return itemValue;
	}

	/**
	 * 
	 * 遍历对象成员[审计用] getDeclaredMethods
	 * 
	 * @param Object
	 *            obj 待遍历对象
	 * @return String 对象内容
	 * 
	 */
	public static String objReflect(Object obj) {
		StringBuffer reValue = new StringBuffer();
		Method[] method = obj.getClass().getDeclaredMethods();
		String methodName = null;
		String value = null;
		for (int j = 0; j < method.length; j++) {
			methodName = method[j].getName();
			reValue.append("mname:").append(methodName);
			if (method[j].getName().indexOf("get") >= 0) {
				try {
					value = method[j].invoke(obj) + "";
				} catch (IllegalArgumentException e) {
					log.error("Error:" + e.getMessage());
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					log.error("Error:" + e.getMessage());
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					log.error("Error:" + e.getMessage());
					e.printStackTrace();
				}
				reValue.append(" ").append("mvalue:").append(value).append(" ");
			}
		}
		return reValue.toString();
	}

	/**
	 * 去掉小时
	 * 
	 * @param String
	 *            rptName 报表文件名
	 * @param String
	 *            bFlag 是否替换小时 true替换 false不替换 自定义名称
	 * @return boolean
	 */
	public static String changeRptName(String rptName, boolean bFlag) {
		if (rptName == null)
			return rptName;
		if (rptName.indexOf("*") >= 0) {
			rptName = rptName.replace("*", "");
			if (bFlag){
				rptName = rptName.replace("小时", "");
				rptName = rptName.replace("()", "");
			}
		}

		return rptName;
	}

	public static String changeRptName2(String rptName, boolean bFlag) {
		if (rptName == null)
			return rptName;
		if (rptName.indexOf("*") >= 0) {
			rptName = rptName.replace("*", "");
			if (bFlag){
				rptName = rptName.replace("小时", "");
			}
		}

		return rptName;
	}	
	
	/**
	 * 获取报表名称
	 * 
	 * @param String
	 *            rptName 报表文件名
	 * @param String
	 *            subrptType 报表类型 自定应 非自定义
	 * @return boolean
	 */
	public static String viewRptName(String rptName, String subrptType) {

		boolean subrptTypeFlag = false;
		if (subrptType != null && subrptType.equals("2"))// 自定义
			subrptTypeFlag = true;
		String subTitle = ReportUiUtil.changeRptName(rptName, subrptTypeFlag);
		return subTitle;
	}

	public static String viewRptName2(String rptName, String subrptType) {

		boolean subrptTypeFlag = false;
		if (subrptType != null && subrptType.equals("2"))// 自定义
			subrptTypeFlag = true;
		String subTitle = ReportUiUtil.changeRptName2(rptName, subrptTypeFlag);
		return subTitle;
	}	

	// 获取table
	public static String getTable(String sqlQuShi) {
		String reg = "from\\s+([^\\s]+)";
		String table = ((ReportUiUtil.getRegStr(reg, sqlQuShi)).split(" "))[1];
		return table;
	}

	/**
	 * 根据时间间隔返回报表类型
	 * @param sTime
	 * @param eTime
	 * @return
	 */
	public static String rptTypeByTime(String sTime,String eTime){
		try {
			SimpleDateFormat sim=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date sDate=sim.parse(sTime);
			Date eDate=sim.parse(eTime);
			if (eDate.getTime()-sDate.getTime()>0 && eDate.getTime()-sDate.getTime()<1000l*3600*24*2) {
				return "日报";
			}else if (eDate.getTime()-sDate.getTime()<1000l*3600*24*8){
				return "周报";
			}else if (eDate.getTime()-sDate.getTime()<1000l*3600*24*32){
				return "月报";
			}
			return "年报";
		} catch (ParseException e) {
			return "基础报表";
		}
	}
	/**
	 * 
	 * @param sTime
	 * @param eTime
	 * @return 表的名称
	 */
	public static String tableNameByTime(String sTime,String eTime){
		try {
			SimpleDateFormat sim=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date sDate=sim.parse(sTime);
			Date eDate=sim.parse(eTime);
			if (eDate.getTime()-sDate.getTime()>0 && eDate.getTime()-sDate.getTime()<1000l*3600*24*2) {
				return "DAY";
			}else if (eDate.getTime()-sDate.getTime()<1000l*3600*24*8){
				return "WEEK";
			}else if (eDate.getTime()-sDate.getTime()<1000l*3600*24*32){
				return "MONTH";
			}
			return "YEAR";
		} catch (ParseException e) {
			return "HOUR";
		}
	}
	/**
	 * 根据时间类型设置报表名称
	 * @param timetype
	 * @return
	 */
	public static String rptTypeByEgType(String timetype){
		try {
			if ("day".equalsIgnoreCase(timetype)) {
				return "日报";
			}else if ("week".equalsIgnoreCase(timetype)){
				return "周报";
			}else if ("month".equalsIgnoreCase(timetype)){
				return "月报";
			}else if ("year".equalsIgnoreCase(timetype)){
				return "年报";
			}
			return "基础报表";
		} catch (Exception e) {
			return "基础报表";
		}
	}
	/**
	 * sanitize 对HTML符号进行TAG处理 "&" --> "&amp" "<" --> "&lt" ">" --> "&gt" "\""
	 * --> "&quot" "\r\n" --> "<br>" &amp; &#39;
	 * 
	 * @param str
	 *            strin待转换的字符串
	 * @return String 处理后的字符串
	 * @version 20040810
	 */

	public static String sanitize(String s) {
		if (s == null)
			return null;
		StringBuffer stringbuffer = new StringBuffer();
		StringCharacterIterator stringcharacteriterator = new StringCharacterIterator(
				s);
		for (char c = stringcharacteriterator.first(); c != '\uFFFF'; c = stringcharacteriterator
				.next()) {
			String s1 = (String) sanitizeTable.get(new Character(c));
			if (s1 != null)
				stringbuffer.append(s1);
			else
				stringbuffer.append(c);
		}

		return stringbuffer.toString();
	}

	private static Hashtable sanitizeTable;
	static {
		sanitizeTable = new Hashtable();
		sanitizeTable.put(new Character('<'), "&lt;");
		sanitizeTable.put(new Character('>'), "&gt;");
		sanitizeTable.put(new Character('"'), "&quot;");
		sanitizeTable.put(new Character('&'), "&amp;");
		sanitizeTable.put(new Character('\''), "&#39;");
	}

	public static String getDeviceTypeName(String key,Locale locale){
		ResourceBundle rb = ResourceBundle.getBundle("resource.application",locale);
		String resValue = null;
		if(key != null){
			resValue = TalSourceTypeFactory.getInstance().getTypeName(key);
			if(resValue == null){
				try{
					if(key.indexOf("/") > 0){
						key = key.substring(0,key.indexOf("/"));
					}
					resValue = rb.getString(key);
				}catch(MissingResourceException me){
					try{
						resValue = key;
					}catch(MissingResourceException e){
						resValue = key;
					}
				}
			}
		}else{
			resValue = null;
		}
		return resValue;
	}

	public static void main(String[] args) {
		System.out.println("123.00000");
		System.out.println(getRound(4591.00D, 2));
	}
}

/*
 * TopSec-Ta-l 2009 系统名：Ta-L.Report 类一览 NO 类名 概要 1 QuarterDate Ta-L Report 获取季度类
 * 历史: NO 日期 版本 修改人 内容 1 2009/04/30 V1.0.1 Rick 初版
 */
class QuarterDate {

	private int x; // 日期属性：年

	private int y; // 日期属性：月

	private Calendar localTime; // 当前日期

	public QuarterDate() {
		localTime = Calendar.getInstance();
	}

	/**
	 * 功能：季度季初<br>
	 * 
	 * @return String
	 * @author
	 */
	public String getQuarterS() {
		String dateString = "";
		x = localTime.get(Calendar.YEAR);
		y = localTime.get(Calendar.MONTH) + 1;
		if (y >= 1 && y <= 3)
			dateString = x + "-" + "01" + "-" + "01";

		if (y >= 4 && y <= 6)
			dateString = x + "-" + "04" + "-" + "01";

		if (y >= 7 && y <= 9)
			dateString = x + "-" + "07" + "-" + "01";

		if (y >= 10 && y <= 12)
			dateString = x + "-" + "10" + "-" + "01";

		return dateString;
	}

	/**
	 * 功能：季度季末
	 * 
	 * @return String
	 * @author
	 */
	public String getQuarterE() {
		String dateString = "";
		x = localTime.get(Calendar.YEAR);
		y = localTime.get(Calendar.MONTH) + 1;
		if (y >= 1 && y <= 3)
			dateString = x + "-" + "03" + "-" + "31";

		if (y >= 4 && y <= 6)
			dateString = x + "-" + "06" + "-" + "30";

		if (y >= 7 && y <= 9)
			dateString = x + "-" + "09" + "-" + "30";

		if (y >= 10 && y <= 12)
			dateString = x + "-" + "12" + "-" + "31";

		return dateString;
	}
}
