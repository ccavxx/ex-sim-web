/*
 * 版权声明:北京天融信科技有限公司，版权所有违者必究
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author zhouxiaohu 
 * 2011-08-03
 * @version 1.0
 */
package com.topsec.tsm.sim.node.service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.Item;
import com.topsec.tal.base.util.config.webitems.SelectItem;
import com.topsec.tsm.node.component.collector.FlowConfiguration;
import com.topsec.tsm.node.component.collector.SnmpConfiguration;
import com.topsec.tsm.node.component.collector.SyslogConfiguration;
import com.topsec.tsm.node.component.handler.ArchiveConfiguration;
import com.topsec.tsm.node.component.handler.ForwardConfiguration;
import com.topsec.tsm.node.component.handler.JMSForwardConfiguration;
import com.topsec.tsm.node.component.service.ArchiveIndexConfiguration;
import com.topsec.tsm.node.component.service.ArchiveLogConfiguration;
import com.topsec.tsm.node.component.service.DbManagerConfiguration;
import com.topsec.tsm.node.component.service.IndexConfiguration;
import com.topsec.tsm.node.component.service.RapidReportConfiguration;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.dao.NodeMgrDao;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.node.ComponentFinder;
import com.topsec.tsm.sim.resource.node.SegmentFinder;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.resource.persistence.Segment;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.SystemConfigDispatcher;
import com.topsec.tsm.sim.util.SystemConfigUtil;
import com.topsec.tsm.sim.util.TalVersionUtil;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;

/**
 * 用于添加SMP、Auditor时的系统配置实现类
 * @author Meteor
 * @version 1.0
 */
public class SystemConfigServiceImpl implements SystemConfigService {
	
	private static final Logger log = LoggerFactory.getLogger(SystemConfigServiceImpl.class);
	
	private NodeMgrDao nodeMgrDao;
	private EventResponseService eventResponseService;
	
	
	@Override
	/**
	 * 第一次启动.SOC节点首先注册,注册后,会调用改接口
	 * 在数据库表里会插入服务器配置的相关记录
	 * @param nodeId
	 */
	public void configSMP(String nodeId) throws Exception{
		if(nodeId==null){
			log.error("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configSMP(),nodeId==null!!!");
			throw new Exception("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configSMP(),nodeId==null!!!");
		}
		
		eventResponseService.deleteResponsesByNodeId(nodeId);
		Node node = nodeMgrDao.getNodeByNodeId(nodeId);
		List<Response> responses = SystemConfigUtil.getInstance().getSystemConfigByKey("SMP");
		
		for (Response response : responses) {
			if("sys_cfg_backup_auto".equalsIgnoreCase(response.getCfgKey())){
				if(TalVersionUtil.TAL_VERSION_STANDARD.equalsIgnoreCase(TalVersionUtil.getInstance().getVersion())){
					Config config = RespCfgHelper.getConfigTemplateDefaultValue(response);
					Block block = config.getBlockbyKey("autoback");
					if(block.getItembyKey("partitionCount") !=null){
						block.removeItem("partitionCount");
					}
					response.setConfig(config.toString());
				}
				
			}
			response.setNode(node);
			//保存到数据库
			eventResponseService.addResponse(response);
		}
	}
	/**
	 * 当Auditor注册时, 会调用该接口,参数(String  nodeId),这时在数据库表里会插入这个Auditor配置的相关记录
	 * @param nodeId
	 * @throws Exception 
	 */
	@Override
	public void configAuditor(Node node,NodeMgrFacade nodeMgrFacade) throws Exception {
		if(node.getNodeId()==null){
			log.error("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configAuditor(),nodeId==null!!!");
			throw new Exception("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configAuditor(),nodeId==null!!!");
		}
		
		eventResponseService.deleteResponsesByNodeId(node.getNodeId());
		List<Response> responses = SystemConfigUtil.getInstance().getSystemConfigByKey("Auditor");
		for (Response response : responses) {
			response.setNode(node);
			if("sys_cfg_store".equals(response.getCfgKey())){
				response =setLogStore(response,node,nodeMgrFacade);
			}else if ("sys_cfg_index".equals(response.getCfgKey())) {
				response =setSysIndex(response,node,nodeMgrFacade);
			}else if ("sys_cfg_sendlog".equals(response.getCfgKey())) {
				response =setLogSend(response,node,nodeMgrFacade);
			}else if ("sys_cfg_jmssendlog".equals(response.getCfgKey())) {
				response =setJMSLogSend(response,node,nodeMgrFacade);
			}else if ("sys_cfg_report".equals(response.getCfgKey())) {
				response =setSysReport(response,node,nodeMgrFacade);
			}
			//保存到数据库
			eventResponseService.addResponse(response);
		}
	}
	/**
	 * 当Service注册时, 会调用该接口,参数(String  nodeId),这时在数据库表里会插入这个Service配置的相关记录
	 * @param nodeId
	 * @throws Exception 
	 */
	@Override
	public void configService(Node node,NodeMgrFacade nodeMgrFacade) throws Exception {
		if(node.getNodeId()==null){
			log.error("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configService(),nodeId==null!!!");
			throw new Exception("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configService(),nodeId==null!!!");
		}
		
		eventResponseService.deleteResponsesByNodeId(node.getNodeId());
		List<Response> responses = SystemConfigUtil.getInstance().getSystemConfigByKey("Service");
		for (Response response : responses) {
			response.setNode(node);
			if("sys_cfg_store".equals(response.getCfgKey())){
				response =setLogStore(response,node,nodeMgrFacade);
//			}else if ("sys_cfg_index".equals(response.getCfgKey())) {
//				response =setSysIndex(response,node,nodeMgrFacade);
//			}else if ("sys_cfg_report".equals(response.getCfgKey())) {
//				response =setSysReport(response,node,nodeMgrFacade);
			}
			//保存到数据库
			eventResponseService.addResponse(response);
		}
	}

	/**
	 * 当Service注册时, 会调用该接口,参数(String  nodeId),这时在数据库表里会插入这个Service配置的相关记录
	 * @param nodeId
	 * @throws Exception 
	 */
	@Override
	public void configReportService(Node node,NodeMgrFacade nodeMgrFacade) throws Exception {
		/* modify by yangxuanjia at 2011-09-04 start */
		if(node.getNodeId()==null){
			log.error("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configService(),nodeId==null!!!");
			throw new Exception("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configService(),nodeId==null!!!");
		}
		
		eventResponseService.deleteResponsesByNodeId(node.getNodeId());
		List<Response> responses = SystemConfigUtil.getInstance().getSystemConfigByKey("ReportService");
		for (Response response : responses) {
			response.setNode(node);
			if ("sys_cfg_report".equals(response.getCfgKey())) {
				response =setSysReport(response,node,nodeMgrFacade);
			}
			//保存到数据库
			eventResponseService.addResponse(response);
		}
	}
	
	@Override
	public void configCollector(Node node, NodeMgrFacade nodeMgrFacade) throws Exception {
		if(node.getNodeId()==null){
			log.error("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configAuditor(),nodeId==null!!!");
			throw new Exception("com.topsec.tsm.tal.ui.report.service.SystemConfigServiceImpl.configAuditor(),nodeId==null!!!");
		}
		
		eventResponseService.deleteResponsesByNodeId(node.getNodeId());
		List<Response> responses = SystemConfigUtil.getInstance().getSystemConfigByKey("Collector");
		for (Response response : responses) {
			if ("sys_cfg_port".equals(response.getCfgKey())) {
				response.setNode(node);
				response =setSysPort(response,node,nodeMgrFacade);
			}else{
				continue ;
			}
			//保存到数据库
			eventResponseService.addResponse(response);
		}		
	}
	/**
	 * 设置日志存储配置信息
	 * @param response
	 * @param node
	 * @param nodeMgrFacade
	 * @return response
	 * @throws Exception
	 */
	private Response setLogStore(Response response,Node node,NodeMgrFacade nodeMgrFacade) throws Exception{
		Config config = RespCfgHelper.getConfigTemplateDefaultValue(response);
		Block archivePath  = config.getBlockbyKey("archive_path");
//		Block archive = config.getBlockbyKey("archive");
		Component logArchiveComponent = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_ARCHIVE, true);
		if(logArchiveComponent!= null){
			ArchiveConfiguration archiveConfiguration=new ArchiveConfiguration();
			archiveConfiguration=nodeMgrFacade.getSegConfigByComAndT(logArchiveComponent,archiveConfiguration);
			archivePath.setItemValue("archive_path", archiveConfiguration.getDataHome());
		}else {
			String[] route = NodeUtil.getAuditorRouteByAgentNode(node);
			Node node_a = nodeMgrDao.getNodeByNodeId(route[route.length-1]);
			logArchiveComponent = nodeMgrFacade.getBindableComponentByType(node_a, NodeDefinition.HANDLER_ARCHIVE, true);
			ArchiveConfiguration archiveConfiguration=new ArchiveConfiguration();
			archiveConfiguration=nodeMgrFacade.getSegConfigByComAndT(logArchiveComponent,archiveConfiguration);
			archivePath.setItemValue("archive_path", archiveConfiguration.getDataHome());
		}
		Component archiveLogComponent = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.SERVICE_ARCHIVELOG, true);
		if(archiveLogComponent != null){
			ArchiveLogConfiguration archiveLogConfiguration = new ArchiveLogConfiguration();
			archiveLogConfiguration = nodeMgrFacade.getSegConfigByComAndT(archiveLogComponent, archiveLogConfiguration);
			archivePath.setItemValue("override", archiveLogConfiguration.getOverride()+"");
			//取消系统日志设置
//			archive.setItemValue("systemlongevity", archiveLogConfiguration.getExpired());
		}		
		response.setConfig(config.toString());
		return response;
	}
	/**
	 * 设置采集器配置信息
	 * @param response
	 * @param node
	 * @param nodeMgrFacade
	 * @return response
	 * @throws Exception
	 */
	private Response setSysPort(Response response,Node node,NodeMgrFacade nodeMgrFacade) throws Exception{
		Config config = RespCfgHelper.getConfigTemplateDefaultValue(response);
		List<String> syslogPorts = new ArrayList<String>();
		List<String> snmpPorts = new ArrayList<String>();
		List<String> flowPorts = new ArrayList<String>();
		//设置syslog端口信息
		SyslogConfiguration syslogConfiguration = (SyslogConfiguration) NodeUtil.findFirstSegmentConfig(node, NodeDefinition.COLLECTOR_SYSLOG, SyslogConfiguration.class) ;
		Block sysLogPort  = config.getBlockbyKey("syslog");
		syslogPorts.add(syslogConfiguration.get_ports().get(0)+"");
		sysLogPort.addItemValue("syslog_port", syslogPorts);

		//设置snmp端口信息
		SnmpConfiguration snmpConfiguration = (SnmpConfiguration) NodeUtil.findFirstSegmentConfig(node, NodeDefinition.COLLECTOR_SNMPTRAP, SnmpConfiguration.class) ;
		Block snmpPort  = config.getBlockbyKey("snmp");
		snmpPorts.add(snmpConfiguration.getPorts().get(0)+"");
		snmpPort.addItemValue("snmp_port", snmpPorts);
		
		//设置netflow端口信息
		FlowConfiguration flowConfiguration = (FlowConfiguration) NodeUtil.findFirstSegmentConfig(node, NodeDefinition.COLLECTOR_NETFLOW, FlowConfiguration.class) ;
		Block netflow  = config.getBlockbyKey("netflow");
		flowPorts.add(flowConfiguration.get_ports().get(0)+"");
		netflow.addItemValue("netflow_port", flowPorts);
		   
		response.setConfig(config.toString());
		return response;
	}
	/**
	 * 设置日志索引配置信息
	 * @param response
	 * @param node
	 * @param nodeMgrFacade
	 * @return response
	 * @throws Exception
	 */
	private Response setSysIndex(Response response,Node node,NodeMgrFacade nodeMgrFacade) throws Exception{
		Config config = RespCfgHelper.getConfigTemplateDefaultValue(response);
		if(config != null){
			Block block= config.getBlockbyKey("index");
			ArchiveIndexConfiguration archiveIndexConfiguration = (ArchiveIndexConfiguration) NodeUtil.findFirstSegmentConfig(node,NodeDefinition.SERVICE_ARCHIVEINDEX, ArchiveIndexConfiguration.class) ;
			if(archiveIndexConfiguration != null){
				block.setItemValue("longevity", archiveIndexConfiguration.getExpired());
				block.setItemValue("archive", archiveIndexConfiguration.getArchiveTime());
			}
			IndexConfiguration indexConfiguration = (IndexConfiguration) NodeUtil.findFirstSegmentConfig(node, NodeDefinition.SERVICE_INDEX, IndexConfiguration.class) ;
			if(indexConfiguration != null){
				block= config.getBlockbyKey("index");
				block.setItemValue("realtimeindex", indexConfiguration.isAuto()+"");
			}		
			
			response.setConfig(config.toString());
		}
		return response;
	}
	/**
	 * 设置日志转发配置信息
	 * @param response
	 * @param node
	 * @param nodeMgrFacade
	 * @return response
	 * @throws Exception
	 */
	private Response setLogSend(Response response,Node node,NodeMgrFacade nodeMgrFacade) throws Exception{
		Config config = RespCfgHelper.getConfigTemplateDefaultValue(response);
		ForwardConfiguration eventFormater = NodeUtil.findFirstSegmentConfig(node, NodeDefinition.HANDLER_SYSLOG_FORWARDER, ForwardConfiguration.class) ;
		Block block= config.getDefaultBlock();
		block.setItemValue("receivers", "");
		block.setItemValue("port",eventFormater.get_port().toString());
		block.setItemValue("frequency",eventFormater.getLimit()+"");
		block.setItemValue("send",eventFormater.isEnable()+"");
		block.setItemValue("filterSql",eventFormater.getFilter());
		
		response.setConfig(config.toString());
		return response;
	}
	/**
	 * 设置Jms日志转发配置信息
	 * @param response
	 * @param node
	 * @param nodeMgrFacade
	 * @return response
	 * @throws Exception
	 */
	private Response setJMSLogSend(Response response,Node node,NodeMgrFacade nodeMgrFacade) throws Exception{
		Config config = RespCfgHelper.getConfigTemplateDefaultValue(response);
		JMSForwardConfiguration eventFormater = NodeUtil.findFirstSegmentConfig(node, NodeDefinition.HANDLER_JMS_FORWARDER, JMSForwardConfiguration.class) ;
		Block block= config.getDefaultBlock();
		block.setItemValue("receivers", "");
		block.setItemValue("port",eventFormater.get_port().toString());
		block.setItemValue("topic",eventFormater.get_topic());
		block.setItemValue("encrypt",eventFormater.isEncrypt()+"");
		block.setItemValue("frequency",eventFormater.getLimit()+"");
		block.setItemValue("send",eventFormater.isEnable()+"");
		block.setItemValue("filterSql",eventFormater.getFilter());
		block.setItemValue("user",eventFormater.getUser());
		block.setItemValue("pass",eventFormater.getPass());
//		String temp = eventFormater.getSenderIp();
//		if(temp!=null&&temp.length()>0)
//			block.setItemValue("senderIp",temp);
//		temp = eventFormater.getSenderName();
//		if(temp!=null&&temp.length()>0)
//			block.setItemValue("senderName",temp);
		
		response.setConfig(config.toString());
		return response;
	}
	/**
	 * 设置报表存贮配置信息
	 * @param response
	 * @param node
	 * @param nodeMgrFacade
	 * @return
	 * @throws Exception
	 */
	private Response setSysReport(Response response,Node node,NodeMgrFacade nodeMgrFacade) throws Exception{
		Config config = RespCfgHelper.getConfigTemplateDefaultValue(response);
		RapidReportConfiguration reportConfiguration = NodeUtil.findFirstSegmentConfig(node, NodeDefinition.SERVICE_RAPIDREPORT, RapidReportConfiguration.class) ;
		if(reportConfiguration != null){
			Block block = config.getBlockbyKey("reportcfg");
			block.setItemValue("passivereport", reportConfiguration.getPassivereport());
			block.setItemValue("activereport", reportConfiguration.getActivereport());
			block.setItemValue("systemreport", reportConfiguration.getSystemreport());
		}
		response.setConfig(config.toString());
		return response;
	}
	
	public NodeMgrDao getNodeMgrDao() {
		return nodeMgrDao;
	}
	public void setNodeMgrDao(NodeMgrDao nodeMgrDao) {
		this.nodeMgrDao = nodeMgrDao;
	}
	public EventResponseService getEventResponseService() {
		return eventResponseService;
	}
	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}
	
}
