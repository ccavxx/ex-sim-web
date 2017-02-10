package com.topsec.tsm.sim.sysconfig.web;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.topsec.tal.base.index.template.DeviceTypeTemplate;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.index.template.LogFieldPropertyFilter;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.Item;
import com.topsec.tal.base.util.config.webitems.PasswordItem;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.AuthRole;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.license.util.LicenceStateConstants;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.node.component.handler.AutoProtectConfigration;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.rest.server.common.HttpUtil;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.common.exception.DataAccessException;
import com.topsec.tsm.sim.common.web.SecurityRequestBody;
import com.topsec.tsm.sim.newreport.mail.EmailReport;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.exp.ExpMailReport;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.sysconfig.bean.TreeData;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.FtpConfigUtil;
import com.topsec.tsm.sim.util.LicenceServiceUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.SystemConfigDispatcher;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.SystemInfo.StorePath;
import com.topsec.tsm.util.encrypt.RSAUtil;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("systemConfig")
public class SystemConfigController {

	protected static Logger log= LoggerFactory.getLogger(SystemConfigController.class);
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	@Autowired
	private EventResponseService eventResponseService;
	
	/**
	 * 修改邮件服务器配置<br>
     *{"responseId" : "4028cbd04211d573014211d6164e0002",<br>
     * "mailserver" : {<br>
     *		"serverip" : "192.168.66.9",<br>
     *		"serverport" : "25",<br>
     *		"mailsender" : "feng_liming@topsec.com.cn",<br>
     *		"loginaccount" : "feng_liming",<br>
     *		"loginpwd" : "feng_liming"<br>
     *  }<br>
     *}<br>
	 * @param sid
	 * @param config
	 * @return
	 */
	@RequestMapping("modifyMailServerConfig")
	@ResponseBody
	public Result modifyMailServerConfig(SID sid, @SecurityRequestBody @RequestBody Map<String, Object> config,HttpServletRequest request) {
		Result result = new Result(true, "保存成功！");
		String respId = (String) config.get("responseId");
		Response resp = eventResponseService.getResponse(respId);
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			Map<String,Object> serverConfig = (Map<String, Object>) config.get("mailserver") ;
			if(!serverConfig.containsKey("ssl")){
				serverConfig.put("ssl", "false") ;
			}
			updateConfig(conf, "mailserver", serverConfig);
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			EmailReport.setMailServerConfig(conf);
			SystemConfigDispatcher.getInstance().sendMail(conf, nodeMgrFacade, resp, sid.getUserName(),request);
		}catch(CommonUserException e){
			result.buildError(e.getMessage()) ;
		} catch (Exception e) {
			result.buildError("保存失败！");
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 更新配置信息
	 * @param config
	 * @param blockKey
	 * @param editCfg
	 */
	private void updateConfig(Config config, String blockKey, Map<String, Object> editCfg) {
		for (Block block : config.getCfgBlocks()) {
			if (block.getKey().equals(blockKey)) {
				block.setGroupSelect(true);
				for (Item item : block.getCfgItems()) {
					String key = item.getKey();
					if (editCfg.containsKey(key)) {
						Object value = editCfg.get(key) ;
						if(value instanceof String){
							item.setValue(item instanceof PasswordItem ? CommonUtils.decrypt((String)value) : (String)value);
						}else{
							item.setValueList((ArrayList)editCfg.get(key));
						}
					}
				}
			}else{
				block.setGroupSelect(false);
			}

		}
	}
	
	/**
	 * 修改日志备份策略
		// --config--		
		//		{
		//			"sys_cfg_backup_id" : "4028cbd04211d573014211d6164e0004",
		//			"sys_cfg_backup_auto_id" : "4028cbd04211d573014211d6164e0005",
		//			"autoback" : {
		//				"autobackManner" : "3m",
		//				"partitionCount" : "6m",
		//				"enable" : false
		//			},
		//			"ftp" : {
		//				"serverip" : "192.168.75.20",
		//				"user" : "xx",
		//				"password" : "111",
		//				"encoding" : "GB2312"
		//			}
		//		}
		// --或--		
		//		{
		//			"sys_cfg_backup_id" : "4028cbd04211d573014211d6164e0004",
		//			"sys_cfg_backup_auto_id" : "4028cbd04211d573014211d6164e0005",
		//			"autoback" : {
		//				"autobackManner" : "3m",
		//				"partitionCount" : "6m",
		//				"enable" : false
		//			},
		//			"local" : {
		//				"path" : "C:\\chart"
		//			}
		//		}	 * @param sid
	 * @param config
	 * @return
	 */
	@RequestMapping("modifyLogBackupConfig")
	@ResponseBody
	public Result modifyLogBackupConfig(SID sid, @SecurityRequestBody @RequestBody Map<String,Object> config,HttpServletRequest request) {
		Result result = new Result(true, "备份策略修改成功！");
		String respId = (String) config.get("sys_cfg_backup_id");
		Response resp = eventResponseService.getResponse(respId);
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			String backup = config.containsKey("ftp") ? "ftp" : "local";
			if(backup.equals("ftp")){
				Map<String, Object> ftpConfig = (Map<String, Object>) config.get("ftp");
 				String serverip = ftpConfig.get("serverip").toString();
 				String user =  ftpConfig.get("user").toString();
 				String password = CommonUtils.decrypt((String)ftpConfig.get("password"),"");
 				boolean isConn = FtpUploadUtil.testConnection(serverip, 21, user, password,3000);
 				if(!isConn){
 				   result= new Result(false, "连接FTP失败，请检查FTP是否可用！");
 				   return result;
 				}
			}
			updateConfig(conf, backup, (Map<String,Object>)config.get(backup));
			
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_INDEXSERVICE, false, false, false, false);
			String nodeId = nodes.get(0).getNodeId();
			Response response = eventResponseService.getResponsesByNodeIdAndCofingKey(nodeId, "sys_cfg_store");
			Config storeConfig = RespCfgHelper.getConfig(response) ;
			if(backup.equals("local") && backupPartitionInStorePartition(conf, storeConfig)){
				return result.buildError("备份路径不能与存储路径位于同一分区！") ;
			}
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendLogBackupPath(conf, nodeMgrFacade, resp, request, sid.getUserName());
			
			respId = (String) config.get("sys_cfg_backup_auto_id");
			resp = eventResponseService.getResponse(respId);
			conf = RespCfgHelper.getConfig(resp);
			Map<String, Object> autobackTemp = (Map<String,Object>)config.get("autoback");
			Object enable = autobackTemp.get("enable");
			if(enable == null){
				autobackTemp.put("enable", "false");
			}
			updateConfig(conf, "autoback", autobackTemp);
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendLogBackupAuto(conf, nodeMgrFacade, resp, request, sid.getUserName());
			
			
		} catch (Exception e) {
			result = new Result(false, "备份策略修改失败！");
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 判断备份路径所属分区是否在存储路径分区中
	 * @param backupConfig
	 * @param storeConfig
	 * @return
	 * @throws Exception
	 */
	private boolean backupPartitionInStorePartition(Config backupConfig,Config storeConfig) throws Exception{
		if(backupConfig == null){
			return false ;
		}
		Block block = backupConfig.getBlockbyKey("local") ;
		if(block==null || StringUtil.isBlank(block.getItemValue("path"))){
			return false ;
		}
		String backupPath = block.getItemValue("path") ;
		if(StringUtil.isBlank(backupPath)){
			return false ;
		}
		Block storeBlock = storeConfig.getBlockbyKey("archive_path") ;
		String archivePathList = StringUtil.ifBlank(storeBlock.getItemValue("archive_path_list"),storeBlock.getItemValue("archive_path")) ;
		String[] storePathList = StringUtil.split(archivePathList,";") ;
		String[] storePathPartition = new String[storePathList.length] ;
		String backupPartition ;
		if(SystemUtils.IS_OS_WINDOWS){
			backupPartition = backupPath.substring(0,backupPath.indexOf(':')) ;
			for(int i=0;i<storePathList.length;i++){
				String storePath = storePathList[i] ;
				storePathPartition[i] = storePath.substring(0,storePath.indexOf(':')).toLowerCase() ;//获取磁盘
			}
			if(ArrayUtils.indexOf(storePathPartition, backupPartition.toLowerCase()) > -1){
				return true ;
			}
		}else{
			Node auditorNode = nodeMgrFacade.getKernelAuditor(false);
			try {
				//将存储路径与备份路径打包到一个列表中，向下级节点发送路径查询所属分区的请求
				String[] params = new String[storePathList.length+1] ;
				params[0] = backupPath ;//备份路径放入第一个位置
				System.arraycopy(storePathList, 0, params, 1, storePathList.length) ;
				String[] pathPartition = (String[]) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditorNode), MessageDefinition.CMD_QUERY_PATH_PARTITION, params, 60*1000) ;
				backupPartition = pathPartition[0] ;
				System.arraycopy(pathPartition, 1, storePathPartition, 0, storePathPartition.length) ;
				if(ArrayUtils.indexOf(storePathPartition, backupPartition) > -1){
					return true ;
				}
			} catch (Exception e) {
				throw e ;
			}
		}
		return false ;
	}
	
	/**
	 * 修改采集器端口配置
//		{
//			"responseId" : "4028cbc644f703750144f70532be0004",
//			"netflow" : {
//				"netflow_port" : ["9991"]
//			},
//			"snmp" : {
//				"snmp_port" : ["162"]
//			},
//			"syslog" : {
//				"syslog_port" : ["514"]
//			}
//		}	
	 * @param sid
	 * @param config
	 * @return
	 */
	@RequestMapping("modifyCollectorPortConfig")
	@ResponseBody
	public Result modifyCollectorPortConfig(SID sid, @SecurityRequestBody @RequestBody Map<String,Object> config,HttpServletRequest request){
		Result result = new Result(true, "保存成功！");
		String respId = (String) config.get("responseId");
		Response resp = eventResponseService.getResponse(respId);
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			updateConfig(conf, "netflow", (Map<String, Object>) config.get("netflow"));
			updateConfig(conf, "snmp", (Map<String, Object>) config.get("snmp"));
			updateConfig(conf, "syslog", (Map<String, Object>) config.get("syslog"));
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendPort(conf, nodeMgrFacade, resp, request, sid.getUserName());
		} catch (Exception e) {
			result = new Result(false, "保存失败！");
			e.printStackTrace();
		}	
		return result;
	}
	
	/**
	 * 修改syslog日志转发配置
//		{
//			"responseId" : "4028cbc644f703750144f70532be0002",
//			"sendlog" : {
//				"receivers" : "192.168.1.1",
//				"port" : "514",
//				"frequency" : "1000",
//				"send" : "false",
//				"filterSql" : "SELECTOR(DVC_TYPE = 'aaaaa' )"
//			}
//		}
	 * @param configId
	 * @param config
	 * @return
	 */
	@RequestMapping("modirySyslogForwardConfig")
	@ResponseBody	
	public Result modirySyslogForwardConfig(SID sid,@SecurityRequestBody @RequestBody Map<String,Object> config,HttpServletRequest request){
		Result result = new Result(true, "保存成功！");
		String respId = (String) config.get("responseId");
		Response resp = eventResponseService.getResponse(respId);
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			Map<String, Object> sendlogTemp = (Map<String, Object>) config.get("sendlog");
			Object send = sendlogTemp.get("send");
			if(send == null){
				sendlogTemp.put("send","false");
			}
			if(!sendlogTemp.containsKey("isIncludeLogDeviceIp")){
				sendlogTemp.put("isIncludeLogDeviceIp", "false") ;
			}
			updateConfig(conf, "sendlog", sendlogTemp);
			Block block = conf.getBlockbyKey("sendlog") ;
			Item item = block.getItembyKey("onlyRawLog") ;
			String onlyRawLog = (String) (sendlogTemp.containsKey("onlyRawLog") ? sendlogTemp.get("onlyRawLog") : "false");
			if (item == null) {
				block.addItem(new Item("onlyRawLog",onlyRawLog)) ;
			}else{
				item.setValue(onlyRawLog) ;
			}
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendLogSend(conf, nodeMgrFacade, resp, request, sid.getUserName());
		} catch(CommonUserException e){
			result = new Result(false, e.getMessage()) ;
		} catch (Exception e) {
			result = new Result(false, "保存失败！");
			e.printStackTrace();
		}	
		return result;
	}
	
	/**
	 * 修改jms日志转发配置
//		{
//			"responseId" : "4028cbc644f703750144f70532be0003",
//			"jmssendlog" : {
//				"receivers" : "192.168.1.1",
//				"port" : "61616",
//				"topic" : "com.topsec.tsm.event",
//				"user" : "admin",
//				"pass" : "admin",
//				"encrypt" : "true",
//				"frequency" : "5000",
//				"send" : "true",
//				"filterSql" : "SELECTOR(TRUE)"
//			}
//		} 
	 * @param configId
	 * @param config
	 * @return
	 */
	@RequestMapping("modiryJMSForwardConfig")
	@ResponseBody	
	public Result modiryJMSForwardConfig(SID sid,@SecurityRequestBody @RequestBody Map<String,Object> config,HttpServletRequest request){
		Result result = new Result(true, "保存成功！");
		String respId = (String) config.get("responseId");
		Response resp = eventResponseService.getResponse(respId);
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			Map<String, Object> jmssendlogTemp = (Map<String, Object>) config.get("jmssendlog");
			Object send = jmssendlogTemp.get("send");
			if(send == null){
				jmssendlogTemp.put("send","false");
			}
			updateConfig(conf, "jmssendlog", jmssendlogTemp);
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendJMSLogSend(conf, nodeMgrFacade, resp, request, sid.getUserName());
		} catch (Exception e) {
			result = new Result(false, "保存失败！");
			e.printStackTrace();
		}	
		return result;
	}

	/**
	 * 修改报表存储策略
//		{
//			"reportcfg" : {
//				"activereport" : "0",
//				"passivereport" : "0",
//				"systemreport" : "0"
//			},
//			"responseId" : "4028cb9444ce9ad80144ceb04d720006"
//		}	 
	 * @param sid
	 * @param config
	 * @return
	 */
	@RequestMapping("modifyReportBackupConfig")
	@ResponseBody
	public Result modifyReportBackupConfig(SID sid,@SecurityRequestBody @RequestBody Map<String,Object> config,HttpServletRequest request){
		Result result = new Result(true, "保存成功！");
		String respId = (String) config.get("responseId");
		Response resp = eventResponseService.getResponse(respId);
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			updateConfig(conf, "reportcfg", (Map<String, Object>) config.get("reportcfg"));
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendReport(conf, nodeMgrFacade, resp, request, sid.getUserName());
		} catch (Exception e) {
			result = new Result(false, "保存失败！");
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 更新日志存储策略
//		{
//			"archive" : {
//				"systemlongevity" : "6m"
//			},
//			"archive_path" : {
//				"alert" : "70",
//				"archive_path" : "C:\\Events",
//				"override" : "90"
//			},
//			"responseId" : "4028cb9444ce9ad80144ceb04d720004"
//		}	 
	 * @param sid
	 * @param config
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("modifyLogStoreConfig")
	@ResponseBody	
	public Result modifyLogStoreConfig(SID sid,@SecurityRequestBody @RequestBody Map<String,Object> config,HttpServletRequest request){
		Result result = new Result(true, "存储策略修改成功！");
		String respId = (String) config.get("responseId");
		Response resp = eventResponseService.getResponse(respId);
		try {
			Config conf = RespCfgHelper.getConfig(resp);
			Map<String,Object> pathInfo = (Map<String, Object>) config.get("archive_path") ;
			//pathInfo.put("archive_path_list",pathInfo.get("archive_path_list")) ;
			Block archiveBlock = conf.getBlockbyKey("archive_path") ;
			String oldArchivePathList = StringUtil.nvl(archiveBlock.getItemValue("archive_path_list"),archiveBlock.getItemValue("archive_path")) ;
			archiveBlock.addItemValue("archive_path_list",(String)pathInfo.get("archive_path_list")) ;
			updateConfig(conf, "archive_path", pathInfo);
			if(!storePathAvailable(StringUtil.split(oldArchivePathList,";"),conf, pathInfo, result)){
				return result;
			}
			RespCfgHelper.setConfig(resp, conf);
			eventResponseService.updateResponse(resp);
			SystemConfigDispatcher.getInstance().sendSyslogStore(conf, nodeMgrFacade, resp, request, sid.getUserName());
			SystemConfigDispatcher.getInstance().sendLogPath(conf, nodeMgrFacade, resp, request, sid.getUserName());
			SystemConfigDispatcher.getInstance().sendLogProtection(nodeMgrFacade, StringUtil.booleanVal(config.get("enable_log_protection")), conf) ;
		} catch (Exception e) {
			result.buildError("存储策略修改失败！");
			e.printStackTrace();
		}		
		return result;
	}
	/**
	 * 判断存储路径是否可用
	 * @param conf
	 * @param pathInfo
	 * @param result
	 * @return
	 */
	private boolean storePathAvailable(String[] oldArchivePathList,Config conf,Map<String,Object> pathInfo,Result result){
		try{
			String pathListString = (String) pathInfo.get("archive_path_list") ;
			String[] pathList = StringUtil.split(pathListString,";");
			int override = StringUtil.toInt(pathInfo.get("override").toString()) ;
			if(override > 95 || override < 11){
				result.buildError("磁盘使用率范围11%-95%") ;
				return false ;
			}
			int alarmThreshold = StringUtil.toInt(pathInfo.get("alert").toString()) ;
			if(alarmThreshold > 94 || alarmThreshold < 10){
				result.buildError("磁盘告警上限范围10%-94%") ;
				return false ;
			}
			int usePercent = getDiskUsePercent(pathList) ;
			if(alarmThreshold <= usePercent){
				result.buildError("当前磁盘使用率为"+usePercent+"%,磁盘告警上限必须大于实际使用率！") ;
				return false ;
			}
			try {
				Node auditorNode = nodeMgrFacade.getKernelAuditor(false);
				StorePath[] pathDetailInfo = (StorePath[]) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditorNode), MessageDefinition.CMD_QUERY_PATH_INFO, pathList, 60*1000) ;
				//<partition,path>
				Map<String,String> partitionMap = new HashMap<String,String>() ;
				//检测分区是否可用，分区最少可用空间必须大于10G且多个路径不能指向同一分区
				long partitionMinSpace = 10 ;
				for(int i = 0 ;i < pathDetailInfo.length;i++){
					StorePath info = pathDetailInfo[i] ;
					if(info == null){
						result.buildError(pathList[i]+"不可用！") ;
						return false ;
					}
					//如果是新增加的分区，则新增分区的空间必须大于最小空间限制
					if(!ArrayUtils.contains(oldArchivePathList, info.getPath()) && info.getUsage().availableGB() < partitionMinSpace){
						result.buildError("{}可用空间至少大于{}G",info.getPath(),partitionMinSpace) ;
						return false ;
					}
					if(partitionMap.containsKey(info.getPartition())){//包含重复的分区
						result.buildError("{}与{}位于同一分区！",info.getPath(),partitionMap.get(info.getPartition()));
						return false ;
					}
					partitionMap.put(info.getPartition(),info.getPath()) ;
				}
			} catch (Exception e) {
				result.buildError("存储路径查询命令下发出错，存储策略修改失败!") ;
				return false ;
			}
			Response response = eventResponseService.getResponsesByNodeIdAndCofingKey(NodeDefinition.NODE_TYPE_SMP, "sys_cfg_backup");
			if(response != null){
				Config backupConfig = RespCfgHelper.getConfig(response) ;
				if(backupPartitionInStorePartition(backupConfig, conf)){
					result.buildError("存储路径不能与备份路径位于同一分区！") ;
					return false ;
				}
			}
		}catch(CommunicationException e){
			result.buildError("查询磁盘使用率超时，存储策略修改失败!") ;
			return false ;
		}catch(Exception e){
			result.buildError("查询磁盘使用率出错，存储策略修改失败！") ;
			return false ;
		}
		return true ;
	}
	/**
	 * 获取路径所有分区的磁盘使用率
	 * @param path
	 * @return
	 */
	private int getDiskUsePercent(String[] pathList)throws Exception{
		Node auditor = nodeMgrFacade.getKernelAuditor(false) ;
		Double userPercent = (Double) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_QUERY_DISK_PERCENT, pathList,30000) ;
		if(userPercent == null){
			throw new Exception("磁盘使用率为空！") ;
		}
		return userPercent.intValue() ;
	}
	@RequestMapping("getCfgResponse")
	@ResponseBody
	public Object getConfigResponse(SID sid,@RequestParam("cfgKey") String cfgKey,@RequestParam("nodeType") String nodeType) {
		Result result = new Result();
		//1.根据节点类型获取节点列表
		List<Node> nodes = nodeMgrFacade.getNodesByType(nodeType, false, false, false, false);
		if (nodes.isEmpty())
			return new Result(false, "");
		//默认节点ID
		String defaultNodeId = nodes.get(0).getNodeId();
		//2.根据节点ID和配置KEY获取这个节点的配置信息
		Response response = eventResponseService.getResponsesByNodeIdAndCofingKey(defaultNodeId, cfgKey);
		
		Map<String,Object> configMap = new HashMap<String,Object>();
		configMap.put("responseId", response.getId());
		try {
			Config config = RespCfgHelper.getConfig(response);
			for(Block block:config.getCfgBlocks()){
				Map<String,Object> blockMap = new HashMap<String,Object>();
				if(!config.getGroupList().isEmpty()){
					//当存在多个group时只加载选择的那个
					if(block.isGroupSelect()){
						putBlockItemsToMap(sid, block, blockMap) ;
					}
				}else{
					putBlockItemsToMap(sid, block, blockMap) ;
				}
				if(!blockMap.isEmpty()){
					String blockKey = block.getKey();
					configMap.put(blockKey, blockMap);
				}
			}
			configMap.put("isSoft",CommonUtils.isSoftwarePlatform());//true：版本号是软件化
		    configMap.put("isWindows", SystemUtils.IS_OS_WINDOWS);
		} catch (I18NException e) {
			e.printStackTrace();
		}
		result.setResult(configMap);
		return result;
	}
	
	private static void putBlockItemsToMap(SID sid,Block block,Map<String,Object> blockMap){
		//否则处理所有
		for(Item item:block.getCfgItems()){
			if(item instanceof PasswordItem){
				blockMap.put(item.getKey(), CommonUtils.encrypt(sid, item.getValue())) ;
			}else{
				blockMap.put(item.getKey(), (item.getValueList().isEmpty()) ? item.getValue() : item.getValueList());
			}
		}
	}
	
	@RequestMapping(value = "logProtectionEnabled")
	@ResponseBody
	public Object logProtectionEnabled(){
		Node auditor = nodeMgrFacade.getKernelAuditor(false, false, true, true) ;
		AutoProtectConfigration config = NodeUtil.findFirstSegmentConfig(auditor, NodeDefinition.HANDLER_AUTOPROTECT, AutoProtectConfigration.class) ;
		boolean enabled = config != null && config.isEnableProtected() ;
		JSONObject result = new JSONObject() ;
		result.put("enabled", enabled) ;
		return result ;
	}
	/**
	 * 获取本地目录
	 * @param parentDir
	 * @return
	 */
	@RequestMapping(value = "getLocalFileDirectory",method=RequestMethod.POST)
	@ResponseBody
	public Object getLocalFileDirectory(HttpServletRequest request) {
		String parentDir = request.getParameter("id");
		if (parentDir == null)
			return getLocalFileRoots();
		return getDirectoryLists(parentDir);
	}
	/**
	 * 获取本地文件系统根目录
	 * 
	 * @return
	 */
	private List getLocalFileRoots() {
		List listroots = new ArrayList();
		File[] roots = File.listRoots();
		for (File file : roots) {
			if (file.getTotalSpace() <= 0)
				continue;
			TreeData treedata = new TreeData(file.getPath());
			Map attr = new HashMap();
			attr.put("path", file.getPath());
			attr.put("totalSpace", CommonUtils.formatterDiskSize(file.getTotalSpace()));
			attr.put("freeSpace", CommonUtils.formatterDiskSize(file.getFreeSpace()));
			treedata.setId(file.getPath());
			treedata.setAttributes(attr);
			listroots.add(treedata);
		}
		return listroots;
	}
	/**
	 * 获取指定文件夹下所有子文件夹
	 * @param parentDirPath
	 * @return
	 */
	private List getDirectoryLists(String parentDirPath) {
		List lists = new ArrayList();
		if (parentDirPath != null) {
			File file = new File(parentDirPath);
			File[] childDir = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory() && !StringUtil.matchPattern(pathname.getName(),CommonUtils.REGEX_ZH) && !pathname.isHidden())
						return true;
					return false;
				}
			});

			for (File dir : childDir) {
				TreeData treedata = new TreeData(dir.getName());
				Map attr = new HashMap();
				attr.put("path", dir.getPath());
				treedata.setId(dir.getPath());
				treedata.setAttributes(attr);
				lists.add(treedata);
			}
		}
		return lists;
	}
	
	/**
	 * 下载Agent代理文件
	 *  @param request
	 *  @param response
	 *  @return
	 *  @throws Exception
	 */
	@RequestMapping(value="downloadAgentFile")
	public Object downloadAgentFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = (String) request.getParameter("ftpfilepath");
		if(filename.equals("action.rar") || filename.equals("agent.rar")){
			response.setHeader("Content-Type", "application/octet-stream");
			response.setContentType("text/html;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			CommonUtils.setHeaders4Download(response) ;
			String serverHome = System.getProperty("jboss.server.home.dir");
			String downPath=(String)FtpConfigUtil.getInstance().getFTPConfigByKey("agent").get("downPath");
			if (SystemUtils.IS_OS_WINDOWS) {
				downPath = StringUtils.replace(downPath, "/", File.separator);
			}else{
				downPath = StringUtils.replace(downPath, "\\", File.separator);
			}
			String savaLogPathfile = new StringBuilder(serverHome).append(downPath).append(filename).toString();
	
			File file = new File(savaLogPathfile);
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				bos = new BufferedOutputStream(response.getOutputStream());
				byte[] buff = new byte[2048];
				int bytesRead;
				while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
				bos.flush();
			} catch (ClientAbortException e){
				//客户端取消
	
			}catch (Exception e) {
				log.error("下载文件出错....");
				log.error(e.getMessage());
			} finally {
				ObjectUtils.close(bis) ;
				ObjectUtils.close(bos) ;
			}
		}
		return null;
	}
	
	// 限制文件的上传大小
	private long maxPostSize = 1024L * 1024L;
	private final static String TMP_LICENSE_PATH = "../../../../TopAnalyzer/app-server/server/server/default/tmp/";
	
	@RequestMapping(value="licenseImport",produces="text/html;charset=utf-8")
	@ResponseBody
	public Object licenseImport(SID sid,
							    @RequestParam("theLicenseFile") MultipartFile theLicenseFile,
			                    HttpServletRequest request) throws Exception {

		Result result = new Result();
		if (theLicenseFile == null) {
			return JSON.toJSONString(result.buildError("许可文件丢失")) ;
		}

		//判断上传文件的大小
		if (theLicenseFile.getSize() >maxPostSize)  {
			return JSON.toJSONString(result.buildError("许可文件大小异常"));
		}

		//判断文件路径是否存在
		File basefile = new File(TMP_LICENSE_PATH + theLicenseFile.getOriginalFilename());
		if (!basefile.exists()) {
			FileUtils.forceMkdir(basefile);
		}
		try {
			//将上传的文件写入指定的文件夹
			theLicenseFile.transferTo(basefile);
			Result checkResult = LicenceServiceUtil.checkLicenseFile(basefile, "TAEX-S","TAEX_S") ;
			AuditLogFacade.userOperation("升级许可", sid.getUserName(), checkResult.getMessage(), IpAddress.IPV4_LOCALHOST, 
										 Severity.HIGHEST,AuditCategoryDefinition.SYS_UPGRADE, result.isSuccess());
			log.warn(checkResult.getMessage()) ;
			result.build(checkResult.isSuccess() ,checkResult.getMessage());
		} catch (Exception e){
			result.build(false ,"许可文件操作异常");
		} finally{
			basefile.delete();
		}
		return JSON.toJSONString(result);
	}

	@RequestMapping("/aboutUsView")
	public String aboutUsView(SID sid, HttpServletRequest request) throws Exception {
		UserService userService = (UserService) SpringWebUtil.getBean("userService", request);
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request);
		// 获取角色信息
		Integer accountID = sid.getAccountID();
		AuthAccount authAccount = userService.getUserByID(accountID);
		Set<AuthRole> roles= authAccount.getRoles();
		for (AuthRole authRole : roles) {// tal3.1用户与角色是一对一
			if ("操作管理员".equals(authRole.getName()) || "系统管理员".equals(authRole.getName())) {
				request.setAttribute("showImportLicence", true);
				break;
			}
		}
		
		Map licenseInfo = LicenceServiceUtil.getInstance().getLicenseInfo();
		String talVersion=(String)licenseInfo.get("TAL_VERSION");//版本信息
//		String versionInfo=null;
//
//		if(talVersion.equals(TalVersionUtil.TAL_VERSION_ENTERPRISE)){
//			versionInfo="企业版";
//		}else if(talVersion.equals(TalVersionUtil.TAL_VERSION_STANDARD)){
//			versionInfo="标准版";
//		}else if(talVersion.equals(TalVersionUtil.TAL_VERSION_SIMPLE)){
//			versionInfo="简版";
//		}else if(talVersion.equals(TalVersionUtil.TAL_VERSION_SIM)){
//			versionInfo="sim版";
//		}

		String max_tal_num = (String)licenseInfo.get("TSM_ASSET_NUM");
		String expireTime = (String)licenseInfo.get("EXPIRE_TIME");
		String haspID = (String)licenseInfo.get("HASP_ID");
		String license_valid = (String)licenseInfo.get("LICENSE_VALID");
		String license_error = (String)licenseInfo.get("LICENSE_STATE");

		if (license_valid == null || license_valid.equals("0")) {
			haspID = "未知";
			expireTime = "未知";
			if (license_error != null) {
				if (license_error.equals(LicenceStateConstants.LICENCE_FILE_INVALID)) {
					max_tal_num = "文件错误";
				} else {
					max_tal_num = "License Key 异常";
				}
			}
		}
		if(expireTime==null || expireTime.equals("-1")){
			expireTime = "永不过期";
		}
		if(haspID.equals("0")){
			haspID = "软件许可";
		}else if(haspID.equals("-1")){
			haspID = "试用版";
		}

		request.setAttribute("show", !"true".equals(System.getProperty("FW")));
		request.setAttribute("version", System.getProperty("tal.version"));// 获取版本信息
		request.setAttribute("type", talVersion);// 获取版本型号
		request.setAttribute("haspID", haspID);
		request.setAttribute("expireTime", expireTime);
		request.setAttribute("max_tal_num", max_tal_num);
		request.setAttribute("used_devicetotal", deviceService.getEnabledTotal());
		request.setAttribute("hasOperatorRole", sid.hasOperatorRole());

		return "/page/about/about_us";
	}

	@RequestMapping("/superiorConfigShowUI")
	public String superiorConfigShowUI(SID sid, HttpServletRequest request) throws Exception {
		Node node = nodeMgrFacade.getParentNode();
		if(node !=null){
			request.setAttribute("resourceId", node.getResourceId());
			request.setAttribute("registIp", node.getIp());
			request.setAttribute("registName", node.getResourceName());
		}
//		superiorConfigList(sid, request);
		return "/page/sysconfig/sysconfig_superiorRegist";
	}
	/*
	 * 修改公司系统logo和系统名称及版权所有公司
	 * */
	@RequestMapping(value="modifyCompanyInfo")
	@ResponseBody
	public Object modifyCompanyInfo(@RequestParam(value="companyLogoFile") MultipartFile companyLogoFile,HttpServletRequest request) throws Exception {
		Result result = new Result();
		String companyName = StringUtil.recode(request.getParameter("companyName"));
		String productName = StringUtil.recode(request.getParameter("productName"));
		String companyLogo = companyLogoFile.getOriginalFilename();
		String path = request.getSession().getServletContext().getRealPath("/");
		String logoPath = "";
		if(StringUtil.isNotBlank(companyLogo)){
			if(companyLogoFile.getSize() > 1024*1024){
				return result.buildError("请选择大小在1M以内的公司logo图片");
			}
			logoPath =  "/img/skin/top/user_logo." + FilenameUtils.getExtension(companyLogo) ;
			String filePath = path + logoPath;
			companyLogoFile.transferTo(new File(filePath)) ;
			BufferedImage src = javax.imageio.ImageIO.read(new File(filePath)); 
			int width = src.getWidth();
			int height = src.getHeight();
			if(width > 500 || width < 300 || height > 51 || height < 40){
				return result.buildError("请选择宽在300px~500px之间、高在40px~51px之间的公司logo图片(425px*51px最佳)");
			}
		}
		companyLogo = StringUtil.isBlank(companyLogo) ? CommonUtils.getCompanyLogo() : logoPath;
		companyName = StringUtil.ifBlank(companyName, CommonUtils.getCompanyName()) ;
		productName = StringUtil.ifBlank(productName, CommonUtils.getProductName()) ;
		CommonUtils.setCompanyInfo(companyLogo, companyName,productName);
		return result.build(true,"修改成功");
	}
	
	@RequestMapping(value="restoreDefault",produces="text/html;charset=utf-8")
	@ResponseBody
	public void restoreDefault(HttpServletRequest request) throws Exception {
		CommonUtils.resetDefaultCompanyInfo();
	}
	@RequestMapping("/superiorConfigRegist")
	@ResponseBody
	public Object superiorConfigRegist(SID sid, HttpServletRequest request) throws Exception {
		Result result = new Result(true, "操作成功");
		String operator = request.getParameter("operator");
		String registIp = request.getParameter("registIp");
		String registName = request.getParameter("registName");
		String param="";
		if(registIp.equals(IpAddress.getLocalIp().toString())){
			 result = new Result(false, "不能向本机注册！");
			 return result;
		}
		if(StringUtils.containsAny(registName, "<>'*?:/|\"\\")){
			return result.buildError("名称包含非法字符！") ;
		}
		Node parentNode = nodeMgrFacade.getParentNode();
		Node KernelNode = nodeMgrFacade.getKernelAuditor(false);
		try{
			if("regist".equals(operator)){
				if(parentNode != null){
					return result.buildError("上级已经被注册！");
				}else{
					//向上级注册信息
				    param ="<Register>" +
								"<Ip>"+IpAddress.getLocalIp()+"</Ip>" +//本机物理IP
								"<Alias>"+registName+"</Alias>" +
								"<NodeId>"+KernelNode.getNodeId()+"</NodeId>" +
								"<Type>register</Type>" +
							"</Register>";
				}
			}else if("delete".equals(operator)){
				if(parentNode == null){
					return result.buildError("节点信息已被删除！") ;
				}
				param ="<Register>" +
							"<Ip>"+IpAddress.getLocalIp()+"</Ip>" +
							"<Type>delete</Type>" +
					   "</Register>";
			}else if("update".equals(operator)){
				if(parentNode == null){
					return result.buildError("节点信息已被删除！") ;
				}
				try {
					nodeMgrFacade.delNode(parentNode) ;
					param ="<Register>" +
								"<Ip>"+IpAddress.getLocalIp()+"</Ip>" +
								"<Type>delete</Type>" +
							"</Register>";
					String url = "https://"+parentNode.getIp()+"/resteasy/node/register";
					Map<String,String> cookies = new HashMap<String,String>();
					cookies.put("sessionid",RestUtil.getSessionId(registIp));
					String returnInfo = HttpUtil.doPostWithSSLByString(url, param, cookies, "UTF-8");
					if(StringUtils.isNotBlank(returnInfo)){
						log.info("删除上级节点成功，节点IP:"+parentNode.getIp());
					}
				}catch(ConnectException e){
					log.warn("连接{}失败，可能服务器已经掉线！",parentNode.getIp()) ;
				}
				
				//重新注册上级节点
//				registNode(registIp, registName,KernelNode);
				param ="<Register>" +
								"<Ip>"+IpAddress.getLocalIp()+"</Ip>" +//本机物理IP
								"<Alias>"+registName+"</Alias>" +
								"<NodeId>"+KernelNode.getNodeId()+"</NodeId>" +
								"<Type>register</Type>" +
						"</Register>";
			}
			String url = "https://"+registIp+"/resteasy/node/register";
			Map<String,String> cookies = new HashMap<String,String>();
			cookies.put("sessionid",RestUtil.getSessionId(registIp));
			String returnInfo = HttpUtil.doPostWithSSLByString(url, param, cookies, "UTF-8");
			if(StringUtils.isNotBlank(returnInfo)){
				Document document = DocumentHelper.parseText(returnInfo);
				Element root = document.getRootElement();
				String successResponse = root.attribute("success").getValue();
				if("false".equals(successResponse)) {
					Element elementMessage = root.element("Message");
					result.buildError(elementMessage.getText());
					return result;
				}
				if("regist".equals(operator)) {
					//本地注册上级节点
					registNode(registIp, registName,KernelNode);
					log.info("上级注册成功，节点IP:"+registIp);
					result = new Result(true, "注册成功");
					result.setResult(KernelNode.getResourceId());
				} else if("update".equals(operator)) {
					registNode(registIp, registName,KernelNode);
					log.info("变更节点成功，节点IP:"+registIp);
					result = new Result(true, "上级变更成功");
					result.setResult(KernelNode.getResourceId());
				} else {
					//删除原有的上级节点
					nodeMgrFacade.delNode(parentNode);
					log.info("删除上级节点成功，节点IP:"+parentNode.getIp());
					result = new Result(true, "删除上级节点成功");
				}
			}else{
				result.buildError("操作失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.buildError("操作失败");
		}
		return result;
	}
	/**
	 * 本地注册上级节点
	 * @param registIp
	 * @param registName
	 * @return
	 * @throws DataAccessException
	 * @throws NodeException
	 */
	private boolean registNode(String registIp,String registName,Node kernelAuditor) throws DataAccessException, NodeException{
		//本地注册上级信息
		Node node = new Node();
		node.setIp(registIp);
		node.setType(NodeDefinition.NODE_TYPE_PARENT);
		node.setNodeId(NodeDefinition.NODE_TYPE_PARENT);
		kernelAuditor.setResourceName(registName);
		node.setRouteUrl(kernelAuditor.getRouteUrl());
		node.setParent(kernelAuditor);
		node.setResourceName(registName);
		String[] route = NodeUtil.getRoute(node);
		return nodeMgrFacade.registerNode(node,route);
	}
	
	@RequestMapping("/superiorConfigDeleteChildNode")
	@ResponseBody
	public Object superiorConfigDeleteChildNode(SID sid, HttpServletRequest request) throws Exception {
		
		Result result = new Result(true, "操作成功");
		String registIp = request.getParameter("registIp");
		
		if(StringUtils.isBlank(registIp)) {
			return result.buildError("子节点ip为空！");
		}
		Node node = nodeMgrFacade.getChildByIp(registIp);
		if(node == null) {
			return result.buildError("节点已被删除！") ;
		}
		
		try {
			nodeMgrFacade.delNode(node) ;
			String param = "<Register><Ip>" + IpAddress.getLocalIp() + "</Ip><Type>delete</Type></Register>";
			String url = "https://"+node.getIp()+"/resteasy/node/deleteChildNode";
			Map<String,String> cookies = new HashMap<String,String>();
			cookies.put("sessionid",RestUtil.getSessionId(node.getIp()));
			String returnInfo = HttpUtil.doPostWithSSLByString(url, param, cookies, "UTF-8");
			System.out.println("return = " + returnInfo);
			if(StringUtils.isNotBlank(returnInfo)){
				log.info("删除子节点成功，节点IP:"+node.getIp());
			}
			
		}catch(ConnectException e){
			log.warn("连接{}失败，可能服务器已经掉线！",node.getIp()) ;
		}
		return result.buildSuccess();
	}
	/**
	 * 多级的页面跳转
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/superiorConfigListPage")
	public String superiorConfigListPage(HttpServletRequest request) throws Exception {
		
		String pageSize = request.getParameter("pageSize");
		String pageNo = request.getParameter("pageNo");
		
		request.setAttribute("pageSize", pageSize == null ? 10 : pageSize);
		request.setAttribute("pageNo", pageNo == null ? 1 : pageNo);
		
		return "/page/sysconfig/sysconfig_superiorList";
	}
	
	/**
	 * 加载本地数据
	 * @param sid
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/superiorConfigListFirst")
	@ResponseBody
	public Object superiorConfigListFirst(SID sid, HttpServletRequest request) throws Exception {
		
		JSONObject result = new JSONObject();
		
		JSONObject page = new JSONObject();
		JSONArray jArray = new JSONArray();
		
		String pageSize = request.getParameter("pageSize");
		String pageNo = request.getParameter("pageNo");
		String numFlag = request.getParameter("numFlag");
		
		result.put("numFlag", numFlag);
		if(StringUtil.isBlank(pageSize) || StringUtil.isBlank(pageNo)) {
			result.put("page", page);
			result.put("dataList", jArray);
			return result;
		}
		
		PageBean<Node> pageBean = nodeMgrFacade.queryPageNodes(Integer.valueOf(pageSize), Integer.valueOf(pageNo),
				NodeDefinition.NODE_TYPE_CHILD,false, false, false, false);
		List<Node> nodes = pageBean.getData();
		
		page.put("pageNo", pageBean.getPageNo());
		page.put("pageSize", pageBean.getPageSize());
		page.put("firstNum", pageBean.getFirstNum());
		page.put("lastNum", pageBean.getLastNum());
		page.put("totalPage", pageBean.getTotalPage());
		page.put("total", pageBean.getTotal());
		page.put("haveNext", pageBean.getHaveNext());
		page.put("havePrevious", pageBean.getHavePrevious());
		page.put("haveFirstPage", pageBean.getHaveFirstPage());
		page.put("haveLastPage", pageBean.getHaveLastPage());
		page.put("nextPageNo", pageBean.getNextPageNo());
		page.put("previousPageNo", pageBean.getPreviousPageNo());
		
		if(nodes != null) {
			for(Node node : nodes) {
				JSONObject jObj = new JSONObject();
				jObj.put("sessionid", "");
				jObj.put("registIp", node.getIp());
				jObj.put("registName", node.getResourceName() == null ? 0 : HtmlUtils.htmlEscape(node.getResourceName()));
				jObj.put("cpu_usage", 0);
				jObj.put("mem_usage", 0);
				jObj.put("log_flow", 0);
				jObj.put("storage_usage", 0);
				jObj.put("storage_avaliable", 0);
				jObj.put("offlineAssetCount", 0);
				jObj.put("onlineAssetCount", 0);
				jObj.put("assetCount", 0);
				jObj.put("alarmCount", 0);
				jObj.put("eventCount", 0);
				jArray.add(jObj);
			}
		}
		result.put("page", page);
		result.put("dataList", jArray);
		return result;
	}
	/**
	 * 加载相应远程数据
	 * @param sid
	 * @param ip
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/superiorConfigListSingle")
	@ResponseBody
	public Object superiorConfigListSingle(SID sid, @RequestParam("ip") String ip,@RequestParam("numFlag") String numFlag,
			HttpServletRequest request) throws Exception {
		Node node = nodeMgrFacade.getChildByIp(ip);
		JSONObject jObj = new JSONObject();
		jObj.put("numFlag", numFlag);
		if(node != null) {
			String url = "https://" + node.getIp() + "/resteasy/node/systemInfo";
			Map<String,String> cookies = new HashMap<String,String>();
			cookies.put("sessionid",RestUtil.getSessionId(ip));
			String returnInfo = HttpUtil.doPostWithSSLByMap(url, null, cookies, "UTF-8");
			
			
//			// 发送远程请求
//			ClientRequest cRequest = new ClientRequest("https://" + node.getIp() + "/resteasy/node/systemInfo");
//			cRequest.setHttpMethod("POST");
//			String sessionId = RestUtil.getSessionId(node.getIp()) ;
//			cRequest.cookie("sessionid",sessionId);
			
			String sessionId = RestUtil.getSessionId(node.getIp()) ;
			jObj.put("sessionid", sessionId);
			jObj.put("registIp", node.getIp());
			jObj.put("registName", node.getResourceName()==null ? 0 : node.getResourceName());
//			String info = null;
//			if(!"".equals(sessionId)) {
//				ClientResponse<String> response;
//				response = cRequest.post(String.class);
//				info = response.getEntity();
//			}
			if(StringUtil.isNotBlank(returnInfo)) {
				Document document = DocumentHelper.parseText(returnInfo);
				Element root = document.getRootElement();
				jObj.put("cpu_usage", nvlAttribute(root, "cpu_usage", 0));
				jObj.put("mem_usage", nvlAttribute(root, "mem_usage", 0));
				jObj.put("log_flow", nvlAttribute(root, "log_flow", 0));
				jObj.put("storage_usage", nvlAttribute(root, "storage_usage", 0));
				jObj.put("storage_avaliable", nvlAttribute(root, "storage_avaliable", 0));
				jObj.put("storage_total", nvlAttribute(root, "storage_total", 0));
				jObj.put("offlineAssetCount", nvlAttribute(root, "offlineAssetCount", 0));
				jObj.put("onlineAssetCount", nvlAttribute(root, "onlineAssetCount", 0));
				jObj.put("assetCount", nvlAttribute(root, "assetCount", 0));
				jObj.put("alarmCount", nvlAttribute(root, "alarmCount", 0));
				jObj.put("eventCount", nvlAttribute(root, "eventCount", 0));
			} else {
				jObj.put("cpu_usage", 0);
				jObj.put("mem_usage", 0);
				jObj.put("log_flow", 0);
				jObj.put("storage_usage", 0);
				jObj.put("storage_avaliable", 0);
				jObj.put("storage_total", 0);
				jObj.put("offlineAssetCount", 0);
				jObj.put("assetCount", 0);
				jObj.put("alarmCount", 0);
				jObj.put("eventCount", 0);
			}
		}
		return jObj;
	}
	private static String nvlAttribute(Element element, String attributeName, int defaultValue) {
		if(element == null || element.attribute(attributeName) == null || "null".equals(element.attributeValue(attributeName))){
			return String.valueOf(defaultValue);
		} else {
			String result = element.attributeValue(attributeName);
			return result;
		}
	}
	/**
	 * 根据日志源类型ID 获取过滤器字段
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("getFilterFieldById")
	public String getFilterFieldById(@RequestParam("id")String id,HttpServletRequest request){
		if(id != null){
			DeviceTypeTemplate template = IndexTemplate.getTemplate(id) ;
			request.setAttribute("fieldset", template.getFields(new LogFieldPropertyFilter("visiable",true)));
			request.setAttribute("filterSql", StringUtil.recode(request.getParameter("filterSql"))) ;
		}		
		return "page/sysconfig/filter_editor";
	}
}
