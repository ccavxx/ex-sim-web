package com.topsec.tsm.sim.alarm.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.alarm.bean.AlarmQueryCriteria;
import com.topsec.tsm.sim.alarm.persistence.AlarmCategory;
import com.topsec.tsm.sim.alarm.persistence.SimAlarm;

public interface AlarmDao {
	public static final String HOUR = "hour";
	public static final String DAY = "day";
	public static final String WEEK = "week";
	public static final String MONTH = "month";
	public static final String[] LEVEL = { "非常低", "低", "中", "高", "非常高" };
	public abstract List getLevelData(String timeUnit) throws Exception;
	
	
	public abstract List<Map<String, Object>> getAlarmLevelStatisticByTime(Date stime, Date etime);


	public abstract List<Map<String, Object>> getDevAlarmStatisticByTime(Date startTime, Date endTime);


	public abstract List<Map<String, Object>> getDayStatisticByTime(Date startTime, Date endTime);

	
/*	*//**
	 * 按条件查询告警
	 * @author zhaojun 2014-3-11下午5:22:50
	 * @param alermq
	 * @return
	 *//*
	public abstract List<SimAlarm> getAlarmPage(AlarmQueryCriteria alermq);
	
	*//**
	 * 按条件统计告警总数
	 * @author zhaojun 2014-3-11下午5:23:00
	 * @param alermq
	 * @return
	 *//*
	public abstract int getAlarmCount(AlarmQueryCriteria alermq);*/
	/**
	 * 根据ip地址查询告警信息
	 * @param pageIndex
	 * @param pageSize
	 * @param ip
	 */
	public abstract List<SimAlarm> getByIp(int pageIndex, int pageSize, String ip);

	public abstract PageBean<SimAlarm> getPageByIp(int pageIndex, int pageSize, String ip, Map<String, Object> params);

	public abstract List<AlarmCategory> getAlarmCategories();

	public abstract List<Map<String, String>> getExistedAlarmNames(Map<String, Object> categoryMapCopy);

}