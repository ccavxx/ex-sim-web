package com.topsec.tsm.sim.log.web;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;

public class UnitFormatter {

	public static final long KB = 1024L ;
	public static final long MB = KB*1024 ;
	public static final long GB = MB*1024 ;
	public static final long TB = GB*1024 ;
	/**万*/
	public static final long WAN = 10000 ;
	/**亿*/
	public static final long YI = 100000000 ;
	
	public static String format(JSONArray data,String funField,String fun){
		String unit = "";
		if(ObjectUtils.isEmpty(data)){
			return unit;
		}
		//根据平均值来决定格式化标准
		BigInteger sum = new BigInteger("0") ;
		for(Object obj:data){
			JSONObject record = (JSONObject)obj ;
			long value = ((Number) record.get(fun)).longValue() ; 
			sum = sum.add(BigInteger.valueOf(value)) ; 
		}
		long avg = sum.divide(BigInteger.valueOf((long)data.size())).longValue() ;
		//流量求和、最小值、最大值
		long unitStandard ;
		if(ObjectUtils.equalsAny(funField, "BYTES_IN","BYTES_OUT") && (fun.equalsIgnoreCase("sum")||fun.equals("max")||fun.equals("min"))){
			if(avg > TB){
				unit = "TB" ;
				unitStandard = TB ;
			}else if(avg > GB){
				unit = "GB" ;
				unitStandard = GB ;
			}else if(avg > MB){
				unit = "MB" ;
				unitStandard = MB ;
			}else if(avg > KB){
				unit = "KB" ;
				unitStandard = KB ;
			}else{
				return unit;
			}
		}else{
			if(avg > YI){
				unit = "亿" ;
				unitStandard = YI ;
			}else if(avg > WAN){
				unit = "万" ;
				unitStandard = WAN ;
			}else{
				return unit;
			}
		}
		for(Object obj:data){
			JSONObject record = (JSONObject)obj ;
			double value = ((Number) record.get(fun)).doubleValue() ; 
			double val = value / unitStandard ;
			if(val < 1){
				if(val >= 0.01){
					record.put(fun,ObjectUtils.round(val)) ;
				}else if(val >= 0.001){
					record.put(fun,ObjectUtils.round(val,3)) ;
				}else if(val >= 0.0001){
					record.put(fun,ObjectUtils.round(val,4)) ;
				}else{
					record.put(fun,ObjectUtils.round(val,5)) ;
				}
			}else{
				record.put(fun,ObjectUtils.round(val)) ;
			}
		}
		return unit ;
	}

}
