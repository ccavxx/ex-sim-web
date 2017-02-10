package com.topsec.tsm.sim.report.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.ConfigFactory;
import com.topsec.tal.base.util.config.ConfigName;
import com.topsec.tal.base.util.config.ConfigType;
import com.topsec.tal.base.util.config.Item;
import com.topsec.tal.base.util.config.webitems.BrowsePathItem;
import com.topsec.tal.base.util.config.webitems.ButtonItem;
import com.topsec.tal.base.util.config.webitems.EditItem;
import com.topsec.tal.base.util.config.webitems.HtmlConfig;
import com.topsec.tal.base.util.config.webitems.InputItem;
import com.topsec.tal.base.util.config.webitems.ListInputItem;
import com.topsec.tal.base.util.config.webitems.PasswordItem;
import com.topsec.tal.base.util.config.webitems.SelectInTextAreaItem;
import com.topsec.tal.base.util.config.webitems.SelectItem;
import com.topsec.tal.base.util.config.webitems.SelectMultipleItem;
import com.topsec.tal.base.util.config.webitems.TimeItem;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.web.NotCheck;
import com.topsec.tsm.sim.event.service.SceneUserService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.bean.PlanTaskResult;
import com.topsec.tsm.sim.report.bean.ReportBean;
import com.topsec.tsm.sim.report.bean.StandardTree;
import com.topsec.tsm.sim.report.common.EventPolicySend;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.XmlStringAnalysis;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.EventPolicy;
import com.topsec.tsm.sim.response.persistence.EventPolicy_R_Response;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.ResponseResult;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.sim.sysconfig.web.EventRuleController;
import com.topsec.tsm.sim.sysman.bean.RespConfig;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.ResponseSend;
import com.topsec.tsm.sim.util.TSMConstant;
import com.topsec.tsm.tal.response.adjudicate.ResponseInMem;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.schedule.SystemSchedule;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.ui.util.tree.TreeNode;

/*******************************************************************************
 * 此类是告警设置所有请求的响应类。<br>
 * 
 * <BR>
 * <table border=1>
 * <tr align=center>
 * <td><b>类别</b></td>
 * </tr>
 * <tr align=left>
 * <td>◆Action◇Bean◇Interface◇Service◇Class</td>
 * </tr>
 * </table>
 * <br>
 * 
 * @author <a href="mailto:@topsec.com.cn"></a>
 * 
 *         <a href="http://www.topsec.com">天融信TSM</a><br>
 * @version Ver 3.2 2014-4-25<br>
 * @since H16
 ******************************************************************************/
@Controller("mgrPlanReport")
@RequestMapping("mgrPlanReport")
public class MgrPlanReportController{
	private static Logger log = LoggerFactory.getLogger(MgrPlanReportController.class) ;
	@Autowired
	private EventResponseService eventResponseService ;
	@Autowired
	private NodeMgrFacade nodeMgrFacade;
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private SceneUserService sceneUserService;
	@Autowired
	private UserService userService;
	@Autowired
	private ReportService reportService ;

	
	
	private void setEncode(HttpServletRequest request, HttpServletResponse response){
		try {
			if(null!=request){
				request.setCharacterEncoding("UTF-8");
			}
			if(null!=request){
				response.setCharacterEncoding("UTF-8");
			}
			
		} catch (UnsupportedEncodingException e) {
		}
		
	}
	
	/* 列表 */
	@SuppressWarnings({ "unchecked"})
	/* modify by wang_zhiai : 加入计划报表兼容代码 */
	@RequestMapping("getPlanBySubType")
	public void getPlanBySubType(SID sid,HttpServletRequest request, HttpServletResponse response) throws Exception {
		setEncode(request,response);
		String cfgType = request.getParameter("cfgType");
		String realCfgType = "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
		String subType = request.getParameter("subType");
		String scheduleType=request.getParameter("scheduleType");
		String newSubType = subType.substring(subType.lastIndexOf(".") + 1);

		int pageNum = 1;// 页码
		int pageSize = TSMConstant.PAGE_SIZE;// 每页显示的记录数
		// 当翻页时
		String _pageNum = request.getParameter("page");
		String _pageSize = request.getParameter("rows");

		if (StringUtils.isNotBlank(_pageNum) && StringUtils.isNotBlank(_pageSize)) {
			try {
				pageNum = Integer.parseInt(_pageNum);
				pageSize = Integer.parseInt(_pageSize);
			} catch (Exception e) {
				pageNum = 1;
				e.printStackTrace();
			}
		}
		Long _total = eventResponseService.getResponseRecordCount(realCfgType);// 该实体总记录数
		Integer total = _total.intValue();

		List<Response> responses = new ArrayList<Response>();
		String currentUser = sid.getUserName();// 得到当前登录用户
		if (StringUtils.isNotBlank(realCfgType)) {
			/*if (sid.isOperator()) {
				if (StringUtils.isNotBlank(scheduleType)) {
					total = reportService.showCountPlanByTypeAndExeTimeType(
							realCfgType, scheduleType);
					responses = reportService.showPlanByTypeAndExeTimeType(
							realCfgType, scheduleType, pageNum, pageSize);
				} else {
					responses = eventResponseService.getResponsesByType(
							realCfgType, pageNum, pageSize);
				}
			}else{*/
				if (StringUtils.isNotBlank(currentUser)) {
					if (StringUtils.isNotBlank(scheduleType)) {
						total = reportService
								.showCountPlanByTypeAndExeTimeTypeAndUser(
										realCfgType, scheduleType, currentUser);
						responses = reportService
								.showPlanByTypeAndExeTimeTypeAndUser(
										realCfgType, scheduleType, currentUser,
										pageNum, pageSize);
					}else{
						total = reportService
								.showCountPlanByTypeAndUser(
										realCfgType, currentUser);
						responses = reportService
								.showPlanByTypeAndUser(
										realCfgType, currentUser,
										pageNum, pageSize);
					}
				}
//			}
			
		}
		
		List<AuthAccount> usrList = userService.getUsersByRoleName(sid.getUserType());
		List<ConfigName> configKeyList = ConfigFactory.getConfigKeysbyType(realCfgType, newSubType);
		List<Config> configList = new ArrayList<Config>();
		Iterator<ConfigName> iter = configKeyList.iterator();
		Config cfg = null;
		while (iter.hasNext()) {
			cfg = ConfigFactory.getCfgTemplate(iter.next().getKey());
			configList.add(cfg);
		}
		
		List<ReportBean> responseList = new ArrayList<ReportBean>();
		for (Response response1 : responses) {
			ReportBean responseForm = new ReportBean();
			Response _response=reportService.showPlanTaskById(response1.getId());
			int successCount=reportService.showPlanResultSuccessCountByRespId(_response.getId());
			int failedCount=reportService.showPlanResultFailedCountByRespId(_response.getId());
			responseForm.setSuccessResultCount(successCount);
			responseForm.setFailedResultCount(failedCount);
			responseForm.setId(_response.getId());
			responseForm.setName(HtmlUtils.htmlEscape(_response.getName()));
			responseForm.setCreater(_response.getCreater());
			String configString=_response.getConfig();
			responseForm.setScheduleType(_response.getScheduleType());
			Map<String, String>map=XmlStringAnalysis.getMap(XmlStringAnalysis.stringDocument(configString));
			responseForm.setReportFileType(map.get("report_filetype"));
			responseForm.setReportMailList(map.get("report_maillist"));
			responseForm.setReportSys(map.get("report_sys"));
			responseForm.setReportTopn(map.get("report_topn"));
			responseForm.setReportType(map.get("report_type"));
			responseForm.setReportUser(map.get("report_user"));
			Node node=_response.getNode();
			if(!(null==node)&&null!=node.getNodeId()){
				responseForm.setOutNodeIdJsonString(node.getNodeId());
			}
			
			String configID = _response.getCfgKey();
			responseForm.setCfgKey(configID);
			cfg = ConfigFactory.getCfgTemplate(configID);
			String createTime ;
			if (_response.getCreateTime() != null) {
				createTime = StringUtil.dateToString(_response.getCreateTime(),"yyyy-MM-dd HH:mm:ss") ;
			}else{
				createTime = "" ;
			}
			responseForm.setCreateTime(createTime);
			Calendar calendar=Calendar.getInstance();
			
			String timeExp=_response.getScheduleExpression();
			String []tempexpr=timeExp.split(" ");
			String nextExeTime="";
			if ("EVERY_DAY".equals(_response.getScheduleType())) {
				if (calendar.get(Calendar.HOUR_OF_DAY)<Integer.valueOf(tempexpr[2])
						||(calendar.get(Calendar.HOUR_OF_DAY)==Integer.valueOf(tempexpr[2])
							&&calendar.get(Calendar.MINUTE)<=Integer.valueOf(tempexpr[1]))) {
					
					nextExeTime=tempexpr[2]+"时"+tempexpr[1]+"分";
					
				}else{
					nextExeTime=calendar.get(Calendar.DAY_OF_MONTH)+1+"日 "+tempexpr[2]+"时"+tempexpr[1]+"分";
				}
				
			}else if ("EVERY_WEEK".equals(_response.getScheduleType())) {
				if (calendar.get(Calendar.DAY_OF_WEEK)==Integer.valueOf(tempexpr[5])
						&&(calendar.get(Calendar.HOUR_OF_DAY)<Integer.valueOf(tempexpr[2])
								||(calendar.get(Calendar.HOUR_OF_DAY)==Integer.valueOf(tempexpr[2])
								&&calendar.get(Calendar.MINUTE)<=Integer.valueOf(tempexpr[1])))){
					
					nextExeTime=tempexpr[2]+"时"+tempexpr[1]+"分";
										
				}else{
					nextExeTime=intToWeek(Integer.valueOf(tempexpr[5]))+" "+tempexpr[2]+"时"+tempexpr[1]+"分";
				}
								
			}else if ("EVERY_MONTH".equals(_response.getScheduleType())) {
				if (calendar.get(Calendar.DAY_OF_MONTH)==Integer.valueOf(tempexpr[3])
						&&(calendar.get(Calendar.HOUR_OF_DAY)<Integer.valueOf(tempexpr[2])
								||(calendar.get(Calendar.HOUR_OF_DAY)==Integer.valueOf(tempexpr[2])
								&&calendar.get(Calendar.MINUTE)<=Integer.valueOf(tempexpr[1])))){
					
					nextExeTime=tempexpr[2]+"时"+tempexpr[1]+"分";
					
				}else{
					nextExeTime=calendar.get(Calendar.MONTH)+2+"月 "+tempexpr[3]+"日 "+tempexpr[2]+"时 "+tempexpr[1]+"分";
				}
				
			}else if ("EVERY_YEAR".equals(_response.getScheduleType())) {
				if (calendar.get(Calendar.MONTH)+1==Integer.valueOf(tempexpr[4])
						&&calendar.get(Calendar.DAY_OF_MONTH)<=Integer.valueOf(tempexpr[3])
						&&(calendar.get(Calendar.HOUR_OF_DAY)<Integer.valueOf(tempexpr[2])
								||(calendar.get(Calendar.HOUR_OF_DAY)==Integer.valueOf(tempexpr[2])
								&&calendar.get(Calendar.MINUTE)<=Integer.valueOf(tempexpr[1])))){
					
					nextExeTime=tempexpr[2]+"时"+tempexpr[1]+"分";
					
				}else{
					nextExeTime=tempexpr[4]+"月 "+tempexpr[3]+"日 "+tempexpr[2]+"时 "+tempexpr[1]+"分";
				}
				
			}
			responseForm.setNextExeTime(nextExeTime);
			String type = cfg.getName();
			responseForm.setType(type);
			responseForm.setStatus(String.valueOf(_response.isStart()));
			responseForm.setCfgType(cfgType);
			responseForm.setEditType(_response.getEditType());
			responseForm.setSubType(subType);

			if (currentUser.equals(_response.getCreater())) {// 如果当前登录用户是该“审计对象”的创建者的话，则有权限
				responseForm.setUpdate(true);
				responseForm.setDelete(true);
				responseForm.setStart(true);
				responseForm.setShowResult(true);
			} else {
				// "creater"在用户表中存在则没权限，没有则有(账户已被删除)权限

				Boolean isExist = false;
				for(AuthAccount auth:usrList){
					if (auth.getName().equals(_response.getCreater())) {// 该角色对应的用户和创建者相同
						isExist = true;
						break;
					}
				}
				if (isExist) {
					responseForm.setUpdate(false);
					responseForm.setDelete(false);
					responseForm.setStart(false);
					responseForm.setShowResult(false);
				} else {
					// 该角色对应的账户已被删除（数据库中没有此creater的记录）
					responseForm.setUpdate(true);
					responseForm.setDelete(true);
					responseForm.setStart(true);
					responseForm.setShowResult(true);
				}
			}
			responseList.add(responseForm);

		}
		JSONObject result = new JSONObject();
		result.put("total", total) ;
		Object json=JSON.toJSON(responseList);
		result.put("rows", json) ;
		PrintWriter writer=response.getWriter();
		writer.print(result);
		
	}

	private String intToWeek(int value){
		if(value%7==1){
			return "周日";
		}else if(value%7==2){
			return "周一";
		}else if(value%7==3){
			return "周二";
		}else if(value%7==4){
			return "周三";
		}else if(value%7==5){
			return "周四";
		}else if(value%7==6){
			return "周五";
		}else {
			return "周六";
		}
	}

	/**
	 * removeResponse 删除告警; 删除计划报表(每次删除一条记录)
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("removePlanReport")
	public void removePlanReport(SID sid, HttpServletRequest request, HttpServletResponse response) {
		String cfgType = request.getParameter("cfgType");
		String realCfgType = "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
		String respId = null;
		
		if (null!=sid&&sid.getUserName()!=null) {
			respId = request.getParameter("respId");
			Response resp = eventResponseService.getResponse(respId);
			request.setAttribute("resp", resp);
			if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
				try {
					Config config = RespCfgHelper.getConfig(resp);
					send(config, resp, "delete");// 下发

				} catch (I18NException e) {
					e.printStackTrace();
				}

				Set<EventPolicy_R_Response> set = resp.getEventPolicies();// 该响应所关联的告警对象
				if (set != null && set.size() > 0) {
					Iterator<EventPolicy_R_Response> iterator = set.iterator();
					EventPolicy_R_Response eprr = null;
					while (iterator.hasNext()) {
						eprr = iterator.next();
						EventPolicy _eventPolicyPo = eprr.getEventPolicy();// 删除的响应对象所关联的告警对象
						EventPolicy eventPolicyPo = eventResponseService.getEventPolicy(_eventPolicyPo.getId());
						Set<EventPolicy_R_Response> responses = eventPolicyPo.getResponses();
						Set<EventPolicy_R_Response> responseList = new HashSet<EventPolicy_R_Response>();

						for (EventPolicy_R_Response eventPolicyRResponse : responses) {
							Response _response = eventPolicyRResponse.getResponse();// 告警对象关联到的响应对象
							Response _resp = eventResponseService.getResponse(_response.getId());
							if (!_resp.getId().equals(resp.getId())) {// 剔除本次删除的响应对象
								EventPolicy_R_Response _eprr = new EventPolicy_R_Response();
								_eprr.setResponse(_resp);
								responseList.add(_eprr);
							}
						}
						eventPolicyPo.setResponses(responseList);

						EventPolicySend.getInstance().send(eventPolicyPo, nodeMgrFacade, "modify", true, true);// 下发
					}
				}
			} else if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
				ResponseInMem.getInstance().delResponse(resp);
			}

			eventResponseService.delResponse(respId);
			if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
				toLog(sid,AuditCategoryDefinition.SYS_DELETE, "删除响应对象", "删除响应对象名称: " + resp.getName(), Severity.MEDIUM);
			}

			if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
				toLog(sid,AuditCategoryDefinition.SYS_DELETE, "删除计划报表", "删除计划报表名称:" + resp.getName(), Severity.MEDIUM);
			}

		} 
		String subType = request.getParameter("subType");
		String newSubType = subType.substring(((String) request.getParameter("subType")).lastIndexOf(".") + 1);
		
		List<ConfigName> configKeyList = ConfigFactory.getConfigKeysbyType(realCfgType, newSubType);
		List<Config> configList = new ArrayList<Config>();
		Iterator<ConfigName> iter = configKeyList.iterator();
		Config cfg = null;
		while (iter.hasNext()) {
			cfg = ConfigFactory.getCfgTemplate(iter.next().getKey());
			configList.add(cfg);
		}
		PrintWriter printWriter;
		try {
			printWriter = response.getWriter();
			printWriter.print("success");
		} catch (IOException e) {		}
		
	}
	
	/**
	 * removeResponse 删除告警; 删除计划报表(批量删除)
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("removeBatchPlanReport")
	@ResponseBody
	@NotCheck(properties={"subType","respCfgType"},allows={"exectimes","execcmd","execinterval"})
	public Object removeBatchPlanReport(SID sid, @RequestParam("respIds") String respIds,
			@RequestParam("subType") String subType, @RequestParam("cfgType") String cfgType,
			HttpServletRequest request) {

		JSONObject result = new JSONObject();
		result.put("status", false) ;
		if(StringUtil.isBlank(respIds) || StringUtil.isBlank(subType) || StringUtil.isBlank(cfgType)){
			return result;
		}
		boolean isAllDeleteSuccess=true;
		
		if (sid != null && sid.getUserName() != null) {
			
			String realCfgType = "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
			// String newSubType = subType.substring((subType).lastIndexOf(".") + 1);
			String[] respIdArray = respIds.split(",");
			for(int i=0; i<respIdArray.length; i++){
				
				String respId = respIdArray[i];
				if(respId != null){
					Response resp = eventResponseService.getResponse(respId);
					
					if(resp != null){
						try {
							removeBatchSubMethod(realCfgType, resp, sid, respId);
							send(RespCfgHelper.getConfig(resp), resp, "delete") ;
						} catch (I18NException e) {
							isAllDeleteSuccess=false;
							e.printStackTrace();
						}
					}
				}
			}
			if (isAllDeleteSuccess) {
				result.put("status",true);
			}
		}
		return result;
	}
	/**
	 * 批量删除辅助方法
	 * @param realCfgType
	 * @param resp
	 * @param sid
	 * @param respId
	 * @throws I18NException 
	 */
	public void removeBatchSubMethod(String realCfgType, Response resp, SID sid, String respId) throws I18NException{
		
		if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
			try {
				Config config = RespCfgHelper.getConfig(resp);
				send(config, resp, "delete");// 下发
				
			} catch (I18NException e) {
				throw e;
			}
			
			Set<EventPolicy_R_Response> set = resp.getEventPolicies();// 该响应所关联的告警对象
			if (set != null && set.size() > 0) {
				Iterator<EventPolicy_R_Response> iterator = set.iterator();
				EventPolicy_R_Response eprr = null;
				while (iterator.hasNext()) {
					
					eprr = iterator.next();
					EventPolicy _eventPolicyPo = eprr.getEventPolicy();// 删除的响应对象所关联的告警对象
					EventPolicy eventPolicyPo = eventResponseService.getEventPolicy(_eventPolicyPo.getId());
					Set<EventPolicy_R_Response> responses = eventPolicyPo.getResponses();
					Set<EventPolicy_R_Response> responseList = new HashSet<EventPolicy_R_Response>();
					
					for (EventPolicy_R_Response eventPolicyRResponse : responses) {
					
						Response _response = eventPolicyRResponse.getResponse();// 告警对象关联到的响应对象
						Response _resp = eventResponseService.getResponse(_response.getId());
						if (!_resp.getId().equals(resp.getId())) {// 剔除本次删除的响应对象
						
							EventPolicy_R_Response _eprr = new EventPolicy_R_Response();
							_eprr.setResponse(_resp);
							responseList.add(_eprr);
						}
					}
					eventPolicyPo.setResponses(responseList);
					
					EventPolicySend.getInstance().send(eventPolicyPo, nodeMgrFacade, "modify", true, true);// 下发
				}
			}
		} else if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
			ResponseInMem.getInstance().delResponse(resp); 
		}
		
		eventResponseService.delResponse(respId);
		if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
			toLog(sid, AuditCategoryDefinition.SYS_DELETE, "删除响应对象", "删除响应对象名称: " + resp.getName(), Severity.MEDIUM);
		}
		
		if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
			toLog(sid, AuditCategoryDefinition.SYS_DELETE, "删除计划报表", "删除计划报表名称:" + resp.getName(), Severity.MEDIUM);
		}
	}
	@RequestMapping("delPlanResultsByRespId")
	public void delPlanResultsByRespId(@RequestParam("respId")String respId,HttpServletResponse response){
		if (null==respId||respId.trim().length()<2) {
			return;
		}
		Response planTask=new Response(respId);
		
		List<ResponseResult>planTaskResultList=eventResponseService.getResponseResultsByResponse(planTask);
		reportService.delPlanResults(planTaskResultList);
		PrintWriter printWriter;
		setEncode(null,response);
		try {
			printWriter = response.getWriter();
			printWriter.print("success");
		} catch (IOException e) {		}
	}
	
	@RequestMapping("showPlanTaskTreeByPeriod")
	public void showPlanTaskTreeByPeriod(SID sid,HttpServletRequest request, HttpServletResponse response){
		setEncode(request,response);
		String createrString=sid.getUserName();
		StandardTree[] treeResultArr=new StandardTree[1];
		StandardTree treeResult=new StandardTree();
		treeResult.setId("0");
		treeResult.setText("执行周期");
		treeResult.setState("open");
		String[]scheTypes={"EVERY_DAY","EVERY_WEEK","EVERY_MONTH","EVERY_YEAR"};
		for (int i=0;i<scheTypes.length;i++) {//String scheduleType : scheTypes
			List<Response> planTaskList = null;
			/*if (sid.isOperator()) {
				planTaskList=reportService.showAllResponses(scheTypes[i]);
			} else {*/
				planTaskList=reportService.showAllResponsesByCreater(createrString,scheTypes[i]);
//			}
			StandardTree standardTree=new StandardTree();
			standardTree.setParentId("0");
			standardTree.setId(scheTypes[i]);
			if (i==0) {
				standardTree.setText("每天执行");
			}else if(i==1) {
				standardTree.setText("每周执行");
			}else if(i==2) {
				standardTree.setText("每月执行");
			}else if(i==3) {
				standardTree.setText("每年执行");
			}
			if(ObjectUtils.isEmpty(planTaskList)){
				standardTree.setState("open") ;
			}else{
				for (Response plantask : planTaskList) {
					StandardTree standardTreels=new StandardTree();
					standardTreels.setParentId(scheTypes[i]);
					standardTreels.setId(plantask.getId());
					standardTreels.setText(plantask.getName());
					standardTreels.setState("open") ;
					standardTree.addChild(standardTreels);
				}
//				standardTree.setState("closed") ;
			}
			treeResult.addChild(standardTree);
		}
		
		PrintWriter printWriter=null;
		try {
			treeResultArr[0]=treeResult;
			printWriter=response.getWriter();
			Object json=JSON.toJSON(treeResultArr);
			printWriter.print(json);
		} catch (IOException e) {
			
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("showPlanReport")
	public void showPlanReport(SID sid,@RequestParam("respId")String respId,
			HttpServletRequest request, HttpServletResponse response) {
		setEncode(request, response);
		String cfgType = request.getParameter("cfgType");
		String subType = request.getParameter("subType");
		
		String currentUser = sid.getUserName();// 得到当前登录用户
		
		List<AuthAccount> usrList = userService.getUsersByRoleName(sid.getUserType());

		Response planTask = reportService.showPlanTaskById(respId);
		if (null==planTask) {
			return;
		}
		ReportBean responseForm = new ReportBean();
		responseForm.setId(respId);
		responseForm.setName(planTask.getName());
		responseForm.setCreater(planTask.getCreater());
		Node node=planTask.getNode();
		if(!(null==node)&&null!=node.getNodeId()){
			responseForm.setOutNodeIdJsonString(node.getNodeId());
		}
		responseForm.setDesc(planTask.getDesc());
		responseForm.setConfig(planTask.getConfig());
		responseForm.setScheduleExpression(planTask.getScheduleExpression());
		responseForm.setScheduleType(planTask.getScheduleType());
		String configID = planTask.getCfgKey();
		Config cfg = ConfigFactory.getCfgTemplate(configID);
		
		String createTime= null;
		try {
			DateFormat dfDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(null!=planTask.getCreateTime()){}
			createTime=dfDateFormat.format(planTask.getCreateTime());
			responseForm.setCreateTime(createTime);
		} catch (Exception e1) {
		}
		String type = cfg.getName();
		responseForm.setType(type);
		responseForm.setStatus(String.valueOf(planTask.isStart()));
		responseForm.setCfgType(cfgType);
		responseForm.setEditType(planTask.getEditType());
		responseForm.setSubType(subType);
		int successCount=reportService.showPlanResultSuccessCountByRespId(respId);
		int failedCount=reportService.showPlanResultFailedCountByRespId(respId);
		responseForm.setSuccessResultCount(successCount);
		responseForm.setFailedResultCount(failedCount);
		String configString=planTask.getConfig();
		Map<String, String>map=XmlStringAnalysis.getMap(XmlStringAnalysis.stringDocument(configString));
		responseForm.setReportFileType(map.get("report_filetype"));
		responseForm.setReportMailList(map.get("report_maillist"));
		responseForm.setReportSys(map.get("report_sys"));
		responseForm.setReportTopn(map.get("report_topn"));
		responseForm.setReportType(map.get("report_type"));
		responseForm.setReportUser(map.get("report_user"));

		if (currentUser.equals(planTask.getCreater())) {// 如果当前登录用户是该“审计对象”的创建者的话，则有权限
			responseForm.setUpdate(true);
			responseForm.setDelete(true);
			responseForm.setStart(true);
			responseForm.setShowResult(true);
		} else {
			// "creater"在用户表中存在则没权限，没有则有(账户已被删除)权限

			Boolean isExist = false;
			for(AuthAccount auth:usrList){
				if (auth.getName().equals(planTask.getCreater())) {// 该角色对应的用户和创建者相同
					isExist = true;
					break;
				}
			}
			if (isExist) {
				responseForm.setUpdate(false);
				responseForm.setDelete(false);
				responseForm.setStart(false);
				responseForm.setShowResult(false);
			} else {
				// 该角色对应的账户已被删除（数据库中没有此creater的记录）
				responseForm.setUpdate(true);
				responseForm.setDelete(true);
				responseForm.setStart(true);
				responseForm.setShowResult(true);
			}
		}

		PrintWriter printWriter=null;
		try {
			printWriter=response.getWriter();
			Object json=JSON.toJSON(responseForm);
			printWriter.print(json);
		} catch (IOException e) {
			
		}
	}

	/* 编辑响应; 修改计划报表 */
	@RequestMapping("editPlanReport")
	public void editPlanReport(SID sid,@ModelAttribute("planTask")Response planTask,
			@RequestParam("reportinfo")String reportinfo,HttpServletRequest request, HttpServletResponse response) throws I18NException {

		String cfgType = request.getParameter("cfgType");
		String realCfgType = "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
		String respId =null;

		String nodeId =null;
		try {
			respId=request.getParameter("respId");
			nodeId=request.getParameter("nodeId");
		} catch (Exception e) {
			
		}
		
		if (null==respId) {
			return;
		}
		Response resp = reportService.showPlanTaskById(respId);
		if (StringUtils.isNotBlank(planTask.getName())||planTask.getName().trim().length()<2) {
			resp.setName(planTask.getName());
		}
		if (StringUtils.isNotBlank(planTask.getDesc())) {
			resp.setDesc(planTask.getDesc());
		}
		if (StringUtils.isNotBlank(planTask.getScheduleType())) {
			resp.setScheduleType(planTask.getScheduleType());
		}
		if (StringUtils.isNotBlank(planTask.getCfgKey())) {
			resp.setCfgKey(planTask.getCfgKey());
		}
		Config config = RespCfgHelper.getConfig(resp);
//		config.setKey(planTask.getCfgKey());
		try {
			config = requestFormConfig(config, request);
		} catch (Exception e1) {
			return;
		}
		RespCfgHelper.setConfig(resp, config);
		if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
			
			if (null!=reportinfo&&StringUtils.isNotBlank(reportinfo)) {
				String reportintmpString=reportinfo.trim();
				if(null!=reportintmpString&&reportintmpString.length()>2){
					String nodeid = reportinfo.split("=:")[0];
					Node node = new Node();
					node.setNodeId(nodeid);// 报表nodeid关联
					resp.setNode(node);
				}
			}else if(nodeId!=null){
				Node node = new Node();
				node.setNodeId(nodeId);// 报表nodeid关联
				resp.setNode(node);
			}

			TimeExpression timeExpression = new TimeExpression();
			if (TimeExpression.TYPE_USER_DEFINE.equals(request.getParameter("intervalType"))) {
				// timeExpression.setUserDefine(request.getParameter("expression").toString());
			} else {
				if (resp.getScheduleType().equals(TimeExpression.TYPE_EVERY_YEAR)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_YEAR);
						int month=getRequestInteger(request, "month");
						int date=getRequestInteger(request, "date");
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkDate(month, date)||checkHour(hour)||checkMin(min))){
							return;
						}
						timeExpression.setEveryYear(month, date, hour, min, 0);
					} catch (Exception e) {
						return;
					}

				} else if (resp.getScheduleType().equals(TimeExpression.TYPE_EVERY_MONTH)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_MONTH);
						int date=getRequestInteger(request, "date");
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkDate(2, date)||checkHour(hour)||checkMin(min))) {
							return;
						}
						timeExpression.setEveryMonth(date, hour, min, 0);
					} catch (Exception e) {
						return;
					}

				} else if (resp.getScheduleType().equals(TimeExpression.TYPE_EVERY_WEEK)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_WEEK);
						int day=getRequestInteger(request, "day");
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkHour(hour)||checkMin(min))) {
							return;
						}
						timeExpression.setEveryWeek(day, hour,min, 0);
					} catch (Exception e) {
						return;
					}

				} else if (resp.getScheduleType().equals(TimeExpression.TYPE_EVERY_DAY)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_DAY);
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkHour(hour)||checkMin(min))) {
							return;
						}
						timeExpression.setEveryDay(hour, min,0);
					} catch (Exception e) {
						return;
					}

				}
			}
			resp.setTimeExpression(timeExpression);
		}
		eventResponseService.updateResponse(resp);
		if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
			toLog(sid,AuditCategoryDefinition.SYS_UPDATE, "更新响应对象", "更新响应对象名称: " + resp.getName(), Severity.LOW);
		}

		if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
			toLog(sid,AuditCategoryDefinition.SYS_UPDATE, "修改计划报表", "修改计划报表名称:" + resp.getName(), Severity.LOW);
			ResponseInMem.getInstance().updateResponse(resp);
		} else {
			if (!realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
				// 添加自审计日志
				toLog(sid,AuditCategoryDefinition.SYS_UPDATE, request.getParameter("response.name"), request.getParameter("response.desc"), Severity.LOW);
			}
			send(config, resp, "modify");// 响应下发
		}
		PrintWriter printWriter;
		try {
			printWriter = response.getWriter();
			printWriter.print("success");
		} catch (IOException e) {
			System.out.println(e);
		}
		
	}

	/** 
	 * 保存响应页面 ; 跳转到计划报表添加页 
	 */
	@RequestMapping("getAllConfig")
	public String getAllConfig(SID sid,HttpServletRequest request, HttpServletResponse response) {

		String cfgType = request.getParameter("cfgType");
		String realCfgType = "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
		String subType = request.getParameter("subType");
		String newSubType = subType.substring(((String) request.getParameter("subType")).lastIndexOf(".") + 1);

		List<ConfigName> configKeyList = ConfigFactory.getConfigKeysbyType(realCfgType, newSubType);// 命令告警配置这里newSubType=exec , 也可能是计划报表

		List<Config> configList = new ArrayList<Config>();
		Iterator<ConfigName> iter = configKeyList.iterator();
		Config cfg = null;
		while (iter.hasNext()) {
			cfg = ConfigFactory.getCfgTemplate(iter.next().getKey());
			configList.add(cfg);
		}

		if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
			List<ConfigName> msg_List = ConfigFactory.getConfigKeysbyType(realCfgType, "msg");// 消息通知配置
			Iterator<ConfigName> msg_iterator = msg_List.iterator();
			while (msg_iterator.hasNext()) {
				cfg = ConfigFactory.getCfgTemplate(msg_iterator.next().getKey());
				configList.add(cfg);
			}
		}

		Response resp = new Response();
		request.setAttribute("resp", resp);
		request.setAttribute("userId", sid.getAccountID());

		request.setAttribute("configList", configList);
		request.setAttribute("cfgType", cfgType);
		request.setAttribute("subType", subType);
		
		//如果是从‘规则引擎’中的‘事件规则配置’中的‘关联通知方式’点击“新建”会传过一个sceneCreateResponse的参数判断是从什么地反点击的新建
		//页面处理流程会有不同，详见createResponse.jsp中JSTL的注释说明
		request.setAttribute("sceneCreateResponse", request.getParameter("sceneCreateResponse"));
		
		return "/page/report/createPlanReport";

	}

	/* "响应方式列表" 下拉列表框onchange触发函数 ; 计划报表类型下拉框获取配置 */
	@RequestMapping("changeConfig")
	public void changeConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String configId = request.getParameter("configId");//resp_cfg_wavashinelert
		if (StringUtils.isNotBlank(configId) || "0".equals(configId)) {
			Config cfg = ConfigFactory.getCfgTemplate(configId);
			request.setAttribute("cfg", cfg);
			response.setContentType("text/html;charset=utf-8");
			PrintWriter writer;
			writer = response.getWriter();
			String info = ((HtmlConfig) cfg).toHtml(request) + "<input type='hidden' name='configId'  value='" + configId + "'></input>";
			writer.print(info);
			request.setAttribute("configId", configId);
		}
		
	}

	 /* 保存响应 ; 保存计划报表 */
	@RequestMapping("createPlanReport")
	public void createPlanReport(SID sid,
			@ModelAttribute("planTask")Response planTask,HttpServletRequest request,
			@RequestParam("reportinfo")String reportinfo,HttpServletResponse response) throws Exception {
		
		String cfgType = null;
		String realCfgType =null;
		String subType =null;
		String configId=null;
		Config config =null;
		try {
			cfgType=request.getParameter("cfgType");
			realCfgType= "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
			subType = request.getParameter("subType");
			configId = request.getParameter("configId");
		} catch (Exception e) {
			return;
		}
		try {
			if(null==planTask||null==planTask.getName().trim()||planTask.getName().trim().length()<2
					||null==cfgType||null==cfgType.trim()||cfgType.trim().length()<1
					||null==subType||null==subType.trim()||subType.trim().length()<1
					||null==configId||null==configId.trim()||configId.trim().length()<1
					||null==realCfgType){
				return;
			}
		} catch (Exception e) {
			return;
		}
		config = ConfigFactory.getCfgTemplate(configId);
		try {
			config = requestFormConfig(config, request);
		} catch (Exception e) {
			return;
		}
		RespCfgHelper.setConfig(planTask, config);
		planTask.setCreater(sid.getUserName());
		planTask.setCreateTime(new Date());

		if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {

			if (StringUtils.isNotBlank(reportinfo)) {
				String nodeid = reportinfo.split("=:")[0];
				Node node = new Node();
				node.setNodeId(nodeid);// 报表nodeid关联
				planTask.setNode(node);
			}

			TimeExpression timeExpression = new TimeExpression();
			if (TimeExpression.TYPE_USER_DEFINE.equals(request.getParameter("intervalType"))) {
				toCheckExpression(request.getParameter("expression").toString());
				// timeExpression.setUserDefine(request.getParameter("expression").toString());
			} else {
				if (request.getParameter("scheduleType").equals(TimeExpression.TYPE_EVERY_YEAR)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_YEAR);
						int month=getRequestInteger(request, "month");
						int date=getRequestInteger(request, "date");
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkDate(month, date)||checkHour(hour)||checkMin(min))){
							return;
						}
						timeExpression.setEveryYear(month, date, hour, min, 0);
					} catch (Exception e) {
						return;
					}

				} else if (request.getParameter("scheduleType").equals(TimeExpression.TYPE_EVERY_MONTH)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_MONTH);
						int date=getRequestInteger(request, "date");
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkDate(2, date)||checkHour(hour)||checkMin(min))) {
							return;
						}
						timeExpression.setEveryMonth(date, hour, min, 0);
					} catch (Exception e) {
						return;
					}

				} else if (request.getParameter("scheduleType").equals(TimeExpression.TYPE_EVERY_WEEK)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_WEEK);
						int day=getRequestInteger(request, "day");
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkHour(hour)||checkMin(min))) {
							return;
						}
						timeExpression.setEveryWeek(day, hour,min, 0);
					} catch (Exception e) {
						return;
					}

				} else if (request.getParameter("scheduleType").equals(TimeExpression.TYPE_EVERY_DAY)) {
					try {
						timeExpression.setType(TimeExpression.TYPE_EVERY_DAY);
						int hour=getRequestInteger(request, "hour");
						int min=getRequestInteger(request, "min");
						if (!(checkHour(hour)||checkMin(min))) {
							return;
						}
						timeExpression.setEveryDay(hour, min,0);
					} catch (Exception e) {
						return;
					}

				}

			}
			planTask.setTimeExpression(timeExpression);
		}
		request.setAttribute("cfgType", cfgType);
		request.setAttribute("subType", subType);
		request.setAttribute("planTask", planTask);

		if (!realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
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

		eventResponseService.addResponse(planTask);
		if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
			toLog(sid,AuditCategoryDefinition.SYS_ADD, "添加响应对象", "添加响应对象名称: " + planTask.getName(), Severity.LOWEST);
		}

		if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
			toLog(sid,AuditCategoryDefinition.SYS_ADD, "添加计划报表", "添加计划报表名称:" + planTask.getName(), Severity.LOWEST);

			ResponseInMem.getInstance().addResponse(planTask);
		} else {
			Config _config = RespCfgHelper.getConfig(planTask);
			send(_config, planTask, "save");// 下发
		}
		PrintWriter printWriter=response.getWriter();
		printWriter.print("success");
	}
	private Integer getRequestInteger(HttpServletRequest request,String string){
		String temString=null;
		try {
			temString=request.getParameter(string).toString();
		} catch (Exception e) {
		}
		if (null==temString||temString.trim().length()<1) {
			temString="1";
		}
		Integer result=0;
		try {
			result=Integer.valueOf(temString);
		} catch (Exception e) {
			result=1;
		}
		return result;
	} 
	
	private boolean checkHour(int hour){
		if (hour<0||hour>23) {
			return false;
		}
		return true;
	}
	
	private boolean checkMin(int min){
		if (min<0||min>59) {
			return false;
		}
		return true;
	}
	
	private boolean checkDate(int month,int date){
		if (month<1||month>12) {
			return false;
		}
		if (month==2) {
			if (date<1||date>29) {
				return false;
			}
			return true;
		}else if (month==1||month==3||month==5||month==7||month==8||month==10||month==12) {
			if (date<1||date>31) {
				return false;
			}
			return true;
		}else {
			if (date<1||date>30) {
				return false;
			}
		}
		return true;
	}
	/**
	 *  响应; 计划报表的启用、停用
	 *  @param sid
	 *  @param request
	 *  @param response
	 */
	@RequestMapping("changeStates")
	public void changeStates(SID sid,HttpServletRequest request, HttpServletResponse response) {
		String respId = request.getParameter("respId");
		String status = request.getParameter("status");
		if (null==status||status.trim().length()<2) {
			return;
		}
		String cfgType = request.getParameter("cfgType");
		String realCfgType = "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
		eventResponseService.setResponseState(respId, Boolean.valueOf(status));

		if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
			String action = null;
			String name = null;
			String desc = null;
			String respName = request.getParameter("respName");
			if (Boolean.valueOf(status)) {
				action = AuditCategoryDefinition.SYS_START;
				name = "启用响应对象";
				desc = "启用响应对象名称: " + StringUtil.recode(respName);
			} else {
				action = AuditCategoryDefinition.SYS_STOP;
				name = "禁用响应对象";
				desc = "禁用响应对象名称: " + StringUtil.recode(respName);
			}
			toLog(sid,action, name, desc, Severity.LOW);

		}

		Response resp = eventResponseService.getResponse(respId);
		if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
			AuditRecord _log = AuditLogFacade.createConfigAuditLog();
			_log.setSubject(sid.getUserName());
			_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
			_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
			_log.setSuccess(true);
			_log.setSeverity(Severity.LOW);
			if (Boolean.valueOf(status)) {
				_log.setBehavior(AuditCategoryDefinition.SYS_START);
				_log.setSecurityObjectName("启用计划报表");
				_log.setDescription("启用计划报表名称:" + resp.getName());
			} else {
				_log.setBehavior(AuditCategoryDefinition.SYS_STOP);
				_log.setSecurityObjectName("禁用计划报表");
				_log.setDescription("禁用计划报表名称:" + resp.getName());
			}

			AuditLogFacade.send(_log);

			ResponseInMem.getInstance().updateResponse(resp);
		} else {
			try {
				Config config = RespCfgHelper.getConfig(resp);
				send(config, resp, "modify");// 下发

			} catch (I18NException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 *  响应; 计划报表的启用、停用()
	 *  @param sid
	 *  @param request
	 *  @param response
	 */
	@RequestMapping("changeBatchStates")
	@ResponseBody
	@NotCheck(properties="subType")
	public Result changeBatchStates(SID sid, @RequestParam("respIds") String respIds,
			@RequestParam("status") String status, @RequestParam("subType") String subType,
			@RequestParam("cfgType") String cfgType, HttpServletRequest request) {

		Result result = new Result(false, "操作失败！");
		
		if(StringUtil.isBlank(respIds) || StringUtil.isBlank(subType)  || StringUtil.isBlank(cfgType) || StringUtil.isBlank(status)){
			return result;
		}
		
		String[] respIdArray = respIds.split(",");
		String realCfgType = "action.type." + cfgType.substring(cfgType.indexOf(".") + 1);
		
		for(int i=0; i < respIdArray.length; i++) {
			
			String respId = respIdArray[i];
			if(respId != null){
				changeBatchStateSub(sid, respId, status, realCfgType);
			}
			if(i == (respIdArray.length - 1)){
				result = new Result(true, "操作成功！");
			}
		}
		return result;
	}

	public void changeBatchStateSub(SID sid, String respId, String status, String realCfgType){
		eventResponseService.setResponseState(respId, Boolean.valueOf(status));
		
		Response resp = eventResponseService.getResponse(respId);
		
		if (realCfgType.equals(ConfigType.TYPE_RESPONSE)) {
			String action = null;
			String name = null;
			String desc = null;
			if (Boolean.valueOf(status)) {
				action = AuditCategoryDefinition.SYS_START;
				name = "启用响应方式";
				desc = "启用响应方式名称: " + resp.getName();
			} else {
				action = AuditCategoryDefinition.SYS_STOP;
				name = "禁用响应方式";
				desc = "禁用响应方式: " + resp.getName();
			}
			toLog(sid,action, name, desc, Severity.LOW);
		}
		
		if (realCfgType.equals(ConfigType.TYPE_SCHEDULE)) {
			AuditRecord _log = AuditLogFacade.createConfigAuditLog();
			_log.setSubject(sid.getUserName());
			_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
			_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
			_log.setSuccess(true);
			_log.setSeverity(Severity.LOW);
			if (Boolean.valueOf(status)) {
				_log.setBehavior(AuditCategoryDefinition.SYS_START);
				_log.setSecurityObjectName("启用计划报表");
				_log.setDescription("启用计划报表名称:" + resp.getName());
			} else {
				_log.setBehavior(AuditCategoryDefinition.SYS_STOP);
				_log.setSecurityObjectName("禁用计划报表");
				_log.setDescription("禁用计划报表名称:" + resp.getName());
			}

			AuditLogFacade.send(_log);
			ResponseInMem.getInstance().updateResponse(resp);
		} else {
			try {
				Config config = RespCfgHelper.getConfig(resp);
				send(config, resp, "modify");// 下发

			} catch (I18NException e) {
				e.printStackTrace();
			}
		}
	}
	/* 响应结果列表 */
	@RequestMapping("showPlanTaskResult")
	public void showPlanTaskResult(HttpServletRequest request, HttpServletResponse response) {
		setEncode(request, response);
		String cfgType = request.getParameter("cfgType");
		String respId = request.getParameter("respId");
		int pageNum = 1;// 页码
		int pageSize = TSMConstant.PAGE_SIZE;// 每页显示的记录数
		// int pageSize = 1;// 每页显示的记录数
		// 当翻页时
		String _pageNum = request.getParameter("page");
		String _pageSize = request.getParameter("rows");

		if (StringUtils.isNotBlank(_pageNum) && StringUtils.isNotBlank(_pageSize)) {

			pageNum = Integer.parseInt(_pageNum);
			pageSize = Integer.parseInt(_pageSize);
		}

		Response _response = new Response();
		_response.setId(respId);
		Long _total = eventResponseService.getResponseResultCountByResonse(_response);
		Integer total = _total.intValue();

		List<ResponseResult> responseResultList = eventResponseService.getResponseResultsByResponse(_response, pageNum, pageSize);
		List<PlanTaskResult> planTaskResults=toJsonDataCompatibilityDate(responseResultList);
		request.setAttribute("responseResultList", responseResultList);
		request.setAttribute("respId", respId);
		request.setAttribute("cfgType", cfgType);
		
		
		JSONObject result = new JSONObject();
		result.put("total", total) ;
		Object json=JSON.toJSON(planTaskResults);
		result.put("rows", json) ;
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(result);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	private List<PlanTaskResult> toJsonDataCompatibilityDate(List<ResponseResult> responseResultList){
		List<PlanTaskResult> planTaskResults=new ArrayList<PlanTaskResult>();
		DateFormat dateFormat=new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
		for (ResponseResult respResult : responseResultList) {
			PlanTaskResult planTaskResult=new PlanTaskResult();
			planTaskResult.setId(respResult.getId());
			planTaskResult.setName(respResult.getName());
			planTaskResult.setEventDesc(respResult.getEventDesc());
			String respTimeString=dateFormat.format(respResult.getRespTime());
			planTaskResult.setRespTime(respTimeString);
			planTaskResult.setResult(respResult.getResult());
			planTaskResult.setResultDesc(respResult.getResultDesc());
			planTaskResult.setSubType(respResult.getSubType());
			planTaskResult.setType(respResult.getType());
			planTaskResult.setUseTime(respResult.getUseTime());
			planTaskResults.add(planTaskResult);
		}
		return planTaskResults;
	}

	/** 
	 *删除响应结果 
	 **/
	@RequestMapping("removePlanTaskResult")
	public void removePlanTaskResult(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("respResultIds")String respResultIds) {
		try {
			setEncode(request, response);
			if (StringUtils.isNotBlank(respResultIds)) {
				String[] ids = respResultIds.split("ADDdelID");
				List<String> respResultIdList = new ArrayList<String>();
				if (ids.length > 0) {
					for (String id : ids) {
						respResultIdList.add(id);
					}
					eventResponseService.delResponseResults(respResultIdList);
				}
			}
			PrintWriter printWriter=response.getWriter();
			printWriter.print("success");
		} catch (IOException e) {
			e.printStackTrace() ;
		}
	}

	private Config requestFormConfig(Config config, HttpServletRequest request) throws I18NException {
		List<Block> blocks = config.getCfgBlocks();
		String selectBlock = null;
		List<String> blockKeyList = null;
		Block aSelectBlock = null;
		for (Block block : blocks) {

			if (block.getGroup() != null) {
				blockKeyList = config.getGroupBlockKeys(block.getGroup());
				if ((request.getParameter(block.getGroup()) != null) && (!request.getParameter(block.getGroup()).equals(""))) {
					selectBlock = request.getParameter(block.getGroup());
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
					//key=report_user
					//刘根祥report_user
					newValue = request.getParameter("reportUser");
					if (newValue == null) {
						newValue = "";
					} else {
						newValue = newValue.replaceAll("'", "~dyh~");
					}
					newValue=HtmlUtils.htmlEscape(newValue);
					item.setValue(newValue);
				}
				if (item instanceof BrowsePathItem) {
					newValue = request.getParameter(config.getKey() + "." + block.getKey() + "." + item.getKey());
					item.setValue(newValue);
				}
				if (item instanceof EditItem) {
					newValue = request.getParameter(config.getKey() + "." + block.getKey() + "." + item.getKey());
					item.setValue(newValue);
				}
				if (item instanceof ListInputItem) {
					//key=report_maillist
					//selectListInput= liu_gengxiang@topsec.com.cn
					// liu_gengxiang@topsec.com.cn feng_lingming@topsec.com.cn
					String selectListInput = request.getParameter("report_maillist");
					selectListInput=selectListInput.trim();
					if (selectListInput.indexOf("@") <1 || selectListInput.lastIndexOf("@") == selectListInput.length()-1) {
						throw new RuntimeException("邮件格式不正确"); 
					}
					List<String> newValueList = new ArrayList<String>();
					if ((selectListInput != null) && (selectListInput.length() != 0)) {
						String[] selectedFiels = selectListInput.split("EMAILadd");
						for (int i = 0; i < selectedFiels.length; i++) {
							if (selectedFiels[i].indexOf("@") < 1 ||selectedFiels[i].lastIndexOf("@")==selectListInput.length()-1) {
								continue; 
							}
							newValueList.add(selectedFiels[i]);
						}
						item.setValueList(newValueList);
					}
				}
				if (item instanceof ButtonItem) {//key=report_sys
					//newValue =Esm/Topsec/SimEvent;;127.0.0.1;;165=:事件分类排行
					try {
						newValue = (String) request.getParameter("reportinfo");//直接引用
						if(null!=newValue&&newValue.length()>2){
							item.setValue(newValue);
						}
						
					} catch (Exception e) {
					}
				}
				//key=report_topn
				//5=:Top5;;10=:Top10;;15=:Top15;;20=:Top20;;25=:Top25
				//newValue =5
				
				//key=report_filetype
				//doc=:Word文件;;pdf=:PDF文件;;excel=:Excel文件
				//newvalue=doc
				if (item instanceof SelectItem) {
					//key=report_type
					//lable.report.typeyearReportType=:年报表;;lable.report.quarterReportType=:季报表;;lable.report.typemonthReportType=:月报表;;lable.report.weekReportType=:周报表;;lable.report.typedayReportType=:日报表
					//newValue = lable.report.typeyearReportType
					if("report_type".equals(item.getKey())){
						newValue = request.getParameter("reportType");
					}else if ("report_topn".equals(item.getKey())) {
						newValue = request.getParameter("reportTopn");
					}else if ("report_filetype".equals(item.getKey())) {
						newValue = request.getParameter("reportFileType");
					}
					
					item.setValue(newValue);
				}
				if (item instanceof PasswordItem) {
					newValue = (String) request.getParameter(config.getKey() + "." + block.getKey() + "." + item.getKey());
					item.setValue(newValue);
				}
				if (item instanceof SelectInTextAreaItem) {
					newValue = (String) request.getParameter(config.getKey() + "." + block.getKey() + "." + item.getKey());
					item.setValue(newValue);
				}
				if (item instanceof TimeItem) {
					newValue = (String) request.getParameter(config.getKey() + "." + block.getKey() + "." + item.getKey());
					item.setValue(newValue);
				}
				if (item instanceof SelectMultipleItem) {
					String[] selectListInput = request.getParameterValues(config.getKey() + "." + block.getKey() + "." + item.getKey());
					List<String> newValueList = new ArrayList<String>();
					if ((selectListInput != null) && (selectListInput.length != 0)) {
						for (int i = 0; i < selectListInput.length; i++) {
							newValueList.add(selectListInput[i]);
						}
						item.setValueList(newValueList);
					}
				}
			}
		}
		return config;
	}
	
	//
	private boolean toCheckExpression(String expression) {

		try {
			SystemSchedule.checkExpression(expression);
			return true;
		} catch (com.topsec.tsm.framework.exceptions.I18NException e) {
			return false;

		}
	}

	// /--------yangming-------------------------------------------
	String rootPath = "";

	// 日志存储用
	@SuppressWarnings("unchecked")
	public void getPathTreeNodeForLocal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String nodeId = request.getParameter("nodeId");
		String dir = request.getParameter("dir");
		Node auditorNode = nodeMgrFacade.getNodeByNodeId(nodeId);
		String folderId = request.getParameter("folderId");
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		dir = new String(dir.getBytes("ISO8859-1"), "utf-8");
		List<String> dirList = new ArrayList<String>();
		if (auditorNode != null) {
			String[] route = null;
			route = NodeUtil.getRoute(auditorNode);
			// 获取查询结果
			try {
				// result为返回的子目录集合
				Serializable result = NodeUtil.getCommandDispatcher().dispatchCommand(route, MessageDefinition.CMD_NODE_QUERY_DISK, dir, 60 * 1000);
				dirList = (ArrayList<String>) result;
			} catch (CommunicationException e) {
				log.error(e.getMessage());
				
			}
		}
		TreeNode[] nodes = null;
		if (dirList != null && dirList.size() != 0) {
			nodes = new TreeNode[dirList.size()];
			int i = 0;
			for (String dec : dirList) {
				TreeNode tn = new TreeNode();
				tn.setNodeId(UUID.randomUUID().toString());
				tn.setNodeText(dec);
				tn.setFolder(true);
				tn.setUrl(dec);
				nodes[i++] = tn;

			}
		} else {
			nodes = new TreeNode[0];
		}

		String nodesStr = JSONSerializer.toJSON(nodes).toString();
		StringBuffer temp = new StringBuffer();
		temp.append("{\"folderId\":").append("\"").append(folderId).append("\"").append(",").append("\"nodes\":").append(nodesStr).append("}");
		out.print(temp.toString());

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
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(action);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(sid.getUserName());
		_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(true);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
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
		}else if("resp_cfg_umsgate".equals(config.getKey())){//一信通响应下发
			ResponseSend.getInstance().sendUMSGate(config, nodeMgrFacade, resp, type);
		}
	}
	
	@RequestMapping("testResp")
	@ResponseBody
	@NotCheck(properties={"subType","respCfgType"},allows={"exectimes","execcmd","execinterval"})
	public void testResp(@ModelAttribute RespConfig  respcfg,SID sid,HttpServletRequest request, HttpServletResponse response) throws I18NException{
		try {
			Response resp=new Response();
			resp.setCreateTime(new Date());
			resp.setName(respcfg.getRespName());
			resp.setDesc(respcfg.getRespDesc());
			resp.setCfgKey(respcfg.getRespCfgType());
			String creater = sid.getUserName();
			resp.setCreater(GlobalUtil.isNullOrEmpty(creater)?" ":creater);
			Config config=ConfigFactory.getCfgTemplate(respcfg.getRespCfgType());
			config.setKey(respcfg.getRespCfgType());
			config.resetDefaultBlock() ;
			List<Map<String, Object>> mCfgItems = respcfg.getCfgItems();
			EventRuleController.handleCfgBlocks(config, mCfgItems);
			RespCfgHelper.setConfig(resp, config);
			Node node = nodeMgrFacade.getNodeByNodeId(request.getParameter("nodeId"),false,false,false,false);
			if(node == null){
				return ;
			}
			resp.setNode(node) ;
			// 执行服务命令测试响应下发
			if ("resp_cfg_execcmd".equals(config.getKey())) {
				ResponseSend.getInstance().sendTestExeccmd(request, node, config, resp);
			}
			// 声音测试响应 下发
			else if ("resp_cfg_wavalert".equals(config.getKey())) {
				ResponseSend.getInstance().sendTestWavalert(request, node, config, resp);
			}
			// Snmp Trap 测试响应下发
			else if ("resp_cfg_snmptrap".equals(config.getKey())) {
				ResponseSend.getInstance().sendTestSnmpTrap(request, node, config, resp);
			}
			// 邮件测试响应下发
			else if ("resp_cfg_mail".equals(config.getKey())) {
				ResponseSend.getInstance().sendTestToMail(request, node, config, resp);
			}
			// TopAnalyzer联动响应下发
			else if ("rep_cfg_integer".equals(config.getKey())) {
				ResponseSend.getInstance().sendTestInteger(request, node, config, resp);
			}
			// 短信响应下发
			else if ("resp_cfg_phonemsg".equals(config.getKey())) {
				ResponseSend.getInstance().sendTestPhonemsg(request, node, config, resp);
			}else if ("resp_cfg_wavashinelert".equals(config.getKey())) {
				ResponseSend.getInstance().sendTestWavaShinelert(request, node, config, resp);
			}else if("resp_cfg_umsgate".equals(config.getKey())){//一信通响应下发
				ResponseSend.getInstance().sendTestUMSGate(request, node, config, resp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping("isBlankEmailServerIP")
	@ResponseBody
	public Result isBlankEmailServerIP(){
		Result result = new Result();
		result.build(false);
		try{
			EventResponseService eventResponseService = (EventResponseService) SpringContextServlet.springCtx.getBean("eventResponseService") ;
			List<Response> responses=eventResponseService.getResponsesbyCfgKey("sys_cfg_mailserver");
			Response response2=responses.get(0);
			Config mailServerConfig = RespCfgHelper.getConfig(response2);
			Block mailsvrBlock = mailServerConfig.getDefaultBlock();
			String serverIP = mailsvrBlock.getItemValue("serverip");
			if(StringUtil.isBlank(serverIP)){
				result.buildSuccess(true);
			}
		}catch (Exception e) {
		}
		return result;
	}

}
