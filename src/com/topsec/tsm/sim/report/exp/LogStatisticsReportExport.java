package com.topsec.tsm.sim.report.exp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xylz.util.common.StringUtils;

import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.search.LogRecordSet;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.log.web.JRReportFileExporter;
import com.topsec.tsm.sim.log.web.LogReportTaskController;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.common.ReportFileCreator;
import com.topsec.tsm.sim.report.jasper.JRReportFileCreator;
import com.topsec.tsm.sim.report.jasper.SchemeReport;
import com.topsec.tsm.sim.util.CommonUtils;

/**
 * @ClassName: LogStatisticsReportExport
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年12月24日下午5:38:42
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class LogStatisticsReportExport extends SchemeReport {
	private static final Logger log = LoggerFactory.getLogger(LogStatisticsReportExport.class);
	private ReportTask reportTask;
	public String saveMstReport(ExpStruct exp) throws Exception{
		
		StringBuffer filePath = new StringBuffer(System.getProperty("jboss.server.home.dir")).append(File.separatorChar).append("tmp").append(File.separatorChar);
		//文件名称
		filePath.append(reportTask.getTaskName()).append("_").append(StringUtil.currentDateToString("yyyy-MM-dd HH.mm.ss.SSS")) ;
		File tempFile = new File(filePath.append(exp.getFileExtension()).toString()) ;
		if(tempFile.exists()){//如果文件存在，在文件后追加一个1000以内的随机数
			filePath.append(".").append(new Random().nextInt(1000)) ;
			tempFile = new File(filePath.append(exp.getFileExtension()).toString()) ;
		}
		String zipFile=null;
		try {
			FileOutputStream fileOS = new FileOutputStream(tempFile) ;
			JRReportFileExporter reportFileCreator=initJRReportResultFileCreator(reportTask, exp);
			if (null != reportFileCreator) {
				reportFileCreator.exportReportTo(fileOS);
			}
			fileOS.flush() ;
			ObjectUtils.close(fileOS) ;
			zipFile = zipFile(tempFile.getAbsolutePath().replace('\\', '/')) ;
		} catch (Exception e) {
			e.printStackTrace();
			zipFile=null;
		}finally{
			if(tempFile!=null&&tempFile.exists()){
				tempFile.delete() ;
			}
		}
		return zipFile ;
	}
	public LogStatisticsReportExport() {
		
	}
	public LogStatisticsReportExport(ReportTask reportTask) {
		this.reportTask=reportTask;
	}

	private JRReportFileExporter initJRReportResultFileCreator(ReportTask task,ExpStruct exp){
		JRReportFileExporter reportFileCreator=null;
		try {
			List<List<Object>> tableData = new ArrayList<List<Object>>();
			Map<Object, Object> chartData = new HashMap<Object, Object>();
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
			String host=task.getBrowseObject().getHost();
			String start=StringUtil.dateToString(task.getBrowseObject().getStart(),"yyyy-MM-dd HH:mm:ss");
			String end=StringUtil.dateToString(task.getBrowseObject().getEnd(),"yyyy-MM-dd HH:mm:ss");
	       	 if(StringUtils.isBlank(host)){
	       		 host="全部";
	       	 }
       	    String content="筛选条件：日志源："+host+"    接收时间："+start+"至"+end;
       	    String queryConditon="";
       	    for(int i=0;i<searchCondition.length;i++){
       		 if(searchCondition[i]!=""){
       			  queryConditon+="【"+searchCondition[i]+"】";
       		 }
       	 }
       	 if(queryConditon!=""){
       		 content+="    过滤条件："+queryConditon+"";
       	 }
			headers.add(getFunctionDesc(task.getBrowseObject().getFunctionName()));
			reportFileCreator = new JRReportFileExporter(
					task.getTaskName(), headers, tableData, chartData,
					task.getDiagram(), exp.getFileType(),content);
		} catch (Exception e) {
			log.error("报表任务导出异常:" + e.getMessage());
		}
		return reportFileCreator;
	}
	private void fillData(List<List<Object>> tableData, Map<Object, Object> chartData,
			ReportTask task) {
		List<String> selectPropertys = new ArrayList<String>(task.getBrowseObject().getGroupColumns());
		String functionName = task.getBrowseObject().getFunctionName();
		LogRecordSet result = new LogRecordSet();
		result.parseMapsFromJson(task.getJsonResult());
		
		if (result != null) {
			List<Map<String,Object>> records = result.getMaps();
			int diagram = task.getDiagram() ;
			for (int index = 0; index < records.size(); index++) {
				List<Object> recordObject = new ArrayList<Object>();
				Map record = records.get(index);
				List<String> columnValues = new ArrayList<String>(selectPropertys.size()) ;
				for (String column:selectPropertys) {
						Object value = column.equals("PRIORITY") ? CommonUtils.getLevel(record.get(column)) : record.get(column);
						if(value instanceof Date){
							columnValues.add(StringUtil.longDateString((Date)value)) ;
						}else{
							columnValues.add(StringUtil.toString(value)) ;
						}
						recordObject.add(value);
					
				}
				if(diagram !=0 ) {
					chartData.put(StringUtil.join(columnValues,"|"),record.get(functionName));
				}
				recordObject.add(record.get(functionName));
				tableData.add(recordObject);
			}
		}
	}
	
	private List<String> getReportTableHeaders(ReportTask task) {
		List<Map<String, String>> columnNames = IndexTemplateUtil.getInstance()
				.getVisiableGroupColumnNames(task.getBrowseObject().getType(),
						task.getBrowseObject().getGroup());
		List<String> displayColumnNames = new ArrayList<String>(columnNames.size());
		if (ObjectUtils.isNotEmpty(columnNames)) {
			Map<String, String> columnNameMapping = new HashMap<String, String>();
			for (Map<String, String> column : columnNames) {
				Map.Entry<String, String> entry = (Map.Entry) column.entrySet().iterator().next();
				columnNameMapping.put(entry.getKey(), entry.getValue());
			}
			for (String columnName : task.getBrowseObject().getGroupColumns()) {
				displayColumnNames.add(columnNameMapping.get(columnName));
			}
		} else {
			displayColumnNames.addAll(task.getBrowseObject().getGroupColumns());
		}
		return displayColumnNames;
	}
	
	private String getFunctionDesc(String functionName) {
		String functionDesc = null;
		if (functionName != null) {
			if (functionName.equalsIgnoreCase("count")) {
				functionDesc = "统计";
			}
			if (functionName.equals("sum")) {
				functionDesc = "和";
			}
			if (functionName.equals("avg")) {
				functionDesc = "平均值";
			}
		}
		return functionDesc;
	}
}
