package com.topsec.tsm.sim.sysconfig.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.event.EventRule;

public interface EventRuleDao extends BaseDao<EventRule, Integer>{
	
	/**
	 * 时间降序获取所有EventRule
	 * @author zhaojun 2014-3-14上午11:56:26
	 * @return
	 */
	public  List<EventRule> getAllByDescCreateTime();

	/**
	 * 获取没有分类的事件规则
	 * @author zhaojun 2014-3-18下午3:25:49
	 * @return
	 */
	public List<EventRule> getEventRulesNoCategory();

	public List<EventRule> getEnableRule();

	public void batchAlterRuleStatus(Integer status, List<Integer> idlist);

	/**
	 * 保存一个对象并返回ID
	 * @author zhaojun 2014-6-16下午5:02:05
	 * @param object
	 * @return
	 */
	public Serializable saveObject(Object object);

	public List<EventRule> getEventRulesByGroupId(Integer id);

	public List<EventRule> getAllEventRuleByDispatch();

	public List<EventRule> getEnableRuleInRuleGroup();

	public int countByCondtion(Map<String, Object> condition);
}
