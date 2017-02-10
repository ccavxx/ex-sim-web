package com.topsec.tsm.sim.kb.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.event.EventAssocKb;

public class KnowledgeAsscoDaoImpl extends HibernateDaoImpl<EventAssocKb, Integer> implements KnowledgeAsscoDao {

	protected static final Logger log=LoggerFactory.getLogger(KnowledgeAsscoDaoImpl.class);
	
	@Override
	public void deleteByEventName(String name) {
		Session session = this.getSession();
		session.createQuery("delete EventAssocKb where event = :evt_name")
		       .setParameter("evt_name", name)
		       .executeUpdate();
	}

	@Override
	public List<EventAssocKb> findByCondition(Map<String, Object> condition) {
		if(condition!=null){
			Set<Entry<String, Object>> entrySet = condition.entrySet();
			Criteria criteria = this.getSession().createCriteria(EventAssocKb.class);
			for (Entry<String, Object> entry : entrySet) {
				criteria=criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
			}
			return criteria.list();
		}
		return null;
	}

}
