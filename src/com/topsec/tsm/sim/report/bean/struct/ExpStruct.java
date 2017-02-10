package com.topsec.tsm.sim.report.bean.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpStruct {
	public String mstrptid ;
	//文件类型 doc pdf excel  FileType {  PDF, HTML, XLS, XML, RTF, CSV, TXT } 
	private String fileType; 
	private String rptName;// 报表名
	private String dvc;// 设备名
	private String creTime;// 报表生成时间
	private String rptTimeS;// 生成报表区间
	private String rptTimeE;// 生成报表区间
	private String subList;// 报表列表
	private String top;  // top n
	private String rptType; //报表类型
	private String rptUser; //制作人
	private String rptIp; //报表ip
	private String rptSummarize; //报表简介
	private String pdffooter;//pdffooter

	private String reportTaskId;
	private String onlyByDvctype;
	private StringBuffer html;
	public StringBuffer getHtml() {
		return html;
	}

	public void setHtml(StringBuffer html) {
		this.html = html;
	}
	private List<Map> resultList=new ArrayList<Map>();

	private Map map;	//扩展，添加其他参数

	public ExpStruct() {
		super();
		map = new HashMap();
	}

	public String getRptSummarize() {
		return rptSummarize;
	}

	public void setRptSummarize(String rptSummarize) {
		this.rptSummarize = rptSummarize;
	}

	public String getMstrptid() {
		return mstrptid;
	}

	public void setMstrptid(String mstrptid) {
		this.mstrptid = mstrptid;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getRptName() {
		return rptName;
	}

	public void setRptName(String rptName) {
		this.rptName = rptName;
	}

	public String getDvc() {
		return dvc;
	}

	public void setDvc(String dvc) {
		this.dvc = dvc;
	}

	public String getCreTime() {
		return creTime;
	}

	public void setCreTime(String creTime) {
		this.creTime = creTime;
	}

	public String getRptTimeS() {
		return rptTimeS;
	}

	public void setRptTimeS(String rptTimeS) {
		this.rptTimeS = rptTimeS;
	}

	public String getRptTimeE() {
		return rptTimeE;
	}

	public void setRptTimeE(String rptTimeE) {
		this.rptTimeE = rptTimeE;
	}

	public String getSubList() {
		return subList;
	}

	public void setSubList(String subList) {
		this.subList = subList;
	}

	public String getTop() {
		return top;
	}

	public void setTop(String top) {
		this.top = top;
	}

	public String getRptType() {
		return rptType;
	}

	public void setRptType(String rptType) {
		this.rptType = rptType;
	}

	public String getRptUser() {
		return rptUser;
	}

	public void setRptUser(String rptUser) {
		this.rptUser = rptUser;
	}

	public String getRptIp() {
		return rptIp;
	}

	public void setRptIp(String rptIp) {
		this.rptIp = rptIp;
	}

	public String getPdffooter() {
		return pdffooter;
	}

	public void setPdffooter(String pdffooter) {
		this.pdffooter = pdffooter;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public String getOnlyByDvctype() {
		return onlyByDvctype;
	}

	public void setOnlyByDvctype(String onlyByDvctype) {
		this.onlyByDvctype = onlyByDvctype;
	}

	public List<Map> getResultList() {
		return resultList;
	}

	public void setResultList(List<Map> resultList) {
		this.resultList = resultList;
	}
	public String getReportTaskId() {
		return reportTaskId;
	}

	public void setReportTaskId(String reportTaskId) {
		this.reportTaskId = reportTaskId;
	}

	/**
	 * 根据fileType获得导出文件的扩展名
	 * @return
	 */
	public String getFileExtension(){
		if(fileType==null){
			return ".rpt" ;
		}
		if(fileType.equalsIgnoreCase("pdf")){
			return ".pdf" ;
		}else if(fileType.equalsIgnoreCase("doc")){
			return ".doc" ;
		}else if(fileType.equalsIgnoreCase("excel")){
			return ".xlsx" ;
		}else if(fileType.equalsIgnoreCase("html")){
			return ".html" ;
		}else if(fileType.equalsIgnoreCase("rtf")){
			return ".rtf" ;
		}
		return null ;
	}
}
