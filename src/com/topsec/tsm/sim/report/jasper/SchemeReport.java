//
// COPYRIGHT (C) 2009 TOPSEC CORPORATION
//
// ALL RIGHTS RESERVED BY TOPSEC CORPORATION, THIS PROGRAM
// MUST BE USED SOLELY FOR THE PURPOSE FOR WHICH IT WAS
// FURNISHED BY TOPSEC CORPORATION, NO PART OF THIS PROGRAM
// MAY BE REPRODUCED OR DISCLOSED TO OTHERS, IN ANY FORM
// WITHOUT THE PRIOR WRITTEN PERMISSION OF TOPSEC CORPORATION.
// USE OF COPYRIGHT NOTICE DOES NOT EVIDENCE PUBLICATION
// OF THE PROGRAM
//
//            TOPSEC CONFIDENTIAL AND PROPROETARY
//
////////////////////////////////////////////////////////////////////////////
package com.topsec.tsm.sim.report.jasper;
/*
 *	TopSec-Ta-l 2009
 *	系统名：Ta-L Report
 *	类一览
 *		NO	类名		概要
 *		1	SchemeReport	计划报表使用类
 *	历史:
 *		NO	日期		版本		修改人		内容				
 *		1	2009/04/30	V1.0.1		Rick		初版
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;

import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.sim.report.bean.struct.ExpDateStruct;
import com.topsec.tsm.sim.report.bean.struct.ExpMstRpt;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.exp.ExportTalReportWord2Doc;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

public class SchemeReport {
	private Logger logger = LoggerFactory.getLogger(SchemeReport.class) ;
	// 提供给发邮件的接口
	public String saveMstReport(ExpStruct exp) throws Exception {
		// 文件路径
		String reValue = null;
		
		ExpMstRpt expMstRpt=new ExpMstRpt();
		
		//生成报表所需要的数据
		LinkedHashMap mapList=expMstReport(exp);
		
		//根据数据,得到jasperPrint集合
		List jasperPrintList = expMstRpt.creMstRpt(mapList,null, exp);
		
		JRAbstractExporter exporter = ReportUiUtil.getJRExporter(exp.getFileType());
		
		//reValue = ReportUiUtil.getExpFilePath("emailPdf");
		String serverHome = System.getProperty("jboss.server.home.dir");
		reValue=serverHome+File.separatorChar+"tmp"+File.separatorChar;
		Calendar date = Calendar.getInstance();
		reValue+= "emailPdf/" + date.get(date.YEAR) + "/" + (date.get(date.MONTH)+1)+ "/" + date.get(date.DAY_OF_MONTH) + "/";
		
		if (!new File(reValue).exists()){
			new File(reValue).mkdirs();
		}
		if(exp.getFileType().equals("doc")){
			exp.setFileType("docx");
		}
		reValue += ReportUiUtil.getFileName2(exp);
		
		if(exp.getFileType().equals("doc")){//基本有问题，类可以删除
			ExportTalReportWord2Doc wordExport=new ExportTalReportWord2Doc();
			File file=new File(reValue);
			FileOutputStream fos=new FileOutputStream(file);
			OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fos,"UTF-8");
			
			wordExport.exportToFile(exp, mapList, jasperPrintList, outputStreamWriter);
		}else{
			exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST,jasperPrintList);
			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, reValue);
			if (exp.getFileType().equals("excel")) {
				exporter.setParameter(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET,Boolean.TRUE);
			}
			exporter.exportReport();
		}
		String outName=null;
		try {
			outName=zipFile(reValue);
		} finally {
			clearTmpImage(expMstRpt);
		}
		return outName;
	}
	private void clearTmpImage(ExpMstRpt expMst){
		if(expMst != null && expMst.reportImages != null){
			for(Map.Entry<ExpDateStruct, String> entry:expMst.reportImages.entrySet()){
				String imageFileName = entry.getValue() ;
				try {
					if (imageFileName != null) {
						File f = new File(imageFileName) ;
						if(f.exists()){
							FileUtils.forceDelete(f) ;
						}
					}
				} catch (Exception e) {
					logger.error("删除文件"+imageFileName+"失败",e);
				}
			}
		}
	}

	public RptMasterTbService rptMasterTbImp;

	public RptMasterTbService getRptMasterTbImp() {
		return rptMasterTbImp;
	}

	public void setRptMasterTbImp(RptMasterTbService rptMasterTbImp) {
		this.rptMasterTbImp = rptMasterTbImp;
	}

	// 导出主报表
	public LinkedHashMap expMstReport(ExpStruct exp) throws Exception {
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean) ;
		return ReportModel.expMstReport(rptMasterTbImp,exp, null);
	}

	public String zipFile(String fileName) throws IOException{
		
	    FileInputStream file = new FileInputStream(fileName);//从硬盘c找到xx.txt文件，作为文件流
	    //创建文件读入类
	    String outName = fileName.substring(0, fileName.lastIndexOf('.'))+".zip";
	    BufferedInputStream in = new BufferedInputStream(file);
	    FileOutputStream outFile = new FileOutputStream(outName);//将文件xx.zip文件写的c盘
	    ZipOutputStream outGZIP = new ZipOutputStream(outFile);//压缩文件
	    String zipEntryFileName=fileName.substring(fileName.lastIndexOf('/')+1,fileName.length());
	   // zipEntryFileName=java.net.URLEncoder.encode(zipEntryFileName,"UTF-8");
	    
	    //ZipEntry ze = new ZipEntry(zipEntryFileName);
	    org.apache.tools.zip.ZipEntry ze=new org.apache.tools.zip.ZipEntry(zipEntryFileName);
	    outGZIP.putNextEntry(ze);
	    outGZIP.setEncoding("UTF-8");
	    
	    //应用GZIPOutputStream文件压缩写出类创建文件写出类
	    BufferedOutputStream out = new BufferedOutputStream(outGZIP);
	    int c;
	    //读入数据
	    while((c = in.read()) != -1){
	     //写出并且压缩数据
	     out.write(c);
	    }
	    //保存写出数据
	    out.flush();
	    try{
	    	
	    }finally{
	    	if(out!=null){
	    		try {
	    			 out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    	if(in!=null){
	    		try {
	    			in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    }
	  
	    //System.out.println("创建成功F:\\xx.zip");		
		return outName;
	}
}
