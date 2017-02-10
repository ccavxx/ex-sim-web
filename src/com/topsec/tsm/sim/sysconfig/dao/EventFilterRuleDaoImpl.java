package com.topsec.tsm.sim.sysconfig.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.event.EventFilterRule;

public class EventFilterRuleDaoImpl extends HibernateDaoImpl<EventFilterRule, Integer> implements EventFilterRuleDao{

	@Override
	public EventFilterRule getByUniqueId(String uniqueId) {
		EventFilterRule rule = findUniqueByCriteria(Restrictions.eq("uniqueId", uniqueId));
		if(rule != null){
			getSession().evict(rule) ;
		}
		return rule ;
	}
	
	@Override
	protected Criterion[] getSearchCriterions(Map<String, Object> searchCondition) {
		List<Criterion> criterion = new ArrayList<Criterion>();
		String creater = (String) searchCondition.get("creater");
		if(StringUtil.isNotBlank(creater)) {
			criterion.add(Restrictions.eq("creater", creater));
		}
		return criterion.toArray(new Criterion[]{});
	}
}
