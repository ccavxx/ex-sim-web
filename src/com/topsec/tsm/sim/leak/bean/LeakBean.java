package com.topsec.tsm.sim.leak.bean;

import java.io.Serializable;

public class LeakBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	private Float score;
	private Long publishedTime;
	private Long mdfTime;
	private String cpe;
	private String summary;
	private String detail;
	private String year;
	
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
	public Float getScore() {
		return score;
	}
	public void setScore(Float score) {
		this.score = score;
	}
	public Long getPublishedTime() {
		return publishedTime;
	}
	public void setPublishedTime(Long publishedTime) {
		this.publishedTime = publishedTime;
	}
	public Long getMdfTime() {
		return mdfTime;
	}
	public void setMdfTime(Long mdfTime) {
		this.mdfTime = mdfTime;
	}
	public String getCpe() {
		return cpe;
	}
	public void setCpe(String cpe) {
		this.cpe = cpe;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	
}
