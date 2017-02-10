package com.topsec.tsm.sim.node.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.xml.AbstractElementFormater;
import com.topsec.tsm.base.xml.XmlSerializable;
import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.sim.common.exception.DataAccessException;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;

public interface NodeMgrFacade {
	
	/**
	 * 根据节点标识查找节点对象
	 * @param id
	 * @return
	 */
	public Node getNodeById(Long id) ;
	
	/**
	 * 根据节点id(nodeId)获取node
	 * @return Node
	 */
	public Node getNodeByNodeId(String nodeId) throws DataAccessException ;
	
	/**
	* @method: getNodeByNodeId 
	* 		根据节点的唯一标识(nodeId)获取node
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param nodeId nodeId
	* @param hasNodeSegment:是否遍历node的segment配置
	* @param hasChildren:是否遍历node的子节点
	* @param hasDataflowComponents:是否遍历node中dataflow中的component
	* @param hasComponentSegments:是否遍历dataflow中component中的segment配置
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public Node getNodeByNodeId(String nodeId,boolean hasNodeSegment,boolean hasChildren,boolean hasDataflowComponents,boolean hasComponentSegments) throws DataAccessException;
	
	public Node getNodeByNodeId(String nodeId,boolean hasNodeSegment,boolean hasChildren,boolean hasDataflowComponents,boolean hasComponentSegments,boolean hasParent) throws DataAccessException;
	
	//dinggf
	//临时增加方法 ，确保自审计系统日志源能够创建，主要解决已经部署的系统问题
	//该方法不会被频繁调度
	public void ensureSystemDatasource() throws Exception;
	
	
	/**
	 *@标题:注册一个节点
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 6:06:57 PM
	 *@参数:node 节点对象,route 路由
	 *@返回值:boolean
	 */
	public boolean  registerNode(Node node,String[] route) throws DataAccessException,NodeException ;	

	/**
	 *@标题:注册SMP
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 6:06:57 PM
	 *@返回值:boolean
	 */
	public boolean  registerSMP() throws DataAccessException ;	

	/**
	 *@标题:删除一个节点
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 6:06:57 PM
	 *@参数:node 节点对象
	 *@返回值:void
	 */
	public void delNode(Node node) throws DataAccessException ;	
	
	/**
	 *@标题:删除一个节点
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 6:06:57 PM
	 *@参数:node 节点对象
	 *@返回值:void
	 */
	public void delNode(long resourceId) throws DataAccessException ;		
	
	/**
	 *@标题:获取子节点
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 7:56:27 PM
	 *@参数:resourceId 资源id
	 *@参数:recursive 是否递归获取所有的节点 true-->递归获取,false-->只获取下级
	 *@返回值:List
	 */
	public List<Node> getSubNodesByResourceId(long resourceId,boolean recursive)throws DataAccessException ;
	
	/**
	 * 
	 *@标题:根据节点的url获取所有的子节点
	 *@作者:ysf 
	 *@创建时间:Dec 7, 2010 3:47:22 PM
	 *@参数:
	 *@返回值:List<Node>
	 */
	public List<Node> getSubNodesByRrouteUrl(String routeUrl)throws DataAccessException;
	
	/**
	 * 
	 *@标题:
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 8:44:15 PM
	 *@参数:resourceIds-->id集合，state-->要改变的状态
	 *@返回值:void
	 */
	public void changeState(long[] resourceIds,int state)throws DataAccessException;
	
	/**
	 * 
	 *@标题:查询符合条件的名单,并且分页
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 8:52:36 PM
	 *@参数:pageSize-->每页大小,pageNo-->当前页码,types-->节点类型集合,states-->名单状态集合
	 *@返回值:Map, List<Node>,total
	 */
	public PageBean<Node> queryNodes(int pageSize,int pageNo,String[] types,int[] states) throws DataAccessException;
	
	/**
	 * 
	 *@标题:获取根节点
	 *@作者:ysf 
	 *@创建时间:Aug 11, 2010 12:21:52 PM
	 *@参数:nodeType :soc或者liveUpdate
	 *@返回值:Node
	 */
	public Node getRootNode(String nodeType)throws DataAccessException;
	
	/**
	 * 
	 *@标题:更新一个节点
	 *@作者:ysf 
	 *@创建时间:Aug 12, 2010 4:11:47 PM
	 *@参数:
	 *@返回值:Node
	 */
	public Node updateNode(Node node)throws DataAccessException; 

	/**
	 * 
	 *@标题:获取一个节点
	 *@作者:ysf 
	 *@创建时间:Aug 12, 2010 4:12:42 PM
	 *@参数:
	 *@返回值:Node
	 */
	public Node getNodeWithPolicy(long resourceId)throws DataAccessException;
	
	public Node getNodeWithDataFlow(long resourceId)throws DataAccessException;
	
	public Node getNodeWithDataFlowByNodeId(String nodeId)throws DataAccessException;
	public List<Node> getAllNodesWithComponents()throws DataAccessException;
	public Node getNodeByComponentId(long componentId)throws DataAccessException;
	
	/* modify by yangxuanjia at 2011-01-13 start */
	/**
	 * 
	 *@标题:更新Node接口配置,包括级联关系(先删除,再添加.)
	 *@作者:yangxuanjia
	 *@创建时间:2011-01-13
	 *@参数: nodeId:数据库表里的nodeId
	 *		 node: 从综合审计端传过来的node	
	 *@返回值: 无
	 */
	public void updateNodeConfig(String nodeId,Node node)throws DataAccessException;	
	
	/**
	* @method: isDistributed 
	* 		    判断是否是多级.
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  
	* @return: boolean: true为多级,false为单级
	* @exception: Exception
	*/
	public boolean isDistributed() throws DataAccessException;
	
	/**
	* @method: getDataSourceBindableNodes
	* 		 得到数据源可绑定的node节点
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  hasComponent:是否包含组件
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getDataSourceBindableNodes(boolean hasComponent) throws DataAccessException;
	
	/**
	* @method: getKernelAuditor 
	* 		  得到核心Auditor
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  hasComponent:是否包含组件
	* @return: Node:核心Auditor
	* @exception: Exception
	*/
	public Node getKernelAuditor(boolean hasComponent) throws DataAccessException;
	
	/**
	* @method: getNodesByType 
	* 		得到节点,根据节点类型
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  type:节点类型
	* 		   hasNodeSegment:是否遍历node配置
	*  		   hasChildren:是否遍历node孩子
	*   	   hasComponent:是否遍历node的组件
	*    	   hasSegment:是否遍历node组件的配置
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getNodesByType(String type,boolean hasNodeSegment,boolean hasChildren,boolean hasComponent,boolean hasSegment) throws DataAccessException;
	
	/**
	* @method: getNodesByTypes 
	* 		得到节点,根据多个节点类型
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  types:多个节点类型
	*  		   hasNodeSegment:是否遍历node配置
	*  		   hasChildren:是否遍历node孩子
	*   	   hasComponent:是否遍历node的组件
	*    	   hasSegment:是否遍历node组件的配置
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getNodesByTypes(List<String> types,boolean hasNodeSegment,boolean hasChildren,boolean hasComponent,boolean hasSegment) throws DataAccessException;
	
	public Map<String, Object> getPageNodesByTypes(List<String> types, int pageNo, int pageSize,Map<String, String> condition,
			boolean hasNodeSegment, boolean hasChildren,
			boolean hasComponent, boolean hasSegment) throws DataAccessException;
	/**
	* @method: getBuildState
	* 			读取conf目录下buildVersion.xml文件,得到上一次系统单级多级信息
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param: 
	* @return: buildVersion 上一次系统单级多级信息
	*/
	public boolean getBuildState() throws DataAccessException;
	/**
	 * 获取节点组件
	 * @param componentId　组件id
	 * @return
	 */
	public Component getComponentWithSegments(Long componentId) ;
	
	public <T extends XmlSerializable> void updateComponentSegmentAndDispatch(Component component,T t) throws NodeException ;	
	/**
	 * 更新并下发节点配置
	 *  @param node
	 *  @param t
	 */
	public <T extends XmlSerializable> void updateNodeSegmentAndDispatch(Node node,T t) ;
	/**
	 * 禁用用户创建的日志源
	 * 系统自审计日志源不会被禁用
	 * @param
	 * @
	 */
	public void disableUserDataSources() ;
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
	public Component getBindableComponentByType(Node node,String type,boolean hasSegments) throws DataAccessException;
	/**
	* @method: getSegConfigByComAndT 
	* 		   根据组件和T得到可配置的segment配置修改器
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  Component:组件
	* 		   T:t
	* @return: T
	* @exception: Exception
	*/
	public <T extends XmlSerializable> T getSegConfigByComAndT(Component component,T t) throws Exception;
	/**
	 * 根据指定的类型获取组件的segment配置
	 * @param component 组件
	 * @param clazz 要获取的类型
	 * @return
	 */
	public <T extends  XmlSerializable> T getSegmentConfigByClass(Component component,Class<T> clazz) ;
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
	public Node getKernelAuditor(boolean hasNodeSegment,boolean hasChildren,boolean hasComponent,boolean hasSegment) throws DataAccessException;

	/**
	* @method: updateNodeNameById 
	* 		  通过resourceId更新resourceName
	* @author: 王桥(wang_qiao@topsec.com.cn)
	* @param:  resourceName:name
	*  		   resourceId:id
	* @return: boolean:核心Auditor
	* @exception: Exception
	*/
	public int updateNodeNameById(String resourceName, Long resourceId);
	/**
	 * 根据ip地址查询auditor或者agent
	 * @param ip
	 * @return
	 */
	public Node getAuditorOrAgentByIp(String ip) ;
	
	/**
	 * 获取父节点信息
	 * @return
	 */
	public Node getParentNode() ;
	/**
	 * 根据ip地址查询下级节点
	 * @param ip
	 * @return
	 */
	public Node getChildByIp(String ip) ;
	
	/**
	 * 分页查询
	 * by horizon
	 * @param pageSize
	 * @param pageNum
	 * @param type
	 * @param hasNodeSegment
	 * @param hasChildren
	 * @param hasComponent
	 * @param hasSegment
	 * @return
	 */
	public PageBean<Node> queryPageNodes(int pageSize, int pageNum, String type,boolean hasNodeSegment,boolean hasChildren,boolean hasComponent,boolean hasSegment);
	
	public List<Node> getAll() ;
	/**
	 * 获取子节点类型为指定类型的节点，如果不存在则返回此节点
	 * @param nodeType
	 * @return
	 */
	public Node getChildOrSelf(String nodeId,String nodeType) ;
}
