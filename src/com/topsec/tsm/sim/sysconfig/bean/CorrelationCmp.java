package com.topsec.tsm.sim.sysconfig.bean;

import java.io.Serializable;

public class CorrelationCmp implements Serializable{
	private Integer id;
	private String func;
	private String field;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getFunc() {
		return func;
	}
	public void setFunc(String func) {
		this.func = func;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	
	
}
