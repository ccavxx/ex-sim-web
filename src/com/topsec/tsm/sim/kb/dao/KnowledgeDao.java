package com.topsec.tsm.sim.kb.dao;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.kb.KBEvent;

public interface KnowledgeDao  extends BaseDao<KBEvent,Integer>{

	public List<KBEvent> findByPages(Map<String, Object> cmap,int page,int rows);
	public Integer getCountByCondition(Map<String, Object> cmap);
	public List<KBEvent> findByEvtName(String name);
	public List<KBEvent> getKnowledgesNoCategory();
	public List<KBEvent> findByEvtRuleId(Integer id);
	public List<KBEvent> findByEvtId(float id);
	public List<KBEvent> findByGid(Integer id); 
	public List<KBEvent> findByEvtIdAndEndTime(float id, String end_time);
	
}
