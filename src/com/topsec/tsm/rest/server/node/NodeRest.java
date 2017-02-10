package com.topsec.tsm.rest.server.node;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.node.status.ComponentStatusMap;
import com.topsec.tsm.node.status.NodeStatusMap;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.StatusDefinition;
import com.topsec.tsm.rest.server.common.RestSecurityAuth;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.service.EventService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueue;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.FacadeUtil;
import com.topsec.tsm.sim.util.NodeUtil;
@Path("/")
public class NodeRest {
	
	protected static Logger log = LoggerFactory.getLogger(NodeRest.class);
	
	NodeMgrFacade nodeMgrFacade;
	private AssetFacade assetFacade = AssetFacade.getInstance() ;
	/**
	 * 下级节点注册
	 * @param request
	 * @param id 服务端sessionid
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/node/register")
	public Response register(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build = Response.status(200);
		String msg = null;
		try {
			build.header("Content-Type","text/xml;charset=UTF-8");
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				msg = RestUtil.fail("访问 任务启动  接口前未登陆！","");
				Log.info(msg) ;
				return build.entity(msg).build();
			}
			if(nodeMgrFacade == null)
				nodeMgrFacade = getNodeMgrFacade(request);
			Node parentNode = nodeMgrFacade.getKernelAuditor(false);
			if(parentNode == null){
				msg = RestUtil.fail("父级节点为空！", "");
				Log.info(msg) ;
				return build.entity(msg).build();
			}
			String content = RestUtil.getStrFromInputStream(request.getInputStream());
			Document document = DocumentHelper.parseText(content);
			Element root = document.getRootElement();
			String type = root.element("Type").getTextTrim();
			//根据Type 判断是删除节点还是注册节点
			if("register".equals(type)){
				String nodeId = root.element("NodeId").getTextTrim() ;
				Node dbNode = nodeMgrFacade.getNodeByNodeId(nodeId) ;
				if(dbNode != null){
					msg = RestUtil.fail("节点重复注册！","");
					Log.info(msg) ;
					return build.entity(msg).build() ;
				}
				String ip = root.element("Ip").getTextTrim();
				String alias = root.element("Alias").getTextTrim();
				Node node = new Node();
				node.setIp(ip);
				node.setType(NodeDefinition.NODE_TYPE_CHILD);
				node.setNodeId(nodeId);
				node.setAlias(alias);
				node.setResourceName(alias);
				node.setRouteUrl(parentNode.getRouteUrl());
				node.setParent(parentNode);
				String[] route = NodeUtil.getRoute(node);
				nodeMgrFacade.registerNode(node,route);
			}else if("delete".equals(type)){
				String ip = root.element("Ip").getTextTrim();
				Node node = nodeMgrFacade.getChildByIp(ip);
				if(node != null)
					nodeMgrFacade.delNode(node);
			}
			msg = RestUtil.success("success");
		} catch (Exception e) {
			e.printStackTrace();
			msg = RestUtil.fail("注册失败！", "");
		}
		return build.entity(msg).build();
	}
	
	/**
	 * 下级节点注册
	 * @param request
	 * @param id 服务端sessionid
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/node/deleteChildNode")
	public Response deleteChildNode(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build = Response.status(200);
		String msg = null;
		try {
			build.header("Content-Type","text/xml;charset=UTF-8");
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				msg = RestUtil.fail("访问 任务启动  接口前未登陆！","");
				Log.info(msg) ;
				return build.entity(msg).build();
			}
			
			String content = RestUtil.getStrFromInputStream(request.getInputStream());
			Document document = DocumentHelper.parseText(content);
			Element root = document.getRootElement();
			String type = root.element("Type").getTextTrim();
			String ip = root.element("Ip").getTextTrim();
			//根据Type 判断是删除节点还是注册节点
			if("delete".equals(type)){
				if(nodeMgrFacade == null)
					nodeMgrFacade = getNodeMgrFacade(request);
				Node node = nodeMgrFacade.getParentNode();
				if(node.getIp().equals(ip)) {
					//删除原有的上级节点
					nodeMgrFacade.delNode(node);
					log.info("删除原有的上级节点成功，上级节点IP：" + ip);
				}
			}
			msg = RestUtil.success("success");
		} catch (Exception e) {
			e.printStackTrace();
			msg = RestUtil.fail("删除子节点出错！", "");
		}
		return build.entity(msg).build();
	}
	
	/**
	 * 获取本机状态信息
	 * @param request
	 * @param id
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/node/systemInfo")
	public Response systemInfo(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		try {
			ResponseBuilder build=Response.status(200);
			build.header("Content-Type","text/xml;charset=UTF-8");
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
	           return build.entity(msg).build();
			}
			if(nodeMgrFacade == null)
				nodeMgrFacade = getNodeMgrFacade(request);
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_COLLECTOR, false, false, false, false);
			NodeStatusQueue nodeStatusQueue =null;
			if(!ObjectUtils.isEmpty(nodes)){//节点还没有注册完成
				Node collectNode = nodes.get(0) ; 
				nodeStatusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(collectNode.getNodeId());
			}
			Node auditorNode = nodeMgrFacade.getKernelAuditor(false);
			NodeStatusQueue auditorNodeStatusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(auditorNode.getNodeId());
			if (nodeStatusQueue == null || auditorNodeStatusQueue == null) {
				return null;
			}
			NodeStatusMap nodeStatusMap = (NodeStatusMap) nodeStatusQueue.lastElement();
			NodeStatusMap auditorNodeStatusMap =  (NodeStatusMap) auditorNodeStatusQueue.lastElement(); 
			if(nodeStatusMap == null || (Boolean)(nodeStatusMap.get(NodeStatusMap.STATUS_START))==false){
				return null ;
			}
			int cpu = (Integer) auditorNodeStatusMap.get(StatusDefinition.CPU_USAGE);
			int memory = (Integer) auditorNodeStatusMap.get(StatusDefinition.MEM_USAGE);
			Date now = new Date();
			Date  dayBegin = ObjectUtils.dayBegin(now) ;
			Condition con = new Condition() ;
			con.setStart_time(StringUtil.longDateString(dayBegin)) ;
			con.setEnd_time(StringUtil.longDateString(now)) ;
			EventService  eventService = (EventService) SpringWebUtil.getBean("eventService", request);
			Map<String,Object> eventAndAlarmCount = (Map<String, Object>) eventService.getEventAlarmCount(con) ;
			StringBuffer sb = new StringBuffer();
			sb.append("<system cpu_usage=\""+cpu
					   +"\" mem_usage=\""+memory
					   +"\" log_flow=\""+ getLogFlowData(auditorNodeStatusMap,nodeStatusMap)
					   +"\" storage_usage=\""+auditorNodeStatusMap.get("storage_usage")
					   +"\" storage_avaliable=\""+ auditorNodeStatusMap.get("storage_avaliable")
					   +"\" storage_total=\""+ auditorNodeStatusMap.get("storage_total")
					   +"\" assetCount=\""+assetFacade.getTotal()
					   +"\" onlineAssetCount=\""+assetFacade.getOnlineCount()
					   +"\" offlineAssetCount=\""+assetFacade.getOfflineCount()
					   +"\" alarmCount=\""+0
					   +"\" eventCount=\""+eventAndAlarmCount.get("event")
					   +"\"/>");
			return build.entity(sb.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	private NodeMgrFacade getNodeMgrFacade(HttpServletRequest request) {
		if(this.nodeMgrFacade==null){
			this.nodeMgrFacade=FacadeUtil.getNodeMgrFacade(request,null);
		}
		return this.nodeMgrFacade;
	}
	
	/**
	 * 服务器流量总数
	 * @param nodeStatusMap
	 * @return
	 */
	private static long getLogFlowData(NodeStatusMap audNodeStatusMap,NodeStatusMap nodeStatusMap){
		long flowCount=getComponentRate(nodeStatusMap,"SyslogCollector");
		flowCount+=getComponentRate(nodeStatusMap,"NetFlowCollector");
		flowCount+=getComponentRate(nodeStatusMap,"SnmpTrapCollector");
		flowCount+=getComponentRate(nodeStatusMap,"SchedularCollector");
		flowCount+=getComponentRate(nodeStatusMap,"AuditJmsCollector");
		flowCount+=getComponentRate(audNodeStatusMap, "AgentJmsCollector") ;
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
}
