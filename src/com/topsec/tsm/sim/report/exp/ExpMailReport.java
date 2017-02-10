//
// COPYRIGHT (C) 2009 TOPSEC CORPORATION
//
// ALL RIGHTS RESERVED BY TOPSEC CORPORATION, THIS PROGRAM
// MUST BE USED SOLELY FOR THE PURPOSE FOR WHICH IT WAS
// FURNISHED BY TOPSEC CORPORATION, NO PART OF THIS PROGRAM
// MAY BE REPRODUCED OR DISCLOSED TO OTHERS, IN ANY FORM
// WITHOUT THE PRIOR WRITTEN PERMISSION OF TOPSEC CORPORATION.
// USE OF COPYRIGHT NOTICE DOES NOT EVIDENCE PUBLICATION
// OF THE PROGRAM
//
//            TOPSEC CONFIDENTIAL AND PROPROETARY
//
////////////////////////////////////////////////////////////////////////////
package com.topsec.tsm.sim.report.exp;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.IAction;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Ipv4Address;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.jasper.SchemeReport;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.ResponseResult;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.response.respimp.mail.MailHelper;
import com.topsec.tsm.tal.response.respimp.mail.MailSenderInfo;
import com.topsec.tsm.tal.service.EventResponseService;


public class ExpMailReport implements IAction {
	private Logger _log = Logger.getLogger(ExpMailReport.class);
	
	public RptMasterTbService rptMasterTbImp;
	
	private EventResponseService eventResponseService;
	
	private static Config mailServerConfig;

	public void response(Config actCfg) throws Exception {
		// key same
		// mail affix
		ExpStruct exp = getMstInfo(actCfg); 
		SchemeReport sReport =null;
		List result=null;
		
		if (! "schedule_cfg_logstatistics".equals(actCfg.getKey())) {
			//自定义报表被删除后不能再发送邮件	
			RptMasterTbService rptMasterTbImp2 = (RptMasterTbService) SpringContextServlet.springCtx
					.getBean(ReportUiConfig.MstBean);
			String sql = "from RptMaster where (delflg is null or delflg=0) and id = '"
					+ exp.getMstrptid() + "'";
			result = rptMasterTbImp2.queryList(sql, new Object[0]);
			if (result == null || result.size() == 0) {
				_log.warn("报表（" + exp.getMstrptid() + "）已经被删除！ ");
				throw new Exception("报表（" + exp.getMstrptid() + "）已经被删除！ ");
			}
			RptMaster report = (RptMaster) result.get(0);
			if (report.getMstType() == 2) {//自定义报表
				if("docx".equalsIgnoreCase(exp.getFileType())){
					exp.setFileType("doc");
				}
				sReport = new CustomReportExport(report);
			} else {
				sReport = new SchemeReport();
				sReport.setRptMasterTbImp(rptMasterTbImp);
			}
			exp.setRptName(report.getMstName());
		}else {
			Integer taskId=Integer.valueOf(exp.getReportTaskId());
			LogReportTaskService logReportTaskService=(LogReportTaskService) SpringContextServlet.springCtx.getBean("logReportTaskService");
			ReportTask reportTask=logReportTaskService.getTask(taskId);
			if (reportTask == null) {
				_log.warn("日志统计模板（" + taskId + "）已经被删除！ ");
				throw new Exception("日志统计模板（" + taskId + "）已经被删除！ ");
			}
			if("docx".equalsIgnoreCase(exp.getFileType())){
				exp.setFileType("doc");
			}
			sReport=new LogStatisticsReportExport(reportTask);
			result=new ArrayList(1);
			result.add(reportTask);
		}
		//生成报表,并返回报表压缩文件的路径
		String mailReportAffix = sReport.saveMstReport(exp);
		Block receiveBlock = actCfg.getBlockbyKey("reportinfo");
		List<String> re = receiveBlock.getItemValueList("report_maillist");
		//循环给每一个收件人发送邮件，并记录其状态
		AuditRecord log = null;
		String desc = "";
		if (StringUtils.isBlank(mailReportAffix)) {
			_log.error("报表生成失败！ ");
			for (int i = 0; i < re.size(); i++) {
				ResponseResult responseResult = RespCfgHelper.newResponseResult(actCfg);
				try{
					log = actCfg.getAction().log(actCfg, new Exception("计划报表执行失败!"));
					if(log != null){
						desc = re.get(i)+" " +log.getDescription();
						log.setDescription(desc);
						log.setSuccess(false);
					}				
				}catch(Exception ex){
					_log.error("task log fail,"+ex.getMessage());
				}
				RespCfgHelper.setResponseResult(responseResult, "false", desc);
			}
			if(log != null){
				AuditLogFacade.send(log);
			}
			return;
		}
		
		//得到报表文件, 并作为附件文件发生邮件
		List<String> mailAffix = new ArrayList<String>();
		mailAffix.add(mailReportAffix);

		_log.debug("SendMail: net send ");
		
		if(mailServerConfig==null){
			List<Response> responses=eventResponseService.getResponsesbyCfgKey("sys_cfg_mailserver");
			if(responses!=null&&responses.size()>0){
				Response response=responses.get(0);
				if (GlobalUtil.isNullOrEmpty(exp.getRptUser())) {
					exp.setRptUser(response.getCreater()==null?" ":response.getCreater());
				}
				mailServerConfig = RespCfgHelper.getConfig(response);
			}
		}

		Block mailsvrBlock = mailServerConfig.getDefaultBlock();
		String serverip = mailsvrBlock.getItemValue("serverip");
		String serverport = mailsvrBlock.getItemValue("serverport");
		String mailsender = mailsvrBlock.getItemValue("mailsender");
		String needauth = "true";// wanke
		String loginaccount = mailsvrBlock.getItemValue("loginaccount");
		String loginpwd = mailsvrBlock.getItemValue("loginpwd");
		boolean ssl = StringUtil.booleanVal(mailsvrBlock.getItemValue("ssl")) ;
		
		MailSenderInfo info = null;
		try{
			info = new MailSenderInfo(serverip, serverport, mailsender, needauth, loginaccount, loginpwd,ssl);
		}catch(Exception ex){
			_log.error("task log fail:"+ex.getMessage());
			ResponseResult ResponseResult = RespCfgHelper.newResponseResult(actCfg);
			RespCfgHelper.setResponseResult(ResponseResult, "false", "邮件服务器不存在!");
		}
			
		for (int i = 0; i < re.size(); i++) {
			ResponseResult responseResult = RespCfgHelper.newResponseResult(actCfg);
			try{
				String title=mailReportAffix.substring(mailReportAffix.lastIndexOf('/')+1,mailReportAffix.length());
				MailHelper.mailAlert(re.get(i), title, "以下是天融信生成报表，请查看附件。", mailAffix, info);
				log = log(actCfg, null);
				if(log != null){
					log.setSuccess(true);
					desc = re.get(i)+" " +(String) log.getDescription();
				}
				
				RespCfgHelper.setResponseResult(responseResult, "true", desc);
			}catch(Exception e){
				
				try{
					log = actCfg.getAction().log(actCfg, e);
				if(log != null){
						desc = re.get(i)+" " +log.getDescription();
						log.setDescription(desc);
						log.setSuccess(false);
					}				
				}catch(Exception ex){
					_log.error("task log fail,"+ex.getMessage());
				}
				RespCfgHelper.setResponseResult(responseResult, "false", desc);
			}
			
			if(log != null){
				AuditLogFacade.send(log);
			}
			if(result != null){
				try{
					eventResponseService.addResponseResult(responseResult);
				}catch(Exception e){
					_log.error("add response result fail,"+e.getMessage());
				}
			}
		}
		
	}

	private ExpStruct getMstInfo(Config actCfg) {

		Block rptInfoBlock = actCfg.getBlockbyKey("reportinfo");
		// top
		String top = rptInfoBlock.getItemValue("report_topn");
		// ;; =:

		String mstInfo = rptInfoBlock.getItemValue("report_sys");
		mstInfo = mstInfo.substring(0, mstInfo.indexOf("=:"));
		String[] mstDvc = mstInfo.split(";;");

		// 年报表;;quarter=:季报表;;month=:月报表;;week=:周报表;;day=:日报表
		// dvc
		String dvc = null;
		// dvc ip
		String rptIp = null;
		// mstId
		String mstId = null;
		//reportTaskId
		String reportTaskId=null;
		if (actCfg.getKey().equals("schedule_cfg_sysreport")) {
			dvc = mstDvc[0];
			rptIp = mstDvc[1];
			mstId = mstDvc[2];
		} else if (actCfg.getKey().equals("schedule_cfg_logstatistics")) {
			reportTaskId=mstInfo;
		}else {
			mstId = mstInfo;
		}

		String type = rptInfoBlock.getItemValue("report_type");
		// strarttime endtime
		String[] rptDate = ReportUiUtil.getExpTime(type);
		type = ReportUiUtil.rptTypeByEgType(type);//
		// 制作人
		String rptUser = GlobalUtil.isNullOrEmpty(rptInfoBlock.getItemValue("report_user"))?"":rptInfoBlock.getItemValue("report_user");
		ExpStruct exp = new ExpStruct();
		String fileType = rptInfoBlock.getItemValue("report_filetype");
		// 开始时间
		exp.setRptTimeS(rptDate[0]);
		// 结束时间
		exp.setRptTimeE(rptDate[1]);
		// 报表类型
		exp.setRptType(type);
		// 文件类型1 =pdf
		exp.setFileType(fileType);
		// 主报表ID
		exp.setMstrptid(mstId);
		// top
		exp.setTop(top);
		// dvc
		exp.setDvc(dvc);
		// 报表设备ip
		exp.setRptIp(rptIp);
		// 报表类型
		exp.setRptUser(rptUser);
		if (null !=reportTaskId) {
			exp.setReportTaskId(reportTaskId);
		}
		return exp;

	}

	public AuditRecord log(Config actCfg, Exception e) {
		String actionName = "ExpMailReport." + actCfg.getKey();
		AuditRecord log = AuditLogFacade.createSystemAuditLog();

		Block mailsvrBlock = actCfg.getBlockbyKey("mailserver");
		String serverip = mailsvrBlock.getItemValue("serverip");
		
		if(serverip==null){
			if(mailServerConfig==null){
				List<Response> responses=eventResponseService.getResponsesbyCfgKey("sys_cfg_mailserver");
				if(responses!=null&&responses.size()>0){
					Response response=responses.get(0);
					try {
						mailServerConfig = RespCfgHelper.getConfig(response);
					} catch (I18NException e1) {
						e1.printStackTrace();
					}
				}
			}
			mailsvrBlock = mailServerConfig.getDefaultBlock();
			serverip = mailsvrBlock.getItemValue("serverip");
		}
		// 邮件内容
		Block receiveBlock = actCfg.getBlockbyKey("reportinfo");
		log.setBehavior(actionName);
		log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		if(Ipv4Address.validIPv4(serverip)){
			log.setObjectAddress(new IpAddress(serverip));
		}
		log.setObject(StringUtil.join(receiveBlock.getItemValueList("report_maillist"),";"));
		log.setSecurityObjectName("计划报表");
		log.setDescription(e == null ? "计划报表执行成功" : "计划报表执行失败");
		log.setSuccess(true);
		log.setSeverity(e== null ? Severity.LOWEST : Severity.HIGH);
		return log;
	}

	public RptMasterTbService getRptMasterTbImp() {
		return rptMasterTbImp;
	}

	public void setRptMasterTbImp(RptMasterTbService rptMasterTbImp) {
		this.rptMasterTbImp = rptMasterTbImp;
	}

	public EventResponseService getEventResponseService() {
		return eventResponseService;
	}

	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}

	public static Config getMailServerConfig() {
		return mailServerConfig;
	}

	public static void setMailServerConfig(Config mailServerConfig) {
		ExpMailReport.mailServerConfig = mailServerConfig;
	}
}
