package com.topsec.tsm.sim.sysconfig.web;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.index.template.DeviceTypeTemplate;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.index.template.LogFieldPropertyFilter;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.selector.ParseException;
import com.topsec.tsm.framework.selector.Selector;
import com.topsec.tsm.framework.util.Helper;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.FilterField;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.common.model.TreeModel;
import com.topsec.tsm.sim.common.web.SecurityRequestBody;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.rule.SimRule;
import com.topsec.tsm.sim.sysconfig.service.LogFilterRuleService;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.util.UUIDUtils;

@Controller
@RequestMapping("LogFilterRule")
public class LogFilterRuleController {
	@Autowired
	private LogFilterRuleService logFilterRuleService;

	@RequestMapping("saveOrUpdateSimRule")
	@ResponseBody
	public Result saveOrUpdateSimRule(SID sid,@SecurityRequestBody @RequestBody Map<String, Object> rule) {
		Result result = new Result(true, "保存成功！");
		if (!rule.isEmpty()) {
			boolean isUpdate = false;
			SimRule simrule = new SimRule();
			if(rule.get("id") != null && String.valueOf(rule.get("id")).length() > 0){
				simrule.setId(Long.parseLong(String.valueOf(rule.get("id"))));
				isUpdate = true;
			}
			simrule.setName(String.valueOf(rule.get("name")));
			simrule.setDeviceType(String.valueOf(rule.get("securityObjectType")));
			simrule.setAvailable(rule.get("available")==null ? 0 : Integer.parseInt(String.valueOf(rule.get("available"))));
			simrule.setDiscard(Boolean.parseBoolean(String.valueOf(rule.get("discard"))));
			simrule.setCondition(String.valueOf(rule.get("filterSql")));
			List<SimRule> simRuleList = logFilterRuleService.getSimRuleByName(simrule.getName());
			if(isUpdate){
				if(simRuleList !=null && simRuleList.size()>0 && (!simrule.getId().equals(simRuleList.get(0).getId())) ){
					result.setStatus(false);
					result.setMessage("该名称已经存在");
					return result;
				}
				SimRule tempRule = logFilterRuleService.getSimRule(simrule.getId());
				simrule.setCreater(tempRule.getCreater());
				String name = tempRule.getName();
				logFilterRuleService.updateSimRule(simrule);
				if(!name.equals(simrule.getName())){
					name = name + " 更新为  " + simrule.getName();
				}
				AuditLogFacade.updateSuccess("更新日志过滤规则", sid.getUserName(), "更新日志过滤规则名称:" + name, new IpAddress(sid.getLoginIP()));
			} else {
				simrule.setCreater(sid.getUserName());
				if( simRuleList != null && simRuleList.size() > 0 ) {
					result.setStatus(false);
					result.setMessage("该名称已经存在");
					return result;
				}
				logFilterRuleService.saveSimRule(simrule);
				AuditLogFacade.addSuccess("添加日志过滤规则", sid.getUserName(), "添加日志过滤规则名称:" + simrule.getName(), new IpAddress(sid.getLoginIP()));
			}
		}
		return result;
	}
	
	/**
	 * ajax验证过滤器名称是否存在
	 * @param name
	 * @param id
	 * @return
	 */
	@RequestMapping(value="validateLogFilterRule", produces="text/javascript; charset=utf-8")
	@ResponseBody
	public Object validateLogFilterRule(@RequestParam("name") String name, @RequestParam("id") String id) {
		List<SimRule> simRuleList = logFilterRuleService.getSimRuleByName(name);
		boolean isUpdate = false;
		if (null != id && !"".equals(id)) {
			isUpdate = true;
		}
		JSONObject result = new JSONObject();
		if(isUpdate) {
			if (simRuleList != null && simRuleList.size()>0 && !id.equals(String.valueOf(simRuleList.get(0).getId()))) {
				result.put("error", "该名称已存在");
			} 
		} else {
			if (simRuleList != null && simRuleList.size()>0) {
				result.put("error", "该名称已存在");
			}
		}
		
		return result;
	}
	
	/**
	 * 每次删除一个
	 * @param sid
	 * @param id
	 * @param name
	 * @return
	 */
	@RequestMapping("deleteLogFilterRule")
	@ResponseBody
	public Result deleteLogFilterRule(SID sid, @RequestParam("id") Long id){
		Result result = new Result(false, "操作失败！");
		if(id != null){
			SimRule rule = logFilterRuleService.deleteSimRule(id);
			AuditLogFacade.deleteSuccess("删除日志过滤规则", sid.getUserName(), "删除日志过滤规则名称:" + StringUtil.recode(rule.getName()), new IpAddress(sid.getLoginIP()));
			result = new Result(true, "操作成功！");
		}
		return result;
	}
	
	/**
	 * 批量删除
	 * @param sid
	 * @param ids
	 * @return
	 */
	@RequestMapping("deleteBatchLogFilterRule")
	@ResponseBody
	public Result deleteBatchLogFilterRule(SID sid, @RequestParam("ids") String ids){
		Result result = new Result(false, "操作失败！");
		String[] idArray = ids.split(",");
		for(int i=0; i<idArray.length; i++){
			String id = idArray[i];
			if(id != null){
				SimRule rule = logFilterRuleService.deleteSimRule(Long.parseLong(id));
				AuditLogFacade.deleteSuccess("删除日志过滤规则", sid.getUserName(), "删除日志过滤规则名称:" + StringUtil.recode(rule.getName()), new IpAddress(sid.getLoginIP()));
			}
			if(i == (idArray.length - 1)){
				result = new Result(true, "操作成功！");
			}
		}
		return result;
	}
	/**
	 * 每次更新一条信息的状态
	 * @param sid
	 * @param id
	 * @param available
	 * @return
	 */
	@RequestMapping("modifyLogFilterRuleAvailable")
	@ResponseBody
	public Result modifyLogFilterRuleAvailable(SID sid,
			@RequestParam("id") Long id, @RequestParam("available") Integer available){
		Result result = new Result(false, "操作失败！");
		SimRule rule = logFilterRuleService.getSimRule(id);
		if(rule != null && available != null){
			rule.setAvailable(available);
			logFilterRuleService.updateSimRule(rule);
			if(available==1){
				AuditLogFacade.start("启用过滤规则",sid.getUserName(), "启用过滤规则:" + rule.getName(), new IpAddress(sid.getLoginIP())) ;
			}else if(available==0){
				AuditLogFacade.stop("禁用过滤规则",sid.getUserName(), "禁用过滤规则:" + rule.getName(), new IpAddress(sid.getLoginIP())) ;
			}
			result = new Result(true, "操作成功！");
		}
		return result;
	}
	/**
	 * 批量更新状态
	 * @param sid
	 * @param id
	 * @param available
	 * @return
	 */
	@RequestMapping("modifyBatchLogFilterRuleAvailable")
	@ResponseBody
	public Result modifyBatchLogFilterRuleAvailable(SID sid,
			@RequestParam("ids") String ids, @RequestParam("available") Integer available){

		Result result = new Result(false, "操作失败！");
		String[] idArray = ids.split(",");

		for(int i=0; i<idArray.length; i++){
			String id = idArray[i];
			SimRule rule = logFilterRuleService.getSimRule(Long.parseLong(id));
			
			if(rule != null && available != null){
			
				rule.setAvailable(available);
				logFilterRuleService.updateSimRule(rule);
				if(available == 1) {
					AuditLogFacade.start("启用过滤规则",sid.getUserName(), "启用过滤规则:" + rule.getName(), new IpAddress(sid.getLoginIP()));
				} else if(available == 0) {
					AuditLogFacade.stop("禁用过滤规则",sid.getUserName(), "禁用过滤规则:" + rule.getName(), new IpAddress(sid.getLoginIP()));
				}
			}
			if(i == (idArray.length - 1)) {
				result = new Result(true, "操作成功！");
			}
		}
		return result;
	}
	
	@RequestMapping("getLogFilterRuleList")
	@ResponseBody
	public Object getLogFilterRuleList(SID sid, @RequestParam(value = "page", defaultValue = "1") Integer pageNumber, @RequestParam(value = "rows", defaultValue = "10") Integer pageSize,
			String order, String sort) {
		List<SimOrder> orders = new ArrayList<SimOrder>();
		if (order != null && sort != null) {
			if ("deviceTypeName".equals(sort)) {
				sort = "deviceType";
			}
			if ("nameEscapeHtml".equals(sort)) {
				sort = "name";
			}
			if ("conditionEscapeHtml".equals(sort)) {
				sort = "condition";
			}
			orders.add(order.equals("asc") ? SimOrder.asc(sort) : SimOrder.desc(sort));
		}
		Map<String, Object> searchCondition = new HashMap<String, Object>();
		if (!"operator".equals(sid.getUserName())) {
			searchCondition.put("creater", sid.getUserName());
		}
		PageBean<SimRule> page = logFilterRuleService.getList(pageNumber, pageSize, searchCondition, orders.toArray(new SimOrder[0]));
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("total", page.getTotal());
		result.put("rows", FastJsonUtil.toJSONArray(page.getData(),new JSONConverterCallBack<SimRule>(){
			@Override
			public void call(JSONObject result, SimRule obj) {
				result.put("deviceTypeName", DeviceTypeNameUtil.getDeviceTypeName(obj.getDeviceType(),Locale.getDefault())) ;
				result.put("name", obj.getName()) ;
				result.put("nameEscapeHtml", HtmlUtils.htmlEscape(obj.getName())) ;
				result.put("condition", obj.getCondition());
				result.put("conditionEscapeHtml", HtmlUtils.htmlEscape(obj.getCondition()));
			}
		}, "id","available","discard","deviceType","creater"));
		return result;
	}
	
	/**
	 * 根据日志源类型ID 获取过滤器字段
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("getFilterFieldById")
	public String getFilterFieldById(@RequestParam("id")String id,HttpServletRequest request){
		DeviceTypeTemplate template = IndexTemplate.getTemplate(id) ;
		request.setAttribute("filterSql", StringUtil.recode(request.getParameter("filterSql"))) ;
		request.setAttribute("fieldset", template.getFields(new LogFieldPropertyFilter("visiable",true)));
		return "page/sysconfig/filter_editor";
	}
	
	@SuppressWarnings("unchecked")
	private void convertChild(TreeModel parent,Element parentElement,String parentOperator,TypeDef typeDef){
		List<Element> child = parentElement.elements() ; 
		if(child.size() > 0){
			for(Element el:child){
				String tagName = el.getName() ;
				boolean notFirst = ObjectUtils.isNotEmpty(parent.getChildren()) ; 
				if(notFirst && parentOperator != null){
					parent.addChild(new TreeModel(UUIDUtils.compactUUID(),parentOperator)) ;
				}
				if("compare".equalsIgnoreCase(tagName)){
					String loperand = el.attributeValue("loperand") ;
					int filedType = typeDef.getType(loperand) ;
					String operator = el.attributeValue("operator") ;
					String roperand = el.attributeValue("roperand") ;
					boolean isNumber = filedType == TypeDef.KeyWord.INT || filedType == TypeDef.KeyWord.LONG ;
					roperand = isNumber ? roperand : "'"+roperand+"'" ;
					parent.addChild(new TreeModel(UUIDUtils.compactUUID(),loperand + " " + operator + " " +roperand)) ;
				}else if("OR".equalsIgnoreCase(tagName)){
					TreeModel tm = parentOperator != null ? new TreeModel(UUIDUtils.compactUUID(), "()") : parent ;
					if(parentOperator != null){
						parent.addChild(tm) ;
					}
					convertChild(tm, el, "OR",typeDef) ;
				}else if("AND".equalsIgnoreCase(tagName)){
					TreeModel tm = parentOperator != null ? new TreeModel(UUIDUtils.compactUUID(), "()") : parent ;
					if(parentOperator != null){
						parent.addChild(tm) ;
					}
					convertChild(tm, el, "AND",typeDef) ;
				}else if("NOT".equalsIgnoreCase(tagName)){
					TreeModel notModel = new TreeModel(UUIDUtils.compactUUID(), "NOT") ;
					parent.addChild(notModel) ;
					convertChild(notModel, el, null,typeDef) ;
				}
			}
		}
	}
	/**
	 * 将filterSql反向解析
	 * @return
	 */
	@RequestMapping("parseFilterSql")
	@ResponseBody
	public Object parseFilterSql(HttpServletRequest request) {
		TreeModel root = new TreeModel() ;
		String selectorText = request.getParameter("filterSql") ;
		TypeDef typeDef = TypeDefFactory.createInstance("../conf/node/typedef.xml") ;
		Selector selector = new Selector(typeDef, null) ;
		try {
			if(StringUtil.isNotBlank(selectorText)){
				selector.check(selectorText) ;
				String xml = selector.sqlStringToXmlString(selectorText) ;
				SAXReader reader = new SAXReader() ;
				Document doc = reader.read(new StringReader(xml)) ;
				Element rootElement = doc.getRootElement() ;//sql element
				convertChild(root,rootElement,null,typeDef) ;
			}
		} catch (ParseException e) {
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return root ; 
	} 
	
	
	@RequestMapping("checkSelector")
	@ResponseBody
	public Object checkSelector(@RequestParam("selector")String selectorText) {
		Result result = new Result() ;
		try {
			selectorText = StringUtil.decode(selectorText, "utf8") ;
			Selector selector = new Selector(TypeDefFactory.createInstance("../conf/node/typedef.xml"), null) ;
			selector.check(selectorText) ;
		} catch (ParseException e) {
			result.buildError("过滤条件解析失败！") ; 
		}catch (Throwable e) {
			result.buildError("过滤条件解析失败！") ;
		}
		return result ;
	}
	
	/**
	 * 根据日志源类型ID 获取对应的过滤器数据
	 * @param deviceType
	 * @param request
	 * @return
	 */
	@RequestMapping("getByDeviceObjectType")
	@ResponseBody
	public Object getByDeviceObjectType(@RequestParam("deviceType")String deviceType,HttpServletRequest request){
		List<SimRule> simRules = logFilterRuleService.getByDeviceObjectType(deviceType);
		return FastJsonUtil.toJSONArray(simRules, "id=value","name");
	}

}