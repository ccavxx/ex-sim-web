package com.topsec.tsm.sim.sysconfig.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.topsec.tsm.util.UUIDUtils;

@JsonInclude(Include.NON_EMPTY)
public class TreeData implements Serializable {
	private String id;
	private String text;
	// private String iconCls="";
	private String state = "closed";
	private boolean checked = false;
	private TreeDataList children = new TreeDataList();
	private Map<String, Object> attributes = new HashMap();

	public TreeData(String text) {
		this.id = UUIDUtils.compactUUID();
		this.text = text;
	}

	public TreeData(String id, String text) {
		this.id = id;
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public TreeDataList getChildren() {
		return children;
	}

	public void setChildren(TreeDataList children) {
		this.children = children;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

}
