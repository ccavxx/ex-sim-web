package com.topsec.tsm.sim.alarm.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.alarm.bean.AlarmQueryCriteria;
import com.topsec.tsm.sim.alarm.persistence.AlarmCategory;
import com.topsec.tsm.sim.alarm.persistence.SimAlarm;

public interface AlarmService {


	/**
	 * 从Cache中获取告警数据
	 * 
	 * @param limit
	 *            限制返回记录数,当该值小于0时则返回所有记录.
	 * @return 返回集合
	 */
	public List getDataFromCache(int limit) throws Exception;

	/**
	 * 返回告警级别统计数据
	 * 
	 * @param timeUnit
	 *            时间单位
	 * @return
	 * @throws Exception
	 */
	public List getLevelData(String timeUnit) throws Exception;

	
	/**
	 * 按时间统计告警级别
	 * @author zhaojun 2014-3-10下午2:22:55
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<Map<String,Object>> getAlarmLevelStatisticByTime(Date startTime, Date endTime);

	public List<Map<String, Object>> getDevAlarmStatisticByTime(Date startTime, Date endTime);

	public List<Map<String, Object>> getDayStatisticByTime(Date startTime, Date endTime);
 
	/**
	 * 根据ip地址查询相关告警信息
	 * @param pageIndex
	 * @param pageSize
	 * @param ip
	 * @return
	 */
	public List<SimAlarm> getByIp(int pageIndex,int pageSize,String ip) ;
	/**
	 * 根据ip地址查询相关告警信息
	 * @param pageIndex
	 * @param pageSize
	 * @param ip
	 * @param params 
	 * @return
	 */
	public PageBean<SimAlarm> getPageByIp(int pageIndex,int pageSize,String ip, Map<String, Object> params) ;
	
	public List<AlarmCategory> getAlarmCategories();

	public List<Map<String, String>> getExistedAlarmNames(Map<String, Object> categoryMap);
	
	
	

}