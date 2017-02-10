package com.topsec.tsm.sim.newreport.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;

/**
 * @ClassName: QueryByResourseMysqlFormatImpl
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2016年5月31日下午12:22:51
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class QueryByResourceMysqlFormatImpl extends
		QueryConditionsMySqlFormatImpl implements QueryConditionsFormat {
	private static final String UNIT_RESOURCE=" AND RESOURCE_ID= ? ";
	private static final String RESOURCE_ID="RESOURCE_ID";
	private static final String RESOURCE_ID_QUERY="WHERE 1=1 AND RESOURCE_ID= ?"+START_END_TIME;
	
	@Override
	public String assemblingQueryString(Map<String, Object> queryMap,Object queryConditions) {
		
		StringBuffer sqlbBuffer=new StringBuffer();
		if (queryMap.containsKey("isUnion")) {
			sqlbBuffer.append(foundCondition((String)queryMap.get("sql"),queryConditions,false)).append(" ");
		}else {
			sqlbBuffer.append(queryMap.get("sql")).append(" ")
			.append(foundCondition((String)queryMap.get("allPurposeCondition"),queryConditions,true)).append(" ");
		}
		
		sqlbBuffer.append(foundCondition((String)queryMap.get("specialCondition"),queryConditions,true)).append(" ")
		.append(foundCondition((String)queryMap.get("linkCondition"),queryConditions,true)).append(" ")
		.append(queryMap.get("groupCondition"));
		if ("true".equals(queryMap.get("needReGroup"))) {
			sqlbBuffer.append(",").append(RESOURCE_ID);
		}
		sqlbBuffer.append(" ").append(queryMap.get("sortCondition"));
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
	private String foundCondition(String condition,boolean toUpperCase){
		if (GlobalUtil.isNullOrEmpty(condition)) {
			return condition;
		}
		if ("ONLY_BY_TYPE".equalsIgnoreCase(condition)
				|| "TYPE_AND_IP".equalsIgnoreCase(condition)) {
			return RESOURCE_ID_QUERY;
		}
		return toUpperCase?condition.toUpperCase():condition;
	}
	
	private String foundCondition(String condition,Object object,boolean toUpperCase){
		condition =foundCondition(condition,toUpperCase);
		if (GlobalUtil.isNullOrEmpty(condition)) {
			return "";
		}
		ReportQueryConditions queryConditions=(ReportQueryConditions)object;
		Map<String, Object> paramsMap=queryConditions.getParamMap();
		condition=formatConditionOnlyType(condition,queryConditions);
		if (null !=paramsMap && 0<paramsMap.size()) {
			boolean allRoleDvc="ALL_ROLE_ADDRESS".equals(queryConditions.getDvcAddress())
					|| "ONLY_BY_DVCTYPE".equals(queryConditions.getDvcAddress());
			for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
				String property=entry.getKey();
				if (! allRoleDvc && RESOURCE_ID.equals(property) ) {
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
		
		if ("ONLY_BY_DVCTYPE".equals((queryConditions.getDvcAddress()))) {
			
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
		
		if ("ONLY_BY_TYPE".equalsIgnoreCase(qconString)
				||"TYPE_AND_IP".equalsIgnoreCase(qconString)) {
			
			manyParamsInit(UNIT_RESOURCE,queryConditions,list);
			
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
		boolean allRoleDvc="ALL_ROLE_ADDRESS".equals(queryConditions.getDvcAddress())
				||"ONLY_BY_DVCTYPE".equals((queryConditions.getDvcAddress()));
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
			}else if (RESOURCE_ID.equals(prop) ) {
				if (!allRoleDvc) {
					paramValue=queryConditions.getResourceId();
				}else {
					paramValue=paramsMap.get(RESOURCE_ID);
				}
				
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
}
