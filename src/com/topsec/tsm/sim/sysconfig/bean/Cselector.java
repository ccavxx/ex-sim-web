package com.topsec.tsm.sim.sysconfig.bean;

import java.util.List;

public class Cselector {
	private List<Cprop> cpList;
	private String name;
	private boolean visiable;
	private boolean isOption;
	private String type;
	private String value;
	private String alias;
	private String desc;
	
	

	@Override
	public String toString() {
		return "Cselector [alias=" + alias + ", cpList=" + cpList + ", desc=" + desc + ", isOption=" + isOption + ", name=" + name + ", type=" + type + ", value=" + value
				+ ", visiable=" + visiable + "]";
	}

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

	public List<Cprop> getCpList() {
		return cpList;
	}

	public void setCpList(List<Cprop> cpList) {
		this.cpList = cpList;
	}

}
