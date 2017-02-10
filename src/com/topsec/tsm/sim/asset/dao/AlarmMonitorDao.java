package com.topsec.tsm.sim.asset.dao;

import java.util.List;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.response.persistence.EventPolicyMonitor;

public interface AlarmMonitorDao extends BaseDao<EventPolicyMonitor, String>{

	List<EventPolicyMonitor> getByMonitorId(long resourceId);
}
