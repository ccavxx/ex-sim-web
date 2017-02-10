package com.topsec.tsm.sim.asset.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.node.component.handler.AlarmConfiguration;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.ComponentConfigException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.EventPolicyMonitor;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.tal.alarm.adjudicate.EventPolicy0;
import com.topsec.tsm.tal.alarm.adjudicate.Response0;

/**
 * 告警下发类
 * 
 * @author liuzhan
 */
public class AlarmPolicyManager {
	
	public static int ADD_ALARM_POLICY = 1 ;//增加告警策略
	public static int REMOVE_ALARM_POLICY = -1 ;//删除告警策略
	public static int MODIFY_ALARM_POLICY = 0 ;//删除告警策略

	
	private NodeMgrFacade nodeMgrFacade ;
	private DataSourceService monitorService ;
	public AlarmPolicyManager(){
		try {
			nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
			monitorService = (DataSourceService) SpringContextServlet.springCtx.getBean("monitorService") ;
		} catch (Exception e) {
			throw new ComponentConfigException("配置下发出错", e) ;
		}
	}
	
	/**
	 * 告警下发入口，主要是预先处理好单级、多级，在自定义和默认情况下发给那些节点
	 * 
	 * @param epmPo
	 * @param nodeMgrFacade
	 * @param type
	 * @param ifRemove
	 * @param ifAdd
	 * @param dataSourceService
	 * @throws ComponentConfigException 
	 */
	public void add(EventPolicyMonitor epmPo) throws ComponentConfigException {
		SimDatasource monitor = monitorService.getById(epmPo.getMonitorId()) ;
		if (monitor != null) {
			epmPo.addNode(new Node(monitor.getAuditorNodeId())) ;
			epmPo.setDeviceIp(monitor.getDeviceIp()) ;
			sendToNode(epmPo, ADD_ALARM_POLICY);
		}
	}
	/**
	 * 删除告警策略
	 * @param epm
	 */
	public void delete(EventPolicyMonitor epm){
		SimDatasource monitor = monitorService.getById(epm.getMonitorId()) ;
		if (monitor != null) {
			epm.addNode(new Node(monitor.getAuditorNodeId())) ;
			sendToNode(epm, REMOVE_ALARM_POLICY);
		}
	}
	public void deleteAll(SimDatasource monitor){
		if (monitor != null) {
			AlarmMonitorService service = (AlarmMonitorService) SpringContextServlet.springCtx.getBean("alarmMonitorService") ;
			List<EventPolicyMonitor> allAlarmPolicys = service.getByMonitorId(monitor.getResourceId()) ;
			if (allAlarmPolicys == null) {
				return ;
			}
			Node node = new Node(monitor.getAuditorNodeId());
			boolean sendImmediate ;//如果存在多个告警对象，只有在最后一个告警对象被删除后才下发告警配置
			for(int i=0;i<allAlarmPolicys.size();i++){
				EventPolicyMonitor epm = allAlarmPolicys.get(i) ;
				epm.addNode(node) ;
				sendImmediate = (i == allAlarmPolicys.size() -1) ;
				sendToNode(epm, REMOVE_ALARM_POLICY,sendImmediate);
			}
		}
	}
	/**
	 * 修改告警策略
	 * @param epm
	 */
	public void modify(EventPolicyMonitor epm){
		SimDatasource monitor = monitorService.getById(epm.getMonitorId()) ;
		if (monitor != null) {
			Node node = nodeMgrFacade.getNodeByNodeId(monitor.getAuditorNodeId());
			epm.addNode(node) ;
			epm.setDeviceIp(monitor.getDeviceIp()) ;
			sendToNode(epm, MODIFY_ALARM_POLICY);
		}
	}
	private void sendToNode(EventPolicyMonitor epmPo, int operation) throws ComponentConfigException{
		sendToNode(epmPo, operation, true) ;
	}
	/**
	 * 告警下发
	 * 
	 * @param epmPo
	 *           告警对象
	 * @param nodeMgrFacade
	 *           操作node的dao
	 * @param operation
	 *           操作类型
	 * @throws ComponentConfigException 
	 */
	private void sendToNode(EventPolicyMonitor epmPo, int operation,boolean sendImmediate) throws ComponentConfigException {
		try {
			Set<Node> nodes = epmPo.getNodes();// 1、下发到的节点对象
			if (nodes != null && nodes.size() > 0) {
				for (Node _node : nodes) {
					Node node = nodeMgrFacade.getNodeByNodeId(_node.getNodeId(), false, false, true, true);// 下发节点的所有属性（而不是懒加载）
					Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_ALARM, true);// 2、 下发节点的组件对象
					if (component != null) {
						AlarmConfiguration alarmConfiguration  = nodeMgrFacade.getSegmentConfigByClass(component, AlarmConfiguration.class);// 3、 下发配置封装对象
						if (operation==ADD_ALARM_POLICY) {
							EventPolicy0 eventPolicy0 = createEventPolicy(epmPo);
							addEventPolicy(alarmConfiguration, eventPolicy0) ;
						}else if (operation==REMOVE_ALARM_POLICY) {
							deleteEventPolicy(alarmConfiguration, new EventPolicy0(epmPo.getId())) ;
						}else if(operation == MODIFY_ALARM_POLICY){
							deleteEventPolicy(alarmConfiguration, new EventPolicy0(epmPo.getId())) ;
							addEventPolicy(alarmConfiguration, createEventPolicy(epmPo)) ;
						}
						if(sendImmediate){
							nodeMgrFacade.updateComponentSegmentAndDispatch(component, alarmConfiguration);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ComponentConfigException("告警策略下发出错", e) ;
		}
	}
	/**
	 * 删除告警策略
	 */
	private void deleteEventPolicy(AlarmConfiguration alarmConfiguration,EventPolicy0 eventPolicy){
		List<EventPolicy0> eventPolicys = alarmConfiguration.getEventpolicys();// 旧的下发配置
		if (eventPolicys != null) {
			eventPolicys.remove(eventPolicy) ;
		}
	}
	/**
	 * 增加告警策略
	 * @param alarmConfiguration
	 * @param eventPolicy
	 */
	private void addEventPolicy(AlarmConfiguration alarmConfiguration,EventPolicy0 eventPolicy){
		List<EventPolicy0> eventpolicys = alarmConfiguration.getEventpolicys();
		if(eventpolicys == null){
			eventpolicys = new ArrayList<EventPolicy0>() ;
			alarmConfiguration.setEventpolicys(eventpolicys) ;
		}
		eventpolicys.add(eventPolicy);
	}
	/**
	 * 拷贝属性
	 * 
	 * @param epmPo
	 *           新的下发配置对象EventPolicy
	 * @return 新的下发配置对象EventPolicy0
	 */
	private EventPolicy0 createEventPolicy(EventPolicyMonitor epmPo) {
		// 下发时把设备类型，ip 拼到过滤器中、
		String selector = epmPo.getFilterSql();
		String[] strs = selector.split("\\(",2);
		String dvcTypeAndIp = "(DVC_TYPE='" + epmPo.getSecurityObjectType() + "' AND DVC_ADDRESS='" + epmPo.getDeviceIp() + "' AND DATA_OBJECT_TYPE='monitor' AND ";
		String filterSql = strs[0] + dvcTypeAndIp + "(" + strs[1] +")";
		EventPolicy0 eventPolicy0 = new EventPolicy0(epmPo.getId(),epmPo.getName(),epmPo.getPriority(),epmPo.isStart(),
				epmPo.getSymbol(),epmPo.getEventFrequencyType()*60,epmPo.getFrequency(),filterSql,EventPolicy0.CAT_STATUS);
		eventPolicy0.setDesc(epmPo.getDesc());
		Set<Response> responses = epmPo.getResponses();// 响应对象列表
		if (ObjectUtils.isNotEmpty(responses)) {
			List<Response0> responsels = new ArrayList<Response0>();// po
			for (Response resp : responses) {
				responsels.add(new Response0(resp.getId(),resp.getName(),resp.getCfgKey(),resp.isStart()));
			}
			eventPolicy0.setResponsels(responsels);// 响应
		}
		return eventPolicy0;
	}

}
