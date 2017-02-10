package com.topsec.tsm.sim.log.util;

import java.util.List;
import java.util.Map;
public class LogRecordList {
	
	private List<LogUtil> logUtil; //列集
	private List<Map<String,Object>> maps ;	   //结果集
	private List<LogUtil> records;     //源日志
	private String exceptionInfo; //错误信息
	private boolean finished; //
	private int type;
	private String logType;//日志类型
	private String group;//列集
	private String totalCount;//格式化之后的日志总条数
	private int seq ;
	private List<?> columns ;
	public String getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(String totalCount) {
		this.totalCount = totalCount;
	}
	private List<String> columnTypes;//每一列类型
	public List<String> getColumnTypes() {
		return columnTypes;
	}
	public void setColumnTypes(List<String> columnTypes) {
		this.columnTypes = columnTypes;
	}
	private String flag;//唯一标识
	private int lapTime;//耗时·
	
	//库中总日志条数
	private long totalLogs = 0;
	//查询结果命中总数
	private int totalRecords;
	//可显示 数
	private int displayCount;
	//超过查询结果上限
	private boolean reachLimit;
	private int searchLimit;
	private int displayLimit;
	public int getSearchLimit() {
		return searchLimit;
	}
	public void setSearchLimit(int searchLimit) {
		this.searchLimit = searchLimit;
	}
	//操作符
	private String[] operator;
	//条件
	private String[] condition;
	//查询内容
	private String[] queryConent;
	//日志源列表
	private List<Map<String, Object>>  dataSource;
	//查询列信息
	private List<Map<String,Object>> filters;
	//查询结果时间分布
	private List<Map<String, Object>> timeline; 
	
	public List<Map<String, Object>> getTimeline() {
		return timeline;
	}
	public void setTimeline(List<Map<String, Object>> timeline) {
		this.timeline = timeline;
	}
	public List<Map<String, Object>> getDataSource() {
		return dataSource;
	}
	public void setDataSource(List<Map<String, Object>> dataSource) {
		this.dataSource = dataSource;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public List<Map<String, Object>> getFilters() {
		return filters;
	}
	public void setFilters(List<Map<String, Object>> filters) {
		this.filters = filters;
	}
	public String[] getCondition() {
		return condition;
	}
	public void setCondition(String[] condition) {
		this.condition = condition;
	}
	public String[] getQueryConent() {
		return queryConent;
	}
	public void setQueryConent(String[] queryConent) {
		this.queryConent = queryConent;
	}
	
	public String[] getOperator() {
		return operator;
	}
	public void setOperator(String[] operator) {
		this.operator = operator;
	}
	public boolean isReachLimit() {
		return reachLimit;
	}
	public void setReachLimit(boolean reachLimit) {
		this.reachLimit = reachLimit;
	}
	public long getTotalLogs() {
		return totalLogs;
	}
	public void setTotalLogs(long totalLogs) {
		this.totalLogs = totalLogs;
	}
	public int getDisplayCount() {
		return displayCount;
	}
	public void setDisplayCount(int displayCount) {
		this.displayCount = displayCount;
	}
	public int getLapTime() {
		return lapTime;
	}
	public void setLapTime(int lapTime) {
		this.lapTime = lapTime;
	}
	public String getLogType() {
		return logType;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	public String getExceptionInfo() {
		return exceptionInfo;
	}
	public void setExceptionInfo(String exceptionInfo) {
		this.exceptionInfo = exceptionInfo;
	}
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	public List<LogUtil> getRecords() {
		return records;
	}
	public void setRecords(List<LogUtil> records) {
		this.records = records;
	}
	public List<LogUtil> getLogUtil() {
		return logUtil;
	}
	public void setLogUtil(List<LogUtil> logUtil) {
		this.logUtil = logUtil;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public List<Map<String,Object>> getMaps() {
		return maps;
	}
	public void setMaps(List<Map<String,Object>> maps) {
		this.maps = maps;
	}
	public List<?> getColumns() {
		return columns;
	}
	public void setColumns(List<?> columns) {
		this.columns = columns;
	}
	public int getDisplayLimit() {
		return displayLimit;
	}
	public void setDisplayLimit(int displayLimit) {
		this.displayLimit = displayLimit;
	}


}
