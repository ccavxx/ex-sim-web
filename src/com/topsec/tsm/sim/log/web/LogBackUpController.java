package com.topsec.tsm.sim.log.web;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSONObject;
import com.enterprisedt.net.ftp.FTPClient;
import com.topsec.tal.base.util.BackupException;
import com.topsec.tal.base.util.FTPUtil;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("logBackUp")
public class LogBackUpController {
	private static final Logger log = LoggerFactory.getLogger(LogBackUpController.class);
	@Autowired
	private EventResponseService eventResponseService;
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	
	@RequestMapping(value="checkBkServer",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public String checkBackUpServer(){
    	Boolean isDistributed = nodeMgrFacade.isDistributed();
 	    JSONObject result = new JSONObject();
 	    Response bakResponse = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup").get(0);
 	    Config config=null;
		try {
			config = RespCfgHelper.getConfig(bakResponse);
		} catch (I18NException e4) {
			e4.printStackTrace();
		}
 	    Block backupPath  = config.getBlockbyGroup("backuppath");
 	    if("local" .equalsIgnoreCase(backupPath.getKey()) && true != isDistributed){
 	    	if(backupPath.getItemValue("path") == null ||backupPath.getItemValue("path").isEmpty()){
 	    		result.put("station", "nopath");
 	    	}else{
 	    		result.put("station", "ok");
 	    	}
        }else if("ftp" .equalsIgnoreCase(backupPath.getKey())) {
    		FTPClient client = null;
    		try {
    			client = FTPUtil.getFTPClient(backupPath.getItemValue("serverip"), 
    										  backupPath.getItemValue("user"), 
    										  backupPath.getItemValue("password"), 
    										  backupPath.getItemValue("encoding"));
    			result.put("station", "ok");
    		} catch (Exception e) {
    			try {
    				result.put("station", "ftpivalid");
    			} catch (Exception e1) {
    			}
    			try {
    				FTPUtil.logOut(client);
    			} catch (Exception e2) {
    				try {
    					result.put("station", "ftpivalid");
    				} catch (Exception e3) {
    				}
    			}

    		}
    		try {
    			FTPUtil.logOut(client);
    		} catch (Exception e) {
    		}
        }else if("local" .equalsIgnoreCase(backupPath.getKey()) && true == isDistributed){
        	result.put("station", "nopath");
        }
		return result.toJSONString();
	}

	/**
	* 功能描述: 检查节点是否在线
	* @author zhou_xiaohu@topsec.com.cn
	*/
	@RequestMapping("checkNodeIsOnline")
	@ResponseBody
	public Object checkNodeIsOnline(@RequestParam(value="nodeId")String nodeId) throws Exception {
		nodeId = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, false, false, false).get(0).getNodeId();
		JSONObject jsonObject=new JSONObject(); 		
		if(NodeStatusQueueCache.offline(nodeId)){
			log.warn("auditor节点掉线!");
			jsonObject.put("isOnline", false);
			return null;
		}
		jsonObject.put("isOnline", true);  
		return jsonObject;
	}
	/**
	 * 日志备份
	 * @author zhou_xiaohu@topsec.com.cn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("backupLog")
	@ResponseBody
	public Object backupLog(@RequestParam("dataSourceName") String dataSourceName,
							@RequestParam("startDate")String startDate,
							@RequestParam("endDate")String endDate,
							@RequestParam("dataSourceList") String dataSourceId,
							SID sid,
							HttpServletRequest request) throws Exception {
		   //添加自审计日志
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(AuditCategoryDefinition.SYS_BACKUP_LOG);
		_log.setSecurityObjectName("日志备份管理");
		_log.setDescription("手动备份");
    	_log.setSubject(sid.getUserName());
		_log.setSubjectAddress(new IpAddress(request.getRemoteHost()));
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(true);
		_log.setSeverity(Severity.LOW);
		
		String[] dataSourceList = StringUtil.split(StringUtil.recode(dataSourceId));
		_log.setDescription("手动备份从"+startDate+"到"+endDate+"的日志源名称为:"+StringUtil.recode(dataSourceName)+"日志。");

        //根据日志源名称列表获取日志源
		List<SimDatasource> dataSources = new ArrayList<SimDatasource>();
		DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request) ;
		for(int i=0;i<dataSourceList.length;i++){
        	dataSources.add(dataSourceService.getById(Long.valueOf(dataSourceList[i])));
		}
		JSONObject result = new JSONObject();
		HashMap<String,Object> bakcondition = new HashMap<String, Object>();
		bakcondition.put("start_time", StringUtil.toDate(startDate, "yyyy-MM-dd HH:mm:ss"));
		bakcondition.put("end_time", StringUtil.toDate(endDate, "yyyy-MM-dd HH:mm:ss"));
		bakcondition.put("dataSources", dataSources);
 		try {
 			String[] route = RouteUtils.getIndexServiceRoutes();
			Response resp = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup").get(0);
 			Config config = RespCfgHelper.getConfig(resp);
 			if (config != null) {
 				Block backupBlock = config.getBlockbyGroup("backuppath");
 				String blockKey = backupBlock.getKey();
 				if ("ftp".equals(blockKey)) {
 					boolean available = FtpUploadUtil.testConnection(backupBlock.getItemValue("serverip"), 21, 
 																	 backupBlock.getItemValue("user"), 
 																	 backupBlock.getItemValue("password"));
 					if(!available){
 						result.put("bakresult", "ftp连接失败，请检查！");
 						return result;
 					}
 				}else if("local".equalsIgnoreCase(blockKey)){
 					if(backupBlock == null || StringUtil.isBlank(backupBlock.getItemValue("path"))){
 		 	    		result.put("bakresult", "未设置备份路径，无法进行备份！");
 		 	    		return result;
 		 	    	}
 				}
 				//获取查询结果
				Object backupResult = NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_BAK_LOG,  bakcondition, 2*60*1000);
 	    	    if(backupResult == null){
     				result.put("bakresult", "true");
     				AuditLogFacade.send(_log);//发送系统自审计日志
 	    	    }
 			}
 		} catch(Exception e) {
 			if (e instanceof  com.topsec.tsm.comm.CommunicationExpirationException){
 				_log.setSuccess(false);
 				result.put("bakresult", "数据请求超时!");
 			}else{
 				if (e.getCause() instanceof BackupException) {
 					BackupException bkEx = (BackupException) e.getCause();
 					int type = bkEx.getType();
 					if(type == BackupException.AUTO){
 						result.put("bakresult", "正在进行自动备份任务");
 					}else if(type== BackupException.MANUAL){
 	    	    		result.put("bakresult", "正在手动备份日志, 还有" +bkEx.getLeftCount() +"个日志源在等待备份，请稍候再试！");
 					}else{
 						result.put("bakresult", e.getMessage());
 					}
 			    } else {
 			    	_log.setSuccess(false);
 			       result.put("bakresult", e.getMessage());
 				   e.printStackTrace();
 			    }
 		   } 

 		} finally{
 			AuditLogFacade.send(_log);//发送系统自审计日志
 			return result;
	}
 }


	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}
}
