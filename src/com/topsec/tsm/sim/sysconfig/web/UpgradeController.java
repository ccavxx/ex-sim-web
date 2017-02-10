package com.topsec.tsm.sim.sysconfig.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.metadata.NodeConfigurationFormater;
import com.topsec.tsm.node.metadata.NodeConfigurationMetadata;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.rest.server.common.HttpUtil;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.service.SimNodeUpgradeService;
import com.topsec.tsm.sim.node.service.SimRuleUpgradeService;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.resource.persistence.SimNodeUpgrade;
import com.topsec.tsm.sim.resource.persistence.SimNodeUpgradePlan;
import com.topsec.tsm.sim.resource.persistence.SimRuleUpgrade;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DateUtils;
import com.topsec.tsm.sim.util.FtpConfigUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.UpdateUtil;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("/sysconfig/upgrade")
public class UpgradeController {
	protected Logger log = LoggerFactory.getLogger(getClass());
	public final static String EXTURL = "../../../../patch/";// 服务器升级包保存路径
	private final static long MAX_SIZE = 524288000;  //500M
	private NodeMgrFacade nodeMgrFacade;
	
	public SimNodeUpgradeService getService(HttpServletRequest request) {
		return (SimNodeUpgradeService) SpringWebUtil.getBean("simNodeUpgradeService",request) ;
	}

	public SimRuleUpgradeService getRuleService(HttpServletRequest request) {
		return (SimRuleUpgradeService) SpringWebUtil.getBean("simRuleUpgradeService",request) ;
	}

	public NodeMgrFacade getNodeService(HttpServletRequest request) {
		return com.topsec.tsm.sim.util.FacadeUtil.getNodeMgrFacade(request, null);
	}
	
	/* 服务器升级页面 */
	@RequestMapping("/serverUpgrade")
	public String serverUpgrade( HttpServletRequest request) throws Exception {
		request.setAttribute("version", System.getProperty("tal.version"));// 获取版本信息
		return "/page/sysconfig/sysconfig_serverUpgrade";
	}
	
	/* 设置节点升级计划页面 */
	@RequestMapping("/nodeUpgrade")
	public String nodeUpgrade( HttpServletRequest request) throws Exception {
		// 权限
		request.setAttribute("deleteFlag", true);
		return "/page/sysconfig/sysconfig_nodeUpgrade";
	}
	
	/* 设置节点升级计划页面 */
	@RequestMapping("/nodeUpgradeRefreshPlan")
	public String nodeUpgradeRefreshPlan( HttpServletRequest request) throws Exception {
		
		String planType = (String)UpdateMgrConfig.getInstance().getUpdateMgrConfigUtilValueByName("planType");
		String timeExpressionString = (String)UpdateMgrConfig.getInstance().getUpdateMgrConfigUtilValueByName("timeExpression");
		String date = (String)UpdateMgrConfig.getInstance().getUpdateMgrConfigUtilValueByName("date");
		if("perTime".equals(planType) && StringUtils.isNotBlank(timeExpressionString)){
			int index = timeExpressionString.indexOf(':') ;
			String timeExString = timeExpressionString.substring(index + 1);
			TimeExpression timeExpression = new TimeExpression(timeExpressionString.substring(0,index),timeExString);
			request.setAttribute("timerExpression", timeExpression);
		}
		request.setAttribute("planType", planType);
		request.setAttribute("date", date);
		
		return "/page/sysconfig/sysconfig_nodeUpgrade_refreshPlan";
	}
	
	/* 校验文件是否已经存在 */
	@RequestMapping("/checkFile")
	@ResponseBody
	public Object checkFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Result result = new Result(false, null);
		String fileName = request.getParameter("name");// 文件名
		String type = request.getParameter("type");// 识别是服务器升级还是节点升级
		if (StringUtils.isNotBlank(fileName) && StringUtils.isNotBlank(type)) {
			try {
				if ("server".equals(type)) {// 服务器
					File webUpdateDir = new File(EXTURL);
					if (webUpdateDir.exists()) {
						File[] listFiles = webUpdateDir.listFiles();
						for (File file : listFiles) {
							if (file.isFile()) {
								file.delete();// 删除所有文件
							}
						}
					} else {
						webUpdateDir.mkdirs();// 没有就创建
					}
					result.setStatus(true);
					result.setMessage(null);
				} else if ("node".equals(type)) {// 节点
					SimNodeUpgradeService service = getService(request);
					SimNodeUpgrade simNodeUpgrade = service.getSimNodeUpgradeByName(fileName);
					if (simNodeUpgrade == null) {// 不存在
						result.setStatus(true);
						result.setMessage(null);// 可用不重复
					}
				} else if ("rule".equals(type)) {// 事件规则库
					SimRuleUpgradeService service = getRuleService(request);
					SimRuleUpgrade simRuleUpgrade = service.getSimRuleUpgradeByName(fileName);
					if (simRuleUpgrade == null) {// 不存在
						result.setStatus(true);
						result.setMessage(null);// 可用不重复
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	/**
	 * 服务器升级上传文件
	 */
	@RequestMapping(value="uploadFile",produces="text/html;charset=utf-8")
	@ResponseBody
	public Object uploadFile(@RequestParam("theFile") MultipartFile file,
			HttpSession session,
			HttpServletRequest request) throws Exception {
		File webUpdateFile = null;
		boolean usable = false;
		boolean restart = true;// 是否重新启动以更新
		SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
		JSONObject result = new JSONObject() ;
		try {
			//判断上传文件的大小
			if (file.getSize() > MAX_SIZE)  {
				AuditLogFacade.userOperation("服务器升级",sid.getUserName(), "服务器升级上传文件失败:文件过大",new IpAddress(sid.getLoginIP()),Severity.HIGH,AuditCategoryDefinition.SYS_UPGRADE,false);
				log.warn("服务器升级上传的文件过大");
				result.put("resultLoad", "tooLarge");
				return result ;
			}
			//获取文件名称
			String fileName=file.getOriginalFilename();
			
			File dirFile = new File(EXTURL);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			webUpdateFile = new File(EXTURL + fileName);
			//将上传的文件写入指定的文件夹
			file.transferTo(webUpdateFile);

			usable = UpdateUtil.checkFile(webUpdateFile.getAbsolutePath(), fileName);
			if (usable) {
				boolean versionAble = UpdateUtil.checkVersion(webUpdateFile.getAbsolutePath());
				restart = UpdateUtil.checkRestart(webUpdateFile.getAbsolutePath());
				if (versionAble) {
					if (restart) {
						result.put("resultLoad", "success");
					}
				} else {// 版本过低
					result.put("resultLoad", "oVersion");
				}
			} else {// 不可用
				result.put("resultLoad", "disable");
				if (webUpdateFile.exists() && webUpdateFile.isFile()) {
					webUpdateFile.delete();
				}
			}

		} catch (Exception e) {
			AuditLogFacade.userOperation("服务器升级",sid.getUserName(), "服务器升级上传的文件不可用"+e.getMessage(),new IpAddress(sid.getLoginIP()),Severity.HIGH,AuditCategoryDefinition.SYS_UPGRADE,false);
			log.error("服务器升级上传的文件不可用" + e.getMessage());
		} finally {
			File versionFile = new File(EXTURL + "/patch-info.xml");
			if (versionFile.exists() && versionFile.isFile()) {
				versionFile.delete();
			}
		}
		AuditLogFacade.userOperation("服务器升级",sid.getUserName(), "服务器升级上传的文件",new IpAddress(sid.getLoginIP()),Severity.LOW,AuditCategoryDefinition.SYS_UPGRADE,true);
		result.put("version", System.getProperty("tal.version"));
		if (restart) {
			result.put("result", "success") ;
		} else {
			try {
				//new NodeUpdateManager(EXTURL, null).execute();
				AuditLogFacade.userOperation("服务器升级",sid.getUserName(), "服务器升级成功",new IpAddress(sid.getLoginIP()),Severity.LOWEST,AuditCategoryDefinition.SYS_UPGRADE,true);
			} catch (Exception e) {
				result.put("resultLoad", "2") ;
				AuditLogFacade.userOperation("服务器升级",sid.getUserName(), "服务器升级失败",new IpAddress(sid.getLoginIP()),Severity.LOWEST,AuditCategoryDefinition.SYS_UPGRADE,false);
			}
			result.put("resultLoad", "1") ;
			result.put("result", "success");
		}
		return result.toString() ;
	}
	
	/* 节点升级列表 */
	@SuppressWarnings("unchecked")
	@RequestMapping("queryUpdateNodeList")
	@ResponseBody
	public Object queryUpdateNodeList(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "rows", defaultValue = "10") Integer pageSize,
			HttpServletRequest request,
			HttpSession session) throws Exception {

		JSONObject resultJson = new JSONObject();
		UserService userService = (UserService) SpringWebUtil.getBean("userService", request);
		SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
		// 分页显示
		SimNodeUpgradeService service = getService(request);
		Long _total = service.getRecordCount();// 该实体总记录数
		Integer total = _total.intValue();
		resultJson.put("total", total);

		List<SimNodeUpgrade> simNodeUpgradeList = service.getRecordList(pageNumber, pageSize);

		String currentUser = null;// 得到当前登录用户
		List<String> operatorNameList = new ArrayList<String>();// // 这里是所有操作管理员名单集合

		try {
			// 以下代码主要是处理，列表中，显示权限，即修改和删除的权限
			currentUser = sid.getUserName();
			List<AuthAccount> users = (List<AuthAccount>)userService.getAllUsers();

			for (AuthAccount authAccount : users) {
				operatorNameList.add(authAccount.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONArray updateFormJson = new JSONArray();
		for (SimNodeUpgrade simNodeUpgrade : simNodeUpgradeList) {

			JSONObject updateForm = new JSONObject();

			updateForm.put("id", simNodeUpgrade.getId().toString());
			updateForm.put("name", simNodeUpgrade.getName());
			updateForm.put("createDate", DateUtils.formatDatetime(simNodeUpgrade.getCreateDate()));
			updateForm.put("creater", simNodeUpgrade.getCreater());

			String description = simNodeUpgrade.getDescription();

			if (StringUtils.isNotBlank(description)) {
				while (description.indexOf("\r\n") != -1) {
					description = description.replace("\r\n", "&nbsp;&nbsp;&nbsp;");
				}
			} else {
				description = "&nbsp;";
			}

			updateForm.put("description", description);

			// 以下代码主要是处理，列表中，显示权限，即修改和删除的权限
			if (currentUser.equals(simNodeUpgrade.getCreater())) {// 如果当前登录用户是该记录的创建者的话，则有权限
				updateForm.put("update", true);
				updateForm.put("delete", true);
			} else {
				boolean isExist = false;
				if (operatorNameList != null && operatorNameList.size() > 0) {
					for (String name : operatorNameList) {
						if (name.equals(simNodeUpgrade.getCreater())) {// 数据库有有记录
							isExist = true;
							break;
						}
					}
				}
				if (isExist) {// "creater"在用户表中存在，则没权限
					updateForm.put("update", false);
					updateForm.put("delete", false);
				} else {// 没有(账户已被删除)，则有权限
					updateForm.put("update", true);
					updateForm.put("delete", true);
				}
			}

			updateFormJson.add(updateForm);
		}

		resultJson.put("rows", updateFormJson);
		return resultJson;
	}

	/**
	 * 节点升级上传文件
	 * 
	 *  @param theFile
	 *  @param session
	 *  @param request
	 *  @return
	 *  @throws Exception
	 */
	@RequestMapping(value="uploadNodeFile",produces="text/html;charset=utf-8")
	@ResponseBody
	public Object uploadNodeFile(@RequestParam("theFile") MultipartFile theFile,
			@RequestParam("description") String description,
			HttpSession session,
			HttpServletRequest request) throws Exception {

		String updateVesionConf = "patch-info.xml";// 内部版本号的配置文件名称
		String tempDir = System.getProperty("java.io.tmpdir") + "updateTempDir";// 临时备份文件目录(C:/Users/TAL/AppData/Local/Temp/)
		File tempFileDir = null;// 升级包存放的临时文件夹
		File tempfile = null;// 升级包文件
		SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
		JSONObject result = new JSONObject() ;
		try {
			SimNodeUpgradeService service = getService(request);
			if ((theFile.getSize()/1024/1024) > 500) {
				log.warn("节点升级上传的文件过大");
				AuditLogFacade.userOperation("节点升级",sid.getUserName(), "节点升级上传文件失败:文件过大",new IpAddress(sid.getLoginIP()),Severity.HIGH,AuditCategoryDefinition.SYS_UPGRADE,false);
				result.put("resultLoad", "tooLarge");
				return result ;
			}

			String fileName = theFile.getOriginalFilename();

			// 上传至FTP 
			Map<String, Object> args = FtpConfigUtil.getInstance().getFTPConfigByKey("patch");
			int port = Integer.parseInt((String) args.get("port"));
			String host = (String) args.get("host");
			String user = (String) args.get("user");
			String password = (String) args.get("password");
			String encoding = (String) args.get("encoding");
			FtpUploadUtil.uploadFile(host, port, user, password, encoding, ".", fileName, theFile.getInputStream());

			tempFileDir = new File(tempDir);
			if (!tempFileDir.exists()) {
				tempFileDir.mkdirs();
			}

			tempfile = new File(tempDir + "/" + fileName);
			FtpUploadUtil.downFile(host, port, user, password, encoding, ".", fileName, tempDir);// 下载到临时目录
			boolean usable = UpdateUtil.checkNodeFile(tempfile.getAbsolutePath(), fileName);// 是否可用
			if (!usable) {// 不可用，则要删除临时文件，且重新回到上传页面
				FtpUploadUtil.deleteFile(host, port, user, password, encoding, ".", fileName);
				log.warn("节点升级上传的文件不可用");
				AuditLogFacade.userOperation("节点升级",sid.getUserName(), "节点升级上传的文件不可用",new IpAddress(sid.getLoginIP()),Severity.HIGH,AuditCategoryDefinition.SYS_UPGRADE,false);
				result.put("resultLoad", "disable");
				return result ;
			}

			SimNodeUpgrade simNodeUpgradePo = new SimNodeUpgrade();
			simNodeUpgradePo.setCreateDate(new Date());
			if (description != null) {
				simNodeUpgradePo.setDescription(description.trim());
			}

			// 上传包的格式示例: "TAL_AGENT_3_0_029_002-3_0_029_008.sp"
			String _fileName = "TAL_" + fileName;
			String[] names = _fileName.split("_");
			simNodeUpgradePo.setFilePath(".");
			String nodeType = names[1];
			String versionFrom = names[2] + "_" + names[3] + "_" + names[4] + "_" + names[5].substring(0, 3);
			String versionTo = names[5].substring(4, names[5].length()) + "_" + names[6] + "_" + names[7] + "_" + names[8].substring(0, 3);

			simNodeUpgradePo.setName(fileName);
			simNodeUpgradePo.setNodeType(nodeType);
			simNodeUpgradePo.setVersionTo(versionTo);
			simNodeUpgradePo.setVersionFrom(versionFrom);
			String userName = sid.getUserName();// 系统登录的用户名
			simNodeUpgradePo.setCreater(userName);
			service.save(simNodeUpgradePo);
			AuditLogFacade.userOperation("保存(上传)升级对象",sid.getUserName(),  "保存(上传)升级对象名称: " + simNodeUpgradePo.getName(), new IpAddress(sid.getLoginIP()),Severity.LOW,AuditCategoryDefinition.SYS_UPGRADE,true);

		} catch (Exception e) {
			log.error("节点升级上传文件时抛出异常" + e.getMessage());
			AuditLogFacade.userOperation("节点升级",sid.getUserName(), "节点升级上传文件时抛出异常", new IpAddress(sid.getLoginIP()),Severity.HIGH,AuditCategoryDefinition.SYS_UPGRADE,false);
			result.put("resultLoad", "2");
			return result;
		} finally {// 主要是删除一些目录
			File versionFile = new File(tempDir + "\\" + updateVesionConf);
			if (versionFile.exists() && versionFile.isFile()) {
				versionFile.delete();
			}
			if (tempfile != null && tempfile.exists() && tempfile.isFile()) {
				tempfile.delete();
			}
			if (tempFileDir != null && tempFileDir.isDirectory() && tempFileDir.exists()) {
				tempFileDir.delete();
			}
		}
		AuditLogFacade.userOperation("节点升级",sid.getUserName(), "节点升级上传文件", new IpAddress(sid.getLoginIP()),Severity.LOW,AuditCategoryDefinition.SYS_UPGRADE,true);
		result.put("resultLoad", "1");
		return result.toString();
	}

	/**
	 * 保存节点升级计划
	 *  @param planType 区分 特定一次执行、周期性
	 *  @param disc 描述
	 *  @param session
	 *  @param request
	 *  @return
	 *  @throws Exception
	 */
	@RequestMapping("uploadNodePlanSave")
	@ResponseBody
	public void uploadNodePlanSave(SID sid,@RequestParam("planType") String planType,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		JSONObject result = new JSONObject() ;
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		SimNodeUpgradePlan simNodeUpgradePlanPo = new SimNodeUpgradePlan();
		simNodeUpgradePlanPo.setPlanType(planType);

		// 特定时间执行一次
		if ("spcOne".equals(planType)) {
			// 时间处理
			String exeTime = request.getParameter("exeTime");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = format.parse(exeTime);
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			Integer year = calendar.get(Calendar.YEAR);
			Integer month = calendar.get(Calendar.MONTH) + 1;
			Integer day = calendar.get(Calendar.DAY_OF_MONTH);
			Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
			Integer min = calendar.get(Calendar.MINUTE);
			Integer sec = calendar.get(Calendar.SECOND);

			// 赋值更新
			TimeExpression timeExpression = new TimeExpression();
			timeExpression.setType(TimeExpression.TYPE_USER_DEFINE);
			timeExpression.setUserDefine(year, month, day, hour, min, sec);

			UpdateMgrConfig.getInstance().updateConfigByName("timeExpression", timeExpression.getType()+":"+timeExpression.getExpression());
			UpdateMgrConfig.getInstance().updateConfigByName("planType", "spcOne");
			UpdateMgrConfig.getInstance().updateConfigByName("date", exeTime);
			simNodeUpgradePlanPo.setTimeExpression(timeExpression);

		}else if ("perTime".equals(planType)) { // 周期性
			UpdateMgrConfig.getInstance().updateConfigByName("planType", "perTime");
			UpdateMgrConfig.getInstance().updateConfigByName("date", null);
			TimeExpression timeExpression = CommonUtils.createTimeExpression(request);
			UpdateMgrConfig.getInstance().updateConfigByName("timeExpression", timeExpression.getType()+":"+timeExpression.getExpression());
			simNodeUpgradePlanPo.setTimeExpression(timeExpression);
		}else{
			result.put("resultLoad", "emptyFlag");
			response.getWriter().print(result);
			return;
		}
		// 处理下发
		NodeMgrFacade nodeMgrFacade = getNodeService(request);
		List<String> nodeTypes = new ArrayList<String>();// 下发节点的类型
		nodeTypes.add(NodeDefinition.NODE_TYPE_CHILD);
		nodeTypes.add(NodeDefinition.NODE_TYPE_AGENT);
		List<Node> nodes = nodeMgrFacade.getNodesByTypes(nodeTypes, true, false, true, true);

		TimeExpression timeExpression = simNodeUpgradePlanPo.getTimeExpression();
		for (Node node : nodes) {
			if(NodeUtil.isAgent(node.getType())){
				NodeConfigurationFormater formater = (NodeConfigurationFormater) NodeUtil.findNodeSegmentConfig(node, NodeConfigurationFormater.class) ;
				if (formater != null) {
					NodeConfigurationMetadata metaData = formater.getConfiguration();
					metaData.setUpgradeCycle(timeExpression.getExpression());
					formater.setConfiguration(metaData);
					nodeMgrFacade.updateNodeSegmentAndDispatch(node, formater);
				}
			}else if(NodeDefinition.NODE_TYPE_CHILD.equals(node.getType())){
				//下发webservice命令
				try{
					String url = "https://"+node.getIp()+"/resteasy/upgrade/updateNodeSegment?timeExpression="+timeExpression.getExpression();
					url = HttpUtil.cleanURL(url);
					Map<String,String> cookies = new HashMap<String,String>();
					cookies.put("sessionid",RestUtil.getSessionId(node.getIp()));
					HttpUtil.doPostWithSSLByString(url, null, cookies, "UTF-8");
				}catch(Exception e){//如果设置下载升级计划失败，继续其它节点
					e.printStackTrace();
				}
			}
		}
		AuditLogFacade.userOperation("升级计划",sid.getUserName(), "设置代理升级计划", new IpAddress(sid.getLoginIP()),Severity.LOWEST,AuditCategoryDefinition.SYS_ADD,true);
		result.put("resultLoad", "success");
		response.getWriter().print(result);
		return;
	}
	
	/**
	 * 服务器升级后，系统重新启动
	 *  @param session
	 *  @param request
	 *  @return
	 *  @throws Exception
	 */
	@RequestMapping("/restart")
	public String restart(HttpSession session, HttpServletRequest request) throws Exception {
		SID sid = (SID) session.getAttribute(SIMConstant.SESSION_SID) ;
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
					String[] route = NodeUtil.getRoute(node);
					NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_NODE_RESTART, Integer.valueOf(7000));
				}
			}

		} catch (Exception e) {
			log.error("服务器接收到重启命令，系统重新启动抛出异常" + e.getMessage());
			AuditLogFacade.userOperation("系统重新启动",sid.getUserName(), "服务器接收到重启命令，系统重新启动抛出异常" + e.getMessage(),new IpAddress(sid.getLoginIP()),Severity.HIGH,AuditCategoryDefinition.SYS_UPGRADE,false);
			e.printStackTrace();
		}
		AuditLogFacade.userOperation("系统重新启动",sid.getUserName(), "服务器接收到重启命令，系统重新启动。",new IpAddress(sid.getLoginIP()),Severity.HIGH,AuditCategoryDefinition.SYS_UPGRADE,true);
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
		return "redirect:/";
	}

	/**
	 * 删除节点升级包
	 *  @param sid
	 *  @param request
	 *  @return
	 */
	@RequestMapping("/deleteNodeUpgradeFile")
	@ResponseBody
	public Object deleteNodeUpgradeFile(SID sid, HttpServletRequest request) {
		Result result = new Result(false, "删除失败！");
		try {
			String id = request.getParameter("id");
			String fileName = request.getParameter("fileName");

			SimNodeUpgrade simNodeUpgrade = new SimNodeUpgrade();
			simNodeUpgrade.setId(Integer.parseInt(id));
			getService(request).delete(simNodeUpgrade);

			// 从FTP上删除
			Map<String, Object> args = FtpConfigUtil.getInstance().getFTPConfigByKey("patch");
			int port = Integer.parseInt((String) args.get("port"));
			String host = (String) args.get("host");
			String user = (String) args.get("user");
			String password = (String) args.get("password");
			String encoding = (String) args.get("encoding");
			FtpUploadUtil.deleteFile(host, port, user, password, encoding, ".", fileName);

			toLog(AuditCategoryDefinition.SYS_DELETE, "删除节点升级包", "删除升级包名称: " + simNodeUpgrade.getName(),sid.getUserName(),true,Severity.MEDIUM);

			result.setStatus(true);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return result;
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
	private void toLog(String cat, String name, String desc, String subject, boolean result, Severity severity) throws Exception {
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(cat);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(subject);
		_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(result);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}

	public NodeMgrFacade getNodeMgrFacade() {
		return nodeMgrFacade;
	}
    
	@Autowired
	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}
}
