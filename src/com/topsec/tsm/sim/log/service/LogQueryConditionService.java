package com.topsec.tsm.sim.log.service;

import java.util.List;
import java.util.Map;

import com.topsec.tal.base.hibernate.LogQueryCondition;


public interface LogQueryConditionService {
	/**
	 * 保存查询条件
	 * @param object
	 */
	public void saveQueryCondition(LogQueryCondition object);
	/**
	 * 删除日志查询条件
	 * @param id
	 * @return
	 */
	public LogQueryCondition deleteQueryCondition(int id);
	/**
	 * 获取查询条件列表
	 * @param condition
	 * @return
	 */
	public List<LogQueryCondition>  queryConditionList(Map<String, Object> condition);
	/**
	 * 根据id获取一个对象信息
	 * @param id
	 * @return
	 */
	public LogQueryCondition findQueryConditionById(Integer id);
}
