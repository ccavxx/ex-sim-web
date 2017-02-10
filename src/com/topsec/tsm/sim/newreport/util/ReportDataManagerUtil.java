package com.topsec.tsm.sim.newreport.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.newreport.model.ReportQuery;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.NodeUtil;

/**
 * @ClassName: ReportDataManagerUtil
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2016年6月3日下午4:24:41
 * @modify: 
 * <p>此类的方法多线程不安全</p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public final class ReportDataManagerUtil {
	static final String TMP="_TMP";
	static final String BASE="_BASE";
	static final String HOUR="_HOUR";
	static final String DAY="_DAY";
	static final String MONTH="_MONTH";
	static final String DEL_SQL="DELETE FROM ";
	static final String CONDITION_SQL=" WHERE RESOURCE_ID= ";
	static final String DELETE_TIME_DAY_SQL=" AND START_TIME < ADDDATE(CURRENT_TIMESTAMP(), INTERVAL -$HISLONG DAY) ";
	
	public static void deleteReportData(SimDatasource datasource) {
		ReportQuery reportQuery=(ReportQuery)SpringContextServlet.springCtx.getBean("reportQuery");
		List<String> executeSqls=getDelSqlList(datasource,reportQuery);
		if (null ==executeSqls) {
			return;
		}
		Map<String, Object>paramMap=new HashMap<String, Object>();
		paramMap.put("executeSqls", executeSqls);
		paramMap.put("operation", "delete");
		paramMap.put("simDatasource", datasource);
		try {
			NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade"); ;
			
			Node node = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_REPORTSERVICE, false, false, false, false).get(0) ;
			String[] routes = NodeUtil.getRoute(node) ;//RouteUtils.getReportRoutes(node)
			
			NodeUtil.sendCommand(routes, MessageDefinition.CMD_REPORT_DEL_DATA,(Serializable)paramMap,10*1000) ;
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	}
	public static List<String> getDelSqlList(SimDatasource datasource,ReportQuery reportQuery){
		List<String> sqlList=null;
		
		List<String> relevanceTables=getAllRelevanceTables(datasource,reportQuery);
		if (null == relevanceTables ) {
			return sqlList;
		}
		Long resourceId=datasource.getResourceId();
		sqlList=new ArrayList<String>(relevanceTables.size());
		for (String tableName : relevanceTables) {
			sqlList.add(getDelSql(tableName,resourceId));
		}
		
		return sqlList;
	}
	private static List<String> getAllRelevanceTables(SimDatasource datasource,ReportQuery reportQuery) {
		List<String> relevanceTables=null;
		if (null == datasource) {
			return relevanceTables;
		}
		List<String>tables=reportQuery.findTableNameListByType(datasource.getSecurityObjectType());
		if (null == tables || tables.size()==0) {
			return relevanceTables;
		}
		relevanceTables=new ArrayList<String>();
		for (String string : tables) {
			String[]relevanceTabs=getRelevanceTables(string);
			if (null !=relevanceTabs) {
				for (String retab : relevanceTabs) {
					if (! relevanceTables.contains(retab)) {
						relevanceTables.add(retab);
					}
				}
			}
			
		}
		return relevanceTables;
	}
	private static String getDelSql(String tableName,Long resourceId){
		StringBuffer sql=new StringBuffer(DEL_SQL);
		sql.append(tableName).append(CONDITION_SQL).append(resourceId);
		return sql.toString();
	}
	public static String[] getRelevanceTables(String tableName){
		if (GlobalUtil.isNullOrEmpty(tableName)) {
			return null;
		}
		if (!tableName.contains(HOUR)) {
			return new String[]{tableName};
		}
		String []tables=new String[7];
		tableName=tableName.toUpperCase();
		tables[0]=tableName.replace(HOUR, BASE);
		tables[1]=tableName;
		tables[2]=tables[1]+TMP;
		tables[3]=tableName.replace(HOUR, DAY);
		tables[4]=tables[3]+TMP;
		tables[5]=tableName.replace(HOUR, MONTH);
		tables[6]=tables[5]+TMP;
		return tables;
	}
	
	public static void removeHistoryByDatasource(SimDatasource datasource) {
		ReportQuery reportQuery=(ReportQuery)SpringContextServlet.springCtx.getBean("reportQuery");
		List<String> executeSqls=getDeleteHistorySqlList(datasource,reportQuery);
		if (null ==executeSqls) {
			return;
		}
		Map<String, Object>paramMap=new HashMap<String, Object>();
		paramMap.put("executeSqls", executeSqls);
		paramMap.put("flag", "delete");
		paramMap.put("simDatasource", datasource);
		try {
			NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade"); ;
			
			Node node = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_REPORTSERVICE, false, false, false, false).get(0) ;
			String[] routes = NodeUtil.getRoute(node) ;//RouteUtils.getReportRoutes(node)
			
			NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_DATASOURCE_DELETE,(Serializable)paramMap,600000) ;
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	}
	public static List<String> getDeleteHistorySqlList(SimDatasource datasource,ReportQuery reportQuery){
		List<String> sqlList=null;
		
		List<String> relevanceTables=getAllRelevanceTables(datasource,reportQuery);
		if (null == relevanceTables ) {
			return sqlList;
		}
		Long resourceId=datasource.getResourceId();
		int keepTime = timeUnitToDayNum(datasource.getReportKeepTime());
		sqlList=new ArrayList<String>(relevanceTables.size());
		for (String tableName : relevanceTables) {
			sqlList.add(getDeleteHistorySql(tableName,resourceId,keepTime));
		}
		
		return sqlList;
	}
	
	public static String getDeleteHistoryName(String tableName){
		if (GlobalUtil.isNullOrEmpty(tableName)
				|| !tableName.toUpperCase().contains(HOUR)) {
			return tableName;
		}
		return tableName.replace(HOUR, MONTH);
	}
	
	private static String getDeleteHistorySql(String tableName,Long resourceId,int daylong){
		String sqlString = getDelSql(tableName, resourceId);
		String delCdt = DELETE_TIME_DAY_SQL.replace("$HISLONG", daylong+"");
		return sqlString+delCdt;
	}
	private static int timeUnitToDayNum(String keepTime){
		int defaultTime=90;
		if (! GlobalUtil.isNullOrEmpty(keepTime)) {
			keepTime = keepTime.toUpperCase();
			int num = Integer.valueOf(keepTime.substring(0, keepTime.length()-1));
			String unit = keepTime.substring(keepTime.length()-1);
			if("D".equals(unit))
				return num;
			else if("M".equals(unit))
				return num*30;
			else if("Y".equals(unit))
				return num*365;
		}
		return defaultTime;
	}
	
}
