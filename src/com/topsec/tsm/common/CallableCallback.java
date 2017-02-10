package com.topsec.tsm.common;

/**
 * Callable接口回调函数
 * 在Callable.call执行完成，结果返回以前调用此函数，访问返回结果
 * @author hp
 *
 * @param <T>
 */
public interface CallableCallback<T> {

	/**
	 * 回调方法，此callable接口执行完成以后，调用此方法访问返回结果
	 * @param callResult
	 */
	public void callback(T callResult) ;
}
