package com.topsec.tsm.sim.test.web;

public class TestBean {

	Integer id ;
	String name ;
	
	public TestBean() {
		super();
	}
	public TestBean(int id, String name) {
		this.id = id ;
		this.name = name ;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
