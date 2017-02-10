package com.topsec.tsm.sim.sysconfig.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.event.EventRuleGroup;

public interface EventRuleGroupDao   extends BaseDao<EventRuleGroup, Integer>{

	List<EventRuleGroup> getAll();

	Serializable saveObject(Object eventRuleGroup);

	int updateEventRuleGroupById(Integer groupId, Map<String, Object> condition);

	int batchAlterRuleGroupStatus(Integer status, Integer... id);

	int countByCondtion(Map<String, Object> condition);

	EventRuleGroup getEventRuleByName(String evtName);
}
