package com.topsec.tsm.sim.log.web;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import xylz.util.common.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.index.template.DeviceTypeTemplate;
import com.topsec.tal.base.index.template.GroupCollection;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.index.template.LogField;
import com.topsec.tal.base.index.template.LogFieldPropertyFilter;
import com.topsec.tal.base.log.stat.StatInterval;
import com.topsec.tal.base.log.stat.StatUtil;
import com.topsec.tal.base.search.LogRecordSet;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.ColumnConfigId;
import com.topsec.tsm.base.exception.ResourceNotFoundException;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.service.UserService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.log.bean.LogReportTask;
import com.topsec.tsm.sim.log.service.LogQueryConditionService;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tal.base.hibernate.LogQueryCondition;
@Controller
@RequestMapping("logReport")
public class LogReportTaskController {
	
	private static final Logger log = LoggerFactory.getLogger(LogReportTaskController.class);
	
	private NodeMgrFacade nodeMgrFacade;
	private LogReportTaskService logReportTaskService ;
	private LogQueryConditionService logQueryConditionService;
	@Autowired
	public void setLogQueryConditionService(LogQueryConditionService logQueryConditionService) {
		this.logQueryConditionService = logQueryConditionService;
	}
	private DataSourceService dataSourceService;
	
	@Autowired
	public void setLogReportTaskService(LogReportTaskService logReportTaskService) {
		this.logReportTaskService = logReportTaskService;
	}
	@Autowired
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	
	@Autowired
	public void setNodeMgr(NodeMgrFacade nodeMgr) {
		this.nodeMgrFacade = nodeMgr;
	}
	/**
	 * 判断任务名称是否已经存在
	 * 
	 * @param taskName
	 * @return
	 */
	@RequestMapping("isTaskNameExist")
	@ResponseBody
	public Object isTaskNameExist(@RequestParam(value = "taskName", defaultValue = "") String taskName,
								  @RequestParam("operation")String operation,
								  @RequestParam(value="taskId",required=false)Integer taskId) {
		JSONObject object = new JSONObject();
		LogReportTaskService schedule = logReportTaskService;
		ReportTask task = schedule.getByName(taskName) ; 
		if("add".equals(operation)){//创建任务时只需要判断名称是否存在就可以
			object.put("result", task != null ? true : false);// 任务名称已存在
		}else{//编辑模式
			if(task != null){
				boolean exist =!task.getId().equals(taskId)  ;//名称相同，id不相同说明同名的任务已经存在
				object.put("result", exist) ;
			}
		}
		return object;
	}
	/**
	 * 获取时间间隔list
	 * 
	 */
	@RequestMapping("getLogInterval")
	@ResponseBody
	public Object getLogInterval(){
		List<StatInterval> listInterval = StatUtil.getIntervals();
		return listInterval;
	}
	
	/**
	 * 根据任务id执行任务
	 * @param taskId
	 * @return
	 */
	@RequestMapping("runNowTask")
	@ResponseBody
	public Object runNowTask(@RequestParam(value = "taskId", defaultValue = "")Integer taskId){
			Result result = new Result();
		try {
			ReportTask reportTask = logReportTaskService.getTask(taskId);
			result.build(logReportTaskService.executeSubject(reportTask));
		} catch (Exception e) {
			e.printStackTrace() ;
			result.buildError("主题执行失败!");
		}
		
		return result;
	}
	/**
	 * 删除任务
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public Object delTaskReport(@RequestParam(value = "id", defaultValue = "") String id) {
		Result result = new Result();
		LogReportTaskService schedule = logReportTaskService;
		String[] taskId=id.split(",");
		ReportTask reportTask=null;
		for(int i=0;i<taskId.length;i++){
			if(!StringUtils.isBlank(taskId[i])){
				reportTask = schedule.deleteTask(Integer.parseInt(taskId[i]));
			}
		}
		
		if (reportTask != null) {
			result.buildSuccess(true);
		} else {
			result.buildSuccess(false);
		}
		return result;
	}
	/**
	 * 
	 * 取消任务
	 * @param taskId
	 * @return
	 */
	@RequestMapping("cancelTask")
	@ResponseBody
	public Object cancelTask(@RequestParam(value = "id", defaultValue = "")int taskId){
		
		Result result = new Result();
		ReportTask reportTask = logReportTaskService.cancelTask(taskId);
		
		if (reportTask != null) {
			result.buildSuccess(true);
		} else {
			result.buildSuccess(false);
		}
		return result;
	}

	/**
	 * 创建任务
	 * 
	 * @param logReportTask
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("addTask")
	@ResponseBody
	public Object addTask(@RequestBody LogReportTask logReportTask,SID sid)throws Exception {
		Result result = new Result();

		// 属性英文名称(统计的列,分组的列)
		List<String> fieldNames = new ArrayList<String>();
		//分组方式
		List<String> statMethods=new ArrayList<String>();
		String[] selectProperty = StringUtil.split(logReportTask.getGroupColumn());
		for (String field:selectProperty) {
			String[] selectPt = StringUtil.split(field,":") ;
			fieldNames.add(selectPt[0]);
			statMethods.add(selectPt.length > 1 ? selectPt[1] : null);
		}
        
		// 排序字段
		List<String> orderNames  = Arrays.asList(StringUtil.split(logReportTask.getOrderbyColumm()));
		String method = "";// 函数方法名称
		String fieldname = "";// 函数参数字段
		String[] functionName = StringUtil.split(logReportTask.getFunctionName(),":");
		if (functionName.length > 1) {
			fieldname = functionName[0];
			method = functionName[1];
		}
		
		String nodeid = nodeMgrFacade.getKernelAuditor(false).getNodeId();
		SearchObject browseObject = new SearchObject();
		browseObject.setGroupFunctions(statMethods);
		browseObject.setNodeId(nodeid);
		browseObject.setType(logReportTask.getDeviceType());
		String host = StringUtil.isBlank(logReportTask.getHost()) ? null : logReportTask.getHost() ; 
		browseObject.setHost(host);
		browseObject.setStart(StringUtil.toDateL(logReportTask.getStartTime()));
		browseObject.setEnd(StringUtil.toDateL(logReportTask.getEndTime()));
		
		browseObject.setInterval(logReportTask.getInterval());
		browseObject.setPerPage(logReportTask.getPageNo());
		browseObject.setPage(logReportTask.getPageSize());
		browseObject.setStatColumns(fieldNames);
		browseObject.setGroupColumns(fieldNames);
		browseObject.setOrderColumns(orderNames);
		browseObject.setFunctionName(method);// 函数方法
		browseObject.setFunctionField(fieldname);// 函数参数(获取属性fieldname)
		browseObject.setGroup(logReportTask.getGroup());// 列集
		browseObject.setGroupTopFields(StringUtil.split(logReportTask.getGroupTopFields())) ;
		browseObject.setTop(logReportTask.getTopNumber());
		browseObject.setConditionNames(logReportTask.getConditionName().trim().split(","));
		browseObject.setOperators(logReportTask.getOperator().trim().split(","));
		browseObject.setQueryContents(logReportTask.getQueryContent().trim().split(","));
		browseObject.setQueryTypes(logReportTask.getQueryType().trim().split(","));

		LogReportTaskService schedule = logReportTaskService;
		ReportTask task = new ReportTask();
		task.setBrowseObject(browseObject);
		task.setDiagram(logReportTask.getDiagram());
		task.setTaskName(logReportTask.getTaskName());
		task.setSearchCondition(logReportTask.getSearchCondition());
		task.setCategoryAxisField(logReportTask.getCategoryAxisField());
		
		try {
			if (logReportTask.getTaskOperator().equals("add")) {
				task.setRole(sid.hasAuditorRole() ? "auditor" : sid.hasOperatorRole() ? "operator" : null);
				task.setCreater(sid.getUserName());
				task.setCreateDate(new Date());
				schedule.saveTask(task);
				result.buildSuccess(null);
			} else if (logReportTask.getTaskOperator().equals("update")) {
				task.setId(logReportTask.getTaskId());
				result.build(schedule.updateTask(task));
			}
		} catch (ResourceNameExistException e) {
			result.buildError("主题名称已经存在!");
		}
		return result;
	}
	/**
	 * 根据设备类型和列集获取属性信息
	 * @param deviceType
	 * @param group
	 * @return
	 */
	@RequestMapping(value = "getTableHaderProperty", produces = "text/javascript;charset=UTF-8")
	@ResponseBody
    public Object getTableHaderProperty(@RequestParam(value = "type")String deviceType,
    									@RequestParam(value = "group")String group){
		DeviceTypeTemplate template = IndexTemplate.getTemplate(deviceType) ;
		GroupCollection groupCollection = template.getGroup(group) ;
		JSON groupJSON = FastJsonUtil.toJSON(groupCollection, "id","name","visibleFields") ;
		String result = groupJSON.toJSONString() ;
		return result ;
    }
	
	@RequestMapping("showTaskEditor")
	public String showTaskEditor(@RequestParam("taskId")Integer taskId) {
		Result result = new Result() ;
		if (taskId != null) {
			ReportTask reportTask = logReportTaskService.getTaskWithoutResult(taskId) ;
			result.buildSuccess(reportTask) ;
		}else{
			result.buildError("主题不存在！") ;
		}
		return "redirect:/page/" ;
	}
	/**
	 * 获取所有任务
	 * @return
	 */
	@RequestMapping("getAll")
	@ResponseBody
	public Object getAllReportTask(SID sid){
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("role", sid.getRole()) ;
		condition.put("creater", sid.isDefaultUser() ? null : sid.getUserName()) ;
		List<ReportTask> task = logReportTaskService.getAllTask(condition);
		JSONArray jsonArray = FastJsonUtil.toJSONArray(task,new JSONConverterCallBack<ReportTask>() {
			@Override
			public void call(JSONObject result, ReportTask report) {
				SearchObject searchObject = report.getBrowseObject();
				result.put("queryStartTime", StringUtil.longDateString(searchObject.getStart()));
				result.put("queryEndTime", StringUtil.longDateString(searchObject.getEnd()));
				String interval = searchObject.getInterval();
				String intervalCH = StatUtil.getInterval(interval).getLabel();
				result.put("interval",interval);
				result.put("intervalTxt",intervalCH);
				if(searchObject.getStart() != null && searchObject.getEnd() != null){
					result.put("logBeginTime",StringUtil.longDateString(searchObject.getStart()));
					result.put("logEndTime",StringUtil.longDateString(searchObject.getEnd()));
				}
				result.put("deviceType", searchObject.getType());
				result.put("securityObjectType", searchObject.getType()) ;
				result.put("host", searchObject.getHost()) ;
				result.put("deviceTypeTxt", DeviceTypeNameUtil.getDeviceTypeName(searchObject.getType()));				
				result.put("dataSource", StringUtil.ifBlank(searchObject.getHost(), searchObject.getType())) ;//主题资产类型
				SimDatasource ds = dataSourceService.findByDeviceTypeAndIp(searchObject.getType(),searchObject.getHost()) ;
				if(ds == null){
					result.put("dataSource", searchObject.getType()) ;
					result.put("dataSourceName", DeviceTypeNameUtil.getDeviceTypeName(searchObject.getType())) ;
				}else{
					result.put("dataSource", String.valueOf(ds.getResourceId())) ;
					result.put("dataSourceName", ds.getResourceName()) ;
				}
			}
			
		},"id","creater","taskName","$d2s:startTime","$d2s:endTime","diagram","progress","browseObject=searchObject","searchCondition") ;
		return jsonArray;
	}
	/**
	 * 根据主题获取可用的日志源树(日志源模板中必须包含主题定义的所有分组字段和统计字段)
	 * @param sid
	 * @param subjectId
	 * @return
	 */
	@RequestMapping("dataSourceTree")
	@ResponseBody
	public Object dataSourceTree(SID sid,@RequestParam("taskId") Integer taskId){
		ReportTask task = logReportTaskService.getTask(taskId) ;
		if(task == null){
			return new JSONArray(0) ;
		}
		DataSourceService dataSourceService = (DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService") ;
		List<SimDatasource> userDataSources = dataSourceService.getUserDataSource(sid) ;
		List<SimDatasource> validDataSource = new ArrayList<SimDatasource>(userDataSources.size()) ;
		SearchObject searchObject = task.getBrowseObject() ;
		//主题包含的所有列
		Set<String> columns = new HashSet<String>(searchObject.getGroupColumns()) ;
		columns.add(searchObject.getFunctionField()) ;
		//遍历日志源，检查日志源是否适用于指定主题，如果日志源模板中不包含统计主题的所有字段，则日志源认为是不适用于指定的主题
		loop:for(SimDatasource ds:userDataSources){
			DeviceTypeTemplate template = IndexTemplate.getTemplate(ds.getSecurityObjectType()) ;
			for(String col:columns){
				if(template.getField(col) == LogField.NULL_FIELD){
					continue loop;
				}
			}
			validDataSource.add(ds) ;
		}
		JSONArray treeData = DataSourceUtil.getJSONTree(validDataSource,true) ;
		return treeData ;
	}
	
	/**
	 * 获取任务列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "taskList", produces = "text/javascript;charset=utf-8")
	@ResponseBody
	public Object taskList(
			@RequestParam(value = "page", defaultValue = "1") Integer pageIndex,
			@RequestParam(value = "rows", defaultValue = "20") Integer pageSize,SID sid) {
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("role", sid.getRole())  ;
		condition.put("creater", sid.isDefaultUser() ? null : sid.getUserName()) ;
		PageBean<ReportTask> pager = logReportTaskService.search(condition, pageIndex, pageSize);
		List<ReportTask> task = pager.getData();
		List<Map<String, Object>> tasklist = new ArrayList<Map<String, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		for (ReportTask report : task) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("creater",ObjectUtils.nvl(report.getCreater(),""));
			row.put("taskId",report.getId());
			row.put("taskName", report.getTaskName());
			row.put("taskState", report.getTaskState());
			row.put("beginTime", StringUtil.longDateString(report.getStartTime()));
			row.put("endTime", StringUtil.longDateString(report.getEndTime()));
			row.put("diagram", report.getDiagram());
			row.put("progress", report.getProgress() == null ? 0 : report.getProgress().intValue()) ;
			row.put("searchCondition", report.getSearchCondition()) ;
			SearchObject searchObject = report.getBrowseObject();
			row.put("searchObject", searchObject);
			row.put("deviceType",searchObject.getType().equalsIgnoreCase("ALL/ALL/Default") ? "全部":DeviceTypeNameUtil.getDeviceTypeName(searchObject.getType()));
			SimDatasource ds = dataSourceService.findByDeviceTypeAndIp(searchObject.getType(), searchObject.getHost()) ;
			row.put("host",ds == null ? "" : ds.getResourceName());
			row.put("queryStartTime", StringUtil.longDateString(searchObject.getStart()));
			row.put("queryEndTime", StringUtil.longDateString(searchObject.getEnd()));
			row.put("axisField",report.getCategoryAxisField());
			row.put("groupTopFields",report.getBrowseObject().getGroupTopFields());
			StatInterval interval=StatUtil.getInterval(searchObject.getInterval());
			row.put("intervalTxt",interval.getLabel());
			row.put("interval", interval.getValue()) ;
			if(searchObject.getStart() != null && searchObject.getEnd() != null){
				row.put("logIntervalStart",StringUtil.longDateString(searchObject.getStart()));
				row.put("logIntervalEnd",StringUtil.longDateString(searchObject.getEnd()));
			}
			
			tasklist.add(row);
		}
		result.put("total", pager.getTotal());
		result.put("rows", tasklist);
		return result;
	}

	/**
	 * 将表格数据和图表数据添加到对应的数据结构
	 * 
	 * @param tableData
	 *            表格数据
	 * @param chartData
	 *            图表数据
	 * @param task
	 *            任务对象
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void fillData(List<List<Object>> tableData, Map<Object, Object> chartData, ReportTask task) {
		List<String> selectPropertys = new ArrayList<String>(task.getBrowseObject().getGroupColumns());
		String functionName = task.getBrowseObject().getFunctionName();
		LogRecordSet result = new LogRecordSet();
		result.parseMapsFromJson(task.getJsonResult());
		List<Map<String,Object>> records = result.getMaps();
		int diagram = task.getDiagram() ;
		for (int index = 0; index < records.size(); index++) {
			List<Object> tableRow = new ArrayList<Object>();
			Map<String,Object> record = records.get(index);
			List<String> columnValues = new ArrayList<String>(selectPropertys.size()) ;
			for (String column:selectPropertys) {
				Object value = column.equals("PRIORITY") ? CommonUtils.getLevel(record.get(column)) : record.get(column);
				if(value instanceof Date){
					columnValues.add(StringUtil.longDateString((Date)value)) ;
				}else{
					columnValues.add(StringUtil.toString(value)) ;
				}
				tableRow.add(value);
			}
			if(diagram != 0 ) {
				chartData.put(StringUtil.join(columnValues,"|"),record.get(functionName));
			}
			tableRow.add(record.get(functionName));
			tableData.add(tableRow);
		}
	}

	/**
	 * 查看统计结果
	 * 
	 * @param taskid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "viewTaskResult", produces = "text/javascript;charset=utf-8")
	@ResponseBody
	public Object viewTaskResult(@RequestParam(value = "taskid") Integer taskid) throws Exception {
		JSONObject jsonObject = new JSONObject();
		try {
			LogReportTaskService schedule = logReportTaskService;
			ReportTask task = schedule.getTask(taskid);
			if (task == null) {
				throw new ResourceNotFoundException(String.valueOf(taskid)) ;
			}
			if(task.getTaskState() != ReportTask.TASK_STATE_SUCCESS){
				throw new CommonUserException("主题未统计完毕，不能查看结果！") ;
			}
			jsonObject.put("diagram", task.getDiagram());
			jsonObject.put("subTitle",task.getTaskName());
			jsonObject.put("browseObject", task.getBrowseObject());
			List<Map<String, String>> columnNames = IndexTemplateUtil.getInstance().getVisiableGroupColumnNames(task.getBrowseObject().getType(), task.getBrowseObject().getGroup());
			Map<String, String> nameMap = new TreeMap<String, String>();// 字段中文
			// 全部的属性信息
			for (Map<String, String> column : columnNames) {
				Map.Entry<String, String> entry = column.entrySet().iterator().next();
				String key = entry.getKey();
				String value = entry.getValue();
				nameMap.put(key, value);
			}
			jsonObject.put("conditionName",task.getSearchCondition());
			SimDatasource ds = dataSourceService.findByDeviceTypeAndIp(task.getBrowseObject().getType(), task.getBrowseObject().getHost()) ;
			jsonObject.put("host",ds == null ? "" : ds.getResourceName());
			String interval=StatUtil.getInterval(task.getBrowseObject().getInterval()).getLabel();
			jsonObject.put("interval",interval);
			jsonObject.put("logIntervalStart",StringUtil.longDateString(task.getBrowseObject().getStart()));
			jsonObject.put("logIntervalEnd",StringUtil.longDateString(task.getBrowseObject().getEnd()));
			jsonObject.put("chartData",SubjectChartData.chartData(task,toJSONArray(task.getJsonResult())) );
		} catch(ResourceNotFoundException e){
			jsonObject.put("errorMessage", "主题不存在！") ;
		} catch(CommonUserException e){
			jsonObject.put("errorMessage", e.getMessage()) ;
		}catch (Exception e) {
			jsonObject.put("errorMessage", "系统内部出错，无法查看主题结果！") ;
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	private static JSONArray toJSONArray(List<String> result){
		JSONArray gridData = new JSONArray() ;
		if(result == null) return gridData ;
		for (String jsonString:result) {
			try {
				JSONObject record = JSONObject.parseObject(jsonString);
				gridData.add(record) ;
			} catch (Exception e) {
				log.error("转换数据失败",e) ;
			}
		}
		return gridData ;
	}
	/**
	 * 获得任务结果列表的表头信息
	 * 
	 * @param task
	 * @return
	 */
	private List<String> getReportTableHeaders(ReportTask task) {
		SearchObject searchObject = task.getBrowseObject() ;
		DeviceTypeTemplate template = IndexTemplate.getTemplate(searchObject.getType()) ;
		GroupCollection groupCollection = template.getGroup(searchObject.getGroup()) ;
		List<String> groupColumns = searchObject.getGroupColumns() ;
		List<String> columnAlias = new ArrayList<String>(groupColumns.size());
		for(String column:groupColumns){
			LogField field = groupCollection.getField(column) ;
			columnAlias.add(field == null ? column : field.getAlias()) ;
		}
		return columnAlias;
	}

	/**
	 * 根据函数名称，返回函数中文描述
	 * 
	 * @return
	 */
	private String getFunctionDesc(String functionName) {
		String functionDesc = null;
		if (functionName != null) {
			if (functionName.equalsIgnoreCase("count")) {
				functionDesc = "统计";
			}
			if (functionName.equalsIgnoreCase("sum")) {
				functionDesc = "和";
			}
			if (functionName.equalsIgnoreCase("avg")) {
				functionDesc = "平均值";
			}
		}
		return functionDesc;
	}

	/**
	 * 生成报表
	 * 
	 * @param response
	 * @param taskid
	 * @param fileformat
	 * @param filextension
	 */
	@RequestMapping(value = "exportTaskResult")
	@ResponseBody
	public void exportTaskResult(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value = "taskid") Integer taskid,
			@RequestParam(value = "fileformat") String fileformat,
			@RequestParam(value = "filextension") String filextension) {
		try {
			ReportTask task = logReportTaskService.getTask(taskid);
			List<List<Object>> tableData = new ArrayList<List<Object>>();
			Map<Object, Object> chartData = new LinkedHashMap<Object, Object>();
			fillData(tableData, chartData, task);
			if(!task.getDiagram().equals(0)){
				SearchObject searchObject = task.getBrowseObject();
				List<String> field = new ArrayList<String>();
				field.add(task.getBrowseObject().getFunctionField());
				searchObject.setStatColumns(field);
				task.setBrowseObject(searchObject);
			}
			
			List<String> headers = getReportTableHeaders(task);
			String[] searchCondition=task.getSearchCondition().split(",");
			String host = task.getBrowseObject().getHost();
			StringBuffer conditionStr = new StringBuffer();
			String intervalLabel = StatUtil.getInterval(task.getBrowseObject().getInterval()).getLabel();
			StringBuffer intervalBuffer = new StringBuffer(); 
			if(task.getBrowseObject().getStart() != null){
				intervalBuffer.append("(")
							  .append(StringUtil.longDateString(task.getBrowseObject().getStart()))
							  .append("至")
							  .append(StringUtil.longDateString(task.getBrowseObject().getEnd())+")");
			}
		   	conditionStr.append("日志源：").append(StringUtil.ifBlank(host,"全部")).append("   ");
		   	conditionStr.append("时间：").append(intervalLabel).append(intervalBuffer.toString());
		    String queryCondition = StringUtil.join(searchCondition,"");
			if(StringUtil.isNotBlank(queryCondition)){
				conditionStr.append("    过滤条件：");
				conditionStr.append(queryCondition);
			}
			headers.add(getFunctionDesc(task.getBrowseObject().getFunctionName()));
			JRReportFileExporter reportFileCreator = new JRReportFileExporter(
					task.getTaskName(), headers, tableData, chartData,
					task.getDiagram(), fileformat,conditionStr.toString());
			response.setCharacterEncoding("UTF-8");
			CommonUtils.setDownloadHeaders(request, response, task.getTaskName()+"."+filextension) ;
			reportFileCreator.exportReportTo(response.getOutputStream());
		} catch (Exception e) {
			log.error("导出主题异常:" + e.getMessage());
		}
	}
	/**
	 * 
	 * 任务列表中导出excel数据
	 * @param request
	 * @param response
	 * @param taskid
	 */
	@RequestMapping(value = "exportTaskExcel")
	@ResponseBody
	public void exportTaskExcel(HttpServletRequest request,HttpServletResponse response,
								@RequestParam(value = "taskid") Integer taskid){
		     
		try {
			 ReportTask task= logReportTaskService.getTask(taskid);
			 List<List<Object>> tableData = new ArrayList<List<Object>>();
			 Map<Object, Object> chartData = new LinkedHashMap<Object, Object>();
			 fillData(tableData, chartData, task);
			 if(!task.getDiagram().equals(0)){
				SearchObject searchObject = task.getBrowseObject();
				List<String> field = new ArrayList<String>();
				field.add(task.getBrowseObject().getFunctionField());
				searchObject.setStatColumns(field);
				task.setBrowseObject(searchObject);
			}
			List<String> headers = getReportTableHeaders(task);
			headers.add(getFunctionDesc(task.getBrowseObject().getFunctionName()));
			response.setCharacterEncoding("UTF-8");
			String userAgent = request.getHeader("User-Agent") ;
		    response.setContentType("application/vnd.ms-excel");
			if(userAgent.indexOf("Firefox")>0){
				response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" + java.net.URLEncoder.encode(task.getTaskName(), "UTF-8") + ".xls\"");
			}else{
				response.setHeader("Content-Disposition", "attachment; filename=\"" + java.net.URLEncoder.encode(task.getTaskName(), "UTF-8") + ".xls\"");
			}
			exportExcel(response,headers,tableData);  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void exportExcel(HttpServletResponse response,List<String> headers,List<List<Object>> dataset) {
	      try {
	    	  HSSFWorkbook workbook = new HSSFWorkbook();
	          HSSFSheet sheet = workbook.createSheet();
	          sheet.setDefaultColumnWidth(15);
	          HSSFRow row = sheet.createRow((int) 0);
	          HSSFCellStyle style = workbook.createCellStyle();
	          style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	          style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	          style.setBorderRight(HSSFCellStyle.BORDER_THIN);
	          style.setBorderTop(HSSFCellStyle.BORDER_THIN);
	          style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	          style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	          HSSFFont font=workbook.createFont();
	          font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	          style.setFont(font);
	          
	          for (int i = 0; i < headers.size(); i++) {
	              HSSFCell cell = row.createCell(i);
	              String headerText=(String)headers.get(i);
	              cell.setCellValue(headerText);
	              cell.setCellStyle(style);
	          }

	          //遍历集合数据，产生数据行
		      for (int i = 0; i < dataset.size(); i++) {
		    	   List rowlist=(List)dataset.get(i);
		    	   row = sheet.createRow(i+1);
		    	   for (int j = 0; j < rowlist.size(); j++) {
		    		    Object value=(Object)rowlist.get(j);
				    	HSSFCell cell = row.createCell(j);
				    	if(value == null){
				    		continue ;
				    	}
			            if (value instanceof Double) {
			            	cell.setCellValue((Double)value);
			            }else if (value instanceof Number) {
			            	cell.setCellValue(((Number)value).longValue());
			            }else{
			            	cell.setCellValue(value.toString());
			            }
				   }
		    	  
			   }
	    	  OutputStream ouputStream =response.getOutputStream();
	    	  workbook.write(ouputStream);
		      ouputStream.flush();
		      ouputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 保存查询条件
	 * 
	 * @param logReportTask
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("saveQueryCondition")
	@ResponseBody
	public Object saveQueryCondition(@RequestBody LogReportTask logReportTask,SID sid)throws Exception {
		Result result = new Result();
		SearchObject searchObject = new SearchObject();
		searchObject.setType(logReportTask.getDeviceType());
		String host = StringUtil.isBlank(logReportTask.getHost()) ? null : logReportTask.getHost() ; 
		searchObject.setHost(host);
		searchObject.setStart(StringUtil.toDateL(logReportTask.getStartTime()));
		searchObject.setEnd(StringUtil.toDateL(logReportTask.getEndTime()));
		
		searchObject.setInterval(logReportTask.getInterval());
		searchObject.setGroup(logReportTask.getGroup());// 列集
		searchObject.setConditionNames(logReportTask.getConditionName().trim().split(","));
		searchObject.setQueryContents(logReportTask.getQueryContent().trim().split(","));
		searchObject.setQueryTypes(logReportTask.getQueryType().trim().split(","));
		searchObject.setOperators(logReportTask.getOperator().trim().split(","));
		LogQueryCondition logQueryCondition = new LogQueryCondition();
		logQueryCondition.setCreatetime(new Date());
		logQueryCondition.setName(logReportTask.getTitle());
		logQueryCondition.setSearchObject(searchObject);
		logQueryCondition.setCreator(sid.getUserName());
		logQueryConditionService.saveQueryCondition(logQueryCondition);
		result.buildSuccess(null);
		return result;
	}
	@RequestMapping("queryConditionList")
	@ResponseBody
	public Object queryConditionList(SID sid) {
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("creator",sid.getUserName()) ;
		List<LogQueryCondition>  list = logQueryConditionService.queryConditionList(condition);
		return list;
	}
	@RequestMapping("deleteQueryCondition")
	@ResponseBody
	public Object deleteQueryCondition(@RequestParam(value = "id")Integer id){
		Result result = new Result();
		LogQueryCondition logQueryCondition=logQueryConditionService.deleteQueryCondition(id);
		if (logQueryCondition != null) {
			result.buildSuccess(true);
		} else {
			result.buildSuccess(false);
		}
		return result;
	}
	@RequestMapping("findQueryCondition")
	@ResponseBody
	public Object findQueryCondition(@RequestParam(value = "id")Integer id){
 		LogQueryCondition condition = logQueryConditionService.findQueryConditionById(id);
		String interval = condition.getSearchObject().getInterval();
		StatInterval intervalObject = StatUtil.getInterval(interval) ;
		Date[] dateRange = intervalObject.computeDateRange(condition.getSearchObject().getStart(),condition.getSearchObject().getEnd());
		Date startDate = dateRange[0];
		Date endDate = dateRange[1];
        condition.getSearchObject().setStart(startDate);
        condition.getSearchObject().setEnd(endDate);
        String[] en_name = condition.getSearchObject().getConditionNames();
       // String[] zh_name = new String[en_name.length];
        List<String> zh_name = new ArrayList<String>();
        GroupCollection collection = IndexTemplate.getTemplate(condition.getSearchObject().getType()).getGroup(condition.getSearchObject().getGroup()) ;
        for(int i=0;i<en_name.length;i++){
        	List<LogField> list = collection.getFields();
        	for(LogField field:list){
        		LogField logField = new LogField(en_name[i],"");
    			if(list.contains(logField)){
               		if(en_name[i].equals(field.getName())){
               			if(field.getAlias() != null){
               				zh_name.add(field.getAlias());
               				
               			}
               	    }
        		}
        	}
        } 
        condition.getSearchObject().setGroupColumns(zh_name);
		return condition;
	}

}
