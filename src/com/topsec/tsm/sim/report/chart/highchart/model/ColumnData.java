package com.topsec.tsm.sim.report.chart.highchart.model;

import java.util.TreeMap;

public class ColumnData {
	private TreeMap data;
	private String href;
	private int hrefIndex = 0;
	
	public ColumnData() {
		super();
	}

	public ColumnData(TreeMap data, String href, int hrefIndex) {
		super();
		this.data = data;
		this.href = href;
		this.hrefIndex = hrefIndex;
	}

	public TreeMap getData() {
		return data;
	}

	public void setData(TreeMap data) {
		this.data = data;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public int getHrefIndex() {
		return hrefIndex;
	}

	public void setHrefIndex(int hrefIndex) {
		this.hrefIndex = hrefIndex;
	}
}
