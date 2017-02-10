package com.topsec.tsm.sim.log.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.hibernate.LogQueryCondition;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
public class LogQueryConditionDaoImp extends HibernateDaoImpl<LogQueryCondition, Integer> implements logQueryConditionDao{
	@Override
	public List<LogQueryCondition>  queryConditionList(Map<String, Object> condition) {
		List<Criterion> conditions = new ArrayList<Criterion>() ;
		String creator = (String)condition.get("creator") ;
		if(StringUtil.isNotBlank(creator)){
			conditions.add(Restrictions.eq("creator", creator)) ;
		}
		Criteria cri = createCriteria(conditions.toArray(new Criterion[0])) ;
		cri.addOrder(Order.desc("createtime")) ;
		return cri.list();
	}
}
