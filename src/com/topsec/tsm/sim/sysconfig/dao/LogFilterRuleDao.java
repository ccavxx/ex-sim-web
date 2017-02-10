package com.topsec.tsm.sim.sysconfig.dao;

import java.util.List;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.rule.SimRule;

public interface LogFilterRuleDao extends BaseDao<SimRule, Long>{

	List<SimRule> findByDeviceType(String deviceType);

	List<SimRule> findSimRuleByName(String name);

}