package com.topsec.tsm.sim.sysconfig.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.event.EventAssocKb;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.EventRuleDispatch;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.event.EventRuleGroupResp;

public interface EventRuleService {
	
	public PageBean<EventRule> search(int pageNum,int pageSize,Map<String,Object> condition,SimOrder... ordres) ;
	
	public List<EventRule> getEventRules();

	public List<EventRule> getEventRulesByCategory(Map<String, Object> categoryMap);

	/**
	 * 
	 * @author zhaojun 2014-3-27下午2:12:07
	 * @param eventRule
	 */
	
	public Integer saveEventRule(EventRule eventRule);
	
	/**
	 * 返回所有可用的事件规则
	 * @return
	 */
	public List<EventRule> getEnableRule() ;

	
	/**
	 * 删除规则配置
	 * @author zhaojun 2014-3-28上午11:56:55
	 * @param idlist
	 * @return
	 */
	public boolean delRuleConfById(List<Integer> idlist);

	/**
	 * 获取一个配置信息
	 * @author zhaojun 2014-3-28下午2:20:27
	 * @param id
	 * @return
	 */
	public EventRule getRuleConfigById(Integer id);

	/**
	 * 更新配置信息
	 * @author zhaojun 2014-4-1下午3:08:40
	 * @param eventRule
	 */
	public void updateEventRule(EventRule eventRule);

	public void batchAlterRuleStatus(Integer status, Integer...id);

	
	//-----------------------------------------------------
	public Integer addEventRuleGroup(EventRuleGroup eventRuleGroup);

	public List<EventRuleGroup> getAllEventRuleGroups();

	public  EventRuleGroup getEventRuleGroupById(Integer id);

	public void associate2EventRuleGroup(EventRuleDispatch... eventRuleDispatch);
	
	public void disassocite2EventRuleGroupByGid(Integer id);
	
	public PageBean<EventRuleGroup> getEventRuleGroupsByPage(int pageNum, int pageSize, Map<String, Object> conditionMap,SimOrder... orders);

	public void delEventRuleGroupById(Integer... id);

	public List<EventRuleGroup> getEnableRuleGroup();

	public List<EventRuleDispatch> getEnableEventRuleDispatch();

	public List<EventRule> getEventRulesByGroupId(Integer id);

	public List<EventRule> getAllDispatchEventRules();
	
	public int updateEventRuleGroup(EventRuleGroup eventRuleGroup);

	public List<EventRuleDispatch> getAllEventDispatch();

	public List<EventRuleDispatch> getEventDispatchByGroupId(Integer id);

	public int batchAlterRuleGroupStatus(Integer status, Integer... id);

	public List<EventRule> getEnableRuleInRuleGroup();

	/**
	 * @author zhaojun  2014-8-11  下午6:23:23
	 */
	public void addEventRuleGroupResp(EventRuleGroupResp...resp);
	
	public void updateEventRuleResponses(Integer ruleId,EventRuleGroupResp... resp) ;
	
	public void delAllGroupRespByGid(int groupId);

	public List<EventRuleGroupResp> getAllRuleGroupResp();

	public List<EventRuleGroup> getEventRuleGroupsByCategory(Map<String, Object> categoryMap);

	public int countEventRuleByConditon(Map<String, Object> condition);

	public int countEventRuleGroupByConditon(Map<String, Object> condition);

	public boolean deleteEventRuleAndDispByGroupId(int groupId);

	public EventRuleGroup getEventRuleByName(String evtName);



}
