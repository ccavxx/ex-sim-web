package com.topsec.tsm.sim.sysconfig.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.event.EventFilterRule;

public interface EventFilterRuleService {

	public void add(EventFilterRule eventFilterRule);
	
	public List<EventFilterRule> getAll() ;
	
	public void delete(Integer id);
	
	public EventFilterRule get(Integer id);

	public void update(EventFilterRule eventFilterRule);

	public PageBean<EventFilterRule> getList(int pageIndex, int pageSize,Map<String, Object> searchCondition, SimOrder... orders);

}
