package com.topsec.tsm.sim.report.common;

import java.io.Serializable;
import java.util.Map;

public class ReportDataComparable implements Comparable,Serializable{
	
	private Object key;

	public ReportDataComparable() {
		
	}

	public ReportDataComparable(String key) {
		this.key = key;
	}

	@Override
	public int compareTo(Object o) {
		
		return 0;
	}
	
	public int compareTo(Object o1,Object o2) {
		Map map1=(Map)o1;
		Map map2=(Map)o2;
		
		Object value1=map1.get(key);
		Object value2=map2.get(key);
		
		if(value1 instanceof Integer&&value2 instanceof Integer){
			Integer value1Integer=(Integer)value1;
			Integer value2Integer=(Integer)value2;
			if(value1Integer.compareTo(value2Integer)<0){
				return -1;
			}else if(value1Integer.compareTo(value2Integer)==0){
				return 0;
			}else{
				return 1;
			}
		}else if(value1 instanceof Long&&value2 instanceof Long){
			Long value1Long=(Long)value1;
			Long value2Long=(Long)value2;
			if(value1Long.compareTo(value2Long)<0){
				return -1;
			}else if(value1Long.compareTo(value2Long)==0){
				return 0;
			}else{
				return 1;
			}
		}else if(value1 instanceof Float&&value2 instanceof Float){
			Float value1Float=(Float)value1;
			Float value2Float=(Float)value2;
			if(value1Float.compareTo(value2Float)<0){
				return -1;
			}else if(value1Float.compareTo(value2Float)==0){
				return 0;
			}else{
				return 1;
			}
		}else if(value1 instanceof Double&&value2 instanceof Double){
			Double value1Double=(Double)value1;
			Double value2Double=(Double)value2;
			if(value1Double.compareTo(value2Double)<0){
				return -1;
			}else if(value1Double.compareTo(value2Double)==0){
				return 0;
			}else{
				return 1;
			}
		}
		return 0;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}
	
}
