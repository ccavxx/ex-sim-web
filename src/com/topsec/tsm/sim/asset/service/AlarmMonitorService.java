package com.topsec.tsm.sim.asset.service;

import java.util.List;

import com.topsec.tsm.sim.response.persistence.EventPolicyMonitor;

public interface AlarmMonitorService {

	public EventPolicyMonitor get(String id);

	public void save(EventPolicyMonitor eventPolicyMonitor);

	public EventPolicyMonitor delete(EventPolicyMonitor eventPolicyMonitor);

	public void update(EventPolicyMonitor eventPolicyMonitor);
	/**
	 * 根据监视对象的id返回此监视对象上的告警策略
	 * @param resourceId
	 * @return
	 */
	public List<EventPolicyMonitor> getByMonitorId(long resourceId);
}
