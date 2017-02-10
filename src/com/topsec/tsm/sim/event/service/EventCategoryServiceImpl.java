package com.topsec.tsm.sim.event.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.event.dao.EventCategoryDao;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;

public class EventCategoryServiceImpl implements EventCategoryService {

	private EventCategoryDao eventCategoryDao ;

	public void setEventCategoryDao(EventCategoryDao eventCategoryDao) {
		this.eventCategoryDao = eventCategoryDao;
	}

	@Override
	public List<EventCategory> getRootCategories() {
		return eventCategoryDao.getRootCategories();
	}

	@Override
	public List<EventCategory> getChild(Integer id) {
		return eventCategoryDao.getChild(id);
	}

	@Override
	public List<EventCategory> getChild(EventCategory category) {
		return eventCategoryDao.getChild(category.getId());
	}

	@Override
	public EventCategory get(Integer id) {
		return eventCategoryDao.get(id);
	}

	@Override
	public List<EventCategory> getChildByName(String categoryName) {
		EventCategory category = eventCategoryDao.get(categoryName) ;
		List<EventCategory> result  ;
		if (category != null) {
			result = getChild(category) ;
		}else{
			result = Collections.emptyList() ;
		}
		return result;
	}

	@Override
	public List<EventCategory> getAllEventCategories() {
		return eventCategoryDao.getAllCategories();
	}

	@Override
	public Integer addRootCategory(String cat1) {
		return eventCategoryDao.addRootCategory(cat1);
	}

	@Override
	public Integer addCategory(String cat2, Integer parentId) {
		EventCategory parent=eventCategoryDao.get(parentId);
		EventCategory category=new EventCategory();
		category.setCategoryName(cat2);
		category.setDescription(parent.getCategoryName());
		category.setParentId(parentId);
		return eventCategoryDao.addCategory(category);
	}

	@Override
	public EventCategory getRootCategoryByName(String cat1) {
		EventCategory eventCategory=eventCategoryDao.get(cat1,null);
		return eventCategory;
	}

	@Override
	public EventCategory getChild(String cat2, Integer id1) {
		return eventCategoryDao.get(cat2,id1);
	}

	@Override
	public List<EventCategory> findCategoryByCondition(Map<String, Object> condition) {
		return eventCategoryDao.findByCondtion(condition);
	}

	 

}
