package com.topsec.tsm.ui.topo.vo;

import java.util.List;

public class ItemVo {
	private String id;
	private String text;
	private String pid;
	private String nodeId;
	private List<ItemVo> children;
	private Integer _id;
	private Integer _pid;
	private Integer _level;
	private String isView;
	private String url;
	private Integer time;
	private boolean expanded;
	private boolean isLeaf = false;
	public boolean isLeaf() {
		return isLeaf;
	}
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	} 
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getIsView() {
		return isView;
	}
	public void setIsView(String isView) {
		this.isView = isView;
	} 
	public Integer get_id() {
		return _id;
	}
	public void set_id(Integer id) {
		_id = id;
	}
	public Integer get_pid() {
		return _pid;
	}
	public void set_pid(Integer pid) {
		_pid = pid;
	}
	public Integer get_level() {
		return _level;
	}
	public void set_level(Integer level) {
		_level = level;
	}
	public boolean isExpanded() {
		return expanded;
	}
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	public List<ItemVo> getChildren() {
		return children;
	}
	public void setChildren(List<ItemVo> children) {
		this.children = children;
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
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
}
