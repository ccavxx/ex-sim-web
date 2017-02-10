package com.topsec.tsm.sim.auth.util;

public interface Authority {
	
	public void setSid(SID sid) ;
	/**
	 * 判断用户对指定的主机的日志类型是否具有权限
	 * @param type
	 * @return
	 */
	public boolean hasAuthority(String host,String type) ;
	
}
