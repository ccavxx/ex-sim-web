package com.topsec.tsm.ui.topo.svg.elements;

import java.sql.Clob;
import java.sql.Date;
import java.sql.Timestamp;



public class ViewVo {
	private String viewId;//拓扑图id
	private String nodeId;//节点id
	private Clob view;//拓扑图
	private Clob states;//事件显示设置
	private String typeState;//类型显示设置
	private String creater;//创建人
	private Timestamp createTime;//创建时间
	private String lastModifyed;//最后修改人
	private Timestamp lastModifyedTime;//最后修改时间
	private String lastIp;
	private String creIp;
	private Integer updateCount;
	private Integer delflg;
	private Integer custom01;
	private Integer custom02;
	private Date custom03;
	private Date custom04;
	private String custom05; 
	private String custom06;
	private String custom07;
	private String custom08;
	public String getViewId() {
		return viewId;
	}
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public Clob getView() {
		return view;
	}
	public void setView(Clob view) {
		this.view = view;
	}
	public Clob getStates() {
		return states;
	}
	public void setStates(Clob states) {
		this.states = states;
	}
	public String getTypeState() {
		return typeState;
	}
	public void setTypeState(String typeState) {
		this.typeState = typeState;
	}
	public String getCreater() {
		return creater;
	}
	public void setCreater(String creater) {
		this.creater = creater;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public String getLastModifyed() {
		return lastModifyed;
	}
	public void setLastModifyed(String lastModifyed) {
		this.lastModifyed = lastModifyed;
	}
	public Timestamp getLastModifyedTime() {
		return lastModifyedTime;
	}
	public void setLastModifyedTime(Timestamp lastModifyedTime) {
		this.lastModifyedTime = lastModifyedTime;
	}
	public String getLastIp() {
		return lastIp;
	}
	public void setLastIp(String lastIp) {
		this.lastIp = lastIp;
	}
	public String getCreIp() {
		return creIp;
	}
	public void setCreIp(String creIp) {
		this.creIp = creIp;
	}
	public Integer getUpdateCount() {
		return updateCount;
	}
	public void setUpdateCount(Integer updateCount) {
		this.updateCount = updateCount;
	}
	public Integer getDelflg() {
		return delflg;
	}
	public void setDelflg(Integer delflg) {
		this.delflg = delflg;
	}
	public Integer getCustom01() {
		return custom01;
	}
	public void setCustom01(Integer custom01) {
		this.custom01 = custom01;
	}
	public Integer getCustom02() {
		return custom02;
	}
	public void setCustom02(Integer custom02) {
		this.custom02 = custom02;
	}
	public Date getCustom03() {
		return custom03;
	}
	public void setCustom03(Date custom03) {
		this.custom03 = custom03;
	}
	public Date getCustom04() {
		return custom04;
	}
	public void setCustom04(Date custom04) {
		this.custom04 = custom04;
	}
	public String getCustom05() {
		return custom05;
	}
	public void setCustom05(String custom05) {
		this.custom05 = custom05;
	}
	public String getCustom06() {
		return custom06;
	}
	public void setCustom06(String custom06) {
		this.custom06 = custom06;
	}
	public String getCustom07() {
		return custom07;
	}
	public void setCustom07(String custom07) {
		this.custom07 = custom07;
	}
	public String getCustom08() {
		return custom08;
	}
	public void setCustom08(String custom08) {
		this.custom08 = custom08;
	}
	
}
