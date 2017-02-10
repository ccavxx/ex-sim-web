package com.topsec.tsm.sim.alarm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.alarm.AlarmEventCache;
import com.topsec.tsm.sim.alarm.bean.AlarmModel;
import com.topsec.tsm.sim.alarm.bean.AlarmQueryCriteria;
import com.topsec.tsm.sim.alarm.dao.AlarmDao;
import com.topsec.tsm.sim.alarm.persistence.AlarmCategory;
import com.topsec.tsm.sim.alarm.persistence.SimAlarm;
import com.topsec.tsm.sim.util.CommonUtils;

public class AlarmServiceImpl implements AlarmService {
	private AlarmDao alarmDao;

	public void setAlarmDao(AlarmDao alarmDao) {
		this.alarmDao = alarmDao;
	}

	public AlarmDao getAlarmDao() {
		return alarmDao;
	}

	@Override
	public List getDataFromCache(int limit) throws Exception {
		List<Object> result = new ArrayList<Object>();
		List<Map<String, Object>> caches = AlarmEventCache.getInstence().list();
		if (limit > 0)
			caches = caches.subList(0, limit > caches.size() ? caches.size() : limit);
		for (Map<String, Object> cache : caches) {
			AlarmModel alarm = new AlarmModel();
			if (cache.get("NAME") != null) {
				alarm.setName(cache.get("NAME").toString());
			} else if (cache.get("DVC_NAME") != null) {
				alarm.setName(cache.get("DVC_NAME").toString());
			}
			if (cache.get("SRC_ADDRESS") != null)
				alarm.setSourceAddress(cache.get("SRC_ADDRESS").toString());
			if (cache.get("MESSAGE") != null){
				alarm.setDescription(cache.get("MESSAGE").toString());
			}
			if (cache.get("DVC_ADDRESS") != null)
				alarm.setDeviceIp(cache.get("DVC_ADDRESS").toString());
			if (cache.get("CAT4_ID") != null)
				alarm.setType(cache.get("CAT4_ID").toString());
			if (cache.get("END_TIME") != null) {
				Date evenDate = (Date) cache.get("END_TIME");
				alarm.setCreateTime(evenDate);
			}
			if (cache.get("PRIORITY") != null) {
				int priority = (Integer) cache.get("PRIORITY");
				alarm.setPriority(priority);
				alarm.setLevel(CommonUtils.getLevel(priority));
			}
			result.add(alarm);
		}
		return result;
	}

	@Override
	public List getLevelData(String timeUnit) throws Exception {
		return alarmDao.getLevelData(timeUnit);
	}

	/**
	 * 统计每一种优先级的告警
	 */
	@Override
	public List<Map<String, Object>> getAlarmLevelStatisticByTime(Date startTime, Date endTime) {
		return alarmDao.getAlarmLevelStatisticByTime(startTime,endTime);
	}

	/**
	 * 统计每一设备的告警
	 */
	@Override
	public List<Map<String, Object>> getDevAlarmStatisticByTime(Date startTime, Date endTime) {
		return alarmDao.getDevAlarmStatisticByTime(startTime,endTime);
	}

	@Override
	public List<Map<String, Object>> getDayStatisticByTime(Date startTime, Date endTime) {
		return alarmDao.getDayStatisticByTime(startTime,endTime);
	}

	@Override
	public List<AlarmCategory> getAlarmCategories() {
		
		return alarmDao.getAlarmCategories();
	}

	public List<SimAlarm> getByIp(int pageIndex,int pageSize,String ip){
		return alarmDao.getByIp(pageIndex,pageSize,ip) ;
	}

	@Override
	public PageBean<SimAlarm> getPageByIp(int pageIndex, int pageSize, String ip,Map<String,Object> params) {
		return alarmDao.getPageByIp(pageIndex,pageSize,ip,params);
	}

	@Override
	public List<Map<String, String>> getExistedAlarmNames(Map<String, Object> categoryMap) {
		categoryMap.put("alarmState", 1);
		return alarmDao.getExistedAlarmNames(categoryMap);
	}
	
	
}
