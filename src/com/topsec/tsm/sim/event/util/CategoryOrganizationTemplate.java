package com.topsec.tsm.sim.event.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tsm.sim.event.service.EventCategoryService;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;

/**
 * 事件类型动态数据组织类
 * @author zhaojun 2014-4-29上午10:29:48
 */
public class CategoryOrganizationTemplate {
	
	private Integer id;
	private EventCategory currentCategory;
	
	
	public EventCategory getCurrentCategory() {
		return currentCategory;
	}

	public CategoryOrganizationTemplate(Integer id) {
		this.id = id;
	}
	public void extractor(JSONObject jsonObject){	}
	public JSONArray genDynamicCategoryJson(EventCategoryService eventCategoryService,AbstractEndModel endModel){
		
		JSONArray  jsonArray=new JSONArray();
		if(id==null){
			List<EventCategory>  rootCategories=eventCategoryService.getRootCategories();
			for (EventCategory eventCategory : rootCategories) {
				 JSONObject  parentJsonObject=new JSONObject();
				 parentJsonObject.put("id", eventCategory.getId());
				 parentJsonObject.put("text", eventCategory.getCategoryName());//统计此类型事件条数
				 parentJsonObject.put("state","closed");
				 
				 JSONObject attributes=new JSONObject();
				 attributes.put("id", eventCategory.getId());
				 attributes.put("type", "1");//一级分类
				 attributes.put("realName", eventCategory.getCategoryName());//名称
				 parentJsonObject.put("attributes", attributes);
				 jsonArray.add(parentJsonObject);
				 extractor(parentJsonObject);
			}
			//查找没有分类的事件规则
			endModel.level3(jsonArray);
			
		}else{	//查找下级节点
			currentCategory=eventCategoryService.get(id);
			if(currentCategory!=null){
				List<EventCategory>  childCategories=eventCategoryService.getChild(id);
				if(childCategories!=null&&childCategories.size()>0){
					for (EventCategory eventCategory : childCategories) {
						JSONObject  parentJsonObject=new JSONObject();
						parentJsonObject.put("id", eventCategory.getId());
						parentJsonObject.put("text", eventCategory.getCategoryName());
						JSONObject attributes=new JSONObject();
						attributes.put("id", eventCategory.getId());
						attributes.put("type", "2");//二级分类
						attributes.put("realName", eventCategory.getCategoryName());//名称
						parentJsonObject.put("attributes", attributes);
						parentJsonObject.put("state","open");
						jsonArray.add(parentJsonObject);
						extractor(parentJsonObject);
					}
					
					Map<String, Object> categoryMap=new HashMap<String, Object>();
					categoryMap.put("cat1id", id);
					categoryMap.put("cat2id", null);
					
					if(currentCategory.getParentId()==null||currentCategory.getParentId()==0){//当前节点是1级节点
						endModel.level3(jsonArray, categoryMap);
					}
					
				}else{
					if(currentCategory.getParentId()!=null&&currentCategory.getParentId()!=0){
						Map<String, Object> categoryMap=new HashMap<String, Object>();
						categoryMap.put("cat2id", id);
						endModel.level3(jsonArray, categoryMap);
					}
				}
			}
		}
		return jsonArray;
	}
	
	/**
	 * 组织末端数据结构
	 * 事件 告警 知识复用 复写末端组织方式
	 * @author zhaojun 2014-4-29上午10:31:25
	 */
	static public abstract class AbstractEndModel{
		public void level3(JSONArray jsonArray){
			level3(jsonArray, null);
		}
		public abstract void level3(JSONArray jsonArray, Map<String, Object> categoryMap);
	}
	
}
