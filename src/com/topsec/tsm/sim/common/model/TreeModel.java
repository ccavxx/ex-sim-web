package com.topsec.tsm.sim.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeModel {
	private String id;
	private String text;
	private String state;
	private Map<String,Object> attributes = new HashMap<String,Object>();
	private List<TreeModel> children; 
	
	public TreeModel() {
		super();
	}
	
	public TreeModel(String id, String text) {
		super();
		this.id = id;
		this.text = text;
	}

	public TreeModel(String id, String text, String state) {
		super();
		this.id = id;
		this.text = text;
		this.state = state;
	}

	public TreeModel(String text, Map<String, Object> attributes) {
		super();
		this.text = text;
		this.attributes = attributes;
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
	
	public Map getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String,Object> attributes) {
		this.attributes = attributes;
	}

	public void putAttribute(String key,Object value){
		attributes.put(key, value) ;
	}
	
	public List<TreeModel> getChildren() {
		return children;
	}
	
	public void addChild(TreeModel child){
		if(children==null){
			children = new ArrayList<TreeModel>();
		}
		children.add(child) ;
	}
	public void setChildren(List<TreeModel> children) {
		this.children = children;
	}
}
