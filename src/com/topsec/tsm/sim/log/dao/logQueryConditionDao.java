package com.topsec.tsm.sim.log.dao;

import java.util.List;
import java.util.Map;

import com.topsec.tal.base.hibernate.LogQueryCondition;
import com.topsec.tsm.sim.common.dao.BaseDao;
public interface logQueryConditionDao extends BaseDao<LogQueryCondition, Integer>{
	
	public List<LogQueryCondition>  queryConditionList(Map<String, Object> condition);
}
