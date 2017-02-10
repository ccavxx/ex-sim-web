package com.topsec.tsm.sim.log.bean;

/**
 * 事后报表任务
 * @author hp
 *
 */
public class LogReportTask{
	/**
	 * 任务状态
	 */

	private Integer taskId ;//任务id
	private String taskName ;//任务名称
	private int taskState ;//任务状状态
	private String startTime ;//任务开始时间
	private String endTime ;//任务结束时间
	private Integer diagram ;//结果类型
	private String filedname;//获取属性的英文名称，组成字符串
	private String host;            //主机地址
	private String deviceType;  //设备类型
	private String nodeId;		//节点ID
	private int pageNo=0;			//页码
	private int pageSize=0;		//每页显示的条数
	private String conditionName; //列名
	private String operator;			//操作符
	private String queryContent;	//查询内容
	private String queryStartDate;	//开始时间
	private String queryEndDate;	//结束时间
	private String group;				//列集
	private String queryType;	//列对应的数据类型
	private String functionName;//统计方式（求和、求平均、统计记录）
	private String functionField; //统计函数字段
	private String groupColumn;//分组列
	private String orderbyColumm;//排序列
	private String groupTopFields ;
	private Integer topNumber;//用来显示前几条日志
	private String taskOperator;
	private String searchCondition;//用来存储日志查询条件
	private String interval;//用来存储时间间隔
	private String categoryAxisField ;
	private String title;//日志查询条件标题
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSearchCondition() {
		return searchCondition;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public void setSearchCondition(String searchCondition) {
		this.searchCondition = searchCondition;
	}

	public String getTaskOperator() {
		return taskOperator;
	}

	public void setTaskOperator(String taskOperator) {
		this.taskOperator = taskOperator;
	}

	public Integer getTopNumber() {
		return topNumber;
	}

	public void setTopNumber(Integer topNumber) {
		this.topNumber = topNumber;
	}

	public String getOrderbyColumm() {
		return orderbyColumm;
	}

	public void setOrderbyColumm(String orderbyColumm) {
		this.orderbyColumm = orderbyColumm;
	}
	private String filextension;//导出的格式
	public String getGroupColumn() {
		return groupColumn;
	}

	public void setGroupColumn(String groupColumn) {
		this.groupColumn = groupColumn;
	}
	public String getFilextension() {
		return filextension;
	}

	public void setFilextension(String filextension) {
		this.filextension = filextension;
	}

	public String getFileformat() {
		return fileformat;
	}

	public void setFileformat(String fileformat) {
		this.fileformat = fileformat;
	}
	private String fileformat;//导出方式
	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getFunctionField() {
		return functionField;
	}

	public void setFunctionField(String functionField) {
		this.functionField = functionField;
	}

	public LogReportTask() {
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public int getTaskState() {
		return taskState;
	}
	public void setTaskState(int taskState) {
		this.taskState = taskState;
	}
	public Integer getDiagram() {
		return diagram;
	}
	public void setDiagram(Integer diagram) {
		this.diagram = diagram;
	}
	public String getFiledname() {
		return filedname;
	}
	public void setFiledname(String filedname) {
		this.filedname = filedname;
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
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public String getConditionName() {
		return conditionName;
	}
	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getQueryContent() {
		return queryContent;
	}
	public void setQueryContent(String queryContent) {
		this.queryContent = queryContent;
	}
	public String getQueryStartDate() {
		return queryStartDate;
	}
	public void setQueryStartDate(String queryStartDate) {
		this.queryStartDate = queryStartDate;
	}
	public String getQueryEndDate() {
		return queryEndDate;
	}
	public void setQueryEndDate(String queryEndDate) {
		this.queryEndDate = queryEndDate;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public String getGroupTopFields() {
		return groupTopFields;
	}

	public void setGroupTopFields(String groupTopFields) {
		this.groupTopFields = groupTopFields;
	}

	public String getCategoryAxisField() {
		return categoryAxisField;
	}

	public void setCategoryAxisField(String categoryAxisField) {
		this.categoryAxisField = categoryAxisField;
	}
	
}