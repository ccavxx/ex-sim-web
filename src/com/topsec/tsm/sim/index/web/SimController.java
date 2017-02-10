package com.topsec.tsm.sim.index.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.AssetState;
import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.status.ComponentStatusMap;
import com.topsec.tsm.node.status.NodeStatusMap;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.StatusDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupByAssetCategory;
import com.topsec.tsm.sim.asset.service.TopoService;
import com.topsec.tsm.sim.auth.util.LoginUserCache;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.service.EventService;
import com.topsec.tsm.sim.newreport.model.ReportQuery;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueue;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DateUtils;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.NodeUtil;

@Controller
@RequestMapping("/index/*")
public class SimController {

	private NodeMgrFacade nodeMgrFacade;
	@Autowired
	private ReportQuery reportQuery;
	private String kernelAuditorNodeId ;
	private String kernelCollectorId ;
	
	@RequestMapping("/")
	public String index(){
		return "/page/index" ;
	}
	
	@RequestMapping("serverRealTimeData")
	@ResponseBody
	public Object getServerRealTimeData(HttpServletRequest request){
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			String auditorNodeId = kernelAuditorNodeId ;
			String collectorNodeId = kernelCollectorId;
			if (StringUtil.isBlank(collectorNodeId)) {
				Node auditorNode = nodeMgrFacade.getKernelAuditor(false) ;
				List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_COLLECTOR, false, false, false, false);
				if(ObjectUtils.isEmpty(nodes)){//节点还没有注册完成
					return result ;
				}
				Node collectNode = nodes.get(0) ; 
				collectorNodeId = kernelCollectorId = collectNode.getNodeId();
				auditorNodeId = kernelAuditorNodeId = auditorNode.getNodeId() ;
			}
			if(NodeStatusQueueCache.offline(auditorNodeId) || 
			   NodeStatusQueueCache.offline(collectorNodeId)){
				return result ;
			}
			NodeStatusQueue collectNodeStatusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(collectorNodeId);
			NodeStatusQueue auditorNodeStatusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(auditorNodeId);
			if (collectNodeStatusQueue == null || auditorNodeStatusQueue == null) {
				return result;
			}
			NodeStatusMap collectorNodeStatusMap = (NodeStatusMap) collectNodeStatusQueue.lastElement();
			NodeStatusMap auditorNodeStatusMap =  (NodeStatusMap) auditorNodeStatusQueue.lastElement(); 
			result.put("cpu_usage", auditorNodeStatusMap.get(StatusDefinition.CPU_USAGE));
			result.put("mem_usage", auditorNodeStatusMap.get(StatusDefinition.MEM_USAGE));
			result.put("mem_total", auditorNodeStatusMap.get(StatusDefinition.TOTAL_MEM));
			result.put("storage_usage", auditorNodeStatusMap.get(StatusDefinition.STORAGE_USAGE)) ;
			result.put("storage_avaliable", auditorNodeStatusMap.get(StatusDefinition.STORAGE_AVALIABLE)) ;
			result.put("storage_total", auditorNodeStatusMap.get(StatusDefinition.STORAGE_TOTAL)) ;
			result.put("log_flow", getLogFlowData(auditorNodeStatusMap,collectorNodeStatusMap));
			result.put("server_time", DateUtils.currentDatetime());
			
			/*
			此代码实现实时消息功能，可能通过向result中增加realTimeEvents，向前台弹出提示信息 
			List<Map<String, Object>> realTimeEvents = new ArrayList<Map<String, Object>>();
			Map<String, Object> realTimeEvent = new HashMap<String, Object>();
			realTimeEvent.put("eventName", "磁盘存储超出上限");
			realTimeEvents.add(realTimeEvent);
			
			Map<String, Object> realTimeEvent2 = new HashMap<String, Object>();
			realTimeEvent2.put("eventName", "CPU超出上限");
			realTimeEvents.add(realTimeEvent2);
			
			result.put("realTimeEvents", realTimeEvents);
			*/
			Date now = new Date();
			Date  dayBegin = ObjectUtils.dayBegin(now) ;
			Condition con = new Condition() ;
			con.setStart_time(StringUtil.longDateString(dayBegin)) ;
			con.setEnd_time(StringUtil.longDateString(now)) ;
			con.setConfirm("0");//0：未确认事件
			EventService  eventService = (EventService) SpringWebUtil.getBean("eventService", request);
			Map<String,Object> eventAndAlarmCount = (Map<String, Object>) eventService.getEventAlarmCount(con) ;
			result.put("eventCount",eventAndAlarmCount.get("event")) ;
		} catch (Exception ex) {
			ex.printStackTrace() ;
		}
		return result;
	}
	/**
	 * 资产状态列表
	 * @return
	 */
	@RequestMapping(value="assetList")
	@ResponseBody
	public Object getAssetList(SID sid){
		JSONObject result = new JSONObject() ;
		List<AssetGroup> assetGroups;
		JSONArray groupsJson;
		try {
			SID.setCurrentUser(sid) ;
			assetGroups = AssetFacade.getInstance().groupBy(new GroupByAssetCategory());
			groupsJson = new JSONArray(assetGroups.size());
			int onlineCount = 0 ;
			int offlineCount = 0 ;
			int eventCount = 0 ;
			for (AssetGroup assetGroup : assetGroups) {
				onlineCount += assetGroup.getOnlineCount() ;
				offlineCount += assetGroup.getOfflineCount() ;
				eventCount += assetGroup.getEventCount() ;
				groupsJson.add(createAssetGroupJson(assetGroup)) ;
			}
			result.put("total", assetGroups.size()) ;
			result.put("rows", groupsJson) ;
			JSONObject totalData = new JSONObject();
			totalData.put("name", "总计") ;
			totalData.put("iconCls", "icon-none") ;
			//totalData.put("alarmCount", alarmCount) ;
			totalData.put("eventCount", eventCount) ;
			totalData.put("onlineCount", onlineCount) ;
			totalData.put("offlineCount", offlineCount) ;
			result.put("footer", FastJsonUtil.wrapper(totalData)) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}finally{
			SID.removeCurrentUser() ;
		}
		return result ;
	}
	@RequestMapping("logFlowList")
	@ResponseBody
	public Object logFlowList(@RequestParam(value="limit",defaultValue="40")int limit,HttpServletRequest request) {
		JSONArray result = new JSONArray() ;
		String auditorNodeId = kernelAuditorNodeId ;
		String collectorNodeId = kernelCollectorId;
		if (StringUtil.isBlank(collectorNodeId)) {
			Node auditorNode = nodeMgrFacade.getKernelAuditor(false) ;
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_COLLECTOR, false, false, false, false);
			if(ObjectUtils.isEmpty(nodes)){
				return result ;
			}
			Node collectNode = nodes.get(0) ; 
			collectorNodeId = kernelCollectorId = collectNode.getNodeId();
			auditorNodeId = kernelAuditorNodeId = auditorNode.getNodeId() ;
		}
		if(NodeStatusQueueCache.getInstance().isNodeOffline(auditorNodeId) || 
		   NodeStatusQueueCache.getInstance().isNodeOffline(collectorNodeId)){
			return result ;
		}
		NodeStatusQueue collectNodeStatusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(collectorNodeId);
		NodeStatusQueue auditorNodeStatusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(auditorNodeId);
		if (collectNodeStatusQueue == null || auditorNodeStatusQueue == null) {
			return result;
		}
		int size = Math.min(auditorNodeStatusQueue.size(),collectNodeStatusQueue.size())  ;
		if(size == 0){
			return result ;
		}
		int index = size > limit ? size-limit : 0 ;
		for(;index < size;index++){
			Object auditorStatus = auditorNodeStatusQueue.get(index) ;
			Object collectorStatus = collectNodeStatusQueue.get(index) ;
			if(auditorStatus instanceof NodeStatusMap && collectorStatus instanceof NodeStatusMap){
				result.add(getLogFlowData((NodeStatusMap)auditorStatus,(NodeStatusMap)collectorStatus)) ;
			}
		}
		return result ;
	}
	/**
	 * 获得当前在线用户列表
	 */
	@RequestMapping("onlineUserList")
	@ResponseBody
	public Object onlineUserList(HttpServletRequest request) {
		JSONArray result = new JSONArray() ;
		List<SID> sidList = LoginUserCache.getInstance().getOnlineUsers();
		result = FastJsonUtil.toJSONArray(sidList, "loginIP", "userName");
		return result ;
	}
	/**
	 * 保活信号
	 * @return
	 */
	@RequestMapping("keepAlive")
	@ResponseBody
	public Object keepAlive() {
		return new JSONObject(0) ;
	}
	
	@RequestMapping("getSystemInfo")
	@ResponseBody
	public Object getSystemInfo(SID sid,@RequestParam("clientPublicKey")String clientPublicKey) {
		JSONObject systemInfo = new JSONObject() ;
		systemInfo.put("isWindows", SystemUtils.IS_OS_WINDOWS) ;
		systemInfo.put("fileSeparator", File.separator) ;
		sid.setClientPublicKey(clientPublicKey) ;
		return systemInfo ;
	}
	
	private JSONObject createAssetGroupJson(AssetGroup group){
		JSONObject groupJson = new JSONObject() ;
		groupJson.put("id", group.getId()) ;
		groupJson.put("name", group.getName()) ;
		groupJson.put("state", "closed") ;
		groupJson.put("iconCls", AssetUtil.getBigIconClsByDeviceType(group.getId())) ;
		groupJson.put("alarmCount",group.getAlarmCount()) ;
		groupJson.put("onlineCount",group.getOnlineCount()) ;
		groupJson.put("offlineCount",group.getOfflineCount()) ;
		groupJson.put("eventCount", group.getEventCount()) ;
		groupJson.put("isdevice", false) ;
		groupJson.put("children", createGroupAssetsJson(group.getAssets())) ;
		return groupJson ;
	}
	
	private JSONArray createGroupAssetsJson(List<AssetObject> assets) {
		JSONArray groupAssetsJson = new JSONArray(assets.size()) ;
		for(AssetObject asset:assets){
			groupAssetsJson.add(createAssetJson(asset)) ;
		}
		return groupAssetsJson;
	}

	private JSONObject createAssetJson(AssetObject asset) {
		SID sid = SID.currentUser() ;
		JSONObject assetJson = new JSONObject() ;
		assetJson.put("id", asset.getIp().toString()) ;
		assetJson.put("name", asset.getName()) ;
		assetJson.put("ip", asset.getMasterIp().toString()) ;
		assetJson.put("alarmCount", asset.getAlarmCount()) ;
		assetJson.put("eventCount", asset.getEventCount()) ;
		assetJson.put("iconCls",AssetUtil.getIconClsByState(asset.getState())) ;
		assetJson.put("online", asset.getState()==AssetState.ONLINE) ;
		assetJson.put("isdevice", true) ;
		assetJson.put("enabled", asset.getEnabled()) ;
		assetJson.put("hasOperatorRole", sid != null && sid.hasOperatorRole()) ;
		return assetJson;
	}
	@RequestMapping("assetTopoTabs")
	public String assetTopoTabs(SID sid,HttpServletRequest request) {
		TopoService topoService = (TopoService) SpringWebUtil.getBean("topoService", request) ;
		List<AssTopo> topoList = topoService.getUserTopoList(sid.isOperator() ? null : sid.getUserName()) ;
		request.setAttribute("topoList", topoList) ;
		return "page/main/topo_list" ;
	}
	/**
	 * Collector流量总数
	 * @param nodeStatusMap
	 * @return
	 */
	private static long getCollectorLogFlowData(NodeStatusMap nodeStatusMap){
		long flowCount=getComponentRate(nodeStatusMap,"SyslogCollector");
		flowCount+=getComponentRate(nodeStatusMap,"NetFlowCollector");
		flowCount+=getComponentRate(nodeStatusMap,"SnmpTrapCollector");
		flowCount+=getComponentRate(nodeStatusMap,"SchedularCollector");
		flowCount+=getComponentRate(nodeStatusMap,"AuditJmsCollector");
		return flowCount ;
	}
	private static long getLogFlowData(NodeStatusMap auditorStatus,NodeStatusMap collectorStatus){
		return getCollectorLogFlowData(collectorStatus) + getAuditorLogFlowData(auditorStatus) ;
	}
	/**
	 * 服务器流量总数
	 * @param nodeStatusMap
	 * @return
	 */
	private static long getAuditorLogFlowData(NodeStatusMap nodeStatusMap){
		long flowCount = 0 ;
		flowCount+=getComponentRate(nodeStatusMap,"AgentJmsCollector");
		return flowCount ;
	}
	private static long getComponentRate(NodeStatusMap nodeStatusMap,String componentId){
		ComponentStatusMap csm ;
		if(nodeStatusMap == null || (csm = nodeStatusMap.getComponentStatusMap(componentId)) == null){
			return 0 ;
		}
		Long rate =  (Long)csm.get(StatusDefinition.EVENT_RATE) ;	
		return rate == null ? 0 : rate.longValue() ;
	}
	@Autowired
	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}
	
	@RequestMapping("/getMapData")
	@ResponseBody
	public Object getMapData(@RequestParam("mapType")String mapType, @RequestParam("parentName")String parentName){
		JSONObject mapData = new JSONObject();
		try {
			Node node = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_REPORTSERVICE, false, false, false, false).get(0);
			String[] routes = NodeUtil.getRoute(node) ;
			HashMap<String,Object> params = new HashMap<String,Object>(3) ;
			params.put("mapType", mapType) ;
			params.put("parentName", parentName) ;
			Map data = (Map)NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_REPORT_GET_MAPDATA, params, 5000);
			List<HashMap<String, Object>> srcData = (List<HashMap<String, Object>>)data.get("srcData");
			List<HashMap<String, Object>> destData = (List<HashMap<String, Object>>)data.get("destData");
			if("department".equals(mapType)){
				Map<String, List<JSONArray>> lanData = new HashMap<String, List<JSONArray>>();
				List<HashMap<String, Object>> result = "src".equals(parentName) ? srcData : destData;
				if(result != null && result.size() > 0){
					for (HashMap<String, Object> map : result) {
						JSONArray temp = new JSONArray();
						String name = map.get("department").toString();
						temp.add(0, (Date)map.get("START_TIME"));
						temp.add(1, Integer.valueOf(map.get("OPCOUNT").toString()));
						temp.add(2, name);
						if(!lanData.containsKey(name)){
							lanData.put(name, new ArrayList<JSONArray>());
						}
						lanData.get(name).add(temp);
					}
					mapData.put("srcData",lanData);
					mapData.put("success", true);
				} else {
					mapData.put("success", false);
				}
			} else {
				mapData.put("srcData", FastJsonUtil.toJSONArray(srcData, mapType+"=name","OPCOUNT=value"));
				mapData.put("destData", FastJsonUtil.toJSONArray(destData, mapType+"=name","OPCOUNT=value"));
				mapData.put("success", true);
			}
		}catch (CommunicationException e) {
			mapData.put("success", false);
			mapData.put("msg", "指定节点可能掉线！");
		}catch (Exception e) {
			mapData.put("success", false);
			mapData.put("msg", "系统异常，请与管理员联系！");
		}
		return mapData ;
	}
	
	@RequestMapping("/getForceData")
	@ResponseBody
	public Map<String, Object> getForceData(@RequestParam(value="scope",defaultValue="500")Integer top){
		Map<String,Object> result ;
		try {
			if(top > 2000){
				top = 2000 ;
			}
			String sql = "SELECT SRC_ADDRESS,DEST_ADDRESS,SUM(OPCOUNT) OPCOUNT " +
					     "FROM SRC_DEST_DAY " +
					     "WHERE START_TIME >= CURRENT_DATE AND START_TIME < ADDDATE(CURRENT_DATE,INTERVAL 1 DAY) " +
					     "GROUP BY SRC_ADDRESS,DEST_ADDRESS " +
					     "ORDER BY OPCOUNT DESC " +
					     "LIMIT ?";
			List<Map<String,Object>> srcDestList = reportQuery.findBySQL(sql,top);
			result = CommonUtils.buildRelationTree("SRC_ADDRESS", "DEST_ADDRESS", "", "OPCOUNT", srcDestList);
			result.put("count", srcDestList.size());
		} catch (Exception e) {
			e.printStackTrace() ;
			result = Collections.emptyMap();
		}
		return result;
	}
}

