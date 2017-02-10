package com.topsec.tsm.sim.report.bean.struct;

import java.io.Serializable;
import java.util.List;

public class ExportWordStruts implements Serializable{

	private Boolean talCategory;

	private String reportSubDetailTitle;
	
	private String reportSubDetailSubTitle;

	private String reportSubDetailDeviceIp;
	
	private String reportSubDetailDesc;
	
	private boolean reportSubDetailHaveChart;
	
	private boolean reportSubDetailHaveTable;
	
	private String reportSubDetailChartTitle;
	
	private String reportSubDetailChartId;
	
	private String reportSubDetailTableTitle;
	
	private String reportSubDetailTableKeyTitle;
	
	private String reportSubDetailTableValueTitle;
	
	private List<ExportWordTableStruts> reportSubDetailTableValueList;
	
	private String reportSubDetailCurrentTime;
	
	private Integer reportSubDetailCurrentPage;
	
	private String docPrId;
	
	private String docPrId2;
	
	private String relativeHeight;
	
	private String relativeHeight2;
	
	private List<String> reportSubDetailTableTitleName;
	
	public List<List> getReportSubDetailTableBrowseValueList() {
		return reportSubDetailTableBrowseValueList;
	}

	public void setReportSubDetailTableBrowseValueList(
			List<List> reportSubDetailTableBrowseValueList) {
		this.reportSubDetailTableBrowseValueList = reportSubDetailTableBrowseValueList;
	}

	private List<List> reportSubDetailTableBrowseValueList;

	public List<String> getReportSubDetailTableTitleName() {
		return reportSubDetailTableTitleName;
	}

	public void setReportSubDetailTableTitleName(
			List<String> reportSubDetailTableTitleName) {
		this.reportSubDetailTableTitleName = reportSubDetailTableTitleName;
	}

	public Boolean getTalCategory() {
		return talCategory;
	}

	public void setTalCategory(Boolean talCategory) {
		this.talCategory = talCategory;
	}

	public String getReportSubDetailTitle() {
		return reportSubDetailTitle;
	}

	public void setReportSubDetailTitle(String reportSubDetailTitle) {
		this.reportSubDetailTitle = reportSubDetailTitle;
	}

	public String getReportSubDetailSubTitle() {
		return reportSubDetailSubTitle;
	}

	public void setReportSubDetailSubTitle(String reportSubDetailSubTitle) {
		this.reportSubDetailSubTitle = reportSubDetailSubTitle;
	}

	public String getReportSubDetailDeviceIp() {
		return reportSubDetailDeviceIp;
	}

	public void setReportSubDetailDeviceIp(String reportSubDetailDeviceIp) {
		this.reportSubDetailDeviceIp = reportSubDetailDeviceIp;
	}

	public String getReportSubDetailDesc() {
		return reportSubDetailDesc;
	}

	public void setReportSubDetailDesc(String reportSubDetailDesc) {
		this.reportSubDetailDesc = reportSubDetailDesc;
	}

	public String getReportSubDetailChartTitle() {
		return reportSubDetailChartTitle;
	}

	public void setReportSubDetailChartTitle(String reportSubDetailChartTitle) {
		this.reportSubDetailChartTitle = reportSubDetailChartTitle;
	}

	public String getReportSubDetailChartId() {
		return reportSubDetailChartId;
	}

	public void setReportSubDetailChartId(String reportSubDetailChartId) {
		this.reportSubDetailChartId = reportSubDetailChartId;
	}

	public String getReportSubDetailTableTitle() {
		return reportSubDetailTableTitle;
	}

	public void setReportSubDetailTableTitle(String reportSubDetailTableTitle) {
		this.reportSubDetailTableTitle = reportSubDetailTableTitle;
	}

	public String getReportSubDetailTableKeyTitle() {
		return reportSubDetailTableKeyTitle;
	}

	public void setReportSubDetailTableKeyTitle(String reportSubDetailTableKeyTitle) {
		this.reportSubDetailTableKeyTitle = reportSubDetailTableKeyTitle;
	}

	public String getReportSubDetailTableValueTitle() {
		return reportSubDetailTableValueTitle;
	}

	public void setReportSubDetailTableValueTitle(
			String reportSubDetailTableValueTitle) {
		this.reportSubDetailTableValueTitle = reportSubDetailTableValueTitle;
	}

	public List<ExportWordTableStruts> getReportSubDetailTableValueList() {
		return reportSubDetailTableValueList;
	}

	public void setReportSubDetailTableValueList(
			List<ExportWordTableStruts> reportSubDetailTableValueList) {
		this.reportSubDetailTableValueList = reportSubDetailTableValueList;
	}

	public String getReportSubDetailCurrentTime() {
		return reportSubDetailCurrentTime;
	}

	public void setReportSubDetailCurrentTime(String reportSubDetailCurrentTime) {
		this.reportSubDetailCurrentTime = reportSubDetailCurrentTime;
	}

	public Integer getReportSubDetailCurrentPage() {
		return reportSubDetailCurrentPage;
	}

	public void setReportSubDetailCurrentPage(Integer reportSubDetailCurrentPage) {
		this.reportSubDetailCurrentPage = reportSubDetailCurrentPage;
	}

	public String getDocPrId() {
		return docPrId;
	}

	public void setDocPrId(String docPrId) {
		this.docPrId = docPrId;
	}

	public String getDocPrId2() {
		return docPrId2;
	}

	public void setDocPrId2(String docPrId2) {
		this.docPrId2 = docPrId2;
	}

	public String getRelativeHeight() {
		return relativeHeight;
	}

	public void setRelativeHeight(String relativeHeight) {
		this.relativeHeight = relativeHeight;
	}

	public String getRelativeHeight2() {
		return relativeHeight2;
	}

	public void setRelativeHeight2(String relativeHeight2) {
		this.relativeHeight2 = relativeHeight2;
	}

	public boolean getReportSubDetailHaveChart() {
		return reportSubDetailHaveChart;
	}

	public void setReportSubDetailHaveChart(boolean reportSubDetailHaveChart) {
		this.reportSubDetailHaveChart = reportSubDetailHaveChart;
	}
	
	public boolean isReportSubDetailHaveTable() {
		return reportSubDetailHaveTable;
	}

	public void setReportSubDetailHaveTable(boolean reportSubDetailHaveTable) {
		this.reportSubDetailHaveTable = reportSubDetailHaveTable;
	}
}
