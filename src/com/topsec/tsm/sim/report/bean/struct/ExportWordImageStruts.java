package com.topsec.tsm.sim.report.bean.struct;

import java.io.Serializable;

public class ExportWordImageStruts implements Serializable{
	
	private String id;
	
	private String name;
	
	private String contentType;
	
	private String contentBase64Code;
	
	private boolean reportImageHasChart;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentBase64Code() {
		return contentBase64Code;
	}

	public void setContentBase64Code(String contentBase64Code) {
		this.contentBase64Code = contentBase64Code;
	}

	public boolean getReportImageHasChart() {
		return reportImageHasChart;
	}

	public void setReportImageHasChart(boolean reportImageHasChart) {
		this.reportImageHasChart = reportImageHasChart;
	}
}
