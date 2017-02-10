package com.topsec.tsm.sim.node.util;

public class CollectorType {
	private String type;
	private String name;
	private String componenttype;
	private boolean allowDupIp ;
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComponenttype() {
		return componenttype;
	}

	public void setComponenttype(String componenttype) {
		this.componenttype = componenttype;
	}
	
	public boolean isAllowDupIp() {
		return allowDupIp;
	}

	public void setAllowDupIp(boolean allowDupIp) {
		this.allowDupIp = allowDupIp;
	}

	public CollectorType(){
		
	}
	
	public CollectorType(String type, String name, String componenttype,boolean allowDupIp) {
		super();
		this.type = type;
		this.name = name;
		this.componenttype = componenttype;
		this.allowDupIp = allowDupIp ;
	}

}
