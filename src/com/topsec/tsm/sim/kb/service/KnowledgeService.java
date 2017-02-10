package com.topsec.tsm.sim.kb.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.event.EventAssocKb;
import com.topsec.tsm.sim.kb.KBEvent;
import com.topsec.tsm.sim.kb.bean.KnowledgeQueryBean;

public interface KnowledgeService {
	/**
	 * @method 获取所有事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return List
	 */
	public List<KBEvent> getEvents();
	/**
	 * @method 添加事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return NULL
	 */
	public void addKBEvent(KBEvent event);
	/**
	 * @method 删除事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return NULL
	 */
	public void deleteKBEvent(KBEvent event);
	/**
	 * @method 修改事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return NULL
	 */
	public void updateKBEvent(KBEvent event);
	
	
	public Map<String, Object> getKBEventsByPage(KnowledgeQueryBean knowledgeQueryBean);
	
	
	public boolean deleteKBEventById(Integer... id);
	
	
	public boolean associate2Knowledge(EventAssocKb...assocKbs);
	
	
	/**
	 * 按事件名称获取关联知识
	 * @author zhaojun 2014-4-25下午3:36:36
	 * @param name
	 * @return
	 */
	public List<KBEvent> getAssociatedKnowledgeByEvtName(String name);
	
	/**
	 * 按照规则ID查询规则关联知识
	 * @author zhaojun 2014-5-19下午5:38:07
	 * @param id
	 * @return
	 */
	public List<KBEvent> getAssociatedKnowledgeByEvtRuleId(Integer id);
	
	/**
	 * 删除事件知识关联
	 * @author zhaojun 2014-4-26下午1:13:20
	 * @param name
	 */
	public void deleteKnAssoc(String name);
	
	
	/**
	 * 修改事件知识关联
	 * @author zhaojun 2014-4-26下午1:12:44
	 * @param eventName
	 * @param knid
	 */
	public void updateKnAssocByEvtName(String eventName, Integer... knid);

	public List<KBEvent>  getKnowledgeByCategory(Map<String, Object> categoryMap);
	
	public KBEvent getKnowledgeById(Integer id);
	
	public List<KBEvent> getAssociatedKnowledgeByEvtId(float id);
	
	public List<KBEvent> getAssociatedKnowledgeByGid(Integer id);
	
	public List<KBEvent> getAssociatedKnowledgeByEvtIdAndEndTime(float id, String end_time);
	
	

}
