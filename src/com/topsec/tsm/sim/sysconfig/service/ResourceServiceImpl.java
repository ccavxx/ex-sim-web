package com.topsec.tsm.sim.sysconfig.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.type.IpLocation;
import com.topsec.tsm.base.type.IpLocationUtil;
import com.topsec.tsm.base.type.Ipv4Address;
import com.topsec.tsm.sim.common.dao.SimOrder;

public class ResourceServiceImpl extends HibernateDaoSupport implements ResourceService {

	@Override
	public void saveOrUpdateIpLocation(IpLocation ip) {
		super.getSession().saveOrUpdate(ip) ;
	}

	@Override
	public void deleteIpLocation(IpLocation ip) {
		getSession().delete(ip) ;
	}

	@Override
	public void deleteIpLocations(Integer... ids) {
		Session session = getSession() ;
		for(Integer id:ids){
			IpLocation location = (IpLocation) session.get(IpLocation.class, id) ;
			if (location != null) {
				session.delete(location) ;
			}
		}
	}

	@Override
	public PageBean<IpLocation> search(int pageIndex, int pageSize, Map<String, Object> searchCondition, SimOrder... orders) {
		List<Criterion> criterions = new ArrayList<Criterion>(4) ; 
		if(StringUtil.isNotBlank((String)searchCondition.get("name"))){
			criterions.add(Restrictions.like("netSegment", (String)searchCondition.get("name"), MatchMode.ANYWHERE)) ;
		}
		if(StringUtil.isNotBlank((String)searchCondition.get("ip"))){
			long value = Ipv4Address.parseLong((String)searchCondition.get("ip")) ; 
			criterions.add(Restrictions.le("small", value)) ;
			criterions.add(Restrictions.ge("big", value)) ;
		}
		return searchForClass(IpLocation.class, pageIndex, pageSize, criterions.toArray(new Criterion[0]), orders);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private PageBean searchForClass(Class clazz,int pageIndex, int pageSize,Criterion[] criterions, SimOrder... orders) {
		Criteria cri = createCriteriaFor(clazz,criterions) ;
		cri.setProjection(Projections.rowCount()) ;
		Number rowCount = (Number) cri.uniqueResult() ;
		cri.setProjection(null) ;
		cri.setResultTransformer(CriteriaSpecification.ROOT_ENTITY) ;
		cri.setFirstResult((pageIndex-1)*pageSize) ;
		cri.setMaxResults(pageSize) ;
		for(SimOrder od:orders){
			cri.addOrder(od.isAsc() ? Order.asc(od.getProperty()) : Order.desc(od.getProperty())) ;
		}
		PageBean result = new PageBean(pageIndex, pageSize, rowCount.intValue()) ;
		result.setData(cri.list()) ;
		return result ;
	}
	private Criteria createCriteriaFor(Class<?> clazz,Criterion... criterions){
		Criteria cri = getSession().createCriteria(clazz) ;
		for(Criterion condition:criterions){
			cri.add(condition) ;
		}
		return cri ;
	}
}
