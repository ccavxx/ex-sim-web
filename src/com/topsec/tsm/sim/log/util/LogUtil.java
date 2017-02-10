package com.topsec.tsm.sim.log.util;

import java.util.ArrayList;
import java.util.List;

import com.topsec.tal.base.search.SearchObject;

public class LogUtil {
	
	private String headerText;
	private String dataField;
	private String dataType;
	
	private String msg;
	private String ip;
	private String time;
	
	private String deviceType;
	private String host;
	private String rname;
	private String enable;
	
	private String nodeId;//节点ID
	private String[] route;
	private SearchObject searchObject;//查询条件
	private List<Object> otherValues ;
	private  int counts;//丢入到Map中的总时间
	private List<LogUtil> childs = new ArrayList<LogUtil>();
	
	
	public LogUtil(){
	}
	
	public LogUtil(String dataField, String headerText) {
	}
	public SearchObject getSearchObject() {
		return searchObject;
	}
	public void setSearchObject(SearchObject searchObject) {
		this.searchObject = searchObject;
	}
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
	private int state;
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getEnable() {
		return enable;
	}
	public void setEnable(String enable) {
		this.enable = enable;
	}
	
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public List<LogUtil> getChilds() {
		return childs;
	}
	public void setChilds(List<LogUtil> childs) {
		this.childs = childs;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getRname() {
		return rname;
	}
	public void setRname(String rname) {
		this.rname = rname;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getHeaderText() {
		return headerText;
	}
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}
	public String getDataField() {
		return dataField;
	}
	public void setDataField(String dataField) {
		this.dataField = dataField;
	}
	public String[] getRoute() {
		return route;
	}
	public void setRoute(String[] route) {
		this.route = route;
	}

	public List<Object> getOtherValues() {
		return otherValues;
	}

	public void setOtherValues(List<Object> otherValues) {
		this.otherValues = otherValues;
	}

}
