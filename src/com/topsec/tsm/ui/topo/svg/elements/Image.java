package com.topsec.tsm.ui.topo.svg.elements;

public class Image extends BaseElement {
	private String href;
	private String ip;
	private String hostName;
	private String rux;
	private String nodeId;
	private String groupId;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getRux() {
		return rux;
	}

	public void setRux(String rux) {
		this.rux = rux;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHref() {
		return href;
	}  

	public void setHref(String href) {
		this.href = href;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

}
