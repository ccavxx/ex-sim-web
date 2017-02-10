package com.topsec.tsm.sim.report.bean.struct;

/*
 *	BaseStruct	基础结构体供继承用
 */

public class BaseStruct {
	// 子报表
	private String subrptid;
	//主报表
	private String mstrptid;
	// 设备地址 防火墙 ip
	private String dvcaddress;
	// 设备类型 防火墙
	private String dvctype;
	// chart 下探参数2
	private String[] talCategory;
	
	private String dig;
	//开始时间
	private String sTime;
	// 结束时间
	private String eTime;

	// show input time
	private String viewshow;
	// 极度
	private String viewji ;
	// 类型
	private String viewtype;
	//action name
	private String actionname;
	
	private String[] nodeId;
	
	private String onlyByDvctype;
	
	public String getOnlyByDvctype() {
		return onlyByDvctype;
	}

	public void setOnlyByDvctype(String onlyByDvctype) {
		this.onlyByDvctype = onlyByDvctype;
	}

	private String mstType;//自定义 非自定义


	public String getMstType() {
		return mstType;
	}

	public void setMstType(String mstType) {
		this.mstType = mstType;
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

	public String getDvcaddress() {
		return dvcaddress;
	}

	public void setDvcaddress(String dvcaddress) {
		this.dvcaddress = dvcaddress;
	}

	public String getDvctype() {
		return dvctype;
	}

	public void setDvctype(String dvctype) {
		this.dvctype = dvctype;
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

	public String getDig() {
		return dig;
	}

	public void setDig(String dig) {
		this.dig = dig;
	}

	public String getViewshow() {
		return viewshow;
	}

	public void setViewshow(String viewshow) {
		this.viewshow = viewshow;
	}

	public String getViewji() {
		return viewji;
	}

	public void setViewji(String viewji) {
		this.viewji = viewji;
	}

	public String getViewtype() {
		return viewtype;
	}

	public void setViewtype(String viewtype) {
		this.viewtype = viewtype;
	}

	public String getActionname() {
		return actionname;
	}

	public void setActionname(String actionname) {
		this.actionname = actionname;
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

}