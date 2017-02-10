package com.topsec.tsm.sim.sysconfig.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.ConfigFactory;
import com.topsec.tal.base.util.config.ConfigType;
import com.topsec.tal.base.util.config.Item;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.common.message.CommandHandlerUtil;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.detect.Comparator;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.common.web.NotCheck;
import com.topsec.tsm.sim.common.web.SecurityModelAttribute;
import com.topsec.tsm.sim.event.EventAssocKb;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.EventRuleDispatch;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.event.EventRuleGroupResp;
import com.topsec.tsm.sim.event.service.EventCategoryService;
import com.topsec.tsm.sim.kb.KBEvent;
import com.topsec.tsm.sim.kb.service.KnowledgeService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.XmlStringAnalysis;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.sysconfig.bean.CorrRuleCondition;
import com.topsec.tsm.sim.sysconfig.bean.CorrRuleConfig;
import com.topsec.tsm.sim.sysconfig.bean.CorrelationCmp;
import com.topsec.tsm.sim.sysconfig.bean.FreqState;
import com.topsec.tsm.sim.sysconfig.convert.EventRuleConverter;
import com.topsec.tsm.sim.sysconfig.convert.XMLRuleColumnHandler;
import com.topsec.tsm.sim.sysconfig.service.EventRuleService;
import com.topsec.tsm.sim.sysconfig.service.ExcelExportPOI;
import com.topsec.tsm.sim.sysconfig.service.ExcelImportPOI;
import com.topsec.tsm.sim.sysman.bean.EventRuleConfig;
import com.topsec.tsm.sim.sysman.bean.RespConfig;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DateUtils;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.ResponseSend;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;


/**
 *功能描述：
 * 
 * @author: ZhouZhijie
 *@date： 日期：2014-3-6 时间：下午02:22:00
 *@param args
 */
@Controller
@RequestMapping("sysconfig/event")
public class EventRuleController {
	
	private static final Logger log = LoggerFactory.getLogger(EventRuleController.class) ;
	
	@Autowired
	private EventRuleService eventRuleService;
	
	@Autowired
	private EventCategoryService eventCategoryService;
	
	@Autowired
	private EventResponseService eventResponseService;
	
	
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	
	@Autowired
	private KnowledgeService knowledgeService;
	@Autowired
	private ReportService reportService ;
	
	@RequestMapping(value="jsondata" ,produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public String getJsonFile(@RequestParam(value="json",required=true) String name){
		JSONArray jsonArray = null ;
		try {
			InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream("resource/ui/rule/"+name+".json") ;
			String json = IOUtils.toString(jsonStream,"utf-8") ;
			IOUtils.closeQuietly(jsonStream) ;
			jsonArray = JSONArray.parseArray(json);
			jsonArray = checkSoftwarePlatform(jsonArray);
		} catch (IOException e) {
			e.printStackTrace() ;
		}
		return jsonArray.toJSONString() ;
	}
	
	/**
	 * 根据设备类型显示告警方式列表
	 * @param jsonArray
	 * @return
	 */
	private JSONArray checkSoftwarePlatform(JSONArray jsonArray){
		JSONArray newArray = new JSONArray();
		if(CommonUtils.isHardwarePlatform()){
			for (Object obj :jsonArray) {
				JSONObject jsonobj = (JSONObject)obj;
				if(jsonobj.get("name").equals("声音响应") || jsonobj.get("name").equals("执行本地命令") || jsonobj.get("name").equals("声光响应")){
					newArray.add(jsonobj);
				}
			}
			if(newArray.size() > 0){
				jsonArray.removeAll(newArray);
			}
		}
		return jsonArray;
	}
	
	/**
	 *功能描述：事件规则列表
	 * 
	 * @author: ZhouZhijie
	 *@date： 日期：2014-3-6 时间：下午06:56:07
	 *@param form
	 */
	@RequestMapping(value="eventRuleConflist")
	@ResponseBody
	public Object eventRuleConflist(@RequestParam(value="rows",defaultValue="10")int pageSize,
			                        @RequestParam(value="page",defaultValue="1")int pageNum) {
		PageBean<EventRule> pager = eventRuleService.search(pageNum, pageSize, Collections.<String,Object>emptyMap()) ;
		List<EventRule> eventRuleList = pager.getData() ;
		JSONArray eventRulesJson = new JSONArray();
		for (EventRule su : eventRuleList) {
			JSONObject eventRuleJson =new JSONObject();
			eventRuleJson.put("id",su.getId() );
			eventRuleJson.put("name", su.getName());
			eventRuleJson.put("createTime", StringUtil.longDateString(su.getCreateTime()));
			eventRuleJson.put("status", su.getStatus());
			eventRuleJson.put("osName", null);
			// 关联通知方式
			String ruleTemplate = su.getRuleTemplate();
			Document doc = null;
			try {
				doc = DocumentHelper.parseText(ruleTemplate);
			} catch (DocumentException e) {
				e.printStackTrace();
			}
			Element rootElement = doc.getRootElement();
			// 取得已选通知方式的id数组
			String[] responseIds = StringUtil.split(rootElement.attributeValue("responseIds"));// 已选通知方式的id
			
			if(responseIds!=null){
				String[] responseNames = new String[responseIds.length];
				for (int i = 0; i < responseIds.length; i++) {
					Response res = eventResponseService.getResponse(responseIds[i]);
					if (res != null) {
						responseNames[i] = res.getName();
					}
				}
				eventRuleJson.put("osName",StringUtil.join(responseNames) );
			}
			eventRulesJson.add(eventRuleJson) ;
		}
		
		Map<String,Object> resultMap=new HashMap<String, Object>();
		resultMap.put("rows", eventRulesJson);
		resultMap.put("total", pager.getTotal());
		
 
		return  resultMap;
	}

	/**
	 * 按照规则ID删除规则
	 * @author zhaojun 2014-3-28下午6:49:17
	 * @param idlist
	 * @return
	 */
	@RequestMapping(value="delRuleConf",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object delRuleConfById(SID sid, @RequestParam(value="idlist")List<Integer> idlist){

		JSONObject  result=new JSONObject();
		result.put("result", "success");

		// 获得删除的名单
		StringBuffer eventRuleNames = new StringBuffer();
		for(int i=0, leg=idlist.size() ; i<leg ; i++){
			EventRule eventRuleTemp = eventRuleService.getRuleConfigById(idlist.get(i));
			if(eventRuleTemp!=null){
				eventRuleNames.append(eventRuleTemp.getName());
				eventRuleNames.append((i==leg-1) ? "" : "，");
			}
		}

		// 判断是否删除失败
		if(!eventRuleService.delRuleConfById(idlist)){
			result.put("result", "fault");
			return result;
		}
		
		AuditLogFacade.deleteSuccess("删除事件分析规则配置", sid.getUserName(), "删除事件分析规则配置名称:" + eventRuleNames.toString(), new IpAddress(sid.getLoginIP()));
		return result;
	}
	
	/**
	 * 按照规则ID 获取规则当前配置
	 * @author zhaojun 2014-3-28下午6:48:54
	 * @param id
	 * @return
	 */
	@RequestMapping(value="getOneRuleConf",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object getRuleConfById(@RequestParam(value="id") Integer id){
		EventRule eventRule=eventRuleService.getRuleConfigById(id);
		String ruleTemplate=eventRule.getRuleTemplate();
		JSONObject ruleJson=createRuleJson(ruleTemplate);
		ruleJson.put("status", eventRule.getStatus());
		ruleJson.put("id", id);
		ruleJson.put("createTime", eventRule.getCreateTime().getTime());
		if(log.isDebugEnabled()){
			log.debug(ruleJson.toJSONString());
		}
		return ruleJson;
		
	}
	/**
	 * 规则数据转换
	 * @author zhaojun 2014-3-28下午6:49:41
	 * @param ruleTemplate
	 * @return
	 */
	private JSONObject createRuleJson(String ruleTemplate) {
		EventRuleConverter eventRuleConverter=new EventRuleConverter();
		eventRuleConverter.setColumnHandler(new XMLRuleColumnHandler());
		eventRuleConverter.build(ruleTemplate);
		JSONObject ruleJson=(JSONObject) JSONObject.toJSON(eventRuleConverter);
		return ruleJson;
	}
	
	protected static final String  EVENT_CATEGORY_LEVEL1="ec1";
	protected static final String  EVENT_CATEGORY_LEVEL2="ec2";
	
	/**
	 * 获取事件分类
	 * @author zhaojun 2014-3-21下午4:48:08
	 * @param type
	 * @param id
	 * @return
	 */
	@RequestMapping(value="eventCategory",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object getEventCategory(@RequestParam(value="cattype",required=false,defaultValue=EVENT_CATEGORY_LEVEL1)String type,@RequestParam(value="id",defaultValue="-1",required=false)Integer id){
		if(type.equals(EVENT_CATEGORY_LEVEL1)&&id==-1){
			return eventCategoryService.getRootCategories();
		}
		if(type.equals(EVENT_CATEGORY_LEVEL2)&&id!=-1){
			return eventCategoryService.getChild(id);
		}
		return null;
	}
	
	/**
	 * 添加事件规则
	 * @author zhaojun 2014-3-26下午6:25:35
	 * @param erc
	 * @return
	 */
	@RequestMapping(value="addEventRuleCfg",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object addEventRuleCfg(SID sid, @ModelAttribute EventRuleConfig  erc){
		
		JSONObject  jsonResult=new JSONObject();
		jsonResult.put("result", "success");
		Integer ruleId=-1;
		try {
			if(log.isDebugEnabled()){
				log.debug(JSONObject.toJSONString(erc));
			}
			//规则启用禁用和告警由规则组统一决定  这里新添加的规则全为禁用 不告警
			erc.setIsAlarm(0);
			erc.setStatus(0);
			RuleBuilder ruleBuilder=new RuleBuilder(){
				@Override
				public Response getReponse(String responseId) {
					return eventResponseService.getResponse(responseId);
				}

				@Override
				public EventCategory getEventCategory(Integer catid) {
					return eventCategoryService.get(catid);
				}
				
			};
			EventRule eventRule =/* createEventRule(erc)*/ruleBuilder.buildEventRuleFromConfig(erc);
			ruleId=eventRuleService.saveEventRule(eventRule);
			AuditLogFacade.addSuccess("添加事件分析规则配置", sid.getUserName(), "添加事件分析规则配置名称:" + eventRule.getName(), new IpAddress(sid.getLoginIP()));
		} catch (Exception e) {
			jsonResult.put("result", "fault");
			e.printStackTrace();
		}
		
		Integer groupId = erc.getGroupId();
		if(groupId!=-1&&ruleId!=-1){
			EventRuleDispatch eventRuleDispatch=new EventRuleDispatch(ruleId, groupId, 1);
			eventRuleService.associate2EventRuleGroup(eventRuleDispatch);
		}
		return jsonResult;
	}
	
	/**
	 * 下发事件规则
	 * @author zhaojun 2014-4-3下午4:22:12
	 * @param eventRules
	 * @throws CommunicationException 
	 */
	private void dispatchEventRules(List<EventRule> eventRules) throws CommunicationException {
	    List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, false, false, false);
		for (Node auditerNode : nodes) {
			Node node = nodeMgrFacade.getNodeByNodeId(auditerNode.getNodeId());
			String[] route =(node!=null?NodeUtil.getRoute(node):null);
			if(route!=null){
				NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_RULE_UPDATE_USERDEFINE, (Serializable) eventRules);
			}
		} 
	}
	 

	@RequestMapping(value="allResponse",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object getAllResponse(){
		List<Response> list = eventResponseService.getResponsesbyType(ConfigType.TYPE_RESPONSE, null, -1, -1);
		JSONArray jsons=new JSONArray();
		for (Response resp : list) {
			JSONObject json=new JSONObject();
			json.put("cfgKey", resp.getCfgKey());
			json.put("name", resp.getName());
		//	json.put("cfg", resp.getConfig());
			json.put("id", resp.getId());
			json.put("type", resp.getType());
			json.put("subType", resp.getSubType());
			jsons.add(json);
		}
		return jsons;
	}
	
	/**
	 * 添加响应
	 * @author zhaojun 2014-3-28下午4:30:47
	 * @param respcfg
	 * @param session
	 * @return
	 */
	@RequestMapping(value="addResponse",produces="text/javascript;charset=utf-8")
	@ResponseBody
	@NotCheck(properties={"subType","respCfgType"},allows={"exectimes","execcmd","execinterval"})
	public Object addResponse(@ModelAttribute RespConfig  respcfg,SID sid,HttpServletRequest request){
		if (respcfg == null) {
			return null;
		}
		JSONObject  jsonResult=new JSONObject();
		jsonResult.put("result", "success");
		if(log.isDebugEnabled()){
			log.debug(JSONObject.toJSONString(respcfg));
		}
		String respId=request.getParameter("respId");
		boolean updateResp = StringUtil.isNotBlank(respId);
		Response  resp=null;
		if (updateResp) {
			resp = reportService.showPlanTaskById(respId);
		}else{
			resp=new Response();
			resp.setCreateTime(new Date());
			resp.setCreater(sid.getUserName());
		}
		resp.setName(respcfg.getRespName());
		resp.setDesc(respcfg.getRespDesc());
		resp.setCfgKey(respcfg.getRespCfgType());
		try {
			Config config = null;
			if (!updateResp) {
				config=ConfigFactory.getCfgTemplate(respcfg.getRespCfgType());
			}else{
				config = RespCfgHelper.getConfig(resp);
			}
			config.setKey(respcfg.getRespCfgType());
			config.resetDefaultBlock() ;
			List<Map<String, Object>> mCfgItems = respcfg.getCfgItems();
			handleCfgBlocks(config, mCfgItems);
			//设置config XML格式信息到 Response字段中，包括下发XML格式
			RespCfgHelper.setConfig(resp, config);
			
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_ACTION, false, false, false, false);
			if (ObjectUtils.isEmpty(nodes)) {
				throw new CommonUserException("没有找到Action节点") ;
			}
			Node node =nodeMgrFacade.getNodeByNodeId(request.getParameter("nodeId"));
			resp.setNode(node);
			Response responseForName = eventResponseService.getResponseByName(resp.getName());
			if (!updateResp) {
					if(responseForName != null) {
						jsonResult.put("result", "false");
						jsonResult.put("message", "该名称已经存在");
						return jsonResult;
					}
					eventResponseService.addResponse(resp);
					toLog(sid,AuditCategoryDefinition.SYS_ADD, "告警方式", "添加告警方式: " + resp.getName(), Severity.LOW);
					dispathResponse(resp, config, "save");
			}else {
				if(responseForName != null && !(resp.getId().equals(responseForName.getId()))) {
					jsonResult.put("result", "false");
					jsonResult.put("message", "该名称已经存在");
					return jsonResult;
				}
				String responseName = eventResponseService.getResponse(resp.getId()).getName();
				eventResponseService.updateResponse(resp);
				if(!responseName.equals(resp.getName())){
					responseName = responseName + " 更新为  " + resp.getName();
				}
				toLog(sid,AuditCategoryDefinition.SYS_UPDATE, "告警方式", "更新告警方式: " + responseName, Severity.LOW);
				dispathResponse(resp,config, "modify");
			}
		}catch(CommonUserException e){
			jsonResult.put("result", "fault") ;
			jsonResult.put("message", e.getMessage()) ;
		} catch (I18NException e) {
			jsonResult.put("result", "fault");
		}
		return jsonResult;
	}
	/**
	 * 响应配置下发
	 * @author zhaojun 2014-4-3下午5:35:53
	 * @param resp
	 * @param config
	 * @param type
	 */
	private void dispathResponse(Response resp, Config config, String type) {
		if ("resp_cfg_execcmd".equals(config.getKey())) {
			ResponseSend.getInstance().sendExeccmd(config, nodeMgrFacade, resp, type);
		}
		// 声音响应 下发
		else if ("resp_cfg_wavalert".equals(config.getKey())) {
			ResponseSend.getInstance().sendWavalert(config, nodeMgrFacade, resp, type);
		}
		// Snmp Trap 响应下发
		else if ("resp_cfg_snmptrap".equals(config.getKey())) {
			ResponseSend.getInstance().sendSnmpTrap(config, nodeMgrFacade, resp, type);
		}
		// TopAnalyzer联动响应下发
		else if ("rep_cfg_integer".equals(config.getKey())) {
			ResponseSend.getInstance().sendInteger(config, nodeMgrFacade, resp, type);
		}
		// 邮件响应下发
		else if ("resp_cfg_mail".equals(config.getKey())) {
			ResponseSend.getInstance().sendToMail(config, nodeMgrFacade, resp, type);
		}
		// 短信响应下发
		else if ("resp_cfg_phonemsg".equals(config.getKey())) {
			ResponseSend.getInstance().sendPhonemsg(config, nodeMgrFacade, resp, type);
		}
		// 声光响应 下发
		else if ("resp_cfg_wavashinelert".equals(config.getKey())) {
			ResponseSend.getInstance().sendWavaShinelert(config, nodeMgrFacade, resp, type);
		}
		// 一信通短信响应下发
		else if ("resp_cfg_umsgate".equals(config.getKey())) {
			ResponseSend.getInstance().sendUMSGate(config, nodeMgrFacade, resp, type);
		}
	}
	
	/**
	 * config映射成XML配置信息
	 * @author zhaojun 2014-4-1下午6:10:45
	 * @param config
	 * @param mCfgItems
	 */
	public static void handleCfgBlocks(Config config, List<Map<String, Object>> mCfgItems) {
		List<Block> cfgblocks = config.getCfgBlocks();
		for (Block block : cfgblocks) {
			List<Item> cfgItems = block.getCfgItems();
			for (Item item : cfgItems) {
				String key = item.getKey();
				Object value=null;
				for (Map<String, Object> mItem : mCfgItems) {
					String name=(String) mItem.get("name");
					 if(name.equals(key)){
						 value=mItem.get("value");
						 if(StringUtil.booleanVal(mItem.get("splitToArray"))){
							 item.setValueList(Arrays.asList(StringUtil.split((String)value,";"))) ;
						 }else{
							 item.setValue(value.toString());
						 }
					 }
				}
			}
		}
		if(log.isDebugEnabled()){
			log.debug(config.toXml());
		}
	}
	
	/**
	 * 更新规则
	 * @author zhaojun 2014-4-1下午3:26:05
	 * @param erc
	 * @return
	 */
	
	@RequestMapping(value="editEventRuleCfg",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object editEventRuleCfg(SID sid,@ModelAttribute EventRuleConfig erc){
		JSONObject  jsonResult=new JSONObject();
		jsonResult.put("result", "success");
		EventRule eventRule = null;
		try {
			RuleBuilder ruleBuilder=new RuleBuilder(){
				@Override
				public Response getReponse(String responseId) {
					return eventResponseService.getResponse(responseId);
				}

				@Override
				public EventCategory getEventCategory(Integer catid) {
					return eventCategoryService.get(catid);
				}
				
			};
			eventRule = ruleBuilder.buildEventRuleFromConfig(erc);
			eventRule.setId(erc.getId());
			eventRule.setCreateTime(new Date(erc.getCreateTime()));//修改创建时间为最初的时间
			eventRuleService.updateEventRule(eventRule);

			EventRule eventRuleTemp = eventRuleService.getRuleConfigById(erc.getId());
			String eventRuleNameTemp = eventRuleTemp.getName();
			if(!eventRuleNameTemp.equals(eventRule.getName())){
				eventRuleNameTemp = eventRuleNameTemp + " 更新为 " + eventRule.getName();
			}
			AuditLogFacade.updateSuccess("更新事件规则", sid.getUserName(), "更新事件规则:" + eventRuleNameTemp, new IpAddress(sid.getLoginIP()));

		} catch (Exception e) {
			jsonResult.put("result", "fault");
			log.error(e.getMessage());
			e.printStackTrace();
		}
		
		 
		return jsonResult;
	}
	/**
	 * @deprecated
	 * @param sid
	 * @param status
	 * @param id
	 * @return
	 */
	@RequestMapping(value="alterRuleStatus",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object batchAlterRuleStatus(SID sid, @RequestParam(value="status")Integer status,
									@RequestParam(value="id[]",required=true)Integer[] id){
		JSONObject  jsonResult=new JSONObject();
		jsonResult.put("result", "success");
		try {
			eventRuleService.batchAlterRuleStatus(status,id);
			StringBuffer eventRuleNames = new StringBuffer();
			for(int i=0, leg=id.length ; i<leg ; i++){
				EventRuleGroup eventRuleGroupTemp = eventRuleService.getEventRuleGroupById(id[i]);
				if(eventRuleGroupTemp!=null){
					eventRuleNames.append(eventRuleGroupTemp.getGroupName());
					eventRuleNames.append((i==leg-1) ? "" : "，");
				}
			}
			if( status == 1 ) {
				AuditLogFacade.updateSuccess("启用事件分析规则配置状态", sid.getUserName(), "启用事件分析规则配置状态名称:" + eventRuleNames.toString(), new IpAddress(sid.getLoginIP()));
			} else {
				AuditLogFacade.updateSuccess("禁用事件分析规则配置状态", sid.getUserName(), "禁用事件分析规则配置状态名称:" + eventRuleNames.toString(), new IpAddress(sid.getLoginIP()));
			}
		} catch (Exception e) {
			jsonResult.put("result", "fault");
			e.printStackTrace();
		}
		/**
		 * 重新下发
		 */
		try {
			dispatchEventRules(getEnableEventRule());
			if( status == 1 ) {
				AuditLogFacade.updateSuccess("启用事件分析规则配置状态按组重新下发", sid.getUserName(), "启用事件分析规则配置状态按组重新下发成功", new IpAddress(sid.getLoginIP()));
			} else {
				AuditLogFacade.updateSuccess("禁用事件分析规则配置状态按组重新下发", sid.getUserName(), "禁用事件分析规则配置状态按组重新下发成功", new IpAddress(sid.getLoginIP()));
			}
		} catch (CommunicationException e) {
			jsonResult.put("result", "fault");
			e.printStackTrace();
		}
		return jsonResult;
	}
	
	/**
	 * 事件关联分析配置部分
	 * @author zhaojun 2014-6-23上午10:31:17
	 * @param ruleGroup
	 * @return
	 */
	//--------------------------------------------------------------------
	/**
	 * 修改关联状态
	 * @author zhaojun 2014-7-2下午3:40:29
	 * @param status
	 * @param id
	 * @return
	 */
	@RequestMapping(value="alterRuleGroupStatus",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object batchAlterRuleGroupStatus(SID sid,@RequestParam(value="status")Integer status,
			@RequestParam(value="id[]",required=true)Integer[] id){
		JSONObject  jsonResult=new JSONObject();
		jsonResult.put("result", "success");
		try {
			eventRuleService.batchAlterRuleGroupStatus(status,id);
			
			StringBuffer eventRuleNames = new StringBuffer();
			for(int i=0, leg=id.length ; i<leg ; i++){
				EventRuleGroup eventRuleGroupTemp = eventRuleService.getEventRuleGroupById(id[i]);
				if(eventRuleGroupTemp!=null){
					eventRuleNames.append(eventRuleGroupTemp.getGroupName());
					eventRuleNames.append((i==leg-1) ? "" : "，");
				}
			}
			if( status == 1 ) {
				AuditLogFacade.start("启用事件关联分析配置状态", sid.getUserName(), "启用事件关联分析配置状态名称:" + eventRuleNames.toString(), new IpAddress(sid.getLoginIP()));
			} else {
				AuditLogFacade.stop("禁用事件关联分析配置状态", sid.getUserName(), "禁用事件关联分析配置状态名称:" + eventRuleNames.toString(), new IpAddress(sid.getLoginIP()));
			}
		} catch (Exception e) {
			jsonResult.put("result", "fault");
			e.printStackTrace();
		}
		//按组重新下发
		List<EventRule> eventRules = getEnableEventRule();
		try {
			dispatchEventRules(eventRules);
			if( status == 1 ) {
				AuditLogFacade.start("启用事件关联分析配置状态按组重新下发", sid.getUserName(), "启用事件关联分析配置状态按组重新下发成功", new IpAddress(sid.getLoginIP()));
			} else {
				AuditLogFacade.stop("禁用事件关联分析配置状态按组重新下发", sid.getUserName(), "禁用事件关联分析配置状态按组重新下发成功", new IpAddress(sid.getLoginIP()));
			}
		} catch (CommunicationException e) {
			jsonResult.put("result", "fault");
			e.printStackTrace();
		}  
		return jsonResult;
	}
	
	/**
	 * 添加关联规则配置
	 * @param sid
	 * @param ruleGroup
	 * @return
	 */
	
	@RequestMapping(value="addEventCorrRule", produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object addEventCorrRule(SID sid, @ModelAttribute CorrRuleConfig ruleGroup) {
		JSONObject result = (JSONObject)validateEventRule(ruleGroup.getGroupName(), null);
		Object error = result.get("error");
		if(error != null) {
			return result;
		}
		//关联基本属性
		EventRuleGroup eventRuleGroup=new EventRuleGroup();
		eventRuleGroup.setStatus(ruleGroup.getStatus());//启用禁用
		eventRuleGroup.setGroupName(ruleGroup.getGroupName());//关联名称
		eventRuleGroup.setCreateTime(new Date());//添加日期
		eventRuleGroup.setAlarmState(ruleGroup.getAlarmState());//告警
		eventRuleGroup.setTimeout(ruleGroup.getTimeout());//超时
		eventRuleGroup.setPriority(ruleGroup.getPriority());//优先级
		eventRuleGroup.setIsSystem(0);
		eventRuleGroup.setCreater(sid.getUserName());
		eventRuleGroup.setDesc(ruleGroup.getDesc()) ;
		//
		EventCategory cat1 = eventCategoryService.get(ruleGroup.getCat1id());
		EventCategory cat2 = eventCategoryService.get(ruleGroup.getCat2id());
		eventRuleGroup.setCat1id(cat1!=null?cat1.getCategoryName():null);
		eventRuleGroup.setCat2id(cat2!=null?cat2.getCategoryName():null);
		
		//保存关联规则
		Integer groupId = eventRuleService.addEventRuleGroup(eventRuleGroup);
		
		//创建规则
		EventRuleConfig[] eventRuleCfgs = ruleGroup.getEventRuleConfigs();
		
		List<Integer> ruleIds=new ArrayList<Integer>();
		if(eventRuleCfgs!=null){
			for (EventRuleConfig erc : eventRuleCfgs) {
				erc.setIsAlarm(0);
				erc.setStatus(0);
				erc.setCat1(ruleGroup.getCat1id());
				erc.setCat2(ruleGroup.getCat2id());
				RuleBuilder ruleBuilder=new RuleBuilder(){
					@Override
					public Response getReponse(String responseId) {
						return eventResponseService.getResponse(responseId);
					}
					
					@Override
					public EventCategory getEventCategory(Integer catid) {
						return eventCategoryService.get(catid);
					}
					
				};
				EventRule eventRule =/* createEventRule(erc)*/ruleBuilder.buildEventRuleFromConfig(erc);
				Integer ruleId = eventRuleService.saveEventRule(eventRule);
				ruleIds.add(ruleId);
			}
			
		}
		
		//关联多规则关系
		EventRuleDispatch[] eventRuleDispatch=new EventRuleDispatch[ruleIds.size()];
		for (int i = 0; i < ruleIds.size(); i++) {
			Document doc = DocumentHelper.createDocument();
			Element root =  doc.addElement("comparators");
			List<Map<String,Object>> combinations = eventRuleCfgs[i].getCombinations();
			  
			for (int j = 0;combinations!=null&& j < combinations.size(); j++) {
				Map<String,Object> combination = combinations.get(j);
				String field = (String) combination.get("field");
				String func = (String) combination.get("func");
				Comparator comparator=new Comparator(func, field);
				Element comparatorELem = root.addElement(comparator.elementName());
				comparator.setElementFromObj(comparatorELem);
			}
			eventRuleDispatch[i]=new EventRuleDispatch(ruleIds.get(i), groupId, i+1,doc.getRootElement().asXML(),0);
		}
		eventRuleService.associate2EventRuleGroup(eventRuleDispatch);
		
		//知识库
		List<Integer> knowledgeId = ruleGroup.getKnowledgeId();
		if(ObjectUtils.isNotEmpty(knowledgeId)){
			EventAssocKb[] assokbs=new EventAssocKb[knowledgeId.size()];
			int i=0;
			for (Integer knId : knowledgeId) {
				assokbs[i++]=new EventAssocKb(ruleGroup.getGroupName(), knId);
			}
			knowledgeService.associate2Knowledge(assokbs);
		}
		
		//响应
		List<String>  respIds = ruleGroup.getResponseIds();
		if(respIds!=null){
			EventRuleGroupResp[] resps=new EventRuleGroupResp[respIds.size()];
			for (int i = 0; i < respIds.size(); i++) {
				EventRuleGroupResp resp = new EventRuleGroupResp();
				resp.setGroupId(groupId);
				resp.setResponseId(respIds.get(i));
				resps[i]=resp;
			}
			eventRuleService.addEventRuleGroupResp(resps);
		}
		
		//下发关联配置
		AuditLogFacade.addSuccess("添加事件关联分析配置", sid.getUserName(), "添加事件关联分析配置名称:" + eventRuleGroup.getGroupName(), new IpAddress(sid.getLoginIP()));
		//按组重新下发
		List<EventRule> eventRules = getEnableEventRule();
		try {
			dispatchEventRules(eventRules);
			AuditLogFacade.updateSuccess("添加事件关联分析配置按组重新下发", sid.getUserName(), "添加事件关联分析配置按组重新下发成功", new IpAddress(sid.getLoginIP()));
		} catch (CommunicationException e) {
			log.warn("事件规则下属超时！") ;
		}
		return groupId;
	}
	@RequestMapping(value="editEventCorrRule",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object editEventCorrRule(SID sid,@ModelAttribute CorrRuleConfig ruleGroup){
		
		Integer groupId = ruleGroup.getGroupId();
		
		JSONObject result = (JSONObject)validateEventRule(ruleGroup.getGroupName(), groupId.toString());
		Object error = result.get("error");
		if(error != null) {
			return result;
		}
		//关联基本属性
		EventRuleGroup eventRuleGroup=eventRuleService.getEventRuleGroupById(groupId);
		eventRuleGroup.setGroupName(ruleGroup.getGroupName());
		eventRuleGroup.setStatus(ruleGroup.getStatus());
		eventRuleGroup.setAlarmState(ruleGroup.getAlarmState());
		eventRuleGroup.setGroupId(groupId);
		eventRuleGroup.setTimeout(ruleGroup.getTimeout());
		eventRuleGroup.setPriority(ruleGroup.getPriority());
		eventRuleGroup.setDesc(ruleGroup.getDesc()) ;
		//类型
		EventCategory cat1 = eventCategoryService.get(ruleGroup.getCat1id());
		EventCategory cat2 = eventCategoryService.get(ruleGroup.getCat2id());
		eventRuleGroup.setCat1id(cat1!=null?cat1.getCategoryName():null);
		eventRuleGroup.setCat2id(cat2!=null?cat2.getCategoryName():null);
		
		eventRuleService.updateEventRuleGroup(eventRuleGroup);
		
		//规则关联
		
		//1.删除规则和关联全部重新关联
		if(eventRuleService.deleteEventRuleAndDispByGroupId(groupId)){
			//重新插入规则
			
			//创建规则
			EventRuleConfig[] eventRuleCfgs = ruleGroup.getEventRuleConfigs();
			List<Integer> ruleIds=new ArrayList<Integer>();
			if(eventRuleCfgs!=null){
				for (EventRuleConfig erc : eventRuleCfgs) {
					erc.setIsAlarm(0);
					erc.setStatus(0);
					erc.setCat1(ruleGroup.getCat1id());
					erc.setCat2(ruleGroup.getCat2id());
					RuleBuilder ruleBuilder=new RuleBuilder(){
						@Override
						public Response getReponse(String responseId) {
							return eventResponseService.getResponse(responseId);
						}
						
						@Override
						public EventCategory getEventCategory(Integer catid) {
							return eventCategoryService.get(catid);
						}
						
					};
					EventRule eventRule =/* createEventRule(erc)*/ruleBuilder.buildEventRuleFromConfig(erc);
					Integer ruleId = eventRuleService.saveEventRule(eventRule);
					ruleIds.add(ruleId);
				}
			}
			
			//创建关联
			//关联多规则关系
			EventRuleDispatch[] eventRuleDispatch=new EventRuleDispatch[ruleIds.size()];
			for (int i = 0; i < ruleIds.size(); i++) {
				Document doc = DocumentHelper.createDocument();
				Element root =  doc.addElement("comparators");
				List<Map<String,Object>> combinations = eventRuleCfgs[i].getCombinations();
				  
				for (int j = 0;combinations!=null&& j < combinations.size(); j++) {
					Map<String,Object> combination = combinations.get(j);
					String field = (String) combination.get("field");
					String func = (String) combination.get("func");
					Comparator comparator=new Comparator(func, field);
					Element comparatorELem = root.addElement(comparator.elementName());
					comparator.setElementFromObj(comparatorELem);
				}
				eventRuleDispatch[i]=new EventRuleDispatch(ruleIds.get(i), groupId, i+1,doc.getRootElement().asXML(),0);
			}
			eventRuleService.associate2EventRuleGroup(eventRuleDispatch);
			
		}
		
		
		//更新知识关联
		List<Integer> knowledgeIdList = ruleGroup.getKnowledgeId();
		if(knowledgeIdList!=null&&knowledgeIdList.size()!=0){
			Integer []knids=new Integer[knowledgeIdList.size()];
			for (int i = 0; i < knids.length; i++) {
				knids[i]=knowledgeIdList.get(i);
			}
			knowledgeService.updateKnAssocByEvtName(ruleGroup.getGroupName(),knids);
		}else{
			knowledgeService.deleteKnAssoc(ruleGroup.getGroupName());
		}
		
		//更新响应
		eventRuleService.delAllGroupRespByGid(groupId);
		List<String>  respIds = ruleGroup.getResponseIds();
		if(respIds!=null){
			EventRuleGroupResp[] resps=new EventRuleGroupResp[respIds.size()];
			for (int i = 0; i < respIds.size(); i++) {
				EventRuleGroupResp resp = new EventRuleGroupResp();
				resp.setGroupId(groupId);
				resp.setResponseId(respIds.get(i));
				resps[i]=resp;
			}
			eventRuleService.addEventRuleGroupResp(resps);
		}
		
		AuditLogFacade.addSuccess("编辑事件关联分析配置", sid.getUserName(), "编辑事件关联分析配置名称:" + eventRuleGroup.getGroupName(), new IpAddress(sid.getLoginIP()));
		//按组重新下发
		List<EventRule> eventRules = getEnableEventRule();
		try {
			dispatchEventRules(eventRules);
			AuditLogFacade.updateSuccess("编辑事件关联分析配置按组重新下发", sid.getUserName(), "编辑事件关联分析配置按组重新下发成功", new IpAddress(sid.getLoginIP()));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		return groupId;
	}
	/**
	 * 添加事件关联分析配置
	 *  @param sid
	 *  @param ruleGroup
	 *  @return
	 */
	@RequestMapping(value="addEvtRuleGroup",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object addEventRuleGroup(SID sid, @ModelAttribute CorrRuleCondition ruleGroup){
		
		//关联基本属性
		EventRuleGroup eventRuleGroup=new EventRuleGroup();
		eventRuleGroup.setStatus(ruleGroup.getStatus());//启用禁用
		eventRuleGroup.setGroupName(ruleGroup.getGroupName());//关联名称
		eventRuleGroup.setCreateTime(new Date());//添加日期
		eventRuleGroup.setAlarmState(ruleGroup.getAlarmState());//告警
		eventRuleGroup.setTimeout(ruleGroup.getTimeout());//超时
		eventRuleGroup.setPriority(ruleGroup.getPriority());//优先级
		eventRuleGroup.setCreater(sid.getUserName());
		//分类
		EventCategory cat1 = eventCategoryService.get(ruleGroup.getCat1id());
		EventCategory cat2 = eventCategoryService.get(ruleGroup.getCat2id());
		eventRuleGroup.setCat1id(cat1!=null?cat1.getCategoryName():null);
		eventRuleGroup.setCat2id(cat2!=null?cat2.getCategoryName():null);
		eventRuleGroup.setDesc(ruleGroup.getDesc()) ;
		
		//保存关联规则
		Integer groupId = eventRuleService.addEventRuleGroup(eventRuleGroup);
		
		//知识库
		List<Integer> knowledgeId = ruleGroup.getKnowledgeId();
		if(knowledgeId!=null&&knowledgeId.size()!=0){
			EventAssocKb[] assokbs=new EventAssocKb[knowledgeId.size()];
			int i=0;
			for (Integer knId : knowledgeId) {
				assokbs[i++]=new EventAssocKb(ruleGroup.getGroupName(), knId);
			}
			knowledgeService.associate2Knowledge(assokbs);
		}
		
		//响应
		List<String>  respIds = ruleGroup.getResponseIds();
		if(respIds!=null){
			EventRuleGroupResp[] resps=new EventRuleGroupResp[respIds.size()];
			for (int i = 0; i < respIds.size(); i++) {
				EventRuleGroupResp resp = new EventRuleGroupResp();
				resp.setGroupId(groupId);
				resp.setResponseId(respIds.get(i));
				resps[i]=resp;
			}
			eventRuleService.addEventRuleGroupResp(resps);
		}
		
		//关联多规则关系
		CorrelationCmp[] combinations = ruleGroup.getCombinations();
		FreqState[] freqStates = ruleGroup.getFreqStates();
		if(groupId>0){
			List<Integer> eventIdList = ruleGroup.getEventRuleIdList();
			EventRuleDispatch[] eventRuleDispatch=new EventRuleDispatch[eventIdList.size()];
			int timeout=0;
			for (int i = 0; i < eventIdList.size(); i++) {
				Integer eventId=eventIdList.get(i);
				Document doc = DocumentHelper.createDocument();
				Element root =  doc.addElement("comparators");
				for (int j = 0;combinations!=null&& j < combinations.length; j++) {
					CorrelationCmp combination = combinations[j];
					if(combination.getId().intValue()==eventId.intValue()){
						String field = combination.getField();
						String func = combination.getFunc();
						Comparator comparator=new Comparator(func, field);
						Element comparatorELem = root.addElement(comparator.elementName());
						comparator.setElementFromObj(comparatorELem);
					}
				}
				timeout=0;
				for (int j = 0; freqStates!=null&&j < freqStates.length; j++) {
					FreqState freqState = freqStates[j];
					if (freqState.getId().intValue()==eventId.intValue()) {
						timeout=freqState.getTimeout();
					}
				}
				eventRuleDispatch[i]=new EventRuleDispatch(eventId, groupId, i+1,doc.getRootElement().asXML(),timeout);
			}
			//保存事件关联多规则
			eventRuleService.associate2EventRuleGroup(eventRuleDispatch);
		}
		
		AuditLogFacade.addSuccess("添加事件关联分析配置", sid.getUserName(), "添加事件关联分析配置名称:" + eventRuleGroup.getGroupName(), new IpAddress(sid.getLoginIP()));
		//按组重新下发
		List<EventRule> eventRules = getEnableEventRule();
		try {
			dispatchEventRules(eventRules);
			AuditLogFacade.updateSuccess("添加事件关联分析配置按组重新下发", sid.getUserName(), "添加事件关联分析配置按组重新下发成功", new IpAddress(sid.getLoginIP()));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		return groupId;
	}
	
	@RequestMapping(value="editEvtRuleGroup",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object editEventRuleGroup(SID sid,@ModelAttribute CorrRuleCondition ruleGroup){
		int groupId=ruleGroup.getGroupId();
		
		//关联基本属性
		EventRuleGroup eventRuleGroup=new EventRuleGroup();
		eventRuleGroup.setGroupName(ruleGroup.getGroupName());
		eventRuleGroup.setStatus(ruleGroup.getStatus());
		eventRuleGroup.setAlarmState(ruleGroup.getAlarmState());
		eventRuleGroup.setGroupId(groupId);
		eventRuleGroup.setTimeout(ruleGroup.getTimeout());
		eventRuleGroup.setPriority(ruleGroup.getPriority());
		
		//类型
		EventCategory cat1 = eventCategoryService.get(ruleGroup.getCat1id());
		EventCategory cat2 = eventCategoryService.get(ruleGroup.getCat2id());
		eventRuleGroup.setCat1id(cat1!=null?cat1.getCategoryName():null);
		eventRuleGroup.setCat2id(cat2!=null?cat2.getCategoryName():null);
		
		//获得旧关联规则
		EventRuleGroup eventRuleGroupTemp = eventRuleService.getEventRuleGroupById(groupId);
		String groupNameTemp = eventRuleGroupTemp.getGroupName();
		
		//更新关联规则属性
		eventRuleService.updateEventRuleGroup(eventRuleGroup);
		
		//更新知识关联
		List<Integer> knowledgeIdList = ruleGroup.getKnowledgeId();
		if(knowledgeIdList!=null&&knowledgeIdList.size()!=0){
			Integer []knids=new Integer[knowledgeIdList.size()];
			for (int i = 0; i < knids.length; i++) {
				knids[i]=knowledgeIdList.get(i);
			}
			knowledgeService.updateKnAssocByEvtName(ruleGroup.getGroupName(),knids);
		}else{
			knowledgeService.deleteKnAssoc(ruleGroup.getGroupName());
		}
		
		//更新响应
		eventRuleService.delAllGroupRespByGid(groupId);
		List<String>  respIds = ruleGroup.getResponseIds();
		if(respIds!=null){
			EventRuleGroupResp[] resps=new EventRuleGroupResp[respIds.size()];
			for (int i = 0; i < respIds.size(); i++) {
				EventRuleGroupResp resp = new EventRuleGroupResp();
				resp.setGroupId(groupId);
				resp.setResponseId(respIds.get(i));
				resps[i]=resp;
			}
			eventRuleService.addEventRuleGroupResp(resps);
		}
		
		
		//更新关联多规则
		//取消关联多规则   重新关联
		eventRuleService.disassocite2EventRuleGroupByGid(ruleGroup.getGroupId());
		CorrelationCmp[] combinations = ruleGroup.getCombinations();
		FreqState[] freqStates = ruleGroup.getFreqStates();
		if(groupId>0){
			List<Integer> eventIdList = ruleGroup.getEventRuleIdList();
			EventRuleDispatch[] eventRuleDispatch=new EventRuleDispatch[eventIdList.size()];
			int timeout=0;
			for (int i = 0; i < eventIdList.size(); i++) {
				Integer eventId = eventIdList.get(i);
				Document doc = DocumentHelper.createDocument();
				Element root =  doc.addElement("comparators");
				for (int j = 0;combinations!=null&& j < combinations.length; j++) {
					CorrelationCmp combination = combinations[j];
					if(combination.getId().intValue()==eventId.intValue()){
						String field = combination.getField();
						String func = combination.getFunc();
						Comparator comparator=new Comparator(func, field);
						Element comparatorELem = root.addElement(comparator.elementName());
						comparator.setElementFromObj(comparatorELem);
					}
				}
				 timeout=0;
				for (int j = 0; freqStates!=null&&j < freqStates.length; j++) {
					FreqState freqState = freqStates[j];
					if (freqState.getId().intValue()==eventId.intValue()) {
						timeout=freqState.getTimeout();
					}
				}
				eventRuleDispatch[i]=new EventRuleDispatch(eventIdList.get(i), groupId, i+1,doc.getRootElement().asXML(),timeout);
			}
			eventRuleService.associate2EventRuleGroup(eventRuleDispatch);
		}
		
		
		//关联分析规则下发
		if(!groupNameTemp.equals(eventRuleGroup.getGroupName())){
			groupNameTemp = groupNameTemp + " 更新为 " + eventRuleGroup.getGroupName();
		}
		AuditLogFacade.updateSuccess("更新事件关联分析配置", sid.getUserName(), "更新事件关联分析配置名称:" + groupNameTemp, new IpAddress(sid.getLoginIP()));
		List<EventRule> eventRules = getEnableEventRule();
		try {
			dispatchEventRules(eventRules);
			AuditLogFacade.updateSuccess("更新事件关联分析配置按组重新下发", sid.getUserName(), "更新事件关联分析配置按组重新下发成功", new IpAddress(sid.getLoginIP()));
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		return groupId;
	}
	private List<EventRule> getEnableEventRule() {
		return (List<EventRule>) CommandHandlerUtil.handleGetUserRule(null, eventRuleService,eventResponseService);
	}
	
	
	@RequestMapping(value="getEvtRuleGroups",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public 	Object getAllEventRuleGroups(){
		return eventRuleService.getAllEventRuleGroups();
	}
	@RequestMapping(value="getAllEvtRules",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public 	Object getAllEvtRule(){
		List<EventRule> eventRules= eventRuleService.getEventRules();
		JSONArray  jsonArray=new JSONArray();
		for (EventRule eventRule : eventRules) {
			JSONObject eJson=new JSONObject();
			eJson.put("id", eventRule.getId());
			eJson.put("eventName", eventRule.getName());
			jsonArray.add(eJson);
		}
		return jsonArray;
	}
	
	@RequestMapping(value="getAllEvtDispRelas",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object getEventDispatchRelas(){
	    List<EventRuleDispatch> relasList=eventRuleService.getAllEventDispatch();
	    List<Map<String,Object>>  relaMapList=new ArrayList<Map<String,Object>>();
	    for (EventRuleDispatch eventRuleDispatch : relasList) {
			Map<String,Object> map=new HashMap<String, Object>();
			relaMapList.add(map);
			map.put("id", eventRuleDispatch.getId());
			map.put("groupId", eventRuleDispatch.getGroupId());
			map.put("ruleId", eventRuleDispatch.getRuleId());
			map.put("order", eventRuleDispatch.getOrder());
			List<Comparator> cmps=new ArrayList<Comparator>();
			map.put("cmps", cmps);
			String compTemplate = eventRuleDispatch.getComparatorTemplate();
			try {
				Document doc = DocumentHelper.parseText(compTemplate);
				Element root = doc.getRootElement();
				List<Element> comparatorElems=root.elements();
				if(comparatorElems!=null){
					for (Element cmp : comparatorElems) {
						Comparator comparator=new Comparator();
						comparator.setObjFromElement(cmp);
						cmps.add(comparator);
					}
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		
		}
		return relaMapList;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="evtRuleGroupList",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public 	Object getEvtRuleGroupList(SID sid,
			@RequestParam(value="rows",defaultValue="10")int pageSize,
            @RequestParam(value="page",defaultValue="1")int pageNum
			){
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		if(!sid.isOperator()){
			String userName = sid.getUserName();
			conditionMap.put("creater", userName);
		}
		
	    PageBean<EventRuleGroup> evtRuleGroupsPageBean = eventRuleService.getEventRuleGroupsByPage(pageNum, pageSize,conditionMap,SimOrder.desc("status"),SimOrder.asc("isSystem"),SimOrder.desc("createTime"));
	    int total = evtRuleGroupsPageBean.getTotal();
	    List<EventRuleGroup> dataList = evtRuleGroupsPageBean.getData();

	    List<Map<String,Object>> dataMaps = new ArrayList<Map<String,Object>>();
	    if(dataList != null) {
	    	List<Integer> groupids = new ArrayList<Integer>();
	    	for(EventRuleGroup evtRuleGroup : dataList) {
	    		String createrName = evtRuleGroup.getCreater();

	    		Map<String,Object> map = new HashMap<String, Object>();
	    		map.put("creater", createrName);
	    		map.put("groupName", evtRuleGroup.getGroupName());
	    		map.put("groupNameEscapeHtml", HtmlUtils.htmlEscape(evtRuleGroup.getGroupName()));
	    		map.put("groupId", evtRuleGroup.getGroupId());
	    		map.put("priority", evtRuleGroup.getPriority());
	    		map.put("status", evtRuleGroup.getStatus());
	    		map.put("cat1id", evtRuleGroup.getCat1id());
	    		map.put("cat2id", evtRuleGroup.getCat2id());
	    		map.put("createTime", DateUtils.formatDatetime(evtRuleGroup.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
	    		map.put("isSystem", evtRuleGroup.getIsSystem());
	    		groupids.add(evtRuleGroup.getGroupId());
	    		dataMaps.add(map);
	    	}
	    	Map<Integer, List<Response>> respMap = eventResponseService.getResponsesByGroup(groupids);
	    	for (Map<String,Object> map : dataMaps) {
	    		List<Response> responses=respMap.get(map.get("groupId"));
	    		
	    		if(ObjectUtils.isNotEmpty(responses)){
	    			String responseNames = StringUtil.join((List<String>)CollectionUtils.collect(responses, new BeanToPropertyValueTransformer("name"))) ;
	    			String responseId = StringUtil.join((List<String>)CollectionUtils.collect(responses, new BeanToPropertyValueTransformer("id"))) ;
	    			map.put("resp", "["+responseNames+"]");
	    			map.put("respIds",responseId);
	    			
	    		}
			}
	    }
	    Map<String,Object> evtRuleGroupsMap = new HashMap<String, Object>();
	    evtRuleGroupsMap.put("total", total);
	    evtRuleGroupsMap.put("rows", dataMaps);
		return evtRuleGroupsMap;
	}
	
	@RequestMapping(value="delEvtRuleGroup",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object delEventRuleGroup(SID sid, @RequestParam(value="groupIds")Integer ...id){
		Map<String,String>  resultMap=new HashMap<String, String>();
		try {
			resultMap.put("status", "success");
			StringBuffer eventRuleNames = new StringBuffer();
			for(int i=0, leg=id.length ; i<leg ; i++){
				EventRuleGroup eventRuleGroupTemp = eventRuleService.getEventRuleGroupById(id[i]);
				if(eventRuleGroupTemp!=null){
					eventRuleNames.append(eventRuleGroupTemp.getGroupName());
					eventRuleNames.append((i==leg-1) ? "" : "，");
				}
			}
			eventRuleService.delEventRuleGroupById(id);
			AuditLogFacade.deleteSuccess("删除事件关联分析配置", sid.getUserName(), "删除事件关联分析配置名称:" + eventRuleNames.toString(), new IpAddress(sid.getLoginIP()));
			dispatchEventRules(getEnableEventRule());
			AuditLogFacade.updateSuccess("删除事件关联分析配置按组重新下发", sid.getUserName(), "删除事件关联分析配置按组重新下发成功", new IpAddress(sid.getLoginIP()));
		} catch (Exception e) {
			resultMap.put("status", "fault");
		}
		return resultMap;
	}
	/**
	 * 关联规则信息编辑
	 * @param id
	 * @return
	 */
	@RequestMapping(value="getEventRulesByGroupId", produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object getEventRulesByGroupId(@RequestParam(value="groupId") Integer id) {
		JSONObject jsonObject = new JSONObject();
		// 获得组信息
		EventRuleGroup group = eventRuleService.getEventRuleGroupById(id);
		jsonObject.put("group", group);
		// 按关联事件获取关联知识
		List<KBEvent> knowledgeList = knowledgeService.getAssociatedKnowledgeByEvtName(group.getGroupName());
		List<Map<String,Object>> kbFormatMaps = new ArrayList<Map<String,Object>>();
		if(knowledgeList != null) {
			for (KBEvent kbevt : knowledgeList) {
				Map<String,Object>  formartMap = new HashMap<String, Object>();
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
		jsonObject.put("knowledge", kbFormatMaps);
		// 关联信息
	    List<EventRuleDispatch> relasList = eventRuleService.getEventDispatchByGroupId(id);
	    List<Map<String,Object>>  relaMapList = new ArrayList<Map<String,Object>>();

    	for (EventRuleDispatch eventRuleDispatch : relasList) {
    		Map<String,Object> map=new HashMap<String, Object>();
    		relaMapList.add(map);
    		map.put("id", eventRuleDispatch.getId());
    		map.put("groupId", eventRuleDispatch.getGroupId());
    		map.put("ruleId", eventRuleDispatch.getRuleId());
    		map.put("order", eventRuleDispatch.getOrder());
    		map.put("timeout", eventRuleDispatch.getTimeout());
    		List<Comparator> cmps = new ArrayList<Comparator>();
    		map.put("cmps", cmps);
    		String compTemplate = eventRuleDispatch.getComparatorTemplate();
    		if(compTemplate != null && compTemplate.trim().length() != 0) {
    			try {
    				Document doc = DocumentHelper.parseText(compTemplate);
    				Element root = doc.getRootElement();
    				List<Element> comparatorElems = root.elements();
    				if(comparatorElems != null) {
    					for (Element cmp : comparatorElems) {
    						Comparator comparator = new Comparator();
    						comparator.setObjFromElement(cmp);
    						cmps.add(comparator);
    					}
    				}
    			} catch (DocumentException e) {
    				e.printStackTrace();
    			}
    		}
    	}
	    jsonObject.put("assc", relaMapList);
		// 告警方式
		List<Response> list = eventResponseService.getRespByGroupId(id);
		List<Map<String,String>> resps = new ArrayList<Map<String,String>>();
		if(list != null) {
			for (Response response : list) {
				Map<String,String> map = new HashMap<String, String>();
				String rid = response.getId();
				String name = response.getName();
				map.put("rid", rid);
				map.put("rname", name);
				resps.add(map);
			}
		}
		jsonObject.put("resp", resps);
		// 规则信息
		List<EventRule> eventRules = eventRuleService.getEventRulesByGroupId(id);
		JSONArray array = new JSONArray();
		for (EventRule eventRule : eventRules) {
			String ruleTemplate=eventRule.getRuleTemplate();
			JSONObject ruleJson=createRuleJson(ruleTemplate);
			ruleJson.put("status", eventRule.getStatus());
			ruleJson.put("id", id);
			Integer ruleId = eventRule.getId();
			ruleJson.put("ruleId", ruleId);
			ruleJson.put("createTime", eventRule.getCreateTime().getTime());
			if(log.isDebugEnabled()) {
				log.debug(ruleJson.toJSONString());
			}
			array.add(ruleJson);
		}
		jsonObject.put("rule", array);
		return jsonObject;
	}
	/**
	 * 关联规则 EXCEL 文件导出
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("exportEventRulesExcel")
	public void exportEventRulesExcel(SID sid, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			ExcelExportPOI excelExportPOI = ExcelExportPOI.newInstance();
			
			HSSFWorkbook workbook = excelExportPOI.getWorkbook(eventRuleService, sid);
			response.setContentType("application/octet-stream");
			response.setCharacterEncoding("UTF-8");
			String userAgent = request.getHeader("User-Agent") ;
			if(userAgent.indexOf("Firefox") > 0) {
				response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" + java.net.URLEncoder.encode(excelExportPOI.FILENAME_PRENAME, "UTF-8") + ".xls\"");
			}else{
				response.setHeader("Content-Disposition", "attachment; filename=\"" + java.net.URLEncoder.encode(excelExportPOI.FILENAME_PRENAME, "UTF-8") + ".xls\"");
			}
			ServletOutputStream out = response.getOutputStream();
			workbook.write(out);
		} catch (Exception e) {
			log.warn(e.getMessage());
			throw e;
		}
	}
	/**
	 * 关联规则 EXCEL 文件导入
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("importEventRulesExcel")
	@ResponseBody
	public void importEventRulesExcel(SID sid, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject result = new JSONObject();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		try {
	        // 转型为MultipartHttpRequest : (multipartResolver)
	        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
	        // 获得文件
	        MultipartFile file = multipartRequest.getFile("eventRulesExcel");
	        
	        if (file.getSize() > 200L*1024*1024) {
	        	result.put("uploadInfo", "failed");
				result.put("message", "文件不能大于200M!");
				response.getWriter().print(result);
				return;
			}
	        if (!(file.getOriginalFilename().endsWith(".xls"))) {
	        	result.put("uploadInfo", "failed");
	        	result.put("message", "文件类型错误!");
				response.getWriter().print(result);
				return;
	        }
	        ExcelImportPOI excelImportPOI = new ExcelImportPOI();
	        excelImportPOI.init(file);
	        excelImportPOI.xlsToCorrRuleConfig(eventRuleService, sid);
	        
	        String errorMsg = excelImportPOI.getErrorMsg();
	        result.put("uploadInfo", "success");
	        if(errorMsg.length() > 0) {
	        	result.put("message", errorMsg);
				response.getWriter().print(result);
				return;
	        } else {
	        	result.put("message", "导入完成") ;
	        }
	        
    		//下发关联配置
    		AuditLogFacade.addSuccess("导入事件关联分析配置", sid.getUserName(), "导入事件关联分析配置名称:" + excelImportPOI.getGroupNames(), new IpAddress(sid.getLoginIP()));
    		//按组重新下发
    		List<EventRule> eventRules = getEnableEventRule();
    		try {
    			dispatchEventRules(eventRules);
    			AuditLogFacade.updateSuccess("导入事件关联分析配置按组重新下发", sid.getUserName(), "导入事件关联分析配置按组重新下发成功", new IpAddress(sid.getLoginIP()));
    		} catch (CommunicationException e) {
    			log.warn("事件规则下属超时！") ;
    		}
		} catch (Exception e) {
			log.warn(e.getMessage());
        	result.put("uploadInfo", "failed") ;
			result.put("message", "文件上传错误!") ;
			throw e;
		}
		response.getWriter().print(result);
		return;
	}

	@RequestMapping(value="validateEventRuleGroup", produces="text/javascript; charset=utf-8")
	@ResponseBody
	public Object validateEventRuleGroup(@RequestParam("rule_group") String rule_group, @RequestParam("id") String id) {
		return validateEventRule(rule_group, id);
	}
	
	public Object validateEventRule(String rule_group, String id) {
		
		Map<String,Object> condition = new HashMap<String, Object>();
		condition.put("groupName", rule_group);
		
		int count = eventRuleService.countEventRuleGroupByConditon(condition);
		
		JSONObject result = new JSONObject();
		if(count > 0 && StringUtil.isBlank(id)) {
			result.put("error", "该名称已存在") ;
		} else if(StringUtil.isNotBlank(id)) {
			EventRuleGroup eventRuleGroup = eventRuleService.getEventRuleGroupById(Integer.valueOf(id));
			if(!eventRuleGroup.getGroupName().equals(rule_group) && count > 0) {
				result.put("error", "该名称已存在") ;
			} else {
				result.put("ok", "");
			}
		}  else {
			result.put("ok", "");
		}
		
		return result;
	}
	/*****************事件关联配置部分结束***************/
	@RequestMapping(value="showAllInform")
	@ResponseBody
	public Object showAllInform(final SID sid, @RequestParam(value="page",defaultValue="1")Integer page,
								@RequestParam(value="rows",defaultValue="10")Integer rows,
								HttpServletRequest request){
		List<Response> responses = eventResponseService.getResponsesByType(ConfigType.TYPE_RESPONSE, page, rows) ;//所有响应方式
		JSONObject result = new JSONObject();
		if (ObjectUtils.isEmpty(responses)) {
			result.put("total", 0) ;
			result.put("rows", new JSONArray()) ;
			return result;
		}
		Long total = eventResponseService.getResponseRecordCount(ConfigType.TYPE_RESPONSE);
		JSONArray rowsJSON = new JSONArray();
		//如果账号不是operator,添加权限，使之只能查看自己账号内的告警
		if (!"operator".equals(sid.getUserName())) {
			for (int i = 0; i < responses.size(); i++) {
				if (sid.getUserName().equals(responses.get(i).getCreater())) {
					JSONObject jo = new JSONObject();
					jo.put("name", HtmlUtils.htmlEscape(responses.get(i).getName())) ;
					jo.put("nameEscapeHtml", HtmlUtils.htmlEscape(responses.get(i).getName())) ;
					Node node = responses.get(i).getNode() ;
					jo.put("node", node == null ? "节点已删除" : node.getAlias()) ;
					jo.put("desc", HtmlUtils.htmlEscape(responses.get(i).getDesc())) ;
					jo.put("createTime", StringUtil.dateToString(responses.get(i).getCreateTime())) ;
					jo.put("id", responses.get(i).getId());
					jo.put("creater", responses.get(i).getCreater());
					jo.put("cfgKey", responses.get(i).getCfgKey());
					jo.put("scheduleType", responses.get(i).getScheduleType());
					jo.put("start", responses.get(i).isStart());
					jo.put("nodeId", node.getNodeId());
					rowsJSON.add(jo);
				}
			}
			total = Long.valueOf(rowsJSON.size());	
		} else {
			rowsJSON = FastJsonUtil.toJSONArray(responses,new JSONConverterCallBack<Response>(){
				@Override
				public void call(JSONObject result, Response obj) {
					result.put("name", HtmlUtils.htmlEscape(obj.getName())) ;
					result.put("nameEscapeHtml", HtmlUtils.htmlEscape(obj.getName())) ;
					Node node = obj.getNode() ;
					result.put("node", node == null ? "节点已删除" : node.getAlias()) ;
					result.put("desc", HtmlUtils.htmlEscape(obj.getDesc())) ;
					result.put("createTime", StringUtil.dateToString(obj.getCreateTime())) ;
				}
			}, "id","creater","cfgKey","scheduleType","start","node.nodeId=nodeId") ;
		}
		result.put("total", total) ;
		result.put("rows", rowsJSON) ;
		return result ;
	}
	/*
	 * 
	 * 关联告警
	 * */
	@RequestMapping(value="showAlarmCorrelation")
	@ResponseBody
	public Object showAlarmCorrelation(){
		List<Response> responses = eventResponseService.getResponsesbyType(ConfigType.TYPE_RESPONSE,null, 0, 1) ;//所有响应方式
		JSONObject result = new JSONObject();
		if (ObjectUtils.isEmpty(responses)) {
			result.put("rows", new JSONArray());
			return result;
		}
		JSONArray rowsJSON = FastJsonUtil.toJSONArray(responses,new JSONConverterCallBack<Response>(){
			@Override
			public void call(JSONObject result, Response obj) {
				result.put("createTime", StringUtil.dateToString(obj.getCreateTime())) ;
				JSONArray jsonArray = null ;
					InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream("resource/ui/rule/evt_resp_options.json") ;
					String json = null;
					try {
						json = IOUtils.toString(jsonStream,"utf-8");
					} catch (IOException e) {
						e.printStackTrace();
					}
					IOUtils.closeQuietly(jsonStream) ;
					jsonArray = JSONArray.parseArray(json);
					for(int i=0;i<jsonArray.size();i++){
						JSONObject jsonObject=(JSONObject)jsonArray.get(i);
						if(jsonObject.getString("value").equals(obj.getCfgKey())){
							result.put("cfgKey", jsonObject.getString("name")) ;
							break;
						}
					}
			}
		}, "id","name","creater","scheduleType","start","desc") ;
		return rowsJSON ;
	}
	//关联规则添加告警方式
	@RequestMapping(value="addAlarmCorrelation")
	@ResponseBody
	public Object addAlarmCorrelation(@RequestParam("alarmIds")String alarmIds,
									  @RequestParam("ruleId") Integer ruleId,
									  SID sid){
		Result result = new Result();
		String[] alarmsIdArr = StringUtil.split(alarmIds,";");
		try {
		   	EventRuleGroupResp[] resps=new EventRuleGroupResp[alarmsIdArr.length];
			for (int i = 0; i < alarmsIdArr.length; i++) {
				EventRuleGroupResp eventRuleGroupResp = new EventRuleGroupResp();
	     	    eventRuleGroupResp.setGroupId(ruleId);
	     	    eventRuleGroupResp.setResponseId(alarmsIdArr[i]);
				resps[i]=eventRuleGroupResp;
			}
			eventRuleService.updateEventRuleResponses(ruleId,resps);
			EventRuleGroup eventRuleGroup = eventRuleService.getEventRuleGroupById(ruleId);
			AuditLogFacade.updateSuccess("修改关联规则告警方式", sid.getUserName(), sid.getUserName()+"修改关联规则:"+eventRuleGroup.getGroupName()+"   告警方式", new IpAddress(sid.getLoginIP()));
			List<EventRule> eventRules = getEnableEventRule();
			dispatchEventRules(eventRules);
			result.buildSuccess(null) ;
		}catch(CommunicationException e){
			 result.buildError("关联规则下发失败！") ;
		} catch (Exception e) {
			 e.printStackTrace();
			 result.buildError("关联告警方式出错！");
		}
		return result;
	}
	
	@RequestMapping(value="showOneInform")
	@ResponseBody
	public Object showOneInform(@RequestParam("respId")String respId){
		JSONObject result = new JSONObject();
		Response planTask = reportService.showPlanTaskById(respId);
		if (GlobalUtil.isNullOrEmpty(planTask)) {
			return result;
		}
		String configString=planTask.getConfig();
		Map<String, String>map=XmlStringAnalysis.getMap(XmlStringAnalysis.stringDocument(configString));
		
		JSONObject rowsJSON = FastJsonUtil.toJSON(planTask,new JSONConverterCallBack<Response>(){
			@Override
			public void call(JSONObject result, Response obj) {
				result.put("createTime", StringUtil.dateToString(obj.getCreateTime())) ;
				result.put("name", obj.getName()) ;
				result.put("nameEscapeHtml", HtmlUtils.htmlEscape(obj.getName())) ;
				result.put("desc", obj.getDesc()) ;
				result.put("descEscapeHtml", HtmlUtils.htmlEscape(obj.getDesc())) ;
			}
		}, "id","creater","cfgKey","scheduleType","start") ;
		result.put("plantask", rowsJSON) ;
		result.put("plantaskAttr", map) ;
		return result ;
	}
	
	@RequestMapping("getAvaliableComPort")
	@ResponseBody
	public Object getAvaliableComPort(@RequestParam("nodeId")String nodeId) {
		Result result = new Result() ;
		if(NodeStatusQueueCache.offline(nodeId)){
			return result.buildError("告警节点已掉线！") ;
		}
		Node action = nodeMgrFacade.getNodeByNodeId(nodeId) ;
		Map<String, Object> avaiComPorts = null;
		try {
			avaiComPorts = (Map<String, Object>)NodeUtil.dispatchCommand(NodeUtil.getRoute(action), MessageDefinition.CMD_GET_AVAI_COMPORT, null, 1000*120) ;
			result.buildSuccess(avaiComPorts);
		} catch (Exception e) {
			result.buildError("获取短信端口失败") ;
			log.error("获取短信端口失败", e);
		}
		return result;
	}	
	/**
	 * 产生自审计日志
	 * 
	 * @param action
	 *           操作类型
	 * @param name
	 *           操作对象名称
	 * @param desc
	 *           动作描述信息
	 * @param severity
	 *           安全级别
	 */

	private void toLog(SID sid,String action, String name, String desc, Severity severity) {
		// 以下产生日志信息
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(action);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(sid.getUserName());
		_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(true);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}

}
