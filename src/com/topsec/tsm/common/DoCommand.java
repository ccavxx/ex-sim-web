package com.topsec.tsm.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.FlowConfiguration;
import com.topsec.tsm.node.NodeConfiguration;
import com.topsec.tsm.node.NodeConfigurationImpl;
import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.node.status.ComponentStatusMap;
import com.topsec.tsm.node.status.NodeStatusMap;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.service.NodeMgrFacadeImpl;
import com.topsec.tsm.sim.node.util.NodeStateCache;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.DataFlow;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.NodeUtil;

public class DoCommand {
	private static DoCommand _instance = null;
	private static final Logger log = LoggerFactory.getLogger(DoCommand.class);

	public synchronized static DoCommand getInstance() {
		if (_instance == null) {
			_instance = new DoCommand();
		}
		return _instance;
	}

	/**
	 * 
	 *@标题:下发活动链表项到综合审计
	 *@作者:ysf 
	 *@创建时间:Feb 18, 2011 10:14:08 AM
	 *@参数:
	 *@返回值:void
	 */
	public void sendActiveListCommand(NodeMgrFacade nodeMgrFacade,Serializable obj, String nodeIds) throws Exception {
		String[] ids = StringUtil.split(nodeIds);
		for (String id : ids) {
			Node node = (Node) nodeMgrFacade.getNodeById(Long.parseLong(id));
			String[] route = NodeUtil.getRoute(node);
			NodeUtil.getCommandDispatcher().sendCommand(route,MessageDefinition.CMD_ACTIVELIST_GET_ACTIVEENTRYS, obj);				
		}
	}

	/**
	 * 
	 * @throws Exception
	 * @标题:注册node
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 3:49:07 PM
	 * @参数:
	 * @返回值:Serializable
	 */
	public Serializable doRegister(Serializable obj, String[] route,NodeMgrFacade nodeMgrFacade) throws Exception {
		NodeConfiguration configuration = (NodeConfiguration) obj;
		String nodeType = configuration.getType();
		if (nodeType != null) {
			Node node = NodeUtil.toNode(configuration);
			List<FlowConfiguration> flowConfs = configuration.getFlows();
			Set<DataFlow> flows = new HashSet<DataFlow>();
			if (flowConfs != null) {
				for (FlowConfiguration flowConf : flowConfs) {
					DataFlow dataFlow = NodeUtil.toFlow(flowConf, configuration.getComponents());
					flows.add(dataFlow);
				}
			}
			node.setDataFlows(flows);

			Boolean b=false;
			try {
				b=nodeMgrFacade.registerNode(node, route);
			}catch (NodeException e) {
				throw e ;
			} catch (Exception e) {
				log.error("nodeMgrFacade.registerNode failed!!!"+e.getMessage());
				NodeMgrFacadeImpl.nodeIdMap.remove(node.getNodeId());
				throw new NodeException(e) ;
			}
			
			if(!b){
				return null;
			}else{
				//成功注册, 节点信息已经在数据库中存在
				try{
					//有节点注册时,清空首页topo节点缓存
//					NodeMgrFacadeImpl.nodeTopoMap.clear();
					NodeMgrFacadeImpl.isNodesChanged=true;
//					//当节点注册时,需要更新可能已经修改的默认告警规则到新注册的节点上.
//					EventResponseService eventResponseService=(EventResponseService)SpringContextServlet.springCtx.getBean("com.topsec.tsm.tal.service.EventResponseService");
//					EventPolicySend.getInstance().updateDefaultEPtoNode(eventResponseService, nodeMgrFacade, node.getNodeId());
				}catch (Exception e) {
				    log.error(e.getMessage());
//				    e.printStackTrace();
				}
			}
			log.debug("node "+node.getNodeId()+" is regist success!");
		} else {
			log.error("The nodetype is not null!");
			throw new Exception("The nodetype is not null!");
		}
		return null;
	}

	/**
	 * 
	 * @标题:获取node所有配置信息
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 3:49:23 PM
	 * @参数:
	 * @返回值:Serializable
	 */
	public Serializable doGetConfig(Serializable obj, String[] route,NodeMgrFacade nodeMgrFacade) {
		String nodeId = route[0];
		
		//清空node缓存
		NodeStatusQueueCache.getInstance().removeNodeCacheByNameSpace(nodeId);
		
		try {
			Node node = nodeMgrFacade.getNodeWithDataFlowByNodeId(nodeId);
			NodeConfigurationImpl configuration = null;
			if (node == null) {
				configuration = null;
			} else {
				configuration = NodeUtil.toNodeConfiguration(node);
				sendGlobalPropertyCommand(node);
			}

			return configuration;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 下发全局变量
	 * 
	 * @标题:
	 * @作者:ysf
	 * @创建时间:Nov 5, 2010 10:34:34 AM
	 * @参数:node
	 * @返回值:void
	 */
	private void sendGlobalPropertyCommand(Node node) {
/*		try {
//			List<XmlModel> list = MergeFieldUtil.getInstance().readXMLWarn(
//					SceneXmlAction.AROUND);
			List<GlobalProperty> result = new ArrayList<GlobalProperty>();
//			if (list != null) {
//				for (XmlModel model : list) {
//					GlobalProperty p = new GlobalProperty(model.getKey(), model
//							.getAroundValue(), false);
//					result.add(p);
//				}
//			}
			String[] route = NodeUtil.getRoute(node);
			NodeUtil.getCommandDispatcher().sendCommand(route,
					MessageDefinition.CMD_NODE_SET_GLOBAL_PROPERTIES,
					(Serializable) result);
		//	NodeUtil.getluCommandDispatcher().sendCommand(route,
		//			MessageDefinition.CMD_NODE_SET_GLOBAL_PROPERTIES,
		//			(Serializable) result);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * 
	 * @throws Exception
	 * @throws NumberFormatException
	 * @标题:启动一个组件
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 3:49:49 PM
	 * @参数:
	 * @返回值:void
	 */
	public void startComponent(String nodeResourceId, String componentId,NodeMgrFacade nodeMgrFacade) throws Exception {
		Node node = (Node) nodeMgrFacade.getNodeById(StringUtil.toLong(nodeResourceId)) ;
		String[] route = NodeUtil.getRoute(node);
		NodeUtil.getCommandDispatcher().sendCommand(route,MessageDefinition.CMD_NODE_START_COMPONENT, componentId);
	}

	/**
	 * 
	 * @throws Exception
	 * @throws NumberFormatException
	 * @标题:启动一个节点
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 3:49:49 PM
	 * @参数:
	 * @返回值:void
	 */
	public void startNode(String deamonId, NodeMgrFacade nodeMgrFacade)
			throws Exception {

		Node deamon = (Node) nodeMgrFacade.getNodeByNodeId(deamonId);
		if (deamon != null) {
			String[] route = NodeUtil.getRoute(deamon);
			NodeUtil.getCommandDispatcher().sendCommand(route,
					MessageDefinition.CMD_NODE_START_NODE, deamonId);
		}
	}

	public void startLiveUpdateNode(Node node, String bussinessId)
			throws Exception {
		String[] route = NodeUtil.getRoute(node);
		NodeUtil.getCommandDispatcher().sendCommand(route,
				MessageDefinition.CMD_NODE_START_NODE, bussinessId);

	}

	public void restartLiveUpdateNode(Node node) throws Exception {
		String[] route = NodeUtil.getRoute(node);
		NodeUtil.getCommandDispatcher().sendCommand(route,
				MessageDefinition.CMD_NODE_RESTART_NODE, node.getNodeId());

	}

	public void stopLiveUpdateNode(Node node, String bussinessId)
			throws Exception {
		String[] route = NodeUtil.getRoute(node);
		NodeUtil.getCommandDispatcher().sendCommand(route,
				MessageDefinition.CMD_NODE_STOP_NODE, bussinessId);

	}

	/**
	 * 
	 * @throws Exception
	 * @throws NumberFormatException
	 * @标题:停止一个节点
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 3:49:49 PM
	 * @参数:
	 * @返回值:void
	 */
	public void stopNode(String deamonId, NodeMgrFacade nodeMgrFacade)
			throws Exception {

		Node deamon = (Node) nodeMgrFacade.getNodeByNodeId(deamonId);
		if (deamon != null) {
			String[] route = NodeUtil.getRoute(deamon);
			NodeUtil.getCommandDispatcher().sendCommand(route,
					MessageDefinition.CMD_NODE_STOP_NODE, deamonId);
		}
	}

	/**
	 * 
	 * @标题:停止一个组件
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 3:50:09 PM
	 * @参数:
	 * @返回值:void
	 */
	public void stopComponent(String nodeResourceId, String componentId,NodeMgrFacade nodeMgrFacade) {
		try {
			Node node = (Node) nodeMgrFacade.getNodeById(StringUtil.toLong(nodeResourceId));
			String[] route = NodeUtil.getRoute(node);
			NodeUtil.getCommandDispatcher().sendCommand(route,MessageDefinition.CMD_NODE_STOP_COMPONENT, componentId);
			/*
			 * NodeUtil.getCommandDispatcher().dispatchCommand(route,
			 * MessageDefinition.CMD_STOP, componentId,5000);
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @标题:给一个组件下发配置
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 3:50:23 PM
	 * @参数:
	 * @返回值:void
	 */
	public void setComponentConfiguration(String nodeResourceId, String componentId,NodeMgrFacade nodeMgrFacade) {
		try {
			Node node = (Node) nodeMgrFacade.getNodeById(StringUtil.toLong(nodeResourceId));
			String[] routes = NodeUtil.getRoute(node);
			Component component = nodeMgrFacade.getComponentWithSegments(StringUtil.toLong(componentId));
			setComponentConfiguration(routes, component) ;
		} catch (Exception e) {
			log.error("setComponentConfiguration(),"+e.getMessage());
//			e.printStackTrace();
		}
	}

	public void setComponentConfiguration(String[] routes,Component component)throws CommunicationException{
		try {
			NodeUtil.getCommandDispatcher().sendCommand(
							routes,
							MessageDefinition.CMD_NODE_SET_COMPONENT_CONFIGURATION,
							NodeUtil.toComponentConfiguration(component));
		} catch (CommunicationException e) {
			throw e ;
		}
	}

	/**
	 * 
	 * @标题:给一个节点下发配置
	 * @作者:ysf
	 * @创建时间:Sep 2, 2010 4:01:00 PM
	 * @参数:
	 * @返回值:void
	 */
	public void setNodeConfiguration(String nodeResourceId, NodeMgrFacade nodeMgrFacade) {
		try {
			Node node = (Node) nodeMgrFacade.getNodeWithDataFlow(Long.parseLong(nodeResourceId));
			String[] route = NodeUtil.getRoute(node);
			NodeUtil.getCommandDispatcher().sendCommand(route,
					MessageDefinition.CMD_NODE_SET_CONFIGURATION,
					NodeUtil.toNodeConfiguration(node));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @throws Exception
	 * @标题:主动获取节点的状态信息
	 * @作者:ysf
	 * @创建时间:Sep 28, 2010 11:21:33 AM
	 * @参数:
	 * @返回值:NodeStatusMap
	 */
	public NodeStatusMap getNodeStatus(long resourceId,NodeMgrFacade nodeMgrFacade) throws Exception {
		Node node = nodeMgrFacade.getNodeById(resourceId) ;
		return this.getNodeStatus(node);
	}

	public NodeStatusMap getNodeStatus(Node node) throws Exception {
		String[] route = NodeUtil.getRoute(node);
		if (NodeDefinition.NODE_TYPE_LIVEUPDATE.equals(node.getType())) {
			return (NodeStatusMap) NodeUtil.getCommandDispatcher()
					.dispatchCommand(route,
							MessageDefinition.MSG_NODE_GET_STATUS,
							node.getNodeId(), 3000);
		} else {
			return (NodeStatusMap) NodeUtil.getCommandDispatcher()
					.dispatchCommand(route,
							MessageDefinition.MSG_NODE_GET_STATUS,
							node.getNodeId(), 3000);
		}
	}

	/**
	 * 
	 * @标题:获取节点(包含组件)所有的状态信息
	 * @作者:ysf
	 * @创建时间:Sep 5, 2010 11:56:44 AM
	 * @参数:
	 * @返回值:void
	 */
	public void getNodeStatus(Serializable obj) {
		NodeStatusMap map = (NodeStatusMap) obj;
		String nameSpace = map.getNamespace();

		NodeStateCache.getInstance().putState(nameSpace, map);
		/*
		 * Map<String, Object> stats= map.toMap(); Set<String> _keys =
		 * stats.keySet(); for (String _key :_keys) {
		 * System.out.println("key==="+_key);
		 * System.out.println("value==="+stats.get(_key)); }
		 */
		Collection<ComponentStatusMap> comstatuses = map.getComponentStatusMaps();
		for (ComponentStatusMap comstatus : comstatuses) {
			Map<String, Object> status = comstatus.toMap();
			Set<String> keys = status.keySet();
			for (String key : keys) {
				// System.out.println("key==="+key);
				// System.out.println("value==="+status.get(key));
			}
		}
	}

	public String getAgentWorkStatus(NodeMgrFacade facade, String[] route) {
		try {
			String nodeId = route[0];
			Node node = facade.getNodeByNodeId(nodeId);
			if (node == null) {
				return NodeDefinition.NODE_WORKSTATUS_GREY;
			}
			int state = node.getState();
			return NodeUtil.transStateToWorkStatus(state);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NodeDefinition.NODE_WORKSTATUS_GREY;
	}

	public void changeState(Node node, int state) {
		String[] route = NodeUtil.getRoute(node);
		String workStatus = NodeUtil.transStateToWorkStatus(state);
		try {
			NodeUtil.getCommandDispatcher().sendCommand(route,
					MessageDefinition.CMD_NODE_SET_WORKSTATUS, workStatus);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* modify by yangxuanjia at 2011-01-11 start */
	/**
	 * 
	 * @标题:手动更新节点, 先删除node以下的所有数据(包括dataflow,component,segment),在更新node和其他.
	 * @作者:yangxuanjia
	 * @创建时间: 2011-1-12
	 * @参数:  
	 * @返回值:void
	 */
	public void updateNode(NodeMgrFacade facade,NodeConfiguration configuration, String nodeId) throws Exception {
		String nodeType = configuration.getType();
		if (nodeType != null) {
			Node node = NodeUtil.toNode(configuration);
			List<FlowConfiguration> flowConfs = configuration.getFlows();
			Set<DataFlow> flows = new HashSet<DataFlow>();
			if (flowConfs != null) {
				for (FlowConfiguration flowConf : flowConfs) {
					DataFlow dataFlow = NodeUtil.toFlow(flowConf, configuration.getComponents());
					flows.add(dataFlow);
				}
			}
			node.setDataFlows(flows);
			try {
				facade.updateNodeConfig(nodeId, node);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("update node" + node.getNodeId()+ " is failed:" + e.getMessage());
			}
		}
	}
	/**
	 * @method: doReGetNodeConfiguration 
	 * 			当节点内部组件结构发生改变时,会向SOC发送重新获取节点配置请求.
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param: obj: NodeConfiguration类型,包含节点基本信息
	 * 		   route: 节点的路由路径
	 * 	       nodeMgrFacade: 节点模块service
	 * @return: null
     * @exception: Exception
	 */
	public Serializable doReGetNodeConfiguration(Serializable obj, String[] route,NodeMgrFacade nodeMgrFacade) throws Exception {
        log.debug("com.topsec.tsm.sim.node.listener.DoCommand.doReGetNodeConfiguration() start!");
        if(obj==null||nodeMgrFacade==null||!(obj instanceof NodeConfiguration)){
        	log.error("Invalid Argument!");
        	throw new RuntimeException("Invalid Argument!") ;
        }
		NodeConfiguration nodeConfiguration=(NodeConfiguration)obj;
		String nodeId=nodeConfiguration.getId();
		try {
			updateNode(nodeMgrFacade, nodeConfiguration, nodeId);
		} catch (Exception e) {
			log.error("DoCommand.getInstance().updateNode() has Exception!"+ e.getMessage());
		}
		try {
			updateSimDataSource(nodeMgrFacade,nodeId);
		} catch (Exception e) {
			log.error("DoCommand.getInstance().updateSimDataSource() has Exception!"+ e.getMessage());
		}
		log.debug("com.topsec.tsm.sim.node.listener.DoCommand.doReGetNodeConfiguration() end!");
		return null;
	}
	public void updateSimDataSource(NodeMgrFacade nodeMgrFacade,String nodeId){
		//rewrite code
	}
}
