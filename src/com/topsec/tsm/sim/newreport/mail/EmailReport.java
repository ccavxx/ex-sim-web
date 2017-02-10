package com.topsec.tsm.sim.newreport.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.report.poi.SimXWPFDocument;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.util.config.IAction;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Ipv4Address;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.framework.exceptions.I18NException;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;
import com.topsec.tsm.sim.newreport.model.ReportQuery;
import com.topsec.tsm.sim.newreport.util.ExportDocumentUtil;
import com.topsec.tsm.sim.newreport.util.QueryUtil;
import com.topsec.tsm.sim.newreport.util.ResourceContainer;
import com.topsec.tsm.sim.report.util.HtmlAndFileUtil;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.response.persistence.ResponseResult;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.response.respimp.mail.MailHelper;
import com.topsec.tsm.tal.response.respimp.mail.MailSenderInfo;
import com.topsec.tsm.tal.service.EventResponseService;

/**
 * @ClassName: EmailReport
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年12月14日上午9:58:49
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class EmailReport implements IAction {
	private Logger logger=Logger.getLogger(EmailReport.class);
	private ReportQuery reportQuery;
	private EventResponseService eventResponseService;
	private static Config mailServerConfig;
	
	public EventResponseService getEventResponseService() {
		return eventResponseService;
	}
	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}

	public ReportQuery getReportQuery() {
		return reportQuery;
	}
	public void setReportQuery(ReportQuery reportQuery) {
		this.reportQuery = reportQuery;
	}
	
	public static Config getMailServerConfig() {
		return mailServerConfig;
	}
	public static void setMailServerConfig(Config mailServerConfig) {
		EmailReport.mailServerConfig = mailServerConfig;
	}

	public EmailReport() {
	}

	@Override
	public AuditRecord log(Config config, Exception exception) {
		String actionName = "ExpMailReport." + config.getKey();
		AuditRecord auditRecord = AuditLogFacade.createSystemAuditLog();

		Block mailsvrBlock = config.getBlockbyKey("mailserver");
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
		Block receiveBlock = config.getBlockbyKey("reportconfig");
		
		auditRecord.setBehavior(actionName);
		auditRecord.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		if(Ipv4Address.validIPv4(serverip)){
			auditRecord.setObjectAddress(new IpAddress(serverip));
		}		
		auditRecord.setObject(StringUtil.join(receiveBlock.getItemValueList("report_maillist"),";"));
		
		auditRecord.setSecurityObjectName((exception == null)?"计划报表执行成功":"计划报表执行失败");
		auditRecord.setDescription((exception == null)?"计划报表邮件已执行成功 。":"计划报表邮件执行失败 。");
		auditRecord.setSuccess((exception == null)?true:false);
		auditRecord.setSeverity((exception == null)?Severity.LOWEST:Severity.HIGH);
		
		return auditRecord;
	}

	@Override
	public void response(Config config) throws Exception {
		logger.info("------计划报表开始执行！--------");
		if (null == config) {
			return;
		}
		try {
			ReportQueryConditions conditions=configToConditions(config);
			List<Map>maps=reportQuery.findResultPutInDataStructureDescByConditions(conditions);
			String headline=ExportDocumentUtil.headlineByConditions(conditions,reportQuery);
			String reportDesc=ExportDocumentUtil.reportDescByConditions(conditions,reportQuery);
			
			Map<String, Object>exportStructMap=new HashMap<String, Object>();
			exportStructMap.put(QueryUtil.EXECUTE_TIME, QueryUtil.nowTime(QueryUtil.TIME_FORMAT));
			exportStructMap.put(QueryUtil.START_TIME, conditions.getStime());
			exportStructMap.put(QueryUtil.END_TIME, conditions.getEndtime());
			exportStructMap.put(QueryUtil.AUTHOR, conditions.getUsername());
			exportStructMap.put(QueryUtil.EXPORT_CATEGORY, QueryUtil.timeQuantum(conditions.getStime(), conditions.getEndtime()));
			exportStructMap.put(QueryUtil.EXPORT_HEADLINE, headline);
			exportStructMap.put(QueryUtil.REPORT_DESC, reportDesc);
			exportStructMap.put(QueryUtil.SECURITY_OBJECT_TYPE, QueryUtil.getDeviceTypeName(conditions.getSecurityObjectType(), new Locale("")));
			exportStructMap.put(QueryUtil.DVC_ADDRESS, QueryUtil.getQueryDvcAddresses(conditions));
			exportStructMap.put(QueryUtil.REPORT_SUMMARY, ExportDocumentUtil.getReportSummary(maps, exportStructMap,conditions.getExportFormat()));
			
			String title=QueryUtil.getDeviceTypeName(conditions.getSecurityObjectType(), new Locale(""))+headline;
			String filePath=QueryUtil.fileStartPath();
			String timeTitle=StringUtil.currentDateToString("yyyyMMddHHmmss");
			String fileSuffix=QueryUtil.getFileSuffix(conditions.getExportFormat());
			HtmlAndFileUtil.createPath(filePath+"/tmp");
			String outMailpath=filePath+"tmp/"+title+timeTitle+fileSuffix;
			OutputStream out =null;
			out=new FileOutputStream(outMailpath);
			
			if ("pdf".equalsIgnoreCase(conditions.getExportFormat())) {
				ExportDocumentUtil.pdf(exportStructMap,maps,out);
			} else if ("rtf".equalsIgnoreCase(conditions.getExportFormat()) 
					||"doc".equalsIgnoreCase(conditions.getExportFormat())
					||"docx".equalsIgnoreCase(conditions.getExportFormat())) {
				SimXWPFDocument doc=ExportDocumentUtil.doc(exportStructMap,maps);
				doc.write(out);
			} else if ("excel".equalsIgnoreCase(conditions.getExportFormat())
					||"xls".equalsIgnoreCase(conditions.getExportFormat())
					||"xlsx".equalsIgnoreCase(conditions.getExportFormat())) {
				XSSFWorkbook xlsx=ExportDocumentUtil.xlsx(exportStructMap, maps);
				xlsx.write(out);
			} else if("html".equalsIgnoreCase(conditions.getExportFormat())){
				String htmlTemplate=ResourceContainer.htmlTemplate;
				exportStructMap.put(QueryUtil.RESULT_DATA_AND_STRUCTURE, JSONObject.toJSONString(maps));
				htmlTemplate=ExportDocumentUtil.setHtml(htmlTemplate,exportStructMap);
				
				String resourceJsPath=filePath+"exportjs/";
				filePath=filePath+"htmlExp/";
				String exportJsPath=filePath+"exphtml/"+"html/"+"js/";
				
				String htmlName= timeTitle+ ".htm";
				String zipFileName=title+timeTitle+fileSuffix;
				HtmlAndFileUtil.createPath(filePath);
				HtmlAndFileUtil.clearPath(filePath);
				HtmlAndFileUtil.createPath(filePath+"exphtml/"+"html/");
				HtmlAndFileUtil.createPath(exportJsPath);
				HtmlAndFileUtil.copyfromPathToPath(resourceJsPath, exportJsPath);
				HtmlAndFileUtil.writeContent(HtmlAndFileUtil.createFile(filePath+"exphtml/"+"html/"+htmlName),htmlTemplate);

				HtmlAndFileUtil.compressFloderChangeToZip(filePath+"exphtml/", filePath, zipFileName);
				outMailpath=filePath+zipFileName;
				HtmlAndFileUtil.outzipFile(outMailpath, out);
			}
			
			if(mailServerConfig==null){
				List<Response> responses=eventResponseService.getResponsesbyCfgKey("sys_cfg_mailserver");
				if(responses!=null&&responses.size()>0){
					Response response=responses.get(0);
					mailServerConfig = RespCfgHelper.getConfig(response);
				}
			}
			// 保存导出文件开始
			Block receiveBlock = config.getBlockbyKey("reportconfig");
			String eDirectory = receiveBlock.getItemValue("report_save_path");
			String savePath = QueryUtil.getSavePath(eDirectory);
			//总是保存最近生成的报表
			HtmlAndFileUtil.clearPath(savePath);
			HtmlAndFileUtil.createPath(savePath);
			try {
				FileUtils.copyFileToDirectory(new File(outMailpath), new File(savePath));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// 结束
			Block mailsvrBlock = mailServerConfig.getDefaultBlock();
			String serverip = mailsvrBlock.getItemValue("serverip");
			String serverport = mailsvrBlock.getItemValue("serverport");
			String mailsender = mailsvrBlock.getItemValue("mailsender");
			String needauth = "true";
			String loginaccount = mailsvrBlock.getItemValue("loginaccount");
			String loginpwd = mailsvrBlock.getItemValue("loginpwd");

			List<String> authEmails = receiveBlock.getItemValueList("report_maillist");
			boolean ssl = StringUtil.booleanVal(mailsvrBlock.getItemValue("ssl")) ;
			MailSenderInfo info = null;
			try{
				info = new MailSenderInfo(serverip, serverport, mailsender, needauth, loginaccount, loginpwd,ssl);
			}catch(Exception ex){
				logger.error("task log fail:"+ex.getMessage());
				ResponseResult responseResult = RespCfgHelper.newResponseResult(config);
				RespCfgHelper.setResponseResult(responseResult, "false", "邮件服务器不存在!");
			}
			
			List<String> mailAffix = new ArrayList<String>();
			mailAffix.add(outMailpath);
			//循环给每一个收件人发送邮件，并记录其状态
			AuditRecord log = null;
			String desc = "";
			for (int i = 0; i < authEmails.size(); i++) {
				ResponseResult responseResult = RespCfgHelper.newResponseResult(config);
				try{
					MailHelper.mailAlert(authEmails.get(i), title, "以下是天融信生成报表，请查看附件。", mailAffix, info);
					log = log(config, null);
					if(log != null){
						log.setSuccess(true);
						desc = authEmails.get(i)+" " +(String) log.getDescription();
					}
					RespCfgHelper.setResponseResult(responseResult, "true", desc);
				}catch(Exception e){
					try{
						log = config.getAction().log(config, e);
						if(log != null){
							desc = authEmails.get(i)+" " +log.getDescription();
							log.setDescription(desc);
							log.setSuccess(false);
						}				
					}catch(Exception ex){
						logger.error("task log fail,"+ex.getMessage());
					}
					RespCfgHelper.setResponseResult(responseResult, "false", desc);
				}
				if(log != null){
					AuditLogFacade.send(log);
				}
				ReportTask reportTask=new ReportTask();
				if(reportTask != null){
					try{
						eventResponseService.addResponseResult(responseResult);
					}catch(Exception e){
						logger.error("add response result fail,"+e.getMessage());
					}
				}
			}
			HtmlAndFileUtil.clearPath(filePath+"/tmp");
		} catch (Exception e) {
			AuditRecord log = null;
			Block receiveBlock = config.getBlockbyKey("reportconfig");
			List<String> authEmails = receiveBlock.getItemValueList("report_maillist");
			String desc = "";
			for (int i = 0; i < authEmails.size(); i++) {
				ResponseResult responseResult = RespCfgHelper.newResponseResult(config);
				try{
					log = config.getAction().log(config, e);
					if(log != null){
						desc = authEmails.get(i)+" " +log.getDescription();
						log.setDescription(desc);
						log.setSuccess(false);
					}
				}catch(Exception ex){
					logger.error("task log fail,"+ex.getMessage());
				}
				RespCfgHelper.setResponseResult(responseResult, "false", desc);
				if(log != null){
					AuditLogFacade.send(log);
				}
			}
		}
		logger.info("------计划报表执行结束！--------");
	}

	private ReportQueryConditions configToConditions(Config config){
		ReportQueryConditions conditions=new ReportQueryConditions();
		Block rptInfoBlock = config.getBlockbyKey("reportconfig");
		String type = rptInfoBlock.getItemValue("report_type");
		
		String[] rptDate = ReportUiUtil.getExpTime(type);
		type = ReportUiUtil.rptTypeByEgType(type);//
		// 制作人
		String rptUser = GlobalUtil.isNullOrEmpty(rptInfoBlock.getItemValue("report_user"))?"":rptInfoBlock.getItemValue("report_user");
		// 开始时间
		conditions.setStime(rptDate[0]);
		// 结束时间
		conditions.setEndtime(rptDate[1]);
		conditions.setExportFormat(rptInfoBlock.getItemValue("report_filetype"));
		// 主报表ID
		conditions.setParentIds(new Integer[]{new Integer(rptInfoBlock.getItemValue("parent_report_id"))});
		// top
		conditions.setTopn(Integer.valueOf(rptInfoBlock.getItemValue("report_topn")));
		// 日志源类型
		conditions.setSecurityObjectType(rptInfoBlock.getItemValue("security_object_type"));
		if (null != rptInfoBlock.getItemValue("resource_id")) 
			conditions.setResourceId(Long.valueOf(rptInfoBlock.getItemValue("resource_id")));
		
		// 报表设备ip
		if (null != rptInfoBlock.getItemValue("device_ip")) 
			conditions.setDvcAddress(rptInfoBlock.getItemValue("device_ip"));
		conditions.setParams(rptInfoBlock.getItemValue("device_params"));
		// 报表类型
		conditions.setUsername(rptUser);
		/***************************************/
		conditions.setNodeIds(new String[]{rptInfoBlock.getItemValue("node_id")});
		/***************************************/
		return conditions;
	}
	
}
