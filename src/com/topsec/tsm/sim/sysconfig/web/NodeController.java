package com.topsec.tsm.sim.sysconfig.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ChainMap;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.AssetState;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.node.component.handler.AutoProtectConfigration;
import com.topsec.tsm.node.status.NodeStatusMap;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.StatusDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueue;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.FtpConfigUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.util.diagnose.DebugZipUtil;
import com.topsec.tsm.util.diagnose.SnapShot;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("node")
public class NodeController {
	protected static Logger log= LoggerFactory.getLogger(NodeController.class);
	private final String localHostAddress= IpAddress.getLocalIp().getLocalhostAddress() ;
	
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	
	/**
	 * @Title: getNodeJSONArray
	 * @Description: 格式化获得的node list 数据
	 * @author wq
	 * @param nodeList
	 * @return
	 * @throws
	 */
	private JSONArray getNodeJSONArray(final SID sid,List<Node> nodeList) {
		
		JSONArray jsonNodes = FastJsonUtil.toJSONArray(nodeList,new JSONConverterCallBack<Node>() {
					@Override
					public void call(JSONObject result, Node obj) {
						Long parentId = obj.getParent().getResourceId();
						Node parentNode = nodeMgrFacade.getNodeById(parentId);
						if(!NodeUtil.isSMP(parentNode.getNodeId())){
							String parentIp = parentNode.getIp();
							result.put("parentIp", parentIp);
						}
						String type = obj.getType() ;
						boolean isAgent = NodeUtil.isAgent(type) ;
						boolean isAction = NodeUtil.isAction(type) ;
						boolean isAuditor = NodeUtil.isAuditor(type) ;
						result.put("entyType", isAgent ? "代理" : isAction ? "告警节点" : isAuditor ? "服务器" : "");
						if (isAgent || isAction) {
							Node nodeWithSegment = nodeMgrFacade.getNodeByNodeId(obj.getNodeId(), false, false, true, true) ;
							AutoProtectConfigration protectConfig = NodeUtil.findFirstSegmentConfig(nodeWithSegment, NodeDefinition.HANDLER_AUTOPROTECT, AutoProtectConfigration.class) ;
							if(protectConfig != null && !localHostAddress.equals(obj.getIp()) && sid.isOperator()){
								result.put("showProtectButton", "true") ;
								result.put("protectState", protectConfig.isEnableProtected()) ;
							}
							if(!localHostAddress.equals(obj.getIp()) && sid.isOperator()){
								result.put("action","delAction");
							}
						}
						result.put("showPcap", isAuditor || isAgent) ;
						result.put("state", NodeStatusQueueCache.online(obj.getNodeId()) ? "0" : "1");
						Date date = NodeStatusQueueCache.getInstance().getFirstConnectDate(obj.getNodeId());
						result.put("aliveTime",date == null ? "0秒" : time2StrLeng(System.currentTimeMillis() - date.getTime()));
					}
				}, "resourceId", "resourceName", "ip", "type","version", "nodeId", "ip", "alias");
		return jsonNodes;
	}
	/**
	 * @Title: changeProtectState
	 * @Description: 代理自保护启用/禁用
	 */
	@RequestMapping("changeProtectState")
	@ResponseBody
	public Object changeProtectState(@RequestParam("state")boolean state,@RequestParam("nodeId")String nodeId){
		Node nodeWithSegment = nodeMgrFacade.getNodeByNodeId(nodeId, false, false, true, true) ;
		Component component = NodeUtil.findFirstComponent(nodeWithSegment,NodeDefinition.HANDLER_AUTOPROTECT) ;
		AutoProtectConfigration protectConfig = NodeUtil.findFirstSegmentConfig(nodeWithSegment, NodeDefinition.HANDLER_AUTOPROTECT, AutoProtectConfigration.class) ;
		protectConfig.setEnableProtected(state) ;
		Result result = new Result() ;
		try {
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, protectConfig) ;
			result.buildSuccess(null);
		} catch (NodeException e) {
			result.buildError("自我保护失败！！");
			e.printStackTrace();
		}
		return result ;
	}

	/**
	 * @Title: time2StrLeng
	 * @Description: 将时间差直接转为*天*小时*分*秒
	 * @author wq
	 * @param time
	 * @return String *天*小时*分*秒
	 * @throws
	 */
	private static String time2StrLeng(long time) {
		time = time / 1000 ;
		StringBuilder str = new StringBuilder();
		if(time <= 0){
			return str.append("0秒").toString();
		}
		int hour = 3600;
		int day = 86400;
		if(time < 60) {
			str.append(time).append("秒");
		} else if(time < hour){
			str.append(time / 60).append("分")
			   .append(time % 60).append("秒");
		} else if(time < day){
			str.append(time / hour).append("小时")
			   .append(time % hour / 60).append("分")
			   .append(time % hour % 60).append("秒");
		} else {
			str.append(time / day).append("天")
			   .append(time % day / hour).append("小时")
			   .append(time % day % hour / 60).append("分钟")
			   .append(time % day % hour % 60).append("秒");
		}
		return str.toString();
	}
	/**
	 * 获得节点管理分页列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="allNodePage")
	@ResponseBody
	public Object allNodePage(SID sid,HttpServletRequest request) {
		String pageNo = request.getParameter("page");
		String pageSize = request.getParameter("rows");
		
		if(StringUtil.isBlank(pageNo) || StringUtil.isBlank(pageSize)) {
			return new JSONArray();
		}
		
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("resourceName", request.getParameter("resourceName"));
		condition.put("state", request.getParameter("state"));
		condition.put("ip", request.getParameter("ip"));
		
		List<String> types = new ArrayList<String>(2);
		types.add(NodeDefinition.NODE_TYPE_AUDIT);
		types.add(NodeDefinition.NODE_TYPE_AGENT);
		types.add(NodeDefinition.NODE_TYPE_ACTION);
		Map<String, Object> nodesMap = nodeMgrFacade.getPageNodesByTypes(types, Integer.valueOf(pageNo), Integer.valueOf(pageSize), condition, false, false, false, false);
		List<Node> pageNodes = (List<Node>)nodesMap.get("allNodes");
		JSONArray jsonNodes = getNodeJSONArray(sid,pageNodes);
		
		JSONObject result = new JSONObject();
		result.put("total", nodesMap.get("total"));
		result.put("rows", jsonNodes);
		return result;
	}
	
	/**
	 * 获得节点管理列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="allNode")
	@ResponseBody
	public Object allNode(SID sid,HttpServletRequest request) {
		
		List<String> types = new ArrayList<String>(2);
		types.add(NodeDefinition.NODE_TYPE_AUDIT);
		types.add(NodeDefinition.NODE_TYPE_AGENT);
		
		List<Node> allNodes = nodeMgrFacade.getNodesByTypes(types, false, false, false, false);
		
		JSONArray jsonNodes = getNodeJSONArray(sid,allNodes);
		return jsonNodes;
	}
	/**
	 * 获得节点管理列表
	 * 
	 * @return
	 */
	@RequestMapping(value="getAllNodeList")
	@ResponseBody
	public Object getAllNodeList(HttpServletRequest request) {
		List<String> nodesList= new ArrayList<String>();
		nodesList.add(NodeDefinition.NODE_TYPE_AUDIT);
		nodesList.add(NodeDefinition.NODE_TYPE_COLLECTOR);
		nodesList.add(NodeDefinition.NODE_TYPE_QUERYSERVICE);
		nodesList.add(NodeDefinition.NODE_TYPE_INDEXSERVICE);
		nodesList.add(NodeDefinition.NODE_TYPE_REPORTSERVICE);
		nodesList.add(NodeDefinition.NODE_TYPE_ACTION);
		return nodesList;
	}
	/**
	 * 通过resourceId更新节点名称
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("editNodeName")
	@ResponseBody
	public Object updateNodeNameById(
			@RequestParam(value = "resourceId") Long resourceId,
			@RequestParam(value = "resourceName") String resourceName
			 ) {
		int count = nodeMgrFacade.updateNodeNameById(resourceName, resourceId);
		JSONObject result = new JSONObject();
		boolean flag = false;
		if(count==1){
			flag = true;
		}
		if(StringUtils.containsAny(resourceName, "<>'*?:/|\"\\")){
			flag = false ;
		}
		result.put("status", flag);
		return result;
	}

	/**
	 * 功能描述: 检查节点是否在线
	 */
	@RequestMapping("checkNodeIsOnline")
	@ResponseBody
	public Object checkNodeIsOnline(
			@RequestParam(value = "resourceId") Long resourceId,
			@RequestParam(value = "ip") String ip,
			@RequestParam(value = "type") String type,
			@RequestParam(value = "nodeId") String nodeId,
			HttpServletRequest request) throws Exception {

		if(type==null||resourceId==null||nodeId==null){
			throw new Exception("com.topsec.tsm.sim.sysconfig.web.NodeController.checkNodeIsOnline(),type==null||resourceId==null||nodeId==null!!!");
		}
		JSONObject result = new JSONObject();
		result.put("resourceId", resourceId);
		result.put("ip", ip);
		result.put("type", type);
		result.put("nodeId", nodeId);
		result.put("isOnline", NodeStatusQueueCache.online(nodeId));
		return result;
	}

	/**
	 * 下载日志
	 */
	@RequestMapping(value="sendLogCommand")
	public void sendLogCommand(@RequestParam(value="nodeId") String nodeId,HttpServletRequest request, HttpServletResponse response)throws Exception {
			Map<String,Object> map=FtpConfigUtil.getInstance().getFTPConfigByKey("log");
			if(map==null || nodeId==null){
				throw new Exception("FtpConfigUtil.getInstance().getFTPConfigByKey(), map == null || nodeId == null!!");
			}
			try {
				SnapShot snap = new SnapShot();
				File snapFile = new File(new File("logs"),"SMP snapshot.log"); 
				snap.makeThreadDump(snapFile);
			} catch (Exception e) {
				log.warn("Get system dump failed!", e);
			}
			
			Node node = nodeMgrFacade.getNodeByNodeId(nodeId,false,true,false,false);
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				String[] route = null;
				//通知每个节点生成环境dump信息
				route = NodeUtil.getRoute(child);
				NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_NODE_GET_DUMP, null);
				if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
					route = NodeUtil.getRoute(child);
					try {
						NodeUtil.dispatchCommand(route,MessageDefinition.CMD_NODE_GET_LOG, null,5*60*1000);
					} catch (Exception e) {
						log.warn("获取数据表信息失败，"+e.getMessage());
					}
//					break;
				}
			}
			String[] route = NodeUtil.getRoute(node);
			try {
				String ftpPathFile = (String) NodeUtil.dispatchCommand(route, MessageDefinition.CMD_NODE_GET_LOG, (Serializable) map, 5*60*1000);
				request.setAttribute("ftpfilepath", ftpPathFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//下载日志信息
			downloadFile( request, response);
	}
	
	/**
	 * 下载核心auditor日志
	 */
	@RequestMapping(value="sendLogCommandForKernel")
	public void sendLogCommandForKernel(HttpServletRequest request, HttpServletResponse response)throws Exception {
			Map<String,Object> map=FtpConfigUtil.getInstance().getFTPConfigByKey("log");
			if(map==null){
				throw new Exception("FtpConfigUtil.getInstance().getFTPConfigByKey(), map == null!");
			}
			//TODO  调用获得日志接口, 然后调用downloadFile方法到ftp上下载日志
			try {
				int port = Integer.parseInt((String) map.get("port"));
				String host = (String) map.get("host");
				String user = (String) map.get("user");
				String password = (String) map.get("password");
				String home = (String) map.get("home");
				String encoding = (String)map.get("encoding");
				List<String> exts = (List<String>) map.get("exts");

				File file = DebugZipUtil.createDebugZip("Server", home, exts);

				String fileName = file.getName();
				boolean result = FtpUploadUtil.uploadFile(host, port, user,password,encoding, ".", fileName, new FileInputStream(file));
				
				FileUtils.deleteQuietly(file);
				if (result){
					request.setAttribute("ftpfilepath", fileName);
				} else{
					request.setAttribute("errorInfor", "下载诊断日志失败.");
				}
			} catch (Exception e) {
				request.setAttribute("errorInfor", "发送请求处理错误:" + e.getMessage());
			}
			//下载日志信息
			downloadFile( request, response);
	}

	public void downloadFile(HttpServletRequest request, HttpServletResponse response)throws Exception {
		String filename = (String) request.getAttribute("ftpfilepath");
		CommonUtils.setDownloadHeaders(request, response, filename) ;
		String serverHome = System.getProperty("jboss.server.home.dir");
		String downPath=(String)FtpConfigUtil.getInstance().getFTPConfigByKey("log").get("downPath");
		if (SystemUtils.IS_OS_WINDOWS) {
			downPath = StringUtils.replace(downPath, "/", File.separator);
		}else{
			downPath = StringUtils.replace(downPath, "\\", File.separator);
		}
		String savaLogPathfile = new StringBuilder(serverHome).append(downPath).append(filename).toString();
		File file = new File(savaLogPathfile);
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[1024*1024];
			int bytesRead;
			while ((bytesRead = bis.read(buff, 0, buff.length)) != -1) {
				bos.write(buff, 0, bytesRead);
			}
			bos.flush();
		} catch (ClientAbortException e){
			//客户端取消
		}catch (Exception e) {
			log.error("下载文件出错....", e);
		} finally {
			ObjectUtils.close(bis) ;
			ObjectUtils.close(bos) ;
		}
	}

	@RequestMapping("delete")
	@ResponseBody
	public Object delete(SID sid,@RequestParam("nodeId")String nodeId) {
		Result result = new Result() ;
		try {
			if(!sid.isOperator()){
				return result.buildError("没有删除权限！") ;
			}
			Node node = nodeMgrFacade.getNodeByNodeId(nodeId) ;
			if(!nodeId.equals(localHostAddress)){
				if(NodeUtil.isAgent(node.getType())){
					List<AssetObject> allAssets = AssetFacade.getInstance().getByScanNode(nodeId) ;
					for(AssetObject ao:allAssets){
						AssetFacade.getInstance().changeState(ao.getId(), 0, true) ;
					}
					nodeMgrFacade.delNode(node) ;
				}else if(NodeUtil.isAction(node.getType())){
					nodeMgrFacade.delNode(node) ;
				}
			}
		} catch (Exception e) {
			log.error("删除节点出错!",e) ;
			result.buildError("删除节点失败，系统内部错误!") ;
		}
		return result ;
	}
	
	@RequestMapping("getNodesStatus")
	@ResponseBody
	public Object getNodesStatus(@RequestParam("nodeIds")String nodeIdString) {
		String[] nodeIds = StringUtil.split(nodeIdString) ;
		List<String> types = Arrays.asList(new String[]{NodeDefinition.NODE_TYPE_AUDIT,NodeDefinition.NODE_TYPE_AGENT}) ;
		List<Node> nodes = nodeMgrFacade.getNodesByTypes(types, false, false, false, false) ;
		JSONArray result = new JSONArray(nodeIds.length) ;
		for(Node node:nodes){
			if(ArrayUtils.contains(nodeIds, node.getNodeId())){
				JSONObject nodeJSON = FastJsonUtil.toJSON(node,"resourceName=name","ip") ;
				boolean online = NodeStatusQueueCache.online(node.getNodeId()) ;
				
				nodeJSON.put("id", node.getNodeId()) ;
				nodeJSON.put("typeName", NodeUtil.isAuditor(node.getType()) ? "服务器" : "代理");
				nodeJSON.put("state",  online ? "ONLINE" : "OFFLINE") ;
				nodeJSON.put("stateText", online ? "在线" : "离线") ;
				nodeJSON.put("stateIcon", AssetUtil.getStateIcon(online ? AssetState.ONLINE : AssetState.OFFLINE)) ;
				NodeStatusQueue nodeStatusQueue = NodeStatusQueueCache.getInstance().getNodeStatus(node.getNodeId());
				if (nodeStatusQueue != null) {
					NodeStatusMap nodeStatusMap =  (NodeStatusMap) nodeStatusQueue.lastElement(); 
					nodeJSON.put("cpuUsage", nodeStatusMap.get(StatusDefinition.CPU_USAGE)+"%");
					nodeJSON.put("memUsage", nodeStatusMap.get(StatusDefinition.MEM_USAGE)+"%");
				}
				result.add(nodeJSON) ;
			}
		}
		return result ;
	}
	
	@RequestMapping("resetLogLevel")
	public String resetLogLevel(@RequestParam(value="nodeId",required=false)String nodeId,
								@RequestParam(value="className",required=false)String className,
								@RequestParam(value="level",required=false)String level,
								HttpServletRequest request){
		if(StringUtil.isNotBlank(nodeId)){
			Node node = nodeMgrFacade.getNodeByNodeId(nodeId) ;
			if (node != null) {
				Map<String,String> params = new HashMap<String,String>(2) ;
				params.put("className", className) ;
				params.put("level", level) ;
				try {
					NodeUtil.sendCommand(NodeUtil.getRoute(node), MessageDefinition.CMD_NODE_MODIFY_LOG_LEVEL, (Serializable)params,1000*10);
				} catch (CommunicationException e) {
					System.out.println("日志级别修改命令下发超时！");
				} catch(Exception e){
					System.out.println("日志级别修改出错！");
				}
			}
		}
		List<Node> nodes = nodeMgrFacade.getAll() ;
		request.setAttribute("nodes", nodes) ;
		return "/page/sysconfig/resetLogLevel" ;
	}
	
	/**
	 * 
	 * @param nodeId 节点ID
	 * @param field 属性名称（使用.分割各级属性）
	 * @param ownerClassNameList （属性所属class，使用,分割多个class）
	 * @param fieldFilters 属性过滤器，只有符合条件的才显示（使用,将多个属性过滤器分割，每个过滤器使用.将每个字段过滤器再分割）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getFields")
	@ResponseBody
	public Object getFields(
			@RequestParam(value="nodeId",required=false)String nodeId,
			@RequestParam(value="field",required=false)String field,
			@RequestParam(value="ownerClassNameList",required=false)String ownerClassNameList,
			@RequestParam(value="fieldFilters",required=false)String fieldFilters){
		try {
			if(StringUtil.isBlank(nodeId)){
				List<Node> nodes = nodeMgrFacade.getAll() ;
				List<Map<String,Object>> nodeList = new ArrayList<Map<String,Object>>() ;
				for(Node node:nodes){
					ChainMap<String, Object> nodeMap = new ChainMap<String, Object>() ;
					nodeMap.put("name", node.getIp()+"("+node.getType()+")") ;
					nodeMap.push("id", StringUtil.getUUIDString()) ;
					nodeMap.push("state", "closed") ;
					nodeMap.put("attributes", ChainMap.<String, Object>newMap("nodeId", node.getNodeId())) ;
					nodeList.add(nodeMap) ;
				}
				return nodeList ;
			}else{
				ChainMap<String, Object> params = ChainMap.<String, Object>newMap("field", field) ;
				params.push("ownerClassNameList", ownerClassNameList) ;
				params.push("fieldFilters", StringUtil.split(fieldFilters)) ;
				List<Map<String,Object>> fields = (List<Map<String, Object>>) NodeUtil.dispatchCommand(NodeUtil.getRoute(nodeMgrFacade.getNodeByNodeId(nodeId)), MessageDefinition.CMD_NODE_GET_FIELDS, params,1000*30);
				Collections.sort(fields,new Comparator<Map<String,Object>>() {
					@Override
					public int compare(Map<String, Object> o1, Map<String, Object> o2) {
						return ((String)o1.get("name")).compareTo((String)o2.get("name"));
					}
				}) ;
				for(Map<String,Object> fd:fields){
					fd.put("state", ((Boolean)fd.remove("hasChildren")) ? "closed" : "open") ;
					fd.put("id", StringUtil.getUUIDString()) ;
					Map<String,Object> attributes = ChainMap.<String, Object>newMap("nodeId", nodeId).push("ownerClassName", fd.get("ownerClassName")) ;
					attributes.put("nestFieldName", StringUtil.isBlank(field) ? fd.get("name") : field+"."+fd.get("name")) ;
					fd.put("attributes", attributes) ;
				}
				return fields ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList() ;
	}
	
	@RequestMapping("nodeInfo")
	public String nodeInfo(@RequestParam("pass")String pass){
		UserService userService = (UserService) SpringContextServlet.springCtx.getBean("userService") ;
		AuthAccount ac = userService.getUserByUserName("administrator") ;
		if(ac == null){
			return null ;
		}
		if(!StringUtil.MD5(pass).equalsIgnoreCase(ac.getPasswd())){
			return null ;
		}
		
		return "/page/sysconfig/node_info" ;
	}
	
	@RequestMapping("getNodeByType")
	@ResponseBody
	public Object getNodeByType(HttpServletRequest request){
		String[] nodeTypes = request.getParameterValues("nodeType") ;
		if(nodeTypes == null || nodeTypes.length == 0){
			return Collections.emptyList() ;
		}
		List<Node> nodes = nodeMgrFacade.getNodesByTypes(Arrays.asList(nodeTypes), false, false, false, false) ;
		JSONArray nodeJson = FastJsonUtil.toJSONArray(nodes, "ip","nodeId","resourceId","type","alias") ;
		return nodeJson ;
	}
	
	/**
	 * 获取所有action节点
	 * @return
	 */
	@RequestMapping("getActionNodes")
	@ResponseBody
	public Object getActionNodes(){
		List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_ACTION, false, false, false, false);
		JSONArray nodesJSON = FastJsonUtil.toJSONArray(nodes, "nodeId","ip") ;
		return nodesJSON;
	}
}
