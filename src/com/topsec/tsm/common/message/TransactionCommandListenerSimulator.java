package com.topsec.tsm.common.message;

import java.io.Serializable;

import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.comm.CommandDispatcher;
import com.topsec.tsm.node.comm.CommandHandle;
import com.topsec.tsm.node.comm.CommandListener;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.LoginUserCache;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.log.service.ScheduleStatTaskService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.service.SimNodeUpgradeService;
import com.topsec.tsm.sim.sysconfig.service.EventRuleService;
import com.topsec.tsm.tal.service.EventResponseService;
 
public class TransactionCommandListenerSimulator{
	private NodeMgrFacade nodeMgrFacade;
	private DataSourceService dataSourceService;
	private DataSourceService monitorService ;
	private SimNodeUpgradeService simNodeUpgradeService;
	private EventResponseService eventResponseService;
	private EventRuleService eventRuleService ;
	private LogReportTaskService logReportTaskService ;
	private ScheduleStatTaskService scheduleStatTaskService ;
	class MyTransactionCommandListener implements CommandListener {
		
		public Serializable onCommand(String command, Serializable obj,String[] route, CommandHandle handle){	
			if(command.equals(MessageDefinition.CMD_NODE_ACTIVEUSR)){
				return LoginUserCache.getInstance().getOnlineUserCount() > 0 ;
			}else if (command.equals(MessageDefinition.CMD_GET_DATASOURCE_BLACKLIST)){  //获取日志源黑名单
				return CommandHandlerUtil.handleDataSourceBlankList(dataSourceService) ;				
			}else if(command.equals(MessageDefinition.CMD_NODE_QUERY_DATASOURCE)){  //获取日志源
				return CommandHandlerUtil.handleQueryDataSource(obj, nodeMgrFacade, dataSourceService) ;
			}else if(command.equals(MessageDefinition.CMD_NODE_QUERY_PARENTNODE)){  //获取父节点
				return CommandHandlerUtil.handleQueryParentNode(nodeMgrFacade);
			}else if(command.equals(MessageDefinition.CMD_NODE_QUERY_PATCH)){  //请求升级补丁最新版本号
				return CommandHandlerUtil.handleQueryPatch(obj, simNodeUpgradeService) ;
			}else if (command.equals(MessageDefinition.CMD_GET_DATAHOME)){  //获取存储根目录
				return CommandHandlerUtil.handleGetDataHome(obj,nodeMgrFacade,eventResponseService) ;				
			}else if (command.equals(MessageDefinition.CMD_NODE_QUERY_BACKUP_CONFIG)){  //获取备份配置
				return CommandHandlerUtil.handleQueryBackConfig(obj,eventResponseService) ;				
			}else if (command.equals(MessageDefinition.CMD_UPDATE_VERSION)){  //节点升级后,更新soc端sim_node表里节点的版本信息
				return CommandHandlerUtil.handleUpdateVersion(obj, nodeMgrFacade) ;	
			}else if (command.equals(MessageDefinition.CMD_QUERY_SERVER_INFO)){  //得到系统信息
				return CommandHandlerUtil.handleQueryServerInfo(obj, nodeMgrFacade) ;
			}else if (command.equals(MessageDefinition.CMD_GET_RULE_USERDEFINE)){  //下发事件规则
				return CommandHandlerUtil.handleGetUserRule(obj, eventRuleService,eventResponseService) ;
			}else if (command.equals(MessageDefinition.CMD_NODE_ALIVE)){  //获取节点存活状态信息
				return CommandHandlerUtil.handleNodeAlive(obj) ;	
			}else if(command.equals(MessageDefinition.CMD_DEVICE_SUBMIT)){
				return CommandHandlerUtil.handleDeviceSubmit(obj) ;
			}else if(command.equals(MessageDefinition.CMD_ASSET_STATUS)){
				return CommandHandlerUtil.handleAssetStatus(obj) ;
			}else if(command.equals(MessageDefinition.CMD_GET_DEVICE_LIST)){
				return CommandHandlerUtil.handleGetDeviceList(nodeMgrFacade,obj) ;
			}else if(command.equals(MessageDefinition.CMD_LOG_STATISTIC_RESULT)){
				return CommandHandlerUtil.handleLogStatisticResult(obj,logReportTaskService) ;
			}else if(command.equals(MessageDefinition.CMD_LOG_STAT_TASK_LIST)){
				return CommandHandlerUtil.handleLogStatTaskList(logReportTaskService) ;
			}else if(command.equals(MessageDefinition.CMD_SUBJECT_STAT_PROGRESS)){
				return CommandHandlerUtil.handleLogStatProgress(obj,logReportTaskService) ;
			}else if(command.equals(MessageDefinition.CMD_CHILD_UPGRADE)){//下级节点升级，取升级包、重启服务
				return CommandHandlerUtil.handleChildUpgrade(obj, nodeMgrFacade) ;
			}else if(command.equals(MessageDefinition.CMD_SCHEDULE_STAT_PROGRESS)){
				return CommandHandlerUtil.handleScheduleStatProcess(scheduleStatTaskService,obj) ;
			}else if(command.equals(MessageDefinition.CMD_SCHEDULE_STAT_RESULT)){
				return CommandHandlerUtil.handleScheduleStatResult(scheduleStatTaskService,obj) ;
			}else if(command.equals(MessageDefinition.CMD_SCHEDULE_EXECUTE)){
				return CommandHandlerUtil.handleScheduleExecute(scheduleStatTaskService,obj) ;
			}else if(command.equals(MessageDefinition.CMD_SCHEDULE_STAT_BEGIN)){
				return CommandHandlerUtil.handleScheduleStatBegin(scheduleStatTaskService,obj) ;
			}else if(command.equals(MessageDefinition.CMD_SCHEDULE_STAT_LIST)){
				return CommandHandlerUtil.handleScheduleStatList(scheduleStatTaskService) ;
			}else if(command.equals(MessageDefinition.CMD_SCHEDULE_STAT_DONE)){
				return CommandHandlerUtil.handleScheduleTaskDone(scheduleStatTaskService, obj) ;
			}else if(command.equals(MessageDefinition.CMD_CHANGE_DATA_HOME)){
				return CommandHandlerUtil.handleChangeDataHome(eventResponseService,nodeMgrFacade,obj) ;
			}else if(command.equals(MessageDefinition.CMD_GET_USER_DATASOURCE)){
				return CommandHandlerUtil.handleGetUserDevices(null,dataSourceService,(String)obj) ;
			}else if(command.equals(MessageDefinition.CMD_NODE_QUERY_MONITOR)){
				return CommandHandlerUtil.handleQueryMonitor(nodeMgrFacade,monitorService,(String)obj) ;
			}
			return null;
		}
		 
	}
	
	public void init() {
		nodeMgrFacade.registerSMP() ;
		String[] cmdsTransaction = new String[] {
				MessageDefinition.CMD_GET_DATASOURCE_BLACKLIST,
				MessageDefinition.CMD_NODE_QUERY_DATASOURCE,
				MessageDefinition.CMD_GET_AUDIT_OBJECT,
				MessageDefinition.CMD_NODE_QUERY_PATCH,
				MessageDefinition.CMD_GET_DATAHOME,
				MessageDefinition.CMD_NODE_QUERY_BACKUP_CONFIG,
				MessageDefinition.CMD_UPDATE_VERSION,
				MessageDefinition.CMD_RULE_QUERY_PATCH,
				MessageDefinition.CMD_QUERY_SERVER_INFO,
				MessageDefinition.CMD_GET_RULE_USERDEFINE,
				MessageDefinition.CMD_NODE_ALIVE,
				MessageDefinition.CMD_DEVICE_SUBMIT,
				MessageDefinition.CMD_NODE_ACTIVEUSR,
				MessageDefinition.CMD_ASSET_STATUS,
				MessageDefinition.CMD_GET_DEVICE_LIST,
				MessageDefinition.CMD_DEVICE_SCAN_RESULT_SUBMIT,
				MessageDefinition.CMD_LOG_STATISTIC_RESULT,
				MessageDefinition.CMD_SUBJECT_STAT_PROGRESS,
				MessageDefinition.CMD_LOG_STAT_TASK_LIST,
				MessageDefinition.CMD_NODE_QUERY_PARENTNODE,
				MessageDefinition.CMD_CHILD_UPGRADE,
				MessageDefinition.CMD_SCHEDULE_EXECUTE,
				MessageDefinition.CMD_SCHEDULE_STAT_BEGIN,
				MessageDefinition.CMD_SCHEDULE_STAT_PROGRESS,
				MessageDefinition.CMD_SCHEDULE_STAT_RESULT,
				MessageDefinition.CMD_SCHEDULE_STAT_LIST,
				MessageDefinition.CMD_SCHEDULE_STAT_DONE,
				MessageDefinition.CMD_CHANGE_DATA_HOME,
				MessageDefinition.CMD_GET_USER_DATASOURCE,
				MessageDefinition.CMD_NODE_QUERY_MONITOR
		};
		
		try {
			CommandDispatcher transactionCommandChannel = ChannelGate.getCommandChannel(ChannelConstants.COMM_BASE_COMMAND_CHANNEL);
			transactionCommandChannel.regist(cmdsTransaction, new MyTransactionCommandListener());
			
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public NodeMgrFacade getNodeMgrFacade() {
		return nodeMgrFacade;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}

	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	public void setMonitorService(DataSourceService monitorService) {
		this.monitorService = monitorService;
	}

	public void setSimNodeUpgradeService(SimNodeUpgradeService simNodeUpgradeService) {
		this.simNodeUpgradeService = simNodeUpgradeService;
	}
	// rewrite code
/*
	public AuditNodeService getAuditNodeService() {
		return auditNodeService;
	}

	public void setAuditNodeService(AuditNodeService auditNodeService) {
		this.auditNodeService = auditNodeService;
	}*/

	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}

	public void setEventRuleService(EventRuleService eventRuleService) {
		this.eventRuleService = eventRuleService;
	}

	public void setLogReportTaskService(LogReportTaskService logReportTaskService) {
		this.logReportTaskService = logReportTaskService;
	}

	public void setScheduleStatTaskService( ScheduleStatTaskService scheduleStatTaskService) {
		this.scheduleStatTaskService = scheduleStatTaskService;
	}
}
