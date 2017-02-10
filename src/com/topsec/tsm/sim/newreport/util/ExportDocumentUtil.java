package com.topsec.tsm.sim.newreport.util;

import java.awt.Color;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.charts.XSSFLineChartData;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.report.poi.BarChartData;
import com.topsec.tal.base.report.poi.ChartData;
import com.topsec.tal.base.report.poi.LineChartData;
import com.topsec.tal.base.report.poi.PieChartData;
import com.topsec.tal.base.report.poi.SimChartDataFactory;
import com.topsec.tal.base.report.poi.SimChartDataFactoryImpl;
import com.topsec.tal.base.report.poi.SimXWPFDocument;
import com.topsec.tal.base.report.poi.XWPFUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.newreport.bean.ReportQueryConditions;
import com.topsec.tsm.sim.newreport.chart.echart.EChartImageFactory;
import com.topsec.tsm.sim.newreport.model.ReportQuery;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.util.CommonUtils;
import com.lowagie.text.Chapter;
import com.lowagie.text.Document;  
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;  
/**
 * @ClassName: ExportDocumentUtil
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年8月6日上午9:43:15
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ExportDocumentUtil {
	private static Logger logger = LoggerFactory.getLogger(ExportDocumentUtil.class) ;
	/**
	 * 
	 * @param map
	 * @param stringExport
	 */
	public static void initExportString(Map<Object, List<Object>> map,StringBuffer stringExport){
		if (null == map || map.size()<1) {
			return;
		}
		for (Map.Entry<Object, List<Object>> entry : map.entrySet()) {
			StringBuffer stringBuffer=initUnitExportString(entry);
			stringExport.append(stringBuffer);
		}
	}
	
	public static void initExportString(List<Map> maps,StringBuffer stringExport){
		if (null == maps || maps.size()<1) {
			return;
		}
		for (Map map: maps) {
			StringBuffer stringBuffer=initUnitExportString((Map<String,Object>)map);
			stringExport.append(stringBuffer);
		}
	}
	
	/**
	 * 最小单元的数据集组装
	 * @param entry 数据结构体
	 * @return
	 */
	public static StringBuffer initUnitExportString(Map.Entry<Object, List<Object>> entry){
		StringBuffer stringBuffer=new StringBuffer();

		Map<String, Object> keyMap=(Map)entry.getKey();
		Object valueObject=entry.getValue().get(0);
		List<Map> valueList=(List<Map>)valueObject;
		
		String parentReportName=(String)keyMap.get("parentReportName");
		String parentDescribe=(String)keyMap.get("parentDescribe");
		String formatStyle=(String)keyMap.get("formatStyle");
		String subReportName=(String)keyMap.get("subReportName");
		String dataStructureDesc=(String)keyMap.get("dataStructureDesc");
		String subDescribe=(String)keyMap.get("subDescribe");
		String showType=(String)keyMap.get("showType");
		Integer showOrder=Integer.valueOf(keyMap.get("showOrder").toString());
		String special=(String)keyMap.get("special");
		//一级标题样式 func1(content)
		
		//二级标题样式 func2(content)
		
		//小主题主标题样式 func3(content)
		
		//小主题副标题样式 func4(content)
		
		//小主题正文样式 func5(content)
		
		//小主题表格样式 func6(content)
		
		//小主题图片样式 func7(content)
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		
		return stringBuffer;
	}
	
	public static StringBuffer initUnitExportString(Map<String, Object> mapDat){
		StringBuffer stringBuffer=new StringBuffer();
		List list=(List)(mapDat.get(QueryUtil.RESULT_DATA));
		Object valueObject=list.get(0);
		List<Map> valueList=(List<Map>)valueObject;
		
		String parentReportName=(String)mapDat.get("parentReportName");
		String parentDescribe=(String)mapDat.get("parentDescribe");
		String formatStyle=(String)mapDat.get("formatStyle");
		String subReportName=(String)mapDat.get("subReportName");
		String dataStructureDesc=(String)mapDat.get("dataStructureDesc");
		String subDescribe=(String)mapDat.get("subDescribe");
		String showType=(String)mapDat.get("showType");
		Integer showOrder=Integer.valueOf(mapDat.get("showOrder").toString());
		String special=(String)mapDat.get("special");
		//一级标题样式 func1(content)
		
		//二级标题样式 func2(content)
		
		//小主题主标题样式 func3(content)
		
		//小主题副标题样式 func4(content)
		
		//小主题正文样式 func5(content)
		
		//小主题表格样式 func6(content)
		
		//小主题图片样式 func7(content)
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		
		return stringBuffer;
	}
	public static void pdf(Map<String, Object>exportStructMap,List<Map>maps,OutputStream out)throws Exception{
//		String FontName = "微软雅黑";
//		final String PdfEncodUCS2 = "Identity-H";
//		BaseFont chineseFont = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);//设置中文字体  
		final String WinFont = "/resource/report/msyh.ttf";
		final String PdfFontNameSong = ReportUiUtil.getSysPath()+ WinFont;
		BaseFont chineseFontMc = BaseFont.createFont(PdfFontNameSong,BaseFont.IDENTITY_H,BaseFont.EMBEDDED);//设置中文字体  
		Font headlinefont = new Font(chineseFontMc, 20, Font.BOLD); 
		Font summaryFont=new Font(chineseFontMc, 14, Font.BOLD); 
		Font contentFont=new Font(chineseFontMc, 12, Font.NORMAL); 
		Font contentBoldFont=new Font(chineseFontMc, 12, Font.BOLD); 
		
		Document document = new Document(PageSize.A4,17,17,21,21);  
//	    StyleSheet st = new StyleSheet();  
//	    st.loadTagStyle("body", "leading", "16,0");  
	    PdfWriter.getInstance(document, out);  
	    document.open();
	    //封面开始
	    addPdfParagraph(document,exportStructMap.get(QueryUtil.EXPORT_HEADLINE).toString(),headlinefont,Element.ALIGN_CENTER,0f,0f,0f);
	    
	    Paragraph spaceParagraph=new Paragraph(null,headlinefont);
	    spaceParagraph.setSpacingBefore(60f);
	    document.add(spaceParagraph);
	    
	    addPdfParagraph(document,"报表类型："+exportStructMap.get(QueryUtil.EXPORT_CATEGORY).toString(),summaryFont,Element.ALIGN_LEFT,60f,20f,0f);
	    addPdfParagraph(document,"制 作 人："+exportStructMap.get(QueryUtil.AUTHOR).toString(),summaryFont,Element.ALIGN_LEFT,60f,20f,0f);
	    addPdfParagraph(document,"制作时间："+exportStructMap.get(QueryUtil.EXECUTE_TIME).toString(),summaryFont,Element.ALIGN_LEFT,60f,20f,0f);
	    addPdfParagraph(document,"日志时间："+exportStructMap.get(QueryUtil.START_TIME).toString()+" 至 "+exportStructMap.get(QueryUtil.END_TIME).toString(),summaryFont,Element.ALIGN_LEFT,60f,20f,0f);
	    document.add(spaceParagraph);
	    
	    addPdfParagraph(30f,document,exportStructMap.get(QueryUtil.REPORT_SUMMARY).toString(),contentFont,Element.ALIGN_LEFT,60f,0f,30f);
	    //****封面结束
	    
	    //目录
	    /*****************
	     * 
	     ****************/
	    
	    //主题内容
//	    document.newPage();
	    int len=maps.size();
		for (int i=0;i<len;i++) {
			Map map= maps.get(i);
			List result=(List)map.get(QueryUtil.RESULT_DATA);
			if (null ==result || result.size()<1) {
				continue;
			}
			List<Map> datList=(List<Map>)result.get(0);
			if (null == datList || datList.size()<1) {
				continue;
			}
			//主主标题
			Paragraph paragraph=getPdfParagraph(null,map.get("subReportName").toString(),contentBoldFont,Element.ALIGN_LEFT,30f,10f,0f);
			paragraph.setSpacingBefore(20f);
			Chapter chapter=new Chapter(paragraph,i+1);
			document.add(chapter);
			//主副标题
			addPdfParagraph(document,(i+1)+".1  描述："+map.get("subDescribe").toString(),contentFont,Element.ALIGN_LEFT,50f,10f,0f);
			addPdfParagraph(document,(i+1)+".2  详细内容请看图表所示",contentFont,Element.ALIGN_LEFT,50f,10f,0f);
			//图片
			createChart(document,map,i+1,contentFont) ;
			
			//表格
			createTable(document,map,i+1,contentFont,contentBoldFont);
		}
	    document.close(); 
	}
	private static void addPdfParagraph(Document document,String content,Font font,int align,float left,float after,float right)throws Exception{
		addPdfParagraph(null,document,content,font,align,left,after,right);
	}
	private static void addPdfParagraph(Float lineSpace,Document document,String content,Font font,int align,float left,float after,float right)throws Exception{
		Paragraph paragraph=getPdfParagraph(lineSpace,content,font,align,left,after,right);
	    document.add(paragraph);
	}
	private static Paragraph getPdfParagraph(Float lineSpace,String content,Font font,int align,float left,float after,float right){
		Paragraph paragraph=null;
		if (null !=lineSpace && 0!=lineSpace) {
			paragraph=new Paragraph(lineSpace,content,font);
		}else {
			paragraph=new Paragraph(content,font);
		}
		paragraph.setAlignment(align);
		paragraph.setIndentationLeft(left);
		paragraph.setSpacingAfter(after);
		paragraph.setIndentationRight(right);
		return paragraph;
	}
	public static SimXWPFDocument doc(Map<String, Object>exportStructMap,List<Map>maps) throws Exception{
		SimXWPFDocument doc = new SimXWPFDocument();
		//封面
		setCover(doc,exportStructMap);

		//目录部分
//		exportStructMap.get(QueryUtil.RESULT_DATA_AND_STRUCTURE).toString();
//		XWPFParagraph cat = doc.createParagraph();
//		cat.setPageBreak(true) ;
		
		//主题内容
		int len=maps.size();
		for (int i=0;i<len;i++) {
			Map map= maps.get(i);
			List result=(List)map.get(QueryUtil.RESULT_DATA);
			if (null ==result || result.size()<1) {
				continue;
			}
			List<Map> datList=(List<Map>)result.get(0);
			if (null == datList || datList.size()<1) {
				continue;
			}
			XWPFParagraph subParagraph=addParagraph(doc,i+1+"、"+map.get("subReportName").toString(),12,0,true,ParagraphAlignment.LEFT) ;
			//主副标题
			addParagraph(doc,"   "+(i+1)+".1  描述："+map.get("subDescribe").toString(),12,0,false,ParagraphAlignment.LEFT) ;
			addParagraph(doc,"   "+(i+1)+".2  详细内容请看图表所示",12,0,false,ParagraphAlignment.LEFT) ;
			//图片
			createChart(doc,map,i+1) ;
			
			//表格
			createTable(doc,map,i+1);
			subParagraph.setPageBreak(true);
		}
        return doc;
	}
	public static XSSFWorkbook xlsx(Map<String, Object>exportStructMap,List<Map>maps){
		XSSFWorkbook xlsx=new XSSFWorkbook();
		XSSFSheet sheet=xlsx.createSheet("报表数据导出");

		XSSFFont headlineFont=getFont(xlsx, "华文楷体", true, (short)20);
		XSSFFont exportSummaryFont=getFont(xlsx, "华文楷体", true, (short)14);
		XSSFFont contentFont=getFont(xlsx, "华文楷体", false, (short)12);
//		XSSFFont contentBoldFont=getFont(xlsx, "华文楷体", true, (short)12);
		
		CellStyle headlineStyle=getCellStyle(xlsx,CellStyle.ALIGN_CENTER,CellStyle.VERTICAL_CENTER,headlineFont,false);
		CellStyle contentStyle=getCellStyle(xlsx,CellStyle.ALIGN_LEFT,CellStyle.VERTICAL_CENTER,contentFont,true);
		CellStyle exportSummaryStyle=getCellStyle(xlsx,CellStyle.ALIGN_LEFT,CellStyle.VERTICAL_CENTER,exportSummaryFont,false);
		sheet.setColumnWidth(0, 900);
		sheet.setColumnWidth(1, 6000);
		for (int i = 2; i < 6; i++) {
			sheet.setColumnWidth(i, 4500);
		}
		int startRow=0;
		setRowOneContentAndStyle(sheet,startRow,(short)1000,exportStructMap.get(QueryUtil.EXPORT_HEADLINE).toString(),headlineStyle,0,6);
		getSheetRowOneColomn(sheet,++startRow,(short)1200,0,6);
		setRowTwoContentAndStyle(sheet,++startRow,(short)800,"报表类型：",exportStructMap.get(QueryUtil.EXPORT_CATEGORY).toString(),exportSummaryStyle,1,2,6);
		setRowTwoContentAndStyle(sheet,++startRow,(short)800,"制 作 人 ：",exportStructMap.get(QueryUtil.AUTHOR).toString(),exportSummaryStyle,1,2,6);
		setRowTwoContentAndStyle(sheet,++startRow,(short)800,"制作时间：",exportStructMap.get(QueryUtil.EXECUTE_TIME).toString(),exportSummaryStyle,1,2,6);
		setRowTwoContentAndStyle(sheet,++startRow,(short)800,"日志时间：",exportStructMap.get(QueryUtil.START_TIME)+" 至 "+exportStructMap.get(QueryUtil.START_TIME),exportSummaryStyle,1,2,6);
		getSheetRowOneColomn(sheet,++startRow,(short)1800,0,6);
		
		setRowOneContentAndStyle(sheet,++startRow,(short)3600,"      "+exportStructMap.get(QueryUtil.REPORT_SUMMARY),contentStyle,0,6);
		getSheetRowOneColomn(sheet,++startRow,(short)800,0,6);
		
		//主题内容
		int len=maps.size();
		for (int i=0;i<len;i++) {
			Map map= maps.get(i);
			List result=(List)map.get(QueryUtil.RESULT_DATA);
			if (null ==result || result.size()<1) {
				continue;
			}
			List<Map> datList=(List<Map>)result.get(0);
			if (null == datList || datList.size()<1) {
				continue;
			}
			setRowOneContentAndStyle(sheet,++startRow,(short)700,i+1+"、"+map.get("subReportName"),exportSummaryStyle,1,6);
			//主副标题
			setRowOneContentAndStyle(sheet,++startRow,(short)600,"   "+(i+1)+".1  描述："+map.get("subDescribe"),contentStyle,1,6);
			setRowOneContentAndStyle(sheet,++startRow,(short)600,"   "+(i+1)+".2  详细内容请看图表所示",contentStyle,1,6);
			
			//图片
			startRow=createChart(sheet,map,i+1,startRow,contentStyle,1,6,startRow+5,startRow+4+datList.size());
			//表格
			startRow=createTable(sheet,map,i+1,startRow,contentStyle,1,6);
			getSheetRowOneColomn(sheet,++startRow,(short)800,0,6);
		}
		
		return xlsx;
	}
	private static void setRowTwoContentAndStyle(XSSFSheet sheet,int rowNum,short rowHeight,String contentDesc,String content,CellStyle cellStyle,int colFromNum,int colToNum,int colEndNum){
		XSSFRow sheetRowCategory = getSheetRowTowColomn(sheet,rowNum,rowHeight,colFromNum,colToNum,colEndNum);
		setCell(sheetRowCategory,colFromNum,contentDesc,cellStyle);
		setCell(sheetRowCategory,colToNum+1,content,cellStyle);
	}
	private static void setRowOneContentAndStyle(XSSFSheet sheet,int rowNum,short rowHeight,String content,CellStyle cellStyle,int colFromNum,int colEndNum){
		XSSFRow sheetRowCategory = getSheetRowOneColomn(sheet,rowNum,rowHeight,colFromNum,colEndNum);
		setCell(sheetRowCategory,colFromNum,content,cellStyle);
	}
	private static XSSFFont getFont(XSSFWorkbook xlsx,String fontName,boolean bold,short fontSize){
		XSSFFont font=xlsx.createFont();
		font.setFontName(fontName);
		font.setBold(bold);
		font.setFontHeightInPoints(fontSize);
		return font;
	}
	private static CellStyle getCellStyle(XSSFWorkbook xlsx,short align,short vertical,XSSFFont font,boolean wrapText){
		CellStyle style=xlsx.createCellStyle();
		style.setAlignment(align);
		style.setVerticalAlignment(vertical);
		style.setFont(font);
		style.setWrapText(wrapText);
		return style;
	}
	private static void setCell(XSSFRow sheetRow,int colnum,String content,CellStyle cellStyle){
		XSSFCell cellAuth =sheetRow.createCell(colnum);
		cellAuth.setCellType(Cell.CELL_TYPE_STRING) ;
		cellAuth.setCellValue(content) ;
		cellAuth.setCellStyle(cellStyle);
	}
	private static void setCellNumValue(XSSFRow sheetRow,int colnum,double value,CellStyle cellStyle){
		XSSFCell cellAuth =sheetRow.createCell(colnum);
		cellAuth.setCellType(Cell.CELL_TYPE_NUMERIC) ;
		cellAuth.setCellValue(value) ;
		cellAuth.setCellStyle(cellStyle);
	}
	
	private static XSSFRow getSheetRowTowColomn(XSSFSheet sheet,int rownum,short height,int colFromNum,int colToNum,int colEndNum){
		XSSFRow sheetRow = sheet.createRow(rownum);
		sheetRow.setHeight(height);
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, colFromNum, colToNum));
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, colToNum+1, colEndNum));
		return sheetRow;
	}
	private static XSSFRow getSheetRowOneColomn(XSSFSheet sheet,int rownum,short height,int colFromNum,int colEndNum){
		XSSFRow sheetRow = sheet.createRow(rownum);
		sheetRow.setHeight(height);
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, colFromNum, colEndNum));
		return sheetRow;
	}
	private static int createTable(XSSFSheet sheet,Map mapUnit,int reportOrder,int startRow,CellStyle cellStyle,int colFromNum,int colEndNum){
		if (null == mapUnit || mapUnit.size()==0) {
			return startRow;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []categorysCNName=(String [])dataStructureMap.get("categorysCNName");
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		if (null == list || list.size()==0) {
			return startRow;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
    	if(colon>-1){
    		showType=showType.substring(0,colon);
    	}
    	int maplistlen=mapDatList.size();
    	long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		setRowOneContentAndStyle(sheet,++startRow,(short)600,"   "+reportOrder+".4  详细内容统计表",cellStyle,colFromNum,colEndNum);
		startRow++;
		XSSFRow rowHeader=sheet.createRow(startRow);
		for(int i=0;i<categorysCNName.length;i++){
			String caName = categorysCNName[i] ;
			setCell(rowHeader,i+1,caName,cellStyle);
		}
		for(int i=0;i<statisticalCNName.length;i++){
			String stName = statisticalCNName[i] ;
			if (null != unit) {
				stName=stName+"("+unit+")";
			}
			setCell(rowHeader,i+1+categorysCNName.length,stName,cellStyle);
		}
		
		for(int rowIndex=0;rowIndex<maplistlen;rowIndex++){
			startRow++;
			XSSFRow rowContent=sheet.createRow(startRow);
			Map mapdat=mapDatList.get(rowIndex);
			for(int columnIndex=0;columnIndex<categorysName.length;columnIndex++){
				
				String field = mapdat.get(categorysName[columnIndex]).toString() ;
				if ("PRIORITY".equalsIgnoreCase(categorysName[columnIndex]) 
						|| "RISK".equals(categorysName[columnIndex])) {
					if (field.indexOf("危险")==-1) {
						int val=Integer.valueOf(field);
						field=ResultOperatorUtils.riskCnName(val);
					}
				}
				setCell(rowContent,columnIndex+1,field,cellStyle);
			}
			for(int columnIndex=0;columnIndex<statisticalsName.length;columnIndex++){
				Object valObject=mapdat.get(statisticalsName[columnIndex]);
				valObject=null==valObject?"0":valObject;
				String fieldValue = valObject.toString() ;
				if (null != unit) {
					long val=Long.valueOf(fieldValue);
					double vald=0;
					if ("FLOW_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.flowOperater(val, unit);
					}else if ("COUNT_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.showNumberOperater(val, unit);
					}
					setCellNumValue(rowContent,columnIndex+1+categorysName.length,vald,cellStyle);
					
				}else {
					setCellNumValue(rowContent,columnIndex+1+categorysName.length,Double.valueOf(fieldValue),cellStyle);
				}
			}
		}
		return startRow;
	}
	private static int createChart(XSSFSheet sheet,Map mapUnit,int reportOrder,int startRow,CellStyle cellStyle,int colFromNum,int colEndNum,int chartdataStartRow,int chartdataEndRow){
		
		setRowOneContentAndStyle(sheet,++startRow,(short)600,"   "+reportOrder+".3  详细内容统计图",cellStyle,colFromNum,colEndNum);
		if (null == mapUnit || mapUnit.size()==0) {
			return startRow;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		if (null == list || list.size()==0) {
			return startRow;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		getSheetRowOneColomn(sheet,++startRow,(short)5000,0,6);
		
		for (int j = 0; j < 7; j++) {
			sheet.getRow(startRow).createCell(j);
		}
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
	    String recommend=null;
	    if(colon>-1){
	    	recommend=showType.substring(showType.indexOf("(")+1, showType.indexOf(")") );
	    	showType=showType.substring(0,colon);
	    }
		
		long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		
		unit=null == unit ?"":unit;
		
		XSSFDrawing drawing = sheet.createDrawingPatriarch() ;
		
		XSSFClientAnchor xssfClientAnchor=new XSSFClientAnchor(0,0,0,0,1,startRow,6,startRow+1);
		XSSFChart chart = drawing.createChart(xssfClientAnchor) ;
		XSSFSimpleShape xssfSimpleShape=drawing.createSimpleShape(xssfClientAnchor);
		
		XSSFLineChartData chartData =chart.getChartDataFactory().createLineChartData() ;
		ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, new CellRangeAddress(chartdataStartRow, chartdataEndRow , 1, 1));
		for(int colIndex=0;colIndex < statisticalsName.length;colIndex++){
	       	ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(chartdataStartRow, chartdataEndRow , categorysName.length+colIndex+1, categorysName.length+colIndex+1));
	       	chartData.addSeries(xs, ys);
	    }
		 
    	ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN) ;
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        
    	chartData.fillChart(chart, bottomAxis,leftAxis);

    	/*XSSFClientAnchor xssfClientAnchor=new XSSFClientAnchor(0,0,0,0,1,startRow,6,startRow+1);
    	org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing ctDrawing=drawing.getCTDrawing();
		
		XSSFChart chart = drawing.createChart(xssfClientAnchor) ;
		ChartLegend legend = chart.getOrCreateLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        ScatterChartData data = chart.getChartDataFactory().createScatterChartData();
        ValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, new CellRangeAddress(chartdataStartRow, chartdataEndRow , 1, 1));
		for(int colIndex=0;colIndex < statisticalsName.length;colIndex++){
	       	ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(chartdataStartRow, chartdataEndRow , categorysName.length+colIndex+1, categorysName.length+colIndex+1));
	       	chartData.addSeries(xs, ys);
	    }
        chart.plot(data, bottomAxis, leftAxis);*/

        
		if("TREND".equals(showType)){
    		recommend=(recommend==null)?"line":recommend;
    		
    	}else if("NOT_TREND".equals(showType)){
    		recommend=(recommend==null)?"bar":recommend;
    		if("standardbar".equals(recommend)
    				|| "rainbow".equals(recommend)
    				|| "bar".equals(recommend) 
    				|| "eventRiver".equals(recommend) ){
    			
    		}else if("standardline".equals(recommend)){
    			
    		}else if("pie".equals(recommend)){
    			
    		}else {
    			recommend=recommend.replace("standard", "");
    			recommend=recommend.replace("rainbow", "");
    			throw new RuntimeException("无效的图表类型:"+recommend);
    		}
    	}
		
//		chartData.addSeries(new , values)
		return startRow;
	}
	/**
	 * 设置首页（封面）
	 * @param doc
	 * @param exportStructMap
	 */
	public static void setCover(SimXWPFDocument doc,Map<String, Object>exportStructMap){
		
		addParagraph(doc,exportStructMap.get(QueryUtil.EXPORT_HEADLINE).toString(),20,10,true,ParagraphAlignment.CENTER) ;//标题
		addParagraph(doc,"",20,10,false,ParagraphAlignment.CENTER) ;
		addParagraph(doc,"",20,10,false,ParagraphAlignment.CENTER) ;
		addParagraph(doc,"报表类型："+exportStructMap.get(QueryUtil.EXPORT_CATEGORY).toString(),14,10,true,ParagraphAlignment.LEFT) ;//
		addParagraph(doc,"制 作 人："+exportStructMap.get(QueryUtil.AUTHOR).toString(),14,10,true,ParagraphAlignment.LEFT) ;//
		addParagraph(doc,"制作时间："+exportStructMap.get(QueryUtil.EXECUTE_TIME).toString(),14,10,true,ParagraphAlignment.LEFT) ;//
		addParagraph(doc,"日志时间："+exportStructMap.get(QueryUtil.START_TIME).toString()+" 至 "+exportStructMap.get(QueryUtil.END_TIME).toString(),14,10,true,ParagraphAlignment.LEFT) ;//
		addParagraph(doc,"",20,10,false,ParagraphAlignment.CENTER) ;
		addParagraph(doc,"",20,10,false,ParagraphAlignment.CENTER) ;
		addParagraph(doc,exportStructMap.get(QueryUtil.REPORT_SUMMARY).toString(),12,8,false,ParagraphAlignment.LEFT) ;//

	}
	
	/**
	 * 导出doc封面设计
	 * @param map
	 * @return
	 */
	public static StringBuffer cover(Map<Object, List<Object>> map){
		return null;
	}
	
	/**
	 * 导出doc目录设计
	 * @param map
	 * @return
	 */
	public static StringBuffer catalogue(Map<Object, List<Object>> map){
		return null;
	}
	
	/**
	 * 导出doc的摘要信息
	 * @param map
	 * @return
	 */
	public static StringBuffer summary(Map<Object, List<Object>> map){
		return null;
	}
	
	/**
	 * 导出doc的正文信息
	 * @param map
	 * @return
	 */
	public static StringBuffer mainBodyText(Map<Object, List<Object>> map){
		return null;
	}
	
	public static void filldat(Map<String, Object> dataStructureMap,List<Object> valueList){
		if (GlobalUtil.isNullOrEmpty(valueList) || GlobalUtil.isNullOrEmpty(dataStructureMap)) {
			return;
		}
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueList.get(0);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []categorysCNName=(String [])dataStructureMap.get("categorysCNName");
		
		for (Map<String, Object> mapUnit : mapDatList) {
			
			for (int i = 0; i < categorysName.length; i++) {
				
				String catName=categorysName[i];
				String catcnName=categorysCNName[i];
				
				Object numDat=mapUnit.get(catName);
				
			}
			
		}
	}
	
	public static String setHtml(String html,Map<String, Object>exportStructMap){
		if (null == html) {
			return "";
		}
		html=html.replace("$EXPORT_HEADLINE", exportStructMap.get(QueryUtil.EXPORT_HEADLINE).toString());
		html=html.replace("$EXPORT_CATEGORY", exportStructMap.get(QueryUtil.EXPORT_CATEGORY).toString());
		html=html.replace("$AUTHOR", exportStructMap.get(QueryUtil.AUTHOR).toString());
		html=html.replace("$EXECUTE_TIME", exportStructMap.get(QueryUtil.EXECUTE_TIME).toString());
		html=html.replace("$START_TIME", exportStructMap.get(QueryUtil.START_TIME).toString());
		html=html.replace("$END_TIME", exportStructMap.get(QueryUtil.END_TIME).toString());
		html=html.replace("$REPORT_SUMMARY", exportStructMap.get(QueryUtil.REPORT_SUMMARY).toString());
		html=html.replace("$RESULT_DATA_AND_STRUCTURE", exportStructMap.get(QueryUtil.RESULT_DATA_AND_STRUCTURE).toString());
		return html;
	}
	
	public static StringBuffer getReportSummary(List<Map> maps,Map<String, Object>exportStructMap,String exportFormat){
		StringBuffer stringBuffer=new StringBuffer();
		if (null == maps || maps.size()<1) {
			return stringBuffer;
		}
		int len=maps.size();
		if ("html".equals(exportFormat)) {
			stringBuffer.append("<b>报告概要：</b>").append("<br/><br/>");
			stringBuffer.append("&nbsp;&nbsp;");
		}else {
			stringBuffer.append("报告概要：");
		}
		stringBuffer.append(exportStructMap.get(QueryUtil.SECURITY_OBJECT_TYPE));
		stringBuffer.append("类型报告，");
		stringBuffer.append(exportStructMap.get(QueryUtil.DVC_ADDRESS));
		stringBuffer.append(exportStructMap.get(QueryUtil.EXPORT_HEADLINE));
		stringBuffer.append("，显示自 ");
		stringBuffer.append(exportStructMap.get(QueryUtil.START_TIME));
		stringBuffer.append(" 至 ");
		stringBuffer.append(exportStructMap.get(QueryUtil.END_TIME));
		stringBuffer.append(" 的时间段内 ，");
		stringBuffer.append(exportStructMap.get(QueryUtil.REPORT_DESC));
		stringBuffer.append("。");
		StringBuffer datBuffer=new StringBuffer();
		for (int i=0;i<len;i++) {
			Map map= maps.get(i);
			List result=(List)map.get(QueryUtil.RESULT_DATA);
			if (null ==result || result.size()<1) {
				continue;
			}
			List<Map> datList=(List<Map>)result.get(0);
			if (null == datList || datList.size()<1) {
				continue;
			}
			datBuffer.append(map.get("subReportName"));
			if (i != len-1) {
				datBuffer.append(",");
			}
		}
		if (datBuffer.length()>1) {
			stringBuffer.append("其中：").append(datBuffer);
			stringBuffer.append("有业务数据，可以重点关注，详细情况请看下面详细内容。");
		}else {
			if ("html".equals(exportFormat))
			stringBuffer.append("&nbsp;<b>所选时间段内无重点数据，可以不予关注。</b>");
			else {
				stringBuffer.append(" 所选时间段内无重点数据，可以不予关注。 ");
			}
		}
		
		return stringBuffer;
	}
	
	/**
	 * 添加三级标题的段落
	 * @param doc
	 * @param text
	 * @param align
	 * @return
	 */
	static XWPFParagraph addParagraph(SimXWPFDocument doc,String text,int fontSize,int textPosition,boolean bold,ParagraphAlignment align){
		XWPFParagraph par = doc.createParagraph() ; 
		return setAttribute(par, text, bold, text, fontSize,textPosition, align) ;
	}
	static XWPFParagraph setAttribute(XWPFParagraph par,String text,boolean bold,String textFont,int fontSize,int textPosition,ParagraphAlignment align){
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
		run.setTextPosition(textPosition);
		return par ;
	}
	
	static void createTable(SimXWPFDocument doc,Map mapUnit,int reportOrder){
		if (null == mapUnit || mapUnit.size()==0) {
			return;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []categorysCNName=(String [])dataStructureMap.get("categorysCNName");
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		if (null == list || list.size()==0) {
			return;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
    	if(colon>-1){
    		showType=showType.substring(0,colon);
    	}
    	int maplistlen=mapDatList.size();
    	long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		addParagraph(doc,"   "+reportOrder+".4  详细内容统计表",12,0,false,ParagraphAlignment.LEFT) ;

		int headerlen=categorysName.length+statisticalsName.length;
		XWPFTable table = doc.createTable(maplistlen+1, headerlen);
		int cellWidths = 8100/(headerlen) ;
		int rowHeight = 500 ;//大概0.8厘米左右
		XWPFTableRow headerRow = table.getRow(0) ;
		headerRow.setHeight(rowHeight) ;
		for(int i=0;i<categorysCNName.length;i++){
			String caName = categorysCNName[i] ;
			XWPFTableCell headerCell = headerRow.getCell(i) ;
			headerCell.setText(caName) ;
			XWPFUtil.setCellWidth(headerCell, cellWidths,"center") ;
		}
		for(int i=0;i<statisticalCNName.length;i++){
			String stName = statisticalCNName[i] ;
			XWPFTableCell headerCell = headerRow.getCell(i+categorysCNName.length) ;
			if (null != unit) {
				stName=stName+"("+unit+")";
			}
			headerCell.setText(stName) ;
			XWPFUtil.setCellWidth(headerCell, cellWidths,"center") ;
		}
		
		for(int rowIndex=0;rowIndex<maplistlen;rowIndex++){
			Map mapdat=mapDatList.get(rowIndex);
			XWPFTableRow row = table.getRow(rowIndex+1) ;
			row.setHeight(rowHeight) ;
			for(int columnIndex=0;columnIndex<categorysName.length;columnIndex++){
				XWPFTableCell cell = row.getCell(columnIndex) ; 
				String field = mapdat.get(categorysName[columnIndex]).toString() ;
				if ("PRIORITY".equalsIgnoreCase(categorysName[columnIndex]) 
						|| "RISK".equals(categorysName[columnIndex])) {
					if (field.indexOf("危险")==-1) {
						int val=Integer.valueOf(field);
						field=ResultOperatorUtils.riskCnName(val);
					}
				}
				cell.setText(field) ;
				XWPFUtil.setCellWidth(cell, cellWidths,"center") ;
			}
			for(int columnIndex=0;columnIndex<statisticalsName.length;columnIndex++){
				XWPFTableCell cell = row.getCell(columnIndex+categorysName.length) ; 
				Object valObject=mapdat.get(statisticalsName[columnIndex]);
				valObject=null==valObject?"0":valObject;
				String fieldName = valObject.toString() ;
				if (null != unit) {
					long val=Long.valueOf(fieldName);
					double vald=0;
					if ("FLOW_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.flowOperater(val, unit);
					}else if ("COUNT_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.showNumberOperater(val, unit);
					}
					cell.setText(""+vald) ;
				}else {
					cell.setText(fieldName) ;
				}
				
				XWPFUtil.setCellWidth(cell, cellWidths,"center") ;
			}
		
		}
	}
	
	private static void createTable(Document document,Map mapUnit,int reportOrder,Font contentFont,Font contentBoldFont)throws Exception{
		if (null == mapUnit || mapUnit.size()==0) {
			return;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []categorysCNName=(String [])dataStructureMap.get("categorysCNName");
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		if (null == list || list.size()==0) {
			return;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
    	if(colon>-1){
    		showType=showType.substring(0,colon);
    	}
    	int maplistlen=mapDatList.size();
    	long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		addPdfParagraph(document,reportOrder+".4  详细内容统计表",contentFont,Element.ALIGN_LEFT,50f,10f,0f);
		int headerlen=categorysName.length+statisticalsName.length;
		float[] cols=new float[headerlen];
		cols[0]=170;
		int cellWidths = 290/(headerlen-1) ;
		int rowHeight = 25 ;//
		for (int i = 1; i < headerlen; i++) {
			cols[i]=cellWidths;
		}
		PdfPTable table=new PdfPTable(cols);
		table.setSpacingBefore(20f);
		
		for(int i=0;i<categorysCNName.length;i++){
			String caName = categorysCNName[i] ;
			Paragraph paragraph=getPdfParagraph(null, caName, contentBoldFont, Element.ALIGN_CENTER, 10, 0, 0);
			PdfPCell headerPCell=new PdfPCell();
			headerPCell.setPadding(0);
			headerPCell.setBackgroundColor(Color.lightGray);
			headerPCell.addElement(paragraph);
			headerPCell.setFixedHeight(rowHeight+5);
			table.addCell(headerPCell);
		}
		for(int i=0;i<statisticalCNName.length;i++){
			String stName = statisticalCNName[i] ;
			if (null != unit) {
				stName=stName+"("+unit+")";
			}
			Paragraph paragraph=getPdfParagraph(null, stName, contentBoldFont, Element.ALIGN_CENTER, 10, 0, 0);
			PdfPCell headerPCell=new PdfPCell();
			headerPCell.setPadding(0);
			headerPCell.setBackgroundColor(Color.lightGray);
			headerPCell.addElement(paragraph);
			headerPCell.setFixedHeight(rowHeight);
			table.addCell(headerPCell);
		}
		
		for(int rowIndex=0;rowIndex<maplistlen;rowIndex++){
			Map mapdat=mapDatList.get(rowIndex);
			
			for(int columnIndex=0;columnIndex<categorysName.length;columnIndex++){
				
				String field = mapdat.get(categorysName[columnIndex]).toString() ;
				if ("PRIORITY".equalsIgnoreCase(categorysName[columnIndex]) 
						|| "RISK".equals(categorysName[columnIndex])) {
					if (field.indexOf("危险")==-1) {
						int val=Integer.valueOf(field);
						field=ResultOperatorUtils.riskCnName(val);
					}
				}
				
				Paragraph paragraph=getPdfParagraph(null, field, contentFont, Element.ALIGN_LEFT, 10, 0, 0);
				PdfPCell rowPCell=new PdfPCell();
				rowPCell.setPadding(0);
				rowPCell.setFixedHeight(rowHeight);
				rowPCell.addElement(paragraph);
//				rowPCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//				rowPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
				table.addCell(rowPCell);
				
			}
			for(int columnIndex=0;columnIndex<statisticalsName.length;columnIndex++){
				Paragraph paragraph=null;
				Object valObject=mapdat.get(statisticalsName[columnIndex]);
				valObject=null==valObject?"0":valObject;
				String fieldName = valObject.toString() ;
				if (null != unit) {
					long val=Long.valueOf(fieldName);
					double vald=0;
					if ("FLOW_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.flowOperater(val, unit);
					}else if ("COUNT_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.showNumberOperater(val, unit);
					}
					paragraph=getPdfParagraph(null, ""+vald, contentFont, Element.ALIGN_CENTER, 10, 0, 0);
					
				}else {
					paragraph=getPdfParagraph(null, fieldName, contentFont, Element.ALIGN_CENTER, 10, 0, 0);
				}
				PdfPCell rowPCell=new PdfPCell();
				rowPCell.setPadding(0);
				rowPCell.addElement(paragraph);
				rowPCell.setFixedHeight(rowHeight);
				table.addCell(rowPCell);
			}
			
		}
		document.add(table);
	}
	
	public static long getMaxValueFromDat(String showType,List<Map<String,Object>>mapDatList,String[]statisticalsName){
		long maxVal=-1;
		/**
		 * 趋势图是按照时间排序的，不是按照数据大小排序的，因此需要完全遍历才能找到最大值
		 */
    	if("TREND".equals(showType)){
    		for(int rowIndex=0;rowIndex<mapDatList.size();rowIndex++){
    			Map mapdat=mapDatList.get(rowIndex);
    			for(int columnIndex=0;columnIndex<statisticalsName.length;columnIndex++){
        			Object valObject=mapdat.get(statisticalsName[columnIndex]);
    				valObject=null==valObject?"0":valObject;
    				long tmpL=Long.valueOf(valObject.toString());
        			if (maxVal<tmpL) {
        				maxVal=tmpL;
    				}
        		}
    		}
    	}else {
    		Map mapdat=mapDatList.get(0);
    		for(int columnIndex=0;columnIndex<statisticalsName.length;columnIndex++){
    			Object valObject=mapdat.get(statisticalsName[columnIndex]);
				valObject=null==valObject?"0":valObject;
				long tmpL=Long.valueOf(valObject.toString());
    			if (maxVal<tmpL) {
    				maxVal=tmpL;
				}
    		}
		}
    	return maxVal;
	}
	static String translate(Object value,String fieldName){
		if(DataConstants.PRIORITY.equalsIgnoreCase(fieldName)){
			return CommonUtils.getLevel(value) ;
		}
		return StringUtil.toString(value) ;
	}
	private static void createChart(Document document,Map mapUnit,int reportOrder,Font contentFont)throws Exception{
		addPdfParagraph(document,reportOrder+".3  详细内容统计图",contentFont,Element.ALIGN_LEFT,50f,10f,0f);
		if (null == mapUnit || mapUnit.size()==0) {
			return ;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		if (null == list || list.size()==0) {
			return ;
		}
				
		String jsonData=null;
		
		jsonData=EChartImageFactory.getOptions(mapUnit);
		String exportImgName=EChartImageFactory.exportUsePhantomjs(jsonData);
		Image image=null;
		try {
			image=Image.getInstance(exportImgName);
		} catch (Exception e) {
			exportImgName=exportImgName.substring(0, exportImgName.lastIndexOf("/tmp"))+"/default.png";
			image=Image.getInstance(exportImgName);
			logger.error("...生成图片失败", e);
		}
		image.scalePercent(75f);
		image.setAlignment(Element.ALIGN_CENTER);
		document.add(image);
	}
	public static Object[]getCategorys(Map mapUnit){
		Object[]objects=null;
		if (null ==mapUnit || 0==mapUnit.size()) {
			return objects;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		
		if (null == list || list.size()==0) {
			return objects;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		ReportQueryConditions queryConditions=(ReportQueryConditions)mapUnit.get(QueryUtil.QUERY_CONDITIONS_OBJ);
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
	    if(colon>-1){
	    	showType=showType.substring(0,colon);
	    }
	    int len=-1;
	    if("TREND".equals(showType)){
	    	len=mapDatList.size();
	    }else {
	    	len=Math.min(queryConditions.getTopn(),mapDatList.size());
		}
		objects=new Object[len*categorysName.length];
		int pos=0;
		//填充图表categories行数据
    	for (int columnIndex = 0; columnIndex < categorysName.length; columnIndex++){
    		String timeFormat=null;
    		if ("START_TIME".equalsIgnoreCase(categorysName[columnIndex])){
    			Map mapdat=mapDatList.get(0);
        		Object object=mapdat.get("START_TIME");
        		Date startDate= null==object?(new Date()):(Date)object;
        		mapdat=mapDatList.get(len-1);
        		object=mapdat.get("START_TIME");
        		Date endDate= null==object?(new Date()):(Date)object;
        		timeFormat=QueryUtil.timeFormat(startDate, endDate);
        		object=null;
        		startDate=null;
        		endDate=null;
    		}
        	for(int rowIndex=0;rowIndex<len;rowIndex++){
        		Map mapdat=mapDatList.get(rowIndex);
        		Object object=mapdat.get(categorysName[columnIndex]);
        		object= null==object?"UNKNOW":object;
        		if ("PRIORITY".equalsIgnoreCase(categorysName[columnIndex]) 
						|| "RISK".equals(categorysName[columnIndex])) {
					if (object.toString().indexOf("危险")==-1) {
						int val=Integer.valueOf(object.toString());
						object=ResultOperatorUtils.riskCnName(val);
					}
				}
        		if ("START_TIME".equalsIgnoreCase(categorysName[columnIndex])) {
        			if (object instanceof Date) {
						object=QueryUtil.dateStringFormat(timeFormat, (Date)object);
					}
				}
        		objects[pos++]=object;
        	}
        }
		return objects;
	}
	public static Object[]getLegend(Map mapUnit){
		
		if (null ==mapUnit || 0==mapUnit.size()) {
			return null;
		}
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		return statisticalCNName;
	}
	
	public static Map<String,Object> getChartTypeInfo(Map mapUnit){
		Map<String,Object>mapSeries=null;
		if (null ==mapUnit || 0==mapUnit.size()) {
			return mapSeries;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		if (null == list || list.size()==0) {
			return mapSeries;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
    	String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
	    String recommend=null;
	    if(colon>-1){
	    	recommend=showType.substring(showType.indexOf("(")+1, showType.indexOf(")") );
	    	showType=showType.substring(0,colon);
	    }
		
		mapSeries=new HashMap<String, Object>();
		mapSeries.put("showType", showType);
		mapSeries.put("recommend", recommend);
    	
    	
		return mapSeries;
	}
	
	public static Map<String,Object> getSeries(Map mapUnit){
		Map<String,Object>mapSeries=null;
		if (null ==mapUnit || 0==mapUnit.size()) {
			return mapSeries;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		if (null == list || list.size()==0) {
			return mapSeries;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
    	String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
	    String recommend=null;
	    if(colon>-1){
	    	recommend=showType.substring(showType.indexOf("(")+1, showType.indexOf(")") );
	    	showType=showType.substring(0,colon);
	    }
		
		long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		unit=null == unit ?"":unit;
		mapSeries=new HashMap<String, Object>();
		mapSeries.put("showType", showType);
		mapSeries.put("unit", unit);
		mapSeries.put("recommend", recommend);
    	
    	ReportQueryConditions queryConditions=(ReportQueryConditions)mapUnit.get(QueryUtil.QUERY_CONDITIONS_OBJ);
    	int len=-1;
	    if("TREND".equals(showType)){
	    	len=mapDatList.size();
	    }else {
	    	len=Math.min(queryConditions.getTopn(),mapDatList.size());
		}
		List<Map<String, Object>> datMaps=new ArrayList<Map<String,Object>>(statisticalsName.length);
		Map<String,Object>datAndDes=new HashMap<String, Object>(statisticalsName.length);
		for(int columnIndex=0;columnIndex<statisticalsName.length;columnIndex++){
			Object[]vals=new Object[len];
			for(int rowIndex=0;rowIndex<len;rowIndex++){
				Map mapdat=mapDatList.get(rowIndex);
				Object valObject=mapdat.get(statisticalsName[columnIndex]);
				valObject=null==valObject?"0":valObject;
				
				if (null != unit) {
					long val=Long.valueOf(valObject.toString());
					double vald=0;
					if ("FLOW_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.flowOperater(val, unit);
					}else if ("COUNT_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.showNumberOperater(val, unit);
					}
					vals[rowIndex]=vald;
					
				}else {
					vals[rowIndex]=valObject;
				}
			}
			datAndDes.put(statisticalCNName[columnIndex], vals);
		}
		datMaps.add(datAndDes);
		mapSeries.put("datList", datMaps);
		return mapSeries;
	}
	
	public static Map<String,Object> getStatisticals(Map mapUnit){
		Map<String,Object>mapStatistical=null;
		if (null ==mapUnit || 0==mapUnit.size()) {
			return mapStatistical;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		if (null == list || list.size()==0) {
			return mapStatistical;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
    	String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
	    String recommend=null;
	    if(colon>-1){
	    	recommend=showType.substring(showType.indexOf("(")+1, showType.indexOf(")") );
	    	showType=showType.substring(0,colon);
	    }
		
		long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		unit=null == unit ?"":unit;
		mapStatistical=new HashMap<String, Object>();
		mapStatistical.put("showType", showType);
		mapStatistical.put("unit", unit);
		mapStatistical.put("recommend", recommend);
		mapStatistical.put("statisticalCNName", statisticalCNName[0]);
		
    	ReportQueryConditions queryConditions=(ReportQueryConditions)mapUnit.get(QueryUtil.QUERY_CONDITIONS_OBJ);
    	int len=-1;
	    if("TREND".equals(showType)){
	    	len=mapDatList.size();
	    }else {
	    	len=Math.min(queryConditions.getTopn(),mapDatList.size());
		}
		Object[]vals=new Object[len*statisticalsName.length];
		int pos=0;
		for(int columnIndex=0;columnIndex<statisticalsName.length;columnIndex++){
			for(int rowIndex=0;rowIndex<len;rowIndex++){
				Map mapdat=mapDatList.get(rowIndex);
				Object valObject=mapdat.get(statisticalsName[columnIndex]);
				valObject=null==valObject?"0":valObject;
				
				if (null != unit) {
					long val=Long.valueOf(valObject.toString());
					double vald=0;
					if ("FLOW_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.flowOperater(val, unit);
					}else if ("COUNT_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.showNumberOperater(val, unit);
					}
					vals[pos++]=vald;
					
				}else {
					vals[pos++]=valObject;
				}
			}
		}
		mapStatistical.put("datArray", vals);
		return mapStatistical;
	}
	@Deprecated
	public static Map<String, Object> getPieSeriesDat(Map mapUnit){
		Map<String, Object> seriesUnits=null;
		if (null == mapUnit || mapUnit.size()==0) {
			return seriesUnits;
		}
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []categorysCNName=(String [])dataStructureMap.get("categorysCNName");
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		if (null == list || list.size()==0) {
			return seriesUnits;
		}
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
    	if(colon>-1){
    		showType=showType.substring(0,colon);
    	}
    	int maplistlen=mapDatList.size();
    	long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
		
		for(int i=0;i<categorysCNName.length;i++){
			String caName = categorysCNName[i] ;
		}
		for(int i=0;i<statisticalCNName.length;i++){
			String stName = statisticalCNName[i] ;
			if (null != unit) {
				stName=stName+"("+unit+")";
			}
		}
		seriesUnits=new HashMap<String, Object>();
		List[]seriesArray=new ArrayList[statisticalsName.length];
		for(int index=0;index<statisticalsName.length;index++){
			seriesArray[index]=new ArrayList<Object>();
		}
		for(int rowIndex=0;rowIndex<maplistlen;rowIndex++){
			Map mapdat=mapDatList.get(rowIndex);
			Map[]seriesUnitsArray=new Map[statisticalsName.length];
			String field = mapdat.get(categorysName[0]).toString() ;
			if ("PRIORITY".equalsIgnoreCase(categorysName[0]) 
					|| "RISK".equals(categorysName[0])) {
				if (field.indexOf("危险")==-1) {
					int val=Integer.valueOf(field);
					field=ResultOperatorUtils.riskCnName(val);
				}
			}
			for(int index=0;index<statisticalsName.length;index++){
				seriesUnitsArray[index]=new HashMap<String, Object>();
				seriesUnitsArray[index].put("name", field);
			}
			for(int index=0;index<statisticalsName.length;index++){
				Object valObject=mapdat.get(statisticalsName[index]);
				valObject=null==valObject?"0":valObject;
				
				if (null != unit) {
					long val=Long.valueOf(valObject.toString());
					double vald=0;
					if ("FLOW_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.flowOperater(val, unit);
					}else if ("COUNT_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.showNumberOperater(val, unit);
					}
					seriesUnitsArray[index].put("value",vald);
					
				}else {
					seriesUnitsArray[index].put("value",valObject);
				}
				seriesArray[index].add(seriesUnitsArray[index]);
			}
		}
		for(int index=0;index<statisticalCNName.length;index++){
			seriesUnits.put(statisticalCNName[index], seriesArray[index]);
		}
		return seriesUnits;
	}
	public static String headlineByConditions(ReportQueryConditions queryConditions,ReportQuery reportQuery){
		List<Map<String, Object>> fieldList=null;
		Map<String, Object> templateMap=IndexTemplateUtil.getInstance().getTemplateByDeviceType(queryConditions.getSecurityObjectType());
		if (null != templateMap && templateMap.size() != 0)
			fieldList=(List<Map<String, Object>>)templateMap.get("fieldList");
		Integer[]parentIds=queryConditions.getParentIds();
		
		if (null == parentIds || 0==parentIds.length) {
			return "";
		}
		
		StringBuffer headlineBuffer=new StringBuffer();
		for (Integer integer : parentIds) {
			List<Map<String, Object>> queryMaps=reportQuery.findDetailSubThemeList(integer);
			Map<String, Object> queryMap = queryMaps.get(0);
			String reportName=(String)queryMap.get("parentReportName");
			int leftco=reportName.indexOf("{");
			int rightco=reportName.indexOf("}",leftco);
			if (-1 != leftco && -1 != rightco) {
				String needRepalce=reportName.substring(leftco+1,rightco);
				Object reval="";
				if (null != fieldList) {
					for (Map<String, Object> field : fieldList)
						if (field.get("name").equals(needRepalce)) 
							reval=field.get("alias");
					reportName=reportName.substring(0,leftco)+reval+reportName.substring(rightco+1);
					
				}
			}
			headlineBuffer.append(reportName);
			headlineBuffer.append("<br>").append("&");
		}
		return headlineBuffer.substring(0, headlineBuffer.length()-5);
	 }
	 
	 public static String reportDescByConditions(ReportQueryConditions queryConditions,ReportQuery reportQuery){
		 Integer[]parentIds=queryConditions.getParentIds();
			if (null == parentIds || 0==parentIds.length) {
				return "";
			}
			StringBuffer headlineBuffer=new StringBuffer();
			for (Integer integer : parentIds) {
				List<Map<String, Object>> queryMaps=reportQuery.findDetailSubThemeList(integer);
				Map<String, Object> queryMap = queryMaps.get(0);
				Object descObj=queryMap.get("parentDescribe");
				String desc=QueryUtil.replaceReportName(queryConditions.getSecurityObjectType(), descObj.toString());
				headlineBuffer.append(desc);
				headlineBuffer.append("<br>").append("&");
			}
			return headlineBuffer.substring(0, headlineBuffer.length()-5);
	 }
	private static void createChart(SimXWPFDocument doc,Map mapUnit,int reportOrder){
		
		List list=(List)(mapUnit.get(QueryUtil.RESULT_DATA));
		String dataStructureDesc=(String)mapUnit.get("dataStructureDesc");
		
		Map<String, Object> dataStructureMap=ResultOperatorUtils.datStructure(dataStructureDesc);
		String []categorysName=(String [])dataStructureMap.get("categorys");
		String []categorysCNName=(String [])dataStructureMap.get("categorysCNName");
		String []statisticalsName=(String [])dataStructureMap.get("statistical");
		String []statisticalCNName=(String [])dataStructureMap.get("statisticalCNName");
		String []statisticalType=(String [])dataStructureMap.get("statisticalType");
		
		if (null == list || list.size()==0) {
			return;
		}
		String showType=(String)mapUnit.get("showType");
		int colon=showType.indexOf(":");
	    String recommend=null;
	    if(colon>-1){
	    	recommend=showType.substring(showType.indexOf("(")+1, showType.indexOf(")") );
	    	showType=showType.substring(0,colon);
	    }
    	
		Object valueObject=list.get(0);
		List<Map<String, Object>>mapDatList=(List<Map<String,Object>>)valueObject;
		
		long maxVal=getMaxValueFromDat(showType,mapDatList,statisticalsName);
    	String unit=null;
    	unit=ResultOperatorUtils.getUnit(maxVal,statisticalType[0]);
    	
		addParagraph(doc,"   "+reportOrder+".3  详细内容统计图",12,0,false,ParagraphAlignment.LEFT) ;
		
		XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("chartData"+reportOrder);
        final int NUM_OF_ROWS = mapDatList.size()+1;
        final int NUM_OF_COLUMNS = categorysName.length+statisticalsName.length;
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
        
        //填充图表series列数据
        XSSFRow firstRow = sheet.getRow(0);
        for (int columnIndex = 0; columnIndex < categorysCNName.length; columnIndex++) {
        	firstRow.getCell(columnIndex).setCellValue(categorysCNName[columnIndex]) ;
		}
        for (int columnIndex = 0; columnIndex < statisticalCNName.length; columnIndex++) {
        	firstRow.getCell(columnIndex+categorysCNName.length).setCellValue(statisticalCNName[columnIndex]) ;
		}
        
        for(int rowIndex=0;rowIndex<mapDatList.size();rowIndex++){
        	Map mapdat=mapDatList.get(rowIndex);
        	//填充图表categories行数据
        	for (int columnIndex = 0; columnIndex < categorysName.length; columnIndex++) {
        		Object object=mapdat.get(categorysName[columnIndex]);
        		object= null==object ?"unknow":object;
        		String catName = object.toString() ;
        		if ("PRIORITY".equalsIgnoreCase(categorysName[columnIndex]) 
						|| "RISK".equals(categorysName[columnIndex])) {
					if (catName.indexOf("危险")==-1) {
						int val=Integer.valueOf(catName);
						catName=ResultOperatorUtils.riskCnName(val);
					}
				}
        		XSSFCell cell =sheet.getRow(rowIndex+1).getCell(columnIndex);
        		cell.setCellValue(catName) ;
			}
        	//填充图表series行数据
        	for (int columnIndex = 0; columnIndex < statisticalsName.length; columnIndex++) {
        		Object object=mapdat.get(statisticalsName[columnIndex]);
        		object= null==object ?0:object;
        		Long staVal = Long.valueOf(object.toString()) ;
        		XSSFCell cell =sheet.getRow(rowIndex+1).getCell(categorysName.length+columnIndex);
        		if (null != unit) {
					double vald=0;
					if ("FLOW_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.flowOperater(staVal, unit);
					}else if ("COUNT_NO".equalsIgnoreCase(statisticalType[0])) {
						vald=ResultOperatorUtils.showNumberOperater(staVal, unit);
					}
					cell.setCellValue(vald) ;
				}else {
					cell.setCellValue(staVal) ;
				}
			}
        }
    	
        XSSFChart chart;
		try {
			chart = doc.createChart(wb);
			ChartLegend legend  = chart.getOrCreateLegend();
	        legend.setPosition(LegendPosition.TOP_RIGHT);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
		unit=null == unit ?"":unit;
		if("TREND".equals(showType)){
    		recommend=(recommend==null)?"line":recommend;
    		createLineChartData(sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);
    	}else if("NOT_TREND".equals(showType)){
    		recommend=(recommend==null)?"bar":recommend;
    		if("standardbar".equals(recommend)
    				|| "rainbow".equals(recommend)
    				|| "bar".equals(recommend)){
    			createBarChartData(sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);
    		}else if("standardline".equals(recommend)){
    			createLineChartData(sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);
    		}else if("pie".equals(recommend)){
    			createPieChartData(sheet, chart, NUM_OF_ROWS,NUM_OF_COLUMNS,unit);
    		}else if("eventRiver".equals(recommend)){
    			createLineChartData(sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);
    		}else {
    			recommend=recommend.replace("standard", "");
    			recommend=recommend.replace("rainbow", "");
    			throw new RuntimeException("无效的图表类型:"+recommend);
    		}
    	}
        
	}
	private static ChartData createBarChartData(XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
        BarChartData data = dataFactory.createBarChartLapData();
        data.setUnit(unit) ;
        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN) ;
        fillChartData(sheet, chart, data, rows, columns) ;
        data.fillChart(chart, bottomAxis,leftAxis) ;
        return data ;
	}
	
	private static ChartData createLineChartData(XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
        LineChartData data = dataFactory.createLineChartData();
        data.setUnit(unit) ;
        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN) ;
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        fillChartData(sheet, chart, data, rows, columns) ;
        data.fillChart(chart, bottomAxis,leftAxis) ;
        return data ;
	}
	
	private static ChartData createPieChartData(XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
		PieChartData data = dataFactory.createPieChartData();
        //data.setUnit(unit) ;
		fillChartData(sheet, chart, data, rows, columns) ;
		data.fillChart(chart) ;
		return data ;
	}
	
	private static void fillChartData(XSSFSheet sheet,XSSFChart chart,ChartData data,int rows,int columns){
		ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, new CellRangeAddress(1, rows-1 , 0, 0));
        data.setCategories(xs) ;
//        data.setTitle("XXXXXXXXtu") ;
        for(int colIndex=1;colIndex < columns;colIndex++){
        	ChartDataSource<String> seriesName = DataSources.fromStringCellRange(sheet, new CellRangeAddress(0, 0, colIndex, colIndex)) ;
        	ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(1, rows-1, colIndex, colIndex));
        	data.addSeries(ys,seriesName);
        }
	}
}
