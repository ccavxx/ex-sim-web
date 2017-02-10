package com.topsec.tsm.sim.log.bean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.auth.util.SID;

/**
 * 用于日志查询模块
 * @author zhou_xiaohu@topsec.com.cn
 *	@version 1.0
 * @createTime 2014-03-14
 */
public class LogSearchObject implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String host="";            //主机地址
	private String deviceType;  //设备类型
	private String nodeId="";		//节点ID
	private int pageNo=0;			//页码
	private int pageSize=20;		//每页显示的条数
	private String conditionName=""; //列名
	private String operator="";			//操作符
	private String queryContent="";	//查询内容
	private String queryStartDate;	//开始时间
	private String queryEndDate;	//结束时间
//	private boolean quickSearch;	//
	private boolean cancel = false;//退出
	private String group;				//列集
	private String queryType="";			//列对应的数据类型
	private int isFormate;//是否为格式化日志
	private int seq ;
	private String traceField ;//跟踪字段
	private String value ;//值
	private boolean onlyWanSrc ;//是否是统计外网地址数据
	private List<String> traceGroupFields ;//根据分组字段
	
	public int getIsFormate() {
		return isFormate;
	}
	private String statColumn;

	public String getStatColumn() {
		return statColumn;
	}
	public void setStatColumn(String statColumn) {
		this.statColumn = statColumn;
	}
	public void setIsFormate(int isFormate) {
		this.isFormate = isFormate;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public int getPageNo() {
		if(pageNo == 0 )
			return 1;
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		if(pageSize == 0 )
			return 20;
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public String[] getConditionName() {
		return conditionName.split(",");
	}
	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}
	public String[] getOperator() {
		return operator.split(",");
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String[] getQueryContent() {
		if(queryContent == null && conditionName != null){
			String[] condition =  this.getConditionName();
			queryContent ="";
			for(int i=0;i<condition.length;i++){
				queryContent +=",";
			}
		}
		return queryContent.replace("'","").split(",");
	}
	public void setQueryContent(String queryContent) {
		this.queryContent = queryContent;
	}
	public String getQueryStartDate() {
//		if(queryStartDate == null || "".equals(queryStartDate)){
//			Date date = new Date();
//			date.setTime(new Long("1396333153553")-100*60*60*1000);
//			queryStartDate = StringUtil.dateToString(date,"yyyy-MM-dd HH:mm:ss");
//		}
		return queryStartDate;
	}
	public void setQueryStartDate(String queryStartDate) {
		this.queryStartDate = queryStartDate;
	}
	public String getQueryEndDate() {
//		if(queryEndDate == null || "".equals(queryEndDate)){
//			Date date = new Date();
//			date.setTime(new Long("1396333153553")+60*60*1000);
//			queryEndDate = StringUtil.dateToString(date,"yyyy-MM-dd HH:mm:ss");
//		}
		return queryEndDate;
	}
	public void setQueryEndDate(String queryEndDate) {
		this.queryEndDate = queryEndDate;
	}
	public boolean isCancel() {
		return cancel;
	}
	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}
	public String getGroup() {
		if(group == null || "".equals(group))
			group=  IndexTemplateUtil.getInstance().getGroups(deviceType).get(0);
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String[] getQueryType() {
		return queryType.split(",");
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	public String getTraceField() {
		return traceField;
	}
	public String getValue() {
		return value;
	}
	public void setTraceField(String traceField) {
		this.traceField = traceField;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isOnlyWanSrc() {
		return onlyWanSrc;
	}
	public void setOnlyWanSrc(boolean onlyWanSrc) {
		this.onlyWanSrc = onlyWanSrc;
	}
	
	public void setTraceGroupFields(List<String> traceGroupFields) {
		this.traceGroupFields = traceGroupFields;
	}
	
	public Map<String,String> getTraceGroupFields(){
		return StringUtil.splitAsMap("=", true,traceGroupFields) ;
	}
	
	public SearchObject asSeachObject(SID sid){
		SearchObject searchObject = new SearchObject() ;
		fillSearchObject(searchObject) ;
		return searchObject ;
	}
	
	public void fillSearchObject(SearchObject searchObject){
		searchObject.setStart(StringUtil.toDateL(this.getQueryStartDate()));
		searchObject.setEnd(StringUtil.toDateL(this.getQueryEndDate()));
		searchObject.setHost(this.getHost());
		searchObject.setType(this.getDeviceType());
		searchObject.setPage(this.getPageNo());
		searchObject.setPerPage(this.getPageSize());
		searchObject.setConditionNames(this.getConditionName());
		searchObject.setOperators(this.getOperator());
		searchObject.setQueryContents(this.getQueryContent());
		searchObject.setQueryTypes( this.getQueryType());
		searchObject.setGroup(this.getGroup());
		searchObject.setCancel(this.isCancel());
		searchObject.setStatColumns(Arrays.asList(this.getStatColumn())) ;
	}
}
