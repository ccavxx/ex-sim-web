package com.topsec.tsm.sim.asset.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.collector.datasource.DataSource;
import com.topsec.tsm.sim.common.tree.Tree;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

public class DataSourceNode implements Tree<DataSourceNode>,Comparable<DataSourceNode> {
	/**
	 * 节点id，例如：Firewall,Topsec
	 */
	private String id ;
	/**
	 * 节点名称,例如:防火墙，交换机，天融信
	 */
	private String name ;
	/**
	 * 节点路径，例如：Flow/TOPSEC/TA-W NetflowV5,Firewall
	 */
	private String pathId ;
	/**
	 * 日志源，只有在日志源树的最末一级才能有日志源
	 */
	private List<DataSource> dataSources ;
	/**
	 * 子节点
	 */
	private Map<String,DataSourceNode> child ;
	
	private int order ;
	
	private DataSourceNode parent ;
	
	public DataSourceNode() {
		this.id = "" ;
	}
	public DataSourceNode(String id,String name, DataSourceNode parent,int order) {
		super();
		this.id = id;
		this.name = name ;
		this.parent = parent ;
		this.order = order ;
		this.pathId = parent == null || StringUtil.isBlank(parent.getPathId()) ? id : parent.getPathId()+"/"+id ;
	}
	public String getId() {
		return id;
	}
	public String getPathId() {
		return pathId;
	}
	public String getName() {
		return name;
	}
	public List<DataSourceNode> getChildren(){
		if(child == null){
			return Collections.emptyList() ;
		}
		ArrayList<DataSourceNode> childNodes = new ArrayList<DataSourceNode>(child.values()) ;
		Collections.sort(childNodes) ;
		return childNodes ;
	}
	
	@Override
	public boolean isLeaf() {
		return ObjectUtils.isEmpty(child);
	}
	public void addDataSource(DataSource dataSource){
		if(dataSources == null){
			dataSources = new ArrayList<DataSource>() ;
		}
		dataSources.add(dataSource) ;
	}
	
	public List<DataSource> getDataSources() {
		return dataSources;
	}
	public void addChild(DataSourceNode node){
		if (child == null) {
			child = new HashMap<String, DataSourceNode>() ;
		}
		child.put(node.getId(), node) ;
	}
	
	public DataSourceNode getChildById(String id){
		if (child == null) {
			return null ;
		}
		return child.get(id) ;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this){
			return true ;
		}
		if(!(obj instanceof DataSourceNode)){
			return false ;
		}
		DataSourceNode node = (DataSourceNode)obj ;
		return this.id.equals(node.id) ;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode() ;
	}
	@Override
	public int getLevel() {
		int level = 1 ;
		DataSourceNode node = this ;
		while((node = node.getParent()) != null){
			level++ ;
		}
		return level ;
	}
	@Override
	public DataSourceNode getParent() {
		return parent;
	}
	public String getPathName(){
		return DeviceTypeNameUtil.getDeviceTypeName(getPathId(), Locale.getDefault()) ;
	}
	@Override
	public int compareTo(DataSourceNode o) {
		return this.order >= o.order ? 1 : -1 ;
	}
	
}
