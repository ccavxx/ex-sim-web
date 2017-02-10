package com.topsec.tsm.sim.log.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.charts.AxisCrossBetween;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.hibernate.ScheduleStatTask;
import com.topsec.tal.base.hibernate.StatSubject;
import com.topsec.tal.base.index.template.DeviceTypeTemplate;
import com.topsec.tal.base.index.template.GroupCollection;
import com.topsec.tal.base.index.template.IndexTemplate;
import com.topsec.tal.base.report.poi.BarChartData;
import com.topsec.tal.base.report.poi.ChartData;
import com.topsec.tal.base.report.poi.LineChartData;
import com.topsec.tal.base.report.poi.Pie3DChartData;
import com.topsec.tal.base.report.poi.PieChartData;
import com.topsec.tal.base.report.poi.SimChartDataFactory;
import com.topsec.tal.base.report.poi.SimChartDataFactoryImpl;
import com.topsec.tal.base.report.poi.SimXWPFDocument;
import com.topsec.tal.base.report.poi.XWPFUtil;
import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.log.service.LogReportTaskService;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

public class ScheduleStatResultWordExporter implements ScheduleStatResultExporter {

	private static String textFont = "宋体" ;
	private SimXWPFDocument doc ;
	private LogReportTaskService subjectService ;
	private ScheduleStatTask task;
	public ScheduleStatResultWordExporter() {
		super();
		this.doc = new SimXWPFDocument() ;
		this.subjectService = (LogReportTaskService) SpringContextServlet.springCtx.getBean("logReportTaskService") ;
	}

	@Override
	public void setTask(ScheduleStatTask task) {
		this.task = task ;
	}

	@Override
	public void exportTo(OutputStream os) {
		//标题部分
		addParagraph1(task.getName(),ParagraphAlignment.CENTER) ;//标题
		addParagraph2("创建人："+task.getCreator()) ;//创建人
		addParagraph2("生成时间："+StringUtil.currentDateToString("yyyy-MM-dd HH:mm:ss")) ;//报表生成时间
		XWPFParagraph cat = doc.createParagraph();
		cat.setPageBreak(true) ;
		//目录部分
		List<StatSubject> statSubjects = task.getSubjects() ;
		int seq = 0 ;
		for(StatSubject ss:statSubjects){
			createSubject(ss,seq++) ;
		}
		try {
			doc.write(os) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void createSubject(StatSubject statSubject,int seq){
		ReportTask reportSubject = subjectService.getTask(statSubject.getSubjectId()) ;
		if (reportSubject == null) {
			addParagraph2("主题已经被删除！");
			return ;
		}
		try {
			SearchObject so = reportSubject.getBrowseObject() ;
			DeviceTypeTemplate template = IndexTemplate.getTemplate(so.getType()) ;
			String templateAlias = DeviceTypeNameUtil.getDeviceTypeName(template.getId()) ;
			GroupCollection groupCollection = template.getGroup(so.getGroup()) ;
			List<String> groupColumns = so.getGroupColumns() ;
			JSONArray statResult = JSON.parseArray(statSubject.getStatResult()) ;
			if(statResult == null || statResult.isEmpty()){
				addParagraph2("无统计结果！") ;
				return ;
			}
			String catAxisField = StringUtil.ifBlank(reportSubject.getCategoryAxisField(),groupColumns.get(0)) ;
			formatRecord(statResult, catAxisField) ;
			String unit = UnitFormatter.format(statResult, so.getFunctionField(), so.getFunctionName()) ;
			String seqNum = String.valueOf(++seq)   ; 
			addParagraph2(seqNum + "  " + reportSubject.getTaskName()) ;
			createStatCondition(seqNum+".1",templateAlias,groupCollection.getName(), so,reportSubject) ;
			createDescription(seqNum+".2", so,statSubject) ;
			if(!(reportSubject.getDiagram()== 7)){
				createChart(seqNum+".3", so, statResult,unit, groupColumns, groupCollection,reportSubject) ;
				createTable(seqNum+".4", so, statResult,unit, groupColumns, groupCollection,reportSubject) ;
			}else{
				createTable(seqNum+".3", so, statResult,unit, groupColumns, groupCollection,reportSubject) ;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void formatRecord(JSONArray allRecord,String catAxisField){
		Collection<JSONObject> c=new ArrayList<JSONObject>();
		for(Object record:allRecord){
			JSONObject jsonRecord = (JSONObject) record;
			String catValue = jsonRecord.getString(catAxisField) ;
			if (catValue != null && catValue.indexOf('�') > -1) {
				c.add(jsonRecord);
			}
		}
		allRecord.removeAll(c);
	}
	/**
	 * 1.1 日志源信息
	 * 创建统计条件
	 */
	private void createStatCondition(String seqNum,String dvcType,String groupCollectionName,SearchObject so,ReportTask reportSubject){
		addParagraph3(seqNum+" 统计条件") ;//1.1
		String assetName = StringUtil.isBlank(so.getHost()) ? "全部" : so.getHost() ;
		AssetObject asset = AssetFacade.getInstance().getAssetByIp(so.getHost());
		if (asset != null) {
			assetName = asset.getName() ;
		}
		String filterCondition = StringUtil.isBlank(reportSubject.getSearchCondition()) ? "" : "，过滤条件："+reportSubject.getSearchCondition() ;
		addParagraph4("日志类型：" + dvcType + "，列集：" + groupCollectionName + "，设备:"+assetName + filterCondition) ;
	}
	/**
	 * 1.2 报表综述<br>
	 */
	private void createDescription(String seqNum,SearchObject so,StatSubject subject){
		addParagraph3(seqNum + " 报表综述") ;
		addParagraph4("现在展示的是"+
					  StringUtil.longDateString(subject.getStartTime()) +
					  "至" +
					  StringUtil.longDateString(subject.getEndTime()) +
					  "时间的报表数据") ;
	}
	/**
	 * 1.3 图表信息
	 * 创建图表
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	private void createChart(String seqNum,SearchObject so,JSONArray statResult,String unit,List<String> groupColumns,
							 GroupCollection groupCollection,ReportTask reportSubject){
		addParagraph3(seqNum + " " + reportSubject.getTaskName() +"统计图") ;
		XSSFWorkbook wb = new XSSFWorkbook();
		String catAxisField = reportSubject.getCategoryAxisField() ;//X轴字段名称
		if (StringUtil.isBlank(catAxisField)) {
			catAxisField = groupColumns.get(0) ;
		}
		//柱状图或者拆线图的categories
		List<String> categories = new ArrayList<String>() ;
		String seriesField = null ;
		//柱状图或者拆线图的series
		List<String> series = new ArrayList<String>() ;
		if(groupColumns.size() == 2){//包含多个series
			//如果catAxisFeild是第0个则seriesField是第一个，否则相反
			seriesField = groupColumns.indexOf(catAxisField) == 0 ? groupColumns.get(1) : groupColumns.get(0) ;
		}
		for(Object obj:statResult){
			JSONObject recordJSON = (JSONObject)obj ; 
			String catValue = recordJSON.getString(catAxisField) ;
			if(!categories.contains(catValue)){
				categories.add(catValue) ;
			}
			//如果seriesField为null则返回值也为null，这样就等于只有一个series
			String seriesValue = recordJSON.getString(seriesField) ;
			if(!series.contains(seriesValue)){
				series.add(seriesValue) ;
			}
		}
        XSSFSheet sheet = wb.createSheet("chartData"+seqNum);
        final int NUM_OF_ROWS = categories.size()+1;
        final int NUM_OF_COLUMNS = series.size()+1;
        for (int rowIndex = 0; rowIndex < NUM_OF_ROWS; rowIndex++) {
        	XSSFRow sheetRow = sheet.createRow(rowIndex) ;
        	for (int colIndex = 0; colIndex < NUM_OF_COLUMNS; colIndex++) {
        		XSSFCell cell = sheetRow.createCell(colIndex) ;
        		if(rowIndex != 0 && colIndex !=0){
        			cell.setCellType(Cell.CELL_TYPE_NUMERIC) ;
        			cell.setCellValue(0) ;//初始化默认值0
        		}else{
        			cell.setCellType(Cell.CELL_TYPE_STRING) ;
        		}
        	}
        }
        /**
         * excel数据模型
         * |            | Series1  | Series2  |
         * | category1  |    1     |    4     |
         * | category2  |    2     |    5     |
         * | category3  |    3     |    6     |
         */
        //填充图表series列数据
        XSSFRow firstRow = sheet.getRow(0);
        for(int columnIndex = 1 ;columnIndex < NUM_OF_COLUMNS;columnIndex++){
        	String seriesName = seriesField == null ? "统计结果" : translate(series.get(columnIndex-1),seriesField) ; 
        	firstRow.getCell(columnIndex).setCellValue(seriesName) ;
        }
        //填充图表categories行数据
        for(int rowIndex = 1;rowIndex < NUM_OF_ROWS;rowIndex++){
        	String catName = translate(categories.get(rowIndex-1),catAxisField) ;
        	sheet.getRow(rowIndex).getCell(0).setCellValue(catName) ;
        }
        for(Object obj:statResult){
        	JSONObject recordJSON = (JSONObject)obj ;
        	int rowIndex = categories.indexOf(recordJSON.getString(catAxisField)) ;
        	int columnIndex = series.indexOf(recordJSON.getString(seriesField)) ;
        	String totalValue = recordJSON.getString(so.getFunctionName()) ;
        	sheet.getRow(rowIndex+1).getCell(columnIndex+1).setCellValue(StringUtil.toDoubleNum(totalValue)) ;
        }
        Integer diagram = reportSubject.getDiagram() ;
        if(diagram ==  0){
        	return ;
        }
        XSSFChart chart;
		try {
			chart = doc.createChart(wb);
			ChartLegend legend  = chart.getOrCreateLegend();
	        legend.setPosition(LegendPosition.TOP_RIGHT);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
        switch(diagram){
        	case 1:{createBarChartData(reportSubject,sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);break; }
        	case 5:{createPieChartData(reportSubject,sheet, chart, NUM_OF_ROWS,NUM_OF_COLUMNS,unit);break;}
        	case 6:{createLineChartData(reportSubject,sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);break;}
        	default:{throw new RuntimeException("无效的图表类型"+diagram);}
        }
	}
	/**
	 * 创建柱状图数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param rows
	 * @param columns
	 * @return
	 */
	private static ChartData createBarChartData(ReportTask reportSubject,XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
        BarChartData data = dataFactory.createBarChartLapData();
        data.setUnit(unit) ;
        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN) ;
        fillChartData(reportSubject,sheet, chart, data, rows, columns) ;
        data.fillChart(chart, bottomAxis,leftAxis) ;
        return data ;
	}
	/**
	 * 创建拆线图数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param rows
	 * @param columns
	 * @return
	 */
	private static ChartData createLineChartData(ReportTask reportSubject,XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
        LineChartData data = dataFactory.createLineChartData();
        data.setUnit(unit) ;
        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN) ;
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        fillChartData(reportSubject,sheet, chart, data, rows, columns) ;
        data.fillChart(chart, bottomAxis,leftAxis) ;
        return data ;
	}
	/**
	 * 创建饼图数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param rows
	 * @param columns
	 * @return
	 */
	private static ChartData createPieChartData(ReportTask reportSubject,XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
		PieChartData data = dataFactory.createPieChartData();
        //data.setUnit(unit) ;
		fillChartData(reportSubject,sheet, chart, data, rows, columns) ;
		data.fillChart(chart) ;
		return data ;
	}
	/**
	 * 填充数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param data
	 * @param rows
	 * @param columns
	 */
	private static void fillChartData(ReportTask reportSubject,XSSFSheet sheet,XSSFChart chart,ChartData data,int rows,int columns){
		ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, new CellRangeAddress(1, rows-1 , 0, 0));
        data.setCategories(xs) ;
        data.setTitle(reportSubject.getTaskName()) ;
        for(int colIndex=1;colIndex < columns;colIndex++){
        	ChartDataSource<String> seriesName = DataSources.fromStringCellRange(sheet, new CellRangeAddress(0, 0, colIndex, colIndex)) ;
        	ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(1, rows-1, colIndex, colIndex));
        	data.addSeries(ys,seriesName);
        }
	}
	/**
	 * 创建表格<br>
	 * 1.4 表格信息
	 * @param so
	 * @param statResult
	 * @param groupColumns
	 * @param groupCollection
	 */
	private void createTable(String seqNum,SearchObject so,JSONArray statResult,String unit,
							 List<String> groupColumns,GroupCollection groupCollection,
							 ReportTask reportSubject){
		addParagraph3(seqNum +" " + reportSubject.getTaskName() +"统计表") ;
		XWPFTable table = doc.createTable(statResult.size()+1, groupColumns.size()+1);
		int cellWidths = 8100/(groupColumns.size()+1) ;
		int rowHeight = 500 ;//大概0.8厘米左右
		XWPFTableRow headerRow = table.getRow(0) ;
		headerRow.setHeight(rowHeight) ;
		for(int i=0;i<groupColumns.size();i++){
			String groupName = groupCollection.getField(groupColumns.get(i)).getAlias() ;
			XWPFTableCell headerCell = headerRow.getCell(i) ;
			headerCell.setText(groupName) ;
			XWPFUtil.setCellWidth(headerCell, cellWidths,"center") ;
		}
		XWPFTableCell headerResultCell = headerRow.getCell(groupColumns.size());
		headerResultCell.setText(StringUtil.isNotBlank(unit) ? "统计结果("+unit+")" :"统计结果") ;
		XWPFUtil.setCellWidth(headerResultCell, cellWidths, "center") ;
		String fun = so.getFunctionName() ;
		for(int rowIndex=0;rowIndex<statResult.size();rowIndex++){
			JSONObject record = (JSONObject)statResult.get(rowIndex) ;
			XWPFTableRow row = table.getRow(rowIndex+1) ;
			row.setHeight(rowHeight) ;
			for(int columnIndex=0;columnIndex<groupColumns.size();columnIndex++){
				XWPFTableCell cell = row.getCell(columnIndex) ; 
				String fieldName = groupColumns.get(columnIndex) ;
        		cell.setText(translate(record.getString(fieldName),fieldName)) ;
				XWPFUtil.setCellWidth(cell, cellWidths,"center") ;
			}
			XWPFTableCell resultCell = row.getCell(groupColumns.size()) ; 
			resultCell.setText(record.getString(fun)) ;
			XWPFUtil.setCellWidth(resultCell, cellWidths,"center") ;
		}
	}

	private String translate(Object value,String fieldName){
		if(DataConstants.PRIORITY.equalsIgnoreCase(fieldName)){
			return CommonUtils.getLevel(value) ;
		}
		return StringUtil.toString(value) ;
	}

	/**
	 * 添加一级标题的段落
	 * @return
	 */
	private XWPFParagraph addParagraph1(String text,ParagraphAlignment align){
		XWPFParagraph par = doc.createParagraph() ; 
		return setAttribute(par, text, true, text, 20, ParagraphAlignment.CENTER) ;
	}
	/**
	 *  添加二级标题的段落
	 * @param doc
	 * @param text
	 * @param align
	 * @return
	 */
	private XWPFParagraph addParagraph2(String text){
		XWPFParagraph par = doc.createParagraph() ; 
		return setAttribute(par, text, true, text, 14, ParagraphAlignment.LEFT) ;
	}
	/**
	 * 添加三级标题的段落
	 * @param doc
	 * @param text
	 * @param align
	 * @return
	 */
	private XWPFParagraph addParagraph3(String text){
		XWPFParagraph par = doc.createParagraph() ; 
		return setAttribute(par, text, true, text, 12, ParagraphAlignment.LEFT) ;
	}
	/**
	 * 添加正文段落
	 * @param doc
	 * @param text
	 * @param align
	 * @return
	 */
	private XWPFParagraph addParagraph4(String text){
		XWPFParagraph par = doc.createParagraph() ; 
		return setAttribute(par, text, false, text, 11, ParagraphAlignment.LEFT) ;
	}
	
	private XWPFParagraph setAttribute(XWPFParagraph par,String text,boolean bold,String textFont,int fontSize,ParagraphAlignment align){
		par.setAlignment(align) ;
		par.setSpacingLineRule(LineSpacingRule.AUTO) ;
		par.setSpacingBefore(120) ;//1磅相当于20
		par.setSpacingAfter(200) ;//1磅相当于20
		//par.setSpacingAfterLines(20) ;
		par.setAlignment(align) ;
		XWPFRun run = par.createRun() ;
		run.setText(text) ;
		run.setBold(bold) ;
		run.setFontFamily(textFont);
		run.setFontSize(fontSize) ;
		return par ;
	}
	
	/**
	 * 添加一级标题的段落
	 * @return
	 */
	private XWPFParagraph addRun1(XWPFParagraph par,String text,ParagraphAlignment align){
		
		par.setAlignment(align) ;
		XWPFRun run = par.createRun() ;
		run.setText(text) ;
		run.setBold(true) ;
		run.setFontFamily(textFont);
		run.setFontSize(24) ;
		return par ;
	}
	/**
	 *  添加二级标题的段落
	 * @param doc
	 * @param text
	 * @param align
	 * @return
	 */
	private static XWPFParagraph addRun2(XWPFParagraph par,String text,ParagraphAlignment align){
		par.setAlignment(align) ;
		XWPFRun run = par.createRun() ;
		run.setText(text) ;
		run.setBold(true) ;
		run.setFontFamily(textFont);
		run.setFontSize(12) ;
		return par ;
	}
	/**
	 * 添加三级标题的段落
	 * @param doc
	 * @param text
	 * @param align
	 * @return
	 */
	private static XWPFParagraph addRun3(XWPFParagraph par,String text,ParagraphAlignment align){
		par.setAlignment(align) ;
		XWPFRun run = par.createRun() ;
		run.setText(text) ;
		run.setBold(false) ;
		run.setFontFamily(textFont);
		run.setFontSize(11) ;
		return par ;
	}
}
