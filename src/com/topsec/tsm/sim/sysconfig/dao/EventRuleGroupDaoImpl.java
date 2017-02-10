package com.topsec.tsm.sim.sysconfig.dao;


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;


import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.event.EventRuleGroup;


public class EventRuleGroupDaoImpl extends  HibernateDaoImpl<EventRuleGroup, Integer> implements EventRuleGroupDao {

	@Override
	public List<EventRuleGroup> getAll() {
		return this.getSession().createQuery("from EventRuleGroup").list();
	}

	@Override
	public Serializable saveObject(Object o) {
		return this.getSession().save(o);
	}

	@Override
	public List<EventRuleGroup> findByCondition(Map<String, Object> condition) {
		if(condition!=null){
			Set<Entry<String, Object>> entryset = condition.entrySet();
			Criteria c = this.getSession().createCriteria(EventRuleGroup.class);
			for (Entry<String, Object> entry : entryset) {
				c=c.add(Restrictions.eq(entry.getKey(), entry.getValue()));
			}
			return c.list();
		}
		return Collections.emptyList();
	}

	@Override
	public int updateEventRuleGroupById(Integer groupId, Map<String, Object> condition) {
		EventRuleGroup eventRuleGroup = (EventRuleGroup) this.getSession().get(EventRuleGroup.class, groupId);
		Set<Entry<String, Object>> entryset = condition.entrySet();
		for (Entry<String, Object> entry : entryset) {
			try {
				BeanUtils.setProperty(eventRuleGroup, entry.getKey(), entry.getValue());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	@Override
	public int batchAlterRuleGroupStatus(Integer status, Integer... id) {
		Session session=this.getSession();
		for (int i = 0; i < id.length; i++) {
			EventRuleGroup eventRuleGroup= (EventRuleGroup) session.load(EventRuleGroup.class, id[i]);
			eventRuleGroup.setStatus(status);
			if(i%10==0){
				session.flush();
			}
		}
		return id.length;
	}

	/* (non-Javadoc)
	 * @see com.topsec.tsm.sim.sysconfig.dao.EventRuleGroupDao#countByCondtion(java.util.Map)
	 */
	@Override
	public int countByCondtion(Map<String, Object> condition) {
		Criteria criteria = this.getSession().createCriteria(EventRuleGroup.class);
		if(condition!=null){
	 		Set<Entry<String, Object>> entrys = condition.entrySet();
	 		for (Entry<String, Object> entry : entrys) {
	 			if(entry.getValue()==null){
	 				criteria=criteria.add(Restrictions.isNull(entry.getKey()));
	 			}else{
	 				criteria=criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
	 			}
			}
	 	}
		Number n=(Number) criteria.setProjection(Projections.rowCount()).uniqueResult();
		return n.intValue();
	}

	
	
	@Override
	protected Criterion[] getSearchCriterions(Map<String, Object> searchCondition) {
		List<Criterion> criterion = new ArrayList<Criterion>() ;
		String creater = (String) searchCondition.get("creater") ;
		if(StringUtil.isNotBlank(creater)) {
			criterion.add(Restrictions.eq("creater", creater)) ;
		}
		return criterion.toArray(new Criterion[]{});
	}

	@Override
	public EventRuleGroup getEventRuleByName(String evtName) {
		return findFirstByCriteria(Restrictions.eq("groupName", evtName));
	}
	
}
