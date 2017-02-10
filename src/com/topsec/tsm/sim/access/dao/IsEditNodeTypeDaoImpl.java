package com.topsec.tsm.sim.access.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.access.IsEditNodeType;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;

/**
 * @ClassName: IsEditNodeTypeDaoImpl
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年12月1日下午4:18:12
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class IsEditNodeTypeDaoImpl extends HibernateDaoImpl<IsEditNodeType, Integer> implements IsEditNodeTypeDao{

	@Override
	public IsEditNodeType findByUserName(String userName) {
		List<IsEditNodeType> isEditNodeTypes=findByCriteria(Restrictions.eq("userName", userName));
		if (isEditNodeTypes!=null && isEditNodeTypes.size()==1) {
			return isEditNodeTypes.get(0);
		}
		return null;
	}

}
