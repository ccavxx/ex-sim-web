package com.topsec.tsm.sim.util;

import java.util.Set;

import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;

public class RouteUtils {

	private static NodeMgrFacade nodeMgrFacade;

	public static String[] getRoute() {
		return getIndexServiceRoutes() ;
	}
	
	public static String[] getAuditorRoutes(){
		Node auditor = nodeMgrFacade.getKernelAuditor(false) ;
		return NodeUtil.getRoute(auditor) ;
	}
	
	/**
	 * 获取Service节点路径
	 * @return
	 */
	public static String[] getIndexServiceRoutes(){
		Node node = nodeMgrFacade.getKernelAuditor(false);
		return getIndexServiceRoutes(node) ;
	}
	public static String[] getQueryServiceRoutes(){
		Node node = nodeMgrFacade.getKernelAuditor(false);
		return getQueryServiceRoutes(node) ;
	}
	
//	public static String[] getRoute(String nodeId){
//		return getServiceRoutes(nodeId) ;
//	}
	
	public static String[] getQueryServiceRoutes(String nodeId){
		Node node=nodeMgrFacade.getNodeByNodeId(nodeId,false,true,false,false);
		return getQueryServiceRoutes(node) ;
	}
	public static String[] getIndexServiceRoutes(String nodeId){
		Node node=nodeMgrFacade.getNodeByNodeId(nodeId,false,true,false,false);
		return getIndexServiceRoutes(node) ;
	}
	
	public static String[] getIndexServiceRoutes(Node node){
		String[] route = null;
		if (node != null) {
			route=NodeUtil.getRoute(node);
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
					route = NodeUtil.getRoute(child);
					break;
				}
			}
		} 
		return route;
	}
	
	public static String[] getCollectorRoutes(Node node){
		String[] route = null;
		if (node != null) {
			route=NodeUtil.getRoute(node);
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_COLLECTOR.equals(type)){
					route = NodeUtil.getRoute(child);
					break;
				}
			}
		} 
		return route;
	}
	public static String[] getActionRoutes(Node node){
		String[] route = null;
		if (node != null) {
			route=NodeUtil.getRoute(node);
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_ACTION.equals(type)){
					route = NodeUtil.getRoute(child);
					break;
				}
			}
		} 
		return route;
	}
	public static String[] getReportRoutes(Node node){
		String[] route = null;
		if (node != null) {
			route=NodeUtil.getRoute(node);
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_REPORTSERVICE.equals(type)){
					route = NodeUtil.getRoute(child);
					break;
				}
			}
		} 
		return route;
	}
	public static String[] getQueryServiceRoutes(Node node){
		String[] route = null;
		if (node != null) {
			route=NodeUtil.getRoute(node);
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_QUERYSERVICE.equals(type)){
					route = NodeUtil.getRoute(child);
					break;
				}
			}
		} 
		return route;
	}
	
	public static NodeMgrFacade getNodeMgrFacade() {
		return nodeMgrFacade;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		RouteUtils.nodeMgrFacade = nodeMgrFacade;
	}

}
