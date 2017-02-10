package com.topsec.tsm.sim.report.chart.highchart.model;

import java.util.List;


public class SubItem {
	private String type;
	private String name;
	private List<?>attrList;
	private Object[] data;
	public SubItem() {
		super();
	}
	
	public SubItem(String type, String name, Object[] data) {
		super();
		this.type = type;
		this.name = name;
		this.data = data;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object[] getData() {
		return data;
	}

	public void setData(Object[] data) {
		this.data = data;
	}

	public List<?> getAttrList() {
		return attrList;
	}

	public void setAttrList(List<?> attrList) {
		this.attrList = attrList;
	}
	
}
