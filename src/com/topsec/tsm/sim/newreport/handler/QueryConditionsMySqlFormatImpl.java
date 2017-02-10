package com.topsec.tsm.sim.newreport.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.newreport.bean.PageVo;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;
import com.topsec.tsm.sim.newreport.model.ReportQuery;

/**
 * @ClassName: QueryConditionsFormatImpl
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月13日下午3:02:17
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class QueryConditionsMySqlFormatImpl implements QueryConditionsFormat {
	private static final String IPADDRESS_CONDITION=" AND DVC_ADDRESS = ? ";
	protected static final String START_END_TIME=" AND START_TIME >= ? AND START_TIME< ? ";
	private static final String ONLY_BY_TYPE="WHERE DVC_TYPE= ?"+START_END_TIME;
	private static final String TYPE_AND_IP="WHERE DVC_TYPE= ?"+IPADDRESS_CONDITION+START_END_TIME;
	
	@Override
	public String changeQueryString(String queryString,Long stime,PageVo pageable) {
		queryString=changeQueryString(queryString,stime);
		queryString=changeQueryString(queryString,pageable);
		return queryString;
	}

	@Override
	public Long executeStartTime(Long eTime, String cycleReportTimeType) {
		Long stime=null;
		if ("DAY".equalsIgnoreCase(cycleReportTimeType)) {
			stime=eTime-24L*3600*1000;
		}else if ("WEEK".equalsIgnoreCase(cycleReportTimeType)) {
			stime=eTime-24L*3600*1000*7;
		}else if ("MONTH".equalsIgnoreCase(cycleReportTimeType)) {
			stime=eTime-24L*3600*1000*30;
		}else if ("YEAR".equalsIgnoreCase(cycleReportTimeType)) {
			stime=eTime-24L*3600*1000*365;
		}
		return stime;
	}

	@Override
	public List<Object> queryParamsFormat(List<Object> params) {
		return null;
	}
	private String changeQueryString(String queryString,Long stime){
		if (null == stime) {
			return queryString;
		}
		queryString=queryString.toUpperCase();
		Date date=new Date();
		long nowtime=date.getTime();
		if (nowtime-stime <= 1000L*3600*24*2) {
		}else if (nowtime-stime <= 1000L*3600*24*28){
			queryString=queryString.replace("_HOUR", "_DAY");
		}else if (nowtime-stime > 1000L*3600*24*28){
			queryString=queryString.replace("_HOUR", "_MONTH");
		}
		return queryString;
	}
	private String changeQueryString(String queryString,PageVo pageable){
		if (null == pageable || pageable.getPageSize() == 0) {
			return queryString +" LIMIT 30 ";
		}
		return queryString+" LIMIT "+(pageable.getPageIndex()-1)*pageable.getPageSize()+", "+pageable.getPageSize();
	}

	@Override
	public String assemblingQueryString(Map<String, Object> queryMap,Object queryConditions) {
		
		StringBuffer sqlbBuffer=new StringBuffer();
		sqlbBuffer.append(queryMap.get("sql")).append(" ")
		.append(foundCondition((String)queryMap.get("allPurposeCondition"),queryConditions)).append(" ")
		.append(foundCondition((String)queryMap.get("specialCondition"),queryConditions)).append(" ")
		.append(foundCondition((String)queryMap.get("linkCondition"),queryConditions)).append(" ")
		.append(queryMap.get("groupCondition")).append(" ")
		.append(queryMap.get("sortCondition"));
		return sqlbBuffer.toString();
	}

	@Override
	public List<Object> assemblingQueryParams(Map<String, Object> queryMap,
			Object queryConditions) {
		List<Object> list=null;
		if (null==queryMap || 0==queryMap.size()) {
			return list;
		}
		ReportQueryConditions queryCdt=(ReportQueryConditions)queryConditions;
		String allPurposeCondition=(String)queryMap.get("allPurposeCondition");
		allPurposeCondition=formatConditionOnlyType(allPurposeCondition,queryCdt);
		list=getParams(allPurposeCondition,queryConditions);
		String specialCondition=(String)queryMap.get("specialCondition");
		List<Object> listspecial=getParams(specialCondition,queryConditions);
		String linkCondition=(String)queryMap.get("linkCondition");
		List<Object> listlink=getParams(linkCondition,queryConditions);
		if (null != list) {
			if (null != listspecial) {
				list.addAll(listspecial);
			}
			if (null != listlink) {
				list.addAll(listlink);
			}
		}else {
			if (null != listspecial) {
				list=listspecial;
				if (null != listlink) {
					list.addAll(listlink);
				}
			}else {
				list=listlink;
			}
		}
		return list;
	}

	private String foundCondition(String condition){
		if (GlobalUtil.isNullOrEmpty(condition)) {
			return condition;
		}
		if ("ONLY_BY_TYPE".equalsIgnoreCase(condition)) {
			return ONLY_BY_TYPE;
		}else if ("TYPE_AND_IP".equalsIgnoreCase(condition)) {
			return TYPE_AND_IP;
		}
		return condition.toUpperCase();
	}
	
	private String foundCondition(String condition,Object object){
		condition =foundCondition(condition);
		if (GlobalUtil.isNullOrEmpty(condition)) {
			return "";
		}
		ReportQueryConditions queryConditions=(ReportQueryConditions)object;
		Map<String, Object> paramsMap=queryConditions.getParamMap();
		condition=formatConditionOnlyType(condition,queryConditions);
		if (null !=paramsMap && 0<paramsMap.size()) {
			boolean allRoleDvc="ALL_ROLE_ADDRESS".equals(queryConditions.getDvcAddress());
			for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
				String property=entry.getKey();
				if (! allRoleDvc && "DVC_ADDRESS".equals(property) ) {
					continue;
				}
				Object paramValue=entry.getValue();
				String patternOther="(AND\\s+"+property+"\\s*=\\s*\\?)";
				Pattern ptn=Pattern.compile(patternOther) ;
				if (! ptn.matcher(condition).find()) {
					continue;
				}
				String oldString=" AND ("+property+" = ? ";
				String newReString=" OR "+property+" = ? ";
				String endsString=" ) ";
				if (null !=paramValue) {
					if (paramValue instanceof Object[]){
						Object[]objects=(Object[])paramValue;
						if (objects.length>1) {
							StringBuffer buffer=new StringBuffer(oldString);
							for (int i = 0; i < objects.length-1; i++) {
								buffer.append(newReString);
							}
							buffer.append(endsString);
							condition=condition.replaceAll(patternOther, buffer.toString());
						}
					}//其他情况不需要改变查询条件
				}
			}
			
		}
		return condition;
	}
	
	private String formatConditionOnlyType(String condition,ReportQueryConditions queryConditions){
		if (GlobalUtil.isNullOrEmpty(condition)) {
			return "";
		}
		String patternDvc="(AND\\s+DVC_ADDRESS\\s*=\\s*\\?)";
		if ("ONLY_BY_DVCTYPE".equals((queryConditions.getDvcAddress()))) {
			//.*\s+(AND\s+DVC_ADDRESS\s*=\s*\?)\s+.*
			// 正则实现 （以后有更好的方式可以 换成其他实现）
			condition=condition.replaceAll(patternDvc, "");
		}
		return condition;
	}
	 
	private List<Object> getParams(String qconString,Object	object){
		List<Object> list=null;
		if (GlobalUtil.isNullOrEmpty(qconString)) {
			return list;
		}
		list=new ArrayList<Object>();
		ReportQueryConditions queryConditions=(ReportQueryConditions)object;
		boolean allRoleDvc="ALL_ROLE_ADDRESS".equals(queryConditions.getDvcAddress());
		boolean notNeedIp="ONLY_BY_DVCTYPE".equals((queryConditions.getDvcAddress()));
		if ("ONLY_BY_TYPE".equalsIgnoreCase(qconString)) {
			list.add(queryConditions.getSecurityObjectType());
			list.add(queryConditions.getStime());
			list.add(queryConditions.getEndtime());
			return list;
		}else if ("TYPE_AND_IP".equalsIgnoreCase(qconString)) {
			list.add(queryConditions.getSecurityObjectType());
			if (allRoleDvc) {
				manyParamsInit(IPADDRESS_CONDITION,queryConditions,list);
			}else if(!notNeedIp) {
				list.add(queryConditions.getDvcAddress());
			}
			list.add(queryConditions.getStime());
			list.add(queryConditions.getEndtime());
			return list;
		}
		// AND SRC_ADDRESS = ? AND OP =?
		manyParamsInit(qconString,queryConditions,list);
		return list;
	}
	
	private void manyParamsInit(String qconString,ReportQueryConditions queryConditions,List<Object> list){
		Map<String, Object> paramsMap=queryConditions.getParamMap();
		if (null ==paramsMap || 0==paramsMap.size()){
			return;
		}
		boolean allRoleDvc="ALL_ROLE_ADDRESS".equals(queryConditions.getDvcAddress());
		String[]operatorStrings={">=","<=","!=","=",">","<"};
		qconString=qconString.toUpperCase();
		int wre=qconString.indexOf("WHERE");
		if (wre>-1) {
			qconString=qconString.substring(wre+5).trim();
		}
		int qsignPosition=qconString.indexOf("?");
		while (qsignPosition>1) {
			String conS=qconString.substring(0, qsignPosition+1).trim();
			String operatorString=null;
			int operpos=-1;
			for (int i = 0; i < operatorStrings.length; i++) {
				if (conS.indexOf(operatorStrings[i])>-1) {
					operatorString=operatorStrings[i];
					operpos=i;
					break;
				}
			}
			int andlastPo=conS.lastIndexOf("AND ");
			andlastPo=andlastPo==-1?0:andlastPo+4;
				
    		String prop=conS.substring(andlastPo,conS.lastIndexOf(operatorString)).trim();
    		Object paramValue=null;
    		if ("START_TIME".equals(prop)) {
    			if (operpos==0||operpos==4) {
    				paramValue=queryConditions.getStime();
				}else if(operpos==1||operpos==5){
					paramValue=queryConditions.getEndtime();
				}
			}else if ("DVC_TYPE".equals(prop)) {
				paramValue=queryConditions.getSecurityObjectType();
			}else if ("DVC_ADDRESS".equals(prop) && !allRoleDvc) {
				paramValue=queryConditions.getDvcAddress();
			}else {
				paramValue=paramsMap.get(prop);
			}
    		boolean isF=false;
    		if (null !=paramValue) {
				if (paramValue instanceof Object[]){
					Object[]objects=(Object[])paramValue;
					if (objects.length>1) {
						for (int i = 0; i < objects.length; i++) {
							list.add(objects[i]);
						}
						isF=true;
					}
				}
			}
    		if (!isF) {
    			list.add(paramValue);
			}
    		
    		qconString=qconString.substring(qsignPosition+1).trim();
    		qsignPosition=qconString.indexOf("?");
		}
	}

	@Override
	public Map.Entry<String, List<Object>> assemblingEntryQueryStringAndParams(
			final Map<String, Object> queryMap,final Object queryConditions) {
		Map.Entry<String, List<Object>> entry=new Map.Entry<String, List<Object>>() {
			private List<Object> value=assemblingQueryParams(queryMap,queryConditions);
			@Override
			public String getKey() {
				return assemblingQueryString(queryMap,queryConditions);
			}

			@Override
			public List<Object> getValue() {
				return value;
			}

			@Override
			public List<Object> setValue(List<Object> value) {
				this.value=value;
				return value;
			}
		};
		
		return entry;
	}
	
	@Override
	public Map<String, List<Object>> assemblingQueryStringAndParams(
			Map<String, Object> queryMap, Object queryConditions) {
		Map<String, List<Object>> map=null;
		//key
		String key = assemblingQueryString(queryMap,queryConditions);
		//value
		List<Object> params=assemblingQueryParams(queryMap,queryConditions);
		if (null != key) {
			map=new HashMap<String, List<Object>>();
			map.put(key, params);
		}
		return map;
	}
	
	@Override
	public List<Map<String, List<Object>>> assemblingQueryStringAndParams(
			Object queryConditions) {
		List<Map<String, List<Object>>> mapList=null;
		if (null == queryConditions) {
			return mapList;
		}
		ReportQueryConditions conditions=(ReportQueryConditions)queryConditions;
		ReportQuery reportQuery=(ReportQuery)SpringContextServlet.springCtx.getBean("reportQuery");
		mapList=new ArrayList<Map<String,List<Object>>>(1);
		for (Integer integer : conditions.getParentIds()) {
			Map<String, List<Object>> map=null;
			if (null != integer) {
				List<Map<String, Object>> subList=reportQuery.findSimpleSubThemes(integer);
				for (Map<String, Object> mapsub : subList) {
					Map<String, Object> queryMap=reportQuery.findDetailSubTheme((Integer)mapsub.get("parentSubId"));
					Map<String, List<Object>> tmpmap=assemblingQueryStringAndParams(queryMap, queryConditions);
					if (null != map) {
						map.putAll(tmpmap);
					}else {
						map=tmpmap;
					}
				}
			}
		}
		return mapList;
	}
}
