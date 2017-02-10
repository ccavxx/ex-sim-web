package com.topsec.tsm.ui.topo.vo;

public class EventTypeVo {
	private String name;
	private String description;
	private EventTypeVo parentType;

	public EventTypeVo getParentType() {
		return parentType;
	}

	public void setParentType(EventTypeVo parentType) {
		this.parentType = parentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
 
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean hasParent() {
		return parentType != null;
	} 

}
