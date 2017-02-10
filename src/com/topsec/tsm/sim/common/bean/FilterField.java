package com.topsec.tsm.sim.common.bean;

/**
 * 过滤器字段
 * @author hp
 *
 */
public class FilterField {

	private String field;
	private String alias;
	private String type;
	private String[] values;

	public FilterField(String field, String alias, String type) {
		this.field = field;
		this.alias = alias;
		this.type = type;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

}
