package com.topsec.tsm.sim.access.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.access.SysMenuRoleCorrelation;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;

/**
 * @ClassName: SysMenuRoleCorrelationDaoImpl
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年6月27日下午6:37:46
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class SysMenuRoleCorrelationDaoImpl extends HibernateDaoImpl<SysMenuRoleCorrelation, String> implements SysMenuRoleCorrelationDao {

	@Override
	public List<SysMenuRoleCorrelation> findByRoleId(Integer roleId) {
		return findByCriteria(Restrictions.eq("roleId", roleId));
	}

}
