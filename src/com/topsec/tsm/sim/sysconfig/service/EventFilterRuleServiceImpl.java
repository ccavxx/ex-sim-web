package com.topsec.tsm.sim.sysconfig.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.node.component.handler.EventConfiguration;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.event.EventFilterRule;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.sysconfig.dao.EventFilterRuleDao;

public class EventFilterRuleServiceImpl implements EventFilterRuleService {

	private EventFilterRuleDao eventFilterRuleDao ;
	private NodeMgrFacade nodeMgrFacade ;
	
	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}

	public void setEventFilterRuleDao(EventFilterRuleDao eventFilterRuleDao) {
		this.eventFilterRuleDao = eventFilterRuleDao;
	}

	@Override
	public void add(EventFilterRule eventFilterRule) {
		if(eventFilterRuleDao.getByUniqueId(eventFilterRule.getUniqueId()) != null){
			throw new CommonUserException("相同条件的事件过滤规则已经存在！") ;
		}
		eventFilterRuleDao.save(eventFilterRule) ;
		dispatch() ;
	}

	@Override
	public List<EventFilterRule> getAll() {
		return eventFilterRuleDao.getAll();
	}

	@Override
	public void delete(Integer id) {
		eventFilterRuleDao.delete(id) ;
		dispatch() ;
	}

	@Override
	public EventFilterRule get(Integer id) {
		return eventFilterRuleDao.findById(id);
	}

	@Override
	public void update(EventFilterRule eventFilterRule) {
		EventFilterRule rule = eventFilterRuleDao.getByUniqueId(eventFilterRule.getUniqueId()) ;
		if(rule != null && !rule.getId().equals(eventFilterRule.getId())){
			throw new CommonUserException("相同条件的事件过滤规则已经存在！") ;
		}
		eventFilterRule.setCreater(rule.getCreater());
		eventFilterRuleDao.update(eventFilterRule) ;
		dispatch() ;
	}
	
	private void dispatch(){
		try {
			List<EventFilterRule> rules = eventFilterRuleDao.getAll() ;
			Node auditorNode = nodeMgrFacade.getKernelAuditor(false, false, true, true) ;
			Component component = nodeMgrFacade.getBindableComponentByType(auditorNode, NodeDefinition.HANDLER_EVENTDETECT, true);
			if(component == null){
				throw new CommonUserException("没有找到组件！") ;
			}
			EventConfiguration config = nodeMgrFacade.getSegmentConfigByClass(component, EventConfiguration.class) ;
			config.setFilters(rules) ;
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, config) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PageBean<EventFilterRule> getList(int pageIndex, int pageSize,Map<String, Object> searchCondition, SimOrder... orders){
		return eventFilterRuleDao.search(pageIndex, pageSize, searchCondition, orders);
	}
}
