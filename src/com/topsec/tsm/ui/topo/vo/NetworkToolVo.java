package com.topsec.tsm.ui.topo.vo;

public class NetworkToolVo {
	private String resourceId;
	private int ping_count=4;
	private int ping_timeout=1000;
	private int telnet_port=23;
	private int ssh_port=22;
	private String ssh_user;
	private String ssh_pwd;
	private String web_url;
	
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public int getPing_count() {
		return ping_count;
	}
	public void setPing_count(int ping_count) {
		this.ping_count = ping_count;
	} 
	public int getPing_timeout() {
		return ping_timeout;
	}
	public void setPing_timeout(int ping_timeout) {
		this.ping_timeout = ping_timeout;
	}
	public int getTelnet_port() {
		return telnet_port;
	}
	public void setTelnet_port(int telnet_port) {
		this.telnet_port = telnet_port;
	}
	public int getSsh_port() {
		return ssh_port;
	}
	public void setSsh_port(int ssh_port) {
		this.ssh_port = ssh_port;
	}
	public String getWeb_url() {
		return web_url;
	}
	public void setWeb_url(String web_url) {
		this.web_url = web_url;
	}
	public String getSsh_user() {
		return ssh_user;
	}
	public void setSsh_user(String ssh_user) {
		this.ssh_user = ssh_user;
	}
	public String getSsh_pwd() {
		return ssh_pwd;
	}
	public void setSsh_pwd(String ssh_pwd) {
		this.ssh_pwd = ssh_pwd;
	}
	

}
