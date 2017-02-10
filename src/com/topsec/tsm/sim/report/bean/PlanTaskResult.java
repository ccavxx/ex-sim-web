package com.topsec.tsm.sim.report.bean;

import java.io.Serializable;
import java.util.Date;

import com.topsec.tsm.sim.response.persistence.Response;

/**
 * @ClassName: PlanTaskResult
 * @Declaration: TODO
 * 
 * @author: WangZhiai create on2014年5月7日下午7:03:45
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class PlanTaskResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private java.lang.String id;

	// fields
	//private java.lang.String respId;
	private java.lang.String name;
	private java.lang.String type;
	private java.lang.String subType;
	private java.lang.String result;
	private String respTime;
	//private com.topsec.tsm.sim.response.persistence.Response response;

	private java.lang.String resultDesc;
	private java.lang.String eventDesc;
	private java.lang.Integer useTime;

	public PlanTaskResult() {
		super();
	}

	public java.lang.String getId() {
		return id;
	}

	public void setId(java.lang.String id) {
		this.id = id;
	}

	public java.lang.String getName() {
		return name;
	}

	public void setName(java.lang.String name) {
		this.name = name;
	}

	public java.lang.String getType() {
		return type;
	}

	public void setType(java.lang.String type) {
		this.type = type;
	}

	public java.lang.String getSubType() {
		return subType;
	}

	public void setSubType(java.lang.String subType) {
		this.subType = subType;
	}

	public java.lang.String getResult() {
		return result;
	}

	public void setResult(java.lang.String result) {
		this.result = result;
	}

	public String getRespTime() {
		return respTime;
	}

	public void setRespTime(String respTime) {
		this.respTime = respTime;
	}

	public java.lang.String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(java.lang.String resultDesc) {
		this.resultDesc = resultDesc;
	}

	public java.lang.String getEventDesc() {
		return eventDesc;
	}

	public void setEventDesc(java.lang.String eventDesc) {
		this.eventDesc = eventDesc;
	}

	public java.lang.Integer getUseTime() {
		return useTime;
	}

	public void setUseTime(java.lang.Integer useTime) {
		this.useTime = useTime;
	}

	
}
