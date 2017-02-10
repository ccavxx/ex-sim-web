package com.topsec.tsm.sim.report.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.bean.base.BaseBean;

public class ReportBean extends BaseBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1900000L;
	private String subrptid; // 子报表
	private String mstrptid;// 主报表
	private String dvctype; // 设备类型 防火墙
	private String dvctypeCnName; // 设备类型 防火墙
	private String[] talCategory; // chart 下探参数2
	private String talCategories;//chart 下探参数字符串
	private String msttype;// 主报表类型
	private String rickniu;
	private String dvcaddress;
	private String rptType; // 报表类型
	private String rptUser; // 制作人
	private String rptIp; // 报表ip
	private String showTimeInput;// 报表类型
	private Map<String,String> quarters;	// 季度
	private String qselected;
	private Map<String,String> quarter2s;// 季度
	private String quarterSelected;
	private String talEndTime; // 开始时间
	private String talStartTime; // 结束时间
	private List<Map> titles;
	private String selected;
	private String talTop;	// top N
	private Map<String,String> tops;
	private Map<String,String> dtypes;// 日期列表
	private String dtypeSelected;
	private Map<String,String> pagesizes;	// 分页
	private String pagesize;
	private boolean isRptList; // 报表list
	private String viewItem;// 前台显示项
	private String id;// 响应id
	private String name;// 响应名称
	private String creater;// 创建者
	private String type;// 类型
	private String status;// 状态
	private String cfgType;// 配置
	private String editType;// 编辑类型
	private String subType;// 编辑类型
	private String reportMailList;
	private String nextExeTime;
	private int failedResultCount;
	private int successResultCount;
	private Boolean update;
	private Boolean delete;
	private Boolean start;
	private Boolean showResult;
	private String[] nodeId;
	private String outNodeIdJsonString;
	private String cfgKey;// 配置
	private String createTime;
	private String desc;
	private String config;
	private String ScheduleExpression;
	private String onlyByDvctype;
	private String reportSys;
	private String reportType;
	private String reportTopn;
	private String reportFileType;
	private String reportUser;
	private String scheduleType;
	private String rootId;//业务报表根id
	private String assGroupNodeId;//设备组Id
	private String topoId;//拓扑Id
	private String nodeLevel;//节点级别
	private String nodeType;//日志源类型
	public ReportBean(){
		super();
	}
	public String getReportSys() {
		return reportSys;
	}

	public void setReportSys(String reportSys) {
		this.reportSys = reportSys;
	}

	public String getRootId() {
		return rootId;
	}
	public void setRootId(String rootId) {
		this.rootId = rootId;
	}
	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getReportTopn() {
		return reportTopn;
	}

	public void setReportTopn(String reportTopn) {
		this.reportTopn = reportTopn;
	}

	public String getReportFileType() {
		return reportFileType;
	}

	public void setReportFileType(String reportFileType) {
		this.reportFileType = reportFileType;
	}

	public String getReportUser() {
		return reportUser;
	}

	public void setReportUser(String reportUser) {
		this.reportUser = reportUser;
	}

	public String getReportMailList() {
		return reportMailList;
	}

	public void setReportMailList(String reportMailList) {
		this.reportMailList = reportMailList;
	}

	
	public int getFailedResultCount() {
		return failedResultCount;
	}

	public void setFailedResultCount(int failedResultCount) {
		this.failedResultCount = failedResultCount;
	}

	public int getSuccessResultCount() {
		return successResultCount;
	}

	public void setSuccessResultCount(int successResultCount) {
		this.successResultCount = successResultCount;
	}
	
	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCfgType() {
		return cfgType;
	}

	public void setCfgType(String cfgType) {
		this.cfgType = cfgType;
	}

	public String getEditType() {
		return editType;
	}

	public void setEditType(String editType) {
		this.editType = editType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreater() {
		return creater;
	}

	public void setCreater(String creater) {
		this.creater = creater;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getViewItem() {
		return viewItem;
	}

	public void setViewItem(String viewItem) {
		this.viewItem = viewItem;
	}

	public String getSelected() {
		return selected;
	}

	public void setSelected(String selected) {
		this.selected = selected;
	}

	public String getDvcaddress() {
		return dvcaddress;
	}

	public void setDvcaddress(String dvcaddress) {
		this.dvcaddress = dvcaddress;
	}

	public String getTalTop() {
		return talTop;
	}

	public void setTalTop(String talTop) {
		this.talTop = talTop;
	}

	public String getTalEndTime() {

		return talEndTime;
	}

	public void setTalEndTime(String talEndTime) {

		this.talEndTime = talEndTime;
	}

	public String getTalStartTime() {

		return talStartTime;
	}

	public void setTalStartTime(String talStartTime) {
		this.talStartTime = talStartTime;
	}
	public String getPagesize() {
		return pagesize;
	}

	public void setPagesize(String pagesize) {
		this.pagesize = pagesize;
	}

	public String getSubrptid() {
		return subrptid;
	}

	public void setSubrptid(String subrptid) {
		this.subrptid = subrptid;
	}

	public String getMstrptid() {
		return mstrptid;
	}

	public void setMstrptid(String mstrptid) {
		this.mstrptid = mstrptid;
	}

	public String getDvctype() {
		return dvctype;
	}

	public void setDvctype(String dvctype) {
		this.dvctype = dvctype;
	}

	public String[] getTalCategory() {
		return talCategory;
	}

	public void setTalCategory(String[] talCategory) {
		this.talCategory = talCategory;
	}

	public String getTalCategoryString(){
		return StringUtil.join(talCategory) ;
	}
	
	public String getRptType() {
		return rptType;
	}

	public void setRptType(String rptType) {
		this.rptType = rptType;
	}

	public String getRptUser() {
		return rptUser;
	}

	public void setRptUser(String rptUser) {
		this.rptUser = rptUser;
	}

	public String getRptIp() {
		return rptIp;
	}

	public void setRptIp(String rptIp) {
		this.rptIp = rptIp;
	}

	public String getShowTimeInput() {
		return showTimeInput;
	}

	public void setShowTimeInput(String showTimeInput) {
		this.showTimeInput = showTimeInput;
	}

	public String getQselected() {
		return qselected;
	}

	public void setQselected(String qselected) {
		this.qselected = qselected;
	}

	public boolean isRptList() {
		return isRptList;
	}

	public void setRptList(boolean isRptList) {
		this.isRptList = isRptList;
	}

	public String getRickniu() {
		return rickniu;
	}

	public void setRickniu(String rickniu) {
		this.rickniu = rickniu;
	}

	public String getMsttype() {
		return msttype;
	}

	public void setMsttype(String msttype) {
		this.msttype = msttype;
	}

	public String[] getNodeId() {
		return nodeId;
	}

	public void setNodeId(String[] nodeId) {
		this.nodeId = nodeId;
	}
	

	public String getOnlyByDvctype() {
		return onlyByDvctype;
	}

	public void setOnlyByDvctype(String onlyByDvctype) {
		this.onlyByDvctype = onlyByDvctype;
	}
	
	public Boolean getUpdate() {
		return update;
	}

	public void setUpdate(Boolean update) {
		this.update = update;
	}

	public Boolean getDelete() {
		return delete;
	}

	public void setDelete(Boolean delete) {
		this.delete = delete;
	}

	public Boolean getStart() {
		return start;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public Boolean getShowResult() {
		return showResult;
	}

	public void setShowResult(Boolean showResult) {
		this.showResult = showResult;
	}

	public String getQuarterSelected() {
		return quarterSelected;
	}

	public Map<String, String> getQuarters() {
		return quarters;
	}

	public void setQuarters(Map<String, String> quarters) {
		this.quarters = quarters;
	}

	public Map<String, String> getQuarter2s() {
		return quarter2s;
	}

	public void setQuarter2s(Map<String, String> quarter2s) {
		this.quarter2s = quarter2s;
	}


	public List<Map> getTitles() {
		return titles;
	}

	public void setTitles(List<Map> titles) {
		this.titles = titles;
	}

	public Map<String, String> getTops() {
		return tops;
	}

	public String getTalCategories() {
		return talCategories;
	}

	public void setTalCategories(String talCategories) {
		this.talCategories = talCategories;
	}

	public void setTops(Map<String, String> tops) {
		this.tops = tops;
	}

	public Map<String, String> getDtypes() {
		return dtypes;
	}

	public void setDtypes(Map<String, String> dtypes) {
		this.dtypes = dtypes;
	}

	public String getDtypeSelected() {
		return dtypeSelected;
	}

	public void setDtypeSelected(String dtypeSelected) {
		this.dtypeSelected = dtypeSelected;
	}

	public Map<String, String> getPagesizes() {
		return pagesizes;
	}

	public void setPagesizes(Map<String, String> pagesizes) {
		this.pagesizes = pagesizes;
	}

	public void setQuarterSelected(String quarterSelected) {
		this.quarterSelected = quarterSelected;
	}

	public String getOutNodeIdJsonString() {
		return outNodeIdJsonString;
	}

	public void setOutNodeIdJsonString(String outNodeIdJsonString) {
		this.outNodeIdJsonString = outNodeIdJsonString;
	}

	public String getCfgKey() {
		return cfgKey;
	}

	public void setCfgKey(String cfgKey) {
		this.cfgKey = cfgKey;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getScheduleExpression() {
		return ScheduleExpression;
	}

	public void setScheduleExpression(String scheduleExpression) {
		ScheduleExpression = scheduleExpression;
	}

	public String getNextExeTime() {
		return nextExeTime;
	}

	public void setNextExeTime(String nextExeTime) {
		this.nextExeTime = nextExeTime;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getDvctypeCnName() {
		return dvctypeCnName;
	}

	public void setDvctypeCnName(String dvctypeCnName) {
		this.dvctypeCnName = dvctypeCnName;
	}
	public String getAssGroupNodeId() {
		return assGroupNodeId;
	}
	public void setAssGroupNodeId(String assGroupNodeId) {
		this.assGroupNodeId = assGroupNodeId;
	}
	public String getTopoId() {
		return topoId;
	}
	public void setTopoId(String topoId) {
		this.topoId = topoId;
	}
	public String getNodeLevel() {
		return nodeLevel;
	}
	public void setNodeLevel(String nodeLevel) {
		this.nodeLevel = nodeLevel;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	
}
