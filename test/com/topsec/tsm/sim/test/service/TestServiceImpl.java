package com.topsec.tsm.sim.test.service;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.test.dao.TestDao;
import com.topsec.tsm.sim.test.web.TestBean;

public class TestServiceImpl implements TestService{

	private JdbcTemplate template ;
	private TestDao dao ;
	public void setDao(TestDao dao) {
		this.dao = dao;
	}

	public JdbcTemplate getTemplate() {
		return template;
	}

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}
	
	public void add(Integer id,String name){
		String sql = "insert into test(id,name) values("+id+",'"+name+"')" ;
		template.execute(sql) ;
	}
	
	public String get(Integer id){
		String sql = "select name from test where id = "+id ;
		Map<String,Object> result = template.queryForMap(sql) ;
		String name = (String) (result == null ? "no record!" : result.get("name")) ;
		return name ;
	}
	
	public void update(Integer id,String name){
		template.execute("update test set name='"+name+"' where id="+id) ;
		throw new RuntimeException("update failed !") ;
	}
	
	public void update1(Integer id,String name){
		template.execute("delete from test where id="+id) ;
		add(id,name) ;
		throw new RuntimeException("update failed!") ;
	}
	
	public void addBean(TestBean bean){
		dao.add(bean) ;
	}
	
	public List<EventCategory> getEventCategories(){
		return dao.getEventCategories() ;
	}
}
