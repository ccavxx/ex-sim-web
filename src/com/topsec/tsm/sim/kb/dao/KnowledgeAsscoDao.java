package com.topsec.tsm.sim.kb.dao;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.event.EventAssocKb;

public interface KnowledgeAsscoDao extends BaseDao<EventAssocKb,Integer>{

	/**
	 * 按事件名称删除关联
	 * @author zhaojun 2014-4-25下午3:05:11
	 * @param name
	 */
	public void deleteByEventName(String name);
}
