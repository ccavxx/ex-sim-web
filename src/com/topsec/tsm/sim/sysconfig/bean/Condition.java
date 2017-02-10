package com.topsec.tsm.sim.sysconfig.bean;

public class Condition {
	private Cprops cprops;
	private Cselector cselector;
	private Cwindow cwindow;
	public Cprops getCprops() {
		return cprops;
	}
	public void setCprops(Cprops cprops) {
		this.cprops = cprops;
	}
	public Cselector getCselector() {
		return cselector;
	}
	public void setCselector(Cselector cselector) {
		this.cselector = cselector;
	}
	public Cwindow getCwindow() {
		return cwindow;
	}
	public void setCwindow(Cwindow cwindow) {
		this.cwindow = cwindow;
	}

}
