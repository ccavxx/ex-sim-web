package com.topsec.tsm.sim.sysconfig.service;

import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.type.IpLocation;
import com.topsec.tsm.sim.common.dao.SimOrder;

public interface ResourceService{

	
	public void saveOrUpdateIpLocation(IpLocation ip) ;
	
	public void deleteIpLocation(IpLocation ip) ;
	/**
	 * 批量删除ip地址
	 * @param ids
	 */
	public void deleteIpLocations(Integer... ids) ;
	/**
	 * 检索ip地址定位信息
	 * @param pageIndex
	 * @param pageSize
	 * @param searchCondition
	 * @param orders
	 * @return
	 */
	public PageBean<IpLocation> search(int pageIndex,int pageSize,Map<String,Object> searchCondition,SimOrder... orders) ;
}
