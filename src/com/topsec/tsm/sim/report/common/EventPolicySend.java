/**
 * 版权声明北京天融信科技有限公司，版权所有违者必究
 *
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author liuzhan
 * @since  2011-08-02
 * @version 1.0
 * 
 */
package com.topsec.tsm.sim.report.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.topsec.tsm.node.component.handler.AlarmConfiguration;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.EventPolicy;
import com.topsec.tsm.sim.response.persistence.EventPolicy_R_Response;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.tal.alarm.adjudicate.EventPolicy0;
import com.topsec.tsm.tal.alarm.adjudicate.Response0;
import com.topsec.tsm.tal.service.EventResponseService;

/**
 * 告警下发类
 * 
 * @author liuzhan
 */
public class EventPolicySend {
	private static EventPolicySend dispatcher = null;

	public synchronized static EventPolicySend getInstance() {
		if (dispatcher == null)
			dispatcher = new EventPolicySend();
		return dispatcher;
	}

	/**
	 * 告警下发入口，主要是预先处理好单级、多级，在自定义和默认情况下发给那些节点
	 * 
	 * @param eventPolicyPo
	 * @param nodeMgrFacade
	 * @param type
	 * @param ifRemove
	 * @param ifAdd
	 */
	public void send(EventPolicy eventPolicyPo, NodeMgrFacade nodeMgrFacade, String type, boolean ifRemove, boolean ifAdd) {
		try {
			if (!nodeMgrFacade.isDistributed()) {// 单级，默认和自定义都下发给核心Auditor
				Node node = nodeMgrFacade.getKernelAuditor(false);// 核心Auditor
				Set<Node> nodes = new HashSet<Node>();
				nodes.add(node);
				eventPolicyPo.setNodes(nodes);
				sendToNode(eventPolicyPo, nodeMgrFacade, type, ifRemove, ifAdd);

			} else {// 多级，分默认和自定义
				if (0 == eventPolicyPo.getSortKey()) {// 默认告警规则，下发给所有Auditor，页面无“节点关联”
					List<Node> nodeList = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, false, true, true);
					Set<Node> nodes = new HashSet<Node>(nodeList);
					eventPolicyPo.setNodes(nodes);
					sendToNode(eventPolicyPo, nodeMgrFacade, type, ifRemove, ifAdd);
				} else {// 自定义，下发给选择的Auditor
					sendToNode(eventPolicyPo, nodeMgrFacade, type, ifRemove, ifAdd);
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 告警下发
	 * 
	 * @param eventPolicyPo
	 *           告警对象
	 * @param nodeMgrFacade
	 *           操作node的dao
	 * @param type
	 *           操作类型
	 * @param ifRemove
	 *           旧的下发配置是否移除
	 * @param ifAdd
	 *           新的下发配置是否添加
	 */

	public void sendToNode(EventPolicy eventPolicyPo, NodeMgrFacade nodeMgrFacade, String type, boolean ifRemove, boolean ifAdd) {
		try {
			Set<Node> nodes = eventPolicyPo.getNodes();// 1、下发到的节点对象
			if (nodes != null && nodes.size() > 0) {
				for (Node _node : nodes) {
					Node node = nodeMgrFacade.getNodeByNodeId(_node.getNodeId(), false, false, true, true);// 下发节点的所有属性（而不是懒加载）
					Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_ALARM, true);// 2、 下发节点的组件对象
					if (component != null) {
						AlarmConfiguration alarmConfiguration = new AlarmConfiguration();
						alarmConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, alarmConfiguration);// 3、 下发配置封装对象
						if (alarmConfiguration != null) {
							if ("save".equals(type)) {
								List<EventPolicy0> eventpolicys = alarmConfiguration.getEventpolicys();
								EventPolicy0 _eventPolicy0 = copyProp(eventPolicyPo);
								eventpolicys.add(_eventPolicy0);
								nodeMgrFacade.updateComponentSegmentAndDispatch(component, alarmConfiguration);
							}
							if ("modify".equals(type)) {
								List<EventPolicy0> eventpolicys = alarmConfiguration.getEventpolicys();// 旧的下发配置
								/* modify by yangxuanjia at 2011-11-14 start */
								if (eventpolicys != null) {
									for (EventPolicy0 eventPolicy0 : eventpolicys) {// 遍历旧的下发配置(EventPolicy0用来封装下发所需参数)
										
										if (0 == eventPolicyPo.getSortKey()) {// 默认的告警规则
											if (ifRemove) {
												if (eventPolicy0.getSymbol().equals(eventPolicyPo.getSymbol())) {// 区分默认的告警规则
													eventpolicys.remove(eventPolicy0);
													break;// 下发
												}
											}
										} else {// 自定义告警规则
											if (ifRemove) {
												if (eventPolicy0.getId().equals(eventPolicyPo.getId())) {// 通过id区分
													eventpolicys.remove(eventPolicy0);
													break;
												}
											}
										 }
									}
									if (ifAdd) {
										EventPolicy0 _eventPolicy0 = copyProp(eventPolicyPo);
										eventpolicys.add(_eventPolicy0);
									}
										
									nodeMgrFacade.updateComponentSegmentAndDispatch(component, alarmConfiguration);
								}
								/* modify by yangxuanjia at 2011-11-14 end */
							}
						}
					}
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 主要针对所有默认告警的下发，指在新添加节点时的操作
	 * 
	 * @param eventResponseService
	 *           告警的service
	 * @param nodeMgrFacade
	 *           node的dao
	 * @param nodeId
	 *           节点
	 */
	public void updateDefaultEPtoNode(EventResponseService eventResponseService, NodeMgrFacade nodeMgrFacade, String nodeId) {
	
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(nodeId, false, false, true, true);// 1、下发到的节点对象， 下发节点的所有属性（而不是懒加载）
			Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_ALARM, true);// 2、 下发节点的组件对象
			if (component != null) {
				AlarmConfiguration alarmConfiguration = new AlarmConfiguration();
				alarmConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, alarmConfiguration);// 3、 下发配置封装对象
				List<EventPolicy0> eventpolicys = alarmConfiguration.getEventpolicys();// 旧的下发配置
				if (eventpolicys != null && eventpolicys.size() > 0) {
					List<EventPolicy> allEventPolicys = eventResponseService.getAllEventPolicys(-1, -1);// 所有的告警对象
					for (EventPolicy eventPolicyPo : allEventPolicys) {
						if (0 == eventPolicyPo.getSortKey()) {// 只针对默认的告警规则
							for (EventPolicy0 eventPolicy0 : eventpolicys) {// 遍历旧的下发配置(EventPolicy0用来封装下发所需参数)
								if (eventPolicy0.getSymbol().equals(eventPolicyPo.getSymbol())) {// 区分默认的告警规则
									eventpolicys.remove(eventPolicy0);// 去掉旧的
									EventPolicy0 _eventPolicy0 = copyProp(eventPolicyPo);// 拷贝新的
									eventpolicys.add(_eventPolicy0);
									break;
								}
							}
						}
					}
					nodeMgrFacade.updateComponentSegmentAndDispatch(component, alarmConfiguration);
				}
	
			}
		} catch (Exception e) {
	
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝属性
	 * 
	 * @param eventPolicyPo
	 *           新的下发配置对象EventPolicy
	 * @return 新的下发配置对象EventPolicy0
	 */
	public EventPolicy0 copyProp(EventPolicy eventPolicyPo) {
		EventPolicy0 eventPolicy0 = new EventPolicy0();
		eventPolicy0.setEventFrequencyType(eventPolicyPo.getEventFrequencyType());
		eventPolicy0.setFilterSql(eventPolicyPo.getFilterSql());
		eventPolicy0.setFrequency(eventPolicyPo.getFrequency());
		eventPolicy0.setId(eventPolicyPo.getId());
		eventPolicy0.setName(eventPolicyPo.getName());
		eventPolicy0.setStart(eventPolicyPo.isStart());
		eventPolicy0.setSymbol(eventPolicyPo.getSymbol());
		eventPolicy0.setDesc(eventPolicyPo.getDesc());
		List<Response> responseList = new ArrayList<Response>();// vo
		Set<EventPolicy_R_Response> set = eventPolicyPo.getResponses();// 响应对象列表
		if (set != null && set.size() > 0) {
			Iterator<EventPolicy_R_Response> iterator = set.iterator();
			EventPolicy_R_Response eprr = null;
			while (iterator.hasNext()) {
				eprr = iterator.next();
				responseList.add(eprr.getResponse());
			}
			List<Response0> responsels = new ArrayList<Response0>();// po
			for (Response _response : responseList) {
				Response0 response0 = new Response0();
				response0.setCfgKey(_response.getCfgKey());
				response0.setId(_response.getId());
				response0.setName(_response.getName());
				response0.setStart(_response.isStart());
				responsels.add(response0);
			}
			eventPolicy0.setResponsels(responsels);// 响应
		}
		return eventPolicy0;
	}

}
