/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 周小虎
* @since  2011-06-15
* @version 1.0
* 
*/
package com.topsec.tsm.sim.node.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NodeVO implements Serializable {
	private static final long serialVersionUID = 1L;
	private String ip;
	private String nodeId;
	private String routeUrl;
	private String type;
	private int state;
	private String version;
	private String domainId;
	private String parentId;
	private String flux;
	private String alias;
	private String resourceName;
	private String securityObjectType;
	private String deviceType;
	private String bmjVersion;//保密局版本
	
	public String getBmjVersion() {
		return bmjVersion;
	}
	public void setBmjVersion(String bmjVersion) {
		this.bmjVersion = bmjVersion;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getSecurityObjectType() {
		return securityObjectType;
	}
	public void setSecurityObjectType(String securityObjectType) {
		this.securityObjectType = securityObjectType;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getFlux() {
		return flux;
	}
	public void setFlux(String flux) {
		this.flux = flux;
	}
	private List<NodeVO> childs = new ArrayList<NodeVO>();

	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getRouteUrl() {
		return routeUrl;
	}
	public void setRouteUrl(String routeUrl) {
		this.routeUrl = routeUrl;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDomainId() {
		return domainId;
	}
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public List<NodeVO> getChilds() {
		return childs;
	}
	public void setChilds(List<NodeVO> childs) {
		this.childs = childs;
	}
	

}
