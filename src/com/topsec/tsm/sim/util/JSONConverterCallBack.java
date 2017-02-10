package com.topsec.tsm.sim.util;

import com.alibaba.fastjson.JSONObject;

/**
 * 将对象转换为json后的回调接口<br>
 * @param <T>
 */
public interface JSONConverterCallBack<T> {
	
	/**
	 * 对象转换为json后的回调函数<br>
	 * @param result　转换结果
	 * @param obj　要转换的对象
	 */
	public void call(JSONObject result,T obj) ;
}
