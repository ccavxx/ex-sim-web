package com.topsec.tsm.sim.test.service;

import java.util.List;

import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.test.web.TestBean;

public interface TestService {
	
	public void add(Integer id,String name) ;
	
	public String get(Integer id) ;
	
	public void update(Integer id,String name) ;
	
	public void update1(Integer id,String name) ;
	
	public void addBean(TestBean bean);
	
	public List<EventCategory> getEventCategories() ;
}
