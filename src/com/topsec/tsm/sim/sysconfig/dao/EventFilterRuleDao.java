package com.topsec.tsm.sim.sysconfig.dao;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.event.EventFilterRule;

public interface EventFilterRuleDao extends BaseDao<EventFilterRule, Integer>{

	public EventFilterRule getByUniqueId(String uniqueId) ;
	
}
