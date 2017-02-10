package com.topsec.tsm.sim.kb.web;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.event.service.EventCategoryService;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate;
import com.topsec.tsm.sim.event.util.CategoryOrganizationTemplate.AbstractEndModel;
import com.topsec.tsm.sim.kb.KBEvent;
import com.topsec.tsm.sim.kb.bean.KnowledgeBean;
import com.topsec.tsm.sim.kb.bean.KnowledgeQueryBean;
import com.topsec.tsm.sim.kb.service.KnowledgeService;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DateUtils;


@Controller
@RequestMapping("knowledge")
public class KnowledgeController {
	protected static Logger log= LoggerFactory.getLogger(KnowledgeController.class);
	
	
	@Autowired
	private KnowledgeService knowledgeService;
	@Autowired
	private EventCategoryService eventCategoryService;
	
	
	@RequestMapping(value="jsondata" ,produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public String getJsonFile(@RequestParam(value="json",required=true)String name){
		String json = null;
		try {
			
		    InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream("resource/ui/knowledge/"+name+".json");
			json = IOUtils.toString(jsonStream,"utf-8");
			IOUtils.closeQuietly(jsonStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return JSONArray.parseArray(json).toJSONString();
	}
	
	@RequestMapping(value="uniqueName", produces="text/javascript; charset=utf-8")
	@ResponseBody
	public Object uniqueName(@RequestParam("name") String name, @RequestParam("id") String id) {

		JSONObject result = new JSONObject();
		if(name == null){
			result.put("ok","");
			return result;
		}
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("name", name);
		List<KBEvent> listKBEvent = knowledgeService.getKnowledgeByCategory(condition);
		
		KBEvent kbevent = null;
		if(!StringUtil.isBlank(id)) {
			kbevent = knowledgeService.getKnowledgeById(Integer.valueOf(id));
		}
		
		if(listKBEvent.size() > 0) {
			if(kbevent == null || (kbevent != null && !name.equals(kbevent.getName()))){
				result.put("error", name + "名称已存在");
				return result;
			}
		}
		result.put("ok","");
		return result;
	}
	/**
	 * 添加事件知识库
	 * @param sid
	 * @param event
	 * @return
	 */
	@RequestMapping("addEventKnowlege")
	@ResponseBody
	public Object addKBEvent(@ModelAttribute KnowledgeBean ackbean,SID sid ) {
		KBEvent kbEvent=new KBEvent();
		try {
			BeanUtils.copyProperties(kbEvent, ackbean);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	 
		
		kbEvent.setCreater(sid.getUserName());//以后改
		kbEvent.setCreateTime(new Date());
		knowledgeService.addKBEvent(kbEvent); 
		return null;
	}
	
	
	/**
	 * 查询知识库
	 * @author zhaojun 2014-4-23下午3:17:36
	 * @param KnowledgeQueryBean
	 * @return
	 */
	@RequestMapping("queryEventKnowlege")
	@ResponseBody
	public Object getKBEvents(@ModelAttribute KnowledgeQueryBean KnowledgeQueryBean){
		Map<String,Object> mapQuery=knowledgeService.getKBEventsByPage(KnowledgeQueryBean);
		List<KBEvent> list=(List<KBEvent>) mapQuery.get("rows");
		Map<String,Object> resultMap=new HashMap<String, Object>(mapQuery);
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>();
		if(list!=null&&list.size()!=0){
			for (KBEvent kbevent : list) {
				Map<String,Object> rowMap=new HashMap<String, Object>();
				rowMap.put("cat1id", kbevent.getCat1id());
				rowMap.put("cat2id", kbevent.getCat2id());
				rowMap.put("creater", kbevent.getCreater());
				rowMap.put("createTime", DateUtils.formatDatetime(kbevent.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
				rowMap.put("description", kbevent.getDescription());
				rowMap.put("id", kbevent.getId());
				rowMap.put("name", kbevent.getName());
				rowMap.put("priority",CommonUtils.getLevel(kbevent.getPriority()));
				rowMap.put("solution", kbevent.getSolution());
				resultList.add(rowMap);
			}
		}
		resultMap.put("rows", resultList);
		return resultMap;
	}
	
	/**
	 * 删除知识库
	 * @author zhaojun 2014-4-24下午2:51:53
	 * @param id
	 * @return
	 */
	@RequestMapping("delEventKnowlegeById")
	@ResponseBody
	public Object deleteKBEventsById(@RequestParam("id[]")Integer... id){
		Map<String,Object> resultMap = new HashMap<String, Object>();
		boolean success = knowledgeService.deleteKBEventById(id);
		resultMap.put("status", success);
		return resultMap;
	}
	
	
	/**
	 * 更新知识
	 * @author zhaojun 2014-5-8下午7:12:29
	 * @param kbevent
	 * @return
	 */
	
	@RequestMapping("updateEventKnowlege")
	@ResponseBody
	public Object updateKBEvent(@ModelAttribute KBEvent kbevent){
		Map<String,String> statusMap=new HashMap<String, String>();
		try {
			kbevent.setCreateTime(new Date());
			knowledgeService.updateKBEvent(kbevent);
		} catch (Exception e) {
			statusMap.put("result", "fault");
			log.error(e.getMessage());
			return statusMap;
		}
		statusMap.put("result", "success");
		return statusMap;
	}

	private List<Map<String, Object>> convetAk2Maps(List<KBEvent> list) {
		List<Map<String,Object>> kbFormatMaps=new ArrayList<Map<String,Object>>();
		if(list!=null){
			for (KBEvent kbevt : list) {
				Map<String,Object>  formartMap=new HashMap<String, Object>();
				/*formartMap.put("name", kbevt.getName());
				formartMap.put("name", kbevt.getName());*/
				formartMap.put("id", kbevt.getId());
				formartMap.put("name", kbevt.getName());
				formartMap.put("priority", CommonUtils.getLevel(kbevt.getPriority()));
				formartMap.put("description", kbevt.getDescription());
				formartMap.put("solution", kbevt.getSolution());
				formartMap.put("creater", kbevt.getCreater());
				formartMap.put("createTime", DateUtils.formatDatetime(kbevt.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
				kbFormatMaps.add(formartMap);
			}
		}
		return kbFormatMaps;
	}

	@RequestMapping(value="getAssociatedKnowledgebyEvtRuleId")
	@ResponseBody
	public Object getAssociatedKnowledgeByEvtRuleId(@RequestParam(value="evtid")Integer id){
		 /*list = knowledgeService.getAssociatedKnowledgeByEvtRuleId(id);*/
		List<KBEvent>list=knowledgeService.getAssociatedKnowledgeByGid(id);
		List<Map<String, Object>> kbFormatMaps = convetAk2Maps(list);
		return kbFormatMaps;
	}
	
	@RequestMapping(value="getAssociatedKnowledgebyEvtId")
	@ResponseBody
	public Object getAssociatedKnowledgeByEvtId(@RequestParam(value="evtid")float id){
		List<KBEvent> list = knowledgeService.getAssociatedKnowledgeByEvtId(id);
		List<Map<String, Object>> kbFormatMaps = convetAk2Maps(list);
		return kbFormatMaps;
	}
	
	@RequestMapping(value="getOneKnowledgebyId")
	@ResponseBody
	public Object getOneKnowledgeById(@RequestParam(value="id")Integer id){
		KBEvent kbevent=knowledgeService.getKnowledgeById(id);
		return kbevent;
	}
	
	
	@RequestMapping(value="knowledgeCategory",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getKnowledgeCategory(@RequestParam(value="id",required=false)Integer id){
		final CategoryOrganizationTemplate  cgenTemplate=new CategoryOrganizationTemplate(id);
		JSONArray  jsonArray=cgenTemplate.genDynamicCategoryJson(eventCategoryService, new AbstractEndModel() {
			@Override
			public void level3(JSONArray jsonArray, Map<String, Object> categoryMap) {
				
				if(categoryMap!=null&&categoryMap.size()!=0){
					
						EventCategory evtcategory= cgenTemplate.getCurrentCategory();
						HashMap<String, Object> copy = new HashMap<String, Object>();
//						Integer id1 = evtcategory.getId();
						Integer pid = evtcategory.getParentId();
//						Integer cat2id = (Integer) categoryMap.get("cat2id");
						
						if(pid==null){
							copy.put("cat1id", evtcategory.getCategoryName());
							copy.put("cat2id", null);
						}else{
							copy.put("cat2id", evtcategory.getCategoryName());
						}
//						List<KBEvent>  kbevents=knowledgeService.getKnowledgeByCategory(copy);
//						if(kbevents!=null){
//							for (KBEvent kbEvent : kbevents) {
//								JSONObject  parentJsonObject=new JSONObject();
//								parentJsonObject.put("text", kbEvent.getName());
////								JSONObject attributes=new JSONObject();
////								attributes.put("id", kbEvent.getId());
////								attributes.put("type", "3");//三级事件
////								parentJsonObject.put("attributes", attributes);
//								parentJsonObject.put("state","open");
//								jsonArray.add(parentJsonObject);
//							}
//						}
				}
			}
		});
		return jsonArray.toJSONString();
	}
	
	@RequestMapping(value="knowledgeCategoryByLevel",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getKnowledgeCategoryByLevel(@RequestParam(value="id",required=false)Integer id){
		final CategoryOrganizationTemplate  cgenTemplate=new CategoryOrganizationTemplate(id);
		JSONArray  jsonArray=cgenTemplate.genDynamicCategoryJson(eventCategoryService, new AbstractEndModel() {
			@Override
			public void level3(JSONArray jsonArray, Map<String, Object> categoryMap) {
			}
		});
		
		return jsonArray.toJSONString();
	}
}
