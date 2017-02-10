package com.topsec.tsm.sim.asset.service;

import java.util.List;

import org.hibernate.Hibernate;

import com.topsec.tsm.sim.asset.dao.AlarmMonitorDao;
import com.topsec.tsm.sim.response.persistence.EventPolicyMonitor;

public class AlarmMonitorServiceImpl implements AlarmMonitorService {

	private AlarmMonitorDao alarmMonitorDao;

	@Override
	public EventPolicyMonitor delete(EventPolicyMonitor eventPolicyMonitor) {
		EventPolicyMonitor epm = alarmMonitorDao.findById(eventPolicyMonitor.getId()) ;
		alarmMonitorDao.delete(epm) ;
		AlarmPolicyManager apm = new AlarmPolicyManager() ;
		apm.delete(epm) ;
		return epm ;
	}

	@Override
	public EventPolicyMonitor get(String id) {
		EventPolicyMonitor eventPolicyMonitor = alarmMonitorDao.findById(id);
		if (eventPolicyMonitor != null) {
			Hibernate.initialize(eventPolicyMonitor.getResponses());
		}
		return eventPolicyMonitor;
	}

	@Override
	public void save(EventPolicyMonitor eventPolicyMonitor) {
		alarmMonitorDao.save(eventPolicyMonitor);
		AlarmPolicyManager apm = new AlarmPolicyManager() ;
		apm.add(eventPolicyMonitor) ;
	}

	@Override
	public void update(EventPolicyMonitor eventPolicyMonitor) {
		alarmMonitorDao.update(eventPolicyMonitor);
		AlarmPolicyManager apm = new AlarmPolicyManager() ;
		apm.modify(eventPolicyMonitor) ;
	}

	@Override
	public List<EventPolicyMonitor> getByMonitorId(long resourceId) {
		return alarmMonitorDao.getByMonitorId(resourceId);
	}

	public void setAlarmMonitorDao(AlarmMonitorDao alarmMonitorDao) {
		this.alarmMonitorDao = alarmMonitorDao;
	}
	
}
