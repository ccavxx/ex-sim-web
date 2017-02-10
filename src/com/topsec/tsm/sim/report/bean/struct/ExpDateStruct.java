package com.topsec.tsm.sim.report.bean.struct;

import java.util.List;
/*
 *	ExpDateStruct	PDF DOC XLS报表导出内容数据集所用结构体
 */
public class ExpDateStruct {
	private String subTitle;

	private Object subChart;
	private List subTable;

	private String mstTitle;
	private String subType;// 子报表主题
	private String dvcIp;// 设备ip

	private String subTitleLable;
	private String subTableFile;

	private String sTime;
	private String eTime;
	
	private short talCategoryLevel;
	
	private int mstType;
	
	private String[] nodeId;
	
	private String onlyByDvctype;
	
	
	private String[] talCategory;
	
	
	
	public String getOnlyByDvctype() {
		return onlyByDvctype;
	}

	public void setOnlyByDvctype(String onlyByDvctype) {
		this.onlyByDvctype = onlyByDvctype;
	}
	
	public String getTitle() {
		return subTitle;
	}

	public void setTitle(String title) {
		this.subTitle = title;
	}

	public List getTable() {
		return subTable;
	}

	public void setTable(List table) {
		this.subTable = table;
	}

	public String getTitleLable() {
		return subTitleLable;
	}

	public void setTitleLable(String titleLable) {
		this.subTitleLable = titleLable;
	}

	public String getMstTitle() {
		return mstTitle;
	}

	public void setMstTitle(String mstTitle) {
		this.mstTitle = mstTitle;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getSubTitleLable() {
		return subTitleLable;
	}

	public void setSubTitleLable(String subTitleLable) {
		this.subTitleLable = subTitleLable;
	}




	public Object getSubChart() {
		return subChart;
	}

	public void setSubChart(Object subChart) {
		this.subChart = subChart;
	}

	public List getSubTable() {
		return subTable;
	}

	public void setSubTable(List subTable) {
		this.subTable = subTable;
	}

	public String getSubTableFile() {
		return subTableFile;
	}

	public void setSubTableFile(String subTableFile) {
		this.subTableFile = subTableFile;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getDvcIp() {
		return dvcIp;
	}

	public void setDvcIp(String dvcIp) {
		this.dvcIp = dvcIp;
	}

	public String getSTime() {
		return sTime;
	}

	public void setSTime(String time) {
		sTime = time;
	}

	public String getETime() {
		return eTime;
	}

	public void setETime(String time) {
		eTime = time;
	}

	public String[] getTalCategory() {
		return talCategory;
	}

	public void setTalCategory(String[] talCategory) {
		this.talCategory = talCategory;
	}

	public String[] getNodeId() {
		return nodeId;
	}

	public void setNodeId(String[] nodeId) {
		this.nodeId = nodeId;
	}

	public short getTalCategoryLevel() {
		return talCategoryLevel;
	}

	public void setTalCategoryLevel(short talCategoryLevel) {
		this.talCategoryLevel = talCategoryLevel;
	}

	public int getMstType() {
		return mstType;
	}

	public void setMstType(int mstType) {
		this.mstType = mstType;
	}
}
