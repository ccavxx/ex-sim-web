package com.topsec.tsm.sim.log.dao;

import java.util.List;

import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tsm.sim.common.dao.BaseDao;


public interface ScheduleStatTaskDao extends BaseDao<ScheduleStatTask, Integer> {
	
	public ScheduleStatTask getByName(String name) ;

	public List<ScheduleStatTask> getEnabled();

	public int getEnabledCount();
	
	public ScheduleStatTask get(Integer taskId, boolean loadSubjects, boolean loadResult);
}
