package com.topsec.tsm.sim.event.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CategoryLevelParam implements Serializable{
	private int level;
	private String name;
	private Integer alarmState;//null：统计全部   为1统计事件告警   0统计全部非告警事件
	private Map<String,String> category=new HashMap<String, String>();
	private String requestIp;
	public String getRequestIp() {
		return requestIp;
	}
	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Map<String, String> getCategory() {
		return category;
	}
	public void setCategory(Map<String, String> category) {
		this.category = category;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAlarmState() {
		return alarmState;
	}
	public void setAlarmState(Integer alarmState) {
		this.alarmState = alarmState;
	}
	
}
