package com.topsec.tsm.sim.report.jasper;

import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.chart.highchart.HighChartImageCreator;
import com.topsec.tsm.sim.report.chart.jfreechart.JFreeChartImageCreator;
import com.topsec.tsm.sim.report.common.ReportException;
import com.topsec.tsm.sim.report.common.ReportFileCreator;
import com.topsec.tsm.sim.report.common.SubjectImageCallback;
import com.topsec.tsm.sim.report.common.SubjectModel;
import com.topsec.tsm.sim.report.common.SubjectType;
import com.topsec.tsm.sim.report.component.ChartSubject;
import com.topsec.tsm.sim.report.component.Column;
import com.topsec.tsm.sim.report.component.Grid;
import com.topsec.tsm.sim.report.component.ImageFillMode;
import com.topsec.tsm.sim.report.component.ImageSubject;
import com.topsec.tsm.sim.report.component.Row;
import com.topsec.tsm.sim.report.component.Subject;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.util.JasperClasspath;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

/**
 * 使用JasperReport生成报表文件
 * @author hp
 *
 */
public class JRReportFileCreator extends ReportFileCreator {

	public JRReportFileCreator(RptMaster report, String fileFormat,Parameter parameter) {
		super(report, fileFormat, parameter);
	}
	public JRReportFileCreator(RptMaster report, String fileFormat,Parameter parameter,OutputStream os){
		super(report, fileFormat, parameter,null) ;
	}
	@Override
	public void exportReportTo(OutputStream os)throws ReportException {
		try {
			RptMaster rpt = getReport() ;
			String layoutString = rpt.getLayout() ;
			
			boolean enablePagination = false ;//是否需要分页
			int pageHeight = 620 ;//每页高度(两行的高度+行间距+10),此高度不是word页的高度，报表根据组件的y坐标/高度值将组件分到不同的页中
			JRAbstractExporter exporter =null ;
			if("pdf".equalsIgnoreCase(fileFormat)){//pdf类型的不需要分页
				exporter = new JRPdfExporter() ;
			}else if("doc".equalsIgnoreCase(fileFormat)){//word类型需要分页，否则内容显示不完整
				enablePagination = true ;
				exporter = new JRDocxExporter();
			}else if("rtf".equalsIgnoreCase(fileFormat)){//rtf类型的也需要分页，否则内容显示不完整
				enablePagination = true ;
				exporter = new JRRtfExporter() ;
			}else if("excel".equalsIgnoreCase(fileFormat)){
				exporter = new JRXlsxExporter() ;
			}
			
			JasperClasspath.setJasperClasspath() ;
			JasperDesign reportDesign = JRXmlLoader.load(ReportUiUtil.getSysPath("CustomReport.jrxml")) ;
			JRDesignBand detail = (JRDesignBand)reportDesign.getDetailSection().getBands()[0] ;
			JasperSubjectVisitor visitor = new JasperSubjectVisitor(detail,reportDesign.getStylesMap(),enablePagination,pageHeight) ;
			int reportWidth = reportDesign.getPageWidth()-reportDesign.getLeftMargin()-reportDesign.getRightMargin() ;
			int reportHeight = reportDesign.getPageHeight() - reportDesign.getTopMargin()-reportDesign.getBottomMargin()-reportDesign.getTitle().getHeight() ;
			//导出文件生成的临时文件列表
			List<String> tempFiles = new ArrayList<String>() ;
			Subject report = createReportFromXML(layoutString, reportWidth,reportHeight,tempFiles) ;
			int heightMinus = detail.getHeight()-report.getHeight() ;//实际报表的高度与模板高度之差
			//如果导出的报表的高度大于模板中定义的报表高度,需要调整模板报表的高度,这样才能将报表整个导出
			int newReportHeight = Math.max(reportDesign.getPageHeight()-heightMinus+reportDesign.getTopMargin()+reportDesign.getBottomMargin()+reportDesign.getTitle().getHeight(),
										   reportDesign.getPageHeight()) ;
			reportDesign.setPageHeight(newReportHeight) ;
			detail.setHeight(report.getHeight()) ;
			report.accept(visitor) ;
			
			JasperReport reportPrint = JasperCompileManager.compileReport(reportDesign);
			Map<String,Object> param = getReportParamters() ;
			JasperPrint print=null;
			try {
				print = JasperFillManager.fillReport(reportPrint, param,
						new JREmptyDataSource());
			} catch (Exception e) {
			}
			
			if(exporter!=null){
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os) ;
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print) ;
				exporter.exportReport() ;
			}
			for(String file:tempFiles){
				try {
					if(file!=null){
						File f = new File(file) ;
						if(f.exists()){
							f.delete() ;
						}
					}
				} catch (Exception e) {//删除失败也要继续，防止删除文件出现异常后，程序出错
					e.printStackTrace() ;
				}
			}
		} catch (JRException e) {
			throw new ReportException("报表模板出错:"+e.getMessage()) ;
		} catch (Exception e) {
			throw new ReportException(e.getMessage()) ;
		}		
	}
	public Map<String,Object> getReportParamters(){
		Map<String,Object> param = new HashMap<String, Object>() ;
		param.put("reportTitle", report.getMstName()) ;
		param.put("exportDate", StringUtil.currentDateToString("yyyy-MM-dd HH:mm:ss")) ;//报表导出日期
		param.put("reportDateRange", getBeginDateParam().substring(0, 10)+"至"+getEndDateParam().substring(0, 10)) ;//报表日期范围
		return param ;
	}
	/**
	 * 从xml类型的数据中解析并创建各个主题
	 * xml数据结构示例
	 * <template>
	 *  <row>
	 *    <column><Subject></Subject></column>
	 *    <column><Subject></Subject></column>
	 *  </row>
	 *  <row>
	 *    <column><Subject></Subject></column>
	 *  </row>
	 * </template>
	 * @param layoutString 报表定义
	 * @param reportWidth 报表宽度
	 * @return 创建的报表
	 */
	@SuppressWarnings("unchecked")
	private Subject createReportFromXML(String layoutString,int reportWidth,int reportHeight1,List<String> tempFiles)throws Exception{
		SAXReader reader = new SAXReader() ;
		Document doc = reader.read(new StringReader(layoutString)) ;
		Element root = doc.getRootElement() ;
		List<Element> rowElements = root.elements("row") ;
		Grid report = new Grid(reportWidth,reportHeight1,10) ;
		int reportRealHeight = 0 ;//报表实际总高度
		int rowHeight = 300 ;//每行的高度
		List<Callable<String>> tasks = new ArrayList<Callable<String>>();
		for(Element rowElelment:rowElements){
			reportRealHeight += rowHeight + report.getGap();
			Row rowSubject = new Row(rowHeight,10) ;
			report.addChild(rowSubject) ;
			List<Element> columnElements = rowElelment.elements("column") ;
			int columnCount = columnElements.size() ;
			//可用的宽度(总宽度减去各列之间的间隔)
			int availableWidth = report.getWidth() - rowSubject.getGap()*(columnCount-1) ; 
			int remainder = availableWidth%columnCount ;//平均宽度余数
			int avgColumnWidth = (availableWidth-remainder)/columnCount ;//平均每列的宽度
			for(int i=0;i<columnCount;i++){
				Element columnElement = columnElements.get(i) ;
				Column columnSubject = new Column(avgColumnWidth) ;
				rowSubject.addChild(columnSubject) ;
				Element subjectElement = columnElement.element("Subject") ;
				if(SubjectType.CHART.equals(subjectElement.attributeValue("subjectType"))){
					String subjectClass = subjectElement.attributeValue("subjectClass") ;
					String onlyByDeviceType = "deviceType".equals(subjectClass)||"special".equals(subjectClass) ? "onlyByDvctype" : "" ;
					Parameter allParameter = new Parameter(parameter.getParameterMap()) ;
					allParameter.put("onlyByDvctype", onlyByDeviceType) ;
					allParameter.put("deviceType",subjectElement.attributeValue("deviceType")) ;
					allParameter.put("dvcaddress", subjectElement.attributeValue("deviceIp")) ;
					SubjectModel model = new SubjectModel(StringUtil.toInt(subjectElement.attributeValue("id")),allParameter) ;
					//自定义主题查询时间，目前禁用此功能，统一使用一个日期进行查询
					//setDateParameter(subjectElement.element("QueryStrategy"), parameter) ;
					ChartSubject chartSubject = new ChartSubject(new ImageSubject(ReportUiUtil.getSysPath("picture_error.png"),ImageFillMode.SCALE),//默认生成图片失败使用的图片
																 subjectElement.attributeValue("name")+"【"+subjectElement.attributeValue("subjectClassName")+"】",//标题
																 avgColumnWidth,rowHeight,//宽度,高度
																 ReportUiUtil.getSysPath("subjectBG.gif"),//主题标题背景
																 ChartSubject.DEFAULT_TITLE_HEIGHT) ;//主题标题高度
					columnSubject.addChild(chartSubject) ;
					HighChartImageCreator imageCreator = new HighChartImageCreator(StringUtil.toInteger(subjectElement.attributeValue("chartType")), "jpg",
			 				   avgColumnWidth, chartSubject.getHeight()-chartSubject.getTitleSubject().getHeight()-2, 
			 				   model.getChartData(), 
			 				   new SubjectImageCallback(chartSubject)) ;
/*					JFreeChartImageCreator imageCreator = 
					new JFreeChartImageCreator(StringUtil.toInteger(subjectElement.attributeValue("chartType")), "jpg",
 							 				   avgColumnWidth, chartSubject.getHeight()-chartSubject.getTitleSubject().getHeight()-2, 
 							 				   model.getChartData(), 
 							 				   new SubjectImageCallback(chartSubject)) ;
					imageCreator.setCategoryAxisLabel(model.getCategoryAxisLabel()) ;
					imageCreator.setValueAxisLabel(model.getValueAxisLabel()) ;*/
					tasks.add(imageCreator) ;
/*					tasks.add(new FusionChartImageCreator(StringUtil.toInteger(subjectElement.attributeValue("chartType")), "jpg",
							avgColumnWidth, chartSubject.getHeight()-ChartSubject.DEFAULT_TITLE_HEIGHT, 
							model.getFusionChartXML(), new SubjectImageCallback(chartSubject))) ;*/
				}
			}
		}
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(10,tasks.size())) ;
		List<Future<String>> results = executor.invokeAll(tasks) ;
		executor.shutdownNow() ;
		for(Future<String> f:results){
			tempFiles.add(f.get()) ;
		}
		report.setHeight(reportRealHeight) ;
		return report ;
	}

	/**
	 * 根据查询策略设置日期参数
	 * 如果查询策略是固定时间段，需要根据不同的时间类型来设置参数的起止时间
	 * @param queryStrategy
	 * @param parameter
	 */
	private void setDateParameter(Element queryStrategy,Parameter parameter){
		if(queryStrategy==null){
			return ;
		}
		if("fix_date".equals(queryStrategy.attributeValue("strategyName"))){
			String dateType = queryStrategy.attributeValue("dateType") ;
			Calendar now = Calendar.getInstance() ;
			Date beginDate = null ;
			Date endDate = null ;
			if(dateType.equals("week")){
				now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) ;
				beginDate = now.getTime() ;//周一
				endDate = ObjectUtils.addDays(beginDate, 6) ;//周日
			}else if(dateType.equals("month")){
				now.set(Calendar.DAY_OF_MONTH, 1) ;//每月1号
				beginDate = now.getTime() ;//每月最后一天
				endDate = ObjectUtils.getLastDay(beginDate) ;
			}else if(dateType.equals("season")){
				now.set(Calendar.DAY_OF_MONTH, 1) ;//每季度1号
				now.set(Calendar.MONTH, now.get(Calendar.MONTH)-now.get(Calendar.MONTH)%3) ;//每季度最后一天
				beginDate = now.getTime() ;
				endDate = ObjectUtils.getLastDay(ObjectUtils.addMonths(beginDate,2)) ;
			}else if(dateType.equals("year")){
				beginDate = ObjectUtils.trunc(now.getTime(), "y") ;//每年1月1号
				endDate = ObjectUtils.getLastDay(ObjectUtils.addMonths(beginDate, 11)) ;//每年12月31号
			}
			parameter.put(ReportUiConfig.Html_Field.get(2), StringUtil.dateToString(beginDate,"yyyy-MM-dd 00:00:00")) ;//开始时间参数
			parameter.put(ReportUiConfig.Html_Field.get(3), StringUtil.dateToString(endDate, "yyyy-MM-dd 23:59:59")) ;//结束时间参数
		}
	}
}
