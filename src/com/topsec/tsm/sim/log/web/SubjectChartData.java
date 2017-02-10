package com.topsec.tsm.sim.log.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thoughtworks.xstream.alias.ClassMapper.Null;
import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.hibernate.StatSubject;
import com.topsec.tal.base.index.template.DeviceTypeTemplate;
import com.topsec.tal.base.index.template.GroupCollection;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.index.template.LogField;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.util.CommonUtils;

public class SubjectChartData {
	
	public static JSONObject chartData(ReportTask task,JSONArray allRecord){
		List<String> groupColumns = task.getBrowseObject().getGroupColumns();
		String catAxisField = task.getCategoryAxisField() ;
		if(StringUtil.isBlank(catAxisField)){
			catAxisField = groupColumns.get(0) ;
		}
		List<String> seriesFieldList = new ArrayList<String>() ;
		List<String> categories = new ArrayList<String>() ;
		String seriesField = null ;
		if(groupColumns.size() == 2){//包含多个series
			//如果catAxisFeild是第0个则seriesField是第一个，否则相反
			seriesField = groupColumns.indexOf(catAxisField) == 0 ? groupColumns.get(1) : groupColumns.get(0) ;
		}
		String functionName = task.getBrowseObject().getFunctionName() ;//统计方式
		formatRecord(allRecord,catAxisField);
		String unit = UnitFormatter.format(allRecord, task.getBrowseObject().getFunctionField(), functionName) ;
		JSONArray chartData= new JSONArray();
		JSONObject result = new JSONObject();
		for(Object record:allRecord){
			JSONObject jsonRecord = (JSONObject) record;
			String catValue = jsonRecord.getString(catAxisField) ;
			if(!categories.contains(catValue)){
				categories.add(catValue) ;
			}
			String seriesValue = jsonRecord.getString(seriesField) ;
			if(!seriesFieldList.contains(seriesValue)){
				seriesFieldList.add(seriesValue) ;
			}
			if(task.getDiagram() == 5){
				JSONArray pieData = new JSONArray();
				pieData.add(catValue);
				pieData.add(jsonRecord.get(functionName));
				chartData.add(pieData);
			}
		}
		if(task.getDiagram() == 6 || task.getDiagram() == 1){
			for(String series: seriesFieldList){
					JSONObject seriesObject = new JSONObject();
					Object[] seriesData = new Object[categories.size()] ;
						if(series == null){
							series = ObjectUtils.equalsAny(seriesField, "BYTES_IN","BYTES_OUT") ? "流量" : "数量" ;
							for(int i=0;i<allRecord.size();i++){
								JSONObject record = (JSONObject) allRecord.get(i) ;
								seriesData[i] = record.get(functionName);
							}
						}else{
							for(int i=0;i<allRecord.size();i++){
								JSONObject record = (JSONObject) allRecord.get(i) ;
								String catValue = record.getString(catAxisField) ;
								int catIndex = categories.indexOf(catValue) ;
								if(!record.getString(seriesField).equals(series)){
									seriesData[catIndex] = 0;
									continue ;
								}
								if(catIndex >= 0){
									seriesData[catIndex] = record.get(functionName);
								}
							}
						}
					seriesObject.put("name",series);
					seriesObject.put("data",seriesData);
					if(task.getDiagram()== 1){
						seriesObject.put("type","bar");
						if(seriesData.length>20){
							
							//seriesObject.put("barMaxWidth", "35");
						}else{
							seriesObject.put("barMaxWidth", "35");
						}
					}	
					if(task.getDiagram() == 6){
						seriesObject.put("type","line");
						seriesObject.put("smooth","true");
					}
					//seriesObject.put("stack", "true");
					chartData.add(seriesObject);
			}
			result.put("xAxis", categories);
			
		}
		result.put("tabledata", allRecord);
		result.put("seriesData", chartData);
		result.put("groupFields",getGroupFields(task));
		result.put("functionName",task.getBrowseObject().getFunctionName());
		result.put("unit", unit) ;
		if(StringUtil.isBlank(unit)){
			result.put("statFunDesc", getFunctionDesc(task.getBrowseObject().getFunctionName()));
		}else{
			result.put("statFunDesc", getFunctionDesc(task.getBrowseObject().getFunctionName())+"("+unit+")");
		}
		return result;
	}
	private static void formatRecord(JSONArray allRecord,String catAxisField){
		Collection<JSONObject> c=new ArrayList<JSONObject>();
		for(Object record:allRecord){
			JSONObject jsonRecord = (JSONObject) record;
			String catValue = jsonRecord.getString(catAxisField) ;
			if(jsonRecord.containsKey("PRIORITY")){
				jsonRecord.put("PRIORITY", CommonUtils.getLevel(jsonRecord.get("PRIORITY") )) ;
			}
			if (catValue != null && catValue.indexOf('�') > -1) {
				c.add(jsonRecord);
			}
		}
		allRecord.removeAll(c);
	}
	private static List<LogField> getGroupFields(ReportTask task){
		SearchObject searchObject = task.getBrowseObject() ;
		DeviceTypeTemplate template = IndexTemplate.getTemplate(searchObject.getType()) ;
		GroupCollection groupCollection = template.getGroup(searchObject.getGroup()) ;
		List<String> groupColumns = searchObject.getGroupColumns() ;
		return groupCollection.getField(groupColumns) ;
	}
	private static List<String> getReportTableHeaders(ReportTask task) {
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
	private static String getFunctionDesc(String functionName) {
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
}
