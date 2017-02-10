package com.topsec.tsm.sim.kb.web;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.topsec.tal.base.report.poi.SimXWPFDocument;
import com.topsec.tal.base.report.poi.XWPFUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.collector.datasource.DataSource;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.DeviceTypeShortKeyUtil;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.util.PinyingUtil;


@Controller
@RequestMapping("productsupport")
public class ProductsupportController {

	protected static Logger log= LoggerFactory.getLogger(ProductsupportController.class);

	@RequestMapping(value="showUI")
	public Object showUI(HttpServletRequest request){
		/* 获得资产列表集合 */
		Collection<DataSource> iter = DataSourceUtil.getDataSourceList(SimDatasource.DATASOURCE_TYPE_LOG);
		Map<String, String> typeMap = new HashMap<String, String>();
		Map<String, String> venderMap = new HashMap<String, String>();
		
		/* 将securityObjectType存入集合，并按拼音排序 */
		MyPinYinCompartor myPinYinCompartor = new MyPinYinCompartor();
		Map<String,Map<String,Set<String>>> allTypes = new TreeMap<String, Map<String,Set<String>>>(myPinYinCompartor) ;
		for(DataSource ds : iter){
			String[] tmp = ds.getSecurityObjectType().split("/", 3) ;
			String type = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(tmp[0]) ;
			typeMap.put(type, tmp[0].replace(" ", "_"));
			String vender = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(tmp[1]) ;
			venderMap.put(vender, tmp[1].replace(" ", "_"));
			String version = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(tmp[2].replace("/", " ")) ;
			Map<String, Set<String>> venders = allTypes.get(type) ;
			if(venders == null){
				venders = new TreeMap<String, Set<String>>(myPinYinCompartor) ;
				allTypes.put(type, venders) ;
			}
			Set<String> versions = venders.get(vender) ;
			Set<String> versionSet = new HashSet<String>();
			if(versions != null){
				versionSet.addAll(versions);
			}
			versionSet.add(version);
			venders.put(vender, versionSet);
		}
		request.setAttribute("typeMap",typeMap);
		request.setAttribute("venderMap",venderMap);
		request.setAttribute("allTypes",allTypes);

		return "/page/knowledge/productsupport" ;
	}
	@RequestMapping(value="exportExcelProductSupport", produces="text/html;charset=utf-8")
	public void exportExcelProductSupport(HttpServletRequest request,HttpServletResponse response){
		try {
			response.setContentType("application/vnd.ms-excel");
			String userAgent = request.getHeader("User-Agent") ;
			String fileName=URLEncoder.encode("", "UTF-8") + StringUtil.currentDateToString("yyyy-MM-dd")+"-"+(new Date()).getTime()+".xlsx";
			if(userAgent.indexOf("Firefox")>0){
				response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" +fileName + "\"");
			}else{
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName+"\"");
			}
			XSSFWorkbook  workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("设备支持列表");
			/* 合并单元格设置居中并加粗*/
			XSSFCellStyle cellStyle=workbook.createCellStyle();
			cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			sheet.addMergedRegion(new CellRangeAddress(0,1,0,2));
			XSSFCell cell = sheet.createRow(0).createCell(0);
			XSSFFont  fontStyle=workbook.createFont();
			fontStyle.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFont(fontStyle);
			
			cell.setCellValue("设备支持列表");
			cell.setCellStyle(cellStyle);
		    XSSFRow row = sheet.createRow(2);    
		    XSSFCell cell0 = row.createCell(0);
		    XSSFCell cell1 = row.createCell(1);   
		    XSSFCell cell2 = row.createCell(2);   
		    sheet.setColumnWidth(0, 10000);
			sheet.setColumnWidth(1, 10000);
			sheet.setColumnWidth(2, 10000);
	        cell0.setCellValue("类型");    
	        cell1.setCellValue("厂商");
	        cell2.setCellValue("版本");
	        XSSFCellStyle cellStyle2=workbook.createCellStyle();
	        XSSFFont  fontStyle2=workbook.createFont();
			fontStyle2.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			cellStyle2.setFont(fontStyle);
	        cell0.setCellStyle(cellStyle2);
	        cell1.setCellStyle(cellStyle2);
	        cell2.setCellStyle(cellStyle2);
	        int rows = 3;
	        boolean typeFlag = false;
	        boolean venderFlag = false;
	        Map<String,Map<String,Set<String>>> allTypes = getAllTypes(request);
			for (Entry<String, Map<String, Set<String>>> allType : allTypes.entrySet()) {
				   String type = allType.getKey();
				   typeFlag = true;
				   for(Entry<String, Set<String>> venders : allType.getValue().entrySet()){
					   String vender = venders.getKey();
					   venderFlag = true;
					   for (String version : venders.getValue()) { 
						   row = sheet.createRow(rows);
						   if(typeFlag == true){
							   row.createCell(0).setCellValue(type); 
							   typeFlag = false;
						   }
						   if(venderFlag == true){
							   row.createCell(1).setCellValue(vender);
							   venderFlag = false;
						   }
						   row.createCell(2).setCellValue(version); 
						   rows++;
						}  
				   }
				   rows++;
			}
	        workbook.write(response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping(value="exportWordProductSupport", produces="text/html;charset=utf-8")
	public void exportWordProductSupport(HttpServletRequest request,HttpServletResponse response){
		try {
			response.setContentType("application/msword");
			String userAgent = request.getHeader("User-Agent") ;
			String fileName=URLEncoder.encode("", "UTF-8") + StringUtil.currentDateToString("yyyy-MM-dd")+"-"+(new Date()).getTime()+".docx";
			if(userAgent.indexOf("Firefox")>0){
				response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" +fileName + "\"");
			}else{
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName+"\"");
			}
			Map<String,Map<String,Set<String>>> allTypes = getAllTypes(request);
			int rows = 0;
			for (Entry<String, Map<String, Set<String>>> allType : allTypes.entrySet()) {
				   for(Entry<String, Set<String>> venders : allType.getValue().entrySet()){
					   for (String version : venders.getValue()) {  
						      rows++;
						}  
				   }
				   rows++;
			}
			SimXWPFDocument doc = new SimXWPFDocument();
			XWPFParagraph para = doc.createParagraph();
			para.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun run = para.createRun();
			run = para.createRun();  
			run.setFontSize(20);
			run.setBold(true);
			run.setText("设备支持列表"); 
			XWPFTable deviceTypeTable = doc.createTable(rows,3);
			XWPFUtil.setCellWidths(deviceTypeTable, 3000) ;
			XWPFTableRow deviceTypeRow = deviceTypeTable.getRow(0) ;
			/* 设置表头并加黑*/
			XWPFRun typeRun = deviceTypeRow.getCell(0).getParagraphs().get(0).createRun() ;
			typeRun.setBold(true) ;
			typeRun.setText("类型") ;
			XWPFRun venderRun = deviceTypeRow.getCell(1).getParagraphs().get(0).createRun() ;
			venderRun.setBold(true) ;
			venderRun.setText("厂商") ;
			XWPFRun versionRun = deviceTypeRow.getCell(2).getParagraphs().get(0).createRun() ;
			versionRun.setBold(true) ;
			versionRun.setText("类型") ;
			//控制行
			int row = 1;
			for (Entry<String, Map<String, Set<String>>> allType : allTypes.entrySet()) {
				   deviceTypeRow = deviceTypeTable.getRow(row) ;
				   String type = allType.getKey();
				   deviceTypeRow.getCell(0).setText(type) ;
				   for(Entry<String, Set<String>> venders : allType.getValue().entrySet()){
					   String vender = venders.getKey();
					   deviceTypeRow = deviceTypeTable.getRow(row) ;
					   deviceTypeRow.getCell(1).setText(vender) ;
					   for (String version : venders.getValue()) {  
						      deviceTypeRow = deviceTypeTable.getRow(row) ;
						      deviceTypeRow.getCell(2).setText(version) ;
						      row++;
						}  
				   }
				   row++;
			}
			doc.write(response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping(value="productsupportHelp", produces="text/html;charset=utf-8")
	public Object productsupportHelp(HttpServletRequest request){
		String requestName = request.getParameter("name");
		String asset = request.getParameter("asset");
		
		if(StringUtils.isNotBlank(asset)) {
			request.setAttribute("asset", asset);
		}
		
		if(StringUtils.isNotBlank(requestName)) {
			String name = StringUtil.recode(requestName);
			request.setAttribute("name", name);
		}
		
		return "/page/knowledge/logDataSource/main" ;
	}
	
	/* 获取设备支持列表中的信息*/
	public static Map<String,Map<String,Set<String>>> getAllTypes(HttpServletRequest request){
		/* 获得资产列表集合 */
		Collection<DataSource> iter = DataSourceUtil.getDataSourceList(SimDatasource.DATASOURCE_TYPE_LOG);
		Map<String, String> typeMap = new HashMap<String, String>();
		Map<String, String> venderMap = new HashMap<String, String>();
		
		/* 将securityObjectType存入集合，并按拼音排序 */
		MyPinYinCompartor myPinYinCompartor = new MyPinYinCompartor();
		Map<String,Map<String,Set<String>>> allTypes = new TreeMap<String, Map<String,Set<String>>>(myPinYinCompartor) ;
		for(DataSource ds : iter){
			String[] tmp = ds.getSecurityObjectType().split("/", 3) ;
			String type = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(tmp[0]) ;
			typeMap.put(type, tmp[0].replace(" ", "_"));
			String vender = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(tmp[1]) ;
			venderMap.put(vender, tmp[1].replace(" ", "_"));
			String version = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(tmp[2].replace("/", " ")) ;
			Map<String, Set<String>> venders = allTypes.get(type) ;
			if(venders == null){
				venders = new TreeMap<String, Set<String>>(myPinYinCompartor) ;
				allTypes.put(type, venders) ;
			}
			Set<String> versions = venders.get(vender) ;
			Set<String> versionSet = new HashSet<String>();
			if(versions != null){
				versionSet.addAll(versions);
			}
			versionSet.add(version);
			venders.put(vender, versionSet);
		}
		request.setAttribute("typeMap",typeMap);
		request.setAttribute("venderMap",venderMap);
		request.setAttribute("allTypes",allTypes);
		return allTypes;
	}

}

class MyPinYinCompartor implements Comparator<String> {

	private static final String TOPSEC = "天融信";
	private static final String Unknown = "Unknown";

	@Override
	public int compare(String s1, String s2) {

		if (Unknown.equalsIgnoreCase(s1)) {
			return 1;
		} else if (Unknown.equalsIgnoreCase(s2)) {
			return -1;
		}

		if (!TOPSEC.equalsIgnoreCase(s1) && !TOPSEC.equalsIgnoreCase(s2)) {
			String[] o1PinyinArray = PinyingUtil.stringToPinyin(s1);
			String[] o2PinyinArray = PinyingUtil.stringToPinyin(s2);
			if (o1PinyinArray != null && o2PinyinArray != null) {
				String o1Pinyin = PinyingUtil.stringArrayToString(o1PinyinArray);
				String o2Pinyin = PinyingUtil.stringArrayToString(o2PinyinArray);
				int i = o1Pinyin.compareToIgnoreCase(o2Pinyin);
				return i;
			} else {
				return 0;
			}
		} else {
			if (s1.equalsIgnoreCase(s2)) {
				return 0;
			} else if (TOPSEC.equalsIgnoreCase(s1)) {
				return -1;
			} else if (TOPSEC.equalsIgnoreCase(s2)) {
				return 1;
			}
		}
		return 0;
	}
}