package com.topsec.tsm.sim.sysconfig.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.event.EventRule;

public class EventRuleDaoImpl extends HibernateDaoImpl<EventRule, Integer> implements EventRuleDao{

	@Override
	protected Criterion[] getSearchCriterions(Map<String, Object> searchCondition) {
		List<Criterion> conditions = new ArrayList<Criterion>() ;
		if(searchCondition.containsKey("name")){
			conditions.add(Restrictions.eq("name", searchCondition.get("name"))) ;
		}
		Criterion[] result = new Criterion[conditions.size()] ; 
		conditions.toArray(result) ;
		return result ;
	}
	
	@Override
	public List<EventRule> findByCondition(Map<String, Object> condition) {
		Criteria criteria=this.getSession().createCriteria(EventRule.class);
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
		return criteria.list();
	}

	public List<EventRule> getAllByDescCreateTime(){
		return getSession().createQuery("from EventRule r Order by r.createTime desc").list();
//		return getSession().createCriteria(EventRule.class).addOrder(Order.desc("createTime")).list();
	}

	@Override
	public List<EventRule> getEventRulesNoCategory() {
		return this.findByCriteria(Restrictions.isNull("cat1id"),Restrictions.isNull("cat2id"));
	}

	@Override
	public List<EventRule> getEnableRule() {
		return findByCriteria(Restrictions.eq("status", 1));
	}

	@Override
	public void batchAlterRuleStatus(Integer status, List<Integer> idlist) {
		Session session = this.getSession();
		int i=0;
		for (Integer id : idlist) {
			 i++;
			 EventRule rule=(EventRule) session.load(EventRule.class, id);
			 rule.setStatus(status);
			 if(i%10==0){
				 session.flush();
			 }
		}
	}

	/**
	 * 
	 */
	@Override
	public Serializable saveObject(Object object) {
		return getSession().save(object);
	}

	@Override
	public List<EventRule> getEventRulesByGroupId(Integer id) {
		return this.getSession().createQuery("select er from EventRule er,EventRuleDispatch ed where er.id=ed.ruleId and ed.groupId=:groupId order by ed.order asc")
			    .setInteger("groupId", id).list();
	}

	@Override
	public List<EventRule> getAllEventRuleByDispatch() {
		return this.getSession().createQuery("select e from EventRule e,EventRuleDispatch d where e.id=d.ruleId").list();
	}

	@Override
	public List<EventRule> getEnableRuleInRuleGroup() {
		return this.getSession().createQuery("select e from EventRule e,EventRuleDispatch d,EventRuleGroup g where e.id=d.ruleId and g.groupId=d.groupId and g.status=1").list();
	}

 
	@Override
	public int countByCondtion(Map<String, Object> condition) {
		Criteria criteria = this.getSession().createCriteria(EventRule.class);
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
	
	
}
