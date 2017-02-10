package com.topsec.tsm.sim.newreport.bean;

import com.topsec.tsm.sim.datasource.SimDatasource;

/**
 * @ClassName: SimDsVo
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月23日下午2:05:51
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class SimDsVo {
	private Long resourceId;
	private String deviceIp;
	private String securityObjectType;
	private String auditorNodeId;
	private String resourceName;
	private String deviceType;
	private String nodeId;
	public SimDsVo() {
		super();
	}
	public SimDsVo(Long resourceId,String deviceIp, String securityObjectType,
			String auditorNodeId, String reourceName, String deviceType,
			String nodeId) {
		super();
		this.resourceId=resourceId;
		this.deviceIp = deviceIp;
		this.securityObjectType = securityObjectType;
		this.auditorNodeId = auditorNodeId;
		this.resourceName = reourceName;
		this.deviceType = deviceType;
		this.nodeId = nodeId;
	}
	public SimDsVo(SimDatasource simDatasource) {
		super();
		if (null == simDatasource) {
			return;
		}
		this.resourceId=simDatasource.getResourceId();
		this.deviceIp = simDatasource.getDeviceIp();
		this.securityObjectType = simDatasource.getSecurityObjectType();
		this.auditorNodeId = simDatasource.getAuditorNodeId();
		this.resourceName = simDatasource.getResourceName();
		this.deviceType = simDatasource.getDeviceType();
		this.nodeId = simDatasource.getNodeId();
	}
	public String getDeviceIp() {
		return deviceIp;
	}
	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}
	public String getSecurityObjectType() {
		return securityObjectType;
	}
	public void setSecurityObjectType(String securityObjectType) {
		this.securityObjectType = securityObjectType;
	}
	public String getAuditorNodeId() {
		return auditorNodeId;
	}
	public void setAuditorNodeId(String auditorNodeId) {
		this.auditorNodeId = auditorNodeId;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public Long getResourceId() {
		return resourceId;
	}
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}
}
