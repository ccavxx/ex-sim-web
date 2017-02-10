package com.topsec.tsm.sim.node.service;

import java.lang.reflect.Constructor;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.util.RuntimeExceptionWrapper;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.base.xml.AbstractElementFormater;
import com.topsec.tsm.base.xml.XmlAccessException;
import com.topsec.tsm.base.xml.XmlSerializable;
import com.topsec.tsm.collector.datasource.DataSource;
import com.topsec.tsm.collector.datasource.foramter.DataSourcesElementFormater;
import com.topsec.tsm.common.DoCommand;
import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.node.SegmentConfigurationImpl;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.dao.DataSourceDao;
import com.topsec.tsm.sim.common.exception.DataAccessException;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.dao.NodeMgrDao;
import com.topsec.tsm.sim.node.dao.ResourceDao;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.node.SegmentFinder;
import com.topsec.tsm.sim.resource.object.ResourceType;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.ConfigPolicy;
import com.topsec.tsm.sim.resource.persistence.DataFlow;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.resource.persistence.Segment;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.Constant;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.TalVersionUtil;

public class NodeMgrFacadeImpl implements NodeMgrFacade {
	
	/**此map为了放在节点重复注册*/
	public static Map<String,String> nodeIdMap=new ConcurrentHashMap<String, String>();

	public static boolean isNodesChanged;
	
	private NodeMgrDao dao;
	private DataSourceDao dataSourceDao;
	private ResourceDao resourceDao;
	private SystemConfigService systemConfigService;
	
	private static final Logger log = LoggerFactory.getLogger(NodeMgrFacadeImpl.class);
	
	/**
	 * 
	 */
	public Node getNodeById(Long id){
		return dao.findById(id) ;
	}
	/**
	 * @标题:根据节点的唯一标识(nodeId)获取node
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 6:06:57 PM
	 * @参数:nodeId 节点对象的唯一标识
	 * @返回值:Node
	 */
	public Node getNodeByNodeId(String nodeId) throws DataAccessException {
		if(nodeId == null || StringUtil.isBlank(nodeId)){
			return null;
		}
		Node result = this.dao.getNodeByNodeId(nodeId);
		return result;
	}
	
	/**
	 * @标题:注册一个节点
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 6:06:57 PM
	 * @参数:node 节点对象,route 路由
	 * @返回值:boolean
	 */
	public synchronized boolean registerNode(Node node, String[] route) throws DataAccessException,NodeException {
		Node cnode = dao.getNodeByNodeId(node.getNodeId());
		// 首先判断该node是否已经注册过
		if (cnode == null) {// 没有注册过
			String nodeIdMapString=nodeIdMap.get(node.getNodeId());
			if(nodeIdMapString!=null){
				log.warn("node is registing or node has bean registed!!!");
				return false;
			}
			
			Node parent = null;
			Node nd =null;
			if (route.length > 1) {
				String parentNodeId = route[1];
				nd = this.getNodeByNodeId(parentNodeId);
				if (!NodeUtil.isSMP(parentNodeId)&& nd == null) {
					log.error("com.topsec.tsm.sim.node.service.NodeMgrFacadeImpl.registerNode(), parentNodeId is not registed!");
					throw new NodeException("父节点'" + parentNodeId + "'还没有注册");
				}
				if (nd == null) {
					parent = this.getRootNode(NodeDefinition.NODE_TYPE_SMP);
				} else {
					parent = nd;
				}
				node.setParent(parent);
			}
			// Agent不能直接注册到SOC端，而必须注册某个综合审计下面
			if (NodeUtil.isAgent(node.getType())) {
				if (parent != null&& NodeUtil.isSMP(parent.getType())) {
					log.error("com.topsec.tsm.sim.node.service.NodeMgrFacadeImpl.registerNode(), The agent's parent can not be SOC!");
					throw new NodeException("The agent's parent can not be SOC");
				}
			}
			node.setState(Constant.NODE_STATE_WRITE);
			
			node.setRouteUrl(node.getParent().getRouteUrl() + "/"+ node.getNodeId());
			node.setAlias(node.getIp());
			node.setCreateTime(new Date()) ;
			try {
				if(!NodeUtil.isAgent(node.getType())){
					 if(node.getIp().equalsIgnoreCase(IpAddress.getLocalIp().toString())){
						 node.setIp(IpAddress.getLocalIp().getLocalhostAddress());
						 node.setAlias(IpAddress.getLocalIp().getLocalhostAddress());
					 }
					
				}
				dao.save(node);
	    		toLog(AuditCategoryDefinition.SYS_UPDATE, "注册节点", "注册节点,名称:"+node.getAlias()+",IP:"+node.getIp()
						+",类型:"+node.getType()+",版本:"+node.getVersion(),"LocalSystem", true,Severity.HIGH);
				
			} catch (Exception e) {
				 log.error(e.getMessage());
		    		toLog(AuditCategoryDefinition.SYS_UPDATE, "注册节点", "注册节点,名称:"+node.getAlias()+",IP:"+node.getIp()
							+",类型:"+node.getType()+",版本:"+node.getVersion()+e.getMessage(),"LocalSystem", false,Severity.HIGH);
				 throw new NodeException("node regist failure!!!");
			}
			nodeIdMap.put(node.getNodeId(), node.getNodeId());
			
			iteratorNode(node,true,true,true,true);
			//添加自审计日志源
			try {
				if(node.getType().equals(NodeDefinition.NODE_TYPE_AUDIT)){
					//注册系统日志源
					addSystemDataSource(node ,LogKeyInfo.LOG_SYSTEM_RUN_TYPE, "log");
					addSystemDataSource(node ,LogKeyInfo.LOG_SYSTEM_TYPE, "system");
					//systemConfigService.configSMP(node.getParent().getNodeId());
					systemConfigService.configAuditor(node,this);
				}else if(node.getType().equals(NodeDefinition.NODE_TYPE_INDEXSERVICE)){
					systemConfigService.configService(node,this);
				}else if(node.getType().equals(NodeDefinition.NODE_TYPE_REPORTSERVICE)){
					 systemConfigService.configReportService(node,this);
				}else if(node.getType().equals(NodeDefinition.NODE_TYPE_COLLECTOR)){
					systemConfigService.configCollector(node,this);
				}
			} catch (Exception e) {
				throw new NodeException(e) ;
			}
			//注册节点返回true
			return true;
			/* modify by yangxuanjia at 2011-07-10 end */
		} else {
			cnode.setResourceName(node.getResourceName());
			if(!NodeUtil.isAgent(node.getType())){
				 if(node.getIp().equalsIgnoreCase(IpAddress.getLocalIp().toString())){
					 node.setIp(IpAddress.getLocalIp().getLocalhostAddress());
					 node.setAlias(IpAddress.getLocalIp().getLocalhostAddress());
				 }
			}
			cnode.setIp(node.getIp());
			cnode.setVersion(node.getVersion());// 设置版本
			if (node.getDomainId() != null) {
				cnode.setDomainId(node.getDomainId());
			}
			node.setRouteUrl(cnode.getRouteUrl());
			dao.update(cnode);
			//更新节点返回false
			return false;
		}
	}

	/**
	 * 
	 * @throws DataAccessException
	 * @标题:获取子节点
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 7:56:27 PM
	 * @参数:resourceId 资源id
	 * @参数:recursive 是否递归获取所有的节点 true-->递归获取,false-->只获取下级
	 * @返回值:List
	 */
	public List<Node> getSubNodesByResourceId(long resourceId, boolean recursive)throws DataAccessException {
		List<Node> result = new ArrayList<Node>();
		Node node = this.dao.findById(resourceId);
		if (!recursive) {// 直接获取下级子节点
			if (node.getChildren() != null){
				result.addAll(node.getChildren());
			}
		} else {// 递归获取所有的子节点
			result = this.dao.getSubNodesByRrouteUrl(node.getRouteUrl());
		}
		return result;
	}

	/**
	 * 
	 * @标题:根据节点的url获取所有的子节点
	 * @作者:ysf
	 * @创建时间:Dec 7, 2010 3:47:22 PM
	 * @参数:
	 * @返回值:List<Node>
	 */
	public List<Node> getSubNodesByRrouteUrl(String routeUrl) throws DataAccessException {
		return this.dao.getSubNodesByRrouteUrl(routeUrl);
	}

	/**
	 * @标题:删除一个节点
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 6:06:57 PM
	 * @参数:node 节点对象
	 * @返回值:void
	 */
	public void delNode(Node node) throws DataAccessException {
		this.dao.delete(node);
		nodeIdMap.remove(node.getNodeId());
	}

	/**
	 * @标题:删除一个节点
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 6:06:57 PM
	 * @参数:node 节点对象
	 * @返回值:void
	 */
	public void delNode(long resourceId) throws DataAccessException {
		Node node = (Node) this.dao.findById(resourceId);
		this.delNode(node);
		nodeIdMap.remove(node.getNodeId());
	}


	/**
	 * 
	 * @标题:
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 8:44:15 PM
	 * @参数:resourceIds-->id集合，state-->要改变的状态
	 * @返回值:void
	 */
	public void changeState(long[] resourceIds, int state) throws DataAccessException {
		if (state < 0 || state > 2)
			throw new RuntimeException("无效的节点状态");
		for (long resourceId : resourceIds) {
			Node node = (Node) this.dao.findById(resourceId);
			if (node != null) {
				node.setState(state);
				this.dao.update(node);
				DoCommand.getInstance().changeState(node, state);
			}
		}
	}

	/**
	 * 
	 * @标题:查询符合条件的名单,并且分页
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 8:52:36 PM
	 * @参数:pageSize-->每页大小,pageNo-->当前页码,types-->节点类型集合,states-->名单状态集合
	 * @返回值:Map
	 */
	public PageBean<Node> queryNodes(int pageSize, int pageNo, String[] types, int[] states)throws DataAccessException {
		return this.dao.queryNodes(pageSize, pageNo,types, states) ;
	}

	/**
	 * 
	 * @标题:获取根节点，默认是当前服务器(SOC)
	 * @作者:ysf
	 * @创建时间:Aug 11, 2010 12:21:52 PM
	 * @参数:
	 * @返回值:Node
	 */
	public Node getRootNode(String nodeType) throws DataAccessException {
//		String serverIp = ServerUtil.getServerIp();
		String serverIp = IpAddress.getLocalIp().getLocalhostAddress() ;
		Node root = this.dao.getRootNode(nodeType, serverIp);
		if (root == null) {
			root = new Node();
			root.setNodeId(NodeDefinition.NODE_TYPE_SMP);
			root.setResourceName(NodeDefinition.NODE_TYPE_SMP);
			root.setRouteUrl(NodeDefinition.NODE_TYPE_SMP);
			root.setResourceType(Constant.NODE_RESOURCE_TYPE);
			root.setType(nodeType);
			root.setIp(serverIp);
			log.info("注册SMP节点") ;
			this.dao.save(root);
		}
		return root;
	}

	/**
	 * 
	 * @标题:更新一个节点
	 * @作者:ysf
	 * @创建时间:Aug 12, 2010 4:11:47 PM
	 * @参数:
	 * @返回值:Node
	 */
	public Node updateNode(Node node) throws DataAccessException {
		this.dao.update(node);
		return node;
	}

	/**
	 * 
	 * @标题:获取一个节点
	 * @作者:ysf
	 * @创建时间:Aug 12, 2010 4:12:42 PM
	 * @参数:
	 * @返回值:Node
	 */
	public Node getNodeWithPolicy(long resourceId) throws DataAccessException {
		Node node = this.dao.findById(resourceId);
		Set<ConfigPolicy> policys = node.getPolicys();
		if (policys != null) {
			for (ConfigPolicy policy : policys) {
				policy.getResourceName();
			}
		}
		return node;
	}

	public Node getNodeWithDataFlow(long resourceId)throws DataAccessException {
		Node node = this.dao.findById(resourceId);
		loadNodeDataFlow(node) ;
		return node;
	}

	public Node getNodeWithDataFlowByNodeId(String nodeId) throws DataAccessException {
		Node node = (Node) this.getNodeByNodeId(nodeId,true,false,true,true);
//		loadNodeDataFlow(node) ;
		return node;
	}

	private Node loadNodeDataFlow(Node node){
		Set<DataFlow> dataFlows = node.getDataFlows();
		if (dataFlows != null) {
			for (DataFlow dataFlow : dataFlows) {
				dataFlow.getResourceName();
				Set<Component> coms = dataFlow.getComponents();
				if (coms != null) {
					for (Component com : coms) {
						com.getResourceName();
						Set<Segment> segments = com.getSegments();
						if (segments != null) {
							for (Segment segment : segments) {
								segment.getResourceName();
							}
						}
					}
				}
			}
		}
		Set<Segment> segments = node.getSegments();
		if (segments != null) {
			for (Segment segment : segments) {
				segment.getResourceName();
			}
		}
		return node ;
	}

	public List<Node> getAllNodesWithComponents() throws DataAccessException {
		List<Node> nodes = this.dao.getNodesExcludeTypes(NodeDefinition.NODE_TYPE_LIVEUPDATE) ;
		for (Node node : nodes) {
			node.getChildren().size();
			if (node.getParent() != null) {
				node.getParent().getResourceName();
			}
			Set<DataFlow> flows = node.getDataFlows();
			for (DataFlow flow : flows) {
				Set<Component> coms = flow.getComponents();
				for (Component com : coms) {
					com.getResourceName();
				}
			}
		}

		return nodes;
	}

	public Node getNodeByComponentId(long componentId) throws DataAccessException {
		Node node = this.dao.getNodeByComponentId(componentId) ;
		return node ;
	}

	/**
	 * 
	 *@标题:更新Node接口配置,包括级联关系(先删除,再添加.)
	 *@参数: nodeId:数据库表里的nodeId node: 从综合审计端传过来的node
	 */
	public void updateNodeConfig(String nodeId, Node node) throws DataAccessException {
		this.dao.updateNodeConfig(nodeId, node);
		try {
			if(NodeUtil.isAuditor(node.getType())){
				systemConfigService.configAuditor(node, this);
			}else if(NodeUtil.isIndexService(node.getType())){
				systemConfigService.configService(node, this);
			}else if(NodeUtil.isReportService(node.getType())){
				systemConfigService.configReportService(node, this);
			}else if(NodeUtil.isCollector(node.getType())){
				systemConfigService.configCollector(node, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	/**
	 * @method: isDistributed 判断是否是多级.
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param:
	 * @return: boolean: true为多级,false为单级
	 * @exception: Exception
	 */
	@Override
	public boolean isDistributed() throws DataAccessException {
		return this.dao.isDistributed();
	}
	
	/**
	* @method: getDataSourceBindableNodes
	* 		 得到数据源可绑定的node节点
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  hasComponent:是否包含组件
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getDataSourceBindableNodes(boolean hasComponent) throws DataAccessException{
		List<Node> nodes=dao.getNodesByTypes(NodeDefinition.NODE_TYPE_AUDIT,NodeDefinition.NODE_TYPE_AGENT);
		if(nodes!=null){
			for (Node node : nodes) {
				if(hasComponent){
					iteratorNode(node, false, true, hasComponent, false);
				}
				if(NodeUtil.isAgent(node.getType())){
					node.getParent().getResourceName();
				}
			}
		}
		
		return nodes;
	}

	//dinggf
	//临时增加方法 ，确保自审计系统日志源能够创建，主要解决已经部署的系统问题
	//该方法不会被频繁调度
	public void ensureSystemDatasource() throws Exception{
		Node node = getKernelAuditor(true);
		if(node != null){
			addSystemDataSource(node ,LogKeyInfo.LOG_SYSTEM_RUN_TYPE, "log");
			addSystemDataSource(node ,LogKeyInfo.LOG_SYSTEM_TYPE, "system");
		}
	}

	/**
	* @method: getKernelAuditor 
	* 		  得到核心Auditor
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  hasComponent:是否包含组件
	* @return: Node:核心Auditor
	* @exception: Exception
	*/
	@Override
	public Node getKernelAuditor(boolean hasComponent)throws DataAccessException{
		Node node=this.dao.getKernelAuditor();
	    iteratorNode(node, false,true, hasComponent, true);
		return node;
	}
	
	/**
	* @method: getKernelAuditor 
	* 		  得到核心Auditor
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  hasNodeSegment:是否遍历node配置
	*  		   hasChildren:是否遍历node孩子
	*   	   hasComponent:是否遍历node的组件
	*    	   hasSegment:是否遍历node组件的配置
	* @return: Node:核心Auditor
	* @exception: Exception
	*/
	public Node getKernelAuditor(boolean hasNodeSegment,boolean hasChildren,boolean hasComponent,boolean hasSegment) throws DataAccessException{
		Node node=this.dao.getKernelAuditor();
	    iteratorNode(node, hasNodeSegment,hasChildren, hasComponent, hasSegment);
		return node;
	}
	

	/**
	* @method: getBindableComponentByType 
	* 		根据组件类型得到可绑定的的组件
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  node:节点
	* 		   type:类型
	* 		   hasSegments:是否需要segments
	* @return: Component:组件
	* @exception: Exception
	*/
	@Override
	public Component getBindableComponentByType(Node node,String type,boolean hasSegments) throws DataAccessException{
		if(node==null||type==null){
			return null;
		}
		Component component = NodeUtil.findFirstComponent(node, type) ;
		if(component == null){
			return null ;
		}
		if(hasSegments && component.getSegments() != null ){
			component.getSegments().iterator() ;//如果使用Hibernate延迟初始化component的segments调用此方法会主动初始化segments集合
		}
		return component ;
	}
	
	/**
	* @method: iteratorNode 
	* 		   遍历node
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  node:节点
	* 		   hasNodeSegment:是否遍历node配置
	*  		   hasChildren:是否遍历node孩子
	*   	   hasComponent:是否遍历node的组件
	*    	   hasSegment:是否遍历node组件的配置
	* @return: void
	* @exception: Exception
	*/
	private void iteratorNode(Node node,boolean hasNodeSegment,boolean hasChildren,boolean hasComponent,boolean hasSegment){
		if (node != null) {
			if(hasNodeSegment){
				Set<Segment> segments = node.getSegments();
				if (segments != null) {
					segments.iterator() ;
				}
			}
			
			if(hasChildren){
				Set<Node> childrenNodes=node.getChildren();
				if(childrenNodes!=null){
					childrenNodes.iterator() ;
				}
			}
			
			if(hasComponent){
				Set<DataFlow> dataFlows = node.getDataFlows();
				if (dataFlows != null) {
					for (DataFlow dataFlow : dataFlows) {
						Set<Component> coms = dataFlow.getComponents();
						if (coms != null) {
							for (Component com : coms) {
								if(hasSegment){
									Set<Segment> segments = com.getSegments();
									if (segments != null) {
										segments.iterator() ;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	* @method: getBuildState
	* 			读取conf目录下buildVersion.xml文件,得到上一次系统单级多级信息
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param: 
	* @return: buildVersion 上一次系统单级多级信息
	*/
	public synchronized boolean getBuildState() throws DataAccessException{
		Element readVersionFile = TalVersionUtil.getInstance().readVersionFile();
		String buildVersion = readVersionFile.elementTextTrim("buildState");
		boolean versionResult=false;
		if(buildVersion==null||buildVersion.equals("")){
			boolean version = this.isDistributed();
			TalVersionUtil.getInstance().writeVersionFile(String.valueOf(version),"buildState");
			versionResult=version;
		}else{
			versionResult=Boolean.valueOf(buildVersion);
		}
		return versionResult;
	}
	
	public NodeMgrDao getDao() {
		return dao;
	}

	public void setDao(NodeMgrDao dao) {
		this.dao = dao;
	}
	
	public DataSourceDao getDataSourceDao() {
		return dataSourceDao;
	}

	public void setDataSourceDao(DataSourceDao dataSourceDao) {
		this.dataSourceDao = dataSourceDao;
	}

	public ResourceDao getResourceDao() {
		return resourceDao;
	}

	public void setResourceDao(ResourceDao resourceDao) {
		this.resourceDao = resourceDao;
	}

	public SystemConfigService getSystemConfigService() {
		return systemConfigService;
	}

	public void setSystemConfigService(SystemConfigService systemConfigService) {
		this.systemConfigService = systemConfigService;
		
//		new Thread(this).start();

	}
	
	public synchronized boolean registerSMP()throws DataAccessException {
		//如果SMP服务器节点还没有注册，则主动注册
		Node nd = getNodeByNodeId(NodeDefinition.NODE_TYPE_SMP);
		if(nd == null){
			try {
				getRootNode(NodeDefinition.NODE_TYPE_SMP);
				systemConfigService.configSMP(NodeDefinition.NODE_TYPE_SMP);
				nd = getNodeByNodeId(NodeDefinition.NODE_TYPE_SMP);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	/**
	* @method: addSystemDataSource 
	* 		  添加系统日志源
	* @author:杨轩嘉
	* @param :node: 节点
	* @return: void
	* @exception: Exception
	*/
	private synchronized void addSystemDataSource(Node node ,String sot, String group) throws Exception{
		String localIp = IpAddress.getLocalIp().getLocalhostAddress() ;
		boolean exist = dataSourceDao.exist(localIp, sot, node.getNodeId(), SimDatasource.DATASOURCE_TYPE_LOG);
		if(!exist){
			SimDatasource simDatasource=new SimDatasource();
			simDatasource.setDeviceIp(localIp);
			simDatasource.setSecurityObjectType(sot);
			simDatasource.setAvailable(1);
			simDatasource.setAuditorNodeId(node.getNodeId());
			simDatasource.setNodeId(node.getNodeId());
			simDatasource.setResourceName(DeviceTypeNameUtil.getDeviceTypeName(sot));
			simDatasource.setArchiveTime("6m");
			simDatasource.setTimer("undefined");
			simDatasource.setCollectMethod("Jms");
			simDatasource.setDataObjectType("log");
			simDatasource.setRate(100);
			simDatasource.setPeak(100);
			simDatasource.setDuration(100);
			simDatasource.setOverwriteEventTime(1);
			simDatasource.setReadonly(1);
			String actionTemplate="<?xml version='1.0' encoding='UTF-8'?><actiontemplate name='properties' class='com.topsec.tsm.util.actiontemplate.action.impl.DefaultTemplateAction'><actionconfiguration><property p_ui='true' p_alias='编码' p_biztype='Select' encoding='UTF-8'/></actionconfiguration></actiontemplate>";
			simDatasource.setActionTemplate(actionTemplate);
			simDatasource.setCustomerId(0L);
			simDatasource.setOwner("system");
			/*if(!group.equalsIgnoreCase("system"))
				simDatasource.setOwnGroup(group);*/
			simDatasource.setOwnGroup(SimDatasource.DATASOURCE_TYPE_LOG) ;
			simDatasource.setCreater("system");
			simDatasource.setCreateTime(new Date());
			simDatasource.setLastModifyed("system");
			simDatasource.setLastModifyedTime(new Date());
			simDatasource.setResourceType(ResourceType.TYPE_SIMDATASOURCE);
			dataSourceDao.save(simDatasource);
		}
	}
	
	private void toLog(String cat, String name, String desc, String subject, boolean result, Severity severity) {
		AuditRecord _log = AuditLogFacade.createSystemAuditLog();
		_log.setBehavior(cat);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(subject);
		_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(result);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}
	@Override
	public Node getNodeByNodeId(String nodeId, boolean hasNodeSegment,boolean hasChildren, boolean hasComponent, boolean hasComponentSegment)throws DataAccessException {
		return dao.getNodeByNodeId(nodeId, hasNodeSegment, hasChildren, hasComponent, hasComponentSegment,false) ;
	}
	
	@Override
	public Node getNodeByNodeId(String nodeId, boolean hasNodeSegment, boolean hasChildren, boolean hasComponent, boolean hasComponentSegment, boolean hasParent) throws DataAccessException {
		return dao.getNodeByNodeId(nodeId, hasNodeSegment, hasChildren, hasComponent, hasComponentSegment, hasParent);
	}
	@Override
	public List<Node> getNodesByType(String type, boolean hasNodeSegment,boolean hasChildren, boolean hasComponent, boolean hasSegment)throws DataAccessException {
		List<Node> nodes=dao.getNodesByType(type);
		if(nodes!=null){
			for (Node node : nodes) {
				iteratorNode(node, hasNodeSegment,hasChildren, hasComponent, hasSegment);
			}
		}
		return nodes;
	}
	@Override
	public List<Node> getNodesByTypes(List<String> types,boolean hasNodeSegment, boolean hasChildren, boolean hasComponent,boolean hasSegment) throws DataAccessException {
		List<Node> allNodes = new ArrayList<Node>() ;
		for(String type:types){
			allNodes.addAll(getNodesByType(type, hasNodeSegment, hasChildren, hasComponent, hasSegment)) ;
		}
		return allNodes;
	}
	@Override
	public Map<String, Object> getPageNodesByTypes(List<String> types, int pageNo, int pageSize, Map<String, String> condition,
			boolean hasNodeSegment, boolean hasChildren,
			boolean hasComponent, boolean hasSegment) throws DataAccessException {
		
		
		List<Node> allNodes = new ArrayList<Node>();
		
		Map<String, Object> result = dao.getPageNodesTypes(pageSize, pageNo, condition, types);
		List<Node> nodes = (List<Node>)result.get("nodes");
		Integer total = (Integer)result.get("total");
		
		Boolean stateCondition = null;
		String state = condition.get("state");
		if(StringUtil.isNotBlank(state)) {
			stateCondition = "0".equals(state);
		}
		List<Node> resultNodes = new ArrayList<Node>();
		if(nodes != null) {
			for (Node node : nodes) {
				if(stateCondition != null) {
					if(NodeStatusQueueCache.online(node.getNodeId()) != stateCondition) {
						if(total > 0) {
							total = total-1;
						}
						continue;
					}
				} else {
					iteratorNode(node, hasNodeSegment, hasChildren, hasComponent, hasSegment);
				}
				resultNodes.add(node);
			}
		}
		allNodes.addAll(resultNodes);

		result.put("allNodes", allNodes);
		result.put("total", total);
		return result;
	}
	@Override
	public Component getComponentWithSegments(Long componentId) {
		return dao.getComponentWithSegments(componentId);
	}
	@Override
	public <T extends XmlSerializable>void updateComponentSegmentAndDispatch(Component component, T t) throws NodeException {
		if(component==null||t==null){
			throw new NodeException("Dispatcher component is null!");
		}
		Segment segment=NodeUtil.findFirstSegment(component, null, t.getClass());
		if(segment==null){
			throw new NodeException("Dispatcher segment is null!") ;
		}
		Blob blob;
		try {
			blob = NodeUtil.convertConfigToBlob(t);
		} catch (XmlAccessException e) {
			throw new NodeException("Convert segment to blob fail!",e) ;
		}
		segment.setLastConfig(segment.getCurrentConfig());
		segment.setCurrentConfig(blob);
		dao.updateComponentSegment(segment) ;
		DoCommand.getInstance().setComponentConfiguration(
				String.valueOf(getNodeByComponentId(component.getResourceId()).getResourceId()), 
				String.valueOf(component.getResourceId()),
				this);
	}
	@Override
	public void disableUserDataSources() {
		List<Node> nodes = dao.getNodesByTypes(NodeDefinition.NODE_TYPE_COLLECTOR,NodeDefinition.NODE_TYPE_AGENT) ;
		SegmentFinder dataSourceFinder = new SegmentFinder(DataSourcesElementFormater.class.getName(), false) ;
		for(Node node:nodes){
			node.iterate(dataSourceFinder) ;
		}
		for(Segment segment:dataSourceFinder.getSegments()){
			try {
				DataSourcesElementFormater dataSourceConfig = (DataSourcesElementFormater) NodeUtil.toSegmentConfiguration(segment).getConfiguration() ;
				List<DataSource> dataSources = dataSourceConfig.getDataSources() ;
				for(DataSource dataSource:dataSources){
					if(DataSourceUtil.isSysDataSource(dataSource.getSecurityObjectType())){
						continue;
					}
					dataSource.setAvailable(false) ;
				}
				Blob blob = NodeUtil.convertConfigToBlob(dataSourceConfig) ;
				segment.setLastConfig(segment.getCurrentConfig());
				segment.setCurrentConfig(blob);
				dao.updateComponentSegment(segment) ;
			} catch (Exception e) {
				log.error("Invalid DataSourceConfig ") ;
			}
		}
	}
	public <T extends XmlSerializable> void updateNodeSegmentAndDispatch(Node node,T t){
		if(node==null||t==null){
			throw new NullPointerException("Parameter is null!") ;
		}
		Segment segment= NodeUtil.findNodeSegment(node, t.getClass().getName()) ;
		if(segment==null){
			segment=new Segment();
			segment.setResourceName("segment");
			segment.setNode(node);
			segment.setType(t.getClass().getName());
		}
		Blob blob;
		try {
			blob = NodeUtil.convertConfigToBlob(t);
			segment.setLastConfig(segment.getCurrentConfig());
			segment.setCurrentConfig(blob);
			dao.updateComponentSegment(segment);
			DoCommand.getInstance().setNodeConfiguration(String.valueOf(node.getResourceId()), this);
		} catch (XmlAccessException e) {
			throw new RuntimeException(e) ;
		}
	}
		
	/**
	* @method: getSegConfigByComAndT 
	* 		   根据组件和T得到可配置的segment配置修改器
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  Component:组件
	* 		   T:t
	* @return: T
	* @exception: Exception
	*/
	@SuppressWarnings("unchecked")
	public <T extends XmlSerializable> T getSegConfigByComAndT(Component component,T t) throws Exception{
		if(component==null||t==null){
			log.error("NodeMgrFacadeImpl.getSegConfigByComAndT(), component==null||t==null!");
			throw new Exception("com.topsec.tsm.sim.node.service.NodeMgrFacadeImpl.getSegConfigByComAndT(), component==null||t==null!");
		}
		T result = getSegmentConfigByClass(component, (Class<T>)t.getClass()) ;
		return result == null ? t : result ;
	}
	@Override
	public <T extends XmlSerializable> T getSegmentConfigByClass(Component component, Class<T> clazz) {
		try {
			String className = clazz.getName() ;
			Set<Segment> segments = component.getSegments();
			if(segments!=null){
				for (Segment segment : segments) {
					if (className.equals(segment.getType())) {
						SegmentConfigurationImpl configuration = (SegmentConfigurationImpl) NodeUtil.toSegmentConfiguration(segment);
						XmlSerializable x=configuration.getConfiguration();
						return (T)x;
					}
				}
			}
			//如果没有找到，并且此类具有默认无参构造方法，则使用类的默认无参构造方法创建一个对象返回
			Constructor<T> defaultConstructor = clazz.getDeclaredConstructor() ;
			if(defaultConstructor != null){
				return defaultConstructor.newInstance() ;
			}else{
				return null ;
			}
		} catch (Exception e) {
			throw new RuntimeExceptionWrapper(e) ;
		}
	}

	@Override
	public int updateNodeNameById(String resourceName, Long resourceId) {
		return this.getResourceDao().updateProperty(resourceId, "ResourceName", resourceName);
	}
	@Override
	public Node getAuditorOrAgentByIp(String ip) {
		return dao.getAuditorOrAgentByIp(ip);
	}
	
	/**
	 * 根据ip地址查询下级节点
	 * @param ip
	 * @return
	 */
	public Node getChildByIp(String ip){
		return dao.getChildByIp(ip);
	}
	/**
	 * 获取父节点信息
	 * @return
	 */
	@Override
	public Node getParentNode() {
		return dao.getParentNode();
	}
	@Override
	public PageBean<Node> queryPageNodes(int pageSize, int pageNo,
			String type, boolean hasNodeSegment, boolean hasChildren,
			boolean hasComponent, boolean hasSegment) {
		
		PageBean<Node> pageNodes = dao.getPageNodes(pageSize, pageNo, type);
		
		List<Node> nodes = pageNodes.getData();
		if(nodes != null) {
			for (Node node : nodes) {
				iteratorNode(node, hasNodeSegment,hasChildren, hasComponent, hasSegment);
			}
		}
		
		return pageNodes;
	}
	
	@Override
	public List<Node> getAll() {
		return dao.getAll();
	}
	@Override
	public Node getChildOrSelf(String nodeId, String nodeType) {
		if(nodeId == null){
			return null ;
		}
		Node node = getNodeByNodeId(nodeId, false,true,false,false) ;
		if(node == null){
			return null ;
		}
		if(NodeUtil.isAuditor(node.getType())){
			return NodeUtil.getChildByType(node, nodeType) ;
		}
		return node;
	}
	
}
