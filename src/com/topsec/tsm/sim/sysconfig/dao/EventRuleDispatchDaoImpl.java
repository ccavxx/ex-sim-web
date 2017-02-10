package com.topsec.tsm.sim.sysconfig.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.event.EventRuleDispatch;

public class EventRuleDispatchDaoImpl extends HibernateDaoImpl<EventRuleDispatch, Integer> implements EventRuleDispatchDao{

	@Override
	public int deleteByGroupId(Integer gid) {
		return this.getSession().createQuery("delete from EventRuleDispatch where groupId =:groupId")
								.setInteger("groupId", gid)
								.executeUpdate();
	}

	@Override
	public List<EventRuleDispatch> findByCondition(Map<String, Object> condition) {
		Criteria criteria = this.getSession().createCriteria(EventRuleDispatch.class);
		Set<Entry<String, Object>> entryset = condition.entrySet();
		if(entryset!=null){
			for (Entry<String, Object> entry : entryset) {
				criteria=criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
			}
		}
		return criteria.list();
	}
	
}
