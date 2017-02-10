package com.topsec.tsm.sim.report.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.topsec.license.util.ChangePageEncode;
import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.asset.service.TopoService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.bean.ReportBean;
import com.topsec.tsm.sim.report.bean.struct.ExpDateStruct;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.bean.struct.SqlStruct;
import com.topsec.tsm.sim.report.chart.ReportTalChart;
import com.topsec.tsm.sim.report.common.ReportDataComparable;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.report.util.ThreadPoolExecuteDispatchUtil;
import com.topsec.tsm.sim.report.util.TopoUtil;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.FacadeUtil;
import com.topsec.tsm.util.thread.TsmThreadFactory;

public class ReportModel {
	private static Logger log = Logger.getLogger(ReportModel.class);
	/**
	 * 在前台数据展示时，有些数据需要格式化，数据格式化后会使用此值+原始键存储原始的未被格式化的数据
	 */
	public static final String UNFMT_DATA_PREFIX = "UNFORMAT_DATA_";
	public static final int showNo=28;
	public static SqlStruct getSqlStruct(int mstType, List ruleResult, HttpServletRequest request, ExpStruct exp,Map<Object, Object> subMap,String sTime,String eTime){
		// 报表类别//mstType==null子报无该项//1系统报表
		SqlStruct sqlStruct = getHqlTerm(mstType, ruleResult, request, exp,subMap,sTime,eTime);
		if (sqlStruct.getDvcIp() == null){
			String localIp = IpAddress.getLocalIp().toString();
			if(localIp.indexOf(':')>=0)
				localIp = "::1";
			else 
				localIp = "127.0.0.1";
			sqlStruct.setDvcIp(localIp);
		} 
		
		if(request!=null){
			String onlyByDvctype=request.getParameter("onlyByDvctype");
			if(onlyByDvctype!=null&&onlyByDvctype.equals("onlyByDvctype")){
				String deviceTypeZh ="";
				String dvctype=request.getParameter("dvctype");
				if(dvctype != null && dvctype.contains("Monitor/")){
					deviceTypeZh=ReportUiUtil.getDeviceTypeName(dvctype.replace("Monitor/", ""), request.getLocale());
				}else{
					deviceTypeZh=ReportUiUtil.getDeviceTypeName(dvctype, request.getLocale());
				}
				sqlStruct.setDvcIp(deviceTypeZh);
			} 
			
		}else{
			String dvc = exp.getDvc();
			if(dvc!=null){
				if(!"Log/Global/Detail".equals(dvc)){
					String deviceTypeZh ="";
					if(dvc.contains("Monitor/")){
						deviceTypeZh=ReportUiUtil.getDeviceTypeName(dvc.replace("Monitor/", ""), Locale.getDefault());
					}else{
						deviceTypeZh=ReportUiUtil.getDeviceTypeName(dvc, Locale.getDefault());
					}
					sqlStruct.setDvcIp(deviceTypeZh);
					sqlStruct.setDevTypeName(dvc);
				}			
			}
		}
		return sqlStruct;
    }
	private static List<String> getparamIpList(HttpServletRequest request){
		ReportBean bean = new ReportBean();
		bean = ReportUiUtil.tidyFormBean(bean, request);
		List<String>paramipList=assGroupIpList(bean,(TopoService) SpringContextServlet.springCtx.getBean("topoService"));
		return paramipList;
	}
	/**
	 * 拼子报表的sql where条件
	 * 
	 * @param int
	 *            mstType 报表类别(系统/非系统)
	 * @param List
	 *            ruleResult 子报表对应的规则、值表信息
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @param ExpStruct
	 *            exp 报表导出用结构体
	 * @return SqlStruct 子报表的sql where条件结构体
	 */
	private static SqlStruct getHqlTerm(int mstType, List ruleResult, HttpServletRequest request, ExpStruct exp,Map<Object, Object> subMap,String sTime,String eTime) {
		String _TalStartTime=ReportUiUtil.getNowTime(ReportUiConfig.dFormat2)+ ReportUiConfig.sTimePostfix;//当天开始时间
		String _TalEndTime=ReportUiUtil.getNowTime(ReportUiConfig.dFormat2)+ ReportUiConfig.eTimePostfix;//当天结束时间
		String type=null;
		try {
			type=request.getParameter(ReportUiConfig.dvctype);
		} catch (Exception e) {
		}
		boolean isevtReport="Profession/Group".equals(type);
		boolean evtOther=false;
		SqlStruct sqlStruct = new SqlStruct();
		List sqlParam = new ArrayList();
		Integer paramIndex = null; // 前台参数对应名
		StringBuffer sqlTerm = new StringBuffer();
		Map<Object,Object> map = new HashMap<Object,Object>();
		for (int i = 0; i < ruleResult.size(); i++) {
			map = (Map) ruleResult.get(i);
			paramIndex = (Integer) map.get("htmlField"); 
			String talCategoryKey=(String)map.get("sqlParam");
			boolean onlyDvc =false; //设备类型IP
			if ("and paramIps = ?".equals(talCategoryKey)) {
				List<String>paramipList=getparamIpList(request);
				map.put("sqlParam",TopoUtil.getIpSqlParm(paramipList));
			}
			if ("and DVC_ADDRESS in ?".equals(talCategoryKey)) {
				List<String>paramipList=getparamIpList(request);
				String paramString=TopoUtil.getIpSqlParmScope(paramipList);
				map.put("sqlParam","and DVC_ADDRESS in "+paramString);
			}
			if ("and dvcAddress = ?".equals(talCategoryKey)
					|| "and alias.dvcAddress = ?".equals(talCategoryKey)
					|| "and fwrisk.dvcAddress = ?".equals(talCategoryKey)) {
				
				if (request != null) {
					String onlyByDvctype = request.getParameter("onlyByDvctype");
					
					if (onlyByDvctype != null&& onlyByDvctype.equals("onlyByDvctype")) {
						
						if (request.getParameter("dvctype") != null) {
							map.remove("sqlParam");
							String key = talCategoryKey.replace("= ?", " = ").replace("and", "");
							map.put("sqlParam"," and ("+ getDvcIp(request.getParameter("dvctype"),key) + ")");
							onlyDvc = true;
						}
					}
				}

				String sqlValue = (String) map.get("sqlValue");
				if (sqlValue != null && sqlValue.indexOf("onlyByDvctype") != -1) {
					String[] sqlValueArray = sqlValue.split(";;;");
					if (request != null) {
						request.setAttribute("dvctype", sqlValueArray[1]);
						request.setAttribute("onlyByDvctype", sqlValueArray[0]);
					}
					sqlStruct.setDevTypeName(sqlValueArray[1]);
					sqlStruct.setDvcIp("onlyByDvctype");
					String deviceTypeZh = ReportUiUtil.getDeviceTypeName(
							sqlValueArray[1], Locale.getDefault());
					sqlStruct.setOnlyByDvctype(deviceTypeZh);
					map.remove("sqlParam");

					String key = talCategoryKey.replace("= ?", " = ").replace("and", "");
					map.put("sqlParam",	" and (" + getDvcIp(sqlValueArray[1], key) + ")");
					onlyDvc = true;
				}
				
				if (exp != null) {
					String onlyByDvctype2 = null;
					onlyByDvctype2 = exp.getOnlyByDvctype();
					if (onlyByDvctype2 != null
							&& onlyByDvctype2.equals("onlyByDvctype")) {
						map.remove("sqlParam");
						String key = talCategoryKey.replace("= ?", " = ")
								.replace("and", "");
						if (exp.getDvc() != null) {
							String[] sqlValueArray = exp.getDvc().split(";;;");
							map.put("sqlParam",	" and (" + getDvcIp(sqlValueArray[1], key)+ ")");
						}
						onlyDvc = true;
					}
					
					onlyByDvctype2 = exp.getRptIp();
					if (onlyByDvctype2 != null
							&& onlyByDvctype2.equals("onlyByDvctype")) {
						map.remove("sqlParam");
						String key = talCategoryKey.replace("= ?", " = ").replace("and", "");
						map.put("sqlParam"," and (" + getDvcIp(exp.getDvc(), key) + ")");
						onlyDvc = true;
					}
				}

			}

			// 获取前台参数
			String htmlField = null;
			// 若发计划报表则request为null
			// 趋势报表viewItem为空 2:为开始时间 3:为结束时间
			// _TalStartTime:当天开始时间_TalEndTime:当天结束时间
			if ((subMap.get("viewItem") == null
					|| subMap.get("viewItem").equals(""))
					&& paramIndex.equals(2)) {
				if (ReportUiUtil.checkNull(sTime)) {
					htmlField = sTime;
				} else {
					htmlField = _TalStartTime;
				}
			} else if ((subMap.get("viewItem") == null
					|| subMap.get("viewItem").equals(""))
					&& paramIndex.equals(3)) {
				if (ReportUiUtil.checkNull(eTime)) {
					htmlField = eTime;
				} else {
					htmlField = _TalEndTime;
				}
			}
			if (request != null) {
				if (subMap.get("viewItem") != null
						&& !subMap.get("viewItem").equals("")
						&& paramIndex.equals(2)) {
					htmlField = getHtmlField(request, paramIndex,
							talCategoryKey);
				} else if (subMap.get("viewItem") != null
						&& !subMap.get("viewItem").equals("")
						&& paramIndex.equals(3)) {
					htmlField = getHtmlField(request, paramIndex,
							talCategoryKey);
				} else if (htmlField == null){
					htmlField = getHtmlField(request, paramIndex,
							talCategoryKey);
				}
			}

			// 为了导出下钻报表而写
			if (htmlField == null && exp != null && paramIndex == 6) {
				// {sqlParam=and cat1ID = ?, htmlField=6, ruleDisplay=1,
				// sqlValue=系统审核, sqlDefValue=系统审核, ruleName=19}
				if (mstType == 3 && map.get("ruleDisplay") != null
						&& map.get("sqlValue") != null
						&& map.get("sqlDefValue") != null) {
					if ((Integer) map.get("ruleDisplay") == 1
							&& map.get("sqlValue").equals(map.get("sqlDefValue"))) {
						htmlField = (String) map.get("sqlValue");
					}
				} else {
					String param = ReportUiConfig.Html_Field.get(paramIndex);
					htmlField = (String) exp.getMap().get(param);
				}
			}

			String ruleName = map.get("ruleName").toString().trim();// 规则名称
			if (ruleName.equals("1")) {// 1 top规则
				// 前台不为空获取默认值
				Object top = null;// HB分页不需要DB方言
				if (request != null) {
					top = (htmlField == null ? getDefValue(map, mstType)
							: htmlField);
				} else {
					top = exp.getTop();
				}
				sqlStruct.setSqlpage(Integer.parseInt(top.toString()));
				continue;
			}
			if (ruleName.equals("7")) {// union
				sqlParam = getUnion(sqlParam,
						Integer.parseInt(getDefValue(map, mstType).toString()));
				sqlStruct.setSqlparam(sqlParam);
				return sqlStruct;
			}
			
			// 显示项才有参数,0、1不显示,有没有可能有默认值或 前台可能有参数来。
			if (!ruleName.equals("0") && !onlyDvc) {
				Object tmpO = null;
				// 前台参数为空 获取该项默认的数值 sqlDefValue 字段对应的数值
				/*String param = ReportUiConfig.Html_Field.get(6);
				if (htmlField == null && exp != null && paramIndex == 0 && exp.getMap().containsKey(param)){
					htmlField = (String) exp.getMap().get(param);
				}*/

				if (htmlField == null) {
					tmpO = getDefValue(map, mstType, exp, request);
				} else {
					tmpO = htmlField;
				}
				if (isevtReport) {
					if(!GlobalUtil.isNullOrEmpty(tmpO)){
						sqlParam.add(tmpO);
					}
				}else {
					if(null !=tmpO){
						sqlParam.add(tmpO);
					}
				}
				// 若为导出,则需要记录的相关信息
				setExpTime(sqlStruct, ruleName, map, mstType, tmpO);// String类型可能有问题
				// 如果规则是下探规则，同时记录下探信息
				if (ReportUiConfig.digRuleList.indexOf("," + ruleName + ",") >= 0) {
					String[] talCategoryArray = sqlStruct.getTalCategory();
					String[] talCategoryArrayNew;
					if (talCategoryArray == null) {
						talCategoryArrayNew = new String[1];
					} else {
						talCategoryArrayNew = new String[talCategoryArray.length + 1];
						System.arraycopy(talCategoryArray, 0,
								talCategoryArrayNew, 0, talCategoryArray.length);
					}
					talCategoryArrayNew[talCategoryArrayNew.length - 1] = sqlParam
							.get(sqlParam.size() - 1).toString();
					sqlStruct.setTalCategory(talCategoryArrayNew);
				}
			}
			Object oSqlParam = map.get("sqlParam");
			if (oSqlParam != null)
				sqlTerm.append(" " + map.get("sqlParam") + " ");
		}
		sqlStruct.setSql(sqlTerm.toString());
		sqlStruct.setSqlparam(sqlParam);
		return sqlStruct;
	}
	
	private static List<String> assGroupIpList(ReportBean bean,TopoService topoService){
		List<String>ipList=new ArrayList<String>();
		String rootId=bean.getRootId();
		if ("0".equals(rootId)) {
			//
		}
		AssTopo assTopo=null;
		if (!GlobalUtil.isNullOrEmpty(bean.getTopoId())&&!"allAssTopos".equals(bean.getTopoId())) {
			if ("-1".equals(bean.getTopoId())) {
				assTopo=topoService.getSystemTopo((NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade"));
			}else {
				assTopo=topoService.get(Integer.valueOf(bean.getTopoId()));
			}
		}
		if (null!=assTopo&&!GlobalUtil.isNullOrEmpty(bean.getAssGroupNodeId())) {
			if ("1".equals(bean.getNodeLevel())) {
				List<Device>devices=TopoUtil.showIpsByAssTopo(assTopo);
				if (!GlobalUtil.isNullOrEmpty(devices)) {
					for (Device device : devices) {
						if (!ipList.contains(device.getMasterIp().toString())) {
							ipList.add(device.getMasterIp().toString());
						}
					}
				}
			}else {
				List<Device>devices=TopoUtil.showIpsByAssGroup(assTopo, bean.getAssGroupNodeId());
				for (Device device : devices) {
					if (!ipList.contains(device.getMasterIp().toString())) {
						ipList.add(device.getMasterIp().toString());
					}
				}
			}
		}
		return ipList;
	}
	/**
	 * 构建子报表的Html
	 * 
	 * @param String
	 *            sql 待执行的Sql
	 * @param List
	 *            sqlParam sql所用参数
	 * @param Integer
	 *            iPage Top(n)
	 *         tableName: 表名   
	 *         nodeIds: 节点
	 * @return List DB Result|页数
	 * @throws Exception 
	 * @throws Exception
	 */
	public static List getList(String sql, List sqlParam, Integer iPage,String tableName,String[] nodeIds,HttpServletRequest request) throws Exception {
		log.debug("*************Rick Start***************");
		log.debug(sql);
		log.debug(sqlParam);
		log.debug(iPage);
		log.debug("*************Rick End***************");
		List reValue = new ArrayList();
		List result = null;
		String type=null;
		try {
			type=request.getParameter(ReportUiConfig.dvctype);
		} catch (Exception e) {
		}
		boolean isevtReport="Profession/Group".equals(type);
		boolean isevtAssetReport="Profession/Group/Asset".equals(type);
		if(nodeIds==null&&!isevtReport){
			log.error("getList(),nodeIds==null!!!");
			throw new Exception("getList(),nodeIds==null!!!");
		}
		String flag=null;
		if (sql.indexOf("union") != -1) {
			flag="union";
				if(request!=null){
					String onlyByDvctype = request.getParameter("onlyByDvctype");
					if("onlyByDvctype".equals(onlyByDvctype)){
						sql=sql.replace("DVC_ADDRESS=?", " ("+getDvcIp(request.getParameter("dvctype"),"DVC_ADDRESS =")+")");
					}else if("onlyByDvctype".equals(request.getAttribute("onlyByDvctype"))){
						sql=sql.replace("DVC_ADDRESS=?", " ("+getDvcIp(request.getAttribute("dvctype").toString(),"DVC_ADDRESS =")+")");
					}
				}
		} else {
			flag="list";
		}
		NodeMgrFacade nodeMgrFacade = (NodeMgrFacade)SpringContextServlet.springCtx.getBean("nodeMgrFacade");
		if (isevtReport || (isevtAssetReport && sql.contains("SIM_EVENT_"))) {
			flag="union";
		}
		List<ReportDispatchModel> tList = new ArrayList<ReportDispatchModel>();
		int len = nodeIds.length;
		for (int i = 0; i < len; i++) {
			ReportDispatchModel reportDispatchModel = new ReportDispatchModel();
			reportDispatchModel.setNodeId(nodeIds[i]);
			reportDispatchModel.setNodeMgrFacade(nodeMgrFacade);
			reportDispatchModel
					.setCmd(MessageDefinition.CMD_REPORT_GET_RESULT);
			Map<String, Object> map = reportDispatchModel.getMap();
			map.put("sql", sql);
			map.put("sqlParam", sqlParam);
			if (iPage != null) {
				map.put("iPage", iPage * 4);
			}
			map.put("tableName", tableName);
			map.put("flag", flag);
			tList.add(reportDispatchModel);
		}
		int wenNo=stringNumbers(sql,"?");
		if (wenNo>sqlParam.size()) {
			result=new ArrayList<List<Map>>();
		}else{
			result = reportDispatch(tList, request);
		}
		
		List list=null;
		try {
			list = (List) result.get(0);
			if (!isevtReport && !isevtAssetReport) {
				List deviceTypes=null;
				List deviceIps =null;
				DataSourceService dataSourceService = (DataSourceService) SpringContextServlet.springCtx
						.getBean("dataSourceService");
				if (!GlobalUtil.isNullOrEmpty(SID.currentUser())) {
					SID sid = SID.currentUser();
					deviceTypes = getDeviceTypeList(dataSourceService,sid);
					deviceIps = getDeviceIpList(dataSourceService,sid);
				}
				noIpTypeAndNumberNullFormat(deviceTypes, deviceIps, list);
			}
			if (isevtReport) {
				evtReportFormat(list, request);
			}
			if (isevtAssetReport) {
				priorityShowCnFormat(list);
			}
		} catch (Exception e) {
		}
		
		if (sql.indexOf("union") != -1) {
			//设置几个极大数值,防止数据被过滤掉
			reValue.add(GlobalUtil.isNullOrEmpty(list)?9999:list.size());//9999
		} else {
			reValue.add(iPage);
		}
		reValue.add(result);
		return reValue;
	}
	
	/**
	* @method: reportDispatch 
	* 		从各个Auditor得到报表数据
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  tList:模板集合
	* 		   request: HttpServletRequest
	* @return: result
	*/
	public static List reportDispatch(List<ReportDispatchModel> tList,HttpServletRequest request){
		List result=new ArrayList();
		try {
			ThreadPoolExecuteDispatchUtil<ReportDispatchModel> threadPoolExecuteDispatchUtil=new ThreadPoolExecuteDispatchUtil<ReportDispatchModel>(tList);
			ThreadPoolExecutor threadPoolExecutor=null;
			if(request==null){
				threadPoolExecutor=(ThreadPoolExecutor)SpringContextServlet.springCtx.getBean("commondDispatchThreadPool");
			}else{
				threadPoolExecutor=(ThreadPoolExecutor)FacadeUtil.getWebApplicationContext(request).getBean("commondDispatchThreadPool");
			}
			
			threadPoolExecuteDispatchUtil.setThreadPool(threadPoolExecutor);
			threadPoolExecuteDispatchUtil.execute();
			 
			while(true){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Boolean f=true;
				for (ReportDispatchModel rdm : tList) {
					if(!rdm.isQueryComplete()){
						f=false;
						break;
					}
				}
				if(f){
					break;
				}
			}
			if(tList!=null){
				for (ReportDispatchModel reportDispatchModel : tList) {
					result.add(reportDispatchModel.getList());
				}
			}
		} catch (Exception e) {
			 log.error(e.getMessage());
		}
		return result;
	}
	
	/**
	 * 获取规则对应默认值
	 * 
	 * @param Map
	 *            map 子报表对应的规则、值表信息
	 * @param int
	 *            mstType 报表类别(系统/非系统)
	 * @return Object 获取规则对应默认值
	 */
	private static Object getDefValue(Map map, int mstType) {
		Object reValue = null; 
		if (mstType == 2){
			reValue = map.get("sqlValue").toString();
		}else{
			reValue = map.get("sqlDefValue").toString();
		}
		return reValue;
	}
	
	/**
	 * 获取子报表默认数值
	 * 
	 * @param Map
	 *            map 子报表对应的规则、值表信息
	 * @param int
	 *            mstType 报表类别(系统/非系统)
	 * @param ExpStruct
	 *            exp 报表导出用结构体
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @return Object 系统默认值
	 */
	private static Object getDefValue(Map map, int mstType, ExpStruct exp, HttpServletRequest request) {
		Object reValue = getDefValue(map, mstType);
		if (reValue.equals("talsysdates")) {
			if (exp != null && request == null)
				reValue = exp.getRptTimeS();// String
			else
				reValue = getTimeValue("rick1979");// String
		} else if (reValue.equals("talsysdaten")) {
			if (exp != null && request == null)
				reValue = exp.getRptTimeE();// String
			else
				reValue = getTimeValue("addNow");// String
		} else if (ReportUiUtil.checkNull(map.get("ruleName"))&& map.get("ruleName").toString().equals("8")){
			reValue = getExpValue(map, mstType, exp, request);
		}
		String ruleName = map.get("ruleName").toString().trim();
		if (ruleName.equals("2") && mstType != 2 && exp != null){
			// 设备ip dvc
			reValue = exp.getRptIp();
		}
		return reValue;
	}
	
	/**
	 * 获取规则对应日期
	 * 
	 * @param String
	 *            dType 规则8的规则类型
	 * @return Object 按照规则8的规则返回日期
	 */
	private static Object getTimeValue(String dType) {
		Object reValue = null;
		if (dType.equals("addNow"))
			reValue = ReportUiUtil.getNowTime(ReportUiConfig.dFormat2 + ReportUiConfig.eTimePostfix);
		else
			reValue = ReportUiUtil.getNowTime(ReportUiConfig.dFormat2+ ReportUiConfig.sTimePostfix);
		return reValue;
	}
	
	/**
	 * 根据规则获取前台数值
	 * 
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @param Integer
	 *            paramIndex Html parameter
	 * @return String 前台获取的数值
	 * @throws UnsupportedEncodingException 
	 */
	private static String getHtmlField(HttpServletRequest request, Integer paramIndex,String talCategoryKey) {
		String htmlField=null;
		if(paramIndex==6){
			String[] talCategoryArray=request.getParameterValues(ReportUiConfig.Html_Field.get(paramIndex));
			if(talCategoryArray!=null&&talCategoryArray.length>0){
				for (int j = 0; j < talCategoryArray.length; j++) {
					if(talCategoryArray[j]!=null&&!talCategoryArray[j].equals("")&&!talCategoryArray[j].equals("null")){
						String[] talCategoryValueArray=StringUtils.split(talCategoryArray[j],"***");
						String talCategoryKeyConvert=ReportUiConfig.ColumnRuleMap.get(talCategoryValueArray[0]);       
						if(!GlobalUtil.isNullOrEmpty(talCategoryKeyConvert)
								&&talCategoryKey.indexOf(talCategoryKeyConvert)!=-1){ 
							String talcat=talCategoryValueArray[1];
							if (talcat.indexOf("%")>-1) {
								try {
									talcat=URLDecoder.decode(talcat, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								htmlField=talcat;
							}else {
								htmlField=ChangePageEncode.IsoToUtf8(talcat);
							}
							
						}
					}
				}
			}
		}else{
			htmlField = request.getParameter(ReportUiConfig.Html_Field.get(paramIndex));
		}
		
		if (htmlField !=null && htmlField.equals(ReportUiConfig.NA)){
			htmlField = "";
			return htmlField;
		}
		if (ReportUiUtil.checkNull(htmlField))
			return htmlField;
		else
			return null;
				
	}
	
	/**
	 * 根据设备类型 或许该类型下的所有数据源IP
	 * @param request
	 * @return
	 */
	public static String getDvcIp(String dvc,String key){
		String dvcIP ="";
		DataSourceService dataSourceService = (DataSourceService)SpringContextServlet.springCtx.getBean("dataSourceService");
		List<Map<String, Object>> list = null;
		try {
			list = dataSourceService.getDataSourceTreeWithNodeList(null);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				if (!GlobalUtil.isNullOrEmpty(dvc)&&dvc.replace("Monitor/", "").equals((String) m.get("securityObjectType"))) {
					if(dvcIP.equals(""))
						dvcIP =key +"'"+(String) m.get("dataSourceIp")+"'";
					else
						dvcIP +=" or "+key+"'"+(String) m.get("dataSourceIp")+"'";		
				}
			}
		}
		return dvcIP;
	}
	private static List getUnion(List sqlParam, int num) {
		List reValue = new ArrayList();
		for (int j = 0; j < num; j++) {
			for (int i = 0; i < sqlParam.size(); i++)
				reValue.add(sqlParam.get(i));
		}
		return reValue;
	}
	
	/**
	 * 导出文件的封面及内容所需要的信息
	 * 
	 * @param SqlStruct
	 *            sqlStruct 子报表的sql where条件结构体
	 * @param String
	 *            ruleName 规则名称
	 * @param Map
	 *            ruleMap 子报表对应的规则、值表信息
	 * @param int
	 *            mstType 报表类别(系统/非系统)
	 * @param Object
	 *            tmpO 该规则具体数值
	 * @return void
	 */
	private static void setExpTime(SqlStruct sqlStruct, String ruleName, Map ruleMap,
			int mstType, Object tmpO) {
		SimpleDateFormat smpFmt1 = new SimpleDateFormat(ReportUiConfig.dFormat1);
		if (ruleName.equals("2"))// 设备IP
			sqlStruct.setDvcIp(StringUtil.toString(tmpO, "")); // 设备IP可能为null
		else if (ruleName.equals("3"))// 开始时间
			sqlStruct.setsTime(tmpO + "");// String
		else if (ruleName.equals("4"))// 结束时间
			sqlStruct.seteTime(tmpO + ""); // String
		else if (ruleName.equals("8")) {// addDate规则
			Object defValue = getDefValue(ruleMap, mstType);
			if (defValue.toString().equals("addNow"))
				sqlStruct.seteTime(tmpO instanceof Date ? smpFmt1.format(tmpO) : tmpO + "");
			else if(defValue.toString().equals("addMonth"))
				sqlStruct.setsTime(tmpO instanceof Date ? smpFmt1.format(tmpO) : tmpO + "");
			else
				sqlStruct.setsTime(tmpO instanceof Date ? smpFmt1.format(tmpO) : tmpO + "");
		}
	}
	
	/**
	 * 获取子报表默认数值
	 * 
	 * @param Map
	 *            map 子报表对应的规则、值表信息
	 * @param int
	 *            mstType 报表类别(系统/非系统)
	 * @param ExpStruct
	 *            exp 报表导出用结构体
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @return Object 系统默认值
	 */
	private static Object getExpValue(Map map, int mstType, ExpStruct exp, HttpServletRequest request) {
		Object reValue = getDefValue(map, mstType);
		if (exp != null && request == null) {
			if (reValue.toString().equals("addNow")){
				reValue = exp.getRptTimeE();
			}else if (reValue.toString().equals("addHour")) {
				reValue = ReportUiUtil.toStartTime("hour", exp.getRptTimeE());
			}else if (reValue.toString().equals("addMonth")) {
				reValue = ReportUiUtil.toStartTime("month", exp.getRptTimeE());
			}else{
				reValue = exp.getRptTimeS();
			}
			return reValue;
		}
		if (mstType == 2){// 自定义
			reValue = getTimeValue(reValue.toString());
		}// String
		else{
			reValue = ReportUiUtil.getAddDate(reValue.toString());
		}
		return reValue;
	}

	/**
	 * 根据不同页面设置叶面上的下拉框
	 * 
	 * @param ReportBean
	 *            creRpt StrutsFormBean
	 * @return void 前台显示用FormBean
	 */
	public static void setBeanPropery(ReportBean bean) {
		/*if (!bean.isRptList()){
			getMstList(bean);// mst list
		}*/
		getJi(bean);  // 季度
		getTop(bean); // top
		getSDatetype(bean);// 时间类型
	}
	/**
	 * 根据不同页面设置叶面上的日期类型下拉框
	 * 
	 * @param ReportBean
	 *            bean ReportBean
	 * @return void 前台显示用 ReportBean
	 */

	private static void getSDatetype(ReportBean bean) {
		String dateTypes = ReportUiConfig.DateTypeStr;
		int i = 0;
		String[] value = new String[dateTypes.split(",").length];
		String[] lable = new String[dateTypes.split(",").length];
		for (String dateType : dateTypes.split(",")) {
			lable[i] = ReportUiUtil.getProperty(dateType.trim());
			value[i] = (i + 1) + "";
			i++;
		}
		Map<String,String> dtypes = new HashMap<String,String>();
		for(int j=0,len=value.length;j<len;j++){
			dtypes.put(value[j], lable[j]);
		}
		bean.setDtypes(dtypes);
	}

	/**
	 * 根据type获取图形类型
	 * 
	 * @param int
	 *            subType DB.subType字段
	 * @return String type对应的Sql
	 */
	public static String getRunSql(int subType) {
		String reValue = null;
		switch (subType) {
		case 1:
			reValue = "chartSql";
			break;
		case 2:
			reValue = "tableSql";
			break;
		case 3:
		case 5:	
			reValue = "tableSql";
			break;
		}	
		return reValue;

	}
	
	/**
	 * 根据不同页面设置叶面上的Top 下拉框
	 * 
	 * @param ReportBean
	 *            bean ReportBean
	 * @return void 前台显示用 ReportBean
	 */
	private static void getTop(ReportBean bean) {
		String [] values= ReportUiConfig.Topvalues;
		String [] labels = ReportUiConfig.Toplabels;
		Map<String,String> tops = new LinkedHashMap<String,String>();
		for(int i=0,len=values.length;i<len;i++){
			tops.put(values[i], labels[i]);
		}
		bean.setTops(tops);
	}

	/**
	 * 根据不同页面设置叶面上的季度下拉框
	 * 
	 * @param ReportBean
	 *            bean ReportBean
	 * @return void 前台显示用 ReportBean
	 */
	private static void getJi(ReportBean bean) {
		String [] values = ReportUiConfig.Jivalues;
		String [] labels = ReportUiConfig.Jilabels;
		Map<String,String> quarters = new HashMap<String,String>();
		for(int i=0,len=values.length;i<len;i++){
			quarters.put(values[i], labels[i]);
		}
		bean.setQuarters(quarters);
	}

	/**
	 * 根据不同页面设置叶面上的页数选择下拉框
	 * 
	 * @param ReportBean
	 *            bean ReportBean
	 * @return void 前台显示用 ReportBean
	 */
	public static void getPageSize(ReportBean bean) {
		
		String[] values = ReportUiConfig.PageSizevalues;
		String[] labels = ReportUiConfig.PageSizelabels;
		Map<String,String> pagesizes = new HashMap<String,String>();
		for(int i=0,len=values.length;i<len;i++){
			pagesizes.put(values[i], labels[i]);
		}
		bean.setPagesizes(pagesizes);
		if (!ReportUiUtil.checkNull(bean.getPagesize()))
			bean.setPagesize("10");
	}
	
	/**
	 * 获取每行的列数
	 * @param _layout
	 * @return
	 */
	public static Map<Integer,Integer> getRowColumns(List<Map<String,Object>> mstSubjects){
		Map<Integer,Integer> rowColumns = new HashMap<Integer, Integer>() ;
		for(Map subject:mstSubjects){
			Integer row = (Integer) subject.get("subRow") ;
			Integer currentRowCount = rowColumns.get(row) ;
			if(currentRowCount == null){
				rowColumns.put(row,1) ;
			}else{
				rowColumns.put(row, ++currentRowCount) ;
			}
		}
		return rowColumns ;
	}
	public static String createMstTable(String layout,Map layoutValue) {
		Integer[] _rule = getLayoutRule(layout);
        int col = getMaxValue(_rule);
		StringBuffer sb = new StringBuffer();
		sb.append("<div class=\"rows\">");
		sb.append(ReportUiConfig.MstTable).append(ReportUiConfig.Rn);
		for (int i = 0; i < _rule.length; i++) {
			sb.append("<tr>").append(ReportUiConfig.Rn);
			for (int j = 0; j < _rule[i]; j++) {
				String rowColumn = "" +(i + 1)+ (j + 1);
				if(_rule[i]<col){
					sb.append("<td style='padding-bottom:5px;' id='subReport_"+ rowColumn + "' colspan='"+col+" '" + ">").append(ReportUiConfig.Rn).append(layoutValue.get(rowColumn)).append("</td>").append(ReportUiConfig.Rn);
				}else{
					sb.append("<td style='padding-bottom:5px;' id='subReport_"+ rowColumn + "'" + ">").append(ReportUiConfig.Rn).append(layoutValue.get(rowColumn)).append("</td>").append(ReportUiConfig.Rn);
				}
				}
			sb.append("</tr>").append(ReportUiConfig.Rn);
		}
		sb.append("</table>").append(ReportUiConfig.Rn);
		sb.append("</div>").append(ReportUiConfig.Rn);
		return sb.toString();
	}
	
	/**
	 * 获取布局规则
	 * 
	 * @param String
	 *            _rule 布局规则
	 * 
	 * @return Integer[] 布局规则数组
	 */
	private static Integer[] getLayoutRule(String _rule) {
		Integer[] reValue = null;
		Map<String, Integer> map = ReportUiUtil.orderMap(getRowColumns(_rule));
		reValue = new Integer[map.size()];
		Iterator i = map.keySet().iterator();
		Object strKey = null;
		int n = 0;
		while (i.hasNext()) {
			strKey = i.next();
			reValue[n] = map.get(strKey);
			n++;
		}
		return reValue;
	}
	
	/**
	 * 获取每行的列数
	 * @param _layout
	 * @return
	 */
	public static LinkedHashMap<String, Integer> getRowColumns(String _layout){
		String[] layouts = _layout.split(",");
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		for (String layout : layouts) {
			String rows[] = layout.split(":");
			Integer tmpValue = map.get(rows[0]);
			if (tmpValue != null) {
				tmpValue++;
				map.put(rows[0], tmpValue);
			} else
				map.put(rows[0], 1);
		}
		return ReportUiUtil.orderMap(map);
	}
	
	public static int getMaxValue(Integer[] value){
		int val = value[0];
		for(int i=1,len=value.length;i<len;i++){
			if(value[i]>val)val=value[i];
		}
		return val;
	}
	
	/**
	 * 根据不同报表类型返回Sql执行结果集
	 * 
	 * @param Map
	 *            subMap 子报表对应的规则、值表信息
	 * @param int
	 *            mstType 报表类别(系统/非系统)
	 * @param RptMasterTbService
	 *            rptMasterTbImp DAO
	 * @return List 返回Sql执行结果集
	 */
	public static List getRuleRs(int mstType, Map subMap,RptMasterTbService rptMasterTbImp) {
		List ruleResult = null;
		// 系统策略表信息
		Integer subPolicyId = (Integer) subMap.get("subPolicyId");
		if (mstType != 2) {
			String ruleSql = ReportUiConfig.RuleSql;
			Object[] ruleParam = { subPolicyId };
			ruleResult = rptMasterTbImp.queryTmpList(ruleSql, ruleParam);
		} else {
			// 自定义策略表信息
			Integer mstSubId = (Integer) subMap.get("id");
			String ruleValueSql = ReportUiConfig.RuleValueSql;
			Object[] ruleValueParam = { subPolicyId, mstSubId };
			ruleResult = rptMasterTbImp.queryTmpList(ruleValueSql,
					ruleValueParam);
		}
		return ruleResult;
	}
	
	/**
	 * 根据时间区间替换sql对应的日、月、年表
	 * 
	 * @param String
	 *            sql 原始Sql
	 * @param String
	 *            sTime 开始时间
	 * @param String
	 *            eTime 结束时间
	 * @return String 替换后的Sql
	 * @throws Exception
	 */
	public static String getTimeSql(String sql, String sTime, String eTime) {
		String reValue = sql;
		if (sql.indexOf("_month") > 0
				||(sql.indexOf("from") > 0 
						&& sql.indexOf("Month") > 0 
						&& sql.indexOf("from")<sql.indexOf("Month")))
			return sql;
		if (sql.indexOf("_day") > 0||(sql.indexOf("from") > 0 
				&& sql.indexOf("Day") > 0 
				&& sql.indexOf("from")<sql.indexOf("Day")))
			return sql;
		Long time = null;
		if (ReportUiUtil.checkNull(sTime) && ReportUiUtil.checkNull(eTime))
			time = ReportUiUtil.countTime(sTime, eTime);
		else
			return sql;
		if (time >= (86400 * 28)) {
			reValue = sql.replace("hour", "month");
			reValue = reValue.replace("Hour", "Month");
			reValue = reValue.replace("HOUR", "MONTH");
		} else if (time > 86400*2) {
			reValue = sql.replace("hour", "day");
			reValue = reValue.replace("Hour", "Day");
			reValue = reValue.replace("HOUR", "DAY");
		}
		return reValue;
	}
	/**
	 * 获取子主题数据
	 * @param subMap 查询参数
	 * @param sTime 开始时间
	 * @param eTime 结束时间
	 * @param subId 子报表ID
	 * @param isAllResult 是否查询所有
	 * @param request
	 * @return
	 */
	public static Map<String,Object> getSubTitleData(RptMasterTbService rptMasterTbImp,List<String>deviceTypes,List<String>deviceIps,Map subMap,String sTime,String eTime,String subId,boolean isAllResult,HttpServletRequest request){
		
		String mstType = subMap.get("mstType").toString();
		int _mstType = StringUtil.toInt(mstType,1);
		List ruleResult =getRuleRs(_mstType, subMap, rptMasterTbImp);
		boolean isCoreNode = ReportUiUtil.isCoreNodeReport(subMap);
		String type=request.getParameter(ReportUiConfig.dvctype);
		boolean isevtReport="Profession/Group".equals(type)||"Profession/Group/Asset".equals(type);
		if (isCoreNode) {
			for (int j = 0; j < ruleResult.size(); j++) {
				Map map = (Map) ruleResult.get(j);
				String talCategoryKey = (String) map.get("sqlParam");
				if ("and dvcAddress = ?".equals(talCategoryKey) || "and alias.dvcAddress = ?".equals(talCategoryKey) || "and fwrisk.dvcAddress = ?".equals(talCategoryKey)) {
					ruleResult.remove(j);
					break;
				}
			}
		}
		SqlStruct struct = getSqlStruct(_mstType, ruleResult, request,null, subMap, sTime, eTime);
		int subType = StringUtil.toInt(subMap.get("subType").toString());
		String runSql =getRunSql(subType);
		String sql = subMap.get(runSql).toString();
		// 根据时间区间换表查询
		sql = getTimeSql(sql, sTime, eTime);
		String[] nodeIds=request.getParameterValues("nodeId"); 
		int paramNo=0;
		if (!isevtReport) {
			// String sqlQuShi = sql;
			sql += struct.getSql();
			if (sql.indexOf("union") != -1) {
				if (struct != null) {
					String onlyByDvctype = struct.getDvcIp();
					if ("onlyByDvctype".equals(onlyByDvctype)) {
						sql = sql.replace("DVC_ADDRESS=?"," ("+ getDvcIp(struct.getDevTypeName(),"DVC_ADDRESS =") + ")");
					}
				}
			}
		}else {
			if (sql.indexOf("union") != -1) {
				if (struct != null) {
					String sqlunion = struct.getSql();
					sql = sql.replaceAll("union",sqlunion+ " union");
				}
			}
			if (sql.indexOf("DVC_ADDRESSInAndTimeConditionGroup") != -1
					||sql.indexOf("ipInAndTimeConditionGroup") != -1) {
				paramNo+=stringNumbers(sql,"DVC_ADDRESSInAndTimeConditionGroup");
				paramNo+=stringNumbers(sql,"ipInAndTimeConditionGroup");
				if (struct != null) {
					String sqlpar = struct.getSql();
					sql = sql.replaceAll(
							"DVC_ADDRESSInAndTimeConditionGroup",sqlpar);
					sqlpar=sqlpar.replaceAll("DVC_ADDRESS", "IP");
					sql = sql.replaceAll(
							"ipInAndTimeConditionGroup",sqlpar);
					if (sqlpar.contains("group")) {
						sql+=" "+sqlpar.substring(sqlpar.indexOf("group"));
					}
				}
			}else if (sql.indexOf("DVC_ADDRESSEqAndTimeCondition") != -1
					||sql.indexOf("ipEqAndTimeCondition") != -1) {
				paramNo+=stringNumbers(sql,"DVC_ADDRESSEqAndTimeCondition");
				paramNo+=stringNumbers(sql,"ipEqAndTimeCondition");
				if (struct != null) {
					String sqlpar = struct.getSql();
					sql = sql.replaceAll(
							"DVC_ADDRESSEqAndTimeCondition",sqlpar);
					sqlpar=sqlpar.replaceAll("DVC_ADDRESS", "IP");
					sql = sql.replaceAll(
							"ipEqAndTimeCondition",sqlpar);
				}
			}else {
				sql += struct.getSql();
			}
			
			if (sql.indexOf("union") != -1 && sql.indexOf("LOG_FILE") != -1){
				String[] sqls=sql.split("union");
				sql="";
				for (int i=0;i<sqls.length;i++) {
					if (sqls[i].contains("LOG_FILE")) {
						sqls[i]=sqls[i].replaceAll("DVC_ADDRESS", "IP");
					}
					sql+=sqls[i]+" union ";
				}
				sql=sql.substring(0, sql.length()-7);
			}
		}
		int unionCount=stringNumbers(sql,"union");
		List sqlparam=new ArrayList();
		sqlparam.addAll(struct.getSqlparam());
		if (isevtReport) {
			if (unionCount > 0) {
				for (int i = 0; i < unionCount; i++) {
					sqlparam.addAll(struct.getSqlparam());
				}
			}
			if (paramNo>0) {
				sqlparam.clear();
				for (int i = 0; i < paramNo; i++) {
					sqlparam.addAll(struct.getSqlparam());
				}
			}
		}
		Object more=request.getAttribute("moredat");
		if ("999".equals(more)) {
			struct.setSqlpage(30);
		}
		Map<String,Object> rsMap = reformingSubTitleData(deviceTypes,deviceIps,sql, sqlparam, subMap, struct.getSqlpage(), nodeIds, sTime, eTime,isAllResult, request);
		return rsMap;
	}
	public static Integer stringNumbers(String parentStr,String childsString){
		int counter=0;
		String parentString=parentStr;
		if (parentString.indexOf(childsString)==-1){
			return counter;
		}
		while(parentString.indexOf(childsString) != -1){
			counter++;
			if (parentString.indexOf(childsString)==-1) {
				break;
			}
			parentString=parentString.substring(parentString.indexOf(childsString)+childsString.length());
		}
		return counter;
	}
	private static void noIpTypeAndNumberNullFormat(List<String>deviceTypes,List<String>deviceIps,List<Map> result){
		try {
			if (GlobalUtil.isNullOrEmpty(result)) {
				return;
			}
			if (!GlobalUtil.isNullOrEmpty(SID.currentUser())
					&&!SID.currentUser().isOperator()
					&&!SID.currentUser().hasAuditorRole()) {
				boolean isdType = false;
				boolean isdIp = false;
				boolean isDvcAdd=false;
				isdType = result.get(0).containsKey("ALLLOGTYPE");
				isdIp = result.get(0).containsKey("ALLLOGIP");
				isDvcAdd = result.get(0).containsKey("DVC_ADDRESS") && deviceTypes.size()==1;
				boolean contains = false;
				if (!GlobalUtil.isNullOrEmpty(deviceTypes) && isdType) {
					List<Map> removeMaps = new ArrayList<Map>();
					for (Map map : result) {
						contains = false;
						for (String string : deviceTypes) {
							if (string.equals(map.get("ALLLOGTYPE"))) {
								contains = true;
							}
						}
						if (!contains && !removeMaps.contains(map)) {
							removeMaps.add(map);
						}
					}

					result.removeAll(removeMaps);
				}
				if (!GlobalUtil.isNullOrEmpty(deviceIps) && (isdIp||isDvcAdd)) {
					List<Map> removeMaps = new ArrayList<Map>();
					for (Map map : result) {
						contains = false;
						for (String string : deviceIps) {
							if (!GlobalUtil.isNullOrEmpty(map)
									&& (!GlobalUtil.isNullOrEmpty(map.get("ALLLOGIP"))
											|| !GlobalUtil.isNullOrEmpty(map.get("DVC_ADDRESS")))) {
								Object ip = map.get("ALLLOGIP");
								Object dvcAdd = map.get("DVC_ADDRESS");
								if (string.equals(ip) || string.equals(dvcAdd)) {
									contains = true;
								}
							}
						}
						if (!contains && !removeMaps.contains(map)) {
							removeMaps.add(map);
						}
					}

					result.removeAll(removeMaps);
				}
			}
			for (Map map : result){
				nullNumberMapFormat(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void evtReportFormat(List<Map> result,HttpServletRequest request){
		try {
			if (GlobalUtil.isNullOrEmpty(result)) {
				return;
			}
			Long evtCount=0l,logCount=0l;
			boolean iscreateCol=false;
			int topoAssNo=0;
			try {
				topoAssNo=getparamIpList(request).size();
			} catch (Exception e) {
			}
			for (Map map : result) {
				nullNumberMapFormat(map);
				if ("事件数(总)".equals(map.get("SUMMARY"))) {
					iscreateCol=true;
					evtCount=Long.valueOf(map.get("TOTAL").toString());
				}
				if ("日志数(总)".equals(map.get("SUMMARY"))) {
					iscreateCol=true;
					logCount=Long.valueOf(map.get("TOTAL").toString());
				}
			}
			if (iscreateCol) {
				Map map=new HashMap();
				map.put("SUMMARY", "设备数");
				map.put("TOTAL", topoAssNo);
				result.add(0, map);
				
				map=new HashMap();
				map.put("SUMMARY", "每台设备平均事件数");
				map.put("TOTAL", topoAssNo==0?evtCount:evtCount/topoAssNo);
				result.add(2, map);
				
				map=new HashMap();
				map.put("SUMMARY", "每台设备平均日志数");
				map.put("TOTAL", topoAssNo==0?logCount:logCount/topoAssNo);
				result.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void priorityShowCnFormat(List<Map> result){
		try {
			if (GlobalUtil.isNullOrEmpty(result)) {
				return;
			}
			//[{evtCount=5622, PRIORITY=0}]
			for (Map map : result) {
				nullNumberMapFormat(map);
				if (map.containsKey("PRIORITY")) {
					if (0==Integer.valueOf(map.get("PRIORITY").toString())) {
						map.put("PRIORITY", "无危险");
					}else if (1==Integer.valueOf(map.get("PRIORITY").toString())) {
						map.put("PRIORITY", "低危险");
					}else if (2==Integer.valueOf(map.get("PRIORITY").toString())) {
						map.put("PRIORITY", "一般危险");
					}else if (3==Integer.valueOf(map.get("PRIORITY").toString())) {
						map.put("PRIORITY", "高危险");
					}else if (4==Integer.valueOf(map.get("PRIORITY").toString())) {
						map.put("PRIORITY", "非常危险");
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void nullNumberMapFormat(Map map){
		String[] keys={"BYTES","BYTE","OPCOUNT","TOTAL","COUNTS","opCount","opCount1","opCount2","opCount3","opCount4","BYTES_IN","BYTES_OUT"};
		for (int i = 0; i < keys.length; i++) {
			if (map.containsKey(keys[i])){
				if (GlobalUtil.isNullOrEmpty(map.get(keys[i]))) {
					map.put(keys[i], 0);
				}
			}
		}
	}
	private static void notypeShowFormat(List<Map> result){
		try {
			if (GlobalUtil.isNullOrEmpty(result)) {
				return;
			}
			for (Map map : result) {
				notypeMapFormat(map);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void notypeMapFormat(Map map){
		String[]dvcTypes={"ALLLOGTYPE","TYPE","UNFORMAT_DATA_ALLLOGTYPE","UNFORMAT_DATA_TYPE"};
		Map<String, Object>map2=(Map<String, Object>)map;
		for (Map.Entry<String, Object> entry : map2.entrySet()) {
			String keyString=entry.getKey();
			Object valueObject=entry.getValue();
			boolean isTypeAndNonull=false;
			for (String string : dvcTypes) {
				if (string.equalsIgnoreCase(keyString) || GlobalUtil.isNullOrEmpty(valueObject)) {
					isTypeAndNonull=true;
					break;
				}
			}
			if (!isTypeAndNonull) {
				String tempStr=valueObject+"";
				if (tempStr.contains("/")) {
					tempStr=tempStr.replaceAll("/", "／");
				}
				map.put(keyString, tempStr);
			}
		}
		
	}
	
	public static Map<String,Object> reformingSubTitleData(List<String>deviceTypes,List<String>deviceIps,String hql,List params,Map<String,Object> subMap,Integer iPage,String [] nodeIds,String sTime,String eTime,boolean isAllResult,HttpServletRequest request){
		Map<String,Object>  rsMap = null;
		try{
			String type=null;
			try {
				type=request.getParameter(ReportUiConfig.dvctype);
			} catch (Exception e) {
			}
			boolean isevtReport="Profession/Group".equals(type)||"Profession/Group/Asset".equals(type);
			String tableName = null;
			if (!isevtReport) {
				tableName=ReportUiUtil.getTable(hql);
			}else {
				try {
					tableName=ReportUiUtil.getTable(hql);
				} catch (Exception e) {
					tableName="SIM_EVENT_HOUR";
				}
			}
			List resultValue = getList(hql, params, iPage,tableName,nodeIds,request);
			Object sumPageObject=resultValue.get(0);
			int sumPage=5;
			if(sumPageObject!=null){
				sumPage = (Integer) sumPageObject;
			}
			
			// DB返回的 详细数据
			List result = (List) resultValue.get(1);
			// 总记录数 是否显示更多
			List<Map> resultMaps=(List<Map>)result.get(0);
			if (!isevtReport) {
				noIpTypeAndNumberNullFormat(deviceTypes, deviceIps, resultMaps);
			}
			boolean qushiFlag = subMap.get("chartProperty") != null&& subMap.get("chartProperty").toString().equals("1");// 趋势报表
			
			String chartItem = subMap.get("chartItem") == null ? "" : subMap.get("chartItem").toString();
			String[] chartItems=chartItem.split(",");
			String categorys = (String)subMap.get("category");
			/*String category = (String)subMap.get("category");*/
			String category = null;
			if(categorys.indexOf("1")==-1){
				category = categorys;
			}else{
				category = categorys.split("|")[0];
			}
			int subType = StringUtil.toInt(subMap.get("subType").toString());
			
			if (qushiFlag)// 针对趋势报表重构数据
			{
				String mstType = subMap.get("mstType").toString();
				result = changeResultPro(result, sTime, eTime, tableName,category, chartItem, mstType);
				rsMap = new HashMap<String,Object>();
				rsMap.put("result",result);
				rsMap.put("sumPage", result.size());// 趋势报表增加的数据
			}else{
				if (hql.indexOf("union") != -1) {
					rsMap= reformingStatisticDataForUnion(result, category, chartItems);
				}else if(subType==5){
					String tmpCate = categorys.split("&")[1];
					String catestr = categorys.split("&")[0];
					rsMap = reformingStatisticDataForSimpleMultidata(result, catestr.split(","), chartItems, tmpCate.split(","),sumPage,isAllResult);
				}else {
					rsMap= reformingStatisticDataForTop(result,category,chartItems,sumPage,isAllResult);
				}
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return rsMap;
	}
	
	
	/**
	* @method: reformingStatisticDataForTop 
	* 		 重新构造排行报表数据
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  result:从各个Auditor得到的结果result<List>
	*  		   category: 种类key
	*   	   chartItems: 统计项集合
	*    	   sumPage: 每页显示几条
	*    	   isAllResult: true为返回所有值,false为返回topN
	* @return: Map<String,Object>: 用map返回结果
	* @exception: Exception
	*/
	public static Map<String,Object> reformingStatisticDataForTop(List result,String category,String[] chartItems,int sumPage,Boolean isAllResult){
		
		//排行报表和统计报表yang_xuanjia
		Map<String,Object> rmap=new HashMap<String, Object>();
		
		int chartItemsLen=chartItems.length;
		
		List topResultTemp=new ArrayList(); 
		List topResult=new ArrayList(); 
		int mFlag=0;
		if(result!=null&&result.size()>0){
			for (Object oList : result) {
				List mapList=(List)oList;
				if(mapList!=null&&mapList.size()>0){
					mFlag++;
					for (Object oMap : mapList) {
						topResultTemp.add((Map)oMap);
					}
				}
			}
		}
		
		int len=topResultTemp.size();
		if(mFlag>=2&&len>=2){
			//mFlag: 在多个Auditor内有数据,如果只有1个Auditor中有数据,就不用排序了,因为sql语句就已经排序了.
			//len:大于等于2条记录才排序
			
			//排序前先把多个Auditor内key相同的值求和.
			List topSumResultList=new ArrayList(); 
			Map<String,Object> topSumResulMap=new HashMap<String, Object>();
			for (int i = 0; i < len; i++) {
				Map<String,Object> m=(Map<String,Object>)topResultTemp.get(i);
				String categoryTemp=m.get(category).toString();
				
				if(topSumResulMap.get(categoryTemp)==null){
					topSumResulMap.put(categoryTemp, m);
				}else{
					Map<String,Object> mOld =(Map<String,Object>)topSumResulMap.get(categoryTemp);
					for (int j = 0; j < chartItemsLen; j++) {
						Object old=mOld.get(chartItems[j]);
						Object o=m.get(chartItems[j]);
						old=ReportUiUtil.sumValue(old, o);
						mOld.put(chartItems[j], old);
					}
				}
			}
			
			Set<String> keys=topSumResulMap.keySet();
			if(keys!=null&&keys.size()>0){
				 for (String key : keys) {
					 topSumResultList.add(topSumResulMap.get(key));
				}
			}
			
			topResultTemp=topSumResultList;
			
			len=topResultTemp.size();
			ReportDataComparable comparable=new ReportDataComparable(chartItems[0]); 
			for (int i = 0; i < len-1; i++) {
				for (int j = i+1; j < len; j++) {
					if(comparable.compareTo(topResultTemp.get(i), topResultTemp.get(j))<0){
						Object temp=topResultTemp.get(i);
						topResultTemp.remove(i);
						topResultTemp.add(i, topResultTemp.get(j-1));
						topResultTemp.remove(j);
						topResultTemp.add(j,temp);
					}
				}
			}
		}
		
		
		
		int lenTemp=topResultTemp.size();
		if(isAllResult){
			result=topResultTemp;
		}else{
			//lenResult为要显示的记录数
			int lenResult=0;
			if(sumPage>=lenTemp){
				lenResult=lenTemp; 
			}else{
				lenResult=sumPage;
			}
			for (int i = 0; i < lenResult; i++) {
				topResult.add(topResultTemp.get(i));
			}
			result=topResult;
		}
		
		sumPage=lenTemp;
		//sumPage为总记录数
		rmap.put("result", result);
		rmap.put("sumPage", sumPage);
		
		return rmap;
	}
	
	/**
	* @method: reformingStatisticDataForTrend 
	* 		 重新构造趋势报表数据
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  qushiListTemp: 列表集合list<List>
	*  		   qushiList: 需要返回的结果
	*  		   chartItem: 统计项  aa,bb
	* @return: void
	* @exception: Exception
	*/
	public static void reformingStatisticDataForTrend(List qushiListTemp,List qushiList,String category,String chartItem){ 
		
		int size=qushiListTemp.size();
		if(size>0){
			if(size<2){
				List qushiListTemp2=(List)qushiListTemp.get(0);
				if(qushiListTemp2!=null&&qushiListTemp2.size()>0){
					qushiList.addAll(qushiListTemp2);
				}
			}else{
				String[] chartItems=chartItem.split(",");
				int lenChartItems=chartItems.length;
				List<Map<String, Object>> mapListTemp=(List<Map<String, Object>>)qushiListTemp.get(0);
				int len=mapListTemp.size();
				if(len>0){
					for (int i = 0; i < len; i++) {
						
						Map<String,Object> mapTemp=new HashMap<String, Object>();
						for (int j = 0; j < size; j++) {
							List<Map<String, Object>> mapList=(List<Map<String, Object>>)qushiListTemp.get(j);
							 
							Map<String, Object> map=mapList.get(i);

							if(map.get("PRIORITY")==null){
								Object startTime=mapTemp.get(category);
								if(startTime==null){
									Object startTimeObj=map.get(category);
									mapTemp.put(category, startTimeObj);
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										mapTemp.put(chartItems[k], value);
									}
								}else{
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										Object sumValue=mapTemp.get(chartItems[k]);
										sumValue=ReportUiUtil.sumValue(sumValue, value);
										mapTemp.put(chartItems[k], sumValue);
									}
								}
							}else{
								String prioriry=(String)map.get("PRIORITY");
								
								String key=(String)mapTemp.get(category+prioriry);
								if(key==null){
									mapTemp.put(category+prioriry, category+prioriry);
									Object startTimeObj=map.get(category);
									Object priority=map.get("PRIORITY");
									mapTemp.put(category, startTimeObj);
									mapTemp.put("PRIORITY", priority);
									
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										mapTemp.put(chartItems[k], value);
									}
								}else{
									for (int k = 0; k < lenChartItems; k++) {
										Object value=map.get(chartItems[k]);
										Object sumValue=mapTemp.get(chartItems[k]);
										sumValue=ReportUiUtil.sumValue(sumValue, value);
										mapTemp.put(chartItems[k], sumValue);
									}
								}
							}
						}
						qushiList.add(mapTemp);
					}
				}
			}
		}
	}
	
	/**
	 * 多列合并数据，非归并字段暂时只支持String 类型的
	 * @param result 
	 * @param category 非合并字段
	 * @param chartItems 合并字段
	 * @param compositorFields 排序字段  "id|1,opCount|-1"
	 * @return
	 */
	public static Map<String,Object> reformingStatisticDataForSimpleMultidata(List result,String[] category,String[] chartItems,String[] compositorFields,int sumPage,boolean isAllResult){
		
		List tmpList = new ArrayList();
	    int nodes = 0;
	     if(result!=null&&result.size()>0){
			for (Object oList : result) {
				if(oList!=null){
					nodes++;
					tmpList.addAll((Collection) oList);
				}
			}
		}
		if(nodes>=2&&tmpList.size()>1){
			for(int i=0,len=tmpList.size();i<len;i++){
				HashMap iMap =(HashMap) tmpList.get(i);
				for(int j=i+1;j<len;j++){
					boolean jflag = true;
					HashMap jMap = (HashMap) tmpList.get(j);
					for(int ic=0,clen=category.length;ic<clen;ic++){
						jflag =jflag&&iMap.get(category[ic]).equals(jMap.get(category[ic])); 
						if(ic==clen-1){
							if(jflag){
								Map tMap = iMap;
								for(int y=0,ylen=chartItems.length;y<ylen;y++){
									Object iobj=iMap.get(chartItems[y]);
									Object jobj=jMap.get(chartItems[y]);
									Object tobj=ReportUiUtil.sumValue(iobj, jobj);
									tMap.remove(chartItems[y]);
									tMap.put(chartItems[y], tobj);
								}
								tmpList.remove(j);
								tmpList.remove(i);
								tmpList.add(i, tMap);
								len = tmpList.size();
							}
						}
					}
				}
			}
			
			if(compositorFields!=null&&compositorFields.length>0){
				for(int k=0,klen= compositorFields.length;k<klen;k++){
					String order[] = compositorFields[k].split("|");
					String field =order[0];
					ReportDataComparable comparable=new ReportDataComparable(field); 
					int ord = Integer.parseInt(order[1]!=null?order[1]:"0");
					for(int i=0,len=tmpList.size();i<len;i++){
						for(int j=i+1;j<len;j++){
							int myOrd = comparable.compareTo(tmpList.get(i),tmpList.get(j));
	                        if(ord<0){
	                        	if(myOrd<0){
	                        		Object temp=tmpList.get(i);
	                        		tmpList.remove(i);
	                        		tmpList.add(i, tmpList.get(j-1));
	                        		tmpList.remove(j);
	                        		tmpList.add(j,temp);
	                        	}
	                        }else if(ord>0){
	                        	if(myOrd>0){
	                        		Object temp=tmpList.get(i);
	                        		tmpList.remove(i);
	                        		tmpList.add(i, tmpList.get(j-1));
	                        		tmpList.remove(j);
	                        		tmpList.add(j,temp);
	                        	}
	                        }
							}
						}
					}
				}
			}
		List topResult=new ArrayList(); 
		int lenTemp=tmpList.size();
		if(isAllResult){
			result=tmpList;
		}else{
			//lenResult为要显示的记录数
			int lenResult=0;
			if(sumPage>=lenTemp){
				lenResult=lenTemp; 
			}else{
				lenResult=sumPage;
			}
			for (int i = 0; i < lenResult; i++) {
				topResult.add(tmpList.get(i));
			}
			result=topResult;
		}
		sumPage=lenTemp;
		
		
		Map<String,Object> rmap=new HashMap<String, Object>();
			rmap.put("result", result);
			rmap.put("sumPage", sumPage);
			return rmap;
	}
	
	/**
	* @method: reformingStatisticDataForUnion 
	* 		 重新构造概要(Union)报表数据
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  result:从各个Auditor得到的结果result<List>
	*  		   category: 种类key
	*   	   chartItems: 统计项集合
	* @return: Map<String,Object>: 用map返回结果
	* @exception: Exception
	*/
	public static Map<String,Object> reformingStatisticDataForUnion(List result,String category,String[] chartItems){
		
		//概要yang_xuanjia
		Map<String,Object> rmap=new HashMap<String, Object>();
		int chartItemsLen=chartItems.length;
		List resultHaveData=new ArrayList();
		
		if(result!=null){
			
			for (Object object : result) {
				List tempList=(List)object;
				if(tempList!=null&&tempList.size()>0){
					resultHaveData.add(tempList);
				}
			}
			result=resultHaveData;
			
			int resultArrayLen=result.size();
			List<String> keyList = new ArrayList<String>();
			if(resultArrayLen==1){
				result=(List)result.get(0);
			}else if(resultArrayLen>=2){
				 int column=((List)result.get(0)).size();
				 List resultList=new ArrayList();
				 Map topSumResulMap=new HashMap();
				 for (int j = 0; j < column; j++) {
					 for (int i = 0; i < resultArrayLen; i++) {
							Map<String,Object> m=(Map)((List)result.get(i)).get(j);
							String categoryTemp=(String)m.get(category);
							
							if(topSumResulMap.get(categoryTemp)==null){
								topSumResulMap.put(categoryTemp, m);
								keyList.add(categoryTemp);
							}else{
								Map<String,Object> mOld =(Map<String,Object>)topSumResulMap.get(categoryTemp);
								for (int k = 0; k < chartItemsLen; k++) {
									Object old=mOld.get(chartItems[k]);
									Object o=m.get(chartItems[k]);
									old=ReportUiUtil.sumValue(old, o);
									mOld.put(chartItems[k], old);
								}
							}
					 }
				 }
				 
				 Set<String> keys=topSumResulMap.keySet();
					 for (String key : keyList) {
						 resultList.add(topSumResulMap.get(key));
					}
					result=resultList;
			}
		}else{
			result=resultHaveData;
		}
		
		//sumPage为总记录数
		rmap.put("result", result);
		rmap.put("sumPage", result.size());//9999
		
		return rmap;
	}
	
	/**
	 * 针对趋势报表重构数据
	 * 
	 * @param List
	 *            result DB中原始数据
	 * @param String
	 *            sql 获取重构类型[hour day month]
	 * @return List 重构后的数据
	 * 
	 */
	public static List<Map<String, Object>> changeResultPro(List<Map<String, Object>> result, String sTime, String eTime,
			String table, String category, String chartItem, String mstType) {
		if (result.size()==0){
			return result;
		}
		
		String[] DateV=new String[]{sTime,eTime};
		List qushiList=new ArrayList();
		List qushiListTemp=new ArrayList();

		if (DateV != null && DateV[0]!="" && DateV[1]!=""&& ReportUiUtil.countTime(DateV[0], eTime, "qushi")) {
			
			// 输入的结束时间大于等于DB的开始时间
			if (!ReportUiUtil.countTime(DateV[0], sTime, "qushi")){
				sTime = DateV[0];
			}
			
			if(result!=null&&result.size()>0){
				
				Map<String,String> priorityKeys=new HashMap<String,String>();
				for (Object oList : result) {
					List<Map<String, Object>> mapList=(List<Map<String, Object>>)oList;
					if(mapList!=null&&mapList.size()>0){
						if (mapList.get(0).get("PRIORITY")!=null){
							for (Map<String, Object> map : mapList) {
								String priority=map.get("PRIORITY")+"";
								if(priorityKeys.get(priority)==null){
									priorityKeys.put(priority, priority);
								} 
							}
					    } 
					}
				}
				
				
				for (Object oList : result) {
					List<Map<String, Object>> mapList=(List<Map<String, Object>>)oList;
					
					if(mapList!=null&&mapList.size()>0){
							if (mapList.get(0).get("PRIORITY")!=null){
								Map<String,List<Map<String, Object>>> maps=new HashMap<String,List<Map<String, Object>>>();
								for (Map<String, Object> map : mapList) {
									String priority=map.get("PRIORITY")+"";
									if(maps.get(priority)==null){
										List<Map<String, Object>> innerMapList=new ArrayList<Map<String,Object>>();
										innerMapList.add(map);
										maps.put(priority, innerMapList);
									}else{
										List<Map<String, Object>> innerMapList = maps.get(priority);
										innerMapList.add(map);
									}
								}
								
								mapList.clear();
								Set<String> keySet = priorityKeys.keySet();
								for (String key : keySet) {
									List<Map<String, Object>> innerMapList = maps.get(key);
									if(innerMapList==null){
										innerMapList=new ArrayList<Map<String,Object>>();
									}
									innerMapList = changeResult(innerMapList, table, sTime, eTime,category, chartItem, mstType, DateV[1]);
									mapList.addAll(innerMapList);
								}
							}else{	
								//时间趋势
								mapList = changeResult(mapList, table, sTime, eTime,category, chartItem, mstType, DateV[1]);			
							}
							qushiListTemp.add(mapList);
					}
				}
				
				reformingStatisticDataForTrend(qushiListTemp,qushiList,category,chartItem);
			}
		}
		return qushiList;
	}
	
	/**
	 * 针对趋势报表重构数据
	 * 
	 * @param List
	 *            result DB中原始数据
	 * @param String
	 *            sql 获取重构类型[hour day month]
	 * @return List 重构后的数据
	 * 
	 */
	public static List<Map<String, Object>> changeResult(
			List<Map<String, Object>> result, String table, String sTime,
			String eTime, String category, String chartItem, String mstType, String maxTime) {
		List<Map<String, Object>> reValue = new ArrayList<Map<String, Object>>();
		// 计算开始时间 结束时间Start
		String ruleDate = null;
		int sC = 0;
		int eC = 0;
		String tmpEc = "";

		if (ReportUiUtil.getSEcount(maxTime, eTime) > 0){
			eTime = maxTime;
		}
		if (sTime.endsWith(".0")){
			sTime = sTime.substring(0, sTime.length() - 2);
		}
		if (eTime.endsWith(".0")){
			eTime = eTime.substring(0, eTime.length() - 2);
		}
		if (table.toLowerCase().indexOf("hour") >= 0) {
			ruleDate = "hour";
			if (!sTime.endsWith("0:00")){
				sTime = ReportUiUtil.getAddDate(countTime(sTime,ruleDate), ruleDate,table);
			}
			eTime = countTime(eTime,ruleDate);
		} else if (table.toLowerCase().indexOf("day") >= 0) {
			ruleDate = "day";
			if (!(sTime.endsWith("00:00")||sTime.endsWith("06:00")||sTime.endsWith("12:00")
					||sTime.endsWith("18:00"))){
				sTime = ReportUiUtil.getAddDate(countTime(sTime,ruleDate), ruleDate,table);
			}
			eTime = countTime(eTime,ruleDate);

		} else if (table.toLowerCase().indexOf("month") >= 0) {
			ruleDate = "month";
			if (!sTime.endsWith("00:00:00")){
				sTime = ReportUiUtil.getAddDate(sTime.substring(0, 11)
						+ "00:00:00", ruleDate,table);
			}
			eTime = eTime.substring(0, 11) + "00:00:00";
		} 
		
		// 计算开始时间 结束时间End
		// 设定开始时间！
		String tmpTime = sTime;
		int idx = 0; // 计数
		while (ReportUiUtil.countTime(tmpTime, eTime, "qs")) {
			if (idx < result.size()) {
				/*start
				此 if 判断 可以替代 if (!ReportUiUtil.countTime(tmpTime, result.get(idx).get(category).toString(), ruleDate))
				下面的 判断可能 在sql语句排序正确的情况下产生正确的结果，但是sql未排序 或者不是按升序情况下排序就会出问题，下面的if 效率会更高一些
				此if 不管sql是否排序都会正确执行结果，缺点是 效率会低一些，暂时用下面的 if语句,以后如果 bug太多，则会把此if更换下面的if
				*/
				//if (!isListContainsMapValue(result,category,tmpTime)) 
				/*end*/
				if (!ReportUiUtil.countTime(tmpTime, result.get(idx).get(
						category).toString(), ruleDate)) {//两个时间值不等
					Map<String, Object> tmp = getTimeMap(tmpTime, chartItem);
					if (result.size()>0 && result.get(idx).get("PRIORITY") != null){
						tmp.put("PRIORITY", result.get(idx).get("PRIORITY"));
					}
					reValue.add(tmp);
					tmpTime = ReportUiUtil.getAddDate(tmpTime, ruleDate,table);
				} else {
					tmpTime = result.get(idx).get("START_TIME").toString();
					reValue.add(result.get(idx));
					tmpTime = ReportUiUtil.getAddDate(tmpTime, ruleDate,table);
					idx++;
				}
			} else {
				Map<String, Object> tmp = getTimeMap(tmpTime, chartItem);
				if (result.size()>0 && result.get(0).get("PRIORITY") != null){
					tmp.put("PRIORITY", result.get(0).get("PRIORITY"));			
				}
				reValue.add(tmp);
				tmpTime = ReportUiUtil.getAddDate(tmpTime, ruleDate,table);
			}
		}
		return reValue;
	}
	/**
	 * 判断map 的list 集合中 是否存在某个值
	 * @param mapList
	 * @param key
	 * @param value
	 * @return 含有则true
	 */
	private static boolean isListContainsMapValue(List<Map<String,Object>>mapList,String key,String value){
		if (GlobalUtil.isNullOrEmpty(mapList)||GlobalUtil.isNullOrEmpty(key)||GlobalUtil.isNullOrEmpty(value)) {
			return false;
		}
		for (Map<String, Object> map : mapList) {
			if (map.containsKey(key)) {
				String temp=map.get(key).toString();
				if (value.equals(temp)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 针对趋势报表重构数据
	 * 
	 * @param List
	 *            result DB中原始数据
	 * @param int
	 *            sPage 开始页
	 * @param int
	 *            ePage 结束页
	 * @return List 重构后的数据
	 * 
	 */
	public static Map<String, Object> getTimeMap(String tmpTime, String chartItem) {
		Map<String, Object> lastTimeMap = null;
		lastTimeMap = new HashMap<String, Object>();
		lastTimeMap.put("START_TIME", tmpTime);
		String[]  chartItems =chartItem.split(",");
		if(chartItems!=null&&chartItems.length>0){
			for(String item:chartItems){
				lastTimeMap.put(item, "0");
			}
		}else{
			lastTimeMap.put(chartItem, "0");
		}
		return lastTimeMap;
	}
	
	public static Map<Object,Object> reformingResult(Map<Object, Object> subMap,Map<String,Object> rsMap){
		
		List<Map> result = null;
		if (!GlobalUtil.isNullOrEmpty(rsMap) &&
				!GlobalUtil.isNullOrEmpty(rsMap.get("result"))) {
			result = (List<Map>) rsMap.get("result");
		}
		String chartItem = subMap.get("chartItem") == null ? "" : subMap.get("chartItem").toString();
		String[] chartItems=chartItem.split(",");
		int countSign = getCountSign(chartItems, result);
		boolean qushiFlag = subMap.get("chartProperty") != null&& subMap.get("chartProperty").toString().equals("1");// 趋势报表
		Integer subType = (Integer) subMap.get("subType");
		String category = subMap.get("category").toString();
		String serise = (String)subMap.get("serise");
		String subject=(String)subMap.get("subject");
		String deviceType=(String)subMap.get("deviceType");
		String unit="";
		List<Map> rsList = new ArrayList<Map>();
		if (!GlobalUtil.isNullOrEmpty(result)) {
			for (int i = 0, len = result.size(); i < len; i++) {
				Map dataMap = result.get(i);
				for (int j = 0; j < chartItems.length ; j++) {//&& subType != 2(sox默认值为0的bug修改)
					try {
						String chartItemVal=dataMap.get(chartItems[j])==null?"0":dataMap.get(chartItems[j]).toString();
						Object value=null;
						Double chartItemValue =null;
						Long longValue=null;
						if (!chartItemVal.contains(".")) {
							longValue=Long.parseLong(chartItemVal);
							value=longValue;
						}
						if (chartItemVal.contains(".")||countSign > -1) {
							chartItemValue=Double.parseDouble(chartItemVal);
							chartItemValue = ReportUiUtil.getCapability(chartItemValue, countSign);
							value=chartItemValue;
						}
						dataMap.put(chartItems[j], value);
					} catch (NumberFormatException e) {
					}
				}
				rsList.add(i, dataMap);
			}
		}
		if(countSign>-1){
			unit = ReportUiConfig.Capability.get(countSign);
		}
		result = rsList;
		Map<Object,Object> map = new HashMap<Object ,Object>();
		map.put("sumPage", GlobalUtil.isNullOrEmpty(rsMap)?0:rsMap.get("sumPage"));
		map.put("subMap", subMap);
		map.put("unit", unit);
		map.put("result", result);
		return map;
	}
	
	/**
	 * 导出主报表 1.mail计划 2.前台
	 * 
	 * @param RptMasterTbService
	 *            rptMasterTbImp DAO
	 * @param ExpStruct
	 *            exp 报表导出用结构体
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @return LinkedHashMap<String, List> 规范的exp结构体集合
	 * @throws Exception
	 *             2.
	 */
	public static LinkedHashMap<String, List> expMstReport(RptMasterTbService rptMasterTbImp,ExpStruct exp,HttpServletRequest request) throws Exception {
		String mstRptId = exp.getMstrptid();// 主报表ID
		Integer mstRptIdInt = 0;
		if (!GlobalUtil.isNullOrEmpty(mstRptId)) {
			mstRptIdInt=Integer.valueOf(mstRptId);
		}
		String mstSql = ReportUiConfig.MstSubSql;
		Object[] subParam = { mstRptIdInt };
//		List<Map<String,Object>> subResult = rptMasterTbImp.queryTmpList(mstSql, subParam);
		
		List<Map<String,Object>> subResult = new ArrayList<Map<String,Object>>();
		Map<Integer,Integer> rowColumns = new HashMap<Integer, Integer>();
		
		List<Map<String,Object>> subResultTemp = rptMasterTbImp.queryTmpList(mstSql, subParam);
		if (subResultTemp.size()>0) {
			Map subMap = subResultTemp.get(0);
			String viewItem=StringUtil.toString(subMap.get("viewItem"), "");
			if (viewItem.indexOf("2") < 0) {
				exp.setRptType(ReportUiConfig.rptDirection);
				String[] time = ReportUiUtil.getExpTime("month");
				exp.setRptTimeS(time[0]);
				exp.setRptTimeE(time[1]);
			}
		}
		int evtRptsize=subResultTemp.size();
		if (!GlobalUtil.isNullOrEmpty(subResultTemp)) {
			subResult.addAll(subResultTemp);
		}
		ReportBean bean = new ReportBean();
		if (!GlobalUtil.isNullOrEmpty(request)) {
			bean = ReportUiUtil.tidyFormBean(bean, request);
		}
		String nodeType=bean.getNodeType();
		String dvcaddress=bean.getDvcaddress();
		DataSourceService dataSourceService=(DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService");
		if (!GlobalUtil.isNullOrEmpty(bean.getDvctype())
				&& bean.getDvctype().startsWith("Profession/Group")
				&& !GlobalUtil.isNullOrEmpty(nodeType) 
				&& !GlobalUtil.isNullOrEmpty(dvcaddress)) {
			Map map=TopoUtil.getAssetEvtMstMap();
			String mstIds=null;
			List<SimDatasource> simDatasources=dataSourceService.getByIp(dvcaddress);
			if (!GlobalUtil.isNullOrEmpty(simDatasources)) {
				mstIds="";
				for (SimDatasource simDatasource : simDatasources) {
					if (map.containsKey(simDatasource.getSecurityObjectType())) {
						mstIds+=map.get(simDatasource.getSecurityObjectType()).toString()+":::";
					}else {
						String keyString=getStartStringKey(map, simDatasource.getSecurityObjectType());
						if (!GlobalUtil.isNullOrEmpty(keyString)) {
							mstIds+=map.get(keyString).toString()+":::";
						}
					}
				}
				if (mstIds.length()>3) {
					mstIds=mstIds.substring(0,mstIds.length()-3);
				}
			}else {
				if (map.containsKey(nodeType)) {
					mstIds=map.get(nodeType).toString();
				}else {
					String keyString=getStartStringKey(map, nodeType);
					if (!GlobalUtil.isNullOrEmpty(keyString)) {
						mstIds=map.get(keyString).toString();
					}
				}
			}
			if (!GlobalUtil.isNullOrEmpty(mstIds)) {
				String[]mstIdArr=mstIds.split(":::");
				for (String string : mstIdArr) {
					List<Map<String,Object>> subTemp = rptMasterTbImp.queryTmpList(mstSql, new Object[]{StringUtil.toInt(string, 5)});
					if (!GlobalUtil.isNullOrEmpty(subTemp)) {
						int maxCol=0;
						if (!GlobalUtil.isNullOrEmpty(rowColumns)) {
							maxCol=getMaxOrMinKey(rowColumns, 1);
						}
						for (Map map2 : subTemp) {
							Integer row = (Integer) map2.get("subRow")+maxCol;
							map2.put("subRow", row);
						}
						subResult.addAll(subTemp);
					}
				}
			}
		}
		
		if (!GlobalUtil.isNullOrEmpty(bean.getDvctype())
				&& bean.getDvctype().startsWith("Comprehensive")) {
			List<String> dvcTypes = dvcTypes=new ArrayList<String>();
			dvcTypes.add(bean.getDvctype().replace("Comprehensive", ""));
			
			List<String>mstrptidAndNodeTypeList=new ArrayList<String>();
			setMstIdAndScanNodeType(dvcTypes,mstrptidAndNodeTypeList);
			subResultTemp=null;
			if (!GlobalUtil.isNullOrEmpty(mstrptidAndNodeTypeList)) {
				subResultTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt((mstrptidAndNodeTypeList.get(0).split("IDandNODEtype"))[0], StringUtil.toInt(bean.getTalTop(),5))});
				Map<Integer,Integer> rowColumnsTeMap = ReportModel.getRowColumns(subResultTemp) ;
				evtRptsize=subResultTemp.size();
				if (!GlobalUtil.isNullOrEmpty(subResultTemp)) {
					for (Map map2 : subResultTemp) {
						map2.put("subject", (mstrptidAndNodeTypeList.get(0).split("IDandNODEtype"))[1]);
					}
					subResult.addAll(subResultTemp);
					rowColumns.putAll(rowColumnsTeMap);
				}
				
				int len=mstrptidAndNodeTypeList.size();
				for (int i=1;i<len;i++) {
					String mstrptidAndNodeType=mstrptidAndNodeTypeList.get(i);
					String string=mstrptidAndNodeType.split("IDandNODEtype")[0];
					List<Map<String,Object>> subTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt(string, StringUtil.toInt(bean.getTalTop(),5))});
					if (!GlobalUtil.isNullOrEmpty(subTemp)) {
						int maxCol=0;
						if (!GlobalUtil.isNullOrEmpty(rowColumns)) {
							maxCol=getMaxOrMinKey(rowColumns, 1);
						}
						for (Map map2 : subTemp) {
							Integer row = (Integer) map2.get("subRow")+maxCol;
							map2.put("subRow", row);
							map2.put("subject", mstrptidAndNodeType.split("IDandNODEtype")[1]);
						}
						subResult.addAll(subTemp);
						Map<Integer,Integer> rowColTemp = ReportModel.getRowColumns(subTemp) ;
						rowColumns.putAll(rowColTemp);
					}
				}
			}
		}
		
		List<ExpDateStruct> expList = new ArrayList<ExpDateStruct>(); // 子报表内容
		Map<ReportExecutor.SubjectKey, Map<Integer,ExpDateStruct>> exportMap = Collections.synchronizedMap(new LinkedHashMap());
		ExecutorService threadPool = Executors.newFixedThreadPool(subResult.size(),new TsmThreadFactory("ReportSubjectExport")) ;
		LinkedHashMap<String, List> expMap = null;
		try {
			List<ReportExecutor> tasks = new ArrayList<ReportExecutor>(subResult.size()) ;
			int order = 0;
			for (Map sub:subResult) {
				order+=100 ;
				tasks.add(new ReportExecutor(order,rptMasterTbImp, exp, exportMap, expList, sub, request,SID.currentUser())) ;
			}
			threadPool.invokeAll(tasks) ;
			expMap = new LinkedHashMap<String, List>(exportMap.size());
			for(Map.Entry<ReportExecutor.SubjectKey, Map<Integer,ExpDateStruct>> entry:exportMap.entrySet()){
				expMap.put(entry.getKey().subject, new ArrayList(entry.getValue().values())) ;
			}
		}finally{
			threadPool.shutdownNow() ;
		}
		return expMap;
	}

	public static LinkedHashMap<String, List> expMstReport2(RptMasterTbService rptMasterTbImp, ExpStruct exp,HttpServletRequest request) throws Exception {
		String mstRptId = exp.getMstrptid();// 主报表ID
		Integer mstRptIdInt = Integer.valueOf(mstRptId);
		String mstSql = ReportUiConfig.MstSubSql;
		Object[] subParam = { mstRptIdInt };
		List subResult = rptMasterTbImp.queryTmpList(mstSql, subParam);
		Map sub = new HashMap();
		List<ExpDateStruct> expList = new ArrayList(); // 子报表内容
		String mstTitle = null;// 主报表名称
		String pdffooter = null;// pdf footer
		LinkedHashMap<String, List> expMap = new LinkedHashMap();
		int mstType = 0;
		for (int i = 0; i < subResult.size(); i++) {
			sub = (Map) subResult.get(i);
		    boolean flag = ReportUiUtil.isSystemLog(sub);
			mstType = Integer.parseInt(sub.get("mstType").toString());
		 
			// 子报表信息
			List ruleResult = getRuleRs(mstType, sub, rptMasterTbImp);
			List<Map> resultList = exp.getResultList();
			if(resultList!=null){
				for (Map map : resultList) {
					for (Object o2 : ruleResult) {
						Map map2=(Map)o2;
						if(map.get("sqlParam").equals(map2.get("sqlParam"))){
							map2.put("sqlValue",map.get("sqlValue"));
							map2.put("sqlDefValue",map.get("sqlValue"));
						}
					}
				}
			}
			boolean isCoreNode = ReportUiUtil.isCoreNodeReport(sub);
			if(isCoreNode){
				for (int j = 0; j < ruleResult.size(); j++) {
					Map map = (Map) ruleResult.get(j);
					String talCategoryKey=(String)map.get("sqlParam");
					if("and dvcAddress = ?".equals(talCategoryKey) ||"and alias.dvcAddress = ?".equals(talCategoryKey) ||"and fwrisk.dvcAddress = ?".equals(talCategoryKey)){
						ruleResult.remove(j);
						break;
					}
				}
			}
			
			// 设备类型 报表原子主题 vpn risk等
			String subSubject = (String) sub.get("subSubject");
			
//			ExpDateStruct exptmp = (ExpDateStruct) model.createHtml(sub,ruleResult, null, request, exp);
			Integer subType = (Integer) sub.get("subType");
			boolean qushiFlag = sub.get("chartProperty") != null && sub.get("chartProperty").toString().equals("1");
			SqlStruct struct =null;
			if (qushiFlag) {
				struct=resetImpoTime(mstType, ruleResult,sub, request, exp);
			}else {
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, exp.getRptTimeS(), exp.getRptTimeE());
			}
			
			ExpDateStruct exptmp = createExp2(sub, struct, subType, exp, request);
			exptmp.setTalCategoryLevel((short) 2);
			exptmp.setMstType(mstType);
			exptmp.setSubType(subSubject.replace("Monitor/", "")+"*");
			// 将同一种主题的报表放在一起
			if (expMap.get(subSubject) == null) {
				List<ExpDateStruct> tpmExpList = new ArrayList();
				tpmExpList.add(exptmp);
				expMap.put(subSubject, tpmExpList);
			} else
				expMap.get(subSubject).add(exptmp);
			// 子报表内容
			//加入下钻标题
			String talCategory=""; 
			if (!ReportUiUtil.checkNull(exptmp.getTalCategory())){
				String talCategoryTemp=(String)exp.getMap().get("talCategory");
				if(talCategoryTemp!=null){
					if(flag){
						talCategoryTemp=ReportUiUtil.getDeviceTypeName(talCategoryTemp, Locale.getDefault());
					}
					exptmp.setTalCategory(new String[]{talCategoryTemp});
				}
			}
			
			if (ReportUiUtil.checkNull(exptmp.getTalCategory())){
				String[] talCategoryArray=exptmp.getTalCategory();
				if(talCategoryArray!=null&&talCategoryArray.length>0){
					for (int j = 0; j < talCategoryArray.length; j++) {
						if(talCategoryArray[j]!=null&&!talCategoryArray[j].equals("")&&!talCategoryArray[j].equals("null")){
							if(flag){
								talCategoryArray[j]=ReportUiUtil.getDeviceTypeName(talCategoryArray[j], Locale.getDefault());
							}
							talCategory+="->"+talCategoryArray[j];
						}
					}
					if(talCategory.length()>2){
						talCategory=talCategory.substring(2);
					}
				}
				
				exptmp.setTitle(exptmp.getTitle().replace("(","(" + talCategory + " "));
			}
			expList.add(exptmp);
			exp.setRptSummarize(StringUtil.nvl((String)sub.get("summarize"),""));
			mstTitle = (String) sub.get("mstName");
			pdffooter = StringUtil.nvl((String)sub.get("pdffooter"),"");			
		}
		exp.setRptName(mstTitle);
		exp.setPdffooter(pdffooter);
		return expMap;
	}
	private static SqlStruct resetImpoTime(Integer mstType,List ruleResult,Map sub,HttpServletRequest request,ExpStruct exp){
		if (GlobalUtil.isNullOrEmpty(sub)) {
			return null;
		}
		boolean qushiFlag = sub.get("chartProperty") != null && sub.get("chartProperty").toString().equals("1");
		SqlStruct struct=null;
		String tableSql = StringUtil.nvl((String)sub.get("tableSql")) ;
		String pageSql = StringUtil.nvl((String)sub.get("pagesql")) ;
		String chartSql = StringUtil.nvl((String)sub.get("chartSql")) ;
		String subName = StringUtil.nvl((String)sub.get("subName"));
		String mstName = StringUtil.nvl((String)sub.get("mstName"));
		if (qushiFlag && (subName.contains("时趋势")||subName.contains("日趋势")||subName.contains("月趋势")
				||subName.contains("时总趋势")||mstName.contains("时总趋势")
				||mstName.contains("时趋势")||mstName.contains("日趋势")||mstName.contains("月趋势"))) {
			if (tableSql.indexOf("Hour") > 20 || tableSql.indexOf("_hour") > 20) {
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, ReportUiUtil.toStartTime("hour", exp.getRptTimeE()), exp.getRptTimeE());
			} else if (tableSql.indexOf("Day") > 20 || tableSql.indexOf("_day") > 20) {
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, ReportUiUtil.toStartTime("day", exp.getRptTimeE()), exp.getRptTimeE());
			} else if (pageSql.indexOf("Hour") > 20 || pageSql.indexOf("_hour") > 20 || chartSql.indexOf("Hour") > 20){
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, ReportUiUtil.toStartTime("hour", exp.getRptTimeE()), exp.getRptTimeE());
			} else if (pageSql.indexOf("Day") > 20 || pageSql.indexOf("_day") > 20|| chartSql.indexOf("Day") > 20) {
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, ReportUiUtil.toStartTime("day", exp.getRptTimeE()), exp.getRptTimeE());
			} else{
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, exp.getRptTimeS(), exp.getRptTimeE());
			}	
		}else if(sub.get("subName").toString().indexOf("分布图") > 1){
			if (tableSql.indexOf("Hour") > 20 || tableSql.indexOf("_hour") > 20) {
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, ReportUiUtil.toStartTime("undefined", exp.getRptTimeE()), exp.getRptTimeE());
			}else{
				struct = getSqlStruct(mstType, ruleResult, request, exp, sub, exp.getRptTimeS(), exp.getRptTimeE());
			}
		} else{
			struct = getSqlStruct(mstType, ruleResult, request, exp, sub, exp.getRptTimeS(), exp.getRptTimeE());
		}
		return struct;
	}
	private static boolean isEvtAssetReport(HttpServletRequest request){
		if (GlobalUtil.isNullOrEmpty(request)) {
			return false;
		}
		String nodeType=request.getParameter(ReportUiConfig.nodeType);
		String dvcaddress=request.getParameter(ReportUiConfig.dvcaddress);
		String dvcType = request.getParameter(ReportUiConfig.dvctype);
		if (!GlobalUtil.isNullOrEmpty(nodeType)
				&& !GlobalUtil.isNullOrEmpty(dvcType)
				&& dvcType.startsWith("Profession/Group")
				&& !GlobalUtil.isNullOrEmpty(nodeType) 
				&& !GlobalUtil.isNullOrEmpty(dvcaddress)){
			return true;
		}
		return false;
	}
	public static ExpDateStruct createExp2(Map<Object, Object> subMap, SqlStruct sqlStruct, int subType, ExpStruct exp,HttpServletRequest request) throws Exception {
		ExpDateStruct expI = new ExpDateStruct();
		String runSql = getRunSql(subType);// 获取sql
		String sql = subMap.get(runSql).toString();
		String tableName=subMap.get("subTableName").toString();
		sql = getTimeSql(sql, sqlStruct.getsTime(), sqlStruct.geteTime());
		String type="";
		if (request != null) {
			type=request.getParameter(ReportUiConfig.dvctype);
		}else {
			type=subMap.get("subSubject")==null?"":subMap.get("subSubject").toString();
		}
		boolean isevtReport="Profession/Group".equals(type)||"Profession/Group/Asset".equals(type);
		boolean isComprehensive=type.startsWith("Comprehensive");
		boolean isEvtAssetReport=isEvtAssetReport(request);
		int paramNo=0;
		if (!isevtReport) {
			sql += sqlStruct.getSql();
			if (sql.indexOf("union") != -1) {
				String rptIp = exp.getRptIp();
				String dvcIP=sqlStruct.getDvcIp();
				if("onlyByDvctype".equals(rptIp)||"onlyByDvctype".equals(dvcIP)){
					sql=sql.replace("DVC_ADDRESS=?", " ("+getDvcIp(sqlStruct.getDevTypeName(),"DVC_ADDRESS =")+")");
				}
			}
		}else {
			if (sql.indexOf("union") != -1) {
				if (sqlStruct != null) {
					String sqlunion = sqlStruct.getSql();
					sql = sql.replaceAll(
							"union",sqlunion+ " union");
				}
			}
			if (sql.indexOf("DVC_ADDRESSInAndTimeConditionGroup") != -1
					||sql.indexOf("ipInAndTimeConditionGroup") != -1) {
				paramNo+=stringNumbers(sql,"DVC_ADDRESSInAndTimeConditionGroup");
				paramNo+=stringNumbers(sql,"ipInAndTimeConditionGroup");
				if (sqlStruct != null) {
					String sqlpar = sqlStruct.getSql();
					sql = sql.replaceAll(
							"DVC_ADDRESSInAndTimeConditionGroup",sqlpar);
					sqlpar=sqlpar.replaceAll("DVC_ADDRESS", "IP");
					sql = sql.replaceAll(
							"ipInAndTimeConditionGroup",sqlpar);
					if (sqlpar.contains("group")) {
						sql+=" "+sqlpar.substring(sqlpar.indexOf("group"));
					}
				}
			}else if (sql.indexOf("DVC_ADDRESSEqAndTimeCondition") != -1
					||sql.indexOf("ipEqAndTimeCondition") != -1) {
				paramNo+=stringNumbers(sql,"DVC_ADDRESSEqAndTimeCondition");
				paramNo+=stringNumbers(sql,"ipEqAndTimeCondition");
				if (sqlStruct != null) {
					String sqlpar = sqlStruct.getSql();
					sql = sql.replaceAll(
							"DVC_ADDRESSEqAndTimeCondition",sqlpar);
					sqlpar=sqlpar.replaceAll("DVC_ADDRESS", "IP");
					sql = sql.replaceAll(
							"ipEqAndTimeCondition",sqlpar);
				}
			}else {
				sql += sqlStruct.getSql();
			}
			
			if (sql.indexOf("union") != -1 && sql.indexOf("LOG_FILE") != -1){
				String[] sqls=sql.split("union");
				sql="";
				for (int i=0;i<sqls.length;i++) {
					if (sqls[i].contains("LOG_FILE")) {
						sqls[i]=sqls[i].replaceAll("DVC_ADDRESS", "IP");
					}
					sql+=sqls[i]+" union ";
				}
				sql=sql.substring(0, sql.length()-7);
			}
		}
		List result=null;
		String[] nodeId=null;
		if(request==null){ 
			NodeMgrFacade nodeMgrFacade=(NodeMgrFacade)SpringContextServlet.springCtx.getBean("nodeMgrFacade");
			List<Node> nodes=nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, false, false, false);
			if(ObjectUtils.isNotEmpty(nodes)){
				int len=nodes.size();
				nodeId=new String[len];
				for (int i = 0; i < len; i++) {
					nodeId[i]=nodes.get(i).getNodeId();
				}
			}else{
				log.error("createExp(),nodes==null!!!");
				throw new Exception("createExp(),nodes==null!!!");
			}
		}else{
			nodeId=request.getParameterValues("nodeId");
		}
		int unionCount=stringNumbers(sql,"union");
		List sqlparam=new ArrayList();
		sqlparam.addAll(sqlStruct.getSqlparam());
		if (isevtReport) {
			if (unionCount > 0 && !isEvtAssetReport) {
				for (int i = 0; i < unionCount; i++) {
					sqlparam.addAll(sqlStruct.getSqlparam());
				}
			}
			if (paramNo>0) {
				sqlparam.clear();
				for (int i = 0; i < paramNo; i++) {
					sqlparam.addAll(sqlStruct.getSqlparam());
				}
			}
		}
		List resultValue =getList(sql, sqlparam,sqlStruct.getSqlpage(),tableName,nodeId,request);
		
		if(resultValue!=null&&resultValue.size()>1){
			result=(List)resultValue.get(1);
		}
		
		boolean qushiFlag = subMap.get("chartProperty") != null && subMap.get("chartProperty").toString().equals("1");// 趋势报表
		// if (qushiFlag && result.size() > 0)// 针对趋势报表重构数据
		// 总记录数 是否显示更多
		Integer sumPage = sqlStruct.getSqlpage();
		if(sumPage==null){
			sumPage=5;
		}
		// DB返回的 详细数据
		String chartItem = subMap.get("chartItem") == null ? "" : subMap.get("chartItem").toString();
		String[] chartItems=chartItem.split(",");
		String category = (String)subMap.get("category");
		
		String tableType = null;
		if (qushiFlag)// 针对趋势报表重构数据
		{
			String mstType = subMap.get("mstType").toString();
			tableType = ReportUiUtil.getTable(sql);
			result = changeResultPro(result, sqlStruct.getsTime(), sqlStruct.geteTime(), tableType,category, chartItem, mstType);
			sumPage=result.size();
		}else{
			Map<String,Object> map=null;
			
			if (sql.indexOf("union") != -1) {
				map=reformingStatisticDataForUnion(result, category, chartItems);
			} else {
				map=reformingStatisticDataForTop(result,category,chartItems,sumPage,false);
			}
			
			List reformingResult=(List)map.get("result");
			int reformingSumPage=(Integer)map.get("sumPage");
			result=reformingResult;
			sumPage=reformingSumPage;
		}
		
		int countSign = 0;
		double tmpCountSign = 0;
		boolean bCountSing = true;
		if (!GlobalUtil.isNullOrEmpty(result)) {
			for (Object o : result) {
				Map map = (Map) o;
				double tmpCount = ReportUiUtil.getTByteValue(chartItems, map);
				if (tmpCount > tmpCountSign){
					tmpCountSign = tmpCount;
					break;
				}
			}
		}
		if(bCountSing &&tmpCountSign>0){
			countSign = ReportUiUtil.getCountCapability(tmpCountSign);
			bCountSing = false;
		}
		if (!qushiFlag) {
			int top = StringUtil.toInt(exp.getTop(),0);
			if(ObjectUtils.isNotEmpty(result)){
				result = result.subList(0, Math.min(top,result.size()));
			}
		}
	
		if (subType != 2) {// 图
			ReportTalChart chart = new ReportTalChart();
			ReportUiUtil.getUnit(subMap, countSign);
			List resultList=formatPictureData(result,chartItems);
//			ReportUiUtil.filterExpData(result, countSign);
			expI.setSubChart(chart.creChart(result, subMap, request, null));
			result=resultList;
		}
		countSign = 0;
		if (subType != 1) {// 文
			countSign = ReportUiUtil.getCountSign(result);
			ReportUiUtil.filterExpList(result, countSign, qushiFlag, tableType);
			notypeShowFormat(result);
			expI.setTable(result);
		}
		expI.setTitleLable(ReportUiUtil.addExpTableTitle((String) subMap.get("tableLable"), countSign));
		// 主报表名
		String mstTitle = (String) subMap.get("mstName");
		String subTitle = (String) subMap.get("subName");
		String subrptType = subMap.get("mstType") + "";
		subTitle = ReportUiUtil.viewRptName(subTitle, subrptType);
		expI.setMstTitle(mstTitle);
		expI.setTitle(subTitle);
		if("onlyByDvctype".equals(sqlStruct.getDvcIp())){
			expI.setDvcIp(sqlStruct.getOnlyByDvctype());
			expI.setOnlyByDvctype("onlyByDvctype");
		}else{
			expI.setDvcIp(sqlStruct.getDvcIp());
		}
		
		expI.setSubTableFile(subMap.get("tableFiled").toString());
		expI.setSTime(sqlStruct.getsTime());
		expI.setETime(sqlStruct.geteTime());
		expI.setTalCategory(sqlStruct.getTalCategory());
		return expI;
	}
	private static List formatPictureData(List result,String[]chartItems){
		if (GlobalUtil.isNullOrEmpty(result)) {
			return result;
		}
		result=(List<Map>)result;
		List resultList=copyList(result);
		if (result.size()<showNo) {
			return result;
		}
		int size=result.size()+27;
		int sizeWidth=size/showNo;
		try {
			Map map=(Map)result.get(0);
			if (map.containsKey("BYTES")
					||map.containsKey("BYTE")
					||map.containsKey("OPCOUNT")
					||map.containsKey("COUNTS")
					||map.containsKey("opCount")
					||map.containsKey("opCount1")
					||map.containsKey("opCount2")
					||map.containsKey("opCount3")
					||map.containsKey("opCount4")
					||map.containsKey("BYTES_IN")
					||map.containsKey("BYTES_OUT")) {
				
				int tempc=0;
				int tempe=0;
				boolean isRemove=true;
				boolean isRemoveD=true;
				boolean isRemoveE=true;
				int sremoveTotal=0;
				for (int i=1;i<result.size()-1;) {
					if (result.size()<showNo) {
						break;
					}
					Map dataMap=(Map)result.get(i);
					int ends=0;
					ends=result.size()-2;
					if (ends<showNo-2) {
						break;
					}
					Map eMap=(Map)result.get(ends);
					
					if (!GlobalUtil.isNullOrEmpty(dataMap)) {
						if (0==ReportUiUtil.getDoubleValue(chartItems,dataMap)) {
							if (isRemove&&tempc<sizeWidth*3+1) {
								tempc++;
								result.remove(dataMap);
								sremoveTotal++;
								if (result.size()<showNo) {
									break;
								}
							}else{
								isRemove=true;
								i++;
								tempc=0;
							}
							isRemoveD=false;
						}else {
							isRemove=false;
							if (sizeWidth>1) {
								Random random = new Random(sizeWidth);
								int nodelete=Math.abs(random.nextInt());
								for (int j = 0; j < sizeWidth; j++) {
									if (j!=nodelete) {
										int deleteIndex=i+j;
										if (deleteIndex<result.size()-1) {
											if (isRemoveD&&0!=ReportUiUtil.getDoubleValue(chartItems,(Map)result.get(deleteIndex))) {
												result.remove(deleteIndex);
												sremoveTotal++;
												isRemoveD=true;
											}
											if (result.size()<showNo) {
												break;
											}
										}
									}
								}
							}else{
								if (isRemoveD) {
									result.remove(i);
								}
								isRemoveD=true;
								sremoveTotal++;
								if (result.size()<showNo) {
									break;
								}
							}
							i++;
						}
					}
					if (!GlobalUtil.isNullOrEmpty(eMap)) {
						if (0==ReportUiUtil.getDoubleValue(chartItems,eMap)) {
							if (isRemoveE&&tempe<sizeWidth*2+1) {
								tempe++;
								result.remove(eMap);
								if (result.size()<showNo) {
									break;
								}
							}else{
								isRemoveE=true;
								ends--;
								tempe=0;
							}
						}else{
							isRemoveE=false;
						}
					}
					
				}
			}
			return resultList;	
		} catch (Exception e) {
			return result;
		}
	}
	private static List copyList(List result){
		if (GlobalUtil.isNullOrEmpty(result)) {
			return result;
		}
		List resultList=new ArrayList();
		for (Object object : result) {
			resultList.add(object);
		}
		return resultList;
	}
	/**
	 * 显示检索条件文本框
	 * 
	 * @param String
	 *            lables 检索条件文本框名称
	 * @param String
	 *            htmls 检索条件文本框属性
	 * @return 检索条件文本框Html
	 */
	public static String createSearchItem(String lables, String htmls) {
		StringBuffer reValue = new StringBuffer();
		int i = 0;
		if (!GlobalUtil.isNullOrEmpty(htmls)) {
			String[] html = htmls.split(",");
			if (!GlobalUtil.isNullOrEmpty(lables)) {
				for (String lable : lables.split(",")) {
					try {
						if (!html[i].equals("99")) {// 99不显示该文本框
							reValue.append(ReportUiUtil.addSpan(lable)
									+ "<input type=\"text\" id=\"Param"
									+ new Integer(html[i]) + "\""
									+ ReportUiConfig.pageStyle + " name=\"Param"
									+ new Integer(html[i]) + "\">");
						}
						i++;
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
		}
		return reValue.toString();

	}

	/**
	 * 获取总页数
	 * 
	 * @param int
	 *            resultSize 总记录数
	 * @param int
	 *            pageSize 每页显示记录数
	 * 
	 * @return int 返回页数
	 */
	public static int getPages(int resultSize, int pageSize) {
		int reValue = 0;
		reValue = (resultSize + pageSize - 1) / pageSize;
		return reValue;
	}
	private static String countTime(String time,String type) {
		int minuteV = 0;
		SimpleDateFormat sdf=new SimpleDateFormat(ReportUiConfig.dFormat1);
		Date date=null;
		Calendar calendar=Calendar.getInstance();
		try {
			date=sdf.parse(time);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		calendar.setTime(date);
		
		if("hour".equals(type)){
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)/10*10);
			calendar.set(Calendar.SECOND, 0);
			date=calendar.getTime();
			time = sdf.format(date);
		}else if("day".equals(type)){
			minuteV = calendar.get(Calendar.HOUR_OF_DAY);
			if (minuteV > 18){
				calendar.set(Calendar.HOUR_OF_DAY, 18);
			}else if(minuteV > 12){
				calendar.set(Calendar.HOUR_OF_DAY, 12);
			}else if(minuteV > 6){
				calendar.set(Calendar.HOUR_OF_DAY, 6);
			}else if(minuteV > 0){
				calendar.set(Calendar.HOUR_OF_DAY, 0);
			}
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			date=calendar.getTime();
			time = sdf.format(date);
		}
		return time;
	}
	
	private static double getMapMaxValue(String[] chartItems,List<Map> result){
		double tmpvalue = 1024;
		if (!GlobalUtil.isNullOrEmpty(result)) {
			boolean isMin=false;
			for (Map dataMap : result) {
				if (!GlobalUtil.isNullOrEmpty(dataMap)) {
					if (tmpvalue<ReportUiUtil.getTByteValue(chartItems,dataMap)) {
						tmpvalue=ReportUiUtil.getTByteValue(chartItems,	dataMap);
						isMin=true;
						return tmpvalue;
					}
				}
			}
			if (!isMin) {
				return ReportUiUtil.getTByteValue(chartItems,result.get(0));
			}
		}
		return 0;
	}
	public static int getCountSign(String[] chartItems,List<Map> result ){
		int countSign = -1;// 决定显示 kb mb
		double tmpCountSign = 0;
		if (!GlobalUtil.isNullOrEmpty(result)) {
			tmpCountSign=getMapMaxValue(chartItems, result);
			countSign = ReportUiUtil.getCountCapability(tmpCountSign);
		}
		return countSign;
	}
	@SuppressWarnings("unchecked")
	public static List<String> getDeviceTypeList(DataSourceService dataSourceService,SID sid){
		if (GlobalUtil.isNullOrEmpty(sid)) {
			return null;
		}
		Set<?> devices=GlobalUtil.isNullOrEmpty(sid.getUserDevice())?Collections.emptySet():sid.getUserDevice();
		List<String> dvcTypes = null;
		if (sid.isOperator()) {
			dvcTypes = dataSourceService.getDistinctDvcType(DataSourceService.CMD_ALL);			
		}else{
			List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
			dvcTypes = new ArrayList<String>();
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
					if(!dvcTypes.contains(simDatasource.getSecurityObjectType())){
						dvcTypes.add(simDatasource.getSecurityObjectType());
					}
				}
			}
		}
		if (!sid.hasAuditorRole() && dvcTypes.contains(LogKeyInfo.LOG_SYSTEM_TYPE)) {
			dvcTypes.remove(LogKeyInfo.LOG_SYSTEM_TYPE);
		}
		return dvcTypes;
	}
	@SuppressWarnings("unchecked")
	public static List<String> getDeviceIpList(DataSourceService dataSourceService,SID sid){
		List<String> deviceIpList = null;
		if (sid==null) {
			return deviceIpList;
		}
		Set<?> devices=GlobalUtil.isNullOrEmpty(sid.getUserDevice())?Collections.emptySet():sid.getUserDevice();
		List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		if (sid.hasOperatorRole()){
			deviceIpList=new ArrayList<String>();
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
					if(!deviceIpList.contains(simDatasource.getDeviceIp())){
						deviceIpList.add(simDatasource.getDeviceIp());
					}
				}
			}
		}
		return deviceIpList;
	}

	private static String getStartStringKey(Map<Object,Object> map,String string){
		if (GlobalUtil.isNullOrEmpty(map) || GlobalUtil.isNullOrEmpty(string)) {
			return "";
		}
		for (Map.Entry<Object,Object> entry : map.entrySet()) {
			Object keyObject=entry.getKey();
			if (!GlobalUtil.isNullOrEmpty(keyObject) && string.startsWith(keyObject.toString())) {
				return keyObject.toString();
			}
		}
		return "";
	}
	
	private static void setMstIdAndScanNodeType(List<String>dvcTypes,List<String>mstrptidAndNodeTypeList){
		if (!GlobalUtil.isNullOrEmpty(dvcTypes)) {
			ReportService reportService=(ReportService) SpringContextServlet.springCtx.getBean("reportService");
			for (String dvcType : dvcTypes) {
				List<Map> rptList = reportService.getRptMaster("Comprehensive"+dvcType);
				if (!GlobalUtil.isNullOrEmpty(rptList)) {
					for (Map map : rptList) {
						if (map.get("id")!=null) {
							String string=StringUtil.toString(map.get("id"));
							if (! mstrptidAndNodeTypeList.contains(string)) {
								mstrptidAndNodeTypeList.add(string+"IDandNODEtypeComprehensive"+dvcType);
							}
						}
					}
				}
			}
			
		}
	}
	
	private static Integer getMaxOrMinKey(Map<Integer,Integer> rowColumns,int status){
		if (GlobalUtil.isNullOrEmpty(rowColumns)) {
			if (status>=0)return Integer.MIN_VALUE;
			return Integer.MAX_VALUE;
		}
		Integer maxKey=Integer.MIN_VALUE;
		Integer minKey=Integer.MAX_VALUE;
		for (Map.Entry<Integer, Integer> entry : rowColumns.entrySet()) {
			Integer temp=entry.getKey();
			if (maxKey<temp) {
				maxKey=temp;
			}
			if (minKey>temp) {
				minKey =temp;
			}
		}
		Integer resultInteger=null;
		if (status>=0) {
			resultInteger=maxKey;
		}else {
			resultInteger=minKey;
		}
		return resultInteger;
	}
}
