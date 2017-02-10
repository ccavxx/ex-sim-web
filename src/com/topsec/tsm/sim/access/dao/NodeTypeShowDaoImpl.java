package com.topsec.tsm.sim.access.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.access.NodeTypeShow;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;

/**
 * @ClassName: NodeTypeShowDaoImpl
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年12月1日下午4:27:24
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class NodeTypeShowDaoImpl extends HibernateDaoImpl<NodeTypeShow, Integer> implements NodeTypeShowDao{

	@Override
	public List<NodeTypeShow> findByUserName(String userName) {
		return findByCriteria(Restrictions.eq("userName", userName));
	}

	@Override
	public NodeTypeShow findByUserNameAndNodeType(String userName,
			String nodeType) {
		List<NodeTypeShow> nodeTypeShows=findByCriteria(Restrictions.eq("userName", userName),Restrictions.eq("nodeType", nodeType));
		if (nodeTypeShows!=null && nodeTypeShows.size()==1) {
			return nodeTypeShows.get(0);
		}//findUniqueByCriteria(Restrictions.eq("userName", userName),Restrictions.eq("nodeType", nodeType));
		return null;
	}

	@Override
	public List<NodeTypeShow> findByUserNameAndIsShow(String userName,
			Boolean isShow) {
		return findByCriteria(Restrictions.eq("userName", userName),Restrictions.eq("isShow", isShow));
	}
	
}
