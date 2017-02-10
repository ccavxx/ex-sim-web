package com.topsec.tsm.sim.sysconfig.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.rule.SimRule;

public interface LogFilterRuleService {

	public PageBean<SimRule> getList(int pageIndex, int pageSize, Map<String, Object> searchCondition, SimOrder... orders);
	
	public void saveSimRule(SimRule rule);
	
	public void updateSimRule(SimRule rule);
	
	public SimRule deleteSimRule(Long id);
	
	public SimRule getSimRule(Long id);

	public List<SimRule> getByDeviceObjectType(String deviceType) ;

	public List<SimRule> getSimRuleByName(String name);
}