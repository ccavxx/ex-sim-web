package com.topsec.tsm.sim.sysconfig.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.EventRuleDispatch;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.event.EventRuleGroupResp;
import com.topsec.tsm.sim.kb.dao.KnowledgeAsscoDao;
import com.topsec.tsm.sim.sysconfig.dao.EventRuleDao;
import com.topsec.tsm.sim.sysconfig.dao.EventRuleDispatchDao;
import com.topsec.tsm.sim.sysconfig.dao.EventRuleGroupDao;
import com.topsec.tsm.sim.sysconfig.dao.EventRuleGroupRespDao;


public class EventRuleServiceImpl implements EventRuleService {

	
	protected static final Logger log=LoggerFactory.getLogger(EventRuleServiceImpl.class);
	private EventRuleDao eventRuleDao;
	private KnowledgeAsscoDao knowledgeAsscoDao;
	private EventRuleGroupDao eventRuleGroupDao;
	private EventRuleDispatchDao eventRuleDispatchDao;
	private EventRuleGroupRespDao eventRuleGroupRespDao;

	public void setEventRuleDao(EventRuleDao eventRuleDao) {
		this.eventRuleDao = eventRuleDao;
	}

	public void setKnowledgeAsscoDao(KnowledgeAsscoDao knowledgeAsscoDao) {
		this.knowledgeAsscoDao = knowledgeAsscoDao;
	}

	public EventRuleGroupDao getEventRuleGroupDao() {
		return eventRuleGroupDao;
	}

	public void setEventRuleGroupDao(EventRuleGroupDao eventRuleGroupDao) {
		this.eventRuleGroupDao = eventRuleGroupDao;
	}

	public void setEventRuleDispatchDao(EventRuleDispatchDao eventRuleDispatchDao) {
		this.eventRuleDispatchDao = eventRuleDispatchDao;
	}

	/**
	 * @param  
	 */
	public void setEventRuleGroupRespDao(EventRuleGroupRespDao eventRuleGroupRespDao) {
		this.eventRuleGroupRespDao = eventRuleGroupRespDao;
	}

	/**
	 * @return the eventRuleGroupRespDao
	 */
	/*public EventRuleGroupRespDao getEventRuleGroupRespDao() {
		return eventRuleGroupRespDao;
	}
*/
	@Override
	public PageBean<EventRule> search(int pageNum, int pageSize, Map<String, Object> condition,SimOrder... orders) {
		return eventRuleDao.search(pageNum, pageSize, condition,orders);
	}

	@Override
	public List<EventRule> getEventRules() {
		return eventRuleDao.getAllByDescCreateTime();
	}

	@Override
	public List<EventRule> getEventRulesByCategory(Map<String, Object> categoryMap) {
		if(categoryMap==null||categoryMap!=null&&categoryMap.size()==0){
			return eventRuleDao.getEventRulesNoCategory();
		} 
		return	eventRuleDao.findByCondition(categoryMap);
	}

	@Override
	public Integer saveEventRule(EventRule eventRule) {
		return (Integer) eventRuleDao.save(eventRule);
	}

 

	@Override
	public List<EventRule> getEnableRule() {
		return eventRuleDao.getEnableRule();
	}

	@Override
	public boolean delRuleConfById(List<Integer> idlist) {
		try {
			if(idlist!=null){
				for (Integer id : idlist) {
					EventRule rule = eventRuleDao.findById(id);
					if(rule!=null){
						eventRuleDao.delete(id);
						knowledgeAsscoDao.deleteByEventName(rule.getName());
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public EventRule getRuleConfigById(Integer id) {
		return eventRuleDao.findById(id);
	}

	@Override
	public void updateEventRule(EventRule eventRule) {
		eventRuleDao.update(eventRule);
	}

	@Override
	public void batchAlterRuleStatus(Integer status, Integer... id) {
		List<Integer> idlist=new ArrayList<Integer>();
		Collections.addAll(idlist, id);
		eventRuleDao.batchAlterRuleStatus(status,idlist);
	}

	@Override
	public Integer addEventRuleGroup(EventRuleGroup eventRuleGroup) {
		
		return  (Integer) eventRuleGroupDao.saveObject(eventRuleGroup);
	}

	@Override
	public List<EventRuleGroup> getAllEventRuleGroups() {
		return eventRuleGroupDao.getAll();
	}

	@Override
	public void associate2EventRuleGroup(EventRuleDispatch... eventRuleDispatch) {
		
		List<EventRuleDispatch> list=new ArrayList<EventRuleDispatch>();
		Collections.addAll(list, eventRuleDispatch);
		eventRuleDispatchDao.batchSave(list);
		//eventRuleGroupDao.saveObject(eventRuleDispatch);
	}

	@Override
	public PageBean<EventRuleGroup> getEventRuleGroupsByPage(int pageNum, int pageSize, Map<String, Object> conditionMap,SimOrder... orders) {
		return eventRuleGroupDao.search(pageNum, pageSize, conditionMap,orders);
 
		 
	}

	@Override
	public void delEventRuleGroupById(Integer... id) {
		for (int i = 0; i < id.length; i++) {
			List<EventRule> eventRules = eventRuleDao.getEventRulesByGroupId(id[i]);
			if(eventRules!=null){
				for (EventRule eventRule : eventRules) {
					eventRuleDao.delete(eventRule);//级联删除规则关联关系 event_rule_dispatch
				}
			}
			eventRuleGroupDao.delete(id[i]);//删除规则组
		}
	}

	/**
	 * 暂时以规则作为启用禁用条件
	 */
	@Override
	public List<EventRuleGroup> getEnableRuleGroup() {
		Map<String, Object> condition=new HashMap<String, Object>();
		condition.put("status", 1);
		return eventRuleGroupDao.findByCondition(condition);
	}

	@Override
	public List<EventRuleDispatch> getEnableEventRuleDispatch() {
		return eventRuleDispatchDao.getAll();
	}

	@Override
	public List<EventRule> getEventRulesByGroupId(Integer id) {
		return eventRuleDao.getEventRulesByGroupId(id);
	}

	@Override
	public EventRuleGroup getEventRuleGroupById(Integer id) {
		return eventRuleGroupDao.findById(id);
	}

	@Override
	public int updateEventRuleGroup(EventRuleGroup eventRuleGroup) {
		Map<String,Object> condition=new HashMap<String, Object>();
		Class<? extends EventRuleGroup> clazz = eventRuleGroup.getClass();
		Field[] fields = eventRuleGroup.getClass().getDeclaredFields();
	 
		for (int i = 0; i < fields.length; i++) {
			 String fieldName=fields[i].getName();
			 String methodName="get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
			 try {
				Method method = clazz.getMethod(methodName);
				Object value = method.invoke(eventRuleGroup, null);
				if(value!=null){
					condition.put(fieldName, value);
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return eventRuleGroupDao.updateEventRuleGroupById(eventRuleGroup.getGroupId(),condition);
		//return eventRuleGroupDao.updateProperty(eventRuleGroup.getGroupId(), "groupName", eventRuleGroup.getGroupName());
	}

	@Override
	public void disassocite2EventRuleGroupByGid(Integer gid) {
		eventRuleDispatchDao.deleteByGroupId(gid);
	}

	@Override
	public List<EventRuleDispatch> getAllEventDispatch() {
		return eventRuleDispatchDao.getAll();
	}

	@Override
	public List<EventRuleDispatch> getEventDispatchByGroupId(Integer id) {
		Map<String, Object> condition=new HashMap<String, Object>();
		condition.put("groupId", id);
		return eventRuleDispatchDao.findByCondition(condition);
	}

	@Override
	public int batchAlterRuleGroupStatus(Integer status, Integer... id) {
		return eventRuleGroupDao.batchAlterRuleGroupStatus(status,id);
	}

	@Override
	public List<EventRule> getAllDispatchEventRules() {
		return eventRuleDao.getAllEventRuleByDispatch();
	}

	@Override
	public List<EventRule> getEnableRuleInRuleGroup() {
		// TODO Auto-generated method stub
		return eventRuleDao.getEnableRuleInRuleGroup();
	}
	@Override
	public void addEventRuleGroupResp(EventRuleGroupResp... resp) {
		if(resp!=null&&resp.length>0){
			ArrayList<EventRuleGroupResp> list = new ArrayList<EventRuleGroupResp>();
			Collections.addAll(list, resp);
			this.eventRuleGroupRespDao.batchSave(list);
		}
	}

	@Override
	public void updateEventRuleResponses(Integer ruleId,EventRuleGroupResp... responses) {
		delAllGroupRespByGid(ruleId) ;
		addEventRuleGroupResp(responses) ;
	}

	@Override
	public void delAllGroupRespByGid(int groupId) {
		this.eventRuleGroupRespDao.deleteRuleGroupRspByGid(groupId);
	}

	@Override
	public List<EventRuleGroupResp> getAllRuleGroupResp() {
		return this.eventRuleGroupRespDao.getAll();
	}
 
	@Override
	public List<EventRuleGroup> getEventRuleGroupsByCategory(
			Map<String, Object> categoryMap) {
		 
		return eventRuleGroupDao.findByCondition(categoryMap);
	}

 
	@Override
	public int countEventRuleByConditon(Map<String, Object> condition) {
		return eventRuleDao.countByCondtion(condition);
	}

	 
	@Override
	public int countEventRuleGroupByConditon(Map<String, Object> condition) {
		return eventRuleGroupDao.countByCondtion(condition);
	}

	@Override
	public boolean deleteEventRuleAndDispByGroupId(int groupId) {
		
		try {
			Map<String, Object> condition=new HashMap<String, Object>();
			condition.put("groupId", groupId);
			List<EventRuleDispatch> dispatchs=eventRuleDispatchDao.findByCondition(condition);
			if(dispatchs!=null){
				for (EventRuleDispatch eventRuleDispatch : dispatchs) {
					eventRuleDao.delete(eventRuleDispatch.getRuleId());//级联删除关联
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public EventRuleGroup getEventRuleByName(String evtName) {
		return eventRuleGroupDao.getEventRuleByName(evtName);
	}
	
}
