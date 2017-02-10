package com.topsec.tsm.sim.event.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.bean.EventCategoryAddModel;
import com.topsec.tsm.sim.event.service.EventCategoryService;
import com.topsec.tsm.sim.event.service.EventQueryService;
import com.topsec.tsm.sim.event.service.EventService;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate.AbstractEndModel;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.sysconfig.service.EventRuleService;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.FastJsonUtil;

@Controller
@RequestMapping("/event/*")
public class EventController {
	@Autowired
	private EventService eventService;

	@Autowired
	private EventCategoryService eventCategoryService;
	
	
	@Autowired
	private EventRuleService eventRuleService;
	
	@RequestMapping("getRealTimeData")
	@ResponseBody
	public Object getRealTimeData(SID sid,@RequestParam(value = "limit", defaultValue = "10") Integer limit) throws Exception {
		return todayEvent(sid, 1, limit) ;
	}
	@RequestMapping("todayEvent")
	@ResponseBody
	public Object todayEvent(final SID sid,@RequestParam(value="page",defaultValue="1")int page,
			@RequestParam(value="rows",defaultValue="10")int rows) throws Exception {
		Date now = new Date();
		Date  dayBegin = ObjectUtils.dayBegin(now) ;
		EventQueryService eqs = (EventQueryService) SpringContextServlet.springCtx.getBean("eventQueryService") ;
		Condition con = new Condition() ;
		con.setColumnsSet("EVENT_ID,PRIORITY,NAME,SRC_ADDRESS,DEST_ADDRESS,DVC_ADDRESS,CAT1_ID,CAT2_ID,CAT3_ID,END_TIME,DESCR,CONFIRM,CONFIRM_PERSON") ;
		con.setStart_time(StringUtil.longDateString(dayBegin)) ;
		con.setEnd_time(StringUtil.longDateString(now)) ;
		con.setPageSize(rows) ;
		con.setSizeStart((page-1)*rows) ;
		con.setConfirm("0");
		List<Map> data = eqs.getEventsForFlex(con) ;
		List totalList = eqs.getEventsTotalForFlex(con, false) ;
		JSONObject result = new JSONObject() ;
		result.put("total", ((Map)totalList.get(0)).get("value")) ;
		//result.put("rows", data) ;
		for (Map map : data) {
			if ("operator".equals(sid.getRole())) {
				map.put("isOperator", true);
			}
		}
		result.put("rows", FastJsonUtil.toJSONArray(data, 
				"EVENT_ID","PRIORITY","NAME","SRC_ADDRESS","DEST_ADDRESS","DVC_ADDRESS","CAT1_ID","CAT2_ID","CAT3_ID","END_TIME",
				"$htmlEscape:DESCR=DESCR","CONFIRM","CONFIRM_PERSON","isOperator"));
		return result;
	}

	/**
	 * 事件级别
	 * @author zhaojun 2014-3-4上午11:07:29
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@RequestMapping("levelStatisticByTime")
	@ResponseBody
	public Object getLevelStatisticByTime(@RequestParam(value="startTime") String startTime,
									   @RequestParam(value="endTime") String endTime){
		List<Map<String,Integer>>	list=eventService.getEventLevelStatisticByTime(startTime,endTime);
		List<Map<String,Object>> levelMapList=new ArrayList<Map<String,Object>>();
		for (Map<String, Integer> node : list) {
			Map<String, Object> levelMap=new HashMap<String, Object>();
			levelMap.put("name", CommonUtils.getLevel(node.get("priority")));
			levelMap.put("value", node.get("opCount"));
			levelMapList.add(levelMap);
		}
		return levelMapList;
	}
	/**
	 * 获取事件河流数据
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@RequestMapping("eventRiverDataByTime")
	@ResponseBody
	public Object getEventRiverDataByTime(@RequestParam(value="startTime") String startTime,
			   @RequestParam(value="endTime") String endTime,  @RequestParam(value="scope") String scope){
		Map<String,List<Map<String,Object>>> resultMap=new HashMap<String,List<Map<String,Object>>>();
		List<Map<String,Object>> list=eventService.getEventRiverDataByTime(startTime, endTime);
		String pattern = "yyyy-MM-dd HH:mm:ss";
		for (Map<String,Object> node : list) {
			String eventName = StringUtil.toString(node.get("name"));
			Map<String, Object> element = new HashMap<String, Object>();
			int priority = Integer.valueOf(node.get("priority").toString());
			Date tempDate = (Date)node.get("end_time");
			element.put("name", eventName);
			element.put("weight", priority);
			element.put("priority", CommonUtils.getLevel(priority));
			element.put("time", tempDate);
			if(!resultMap.containsKey(eventName)){
				resultMap.put(eventName, new ArrayList<Map<String,Object>>());		//LinkedList
				element.put("value", 1);
			}
			resultMap.get(eventName).add(element);	
		}
		int step = 0;
		if("day".equals(scope)){
			step = 30*60*1000;
		} else if("week".equals(scope)){
			step = 3*60*60*1000;
		} else {
			step = 12*60*60*1000;
		}
		for (String key : resultMap.keySet()) {
			List<Map<String,Object>> lists = resultMap.get(key);
			int len = lists.size();
			List<Map<String,Object>> temp = new ArrayList<Map<String,Object>>();
			Date firstDate = null;
			Date lastDate = null;
			int sum = 1;
			for(int index = 0; index < len; index++){
				Map<String,Object> data = lists.get(index);
				if(lastDate == null){
					firstDate = (Date)data.get("time");
					lastDate = (Date)data.get("time");
					data.put("time", StringUtil.dateToString((Date)data.get("time"), pattern));
					temp.add(data);
					continue;
				}
				Date tempTime = (Date)data.get("time");
				if(tempTime.getTime() - lastDate.getTime() > step){
					data.put("value", 1);
					data.put("time", StringUtil.dateToString(tempTime, pattern));
					sum = 1;
					lastDate = tempTime;
					temp.add(data);
				} else {
					if(firstDate != lastDate){
						temp.get(temp.size() -1).put("time", StringUtil.dateToString(tempTime, pattern));
					}
					temp.get(temp.size() -1).put("value", ++sum);
				}
			}
			resultMap.put(key, temp);
		}
		return resultMap;
	}
	
	/**
	 * 事件分类
	 * @author zhaojun 2014-3-4上午11:07:49
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@RequestMapping("categoryStaticsticByTime")
	@ResponseBody
	public Object getCategoryStaticsticByTime(@RequestParam(value="startTime") String startTime,
			   @RequestParam(value="endTime") String endTime){
		
		List<Map<String,Integer>>	list=eventService.getEventCategoryStatisticByTime(startTime,endTime);
		return list;
		
	}
	@RequestMapping("nameStatistic")
	@ResponseBody
	public Object nameStatisticByTime(@RequestParam("startTime")String startTime,@RequestParam("endTime")String endTime) {
		return FastJsonUtil.toJSONArray(eventService.getEventNameStatistic(startTime,endTime), "opCount=value", "name", "priority");
	}
	/**
	 * 事件分类树
	 * 一级二级为事件分类
	 * 三级节点为具体事件
	 * 异步加载
	 * @author zhaojun 2014-3-17下午6:06:56
	 * @return
	 */
	@RequestMapping(value="eventCategory",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getEventCategory(@RequestParam(value="id",required=false)Integer id){
		final CategoryOrganizationTemplate  cgenTemplate=new CategoryOrganizationTemplate(id);
		JSONArray  jsonArray=cgenTemplate.genDynamicCategoryJson(eventCategoryService, new AbstractEndModel() {
			@Override
			public void level3(JSONArray jsonArray, Map<String, Object> categoryMap) {
				if(categoryMap!=null) {
					if(categoryMap.get("cat2id")!=null){
						EventCategory currentCategory = cgenTemplate.getCurrentCategory();
						String categoryName=currentCategory.getCategoryName();
						Map<String,Object> cmap=new HashMap<String, Object>();
						cmap.put("cat2id", categoryName);
						getEventRuleLevel3(jsonArray, cmap);
					}
				}
			}
		});
		
		return jsonArray.toJSONString();
	}

	/**
	 * 分类事件规则
	 * @author zhaojun 2014-3-18下午5:40:29
	 * @param jsonArray
	 * @param categoryMap
	 */
	private void getEventRuleLevel3(JSONArray jsonArray, Map<String, Object> categoryMap) {
		//List<EventRule> eventRules =eventRuleService.getEventRulesByCategory(categoryMap);
		List<EventRuleGroup> eventRuleGroups=eventRuleService.getEventRuleGroupsByCategory(categoryMap);
		if(eventRuleGroups!=null){
			for (EventRuleGroup eventRuleGroup : eventRuleGroups) {
				JSONObject  parentJsonObject=new JSONObject();
				parentJsonObject.put("text", eventRuleGroup.getGroupName());
				JSONObject attributes=new JSONObject();
				attributes.put("id", eventRuleGroup.getGroupId());
				attributes.put("type", "3");//三级事件
				parentJsonObject.put("attributes", attributes);
				parentJsonObject.put("state","open");
				jsonArray.add(parentJsonObject);
			}
		}
	}
	
	@RequestMapping(value="addEventCategory",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object addNewEventCategory(@ModelAttribute EventCategoryAddModel addModel){
		String cat1=addModel.getCat1();
		String cat2=addModel.getCat2();
		
		EventCategory root=eventCategoryService.getRootCategoryByName(cat1);
		Integer id1=null;
		if(root==null){
			  id1=eventCategoryService.addRootCategory(cat1);
		}else{
			id1=root.getId();
		}
		Map<String,Integer> categoryMapResult=new HashMap<String, Integer>();
		categoryMapResult.put("cat1id", id1);
		if(cat2!=null&&id1!=null){
			EventCategory child=eventCategoryService.getChild(cat2,id1);
			if(child==null){
				Integer id2=eventCategoryService.addCategory(cat2,id1);
				categoryMapResult.put("cat2id", id2);
			}
		}
		return categoryMapResult;
	}
}
