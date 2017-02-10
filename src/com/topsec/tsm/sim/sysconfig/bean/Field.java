package com.topsec.tsm.sim.sysconfig.bean;

public class Field {
	private String name;
	private String type;
	private String validate;
	private String alias;
	private String validateSize;
	private String showItem;

	
	
	public String getShowItem() {
		return showItem;
	}

	public void setShowItem(String showItem) {
		this.showItem = showItem;
	}

	@Override
	public String toString() {
		return "Field [alias=" + alias + ", name=" + name + ", type=" + type + ", validate=" + validate + "]";
	}

	public String getValidateSize() {
		return validateSize;
	}

	public void setValidateSize(String validateSize) {
		this.validateSize = validateSize;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValidate() {
		return validate;
	}

	public void setValidate(String validate) {
		this.validate = validate;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
