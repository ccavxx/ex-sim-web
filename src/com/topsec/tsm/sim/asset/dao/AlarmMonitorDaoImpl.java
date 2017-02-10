package com.topsec.tsm.sim.asset.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.response.persistence.EventPolicyMonitor;

public class AlarmMonitorDaoImpl extends HibernateDaoImpl<EventPolicyMonitor, String> implements AlarmMonitorDao {

	@Override
	public List<EventPolicyMonitor> getByMonitorId(long resourceId) {
		List<EventPolicyMonitor> result = findByCriteria(Restrictions.eq("monitorId", resourceId)) ;
		return result;
	}

}
