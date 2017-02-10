package com.topsec.tsm.sim.event.bean;

import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;


public class BasicQueryCriteria implements Serializable {
	/*@DateTimeFormat
	private Date startTime;*/
	public String ip ;
	public  String deviceIp;
	public  String deviceType;
	public  String eventType;
	private String eventName;
	private String srcIp;
	private String destIp;
	private String srcPort;
	private String destPort;
	private String category1;
	private String category2;
	private int page;
	private int rows;
	private String fields;
	private String header;
	private String protocol;
	public String startTime;
	private String requestIp;
	public String endTime;
	public String priority;
	public String ruleName;
	public String query_event_Name;
	private String confirm;
	private String confirm_person;
	public String getConfirm() {
		return confirm;
	}
	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}
	public String getConfirm_person() {
		return confirm_person;
	}
	public void setConfirm_person(String confirm_person) {
		this.confirm_person = confirm_person;
	}
	
	public String getQuery_event_Name() {
		return query_event_Name;
	}
	public void setQuery_event_Name(String query_event_Name) {
		this.query_event_Name = query_event_Name;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getRequestIp() {
		return requestIp;
	}
	public void setRequestIp(String requestIp) {
		this.requestIp = requestIp;
	}
	public String getCategory1() {
		return category1;
	}
	public String getCategory2() {
		return category2;
	}
	public String getDestIp() {
		return destIp;
	}
	public String getDestPort() {
		return destPort;
	}
	public String getDeviceIp() {
		return deviceIp;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public String getEndTime() {
		return endTime;
	}
	public String getEventName() {
		return eventName;
	}
	public String getEventType() {
		return eventType;
	}
	public String getFields() {
		return fields;
	}
	public String getHeader() {
		return header;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPage() {
		return page;
	}
	
	public String getPriority() {
		return priority;
	}
	public String getProtocol() {
		return protocol;
	}
	public int getRows() {
		return rows;
	}
	public String getSrcIp() {
		return srcIp;
	}
	
	public String getSrcPort() {
		return srcPort;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setCategory1(String category1) {
		this.category1 = category1;
	}
	public void setCategory2(String category2) {
		this.category2 = category2;
	}
	public void setDestIp(String destIp) {
		this.destIp = destIp;
	}
	public void setDestPort(String destPort) {
		this.destPort = destPort;
	}
	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}
	public void setDeviceType( String deviceType) {
		this.deviceType = deviceType;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public void setFields(String fields) {
		this.fields = fields;
	}
	 
	public void setHeader(String header) {
		this.header = header;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	 
	 
	public void setRows(int rows) {
		this.rows = rows;
	}
	public void setSrcIp(String srcIp) {
		this.srcIp = srcIp;
	}
	public void setSrcPort(String srcPort) {
		this.srcPort = srcPort;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.deviceIp+":"+deviceType+":"+startTime;
	}
	
}
