package com.topsec.tsm.sim.asset.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Ipv4Address;
import com.topsec.tsm.base.xml.XmlAccessException;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.comm.NodeMessageObject;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.PropertyEntryUtil;
import com.topsec.tsm.sim.asset.exception.AssetException;
import com.topsec.tsm.sim.asset.exception.InvalidLicenseException;
import com.topsec.tsm.sim.asset.exception.LimitedNumException;
import com.topsec.tsm.sim.asset.service.AssetService;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.common.exception.TimeExpressionException;
import com.topsec.tsm.sim.common.web.IgnoreSecurityCheck;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.ComponentNotFoundException;
import com.topsec.tsm.sim.node.exception.DataSourceException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.resource.object.ResourceType;
import com.topsec.tsm.sim.resource.persistence.Node;
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
import com.topsec.tsm.util.actiontemplate.ActionTemplate;
import com.topsec.tsm.util.actiontemplate.ActionTemplateElementFormater;
import com.topsec.tsm.util.actiontemplate.PropertyEntry;
import com.topsec.tsm.util.actiontemplate.accessor.ActionTemplateUiAccessor;
import com.topsec.tsm.util.xml.DefaultDocumentFormater;


@Controller
@RequestMapping("datasource")
public class DataSourceController {
	private static final Logger logger = LoggerFactory.getLogger(DataSourceController.class) ;
	
	private DataSourceService dataSourceService ;	
	/**
	 * 日志源分类树,只包含日志源的分类信息，不包含日志的具体信息
	 * @return
	 */
	@RequestMapping("categoryTree")
	@ResponseBody
	public Object categoryTree() {
		return DataSourceUtil.getJSONTree(SimDatasource.DATASOURCE_TYPE_LOG) ;
	}
	
	/**
	 * 日志源树，此方法返回的对象即包含日志源的分类，也包含每种日志源的信息
	 * @return
	 */
	@RequestMapping("dataSourceTree")
	@ResponseBody
	public Object dataSourceTree(@RequestParam("deviceType")String deviceType) {
		if ("all".equals(deviceType)) {
			deviceType = null;
		}
		JSONArray result = DataSourceUtil.getJSONTree(SimDatasource.DATASOURCE_TYPE_LOG, deviceType,true);
		if(result == null) {
			result = new JSONArray(0);
		}
		return result;
	}
	@RequestMapping("userDataSourceTree")
	@ResponseBody
	public Object userDataSourceTree(SID sid,@RequestParam(value="includeDataSource",defaultValue="true")boolean includeDataSource){
		List<SimDatasource> userDataSources = null ;
		JSONArray treeData = DataSourceUtil.getJSONTree(userDataSources,includeDataSource) ;
		return treeData ;
	}
	/**
	 * 资产日志源信息
	 * @param ip 资产ip
	 * @param request
	 * @return
	 */
	@RequestMapping("assetDataSource")
	public Object assetDataSource(@RequestParam("ip")String ip,
			@RequestParam("tabSeq")String tabSeq,HttpServletRequest request) {
		AssetObject asset = AssetFacade.getInstance().getAssetByIp(ip) ;
		dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request) ;
		List<SimDatasource> assetDataSources = dataSourceService.getByIp(ip) ;
		final NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade", request) ;
        final LogFilterRuleService logFilterRuleService = (LogFilterRuleService) SpringWebUtil.getBean("logFilterRuleService", request) ;
        final AggregatorRuleService aggregatorRuleService = (AggregatorRuleService) SpringWebUtil.getBean("aggregatorRuleService", request) ;

		JSONArray result = new JSONArray();
		if(assetDataSources != null){
			result = FastJsonUtil.toJSONArray(assetDataSources,new JSONConverterCallBack<SimDatasource>(){
				@Override
				public void call(JSONObject result, SimDatasource obj) {
					result.put("nodeId", obj.getNodeId()) ;
					SimRule simRule = logFilterRuleService.getSimRule(obj.getRuleId());
					if(simRule!=null){
						result.put("rule", simRule.getName()) ;
					}
					AggregatorScene aggregatorScene = aggregatorRuleService.getAggregatorScene(obj.getAggregatorId());
					if(aggregatorScene!=null){							
						result.put("aggregator", aggregatorScene.getName()) ;
					}
					result.put("nodeIp", nodeFacade.getNodeByNodeId(obj.getNodeId()).getIp()) ;
					result.put("deviceTypeName",DeviceTypeNameUtil.getDeviceTypeName(obj.getSecurityObjectType(),Locale.getDefault()));
				}
			}, "resourceId","resourceName","deviceType","collectMethod","available","rate","archiveTime","saveRawLog") ;
		}
		request.setAttribute("assetDatasourceJson", result) ;
		request.setAttribute("tabSeq", tabSeq) ;
		request.setAttribute("ip", ip) ;
		request.setAttribute("deviceType", asset.getDeviceType()) ;
		request.setAttribute("assetEnabled", asset.getEnabled() == 1) ;
		return "/page/asset/asset_detail_datasource";
	}
	@RequestMapping("showAdd")
	public String showAdd(@RequestParam("ip")String ip,HttpServletRequest request) {
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request) ;
		Device device = deviceService.getDeviceByIp(ip) ;
		request.setAttribute("ip", ip) ;
		request.setAttribute("name", device.getName()) ;
		request.setAttribute("deviceType", device.getDeviceType()) ;
		request.setAttribute("scanNodeId", device.getScanNodeId()) ;
		request.setAttribute("operation", "add") ;
		return "/page/asset/add_datasource" ;
	}
	
	@RequestMapping("showEdit")
	public String showEdit(@RequestParam("id")Long resourceId,HttpServletRequest request) {
		SimDatasource dataSource = dataSourceService.getById(resourceId) ;
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request) ;
		Device device = deviceService.getDeviceByIp(dataSource.getDeviceIp()) ;
		request.setAttribute("ip", dataSource.getDeviceIp()) ;
		request.setAttribute("name", dataSource.getResourceName()) ;
		request.setAttribute("deviceType", device.getDeviceType()) ;
		request.setAttribute("operation", "edit") ;
		request.setAttribute("scanNodeId", AssetFacade.getInstance().getAssetByIp(dataSource.getDeviceIp()).getScanNodeId()) ;
		request.setAttribute("dataSource", dataSource) ;
		if(dataSource.getTimerType() != null){
			request.setAttribute("timerExpression", new TimeExpression(dataSource.getTimerType(), dataSource.getTimer())) ;
		}
		return "/page/asset/add_datasource" ;
	}
	
	/**
	 * 列表日志源表单回显
	 * @param operation
	 * @param request
	 * @return
	 */
	@RequestMapping("dataSourceForm")
	@IgnoreSecurityCheck
	public String dataSourceForm(@RequestParam("operation")String operation, HttpServletRequest request) {
		if("add".equals(operation)){
			request.setAttribute("operation", "add");
		}else if("edit".equals(operation)){
			SimDatasource dataSource = dataSourceService.getById(Long.valueOf(request.getParameter("id")));
			DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request);
			Device device = deviceService.getDeviceByIp(dataSource.getDeviceIp());
			request.setAttribute("ip", dataSource.getDeviceIp());
			request.setAttribute("name", dataSource.getResourceName());
			request.setAttribute("deviceType", device.getDeviceType());
			request.setAttribute("operation", "edit");
			request.setAttribute("scanNodeId", AssetFacade.getInstance().getAssetByIp(dataSource.getDeviceIp()).getScanNodeId());
			request.setAttribute("dataSource", dataSource);
			if(dataSource.getTimerType() != null){
				request.setAttribute("timerExpression", new TimeExpression(dataSource.getTimerType(), dataSource.getTimer()));
			}
		}
		return "page/asset/datasource_form";
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
		JSONArray result = FastJsonUtil.toJSONArray(availableNodes, "nodeId","auditorNodeId","componentId","nodeName","online") ;
		return result ;
	}
	/**
	 * 根据收集节点和收集方式获取节点对应的收集组件信息
	 * @param collectType
	 * @param manageNodeId
	 * @param request
	 * @return
	 */
	@RequestMapping("getCollectComponent")
	@ResponseBody
	public Object getCollectComponent(@RequestParam("collectType")String collectType,@RequestParam("collectNode")String manageNodeId,HttpServletRequest request){
		NodeMgrFacade nodeMgr = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Node collectNode = NodeUtil.getCollectNode(manageNodeId, nodeMgr) ;
		return NodeUtil.getComponentByCollectMethod(collectNode, collectType) ;
	}
	/**
	 * 根据日志源类型与收集方式获取日志源配置信息,或者根据日志源id获取配置信息
	 * @param securityObjectType
	 * @return
	 */
	@RequestMapping(value="getConfigParam")
	@ResponseBody
	public Object getConfigParam(@RequestParam(value="securityObjectType",required=false)String securityObjectType,
			                     @RequestParam(value="collectType",required=false)String collectType,
			                     @RequestParam(value="dataSourceId",required=false)Long dataSourceId,
			                     @RequestParam(value="ip",required=false)String ip,
			                     SID sid) {
		try {
			SID.setCurrentUser(sid) ;
			ActionTemplate template = null;
			if (dataSourceId != null) {
				try {
					ActionTemplateElementFormater templateFormater = new ActionTemplateElementFormater();
					DefaultDocumentFormater docmentFormater = new DefaultDocumentFormater(
							templateFormater);
					SimDatasource dataSource = dataSourceService
							.getById(dataSourceId);
					collectType = dataSource.getCollectMethod();
					docmentFormater.importObjectFromString(dataSource
							.getActionTemplate());
					template = templateFormater.getActionTemplate();
				} catch (XmlAccessException e) {
					e.printStackTrace();
				}
			} else {
				template = DataSourceUtil.getDataSourceTemplate(
						securityObjectType, collectType,
						SimDatasource.DATASOURCE_TYPE_LOG);
			}
			if (template != null) {
				ActionTemplateUiAccessor accessor = new ActionTemplateUiAccessor();
				accessor.setActionTemplate(template);
				List<PropertyEntry> properties = accessor.getProperties(true);
				JSONArray propertiesJSON = PropertyEntryUtil.toJSON(
						collectType, properties, AssetFacade.getInstance()
								.getAssetByIp(ip));
				JSONArray uiDisplayControl = PropertyEntryUtil
						.getDisplayControl(collectType);
				JSONObject result = new JSONObject(2);
				result.put("properties", propertiesJSON);
				result.put("displayControl", uiDisplayControl);
				return result;
			} else {
				return null;
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
			SimDatasource datasource = buildDataSource(params, sid) ;
			dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request) ;
			AssetObject ao = AssetFacade.getInstance().getAssetByIp(datasource.getDeviceIp()) ;
			if(ao.getEnabled() == 0){
				throw new AssetException("资产已被禁用！") ;
			}
			if(operation.equals("add")){
				dataSourceService.save(datasource) ;
				//向所有下级广播增加日志源事件
				NodeUtil.sendCommand(
						new String[]{NodeMessageObject.DOWN_BROADCAST_NODE}, 
						MessageDefinition.CMD_DATASOURCE_ADD, 
						DataSourceUtil.toDataSource(datasource)) ;
				AuditLogFacade.addSuccess("添加日志源",sid.getUserName(), "添加日志源:"+datasource.getResourceName(), new IpAddress(sid.getLoginIP())) ; 
			}else{
				datasource.setResourceId(params.getLong("id")) ;
				dataSourceService.update(datasource) ;
				AuditLogFacade.updateSuccess("更新日志源",sid.getUserName(), "更新日志源:"+datasource.getResourceName(), new IpAddress(sid.getLoginIP())) ;
			}
			result.build(true) ;
		}catch(ComponentNotFoundException e){
			result.buildError("没有找到合适的收集组件") ;
		}catch (ResourceNameExistException e) {
			result.buildError("日志源名称已经存在！") ;
		}catch(TimeExpressionException e){
			result.buildError("非法的轮询时间！") ;
		}catch(AssetException e){
			result.buildError(e.getMessage()) ;
		}catch(DataSourceException e){
			result.buildError(e.getMessage()) ;
		}catch (Exception e) {
			e.printStackTrace();
			result.buildError("系统内部错误!") ;
		}
		return result;
	}
	@RequestMapping("switchState")
	@ResponseBody
	public Object switchState(@RequestParam("id")String[] resourceIds,@RequestParam("available")boolean available,
			HttpSession session,HttpServletRequest request) {
		SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
		Result result = new Result();
		if(ObjectUtils.isEmpty(resourceIds)){
			return result ;
		}
		for(String resourceId : resourceIds){
			if(StringUtil.isBlank(resourceId)) {
				continue;
			}
			try{
				SimDatasource datasource = dataSourceService.getById(Long.valueOf(resourceId)) ;
				AssetObject ao = AssetFacade.getInstance().getAssetByIp(datasource.getDeviceIp()) ;
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
					//throw new AssetException("操作失败,日志源所属的资产已被禁用！") ;
				}
				dataSourceService.switchState(Long.valueOf(resourceId), available ? 1 : 0) ;
				if(available){
					AuditLogFacade.start("启用日志源",sid.getUserName(), "启用日志源:"+datasource.getResourceName(), new IpAddress(sid.getLoginIP())) ;
				}else{
					AuditLogFacade.stop("禁用日志源",sid.getUserName(), "禁用日志源:"+datasource.getResourceName(), new IpAddress(sid.getLoginIP())) ;
				}
			} catch (InvalidLicenseException e) {
				result.buildError("当前License无效！");
			} catch (LimitedNumException e) {
				result.buildError("启用的资产已达License上限！");
			} catch (Exception e) {
				logger.error("DataSourceController.switchState Exception!",e);
				result.buildError("系统内部错误！");
				break;
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
		Result result = new Result();
		if(ObjectUtils.isEmpty(ids)){
			result.build(false, "无效的日志源信息！") ;
			return result;
		}
		for(String id : ids){
			if(StringUtil.isBlank(id)) {
				continue;
			}
			try {
				/*dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
				SimDatasource datasource = dataSourceService.getById(Long.valueOf(id));
				AssetObject ao = AssetFacade.getInstance().getAssetByIp(datasource.getDeviceIp());
				if(ao.getEnabled() == 0){
					throw new AssetException("操作失败,日志源所属的资产已被禁用！");
				}*/
				SimDatasource datasource = dataSourceService.delete(Long.valueOf(id));
				SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID);
				AuditLogFacade.deleteSuccessHighest("删除日志源",sid.getUserName(), "删除日志源:"+datasource.getResourceName(), new IpAddress(sid.getLoginIP()));
			} catch (Exception e) {
				logger.error("删除日志源失败!", e);
				result.build(false,"日志源删除失败！");
				break;
			}
		}
		return result;
	}
	
	/**
	 * 根据提交参数信息，创建日志源对象
	 * @param params
	 * @return
	 * @throws TimeExpressionException 
	 */
	private SimDatasource buildDataSource(Parameter params, SID sid) throws TimeExpressionException {
		SimDatasource dataSource = new SimDatasource() ;
		String collectType = params.getValue("collectType") ;
		String securityObjectType = params.getValue("dataSourceType") ;
		dataSource.setOwnGroup(SimDatasource.DATASOURCE_TYPE_LOG) ;
		dataSource.setResourceName(StringUtil.trim(params.getValue("name"))) ;
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
		dataSource.setArchiveTime(params.getValue("archiveTime")) ;
		dataSource.setReportKeepTime(params.getValue("reportKeepTime")) ; 
		dataSource.setSaveRawLog(params.getInt("saveRawLog", 1)) ;
		dataSource.setRuleId(params.getLongValue("ruleId")) ;		// 为过滤器赋值
		dataSource.setAggregatorId(params.getLongValue("aggregatorId")) ;		// 为合并规则赋值
		dataSource.setLastModifyedTime(new Date()) ;
		dataSource.setCreateTime(new Date()) ;
		dataSource.setCreater(sid.getUserName());
		dataSource.setOverwriteEventTime(params.getInt("overwriteLogTime")) ;
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
				logger.error("日志源模板转换失败！",e) ;
			}
		}
		return dataSource;
	}
	@Autowired
	@Qualifier("dataSourceService")
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	
	/**
	 * 查询日志源列表
	 * @param resourceId
	 * @param request
	 * @return
	 */
	@RequestMapping("showDataSourceList")
	@ResponseBody
	public Object showDataSourceList(SID sid, @RequestParam Map<String,Object> searchCondition, 
			@RequestParam(value = "page", defaultValue = "1") Integer pageNumber, 
			@RequestParam(value = "rows", defaultValue = "20") Integer pageSize,
			String order, String sort, HttpServletRequest request) {
		
		List<SimOrder> orders = new ArrayList<SimOrder>();
		if (order != null && sort != null) {
			orders.add(order.equals("asc") ? SimOrder.asc(sort) : SimOrder.desc(sort));
		}
		PageBean<SimDatasource> page = dataSourceService.getList(sid, pageNumber, pageSize, searchCondition, orders.toArray(new SimOrder[0]));
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
		}, "deviceIp","resourceId","resourceName","deviceType","collectMethod","available","rate","archiveTime","saveRawLog",
		"overwriteEventTime","reportKeepTime","creater"));
		
		return result;
	}
	
	/**
	 * 判断设备地址是否存在，否则无法新建日志源
	 * @param ip
	 * @param request
	 * @return
	 */
	@RequestMapping(value="checkExistIp", produces="text/javascript; charset=utf-8")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object checkExistIp(@RequestParam("ip") String ip, HttpServletRequest request) {
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
			result.put("ok","");
		}
		return result;
	}
	
	/**
	 * 判断日志源名称是否存在，否则无法新建日志源
	 * @param name
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
			result.put("error", "日志源名称不能为空");
			return result;
		}
		if ("add".equals(operation)) {
			if(dataSourceService.isResourceNameExist(name)){
				result.put("error", "日志源名称已经存在");
			} else {
				result.put("ok","");
			}
		} else {
			SimDatasource sd = dataSourceService.getById(id);
			if(dataSourceService.isResourceNameExist(name) && !sd.getResourceName().equals(name)){
				result.put("error", "日志源名称已经存在");
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
	
	/**
	 * 收集方式列表
	 * @return
	 */
	@RequestMapping("getCollectMethodList")
	@ResponseBody
	public Object getCollectMethodList(){
		JSONArray osArray = new JSONArray() ;
		osArray.add(createJson("value", "Syslog"));
		osArray.add(createJson("value", "SNMPTrap"));
		osArray.add(createJson("value", "JDBC"));
		osArray.add(createJson("value", "WMI"));
		osArray.add(createJson("value", "TXT"));
		osArray.add(createJson("value", "NetFlow"));
		osArray.add(createJson("value", "FTP"));
		osArray.add(createJson("value", "SFTP"));
		osArray.add(createJson("value", "SCP"));
		osArray.add(createJson("value", "SNMPGet"));
		osArray.add(createJson("value", "SSH"));
		return osArray;
	}
	
	private JSONObject createJson(String key,Object value){
		JSONObject obj = new JSONObject();
		obj.put(key, value);
		return obj;
	}
}
