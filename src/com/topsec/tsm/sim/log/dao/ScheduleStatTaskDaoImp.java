package com.topsec.tsm.sim.log.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
public class ScheduleStatTaskDaoImp extends HibernateDaoImpl<ScheduleStatTask, Integer> implements ScheduleStatTaskDao{

	
	
	@Override
	protected Criterion[] getSearchCriterions(Map<String, Object> searchCondition) {
		List<Criterion> conditions = new ArrayList<Criterion>() ;
		String creator = (String)searchCondition.get("creator") ;
		String role = (String)searchCondition.get("role");
		if(StringUtil.isNotBlank(creator)){
			conditions.add(Restrictions.eq("creator", creator)) ;
		}
		if(StringUtil.isNotBlank(role)){
			conditions.add(Restrictions.eq("role",role));
		}
		return conditions.toArray(new Criterion[0]);
	}

	@Override
	public ScheduleStatTask getByName(String name) {
		return findUniqueByCriteria(Restrictions.eq("name", name));
	}

	@Override
	public ScheduleStatTask get(Integer id,boolean loadSubjects, boolean loadResult) {
		ScheduleStatTask task = findById(id) ;
		if (task != null && loadSubjects) {
			Hibernate.initialize(task.getSubjects()) ;
		}
		return task ;
	}

	@Override
	public List<ScheduleStatTask> getEnabled() {
		return findByCriteria(Restrictions.eq("enabled",true));
	}

	@Override
	public int getEnabledCount() {
		Query query = getSession().createQuery("select count(*) from ScheduleStatTask where enabled = ?") ;
		query.setBoolean(1, true) ;
		Number count = (Number) query.uniqueResult() ;
		return count.intValue();
	}
}
