package com.topsec.tsm.sim.sysconfig.web;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.common.web.IgnoreSecurityCheck;
import com.topsec.tsm.sim.common.web.NotCheck;
import com.topsec.tsm.sim.common.web.SecurityModelAttribute;
import com.topsec.tsm.sim.common.web.SecurityRequestBody;
import com.topsec.tsm.sim.event.EventFilterRule;
import com.topsec.tsm.sim.sysconfig.service.EventFilterRuleService;
import com.topsec.tsm.sim.util.FastJsonUtil;

@Controller
@RequestMapping("EventFilterRule")
public class EventFilterRuleController {
	
	@Autowired
	private EventFilterRuleService eventFilterRuleService;

	/**
	 * 获取事件过滤规则列表
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("getAll")
	@ResponseBody
	public Object getAll(SID sid, @RequestParam(value = "page", defaultValue = "1") Integer pageNumber, 
			@RequestParam(value = "rows", defaultValue = "10") Integer pageSize,String order, String sort) {
		List<SimOrder> orders = new ArrayList<SimOrder>();
		if (order != null && sort != null) {
			if ("nameEscapeHtml".equals(sort)) {
				sort = "name";
			}
			if ("descr".equals(sort)) {
				sort = "description";
			}
			orders.add(order.equals("asc") ? SimOrder.asc(sort) : SimOrder.desc(sort));
		}
		String username = sid.getUserName();
		Map<String, Object> searchCondition = new HashMap<String, Object>();
		//如果账号不等于operator,则添加账号权限,只能查看自己的数据,否则可以查看全部数据
		if (!"operator".equals(username)) {
			searchCondition.put("creater", username);
		}
		PageBean<EventFilterRule> page = eventFilterRuleService.getList(pageNumber, pageSize, searchCondition, orders.toArray(new SimOrder[0]));
		//List<EventFilterRule> resultList = eventFilterRuleService.getAll();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("total", page.getTotal());
		result.put("rows", FastJsonUtil.toJSONArray(page.getData(), 
				"id","$htmlEscape:name=nameEscapeHtml","name",
				"deviceAddress","srcAddress","destAddress",
				"$htmlEscape:description=descr",
				"description",
				"window","rate","available","creater"));
		return result;
	}
	
	/**
	 * 添加或修改事件过滤规则
	 * @param eventFilterRule
	 * @return
	 */
	@RequestMapping("save")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object save(SID sid, @SecurityModelAttribute(uncheck="description") EventFilterRule eventFilterRule, HttpServletRequest request) {
		Result result = new Result(true, "保存成功！");
		try {
			if(!sid.hasOperatorRole()){
				return result.buildError("无权进行此操作!") ;
			}
			boolean isUpdate = eventFilterRule.getId() != null;
			if (isUpdate) {
				eventFilterRule.setCreater(sid.getUserName());
				eventFilterRuleService.update(eventFilterRule);
				//AuditLogFacade.addSuccess("事件过滤", subject, desc, subjectAddress)
			}else {
				eventFilterRule.setCreater(sid.getUserName());
				eventFilterRuleService.add(eventFilterRule);
			}
		}catch(CommonUserException e){
			result.buildError(e.getMessage()) ;
		} catch (Exception e) {
			result.buildSystemError() ;
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 删除事件过滤规则
	 * @param ids
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public Object del(SID sid,@RequestParam("ids") String ids) {
		Result result = new Result(true, "操作成功！");
		try {
			if(!sid.hasOperatorRole()){
				return result.buildError("无权进行此操作!") ;
			}
			String[] idArray = StringUtil.split(ids);
			for(String id:idArray){
				eventFilterRuleService.delete(StringUtil.toInt(id));
			}
		} catch (Exception e) {
			result.buildSystemError() ;
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 批量启用、禁用
	 * @return
	 */
	@RequestMapping("changeState")
	@ResponseBody
	public Object changeState(SID sid,@RequestParam("ids") String ids, @RequestParam("available") boolean available) {
		Result result = new Result(true, "操作成功！");
		try {
			if(!sid.hasOperatorRole()){
				return result.buildError("无权进行此操作!") ;
			}
			String[] idArray = StringUtil.split(ids);
			for(String id:idArray){
				EventFilterRule rule = eventFilterRuleService.get(StringUtil.toInteger(id)) ;
				if(rule != null){
					rule.setAvailable(available) ;
					eventFilterRuleService.update(rule);
				}
			}
		} catch (Exception e) {
			result.buildSystemError() ;
			e.printStackTrace();
		}
		return result;
	}
}