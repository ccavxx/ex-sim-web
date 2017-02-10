package com.topsec.tsm.sim.sysconfig.bean;

import java.util.List;

public class Cprop {
	private List<Field> fields;
	private List<Op> ops;
	private List<Val> values;
	private boolean isOption;
	
	
	

	public boolean isOption() {
		return isOption;
	}

	public void setOption(boolean isOption) {
		this.isOption = isOption;
	}

	public List<Val> getValues() {
		return values;
	}

	public void setValues(List<Val> values) {
		this.values = values;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public List<Op> getOps() {
		return ops;
	}

	public void setOps(List<Op> ops) {
		this.ops = ops;
	}

}
