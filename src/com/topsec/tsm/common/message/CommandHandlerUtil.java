package com.topsec.tsm.common.message;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.collector.datasource.DataSource;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.component.collector.NmapResultObj;
import com.topsec.tsm.node.component.service.stat.LogStatisticResult;
import com.topsec.tsm.node.detect.Group;
import com.topsec.tsm.node.detect.Rule;
import com.topsec.tsm.node.detect.State;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.rest.server.common.HttpUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.asset.web.DiscoveredAssetManager;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.event.EventRuleDispatch;
import com.topsec.tsm.sim.event.EventRuleGroup;
import com.topsec.tsm.sim.event.EventRuleGroupResp;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.log.service.ScheduleStatTaskService;
import com.topsec.tsm.sim.log.web.ScheduleStatTaskMailSender;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.service.SimNodeUpgradeService;
import com.topsec.tsm.sim.node.util.NodeAliveCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.sysconfig.service.EventRuleService;
import com.topsec.tsm.sim.sysconfig.web.CorrRuleBuilder;
import com.topsec.tsm.sim.sysconfig.web.RuleBuilder;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.SystemConfigDispatcher;
import com.topsec.tsm.sim.util.TalVersionUtil;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.net.FtpUploadUtil;

public final class CommandHandlerUtil {
	private static Logger logger = LoggerFactory.getLogger(CommandHandlerUtil.class) ;
	
	public static Serializable handleDeviceSubmit(Serializable obj){
		NmapResultObj result = (NmapResultObj)obj ;
		logger.debug("Recevice CMD_DEVICE_SUBMIT Command From {}",result) ;
		if(result != null){
			DiscoveredAssetManager assetManager = DiscoveredAssetManager.getInstance() ;
			assetManager.addAsset(result) ;
		}
		return null ;
	}
	public static Serializable handleNodeAlive(Serializable obj){
		try {
			String nodeid = (String)obj;
			boolean nodeIsAlive = NodeAliveCache.getInstance().isAlive(nodeid);
			return (Serializable)nodeIsAlive;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	public static Serializable handleQueryPatch(Serializable obj,SimNodeUpgradeService simNodeUpgradeService){
		try {
			Map<String,String> map=(Map<String,String>)obj;
			String varsion=map.get("version");
			String type=map.get("type");
			return (Serializable)simNodeUpgradeService.getMaxVersionStrByType(type, varsion);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}	
	}
	public static Serializable handleQueryDataSource(Serializable obj,NodeMgrFacade nodeMgrFacade,DataSourceService dataSourceService){
		try {
			Map<String,Object> args = (Map<String,Object>)obj ;
			boolean includeMonitor = (Boolean) (args.containsKey("includeMonitor") ? args.get("includeMonitor") : true) ;
			boolean includeAuditLog = (Boolean) (args.containsKey("includeAuditLog") ? args.get("includeAuditLog") : true) ;
			boolean includeSystemLog = (Boolean) (args.containsKey("includeSystemLog") ? args.get("includeSystemLog") : true) ;
			List<SimDatasource> simDatasources=dataSourceService.getAll(includeMonitor,includeAuditLog,includeSystemLog);
			List<DataSource> datasources=new ArrayList<DataSource>(simDatasources == null ? 0 : simDatasources.size());
			if(ObjectUtils.isNotEmpty(simDatasources)){
				for (SimDatasource simDatasource : simDatasources) {
					DataSource dataSource=DataSourceUtil.toDataSource(simDatasource,false);
					datasources.add(dataSource);
				}
			}
			return (Serializable)datasources;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}	
	}
	public static Serializable handleDataSourceBlankList(DataSourceService dataSourceService){
		try {
			return (Serializable)dataSourceService.getBlackList();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}	
	}
	
	public static Serializable handleGetUserRule(Serializable obj,EventRuleService eventRuleService,EventResponseService eventResponseService) {
		try {
			List<EventRule> eventRules = eventRuleService.getEnableRuleInRuleGroup();
			if(eventRules!=null&&eventRules.size()!=0){
				final List<EventRuleGroup> eventRuleGroups= eventRuleService.getEnableRuleGroup();
				final List<EventRuleDispatch> eventRuleGroupDispatchs= eventRuleService.getEnableEventRuleDispatch();
				final List<EventRuleGroupResp> ruleGroupResps=eventRuleService.getAllRuleGroupResp();
				final CorrRuleBuilder groupBuilder=new CorrRuleBuilder();
				final EventResponseService responseService=eventResponseService;
				RuleBuilder ruleBuilder=new RuleBuilder(){
					@Override
					public Response getReponse(String responseId) {
						return responseService.getResponse(responseId);
					}

					@Override
					public Rule convertXMLRule(EventRule eventRule) {
						Rule rule=super.convertXMLRule(eventRule);
						List<Group> groups=new ArrayList<Group>();
						Map<String,Integer> sizeMap=new HashMap<String, Integer>();
						if(eventRuleGroups!=null){
							for(EventRuleDispatch eventRuleDispatch: eventRuleGroupDispatchs){
								Integer ruleId = eventRule.getId();
								Integer groupId=eventRuleDispatch.getGroupId();
								Integer count = sizeMap.get(groupId.toString());
								sizeMap.put(groupId.toString(), (count==null?1:++count));
								if(ruleId.intValue()==eventRuleDispatch.getRuleId().intValue()){
									Integer order = eventRuleDispatch.getOrder();
									String groupName=null;
									Integer alarmState=0;
									Integer priority=0;
									String category1=null;
									String category2=null;
									String desc = null ;
									long timeout =0;
									for(EventRuleGroup eventRuleGroup :eventRuleGroups){
										if(eventRuleGroup.getGroupId().intValue()==groupId.intValue()){
											  groupName=eventRuleGroup.getGroupName();
											  alarmState=eventRuleGroup.getAlarmState();
											  timeout = eventRuleGroup.getTimeout();
											  priority=eventRuleGroup.getPriority();
											  category1=eventRuleGroup.getCat1id();
											  category2=eventRuleGroup.getCat2id();
											  desc = eventRuleGroup.getDesc() ;
											  break;
										}
									}
									if(groupName!=null){
										groupBuilder.setDispatch(eventRuleDispatch);
										Integer dtimeout = eventRuleDispatch.getTimeout();
										State state=new State(dtimeout);
										Group group=new Group(groupId.toString(),groupName,order,alarmState,state);
										group.setTimeout(timeout);
										group.setPriority(priority==null?-1:priority);
										group.setDesc(desc) ;
										groupBuilder.rebuild(group);
										List<String >respIdList=new ArrayList<String>();
										for (EventRuleGroupResp eventRuleGroupResp : ruleGroupResps) {
											if(eventRuleGroupResp.getGroupId().intValue()==groupId.intValue()){
												String responseId=eventRuleGroupResp.getResponseId();
												respIdList.add(responseId);
											}
										}
										if( category1!=null){
											group.setCategory1(category1);
										}
										if(category2!=null){
											group.setCategory2(category2);
										}
										
										String[] responseIds=new String[respIdList.size()];
										respIdList.toArray(responseIds);
										group.setResponseIds(responseIds);
										String[] responsecfgkeys=new String[respIdList.size()];
										String[] responsecfgNames=new String[respIdList.size()];
										
										for (int i = 0; i < responseIds.length; i++) {
											Response response = this.getReponse(responseIds[i]);
											if(response!=null){
												responsecfgkeys[i]=response.getCfgKey();
												responsecfgNames[i]=response.getName();
											}
										}
										group.setResponsecfgkeys(responsecfgkeys);
										group.setResponsecfgNames(responsecfgNames);
										groups.add(group);
									}
								}
							}
							 
							for(Group group:groups){
								String groupId=group.getGroupId();
								Integer size=sizeMap.get(groupId);
								if(size.intValue()<group.getOrder()){
									throw new RuntimeException("规则计数错误![groupId="+groupId+"],[size="+size+"],[order="+group.getOrder()+"]");
								}
								group.setSize(size);
							} 
							rule.setBelongGroups(groups);
						}
						return rule;
					}
				};
				
				EventRule[]ruleEventRules=new EventRule[eventRules.size()];
				eventRules.toArray(ruleEventRules);
				ruleBuilder.rebuildEventRule(ruleEventRules);
			}else{
				return new ArrayList<EventRule>();
			}
			return (Serializable)eventRules;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}
	
	public static Serializable handleQueryServerInfo(Serializable obj,NodeMgrFacade nodeMgrFacade){
		long currentTime = System.currentTimeMillis() ;
		try {
			//把server节点信息、License信息返回
			//如果能取到server节点信息，表明服务器端已经加载完毕
			//change code
			Node smpNode = nodeMgrFacade.getNodeByNodeId(NodeDefinition.NODE_TYPE_SMP) ;
			while(smpNode==null){
				nodeMgrFacade.registerSMP();
				Thread.sleep(2000);
				System.out.println("Server Starting...");
				smpNode = nodeMgrFacade.getNodeByNodeId(NodeDefinition.NODE_TYPE_SMP);
			}
			Properties props = new Properties();
			props.put("LicenseType", TalVersionUtil.getInstance().getVersion());
			props.put("NODEID", smpNode.getNodeId());
			long endTimer = System.currentTimeMillis() ;
			logger.info("Get server info used:"+(endTimer-currentTime));
			return props;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}	
	}
	
	public static Serializable handleUpdateVersion(Serializable obj,NodeMgrFacade nodeMgrFacade){
		try {
			Map<String,String> map=(Map<String,String>)obj;
			String nodeId=map.get("id");
			String version=map.get("version");
			Node node = nodeMgrFacade.getNodeByNodeId(nodeId);
			node.setVersion(version);
			nodeMgrFacade.updateNode(node);
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static Serializable handleQueryBackConfig(Serializable obj,EventResponseService eventResponseService){
		try {
			Map<String, String> ret = new HashMap<String, String>();
			List<Response> responses = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup");
			Config config = RespCfgHelper.getConfig(responses.get(0));
			Block backupBlock = config.getBlockbyKey("local");
			if((backupBlock != null) && (backupBlock.isGroupSelect())) {
				String path = backupBlock.getItemValue("path");
				ret.put("type", "local");
				ret.put("path", path);
			} else {
				backupBlock = config.getBlockbyKey("ftp");
				if((backupBlock != null) && (backupBlock.isGroupSelect())) {
					ret.put("type", "ftp");
					ret.put("serverip", backupBlock.getItemValue("serverip"));
					ret.put("user", backupBlock.getItemValue("user"));
					ret.put("password", backupBlock.getItemValue("password"));
					ret.put("encoding", backupBlock.getItemValue("encoding"));
				}						
			}
			responses = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup_auto");
			config = RespCfgHelper.getConfig(responses.get(0));
			backupBlock = config.getBlockbyKey("autoback");
			if(backupBlock != null) {
				String enable = backupBlock.getItemValue("enable");
				if(!Boolean.valueOf(enable)) {
					ret.put("bktype", "0");
				} else {
					String interval = backupBlock.getItemValue("autobackManner");
//                	if("lastday".equals(interval)){
//                		ret.put("bktype", "1");
//                	}else if ("lastweek".equals(interval)){
//                		ret.put("bktype", "2");
//                	}else if ("lastmonth".equals(interval)){
//                		ret.put("bktype", "3");
//                	}
                	if("1m".equals(interval)){
                		ret.put("bktype", "1");
                	}else if ("2m".equals(interval)){
                		ret.put("bktype", "2");
                	}else if ("3m".equals(interval)){
                		ret.put("bktype", "3");
                	}else if ("4m".equals(interval)){
                		ret.put("bktype", "4");
                	}else if ("5m".equals(interval)){
                		ret.put("bktype", "5");
                	}else if ("6m".equals(interval)){
                		ret.put("bktype", "6");
                	}else if ("1y".equals(interval)){
                		ret.put("bktype", "12");
                	}
				}
			} 
			return (Serializable)ret;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}	
	}
	
	public static Serializable handleGetDataHome(Serializable obj,NodeMgrFacade nodeMgrFacade,EventResponseService eventResponseService){
		try {
			String nodeId=(String)obj;
			if(nodeId==null){
				return null;
			}
			List<Node> nodeList = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_INDEXSERVICE, false, false, false, false) ;
			while(ObjectUtils.isEmpty(nodeList)){//如果是系统第一次启动，可能会节点信息还未注册
				logger.warn("IndexService节点未注册！") ;
				Thread.sleep(1000) ;
				nodeList = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_INDEXSERVICE, false, false, false, false) ;
			}
			Node indexNode = nodeList.get(0) ;
			Response response = eventResponseService.getResponsesByNodeIdAndCofingKey(indexNode.getNodeId(), "sys_cfg_store");
			while(response == null){
				logger.warn("存储策略未初始化！") ;
				Thread.sleep(1000) ;
				response = eventResponseService.getResponsesByNodeIdAndCofingKey(indexNode.getNodeId(), "sys_cfg_store");
			}
			Config config = RespCfgHelper.getConfig(response);
			Block archivePathBlock = config.getBlockbyKey("archive_path");
			String itemValue = archivePathBlock.getItemValue("archive_path");
			return (Serializable)itemValue;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}	
	}
	
	public static Serializable handleAssetStatus(Serializable obj){
		NmapResultObj result = (NmapResultObj)obj ;
		AssetObject ao = AssetFacade.getInstance().getAssetByIp(result.getIp()) ;
		if (ao != null) {
			ao.setState(result.getState()) ;
		}
		logger.debug("Recevice CMD_ASSET_STATUS Command From {}"+result) ;
		return null ;
	}
	public static Serializable handleGetDeviceList(NodeMgrFacade nodeMgrFacade,Serializable obj){
		String nodeId = (String)obj ;
		Node node = nodeMgrFacade.getNodeByNodeId(nodeId,false,false,false,false,true) ;
		if(node == null){//第一次启动节点未注册
			return (Serializable) Collections.EMPTY_LIST ;
		}
		List<AssetObject> deviceList = AssetFacade.getInstance().getByScanNode(nodeId) ;
		if(NodeUtil.isAgent(node.getType()) || NodeUtil.isAuditor(node.getType())){
			deviceList = AssetFacade.getInstance().getByScanNode(nodeId)  ;
		}else{
			Node parentNode = node.getParent();
			deviceList = AssetFacade.getInstance().getByScanNode(parentNode.getNodeId()) ;
		}
		if(ObjectUtils.isEmpty(deviceList)){
			return (Serializable) Collections.EMPTY_LIST;
		}
		List<String> ipList = new ArrayList<String>(deviceList.size()) ;
		for(AssetObject asset:deviceList){
			if(asset.getEnabled() == 1){
				ipList.add(asset.getMasterIp().toString()) ;
			}
		}
		return (Serializable) ipList ;
	}

	@SuppressWarnings({ "rawtypes" })
	public static Serializable handleLogStatisticResult(Serializable obj,LogReportTaskService logReportTaskService) {
		logger.debug("Receive CMD_LOG_STATISTIC_RESULT command") ;
		LogStatisticResult result = (LogStatisticResult)obj;
		try {
			if(ObjectUtils.isEmpty(result.getResults())){
				return null;
			}
			Map.Entry<Integer,List> entry = result.getResults().entrySet().iterator().next() ; 
			Integer subjectId = entry.getKey() ;
			List statResult = entry.getValue() ;
			ReportTask task = logReportTaskService.getTaskWithoutResult(subjectId) ;
			if (task != null) {
				task.setEndTime(new Date()) ;
				task.setProgress(100.0) ;
				if(result.isSuccess()){
					List<String> jsonResult = new ArrayList<String>(statResult.size()) ;
					for(Object record:statResult){
						jsonResult.add(JSON.toJSONString(record)) ;
					}
					task.setTaskState(ReportTask.TASK_STATE_SUCCESS) ;
					task.setJsonResult(jsonResult) ;
					task.getBrowseObject().setStart(result.getStart()) ;
					task.getBrowseObject().setEnd(result.getEnd()) ;
					logReportTaskService.refreshTask(task) ;
				}else{
					task.setTaskState(ReportTask.TASK_STATE_FAIL) ;
					logReportTaskService.refreshTask(task) ;
				}
			}else{
				logger.warn("统计任务{}已经被删除!",subjectId) ;
			}
		} catch (Exception e) {
			logger.error("查询分析统计结果更新出错！",e) ;
		}
		return null ;
	}
	
	public static Serializable handleLogStatProgress(Serializable obj,LogReportTaskService logReportTaskService) {
		Map<String,Object> progress = (Map<String, Object>) obj ;
		Integer id = (Integer) progress.get("id") ;
		Integer percent = (Integer) progress.get("percent") ; 
		ReportTask task = logReportTaskService.getTaskWithoutResult(id) ;
		if (task != null) {
			if(task.getProgress() == null || !task.getProgress().equals(percent)){
				task.setProgress(percent.doubleValue()) ;
				logReportTaskService.refreshTask(task) ;
			}
		}else{
			logger.warn("统计任务{}已经被删除!",id) ;
		}
		return null ;
	}
	
	public static Serializable handleLogStatTaskList(LogReportTaskService logReportTaskService) {
		List<ReportTask> uncompleteTasks = logReportTaskService.getUncompleteTask() ;
		if(ObjectUtils.isNotEmpty(uncompleteTasks)){
			List<SearchObject> list = new ArrayList<SearchObject>(uncompleteTasks.size()) ;
			for(ReportTask task:uncompleteTasks){
				SearchObject so = task.getBrowseObject() ;
				so.setId(String.valueOf(task.getId())) ;
				list.add(so) ;
				task.setStartTime(new Date()) ;
				task.setTaskState(ReportTask.TASK_STATE_RUNNING) ;
				logReportTaskService.refreshTask(task) ;
			}
			return (Serializable) list ;
		}else{
			return (Serializable) Collections.emptyList() ;
		}
	}

	public static Serializable handleQueryParentNode(NodeMgrFacade nodeMgrFacade){
		try {
			//获取父节点
			Node node= nodeMgrFacade.getParentNode();
			return (Serializable)node.getIp();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}	
	}

	//child（下级）升级：下载升级包，重启服务（所有节点）
	public static Serializable handleChildUpgrade(Serializable obj,
			NodeMgrFacade nodeMgrFacade) {
		// TODO Auto-generated method stub
		Map<String, String> args = (Map<String, String>)obj;
		try {
			Node node = nodeMgrFacade.getParentNode();
			if(node == null)
				node = nodeMgrFacade.getKernelAuditor(false);
			String path = "https://"+node.getIp()+"/resteasy/upgrade/queryPatch";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("version", args.get("version").toString());
			String receviveInfo = HttpUtil.doPostWithSSLByMap(path, params, null, "UTF-8");
			if(StringUtil.isNotBlank(receviveInfo)) {
				Document document = DocumentHelper.parseText(receviveInfo);
				Element root = document.getRootElement();
				int port = Integer.parseInt(root.attributeValue("port"));
				String host =  root.attributeValue("host");
				String user =root.attributeValue("user");
				String password = root.attributeValue("password");
				String home = root.attributeValue("home");
				String patch =  root.attributeValue("patch");
				String encoding =root.attributeValue("encoding");
		
				//make patch directory	
				FileUtils.forceMkdir(new File(SystemDefinition.DEFAULT_INSTALL_DIR, "patch"));
				String patchDir = new File(SystemDefinition.DEFAULT_INSTALL_DIR, "patch").getAbsolutePath();
				//download patch file
				 FtpUploadUtil.downFile(host, port, user,
						password, encoding, home, patch,patchDir);
				 
				 //重启所有节点(auditor\service\reportserv\smp)
				 restartAllNode(nodeMgrFacade);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		 return null;
	}
	
	public static  void restartAllNode(NodeMgrFacade nodeMgrFacade){		
		try {
			List<String> types = new ArrayList<String>();
			types.add(NodeDefinition.NODE_TYPE_AUDIT);
			types.add(NodeDefinition.NODE_TYPE_AGENT);
			types.add(NodeDefinition.NODE_TYPE_FLEXER);
			types.add(NodeDefinition.NODE_TYPE_ACTION);
			types.add(NodeDefinition.NODE_TYPE_REPORTSERVICE);
			types.add(NodeDefinition.NODE_TYPE_QUERYSERVICE);
			types.add(NodeDefinition.NODE_TYPE_INDEXSERVICE);
			types.add(NodeDefinition.NODE_TYPE_COLLECTOR);

			List<Node> nodes = nodeMgrFacade.getNodesByTypes(types, false, false, false, false);
			if (nodes != null) {
				for (Node node : nodes) {
					String[] route = null;
					route = NodeUtil.getRoute(node);
					NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_NODE_RESTART, Integer.valueOf(7000));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
			new Thread() {
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("服务器接收到重启命令。TopsecAudit Restart Now");

			}
		}.start();
	}

	public static Serializable handleScheduleStatProcess(ScheduleStatTaskService service,Serializable obj) {
		Map<Object,Object> params = (Map)obj ;
		Integer taskId = (Integer) params.get("taskId") ;
		Map<Integer,Integer> subjectProgress = (Map<Integer,Integer>) params.get("subjectProgress") ;
		service.updateSubjectProgress(taskId, subjectProgress) ;
		return null;
	}

	public static Serializable handleScheduleStatResult(ScheduleStatTaskService service,Serializable obj) {
		Map<String,Object> params = (Map)obj ;
		Integer taskId = (Integer) params.get("taskId") ;
		LogStatisticResult result = (LogStatisticResult) params.get("result") ;
		service.saveSubjectResult(taskId, result) ;
		return null;
	}

	public static Serializable handleScheduleStatBegin(ScheduleStatTaskService scheduleStatTaskService, Serializable obj) {
		return null;
	}
	/**
	 * 下级节点获取周期性任务列表
	 * @param service
	 * @return
	 */
	public static Serializable handleScheduleStatList( ScheduleStatTaskService service) {
		List<ScheduleStatTask> tasks = service.getEnabled() ;
		return (Serializable) tasks;
	}
	/**
	 * 周期性统计任务执行完成
	 * @param service
	 * @param param
	 * @return
	 */
	public static Serializable handleScheduleTaskDone(ScheduleStatTaskService service,Serializable param){
		Integer taskId = (Integer)param ;
		service.done(taskId) ;
		ScheduleStatTaskMailSender sender = new ScheduleStatTaskMailSender(service, taskId) ;
		sender.start() ;
		return null ;
	}
	/**
	 * 周期性任务开始执行
	 * @param service
	 * @param obj
	 * @return
	 */
	public static Serializable handleScheduleExecute(ScheduleStatTaskService service,Serializable obj) {
		Integer taskId = (Integer)obj ;
		service.onExecute(taskId);
		return null ;
	}
	public static Serializable handleGetUserDevices(UserService userService,DataSourceService dataSourceService, String userName) {
		return (Serializable) dataSourceService.getUserDataSource(userName);
	}
	public static Serializable handleChangeDataHome(EventResponseService eventResponseService,NodeMgrFacade nodeMgrFacade, Serializable obj) {
		Node serviceNode = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_INDEXSERVICE, false, false, false, false).get(0) ;
		Response resp = eventResponseService.getResponsesByNodeIdAndCofingKey(serviceNode.getNodeId(), "sys_cfg_store") ;
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			Block block = conf.getBlockbyKey("archive_path") ;
			block.getItembyKey("archive_path").setValue((String)obj) ;
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendSyslogStore(conf, nodeMgrFacade, resp);
			SystemConfigDispatcher.getInstance().sendLogPath(conf, nodeMgrFacade, resp);
			return true ;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return false ;
	}

	public static Serializable handleQueryMonitor(NodeMgrFacade nodeMgrFacade, DataSourceService monitorService,String ip) {
		//因为第一次注册时下级节点没有注册，此时下级节点的ip地址为本机ip(非127.0.0.1),所以需要转换为本机地址127.0.0.1再进行查询
		ip = ip.equals(IpAddress.getLocalIp().toString()) ? IpAddress.getLocalIp().getLocalhostAddress() : ip ;
		Node node = nodeMgrFacade.getAuditorOrAgentByIp(ip) ;
		if(node == null){
			return (Serializable) Collections.emptyList() ;
		}
		String nodeId = null ;
		if(NodeUtil.isAgent(node.getType())){
			nodeId = node.getNodeId() ;
		}else{
			List<Node> collectorNode = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_COLLECTOR, false, false, false, false) ;
			if(ObjectUtils.isNotEmpty(collectorNode)){
				nodeId = collectorNode.get(0).getNodeId();
			}
		}
		if(nodeId == null){
			return (Serializable) Collections.emptyList() ;
		}
		List<SimDatasource> dataSources = monitorService.getByNodeId(nodeId) ;
		if(ObjectUtils.isEmpty(dataSources)){
			return (Serializable) Collections.emptyList() ;
		}
		ArrayList<DataSource> result = new ArrayList<DataSource>(dataSources.size()) ;
		for(SimDatasource ds:dataSources){
			result.add(DataSourceUtil.toDataSource(ds, false)) ;
		}
		return result ;
	}
}
