package com.topsec.tsm.sim.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.xml.XmlAccessException;
import com.topsec.tsm.base.xml.XmlElementFormatable;
import com.topsec.tsm.base.xml.XmlSerializable;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.node.ComponentConfiguration;
import com.topsec.tsm.node.ComponentConfigurationImpl;
import com.topsec.tsm.node.ComponentStepEntry;
import com.topsec.tsm.node.FlowConfiguration;
import com.topsec.tsm.node.FlowConfigurationFormater;
import com.topsec.tsm.node.FlowConfigurationImpl;
import com.topsec.tsm.node.NodeConfiguration;
import com.topsec.tsm.node.NodeConfigurationImpl;
import com.topsec.tsm.node.SegmentConfiguration;
import com.topsec.tsm.node.SegmentConfigurationImpl;
import com.topsec.tsm.node.comm.CommandDispatcher;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.CollectorType;
import com.topsec.tsm.sim.node.util.CollectorTypeUtil;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.node.ComponentFinder;
import com.topsec.tsm.sim.resource.node.IterateElement;
import com.topsec.tsm.sim.resource.node.SegmentFinder;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.DataFlow;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.resource.persistence.Resource;
import com.topsec.tsm.sim.resource.persistence.Segment;
import com.topsec.tsm.util.xml.DefaultDocumentFormater;

public class NodeUtil {

	private static void setDefault(Resource resource) {
		resource.setCreateTime(new Date());
		resource.setLastModifyedTime(new Date());
		if (resource.getOwner() == null) {
			resource.setOwner("admin");
		}
		if (resource.getCreater() == null) {
			resource.setCreater("admin");
		}
		if (resource.getLastModifyed() == null) {
			resource.setLastModifyed("admin");
		}
		if (resource.getAlias() == null) {
			resource.setAlias(resource.getResourceName());
		}
	}

	public static NodeConfigurationImpl toNodeConfiguration(Node node) {
		NodeConfigurationImpl configuration = new NodeConfigurationImpl();
		// 设置基本信息
		configuration.setId(node.getNodeId());
		configuration.setName(node.getResourceName());
		configuration.setType(node.getType());
		if (node.getVersion() != null) {
			configuration.setVersion(node.getVersion());
		}

		// 设置IP
		if (node.getIp() != null) {
			configuration.setIp(new IpAddress(node.getIp()));
		}

		Set<Component> coms = new HashSet<Component>();

		// 设置DataFlow
		List<FlowConfiguration> flowConfigurations = new ArrayList<FlowConfiguration>();
		Set<DataFlow> dataflows = node.getDataFlows();
		if (dataflows != null) {
			for (DataFlow dataflow : dataflows) {
				coms.addAll(dataflow.getComponents());
				FlowConfiguration flowConfiguration = toFlowConfiguration(dataflow);
				if (flowConfiguration != null) {
					flowConfigurations.add(flowConfiguration);
				}
			}
		}
		configuration.setFlows(flowConfigurations);

		// 设置组件以及相关块配置
		Map<String, ComponentConfiguration> components = new HashMap<String, ComponentConfiguration>();
		for (Component com : coms) {
			ComponentConfiguration componentConfiguration = toComponentConfiguration(com);
			if (componentConfiguration != null) {
				components.put(com.getComponentId(), componentConfiguration);
			}
		}
		configuration.setComponents(components);

		// 设置系统相关配置信息
		List<SegmentConfiguration> segmentconfigs = new ArrayList<SegmentConfiguration>();
		Set<Segment> segments = node.getSegments();
		if (segments != null) {
			for (Segment segment : segments) {
				try {
					SegmentConfiguration segmentconfig = toSegmentConfiguration(segment);
					if (segmentconfig != null) {
						segmentconfigs.add(segmentconfig);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		configuration.setSegments(segmentconfigs);

		return configuration;
	}

	public static ComponentConfigurationImpl toComponentConfiguration(Component com) {
		ComponentConfigurationImpl configuration = new ComponentConfigurationImpl();
		toComponentKnown(configuration, com);

		List<SegmentConfiguration> segmentconfigs = new ArrayList<SegmentConfiguration>();

		Set<Segment> segments = com.getSegments();
		if (segments != null) {
			for (Segment segment : segments) {
				try {
					SegmentConfiguration segmentconfig = toSegmentConfiguration(segment);
					if (segmentconfig != null) {
						segmentconfigs.add(segmentconfig);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		configuration.setSegments(segmentconfigs);

		return configuration;
	}

	public static SegmentConfiguration toSegmentConfiguration(Segment segment) throws Exception {
		if (segment.getType() != null) {
			String type = segment.getType();
			Object object = Class.forName(type).newInstance();
			if (object instanceof XmlSerializable) {
				XmlSerializable formater = (XmlSerializable) object;
				DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
				documentFormater.setFormater(formater);
				try {
					SegmentConfigurationImpl sg = new SegmentConfigurationImpl();
					String config = blobToString(segment.getCurrentConfig());
					if (config != null && !config.equals("")) {
						documentFormater.importObjectFromString(config);
						sg.setName(segment.getResourceName());
						sg.setConfiguration(formater);
					}
					return sg;

				} catch (XmlAccessException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		return null;
	}
	public static SegmentConfiguration toNodeSegmentConfiguration(Segment segment) throws Exception {
		if (segment.getType() != null) {
			String type = segment.getType();
			Object object = Class.forName(type).newInstance();
			if (object instanceof XmlSerializable) {
				XmlSerializable formater = (XmlSerializable) object;
				DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
				documentFormater.setFormater(formater);
				try {
					SegmentConfigurationImpl sg = new SegmentConfigurationImpl();
					String config = blobToString(segment.getCurrentConfig());
					if (config != null && !config.equals("")) {
						documentFormater.importObjectFromString(config);
						sg.setName(segment.getResourceName());
						sg.setConfiguration(formater);
					}
					return sg;

				} catch (XmlAccessException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		return null;
	}
	public static void toComponentKnown(ComponentConfigurationImpl configuration, Component com) {
		configuration.setId(com.getComponentId());
		configuration.setName(com.getResourceName());
		configuration.setType(com.getType());
		configuration.setVersion(com.getVersion());
		configuration.setRemoteControlable(com.isRemoteControlable());
		configuration.setRemoteVisible(com.isRemoteVisible());
	}

	public static FlowConfiguration toFlowConfiguration(DataFlow dataflow) {
		FlowConfigurationFormater formater = new FlowConfigurationFormater();
		DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
		documentFormater.setFormater(formater);
		try {
			documentFormater.importObjectFromString(blobToString(dataflow.getConfig()));
			return formater.getConfiguration();
		} catch (XmlAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String blobToString(Blob blob) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (blob != null) {
				InputStream in = blob.getBinaryStream();
				byte[] buff = new byte[1024];
				for (int i = 0; (i = in.read(buff)) > 0;) {
					baos.write(buff, 0, i);
				}
				in.close();
			}
			return baos.toString();
		} finally {
			baos.close();
		}
	}
	
	public static Blob createBlob(byte[] bytes){
		try {
			return new SerialBlob(bytes) ;
		} catch (SerialException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null ;
	}
	
	public static Node toNode(NodeConfiguration configuration) {
		Node node = new Node();
		node.setNodeId(configuration.getId());
		node.setType(configuration.getType());
		if (configuration.getIp() != null) {
			node.setIp(configuration.getIp().toString());
		}
		node.setVersion(configuration.getVersion());
		node.setResourceType(Constant.NODE_RESOURCE_TYPE);
		node.setResourceName(configuration.getName());
		node.setSegments(createSegments(configuration.getSegments()));

		return node;
	}

	public static DataFlow toFlow(FlowConfiguration configuration,Map<String, ComponentConfiguration> components) {
		DataFlow flow = new DataFlow();
		flow.setResourceName(configuration.getName());
		flow.setResourceType(Constant.FLOW_RESOURCE_TYPE);
		FlowConfigurationFormater formater = new FlowConfigurationFormater();
		DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
		documentFormater.setFormater(formater);
		try {
			formater.setConfiguration((FlowConfigurationImpl) configuration);
			String xml = documentFormater.exportObjectToString();
			Blob blob = createBlob(xml.getBytes());
			flow.setConfig(blob);
		} catch (Exception e) {
			e.printStackTrace();
		}

		setDefault(flow);
		Set<Component> coms = new HashSet<Component>();
		List<ComponentStepEntry> entrys = configuration.getNextComponentSteps();
		createComponents(coms, entrys, components);
		flow.setComponents(coms);
		return flow;
	}

	private static void createComponents(Set<Component> coms,
			List<ComponentStepEntry> entrys,
			Map<String, ComponentConfiguration> components) {
		for (ComponentStepEntry entry : entrys) {
			Component com = new Component();
			ComponentConfiguration componentConfiguration = (ComponentConfiguration) components.get(entry.getComponentId());

			com.setResourceName(componentConfiguration.getName());
			com.setComponentId(componentConfiguration.getId());
			if (componentConfiguration.getVersion() != null) {
				com.setVersion(componentConfiguration.getVersion());
			}
			com.setType(componentConfiguration.getType());
			com.setResourceType(Constant.COMPONENT_RESOURCE_TYPE);

			com.setRemoteControlable(componentConfiguration.isRemoteControlable());
			com.setRemoteVisible(componentConfiguration.isRemoteVisible());

			setDefault(com);

			List<SegmentConfiguration> segmentConfs = componentConfiguration.getSegments();

			com.setSegments(createSegments(segmentConfs));

			if (entry.getNextComponentSteps() != null) {
				createComponents(coms, entry.getNextComponentSteps(),components);
			}
			coms.add(com);
		}
	}

	public static Set<Segment> createSegments(
			List<SegmentConfiguration> segmentConfs) {
		Set<Segment> segments = new HashSet<Segment>();
		if (segmentConfs != null) {
			for (SegmentConfiguration segmentConf : segmentConfs) {
				segments.add(createSegment(segmentConf));
			}
		}

		return segments;
	}

	public static Segment createSegment(SegmentConfiguration segmentConf) {
		Segment segment = new Segment();
		segment.setResourceName(segmentConf.getName());
		segment.setResourceType(Constant.SEGMENT_RESOURCE_TYPE);
		Serializable conf = segmentConf.getConfiguration();
		setDefault(segment);
		if (conf instanceof XmlElementFormatable) {
			DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
			documentFormater.setFormater((XmlElementFormatable) conf);
			try {
				String xml = documentFormater.exportObjectToString();
				Blob blob = createBlob(xml.getBytes());
				segment.setCurrentConfig(blob);
				segment.setLastConfig(blob);
			} catch (Exception e) {
				e.printStackTrace();
			}
			segment.setType(conf.getClass().getName());
		}
		return segment;
	}
	/**
	 * 从节点中查找第一个指定组件类型的组件
	 * @param node　节点
	 * @param componentType　组件类型
	 * @return
	 */
	public static Component findFirstComponent(IterateElement element,String componentType){
		ComponentFinder finder = new ComponentFinder(componentType, true) ;
		element.iterate(finder) ;
		return finder.getComponent() ;
	}
	/**
	 * 从节点中查找所有指定组件类型的组件
	 * @param node　节点
	 * @param componentType　组件类型
	 * @return
	 */
	public static List<Component> findComponents(IterateElement element,String componentType){
		ComponentFinder finder = new ComponentFinder(componentType, false) ;
		element.iterate(finder) ;
		return finder.getCompoments();
	}
	/**
	 * 从节点中查找第一个指定组件类型指定segment类型的segment
	 * @param node　节点
	 * @param segmentType　segment类型
	 * @return
	 */
	public static Segment findFirstSegment(IterateElement element,String componentType,String segmentType){
		SegmentFinder finder = new SegmentFinder(componentType, segmentType, true) ;
		element.iterate(finder) ;
		return finder.getSegment() ;
	}
	/**
	 * 从节点中查找所有指定组件类型指定segment类型的segment
	 * @param node　节点
	 * @param segmentType　segment类型
	 * @return
	 */
	public static List<Segment> findSegments(IterateElement element,String componentType,String segmentType){
		SegmentFinder finder = new SegmentFinder(componentType, segmentType, true) ;
		element.iterate(finder) ;
		return finder.getSegments() ;
	}
	public static Segment findFirstSegment(IterateElement element,String componentType,Class<?> segmentClass){
		return findFirstSegment(element, componentType, segmentClass.getName()) ;
	}
	public static List<Segment> findSegments(IterateElement element,String componentType,Class<?> segmentClass){
		return findSegments(element, componentType, segmentClass.getName()) ;
	}
	
	public static Segment findNodeSegment(Node node,Class<?> segmentClass){
		return findNodeSegment(node, segmentClass.getName()) ;
	}
	public static Segment findNodeSegment(Node node,String className){
		Set<Segment> segments = node.getSegments() ;
		for (Segment segment : segments) {
			if (className.equals(segment.getType())) {
				return segment ;
			}
		}
		return null ;
	}
	
	public static XmlSerializable findNodeSegmentConfig(Node node,Class<?> segmentClass){
		return findNodeSegmentConfig(node, segmentClass.getName()) ;
	}
	
	public static XmlSerializable findNodeSegmentConfig(Node node,String className){
		Segment segment = findNodeSegment(node, className) ;
		if (segment != null) {
			try {
				SegmentConfiguration config = toSegmentConfiguration(segment) ;
				if(config!=null){
					return config.getConfiguration() ;
				}
				return null ;
			} catch (Exception e) {
				return null ;
			}
		}
		return null ;
	}
	/**
	 * 从节点中查找指定componentType下的指定segmentType中的配置信息
	 * @return
	 */
	public static XmlSerializable findFirstSegmentConfig(IterateElement element,String componentType,String segmentType){
		Segment segment = findFirstSegment(element, componentType, segmentType) ;
		if(segment!=null){
			try {
				SegmentConfiguration config = toSegmentConfiguration(segment) ;
				if(config!=null){
					return config.getConfiguration() ;
				}
				return null ;
			} catch (Exception e) {
				return null ;
			}
		}
		return null ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T findFirstSegmentConfig(IterateElement element,String componentType,Class<T> segmentClass){
		return (T) findFirstSegmentConfig(element, componentType, segmentClass.getName()) ;
	}

	public static boolean isAction(String nodeType) {
		return NodeDefinition.NODE_TYPE_ACTION.equals(nodeType);
	}

	public static boolean isFlexer(String nodeType) {
		return NodeDefinition.NODE_TYPE_FLEXER.equals(nodeType);
	}	
	/**
	 * 判断一个节点是否是auditor节点
	 * @return
	 */
	public static boolean isAuditor(String nodeType){
		return NodeDefinition.NODE_TYPE_AUDIT.equals(nodeType) ;
	}
	/**
	 * 判断一个节点是否是agent节点
	 * @param nodeType
	 * @return
	 */
	public static boolean isAgent(String nodeType){
		return NodeDefinition.NODE_TYPE_AGENT.equals(nodeType) ;
	}
	
	/**
	 * 判断一个节点是否是service节点,ReportService,IndexService,QueryService
	 * @param nodeType
	 * @return
	 */
	public static boolean isService(String nodeType){
		return isQueryService(nodeType) || isReportService(nodeType) || isIndexService(nodeType) ;
	}
	
	/**
	 * 判断一个节点是否是service节点
	 * @return
	 */
	public static boolean isQueryService(String nodeType){
		return NodeDefinition.NODE_TYPE_QUERYSERVICE.equals(nodeType) ;
	}
	/**
	 * 判断一个节点是否是index节点
	 * @return
	 */
	public static boolean isIndexService(String nodeType){
		return NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(nodeType) ;
	}
	/**
	 * 判断一个节点是否是collector节点
	 * @return
	 */
	public static boolean isCollector(String nodeType){
		return NodeDefinition.NODE_TYPE_COLLECTOR.equals(nodeType);
	}
	/**
	 * 判断一个节点是否是ReportService节点
	 * @return
	 */
	public static boolean isReportService(String nodeType){
		return NodeDefinition.NODE_TYPE_REPORTSERVICE.equals(nodeType) ;
	}
	/**
	 * 判断一个节点是否是smp节点
	 * @param nodeTypeOrId 节点类型或者节点id
	 * @return
	 */
	public static boolean isSMP(String nodeTypeOrId){
		return NodeDefinition.NODE_TYPE_SMP.equals(nodeTypeOrId) ;
	}
	public static String[] getRoute(Node node) {
		String url = node.getRouteUrl();
		String[] routes = url.split("/");
		String[] route = new String[routes.length - 1];
		for (int i = 1; i < routes.length; i++) {
			route[i - 1] = routes[i];
		}
		return route;
	}

	public static String[] getAuditorRouteByAgentNode(Node node) {
		String url = node.getRouteUrl();
		String[] routes = url.split("/");
		String[] route = new String[routes.length - 2];
		for (int i = 1; i < routes.length - 1; i++) {
			route[i - 1] = routes[i];
		}
		return route;
	}
	/**
	 * 根据节点路由，获得auditor节点的nodeId
	 * 如果当前节点是auditor节点直接返回最后一级节点id
	 * 如果当前节点是service或agent节点返回倒数第二级节点
	 * @param node
	 * @return
	 */
	public static String getAuditorNodeIdByRoute(Node node){
		String nodeType = node.getType() ; 
		if(isSMP(nodeType)){
			throw new RuntimeException("Can't get auditor id from SMP") ;
		}
		String[] route = StringUtil.split(node.getRouteUrl(),"/") ;
		if(isAgent(nodeType)||isService(nodeType)||isCollector(nodeType)){
			return route[route.length-2] ;
		}
		if(isAuditor(nodeType)){
			return route[route.length-1] ; 
		}
		throw new RuntimeException("Unknow node type") ;
	}
	
	public static String transStateToWorkStatus(int state) {
		if (state == Constant.NODE_STATE_BLACK) {
			return NodeDefinition.NODE_WORKSTATUS_BLACK;
		} else if (state == Constant.NODE_STATE_WRITE) {
			return NodeDefinition.NODE_WORKSTATUS_WHITE;
		} else {
			return NodeDefinition.NODE_WORKSTATUS_GREY;
		}
	}

	public static CommandDispatcher getCommandDispatcher()throws CommunicationException {
		CommandDispatcher commandChannel = ChannelGate.getCommandChannel(ChannelConstants.COMM_BASE_COMMAND_CHANNEL);
		return commandChannel;
	}

	public static <T> T dispatchCommand(String[] routes, String command,Serializable object, long expiration)throws CommunicationException{
		return (T)getCommandDispatcher().dispatchCommand(routes, command, object, expiration) ;
	}
	
	public static void sendCommand(String[] routes, String command,Serializable object)throws CommunicationException{
		getCommandDispatcher().sendCommand(routes, command, object) ;
	}
	
	public static void sendCommand(String[] routes, String command,Serializable object, long expiration)throws CommunicationException{
		getCommandDispatcher().sendCommand(routes, command, object,expiration) ;
	}
	/**
	 * 合并节点信息
	 * @param dbNode
	 * @param node
	 */
	public static void mergeNode(Node dbNode, Node node) {
		dbNode.setNodeId(node.getNodeId());
		dbNode.setType(node.getType());
		dbNode.setDomainId(node.getDomainId());
		dbNode.setIp(node.getIp());
		dbNode.setVersion(node.getVersion());
		dbNode.setResourceType(Constant.NODE_RESOURCE_TYPE);
		dbNode.setResourceName(node.getResourceName());
		dbNode.getSegments().clear();
		dbNode.getSegments().addAll(node.getSegments());
		dbNode.getDataFlows().clear();
		dbNode.getDataFlows().addAll(node.getDataFlows());
		node.setRouteUrl(dbNode.getRouteUrl());
	}

	/* modify by yangxuanjia at 2011-01-11 end */

	/**
	 * 获取节点显示名称，nodeDisplayName与nodeName不同,当前台页面使用Select(html),ComboBox(flex)时,<br>
	 * nodeName作为文本框的显示名称<br/>
	 * nodeDisplayName作为下拉列表中的显示名称
	 * 
	 * @param node
	 * @return
	 */
	public static String getNodeDisplayName(String route, String nodeName) {
		String[] routes = StringUtil.split(route, "/");
		StringBuffer nodeDisplayName = new StringBuffer();
		// 从第三级开始，第一级为SMP节点，第二级为根节点
		for (int i = 2; i < routes.length; i++) {
			nodeDisplayName.append("-->");
		}
		nodeDisplayName.append(nodeName);
		return nodeDisplayName.toString();
	}
	/**
	 * 将配置文件转换为blob对象
	 * @param t
	 * @return
	 * @throws XmlAccessException
	 */
	public static Blob convertConfigToBlob(XmlElementFormatable t) throws XmlAccessException{
		try {
			DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
			documentFormater.setFormater(t);
			Blob blob = createBlob(documentFormater.exportObjectToString().getBytes());
			return blob ;
		} catch (XmlAccessException e) {
			throw e ;
		}
	}
	/**
	 * 将blob对象转换为配置文件
	 * @param blob
	 * @param configClass
	 * @return
	 */
	public static <T> T convertBlobToConfig(Blob blob,Class<T> configClass){
		return null ;
	}
	/**
	 * 获取节点列表中具有指定的收集方式的节点组件信息<br/>
	 * @param nodeList
	 * @param collectMethod
	 * @return 返回集合中每一个Map对象包含nodeId,auditorNodeId,componentId
	 */
	public static List<Map<String,Object>> getComponentsByCollectMethod(List<Node> nodeList,String collectMethod){
		List<Map<String,Object>> availableNodes = new ArrayList<Map<String,Object>>() ;
		CollectorType collectorType = CollectorTypeUtil.getInstance().getCollectorType(collectMethod);
		if(collectorType==null){
			return availableNodes ;
		}
		String componentType = collectorType.getComponenttype() ;
		for (Node node : nodeList) {
			Set<DataFlow> flows = node.getDataFlows();
			if (node.getParent() == null||ObjectUtils.isEmpty(flows)) {
				continue ;
			}
			boolean nodeAvailable = false;
			for (DataFlow flow : flows) {
				Set<Component> coms = flow.getComponents();
				if (ObjectUtils.isEmpty(coms)) {
					continue ;
				}
				for (Component com : coms) {
					if (componentType.equals(com.getType())) {
						availableNodes.add(createComponentMap(node, com)) ;
						nodeAvailable = true ;
						break;
					}
				}
				if (nodeAvailable) {
					break;
				}
			}
		}
		return availableNodes;
	}
	
	public static Node getCollectNode(String managerNodeId,NodeMgrFacade nodeMgr){
		Node manageNode = nodeMgr.getNodeByNodeId(managerNodeId,false,true,false,false,false) ;
		Node node = null ;
		if(NodeUtil.isAuditor(manageNode.getType())){
			for(Node child:manageNode.getChildren()){
				if(NodeUtil.isCollector(child.getType())){
					node = nodeMgr.getNodeByNodeId(child.getNodeId(), false, false, true,false,true) ;
					break ;
				}
			}
		}else{
			node = nodeMgr.getNodeByNodeId(managerNodeId, false, false, true, false, true) ;
		}
		return node ;
	}
	
	/**
	 * 获取指定节点具有指定的收集方式的节点组件信息<br/>
	 * @param node
	 * @param collectMethod
	 * @return 返回一个Map对象包含nodeId,auditorNodeId,componentId
	 */
	public static Map<String,Object> getComponentByCollectMethod(Node node,String collectMethod){
		CollectorType collectorType = CollectorTypeUtil.getInstance().getCollectorType(collectMethod);
		if(collectorType==null){
			return null ;
		}
		String componentType = collectorType.getComponenttype() ;
		Set<DataFlow> flows = node.getDataFlows();
		if (ObjectUtils.isEmpty(flows)) {
			return null ;
		}
		for (DataFlow flow : flows) {
			Set<Component> coms = flow.getComponents();
			if (ObjectUtils.isEmpty(coms)) {
				continue ;
			}
			for (Component com : coms) {
				if (componentType.equals(com.getType())) {
					return createComponentMap(node, com) ;
				}
			}
		}
		return Collections.emptyMap();
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	private static Map<String,Object> createComponentMap(Node node,Component com){
		Map<String,Object> monitorInfo = new HashMap<String,Object>() ;
		monitorInfo.put("nodeId", node.getNodeId()) ;
		monitorInfo.put("auditorNodeId", node.getParent().getNodeId()) ;
		monitorInfo.put("componentId", com.getResourceId()) ;
		monitorInfo.put("online", NodeStatusQueueCache.online(node.getNodeId())) ;
		String nodeName = node.getResourceName() != null ? node.getResourceName()+"("+node.getIp()+")" : node.getIp() ;
		monitorInfo.put("nodeName", nodeName ) ;
		return monitorInfo ;
	}	

	public static Node getNodeByType(NodeMgrFacade nodeMgrFacade,String type){
		List<Node> nodes = nodeMgrFacade.getNodesByType(type, false, false, false, false) ;
		if(ObjectUtils.isEmpty(nodes)){
			throw new CommonUserException(type+"节点还未注册！") ;
		}
		return nodes.get(0) ;
	}
	
	public static Node getChildByType(Node auditor,String type){
		for(Node child:auditor.getChildren()){
			if(child.getType().equals(type)){
				return child ;
			}
		}
		return null ;
	}

	public static String getChildNodeIdByType(Node auditor,String type){
		Node node = getChildByType(auditor, type) ;
		if(node == null){
			return null ;
		}
		return node.getNodeId() ;
	}
	
}
