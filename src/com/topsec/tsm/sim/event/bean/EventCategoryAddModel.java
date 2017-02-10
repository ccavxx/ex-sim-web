package com.topsec.tsm.sim.event.bean;

import java.io.Serializable;

public class EventCategoryAddModel implements Serializable{
	private String cat1;
	private String cat2;
	
	public String getCat1() {
		return cat1;
	}
	public void setCat1(String cat1) {
		this.cat1 = cat1;
	}
	public String getCat2() {
		return cat2;
	}
	public void setCat2(String cat2) {
		this.cat2 = cat2;
	}
	
	
}
