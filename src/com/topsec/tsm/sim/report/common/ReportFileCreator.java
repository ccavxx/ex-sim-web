package com.topsec.tsm.sim.report.common;

import java.io.OutputStream;

import com.topsec.tal.base.util.Parameter;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.util.ReportUiConfig;

/**
 * 自定义报表文件导出程序
 * @author hp
 *
 */
public abstract class ReportFileCreator {

	/**
	 * 报表实例
	 */
	protected RptMaster report;
	/**
	 * 导出报表文件格式
	 */
	protected String fileFormat ;
	/**
	 * 报表输出流
	 */
	protected OutputStream outputStream ;
	/**
	 * 报表参数信息
	 */
	protected Parameter parameter ;
	public ReportFileCreator(RptMaster report, String fileFormat,Parameter parameter,OutputStream outputStream) {
		this.report = report;
		this.fileFormat = fileFormat;
		this.outputStream = outputStream;
		this.parameter = parameter ;
	}
	
	public ReportFileCreator(RptMaster report, String fileFormat,Parameter parameter) {
		this(report,fileFormat,parameter,null) ;
	}

	/**
	 * 导出报表文件到指定输出流
	 * @param os
	 */
	public abstract void exportReportTo(OutputStream os)throws ReportException ;
	/**
	 * 导出报表文件到当前实例关联的输出流
	 */
	public void exportReport(){
		exportReportTo(outputStream);
	}

	public RptMaster getReport() {
		return report;
	}

	public void setReport(RptMaster report) {
		this.report = report;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}
	/**
	 * 开始日期参数
	 * @return
	 */
	public String getBeginDateParam(){
		return parameter.getValue(ReportUiConfig.Html_Field.get(2)) ;
	}
	/**
	 * 结束日期参数
	 * @return
	 */
	public String getEndDateParam(){
		return parameter.getValue(ReportUiConfig.Html_Field.get(3)) ;
	}
}
