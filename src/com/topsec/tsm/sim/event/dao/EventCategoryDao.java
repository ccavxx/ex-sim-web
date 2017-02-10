package com.topsec.tsm.sim.event.dao;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;

public interface EventCategoryDao {

	/**
	 * 获得事件所有根分类
	 * @return
	 */
	public List<EventCategory> getRootCategories() ;
	/**
	 * 获得事件分类的子分类
	 * @param id
	 * @return
	 */
	public List<EventCategory> getChild(Integer id) ;
	/**
	 * 根据id返回事件分类
	 * @param id
	 * @return
	 */
	public EventCategory get(Integer id);
	/**
	 * 根据名称获取事件分类信息
	 * @param categoryName
	 * @return
	 */
	public EventCategory get(String categoryName) ;
	
	
	public List<EventCategory> getAllCategories();
	
	/**
	 * 
	 * @author zhaojun 2014-4-24上午10:41:51
	 * @param cat1
	 * @return
	 */
	public Integer addRootCategory(String cat1);
	
	/**
	 * 
	 * @author zhaojun 2014-4-24上午10:57:20
	 * @param category
	 * @return
	 */
	public Integer addCategory(EventCategory category);
	
	
	public EventCategory get(String categoryName, Integer parentId);
	
	
	public List<EventCategory>  findByCondtion(Map<String,Object> condition);
	
	public Object getEventAlarmCount(String dvcAddress);
}
