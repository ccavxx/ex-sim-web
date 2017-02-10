package com.topsec.tsm.sim.asset.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.ConfigType;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Ipv4Address;
import com.topsec.tsm.base.xml.XmlAccessException;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.MonitorParamsUtil;
import com.topsec.tsm.sim.asset.PropertyEntryUtil;
import com.topsec.tsm.sim.asset.exception.AssetException;
import com.topsec.tsm.sim.asset.exception.InvalidLicenseException;
import com.topsec.tsm.sim.asset.exception.LimitedNumException;
import com.topsec.tsm.sim.asset.service.AlarmMonitorService;
import com.topsec.tsm.sim.asset.service.AssetService;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.FilterField;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.common.exception.TimeExpressionException;
import com.topsec.tsm.sim.common.web.IgnoreSecurityCheck;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.ComponentNotFoundException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.resource.object.ResourceType;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.EventPolicyMonitor;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.sim.rule.SimRule;
import com.topsec.tsm.sim.sysconfig.service.AggregatorRuleService;
import com.topsec.tsm.sim.sysconfig.service.LogFilterRuleService;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.sim.util.LicenceServiceUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.actiontemplate.ActionTemplate;
import com.topsec.tsm.util.actiontemplate.ActionTemplateElementFormater;
import com.topsec.tsm.util.actiontemplate.PropertyEntry;
import com.topsec.tsm.util.actiontemplate.accessor.ActionTemplateUiAccessor;
import com.topsec.tsm.util.xml.DefaultDocumentFormater;


@Controller
@RequestMapping("monitor")
public class MonitorController {
	private static final Logger logger = LoggerFactory.getLogger(MonitorController.class) ;
	
	private DataSourceService monitorService ;	
	
	/**
	 * 监视对象树，此方法返回的对象即包含监视对象的分类，也包含每种监视对象的信息
	 * @return
	 */
	@RequestMapping("monitorCategory")
	@ResponseBody
	public Object monitorCategory(String deviceType) {
		if ("all".equals(deviceType)) {
			deviceType = null;
		}
		JSONArray result = DataSourceUtil.getJSONTree(SimDatasource.DATASOURCE_TYPE_MONITOR,deviceType, true);
		if(result == null) {
			result = new JSONArray(0);
		}
		return result;
	}
	/**
	 * 资产监视对象信息
	 * @param ip 资产ip
	 * @param request
	 * @return
	 */
	@RequestMapping("assetMonitor")
	public Object assetMonitor(@RequestParam("tabSeq")String tabSeq,@RequestParam("ip")String ip,HttpServletRequest request) {
		AssetObject asset = AssetFacade.getInstance().getAssetByIp(ip) ;
		SimDatasource ds = monitorService.getFirstByIp(ip) ;
		JSONObject monitor = new JSONObject();
		if(ds != null){
			monitor = FastJsonUtil.toJSON(ds,"resourceId","deviceType","nodeId","securityObjectType","resourceName","collectMethod","available") ;
			monitor.put("deviceTypeName", DeviceTypeNameUtil.getDeviceTypeName(ds.getSecurityObjectType())) ;
			/*
			AlarmMonitorService alarmMonitorService = (AlarmMonitorService) SpringWebUtil.getBean("alarmMonitorService", request) ;
			List<EventPolicyMonitor> alarmPolicys = alarmMonitorService.getByMonitorId(ds.getResourceId()) ;
			monitor.put("alarmPolicys", alarmPolicys) ;
			*/
		}
		request.setAttribute("deviceType", asset.getDeviceType()) ;
		request.setAttribute("monitor", monitor) ;
		request.setAttribute("tabSeq", tabSeq) ;
		request.setAttribute("ip", ip) ;
		return "/page/asset/asset_detail_monitor";
	}
	@RequestMapping("showAdd")
	public String showAdd(@RequestParam("ip")String ip,HttpServletRequest request) {
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request) ;
		Device device = deviceService.getDeviceByIp(ip) ;
		request.setAttribute("ip", ip) ;
		request.setAttribute("name", device.getName()) ;
		request.setAttribute("deviceType", device.getDeviceType()) ;
		request.setAttribute("operation", "add") ;
		request.setAttribute("scanNodeId", device.getScanNodeId()) ;
		return "/page/asset/add_monitor" ;
	}
	@RequestMapping("showEdit")
	public String showEdit(@RequestParam("id")Long resourceId,HttpServletRequest request) {
		SimDatasource monitor = monitorService.getById(resourceId) ;
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request) ;
		Device device = deviceService.getDeviceByIp(monitor.getDeviceIp()) ;
		request.setAttribute("ip", monitor.getDeviceIp()) ;
		request.setAttribute("name", monitor.getResourceName()) ;
		request.setAttribute("deviceType", device.getDeviceType()) ;
		request.setAttribute("scanNodeId", AssetFacade.getInstance().getAssetByIp(monitor.getDeviceIp()).getScanNodeId()) ;
		request.setAttribute("operation", "edit") ;
		request.setAttribute("monitor", monitor) ;
		if(monitor.getTimerType() != null){
			request.setAttribute("timerExpression", new TimeExpression(monitor.getTimerType(), monitor.getTimer())) ;
		}
		return "/page/asset/add_monitor" ;
	}
	
	/**
	 * 列表监视对象表单回显
	 * @param operation
	 * @param request
	 * @return
	 */
	@RequestMapping("monitorForm")
	@IgnoreSecurityCheck
	public String monitorForm(@RequestParam("operation")String operation, HttpServletRequest request) {
		if("add".equals(operation)){
			request.setAttribute("operation", "add");
		}else if("edit".equals(operation)){
			SimDatasource monitor = monitorService.getById(Long.valueOf(request.getParameter("id")));
			DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request);
			Device device = deviceService.getDeviceByIp(monitor.getDeviceIp());
			request.setAttribute("ip", monitor.getDeviceIp());
			request.setAttribute("name", monitor.getResourceName());
			request.setAttribute("deviceType", device.getDeviceType());
			request.setAttribute("operation", "edit");
			request.setAttribute("scanNodeId", AssetFacade.getInstance().getAssetByIp(monitor.getDeviceIp()).getScanNodeId());
			request.setAttribute("monitor", monitor);
			if(monitor.getTimerType() != null){
				request.setAttribute("timerExpression", new TimeExpression(monitor.getTimerType(), monitor.getTimer()));
			}
		}
		return "page/asset/monitor_form";
	}
	
	/**
	 * 告警策略新建页面
	 * @param ip
	 * @param securityObjectType
	 * @param request
	 * @return
	 */
	@RequestMapping("showAddAlarmPolicy")
	public String showAddAlarmPolicy(@RequestParam("monitorId")Long monitorId,
									 @RequestParam("securityObjectType")String securityObjectType,
								     HttpServletRequest request) {
		EventResponseService responseServcie = (EventResponseService) SpringWebUtil.getBean("eventResponseService", request) ;
		List<Response> responses = responseServcie.getResponsesByType(ConfigType.TYPE_RESPONSE, 1, Integer.MAX_VALUE) ;//所有响应方式
		request.setAttribute("monitorId", monitorId) ;
		request.setAttribute("securityObjectType", securityObjectType) ;
		request.setAttribute("responses", responses) ;
		return "/page/asset/add_alarm_policy" ;
	}

	/**
	 * 告警策略新建页面
	 * 
	 * @param ip
	 * @param securityObjectType
	 * @param request
	 * @return
	 */
	@RequestMapping("showEditAlarmPolicy")
	public String showEditAlarmPolicy(
			@RequestParam("monitorId") Long monitorId,
			@RequestParam("securityObjectType") String securityObjectType,
			@RequestParam("id") String id, HttpServletRequest request) {
		EventResponseService responseServcie = (EventResponseService) SpringWebUtil.getBean("eventResponseService", request);
		List<Response> responses = responseServcie.getResponsesByType(ConfigType.TYPE_RESPONSE, 1, Integer.MAX_VALUE);// 所有响应方式
		AlarmMonitorService service = (AlarmMonitorService) SpringWebUtil.getBean("alarmMonitorService", request);
		EventPolicyMonitor eventPolicyMonitor = service.get(id);
		StringBuffer responseIds = new StringBuffer();
		Set<Response> responseSet = eventPolicyMonitor.getResponses();
		int count = 0;
		for (Response response : responseSet) {
			responseIds.append(response.getId());
			if (count++ != (responseSet.size() - 1)) {
				responseIds.append(",");
			}
		}
		request.setAttribute("responses", responses);
		request.setAttribute("monitorId", monitorId);
		request.setAttribute("securityObjectType", securityObjectType);
		request.setAttribute("eventPolicyMonitor", eventPolicyMonitor);
		request.setAttribute("responseIds", responseIds.toString());
		return "/page/asset/add_alarm_policy";
	}

	/**
	 * 告警策略过滤器
	 * @param secuirtyObjectType
	 * @param request
	 * @return
	 */
	@RequestMapping("alarmPolicyFilter")
	public String alarmPolicyFilter(@RequestParam("securityObjectType")String secuirtyObjectType,HttpServletRequest request) {
		List<FilterField> fieldset = MonitorParamsUtil.getInstance().getParamsByType(secuirtyObjectType) ;
		request.setAttribute("fieldset", fieldset);
		return "page/sysconfig/filter_editor" ;
	}
	
	@RequestMapping("addAlarmPolicy")
	@ResponseBody
	public Object addAlarmPolicy(@ModelAttribute EventPolicyMonitor alarmPolicy,
		                         HttpServletRequest request,
		                         HttpSession session){
		String[] responseIds = request.getParameterValues("responseId") ;
		if(ObjectUtils.isNotEmpty(responseIds)){
			EventResponseService responseServcie = (EventResponseService) SpringWebUtil.getBean("eventResponseService", request) ;
			Set<Response> responses = new HashSet<Response>() ;
			for(String responseId:responseIds){
				Response response = responseServcie.getResponse(responseId) ;
				responses.add(response) ;
			}
			alarmPolicy.setResponses(responses) ;
		}
		Result result = new Result() ;
		try {
			AlarmMonitorService service = (AlarmMonitorService) SpringWebUtil.getBean("alarmMonitorService", request) ;
			service.save(alarmPolicy) ;
			SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
			AuditLogFacade.addSuccess("添加告警策略",sid.getUserName(), "添加告警策略:"+alarmPolicy.getName(), new IpAddress(sid.getLoginIP())) ;
			result.buildSuccess(null) ;
		} catch (Exception e) {
			logger.error("告警策略保存失败！",e) ;
			result.buildError("告警策略保存失败，系统内部错误!") ;
		}
		return result ;
	}

	@RequestMapping("editAlarmPolicy")
	@ResponseBody
	public Object editAlarmPolicy(
			@ModelAttribute EventPolicyMonitor alarmPolicy,
			HttpServletRequest request,
			HttpSession session) {
		String[] responseIds = request.getParameterValues("responseId");
		if (ObjectUtils.isNotEmpty(responseIds)) {
			EventResponseService responseServcie = (EventResponseService) SpringWebUtil
					.getBean("eventResponseService", request);
			Set<Response> responses = new HashSet<Response>();
			for (String responseId : responseIds) {
				Response response = responseServcie.getResponse(responseId);
				responses.add(response);
			}
			alarmPolicy.setResponses(responses);
		}
		Result result = new Result();
		try {
			AlarmMonitorService service = (AlarmMonitorService) SpringWebUtil.getBean("alarmMonitorService", request);
			service.update(alarmPolicy);
			SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
			AuditLogFacade.updateSuccess("更新告警策略",sid.getUserName(), "更新告警策略:"+alarmPolicy.getName(), new IpAddress(sid.getLoginIP())) ;
			result.buildSuccess(null);
		} catch (Exception e) {
			logger.error("告警策略保存失败！", e);
			result.buildError("告警策略保存失败，系统内部错误!");
		}
		return result;
	}

	@RequestMapping("deleteAlarmPolicy")
	@ResponseBody
	public Object deleteAlarmPolicy(@RequestParam("id")String id,HttpServletRequest request,HttpSession session) {
		Result result = new Result() ;
		AlarmMonitorService service = (AlarmMonitorService) SpringWebUtil.getBean("alarmMonitorService", request) ;
		EventPolicyMonitor alarmPolicy = service.delete(new EventPolicyMonitor(id)) ;
		SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
		AuditLogFacade.deleteSuccess("删除告警策略",sid.getUserName(), "删除告警策略:"+alarmPolicy.getName(), new IpAddress(sid.getLoginIP())) ;
		return result ;
	}
	/**
	 * 根据日志收集方式，返回所有可用的收集日志节点
	 * @param collectType
	 * @param request
	 * @return
	 */
	@RequestMapping("getAvailableNodes")
	@ResponseBody
	public Object getAvailableNodes(@RequestParam("collectType")String collectType,HttpServletRequest request) {
		NodeMgrFacade nodeMgr = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		List<Node> nodes = nodeMgr.getAllNodesWithComponents() ;
		List<Map<String,Object>> availableNodes = NodeUtil.getComponentsByCollectMethod(nodes, collectType) ;
		JSONArray result = FastJsonUtil.toJSONArray(availableNodes, "nodeId","auditorNodeId","componentId","nodeName") ;
		return result ;
	}
	
	/**
	 * 根据监视对象类型与收集方式获取监视对象配置信息
	 * @param securityObjectType
	 * @return
	 */
	@RequestMapping(value="getConfigParam")
	@ResponseBody
	public Object getConfigParam(@RequestParam(value="securityObjectType",required=false)String securityObjectType,
			                     @RequestParam(value="collectType",required=false)String collectType,
			                     @RequestParam(value="monitorId",required=false)Long monitorId,
			                     @RequestParam(value="ip",required=false)String ip,
			                     SID sid) {
		try {
			SID.setCurrentUser(sid) ;
			ActionTemplate template = null;
			if(monitorId != null){
				try {
					ActionTemplateElementFormater  templateFormater=new ActionTemplateElementFormater();
					DefaultDocumentFormater docmentFormater=new DefaultDocumentFormater(templateFormater);
					SimDatasource monitor = monitorService.getById(monitorId) ;
					collectType = monitor.getCollectMethod() ;
					docmentFormater.importObjectFromString(monitor.getActionTemplate()) ;
					template = templateFormater.getActionTemplate() ;
				} catch (XmlAccessException e) {
					e.printStackTrace();
				}
			}else{
				template = DataSourceUtil.getDataSourceTemplate(securityObjectType, collectType, SimDatasource.DATASOURCE_TYPE_MONITOR) ;
			}
			if (template != null) {
				ActionTemplateUiAccessor accessor = new ActionTemplateUiAccessor() ;
				accessor.setActionTemplate(template) ;
				List<PropertyEntry> properties = accessor.getProperties(true) ;
				JSONArray propertyJSON = PropertyEntryUtil.toJSON(collectType, properties,AssetFacade.getInstance().getAssetByIp(ip)) ;
				JSONObject result = new JSONObject(2) ;
				result.put("properties", propertyJSON) ;
				result.put("displayControl", PropertyEntryUtil.getDisplayControl(collectType));
				return result;
			}else{
				return null ;
			}
		}finally{
			SID.removeCurrentUser() ;
		}
	}
	
	@RequestMapping("save")
	@ResponseBody
	public Object save(HttpServletRequest request,HttpSession session) {
		String operation = request.getParameter("operation") ;
		Result result = new Result() ;
		Parameter params = new Parameter(request.getParameterMap()) ;
		try{
			SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
			String ip = params.getValue("ip") ;
			SimDatasource datasource = buildDataSource(params, sid) ;
			AssetObject ao = AssetFacade.getInstance().getAssetByIp(datasource.getDeviceIp()) ;
			if(ao.getEnabled() == 0){
				throw new AssetException("操作失败,此资产已被禁用！") ;
			}
			if(operation.equals("add")){
				if(monitorService.getFirstByIp(ip) == null){//监视对象不存在
					monitorService.save(datasource) ;
					NodeUtil.sendCommand(RouteUtils.getAuditorRoutes(), MessageDefinition.CMD_DATASOURCE_ADD, null) ;
					AuditLogFacade.addSuccess("添加监视对象",sid.getUserName(), "添加监视对象:"+datasource.getResourceName(), new IpAddress(sid.getLoginIP())) ;
				}else{
					result.buildError("此资产的监视对象已经存在！") ;
				}
			}else{
				datasource.setResourceId(params.getLong("id")) ;
				monitorService.update(datasource) ;
				AuditLogFacade.updateSuccess("更新监视对象",sid.getUserName(), "更新监视对象:"+datasource.getResourceName(), new IpAddress(sid.getLoginIP())) ;
			}
		}catch(ComponentNotFoundException e){
			result.buildError("没有找到合适的收集组件") ;
		}catch(AssetException e){
			result.buildError(e.getMessage()) ;
		}catch(TimeExpressionException e){
			result.buildError("非法的轮询时间！") ;
		}catch (ResourceNameExistException e) {
			result.build(false,"监视对象名称已经存在！") ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping("switchState")
	@ResponseBody
	public Object switchState(@RequestParam("id")String[] monitorIds,@RequestParam("available")boolean available,
			HttpSession session,HttpServletRequest request) {
		SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
		Result result = new Result() ;
		if(ObjectUtils.isEmpty(monitorIds)){
			return result;
		}
		for(String monitorId : monitorIds){
			if(StringUtil.isBlank(monitorId)) {
				continue;
			}
			try{
				SimDatasource monitor = monitorService.getById(Long.valueOf(monitorId)) ;
				AssetObject ao = AssetFacade.getInstance().getAssetByIp(monitor.getDeviceIp()) ;
				if(ao != null && ao.getEnabled() == 0){
					//当日志源所属的资产禁用，启用日志源的同时启用其所属的资产
					if (available) {
						AssetService assetService = (AssetService) SpringWebUtil.getBean("assetService", request);
						checkLicenseLimit(assetService.getEnabledTotal());
						NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade");
						if(nodeMgrFacade.getNodeByNodeId(ao.getScanNodeId()) == null){
							throw new AssetException(ao.getName()+"的管理节点已删除，请指定其它管理节点，再启用！");
						}
						AssetFacade.getInstance().changeState(ao.getId(), 1, false);
						AuditLogFacade.start("启用资产", sid.getUserName(), "启用资产"+ao.getName(), new IpAddress(sid.getLoginIP()));
					}
					//throw new AssetException("操作失败,监视对象所属的资产已被禁用！") ;
				}
				
				monitorService.switchState(Long.valueOf(monitorId), available ? 1 : 0) ;
				if(available){
					AuditLogFacade.start("启用监视对象",sid.getUserName(), "启用监视对象:"+monitor.getResourceName(), new IpAddress(sid.getLoginIP())) ;
				}else{
					AuditLogFacade.stop("禁用监视对象",sid.getUserName(), "禁用监视对象:"+monitor.getResourceName(), new IpAddress(sid.getLoginIP())) ;
				}
				result.buildSuccess(monitorId) ;
			} catch (InvalidLicenseException e) {
				result.buildError("当前License无效！");
			} catch (LimitedNumException e) {
				result.buildError("启用的资产已达License上限！");
			} catch (Exception e) {
				logger.error("MonitorController.switchState(Long, boolean).switchState Exception!",e) ;
				result.buildError("系统内部错误！") ;
			}
		}
		return result ;
	}
	
	/**
	 * license检测
	 * @param total
	 * @throws InvalidLicenseException
	 * @throws LimitedNumException
	 */
	private void checkLicenseLimit(int total) throws InvalidLicenseException, LimitedNumException {
		Map licenceMap=LicenceServiceUtil.getInstance().getLicenseInfo();
		String licenseValid=(String)licenceMap.get("LICENSE_VALID");
		if(licenseValid==null||licenseValid.equals("0")){
			throw new InvalidLicenseException("Licence invalid!!!");
		} 
		int licenceNum = Integer.valueOf((String)licenceMap.get("TSM_ASSET_NUM"));
		if(total >= licenceNum){
			throw new LimitedNumException("dataCount.compareTo(licenceNum)>=0!!!");
		}
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Object delete(@RequestParam("id")String[] ids,HttpServletRequest request,HttpSession session){
		Result result = new Result() ;
		if(ObjectUtils.isEmpty(ids)){
			result.build(false, "无效的监视对象信息") ;
			return result;
		}
		for(String id : ids){
			if(StringUtil.isBlank(id)) {
				continue;
			}
			try {
				//SimDatasource datasource = monitorService.getById(Long.valueOf(id));
				/*AssetObject ao = AssetFacade.getInstance().getAssetByIp(datasource.getDeviceIp());
				if(ao.getEnabled() == 0){
					throw new AssetException("操作失败,监视对象所属的资产已被禁用！");
				}*/
				SimDatasource monitor = monitorService.delete(Long.valueOf(id)) ;
				SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
				AuditLogFacade.deleteSuccess("删除监视对象",sid.getUserName(), "删除监视对象:"+monitor.getResourceName(), new IpAddress(sid.getLoginIP())) ;
			} catch (Exception e) {
				logger.error("删除监视对象失败!", e) ;
				result.build(false,"监视对象删除失败！") ;
			}
		}
		return result ;
	}
	
	/**
	 * 根据提交参数信息，创建监视对象对象
	 * @param params
	 * @return
	 * @throws TimeExpressionException 
	 */
	private SimDatasource buildDataSource(Parameter params, SID sid) throws TimeExpressionException {
		SimDatasource dataSource = new SimDatasource() ;
		String collectType = params.getValue("collectType") ;
		String securityObjectType = params.getValue("dataSourceType") ;
		dataSource.setOwnGroup(SimDatasource.DATASOURCE_TYPE_MONITOR) ;
		dataSource.setResourceName(params.getValue("name")) ;
		dataSource.setDeviceIp(params.getValue("ip")) ;
		dataSource.setSecurityObjectType(securityObjectType) ;
		dataSource.setDataObjectType(params.getValue("dataObjectType")) ;
		dataSource.setCollectMethod(collectType) ;
		dataSource.setAvailable(params.getInt("enabled",0)) ;
		dataSource.setNodeId(params.getValue("collectNode") ) ;
		dataSource.setAuditorNodeId(params.getValue("auditorNodeId")) ;
		dataSource.setComponentId(params.getLong("componentId")) ;
		dataSource.setDuration(params.getInt("duration", 30) * 60 * 1000) ;//将分钟换算为秒,默认为30分钟 
		dataSource.setRate(params.getInt("rate",0)) ;
		dataSource.setArchiveTime(params.getValue("archiveTime","6m")) ;
		dataSource.setLastModifyedTime(new Date()) ;
		dataSource.setCreateTime(new Date()) ;
		dataSource.setCreater(sid.getUserName());
		dataSource.setOverwriteEventTime(1) ;
		dataSource.setResourceType(ResourceType.TYPE_SIMDATASOURCE);
		dataSource.setReadonly(0) ;
		if(params.getBoolean("isJob")){
			DataSourceUIUtil.buildJob(params, dataSource) ;
		}
		ActionTemplate template = DataSourceUIUtil.buildActionTemplate(collectType, securityObjectType, dataSource.getOwnGroup(), params);
		if (template != null) {
			try {
				 ActionTemplateElementFormater  templateFormater=new ActionTemplateElementFormater(template);
				 DefaultDocumentFormater docmentFormater=new DefaultDocumentFormater(templateFormater);
				 dataSource.setActionTemplate(docmentFormater.exportObjectToString()) ;
			} catch (XmlAccessException e) {
				logger.error("监视对象模板转换失败！",e) ;
			}
		}
		return dataSource;
	}
	@Autowired
	@Qualifier("monitorService")
	public void setMonitorService(DataSourceService monitorService) {
		this.monitorService = monitorService;
	}
	
	/**
	 * 查询监视对象列表
	 * @param resourceId
	 * @param request
	 * @return
	 */
	@RequestMapping("showMonitorList")
	@ResponseBody
	public Object showMonitorList(SID sid, @RequestParam Map<String,Object> searchCondition,
			@RequestParam(value = "page", defaultValue = "1") Integer pageNumber, 
			@RequestParam(value = "rows", defaultValue = "20") Integer pageSize,
			String order, String sort, HttpServletRequest request) {
		List<SimOrder> orders = new ArrayList<SimOrder>();
		if (order != null && sort != null) {
			orders.add(order.equals("asc") ? SimOrder.asc(sort) : SimOrder.desc(sort));
		}
		PageBean<SimDatasource> page = monitorService.getList(sid, pageNumber, pageSize, searchCondition, orders.toArray(new SimOrder[0]));
		final NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade", request);
		final LogFilterRuleService logFilterRuleService = (LogFilterRuleService) SpringWebUtil.getBean("logFilterRuleService", request);
		final AggregatorRuleService aggregatorRuleService = (AggregatorRuleService) SpringWebUtil.getBean("aggregatorRuleService", request);
		JSONObject result = new JSONObject();
		result.put("total", page.getTotal());
		result.put("rows", FastJsonUtil.toJSONArray(page.getData(), new JSONConverterCallBack<SimDatasource>(){
			@Override
			public void call(JSONObject result, SimDatasource obj) {
				result.put("nodeId", obj.getNodeId());
				SimRule simRule = logFilterRuleService.getSimRule(obj.getRuleId());
				if(simRule!=null){
					result.put("rule", simRule.getName());
				}
				AggregatorScene aggregatorScene = aggregatorRuleService.getAggregatorScene(obj.getAggregatorId());
				if(aggregatorScene!=null){
					result.put("aggregator", aggregatorScene.getName());
				}
				result.put("nodeIp", nodeFacade.getNodeByNodeId(obj.getNodeId()).getIp());
				result.put("deviceTypeName",DeviceTypeNameUtil.getDeviceTypeName(obj.getSecurityObjectType(),Locale.getDefault()));
			}
		}, "deviceIp","resourceId","resourceName","deviceType","collectMethod","available","rate","archiveTime","saveRawLog","creater"));
		
		return result;
	}
	
	/**
	 * 判断设备地址是否存在，否则无法新建监视对象
	 * @param ip
	 * @param request
	 * @return
	 */
	@RequestMapping(value="checkExistIp", produces="text/javascript; charset=utf-8")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object checkExistIp(@RequestParam("ip") String ip, @RequestParam("id") Long id, HttpServletRequest request) {
		JSONObject result = new JSONObject();
		if(StringUtil.isBlank(ip)){
			result.put("error", "IP地址不能为空");
			return result;
		}
		if (!Ipv4Address.validIPv4(ip)) {
			result.put("error", "IP地址无效");
			return result;
		}
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("assetService", request);
		Device device = deviceService.getDeviceByIp(ip);
		if(device == null){
			result.put("error", "资产不存在");
		} else {
			SimDatasource monitor = monitorService.getById(id);
			if (monitor != null && !monitor.getDeviceIp().equals(ip)) {
				result.put("error", "该设备已经存在监视对象");
			} else {
				result.put("ok","");
			}
		}
		return result;
	}
	
	/**
	 * 判断监视对象是否存在（监视对象只允许有一条），否则无法新建监视对象
	 * @param name
	 * @param id
	 * @param operation
	 * @param request
	 * @return
	 */
	@RequestMapping(value="checkExistName", produces="text/javascript; charset=utf-8")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object checkExistName(@RequestParam("name") String name, @RequestParam("id") Long id, 
			@RequestParam("operation") String operation, HttpServletRequest request) {
		JSONObject result = new JSONObject();
		if (StringUtil.isBlank(name)) {
			result.put("error", "监视对象名称不能为空");
			return result;
		}
		if ("add".equals(operation)) {
			if(monitorService.isResourceNameExist(name)){
				result.put("error", "监视对象名称已经存在");
			} else {
				result.put("ok","");
			}
		} else {
			SimDatasource sd = monitorService.getById(id);
			if(monitorService.isResourceNameExist(name) && !sd.getResourceName().equals(name)){
				result.put("error", "监视对象名称已经存在");
			} else {
				result.put("ok","");
			}
		}
		
		return result;
	}
	
	/**
	 * 根据输入的设备地址查询其设备类型
	 * @param ip
	 * @param request
	 * @return
	 */
	@RequestMapping(value="getDeviceType", produces="text/javascript; charset=utf-8")
	@ResponseBody
	public Object getDeviceType(@RequestParam("ip") String ip, HttpServletRequest request) {
		JSONObject result = new JSONObject();
		if(StringUtil.isBlank(ip) || !Ipv4Address.validIPv4(ip)){
			return result;
		}
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("assetService", request);
		Device device = deviceService.getDeviceByIp(ip);
		if(device != null){
			result.put("deviceType", device.getDeviceType());
			result.put("scanNodeId", AssetFacade.getInstance().getAssetByIp(ip).getScanNodeId());
		}
		return result;
	}
}
