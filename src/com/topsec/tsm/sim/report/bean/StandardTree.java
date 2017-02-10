package com.topsec.tsm.sim.report.bean;

import com.topsec.tsm.sim.common.model.TreeModel;

/**
 * @ClassName: StandardTree
 * @Declaration: ToDo
 * 
 * @author: WangZhiai create on2014年5月14日下午6:16:53
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class StandardTree extends TreeModel implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String parentId;
	private String type;
	public StandardTree() {
		super();
	}
	public StandardTree(String id, String text, String state) {
		super(id, text, state);
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
