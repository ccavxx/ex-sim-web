package com.topsec.tsm.sim.newreport.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;





import java.util.Map.Entry;

import com.topsec.tsm.sim.access.util.GlobalUtil;

/**
 * @ClassName: ResultOperatorUtils
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年9月15日上午11:02:04
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ResultOperatorUtils {
	private static final String[] OPERATOR_SYMBOLS={">=","<=","!=","<>",">","<","="};
	/**
	 * 
	 * @param dataStructure
	 */
	public static Map<String, Object> datStructure(String dataStructure){
		//dataStructure eg:{categorys:{PRIORITY~危险级别},series:{PRIORITY~危险级别},statistical:{OPCOUNT~计数}}
		if (GlobalUtil.isNullOrEmpty(dataStructure)) {
			return null;
		}
		dataStructure=dataStructure.trim();
		dataStructure=dataStructure.substring(dataStructure.indexOf("{", 0)+1, dataStructure.lastIndexOf("}") );
		String[]targets=dataStructure.split("\\,");
		
		Map<String, Object> map=new HashMap<String, Object>();
		
		for (String targetStr : targets) {
			String[]strs=targetStr.split(":");
			if (2 != strs.length) {
				continue;
			}
			String string=strs[0];
			if (GlobalUtil.isNullOrEmpty(string)) {
				continue;
			}
			string=string.trim();
			
			String value=strs[1];
			value=value.substring(value.indexOf("{")+1, value.lastIndexOf("}") );
			
			String[]strings=value.split(";");
			
			String []strName=new String[strings.length];
			String []strCNName=new String[strings.length];
			String []strType=new String[strings.length];
			
			for (int i=0;i<strings.length;i++) {
				String []props=strings[i].split("~");
				if (3 != props.length) {
					continue;
				}
				strName[i]=props[0];
				strCNName[i]=props[1];
				strType[i]=props[2];
			}
			
			if ("categorys".equalsIgnoreCase(string)) {
				map.put("categorys", strName);
				map.put("categorysCNName", strCNName);
				map.put("categorysType", strType);
			}
			if ("series".equalsIgnoreCase(string)) {
				map.put("series", strName);
				map.put("seriesCNName", strCNName);
				map.put("seriesType", strType);
			}
			if ("statistical".equalsIgnoreCase(string)) {
				map.put("statistical", strName);
				map.put("statisticalCNName", strCNName);
				map.put("statisticalType", strType);
			}
			if ("formats".equalsIgnoreCase(string)) {
				map.put("formats", strName);
				map.put("formatsFileName", strCNName);
				map.put("formatsType", strType);
			}
			
		}
		return map;
	}
	
	public static Object mapping(String type,Object fromobj){
		if (GlobalUtil.isNullOrEmpty(fromobj)) {
			return fromobj;
		}
		Object resultObj=null;
		if ("WINDOWS_OS_EVENT_MAPPING".equals(type)) {
			boolean isopersuccess=false;
			for (Entry<Object, Object> entry : ResourceContainer.WINOS_MAPPING_PROP.entrySet()) {
				String key=(String)entry.getKey();
				String value=(String)entry.getValue();
				if (value.contains(","+fromobj+",")) {
					return key;
				}else {
					int fromVal=Integer.valueOf(fromobj.toString());
					int wsign=value.lastIndexOf("?");
					while (wsign>0){
						int comma=value.indexOf(",", wsign);
						int sublen=value.length();
						if (-1 !=comma) {
							sublen=comma;
						}
						value=value.substring(0, sublen);
						comma=value.lastIndexOf(",");
						
						String operatorString=null;
						if (-1!=comma) {
							operatorString=value.substring(comma+1);
						}else {
							operatorString=value;
						}
						if (null !=operatorString) {
							int qsymbol=operatorString.indexOf("?");
							if (qsymbol !=operatorString.length()) {
								String tmp1=operatorString.substring(0,qsymbol);
								String tmp2=operatorString.substring(qsymbol+1);
								
								if(isBelong(tmp1,fromVal,"right") && isBelong(tmp2,fromVal,"left")){
									isopersuccess=true;
									return key;
								}
							}else if(isBelong(operatorString,fromVal,"right")){
								isopersuccess=true;
								return key;
							}
						}
						if (-1 != comma) {
							value=value.substring(0,comma);
							wsign=value.lastIndexOf("?");
						}else {
							value="";
							wsign=-1;
						}
					}
				}
			}
			if (!isopersuccess) {
				resultObj=ResourceContainer.WINOS_OTHER_MAPPING_PROP.get(fromobj);
				return null==resultObj?fromobj:resultObj;
			}
		}
		
		if ("IP_MAPPING".equals(type)) {
			resultObj=ResourceContainer.IP_MAPPING_PROP.get(fromobj);
			return null==resultObj?fromobj:resultObj;
		}
		if ("PROPERTY_MAPPING".equals(type)) {
			resultObj= ResourceContainer.PROPERTY_MAPPING_PROP.get(fromobj);
			return null==resultObj?fromobj:resultObj;
		}
		if ("OTHER_MAPPING".equals(type)) {
			resultObj= ResourceContainer.OTHER_MAPPING_PROP.get(fromobj);
			return null==resultObj?fromobj:resultObj;
		}
		if ("EVERY_MAPPING_PATH".equals(type)) {
			resultObj= ResourceContainer.EVERY_MAPPING_PROP.get(fromobj);
			return null==resultObj?fromobj:resultObj;
		}
		return fromobj;
	}
	/**
	 * 
	 * @param longval
	 * @param units 单位 B Kb Mb Gb Tb Pb ..
	 * @return
	 */
	public static Double flowOperater(long longval,String units){
		
		if(longval>=Long.MAX_VALUE 
				|| "UNKNOW".equalsIgnoreCase(units)){
			return -1.0;
		}
		if("eb".equalsIgnoreCase(units)){
			return new Double((new BigDecimal((longval/(1024*1024*1024.0))/(1024*1024*1024))).setScale(3,BigDecimal.ROUND_HALF_UP).toString()); 
		}
		if("pb".equalsIgnoreCase(units)){
			return new Double((new BigDecimal((longval/(1024*1024*1024.0))/(1024*1024))).setScale(3,BigDecimal.ROUND_HALF_UP).toString());
		}
		if("tb".equalsIgnoreCase(units)){
			return new Double((new BigDecimal((longval/(1024*1024.0))/(1024*1024))).setScale(3,BigDecimal.ROUND_HALF_UP).toString());
		}
		if("gb".equalsIgnoreCase(units)){
			return new Double((new BigDecimal(longval/(1024*1024*1024.0))).setScale(3,BigDecimal.ROUND_HALF_UP).toString());
		}
		if("mb".equalsIgnoreCase(units)){
			return new Double((new BigDecimal(longval/(1024*1024.0))).setScale(3,BigDecimal.ROUND_HALF_UP).toString());
		}
		if("kb".equalsIgnoreCase(units)){
			return new Double((new BigDecimal(longval/1024.0)).setScale(3,BigDecimal.ROUND_HALF_UP).toString());
		}
		
		return longval/1.0;
	}
	
	/**
	 * 
	 * @param data
	 * @param units 单位 一、万 、亿、万亿、亿亿
	 * @return
	 */
	public static Double showNumberOperater(long longval,String units){
		
		if(longval>=Long.MAX_VALUE ||
				"UNKNOW".equalsIgnoreCase(units)){
			return -1.0;
		}
		if("亿亿".equalsIgnoreCase(units)){
			return (longval/(10000*10000.0))/(10000*10000);
		}
		if("万亿".equalsIgnoreCase(units)){
			return (longval/(10000*10000.0))/(10000);
		}
		if("亿".equalsIgnoreCase(units)){
			return longval/(10000*10000.0);
		}
		if("万".equalsIgnoreCase(units)){
			return longval/(10000.0);
		}
		
		return longval/1.0;
	}
	
	public static String getUnit(long longval,String dataType){
		if(longval>=Long.MAX_VALUE){
			return "UNKNOW";
		}
		if ("COUNT_NO".equals(dataType)) {
			if((longval/(10000*10000.0))/(10000*10000)>10){
				return "亿亿";
			}else if((longval/(10000*10000.0))/(10000)>10){
				return "万亿";
			}else if(longval/(10000*10000.0)>10){
				return "亿";
			}else if(longval/10000.0>10){
				return "万";
			}
		}else if ("FLOW_NO".equals(dataType)) {
			if ((longval/(1024*1024*1024.0))/(1024*1024*1024)>=1) {
				return "eb";
			}else if ((longval/(1024*1024*1024.0))/(1024*1024)>=1) {
				return "pb";
			}else if ((longval/(1024*1024*1024.0))/1024>=1) {
				return "tb";
			}else if (longval/(1024*1024*1024.0)>=1) {
				return "gb";
			}else if (longval/(1024*1024.0)>=1) {
				return "mb";
			}else if (longval/1024.0>=1) {
				return "kb";
			}else {
				return "b";
			}
		}
		return null;
	}
	
	private static boolean isBelong(String string,Integer num,String qmarkPos){
    	for (int i = 0; i < OPERATOR_SYMBOLS.length; i++) {
			int symbolpo=string.indexOf(OPERATOR_SYMBOLS[i]);
			if (symbolpo>=0) {
				if ("right".equalsIgnoreCase(qmarkPos)) {
					String numberString=string.substring(0,symbolpo);
					int number=Integer.valueOf(numberString);
					return expressionBooleanValue(i,number,num);
				}
				if ("left".equalsIgnoreCase(qmarkPos)) {
					String numberString=string.substring(symbolpo+OPERATOR_SYMBOLS[i].length());
					int number=Integer.valueOf(numberString);
					return expressionBooleanValue(i,num,number);
				}
				
			}
		}
    	return false;
    }
    
    private static boolean expressionBooleanValue(int i,int number,int num){
    	switch (i) {
		case 0:
			if (number>=num) {
				return true;
			}
			return false;
		case 1:
			if (number<=num) {
				return true;
			}
			return false;
		case 2:
			if (number!=num) {
				return true;
			}
			return false;
		case 3:
			if (number!=num) {
				return true;
			}
			return false;
		case 4:
			if (number>num) {
				return true;
			}
			return false;
		case 5:
			if (number<num) {
				return true;
			}
			return false;
		case 6:
			if (number==num) {
				return true;
			}
			return false;
		}
    	return false;
    }
    public static String riskCnName(int val){
    	if(0 == val)return "无危险";
    	else if(1 == val)return "低危险";
    	else if(2 == val)return "中危险";
    	else if(3 == val)return "高危险";
    	else if(4 == val)return "非常危险";
    	return ""+val;
    }
}
