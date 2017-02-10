package com.topsec.tsm.sim.report.model;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.NodeUtil;


public class ReportDispatchModel implements ThreadPoolExecuteDispatchUtilListener{
	
	private static final Logger log = LoggerFactory.getLogger(ReportDispatchModel.class);
	
	private String cmd;
	
	private final Long timeout=2*60*1000L;

	private String nodeId;
	
	private Boolean queryComplete=false;
	
	private NodeMgrFacade nodeMgrFacade;
	
	private Map<String,Object> map;
	
	private List list;  //结果集
	
	public ReportDispatchModel() {
		map=new HashMap<String, Object>();
	}

	public ReportDispatchModel(String nodeId) {
		this.nodeId = nodeId;
		map=new HashMap<String, Object>();
	}

	@Override
	public void onCommand(){
		Node node=null;
		try {
			node=nodeMgrFacade.getNodeByNodeId(nodeId,false,true,false,false);
		} catch (Exception e) {
			log.error(e.getMessage());
//			e.printStackTrace();
		}
		
		if(node==null){
			log.error("onCommand(),node==null!!! "+nodeId+" node is deleted by other user!!!");
			queryComplete=true; //异常, 外面有while(true)循环, 所以即使有异常,也需要更改flag值为true, list为null
			return;
		}else{
			Date date=NodeStatusQueueCache.getInstance().getLastUpdateDate(nodeId);
			if(date==null){
				log.warn("onCommand(), date==null, auditor "+node.getNodeId()+" offline!!!");
				queryComplete=true;  
				return;
			}
			
			long duration=GregorianCalendar.getInstance().getTimeInMillis()-date.getTime();
			if(duration>NodeStatusQueueCache.nodeTimeout){
				//一分钟
//				log.info("onCommand(), duration>"+NodeStatusQueueCache.nodeTimeout+", auditor "+node.getNodeId()+" offline!!!");
				queryComplete=true;  
				return;
			}
		}
		
		String[] route=NodeUtil.getRoute(node);
		//如果有独立Reportservice节点，则修改节点路由
		Set<Node> children = node.getChildren();
		for(Node child:children){
			String type = child.getType();
//			if(NodeDefinition.NODE_TYPE_SERVICE.equals(type)){
//				route = NodeUtil.getRoute(child);
//				break;
//			}
			if(NodeDefinition.NODE_TYPE_REPORTSERVICE.equals(type)){
				route = NodeUtil.getRoute(child);
				break;
			}
		}
		Serializable serializable=null;
		try {
//			log.info("Auditor "+node.getNodeId()+" online!!! dispatchCommand is going on!!!");
			serializable=NodeUtil.getCommandDispatcher().dispatchCommand(route, cmd,  (Serializable)map, timeout);
		} catch (CommunicationException e) {
			log.error(node.getNodeId()+" CommunicationException!!!"+e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(node.getNodeId()+" Exception!!!"+e.getMessage());
		}
		
		list=(List)serializable;
		queryComplete=true;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public boolean isQueryComplete() {
		return queryComplete;
	}

	public void setQueryComplete(boolean flag) {
		this.queryComplete = flag;
	}
	
	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
	
	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}
	
	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public NodeMgrFacade getNodeMgrFacade() {
		return nodeMgrFacade;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}

}
