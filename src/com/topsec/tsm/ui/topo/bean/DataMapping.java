package com.topsec.tsm.ui.topo.bean;

import java.util.Map;
import java.util.TreeMap;

public class DataMapping {
	
	private String id;
	
	private String severity;
	
	private Long total;
	
	private Map<String,Map<String,Long>> dataMappingMap = new TreeMap<String,Map<String,Long>>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}
	
	public void add(String id,String severity,Long total){
		Map<String,Long> map  = dataMappingMap.get(id);
		if(map == null){
			map = new TreeMap<String,Long>();
			dataMappingMap.put(id, map);
		}
		map.put(severity, total);
	}

	public Map<String, Map<String, Long>> getDataMappingMap() {
		return dataMappingMap;
	}

	public void setDataMappingMap(Map<String, Map<String, Long>> dataMappingMap) {
		this.dataMappingMap = dataMappingMap;
	}

}
