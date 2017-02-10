package com.topsec.tsm.sim.newreport.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.ConfigFactory;
import com.topsec.tal.base.util.config.ConfigType;
import com.topsec.tal.base.util.config.Item;
import com.topsec.tal.base.util.config.webitems.InputItem;
import com.topsec.tal.base.util.config.webitems.ListInputItem;
import com.topsec.tal.base.util.config.webitems.SelectItem;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.newreport.bean.PlanConfig;
import com.topsec.tsm.sim.newreport.util.QueryUtil;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.HtmlAndFileUtil;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.ResponseSend;
import com.topsec.tsm.tal.response.adjudicate.ResponseInMem;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;

/**
 * @ClassName: PlanReportController
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年12月24日下午5:23:10
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
@Controller
@RequestMapping("planExecute")
public class PlanReportController {
	@Autowired
	private EventResponseService eventResponseService ;
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private ReportService reportService ;
	@RequestMapping("savePlanConfig")
	@ResponseBody
	public Object savePlanConfig(SID sid,@RequestBody PlanConfig planConfig) {
		JSONObject json = new JSONObject();
		String saveResult="saveResult";
		resetPlanConfig(sid,planConfig);
		try {
			Response planTask=getResponse(planConfig);
			planTask.setCreateTime(new Date());
			Config config =ConfigFactory.getCfgTemplate(planConfig.getReportConfigType());
			setConfig(config,planConfig,planTask.getId());
			RespCfgHelper.setConfig(planTask, config);
			if (null ==planTask.getId()) {
				eventResponseService.addResponse(planTask);
				if (ConfigType.TYPE_RESPONSE.equals(planTask.getType())) {
					toLog(sid,AuditCategoryDefinition.SYS_ADD, "添加响应对象", "添加响应对象名称: " + planTask.getName(), Severity.LOWEST);
				}else if (ConfigType.TYPE_SCHEDULE.equals(planTask.getType())) {
					toLog(sid,AuditCategoryDefinition.SYS_ADD, "添加计划报表", "添加计划报表名称:" + planTask.getName(), Severity.LOWEST);
					ResponseInMem.getInstance().addResponse(planTask);
				} else {
					Config configSend = RespCfgHelper.getConfig(planTask);
					send(configSend, planTask, "save");// 下发
				}
			}else {
				eventResponseService.updateResponse(planTask);
				if (ConfigType.TYPE_RESPONSE.equals(planTask.getType())) {
					toLog(sid,AuditCategoryDefinition.SYS_UPDATE, "更新响应对象", "更新响应对象名称: " + planTask.getName(), Severity.LOW);
				}else if (ConfigType.TYPE_SCHEDULE.equals(planTask.getType())) {
					toLog(sid,AuditCategoryDefinition.SYS_UPDATE, "修改计划报表", "修改计划报表名称:" + planTask.getName(), Severity.LOW);
					ResponseInMem.getInstance().updateResponse(planTask);
				} else {
					if (!ConfigType.TYPE_RESPONSE.equals(planTask.getType())) {
						// 添加自审计日志
						toLog(sid,AuditCategoryDefinition.SYS_UPDATE, planTask.getName(), planTask.getDesc(), Severity.LOW);
					}
					send(config, planTask, "modify");// 响应下发
				}
			}
			json.put(saveResult, true);
			
		} catch (Exception e) {
			json.put(saveResult, false);
			e.printStackTrace();
		}
		return json;
	}
	@RequestMapping("showPlanReport")
	@ResponseBody
	public Object showPlanReport(@RequestParam("respId")String respId) {
		
		Response planTask = reportService.showPlanTaskById(respId);
		PlanConfig planConfig = new PlanConfig();
		planConfig.setResponseId(respId);
		planConfig.setTaskName(planTask.getName());
		planConfig.setRoleAccount(planTask.getCreater());
		Node node=planTask.getNode();
		if(!(null==node)&&null!=node.getNodeId()){
			planConfig.setNodeId(node.getNodeId());
		}
		planConfig.setResponseDesc(planTask.getDesc());
//		planConfig.setResponseConfig(planTask.getConfig());
		String expre=planTask.getScheduleExpression();
		String[]expreArr=expre.split(" ");
		//[0, 36, 14, *, *, ?]
		planConfig.setMin(Integer.valueOf(expreArr[1]));
		planConfig.setHour(Integer.valueOf(expreArr[2]));
		int date="?".equals(expreArr[3])?1:("*".equals(expreArr[3])?1:Integer.valueOf(expreArr[3]));
		int day="?".equals(expreArr[5])?1:("*".equals(expreArr[5])?1:Integer.valueOf(expreArr[5]));
		int month="?".equals(expreArr[4])?1:("*".equals(expreArr[4])?1:Integer.valueOf(expreArr[4]));
		planConfig.setDate(date);
		planConfig.setDay(day);
		planConfig.setMonth(month);
		planConfig.setScheduleExpression(expre);
		planConfig.setScheduleType(planTask.getScheduleType());
		
		Config config=null;
		try {
			config = RespCfgHelper.getConfig(planTask);
		} catch (I18NException e) {
//			config=new Config(planTask.getConfig());
			e.printStackTrace();
		}//String configID = planTask.getCfgKey();ConfigFactory.getCfgTemplate(configID);
		
		planConfig.setCreateTime(StringUtil.dateToString(planTask.getCreateTime()));
		planConfig.setStatus(String.valueOf(planTask.isStart()));
		planConfig.setConfigType(config.getKey());
		planConfig.setSubConfigType(config.getSubType());
		int successCount=reportService.showPlanResultSuccessCountByRespId(respId);
		int failedCount=reportService.showPlanResultFailedCountByRespId(respId);
		planConfig.setSuccessCount(successCount);
		planConfig.setFailedCount(failedCount);
		Block block = config.getBlockbyKey("reportconfig");
		String reportUser = GlobalUtil.isNullOrEmpty(block.getItemValue("report_user"))?"":block.getItemValue("report_user");
		List<String>mailList=block.getItemValueList("report_maillist");
		String[]emails=new String[mailList.size()];
		for (int i=0;i<mailList.size();i++) {
			emails[i]=mailList.get(i);
		}
		planConfig.setResourceId(block.getItemValue("resource_id"));
		planConfig.setSecurityObjectType(block.getItemValue("security_object_type"));
		planConfig.setDeviceIp(block.getItemValue("device_ip"));
		planConfig.setParentReportId(new Integer(block.getItemValue("parent_report_id")));
		planConfig.setReportName(block.getItemValue("report_name"));
		planConfig.setNodeId(block.getItemValue("node_id"));
		planConfig.setReportType(block.getItemValue("report_type"));
		planConfig.setReportTopn(block.getItemValue("report_topn"));
		planConfig.setReportFiletype(block.getItemValue("report_filetype"));
		planConfig.setReportMaillist(emails);
		planConfig.setRoleAccount(block.getItemValue("role_account"));
		planConfig.setReportUser(reportUser);
		
		Object json=JSONObject.toJSON(planConfig);
		return json;
	}
	@RequestMapping("downloadPlanResult")
	public void downloadPlanResult(@RequestParam("respId")String respId, HttpServletResponse response) throws Exception {
		try {
			Response planTask = reportService.showPlanTaskById(respId);
			Config config = RespCfgHelper.getConfig(planTask);
			Block receiveBlock = config.getBlockbyKey("reportconfig");
			String eDirectory = receiveBlock.getItemValue("report_save_path");
			String savePath = QueryUtil.getSavePath(eDirectory);
			String zipTmpPath=savePath.substring(0,savePath.indexOf(eDirectory)+1)+"tmp/";
			HtmlAndFileUtil.createPath(zipTmpPath);
			String outputName=StringUtil.currentDateToString("yyyyMMddHHmmss")+".zip";
			HtmlAndFileUtil.compressFloderChangeToZip(savePath, zipTmpPath, outputName);
			String outName = planTask.getName()+outputName;
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\""+outName+"\"");
			ServletOutputStream out = response.getOutputStream();
			HtmlAndFileUtil.outzipFile(zipTmpPath+outputName, out);
			out.flush();
			out.close();
			HtmlAndFileUtil.clearPath(zipTmpPath);
			
		} catch (Exception e) {
		}
	}
	private void setConfig(Config config,PlanConfig planConfig,String respId)throws Exception{
		List<Block> blocks = config.getCfgBlocks();
		String selectBlock = null;
		List<String> blockKeyList = null;
		Block aSelectBlock = null;
		for (Block block : blocks) {
			String group=block.getGroup();
			if (group != null) {
				blockKeyList = config.getGroupBlockKeys(group);
				if (null != planConfig.getBlockGroup()
						&& ! "".equals(planConfig.getBlockGroup())) {
					selectBlock = planConfig.getBlockGroup();
					for (String blockKey : blockKeyList) {
						if (blockKey.equals(selectBlock.substring(selectBlock.lastIndexOf(".") + 1))) {
							aSelectBlock = config.getBlockbyKey(selectBlock.substring(selectBlock.lastIndexOf(".") + 1));
							config.setGroupSelectBlock(aSelectBlock.getKey(), aSelectBlock.getGroup());
						}
					}
				}

			}
			List<Item> cfgItems = block.getCfgItems();
			String newValue = null;
			for (Item item : cfgItems) {

				if (item instanceof InputItem) {
					if("resource_id".equals(item.getKey())){
						newValue = planConfig.getResourceId();
					}else if("security_object_type".equals(item.getKey())){
						newValue = planConfig.getSecurityObjectType();
					}else if("device_ip".equals(item.getKey())){
						newValue = planConfig.getDeviceIp();
					}else if("device_params".equals(item.getKey())){
						newValue = planConfig.getParams();
					}else if("parent_report_id".equals(item.getKey())){
						newValue = ""+planConfig.getParentReportId();
					}else if("report_name".equals(item.getKey())){
						newValue = planConfig.getReportName();
					}else if("node_id".equals(item.getKey())){
						newValue = planConfig.getNodeId();
					}else if("role_account".equals(item.getKey())){
						newValue = planConfig.getRoleAccount();
					}else if("report_user".equals(item.getKey())){
						newValue = planConfig.getReportUser();
					}else if("report_save_path".equals(item.getKey()) ){
						if(null == respId)
							newValue="/create"+StringUtil.currentDateToString("yyyyMMddHHmmss")
								+"time"+(int)(Math.random()* 1000)+"/";
						else {
							Response planTask = reportService.showPlanTaskById(respId);
							Config configPesi = RespCfgHelper.getConfig(planTask);
							Block receiveBlock = configPesi.getBlockbyKey("reportconfig");
							newValue = receiveBlock.getItemValue("report_save_path");
						}
					}
					
					item.setValue(newValue);
				}
				
				if (item instanceof ListInputItem) {
					String[] selectedFiels = planConfig.getReportMaillist();
					if (null ==selectedFiels || selectedFiels.length<1) {
						throw new RuntimeException("收件人为空！"); 
					}//"邮件格式不正确"
					List<String> newValueList = new ArrayList<String>();
					for (int i = 0; i < selectedFiels.length; i++) {
						if (selectedFiels[i].indexOf("@") < 1 ||selectedFiels[i].lastIndexOf("@")==selectedFiels[i].length()-1) {
							continue; 
						}
						if (! newValueList.contains(selectedFiels[i])) {
							newValueList.add(selectedFiels[i]);
						}
					}
					if (0 == newValueList.size()) {
						throw new RuntimeException("邮件格式不正确!"); 
					}
					item.setValueList(newValueList);
				}
				
				if (item instanceof SelectItem) {
					if("report_type".equals(item.getKey())){
						newValue = planConfig.getReportType();
					}else if ("report_topn".equals(item.getKey())) {
						newValue = planConfig.getReportTopn();
					}else if ("report_filetype".equals(item.getKey())) {
						newValue = planConfig.getReportFiletype();
					}
					item.setValue(newValue);
				}
			}
		}
	}
	private Response getResponse(PlanConfig planConfig)throws Exception{
		String configType=planConfig.getConfigType();
		String realCfgType ="action.type." + configType.substring(configType.indexOf(".") + 1);
		Response planTask=new Response();
		planTask.setId(planConfig.getResponseId());
		planTask.setName(planConfig.getTaskName());
		planTask.setCreater(planConfig.getRoleAccount());
		planTask.setStart(true);
//		planTask.setScheduleType(planConfig.getScheduleType());
//		planTask.setCfgKey(planConfig.getReportConfigType());
//		planTask.setSubType(planConfig.getSubConfigType());
		planTask.setType(realCfgType);
		planTask.setEditType("edit");
		if (ConfigType.TYPE_SCHEDULE.equals(realCfgType)){
			if (null != planConfig.getNodeId() ) {
				Node node = new Node();
				node.setNodeId(planConfig.getNodeId());
				planTask.setNode(node);
			}
			TimeExpression timeExpression=getTimeExpression(planConfig);
			planTask.setTimeExpression(timeExpression);
		}else {
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_ACTION, false, false, false, false);
			if (nodes.size() == 1) {
				planTask.setNode(nodes.get(0));
			} else {// 保存核心Auditor
				Node node = nodeMgrFacade.getKernelAuditor(false);// 核心Auditor
				if (node != null) {
					planTask.setNode(node);
				}
			}
		}
		return planTask;
	}
	private TimeExpression getTimeExpression(PlanConfig planConfig)throws Exception{
		TimeExpression timeExpression = new TimeExpression();
		Integer month=planConfig.getMonth();
		Integer date=planConfig.getDate();
		Integer day=planConfig.getDay();
		Integer hour=planConfig.getHour();
		Integer min=planConfig.getMin();
		if (TimeExpression.TYPE_EVERY_YEAR.equals(planConfig.getScheduleType())) {
			timeExpression.setType(TimeExpression.TYPE_EVERY_YEAR);
			if (!(checkDate(month, date)||checkHour(hour)||checkMin(min))){
				return null;
			}
			timeExpression.setEveryYear(month, date, hour, min, 0);

		} else if (TimeExpression.TYPE_EVERY_MONTH.equals(planConfig.getScheduleType())) {
			timeExpression.setType(TimeExpression.TYPE_EVERY_MONTH);
			if (!(checkDate(2, date)||checkHour(hour)||checkMin(min))) {
				return null;
			}
			timeExpression.setEveryMonth(date, hour, min, 0);

		} else if (TimeExpression.TYPE_EVERY_WEEK.equals(planConfig.getScheduleType())) {
			timeExpression.setType(TimeExpression.TYPE_EVERY_WEEK);
			
			if (!(checkHour(hour)||checkMin(min))) {
				return null;
			}
			timeExpression.setEveryWeek(day, hour,min, 0);

		} else if (TimeExpression.TYPE_EVERY_DAY.equals(planConfig.getScheduleType())) {
			timeExpression.setType(TimeExpression.TYPE_EVERY_DAY);
			if (!(checkHour(hour)||checkMin(min))) {
				return null;
			}
			timeExpression.setEveryDay(hour, min,0);
		}
		return timeExpression;
	}
	private boolean checkHour(Integer hour){
		if (null == hour || hour<0 ||hour>23) {
			return false;
		}
		return true;
	}
	
	private boolean checkMin(Integer min){
		if (null==min||min<0||min>59) {
			return false;
		}
		return true;
	}
	
	private boolean checkDate(Integer month,Integer date){
		if (null==month||month<1||month>12) {
			return false;
		}
		if (month==2) {
			if (null==date||date<1||date>29) {
				return false;
			}
			return true;
		}else if (month==1||month==3||month==5||month==7||month==8||month==10||month==12) {
			if (null==date||date<1||date>31) {
				return false;
			}
			return true;
		}else {
			if (null==date||date<1||date>30) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 产生自审计日志
	 * 
	 * @param action
	 *           操作类型
	 * @param name
	 *           操作对象名称
	 * @param desc
	 *           动作描述信息
	 * @param severity
	 *           安全级别
	 */
	private void toLog(SID sid,String action, String name, String desc, Severity severity) {
		// 以下产生日志信息
		AuditRecord auditRecord = AuditLogFacade.createConfigAuditLog();
		auditRecord.setBehavior(action);
		auditRecord.setSecurityObjectName(name);
		auditRecord.setDescription(desc);
		auditRecord.setSubject(sid.getUserName());
		auditRecord.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		auditRecord.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		auditRecord.setSuccess(true);
		auditRecord.setSeverity(severity);
		AuditLogFacade.send(auditRecord);// 发送系统自审计日志
	}

	/**
	 * 下发
	 * 
	 * @param config
	 * @param resp
	 * @param type
	 */
	private void send(Config config, Response resp, String type) {
		// 执行服务命令响应下发
		if ("resp_cfg_execcmd".equals(config.getKey())) {
			ResponseSend.getInstance().sendExeccmd(config, nodeMgrFacade, resp, type);
		}
		// 声音响应 下发
		else if ("resp_cfg_wavalert".equals(config.getKey())) {
			ResponseSend.getInstance().sendWavalert(config, nodeMgrFacade, resp, type);
		}
		// Snmp Trap 响应下发
		else if ("resp_cfg_snmptrap".equals(config.getKey())) {
			ResponseSend.getInstance().sendSnmpTrap(config, nodeMgrFacade, resp, type);
		}
		// TopAnalyzer联动响应下发
		else if ("rep_cfg_integer".equals(config.getKey())) {
			ResponseSend.getInstance().sendInteger(config, nodeMgrFacade, resp, type);
		}
		// 邮件响应下发
		else if ("resp_cfg_mail".equals(config.getKey())) {
			ResponseSend.getInstance().sendToMail(config, nodeMgrFacade, resp, type);
		}
		// 短信响应下发
		else if ("resp_cfg_phonemsg".equals(config.getKey())) {
			ResponseSend.getInstance().sendPhonemsg(config, nodeMgrFacade, resp, type);
		}
		// 声光响应 下发
		else if ("resp_cfg_wavashinelert".equals(config.getKey())) {
			ResponseSend.getInstance().sendWavaShinelert(config, nodeMgrFacade, resp, type);
		}
		// 一信通短信响应下发
		else if ("resp_cfg_umsgate".equals(config.getKey())) {
			ResponseSend.getInstance().sendUMSGate(config, nodeMgrFacade, resp, type);
		}

	}
	@SuppressWarnings("unchecked")
	private List<SimDatasource> roleDsByLogType(SID sid,String securityObjectType){
		List<SimDatasource> dslist =null;
		if ("Esm/Topsec/SimEvent".equals(securityObjectType)) {
			dslist =new ArrayList<SimDatasource>();
			Node auditor=nodeMgrFacade.getKernelAuditor(false);
			SimDatasource dsource = allDatasource(securityObjectType,auditor.getNodeId(),"ONLY_BY_DVCTYPE");
			dslist.add(0, dsource);
			return dslist;
		}
		List<SimDatasource> simDatasources=dataSourceService.getDataSourceByDvcType(securityObjectType);
		if (null == simDatasources || 0==simDatasources.size()) {
			return simDatasources;
		}
		String auditorNodeId=simDatasources.get(0).getAuditorNodeId();
		removeRepeatDs(simDatasources);
		Set<AuthUserDevice> devices= sid.getUserDevice() == null ? Collections.<AuthUserDevice>emptySet() : sid.getUserDevice() ;
		dslist =new ArrayList<SimDatasource>();
		if (sid.isOperator()) {
			SimDatasource dsource = allDatasource(securityObjectType,auditorNodeId,"ONLY_BY_DVCTYPE");
			dslist.add(0, dsource);
			dslist.addAll(simDatasources) ;
		}else{
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
			Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
			for (SimDatasource simDatasource : simDatasources) {
				Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
				if (userDeviceIPs.contains(simDatasource.getDeviceIp()) || (device != null && sid.getUserName().equalsIgnoreCase(device.getCreator()))) {
					dslist.add(simDatasource);
				}
			}
			if (0<dslist.size()) {
				SimDatasource dsource = allDatasource(securityObjectType,auditorNodeId,"ALL_ROLE_ADDRESS");
				dslist.add(0, dsource);
			}
		}
		return dslist;
	}
	
	private SimDatasource allDatasource(String securityObjectType,String auditorNodeId ,String ipType){
		SimDatasource dsource = new SimDatasource();
		dsource.setDeviceIp(ipType);
		dsource.setSecurityObjectType(securityObjectType);
		dsource.setAuditorNodeId(auditorNodeId);
		dsource.setResourceName("全部");
		dsource.setNodeId("");
		dsource.setDeviceType(securityObjectType);
		return dsource;
	}
	 private void removeRepeatDs(List<SimDatasource> simDatasources){
		if (null==simDatasources || simDatasources.size()<1) {
			return;
		}
		List<SimDatasource>removedDatasources=new ArrayList<SimDatasource>();
		for (int i = 0; i < simDatasources.size(); i++) {
			SimDatasource simDatasource=simDatasources.get(i);
			for (int j = i+1; j < simDatasources.size(); j++) {
				SimDatasource simDatasourceOther=simDatasources.get(j);
				if (simDatasource.getDeviceIp().equals(simDatasourceOther.getDeviceIp())
						&& simDatasource.getSecurityObjectType().equals(simDatasourceOther.getSecurityObjectType())) {
						removedDatasources.add(simDatasourceOther);
				}
			}
		}
		if (removedDatasources.size()>0) {
			simDatasources.removeAll(removedDatasources);
		}
	}
	private void resetPlanConfig(SID sid,@RequestBody PlanConfig planConfig){
		 if ("ALL_ROLE_ADDRESS".equals(planConfig.getDeviceIp())
				 || "ONLY_BY_DVCTYPE".equals(planConfig.getDeviceIp())) {
			 List<SimDatasource> hasroleDatasources= roleDsByLogType(sid,planConfig.getSecurityObjectType());
			 StringBuffer ipParams=new StringBuffer("DVC_ADDRESS=");
			 StringBuffer idParams=new StringBuffer("RESOURCE_ID=");
			 int size=hasroleDatasources.size();
			 if (1<size) {
				 for (int i = 1; i < size-1; i++) {
					 SimDatasource datasource=hasroleDatasources.get(i);
					 ipParams.append(datasource.getDeviceIp()).append(",");
					 idParams.append(datasource.getResourceId()).append(",");
				 }
				 ipParams.append(hasroleDatasources.get(size-1).getDeviceIp());
				 idParams.append(hasroleDatasources.get(size-1).getResourceId());
				 planConfig.setParams((ipParams.append(";").append(idParams)).toString());
			 }
			 
		 }else{
			 planConfig.setParams("DVC_ADDRESS="+planConfig.getDeviceIp()+";"+"RESOURCE_ID="+planConfig.getResourceId());
		 }
		 
	 }
}
/*InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("");
response.setContentType("application/octet-stream");
response.setHeader("Content-Disposition", "attachment; filename=\"xxx.xxx\"");
ServletOutputStream out = response.getOutputStream();
byte []b=new byte[1024];
int len=-1;
while((len=inputStream.read(b))!=-1){
	out.write(b, 0, len);
}
out.flush();
inputStream.close();*/