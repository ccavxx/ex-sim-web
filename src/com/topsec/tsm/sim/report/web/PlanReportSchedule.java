/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
* @since  2011-08-14
* @version 1.0
* 
*/
package com.topsec.tsm.sim.report.web;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.topsec.tal.base.util.CanInit;
import com.topsec.tal.base.util.ISpringUtil;
import com.topsec.tal.base.util.config.ConfigType;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.tal.response.adjudicate.ResponseInMem;
import com.topsec.tsm.tal.schedule.QuartzManager;
import com.topsec.tsm.tal.service.EventResponseService;

/**
* 功能描述: 计划报表初始化调度器, 配置在spring中, 随servlet启动而启动
*/
public class PlanReportSchedule implements CanInit{

	private static Logger _log = Logger.getLogger(PlanReportSchedule.class);
	
	private EventResponseService eventResponseService;
	
	@Override
	public void init(Map<String, Object> args) {
		_log.debug("PlanReportSchedule init start");
		try {
			QuartzManager.clearJob();
		} catch (Exception e) {
			e.printStackTrace();
		}
		initPlanReportResponse(args);
		_log.debug("PlanReportSchedule init end");
	}
	
	
	/**
	 * initPlanReportResponse初始化
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void initPlanReportResponse(Map<String, Object> args) {
		_log.debug("com.topsec.tsm.tal.ui.report.schedulerpt.PlanReportSchedule.initPlanReportResponse() start!");
		ISpringUtil spring = null;
		if(args != null){
			spring = (ISpringUtil)args.get(ISpringUtil.SYS_SPRING);
		}
		else{
			throw new RuntimeException("something is error args is null");
		}
		if(spring == null){
			throw new RuntimeException("something is error spring is null");
		}
		
		List<Response> responses = eventResponseService.getResponsesbyType(ConfigType.TYPE_SCHEDULE,"report",-1,-1); 
		if(responses != null){
			_log.debug("init planReportResponse.getResponsesbyType(ConfigType.TYPE_SCHEDULE,'report',-1,-1) to mem. size is:"+ responses.size());
			ResponseInMem.getInstance().clear();
			for (Response response : responses) {
				ResponseInMem.getInstance().addResponse(response);
			}
		}
		_log.debug("com.topsec.tsm.tal.ui.report.schedulerpt.PlanReportSchedule.initPlanReportResponse() end!");
	}

	public EventResponseService getEventResponseService() {
		return eventResponseService;
	}

	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}
}
