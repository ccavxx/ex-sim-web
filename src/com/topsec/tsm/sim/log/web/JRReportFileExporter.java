package com.topsec.tsm.sim.log.web;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.components.table.DesignCell;
import net.sf.jasperreports.components.table.StandardColumn;
import net.sf.jasperreports.components.table.StandardTable;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.component.ComponentKey;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignComponentElement;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.design.JRDesignDatasetRun;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import com.topsec.tal.base.hibernate.ReportTask;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.chart.highchart.HighChartImageCreator;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.JasperClasspath;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

public class JRReportFileExporter implements ReportFileExporter {
	/**
	 * 报表标题
	 */
	private String reportTitle ;
	/**
	 * 表格列头
	 */
	private List<String> headers ;

	/**
	 * 表格数据
	 */
	private List<List<Object>> tableData ;
	/**
	 * 图表数据
	 */
	private Map<Object,Object> chartData ;
	/**
	 * 导出文件格式
	 */
	private String fileFormat ;
	/**
	 * 图表类型
	 */
	private int chartType ;
	
	/**
	 * 筛选条件
	 * 
	 */
	private String queryCondition;
	
	private ReportTask task ;
	
	public String getQueryCondition() {
		return queryCondition;
	}

	public void setQueryCondition(String queryCondition) {
		this.queryCondition = queryCondition;
	}

	@Override
	public void setReport(ReportTask task) {
		this.task = task ;
	}

	public JRReportFileExporter(String reportTitle,List<String> headers,List<List<Object>> tableData,Map<Object,Object> chartData,int chartType, String fileFormat,String queryCondition) {
		this.reportTitle = reportTitle ;
		this.headers = headers ;
		this.fileFormat = fileFormat;
		this.chartType = chartType ;
		this.chartData = chartData ;
		this.tableData = tableData ;
		this.queryCondition=queryCondition;
	}
	@Override
	public void exportReportTo(OutputStream os) {
		try {
			JRAbstractExporter exporter =null ;
			if("pdf".equalsIgnoreCase(fileFormat)){//pdf类型的不需要分页
				exporter = new JRPdfExporter() ;
			}else if("doc".equalsIgnoreCase(fileFormat)){//word类型需要分页，否则内容显示不完整
				exporter = new JRDocxExporter() ;
			}else if("rtf".equalsIgnoreCase(fileFormat)){//rtf类型的也需要分页，否则内容显示不完整
				exporter = new JRRtfExporter() ;
			}else if("excel".equalsIgnoreCase(fileFormat)){
				exporter = new JRXlsxExporter() ;
			}else if("html".equalsIgnoreCase(fileFormat)){
				exporter = new JRHtmlExporter() ;
			}
			
			JasperClasspath.setJasperClasspath() ;
			JasperDesign reportDesign = JRXmlLoader.load(ReportUiUtil.getSysPath("AfterwardReport.jrxml")) ;
			JRDesignBand detail = (JRDesignBand) reportDesign.getDetailSection().getBands()[0] ;
			String imageFile = generateImage(1280, 300) ;
			JRDesignElement imageElement = createImageElement(imageFile,reportDesign.getPageWidth()-reportDesign.getLeftMargin()*2, 150) ;
			if (imageElement != null) {
				detail.addElement(imageElement) ;
			}
			int tableHeight = imageElement==null ? detail.getHeight()-50 : detail.getHeight()-imageElement.getHeight()-50 ;
			int tableY = imageElement==null ? 10 : imageElement.getHeight() + 10 ;
			detail.addElement(createTableElement(reportDesign.getPageWidth()-reportDesign.getLeftMargin()*2,tableHeight,0,tableY)) ;
			buildDataset((JRDesignDataset)reportDesign.getDatasetMap().get("tableDataset")) ;
			buildTableColumns(detail) ;
			JasperReport reportPrint = JasperCompileManager.compileReport(reportDesign);
			Map<String,Object> param = getReportParamters() ;
			JasperPrint print = JasperFillManager.fillReport(reportPrint, param, new JREmptyDataSource());
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os) ;
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print) ;
			exporter.exportReport() ;
			if(StringUtil.isNotBlank(imageFile)){
				try {new File(imageFile).delete() ;} catch (Exception e) {}
			}
		} catch (JRException e) {
			e.printStackTrace();
		}
		
	}
	
	private Map<String,Object> getReportParamters(){
		Map<String,Object> parameters = new HashMap<String, Object>() ;
		parameters.put("reportTitle", reportTitle) ;
		parameters.put("exportDate", StringUtil.currentDateToString("yyyy-MM-dd HH:mm:ss")) ;
		parameters.put("queryCondition", queryCondition);
		parameters.put("tableDataSource",buildDataSource()) ;
		return parameters ;
	}
	
	private void buildTableColumns(JRDesignBand detail){
		JRComponentElement component = (JRComponentElement) detail.getElementByKey("table") ;
		StandardTable table = (StandardTable) component.getComponent() ;
		int avgWidth = component.getWidth()/headers.size() ;
		for(int columnIndex=0;columnIndex<headers.size();columnIndex++){
			StandardColumn column = new StandardColumn() ;
			column.setWidth(avgWidth) ;
			column.setColumnHeader(createColumnHeader(headers.get(columnIndex),avgWidth-1)) ;
			column.setDetailCell(createColumnDetail(columnIndex, avgWidth-1)) ;
			table.addColumn(column) ;
		}
	}
	/**
	 * 创建列头
	 * @param headerText
	 * @return
	 */
	private DesignCell createColumnHeader(String headerText,int width){
		DesignCell header = new DesignCell() ;
		header.setHeight(25) ;
		header.setStyleNameReference("table_CH") ;
		JRDesignStaticText text = new JRDesignStaticText() ;
		text.setText(headerText) ;
		text.setWidth(width) ;
		text.setHeight(25) ;
		header.addElement(text) ;
		return header ;
	}
	
	/**
	 * 创建列内容
	 * @param columnIndex
	 * @param width
	 * @return
	 */
	private DesignCell createColumnDetail(int columnIndex,int width){
		JRDesignExpression expression = new JRDesignExpression() ;
		expression.setText("$F{columnField"+columnIndex+"}") ;
		expression.setValueClass(String.class) ;
		
		JRDesignTextField textField = new JRDesignTextField() ;
		textField.setExpression(expression) ;
		textField.setWidth(width) ;
		textField.setHeight(25) ;

		DesignCell detailCell = new DesignCell() ;
		detailCell.setHeight(25) ;
		detailCell.addElement(textField) ;
		detailCell.setStyleNameReference("table_TH") ;
		return detailCell ;
	}
	/**
	 * 创建表格数据源
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JRDataSource buildDataSource(){
		Map<String,Object>[] dataSourceArr = new Map[tableData.size()] ;
		for(int rowIndex=0;rowIndex<tableData.size();rowIndex++){
			List<Object> rowData = tableData.get(rowIndex);
			Map<String,Object> d = new HashMap() ;
			for(int columnIndex=0;columnIndex<rowData.size();columnIndex++){
				d.put("columnField"+columnIndex, rowData.get(columnIndex)) ;
			}
			dataSourceArr[rowIndex] = d ; 
		}
		JRMapArrayDataSource dataSource = new JRMapArrayDataSource(dataSourceArr) ;
		return dataSource ;
	}
	/**
	 * 根据表格数据，创建dataset中的field字段
	 * @param dataset
	 */
	private void buildDataset(JRDesignDataset dataset){
		try {
			for(int i=0;i<headers.size();i++){
				JRDesignField field = new JRDesignField() ;
				field.setName("columnField"+i) ;
				field.setValueClass(String.class) ;
				dataset.addField(field) ;
			}
		}catch (JRException e) {
			e.printStackTrace();
		}
	}

	private String generateImage(int width,int height){
		//chartType==0表示结果是表格类型，不需要渲染图表
		if(chartData==null||chartType==0){
			return null ;
		}
		List<ChartData> datas = new ArrayList<ChartData>() ;
		for(Map.Entry entry:chartData.entrySet()){
			ChartData data = new ChartData() ;
			data.setSerise((String)entry.getKey()) ;
			data.setValue(StringUtil.toDoubleNum(String.valueOf(entry.getValue()))) ;
			datas.add(data) ;
		}
		HighChartImageCreator imageCreator = new HighChartImageCreator(chartType, "jpg", width, height,datas) ;
		String filePath = imageCreator.generateChartImage();
		return filePath ;
	}
	
	private JRDesignElement createImageElement(String filePath,int width,int height){
		if(filePath==null){
			return null ;
		}
		JRDesignImage image = new JRDesignImage(null) ;
		image.setWidth(width-5) ;
		image.setHeight(height-5) ;
		JRDesignExpression imageExpression = new JRDesignExpression() ;
		if(filePath!=null){
			filePath = filePath.replace('\\', '/') ;
			imageExpression.setText("\""+filePath+"\"") ;
			imageExpression.setValueClass(String.class) ;
			image.setExpression(imageExpression) ;
		}
		return image ;
	}
	/**
	 * 创建表格
	 * @param width
	 * @param height
	 * @return
	 */
	private JRDesignElement createTableElement(int width,int height,int x,int y){
		JRDesignComponentElement component = new JRDesignComponentElement() ;
		component.setStyleNameReference("table") ;
		component.setX(x) ;
		component.setY(y) ;
		component.setKey("table") ;
		component.setHeight(height) ;
		component.setWidth(width) ;
		ComponentKey key = new ComponentKey("http://jasperreports.sourceforge.net/jasperreports/components", "jr", "table") ;
		component.setComponentKey(key) ;
		StandardTable table = new StandardTable() ;
		component.setComponent(table) ;

		JRDesignExpression dataSourceExpression = new JRDesignExpression() ;
		dataSourceExpression.setText("$P{tableDataSource}") ;
		dataSourceExpression.setValueClass(net.sf.jasperreports.engine.JRDataSource.class) ;
		
		JRDesignDatasetRun datasetRun = new JRDesignDatasetRun() ;
		datasetRun.setDatasetName("tableDataset") ;
		datasetRun.setDataSourceExpression(dataSourceExpression) ;
		table.setDatasetRun(datasetRun) ;
		return component ;
	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public List<List<Object>> getTableData() {
		return tableData;
	}

	public void setTableData(List<List<Object>> tableData) {
		this.tableData = tableData;
	}

	public Map<Object, Object> getChartData() {
		return chartData;
	}

	public void setChartData(Map<Object, Object> chartData) {
		this.chartData = chartData;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
}
