package com.topsec.tsm.sim.asset.bean;


/**
 * @ClassName: InformationSystem
 * @Declaration: TODO
 * 
 * @author: WangZhiai create on2014年6月13日下午7:16:17
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class InformationSystem implements java.io.Serializable{
	private int id;
	private String name;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
