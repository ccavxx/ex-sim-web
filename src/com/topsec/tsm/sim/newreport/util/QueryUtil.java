package com.topsec.tsm.sim.newreport.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.common.common.TalSourceTypeFactory;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

/**
 * @ClassName: QueryUtil
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月13日下午12:22:23
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class QueryUtil {
	public static final String TIME_FORMAT="yyyy-MM-dd HH:mm:ss";
	public static final String TIME_FORMAT_SHOT = "yyyy-MM-dd";
	public static final String TIME_DAY_MINUTE="dd HH:mm";
	public static final String ZERO_TIME = " 00:00:00";
	public static final String FINALLY_TIME = " 23:59:59";

	public static final String SECURITY_OBJECT_TYPE="securityObjectType";
	public static final String DVC_ADDRESS="dvcAddress";
	public static final String NODE_ID="nodeIds";
	public static final String PAGE_INDEX="pageIndex";
	public static final String PAGE_SIZE="pageSize";
	public static final String PARENT_IDS="parentIds";
	public static final String START_TIME="stime";
	public static final String END_TIME="endtime";
	public static final String EXECUTE_TIME="executeTime";
	public static final String PARENT_SUB_ID="parentSubId";
	public static final String PARAMS="params";
	public static final String EXPORT_FORMAT="exportFormat";
	public static final String SUB_ID="subId";
	public static final String TOP_N="topn";
	public static final String AUTHOR="author";
	public static final String EXPORT_HEADLINE="exportHeadline";
	public static final String EXPORT_CATEGORY="exportCategory";
	public static final String RESULT_DATA="data";
	public static final String QUERY_CONDITIONS_OBJ="queryConditionsObj";
	public static final String RESULT_DATA_AND_STRUCTURE="resultDataAndStructure";
	public static final String REPORT_SUMMARY="reportSummary";
	public static final String REPORT_DESC="reportDesc";
	public static final String RESOURCE_ID="resourceId";
	
	public static Long stringTimeToLong(String time) {
		if (GlobalUtil.isNullOrEmpty(time)) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT);
		try {
			return simpleDateFormat.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static String longTimeToString(Long time){
		Date date=new Date(time);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT);
		return simpleDateFormat.format(date);
	}
	public static Date toDate(String dateString,String format){
		if(GlobalUtil.isNullOrEmpty(dateString)){
			return null ;
		}
		Date date = null ;
		try{
			SimpleDateFormat fmt = new SimpleDateFormat(format) ;
			date = fmt.parse(dateString) ;
		}catch(Exception e){}
		return date ;
	}
	
	public static Date toDate(String dateString){
		return toDate(dateString, TIME_FORMAT) ;
	}
	
	public static String nowTime(String dateformat) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		String reValue = dateFormat.format(now);
		return reValue;
	}
	
	public static String dateStringFormat(String dateformat,Date date) {
		if (null ==date) {
			return "未知时间";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		String reValue = dateFormat.format(date);
		return reValue;
	}
	
	public static void setQueryTime(ReportQueryConditions queryConditions) {
		if (null == queryConditions) {
			return;
		}
		if (GlobalUtil.isNullOrEmpty( queryConditions.getStime())) {
			queryConditions.setStime(QueryUtil.nowTime(QueryUtil.TIME_FORMAT_SHOT)+QueryUtil.ZERO_TIME);
		}
		if (GlobalUtil.isNullOrEmpty(queryConditions.getEndtime())) {
			queryConditions.setEndtime(QueryUtil.nowTime(QueryUtil.TIME_FORMAT_SHOT+" HH:00:00"));
		}
	}
	
	public static ReportQueryConditions getRequestReportQueryConditions(HttpServletRequest request){
		ReportQueryConditions queryConditions=new ReportQueryConditions();
		queryConditions.setDvcAddress(request.getParameter(QueryUtil.DVC_ADDRESS));
		queryConditions.setResourceId(Long.valueOf(request.getParameter(QueryUtil.RESOURCE_ID)));
		queryConditions.setEndtime(request.getParameter(QueryUtil.END_TIME));
		queryConditions.setExportFormat(request.getParameter(QueryUtil.EXPORT_FORMAT));
		queryConditions.setNodeIds(request.getParameterValues(QueryUtil.NODE_ID));
		queryConditions.setPageIndex(Long.valueOf(request.getParameter(PAGE_INDEX)));
		queryConditions.setPageSize(Long.valueOf(request.getParameter(PAGE_SIZE)));
		queryConditions.setParams(request.getParameter(QueryUtil.PARAMS));
		String []parentIdString=request.getParameterValues(QueryUtil.PARENT_IDS);
		Integer[]parentIds=new Integer[parentIdString.length];
		for (int i = 0; i < parentIdString.length; i++) {
			parentIds[i]=Integer.valueOf(parentIdString[i]);
		}
		queryConditions.setParentIds(parentIds);
		queryConditions.setParentSubId(Integer.valueOf(request.getParameter(QueryUtil.PARENT_SUB_ID)));
		queryConditions.setSecurityObjectType(request.getParameter(QueryUtil.SECURITY_OBJECT_TYPE));
		queryConditions.setStime(request.getParameter(QueryUtil.START_TIME));
		queryConditions.setTopn(Integer.valueOf(request.getParameter(QueryUtil.TOP_N)));
		/*Map<String,String[]> map=request.getParameterMap();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
		}*/
		return queryConditions;
	}
	
	public static String getFileSuffix(String exportFormat) {
		if ("pdf".equalsIgnoreCase(exportFormat)){
			return ".pdf";
		}
		else if ("rtf".equalsIgnoreCase(exportFormat)){
			return ".rtf";
		}
		else if ("excel".equalsIgnoreCase(exportFormat)
				||"xls".equalsIgnoreCase(exportFormat)
				||"xlsx".equalsIgnoreCase(exportFormat)){
			return ".xlsx";
		}
		else if("doc".equalsIgnoreCase(exportFormat)
				||"docx".equalsIgnoreCase(exportFormat)){
			return ".docx";
		}else if("html".equalsIgnoreCase(exportFormat)){
			return ".zip";
		}
		return null;
	}
	
	public static String timeQuantum(String startTime,String endTime){
		if (GlobalUtil.isNullOrEmpty(startTime) || GlobalUtil.isNullOrEmpty(endTime)) {
			return "报表";
		}
		long sval=stringTimeToLong(startTime);
		long eval=stringTimeToLong(endTime);
		long timeQuantumVal=eval-sval;
		if (0<=timeQuantumVal&& timeQuantumVal<2*86400000L) 
			return "日报";
		if (timeQuantumVal<8*86400000L) 
			return "周报";
		if (timeQuantumVal<32*86400000L) 
			return "月报";
		return "年报";
	}
	
	public static String timeFormat(Date startTime,Date endTime){
		if (GlobalUtil.isNullOrEmpty(startTime) || GlobalUtil.isNullOrEmpty(endTime)) {
			return TIME_FORMAT;
		}
		long sval=startTime.getTime();
		long eval=endTime.getTime();
		long timeQuantumVal=eval-sval;
		if (0<=timeQuantumVal&& timeQuantumVal<86400000) {
			return TIME_FORMAT;
		}else if (timeQuantumVal<7*86400000L) {
			return TIME_DAY_MINUTE;
		}else {
			return TIME_FORMAT_SHOT;
		}
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
	
	public static String getQueryDvcAddresses(ReportQueryConditions queryConditions){
		if (GlobalUtil.isNullOrEmpty(queryConditions.getDvcAddress())) {
			return "";
		}
		if("ALL_ROLE_ADDRESS".equals(queryConditions.getDvcAddress())){
			Map<String, Object>paraMap=queryConditions.getParamMap();
			if (null == paraMap || paraMap.size()<1) {
				return "";
			}
			Object paramValue=paraMap.get("DVC_ADDRESS");
			boolean isF=false;
			if (null != paramValue) {
				StringBuffer stringBuffer=new StringBuffer();
				if (paramValue instanceof Object[]){
					Object[]objects=(Object[])paramValue;
					if (objects.length>1) {
						stringBuffer.append("设备地址（包含以下：");
						for (int i = 0; i < objects.length; i++) {
							stringBuffer.append(objects[i]).append(",");
						}
						stringBuffer.append("）");
						isF=true;
					}
				}
				if (!isF) {
					stringBuffer.append("设备地址（").append(paramValue).append("）");
				}
				return stringBuffer.toString();
			}
			
		}else if("ONLY_BY_DVCTYPE".equals((queryConditions.getDvcAddress()))){
			return "设备地址（该类型所有的设备）";
		}
		return "设备地址（"+queryConditions.getDvcAddress()+"）";
	}
	public static <T>List<T> copyList(List<T> list){
		if (null ==list) {
			return null;
		}
		List<T>rTs=new ArrayList<T>(list.size());
		rTs.addAll(list);
		return rTs;
	}
	
	public static SimDatasource containsIpDatasource(List<SimDatasource> simDatasources,String ip){
		if (null == ip || null == simDatasources || simDatasources.size() ==0) {
			return null;
		}
		for (SimDatasource simDatasource : simDatasources) {
			if (ip.equals(simDatasource.getDeviceIp())) {
				return simDatasource;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static String replaceReportName(String securityObjectType,String reportName){
		List<Map<String, Object>> fieldList=null;
		Map<String, Object> templateMap=IndexTemplateUtil.getInstance().getTemplateByDeviceType(securityObjectType);
		if (null != templateMap && templateMap.size() != 0)
			fieldList=(List<Map<String, Object>>)templateMap.get("fieldList");
		if (null == fieldList) 
			return reportName;
		int leftco=reportName.indexOf("{");
		int rightco=reportName.indexOf("}",leftco);
		if (-1 != leftco && -1 != rightco) {
			String needRepalce=reportName.substring(leftco+1,rightco);
			Object reval="";
			for (Map<String, Object> field : fieldList)
				if (field.get("name").equals(needRepalce)) 
					reval=field.get("alias");
			reportName=reportName.substring(0,leftco)+reval+reportName.substring(rightco+1);
			
		}
		return reportName;
	}
	
	@SuppressWarnings("unchecked")
	public static String filedAliasName(String securityObjectType,String filedName) {
		List<Map<String, Object>> fieldList=null;
		Map<String, Object> templateMap=IndexTemplateUtil.getInstance().getTemplateByDeviceType(securityObjectType);
		if (null != templateMap && templateMap.size() != 0)
			fieldList=(List<Map<String, Object>>)templateMap.get("fieldList");
		if (null == fieldList) 
			return filedName;
		for (Map<String, Object> field : fieldList)
			if (field.get("name").equals(filedName)) 
				return	(String)field.get("alias");	
		
		return filedName;
	}
	
	public static void aliasMapFiledValue(String securityObjectType,Map<String, Object> map){
		if (GlobalUtil.isNullOrEmpty(securityObjectType)) {
			return;
		}
		if (null !=map && map.size()!=0) {
			List<Map<String, Object>> fieldList=null;
			Map<String, Object> templateMap=IndexTemplateUtil.getInstance().getTemplateByDeviceType(securityObjectType);
			if (null != templateMap && templateMap.size() != 0)
				fieldList=(List<Map<String, Object>>)templateMap.get("fieldList");
			if (null != fieldList){
				
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					//key 暂时没有别名的需要
					String key=entry.getKey();
					//暂时只对 key  为 subReportName的数据进行处理
					if(!"subReportName".equals(key))continue;
					//value 需要更改别名
					Object object=entry.getValue();
					String revalue=(null == object)?"":object.toString();
					int leftco=revalue.indexOf("{");
					int rightco=revalue.indexOf("}",leftco);
					if (-1 != leftco && -1 != rightco) {
						String needRepalce=revalue.substring(leftco+1,rightco);
						Object reval="";
						for (Map<String, Object> field : fieldList)
							if (field.get("name").equals(needRepalce)) 
								reval=field.get("alias");
						revalue=revalue.substring(0,leftco)+reval+revalue.substring(rightco+1);
						
					}
					map.put(key, revalue);
				}
				 
			} 
		}
	}
	
	public static String fileStartPath(){
	 	String filePath=ReportUiUtil.getSysPath();
		filePath=filePath.substring(0, filePath.length()-16);
		return filePath;
	}
	
	public static String getSavePath(String eDirectory){
		return fileStartPath()+"download"+eDirectory;
	}
}
