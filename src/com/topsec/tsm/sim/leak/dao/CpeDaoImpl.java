package com.topsec.tsm.sim.leak.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.kb.CpeBean;

public class CpeDaoImpl extends HibernateDaoImpl<CpeBean, Integer> implements CpeDao{

	@Override
	public List<CpeBean> getByName(String cpeName) {
		List<CpeBean> cpes = findByCriteria(Restrictions.eq("name", cpeName));
		return cpes;
	}

}
