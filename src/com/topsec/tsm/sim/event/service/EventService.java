package com.topsec.tsm.sim.event.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.bean.EventModel;

public interface EventService {
	/**
	 * 从缓存中获取指定数量的事件
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public Object getEventFromCache(int limit) throws Exception;
	
	public List<Map<String, Integer>> getEventLevelStatisticByTime(String startTime, String endTime);
	
	public PageBean<EventModel> getEventFromCache(int pageIndex,int pageSize)  ;
	/**
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param ip
	 * @return
	 */
	public PageBean<EventModel> getEventByIp(int pageIndex,int pageSize,String ip,Date startTime,Date endTime) ;
	public List<Map<String, Integer>> getEventCategoryStatisticByTime(String startTime, String endTime);
	
	public Object getEventAlarmCount(Condition condition);
	
	public List<Map> getAlarmCount(Condition condition);
	
	public List<Map<String, Object>> getEventStatistic(Date beginDate,Date endDate);
	
	public List<Map> getEventCount(Condition condition);

	public List<Map<String, Object>> getEventNameStatistic( String startTime, String endTime);

	public List<Map<String, Object>> getEventRiverDataByTime(String startTime, String endTime);
}