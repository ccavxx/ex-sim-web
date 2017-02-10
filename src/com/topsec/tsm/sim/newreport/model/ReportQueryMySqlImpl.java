package com.topsec.tsm.sim.newreport.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.newreport.bean.PageVo;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;
import com.topsec.tsm.sim.newreport.dao.ReportNewDao;
import com.topsec.tsm.sim.newreport.handler.QueryConditionsFormat;
import com.topsec.tsm.sim.newreport.handler.QueryResultFormat;
import com.topsec.tsm.sim.newreport.util.QueryUtil;
import com.topsec.tsm.sim.newreport.util.ResourceContainer;
import com.topsec.tsm.sim.newreport.util.ResultOperatorUtils;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.model.ReportDispatchModel;
import com.topsec.tsm.sim.report.util.ThreadPoolExecuteDispatchUtil;

/**
 * @ClassName: ReportQueryMySqlImpl
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月13日下午2:17:33
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ReportQueryMySqlImpl implements ReportQuery {
	private static Logger log = Logger.getLogger(ReportQueryMySqlImpl.class);
	
	private ReportNewDao reportNewDao;
	private QueryConditionsFormat queryConditionsFormat;
	private QueryResultFormat queryResultFormat;
	private List<Map<String,Object>> allParentTheme;
	public void setReportNewDao(ReportNewDao reportNewDao) {
		this.reportNewDao = reportNewDao;
	}
	
	public void setQueryConditionsFormat(QueryConditionsFormat queryConditionsFormat) {
		this.queryConditionsFormat = queryConditionsFormat;
	}

	public void setQueryResultFormat(QueryResultFormat queryResultFormat) {
		this.queryResultFormat = queryResultFormat;
	}

	@Override
	public <T> List<T> findByConditions(String queryString, Object... params) {
		NodeMgrFacade nodeMgrFacade=(NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade"); 
		com.topsec.tsm.sim.resource.persistence.Node auditor = nodeMgrFacade.getKernelAuditor(false);
		List<Object> paramList=arrayToList(params);
		return findByConditions(queryString,paramList,new String[]{auditor.getNodeId()});
	}

	@Override
	public List<Map<String,Object>> findBySQL(String queryString,Object ... params){
		List<List<Map<String,Object>>> result=findByConditions(queryString, params);
		if(ObjectUtils.isNotEmpty(result) && ObjectUtils.isNotEmpty(result.get(0))){
			return result.get(0) ;
		}
		return Collections.emptyList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String,Object>> findParentTheme(String securityObjectType) {
		List<Map<String,Object>> listUs=null;
		List<Map<String, Object>> fieldList=null;
		
		if (!LogKeyInfo.LOG_SIM_EVENT.equals(securityObjectType) &&
				!LogKeyInfo.LOG_SYSTEM_TYPE.equals(securityObjectType) &&
				!LogKeyInfo.LOG_SYSTEM_RUN_TYPE.equals(securityObjectType) &&
				!"MONITOR".equals(securityObjectType)) { 
			String string="AllType"; 
			listUs = reportNewDao.findParentTheme(string);
			Map<String, Object> templateMap=IndexTemplateUtil.getInstance().getTemplateByDeviceType(securityObjectType);
			if (null != templateMap && templateMap.size() != 0)
				fieldList=(List<Map<String, Object>>)templateMap.get("fieldList");
		}else {
			listUs = new ArrayList<Map<String,Object>>();
		}
		
		List<Map<String,Object>> list = reportNewDao.findParentTheme(securityObjectType);
		if((null == list|| list.size()==0) && null != securityObjectType && !securityObjectType.equals("MONITOR")){
			list = reportNewDao.findParentTheme(securityObjectType.substring(0,securityObjectType.indexOf("/"))); 
		}
		if (null != list && list.size()>0 ) {
			listUs.addAll(list);
		}
		
		if (null == allParentTheme) {
			allParentTheme=reportNewDao.findParentTheme();
		}
		/**
		 * <p>以下代码是取得查询模板中所含字段自动绑定显示报表，<br/>
		 * 并替换模板中的需要显示字段的名称</p>
		 */
		if (null != fieldList) {
			for (Map<String, Object> fieldMap : fieldList) {
				for (Map<String, Object> map : allParentTheme){
					if (fieldMap.get("name").equals(map.get("fieldType")) ){
						Map<String, Object> cMap=new HashMap<String, Object> (2);
						cMap.putAll(map);
						if(!listUs.contains(cMap)) {
							String reportName=(String)cMap.get("reportName");
							int leftco=reportName.indexOf("{");
							int rightco=reportName.indexOf("}",leftco);
							if (-1 != leftco && -1 != rightco) {
								String needRepalce=reportName.substring(leftco+1,rightco);
								Object reval="";
								for (Map<String, Object> field : fieldList)
									if (field.get("name").equals(needRepalce)) 
										reval=field.get("alias");
								reportName=reportName.substring(0,leftco)+reval+reportName.substring(rightco+1);
								cMap.put("reportName", reportName);
							}
							listUs.add(cMap);
						}
						
					}
				}
			}
		}
		/**
		 * <p>以下代码是通过读取报表映射文件主动生成 报表<br/>
		 * 如果没有映射文件的话可以忽略以下代码
		 */
		//* --------- 映射文件报表开始
		List<Map<String,Object>> xmlMaps=ResourceContainer.getParentThemesByDeviceType(securityObjectType);
		if (null != xmlMaps) 
			listUs.addAll(xmlMaps);
		//* --------- 映射文件报表结束
		
		/**
		 * <p>以下代码是对左侧的数重新排序<br/>
		 * 比如说：概要报表放在第一的位置，其他的报表放在最后面</p>
		 */
		if (listUs.size()>0) 
			reSortListByName(listUs);
		
		/**
		 * <p>以下注释掉的内容是为了比较灵活配置的报表，比如说 自定义报表 或者给报表灵活加主题可以打开用，<br/>
		 * 基本报表引用的话可能造成重复，要打开用于基本报表的话需要注意不要重复。</p>
		 */
		/*List<Map<String,Object>> relevanceList =reportNewDao.findRelevanceParentTheme(securityObjectType);
		if ((null ==relevanceList || relevanceList.size() == 0) && null != securityObjectType) {
			relevanceList=reportNewDao.findRelevanceParentTheme(securityObjectType.substring(0,securityObjectType.indexOf("/")));
		}
		if (null != relevanceList && relevanceList.size()>0) {
			for (Map<String, Object> map : relevanceList) {
				Object object=map.get("coverReportName");
				if (null !=object && !"".equals(object.toString().trim())) {
					map.put("reportName", object);
				}
			}
			listUs.addAll(relevanceList);
		}*/
		return listUs;
	}

	@Override
	public List<Map<String,Object>> findSimpleSubThemes(Integer parentId) {
		List<Map<String, Object>>maps = reportNewDao.findSimpleSubThemes(parentId);
		if(null == maps || maps.size() == 0)
			maps = ResourceContainer.getParentSubs(parentId);
		return maps;
	}

	@Override
	public List<Map<String, Object>> findDetailSubThemeList(Integer parentId) {
		List<Map<String, Object>>maps = reportNewDao.findDetailSubThemeList(parentId);
		if(null == maps || maps.size() == 0)
			maps = ResourceContainer.getDetailParentSubs(parentId);
		return maps;
	}

	@Override
	public Map<String,Object> findDetailSubTheme(Integer parentSubId) {
		Map<String,Object> map = reportNewDao.findDetailSubThemes(parentSubId);
		if (null == map || map.size() == 0) 
			map=ResourceContainer.getSubThemes(parentSubId);
		return map;
	}

	@Override
	public <T> List<T> findByConditions(String queryString, List<Object> params, String[] nodeIds) {
		NodeMgrFacade nodeMgrFacade = (NodeMgrFacade)SpringContextServlet.springCtx.getBean("nodeMgrFacade"); 
		List<ReportDispatchModel> reportDispatchModels = new ArrayList<ReportDispatchModel>();
		int len = nodeIds.length;
		for (int i = 0; i < len; i++) {
			ReportDispatchModel reportDispatchModel = new ReportDispatchModel();
			reportDispatchModel.setNodeId(nodeIds[i]);
			reportDispatchModel.setNodeMgrFacade(nodeMgrFacade);
			reportDispatchModel.setCmd(MessageDefinition.CMD_REPORT_GET_RESULT);
			Map<String, Object> map = reportDispatchModel.getMap();
			map.put("sql", queryString); 
			map.put("sqlParam", params); 
			map.put("flag", "sql"); 
			reportDispatchModels.add(reportDispatchModel);
		}
		return reportDispatch(reportDispatchModels);
	}

	@Override
	public <T> Map<Object,List<T>> findDataByConditions(Object conditionsObj) {
		ReportQueryConditions queryConditions=(ReportQueryConditions)conditionsObj;
		Map<Object,List<T>> resultMap=null;
		if (null == conditionsObj) {
			return resultMap;
		}
		Map<String, Object> queryMap=findDetailSubTheme(queryConditions.getParentSubId());
		String queryString=getQueryString(queryMap,queryConditions);
		List<Object> params=queryConditionsFormat.assemblingQueryParams(queryMap, queryConditions);
		List result=findByConditions(queryString, params, queryConditions.getNodeIds());
		if (null != queryMap && null !=result) {
			
			resultMap =new HashMap<Object, List<T>>();
			resultMap.put(queryMap, result);//.get("dataStructureDesc")
		}
		/**
		 *  当选择的设备类型是全部设备的时候处理方法,暂时只支持web端展示
		 *  --------------------- start
		 */
		if (! "NO_RE_GROUP".equals(queryMap.get("special"))) {
			boolean allRoleDvc="ALL_ROLE_ADDRESS".equals(queryConditions.getDvcAddress())
					|| "ONLY_BY_DVCTYPE".equals(queryConditions.getDvcAddress());
			Map paraMap=queryConditions.getParamMap() ;
			if(null != paraMap && paraMap.get("RESOURCE_ID")!=null 
					&& paraMap.get("RESOURCE_ID") instanceof Object[]){
				if (allRoleDvc && null != resultMap ) {
					queryMap.put("needReGroup", "true");
					String querySql=getQueryString(queryMap,queryConditions);
					twiceQuery(querySql,params,queryConditions,resultMap);
				}
			}
			
		}
		
		/**
		 * ---------------------end
		 */
		
		//如需 数据格式化处理start
		for (Entry<Object, List<T>>  entry : resultMap.entrySet()) {
			Map keyMap=(Map)entry.getKey();
			List value=entry.getValue();
			Map<String, Object> structMap=ResultOperatorUtils.datStructure((String)keyMap.get("dataStructureDesc")); 
			if (null !=structMap.get("formats")) { 
				queryResultFormat.preprocess(value, structMap);
			}
		}
		//数据格式化处理 END
		return resultMap;
	}
	
	private void twiceQuery(String querySql,List<Object> params,ReportQueryConditions conditions,Map map){
		Map<Object,List<Object>> resultMap=(Map<Object,List<Object>>)map;
		
		Map keyMap=null;
		List<Object> value=null;
		for (Map.Entry<Object, List<Object>>  entry : resultMap.entrySet()) {
			keyMap=(Map)entry.getKey();
			value=entry.getValue();
			if (null == keyMap || 0 == value.size()) {
				return;
			}
		}
		List<Map<String, Object>> valueMaps=(List<Map<String, Object>>)value.get(0);
		if(null == valueMaps || valueMaps.size()==0)return;
		int showNo=Math.min(conditions.getTopn(), valueMaps.size());
		if (showNo<1) {
			return;
		}
		Object paramValue=conditions.getParamMap().get("RESOURCE_ID");
		if (null ==paramValue || ! (paramValue instanceof Object[]) ) {
			return;
		}
		Object[]objects=(Object[])paramValue;
		if (objects.length<2){
			return;
		}
		String dataStructureDesc=(String)keyMap.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		
		StringBuffer addBuffer=new StringBuffer(" AND (");
		List<Object> addParams=new ArrayList<Object>();
		for (int i = 0; i < showNo; i++) {
			
			Map<String, Object> valMap=valueMaps.get(i);
			for (int j = 0; j < categorysName.length; j++) {
				String category=categorysName[j];
				if ("START_TIME".equalsIgnoreCase(category)) {
					continue;
				}
				Object val=valMap.get(category);
				if (!GlobalUtil.isNullOrEmpty(val)) {
					
				}
				addBuffer.append(category).append(" = ?");
				addParams.add(val);
				if (j !=categorysName.length-1) {
					addBuffer.append(" AND ");
				}
			}
			if (i != showNo-1) {
				addBuffer.append(" OR ");
			}
		}
		addBuffer.append(")");
		String addSql="";
		if (addBuffer.length()>7 && addParams.size()>0) {
			addSql=addBuffer.toString();
		}
		querySql=injectSampleSql(querySql, params, addSql, addParams);
		List require=findByConditions(querySql, params, conditions.getNodeIds());
		resultMap.clear();
		resultMap.put(keyMap, require);
	}
	
	private String injectSampleSql(String querySql,List<Object> params,String inject,List<Object> injectParams){
		
		int last=querySql.lastIndexOf("?");
		if (last == querySql.length()-1) {
			querySql=querySql+inject;
		}else {
			querySql=querySql.substring(0,last+1)+inject+querySql.substring(last+1);
		}
		
		params.addAll(injectParams);
		
		return querySql;
	}
	
	private String getQueryString(Map<String, Object> queryMap,ReportQueryConditions queryConditions){
		String queryString=queryConditionsFormat.assemblingQueryString(queryMap,queryConditions);
		PageVo pageable = new PageVo(queryConditions.getPageIndex(),queryConditions.getPageSize());
		queryString=queryConditionsFormat.changeQueryString(queryString,QueryUtil.stringTimeToLong(queryConditions.getStime()),pageable);
		return queryString;
	}
	
	@Deprecated
	@Override
	public <T> List<T> findByConditions(String queryString, List<Object> params,
			String[] nodeIds, PageVo pageable) {
		if (0>queryString.indexOf("LIMIT")) { 
			queryString=queryConditionsFormat.changeQueryString(queryString, null, pageable);
		}
		return findByConditions(queryString,params,nodeIds);
	}
	
	public static List reportDispatch(List<ReportDispatchModel> reportDispatchModels){
		List result=new ArrayList();
		try {
			ThreadPoolExecuteDispatchUtil<ReportDispatchModel> threadPoolExecuteDispatchUtil=new ThreadPoolExecuteDispatchUtil<ReportDispatchModel>(reportDispatchModels);
			ThreadPoolExecutor threadPoolExecutor=(ThreadPoolExecutor)SpringContextServlet.springCtx.getBean("commondDispatchThreadPool");
			threadPoolExecuteDispatchUtil.setThreadPool(threadPoolExecutor);
			threadPoolExecuteDispatchUtil.execute();
			while(true){
				Thread.sleep(100);
				boolean allComplete=true;
				for (ReportDispatchModel rdm : reportDispatchModels) {
					if(!rdm.isQueryComplete()){
						allComplete=false;
						break;
					}
				}
				if(allComplete){
					break;
				}
			}
			for (ReportDispatchModel reportDispatchModel : reportDispatchModels) {
				result.add(reportDispatchModel.getList());
			}
		} catch (Exception e) {
			 log.error(e.getMessage());
		}
		return result;
	}

	@Override
	public <T> Map<Object, List<T>> findAllDataByConditions(Object conditionsObj) {
		ReportQueryConditions queryConditions=(ReportQueryConditions)conditionsObj;
		Map<Object,List<T>> resultMap=null;
		if (null == conditionsObj) {
			return resultMap;
		}
		resultMap =new HashMap<Object, List<T>>();
		Integer[]parentIds=queryConditions.getParentIds();
		for (Integer integer : parentIds) {
			List<Map<String, Object>> queryMaps=findDetailSubThemeList(integer);
			for (Map<String, Object> queryMap : queryMaps) {
				String queryString=getQueryString(queryMap,queryConditions);
				List<Object> params=queryConditionsFormat.assemblingQueryParams(queryMap, queryConditions);
				List result=findByConditions(queryString, params, queryConditions.getNodeIds());
				if (null != queryMap && null !=result) {
					resultMap.put(queryMap, result);//.get("dataStructureDesc")
				}
			}
			
		}
		
		return resultMap;
	}

	private List<Object> arrayToList(Object... params){
		List<Object>paramList=null;
		if (null == params || params.length==0) {
			return paramList;
		}
		paramList=new ArrayList<Object>();
		for (Object object : params) {
			if (null!=object) {
				paramList.add(object);
			}
		}
		return paramList;
	}

	@Override
	public List<Map> findResultPutInDataStructureDescByConditions(Object conditionsObj) {
		ReportQueryConditions queryConditions=(ReportQueryConditions)conditionsObj;
		List<Map> resultList=null;
		if (null == conditionsObj) {
			return resultList;
		}
		resultList=new ArrayList<Map>();
		Integer[]parentIds=queryConditions.getParentIds();
		for (Integer integer : parentIds) {
			List<Map<String, Object>> queryMaps=findDetailSubThemeList(integer);
			for (Map<String, Object> queryMap : queryMaps) {
				QueryUtil.aliasMapFiledValue(queryConditions.getSecurityObjectType(), queryMap);

				String queryString=getQueryString(queryMap,queryConditions);
				List<Object> params=queryConditionsFormat.assemblingQueryParams(queryMap, queryConditions);
				List result=findByConditions(queryString, params, queryConditions.getNodeIds());
				//如需 数据格式化处理start
				Map<String, Object> structMap=ResultOperatorUtils.datStructure((String)queryMap.get("dataStructureDesc")); 
				if (null !=structMap.get("formats")) { 
					queryResultFormat.preprocess(result, structMap);
				}
				//数据格式化处理 END
				if (null != queryMap) {
					queryMap.put(QueryUtil.QUERY_CONDITIONS_OBJ, queryConditions);
					queryMap.put(QueryUtil.RESULT_DATA, result);
					resultList.add(queryMap);
				}
			}
			
		}
		
		return resultList;
	}

	@Override
	public List<String> findTableNameList(Integer parentId) {
		List<Map<String,Object>> maps=reportNewDao.findTableNameList(parentId);
		if (maps == null || maps.size()==0) 
			maps = ResourceContainer.getDetailParentSubs(parentId);
		List<String> result=null;
		if (maps == null || maps.size()==0){
			return result;
		}
		result=new ArrayList<String>(maps.size());
		for (Map<String,Object> map : maps) {
			Object tableObj=map.get("tableName");
			if (null != tableObj && !result.contains(tableObj.toString())) {
				result.add(tableObj.toString());
			}
		}
		
		return result;
	}

	@Override
	public final List<String> findTableNameListByType(String securityObjectType) {
		List<Map<String,Object>> parent=findParentTheme(securityObjectType);
		List<String>result=null;
		if (null == parent || parent.size()==0) {
			return result;
		}
		result=new ArrayList<String>();
		for (Map<String,Object> pMap : parent) {
			Object pid=pMap.get("id");
			if (null !=pid) {
				List<String> tabs=findTableNameList(Integer.valueOf(pid.toString()));
				if (null !=tabs) {
					for (String string : tabs) {
						if (!result.contains(string)) {
							result.add(string);
						}
					}
				}
			}
		}
		return result;
	}
	
	private void reSortListByName(List<Map<String,Object>> list){
		if (null == list || list.size() == 0) 
			return;
		for (int i = 0,len=list.size(); i < len; i++) {
			Map<String, Object>map=list.get(i);
			if(map.get("reportName").toString().indexOf("概要") > -1){
				list.remove(i);
				list.add(0, map);
			}else if(map.get("reportName").toString().indexOf("其它") > -1
					|| map.get("reportName").toString().indexOf("其他") > -1){
				list.remove(i);
				list.add(map);
			}
		}
	}
	public static void main(String[] args) {
//		ReportQueryMySqlImpl query=new ReportQueryMySqlImpl();
//		String yString="SELECT START_TIME,SUM(CASE WHEN PRIORITY=4 THEN OPCOUNT END) AS OPCOUNT4,SUM(CASE WHEN PRIORITY=3 THEN OPCOUNT END) AS OPCOUNT3 FROM RISK_HOUR WHERE 1=1 AND RESOURCE_ID = ?  AND START_TIME >= ? AND START_TIME<= ? AND PRIORITY>=3";
//		String ySt="SELECT START_TIME,SUM(CASE WHEN PRIORITY=4 THEN OPCOUNT END) AS OPCOUNT4,SUM(CASE WHEN PRIORITY=3 THEN OPCOUNT END) AS OPCOUNT3 FROM RISK_HOUR WHERE 1=1 AND RESOURCE_ID = ?  AND START_TIME >= ? AND START_TIME<= ?";
//		String sql=query.injectSampleSql(yString, new ArrayList<Object>(), "hahahahah", new ArrayList<Object>());
//		String sql1=query.injectSampleSql(ySt, new ArrayList<Object>(), "hahahahah", new ArrayList<Object>());
//
//		System.out.println(sql);
//		System.out.println(sql1);
		String reportName="访客{SRC_USER_NAME}报表";//{DEST_USER_NAME}报表
		int leftco=reportName.indexOf("{");
		int rightco=reportName.indexOf("}",leftco);
		if (-1 != leftco && -1 != rightco ) {
			String needRepalce=reportName.substring(leftco+1,rightco);
			
			System.out.println(reportName.substring(0,leftco));
			System.out.println(needRepalce);
			System.out.println(reportName.substring(rightco+1));
		}
	}
}
