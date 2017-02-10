package com.topsec.tsm.sim.log.service;

import java.util.List;
import java.util.Map;

import com.topsec.tal.base.hibernate.LogQueryCondition;
import com.topsec.tsm.sim.log.dao.logQueryConditionDao;

public class LogQueryConditionServiceImp implements LogQueryConditionService {
	private logQueryConditionDao conditionDao;
	
	public logQueryConditionDao getConditionDao() {
		return conditionDao;
	}
	public void setConditionDao(logQueryConditionDao conditionDao) {
		this.conditionDao = conditionDao;
	}
	public void saveQueryCondition(LogQueryCondition searchObject) {
		conditionDao.save(searchObject);
		
	}
	public LogQueryCondition deleteQueryCondition(int id) {
		LogQueryCondition logQueryCondition = conditionDao.findById(id);
		 conditionDao.delete(logQueryCondition);
		 return logQueryCondition;
	}
	@Override
	public List<LogQueryCondition> queryConditionList(
			Map<String, Object> condition) {
		return conditionDao.queryConditionList(condition);
	}
	@Override
	public LogQueryCondition findQueryConditionById(Integer id) {
		return conditionDao.findById(id);
	}
}
