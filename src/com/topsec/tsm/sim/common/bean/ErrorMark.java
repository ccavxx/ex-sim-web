package com.topsec.tsm.sim.common.bean;

/**
 * @ClassName: ErrorMark
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2014年6月18日下午4:24:11
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ErrorMark implements java.io.Serializable{
	private String serialId;
	private String deviceIp;
	private String deviceName;
	private String errorContent;
	public ErrorMark() {
		super();
	}
	public String getSerialId() {
		return serialId;
	}
	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}
	public String getDeviceIp() {
		return deviceIp;
	}
	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getErrorContent() {
		return errorContent;
	}
	public void setErrorContent(String errorContent) {
		this.errorContent = errorContent;
	}
	
}
