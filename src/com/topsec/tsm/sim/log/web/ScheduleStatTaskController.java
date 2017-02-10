package com.topsec.tsm.sim.log.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tal.base.hibernate.StatSubject;
import com.topsec.tal.base.log.stat.DefaultStatInterval;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.exception.TimeExpressionException;
import com.topsec.tsm.sim.common.web.JSONArgument;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.log.service.ScheduleStatTaskService;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;

@Controller
@RequestMapping("scheduleStatTask")
public class ScheduleStatTaskController {
	
	private static final Logger log = LoggerFactory.getLogger(ScheduleStatTaskController.class);
	private ScheduleStatTaskService scheduleStatTaskService;
	private LogReportTaskService logReportTaskService;
	
	@Autowired
	public void setScheduleStatTaskService(ScheduleStatTaskService scheduleStatTaskService) {
		this.scheduleStatTaskService = scheduleStatTaskService;
	}
	@Autowired
	public void setLogReportTaskService(LogReportTaskService logReportTaskService) {
		this.logReportTaskService = logReportTaskService;
	}
	/**
	 * 判断任务名称是否已经存在
	 * 
	 * @param taskName
	 * @return
	 */
	@RequestMapping("isNameExist")
	@ResponseBody
	public Object isNameExist(@RequestParam("operation")String operation,@RequestParam("name") String taskName,@RequestParam(value="taskId")Integer taskId) {
		ScheduleStatTask task = scheduleStatTaskService.getStatTaskByName(taskName);
		JSONObject object = new JSONObject();
		if("add".equals(operation)){//创建任务时只需要判断名称是否存在就可以
			object.put("result", task != null ? true : false);// 任务名称已存在
		}else{//编辑模式
			if(task != null){
				object.put("result", !task.getId().equals(taskId)) ;//名称相同，id不相同说明同名的任务已经存在
			}
		}
		return object ;
	}
	
	@RequestMapping("getSubjectById")
	@ResponseBody
	public Object getSubjectById(@RequestParam("id")Integer id){
		List<StatSubject> subjectList = scheduleStatTaskService.getStatTask(id,true,false).getSubjects();
		DataSourceService dataSourceService = (DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService") ;
		JSONArray resultList = new JSONArray(subjectList.size());
		try{
			for(StatSubject subjectObject:subjectList){
				ReportTask reportTask=logReportTaskService.getTask(subjectObject.getSubjectId());
				if(reportTask == null){
					continue ;
				}
				JSONObject taskJSON = FastJsonUtil.toJSON(reportTask, "creater","diagram","taskName") ;
				FastJsonUtil.mergeToJSON(taskJSON, subjectObject,"subjectId=id","progress",
					"interval","$itvl:interval=intervalTxt","$d2s:logBeginTime","$d2s:logEndTime",
					"securityObjectType=deviceType","$dt_cn:securityObjectType=deviceTypeTxt","host") ;
				if(StringUtil.isBlank(subjectObject.getHost())){
					taskJSON.put("dataSource", subjectObject.getSecurityObjectType()) ;
					taskJSON.put("dataSourceName", DeviceTypeNameUtil.getDeviceTypeName(subjectObject.getSecurityObjectType())) ;
				}else{
					SimDatasource ds = dataSourceService.findByDeviceTypeAndIp(subjectObject.getSecurityObjectType(),subjectObject.getHost()) ;
					if(ds != null){
						taskJSON.put("dataSource", String.valueOf(ds.getResourceId())) ;
						taskJSON.put("dataSourceName", ds.getResourceName()) ;
					}else{
						taskJSON.put("dataSource", subjectObject.getHost()) ;
						taskJSON.put("dataSourceName", subjectObject.getHost()) ;
					}
				}
				resultList.add(taskJSON) ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	
	/**
	 * 判断任务是否执行完成
	 * @param id
	 * @return
	 */
	@RequestMapping("getStatus")
	@ResponseBody
	public Object getStatTaskStatus(@RequestParam(value="id") Integer id){
		ScheduleStatTask statTask = scheduleStatTaskService.getStatTask(id,false,false);
		return statTask.getStatus();
	}
	/**
	 * 根据id获取任务信息
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("getById")
	public String getStatTaskById( HttpServletRequest request,@RequestParam(value = "id") Integer id) {
		ScheduleStatTask statTask = null;
		try {
			statTask = scheduleStatTaskService.getStatTask(id,true,true);
			request.setAttribute("statTask", statTask);
			String timeExpressionString = statTask.getPeriod();
			if(StringUtil.isNotBlank(timeExpressionString)){
				request.setAttribute("timerExpression", new TimeExpression(statTask.getPeriod(),statTask.getTime()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/page/log/scheduleTaskCreate";
	}

	/**
	 * 删除任务
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public Object delStatTask(@RequestParam(value = "id", defaultValue = "") String id) {
		Result result = new Result();
		String[] taskIds=StringUtil.split(id);
		if(taskIds.length ==0 ){
			return result.buildError("无效的统计任务！") ;
		}
		ScheduleStatTask statTask=null;
		for(String taskId:taskIds){
			if(StringUtil.isNotBlank(taskId)){
				statTask = scheduleStatTaskService.deleteStatTask(Integer.parseInt(taskId));
				try {
					NodeUtil.sendCommand(RouteUtils.getQueryServiceRoutes(), MessageDefinition.CMD_SCHEDULE_STAT_DEL,statTask.getId()) ;
				} catch (CommunicationException e) {
					e.printStackTrace();
				}
			}
		}
		result.buildSuccess(statTask != null);
		return result;
	}

	/**
	 * 创建任务
	 * @param logReportTask
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("save")
	@ResponseBody
	public Object save(SID sid,@JSONArgument ScheduleStatTask scheduleStatTask)throws Exception {
		Result result = new Result();
		try {
			Parameter parameter = scheduleStatTask.getOtherParameter() ;
			TimeExpression timeExpression = CommonUtils.createTimeExpression(scheduleStatTask.getOtherParameter());
			scheduleStatTask.setCreator(sid.getUserName());
			scheduleStatTask.setPeriod(timeExpression.getType());
			scheduleStatTask.setTime(timeExpression.getExpression());
			scheduleStatTask.setRole(sid.getRole()) ;
			result = valid(scheduleStatTask, parameter) ;
			if(!result.isSuccess()){
				return result ;
			}
			String operation = parameter.getValue("operator");
			String[] searchNodeRoutes = RouteUtils.getQueryServiceRoutes() ;
			if (operation.equals("add")) {
				scheduleStatTaskService.saveStatTask(scheduleStatTask);
				NodeUtil.sendCommand(searchNodeRoutes, MessageDefinition.CMD_SCHEDULE_STAT_ADD,scheduleStatTask) ;
				result.buildSuccess();
			} else if (operation.equals("update")){
				ScheduleStatTask statTask = scheduleStatTaskService.getStatTask(scheduleStatTask.getId(),true,false);
				scheduleStatTask.setEnabled(statTask.isEnabled());
				if(!statTask.isEnabled()){
					scheduleStatTask.setStatus(statTask.getStatus());
				}
				scheduleStatTaskService.updateStatTask(scheduleStatTask);
				HashMap<String,Object> commandParams = new HashMap<String,Object>(2) ;
				commandParams.put("taskId", scheduleStatTask.getId()) ;
				commandParams.put("expression", scheduleStatTask.getTime()) ;
				NodeUtil.sendCommand(searchNodeRoutes, MessageDefinition.CMD_SCHEDULE_STAT_MODIFY,commandParams) ;
				result.buildSuccess();
			}
		}catch(TimeExpressionException e){
			result.buildError(e.getMessage()) ;
		} catch (ResourceNameExistException e) {
			result.buildError("任务名称已经存在!");
		} catch (Exception e) {
			e.printStackTrace() ;
			result.buildError("创建任务失败，系统出错！") ;
		}
		return result;
	}
	
	private Result valid(ScheduleStatTask task,Parameter parameter){
		Result result = new Result(true, null) ;
		int timeLimit = 10 ;
		if(task.getPeriod().equals(TimeExpression.TYPE_INTERVAL_MINUTE) && parameter.getInt("min") < timeLimit){
			return result.buildError("分钟间隔时间不能小于{}分钟！",timeLimit) ;
		}
		if(ObjectUtils.isEmpty(task.getSubjects())){
			return result.buildError("任务主题列表为空！") ;
		}
		int index = 0 ;
		for(StatSubject subject:task.getSubjects()){
			index++ ;
			ReportTask rt = logReportTaskService.getTask(subject.getSubjectId()) ;
			if(rt == null){
				return result.buildError("第{}个主题已经被删除",index) ;
			}
			if(DefaultStatInterval.INTERVAL_USER_DEFINE.equals(subject.getInterval())){
				if(subject.getLogBeginTime() == null || subject.getLogEndTime() == null){
					return result.buildError("“{}”没有选择时间范围！",rt.getTaskName()) ;
				}
			}
		}
		return result ;
	}
	
	/**
	 * 启用禁用
	 * @param scheduleId
	 * @param status
	 * @return
	 */
	@RequestMapping("changeStatus")
	@ResponseBody
	public Object changeStatus(@RequestParam(value = "id")Integer id,@RequestParam("enabled") boolean enabled){
		Result result = new Result();
		try {
			ScheduleStatTask scheduleStatTask=scheduleStatTaskService.getStatTask(id,true,true);
			scheduleStatTask.setEnabled(enabled);
			scheduleStatTaskService.updateStatTask(scheduleStatTask);
			
			String[] searchNodeRoutes = RouteUtils.getQueryServiceRoutes() ;
			try {
				if(scheduleStatTask.isEnabled()){
					NodeUtil.sendCommand(searchNodeRoutes, MessageDefinition.CMD_SCHEDULE_STAT_ADD,scheduleStatTask) ;
				}else{
					NodeUtil.sendCommand(searchNodeRoutes, MessageDefinition.CMD_SCHEDULE_STAT_DEL,scheduleStatTask.getId()) ;
				}
			} catch (CommunicationException e){
				e.printStackTrace();
			}
			result.buildSuccess();
		} catch (Exception e) {
			log.error("", e) ;
			result.buildSystemError();
		}
		return result;
	} 
	/**
	 * 获取任务列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "getList", produces = "text/javascript;charset=utf-8")
	@ResponseBody
	public String getStatTaskList(
			@RequestParam(value = "page", defaultValue = "1") Integer pageIndex,
			@RequestParam(value = "rows", defaultValue = "20") Integer pageSize,SID sid) {
		Map<String, Object> condition = new HashMap<String, Object>();
		if(sid.hasOperatorRole()){
			if(sid.isOperator()){
				condition.put("role","operator");
			}else{
				condition.put("creator", sid.getUserName());
			}
		}else if(sid.hasAuditorRole()){
			if(sid.isAuditor()){
				condition.put("role","auditor");
			}else{
				condition.put("creator", sid.getUserName());
			}	
		}else if(sid.hasAdminRole()){
			if(sid.isAdmin()){
				condition.put("role","admin");
			}else{
				condition.put("creator", sid.getUserName());
			}	
		}
		PageBean<ScheduleStatTask> pager = scheduleStatTaskService.searchStatTaskList(pageIndex, pageSize,condition);
		JSONObject result = new JSONObject() ;
		JSONArray tasklist = FastJsonUtil.toJSONArray(pager.getData(),new JSONConverterCallBack<ScheduleStatTask>(){
			@Override
			public void call(JSONObject result, ScheduleStatTask obj) {
				result.put("period", CommonUtils.getTimeExpressionText(obj.getPeriod())) ;
				result.put("beginTime", StringUtil.longDateString(obj.getBeginTime())) ;
				result.put("endTime", StringUtil.longDateString(obj.getEndTime())) ;
			}
		}, "creator","id","name","enabled","time","status") ;
		
		result.put("total", pager.getTotal());
		result.put("rows", tasklist);
		return result.toJSONString();
	}
	/**
	 * 查看主题结果
	 * 
	 * @param scheduleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "previewSubjectResult", produces = "text/javascript;charset=utf-8")
	public String previewSubjectResult(@RequestParam(value = "scheduleId") Integer scheduleId,HttpServletRequest request) throws Exception {
		JSONArray subArray = new JSONArray();
		try {
			List<StatSubject> subjectList=scheduleStatTaskService.getStatTask(scheduleId,true,true).getSubjects();
			for(StatSubject subject:subjectList){
				JSONObject jsonObject = new JSONObject();
				ReportTask reportTask=logReportTaskService.getTask(subject.getSubjectId());
				if(reportTask != null && subject != null){
					jsonObject.put("subName",reportTask.getTaskName());
					jsonObject.put("diagram", reportTask.getDiagram());
					JSONArray allRecord = JSON.parseArray(subject.getStatResult()) ;
					jsonObject.put("data",SubjectChartData.chartData(reportTask,allRecord) );
					jsonObject.put("browseObject", reportTask.getBrowseObject());
					subArray.add(jsonObject);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		request.setAttribute("subjectList", subArray);
		return "page/log/scheduleTaskview";
	}
	
	@RequestMapping("export")
	public void export(@RequestParam("taskId")Integer taskId,HttpServletRequest request,HttpServletResponse response){
		ScheduleStatResultExporter exporter = new ScheduleStatResultWordExporter() ;
		ScheduleStatTask task = scheduleStatTaskService.getStatTask(taskId,true,true) ;
		if (task != null) {
			try {
				response.setCharacterEncoding("UTF-8");
				CommonUtils.setDownloadHeaders(request, response, task.getName()+".docx") ;
				exporter.setTask(task) ;
				exporter.exportTo(response.getOutputStream()) ;
			} catch (IOException e) {
				log.error("导出报表失败！",e) ;
			}
		}
	}

}