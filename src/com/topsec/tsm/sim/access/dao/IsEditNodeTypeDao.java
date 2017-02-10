package com.topsec.tsm.sim.access.dao;

import com.topsec.tsm.sim.access.IsEditNodeType;
import com.topsec.tsm.sim.common.dao.BaseDao;

/**
 * @ClassName: IsEditNodeTypeDao
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年12月1日下午4:09:36
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface IsEditNodeTypeDao extends BaseDao<IsEditNodeType, Integer> {
	public IsEditNodeType findByUserName(String userName);
}
