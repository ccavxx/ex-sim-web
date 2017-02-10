package com.topsec.tsm.sim.log.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.sim.log.service.ScheduleStatTaskService;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.response.respimp.mail.MailHelper;
import com.topsec.tsm.tal.response.respimp.mail.MailSenderInfo;
import com.topsec.tsm.tal.service.EventResponseService;
/**
 * 周期性统计报表发送邮件
 * @author hp
 *
 */
public class ScheduleStatTaskMailSender extends Thread{

	private ScheduleStatTaskService service ;
	private ScheduleStatTask task ;
	
	public ScheduleStatTaskMailSender(ScheduleStatTaskService service, ScheduleStatTask task) {
		super();
		this.service = service;
		this.task = task;
	}
	
	public ScheduleStatTaskMailSender(ScheduleStatTaskService service,Integer taskId) {
		super();
		this.service = service;
		this.task = service.getStatTask(taskId, true, true) ;
	}

	@Override
	public void run() {
		send(task) ;
	}
	
	public void send(ScheduleStatTask task){
		EventResponseService eventResponseService = (EventResponseService) SpringContextServlet.springCtx.getBean("eventResponseService") ;
		List<Response> responses=eventResponseService.getResponsesbyCfgKey("sys_cfg_mailserver");
		if(ObjectUtils.isEmpty(responses) || task == null){
			return ;
		}
		String mailAddresses = task.getEmail() ;
		if(StringUtil.isBlank(mailAddresses)){
			return  ;
		}
		Response response=responses.get(0);
		String fileName = task.getName() ;
		String tmpDir = System.getProperty("java.io.tal.tmpdir");
		if(tmpDir == null)
			tmpDir = System.getProperty("java.io.tmpdir");
		String directory = tmpDir+File.separator+"schedule_task_file" ;
		try {
			File dir = new File(directory) ;
			if(!dir.exists()){
				FileUtils.forceMkdir(dir);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			directory = tmpDir ;
		}
		String filePath = directory + File.separator+fileName ;
		File reportFile = new File(filePath+".docx") ;
		int count = 0 ;
		while(reportFile.exists() && !reportFile.delete()){//如果文件存在，而且没删除成功，增加计数，继续尝试
			reportFile = new File(filePath + (++count) +".docx") ;
		}
		FileOutputStream fos = null ;
		try {
			Config mailServerConfig = RespCfgHelper.getConfig(response);
			Block mailsvrBlock = mailServerConfig.getDefaultBlock();
			String serverIP = mailsvrBlock.getItemValue("serverip");
			String serverPort = mailsvrBlock.getItemValue("serverport");
			String mailSender = mailsvrBlock.getItemValue("mailsender");
			String needAuth = "true";
			String account = mailsvrBlock.getItemValue("loginaccount");
			String password = mailsvrBlock.getItemValue("loginpwd");
			boolean ssl = StringUtil.booleanVal(mailsvrBlock.getItemValue("ssl")) ;
			ScheduleStatResultWordExporter exporter = new ScheduleStatResultWordExporter() ;
			exporter.setTask(task) ;
			fos = new FileOutputStream(reportFile) ;
			exporter.exportTo(fos) ;
			MailSenderInfo senderInfo = new MailSenderInfo(serverIP, serverPort, mailSender, needAuth, account, password,ssl) ;
			List<String> files = new ArrayList<String>(1) ;
			files.add(reportFile.getAbsolutePath()) ;
			MailHelper.mailAlert(mailAddresses, task.getName(), "附件中是"+task.getName(), files, senderInfo) ;
		} catch (Exception e) {
			e.printStackTrace();
			AuditLogFacade.send(AuditLogFacade.createSystemAuditLog().sysFailNotify("查询统计", "查询统计结果发送邮件时出错:"+e.getMessage())) ;
		}finally{
			ObjectUtils.close(fos) ;
			if(reportFile.exists()){
				try {
					FileUtils.forceDelete(reportFile) ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
