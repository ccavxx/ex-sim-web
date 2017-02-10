package com.topsec.tsm.sim.common.dao;

public class SimOrder {

	private String property ;
	private boolean asc ;
	
	public SimOrder(String property, boolean asc) {
		this.property = property;
		this.asc = asc;
	}
	
	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public static SimOrder desc(String property){
		return new SimOrder(property,false) ;
	}
	
	public static SimOrder asc(String property){
		return new SimOrder(property,true) ;
	}
}
