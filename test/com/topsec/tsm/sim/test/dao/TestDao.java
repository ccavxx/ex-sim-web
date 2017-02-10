package com.topsec.tsm.sim.test.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.test.web.TestBean;

public class TestDao {

	private SessionFactory sessionFactory ;
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void add(TestBean bean){
		sessionFactory.getCurrentSession().save(bean) ;
	}
	
	public List<EventCategory> getEventCategories(){
		return sessionFactory.getCurrentSession().createQuery("from EventCategory").list() ;
	}
}
