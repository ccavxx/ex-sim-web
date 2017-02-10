/**
 * 版权声明北京天融信科技有限公司，版权所有违者必究
 *
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
 * @since  2011-06-24
 * @version 1.0
 * 
 */
package com.topsec.tsm.sim.node.util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.node.status.NodeStatusMap;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.AuditLogFacade;

/**
 * 功能描述: 节点状态缓存队列
 */
public class NodeStatusQueueCache {
	private static final Logger log = LoggerFactory.getLogger(NodeStatusQueueCache.class);

	private static NodeStatusQueueCache instance;

	// 节点状态缓存队列map
	private Map<String, NodeStatusQueue> nodeStatusQueueCache = new ConcurrentHashMap<String, NodeStatusQueue>();
	// 时间更新标识
	private Map<String, Date> dateMap = new HashMap<String, Date>();

	// 第一次连接时间标识
	private Map<String, Date> firstConnectDateMap = new HashMap<String, Date>();

	private int cacheNum = 60;

	private Object object = new Object();

	private NodeMgrFacade nodeMgrFacade;

	private volatile boolean _shutdown = false;

	public static final int nodeTimeout = 60 * 1000;

	private NodeStatusQueueCache() {
		init();
	}

	public synchronized static NodeStatusQueueCache getInstance() {
		if (instance == null) {
			instance = new NodeStatusQueueCache();
		}
		return instance;
	}

	/**
	 * @method: putNodeStatusMap 放入节点状态Map
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param: nameSpace: 节点标识 nodeStatusMap: 节点状态Map
	 * @return: void
	 * @exception: Exception
	 */
	public synchronized void putNodeStatusMap(String nameSpace, NodeStatusMap nodeStatusMap) throws Exception {
		nodeStatusMap.setStatusTime(new Date()) ;
		synchronized (object) {
			if (!nodeStatusQueueCache.containsKey(nameSpace)) {
				NodeStatusQueue nodeStatusQueue = new NodeStatusQueue(cacheNum);
				nodeStatusQueue.push(nodeStatusMap);
				nodeStatusQueueCache.put(nameSpace, nodeStatusQueue);
				Date date = GregorianCalendar.getInstance().getTime();
				dateMap.put(nameSpace, date);
				firstConnectDateMap.put(nameSpace, date);
			} else {
				NodeStatusQueue nodeStatusQueue = nodeStatusQueueCache.get(nameSpace);
				nodeStatusQueue.push(nodeStatusMap);
				nodeStatusQueueCache.put(nameSpace, nodeStatusQueue);
				Date date = dateMap.get(nameSpace);
				// date相当于又指向了一个新的堆内存,而不是在源地址上赋值(date.setHour("XXX")).所以需要把新的堆内存地址存到map中.
				date = GregorianCalendar.getInstance().getTime();
				dateMap.put(nameSpace, date);
			}
		}
	}

	/**
	 * @method: getNodeStatus 得到节点状态缓存队列
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param: nodeNameSpance: 节点标识
	 * @return: NodeStatusQueue:缓存队列
	 * @exception: Exception
	 */
	public NodeStatusQueue getNodeStatus(String nodeNameSpance) {
		NodeStatusQueue nodeStatusQueue = nodeStatusQueueCache
				.get(nodeNameSpance);
		if (nodeStatusQueue == null) {
			return null;
		}
		return nodeStatusQueue;
	}

	/**
	 * @method: getLastUpdateDate 根据节点标识得到缓存队列最后一次更新时间
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param: nameSpace: 节点标识
	 * @return: Date:最后一次更新时间
	 * @exception: Exception
	 */
	public Date getLastUpdateDate(String nameSpace) {
		Date date = dateMap.get(nameSpace);
		if (date == null) {
			return null;
		}
		return date;
	}

	public boolean isNodeOnline(String nodeId){
		Date date = getLastUpdateDate(nodeId) ;
		if(date == null || date.getTime() - System.currentTimeMillis() > NodeStatusQueueCache.nodeTimeout){
			return false ;
		}
		return true ;
	}
	
	public boolean isNodeOffline(String nodeId){
		return !isNodeOnline(nodeId) ;
	}
	
	/**
	 * @method: getFirstConnectDate 根据节点标识得到缓存队列最后一次更新时间
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param: nameSpace: 节点标识
	 * @return: Date:最后一次更新时间
	 * @exception: Exception
	 */
	public Date getFirstConnectDate(String nameSpace) {
		Date date = firstConnectDateMap.get(nameSpace);
		if (date == null) {
			return null;
		}
		return date;
	}

	/**
	 * @method: removeCacheByNameSpace 从缓存中删除节点缓存数据
	 * @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	 * @param: nameSpace: 节点标识
	 * @exception: Exception
	 */
	public synchronized void removeNodeCacheByNameSpace(String nameSpace) {
		synchronized (object) {
			nodeStatusQueueCache.remove(nameSpace);
			dateMap.remove(nameSpace);
			firstConnectDateMap.remove(nameSpace);
		}
	}

	private void init() {
		Thread hook = new Thread() {
			public void run() {
				_shutdown = true;
			}
		};
		Runtime.getRuntime().addShutdownHook(hook);
		new Thread(new ValidateState(),"NodeStatusChecker").start();
	}

	class ValidateState implements Runnable {

		@Override
		public void run() {
			while (!_shutdown) {
				long now = System.currentTimeMillis();
				synchronized (object) {
					Set<String> keysSet = nodeStatusQueueCache.keySet();
					if (keysSet != null && keysSet.size() > 0) {
						for (Iterator iterator = keysSet.iterator(); !_shutdown
								&& iterator.hasNext();) {
							String s = (String) iterator.next();
							Date d = dateMap.get(s);
							if (d == null) {
								log.warn("ValidateState.run(), Date d=dateMap.get(s), d==null!!!!");
								continue;
							}
							if (now - d.getTime() >= nodeTimeout) {
								/*
								 * nodeStatusQueueCache.remove(s); 报错
								 * 虽然循环的是keysSet,
								 * 但是其实还是在循环nodeStatusQueueCache这个map,
								 * 在循环里删除nodeStatusQueueCache内的对象,
								 * 会使内部的指针与删除前的size不一致. (本以为循环的是keysSet,
								 * 而删除的是nodeStatusQueueCache里的对象没有问题
								 * ,结果还是没有考虑周期.很隐蔽的问题~)
								 */
								iterator.remove();
								dateMap.remove(s);
								firstConnectDateMap.remove(s);
								checkNodeTypeAndSendLog(s);
							}
						}
					}
				}
				try {
					Thread.sleep(30 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void checkNodeTypeAndSendLog(String nodeId) {
		try {
			nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade");
			Node node = nodeMgrFacade.getNodeByNodeId(nodeId);
			if (node != null) {
				if (NodeDefinition.NODE_TYPE_AUDIT.equals(node.getType()) || 
					NodeDefinition.NODE_TYPE_AGENT.equals(node.getType())) {
					Map<String, String> logMap = new HashMap<String, String>();
					logMap.put("nodeIp", node.getIp());
					if (NodeDefinition.NODE_TYPE_AGENT.equals(node.getType())) {
						logMap.put("typeZH", "代理");
					} else if (NodeDefinition.NODE_TYPE_AUDIT.equals(node.getType())) {
						logMap.put("typeZH", "服务器");
					}
					sendAgentOfflineLog(logMap);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	public void sendAgentOfflineLog(Map<String, String> map) {
		AuditRecord talSysLog = AuditLogFacade.createSystemAuditLog();
		talSysLog.setBehavior(AuditCategoryDefinition.SYS_AGENT_OFFLINE); // 代理掉线
		talSysLog.setSecurityObjectName(map.get("typeZH") + "掉线"); // 名称
		talSysLog.setDescription(map.get("typeZH") + map.get("nodeIp") + "掉线");// 描述
		talSysLog.setObjectAddress(new IpAddress(map.get("nodeIp")));// 主体地址
		talSysLog.setSubjectAddress(IpAddress.getLocalIp());// 客体地址
		talSysLog.setSeverity(Severity.HIGHEST);// 安全级别
		AuditLogFacade.send(talSysLog);
	}
	/**
	 * 判断节点是否在线
	 * @param nodeId
	 * @return
	 */
	public static boolean online(String nodeId){
		return getInstance().isNodeOnline(nodeId) ;
	}
	/**
	 * 判断一个节点是否掉线
	 * @param nodeId
	 * @return
	 */
	public static boolean offline(String nodeId){
		return getInstance().isNodeOffline(nodeId) ;
	}
}
