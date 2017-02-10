/**
 * 版权声明北京天融信科技有限公司，版权所有违者必究
 *
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
 * @since  2011-09-16
 * @version 1.0
 * 
 */
package com.topsec.tsm.sim.report.exp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.base.JRBasePrintPage;
import net.sf.jasperreports.engine.fill.JRTemplatePrintText;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Encoder;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.bean.struct.ExpDateStruct;
import com.topsec.tsm.sim.report.bean.struct.ExpMstRpt;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.bean.struct.ExportWordImageStruts;
import com.topsec.tsm.sim.report.bean.struct.ExportWordStruts;
import com.topsec.tsm.sim.report.bean.struct.ExportWordTableStruts;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

/**
 * 功能描述: 报表导出word
 */
public class ExportTalReportWord2Doc {

	private static final Logger log = LoggerFactory
			.getLogger(ExportTalReportWord2Doc.class);

	public static final String VELOCITY_CONFIG = "resource/report/velocity.properties";
	public static final String WORD_TEMPLATE = "resource/report/exportWordTemplate.vm";
	public static final String WORDBROWSE_TEMPLATE = "resource/report/exportBrowseWordTemplate.vm";
	public ExportTalReportWord2Doc() {

	}

	public void export(ExpStruct exp,LinkedHashMap<String, List> expmap,List<JasperPrint> jasperPrintList,PrintWriter writer) {
		try {
			 
			VelocityContext context = new VelocityContext();
			boolean isResourceExists=Velocity.resourceExists(WORD_TEMPLATE);
			Template template =null;
			if (isResourceExists) {
				template=Velocity.getTemplate(WORD_TEMPLATE,"UTF-8");
			}
			handleData(exp,expmap,jasperPrintList,context);
			 
			if (template != null) {
				template.merge(context, writer);
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			
		}
	}
	
	public void exportToFile(ExpStruct exp,LinkedHashMap<String, List> expmap,List<JasperPrint> jasperPrintList,OutputStreamWriter writer) {
		try {
			VelocityContext context = new VelocityContext();
			boolean isResourceExists=Velocity.resourceExists(ReportUiUtil.getSysPath()+WORD_TEMPLATE);
			Template template =null;
			if (isResourceExists) {
				template=Velocity.getTemplate(WORD_TEMPLATE,"UTF-8");
			}
			handleData(exp,expmap,jasperPrintList,context);
			 
			if (template != null) {
				template.merge(context, writer);
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			
		}
	}
	
	
	/*
	* 变量:
	源IP流量报表                                    ${reportMainTitle}
	日                                                             ${reportMainDateType}
	administrator:           ${reportMainAuthor}
	2011-09-16 11:06:05      ${reportMainCreateTime}
	2011-09-16 00:00:00      ${reportMainStartTime}
	2011-09-16 23:59:59      ${reportMainEndTime}
	1、主报表简介：                             ${reportMainDetail}
	1.1                      ${reportMainDetailFirstNum}
	源IP流量报表，包含防火墙源IP流入流量排行...:            ${reportMainDetailFirstContent} 
	1.2                      ${reportMainDetailSecondNum} 
	源IP流量报表，向您综合展示自2011-09-16...:              ${reportMainDetailSecondContent}
	1.3                      ${reportMainDetailThirdNum}
	aaaaaaaaaaaaa:           ${reportMainDetailThirdContent}
	
	---------------------
	                        ${subReports}
	判断是否是下钻报表:	    ${subReport.talCategory}
	---------------------------
	
	2 防火墙                                                                  ${subReport.reportSubDetailTitle}
	2.1 防火墙源IP流入流量排行                    ${subReport.reportSubDetailSubTitle}
	2.1.1 报表设备：192.168.72.229   ${subReport.reportSubDetailDeviceIp}
	报表综述：防火墙源IP流入流量排行，向您                   ${subReport.reportSubDetailDesc}
	
	                                           ${subReport.reportSubDetailHaveChart}
	2.1.3 防火墙源IP流入流量排行统计图：                        ${subReport.reportSubDetailChartTitle}
	图的id引用			 	                   ${subReport.reportSubDetailChartId}
	
	
	2.1.4 防火墙源IP流入流量排行统计表：                         ${subReport.reportSubDetailTableTitle}
	源IP                                        ${subReport.reportSubDetailTableKeyTitle}					
	流入量(KB)                                  ${subReport.reportSubDetailTableValueTitle}
	
	
												${subReport.docPrId}
												${subReport.docPrId2}
												${subReport.relativeHeight}
												${subReport.relativeHeight2}
	
	
	values                                      ${subReport.reportSubDetailTableValueList}
	value                                       ${reportSubValueMap}
	
	192.168.72.108                              ${reportSubValueMap.key}
	2,645.38                                    ${reportSubValueMap.value}
	
	页脚图片 rId8                                ${pageFooterImage.id}
	页脚名字  media/image2.jpeg                   ${pageFooterImage.name}
	pkg:contentType="image/jpeg"                ${pageFooterImage.contentType}
	内容 							            ${pageFooterImage.contentBase64Code}
	
												 ${jfreeCharts}
												 ${jfreeChart}
	Id="rId7"                                    ${jfreeChart.id}
	Target="media/image1.png"					 ${jfreeChart.name}
	pkg:contentType="image/png"					 ${jfreeChart.contentType}
	内容                                                  						 ${jfreeChart.contentBase64Code}
	是否有图片									 ${jfreeChart.reportImageHasChart}
												
	*/
	
	private void handleData(ExpStruct exp,LinkedHashMap<String, List> expmap,List<JasperPrint> jasperPrintList,VelocityContext context){
		setReportCoverData(exp, context);
		setReportDirectoryData(jasperPrintList, context);
		setSubReportData(expmap, jasperPrintList, context);
	}
	
	private void setReportCoverData(ExpStruct exp,VelocityContext context){
		String reportMainTitle = exp.getRptName();
		context.put("reportMainTitle", reportMainTitle);
		
		String reportMainDateType = exp.getRptType();
		context.put("reportMainDateType", reportMainDateType);
		
		String reportMainAuthor = exp.getRptUser();
		context.put("reportMainAuthor", reportMainAuthor);
		 
		Calendar currentTimeCalendar=GregorianCalendar.getInstance();
		String reportMainCreateTime = StringUtil.dateToString(currentTimeCalendar.getTime(), ReportUiConfig.dFormat1);
		context.put("reportMainCreateTime", reportMainCreateTime);
		
		String reportMainStartTime = exp.getRptTimeS();
		context.put("reportMainStartTime", reportMainStartTime);
		
		String reportMainEndTime = exp.getRptTimeE();
		context.put("reportMainEndTime", reportMainEndTime);
	}
	
	private void setReportDirectoryData(List<JasperPrint> jasperPrintList,VelocityContext context){
		
		if(jasperPrintList.size()<2){
			return;
		}
		JasperPrint jasperPrint = jasperPrintList.get(1);
		List pages = jasperPrint.getPages();
		if(pages!=null&&pages.size()>0){
			
			if(pages.size()==1){
				 JRBasePrintPage jRBasePrintPage = (JRBasePrintPage)pages.get(0);
				 int i=0;
				 for (Iterator iterator = jRBasePrintPage.getElements().iterator(); iterator.hasNext();i++) {
					 Object o = iterator.next();
					 if(o instanceof JRTemplatePrintText){
						 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
						 String text=jRTemplatePrintText.getText();
						 
						 if(i==1){
							 context.put("reportMainDetail", text);
						 }else if(i==2){
							 context.put("reportMainDetailFirstNum", text);
						 }else if(i==3){
							 context.put("reportMainDetailFirstContent", text);
						 }else if(i==4){
							 context.put("reportMainDetailSecondContent", text);
						 }else if(i==5){
							 context.put("reportMainDetailSecondNum", text);
						 }else if(i==6){
							 StringBuilder sb=new StringBuilder();
							 String[] ss = text.split("\\n");
							 for (String s : ss) {
								sb.append("<w:p w:rsidR=\"007B6484\" w:rsidRDefault=\"00787E8C\" w:rsidP=\"00DF0876\"><w:pPr><w:pStyle w:val=\"BOLB1\"/></w:pPr><w:r><w:t>"); 
								sb.append(s); 
								sb.append("</w:t></w:r></w:p>"); 
							 } 
							 context.put("reportMainDetailThirdContent", sb.toString());
						 }else if(i==7){
							 context.put("reportMainDetailThirdNum", text);
						 } 
					 }
				}
			}else{
				int index=0;
				
				StringBuffer reportMainDetail=new StringBuffer();
				StringBuffer reportMainDetailFirstNum=new StringBuffer();
				StringBuffer reportMainDetailFirstContent=new StringBuffer();
				StringBuffer reportMainDetailSecondContent=new StringBuffer();
				StringBuffer reportMainDetailSecondNum=new StringBuffer();
				StringBuffer reportMainDetailThirdContent=new StringBuffer();
				StringBuffer reportMainDetailThirdNum=new StringBuffer();
				
				int level=0;
				int i=0;
				
				for (Object p : pages) {
					 JRBasePrintPage jRBasePrintPage =(JRBasePrintPage)p;
					 
					 if(index==0){
						 if(jRBasePrintPage.getElements().size()==4){
							 i=0;
							 for (Iterator iterator = jRBasePrintPage.getElements().iterator(); iterator.hasNext();i++) {
								 Object o = iterator.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(i==1){
										 reportMainDetail.append(text);
									 }else if(i==2){
										 reportMainDetailFirstNum.append(text);
									 }else if(i==3){
										 reportMainDetailFirstContent.append(text);
									 }
								 }
							}
						 }
						 level++;
					 }else{
						 if(jRBasePrintPage.getElements().size()!=2){
							 if(jRBasePrintPage.getElements().size()==4){
								 i=0;
								 for (Iterator iterator = jRBasePrintPage.getElements().iterator(); iterator.hasNext();i++) {
									 Object o = iterator.next();
									 if(o instanceof JRTemplatePrintText){
										 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
										 String text=jRTemplatePrintText.getText();
										 if(i==1){
											 if(level==1){
												 reportMainDetailFirstContent.append(text);
											 }else if(level==2){
												 reportMainDetailSecondContent.append(text);
											 }else if(level==3){
												 reportMainDetailThirdContent.append(text);
											 }
										 }else if(i==2){
											 if(level==1){
												 reportMainDetailSecondContent.append(text);
											 }else if(level==2){
												 reportMainDetailThirdContent.append(text);
											 }
										 }else if(i==3){
											 if(level==1){
												 reportMainDetailSecondNum.append(text); 
											 }else if(level==2){
												 reportMainDetailThirdNum.append(text);
											 }
										 }
									 }
								}
							 }else if(jRBasePrintPage.getElements().size()==3){
								 i=0;
								 for (Iterator iterator = jRBasePrintPage.getElements().iterator(); iterator.hasNext();i++) {
									 Object o = iterator.next();
									 if(o instanceof JRTemplatePrintText){
										 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
										 String text=jRTemplatePrintText.getText();
										 if(i==1){
											 if(level==1){
												 reportMainDetailSecondContent.append(text);
											 }else if(level==2){
												 reportMainDetailThirdContent.append(text);
											 }
										 }else if(i==2){
											 if(level==1){
												 reportMainDetailSecondNum.append(text); 
											 }else if(level==2){
												 reportMainDetailThirdNum.append(text);
											 }
										 }
									 }
								}
							 }
							 level++;
						 }else if(jRBasePrintPage.getElements().size()==2){
							 i=0;
							 for (Iterator iterator = jRBasePrintPage.getElements().iterator(); iterator.hasNext();i++) {
								 Object o = iterator.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(i==1){
										 if(level==1){
											 reportMainDetailFirstContent.append(text);
										 }else if(level==2){
											 reportMainDetailSecondContent.append(text);
										 }else if(level==3){
											 reportMainDetailThirdContent.append(text);
										 }
									 } 
								 }
							}
						 }
					 }
					 index++;
				}
				
			 context.put("reportMainDetail", reportMainDetail.toString());
			 context.put("reportMainDetailFirstNum", "1.1");
			 context.put("reportMainDetailFirstContent", reportMainDetailFirstContent.toString());
			 
			 context.put("reportMainDetailSecondNum", "1.2");
			 context.put("reportMainDetailSecondContent", reportMainDetailSecondContent.toString());
			 
			 context.put("reportMainDetailThirdNum", "1.3");
			 StringBuilder sb=new StringBuilder();
			 String[] ss = reportMainDetailThirdContent.toString().split("\\n");
			 for (String s : ss) {
				sb.append("<w:p w:rsidR=\"007B6484\" w:rsidRDefault=\"00787E8C\" w:rsidP=\"00DF0876\"><w:pPr><w:pStyle w:val=\"BOLB1\"/></w:pPr><w:r><w:t>"); 
				sb.append(s); 
				sb.append("</w:t></w:r></w:p>"); 
			 } 
			 context.put("reportMainDetailThirdContent", sb.toString());
			} 
		}
	}
	
	private void setSubReportData(LinkedHashMap<String, List> expmap,List<JasperPrint> jasperPrintList,VelocityContext context){
			
		if(expmap.size()<=0){
			return;
		}
		
		ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
		String serverHome = System.getProperty("jboss.server.home.dir");
		
		List<ExportWordStruts> subReports=new ArrayList<ExportWordStruts>();
		List<ExportWordImageStruts> jfreeCharts=new ArrayList<ExportWordImageStruts>();
		ExportWordImageStruts pageFooterImage=new ExportWordImageStruts();
		
		//计算变量
		int i=0;
		int j=i+2;
		int k=i+1;
		long relativeHeight=251656192L;
		
		Set<String> keySet = expmap.keySet(); 
		for (String key : keySet) {
			List list = expmap.get(key);
			int n=0;
			for (Iterator iterator = list.iterator(); iterator.hasNext();i++,j++,k+=2,relativeHeight+=2408L) {
				
				ExpDateStruct expDateStruts = (ExpDateStruct) iterator.next();
				JasperPrint jasperPrint = jasperPrintList.get(j);
				ExportWordStruts exportWordStruts=new ExportWordStruts();
				ExportWordImageStruts exportWordImageStruts=new ExportWordImageStruts();
				List<ExportWordTableStruts> exportWordTableStrutses=new ArrayList<ExportWordTableStruts>();
				
				List pages = jasperPrint.getPages();
				JRBasePrintPage jRBasePrintPage = (JRBasePrintPage)pages.get(0);
				
				String[] talCategory = expDateStruts.getTalCategory();
				int m=0;
				int pagesLen=jRBasePrintPage.getElements().size();
				
				//生成图片
				String imageId=generateImage(expDateStruts, serverHome, info, exportWordImageStruts, jfreeCharts,i);
				if(imageId==null){
					exportWordStruts.setReportSubDetailHaveChart(false);
				}else{
					exportWordStruts.setReportSubDetailHaveChart(true);
				}
				
				//生成表格
				if(expDateStruts.getSubTable() ==  null){
					exportWordStruts.setReportSubDetailHaveTable(false);
				}else{
					exportWordStruts.setReportSubDetailHaveTable(true);
					generateTable(expDateStruts, exportWordStruts, exportWordTableStrutses);
				}
				 
				if(talCategory==null||talCategory.length==0||talCategory[0]==null||talCategory[0].equals("")||talCategory[0].equals("null")){
					exportWordStruts.setTalCategory(false);
					
					if(n==0){
						
						if(exportWordStruts.getReportSubDetailHaveChart()){
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==6){
										 exportWordStruts.setReportSubDetailChartTitle(text);
									 }else if(m==7){
										//reportSubDetailChartId
										 //TODO  chart
									 }else if(m==8){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==9){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==10){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=11&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 } 
								 }
							}
						}else{
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==6){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==7){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==8){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=9&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 } 
								 }
							}
						}
						n=1;
					}else{
						if(exportWordStruts.getReportSubDetailHaveChart()){
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 //exportWordStruts.setReportSubDetailTitle("");
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailChartTitle(text);
									 }else if(m==6){
										//reportSubDetailChartId
										 //TODO  chart
									 }else if(m==7){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==8){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==9){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=10&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								 }
							}
						}else{
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 //exportWordStruts.setReportSubDetailTitle("");
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==6){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==7){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=8&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								 }
							}
						}
					}
					//子报表
				}else{
					//跟踪报表
					//子报表
					short talCategoryLevel = expDateStruts.getTalCategoryLevel();
					if(talCategoryLevel==2){
						exportWordStruts.setTalCategory(true);
						if(exportWordStruts.getReportSubDetailHaveChart()){
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailChartTitle(text);
									 }else if(m==3){
										//reportSubDetailChartId
										 //TODO  chart
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==5){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==6){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=7&&m<pagesLen-4){
										 //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								}
							}
						}else{
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==3){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==4){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=5&&m<pagesLen-4){
										 //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								}
							}
						}
					}else if(talCategoryLevel==1){
						//下钻报表为主要导出报表, 而不是导出上级报表里包括的下钻报表. 
						//(基本报表下钻后,再导出word情况,和自定义报表页面的下钻报表导出word情况)
						exportWordStruts.setTalCategory(false);
						
						int mstType = expDateStruts.getMstType();
						if(mstType==2){
							//自定义报表第一页时的跟踪报表
							if(exportWordStruts.getReportSubDetailHaveChart()){
								for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
									Object o = (Object) iterator2.next();
									if(o instanceof JRTemplatePrintText){
										 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
										 String text=jRTemplatePrintText.getText();
										 if(m==2){
											 //exportWordStruts.setReportSubDetailTitle("");
											 exportWordStruts.setReportSubDetailSubTitle(text);
										 }else if(m==3){
											 exportWordStruts.setReportSubDetailDeviceIp(text);
										 }else if(m==4){
											 exportWordStruts.setReportSubDetailDesc(text);
										 }else if(m==5){
											 exportWordStruts.setReportSubDetailChartTitle(text);
										 }else if(m==6){
											//reportSubDetailChartId
											 //TODO  chart
										 }else if(m==7){
											 exportWordStruts.setReportSubDetailTableTitle(text);
										 }else if(m==8){
											 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
										 }else if(m==9){
											 //exportWordStruts.setReportSubDetailTableValueTitle(text);
										 }else if(m>=10&&m<pagesLen-4){
											  //表数据
										 }else if(m==pagesLen-4){
											 
										 }else if(m==pagesLen-3){
											 //exportWordStruts.setReportSubDetailCurrentTime(text);
										 }else if(m==pagesLen-2){
											  
										 }else if(m==pagesLen-1){
											 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
										 }
									}
								}
							}else{
								for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
									Object o = (Object) iterator2.next();
									if(o instanceof JRTemplatePrintText){
										 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
										 String text=jRTemplatePrintText.getText();
										 if(m==2){
											 //exportWordStruts.setReportSubDetailTitle("");
											 exportWordStruts.setReportSubDetailSubTitle(text);
										 }else if(m==3){
											 exportWordStruts.setReportSubDetailDeviceIp(text);
										 }else if(m==4){
											 exportWordStruts.setReportSubDetailDesc(text);
										 }else if(m==5){
											 exportWordStruts.setReportSubDetailTableTitle(text);
										 }else if(m==6){
											 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
										 }else if(m==7){
											 //exportWordStruts.setReportSubDetailTableValueTitle(text);
										 }else if(m>=8&&m<pagesLen-4){
											  //表数据
										 }else if(m==pagesLen-4){
											 
										 }else if(m==pagesLen-3){
											 //exportWordStruts.setReportSubDetailCurrentTime(text);
										 }else if(m==pagesLen-2){
											  
										 }else if(m==pagesLen-1){
											 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
										 }
									}
								}
							}
						}else if(mstType==3){
							//基本报表的下钻报表为主要导出页, 自定义报表的下钻报表为主要导出页
							if(n==0){
								if(exportWordStruts.getReportSubDetailHaveChart()){
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 exportWordStruts.setReportSubDetailTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==6){
												 exportWordStruts.setReportSubDetailChartTitle(text);
											 }else if(m==7){
												//reportSubDetailChartId
												 //TODO  chart
											 }else if(m==8){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==9){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==10){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=11&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 } 
										}
									}
								}else{
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 exportWordStruts.setReportSubDetailTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==6){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==7){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==8){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=9&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 } 
										}
									}
								}
								n=1;
							}else{
								if(exportWordStruts.getReportSubDetailHaveChart()){
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										 if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 //exportWordStruts.setReportSubDetailTitle("");
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailChartTitle(text);
											 }else if(m==6){
												//reportSubDetailChartId
												 //TODO  chart
											 }else if(m==7){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==8){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==9){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=10&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 }
										 }
									}
								}else{
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										 if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 //exportWordStruts.setReportSubDetailTitle("");
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==6){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==7){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=8&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 }
										 }
									}
								}
							}
						}
					}
				}
				
				fillNullValue(exportWordStruts);
				
				if(imageId!=null){
					exportWordStruts.setReportSubDetailChartId(imageId);
				}
				exportWordStruts.setReportSubDetailTableValueList(exportWordTableStrutses);
				
				exportWordStruts.setDocPrId(Integer.toString(k));
				exportWordStruts.setDocPrId2(Integer.toString(k+1));
				
				exportWordStruts.setRelativeHeight(Long.toString(relativeHeight));
				exportWordStruts.setRelativeHeight2(Long.toString(relativeHeight+1024));
				
				subReports.add(exportWordStruts);
			}
		}
		
		context.put("subReports", subReports);
		context.put("jfreeCharts", jfreeCharts);
		
		generateFooterImage(pageFooterImage, context);
	}
	
	
	private String generateImage(ExpDateStruct expDateStruct,String serverHome,ChartRenderingInfo info,ExportWordImageStruts exportWordImageStruts,List<ExportWordImageStruts> jfreeCharts,int index){
		//图
		JFreeChart jFreeChartParam = (JFreeChart)expDateStruct.getSubChart();
		if(jFreeChartParam==null){
			exportWordImageStruts.setReportImageHasChart(false);
			jfreeCharts.add(exportWordImageStruts);
			return null;
		}else{
			exportWordImageStruts.setReportImageHasChart(true);
			
			Calendar currentTimeCalendar=GregorianCalendar.getInstance();
			String reportMainCreateTime = StringUtil.dateToString(currentTimeCalendar.getTime(), "yyyyMMddHHmmss");
			//String fileName=reportMainCreateTime+new Random().nextInt(10000)+".png";
			String fileName=reportMainCreateTime+new Random().nextInt(10000)+".jpg";
			File file=new File(serverHome+File.separatorChar+"tmp"+File.separatorChar+fileName);
			try {
				// 不要修改宽高大小
				int width=640;
				int height=303;
				//ChartUtilities.saveChartAsPNG(file, jFreeChartParam, width, height, info);
				ChartUtilities.saveChartAsJPEG(file, 1.0f, jFreeChartParam, width, height, info);
			} catch (IOException e) {
				log.error(e.getMessage());
//				e.printStackTrace();
			} 
			
			
			String contentBase64Code = toBASE64codeFile(file);
			exportWordImageStruts.setId("rId000"+(index+1));
			exportWordImageStruts.setName(fileName);
			int lastPointIndex=StringUtils.lastIndexOf(fileName, ".")+1; 
			String ext=StringUtils.substring(fileName, lastPointIndex, fileName.length());
			if(ext!=null){
				if(ext.equalsIgnoreCase("JPG")||ext.equalsIgnoreCase("JPEG")){
					exportWordImageStruts.setContentType("image/jpeg");
				}else if(ext.equalsIgnoreCase("PNG")){
					exportWordImageStruts.setContentType("image/png");
				}else if(ext.equalsIgnoreCase("BMP")){
					exportWordImageStruts.setContentType("image/bmp");
				}
			}
			exportWordImageStruts.setContentBase64Code(contentBase64Code);
			jfreeCharts.add(exportWordImageStruts);
			return exportWordImageStruts.getId();
		}
	}
	
	private void generateTable(ExpDateStruct expDateStruct,ExportWordStruts exportWordStruts,List<ExportWordTableStruts> exportWordTableStrutses){
		String subTitleLable = expDateStruct.getSubTitleLable();
		String[] subTitleLableArray = StringUtils.split(subTitleLable, ",");
		exportWordStruts.setReportSubDetailTableKeyTitle(subTitleLableArray[0]); 
		exportWordStruts.setReportSubDetailTableValueTitle(subTitleLableArray[1]);
		
		List subTable = expDateStruct.getSubTable();
		if(subTable!=null&&subTable.size()>0){
			String subTableFile = expDateStruct.getSubTableFile();
			String[] subTableFileArray = StringUtils.split(subTableFile, ",");
			for (Object object : subTable) {
				Map map=(Map)object;
				String keyColumn = (String) map.get(subTableFileArray[0]);  
				String valueColumn = (String) map.get(subTableFileArray[1]);  
				
				ExportWordTableStruts exportWordTableStruts=new ExportWordTableStruts();
				exportWordTableStruts.setKey(keyColumn);
				exportWordTableStruts.setValue(valueColumn);
				
				exportWordTableStrutses.add(exportWordTableStruts);
			}
		}
	}
	
	private void generateFooterImage(ExportWordImageStruts pageFooterImage,VelocityContext context){
		String footerImage = ExpMstRpt.getHead();
		File footerImageFile=new File(footerImage);
		pageFooterImage.setId("rId0000");
		pageFooterImage.setName(footerImageFile.getName());
		
		int lastPointIndex=StringUtils.lastIndexOf(footerImage, ".")+1; 
		String ext=StringUtils.substring(footerImage, lastPointIndex, footerImage.length());
		if(ext!=null){
			if(ext.equalsIgnoreCase("JPG")||ext.equalsIgnoreCase("JPEG")){
				pageFooterImage.setContentType("image/jpeg");
			}else if(ext.equalsIgnoreCase("PNG")){
				pageFooterImage.setContentType("image/png");
			}else if(ext.equalsIgnoreCase("BMP")){
				pageFooterImage.setContentType("image/bmp");
			}
		}
		String base64codeFile = toBASE64codeFile(footerImageFile);
		pageFooterImage.setContentBase64Code(base64codeFile);
		
		context.put("pageFooterImage", pageFooterImage);
	}

	private String toBASE64codeFile(File file) {
		FileInputStream fis=null;
		
		try {
			fis = new FileInputStream(file);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(b);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	private void fillNullValue(ExportWordStruts exportWordStruts){
		if(exportWordStruts.getReportSubDetailTitle()==null){
			exportWordStruts.setReportSubDetailTitle("");
		}
		if(exportWordStruts.getReportSubDetailSubTitle()==null){
			exportWordStruts.setReportSubDetailSubTitle("");
		}
		if(exportWordStruts.getReportSubDetailDeviceIp()==null){
			exportWordStruts.setReportSubDetailDeviceIp("");
		}
		if(exportWordStruts.getReportSubDetailDesc()==null){
			exportWordStruts.setReportSubDetailDesc("");
		}
		if(exportWordStruts.getReportSubDetailChartTitle()==null){
			exportWordStruts.setReportSubDetailChartTitle("");
		}
		if(exportWordStruts.getReportSubDetailTableTitle()==null){
			exportWordStruts.setReportSubDetailTableTitle("");
		}
	}
	
	public void exportBrowse(ExpStruct exp,ExpDateStruct expDateStruct,List<JasperPrint> jasperPrintList,PrintWriter writer) {
		try {
			 
			VelocityContext context = new VelocityContext();
			Template template = Velocity.getTemplate(WORDBROWSE_TEMPLATE,"UTF-8");
			exp.setRptType("报表查询");
			handleDataDataBrowse(exp,expDateStruct,jasperPrintList,context);
			 
			if (template != null) {
				template.merge(context, writer);
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			
		}
	}
	
	private void handleDataDataBrowse(ExpStruct exp,ExpDateStruct expdatestruts,List<JasperPrint> jasperPrintList,VelocityContext context){
		setReportCoverData(exp, context);
		setSubReportDataBrowse(expdatestruts, jasperPrintList, context);
	}
	private void generateTableBrowse(ExpDateStruct expDateStruts,ExportWordStruts exportWordStruts,List<ExportWordTableStruts> exportWordTableStrutses){
		String subTitleLable = expDateStruts.getSubTitleLable();
		String[] subTitleLableArray = StringUtils.split(subTitleLable, ",");
		List browsetablename =new ArrayList();
		for (String titlename : subTitleLableArray){
			browsetablename.add(titlename);			
		}
		exportWordStruts.setReportSubDetailTableTitleName(browsetablename);
		List tablevaluelist =new ArrayList();


		List subTable = expDateStruts.getSubTable();
		if(subTable!=null&&subTable.size()>0){
			String subTableFile = expDateStruts.getSubTableFile();
			String[] subTableFileArray = StringUtils.split(subTableFile, ",");
			for (Object object : subTable) {
				Map map=(Map)object;
				List tablevalue =new ArrayList();
				for (String titlenameE : subTableFileArray){
					String keyColumn=(String) map.get(titlenameE);  
					tablevalue.add(keyColumn);
				}
				tablevaluelist.add(tablevalue);
			}
			exportWordStruts.setReportSubDetailTableBrowseValueList(tablevaluelist);
		}
	}

	private void setSubReportDataBrowse(ExpDateStruct expDateStruct,List<JasperPrint> jasperPrintList,VelocityContext context){
		

		ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
		String serverHome = System.getProperty("jboss.server.home.dir");
		
		List<ExportWordStruts> subReports=new ArrayList<ExportWordStruts>();
		List<ExportWordImageStruts> jfreeCharts=new ArrayList<ExportWordImageStruts>();
		ExportWordImageStruts pageFooterImage=new ExportWordImageStruts();
		
		//计算变量
		int i=0;
		int j=i+2;
		int k=i+1;
		long relativeHeight=251656192L;
		
		        int n=0;
				
				JasperPrint jasperPrint = jasperPrintList.get(1);
				ExportWordStruts exportWordStruts=new ExportWordStruts();
				ExportWordImageStruts exportWordImageStruts=new ExportWordImageStruts();
				List<ExportWordTableStruts> exportWordTableStrutses=new ArrayList<ExportWordTableStruts>();
				
				List pages = jasperPrint.getPages();
				JRBasePrintPage jRBasePrintPage = (JRBasePrintPage)pages.get(0);
				
				String[] talCategory = expDateStruct.getTalCategory();
				int m=0;
				int pagesLen=jRBasePrintPage.getElements().size();
				
				//生成图片
				String imageId=generateImage(expDateStruct, serverHome, info, exportWordImageStruts, jfreeCharts,i);
				if(imageId==null){
					exportWordStruts.setReportSubDetailHaveChart(false);
				}else{
					exportWordStruts.setReportSubDetailHaveChart(true);
				}
				
				//生成表格
				if(expDateStruct.getSubTable() ==  null){
					exportWordStruts.setReportSubDetailHaveTable(false);
				}else{
					exportWordStruts.setReportSubDetailHaveTable(true);
					generateTableBrowse(expDateStruct, exportWordStruts, exportWordTableStrutses);
				}
				 
				if(talCategory==null||talCategory.length==0||talCategory[0]==null||talCategory[0].equals("")||talCategory[0].equals("null")){
					exportWordStruts.setTalCategory(false);
					
					if(n==0){
						
						if(exportWordStruts.getReportSubDetailHaveChart()){
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==6){
										 exportWordStruts.setReportSubDetailChartTitle(text);
									 }else if(m==7){
										//reportSubDetailChartId
										 //TODO  chart
									 }else if(m==8){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==9){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==10){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=11&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 } 
								 }
							}
						}else{
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==6){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==7){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==8){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=9&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 } 
								 }
							}
						}
						n=1;
					}else{
						if(exportWordStruts.getReportSubDetailHaveChart()){
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 //exportWordStruts.setReportSubDetailTitle("");
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailChartTitle(text);
									 }else if(m==6){
										//reportSubDetailChartId
										 //TODO  chart
									 }else if(m==7){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==8){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==9){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=10&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								 }
							}
						}else{
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								 if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 //exportWordStruts.setReportSubDetailTitle("");
										 exportWordStruts.setReportSubDetailSubTitle(text);
									 }else if(m==3){
										 exportWordStruts.setReportSubDetailDeviceIp(text);
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailDesc(text);
									 }else if(m==5){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==6){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==7){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=8&&m<pagesLen-4){
										  //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								 }
							}
						}
					}
					//子报表
				}else{
					//跟踪报表
					//子报表
					short talCategoryLevel = expDateStruct.getTalCategoryLevel();
					if(talCategoryLevel==2){
						exportWordStruts.setTalCategory(true);
						if(exportWordStruts.getReportSubDetailHaveChart()){
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailChartTitle(text);
									 }else if(m==3){
										//reportSubDetailChartId
										 //TODO  chart
									 }else if(m==4){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==5){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==6){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=7&&m<pagesLen-4){
										 //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								}
							}
						}else{
							for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
								Object o = (Object) iterator2.next();
								if(o instanceof JRTemplatePrintText){
									 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
									 String text=jRTemplatePrintText.getText();
									 if(m==2){
										 exportWordStruts.setReportSubDetailTableTitle(text);
									 }else if(m==3){
										 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
									 }else if(m==4){
										 //exportWordStruts.setReportSubDetailTableValueTitle(text);
									 }else if(m>=5&&m<pagesLen-4){
										 //表数据
									 }else if(m==pagesLen-4){
										 
									 }else if(m==pagesLen-3){
										 //exportWordStruts.setReportSubDetailCurrentTime(text);
									 }else if(m==pagesLen-2){
										  
									 }else if(m==pagesLen-1){
										 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
									 }
								}
							}
						}
					}else if(talCategoryLevel==1){
						//下钻报表为主要导出报表, 而不是导出上级报表里包括的下钻报表. 
						//(基本报表下钻后,再导出word情况,和自定义报表页面的下钻报表导出word情况)
						exportWordStruts.setTalCategory(false);
						
						int mstType = expDateStruct.getMstType();
						if(mstType==2){
							//自定义报表第一页时的跟踪报表
							if(exportWordStruts.getReportSubDetailHaveChart()){
								for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
									Object o = (Object) iterator2.next();
									if(o instanceof JRTemplatePrintText){
										 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
										 String text=jRTemplatePrintText.getText();
										 if(m==2){
											 //exportWordStruts.setReportSubDetailTitle("");
											 exportWordStruts.setReportSubDetailSubTitle(text);
										 }else if(m==3){
											 exportWordStruts.setReportSubDetailDeviceIp(text);
										 }else if(m==4){
											 exportWordStruts.setReportSubDetailDesc(text);
										 }else if(m==5){
											 exportWordStruts.setReportSubDetailChartTitle(text);
										 }else if(m==6){
											//reportSubDetailChartId
											 //TODO  chart
										 }else if(m==7){
											 exportWordStruts.setReportSubDetailTableTitle(text);
										 }else if(m==8){
											 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
										 }else if(m==9){
											 //exportWordStruts.setReportSubDetailTableValueTitle(text);
										 }else if(m>=10&&m<pagesLen-4){
											  //表数据
										 }else if(m==pagesLen-4){
											 
										 }else if(m==pagesLen-3){
											 //exportWordStruts.setReportSubDetailCurrentTime(text);
										 }else if(m==pagesLen-2){
											  
										 }else if(m==pagesLen-1){
											 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
										 }
									}
								}
							}else{
								for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
									Object o = (Object) iterator2.next();
									if(o instanceof JRTemplatePrintText){
										 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
										 String text=jRTemplatePrintText.getText();
										 if(m==2){
											 //exportWordStruts.setReportSubDetailTitle("");
											 exportWordStruts.setReportSubDetailSubTitle(text);
										 }else if(m==3){
											 exportWordStruts.setReportSubDetailDeviceIp(text);
										 }else if(m==4){
											 exportWordStruts.setReportSubDetailDesc(text);
										 }else if(m==5){
											 exportWordStruts.setReportSubDetailTableTitle(text);
										 }else if(m==6){
											 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
										 }else if(m==7){
											 //exportWordStruts.setReportSubDetailTableValueTitle(text);
										 }else if(m>=8&&m<pagesLen-4){
											  //表数据
										 }else if(m==pagesLen-4){
											 
										 }else if(m==pagesLen-3){
											 //exportWordStruts.setReportSubDetailCurrentTime(text);
										 }else if(m==pagesLen-2){
											  
										 }else if(m==pagesLen-1){
											 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
										 }
									}
								}
							}
						}else if(mstType==3){
							//基本报表的下钻报表为主要导出页, 自定义报表的下钻报表为主要导出页
							if(n==0){
								if(exportWordStruts.getReportSubDetailHaveChart()){
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 exportWordStruts.setReportSubDetailTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==6){
												 exportWordStruts.setReportSubDetailChartTitle(text);
											 }else if(m==7){
												//reportSubDetailChartId
												 //TODO  chart
											 }else if(m==8){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==9){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==10){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=11&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 } 
										}
									}
								}else{
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 exportWordStruts.setReportSubDetailTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==6){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==7){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==8){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=9&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 } 
										}
									}
								}
								n=1;
							}else{
								if(exportWordStruts.getReportSubDetailHaveChart()){
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										 if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 //exportWordStruts.setReportSubDetailTitle("");
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailChartTitle(text);
											 }else if(m==6){
												//reportSubDetailChartId
												 //TODO  chart
											 }else if(m==7){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==8){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==9){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=10&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 }
										 }
									}
								}else{
									for (Iterator iterator2 = jRBasePrintPage.getElements().iterator(); iterator2.hasNext();m++) {
										Object o = (Object) iterator2.next();
										 if(o instanceof JRTemplatePrintText){
											 JRTemplatePrintText jRTemplatePrintText=(JRTemplatePrintText)o;
											 String text=jRTemplatePrintText.getText();
											 if(m==2){
												 //exportWordStruts.setReportSubDetailTitle("");
												 exportWordStruts.setReportSubDetailSubTitle(text);
											 }else if(m==3){
												 exportWordStruts.setReportSubDetailDeviceIp(text);
											 }else if(m==4){
												 exportWordStruts.setReportSubDetailDesc(text);
											 }else if(m==5){
												 exportWordStruts.setReportSubDetailTableTitle(text);
											 }else if(m==6){
												 //exportWordStruts.setReportSubDetailTableKeyTitle(text);
											 }else if(m==7){
												 //exportWordStruts.setReportSubDetailTableValueTitle(text);
											 }else if(m>=8&&m<pagesLen-4){
												  //表数据
											 }else if(m==pagesLen-4){
												 
											 }else if(m==pagesLen-3){
												 //exportWordStruts.setReportSubDetailCurrentTime(text);
											 }else if(m==pagesLen-2){
												  
											 }else if(m==pagesLen-1){
												 //exportWordStruts.setReportSubDetailCurrentPage(Integer.valueOf(text));
											 }
										 }
									}
								}
							}
						}
					}
				}
				
				fillNullValue(exportWordStruts);
				
				if(imageId!=null){
					exportWordStruts.setReportSubDetailChartId(imageId);
				}
				exportWordStruts.setReportSubDetailTableValueList(exportWordTableStrutses);
				
				exportWordStruts.setDocPrId(Integer.toString(k));
				exportWordStruts.setDocPrId2(Integer.toString(k+1));
				
				exportWordStruts.setRelativeHeight(Long.toString(relativeHeight));
				exportWordStruts.setRelativeHeight2(Long.toString(relativeHeight+1024));
				
				subReports.add(exportWordStruts);
	
		
		context.put("subReports", subReports);
		context.put("jfreeCharts", jfreeCharts);
		
		generateFooterImage(pageFooterImage, context);
	}
	

}
