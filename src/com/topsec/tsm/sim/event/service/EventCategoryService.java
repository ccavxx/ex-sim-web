package com.topsec.tsm.sim.event.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;

public interface EventCategoryService {

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
	 * 获得事件分类的子分类
	 * @param category
	 * @return
	 */
	public List<EventCategory> getChild(EventCategory category) ;
	/**
	 * 根据分类名称，获取分类的子分类
	 * @param categoryName
	 * @return
	 */
	public List<EventCategory> getChildByName(String categoryName) ;
	/**
	 * 根据事件分类id，获取事件分类对象
	 * @param id
	 * @return
	 */
	public EventCategory get(Integer id) ;

	public List<EventCategory> getAllEventCategories();
	
	/**
	 * 
	 * @author zhaojun 2014-4-24上午10:39:42
	 * @param cat1
	 * @return
	 */
	public Integer addRootCategory(String cat1);
	
	public Integer addCategory(String cat2,Integer parentId);
	
	
	public EventCategory getRootCategoryByName(String cat1);
	
	public EventCategory getChild(String cat2, Integer id1);
	
	public List<EventCategory>  findCategoryByCondition(Map<String,Object> condition);
}
