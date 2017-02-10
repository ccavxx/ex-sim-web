package com.topsec.tsm.sim.report.chart.highchart.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartTable implements java.io.Serializable{
	private boolean drill = false;
	private String [] header;
	private String [] fields;
	private String moreUrl;
	private List<ColumnData> bodyList = new ArrayList<ColumnData>();
	private int total;
	private Map<String, Object>attrsMap =new HashMap<String, Object>();
	private String subSummarize;
	public ChartTable() {
		super();
	}
	
	public ChartTable(boolean drill, String[] header, String[] fields,
			String moreUrl, List<ColumnData> bodyList, int total) {
		super();
		this.drill = drill;
		this.header = header;
		this.fields = fields;
		this.moreUrl = moreUrl;
		this.bodyList = bodyList;
		this.total = total;
	}

	public boolean isDrill() {
		return drill;
	}

	public void setDrill(boolean drill) {
		this.drill = drill;
	}

	public String[] getHeader() {
		return header;
	}

	public void setHeader(String[] header) {
		this.header = header;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public String getMoreUrl() {
		return moreUrl;
	}

	public void setMoreUrl(String moreUrl) {
		this.moreUrl = moreUrl;
	}

	public List<ColumnData> getBodyList() {
		return bodyList;
	}

	public void setBodyList(List<ColumnData> bodyList) {
		this.bodyList = bodyList;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public String getSubSummarize() {
		return subSummarize;
	}

	public void setSubSummarize(String subSummarize) {
		this.subSummarize = subSummarize;
	}

	public Map<String, Object> getAttrsMap() {
		return attrsMap;
	}

	public void setAttrsMap(Map<String, Object> attrsMap) {
		this.attrsMap = attrsMap;
	}
	
	public void putAttrsMap(String key,Object value){
		this.attrsMap.put(key, value);
	}
}
