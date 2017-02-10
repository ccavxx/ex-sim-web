package com.topsec.tsm.sim.access.dao;

import java.util.List;

import com.topsec.tsm.sim.access.SysMenuRoleCorrelation;
import com.topsec.tsm.sim.common.dao.BaseDao;

/**
 * @ClassName: SysMenuRoleCorrelationDao
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年6月27日下午6:37:14
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface SysMenuRoleCorrelationDao extends BaseDao<SysMenuRoleCorrelation,String>{
	public List<SysMenuRoleCorrelation> findByRoleId(Integer roleId);
}
