package com.topsec.tsm.sim.event.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;

public class EventCategoryDaoImpl implements EventCategoryDao {

	private SessionFactory sessionFactory ;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EventCategory> getRootCategories() {
		Session session = sessionFactory.getCurrentSession() ;
		List<EventCategory> result = (List<EventCategory>) session.createQuery("from EventCategory where parentId is null ").list() ;
		return result;
	}

	@Override
	public List<EventCategory> getChild(Integer id) {
		Session session = sessionFactory.getCurrentSession() ;
		Query query = session.createQuery("from EventCategory where parentId=?") ;
		query.setInteger(0, id) ;
		List<EventCategory> result = query.list() ;
		return result;
	}

	@Override
	public EventCategory get(Integer id) {
		Session session = sessionFactory.getCurrentSession() ;
		return (EventCategory) session.get(EventCategory.class, id) ;
	}

	@Override
	public EventCategory get(String categoryName) {
		Session session = sessionFactory.getCurrentSession() ;
		EventCategory category = (EventCategory) session.createQuery("from EventCategory where categoryName =:categoryName")
										.setString("categoryName", categoryName)
										.uniqueResult() ;
		return category;
	}

	@Override
	public List<EventCategory> getAllCategories() {
		Session session =sessionFactory.getCurrentSession();
		return session.createCriteria(EventCategory.class).list();
	}

	@Override
	public Integer addRootCategory(String cat1) {
		EventCategory ctg=new EventCategory();
		ctg.setCategoryName(cat1);
		Integer id = (Integer) sessionFactory.getCurrentSession().save(ctg);
		return id;
	}

	@Override
	public Integer addCategory(EventCategory category) {
		return (Integer) sessionFactory.getCurrentSession().save(category);
	}

	@Override
	public EventCategory get(String categoryName, Integer parentId) {
		
		Criteria criteria =sessionFactory.getCurrentSession().createCriteria(EventCategory.class)
		 .add(Restrictions.eq("categoryName", categoryName));
		 if(parentId==null){
			 return (EventCategory) criteria.add(Restrictions.isNull("parentId")).uniqueResult();
		 } 
		 return (EventCategory) criteria.add(Restrictions.eq("parentId",parentId)).uniqueResult();
	}

	@Override
	public List<EventCategory> findByCondtion(Map<String, Object> condition) {
		Criteria criteria =sessionFactory.getCurrentSession().createCriteria(EventCategory.class);
		Set<Entry<String, Object>> entryset = condition.entrySet();
		for (Entry<String, Object> entry : entryset) {
			String key = entry.getKey();
			Object val=entry.getValue();
			if(val==null){
				criteria=criteria.add(Restrictions.isNull(key));
			}else{
				criteria=criteria.add(Restrictions.eq(key, val));
			}
		}
		return criteria.list();
	}
	
	public Object getEventAlarmCount(String dvcAddress){
		return null;
	}
}
