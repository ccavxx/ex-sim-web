package com.topsec.tsm.sim.asset.web;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.component.handler.MonitorState;
import com.topsec.tsm.node.status.ComponentStatusMap;
import com.topsec.tsm.node.status.NamespaceStatusMap;
import com.topsec.tsm.node.status.NodeStatusMap;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.StatusDefinition;
import com.topsec.tsm.sim.alarm.persistence.SimAlarm;
import com.topsec.tsm.sim.alarm.service.AlarmService;
import com.topsec.tsm.sim.asset.AssetAttribute;
import com.topsec.tsm.sim.asset.AssetAttributeType;
import com.topsec.tsm.sim.asset.AssetCategory;
import com.topsec.tsm.sim.asset.AssetCategoryUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DeviceTypeShortKeyUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.bean.EventModel;
import com.topsec.tsm.sim.event.service.EventService;
import com.topsec.tsm.sim.kb.Leak;
import com.topsec.tsm.sim.leak.service.LeakService;
import com.topsec.tsm.sim.newreport.model.ReportQuery;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueue;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.report.chart.highchart.HChart;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DateUtils;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.sim.util.NodeUtil;

@Controller
@RequestMapping("assetdetail")
public class AssetDetailController {
	private static Logger logger = LoggerFactory.getLogger(AssetDetailController.class) ;
	private AssetFacade assetFacade = AssetFacade.getInstance() ;
	private Map<Long,String> dataSourceComponentCache = new HashMap<Long,String>() ; 
	@Autowired
	LeakService leakService;
	@Autowired
	ReportQuery reportQuery;
	//资产详细页面组件id的后辍
	@RequestMapping("/showDetail")
	public String showAssetDetail(@RequestParam("ip")String ip,@RequestParam("tabSeq")String tabId,HttpServletRequest request){
		AssetObject asset =  assetFacade.getAssetByIp(ip) ;
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		if (asset != null) {
			AssetCategory category = AssetCategoryUtil.getInstance().getCategoryByPathId(asset.getDeviceType()) ;
			if(category != null ){
				AssetAttribute attr = category.getAttribute("interface") ;//接口属性
				request.setAttribute("interface", attr) ;
				request.setAttribute("scanNode", nodeFacade.getNodeByNodeId(asset.getScanNodeId())) ;
				request.setAttribute("asset", asset) ;
				request.setAttribute("accountPassword", StringUtil.decrypt(asset.getAccountPassword())) ;
				request.setAttribute("tabSeq", tabId) ;
				request.setAttribute("ip", ip) ;
				request.setAttribute("tools", category.getTools()) ;
				request.setAttribute("assetCategory", asset.getAssetCategory()) ;
				request.setAttribute("deviceTypeName", DeviceTypeShortKeyUtil.getInstance().deviceTypeToCN(asset.getDeviceType(),"/")) ;
				request.setAttribute("baseInfo", category.getAttributesByGroup("base")) ;
			}
			return "page/asset/asset_detail" ;
		}else{
			return "/page/asset/no_asset" ;
		}
	}
	@RequestMapping("moreInfo")
	@ResponseBody
	public Object moreInfo(@RequestParam("ip")String ip,HttpServletRequest request) {
		DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
		Device device = assetFacade.getAssetByIp(ip) ;
		if (device == null) {
			return new Result(false,"资产可能已经被删除！") ;
		}
		Result result = new Result() ;
		AssetCategory category = AssetCategoryUtil.getInstance().getCategoryByPathId(device.getDeviceType()) ;
		List<AssetAttribute> attributes = category.getAttributesByGroup("base") ;
		SimDatasource monitor = monitorService.getFirstByIp(ip) ;
		try {
			if (monitor == null) {
				JSONArray stateJSONArray = FastJsonUtil.toJSONArray(attributes, "label","") ;
				FastJsonUtil.put(stateJSONArray, "value", "") ;
				result.buildSuccess(stateJSONArray) ;
			}else{
				JSONArray stateJSONArray = new JSONArray();
				NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade", request) ;
				Node auditor = nodeMgrFacade.getNodeByNodeId(monitor.getAuditorNodeId()) ;
				List<MonitorState> status = new ArrayList<MonitorState>(attributes.size()) ;
				for(AssetAttribute attr:attributes){
					status.add(new MonitorState(ip, monitor.getSecurityObjectType(), attr.getField())) ;
				}
				List<MonitorState> stateValues = (List<MonitorState>) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_MONITORS, (Serializable)status, 15*60*1000) ;
				for(int i=0;i<attributes.size();i++){
					AssetAttribute att = attributes.get(i) ;
					JSONObject attJSON = new JSONObject() ;
					attJSON.put("label", att.getLabel()) ;
					attJSON.put("value",att.getFormatValue(stateValues.get(i).getData())) ;
					stateJSONArray.add(attJSON) ;
				}
				result.buildSuccess(stateJSONArray) ;
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!") ;
		}
		return result ;
	}
	@RequestMapping("baseInfo")
	@ResponseBody
	public Object baseInfo(@RequestParam("ip")String ip) {
		JSONObject result = new JSONObject();
		DeviceService deviceService = (DeviceService)SpringContextServlet.springCtx.getBean("deviceService") ;
		Device device = deviceService.getDeviceByIp(ip) ;
		AssetObject asset = new AssetObject(device) ;
		AssetCategory assetCategory = AssetCategoryUtil.getInstance().getCategoryByPathId(asset.getDeviceType()) ;
		for(AssetAttribute attr:assetCategory.getAttributes(AssetAttributeType.STATIC)){
			result.put(attr.getId(),attr.getValue(asset)) ;
		}
		return result ;
	}
	
	/**
	 * 资产状态信息(CPU,内存,磁盘,链接数)
	 * @param status
	 * @return
	 */
	@RequestMapping("assetStatus")
	@ResponseBody
	public Object assetStatus(@RequestParam("ip")String ip,@RequestParam("attributeId")String attributeId,HttpServletRequest request) {
		Device device = assetFacade.getAssetByIp(ip) ;
		if (device == null || device.getEnabled() == 0) {
			return new Result(false,"资产不存在或已被禁用！") ;
		}
		AssetCategory category = AssetCategoryUtil.getInstance().getCategoryByPathId(device.getDeviceType()) ;
		AssetAttribute attr = category.getAttribute(attributeId) ;
		DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
		SimDatasource monitor = monitorService.getFirstByIp(ip) ;
		if (monitor == null || attr == null || monitor.getAvailable() == 0) {
			return new Result(false,"监视对象不存在或已被禁用！") ;
		}
		MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), attr.getField()) ;
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		try {
			MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 15*60*1000) ;
			if(ste != null && ste.getData() != null && !isStateExpire(monitor, ste)){
				result.buildSuccess(attr.getValue(ste.getData())) ;
			}else{
				result.build(false,null,Collections.emptyMap());
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!") ;
		}
		return result ;
	}
	
	/**
	 * 根据设备IP获取其所在的业务节点
	 * @return
	 */
	private Node getCollectorNodeByIp(String assetIp){
		NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
		Device device = AssetFacade.getInstance().getAssetByIp(assetIp) ;
		Node managerNode = nodeMgrFacade.getNodeByNodeId(device.getScanNodeId(),false,true,false,false) ;
		Node collectorNode ;
		if(NodeUtil.isAgent(managerNode.getType())){
			collectorNode = managerNode ;
		}else{
			collectorNode = NodeUtil.getChildByType(managerNode, NodeDefinition.NODE_TYPE_COLLECTOR) ;
		}
		return collectorNode ;
	}
	
	/**
	 * 获取日志源流量
	 * @param dataSource
	 * @return
	 */
	@RequestMapping("assetFlow")
	@ResponseBody
	public Object assetFlow(@RequestParam("ip")String ip, HttpServletRequest request) {
		JSONObject result = new JSONObject();
		DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
		List<SimDatasource> dataSourceList = dataSourceService.getByIp(ip) ;
		Node collectorNode = getCollectorNodeByIp(ip) ;
		NodeStatusQueue statusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(collectorNode.getNodeId()) ;
		if(statusQueue == null || statusQueue.size() == 0){
			return Collections.emptyList();
		}
		Map<String,Object> logFlowMap = getFlowMap((NodeStatusMap)statusQueue.lastElement(), dataSourceList) ;
		long rate = (Long) logFlowMap.get("rate") ;
		result.put("flow", rate);
		return result;
	}
	
	/**
	 * 获取日志源流量趋势
	 * @param ip
	 * @param request
	 * @return
	 */
	@RequestMapping("flowTrend")
	@ResponseBody
	public Object flowTrend(@RequestParam("ip")String ip, HttpServletRequest request) {
		DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
		Node collectorNode = getCollectorNodeByIp(ip);
		List<SimDatasource> dataSourceList = dataSourceService.getByIp(ip) ;
		NodeStatusQueue statusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(collectorNode.getNodeId()) ;
		if(statusQueue == null || statusQueue.size() == 0){
			return Collections.emptyList();
		}
		NodeStatusMap nodeStatusMap = null;
		Enumeration<?> enu = statusQueue.getElements();
		List<Map<String,Object>> flowList = new ArrayList<Map<String,Object>>() ;
		while (enu.hasMoreElements()) {
			nodeStatusMap = (NodeStatusMap) enu.nextElement();
			Map<String,Object> flowMap = getFlowMap(nodeStatusMap, dataSourceList) ;
			flowMap.put("time", StringUtil.dateToString((Date) flowMap.get("statusTime"), "HH:mm:ss")) ;
			flowList.add(flowMap) ;
		}
		return flowList;
	}

	/**
	 * 计算一个时间点所有日志源流量
	 * @param nodeStatusMap
	 * @param dataSources
	 * @return
	 */
	private Map<String,Object> getFlowMap(NodeStatusMap nodeStatusMap,List<SimDatasource> dataSources){
		Map<String, Object> flowMap = new HashMap<String, Object>();
		flowMap.put("statusTime", nodeStatusMap.getStatusTime()) ;
		long rate = 0 ;
		for(SimDatasource dataSource:dataSources){
			Long dataSourceId = dataSource.getResourceId() ;
			Long componentResourceId = dataSource.getComponentId() ;
			//首先从缓存中获取日志源的收集组件信息
			String componentId = dataSourceComponentCache.get(dataSourceId);
			NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
			if(componentId == null){
				Component cmp = nodeMgrFacade.getComponentWithSegments(componentResourceId) ;
				if(cmp == null){
					continue;
				}
				dataSourceComponentCache.put(dataSourceId, (componentId = cmp.getComponentId())) ;
			}
			ComponentStatusMap csm ;
			if(nodeStatusMap == null || (csm = nodeStatusMap.getComponentStatusMap(componentId)) == null){
				continue;
			}
			NamespaceStatusMap dataSourceCounter = (NamespaceStatusMap) csm.getStatusMap().get(StatusDefinition.DATASOURCE_COUNTER) ;
			if(dataSourceCounter == null){
				continue ;
			}
			NamespaceStatusMap dataSourceStatus = (NamespaceStatusMap) dataSourceCounter.get(String.valueOf(dataSourceId)) ;
			Long dataSourceRate = null ;
			if(dataSourceStatus != null){
				dataSourceRate = (Long) dataSourceStatus.get(StatusDefinition.EVENT_RATE) ;
			}
			if (dataSourceRate != null) {
				dataSourceRate = dataSourceRate.longValue();
				rate += dataSourceRate ;
			}
		}
		flowMap.put("rate", rate) ;
		return flowMap ;
	}
	

	@RequestMapping("assetAttrGroupStatus")
	@ResponseBody
	public Object assetAttrGroupStatus(@RequestParam("ip")String ip,@RequestParam("group")String group,HttpServletRequest request) {
		JSONArray result = new JSONArray() ;
		Device device = assetFacade.getAssetByIp(ip) ;
		if (device == null || device.getEnabled() == 0) {
			return result ;
		}
		AssetCategory category = AssetCategoryUtil.getInstance().getCategoryByPathId(device.getDeviceType()) ;
		List<AssetAttribute> attrs = category.getAttributesByGroup(group) ;
		DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
		SimDatasource monitor = monitorService.getFirstByIp(ip) ;
		if (monitor == null || attrs == null || monitor.getAvailable() == 0) {
			return result;
		}
		ArrayList<MonitorState> status = new ArrayList<MonitorState>() ;
		for(AssetAttribute att:attrs){
			status.add(new MonitorState(att.getId(),ip, monitor.getSecurityObjectType(), att.getField())) ;
		}
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		try {
			List<MonitorState> ste = (List<MonitorState>) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_MONITORS, status, 15*60*1000) ;
			if(ObjectUtils.isNotEmpty(ste)){
				for(AssetAttribute att:attrs){
					for(MonitorState ms:ste){
						if(ms != null && !att.getId().equals(ms.getId())){
							continue ;
						}
						if(!isStateExpire(monitor, ms)){
							JSONObject stateJSON = new JSONObject() ;
							stateJSON.put("name",ms.getStateName()) ;
							stateJSON.put("label",att.getLabel()) ;
							stateJSON.put("value", att.getValue(ms.getData())) ;
							result.add(stateJSON) ;
						}
						break ;
					}
				}
			}
		} catch (CommunicationException e) {
			logger.warn("获取属性状态信息失败，命令执行超时！") ;
		}
		return result ;
	}
	
	/**
	 * 判断状态是否有效<br>
	 * 此方法根据状态信息的获取时间与当前时间做比较<br>
	 * 如果当前时间减去状态时间大于3倍的监视对象轮询时间间隔，则认为状态信息无效<br>
	 * 例如：当前时间为2014-01-01 00:03:00,状态时间为2014-01-01 00:00:00<br>
	 * 监视对象轮询时间为1分钟，则此状态信息无效(说明状态信息已经过时)
	 * @param monitor
	 * @param state
	 * @return
	 */
	private static boolean isStateExpire(SimDatasource monitor,MonitorState state){
		if(state == null || state.getStateTime() == null){
			return false ;
		}
		String timerType = monitor.getTimerType() ;
		boolean expire = false;
		Date stateTime = state.getStateTime() ;
		if(TimeExpression.TYPE_INTERVAL_MINUTE.equals(timerType)){
			TimeExpression te = new TimeExpression(monitor.getTimerType(), monitor.getTimer()) ;
			Date now = new Date() ;
			int intervalMills = 1000*60*te.getInterval() ;
			//如果状态时间与当前时间相差超过3倍的监视对象时间，则认为状态时间无效
			if((now.getTime() - stateTime.getTime()) > 3*intervalMills){
				return true ;
			}
			return false ;
		}else if(TimeExpression.TYPE_INTERVAL_HOUR.equals(timerType)){
			TimeExpression te = new TimeExpression(monitor.getTimerType(), monitor.getTimer()) ;
			Date now = new Date() ;
			int intervalMills = 1000*60*60*te.getInterval() ;
			//如果状态时间与当前时间相差超过3倍的监视对象时间，则认为状态时间无效
			if((now.getTime() - stateTime.getTime()) > 3*intervalMills){
				return true ;
			}
			return false ;
		}
		return expire ;
	}
	/**
	 * 状态列表
	 * @return
	 */
	@RequestMapping("stateList")
	@ResponseBody
	public Object stateList(@RequestParam("ip")String ip,
			                @RequestParam("stateName")String stateName,
			                @RequestParam(value="count",defaultValue="60")int count,
			                HttpServletRequest request) {
		//此方法目前不在使用
		return null ;
	}
	
	@RequestMapping("monitor")
	@ResponseBody
	public Object monitor() {
		return null ;
	}
	@RequestMapping("showLog")
	public Object showLog(@RequestParam("ip")String ip,HttpServletRequest request) {
		DataSourceService dataSoruceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request) ;
		SimDatasource dataSource = dataSoruceService.getFirstByIp(ip, DataSourceStatus.ENABLE) ;
		if (dataSource != null) {
			String deviceType = ObjectUtils.nullChoice(dataSource.getSecurityObjectType(),"",dataSource.getSecurityObjectType()) ;
			String startDate = StringUtil.nvl(request.getParameter("startDate"),StringUtil.yesterday("yyyy-MM-dd HH:mm:ss")) ;
			String endDate = StringUtil.nvl(request.getParameter("endDate"),StringUtil.today("yyyy-MM-dd HH:mm:ss")) ;
			request.setAttribute("nodeId", dataSource.getAuditorNodeId()) ;
			request.setAttribute("deviceType",deviceType) ;
			request.setAttribute("startDate",startDate) ;
			request.setAttribute("endDate", endDate) ;
			return "/page/asset/asset_detail_log" ;
		}else{
			return "/page/asset/asset_detail_nolog" ;
		}
	}
	
	@RequestMapping(value="event")
	@ResponseBody
	public Object event(final SID sid,
						@RequestParam("ip")String ip,
						@RequestParam(value="page",defaultValue="1")Integer pageIndex,
						@RequestParam(value="rows",defaultValue="15")Integer pageSize,
						HttpServletRequest request) {
		String date = request.getParameter("date") ;
		if(StringUtil.isBlank(date)){
			return FastJsonUtil.EMPTY_JSON;
		}	
		EventService eventService = (EventService) SpringWebUtil.getBean("eventService",request) ;
		Date beginTime = null;
		Date endTime = null; 
		if(date.equals("today")){//今天
			Date today = new Date() ;
			beginTime = ObjectUtils.dayBegin(today) ;
			endTime = ObjectUtils.dayEnd(today) ;
		}else if(date.equals("week")){//最近一周(7天)
			endTime = new Date() ;
			beginTime = ObjectUtils.addDays(endTime,-7) ;
		}
		PageBean<EventModel> page = eventService.getEventByIp(pageIndex, pageSize, ip,beginTime,endTime) ;
		JSONObject pageJSON = new JSONObject();
		pageJSON.put("total", page.getTotal()) ;
		JSONArray rows = FastJsonUtil.toJSONArray(page.getData(),
			"$d2s:createTime","$htmlEscape:description=DESCR","eventId=EVENT_ID","name=NAME","priority",
			"dvcAddress=DVC_ADDRESS","srcAddress=SRC_ADDRESS","destAddress=DEST_ADDRESS","uuid","level","cat1","cat2");
		FastJsonUtil.put(rows, "isOperator", sid.hasOperatorRole()) ;
		pageJSON.put("rows",rows) ;
		return pageJSON ;
	}
	
	@RequestMapping("alarm")
	@ResponseBody
	public Object alarm(@RequestParam("ip")String ip,
						@RequestParam(value="page",defaultValue="1")int pageIndex,
						@RequestParam(value="rows",defaultValue="15")int pageSize,
						HttpServletRequest request) {
		AlarmService service = (AlarmService) SpringWebUtil.getBean("alarmService", request) ;
		String timeType = request.getParameter("date") ;
		Map<String,Object> params = new HashMap<String, Object>() ;
		if (StringUtil.isNotBlank(timeType)) {
			if(timeType.equals("today")){
				Date today = new Date() ;
				params.put("startTime", ObjectUtils.dayBegin(today)) ;
				params.put("endTime", ObjectUtils.dayEnd(today)) ;
			}
		}
		PageBean<SimAlarm> page = service.getPageByIp(pageIndex, pageSize, ip,params) ;
		JSONObject result = new JSONObject() ;
		result.put("total", page.getTotal()) ;
		result.put("rows", page.getData()) ;
		return result ;
	}
	
	@RequestMapping("report")
	public String report(@RequestParam("ip")String ip,@RequestParam("tabSeq")String tabSeq,HttpServletRequest request) {
	   ReportService rptService = (ReportService) SpringWebUtil.getBean("reportService", request);
		// 日志总数
		List<Map> log = rptService.getLogCount(ip);
		String [] lcategory = new String[log.size()];
		List<Map<String,Object>> seriesData = new ArrayList<Map<String,Object>>();
		int len = Math.min(log.size(),10) ;
		for(int i=0;i<len;i++){
			Map<String,Object> map = log.get(i);
			String type = ReportUiUtil.getDeviceTypeName(StringUtil.toString(map.get("type")),request.getLocale());
			lcategory[i] = type;
			Map<String,Object> serieItem = new HashMap<String,Object>();
			serieItem.put("color", HChart.colors[i]) ;
			serieItem.put("y", StringUtil.toInt(StringUtil.toString(map.get("counts"),"0")));
			seriesData.add(serieItem);
		}
		
		JSONObject logJSON = new JSONObject();
		logJSON.put("categories", lcategory);
		logJSON.put("series", seriesData);
		EventService  eventService = (EventService) SpringWebUtil.getBean("eventService", request);
		Condition params=new Condition();
		params.setDvc_address(ip);
		Map obj = (Map) eventService.getEventAlarmCount(params);
		List event = eventService.getEventCount(params);
		Object[][] eventCount = createPie(event);
		//JSONObject alarmJSON = new JSONObject();
		//alarmJSON.put("data", alarmCount);
		request.setAttribute("tabSeq", tabSeq);
		request.setAttribute("log",logJSON);
		request.setAttribute("eventStat", obj);
		request.setAttribute("eventPriorityStat", JSON.toJSON(eventCount));
		//request.setAttribute("alarm", alarmJSON.toJSONString());
		
		return "/page/asset/asset_detail_report" ;
	}
	
	/**
	 * 端口、服务、版本查询
	 * @param ip
	 * @param tabSeq
	 * @param request
	 * @return
	 */
	@RequestMapping("port")
	@ResponseBody
	public Object port(@RequestParam("ip")String ip,@RequestParam("tabSeq")String tabSeq,HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		Map<String,Object> root = null;
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
			SimDatasource monitor = monitorService.getFirstByIp(ip) ; 
			if (monitor != null && monitor.getAvailable() != 0) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "service") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 60*1000) ;
				Map<String,Object> data = (Map<String, Object>) ste.getData() ;
				if(data != null){
					List<Map<String,Object>> services = (List<Map<String, Object>>) data.get("DEVICE_SERVICES") ;
					if (services != null) {
						root = createTreeNode(ip, null);
						for(Map<String,Object> entry:services){
							Map<String,Object> portItem = createTreeNode(String.valueOf(entry.get("DEST_PORT")), root);
							Map<String,Object> serviceItem = createTreeNode((String)entry.get("DEST_SERVICE_NAME"), portItem) ;
							Map<String,Object> appItem = createTreeNode((String)entry.get("SYS_CAPTION"), serviceItem) ;
							String[] cpeList = StringUtil.split((String)entry.get("CPE")) ;
							for(String cpe:cpeList){
								Map<String,Object> cpeItem;
								if (appItem.size() > 0) {
									cpeItem = createTreeNode(cpe, appItem) ;
								} else {
									cpeItem = createTreeNode(cpe, serviceItem) ;
								}
							}
							
						}
						request.setAttribute("services",services) ;
					}
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!") ;
		}
		//return "/page/asset/asset_detail_port" ;
		return root;
	}
	
	/**
	 * 构造echarts树形数据结构
	 * @param name
	 * @param parent
	 * @return
	 */
	private Map<String,Object> createTreeNode(String name,Map<String,Object> parent){
		Map<String,Object> node = new HashMap<String, Object>(2) ;
		if (name != null) {
			node.put("name", name) ;
			node.put("children", new ArrayList<Map<String,Object>>()) ;
			if(parent != null){
				((List<Map<String,Object>>)parent.get("children")).add(node) ;
			}
		}
		return node ;
	}
	
	/**
	 * 进程查询
	 * @param ip
	 * @param tabSeq
	 * @param request
	 * @return
	 */
	@RequestMapping("process")
	public String process(@RequestParam("ip")String ip,@RequestParam("tabSeq")String tabSeq,HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
			SimDatasource monitor = monitorService.getFirstByIp(ip) ; 
			if (monitor != null && monitor.getAvailable() != 0) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "process") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 15*60*1000) ;
				Map<String,Object> data = (Map<String, Object>) ste.getData() ;
				if(data != null){
					Object process = data.get("DVC_COMMONINF") ;
					request.setAttribute("processList", process) ;
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!") ;
		}
		return "/page/asset/asset_detail_process" ;
	}
	
	/**
	 * 服务查询
	 * @param ip
	 * @param tabSeq
	 * @param request
	 * @return
	 */
	@RequestMapping("win32Service")
	public String win32Service(@RequestParam("ip")String ip,@RequestParam("tabSeq")String tabSeq,HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
			SimDatasource monitor = monitorService.getFirstByIp(ip) ; 
			if (monitor != null && monitor.getAvailable() != 0) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "Win32Service") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 15*60*1000) ;
				Map<String,Object> data = (Map<String, Object>) ste.getData() ;
				if(data != null){
					Object win32Service = data.get("DVC_COMMONINF") ;
					request.setAttribute("win32ServiceList", win32Service) ;
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!") ;
		}
		return "/page/asset/asset_detail_win32Service" ;
	}
	
	/**
	 * 网卡查询
	 * @param ip
	 * @param tabSeq
	 * @param request
	 * @return
	 */
	@RequestMapping("networkCard")
	public String networkCard(@RequestParam("ip")String ip,@RequestParam("tabSeq")String tabSeq,HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
			SimDatasource monitor = monitorService.getFirstByIp(ip) ; 
			if (monitor != null && monitor.getAvailable() != 0) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "NetworkAdapterConfiguration") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 15*60*1000) ;
				Map<String,Object> data = (Map<String, Object>) ste.getData() ;
				if(data != null){
					Object networkCard = data.get("DVC_COMMONINF") ;
					//如果flag为true，证明资产是windows系统
					boolean flag = monitor.getSecurityObjectType().equals("MonitorOS/Microsoft/WindowsStatus");
					request.setAttribute("networkCardList", networkCard) ;
					request.setAttribute("isWindows", flag) ;
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!") ;
		}
		return "/page/asset/asset_detail_networkCard" ;
	}
	
	/**
	 * 获取监视对象信息,调用报表模块方法，查询cpu,内存数据
	 * @param ip
	 * @param request
	 * @return
	 */
	
	@RequestMapping("getCpuAndMemoryTrend")
	@ResponseBody
	public Object getCpuAndMemoryTrend(@RequestParam("ip")String ip, HttpServletRequest request) {
		DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
		SimDatasource monitor = monitorService.getFirstByIp(ip) ;
		String sql = "SELECT * FROM CPU_MEM_USED_PERCENT_BASE " +
				     "WHERE RESOURCE_ID=? AND START_TIME >= ? AND START_TIME<= ? " +
				     "ORDER BY START_TIME ASC";
		List<?> cpu_mem_List = null;
		if (monitor != null && monitor.getAvailable() != 0) {
			Date now = new Date();
			//查询某一段时间内cpu、内存数据
			cpu_mem_List = reportQuery.findBySQL(sql, monitor.getResourceId(), ObjectUtils.addHours(now, -1), now);
		}
		return cpu_mem_List;
	}
	
	private Object [][] createPie(List<Map> list){
		Object[][] data = new Object [list.size()][2];
		for(int i=0,len=list.size();i<len;i++){
			Map map = list.get(i);
			data[i] = new Object[]{CommonUtils.getLevel(map.get("priority")),map.get("counts")};
		}
		return data;
	}
}
