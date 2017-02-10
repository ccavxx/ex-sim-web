package com.topsec.tsm.sim.event.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.event.CorrelatorCache;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.bean.EventModel;
import com.topsec.tsm.sim.event.dao.EventDao;

public class EventServiceImpl implements EventService {
	private EventDao eventDao;

	public EventDao getEventDao() {
		return eventDao;
	}

	public void setEventDao(EventDao eventDao) {
		this.eventDao = eventDao;
	}

	public Object getEventFromCache(int limit) throws Exception {
		List<Object> result = new ArrayList<Object>();
		LinkedList cache = (LinkedList) CorrelatorCache.getCach();
		int count = cache.size() > limit ? limit : cache.size();
		for (int i = cache.size() - 1; i >= 0; i--) {
			if (count-- == 0)
				break;
			Map<String, Object> map = (Map) cache.get(i);
			EventModel model = EventModel.createEvent(map);
			result.add(model);
		}

		return result;
	}
	
	public PageBean<EventModel> getEventFromCache(int pageIndex,int pageSize){
		List cache = CorrelatorCache.getCach() ;
		PageBean<EventModel> result = new PageBean<EventModel>(pageIndex,pageSize,cache.size()) ;
		int fromIndex = pageSize*(pageIndex-1) ;
		int toIndex = fromIndex+pageSize ;
		if(fromIndex>cache.size()){
			return result ;
		}
		if(toIndex>cache.size()){
			toIndex = cache.size() ;
		}
		List<Map> events = cache.subList(fromIndex, toIndex) ;
		List<EventModel> data = new ArrayList<EventModel>(events.size()) ;
		for(Map event:events){
			data.add(EventModel.createEvent(event)) ;
		}
		result.setData(data) ;
		return result ;
	}

	@Override
	public List<Map<String, Integer>> getEventLevelStatisticByTime(String startTime, String endTime) {
		Condition  condition=new Condition();
		condition.setEnd_time(endTime);
		condition.setStart_time(startTime);
		
		try {
			return eventDao.getEventLevelStatistics(condition);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PageBean<EventModel> getEventByIp(int pageIndex, int pageSize,String ip, Date startTime, Date endTime) {
		return eventDao.getByIp(pageIndex, pageSize, ip,startTime,endTime);
	}

	@Override
	public List<Map<String, Integer>> getEventCategoryStatisticByTime(String startTime, String endTime) {
		Condition  condition=new Condition();
		condition.setEnd_time(endTime);
		condition.setStart_time(startTime);
		try {
			return eventDao.getEventCategoryStatistics(condition);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Object getEventAlarmCount(Condition condition){
		return eventDao.getEventAlarmCount(condition);
	}
	
	public List<Map> getAlarmCount(Condition condition){
		return eventDao.getAlarmCount(condition);
	}
	
	@Override
	public List<Map<String, Object>> getEventStatistic(Date beginDate, Date endDate) {
		return eventDao.getEventStatistic(beginDate, endDate);
	}

	public List<Map> getEventCount(Condition condition){
		return eventDao.getEventCount(condition);
	}

	@Override
	public List<Map<String, Object>> getEventNameStatistic( String startTime, String endTime) {
		return eventDao.getEventNameStatistic(startTime,endTime);
	}
	
	public List<Map<String, Object>> getEventRiverDataByTime(String startTime, String endTime){
		return eventDao.getEventRiverDataByTime(startTime,endTime);
	}
}
