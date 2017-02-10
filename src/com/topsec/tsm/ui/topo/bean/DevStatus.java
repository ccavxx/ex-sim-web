package com.topsec.tsm.ui.topo.bean;

public class DevStatus extends EvtCol {
	public DevStatus(String id, String name, String colName) {
		super(id, name, colName);
	}

	private String val;

	private String ip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	@Override
	public String toString() {
		return "ip:" + this.ip + "/val:" + val + "/colName:" + super.getColName();
	}
  
}
