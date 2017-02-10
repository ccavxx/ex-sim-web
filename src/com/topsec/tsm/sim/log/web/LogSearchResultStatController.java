package com.topsec.tsm.sim.log.web;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.index.template.DeviceTypeTemplate;
import com.topsec.tal.base.index.template.GroupCollection;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.index.template.LogField;
import com.topsec.tal.base.search.StatisticObject;
import com.topsec.tal.base.util.ChainMap;
import com.topsec.tal.base.util.LogUtils;
import com.topsec.tal.base.util.MapComparator;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.TreeNode;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.framework.statisticor.exception.StatisticException;
import com.topsec.tsm.framework.statisticor.metadata.OrderMeta;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.node.component.service.LogStatistic;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.log.bean.LogSearchObject;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.ExportExcelHandler;
import com.topsec.tsm.sim.util.ExportExcelUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;
import com.topsec.tsm.util.StringFormater;


/**
 * 日志查询结果统计
 * @author hp
 *
 */
@Controller
@RequestMapping("logSearchResultStat")
public class LogSearchResultStatController {
	/**SRC_ADDRESS*/
	private static String SRC = DataConstants.SRC_ADDRESS ;
	/**DEST_ADDRESS*/
	private static String DEST = DataConstants.DEST_ADDRESS ;
	/**DEST_PORT*/
	private static String DEST_PORT = DataConstants.DEST_PORT ;
	/**DVC_ADDRESS*/
	private static String DVC = DataConstants.DVC_ADDRESS ;
	
	private DataSourceService dataSourceSerivce;
	
	public static String SRC_WAN_CONDITION = 
			"(SRC_ADDRESS NOT BETWEEN '192.168.0.0' AND '192.168.255.255') AND " +
			"(SRC_ADDRESS NOT BETWEEN '172.16.0.0' AND '172.31.255.255') AND " +
			"(SRC_ADDRESS NOT BETWEEN '10.0.0.0' AND '10.255.255.255') AND" +
			"(SRC_ADDRESS NOT BETWEEN '127.0.0.0' AND '127.255.255.255') AND " +
			"(SRC_ADDRESS <> '0.0.0.0')" ;
	
	@Autowired
	@Qualifier("dataSourceService")
	public void setDataSourceSerivce(DataSourceService dataSourceSerivce) {
		this.dataSourceSerivce = dataSourceSerivce;
	}

	/**
	 * doLogFieldStatic 获取日志查询结果
	 * @return
	 */
	@RequestMapping("doLogFieldStatic")
	@ResponseBody
	public Object doLogFieldStatic(SID sid,@RequestBody LogSearchObject logSearchObject,HttpServletRequest request,HttpSession session) {
		StatisticObject searchObject = new StatisticObject();
		List<Map<String,Object>> datas = null;
		try {
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
			searchObject.countGroup(new String[]{logSearchObject.getStatColumn()},StatisticObject.COUNT_RESULT_FIELD_NAME,"DESC",-1) ;
			session.setAttribute("condition", searchObject);
			String[] route = RouteUtils.getQueryServiceRoutes();
			datas = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject,60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject jsonObject=new JSONObject();
		if(datas == null){
			jsonObject.put("flag", "failure");
			jsonObject.put("error","统计失败!");
		}else{
			List<Map<String, String>> columnTypes = IndexTemplateUtil.getInstance().getVisiableGroupColumnTypes(logSearchObject.getDeviceType(),logSearchObject.getGroup());
			String columnType="";
			for (Map<String, String> column : columnTypes) {
				Map.Entry<String, String> entry = column.entrySet().iterator().next();
				String key = entry.getKey();
				String value = entry.getValue();
                if(logSearchObject.getStatColumn().equals(key)){
                	columnType=value;
                	break;
                }				
			}
		    jsonObject.put("flag", "success");
		    Map<String,Object> stats = LogStatistic.list2Map(datas, logSearchObject.getStatColumn(), StatisticObject.COUNT_RESULT_FIELD_NAME) ;
			Map<String,Object> statResult=ObjectUtils.nvl(stats,Collections.<String,Object>emptyMap());

			JSONArray pieData = new JSONArray() ;
			JSONArray columnCategory= new JSONArray() ;
			JSONArray columnData= new JSONArray() ;
			JSONArray tableJsonArr= new JSONArray() ;
			int count = 1 ;
			double totalCount=0;
			String statColumn = logSearchObject.getStatColumn() ;
			for(Map.Entry<String, Object> entry:statResult.entrySet()){  
				if(count++>20){//如果数据超过20条，剩余的数据都合并为其它
			      	break ;
			    }
				String key=entry.getKey();           
				if(StringUtil.isBlank(key)){
					continue ;
				}
				
		        String value=StringUtil.toString(entry.getValue()); 
		        totalCount+=StringUtil.toDoubleNum(value);
		        if(statColumn.equalsIgnoreCase("PRIORITY")){
		        	key=CommonUtils.getLevel(key);
		        }else if(statColumn.equalsIgnoreCase("DVC_TYPE")){
		        	key=statColumn.substring(0,statColumn.indexOf("/"));
		        }
		      
		        columnCategory.add(key);
		        columnData.add(Integer.parseInt(value));
		        pieData.add(new Object[]{key,Integer.parseInt(value)});
		        
		        JSONObject tableJson=new JSONObject();
		        tableJson.put(statColumn,key);
		        tableJson.put("result",value);
		        tableJsonArr.add(tableJson);
			}
			DecimalFormat decimalFormat=new DecimalFormat("0.00");
			for(int i=0;i<tableJsonArr.size();i++){
				 JSONObject tempJson=tableJsonArr.getJSONObject(i);
				 double result=StringUtil.toDoubleNum(tempJson.get("result").toString());
				 double percentResult=result/totalCount*100;
				 tempJson.put("percent",decimalFormat.format(percentResult));
			}
			
			jsonObject.put("tableData", tableJsonArr);
			jsonObject.put("columnType", columnType);
		}
		return jsonObject;
	}
	/**
	 * 
	 * 导出 日志统计字段结果
	 * 
	 */
	@RequestMapping("exportLogField")
	public void exportLogField(HttpServletRequest request,HttpServletResponse response,SID sid,HttpSession session){
		String exportType = (String)request.getParameter("exportType");
		StatisticObject searchObject = (StatisticObject)session.getAttribute("condition");
		String statColumn = searchObject.getStatColumns().get(0);
		String statColumnName="";
		List<Map<String, String>> columnNames = IndexTemplateUtil.getInstance().getVisiableGroupColumnNames(searchObject.getType(),searchObject.getGroup());
	
		for (Map<String, String> column : columnNames) {
			Map.Entry<String, String> entry = column.entrySet().iterator().next();
		    if(statColumn.equals(entry.getKey())){
		    	statColumnName=entry.getValue();
		    	break;
		    }				
		}
		List<String> statColumns = new ArrayList<String>();
		statColumns.add(statColumn);
		searchObject.setStatColumns(statColumns);
		String[] route = RouteUtils.getQueryServiceRoutes();
		
		List<Map<String,Object>> datas = null;
		try {
			datas = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject, 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(datas == null){
			System.out.println("统计失败!");
		}else{
			Map<String,Object> statResult=LogStatistic.list2Map(datas, statColumn, StatisticObject.COUNT_RESULT_FIELD_NAME);
			List<Map<String,Object>> tableDatas= new ArrayList<Map<String,Object>>();
			int count = 1 ;
			long totalCount=0;
			for(Map.Entry<String, Object> entry:statResult.entrySet()){    
				if(count++ > 20){//如果数据超过20条，剩余的数据都合并为其它
			    	break ;
			    }
		        String key=entry.getKey();           
		        if(StringUtil.isBlank(key)){
		        	continue ;
		        }
		        totalCount += (Long)entry.getValue();
		        if(statColumn.equalsIgnoreCase("PRIORITY")){
		        	key=CommonUtils.getLevel(key);
		        }else if(statColumn.equalsIgnoreCase("DVC_TYPE")){
		        	key=statColumn.substring(0,statColumn.indexOf("/"));
		        }
				//表格数据
		        tableDatas.add(new ChainMap<String,Object>().push("name",key).push("result",entry.getValue()));
			}   
			DecimalFormat decimalFormat=new DecimalFormat("0.00");
			for (Map<String, Object> map:tableDatas) {
				double result=StringUtil.toDoubleNum(map.get("result").toString());
				double percentResult=result / totalCount * 100;
				map.put("percent",decimalFormat.format(percentResult));
			}
			try {
				String fileName = statColumnName+"统计." + exportType ;
				CommonUtils.setDownloadHeaders(request, response, fileName) ;
				if(exportType.equals("doc")){
					 
				}else if(exportType.equals("xls")){
					List<String> tableHead = new ArrayList<String>();
					tableHead.add(statColumn);
					tableHead.add(statColumnName);
					tableHead.add("结果");
					tableHead.add("百分比");
					ExportExcelUtil.exportExcel(response, tableHead, tableDatas, new ExportExcelHandler<List<Map<String,Object>>>() {
						@Override
						public void createSheetCell(HSSFSheet tableSheet, List<Map<String,Object>> tableDatas) {
							int rowIndex = 1;
							for (Map<String, Object> record : tableDatas) {
								HSSFRow tableRowData = tableSheet.createRow(rowIndex++);
								tableRowData.createCell(0).setCellValue(StringUtil.toString(record.get("name")));
								tableRowData.createCell(1).setCellValue(StringUtil.toString(record.get("result")));
								tableRowData.createCell(2).setCellValue(StringUtil.toString(record.get("percent")) + "%");
							}
						}
					});
				}else if(exportType.equals("pdf")){
					//exportPDF(response,title,pieImg,columnImg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	   	}
	}
	
	@RequestMapping("exportLogIPRelation")
	public void exportLogIPRelation(HttpServletRequest request,HttpServletResponse response,SID sid){
		try {
			String exportType = (String)request.getParameter("exportType");
			LogSearchObject logSearchObject = new LogSearchObject();
			logSearchObject.setHost((String)request.getParameter("host"));
			logSearchObject.setDeviceType((String)request.getParameter("deviceType"));
			logSearchObject.setGroup((String)request.getParameter("group"));
			logSearchObject.setQueryStartDate((String)request.getParameter("queryStartDate"));
			logSearchObject.setQueryEndDate((String)request.getParameter("queryEndDate"));
			StatisticObject searchObject = new StatisticObject();
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
			String[] fields = { DataConstants.SRC_ADDRESS, DataConstants.DEST_ADDRESS, "DEST_PORT" };
			searchObject.countGroup(fields, StatisticObject.COUNT_RESULT_FIELD_NAME, OrderMeta.DESC, 1000);
			String[] route = RouteUtils.getQueryServiceRoutes();
			List<Map<String, Object>> datas = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject, 5 * 60 * 1000);
			Collections.sort(datas,new MapComparator(StatisticObject.COUNT_RESULT_FIELD_NAME)) ;
			List<String> tableHead = new ArrayList<String>();
			tableHead.add("");
			tableHead.add("源地址");
			tableHead.add("目的地址");
			tableHead.add("数量");
//			String[] tableHead = new String[]{"","源地址", "目的地址", "数量"};
			CommonUtils.setDownloadHeaders(request, response, "IP关系图统计"+"."+exportType) ;
			if(exportType.equals("xls")){
				ExportExcelUtil.exportExcel(response, tableHead, datas, new ExportExcelHandler<List<Map<String, Object>>>(){
					@Override
					public void createSheetCell(HSSFSheet tableSheet, List<Map<String, Object>> tableDatas) {
						int rowIndex = 1;
						for (Map<String, Object> record : tableDatas) {
							String source = StringUtil.toString(record.get(SRC));
							String dest = StringUtil.toString(record.get(DEST));
							if(StringUtils.isBlank(source) || StringUtils.isBlank(dest)){
								continue;
							}
							HSSFRow tableRowData = tableSheet.createRow(rowIndex++);
							tableRowData.createCell(0).setCellValue(source);
							tableRowData.createCell(1).setCellValue(dest);
							tableRowData.createCell(2).setCellValue(StringUtil.toString(record.get(StatisticObject.COUNT_RESULT_FIELD_NAME)));
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<Map<String, Object>> buildExportLogIPRelation(Map<String,Object> treeData) throws Exception{
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> nodes = (List<Map<String, Object>>)treeData.get("nodes");
		List<Map<String, Object>> links = (List<Map<String, Object>>)treeData.get("links");
		for (Map<String, Object> node : nodes) {
			List<Map<String, Object>> tempDatalist = new ArrayList<Map<String,Object>>();
			String srcLoc = StringUtil.toString(node.get("text"));
			int index = Integer.valueOf(StringUtil.toString(node.get("index")));
			for (Map<String, Object> link : links) {
				String source = StringUtil.toString(link.get("source"));
				if(StringUtils.isNotBlank(source) && Integer.valueOf(source) == index){
					String target = StringUtil.toString(link.get("target"));
					if(StringUtils.isNotBlank(target)){
						Map<String, Object> tempData = new HashMap<String, Object>();
						tempData.put("srcLoc", srcLoc);
						tempData.put("count", link.get("count"));
						tempData.put("targetIndex", Integer.valueOf(target));
						tempDatalist.add(tempData);
					}
				}
			}
			for (Map<String, Object> entry : tempDatalist) {
				int targetIndex = Integer.valueOf(StringUtil.toString(entry.get("targetIndex")));
				for (Map<String, Object> searchTarget : nodes) {
					String destIndex = StringUtil.toString(searchTarget.get("index"));
					if(StringUtils.isNotBlank(destIndex) && Integer.valueOf(destIndex) == targetIndex){
						Map<String, Object> addData = new HashMap<String, Object>();
						addData.putAll(entry);
						addData.put("destLoc", StringUtil.toString(searchTarget.get("text")));
						result.add(addData);
					}
				}
			}
		}
		return result;
	}
	
	@RequestMapping("exportLogIPTrace")
	public void exportLogIPTrace(HttpServletRequest request,HttpServletResponse response,SID sid,@RequestParam(value="top",defaultValue="1000")Integer top){
		try {
			String exportType = (String)request.getParameter("exportType");
			LogSearchObject logSearchObject = new LogSearchObject();
			logSearchObject.setHost((String)request.getParameter("host"));
			logSearchObject.setDeviceType((String)request.getParameter("deviceType"));
			logSearchObject.setGroup((String)request.getParameter("group"));
			logSearchObject.setQueryStartDate((String)request.getParameter("queryStartDate"));
			logSearchObject.setQueryEndDate((String)request.getParameter("queryEndDate"));
			final String filterField = (String)request.getParameter("filterField");
			logSearchObject.setValue(filterField);
			String traceField = (String)request.getParameter("traceField");
			logSearchObject.setTraceField(traceField);
			StatisticObject searchObject = new StatisticObject();
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
			String[] traceGroupFields = request.getParameterValues("traceGroupFields") ;
			String[] fields = getTraceFields(traceField) ;
			Map<String,String> selectFields = ObjectUtils.merge(StringUtil.splitAsMap("=", true,traceGroupFields),fields) ;
			Map<String,Object> condition = new HashMap<String,Object>() ;
			if(StringUtil.isNotBlank(traceField)){
				condition.put(traceField, logSearchObject.getValue()) ;
			}
			if(logSearchObject.isOnlyWanSrc()){//只统计源地址为外网的数据
				condition.put(null, SRC_WAN_CONDITION) ;
			}
			searchObject.countGroup(selectFields, fields[0], condition, StatisticObject.COUNT_RESULT_FIELD_NAME, "DESC", top) ;
			final String[] allFields = selectFields.keySet().toArray(new String[0]) ;
			String[] route = RouteUtils.getQueryServiceRoutes();
			List<Map<String,Object>> datas = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject,5 * 60 * 1000);
			Collections.sort(datas, new MapComparator(StatisticObject.COUNT_RESULT_FIELD_NAME)) ;
//			TreeNode root = buildTraceTree(StringUtil.nvl(logSearchObject.getValue()),allFields, datas) ;
			GroupCollection gcollection = IndexTemplate.getTemplate(logSearchObject.getDeviceType()).getGroup(logSearchObject.getGroup());
			String filter = null;
			List<String> tableHead = new ArrayList<String>();
			tableHead.add("");
			if(StringUtils.isNotEmpty(traceField)){
				filter = gcollection.getField(traceField).getAlias();
				tableHead.add(filter);
			}
			for (String egname : allFields) {
				tableHead.add(gcollection.getField(egname).getAlias());
			}
			tableHead.add("数量");
			CommonUtils.setDownloadHeaders(request, response, "IP跟踪树统计"+"."+exportType) ;
			if(exportType.equals("xls")){
				ExportExcelUtil.exportExcel(response, tableHead, datas, new ExportExcelHandler<List<Map<String,Object>>>(){
					@Override
					public void createSheetCell(HSSFSheet tableSheet, List<Map<String,Object>> datas) {
						try {
							for (int i=0, len = datas.size(); i<len; i++) {
								String countStr = StringUtil.toString(datas.get(i).get("OPCOUNT"));
								int count = Integer.valueOf(countStr);
								if(count > 0){
									HSSFRow tableRowData = tableSheet.createRow(1+i);
									int index = 0;
									if(StringUtils.isNotEmpty(filterField)){
										tableRowData.createCell(index++).setCellValue(filterField);
									}
									for (String egname : allFields) {
										tableRowData.createCell(index++).setCellValue(StringUtil.toString(datas.get(i).get(egname)));
									}
									tableRowData.createCell(index).setCellValue(countStr);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@RequestMapping("exportLogMap")
	public void exportLogMap(HttpServletRequest request,HttpServletResponse response,SID sid, @RequestParam(value="type",defaultValue="4")int type){
		List<Map<String, Object>> datas = new ArrayList<Map<String,Object>>();
		String exportType = (String)request.getParameter("exportType");
		try {
			LogSearchObject logSearchObject = new LogSearchObject();
			logSearchObject.setHost((String)request.getParameter("host"));
			logSearchObject.setDeviceType((String)request.getParameter("deviceType"));
			logSearchObject.setGroup((String)request.getParameter("group"));
			logSearchObject.setQueryStartDate((String)request.getParameter("queryStartDate"));
			logSearchObject.setQueryEndDate((String)request.getParameter("queryEndDate"));
			logSearchObject.setSeq(Integer.valueOf(StringUtil.toString(request.getParameter("seq"))));
			logSearchObject.setStatColumn((String)request.getParameter("statColumn"));
			StatisticObject searchObject = new StatisticObject() ;
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
			String groupFunction = StringFormater.format("IpMapper({},{})", logSearchObject.getStatColumn(),type) ;
			//SRC_LOCATION无实际含义，此处只是使用此字段来存储IP地址定位信息
			//可以使用任何String类型的字段替换SRC_LOCATION
			searchObject.putSelect("SRC_LOCATION", groupFunction) ;
			searchObject.putSelect("OPCOUNT", "COUNT("+logSearchObject.getStatColumn()+")") ;
			searchObject.putGroupBy("SRC_LOCATION", groupFunction) ;
			String[] route = RouteUtils.getQueryServiceRoutes() ;
			List<Map<String,Object>> statResult = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject,5 * 60 * 1000);
			if(statResult != null){
				Collections.sort(statResult,new MapComparator(StatisticObject.COUNT_RESULT_FIELD_NAME)) ;
				for(Map<String,Object> item:statResult){
					if(StringUtil.isBlank((String)item.get("SRC_LOCATION")) || "未知".equals(item.get("SRC_LOCATION"))){
						continue ;
					}
					datas.add(ChainMap.newMap("name", item.get("SRC_LOCATION")).push("value", item.get("OPCOUNT"))) ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			datas = Collections.emptyList() ;
		}
		List<String> tableHead = new ArrayList<String>();
		tableHead.add("");
		tableHead.add("地区");
		tableHead.add("数量");
//		String[] tableHead = new String[]{"","地区", "数量"};
		CommonUtils.setDownloadHeaders(request, response, "IP数量分布统计"+"."+exportType) ;
		if(exportType.equals("xls")){
			ExportExcelUtil.exportExcel(response, tableHead, datas, new ExportExcelHandler<List<Map<String, Object>>>(){
				@Override
				public void createSheetCell(HSSFSheet tableSheet, List<Map<String, Object>> datas) {
					try {
						for (int i=0, len = datas.size(); i<len; i++) {
							HSSFRow tableRowData = tableSheet.createRow(1+i);
							tableRowData.createCell(0).setCellValue(StringUtil.toString(datas.get(i).get("name")));
							tableRowData.createCell(1).setCellValue(StringUtil.toString(datas.get(i).get("value")));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	@RequestMapping("doIpLocationStat")
	@ResponseBody
	public Object doIpLocationStat(SID sid,@RequestParam(value="type",defaultValue="4")int type,
			LogSearchObject logSearchObject,
			HttpServletRequest request){
		List<Map<String, Object>> datas = new ArrayList<Map<String,Object>>();
		try {
			logSearchObject.setOperator(StringUtil.nvl(StringUtil.recode(request.getParameter("operator")))) ;
			logSearchObject.setQueryContent(StringUtil.nvl(StringUtil.recode(request.getParameter("queryContent")))) ;
			StatisticObject searchObject = new StatisticObject() ;
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
			if(type < 0 || type > 5){
				return null ;
			}
			String groupFunction = StringFormater.format("IpMapper({},{})", logSearchObject.getStatColumn(),type) ;
			//SRC_LOCATION无实际含义，此处只是使用此字段来存储IP地址定位信息
			//可以使用任何String类型的字段替换SRC_LOCATION
			searchObject.putSelect("SRC_LOCATION", groupFunction) ;
			searchObject.putSelect("OPCOUNT", "COUNT("+logSearchObject.getStatColumn()+")") ;
			searchObject.putGroupBy("SRC_LOCATION", groupFunction) ;
			String[] route = RouteUtils.getQueryServiceRoutes() ;
			List<Map<String,Object>> statResult = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject,5 * 60 * 1000);
			if(statResult != null){
				for(Map<String,Object> item:statResult){
					if(StringUtil.isBlank((String)item.get("SRC_LOCATION")) || "未知".equals(item.get("SRC_LOCATION"))){
						continue ;
					}
					datas.add(ChainMap.newMap("name", item.get("SRC_LOCATION")).push("value", item.get("OPCOUNT"))) ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			datas = Collections.emptyList() ;
		}
		return datas ;
	}
	
	@RequestMapping("showLogIpStatPage")
	public String showLogIpStatPage(SID sid,@RequestParam("goPage")String goPage,HttpServletRequest request,LogSearchObject logSearchObject,HttpSession session){
		//因为前台使用的get(Filter的setCharacterEncoding对get方法无效)方法提交的数据，当数据注入到logSearchObject中时会出现乱码
		//因此，此处需要重新编码
		logSearchObject.setOperator(StringUtil.recode(request.getParameter("operator"))) ;
		logSearchObject.setQueryContent(StringUtil.recode(request.getParameter("queryContent"))) ;
		session.setAttribute("logSearchObject", logSearchObject) ;
		if("trace".equals(goPage)){
			request.setAttribute("fields", IndexTemplate.getTemplate(logSearchObject.getDeviceType()).getGroup(logSearchObject.getGroup()).getVisibleFields()) ;
			return "/page/log/logIpTrace" ;
		}else if("relation".equals(goPage)){
			return "/page/log/logIpRelation" ;
		}else{
			return null;
		}
	}
	/**
	 * 根据查询缓存和统计字段进行数据统计，并根据统计结果(Top n)生成跟踪树(从fields第一个字段到最后一个字段)<br>
	 * 例如:指定fields={DVC_ADDRESS,SRC_ADDRESS,DEST_ADDRESS}，生成过程如下<br>
	 * 1、从查询结果中获取每一条日志(直到limit限制)<br>
	 * 2、根据fields字段做分组统计(计数Count)，并根据orderFields字段为统计结果排序<br>
	 * 3、从统计结果中截取Top n的数据<br>
	 * 4、依次的循环每一条数据，根据fields生成从第一个字段到最后一个字段的树形结构
	 * @param sid
	 * @param session
	 * @return
	 */
	@RequestMapping("doIpTraceStat")
	@ResponseBody
	public Object doIpTraceStat(SID sid,@RequestParam(value="top",defaultValue="1000")Integer top,HttpServletRequest request,HttpSession session){
		JSONObject result = new JSONObject() ;
		try {
			LogSearchObject logSearchObject = (LogSearchObject) session.getAttribute("logSearchObject") ;
			StatisticObject searchObject = new StatisticObject();
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
			String traceField = logSearchObject.getTraceField() ;
			String[] traceGroupFields = request.getParameterValues("traceGroupFields") ;
			String[] fields = getTraceFields(traceField) ;
			Map<String,String> selectFields = ObjectUtils.merge(StringUtil.splitAsMap("=", true,traceGroupFields),fields) ;
			Map<String,Object> condition = new HashMap<String,Object>() ;
			if(StringUtil.isNotBlank(traceField)){
				condition.put(traceField, logSearchObject.getValue()) ;
			}
			if(logSearchObject.isOnlyWanSrc()){//只统计源地址为外网的数据
				condition.put(null, SRC_WAN_CONDITION) ;
			}
			searchObject.countGroup(selectFields, fields[0], condition, StatisticObject.COUNT_RESULT_FIELD_NAME, "DESC", top) ;
			String[] allFields = selectFields.keySet().toArray(new String[0]) ;
			String[] route = RouteUtils.getQueryServiceRoutes();
			List<Map<String,Object>> datas = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject,5 * 60 * 1000);
			Collections.sort(datas, new MapComparator(allFields)) ;
			TreeNode root = buildTraceTree(StringUtil.nvl(logSearchObject.getValue()),allFields, datas) ;
			DeviceTypeTemplate template = IndexTemplate.getTemplate(searchObject.getType()) ;
			GroupCollection collection = template.getGroup(searchObject.getGroup()) ;
			List<LogField> fieldsInfo = collection.getField(Arrays.asList(allFields),true)  ;
			result.put("data", root) ;
			result.put("size", datas.size()) ;
			result.put("reverse", DEST.equals(traceField) || DEST_PORT.equals(traceField)) ;
			result.put("fieldsInfo", FastJsonUtil.toJSONArray(fieldsInfo,"name","type","alias","searchable")) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result ;
	}
	
	private static String[] getTraceFields(String traceField){
		if(StringUtil.isBlank(traceField)){
			return new String[]{SRC,DEST,DEST_PORT};
		}else if(traceField.equals(DVC)){
			return new String[]{SRC,DEST,DEST_PORT};
		}else if(traceField.equals(SRC)){
			return new String[]{DEST,DEST_PORT};
		}else if(traceField.equals(DEST)){
			return new String[]{DEST_PORT,SRC};
		}else if(traceField.equals(DEST_PORT)){
			return new String[]{SRC,DEST};
		}else{
			return new String[0] ;
		}
	}
	
	/**
	 * 依次的循环每一条数据，根据fields生成从第一个字段到最后一个字段的树形结构<br>
	 * 实例:<br>
	 * 例如统计数据为:<br>
	 * [{DVC_ADDRESS:192.168.75,80,SRC_ADDRESS:192.168.75.90,DEST_ADDRESS:192.168.75.100}, <br>
	 *  {DVC_ADDRESS:192.168.75,80,SRC_ADDRESS:192.168.75.91,DEST_ADDRESS:192.168.75.200}, <br>
	 *  {DVC_ADDRESS:192.168.75,80,SRC_ADDRESS:192.168.75.91,DEST_ADDRESS:192.168.75.300}] <br>
	 * 最终树形结构为:<br>
	 *                 192.168.75.90 >>>>> 192.168.75.100
	 *               / 
	 * 192.168.75.80                        192.168.75.200
	 *               \                    / 
	 *                 192.168.75.91 >>>>>
	 *                                    \  192.168.75.300
	 * @throws StatisticException
	 */
	public static TreeNode buildTraceTree(String rootName,String[] fields,List<Map<String,Object>> datas) throws StatisticException{
		TreeNode root = new TreeNode("",StringUtil.nvl(rootName)).setProperty("count", 0L) ;
		for(Map<String,Object> entry:datas){
			TreeNode parent = root ;
			for(String field:fields){
				if((parent = buildNode4Field(parent, entry, field)) == null){
					break ;
				}
			}
			root.setProperty("count", (Long)root.getProperty("count")+(Long)entry.get(StatisticObject.COUNT_RESULT_FIELD_NAME)) ;
		}
		return root ;

	}
	
	private static TreeNode buildNode4Field(TreeNode parent,Map<String,Object> log,String field){
		Object value = log.get(field) ;
		String childId =  LogUtils.fieldToString(field, -1, value);
		if(StringUtil.isBlank(childId)){
			return null;
		}
		TreeNode childNode = parent.getChildById(childId) ;
		if(childNode == null){
			parent.addChild((childNode = new TreeNode(childId, childId).setProperty("count", 0L))) ;
		}
		childNode.setProperty("count", (Long)childNode.getProperty("count")+(Long)log.get(StatisticObject.COUNT_RESULT_FIELD_NAME)) ;
		return childNode ;
	}
	
	@RequestMapping("doIpRelationStat")
	@ResponseBody
	public Object doIpRelationStat(SID sid,@RequestParam(value="top",defaultValue="1000")Integer top,HttpSession session){
		JSONObject result = new JSONObject() ;
		try {
			LogSearchObject logSearchObject = (LogSearchObject) session.getAttribute("logSearchObject") ;
			StatisticObject searchObject = new StatisticObject();
			logSearchObject.fillSearchObject(searchObject) ;
			searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
			String[] fields = {SRC,DEST,"DEST_PORT"} ;
			searchObject.countGroup(fields, StatisticObject.COUNT_RESULT_FIELD_NAME, OrderMeta.DESC, top) ;
			String[] route = RouteUtils.getQueryServiceRoutes();
			List<Map<String,Object>> datas = NodeUtil.dispatchCommand(route, MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject,5 * 60 * 1000);
			Map<String,Object> data = CommonUtils.buildRelationTree(SRC, DEST, DEST_PORT, StatisticObject.COUNT_RESULT_FIELD_NAME, datas) ;
			result.put("data", data) ;
			result.put("count", datas.size()) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result ;
	}

	@RequestMapping("logsTimeLine")
	public Object logsTimeLine(
			SID sid,
			@RequestParam(value="top",defaultValue="500")Integer top,
			HttpServletRequest request,
			LogSearchObject logSearchObject,
			HttpSession session){
		logSearchObject.setValue(StringUtil.recode(request.getParameter("value"))) ;
		logSearchObject.setOperator(StringUtil.recode(request.getParameter("operator"))) ;
		logSearchObject.setQueryContent(StringUtil.recode(request.getParameter("queryContent"))) ;
		StatisticObject searchObject = new StatisticObject();
		logSearchObject.fillSearchObject(searchObject) ;
		searchObject.setUserDevice(dataSourceSerivce.getUserDataSourceAsString(sid, false));//设置查询权限
		DeviceTypeTemplate template = IndexTemplate.getTemplate(searchObject.getType()) ;
		GroupCollection group = template.getGroup(searchObject.getGroup()) ;
		if(group == null){
			return "/page/log/logsTimeLine";
		}
		List<LogField> fields = group.getVisibleFields() ;
		for(LogField field:fields){
			searchObject.putSelect(field.getName(), field.getName()) ;
		}
		if(StringUtil.isNotBlank(logSearchObject.getTraceField())){
			searchObject.putWhere(logSearchObject.getTraceField(), logSearchObject.getValue()) ;
		}
		searchObject.putOrderBy(DataConstants.START_TIME, "ASC") ;
		searchObject.setTop(500) ;
		try {
			List<Map<String,Object>> logs = NodeUtil.dispatchCommand(RouteUtils.getQueryServiceRoutes(), MessageDefinition.CMD_SEARCH_RESULT_STAT, searchObject,5 * 60 * 1000);
			List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>() ;
			String lastDate = null ;
			List<Map<String,Object>> logList = null ;
			for(Map<String,Object> logItem:logs){
				String startDate = StringUtil.dateToString((Date)logItem.get(DataConstants.START_TIME)) ;
				if(!startDate.equals(lastDate)){
					Map<String,Object> dataItem = new HashMap<String, Object>(2,1.0F) ;
					dataItem.put("date", startDate) ;
					dataItem.put("logs", logList = (new ArrayList<Map<String,Object>>())) ;
					datas.add(dataItem) ;
					lastDate = startDate;
				}
				logList.add(logItem) ;
			}
			LogField traceField = group.getField(logSearchObject.getTraceField());
			request.setAttribute("datas", datas) ;
			request.setAttribute("traceFieldAlias",traceField == null ? "日志" : traceField.getAlias()) ;
			request.setAttribute("traceFieldValue", logSearchObject.getValue()) ;
			request.setAttribute("fields", fields) ;
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		return "/page/log/logsTimeLine" ;
	}
}
