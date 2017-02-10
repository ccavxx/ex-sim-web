package com.topsec.tsm.sim.access.dao;

import java.util.List;

import com.topsec.tsm.auth.manage.AuthUserRole;
import com.topsec.tsm.sim.common.dao.BaseDao;

/**
 * @ClassName: SysRoleDao
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2014年6月27日下午4:05:48
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface SysRoleDao extends BaseDao<AuthUserRole,Integer>{
	public AuthUserRole findByUniqueAccountId(Integer accountId);
	public List<AuthUserRole>findByRoleId(Integer roleId);
	public List<AuthUserRole>findByAccountId(Integer accountId);
}
