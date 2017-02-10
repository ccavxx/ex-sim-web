package com.topsec.tsm.sim.alarm.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.topsec.tal.base.util.ObjectUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.alarm.bean.AlarmQueryCriteria;
import com.topsec.tsm.sim.alarm.persistence.AlarmCategory;
import com.topsec.tsm.sim.alarm.persistence.SimAlarm;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.util.DateUtils;

public class AlarmDaoImpl extends HibernateDaoSupport implements AlarmDao {
	private SqlMapClient sqlMapClient;
	public List getLevelData(String timeUnit) throws Exception {
		String beginTime = DateUtils.currentDatetime();
		String endTime = DateUtils.currentDatetime();
		Date now = new Date() ;
		if (timeUnit.equals(HOUR)) {
			now = ObjectUtils.addHours(now, -1);
		} else if (timeUnit.equals(DAY)) {
			now = ObjectUtils.trunc(now, "d") ;
		} else if (timeUnit.equals(WEEK)) {
			now = ObjectUtils.addDay(ObjectUtils.trunc(now, "w")) ;
		} else if (timeUnit.equals(MONTH)) {
			now = ObjectUtils.trunc(now, "M") ;
		}
		beginTime = DateUtils.formatDatetime(now);

		List result = new ArrayList();
		for (String level : LEVEL) {
			Map item = new HashMap();
			item.put("name", level);
			item.put("y", new Random().nextInt(100));
			result.add(item);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getAlarmLevelStatisticByTime(final Date stime, final Date etime) {
		
		/*return this.getHibernateTemplate().execute(new HibernateCallback<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> doInHibernate(Session session) throws HibernateException, SQLException {
				
				return  session.createQuery("select new map(count(s) as opCount,s.priority as priority) from SimAlarm s where s.endTime Between :stime and :etime group by s.priority")
						   .setParameter("stime", stime)
						   .setParameter("etime", etime)
						   .list();
			}
		});*/
		Map<String,String> param=new HashMap<String, String>();
		param.put("start_time", DateUtils.formatDatetime(stime, "yyyy-MM-dd HH:mm:ss"));
		param.put("end_time", DateUtils.formatDatetime(etime, "yyyy-MM-dd HH:mm:ss"));
		try {
			return sqlMapClient.queryForList("getAlarmLevelStatisticByTime", param);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Map<String, Object>> getDevAlarmStatisticByTime(final Date startTime, final Date endTime) {
		Map<String,String> param=new HashMap<String, String>();
		param.put("start_time", DateUtils.formatDatetime(startTime, "yyyy-MM-dd HH:mm:ss"));
		param.put("end_time", DateUtils.formatDatetime(endTime, "yyyy-MM-dd HH:mm:ss"));
		try {
			return sqlMapClient.queryForList("getDevAlarmStatisticByTime", param);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Map<String, Object>> getDayStatisticByTime(final Date startTime, final Date endTime) {
		
		Map<String,String> param=new HashMap<String, String>();
		param.put("start_time", DateUtils.formatDatetime(startTime, "yyyy-MM-dd HH:mm:ss"));
		param.put("end_time", DateUtils.formatDatetime(endTime, "yyyy-MM-dd HH:mm:ss"));
		
		try {
			return sqlMapClient.queryForList("getDayAlarmStatisticByTime",param);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*@Override
	public List<SimAlarm> getAlarmPage(final AlarmQueryCriteria alermq) {
		
		return	this.getHibernateTemplate().execute(new HibernateCallback<List<SimAlarm>>() {

			@Override
			public List<SimAlarm> doInHibernate(Session session) throws HibernateException, SQLException {
					Criteria criteria = session.createCriteria(SimAlarm.class).add(Restrictions.between("endTime", alermq.getStartTime(), alermq.getEndTime()));
					if(alermq.getAlarmName()!=null){
						criteria=criteria.add(Restrictions.like("name", "%"+alermq.getAlarmName()+"%"));
					}
					int[] piy = alermq.getPriority();
					if(piy!=null&&piy.length>=1){
						List<SimpleExpression> expressionList=new ArrayList<SimpleExpression>();
						for (int i = 0; i < piy.length;i++) {
							 expressionList.add(Restrictions.eq("priority", piy[i]));
						} 
						Criterion r=expressionList.get(0);
						Criterion c=null;
						int j=1;
						while(j<expressionList.size()){//0,1,2,3,4
							if(c==null){
								c=Restrictions.or(r, expressionList.get(j));
							}else{
								c=Restrictions.or(c, expressionList.get(j));
							}
							j++;
						}
						if(c==null){
							c=r;
						}
						criteria=criteria.add(c);
					}
					
					return  criteria.setFirstResult((alermq.getPage()-1)*alermq.getRows()).setMaxResults(alermq.getRows()).list();
				}
				
			}
		);
	 
	}*/

/*	@Override
	public int getAlarmCount(final AlarmQueryCriteria alermq) {
		return this.getHibernateTemplate().execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException, SQLException {
				// TODO Auto-generated method stub
				Criteria criteria = session.createCriteria(SimAlarm.class).add(Restrictions.between("endTime", alermq.getStartTime(), alermq.getEndTime()));
				if(alermq.getAlarmName()!=null){
					criteria=criteria.add(Restrictions.like("name", "%"+alermq.getAlarmName()+"%"));
				}
				int[] piy = alermq.getPriority();
				if(piy!=null&&piy.length>=1){
					List<SimpleExpression> expressionList=new ArrayList<SimpleExpression>();
					for (int i = 0; i < piy.length;i++) {
						 expressionList.add(Restrictions.eq("priority", piy[i]));
					} 
					Criterion r=expressionList.get(0);
					Criterion c=null;
					int j=1;
					while(j<expressionList.size()){//0,1,2,3,4
						if(c==null){
							c=Restrictions.or(r, expressionList.get(j));
						}else{
							c=Restrictions.or(c, expressionList.get(j));
						}
						j++;
					}
					if(c==null){
						c=r;
					}
					criteria=criteria.add(c);
				}
				return  (Integer) criteria.setProjection(Projections.rowCount()).uniqueResult();
			}
		});
	}
*/
	@Override
	public List<AlarmCategory> getAlarmCategories() {
		return this.getHibernateTemplate().execute(new HibernateCallback<List<AlarmCategory> >() {
			@Override
			public List<AlarmCategory> doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createCriteria(AlarmCategory.class).list();
			}
		});
	}
	
	

	@Override
	public List<SimAlarm> getByIp(int pageIndex, int pageSize, String ip) {
		Criteria cri = getSession().createCriteria(SimAlarm.class).add(Restrictions.eq("srcAddr", ip)).add(Restrictions.eq("alarmState", 1)) ;
		cri.setFirstResult((pageIndex-1)*pageSize) ;
		cri.setMaxResults(pageSize) ;
		cri.addOrder(Order.desc("endTime")) ;
		List<SimAlarm> result = cri.list() ;
		return result ;
	}

	@Override
	public PageBean<SimAlarm> getPageByIp(int pageIndex, int pageSize, String ip,Map<String,Object> params) {
		Criterion ipCondition = Restrictions.disjunction()
				.add(Restrictions.eq("srcAddr", ip))
				.add(Restrictions.eq("destAddr", ip))
				.add(Restrictions.eq("devAddr", ip)) ;
		Criteria cri = getSession().createCriteria(SimAlarm.class).add(ipCondition).add(Restrictions.eq("alarmState", 1)) ;
		Date startTime = (Date) params.get("startTime") ;
		if(startTime != null){
			cri.add(Restrictions.ge("endTime", startTime)) ;
		}
		Date endTime = (Date) params.get("endTime") ;
		if(endTime != null){
			cri.add(Restrictions.le("endTime",endTime)) ;
		}
		cri.setProjection(Projections.rowCount()) ;
		Number rowCount = (Number) cri.uniqueResult() ;
		cri.setProjection(null) ;
		cri.setResultTransformer(CriteriaSpecification.ROOT_ENTITY) ;
		cri.setFirstResult((pageIndex-1)*pageSize) ;
		cri.setMaxResults(pageSize) ;
		cri.addOrder(Order.desc("endTime")) ;
		List<SimAlarm> data = cri.list() ;
		PageBean<SimAlarm> page = new PageBean<SimAlarm>(pageIndex, pageSize, rowCount.intValue()) ;
		page.setData(data) ;
		return page;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	@Override
	public List<Map<String, String>> getExistedAlarmNames(Map<String, Object> categoryMap) {
		try {
			return sqlMapClient.queryForList("getExistedEvents", categoryMap);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
