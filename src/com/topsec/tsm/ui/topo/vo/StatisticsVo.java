package com.topsec.tsm.ui.topo.vo;

import org.apache.log4j.Logger;

/**
 * ͳ������ģ��
 * 
 * @author Administrator
 * 
 */
public class StatisticsVo {
	private static final Logger log = Logger.getLogger(StatisticsVo.class);
	private String itemId;
	private String[] eventTypes;

	private String[] eventLevels;
	private String[] devStatus;
	public String getItemId() {
		return itemId;
	}

	private String[] phyValues;
	private String[] filters;
	private String lockInfoInView; 


 

	public String getLockInfoInView() {
		return lockInfoInView;
	} 

	public void setLockInfoInView(String lockInfoInView) {
		this.lockInfoInView = lockInfoInView;
	}

	public String[] getFilters() {
		return filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
	}

	public String[] getDevStatus() {
		return devStatus;
	}

	public void setDevStatus(String[] devStatus) {
		this.devStatus = devStatus;
	}

	public String[] getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(String[] eventType) {
		this.eventTypes = eventType;
	}

	public String[] getEventLevels() {
		return eventLevels;
	}

	public void setEventLevels(String[] eventLevel) {
		this.eventLevels = eventLevel;
	}

	public String getItemSimpleId() {
		return itemId==null?null:itemId.indexOf("-DV")!=-1?itemId.split("-DV")[1]:itemId;
	}
	

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String[] getPhyValues() {
		return phyValues;
	}

	public void setPhyValues(String[] phyValues) {
		this.phyValues = phyValues;
	}
	@Override
	public String toString() {
		return this.getItemSimpleId() ;
	}

}
