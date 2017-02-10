package com.topsec.tsm.sim.asset.dao;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;

public class TopoDaoImpl extends HibernateDaoImpl<AssTopo, Integer> implements TopoDao {
	
	@Override
	public void saveOrUpdate(AssTopo topo) {
		getSession().saveOrUpdate(topo) ;
	}

	@Override
	public AssTopo getByName(String name) {
		AssTopo topo = findUniqueByCriteria(Restrictions.eq("name", name)) ;
		if (topo != null) {
			getSession().evict(topo) ;
		}
		return topo ;
	}

	@Override
	public List<AssTopo> getUserTopoList(String userName) {
		if(StringUtil.isNotBlank(userName)){
			return findByCriteria(Restrictions.eq("owner", userName)) ;
		}
		return getAll();
	}
	
}
