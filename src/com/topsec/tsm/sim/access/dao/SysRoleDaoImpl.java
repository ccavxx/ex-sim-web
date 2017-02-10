package com.topsec.tsm.sim.access.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.auth.manage.AuthUserRole;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;

/**
 * @ClassName: SysRoleDaoImpl
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2014年6月27日下午4:12:54
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class SysRoleDaoImpl extends HibernateDaoImpl<AuthUserRole, Integer> implements SysRoleDao {

	@Override
	public AuthUserRole findByUniqueAccountId(Integer accountId) {
		return findFirstByCriteria(Restrictions.eq("AccountId", accountId)) ;
	}

	@Override
	public List<AuthUserRole> findByRoleId(Integer roleId) {
		return findByCriteria(Restrictions.eq("RoleId", roleId));
	}

	@Override
	public List<AuthUserRole> findByAccountId(Integer accountId) {
		return findByCriteria(Restrictions.eq("AccountId", accountId));
	}

}
