package com.topsec.tsm.sim.sysconfig.bean;

public class Op {
	private String name;
	private String alias;
	private boolean showInput;
	private String showItem;
	private String showLabel;
	private boolean showSelect;

	@Override
	public String toString() {
		return "Op [alias=" + alias + ", name=" + name + ", showInput=" + showInput + ", showItem=" + showItem + ", showLabel=" + showLabel + "]";
	}

	public boolean isShowSelect() {
		return showSelect;
	}

	public void setShowSelect(boolean showSelect) {
		this.showSelect = showSelect;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isShowInput() {
		return showInput;
	}

	public void setShowInput(boolean showInput) {
		this.showInput = showInput;
	}

	public String getShowItem() {
		return showItem;
	}

	public void setShowItem(String showItem) {
		this.showItem = showItem;
	}

	public String getShowLabel() {
		return showLabel;
	}

	public void setShowLabel(String showLabel) {
		this.showLabel = showLabel;
	}

}
