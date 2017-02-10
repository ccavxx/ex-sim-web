package com.topsec.tsm.sim.sysconfig.bean;

import java.io.Serializable;

public class FreqState implements Serializable{
	private Integer id;
	private Integer repeat;
	private Integer timeout;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getRepeat() {
		return repeat;
	}
	public void setRepeat(Integer repeat) {
		this.repeat = repeat;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	
	
	
}
