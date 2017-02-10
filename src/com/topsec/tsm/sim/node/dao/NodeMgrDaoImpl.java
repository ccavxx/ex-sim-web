package com.topsec.tsm.sim.node.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.resource.persistence.Segment;
import com.topsec.tsm.sim.util.NodeUtil;

public class NodeMgrDaoImpl extends HibernateDaoImpl<Node, Long> implements NodeMgrDao {
	
	/**
	 * @标题:根据节点的唯一标识(nodeId)获取node
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 6:06:57 PM
	 * @参数:nodeId 节点对象的唯一标识
	 * @返回值:Node
	 */
	public Node getNodeByNodeId(String nodeId){
		Node node = findUniqueByCriteria(Restrictions.eq("NodeId", nodeId)) ;
		return node ;
	}

	@Override
	public Node getNodeByNodeId(String nodeId, boolean hasNodeSegment,boolean hasChildren, boolean hasComponent,boolean hasComponentSegment) {
		return getNodeByNodeId(nodeId, hasNodeSegment, hasChildren, hasComponent, hasComponentSegment, false) ;
	}
	
	@Override
	public Node getNodeByNodeId(String nodeId, boolean hasNodeSegment, boolean hasChildren, boolean hasComponent, boolean hasComponentSegment, boolean hasParent) {
		StringBuffer hql = new StringBuffer("from Node as node ") ;
		if(hasParent){
			hql.append("left join fetch node.parent ") ;
		}
		if(hasNodeSegment){
			hql.append("left join fetch node.segments ") ;
		}
		if(hasChildren){
			hql.append("left join fetch node.children ") ;
		}
		if(hasComponent){
			hql.append("left join fetch node.dataFlows as nodeDataFlows left join fetch nodeDataFlows.components as nodeComponents ") ;
			if(hasComponentSegment){
				hql.append("left join fetch nodeComponents.segments ") ;
			}
		}
		hql.append("where node.NodeId=:nodeId") ;
		Query query = getSession().createQuery(hql.toString()) ;
		query.setString("nodeId", nodeId) ;
		Node node = (Node) query.uniqueResult() ;
		return node ;
	}

	/**
	 * 
	 * @标题:根据多级路由路径递归获取子节点
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 7:56:27 PM
	 * @参数:routeUrl 多级路由url
	 * @返回值:List
	 */
	public List<Node> getSubNodesByRrouteUrl(String routeUrl){
		String hql = "from Node node where node.RouteUrl like '" + routeUrl + "/%' order by node.ResourceId asc";
		List<Node> nodes = (List<Node>) this.getSession().createQuery(hql).list();
		for (Node node : nodes) {
			node.getParent();
			Set<Node> childs = node.getChildren();
			if (childs != null) {
				for (Node child : childs)
					child.getResourceId();
			}

		}
		return nodes;
	}

	/**
	 * 
	 * @标题:查询符合条件的名单,并且分页
	 * @作者:ysf
	 * @创建时间:Aug 10, 2010 8:52:36 PM
	 * @参数:pageSize-->每页大小,pageNo-->当前页码,types-->节点类型集合,states-->名单状态集合
	 * @返回值:Map, List<Node>,total
	 */
	public PageBean<Node> queryNodes(int pageSize, int pageNo, String[] types, int[] states){
		Session session = this.getSession();
		Query query = session.createQuery(createQuery(types, states));
		query.setFirstResult((pageNo - 1) * pageSize);
		query.setMaxResults(pageSize);
		List<Node> nodes = (List<Node>) query.list();
		int totalRecord = queryNodesTotal(types, states) ;
		PageBean<Node> result = new PageBean<Node>(pageNo, pageSize, totalRecord) ;
		result.setData(nodes) ;
		return result;
	}

	/**
	 * 
	 * @标题:查询符合条件的名单总记录数
	 * @作者:ysf
	 * @创建时间:Aug 11, 2010 10:50:44 AM
	 * @参数:types-->节点类型集合,states-->名单状态集合
	 * @返回值:int
	 */
	public int queryNodesTotal(String[] types, int[] states){
		Session session = this.getSession();
		Query query = session.createQuery(createQuery(types, states));
		return query.list().size();
	}

	private String createQuery(String[] types, int[] states) {
		String typeString = StringUtil.join(StringUtil.wrap(types, "'")) ;
		StringBuffer stateString = new StringBuffer() ;
		for(int state:states){
			stateString.append(state).append(',') ;
		}
		stateString.setLength(stateString.length()-1) ;
		return "from Node where parent is not null and  Type in ("+typeString+") and State in("+stateString+")";
	}

	/**
	 * 
	 * @标题:获取根节点，默认是当前服务器
	 * @作者:ysf
	 * @创建时间:Aug 11, 2010 12:21:52 PM
	 * @参数:
	 * @返回值:Node
	 */
	public Node getRootNode(String nodeType, String serverIp){
		Node node = findFirstByCriteria(Restrictions.eq("Type", nodeType)) ;
		return node ;
	}
	/**
	 * 
	 *@标题:更新Node接口配置,包括级联关系(先删除,再添加.)
	 *@参数: nodeId:数据库表里的nodeId
	 *		 node: 从综合审计端传过来的node	
	 *@返回值: 无
	 */
	public void updateNodeConfig(String nodeId, Node node){
		Session session = this.getSession();
		Node oldNode=getNodeByNodeId(nodeId);
		if(oldNode!=null){
			NodeUtil.mergeNode(oldNode, node) ;
			session.update(oldNode);
		}
	}
	/**
	* @method: isDistributed判断是否是多级.
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  
	* @return: Boolean: true为多级,false为单级
	* @exception: Exception
	*/
	@Override
	public boolean isDistributed() {
		Session session = this.getSession();
		String hql="select count(*) from Node node where node.Type='"+NodeDefinition.NODE_TYPE_AUDIT+"'";
		Query query=session.createQuery(hql);
		Number result =(Number) query.uniqueResult();
		if(result.intValue()>1){
			return true;
		} 
		return false;
	}
	
	/**
	* @method: getKernelAuditor 
	* 		  得到核心Auditor
	* @return: Node:核心Auditor
	* @exception: Exception
	*/
	public Node getKernelAuditor() {
		Node node = findUniqueByCriteria(Restrictions.eq("Type", NodeDefinition.NODE_TYPE_AUDIT),
										 Restrictions.eq("Ip", IpAddress.getLocalIp().getLocalhostAddress())) ;
		return node;
	}
	/**
	* @method: getNodesByType 得到节点,根据节点类型
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  type:节点类型
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getNodesByType(String type) {
		Criteria cri = createCriteria(Restrictions.eq("Type", type)) ;
		cri.addOrder(Order.asc("ResourceId")) ;
		return cri.list() ;
	}
	
	/**
	* @method: getNodesByTypes 
	* 		得到节点,根据多个节点类型
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  types:多个节点类型
	* @return: List<Node>: 节点集合
	* @exception: Exception
	*/
	public List<Node> getNodesByTypes(List<String> types) {
		Session session = this.getSession();
		String hql = "from Node node where node.Type in (:types) order by node.ResourceId asc";
		Query query=session.createQuery(hql);
		query.setParameterList("types", types);
		List<Node> list=query.list(); 
		return list;
	}

	@Override
	public List<Node> getNodesByTypes(String... nodeTypes) {
		return getNodesByTypes(Arrays.asList(nodeTypes)) ;
	}

	@Override
	public List<Node> getNodesExcludeTypes(String... nodeTypes) {
		Session session = this.getSession();
		String hql = "from Node node where node.Type not in (:types) order by node.ResourceId asc";
		Query query=session.createQuery(hql);
		query.setParameterList("types", Arrays.asList(nodeTypes));
		List<Node> list=query.list(); 
		return list;
	}

	@Override
	public Node getNodeByComponentId(long componentId) {
		String sql = "from Node node inner join fetch node.dataFlows dataFlow inner join fetch dataFlow.components c where c.ResourceId="+ componentId;
		Node node = (Node) getSession().createQuery(sql).uniqueResult() ;
		return node ;
	}

	@Override
	public void updateComponentSegment(Segment segment) {
		getSession().update(segment) ;
	}

	@Override
	public Component getComponentWithSegments(Long componentId) {
		Query query = getSession().createQuery("from Component comp left join fetch comp.segments where comp.id=:componentId") ;
		query.setLong("componentId", componentId) ;
		return (Component) query.uniqueResult();
	}

	@Override
	public void updateComponent(Component component) {
		getSession().update(component) ;
	}

	@Override
	public Node getAuditorOrAgentByIp(String ip) {
		return findFirstByCriteria(Restrictions.eq("Ip", ip),
				Restrictions.in("Type", new Object[]{NodeDefinition.NODE_TYPE_AUDIT,NodeDefinition.NODE_TYPE_AGENT}));
	}
	@Override
	public Node getChildByIp(String ip) {
		return findFirstByCriteria(Restrictions.eq("Ip", ip),
				Restrictions.in("Type", new Object[]{NodeDefinition.NODE_TYPE_CHILD}));
	}
	
	/**
	 * 获取父节点信息
	 * @return
	 */
	@Override
	public Node getParentNode() {
		String sql = "from Node node  where node.Type='Parent'";
		Node node = (Node) getSession().createQuery(sql).uniqueResult() ;
		return node ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PageBean<Node> getPageNodes(int pageSize, int pageNo, String type) {
		
		String hql = "from Node node where node.Type=:type order by node.ResourceId asc";
		
		Session session = this.getSession();
		Query query = session.createQuery(hql);

		query.setString("type", type);
		
		int totalRecord = query.list().size();
		
		query.setFirstResult((pageNo - 1) * pageSize);
		query.setMaxResults(pageSize);
		
		List<Node> nodes = (List<Node>)query.list();
		
		PageBean<Node> result = new PageBean<Node>(pageNo, pageSize, totalRecord);
		result.setData(nodes);
		
		return result;
	}
	@Override
	public Map<String, Object> getPageNodesTypes(int pageSize, int pageNo, Map<String, String> condition, List<String> types) {
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(Node.class);
		criteria = criteria.add(Restrictions.in("Type", types));
		
		String resourceName = condition.get("resourceName");
		if(StringUtil.isNotBlank(resourceName)){
			criteria = criteria.add(Restrictions.like("ResourceName", "%" + resourceName + "%"));
		}
		
		String ip = condition.get("ip");
		if(StringUtil.isNotBlank(ip)){
			criteria = criteria.add(Restrictions.like("Ip", "%" + ip + "%"));
		}
		criteria = criteria.addOrder(Order.desc("Type"));
		
		int totalRecord = criteria.list().size();
		
		criteria.setFirstResult((pageNo - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		
		List<Node> nodes = (List<Node>)criteria.list();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("total", totalRecord);
		result.put("nodes", nodes);
		
		return result;
	}
}
