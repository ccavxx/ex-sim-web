package com.topsec.tsm.sim.event.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.bean.EventModel;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;

public interface EventDao {
	public static final String HOUR = "hour";
	public static final String DAY = "day";
	public static final String WEEK = "week";
	public static final String MONTH = "month";

	public List<Map<String, Integer>> getEventLevelStatistics(Condition condition)throws SQLException;

	public List<Map<String, Integer>> getEventCategoryStatistics(Condition condition)throws SQLException;

	public List<EventCategory> getAllEventCategorys();

	public Object getEventAlarmCount(Condition condition);
	
	public List<Map> getAlarmCount(Condition condition);
	
	public List<Map<String, Object>> getEventStatistic(Date beginDate,Date endDate);
	
	public List<Map> getEventCount(Condition condition);

	public PageBean<EventModel> getByIp(int pageIndex, int pageSize,String ip, Date startTime, Date endTime);

	public List<Map<String, Object>> getEventNameStatistic( String startTime, String endTime);

	public List<Map<String, Object>> getEventRiverDataByTime(String startTime, String endTime);
}