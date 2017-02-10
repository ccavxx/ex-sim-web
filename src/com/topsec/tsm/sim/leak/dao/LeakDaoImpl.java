package com.topsec.tsm.sim.leak.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.kb.Leak;

public class LeakDaoImpl extends HibernateDaoImpl<Leak, Integer> implements LeakDao{
	public Criterion[] getSearchCriterions(Map<String,Object> searchCondition){
		List<Criterion> criterions = new ArrayList<Criterion>() ;
		if(searchCondition.get("name")!=null){
			criterions.add(Restrictions.like("name", "%"+searchCondition.get("name")+"%"));
		}
		if(searchCondition.get("score")!=null && !"".equals(searchCondition.get("score"))){
			if("低".equals(searchCondition.get("score").toString())){
				criterions.add(Restrictions.between("score", 0.0f, 3.91f));
			}else if("中".equals(searchCondition.get("score").toString())){
				criterions.add(Restrictions.between("score", 4.0f, 6.91f));
			}else{
				criterions.add(Restrictions.between("score", 7.0f, 10.01f));
			}
		}
		if(searchCondition.get("publishedTime")!=null && !"".equals(searchCondition.get("publishedTime"))){
			criterions.add(Restrictions.like("name", "CVE-"+searchCondition.get("publishedTime")+"%"));
		}
		if(searchCondition.get("published_begin_time")!=null && !"".equals(searchCondition.get("published_begin_time"))){
			criterions.add(Restrictions.between("publishedTime", StringUtil.toDate(searchCondition.get("published_begin_time").toString()+" 00:00:00.000","yyyy-MM-dd HH:mm:ss.SSS").getTime(),StringUtil.toDate(searchCondition.get("published_end_time").toString()+" 23:59:59.999","yyyy-MM-dd HH:mm:ss.SSS").getTime()));
		}
		if(searchCondition.get("mdf_begin_time")!=null && !"".equals(searchCondition.get("mdf_begin_time"))){
			criterions.add(Restrictions.between("mdfTime", StringUtil.toDate(searchCondition.get("mdf_begin_time").toString()+" 00:00:00.000","yyyy-MM-dd HH:mm:ss.SSS").getTime(),StringUtil.toDate(searchCondition.get("mdf_end_time").toString()+" 23:59:59.999","yyyy-MM-dd HH:mm:ss.SSS").getTime()));
		}
		return criterions.toArray(new Criterion[0]) ;
	}

	@Override
	public Leak getLeakByName(String name) {
		return findUniqueByCriteria(Restrictions.eq("name", name)) ;
	}

	@Override
	public List<String> getAllYears() {
		String hql = "SELECT DISTINCT year FROM Leak";
		Session session = getSession();
		Query query = session.createQuery(hql);
		List<String> list = query.list();
		return list;
	}

	@Override
	public Leak getLeakById(Integer id) {
		return findById(id) ;
	}
	
}
