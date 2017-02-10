package com.topsec.tsm.sim.sysconfig.bean;

import java.util.List;

public class Cwindow {
	
	
	private String name;
	private boolean visiable;
	private boolean isOption;
	private String type;
	private String value;
	private String alias;
	private String desc;
	private List<Property> propList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isVisiable() {
		return visiable;
	}

	public void setVisiable(boolean visiable) {
		this.visiable = visiable;
	}

	public boolean isOption() {
		return isOption;
	}

	public void setOption(boolean isOption) {
		this.isOption = isOption;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Property> getPropList() {
		return propList;
	}

	public void setPropList(List<Property> propList) {
		this.propList = propList;
	}

}
