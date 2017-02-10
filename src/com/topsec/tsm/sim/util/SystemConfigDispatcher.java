/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 周小虎
* @since  2011-07-26
* @version 1.0
* 
*/
package com.topsec.tsm.sim.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.Item;
import com.topsec.tal.base.util.config.webitems.ListInputItem;
import com.topsec.tal.base.util.config.webitems.SelectItem;
import com.topsec.tsm.action.config.MailServerConfiguration;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Ipv4Address;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.node.component.collector.FlowConfiguration;
import com.topsec.tsm.node.component.collector.SnmpConfiguration;
import com.topsec.tsm.node.component.collector.SyslogConfiguration;
import com.topsec.tsm.node.component.handler.ArchiveConfiguration;
import com.topsec.tsm.node.component.handler.AutoProtectConfigration;
import com.topsec.tsm.node.component.handler.ForwardConfiguration;
import com.topsec.tsm.node.component.handler.JMSForwardConfiguration;
import com.topsec.tsm.node.component.service.ArchiveIndexConfiguration;
import com.topsec.tsm.node.component.service.ArchiveLogConfiguration;
import com.topsec.tsm.node.component.service.DbManagerConfiguration;
import com.topsec.tsm.node.component.service.IndexConfiguration;
import com.topsec.tsm.node.component.service.RapidReportConfiguration;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.util.SystemInfoUtil;
/**
 * 系统配置下发类
 * @author Meteor
 */
public class SystemConfigDispatcher {
	private static SystemConfigDispatcher dispatcher = null;
	public static SystemConfigDispatcher getInstance(){
		if(dispatcher == null)
			dispatcher = new SystemConfigDispatcher();
		return dispatcher;
	}
	
	/**
	 * @category 邮件服务器下发
	 * @param Config,NodeMgrFacade,Response
	 * @return NULL
	 */
	public void sendMail(Config config,NodeMgrFacade nodeMgrFacade,Response resp,String userName,HttpServletRequest request){
		try {
			List<Node> actionNodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_ACTION, false, false, true, true) ;
			for(Node node:actionNodes){
				Component component = NodeUtil.findFirstComponent(node, NodeDefinition.HANDLER_MAILACTION) ;
				if(component == null){
					continue ;
				}
				MailServerConfiguration mailServerConfiguration = NodeUtil.findFirstSegmentConfig(component, null, MailServerConfiguration.class);
				synMailServerConfig(config, mailServerConfiguration) ;
				nodeMgrFacade.updateComponentSegmentAndDispatch(component, mailServerConfiguration);
				toLog(AuditCategoryDefinition.SYS_UPDATE, "邮件服务器下发", "邮件服务器下发成功",userName, true,Severity.LOW,request.getRemoteHost());
			}
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "邮件服务器下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
			e.printStackTrace();
		}
	}
	public static void synMailServerConfig(Config config,MailServerConfiguration mailServerConfiguration){
		Block defaultBlock = config.getDefaultBlock() ;
		mailServerConfiguration.setIp(defaultBlock.getItemValue("serverip"));
		mailServerConfiguration.setPort(defaultBlock.getItemValue("serverport"));
		mailServerConfiguration.setSender(defaultBlock.getItemValue("mailsender"));
		mailServerConfiguration.setName(defaultBlock.getItemValue("loginaccount"));
		mailServerConfiguration.setPassword(defaultBlock.getItemValue("loginpwd"));
		mailServerConfiguration.setSsl(StringUtil.booleanVal(defaultBlock.getItemValue("ssl"))) ;
	}
	/**
	 * 采集器端口配置下发
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendPort(Config config,NodeMgrFacade nodeMgrFacade,Response resp,HttpServletRequest request,String userName){
		try {
			List<Block> blocks = config.getCfgBlocks();
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(),false,false,true,true);
			for (Block block : blocks) {
				if("syslog".equals(block.getKey())){
					Component component = NodeUtil.findFirstComponent(node,NodeDefinition.COLLECTOR_SYSLOG);
					SyslogConfiguration syslogConfiguration = NodeUtil.findFirstSegmentConfig(component, null, SyslogConfiguration.class);
					for (Item item : block.getCfgItems()) {
						if (item instanceof ListInputItem) {
//							String selectListInput = (String) request.getParameter(config.getKey() + "." + block.getKey() + ".selectListInput");
							List<Integer> ports = new ArrayList<Integer>();
//							if ((selectListInput != null) && (selectListInput.length() != 0)) {
//								String[] selectedFiels = selectListInput.split(" ");
//								for (int i = 1; i < selectedFiels.length; i++) {
//									ports.add(new Integer(selectedFiels[i]));
//								}
//								syslogConfiguration.set_ports(ports);
//							}
							for(String port : item.getValueList()){
								ports.add(Integer.parseInt(port));
							}
							syslogConfiguration.set_ports(ports);
						}
					}
					nodeMgrFacade.updateComponentSegmentAndDispatch(component, syslogConfiguration);
				}
				if("snmp".equals(block.getKey())){
					Component component = NodeUtil.findFirstComponent(node,NodeDefinition.COLLECTOR_SNMPTRAP);
					SnmpConfiguration snmpConfiguration = NodeUtil.findFirstSegmentConfig(component, null,SnmpConfiguration.class);
					for (Item item : block.getCfgItems()) {
						if (item instanceof ListInputItem) {
//							String selectListInput = (String) request.getParameter(config.getKey() + "." + block.getKey() + ".selectListInput");
							List<Integer> ports = new ArrayList<Integer>();
//							if ((selectListInput != null) && (selectListInput.length() != 0)) {
//								String[] selectedFiels = selectListInput.split(" ");
//								for (int i = 1; i < selectedFiels.length; i++) {
//									ports.add(new Integer(selectedFiels[i]));
//								}
//								snmpConfiguration.setPorts(ports);
//							}
							for(String port : item.getValueList()){
								ports.add(Integer.parseInt(port));
							}
							snmpConfiguration.setPorts(ports);
						}
					}
					nodeMgrFacade.updateComponentSegmentAndDispatch(component, snmpConfiguration);
				}
				if("netflow".equals(block.getKey())){
					Component component = NodeUtil.findFirstComponent(node,NodeDefinition.COLLECTOR_NETFLOW);
					FlowConfiguration flowConfiguration = new FlowConfiguration();
					flowConfiguration = NodeUtil.findFirstSegmentConfig(component,null, FlowConfiguration.class);
					for (Item item : block.getCfgItems()) {
						if (item instanceof ListInputItem) {
//							String selectListInput = (String) request.getParameter(config.getKey() + "." + block.getKey() + ".selectListInput");
							List<Integer> ports = new ArrayList<Integer>();
//							if ((selectListInput != null) && (selectListInput.length() != 0)) {
//								String[] selectedFiels = selectListInput.split(" ");
//								for (int i = 1; i < selectedFiels.length; i++) {
//									ports.add(new Integer(selectedFiels[i]));
//								}
//								flowConfiguration.set_ports(ports);
//							}
							for(String port : item.getValueList()){
								ports.add(Integer.parseInt(port));
							}
							flowConfiguration.set_ports(ports);
						}
					}
					nodeMgrFacade.updateComponentSegmentAndDispatch(component, flowConfiguration);
				}
				
			}
			
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "采集器端口配置下发", "采集器端口配置下发成功",userName, true,Severity.LOW,request.getRemoteHost());
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "采集器端口配置下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
			e.printStackTrace();
		}
	}
	
	/**
	 * 日志存储下发策略
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 * @param request
	 */
	public void sendSyslogStore(Config config,NodeMgrFacade nodeMgrFacade,Response resp,HttpServletRequest request,String userName){
		try {
			sendSyslogStore(config, nodeMgrFacade, resp) ;
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志存储策略配置下发", "日志存储策略配置下发成功",userName, true,Severity.LOW,request.getRemoteHost());
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志存储策略配置下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
			e.printStackTrace();
		}
	}
	
	public void sendSyslogStore(Config config,NodeMgrFacade nodeMgrFacade,Response resp) throws NodeException{
		String nodeid = resp.getNode().getNodeId();
		Node node = nodeMgrFacade.getNodeByNodeId(nodeid,false,true,true,true);
		if(NodeDefinition.NODE_TYPE_AUDIT.equals(node.getType())){
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
					nodeid = child.getNodeId();
					break;
				}
			}
			node = nodeMgrFacade.getNodeByNodeId(nodeid,false,false,true,true);
		}
		Component component = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_ARCHIVELOG);
		ArchiveLogConfiguration archiveLogConfiguration = new ArchiveLogConfiguration();
		archiveLogConfiguration = NodeUtil.findFirstSegmentConfig(component,null,ArchiveLogConfiguration.class);
		Block archive  = config.getBlockbyKey("archive");
		//archiveLogConfiguration.setExpired(archive.getItemValue("systemlongevity"));
		Block archivePath  = config.getBlockbyKey("archive_path");
		archiveLogConfiguration.setOverride(new Integer(archivePath.getItemValue("override")));
		String alert = archivePath.getItemValue("alert");
		if(alert != null) {
			archiveLogConfiguration.setAlert(Integer.valueOf(alert));
			SystemInfoUtil.getInstance().setData_limit(Integer.valueOf(alert));
		}
		nodeMgrFacade.updateComponentSegmentAndDispatch(component, archiveLogConfiguration);
	}
	
	/**
	 * 日志索引策略配置下发
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 * @param request
	 */
	public void sendLogIndex(Config config,NodeMgrFacade nodeMgrFacade,Response resp,HttpServletRequest request,String userName){
		try {
			
			String nodeid = resp.getNode().getNodeId();
			Node node = nodeMgrFacade.getNodeByNodeId(nodeid,false,true,true,true);
			if(NodeDefinition.NODE_TYPE_AUDIT.equals(node.getType())){
				Set<Node> children = node.getChildren();
				for(Node child:children){
					String type = child.getType();
					if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
						nodeid = child.getNodeId();
						break;
					}
				}
				node = nodeMgrFacade.getNodeByNodeId(nodeid,false,false,true,true);
			}
			Component componentArchive = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_ARCHIVEINDEX);
			ArchiveIndexConfiguration archiveIndexConfiguration = new ArchiveIndexConfiguration();
			archiveIndexConfiguration = NodeUtil.findFirstSegmentConfig(componentArchive,null, ArchiveIndexConfiguration.class);

			Component componentIndex = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_INDEX);
			IndexConfiguration indexConfiguration = new IndexConfiguration();
			indexConfiguration = NodeUtil.findFirstSegmentConfig(componentIndex,null, IndexConfiguration.class);
			
			Block block= config.getBlockbyKey("index");
			if("index".equals(block.getKey())){
				archiveIndexConfiguration.setExpired(block.getItemValue("longevity"));
				archiveIndexConfiguration.setArchiveTime(block.getItemValue("archive"));
				indexConfiguration.setAuto(Boolean.valueOf(block.getItemValue("realtimeindex")));
			}
			nodeMgrFacade.updateComponentSegmentAndDispatch(componentArchive, archiveIndexConfiguration);
			nodeMgrFacade.updateComponentSegmentAndDispatch(componentIndex, indexConfiguration);

    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志索引策略配置下发", "日志索引策略配置下发成功",userName, true,Severity.LOW,request.getRemoteHost());
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志索引策略配置下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
			e.printStackTrace();
		}
		
	}
	/**
	 * 日志存储路径配置下发
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 * @param request
	 */
	public void sendLogPath(Config config,NodeMgrFacade nodeMgrFacade,Response resp,HttpServletRequest request,String userName){
		try {
			sendLogPath(config, nodeMgrFacade, resp) ;
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志存储路径配置下发", "日志存储路径配置下发成功", userName, true,Severity.LOW,request.getRemoteHost());
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志存储路径配置下发", e.getMessage(), userName, false,Severity.HIGH,request.getRemoteHost());
			Log.error("sendLogPath"+e.getMessage());
		}
	}
	public void sendLogPath(Config config,NodeMgrFacade nodeMgrFacade,Response resp) throws NodeException{
		String nodeid = resp.getNode().getNodeId();
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration();
		IndexConfiguration indexConfiguration = new IndexConfiguration();
		Component componentHandler = null;
		Component componentService = null;
			
		Node node = nodeMgrFacade.getNodeByNodeId(nodeid,false,true,true,true);
		if(NodeDefinition.NODE_TYPE_AUDIT.equals(node.getType())){
			componentHandler = NodeUtil.findFirstComponent(node, NodeDefinition.HANDLER_ARCHIVE);
			archiveConfiguration = NodeUtil.findFirstSegmentConfig(componentHandler,null, ArchiveConfiguration.class);

			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
					nodeid = child.getNodeId();
					break;
				}
			}
			node = nodeMgrFacade.getNodeByNodeId(nodeid,false,false,true,true);
			componentService = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_INDEX);
			indexConfiguration = NodeUtil.findFirstSegmentConfig(componentService,null, IndexConfiguration.class);
		}else{
			componentService = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_INDEX);
			indexConfiguration = NodeUtil.findFirstSegmentConfig(componentService,null, IndexConfiguration.class);

			String route[] = NodeUtil.getRoute(node);
			node = nodeMgrFacade.getNodeByNodeId(route[route.length-2],false,false,true,true);
			
			componentHandler = NodeUtil.findFirstComponent(node, NodeDefinition.HANDLER_ARCHIVE);
			archiveConfiguration = NodeUtil.findFirstSegmentConfig(componentHandler,null, ArchiveConfiguration.class);

			
		}
		Block archive_path  = config.getBlockbyKey("archive_path");
		
		if("archive_path".equalsIgnoreCase(archive_path.getKey())){
			archiveConfiguration.setDataHome(archive_path.getItemValue("archive_path"));
			archiveConfiguration.setArchivePathList(archive_path.getItemValue("archive_path_list")) ;
			indexConfiguration.setDatahome(archive_path.getItemValue("archive_path"));
			indexConfiguration.setArchivePathList(archive_path.getItemValue("archive_path_list")) ;
			System.setProperty("DataHome",archive_path.getItemValue("archive_path"));
			SystemInfoUtil.getInstance().setData_home(archive_path.getItemValue("archive_path"));
		}
		nodeMgrFacade.updateComponentSegmentAndDispatch(componentHandler, archiveConfiguration);
		nodeMgrFacade.updateComponentSegmentAndDispatch(componentService, indexConfiguration);
	}
	public void sendLogProtection(NodeMgrFacade nodeMgrFacade,boolean enabled,Config config){
		Block archivePathBlock  = config.getBlockbyKey("archive_path");
		String archivePathList = archivePathBlock.getItemValue("archive_path_list") ;
		Node node = nodeMgrFacade.getKernelAuditor(false, false, true, true) ;
		Component protectComp = NodeUtil.findFirstComponent(node, NodeDefinition.HANDLER_AUTOPROTECT) ;
		AutoProtectConfigration protectConfig = NodeUtil.findFirstSegmentConfig(protectComp, null, AutoProtectConfigration.class);
		if(protectConfig == null){
			protectConfig = new AutoProtectConfigration() ;
		}
		protectConfig.setEnableProtected(enabled) ;
		protectConfig.setProtectedPaths(archivePathList) ;
		try {
			nodeMgrFacade.updateComponentSegmentAndDispatch(protectComp, protectConfig) ;
		} catch (NodeException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Syslog日志转发配置下发
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 * @param request
	 */
	public void sendLogSend(Config config,NodeMgrFacade nodeMgrFacade,Response resp,HttpServletRequest request,String userName){
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(),false,false,true,true);
    		Component component = NodeUtil.findFirstComponent(node, NodeDefinition.HANDLER_SYSLOG_FORWARDER) ;
			ForwardConfiguration eventFormater = new ForwardConfiguration();
			eventFormater = NodeUtil.findFirstSegmentConfig(component,null, ForwardConfiguration.class);
			Block block = config.getCfgBlocks().get(0);
			String[] ipStrs = StringUtil.split(block.getItemValue("receivers"));
			List<InetAddress> ipList = new ArrayList<InetAddress>();
			for(String ipStr:ipStrs) {
				if(StringUtil.isBlank(ipStr)){
					continue ;
				}
				if(!Ipv4Address.validIPv4(ipStr.trim())){
					throw new CommonUserException("无效的IP地址:"+ipStr) ;
				}
				ipList.add(InetAddress.getByName(ipStr));
			}
			eventFormater.setIpList(ipList);
			eventFormater.set_port(StringUtil.toInt(block.getItemValue("port"), 514)) ;
			eventFormater.setLimit(StringUtil.toInt(block.getItemValue("frequency"), 1000));
			eventFormater.setEnable(StringUtil.booleanVal(block.getItemValue("send")));
			eventFormater.setFilter(block.getItemValue("filterSql"));
			eventFormater.setOnlyRawLog(StringUtil.booleanVal(block.getItemValue("onlyRawLog"))) ;
			eventFormater.setSpliter(block.getItemValue("spliter"));
			eventFormater.setPrefix(block.getItemValue("prefix"));
			eventFormater.setIncludeLogDeviceIp(StringUtil.booleanVal(block.getItemValue("isIncludeLogDeviceIp")));
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, eventFormater);
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "Syslog日志转发配置下发", "Syslog日志转发配置下发成功",userName, true,Severity.LOW,request.getRemoteHost());
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "Syslog日志转发配置下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
			e.printStackTrace();
		}
	}
	/**
	 * JMS日志转发配置下发
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 * @param request
	 */
	public void sendJMSLogSend(Config config,NodeMgrFacade nodeMgrFacade,Response resp,HttpServletRequest request,String userName){
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(),false,false,true,true);
    		Component component = NodeUtil.findFirstComponent(node, NodeDefinition.HANDLER_JMS_FORWARDER) ;
			JMSForwardConfiguration eventFormater = new JMSForwardConfiguration();
			eventFormater = NodeUtil.findFirstSegmentConfig(component,null, JMSForwardConfiguration.class);

			List<Item> cfgItems = config.getCfgBlocks().get(0).getCfgItems();
			for (Item item : cfgItems) {
				if(StringUtils.isBlank(item.getValue())){
					continue;
				}
				if(item.getKey().equalsIgnoreCase("receivers")) {
					String ipStrs = StringUtils.trim(item.getValue());
					eventFormater.set_ip(InetAddress.getByName(ipStrs));
				}else if (item.getKey().equalsIgnoreCase("port")) {
					eventFormater.set_port(Integer.valueOf(item.getValue()));
				}else if (item.getKey().equalsIgnoreCase("topic")) {
					eventFormater.set_topic(item.getValue());
				}else if (item.getKey().equalsIgnoreCase("encrypt")) {
					eventFormater.setEncrypt(Boolean.valueOf(item.getValue()));
				}else if(item.getKey().equalsIgnoreCase("frequency")) {
					eventFormater.setLimit(Integer.valueOf(item.getValue()));
				}else if(item.getKey().equalsIgnoreCase("send")) {
					eventFormater.setEnable(Boolean.valueOf(item.getValue()));
				}else if(item.getKey().equalsIgnoreCase("filterSql")) {
					eventFormater.setFilter(item.getValue());
				}else if(item.getKey().equalsIgnoreCase("user")) {
					eventFormater.setUser(item.getValue());
				}else if(item.getKey().equalsIgnoreCase("pass")) {
					eventFormater.setPass(item.getValue());
//				}else if(item.getKey().equalsIgnoreCase("senderIp")) {
//					eventFormater.setPass(item.getValue());
//				}else if(item.getKey().equalsIgnoreCase("senderName")) {
//					eventFormater.setPass(item.getValue());
				}
				
			}
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, eventFormater);
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "JMS日志转发配置下发", "JMS日志转发配置下发成功",userName, true,Severity.LOW,request.getRemoteHost());
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "JMS日志转发配置下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
			e.printStackTrace();
		}
	}
	/**
	 * 报表存储策略配置下发
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 * @param request
	 */
	public void sendReport(Config config,NodeMgrFacade nodeMgrFacade,Response resp,HttpServletRequest request,String userName){
		try {
			
			String nodeid = resp.getNode().getNodeId();
			Node node = nodeMgrFacade.getNodeByNodeId(nodeid,false,true,true,true);
			Set<Node> children = node.getChildren();
			for(Node child:children){
				String type = child.getType();
				if(NodeDefinition.NODE_TYPE_REPORTSERVICE.equals(type)){
					nodeid = child.getNodeId();
					break;
				}
			}
			node = nodeMgrFacade.getNodeByNodeId(nodeid,false,false,true,true);
    		Component component = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_RAPIDREPORT) ;
			RapidReportConfiguration reportConfiguration= new RapidReportConfiguration();
			reportConfiguration = NodeUtil.findFirstSegmentConfig(component,null,RapidReportConfiguration.class);
			List<Item> cfgItems = config.getCfgBlocks().get(0).getCfgItems();
			for (Item item : cfgItems) {
				if (item instanceof SelectItem) {
					if("passivereport".equals(item.getKey())){
						reportConfiguration.setPassivereport(item.getValue());
					}else if("activereport".equals(item.getKey())){
						reportConfiguration.setActivereport(item.getValue());
					}else if("systemreport".equals(item.getKey())){
						reportConfiguration.setSystemreport(item.getValue());
					}
					
				}
			}
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, reportConfiguration);
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "报表存储策略配置下发", "报表存储策略配置下发成功",userName, true,Severity.LOW,request.getRemoteHost());
		} catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "报表存储策略配置下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
			e.printStackTrace();
		}
	}

    /**
     * 日志备份路径下发
     * @param config
     * @param nodeMgrFacade
     * @param resp
     * @param request: 可能是null
     */
    public void sendLogBackupPath(Config config, NodeMgrFacade nodeMgrFacade, Response resp, HttpServletRequest request,String userName) {
    	Block backupPath  = config.getBlockbyGroup("backuppath");
        try {
        	List<Node> auditorNodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, true, true, true);
        	if(auditorNodes == null || auditorNodes.isEmpty()){
        		return ;
        	}
			String localIp = IpAddress.getLocalIp().getLocalhostAddress();
        	for(Node node : auditorNodes){
    			String nodeid = node.getNodeId();
    			Set<Node> children = node.getChildren();
    			for(Node child:children){
    				String type = child.getType();
    				if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
    					nodeid = child.getNodeId();
    					break;
    				}
    			}
    			node = nodeMgrFacade.getNodeByNodeId(nodeid,false,false,true,true);
        		//核心auditor的情况，向DbManager下发一次

        		if(localIp.equals(node.getIp())){
        			Component component = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_DBMANAGER) ;
        			if(component !=null){
        				DbManagerConfiguration dbManagerConfiguration = new DbManagerConfiguration();
            			dbManagerConfiguration = NodeUtil.findFirstSegmentConfig(component,null, DbManagerConfiguration.class);
            			if(dbManagerConfiguration !=null){
            				if("local" .equalsIgnoreCase(backupPath.getKey())){
            					dbManagerConfiguration.setIsLocalpath(true);
            					dbManagerConfiguration.setBackUpLocalPath(backupPath.getItemValue("path"));
                            }else if("ftp" .equalsIgnoreCase(backupPath.getKey())) {
                            	dbManagerConfiguration.setIsLocalpath(false);
                            	dbManagerConfiguration.setBackUpFtpIp(backupPath.getItemValue("serverip"));
                            	dbManagerConfiguration.setFtpUser(backupPath.getItemValue("user"));
                            	dbManagerConfiguration.setFtpPassword(backupPath.getItemValue("password"));
                            	dbManagerConfiguration.setEncoding(backupPath.getItemValue("encoding"));
                            }
            				nodeMgrFacade.updateComponentSegmentAndDispatch(component, dbManagerConfiguration);
            			}
        			}
        		}
        		
        		
        		Component component = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_ARCHIVELOG) ;
    			if(component == null){
    				continue;
    			}
                ArchiveLogConfiguration archiveLogConfiguration = NodeUtil.findFirstSegmentConfig(component, null, ArchiveLogConfiguration.class) ;
                
                //备份路径赋值
                if(archiveLogConfiguration !=null){
                	if("local" .equalsIgnoreCase(backupPath.getKey())){
                    	archiveLogConfiguration.setType(backupPath.getKey());
                    	archiveLogConfiguration.setHost(backupPath.getItemValue("path"));
                    }else if("ftp" .equalsIgnoreCase(backupPath.getKey())) {
                    	archiveLogConfiguration.setType(backupPath.getKey());
                    	archiveLogConfiguration.setHost(backupPath.getItemValue("serverip"));
                    	archiveLogConfiguration.setUser(backupPath.getItemValue("user"));
                    	archiveLogConfiguration.setPassword(backupPath.getItemValue("password"));
                    }               	
                    nodeMgrFacade.updateComponentSegmentAndDispatch(component, archiveLogConfiguration);
                }
        	}
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "下发日志备份路径", "下发日志备份路径成功",userName, true,Severity.LOW,request.getRemoteHost());
        } catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "下发日志备份路径", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
            e.printStackTrace();
        }
    }

    /**
     * 日志自动备份策略下发
     * @param config
     * @param nodeMgrFacade
     * @param resp
     * @param request
     */
    public void sendLogBackupAuto(Config config, NodeMgrFacade nodeMgrFacade,
            Response resp, HttpServletRequest request,String userName) {
        try {
            //Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(),false,false,true,true);
        	List<Node> auditorNodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, true, true, true);
        	if(auditorNodes == null || auditorNodes.isEmpty()){
        		return ;
        	}
        	for(Node node : auditorNodes){
        		String nodeid = node.getNodeId();
    			Set<Node> children = node.getChildren();
    			for(Node child:children){
    				String type = child.getType();
    				if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
    					nodeid = child.getNodeId();
    					break;
    				}
    			}
    			node = nodeMgrFacade.getNodeByNodeId(nodeid,false,false,true,true);
        		Component component = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_ARCHIVELOG) ;
    			if(component == null){
    				continue;
    			}
                ArchiveLogConfiguration archiveLogConfiguration = NodeUtil.findFirstSegmentConfig(component, null, ArchiveLogConfiguration.class) ;
                Block autobackBlock  = config.getBlockbyKey("autoback");
                if(archiveLogConfiguration!=null){
                	if("true".equals(autobackBlock.getItemValue("enable"))){
                    	if("1m".equals(autobackBlock.getItemValue("autobackManner"))){
                    		archiveLogConfiguration.setBackupType("1");
                    	}else if ("2m".equals(autobackBlock.getItemValue("autobackManner"))){
                    		archiveLogConfiguration.setBackupType("2");
                    	}else if ("3m".equals(autobackBlock.getItemValue("autobackManner"))){
                    		archiveLogConfiguration.setBackupType("3");
                    	}else if ("4m".equals(autobackBlock.getItemValue("autobackManner"))){
                    		archiveLogConfiguration.setBackupType("4");
                    	}else if ("5m".equals(autobackBlock.getItemValue("autobackManner"))){
                    		archiveLogConfiguration.setBackupType("5");
                    	}else if ("6m".equals(autobackBlock.getItemValue("autobackManner"))){
                    		archiveLogConfiguration.setBackupType("6");
                    	}else if ("1y".equals(autobackBlock.getItemValue("autobackManner"))){
                    		archiveLogConfiguration.setBackupType("12");
                    	}
    //                	if("lastday".equals(autobackBlock.getItemValue("autobackManner"))){
    //                		archiveLogConfiguration.setBackupType("1");
	//                	}else if ("lastweek".equals(autobackBlock.getItemValue("autobackManner"))){
	//                		archiveLogConfiguration.setBackupType("2");
	//                	}else if ("lastmonth".equals(autobackBlock.getItemValue("autobackManner"))){
	//                		archiveLogConfiguration.setBackupType("3");
	//                	}
                    }else{
                    	archiveLogConfiguration.setBackupType("0");
                    }
                    nodeMgrFacade.updateComponentSegmentAndDispatch(component, archiveLogConfiguration);
                }
        	}
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志自动备份策略下发", "日志自动备份策略下发成功",userName, true,Severity.LOW,request.getRemoteHost());
        } catch (Exception e) {
    		toLog(AuditCategoryDefinition.SYS_UPDATE, "日志自动备份策略下发", e.getMessage(),userName, false,Severity.HIGH,request.getRemoteHost());
            e.printStackTrace();
        }
        
        
    }
    
    /**
     * 事件自动备份策略下发
     * @param config
     * @param nodeMgrFacade
     * @param resp
     * @param request
     */
    public void sendEventBackupAuto(Config config, NodeMgrFacade nodeMgrFacade,
            Response resp, HttpServletRequest request) {
        try {
//            Node node1 = nodeMgrFacade.getKernelAuditor(false);
        	List<Node> auditorNodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, true, true, true);
        	if(auditorNodes == null || auditorNodes.isEmpty()){
        		return ;
        	}
    		String localIp = IpAddress.getLocalIp().getLocalhostAddress() ;
        	for(Node node : auditorNodes){
        		//核心auditor的情况，向DbManager下发一次
	    		if(localIp.equals(node.getIp())){
	    			String nodeid = node.getNodeId();
	    			Set<Node> children = node.getChildren();
	    			for(Node child:children){
	    				String type = child.getType();
	    				if(NodeDefinition.NODE_TYPE_INDEXSERVICE.equals(type)){
	    					nodeid = child.getNodeId();
	    					break;
	    				}
	    			}
	    			node = nodeMgrFacade.getNodeByNodeId(nodeid,false,false,true,true);
					Component component = NodeUtil.findFirstComponent(node, NodeDefinition.SERVICE_DBMANAGER);
					DbManagerConfiguration dbManagerConfiguration = NodeUtil.findFirstSegmentConfig(component, null, DbManagerConfiguration.class) ;
			        Block autobackBlock  = config.getBlockbyKey("autoback");
			        if(dbManagerConfiguration != null){
			        	if("true".equals(autobackBlock.getItemValue("enable"))){
			        		String backupPeriod = autobackBlock.getItemValue("autobackManner");
			        		char unitChar = backupPeriod.charAt(backupPeriod.length()-1) ; //单位m,y
			        		int num = StringUtil.toInt(backupPeriod.substring(0,backupPeriod.length()-1)) ;
			        		int unit = unitChar == 'y' ? 12 : 1 ;
			        		dbManagerConfiguration.setBackupType(String.valueOf(num*unit));
			            }else{
			            	dbManagerConfiguration.setBackupType("0");  
			            }
		        		dbManagerConfiguration.setReservenum(autobackBlock.getItemValue("partitionCount"));
			            nodeMgrFacade.updateComponentSegmentAndDispatch(component, dbManagerConfiguration);
			        }
	    		}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
	
	
	private void toLog(String cat, String name, String desc, String subject, boolean result, Severity severity,String remoteIP ) {
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(cat);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(subject);
		_log.setSubjectAddress(new IpAddress(remoteIP));
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(result);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}
	
}
