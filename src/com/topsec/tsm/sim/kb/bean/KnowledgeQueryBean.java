package com.topsec.tsm.sim.kb.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import com.topsec.tsm.sim.util.DateUtils;


public class KnowledgeQueryBean implements Serializable{
	private Integer priority;
	private String name;
	private String cat1id;
	private String cat2id;
	private String description;
	private String solution;
	private Date datetime;
	private int page;
	private int rows;
	
	
	public Date getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		if(datetime!=null&&datetime.trim().length()>0){
			try {
				this.datetime = DateUtils.parseDatetime(datetime, "yyyy-MM-dd HH:mm:ss");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	 
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCat1id() {
		return cat1id;
	}
	public void setCat1id(String cat1id) {
		this.cat1id = cat1id;
	}
	public String getCat2id() {
		return cat2id;
	}
	public void setCat2id(String cat2id) {
		this.cat2id = cat2id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSolution() {
		return solution;
	}
	public void setSolution(String solution) {
		this.solution = solution;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
 
	 
	
	
}
