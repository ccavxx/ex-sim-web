package com.topsec.tsm.sim.sysconfig.dao;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.event.EventRuleDispatch;

public interface EventRuleDispatchDao extends BaseDao<EventRuleDispatch, Integer> {

	int deleteByGroupId(Integer gid);

}
