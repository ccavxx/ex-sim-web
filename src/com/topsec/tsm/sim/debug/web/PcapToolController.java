package com.topsec.tsm.sim.debug.web;

import java.io.Serializable;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.topsec.tal.base.util.ChainMap;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.NodeUtil;

@Controller
@RequestMapping("pcap")
public class PcapToolController {
	
	@Autowired
	private NodeMgrFacade nodeMgr ;
	
	@RequestMapping("ui")
	public String ui(@RequestParam("nodeId")String nodeId,HttpServletRequest request){
		Node collectNode = NodeUtil.getCollectNode(nodeId, nodeMgr) ;
		request.setAttribute("node", collectNode) ;
		return "page/debug/pcap_tool";
	}
	
	@RequestMapping("start")
	@ResponseBody
	public Object start(@RequestParam("nodeId")String nodeId,HttpServletRequest request){
		Result result = new Result() ;
		String filter = getFilter(request);
		try{
			ChainMap<?,?> params = ChainMap.newMap("filter", filter).push("charset", request.getParameter("charset")) ;
			sendCommand(nodeId, MessageDefinition.CMD_COLLECTOR_PCAP_START, params, 5000) ;
		}catch(CommonUserException e){
			return result.buildError(e.getMessage()) ;
		}
		return result.buildSuccess() ;
	}
	
	private String getFilter(HttpServletRequest request){
		String src = request.getParameter("src") ;
		String dest = request.getParameter("dest") ;
		Integer destPort = StringUtil.toInteger(request.getParameter("destPort")) ;
		StringBuffer filter = new StringBuffer() ;
		appendFilter(filter, "src host", src) ;
		appendFilter(filter, "dst port", destPort == null ? null : String.valueOf(destPort)) ;
		appendFilter(filter, "dst host", dest) ;
		return filter.toString() ;
	}
	
	private StringBuffer appendFilter(StringBuffer filter,String conditionName,String textString){
		if(textString != null && textString.trim().length() > 0){
			if(filter.length() > 0){
				filter.append(" and ") ;
			}
			filter.append(conditionName).append(" ").append(textString) ;
		}
		return filter ;
	}
	
	@RequestMapping("stop")
	@ResponseBody
	public Object stop(@RequestParam("nodeId")String nodeId){
		Result result = new Result() ;
		try {
			sendCommand(nodeId, MessageDefinition.CMD_COLLECTOR_PCAP_STOP, null, 5000);
		} catch (CommonUserException e) {
			return result.buildError(e.getMessage()) ; 
		}
		return result.buildSuccess() ;
	}
	
	@RequestMapping("getPacket")
	@ResponseBody
	public Object getPacket(@RequestParam("nodeId")String nodeId){
		try {
			return sendCommand(nodeId, MessageDefinition.CMD_COLLECTOR_PCAP_GET, null, 5000);
		} catch (CommonUserException e) {
			return Collections.emptyList() ; 
		}
	}
	
	@RequestMapping("download")
	public void download(@RequestParam("nodeId")String nodeId,HttpServletRequest request,HttpServletResponse response){
		try {
			CommonUtils.setDownloadHeaders(request, response, "PcapData.dat") ;
			byte[] datas = (byte[]) sendCommand(nodeId, MessageDefinition.CMD_COLLECTOR_PCAP_GET_ALL, null, 60000) ;
			response.getOutputStream().write(datas) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Object sendCommand(String nodeId,String command,Serializable params,long expiration){
		if(NodeStatusQueueCache.offline(nodeId)){
			throw new CommonUserException("节点掉线！") ;
		}
		Node node = nodeMgr.getNodeByNodeId(nodeId, false, false, false, false) ;
		try {
			return NodeUtil.dispatchCommand(NodeUtil.getRoute(node), command, params,expiration) ;
		} catch (CommunicationException e) {
			throw new CommonUserException("下发命令失败", e) ;
		}
	}
	
}
