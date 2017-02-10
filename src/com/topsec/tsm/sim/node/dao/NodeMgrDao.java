package com.topsec.tsm.sim.node.dao;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.resource.persistence.Segment;

/**
 * node节点管理
 * node分为三种类型：soc，综合审计，代理
 *
 */
public interface NodeMgrDao extends BaseDao<Node, Long>{
	
	/**
	 * 根据节点的唯一标识(nodeId)获取node
	 * @return Node
	 */
	public Node getNodeByNodeId(String nodeId);
	/**
	 * 根据nodeId查找节点对象
	 * @param nodeId
	 * @param hasNodeSegment　是否加载节点segment
	 * @param hasChildren是否加载子节点
	 * @param hasComponent是否加载节点组件
	 * @param hasComponentSegment是否加载组件的segment
	 * @return
	 */
	public Node getNodeByNodeId(String nodeId, boolean hasNodeSegment,boolean hasChildren, boolean hasComponent, boolean hasComponentSegment) ;
	
	public Node getNodeByNodeId(String nodeId, boolean hasNodeSegment,boolean hasChildren, boolean hasComponent, boolean hasComponentSegment,boolean hasParent) ;
	
	/**
	 * 
	 *@标题:根据路由路径递归获取子节点
	 *@创建时间:Aug 10, 2010 7:56:27 PM
	 *@参数:resourceId 资源id
	 *@返回值:List
	 */
	public List<Node> getSubNodesByRrouteUrl(String routeUrl);
	
	/**
	 *@标题:查询符合条件的名单,并且分页
	 *@作者:ysf 
	 *@创建时间:Aug 10, 2010 8:52:36 PM
	 *@参数:pageSize-->每页大小,pageNo-->当前页码,types-->节点类型集合,states-->名单状态集合
	 *@返回值:Map, List<Node>,total
	 */
	public PageBean<Node> queryNodes(int pageSize,int pageNo,String[] types,int[] states);
	
	/**
	 * 
	 *@标题:获取根节点，默认是当前服务器(SOC)
	 *@创建时间:Aug 11, 2010 12:21:52 PM
	 *@参数:
	 *@返回值:Node
	 */
	public Node getRootNode(String nodeType,String serverIp);
	
	/**
	 *@标题:更新Node接口配置,包括级联关系(先删除,再添加.)
	 *@参数: nodeId:数据库表里的nodeId　
	 *node: 从综合审计端传过来的node	
	 *@返回值: 无
	 */
	public void updateNodeConfig(String nodeId,Node node);	
	
	/**
	* @method: isDistributed 判断是否是多级.
	* @param:  
	* @return: Boolean: true为多级,false为单级
	* @exception: Exception
	*/
	public boolean isDistributed() ;
	
	/**
	* @method: getKernelAuditor 得到核心Auditor
	* @param:  
	* @return: Node:核心Auditor
	* @exception: Exception
	*/
	public Node getKernelAuditor() ;
	
	/**
	* @method: getNodesByType 
	* 		得到节点,根据节点类型

	* @param:  type:节点类型
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getNodesByType(String type) ;
	
	/**
	* @method: getNodesByTypes 得到节点,根据多个节点类型
	* @param:  types:多个节点类型
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getNodesByTypes(List<String> types) ;
	/**
	 * 根据节点类型获取节点列表
	 * @param types
	 * @return
	 */
	public List<Node> getNodesByTypes(String... nodeTypes) ;
	/**
	 *　获取节点列表，但排除nodeTypes类型的节点
	 * @param types　要排除的节点类型数组
	 * @return
	 */
	public List<Node> getNodesExcludeTypes(String... nodeTypes) ;
	/**
	 * 根据组件id查找组件所属的节点
	 * @param componentId
	 * @return
	 */
	public Node getNodeByComponentId(long componentId);
	/**
	 * 更新节点组件配置信息 
	 * @param segment
	 */
	public void updateComponentSegment(Segment segment) ;
	/**
	 * 获取组件信息，包含组件中的所有segments信息
	 * @param componentId
	 * @return
	 */
	public Component getComponentWithSegments(Long componentId) ;
	/**
	 * 更新节点component
	 * @param component
	 */
	public void updateComponent(Component component) ;
	
	public Node getAuditorOrAgentByIp(String ip);
	/**
	 * 根据ip地址查询下级节点
	 * @param ip
	 * @return
	 */
	public Node getChildByIp(String ip);
	/**
	 * 获取父节点信息
	 */
	public Node getParentNode();
	/**
	 * 分页查询
	 * @param pageSize
	 * @param pageNo
	 * @param type
	 * @return
	 */
	public PageBean<Node> getPageNodes(int pageSize, int pageNo, String type);
	public Map<String, Object> getPageNodesTypes(int pageSize, int pageNo, Map<String, String> condition, List<String> types);
}
