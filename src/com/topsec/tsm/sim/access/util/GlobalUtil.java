package com.topsec.tsm.sim.access.util;

import java.util.Collection;

/**
 * @ClassName: GlobalUtil
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年6月27日下午7:32:45
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class GlobalUtil {
	public static boolean isNullOrEmpty(Object obj){
		if(null==obj||null==obj.toString()){
			return true;
		}
		return "".equals(obj.toString().trim());
	}
	
	public static boolean isNullOrEmpty(Object[] objArr){
		if(null==objArr||null==objArr.toString()||objArr.length==0){
			return true;
		}
		return "".equals(objArr.toString().trim());
	}
	
	public static boolean isNullOrEmpty(String string){
		if(null==string||string.length()==0){
			return true;
		}
		return "".equals(string.trim());
	}
	
	public static boolean isNullOrEmpty(Collection<?> collection){
		if(null==collection||collection.size()<1||null==collection.toString()){
			return true;
		}
		
		return "".equals(collection.toString().trim());
	}
}
